<?php
/*
 * Session Management for PHP3
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp
 * Copyright (c) 1999-2000 Internet Images srl
 *                    Massimiliano Masserelli
 *
 * $Id$
 *
 */ 

class Auth {
  var $classname = "Auth";
  var $persistent_slots = array("auth");
  
  var $lifetime = 15;             ## Max allowed idle time before
                                  ## reauthentication is necessary.
                                  ## If set to 0, auth never expires.
  
  var $refresh = 0;               ## Refresh interval in minutes. 
                                  ## When expires auth data is refreshed
                                  ## from db using auth_refreshlogin()
                                  ## method. Set to 0 to disable refresh

  var $mode = "log";              ## "log" for login only systems,
                                  ## "reg" for user self registration

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
    global $sess, $$cl, $lifetime;

    // OBM : We set the auth lifetime with the global session lifetime (if set)
    // It is not set at the login (cause page_open before session_load_global)
    if ($lifetime) $this->lifetime = $lifetime;

    ## This is for performance, I guess but I'm not sure if it could
    ## be safely removed -- negro

    $this->auth = array("uid" => 23, "perm" => "admin","uname"=> "mehdi", "exp" => time()+12222, "refresh" => time());
    $this->classname = "OBM_Challenge_Auth";
    $this->lifetime = 14400;
    $this->persistent_slots = array("auth");
    $this->classename = "OBM_Challenge_Auth";
    $this->lifetime = 14400;
    $this->magic = "Simsalabim";
    $this->database_class = "DB_OBM";
    $this->database_table = "UserObm";
    $this->refresh = 0;
    $this->mode = "log";
    $this->nobody = 0;
    $this->cancel_login = "cancel_login";
    $this->in = 1;
    $this->db_obm->Host = "localhost";   
    $this->db_obm->Database = "obm";
    $this->db_obm->type = "MYSQL";
    $this->db_obm->User =  "root";
    $this->db_obm->Password = "";
    $this->db_obm->Auto_Free = 0;
    $this->db_obm->Debug = 0;
    $this->db_obm->Halt_On_Error = "yes";

    if (! $this->in) {
      $sess->register("auth");
      $this->in = true;
    }
  //	A VIRER!!!!!!! Pour tester les pages dans le validateur W3C Offline.
 

/*
auth|O:18:"obm_challenge_auth":13:{s:9:"classname";s:18:"OBM_Challenge_Auth";s:8:"lifetime";s:5:"14400";s:5:"magic";s:10:"Simsalabim";s:14:"database_class";s:6:"DB_OBM";s:14:"database_table";s:7:"UserObm";s:16:"persistent_slots";a:1:{i:0;s:4:"auth";}s:7:"refresh";i:0;s:4:"mode";s:3:"log";s:6:"nobody";b:0;s:12:"cancel_login";s:12:"cancel_login";s:4:"auth";a:5:{s:3:"uid";s:2:"23";s:4:"perm";s:5:"admin";s:3:"exp";i:1034269893;s:7:"refresh";i:1034255491;s:5:"uname";s:5:"mehdi";}s:2:"in";b:1;s:2:"db";O:6:"db_obm":16:{s:4:"Host";s:9:"localhost";s:8:"Database";s:3:"obm";s:4:"type";s:5:"MYSQL";s:4:"User";s:4:"root";s:8:"Password";s:0:"";s:9:"Auto_Free";i:0;s:5:"Debug";i:0;s:13:"Halt_On_Error";s:3:"yes";s:9:"Seq_Table";s:11:"db_sequence";s:6:"Record";a:0:{}s:3:"Row";N;s:5:"Errno";i:0;s:5:"Error";s:0:"";s:8:"revision";s:3:"1.2";s:7:"Link_ID";i:0;s:8:"Query_ID";i:0;}}challenge|s:32:"5d60372111c4ecb7caf55dea4bec762d";lifetime|s:5:"14400";session_cookie|s:1:"1";set_lang|s:2:"fr";set_display|s:2:"no";set_rows|s:2:"12";set_day_weekstart|s:6:"monday";last_payment|s:1:"0";last_invoice|s:1:"0";last_account|s:1:"0";last_incident|s:2:"14";last_contract|s:1:"2";last_user|s:1:"0";last_list|s:1:"0";last_deal|s:1:"0";set_theme|s:8:"standard";last_contact|s:4:"1132";last_company|s:3:"620";set_debug|s:1:"0";last_company_name|s:15:"01 INFORMATIQUE";last_contact_name|s:5:"ADELL";last_deal_name|N;last_list_name|N;last_account_name|N;last_invoice_name|N;last_payment_name|N;last_contract_name|s:33:"Support IO Box pour leurs clients";last_incident_name|s:34:"prb io : aucune connexion possible";*/
 //	A VIRER!!!!!!! Pour tester les pages dans le validateur W3C Offline.

    ## back compatibility: if d_c is set, create db object
    if(isset($this->database_class)) {
      $class = $this->database_class;
      $this->db = new $class;
    }

    # Check current auth state. Should be one of
    #  1) Not logged in (no valid auth info or auth expired)
    #  2) Logged in (valid auth info)
    #  3) Login in progress (if $$cl, revert to state 1)
    if ($this->is_authenticated()) {
      $uid = $this->auth["uid"];
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
              $sess->freeze();
              exit;
            }
            break;
          case "reg":
            # Show the registration form
            $this->auth_registerform();
            $this->auth["uid"] = "form";
            $this->auth["exp"] = 0x7fffffff;
            $this->auth["refresh"] = 0x7fffffff;
            $sess->freeze();
            exit;
            break;
          default:
            # This should never happen. Complain.
            echo "Error in auth handling: no valid mode specified.\n";
            $sess->freeze();
            exit;
        }
        break;
      case 2:
        # Valid auth info
        # Refresh expire info
        ## DEFAUTH handling: do not update exp for nobody.
        if ($uid != "nobody")
//          $this->auth["exp"] = time() + (60 * $this->lifetime);
          // OBM : we get the global lifetime which is in seconds
          $this->auth["exp"] = time() + $this->lifetime;
        break;
      case 3:
        # Login in progress, check results and act accordingly
        // OBM : login paramaters has been submitted
        switch ($this->mode) {
          case "yes":
          case "log":
            // OBM : if login successfull (valid auth)
            if ( $uid = $this->auth_validatelogin() ) {
              $this->auth["uid"] = $uid;
//              $this->auth["exp"] = time() + (60 * $this->lifetime);
              // OBM : in OBM each time is in seconds. this->lifetime too.
              $this->auth["exp"] = time() + $this->lifetime;
              $this->auth["refresh"] = time() + (60 * $this->refresh);
              return true;
            } else {
              $this->auth_loginform();
              $this->auth["uid"] = "form";
              $this->auth["exp"] = 0x7fffffff;
              $this->auth["refresh"] = 0x7fffffff;
              $sess->freeze();
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
              $sess->freeze();
              exit;
            }
            break;
          default:
            # This should never happen. Complain.
            echo "Error in auth handling: no valid mode specified.\n";
            $sess->freeze();
            exit;
            break;
        }
        break;
      default:
        # This should never happen. Complain.
        echo "Error in auth handling: invalid state reached.\n";
        $sess->freeze();
        exit;
        break;
    }
  }

  function login_if( $t ) {
    if ( $t ) {
      $this->unauth();  # We have to relogin, so clear current auth info
      $this->nobody = false; # We are forcing login, so default auth is 
                             # disabled
      $this->start(); # Call authentication code
    }
  }

  function unauth($nobody = false) {
    $this->auth["uid"]   = "";
    $this->auth["perm"]  = "";
    $this->auth["exp"]   = 0;

    ## Back compatibility: passing $nobody to this method is
    ## deprecated
    if ($nobody) {
      $this->auth["uid"]   = "nobody";
      $this->auth["perm"]  = "";
      $this->auth["exp"]   = 0x7fffffff;
    }
  }
  

  function logout($nobody = "") {
    global $sess;
    
    $sess->unregister("auth");
    unset($this->auth["uname"]);
    $this->unauth($nobody == "" ? $this->nobody : $nobody);
  }

  function is_authenticated() {
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

      return $this->auth["uid"];
    } else {
      return false;
      return $this->auth["uid"];
    }
  }
    
  ########################################################################
  ##
  ## Helper functions
  ##
  function url() {
    return $GLOBALS["sess"]->self_url();
  }

  function purl() {
    print $GLOBALS["sess"]->self_url();
  }

  ## This method can authenticate a user before the loginform
  ## is being displayed. If it does, it must set a valid uid 
  ## (i.e. nobody IS NOT a valid uid) just like auth_validatelogin,
  ## else it shall return false.

  function auth_preauth() { return false; }
  
  ##
  ## Authentication dummies. Must be overridden by user.
  ##
  
  function auth_loginform() { ; }

  function auth_validatelogin() { ; }
  
  function auth_refreshlogin() { ; }

  function auth_registerform() { ; }

  function auth_doregister() { ; }
}
?>
