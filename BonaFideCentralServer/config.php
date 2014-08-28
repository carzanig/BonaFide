<?php
// BONAFIDE CONFIGURATION

// number of squares displayed on the map along the horizontal axis
$config["num_squares_horizontal"]=10;
// viewport minimum size - protection against revealing single measurements
$config["minimum_square_side_length"]=0; // in (delta) longitude
// square must aggregate minimum results. 1 means there must be minimum 1 measurement result within the square to display it
$config["minimum_square_aggregation"]=1;

// available filters. Filter = column name
$config["available_filters"]=array("protocol_specification_name","network_type","operator_name","country");
// which scopes should be enabled
// mos is constant for mean-opinion-score and the rest are column names
$config["available_scopes"]=array("mos_browsing","mos_video","mos_voip","download_protocol_bandwidth","download_random_bandwidth","upload_protocol_bandwidth","upload_random_bandwidth","latency","signal_strength");
// which scopes are used for MOS and which type it is. The type is the array name of the mos config
$config["mos_scopes"]=array("mos_browsing"=>"browsing", "mos_video"=>"video", "mos_voip"=>"voip");
//$config["mos_scopes"]=array("mos_browsing"=>"video", "mos_video"=>"browsing", "mos_voip"=>"voip");
// which scope should be selected by default
$config["default_scope"]="download_protocol_bandwidth";

// function for mapping MOS averages into colors
// note: color is automatically computed for span between 0 and 100, where 0 is bad, 100 is best
function config_clasify_mos($mos) {
	if ($mos<=1.8) return 0;
	if ($mos<=2.6) return 25;
	if ($mos<=3.4) return 50;
	if ($mos<=4.2) return 75;
	if ($mos<=5.0) return 100;
}

###### Mean Opinion Score configuration
$config["mos"]["browsing"]["protocol_scope"]="FlashVideo";
$config["mos"]["browsing"]["evaluate_download"]=true;
$config["mos"]["browsing"]["evaluate_upload"]=false;
$config["mos"]["browsing"]["evaluate_latency"]=true;
$config["mos"]["browsing"]["xzero-d-bandwidth"]=1330*1024; // in bytes per second
$config["mos"]["browsing"]["xzero-u-bandwidth"]=0; // in bytes per second
$config["mos"]["browsing"]["xzero-latency"]=523; // in ms
$config["mos"]["browsing"]["xmin-d-bandwidth"]=0; // in bps
$config["mos"]["browsing"]["xmin-u-bandwidth"]=0; // in bps
$config["mos"]["browsing"]["xmin-latency"]=1; // in ms
$config["mos"]["browsing"]["xmax-d-bandwidth"]=50.1*1024*1024; // in bps
$config["mos"]["browsing"]["xmax-u-bandwidth"]=0; // in bps
$config["mos"]["browsing"]["xmax-latency"]=2000; // in ms
$config["mos"]["browsing"]["mplus-d-bandwidth"]=2.58;
$config["mos"]["browsing"]["mplus-u-bandwidth"]=0;
$config["mos"]["browsing"]["mplus-latency"]=6.29;
$config["mos"]["browsing"]["mminus-d-bandwidth"]=2.41;
$config["mos"]["browsing"]["mminus-u-bandwidth"]=0;
$config["mos"]["browsing"]["mminus-latency"]=10.17;
$config["mos"]["browsing"]["wk-d-bandwidth"]=0.75;
$config["mos"]["browsing"]["wk-u-bandwidth"]=0;
$config["mos"]["browsing"]["wk-latency"]=0.25;


$config["mos"]["video"]["protocol_scope"]="HTTP";
$config["mos"]["video"]["evaluate_download"]=true;
$config["mos"]["video"]["evaluate_upload"]=false;
$config["mos"]["video"]["evaluate_latency"]=false;
$config["mos"]["video"]["xzero-d-bandwidth"]=1.5*1024*1024; // in bytes per second
$config["mos"]["video"]["xzero-u-bandwidth"]=0; // in bytes per second
$config["mos"]["video"]["xzero-latency"]=0; // in ms
$config["mos"]["video"]["xmin-d-bandwidth"]=0; // in bps
$config["mos"]["video"]["xmin-u-bandwidth"]=0; // in bps
$config["mos"]["video"]["xmin-latency"]=0; // in ms
$config["mos"]["video"]["xmax-d-bandwidth"]=50.1*1024*1024; // in bps
$config["mos"]["video"]["xmax-u-bandwidth"]=0; // in bps
$config["mos"]["video"]["xmax-latency"]=0; // in ms
$config["mos"]["video"]["mplus-d-bandwidth"]=1.49;
$config["mos"]["video"]["mplus-u-bandwidth"]=0;
$config["mos"]["video"]["mplus-latency"]=0;
$config["mos"]["video"]["mminus-d-bandwidth"]=3.11;
$config["mos"]["video"]["mminus-u-bandwidth"]=0;
$config["mos"]["video"]["mminus-latency"]=0;
$config["mos"]["video"]["wk-d-bandwidth"]=1;
$config["mos"]["video"]["wk-u-bandwidth"]=0;
$config["mos"]["video"]["wk-latency"]=0;


$config["mos"]["voip"]["protocol_scope"]="VoIP-H323";
$config["mos"]["voip"]["evaluate_download"]=true;
$config["mos"]["voip"]["evaluate_upload"]=true;
$config["mos"]["voip"]["evaluate_latency"]=true;
$config["mos"]["voip"]["xzero-d-bandwidth"]=8*1024; // in bytes per second
$config["mos"]["voip"]["xzero-u-bandwidth"]=8; // in bytes per second
$config["mos"]["voip"]["xzero-latency"]=120; // in ms
$config["mos"]["voip"]["xmin-d-bandwidth"]=0; // in bps
$config["mos"]["voip"]["xmin-u-bandwidth"]=0; // in bps
$config["mos"]["voip"]["xmin-latency"]=1; // in ms
$config["mos"]["voip"]["xmax-d-bandwidth"]=50.1*1024*1024; // in bps
$config["mos"]["voip"]["xmax-u-bandwidth"]=12.7*1024*1024; // in bps
$config["mos"]["voip"]["xmax-latency"]=2000; // in ms
$config["mos"]["voip"]["mplus-d-bandwidth"]=0.86;
$config["mos"]["voip"]["mplus-u-bandwidth"]=0.86;
$config["mos"]["voip"]["mplus-latency"]=2.16;
$config["mos"]["voip"]["mminus-d-bandwidth"]=1.67;
$config["mos"]["voip"]["mminus-u-bandwidth"]=1.67;
$config["mos"]["voip"]["mminus-latency"]=10.17;
$config["mos"]["voip"]["wk-d-bandwidth"]=0.25;
$config["mos"]["voip"]["wk-u-bandwidth"]=0.25;
$config["mos"]["voip"]["wk-latency"]=0.5;



// DONT CHANGE
$config["latitude_unknown"]=300.0;
$config["longitude_unknown"]=300.0;
?>