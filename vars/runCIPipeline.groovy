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
        }
    }    
}