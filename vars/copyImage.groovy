#!/usr/bin/env groovy

/**
 * Copia una imagen docker de una registry a otra con ayuda de la herramienta skopeo.
 * Intent:
 * - Algunas registrys son inseguras.
 * - No todas las registrys son publicas, algunas requieren de credenciales.
 * Parametros:
 * - source_credentials: debe ser un token, normalmente de la service account de jenkins.
 * - destination_credentials: usuario y password de la registry destino.
 * - source_image: imagen que deseamos copiar. Imagen de origen. Se espera: registry.url/imagen
 * - destination_image: imagen que se va a producir. Imagen destino. Se espera: registry.url/imagen
 * - no_proxy: exceptiones de las URLs que no deben pasar porun proxy.
 */

def call(parameters) {
    def options = ""
    def credentials = ""
    def no_proxy = ""

    if (parameters.no_proxy) {
        no_proxy = "NO_PROXY=${parameters.no_proxy}"
    }

    // Connection Options
    if (parameters.src_tls) {
        options = options + "--src-tls-verify=${parameters.src_tls}"
    }

    if (parameters.dest_tls) {
        options = options + " --dest-tls-verify=${parameters.dest_tls}"
    }

    // Authentication Method
    if (parameters.source_token) {
        credentials = credentials + "--src-registry-token=${parameters.source_token}"
    }

    if (parameters.source_credentials) {
        credentials = credentials + "--src-creds=${parameters.source_credentials}"
    }

    if (parameters.destination_token) {
        credentials = credentials + " --dest-registry-token=${parameters.destination_token}"
    }

    if (parameters.destination_credentials) {
        credentials = credentials + " --dest-creds=${parameters.destination_credentials}"
    }

    retry (3) {
        sh """  set +x; ${no_proxy} \
                skopeo copy ${options} \
                    --retry-times=5 \
                    ${credentials} \
                    docker://${parameters.source_image} docker://${parameters.destination_image} \
                ; set -x
           """;
    }
}