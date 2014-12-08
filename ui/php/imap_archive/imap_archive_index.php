<?php
/******************************************************************************
Copyright (C) 2014 Linagora

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
// OBM - File : imap_archive_index.php                                       //
//     - Desc : User Backup Index File                                       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


$debug = 1;
$path = '..';
$module = 'imap_archive';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';

include_once("$obminclude/global.inc");

$params = get_imap_archive_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include_once("$obminclude/global_pref.inc");
require_once('imap_archive_display.inc');
require_once('imap_archive_query.inc');
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_error.php");
require_once("$obminclude/of/of_right.inc");
$extra_js_include[] = 'imap_archive.js';

if ($action == 'index') $action = 'detailconsult';

get_imap_archive_action();
$perm->check_permissions($module, $action);

update_last_visit('imap_archive', $params['user_id'], $action);

page_close();

$params['user_id'] = $obm['uid'];

try {

  connect_to_imap_archive_service();

  if ($action == 'update') {
    display_update($params);
  }
  elseif ($action == 'next_treatment_date') {
    display_next_treatment_date($params);
  }
  elseif ($action == 'manual_launch') {
    display_manual_launch($params);
  }
  elseif ($action == 'archiving_logs') {
    display_archiving_logs($params);
  }
  elseif ($action == 'detailupdate') {
    display_detailupdate($params);
  }
  elseif ($action == 'log_page') {
    display_log_page($params);
  }
  else { //if ($action == 'detailconsult' or no action or unknown action) {
    display_detailconsult($params);
  }
}
catch(Exception $e) {
  display_exception($e);
}

function display_update($params) {
  global $display, $err;
  global $l_invalid_data, $l_update_ok;

  if (!check_configuration($params)) {
    $display['msg'] .= display_err_msg($l_invalid_data . ' : ' . $err['msg']);
    display_detailupdate($params);
  } else {
    try {
      run_query_imap_archive_update((object) $params);
      $display['msg'] .= display_ok_msg($l_update_ok);
    }
    catch(Exception $e) {
      display_exception($e);
    }
    display_detailconsult($params);
  }
}

function display_next_treatment_date($params) {
  global $display;

  $configuration = (Object) $params['configuration'];
  try {
    $treatment_date_string = calculate_next_treatment_date_from_imap_archive_service($configuration);
    $next_treatment_date = new DateTime($treatment_date_string);
    $next_treatment_date->setTimezone(new DateTimeZone('UTC'));
    $display['json'] = json_encode($next_treatment_date->format('Y-m-d H:i'));
    echo '('.$display['json'].')';
  }
  catch (Exception $e) {
    http_response_code(500);
    OBM_Error::getInstance()->addError('internal', get_error_message($e));
    echo OBM_Error::getInstance()->toJson();
    exit();
  }
  exit();
}

function display_manual_launch($params) {
  global $display;

  try {
    $archive_treatment_kind = $params['archive_treatment_kind'];
    $launch_data = manual_launch($archive_treatment_kind);
    $display['msg'] .= display_ok_msg($l_archiving_launched);
    $run_id = $launch_data->runId;

    $redirect_url = $_SERVER['SCRIPT_NAME']."?action=log_page&run_id=$run_id";
    redirect_to($redirect_url);
    exit();
  }
  catch (Exception $e) {
    display_exception($e);
    display_detailconsult($params);
  }
}

function display_archiving_logs($params) {
  echo "<pre>";
  $run_id = $params['run_id'];
  try {
    get_logs($run_id);
  }
  catch(Exception $e) {
    display_exception($e);
  }
  echo "</pre>";
  exit();
}

function display_detailconsult($params) {
  global $display;

  $configuration = load_configuration_from_imap_archive_service();

  $next_treatment_date = get_result_or_empty(function() use($configuration) {
    return calculate_next_treatment_date_from_imap_archive_service($configuration);
  });
  $history = get_result_or_empty(function() {
    return treatments_history(false, 3, ImapArchiveOrdering::DESC);
  });
  $last_failure = get_result_or_empty(function() {
    return treatments_history(true, 1, ImapArchiveOrdering::DESC);
  });

  $display['detail'] = dis_imap_archive_consult($params, $configuration,
    $next_treatment_date, $history, $last_failure);
}

function display_detailupdate($params) {
  global $display;

  $configuration = load_configuration_from_imap_archive_service();

  $next_treatment_date = get_result_or_empty(function() use($configuration) {
    return calculate_next_treatment_date_from_imap_archive_service($configuration);
  });

  $display['detail'] = dis_imap_archive_form($params, $configuration, $next_treatment_date);
}

function get_result_or_empty($callback) {
  $result = null;
  try {
    $result = $callback();
  }
  catch (Exception $e) {
    display_exception($e);
    $result = '';
  }
  return $result;
}

function display_log_page($params) {
  global $display;

  $display['detail'] = dis_log_page($params);
}

function get_error_message($e) {
  global $l_email_address_not_found, $l_imap_archive_server_not_found, $l_domain_configuration_not_found;
  global $l_could_not_login_imap_archive_server, $l_imap_archive_server_unreachable, $l_unknown_error;

  $message = null;
  if ($e instanceof ImapArchiveNoEmailException) {
    $message = $l_email_address_not_found;
  }
  elseif ($e instanceof ImapArchiveServerNotFoundException) {
    $message = $l_imap_archive_server_not_found;
  }
  elseif ($e instanceof HttpClientHttpException) {
    error_log("Got HTTP error ".$e->code." with URL ".$e->url);
    if ($e->code == 404) {
      $message = $l_domain_configuration_not_found;
    }
    else {
      $message = $l_could_not_login_imap_archive_server;
    }
  }
  elseif ($e instanceof HttpClientOtherException) {
    error_log("Curl error ".$e->curl_errno." (".$e->message.") with URL ".$e->url);
    $CURLE_COULDNT_CONNECT = 7;
    if ($e->curl_errno == $CURLE_COULDNT_CONNECT) {
      $message = $l_imap_archive_server_unreachable;
    }
    else {
      $message = $l_could_not_login_imap_archive_server;
    }
  }
  else {
    $message = $l_unknown_error;
  }
  return $message;
}

function display_exception($e) {
  global $display;

  $display['msg'] .= display_err_msg(get_error_message($e));
}
///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head('imap_archive');
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}
$display['end'] = display_end();

display_page($display);

///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_imap_archive_params() {
  
    // Get global params
    $params = get_global_params();

    get_global_params_document($params);
  
    return $params;
  }


///////////////////////////////////////////////////////////////////////////////
// IMAP Archive Action 
///////////////////////////////////////////////////////////////////////////////
function get_imap_archive_action() {
  global $actions, $cright_read_admin, $cright_write_admin;
  global $l_header_consult, $l_header_update;
  
// Detail Consult
  $actions['imap_archive']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=detailconsult",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
  );

// Detail Update
  $actions['imap_archive']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=detailupdate",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all')
  );

// Update
  $actions['imap_archive']['update'] = array (
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Next Treatment Date
  $actions['imap_archive']['next_treatment_date'] = array (
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=next_treatment_date",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Manual Launch
  $actions['imap_archive']['manual_launch'] = array (
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=manual_launch",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Log page
  $actions['imap_archive']['log_page'] = array (
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=log_page",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );

// Archiving logs
  $actions['imap_archive']['archiving_logs'] = array (
    'Url'      => "$path/imap_archive/imap_archive_index.php?action=archiving_logs",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None')
  );
}


?>
