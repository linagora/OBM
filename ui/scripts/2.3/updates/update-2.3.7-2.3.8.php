<?php

$root = dirname(__FILE__).'/../../';
set_include_path(get_include_path().':'.$root.'/scripts/2.3/');

class GroupContactUpdate extends UpdateObject {

  private $addressbooks;

  public function main() {
    $query = 'DELETE FROM _userpattern';
    $result = $this->query($query);
    $result->free();
    $query = 'SELECT userobm_id, userobm_lastname, userobm_firstname, userobm_login, userobm_email FROM UserObm';
    $result = $this->query($query);
    while($result->next_record()) {
      $words = array();
      $mails = $result->f('userobm_email');
      $words = explode("\r\n",$mails);
      $words = array_merge($words, $this->splitwords($result->f('userobm_lastname')));
      $words = array_merge($words, $this->splitwords($result->f('userobm_firstname')));
      $words = array_merge($words, $this->splitwords($result->f('userobm_login')));
      $words = array_unique($words);
      foreach($words as $word) {
        if(trim($word) != '') {
          $query = "INSERT INTO _userpattern VALUES(".$result->f('userobm_id').", '".addslashes(trim($word))."')";
          $result2 = $this->query($query);
          $result2->free();
        }
      }
    }  
    $result->free();
  }


  function splitwords($string) {
    $mask = "/\p{L}[\p{L}\p{Mn}\x{2019}0-9]*/u";
    preg_match_all($mask, $string, $matches);
    $matches[0] = array_map('strtolower', $matches[0]);    
    return $matches[0];
  }
}


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
      include_once($dir.'/../lib/pgsql.inc');
    } elseif (strtoupper($conf['dbtype']) == 'MYSQL') {
      include_once($dir.'/../lib/mysql.inc');
    }
  }

  public function __get($key) {
    return $this->$key;
  }

  protected function query($query) {
    if(count(self::$free) > 0) {
      $con = array_shift(self::$free);
    } else {
      $con = new DB($this); 
    }
    $con->setId(count(self::$busy));
    self::$busy[$con->getId()] = $con;
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
}

$update = new GroupContactUpdate();
$update->main();
