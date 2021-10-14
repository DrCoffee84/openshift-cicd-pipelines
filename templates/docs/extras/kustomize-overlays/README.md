# Kustomize Overlays

To deploy an application with a CI/CD pipeline, you must first create the necessary YAML files to create the required objects within OpenShift cluster. The following file structure and organization is suggested:

```bash
openshift
├── base
│   ├── deployment.yaml
│   ├── kustomization.yaml
│   ├── route.yaml
│   └── service.yaml
├── build
│   ├── buildconfig.yaml
│   └── imagestream.yaml 
└── overlays
    └── dev
        ├── kustomization.yaml
        └── resources_patch.yaml
```

They will be applied or created dynamically by the pipeline by executing an:

```bash
oc apply -k openshift/overlays/dev/
```
