#!/usr/bin/env groovy

def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
            def secret = openshift.selector("secrets", "${parameters.secret_name}");
            if (!secret.exists()) {
                error 'Secreto no encontrado en OCP'
            }
            def object_content = secret.object();
            return sh(script: "set +x; echo \$(echo ${object_content.data./${parameters.secret_key}/} | base64 -d); set -x", returnStdout: true).trim()
        }
    }
}