# New project

When creating a new project where we are going to host applications and pipelines, we have to perform the following steps:

```bash
oc project <new_project>

oc create secret generic pipeline-ocp \
  --from-file=ssh-privatekey=$HOME/.ssh/id_rsa \
  --type=kubernetes.io/ssh-auth
oc label secret pipeline-ocp credential.sync.jenkins.openshift.io=true

oc adm policy add-cluster-role-to-user edit system:serviceaccount:custom-cicd:jenkins -n <new_project>
```

With these commands:

- We are located in the project `<new_project>`
- We create a secret `pipeline-ocp` to access the git repo where the Jenkinsfile of the pipeline will be and we give it the label to synchronize with Jenkins
- We give permission to SA cicd:jenkins to edit the new project

*Note:* All these steps must be applied per cluster.

## Configure Jenkins

- Add <new_project> for Jenkins to sync in OpenShift Sync Plugin section
