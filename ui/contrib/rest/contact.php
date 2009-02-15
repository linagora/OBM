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

/**
 * get_contact_list
 *
 * @return string XML representation of contact listing
 */
function get_contact_list() {
  // get sql ids
  $ids = array() ;
  $ids = get_contact_list_ids() ;
  
  // if no ids
  if (count($ids) == 0) {
    return ;
  }

  // else
  // create xml doc
  $xml_doc = new DOMDocument('1.0', 'UTF-8') ;

  // create listing node
  $listing_node = $xml_doc->createElement('listing') ;
  $xml_doc->appendChild($listing_node) ;

  // foreach id, create xml node and add it to doc
  foreach($ids as $id) {
    $id_node = create_listing_node(&$xml_doc, $listing_node, 'contact', $id) ;
  }

  // return xml doc
  $xml_string = $xml_doc->saveXML() ;
  return $xml_string ;
}

/**
 * get_contact_list_ids
 *
 * @param integer $company_id (optionnal) id of company, to get contacts from only this company
 * @return array list of contact's id
 */
function get_contact_list_ids($company_id='') {
  $datas = array() ;

  $params['sql_order_field'] = 'contact_id' ;
  $params['sql_order_desc'] = 'DESC' ;

  if ($company_id != '') {
    $params['company_id'] = $company_id ;
    $obm_q = run_query_contact_search($params) ; 
  }
  else {
    $obm_q = run_query_contact_search($params) ;
  }

  while ($obm_q->next_record()) {
    $datas[] = $obm_q->f('id') ;
  }

  return $datas ;
}

/**
 * get_contact_detail
 *
 * return string XML representation of one contact
 */
function get_contact_detail() {
  // get sql datas
  $datas = get_contact_detail_by_id(sanitize_param($_GET['entity_id'])) ;

  // if no datas, return
  if (count($datas) == 0) {
    return ;
  }

  // else
  // create xml doc
  $xml_doc = new DOMDocument('1.0', 'UTF-8') ;

  // create contact node
  $entity_node = $xml_doc->createElement('contact') ;
  $xml_doc->appendChild($entity_node) ;

  // foreach data, create xml node, add it to contact node
  foreach ($datas as $data) {
    foreach ($data as $name => $value) {
      $new_node = $xml_doc->createElement($name, utf8_encode(htmlspecialchars($value, ENT_COMPAT))) ;
      $entity_node->appendChild($new_node) ;
    }
  }

  // return xml doc
  $xml_string = $xml_doc->saveXML() ;
  return $xml_string ;
}

/**
 * get_contact_detail_by_id
 *
 * @param integer $id id of the contact
 * @return array list of contact fields
 */
function get_contact_detail_by_id($id) {
  $field_list = array('contact_id' => 'contact_id', 
  	 'contactfunction_label' => 'contact_function',
		 'contact_firstname' => 'contact_firstname',
		 'contact_lastname' => 'contact_lastname',
		 'kind_minilabel' => 'contact_genre',
		 'contact_title' => 'contact_title',
		 'contact_service' => 'contact_office',
		 'contact_address1' => 'contact_address1',
		 'contact_address2' => 'contact_address2',
		 'contact_address3' => 'contact_address3',
		 'company_town' => 'company_town',
		 'company_zipcode' => 'company_zipcode',
		 'company_country_name' => 'company_country',
		 'company_phone' => 'company_phone',
		 'company_fax' => 'company_fax',
		 'contact_email' => 'contact_email') ;
  $datas = array() ;

  $obm_q = run_query_contact_detail($id) ;
  if ($obm_q->num_rows() > 0) {
    foreach($field_list as $sql_field => $name) {
      $row[$name] = $obm_q->f($sql_field) ;
    }
    $datas[] = $row ;
  }

  return $datas ;
}

/**
 * get_contact_search
 *
 * @return string XML representation of the search results (list of contact ids that match the query)
 */
function get_contact_search() {
  // the obm var nams and the search parameters names may be different
  $replacement = array('fname' => 'firstname',
    'lname' => 'lastname',
    'privacy' => 'privacy',
    'number'=>'number',
    'phone'=>'phone',
    'email'=>'email',
    'company'=>'company',
    'zip'=>'zip',
    'town'=>'town',
    'country'=>'country',
    'datasource'=>'datasource',
    'function'=>'function',
    'title'=>'title',
    'market'=>'market',
    'date_field'=>'date_type',
    'date_after'=>'date_after',
    'date_before'=>'date_before',
  );

   $field_list = array('contact_id' => 'contact_id', 
  	 'contactfunction_label' => 'contact_function',
		 'contact_firstname' => 'contact_firstname',
		 'contact_lastname' => 'contact_lastname',
		 'kind_minilabel' => 'contact_genre',
		 'contact_title' => 'contact_title',
		 'contact_service' => 'contact_office',
		 'contact_address1' => 'contact_address1',
		 'contact_address2' => 'contact_address2',
		 'contact_address3' => 'contact_address3',
		 'company_town' => 'company_town',
		 'company_zipcode' => 'company_zipcode',
		 'company_country_name' => 'company_country',
		 'company_phone' => 'company_phone',
		 'company_fax' => 'company_fax',
     'contact_email' => 'contact_email'
   );

  $get=array();
  $root_node='listing';
  
  $multi_search = false;
  $require_fields = array();

  $field_list = prepare_field_list($_GET,$field_list);
  
  foreach($replacement as $key => $value){
    if(isset($_GET[$value])){
      $get[$key]=$_GET[$value];
    }
  }

  $multi_search = search_multicritere($get,&$multicritere,&$champ);


  $datas = array();
  if($multi_search){
    $root_node='multisearch';
    foreach($multicritere as $val){
      $get[$champ]=$val;
      $data = get_contact_mono_search($get,$field_list);
      if(!empty($data)){
        $datas=array_merge($datas,$data);
      }
    }
  } else {
    $field_list=array('contact_id' => 'contact_id');
    $datas=get_contact_mono_search($get,$field_list);
  }
  
  // if no datas, return
  if (count($datas) == 0) {
    return ;
  }
  return create_xml($datas,$root_node,'contact',$multi_search);
}

/**
 * get_contact_search
 *
 * @return array list of entity fields
 */
function get_contact_mono_search($get,$field_list) {
  
  $obm_q = run_query_contact_search($get) ;
  // if no result
  if ($obm_q->num_rows_total() < 1) {
    return ;
  }
  
  $row = array();
  // foreach id, create xml node and add it to doc
  while($row = $obm_q->next_record()) {
    foreach($field_list as $sql_field => $name) {
      $data[$name]=$obm_q->f($sql_field);
    }
    $datas[" ".$obm_q->f('id')." "] = $data ;
  }
  return $datas ;
}

?>
