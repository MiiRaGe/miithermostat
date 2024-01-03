#! /usr/bin/bash


export API_URL='http://localhost/api'
export DB_HOST='localhost'
export DB_NAME='miithermostat'
export DB_USER='miithermostat'
export DB_PASSWORD='localpassword'
touch /tmp/miithermostat.sqlite

sudo nginx -s quit
sudo cp WebServer/miithermostat_nginx.conf /etc/nginx/sites-enabled/default
sudo nginx
tmuxinator