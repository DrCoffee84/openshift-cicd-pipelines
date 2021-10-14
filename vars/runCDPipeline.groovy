#!/usr/bin/env groovy

def call(parameters) {
    pipeline {
        agent {
            label parameters.agent
        }
        options {
            skipDefaultCheckout()
            disableConcurrentBuilds()
        }
        stages {
            stage("Checkout") {
                steps {      
                    gatherParameters(parameters)
                    gitClone()
                }
            }
            stage("Compile") {
                steps {
                    sh parameters.compileCommands
                }
            }
            stage("Test") {
                steps {
                    sh parameters.testCommands
                }
            }
            stage("Build Image") {
                steps {
                    applyTemplate(project: env.DEV_PROJECT, 
                                  application: env.APP_NAME, 
                                  template: env.APP_TEMPLATE, 
                                  parameters: env.APP_TEMPLATE_PARAMETERS_DEV,
                                  replaceConfig: env.APP_REPLACE_CONFIG_DEV,
                                  deploymentPatch: env.APP_DEPLOYMENT_PATCH_DEV,
                                  createBuildObjects: true)

                    buildImage(project: env.DEV_PROJECT, 
                               application: env.APP_NAME, 
                               artifactsDir: parameters.artifactsDir)
                }
            }
            stage("Deploy DEV") {
                steps {
                    script {
                        env.TAG_NAME = getVersion(parameters.agent)
                    }   
                    
                    tagImage(srcProject: env.DEV_PROJECT, 
                             srcImage: env.IMAGE_NAME, 
                             srcTag: "latest", 
                             dstProject: env.DEV_PROJECT, 
                             dstImage: env.IMAGE_NAME,
                             dstTag: env.TAG_NAME)
                    
                    deployImage(project: env.DEV_PROJECT, 
                                application: env.APP_NAME, 
                                image: env.IMAGE_NAME, 
                                tag: env.TAG_NAME)
                }
            }
            stage("Deploy TEST") {
                steps {
                    input("Promote to TEST?")

                    applyTemplate(project: env.TEST_PROJECT, 
                                  application: env.APP_NAME, 
                                  template: env.APP_TEMPLATE, 
                                  parameters: env.APP_TEMPLATE_PARAMETERS_TEST,
                                  replaceConfig: env.APP_REPLACE_CONFIG_TEST,
                                  deploymentPatch: env.APP_DEPLOYMENT_PATCH_TEST)

                    tagImage(srcProject: env.DEV_PROJECT, 
                             srcImage: env.IMAGE_NAME, 
                             srcTag: env.TAG_NAME, 
                             dstProject: env.TEST_PROJECT, 
                             dstImage: env.IMAGE_NAME,
                             dstTag: env.TAG_NAME)
                    
                    deployImage(project: env.TEST_PROJECT, 
                                application: env.APP_NAME, 
                                image: env.IMAGE_NAME, 
                                tag: env.TAG_NAME)
                }
            }
            /*
            stage("Integration Test") {
                agent {
                    kubernetes {
                        cloud "openshift"
                        defaultContainer "jnlp"
                        label "${env.APP_NAME}-int-test"
                        yaml readFile(env.APP_INT_TEST_AGENT)
                    }
                }
                steps {
                    gitClone()
                    load env.APP_INT_TEST_COMMANDS
                }
            }
            */
            stage("Deploy PROD (Blue)") {
                steps {
                    script {
                        if (!blueGreen.existsBlueGreenRoute(project: env.PROD_PROJECT, application: env.APP_NAME)) {
                            applyTemplate(project: env.PROD_PROJECT, 
                                          application: blueGreen.getApplication1Name(env.APP_NAME), 
                                          template: env.APP_TEMPLATE, 
                                          parameters: env.APP_TEMPLATE_PARAMETERS_PROD,
                                          replaceConfig: env.APP_REPLACE_CONFIG_PROD,
                                          deploymentPatch: env.APP_DEPLOYMENT_PATCH_PROD)
                                        
                            applyTemplate(project: env.PROD_PROJECT, 
                                          application: blueGreen.getApplication2Name(env.APP_NAME), 
                                          template: env.APP_TEMPLATE, 
                                          parameters: env.APP_TEMPLATE_PARAMETERS_PROD,
                                          replaceConfig: env.APP_REPLACE_CONFIG_PROD,
                                          deploymentPatch: env.APP_DEPLOYMENT_PATCH_PROD) 

                            blueGreen.createBlueGreenRoute(project: env.PROD_PROJECT, application: env.APP_NAME)
                        } else {
                            applyTemplate(project: env.PROD_PROJECT, 
                                          application: blueGreen.getBlueApplication(project: env.PROD_PROJECT, application: env.APP_NAME), 
                                          template: env.APP_TEMPLATE, 
                                          parameters: env.APP_TEMPLATE_PARAMETERS_PROD,
                                          replaceConfig: env.APP_REPLACE_CONFIG_PROD,
                                          deploymentPatch: env.APP_DEPLOYMENT_PATCH_PROD)
                        }
                        
                        tagImage(srcProject: env.TEST_PROJECT, 
                                 srcImage: env.IMAGE_NAME, 
                                 srcTag: env.TAG_NAME, 
                                 dstProject: env.PROD_PROJECT, 
                                 dstImage: env.IMAGE_NAME,
                                 dstTag: env.TAG_NAME)

                        deployImage(project: env.PROD_PROJECT, 
                                    application: blueGreen.getBlueApplication(project: env.PROD_PROJECT, application: env.APP_NAME), 
                                    image: env.IMAGE_NAME, 
                                    tag: env.TAG_NAME)
                    } 
                }
            }
            stage("Deploy PROD (Green)") {
                steps {
                    input("Switch to new version?")

                    script{
                        blueGreen.switchToGreenApplication(project: env.PROD_PROJECT, application: env.APP_NAME)   
                    }              
                }
            }
        }
    }    
}