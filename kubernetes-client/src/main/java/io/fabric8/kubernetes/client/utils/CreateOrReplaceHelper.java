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

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.KubernetesClientException;

import java.net.HttpURLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CreateOrReplaceHelper<T extends HasMetadata> {
  public static final int CREATE_OR_REPLACE_RETRIES = 3;
  private final Supplier<T> createTask;
  private final Supplier<T> replaceTask;
  private final Supplier<T> waitTask;
  private final Supplier<Boolean> deleteTask;
  private final Supplier<T> reloadTask;

  public CreateOrReplaceHelper(Supplier<T> createTask, Supplier<T> replaceTask, Supplier<T> waitTask, Supplier<Boolean> deleteTask, Supplier<T> reloadTask) {
    this.createTask = createTask;
    this.replaceTask = replaceTask;
    this.waitTask = waitTask;
    this.deleteTask = deleteTask;
    this.reloadTask = reloadTask;
  }

  public T createOrReplace(T item, boolean deletingExisting) {
    String resourceVersion = KubernetesResourceUtil.getResourceVersion(item);
    final CompletableFuture<T> future = new CompletableFuture<>();
    int nTries = 0;
    while (!future.isDone() && nTries < CREATE_OR_REPLACE_RETRIES) {
      try {
        // Create
        KubernetesResourceUtil.setResourceVersion(item, null);
        return createTask.get();
      } catch (KubernetesClientException exception) {
        if (Utils.isHttpStatusCodeFromErrorEncounteredByServer(exception.getCode())) {
          T itemFromServer = reloadTask.get();
          if (itemFromServer == null) {
            waitTask.get();
            nTries++;
            continue;
          }
        } else if (exception.getCode() != HttpURLConnection.HTTP_CONFLICT) {
          throw exception;
        }

        future.complete(checkDeletingExistingOrReplace(item, deletingExisting, resourceVersion));
      }
    }
    return future.join();
  }

  private T handleDeletingExistingAndCreate(T item) {
    Boolean deleted = deleteTask.get();
    if (Boolean.FALSE.equals(deleted)) {
      throw new KubernetesClientException("Failed to delete existing item:" + item.getMetadata().getName());
    }
    return createTask.get();
  }

  private T checkDeletingExistingOrReplace(T item, boolean deletingExisting, String resourceVersion) {
    if (Boolean.TRUE.equals(deletingExisting)) {
      return handleDeletingExistingAndCreate(item);
    } else {
      KubernetesResourceUtil.setResourceVersion(item, resourceVersion);
      return replaceTask.get();
    }
  }
}
