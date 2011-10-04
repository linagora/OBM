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
 * get_company_list
 *
 * @return string XML representation of company listing
 */
function get_company_list() {
  // get sql ids
  $ids = array() ;
  $ids = get_company_list_ids() ;
  
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
    $id_node = create_listing_node(&$xml_doc, $listing_node, 'company', $id) ;
  }

  // return xml doc
  $xml_string = $xml_doc->saveXML() ;
  return $xml_string ;
}

/**
 * get_company_list_ids
 *
 * @return array list of company's id
 */
function get_company_list_ids() {
  $datas = array() ;

  $params['sql_order_field'] = 'company_id' ;
  $params['sql_order_desc'] = 'DESC' ;

  $obm_q = run_query_company_search($params) ;

  // if there are some companies
  if ($obm_q->num_rows() > 0) {
    while ($obm_q->next_record()) {
      $datas[] = $obm_q->f('id') ;
    }
  }

  return $datas ;
}

/**
 * get_company_detail
 *
 * return string XML representation of one company (company fields and company's contacts listing)
 */

function get_company_detail() {
  // get sql datas
  $datas = get_company_detail_by_id($_GET['entity_id']) ;
  $contacts = get_contact_list_ids($_GET['entity_id']) ;

  // if no datas, return
  if (count($datas) == 0) {
    return ;
  }

  // else
  // create xml doc
  $xml_doc = new DOMDocument('1.0', 'UTF-8') ;

  // create company node
  $entity_node = $xml_doc->createElement('company') ;
  $xml_doc->appendChild($entity_node) ;

  // foreach data, create xml node, add it to company node
  foreach ($datas as $data) {
    foreach ($data as $name => $value) {
      $new_node = $xml_doc->createElement($name, utf8_encode(htmlspecialchars($value, ENT_COMPAT))) ;
      $entity_node->appendChild($new_node) ;
    }
  }

  // create contacts node
  $contacts_node = $xml_doc->createElement('company_contacts') ;
  $entity_node->appendChild($contacts_node) ;

  // foreach contact ,create xml node, add it to contacts node
  if (count($contacts) != 0) {
    foreach($contacts as $contact_id) {
      $id_node = create_listing_node(&$xml_doc, $contacts_node, 'contact', $contact_id) ;
    }
  }

  // return xml doc
  $xml_string = $xml_doc->saveXML() ;
  return $xml_string ;
}

/**
 * get_company_detail_by_id
 *
 * @param integer $id id of the company
 * @return array list of company fields
 */
function get_company_detail_by_id($id) {
  $field_list = array('company_id' => 'company_id', 
  		 'company_name' => 'company_name',
		 'company_number' => 'company_number', 
		 'company_nafcode_id' => 'company_isin',
		 'company_address1' => 'company_address1',
		 'company_address2' => 'company_address2',
		 'company_address3' => 'company_address3',
		 'company_town' => 'company_town',
		 'company_zipcode' => 'company_zipcode',
		 'country_name' =>  'company_country',
		 'company_phone' => 'company_phone',
		 'company_fax' => 'company_fax',
		 'company_email' => 'company_email',
		 'company_web' => 'company_web') ;
  $datas = array() ;

  $obm_q = run_query_company_detail($id) ;
  if ($obm_q->num_rows() > 0) {
    foreach($field_list as $sql_field => $name) {
      $row[$name] = $obm_q->f($sql_field) ;
    }
    $datas[] = $row ;
  }

  return $datas ;
}

/**
 * get_company_search
 *
 * @return string XML representation of the search results (list of companies ids that match the query)
 */
function get_company_search() {
  // the obm var nams and the search parameters names may be different
  $replacement = array('name' => 'name',
    'number'=>'number',
    'phone'=>'phone',
    'zip'=>'zip',
    'town'=>'town',
    'country'=>'country',
    'type'=>'type',
    'activity'=>'activity',
    'naf'=>'naf',
    'datasource'=>'datasource',
    'market'=>'market',
    'date_after'=>'date_after',
    'date_before'=>'date_before'
  );

  $field_list = array('company_id' => 'company_id', 
  		 'company_name' => 'company_name',
		 'company_number' => 'company_number', 
		 'company_nafcode_id' => 'company_isin',
		 'company_address1' => 'company_address1',
		 'company_address2' => 'company_address2',
		 'company_address3' => 'company_address3',
		 'company_town' => 'company_town',
		 'company_zipcode' => 'company_zipcode',
		 'country_name' =>  'company_country',
		 'company_phone' => 'company_phone',
		 'company_fax' => 'company_fax',
		 'company_email' => 'company_email',
		 'company_web' => 'company_web'
   );

  $get=array();
  $root_node='listing';

  $multi_search = false;
  $require_fields = array();
  //permet de récupérer les champs qu'il faut affiché 
  $field_list = prepare_field_list($_GET,$field_list);

  foreach($replacement as $key => $value){
    if(isset($_GET[$value])){
      $get[$key]=$_GET[$value];
    }
  }
  
  //recupère tous les critères pour le recherche multiple
  $multi_search = search_multicritere($get,&$multicritere,&$champ);


  $datas = array();
  //Si c'est une recherche multicritere on recupere les informations
  //pour chaque critère
  if($multi_search){
    $root_node='multisearch';
    foreach($multicritere as $val){
      //a chaque critère on modifie le champ
      $get[$champ]=$val;
      $data = get_company_mono_search($get,$field_list);
      if(!empty($data)){
        //on fusionne les information que l'on as déjà avec les 
        //nouvelles pour evité les doublons
        $datas=array_merge($datas,$data);
      }
    }
  } else {
    $field_list=array('company_id' => 'company_id');
    $datas=get_company_mono_search($get,$field_list);
  }
  
  // if no datas, return
  if (count($datas) == 0) {
    return ;
  }
  return create_xml($datas,$root_node,'company',$multi_search);
}

/**
 * get_company_mono_search
 *
 * @return array list of entity fields
 */
function get_company_mono_search($get,$field_list) {
  
  $obm_q = run_query_company_search($get) ;
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
