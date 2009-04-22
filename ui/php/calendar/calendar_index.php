<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
 */
?>
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
// - view
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

$path = '..';
$module = 'calendar';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
//FIXME
$params = get_global_params('Entity');
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('calendar_query.inc');
$params = get_calendar_params();
// Get user preferences if set for hour display range 
if (isset($_SESSION['set_cal_first_hour'])) {
  $ccalendar_first_hour = $_SESSION['set_cal_first_hour'];
}
if (isset($_SESSION['set_cal_last_hour'])) {
  $ccalendar_last_hour = $_SESSION['set_cal_last_hour'];
}
if (isset($_SESSION['cal_entity_id'])){
  $cal_entity_id = $_SESSION['cal_entity_id'];
}
if (isset($_SESSION['cal_category_filter'])){
  $cal_category_filter = $_SESSION['cal_category_filter'];
}
if (isset($params['cal_view'])) {
  $cal_view = $params['cal_view'];
} elseif (isset($_SESSION['cal_view'])){
  $cal_view = $_SESSION['cal_view'];
} else {
  $cal_view = 'agenda';
}
if (isset($params['cal_range'])) {
  $cal_range = $params['cal_range'];
} elseif (isset($_SESSION['cal_range'])){
  $cal_range = $_SESSION['cal_range'];
} elseif(isset($_SESSION['set_cal_default_view'])) {
  $params['view_id'] = $_SESSION['set_cal_default_view'];
  $view_properties = run_query_calendar_get_BookmarkProperty_view($_SESSION['set_cal_default_view']);
  $cal_view = $view_properties['cal_view'];
  $cal_range = $view_properties['cal_range'];
  $params['new_group'] = 1;
  $params['group_view'] = $view_properties['group'];
  $params['sel_user_id'] = $view_properties['users'];
  $params['sel_resource_id'] = $view_properties['resources'];
  $params['category_filter'] = $view_properties['category'];
} else {
  $cal_range = 'week';
}

///////////////////////////////////////////////////////////////////////////////

$extra_css[] = $css_calendar;
$extra_js_include[] = 'date.js';
$extra_js_include[] = 'calendar.js';
$extra_js_include[] = 'colorpicker.js';

require('calendar_display.inc');
require_once('calendar_js.inc');
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
get_calendar_action();
update_calendar_action();
$perm->check_permissions($module, $action);

page_close();

OBM_EventFactory::getInstance()->attach(new OBM_EventMailObserver());

// If a group has just been selected, automatically select all its members
if ( ($params['new_group'] == '1')
  && ($params['group_view'] != '') ) {
    // If group selected is ALL, reset group
    if ($params['group_view'] == $c_all) {
      $cal_entity_id['group'] = array();
    } else {
      $cal_entity_id['user'] = of_usergroup_get_group_users($params['group_view']);
      if(is_array($params['sel_user_id'])) {
        array_merge($params['sel_user_id'],$cal_entity_id['user']);
      } else {
        $params['sel_user_id'] = $cal_entity_id['user'];
      }
      $cal_entity_id['group'] = array($params['group_view']);
    }
    $cal_entity_id['group_view'] = $params['group_view'];
    $cal_entity_id['resource'] = array();
  }
// Resources groups, only on meeting
if ($action == 'perform_meeting' &&
  ($params['sel_resource_group_id'] || $params['sel_user_id'] ||
  $params['sel_resource_id'] || $params['sel_group_id'])) { 
    $cal_entity_id['user'] = $params['sel_user_id'];
    $cal_entity_id['resource'] = $params['sel_resource_id'];    
    $cal_entity_id['group'] = $params['sel_group_id'];
    if($params['resource_group_search'] == 'all') {
      if(!is_array($cal_entity_id['resource'])) {
        $cal_entity_id['resource'] = array();
      }
      $resources = run_query_calendar_get_group_resource($params['sel_resource_group_id']);
      $cal_entity_id['resource'] = array_merge($cal_entity_id['resource'], $resources );
    } else {
      $cal_entity_id['resource_group'] = $params['sel_resource_group_id'];
    }
  } else if ($action != 'perform_meeting') {
    unset($cal_entity_id['resource_group']);
  }

// If no group view selected, explicitely set it
if ($cal_entity_id['group_view'] == '') $cal_entity_id['group_view'] = $c_all;

// If user selection present we override session content
if (($params['new_sel']) || (is_array($params['sel_user_id'])) 
  && !(($action == 'insert') || ($action == 'update'))) {
    $cal_entity_id['user'] = $params['sel_user_id'];
    if (is_array($params['sel_group_id'])) {
      $cal_entity_id['group'] = $params['sel_group_id'];
    }       
  }

// If resources selection present we override session content
if (($params['new_sel']) || (is_array($params['sel_resource_id']))
  && !(($action == 'insert') || ($action == 'update'))) {
    $cal_entity_id['resource'] = $params['sel_resource_id'];
  }

// If group selection present we override session content

// If no user or resource selected, we select the connected user
if ( ( (! is_array($cal_entity_id['user']))
       || (count($cal_entity_id['user']) == 0) )
     && ( (! is_array($cal_entity_id['resource']))
	  || (count($cal_entity_id['resource']) == 0))
     && ( (! is_array($cal_entity_id['resource_group']))
          || (count($cal_entity_id['resource_group']) == 0)) ) {
  $cal_entity_id['user'] = array($obm['uid']);
}

// Category Filter 
if (($action == 'insert') || ($action == 'update') 
  || ($action == 'perform_meeting')) {
    $cal_category_filter = '';
  } elseif ( isset($params['category_filter'])) {
    $cal_category_filter = str_replace($c_all,'',$params['category_filter']);
  }
// We copy the entity array structure to the parameter hash
$params['entity'] = $cal_entity_id;
$params['category_filter'] = $cal_category_filter;
///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($popup) {
///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
  if ($action == 'calendar') {
    display_head($l_calendar);
    display_end();
  } elseif ($action == 'export') {
    dis_calendar_export_handle($params);
  }
  exit();
}


$display['search'] = dis_calendar_view_bar($cal_view, $cal_range, $params['date'],$action);

if ($action == 'index') {
  ///////////////////////////////////////////////////////////////////////////////
  if(!$cal_view) {
    $obm_wait = run_query_calendar_waiting_events();
    if ($obm_wait->nf() != 0) {
      $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
      $display['detail'] .= html_calendar_waiting_events($obm_wait);
    }
  } else {
    $display['detail'] .= dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
  }

} elseif ($action == 'waiting_events') {
  ///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
    $display['detail'] = html_calendar_waiting_events($obm_wait);
  } else {
    $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
    $display['detail'] = dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
  }

} elseif ($action == 'decision') {
  ///////////////////////////////////////////////////////////////////////////////
  if (!$params['force'] && $conflicts = check_calendar_decision_conflict($params)) {
    $display['search'] .= html_calendar_dis_conflict($params, $conflicts) ;
    $display['detail'] = html_calendar_conflict_form($params);
    $display['msg'] .= display_err_msg("$l_event : $l_insert_error");
  } else {
    $params['conflicts'] = $conflicts;
    if (check_calendar_participation_decision($params)) {
      $conflict = run_query_calendar_insert_decision($params);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
    $obm_wait = run_query_calendar_waiting_events();
    if ($obm_wait->nf() != 0) {
      $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
      $display['detail'] = html_calendar_waiting_events($obm_wait);
    } else {
      $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
      $display['detail'] = dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
    }
  }

} elseif ($action == 'new') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_event_form($action, $params, '', $cal_entity_id);

} elseif ($action == 'insert') {
  ///////////////////////////////////////////////////////////////////////////////
  $params['sel_user_id']= (is_array($params['sel_user_id']))?$params['sel_user_id']:array();
  $entities['group'] = $params['sel_group_id'];
  $entities['resource'] = $params['sel_resource_id'];
  $entities['user'] = run_query_calendar_merge_groups($params['sel_group_id'],$params['sel_user_id']);
  if (count($entities,COUNT_RECURSIVE) <= 3) {
    $entities['user']  = array($obm['uid']);
    $params['sel_user_id'] = array($obm['uid']);
  }
  $entities['contact'] = $params['sel_contact_id'];
  if ($entities['contact'] == null) {
    $entities['contact'] = array();
  }
  if (check_calendar_data_form($params) && check_access_entity($entities['user'], $entities['resource'])) {

    // Insert "others attendees" as private contacts
    if ($params['others_attendees'] != "") {
      $others_attendees = run_query_insert_others_attendees($params);
      $entities['contact'] = array_merge($entities['contact'], $others_attendees);
    }

    if ( (!$params['force'])
      && ($conflicts = check_calendar_conflict($params, $entities)) ) {
        $display['search'] .= html_calendar_dis_conflict($params,$conflicts) ;
        $display['msg'] .= display_err_msg("$l_event : $l_insert_error");
        $display['detail'] = dis_calendar_event_form($action, $params, '',$entities);
      } else {
        $event_id = run_query_calendar_add_event($params, $entities);
        $params["calendar_id"] = $event_id;
        if ($params['date_begin'] < date('Y-m-d H:')) {
          $display['msg'] .= display_warn_msg("$l_event : $l_warn_date_past");
        }
        $display['msg'] .= display_ok_msg("$l_event : $l_insert_ok");
        $params["date"] = $params["date_begin"];

        $cal_entity_id['user'] = $params['sel_user_id'];

        $display['detail'] = dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
      }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_calendar_event_form($action, $params, '', $entities);
  }

} elseif ($action == 'detailconsult') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }

} elseif ($action == 'detailupdate') {
  ///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $eve_q = run_query_calendar_detail($params['calendar_id']);
      $entities = get_calendar_event_entity($params['calendar_id']);
      $display['detailInfo'] = display_record_info($eve_q);
      $display['detail'] = dis_calendar_event_form($action, $params, $eve_q, $entities);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'duplicate') {
  ///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    $eve_q = run_query_calendar_detail($params['calendar_id']);
    $entities = get_calendar_event_entity($params['calendar_id']);
    $display['detailInfo'] = display_record_info($eve_q);
    $display['detail'] = dis_calendar_event_form($action, $params, $eve_q, $entities);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'update') {
  ///////////////////////////////////////////////////////////////////////////////
  $params['sel_user_id']= (is_array($params['sel_user_id']))?$params['sel_user_id']:array();
  $entities['group'] = $params['sel_group_id'];
  $entities['user'] = $params['sel_user_id'];
  $entities['resource'] = $params['sel_resource_id'];
  $entities['contact'] = $params['sel_contact_id'];
  if ($entities['contact'] == null) {
    $entities['contact'] = array();
  }

  if (check_calendar_access($params["calendar_id"]) && 
    check_calendar_data_form($params)) {
      $c = get_calendar_event_info($params['calendar_id'],false); 
      if ( (!$params['force']) 
        && !($c['date']->equals($params['date_begin']) && $c['event_duration'] == $params['event_duration'])
          && ($conflicts = check_calendar_conflict($params, $entities)) ) {
            $display['search'] .= html_calendar_dis_conflict($params,$conflicts) ;
            $display['msg'] .= display_err_msg("$l_event : $l_update_error");
            $display['detail'] = dis_calendar_event_form($action, $params, '', $entities);
          } else {
            // Insert "others attendees" as private contacts
            if ($params['others_attendees'] != "") {
              $others_attendees = run_query_insert_others_attendees($params);
              $entities['contact'] = array_merge($entities['contact'], $others_attendees);
            }
            run_query_calendar_event_update($params, $entities, $event_id, $mail_data['reset_state']);
            $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
            $params["date"] = $params["date_begin"];
            $display['detail'] = dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
          }
    } else {
      $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
      $display['detail'] = dis_calendar_event_form($action, $params, '', $entities);
    }

} elseif ($action == 'quick_update') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id']) && 
    check_calendar_data_quick_form($params)) {
      $id = $params['calendar_id'];
      $eve_q = run_query_calendar_detail($id);
      run_query_quick_attendee_update($params,$eve_q);    
      if($eve_q->f('event_repeatkind') == 'none' || $params['all'] == 1) {
        run_query_calendar_quick_event_update($params);
      } else {
        $id = run_query_calendar_event_exception_insert($params,$eve_q);
      }
      json_event_data($id,$params);
      json_ok_msg("$l_event : $l_update_ok");
      echo "({".$display['json']."})";
      exit();
    } else {
      json_error_msg($l_invalid_data . " : " . $err['msg']);
      echo "({".$display['json']."})";
      exit();
    }

} elseif ($action == 'quick_insert') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_quick_form($params) && OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'access' )) {
    if( OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'write' )) {
      $state = 'ACCEPTED';
    } else {
      $state = 'NEEDS-ACTION';
    }
    $id = run_query_calendar_quick_event_insert($params, $state);
    $params["calendar_id"] = $id;
    json_ok_msg("$l_event : $l_insert_ok");
    json_event_data($id, $params);
    echo "({".$display['json']."})";
    exit();
  } else {
    json_error_msg($l_invalid_data . ' : ' . $err['msg']);
    echo "({".$display['json']."})";
    exit();
  }

} elseif ($action == 'quick_delete') {  
  ///////////////////////////////////////////////////////////////////////////////
  $id = $params['calendar_id'];
  if (check_calendar_access($id)) {
    $eve_q = run_query_calendar_detail($id);    
    json_event_data($id,$params);
    if($eve_q->f('event_repeatkind') == 'none' || $params['all'] == 1) {      
      run_query_calendar_quick_delete($params);
    } else {
      run_query_calendar_event_exception_insert($params);
    }
    json_ok_msg("$l_event : $l_delete_ok");
    echo "({".$display['json']."})";
    exit();            
  } else {
    json_error_msg($l_invalid_data . " : $err[msg]");
    echo "({".$display['json']."})";    
    exit();
  }

} elseif ($action == 'check_conflict') {
  ///////////////////////////////////////////////////////////////////////////////
  if(isset($params['calendar_id']) && $params['calendar_id'] != '') {
    $entities = get_calendar_event_entity($params['calendar_id']);
  } else {
    $entities['user']['ids'][] = $params['entity_id'];
    $entities['resource']['ids'] = array();
  }
  $conflicts = quick_check_calendar_conflict($params, $entities);
  if ($entities['user']['ids'] == array($obm['uid'])) {
    $json[] = 'mail:false';
  } else {
    $json[] = 'mail:true';
  }
  if (!$conflicts) {
    $json[] = "conflict:false";    
  } else {
    $json[] = "conflict:true";    
  }
  echo "({".implode(',',$json)."})";    
  exit();

} elseif ($action == 'update_decision') {
  ///////////////////////////////////////////////////////////////////////////////
  if (empty($params['entity_id']) && $params['entity_kind'] == 'user') {
    $params['entity_id'] = $obm['uid'];
  }  
  if (check_calendar_event_participation($params)) {
    if (!$params['force'] && $conflicts = check_calendar_decision_conflict($params)) {
      $display['msg'] .= display_warn_msg("$l_event : $l_conflicts");
    }
    $params['conflicts'] = $conflicts;
    if (check_calendar_participation_decision($params)) {
      $retour = run_query_calendar_update_occurrence_state($params['calendar_id'], $params['entity_kind'], $params['entity_id'],$params['decision_event']);
      if ($retour) {
        $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_event  : $err[msg]");
      }
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  } else {
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  }

} elseif ($action == 'update_ext_decision') {
  ///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_calendar_update_occurrence_state($params['calendar_id'], $params['entity_kind'], $params['entity_id'],$params['decision_event']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_event  : $err[msg]");
  }
  if (check_calendar_access($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }


} elseif ($action == 'update_alert') {
  ///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_calendar_event_alert_insert($params['calendar_id'], $params['user_id'],array($params['alert'] => 'dummy'));
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_event  : $err[msg]");
  }
  if (check_calendar_access($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }

} elseif ($action == 'check_delete') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id'])) {
    $display['detail'] = html_calendar_dis_delete($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    } 
  }

} elseif ($action == 'delete') {
  ///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id'])) {
    run_query_calendar_delete($params);
    $display['detail'] = dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    } 
  }

} elseif ($action == 'rights_admin') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_right_dis_admin($params['entity_id']);

} elseif ($action == 'rights_update') {
  ///////////////////////////////////////////////////////////////////////////////
  if (OBM_Acl_Utils::updateRights('calendar', $params['entity_id'], $obm['uid'], $params)) {
    $display['msg'] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display['msg'] .= display_warn_msg($l_of_right_err_auth);
  }
  $display['detail'] = dis_calendar_right_dis_admin($params['entity_id']);

} elseif ($action == 'new_meeting')  {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_meeting_form($params, $cal_entity_id);

} elseif ($action == 'perform_meeting')  {
///////////////////////////////////////////////////////////////////////////////
  $cal_entity_id['user'] = run_query_calendar_get_allusers($cal_entity_id['user'], $params['sel_group_id']);
  $entity_readable = get_calendar_entity_readable();
  $ret = get_calendar_entity_label($cal_entity_id);
  $ret['resourcegroup'] = run_query_resource_resourcegroup($cal_entity_id['resource_group']);
  $entity_store = store_calendar_entities($ret);
  $display['detail'] = dis_calendar_free_interval($params, $entity_store, $cal_entity_id);

} elseif ($action == 'admin')  {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_admin_index();

} elseif ($action == 'category1_insert')  {
  ///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('event', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $display['detail'] .= dis_calendar_admin_index();

} elseif ($action == 'category1_update')  {
  ///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('event', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $display['detail'] .= dis_calendar_admin_index();

} elseif ($action == 'category1_checklink')  {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= of_category_dis_links('event', 'category1', $params, 'mono');

} elseif ($action == 'category1_delete')  {
  ///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_delete('event', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_delete_error");
  }
  $display['detail'] .= dis_calendar_admin_index();

} elseif ($action == 'reset')  {
  ///////////////////////////////////////////////////////////////////////////////
  if(!$params['force']) {
    $display['detail'] .= dis_calendar_reset($params);
  } else {
    run_query_calendar_reset($obm['uid']);
    $display['detail'] .= dis_calendar_calendar_view($params, $cal_entity_id, $cal_view, $cal_range);
  }
} elseif ($action == 'import')  {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_icalendar_import($params);

} elseif ($action == 'ics_insert')  {
  ///////////////////////////////////////////////////////////////////////////////
  $result = run_query_icalendar_insert($params) ;
  if($result !== false) {
    $display['msg'] .= display_ok_msg("$l_event : $l_insert_ok");
    $display['detail'] = dis_icalendar_insert($result);
  } else {
    $display['msg'] .= display_err_msg("$l_file_format $l_unknown");
    $display['detail'] .= dis_icalendar_import($params);
  }

} elseif ($action == 'pdf_export_form') {
  ///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_pdf_options($params, $cal_range, $cal_view);

} elseif ($action == 'pdf_export') {
  ///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lib/Zend/Pdf.php");
  $params['sel_user_id']= (is_array($params['sel_user_id']))?$params['sel_user_id']:array();
  if (count($entities,COUNT_RECURSIVE) <= 4) {
    $entities['user']  = array($obm['uid']);
    $params['sel_user_id'] = array($obm['uid']);
  }
  dis_calendar_pdf_view($params, $cal_entity_id, $cal_range, $cal_view);
  exit();

} elseif ($action == 'conflict_manager') {
  ///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $eve_q = run_query_calendar_detail($params['calendar_id']);
      $entities = get_calendar_event_entity($params['calendar_id']);
      $conflicts_entities['user'] = $params['entity']['user'];
      $conflicts_entities['group'] = $params['entity']['group'];
      $conflicts_entities['resource'] = $params['entity']['resource'];
      $conflicts = check_calendar_conflict($params, $conflicts_entities);
      $display['search'] .= html_calendar_dis_conflict($params, $conflicts) ;
      $display['detail'] = dis_calendar_event_form($action, $params, $eve_q, $entities);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }
}

$_SESSION['cal_entity_id'] = $cal_entity_id;
$_SESSION['cal_category_filter'] = $cal_category_filter;
$_SESSION['cal_view'] = $cal_view;
$_SESSION['cal_range'] = $cal_range;

if (!$params['ajax']) {
  $display['head'] = display_head($l_calendar);
  $display['header'] = display_menu($module);
  $display['end'] = display_end();

} elseif ($action == "insert_view") {
  ///////////////////////////////////////////////////////////////////////////////
  $msg = run_query_calendar_insert_view($params);
  json_ok_msg("$l_view : $l_insert_ok");
  echo "({".$display['json'].",$msg})";
  exit();

} elseif ($action == "delete_view") {
  ///////////////////////////////////////////////////////////////////////////////
  $msg = run_query_calendar_delete_view($params);
  json_ok_msg("$l_view : $l_delete_ok");
  echo "({".$display['json'].",$msg})";
  exit();

} elseif ($action == 'insert_default_view') {
  ///////////////////////////////////////////////////////////////////////////////
  if ($_SESSION['set_cal_default_view']) {
    $obm_default_view = run_query_calendar_update_default_view($params['view_id']);
  } else {
    $obm_default_view = run_query_calendar_insert_default_view($params['view_id']);
  }
  $_SESSION['set_cal_default_view'] = $params['view_id'];
  json_ok_msg("$l_view_default : $l_insert_ok");
  echo "({".$display['json']."})";
  exit();

} elseif ($action == 'delete_default_view') {
  ///////////////////////////////////////////////////////////////////////////////
  if ($_SESSION['set_cal_default_view']) {
    run_query_calendar_delete_default_view();
    $_SESSION['set_cal_default_view'] = NULL;
    json_ok_msg("$l_view_default : $l_delete_ok");
  } else {
    json_error_msg("$l_view_default : $l_delete_error");
  }
  echo "({".$display['json']."})";
  exit();

}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Calendar parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_calendar_params() {
  global $ccalendar_first_hour, $ccalendar_last_hour;

  // Get global params
  $params = get_global_params('Entity');

  // Get calendar specific params
  if ($params['group_view'] == '') {
    $params['group_view'] = $params['group_id'];
  }
  //FIXME
  $params['date'] = of_isodate_convert($params['date']);
  $params['date'] = new Of_Date($params['date']);
  $params['repeat_end'] = of_isodate_convert($params['repeat_end'],true);
  if(!is_null($params['repeat_end'])) {
    $params['repeat_end'] = new Of_Date($params['repeat_end']);
  }
  $params['date_begin'] = of_isodate_convert($params['date_begin'],true);
  if(!is_null($params['date_begin'])) {
    $params['date_begin'] = new Of_Date($params['date_begin']);
  }
  $params['date_end'] = of_isodate_convert($params['date_end'],true);
  if(!is_null($params['date_end'])) {
    $params['date_end'] = new Of_Date($params['date_end']);
  }
  if (isset($params['time_begin']) && !is_null($params['date_begin'])) {
    $params['date_begin']->setHour($params['time_begin']);
    $params['date_begin']->setMinute($params['min_begin']);
  }
  if (isset($params['time_end']) &&  !is_null($params['date_end'])) {
    $params['date_end']->setHour($params['time_end']);
    $params['date_end']->setMinute($params['min_end']);
  } elseif(!is_null($params['date_end'])) {
    $params['date_end']->setHour($ccalendar_last_hour);
  }
  // New meeting event duration
  if (isset($params['time_duration'])) {
    $params['meeting_duration'] = $params['time_duration'] * 3600;
    if (isset($params['min_duration'])) {
      $params['meeting_duration'] += $params['min_duration'] * 60;
    } 
  }
  if (!is_null($params['date_end']) && !is_null($params['date_begin'])) {
    $params['event_duration'] = $params['date_end']->diffTimestamp($params['date_begin']);
    if($params['event_duration'] <= 0) {
      $params['event_duration'] = 0;
    }
  } else {
    $params['event_duration'] = 0;
  }
  if (!is_null($params['date_begin']) && is_null($params['date_end']) && isset($params['duration'])) {
    $clone = clone $params['date_begin'];
    $params['date_end'] = $clone->addSecond($params['duration']);
  } 
  if (is_array($params['date_exception'])) {
    $exceptions = array_unique($params['date_exception']);
    $params['date_exception'] = array();
    foreach($exceptions as $key => $exception) {
      if(trim($exception) != '') {
        $exception = of_isodate_convert($exception);
        $params['date_exception'][$key] = new Of_Date($exception);
      }
    }
  }
  // repeat days
  for ($i=0; $i<7; $i++) {
    if (isset($params["repeatday_$i"])) {
      $params['repeat_days'] .= '1';
    } else {
      $params['repeat_days'] .= '0';
    }
  }

  if ($params['owner']=='') {
    $params['owner'] = $obm['uid'];
  } else {
    if (strcmp(substr($params['owner'], 0, 10),'data-user-') == 0) {
      $data = explode('-', $params['owner']);
      $params['owner'] = $data[2];
    }
  }

  // sel_group_id can be filled by sel_group_id
  if (is_array($params['group_id'])) {
    while (list($key, $value) = each($params['group_id']) ) {
      // sel_group_id contains select infos (data-group-$id)
      if (strcmp(substr($value, 0, 11),'data-group-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_group_id'][] = $id;
      } else {
        // direct id
        $params['sel_group_id'][] = $value;
      }
    }
  }

  // sel_user_id can be filled by sel_user_id or sel_ent (see below)
  if (is_array($params['user_id'])) {
    while (list($key, $value) = each($params['user_id'])) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 10),'data-user-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_user_id'][] = $id;
      } else if (strcmp(substr($value, 0, 13),'data-contact-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_contact_id'][] = $id;
      }
    }
  }

  // sel_resource_id can be filled by sel_resource_id or sel_ent (see below)
  if (is_array($params['resource_id'])) {
    while (list($key, $value) = each($params['resource_id']) ) {
      // sel_resource_id contains select infos (data-resource-$id)
      if (strcmp(substr($value, 0, 14),'data-resource-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_resource_id'][] = $id;
      } else {
        // direct id
        $params['sel_resource_id'][] = $value;
      }
    }
  }
  // sel_resource_id can be filled by sel_resource_id or sel_ent (see below)
  if (is_array($params['resource_group_id'])) {
    while (list($key, $value) = each($params['resource_group_id']) ) {
      // sel_resource_id contains select infos (data-resource-$id)
      if (strcmp(substr($value, 0, 19),'data-resourcegroup-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_resource_group_id'][] = $id;
      } else {
        // direct id
        $params['sel_resource_group_id'][] = $value;
      }
    }
  }  
  // feature params (user & resource)
  if (is_array($params['ent'])) {
    $nb_data = 0;
    $nb['user'] = 0;
    $nb['resource'] = 0;
    while(list($key,$value ) = each($params['ent'])) {
      if (strcmp(substr($value, 0, 5),'data-') == 0) {
        $nb_data++;
        $data = explode('-', $value);
        $ent = $data[1];
        $id = $data[2];
        $nb[$ent]++;
        $params["sel_${ent}_id"][] = $id;
      }
    }
  }

  // imported file
  if (isset ($_FILES['fi_ics'])) {
    $params['ics_tmp'] = $_FILES['fi_ics']['tmp_name'];
    $params['ics_name'] = $_FILES['fi_ics']['name'];
    $params['ics_size'] = $_FILES['fi_ics']['size'];
    $params['ics_type'] = $_FILES['fi_ics']['type'];
  }

  if(is_array($params['others_attendees'])) {
    foreach($params['others_attendees'] as $mail) {
      if(trim($mail) != '') $others_attendees[] = trim($mail);
    }
    $params['others_attendees'] = $others_attendees;
  }
  return $params;
}


///////////////////////////////////////////////////////////////////////////////
//  Calendar Action 
///////////////////////////////////////////////////////////////////////////////
function get_calendar_action() {
  global $actions, $path, $params;
  global $l_header_consult, $l_header_update,$l_header_right,$l_header_meeting;
  global $l_header_planning, $l_header_list, $l_header_duplicate, $l_header_delete;
  global $l_header_new_event,$l_header_admin, $l_header_export, $l_header_import;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;
  global $l_header_waiting_events, $l_calendar;

  $id = $params['calendar_id'];
  $date = $params['date'];
  // Index
  $actions['calendar']['index'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Looking for Conflicts
  $actions['calendar']['check_conflict'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=check_conflict",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Conflict Manager
  $actions['calendar']['conflict_manager'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=conflict_manager",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Decision
  $actions['calendar']['decision'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=decision",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Decision
  $actions['calendar']['calendar'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=calendar",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  // New   
  $actions['calendar']['new'] = array (
    'Name'     => $l_header_new_event,
    'Url'      => "$path/calendar/calendar_index.php?action=new&amp;date_begin=".$date->getURL(),
    'Right'    => $cright_write,
    'Condition'=> array ('index','detailconsult','insert','insert_conflict',
    'update_decision','update_ext_decision', 'update_alert','decision','update','delete', 'new_meeting',
    'rights_admin','rights_update', 'waiting_events','planning')
  );

  // Detail Consult
  $actions['calendar']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );

  // Detail Update
  $actions['calendar']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/calendar/calendar_index.php?action=detailupdate&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult','update_alert','update_decision', 'update_ext_decision') 
  );

  // Duplicate
  $actions['calendar']['duplicate'] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/calendar/calendar_index.php?action=duplicate&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Check Delete
  $actions['calendar']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/calendar/calendar_index.php?action=check_delete&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult')
  );

  // Delete
  $actions['calendar']['delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=delete&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );

  // Insert
  $actions['calendar']['insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );


  // Update
  $actions['calendar']['update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Update
  $actions['calendar']['quick_update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Quick Insert
  $actions['calendar']['quick_insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Quick Delete
  $actions['calendar']['quick_delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Update
  $actions['calendar']['update_decision'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update_decision",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Update external decision
  $actions['calendar']['update_ext_decision'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update_ext_decision",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Update
  $actions['calendar']['update_alert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update_alert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );  

  // Waiting events
  $actions['calendar']['waiting_events'] = array (
    'Name'     => $l_header_waiting_events,
    'Url'      => "$path/calendar/calendar_index.php?action=waiting_events",
    'Right'    => $cright_write,
    'Condition'=> array ('all')
  );

  // New meeting
  $actions['calendar']['new_meeting'] = array (
    'Name'     => $l_header_meeting,
    'Url'      => "$path/calendar/calendar_index.php?action=new_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
  );

  // Search meeting
  $actions['calendar']['perform_meeting'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=perform_meeting",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Right admin
  $actions['calendar']['rights_admin'] = array (
    'Name'     => $l_header_right,
    'Url'      => "$path/calendar/calendar_index.php?action=rights_admin",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );

  // Update Right
  $actions['calendar']['rights_update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=rights_update",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Admin
  $actions['calendar']['admin'] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/calendar/calendar_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  );

  // Kind Insert
  $actions['calendar']['category1_insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Kind Update
  $actions['calendar']['category1_update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Kind Check Link
  $actions['calendar']['category1_checklink'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_checklink",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Kind Delete
  $actions['calendar']['category1_delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=category_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Export
  $actions['calendar']['export'] = array (
    'Name'     => $l_header_export,
    'Url'      => "$path/calendar/calendar_index.php?action=export&amp;popup=1",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );

  // Import iCalendar (get the file)
  $actions['calendar']['import'] = array (
    'Name'     => $l_header_import,
    'Url'      => "$path/calendar/calendar_index.php?action=import",
    'Right'    => $cright_write,
    'Condition'=> array ('all') 
  );
  // Reset Calendar
  $actions['calendar']['reset'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=reset",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  // Insert ICalendar (insert the events)
  $actions['calendar']['ics_insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=ics_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );
  // Insert view
  $actions['calendar']['insert_view'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=insert_view",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

  // Delete view
  $actions['calendar']['delete_view'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=delete_view",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );
  // Insert default view
  $actions['calendar']['insert_default_view'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=insert_default_view",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );
  // Delete default view
  $actions['calendar']['delete_default_view'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=delete_default_view",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );
  // PDF export options form
  $actions['calendar']['pdf_export_form'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=pdf_export_form&amp;date=$date&output_target=print",
    'Right'    => $cright_read,
    'Popup'    => 1,
    'Condition'=> array("None")
  );

  // PDF export
  $actions['calendar']['pdf_export'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=pdf_export",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );

}


///////////////////////////////////////////////////////////////////////////////
// Calendar Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_calendar_action() {
  global $actions, $params, $path, $obm;

  $id = $params['calendar_id'];
  if($id) {
    $event_info = get_calendar_event_info($id);
    $owner = $event_info['owner'];
    if ($owner != $obm['uid'] && !OBM_Acl::canWrite($obm['uid'], 'calendar', $owner)) {
      // Detail Update
      unset($actions['calendar']['detailupdate']);

      // Duplicate
      unset($actions['calendar']['duplicate']);
      $data = "<a href=\"$datas[0]\">$datas[0]</a>";

      // Update
      unset($actions['calendar']['update']);

      // Check Delete
      unset($actions['calendar']['check_delete']);

      // Delete
      unset($actions['calendar']['delete']);
    }
  }
}

?>
