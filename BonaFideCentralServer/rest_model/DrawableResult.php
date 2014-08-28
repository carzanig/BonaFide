<?php
class DrawableResult {
	public $south_west_latitude;
	public $south_west_longitude;
	public $north_east_latitude;
	public $north_east_longitude;
	public $quality;
	
	function __construct()
    {
        $a = func_get_args();
        $i = func_num_args();
        if (method_exists($this,$f='__construct'.$i)) {
            call_user_func_array(array($this,$f),$a);
        }
    } 
	
	public function __construct5($south_west_latitude,$south_west_longitude,$north_east_latitude,$north_east_longitude,$quality) {
		$this->south_west_latitude=$south_west_latitude;
		$this->south_west_longitude=$south_west_longitude;
		$this->north_east_latitude=$north_east_latitude;
		$this->north_east_longitude=$north_east_longitude;
		$this->quality=$quality;
	}
	
	public function __construct0() {
		
	}
}
?>