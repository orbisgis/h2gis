#!/bin/bash
mvn install
# Build H2GIS dependent projects
# Get last child project build number
BUILD_NUM=$(curl -s 'https://api.travis-ci.org/repos/orbisgis/H2GIS/builds' | grep -o '^\[{"id":[0-9]*,' | grep -o '[0-9]' | tr -d '\n')
# Restart last child project build
curl -X POST https://api.travis-ci.org/builds/$BUILD_NUM/restart --header "Authorization: token "$AUTH_TOKEN

exit $?
