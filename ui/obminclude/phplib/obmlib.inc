<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obmlib.inc                                                   //
//     - Desc : Local PHPLIB file for OBM                                    //
// 1999-03-18 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$db_type_mysql = "MYSQL";
$db_type_pgsql = "PGSQL";

///////////////////////////////////////////////////////////////////////////////
// Required packages                                                         //
///////////////////////////////////////////////////////////////////////////////
// Package for the selected DB backend
if ($obmdb_dbtype == $db_type_pgsql) {
  require_once("$obminclude/phplib/db_pgsql.inc");
} elseif ($obmdb_dbtype == $db_type_mysql) {
  require_once("$obminclude/phplib/db_mysql.inc");
} else {
  require_once("$obminclude/phplib/db_mysql.inc");
}
require("$obminclude/phplib/ct_sql.inc");
require("$obminclude/of/of_session.inc");
require("$obminclude/phplib/auth.inc");

///////////////////////////////////////////////////////////////////////////////
// OBM Classes                                                               //
///////////////////////////////////////////////////////////////////////////////

class DB_OBM extends DB_Sql {
  var $Host     = "";
  var $Database = "";
  var $type     = "";
  var $User     = "";
  var $Password = "";
  var $Rows_nolimit = "";

  function DB_OBM ($query = "") {
    global $obmdb_host, $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;

    $this->Host     = "$obmdb_host";
    $this->type     = "$obmdb_dbtype";
    $this->Database = "$obmdb_db";
    $this->User     = "$obmdb_user";
    $this->Password = "$obmdb_password";

    $this->query($query);
  }

  // Aliacom : Set the total rows number (case query is constraint by limit)
  function set_num_rows_total ($total) {
    $this->Rows_nolimit = $total;
  }

  // Aliacom : Get the total rows number (case query is constraint by limit)
  function num_rows_total () {
    if ($this->Rows_nolimit != "")
      return $this->Rows_nolimit;
    else
      return $this->num_rows();
  }
}



class OBM_User_Sql extends CT_Sql {
  var $database_class = "DB_OBM";         // Which database to connect...
  var $database_table = "ActiveUserObm";  // our session data in this table.

  function ac_store($id, $name, $str) {
    $ret = true;
    $name = addslashes($name);

    // update duration of visit
    global $HTTP_REFERER, $HTTP_SERVER_VARS, $HTTP_USER_AGENT, $REMOTE_ADDR, $action;
    $module_name = substr(strrchr($HTTP_SERVER_VARS['SCRIPT_NAME'],"/"),1);
    $module_name = substr($module_name,0,strlen($module_name)-10);
    $now = date("Y-m-d H:i:s", time());
    $uquery = sprintf("update %s
      set activeuserobm_userobm_id='%d',
        activeuserobm_timeupdate='%s',
        activeuserobm_nb_connexions = activeuserobm_nb_connexions + 1,
        activeuserobm_lastpage= '%s'
      where activeuserobm_sid='%s' and activeuserobm_session_name='%s'",
        $this->database_table,
        $str,
        $now,
        ucfirst($module_name).":".ucfirst($action),
        $id,
        $name);
    $squery = sprintf("select count(*)
      from %s
      where activeuserobm_userobm_id='%s'
        and activeuserobm_timeupdate='%s'
        and activeuserobm_sid='%s'
        and activeuserobm_session_name='%s'",
      $this->database_table,
      $str,
      $now,
      $id,
      $name);
    $iquery = sprintf("insert into %s (
        activeuserobm_sid,
        activeuserobm_session_name,
        activeuserobm_userobm_id,
        activeuserobm_timeupdate,
        activeuserobm_timecreate,
        activeuserobm_nb_connexions,
        activeuserobm_lastpage,
        activeuserobm_ip
      ) values ('%s', '%s', '%d', '%s','%s', '%d', '%s', '%s')",
      $this->database_table,
      $id,
      $name,
      $str,
      $now,
      $now,
      1, 
      ucfirst($module_name).":".ucfirst($action),
      $REMOTE_ADDR);

    $this->db->query($uquery);

    // FIRST test to see if any rows were affected.
    //   Zero rows affected could mean either there were no matching rows
    //   whatsoever, OR that the update statement did match a row but made
    //   no changes to the table data (i.e. UPDATE tbl SET col = 'x', when
    //   "col" is _already_ set to 'x') so then,
    // SECOND, query(SELECT...) on the sid to determine if the row is in
    //   fact there,
    // THIRD, verify that there is at least one row present, and if there
    //   is not, then
    // FOURTH, insert the row as we've determined that it does not exist.
 
    if ( $this->db->affected_rows() == 0
        && $this->db->query($squery)
	&& $this->db->next_record() && $this->db->f(0) == 0
        && !$this->db->query($iquery)) {

        $ret = false;
    }
    return $ret;
  }
}


class OBM_Session extends Session {
  var $cookie_name     	= "OBM_Session";
//  var $cookie_domain  	= "aliacom.local";
}


class OBM_Challenge_Auth extends Auth {
  var $classname      = "OBM_Challenge_Auth";
  var $lifetime       = 1200;
  var $magic          = "Simsalabim";  // Challenge seed
  var $database_class = "DB_OBM";
  var $database_table = "UserObm";

  function auth_loginform() {
    global $sess, $obminclude, $obm_version, $menu, $path, $challenge, $login_action;

    $login_form = "$obminclude/auth/crloginform.ihtml";
    $challenge = md5(uniqid($this->magic));
    $sess->register("challenge");
    
    include($login_form);
  }
  
  function auth_validatelogin() {
    global $login, $password, $challenge, $response;

    if (isset($login)) {
      $this->auth["uname"] = $login; // provides access for loginform.ihtml
    }

    $this->db->query(sprintf("select userobm_id,
        userobm_perms,
        userobm_password
      from %s
      where userobm_login = '%s'
        and userobm_archive = '0'",
                          $this->database_table,
                          addslashes($login)));

    while ($this->db->next_record()) {
      $uid  = $this->db->f("userobm_id");
      $perm = $this->db->f("userobm_perms");
      $pass = $this->db->f("userobm_password");
    }
    $exspected_response = md5("$login:$pass:$challenge");

    // True when JS is disabled
    if ($response == "") {
      if ($password != $pass) {
        return false;
      } else {
        $this->auth["perm"] = $perm;
        return $uid;
      }
    }
    
    // Response is set, JS is enabled
    if ($exspected_response != $response) {
      return false;
    } else {
      $this->auth["perm"] = $perm;
      return $uid;
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// OBM_Perm Class                                                            //
// Profile and right definitions                                             //
///////////////////////////////////////////////////////////////////////////////
class OBM_Perm {
  var $classname      = "OBM_Perm";
  var $mod_length     = 2;             // chars by module rights (1 byte = 2)
  var $section_pos    = array();       // Section rights Position in profile 
  var $mod_pos        = array();       // Module rights Position in profile 

  function OBM_Perm () {
    global $profiles;

    // Section position in profile ------------------------------------------//
    $this->section_pos["COM"] = 0;
    $this->section_pos["PROD"] = 1;
    $this->section_pos["COMPTA"] = 2;
    $this->section_pos["USER"] = 3;
    $this->section_pos["ADMIN"] = 4;
    $this->section_pos["ALIAMIN"] = 5;

    $mod_length = $this->mod_length;

    // Module position in profile -------------------------------------------//
    $this->mod_pos["COMPANY"] = 6;
    $this->mod_pos["CONTACT"] = $this->mod_pos["COMPANY"] + $mod_length;
    $this->mod_pos["DEAL"] = $this->mod_pos["CONTACT"] + $mod_length;
    $this->mod_pos["LIST"] = $this->mod_pos["DEAL"] + $mod_length;
    $this->mod_pos["AGENDA"] = $this->mod_pos["LIST"] + $mod_length;
    $this->mod_pos["TODO"] = $this->mod_pos["AGENDA"] + $mod_length;
    $this->mod_pos["PUBLICATION"] = $this->mod_pos["TODO"] + $mod_length;
    $this->mod_pos["STATISTIC"] = $this->mod_pos["PUBLICATION"] + $mod_length;
    $this->mod_pos["TIME"] = $this->mod_pos["STATISTIC"] + $mod_length;
    $this->mod_pos["PROJECT"] = $this->mod_pos["TIME"] + $mod_length;
    $this->mod_pos["CONTRACT"] = $this->mod_pos["PROJECT"] + $mod_length;
    $this->mod_pos["INCIDENT"] = $this->mod_pos["CONTRACT"] + $mod_length;
    $this->mod_pos["DOCUMENT"] = $this->mod_pos["INCIDENT"] + $mod_length;
    $this->mod_pos["ACCOUNT"] = $this->mod_pos["DOCUMENT"] + $mod_length;
    $this->mod_pos["INVOICE"] = $this->mod_pos["ACCOUNT"] + $mod_length;
    $this->mod_pos["PAYMENT"] = $this->mod_pos["INVOICE"] + $mod_length;
    $this->mod_pos["SETTINGS"] = $this->mod_pos["PAYMENT"] + $mod_length;
    $this->mod_pos["USER"] = $this->mod_pos["SETTINGS"] + $mod_length;
    $this->mod_pos["GROUP"] = $this->mod_pos["USER"] + $mod_length;
    $this->mod_pos["ADMIN"] = $this->mod_pos["GROUP"] + $mod_length;
    $this->mod_pos["ADMIN_CODE"] = $this->mod_pos["ADMIN"] + $mod_length;
    $this->mod_pos["ADMIN_PREF"] = $this->mod_pos["ADMIN_CODE"] + $mod_length;
    $this->mod_pos["ADMIN_DATA"] = $this->mod_pos["ADMIN_PREF"] + $mod_length;
    $this->mod_pos["ADMIN_LANG"] = $this->mod_pos["ADMIN_DATA"] + $mod_length;
    $this->mod_pos["ADMIN_REF"] = $this->mod_pos["ADMIN_LANG"] + $mod_length;
    $this->mod_pos["IMPORT"] = $this->mod_pos["ADMIN_REF"] + $mod_length;
    $profile_length = $this->mod_pos["IMPORT"] + $mod_length;

    // reader : 01 (bit 0) : can see data
    // user   : 03 (bits 0+1) : can see data + insert or update his own data
    // editor : 05 (bits 0+2) : can see data + write data
    // admin  : 1D (bits 0+2+3+4) : can do everything
    $perm_reader = "01";
    $perm_user = "03";
    $perm_editor = "05";
    $perm_admin = "1D";

    if (! isset($profiles["reader"])) {
      $profiles["reader"] = str_pad("110100", $profile_length, $perm_reader);
    }
    if (! isset($profiles["user"])) {
      $profiles["user"] = str_pad("110101", $profile_length, $perm_user);
    }
    if (! isset($profiles["editor"])) {
      $profiles["editor"] = str_pad("110101", $profile_length, $perm_editor);
    }
    if (! isset($profiles["admin"])) {
      $profiles["admin"] = str_pad("111111", $profile_length, $perm_admin);
    }
  }

  //-------------------------------------------------------------------------//
  // Return the profile access right of the section given as parameter       //
  // Parameters:
  //   - $section : section to return the right
  //-------------------------------------------------------------------------//
  function get_section_rights($section) {
    global $profiles, $auth;

    $profile = $auth->auth["perm"];
    $rights = $profiles["$profile"];
    $right_start = $this->section_pos["$section"];
    $section_right = hexdec(substr($rights, $right_start, 1));

    return $section_right;
  }

  //-------------------------------------------------------------------------//
  // Return the profile access right of the module given as parameter        //
  // Parameters:
  //   - $menu : module to return the right
  //-------------------------------------------------------------------------//
  function get_module_rights($menu) {
    global $profiles, $auth;

    $profile = $auth->auth["perm"];
    $rights = $profiles["$profile"];
    $right_start = $this->mod_pos["$menu"];
    $module_right = hexdec(substr($rights, $right_start, $this->mod_length));

    return $module_right;
  }

  //-------------------------------------------------------------------------//
  // Check profile permissions for the module and action given as parameters //
  // Parameters:
  //   - $menu   : module to check
  //   - $action : action right to check in the module
  //-------------------------------------------------------------------------//
  function check_permissions($menu, $action) {
    global $actions;

    $module_right = $this->get_module_rights($menu);
    $action_right = $actions[$menu][$action]["Right"];

    // If action right not defined or user has not access rights
    if ( ( ($module_right & $action_right) != $action_right)
         || ($action_right == "") ) {
      echo "<p>check_perm : ACCESS DENIED action=$action, mod_right=$module_right, ac=$action_right";
      exit();
    }
  }

  //-------------------------------------------------------------------------//
  // Check if the current profile has the module right given as parameter    //
  // Parameters:
  //   - $menu  : module to check
  //   - $right : right to check in the module
  //-------------------------------------------------------------------------//
  function check_right($menu, $right) {
    global $actions;

    $module_right = $this->get_module_rights($menu);

    // If action right not defined or user has not access rights
    if ( ( ($module_right & $right) != $right)
         || ($right == "") ) {
      return false;
    } else {
      return true;
    }
  }

}


//---------------------------------------------------------------------------//
// Database sessions
//---------------------------------------------------------------------------//
if ($cgp_sess_db) {

  function sess_db_open($save_path, $session_name) {
    global $sess_save_path, $sess_session_name;
    
    $sess_save_path = $save_path;
    $sess_session_name = $session_name;
    return true;
  }

  function sess_db_close() {
    return(true);
  }

  function sess_db_read ($id) {
    global $sess_save_path, $sess_session_name;
    
    $s_q = new DB_OBM;
    $query = "select obmsession_data
    from ObmSession
    where obmsession_sid = '$id'";
    
    if ($s_q->query($query)) {
      $s_q->next_record();
      $ret = $s_q->f("obmsession_data");
    } else {
      $ret = "";
    }
    
    return $ret;
  }

  function sess_db_destroy ($id) {
    global $sess_save_path, $sess_session_name;
    
    $s_q = new DB_OBM;
    $query = "DELETE from ObmSession
    where
      obmsession_sid = '$id'
      and obmsession_name = '$sess_session_name'";
    
    $ret = $s_q->query($query);
    return $ret;
  }
  
  /*********************************************
   * WARNING - You will need to implement some *
   * sort of garbage collection routine here.  *
   *********************************************/
  function sess_db_gc ($maxlifetime) {
    return true;
  }

  //-------------------------------------------------------------------------//
  // Mysql specific Session function Handler
  //-------------------------------------------------------------------------//
  if ($obmdb_dbtype == $db_type_mysql) {

    function sess_db_write ($id, $sess_data) {
      global $sess_save_path, $sess_session_name;
    
      $now = date("Y-m-d H:i:s");
      $s_q = new DB_OBM;
      $query = "REPLACE ObmSession ( 
      obmsession_sid,
      obmsession_timeupdate,
      obmsession_name,
      obmsession_data)
    VALUES (
      '$id',
      '$now',
      '$sess_session_name',
      '".addslashes($sess_data)."')";
      
      $ret = $s_q->query($query);

      return $ret;
    }
  }

  //-------------------------------------------------------------------------//
  // Postgres specific Session function Handler
  //-------------------------------------------------------------------------//
  if ($obmdb_dbtype == $db_type_pgsql) {

    function sess_db_write ($id, $sess_data) {
      global $sess_save_path, $sess_session_name;
    
      $now = date("Y-m-d H:i");
      $s_q = new DB_OBM;
      $query = "UPDATE ObmSession set
        obmsession_timeupdate='$now',
        obmsession_name='$sess_session_name',
        obmsession_data='".addslashes($sess_data)."'
      where
        obmsession_sid='$id'";
      
      $ret = $s_q->query($query);

      if ($s_q->affected_rows() == 0) {
	$query = "INSERT INTO ObmSession ( 
          obmsession_sid,
          obmsession_timeupdate,
          obmsession_name,
          obmsession_data)
        VALUES (
          '$id',
          '$now',
          '$sess_session_name',
          '".addslashes($sess_data)."')";
      $ret = $s_q->query($query);
      }
    
      return $ret;
    }
  }
  

  session_set_save_handler ("sess_db_open", "sess_db_close", "sess_db_read", "sess_db_write", "sess_db_destroy", "sess_db_gc");

}

</script>
