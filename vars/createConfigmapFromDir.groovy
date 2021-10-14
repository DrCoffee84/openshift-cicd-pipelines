#!/usr/bin/env groovy

/**
 * Crea/Modifica un configmap tomando el contenido de un file o de un directorio
 * Intent:
 * - Algunos clientes tienen mas de un file dentro de los configmap. 
 * - Esto facilita el mantenimiento en git de esos files puros en lugar de un yaml (ie, el replaceConfig)
 * - Facilita el armado/mantenimiento de configMaps con binaryData
 * Notas:
 * - El parametro dir puede ser un file o un dir
 * - La funcion hace un dry-run para generar un yaml y luego un apply que lo ejecuta
 * Requerimientos:
 * - parameters.dir debe existir en el filesystem
 */
def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
            openshift.apply(openshift.raw("create", "configmap", "${parameters.configMap}", "--from-file=${parameters.dir}", "--dry-run", "--output=yaml").actions[0].out)
        }
    }
}
