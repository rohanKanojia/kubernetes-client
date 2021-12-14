package main

import (
	"fmt"
	"github.com/fabric8io/kubernetes-client/generator/pkg/schemagen"
	gatewayv1 "github.com/solo-io/solo-apis/pkg/api/gateway.solo.io/v1"
	gloov1 "github.com/solo-io/solo-apis/pkg/api/gloo.solo.io/v1"
	machinery "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"reflect"
	"sync"
)

func main() {

	crdLists := map[reflect.Type]schemagen.CrdScope{
		reflect.TypeOf(gloov1.UpstreamList{}):          schemagen.Namespaced,
		reflect.TypeOf(gatewayv1.VirtualServiceList{}): schemagen.Namespaced,
	}

	// constraints and patterns for fields
	constraints := map[reflect.Type]map[string]*schemagen.Constraint{
		//		reflect.TypeOf(v1.Step{}): {"Name": &schemagen.Constraint{MaxLength: 63, Pattern: "^[a-z0-9]([-a-z0-9]*[a-z0-9])?$"}},
	}

	// types that are manually defined in the model
	providedTypes := []schemagen.ProvidedType{}

	// go packages that are provided and where no generation is required and their corresponding java package
	providedPackages := map[string]string{
		// external
		"k8s.io/api/core/v1":                   "io.fabric8.kubernetes.api.model",
		"k8s.io/apimachinery/pkg/apis/meta/v1": "io.fabric8.kubernetes.api.model",
		"k8s.io/apimachinery/pkg/api/resource": "io.fabric8.kubernetes.api.model",
		"k8s.io/apimachinery/pkg/runtime":      "io.fabric8.kubernetes.api.model.runtime",
	}

	// mapping of go packages of this module to the resulting java package
	// optional ApiGroup and ApiVersion for the go package (which is added to the generated java class)
	packageMapping := map[string]schemagen.PackageInformation{
		// v1
		"github.com/solo-io/solo-apis/pkg/api/gloo.solo.io/v1":    {JavaPackage: "io.fabric8.solo.gloo.v1", ApiGroup: "gloo.solo.io", ApiVersion: "v1"},
		"github.com/solo-io/solo-apis/pkg/api/gateway.solo.io/v1": {JavaPackage: "io.fabric8.solo.gateway.v1", ApiGroup: "gateway.solo.io", ApiVersion: "v1"},
	}

	// TODO remove
	//converts all packages starting with <key> to a java package using an automated scheme:
	//  - replace <key> with <value> aka "package prefix"
	//  - replace '/' with '.' for a valid java package name
	// e.g. github.com/apache/camel-k/pkg/apis/camel/v1/knative/CamelEnvironment is mapped to "io.fabric8.camelk.internal.pkg.apis.camel.v1.knative.CamelEnvironment"
	mappingSchema := map[string]string{
		//		"github.com/apache/camel-k/pkg/apis/camel/v1/knative": "io.fabric8.camelk.v1beta1.internal",
	}

	// overwriting some times
	manualTypeMap := map[reflect.Type]string{
		reflect.TypeOf(machinery.Time{}):       "java.lang.String",
		reflect.TypeOf(runtime.RawExtension{}): "java.util.Map<String, Object>",
		reflect.TypeOf(sync.Mutex{}):           "java.util.Map<String, Object>",
	}

	json := schemagen.GenerateSchema("http://solo.io/gloo/v1/GlooSchema#", crdLists, providedPackages, manualTypeMap, packageMapping, mappingSchema, providedTypes, constraints, "io.fabric8")

	fmt.Println(json)
}
