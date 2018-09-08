package io.fabric8.kubernetes.client.mock;

import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 * @author Sergii Leshchenko
 */
public class ExecTest {

  private Logger logger = Logger.getLogger(ExecTest.class.getName());

  public static final String MASTER_URL = "https://192.168.42.20:8443";

  interface ClientProvider {
    KubernetesClient getClient();
  }

  @Rule
  public KubernetesServer server = new KubernetesServer(false);

  private ClientProvider mockServerClientProvider = () -> server.getClient();

  private ClientProvider realServerClientProvider = () -> new DefaultKubernetesClient(
    new ConfigBuilder()
      .withMasterUrl(MASTER_URL)
      .build());

  @Test
  public void testMockServer()
    throws InterruptedException {
    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    logger.info("testConnectionLeaksAfterExecutingCommandInNonExistingPodOnMockServer");
    testConnectionLeaks(mockServerClientProvider);
  }

  @Test
  public void testRealServer()
    throws InterruptedException {
    //Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    logger.info("testConnectionLeaksAfterExecutingCommandInNonExistingPodOnRealServer");
    testConnectionLeaks(realServerClientProvider);
  }

  private void testConnectionLeaks(ClientProvider clientProvider) throws InterruptedException {

    KubernetesClient client = clientProvider.getClient();

    int i = 1;
    while (true) {
      logger.info("#######################################################");
      logger.info("####                                               ####");
      logger.info(
        "####                   " + String.format("Iteration #%3d", i++) + "              ####");
      logger.info("####                                               ####");
      logger.info("#######################################################");
      doIteration(client);
      System.gc();
    }
  }

  private void doIteration(KubernetesClient client) throws InterruptedException {
    logger.info("Try to execute 5 echo command in non-existing pod");
    doExec(client);
    doExec(client);
    doExec(client);
    doExec(client);
    doExec(client);

    logger.info("Wait 1 minute");
    Thread.sleep(60 * 1000); // 1 minutes
  }

  private void doExec(KubernetesClient client) {
    ExecWatchdog watchdog = new ExecWatchdog();
    try (ExecWatch exec =
      client
        .pods()
        .inNamespace("test")
        .withName("non-existing")
        .redirectingError()
        .usingListener(watchdog)
        .exec("sh", "-c", "echo hello")) {

      watchdog.wait(1, TimeUnit.MINUTES);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "ERROR: Exception occurred: " + e.getMessage());
    }
  }

  private class ExecWatchdog implements ExecListener {

    private final CompletableFuture<Void> completionFuture;

    private ExecWatchdog() {
      this.completionFuture = new CompletableFuture<>();
    }

    @Override
    public void onOpen(Response response) {
    }

    @Override
    public void onFailure(Throwable t, Response response) {
      completionFuture.completeExceptionally(t);
    }

    @Override
    public void onClose(int code, String reason) {
      completionFuture.complete(null);
    }

    public void wait(long timeout, TimeUnit timeUnit)
      throws InterruptedException {
      try {
        completionFuture.get(timeout, timeUnit);
      } catch (ExecutionException e) {
        throw new RuntimeException(
          "Error occured while executing command in pod: " + e.getMessage(), e);
      } catch (TimeoutException ignored) {
        throw new RuntimeException("Timeout reached while execution of command");
      }
    }
  }

}
