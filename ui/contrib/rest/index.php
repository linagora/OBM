<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
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

function create_xml($entity,$root_node,$name_entity,$multisearch=false){
  $xml_doc = new DOMDocument('1.0', 'UTF-8') ;
  
  $head_node = $xml_doc->createElement($root_node) ;
  $xml_doc->appendChild($head_node) ;
  
  foreach($entity as $key => $data){
    if($multisearch){
      // create entity node
      $entity_node = $xml_doc->createElement($name_entity) ;
      $head_node->appendChild($entity_node) ;
      // foreach data, create xml node, add it to contact node
      foreach ($data as $name => $value) {
        $new_node = $xml_doc->createElement($name, utf8_encode(htmlspecialchars($value, ENT_COMPAT))) ;
        $entity_node->appendChild($new_node) ;
      }
    } else {
      foreach ($data as $name => $value) {
        create_listing_node(&$xml_doc, $head_node, $name_entity, $value) ;
      }
    }
  }
  // return xml doc
  $xml_string = $xml_doc->saveXML() ;
  return $xml_string ;
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
    $new_node = $xml_doc->createElement($entity, utf8_encode(htmlspecialchars($entity_id, ENT_COMPAT)))  ;
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
* search_multicritere
 *
 * Analyse multivalued criteria in search string
 *
 * @param array $get array of all search fields (fields must be pre-filtered for obm run_query_search use)
 * @param array $multicritere array, passed by reference, that will store all criteria
 * @param string $champ passed by reference, will store the name of the multivalued criteria
 *
 */

function search_multicritere($get,&$multicritere,&$champ){
  global $multi_value_separator;
  // boolean : is there a multivalued criteria ?
  $multi=false;
  // get all multivalued criterii
  $pattern = "/^.*[$multi_value_separator].*$/";
  $multivalue = preg_grep($pattern,$get);
  // is there one and only one multivalued field
  // if more than one multivalued field, send an error 500
  $nb_multi = count($multivalue);
  if($nb_multi > 1){
    internal_error('only one multivalued search criteria');
  } elseif ($nb_multi == 1) {
    // else get all values from the multivalued criteria
    // just say that we have one multivalued criteria    
    $multi = true;
    $champ = key($multivalue);
    $multicritere = explode($multi_value_separator,$multivalue[$champ]);
  }

  // returns if there's a multivalued criteria or not
  return $multi;
}

/* prepare_field_list
 * 
 * analyse if required_fields are asked
 *
 * @param array $get array all fields of search string
 * @param array $source_fields array of allowed fields
 * @return array array of exported fields
 */
function prepare_field_list($get,$source_field){
  global $multi_value_separator;
  
  $field_list = array();

  $require_fields = $get['require_fields']; 

  // Is there a required_field parameter ?
  // If there's a required_field, get all required fields
  if(isset($require_fields) && !empty($require_fields)){
    $require_fields = explode($multi_value_separator,$require_fields);
    $field_list = array_intersect($source_field,$require_fields);
    // Are the required fields allowed ?
    $valid = array_diff($require_fields,$field_list);
    // If not allowed, send an error 500
    if(!empty($valid)){
      internal_error('invalid required field');
    }
  } else {
    $field_list=$source_field;
  }
  return $field_list;
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
