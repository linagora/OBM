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
// OBM - File : publication_query.inc                                        //
//     - Desc : publication query File                                       //
// 2004-01-28 Rande Mehdi                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Publication search query execution
// Parametes:
//   - $publication[]    : publication search criteria
//     keys used       archive, title, phone, type, zip
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_search($publication) {
  global $c_all, $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $publication['sql_order_dir'];
  $sql_order_field = $publication['sql_order_field'];

  $archive = $publication['archive'];
  $title = sql_search_text_parse($publication['title']);
  $lang = sql_search_text_parse($publication['lang']);
  $type = $publication['type'];
  $year = sql_search_text_parse($publication['year']);
  $market = $publication[''];
  $zip = $publication['zip'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain('publication');
	
  $and = '';
  if ($title != '') {
    $where .= " publication_title $like '".$obm_q->escape($title)."%'";
    $and = 'AND';
  }
  if (($type != $c_all) && ($type != '')) { 
    $type = sql_parse_id($type, true);
    $where .= " $and publication_type_id $type ";
    $and = 'AND';
  }
  if ($year != '') {
    $year = sql_parse_int($year);
    $where .= " $and publication_year = '".$obm_q->escape($year)."'";
    $and = 'AND';
  }
  if ($lang != '') {
    $where .= " $and publication_lang $like '%".$obm_q->escape($lang)."%'";
    $and = 'AND';
  }
  if ($where != '') {
    $whereq = "WHERE $where $multidomain";
  } else {
    $whereq = "WHERE 1=1 $multidomain";
  }

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : ' publication_year DESC,publication_title';
  $orderq = "ORDER BY $order $sql_order_dir";

  $query = "SELECT
      publication_id as id,
      publication_id,
      publication_title,
      publication_type_id,
      publication_year,
      publication_lang,
      publicationtype_label
    FROM Publication
         LEFT JOIN PublicationType ON publication_type_id=publicationtype_id
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Publication $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_publication_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_detail($p_id) {
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $p_id = sql_parse_id($p_id, true);
  $timeupdate = sql_date_format($type, 'publication_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($type, 'publication_timecreate', 'timecreate');
  $multidomain = sql_multidomain('publication');
	
  $query = "SELECT *,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Publication
         LEFT JOIN PublicationType on publication_type_id=publicationtype_id
         LEFT JOIN UserObm as c on publication_usercreate=c.userobm_id
         LEFT JOIN UserObm as u on publication_userupdate=u.userobm_id
    WHERE publication_id $p_id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// List query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_list() {
  global $cdg_sql;

  $multidomain = sql_multidomain('list');

  $query = "SELECT list_id,
      list_name,
      userobm_login
    FROM List LEFT JOIN UserObm ON list_usercreate=userobm_id
    WHERE 1=1 $multidomain
    ORDER BY list_name"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_publication_list()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : num, archive, title, type, ad1, ad2, ad3, zip, town, cdx
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_insert($publication) {
  global $obm, $cdg_sql, $public_contact_cat;

  $obm_q = new DB_OBM;
  $title = $publication['title'];
  $type = sql_parse_id($obm_q->escape($publication['type']));
  $lang = $publication['lang'];
  $year = $publication['year'];
  $desc = $publication['desc'];
  $cats1 = $publication[$public_contact_cat];
  $list_sub = $publication['list'];
  $time = date('Y-m-d H:i:s');
  $domain_id = sql_parse_id($obm['domain_id']);
  $uid = sql_parse_id($obm['uid']);
  
  $query = "INSERT INTO Publication (
    publication_timeupdate,
    publication_timecreate,
    publication_userupdate,
    publication_usercreate,
    publication_domain_id,
    publication_title,
    publication_type_id,
    publication_lang,
    publication_year,
    publication_desc)
  VALUES (null,
    '$time',
    null,
    $uid,
    $domain_id,
    '".$obm_q->escape($title)."',
    $type,
    '".$obm_q->escape($lang)."',
    '".$obm_q->escape($year)."',
    '".$obm_q->escape($desc)."'
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_insert()');
  $retour = $obm_q->query($query);
  $p_id = $obm_q->lastid(); 
  $entity_id = of_entity_insert($GLOBALS['module'], $p_id);  
  $publication['publication_id'] = $p_id;
  
  if ( (is_array($cats1) && (count($cats1)>0) )
       || (is_array($list_sub) && (count($list_sub)>0) ) ) {
    run_query_publication_auto_subscription($publication);
  }
  return $p_id;
}


///////////////////////////////////////////////////////////////////////////////
// Auto Subscription Insertion query execution                                 
// Parameters:
//   - $publication[] : List of contact_type to subscribe
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_auto_insert($publication) {
  global $obm, $cdg_sql;
  
  $obm_q = new DB_OBM;
  $id = sql_parse_id($publication['publication_id']);
  $id_orig = sql_parse_id($publication['id_orig'], true);
  $recept = sql_parse_id($obm_q->escape($publication['recept']), true);
  $domain_id = sql_parse_id($obm['domain_id']);
  $uid = sql_parse_id($obm['uid']);
  if( $recept == '' || $id_orig == ''  ) {
    return false;
  }
  $query = "INSERT INTO Subscription (
    subscription_timeupdate,
    subscription_timecreate,
    subscription_userupdate,
    subscription_usercreate,
    subscription_domain_id,
    subscription_contact_id,
    subscription_publication_id,
    subscription_renewal,
    subscription_reception_id,
    subscription_quantity)
  SELECT 
    null,
    '" . date('Y-m-d H:i:s') . "',
    null,
    $uid,
    $domain_id,
    subscription_contact_id,
    $id,
    1,
    subscription_reception_id,
    subscription_quantity  
  FROM Subscription 
  WHERE subscription_renewal = '1' 
    AND subscription_reception_id $recept 
    AND subscription_publication_id $id_orig ";
  
  display_debug_msg($query, $cdg_sql, 'run_query_publication_auto_insert()');

  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Auto Subscription Insertion query execution                                 
// Parameters:
//   - $publication : pub hash [$public_contact_cat], [list_sub] of interest
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_auto_subscription($publication) {
  global $obm, $cdg_sql, $c_focsys, $public_contact_cat;

  $p_id = sql_parse_id($publication['publication_id']);
  $cats1 = $publication[$public_contact_cat];
  $list_sub = $publication['list'];
  $recept = sql_parse_id($c_focsys);
  $lang = $publication['lang'];
  $lang_array = explode('-', $lang);
  $domain_id = sql_parse_id($obm['domain_id']);
  $nb = 0;
  $uid = sql_parse_id($obm['uid']);	
  $public_cat = sql_parse_id($public_contact_cat, true);
  // Subscription according to contact category 1
  if (is_array($cats1) && (count($cats1)>0)) {
    $obm_q = new DB_OBM;
    $type = $obm_q->type;
    $like = sql_casei_like($type);

    $sub_query = "
    SELECT 
      null,
      '".date('Y-m-d H:i:s')."',
      null,
      $uid,
      $domain_id,
      contact_id,
      $p_id,
      1,
      $recept,
      1  
    FROM Contact
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
      LEFT JOIN CategoryLink categorylink_category $public_cat 
           AND categorylink_entity_id = contactentity_entity_id
      LEFT JOIN Kind ON kind_id = contact_kind_id
    WHERE categorylink_category_id IN (";

    foreach($cats1 as $category_id) {
      $sub_query .= "$coma '$category_id'";
      $coma = ',';
    }
    $sub_query .= ") 
  AND (";
    $coma = '';
    foreach($lang_array as $lang_id) {
      $sub_query .= "$coma kind_lang $like '$lang_id' ";
      $coma = 'OR';
    }
    $sub_query .= ") 
  GROUP BY contact_id";

    display_debug_msg($sub_query, $cdg_sql, 'run_query_publication_auto_subscription()');
    $retour = $obm_q->query($sub_query);
    if ($obm_q->num_rows() > 0) {
      $nb = $obm_q->num_rows();
      $query = "INSERT INTO Subscription (
      subscription_timeupdate,
      subscription_timecreate,
      subscription_userupdate,
      subscription_usercreate,
      subscription_domain_id,
      subscription_contact_id,
      subscription_publication_id,
      subscription_renewal,
      subscription_reception_id,
      subscription_quantity)
    $sub_query";
      display_debug_msg($query, $cdg_sql, 'run_query_publication_auto_subscription()');
      $retour = $obm_q->query($query);
    } else {
      $retour = true;
    }
  }


  // Subscription according to contact list
  if (is_array($list_sub) && (count($list_sub)>0)) {
    $obm_q = new DB_OBM;
    $contacts = array();

    // Loop for each list
    foreach ($list_sub as $list_id) {
      $c_q = ext_list_get_contact_ids($list_id);

      // Process each list contact (if not already done)
      while ($c_q->next_record()) {
	$c_id = $c_q->f('contact_id');

	// If subscription not already set for this contact
	if (! in_array($c_id, $contacts)) {
	  $c_lang = run_query_publication_get_contact_lang($c_id);
	  if ((in_array($c_lang, $lang_array))
	      || (in_array(strtoupper($c_lang), $lang_array))) {
	    $contacts[] = $c_id;
            $c_id = sql_parse_id($c_id);
	    $query = "INSERT INTO Subscription (
            subscription_timeupdate,
            subscription_timecreate,
            subscription_userupdate,
            subscription_usercreate,
            subscription_domain_id,
            subscription_contact_id,
            subscription_publication_id,
            subscription_renewal,
            subscription_reception_id,
            subscription_quantity)
          VALUES ( 
            null,
            '" . date('Y-m-d H:i:s') . "',
            null,
            $uid,
            $domain_id,
            $c_id,
            $p_id,
            1,
            $recept,
            1)";
	    display_debug_msg($query, $cdg_sql, "run_query_publication_auto_subscription(from list : contact : $c_id)");
	    $retour = $obm_q->query($query);
	  }
	}
      }
    }
    $nb += count($contacts);
  }

  if ($retour) {
    return $nb;
  } else {
    return false;
  }
}

 
///////////////////////////////////////////////////////////////////////////////
// Subscription Insertion query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_insert_subscription($publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $c_id = sql_parse_id($publication['contact_id']);
  $quantity = $publication['quantity'];
  $p_id = sql_parse_id($publication['publication_id']);
  $renewal = sql_parse_int($publication['renewal']);
  $recept = sql_parse_id($publication['recept']);
  $domain_id = sql_parse_id($obm['domain_id']);
  
  $query = "INSERT INTO Subscription (
    subscription_timeupdate,
    subscription_timecreate,
    subscription_userupdate,
    subscription_usercreate,
    subscription_domain_id,
    subscription_contact_id,
    subscription_publication_id,
    subscription_renewal,
    subscription_reception_id,
    subscription_quantity)
  VALUES (null,
    '" . date('Y-m-d H:i:s') . "',
    null,
    $obm[uid],
    $domain_id,
    $c_id,
    $p_id,
    $renewal,
    $recept,
    '".$obm_q->escape($quantity)."'
    )";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_insert_subscription()');
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Subscription Update query execution
// Parameters:
//   - $publication[] : Entry's values
//     keys used  : 
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_update_subscription($publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $s_id = sql_parse_id($publication['subscription_id'], true);
  $quantity = $publication['quantity'];
  $renewal = sql_parse_int($publication['renewal']);
  $recept = sql_parse_id($obm_q->escape($publication['recept']));
  $multidomain = sql_multidomain('subscription');
  
  $query = "UPDATE Subscription SET 
      subscription_timeupdate = '". date('Y-m-d H:i:s') ."',
      subscription_userupdate=$obm[uid],
      subscription_renewal=$renewal,
      subscription_reception_id=$recept,
      subscription_quantity='".$obm_q->escape($quantity)."'
    WHERE	
      subscription_id $s_id $multidomain";
    
  display_debug_msg($query, $cdg_sql, 'run_query_publication_update_subscription()');
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update query execution
// Parameters:
//   - $cid       : publication id
//   - $publication[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_update($pid, $publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $title = $publication['title'];
  $type = sql_parse_id($obm_q->escape($publication['type']));
  $lang = $publication['lang'];
  $year = $publication['year'];
  $desc = $publication['desc'];
  $multidomain = sql_multidomain('publication');
  $pid = sql_parse_id($pid, true);
  $query = "UPDATE Publication SET
      publication_timeupdate='". date('Y-m-d H:i:s') ."',
      publication_userupdate=$obm[uid],
      publication_title='".$obm_q->escape($title)."',
      publication_lang='".$obm_q->escape($lang)."',
      publication_year='".$obm_q->escape($year)."',
      publication_desc='".$obm_q->escape($desc)."',
      publication_type_id=$type
    WHERE publication_id $pid $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_update()');
  $retour = $obm_q->query($query);
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : publication id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_delete($p_id) {
  global $cdg_sql;
  of_entity_delete($GLOBALS['module'], $p_id);
  $p_id = sql_parse_id($p_id, true);	
  $multidomain = sql_multidomain('publication');
  $query = "DELETE FROM Publication WHERE publication_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : publication id
//   - $c_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_delete_subscription($params) {
  global $cdg_sql;

  $s_id = sql_parse_id($params['subscription_id'], true);
  $multidomain = sql_multidomain('subscription');
  $query = "DELETE FROM Subscription
    WHERE subscription_id $s_id 
      $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_publication_delete_subscription()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);
  
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the contacts attached to the publication
// Parameters:
//   - $p_id : publication Id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_contact_links($p_id) {
  global $cdg_sql;
  $p_id = sql_parse_id($p_id, true);
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('contact');
  $query = "SELECT DISTINCT contact_id, contact_lastname, contact_firstname 
    FROM Contact LEFT JOIN Subscription ON subscription_contact_id = contact_id
    WHERE subscription_publication_id $p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_publication_contact_links()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the publications with this title and zip (not with this id if set)
// Parameters:
//   - $p_id : publication Id not to return
//   - $title : title
//   - $zip  : zip
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_check($p_id, $title, $lang) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $like = sql_casei_like($type);

  $where_id = '';
  if ($p_id != '') {
    $p_id = sql_parse_id($p_id, true, true);
    $where_id = "AND publication_id $p_id ";
  }
  $multidomain = sql_multidomain('publication');
  $query = "SELECT distinct publication_id, publication_title, publication_lang
    FROM Publication
    WHERE publication_title $like '".$obm_q->escape($title)."%'
      AND publication_lang $like '".$obm_q->escape($lang)."%'
      $where_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_check()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Type publication query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type() {
  global $cdg_sql;
	
  $multidomain = sql_multidomain('publicationtype');

  $query = "SELECT * FROM PublicationType 
		WHERE 1=1 $multidomain ORDER BY publicationtype_label"; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_publication_type()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode subscription query execution
// Return:
//   Database Object
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_reception() {
  global $cdg_sql, $c_focsys;

  $multidomain = sql_multidomain('subscriptionreception');
  $focsys = sql_parse_id($c_focsys, true, true);
  $query = "SELECT *
    FROM SubscriptionReception
    WHERE subscriptionreception_id $focsys $multidomain
    ORDER BY subscriptionreception_label DESC "; 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'run_query_publication_subscription_reception()');

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Type insertion query construction and execution
// Parameters:
//   - $publication : publication hash info : keys used : type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_insert($publication) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id($obm['uid']);
  $type = $publication['type_label'];
  $domain_id = sql_parse_id($obm['domain_id']);
	
  $query = "INSERT INTO PublicationType (
    publicationtype_timecreate,
    publicationtype_usercreate,
    publicationtype_domain_id,
    publicationtype_label)
  VALUES (
    '$timecreate',
    $usercreate,
    $domain_id,
    '".$obm_q->escape($type)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_type_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Reception mode insertion query construction and execution
// Parameters:
//   - $publication : publication hash info : keys used : recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_insert($publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $timecreate = date('Y-m-d H:i:s');
  $usercreate = sql_parse_id($obm['uid']);
  $recept = $publication['recept_label'];
  $domain_id = sql_parse_id($obm['domain_id']);
	
  $query = "INSERT INTO SubscriptionReception (
    subscriptionreception_timecreate,
    subscriptionreception_usercreate,
    subscriptionreception_domain_id,
    subscriptionreception_label)
  VALUES (
    '$timecreate',
    $usercreate,
    $domain_id,
    '".$obm_q->escape($recept)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_recept_insert()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type update query execution
// Parameters:
//   - $publication : publication hash info : keys used : type, type_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_update($publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id($obm['uid']);
  $id = sql_parse_id($obm_q->escape($publication['type']), true);
  $type = $publication['type_label'];
  $multidomain = sql_multidomain('publicationtype');
	
  $query = "UPDATE PublicationType SET
      publicationtype_label='".$obm_q->escape($type)."',
      publicationtype_timeupdate='$timeupdate',
      publicationtype_userupdate=$userupdate
    WHERE
      publicationtype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_type_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Reception update query execution
// Parameters:
//   - $publication : publication hash info : keys used : recept, recept_label
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_update($publication) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $timeupdate = date('Y-m-d H:i:s');
  $userupdate = sql_parse_id($obm['uid']);
  $id = sql_parse_id($obm_q->escape($publication['recept']), true);
  $recept = $publication['recept_label'];
  $multidomain = sql_multidomain('subscriptionreception');

  $query = "UPDATE SubscriptionReception SET
      subscriptionreception_label='".$obm_q->escape($recept)."',
      subscriptionreception_timeupdate='$timeupdate',
      subscriptionreception_userupdate=$userupdate
    WHERE
      subscriptionreception_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_recept_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type deletion query execution
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_delete($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $multidomain = sql_multidomain('publicationtype');
  $query = "DELETE FROM PublicationType WHERE publicationtype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_type_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Reception deletion query execution
// Parameters:
//   - $id : Type id to delete
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_delete($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('subscriptionreception');
  $id = sql_parse_id($id, true);
  $query = "DELETE
    FROM SubscriptionReception
    WHERE subscriptionreception_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_recept_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Type - Publication links query execution
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_type_links($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('publication');
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT publication_title, publication_id, publication_type_id
    FROM Publication
    WHERE publication_type_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_type_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Reception - Subscription links query execution
// Parameters:
//   - $p_id : type id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_recept_links($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('subscription');
  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT publication_title,
      publication_id,
      subscription_reception_id,
      contact_firstname,
      contact_id  
    FROM Subscription
      LEFT JOIN Publication ON publication_id = subscription_publication_id
      LEFT JOIN Contact ON contact_id = subscription_contact_id
    WHERE subscription_reception_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_recept_links()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Type from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_publication_type_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('publicationtype');
  $id = sql_parse_id($id, true);
  $query = "SELECT publicationtype_label
    FROM PublicationType
    WHERE publicationtype_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_publication_type_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('publicationtype_label');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get the label of a Reception Mode from its id
// Parameters:
//   - $id : id
///////////////////////////////////////////////////////////////////////////////
function get_publication_recept_label($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('subscriptionreception');
  $id = sql_parse_id($id, true);
  $query = "SELECT subscriptionreception_label
    FROM SubscriptionReception 
    WHERE subscriptionreception_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_publication_recept_label()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  
  $retour = $obm_q->f('subscriptionreception_label');
  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Publication environment checking (same publications exists ?)
// Parameters: :/ Tous les champs ici
//   - $cid       : publication id
//   - $publication[] : publication's values
// Returns:
//   - Publication Database object with list of similar publications
///////////////////////////////////////////////////////////////////////////////
function check_publication_context($cid, $publication) {

  $title = $publication['title'];
  $lang = $publication['lang'];

  // if a publication with same title and zip exists, return false
  $co_q = run_query_publication_check($cid, $title, $lang);
  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_publication_data($cid, $publication) {
  global $l_fill_title, $l_j_check_year,$l_j_fill_lang, $php_regexp_pub_lang;
  global $err, $l_j_check_lang;

  $title = $publication['title'];
  $year = $publication['year'];
  $lang = $publication['lang'];
  $desc = $publication['desc'];
  $type = $publication['type'];

  if (trim($title) == '') {
    $err['msg'] = $l_fill_title;
    return false;
  }

  if (($year == '') || ($year < 1900 || $year > 2200)) {
    $err['msg'] = $l_j_check_year. " : $year";
    return false;
  }

  if ($lang == '') {
    $err['msg'] = $l_j_fill_lang;
    return false;
  } else if (preg_match($php_regexp_pub_lang, $lang) == 0) {
    $err['msg'] = $l_j_check_lang . " : $lang";
    return false;
  }

  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Publication Form Data checking and formatting
// Parameters:
//   - $cid   : publication id
//   - $publication[] : values checked
//     keys used  : num, title, zip, phone, fax, web, email
// Returns:
//   - (true | false) : true if data are ok, else false 
///////////////////////////////////////////////////////////////////////////////
function check_publication_data_subscription_form($cid, $publication) {
  global $l_j_check_quantity,$l_j_select_publication ;
  global $err;

  $quantity = $publication['quantity'];
  $p_id = $publication['publication_id'];

  if (($p_id == '') || !($p_id >= 0) ) {
    $err['msg'] = $l_j_select_publication;
    return false;
  }
  if (($quantity == '')) {
    $err['msg'] = $l_j_check_quantity;
    return false;
  }
  return true; 
}


///////////////////////////////////////////////////////////////////////////////
// Check if the publication can be deleted
// Parameters:
//   - $p_id : publication id
// Returns:
//   true if the publication can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_publication_can_delete($p_id) {
  global $err, $ok_msg, $block;
  global $path;
  global $l_contact, $l_link_contact, $l_link_contact_no;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_publication_contact_links($p_id);
  $nb = $obm_q->num_rows();

  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] = $l_link_contact;
  } else {
    $ok_msg = "$l_link_contact_no";
  }

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Contact detail query execution
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_get_contact_lang($p_id) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $multidomain = sql_multidomain('contact');
  $p_id = sql_parse_id($p_id, true);
  $obm_q = new DB_OBM;
    $query = "SELECT kind_lang
      FROM Contact LEFT JOIN Kind ON contact_kind_id = kind_id
      WHERE contact_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_get_contact_lang()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f('kind_lang');
}


///////////////////////////////////////////////////////////////////////////////
//
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_external_search($hash_table) {
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $contact = sql_parse_id($hash_table['contact_id'], true);
  $obm_q = new DB_OBM;
  $type = $obm_q->type;
  $timeupdate = sql_date_format($type, 'subscription_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($type, 'subscription_timecreate', 'timecreate');
  $multidomain = sql_multidomain('subscription');
	
  $query = "SELECT
      $timeupdate,
      $timecreate,
      subscription_id,
      subscription_contact_id,
      subscription_publication_id,
      publication_title,
      publication_year,
      publication_lang,
      subscription_renewal,
      subscriptionreception_label,
      subscription_quantity 
    FROM Subscription 
      LEFT JOIN Publication ON publication_id = subscription_publication_id
      LEFT JOIN SubscriptionReception ON subscriptionreception_id = subscription_reception_id 	
    WHERE subscription_contact_id $contact $multidomain
    ORDER BY subscription_timecreate desc";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_subscription_external_search()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
//
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function run_query_publication_subscription_detail($params){
  global $db_type_mysql,$db_type_pgsql, $cdg_sql;

  $s_id = sql_parse_id($params['subscription_id'], true);
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('subscription');
	
  $query = "SELECT
      subscription_id,
      subscription_contact_id,
      subscription_publication_id,
      publication_title,
      subscription_renewal,
      subscription_reception_id,
      subscription_quantity 
    FROM Subscription 
	LEFT JOIN Publication ON publication_id = subscription_publication_id
    WHERE subscription_id $s_id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_publication_subscription_detail()');
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q;
}

?>
