<?php

##
## Copyright (c) 1998-2000 NetUSE AG
##                    Boris Erdmann, Kristian Koehntopp
##
## Copyright (c) 1998-2000 Sascha Schumann <sascha@schumann.cx>
## 
## $Id$
##
## PHPLIB Data Storage Container using a SQL database
##

class CT_Sql {
  ##
  ## Define these parameters by overwriting or by
  ## deriving your own class from it (recommened)
  ##
    
  var $database_table = "active_sessions";
  var $database_class = "DB_Sql";
  var $database_lock_semaphore = "";

  var $encoding_mode = "base64";

  ## end of configuration

  var $db;

  function ac_start() {
    $name = $this->database_class;
    $this->db = new $name;
  }

  function ac_store($id, $name, $str) {
    $ret = true;

   switch ( $this->encoding_mode ) {
      case "slashes":
        $str = addslashes($name . ":" . $str);
      break;

      case "base64":
      default:
        $str = base64_encode($name . ":" . $str);
    };

    $name = addslashes($name);

    ## update duration of visit
    global $HTTP_REFERER, $HTTP_USER_AGENT, $REMOTE_ADDR;

    $now = date("YmdHis", time());
    $uquery = sprintf("update %s set val='%s', changed='%s' where sid='%s' and name='%s'",
      $this->database_table,
      $str,
      $now,
      $id,
      $name);
    $squery = sprintf("select count(*) from %s where val='%s' and changed='%s' and sid='%s' and name='%s'",
      $this->database_table,
      $str,
      $now,
      $id,
      $name);
    $iquery = sprintf("insert into %s ( sid, name, val, changed ) values ('%s', '%s', '%s', '%s')",
      $this->database_table,
      $id,
      $name,
      $str,
      $now);

    $this->db->query($uquery);

    # FIRST test to see if any rows were affected.
    #   Zero rows affected could mean either there were no matching rows
    #   whatsoever, OR that the update statement did match a row but made
    #   no changes to the table data (i.e. UPDATE tbl SET col = 'x', when
    #   "col" is _already_ set to 'x') so then,
    # SECOND, query(SELECT...) on the sid to determine if the row is in
    #   fact there,
    # THIRD, verify that there is at least one row present, and if there
    #   is not, then
    # FOURTH, insert the row as we've determined that it does not exist.
 
    if ( $this->db->affected_rows() == 0
        && $this->db->query($squery)
	&& $this->db->next_record() && $this->db->f(0) == 0
        && !$this->db->query($iquery)) {

        $ret = false;
    }
    return $ret;
  }


}
?>
