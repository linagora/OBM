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
// OBM - File : imap_archive_query.inc                                       //
//     - Desc : IMAP Archive service File                                    //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Check connection to IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function connect_to_imap_archive_service(){
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $loginPath = "/imap-archive/service/v1/status";
  
  $curl = curlGet($loginPath);
  $success = $curl["success"];
  $code = $curl["code"];
  $errno = $curl["errno"];
  
  if($errno){
    error_log("Couldn't login to IMAP archive server (".$errno.")");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200) {
    return array(1, null);
  }

}

///////////////////////////////////////////////////////////////////////////////
// Load configuration from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function load_configuration_from_imap_archive_service(){
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $configurationPath = "/imap-archive/service/v1/domains/".$obm["domain_uuid"]."/configuration";
  
  $curl = curlGet($configurationPath);
  $success = $curl["success"];
  $code = $curl["code"];
  $errno = $curl["errno"];
  
  if($errno){
    error_log("Couldn't login to IMAP archive server ($errno)");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200) {
    return array(1, json_decode($success));
  }
}

function imapArchiveRootPath() {
  global $obm;
  global $l_imap_archive_server_not_found;

  $imapArchiveServer = of_domain_get_domain_imaparchiveserver($obm['domain_id']);
  if(!count($imapArchiveServer)){
    return array(0, $l_imap_archive_server_not_found);
  }
  $iterator = new ArrayIterator($imapArchiveServer);
  $imapArchiveServer = $iterator->current();
  return "http://".$imapArchiveServer[0]["ip"].":8085";
}

function curlGet($url) {
  global $obm;

  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$url;
  
  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    
  return curlExec($curl);
}

///////////////////////////////////////////////////////////////////////////////
// Calculate next treatment date from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function calculate_next_treatment_date_from_imap_archive_service($configuration) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $configurationPath = "/imap-archive/service/v1/domains/".$obm["domain_uuid"]."/treatments/next";

  $repeatKind = $configuration->repeatKind;
  $dayOfWeek = $configuration->dayOfWeek;
  $dayOfMonth = $configuration->dayOfMonth;
  $dayOfYear = $configuration->dayOfYear;
  $enabled = $configuration->enabled;
  $hour = $configuration->hour;
  $minute = $configuration->minute;
    
  $json = json_encode(array("domainId" => $obm['domain_uuid'],
      "enabled" => ($enabled) ? true : false,
      "repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
      "dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
      "dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
      "dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
      "hour" => ($hour) ? $hour : 0,
      "minute" => ($minute) ? $minute : 0
  ));
  
  $curl = curlPost($configurationPath, $extraParams, $json);
  $success = $curl["success"];
  $code = $curl["code"];
  $errno = $curl["errno"];

  if($errno){
    error_log("Couldn't login to IMAP archive server (".$errno.")");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200) {
    return array(1, json_decode($success));
  }
  if ($code == 204) {
    return array(1, null);
  }
}

function curlPost($url, $extraParams = array(), $json = "") {
  global $obm;
  
  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$url."?".http_build_query($extraParams);
  
  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_POST, 1);
  curl_setopt($curl, CURLOPT_HTTPHEADER, array('Accept: application/json', 'Content-type: application/json'));
  curl_setopt($curl, CURLOPT_POSTFIELDS, $json);
  
  return curlExec($curl);
}

///////////////////////////////////////////////////////////////////////////////
// Check configuration 
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function check_configuration($params) {
  global $err;
  global $php_regexp_imap_sub_folder, $l_excluded_folder_error;
  
  $excludedFolder = $params['excluded_folder'];
  
  if (preg_match($php_regexp_imap_sub_folder, $excludedFolder)) {
    $err['msg'] .= "$excludedFolder : $l_excluded_folder_error";
    return false;
  }
  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Update configuration to IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function run_query_imap_archive_update($params) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $configurationPath = "/imap-archive/service/v1/domains/".$obm["domain_uuid"]."/configuration";

  $repeatKind = $params['repeat_kind'];
  $dayOfWeek = $params['day_of_week'];
  $dayOfMonth = $params['day_of_month'];
  $dayOfYear = $params['day_of_year'];
  $hour = $params['hour'];
  $minute = $params['minute'];
  $excludedFolder = $params['excluded_folder'];
  
  $json = json_encode(array("domainId" => $obm['domain_uuid'],
      "enabled" => ($params['enabled']) ? "true" : "false",
      "repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
      "dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
      "dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
      "dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
      "hour" => ($hour) ? $hour : 0,
      "minute" => ($minute) ? $minute : 0,
      "excludedFolder" => $excludedFolder
  ));
  
  $curl = curlPut($configurationPath, $json);
  $success = $curl["success"];
  $code = $curl["code"];
  $errno = $curl["errno"];
  
  if($errno){
    error_log("Couldn't login to IMAP archive server ($errno)");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200 || $code == 201 || $code == 204) {
    return array(1, json_decode($success));
  }
}

function curlPut($url, $json) {
  global $obm;
  
  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$url;
  
  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_CUSTOMREQUEST, "PUT");
  curl_setopt($curl, CURLOPT_HTTPHEADER, array('Accept: application/json', 'Content-type: application/json'));
  curl_setopt($curl, CURLOPT_POSTFIELDS, $json);
  
  return curlExec($curl);
}

function curlExec($curl) {
  
  $success = curl_exec($curl);
  $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
  $errno = curl_errno($curl);
  curl_close($curl);
  
  return array("success" => $success, "code" => $code, "errno" => $errno);
}

function basicAuthCurlInit($curlUrl) {
  global $obm;

  $userInfo = get_user_info($obm['uid']);  
  $username = $userInfo["login"];
  $password = get_trust_token($userInfo);
  $domainName = $obm['domain_name'];

  $curl = curl_init($curlUrl);
  curl_setopt($curl, CURLOPT_HTTPAUTH, CURLAUTH_BASIC);
  curl_setopt($curl, CURLOPT_USERPWD, "$username@$domainName:$password");
  return $curl;
}

///////////////////////////////////////////////////////////////////////////////
// Launch manually an archive on IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function manual_launch($archiveTreatmentKind) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_domain_configuration_not_found, $l_domain_configuration_disable;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $manualLaunchPath = "/imap-archive/service/v1/domains/".$obm["domain_uuid"]."/treatments";

  $curl = curlPost($manualLaunchPath, array("archive_treatment_kind" => $archiveTreatmentKind));
  $success = $curl["success"];
  $code = $curl["code"];
  $errno = $curl["errno"];
  
  if($errno){
    error_log("Couldn't login to IMAP archive server ($errno)");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 404) {
    return array(0, $l_domain_configuration_not_found);
  }
  if ($code == 409) {
    return array(0, $l_domain_configuration_disable);
  }
  if ($code == 200) {
    return array(1, json_decode($success));
  }
}

///////////////////////////////////////////////////////////////////////////////
// Get log on IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function get_logs($runId) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $logPath = "/imap-archive/service/v1/domains/".$obm["domain_uuid"]."/treatments/logs/";
  
  $queryParams = array("run_id" => $runId);
      
  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$logPath."?".http_build_query($queryParams);
  
  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_WRITEFUNCTION, 'curl_write_flush');
  curl_setopt($ch, CURLOPT_TIMEOUT, 60);
  
  $success = curl_exec($curl);
  $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
  $errno = curl_errno($curl);
  
  curl_close($curl);
  
  if($errno){
    error_log("Couldn't login to IMAP archive server ($errno)");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200 || $code == 204 || $code == 206) {
    return array(1, null);
  }
}

function curl_write_flush($curl_handle, $chunk)
{
  echo "$chunk";
  
  ob_flush();
  flush();
  
  return strlen($chunk);
}
?>
