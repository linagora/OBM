<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

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


define("L_CORE",7);
define("L_DEBUG",6);
define("L_NOTICE",5);
define("L_INFO",4);
define("L_WARN",3);
define("L_ERROR",2);
define("L_ALERT",1);
define("L_CRITICAL",0);


class Logger {

  var $className;
  
  function Logger($className) {
    $this->className = $className;
  }

  function critical($message) {
    Logger::log($message,L_CRITICAL,$this->className);
  }
  
  function alert($message) {
    Logger::log($message,L_ALERT,$this->className);
  }

  function error($message) {
    Logger::log($message,L_ERROR,$this->className);
  }
  
  function warn($message) {
    Logger::log($message,L_WARN,$this->className);
  }

  function info($message) {
    Logger::log($message,L_INFO,$this->className);
  }

  function debug($message) {
    Logger::log($message,L_DEBUG,$this->className);
  }

  function core($message) {
    Logger::log($message,L_CORE,$this->className);
  }

  // TODO set log handler
  static function log($message,$level, $caller) {
    if($level <= L_LEVEL) {
      if($level == L_CORE) $message = DB_OBM::xParser($message);
      echo date("Y-m-d H:i:s")." [".Logger::getLevelLabel($level)."] [$caller] : $message \n";
    }
  }

  static function getLevelLabel($level) {
    switch($level) {
    case L_CORE :
      return "Core";
    case L_DEBUG :
      return "Debug";
    case L_INFO :
      return "Info";
    case L_WARN :
      return "Warning";
    case L_ERROR :
      return "Error";
    case L_ALERT : 
      return "Alert";
    case L_CRITICAL :
      return "Fatal";
    }
  }
  
}

function errorHandler($code, $message, $file, $line) {
    global $logger,$job;
    if(error_reporting() & $code === $code)  {
      /* Map the PHP error to a Log priority. */
      switch ($code) {
      case E_WARNING:
        Logger::log("$message in $file at $line",L_WARN, $file);
        break;
      case E_ERROR:
        Logger::log("$message in $file at $line",L_CRITICAL, $file);
        break;
      case E_NOTICE:
        Logger::log("$message in $file at $line", L_CORE,$file);
        break;     
      case E_STRICT:
        Logger::log("$message in $file at $line", L_CORE, $file);
          break;
      default:
        Logger::log("$message in $file at $line", L_WARN, $file);
        break;
      }
    }
}

?>
