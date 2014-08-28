#!/bin/bash

# The following configuration should be set in bonafide.conf
#main_socket_port="4000"
#specifications_list="list"
#incoming_results_storage="results"


#Check if BonaFide Server is running
server_process_id=`ps -ef|grep -v grep|grep 'java -jar BonaFideServer.jar' |awk '{print $2}'`
if [ ! -n "$server_process_id" ]
then
	#Run BonaFide server component
	nohup java -jar BonaFideServer.jar > bonafideserver.out 2> bonafideserver.out &

	sleep 1

	server_process_id=`ps -ef|grep -v grep|grep 'java -jar BonaFideServer.jar' |awk '{print $2}'`
	if [ ! -n "$server_process_id" ]
	then 
		echo "FAILED: Can't start BonaFideServer.jar process."
		exit
	else
		echo "BonaFideServer.jar process successfully started"
	fi

else
	echo "BonaFideServer.jar process is already started"
fi




