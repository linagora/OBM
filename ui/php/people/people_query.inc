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
//   - $user[] : user search criteria
//     keys used  : name, perms
//   - $sql_order_field : infos for order clause
//   - $sql_order_dir : direction for order clause (asc, desc)
//   - $filter_entity : if and which filter_entity to set (eg calendar readable)
///////////////////////////////////////////////////////////////////////////////
function run_query_user_search($user) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;
  global $cgp_archive_only;

  $sql_order_dir = $user['sql_order_dir'];
  $sql_order_field = $user['sql_order_field'];

  $filter_entity = $user['filter_entity'];
  $filter_pattern = $user['filter_pattern'];
  $lname = sql_search_text_parse($user['lastname']);
  $fname = sql_search_text_parse($user['firstname']);
  $email = sql_search_text_parse($user['email']);
  $desc = sql_search_text_parse($user['desc']);
  $phone = $user['phone'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $datebegin = sql_date_format($db_type, 'userobm_datebegin', 'datebegin');
  $dateexp = sql_date_format($db_type, 'userobm_account_dateexp', 'dateexp');
  $diff = "TIMESTAMPDIFF(DAY,now(),userobm_account_dateexp)";
  $lastaccess = sql_date_format($db_type, 'userobm_timelastaccess', 'timelastaccess');
  $multidomain = sql_multidomain('userobm');

  if ($filter_entity == 'calendar') {
    $users = of_right_entity_for_user('calendar', $obm['uid'], $filter_pattern,'','userobm');
    if ((is_array($users['ids'])) && (count($users['ids']) > 0)) {
      $where_filter_entity = " AND (userobm_id IN ($obm[uid], ";
      foreach($users['ids'] as $u_id) {
	$where_filter_entity .= "$coma'$u_id'";
	$coma = ',';
      }
      $where_filter_entity .= '))';
    } else {
      // If no users have given $uid with read right, return empty set
      $where_filter_entity = ' AND userobm_id < 0';
    }
  }

  $where = '1=1';

  // If a lastname indication has been specified, get it
  if (trim($lname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_lastname $like '$lname%'";
  }

  // If a firstname indication has been specified, get it
  if (trim($fname) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_firstname $like '$fname%'";
  }

  // If an email indication has been specified, get it
  if (trim($email) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_email like '%$email%'";
  }

  // If a phone number indication has been specified, get it
  if (trim($phone) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " (userobm_phone like '%$phone%'
       OR userobm_phone2 like '%$phone%'
       OR userobm_mobile like '%$phone%')";
  }

  // If a description indication has been specified, get it
  if (trim($desc) != '') {
     if (trim($where) != '') $where .= ' AND';
     $where .= " userobm_description like '%$desc%'";
  }

  // User defined data
  $userdata = of_userdata_query_search('user', $user, 'UserObm');
  if ($userdata['where'] != '') {
    if (trim($where) != '') $where .= ' AND ';
    $where .= $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  if ((trim($where) != '') || (trim($where_filter_entity) != '')) {
    $whereq = " WHERE $where $where_filter_entity $multidomain";
  } else {
    $whereq = " WHERE 1=1 $multidomain";
  }

  $select = "SELECT distinct UserObm.*,
      userobm_id as id,
      $lastaccess
    FROM UserObm 
    LEFT JOIN Domain ON userobm_domain_id = domain_id
    $join_userdata";


  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'userobm_lastname';

  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "$select
    $whereq
    $orderq
    $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(distinct userobm_id) FROM UserObm $join_grp $join_userdata $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_user_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


/**
 * Search users from a single field 
 * 
 * @param mixed $user 
 * @access public
 * @return array of 'length' => DB user results nb, and 'datas' => DB User search query results
 */
function run_query_user_ext_search($user) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $user['pattern'];
  $filter_entity = $user['filter_entity'];
  $filter_pattern = $user['filter_pattern'];
  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('UserObm.userobm');

  $limit = $user['limit'];
  if(isset($user['first_row'])) $first_row = $user['first_row'] - 1;
  else $first_row = 0;

  if ($filter_entity == 'calendar') {
    $users = of_right_entity_for_user('calendar', $obm['uid'], $filter_pattern,'','userobm');
    if ((is_array($users['ids'])) && (count($users['ids']) > 0)) {
      $where_filter_entity = " AND (userobm_id IN ($obm[uid], ";
      foreach($users['ids'] as $u_id) {
	$where_filter_entity .= "$coma'$u_id'";
	$coma = ',';
      }
      $where_filter_entity .= '))';
    } else {
      // If no users have given $uid with read right, return empty set
      $where_filter_entity = ' AND userobm_id < 0';
    }
  }

  $query = "SELECT 
      count(*) as resultscount
    FROM UserObm
    WHERE (
      userobm_lastname LIKE '$pattern%'
      OR userobm_firstname LIKE '$pattern%' 
      OR userobm_email LIKE '$pattern%' )
      AND userobm_archive = 0
      $multidomain
      $where_filter_entity";

  display_debug_msg($query, $cdg_sql, 'run_query_user_ext_search()');
  $obm_q->query($query);
  $obm_q->next_record();
  $resultsCount = $obm_q->f('resultscount');
  $query = "SELECT 
      userobm_id,
      userobm_lastname,
      userobm_firstname,
      userobm_email
    FROM UserObm
    WHERE (
      userobm_lastname LIKE '$pattern%' 
      OR userobm_firstname LIKE '$pattern%' 
      OR userobm_email LIKE  '$pattern%' )
      AND userobm_archive = 0
      $multidomain
      $where_filter_entity
    ORDER BY  userobm_lastname, userobm_firstname
    LIMIT $limit OFFSET $first_row";
  display_debug_msg($query, $cdg_sql, 'run_query_user_ext_search()');
  $obm_q->query($query);

  return array('length' => $resultsCount, 'datas' => $obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// User detail query execution
// Parameters:
//   - $p_id : user id
///////////////////////////////////////////////////////////////////////////////
function run_query_user_detail($p_id) {
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

  display_debug_msg($query, $cdg_sql, 'run_query_user_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// User update query execution
// Parameters:
//   - $p_id     : user id
//   - $user[]   : entry values
//     keys used : login, passwd, perms, archive, *name, email
///////////////////////////////////////////////////////////////////////////////
function run_query_user_update($p_id, $user) {
  global $obm, $cdg_sql, $cgp_use, $password_encryption, $perms_admin;
  global $cg_gid_smb_admin, $cg_gid_smb_user, $c_first_gid_admin, $c_first_gid_user;

  // Connect to the BD
  $obm_q = new DB_OBM;

  // infos
  $kind = $user['kind'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $desc = $user['desc'];
  $multidomain = sql_multidomain('userobm');
  
  // Coord
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  $company = $user['company'];
  $direction = $user['direction'];
  $service = $user['service'];
  $ad1 = $user['ad1'];
  $ad2 = $user['ad2'];
  $ad3 = $user['ad3'];
  $zip = $user['zip'];
  $town = $user['town'];
  $cdx = $user['cdx'];
  $ctry = $user['country'];

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

  display_debug_msg($query, $cdg_sql, 'run_query_user_update()');
  $retour = $obm_q->query($query);

  if ($retour) {
    $ret = of_userdata_query_update('user', $p_id, $user);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of admin user
// Returns:
//   - number of admin user
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
//   - $p_id     : user id
//   - $user[]   : values checked
//     keys used : name, passwd, email
///////////////////////////////////////////////////////////////////////////////
function check_user_data_form($p_id, &$user) {
  global $l_lname_error;
  global $err, $action;


  $action = $user['action'];
  $id = $user['user_id'];
  $lname = $user['lastname'];
  $fname = $user['firstname'];
  $title = $user['title'];
  $phone = $user['phone'];
  $phone2 = $user['phone2'];
  $mobile = $user['mobile'];
  $fax = $user['fax'];
  $fax2 = $user['fax2'];
  
  // MANDATORY: Lastname
  if (trim($lname) == '') {
    $err['msg'] = $l_lname_error.' : '. $lname;
    $err['field'] = 'lastname';
    return false;
  }

  return true;

}

?>
