package io.fabric8.kubernetes.client;

import io.fabric8.kubernetes.api.model.policy.DoneablePodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudgetList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PolicyAPIGroupDSL;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.internal.PodDisruptionBudgetOperationsImpl;
import okhttp3.OkHttpClient;

public class PolicyAPIGroupClient extends BaseClient implements PolicyAPIGroupDSL {
  public PolicyAPIGroupClient() throws KubernetesClientException {
    super();
  }

  public PolicyAPIGroupClient(OkHttpClient httpClient, final Config config) throws KubernetesClientException {
    super(httpClient, config);
  }

  @Override
  public MixedOperation<PodDisruptionBudget, PodDisruptionBudgetList, DoneablePodDisruptionBudget, Resource<PodDisruptionBudget, DoneablePodDisruptionBudget>> podDisruptionBudget() {
    return new PodDisruptionBudgetOperationsImpl(httpClient, getConfiguration(), getNamespace());
  }
}
