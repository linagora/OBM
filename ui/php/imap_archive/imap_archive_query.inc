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
require_once('imap_archive_urls.php');

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
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $loginPath = ImapArchiveUrls::status();

  try {
    $curl = curlGet($loginPath);
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $configurationPath = ImapArchiveUrls::configuration($obm["domain_uuid"]);

  try {
    $curl = curlGet($configurationPath);
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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

  $imapArchiveServer = of_domain_get_domain_imaparchiveserver($obm['domain_id']);
  if(!count($imapArchiveServer)){
    throw new ImapArchiveServerNotFoundException();
  }
  $iterator = new ArrayIterator($imapArchiveServer);
  $imapArchiveServer = $iterator->current();
  return "http://".$imapArchiveServer[0]["ip"].":8085";
}

function curlGet($url, $extraParams = array()) {
  global $obm;

  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$url."?".http_build_query($extraParams);
  
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
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $configurationPath = ImapArchiveUrls::nextTreatment($obm["domain_uuid"]);

  try {
    $curl = curlPost($configurationPath, array(), toJson($configuration));
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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

function toJson($configuration) {
  $repeatKind = $configuration->repeatKind;
  $dayOfWeek = $configuration->dayOfWeek;
  $dayOfMonth = $configuration->dayOfMonth;
  $dayOfYear = $configuration->dayOfYear;
  $hour = $configuration->hour;
  $minute = $configuration->minute;
  $excludedFolder = $configuration->excludedFolder;
  $excludedUserIdToLoginMapFromConfig = $configuration->excludedUserIdToLoginMap;
  $excludedUserIdToLoginMap = $excludedUserIdToLoginMapFromConfig != null ? $excludedUserIdToLoginMapFromConfig : new stdClass();
  $mailingEmailsFromConfig = $configuration->mailingEmails;
  $mailingEmails = $mailingEmailsFromConfig != null ? $mailingEmailsFromConfig : array();
	
  return json_encode(array("domainId" => $obm['domain_uuid'],
    "enabled" => ($configuration->enabled) ? "true" : "false",
    "repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
    "dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
    "dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
    "dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
    "hour" => ($hour) ? $hour : 0,
    "minute" => ($minute) ? $minute : 0,
    "excludedFolder" => $excludedFolder,
    "excludedUserIdToLoginMap" => $excludedUserIdToLoginMap,
    "mailingEmails" => $mailingEmails
  ));	
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
function run_query_imap_archive_update($paramsAsConfiguration) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $configurationPath = ImapArchiveUrls::configuration($obm["domain_uuid"]);

  try {
    $curl = curlPut($configurationPath, paramToJson($paramsAsConfiguration));
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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

function paramToJson($paramsAsConfiguration) {
	$repeatKind = $paramsAsConfiguration->repeat_kind;
	$dayOfWeek = $paramsAsConfiguration->day_of_week;
	$dayOfMonth = $paramsAsConfiguration->day_of_month;
	$dayOfYear = $paramsAsConfiguration->day_of_year;
	$hour = $paramsAsConfiguration->hour;
	$minute = $paramsAsConfiguration->minute;
	$excludedFolder = $paramsAsConfiguration->excluded_folder;
	$excludedUserIdToLoginMap = excludedUserIdsToMap($paramsAsConfiguration->excluded_user_id);
	$mailingEmailsFromConfig = $paramsAsConfiguration->mailing_emails;
	$mailingEmails = $mailingEmailsFromConfig != null ? $mailingEmailsFromConfig : array();

	return json_encode(array("domainId" => $obm['domain_uuid'],
			"enabled" => ($paramsAsConfiguration->enabled) ? "true" : "false",
			"repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
			"dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
			"dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
			"dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
			"hour" => ($hour) ? $hour : 0,
			"minute" => ($minute) ? $minute : 0,
			"excludedFolder" => $excludedFolder,
			"excludedUserIdToLoginMap" => $excludedUserIdToLoginMap,
			"mailingEmails" => $mailingEmails
	));
}

function excludedUserIdsToMap($excludedUserIds) {
  $map = new stdClass();
  if ($excludedUserIds == null) {
    return $map;
  }
  $users = get_userobm_from_ext_ids($excludedUserIds, false);
  foreach ($users['entity'] as $u_ext_id => $u_ent) {
    $map->$u_ext_id = $u_ent['login'];
  }
  return $map;
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
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_domain_configuration_not_found, $l_domain_configuration_disable, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $manualLaunchPath = ImapArchiveUrls::treatments($obm["domain_uuid"]);

  try {
    $curl = curlPostWithHeader($manualLaunchPath, array("archive_treatment_kind" => $archiveTreatmentKind));
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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
  if ($code == 303) {
    return followRedirect($curl["redirectUrl"]);
  }
}

function curlPostWithHeader($url, $extraParams = array(), $json = "") {
  global $obm;

  $imapArchiveRootPath = imapArchiveRootPath();
  $curlUrl = $imapArchiveRootPath.$url."?".http_build_query($extraParams);

  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_POST, 1);
  curl_setopt($curl, CURLOPT_HTTPHEADER, array('Accept: application/json', 'Content-type: application/json'));
  curl_setopt($curl, CURLOPT_HEADER, true);
  curl_setopt($curl, CURLOPT_POSTFIELDS, $json);

  return curlExecWithHeader($curl);
}

function curlExecWithHeader($curl) {

  $success = curl_exec($curl);
  $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
  $errno = curl_errno($curl);
	
  $redirectUrl = preg_match_all('/^Location:(.*)$/mi', $success, $matches) ? trim($matches[1][0]) : '';
  
  curl_close($curl);

  return array("success" => $success, "code" => $code, "errno" => $errno, "redirectUrl" => $redirectUrl);
}

function followRedirect($redirectUrl) {
  global $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;

  $curl = basicAuthCurlInit($redirectUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
    
  $result = curlExec($curl);
    
  $successGet = $result["success"];
  $codeGet = $result["code"];
  $errnoGet = $result["errno"];
  
  if($errnoGet){
    error_log("Couldn't login to IMAP archive server ($errnoGet)");
    return array(0, $l_could_not_login_imap_archive_server);
  }
  if ($codeGet == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($codeGet == 200) {
    return array(1, json_decode($successGet));
  }
}

///////////////////////////////////////////////////////////////////////////////
// Get log on IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function get_logs($runId) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $logPath = ImapArchiveUrls::logs($obm["domain_uuid"], $runId);
  $imapArchiveRootPath = null;
  try {
    $imapArchiveRootPath = imapArchiveRootPath();
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
  $curlUrl = $imapArchiveRootPath.$logPath;
  
  $curl = basicAuthCurlInit($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  curl_setopt($curl, CURLOPT_WRITEFUNCTION, 'curl_write_flush');
  
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

///////////////////////////////////////////////////////////////////////////////
// Treatments history from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function treatments_history($onlyFailure, $limit, $sort) {
  global $obm;
  global $l_email_address_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server, $l_imap_archive_server_not_found;
  $userInfo = get_user_info($obm['uid']);

  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }

  $historyPath = ImapArchiveUrls::treatments($obm["domain_uuid"]);

  $parameters = array("domainId" => $obm['domain_uuid'],
      "filter_terminated" => 'true',
      "filter_failure" => ($onlyFailure) ? 'true' : 'false',
      "limit" => $limit,
      "sort_by_date" => $sort
  );

  try {
    $curl = curlGet($historyPath, $parameters);
  }
  catch(ImapArchiveServerNotFoundException $ex) {
      return array(0, $l_imap_archive_server_not_found);
  }
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

class ImapArchiveServerNotFoundException extends Exception {
}

?>
