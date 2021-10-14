# Create Pipelines through Templates

Login to the OpenShift cluster as cluster-admin, and run:

```bash
oc project templates

oc create -f ci-pipeline.yaml

oc new-app --template=ci-pipeline \
  --param GIT_REPO=git@git-url/openshift-pipelines.git \
  --param GIT_BRANCH=1.0 \
  --param APP_NAME=<app_name> \
  --param APP_GIT_REPO=git@git-url.app-configs.git \
  --param APP_GIT_BRANCH=master \
  --param PROJECT_NAME=templates \
  --param PARAM_ENV=dev \
  --param IMAGE_NAME=<image_name>

export PATH_TO_RSA=$HOME/.ssh/id_rsa
oc create secret generic pipeline-ocp \
  --from-file=ssh-privatekey=$PATH_TO_RSA \
  --type=kubernetes.io/ssh-auth
oc label secret pipeline-ocp credential.sync.jenkins.openshift.io=true

export PATH_TO_RSA=$HOME/.ssh/id_rsa
oc create secret generic appname-config-credentials \
  --from-file=ssh-privatekey=$PATH_TO_RSA \
  --type=kubernetes.io/ssh-auth \
  -n cicd
oc label secret appname-config-credentials credential.sync.jenkins.openshift.io=true
```

With these commands:

- We go to the `templates` project
- We create a `ci-pipeline` template
- The template generates a BuildConfig `appname-pipeline` that represents" the CI pipeline and deployment for appname"
- We create a secret `pipeline-ocp` to access the git repo where the Jenkinsfile of the pipeline will be
- We create a secret `appname-config-credentials` to access the git repo where the application configuration will be

## Git over https with credentials

In case you have a git repo that is accessed with user and pass

```bash
oc create secret generic <secret_name> \
    --from-literal=username=<user_name> \
    --from-literal=password=<password> \
    --from-file=ca.crt=</path/to/file> \
    --type=kubernetes.io/basic-auth
```

- A `ca.crt` is required with the CA that validates the self-signed https certificate

## Access to the Nexus

- We create a secret `nexus-credentials` to access the Nexus where the images of the application will be

```bash
oc create secret generic nexus-credentials \
  --from-literal=username=anonymous \
  --from-literal=password=anonymous \
  --type=kubernetes.io/basic-auth \
  -n cicd
oc secrets link default nexus-credentials --for=pull
oc label secret nexus-bajos-credentials credential.sync.jenkins.openshift.io=true -n cicd
```

## Pipeline generated for AppName

The `appname-pipeline.yaml` file contains the BuildConfig created for the AppName module and referencing the git of the pipelines `git@git-url/openshift-pipelines.git` and the config git of the app `git@git-url.app-config.git`.

## Optional

There is another version `ci-pipeline-jenkisfile`, which contains the groovy pipeline explicit in the template to facilitate editing the pipeline, testing and correction.
