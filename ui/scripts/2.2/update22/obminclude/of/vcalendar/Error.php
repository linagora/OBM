<?php


class Error {
  
  var $fatals;
  var $errors;
  var $warnings;
  var $object;
  
  function Error () {
    $this->fatals = array();
    $this->errors = array();
    $this->warning = array(); 
  }
  
  function  fatals ($message, $id) {
    $this->fatals[$id] = $message;
  }
  
  function error ($message, $id) {
    $this->errors[$id] = $message;
  }
  
  function warning ($message, $id) {
    $this->warning[$id] = $message;
  }

  

}
?>