<?php
/*
 * Session Management for PHP4
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp, Barend Scholtus
 *
 * page4.inc,v 0.5 2000/10/29
 *
 */ 

function page_open($feature) {
  global $_PHPLIB;

  # enable sess and all dependent features.
  if (isset($feature["sess"])) {
    global $sess;
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

      # the user feature depends on auth and sess
      if (isset($feature["user"])) {
        global $user;
        
        if (!isset($user)) {
          $user = new $feature["user"];
        }
        $user->start($auth->auth["uid"]);
      }
    }

    ## Load the auto_init-File, if one is specified.
    if (($sess->auto_init != "") && ($sess->in == "")) {
      $sess->in = 1;
      include($_PHPLIB["libdir"] . $sess->auto_init);
    } 
  }
}

function page_close() {
  global $sess, $user,$auth;
  $base_session = NEW OBM_User_Sql;
  $base_session->ac_start();
  $base_session->ac_store($sess->id,"OBM_User_Active",$auth->auth["uid"]);
}


?>
