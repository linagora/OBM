<?php

class UpdateObject {

  protected static $free;
  protected static $busy;

  protected $database;
  protected $host;
  protected $user;
  protected $password;

  public function __construct() {
    if(!is_array(self::$free)) self::$free = array(); 
    if(!is_array(self::$busy)) self::$busy = array(); 
    $dir = dirname(__FILE__);
    $conf = parse_ini_file($dir.'/../../../conf/obm_conf.ini');
    $this->database = $conf['db'];
    $this->host = $conf['host'];
    $this->user = $conf['user'];
    $this->password = $conf['password'];
    if(strtoupper($conf['dbtype']) == 'PGSQL') {
      include_once($dir.'/pgsql.inc');
    } elseif (strtoupper($conf['dbtype']) == 'MYSQL') {
      include_once($dir.'/mysql.inc');
    }
  }

  public function __get($key) {
    return $this->$key;
  }

  protected function get_con() {
    if(count(self::$free) > 0) {
      $con = array_shift(self::$free);
    } else {
      $con = new DB($this);
    }
    $con->setId(count(self::$busy));
    self::$busy[$con->getId()] = $con;
    return $con;
  }

  public function query($query) {
    $con = $this->get_con();
    $con->query($query);
    return $con;
  }

  public function free($id) {
    $con = self::$busy[$id];
    if(is_object($con)) {
      $con->setId(null);
      unset(self::$busy[$id]);
      self::$free[] = $con;
    }
  }

  public function begin() {
    $con = $this->get_con();
    $con->begin();
    $this->free($con->getId());
  }

  public function commit() {
    $con = $this->get_con();
    $con->commit();
    $this->free($con->getId());
  }

  public function rollback() {
    $con = $this->get_con();
    $con->rollback();
    $this->free($con->getId());
  }

  public function lock_table_for_writing($table) {
    $con = $this->get_con();
    $con->lock_table_for_writing($table);
    $this->free($con->getId());
  }

  public function unlock_tables() {
    $con = $this->get_con();
    $con->unlock_tables();
    $this->free($con->getId());
  }
}
?>
