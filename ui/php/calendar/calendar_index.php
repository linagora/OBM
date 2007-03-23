<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : calendar_index.php                                           //
//     - Desc : Calendar Index File                                          //
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
// - rights_update   -- Update calendar access rights
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$module = "calendar";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_calendar_params();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

$extra_css[] = $css_calendar;
$extra_js_include[] = "calendar.js";

require("calendar_query.inc");
require("calendar_display.inc");
require_once("calendar_js.inc");
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");

get_calendar_action();
update_calendar_action();
$perm->check_permissions($module, $action);

$cal_entity_id = $_SESSION['cal_entity_id'];

page_close();
// If a group has just been selected, automatically select all its members
if ( ($params["new_group"] == "1")
     && ($params["group_view"] != "") ) {
  // If group selected is ALL, reset group
  if ($params["group_view"] == $c_all) {
    $cal_entity_id["group"] = array();
  } else {
    $cal_entity_id["user"] = get_all_users_id_from_group($params["group_view"]);
    $cal_entity_id["group"] = array($params["group_view"]);
  }
  $cal_entity_id["group_view"] = $params["group_view"];
  $cal_entity_id["resource"] = array();
}
// Resources groups, only on meeting
if ($action == "perform_meeting" && 
  (is_array($params["sel_resource_group_id"]) || is_array($params["sel_user_id"])
  || is_array($params["sel_resource_id"]))) { 
  $cal_entity_id["resource_group"] = $params["sel_resource_group_id"];
  $cal_entity_id["user"] = $params["sel_user_id"];
  $cal_entity_id["resource"] = $params["sel_resource_id"];    
} else if ($action != "perform_meeting") {
  unset($cal_entity_id["resource_group"]);
}
// If event insert or update, reset the selected group
if (($action == "insert") || ($action == "update")) {
  $cal_entity_id["group_view"] = $c_all;
}
// If no group view selected, explicitely set it
if ($cal_entity_id["group_view"] == "") $cal_entity_id["group_view"] = $c_all;

// If user selection present we override session content
if (($params["new_sel"]) || (is_array($params["sel_user_id"]))) {
  $cal_entity_id["user"] = $params["sel_user_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["user"] = $params["sel_user_id"];
}
// If resources selection present we override session content
if ($action == "new" && (is_array($params["sel_resource_id"]))) {
  // Join resources from group with normal resources.
  $cal_entity_id["resource"] = array_merge($params["sel_resource_id"],$cal_entity_id["resource"]);
} else if (($params["new_sel"]) || (is_array($params["sel_resource_id"]))) {
  $cal_entity_id["resource"] = $params["sel_resource_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["resource"] = $params["sel_resource_id"];
}

// If group selection present we override session content
if (is_array($params["sel_group_id"])) {
  $cal_entity_id["group"] = $params["sel_group_id"];
} else if (($action == "insert") || ($action == "update")) {
  // If event creation (form submission) we set session even if selection empty
  $cal_entity_id["group"] = $params["sel_group_id"];
}

// If no user or resource selected, we select the connected user
if ( ( (! is_array($cal_entity_id["user"]))
       || (count($cal_entity_id["user"]) == 0) )
     && ( (! is_array($cal_entity_id["resource"]))
	  || (count($cal_entity_id["resource"]) == 0)) ) {
  $cal_entity_id["user"] = array($obm["uid"]);
}

//print_r($cal_entity_id);
// We copy the entity array structure to the parameter hash
$params["entity"] = $cal_entity_id;


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == "calendar") {
    display_head($l_calendar);
    display_end();
  } elseif ($action == "export") {
    dis_calendar_export_handle($params);
  }
  exit();
}


if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_calendar_waiting_events($obm_wait);
  } else {
    $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
  }

} elseif ($action == "waiting_events") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_calendar_waiting_events($obm_wait);
  } else {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
  }

} elseif ($action == "decision") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["force"] && $conflicts = check_calendar_decision_conflict($params)) {
    $display["search"] = html_calendar_dis_conflict($params, $conflicts) ;
    $display["detail"] = html_calendar_conflict_form($params);
    $display["msg"] .= display_err_msg("$l_event : $l_insert_error");
  } else {
    $conflict = run_query_calendar_insert_decision($params);
    $obm_wait = run_query_calendar_waiting_events();
    if ($obm_wait->nf() != 0) {
      $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
      $display["detail"] = html_calendar_waiting_events($obm_wait);
    } else {
      $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
      $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
    }
  }

} elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id, "day");

} elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_calendar_view($params, &$cal_entity_id, "week");

} elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id, "month");

} elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id, "year");

} elseif ($action == "view_list") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id, "list");

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_event_form($action, $params, "", $cal_entity_id);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_form($params)) {
    if ( (!$params["force"])
	 && ($conflicts = check_calendar_conflict($params, $cal_entity_id)) ) {
      $display["search"] = html_calendar_dis_conflict($params,$conflicts) ;
      $display["msg"] .= display_err_msg("$l_event : $l_insert_error");
      $display["detail"] = dis_calendar_event_form($action, $params, "",$cal_entity_id);
    } else {
      run_query_calendar_add_event($params, $cal_entity_id, $event_id);
      $display["msg"] .= display_ok_msg("$l_event : $l_insert_ok");
      $params["date"] = $params["date_begin"];
      $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
    }
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err["msg"]);
    $display["detail"] = dis_calendar_event_form($action, $params, "", $cal_entity_id);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_event_consult($params["calendar_id"]);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["calendar_id"] > 0) {  
    $eve_q = run_query_calendar_detail($params["calendar_id"]);
    $entities = get_calendar_event_entity($params["calendar_id"]);
    $display["detailInfo"] = display_record_info($eve_q);
    $display["detail"] = dis_calendar_event_form($action, $params, $eve_q, $entities);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_form($params)) {
    if ( (!$params["force"])
	 && ($conflicts = check_calendar_conflict($params, $cal_entity_id)) ) {
      $display["search"] = html_calendar_dis_conflict($params,$conflicts) ;
      $display["msg"] .= display_err_msg("$l_event : $l_update_error");
      $display["detail"] = dis_calendar_event_form($action, $params, "", $cal_entity_id);
    } else {
      run_query_calendar_event_update($params, $cal_entity_id, $event_id);
      $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
      $params["date"] = $params["date_begin"];
      $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
    }
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err["msg"]);
    $display["detail"] = dis_calendar_event_form($action, $params, "", $cal_entity_id);
  }

} elseif ($action == "quick_update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_quick_form($params)) {
    $id = $params["calendar_id"];
    $eve_q = run_query_calendar_detail($id);
    if ($obm["uid"] != $eve_q->f("calendarevent_owner")) {
      $writable = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write",array($eve_q->f("calendarevent_owner")));
      if(count($writable["ids"]) != 1) {
        json_error_msg($l_invalid_data . " : " . $l_rights );
        echo "({".$display['json']."})";
        exit();
      }
    }
    if($eve_q->f('calendarevent_repeatkind') == 'none') {
      run_query_calendar_quick_event_update($params);
    } else {
      $id = run_query_calendar_event_exception_insert($params,$eve_q);
    }
    json_event_data($id,$params);
    json_ok_msg("$l_event : $l_update_ok");
    echo "({".$display['json']."})";
    exit();
  } else {
    json_error_msg($l_invalid_data . " : " . $err["msg"]);
    echo "({".$display['json']."})";
    exit();
  }

} elseif ($action == "quick_insert") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_quick_form($params)) {
    $id = run_query_calendar_quick_event_insert($params, $cal_entity_id);
    json_ok_msg("$l_event : $l_insert_ok");
    json_event_data($id, $params);
    echo "({".$display['json']."})";
    exit();
  } else {
    json_error_msg($l_invalid_data . " : " . $err["msg"]);
    echo "({".$display['json']."})";
    exit();
  }

} elseif ($action == "quick_delete") {  
///////////////////////////////////////////////////////////////////////////////
  $id = $params["calendar_id"];
  if (check_calendar_can_delete($id)) {
    $eve_q = run_query_calendar_detail($id);    
    if ($obm["uid"] != $eve_q->f("calendarevent_owner")) {
      $writable = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write",array($eve_q->f("calendarevent_owner")));
      if(count($writable["ids"]) != 1) {
        json_error_msg($l_invalid_data . " : " . $l_rights );
        echo "({".$display['json']."})";
        exit();
      }    
    }    
    json_event_data($id,$params);
    if($eve_q->f('calendarevent_repeatkind') == 'none') {      
      run_query_calendar_delete($params);
      json_ok_msg("$l_event : $l_delete_ok");
      echo "({".$display['json']."})";
      exit();      
    } else {
      run_query_calendar_event_exception_insert($params);
      json_ok_msg("$l_event : $l_delete_ok");
      echo "({".$display['json']."})";
      exit();            
    }
  } else {
    json_error_msg($l_invalid_data);
    echo "({".$display['json']."})";    
  }

} elseif ($action == "update_decision") {
///////////////////////////////////////////////////////////////////////////////
  run_query_calendar_update_occurence_state($params["calendar_id"], $obm["uid"],$params["decision_event"]);
  $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
  $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_can_delete($params["calendar_id"])) {
    if ($params["calendar_id"] > 0) {
      $display["detail"] = html_calendar_dis_delete($params);
    }
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_calendar_event_consult($params["calendar_id"]);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_can_delete($params["calendar_id"])) {
    if ($params["calendar_id"] > 0) {
      run_query_calendar_delete($params);
    }
    $display["detail"] = dis_calendar_calendar_view($params, $cal_entity_id);
  } else {
    $display["msg"] .= display_warn_msg($err["msg"], false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_calendar_event_consult($params["calendar_id"]);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_right_dis_admin($params["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  if (of_right_update_right($params, "calendar")) {
    $display["msg"] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display["msg"] .= display_warn_msg($err["msg"]);
  }
  $display["detail"] = dis_calendar_right_dis_admin($params["entity_id"]);

} elseif ($action == "new_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_meeting_form($params, $cal_entity_id);

} elseif ($action == "perform_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  $cal_entity_id["user"] = run_query_calendar_get_allusers($cal_entity_id["user"], $params["sel_group_id"]);
  $entity_readable = get_calendar_entity_readable();
  $ret = run_query_calendar_get_entity_label($cal_entity_id);
  $ret["resourcegroup"] = run_query_resource_resourcegroup($cal_entity_id["resource_group"]);
  $entity_store = store_calendar_entities($ret);
  $display["features"] = html_calendar_planning_bar($params, $cal_entity_id, $entity_store, $entity_readable);
  $display["detail"] = dis_calendar_free_interval($params, $entity_store);

} elseif ($action == "planning") {
///////////////////////////////////////////////////////////////////////////////
  $entity_readable = get_calendar_entity_readable();
  $cal_entity_id["group_view"] =  $params["entity"]["group_view"];
  $calendar_entity = store_calendar_entities(run_query_calendar_get_entity_label($cal_entity_id));
  $display["features"] = html_calendar_planning_bar($params, $calendar_entity, $entity_readable);
  $display["detail"] = dis_calendar_plain_month_planning($params, $calendar_entity);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_calendar_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("calendar", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $display["detail"] .= dis_calendar_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("calendar", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $display["detail"] .= dis_calendar_admin_index();

} elseif ($action == "category1_checklink")  {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] .= of_category_dis_links("calendar", "category1", $params, "mono");

} elseif ($action == "category1_delete")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete("calendar", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_delete_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_delete_error");
  }
  $display["detail"] .= dis_calendar_admin_index();
}

$_SESSION['cal_entity_id'] = $cal_entity_id;
//echo "<p>";
//print_r($cal_entity_id);
if (!$params["ajax"]) {
  $display["head"] = display_head($l_calendar);
  $display["header"] = display_menu($module);
  $display["end"] = display_end();
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Calendar parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_calendar_params() {
  global $ccalendar_first_hour, $ccalendar_last_hour;
  
  // Get global params
  $params = get_global_params("Entity");

  // Get calendar specific params
  if ($params["group_view"] == "") {
    $params["group_view"] = $params["group_id"];
  }

  if (! isset ($params["date"])) {
    $params["date"] = of_isodate_format();
  }
  
  // New meeting event duration
  if (isset($params["time_duration"])) {
    $params["meeting_duration"] = $params["time_duration"];
    if (isset($params["min_duration"])) {
      $params["meeting_duration"] += $params["min_duration"]/60;
    }
  }

  // New appointment hours
  if (isset($params["time_begin"])) {
    $start_hour = $params["time_begin"];
  } else {
    $start_hour = $ccalendar_first_hour;
  }
  if (isset($params["min_begin"])) {
    $start_min = $params["min_begin"];
  } else {
    $start_min = "00";
  }
  if (isset($params["time_end"])) {
    $end_hour = $params["time_end"];
  } else {
    $end_hour = $ccalendar_last_hour;
  }
  if (isset($params["min_end"])) {
    $end_min = $params["min_end"];
  } else {
    $end_min = "00";
  }
  if (strlen($params["date_begin"]) == 10) {
    $params["date_begin"] .= " $start_hour:$start_min:00";
  }
  if (strlen($params["date_end"]) == 10) {
    $params["date_end"] .= " $end_hour:$end_min:00";
  }
  
  if (($params["date_end"] != "") && ($params["date_begin"] != "")) {
    $params["event_duration"] = strtotime($params["date_end"]) - strtotime($params["date_begin"]);
  } else {
    $params["event_duration"] = 0;
  }
  if (($params["date_begin"]) && $params["date_end"] == "" && $params["duration"]) {
    $params["date_end"] = date("Y-m-d H:i:s",strtotime($params["date_begin"]) + $params["duration"]);
  } 
  // repeat days
  for ($i=0; $i<7; $i++) {
    if (isset($params["repeatday_$i"])) {
      $params["repeat_days"] .= '1';
    } else {
      $params["repeat_days"] .= '0';
    }
  }

  // sel_group_id can be filled by sel_group_id
  if (is_array($params["group_id"])) {
    while (list($key, $value) = each($params["group_id"]) ) {
      // sel_group_id contains select infos (data-group-$id)
      if (strcmp(substr($value, 0, 11),"data-group-") == 0) {
        $data = explode("-", $value);
        $id = $data[2];
        $params["sel_group_id"][] = $id;
      } else {
        // direct id
        $params["sel_group_id"][] = $value;
      }
    }
  }

  // sel_user_id can be filled by sel_user_id or sel_ent (see below)
  if (is_array($params["user_id"])) {
    while (list($key, $value) = each($params["user_id"]) ) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),"data-user-") == 0) {
        $data = explode("-", $value);
        $id = $data[2];
        $params["sel_user_id"][] = $id;
      } else {
        // direct id
        $params["sel_user_id"][] = $value;
      }
    }
  }

  // sel_resource_id can be filled by sel_resource_id or sel_ent (see below)
  if (is_array($params["resource_id"])) {
    while (list($key, $value) = each($params["resource_id"]) ) {
      // sel_resource_id contains select infos (data-resource-$id)
      if (strcmp(substr($value, 0, 14),"data-resource-") == 0) {
        $data = explode("-", $value);
        $id = $data[2];
        $params["sel_resource_id"][] = $id;
      } else {
        // direct id
        $params["sel_resource_id"][] = $value;
      }
    }
  }
  // sel_resource_id can be filled by sel_resource_id or sel_ent (see below)
  if (is_array($params["resource_group_id"])) {
    while (list($key, $value) = each($params["resource_group_id"]) ) {
      // sel_resource_id contains select infos (data-resource-$id)
      if (strcmp(substr($value, 0, 19),"data-resourcegroup-") == 0) {
        $data = explode("-", $value);
        $id = $data[2];
        $params["sel_resource_group_id"][] = $id;
      } else {
        // direct id
        $params["sel_resource_group_id"][] = $value;
      }
    }
  }  
  // feature params (user & resource)
  if (is_array($params["ent"])) {
    $nb_data = 0;
    $nb["user"] = 0;
    $nb["resource"] = 0;
    while(list($key,$value ) = each($params["ent"])) {
      if (strcmp(substr($value, 0, 5),"data-") == 0) {
        $nb_data++;
        $data = explode("-", $value);
        $ent = $data[1];
        $id = $data[2];
        $nb[$ent]++;
        $params["sel_${ent}_id"][] = $id;
      }
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Calendar Action 
///////////////////////////////////////////////////////////////////////////////
function get_calendar_action() {
  global $actions, $path, $params;
  global $l_header_consult, $l_header_update,$l_header_right,$l_header_meeting;
  global $l_header_day,$l_header_week,$l_header_year,$l_header_delete;
  global $l_header_planning, $l_header_list;
  global $l_header_month,$l_header_new_event,$l_header_admin, $l_header_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_waiting_events;

  $id = $params["calendar_id"];
  $date = $params["date"];

  // Index
  $actions["calendar"]["index"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  
  // Decision
  $actions["calendar"]["decision"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=decision",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );

  // Decision
  $actions["calendar"]["calendar"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=calendar",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                         );
  // New   
  $actions["calendar"]["new"] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/calendar/calendar_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index','detailconsult','insert','insert_conflict',
       'update_decision','decision','update','delete', 'new_meeting',
       'view_month','view_week','view_day','view_year', 'view_list',
       'rights_admin','rights_update', 'waiting_events',"planning")
		);

  // Detail Consult
  $actions["calendar"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=$id&amp;date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );

  // Detail Update
  $actions["calendar"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/calendar/calendar_index.php?action=detailupdate&amp;calendar_id=$id&amp;date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Check Delete
  $actions["calendar"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/calendar/calendar_index.php?action=check_delete&amp;calendar_id=$id&amp;date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult')
                                     		 );

  // Delete
  $actions["calendar"]["delete"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=delete&amp;calendar_id=$id&amp;date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     		 );

  // Insert
  $actions["calendar"]["insert"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Planning
  $actions["calendar"]["planning"] = array (
    'Name'     => $l_header_planning,
    'Url'      => "$path/calendar/calendar_index.php?action=planning&amp;date=$date",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
                                    	 );
  // View Year
/*  $actions["calendar"]["view_year"] = array (
    'Name'     => $l_header_year,
    'Url'      => "$path/calendar/calendar_index.php?action=view_year&amp;date=$date",
    'Right'    => $cright_read,  
    'Condition'=> array ('all') 
  );*/
  // View Month
  $actions["calendar"]["view_month"] = array (
    'Name'     => $l_header_month,
    'Url'      => "$path/calendar/calendar_index.php?action=view_month&amp;date",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	    );

  // View Week
  $actions["calendar"]["view_week"] = array (
    'Name'     => $l_header_week,
    'Url'      => "$path/calendar/calendar_index.php?action=view_week&amp;date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	  );

  // View Day
  $actions["calendar"]["view_day"] = array (
    'Name'     => $l_header_day,
    'Url'      => "$path/calendar/calendar_index.php?action=view_day&amp;date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	 );

  // View List
  $actions["calendar"]["view_list"] = array (
    'Name'     => $l_header_list,
    'Url'      => "$path/calendar/calendar_index.php?action=view_list&amp;date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    	 );

  // Update
  $actions["calendar"]["update"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // Update
  $actions["calendar"]["quick_update"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Quick Insert
  $actions["calendar"]["quick_insert"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Quick Delete
  $actions["calendar"]["quick_delete"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Update
  $actions["calendar"]["update_decision"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // Waiting events
  $actions["calendar"]["waiting_events"] = array (
    'Name'     => $l_header_waiting_events,
    'Url'      => "$path/calendar/calendar_index.php?action=waiting_events",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
                                         );
					 
  // New meeting
  $actions["calendar"]["new_meeting"] = array (
    'Name'     => $l_header_meeting,
    'Url'      => "$path/calendar/calendar_index.php?action=new_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );
   
  // Search meeting
  $actions["calendar"]["perform_meeting"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=perform_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

  // Right admin
  $actions["calendar"]["rights_admin"] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/calendar/calendar_index.php?action=rights_admin",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
                                         );

  // Update Right
  $actions["calendar"]["rights_update"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=rights_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                         );

// Admin
  $actions["calendar"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/calendar/calendar_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );
				       
// Kind Insert
  $actions["calendar"]["category1_insert"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	     );

// Kind Update
  $actions["calendar"]["category1_update"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	      );

// Kind Check Link
  $actions["calendar"]["category1_checklink"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     		);

// Kind Delete
  $actions["calendar"]["category1_delete"] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                     	       );
					       
// Export
  $actions["calendar"]["export"] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/calendar/calendar_index.php?action=export&amp;popup=1",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

}


///////////////////////////////////////////////////////////////////////////////
// Calendar Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_calendar_action() {
  global $actions, $params, $path, $obm;

  $id = $params["calendar_id"];
  if($id) {
    $event_info = get_calendar_event_info($id);
    $owner = $event_info["owner"];
    $writable_entity = of_right_entity_for_consumer("calendar", "user", $obm["uid"], "write");
    if ($owner != $obm["uid"] && !in_array($owner,$writable_entity["ids"])) {
      // Detail Update
      unset($actions["calendar"]["detailupdate"]);

      // Update
      unset($actions["calendar"]["update"]);
    
      // Check Delete
      unset($actions["calendar"]["check_delete"]);
    
      // Delete
      unset($actions["calendar"]["delete"]);
    }
  }
}

?>
