#!/usr/bin/env groovy

/**
 * Lista todos los tags disponibles para una imagen docker con ayuda de la herramiento skopeo.
 * Intent:
 * - Algunas registrys son inseguras.
 * - No todas las registrys son publicas, algunas requieren de credenciales.
 * Parametros:
 * - image: imagen de la que se quiere obtener los tags. Se espera: registry.url/imagen.
 * - credentials: usuario y password de la registry.
 * - tls: le indica a skopeo si verifica o no que sea una conexion con TLS.
 * - no_proxy: exceptiones de las URLs que no deben pasar porun proxy.
 * Retorno:
 * - Devuelve un string de un array JSON con los tags obtenidos.
 */

def call(parameters) {
    def no_proxy = ""
    def options = ""

    if (parameters.no_proxy) {
        no_proxy = "NO_PROXY=${parameters.no_proxy}"
    }

    if (parameters.tls) {
        options = options + "--tls-verify=${parameters.tls}"
    }

    if (parameters.credentials) {
        options = options + " --creds=${parameters.credentials}"
    }

    def tags = sh(
        script: """ set +x; ${no_proxy} \
                    skopeo list-tags docker://${parameters.image} \
                    ${options} | jq -M .Tags -S -c \
                    ; set -x
                """,
        returnStdout: true
    ).trim()

    return tags
}