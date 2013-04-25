<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : db_mysql.inc                                                 //
//     - Desc : MySQL Database PHPLIB class (updated for OBM)                //
// 2003-12-13 ALIACOM                                                        //
///////////////////////////////////////////////////////////////////////////////
/*
 * Session Management for PHP3
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp
 *
 * $Id: db_mysql.inc 6112 2010-03-11 13:30:30Z mehdi $
 *
 */ 

class DB {
  

  /* public: result array and current row number */
  var $record   = array();
  var $current;

  /* public: current error number and error text */
  var $errno    = 0;
  var $error    = "";

  /* private: link and query handles */
  var $link  = 0;
  var $queryId = 0;

  
  private $id;
  private $object;

  

  /* public: constructor */
  public function __construct($object) {
      $this->object = $object;
      $this->query('SET NAMES UTF8'); 
      $this->query("SET TIME_ZONE='+00:00'"); 
  }

  public function free() {
    $this->object->free($this->id);
    $this->queryId = null;
  }

  public function setId($id) {
    $this->id = $id;
  }

  public function getId() {
    return $this->id;
  }

  /* public: some trivial reporting */
  function link_id() {
    return $this->link;
  }

  function query_id() {
    return $this->queryId;
  }

  /* public: connection management */
  function connect() {
    /* establish connection, select database */
    if ( 0 == $this->link ) {
    
      $this->link=mysql_pconnect($this->object->host, $this->object->user, $this->object->password);
      if (!$this->link) {
        throw new MySQLException("PConnect(".$this->object->host.", ".$this->object->user.", ".$this->object->password.") failed.");
      }

      if (!@mysql_select_db($this->object->database,$this->link)) {
        throw new MySQLException("Cannot use database ".$this->object->database);
        return 0;
      }
    }
    return $this->link;
  }

  function query($Query_String) {
    if ($Query_String == "")
      return 0;
    if (!$this->connect()) {
      return 0; 
    }
    
    $Query_String = self::xParser($Query_String);

    if ($this->queryId) {
      $this->queryId = null;
    }

    $this->queryId = @mysql_query($Query_String,$this->link);
    $this->errno = mysql_errno();
    $this->error = mysql_error();

    if (!$this->queryId) {
      throw new MySQLQueryException($Query_String.": ".$this->errno." (".$this->error.")");
    }

    # Will return nada if it fails. That's fine.
    return $this->queryId;
  }

  /* public: walk result set */
  function next_record() {
    if (!$this->queryId) {
      throw new MySQLException("next_record called with no query pending.");
    }
    $this->record = @mysql_fetch_array($this->queryId, MYSQL_BOTH);
    $this->current   += 1;
    $this->errno  = mysql_errno();
    $this->error  = mysql_error();

    $stat = is_array($this->record);
    return $stat;
  }

  /* public: evaluate the result (size, width) */
  function affected_rows() {
    return @mysql_affected_rows($this->link);
  }

  function num_rows() {
    return @mysql_num_rows($this->queryId);
  }

  function num_fields() {
    return @mysql_num_fields($this->queryId);
  }

  /* public: shorthand notation */
  function nf() {
    return $this->num_rows();
  }

  function np() {
    print $this->num_rows();
  }

  function f($Name) {
    return $this->record[$Name];
  }

  function p($Name) {
    print $this->record[$Name];
  }

  /**
   * lastid 
   * 
   * @access public
   * @return void
   */
  function lastid() {
    $this->query('SELECT LAST_INSERT_ID() as id');
    $this->next_record();
    return $this->f('id');
  }

  function begin() {
    $this->query('BEGIN');
  }

  function commit() {
    $this->query('COMMIT');
  }

  function rollback() {
    $this->query('ROLLBACK');
  }

  function lock_table_for_writing($table) {
    if ( is_array($table) ) {
      $query = array();
      foreach ( $table as $t ) {
        $query [] = $t." WRITE";
      }
      return $this->query("LOCK TABLES ".implode(",",$query));
    }
    $this->query("LOCK TABLES $table WRITE");
  }

  function unlock_tables() {
    $this->query('UNLOCK TABLES');
  }

  static function xParser($query) {
    preg_match_all('/#[\w_]+(\((?:(?>[^()]+)|(?1))*\))?/', $query, $pcodes);
    $translate = array();
    foreach($pcodes[0] as $pcode) {
      $translate= array_merge($translate,self::interpret($pcode));
    }
    $query = strtr($query, $translate);
    return $query;
  } 

  static function interpret($code) {
    preg_match_all("/(#)?([^ (,][^(,]*)(\(((?:(?>[^()]+)|(?3))*)\))?/",$code, $match);
    $return = array();
    foreach($match[0] as $key => $pattern) {
      if(!empty($match[3][$key])) {
        $args = self::interpret($match[4][$key]);
      } else {
        $args = NULL;
      }
      if(!empty($match[1][$key])) {
        if(!is_null($args) && method_exists(__CLASS__, $match[2][$key])) {
          $pcode = self::$match[2][$key]($args);
        } elseif(property_exists(__CLASS__, $match[2][$key])) {
          $pcode = self::${$match[2][$key]};
        }  else {
          $pcode = $pattern;
        }        
      }else  {
        if(!is_null($args)) {
          $pcode = $match[2][$key].'('.implode(',', $args).')';
        } else {
          $pcode = $match[2][$key];
        }
      }
      $return[$pattern] = $pcode;
    }
    return $return;
  }

  static $LIKE = 'LIKE';

  /**
   * ADDSECONDS 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function ADDSECONDS($args) {
    $date = current($args);
    $seconds = next($args);
    return "TIMESTAMPADD(SECOND,$seconds,$date)";
  }

  /**
   * SUBSECONDS 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function SUBSECONDS($args) {
    $date = current($args);
    $seconds = next($args);
    return "TIMESTAMPADD(SECOND,-$seconds,$date)";
  }    

  /**
   * TIMESTAMP 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function TIMESTAMP($args) {
    $date = current($args);
    return "UNIX_TIMESTAMP($date)";
  }

  /**
   * MONTHDIFF 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function MONTHDIFF($args) {
    $start = current($args);
    $end = next($args);
    
    return "TIMESTAMPDIFF(MONTH,$start,$end)";
  }
  
  /**
   * DAYDIFF 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function DAYDIFF($args) {
    $start = current($args);
    $end = next($args);
    
    return "TIMESTAMPDIFF(DAY,$start,$end)";
  }

  /**
   * CONCAT 
   * 
   * @param mixed $args 
   * @static
   * @access public
   * @return void
   */
  static function CONCAT($args) {
    return 'CONCAT('.implode(',',$args).')'; 
  }  


  /**
   * CAST 
   * 
   * @param mixed $args 
   * @static
   * @access public
   * @return void
   */
  static function CAST($args) {
    $data = current($args);
    $type = strtoupper(next($args));    
    switch ($type) {
    case 'INTEGER':
      $type =  'UNSIGNED';
      break;
    }
    return "CAST($data as $type)"; 
  }  

    /**
   * INT 
   * 
   * @param mixed $args 
   * @static
   * @access public
   * @return void
   */
  static function INT($args) {
    $int = current($args);
    return (is_numeric($int))?(int)$int:'NULL';
  }
}

class MySQLException extends Exception {
}

class MySQLQueryException extends MySQLException {
}
?>
