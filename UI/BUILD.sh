#!/bin/sh

cd sensor-graph
npm run build
cd ..
docker buildx build --platform linux/arm64 -t miirage/rpi-cluster:miithermostat-ui-$1 -t miirage/rpi-cluster:miithermostat-ui-latest --push .
