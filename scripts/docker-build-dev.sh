#!/bin/bash

VERSION=$(ls build/libs | sed 's/.*halo-//' | sed 's/.jar$//')

echo "Halo version: $VERSION"

echo "$DOCKER_PASSWORD" | docker login registry.cn-hangzhou.aliyuncs.com -u "$DOCKER_USERNAME" --password-stdin
docker build --build-arg JAR_FILE="build/libs/halo-$VERSION.jar" -t registry.cn-hangzhou.aliyuncs.com/$DOCKER_USERNAME/halo:latest-dev -t registry.cn-hangzhou.aliyuncs.com/$DOCKER_USERNAME/halo:$VERSION.dev .
docker images
docker push registry.cn-hangzhou.aliyuncs.com/$DOCKER_USERNAME/halo
