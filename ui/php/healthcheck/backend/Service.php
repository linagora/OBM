<?php

$method = $_GET["method"];

if ($method == "getAvailableChecks") {
  echo json_encode(getAvailableChecks());
} else if ($method == "executeCheck") {
  echo json_encode(executeCheck($_GET["id"]));
} else {
  header('HTTP/1.1 400');
}
  
function getAvailableChecks() {
  $return = array();
  
  foreach (glob("checks/*.php") as $filename) {
    $return[] = $filename;
  }
  
  return $return;
}

function executeCheck($id) {
  include "checks/$id.php";
  
  $check = new $id();
  
  return $check->execute();
}