#!/usr/bin/env groovy

def call(parameters) {
    env.APP_NAME = parameters.application
    env.IMAGE_NAME = parameters.application
    env.TAG_NAME = parameters.version

    env.PROJECT = getProject()

    env.DEV_PROJECT = "${env.PROJECT}-dev"
    env.TEST_PROJECT = "${env.PROJECT}-test"
    env.PROD_PROJECT = "${env.PROJECT}-prod"

    env.APP_OPENSHIFT_DIR = (env.OPENSHIFT_DIR) ? env.OPENSHIFT_DIR : "./openshift"
    env.APP_TEMPLATE = (parameters.applicationTemplate) ? parameters.applicationTemplate : "./${env.APP_OPENSHIFT_DIR}/template.yaml"
    env.APP_TEMPLATE_PARAMETERS_DEV = (parameters.applicationTemplateParametersDev) ? parameters.applicationTemplateParametersDev : "./${env.APP_OPENSHIFT_DIR}/environments/dev/templateParameters.txt"
    env.APP_TEMPLATE_PARAMETERS_TEST = (parameters.applicationTemplateParametersTest) ? parameters.applicationTemplateParametersTest :  "./${env.APP_OPENSHIFT_DIR}/environments/test/templateParameters.txt"
    env.APP_TEMPLATE_PARAMETERS_PROD = (parameters.applicationTemplateParametersProd) ? parameters.applicationTemplateParametersProd :  "./${env.APP_OPENSHIFT_DIR}/environments/prod/templateParameters.txt"
    env.APP_DEPLOYMENT_PATCH_DEV = (parameters.applicationDeploymentPatchDev) ? parameters.applicationDeploymentPatchDev : "./${env.APP_OPENSHIFT_DIR}/environments/dev/deploymentPatch.yaml"
    env.APP_DEPLOYMENT_PATCH_TEST = (parameters.applicationDeploymentPatchTest) ? parameters.applicationDeploymentPatchTest : "./${env.APP_OPENSHIFT_DIR}/environments/test/deploymentPatch.yaml"
    env.APP_DEPLOYMENT_PATCH_PROD = (parameters.applicationDeploymentPatchProd) ? parameters.applicationDeploymentPatchProd : "./${env.APP_OPENSHIFT_DIR}/environments/prod/deploymentPatch.yaml"
    env.APP_REPLACE_CONFIG_DEV = (parameters.applicationReplaceConfigDev) ? parameters.applicationReplaceConfigDev : "./${env.APP_OPENSHIFT_DIR}/environments/dev/replaceConfig.yaml"
    env.APP_REPLACE_CONFIG_TEST = (parameters.applicationReplaceConfigTest) ? parameters.applicationReplaceConfigTest : "./${env.APP_OPENSHIFT_DIR}/environments/test/replaceConfig.yaml"
    env.APP_REPLACE_CONFIG_PROD = (parameters.applicationReplaceConfigProd) ? parameters.applicationReplaceConfigProd : "./${env.APP_OPENSHIFT_DIR}/environments/prod/replaceConfig.yaml"
    env.APP_INT_TEST_AGENT = (parameters.applicationIntegrationTestAgent) ? parameters.applicationIntegrationTestAgent : "./${env.APP_OPENSHIFT_DIR}/environments/test/integration-test/int-test.yaml"
    env.APP_INT_TEST_COMMANDS = (parameters.applicationIntegrationTestCommands) ? parameters.applicationIntegrationTestCommands : "./${env.APP_OPENSHIFT_DIR}/environments/test/integration-test/int-test.groovy"
}