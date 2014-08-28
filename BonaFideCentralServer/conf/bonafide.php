<?php
/*
 * Bonafide central server configuration.
 */
 
// time to the next keep-alive message required from measurement server in seconds
$bonafide_conf["measurement_server_timeout"]=1*60;

// time reserve for measurement server keep-alive message in seconds. Measurement server must respond in (timeout+latency) to be considered online
$bonafice_conf["measurement_server_latency"]=5;
?>