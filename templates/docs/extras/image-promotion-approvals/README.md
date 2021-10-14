# Approvals

For the approval process, the input step in Jenkins will get the user that clicked on the button and then query the OpenShift API to get the groups, if the user belongs to a certain group (`env.PROD_APPROVAL_GROUP`), then the pipeline will continue, otherwise it will wait for a user that belongs to that certain group.

It is necessary to create a new cluster role in OpenShift (login as cluster administrator):

```bash
oc create -f group-reader.yaml

oc adm policy add-cluster-role-to-user group-reader system:serviceaccount:jenkins-project:jenkins 
```

> **NOTE:** in case you cannot make use of the OC tool, use the YAML files to copy their content and create them via the + button from the OpenShift web console. Files:
> * `group-reader.yaml`
> * `crb_jenkins-group-reader.yaml`

The following code snippet represents the Jenkins step:

```groovy
stage("Approve Deploy") {
    when {
        expression {
            // If the variable env exists and has a value, continue...
            return env.PROD_APPROVERS_GROUP
        }
    }
    steps {
        processApproval(message: "Switch to a new version?", approversGroup: env.PROD_APPROVERS_GROUP)
    }
}
```

For more information of the **processApproval** function, please see the following [code](../../../../vars/processApproval.groovy).
