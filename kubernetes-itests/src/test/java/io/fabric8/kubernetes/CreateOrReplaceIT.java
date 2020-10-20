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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServiceSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class CreateOrReplaceIT {
  @ArquillianResource
  KubernetesClient client;

  @ArquillianResource
  Session session;

  @Test
  public void testCreateOrReplaceConfigMap() {
    ConfigMap configMap = new ConfigMapBuilder()
      .withNewMetadata().withName(getTestResourcePrefix() + "-configmap").endMetadata()
      .addToData("first", "1st")
      .build();

    // 1st createOrReplace(); should create the resource
    configMap = client.configMaps().inNamespace(session.getNamespace()).createOrReplace(configMap);
    assertNotNull(configMap);
    assertEquals(getTestResourcePrefix() + "-configmap", configMap.getMetadata().getName());
    assertEquals("1st", configMap.getData().get("first"));

    // Modify resource
    Map<String, String> data = configMap.getData();
    data.put("second", "2nd");
    data.put("third", "3rd");
    configMap.setData(data);

    // 2nd createOrReplace(); should update the resource
    configMap = client.configMaps().inNamespace(session.getNamespace()).createOrReplace(configMap);
    assertNotNull(configMap);
    assertEquals("2nd", configMap.getData().get("second"));
    assertEquals("3rd", configMap.getData().get("third"));

    // Cleanup
    client.configMaps().inNamespace(session.getNamespace()).withName(configMap.getMetadata().getName()).delete();
  }

  @Test
  public void testCreateOrReplaceService() {
    Service service = new ServiceBuilder()
      .withNewMetadata().withName(getTestResourcePrefix() + "-svc").endMetadata()
      .withNewSpec()
      .addToSelector("app", "testapp")
      .addNewPort()
      .withProtocol("TCP")
      .withPort(80)
      .withTargetPort(new IntOrString(9376))
      .endPort()
      .endSpec()
      .build();

    // 1st createOrReplace(); should create the resource
    service = client.services().inNamespace(session.getNamespace()).createOrReplace(service);
    assertNotNull(service);
    assertEquals(getTestResourcePrefix() + "-svc", service.getMetadata().getName());
    assertEquals(1, service.getSpec().getPorts().size());
    assertEquals(9376, service.getSpec().getPorts().get(0).getTargetPort().getIntVal().intValue());

    // Modify resource
    ServiceSpec serviceSpec = service.getSpec();
    // https://github.com/fabric8io/kubernetes-client/issues/2399
    service.getSpec().setClusterIP(null);
    List<ServicePort> ports = serviceSpec.getPorts();
    ports.get(0).setTargetPort(new IntOrString(9090));

    // 2nd createOrReplace(); should update the resource
    service = client.services().inNamespace(session.getNamespace()).createOrReplace(service);
    assertNotNull(service);
    assertEquals(9090, service.getSpec().getPorts().get(0).getTargetPort().getIntVal().intValue());

    // Cleanup
    client.services().inNamespace(session.getNamespace()).withName(service.getMetadata().getName()).delete();
  }

  @Test
  public void testCreateOrReplaceDeployment() {
    Deployment deployment = new DeploymentBuilder()
      .withNewMetadata()
      .withName(getTestResourcePrefix() + "-deploy")
      .addToLabels("app", "busybox")
      .endMetadata()
      .withNewSpec()
      .withReplicas(1)
      .withNewSelector()
      .addToMatchLabels("app", "busybox")
      .endSelector()
      .withNewTemplate()
      .withNewMetadata().addToLabels("app", "busybox").endMetadata()
      .withNewSpec()
      .addNewContainer()
      .withName("busybox")
      .withImage("busybox:1.32.0")
      .withCommand("sleep", "10")
      .withImagePullPolicy("IfNotPresent")
      .endContainer()
      .withRestartPolicy("Always")
      .endSpec()
      .endTemplate()
      .endSpec()
      .build();

    // 1st createOrReplace(); should create the resource
    deployment = client.apps().deployments().inNamespace(session.getNamespace()).createOrReplace(deployment);
    assertNotNull(deployment);
    assertEquals(getTestResourcePrefix() + "-deploy", deployment.getMetadata().getName());
    assertEquals(1, deployment.getSpec().getTemplate().getSpec().getContainers().size());
    assertEquals("busybox:1.32.0", deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

    // Modify resource
    deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage("busybox:1.32");

    // 2nd createOrReplace(); should update the resource
    deployment = client.apps().deployments().inNamespace(session.getNamespace()).createOrReplace(deployment);
    assertNotNull(deployment);
    assertEquals("busybox:1.32", deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

    // Cleanup
    client.apps().deployments().inNamespace(session.getNamespace()).withName(deployment.getMetadata().getName()).delete();
  }

  @Test
  public void testCreateOrReplaceSecret() {
    Secret secret = new SecretBuilder()
      .withNewMetadata().withName(getTestResourcePrefix() + "-secret").endMetadata()
      .addToData("username", "ferferfe")
      .addToData("password", "MWYyZDFlMmU2N2Rm")
      .build();

    // 1st createOrReplace(); should create the resource
    secret = client.secrets().inNamespace(session.getNamespace()).createOrReplace(secret);
    assertNotNull(secret);
    assertEquals(getTestResourcePrefix() + "-secret", secret.getMetadata().getName());
    assertEquals("ferferfe", secret.getData().get("username"));
    assertEquals("MWYyZDFlMmU2N2Rm", secret.getData().get("password"));

    // Modify resource
    Map<String, String> data = secret.getData();
    data.put("apitoken", "c29tZXRva2Vu");

    // 2nd createOrReplace; should update the resource
    secret = client.secrets().inNamespace(session.getNamespace()).createOrReplace(secret);
    assertNotNull(secret);
    assertEquals("c29tZXRva2Vu", secret.getData().get("apitoken"));

    // Cleanup
    client.secrets().inNamespace(session.getNamespace()).withName(secret.getMetadata().getName()).delete();
  }

  @Test
  public void testCreateOrReplaceIngress() {
    Ingress ingress = new IngressBuilder()
      .withNewMetadata()
      .withName(getTestResourcePrefix() + "-ing")
      .endMetadata()
      .withNewSpec()
      .addNewRule()
      .withNewHttp()
      .addNewPath()
      .withPath("/testPath")
      .withPathType("Prefix")
      .withNewBackend()
      .withServiceName("test")
      .withServicePort(new IntOrString(80))
      .endBackend()
      .endPath()
      .endHttp()
      .endRule()
      .endSpec()
      .build();

    // 1st createOrReplace(); should create the resource
    ingress = client.extensions().ingresses().inNamespace(session.getNamespace()).createOrReplace(ingress);
    assertNotNull(ingress);
    assertEquals(getTestResourcePrefix() + "-ing", ingress.getMetadata().getName());
    assertEquals(1, ingress.getSpec().getRules().size());

    // Modify resource
    ingress.getMetadata().setAnnotations(Collections.singletonMap("nginx.ingress.kubernetes.io/rewrite-target", "/"));

    // 2nd createOrReplace; should update the resource
    ingress = client.extensions().ingresses().inNamespace(session.getNamespace()).createOrReplace(ingress);
    assertNotNull(ingress);
    assertEquals("/", ingress.getMetadata().getAnnotations().get("nginx.ingress.kubernetes.io/rewrite-target"));

    // Cleanup
    client.network().ingresses().inNamespace(session.getNamespace()).withName(ingress.getMetadata().getName()).delete();
  }

  @Test
  public void testCreateOrReplaceGenericResource() {
    // Given
    ConfigMap configMap = new ConfigMapBuilder()
      .withNewMetadata().withName("resource-cm-1").endMetadata()
      .addToData("a1", "A1")
      .addToData("a2", "A2")
      .build();

    // When
    ConfigMap createdResource = client.resource(configMap).inNamespace(session.getNamespace()).createOrReplace();
    configMap.setData(Collections.singletonMap("b1", "B1"));
    ConfigMap replacedResource = client.resource(configMap).inNamespace(session.getNamespace()).createOrReplace();

    // Then
    assertNotNull(createdResource);
    assertEquals(2, createdResource.getData().size());
    assertEquals("A1", createdResource.getData().get("a1"));
    assertEquals("A2", createdResource.getData().get("a2"));
    assertNotNull(replacedResource);
    assertEquals(1, replacedResource.getData().size());
    assertEquals("B1", replacedResource.getData().get("b1"));
    // Cleanup
    client.resource(configMap).inNamespace(session.getNamespace()).delete();
  }

  @Test
  public void testCreateOrReplaceGenericResourceList() {
    // Given
    InputStream resourceListV1 = getClass().getResourceAsStream("/createorreplace-it-testlist-v1.yml");
    InputStream resourceListV2 = getClass().getResourceAsStream("/createorreplace-it-testlist-v2.yml");

    // When
    List<HasMetadata> listCreated = client.load(resourceListV1).inNamespace(session.getNamespace()).createOrReplace();
    List<HasMetadata> listReplaced = client.load(resourceListV2).inNamespace(session.getNamespace()).createOrReplace();

    // Then
    assertNotNull(listCreated);
    assertEquals(2, listCreated.size());
    Optional<HasMetadata> deploymentResult = listCreated.stream().filter(p -> p instanceof Deployment).findFirst();
    assertTrue(deploymentResult.isPresent());
    Deployment deployment = (Deployment) deploymentResult.get();
    assertEquals("nginx:1.16.1", deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

    assertNotNull(listReplaced);
    assertEquals(2, listReplaced.size());
    Optional<HasMetadata> deploymentV2Result = listReplaced.stream().filter(p -> p instanceof Deployment).findFirst();
    assertTrue(deploymentV2Result.isPresent());
    Deployment deploymentV2 = (Deployment) deploymentV2Result.get();
    assertEquals("nginx:stable-alpine", deploymentV2.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

    // Cleanup
    client.resourceList(listReplaced).inNamespace(session.getNamespace()).delete();
  }

  @Test
  public void testCreateOrReplaceDeletingExisting() {
    // Given
    List<HasMetadata> listToCreate = new ArrayList<>();
    listToCreate.add(new ConfigMapBuilder().withNewMetadata().withName("createorreplace-it-delete-existing-configmap").endMetadata()
      .addToData("A", "a")
      .addToData("B", "b")
      .build());
    listToCreate.add(new SecretBuilder().withNewMetadata().withName("createorreplace-it-delete-existing-secret").endMetadata()
      .addToData("USERNAME", "YWRtaW4=")
      .addToData("PASSWORD", "MWYyZDFlMmU2N2Rm")
      .build());

    // When
    List<HasMetadata> listCreated = client.resourceList(listToCreate).inNamespace(session.getNamespace()).createOrReplace();
    List<HasMetadata> listCreatedAfterDeletingExisting = client.resourceList(listToCreate)
      .inNamespace(session.getNamespace())
      .deletingExisting()
      .createOrReplace();

    // Then
    assertNotNull(listCreated);
    assertEquals(2, listCreated.size());
    listCreated.sort(Comparator.comparing(HasMetadata::getKind));

    assertNotNull(listCreatedAfterDeletingExisting);
    assertEquals(2, listCreatedAfterDeletingExisting.size());
    listCreatedAfterDeletingExisting.sort(Comparator.comparing(HasMetadata::getKind));
    assertEquals(listCreated.get(0).getMetadata().getName(), listCreatedAfterDeletingExisting.get(0).getMetadata().getName());
    assertNotEquals(listCreated.get(0).getMetadata().getUid(), listCreatedAfterDeletingExisting.get(0).getMetadata().getUid());
    assertEquals(listCreated.get(1).getMetadata().getName(), listCreatedAfterDeletingExisting.get(1).getMetadata().getName());
    assertNotEquals(listCreated.get(1).getMetadata().getUid(), listCreatedAfterDeletingExisting.get(1).getMetadata().getUid());

    // Cleanup
    client.resourceList(listCreatedAfterDeletingExisting).inNamespace(session.getNamespace()).delete();
  }

  private String getTestResourcePrefix() {
    return getClass().getSimpleName().toLowerCase();
  }
}
