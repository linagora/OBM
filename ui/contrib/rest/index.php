<?php

include_once('config.php') ;
include_once('contact.php') ;
include_once('company.php') ;

/**
 * internal_error
 *
 * @param string $msg Error message to send with HTTP header 500
 * @return HTTP header 500
 */
function internal_error($msg) {
  if ($msg == '') {
    $msg = 'no reason' ;
  }
  header('HTTP/1.0 500 Internal Error : '.$msg) ;
  exit() ;
}

/**
 * no_result
 *
 * Usually used when a search gives no result
 *
 * @return HTTP header 404
 */
function no_result() {
  header('HTTP/1.0 404 No result') ;
  exit() ;
}

/**
 * create_listing_node
 *
 * Used to create a xml listing of nodes, each node represents an entity id
 *
 * @param mixed $xml_doc xml object (previously created with DOM api)
 * @param mixed $listing_node dom object in wich to put the new node
 * @param string $entity obm entity type (contact or company)
 * @param string $entity_id obm id of the entity to add
 */
function create_listing_node($xml_doc, $listing_node, $entity, $entity_id) {
    $new_node = $xml_doc->createElement($entity, $entity_id)  ;
    $new_node->setAttributeNode(new DOMAttr('url', URL_REST.'/'.$entity.'/'.$entity_id));
    $listing_node->appendChild($new_node) ;
}

/**
 * sanitize_param
 *
 * Security control on parameter
 *
 * @param string $key the parameter to control
 * @return HTTP header 404
 */
function sanitize_param($key) {
  return filter_var($key, FILTER_SANITIZE_STRING, FILTER_FLAG_NO_ENCODE_QUOTES) ;
}

/**
 * dispatch
 *
 * Analyse the GET string and call the correct function
 *
 * @return the xml to be sent to the client
 */
function dispatch() {
  global $active_entities ;

  if (!isset($_GET['entity']) or ($_GET['entity'] == '')) {
    internal_error('Entity parameter not defined in request') ;
  }

  $entity = sanitize_param($_GET['entity']) or internal_error('sanitizing error') ;
  
  if (!in_array($entity, $active_entities)) {
    internal_error('Unknown entity "'.$entity.'"') ;
  }
  
  // default action
  $action = 'list' ;
  
  // search (RESTfull way does not like verbs in url, but complex search is usefull...)
  if (isset($_GET['action']) and ($_GET['action'] == 'search')) {
    unset($_GET['entity']) ;
    unset($_GET['action']) ;

    $action = 'search' ;
  }
  
  // detail
  if ((isset($_GET['entity_id'])) and ($_GET['entity_id'] != 0)) {
    $action = 'detail' ;
  }
  
  $func = 'get_'.$entity.'_'.$action ;
  if (!function_exists($func)) {
    internal_error("Can't find function ".sanitize_param($func)) ;
  }

  return $func() ;
}

// Do the job

$xml = dispatch() ;

if ($xml == '') {
  no_result() ;
}
else {
  header('Content-type: text/xml') ;
  echo $xml ;
}

?>
