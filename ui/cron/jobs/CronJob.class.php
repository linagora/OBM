<?php

class CronJob {

  var $logger;
  
  function CronJob() {
    $this->logger = new Logger(get_class($this));
  }

  function mustExecute($date) {
    return false;
  }

  function execute($date) {
    return true;
  }

  function dependsOn() {
    return array();
  }
}
?>
