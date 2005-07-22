<script language="php">
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

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms  Management                                            //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "time";
$extra_css = "time.css";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("time_display.inc");
require("time_query.inc");

$uid = $auth->auth["uid"]; //current user uid

if (!isset($action)) $action = "index";
$time = get_param_time();
get_time_actions();
$perm->check_permissions($module, $action);

page_close();

require("time_js.inc");
//$extra_file_js = "time_js.inc";


///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
$project_managers = run_query_managers();
$stats_users = $project_managers;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  dis_time_index($time);

} elseif ($action == "viewmonth") {
///////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  $display["result"] = dis_time_nav_date($time);
  $display["result"] .= dis_time_planning($time);
  $display["features"] .= dis_user_select($time, run_query_get_obmusers());
 
} elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "week";
  run_query_insert($time);
  run_query_validate($time["user_id"]);
  dis_time_index($time);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  $d_start_week = first_day_week($time["date"]);
  $val_days = run_query_valid_search($time);
  $display["result"] .= dis_form_addtask($time, $val_days);

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  run_query_update($time);
  run_query_validate($time["user_id"]);
  // close the popup
  $display["result"] .= "
    <script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&wbegin=".$wbegin."';
     window.close();
    </script>
  ";

} elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "week";
  run_query_delete($HTTP_POST_VARS);
  run_query_validate($time["user_id"]);
  dis_time_index($time);
  
} if ($action == "globalview") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  $display["result"] = dis_time_nav_date($time);
  $display["result"] .= dis_time_month_users_total($time);

} elseif ($action == "validate") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  run_query_adminvalidate($time);
  $display["result"] = dis_time_nav_date($time);
  $display["result"] .= dis_time_month_users_total($time);

} elseif ($action == "unvalidate") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  run_query_adminunvalidate($time);
  $display["result"] = dis_time_nav_date($time);
  $display["result"] .= dis_time_month_users_total($time);

} elseif ($action == "stats") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "month";
  $statproj_q = run_query_stat_project($time);
  $stattt_q = run_query_stat_tasktype($time);
  $display["result"] = dis_time_nav_date($time);
  $display["features"] .= dis_user_select($time, run_query_get_obmusers(), 1);
  $display["result"] .= dis_time_statsuser($statproj_q, $stattt_q, $time);

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
  $display["header"] = generate_menu($module, $section);
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_time() {
  global $uid, $sess, $sess_users, $set_debug, $cdg_param, $action, $wbegin;
  global $sel_date, $sel_tasktype, $sel_time, $tf_label, $task_id;
  global $sel_project, $sel_projecttask, $wbegin, $user_id;
  global $tf_lweek, $f_time, $sel_user_id, $cb_day, $rd_day;

  if (isset ($action)) $time["action"] = $action;
  if (isset ($sel_tasktype)) $time["tasktype"] = $sel_tasktype;
  if (isset ($sel_project)) $time["project"] = $sel_project;
  if (isset ($sel_projecttask)) $time["projecttask"] = $sel_projecttask;
  if (isset ($sel_time)) $time["time"] = $sel_time;
  if (isset ($tf_label)) $time["label"] = $tf_label;
  if (isset ($f_time)) $time["f_time"] = $f_time;
  if (isset ($submit)) $time["submit"] = $submit;
  if (isset ($wbegin)) $time["date"] = $wbegin;
  if ($time["date"] == "") { $time["date"] = date("Ymd"); }
  if (isset ($task_id)) $time["task_id"] = $task_id;

  if (is_array($cb_day)) $time["sel_date"] = $cb_day;
  elseif (isset ($rd_day)) $time["sel_date"] = $rd_day;

  if (isset ($user_id)) $time["user_id"] = $user_id;
  if (isset ($sel_user_id)) $time["user_id"] = $sel_user_id;

  // We retrieve the selected users if any, else we get them from sessiom
  if (isset ($sel_user_id)) {
    if (is_array($sel_user_id)) {
      $time["user_id"] = $uid;
      $time["user_ids"] = $sel_user_id;
    } else {
      $time["user_id"] = $sel_user_id;
      $time["user_ids"] = array($sel_user_id);
    }
    $sess_users = $sel_user_id;
    $sess->register("sess_users");
  } elseif (isset($sess_users)) {
    if (is_array($sess_users)) {
      $time["user_id"] = $uid;
      $time["user_ids"] = $sess_users;
    } else {
      $time["user_id"] = $sess_users;
      $time["user_ids"] = array($sess_users);
    }
  } else {
    $sess_users = $uid;
    $time["user_id"] = $uid;
    $time["user_ids"] = array($uid);
    $sess->register("sess_users");
  }

  display_debug_param($time);

  return $time;
}


//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////
function get_time_actions() {
  global $time, $path, $actions;
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
                  "&amp;wbegin=" . $time["date"],
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// General Monthly View
  $actions["time"]["globalview"] = array (
    'Name'     => "$l_header_globalview",
    'Url'      => "$path/time/time_index.php?action=globalview".
                  "&amp;wbegin=" . $time["date"],
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

</script>
