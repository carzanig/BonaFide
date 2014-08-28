#main_socket_port="4000"
#specifications_list="list"
#incoming_results_storage="results"


#Check if BonaFide Server is running
server_process_id=`ps -ef|grep -v grep|grep 'java -Djava.net.preferIPv4Stack=true -jar BonaFideServer/target/bonafideserver-1.0.0.jar' |awk '{print $2}'`
if [ ! -n "$server_process_id" ]
then
    #Run BonaFide server component
    java -Djava.net.preferIPv4Stack=true -jar BonaFideServer/target/bonafideserver-1.0.0.jar > bonafide.out 2> bonafide.out &

    sleep 1

    server_process_id=`ps -ef|grep -v grep|grep 'java -Djava.net.preferIPv4Stack=true -jar BonaFideServer/target/bonafideserver-1.0.0.jar' |awk '{print $2}'`
    if [ ! -n "$server_process_id" ]
	then 
	echo "FAILED: Can't start bonafideserver-1.0.0.jar process."
	exit
	else
	echo "bonafideserver-1.0.0.jar process successfully started"
	echo $server_process_id > bonafide.pid
	fi

else
    echo "bonafideserver-1.0.0.jar process is already started"
fi



