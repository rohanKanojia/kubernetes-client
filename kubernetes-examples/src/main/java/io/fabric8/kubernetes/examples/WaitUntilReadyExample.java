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
package io.fabric8.kubernetes.examples;

import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WaitUntilReadyExample {
  private static Logger logger = LoggerFactory.getLogger(WaitUntilReadyExample.class);

  public static void main(String args[]) throws IOException, InterruptedException {
    try (final KubernetesClient client = new DefaultKubernetesClient()) {
      Pod pod = new PodBuilder()
        .withNewMetadata().withName("p2").withLabels(Collections.singletonMap("app", "p2")).endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("myapp-container")
        .withImage("busybox:1.28")
        .withCommand("sh", "-c", "echo The app is running!; sleep 10")
        .endContainer()
        .addNewInitContainer()
        .withName("init-myservice")
        .withImage("busybox:1.28")
        .withCommand("sh", "-c", "echo inititalizing...; sleep 5")
        .endInitContainer()
        .endSpec()
        .build();

      Pod secondPod = new PodBuilder()
        .withNewMetadata().withName("p1").endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("myapp2-container")
        .withImage("busybox:1.28")
        .withCommand("sh", "-c", "echo The app is running!; sleep 10")
        .endContainer()
        .addNewInitContainer()
        .withName("init2-mypod")
        .withImage("busybox:1.28")
        .withCommand("sh", "-c", "echo initializing...; sleep 5")
        .endInitContainer()
        .endSpec()
        .build();

      String namespace = "default";
      pod = client.pods().inNamespace(namespace).create(pod);
      secondPod = client.pods().inNamespace(namespace).create(secondPod);
      log(new Date().toString() + ": Pod created, waiting for it to get ready");
      //client.resource(pod).inNamespace(namespace).waitUntilReady(10, TimeUnit.SECONDS);
      client.resourceList(new KubernetesListBuilder().withItems(pod, secondPod).build()).inNamespace(namespace).waitUntilReady(60, TimeUnit.SECONDS);
      log(new Date().toString() + ": Pods are ready now.");

      //LogWatch watch = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName()).watchLog(System.out);
      //watch.wait(10);

      client.pods().inNamespace("default").withName("p2").delete();
    }
  }

  private static void log(String action, Object obj) {
    logger.info("{}: {}", action, obj);
  }

  private static void log(String action) {
    logger.info(action);
  }
}
