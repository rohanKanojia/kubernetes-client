/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import okhttp3.OkHttpClient;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class DeploymentIT {
  @ArquillianResource
  KubernetesClient client;

  @ArquillianResource
  Session session;

  private Deployment deployment1;

  private String currentNamespace;

  @Before
  public void init() {

    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    currentNamespace = session.getNamespace();

    client.apps().deployments().inNamespace(currentNamespace).delete();
    client.pods().inNamespace(currentNamespace).delete();

    deployment1 = new DeploymentBuilder()
      .withNewMetadata()
        .withName("deployment1")
        .addToLabels("test", "deployment")
      .endMetadata()
      .withNewSpec()
        .withReplicas(1)
        .withNewTemplate()
          .withNewMetadata()
          .addToLabels("app", "httpd")
          .endMetadata()
          .withNewSpec()
            .addNewContainer()
              .withName("busybox")
              .withImage("busybox")
              .withCommand("sleep","36000")
            .endContainer()
          .endSpec()
        .endTemplate()
        .withNewSelector()
          .addToMatchLabels("app","httpd")
        .endSelector()
      .endSpec()
      .build();

    client.apps().deployments().inNamespace(currentNamespace).create(deployment1);
  }

  @Test
  public void load() {

    Deployment aDeployment = client.apps().deployments().inNamespace(currentNamespace).load(getClass().getResourceAsStream("/test-deployments.yml")).get();
    assertThat(aDeployment).isNotNull();
    assertEquals("nginx-deployment", aDeployment.getMetadata().getName());
  }

  @Test
  public void get() {
    deployment1 = client.apps().deployments().inNamespace(currentNamespace)
      .withName("deployment1").get();
    assertNotNull(deployment1);
  }

  @Test
  public void list() {
    DeploymentList aDeploymentList = client.apps().deployments().inNamespace(currentNamespace).list();
    assertThat(aDeploymentList).isNotNull();
    assertEquals(1, aDeploymentList.getItems().size());
  }

  @Test
  public void update() {
    deployment1 = client.apps().deployments().inNamespace(currentNamespace).withName("deployment1").edit()
      .editSpec().withReplicas(2).endSpec().done();
    assertThat(deployment1).isNotNull();
    assertEquals(2, deployment1.getSpec().getReplicas().intValue());
  }

  @Test
  public void delete() throws InterruptedException {
    // Usually creation, deletion of things like Deployments take some time. So let's wait for a while:
    // Wait for resources to get ready
    DeploymentReady deploymentReady = new DeploymentReady(client, "deployment1", currentNamespace);
    await().atMost(30, TimeUnit.MINUTES).until(deploymentReady);
    assertTrue(client.apps().deployments().inNamespace(currentNamespace).delete(deployment1));
  }

  @Test
  public void waitTest() throws InterruptedException {
    // Wait for resources to get ready
    DeploymentReady deploymentReady = new DeploymentReady(client, "deployment1", currentNamespace);
    await().atMost(30, TimeUnit.MINUTES).until(deploymentReady);
    Deployment deploymentOne = client.apps().deployments()
      .inNamespace(currentNamespace).withName("deployment1").get();
    assertTrue(Readiness.isDeploymentReady(deploymentOne));
  }

  @After
  public void cleanup() throws InterruptedException {
    try {
      if (client.apps().deployments().inNamespace(currentNamespace).list().getItems().size() != 0) {
        client.apps().deployments().inNamespace(currentNamespace).delete();
      }
      // Wait for resources to get destroyed
      DeploymentDelete deploymentDelete = new DeploymentDelete(client, "deployment1", currentNamespace);
      await().atMost(30, TimeUnit.MINUTES).until(deploymentDelete);
    } catch (NullPointerException exception){
      cleanup();
    }
  }

}
