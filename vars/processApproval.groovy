def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.clusterToken) {
        openshift.withProject(parameters.project) {
            def canApprove = false
            def groups = openshift.selector("groups").objects()
            
            while (canApprove == false) {
                def submitter = input(message: parameters.message, submitterParameter: 'submitter')
                def user = submitter.substring(0, submitter.indexOf("-"))
                
                for (g in groups) {
                    if (g.metadata.name.equals(parameters.approversGroup) && g.users.contains(user)) {
                        canApprove = true
                        echo "User ${user} from group ${g.metadata.name} approved the deployment"
                    } 
                }

                if (canApprove == false)
                    echo "User ${user} is not allowed to approve the deployment"
            }           
        }  
    }
}