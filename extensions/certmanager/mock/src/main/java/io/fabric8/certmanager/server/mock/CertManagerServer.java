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
package io.fabric8.certmanager.server.mock;

import io.fabric8.kubernetes.client.server.mock.KubernetesCrudDispatcher;
import io.fabric8.mockwebserver.Context;
import io.fabric8.mockwebserver.dsl.MockServerExpectation;
import io.fabric8.certmanager.client.CertManagerClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.rules.ExternalResource;

import java.util.HashMap;

public class CertManagerServer extends ExternalResource {

  protected CertManagerMockServer mock;
  private CertManagerClient client;

  private final boolean https;
  private final boolean crudMode;

  public CertManagerServer() {
    this(true, false);
  }

  public CertManagerServer(boolean https) {
    this(https, false);
  }

  public CertManagerServer(boolean https, boolean crudMode) {
    this.https = https;
    this.crudMode = crudMode;
  }

  @Override
  public void before() {
    mock = crudMode
      ? new CertManagerMockServer(new Context(), new MockWebServer(), new HashMap<>(), new KubernetesCrudDispatcher(), true)
      : new CertManagerMockServer(https);
    mock.init();
    client = mock.createCertManager();
  }

  @Override
  public void after() {
    mock.destroy();
    client.close();
  }

  public CertManagerClient get() {
    return client;
  }

  public MockServerExpectation expect() {
    return mock.expect();
  }
}
