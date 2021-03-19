package io.fabric8.certmanager.client;

import io.fabric8.certmanager.api.model.acme.v1alpha3.Challenge;
import io.fabric8.certmanager.api.model.acme.v1alpha3.ChallengeList;
import io.fabric8.certmanager.api.model.acme.v1alpha3.Order;
import io.fabric8.certmanager.api.model.acme.v1alpha3.OrderList;
import io.fabric8.certmanager.api.model.v1alpha3.*;
import io.fabric8.certmanager.client.api.v1alpha3.internal.*;
import io.fabric8.certmanager.client.dsl.V1alpha3APIGroupDSL;
import io.fabric8.kubernetes.client.BaseClient;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import okhttp3.OkHttpClient;

public class V1alpha3APIGroupClient extends BaseClient implements V1alpha3APIGroupDSL {
  public V1alpha3APIGroupClient() {
    super();
  }

  public V1alpha3APIGroupClient(OkHttpClient httpClient, final Config config) {
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
