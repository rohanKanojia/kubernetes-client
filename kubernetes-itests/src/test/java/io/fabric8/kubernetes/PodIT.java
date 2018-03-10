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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.commons.lang.RandomStringUtils;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class PodIT {

  @ArquillianResource
  public KubernetesClient client;

  @ArquillianResource
  public Session session;

  private Pod pod1;

  private String currentNamespace;

  private static final Logger logger = LoggerFactory.getLogger(PodIT.class);

  @Before
  public void init() throws InterruptedException {
    currentNamespace = session.getNamespace();
    pod1 = new PodBuilder()
      .withNewMetadata().withName("pod1-" + RandomStringUtils.randomAlphanumeric(6).toLowerCase()).endMetadata()
      .withNewSpec()
      .addNewContainer()
      .withName("mysql")
      .withImage("openshift/mysql-55-centos7")
      .addNewPort()
      .withContainerPort(3306)
      .endPort()
      .addNewEnv()
      .withName("MYSQL_ROOT_PASSWORD")
      .withValue("password")
      .endEnv()
      .addNewEnv()
      .withName("MYSQL_DATABASE")
      .withValue("foodb")
      .endEnv()
      .addNewEnv()
      .withName("MYSQL_USER")
      .withValue("luke")
      .endEnv()
      .addNewEnv()
      .withName("MYSQL_PASSWORD")
      .withValue("password")
      .endEnv()
      .endContainer()
      .endSpec()
      .build();

    client.pods().inNamespace(currentNamespace).createOrReplace(pod1);
  }

  @Test
  public void load() {
    Pod aPod = client.pods().inNamespace(currentNamespace).load(getClass().getResourceAsStream("/test-pod.yml")).get();
    assertThat(aPod).isNotNull();
    assertEquals("nginx", aPod.getMetadata().getName());
  }

  @Test
  public void get() {
    pod1 = client.pods().inNamespace(currentNamespace).withName(pod1.getMetadata().getName()).get();
    assertNotNull(pod1);
  }

  @Test
  public void list() {
    PodList podList = client.pods().inNamespace(currentNamespace).list();
    assertThat(podList).isNotNull();
    assertTrue(podList.getItems().size() >= 1);
  }

  @Test
  public void update() {
    pod1 = client.pods().inNamespace(currentNamespace).withName(pod1.getMetadata().getName()).edit()
      .editMetadata().addToLabels("foo", "bar").and().done();
    assertEquals("bar", pod1.getMetadata().getLabels().get("foo"));
  }

  @Test
  public void delete() {
    assertTrue(client.pods().inNamespace(currentNamespace).delete(pod1));
  }

  @After
  public void cleanup() throws InterruptedException {
    client.pods().inNamespace(currentNamespace).withName(pod1.getMetadata().getName()).delete();
    // Wait for resources to get destroyed
    Thread.sleep(30000);
  }
}
