package io.fabric8.kubernetes.client;


import okhttp3.OkHttpClient;

public class PolicyAPIGroupExtensionAdapter extends APIGroupExtensionAdapter<PolicyAPIGroupClient> {

  @Override
  protected String getAPIGroupName() {
    return "batch";
  }

  @Override
  public Class<PolicyAPIGroupClient> getExtensionType() {
    return PolicyAPIGroupClient.class;
  }

  @Override
  protected PolicyAPIGroupClient newInstance(Client client) {
    return new PolicyAPIGroupClient(client.adapt(OkHttpClient.class), client.getConfiguration());
  }
}
