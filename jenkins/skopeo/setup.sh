#! /usr/bin/env bash

oc import-image ose-jenkins-agent-base --confirm --from=registry.redhat.io/openshift4/ose-jenkins-agent-base -n jenkins-project

oc create is skopeo -n jenkins-project

oc create -f ./skopeo-bc.yaml -n jenkins-project

oc start-build skopeo -n jenkins-project --wait

oc label is skopeo role=jenkins-slave -n jenkins-project
