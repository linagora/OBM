<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_index.php                                               //
//     - Desc : Time management Index File                                   //
// 2002-04-01 Pierre Carlier                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- dislay task list and new task form.
// - viewmonth       --                -- display month
// - globalview      --                -- display validation pannel
// - insert          -- form fields    -- insert the task
// - detailupdate    -- $task_id       -- show the update task form in a popup
// - update          -- form fields    -- update the task
// - delete          -- $params        -- delete the tasks
// - validate        --                -- validate a month for a user
// - unvalidate      --                -- cancel admin validation
// - stats           --                -- show stats screen
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

// Todo
// - handle project_managers
// - review actions : stats
// - probleme de date !! stockage different Mysql et postgres (timetask_date)
//   mysql : 20041116..., postgres 2004-11-16...
//   d'ou toutes les fonctions substr... pour recup mois, jour depuis bd ko !! avec pg (attention pas depuis time[date]
// - point sur timetask_status ! dans globalview : incoherent
//   => d'ou sur validate, unvalidate et valid auto

$path = "..";
$module = "time";
$extra_css = "time.css";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
$params = get_time_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
$uid = $auth->auth["uid"];
update_time_session_params();
include("$obminclude/global_pref.inc");
require("time_display.inc");
require("time_query.inc");
require("time_js.inc");

get_time_actions();
$perm->check_permissions($module, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
$project_managers = run_query_time_managers();
$stats_users = $project_managers;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  dis_time_index($params);

} elseif ($action == "viewmonth") {
///////////////////////////////////////////////////////////////////////////////
  $params["interval"] = "month";
  $display["result"] = dis_time_nav_date($params);
  $display["result"] .= dis_time_planning($params);
  $display["features"] .= dis_user_select($params, run_query_time_get_obmusers(), 1);
  // 1.1 => FIXME
  //$display["features"] .= dis_user_select($time, run_query_time_get_obmusers());

} elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////;
  $params["interval"] = "week";
  run_query_time_insert($params);
  run_query_time_validate($params["user_id"]);
  dis_time_index($params);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $d_start_week = time_first_day_week($params["date"]);
  $val_days = run_query_time_valid_search($params);
  $display["result"] .= dis_time_form_addtask($params, $val_days);

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  run_query_time_update($params);
  run_query_time_validate($params["user_id"]);
  // Javascript here because muist be run after the form submit
  $display["result"] .= "
    <script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&user_id=".$params["user_id"]."&date=".$params["date"]."';
     window.close();
    </script>
  ";


} elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $params["interval"] = "week";
  run_query_time_delete($_REQUEST);
  run_query_time_validate($params["user_id"]);
  dis_time_index($params);
  
} if ($action == "globalview") {
//////////////////////////////////////////////////////////////////////////////
  $params["interval"] = "month";
  $display["result"] = dis_time_nav_date($params);
  $display["result"] .= dis_time_month_users_total($params);

} elseif ($action == "validate") {
//////////////////////////////////////////////////////////////////////////////
  $params["interval"] = "month";
  run_query_time_adminvalidate($params);
  $display["result"] = dis_time_nav_date($params);
  $display["result"] .= dis_time_month_users_total($params);

} elseif ($action == "unvalidate") {
//////////////////////////////////////////////////////////////////////////////
  $params["interval"] = "month";
  run_query_time_adminunvalidate($params);
  $display["result"] = dis_time_nav_date($params);
  $display["result"] .= dis_time_month_users_total($params);

} elseif ($action == "stats") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $params["interval"] = "month";
  $statproj_q = run_query_time_stat_project($params);
  $stattt_q = run_query_time_stat_tasktype($params);
  $display["result"] = dis_time_nav_date($params);
  $display["features"] .= dis_user_select($params, run_query_time_get_obmusers(), 1);
  $display["result"] .= dis_time_statsuser($statproj_q, $stattt_q, $params);

} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($prefs);

} else if ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($prefs);

} else if ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($prefs);
}  


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head($l_time);
$display["end"] = display_end();
if (! $popup) {
  $display["header"] = display_menu($module);
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_time_params() {
  global $uid;

  // Get global params
  $params = get_global_params();

  // Get time specific params
  if ($params["date"] == "") { $params["date"] = date("Ymd"); }
  
  // We retrieve the selected users if any, else we get them from sessiom
  if (isset ($params["user_id"])) {
    if (is_array($params["user_id"])) {
      $params["user_ids"] = $params["user_id"];
    } else {
      $params["user_ids"] = array($params["user_id"]);
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Update session and parameters
///////////////////////////////////////////////////////////////////////////////
function update_time_session_params() {
  global $sess, $sess_users, $params, $uid;

  // We retrieve the selected users if any, else we get them from sessiom
  if (isset($params["user_ids"])) {
    $sess_users = $params["user_ids"];
    $sess->register("sess_users");
  } elseif (isset($sess_users)) {
    if (is_array($sess_users)) {
      $params["user_id"] = $uid;
      $params["user_ids"] = $sess_users;
    } else {
      $params["user_id"] = $sess_users;
      $params["user_ids"] = array($sess_users);
    }
  } else {
    $sess_users = $uid;
    $params["user_id"] = $uid;
    $params["user_ids"] = array($uid);
    $sess->register("sess_users");
  }
}


//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////
function get_time_actions() {
  global $params, $path, $actions;
  global $l_header_weeklyview, $l_header_monthlyview, $l_header_globalview;
  global $l_header_stats, $l_header_display;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["time"]["index"] = array (
    'Name'     => "$l_header_weeklyview",
    'Url'      => "$path/time/time_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array('all') 
                                    );

// User Monthly View
  $actions["time"]["viewmonth"] = array (
    'Name'     => "$l_header_monthlyview",
    'Url'      => "$path/time/time_index.php?action=viewmonth".
                  "&amp;date=" . $params["date"],
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// General Monthly View
  $actions["time"]["globalview"] = array (
    'Name'     => "$l_header_globalview",
    'Url'      => "$path/time/time_index.php?action=globalview".
                  "&amp;date=" . $params["date"],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
                                    );

// Detail Update
  $actions["time"]["detailupdate"] = array (
    'Url'      => "$path/time/time_index.php?action=detailupdate",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Insert 
  $actions["time"]["insert"] = array (
    'Url'      => "$path/time/time_index.php?action=insert",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    );

// Update
  $actions["time"]["update"] = array (
    'Url'      => "$path/time/time_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Delete 
  $actions["time"]["delete"] = array (
    'Url'      => "$path/time/time_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Validate
  $actions["time"]["validate"] = array (
    'Url'      => "$path/time/time_index.php?action=validate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Cancel Validation
  $actions["time"]["unvalidate"] = array (
    'Url'      => "$path/time/time_index.php?action=unvalidate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Stats by Users
  $actions["time"]["stats"] = array (
    'Name'     => "$l_header_stats",
    'Url'      => "$path/time/time_index.php?action=stats",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                     );

// Display
   $actions["time"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/time/time_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Preferences
  $actions["time"]["dispref_display"] = array (
    'Url'      => "$path/time/time_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["time"]["dispref_level"]  = array (
    'Url'      => "$path/time/time_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
}

?>
