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
// OBM - File : people_query.inc                                             //
//     - Desc : People query & db File                                       //
// 2008-10-08 : Vincent Bernard                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id: people_query.inc 3114 2008-09-30 16:10:15Z benoitc $
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// User query search query execution
// Parameters:
//   - $people[] : people search criteria
//     keys used  : name, perms
//   - $sql_order_field : infos for order clause
//   - $sql_order_dir : direction for order clause (asc, desc)
//   - $filter_entity : if and which filter_entity to set (eg calendar readable)
///////////////////////////////////////////////////////////////////////////////
function run_query_people_search($people) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;

  $sql_order_dir = $people['sql_order_dir'];
  $sql_order_field = $people['sql_order_field'];

  $filter_entity = $people['filter_entity'];
  $filter_pattern = $people['filter_pattern'];
  $lname = sql_search_text_parse($people['lastname']);
  $fname = sql_search_text_parse($people['firstname']);
  $email = sql_search_text_parse($people['email']);
  $desc = sql_search_text_parse($people['desc']);
  $phone = $people['phone'];
  $address = $people['address'];
  $service = $people['service'];
  $zipcode = $people['zipcode'];
  $cedex = $people['cedex'];
  $delegation = $people['delegation'];
  $direction = $people['direction'];
  $town = $people['town'];
  $fax = $people['fax'];
  $company = $people['company'];
  $status = $people['status'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type);
  $multidomain_userobm = sql_multidomain('userobm');
  $multidomain_contact = sql_multidomain('contact');

  $where_userobm = "userobm_archive = 0";
  $where_contact = "contact_archive = '0'";

  if ($filter_entity == 'calendar') {
    $users = OBM_Acl::getAllowedEntities($obm['uid'], 'calendar', $filter_pattern);
    $users_ids = array_keys($users);
    if ((is_array($users_ids)) && (count($users_ids) > 0)) {
      $where_filter_entity = " AND (userobm_id IN ($obm[uid], ";
      foreach($users_ids as $u_id) {
	$where_filter_entity .= "$coma'$u_id'";
	$coma = ',';
      }
      $where_filter_entity .= '))';
    } else {
      // If no users have given $uid with read right, return empty set
      $where_filter_entity = ' AND userobm_id < 0';
    }
    $show_contact = true;
    $where_userobm .= " AND userobm_status = 'VALID'";
    // XXXXXX Migrate the access right to the Addressbook
    $where_contact .= " AND (";
    $where_contact .= "1 = 1";
    $f_contact_ids = run_query_people_allowed_access_contacts();
    if ((is_array($f_contact_ids)) && (count($f_contact_ids) > 0)) {
      $f_contact_ids = array_map(sql_parse_id,$f_contact_ids);
      $where_contact .= " OR contact_id IN (".implode(',',$f_contact_ids).")";
    }
    $where_contact .= ")";

    
  } else {
    if (trim($status) != '') {
      $where_userobm .= " AND userobm_status = '".$obm_q->escape($status)."'";
    }
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != '') {
     // UserObm
     $where_userobm .= " AND userobm_lastname #LIKE '".$obm_q->escape($lname)."%'";
     // Contact
     $where_contact .= " AND (contact_lastname #LIKE '".$obm_q->escape($lname)."%' or contact_aka #LIKE '".$obm_q->escape($lname)."%')";
  }

  // If a firstname indication has been specified, get it
  if (trim($fname) != '') {
     // UserObm
     $where_userobm .= " AND userobm_firstname #LIKE '".$obm_q->escape($fname)."%'";
     // Contact
     $where_contact .= " AND contact_firstname #LIKE '".$obm_q->escape($fname)."%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != '') {
    list($left,$right) = split('@',$email);
    if ($right === null){
      $left = $obm_q->escape($left).'%';
      $right = $left;
      $condition = 'OR';
    } else {
      $condition = 'AND';
    }
    if (!$left) {
      $left = '%';
    }
    $right = $obm_q->escape($right).'%';
    $where_userobm .= " AND
      userobm_email != '' AND (
          ( userobm_email #LIKE '$left@$right' OR userobm_email #LIKE '$left@$right\r\n%' OR userobm_email #LIKE '%\r\n$left@$right\r\n%' OR userobm_email #LIKE '%\r\n$left@$right' )
          OR (
          ( userobm_email #LIKE '$left' OR userobm_email #LIKE '$left\r\n%' OR userobm_email #LIKE '%\r\n$left\r\n%' OR userobm_email #LIKE '%\r\n$left' )
          $condition
          ( domain_name #LIKE '$right' OR domain_alias #LIKE '$right' OR domain_alias #LIKE '$right\r\n%' OR domain_alias #LIKE '%\r\n$right\r\n%' OR domain_alias #LIKE '%\r\n$right' ))
        ) ";
    // Contact
    $where_contact .= " AND (contactentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address #LIKE '".$obm_q->escape($email)."%'))";
  }

  // If a phone number indication has been specified, get it
  if (trim($phone) != '') {
    // UserObm
    $where_userobm .= " AND (userobm_phone #LIKE '%".$obm_q->escape($phone)."%'
      OR userobm_phone2 #LIKE '%".$obm_q->escape($phone)."%'
      OR userobm_mobile #LIKE '%".$obm_q->escape($phone)."%'
      OR userobm_fax #LIKE '%".$obm_q->escape($phone)."%'
      OR userobm_fax2 #LIKE '%".$obm_q->escape($phone)."%')";
    // Contact
    $where_contact .= " AND (contactentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number #LIKE '".$obm_q->escape($phone)."%'))";
  }

  // If an address indication has been specified, get it
  if (trim($address) != '') {
    // UserObm
    $where_userobm .= " AND (userobm_address1 #LIKE '%".$obm_q->escape($address)."%'
      OR userobm_address2 #LIKE '%".$obm_q->escape($address)."%'
      OR userobm_address3 #LIKE '%".$obm_q->escape($address)."%')";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     $where_userobm .= " AND userobm_description #LIKE '%".$obm_q->escape($desc)."%'";
  }

  // If a direction indication has been specified get it
  if (trim($direction) != '') {
    $where_userobm .= " AND userobm_direction #LIKE '%".$obm_q->escape($direction)."%'";
  }

  // If a service indication has been specified get it
  if (trim($service) != '') {
    $where_userobm .= " AND userobm_service #LIKE '%".$obm_q->escape($service)."%'";
  }

  // If a service indication has been specified get it
  if (trim($company) != '') {
    $where_userobm .= " AND userobm_company #LIKE '%".$obm_q->escape($company)."%'";
  }

  // If a town indication has been specified get it
  if (trim($town) != '') {
    $where_userobm .= " AND userobm_town #LIKE '%".$obm_q->escape($town)."%'";
  }

  // If a zipcode indication has been specified get it
  if (trim($zipcode) != '') {
    $where_userobm .= " AND userobm_zipcode = '".$obm_q->escape($zipcode)."'";
  }

  // If a cedex indication has been specified get it
  if (trim($cedex) != '') {
    $where_userobm .= " AND userobm_expresspostal = '".$obm_q->escape($cedex)."'";
  }

  // If a cedex indication has been specified get it
  if (trim($delegation) != '') {
    $where_userobm .= " AND userobm_delegation #LIKE '".$obm_q->escape($delegation)."%'";
  }

  // User defined data
  // UserObm
  $userdata_userobm = of_userdata_query_search('User', $people, 'UserObm');
  if ($userdata_userobm['where'] != '') {
    if (trim($where_userobm) != '') $where_userobm .= ' AND ';
    $where_userobm .= $userdata_userobm['where'];
    $join_userdata_userobm = $userdata_userobm['join'];
  }

  // Contact
  $userdata_contact = of_userdata_query_search('Contact', $people);
  if ($userdata['where'] != '') {
    $where_contact .= ' AND '. $userdata_contact['where'];
    $join_userdata_contact = $userdata_contact['join'];
  }

  // Where
  // UserObm
  if ((trim($where_userobm) != '') || (trim($where_filter_entity) != '')) {
    $whereq_userobm = " WHERE $where_userobm $where_filter_entity $multidomain_userobm";
  } else {
    $whereq_userobm = " WHERE userobm_archive = 0 $multidomain_userobm";
  }
  // Contact
  $whereq_contact = " WHERE $where_contact $multidomain_contact";

  $select_userobm = "SELECT distinct userobm_lastname,
    userobm_firstname,
    userobm_title,
    userobm_phone,
    userobm_phone2,
    userobm_mobile,
    userobm_fax,
    userobm_fax2,
    userobm_description,
    userobm_email,
    userobm_id,
    userobm_direction,
    userobm_service,
    userobm_zipcode,
    userobm_town,
    userobm_address1,
    userobm_address2,
    userobm_address3,
    domain_name,
    userobm_vacation_enable,
    userobm_vacation_datebegin,
    userobm_vacation_dateend,
    userobm_delegation,
    'user' as type
  FROM UserObm 
    INNER JOIN Domain ON userobm_domain_id = domain_id
    INNER JOIN UserEntity ON userentity_user_id = userobm_id
    $join_userdata_userobm";

  $select_contact = "select distinct contact_lastname as userobm_lastname,
    contact_firstname as userobm_firstname,
    contact_title as userobm_title,
    p1.phone_number as userobm_phone, 
    p2.phone_number as userobm_phone2,
    p3.phone_number as userobm_mobile, 
    p4.phone_number as userobm_fax, 
    p5.phone_number as userobm_fax2, 
    contact_comment,
    email_address as userobm_email,
    contact_id as userobm_id,
    domain_name,
    0 as userobm_vacation_enable,
    NOW() as userobm_vacation_datebegin,
    NOW() as userobm_vacation_dateend,
    '' as userobm_delegation,
    'contact' as type
  FROM Contact";

  $join_contact = "
    LEFT JOIN Domain ON contact_domain_id = domain_id
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id 
    LEFT JOIN Phone p1 ON p1.phone_entity_id = contactentity_entity_id AND p1.phone_label='HOME;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone p2 ON p2.phone_entity_id = contactentity_entity_id AND p2.phone_label='WORK;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone p3 ON p3.phone_entity_id = contactentity_entity_id AND p3.phone_label='CELL;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone p4 ON p4.phone_entity_id = contactentity_entity_id AND p4.phone_label='HOME;FAX;X-OBM-Ref1'
    LEFT JOIN Phone p5 ON p5.phone_entity_id = contactentity_entity_id AND p5.phone_label='WORK;FAX;X-OBM-Ref1'
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label LIKE 'INTERNET;X-OBM-Ref1'
    $join_userdata_contact";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'userobm_lastname';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query_userobm = "$select_userobm $whereq_userobm";
  if ($show_contact) {
    $query_contact = "UNION $select_contact $join_contact $whereq_contact";
  }

  // Union
  $query = "$query_userobm $query_contact $orderq $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm INNER JOIN Domain ON domain_id = userobm_domain_id INNER JOIN UserEntity ON userentity_user_id = userobm_id $join_userdata_userobm $whereq_userobm");
    if ($show_contact) {
      $count += get_query_count("SELECT count(distinct contact_id) FROM Contact $join_userdata_contact $join_contact $whereq_contact");
    }
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_people_search()');
    $obm_q->xquery($query);
  }

  return $obm_q;
}


/**
 * Search users from a single field 
 * 
 * @param mixed $people 
 * @access public
 * @return array of 'length' => DB people results nb, and 'datas' => DB User search query results
 */
function run_query_people_ext_search($people) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $people['pattern'];
  $filter_entity = $people['filter_entity'];
  $filter_pattern = $people['filter_pattern'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $multidomain = sql_multidomain('UserObm.userobm');
  $multidomain_contact = sql_multidomain('Contact');
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  if(isset($people['first_row'])) $first_row = $people['first_row'] - 1;
  else $first_row = 0;

  if ($filter_entity == 'calendar') {
    $join_filter_entity = ' INNER JOIN ('.OBM_Acl::getAclSubselect('calendarentity_calendar_id', 'calendar', null, $obm['uid'], $filter_pattern).') as Rights ON Rights.calendarentity_calendar_id = userobm_id';
  }
  // XXXXXX Migrate the access right to the Addressbook
  $f_contact_ids = run_query_people_allowed_access_contacts();
  if ((is_array($f_contact_ids)) && (count($f_contact_ids) > 0)) {
    $f_contact_ids = array_map(sql_parse_id,$f_contact_ids);
    $where_contact_acl = " OR contact_id IN (".implode(',',$f_contact_ids).")";
  }
  //$where_contact_acl = "AND (contact_privacy = 0 $where_contact_acl)";
  $patterns = explode(' ', $pattern);

  foreach ($patterns as $pattern) {
    $userpattern .= "AND (userobm_lastname $like '".$obm_q->escape($pattern)."%' OR userobm_firstname $like '".$obm_q->escape($pattern)."%' OR userobm_email $like  '%".$obm_q->escape($pattern)."%' )";
    $contactpattern .= "AND (contact_lastname $like '".$obm_q->escape($pattern)."%' OR contact_firstname $like '".$obm_q->escape($pattern)."%' OR contactentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address $like '".$obm_q->escape($pattern)."%'))";
  }

  $query_userobm = "SELECT 
      userobm_id as id,
      userobm_lastname as lastname,
      userobm_firstname as firstname,
      userobm_email as email,
      'user' as type,
      userobm_hidden
    FROM UserObm
    $join_filter_entity
    WHERE 1 = 1 $userpattern 
      AND userobm_archive = 0
      $multidomain
      $limit ";
  $query_contact = "
    SELECT 
      contact_id as id,
      contact_lastname as lastname,
      contact_firstname as firstname,
      email_address as email,
      'contact' as type
    FROM Contact
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label LIKE 'INTERNET;X-OBM-Ref1'
    WHERE 1 = 1 $contactpattern 
      AND contact_archive = 0
      $where_contact_acl
      $multidomain_contact   
      $limit ";


  display_debug_msg($query, $cdg_sql, 'run_query_people_ext_search()');
  $rows =  array();
  $obm_q->query($query_userobm);
  while($obm_q->next_record()) {
    $rows[$obm_q->f("lastname")." ".$obm_q->f("firstname")] = $obm_q->Record;
  }
  $obm_q->query($query_contact);
  while($obm_q->next_record()) {
    $rows[$obm_q->f("lastname")." ".$obm_q->f("firstname")] = $obm_q->Record;
  }
  ksort($rows);
  $resultsCount = count($rows);

  return array('length' => $resultsCount, 'datas' => $rows);
}


///////////////////////////////////////////////////////////////////////////////
// Get all private contacts current user has access to
// return an array of contact ids
///////////////////////////////////////////////////////////////////////////////
function run_query_people_allowed_access_contacts() {
  global $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $uid = sql_parse_id($obm['uid']);
  $search_action = OBM_Acl::ACCESS;

  $allowed_contacts_query = "
      SELECT contactentity_contact_id AS id
      FROM UserEntity
      INNER JOIN EntityRight ON userentity_entity_id = entityright_consumer_id
      INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
      WHERE userentity_user_id = $uid
        AND entityright_$search_action = 1
      UNION ALL SELECT contactentity_contact_id AS id FROM UserEntity
      INNER JOIN of_usergroup ON userentity_user_id = of_usergroup_user_id
      INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id
      INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id
      INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
      WHERE userentity_user_id = $uid
        AND entityright_$search_action = 1
      UNION ALL SELECT contact_id as id FROM Contact 
      WHERE contact_usercreate = $uid    ";

  $f_contact_ids = array();
  display_debug_msg($allowed_contacts_query, $cdg_sql, 'run_query_contact_allowed_access()');
  $obm_q->query($allowed_contacts_query);
  while ($obm_q->next_record()) {
    $f_contact_ids[] = $obm_q->f('id');
  }

  return $f_contact_ids;
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution
// Parameters:
//   - $p_id : people id
///////////////////////////////////////////////////////////////////////////////
function run_query_people_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $multidomain = sql_multidomain('UserObm.userobm');
  $p_id = sql_parse_id($p_id, true);	
  $query = "SELECT UserObm.*
      FROM UserObm
         LEFT JOIN UserObm as c ON UserObm.userobm_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON UserObm.userobm_userupdate=u.userobm_id
         LEFT JOIN Domain on UserObm.userobm_domain_id=domain_id
         WHERE UserObm.userobm_id $p_id 
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_people_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// User insert query execution
// Parameters:
//   - $people[]   : entry values
// Returns:
//    $id : new user id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_people_insert($people){
  global $obm, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $cg_gid_user;

  $uid = sql_parse_id($obm['uid']);
  $login = "''";
  $status = 'INIT';
  $archive = '1';
  $delegation = $obm['delegation_target'];
  
  // infos
  $kind = $people['kind'];
  $lname = $people['lastname'];
  $fname = $people['firstname'];
  $title = $people['title'];
  $desc = $people['desc'];
 

  // Coord
  $phone = $people['phone'];
  $phone2 = $people['phone2'];
  $mobile = $people['mobile'];
  $fax = $people['fax'];
  $fax2 = $people['fax2'];
  $company = $people['company'];
  $direction = $people['direction'];
  $service = $people['service'];
  $ad1 = $people['ad1'];
  $ad2 = $people['ad2'];
  $ad3 = $people['ad3'];
  $zip = $people['zip'];
  $town = $people['town'];
  $cdx = $people['cdx'];
  $ctry = $people['country'];
  
  // User UID/GID
  $user_uid = get_first_user_free_uid();

  $domain_id = sql_parse_id($obm['domain_id']);

  // Connect to the BD
  $obm_q = new DB_OBM;

  $query = "
    INSERT INTO UserObm (
       userobm_domain_id,
       userobm_timecreate,
       userobm_timeupdate,
       userobm_usercreate,
       userobm_userupdate,
       userobm_delegation,
       userobm_login,
       userobm_archive,
       userobm_kind,
       userobm_lastname,
       userobm_firstname,
       userobm_status,
       userobm_title,
       userobm_company,
       userobm_direction,
       userobm_service,
       userobm_address1,
       userobm_address2,
       userobm_address3,
       userobm_zipcode,
       userobm_town,
       userobm_expresspostal,
       userobm_country_iso3166,
       userobm_phone,
       userobm_phone2,
       userobm_mobile,
       userobm_fax,
       userobm_fax2,
       userobm_description,
       userobm_uid
     ) VALUES (
       $domain_id,
       NOW(),
       NOW(),
       $uid,
       $uid,
       '".$obm_q->escape($delegation)."',
       $login,
       $archive,
       '".$obm_q->escape($kind)."',
       '".$obm_q->escape($lname)."',
       '".$obm_q->escape($fname)."',
       '".$obm_q->escape($status)."',
       '".$obm_q->escape($title)."',
       '".$obm_q->escape($company)."',
       '".$obm_q->escape($direction)."',
       '".$obm_q->escape($service)."',
       '".$obm_q->escape($ad1)."',
       '".$obm_q->escape($ad2)."',
       '".$obm_q->escape($ad3)."',
       '".$obm_q->escape($zip)."',
       '".$obm_q->escape($town)."',
       '".$obm_q->escape($cdx)."',
       '".$obm_q->escape($ctry)."',
       '".$obm_q->escape($phone)."',
       '".$obm_q->escape($phone2)."',
       '".$obm_q->escape($mobile)."',
       '".$obm_q->escape($fax)."',
       '".$obm_q->escape($fax2)."',
       '".$obm_q->escape($desc)."',
       '$user_uid'
      )
    ";

  display_debug_msg($query, $cdg_sql, 'run_query_people_insert()');

  $retour = $obm_q->query($query);

  if ($retour) {
    $id = $obm_q->lastid();
  } else {
    $id = false;
  }
  
  if ($id > 0) {
    $user['entity_id'] = $id;
    $entity_id = of_entity_insert('user', $id);    
    $entity_id = of_entity_insert('calendar', $id);    
    $entity_id = of_entity_insert('mailbox', $id);    
    OBM_Acl::setDefaultPublicRights('mailbox', $id);
    OBM_Acl::setDefaultPublicRights('calendar', $id);
    $ret = of_userdata_query_update('user', $id, $user);
    of_index_update('user', $id);
  }

  return $id;
}

///////////////////////////////////////////////////////////////////////////////
// User update query execution
// Parameters:
//   - $p_id     : people id
//   - $people[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_people_update($p_id, $people) {
  global $obm, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;

  // Connect to the BD
  $obm_q = new DB_OBM;

  // infos
  $kind = $people['kind'];
  $lname = $people['lastname'];
  $fname = $people['firstname'];
  $title = $people['title'];
  $desc = $people['desc'];
  $multidomain = sql_multidomain('userobm');
  
  // Coord
  $phone = $people['phone'];
  $phone2 = $people['phone2'];
  $mobile = $people['mobile'];
  $fax = $people['fax'];
  $fax2 = $people['fax2'];
  $company = $people['company'];
  $direction = $people['direction'];
  $service = $people['service'];
  $ad1 = $people['ad1'];
  $ad2 = $people['ad2'];
  $ad3 = $people['ad3'];
  $zip = $people['zip'];
  $town = $people['town'];
  $cdx = $people['cdx'];
  $ctry = $people['country'];

  // User UID/GID
  $userDesc = get_user_info($p_id);

  $id = sql_parse_id($p_id, true);
  $query = "UPDATE UserObm SET
    userobm_timeupdate='". date('Y-m-d H:i:s')."',
    userobm_userupdate=$obm[uid],
    $user_update_q
    userobm_kind='".$obm_q->escape($kind)."',
    userobm_lastname='".$obm_q->escape($lname)."',
    userobm_firstname='".$obm_q->escape($fname)."',
    userobm_title='".$obm_q->escape($title)."',
    userobm_phone='".$obm_q->escape($phone)."',
    userobm_phone2='".$obm_q->escape($phone2)."',
    userobm_mobile='".$obm_q->escape($mobile)."',
    userobm_fax='".$obm_q->escape($fax)."',
    userobm_fax2='".$obm_q->escape($fax2)."',
    userobm_company = '".$obm_q->escape($company)."',
    userobm_direction = '".$obm_q->escape($direction)."',
    userobm_service = '".$obm_q->escape($service)."',
    userobm_address1 = '".$obm_q->escape($ad1)."',
    userobm_address2 = '".$obm_q->escape($ad2)."',
    userobm_address3 = '".$obm_q->escape($ad3)."',
    userobm_zipcode = '".$obm_q->escape($zip)."',
    userobm_town = '".$obm_q->escape($town)."',
    userobm_expresspostal = '".$obm_q->escape($cdx)."',
    userobm_description='".$obm_q->escape($desc)."'
    WHERE userobm_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_people_update()');
  $retour = $obm_q->query($query);

  if ($retour) {
    of_index_update('user', $p_id);
    $ret = of_userdata_query_update('user', $p_id, $people);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of admin people
// Returns:
//   - number of admin people
///////////////////////////////////////////////////////////////////////////////
function get_nb_user_admin() {
  global $cdg_sql, $perms_admin;

  $query = "SELECT count(*) as nb
    FROM UserObm
    WHERE userobm_perms='$perms_admin'";

  display_debug_msg($query, $cdg_sql, 'get_nb_user_admin()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $resultat = $obm_q->f('nb');

  return $resultat;
}

///////////////////////////////////////////////////////////////////////////////
// Data checking and formatting
// Parameters:
//   - $p_id     : people id
//   - $people[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_user_data_form($p_id, &$people) {
  global $l_lname_error;
  global $err, $action;

  $action = $people['action'];
  $id = $people['user_id'];
  $lname = $people['lastname'];
  $fname = $people['firstname'];
  $title = $people['title'];
  $phone = $people['phone'];
  $phone2 = $people['phone2'];
  $mobile = $people['mobile'];
  $fax = $people['fax'];
  $fax2 = $people['fax2'];
  
  // MANDATORY: Lastname
  if (trim($lname) == '') {
    $err['msg'] = $l_lname_error.' : '. $lname;
    $err['field'] = 'lastname';
    return false;
  }

  return true;

}

///////////////////////////////////////////////////////////////////////////////
// User environment checking (same user exists ?)
// Parameters:
//   - $p_id   : user id
//   - $user[] : user's values
// Returns:
//   - User Database object with list of similar users
///////////////////////////////////////////////////////////////////////////////
function check_people_context($p_id, $user) {

  $login = $user['login'];
  $profile = $user['profile'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $phone = $user['phone'];
  $domain_id = $user['domain_id'];

  // if a user with same name exists, return false
  $co_q = run_query_user_check_user($p_id, $user);

  return $co_q;
}


///////////////////////////////////////////////////////////////////////////////
// User specific rights checking (eg : delegation)
// Parameters:
//   - $params[] : user values
// Returns:
//   - true if OK, else false
///////////////////////////////////////////////////////////////////////////////
function check_user_update_rights($params) {
  global $err, $cgp_use, $l_error_delegation;

  $id = $params['user_id'];

  if ($id > 0) {

    // Check if delegation is used
    if ($cgp_use['property']['delegation']) {
      $u = get_user_info($id);
      // Check delegation update right
      if (! of_delegation_check_update_rights($params, $u['delegation'])) {
        $err['msg'] .= $l_error_delegation;
        return false;
      }
    }
  }

  return true;
}

///////////////////////////////////////////////////////////////////////////////
// Return the user with similar names
// Parameters:
//   - $p_id  : user id
//   - $login : login
///////////////////////////////////////////////////////////////////////////////
function run_query_user_check_user($p_id, $user) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('userobm');
  $login = strtolower($user['login']);
  $domain_id = $user['domain_id'];
  if ($domain_id == '') $domain_id = $obm['domain_id'];
  $domain_id = sql_parse_id($domain_id, true);
  $where_multidomain = " AND userobm_domain_id $domain_id ";

  if ($p_id) {
    $p_id = sql_parse_id($p_id, true, true);
    $where_user = "AND userobm_id $p_id ";
  }

  $query = "SELECT distinct userobm_id, userobm_login
    FROM UserObm
    WHERE userobm_login $like '".$obm_q->escape($login)."%'
    $where_user $where_multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_user_check_user()');
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Return the user waiting validation
// Parameters:
//   - $user[]  : user values 
///////////////////////////////////////////////////////////////////////////////
function run_query_people_search_wait($user){
  global $obm;

  $obm_q = new DB_OBM;
  $status = 'INIT';
  $archive = 1;
  $delegation = $obm['delegation_target'];
  $domain_id = $obm['domain_id'];
  if ($domain_id == '') $domain_id = $obm['domain_id'];
  $domain_id = sql_parse_id($domain_id, true);

  $where = "userobm_status = '$status' 
    AND userobm_archive = $archive
    AND userobm_delegation LIKE '".$obm_q->escape($delegation)."%'";
  
  $query = "SELECT userobm_id, userobm_firstname, userobm_lastname, 'user' AS type 
    FROM UserObm
    WHERE $where";

  display_debug_msg($query, $cdg_sql, 'run_query_user_check_user()');
  $obm_q->query($query);

  return $obm_q;
}

///////////////////////////////////////////////////////////////////////////////
// Check people who wait to be valid
// Parameters:
//   - $people[] : user search criteria
// Returns : true if some people wait or false
///////////////////////////////////////////////////////////////////////////////
function check_people_wait($people) {
  global $obm;

  $rep = false;
  
  $people['archive'] = 1;
  $people['status'] = "INIT";
  $people['delegation'] = $obm['delegation_target'];

  $obm_q = run_query_people_search_wait($people);  
  
  if ($obm_q->num_rows() > 0){
    $rep = true;
  }

  return $rep;
}
///////////////////////////////////////////////////////////////////////////////
// User Groups query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_people_user_group($p_id) {
  global $cdg_sql, $obm;

  $p_id = sql_parse_id($p_id, true);
  $query = "SELECT *
    FROM UGroup
      LEFT JOIN UserObmGroup ON group_id = userobmgroup_group_id
    WHERE userobmgroup_userobm_id $p_id 
      AND (group_privacy=0 OR group_usercreate=$obm[uid])";

  display_debug_msg($query, $cdg_sql, 'run_query_people_user_group()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $obm_q;
}

/**
 * run_query_people_get_admin 
 * 
 * @access public
 * @return void
 */
function run_query_people_get_admin($delegation) {
  if($delegation == '') return array(); 
  $query = "SELECT profile_name, userperms.profilemodule_right as userperms, defaultperms.profilemodule_right as defaultperms FROM Profile 
              INNER JOIN ProfileModule defaultperms ON defaultperms.profilemodule_profile_id = profile_id AND defaultperms.profilemodule_module_name = 'default'
              LEFT JOIN ProfileModule userperms ON userperms.profilemodule_profile_id = profile_id AND userperms.profilemodule_module_name = 'user'
            WHERE 1=1 $multidomain"; 
  display_debug_msg($query, $cdg_sql, 'run_query_people_get_admin(0)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $profiles = array();
  while($obm_q->next_record()) {
    if($obm_q->f('userperms') !== NULL) {
      if((intval($obm_q->f('userperms')) & $GLOBALS['cright_write_admin']) == $GLOBALS['cright_write_admin'] ) {
        echo "HERE";
        $profiles[] = $obm_q->f('profile_name');
      }
    } else {
      if((intval($obm_q->f('defaultperms')) & $GLOBALS['cright_write_admin']) == $GLOBALS['cright_write_admin'] ) {
        $profiles[] = $obm_q->f('profile_name');
      }
    }
  }
  $admins = array();
  if(count($profiles) > 0) {
    $query = "SELECT userobm_id, userobm_delegation_target FROM UserObm 
      WHERE '$delegation' #LIKE #CONCAT(userobm_delegation_target ,'%') AND userobm_perms IN ('".implode("','", $profiles)."') $multidomain
      AND userobm_archive=0 ORDER BY userobm_delegation_target DESC";
    $obm_q->xquery($query);
    while($obm_q->next_record()) {
      if(!$minDelegation) $minDelegation = strtolower($obm_q->f('userobm_delegation_target'));
      elseif (strtolower($obm_q->f('userobm_delegation_target')) != $minDelegation) break;
      if($minDelegation == '') return array();
      $admins[] = $obm_q->f('userobm_id');
    }
  }
  return $admins;
}
?>
