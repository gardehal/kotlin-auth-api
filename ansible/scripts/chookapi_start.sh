#!/bin/bash
# Grabs and kill a process from the pidlist that has the phrase http.server

pid=`ps aux | grep http.server | awk "{print $2}"`
kill -9 $pid

cd /var/www/authapi
nohup java -jar AuthApi.jar >> /var/www/logs/AuthApi.log 2>&1 &