package io.fabric8.kubernetes.examples;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MultipleWatcherExample {
  private static final Logger logger = LoggerFactory.getLogger("MultipleWatcher::");
  private static final String NAMESPACE_PREFIX = "multi-";

  public static void main(String[] args) {
    try (OpenShiftClient client = new DefaultOpenShiftClient()) {
      for (int i = 10; i < 30; i++) {
        newPodWatcher(client, NAMESPACE_PREFIX + i);
        newConfigMapWatcher(client, NAMESPACE_PREFIX + i);
        newSecretWatcher(client, NAMESPACE_PREFIX + i);
//        newImageStreamWatcher(client, NAMESPACE_PREFIX + i);
        newRoleBindingWatcher(client, NAMESPACE_PREFIX + i);
      }

      TimeUnit.MINUTES.sleep(30);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
  }
  private static void newSecretWatcher(OpenShiftClient client, String namespace) {
    logger.info("Starting watcher for Secret in {} ", namespace);
    client.secrets().inNamespace(namespace).watch(new GenericWatcher<>());
  }

  private static void newImageStreamWatcher(OpenShiftClient client, String namespace) {
    logger.info("Starting watcher for ImageStream in {} ", namespace);
    client.imageStreams().inNamespace(namespace).watch(new GenericWatcher<>());
  }

  private static void newRoleBindingWatcher(OpenShiftClient client, String namespace) {
    logger.info("Starting watcher for RoleBidning in {}", namespace);
    client.rbac().roleBindings().inNamespace(namespace).watch(new GenericWatcher<>());
  }

  private static void newConfigMapWatcher(OpenShiftClient client, String namespace) {
    logger.info("Starting watcher for ConfigMap in {} ", namespace);
    client.configMaps().inNamespace(namespace).watch(new GenericWatcher<>());
  }

  private static void newPodWatcher(OpenShiftClient client, String namespace) {
    logger.info("Starting watcher for Pod in {} ", namespace);
    client.pods().inNamespace(namespace).watch(new GenericWatcher<>());
  }

  private static class GenericWatcher<T extends HasMetadata> implements Watcher<T> {

    @Override
    public void eventReceived(Action action, T resource) {
      logger.info("{} {} {}/{}", action.name(), resource.getKind(), resource.getMetadata().getNamespace(), resource.getMetadata().getName());
    }

    @Override
    public void onClose(WatcherException cause) {
      logger.info("Closing Watch ::: {}", cause.getMessage());
    }
  }
}
