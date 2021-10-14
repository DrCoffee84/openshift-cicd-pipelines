# Create CD Pipelines through Templates

Login to the OpenShift cluster as cluster-admin, and run:

```bash
oc project sim

oc create -f cd-pipeline.yaml

oc create secret generic pipeline-ocp \
  --from-file=ssh-privatekey=$HOME/.ssh/id_rsa \
  --type=kubernetes.io/ssh-auth \
  -n custom-cicd

oc label secret pipeline-ocp credential.sync.jenkins.openshift.io=true

oc create secret generic custom-config-credentials \
  --from-file=ssh-privatekey=$HOME/.ssh/id_rsa \
  --type=kubernetes.io/ssh-auth \
  -n custom-cicd

oc label secret custom-config-credentials credential.sync.jenkins.openshift.io=true
```

With these commands:

- We place ourselves in the `sim` project
- We create a `cd-pipeline` template
- We create a secret `pipeline-ocp` to access the git repo where the Jenkinsfile of the pipeline will be
- We create a secret `custom-config-credentials` to access the git repo where the application configuration will be

## Git over HTTPS with credentials

In case you have a git repo that is accessed with user and password

```bash
oc create secret generic <secret_name> \
    --from-literal=username=<user_name> \
    --from-literal=password=<password> \
    --from-file=ca.crt=</path/to/file> \
    --type=kubernetes.io/basic-auth
```

- A `ca.crt` is required with the CA that validates the self-signed https certificate

## Cross-cluster

To enable image copying and promotion of cross-cluster images you must:

- Generate credentials in each cluster for the custom-cicd:jenkins SA
- Crossing credentials between clusters, so that both can "modify" elements on the opposite side
- Generate the access credentials for Nexus Low Environments and Nexus High Environments
- Generate the pipelines in each Cluster considering the "opposites"

### Creation of the Jenkins SA in each cluster

In each cluster run:

```bash
oc create sa jenkins -n custom-cicd
oc adm policy add-cluster-role-to-user system:registry system:serviceaccount:custom-cicd:jenkins
oc adm policy add-cluster-role-to-user system:image-builder system:serviceaccount:custom-cicd:jenkins
```

### Creation of local and remote secrets

In Production A run:

```bash
export PRODA_CLUSTER_TOKEN=$(oc sa get-token jenkins -n custom-cicd)
oc create secret generic prod-a-cluster-credentials \
  --from-literal=openshift-client-token=$PRODA_CLUSTER_TOKEN \
  -n custom-cicd
oc label secret prod-a-cluster-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

In Production B run:

```bash
export PRODB_CLUSTER_TOKEN=$(oc sa get-token jenkins -n custom-cicd)
oc create secret generic prod-b-cluster-credentials \
  --from-literal=openshift-client-token=$PRODB_CLUSTER_TOKEN \
  -n custom-cicd
oc label secret prod-b-cluster-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

In Production A, using the content of the token obtained in Production B --> ${PRODB_CLUSTER_TOKEN}, run:

```bash
oc create secret generic prod-b-cluster-credentials \
  --from-literal=openshift-client-token=$PRODB_CLUSTER_TOKEN \
  -n custom-cicd
oc label secret prod-b-cluster-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

In Production B, using the content of the token obtained in Production A --> ${PRODA_CLUSTER_TOKEN}, run:

```bash
oc create secret generic prod-a-cluster-credentials \
  --from-literal=openshift-client-token=$PRODA_CLUSTER_TOKEN \
  -n custom-cicd
oc label secret prod-a-cluster-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

### Creation of credentials for access to nexus high and low environments

#### Low Nexus

```bash
oc create secret generic nexus-low-credentials \
  --from-literal=username=anonymous \
  --from-literal=password=anonymous \
  --type=kubernetes.io/basic-auth \
  -n custom-cicd

oc secrets link default nexus-low-credentials --for=pull
oc label secret nexus-low-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

#### High Nexus

```bash
oc create secret generic nexus-high-credentials \
  --from-literal=username=docker \
  --from-literal=password=Atos20Docker \
  --type=kubernetes.io/basic-auth \
  -n custom-cicd

oc secrets link default nexus-high-credentials --for=pull
oc label secret nexus-high-credentials credential.sync.jenkins.openshift.io=true -n custom-cicd
```

Upon completion, we should see:

- In Jenkins Prod A, 2 credentials: custom-cicd-prod-a-cluster-credentials, custom-cicd-prod-b-cluster-credentials
- In Jenkins Prod B, 2 credentials: custom-cicd-prod-a-cluster-credentials, custom-cicd-prod-b-cluster-credentials

### Jenkins SA Access Grant

In each cluster, for the chosen project (ie, `prod`) run:

```bash
oc adm policy add-cluster-role-to-user edit system:serviceaccount:custom-cicd:jenkins -n prod
```

With this we give the Jenkins SA the editing power over the project in question so that it can create objects (dc, bc, is, etc).

### Create the pipeline in each cluster with the correct parameters

For the **AppName** project run:

- In Production:

```bash
oc new-app --name=backoffice --template=cd-pipeline \
  --param GIT_REPO=https://you-company.git \
  --param GIT_BRANCH=master \
  --param APP_NAME=backoffice \
  --param APP_GIT_REPO=https://you-company.git \
  --param APP_GIT_BRANCH=master \
  --param APP_GIT_SECRET=custom-config-credentials \
  --param APP_CM_REMOTO=cm-b \
  --param APP_CM_LOCAL=cm-a \
  --param PROJECT_NAME=sim \
  --param PARAM_ENV=sim \
  --param IMAGE_NAME=canales-bo \
  --param JENKINS_PROJECT_NAME=custom-cicd \
  --param SRC_REGISTRY_URL='you-registry-url:8181' \
  --param SRC_REGISTRY_CREDENTIALS=nexus-low-credentials \
  --param SRC_REGISTRY_PROJECT=canales-registry \
  --param DST_REGISTRY_URL='your-registry-url:8181' \
  --param DST_REGISTRY_CREDENTIALS=nexus-high-credentials \
  --param DST_REGISTRY_PROJECT=canales-registry \
  --param LOCAL_CLUSTER_URL='insecure://your-cluster-url' \
  --param LOCAL_CLUSTER_CREDENTIALS=prod-a-cluster-credentials \
  --param REMOTO_CLUSTER_URL='insecure://your-cluster-url' \
  --param REMOTO_CLUSTER_CREDENTIALS=prod-b-cluster-credentials \
```

- In Production B:

```bash
oc new-app --name=backoffice --template=cd-pipeline \
  --param GIT_REPO=https://your-company.git \
  --param GIT_BRANCH=master \
  --param APP_NAME=backoffice \
  --param APP_GIT_REPO=https://your-company.git \
  --param APP_GIT_BRANCH=master \
  --param APP_GIT_SECRET=custom-config-credentials \
  --param APP_CM_REMOTO=cm-a \
  --param APP_CM_LOCAL=cm-b \
  --param PROJECT_NAME=sim \
  --param PARAM_ENV=sim \
  --param IMAGE_NAME=canales-bo \
  --param JENKINS_PROJECT_NAME=custom-cicd \
  --param SRC_REGISTRY_URL='your-registry-url:8181' \
  --param SRC_REGISTRY_CREDENTIALS=nexus-low-credentials \
  --param SRC_REGISTRY_PROJECT=canales-registry \
  --param DST_REGISTRY_URL='your-registry-url:8181' \
  --param DST_REGISTRY_CREDENTIALS=nexus-high-credentials \
  --param DST_REGISTRY_PROJECT=custom \
  --param LOCAL_CLUSTER_URL='insecure://your-cluster-url' \
  --param LOCAL_CLUSTER_CREDENTIALS=prod-b-cluster-credentials \
  --param REMOTO_CLUSTER_URL='insecure://your-cluster-url' \
  --param REMOTO_CLUSTER_CREDENTIALS=prod-a-cluster-credentials \
```
