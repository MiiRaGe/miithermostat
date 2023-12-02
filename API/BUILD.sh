#! /bin/sh

gradle build
cp app/build/distributions/app.tar miithermostat-latest.tar
git add miithermostat-latest.tar
