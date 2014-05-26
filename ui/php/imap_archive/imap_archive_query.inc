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
  global $l_email_address_not_found, $l_imap_archive_server_not_found, $l_imap_archive_server_unreachable, $l_could_not_login_imap_archive_server;
  $userInfo = get_user_info($obm['uid']);
  
  if(empty($userInfo["email"])) {
    return array(0, $l_email_address_not_found);
  }
  
  $imapArchiveServer = of_domain_get_domain_imaparchiveserver($obm['domain_id']);
  if(!count($imapArchiveServer)){
    return array(0, $l_imap_archive_server_not_found);
  }
  $iterator = new ArrayIterator($imapArchiveServer);
  $imapArchiveServer = $iterator->current();
  $imapArchiveRootPath = "http://".$imapArchiveServer[0]["ip"].":8085";
  $loginPath = "/imap-archive/services/v1/status";
  $username = $userInfo["login"];
  $password = get_trust_token($userInfo);
  $origin= $GLOBALS['c_origin_web'];

  $params = array("login" => $username,
    "password" => $password,
    "origin" => $origin);
  if ($userInfo["password_type"] == "MD5SUM") {
      $loginUrl .= "&isPasswordHashed=true";
      $params = array_merge($params, array("isPasswordHashed" => "true"));
      error_log("Password already hashed, adding 'isPasswordHashed' parameter to the imap-archive call");
  }
  
  $loginUrl = $imapArchiveRootPath.$loginPath."?".http_build_query($params);
  $xmlLogin = new DomDocument();
  $loadResult = $xmlLogin->load($loginUrl);
  if ($loadResult == false) {
    return array(0, $l_imap_archive_server_unreachable);
  }
  $errors = $xmlLogin->getElementsByTagName("error");
  if($errors->length > 0){
    error_log("Couldn't login to IMAP archive server (".$errors->item(0)->nodeValue.")");
    return array(0, $l_could_not_login_imap_archive_server);
  }

  return array(1, null);
}
?>
