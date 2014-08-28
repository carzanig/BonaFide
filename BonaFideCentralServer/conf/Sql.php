<?php
Class Sql {
	private $conn;
	public static $db_prefix = "DBPREFIXHERE_";
	
	public function connect() {
		$this->conn=@mysql_connect("SERVER","USERNAME","PASSWORD");
		@mysql_select_db("DATABASE",$this->conn);
		@mysql_query("SET CHARACTER SET 'utf8'");
		if (!$this->conn) return false;
		else return true;
	}
	
	public function disconnect() {
		mysql_close($this->conn);
	}
	
	public function getConnection() {
		return $this->conn;
	}
}
?>