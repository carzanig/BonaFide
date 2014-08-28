<?php
/**
* Diverse utilities
*/
function generate_random_token() {
	return strtoupper(substr(md5(rand(100000000,999999999)),0,15));
}

// Time is in microseconds. Returned bandwidth will be in bytes/second
function calculate_bandwidth_bytes($bytes,$time) {
	return ceil($bytes/($time/1000000));
}

function normalize($x, $xmin, $xmax) {
	return ($x-$xmin)/($xmax-$xmin);
}

// config array is array for the particular scope, which is located in the config file. bandwidth is the protocol one
function calculate_mos_score($download_bandwidth, $upload_bandwidth, $latency, $config_array) {
	$e=exp(1);
	
	// check boundaries
	if ($download_bandwidth>$config_array["xmax-d-bandwidth"]) {
		$download_bandwidth=$config_array["xmax-d-bandwidth"];
	}
	if ($upload_bandwidth>$config_array["xmax-u-bandwidth"]) {
		$upload_bandwidth=$config_array["xmax-u-bandwidth"];
	}
	if ($latency>$config_array["xmax-latency"]) {
		$latency=$config_array["xmax-latency"];
	}
	// the same for minimum
	if ($download_bandwidth<$config_array["xmin-d-bandwidth"]) {
		$download_bandwidth=$config_array["xmin-d-bandwidth"];
	}
	if ($upload_bandwidth<$config_array["xmin-u-bandwidth"]) {
		$upload_bandwidth=$config_array["xmin-u-bandwidth"];
	}
	if ($latency<$config_array["xmin-latency"]) {
		$latency=$config_array["xmin-latency"];
	}
	
	// normalize
	if ($config_array["evaluate_download"]) $download_bandwidth=normalize($download_bandwidth,$config_array["xmin-d-bandwidth"],$config_array["xmax-d-bandwidth"]);
	if ($config_array["evaluate_upload"]) $upload_bandwidth=normalize($upload_bandwidth,$config_array["xmin-u-bandwidth"],$config_array["xmax-u-bandwidth"]);
	if ($config_array["evaluate_latency"]) $latency=normalize($latency,$config_array["xmin-latency"],$config_array["xmax-latency"]);
	
	// data are prepared
	// start computation
	$e_i_download=0;
	if ($config_array["evaluate_download"]) {
		$xzero=normalize($config_array["xzero-d-bandwidth"],$config_array["xmin-d-bandwidth"],$config_array["xmax-d-bandwidth"]);
		$x=$download_bandwidth; // already normalized
		$m=1;
		if ($download_bandwidth<$xzero) {
			$m=$config_array["mminus-d-bandwidth"];
		}
		if ($download_bandwidth>$xzero) {
			$m=$config_array["mplus-d-bandwidth"];
		}
		
		$e_i_download=4*(1-pow($e,-1*pow($x/$xzero,$m)*log(4)))+1;
	}
	
	$e_i_upload=0;
	if ($config_array["evaluate_upload"]) {
		$xzero=normalize($config_array["xzero-u-bandwidth"],$config_array["xmin-u-bandwidth"],$config_array["xmax-u-bandwidth"]);
		$x=$upload_bandwidth; // already normalized
		$m=1;
		if ($upload_bandwidth<$xzero) {
			$m=$config_array["mminus-u-bandwidth"];
		}
		if ($upload_bandwidth>$xzero) {
			$m=$config_array["mplus-u-bandwidth"];
		}
		
		$e_i_upload=4*(1-pow($e,-1*pow($x/$xzero,$m)*log(4)))+1;
	}
	
	$e_d_latency=0;
	if ($config_array["evaluate_latency"]) {
		$xzero=normalize($config_array["xzero-latency"],$config_array["xmin-latency"],$config_array["xmax-latency"]);
		$x=$latency; // already normalized
		$m=1;
		if ($latency<$xzero) {
			$m=$config_array["mminus-latency"];
		}
		if ($latency>$xzero) {
			$m=$config_array["mplus-latency"];
		}
		
		// formula for decreasing parameter
		$e_d_latency=4*pow($e,-1*pow($x/$xzero,$m)*log(4/3))+1;
	}
	
	
	// put everything together and compute E(X)
	$product_formula=1;
	if ($config_array["evaluate_download"]) {
		$product_formula*=pow(($e_i_download-1)/4,$config_array["wk-d-bandwidth"]);
	}
	
	if ($config_array["evaluate_upload"]) {
		$product_formula*=pow(($e_i_upload-1)/4,$config_array["wk-u-bandwidth"]);
	}
	
	if ($config_array["evaluate_latency"]) {
		$product_formula*=pow(($e_d_latency-1)/4,$config_array["wk-latency"]);
	}
	
	return 1+4*$product_formula;
}
?>