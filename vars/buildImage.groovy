#!/usr/bin/env groovy

def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
            // First, from binary
            if (parameters.artifactsDir) {
                openshift.selector("bc", parameters.application).startBuild("--from-dir=${parameters.artifactsDir}", "--wait=true")
            } else if (parameters.artifactFile) {
                openshift.selector("bc", parameters.application).startBuild("--from-file=${parameters.artifactFile}", "--wait=true")
            } else {
                // BuildConfig from source
                openshift.selector("bc",  parameters.application).startBuild("--wait=true")
            }
        }
    }
}