# Approval step in pipelines

For the approval of a particular stage, a step is added to the pipeline and a group of `approvers` to OpenShift

## Create Role for Jenkins SA

Generate a cluster roles to be able to read groups and users and assign it to the Jenkins SA

```bash
echo "---
apiVersion: v1
kind: ClusterRole
metadata:
  name: group-reader
rules:
- apiGroups:
  - user.openshift.io
  resources:
  - groups
  - identities
  - useridentitymappings
  - users
  verbs:
  - get
  - list
  - watch"
| oc create -f-

oc adm policy add-cluster-role-to-user group-reader system:serviceaccount:custom-cicd:jenkins
```

This Role will allow Jenkins to know the current user, the one who logs into Jenkins to click, from which group it is.
It also lets Jenkins know the general user groups.

## New Stage in Pipeline

We add to the pipeline the stage and the configuration to use it

```groovy
stage("Approve Deploy") {
  when {
    expression {
      // If the env var exists and has a value, continue...
      return env.PROD_APPROVERS_GROUP
    }
  }
  steps {
    processApproval(message: "Switch to new version?", approversGroup: env.PROD_APPROVERS_GROUP)
  }
}
```

And in the `environment` section we declare the variable with the value of the group that we want to pass in the pipelines.

```groovy
environment {
  PROD_APPROVERS_GROUP='pipeline-approver'
}
```
