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
  
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200) {
    return array(1, null);
  }
  if($errno){
    error_log("Couldn't login to IMAP archive server (".$errno.")");
    return array(0, $l_could_not_login_imap_archive_server);
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
  
  if ($code == 0) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  if ($code == 200) {
    return array(1, json_decode($success));
  }
  if($errno){
    error_log("Couldn't login to IMAP archive server (".$errno.")");
    return array(0, $l_could_not_login_imap_archive_server);
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
  
  $userInfo = get_user_info($obm['uid']);
  $imapArchiveRootPath = imapArchiveRootPath();
  $username = $userInfo["login"];
  $domainName = $userInfo["domain_name"];
  $password = get_trust_token($userInfo);
  $domainName = $obm['domain_name'];
  $origin= $GLOBALS['c_origin_web'];

  $params = array("login" => $username,
    "password" => $password,
    "domain_name" => $domainName,
    "origin" => $origin);
  if ($userInfo["password_type"] == "MD5SUM") {
      $params = array_merge($params, array("isPasswordHashed" => "true"));
      error_log("Password already hashed, adding 'isPasswordHashed' parameter to the imap-archive call");
  }
  
  $curlUrl = $imapArchiveRootPath.$url."?".http_build_query($params);
  
  $curl = curl_init($curlUrl);
  curl_setopt($curl, CURLOPT_RETURNTRANSFER, 1);
  $success = curl_exec($curl);
  $code = curl_getinfo($curl, CURLINFO_HTTP_CODE);
  $errno = curl_errno($curl);
  curl_close($curl);
  
  return array("success" => $success, "code" => $code, "errno" => $errno);
}
?>
