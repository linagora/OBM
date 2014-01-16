<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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
// OBM - File : time_index.php                                               //
//     - Desc : Time management Index File                                   //
// 2002-04-01 Pierre Carlier                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) -- search fields  -- dislay task list and new task form.
// - viewmonth       --                -- display month
// - globalview      --                -- display validation pannel
// - insert          -- form fields    -- insert the task
// - detailupdate    -- $task_id       -- show the update task form in a popup
// - update          -- form fields    -- update the task
// - delete          -- $params        -- delete the tasks
// - validate        --                -- validate a month for a user
// - unvalidate      --                -- cancel admin validation
// - stats           --                -- show stats screen
// - display         --                -- display and set display parameters
// - dispref_display --                -- update one field display value
// - dispref_level   --                -- update one field display position 
///////////////////////////////////////////////////////////////////////////////

// Todo
// - point sur timetask_status ! dans globalview : incoherent
//   => d'ou sur validate, unvalidate et valid auto

$path = '..';
$module = 'time';

$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
require("$obminclude/global.inc");
$params = get_time_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
update_time_session_params();
include("$obminclude/global_pref.inc");
require('time_display.inc');
require('time_query.inc');
require('time_js.inc');
require('time_pdf.inc');


get_time_actions();
$perm->check_permissions($module, $action);

page_close();


///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

if ($action == 'index') {
///////////////////////////////////////////////////////////////////////////////
  dis_time_index($params);

} elseif ($action == 'viewmonth') {
///////////////////////////////////////////////////////////////////////////////
  $params['interval'] = 'month';
  $display['detail'] = dis_time_nav_date($params);
  $display['detail'] .= dis_time_planning($params);
  if ($perm->check_right('time', $cright_read_admin)) {
    $display['features'] .= dis_time_group_select($params, get_private_groups($obm['uid']));
    $display['features'] .= dis_user_select($params, run_query_userobm_active(), 1);
  }

} elseif ($action == 'insert') {
//////////////////////////////////////////////////////////////////////////////;
  $params['interval'] = 'week';
  run_query_time_insert($params);
  run_query_time_validate($params['user_id']);
  dis_time_index($params);

} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $d_start_week = of_date_get_first_day_week($params['date']);
  $val_days = run_query_time_valid_search($params);
  $display['result'] .= dis_time_form_addtask($params, $val_days);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  run_query_time_update($params);
  run_query_time_validate($params['user_id']);
  // Javascript here because muist be run after the form submit
  $display['result'] .= "
    <script language=\"javascript\">
     window.opener.location.href='$path/time/time_index.php?action=index&user_id=".$params['user_id']."&date=".$params['date']."';
     window.close();
    </script>
";

} elseif ($action == 'delete') {
//////////////////////////////////////////////////////////////////////////////
  // interval is week -- see if we may need to use others intervals
  $params['interval'] = 'week';
  run_query_time_delete($_REQUEST);
  run_query_time_validate($params['user_id']);
  dis_time_index($params);
  
} if ($action == 'globalview') {
//////////////////////////////////////////////////////////////////////////////
  $params['interval'] = 'month';
  $display['detail'] = dis_time_nav_date($params);
  $display['detail'] .= dis_time_month_users_total($params);

} elseif ($action == 'validate') {
//////////////////////////////////////////////////////////////////////////////
  $params['interval'] = 'month';
  run_query_time_adminvalidate($params);
  $display['detail'] = dis_time_nav_date($params);
  $display['detail'] .= dis_time_month_users_total($params);

} elseif ($action == 'unvalidate') {
//////////////////////////////////////////////////////////////////////////////
  $params['interval'] = 'month';
  run_query_time_adminunvalidate($params);
  $display['detail'] = dis_time_nav_date($params);
  $display['detail'] .= dis_time_month_users_total($params);

} elseif ($action == 'stats') {
//////////////////////////////////////////////////////////////////////////////
  if (check_time_date_form($params)) {
    get_time_params_date_range($params);
    $statproj = get_time_stat_project($params);
    $stattt = get_time_stat_tasktype($params);
    $display['detail'] = dis_time_date_menu($params);
    if ($perm->check_right('time', $cright_read_admin)) {
      $display['features'] .= dis_time_group_select($params, get_private_groups($obm['uid']));
      $display['features'] .= dis_user_select($params, get_time_active_user($params), 1);
    }
    $display['detail'] .= dis_time_stats_project($statproj, $params);
    $display['detail'] .= dis_time_stats_tasktype($stattt, $params);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'] = dis_time_date_menu($params);
  }

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'time', 1);
  $display['detail'] = dis_time_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'time', 1);
  $display['detail'] = dis_time_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($params);
  $prefs = get_display_pref($obm['uid'], 'time', 1);
  $display['detail'] = dis_time_display_pref($prefs);

} else if ($action == 'activity_report') {
///////////////////////////////////////////////////////////////////////////////
  global $l_pdf_no_project_selected;
  global $l_pdf_bad_date;
  if (isset($params['pdf_form'])) {
    if (empty($params['projects']))
      $display['msg'] = display_err_msg($l_pdf_no_project_selected);
    elseif ($params['interval'] === 'date') {
      $elms = array('d', 'm', 'Y');
      $repl = array('(?P<day>\d{2})', '(?P<month>\d{2})', '(?P<year>\d{4})');
      $patt = '#'.str_replace($elms, $repl, $_SESSION['set_date_upd']).'#';
      preg_match($patt, $params['int_date'], $arr);
      $date = $arr['year'].'-'.$arr['month'].'-'.$arr['day'];

      if (!strtotime($date))
	$display['msg'] = display_err_msg($l_pdf_bad_date);
      else
        $params['int_date'] = date("Ymd", strtotime($date));
    }
  }
  $display['detail']  = dis_time_form_ra($params);
}


///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_time);
$display['end'] = display_end();
if (! $popup) {
  $display['header'] = display_menu($module);
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores time parameters transmited in $task hash
// returns : $task hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_time_params() {

  // Get global params
  $params = get_global_params();

  // Get time specific params
  if ($params['date'] == '') { $params['date'] = date('Y-m-d'); }
  
  // We retrieve the selected users if any, else we get them from sessiom
  if (isset ($params['user_id'])) {
    if (is_array($params['user_id'])) {
      $params['user_ids'] = $params['user_id'];
    } else {
      $params['user_ids'] = array($params['user_id']);
    }
  }

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Update session and parameters
///////////////////////////////////////////////////////////////////////////////
function update_time_session_params() {
  global $params, $obm, $c_none;
  
  // If group selected is None, reset group
  if ($params['group_id'] == $c_none) {
    $_SESSION['group_id'] = $c_none;
  } else if ($params['group_id'] > 0) {
    // If group selected is not None, get group users
    if ($params['group_id'] != $c_none) {
      $params['user_ids'] = of_usergroup_get_group_users($params['group_id']);
      $_SESSION['group_id'] = $params['group_id'];
    }
  } else {
    $params['group_id'] = $_SESSION['group_id'];
  }

  // We retrieve the selected users if any, else we get them from sessiom
  // or from selected user (alone) or we set it to uid
  if ((! isset($params['user_ids'])) ||
      ((is_array($params['user_ids'])) && (count($params['user_ids'])==0))) {
    if (isset($_SESSION['sess_users'])) {
      if (is_array($_SESSION['sess_users'])) {
	$params['user_ids'] = $_SESSION['sess_users'];
      } else {
	$params['user_ids'] = array($_SESSION['sess_users']);
      }
    } else if (isset($params['user_id'])) {
      $params['user_ids'] = array($params['user_id']);
    } else {      
      $params['user_ids'] = array($obm['uid']);
    }
  }
  $_SESSION['sess_users'] = $params['user_ids'];


  // We retrieve the selected user if set, else we set it them from multi select
  // or from uid
  if (! isset($params['user_id'])) {
    if (is_array($params['user_ids']) && (count($params['user_ids']) == 1) ) {
      $params['user_id'] = $params['user_ids'][0];
    } else {
      $params['user_id'] = $obm['uid'];
    }
  }

}


//////////////////////////////////////////////////////////////////////////////
// Time actions
//////////////////////////////////////////////////////////////////////////////
function get_time_actions() {
  global $params, $path, $actions;
  global $l_header_weeklyview, $l_header_monthlyview, $l_header_globalview;
  global $l_header_stats, $l_header_display, $l_header_activityreport;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['time']['index'] = array (
    'Name'     => "$l_header_weeklyview",
    'Url'      => "$path/time/time_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array('all') 
                                    );

// User Monthly View
  $actions['time']['viewmonth'] = array (
    'Name'     => "$l_header_monthlyview",
    'Url'      => "$path/time/time_index.php?action=viewmonth".
                  "&amp;date=" . $params['date'],
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                    );

// General Monthly View
  $actions['time']['globalview'] = array (
    'Name'     => "$l_header_globalview",
    'Url'      => "$path/time/time_index.php?action=globalview".
                  "&amp;date=" . $params['date'],
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
                                    );

// Detail Update
  $actions['time']['detailupdate'] = array (
    'Url'      => "$path/time/time_index.php?action=detailupdate",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Insert 
  $actions['time']['insert'] = array (
    'Url'      => "$path/time/time_index.php?action=insert",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                    );

// Update
  $actions['time']['update'] = array (
    'Url'      => "$path/time/time_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Delete 
  $actions['time']['delete'] = array (
    'Url'      => "$path/time/time_index.php?action=delete",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                    );

// Validate
  $actions['time']['validate'] = array (
    'Url'      => "$path/time/time_index.php?action=validate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Cancel Validation
  $actions['time']['unvalidate'] = array (
    'Url'      => "$path/time/time_index.php?action=unvalidate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
                                    );

// Stats by Users
  $actions['time']['stats'] = array (
    'Name'     => "$l_header_stats",
    'Url'      => "$path/time/time_index.php?action=stats",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                     );

// Display
   $actions['time']['display'] = array (
     'Name'     => $l_header_display,
     'Url'      => "$path/time/time_index.php?action=display",
     'Right'    => $cright_read,
     'Condition'=> array ('all') 
                                       	 );

// Display Preferences
  $actions['time']['dispref_display'] = array (
    'Url'      => "$path/time/time_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

// Display Level
  $actions['time']['dispref_level']  = array (
    'Url'      => "$path/time/time_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                     		 );

///////////////////////////////////////////////////////////////////////////////
// PDF generation for activity reports
///////////////////////////////////////////////////////////////////////////////
// Activity Report View
  $actions['time']['activity_report'] = array (
    'Name'     => $l_header_activityreport,
    'Url'      => "$path/time/time_index.php?action=activity_report",
    'Right'    => $cright_read,
    'Condition'=> array ('all')
                                              );

}

?>
