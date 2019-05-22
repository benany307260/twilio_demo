#!/bin/bash
PID=$(ps -ef | grep rcos_service.jar | grep -v grep | awk '{ print $2 }')
if [ -z "$PID" ]
then
    echo rcos_service is already stopped
else
    echo kill $PID
    kill $PID
fi