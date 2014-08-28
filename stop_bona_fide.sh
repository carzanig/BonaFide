#!/bin/bash

#Use Ctrl+V Ctrl+C to create the ^C character
#screen -x bonafide -X stuff $'\003'

server_process_id=$(cat bonafide.pid)
server_ps_ax_id=`ps -ef|grep -v grep|grep 'java -Djava.net.preferIPv4Stack=true -jar BonaFideServer/target/bonafideserver-1.0.0.jar' |awk '{print $2}'`

if [ ! -n "$server_process_id" ]
then
    echo "bonafideserver-1.0.0.jar process is not running"
else
    sudo kill -9 $server_process_id > /dev/null 2>&1
    sudo kill -9 $server_ps_ax_id > /dev/null 2>&1
    rm bonafide.pid
    echo "bonafideserver-1.0.0.jar process successfully stopped"
fi
