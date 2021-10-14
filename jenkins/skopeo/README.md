# Skopeo Agent for Jenkins

Skopeo is a command line utility that can manage operations between registrys.

An agent with Skopeo installed is required to execute certain operations within the necessary `step` in the Jenkins pipeline. To create the agent, please refer to the instructions in the file [setup.sh](./setup.sh "Installation Bash").

> **Note:** please check the file `skopeo-bc.yaml` that use vars for http/https proxy. Change it!

The following code snippet represents the `step` in the Jenkins pipeline:

```groovy
stage("Promote Image") {
    agent {
        label "skopeo"
    }
    steps {
        script {
            def srcCreds = "usuario:${env.SRC_REGISTRY_PASSWORD}";
            def dstCreds = "usuario:${env.DST_REGISTRY_TOKEN}";

            def src = "docker://${env.SRC_REGISTRY_URL}/${env.SRC_PROJECT}/${env.IMAGE_NAME}:${env.TAG}";
            def dst = "docker://${env.DST_REGISTRY_URL}/${env.DST_PROJECT}/${env.IMAGE_NAME}:${env.TAG}";

            sh "skopeo copy --src-tls-verify=false --dest-tls-verify=false --src-creds=${srcCreds} --dest-creds=${dstCreds} ${src} ${dst}";
        }
    }
}
```
