<?php

/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

class SyncStatus extends AbstractUserStatus {
  
  const SYNC_URL = "http://%HOST%:8080/obm-sync/services";
  
  public function executeForDomain($domain) {
    global $obm;

    $user = $this->getTestUser();
    $servers = of_domain_get_domain_syncserver($domain["id"]);
    
    foreach ($servers as $server) {
      $host = $server[0];      
      $url = str_replace("%HOST%", $host["ip"], self::SYNC_URL);
      $loginResult = self::callSync($url, "login/doLogin", array(
          "login" => $user['login'] . '@' . $domain['name'],
          "password" => $user['password'],
          "origin" => $user['origin']
      ));

      if (is_array($loginResult)) {
        return self::failure($domain['name'], $host['ip'], $loginResult);
      } else {
        $sid = (string) $loginResult->sid;
        $abSyncResult = self::callSync($url, "book/getAddressBookSync", array(
            "sid" => $sid,
            "lastSync" => time() * 1000 // Using current time to receive no changes/deletions
        ));
        
        if (is_array($abSyncResult)) {
          return self::failure($domain['name'], $host['ip'], $abSyncResult);
        }

        $obm['domain_id'] = $domain['id']; //So that get_user_id works as expected
        $userInfo = get_user_info(get_user_id($user['login']));

        $calSyncResult = self::callSync($url, "calendar/getSyncWithSortedChanges", array(
            "sid" => $sid,
            "calendar" => $userInfo['email'],
            "lastSync" => time() * 1000
        ));
        
        if (is_array($calSyncResult)) {
          return self::failure($domain['name'], $host['ip'], $calSyncResult);
        }
      }
      
      return new CheckResult(CheckStatus::OK);
    }
  }
  
  private static function failure($domain, $host, $messages) {
    return new CheckResult(CheckStatus::ERROR, array_merge(array("Synchronization failed for OBM-Sync server at '" . $host . "' for domain '" . $domain . "'."), $messages));
  }
  
  /**
   * Calls an obm-sync endpoint.
   * This returns either:
   * <ul>
   * <li>An array of strings if the call fails. The array holds all applicable error messages.</li>
   * <li>A SimpleXMLElement holding the returned XML data if the call succeeds</li>
   * </ul>
   * 
   * @param string $url Base obm-sync server URL
   * @param string $action obm-sync action, in the form of "handler/method".
   * @param array $params The parameters to send. This must be an assoc. array.
   * 
   * @return SimpleXMLElement|multitype:string
   */
  private static function callSync($url, $action, $params) {
    $messages = array();
    $curl = CheckHelper::curlGet($url . '/' . $action . '?' . http_build_query($params));
    
    if (!$curl['success'] || $curl['code'] != 200) {
      $messages[] = "Failed to invoke '" . $action . "' (Code: " . $curl['code'] . ")";
    } else {
      $xml = new SimpleXMLElement($curl['success']);
      
      if ($xml->getName() == "error") {
        $messages[] = "Failed to invoke '" . $action . "' (Error: " . $xml->message[0] . ")";
      } else {
        return $xml; // HTTP 200 and no <error />, call succeeded
      }
    }
    
    return $messages;
  }
  
}