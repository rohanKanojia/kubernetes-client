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
package io.fabric8.certmanager.client;

import io.fabric8.certmanager.api.model.acme.v1.Challenge;
import io.fabric8.certmanager.api.model.acme.v1.ChallengeList;
import io.fabric8.certmanager.api.model.acme.v1.Order;
import io.fabric8.certmanager.api.model.acme.v1.OrderList;
import io.fabric8.certmanager.api.model.v1.Certificate;
import io.fabric8.certmanager.api.model.v1.CertificateList;
import io.fabric8.certmanager.api.model.v1.CertificateRequest;
import io.fabric8.certmanager.api.model.v1.CertificateRequestList;
import io.fabric8.certmanager.api.model.v1.ClusterIssuer;
import io.fabric8.certmanager.api.model.v1.ClusterIssuerList;
import io.fabric8.certmanager.api.model.v1.Issuer;
import io.fabric8.certmanager.api.model.v1.IssuerList;
import io.fabric8.certmanager.client.api.v1.internal.CertificateOperationsImpl;
import io.fabric8.certmanager.client.api.v1.internal.CertificateRequestOperationsImpl;
import io.fabric8.certmanager.client.api.v1.internal.ChallengeOperationsImpl;
import io.fabric8.certmanager.client.api.v1.internal.ClusterIssuerOperationsImpl;
import io.fabric8.certmanager.client.api.v1.internal.IssuerOperationsImpl;
import io.fabric8.certmanager.client.api.v1.internal.OrderOperationsImpl;
import io.fabric8.kubernetes.client.BaseClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.kubernetes.client.WithRequestCallable;
import io.fabric8.kubernetes.client.dsl.FunctionCallable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import okhttp3.OkHttpClient;

public class DefaultCertManagerClient extends BaseClient implements NamespacedCertManagerClient {

  public DefaultCertManagerClient() {
    super();
  }

  public DefaultCertManagerClient(Config configuration) {
    super(configuration);
  }

  public DefaultCertManagerClient(OkHttpClient httpClient, Config configuration) {
    super(httpClient, configuration);
  }

  @Override
  public NamespacedCertManagerClient inAnyNamespace() {
    return inNamespace(null);
  }

  @Override
  public NamespacedCertManagerClient inNamespace(String namespace) {
    Config updated = new ConfigBuilder(getConfiguration())
      .withNamespace(namespace)
      .build();

    return new DefaultCertManagerClient(getHttpClient(), updated);
  }

  @Override
  public FunctionCallable<NamespacedCertManagerClient> withRequestConfig(RequestConfig requestConfig) {
    return new WithRequestCallable<>(this, requestConfig);
  }

  @Override
  public MixedOperation<Certificate, CertificateList, Resource<Certificate>> certificates() {
    return new CertificateOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }

  @Override
  public MixedOperation<CertificateRequest, CertificateRequestList, Resource<CertificateRequest>> certificateRequests() {
    return new CertificateRequestOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }

  @Override
  public MixedOperation<Issuer, IssuerList, Resource<Issuer>> issuers() {
    return new IssuerOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }

  @Override
  public NonNamespaceOperation<ClusterIssuer, ClusterIssuerList, Resource<ClusterIssuer>> clusterIssuers() {
    return new ClusterIssuerOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }

  @Override
  public MixedOperation<Challenge, ChallengeList, Resource<Challenge>> challenges() {
    return new ChallengeOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }

  @Override
  public MixedOperation<Order, OrderList, Resource<Order>> orders() {
    return new OrderOperationsImpl(this.getHttpClient(), this.getConfiguration());
  }
}
