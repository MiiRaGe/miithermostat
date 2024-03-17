#! /bin/sh

gradle build
docker buildx build --platform linux/arm64 -t miirage/rpi-cluster:miithermostat-api-$1 -t miirage/rpi-cluster:miithermostat-api-latest --push .
