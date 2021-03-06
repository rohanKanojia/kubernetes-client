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

package io.fabric8.kubernetes.client.dsl;

import java.io.OutputStream;
import java.io.Reader;

/**
 * Loggable interface for all resources which produce logs
 *
 * @param <S> logs as plain string
 * @param <W> returns a LogWatch for watching logs
 */
public interface Loggable<S, W> {

  /**
   * Get logs of a resource
   *
   * @return logs as string
   */
  S getLog();

  /**
   * Get logs with pretty enabled
   *
   * @param isPretty whether we need logs with pretty output or not
   * @return logs as string
   */
  S getLog(Boolean isPretty);

  /**
   * Get a Reader for reading logs
   *
   * @return {@link Reader} log reader
   */
  Reader getLogReader();

  /**
   * Watch logs of a resource
   *
   * @return returns a Closeable interface for log watch
   */
  W watchLog();

  /**
   * Watch logs of resource and put them inside OutputStream inside
   *
   * @param out {@link OutputStream} for storing logs
   * @return returns a Closeable interface for log watch
   */
  W watchLog(OutputStream out);

  /**
   * While waiting for Pod logs, how long shall we wait until a Pod
   * becomes ready and starts producing logs
   *
   * @param logWaitTimeout timeout in milliseconds
   * @return {@link Loggable} for fetching logs
   */
  Loggable<S, W> withLogWaitTimeout(Integer logWaitTimeout);

}
