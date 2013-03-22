<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
if (empty($_FILES) && empty($_POST) && isset($_SERVER['REQUEST_METHOD']) && strtolower($_SERVER['REQUEST_METHOD']) == 'post') {
	$post_max_size = ini_get('post_max_size');
	$post_fatal_error = true;
}
$path = '..';
$module = 'calendar';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require_once("$obminclude/global.inc");
//FIXME
$params = get_global_params('Entity');
if(isset($params['date']) && !empty($params['date'])) {
  $set_date = true;
}
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
require_once($obminclude . '/global_pref.inc');
require_once('calendar_query.inc');
require_once($obminclude . '/of/of_contact.php');
require_once($obminclude . '/of/of_query.inc');
require_once($obminclude . '/of/of_captcha.php');
require_once($obminclude . '/of/of_session.inc');

$params = get_calendar_params();
// Get user preferences if set for hour display range 
if (isset($_SESSION['set_cal_first_hour'])) {
  $ccalendar_first_hour = $_SESSION['set_cal_first_hour'];
}
if (isset($_SESSION['set_cal_last_hour'])) {
  $ccalendar_last_hour = $_SESSION['set_cal_last_hour'];
}
if(isset($params['set_cal_view_id']) && $params['set_cal_view_id']!=0) {
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
  $current_view->set_cal_range($params['cal_range'],$params['ndays']);
}
if (isset($params['group_view']) && ($params['group_view']!=$current_view->get_group())) {
  $current_view->set_group($params['group_view']);
}
if ($set_date || !isset($_SESSION['cal_current_view'])) {
  $current_view->set_date($params['date']);
}
// Set the GLOBALS for email, checked by ??
if (! isset($GLOBALS['send_notification_mail'])) {
  $GLOBALS['send_notification_mail'] = isset($params['send_mail']) ? $params['send_mail'] : false;
}
///////////////////////////////////////////////////////////////////////////////

$extra_css[] = $css_calendar;
$extra_js_include[] = 'date.js';
$extra_js_include[] = 'calendar.js';
$extra_js_include[] = 'colorchooser.js';

require_once('calendar_display.inc');
require_once('calendar_js.inc');
require_once("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
require_once('calendar_mailer.php');
require_once('obm_eventdiff.php');
require_once('event_observer.php');
require_once('event_resource_mail_observer.php');
require_once('../contact/addressbook.php');

if ($params['new_sel'] && (($action != 'insert') && ($action != 'update'))) {
  $current_view->set_users($params['sel_user_id']);
  $current_view->set_resources($params['sel_resource_id']);
  $current_view->set_contacts($params['sel_contact_id']);
}

// If no user or resource selected, we select the connected user
$users = $current_view->get_users();
$resources = $current_view->get_resources();
if (empty($users) && empty($resources)) {
  $current_view->add_user($obm['uid']);
}
# Retrieve the list of writable calendars
$writable_calendars = $current_view->get_writable_calendars($obm["uid"]);
$default_writable_calendar = $current_view->get_default_writable_calendar($obm["uid"]);


get_calendar_action();
update_calendar_action($writable_calendars);
$perm->check_permissions($module, $action);

page_close();

OBM_EventFactory::getInstance()->attach(new OBM_EventMailObserver());
OBM_EventFactory::getInstance()->attach(new OBM_EventResourceMailObserver());
if( isset($GLOBALS['ccalendar_ics_eventStompObserver']) && $GLOBALS['ccalendar_ics_eventStompObserver']) {
  OBM_EventFactory::getInstance()->attach(new OBM_EventStompObserver());
}
// For debugging purpose, this observer outputs in /tmp/debug
// OBM_EventFactory::getInstance()->attach(new OBM_EventDebugObserver());

$profiles = get_all_profiles(false);

// Category Filter 
if (($action == 'insert') || ($action == 'update') 
  || ($action == 'perform_meeting')) {
    $cal_category_filter = '';
  } elseif ( isset($params['category_filter'])) {
    $cal_category_filter = str_replace($c_all,'',$current_view->get_category());
  }
// We copy the entity array structure to the parameter hash
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

if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  include_once('obminclude/lib/Solr/Service.php');
  $display['detail'] .= dis_calendar_search_result($params, $current_view);

} elseif ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
	if (isset($post_fatal_error)) {
		$_SESSION['obm_message'] = "<script text='language/javascript'>showErrorMessageCustomTimeout(\"".phpStringToJsString($l_post_fatal_error)."\", 10000)</script>";
	}
  $display['detail'] .= dis_calendar_calendar_view($params, $current_view);

} elseif ($action == 'waiting_events') {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  if ($obm_wait->nf() != 0) {
    $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
    $display['detail'] = html_calendar_waiting_events($obm_wait);
    if ($params['show_calendar']) {
      $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
    }
  } else {
    $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
  }

} elseif ($action == 'decision') {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'freebusy.js';
  if (!$params['force'] && $conflicts = check_calendar_decision_conflict($params)) {
    $display['detail'] = html_calendar_dis_conflict($params, $conflicts) ;
    $display['detail'] .= html_calendar_conflict_form($params);
    $display['msg'] .= display_err_msg("$l_event : $l_insert_error");
  } else {
    if (check_calendar_participation_decision($params)) {
      //we want to send mails
      $GLOBALS["send_notification_mail"] = true;
      run_query_calendar_insert_decision($params, $obm['uid']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
    $obm_wait = run_query_calendar_waiting_events();
    if ($obm_wait->nf() != 0 && $params['uriAction'] == 'waiting_events') {
      $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
      $display['detail'] = html_calendar_waiting_events($obm_wait);
    } else {
      redirect_ok($params, "$l_event: $l_update_ok");
    }
  }

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_js_include[] = 'freebusy.js';
  $extra_js_include[] = '/event/eventForm.js';
  $extra_css[] = $css_ext_color_picker ;
  $GLOBALS['extra_js'] .= '
  window.addEvent("domready", function(){
    var eventForm = new Obm.Event.Form();
  });';
  if (is_array($params['sel_user_id']) || is_array($params['sel_resource_id'])) {
    $entities = array(
      'user' => $params['sel_user_id'],
      'resource' => $params['sel_resource_id'],
      'contact' => $params['sel_contact_id']
    );
  } else {
    $users = null;
    if ($default_writable_calendar != null) {
      $users = array($default_writable_calendar['id']);
    }
    else {
      $users = array($obm['uid']);
    }
    $entities = array(
      'user' => $users,
      'resource' => $current_view->get_resources(),
      'contact' => $current_view->get_contacts()
    );
  }
  $organizer_id = is_null($default_writable_calendar) ? null : $default_writable_calendar['id'];
  $display['detail'] = dis_calendar_event_form($action, $params, '', $entities, $current_view, $organizer_id);

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
  if (check_user_defined_rules() && check_calendar_data_form($params) && check_access_entity($entities['user'], $entities['resource']) && check_upload_errors()) {
    try {
      $conflicts = check_calendar_conflict($params, $entities);
      if ( $conflicts && (!$params['force'] || !can_force_resource_conflict($conflicts)) ) {
          if ($conflicts && !can_force_resource_conflict($conflicts)) {
            $params['force_disabled'] = 1;
            $params['force'] = 0;
          }
          $extra_js_include[] = 'inplaceeditor.js';
          $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
          $extra_js_include[] = 'freebusy.js';
          $extra_css[] = $css_ext_color_picker ;
          $display['detail'] .= html_calendar_dis_conflict($params,$conflicts) ;
          $display['msg'] .= display_err_msg("$l_event : $l_insert_error");
          $display['msg'] .= add_upload_warn_message_if_attachments();
          $display['detail'] .= dis_calendar_event_form($action, $params, '',$entities, $current_view, $params['organizer']);
        } else {
          // Insert "others attendees" as private contacts
          if ($params['others_attendees'] != "") {
            $others_attendees = run_query_insert_others_attendees($params);
            $entities['contact'] = array_merge($entities['contact'], $others_attendees);
          }
          // Insert "other files" as private documents
          if (count($params['other_files']) > 0) {
            $other_files = run_query_insert_other_files($params);
            if (!$other_files) {
              $display['msg'] .= display_warn_msg("$l_event : $l_warn_file_upload");
            } else {
              $entities['document'] = array_merge($entities['document'], $other_files);
            }
          }
          $event_id = run_query_calendar_add_event($params, $entities);
          $params["calendar_id"] = $event_id;
          if ($params['date_begin']->compare(Of_Date::today()) <= 0) {
            $display['msg'] .= display_warn_msg("$l_event : $l_warn_date_past");
          }

          if ($params['show_user_calendar']) $current_view->set_users($params['sel_user_id']);
          if ($params['show_resource_calendar'])  $current_view->set_resources($params['sel_resource_id']);

          $current_view->set_date($params["date_begin"]);
          $detailurl = basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=$event_id";
          $detail = "<a class='B' href='$detailurl'>".phpStringToJsString($GLOBALS[l_details])."</a>";
          if($GLOBALS['display']['warm_add_organizer'] == true){
            redirect_warn($params, "$l_event: $l_insert_ok - $l_event_add_organizer - $detail");
	  } else {
            redirect_ok($params, "$l_event: $l_insert_ok - $detail");
          }
        }
      } catch (OverQuotaDocumentException $e) {
        $extra_js_include[] = 'inplaceeditor.js';
        $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
        $extra_js_include[] = 'freebusy.js';
        $extra_css[] = $css_ext_color_picker ;
        $display['msg'] .= display_err_msg("$l_event : $l_over_quota_error");
        $display['msg'] .= add_upload_warn_message_if_attachments();
        $display['detail'] .= dis_calendar_event_form($action, $params, '',$entities, $current_view);
      }
  } else {
    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['msg'] .= add_upload_error_message_too_big();
    $display['msg'] .= add_upload_error_message_other();
    $display['msg'] .= add_upload_warn_message_if_attachments();
    $extra_js_include[] = 'inplaceeditor.js';
    $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
    $extra_js_include[] = 'freebusy.js';
    $extra_css[] = $css_ext_color_picker ;
    $display['detail'] = dis_calendar_event_form($action, $params, '', $entities, $current_view);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id'], 'read')) {
    if($params['errormessage']){
      $display['msg'] .= display_err_msg($params['errormessage']);
      $display['detail'] = dis_calendar_event_consult($params['calendar_id'], $params['date_edit_occurrence']);
    }else{
      $display['detail'] = dis_calendar_event_consult($params['calendar_id'], $params['date_edit_occurrence']);
    }
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }

} elseif ($action == 'detailconsultExt') {
///////////////////////////////////////////////////////////////////////////////
  //FIXME check access right to the external calendar
 // if (check_calendar_access($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult_ext($params['contact_id'], $params['calendar_id']);
  /*} else {
    $display['msg'] .= display_err_msg($err['msg']);
  }*/

}elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    $extra_js_include[] = 'inplaceeditor.js';
    $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
    $extra_js_include[] = 'freebusy.js';
    $extra_css[] = $css_ext_color_picker ;
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $eve_q = run_query_calendar_detail($params['calendar_id'], $params['date_edit_occurrence']);
      $entities = get_calendar_event_entity($eve_q->f('event_id'));
      
	  $display['detailInfo'] = display_record_info($eve_q);
      $display['detail'] = dis_calendar_event_form($action, $params, $eve_q, $entities, $current_view, null, $params['date_edit_occurrence']);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'duplicate') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    $extra_js_include[] = 'inplaceeditor.js';
    $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
    $extra_js_include[] = 'freebusy.js';
    $extra_css[] = $css_ext_color_picker ;
    $eve_q = run_query_calendar_detail($params['calendar_id']);
    $entities = get_calendar_event_entity($params['calendar_id']);
    $display['detailInfo'] = display_record_info($eve_q);
    $display['detail'] = dis_calendar_event_form($action, $params, $eve_q, $entities, $current_view);
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
  if ($params['date_edit_occurrence'] !== null) {
    $params['repeat_kind'] = 'none';
  }

  if (check_user_defined_rules() && check_calendar_access($params["calendar_id"]) && check_calendar_data_form($params) && check_upload_errors()) {
    try {
      $c = get_calendar_event_info($params['calendar_id'],false); 
      $conflicts = check_calendar_conflict($params, $entities);
      if ($conflicts && (!$params['force'] || !can_force_resource_conflict($conflicts))
        && !($c['date']->equals($params['date_begin']) && $c['event_duration'] == $params['event_duration'])) 
      {
        if ($conflicts && !can_force_resource_conflict($conflicts)) {
          $params['force_disabled'] = 1;
          $params['force'] = 0;
        }
        $extra_js_include[] = 'inplaceeditor.js';
        $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
        $extra_js_include[] = 'freebusy.js';
        $extra_css[] = $css_ext_color_picker ;
        $display['detail'] = html_calendar_dis_conflict($params,$conflicts) ;
        $display['msg'] .= display_err_msg("$l_event : $l_update_error");
        $display['msg'] .= add_upload_warn_message_if_attachments();
        $display['detail'] .= dis_calendar_event_form($action, $params, '', $entities, $current_view);
      } else {
        // Insert "others attendees" as private contacts
        if ($params['others_attendees'] != "") {
          $others_attendees = run_query_insert_others_attendees($params);
          $entities['contact'] = array_merge($entities['contact'], $others_attendees);
        }
        // Insert "other files" as private documents
        if (count($params['other_files']) > 0) {
          $other_files = run_query_insert_other_files($params);
          if (!$other_files) {
            $display['msg'] .= display_warn_msg("$l_event : $l_warn_file_upload");
          } else {
            $entities['document'] = array_merge($entities['document'], $other_files);
          }
        }
        if ($params['date_edit_occurrence'] !== null) {
          $params['event_id'] = $params['calendar_id'];
          $eve_q = run_query_calendar_detail($params['calendar_id']);
          $date_occurrence = new Of_Date($params['date_edit_occurrence']);
          $result = run_query_calendar_event_exception_insert($params, $eve_q, true, $date_occurrence, $entities);
          $id = $result['id'];
        }
        else {
          run_query_calendar_event_update($params, $entities, $event_id, $mail_data['reset_state']);
          $id = $params['calendar_id'];
        }

        if ($params['show_user_calendar']) $current_view->set_users($params['sel_user_id']);
        if ($params['show_resource_calendar'])  $current_view->set_resources($params['sel_resource_id']);
        $detailurl = basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=".htmlspecialchars($id);
        $detail = "<a class='B' href='$detailurl'>".phpStringToJsString($GLOBALS[l_details])."</a>";
        if($GLOBALS['display']['warm_add_organizer'] == true){
          redirect_warn($params, "$l_event: $l_update_ok - $l_event_add_organizer - $detail"); 
        } else {
          redirect_ok($params, "$l_event: $l_update_ok - $detail");
        }
      }
    } catch (OverQuotaDocumentException $e) {
      $extra_js_include[] = 'inplaceeditor.js';
      $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
      $extra_js_include[] = 'freebusy.js';
      $extra_css[] = $css_ext_color_picker ;
      $display['msg'] .= display_err_msg("$l_event : $l_over_quota_error");
      $display['msg'] .= add_upload_warn_message_if_attachments();
      $display['detail'] .= dis_calendar_event_form($action, $params, '',$entities, $current_view);
    }
  } else {
    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
    $display['msg'] .= add_upload_error_message_other();
    $display['msg'] .= add_upload_error_message_too_big();
    $display['msg'] .= add_upload_warn_message_if_attachments();
    $extra_js_include[] = 'inplaceeditor.js';
    $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
    $extra_js_include[] = 'freebusy.js';
    $extra_css[] = $css_ext_color_picker ;
    $display['detail'] = dis_calendar_event_form($action, $params, '', $entities, $current_view);
  }

} elseif ($action == 'quick_update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_calendar_access($params['calendar_id']) && 
    check_calendar_data_quick_form($params)) {
    $conflicts = check_calendar_conflict($params, null);
    if(!$conflicts || can_force_resource_conflict($conflicts)) {
      $id = $params['calendar_id'];
      $eve_q = run_query_calendar_detail($id);
      if($eve_q->f('event_repeatkind') == 'none' || $params['all'] == 1) {
        run_query_calendar_quick_event_update($params);
      } else {
        $id = run_query_calendar_event_exception_insert($params,$eve_q,true);
        if ( !$id ) {
	  $id = $params['calendar_id'];
        }
      }
      
      json_build_html_event($params, $current_view, $id);
      $detailurl = basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=$id";
      $detail = "<a class='B' href='$detailurl'>".phpStringToJsString($GLOBALS[l_details])."</a>";
      json_ok_msg("$l_event : $l_update_ok - $detail");
      echo "({".$display['json']."})";
      exit();
    } else {
      json_error_msg($l_overbooking_not_allowed);
      echo "({".$display['json']."})";
      exit();
    }
  } else {
    json_error_msg($l_invalid_data . " : " . $err['msg']);
    echo "({".$display['json']."})";
    exit();
  }

} elseif ($action == 'quick_insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_calendar_data_quick_form($params) && OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'access')) {
    if( OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'write' )) {
      $state = 'ACCEPTED';
    } else {
      $state = 'NEEDS-ACTION';
    }
    $id = run_query_calendar_quick_event_insert($params, $state);
    $params["calendar_id"] = $id;
    json_insert_event_data($id, $params, $current_view);
    $detailurl = basename($_SERVER['SCRIPT_NAME'])."?action=detailconsult&amp;calendar_id=$id";
    $detail = "<a class='B' href='$detailurl'>".phpStringToJsString($GLOBALS[l_details])."</a>";
    json_ok_msg("$l_event : $l_insert_ok - $detail");
    echo "({".$display['json']."})";

  } else {
    echo "<script type=\"text/javascript\">".json_error_msg($l_invalid_data . ' : ' . $err['msg'])."</script>";
  }
  exit();

} elseif ($action == 'quick_delete') {  
///////////////////////////////////////////////////////////////////////////////
  $id = $params['calendar_id'];
  if (check_calendar_access($id)) {
    $eve_q = run_query_calendar_detail($id);
    $response = json_delete_event_data($id, $params, $current_view);
	$deleted_evt_ids = array();
    if($eve_q->f('event_repeatkind') == 'none' || $params['all'] == 1) {
    	$deleted_evt_ids = run_query_calendar_delete($params,false);
    } else {
      	run_query_calendar_event_exception_insert($params,'',true);
      	run_query_increment_sequence($eve_q->f('event_ext_id'));
    }
    $response["error"] = 0;
    $response["message"] = "$l_event : $l_delete_ok";
    $response["deleted_ids"] = $deleted_evt_ids;
    echo "(".json_encode($response).")";
    exit();
  } else {
    json_error_msg($l_invalid_data . " : $err[msg]");
    echo "({".$display['json']."})";
    exit();
  }

} elseif ($action == 'check_update') {
///////////////////////////////////////////////////////////////////////////////
  if(isset($params['calendar_id']) && $params['calendar_id'] != '') {
    $event_q = run_query_calendar_detail($params['calendar_id']);
    $entities = get_calendar_event_entity($params['calendar_id']);
  } else {
    $entities['user']['ids'][] = $params['entity_id'];
    $entities['resource']['ids'] = array();
  }
  $resourceNotification = false;
  if (is_array($entities['resource']['ids'])) {
    foreach($entities['resource']['ids'] as $r_id) {
      if (!OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) $resourceNotification = true;
    }
  }

  if ((!$event_q) || ($event_q->f('event_repeatkind')=='none')) {
    $json[] = 'occUpdate:false';
  } else {
    $json[] = 'occUpdate:true';
  }
  if ($entities['user']['ids'] == array($obm['uid']) && !$resourceNotification ) {
    $json[] = 'mail:false';
  } else {
    $json[] = 'mail:true';
  }
  echo "({".implode(',',$json)."})";
  exit();

} elseif ($action == 'check_conflict') {
///////////////////////////////////////////////////////////////////////////////
  if(isset($params['calendar_id']) && $params['calendar_id'] != '') {
    $event_q = run_query_calendar_detail($params['calendar_id']);
    $entities = get_calendar_event_entity($params['calendar_id']);
  } else {
    $entities['user']['ids'][] = $params['entity_id'];
    $entities['resource']['ids'] = array();
  }
  $resourceNotification = false;
  if (is_array($entities['resource']['ids'])) {
    foreach($entities['resource']['ids'] as $r_id) {
      if (!OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) $resourceNotification = true;
    }
  }
  $conflicts = quick_check_calendar_conflict($params, $entities);
  if ((!$event_q) || ($event_q->f('event_repeatkind')=='none') || !($params['date_begin']->equals($params['old_date_begin']))) {
    $json[] = 'occUpdate:false';
  } else {
    $json[] = 'occUpdate:true';
  }
  if ( (($entities['user']['ids'] == array($obm['uid']) || run_query_calendar_no_mail($entities['user']['ids'])) && count($entities['contact']['ids']) == 0)  && !$resourceNotification) {
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


} elseif ($action == 'quick_check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if(isset($params['calendar_id']) && $params['calendar_id'] != '') {
    $event_q = run_query_calendar_detail($params['calendar_id']);
    $entities = get_calendar_event_entity($params['calendar_id']);
  } else {
    $entities['user']['ids'][] = $params['entity_id'];
    $entities['resource']['ids'] = array();
  }
  $resourceNotification = false;
  if (is_array($entities['resource']['ids'])) {
    foreach($entities['resource']['ids'] as $r_id) {
      if (!OBM_Acl::canWrite($obm['uid'], 'resource', $r_id)) $resourceNotification = true;
    }
  }
  if ((!$event_q) || ($event_q->f('event_repeatkind')=='none')) {
    $json[] = 'occDelete:false';
    $json[] = 'checkDelete:true';
  } else {
    $json[] = 'occDelete:true';
    $json[] = 'checkDelete:false';
  }
  if ($entities['user']['ids'] == array($obm['uid']) && !$resourceNotification && count($entities['contact']['ids']) == 0 ) {
    $json[] = 'mail:false';
  } else {
    $json[] = 'mail:true';
  }
  $json[] = "conflict:false";
  echo "({".implode(',',$json)."})";    
  exit();

} elseif ($action == 'update_comment') {
///////////////////////////////////////////////////////////////////////////////
  $GLOBALS["send_notification_mail"] = true;
  $comment_inserted = run_query_calendar_event_comment_insert($params['calendar_id'],
    $params['user_id'],$params['comment'],$params['type'], true);
  if ($comment_inserted) {
    json_ok_msg("$l_event : $l_update_ok");
  } else {
    json_error_msg("$l_event : $err[msg]");
  }
  echo "({".$display['json']."})";
  exit();

} elseif ($action == 'update_decision_and_comment') {
///////////////////////////////////////////////////////////////////////////////
  if ( $params['date_edit_occurrence'] ) {
    try {
      $exception_array = create_new_exception($params, $obm['uid']);
    }
    catch (AccessException $ex) {
      json_error_msg("$l_event : $err[msg]");
      echo "({".$display['json']."})";
      exit();
    }
    catch (DBUpdateException $ex) {
      json_error_msg("$l_event : $err[msg]");
      echo "({".$display['json']."})";
      exit();
    }
  }
  try {
    if (isset($exception_array)) {
      if ($exception_array['already_set']){
        $l_exception_already_set = $GLOBALS['l_exception_already_set'];
        json_error_msg( $l_event." : ".$l_exception_already_set );
        echo "({".$display['json']."})";
        exit();
      } else {
        $params['calendar_id'] = $exception_array['id'];
      }
    }
    $GLOBALS["send_notification_mail"] = true;
    $comment_and_decision_updated = update_decision_and_comment($params, $obm['uid']);
    if ($comment_and_decision_updated) {
      json_ok_msg("$l_event : $l_update_ok");
    }
  }
  catch (ConflictException $ex) {
    json_error_msg("$l_event : $l_conflicts");
      
    $obm_q = run_query_calendar_detail($params['calendar_id']);
    $begin = new Of_Date($obm_q->f('event_date'), 'GMT');
    $end = clone $begin;
    $end->addSecond($obm_q->f('event_duration'));
    $date_begin = $begin->getURL();
    $date_end = $end->getURL();
    $time_end = $end->getHour();
    $min_end = $end->getMinute();

    $redirectUrl=$_SERVER['SCRIPT_NAME']."?action=decision&calendar_id=".$params['calendar_id'].
      "&entity_kind=user&entity_id=".$params['entity_id']."&owner_notification=true&date_begin=".
      $date_begin."&date_end=".$date_end."&time_end=".$time_end."&min_end=".$min_end."&rd_decision_event=".$params['decision_event']."&uriAction=".$params['uriAction'];
    echo "({".$display['json'].", \"redirectUrl\" : \"$redirectUrl\"})";
    exit();
  }
  catch (Exception $ex) {
    json_error_msg("$l_event : $err[msg]");
  }
  if( $params['date_edit_occurrence']){
    $redirectUrl=$_SERVER['SCRIPT_NAME']."?action=detailconsult&calendar_id=".$params['calendar_id'];
    echo "({".$display['json'].", \"redirectUrl\" : \"$redirectUrl\"})";
    exit();
  }
  if($params['uriAction'] == 'detailconsult'){
    if (empty($display['json'])){ $display['json'] = "error:0,message:'Evénement : Mise à jour réussie'";}
    $redirectUrl=$_SERVER['SCRIPT_NAME']."?action=detailconsult&calendar_id=".$params['calendar_id'];
    echo "({".$display['json'].", \"redirectUrl\" : \"$redirectUrl\"})";
    exit();
  }
  echo "({".$display['json']."})";
  exit();

} elseif ($action == 'update_decision') {
///////////////////////////////////////////////////////////////////////////////
  if (empty($params['entity_id']) && $params['entity_kind'] == 'user') {
    $params['entity_id'] = $obm['uid'];
  }  
  //we want to send mails
  $GLOBALS["send_notification_mail"] = true;
  if (check_calendar_event_participation($params)) {
    if (!$params['force'] && $conflicts = check_calendar_decision_conflict($params)) {
      $display['msg'] .= display_warn_msg("$l_event : $l_conflicts");
    }
    $params['conflicts'] = $conflicts;
    if (check_calendar_participation_decision($params)) {
    	$event_q = run_query_calendar_detail($params['calendar_id']);
      if(($event_q->f('event_repeatkind')=='none') || $params['all'] == 1) {
        $retour = run_query_reset_comment_entity_event($params['entity_kind'], $params['entity_id'], $params['calendar_id']);
      	$retour = run_query_calendar_update_occurrence_state($params['calendar_id'], $params['entity_kind'], $params['entity_id'],$params['decision_event']);
      } else {
        $retour = run_query_reset_comment_recurrent_event($params['entity_kind'], $params['entity_id'], $params['calendar_id']);
      	$retour = run_query_calendar_update_occurrence_state($params['calendar_id'], $params['entity_kind'], $params['entity_id'],$params['decision_event'], true);
      }
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
     json_ok_msg("$l_event : $l_update_ok");
  } else {
     json_ok_msg("$l_event : $err[msg]");
     echo "({".$display['json']."})";
     exit();
  }
  if (check_calendar_access($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
    echo "({".$display['json']."})";
    exit();
  } else {
    json_ok_msg("$l_event : $err[msg]");
  }
  echo "({".$display['json']."})";
  exit();

} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access($params['calendar_id'])) {
    $display['detail'] = html_calendar_dis_delete($current_view, $params['calendar_id'], $params['date_edit_occurrence']);
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
  $id = $params['calendar_id'];
  if (check_calendar_access($id)) {
    if(!isset($params['exception_date'])) {
        run_query_calendar_delete($params,false);
        OBM_IndexingService::delete('event', $id);
    } else {
        $params['old_date_begin'] = of_isodate_convert($params['exception_date'],true);
        if(!is_null($params['old_date_begin'])) {
            $params['old_date_begin'] = new Of_Date($params['old_date_begin']);
        }  
        run_query_calendar_event_exception_insert($params,'',true);
    }
    redirect_ok($params, "$l_event: $l_delete_ok");
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
  
} elseif ($action == 'attach_documents') {
///////////////////////////////////////////////////////////////////////////////
	if (check_upload_errors()) {
	  try {
	    $doc_ids = isset($params['sel_document_id']) ? $params['sel_document_id'] : array();
	    $existent_doc_ids = get_calendar_event_document_ids($params['calendar_id']);
	    $already_attached_doc_ids = array_intersect($doc_ids, $existent_doc_ids);
	    $doc_ids = array_diff($doc_ids, $existent_doc_ids);
	    $other_files = run_query_insert_other_files($params);
	    if (!$other_files) {
	      $display['msg'] .= display_warn_msg("$l_event : $l_warn_file_upload");
	    } else {
	      $doc_ids = array_merge($doc_ids, $other_files);
	    }
	    $event_entity_id = of_entity_get('event', $params['calendar_id']);
	    foreach ($doc_ids as $document_id) {
	      run_query_calendar_attach_document($document_id, $event_entity_id);
	    }
	    if (count($already_attached_doc_ids) != 0) {
	      redirect_err($params, $l_warn_already_attached);
	    } else {
	      redirect_ok($params, "$l_document: $l_insert_ok");
	    }
	  } catch (OverQuotaDocumentException $e) {
	    redirect_err($params, $l_over_quota_error);
	  }
	} else {
	    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
	    $display['msg'] .= add_upload_error_message_too_big();
	    $display['msg'] .= add_upload_warn_message_if_attachments();
	    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
	}
} elseif ($action == 'download_document') {
///////////////////////////////////////////////////////////////////////////////
  require '../document/document_query.inc';

  if (!check_user_attendance($params['event_id'], $obm['uid'])
    || !in_array($params['document_id'], get_calendar_event_document_ids($params['event_id']))) {
    $display['msg'] .= display_err_msg("$l_err_file_access_forbidden");
  } else {
    $doc_q = run_query_document_detail($params['document_id']);
    if ($doc_q->num_rows() == 1) {
      dis_document_file($doc_q);
    } else {
      $display['msg'] .= display_err_msg("$l_no_document !");
    }
  }

} elseif ($action == 'rights_admin') {
///////////////////////////////////////////////////////////////////////////////
  $peer_profile_id = get_user_profile_id($params['entity_id']);
  if (canUpdateCalendarRights($obm, $params, $profiles, $peer_profile_id)) {
    $display['detail'] = dis_calendar_right_dis_admin($params['entity_id']);
  }
  else{
    $err['msg'] = $l_insufficient_permission;
    $display['msg'] .= display_err_msg($err['msg']);
  }

} elseif ($action == 'rights_update') {
///////////////////////////////////////////////////////////////////////////////
  $peer_profile_id = get_user_profile_id($params['entity_id']);
  if (canUpdateCalendarRights($obm, $params, $profiles, $peer_profile_id) &&
      OBM_Acl_Utils::updateRights('calendar', $params['entity_id'], $obm['uid'], $params)) {
    $display['msg'] .= display_ok_msg("$l_rights : $l_update_ok");
  } else {
    $display['msg'] .= display_warn_msg($l_of_right_err_auth);
  }
  $display['detail'] = dis_calendar_right_dis_admin($params['entity_id']);

} elseif ($action == 'new_meeting')  {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'freebusy.js';
  $display['detail'] = dis_calendar_meeting_form($current_view, $params);

} elseif ($action == 'admin')  {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] = dis_calendar_admin_index($tags_q);

} elseif ($action == 'tags_update')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tag_form($params)) {
    $retour = run_query_tag_update($obm['uid'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_tag : $l_update_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_tag : $l_update_error");
    }
  }
  else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);
  
} elseif ($action == 'tag_insert')  {
///////////////////////////////////////////////////////////////////////////////
  if (check_tag_form($params)) {
    $retour = run_query_tag_insert($obm['uid'], $params);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_tag : $l_insert_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_tag : $l_insert_error");
    }
  }
  else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
  $tags_q = run_query_calendar_get_alltags($obm['uid']) ;
  $display['detail'] .= dis_calendar_admin_index($tags_q);
  
} elseif ($action == 'tag_delete')  {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_tag_delete($obm['uid'], $params);
  if ($retour) {
    $display['msg'] .= display_ok_msg("$l_tag : $l_delete_ok");
  } else {
    $display['msg'] .= display_err_msg("$l_tag : $l_delete_error");
  }
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
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
    if(run_query_check_exists_template_by_name($params['template_name'])){
       $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $l_event_templates_already_exists);
    }
    else{
      $params['template_id'] = run_query_calendar_create_or_update_event_template($params);
      $display['msg'] .= display_ok_msg("$l_template : $l_insert_ok");
    }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
  }
  foreach (array('user', 'group', 'resource', 'contact', 'document') as $type) {
    $entities[$type] = is_array($params["sel_{$type}_id"]) ? $params["sel_{$type}_id"] : array();
  }
  $extra_js_include[] = 'inplaceeditor.js';
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_js_include[] = 'freebusy.js';
  $extra_css[] = $css_ext_color_picker ;
  $display['detail'] = dis_calendar_event_form($action, $params, '', $entities, $current_view);
  
} elseif ($action == 'edit_template') {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
  list($template_q, $entity_ids) = run_query_calendar_get_template($params['template_id']);
  $display['detail'] = dis_calendar_template_form($action, $params, $template_q, $entity_ids, $current_view);
    
} elseif ($action == 'update_template') {
///////////////////////////////////////////////////////////////////////////////
  $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
  $extra_css[] = $css_ext_color_picker ;
  if (check_calendar_data_form($params)) {
    $template_id = run_query_calendar_create_or_update_event_template($params);
    $display['msg'] .= display_ok_msg("$l_template : $l_update_ok");
    $templates_q = run_query_calendar_get_alltemplates($obm['uid']);
    $display['detail'] = dis_calendar_templates_list($templates_q);
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . ' : ' . $err['msg']);
    list($template_q, $entity_ids) = run_query_calendar_get_template($params['template_id']);
    $display['detail'] = dis_calendar_template_form('update_template', $params, $template_q, $entity_ids, $current_view);
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
  
} elseif ($action == 'export_template')  {
///////////////////////////////////////////////////////////////////////////////
  list($name, $xml) = xml_calendar_export_templates($params['template_id']);
  header('Content-Type: text/xml');
  header('Content-Disposition: attachment; filename="template_'.$name.'.xml"');
  echo $xml->flush();
  exit();
  
} elseif ($action == 'export_all_templates')  {
///////////////////////////////////////////////////////////////////////////////
  $xml = xml_calendar_export_templates();
  // if there is only one template, the previous function returns an array
  if (is_array($xml)) {
    list(, $xml) = $xml;
  }
  header('Content-Type: text/xml');
  header('Content-Disposition: attachment; filename="event_templates.xml"');
  echo $xml->flush();
  exit();
  
} elseif ($action == 'import_template')  {
///////////////////////////////////////////////////////////////////////////////
  if ($_FILES['template_file']['tmp_name']) {
    libxml_use_internal_errors(true);
    $xml = simplexml_load_file($_FILES['template_file']['tmp_name']);
    if (!$xml) {
      $display['msg'] .= display_err_msg("$l_template : $l_err_template_xml_import");
    } else {
      $retour = run_query_calendar_import_template($xml);
      if ($retour) {
        redirect_ok($params, "$l_template : $l_import_ok");
      } else {
        $display['msg'] .= display_err_msg("$l_template : $l_err_template_xml_import");
      }
    }
  } else {
    $display['msg'] .= display_err_msg("$l_template : $l_file_error");
  }
  $templates_q = run_query_calendar_get_alltemplates($obm['uid']);
  $display['detail'] = dis_calendar_templates_list($templates_q);
  
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
  if (!empty($params["ics_tmp"])) {
    if(   !array_key_exists("fi_ics", $_FILES)
     || !file_exists($_FILES["fi_ics"]["tmp_name"])
  ){
    $result = false;
  }
  else{
    $ics = file_get_contents($_FILES["fi_ics"]["tmp_name"]);
    $result = run_icalendar_obmsync_insert($ics) ;
  }
    if($result !== false) {
      if (!empty($result[1]) && empty($result[0])){
        $display['msg'] .= display_warn_msg(
                              "$result[1] : ".
                              __("events from this ics could not be imported")
                           );
      }
      else{
        $display['msg'] .= display_ok_msg("$result[0] $l_ics_import_ok");
      }
      $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
    } else {
      $display['msg'] .= display_err_msg("$l_file_format $l_unknown");
      $display['detail'] .= dis_icalendar_import($params);
    }
  } else {
    $display['msg'] .= display_err_msg("$l_file_format $l_unknown");
    $display['detail'] .= dis_icalendar_import($params);
  }

} elseif ($action == 'pdf_export_form') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_pdf_options($current_view);

} elseif ($action == 'pdf_export') {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lib/Zend/Pdf.php");
  dis_calendar_pdf_view($params, $current_view);
  exit();

} elseif ($action == 'conflict_manager') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['calendar_id'] > 0) {  
    if (check_calendar_access($params['calendar_id'], 'read')) {
      $eve_q = run_query_calendar_detail($params['calendar_id']);
      $entities = get_calendar_event_entity($params['calendar_id']);
      $attendees = run_query_get_events_attendee(array($params['calendar_id'])); 
      while ($attendees->next_record()) {
        $conflicts_entities[$attendees->f('eventlink_entity')][] = $attendees->f('eventlink_entity_id');
      } 
      $conflicts = check_calendar_conflict($params, $conflicts_entities);
      $extra_js_include[] = 'inplaceeditor.js';
      $extra_js_include[] = 'mootools/plugins/mooRainbow.1.2b2.js' ;
      $extra_js_include[] = 'freebusy.js';      
      $extra_css[] = $css_ext_color_picker ;
      $display['detail'] = html_calendar_dis_conflict($params, $conflicts) ;
      $display['detail'] .= dis_calendar_event_form($action, $params, $eve_q, $entities, $current_view);
    } else {
      $display['msg'] .= display_err_msg($err['msg']);
    }
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

} elseif ($action == 'portlet') {
///////////////////////////////////////////////////////////////////////////////
  $display['head'] = display_head($l_calendar);
  if (isset($params['list'])) {
    $calendar_entity = $current_view->get_entities();
    $writable_entity = OBM_Acl_Utils::expandEntitiesArray(
      OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', 'write')
    );
    $display['detail'] = dis_calendar_day_list($current_view, $calendar_entity, $writable_entity);
  } else if (isset($params['waiting'])) {
    $display['detail'] = dis_calendar_waiting_portlet();
  } else if (isset($params['task'])) {
    $display['detail'] = dis_calendar_task_portlet();
  }else {
    $display['detail'] = dis_calendar_calendar_view($params, $current_view);
  }
  $display['title'] = dis_portlet_navbar($params, $current_view);
  display_page($display);
  exit();

///////////////////////////////////////////////////////////////////////////////
} elseif ($action == 'draw') {
  echo dis_calendar_draw($current_view, $params['ndays']);
  // Store current_view in session
  $_SESSION['cal_current_view'] = serialize($current_view);
  exit();

} elseif ($action == 'async_indexing') {
///////////////////////////////////////////////////////////////////////////////
  if ($params['remove']) {
    OBM_IndexingService::delete('event', $params['id']);
  } else {
    run_query_calendar_solr_store($params['id']);
  }
  exit();

} elseif ($action == 'share_calendar') {
///////////////////////////////////////////////////////////////////////////////
  if(OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'admin' )) {
    run_query_calendar_update_token($params);
    $display['msg'] .= display_ok_msg("$l_shares : $l_update_ok");
  } else {
    $display['msg'] .= display_warn_msg($l_share_err_auth);
  }
  $display['detail'] = dis_calendar_right_dis_admin($params['entity_id']);
  
} elseif ($action == 'add_shared_calendar') {
///////////////////////////////////////////////////////////////////////////////
  $url_parts = parse_url($params['shared_calendar_url']);
  $url_params = array();
  if (isset($url_parts['query'])) {
    parse_str($url_parts['query'], $url_params);
  }
  $url_params['url'] = $params['shared_calendar_url'];
  if (isset($url_params['email'])) {
    $displayed_contacts = $current_view->get_contacts();
    $displayed_contacts[] = run_query_add_contact_shared_calendar($url_params);
    $current_view->set_contacts($displayed_contacts);
    redirect_ok($params, "$l_add_shared_calendar_ok");
  } else {
    $url = "$path/contact/contact_index.php?action=updateContact&shared_calendar_url=";
    $url .= urlencode($params['shared_calendar_url']);
    redirect_to($url);
  }
}

$_SESSION['cal_current_view'] = serialize($current_view);

if (!$params['ajax']) {
  $display['head'] = display_head($l_calendar);
  $display['header'] = display_menu($module);
  $display['end'] = display_end();
  if ($display['search'] == '') {
    $display['search'] = dis_back_to_calendar($action);
  }

} elseif ($action == 'insert_view') {
///////////////////////////////////////////////////////////////////////////////
  $view = clone $current_view;
  $view->set_label($params['view_label']);
  $view->save();
  $view_id = $view->get_id();
  $view_label = $view->get_label();
  $obmbookmarkproperty = "set_cal_view_id=$view_id";
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
  $entities = array();
  $entities['user'] = is_array($params['sel_user_id']) ? $params['sel_user_id'] : array();
  $entities['resource'] = is_array($params['sel_resource_id']) ? $params['sel_resource_id'] : array();
  if (!is_array($entities['user']) || count($entities['user'])==0)
    $entities['user'] = array($obm['uid']);
  $ret = get_calendar_entity_label($entities);
  $ret['resourcegroup'] = run_query_resource_resourcegroup($params['sel_resource_group_id']);
  $entity_store = store_calendar_entities($ret);
  get_json_entity_events($params, $entity_store);
  echo "({".$display['json']."})";
  exit();

} elseif($action == 'get_json_waiting_events') {
///////////////////////////////////////////////////////////////////////////////
  get_json_waiting_events($obm['uid']);
  echo '('.$display['json'].')';
  exit();

} elseif ($action == 'perform_meeting')  {
///////////////////////////////////////////////////////////////////////////////
  echo dis_calendar_free_interval($current_view, $params);
  exit();

} elseif ($action == 'set_entity_class')  {
///////////////////////////////////////////////////////////////////////////////
  $current_view->set_entity_class($params['entity_type'],$params['entity_id'],$params['entity_class']);
  $_SESSION['cal_current_view'] = serialize($current_view);
  json_ok_msg("$l_view : $l_insert_ok");
  echo "({".$display['json'].",$msg})";
  exit();
} elseif ($action == 'share') {
///////////////////////////////////////////////////////////////////////////////
  if(OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'admin' ) || check_calendar_update_rights($params) ) {
    $token = get_calendar_entity_share($params['entity_id'],$params['entity_type'],$params['type']);
    $loginAtDomain = $obm['login']."@".$obm['domain_name'];
    dis_calendar_share_public($token, $loginAtDomain);
    json_ok_msg("$l_share_calendar : $l_share_ok");
  } else {
    json_error_msg("$l_rights : $l_of_right_err_user");
  }
  if(is_null($msg))
    echo "({".$display['json']."})";
  else
    echo "({".$display['json'].",$msg})";
  exit();

} elseif ($action == 'share_reinit') {
///////////////////////////////////////////////////////////////////////////////
  if(OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'admin' ) || check_calendar_update_rights($params)) {
    run_query_calendar_delete_token($params['entity_id'],$params['entity_type'],$params['type']);
    json_ok_msg("$l_share_calendar : $l_reinit_ok");
  } else {
    json_error_msg("$l_rights : $l_of_right_err_user");
  }
  echo "({".$display['json'].",$msg})";
  exit();

} elseif ($action == 'send_url') {
///////////////////////////////////////////////////////////////////////////////
  if(OBM_Acl::areAllowed($obm['uid'], 'calendar',array($params['entity_id']), 'admin' ) || check_calendar_update_rights($params)) {
  	$format = $params['format'];
    $params['others_attendees'][]=$params['mail'];
    $entity = get_user_info($params['entity_id']);
    $entity['token'] = get_calendar_entity_share($params['entity_id'],$params['entity_type'],$params['type']);
    run_query_insert_others_attendees($params);
    $sharemail = new shareCalendarMailer();
    $sharemail->addRecipient($params['mail']);
    $sharemail->send("userShare$format",array($entity));
    json_ok_msg("$l_share_calendar : $l_mail_ok");
  } else {
    json_error_msg("$l_rights : $l_of_right_err_user");
  }
  echo "({".$display['json'].",$msg})";
  exit();

}

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Calendar parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_calendar_params() {
  global $ccalendar_first_hour, $ccalendar_last_hour, $obm;

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

    if(!is_null($params['repeat_end'])) {
      # Don't remove the two lines below. The side effect is that this sets the
      # end date right.
      $params['repeat_end']->setHour($params['date_begin']->getHour());
      $params['repeat_end']->setMinute($params['date_begin']->getMinute());
    }
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
  if ( $params["repeat_kind"] ) {
    $params["repeat_days"] = "";
    if ( $params["repeat_kind"] == "weekly" ) {
      for ($i=0; $i<7; $i++) {
        if (isset($params["repeatday_$i"])) {
          $params["repeat_days"] .= '1';
        } else {
          $params["repeat_days"] .= '0';
        }
      }
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

  if ($params['organizer']=='') {
    $params['organizer'] = $obm['uid'];
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
      } else {
        // direct id
        $params['sel_user_id'][] = $value;
      }
    }
  }

  // sel_contact_id can be filled by sel_contact_id or sel_ent (see below)
  if (is_array($params['contact_id'])) {
    while (list($key, $value) = each($params['contact_id'])) {
      // sel_user_id contains select infos (data-user-$id)
      if (strcmp(substr($value, 0, 13),'data-contact-') == 0) {
        $data = explode('-', $value);
        $id = $data[2];
        $params['sel_contact_id'][] = $id;
      } else {
        // direct id
        $params['sel_contact_id'][] = $value;
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
  global $l_header_waiting_events, $l_calendar, $l_header_templates, $l_edit_template;

  $id = $params['calendar_id'];
  $date = $params['date'];
  if (isset($params['date_edit_occurrence'])){
    $exception_date="&amp;date_edit_occurrence=".$params['date_edit_occurrence'];
  }

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
  $actions['calendar']['check_update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=check_update",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Looking for Conflicts
  $actions['calendar']['check_conflict'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=check_conflict",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Looking for Delete Conflicts
  $actions['calendar']['quick_check_delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=quick_check_delete",
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
    'Redirection' => "$_SERVER[PHP_SELF]",
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
    'Condition'=> array ('all')
  );

  // Detail Consult
  $actions['calendar']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/calendar/calendar_index.php?action=detailconsult&amp;calendar_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );
  
  // Detail Consult External Event
  $actions['calendar']['detailconsultExt'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/calendar/calendar_index.php?action=detailconsultExt&amp;calendar_id=$id",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Detail Update
  $actions['calendar']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/calendar/calendar_index.php?action=detailupdate&amp;calendar_id=$id$exception_date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult','update_alert','update_decision', 'update_ext_decision') 
  );

  // Duplicate
  $actions['calendar']['duplicate'] = array (
    'Name'     => $l_header_duplicate,
    'Url'      => "$path/calendar/calendar_index.php?action=duplicate&amp;calendar_id=$id",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
  );

  // Check Delete
  $actions['calendar']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/calendar/calendar_index.php?action=check_delete&amp;calendar_id=$id$exception_date",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult')
  );

  // Delete
  $actions['calendar']['delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=delete&amp;calendar_id=$id",
    'Right'    => $cright_write,
    'Redirection' => "$_SERVER[PHP_SELF]",
    'Condition'=> array ('None')
  );

  // Insert
  $actions['calendar']['insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=insert",
    'Right'    => $cright_write,
    'Redirection' => "$_SERVER[PHP_SELF]",
    'Condition'=> array ('None') 
  );


  // Update
  $actions['calendar']['update'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update",
    'Right'    => $cright_write,
    'Redirection' => "$_SERVER[PHP_SELF]",
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
  // Update comment
  $actions['calendar']['update_comment'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update_comment",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
    // Update comment
  $actions['calendar']['update_decision_and_comment'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=update_comment",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  // Waiting events
  $actions['calendar']['waiting_events'] = array (
    'Name'     => $l_header_waiting_events,
    'Url'      => "$path/calendar/calendar_index.php?action=waiting_events",
    'Right'    => $cright_write,
    'Condition'=> array ('None')
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
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Tag Insert
  $actions['calendar']['tag_insert'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Tag Delete
  $actions['calendar']['tag_delete'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Tag Search
  $actions['calendar']['tag_search'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=tag_search",
    'Right'    => $cright_write,
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
  
  // Attach documents
  $actions['calendar']['attach_documents'] = array (
    'Right'    => $cright_write,
    'Redirection' => "$path/calendar/calendar_index.php?action=detailconsult&calendar_id=$id", 
    'Condition'=> array ('None')
  );
  
  // Download documents
  $actions['calendar']['download_document'] = array (
    'Right'    => $cright_read,
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
  
  // Edit template
  $actions['calendar']['edit_template'] = array (
    'Name'     => $l_edit_template,
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Update template
  $actions['calendar']['update_template'] = array (
    'Name'     => $l_edit_template,
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Export template
  $actions['calendar']['export_template'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Export all templates
  $actions['calendar']['export_all_templates'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );
  
  // Import templates
  $actions['calendar']['import_template'] = array (
    'Right'    => $cright_write,
    'Redirection' => "$path/calendar/calendar_index.php?action=list_templates", 
    'Condition'=> array ('None') 
  );
  
  // Delete template
  $actions['calendar']['delete_template'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
  );

  // Portlet
  $actions['calendar']['portlet'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=portlet",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Refresh 
  $actions['calendar']['draw'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=draw",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Async indexing 
  $actions['calendar']['async_indexing'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=async_indexing",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Generate public url
  $actions['calendar']['share'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=share",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Share calendar
  $actions['calendar']['share_reinit'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=share_reinit",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Send url calendar
  $actions['calendar']['send_url'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=send_url&ajax=1",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
  
  // Add shared calendar
  $actions['calendar']['add_shared_calendar'] = array (
    'Url'      => "$path/calendar/calendar_index.php?action=add_shared_calendar",
    'Right'    => $cright_read,
    'Condition'=> array ('None'),
    'Redirection' => "$_SERVER[PHP_SELF]"
  );

}


///////////////////////////////////////////////////////////////////////////////
// Calendar Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_calendar_action($writable_calendars) {
  global $actions, $params, $path, $obm;
  if (!$writable_calendars) {
      unset($actions['calendar']['new']);
  }
  $id = $params['calendar_id'];
  $user_calendar = $_GET['user_id'];
  $user_logged = $obm['uid'];
  $not_calendar_of_logged_user = (isset($user_calendar) && $user_calendar != $user_logged);
  if($id) {
    $event_info = get_calendar_event_info($id);
    $owner = $event_info['owner'];
    $organizerUserObmId = get_event_organizer_userObm_id($id);
    if ( !$organizerUserObmId || $not_calendar_of_logged_user || $owner != $user_logged && !OBM_Acl::canWrite($user_logged, 'calendar', $owner)) {
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

function add_upload_warn_message_if_attachments()
{
  global $l_other_files_detached;

  $result = '';
  $filename_list = array();

  if (isset($_FILES['fi_other_files']['name']) && !empty($_FILES['fi_other_files']['name']) && is_array($_FILES['fi_other_files']['name'])) {
    $has_files = false;
    foreach ($_FILES['fi_other_files']['name'] as $k => $filename) {
      if (!empty($filename) && $_FILES['fi_other_files']['error'][$k] === UPLOAD_ERR_OK) {
        $has_files = true;
        $filename_list[] = $filename;
      }
    }
    if ($has_files) {
      $result .= display_warn_msg("$l_other_files_detached");
      $result .= "<strong>" . display_warn_msg(implode(", ", $filename_list)) . "</strong>";
    }
  }
  return $result;
}

function check_upload_errors() {
	global $err, $l_error_upload;

	$no_errors = true;
	if (isset($_FILES['fi_other_files']['name']) && !empty($_FILES['fi_other_files']['name']) && is_array($_FILES['fi_other_files']['name'])) {
		foreach ($_FILES['fi_other_files']['name'] as $k => $filename) {
			if (!empty($filename) && $_FILES['fi_other_files']['error'][$k] !== UPLOAD_ERR_OK) {
				$no_errors = false;
				$err['msg'] = $l_error_upload;
			}
		}
	}
	return $no_errors;
}

function add_upload_error_message_too_big()
{
	global $l_other_files_upload_error, $c_quota_private_document;

	$result = '';
	$filename_list = array();
	$ini_max_filesize = upload_max_filesize_to_bytes(ini_get('upload_max_filesize'));

	if (isset($_FILES['fi_other_files']['name']) && !empty($_FILES['fi_other_files']['name']) && is_array($_FILES['fi_other_files']['name'])) {
		$is_error = false;
		foreach ($_FILES['fi_other_files']['name'] as $k => $filename) {
			if (!empty($filename) && $_FILES['fi_other_files']['error'][$k] === UPLOAD_ERR_INI_SIZE) {
				$is_error = true;
				$filename_list[] = $filename;
			}
		}
		if ($is_error) {
			$result .= display_err_msg("$l_other_files_upload_error (" . dis_filesize(min($ini_max_filesize, $c_quota_private_document)) . ") :");
			$result .= "<strong>" . display_err_msg(implode(", ", $filename_list)) . "</strong>";
		}
	}
	return $result;
}

function add_upload_error_message_other()
{
	global $l_other_files_upload_error_other;

	$result = '';
	$filename_list = array();

	if (isset($_FILES['fi_other_files']['name']) && !empty($_FILES['fi_other_files']['name']) && is_array($_FILES['fi_other_files']['name'])) {
		$is_error = false;
		foreach ($_FILES['fi_other_files']['name'] as $k => $filename) {
			if (!empty($filename) && $_FILES['fi_other_files']['error'][$k] !== UPLOAD_ERR_OK && $_FILES['fi_other_files']['error'][$k] !== UPLOAD_ERR_INI_SIZE) {
				$is_error = true;
				$filename_list[] = $filename;
			}
		}
		if ($is_error) {
			$result .= display_err_msg("$l_other_files_upload_error_other :");
			$result .= "<strong>" . display_err_msg(implode(", ", $filename_list)) . "</strong>";
		}
	}
	return $result;
}

function update_decision_and_comment($params, $user_id) {
  $calendar_id = $params['calendar_id'];
  $entity_id = $params['entity_id'];
  $comment = $params['comment'];
  $entity_kind = $params['entity_kind'];
  $decision_event = $params['decision_event'];

  $comment_inserted = run_query_calendar_event_comment_insert($calendar_id,
    $entity_id, $comment, $entity_kind, false);
  if (!$comment_inserted) {
    throw new DBUpdateException("Can't insert comment");
  }
  $owner_id = empty($entity_id) && $entity_kind == 'user' ? $user_id : $entity_id;
  if (!check_calendar_event_participation($params)) {
    return false;
  }
  if (!$params['force']) {
    $conflicts = check_calendar_decision_conflict($params);
    if ($conflicts) {
      throw new ConflictException();
    }
  }
  $params['conflicts'] = false;
  if (!check_calendar_participation_decision($params)) {
    throw new AccessException();
  }
  $event_q = run_query_calendar_detail($calendar_id);
  $isRecurrent = $event_q->f('event_repeatkind')!='none' && $params['all'] != 1;
  $GLOBALS["send_notification_mail"] = true;
  if (!run_query_calendar_update_occurrence_state($calendar_id, $entity_kind, $owner_id, $decision_event,
          $isRecurrent, $params['owner_notification'])) {
    throw new DBUpdateException();
  }
  return true;
}

function create_new_exception($params, $user_id) {
  $reccurrence_q = run_query_calendar_detail($params['calendar_id']);
  $event_id = $reccurrence_q->Record['event_id'];
  if ( check_calendar_access( $event_id, 'read' ) ){
    $calendar = $reccurrence_q->Record;
    $date_occurrence = new Of_Date($params['date_edit_occurrence']);
    $send_mail = false;
    $exception_insert = run_query_calendar_event_exception_insert( $calendar, $reccurrence_q, $send_mail ,$date_occurrence);
  } else {
    throw new AccessException();
  }
  if ( !$exception_insert ) {
    throw new DBUpdateException();
  }
  return $exception_insert;
}

class DBUpdateException extends Exception {}

class ConflictException extends Exception {}

class AccessException extends Exception {}

function canUpdateCalendarRights($obm, $params, $profiles, $peer_profile_id) {
    return (Obm_Acl::isAllowed($obm['uid'], 'calendar', $params['entity_id'], "admin") ||
        check_calendar_update_rights($params) ||
        Perm::user_can_update_peer($obm['uid'], $profiles[$obm['profile']], $params['entity_id'], $profiles[$peer_profile_id]));
}
?>
