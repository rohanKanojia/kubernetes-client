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

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSet;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSetBuilder;
import io.fabric8.kubernetes.api.model.extensions.ReplicaSetList;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class ReplicaSetIT {
  @ArquillianResource
  KubernetesClient client;

  @ArquillianResource
  Session session;

  private ReplicaSet replicaset1;

  private String currentNamespace;

  @Before
  public void init() {
    currentNamespace = session.getNamespace();
    Map<String, Quantity> requests = new HashMap<>();
    requests.put("cpu", new Quantity("100m"));
    requests.put("memory", new Quantity("100Mi"));

    List<EnvVar> envVarList = new ArrayList<>();
    envVarList.add(new EnvVar("name", "GET_HOSTS_FROM", null));
    envVarList.add(new EnvVar("value", "dns", null));

    replicaset1 = new ReplicaSetBuilder()
      .withNewMetadata()
      .withName("replicaset1")
      .addToLabels("app", "guestbook")
      .addToLabels("tier", "frontend")
      .endMetadata()
      .withNewSpec()
      .withReplicas(3)
      .withNewSelector()
      .withMatchLabels(Collections.singletonMap("tier", "frontend"))
      .endSelector()
      .withNewTemplate()
      .withNewMetadata()
      .addToLabels("app", "guestbook")
      .addToLabels("tier", "frontend")
      .endMetadata()
      .withNewSpec()
      .addNewContainer()
      .withName("php-redis")
      .withImage("kubernetes/example-guestbook-php-redis")
      .withNewResources()
      .withRequests(requests)
      .endResources()
      .withEnv(envVarList)
      .addNewPort()
      .withContainerPort(80)
      .endPort()
      .endContainer()
      .endSpec()
      .endTemplate()
      .endSpec()
      .build();

    client.extensions().replicaSets().inNamespace(currentNamespace).createOrReplace(replicaset1);
  }

  @Test
  public void load() {
    String currentNamespace = session.getNamespace();
    ReplicaSet replicaSet = client.extensions().replicaSets().inNamespace(currentNamespace)
      .load(getClass().getResourceAsStream("/test-replicaset.yml")).get();
    assertThat(replicaSet).isNotNull();
    assertEquals("frontend", replicaSet.getMetadata().getName());
  }

  @Test
  public void get() {
    replicaset1 = client.extensions().replicaSets().inNamespace(currentNamespace).withName("replicaset1").get();
    assertNotNull(replicaset1);
  }

  @Test
  public void list() {
    ReplicaSetList replicaSetList = client.extensions().replicaSets().inNamespace(currentNamespace).list();
    assertThat(replicaSetList).isNotNull();
    assertTrue(replicaSetList.getItems().size() >= 1);
  }

  @Test
  public void update() {
    replicaset1 = client.extensions().replicaSets().inNamespace(currentNamespace).withName("replicaset1").edit()
      .editSpec().withReplicas(5).endSpec().done();
    assertThat(replicaset1).isNotNull();
    assertEquals(5, replicaset1.getSpec().getReplicas().intValue());
  }

  @Test
  public void delete() {
    boolean bDeleted = client.extensions().replicaSets().inNamespace(currentNamespace).withName("replicaset1").delete();
    assertTrue(bDeleted);
  }

  @After
  public void cleanup() throws InterruptedException {
    client.extensions().replicaSets().inNamespace(currentNamespace).delete();
    // Wait for resources to get destroyed
    Thread.sleep(2000);
  }
}
