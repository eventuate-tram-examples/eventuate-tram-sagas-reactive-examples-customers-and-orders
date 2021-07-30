#! /bin/bash

set -e


./gradlew testClasses

dockerinfrastructure="./gradlew ${DATABASE?}infrastructureCompose"
dockerall="./gradlew ${DATABASE?}Compose"

${dockerall}Down
${dockerinfrastructure}Up

./gradlew -x :end-to-end-tests:test build

${dockerall}Up

./gradlew :end-to-end-tests:cleanTest :end-to-end-tests:test

${dockerall}Down
