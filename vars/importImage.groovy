#!/usr/bin/env groovy

/**
 * Realiza un "oc import-image" de una imagen en un repositorio Docker externo a un ImageStream en un proyecto interno
 * Sirve para casos donde las registries del cliente son externas (ie, Nexus)
 * Notas:
 * - Se permite nombre distinto para la imagen Externa e Interna
 * - El tag en el repo externo se traslada sin modificar al repo interno
 * Requerimientos:
 * - Las credenciales de acceso al repositorio deben existir en un secret
 * - La SA "default" debe tener linkeado el secret para hacer "pull" de la imagen del repo externo
 * - La SA "builder" debe tener linkeado el secret para hacer un "build" usando la imagen del repo externo
 */
def call(parameters) {
    openshift.withCluster(parameters.clusterUrl, parameters.credentialsId) {
        openshift.withProject(parameters.project) {
			openshift.raw("import-image", "${parameters.imageName}:${parameters.tag}", "--from ${parameters.repoUrl}/${parameters.externalImageName}:${parameters.tag}", "--insecure", "--confirm");
        }
    }
}
