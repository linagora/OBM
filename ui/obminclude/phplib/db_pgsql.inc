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
 * $Id$
 *
 */ 

class DB_Sql {
  var $Host     = "";
  var $Database = "";
  var $User     = "";
  var $Password = "";

  var $Link_ID  = 0;
  var $Query_ID = 0;
  var $Record   = array();
  var $Row      = 0;

  var $Seq_Table     = "db_sequence";

  var $Errno    = 0;
  var $Error    = "";
  var $Debug    = 0;

  var $Auto_Free = 0; # Set this to 1 for automatic pg_freeresult on 
                      # last record.

  /* public: last query string : added PB Aliacom */
  var $obm_query;

  var $boolean_values = array(  
    'is_default',
    'syncable',
    'contact_collected',
    'domain_global',
    'entity_mailing',
    'event_allday',
    'event_allow_documents',
    'profilesection_show');

  function ifadd($add, $me) {
          if("" != $add) return " ".$me.$add;
  }
  
  /* public: constructor */
  function DB_Sql($query = "") {
      $this->query("SET TIME ZONE 'GMT'");
      $this->query($query);
  }

  function test() {
    $link = pg_connect($this->buildConnectionString());
  
    if (!$link) {
      throw new Exception("Couldn't connect to database at '$this->Host' using login '$this->User'");
    }
  
    @pg_close($link);
  
    return true;
  }
  
  function buildConnectionString() {
    $cstr = "dbname=".$this->Database.
      $this->ifadd($this->Host, "host=").
      $this->ifadd($this->Port, "port=").
      $this->ifadd($this->User, "user=").
      $this->ifadd($this->Password, "password=");
    
    return $cstr;
  }
  
  function connect() {
    if ( 0 == $this->Link_ID ) {
      $this->Link_ID=pg_connect($this->buildConnectionString());
      if (!$this->Link_ID) {
	    $this->halt("Link-ID == false, connect failed");
      }
    }
  }

  // Added an argument to hide error reporting if it must be handled by OBM
  // Extends: in Dynamic lists, a user can fill a query, checked by OBM
  function query($Query_String, $hide_error="") {
    $this->cache['metadata'] = null;
    /* No empty queries, please, since PHP4 chokes on them. */
    if ($Query_String == "")
      /* The empty query string is passed on from the constructor,
       * when calling the class without a query, e.g. in situations
       * like these: '$db = new DB_Sql_Subclass;'
       */
      return 0;

    $this->connect();

    // Store the query string - PB Aliacom
    $Query_String = self::xParser($Query_String);    
    $this->obm_query = $Query_String;
    
    if ($this->Debug)
      printf("<br>Debug: query = %s<br>\n", $Query_String);

    // This echoes en error
    $this->Query_ID = pg_query($this->Link_ID, $Query_String);
    $this->Row   = 0;

    $this->Error = pg_ErrorMessage($this->Link_ID);
    $this->Errno = ($this->Error == "")?0:1;

    if (!$this->Query_ID) {
      // Added test (! $hide_error) - Aliacom - PB
      if ($hide_error) {
	return "obm:".$this->Error;
      } else {
	$this->halt("Invalid SQL: ".$Query_String);
      }
    }

    return $this->Query_ID;
  }

  function query_fmt(/* $fmt, ... */) {
    $args = func_get_args();
    if (count($args) < 2) {
      return false;
    }
    for ($i=1; $i<count($args); $i++) {
      $args[$i] = $this->escape($args[$i]);
    }
    return call_user_func_array('sprintf', $args);
  }
  
  function next_record($type=PGSQL_BOTH) {
    // AliaSource
    if ($type == 'BD_ASSOC') {
      $type = PGSQL_ASSOC;
    }
    $this->Record = @pg_fetch_array($this->Query_ID, $this->Row++, $type);

    $this->Error = pg_ErrorMessage($this->Link_ID);
    $this->Errno = ($this->Error == "")?0:1;

    $stat = is_array($this->Record);
    if($stat) {
      if($this->cache['metadata'] === null) {
        $this->cache['metadata'] = array();
        $count = $this->num_fields($this->Query_ID);
        for ($i=0; $i<$count; $i++) {
          $type = pg_FieldType  ($this->Query_ID, $i);
          $name = pg_FieldName  ($this->Query_ID, $i);
          if($this->mustBeParsed($type)) {
            $this->cache['metadata'][$i] = $type;
            $this->cache['metadata'][$name] = $type;
          }
        }
      }
      foreach($this->cache['metadata'] as $field => $type) {
        $this->Record[$field] = $this->parse($type, $this->Record[$field]);
      }
    }
    if (!$stat && $this->Auto_Free) {
      pg_freeresult($this->Query_ID);
      $this->Query_ID = 0;
    }
    return $stat;
  }

  function seek($pos) {
    $this->Row = $pos;
  }

  function lock($table, $mode = "write") {
    if ($mode == "write") {
      $result = pg_query($this->Link_ID, "lock table $table");
    } else {
      $result = 1;
    }
    return $result;
  }
  
  function unlock() {
    return pg_query($this->Link_ID, "commit");
  }

  function escape($string, $force = false) {
    if (!$force && get_magic_quotes_gpc())
      return $string;
    else
      return pg_escape_string($string);                                                                
  }      

  /* public: sequence numbers */
  function nextid($seq_name) {
    $this->connect();

    if ($this->lock($this->Seq_Table)) {
      /* get sequence number (locked) and increment */
      $q  = sprintf("select nextid from %s where seq_name = '%s'",
                $this->Seq_Table,
                $seq_name);
      $id  = @pg_query($this->Link_ID, $q);
      $res = @pg_Fetch_Array($id, 0);
      
      /* No current value, make one */
      if (!is_array($res)) {
        $currentid = 0;
        $q = sprintf("insert into %s values('%s', %s)",
                 $this->Seq_Table,
                 $seq_name,
                 $currentid);
        $id = @pg_query($this->Link_ID, $q);
      } else {
        $currentid = $res["nextid"];
      }
      $nextid = $currentid + 1;
      $q = sprintf("update %s set nextid = '%s' where seq_name = '%s'",
               $this->Seq_Table,
               $nextid,
               $seq_name);
      $id = @pg_query($this->Link_ID, $q);
      $this->unlock();
    } else {
      $this->halt("cannot lock ".$this->Seq_Table." - has it been created?");
      return 0;
    }
    return $nextid;
  }

  
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


  function metadata($table) {
    $count = 0;
    $id    = 0;
    $res   = array();
    
    $this->connect();
    $columns = $this->query("SELECT column_name, data_type, character_maximum_length FROM information_schema.columns c WHERE c.table_name ilike '$table'");
    if ($columns < 0) {
      $this->Error = pg_ErrorMessage($id);
      $this->Errno = 1;
      $this->halt("Metadata query failed.");
    }    
    
    while($this->next_record()) {
      $res[$this->f('column_name')]["table"] = $table;
      $res[$this->f('column_name')]["name"]  = $this->f('column_name'); 
      $res[$this->f('column_name')]["type"]  = $this->f('data_type');
      $res[$this->f('column_name')]["len"]   = $this->f('character_maximum_length');
      if($this->f('constraint_type')) {
        $res[$this->f('column_name')]["flags"] .= " ".$this->f('constraint_type');
      }
    }

    pg_FreeResult($columns);

    $constraints = $this->query("SELECT kcu.column_name, tc.constraint_type 
      FROM information_schema.key_column_usage kcu
      LEFT JOIN information_schema.table_constraints tc ON kcu.constraint_name = tc.constraint_name
      WHERE kcu.table_name ilike '$table'"); 

    if ($constraints < 0) {
      $this->Error = pg_ErrorMessage($id);
      $this->Errno = 1;
      $this->halt("Metadata query failed.");
    }
    
    while($this->next_record()) {
      if($this->f('constraint_type')) {
        $res[$this->f('column_name')]["flags"] .= " ".$this->f('constraint_type');
      }
    }
    
    pg_FreeResult($constraints);
    return $res;
  }

  function affected_rows() {
    return pg_cmdtuples($this->Query_ID);
  }

  function num_rows() {
    return pg_numrows($this->Query_ID);
  }

  function num_fields() {
    return pg_numfields($this->Query_ID);
  }

  function nf() {
    return $this->num_rows();
  }

  function np() {
    print $this->num_rows();
  }

  function f($Name) {
    return $this->Record[$Name];
  }

  function p($Name) {
    print $this->Record[$Name];
  }
  
  function halt($msg) {
  }

  function table_names() {
    $this->query("select relname from pg_class where relkind = 'r' and not relname like 'pg_%'");
    $i=0;
    while ($this->next_record())
     {
      $return[$i]["table_name"]= $this->f(0);
      $return[$i]["tablespace_name"]=$this->Database;
      $return[$i]["database"]=$this->Database;
      $i++;
     }
    return $return;
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

  function xquery_expand($query, $hide_error='') {
    $query = self::xParser($query);
    return $query;
  }

  // DEPRECATED
  function xquery($query, $hide_error='') {
    //$query = self::xParser($query);
    $this->query($query, $hide_error);
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
          $pcode = self::{$match[2][$key]}($args);
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
    return implode(' || ',$args);
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

  /**
   * NOW
   *
   * @static
   * @access public
   * @return string
   */
  static function NOW() {
    return "CURRENT_TIMESTAMP";
  }
}
?>
