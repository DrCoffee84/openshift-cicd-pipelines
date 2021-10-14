#!/usr/bin/env groovy

/*
parameters:{
  repo: git url
  branch: git branch
  secret: credentialsId as we seen it in Jenkis : "openshift.projectName + openshift.secretName"
}
*/
def call(parameters) {
  def gitInfo = [:]

  gitInfo['url'] = parameters.repo
  gitInfo['credentialsId'] = parameters.secret

  checkout([$class: 'GitSCM', 
     branches: [[name: parameters.branch]], 
     userRemoteConfigs: [gitInfo]])
}