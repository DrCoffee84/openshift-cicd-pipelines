#!/usr/bin/env groovy

def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
            openshift.apply("-f openshift/build/imagestream.yaml")
            openshift.apply("-f openshift/build/buildconfig.yaml")
        }
    }
}