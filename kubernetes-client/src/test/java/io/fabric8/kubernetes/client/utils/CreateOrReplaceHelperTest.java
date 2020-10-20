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
package io.fabric8.kubernetes.client.utils;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.StatusBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.HttpURLConnection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CreateOrReplaceHelperTest {

  @Test
  void testCreateOrReplaceShouldCreate() {
    // Given
    AtomicBoolean wasPodCreated = new AtomicBoolean(false);
    Supplier<Pod> createPodTask = () -> {
      wasPodCreated.set(true);
      return getPod();
    };
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(
      createPodTask,
      this::getPod,
      this::getPod,
      () -> true,
      this::getPod
    );

    // When
    Pod podCreated = podCreateOrReplaceHelper.createOrReplace(getPod(), false);

    // Then
    assertNotNull(podCreated);
    assertTrue(wasPodCreated.get());
  }

  @Test
  void testCreateOrReplaceShouldReplace() {
    // Given
    AtomicBoolean wasPodReplaced = new AtomicBoolean(false);
    Supplier<Pod> createPodTask = () -> {
      throw new KubernetesClientException("Already exist",
        HttpURLConnection.HTTP_CONFLICT, new StatusBuilder().withCode(HttpURLConnection.HTTP_CONFLICT).build());
    };
    Supplier<Pod> replacePodTask = () -> {
      wasPodReplaced.set(true);
      return getPod();
    };
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(
      createPodTask,
      replacePodTask,
      this::getPod,
      () -> true,
      this::getPod
    );

    // When
    Pod podCreated = podCreateOrReplaceHelper.createOrReplace(getPod(), false);

    // Then
    assertNotNull(podCreated);
    assertTrue(wasPodReplaced.get());
  }

  @Test
  void testCreateOrReplaceShouldRetryOnInternalServerError() {
    // Given
    AtomicBoolean waitedForPod = new AtomicBoolean(false);
    Supplier<Pod> createPodTask = Mockito.mock(Supplier.class, Mockito.RETURNS_DEEP_STUBS);
    Supplier<Pod> reloadTask = Mockito.mock(Supplier.class, Mockito.RETURNS_DEEP_STUBS);
    when(reloadTask.get()).thenReturn(null);
    when(createPodTask.get()).thenThrow(new KubernetesClientException("The POST operation could not be completed at " +
      "this time, please try again",
      HttpURLConnection.HTTP_INTERNAL_ERROR, new StatusBuilder().withCode(HttpURLConnection.HTTP_INTERNAL_ERROR).build()))
      .thenReturn(getPod());
    Supplier<Pod> waitTask = () -> {
      waitedForPod.set(true);
      return getPod();
    };
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(
      createPodTask,
      this::getPod,
      waitTask,
      () -> true,
      reloadTask
    );

    // When
    Pod podCreated = podCreateOrReplaceHelper.createOrReplace(getPod(), false);

    // Then
    assertNotNull(podCreated);
    assertTrue(waitedForPod.get());
  }

  @Test
  void testCreateOrReplaceThrowExceptionOnErrorCodeLessThan500() {
    // Given
    Supplier<Pod> createPodTask = () -> {
      throw new KubernetesClientException("The POST operation could not be completed at " +
        "this time, please try again",
        HttpURLConnection.HTTP_BAD_REQUEST, new StatusBuilder().withCode(HttpURLConnection.HTTP_BAD_REQUEST).build());
    };
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(createPodTask,
      this::getPod, this::getPod, () -> true, this::getPod);
    Pod podToCreate = getPod();

    // When
    assertThrows(KubernetesClientException.class, () -> podCreateOrReplaceHelper.createOrReplace(podToCreate, false));
  }

  @Test
  void testCreateOrReplaceDeleteExistingEnabled() {
    // Given
    AtomicBoolean wasPodDeleted = new AtomicBoolean(false);
    Supplier<Boolean> deletePodTask = () -> {
      wasPodDeleted.set(true);
      return true;
    };
    Supplier<Pod> createPodTask = Mockito.mock(Supplier.class, Mockito.RETURNS_DEEP_STUBS);
    when(createPodTask.get()).thenThrow(new KubernetesClientException("The POST operation could not be completed at " +
      "this time, please try again",
      HttpURLConnection.HTTP_CONFLICT, new StatusBuilder().withCode(HttpURLConnection.HTTP_CONFLICT).build()))
      .thenReturn(getPod());
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(
      createPodTask,
      this::getPod,
      this::getPod,
      deletePodTask,
      this::getPod
    );

    // When
    Pod podCreated = podCreateOrReplaceHelper.createOrReplace(getPod(), true);

    // Then
    assertNotNull(podCreated);
    assertTrue(deletePodTask.get());
  }

  @Test
  void testCreateOrReplaceWithDeletingExistingWhenDeletionFailed() {
    // Given
    Supplier<Pod> createPodTask = Mockito.mock(Supplier.class, Mockito.RETURNS_DEEP_STUBS);
    when(createPodTask.get()).thenThrow(new KubernetesClientException("The POST operation could not be completed at " +
      "this time, please try again",
      HttpURLConnection.HTTP_CONFLICT, new StatusBuilder().withCode(HttpURLConnection.HTTP_CONFLICT).build()));
    CreateOrReplaceHelper<Pod> podCreateOrReplaceHelper = new CreateOrReplaceHelper<>(
      createPodTask,
      this::getPod,
      this::getPod,
      () -> false,
      this::getPod
    );

    // When
    Pod podToCreateOrReplace = getPod();
    assertThrows(KubernetesClientException.class,() -> podCreateOrReplaceHelper.createOrReplace(podToCreateOrReplace, true));
  }

  private Pod getPod() {
    return new PodBuilder()
      .withNewMetadata().withName("p1").endMetadata()
      .build();
  }
}
