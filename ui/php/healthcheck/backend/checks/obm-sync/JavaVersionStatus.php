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
 
require_once dirname(__FILE__) . '/../AbstractMultiDomainStatus.php';

class JavaVersionStatus extends AbstractMultiDomainStatus {
  
  const EXPECTED_SUN_JAVA_VERSION = "1.6";
  const EXPECTED_OPENJDK_JAVA_VERSION = "1.7";
  const OPENJDK_VM_NAME = "OpenJDK";
  const JAVA_VERSION_URL = "http://%HOST%:8080/obm-sync/healthcheck/java/version";
  const JAVA_VMNAME_URL = "http://%HOST%:8080/obm-sync/healthcheck/java/vmname";
  
  public function executeForDomain($domain) {
    $servers = of_domain_get_domain_syncserver($domain["id"]);
    
    foreach ($servers as $server) {
      $host = $server[0];
      $urlvmname = str_replace("%HOST%", $host["ip"], self::JAVA_VMNAME_URL);
      $curlvmname = CheckHelper::curlGet($urlvmname);

      if ($curlvmname["code"] == 200) {
        $urlversion = str_replace("%HOST%", $host["ip"], self::JAVA_VERSION_URL);
        $curlversion = CheckHelper::curlGet($urlversion);
      
        if ($curlversion["code"] == 200) {
          $vmname = $curlvmname["success"];
          $version = $curlversion["success"];
          return self::checkJavaVersion($vmname, $version);
        }
      }
      
      return new CheckResult(CheckStatus::WARNING, array("OBM-Sync server at '" . $host["ip"] . "' for domain '" . $domain["name"] . "' isn't reachable"));
    }
  }
 
  private static function checkJavaVersion($vmname, $version) {
    if (strpos($vmname, self::OPENJDK_VM_NAME) !== false) {
      $versionOk = strpos($version, self::EXPECTED_OPENJDK_JAVA_VERSION) === 0;
      return self::checkResult($versionOk, $version, self::EXPECTED_OPENJDK_JAVA_VERSION);
    } 
        
    $versionOk = strpos($version, self::EXPECTED_SUN_JAVA_VERSION) === 0;
    return self::checkResult($versionOk, $version, self::EXPECTED_SUN_JAVA_VERSION);
  } 

  private static function checkResult($versionOk, $version, $expectedVersion) {
      return $versionOk ? new CheckResult(CheckStatus::OK) : new CheckResult(CheckStatus::ERROR, array("OBM-Sync server at '" . $host["ip"] . "' for domain '" . $domain["name"] . "' runs Java version '" . $version . "', expecting '" . $expectedVersion . "'."));
  }
}
