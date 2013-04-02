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
 
require_once dirname(__FILE__) . '/../AbstractMultiDomainStatus.php';

class JavaVersionStatus extends AbstractMultiDomainStatus {
  
  const EXPECTED_JAVA_VERSION = "1.6";
  const JAVA_VERSION_URL = "http://%HOST%:8080/obm-sync/healthcheck/java/version";
  
  public function executeForDomain($domain) {
    $servers = of_domain_get_domain_syncserver($domain["id"]);
    
    foreach ($servers as $server) {
      $host = $server[0];
      $url = str_replace("%HOST%", $host["ip"], self::JAVA_VERSION_URL);
      $curl = CheckHelper::curlGet($url);
      
      if ($curl["code"] == 200) {
        $version = $curl["success"];
        $versionOk = strpos($version, self::EXPECTED_JAVA_VERSION) === 0;
        
        return $versionOk ? new CheckResult(CheckStatus::OK) : new CheckResult(CheckStatus::ERROR, array("OBM-Sync server at '" . $host["ip"] . "' for domain '" . $domain["name"] . "' runs Java version '" . $version . "', expecting '" . self::EXPECTED_JAVA_VERSION . "'."));
      }
      
      return new CheckResult(CheckStatus::WARNING, array("OBM-Sync server at '" . $host["ip"] . "' for domain '" . $domain["name"] . "' isn't reachable"));
    }
  }
  
}