package io.fabric8.kubernetes.client.dsl.internal;

import io.fabric8.kubernetes.api.model.policy.DoneablePodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudget;
import io.fabric8.kubernetes.api.model.policy.PodDisruptionBudgetList;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.HasMetadataOperation;
import okhttp3.OkHttpClient;

import java.util.Map;
import java.util.TreeMap;

public class PodDisruptionBudgetOperationsImpl extends HasMetadataOperation<PodDisruptionBudget, PodDisruptionBudgetList, DoneablePodDisruptionBudget, Resource<PodDisruptionBudget, DoneablePodDisruptionBudget>> {

  public PodDisruptionBudgetOperationsImpl(OkHttpClient client, Config config, String namespace) {
    this(client, config, "policy/v1beta1", namespace, null, true, null, null, false, -1, new TreeMap<String, String>(), new TreeMap<String, String>(), new TreeMap<String, String[]>(), new TreeMap<String, String[]>(), new TreeMap<String, String>());
  }

  public PodDisruptionBudgetOperationsImpl(OkHttpClient client, Config config, String apiVersion, String namespace, String name, Boolean cascading, PodDisruptionBudget item, String resourceVersion, Boolean reloadingFromServer, long gracePeriodSeconds, Map<String, String> labels, Map<String, String> labelsNot, Map<String, String[]> labelsIn, Map<String, String[]> labelsNotIn, Map<String, String> fields) {
    super(client, config, null, apiVersion, "poddisruptionbudgets", namespace, name, cascading, item, resourceVersion, reloadingFromServer, gracePeriodSeconds, labels, labelsNot, labelsIn, labelsNotIn, fields);
  }
}
