<?php
class DrawableResultFilter {
	public $filter_name="filter name";
	public $filter_options=array(); // string array of possible options
	
	public function add_filter_option($option) {
		$this->filter_options[]=$option;
	}
}
?>