<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : db_pgsql.inc                                                 //
//     - Desc : PostGreSQL Database PHPLIB class (updated for OBM)           //
// 2003-12-13 ALIACOM                                                        //
///////////////////////////////////////////////////////////////////////////////
/*
 * Session Management for PHP3
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp
 *
 * $Id: db_pgsql.inc 6112 2010-03-11 13:30:30Z mehdi $
 *
 */ 

class DB {

  var $link  = 0;
  var $queryId = 0;
  var $record   = array();
  var $current      = 0;
  var $errno    = 0;
  var $error    = "";

  private $id;
  private $object;


  public function __construct($object) {
      $this->object = $object;
      $this->query("SET TIME ZONE 'GMT'");
  }
  
  public function ifadd($add, $me) {
    if("" != $add) return " ".$me.$add;
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

  function connect() {
    if ( 0 == $this->link ) {
      $cstr = "dbname=".$this->object->database.
	$this->ifadd($this->object->host, "host=").
	$this->ifadd($this->object->port, "port=").
	$this->ifadd($this->object->user, "user=").
	$this->ifadd($this->object->password, "password=");
      $this->link=pg_connect($cstr);
      if (!$this->link) {
        throw new PGException("Link-ID == false, connect failed");
      }
    }
  }

  // Added an argument to hide error reporting if it must be handled by OBM
  // Extends: in Dynamic lists, a user can fill a query, checked by OBM
  function query($Query_String) {
    $this->cache['metadata'] = null;
    /* No empty queries, please, since PHP4 chokes on them. */
    if ($Query_String == "")
      /* The empty query string is passed on from the constructor,
       * when calling the class without a query, e.g. in situations
       * like these: '$db = new DB_Sql_Subclass;'
       */
      return 0;

    $this->connect();
    $Query_String =  self::xParser($Query_String); 


    // Store the query string - PB Aliacom
    $Query_String = self::xParser($Query_String);    
    
    // This echoes en error
    $this->queryId = pg_query($this->link, $Query_String);
    $this->current   = 0;

    $this->error = pg_errorMessage($this->link);
    $this->errno = ($this->error == "")?0:1;

    if (!$this->queryId) {
      throw new PGQueryException($Query_String.": ".$this->errno." (".$this->error.")");
    }

    return $this->queryId;
  }
  
  function next_record() {
    $this->record = @pg_fetch_array($this->queryId, $this->current++, PGSQL_BOTH);

    $this->error = pg_errorMessage($this->link);
    $this->errno = ($this->error == "")?0:1;

    $stat = is_array($this->record);
    if($stat) {
      if($this->cache['metadata'] === null) {
        $this->cache['metadata'] = array();
        $count = $this->num_fields($this->queryId);
        for ($i=0; $i<$count; $i++) {
          $type = pg_FieldType  ($this->queryId, $i);
          $name = pg_FieldName  ($this->queryId, $i);
          if($this->mustBeParsed($type)) {
            $this->cache['metadata'][$i] = $type;
            $this->cache['metadata'][$name] = $type;
          }
        }
      }
      foreach($this->cache['metadata'] as $field => $type) {
        $this->record[$field] = $this->parse($type, $this->record[$field]);
      }
    }
    return $stat;
  }

  function seek($pos) {
    $this->current = $pos;
  }

  /* public: sequence numbers */
  
  /**
   * lastid 
   * 
   * @access public
   * @return void
   */
  function lastid() {
    $this->query('SELECT LASTVAL() as id');
    $this->next_record();
    return $this->f('id');
  }


  function affected_rows() {
    return pg_cmdtuples($this->queryId);
  }

  function num_rows() {
    return pg_numrows($this->queryId);
  }

  function num_fields() {
    return pg_numfields($this->queryId);
  }

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
  
  function parse($type,$data) {
    switch(strtolower($type)) {
    case 'bool' :
      return ($data == 't')? TRUE:FALSE;
    default :
      return $data;
    }
  }

  function mustBeParsed($type) {
    switch(strtolower($type)) {
    case 'bool' :
      return true;
    default :
      return false;
    }
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
    $this->query("LOCK TABLE $table IN SHARE MODE");
  }

  function unlock_tables() {
      # No unlock command in Postgres
  }

  static function xParser($query) {
    // Search for any pseuso code substring (begining by #).
    // This is buggy for exemple field='#toto', this should not be interpretted
    // as pseudo  code, but it will.
    preg_match_all('/#[\w_]+(\((?:(?>[^()]+)|(?1))*\))?/', $query, $pcodes);
    $translate = array();
    foreach($pcodes[0] as $pcode) {
      $translate= array_merge($translate,self::interpret($pcode));
    }
    $query = strtr($query, $translate);
    return $query;
  }        
  
  /**
   * Interpret the sql pseudo code 
   * 
   * @param mixed $code 
   * @static
   * @access public
   * @return void
   */
  static function interpret($code) {
    // Search for #X, #X(), #X(Y), #X(Y()) and any subcombinaison of this
    // (#A(#B(Z, #C, #D(E)), F)) 
    // This is buggy for exemple #X('#toto').
    // #toto should not be interpreted as a pseudo code, but it will.

    preg_match_all("/(#)?([^ (,][^(,]*)(\(((?:(?>[^()]+)|(?3))*)\))?/",$code, $match);
    $return = array();
    foreach($match[0] as $key => $pattern) {
      if(!empty($match[3][$key])) {
        //Recursive call for embeb pseudo code (#X(#Y))
        $args = self::interpret($match[4][$key]);
      } else {
        $args = NULL;
      }
      //If the match is pseudo code (#X) it will be parsed =>
      // #X => self::X
      // #X(Y) => self:X(Y)
      if(!empty($match[1][$key])) {
        if(!is_null($args) && method_exists(__CLASS__, $match[2][$key])) {
          $pcode = self::$match[2][$key]($args);
        } elseif(property_exists(__CLASS__, $match[2][$key])) {
          $pcode = self::${$match[2][$key]};
        }  else {
          $pcode = $pattern;
        }            
      // Else only arguments will be replaced :
      // X(#Y) => X(self::Y) 
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

  static $LIKE = 'ILIKE';

  /**
   * ADDSECONDS 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function ADDSECONDS($args) {
    $date = current($args);
    if(strpos($date,"'") !== FALSE) $date = "TIMESTAMP $date";
    $seconds = next($args);
    return "($date + INTERVAL '1 SECOND' * $seconds)";
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
    if(strpos($date,"'") !== FALSE) $date = "TIMESTAMP $date";
    $seconds = next($args);
    return "($date - INTERVAL '1 SECOND' * $seconds)";
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
    return "EXTRACT(EPOCH from $date)";
  }  

  /**
   * MONTHDIFF 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function MONTHDIFF($args) {
    $start = next($args);
    $end = next($args);
    
    return "(EXTRACT(MONTH from AGE($end,$start)) + EXTRACT(YEAR from AGE($end,$start)) * 12)";
  }
  
  /**
   * DAYDIFF 
   * 
   * @param mixed $args 
   * @access public
   * @return void
   */
  static function DAYDIFF($args) {
    $seconds_in_a_day = 3600 * 24;
    
    $start = current($args);
    $end = next($args);
    
    return "FLOOR(EXTRACT(EPOCH from AGE($end,$start))/$seconds_in_a_day)";
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
    return '('.implode('||',$args).')'; 
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

class PGException extends Exception {
}

class PGQueryException extends PGException {
}

?>
