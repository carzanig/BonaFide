<?php
require_once(dirname(__FILE__)."/DrawableResultFilter.php");
require_once(dirname(__FILE__)."/DrawableResult.php");

/**
* Class modelling DrawableResultResponse
*/

class DrawableResultResponse {
	const STATUS_OK="OK";
	const STATUS_ERROR="ERROR";
	
	public $status = self::STATUS_OK;
	public $status_message="";
	public $target_scope="";
	public $available_target_scopes=array();
	public $available_filters=array();
	public $applied_filters=array();
	public $results=array();
	
	public function add_available_filter($filter) {
		$this->available_filters[]=$filter;
	}
	
	public function add_applied_filter($filter) {
		$this->applied_filters[]=$filter;
	}
	/*public function add_applied_filters($filters) {
		$this->applied_filters=$filters;
	}*/
	
	public function add_result($result) {
		$this->results[]=$result;
	}
	
	public function add_available_target_scope($scope) {
		$this->available_target_scopes[]=$scope;
	}
}
?>