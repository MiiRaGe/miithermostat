#! /usr/bin/bash


export API_URL='http://localhost/api'
export DB_OVERRIDE='jdbc:sqlite:/tmp/miithermostat.sqlite'
touch /tmp/miithermostat.sqlite

sudo nginx -s quit
sudo cp WebServer/miithermostat_nginx.conf /etc/nginx/sites-enabled/default
sudo nginx
tmuxinator