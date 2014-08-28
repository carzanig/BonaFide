<?php
/**
* REST interface for handling measurement servers queries
*/

//ini_set('display_errors',1); 
//error_reporting(E_ALL);

require "conf/Sql.php";
require "conf/bonafide.php";
require "utils.php";
require "config.php";

// require only ...Response classes. All dependent classes are automatically required
require "rest_model/DrawableResultResponse.php";

$conn=new Sql();

if (!$conn->connect()) {
	// SQL failied
	header($_SERVER['SERVER_PROTOCOL'] . ' 500 Internal Server Error', true, 500);
	exit();
}

header("Content-Type:application/json");

// provided by .htaccess
$url=$_GET["url"];

// parse $url and deliver responses
$url_parsed=explode("/",$url);

// /measurement_servers
if ($url_parsed[0]=="measurement-servers") {
	// if add...
	
	// list servers based on actual position.
	if ($url_parsed[1]=="list") {
		// /list/latitude/longitude
		if (!empty($url_parsed[2]) && !empty($url_parsed[3]) && is_numeric($url_parsed[2]) && is_numeric($url_parsed[3])) {
			$latitude=$url_parsed[2];
			$longitude=$url_parsed[3];
			
			// server listing ordered by distance from the client
			$query=mysql_query("SELECT id,ip,name,port,latitude,longitude,DISTANCE(".$latitude.",".$longitude.",latitude,longitude) AS distance FROM ".Sql::$db_prefix."measurement_servers WHERE last_seen_timestamp>=".(time()-$bonafide_conf["measurement_server_timeout"]-$bonafice_conf["measurement_server_latency"])." ORDER BY distance ASC");
		}
		// /list
		else {
			// basic server listing
			$query=mysql_query("SELECT id,ip,name,port,latitude,longitude FROM ".Sql::$db_prefix."measurement_servers WHERE last_seen_timestamp>=".(time()-$bonafide_conf["measurement_server_timeout"]-$bonafice_conf["measurement_server_latency"])." ORDER BY name");
		}
		
		$response=array();
		$response["status"]="OK";
		$response["status_message"]="";
		$response["servers"]=array();
		
		while ($out=mysql_fetch_assoc($query)) {
			$response["servers"][]=$out;
		}
		
		echo json_encode($response);
	}
	else if ($url_parsed[1]=="add" && $_SERVER['REQUEST_METHOD']=="POST") {
		// add new or update existing server
		$postdata = file_get_contents("php://input");
		
		$post_array = json_decode($postdata,true);
		
		//print_r($post_array);
			if ($post_array==NULL) {
				$response=array("status"=>"error","status_message"=>"Invalid JSON input");
				echo json_encode($response);
			}
			else {
				$name=mysql_real_escape_string($post_array["name"]);
				$port=mysql_real_escape_string($post_array["port"]);
				$ip=$_SERVER['REMOTE_ADDR'];
				$hostname=gethostbyaddr($ip);
				$latitude=mysql_real_escape_string($post_array["latitude"]);
				$longitude=mysql_real_escape_string($post_array["longitude"]);
				$timestamp=time();
				
				// TODO: check server availability by reverse connection
				
				if (!empty($port) && !empty($latitude) && !empty($longitude) && is_numeric($port) && is_numeric($latitude) && is_numeric($longitude)) {
					// valid inputs
					$query=mysql_query("INSERT INTO ".Sql::$db_prefix."measurement_servers (ip,hostname,port,name,last_seen_timestamp,latitude,longitude) VALUES ('".$ip."','".$hostname."','".$port."','".$name."','".$timestamp."','".$latitude."','".$longitude."') ON DUPLICATE KEY UPDATE hostname='".$hostname."',port='".$port."',name='".$name."',last_seen_timestamp='".$timestamp."',latitude='".$latitude."',longitude='".$longitude."'");
					
					$response=array("status"=>"OK","status_message"=>"","next_advertisement_delay"=>$bonafide_conf["measurement_server_timeout"]);
					echo json_encode($response);
				}
				else {
					$response=array("status"=>"error","status_message"=>"Invalid format of input variables");
					echo json_encode($response);
				}
			}
	}
	else {
		$response=array("status"=>"error","status_message"=>"Operation not supported");
		echo json_encode($response);
	}
} // end if /measurement-servers
else if ($url_parsed[0]=="measurement-results") {
	if ($url_parsed[1]=="add" && $_SERVER['REQUEST_METHOD']=="POST") {
		// process input
		$postdata = file_get_contents("php://input");
		$post_array = json_decode($postdata,true);
		
		//print_r($post_array);

			if ($post_array==NULL) {
				$response=array("status"=>"error","status_message"=>"Invalid JSON input");
				echo json_encode($response);
			}
			else {
				$token=mysql_real_escape_string($post_array["user_token"]);
				
				
				
				// check if user token available
				$query=mysql_query("SELECT user_token FROM ".Sql::$db_prefix."tokens WHERE user_token='".$token."'");
				if (mysql_num_rows($query)==0) {
					// token is not yet assigned
					// -> create a new one
					$is_unique=false;
					
					while (!$is_unique) {
						$token=generate_random_token();
						$query=mysql_query("INSERT INTO ".Sql::$db_prefix."tokens (user_token,created_datetime) VALUES ('".$token."','".date("Y-m-d H:i:s")."')");

						// check if successful
						if (mysql_affected_rows()==1) {
							$is_unique=true;
							// update token in the input
							$post_array["user_token"]=$token;
						}
						else {
							// repeat
						}
					}
				}
				
				// prepare data
				if ($post_array["is_mobile_network"]!="true" && $post_array["is_mobile_network"]!="false") {
					$post_array["is_mobile_network"]="false";
				}
				$post_array["measurement_datetime"]=date("Y-m-d H:i:s");
				
				// calculate bandwidth:
				$post_array["upload_random_bandwidth"]=calculate_bandwidth_bytes($post_array["upload_random_bytes_sent"],$post_array["upload_random_roundtrip_time"]);
				$post_array["upload_protocol_bandwidth"]=calculate_bandwidth_bytes($post_array["upload_protocol_bytes_sent"],$post_array["upload_protocol_roundtrip_time"]);
				$post_array["download_random_bandwidth"]=calculate_bandwidth_bytes($post_array["download_random_bytes_sent"],$post_array["download_random_roundtrip_time"]);
				$post_array["download_protocol_bandwidth"]=calculate_bandwidth_bytes($post_array["download_protocol_bytes_sent"],$post_array["download_protocol_roundtrip_time"]);
				$post_array["measurement_location"]="NULL";
				if (isset($post_array["latitude"]) && isset($post_array["longitude"]) && is_numeric($post_array["latitude"]) && is_numeric($post_array["longitude"]) && $post_array["latitude"]!=$config["latitude_unknown"] && $post_array["longitude"]!=$config["longitude_unknown"]) {
					// we have coordinates
					$post_array["measurement_location"]="GeomFromText('POINT(".$post_array["latitude"]." ".$post_array["longitude"].")')";
				}
				
				// generate insert string
				$db_column_names="user_token,measurement_datetime,measurement_server_name,measurement_server_id,protocol_specification_name,is_mobile_network,network_type,country,operator,operator_name,signal_strength,upload_random_roundtrip_time,upload_random_bytes_sent,upload_random_bandwidth,upload_random_completness,upload_protocol_roundtrip_time,upload_protocol_bytes_sent,upload_protocol_bandwidth,upload_protocol_completness,download_random_roundtrip_time,download_random_bytes_sent,download_random_bandwidth,download_random_completness,download_protocol_roundtrip_time,download_protocol_bytes_sent,download_protocol_bandwidth,download_protocol_completness,download_total_bytes,upload_total_bytes,measurement_location,latency";
				$db_column_names_array=explode(",",$db_column_names);
				$ins_string_columns="";
				$ins_string_values="";
				for ($i=0; $i<count($db_column_names_array); $i++) {
					if ($ins_string_columns!="") $ins_string_columns.=",";
					$ins_string_columns.=$db_column_names_array[$i];
					
					if ($ins_string_values!="") $ins_string_values.=",";
					if ($db_column_names_array[$i]=="measurement_location") {
						// location must be inserted without ''
						// input is checked (sql injection)
						$ins_string_values.=$post_array[$db_column_names_array[$i]];
					}
					else {
						$ins_string_values.="'".mysql_real_escape_string($post_array[$db_column_names_array[$i]])."'";
					}
				}
				
				// insert result to database
				$query=mysql_query("INSERT INTO ".Sql::$db_prefix."measurement_results (".$ins_string_columns.") VALUES (".$ins_string_values.")");
				
				$response=array();
				$response["status"]="OK";
				$response["status_message"]="";
				$response["user_token"]=$token;
				$response["measurement_results"]=array(); // leave empty for serialozation
				
				echo json_encode($response);
			}
	}
	else if ($url_parsed[1]=="list" && $_SERVER['REQUEST_METHOD']=="GET") {
		// list all results
		if (!isset($url_parsed[2])) $url_parsed[2]="";
		$user_token=$url_parsed[2]; // if not set then empty
		
		$query=mysql_query("SELECT measurement_datetime,measurement_server_name,protocol_specification_name,latency,is_mobile_network,network_type,country,operator,operator_name,signal_strength,upload_random_roundtrip_time,upload_random_bytes_sent,upload_random_bandwidth,upload_random_completness,upload_protocol_roundtrip_time,upload_protocol_bytes_sent,upload_protocol_bandwidth,upload_protocol_completness,download_random_roundtrip_time,download_random_bytes_sent,download_random_bandwidth,download_random_completness,download_protocol_roundtrip_time,download_protocol_bytes_sent,download_protocol_bandwidth,download_protocol_completness,download_total_bytes,upload_total_bytes, COALESCE(X(measurement_location),".$config["latitude_unknown"].") AS latitude, COALESCE(Y(measurement_location),".$config["longitude_unknown"].") AS longitude FROM ".Sql::$db_prefix."measurement_results WHERE user_token='".$user_token."' ORDER BY measurement_datetime DESC");
		
		$response=array();
		$response["status"]="OK";
		$response["status_message"]="";
		$response["user_token"]=$user_token;
		$response["measurement_results"]=array();
		
		while ($out=mysql_fetch_assoc($query)) {
		$out["user_token"]=$user_token;	
			$response["measurement_results"][]=$out;
		}

		echo json_encode($response);
	}
	else if ($url_parsed[1]=="list-for-viewport" && $_SERVER['REQUEST_METHOD']=="POST") { // POST because we need to reviede the JSON request
		if (!empty($url_parsed[2]) && !empty($url_parsed[3]) && !empty($url_parsed[4]) && !empty($url_parsed[5]) && !empty($url_parsed[6]) && is_numeric($url_parsed[2]) && is_numeric($url_parsed[3]) && is_numeric($url_parsed[4]) && is_numeric($url_parsed[5]) && is_numeric($url_parsed[6])) {
			// viewport coordinates
			$south_west_latitude=$url_parsed[2];
			$south_west_longitude=$url_parsed[3];
			$north_east_latitude=$url_parsed[4];
			$north_east_longitude=$url_parsed[5];
			
			$zoom_level=$url_parsed[6];
			
			// filter is passed via json
			$postdata = file_get_contents("php://input");
			$post_array = json_decode($postdata,true);
			
			// create empty response
			$response=new DrawableResultResponse();
			
			// default target_scope
			$requested_column=$config["default_scope"];
			// default
			$response->target_scope=$config["default_scope"];
			$mos_scope=""; // empty means we dont return MOS
			
			//print_r($post_array);
			$applied_filters=array();
			$applied_filters_query_injection="";
			$filter_enabled=false;
	
				if ($post_array!=NULL) {
					if ($post_array["filters_active"]=="1") {
						$filter_enabled=true;
					}
					
					// prepare target scope
					$target_scope=$post_array["target_scope"];
					if (in_array($target_scope,$config["available_scopes"])) {
						// mos?
						if (array_key_exists($target_scope,$config["mos_scopes"])) {
							// we are in MOS scope
							$mos_scope=$config["mos_scopes"][$target_scope];
							// hardcoded in the query
							//$requested_column="download_protocol_bandwidth,upload_protocol_bandwidth,latency";
						}
						else {
							$requested_column=$target_scope;
						}
						
						$response->target_scope=$target_scope;
					}
				
					// prepare filter, which will be applied for results
					$filters_array=$post_array["apply_filters"];

					if ($filter_enabled==true) {
						for ($i=0; $i<count($filters_array); $i++) {
							if (in_array($filters_array[$i]["filter_name"],$config["available_filters"])) {
								$response->add_applied_filter($filters_array[$i]);
							
								$filter=new DrawableResultFilter();
								$filter->filter_name=$filters_array[$i]["filter_name"];
								$filter->filter_options=$filters_array[$i]["filter_options"];
								// add to filters
								$applied_filters[]=$filter;
								
								// add to query injection
								$applied_filters_query_injection.=" AND ";
								
								$applied_filters_query_injection.=$filter->filter_name." IN (";
								
								// iterate over options
								$c=0;
								foreach ($filter->filter_options as $filter_option) {
									if ($c>0) $applied_filters_query_injection.=",";
									$applied_filters_query_injection.="'".mysql_real_escape_string($filter_option)."'";
									$c++;
									
								}
								
								$applied_filters_query_injection.=")";
							}
						}
					}
				}
			
			
			
			
			// we have input, so lets work on the output
			
			// split screen into squares - size is computed based on the latitude
			$square_side_size=($north_east_longitude-$south_west_longitude)/$config["num_squares_horizontal"];
			$vertical_bottom_coordinate=$south_west_latitude; // bottom coordinates of the viewport
			//echo $north_east_latitude;
			
			// fill available filters
			foreach ($config["available_filters"] as $filter_column) {
				$query=mysql_query("SELECT ".$filter_column." FROM ".Sql::$db_prefix."measurement_results GROUP BY ".$filter_column." ORDER BY ".$filter_column);
				
				$filter=new DrawableResultFilter();
				$filter->filter_name=$filter_column;
				
				while ($out=mysql_fetch_array($query)) {
					$filter->add_filter_option($out[0]);
				}
				
				$response->add_available_filter($filter);
				// if filters are off, available filters are filled always with all possible filters
				if ($filter_enabled==false) {
					$response->add_applied_filter($filter);
				}
			}
			
			
			// fill available scopes
			foreach ($config["available_scopes"] as $scope_column) {
				$response->add_available_target_scope($scope_column);
			}
			
			
			if ($square_side_size>=$config["minimum_square_side_length"]) {
				// square size is ok
				
				// create and populate squares
				$current_y_latitude=$north_east_latitude; // top of current square (used for rendering squares along Y-axis)
				// render squares along Y-axis
				//echo $current_y_latitude." ".$vertical_bottom_coordinate;
				
				// get maximum and minimum for the viewport
				// results view height can be higher then the height of the viewport due to the bottom square.
				// we will get min and max for all squares, so we need the bottom latitude
				$num_squares_vertical=ceil(($north_east_latitude-$south_west_latitude)/$square_side_size);
				$bottom_square_south_latitude=$north_east_latitude-($num_squares_vertical*$square_side_size);
				
				// now we can compute min and max within the viewport+square overflow are
				// only applicable for non-MOS scopes
				if ($mos_scope=="") {
					$query=mysql_query("SELECT MIN(".$requested_column.") AS min, MAX(".$requested_column.") AS max FROM ".Sql::$db_prefix."measurement_results WHERE within(measurement_location,GeomFromText('POLYGON((".$bottom_square_south_latitude." ".$south_west_longitude.",".$bottom_square_south_latitude." ".$north_east_longitude.",".$north_east_latitude." ".$north_east_longitude.",".$north_east_latitude." ".$south_west_longitude.",".$bottom_square_south_latitude." ".$south_west_longitude."))') ) AND ".$requested_column.">0".$applied_filters_query_injection);
				}
				$viewport_min=0;
				$viewport_max=0;
				if ($out=mysql_fetch_array($query)) {
					$viewport_min=$out["min"];
					$viewport_max=$out["max"];
				}
				
				while ($current_y_latitude>$vertical_bottom_coordinate) {
					// now render X axis
					for ($i=0; $i<$config["num_squares_horizontal"]; $i++) {
						$square = new DrawableResult();
						
						// compute square coordinates
						$square->north_east_latitude=$current_y_latitude;
						$square->north_east_longitude=$south_west_longitude+($square_side_size*($i+1));
						$square->south_west_latitude=$current_y_latitude-$square_side_size;
						$square->south_west_longitude=$south_west_longitude+($square_side_size*($i));
						
						// quality value depends from the applied filter
						// this query is applicable only for non-MOS scopes
						if ($mos_scope=="") {
							$query=mysql_query("SELECT count(*) AS aggregated_count, AVG(".$requested_column.") AS ".$requested_column."_avg FROM ".Sql::$db_prefix."measurement_results WHERE within(measurement_location,GeomFromText('POLYGON((".$square->south_west_latitude." ".$square->south_west_longitude.",".$square->south_west_latitude." ".$square->north_east_longitude.",".$square->north_east_latitude." ".$square->north_east_longitude.",".$square->north_east_latitude." ".$square->south_west_longitude.",".$square->south_west_latitude." ".$square->south_west_longitude."))') ) AND ".$requested_column.">0".$applied_filters_query_injection);
						}
						else {
							// query for MOS scope
							$query=mysql_query("SELECT download_protocol_bandwidth,upload_protocol_bandwidth,latency FROM ".Sql::$db_prefix."measurement_results WHERE within(measurement_location,GeomFromText('POLYGON((".$square->south_west_latitude." ".$square->south_west_longitude.",".$square->south_west_latitude." ".$square->north_east_longitude.",".$square->north_east_latitude." ".$square->north_east_longitude.",".$square->north_east_latitude." ".$square->south_west_longitude.",".$square->south_west_latitude." ".$square->south_west_longitude."))') ) AND protocol_specification_name='".$config["mos"][$mos_scope]["protocol_scope"]."' AND download_protocol_bandwidth>0 AND upload_protocol_bandwidth>0 AND latency>0".$applied_filters_query_injection);
						}
						
						
						if (($results=mysql_fetch_array($query))) {
							// we have results
							$aggregated_count=$mos_scope=="" ? $results["aggregated_count"] : mysql_num_rows($query);
							if ($aggregated_count>=$config["minimum_square_aggregation"]) {
								if ($mos_scope=="") {
									
									// compute span
									$span=$viewport_max-$viewport_min; // 100%
									$quality_in_span=$results[$requested_column."_avg"]-$viewport_min; // x %
									
									$square->quality=round(($quality_in_span/$span)*100); // x
									
									$response->add_result($square);
								}
								else {
									// mos scope
									// return avg MOS value for the region and convert it to color segments
									
									// first result is already fetched
									$mos_count=1;
									$mos_sum=calculate_mos_score($results["download_protocol_bandwidth"], $results["upload_protocol_bandwidth"], $results["latency"], $config["mos"][$mos_scope]);
									// sum other lines
									while ($results=mysql_fetch_array($query)) {
										$mos_count++;
										$mos_sum+=calculate_mos_score($results["download_protocol_bandwidth"], $results["upload_protocol_bandwidth"], $results["latency"], $config["mos"][$mos_scope]);
									}
									
									// calculate average
									$mos_avg=$mos_sum/$mos_count;
									
									// output segmented color
									$square->quality=config_clasify_mos($mos_avg);
									$response->add_result($square);
									
								}
							}
						}
					}
					// move 1 row down
					$current_y_latitude-=$square_side_size;
				}
			}
			else {
				// under the limit, so return no results
			}
			
			//$response=new DrawableResultResponse();
			
			// TODO: implement
			//$response->add_result(new DrawableResult($south_west_latitude,$south_west_longitude,$north_east_latitude,$north_east_longitude,100));
			
			
			/*
			$response=array();
			$response["status"]="OK";
			$response["status_message"]="";
			$response["results"]=array();
			
			$response["results"][0]["south_west_latitude"]=$south_west_latitude;
			$response["results"][0]["south_west_longitude"]=$south_west_longitude;
			$response["results"][0]["north_east_latitude"]=$north_east_latitude;
			$response["results"][0]["north_east_longitude"]=$north_east_longitude;
			$response["results"][0]["quality"]="100";
			*/
			//mail("info@smspripomienky.sk","debug",print_r($post_array,true));
			echo json_encode($response);
		}
		else {
			$response=array("status"=>"error","status_message"=>"Invalid input for operation");
			echo json_encode($response);
		}
	}
	else {
		$response=array("status"=>"error","status_message"=>"Invalid operation of missing arguments");
		echo json_encode($response);
	}
} // end measurement-results

else {
	$response=array("status"=>"error","status_message"=>"Operation not supported");
	echo json_encode($response);
}

?>