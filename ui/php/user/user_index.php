<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : user_index.php                                               //
//     - Desc : User Index File                                              //
// 2000-01-13 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the user search form
// - search          -- search fields  -- show the result set of search
// - new             --                -- show the new user form
// - detailconsult   -- $param_user    -- show the user detail
// - detailupdate    -- $param_user    -- show the user detail form
// - insert          -- form fields    -- insert the user
// - reset           -- $param_user    -- reset user preferences
// - update          -- form fields    -- update the user
// - check_delete    -- $param_user    -- check links before delete
// - delete          -- $param_user    -- delete the user
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms Management                                           //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "USERS";
$menu="USER";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

// the user MUST be "admin" to access this section

require("user_display.inc");
require("user_query.inc");

//There is no page_close()
if($action == "") $action = "index";
$obm_user = get_param_user();  // $user is used by phplib
get_user_action();
$perm->check();
///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_user);        // Head & Body
generate_menu($menu,$section);         // Menu

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  html_user_search_form($obm_user);
  if ($set_display == "yes") {
    dis_user_search_list($obm_user);
  } else {
    display_ok_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  html_user_search_form($obm_user);
  dis_user_search_list($obm_user);

} elseif ($action == "new")  {
///////////////////////////////////////////////////////////////////////////////
  include("user_js.inc");
  html_user_form(1,"",$obm_user);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($param_user);
  if ($obm_q->num_rows() == 1) {
    display_record_info($obm_q->f("userobm_usercreate"),$obm_q->f("userobm_userupdate"),$obm_q->f("timecreate"),$obm_q->f("timeupdate")); 
    html_user_consult($obm_q);
  } else {
    display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_detail($param_user);
  if ($obm_q->num_rows() == 1) {
    include("user_js.inc");
    display_record_info($obm_q->f("userobm_usercreate"),$obm_q->f("userobm_userupdate"),$obm_q->f("timecreate"),$obm_q->f("timeupdate")); 
    html_user_form(1, $obm_q, $obm_user);
  } else {
    display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form("", $obm_user)) {

    // If the context (same user) was confirmed ok, we proceed
    if ($hd_confirm == $c_yes) {
      $retour = run_query_insert($obm_user);
      if ($retour) {
        display_ok_msg($l_insert_ok);
        // insertion of his default preferences : 
        $user_id = run_query_id_user($obm_user["login"], $obm_user["passwd"]);
        run_query_default_preferences_insert($user_id);
      } else {
      display_err_msg($l_insert_error);
      }
      html_user_search_form($obm_user);

    // If it is the first try, we warn the user if some user seem similar
    } else {
      $obm_q = check_user_context("", $obm_user);
      if ($obm_q->num_rows() > 0) {
        dis_user_warn_insert("", $obm_q, $obm_user);
      } else {
        $retour = run_query_insert($obm_user);
        if ($retour) {
          // insertion of his default preferences : 
          $user_id = run_query_id_user($obm_user["login"], $obm_user["passwd"]);
          run_query_default_preferences_insert($user_id);
          display_ok_msg($l_insert_ok);
        } else {
          display_err_msg($l_insert_error);
        }
        html_user_search_form($obm_user);
      }
    }

  // Form data are not valid
  } else {
    display_warn_msg($l_invalid_data . " : " . $err_msg);
    html_user_form(0, "", $obm_user);
  }

} elseif ($action == "reset")  {
///////////////////////////////////////////////////////////////////////////////
  run_query_default_preferences_insert($param_user);
  session_load_user_prefs();
  display_ok_msg($l_reset_ok);
  $obm_q = run_query_detail($param_user);
  if ($obm_q->num_rows() == 1) {
    display_record_info($obm_q->f("userobm_usercreate"),$obm_q->f("userobm_userupdate"),$obm_q->f("timecreate"),$obm_q->f("timeupdate")); 
    html_user_consult($obm_q);
  } else {
    display_err_msg($l_query_error . " - " . $query . " !");
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($param_user, $obm_user)) {
    $retour = run_query_update($param_user, $obm_user);
    if ($retour) {
      display_ok_msg($l_update_ok);
    } else {
      display_err_msg($l_update_error);
    }
    html_user_search_form($obm_user);
  } else {
    display_err_msg($err_msg);
  }

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("user_js.inc");
  dis_check_links($param_user);

} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_delete($param_user);
  if ($retour) {
    display_ok_msg($l_delete_ok);
  } else {
    display_err_msg($l_delete_error);
  }
  run_query_delete_profil($param_user);
  html_user_search_form($obm_user);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  echo "<center>Nothing here</center><br />";
}

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
page_close();
display_end();



///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $obm_user hash
// returns : $obm_user hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_user() {
  global $cdg_param;
  global $param_user, $tf_login, $tf_passwd, $sel_perms,$tf_email;
  global $tf_lastname, $tf_firstname, $cb_archive;

  if (isset ($param_user)) $obm_user["id"] = $param_user;
  if (isset ($tf_login)) $obm_user["login"] = $tf_login;
  if (isset ($tf_lastname)) $obm_user["lastname"] = $tf_lastname;
  if (isset ($tf_passwd)) $obm_user["passwd"] = $tf_passwd;
  if (isset ($sel_perms)) $obm_user["perms"] = $sel_perms;
  if (isset ($tf_email)) $obm_user["email"] = $tf_email;
  if (isset ($tf_lastname)) $obm_user["lastname"] = $tf_lastname;
  if (isset ($tf_firstname)) $obm_user["firstname"] = $tf_firstname;
  if (isset ($cb_archive)) $obm_user["archive"] = $cb_archive;

  if (debug_level_isset($cdg_param)) {
    if ( $obm_user ) {
      while ( list( $key, $val ) = each( $obm_user ) ) {
        echo "<BR>user[$key]=$val";
      }
    }
  }

  return $obm_user;
}

///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_user_action() {
  global $obm_user, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_display,$l_header_admin,$l_header_reset;
  global $user_read, $user_write, $user_admin_read, $user_admin_write;

// Index
  $actions["USER"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/user/user_index.php?action=index",
    'Right'    => $user_read,
    'Condition'=> array ('all') 
                                    );

// New
  $actions["USER"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/user/user_index.php?action=new",
    'Right'    => $user_write,
    'Condition'=> array ('search','index','admin','detailconsult','display') 
                                  );

// Search
  $actions["USER"]["search"] = array (
    'Url'      => "$path/user/user_index.php?action=new",
    'Right'    => $user_read,
    'Condition'=> array ('None') 
                                  );

// Detail Consult
  $actions["USER"]["detailconsult"] = array (
     'Url'      => "$path/user/user_index.php?action=detailconsult",
    'Right'    => $user_read,
    'Condition'=> array ('None') 
                                  );

// Reset
  $actions["USER"]["reset"] = array (
    'Name'     => $l_header_reset,
    'Url'      => "$path/user/user_index.php?action=reset&amp;param_user=".$obm_user["id"]."",
    'Right'    => $user_write,
    'Condition'=> array ('detailconsult') 
                                    );

// Detail Update
  $actions["USER"]["detailupdate"] = array (
     'Name'     => $l_header_update,
     'Url'      => "$path/user/user_index.php?action=detailupdate&amp;param_user=".$obm_user["id"]."",
     'Right'    => $user_write,
     'Condition'=> array ('detailconsult') 
                                     	   );

// Insert
  $actions["USER"]["insert"] = array (
    'Url'      => "$path/user/user_index.php?action=insert",
    'Right'    => $user_write,
    'Condition'=> array ('None') 
                                     );

// Update
  $actions["USER"]["update"] = array (
    'Url'      => "$path/user/user_index.php?action=update",
    'Right'    => $user_write,
    'Condition'=> array ('None') 
                                     );

// Check Delete
  $actions["USER"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/user/user_index.php?action=check_delete&amp;param_user=".$obm_user["id"]."",
    'Right'    => $user_write,
    'Condition'=> array ('detailconsult') 
                                     	   );

// Delete
  $actions["USER"]["delete"] = array (
    'Url'      => "$path/user/user_index.php?action=delete",
    'Right'    => $user_write,
    'Condition'=> array ('None') 
                                     );

// Admin
  $actions["USER"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/user/user_index.php?action=admin",
    'Right'    => $user_admin_read,
    'Condition'=> array ('all') 
                                    );

// Dispay
  $actions["USER"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/user/user_index.php?action=display",
    'Right'    => $user_read,
    'Condition'=> array ('all') 
                                      	 );

}

</SCRIPT>
