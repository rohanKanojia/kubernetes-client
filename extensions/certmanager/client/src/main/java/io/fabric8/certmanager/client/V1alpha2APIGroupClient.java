package io.fabric8.certmanager.client;

import io.fabric8.certmanager.api.model.acme.v1alpha2.Challenge;
import io.fabric8.certmanager.api.model.acme.v1alpha2.ChallengeList;
import io.fabric8.certmanager.api.model.acme.v1alpha2.Order;
import io.fabric8.certmanager.api.model.acme.v1alpha2.OrderList;
import io.fabric8.certmanager.api.model.v1alpha2.*;
import io.fabric8.certmanager.client.api.v1alpha2.internal.*;
import io.fabric8.certmanager.client.dsl.V1alpha2APIGroupDSL;
import io.fabric8.kubernetes.client.BaseClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import okhttp3.OkHttpClient;

public class V1alpha2APIGroupClient extends BaseClient implements V1alpha2APIGroupDSL {
  public V1alpha2APIGroupClient() {
    super();
  }

  public V1alpha2APIGroupClient(OkHttpClient httpClient, final Config config) {
    super(httpClient, config);
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
