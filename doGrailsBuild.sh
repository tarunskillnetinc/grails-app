#!/bin/bash

doExit(){ 
    exit 1
}

trap doExit ERR

isSnapshot=$(grep 'version[a-zA-Z0-9,-]*' build.gradle | head -1 | grep "SNAPSHOT" | wc -l)
versionString=$(grep 'version[a-zA-Z0-9,-]*' build.gradle | head -1)
timestamp=$(date +%s)
versionWithoutTimestamp=
version=$(grep 'version[a-zA-Z0-9,-]*' build.gradle | head -1 | cut -d " " -f 2 | sed 's/^.//;s/.$//')

if [ $1 == 'development' ]
then
    repo=libs-snapshot-local

    if [ $isSnapshot == 0 ]
    then
        versionWithoutTimestamp=$version-SNAPSHOT
        version=$version-$timestamp-SNAPSHOT
        sed -i "s#$versionString#version \"$version\"#" build.gradle
    else
        newVersionString=$(echo "$version" | sed "s/-SNAPSHOT//")
        versionWithoutTimestamp=$newVersionString-SNAPSHOT
        version=$newVersionString-$timestamp-SNAPSHOT
        sed -i "s#$versionString#version \"$version\"#" build.gradle
    fi
else
    repo=libs-release-local
    versionWithoutTimestamp=$version

    if [ $isSnapshot == 1 ]
    then
        version=$(echo "$version" | sed "s/-SNAPSHOT//")
        versionWithoutTimestamp=$version
        sed -i "s#$versionString#version \"$version\"#" build.gradle
    fi

    if [ $1 == 'prestage' ]
    then
        versionString=$(grep 'version[a-zA-Z0-9,-]*' build.gradle | head -1)
        version=$version-$timestamp
        sed -i "s#$versionString#version \"$version\"#" build.gradle
    fi
fi

curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh" && sdk install grails 5.3.2
echo "artifactory_user=$2" >> /home/gradle/.gradle/gradle.properties
echo "artifactory_password=$3" >> /home/gradle/.gradle/gradle.properties
chmod +x gradlew
./gradlew --stacktrace clean test bootRun
/root/.sdkman/candidates/grails/5.3.2/bin/grails -Dgrails.env=$1 assemble --info

ls /builds/securetrading-gl/retail-platform/retail-platform/cloud-office/build/libs/

curl -u $2:$3 -X PUT "https://wonderlane.jfrog.io/artifactory/$repo/uk/co/wonderlane/wlpos/wlpos-back-office/$versionWithoutTimestamp/$1/wlpos-back-office-$version.jar" -T /builds/securetrading-gl/retail-platform/retail-platform/cloud-office/build/libs/cloud-office-$version.jar
