<?php
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
  $adress = $people['adress'];
  $service = $people['service'];
  $zipcode = $people['zipcode'];
  $cedex = $people['cedex'];
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

  $where_userobm = '1=1';
  $where_contact = '1=1';

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
  } else {
    if (trim($status) != '') {
      $where_userobm .= " AND userobm_status = '$status'";
    }
  }

  // If a lastname indication has been specified, get it
  if (trim($lname) != '') {
     // UserObm
     $where_userobm .= " AND userobm_lastname #LIKE '$lname%'";
     // Contact
     $where_contact .= " AND (contact_lastname #LIKE '$lname%' or contact_aka #LIKE '$lname%')";
  }

  // If a firstname indication has been specified, get it
  if (trim($fname) != '') {
     // UserObm
     $where_userobm .= " AND userobm_firstname #LIKE '$fname%'";
     // Contact
     $where_contact .= " AND contact_lastname #LIKE '$fname%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != '') {
    // UserObm
    $where_userobm .= " AND userobm_email #LIKE '%$email%'";
    // Contact
    $where_contact .= " AND (contactentity_entity_id IN (SELECT email_entity_id FROM Email WHERE email_address #LIKE '$email%'))";
  }

  // If a phone number indication has been specified, get it
  if (trim($phone) != '') {
    // UserObm
    $where_userobm .= " AND (userobm_phone #LIKE '%$phone%'
      OR userobm_phone2 #LIKE '%$phone%'
      OR userobm_mobile #LIKE '%$phone%')";
    // Contact
    $where_contact .= " AND (contactentity_entity_id IN (SELECT phone_entity_id FROM Phone WHERE phone_number #LIKE '$phone%'))";
  }

  // If a fax number indication has been specified, get it
  if (trim($fax) != '') {
    $where_userobm .= " AND (userobm_fax #LIKE '%$fax%'
      OR userobm_fax2 #LIKE '%$fax%')";
  }

  // If an adress indication has been specified, get it
  if (trim($adress) != '') {
    // UserObm
    $where_userobm .= " AND (userobm_address1 #LIKE '%$adress%'
      OR userobm_address2 #LIKE '%$adress%'
      OR userobm_address3 #LIKE '%$adress%')";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     $where_userobm .= " AND userobm_description #LIKE '%$desc%'";
  }

  // If a direction indication has been specified get it
  if (trim($direction) != '') {
    $where_userobm .= " AND userobm_direction #LIKE '%$direction%'";
  }

  // If a service indication has been specified get it
  if (trim($service) != '') {
    $where_userobm .= " AND userobm_service #LIKE '%$service%'";
  }

  // If a service indication has been specified get it
  if (trim($company) != '') {
    $where_userobm .= " AND userobm_company #LIKE '%$company%'";
  }

  // If a town indication has been specified get it
  if (trim($town) != '') {
    $where_userobm .= " AND userobm_town #LIKE '%$town%'";
  }

  // If a zipcode indication has been specified get it
  if (trim($zipcode) != '') {
    $where_userobm .= " AND userobm_zipcode = $zipcode";
  }

  // If a cedex indication has been specified get it
  if (trim($cedex) != '') {
    $where_userobm .= " AND userobm_expresspostal = $cedex";
  }

  // User defined data
  // UserObm
  $userdata_userobm = of_userdata_query_search('people', $people, 'UserObm');
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
    $whereq_userobm = " WHERE 1=1 $multidomain_userobm";
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
    domain_name,
    'user' as type
  FROM UserObm 
    LEFT JOIN Domain ON userobm_domain_id = domain_id
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
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label LIKE 'PREF;INTERNET;X-OBM-Ref1%'
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
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm $join_userdata_userobm $whereq_userobm");
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

  $limit = $people['limit'];
  if(isset($people['first_row'])) $first_row = $people['first_row'] - 1;
  else $first_row = 0;

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
  }

  $where_contact_acl = "AND (";
  $where_contact_acl.= "contact_privacy = 0";
  $f_contact_ids = run_query_people_allowed_access_contacts();
  if ((is_array($f_contact_ids)) && (count($f_contact_ids) > 0)) {
    $f_contact_ids = array_map(sql_parse_id,$f_contact_ids);
    $where_contact_acl .= " OR contact_id IN (".implode(',',$f_contact_ids).")";
  }
  $where_contact_acl.= ")";

  $query_userobm = "SELECT 
      userobm_id as id,
      userobm_lastname as lastname,
      userobm_firstname as firstname,
      userobm_email as email,
      'user' as type
    FROM UserObm
    WHERE (
      userobm_lastname $like '$pattern%' 
      OR userobm_firstname $like '$pattern%' 
      OR userobm_email $like  '$pattern%' )
      AND userobm_archive = 0
      $multidomain
      $where_filter_entity";

  $query_contact = "SELECT 
      contact_id as id,
      contact_lastname as lastname,
      contact_firstname as firstname,
      email_address as email,
      'contact' as type
    FROM Contact
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label like 'PREF;%'
    WHERE (
      contact_lastname $like '$pattern%'
      OR contact_firstname $like '$pattern%'
      OR contactentity_entity_id IN (
        SELECT email_entity_id FROM Email WHERE email_address $like '$pattern%'
      ))
      AND contact_archive = 0
      $multidomain_contact
      $where_contact_acl";


  $query = "$query_userobm UNION $query_contact ORDER BY lastname, firstname
    LIMIT $limit OFFSET $first_row";

  display_debug_msg($query, $cdg_sql, 'run_query_people_ext_search()');
  $obm_q->query($query);
  $resultsCount = $obm_q->num_rows();

  return array('length' => $resultsCount, 'datas' => $obm_q);
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
    SELECT contact_id AS id
    FROM UserObm u1
    INNER JOIN UserEntity ON u1.userobm_id = userentity_user_id
    INNER JOIN EntityRight ON userentity_entity_id = entityright_consumer_id
    INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
    INNER JOIN Contact ON contactentity_contact_id = contact_id
    WHERE u1.userobm_id = $uid
      AND entityright_$search_action = 1
      AND contact_privacy = 1
    UNION SELECT contact_id AS id
    FROM EntityRight INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
    INNER JOIN Contact ON contactentity_contact_id = contact_id
    WHERE entityright_consumer_id IS NULL
      AND entityright_$search_action = 1
      AND contact_privacy = 1
    UNION SELECT contact_id AS id FROM UserObm u1
    INNER JOIN UserEntity ON u1.userobm_id = userentity_user_id
    INNER JOIN of_usergroup ON userobm_id = of_usergroup_user_id
    INNER JOIN GroupEntity ON of_usergroup_group_id = groupentity_group_id
    INNER JOIN EntityRight ON groupentity_entity_id = entityright_consumer_id
    INNER JOIN ContactEntity ON contactentity_entity_id = entityright_entity_id
    INNER JOIN Contact ON contactentity_contact_id = contact_id
    WHERE u1.userobm_id = $uid
      AND entityright_$search_action = 1
      AND contact_privacy = 1";

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
       '$delegation',
       $login,
       $archive,
       '$kind',
       '$lname',
       '$fname',
       '$status',
       '$title',
       '$company',
       '$direction',
       '$service',
       '$ad1',
       '$ad2',
       '$ad3',
       '$zip',
       '$town',
       '$cdx',
       '$ctry',
       '$phone',
       '$phone2',
       '$mobile',
       '$fax',
       '$fax2',
       '$desc',
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
    userobm_kind='$kind',
    userobm_lastname='$lname',
    userobm_firstname='$fname',
    userobm_title='$title',
    userobm_phone='$phone',
    userobm_phone2='$phone2',
    userobm_mobile='$mobile',
    userobm_fax='$fax',
    userobm_fax2='$fax2',
    userobm_company = '$company',
    userobm_direction = '$direction',
    userobm_service = '$service',
    userobm_address1 = '$ad1',
    userobm_address2 = '$ad2',
    userobm_address3 = '$ad3',
    userobm_zipcode = '$zip',
    userobm_town = '$town',
    userobm_expresspostal = '$cdx',
    userobm_description='$desc'
    WHERE userobm_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_people_update()');
  $retour = $obm_q->query($query);

  if ($retour) {
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
    WHERE userobm_login $like '$login%'
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

  $status = 'INIT';
  $archive = 1;
  $delegation = $obm['delegation_target'];
  $domain_id = $obm['domain_id'];
  if ($domain_id == '') $domain_id = $obm['domain_id'];
  $domain_id = sql_parse_id($domain_id, true);

  $where = "userobm_status = '$status' 
    AND userobm_archive = $archive
    AND userobm_delegation LIKE '$delegation%'";
  
  $query = "SELECT userobm_id, userobm_firstname, userobm_lastname, 'user' AS type 
    FROM UserObm
    WHERE $where";

  $obm_q = new DB_OBM;
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
?>
