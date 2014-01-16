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
// OBM - File : list_query.inc                                               //
//     - Desc : list query File                                              //
// 2000-06-07 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// List Search query execution 
// Parameters :
//   - $list[]   : list search criteria
//     keys used : name, contact
///////////////////////////////////////////////////////////////////////////////
function run_query_list_search($list) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  $obm_q = new DB_OBM;
  
  $sql_order_dir = $list['sql_order_dir'];
  $sql_order_field = $list['sql_order_field'];
  
  $name = sql_search_text_parse($obm_q->escape($list['name']));
  $email = sql_search_text_parse($obm_q->escape($list['email']));
  $contact = sql_search_text_parse($obm_q->escape($list['contact']));
  $market = $list['marketing_manager'];
  $contact_id = $list['contact_id'];

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type, 'list_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'list_timecreate', 'timecreate');
  $multidomain = sql_multidomain('list');
	
  $where .= sql_obm_entity_privacy('list');

  if ($name != '') {
    $where .= " AND list_name $like '$name%'";
  }
  if ($contact != '') {
    $where .= " AND contact_lastname $like '$contact%'";
    $join_contact = "LEFT JOIN ContactList ON list_id=contactlist_list_id
    LEFT JOIN Contact ON contactlist_contact_id=contact_id";
  }
  if ($contact_id > 0) {
    $contact_id = sql_parse_id($contact_id, true);
    $where .= " AND contact_id $contact_id ";
    $join_contact = "LEFT JOIN ContactList ON list_id=contactlist_list_id
    LEFT JOIN Contact ON contactlist_contact_id=contact_id";
  }
  if ($email != '') {
    $where .= " AND list_email $like '%$email%'";
  }
  if (($market != $c_all) && ($market != '')) { 
    $market = sql_parse_id($market, true);
    $where .= " AND (list_usercreate $market )";
  }

  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'list_name';

  $orderq .= " ORDER BY $order $sql_order_dir $order_next";

  $query = "SELECT DISTINCT List.*, list_id as id,
    $timecreate,
    $timeupdate,
    A.userobm_login as usercreate,
    B.userobm_login as userupdate
  FROM List
    LEFT JOIN UserObm as A ON List.list_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON List.list_userupdate=B.userobm_id
    $join_contact
  $whereq
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM List $join_contact $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'list_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// List detail query execution
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $multidomain = sql_multidomain('list');
  $timeupdate = sql_date_format($db_type, 'list_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'list_timecreate', 'timecreate');
  $id = sql_parse_id($id, true);
  $query = "SELECT *,
      list_privacy as privacy,
      $timecreate,
      $timeupdate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
  FROM List
       LEFT JOIN UserObm as c ON list_usercreate=c.userobm_id
       LEFT JOIN UserObm as u ON list_userupdate=u.userobm_id
  WHERE list_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_list_detail()');
  $obm_q->query($query) ;
  $obm_q->next_record();
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Stripslashes of all elements of an array (walk_array_recursive() only PHP5)
// Parameters:
//   - $item : array or value
//   - $key  : element key
///////////////////////////////////////////////////////////////////////////////
function list_strip_slashes_array(&$item, $key) {

  if (is_array($item)) {
    array_walk($item, 'list_strip_slashes_array');
  } else if ($item != '') {
    $item = stripslashes($item);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Addslashes of all elements of an array (walk_array_recursive() only PHP5)
// Parameters:
//   - $item : array or value
//   - $key  : element key
///////////////////////////////////////////////////////////////////////////////
function list_add_slashes_array(&$item, $key) {

  if (is_array($item)) {
    array_walk($item, 'list_add_slashes_array');
  } else if ($item != '') {
    $item = addslashes($item);
  }
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : list insertion
// Parameters:
//   - $list[] : list hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_list_insert($list) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $priv = sql_parse_int($list['privacy']);
  $name = $list['name'];
  $subject = $list['subject'];
  $email = $list['email'];
  $mode = $list['mode'];
  $list_query = addslashes($list['query']);
  $mailing_ok = sql_parse_int($list['mailing_ok']);
  $contact_arch = sql_parse_int($list['contact_archive']);
  $info_pub = sql_parse_int($list['info_pub']);
  if (isset($list['criteria'])) {
    $ss_criteria = $list['criteria'];
    if (is_array($ss_criteria)) {
      array_walk($ss_criteria, 'list_strip_slashes_array');
    }
    $list_critery = addslashes(serialize($ss_criteria));
  }
  $domain_id = $obm['domain_id'];
	
  $query = "INSERT INTO List (
    list_domain_id,
    list_timeupdate,
    list_timecreate,
    list_userupdate,
    list_usercreate,
    list_privacy,
    list_name,
    list_subject,
    list_email,
    list_mailing_ok,
    list_mode,
    list_contact_archive,
    list_info_publication,
    list_query,
    list_structure)
  VALUES (
    $domain_id,
    null,
    '" . date('Y-m-d H:i:s') ."',
    null,
    " . $obm['uid'] . ",
    '".$obm_q->escape($priv)."',
    '".$obm_q->escape($name)."',
    '".$obm_q->escape($subject)."',
    '".$obm_q->escape($email)."',
    $mailing_ok,
    '".$obm_q->escape($mode)."',
    $contact_arch,
    $info_pub,
    '".$obm_q->escape($list_query)."',
    '".$obm_q->escape($list_critery)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_list_insert()');
  $retour = $obm_q->query($query);
  $id = $obm_q->lastid();
  // Handle list duplication
  if ($retour) {
    $entity_id = of_entity_insert($GLOBALS['module'], $id);  
    $sql_id = sql_parse_id($id);
    $id_dup = $list['id_duplicated'];
    if ($id_dup > 0) {
      $id_dup = sql_parse_id($id_dup, true);
      $query = "SELECT * FROM ContactList WHERE contactlist_list_id $id_dup ";
      $retour = $obm_q->query($query);
      while ($obm_q->next_record()) {
	$c_id = sql_parse_id($obm_q->f('contactlist_contact_id'));
	$query_i = "INSERT INTO ContactList
          (contactlist_list_id, contactlist_contact_id)
        VALUES
          ($id, $c_id)";
	display_debug_msg($query_i, $cdg_sql, 'run_query_list_insert(dup)');
	$obm_qi = new DB_OBM;
	$retour = $obm_qi->query($query_i);
      }
    }
    run_query_list_update_static_nb($id);
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Get the list id from the list Infos
// Parameters:
//   - $list : list infos
///////////////////////////////////////////////////////////////////////////////
function get_list_id($list) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  $name = $list['name'];
  $subject = $list['subject'];
  $email = $list['email'];
  $list_query = addslashes($list['query']);
  $multidomain = sql_multidomain('list');
	
  $query = "SELECT list_id
    FROM List
    WHERE list_name='".$obm_q->escape($name)."'
      AND list_subject='".$obm_q->escape($subject)."'
      AND list_email='".$obm_q->escape($email)."'
      AND list_query='".$obm_q->escape($list_query)."'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_list_id()');
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res = $obm_q->f('list_id');

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// List Update query execution
// Parameters:
//   - $list[] : list hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update($list) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $id = sql_parse_id($list['list_id'], true);
  $priv = sql_parse_int($list['privacy']);
  $name = $list['name'];
  $subject = $list['subject'];
  $email = $list['email'];
  $mode = $list['mode'];
  $list_query = $list['query'];
  $mailing_ok = sql_parse_int($list['mailing_ok']);
  $contact_arch = sql_parse_int($list['contact_archive']);
  $info_pub = sql_parse_int($list['info_pub']);
  if (isset($list['criteria'])) {
    $ss_criteria = $list['criteria'];
    if (is_array($ss_criteria)) {
      array_walk($ss_criteria, 'list_strip_slashes_array');
    }
    $list_critery = serialize($ss_criteria);
  }
  $multidomain = sql_multidomain('list');
  
  $query = "UPDATE List SET
    list_timeupdate='". date('Y-m-d H:i:s')."',
    list_userupdate=".$obm['uid'].",
    list_privacy=$priv,
    list_name='".$obm_q->escape($name)."',
    list_subject='".$obm_q->escape($subject)."',
    list_email='".$obm_q->escape($email)."',
    list_mailing_ok=$mailing_ok,
    list_contact_archive=$contact_arch,
    list_mode='".$obm_q->escape($mode)."',
    list_info_publication=$info_pub,
    list_query='".$obm_q->escape($list_query)."',
    list_structure='".$obm_q->escape($list_critery)."'
  WHERE list_id $id 
     $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_list_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update the SQL for a List
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update_sql($id) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $multidomain = sql_multidomain('list');

  // Check that list has really a query (if not we do nothing)
  $id = sql_parse_id($id, true);
  $query = "SELECT list_structure FROM List WHERE list_id $id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_list_update_sql(1)');
  $list_q = new DB_OBM;
  $retour = $list_q->query($query);
  $list_q->next_record();

  $dynlist = make_list_query_from_db($list_q);
  $list_query = $dynlist['query'];

  if ($list_query != '') {

    $query = "UPDATE List SET
      list_timeupdate='". date('Y-m-d H:i:s')."',
      list_userupdate=".$obm['uid'].",
      list_query='".$obm_q->escape($list_query)."'
    WHERE list_id $id";

    display_debug_msg($query, $cdg_sql, 'run_query_list_update_sql(2)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : list id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_delete($p_id) {
  global $cdg_sql;
  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true);
  // Delete all contacts registration to this list
  $query = "DELETE FROM ContactList WHERE contactlist_list_id $p_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_list_delete(1)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  // Delete the List
  $multidomain = sql_multidomain('list');
  $query = "DELETE FROM List WHERE list_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_list_delete(2)');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : Return the static contacts id list
// Parameters:
//   - $id : list id
// Return: number of contact inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_list_contactlist_array($id) {
  global $cdg_sql;

  $list = array();
  $sql_id = sql_parse_id($id, true);
  $query = "SELECT contactlist_contact_id FROM ContactList WHERE contactlist_list_id $sql_id";
  display_debug_msg($query, $cdg_sql,'run_query_list_contactlist_list()');
  $obm_q = new DB_OBM;
  if ($obm_q->query($query)) {
    while ($obm_q->next_record()) {
      $list[] = $obm_q->f('contactlist_contact_id');
    }
  }
  return $list;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList conditionnal insertion
// Parameters:
//   - $list[] : list hash info : keys used : id, con_nb, static_con (array)
// Return: number of contact inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_list_contactlist_insert($list) {
  global $cdg_sql;

  $id = sql_parse_id($list['list_id'], true);
  $ins_id = sql_parse_id($list['list_id']);
  $multidomain = sql_multidomain('list');

  // We check the list still exist (in case popup windows here for long...)
  $query = "SELECT list_id FROM List WHERE list_id $id $multidomain";
  display_debug_msg($query, $cdg_sql,'run_query_list_contactlist_insert()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() != 1) {
    return -1;
  }

  $cpt_ins = 0;
  if ($list['con_nb'] > 0) {
    $existing = run_query_list_contactlist_array($list['list_id']);
    $to_insert = array_diff($list['static_con'], $existing);
    foreach ($to_insert as $con_id) {
      $sql_id = sql_parse_id($con_id);
      $query = "INSERT INTO ContactList (contactlist_list_id,
        contactlist_contact_id) VALUES ($ins_id, $sql_id)";
      display_debug_msg($query, $cdg_sql,'run_query_list_contactlist_insert(3)');
      $retour = $obm_q->query($query);
    }
    $cpt_ins = count($to_insert);
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : ContactList deletion
// Parameters:
//   - $list[] : list hash info : keys used : id, con_nb, static_con (array)
///////////////////////////////////////////////////////////////////////////////
function run_query_list_contactlist_delete($list) {
  global $cdg_sql;

  $cpt_del = 0;
  if ($list['con_nb'] > 0) {
    $id = sql_parse_id($list['list_id'], true);
    $con_ids = sql_implode_id(',', $list['static_con']);

    $query = "DELETE FROM ContactList
      WHERE contactlist_list_id $id AND contactlist_contact_id IN ($con_ids) ";
    display_debug_msg($query, $cdg_sql, 'run_query_list_contactlist_delete()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del = $obm_q->affected_rows();
    }
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the static contacts of the given list
// Parameters:
//   - $list   : List hash
// -- Optionnals
//   - $entity : dataset entity to order (to handle multiple Display in 1 p)
///////////////////////////////////////////////////////////////////////////////
function run_query_list_contacts_list($list, $entity='') {
  global $cdg_sql, $ctu_sql_limit;

  $id = sql_parse_id($list['list_id'], true);
  $sql_order_field = $list['sql_order_field'];
  $sql_order_dir = $list['sql_order_dir'];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type, '', '', 'sta');
  $if_country = sql_if($db_type, "(ctry1.country_name != '')", 'ctry1.country_name', 'ctry2.country_name');

  // Order exceptions (order on calculated rows)
  // Addresses are taken from contact or from company (if empty for contact)

  if ( (strcmp($sql_order_field,'address')==0) ) {
    $order = 'address_street';
    $order_next = ', address_zipcode, address_town';
  } else if (strcmp($sql_order_field,'zipcode')==0) {
    $order = 'address_zipcode';
  } else if (strcmp($sql_order_field,'town')==0) {
    $order = 'address_town';
  } else if (trim($sql_order_field) == '') {
    $order = 'contact_lastname';
  } else {
    $order = $sql_order_field;
  }

  if ($entity != 'list_contact') {
    $order = 'contact_lastname';
  }

  $orderq = "ORDER BY $order $sql_order_dir $order_next";

  $query = "SELECT contactlist_contact_id as contact_id,
      contactlist_contact_id as id,
      contact_timeupdate,
      contact_timecreate,
      contact_userupdate,
      contact_usercreate,
      contact_company_id,
      contact_kind_id,
      kind_minilabel,
      kind_header,
      kind_lang,
      contact_lastname,
      contact_firstname,
      contact_service,
      ad1.address_street AS contact_street,
      ad1.address_zipcode AS contact_zipcode,
      ad1.address_town AS contact_town,
      ad1.address_expresspostal AS contact_expresspostal,
      ctry1.country_name AS contact_country,
      ctry1.country_iso3166 AS contact_country_iso3166,
      contact_function_id,
      contactfunction_label,
      contact_title,
      ph1.phone_number as contact_phone,
      email_address,
      contact_comment,
      company_id,
      company_name,
      ad2.address_street AS company_street,
      ad2.address_zipcode AS company_zipcode,
      ad2.address_town AS company_town,
      ad2.address_expresspostal AS company_expresspostal,
      ctry2.country_name AS company_country,
      $if_country AS country_name
    FROM ContactList
      INNER JOIN Contact ON contactlist_contact_id=contact_id
      INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
      LEFT JOIN Address as ad1 ON contactentity_entity_id=ad1.address_entity_id AND ad1.address_label = 'WORK;X-OBM-Ref1'
      LEFT JOIN Country as ctry1 ON ad1.address_country=ctry1.country_iso3166
           AND ctry1.country_lang='$lang'
      LEFT JOIN Phone AS ph1 ON contactentity_entity_id=ph1.phone_entity_id
      LEFT JOIN Email ON contactentity_entity_id=email_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
      LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
      LEFT JOIN CompanyEntity ON companyentity_company_id = company_id
      LEFT JOIN Address as ad2 ON companyentity_entity_id=ad2.address_entity_id AND ad2.address_label = 'HQ;X-OBM-Ref1'
      LEFT JOIN Country as ctry2 ON ad2.address_country=ctry2.country_iso3166
           AND ctry2.country_lang='$lang'
      LEFT JOIN ContactFunction ON Contact.contact_function_id = contactfunction_id
      LEFT JOIN Kind ON Contact.contact_kind_id = kind_id
    WHERE contactlist_list_id $id
      AND (ph1.phone_id IN (SELECT MIN(phone_id) FROM Phone WHERE phone_entity_id = contactentity_entity_id) OR phone_id IS NULL)
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    display_debug_msg($query, $cdg_sql, 'run_query_list_contacts_list()');
    $count = get_query_count("SELECT count(*) FROM ContactList WHERE contactlist_list_id $id ");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_list_contacts_list()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered contact in the list specified
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_static_contact_nb($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT count(*) FROM ContactList WHERE contactlist_list_id $id ";

  display_debug_msg($query, $cdg_sql, 'get_list_static_contact_nb()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);
  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Update the dynamic rows number for the list
// Parameters:
//   - $id : list_id
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update_static_nb($id) {
  global $cdg_sql;

  $nb = get_list_static_contact_nb($id);
  $id = sql_parse_id($id, true);

  $query = "UPDATE List SET list_static_nb='$nb' WHERE list_id $id";
  display_debug_msg($query, $cdg_sql, 'run_query_list_update_static_nb()');
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a list (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : list id to exclude
// Returns:
//   - true (if a list exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_list_name_exists($name, $id='') {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND list_id $id ";
  }
	
  $multidomain = sql_multidomain('list');

  $query = "SELECT list_id, list_name
    FROM List
    WHERE list_name='".$obm_q->escape($name)."'
      $where_id
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_list_name_exists()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the lists which matches the name or the subject
// except the one given (update mode)
// Parameters:
//   - $id      : list id
//   - $name    : list name
//   - $subject : list subject
///////////////////////////////////////////////////////////////////////////////
function run_query_list_check_list($id, $name, $subject) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('list');
	
  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "list_id $id AND";
  }

  // We search for a similar name (as same name is already excluded)
  $wname = "list_name $like '%$name%'";

  // If subject is short, we test equality, else similarity
  if (strlen($subject)  > 2) {
    $wsubject = "list_subject $like '%".$obm_q->escape($subject)."%'";
  } else {
    $wsubject = "list_subject = '".$obm_q->escape($subject)."'";
  }

  $query = "SELECT distinct list_id, list_name, list_subject
     FROM List
     WHERE $where_id
       ($wname or $wsubject)
       $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_list_check_list()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Update the dynamic rows number for the list
// Parameters:
//   - $id : list_id
//   - $nb : new rows number
///////////////////////////////////////////////////////////////////////////////
function run_query_list_update_num_rows($id, $nb) {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  if ($nb == '') {
    $nb = '0';
  }
  $id = sql_parse_id($id, true);
  $query = "UPDATE List SET list_query_nb='".$obm_q->escape($nb)."' WHERE list_id $id";
  display_debug_msg($query, $cdg_sql, 'run_query_list_update_num_rows()');
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// List context checking (same lists exists ?)
// Parameters:
//   - $id       : list id
//   - $list[]   : list values
//     keys used : name, subject, email
// Returns:
//   - List Database object with list of similar lists
///////////////////////////////////////////////////////////////////////////////
function check_list_context($id, $list) {
  global $cdg_sql;

  $name = $list['name'];
  $subject = $list['subject'];

  // return the lists with same name or subject
  $list_q = run_query_list_check_list($id, $name, $subject);

  return $list_q;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the query is coherent (publication info should not be On if
// same Publication field combined with AND)
// Parameters:
//   - $list[]   : list search criteria
//     keys used : $criteria.
///////////////////////////////////////////////////////////////////////////////
function check_list_query_coherence($list) {
  global $err, $l_err_list_coh_pub;;

  $coherence_ok = true;

  $id = $list['list_id'];
  $criteria = $list['criteria']['modules'];
  $logical_criterion = $list['criteria']['logical']['AND'];
  $info_pub = $list['info_pub'];

  // If info pub is set, Check it is ok (no 2 pub titles combines by AND)
  if ($info_pub == 1) {
    $info_pub_allowed = true;

    if (is_array($criteria['publication'])) {
      $pub_criteria = $criteria['publication'];
      foreach($pub_criteria as $field => $criterion) {
	if (is_array($criterion)) {
	  foreach($criterion as $line => $value) {
	    if ($value != '') {
	      if (!isset($join_nb[$field])) {
		$join_nb[$field] = 0;
	      } elseif ($logical_criterion[$line] == 'AND'
		      && $line == $old_line + 1) {
		// AND on same criteria
		$info_pub_allowed = false;
		$join_nb[$field] ++;
		$coherence_ok = false;
		$err['msg'] = $l_err_list_coh_pub;
	      }
	    }
	    $old_line = $line;
	  }
	}
      }
    }
  }

  return $coherence_ok;
}


///////////////////////////////////////////////////////////////////////////////
// List Form Data checking and formatting
// Parameters:
//   - $id       : list id  (empty on insertion)
//   - $list[]   : values checked
//     keys used : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function check_list_data($id, $list) {
  global $php_regexp_email, $l_j_check_email, $l_list_exists, $l_fill_name;
  global $l_banned_word, $l_incoherent_criteria, $l_hungry_query;
  global $cdg_sql, $err, $cql_max_row, $cql_max_cost, $l_invalid_query;

  $name = $list['name'];
  $subject = $list['subject'];
  $email = $list['email'];
  $query = $list['query'];

  // MANDATORY: List name not empty
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    return false;
  }

  // MANDATORY: List name unique
  if (get_list_name_exists($name, $id)) {
    $err['msg'] = "$l_list_exists ($name)";
    return false;
  }

  // List email
  if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
    $err['msg'] = " $email : $l_j_check_email";
    return false;
  }

  // If a query has been given, check if it is correct
  if (trim($query) != '') {
    // Check if banned words are included
    $excluded_words = array ('insert', 'update', 'delete', 'create', 'alter', 'drop', 'lock', 'userobm_password', 'truncate');
    while ( list($key, $value) = each($excluded_words) ) {
      if (preg_match("/\b$value\b/i", $query)) {
	$err['msg'] = "$l_invalid_query $l_banned_word : $value";
	return false;
      }
    }

    // Check that criteria are coherent
    if (! check_list_query_coherence($list)) {
	$err['msg'] = "$l_incoherent_criteria : $err[msg]";
      return false;
    }

    // Check if query is syntaxically correct
    display_debug_msg("explain $query", $cdg_sql, 'check_list_data()');
    $obm_q = new DB_OBM;
    // 1 indicate to PHPLIB not to report the error if any
    $ret = @$obm_q->query("explain $query", 1);
    if (substr($ret, 0,4) == 'obm:') {
      $err['msg'] = "$l_invalid_query : " . substr($ret,4);
      return false;
    } else {
      $ret = sql_list_query_resource_high($query);
      if ($ret['ret']) {
	$rows = $ret['rows'];
	$cost = $ret['cost'];
	$err['msg'] = "$l_hungry_query (rows=$rows [max=$cql_max_row] cost=$cost [max=$cql_max_cost])";
	return false;
      }
    }
  }
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the list can be deleted
// Parameters:
//   - $p_id : list id
// Returns:
//   true if the list can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_list_can_delete($p_id) {
  global $ok_msg;
  global $l_has_contact;

  $delete_ok = true;

  $nb_con = get_list_static_contact_nb($p_id);
  $ok_msg .= "$nb_con $l_has_contact";

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Get the infos of a List from its Id
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function get_list_info($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('list');
  $id = sql_parse_id($id, true);
  $query = "SELECT list_id,
      list_name,
      list_mode,
      list_structure,
      list_mailing_ok,
      list_contact_archive,
      list_info_publication
    FROM List
    WHERE list_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_list_info()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $res['list_id'] = $obm_q->f('list_id');
  $res['name'] = $obm_q->f('list_name');
  $res['mode'] = $obm_q->f('list_mode');
  $res['structure'] = $obm_q->f('list_structure');
  $res['mailing_ok'] = $obm_q->f('list_mailing_ok');
  $res['contact_archive'] = $obm_q->f('list_contact_archive');
  $res['info_pub'] = $obm_q->f('list_info_publication');

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get the rows number for the query given
// Parameters:
//   - $dynquery : dynamic query infos [query], [from], [where]
//   - $list_q   : dbo with list info
///////////////////////////////////////////////////////////////////////////////
function get_list_query_num_rows($dynquery='', $list_q) {
  global $cdg_sql;

  // If no extra info needed (as publication) take unique contact id
  $info_pub = ($list_q->f('list_info_publication') == 0 ? 0 : 1);
  // If publication infos asked
  if ($info_pub) {
    // if no subscription or publication criteria, count contacts
    if (preg_match('/subscription/', $dynquery['where']) == 0) {
      $distinct_id = ' contact_id';
    } else {
      $distinct_id = 'distinct subscription_id';
    }
  } else {
    $distinct_id = 'distinct contact_id';
  }

  // Case expert query, we try to find the correct num_rows
  if (! isset($dynquery['from'])) {
    $query = $list_q->f('list_query');
    // Distinguinsh simple queries to queries with sub-query for optimization
    $pos = strpos(strtolower($query), 'foo', 1);

    if ($pos > 1) {
      // The query has a sub-query
      // XXXX potential pb : other sub-queries (AND contact_id NOT IN (SELECT..)
      if (preg_match('/FROM .* (FROM (.*))(\) AS FOO|GROUP BY|ORDER BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_c = "SELECT count($distinct_id) as nb " . $matches[1];
      } else {
	return 0;
      }

    } else {
      // The query is a standard query (no sub-qurey)
      // Get the From ... Where clauses
      // IF 'Group by' present, take between From and group by (ungreedy)
      // else get between From and the end
      if (preg_match('/(FROM (.*))(ORDER BY|GROUP BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_c = "SELECT count($distinct_id) as nb " . $matches[1];
      } else {
	return 0;
      }
    }

    // Normal mode we optimize the query
  } else {
    $from = $dynquery['from'];
    $where = $dynquery['where'];
    $query_c = "SELECT count($distinct_id) as nb FROM $from WHERE $where";
  }

  display_debug_msg($query_c, $cdg_sql, 'get_list_query_num_rows()');
  $obm_q = new DB_OBM;
  $r = $obm_q->query($query_c, 1);
  if (is_string($r)) {
    $ret = -1;
  } else {
    $obm_q->next_record();
    $ret = $obm_q->f('nb');
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get the distinct contacts number for the query given
// Parameters:
//   - $dynquery : dynamic query infos [query], [from], [where]
///////////////////////////////////////////////////////////////////////////////
function get_list_dynamic_contact_nb($dynquery='') {
  global $cdg_sql;

  // Case expert query, we try to find the correct num_rows
  if (! isset($dynquery['from'])) {
    $query = $dynquery['query'];

    // distinguish simple queries to queries with sub-query for optimization
    $pos = strpos(strtolower($query), 'foo', 1);
 
    if ($pos > 1) {
      // The query is has a sub-query
      if (preg_match('/FROM .* (FROM (.*))(\) AS FOO|GROUP BY|ORDER BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_c = "SELECT count(distinct contact_id) as nb " . $matches[1];
      } else {
	return 0;
      }

    } else {
      // The query is a standard query (no sub-qurey)
      if (preg_match('/(FROM (.*))(GROUP BY|ORDER BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_c = "SELECT count(distinct contact_id) as nb " . $matches[1];
      } else {
	return 0;
      }
    }

    // Normal mode we optimize the query
  } else {
    $from = $dynquery['from'];
    $where = $dynquery['where'];
    $query_c = "SELECT count(distinct contact_id) as nb FROM $from WHERE $where";
  }

  display_debug_msg($query_c, $cdg_sql, 'get_list_dynamic_contact_nb()');
  $obm_q = new DB_OBM;
  $r = $obm_q->query($query_c, 1);
  if (is_string($r)) {
    $ret = -1;
  } else {
    $obm_q->next_record();
    $ret = $obm_q->f('nb');
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the number of company from a dynamic query
// Parameters:
//   - $dynquery : dynamic query infos [query], [from], [where]
// Returns:
//   - number of companies
///////////////////////////////////////////////////////////////////////////////
function get_list_dynamic_company_nb($dynquery) {
  global $cdg_sql;

  // Case expert query, we try to find the correct num_rows
  if (! isset($dynquery['from'])) {
    $query = $dynquery['query'];

    // distinguish simple queries to queries with sub-query for optimization
    $pos = strpos(strtolower($query), 'foo', 1);

    if ($pos > 1) {
      // The query has a sub-query
      if (preg_match('/FROM .* (FROM (.*))(\) AS FOO|GROUP BY|ORDER BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_c = "SELECT count(distinct(company_id)) as nb ". $matches[1];
      } else {
	return 0;
      }

    } else {
      // The query is a standard query (no sub-query)
      if (preg_match('/(FROM (.*))\s*(ORDER BY|GROUP BY)?(?(3)|$)/Uis', $query, $matches)) { 
	$query_c = "SELECT count(distinct(company_id)) as nb ". $matches[1];
      } else {
	return 0;
      }
    }

    // Normal mode we optimize the query
  } else {
    $from = $dynquery['from'];
    $where = $dynquery['where'];
    $query_c = "SELECT count(distinct company_id) as nb FROM $from WHERE $where";
  }

  display_debug_msg($query_c, $cdg_sql, 'get_list_dynamic_company_nb()');
  $obm_q = new DB_OBM;

  $obm_q->query($query_c);
  $obm_q->next_record();
  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : get the number of company from the static contact
// of a list
// Parameters:
//   - $id : Id of the list
// Returns:
//   - amount of companies
///////////////////////////////////////////////////////////////////////////////
function get_list_static_company_nb($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query_c = "SELECT count(distinct(company_id)) as nb
    FROM ContactList
      LEFT JOIN Contact ON contactlist_contact_id=contact_id
      LEFT JOIN Company ON Contact.contact_company_id = Company.company_id
    WHERE
      contactlist_list_id $id ";

  display_debug_msg($query_c, $cdg_sql, 'get_list_static_company_nb()');
  $obm_q = new DB_OBM;
  $obm_q->query($query_c);
  $obm_q->next_record();
  return $obm_q->f('nb');
}


///////////////////////////////////////////////////////////////////////////////
// Evaluate if a query can be accepted based on resource consumption
// Evaluation done specificaly for each database backend supported
// Parameters:
//   - $db_type : DB type
//   - $query   : query to evaluate
// Returns:
//   - true if resource consumption is high for this query, else false
///////////////////////////////////////////////////////////////////////////////
function sql_list_query_resource_high($query) {
  global $cdg_sql, $db_type_mysql, $db_type_pgsql;
  global $cql_max_row, $cql_max_cost;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  if ($db_type == $db_type_mysql) {
    $q = "explain $query";
    display_debug_msg($q, $cdg_sql, 'sql_list_query_resource_high()');
    $obm_q->query($q);
    $cpt = 1;
    while ($obm_q->next_record()) {
      $num = $obm_q->f('rows');
      $cpt *= $num;
    }
    $ret['rows'] = $cpt;
    if ($cpt > $cql_max_row) {
      $ret['ret'] = true;
      return $ret;
    } else {
      $ret['ret'] = false;
      return $ret;
    }

    // Postgres Estimate
  } elseif ($db_type == $db_type_pgsql) {
    $q = "explain $query";
    display_debug_msg($q, $cdg_sql, 'sql_list_query_resource_high()');
    $obm_q->query($q, 1);
    // We read the first row of explain command
    $obm_q->next_record();
    $line = $obm_q->f(0);
    $pattern = "/cost=[0-9]*\.[0-9]*\.\.([0-9]*\.[0-9]*) rows=([0-9]*) /";
    if (preg_match($pattern, $line, $match)) {
      $cost = $match[1];
      $rows = $match[2];
      $ret['cost'] = $cost;
      $ret['rows'] = $rows;
    }

    if (($cost > $cql_max_cost) || ($rows > $cql_max_row)) {
      $ret['ret'] = true;
      return $ret;
    } else {
      $ret['ret'] = false;
      return $ret;
    }

    // Other DB backends
  } else {
    $ret['ret'] = false;
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Contact: Kind lang select query 
// Returns:
//   DB object result with all kinds
///////////////////////////////////////////////////////////////////////////////
function run_query_list_language() {
  global $cdg_sql;

  $multidomain = sql_multidomain('kind');

  $query = "SELECT kind_lang FROM Kind
    WHERE 1=1
      $multidomain
    GROUP BY kind_lang";

  display_debug_msg($query, $cdg_sql, 'run_query_list_language()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Publication: publication select query 
// Returns:
//   DB object result with all publication
///////////////////////////////////////////////////////////////////////////////
function run_query_list_publication() {
  global $cdg_sql;

  $multidomain = sql_multidomain('publication');

  $query = "SELECT publication_title, publication_id
    FROM Publication
    WHERE 1=1
      $multidomain
    ORDER by publication_year DESC, publication_type_id, publication_title";

  display_debug_msg($query, $cdg_sql, 'run_query_list_publication()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode subscription query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_list_subscriptionreception() {
  global $cdg_sql;

  $query = "SELECT *
    FROM SubscriptionReception
    ORDER BY subscriptionreception_label DESC"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_list_subscriptionreception()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Generate the SQL Query from a List given as a DBO
// Parameters:
//   - $list_q : DBO with list info
// Returns:
// $ret array [query] full query, [where] where clause, [from] from clause
///////////////////////////////////////////////////////////////////////////////
function make_list_query_from_db(&$list_q) {
  global $cdg_sql;

  if (is_object($list_q)) {
    $id = $list_q->f('list_id');
    $structure = $list_q->f('list_structure');
    $mail_ok = $list_q->f('list_mailing_ok');
    $contact_arch = $list_q->f('list_contact_archive');
    $privacy = $list_q->f('privacy');
    $info_pub = $list_q->f('list_info_publication');
    
    if ($structure != '') {
      $criteria = unserialize($structure);
    } else {
      $criteria = '';
    }

    if ($criteria != '') {
      // Addslashes to all criteria elements
      $as_criteria = $criteria;
      if (is_array($as_criteria)) {
	array_walk($as_criteria, 'list_add_slashes_array');
      }
      $list_info['criteria'] = $as_criteria;
      $list_info['list_id'] = $id;
      $list_info['mailing_ok'] = $mail_ok;
      $list_info['contact_archive'] = $contact_arch;
      $list_info['info_pub'] = $info_pub;
      $list_info['privacy'] = $privacy;
      $dynlist = make_list_query_from_criteria($list_info);
    }
  }

  return $dynlist;
}


///////////////////////////////////////////////////////////////////////////////
// Prepare the list criteria to be handled automatically by the query generator
// Some criteria are interdependant and are translated to other criteria
// eg : usercategory : if tree set -> category_code, else category_id
// Parameters
//   - $criteria : global criteria array
// Returns:
//   new criteria array with final fields
///////////////////////////////////////////////////////////////////////////////
function prepare_list_criteria(&$criteria) {
  global $cdg_sql, $cgp_user;
  
  $target_fields = array();

  // User data categories handling
  // We stamp each category
  if (is_array($cgp_user)) {
    foreach ($cgp_user as $entity => $entity_info) {
      if (is_array($cgp_user[$entity]['category'])) {
	foreach($cgp_user[$entity]['category'] as $cat_name => $one_cat) {
	  $target_fields[$cat_name] = 'category';
	}
      }
    }
  }

  if (is_array($criteria)) {
    foreach($criteria as $module => $module_criteria) {
      if (is_array($module_criteria)) {
        foreach($module_criteria as $field => $criterion) {

	  // If field is a user category, not empty
          if (array_key_exists($field, $target_fields)
	      && (is_array($criterion)) ) {
	    $cats_code_hash = of_category_user_get_code_hash($field);

	    // loop through lines for this criterion
            foreach($criterion as $line => $value) {
	      if (($value != '')
		  && ($module_criteria["${field}_tree"][$line] == 'true')) {
		$values = explode(',', $value);
		$new_value = '';
		foreach ($values as $id) {
		  $code = $cats_code_hash[$id];
		  if ($new_value != '') {
		    $new_value .= ",$code";
		  } else {
		    $new_value .= $code;
		  }
		}
		$criteria[$module]["${field}_code"][$line] = $new_value;
		$criteria[$module]["${field}"][$line] = '';
	      }
	    }
	  }
	}
      }
    }
  }

  return $criteria;
}


///////////////////////////////////////////////////////////////////////////////
// Make an SQL Query with the list of criteria
// Parameters:
//   - $list[]   : list search criteria
//     keys used : $criteria.
// Returns:
// $ret array [query] full query, [where] where clause, [from] from clause
///////////////////////////////////////////////////////////////////////////////
function make_list_query_from_criteria($list) {
  global $cdg_sql, $c_private, $cgp_user, $obm;

  $id = $list['list_id'];
  $criteria = $list['criteria']['modules'];
  $criteria = prepare_list_criteria($criteria);
  $boolean_criterion = $list['criteria']['logical']['NOT'];
  $logical_criterion = $list['criteria']['logical']['AND'];
  $mailing_ok = $list['mailing_ok'];
  $contact_arch = $list['contact_archive'];
  $privacy = $list['privacy'];
  $info_pub = $list['info_pub'];
  $lang = get_lang();

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  // %f replaced by field
  // %v replaced by value
  // %j replaced by join number
  // %t replaced by category table alias (before going to query generation)
  $common_like = "%f $like '%v%'";
  $common_equal = "%f = '%v'";
  $subscription_equal = "s%j.%f = '%v'";
  $category_equal = "(cc%j.%f $like '%v.%' OR cc%j.%f = '%v')";
  $usercategory_id_in = "%t.categorylink_category_id IN (%v)";
  $usercategory_code_equal = "(%t.category_code $like '%v.%' OR %t.category_code = '%v')";
  $connection_equal = "%j.%f = '%v'";
  $connection_in = "%j.%f IN (%v)";
  $pub_lang_like = "p%j.%f $like '%%v%'";
  $comp_time_sup = "(company_timecreate >= '%v' OR company_timeupdate >= '%v')";
  $comp_time_inf = "(company_timecreate <= '%v' OR company_timeupdate <= '%v')";
  $cont_time_sup = "(contact_timecreate >= '%v' OR contact_timeupdate >= '%v')";
  $cont_time_inf = "(contact_timecreate <= '%v' OR contact_timeupdate <= '%v')";
  // Contact Address specific case (if not set, test with company one)
  // sad1 for Search Address 1
  $contact_zipcode = "(sad1%j.address_zipcode $like '%v%' OR (sad1%j.address_zipcode = '' AND sad2%j.address_zipcode $like '%v%'))";
  $company_zipcode = "(sad3%j.address_zipcode $like '%v%')";
  $contact_town = "(sad1%j.address_town $like '%v%' OR (sad1%j.address_town = '' AND sad2%j.address_town $like '%v%'))";
  $company_town = "(sad3%j.address_town $like '%v%')";
  $contact_country = "(sad1%j.address_country = '%v' OR (sad1%j.address_country = '' AND sad2%j.address_country = '%v'))";
  $company_country = "(sad3%j.address_country = '%v')";
  $sub_time_aft = "(s%j.subscription_timecreate >= '%v' OR s%j.subscription_timeupdate >= '%v')";
  $sub_time_bef = "(s%j.subscription_timecreate <= '%v' OR s%j.subscription_timeupdate <= '%v')";

  $operateurs = array(
    'company_name' => $common_like,
    'company_country_iso3166' => $company_country,
    'company_timeafter' => $comp_time_sup,
    'company_timebefore'=> $comp_time_inf,
    'company_zipcode' => $company_zipcode,
    'company_marketingmanager_id' => $common_equal,
    'company_town' => $company_town,
    'company_datasource_id' => $common_equal,
    'contact_firstname' => $common_like,
    'contact_country_iso3166' => $contact_country,
    'contact_timeafter' => $cont_time_sup,
    'contact_timebefore' => $cont_time_inf,
    'contact_lastname' => $common_like,
    'contact_marketingmanager_id' => $common_equal,
    'contact_datasource_id' => $common_equal,
    'contact_town' => $contact_town,
    'contact_zipcode' => $contact_zipcode,
    'contact_function_id' => $common_equal,
    'subscription_publication_id' => $subscription_equal,
    'publication_lang' => $pub_lang_like,
    'publication_year' => "p%j.".$common_equal,
    'subscription_renewal' => "s%j.".$common_equal,
    'subscription_reception_id' => "s%j.".$common_equal,
    'subscription_timeafter' => $sub_time_aft,
    'subscription_timebefore'=> $sub_time_bef,
    'kind_lang' => $common_equal
  );

  // With NOT (or exclusion), some operators needs a different writing
  // eg: equals to a field that can be null (cause if null must return true)
  $common_equal_not = "(%f = '%v' AND NOT %f IS NULL)";

  $operateurs_not = $operateurs;
  $operateurs_not['company_datasource_id'] = $common_equal_not;
  $operateurs_not['contact_datasource_id'] = $common_equal_not;
  $operateurs_not['contact_function_id'] = $common_equal_not;

  // hash field criteria -> join associated needed
  $add_join = array (
    'subscription_publication_id' => "
       LEFT JOIN Subscription s%j ON s%j.subscription_contact_id = contact_id
       LEFT JOIN Publication p%j ON s%j.subscription_publication_id = p%j.publication_id");
  $add_join['contact_country_iso3166'] = "
       LEFT JOIN Address AS sad1%j ON contactentity_entity_id=sad1%j.address_entity_id
       LEFT JOIN Address AS sad2%j ON companyentity_entity_id=sad2%j.address_entity_id";
  $add_join['company_country_iso3166'] = "
       LEFT JOIN Address AS sad3%j ON companyentity_entity_id=sad3%j.address_entity_id";

  $field_join_equiv['publication_lang'] = 'subscription_publication_id';
  $field_join_equiv['publication_year'] = 'subscription_publication_id';
  $field_join_equiv['subscription_renewal'] = 'subscription_publication_id';
  $field_join_equiv['subscription_reception_id'] = 'subscription_publication_id';
  $field_join_equiv['subscription_timeafter'] = 'subscription_publication_id';
  $field_join_equiv['subscription_timebefore'] = 'subscription_publication_id';

  $field_join_equiv['contact_zipcode'] = 'contact_country_iso3166';
  $field_join_equiv['company_zipcode'] = 'company_country_iso3166';
  $field_join_equiv['contact_town'] = 'contact_country_iso3166';
  $field_join_equiv['company_town'] = 'company_country_iso3166';

  // User data categories handling
  $cat_nb = 0;
  $entities = array ('company', 'contact');
  foreach ($entities as $entity) {
    if (is_array($cgp_user[$entity]['category'])) {
      foreach($cgp_user[$entity]['category'] as $cat_name => $one_cat) {
	$cat = "cat${cat_nb}$entity%j";
	$cat_link = "cat${cat_nb}${entity}l%j";
	$operateurs["$cat_name"] = str_replace('%t',"$cat_link",$usercategory_id_in); 
	$operateurs["${cat_name}_code"] = str_replace('%t',"$cat",$usercategory_code_equal); 
	$operateurs_not["$cat_name"] = $operateurs["$cat_name"];
	$operateurs_not["${cat_name}_code"] = $operateurs["${cat_name}_code"];
	$add_join["$cat_name"] = " LEFT JOIN CategoryLink $cat_link
         ON ${entity}entity_entity_id = $cat_link.categorylink_entity_id
           AND $cat_link.categorylink_category = '$cat_name'
       LEFT JOIN Category $cat ON $cat_link.categorylink_category_id = $cat.category_id ";
	
	/*	$add_join["${cat_name}_code"] = " LEFT JOIN CategoryLink $cat_link
         ON ${entity}_id = $cat_link.categorylink_entity_id
           AND $cat_link.categorylink_entity = '$entity'
           AND $cat_link.categorylink_category = '$cat_name'
       LEFT JOIN Category $cat ON $cat_link.categorylink_category_id = $cat.category_id ";
	*/
	$field_multi_op["${cat_name}_code"] = 'OR';
	// cat_code field equiv to cat for join count
	$field_join_equiv["${cat_name}_code"] = "${cat_name}";
	$cat_nb++;
      }
    }
  }

  $select = "
        contact_id as id,
        contact_id,
        contact_lastname,
        contact_firstname,
        kind_minilabel,
        kind_header,
        kind_lang,
        contactfunction_label,
        contact_title,
        contact_service,
        ad1.address_street AS contact_street,
        ad1.address_zipcode AS contact_zipcode,
        ad1.address_town AS contact_town,
        ad1.address_expresspostal AS contact_expresspostal,
        ad1.address_state AS contact_state,
        ctry1.country_name as contact_country,
        WorkPhone.phone_number as \"WorkPhone.phone_number\",
        MobilePhone.phone_number as \"MobilePhone.phone_number\",
        FaxPhone.phone_number as \"FaxPhone.phone_number\",
        HomePhone.phone_number as \"HomePhone.phone_number\",
        email_address,
        contact_archive,
        contact_company,
        company_id,
        company_name,
        ad2.address_street AS company_street,
        ad2.address_zipcode AS company_zipcode,
        ad2.address_town AS company_town,
        ad2.address_expresspostal AS company_expresspostal,
        ad2.address_state AS company_state,
        ctry2.country_name as company_country";

  $from = " Contact LEFT JOIN Company ON contact_company_id=company_id
    INNER JOIN ContactEntity ON contact_id = contactentity_contact_id
    INNER JOIN AddressbookEntity AS abe ON contact_addressbook_id=abe.addressbookentity_addressbook_id
    LEFT JOIN EntityRight ON entityright_entity_id=abe.addressbookentity_entity_id AND entityright_consumer_id IS NULL
    LEFT JOIN Address AS ad1 ON contactentity_entity_id=ad1.address_entity_id AND ad1.address_label = 'WORK;X-OBM-Ref1'
    LEFT JOIN Country as ctry1 ON ad1.address_country=ctry1.country_iso3166 AND ctry1.country_lang='$lang'
    LEFT JOIN Phone as WorkPhone ON WorkPhone.phone_entity_id = contactentity_entity_id AND WorkPhone.phone_label = 'WORK;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as MobilePhone ON MobilePhone.phone_entity_id = contactentity_entity_id  AND MobilePhone.phone_label = 'CELL;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as FaxPhone ON FaxPhone.phone_entity_id = contactentity_entity_id  AND FaxPhone.phone_label = 'WORK;FAX;X-OBM-Ref1'
    LEFT JOIN Phone as HomePhone ON HomePhone.phone_entity_id = contactentity_entity_id  AND HomePhone.phone_label = 'HOME;VOICE;X-OBM-Ref1'
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
    LEFT JOIN CompanyEntity ON company_id = companyentity_company_id
    LEFT JOIN Address as ad2 ON companyentity_entity_id=ad2.address_entity_id AND ad2.address_label = 'HQ;X-OBM-Ref1'
    LEFT JOIN Country as ctry2 ON ad2.address_country=ctry2.country_iso3166 AND ctry2.country_lang='$lang'
    LEFT JOIN Kind ON contact_kind_id = kind_id
    LEFT JOIN ContactFunction ON contact_function_id = contactfunction_id";


  $max_line = 0;
  $has_join_nn = array();

  // handle "n-n relation" as special case (for "AND NOT" or exclusions)
  // To direct criteria (eg contact_name like...) nothing changed
  // To n-n : eg AND NOT companycat = 5.1, handle these in a subquery to
  // exclude the corresponding contact (and not only the line) cause company
  // with 2 cats, will else match
  // Determine which criteria lines are concerned (nn joins)
  if (is_array($criteria)) {
    foreach($criteria as $module => $module_criteria) {
      if (is_array($module_criteria)) {
        foreach($module_criteria as $field => $criterion) {
	  // set default field join equivalence
	  if (isset($field_join_equiv[$field])) {
	    $field_join = $field_join_equiv[$field];
	  } else {
	    $field_join = $field;
	  }
          if (is_array($criterion)) {
            foreach($criterion as $line => $value) {
	      if ($value != '') {
		if ($add_join[$field_join] != '') {
		  $has_join_nn[$line] = true;
		  if ( ($logical_criterion[$line] == 'AND')
		       && ($boolean_criterion[$line] == 'NOT') ) {
		    $cond_andnotn[$line] = true;
		  }
		}
	      }

	      if ($line > $max_line) {
		$max_line = $line;
	      }
	    }
	  }
	}
      }
    }

    // Parse the criteria
    foreach($criteria as $module => $module_criteria) {
      if (is_array($module_criteria)) {
        foreach($module_criteria as $field => $criterion) {
	  // set default field join equivalence
	  if (isset($field_join_equiv[$field])) {
	    $field_join = $field_join_equiv[$field];
	  } else {
	    $field_join = $field;
	  }
          if (is_array($criterion)) {
            foreach($criterion as $line => $value) {
	      
	      if ($value != '') {

		// case AND NOT to n-n criteria line
		if ($cond_andnotn[$line]) {

		  // Add the join only if needed (not already done)
		  if (! isset($cond_andnotn_join_field[$line][$field_join])) {
		    $cond_andnotn_join_field[$line][$field_join] = 0;
		    $cond_andnotn_join[$line] .= ' ' . str_replace('%j','',$add_join[$field_join]);
		  } else {
		    $cond_andnotn_join_field[$line][$field_join]++;
		  }

		  $operation = $operateurs[$field];
		  $operation = str_replace('%j','',$operation);
		  $operation = str_replace('%f',$field,$operation);

		  // handle multi-criteria that need a loop
		  // eg : multi category by code. (c=1 or c like 1.*) OR ...
		  if (isset($field_multi_op[$field])) {
		    $op_multi = '';
		    $values = explode(',', $value);
		    foreach ($values as $one_value) {
		      $one_op = str_replace('%v',$one_value,$operation);
		      if ($op_multi != '') {
			$op_multi .= ' '.$field_multi_op[$field]." $one_op";
		      } else {
			$op_multi .= $one_op;
		      }
		    }
		    $operation = $op_multi;
		    // other criteria (normal)
		  } else {
		    $operation = str_replace('%v',$value,$operation);
		  }

		  // Some fields have no operation associated (eg cat_tree)
		  // so before adding the line with operator, we check
		  if ($operation != '') {
		    if ($cond_andnotn_where[$line] != '') {
		      $cond_andnotn_where[$line] .= " AND ($operation)";
		    } else {
		      $cond_andnotn_where[$line] = " ($operation)";
		    }
		  }

		  // Case Exclusion
		} else if ($boolean_criterion[$line] == 'Exclude') {
		  $has_exclusion = true;
		  // Add the join only if needed (not already done)
		  if (! isset($exclusion_join_field[$line][$field_join])) {
		    $exclusion_join_field[$line][$field_join] = 0;
		    $exclusion_join[$line] .= ' ' . str_replace('%j','',$add_join[$field_join]);
		  } else {
		    $exclusion_join_field[$line][$field_join]++;
		  }
		  $operation = $operateurs_not[$field];
		  $operation = str_replace('%j','',$operation);
		  $operation = str_replace('%f',$field,$operation);

		  // handle multi-criteria that need a loop
		  // eg : multi category by code. c=code or c like code.*
		  if (isset($field_multi_op[$field])) {
		    $op_multi = '';
		    $values = explode(',', $value);
		    foreach ($values as $one_value) {
		      $one_op = str_replace('%v',$one_value,$operation);
		      if ($op_multi != '') {
			$op_multi .= ' '.$field_multi_op[$field]." $one_op";
		      } else {
			$op_multi .= $one_op;
		      }
		    }
		    $operation = $op_multi;
		    // other criteria (normal)
		  } else {
		    $operation = str_replace('%v',$value,$operation);
		  }

		  // Some fields have no operation associated (eg cat_tree)
		  // so before adding the line with operator, we check
		  if ($operation != '') {
		    if ($exclusion_where[$line] != '') {
		      $exclusion_where[$line] .= " AND ($operation)";
		    } else {
		      $exclusion_where[$line] = " ($operation)";
		    }
		  }

		  // Case Normal criteria line
		} else {

		  $select .= $add_select[$field];

		  // If field has not already created a join
		  if (!isset($join_nb[$field_join])) {
		    $join_nb[$field_join] = 0;
		    // If module nb is set And module = publication, do nothing
		    // else add a join
		    if ( (! isset($join_nb[$module]))
			 || ($module != 'publication') ) {
		      $join_nb[$module] = 0;
		      $from .= str_replace('%j',$join_nb[$field_join],$add_join[$field_join]);
		    }

		    // Do nothing if the field has an equivalent field in the
		    // same line, as the from clause will already be set
		  } elseif (($field != $field_join) && ($module_criteria[$field_join][$line] != '')) {

		    // XXXX not optimal as same criteria not tested
		    // But what about crit1 AND toto OR B AND crit2
		    // => crit1 AND (toto OR ( B AND crit2 )) not so easy
		    // %join should increment but same crit not on line+1 
		  } elseif ($logical_criterion[$line] == 'AND'
			    && $line == $old_line + 1) {
		    // AND on same criteria, increment the join cpt associated
		    $join_nb[$field_join] ++;
		    if ($join_nb[$field_join] > $join_nb[$module]) {
		      $join_nb[$module] = $join_nb[$field_join];
		      $from .= str_replace('%j',$join_nb[$field_join],$add_join[$field_join]);
		    }
		  }

		  // join_line to remember the join number for a field on a line
		  // to handle fields that links to the same join
		  // (eg pub_id and subscription_reception)
		  if (! isset($join_line[$field_join][$line])) {
		    $join_line[$field_join][$line] = $join_nb[$field_join];
		  }

		  // tag the module has having criteria
		  $module_fill[$module] = true;

		  //
		  if ($boolean_criterion[$line] == 'NOT') {
		    $operation = $operateurs_not[$field];
		  } else {
		    $operation = $operateurs[$field];
		  }
		  $operation = str_replace('%j',$join_line[$field_join][$line],$operation);
		  $operation = str_replace('%f',$field,$operation);

		  // handle multi-criteria that need a loop
		  // eg : multi category by code. c=code or c like code.*
		  if (isset($field_multi_op[$field])) {
		    $op_multi = '';
		    $values = explode(',', $value);
		    foreach ($values as $one_value) {
		      $one_op = str_replace('%v',$one_value,$operation);
		      if ($op_multi != '') {
			$op_multi .= ' '.$field_multi_op[$field]." $one_op";
		      } else {
			$op_multi .= $one_op;
		      }
		    }
		    $operation = $op_multi;
		    // other criteria (normal)
		  } else {
		    $operation = str_replace('%v',$value,$operation);
		  }

		  // Some fields have no operation associated (eg cat_tree)
		  // so before adding the line with operator, we check
		  if ($operation != '') {
		    $condition[$line] .= $and[$line]." ($operation)";
		    $and[$line] = ' AND';	
		  }
		}

	      }
	      $old_line = $line;
            }
          }
        }
      }
    }

    // Write each criteria
    for ($line = 0; $line <= $max_line; $line++) {
      $cond = $condition[$line];
      $cond_andnotn = $cond_andnotn_where[$line];

      // Case Normal criteria line
      if ($cond != '') {
	if ($line == 0) {
	  $logical_criterion[$line] = '';
	}
	$where .= '
 ' . $logical_criterion[$line].' '.$boolean_criterion[$line]." ($cond)";

	// case AND NOT to n-n criteria line
      } else if ($cond_andnotn != '') {
	$where .= " AND contact_id NOT IN (
          SELECT contact_id
          FROM Contact
            LEFT JOIN Company ON contact_company_id=company_id " .
            $cond_andnotn_join[$line] . "
          WHERE $cond_andnotn)";
      }
    }

    if ($where == '') {
      $where = 'true';
    }

    // We handle exlusion appart from criteria to ensure exclusion
    if ($has_exclusion) {
      foreach($exclusion_where as $line=>$value) {

	// Exclusion on field with n-n relation
	if ($has_join_nn[$line]) {
	  $exclusion .= " AND contact_id NOT IN (
          SELECT contact_id
          FROM Contact
            LEFT JOIN Company ON contact_company_id=company_id " .
            $exclusion_join[$line] . '
          WHERE '. $exclusion_where[$line] . ')';

	// Exclusion on field with no n-n relation
	} else {
	  $exclusion .= ' AND NOT ('. $exclusion_where[$line].')';
	}
      }

      $where = "($where) $exclusion";
    }
  }


  // Handle the Mailing enable flag
  if ($mailing_ok == 1) {
    $where = " contact_mailing_ok = '1' AND ($where) ";
  }

  // Handle the contact archive flag
  if ($contact_arch == 0) {
    $where = " (contact_archive != '1') AND ($where) ";
  }

  // Allow only contacts from public (read wise) address books
  $where = " (entityright_read=1) AND ($where)";

  // If any (not in insert mode), add statics contacts to the query
  if ($id > 0) {
    $con_q = run_query_list_static_contact_id($id);
    if ($con_q->num_rows() > 0) {
      $first = true;
      $where_static = '(';
      while ($con_q->next_record()) {
	$c_id = $con_q->f('contactlist_contact_id');
        $c_id = sql_parse_id($c_id, true);
	if ($first) {
	  $where_static .= "contact_id $c_id ";
	} else {
	  $where_static .= " OR contact_id $c_id ";
	}
	$first = false;
      }
      $where_static .= ')';
    }
  }

  if ($where != '') {
    if ($where_static != '') {
      $where = " ($where) OR $where_static";
    } else {
      $where = " $where";
    }
  }

  //--------------------------------------------------------------------------
  // If publication info asked without any pub criteria, add pub infos
  // and prepare a Subquery
  // SELECT $over_select FROM (SELECT $select ...) AS FOO GROUP BY $over_group
  if ($info_pub == 1) {

    // Add Subscription and publication infos in Select
    $select .= ', subscription_id
                , subscription_timecreate
                , subscription_quantity
                , publication_lang
                , publication_title';

    // If no pub criteria selected add Subscription and publication Join
    if (! $module_fill['publication']) {
      $from .= '
        LEFT JOIN Subscription ON subscription_contact_id = contact_id
        LEFT JOIN Publication ON subscription_publication_id = publication_id';
    }

    // We transform field "column_name as col" to col
    $conv_select .= preg_replace('/^(.* as) (.*)$/mi', '$2', $select);
    // We add extra info to the Over select
    $over_select .= "$conv_select";
    $over_select = "SELECT $over_select FROM (";

    $over_end = ") AS FOO
        GROUP BY
        $conv_select";
  }

  $query = trim("$over_select SELECT distinct $select FROM $from WHERE $where $over_end ORDER BY contact_id");

  $ret['query'] = $query;
  $ret['where'] = $where;
  $ret['from'] = $from;

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Staticaly registered contacts ids to the list 
// Parameters:
//   - $id : List id
// Returns:
//   DB object result with statically registered contact
///////////////////////////////////////////////////////////////////////////////
function run_query_list_static_contact_id($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT contactlist_contact_id
    FROM ContactList
    WHERE contactlist_list_id $id ";

  display_debug_msg($query, $cdg_sql, 'run_query_list_static_contact_id()');
  $obm_q= new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// External use :
// Return the DBO with distincts contact ids of the list
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function ext_list_get_contact_ids($id) {
  global $cdg_sql, $clist_mode_normal, $clist_mode_expert;

  if ($id > 0) {
    $list_q = run_query_list_detail($id);
  } else {
    return false;
  }

  $query = $list_q->f('list_query');
  $mode = $list_q->f('list_mode');

  // If list has a dynamic part
  if ($query != '') {

    // Case expert query, we try to find the correct num_rows
    if ($mode == $clist_mode_expert) {
      $query_c = $query;
 
      // Normal mode we optimize the query
    } else {
      $dynlist = make_list_query_from_db($list_q);
      $from = $dynlist['from'];
      $where = $dynlist['where'];
      $query_c = "SELECT distinct contact_id FROM $from WHERE $where";
    }

    // List is static (has only static contacts)
  } else {
    $id = sql_parse_id($id, true);
    $query_c = "SELECT
        distinct contactlist_contact_id as contact_id
      FROM ContactList
      WHERE contactlist_list_id $id";
  }

  display_debug_msg($query_c, $cdg_sql, 'ext_list_get_contact_ids()');
  $obm_q = new DB_OBM;
  $r = $obm_q->query($query_c, 1);
  if (is_string($r)) {
    return false;
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// External use :
// Return the DBO with distincts company ids of the list
// Parameters:
//   - $id : list id
///////////////////////////////////////////////////////////////////////////////
function ext_list_get_company_ids($id) {
  global $cdg_sql, $clist_mode_normal, $clist_mode_expert;

  if ($id > 0) {
    $list_q = run_query_list_detail($id);
  } else {
    return false;
  }

  $query = $list_q->f('list_query');
  $mode = $list_q->f('list_mode');
  $query_comp = '';

  // If list has a dynamic part
  if ($query != '') {

    // Case expert query, we try to handle the query
    if ($mode == $clist_mode_expert) {

      // We remove the ORDER BY clause
      $query = preg_replace('/order by .*$/i','',$query);

      // We process only if we recognize the query
      if (preg_match('/(FROM (.*))( GROUP BY)?(?(3)|$)/Uis', $query, $matches)) {
	$query_comp = "SELECT distinct(company_id) ". $matches[1];

	// Add static contacts
	$cl_q = run_query_list_static_contact_id($id);
	if ($cl_q->num_rows() > 0) {
	  while($cl_q->next_record()) {
	    $c_id = $cl_q->f('contactlist_contact_id');
	    $where_static .= " OR contact_id = '$c_id'";
	  }
	  $query_comp .= $where_static;
	}
      }

      // Normal mode we optimize the query
    } else {
      $dynlist = make_list_query_from_db($list_q);
      $from = $dynlist['from'];
      $where = $dynlist['where'];
      $query_comp = "SELECT distinct company_id FROM $from WHERE $where";
    }
  }

  // List is static (has only static contacts) or expert query not processed
  // We initialize the query with static contacts
  if ($query_comp == '') {
    $id = sql_parse_id($id, true);
    $query_comp = "SELECT
        distinct company_id
      FROM ContactList
        JOIN Contact ON contactlist_contact_id=contact_id
        JOIN Company ON contact_company_id=company_id
      WHERE contactlist_list_id $id ";
  }

  display_debug_msg($query_comp, $cdg_sql, 'ext_list_get_company_ids()');
  $obm_q = new DB_OBM;
  $obm_q->query($query_comp);

  return $obm_q;
}


?>
