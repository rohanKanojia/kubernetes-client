# Using KubernetesClient with IPv6 based Kubernetes Clusters

Right now Fabric8 Kubernetes Client doesn't work with IPv6 based Kubernetes Clusters due to an issue in OkHttp[square/okhttp#5889](https://github.com/square/okhttp/pull/5889). Fabric8 Kubernetes Client is right now dependent on OkHttp 3.x; unfortunately we can't upgrade to OkHttp 4.x due to it being now based on Kotlin, see [square/okhttp#4723](https://github.com/square/okhttp/issues/4723). We have decided not to upgrade to OkHttp 4.x. Until we find an alternative HTTP library for KubernetesClient, we suggest you to resolve this issue by excluding okhttp dependency coming from KubernetesClient jar and adding your own direct 4.x OkHttp dependencies:

```xml
<properties>
    <fabric8.version>5.5.0</fabric8.version>
    <okhttp.version>4.9.0</okhttp.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.fabric8</groupId>
        <artifactId>kubernetes-client</artifactId>
        <version>${fabric8.version}</version>
        <exclusions>
            <exclusion>
                <groupId>com.squareup.okhttp3</groupId>
                <artifactId>okhttp</artifactId>
            </exclusion>
            <exclusion>
                <groupId>com.squareup.okhttp3</groupId>
                <artifactId>logging-interceptor</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>okhttp</artifactId>
        <version>${okhttp.version}</version>
    </dependency>
    <dependency>
        <groupId>com.squareup.okhttp3</groupId>
        <artifactId>logging-interceptor</artifactId>
        <version>${okhttp.version}</version>
    </dependency>
</dependencies>
```

You can find an example demo project [here](https://github.com/rohankanojia-forks/fabric8-okhttp-ipv6-k8s-cluster-bug-reproducer).
