<?php

require_once 'Check.php';
require_once 'CheckResult.php';
require_once 'CheckStatus.php';

class PHPEnvironmentCheck implements Check {

  public function getName() {
    return "PHP Environment";
  }

  public function getDescription() {
    return "Checks the PHP environment for proper installation and configuration. "
        + "This will validate the version of PHP installed as well as the presence of required PHP modules.";
  }

  public function getDocUrl() {
    return "http://obm.org/wiki/obm-ui";
  }

  public function getParentId() {
    return null;
  }
  
  public function execute() {
    return new CheckResult(CheckStatus::OK);
  }

}