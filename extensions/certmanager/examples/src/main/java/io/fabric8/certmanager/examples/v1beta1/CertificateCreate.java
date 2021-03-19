package io.fabric8.certmanager.examples.v1beta1;

import io.fabric8.certmanager.api.model.v1beta1.Certificate;
import io.fabric8.certmanager.api.model.v1beta1.CertificateBuilder;
import io.fabric8.certmanager.api.model.v1beta1.CertificateList;
import io.fabric8.certmanager.client.DefaultCertManagerClient;
import io.fabric8.certmanager.client.NamespacedCertManagerClient;

public class CertificateCreate {
  public static void main(String[] args) {
    try (NamespacedCertManagerClient certManagerClient = new DefaultCertManagerClient()) {
      String namespace = "default";

      Certificate certificate = new CertificateBuilder()
        .build();

      // Create Certificate
      certManagerClient.v1beta1().certificates().inNamespace(namespace).create(certificate);
      System.out.println("Created: " + certificate.getMetadata().getName());

      // List Certificate
      CertificateList certificateList = certManagerClient.v1beta1().certificates().inNamespace(namespace).list();
      System.out.println("There are " + certificateList.getItems().size() + " TaskRun objects in " + namespace);
    }
  }
}
