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
if(isset($params['set_cal_view_id'])) {
  $current_view = CalendarView::get_from_id($params['set_cal_view_id']);
}
if (!($current_view instanceOf CalendarView) && isset($_SESSION['cal_current_view']) && (is_string($_SESSION['cal_current_view']))) {
  $current_view = unserialize($_SESSION['cal_current_view']);
}
if(!($current_view instanceOf CalendarView) && isset($_SESSION['set_cal_default_view'])) {
  $current_view = CalendarView::get_from_id($_SESSION['set_cal_default_view']);
}
if(!($current_view instanceOf CalendarView)) {
  $current_view = new CalendarView(array(
    // default view settings
    'cal_view'  => 'agenda',
    'cal_range' => 'week',
    'category'  => $c_all,
    'users'     => array($obm['uid'])
  ));
}
if (isset($params['category_filter'])) {
  $current_view->set_category($params['category_filter']);
}
if (isset($params['cal_view'])) {
  $current_view->set_cal_view($params['cal_view']);
}
if (isset($params['cal_range'])) {
  $current_view->set_cal_range($params['cal_range']);
}
if (isset($params['group_view']) && ($params['group_view']!=$current_view->get_group())) {
  $current_view->set_group($params['group_view']);
}

///////////////////////////////////////////////////////////////////////////////

$extra_css[] = $css_calendar;
$extra_css[] = $css_ext_color_picker ;
$extra_js_include[] = 'date.js';
$extra_js_include[] = 'calendar.js';
$extra_js_include[] = 'colorchooser.js';
$extra_js_include[] = 'inplaceeditor.js';

$extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;

require('calendar_display.inc');
require_once('calendar_js.inc');
require("$obminclude/of/of_helpers.php");
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
require('calendar_mailer.php');
require('event_observer.php');
get_calendar_action();
update_calendar_action();
$perm->check_permissions($module, $action);

page_close();

OBM_EventFactory::getInstance()->attach(new OBM_EventMailObserver());

// Resources groups, only on meeting
if ($action == 'add_freebusy_entity' || $action == 'new_meeting' || 
  ($action == 'new' && $params['new_meeting'] == 1) || ($action == 'perform_meeting' &&
  ($params['sel_resource_group_id'] || $params['sel_user_id'] ||
  $params['sel_resource_id'] || $params['sel_group_id']))) { 
    $cal_entity_id['user'] = $params['sel_user_id'];
    $cal_entity_id['resource'] = $params['sel_resource_id'];    
    $cal_entity_id['group'] = $params['sel_group_id'];
    $cal_entity_id['contact'] = $params['sel_contact_id'];
    if($params['resource_group_search'] == 'all') {
      if(!is_array($cal_entity_id['resource'])) {
        $cal_entity_id['resource'] = array();
      }
      $resources = run_query_calendar_get_group_resource($params['sel_resource_group_id']);
      $cal_entity_id['resource'] = array_merge($cal_entity_id['resource'], $resources );
    } else {
      $cal_entity_id['resource_group'] = $params['sel_resource_group_id'];
    }
  } elseif ($action != 'perform_meeting') {
    unset($cal_entity_id['resource_group']);
  }

// If no group view selected, explicitely set it
if ($cal_entity_id['group_view'] == '') $cal_entity_id['group_view'] = $c_all;

// If user selection present we override session content
if (($params['new_sel']) && (is_array($params['sel_user_id'])) 
  && !(($action == 'insert') || ($action == 'update'))) {
    $current_view->set_users($params['sel_user_id']);
  }

// If resources selection present we override session content
if (($params['new_sel']) && (is_array($params['sel_resource_id']))
  && !(($action == 'insert') || ($action == 'update'))) {
    $current_view->set_resources($params['sel_resource_id']);
  }

// If group selection present we override session content

// If no user or resource selected, we select the connected user
$users = $current_view->get_users();
$resources = $current_view->get_resources();
if (empty($users) && empty($resources)) {
  $current_view->add_user($obm['uid']);
}
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
    $cal_category_filter = str_replace($c_all,'',$current_view->get_category());
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
    // display_head($l_calendar);
    // display_end();
  } elseif ($action == 'ics_export') {
    dis_calendar_export_handle($params);
  }
  exit();
}


$display['search'] = dis_calendar_view_bar($current_view, $params['date'],$action, $params);

if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_calendar_search_result($params, $current_view);

} elseif ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_calendar_calendar_view($params, $current_view);

} elseif ($action == 'waiting_events') {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
    $display['detail'] = html_calendar_waiting_events($obm_wait);
  } else {
    $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
    $display['detail'] = dis_calendar_calendar_view($params, $current_view);
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
      $display['detail'] = dis_calendar_calendar_view($params, $current_view);
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
  $entities['document'] = is_array($params['sel_document_id']) ? $params['sel_document_id'] : array();
  
  if (check_calendar_data_form($params) && check_access_entity($entities['user'], $entities['resource'])) {

    $conflicts = check_calendar_conflict($params, $entities);
    if ( $conflicts && (!$params['force'] || !can_force_resource_conflict($conflicts)) ) {
        if ($conflicts && !can_force_resource_conflict($conflicts)) {
          $params['force_disabled'] = 1;
          $params['force'] = 0;
        }
        $display['search'] .= html_calendar_dis_conflict($params,$conflicts) ;
        $display['msg'] .= display_err_msg("$l_event : $l_insert_error");
        $display['detail'] = dis_calendar_event_form($action, $params, '',$entities);
      } else {
        // Insert "others attendees" as private contacts
        if ($params['others_attendees'] != "") {
          $others_attendees = run_query_insert_others_attendees($params);
          $entities['contact'] = array_merge($entities['contact'], $others_attendees);
        }
        // Insert "other files" as private documents
        if (is_array($params['other_files'])) {
          $other_files = run_query_insert_other_files($params);
          $entities['document'] = array_merge($entities['document'], $other_files);
        }
        $event_id = run_query_calendar_add_event($params, $entities);
        $params["calendar_id"] = $event_id;
        if ($params['date_begin'] < date('Y-m-d H:')) {
          $display['msg'] .= display_warn_msg("$l_event : $l_warn_date_past");
        }

        if ($params['add_displayed_users']) {
          if ($params['show_attendees_calendar']) {
            // Display attendees
            $cal_entity_id['user'] = $params['sel_user_id'];
            $cal_entity_id['resource'] = $params['sel_resource_id'];
          } else {
            // Display calendars
          }
        } else {
          if ($params['show_attendees_calendar']) {
            // Display attendees
            $cal_entity_id['user'] = $params['sel_user_id'];
            $cal_entity_id['resource'] = $params['sel_resource_id'];
          } else {
            // Display calendars

          }
        }
        $display['msg'] .= display_ok_msg("$l_event : $l_insert_ok");
        $params["date"] = $params["date_begin"];
        $display['detail'] = dis_calendar_calendar_view($params, $current_view);
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
  $entities['document'] = is_array($params['sel_document_id']) ? $params['sel_document_id'] : array();

  if (check_calendar_access($params["calendar_id"]) && 
    check_calendar_data_form($params)) {
      $c = get_calendar_event_info($params['calendar_id'],false); 
      $conflicts = check_calendar_conflict($params, $entities);
      if ($conflicts && (!$params['force'] || !can_force_resource_conflict($conflicts))
        && !($c['date']->equals($params['date_begin']) && $c['event_duration'] == $params['event_duration'])) 
      {
        if ($conflicts && !can_force_resource_conflict($conflicts)) {
          $params['force_disabled'] = 1;
          $params['force'] = 0;
        }
        $display['search'] .= html_calendar_dis_conflict($params,$conflicts) ;
        $display['msg'] .= display_err_msg("$l_event : $l_update_error");
        $display['detail'] = dis_calendar_event_form($action, $params, '', $entities);
      } else {
        // Insert "others attendees" as private contacts
        if ($params['others_attendees'] != "") {
          $others_attendees = run_query_insert_others_attendees($params);
          $entities['contact'] = array_merge($entities['contact'], $others_attendees);
        }
        // Insert "other files" as private documents
        if (is_array($params['other_files'])) {
          $other_files = run_query_insert_other_files($params);
          $entities['document'] = array_merge($entities['document'], $other_files);
        }
        run_query_calendar_event_update($params, $entities, $event_id, $mail_data['reset_state']);

        if ($params['add_displayed_users']) {
          if ($params['show_attendees_calendar']) {
            // Display attendees
            $cal_entity_id['user'] = $params['sel_user_id'];
            $cal_entity_id['resource'] = $params['sel_resource_id'];
          } else {
            // Display calendars
          }
        } else {
          if ($params['show_attendees_calendar']) {
            // Display attendees
            $cal_entity_id['user'] = $params['sel_user_id'];
            $cal_entity_id['resource'] = $params['sel_resource_id'];
          } else {
            // Display calendars

          }
        }

        $display['msg'] .= display_ok_msg("$l_event : $l_update_ok");
        $params["date"] = $params["date_begin"];
        $display['detail'] = dis_calendar_calendar_view($params, $current_view);
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
      json_event_data($id, $params, $current_view);
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
    json_event_data($id, $params, $current_view);
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
    json_event_data($id, $params, $current_view);
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
    $display['detail'] = dis_calendar_calendar_view($params, $current_view);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    } 
  }
  
} elseif ($action == 'detach_document') {
///////////////////////////////////////////////////////////////////////////////
  run_query_calendar_detach_document($params['document_id'], $params['event_entity_id']);
  json_ok_msg("$l_document : $l_delete_ok");
  echo "({".$display['json']."})";
  exit();

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

} elseif ($action == 'admin')  {
///////////////////////////////////////////////////////////////////////////////
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] = dis_calendar_admin_index($tags_q);

} elseif ($action == 'tags_update')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tag_form($params)) {
    $retour = run_query_tag_update($obm['uid'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("Tag : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("Tag : $l_update_error");
    }
  }
  else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);
  
} elseif ($action == 'tag_insert')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tag_form($params)) {
    $retour = run_query_tag_insert($obm['uid'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("Tag : $l_insert_ok");
    } else {
      $display['msg'] .= display_err_msg("Tag : $l_insert_error");
    }
  }
  else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);
  
} elseif ($action == 'tag_delete')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tag_delete($obm['uid'], $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("Tag : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("Tag : $l_delete_error");
  }
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);

} elseif ($action == 'tag_search')  {
///////////////////////////////////////////////////////////////////////////////
  $tags_q = run_query_tag_search($obm['uid'], $params);
	$json = json_tag_search($tags_q) ;
	echo "(".$json.")" ;
	exit() ;
  
} elseif ($action == 'save_as_template') {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_data_form($params)) {
    // Insert "others attendees" as private contacts
    if ($params['others_attendees'] != "") {
      $others_attendees = run_query_insert_others_attendees($params);
      $params['sel_contact_id'] = is_array($params['sel_contact_id']) 
                                ? array_merge($params['sel_contact_id'], $others_attendees)
                                : $others_attendees;
    }
    // Insert "other files" as private documents
    if (is_array($params['other_files'])) {
      $other_files = run_query_insert_other_files($params);
      $params['sel_document_id'] = is_array($params['sel_document_id'])
                                 ? array_merge($params['sel_document_id'], $other_files)
                                 : $other_files;
    }
    $template_id = run_query_calendar_add_event_template($params);
    
    $display['msg'] .= display_ok_msg("$l_template : $l_insert_ok");
    $params["date"] = $params["date_begin"];
    $display['detail'] = dis_calendar_calendar_view($params, $current_view);
  } else {
    foreach (array('user', 'group', 'resource', 'contact', 'document') as $type) {
      $entities[$type] = is_array($params["sel_{$type}_id"]) ? $params["sel_{$type}_id"] : array();
    }
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['detail'] = dis_calendar_event_form($action, $params, '', $entities);
  }
  
} elseif ($action == 'list_templates')  {
///////////////////////////////////////////////////////////////////////////////
  $templates_q = run_query_calendar_get_alltemplates($obm['uid']);
  $display['detail'] = dis_calendar_templates_list($templates_q);
  
} elseif ($action == 'duplicate_template')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_calendar_duplicate_template($params['template_id'], $params['template_name']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_template : $l_duplicate_ok");
  } else {
    $display['msg'] .= display_ok_msg("$l_template : $l_duplicate_error");
  }
  $templates_q = run_query_calendar_get_alltemplates($obm['uid']);
  $display['detail'] = dis_calendar_templates_list($templates_q);
  
} elseif ($action == 'set_template_name')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_calendar_set_template_name($params['id'], $params['content']);
  if ($retour) {
    echo $params['content'];  
  } else {
    header('HTTP/1.x 500');
    echo 'Error on setting template name';   
  }
  exit();
  
} elseif ($action == 'delete_template')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_calendar_delete_template($params['template_id']);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_template : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_template : $l_delete_error");
  }
  $templates_q = run_query_calendar_get_alltemplates($obm['uid']);
  $display['detail'] = dis_calendar_templates_list($templates_q);

} elseif ($action == 'category1_insert')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_insert('event', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_insert_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_insert_error");
  }
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);

} elseif ($action == 'category1_update')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = of_category_query_update('event', 'category1', $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_category1 : $l_update_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_category1 : $l_update_error");
  }
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);

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
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);
  
} elseif ($action == 'document_add') {
///////////////////////////////////////////////////////////////////////////////
  $params['event_id'] = $params['ext_id'];
  if ($params['doc_nb'] > 0) {
    $nb = run_query_global_insert_documents_links($params, 'event');
    $display['msg'] .= display_ok_msg("$nb $l_document_added");
  } else {
    $display['msg'] .= display_err_msg($l_no_document_added);
  }
  $display['detail'] .= dis_calendar_event_consult($params['event_id']);

} elseif ($action == 'reset')  {
///////////////////////////////////////////////////////////////////////////////
  if(!$params['force']) {
    $display['detail'] .= dis_calendar_reset($params);
  } else {
    run_query_calendar_reset($obm['uid'],$params);
    $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
  }

} elseif ($action == 'export')  {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_icalendar_export($params);

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
  $display['detail'] = dis_calendar_pdf_options($params, $current_view);

} elseif ($action == 'pdf_export') {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lib/Zend/Pdf.php");
  $params['sel_user_id']= (is_array($params['sel_user_id']))?$params['sel_user_id']:array();
  if (count($entities,COUNT_RECURSIVE) <= 4) {
    $entities['user']  = array($obm['uid']);
    $params['sel_user_id'] = array($obm['uid']);
  }
  dis_calendar_pdf_view($params, $current_view);
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

$_SESSION['cal_current_view'] = serialize($current_view);

if (!$params['ajax']) {
  $display['head'] = display_head($l_calendar);
  $display['header'] = display_menu($module);
  $display['end'] = display_end();

} elseif ($action == 'insert_view') {
///////////////////////////////////////////////////////////////////////////////
  $view = clone $current_view;
  $view->set_label($params['view_label']);
  $view->save();
  $view_id = $view->get_id();
  $view_label = $view->get_label();
  $msg = "\"obmbookmark_id\": \"$view_id\", \"obmbookmark_label\":\"$view_label\", \"obmbookmarkproperties\":\"$obmbookmarkproperty\"";
  json_ok_msg("$l_view : $l_insert_ok");
  echo "({".$display['json'].",$msg})";
  exit();

} elseif ($action == 'delete_view') {
///////////////////////////////////////////////////////////////////////////////
  $view = CalendarView::get_from_id($params['view_id']);
  $msg = $view->delete();
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

} elseif ($action == 'add_freebusy_entity') {
///////////////////////////////////////////////////////////////////////////////
  $cal_entity_id['user'] = $params['user_id'];
  $ret = get_calendar_entity_label($cal_entity_id);
  $ret['resourcegroup'] = run_query_resource_resourcegroup($cal_entity_id['resource_group']);
  $entity_store = store_calendar_entities($ret);
  get_json_entity_events($params, $entity_store);
  echo "({".$display['json']."})";
  exit();

} elseif($action == 'get_json_waiting_events') {
  get_json_waiting_events($obm['uid']);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'perform_meeting')  {
///////////////////////////////////////////////////////////////////////////////
  dis_calendar_free_interval($params);
  echo "({".$display['json']."})";
  exit();

} elseif ($action == 'set_entity_class')  {
///////////////////////////////////////////////////////////////////////////////
  $current_view->set_entity_class($params['entity_type'],$params['entity_id'],$params['entity_class']);
  $_SESSION['cal_current_view'] = serialize($current_view);
  json_ok_msg("$l_view : $l_insert_ok");
  echo "({".$display['json'].",$msg})";
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
  $params['event_before_date'] = of_isodate_convert($params['event_before_date']);
  if(!is_null($params['event_before_date'])) {
    $params['event_before_date'] = new Of_Date($params['event_before_date']);
  }
  $params['date_begin'] = of_isodate_convert($params['date_begin'],true);
  if(!is_null($params['date_begin'])) {
    $params['date_begin'] = new Of_Date($params['date_begin']);
  }
  $params['date_end'] = of_isodate_convert($params['date_end'],true);
  if(!is_null($params['date_end'])) {
    $params['date_end'] = new Of_Date($params['date_end']);
  }
  $params['old_date_begin'] = of_isodate_convert($params['old_date_begin'],true);
  if(!is_null($params['old_date_begin'])) {
    $params['old_date_begin'] = new Of_Date($params['old_date_begin']);
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
  
  if (is_array($params['document_id'])) {
    while (list($key, $value) = each($params['document_id']) ) {
      // sel_document_id contains select infos (data-document-$id)
      if (strcmp(substr($value, 0, 14),'data-document-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_document_id'][] = $id;
      } else {
        // direct id
        $params['sel_document_id'][] = $value;
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
  
  if (isset ($_FILES['fi_other_files'])) {
    $params['other_files'] = array();
    foreach ($_FILES['fi_other_files']['name'] as $k => $name) {
      if ($_FILES['fi_other_files']['error'][$k] !== UPLOAD_ERR_OK) {
        continue;
      }
      $params['other_files'][] = array(
        'file_tmp' => $_FILES['fi_other_files']['tmp_name'][$k],
        'name' => $_FILES['fi_other_files']['name'][$k],
        'size' => $_FILES['fi_other_files']['size'][$k],
        'type' => $_FILES['fi_other_files']['type'][$k],
      );
    }
  }

  if(is_array($params['others_attendees'])) {
    foreach($params['others_attendees'] as $mail) {
      if(trim($mail) != '') $others_attendees[] = trim($mail);
    }
    $params['others_attendees'] = $others_attendees;
  }
  
  get_global_params_document($params);
  
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
  global $l_header_waiting_events, $l_calendar, $l_header_templates;

  $id = $params['calendar_id'];
  $date = $params['date'];
  // Index
  $actions['calendar']['index'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  $actions['calendar']['search'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=search",
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
    'Url'      => "$path/calendar/calendar_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('index','detailconsult','insert','insert_conflict',
    'update_decision','update_ext_decision', 'update_alert','decision','update','delete', 'new_meeting',
    'rights_admin','rights_update', 'waiting_events','planning','save_as_template')
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

  // Search meeting
  $actions['calendar']['set_entity_class'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=set_entity_class",
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

  // Tag Update
  $actions['calendar']['tags_update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Tag Insert
  $actions['calendar']['tag_insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_insert",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Tag Delete
  $actions['calendar']['tag_delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
  );

  // Tag Search
  $actions['calendar']['tag_search'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_search",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
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
    'Url'      => "$path/calendar/calendar_index.php?action=export",
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
  
  // Templates
  $actions['calendar']['list_templates'] = array (
    'Name'     => $l_header_templates,
    'Url'      => "$path/calendar/calendar_index.php?action=list_templates",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
  );
  
  // Reset Calendar
  $actions['calendar']['reset'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=reset",
    'Right'    => $cright_write,
    'Condition'=> array ('none') 
  );

  // Export_ics
  $actions['calendar']['ics_export'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=ics_export&amp;popup=1",
    'Right'    => $cright_read,
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

  // Add freebusy entity
  $actions['calendar']['add_freebusy_entity'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=add_freebusy_entity",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );
  
  // Get JSON waiting event 
  $actions['calendar']['get_json_waiting_events'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=get_json_waiting_events",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                                 );
  
  // Document add
  $actions['calendar']['document_add'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );
  
  // Detach document
  $actions['calendar']['detach_document'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
  );
  
  // Save as template
  $actions['calendar']['save_as_template'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Duplicate template
  $actions['calendar']['duplicate_template'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Set template name
  $actions['calendar']['set_template_name'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Delete template
  $actions['calendar']['delete_template'] = array (
    'Right'    => $cright_write,
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
