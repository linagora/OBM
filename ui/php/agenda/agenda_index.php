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
require_once("$obminclude/javascript/calendar_js.inc");

if ($action == "") $action = "index";
$params = get_agenda_params();
get_agenda_action();
$perm->check_permissions($module, $action);

page_close();
$max_display = 6;
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
  $cal_entity_id["user"] = array($uid);
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
    display_head($l_agenda);
    require("$obminclude/calendar.inc");
    display_end();
  } elseif ($action == "export") {
    dis_agenda_export_handle($params);
  }
  exit();
}


if ($action == "index") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_agenda_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_agenda_waiting_events($obm_wait);
  } else {
    require("agenda_js.inc");
    $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
  }

} elseif ($action == "waiting_events") {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_agenda_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    $display["detail"] = html_agenda_waiting_events($obm_wait);
  } else {
    $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
    require("agenda_js.inc");
    $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
  }


} elseif ($action == "decision") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["force"] && $conflicts = check_agenda_decision_conflict($params)) {
    require("agenda_js.inc");
    $display["search"] = html_agenda_dis_conflict($params, $conflicts) ;
    $display["detail"] = html_agenda_conflict_form($params);
    $display["msg"] .= display_err_msg("$l_event : $l_insert_error");
  } else {
    $conflict = run_query_agenda_insert_decision($params);
    $obm_wait = run_query_agenda_waiting_events();
    if ($obm_wait->nf() != 0) {
      $display["msg"] .= display_warn_msg($l_waiting_events." : ".$obm_wait->nf());
      $display["detail"] = html_agenda_waiting_events($obm_wait);
    } else {
      require("agenda_js.inc");
      $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
      $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
    }
  }

} elseif ($action == "view_day") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id, "day");

} elseif ($action == "view_week") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_calendar_view($params, &$cal_entity_id, "week");

} elseif ($action == "view_month") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id, "month");

} elseif ($action == "view_year") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id, "year");

} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_event_form($action, $params, "", $cal_entity_id);

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  if (check_agenda_data_form($params)) {
    if ( (!$params["force"])
	 && ($conflicts = check_agenda_conflict($params, $cal_entity_id)) ) {
      $display["search"] = html_agenda_dis_conflict($params,$conflicts) ;
      $display["msg"] .= display_err_msg("$l_event : $l_insert_error");
      $display["detail"] = dis_agenda_event_form($action, $params, "",$cal_entity_id);
    } else {
      run_query_agenda_add_event($params, $cal_entity_id, $event_id);
      $display["msg"] .= display_ok_msg("$l_event : $l_insert_ok");
      $params["date"] = $params["date_begin"];
      $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
    }
  } else {
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_agenda_event_form($action, $params, "", $cal_entity_id);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  $display["detail"] = dis_agenda_event_consult($params["agenda_id"]);

} elseif ($action == "detailupdate") {
///////////////////////////////////////////////////////////////////////////////
  if ($params["agenda_id"] > 0) {  
    require("agenda_js.inc");
    $eve_q = run_query_agenda_detail($params["agenda_id"]);
    $entities = get_agenda_event_entity($params["agenda_id"]);
    $display["detailInfo"] = display_record_info($eve_q);
    $display["detail"] = dis_agenda_event_form($action, $params, $eve_q, $entities);
  }

} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  if (check_agenda_data_form($params)) {
    if ( (!$params["force"])
	 && ($conflicts = check_agenda_conflict($params, $cal_entity_id)) ) {
      require("agenda_js.inc");
      $display["search"] = html_agenda_dis_conflict($params,$conflicts) ;
      $display["msg"] .= display_err_msg("$l_event : $l_update_error");
      $display["detail"] = dis_agenda_event_form($action, $params, "", $cal_entity_id);
    } else {
      run_query_agenda_event_update($params, $cal_entity_id, $event_id);
      require("agenda_js.inc");
      $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
      $params["date"] = $params["date_begin"];
      $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
    }
  } else {
    require("agenda_js.inc");
    $display["msg"] .= display_warn_msg($l_invalid_data . " : " . $err_msg);
    $display["detail"] = dis_agenda_event_form($action, $params, "", $cal_entity_id);
  }

} elseif ($action == "update_decision") {
///////////////////////////////////////////////////////////////////////////////
  run_query_agenda_update_occurence_state($params["agenda_id"],$uid,$params["decision_event"]);
  require("agenda_js.inc");
  $display["msg"] .= display_ok_msg("$l_event : $l_update_ok");
  $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);

} elseif ($action == "check_delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_agenda_can_delete($params["agenda_id"])) {
    if ($params["agenda_id"] > 0) {
      $display["detail"] = html_agenda_dis_delete($params);
    }
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_agenda_event_consult($params["agenda_id"]);
  }

} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  if (check_agenda_can_delete($params["agenda_id"])) {
    require("agenda_js.inc");
    if ($params["agenda_id"] > 0) {
      run_query_agenda_delete($params);
    }
    $display["detail"] = dis_agenda_calendar_view($params, $cal_entity_id);
  } else {
    $display["msg"] .= display_warn_msg($err_msg, false);
    $display["msg"] .= display_warn_msg($l_cant_delete, false);
    $display["detail"] = dis_agenda_event_consult($params["agenda_id"]);
  }

} elseif ($action == "rights_admin") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["title"] = "<div class=\"title\">$l_agenda</div>";
  $display["detail"] = of_right_dis_admin("calendar", $params["entity_id"]);

} elseif ($action == "rights_update") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["title"] = "<div class=\"title\">$l_agenda</div>";
  of_right_update_right($params, "calendar");
  $display["msg"] .= display_ok_msg("$l_rights : $l_update_ok");
  $display["msg"] .= display_warn_msg($err_msg);
  $display["detail"] = of_right_dis_admin("calendar", $params["entity_id"]);

} elseif ($action == "new_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_meeting_form($params, $cal_entity_id);

} elseif ($action == "perform_meeting")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $cal_entity_id["user"] = run_query_agenda_get_allusers($cal_entity_id["user"], $params["sel_group_id"]);
  $entity_readable = get_agenda_entity_readable();
  $ret = run_query_agenda_get_entity_label($cal_entity_id);
  $ret["resourcegroup"] = run_query_resource_resourcegroup($cal_entity_id["resource_group"]);
  $entity_store = store_agenda_entities($ret);
  $display["features"] = html_agenda_planning_bar($params, $cal_entity_id, $entity_store, $entity_readable);
  $display["detail"] = dis_agenda_free_interval($params, $entity_store);

} elseif ($action == "planning") {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");  
  $entity_readable = get_agenda_entity_readable();
  $cal_entity_id["group_view"] =  $params["entity"]["group_view"];
  $calendar_entity = store_agenda_entities(run_query_agenda_get_entity_label($cal_entity_id));
  $display["features"] = html_agenda_planning_bar($params, $calendar_entity, $entity_readable);
  $display["detail"] = dis_agenda_plain_month_planning($params, $calendar_entity);

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  require("agenda_js.inc");
  $display["detail"] = dis_agenda_admin_index();

} elseif ($action == "category1_insert")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert("calendar", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  require("agenda_js.inc");
  $display["detail"] .= dis_agenda_admin_index();

} elseif ($action == "category1_update")  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update("calendar", "category1", $params);
  if ($retour) {
    $display["msg"] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display["msg"] .= display_err_msg("$l_category1 : $l_update_error");
  }
  require("agenda_js.inc");
  $display["detail"] .= dis_agenda_admin_index();

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
  require("agenda_js.inc");
  $display["detail"] .= dis_agenda_admin_index();
}

$sess->register("cal_entity_id");
//echo "<p>";
//print_r($cal_entity_id);
$display["head"] = display_head($l_agenda);
$display["header"] = display_menu($module);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Agenda parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_agenda_params() {
  global $param_date, $cagenda_first_hour, $cagenda_last_hour;
  
  // Get global params
  $params = get_global_params("Entity");

  // Get agenda specific params
  $params["group_view"] = $params["group_id"];

  if (isset ($param_date)) {
    $params["date"] = $param_date;
  } else {
    $params["date"] = isodate_format();
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
    $start_hour = $cagenda_first_hour;
  }
  if (isset($params["min_begin"])) {
    $start_min = $params["min_begin"];
  } else {
    $start_min = "00";
  }
  if (isset($params["time_end"])) {
    $end_hour = $params["time_end"];
  } else {
    $end_hour = $cagenda_last_hour;
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
  
  $params["event_duration"] =  strtotime($params["date_end"]) - strtotime($params["date_begin"]);
  
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

  display_debug_param($params);
  
  return $params;

}


///////////////////////////////////////////////////////////////////////////////
//  Agenda Action 
///////////////////////////////////////////////////////////////////////////////
function get_agenda_action() {
  global $actions, $path, $params;
  global $l_header_consult, $l_header_update,$l_header_right,$l_header_meeting;
  global $l_header_day,$l_header_week,$l_header_year,$l_header_delete,$l_header_planning;
  global $l_header_month,$l_header_new_event,$l_header_admin, $l_header_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_waiting_events;

  $id = $params["agenda_id"];
  $date = $params["date"];

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
		  'rights_admin','rights_update', 'waiting_events',"planning")
		);

  // Detail Update
  $actions["agenda"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/agenda/agenda_index.php?action=detailconsult&amp;agenda_id=$id&amp;param_date=$date",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );

  // Detail Update
  $actions["agenda"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/agenda/agenda_index.php?action=detailupdate&amp;agenda_id=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Check Delete
  $actions["agenda"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/agenda/agenda_index.php?action=check_delete&amp;agenda_id=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult')
                                     		 );

  // Delete
  $actions["agenda"]["delete"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=delete&amp;agenda_id=$id&amp;param_date=$date",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     		 );

  // Insert
  $actions["agenda"]["insert"] = array (
    'Url'      => "$path/agenda/agenda_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // View Day
  $actions["agenda"]["planning"] = array (
    'Name'     => $l_header_planning,
    'Url'      => "$path/agenda/agenda_index.php?action=planning",
    'Right'    => $cright_read, 
    'Condition'=> array ('all') 
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

//Waiting events.
  $actions["agenda"]["waiting_events"] = array (
    'Name'     => $l_header_waiting_events,
    'Url'      => "$path/agenda/agenda_index.php?action=waiting_events",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
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
