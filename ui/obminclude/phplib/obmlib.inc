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

  function ac_store($id, $name, $u_id) {
    global $module, $REMOTE_ADDR, $action;

    $ret = true;
    $name = addslashes($name);
    $now = date("Y-m-d H:i:s", time());
    $table = $this->database_table;
    $lastpage = "$module:$action";
    // update duration of visit
    $uquery = "UPDATE $table SET
        activeuserobm_userobm_id = '$u_id',
        activeuserobm_timeupdate = '$now',
        activeuserobm_nb_connexions = activeuserobm_nb_connexions + 1,
        activeuserobm_lastpage = '$lastpage'
      WHERE activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $squery = "SELECT count(*)
      FROM $table
      WHERE activeuserobm_userobm_id = '$u_id'
        and activeuserobm_timeupdate = '$now'
        and activeuserobm_sid = '$id'
        and activeuserobm_session_name = '$name'";

    $iquery = "INSERT INTO $table (
        activeuserobm_sid,
        activeuserobm_session_name,
        activeuserobm_userobm_id,
        activeuserobm_timeupdate,
        activeuserobm_timecreate,
        activeuserobm_nb_connexions,
        activeuserobm_lastpage,
        activeuserobm_ip
      ) VALUES ('$id', '$name', '$u_id', '$now', '$now', 1, '$lastpage', '$REMOTE_ADDR')";

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
    global $sess, $obminclude, $obm_version, $module, $path, $login_action;
    global $challenge, $auth_encryption, $password_encryption;

    $login_form = "$obminclude/auth/crloginform.ihtml";
    $challenge = md5(uniqid($this->magic));
    $sess->register("challenge");
    
    include($login_form);
  }
  
  function auth_validatelogin() {
    global $login, $password, $challenge, $response;
    global $auth_encryption, $password_encryption;

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

    if ($auth_encryption) {
      if ($password_encryption == "md5") {

        // Cas md5
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

      // No auth_encryption, password is given as plain
    } else {
      if ($password_encryption == "crypt") {
        // cas crypt
        $encrypted = crypt($password, $pass);
        if ($encrypted == $pass) {
          $this->auth["perm"] = $perm;
          return $uid;
        } else {
          return false;
        }      
      } else if ($password_encryption == "md5") {
        $encrypted = md5($password);
        if ($encrypted == $pass) {
          $this->auth["perm"] = $perm;
          return $uid;
        } else {
          return false;
        }      
      } else {
        $encrypted = $password;
        if ($encrypted == $pass) {
          $this->auth["perm"] = $perm;
          return $uid;
        } else {
          return false;
        }      
      }
    }

  }
}

class CAS_Auth extends Auth {

  var $classname      = "CAS_Auth";
  var $lifetime       =  0;
  var $magic          = "CASSimsalabim";  ## Challenge seed
  var $database_class = "DB_OBM";
  var $database_table = "UserObm";

  function auth_loginform() {
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri,$lock;

    include_once("obmCAS.inc");
    if (!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri);
      $lock = true;
    }
    obmCAS::authenticateIfNeeded();
  }

  function is_authenticated() {
    global $ticket;
    if (
      $this->auth["uid"] 
        && 
      (($this->lifetime <= 0) || (time() < $this->auth["exp"]))
    ) {
      # If more than $this->refresh minutes are passed since last check,
      # perform auth data refreshing. Refresh is only done when current
      # session is valid (registered, not expired).
      if (
        ($this->refresh > 0) 
         && 
        ($this->auth["refresh"])
         && 
        ($this->auth["refresh"] < time())
      ) {
        if ( $this->auth_refreshlogin() ) {
          $this->auth["refresh"] = time() + (60 * $this->refresh);
        } else {
          return false;
        }
      }
      return $this->auth["uid"] ;
    } elseif (isset($ticket)) {
      return "form" ;
    } else {
      return false;
    }
  }

  function auth_validatelogin () {
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri,$lock;

    include_once("obmCAS.inc");
    if(!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri);
      $lock = "true";
    }
    //valider le login CAS
    if (!obmCAS::isAuthenticated()) {
      return false;
    }
    $login = obmCAS::getUser();
    $this->db->query(sprintf("select userobm_id,
        userobm_perms
      from %s
      where userobm_login = '%s'",
			     $this->database_table,
			     addslashes($login)));

    while ($this->db->next_record()) {
      $uid  = $this->db->f("userobm_id");
      $perm = $this->db->f("userobm_perms");
    }

    $this->auth["perm"] = $perm;
    $this->auth["uname"] = $login;
    return $uid;
  }
   
   function logout($nobody = "") {
    global $sess,$lock;
    global $cas_version, $cas_server, $cas_server_port, $cas_server_uri;
    
    include_once("obmCAS.inc");
    $sess->unregister("auth");
    unset($this->auth["uname"]);
    $this->unauth($nobody == "" ? $this->nobody : $nobody);
    
    if(!$lock) {
      obmCAS::client($cas_version, $cas_server, $cas_server_port, $cas_server_uri);
      $lock = "true";
    }

    obmCAS::logout();
  }
  
##
## Initialization
##
  function start() {
    $cl = $this->cancel_login;
    global $sess, $$cl;

    ## This is for performance, I guess but I'm not sure if it could
    ## be safely removed -- negro
    if (! $this->in) {
      $sess->register("auth");
      $this->in = true;
    }
    
    ## back compatibility: if d_c is set, create db object
    if(isset($this->database_class)) {
      $class = $this->database_class;
      $this->db = new $class;
    }

    # Check current auth state. Should be one of
    #  1) Not logged in (no valid auth info or auth expired)
    #  2) Logged in (valid auth info)
    #  3) Login in progress (if $$cl, revert to state 1)
    // seule modif par rapport à Auth::start()
    if ($uid = $this->is_authenticated()) {
      
      switch ($uid) {
        case "form":
          # Login in progress
          if ($$cl) {
            # If $$cl is set, delete all auth info 
            # and set state to "Not logged in", so eventually
            # default or automatic authentication may take place
            $this->unauth();
            $state = 1;
          } else {
            # Set state to "Login in progress"
            $state = 3;
          }
          break;
        default:
          # User is authenticated and auth not expired
          $state = 2;
          break;
      }
    } else {
      # User is not (yet) authenticated
      $this->unauth();
      $state = 1;
    }

    switch ($state) {
      case 1:
        # No valid auth info or auth is expired
        
        # Check for user supplied automatic login procedure 
        if ( $uid = $this->auth_preauth() ) {
          $this->auth["uid"] = $uid;
          $this->auth["exp"] = time() + (60 * $this->lifetime);
          $this->auth["refresh"] = time() + (60 * $this->refresh);
          return true;
        }
        
        # Check for "log" vs. "reg" mode
        switch ($this->mode) {
          case "yes":
          case "log":
            if ($this->nobody) {
              # Authenticate as nobody
              $this->auth["uid"] = "nobody";
              # $this->auth["uname"] = "nobody";
              $this->auth["exp"] = 0x7fffffff;
              $this->auth["refresh"] = 0x7fffffff;
              return true;
            } else {
              # Show the login form
              $this->auth_loginform();
              $this->auth["uid"] = "form";
              $this->auth["exp"] = 0x7fffffff;
              $this->auth["refresh"] = 0x7fffffff;
              exit;
            }
            break;
          case "reg":
            # Show the registration form
            $this->auth_registerform();
            $this->auth["uid"] = "form";
            $this->auth["exp"] = 0x7fffffff;
            $this->auth["refresh"] = 0x7fffffff;
            exit;
            break;
          default:
            # This should never happen. Complain.
            echo "Error in auth handling: no valid mode specified.\n";
            exit;
        }
        break;
      case 2:
        # Valid auth info
        # Refresh expire info
        ## DEFAUTH handling: do not update exp for nobody.
        if ($uid != "nobody")
          $this->auth["exp"] = time() + (60 * $this->lifetime);
        break;
      case 3:
        # Login in progress, check results and act accordingly
        switch ($this->mode) {
          case "yes":
          case "log":
            if ( $uid = $this->auth_validatelogin() ) {
              $this->auth["uid"] = $uid;
              $this->auth["exp"] = time() + (60 * $this->lifetime);
              $this->auth["refresh"] = time() + (60 * $this->refresh);
              return true;
            } else {
              $this->auth_loginform();
              $this->auth["uid"] = "form";
              $this->auth["exp"] = 0x7fffffff;
              $this->auth["refresh"] = 0x7fffffff;
              exit;
            }
            break;
          case "reg":
            if ($uid = $this->auth_doregister()) {
              $this->auth["uid"] = $uid;
              $this->auth["exp"] = time() + (60 * $this->lifetime);
              $this->auth["refresh"] = time() + (60 * $this->refresh);
              return true;
            } else {
              $this->auth_registerform();
              $this->auth["uid"] = "form";
              $this->auth["exp"] = 0x7fffffff;
              $this->auth["refresh"] = 0x7fffffff;
              exit;
            }
            break;
          default:
            # This should never happen. Complain.
            echo "Error in auth handling: no valid mode specified.\n";
            exit;
            break;
        }
        break;
      default:
        # This should never happen. Complain.
        echo "Error in auth handling: invalid state reached.\n";
        exit;
        break;
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
    $this->section_pos["com"] = 0;
    $this->section_pos["prod"] = 1;
    $this->section_pos["compta"] = 2;
    $this->section_pos["user"] = 3;
    $this->section_pos["admin"] = 4;
    $this->section_pos["ALIAMIN"] = 5;

    $mod_length = $this->mod_length;

    // Module position in profile -------------------------------------------//
    $this->mod_pos["company"] = 6;
    $this->mod_pos["contact"] = $this->mod_pos["company"] + $mod_length;
    $this->mod_pos["deal"] = $this->mod_pos["contact"] + $mod_length;
    $this->mod_pos["list"] = $this->mod_pos["deal"] + $mod_length;
    $this->mod_pos["agenda"] = $this->mod_pos["list"] + $mod_length;
    $this->mod_pos["todo"] = $this->mod_pos["agenda"] + $mod_length;
    $this->mod_pos["publication"] = $this->mod_pos["todo"] + $mod_length;
    $this->mod_pos["statistic"] = $this->mod_pos["publication"] + $mod_length;
    $this->mod_pos["time"] = $this->mod_pos["statistic"] + $mod_length;
    $this->mod_pos["project"] = $this->mod_pos["time"] + $mod_length;
    $this->mod_pos["contract"] = $this->mod_pos["project"] + $mod_length;
    $this->mod_pos["incident"] = $this->mod_pos["contract"] + $mod_length;
    $this->mod_pos["document"] = $this->mod_pos["incident"] + $mod_length;
    $this->mod_pos["account"] = $this->mod_pos["document"] + $mod_length;
    $this->mod_pos["invoice"] = $this->mod_pos["account"] + $mod_length;
    $this->mod_pos["payment"] = $this->mod_pos["invoice"] + $mod_length;
    $this->mod_pos["settings"] = $this->mod_pos["payment"] + $mod_length;
    $this->mod_pos["user"] = $this->mod_pos["settings"] + $mod_length;
    $this->mod_pos["group"] = $this->mod_pos["user"] + $mod_length;
    $this->mod_pos["admin"] = $this->mod_pos["group"] + $mod_length;
    $this->mod_pos["admin_code"] = $this->mod_pos["admin"] + $mod_length;
    $this->mod_pos["admin_pref"] = $this->mod_pos["admin_code"] + $mod_length;
    $this->mod_pos["admin_data"] = $this->mod_pos["admin_pref"] + $mod_length;
    $this->mod_pos["admin_lang"] = $this->mod_pos["admin_data"] + $mod_length;
    $this->mod_pos["admin_ref"] = $this->mod_pos["admin_lang"] + $mod_length;
    $this->mod_pos["import"] = $this->mod_pos["admin_ref"] + $mod_length;
    $profile_length = $this->mod_pos["import"] + $mod_length;

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
  //   - $module : module to return the right
  //-------------------------------------------------------------------------//
  function get_module_rights($module) {
    global $profiles, $auth;

    $profile = $auth->auth["perm"];
    $rights = $profiles["$profile"];
    $right_start = $this->mod_pos["$module"];
    $module_right = hexdec(substr($rights, $right_start, $this->mod_length));

    return $module_right;
  }

  //-------------------------------------------------------------------------//
  // Check profile permissions for the module and action given as parameters //
  // Parameters:
  //   - $module : module to check
  //   - $action : action right to check in the module
  //-------------------------------------------------------------------------//
  function check_permissions($module, $action) {
    global $actions;

    $module_right = $this->get_module_rights($module);
    $action_right = $actions[$module][$action]["Right"];

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
  //   - $module : module to check
  //   - $right  : right to check in the module
  //-------------------------------------------------------------------------//
  function check_right($module, $right) {
    global $actions;

    $module_right = $this->get_module_rights($module);

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
