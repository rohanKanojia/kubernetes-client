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
package io.fabric8.openshift;

import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.api.model.rbac.*;
import io.fabric8.kubernetes.client.RequestConfig;
import io.fabric8.openshift.api.model.ProjectRequest;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.client.OpenShiftClient;
import okhttp3.OkHttpClient;
import org.arquillian.cube.kubernetes.api.Session;
import org.arquillian.cube.openshift.impl.requirement.RequiresOpenshift;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(ArquillianConditionalRunner.class)
@RequiresOpenshift
public class UserImpersonationIT {

  private static final String SERVICE_ACCOUNT = "serviceaccount1";
  private static final String NEW_PROJECT = "impersonation" + System.nanoTime();

  @ArquillianResource
  OpenShiftClient client;

  @ArquillianResource
  Session session;

  private ServiceAccount serviceAccount1;
  private KubernetesClusterRole impersonatorRole;
  private KubernetesClusterRoleBinding impersonatorRoleBinding;

  private String currentNamespace;

  @Before
  public void init() {
    Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    currentNamespace = session.getNamespace();
    // Create impersonator cluster role
    impersonatorRole = new KubernetesClusterRoleBuilder()
      .withNewMetadata()
      .withName("impersonator")
      .endMetadata()
      .addToRules(new KubernetesPolicyRuleBuilder()
        .addToApiGroups("")
        .addToResources("users", "groups", "serviceaccounts")
        .addToVerbs("impersonate")
        .build()
      )
      .build();
    client.rbac().kubernetesClusterRoles().inNamespace(currentNamespace).createOrReplace(impersonatorRole);

    // Create Service Account
    serviceAccount1 = new ServiceAccountBuilder()
      .withNewMetadata().withName(SERVICE_ACCOUNT).endMetadata()
      .build();
    client.serviceAccounts().inNamespace(currentNamespace).create(serviceAccount1);

    // Bind Impersonator Role to current user
    impersonatorRoleBinding = new KubernetesClusterRoleBindingBuilder()
      .withNewMetadata()
      .withName("impersonate-role")
      .endMetadata()
      .addToSubjects(new KubernetesSubjectBuilder()
        .withApiGroup("rbac.authorization.k8s.io")
        .withKind("User")
        .withName(client.currentUser().getMetadata().getName())
        .withNamespace(currentNamespace)
        .build()
      )
      .withRoleRef(new KubernetesRoleRefBuilder()
        .withApiGroup("rbac.authorization.k8s.io")
        .withKind("ClusterRole")
        .withName("impersonator")
        .build()
      )
      .build();

    client.rbac().kubernetesClusterRoleBindings().inNamespace(currentNamespace).createOrReplace(impersonatorRoleBinding);
  }


  @Test
  public void should_be_able_to_return_service_account_name_when_impersonating_current_user() {
    RequestConfig requestConfig = client.getConfiguration().getRequestConfig();
    requestConfig.setImpersonateUsername(SERVICE_ACCOUNT);
    requestConfig.setImpersonateGroups("system:authenticated", "system:authenticated:oauth");

    User user = client.currentUser();
    assertThat(user.getMetadata().getName()).isEqualTo(SERVICE_ACCOUNT);
  }


  @Test
  public void should_be_able_to_create_a_project_impersonating_service_account() {
    RequestConfig requestConfig = client.getConfiguration().getRequestConfig();
    requestConfig.setImpersonateUsername(SERVICE_ACCOUNT);
    requestConfig.setImpersonateGroups("system:authenticated", "system:authenticated:oauth");
    // Create a project
    ProjectRequest projectRequest = client.projectrequests().createNew()
      .withNewMetadata()
      .withName(NEW_PROJECT)
      .endMetadata()
      .done();

    // Grab the requester annotation
    String requester = projectRequest.getMetadata().getAnnotations().get("openshift.io/requester");
    assertThat(requester).isEqualTo(SERVICE_ACCOUNT);
  }


  @After
  public void cleanup() {
    // Reset original authentication
    RequestConfig requestConfig = client.getConfiguration().getRequestConfig();
    requestConfig.setImpersonateUsername(null);
    requestConfig.setImpersonateGroups(null);

    // Delete Cluster Role
    client.rbac().kubernetesClusterRoles().inNamespace(currentNamespace).delete(impersonatorRole);
    await().atMost(30, TimeUnit.SECONDS).until(kubernetesClusterRoleIsDeleted());

    // Delete Cluster Role binding
    client.rbac().kubernetesClusterRoleBindings().inNamespace(currentNamespace).delete(impersonatorRoleBinding);
    await().atMost(30, TimeUnit.SECONDS).until(kubernetesClusterRoleBindingIsDeleted());

    // Delete project
    client.projects().withName(NEW_PROJECT).delete();
    await().atMost(30, TimeUnit.SECONDS).until(projectIsDeleted());

    // Delete ServiceAccounts
    client.serviceAccounts().inNamespace(currentNamespace).delete(serviceAccount1);
    await().atMost(30, TimeUnit.SECONDS).until(serviceAccountIsDeleted());
  }

  private Callable<Boolean> serviceAccountIsDeleted() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return client.serviceAccounts().inNamespace(currentNamespace).withName(serviceAccount1.getMetadata().getName()).get() == null;
      }
    };
  }

  private Callable<Boolean> projectIsDeleted() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return client.projects().withName(NEW_PROJECT).get() == null;
      }
    };
  }

  private Callable<Boolean> kubernetesClusterRoleBindingIsDeleted() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return client.rbac().kubernetesClusterRoleBindings().inNamespace(currentNamespace).withName("impersonator-role").get() == null;
      }
    };
  }

  private Callable<Boolean> kubernetesClusterRoleIsDeleted() {
    return new Callable<Boolean>() {
      @Override
      public Boolean call() {
        return client.rbac().kubernetesClusterRoles().inNamespace(currentNamespace).withName("impersonator").get() == null;
      }
    };
  }


}
