#!/bin/bash
# Grabs and kill a process from the pidlist that has the word AuthApi

pid=`ps aux | grep AuthApi | awk "{print $2}"`
kill -9 $pid

nohup python3 -m http.server 9000 -d /var/www/scripts/html/authapi &