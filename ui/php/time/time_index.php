<SCRIPT language="php">
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
// - insert          -- form fields    -- insert the task
// - update          -- $task_id       -- show the update task form in a popup
// -                 -- form fields    -- update the task
// - delete          -- $params        -- delete the tasks
// - export_stats    --                -- export stats
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms  Management                                            //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "PROD";
$menu = "TIME";

// Deal we display in project list
//  added proposal for 'avantvente';

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

//require("../agenda/agenda_functions.inc");
require("time_display.inc");
require("time_query.inc");

$time = get_param_time();

// bcontins akoicasert
// We need to have first day of the month 
//  Maybe should be set other way...
// $time["month_first"] = first_day_month($time, false);
//echo "action $action, show_task_detail $st_detail <br>";

$uid = $auth->auth["uid"]; //current user uid

if (debug_level_isset($cdg_param)) {
  echo "login : $uid <br>";
}

if (! empty($time["user_id"])) {
  $s_users = $time["user_id"];
  $sess->register("s_users");
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
else if (isset($s_users)) {
  // $s_users is a session variable
  $time["user_id"] = $s_users;
  if (debug_level_isset($cdg_param)) {
	echo "variable de session \$s_users is set : ". $s_users ." <br>";
    if (is_array($s_users)) {
	  echo "\$s_users is an array ---";
      print_r($s_users);
	  echo " ---<br>";
	}
    else if (is_string($s_users))
	  echo "\$s_users is a string : ". $s_users ." -- <br>";
  }
}

else {
  $s_users = array($uid);
  $time["user_id"] = $s_users;
  $sess->register("s_users");
  if (debug_level_isset($cdg_param)) {
	echo "tout est vide, on définit user_id sur $uid <br>";
  }
}
page_close();

// $popup = 2 => writing into a file
if ($popup != 2) {
  require("time_js.inc");
}

if ( !isset($action)) $action = "index";

get_time_actions();
$perm->check("user");


///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
// $project_managers = array( '6' , '7' , '8','23' ) ;
// $stats_users = array( '6' , '7' , '8','23' ) ;
$project_managers = run_query_managers();
$stats_users = run_query_managers();
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
if ($popup != 2) {
  display_head($l_time);     // Head & Body
}
if (! $popup) {
  generate_menu($menu,$section);         // Menu
}

///////////////////////////////////////////////////////////////////////////////
//Initialisation                                                             //
///////////////////////////////////////////////////////////////////////////////
//set user_id if not set
if (! isset($time["user_id"]))
  $time["user_id"] = $uid;

if (! isset($time["date"]))
  $time["date"]=date("Ymd");

// bcontins : ne devrait plus servir
//set $show_task_detail flag
// ?? THIS CAN BE A USER PREFERENCE ??
//if( ! isset($time["show_task_detail"]) )
//  $time["show_task_detail"] = true;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
//////////////////////////////////////////////////////////////////////////////

  // interval is week -- see if we may need to use other intervals
  $time["interval"] = "week";
  $time["action"] = "index";

  // USERS : only one for index (and others, except stats)
  if (! in_array($u_id, $project_managers))
    $time["user_id"] == array($u_id);
  else if (sizeof($time["user_id"]) > 1) {
    $tt = $time["user_id"];
    $time["user_id"] == $tt[0];
  }
  else if (sizeof($time["user_id"]) == 0) {
    echo "error \$time[user_id] empty !!! <br>";
    $time["user_id"] == array($u_id);
  }

  // display links to previous and next week
  dis_time_links($time,"week");

  // bcontins ckoica
  // run_query_contactid_user
  //  -- THIS SHOULD CHANGE TO USE THE User TABLE --

  // bcontins planning hebdomadaire
  dis_time_index($time);

  // display addtask form if necessary and full task list
  dis_time_list($time);

  // display user Search Form
  dis_time_search_form($time,
                       run_query_get_obmusers(),
                       $uid);

} 

elseif ($action == "viewmonth") {
//////////////////////////////////////////////////////////////////////////////

  $time["interval"] = "month";

  // USERS : only one for index (and others, except stats)
  if (! in_array($u_id, $project_managers))
	$time["user_id"] == array($u_id);
  else if (sizeof($time["user_id"]) > 1) {
	$tt = $time["user_id"];
	$time["user_id"] == array($tt[0]);
  }
  else if (sizeof($time["user_id"]) == 0) {
    echo "error \$time[user_id] empty !!! <br>";
	$time["user_id"] == array($u_id);
  }

  // display links to previous and next week
  dis_time_links($time,"month");

  // run_query_contactid_user
  //  -- THIS SHOULD CHANGE TO USE THE User TABLE --

  dis_time_index($time);

  // display user Search Form
  dis_time_search_form($time, 
                       run_query_get_obmusers(),
                       $uid);

} 
if ($action == "globalview") {
//////////////////////////////////////////////////////////////////////////////
  $time["interval"] = "month";

  // display links to previous and next week
  dis_time_links($time,"month");

  dis_time_index($time);
  // display user Search Form
  
} 

elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["action"]="index";
  $time["interval"] = "week";

  dis_time_links($time,"week");

  run_query_insert($time);
  run_query_validate($time["user_id"][0]);

  // bcontins planning hebdomadaire
  dis_time_index($time);

  dis_time_list($time);

  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);
}

// bcontins obsolete nouvelle solution a elaborer
// elseif ($action == "validate_admin") {
// //////////////////////////////////////////////////////////////////////////////
//   // interval is week -- see if we may need to use others intervals

//   $time["interval"] = "week";
//   dis_time_links($time,"week");

//   run_query_validate($HTTP_POST_VARS,$time,1);
//   dis_time_list($time); // VALIDATE_ADMIN

//   dis_time_search_form($time, 
//       run_query_get_obmusers(),
//       $uid);

// }

elseif ($action == "stats") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "month";

  dis_time_links($time,$time["interval"]);
  //  dis_time_search_form($time, 
  //  run_query_get_obmusers(),
  //  $uid);
  dis_selectusers_form($time, 
      run_query_get_obmusers(),
      $uid);
  dis_time_stats($time);  // SHOW DETAILS
}

elseif ($action == "export_stats") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "month";

  //  echo "export stats<br>";
  dis_time_export_stats($time);
}

elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////

  // interval is week -- see if we may need to use others intervals
  $time["action"] = "index";
  $time["interval"] = "week";
 
  dis_time_links($time,"week");

  run_query_delete($HTTP_POST_VARS);
  run_query_validate($time["user_id"][0]);

  // bcontins planning hebdomadaire
  dis_time_index($time);

  dis_time_list($time);

  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);
}

elseif ($action == "update") {
//////////////////////////////////////////////////////////////////////////////

  // interval is week -- see if we may need to use others intervals
  if ( $popup == 1 ) {
    // Get elements for insertion of new task
    // TaskType
    $obm_tasktype_q = run_query_tasktype($time["user"]);
    // Project
    $obm_project_q = run_query_project($time);
    //get the current start and end of week
    $d_start_week = first_day_week($time);
    // Creating the dates for the selected (or current) date
    $day_q = get_this_week($d_start_week, $c_days_in_a_week);
    
    // array of validated days
//     $val_days = run_query_valid_search($time);

    dis_form_addtask("update",
		     $obm_project_q, 
		     $obm_tasktype_q, 
		     $day_q, 
		     $c_day_fraction, 
		     $time, 
		     null);
  }

  else {
    run_query_update($time);
    run_query_validate($time["user_id"]["0"]);
  
    $user_id = $time["user_id"]["0"];
    
    echo "
    <Script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&wbegin=".$wbegin."';
     window.close();
    </script>
    ";
  }
}  
 
elseif ($action == "admin") {
//////////////////////////////////////////////////////////////////////////////

}

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
// except export in file
if ($popup != 2)
  display_end();


///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_time() {
  // Forms parameters
  global $sel_date, $sel_tasktype, $sel_project, $sel_time, $tf_label, $task_id;

  global $tf_lweek, $f_time, $sel_user_id, $user_id, $cb_day, $rd_day, $cb_allusers;
  //global $st_detail;

  // URLs parameters
  global $action, $wbegin;
  global $set_debug, $cdg_param;

  if (isset ($action)) $task["action"] = $action;
  if (isset ($sel_tasktype)) $task["tasktype"] = $sel_tasktype;
  if (isset ($sel_project)) $task["project"] = $sel_project;
  if (isset ($sel_time)) $task["time"] = $sel_time;
  if (isset ($tf_label)) $task["label"] = $tf_label;
  if (isset ($f_time)) $task["f_time"] = $f_time;
  if (isset ($sel_user_id)) $task["user_id"] = $sel_user_id;
  if (isset ($user_id)) $task["user_id"] = $user_id;
  if (isset ($cb_allusers)) $task["allusers"] = $cb_allusers;
  if (isset ($submit)) $task["submit"] = $submit;
  if (isset ($wbegin)) $task["date"] = $wbegin;
  if (isset ($task_id)) $task["task_id"] = $task_id;

  // bcontins : obsolete
  //  if (! empty($st_detail)) 
  //   $task["show_task_detail"] = true;
  //else
  //   $task["show_task_detail"] = false;

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
  global $time, $path;
  global $l_header_weeklyview, $l_header_monthlyview, $l_header_globalview;
  global $l_header_stats, $l_header_admin, $l_header_display;
  global $actions, $time_read, $time_write, $time_admin_read, $time_admin_write;

// Index
  $actions["TIME"]["index"] = array (
    'Name'     => "$l_header_weeklyview",
    'Url'      => "$path/time/time_index.php?action=index",
    'Right'    => $time_read,
    'Condition'=> array('all') 
                                    );

// Search
  $actions["TIME"]["viewmonth"] = array (
    'Name'     => "$l_header_monthlyview",
    'Url'      => "$path/time/time_index.php?action=viewmonth".
                  "&amp;wbegin=" . $time["date"],
    'Right'    => $time_read,
    'Condition'=> array ('index', 'globalview', 'stats', 'admin') //, 'display') 
                                    );

// Validate
// bcontins : obsolete si on auto-valide 
//  $actions["TIME"]["validate"] = array (
//    'Url'      => "$path/time/time_index.php?action=validate",
//    'Right'    => $time_write,
//    'Condition'=> array ('None')
//                                    );

// Validate Admin
  $actions["TIME"]["validate_admin"] = array (
    'Url'      => "$path/time/time_index.php?action=validate_admin",
    'Right'    => $time_admin_write,
    'Condition'=> array ('None') 
                                    );

// General Monthly View
  $actions["TIME"]["globalview"] = array (
    'Name'     => "$l_header_globalview",
    'Url'      => "$path/time/time_index.php?action=globalview",
    'Right'    => $time_admin_write,
    'Condition'=> array ('index', 'viewmonth', 'stats', 'admin') //, 'display') 
                                    );

// Stats by Users
  $actions["TIME"]["stats"] = array (
    'Name'     => "$l_header_stats",
    'Url'      => "$path/time/time_index.php?action=stats",
    'Right'    => $time_admin_read,
    'Condition'=> array ('index', 'viewmonth', 'globalview', 'admin') //, 'display') 
                                     );

// Stats by Month
  $actions["TIME"]["stats_month"] = array (
    'Url'      => "$path/time/time_index.php?action=stats_month",
    'Right'    => $time_admin_read,
    'Condition'=> array ('None') 
                                    );

// Stats by Projects
  $actions["TIME"]["stats_project"] = array (
    'Url'      => "$path/time/time_index.php?action=stats_project",
    'Right'    => $time_admin_read,
    'Condition'=> array ('None') 
                                  );

// Insert 
  $actions["TIME"]["insert"] = array (
    'Url'      => "$path/time/time_index.php?action=insert",
    'Right'    => $time_write,
    'Condition'=> array ('None') 
                                    );

// Show Detail
// obsolete : si on utilise la task_list de olivier
//  $actions["TIME"]["show_detail"] = array (
//    'Url'      => "$path/time/time_index.php?action=show_detail",
//    'Right'    => $time_read,
//    'Condition'=> array ('None') 
//                                    );

// Delete 
  $actions["TIME"]["delete"] = array (
    'Url'      => "$path/time/time_index.php?action=delete",
    'Right'    => $time_write,
    'Condition'=> array ('None') 
                                    );

// Detail Update
  $actions["TIME"]["detailupdate"] = array (
    'Url'      => "$path/time/time_index.php?action=detailupdate",
    'Right'    => $time_write,
    'Condition'=> array ('None') 
                                    );

// Update
  $actions["TIME"]["update"] = array (
    'Url'      => "$path/time/time_index.php?action=update",
    'Right'    => $time_write,
    'Condition'=> array ('None') 
                                    );

// Admin
  $actions["TIME"]["admin"] = array (
    'Name'     => "$l_header_admin",
    'Url'      => "$path/time/time_index.php?action=admin",
    'Right'    => $time_admin_read,
    'Condition'=> array ('all')
                                       );

// Display
//   $actions["TIME"]["display"] = array (
//     'Name'     => "$l_header_display",
//     'Url'      => "$path/time/time_index.php?action=display",
//     'Right'    => $time_read,
//     'Condition'=> array ('all')
//                                        );

}

</SCRIPT>

