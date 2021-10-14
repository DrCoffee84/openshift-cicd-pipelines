#!/usr/bin/env groovy

def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
            rolloutApplication(parameters.application, parameters.image, parameters.tag)
        }    
    }
}

def rolloutApplication(application, image, tag) {
    def timestamp = sh(script: "date +'%s'", returnStdout: true).trim()
    openshift.set(
        "image",
        "deployment/${application}",
        "${application}=${image}:${tag}"
    )
    if(tag == "latest") {
        def patchcmd = "{\"spec\":{\"template\":{\"metadata\":{\"annotations\": {\"last-restart\":\"${timestamp}\"}}}}}"
        openshift.raw("patch deployment/${application} --patch '${patchcmd}'")
    }
    openshift.selector("deployment", application).rollout().status()
}