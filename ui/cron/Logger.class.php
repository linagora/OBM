<?php
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
    $message = $this->formatMessage($message);
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
  function log($message,$level, $caller) {
    if($level <= L_LEVEL) {
      echo date("Y-m-d H:i:s")." [$level] [$caller] : $message \n";
    }
  }
  
}

function errorHandler($code, $message, $file, $line) {
    global $logger,$job;
    /* Map the PHP error to a Log priority. */
    switch ($code) {
    case E_WARNING:
      Logger::log("$message in $file at $line",L_WARN, $file);
      break;
    case E_ERROR:
      Logger::log("$message in $file at $line",L_CRITICAL, $file);
      break;
    case E_NOTICE:
        break;      
    default:
      Logger::log("$message in $file at $line", L_INFO, $file);
      break;
    }
}

?>
