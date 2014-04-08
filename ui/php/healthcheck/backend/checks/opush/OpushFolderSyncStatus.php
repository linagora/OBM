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

class OpushFolderSyncStatus extends AbstractUserStatus {
  
  const SPUSHNIK_URL = "http://%SPUSHNIK_HOST%:%SPUSHNIK_PORT%/spushnik/foldersync?serviceUrl=https://%OPUSH_HOST%/Microsoft-Server-ActiveSync/";
  
  public function executeForDomain($domain) {
    $user = $this->getTestUser();
    $servers = of_domain_get_domain_opushfrontendserver($domain["id"]);
    if (empty($servers)) {
      return new CheckResult(CheckStatus::OK);
    }

    try {
      $spushnik = self::getSpushnikConfiguration();
    } catch (Exception $e) {
      return new CheckResult(CheckStatus::WARNING, array($e->getMessage()));
    }

    $url = str_replace("%SPUSHNIK_HOST%", $spushnik['spushnikIp'], self::SPUSHNIK_URL);
    $url = str_replace("%SPUSHNIK_PORT%", $spushnik['spushnikPort'], $url);
    
    foreach ($servers as $server) {
      $host = $server[0];      
      $url = str_replace("%OPUSH_HOST%", $host["ip"], $url);
      $spushnikResult = self::callSpushnik($url, $user, $domain);

      if (is_array($spushnikResult) && !empty($spushnikResult)) {
        return self::failure($host['ip'], $spushnikResult);
      }
      
    }
    return new CheckResult(CheckStatus::OK);
  }
  
  private static function getSpushnikConfiguration() {
    $confFile = dirname(__FILE__) . "/../../../../../conf/healthcheck.ini";
    
    if (!is_readable($confFile)) {
      throw new Exception("File '" . $confFile . "' doesn't exist or isn't readable.");
    }
    
    $ini = parse_ini_file($confFile, true);
    
    if (!isset($ini['spushnik'])) {
      throw new Exception("Configuration file '/etc/obm/healthcheck.ini' doesn't contain a 'spushnik' section.<br />" .
        "Example: " .
        "<br /> <br /> [spushnik] <br /> spushnikIp=127.0.0.1 <br /> spushnikPort=8083");
    }
    
    $spushnik = $ini['spushnik'];
    
    if (!isset($spushnik['spushnikIp']) || !isset($spushnik['spushnikPort'])) {
      throw new Exception("Section 'spushnik' in configuration file '/etc/obm/healthcheck.ini' should contain 'spushnikIp' and 'spushnikPort' entries. <br />" .
        "Example: " .
        "<br /> <br /> [spushnik] <br /> spushnikIp=127.0.0.1 <br /> spushnikPort=8083");
    }
    
    return $spushnik;
  }
  
  private static function failure($host, $messages) {
    return new CheckResult(CheckStatus::ERROR, array_merge(array("FolderSync scenario failed for Opush service at '" . $host . "'."), $messages));
  }
  
  /**
   * Calls an spushnik endpoint.
   * This returns either:
   * <ul>
   * <li>An array of strings if the call fails. The array holds all applicable error messages.</li>
   * <li>A string if the call succeeds</li>
   * </ul>
   * 
   * @param string $url Base Spushnik URL
   * @param string $user OBM user credentials
   * 
   * @return multitype:string
   */
  private static function callSpushnik($url, $user, $domain) {
    $messages = array();
    $params = json_encode(array("loginAtDomain" => $user['login'] . '@' . $domain['name'], "password" => $user['password']));
    $curl = CheckHelper::curlPost($url, array('Accept: application/json', 'Content-type: application/json'), $params);

    if (!$curl['success'] || $curl['code'] != 200) {
      $messages[] = "Failed to invoke Spushnik (Code: " . $curl['code'] . " , Errno: " . $curl['errno'] . ")";
    } else {
      $success = json_decode($curl['success']);
      $messages = $success->messages;
    }
    return $messages;
  }
  
}
