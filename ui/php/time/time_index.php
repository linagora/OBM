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

$uid = $auth->auth["uid"]; //current user uid

page_close();

require("time_js.inc");

///////////////////////////////////////////////////////////////////////////////
//perms for manage task ??? To update when access rights model will change
$project_managers = array( '6' , '7' , '8','23' ) ;
///////////////////////////////////////////////////////////////////////////////

$time = get_param_time();

if (debug_level_isset($cdg_param)) {
  if ( $time ) {
    while ( list( $key, $val ) = each( $time ) ) {
      echo "<BR>task[$key]=$val";
    }
  }
  echo "<br>HTTP_POST_VARS<br>";
  foreach($HTTP_POST_VARS as $key => $val) {
    echo "clé $key val $val <br>";
  }
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

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "search" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time['interval'] = "week";
  html_time_links($time,"week");
  html_time_search_form($time, run_query_userobm(), $uid);
  dis_time_list($time);
} 
elseif ($action == "insert") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time['interval'] = "week";
  html_time_links($time,"week");
  html_time_search_form($time, run_query_userobm(), $uid);
  run_query_insert($time);
  dis_time_list($time);

}
elseif ($action == "validate") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time['interval'] = "week";
  html_time_links($time,"week");
  html_time_search_form($time, run_query_userobm(), $uid);
  run_query_validate($HTTP_POST_VARS,$time,0);
  dis_time_list($time);

}
elseif ($action == "validate_admin") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time['interval'] = "week";
  html_time_links($time,"week");
  html_time_search_form($time, run_query_userobm(), $uid);
  run_query_validate($HTTP_POST_VARS,$time,1);
  dis_time_list($time);


}
elseif ($action == "delete") {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $time['interval'] = "week";
 
  html_time_links($time,"week");
  html_time_search_form($time, run_query_userobm(), $uid);
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
    dis_form_addtask($action, $obm_project_q, $obm_tasktype_q, $day_q, $fraction_jour, $time);  
  }
}

elseif ($action == "update") {
//////////////////////////////////////////////////////////////////////////////
  run_query_update($time);
  echo "
    <Script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&param_begin=".
     $param_begin."&param_end=".$param_end."&user_id=".$user_id."';
     window.close();
    </script>
    ";
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
  global $tf_lweek, $f_time, $sel_user_id, $user_id, $cb_day;
  // URLs parameters
  global $param_begin,$param_end;
  global $set_debug, $cdg_param;

  //$task["datett"] = "toto";
  if (isset ($sel_tasktype)) $task["tasktype"] = $sel_tasktype;
  if (isset ($sel_project)) $task["project"] = $sel_project;
  if (isset ($sel_time)) $task["time"] = $sel_time;
  if (isset ($tf_label)) $task["label"] = $tf_label;
  if (isset ($tf_date)) $task["date"] = $tf_date;
  if (isset ($f_time)) $task["f_time"] = $f_time;
  if (isset ($sel_user_id)) $task["user_id"] = $sel_user_id;
  if (isset ($user_id)) $task["user_id"] = $user_id;
  if (isset ($submit)) $task["submit"] = $submit;
  if (isset ($param_begin)) $task["date"] = $param_begin;
  if (isset ($param_end)) $task["date_end"] = $param_end;
  if (isset ($task_id)) $task["task_id"] = $task_id;
  if (is_array($cb_day)) $task["sel_date"] = $cb_day;
  elseif (isset ($sel_date)) $task["sel_date"] = $sel_date;

  if (debug_level_isset($cdg_param)) {
    global $action;
    echo "<br>action=$action";
    if ( $task ) {
      while ( list( $key, $val ) = each( $task ) ) {
        echo "<br>task[$key]=$val";
      }
    }
  }

  return $task;
}

</SCRIPT>

