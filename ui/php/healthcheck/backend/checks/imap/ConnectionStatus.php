<?php

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
 
require_once dirname(__FILE__) . '/../AbstractUserStatus.php';

class ConnectionStatus extends AbstractUserStatus {
  
  const IMAP_INBOX_URL = "imap://%HOST%/INBOX";
  
  public function executeForDomain($domain) {
    global $obm;
    
    $user = $this->getTestUser();
    $servers = of_domain_get_domain_mailserver('imap', $domain['id']);
    
    $obm['domain_id'] = $domain['id']; //So that get_user_id works as expected
    $userInfo = get_user_info(get_user_id($user['login']));
    
    foreach ($servers as $server) {
      $host = $server[0];      
      $url = str_replace("%HOST%", $host["ip"], self::IMAP_INBOX_URL);
      $curl = CheckHelper::curlGet($url, $userInfo['email'], $user['password']);
      
      if ($curl["errno"] != 0) {
        return new CheckResult(CheckStatus::ERROR, array("IMAP connection failed (using URL: " . $url . ") for domain '" . $domain["name"] . "'. Error: " . $curl['errno']));
      }
    }
    
    return new CheckResult(CheckStatus::OK);
  }
  
}