<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : agenda_index.php                                             //
//     - Desc : Agenda Index File                                            //
// 2002-11-26 - Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- this week for this user.
// - decision
// - view_day
// - view_week
// - view_month
// - view_year
// - new
// - insert
// - insert_conflict
// - detailconsult
// - detailupdate
// - update
// - update_decision
// - rights_admin    -- access rights screen
// - rights_update   -- Update agenda access rights
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "agenda";
$extra_css = "calendar.css";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
$uid = $auth->auth["uid"];

require("agenda_query.inc");
require("agenda_display.inc");
require("$obminclude/lib/right.inc");
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/javascript/calendar.js.inc");

if ($action == "") $action = "index";
$agenda = get_param_agenda();
get_agenda_action();
$perm->check_permissions($module, $action);

page_close();

$max_display = 6;
// If a group has just been selected, automatically select all its members
if ( ($agenda["new_group"] == "1")
     && ($agenda["group_view"] != "") ) {
  // If group selected is ALL, reset group
  if ($agenda["group_view"] == $c_all) {
    $cal_entity_id["group"] = array();
  } else {
    $cal_entity_id["user"] = get_all_users_from_group($agenda["group_view"]);
    $cal_entity_id["group"] = array($agenda["group_view"]);
  }
  $cal_entity_id["group_view"] = $agenda["group_view"];
  $cal_entity_id["resource"] = array();
}
// If event insert or update, reset the selected group
if (($action == "insert") || ($action == "update")) {
  $cal_entity_id["group_view"] = $c_all;
}
// If no group view selected, explicitely set it
if ($cal_entity_id["group_view"] == "") $cal_entity_id["group_view"] = $c_all;

// If user selection present we override session content
if (($agenda["new_sel"]) || (is_array($agenda["sel_user_id"]))) {
  $cal_entity_id["user"] = $agenda["sel_user_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["user"] = $agenda["sel_user_id"];
}
// If resources selection present we override session content
if (($agenda["new_sel"]) || (is_array($agenda["sel_resource_id"]))) {
  $cal_entity_id["resource"] = $agenda["sel_resource_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["resource"] = $agenda["sel_resource_id"];
}

// If group selection present we override session content
if (is_array($agenda["sel_group_id"])) {
  $cal_entity_id["group"] = $agenda["sel_group_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["group"] = $agenda["sel_group_id"];
}

// If no user or resource selected, we select the connected user
if ( ( (! is_array($cal_entity_id["user"]))
       || (count($cal_entity_id["user"]) == 0) )
     && ( (! is_array($cal_entity_id["resource"]))
	  || (count($cal_entity_id["resource"]) == 0)) ) {
  $cal_entity_id["user"] = array($uid);
}

//print_r($cal_entity_id);
// We copy the entity array structure to the parameter hash
$agenda["entity"] = $cal_entity_id;


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == "calendar") {
    display_head($l_agenda);
    require("$obminclude/calendar.inc");
    display_end();
  } elseif ($action == "export") {
    dis_export_handle($agenda);
  }
  exit();
}


if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_waiting_events($obm_wait);
  } else {
    require("agenda_js.inc");
    $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);
  }

} elseif ($action == "decision") {
///////////////////////////////////////////////////////////////////////////////
  if (!$agenda["force"] && $conflicts = check_for_decision_conflict($agenda)) {
    require("$obminclude/calendar.js");
    require("agenda_js.inc");
    $display["search"] = html_dis_conflict($agenda, $conflicts) ;
    $display["detail"] = html_conflict_form($agenda);
    $display["msg"] .= display_err_msg($l_insert_error);
  } else {
    $conflict = run_query_insert_decision($agenda);
    $obm_wait = run_query_waiting_events();
    if ($obm_wait->nf() != 0) {
      $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
      $display["detail"] = html_waiting_events($obm_wait);
    } else {
      require("agenda_js.inc");
      $display["msg"] .= display_ok_msg($l_update_ok); 
      $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);
    }
  }

} elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_calendar_view($agenda, $cal_entity_id, "day");

} elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_calendar_view($agenda, &$cal_entity_id, "week");

} elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_calendar_view($agenda, $cal_entity_id, "month");

} elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_calendar_view($agenda, $cal_entity_id, "year");

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  require("$obminclude/calendar.js");
  $display["detail"] = dis_event_form($action, $agenda, "", $cal_entity_id);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  if (check_data_form($agenda)) {
    if ( (!$agenda["force"])
	 && ($conflicts = check_for_conflict($agenda, $cal_entity_id)) ) {
      require("$obminclude/calendar.js");
      $display["search"] = html_dis_conflict($agenda,$conflicts) ;
      $display["msg"] .= display_err_msg($l_insert_error);
      $display["detail"] = dis_event_form($action, $agenda, "",$cal_entity_id);
    } else {
      run_query_add_event($agenda, $cal_entity_id, $event_id);
      $display["msg"] .= display_ok_msg($l_insert_ok);
      $agenda["date"] = $agenda["datebegin"];
      $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);
    }
  } else {
    require("$obminclude/calendar.js");
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_event_form($action, $agenda, "", $cal_entity_id);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_event_consult($agenda["id"]);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($agenda["id"] > 0) {  
    require("$obminclude/calendar.js");
    require("agenda_js.inc");
    $eve_q = run_query_detail($agenda["id"]);
    $entities = get_event_entity($agenda["id"]);
    $display["detailInfo"] = display_record_info($eve_q);
    $display["detail"] = dis_event_form($action, $agenda, $eve_q, $entities);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_data_form($agenda)) {
    if ( (!$agenda["force"])
	 && ($conflicts = check_for_conflict($agenda, $cal_entity_id)) ) {
      require("$obminclude/calendar.js");
      require("agenda_js.inc");
      $display["search"] = html_dis_conflict($agenda,$conflicts) ;
      $display["msg"] .= display_err_msg($l_insert_error);
      $display["detail"] = dis_event_form($action, $agenda, "", $cal_entity_id);
    } else {
      run_query_event_update($agenda, $cal_entity_id, $event_id);
      require("agenda_js.inc");
      $display["msg"] .= display_ok_msg($l_update_ok);
      $agenda["date"] = $agenda["datebegin"];
      $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);
    }
  } else {
    require("agenda_js.inc");
    require("$obminclude/calendar.js");    
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_event_form($action, $agenda, "", $cal_entity_id);
  }

} elseif ($action == "update_decision") {
///////////////////////////////////////////////////////////////////////////////
  run_query_update_occurence_state($agenda["id"],$uid,$agenda["decision_event"]);
  require("agenda_js.inc");
  $display["msg"] .= display_ok_msg($l_update_ok);
  $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_agenda($agenda["id"])) {
    if ($agenda["id"] > 0) {
      $display["detail"] = html_dis_delete($agenda);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_event_consult($agenda["id"]);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_can_delete_agenda($agenda["id"])) {
    require("agenda_js.inc");
    if ($agenda["id"] > 0) {
      run_query_delete($agenda);
    }
    $display["detail"] = dis_calendar_view($agenda, $cal_entity_id);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_event_consult($agenda["id"]);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["title"] = "<div class=\"title\">$l_agenda</div>";
  $display["detail"] = of_right_dis_admin("calendar", $agenda["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["title"] = "<div class=\"title\">$l_agenda</div>";
  of_right_update_right($agenda, "calendar");
  $display["msg"] .= display_ok_msg($l_right_update_ok);
  $display["msg"] .= display_warn_msg($err_msg);
  $display["detail"] = of_right_dis_admin("calendar", $agenda["entity_id"]);

} elseif ($action == "new_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  require("$obminclude/calendar.js");
  $display["detail"] = dis_meeting_form($agenda, $cal_entity_id);

} elseif ($action == "perform_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $cal_entity_id["user"] = run_query_get_allusers($cal_entity_id["user"], $agenda["sel_group_id"]);
  $entity_readable = get_entity_readable();
  $entity_store = store_entities(run_query_get_entity_label($cal_entity_id));
  $display["features"] = html_planning_bar($agenda, $cal_entity_id, $entity_store, $entity_readable);
  $display["detail"] = dis_free_interval($agenda, $entity_store);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("calendar", "category1", $agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_insert_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_insert_error");
  }
  require("agenda_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("calendar", "category1", $agenda);
  print_r($agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_update_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_update_error");
  }
  require("agenda_js.inc");
  $display["detail"] .= dis_admin_index();

} elseif ($action == "category1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("calendar", "category1", $agenda);

} elseif ($action == "category1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("calendar", "category1", $agenda);
  if ($retour) {
    $display["msg"] .= display_ok_msg(ucfirst($l_category1)." : $l_c_delete_ok");
  } else {
    $display["msg"] .= display_err_msg(ucfirst($l_category1)." : $l_c_delete_error");
  }
  require("agenda_js.inc");
  $display["detail"] .= dis_admin_index();
}

$sess->register("cal_entity_id");
//echo "<p>";
//print_r($cal_entity_id);
$display["head"] = display_head($l_agenda);
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $agenda hash, Agenda parameters transmited
// returns : $agenda hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_agenda() {
  global $action, $cagenda_first_hour, $cagenda_last_hour;
  global $param_date, $tf_title, $tf_location, $ta_event_description;
  global $param_event, $sel_priority;
  global $tf_date_begin, $sel_time_begin, $sel_min_begin;
  global $tf_date_end, $sel_time_end, $sel_min_end;
  global $sel_repeat_kind,$hd_conflict_end,$hd_old_end,$hd_old_begin;
  global $param_user, $param_group, $new_group, $sel_user_id, $sel_group_id;
  global $sel_resource_id, $sel_ent, $new_sel;
  global $cdg_param,$cb_repeatday_0,$cb_repeatday_1,$cb_repeatday_2,$cb_repeatday_3,$cb_repeatday_4,$cb_repeatday_5;
  global $cb_repeatday_6,$cb_repeatday_7,$tf_repeat_end,$cb_force,$cb_privacy,$cb_repeat_update,$rd_conflict_event;
  global $rd_decision_event,$cb_mail,$param_duration;
  global $sel_time_duration,$sel_min_duration;
  global $sel_group_id;
  global $cb_read_public, $cb_write_public,$sel_accept_write,$sel_accept_read,$param_entity; 
  global $ch_all_day;
  global $tf_category1_label, $tf_category1_code, $sel_category1;
  global $HTTP_POST_VARS, $HTTP_GET_VARS;
  
  // Agenda fields
  if (isset ($param_date)) {
    $agenda["date"] = $param_date; 
  } else { 
    $agenda["date"] = isodate_format();
  }
  if (isset($param_event)) $agenda["id"] = $param_event;
  if (isset($ch_all_day)) $agenda["allday"] = $ch_all_day;
  if (isset($tf_title)) $agenda["title"] = $tf_title;
  if (isset($sel_priority)) $agenda["priority"] = $sel_priority;
  if (isset($ta_event_description)) $agenda["description"] = $ta_event_description;
  if (isset ($tf_category1_label)) $agenda["category1_label"] = $tf_category1_label;
  if (isset ($tf_category1_code)) $agenda["category1_code"] = $tf_category1_code;
  if (isset ($sel_category1)) $agenda["category1"] = $sel_category1;
  if (isset($tf_location)) $agenda["location"] = $tf_location;
  if (isset($cb_force)) $agenda["force"] = $cb_force;
  if (isset($cb_privacy)) $agenda["privacy"] = $cb_privacy;
  if (is_array($rd_conflict_event)) $agenda["conflict_event"] = $rd_conflict_event;
  if (is_array($hd_conflict_end)) $agenda["conflict_end"] = $hd_conflict_end;
  if (isset($hd_old_begin)) $agenda["old_begin"] = $hd_old_begin;
  if (isset($hd_old_end)) $agenda["old_end"] = $hd_old_end;
  if (isset($cb_mail)) $agenda["mail"] = $cb_mail;
  if (isset($new_group)) $agenda["new_group"] = $new_group;
  if (isset($param_group) && ($param_group != "")) {
    $agenda["group_view"] = $param_group;
  }
  if (isset($new_sel)) $agenda["new_sel"] = $new_sel;

  if (isset($sel_time_duration)) {
    $agenda["duration"] = $sel_time_duration;
    if (isset($sel_min_duration)) {
      $agenda["duration"] += $sel_min_duration/60;
    }
  }
  if (isset($param_user)) $agenda["user_id"] = $param_user;
  if (isset($param_duration)) $agenda["duration"] = $param_duration;
  if (isset($tf_repeat_end)) $agenda["repeat_end"] = $tf_repeat_end;
  if (isset($cb_repeat_update)) $agenda["repeat_update"] = 1;

  // Rights parameters
  if (isset($param_entity)) $agenda["entity_id"] = $param_entity;
  if (is_array($sel_accept_write)) $agenda["accept_w"] = $sel_accept_write;
  if (is_array($sel_accept_read)) $agenda["accept_r"] = $sel_accept_read;
  if (isset($cb_write_public)) $agenda["public_w"] = $cb_write_public;
  if (isset($cb_read_public)) $agenda["public_r"] = $cb_read_public;

  if (isset($sel_time_begin)) {
    $start_hour = $sel_time_begin;
  } else {
    $start_hour = $cagenda_first_hour;
  }
  if (isset($sel_min_begin)) {
    $start_min = $sel_min_begin;
  } else {
    $start_min = "00";
  }
  if (isset($sel_time_end)) {
    $end_hour = $sel_time_end;
  } else {
    $end_hour = $cagenda_last_hour;
  }
  if (isset($sel_min_end)) {
    $end_min = $sel_min_end;
  } else {
    $end_min = "00";
  }
  if (isset($tf_date_begin)) $agenda["datebegin"] = $tf_date_begin;
  if (strlen($agenda["datebegin"]) == 10) {
    $agenda["datebegin"] .= " $start_hour:$start_min:00";
  }
  if (isset($tf_date_end)) $agenda["dateend"] = $tf_date_end;
  if (strlen($agenda["dateend"]) == 10) {
    $agenda["dateend"] .= " $end_hour:$end_min:00";
  }
  
  $agenda["event_duration"] =  strtotime($agenda["dateend"]) - strtotime($agenda["datebegin"]);

  if (isset($sel_repeat_kind)) $agenda["kind"] = $sel_repeat_kind;
  for ($i=0; $i<7; $i++) {
    if (isset(${"cb_repeatday_".$i}))  {
      $agenda["repeat_days"] .= '1';
    } else {
      $agenda["repeat_days"] .= '0';
    }
  }

  // sel_group_id can be filled by sel_group_id
  if (is_array($sel_group_id)) {
    while ( list( $key, $value ) = each( $sel_group_id ) ) {
      // sel_group_id contains select infos (data-group-$id)
      if (strcmp(substr($value, 0, 11),"data-group-") == 0) {
	$data = explode("-", $value);
	$id = $data[2];
	$agenda["sel_group_id"][] = $id;
      } else {
	// direct id
	$agenda["sel_group_id"][] = $value;
      }
    }
  }

  // sel_user_id can be filled by sel_user_id or sel_ent (see below)
  if (is_array($sel_user_id)) {
    while ( list( $key, $value ) = each( $sel_user_id ) ) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),"data-user-") == 0) {
	$data = explode("-", $value);
	$id = $data[2];
	$agenda["sel_user_id"][] = $id;
      } else {
	// direct id
	$agenda["sel_user_id"][] = $value;
      }
    }
  }

  // sel_resource_id can be filled by sel_resource_id or sel_ent (see below)
  if (is_array($sel_resource_id)) {
    while ( list( $key, $value ) = each( $sel_resource_id ) ) {
      // sel_resource_id contains select infos (data-resource-$id)
      if (strcmp(substr($value, 0, 14),"data-resource-") == 0) {
	$data = explode("-", $value);
	$id = $data[2];
	$agenda["sel_resource_id"][] = $id;
      } else {
	// direct id
	$agenda["sel_resource_id"][] = $value;
      }
    }
  }

  if ((is_array ($HTTP_POST_VARS)) && (count($HTTP_POST_VARS) > 0)) {
    $http_obm_vars = $HTTP_POST_VARS;
  } elseif ((is_array ($HTTP_GET_VARS)) && (count($HTTP_GET_VARS) > 0)) {
    $http_obm_vars = $HTTP_GET_VARS;
  }

  if (isset ($http_obm_vars)) {
    //    print_r($http_obm_vars["sel_ent"]);
    if (is_array($http_obm_vars["sel_ent"])) {
      $nb_data = 0;
      $nb["user"] = 0;
      $nb["resource"] = 0;
      while ( list( $key, $value ) = each( $http_obm_vars["sel_ent"] ) ) {
	if (strcmp(substr($value, 0, 5),"data-") == 0) {
	  $nb_data++;
	  $data = explode("-", $value);
	  $ent = $data[1];
	  $id = $data[2];
	  $nb[$ent]++;
	  $agenda["sel_${ent}_id"][] = $id;
	}
      }
    }
  }

  if (isset($rd_decision_event)) $agenda["decision_event"] = $rd_decision_event;
  if (debug_level_isset($cdg_param)) {
    if ( $agenda ) {
      while ( list( $key, $val ) = each( $agenda ) ) {
        echo "<br />agenda[$key]=";
	var_dump($val);
      }
    }
    echo "<br />action = $action";
  }

  return $agenda;
}


///////////////////////////////////////////////////////////////////////////////
//  Agenda Action 
///////////////////////////////////////////////////////////////////////////////
function get_agenda_action() {
  global $actions, $path, $agenda;
  global $l_header_consult, $l_header_update,$l_header_right,$l_header_meeting;
  global $l_header_day,$l_header_week,$l_header_year,$l_header_delete;
  global $l_header_month,$l_header_new_event,$l_header_admin, $l_header_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $agenda["id"];
  $date = $agenda["date"];

  // Index
  $actions["agenda"]["index"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  
  // Decision
  $actions["agenda"]["decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=decision",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );

  // Decision
  $actions["agenda"]["calendar"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=calendar",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );
  // New   
  $actions["agenda"]["new"] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/agenda/agenda_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index','detailconsult','insert','insert_conflict',
       'update_decision','decision','update','delete', 'new_meeting',
       'view_month','view_week','view_day','view_year',
		  'rights_admin','rights_update')
		);

  // Detail Update
  $actions["agenda"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/agenda/agenda_index.php?action=detailconsult&amp;param_event=$id&amp;param_date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );

  // Detail Update
  $actions["agenda"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/agenda/agenda_index.php?action=detailupdate&amp;param_event=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Check Delete
  $actions["agenda"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=check_delete&amp;param_event=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult')
                                     		 );

  // Delete
  $actions["agenda"]["delete"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=delete&amp;param_event=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     		 );

  // Insert
  $actions["agenda"]["insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // View Year
  $actions["agenda"]["view_year"] = array (
    'Name'     => $l_header_year,
    'Url'      => "$path/agenda/agenda_index.php?action=view_year",
    'Right'    => $cright_read,  
    'Condition'=> array ('all') 
                                    	    );

  // View Month
  $actions["agenda"]["view_month"] = array (
    'Name'     => $l_header_month,
    'Url'      => "$path/agenda/agenda_index.php?action=view_month",
    'Right'    => $cright_read,  
    'Condition'=> array ('all') 
                                    	    );

  // View Week
  $actions["agenda"]["view_week"] = array (
    'Name'     => $l_header_week,
    'Url'      => "$path/agenda/agenda_index.php?action=view_week",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                    	  );

  // View Day
  $actions["agenda"]["view_day"] = array (
    'Name'     => $l_header_day,
    'Url'      => "$path/agenda/agenda_index.php?action=view_day",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                    	 );

  // Update
  $actions["agenda"]["update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );
					 
  // Update
  $actions["agenda"]["update_decision"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );
					 
  // New meeting
  $actions["agenda"]["new_meeting"] = array (
    'Name'     => $l_header_meeting,
    'Url'      => "$path/agenda/agenda_index.php?action=new_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );

  // Search meeting
  $actions["agenda"]["perform_meeting"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=perform_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // Right admin.					 
  $actions["agenda"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/agenda/agenda_index.php?action=rights_admin",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );

  // Update Right
  $actions["agenda"]["rights_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=rights_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

// Admin
  $actions["agenda"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/agenda/agenda_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );
				       
// Kind Insert
  $actions["agenda"]["category1_insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["agenda"]["category1_update"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["agenda"]["category1_checklink"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["agenda"]["category1_delete"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=category_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
					       
// Export
  $actions["agenda"]["export"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/agenda/agenda_index.php?action=export&amp;popup=1",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

}
  

?>
