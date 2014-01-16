<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : global_query.inc                                             //
//     - Desc : Global Query File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get an OBM info
// Parameters:
//   - $info : info name
///////////////////////////////////////////////////////////////////////////////
function get_obm_info($info) {
  global $cdg_sql;

  $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='$info'";

  display_debug_msg($query, $cdg_sql, "get_obm_info($info)");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('obminfo_value');
  return $retour;
}


/**
 * Update the company contact number stored in company (only public contacts)
 * @param $comp_id company id
 **/
function run_query_global_company_contact_number_update($comp_id) {
  global $cdg_sql;

  $comp_id = sql_parse_id($comp_id, true);
  $query = "SELECT count(*) as nb
      FROM Contact
      WHERE contact_company_id $comp_id 
        AND contact_archive = '0'";

  display_debug_msg($query, $cdg_sql, 'run_query_global_company_contact_number_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  $query = "UPDATE Company
    SET company_contact_number='$nb'
    WHERE company_id $comp_id ";

  display_debug_msg($query, $cdg_sql, 'run_query_global_company_contact_number_update()');
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Data Source query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_global_datasource() {
  global $cdg_sql;

  $query = "SELECT * FROM DataSource ORDER BY datasource_name"; 
  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_global_datasource()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the country list filled for the current lang
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_global_country_for_lang() {
  global $cdg_sql;

  $lang = get_lang();
  $query = "SELECT * FROM Country
    WHERE country_lang='$lang'
    ORDER BY country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_global_country_for_lang()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Country query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_global_country() {
  global $cdg_sql;

  $query = "SELECT * FROM Country
     ORDER BY country_name, country_iso3166"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_global_country()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Function select query execution
// Returns:
//   DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_global_contactfunction() {
  global $cdg_sql, $cgp_hide;

  $show_func = (! $cgp_hide['contact']['contact_function']);
  if ($show_func) {
    $query = "SELECT contactfunction_id, contactfunction_label
    FROM ContactFunction
    ORDER by contactfunction_label";

    display_debug_msg($query, $cdg_sql, 'run_query_global_contactfunction()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
  } else {
    $obm_q = '';
  }

  return $obm_q;
}


/**
 * Get Lead sources infos
 * @return   $ret[] : [id]=[id][name]
 **/
function get_global_lead_sources() {
  global $cdg_sql;

  $ret = array();
  $multidomain = sql_multidomain('leadsource');
  $query = "SELECT * FROM LeadSource WHERE 1=1 $multidomain ORDER BY leadsource_label";

  display_debug_msg($query, $cdg_sql, 'get_global_lead_sources()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('leadsource_id');
    $label = $obm_q->f('leadsource_label');
    $ret[$id] = array('id' => $id, 'label' => $label);
  }

  return $ret;
}


/**
 * Get Deal type infos
 * @return   $ret[] : [id]=[id][name]
 **/
function get_global_deal_types() {
  global $cdg_sql;

  $ret = array();
  $multidomain = sql_multidomain('dealtype');

  if ($order != '') {
    $order_sql = ", $order";
  } else {
    $order_sql = ", dealtype_code, dealtype_label";
  }

  $query = "SELECT * FROM DealType
    WHERE 1=1 $multidomain
    ORDER BY dealtype_inout $order_sql";

  display_debug_msg($query, $cdg_sql, 'get_global_deal_types()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('dealtype_id');
    $inout = $obm_q->f('dealtype_inout');
    $code = $obm_q->f('dealtype_code');
    $label = $obm_q->f('dealtype_label');
    $ret[$id] = array('id' => $id,
		      'inout' => $inout,
		      'code' => $code,
		      'label' => $label);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// TaskType list query
// Parameters :
//   - $tt_cat : tasktype category
//               'all' or ''  return all tasktype
//               'project' '  return tasktype from $ctt_sales and $ctt_research
//               one category return the category
//   - $tt_ids : tasktype ids to select
// Returns : tasktype array [id] => [label],[internal]
///////////////////////////////////////////////////////////////////////////////
function get_global_tasktype($tt_cat='', $tt_ids='') {
  global $cdg_sql, $ctt_sales, $ctt_research;
  $tt = array();
  $multidomain = sql_multidomain('tasktype');
  $query = "
    SELECT
      tasktype_id,
      tasktype_code,
      tasktype_label, 
      tasktype_internal
    FROM TaskType";

  if (($tt_cat == '') || ($tt_cat == 'all')) {
    $query .= '';
  } elseif ($tt_cat == 'project') {
    $where .= "AND (tasktype_internal = $ctt_sales
                      or tasktype_internal = $ctt_research)";
  } else {
    $where .= "AND tasktype_internal = $tt_cat";
  }

  if (is_array($tt_ids)) {
    $where .= ' AND ';
    $where .= '(';
    foreach($tt_ids as $id) {
      $id = sql_parse_id($id, true);
      $where .= "$or tasktype_id $id ";
      $or = ' OR';
    }
    $where .= ')';
  } else if ($tt_ids != '') {
    if ($where != '') {
      $where .= ' AND ';
    }
    $tt_ids = sql_parse_id($tt_ids, true);
    $where .= " tasktype_id $tt_ids ";
  }
  
  $query .= " WHERE 1 = 1 $multidomain $where ";
  
  $query .= "ORDER BY tasktype_internal, tasktype_label";

  display_debug_msg($query, $cdg_sql, 'get_global_tasktype()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('tasktype_id');
    $code = $obm_q->f('tasktype_code');
    $label = $obm_q->f('tasktype_label');
    $internal = $obm_q->f('tasktype_internal');
    $tt[$id] = array ('label' => $label,
		      'code' => $code,
		      'internal' => $internal);
  }

  return $tt;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of Elements linked to an entity
// Parameters:
//   - $element : element linked 'Contact, Lead,...' (table name)
//   - $id      : Id of entity
//   - $entity  : entity (company)
//   - $arch    : If true : tell archived elements should be counted too
//   - $public  : If true : tell only public elements should be counted
// Return:
//   - $nb      : number of Contact
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_element_nb($element, $id, $entity, $arch='', $public=false) {
  global $cdg_sql;

  $id = sql_parse_id($id, true);
  $lelement = strtolower($element);
    
  $query = "SELECT count(${lelement}_id) as nb
    FROM $element
    WHERE ${lelement}_${entity}_id $id ";

  if (! $arch) {
    $query .= " AND ${lelement}_archive=0";
  }

  if ($public) {
    $query .= " AND ${lelement}_privacy=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_element_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of deals linked to an entity
// Parameters:
//   - $id      : Id of entity
//   - $entity  : entity (company, contact)
//   - $arch    : flag to tell if archived deal should be counted (if true)
//   - $entity2 : entity 2 (for contact as deal can have 2 contacts)
// Return:
//   - $nb      : number of Deal
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_deal_nb($id, $entity, $arch='', $entity2='') {
  global $cdg_sql;

  $id = sql_parse_id($id, true);
  if ($entity2 != '') {
    $where_ent = "(deal_${entity}_id $id OR deal_${entity2}_id $id )";
  } else {
    $where_ent = "deal_${entity}_id $id ";
  }

  $query = "SELECT count(deal_id) as nb
    FROM Deal
    WHERE $where_ent";

  if (! $arch) {
    $query .= " AND deal_archive=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_deal_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of projects linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company)
//   - $arch   : flag to tell if archived projects should be counted (if true)
// Return:
//   - $nb     : number of Project
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_project_nb($id, $entity, $arch='') {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT count(project_id) as nb
    FROM Project
    WHERE project_${entity}_id $id ";

  if (! $arch) {
    $query .= " AND project_archive=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_project_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of contract linked to an entity
// Parameters:
//   - $id      : Id of entity
//   - $entity  : entity (deal, company)
//   - $arch    : flag to tell if archived contract should be counted (if true)
//   - $entity2 : entity 2 (for contact as contract can have 2 contacts)
// Return:
//   - $nb      : number of contract
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_contract_nb($id, $entity, $arch='', $entity2='') {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  if ($entity2 != '') {
    $where_ent = "(contract_${entity}_id $id OR contract_${entity2}_id $id )";
  } else {
    $where_ent = "contract_${entity}_id $id ";
  }

  $query = "SELECT count(contract_id) as nb
    FROM Contract
    WHERE $where_ent";

  if (! $arch) {
    $query .= " AND contract_archive=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_contract_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of incidents linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (contract))
//   - $arch   : flag to tell if archived incidents should be counted (if true)
// Return:
//   - $nb     : # Incident
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_incident_nb($id, $entity, $arch='') {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT count(incident_id) as nb
    FROM Incident
    WHERE incident_{$entity}_id $id ";

  if (! $arch) {
    $query .= " AND incident_archive=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_incident_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of invoice linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, project)
//   - $arch   : flag to tell if archived invoices should be counted (if true)
// Return:
//   - $nb     : number of invoice
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_invoice_nb($id, $entity, $arch='') {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  if ($entity == 'deal') {
    $query = "SELECT count(invoice_id) as nb
      FROM Invoice
        LEFT JOIN Project on invoice_project_id=project_id
      WHERE (invoice_deal_id $id 
        OR project_deal_id $id )";
  } else {
    $query = "SELECT count(invoice_id) as nb
    FROM Invoice
    WHERE invoice_${entity}_id $id ";
  }
  if (! $arch) {
    $query .= " AND invoice_archive=0";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_invoice_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : number of payment linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (account, company, invoice)
// Return:
//   - $nb     : number of payment
///////////////////////////////////////////////////////////////////////////////
function get_global_linked_payment_nb($id, $entity) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  if ($entity == 'invoice') {
    $query = "SELECT count(paymentinvoice_payment_id) as nb
      FROM PaymentInvoice
      WHERE paymentinvoice_invoice_id $id ";
  } else {
    $query = "SELECT count(payment_id) as nb
    FROM Payment
    WHERE payment_${entity}_id $id ";
  }

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'get_global_linked_payment_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}

// Document 'API'
// of_document ?

///////////////////////////////////////////////////////////////////////////////
// Query execution : number of documents linked to an entity
// Parameters:
//   - $id     : Id of entity
//   - $entity : entity (deal, company, contact..)
// Return:
//   - $nb : document number
///////////////////////////////////////////////////////////////////////////////
function run_query_global_document_nb($id,$entity) {
  global $cdg_sql;
  $entity_id = of_entity_get($entity, $id);
  $query = "SELECT count(*) as nb
    FROM DocumentLink
    WHERE documentlink_entity_id = $entity_id"; 

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_global_document_nb()');
  $obm_q->next_record();

  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Insert document linked with a entity
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - $cpt_ins : # documents inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_global_insert_documents_links($ids, $entity) {
  global $cdg_sql;

  $id = $ids[$entity.'_id'];
  if($id == '') {
    $id = $ids['ext_id'];
  }

  $entity_id = of_entity_get($entity, $id);
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $ids['doc_nb']) {
    $cpt++;
    $d_id = sql_parse_id($ids["doc$cpt"], true);
    $query = "SELECT *
      FROM DocumentLink
      WHERE documentlink_entity_id = $entity_id 
        AND documentlink_document_id $d_id ";
    display_debug_msg($query, $cdg_sql, 'run_query_global_insert_documents_links(1)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $d_id = sql_parse_id($ids["doc$cpt"]);
      $query = "INSERT INTO DocumentLink (
	       documentlink_entity_id,
	       documentlink_document_id
       )VALUES (    
	       $entity_id,
	       $d_id
      )";
      display_debug_msg($query, $cdg_sql, 'run_query_global_insert_documents_links(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Delete document linked with an entity
// Parameters:
//   - $id     : entity Id
//   - $entity : Type of entity (deal, company..)
//   - $doc_id : [optionnal] document Id
///////////////////////////////////////////////////////////////////////////////
function run_query_global_delete_document_links($ent_id, $entity, $doc_id='') {
  global $cdg_sql;

  if(!$ent_id && !$entity && !$doc_id) {
    return true;
  }
  $where = '1=1';
  if ($ent_id != '') {
    $entity_id = of_entity_get($entity, $ent_id);
    $where .= " AND documentlink_entity_id = $entity_id ";
  }
  if ($doc_id != '') {
    $doc_id = sql_parse_id($doc_id, true);
    $where .= " AND documentlink_document_id $doc_id ";
  }

  $query = "DELETE FROM DocumentLink WHERE $where";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql,'run_query_global_delete_document_links()');
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Delete document with an entity
// Parameters:
//   - $id     : entity Id
//   - $entity : Type of entity (deal, company..)
//   - $doc_id : [optionnal] document Id
///////////////////////////////////////////////////////////////////////////////
function run_query_global_delete_document($doc_id) {
  global $cdg_sql;
  if(!$doc_id) {
    return true;
  }
  $multidomain = sql_multidomain('document');

  // Delete all links to documents
  run_query_global_delete_document_links('', '', $doc_id);

  // Delete the file
  run_query_global_document_delete_file($doc_id);
  of_entity_delete('document', $doc_id);
  $doc_id = sql_parse_id($doc_id, true);
  // Delete the database entry
  $query = "DELETE FROM Document 
		WHERE document_id $doc_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_global_delete_document()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Delete File from Disk
// Parameters:
//   - $id : id of file to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_global_document_delete_file($id) {

  $disk_path = get_global_document_disk_path($id);
  $disk_fullname = $disk_path . $id; 

  @rmdir($disk_fullname);
  @unlink($disk_fullname);

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Insert document
// Parameters:
//   - $id  :  Id of entity
//   - $entity  :  Type of entity (deal, company..)
// Return:
//   - $cpt_ins : # documents inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_global_insert_document($params, $id, $entity) {
  global $cdg_sql, $obm, $l_create_file_error;

  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm['uid']);

  $title = $params['title'];
  $usr_q = get_user_info($uid);
  $author = $usr_q['lastname'].' '.$usr_q['firstname'];
  $mime = sql_parse_id($params['type']);
  $name = $params['file_name'];
  $path = '/';
  $size = sql_parse_int($params['size']);
  $kind = '1'; // 1 means upload file
  $url = '';
  $privacy = '0';

  if (isset($params['domain']) && $obm['domain_global']) {
    $domain_id = sql_parse_id($params['domain']);
  } else {
    $domain_id = sql_parse_id($obm['domain_id']);
  }

  $query = "INSERT INTO Document (
      document_timeupdate,
      document_timecreate,
      document_userupdate,
      document_usercreate,
      document_domain_id,
      document_title,
      document_author,
      document_name,
      document_path,
      document_kind,
      document_size,
      document_mimetype_id,
      document_privacy
    ) VALUES (
      null,
      NOW(),
      null,
      $uid,
      $domain_id,
      '".$obm_q->escape($title)."',
      '".$obm_q->escape($author)."',
      '".$obm_q->escape($name)."',
      '".$obm_q->escape($path)."',
      $kind,
      $size,
      $mime,
      $privacy
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_global_insert_document(1)');
  $obm_q->query($query);
  $uid = sql_parse_id($obm['uid'], true);

  display_debug_msg($query, $cdg_sql, 'run_query_global_insert_document(2)');
 
  $id = $obm_q->lastid();
  if ($id) {
    of_entity_insert('document', $id);
    $params['document_id'] = $id;

    // Create the document on the disk
    if(!run_query_global_create_document($params)) {
      $err_msg = $l_create_file_error;
      // If error, delete file
      run_query_global_delete_document($id); // todo
    } else {
      $entity_id = of_entity_get($entity, $params[$entity.'_id']);
      // Insert links
      $query = "INSERT INTO DocumentLink (
        documentlink_entity_id,
        documentlink_document_id
       )VALUES (    
        $entity_id,
        $id)";
      display_debug_msg($query, $cdg_sql, 'run_query_global_insert_document(3)');
      $obm_q->query($query); 
      return $id;
    }
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// query execution : Update document
// Parameters:
//   - $params[]  : Entry's values
//   - $id        : $document_id
//   - $entity    : Type of entity (deal, company..)
// Return:
//   - $cpt_ins : # documents inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_global_update_document($params, $id) {
  global $obm, $cdg_sql;
  global  $l_delete_file_error, $l_create_file_error;
  
  $obm_q = new DB_OBM;
  $uid = sql_parse_id($obm['uid']);
  
  $mime = $params['type'];
  $name = $params['file_name'];
  $path = '/';
  $size = sql_parse_int($params['size']);
  $title = $params['title'];
  $iid = sql_parse_id($id, true);
  $query = "UPDATE Document SET
     document_timeupdate=NOW(),
     document_userupdate=$uid,
     document_size=$size,
     document_name='".$obm_q->escape($name)."',
     document_title='".$obm_q->escape($title)."'
   WHERE document_id $id ";
  
  display_debug_msg($query, $cdg_sql, 'run_query_document_update()');
  $retour = $obm_q->query($query);

  // Delete old file
  if (!run_query_global_remove_document($id)) {
    $err_msg = $l_delete_file_error;
  }

  // Insert new file
  if (!run_query_global_create_document($params)) {
    $err_msg = $l_create_file_error;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Get and register the user's todo into session
// This must be called in an open session (between page_open and page_close )
///////////////////////////////////////////////////////////////////////////////
function global_session_load_user_todos($pri='todo_priority') {
  global $cdg_sql, $cgp_todo_nb, $todos, $obm;

  $uid = sql_parse_id($obm['uid'], true);

  $limit = sql_limit($db_type, $cgp_todo_nb, 0);
  $order = ($pri != '' ? $pri : 'todo_priority');
  if ($order == 'todo_priority') {
    $order = ' event_priority desc';
  } elseif($order == 'todo_deadline') {
    $order = ' event_deadline desc';
  }

  // create the todo list
  $query = "SELECT
      event_id,
      event_title,
      event_priority,
      #ADDSECONDS(event_date, event_duration) as event_deadline
    FROM EventLink
      INNER JOIN Event on eventlink_event_id=event_id
      INNER JOIN UserEntity on eventlink_entity_id=userentity_entity_id
    WHERE event_type='VTODO'
      AND userentity_user_id $uid
      AND eventlink_percent < 100
    ORDER BY $order
    $limit";

  display_debug_msg($query, $cdg_sql, 'global_session_load_user_todos()');
  $todolist_q = new DB_OBM;
  $todolist_q->xquery($query);

  $nb_todo = $todolist_q->num_rows();
  $max = min ($cgp_todo_nb, $nb_todo);

  $todos = array();

  for ($rank = 1; $rank <= $max; $rank++) {
    $todolist_q->next_record();

    $option = "todo_$rank";
    $t_id = $todolist_q->f('event_id');
    $t_title = htmlspecialchars($todolist_q->f('event_title'));

    $todos[$rank]['id'] = $t_id; 
    $todos[$rank]['title'] = $t_title; 
  }

  $_SESSION['todos'] = $todos;
}


///////////////////////////////////////////////////////////////////////////////
// Get the company name from the company Id
// Parameters:
//   - $id : company id 
///////////////////////////////////////////////////////////////////////////////
function get_last_company_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT company_name FROM Company WHERE company_id $id ";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_last_company_text()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('company_name');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contact name from the contact Id
// Parameters:
//   - $id : contact id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contact_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT contact_lastname FROM Contact WHERE contact_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_contact_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('contact_lastname');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the lead label from the lead Id
// Parameters:
//   - $id : lead id 
///////////////////////////////////////////////////////////////////////////////
function get_last_lead_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT lead_name FROM Lead WHERE lead_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_lead_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('lead_name');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal label from the deal Id
// Parameters:
//   - $id : deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_deal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT deal_label FROM Deal WHERE deal_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_deal_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('deal_label');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Parent deal label from the parent deal Id
// Parameters:
//   - $id : parent deal id 
///////////////////////////////////////////////////////////////////////////////
function get_last_parentdeal_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT parentdeal_label
    FROM ParentDeal
    WHERE parentdeal_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_parentdeal_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('parentdeal_label');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list name from the list Id
// Parameters:
//   - $id : list id 
///////////////////////////////////////////////////////////////////////////////
function get_last_list_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT list_name FROM List WHERE list_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_list_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('list_name');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the publication name from the publication Id
// Parameters:
//   - $id : publication id 
///////////////////////////////////////////////////////////////////////////////
function get_last_publication_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT publication_title
      FROM Publication
      WHERE publication_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_publication_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('publication_title');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the deal project from the project Id
// Parameters:
//   - $id : project id 
///////////////////////////////////////////////////////////////////////////////
function get_last_project_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT project_name FROM Project WHERE project_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_project_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('project_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the contract label from the contract Id
// Parameters:
//   - $id : contract id 
///////////////////////////////////////////////////////////////////////////////
function get_last_contract_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT contract_label FROM Contract WHERE contract_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_contract_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('contract_label');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the incident label from the incident Id
// Parameters:
//   - $id : incident id 
///////////////////////////////////////////////////////////////////////////////
function get_last_incident_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT incident_label FROM Incident WHERE incident_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_incident_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('incident_label');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Document name from the document Id
// Parameters:
//   - $id : document id 
///////////////////////////////////////////////////////////////////////////////
function get_last_document_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT document_name FROM Document WHERE document_id $id ";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_last_document_text()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('document_name');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the account label from the account Id
// Parameters:
//   - $id : account id
///////////////////////////////////////////////////////////////////////////////
function get_last_account_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT account_label FROM Account WHERE account_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_account_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('account_label');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the invoice label from the invoice Id
// Parameters:
//   - $id : invoice id 
///////////////////////////////////////////////////////////////////////////////
function get_last_invoice_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT invoice_label FROM Invoice WHERE invoice_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_invoice_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('invoice_label');
  }
  return;
}

///////////////////////////////////////////////////////////////////////////////
// Get the resource label from the resource Id
// Parameters:
//   - $id : resource id
///////////////////////////////////////////////////////////////////////////////
function get_last_resource_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT resource_name FROM Resource WHERE resource_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_resource_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('resource_name');
  }
  return;
}

///////////////////////////////////////////////////////////////////////////////
// Get the resourcegroup label from the resourcegroup Id
// Parameters:
//   - $id : resourcegroup id
///////////////////////////////////////////////////////////////////////////////
function get_last_resourcegroup_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT rgroup_name FROM RGroup WHERE rgroup_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_resourcegroup_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('rgroup_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the mailshare name from the mailshare Id
// Parameters:
//   - $id : mailshare id
///////////////////////////////////////////////////////////////////////////////
function get_last_mailshare_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $id = sql_parse_id($id, true);
    $query = "SELECT mailshare_name FROM MailShare WHERE mailshare_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_mailshare_text()');
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('mailshare_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the payment label from the payment Id
// Parameters:
//   - $id : payment id 
///////////////////////////////////////////////////////////////////////////////
function get_last_payment_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $date = sql_date_format($db_type, 'payment_date', 'date');
    $id = sql_parse_id($id, true);
    $query = "SELECT $date, payment_amount FROM Payment WHERE payment_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_payment_text()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('payment_amount').' ('.of_date_format($obm_q->f('date')).')';
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the Domain label from the domain Id
// Parameters:
//   - $id : domain id 
///////////////////////////////////////////////////////////////////////////////
function get_last_domain_text($id) {
  global $cdg_sql;
  
  if ($id > 0) {
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $id = sql_parse_id($id, true);
    $query = "SELECT domain_name FROM Domain WHERE domain_id $id ";
    display_debug_msg($query, $cdg_sql, 'get_last_domain_text()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('domain_name');
  }
  return;
}


///////////////////////////////////////////////////////////////////////////////
// Get the campaign name from the campaign Id
// Parameters:
//   - $id : campaign id 
///////////////////////////////////////////////////////////////////////////////
function get_last_campaign_text($id) {
  global $cdg_sql;

  if ($id > 0) {
    $query = "SELECT campaign_name FROM Campaign WHERE campaign_id='$id'";
    $obm_q = new DB_OBM;
    display_debug_msg($query, $cdg_sql, 'get_last_campaign_text()');
    $obm_q->query($query);
    $obm_q->next_record();
    return $obm_q->f('campaign_name');
  }

  return;
}


///////////////////////////////////////////////////////////////////////////////
// Return an array of all used emails (users, groups)
// Parameters:
//   - $id  : User id to exclude from the result
//   - $gid : Group id to exclude from the result
//   - $lid : List id to exclude from the result
// Returns:
//   - $mails : array of used emails
///////////////////////////////////////////////////////////////////////////////
function get_global_email_used($id='', $gid='', $lid='') {
  global $cdg_sql, $php_regexp_email_name;

  $mails = array();
  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "WHERE userobm_id $id ";
  }
  $query = "SELECT userobm_lastname, userobm_firstname, userobm_email
    FROM UserObm
    $where_id";

  display_debug_msg($query, $cdg_sql, 'get_global_email_used()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $email = $obm_q->f('userobm_email');
    $name = 'Utilisateur : ' . $obm_q->f('userobm_lastname') .' '. $obm_q->f('userobm_firstname');
    $em = strtok($email, "\r\n");
    while ($em) {
      $mails["$em"] = "$name";
      $em = strtok("\r\n");
    }
  }

  if ($gid != '') {
    $gid = sql_parse_id($gid, true, true);
    $where_gid = "WHERE group_id $gid ";
  }
  $query = "SELECT group_name, group_email
    FROM UGroup
    $where_gid";

  display_debug_msg($query, $cdg_sql, 'get_global_email_used(2)');
  $g_q = new DB_OBM;
  $g_q->query($query);

  while ($g_q->next_record()) {
    $email = $g_q->f('group_email');
    $name = 'Groupe : ' . $g_q->f('group_name');
    $mails["$email"] = "$name";
  }

  if ($lid != '') {
    $lid = sql_parse_id($lid, true, true);
    $where_lid = "WHERE list_id $lid ";
  }
  $query = "SELECT list_name, list_email
    FROM List
    $where_lid";

  display_debug_msg($query, $cdg_sql, 'get_global_email_used(3)');
  $l_q = new DB_OBM;
  $l_q->query($query);

  while ($l_q->next_record()) {
    $email = $l_q->f('list_email');
    $name = 'List : ' . $l_q->f('list_name');
    $mails["$email"] = "$name";
  }

  return $mails;
}


///////////////////////////////////////////////////////////////////////////////
// Return the SQL advanced search clause on Company name
// Parameters:
//   - $name        : company name to search
//   - $like        : like clause (can be ilike for postgres)
//   - $table_alias : Table where to find Company (case of multiple joins)
// Returns:
//   - $sql : sql criteria
///////////////////////////////////////////////////////////////////////////////
function sql_global_company_name_advanced_search($name, $like='like', $table_alias='') {

  $sound = phonetic_key($name);

  if ($table_alias != '') {
    $alias = "$table_alias.";
  } else {
    $alias = '';
  }

  $sql = " AND (${alias}company_name $like '$name%'
                 OR ${alias}company_aka $like '%$name%'
                 OR ${alias}company_sound = '$sound')";

  return $sql;
}


//FIXME
function randomColor($min = 100, $max = 200, $diff = 30) {
  $color1 = mt_rand($min, $max);

  do {
    $color2 = mt_rand($min, $max);
  } while(abs($color2-$color1) < $diff);

  do {
    $color3 = mt_rand($min, $max);
  } while(abs($color3-$color1) < $diff);

  return "rgb($color1, $color2, $color3)";
}


///////////////////////////////////////////////////////////////////////////////
// Freeze an unser
// Parameters:
//   - $params[]  : Entry's values
///////////////////////////////////////////////////////////////////////////////
function global_freeze_user($params) {
  global $cdg_sql, $c_nb_login_failed, $c_frozen_duration, $c_login_interval;
  global $c_default_domain;

  $login = $params['login'];
  $domain = $params['domain_id'];
  if(!$c_singleNameSpace) {
    if(strpos($login,"@") !== false) {
      list($login, $domain) = split("@",$login);
      $domain_query = "domain_name";
    } else {
      $domain_query = "userobm_domain_id";
    }

    // if no domain set, set the default domain
    if ((! isset($domain)) || ($domain == "")) {
      $domain = "$c_default_domain";
    }
    $where_domain = "AND $domain_query = '$domain'";
  }

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $lastaccess = sql_date_format($db_type, 'userobm_timelastaccess', 'timelastaccess');

  // user exists?
  $query = "SELECT userobm_id,
    $lastaccess,
    userobm_nb_login_failed
    FROM UserObm
    INNER JOIN Domain ON userobm_domain_id = domain_id
    WHERE userobm_login='$login'
    $where_domain
    AND userobm_archive=0";
  $obm_q->query($query);
  $obm_q->next_record();
  $uid = $obm_q->f('userobm_id');
  $timelastaccess = $obm_q->f('timelastaccess');
  $nb_login_failed = $obm_q->f('userobm_nb_login_failed');
  $now = mktime();
  if (!empty($uid)) {
    // user is freeze and frozen time is outdated
    $uid = sql_parse_id($uid, true);
    if ( ($nb_login_failed == $c_nb_login_failed && $now > $timelastaccess + $c_frozen_duration) ||
         ($nb_login_failed < $c_nb_login_failed && $now > $timelastaccess + $c_login_interval) ) {
      $query = "UPDATE UserObm SET
        userobm_nb_login_failed=1,
        userobm_timelastaccess=NOW()
      WHERE userobm_id $uid ";
    } else if ($nb_login_failed < $c_nb_login_failed) {
      // increase nb_login_failed 
      $query = "UPDATE UserObm SET
        userobm_nb_login_failed=userobm_nb_login_failed+1,
        userobm_timelastaccess=NOW()
      WHERE userobm_id $uid ";
    } else {
      $query = "UPDATE UserObm SET
        userobm_timelastaccess=NOW()
      WHERE userobm_id $uid ";
    } 
  }
  $obm_q->query($query);

  return;
} 


///////////////////////////////////////////////////////////////////////////////
// Unfreeze an user
// Parameters:
//   - $uid : User's id
///////////////////////////////////////////////////////////////////////////////
function global_unfreeze_user($uid) {
  global $cdg_sql, $c_nb_login_failed, $c_frozen_duration;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $lastaccess = sql_date_format($db_type, 'userobm_timelastaccess', 'timelastaccess');

  if ($uid > 0) {
    $uid = sql_parse_id($uid, true);
    $query = "SELECT userobm_id,
      $lastaccess,
      userobm_nb_login_failed
    FROM UserObm
    WHERE userobm_id $uid ";
    $obm_q->query($query);
    $obm_q->next_record();
    $uid = sql_parse_id($obm_q->f('userobm_id'), true);
    $timelastaccess = $obm_q->f('timelastaccess');
    $nb_login_failed = $obm_q->f('userobm_nb_login_failed');

    $now = mktime();
    if (($nb_login_failed < $c_nb_login_failed) || 
        ($nb_login_failed == $c_nb_login_failed && $now > $timelastaccess + $c_frozen_duration)) {
      $query = "UPDATE UserObm SET
        userobm_nb_login_failed='0',
        userobm_timelastaccess=NOW()
      WHERE userobm_id $uid ";
      $obm_q->query($query);
      return true;
    } else {
      return false;
    }
  } else {
    return true;
  }

}

?>
