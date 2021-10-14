# Create Pipelines through Templates

Login to the OpenShift cluster as cluster-admin, and run:

```bash
oc project templates

oc create -f build-pipeline.yaml -n openshift
```

With these commands:

- We go to the `templates` project
- We create a `build-pipeline` template

## Creation of secrets

For the creation of images we need to create

- The secret of access to the pipelines (this is done in each project where it is going to be built, the others are generated in the `cicd` project)

For this step you need the generated id_rsa file with access to the same git

```bash
export PATH_TO_RSA=$HOME/.ssh/id_rsa
oc create secret generic pipeline-ocp \
  --from-file=ssh-privatekey=$PATH_TO_RSA \
  --type=kubernetes.io/ssh-auth
oc label secret pipeline-ocp credential.sync.jenkins.openshift.io=true
```

- The secret of access to git where the configuration of the applications is

For this step you need the generated id_rsa file with access to the same git

```bash
export PATH_TO_RSA=$HOME/.ssh/id_rsa
oc create secret generic appname-config-credentials \
  --from-file=ssh-privatekey=$PATH_TO_RSA \
  --type=kubernetes.io/ssh-auth \
  -n cicd
oc label secret appname-config-credentials credential.sync.jenkins.openshift.io=true -n cicd
```

- The secret of access to the Nexus environments where the created image will be inserted

```bash
oc create secret generic nexus-credentials \
  --from-literal=username=anonymous \
  --from-literal=password=anonymous \
  --type=kubernetes.io/basic-auth \
  -n cicd
oc secrets link default nexus-credentials --for=pull -n cicd
oc label secret nexus-credentials credential.sync.jenkins.openshift.io=true -n cicd
```

- The access secret to the registry of the clustr where we are building

This secret is the token of the SA Jenkins that already has the accesses we are looking for

```bash
export REGISTRY_TOKEN=$(oc sa get-token jenkins -n cicd)
oc create secret generic registry-credentials \
  --from-literal=username=unused \
  --from-literal=password=${REGISTRY_TOKEN} \
  --type=kubernetes.io/basic-auth \
  -n cicd
oc label secret registry-credentials credential.sync.jenkins.openshift.io=true -n cicd
```

## Access to local registry

In the image creation step, the image is saved in the internal OpenShift registry.
To upload it to Nexus we need to copy registry to registry.

- We obtain the path of the cluster registry

```bash
export REGISTRY_URL=$(oc get route docker-registry -n default --template={{.spec.host}})
```

We inject it in a variable inside the Jenkinsfile in question, in the `environments` section

```groovy
environment {
  // URl of the OpenShift internal docker registry
  SRC_REGISTRY_URL=<valor de $REGISTRY_URL>
}
```

## TEMPORARY: Create Buildconfig of the app

We generate a BC so that the app can do the BuildConfig strategy docker binary from the pipeline
This is your Temporary step, you have to have the bc inside the Template.

```bash
oc new-build --name api --binary --strategy=docker
```

## Create the Build pipeline

```bash
oc new-app --name=appname --template=build-pipeline \
  --param GIT_REPO=git@git-url/openshift-pipelines.git \
  --param GIT_BRANCH=1.0 \
  --param APP_NAME=api \
  --param APP_GIT_REPO=git@git-url/app-config.git \
  --param APP_GIT_BRANCH=master \
  --param PROJECT_NAME=templates \
  --param PARAM_ENV=dev \
  --param IMAGE_NAME=appname-image \
  --param WAR_FOLDER=dev-repository/war_compiled
```
