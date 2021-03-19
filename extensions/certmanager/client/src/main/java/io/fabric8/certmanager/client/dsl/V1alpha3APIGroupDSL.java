package io.fabric8.certmanager.client.dsl;

import io.fabric8.certmanager.api.model.acme.v1alpha3.Challenge;
import io.fabric8.certmanager.api.model.acme.v1alpha3.ChallengeList;
import io.fabric8.certmanager.api.model.acme.v1alpha3.Order;
import io.fabric8.certmanager.api.model.acme.v1alpha3.OrderList;
import io.fabric8.certmanager.api.model.v1alpha3.*;
import io.fabric8.kubernetes.client.Client;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public interface V1alpha3APIGroupDSL extends Client {
  MixedOperation<Certificate, CertificateList, Resource<Certificate>> certificates();
  MixedOperation<CertificateRequest, CertificateRequestList, Resource<CertificateRequest>> certificateRequests();
  MixedOperation<Issuer, IssuerList, Resource<Issuer>> issuers();
  NonNamespaceOperation<ClusterIssuer, ClusterIssuerList, Resource<ClusterIssuer>> clusterIssuers();
  MixedOperation<Challenge, ChallengeList, Resource<Challenge>> challenges();
  MixedOperation<Order, OrderList, Resource<Order>> orders();
}
