<script language="php">
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
    global $sess, $auth;
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
  }
}


//-----------------------------------------------------------------------------
// Store and close the current session
//-----------------------------------------------------------------------------
function page_close() {
  global $sess, $auth;
  $base_session = NEW OBM_User_Sql;
  $base_session->ac_start();
  $base_session->ac_store($sess->id,$sess->cookie_name,$auth->auth["uid"]);
}


///////////////////////////////////////////////////////////////////////////////
// Session Class
///////////////////////////////////////////////////////////////////////////////
class Session {
  var $cookie_name = "";     // Cookie name
  var $cookie_domain = "aliacom.local";   // Cookie domain, if domain cookie used
  var $lifetime = 0;         // Cookie lifetime (seconds, 0=session cookie)
  var $id;                   // Unique Session ID


  function Session () {
    global $cs_lifetime;

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
    global $action, $login_action;

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

</script>
