#!/usr/bin/env groovy

def call(tech) {
    if (tech.equals("maven"))
        return readMavenPom().getVersion()
    else
        return "latest"
}