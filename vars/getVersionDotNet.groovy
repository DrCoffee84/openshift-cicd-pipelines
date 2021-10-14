#!/usr/bin/env groovy

def call(parameters) {
    def version_prefix = sh(
        script: "set +x; sed -ne '/VersionPrefix/{s/.*<VersionPrefix>\\(.*\\)<\\/VersionPrefix>.*/\\1/p;q;}' ${parameters.csproj_file}; set -x",
        returnStdout: true
    ).trim()
    def version_suffix = sh(
        script: "set +x; sed -ne '/VersionSuffix/{s/.*<VersionSuffix>\\(.*\\)<\\/VersionSuffix>.*/\\1/p;q;}' ${parameters.csproj_file}; set -x",
        returnStdout: true
    ).trim()
    
    def tagname = "";
    if (version_suffix != "") {
        tagname = "${version_prefix}-${version_suffix}";
    } else if (version_prefix != "") {
        tagname = version_prefix;
    }
    
    if (tagname)
        return tagname
    else
        return "latest"
}