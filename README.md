# OpenShift CI/CD Pipelines

## Jenkins Installation for CI/CD

Steps performed for the installation of an internal Jenkins to OpenShift with pipelines library included.


## Prerequisites

To work with OpenShift it is necessary to have two essential tools in order to interact with the cluster. On the one hand we have the *Command Line Interface (CLI)* that will allow us to create and manipulate the objects deployed within the cluster, on the other hand it is necessary to have a GIT client that allows us to have access to the application repositories, templates and others components.


## Installing Jenkins in Openshift

1. Login to the OpenShift cluster as cluster-admin, and run:
```bash
oc new-project cicd --display-name="Jenkins and CI/CD Pipelines"

oc create secret generic repository-credentials \
  --from-file=ssh-privatekey=$HOME/.ssh/id_rsa \
  --type=kubernetes.io/ssh-auth -n cicd

oc label secret repository-credentials credential.sync.jenkins.openshift.io=true -n cicd
```

With these commands, the following are generated:

- A `cicd` project where we are going to save all Jenkins elements
- A `secret` with the necessary credentials to connect to the git where the pipelines library is hosted
- A specific `label` so jenkins can synchronize the created secret

> __Note:__ the creation of the secret uses an RSA token already obtained with the repository, in case the access is done through a user/password, the way to create the secret is:

```bash
oc create secret generic repository-credentials \
  --from-literal=username=<username>   \
  --from-literal=password=<password> \
  --type=kubernetes.io/basic-auth -n cicd
```

2. Then, located in this directory, run:
```bash 
oc new-build jenkins:2 --binary --name custom-jenkins -n cicd
oc start-build custom-jenkins --from-dir=./jenkins --wait -n cicd
```

> __Note:__ the `--wait` at the end of the last command makes the terminal remain taken, note that in the OpenShift web console when the build has finished so that in case the terminal is taken, stop the command by pressing the keys combination: CTRL + C

With these commands we have generated:

- A build config `custom-jenkins` based on the base image `jenkins:2`
- A build pod `custom-jenkins-build` that generates the following output:

```bash
Using docker-registry.default.svc:5000/openshift/jenkins@sha256:02310a4743e1dddb29b37a75ebf10ad0279621b31f31b3f1201433c59736696c as the s2i builder image
---> Copying repository files ...
---> Installing 1 Jenkins plugins using /opt/openshift/plugins.txt ...
Creating initial locks...
Locking openshift-sync:1.0.45

Analyzing war...
Using version-specific update center: https://updates.jenkins.io/2.204 ...

Downloading plugins...
Downloading plugin: openshift-sync from https://updates.jenkins.io/download/plugins/openshift-sync/1.0.45/openshift-sync.hpi

Cleaning up locks
Deleting /tmp/artifacts/plugins
---> Installing new Jenkins configuration ...
sending incremental file list
./
io.fabric8.jenkins.openshiftsync.GlobalPluginConfiguration.xml
org.jenkinsci.plugins.workflow.libs.GlobalLibraries.xml

sent 2,058 bytes  received 57 bytes  4,230.00 bytes/sec
total size is 1,781  speedup is 0.84

Pushing image docker-registry.default.svc:5000/cicd/custom-jenkins:latest ...
Pushed 0/11 layers, 1% complete
Pushed 1/11 layers, 10% complete
Pushed 2/11 layers, 20% complete
Pushed 3/11 layers, 31% complete
Pushed 4/11 layers, 41% complete
Pushed 5/11 layers, 45% complete
Push successful
```

It leaves us an image created.

3. Finally we created an instance of 'Jenkins' using this new image:

```bash
oc new-app --template=jenkins-persistent --name=jenkins \
  --param JENKINS_IMAGE_STREAM_TAG=custom-jenkins:latest \
  --param NAMESPACE=cicd \
  -n cicd
```

