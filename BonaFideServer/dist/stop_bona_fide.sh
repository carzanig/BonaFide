#!/bin/bash

#Check if BonaFide Server is running
server_process_id=`ps -ef|grep -v grep|grep 'java -jar BonaFideServer.jar' |awk '{print $2}'`
if [ ! -n "$server_process_id" ]
then
	echo "BonaFideServer.jar process is not running"
else
	sudo kill -9 $server_process_id
	echo "BonaFideServer.jar process successfully stopped"
fi

