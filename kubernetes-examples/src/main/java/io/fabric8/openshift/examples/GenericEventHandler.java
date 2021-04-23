package io.fabric8.openshift.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;

public class GenericEventHandler<T extends HasMetadata> implements ResourceEventHandler<T> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    public void onAdd(T obj) {
        String className = obj.getClass().getSimpleName().toLowerCase();
        ObjectMeta metadata = obj.getMetadata();
        String name = metadata.getName();
        String namespace = metadata.getNamespace();
        logger.info("{}/{}/{} added", namespace, className, name);
    }

    public void onUpdate(T obj, T newObj) {
        String className = obj.getClass().getSimpleName().toLowerCase();
        ObjectMeta metadata = obj.getMetadata();
        String name = metadata.getName();
        String namespace = metadata.getNamespace();
        logger.info("{}/{}/{} updated", namespace, className, name);
    }

    public void onDelete(T obj, boolean deletedFinalStateUnknown) {
        String className = obj.getClass().getSimpleName().toLowerCase();
        ObjectMeta metadata = obj.getMetadata();
        String name = metadata.getName();
        String namespace = metadata.getNamespace();
        logger.info("{}/{}/{} deleted", namespace, className, name);
    }

}
