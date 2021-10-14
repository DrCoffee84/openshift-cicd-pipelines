#!/usr/bin/env groovy

def call() {
    return env.JOB_NAME.split("/")[0].split("-")[0]
}