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
 * $Id$
 *
 */ 

class DB_Sql {
  
  /* public: connection parameters */
  var $Host     = "";
  var $Database = "";
  var $User     = "";
  var $Password = "";

  /* public: configuration parameters */
  var $Auto_Free     = 0;     ## Set to 1 for automatic mysql_free_result()
  var $Debug         = 0;     ## Set to 1 for debugging messages.
  var $Halt_On_Error = "yes"; ## "yes" (halt with message), "no" (ignore errors quietly), "report" (ignore errror, but spit a warning)
  var $Seq_Table     = "db_sequence";

  /* public: result array and current row number */
  var $Record   = array();
  var $Row;

  /* public: last query string : added PB Aliacom */
  var $obm_query;

  /* public: current error number and error text */
  var $Errno    = 0;
  var $Error    = "";

  /* public: this is an api revision, not a CVS revision. */
  var $type     = "mysql";
  var $revision = "1.2";

  /* private: link and query handles */
  var $Link_ID  = 0;
  var $Query_ID = 0;
  

  /* public: constructor */
  function DB_Sql($query = "") {
      $this->query('SET NAMES UTF8'); 
      $this->query("SET TIME_ZONE='+00:00'"); 
      $this->query($query);
  }

  /* public: some trivial reporting */
  function link_id() {
    return $this->Link_ID;
  }

  function query_id() {
    return $this->Query_ID;
  }
  
  function test() {
    $link = @mysql_pconnect($this->Host, $this->User, $this->Password);
    
    if (!$link) {
      throw new Exception("Couldn't connect to database at '$this->Host' using login '$this->User'");
    }
    
    if (!@mysql_select_db($this->Database, $link)) {
      throw new Exception("The database '$this->Database' doesn't exist");
    }
    
    @mysql_close($link);
    
    return true;
  }

  /* public: connection management */
  function connect($Database = "", $Host = "", $User = "", $Password = "") {
    /* Handle defaults */
    if ("" == $Database)
      $Database = $this->Database;
    if ("" == $Host)
      $Host     = $this->Host;
    if ("" == $User)
      $User     = $this->User;
    if ("" == $Password)
      $Password = $this->Password;
      
    /* establish connection, select database */
    if ( 0 == $this->Link_ID ) {
    
      $this->Link_ID=@mysql_pconnect($Host, $User, $Password);
      if (!$this->Link_ID) {
        $this->halt("pconnect($Host, $User, \$Password) failed.");
        return 0;
      }

      if (!@mysql_select_db($Database,$this->Link_ID)) {
        $this->halt("cannot use database ".$this->Database);
        return 0;
      }
    }
    return $this->Link_ID;
  }

  /* public: discard the query result */
  function free() {
      @mysql_free_result($this->Query_ID);
      $this->Query_ID = 0;
  }

  /* public: perform a query */
  // Added an argument to hide error reporting if it must be handled by OBM
  // Extends: in Dynamic lists, a user can fill a query, checked by OBM
  function query($Query_String, $hide_error="") {
    /* No empty queries, please, since PHP4 chokes on them. */
    if ($Query_String == "")
      /* The empty query string is passed on from the constructor,
       * when calling the class without a query, e.g. in situations
       * like these: '$db = new DB_Sql_Subclass;'
       */
      return 0;
    if (!$this->connect()) {
      return 0; /* we already complained in connect() about that. */
    }
    
    $Query_String =  self::xParser($Query_String); 

    // Store the query string - PB Aliacom
    $this->obm_query = $Query_String;
    if ($this->Query_ID) {
      $this->free();
    }

    if ($this->Debug)
      printf("Debug: query = %s<br>\n", $Query_String);

    $this->Query_ID = @mysql_query($Query_String,$this->Link_ID);
    $this->Row   = 0;
    $this->Errno = mysql_errno();
    $this->Error = mysql_error();

    if (!$this->Query_ID) {
      // Added test (! $hide_error) - Aliacom - PB
      if ($hide_error) {
	return "obm:".$this->Error;
      } else {
	$this->halt("Invalid SQL: ".$Query_String);
      }
    }

    # Will return nada if it fails. That's fine.
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

  /* public: walk result set */
  function next_record($type=MYSQL_BOTH) {
    if (!$this->Query_ID) {
      $this->halt("next_record called with no query pending.");
      return 0;
    }
    // AliaSource
    if ($type == 'BD_ASSOC') {
      $type = MYSQL_ASSOC;
    }
    $this->Record = @mysql_fetch_array($this->Query_ID, $type);
    $this->Row   += 1;
    $this->Errno  = mysql_errno();
    $this->Error  = mysql_error();

    $stat = is_array($this->Record);
    if (!$stat && $this->Auto_Free) {
      $this->free();
    }
    return $stat;
  }

  /* public: position in result set */
  function seek($pos = 0) {
    $status = @mysql_data_seek($this->Query_ID, $pos);
    if ($status)
      $this->Row = $pos;
    else {
      $this->halt("seek($pos) failed: result has ".$this->num_rows()." rows");

      /* half assed attempt to save the day, 
       * but do not consider this documented or even
       * desireable behaviour.
       */
      @mysql_data_seek($this->Query_ID, $this->num_rows());
      $this->Row = $this->num_rows;
      return 0;
    }

    return 1;
  }

  /* public: table locking */
  function lock($table, $mode="write") {
    $this->connect();
    
    $query="lock tables ";
    if (is_array($table)) {
      while (list($key,$value)=each($table)) {
        if ($key=="read" && $key!=0) {
          $query.="$value read, ";
        } else {
          $query.="$value $mode, ";
        }
      }
      $query=substr($query,0,-2);
    } else {
      $query.="$table $mode";
    }
    $res = @mysql_query($query, $this->Link_ID);
    if (!$res) {
      $this->halt("lock($table, $mode) failed.");
      return 0;
    }
    return $res;
  }
  
  function unlock() {
    $this->connect();

    $res = @mysql_query("unlock tables", $this->Link_ID);
    if (!$res) {
      $this->halt("unlock() failed.");
      return 0;
    }
    return $res;
  }

  function escape($string, $force = false) {                                          
    if (!$force && get_magic_quotes_gpc())
      return $string;
    else                                                                    
      return mysql_real_escape_string($string);                
  }

  /* public: evaluate the result (size, width) */
  function affected_rows() {
    return @mysql_affected_rows($this->Link_ID);
  }

  function num_rows() {
    return @mysql_num_rows($this->Query_ID);
  }

  function num_fields() {
    return @mysql_num_fields($this->Query_ID);
  }

  /* public: shorthand notation */
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

  /* public: sequence numbers */
  function nextid($seq_name) {
    $this->connect();
    
    if ($this->lock($this->Seq_Table)) {
      /* get sequence number (locked) and increment */
      $q  = sprintf("select nextid from %s where seq_name = '%s'",
                $this->Seq_Table,
                $seq_name);
      $id  = @mysql_query($q, $this->Link_ID);
      $res = @mysql_fetch_array($id);
      
      /* No current value, make one */
      if (!is_array($res)) {
        $currentid = 0;
        $q = sprintf("insert into %s values('%s', %s)",
                 $this->Seq_Table,
                 $seq_name,
                 $currentid);
        $id = @mysql_query($q, $this->Link_ID);
      } else {
        $currentid = $res["nextid"];
      }
      $nextid = $currentid + 1;
      $q = sprintf("update %s set nextid = '%s' where seq_name = '%s'",
               $this->Seq_Table,
               $nextid,
               $seq_name);
      $id = @mysql_query($q, $this->Link_ID);
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
    $this->query('SELECT LAST_INSERT_ID() as id');
    $this->next_record();
    return $this->f('id');
  }

  /* public: return table metadata */
  function metadata($table='',$full=false) {
    $count = 0;
    $id    = 0;
    $res   = array();

    /*
     * Due to compatibility problems with Table we changed the behavior
     * of metadata();
     * depending on $full, metadata returns the following values:
     *
     * - full is false (default):
     * $result[]:
     *   [0]["table"]  table name
     *   [0]["name"]   field name
     *   [0]["type"]   field type
     *   [0]["len"]    field length
     *   [0]["flags"]  field flags
     *
     * - full is true
     * $result[]:
     *   ["num_fields"] number of metadata records
     *   [0]["table"]  table name
     *   [0]["name"]   field name
     *   [0]["type"]   field type
     *   [0]["len"]    field length
     *   [0]["flags"]  field flags
     *   ["meta"][field name]  index of field named "field name"
     *   The last one is used, if you have a field name, but no index.
     *   Test:  if (isset($result['meta']['myfield'])) { ...
     */

    // if no $table specified, assume that we are working with a query
    // result
    if ($table) {
      $this->connect();
      $id = @mysql_list_fields($this->Database, $table);
      if (!$id)
        $this->halt("Metadata query failed.");
    } else {
      $id = $this->Query_ID; 
      if (!$id)
        $this->halt("No query specified.");
    }
 
    $count = @mysql_num_fields($id);

    // made this IF due to performance (one if is faster than $count if's)
    if (!$full) {
      for ($i=0; $i<$count; $i++) {
        $res[$i]["table"] = @mysql_field_table ($id, $i);
        $res[$i]["name"]  = @mysql_field_name  ($id, $i);
        $res[$i]["type"]  = @mysql_field_type  ($id, $i);
        $res[$i]["len"]   = @mysql_field_len   ($id, $i);
        $res[$i]["flags"] = @mysql_field_flags ($id, $i);
      }
    } else { // full
      $res["num_fields"]= $count;
    
      for ($i=0; $i<$count; $i++) {
        $res[$i]["table"] = @mysql_field_table ($id, $i);
        $res[$i]["name"]  = @mysql_field_name  ($id, $i);
        $res[$i]["type"]  = @mysql_field_type  ($id, $i);
        $res[$i]["len"]   = @mysql_field_len   ($id, $i);
        $res[$i]["flags"] = @mysql_field_flags ($id, $i);
        $res["meta"][$res[$i]["name"]] = $i;
      }
    }
    
    // free the result only if we were called on a table
    if ($table) @mysql_free_result($id);
    return $res;
  }

  /* private: error handling */
  function halt($msg) {
    $this->Error = @mysql_error($this->Link_ID);
    $this->Errno = @mysql_errno($this->Link_ID);
    if ($this->Halt_On_Error == "no")
      return;

    $this->haltmsg($msg);
  }

  function haltmsg($msg) {
  }

  function table_names() {
    $this->query("SHOW TABLES");
    $i=0;
    while ($info=mysql_fetch_row($this->Query_ID))
     {
      $return[$i]["table_name"]= $info[0];
      $return[$i]["tablespace_name"]=$this->Database;
      $return[$i]["database"]=$this->Database;
      $i++;
     }
   return $return;
  }

  function xquery_expand($query, $hide_error='') {
    $query = self::xParser($query);
    return $query;
  }

  // TODO: DEPRECATED
  function xquery($query, $hide_error='') {
    $this->query($query, $hide_error);
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

  /**
   * NOW
   *
   * @static
   * @access public
   * @return string
   */
  static function NOW() {
    return "NOW()";
  }
}
?>
