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
// $Id: calendar_index.php 5645 2009-12-16 15:01:48Z david $ //
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
$cgp_cookie_name = 'OBM_Public_Session';
page_open(array('sess' => 'OBM_Session', 'auth' => 'OBM_Token_Auth', 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
require('calendar_query.inc');
require_once("$obminclude/of/of_contact.php");

$params = get_calendar_params();
// Get user preferences if set for hour display range 
if (isset($_SESSION['set_cal_first_hour'])) {
  $ccalendar_first_hour = $_SESSION['set_cal_first_hour']; 
}
if (isset($_SESSION['set_cal_last_hour'])) {
  $ccalendar_last_hour = $_SESSION['set_cal_last_hour'];
}
if (!($current_view instanceOf CalendarView) && isset($_SESSION['cal_current_view']) && (is_string($_SESSION['cal_current_view']))) {
  $current_view = unserialize($_SESSION['cal_current_view']);
}
if(!($current_view instanceOf CalendarView)) {
  $current_view = new CalendarView(array(
    // default view settings
    'cal_view'  => 'agenda',
    'cal_range' => 'week',
    'category'  => $c_all,
    $GLOBALS['token']['entity'].'s'  => array($GLOBALS['token']['entityId'])
  ));
} elseif($GLOBALS['token']['entity'] && $GLOBALS['token']['entityId']) {
  $current_view->remove_all();
  $current_view->add_entity($GLOBALS['entity'], $GLOBALS['entityId']);
}
if (isset($params['cal_view'])) {
  $current_view->set_cal_view($params['cal_view']);
}
if (isset($params['cal_range'])) {
  $current_view->set_cal_range($params['cal_range']);
}
if (isset($params['date']) && !empty($params['date'])) {
  $current_view->set_date($params['date']);
}
///////////////////////////////////////////////////////////////////////////////

$extra_css[] = $css_calendar;
$extra_js_include[] = 'date.js';
$extra_js_include[] = 'calendar.js';
$extra_js_include[] = 'colorchooser.js';

require('calendar_display.inc');
require_once('calendar_js.inc');
require("$obminclude/of/of_right.inc");
require_once("$obminclude/of/of_category.inc");
require('calendar_mailer.php');
require('event_observer.php');
get_calendar_action();
$perm->check_permissions($module, $action);

page_close();

// If no user or resource selected, we select the connected user
$users = $current_view->get_users();
$resources = $current_view->get_resources();
if (empty($users) && empty($resources)) {
  $current_view->add_entity($GLOBALS['token']['entity'],$GLOBALS['token']['entityId']);
}

$params['category_filter'] = $c_all;

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// External calls (main menu not displayed)                                  //
///////////////////////////////////////////////////////////////////////////////
if ($action == 'ics_export') {
  global $ics_sharing_max_months;

  // OBMFULL-2980
  // With this added parameter, we export only event newer than a given date
  $afterDate = new Of_Date();
  $afterDate->subMonth($ics_sharing_max_months);
  $params['event_after_date'] = $afterDate;
  $params['export'] = "postdate";
  
  dis_calendar_export_handle($params, $GLOBALS['token']['entity'], $GLOBALS['token']['entityId'], $GLOBALS['token']['type'] == 'private');
  # Remove the session to force auth next time (Mantis #3007)
  $sess->delete();
  exit();  
} 

$GLOBALS['js']['vars']['conf']['displayRange'] = 'true';

if ($action == 'search') {
///////////////////////////////////////////////////////////////////////////////
  include_once('obminclude/lib/Solr/Service.php');
  $display['detail'] .= dis_calendar_search_result($params, $current_view);

} elseif ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] .= dis_calendar_calendar_view($params, $current_view);

} elseif ($action == 'waiting_events') {
///////////////////////////////////////////////////////////////////////////////
  $obm_wait = run_query_calendar_waiting_events();
  $display['msg'] .= display_info_msg($l_waiting_events.' : '.$obm_wait->nf());
  if ($obm_wait->nf() != 0) {
    $display['detail'] = html_calendar_waiting_events($obm_wait);
    if ($params['show_calendar']) {
      $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
    }
  } else {
    $display['detail'] .= dis_calendar_calendar_view($params, $current_view);
  }

} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  if (check_calendar_access_by_token($params['calendar_id'], 'read')) {
    $display['detail'] = dis_calendar_event_consult($params['calendar_id']);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
  }
  
} elseif ($action == 'download_document') {
///////////////////////////////////////////////////////////////////////////////
  require '../document/document_query.inc';
  require '../document/document_display.inc';
  
  if (!check_document_access_by_token($params['document_id'], $params['event_id'])) {
    $display['msg'] .= display_err_msg("$l_err_file_access_forbidden");
  } else {
    $doc_q = run_query_document_detail($params['document_id']);
    if ($doc_q->num_rows() == 1) {
      dis_document_file($doc_q);
    } else {
      $display['msg'] .= display_err_msg("$l_no_document !");
    }
  }

} elseif ($action == 'pdf_export_form') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = dis_calendar_pdf_options($current_view);

} elseif ($action == 'pdf_export') {
///////////////////////////////////////////////////////////////////////////////
  require_once("$obminclude/lib/Zend/Pdf.php");
  dis_calendar_pdf_view($params, $current_view);
  exit();
} elseif ($action == 'draw') {
///////////////////////////////////////////////////////////////////////////////
  echo dis_calendar_draw($current_view, $params['ndays']);
  // Store current_view in session
  $_SESSION['cal_current_view'] = serialize($current_view);
  exit();  
}
// displayed after, because $params['date'] can be updated by actions
$display['search'] = dis_calendar_view_bar($current_view,  $params);

$_SESSION['cal_current_view'] = serialize($current_view);

if (!$params['ajax']) {
  beautify_title($module, $action);
  $display['head'] = display_head($l_calendar);
  $display['end'] = display_end();
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
  $params['date'] = of_isodate_convert($params['date']);
  $params['date'] = new Of_Date($params['date']);
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
  // Index
  $actions['calendar']['index'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  $actions['calendar']['search'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );

  // Detail Consult
  $actions['calendar']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/calendar/calendar_render.php?action=detailconsult&amp;calendar_id=$id&amp;date=".$date->getURL(),
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
  );
  
  $actions['calendar']['download_document'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=download_document",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );

  // Export_ics
  $actions['calendar']['ics_export'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=ics_export&amp;popup=1",
    'Right'    => $cright_read,
    'Condition'=> array ('none') 
  );
  
  // PDF export options form
  $actions['calendar']['pdf_export_form'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=pdf_export_form&amp;date=$date&output_target=print",
    'Right'    => $cright_read,
    'Popup'    => 1,
    'Condition'=> array("None")
  );

  // PDF export
  $actions['calendar']['pdf_export'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=pdf_export",
    'Right'    => $cright_read,
    'Condition'=> array ('None')
  );
  
 // Refresh 
  $actions['calendar']['draw'] = array (
    'Url'      => "$path/calendar/calendar_render.php?action=draw",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
  );
}

?>
