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

require_once 'IncludeCheckLoader.php';
require_once 'IncludeModuleLoader.php';

class Service {
  
  const MODULE_PHP = "Module.php";
  const CHECKS = "checks";
  private $checkLoader;
  private $moduleLoader;
  
  function route($path) {
    $pathExploded = explode("/", $path);
    $pathExploded = array_slice($pathExploded, 1);
    $pathLength = count($pathExploded);
    $module = $pathLength > 0 ? $pathExploded[0] : null;
    
    if (empty($module)) {
      return $this->getAvailableModules();
    }
    
    if ($pathLength < 1) {
      throw new InvalidArgumentException("Invalid URL, expecting '/<module>' or '/<module>/<test>'");
    }

    if ($pathLength == 1 || empty($pathExploded[1])) {
      return $this->getModuleChecks($module);
    }
    
    return $this->executeCheck($module, $pathExploded[1]);
  }

  function getModuleChecks($module) {
    $file = dirname(__FILE__) . "/" . Service::CHECKS . "/$module/" . Service::CHECKS . ".json";

    if ( !file_exists($file) ) {
      throw new InvalidArgumentException("Unknown module $module ");
    }

    return file_get_contents($file);
  }
  
  function getAvailableModules() {
    $this->loadGlobalInc();

    $domains = $this->listDomains();
    $modulesToPlay = array();
    $checksFolderName = dirname(__FILE__) . "/" . Service::CHECKS;
    $checksFolder = opendir($checksFolderName);
    while (($checkFolder = readdir($checksFolder)) !== false) {
      if ($checkFolder == "." || $checkFolder == "..") {
        continue;
      }
      if ($this->isAModule($checksFolderName . "/" . $checkFolder)) {
        $module = $this->loadModule($checksFolderName, $checkFolder);
        if ($module != NULL && $this->isModuleEnabled($module, $domains)) {
          $modulesToPlay = array_merge(array($module), $modulesToPlay);
        }
      }
    }
    closedir($checksFolder);
    return json_encode(array("modules" => $modulesToPlay));
  }

  function listDomains() {
    return of_domain_get_list();
  }

  function isAModule($moduleFolder) {
    return is_file($moduleFolder . '/' . self::MODULE_PHP);
  }

  function loadModule($modulesFolderName, $moduleFolder) {
    if (!isset($this->moduleLoader)) {
      $this->moduleLoader = new IncludeModuleLoader();
    }
    
    $module = $this->moduleLoader->load($modulesFolderName, $moduleFolder);
    
    if (!isset($module)) {
      throw new InvalidArgumentException("Module " . $modulesFolderName . "/" . $moduleFolder . " doesn't contains module informations");
    }
  
    return $module;
  }

  function isModuleEnabled($module, $domains) {
    return $module->isEnabled($domains);
  }

  function loadGlobalInc() {
    global $obmdb_host, $obmdb_dbtype, $obmdb_db, $obmdb_user, $obmdb_password;

    $path = "../..";
    $obminclude = getenv("OBM_INCLUDE_VAR");
    if ($obminclude == "") {
      $obminclude = "obminclude";
    }
    require_once "$obminclude/global.inc";
  }
  
  function executeCheck($module, $id = null) {
    if (!isset($id)) {
      throw new InvalidArgumentException("ExecuteCheck needs a check identifier");
    }
    
    if (!isset($this->checkLoader)) {
      $this->checkLoader = new IncludeCheckLoader();
    }
    
    $check = $this->checkLoader->load($module, $id);
    
    if (!isset($check)) {
      throw new InvalidArgumentException("Check '$id' doesn't exist for module '$module'");
    }
  
    return json_encode($check->execute());
  }
  
  function setCheckLoader($loader) {
    $this->checkLoader = $loader;
  }
  
}
