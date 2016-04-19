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
$obminclude = getenv('OBM_INCLUDE_VAR');
require_once("$obminclude/of/of_http_client.inc");

// errno -> $l_could_not_login_imap_archive_server
// code = 0 -> l_imap_archive_server_unreachable
// ImapArchiveServerNotFoundException in imapArchiveRootPath -> l_imap_archive_server_not_found
// 404 -> l_domain_configuration_not_found
// ImapArchiveNoEmailException -> l_email_address_not_found

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
  $loginPath = ImapArchiveUrls::status();

  return http_get($loginPath)->as_text();
}

///////////////////////////////////////////////////////////////////////////////
// Load configuration from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function load_configuration_from_imap_archive_service(){
  global $obm;

  $configurationPath = ImapArchiveUrls::configuration($obm["domain_uuid"]);

  return http_get($configurationPath)->as_json();
}

function imapArchiveRootPath() {
  global $obm;

  $imapArchiveServers = of_domain_get_domain_imaparchiveserver($obm['domain_id']);
  if(!$imapArchiveServers) {
    throw new ImapArchiveServerNotFoundException();
  }
  $serverData = null;
  foreach ($imapArchiveServers as $id => $imapArchiveServerData) {
      $serverData = $imapArchiveServerData[0];
      break;
  }
  return "http://".$serverData["ip"].":8085";
}

function http_get($url, $extraParams = array()) {
  $config = build_http_config_with_auth();
  $httpClient = new HttpClient($config);
  return $httpClient->get(new Url(imapArchiveRootPath().$url, $extraParams));
}

///////////////////////////////////////////////////////////////////////////////
// Calculate next treatment date from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function calculate_next_treatment_date_from_imap_archive_service($configuration) {
  global $obm;

  $configurationPath = ImapArchiveUrls::nextTreatment($obm["domain_uuid"]);

  $result = http_post($configurationPath, array(), toJson($configuration))->as_json();
  return $result->nextTreatmentDate;
}

function toJson($configuration) {
  global $obm;

  $repeatKind = $configuration->repeatKind;
  $dayOfWeek = $configuration->dayOfWeek;
  $dayOfMonth = $configuration->dayOfMonth;
  $dayOfYear = $configuration->dayOfYear;
  $hour = $configuration->hour;
  $minute = $configuration->minute;
  $archiveMainFolder = $configuration->archiveMainFolder;
  $excludedFolder = $configuration->excludedFolder;
  $scopeUserIdToLoginMapFromConfig = $configuration->scopeUserIdToLoginMap;
  $scopeUserIdToLoginMap = $scopeUserIdToLoginMapFromConfig != null ? $scopeUserIdToLoginMapFromConfig : new stdClass();
  $scopeUsersIncludes = $configuration->scopeUsersIncludes;
  $scopeSharedMailboxIdToNameMapFromConfig = $configuration->scopeSharedMailboxIdToNameMap;
  $scopeSharedMailboxIdToNameMap = $scopeSharedMailboxIdToNameMapFromConfig != null ? $scopeSharedMailboxIdToNameMapFromConfig : new stdClass();
  $scopeSharedMailboxesIncludes = $configuration->scopeSharedMailboxesIncludes;
  $mailingEmailsFromConfig = $configuration->mailingEmails;
  $mailingEmails = $mailingEmailsFromConfig != null ? $mailingEmailsFromConfig : array();

  return json_encode(array("domainId" => $obm['domain_uuid'],
    "enabled" => ($configuration->enabled) ? "true" : "false",
    "moveEnabled" => ($configuration->moveEnabled) ? "true" : "false",
    "repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
    "dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
    "dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
    "dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
    "hour" => ($hour) ? $hour : 0,
    "minute" => ($minute) ? $minute : 0,
    "archiveMainFolder" => $archiveMainFolder,
    "excludedFolder" => $excludedFolder,
    "scopeUserIdToLoginMap" => $scopeUserIdToLoginMap,
    "scopeUsersIncludes" => $scopeUsersIncludes,
    "scopeSharedMailboxIdToNameMap" => $scopeSharedMailboxIdToNameMap,
    "scopeSharedMailboxesIncludes" => $scopeSharedMailboxesIncludes,
    "mailingEmails" => $mailingEmails
  ));
}

function jsonHeaders() {
    return new HttpHeaders(array(
      'Accept'        => 'application/json',
      'Content-type'  => 'application/json'
    ));
}

function http_post($url, $extraParams = array(), $json = "", $max_redirects = 0) {
  $init_config = build_http_config_with_auth();
  $post_config = HttpConfig::from_config($init_config, array(jsonHeaders()),
    $max_redirects);
  $httpClient = new HttpClient($post_config);
  return $httpClient->post(
    new Url(imapArchiveRootPath().$url, $extraParams),
    $json);
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

  $configurationPath = ImapArchiveUrls::configuration($obm["domain_uuid"]);

  return http_put($configurationPath, paramToJson($paramsAsConfiguration));
}

function paramToJson($paramsAsConfiguration) {
  global $obm;

  $repeatKind = $paramsAsConfiguration->repeat_kind;
  $dayOfWeek = $paramsAsConfiguration->day_of_week;
  $dayOfMonth = $paramsAsConfiguration->day_of_month;
  $dayOfYear = $paramsAsConfiguration->day_of_year;
  $hour = $paramsAsConfiguration->hour;
  $minute = $paramsAsConfiguration->minute;
  $archiveMainFolder = $paramsAsConfiguration->archive_main_folder;
  $excludedFolder = $paramsAsConfiguration->excluded_folder;
  $scopeUserIdToLoginMap = scopeUserIdsToMap($paramsAsConfiguration->scope_user_id);
  $scopeUsersIncludes = $paramsAsConfiguration->scope_users_includes;
  $scopeSharedMailboxIdToNameMap = scopeSharedMailboxIdToNameMap($paramsAsConfiguration->scope_shared_mailbox_id);
  $scopeSharedMailboxesIncludes = $paramsAsConfiguration->scope_shared_mailboxes_includes;
  $mailingEmailsFromConfig = $paramsAsConfiguration->mailing_emails;
  $mailingEmails = $mailingEmailsFromConfig != null ? $mailingEmailsFromConfig : array();

  return json_encode(array("domainId" => $obm['domain_uuid'],
      "enabled" => ($paramsAsConfiguration->enabled) ? "true" : "false",
      "moveEnabled" => ($paramsAsConfiguration->move_enabled) ? "true" : "false",
      "repeatKind" => ($repeatKind) ? $repeatKind : "MONTHLY",
      "dayOfWeek" => ($dayOfWeek) ? $dayOfWeek : 1,
      "dayOfMonth" => ($dayOfMonth) ? $dayOfMonth : -1,
      "dayOfYear" => ($dayOfYear) ? $dayOfYear : 1,
      "hour" => ($hour) ? $hour : 0,
      "minute" => ($minute) ? $minute : 0,
      "archiveMainFolder" => $archiveMainFolder,
      "excludedFolder" => $excludedFolder,
      "scopeUserIdToLoginMap" => $scopeUserIdToLoginMap,
      "scopeUsersIncludes" => ($scopeUsersIncludes) ? "true" : "false",
      "scopeSharedMailboxIdToNameMap" => $scopeSharedMailboxIdToNameMap,
      "scopeSharedMailboxesIncludes" => ($scopeSharedMailboxesIncludes) ? "true" : "false",
      "mailingEmails" => $mailingEmails
  ));
}

function scopeUserIdsToMap($scopeUserIds) {
  $map = new stdClass();
  if ($scopeUserIds == null) {
    return $map;
  }
  $users = get_userobm_from_ext_ids($scopeUserIds, false);
  foreach ($users['entity'] as $u_ext_id => $u_ent) {
    $map->$u_ext_id = $u_ent['login'];
  }
  return $map;
}

function scopeSharedMailboxIdToNameMap($scopeSharedMailboxIds) {
  $map = new stdClass();
  if ($scopeSharedMailboxIds == null) {
    return $map;
  }
  foreach ($scopeSharedMailboxIds as $mailshareId) {
    $mailshare = get_entity_info($mailshareId, 'mailshare');
    $map->$mailshareId = $mailshare['label'];
  }
  return $map;
}

function http_put($url, $json) {
  $init_config = build_http_config_with_auth();
  $config = HttpConfig::from_config($init_config, array(
    jsonHeaders()
  ));
  $httpClient = new HttpClient($config);
  return $httpClient->put(new Url(imapArchiveRootPath().$url), $json);
}

function build_http_config_with_auth() {
  global $obm;

  $userInfo = get_user_info_with_email_or_die($obm['uid']);

  $username = $userInfo["login"];
  $password = get_trust_token($userInfo);
  $domainName = $obm['domain_name'];
  return new HttpConfig(array(
    new BasicAuth(new Credentials($username, $password, $domainName))
  ));
}

///////////////////////////////////////////////////////////////////////////////
// Launch manually an archive on IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function manual_launch($archiveTreatmentKind) {
  global $obm;

  $manualLaunchPath = ImapArchiveUrls::treatments($obm["domain_uuid"]);

  $maxRedirects = 1;
  return http_post($manualLaunchPath, array("archive_treatment_kind" => $archiveTreatmentKind), $max_redirects)->as_json();
}

///////////////////////////////////////////////////////////////////////////////
// Get log on IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function get_logs($runId) {
  global $obm;

  $logPath = ImapArchiveUrls::logs($obm["domain_uuid"], $runId);
  $imapArchiveRootPath = imapArchiveRootPath();

  $auth_config = build_http_config_with_auth();
  $config = HttpConfig::from_config($auth_config,
    array(
      new HttpContentCallback(function ($chunk) {
        echo "$chunk";
        ob_flush();
        flush();
      })
    )
  );
  $httpClient = new HttpClient($config);
  return $httpClient->get(new Url($imapArchiveRootPath.$logPath));
}

///////////////////////////////////////////////////////////////////////////////
// Treatments history from IMAP archive service
// Parameters:
//   - none
///////////////////////////////////////////////////////////////////////////////
function treatments_history($onlyFailure, $limit, $sort) {
  global $obm;

  $historyPath = ImapArchiveUrls::treatments($obm["domain_uuid"]);

  $parameters = array("domainId" => $obm['domain_uuid'],
      "filter_terminated" => 'true',
      "filter_failure" => ($onlyFailure) ? 'true' : 'false',
      "limit" => $limit,
      "sort_by_date" => $sort
  );
  return http_get($historyPath, $parameters)->as_json();
}

function get_user_info_with_email_or_die() {
  global $obm;
  $userInfo = get_user_info($obm['uid']);
  if(empty($userInfo["email"])) {
    throw new ImapArchiveNoEmailException();
  }
  return $userInfo;
}

class ImapArchiveException extends Exception {}

class ImapArchiveNoEmailException extends Exception {}

class ImapArchiveServerNotFoundException extends Exception {}
?>
