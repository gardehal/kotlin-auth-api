#!/bin/bash
# Copy, truncate AuthApi.log

cat /var/www/logs/AuthApi.log >> /var/www/logs/AuthApi-$(date -u +%y%m%d%H%M).log
truncate -s 0 /var/www/logs/AuthApi.log