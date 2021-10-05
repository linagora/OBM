<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : of_session.inc                                               //
//     - Desc : OBM Framework Session Objects                                //
// 2005-02-03 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//-----------------------------------------------------------------------------
// Open the session system
//-----------------------------------------------------------------------------
function page_open($feature) {

  # enable sess and all dependent features.
  if (isset($feature["sess"])) {
    global $sess, $auth, $obm;

    $sess = new $feature["sess"];
    $sess->start();

    # the auth feature depends on sess
    if (isset($feature["auth"])) {
      global $auth;

      if (!isset($auth)) {
        $auth = new $feature["auth"];
      }
      $auth->start();

      # the perm feature depends on auth and sess
      if (isset($feature["perm"])) {
        global $perm;
        
        if (!isset($perm)) {
          $perm = new $feature["perm"];
        }
      }
    }
    $_SESSION["auth"] = $auth;
    $_SESSION["obm"] = $obm;
    //    echo "<p>*** SAVE Session: obm=";
    //    print_r($obm);

  }
}


//-----------------------------------------------------------------------------
// Store and close the current session
//-----------------------------------------------------------------------------
function page_close() {
  global $sess, $obm;

  $base_session = NEW OBM_User_Sql;
  $base_session->ac_start();
  $base_session->ac_store($sess->id,$sess->cookie_name,$obm["uid"]);
}


///////////////////////////////////////////////////////////////////////////////
// Session Class
///////////////////////////////////////////////////////////////////////////////
class Session {
  var $cookie_name = "";     // Cookie name
  var $cookie_domain = "";   // Cookie domain, if domain cookie used
  var $lifetime = 0;         // Cookie lifetime (seconds, 0=session cookie)
  var $id;                   // Unique Session ID


  function Session () {
    global $cs_lifetime, $cgp_cookie_name, $cgp_cookie_domain;

    $this->cookie_name   = "$cgp_cookie_name";
    $this->cookie_domain = "$cgp_cookie_domain";

    if ($cs_lifetime > 0) {
      $this->lifetime = $cs_lifetime;
    }
  }


  //---------------------------------------------------------------------------
  // Register some variables in session
  // Parameters:
  //   - $things : string of variables names (coma separated) to register
  //---------------------------------------------------------------------------
  function register($things) {

    $things = explode(",",$things);
    reset($things);
    while ( list(,$thing) = each($things) ) {
      $thing = trim($thing);
      if ( $thing ) {
        session_register($thing);
      }
    }
  }


  //---------------------------------------------------------------------------
  // Tell if a variable given is registered in the session
  // Parameters:
  //   - $name : variable name
  // Returns: true if the var is registered, else false
  //---------------------------------------------------------------------------
  function is_registered($name) {

    if (session_is_registered($name)) {
      return true;
    }
    return false;
  }


  //---------------------------------------------------------------------------
  // Unregister some variables from the session
  // Parameters:
  //   - $things : string of variables names (coma separated) to unregister
  //---------------------------------------------------------------------------
  function unregister($things) {

    $things = explode(",", $things);
    reset($things);
    while (list(,$thing) = each($things)) {
      $thing = trim($thing);
      if ($thing) {
        session_unregister($thing);
      }
    }
  }


  //---------------------------------------------------------------------------
  // Set the OBM cookie
  //---------------------------------------------------------------------------
  function setcookie() {

    if ($this->lifetime <= 0) {
      setcookie($this->cookie_name, session_id(), 0, "/", $this->cookie_domain);
    } else {
      setcookie($this->cookie_name, session_id(), time()+$this->lifetime, "/", $this->cookie_domain);
    }
  }


  //---------------------------------------------------------------------------
  // Start session
  //---------------------------------------------------------------------------
  function start() {
    // register_global : $action : ?
    global $obm, $action, $login_action;

    if ($action == "logout") {
      $login_action = "obm.php";
    }

    // Set headers
    header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
    header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
    header("Cache-Control: no-cache");
    header("Cache-Control: post-check=0, pre-check=0");
    header("Pragma: no-cache");

    session_name($this->cookie_name);
    session_start();
    $this->id = session_id();
    $obm = $_SESSION['obm'];
    //      echo "<p>*** LOAD Session: obm=";
    //      print_r($obm);

    global $auth;
    $auth = $_SESSION['auth'];
    //echo "<br>END Session_start : obm[uid]=$obm[uid] -";
  }


  //---------------------------------------------------------------------------
  // Delete the curent session
  //---------------------------------------------------------------------------
  function delete() {
    global $HTTP_COOKIE_VARS;

    session_start();
    session_destroy();
    unset($HTTP_COOKIE_VARS[session_name()]);
    //    session_start();
  }
}


///////////////////////////////////////////////////////////////////////////////
// Auth Class
///////////////////////////////////////////////////////////////////////////////
class Auth {
  var $lifetime = 0;         // Auth lifetime (seconds, 0=never expires)
  var $refresh = 0;               ## Refresh interval in minutes. 
                                  ## When expires auth data is refreshed
                                  ## from db using auth_refreshlogin()
                                  ## method. Set to 0 to disable refresh

  var $magic = "";                ## Used in uniqid() generation

  var $nobody = false;            ## If true, a default auth is created...

  var $cancel_login = "cancel_login"; ## The name of a button that can be 
                                      ## used to cancel a login form

  ## End of user qualifiable settings.

  var $auth = array();            ## Data array
  var $in;
  var $db;

  ##
  ## Initialization
  ##
  function start() {
    $cl = $this->cancel_login;
    global $obm, $$cl, $lifetime;

    //echo "<br>***AUTH-start - obm_uid=$obm[uid]";

    // OBM : We set the auth lifetime with the global session lifetime (if set)
    // It is not set at the login (cause page_open before session_load_global)
    if ($lifetime) $this->lifetime = $lifetime;

    if (! $this->in) {
      $_SESSION['auth'] = $auth;
      $_SESSION['obm'] = $obm;
      $this->in = true;
    }

    // Check current login state
    $uid = $this->is_authenticated();

    // User not logged => display login page ----------------------------------
    if ($uid == false) {

      $this->unauth();
      $this->of_session_login_init();    

      // User come from the login page : is trying to login -------------------
    } else if ($uid == "form") {

      if ($$cl) {
	// If $$cl is set, delete all auth info
	// and set state to "Not logged in", so eventually
	// default or automatic authentication may take place
	$this->unauth();
	$this->of_session_login_init();    

      } else {

	// Login OK -> initialize the $obm datas
	if ( $uid = $this->auth_validatelogin() ) {

	  $obm['uid'] = $uid;
	  // Domain in session
	  // XXXX optimisation, recup domaine_label in auth_vali
	  $d = of_domain_get_domain_infos($obm['domain_id'], true);
	  $obm['domain_label'] = $d['label'];
          $obm['domain_name'] = $d['name'];
          $obm['domain_global'] = $d['global'];
          $obm['mailserver'] = $d['mailserver'];
          $obm['group_admin'] = $d['group_admin'];
          $obm['group_com'] = $d['group_com'];
          $obm['group_prod'] = $d['group_prod'];
	  // OBM : in OBM each time is in seconds. this->lifetime too.
	  $this->auth["exp"] = time() + $this->lifetime;
	  $this->auth["refresh"] = time() + (60 * $this->refresh);
	  return true;

	  // Login failed -> login page
	} else {
	  $this->of_session_login_init();    
	}
      }

      // User is logged, just update session expiration -----------------------
    } else {
      
      // DEFAULT handling: do not update exp for nobody.
      if ($uid != "nobody") {
	// OBM : we get the global lifetime which is in seconds
	$this->auth["exp"] = time() + $this->lifetime;
      }
    }

    return true;
  }


  function of_session_login_init() {
    global $obm;

    // Check for user supplied automatic login procedure 
    if ( $uid = $this->auth_preauth() ) {
      $obm["uid"] = "$uid";
      $this->auth["exp"] = time() + (60 * $this->lifetime);
      $this->auth["refresh"] = time() + (60 * $this->refresh);
      return true;
    }

    // Show the login form
    $this->of_session_dis_login_page();
    $obm["uid"] = "form";
    $this->auth["exp"] = 0x7fffffff;
    $this->auth["refresh"] = 0x7fffffff;
    $_SESSION['obm'] = $obm;
    exit;
  }

  function unauth($nobody = false) {

    $this->auth["exp"]   = 0;
  }
  

  function is_authenticated() {
    global $obm, $c_singleNameSpace;

    if (
      $obm["uid"] && 
      (($this->lifetime <= 0) || (time() < $this->auth["exp"]))) {
      
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
      
      return $obm["uid"];
    } else {
      return "form";
    }
  }
    
  ########################################################################
  ##
  ## Helper functions
  ##

  ## This method can authenticate a user before the loginform
  ## is being displayed. If it does, it must set a valid uid 
  ## (i.e. nobody IS NOT a valid uid) just like auth_validatelogin,
  ## else it shall return false.

  function auth_preauth() { return false; }
  
  ##
  ## Authentication dummies. Must be overridden by user.
  ##
  
  function auth_validatelogin() { ; }
  
  function auth_refreshlogin() { ; }

}


///////////////////////////////////////////////////////////////////////////////
// OBM_Perm Class : Profile and right permissions                            //
///////////////////////////////////////////////////////////////////////////////
class Perm {

  function Perm () {
    global $profiles, $obm, $perm_admin, $perm_user;

	$profiles = get_all_profiles();

    if (! isset($profiles["admin"])) {
      $profiles["admin"] = array (
        'section' => array (
          'default' => 1),
        'module' => array (
          'default' => $perm_admin)
      );
    }
    if ($obm["domain_global"]) {
      $profiles[$obm["profile"]] = 
        array (
          'section' => array (
            'default' => 0,
            'user' => 1,
            'admin' => 1,
            'my' => 1 
          ),
          'module' => array (
            'default' => 0,
            'user' => $perm_admin,
            'host' => $perm_admin,
            'domain' => $perm_admin,
            'password' => $perm_admin,
            'settings' => $perm_admin,
            'tools' => $perm_admin,
            'group' => $perm_user,
            'profile' => $perm_admin
          ),
          'properties' => array (
            'admin_realm' => array ('domain')
          ),
          'level' => 0,
          'level_managepeers' => true
        );
    }
  }


  //-------------------------------------------------------------------------//
  // Return the profile access right of the section given as parameter       //
  // Parameters:
  //   - $section : section to return the right
  //-------------------------------------------------------------------------//
  function get_section_rights($section, $profile = false) {
    global $profiles, $obm;
    if(!$profile) $profile = $obm["profile"];
    if (isset($profiles[$profile]['section'][$section])) {
      return $profiles[$profile]['section'][$section];
    } else {
      return $profiles[$profile]['section']['default'];
    }
 
    return $section_right;
  }

  //-------------------------------------------------------------------------//
  // Return the profile access right of the module given as parameter        //
  // Parameters:
  //   - $module : module to return the right
  //-------------------------------------------------------------------------//
  function get_module_rights($module, $profile = false) {
    global $profiles, $obm;
	
    if(!$profile) $profile = $obm["profile"];

    if (isset($profiles[$profile]['module'][$module])) {
      return hexdec($profiles[$profile]['module'][$module]);
		
    } else {
      return hexdec($profiles[$profile]['module']['default']);
		
    }
 
    return $module_right;
  }

    //-------------------------------------------------------------------------//
  // Return true if the profile can access to module given as parameter        //
  // false else
  // Parameters:
  //   - $module : module to return the right
  //-------------------------------------------------------------------------//
  function check_module_rights($module, $profile = false) {
    global $profiles, $obm, $modules, $perm;
	
    if(!$profile) $profile = $obm["profile"];
    return ($GLOBALS['cgp_show']['module'][$module] && (($perm->get_module_rights($module) & $modules[$mod]['Right']) == $modules[$mod]['Right'])); 
  }

  //-------------------------------------------------------------------------//
  // Check profile permissions for the module and action given as parameters //
  // Parameters:
  //   - $module : module to check
  //   - $action : action right to check in the module
  //-------------------------------------------------------------------------//
  function check_permissions($module, $action) {
    global $actions,$path;

    $module_right = $this->get_module_rights($module);
    $action_right = $actions[$module][$action]["Right"];

    // If action right not defined or user has not access rights
    if ( ( ($module_right & $action_right) != $action_right)
      || ($action_right == "" && $action_right !== 0) ) {
        if ($_SESSION['set_debug'] == 0) {
          header("location: $path/obm.php?error=perms");
        } else {
          echo "<p>check_perm : ACCESS DENIED action=$action, mod_right=$module_right, ac=$action_right";
        }
      exit();
    }
  }

  //-------------------------------------------------------------------------//
  // Check if the current profile has the module right given as parameter    //
  // Parameters:
  //   - $module : module to check
  //   - $right  : right to check in the module
  //-------------------------------------------------------------------------//
  function check_right($module, $right, $profile = false) {
    return self::check_module_right($module, $right, $profile);
  }

  function check_module_right($module, $right, $profile = false) {
    $module_right = self::get_module_rights($module, $profile);
    // If action right not defined or user has not access rights
    if ( ( ($module_right & $right) != $right)
         || ($right == "") ) {
      return false;
    } else {
      return true;
    }
  }

  function check_section_right($section, $profile = false) {
    $section_right = self::get_section_rights($section, $profile);

    if ($section_right) {
      return true;
    } else {
      return false;
    }
  }  
  //-------------------------------------------------------------------------//
  // Check if the current profile is superman                                //
  // Parameters:
  //   - $module : module to check
  //-------------------------------------------------------------------------//
  function is_superman($module) {
    global $cright_write_admin;
    
    return (($this->get_module_rights($module) & $cright_write_admin) == $cright_write_admin);
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
    $query = "DELETE FROM ObmSession
    WHERE
      obmsession_sid = '$id'
      AND obmsession_name = '$sess_session_name'";
    
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
    
      $s_q = new DB_OBM;
      $query = "REPLACE ObmSession ( 
      obmsession_sid,
      obmsession_timeupdate,
      obmsession_name,
      obmsession_data)
    VALUES (
      '$id',
      NOW(),
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
    
      $s_q = new DB_OBM;
      $query = "UPDATE ObmSession set
        obmsession_timeupdate=NOW(),
        obmsession_name='$sess_session_name',
        obmsession_data='".addslashes($sess_data)."'
      WHERE
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
          NOW(),
          '$sess_session_name',
          '".addslashes($sess_data)."')";
      $ret = $s_q->query($query);
      }
    
      return $ret;
    }
  }
  

  session_set_save_handler ("sess_db_open", "sess_db_close", "sess_db_read", "sess_db_write", "sess_db_destroy", "sess_db_gc");

}


?>
