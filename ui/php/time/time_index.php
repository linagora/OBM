<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : time_index.php                                               //
//     - Desc : Time management Index File                                   //
// 2002-04-01 Pierre Carlier                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- show the task week list and the task 
//					  insert form.
// - search          -- search fields  -- show the task week list and the task 
//					  insert form.
// - insert          -- form fields    -- insert the task
// - detailupdate    -- $task_id       -- show the update task form in a popup
// - update          -- form fields    -- update the task
// - delete          -- $params        -- delete the tasks
// - validate        --                -- validate the daily task
// - validate_admin		       -- diplay the time day result.
// - show_details    -- form fields  -- show or not details when choice exists
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms  Management                                            //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "USERS";
$menu = "TIME";

$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/phplib/obmlib.inc");
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("user");
include("$obminclude/global_pref.inc");

require("../agenda/agenda_functions.inc");
require("time_display.inc");
require("time_query.inc");

$time = get_param_time();

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

require("time_js.inc");
get_time_actions();
///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
$project_managers = array( '6' , '7' , '8','23' ) ;
$stats_users = array( '6' , '7' , '8','23' ) ;
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
display_head($l_time);     // Head & Body
if (! $popup) {
  generate_menu($menu,$section);         // Menu
  display_bookmarks();
}

///////////////////////////////////////////////////////////////////////////////
//Initialisation                                                             //
///////////////////////////////////////////////////////////////////////////////
//set user_id if not set
if ( ! isset($time["user_id"]) )
  $time["user_id"] = $uid;

//set $show_task_detail flag
// ?? THIS CAN BE A USER PREFERENCE ??
if( ! isset($time["show_task_detail"]) )
  $time["show_task_detail"] = true;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "search" || $action == "") {
//////////////////////////////////////////////////////////////////////////////
	require("time_js.inc");
  // interval is week -- see if we may need to use other intervals
  $time["interval"] = "week";

  // USERS : only one for index (and others, except stats)
  if (! in_array($u_id, $project_managers))
	$time["user_id"] == array($u_id);
  else if (sizeof($time["user_id"]) > 1) {
	$tt = $time["user_id"];
	$time["user_id"] == $tt[0];
  }
  else if (sizeof($time["user_id"]) == 0) {
    echo "erreur \$time[user_id] vide !!! <br>";
	$time["user_id"] == array($u_id);
  }

  // display links to previous and next week
  dis_time_links($time,"week");

  // run_query_contactid_user
  //  -- THIS SHOULD CHANGE TO USE THE User TABLE --

  // display user Search Form
  dis_time_search_form($time, 
                       run_query_get_obmusers(),
                       $uid);

  
  dis_time_list($time);  // INDEX SEARCH
} 
elseif ($action == "show_details") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "week";

  // Setting show_task_detail checkbox
  if (isset($show_task_detail)) 
	 $time["show_task_detail"] = true;
  else
	 $time["show_task_detail"] = false;

  dis_time_links($time,"week");
  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);
  //run_query_delete($HTTP_POST_VARS);
    dis_time_list($time);  // SHOW DETAILS
}

elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals

  $time["interval"] = "week";
  dis_time_links($time,"week");
  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);

  run_query_insert($time);
  dis_time_list($time);  // INSERT

}
elseif ($action == "validate") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals

  $time["interval"] = "week";
  dis_time_links($time,"week");
  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);

  run_query_validate($HTTP_POST_VARS,$time,0);

    dis_time_list($time);  // VALIDATE
}

elseif ($action == "validate_admin") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals

  $time["interval"] = "week";
  dis_time_links($time,"week");
  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);

  run_query_validate($HTTP_POST_VARS,$time,1);
  dis_time_list($time); // VALIDATE_ADMIN


}
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
elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time["interval"] = "week";
 
  dis_time_links($time,"week");
  dis_time_search_form($time, 
      run_query_get_obmusers(),
      $uid);

  run_query_delete($HTTP_POST_VARS);

  dis_time_list($time);
}

elseif ($action == "detailupdate") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  if ( $popup == 1 ) {
    // Get elements for insertion of new task
    // TaskType
    $obm_tasktype_q = run_query_tasktype();
    // Project
    $obm_project_q = run_query_project();  
    //get the current start and end of week
    $d_start_week = first_day_week($time);  
    // Creating the dates for the selected (or current) date
    $day_q = get_this_week($d_start_week, 5); 

    // array of validated days
    $val_days = run_query_valid_search($time);

    dis_form_addtask($action, $obm_project_q, $obm_tasktype_q, $day_q, $c_day_fraction, $time, $val_days);  

  }
}

elseif ($action == "update") {
//////////////////////////////////////////////////////////////////////////////
  run_query_update($time);
  //  $user_id = $time["user_id"][0];
  echo "
    <Script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&param_begin=".
     $param_begin."&param_end=".$param_end."';
     window.close();
    </script>
    ";
  //     $param_begin."&param_end=".$param_end."&user_id=".$user_id."';

}   

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();


///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_time() {
  // Forms parameters
  global $sel_date, $sel_tasktype, $sel_project, $sel_time, $tf_label, $task_id;

  global $tf_lweek, $f_time, $sel_user_id, $user_id, $cb_day, $action, $cb_allusers;
  global $st_detail;

  // URLs parameters
  global $param_begin,$param_end;
  global $set_debug, $cdg_param;

  //  echo "get_param_time() : show_task_detail $st_detail <br>";
  //$task["datett"] = "toto";
  if (isset ($action)) $task["action"] = $action;
  if (isset ($sel_tasktype)) $task["tasktype"] = $sel_tasktype;
  if (isset ($sel_project)) $task["project"] = $sel_project;
  if (isset ($sel_time)) $task["time"] = $sel_time;
  if (isset ($tf_label)) $task["label"] = $tf_label;
  if (isset ($tf_date)) $task["date"] = $tf_date;
  if (isset ($f_time)) $task["f_time"] = $f_time;
  if (isset ($sel_user_id)) $task["user_id"] = $sel_user_id;
  if (isset ($user_id)) $task["user_id"] = $user_id;
  if (isset ($cb_allusers)) $task["allusers"] = $cb_allusers;
  if (isset ($submit)) $task["submit"] = $submit;
  if (isset ($param_begin)) $task["date"] = $param_begin;
  if (isset ($param_end)) $task["date_end"] = $param_end;
  if (isset ($task_id)) $task["task_id"] = $task_id;
  if (! empty($st_detail)) 
     $task["show_task_detail"] = true;
  else
     $task["show_task_detail"] = false;
  if (is_array($cb_day)) $task["sel_date"] = $cb_day;
  elseif (isset ($sel_date)) $task["sel_date"] = $sel_date;

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
global $actions;
//Index

  $actions["TIME"]["index"] = array (
    'Url'      => "$path/time/time_index.php?action=index",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Search

  $actions["TIME"]["search"] = array (
    'Url'      => "$path/time/time_index.php?action=search",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Validate
  $actions["TIME"]["validate"] = array (
    'Url'      => "$path/time/time_index.php?action=validate",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Validate Admin

  $actions["TIME"]["validate_admin"] = array (
    'Url'      => "$path/time/time_index.php?action=validate_admin",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Stats Update

  $actions["TIME"]["stats"] = array (
    'Url'      => "$path/time/time_index.php?action=stats",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Detail Update

  $actions["TIME"]["detailupdate"] = array (
    'Url'      => "$path/time/time_index.php?action=detailupdate",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );

//Update

  $actions["TIME"]["update"] = array (
    'Url'      => "$path/time/time_index.php?action=update",
    'Right'    => $settings_read,
    'Condition'=> array ('None') 
                                    );


}

</SCRIPT>

