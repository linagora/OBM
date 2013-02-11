<?php

class CheckResult {
  
  /**
   * One of CheckStatus::OK, WARN or ERROR
   */
  public $status;
  
  public $messages;
  
  public function __construct($status, $messages = null) {
    $this->status = $status;
    $this->messages = $messages;
  }
  
}