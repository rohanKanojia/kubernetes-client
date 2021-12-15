module github.com/fabric8io/kubernetes-client/extensions/solo/generator-v1

replace (
	github.com/fabric8io/kubernetes-client/generator v0.0.0 => ./../../../generator
	k8s.io/apimachinery => k8s.io/apimachinery v0.19.6
	k8s.io/client-go => k8s.io/client-go v0.19.6
)

go 1.16

require (
	github.com/fabric8io/kubernetes-client/generator v0.0.0
	github.com/solo-io/solo-apis v1.6.31
	google.golang.org/protobuf v1.26.0 // indirect
	k8s.io/apimachinery v0.19.6
)
