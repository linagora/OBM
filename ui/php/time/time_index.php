<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_index.php                                               //
//     - Desc : Time management Index File                                   //
// 2002-04-01 Pierre Carlier                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the task week list and the task 
//					  insert form.
// - viewmonth       --                -- display month
// - globalview      --                -- display validation pannel
// - insert          -- form fields    -- insert the task
// - validate        --                -- validate a month for a user
// - unvalidate      --                -- cancel admin validation
// - stats           --                -- show stats screen
// - delete          -- $params        -- delete the tasks
// - detailupdate    -- $task_id       -- show the update task form in a popup
// -                 -- form fields    -- update the task
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms  Management                                            //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "PROD";
$menu = "TIME";
$extra_css = "time.css";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("time_display.inc");
require("time_query.inc");

$time = get_param_time();
$uid = $auth->auth["uid"]; //current user uid


// user select ---------------------------------------------

// a user was selected
if (! empty($time["user_id"])) {

  // we come back from stats : we retrieve the user
  if (is_array($time["user_id"]) and ($action != "stats")) {
    $time["user_id"] = $uid;
  } else if (!(is_array($time["user_id"])) and ($action == "stats")) {
    $time["user_id"] = array($time["user_id"]);
  }

  if ($action == "stats")
    $time["user_id"] = array_unique($time["user_id"]);

  $s_users = $time["user_id"];
  $sess->register("s_users");

  // debug ------------------------------------------------
  if (debug_level_isset($cdg_param)) {
    echo "\$time[user_id] not empty : $s_users <br>";
    if (is_array($s_users)) {
      echo "\$s_users is an array ---";
      print_r($s_users);
      echo " ---<br>";
    }
    else if (is_string($s_users))
      echo "\$s_users is a string : ". $s_users ." -- <br>";
  }
}

// we use the session variable
else if (isset($s_users)) {

  // we come back fro stats : we retrieve the user
  if ($action == "") {
    $s_users = $uid;
    $sess->register("s_users");
  } else if (is_array($s_users) and ($action != "stats")) {
    $s_users = $uid;
    $sess->register("s_users");
  } else if (!(is_array($s_users)) and ($action == "stats")) {
    $s_users = array($s_users);
    $sess->register("s_users");
  }

  $time["user_id"] = $s_users;

  // debug ------------------------------------------------
  if (debug_level_isset($cdg_param)) {
    echo "session variable \$s_users is set : ". $s_users ." <br>";
    if (is_array($s_users)) {
      echo "\$s_users is an array ---";
      print_r($s_users);
      echo " ---<br>";
    }
    else if (is_string($s_users))
      echo "\$s_users is a string : ". $s_users ." -- <br>";
  }
}

// we arrive in the time module
else {
  $s_users = $uid;
  $time["user_id"] = $s_users;
  $sess->register("s_users");

  // debug ------------------------------------------------
  if (debug_level_isset($cdg_param)) {
     echo "everything empty, we define user_id with $uid <br>";
  }
}

page_close();

// $popup = 2 => writing into a file
if ($popup != 2) {
  require("time_js.inc");
}

///////////////////////////////////////////////////////////////////////////////
//Initialisation                                                             //
///////////////////////////////////////////////////////////////////////////////
//set user_id if not set
if (!(isset($time["user_id"])))
  $time["user_id"] = $uid;

if (!(isset($time["date"])))
  $time["date"] = date("Ymd");

if ( !isset($action)) $action = "index";

get_time_actions();
$perm->check_permissions($menu, $action);


///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
$project_managers = run_query_managers();
$stats_users = $project_managers;
///////////////////////////////////////////////////////////////////////////////

if (debug_level_isset($cdg_param)) {

  if ( $time ) {
    echo "<br>\$time : <br>";
    debug_array($time);
  }

  echo "<br>HTTP_POST_VARS<br>";
  foreach($HTTP_POST_VARS as $key => $val) {
    echo "clé $key val $val <br>";
  }
  echo "sel_user_id $sel_user_id";
  if (!is_null($sel_user_id))
    echo " sel_user_id is not null <br>";
  else
    echo " sel_user_id is NULL <br>";
}


///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
if (! $popup) {
  $display["header"] = generate_menu($menu,$section);         // Menu
}

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
//////////////////////////////////////////////////////////////////////////////

  // interval is week -- see if we may need to use other intervals
  $time["interval"] = "week";
  $time["action"] = "index";

  // display links to previous and next week
  $display["result"] = dis_time_links($time,"week");

  // week planning display
  $display["result"] .= dis_time_index($time);

  // display addtask form if necessary and full task list
  $display["result"] .= dis_time_list($time);

  // display user Search Form
  $display["features"] .= dis_time_search_form($time,
                       run_query_get_obmusers(),
                       $uid);

} elseif ($action == "viewmonth") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  // display links to previous and next week
  $display["result"] = dis_time_links($time,"month");
  // display the month panel
  $display["result"] .= dis_time_index($time);
  // display user Search Form
  $display["features"] .= dis_time_search_form($time, 
                       run_query_get_obmusers(),
                       $uid);
 
} if ($action == "globalview") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  // display links to previous and next week
  $display["result"] = dis_time_links($time,"month");
  // display validation panel
  $display["result"] .= dis_time_index($time);

} elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["action"]="index";
  $time["interval"] = "week";
  $display["result"] = dis_time_links($time,"week");
  run_query_insert($time);
  run_query_validate($time["user_id"]);
  $display["result"] .= dis_time_index($time);
  $display["result"] .= dis_time_list($time);
  $display["features"] .= dis_time_search_form($time,
					     run_query_get_obmusers(),
					     $uid);

} elseif ($action == "validate") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";
  run_query_adminvalidate($time);
  // display links to previous and next week
  $display["result"] = dis_time_links($time,"month");
  // display validation panel
  $display["result"] .= dis_time_index($time);

} elseif ($action == "unvalidate") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";

  run_query_adminunvalidate($time);

  // display links to previous and next week
  $display["result"] = dis_time_links($time,"month");

  // display validation panel
  $display["result"] .= dis_time_index($time);
}

elseif ($action == "stats") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "month";

  $statproj_q = run_query_stat_project($time);
  $stattt_q = run_query_stat_tasktype($time);

  $display["result"] = dis_time_links($time,$time["interval"]);
  $display["features"] .= dis_time_search_form($time, 
					       run_query_get_obmusers(),
					       $uid, 1);


  $display["result"] .= dis_time_statsuser($statproj_q, $stattt_q, $time);
}

// bcontins : pas utilise pour le moment
// elseif ($action == "export_stats") {
// //////////////////////////////////////////////////////////////////////////////
//   // interval is week -- see if we may need to use others intervals
//   $time["interval"] = "month";

//   //  echo "export stats<br>";
//   $display["result"] = dis_time_export_stats($time);
// }

elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////

  // interval is week -- see if we may need to use others intervals
  $time["action"] = "index";
  $time["interval"] = "week";
 
  $display["result"] = dis_time_links($time,"week");

  run_query_delete($HTTP_POST_VARS);
  run_query_validate($time["user_id"]);

  $display["result"] .= dis_time_index($time);
  $display["result"] .= dis_time_list($time);
  $display["features"] .= dis_time_search_form($time, 
					       run_query_get_obmusers(),
					       $uid);
}

elseif ($action == "detailupdate") {
//////////////////////////////////////////////////////////////////////////////
  
  // open the popup
  // interval is week -- see if we may need to use others intervals
  if ( $popup == 1 ) {
    // Get elements for insertion of new task
    // TaskType
    $obm_tasktype_q = run_query_used_tasktype($time["user_id"]);
    // Project
    $obm_project_q = run_query_project($time);
    // ProjectTask
    $obm_projecttask_q = run_query_projecttask($time);
    //get the current start and end of week
    $d_start_week = first_day_week($time);
    // Creating the dates for the selected (or current) date
    $day_q = get_this_week($d_start_week, $c_days_in_a_week);
    
    $display["result"] .= dis_form_addtask("detailupdate",
					   $obm_project_q, 
					   $obm_projecttask_q, 
					   $obm_tasktype_q, 
					   $day_q, 
					   $c_day_fraction, 
					   $time, 
					   null);

  }

  // close the popup
  else {
    run_query_update($time);
    run_query_validate($time["user_id"]);
    $user_id = $time["user_id"];
    $display["result"] .= "
      <script language=\"javascript\">
       window.opener.location.href='$path/time/time_index.php?action=index&wbegin=".$wbegin."';
       window.close();
      </script>
    ";
  }
  
}  elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($pref_search_q);

} else if ($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($pref_search_q);

} else if ($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_search_q = run_query_display_pref($auth->auth["uid"], "time", 1);
  $display["detail"] = dis_time_display_pref($pref_search_q);
}  

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
// except export in file
if ($popup != 2) {
  $display["head"] = display_head($l_time);
  $display["end"] = display_end();
  display_page($display);
}

///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_time() {
  // Forms parameters
  global $sel_date, $sel_tasktype, $sel_time, $tf_label, $task_id;
  global $sel_project, $sel_projecttask, $wbegin, $user_id;
  global $tf_lweek, $f_time, $sel_user_id, $cb_day, $rd_day, $cb_allusers;
  //global $st_detail;

  // URLs parameters
  global $action, $wbegin;
  global $set_debug, $cdg_param;

  if (isset ($action)) $task["action"] = $action;
  if (isset ($sel_tasktype)) $task["tasktype"] = $sel_tasktype;
  if (isset ($sel_project)) $task["project"] = $sel_project;
  if (isset ($sel_projecttask)) $task["projecttask"] = $sel_projecttask;
  if (isset ($sel_time)) $task["time"] = $sel_time;
  if (isset ($tf_label)) $task["label"] = $tf_label;
  if (isset ($f_time)) $task["f_time"] = $f_time;
  if (isset ($user_id)) $task["user_id"] = $user_id;
  if (isset ($sel_user_id)) $task["user_id"] = $sel_user_id;
  if (isset ($cb_allusers)) $task["allusers"] = $cb_allusers;
  if (isset ($submit)) $task["submit"] = $submit;
  if (isset ($wbegin)) $task["date"] = $wbegin;
  if (isset ($task_id)) $task["task_id"] = $task_id;

  if (is_array($cb_day)) $task["sel_date"] = $cb_day;
  elseif (isset ($rd_day)) $task["sel_date"] = $rd_day;

  if (debug_level_isset($cdg_param)) {
    global $action;
    echo "<br>action=$action";
    if ( $task ) {
      echo "<br>get_param_time() : ";
      while ( list( $key, $val ) = each( $task ) ) {
	if (is_array($val)) {
          echo "<br>task[$key]=";
	  print_r($val);
	}
	else
          echo "<br>task[$key]=$val";
      }
    }
  }

  return $task;
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
  $actions["TIME"]["index"] = array (
    'Name'     => "$l_header_weeklyview",
    'Url'      => "$path/time/time_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array('all') 
                                    );

// User Monthly View
  $actions["TIME"]["viewmonth"] = array (
    'Name'     => "$l_header_monthlyview",
    'Url'      => "$path/time/time_index.php?action=viewmonth".
                  "&amp;wbegin=" . $time["date"],
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// General Monthly View
  $actions["TIME"]["globalview"] = array (
    'Name'     => "$l_header_globalview",
    'Url'      => "$path/time/time_index.php?action=globalview".
                  "&amp;wbegin=" . $time["date"],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
                                    );

// Validate
  $actions["TIME"]["validate"] = array (
    'Url'      => "$path/time/time_index.php?action=validate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Cancel Validation
  $actions["TIME"]["unvalidate"] = array (
    'Url'      => "$path/time/time_index.php?action=unvalidate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Stats by Users
  $actions["TIME"]["stats"] = array (
    'Name'     => "$l_header_stats",
    'Url'      => "$path/time/time_index.php?action=stats",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                     );

// Insert 
  $actions["TIME"]["insert"] = array (
    'Url'      => "$path/time/time_index.php?action=insert",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    );

// Delete 
  $actions["TIME"]["delete"] = array (
    'Url'      => "$path/time/time_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Detail Update
  $actions["TIME"]["detailupdate"] = array (
    'Url'      => "$path/time/time_index.php?action=detailupdate",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Display
   $actions["TIME"]["display"] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/time/time_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Préférences
  $actions["TIME"]["dispref_display"] = array (
    'Url'      => "$path/time/time_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions["TIME"]["dispref_level"]  = array (
    'Url'      => "$path/time/time_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );
}

</script>
