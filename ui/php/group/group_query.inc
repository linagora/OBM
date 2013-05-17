<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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
// OBM - File : group_query.inc                                              //
//     - Desc : group query File                                             //
// 2003-08-22 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Group Search query execution
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_group_search($group) {
  global $c_all, $cdg_sql, $ctu_sql_limit;
  global $c_public, $c_private;

  $name = sql_search_text_parse($group['name']);
  $domain_id = $group['domain_id'];
  $delegation = sql_search_text_parse($group['delegation']);
  $email = sql_search_text_parse($group['email']);
  $privacy = $group['privacy'];
  $user = sql_search_text_parse($group['user']);
  $desc = sql_search_text_parse($group['desc']);
  $samba = ($group['samba']);

  $sql_order_field = $group['sql_order_field'];
  $sql_order_dir = $group['sql_order_dir'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $timeupdate = sql_date_format($db_type,'UGroup.group_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'UGroup.group_timecreate', 'timecreate');
  $multidomain = sql_multidomain('group');

  $where = "group_name $like '".$obm_q->escape($name)."%'";
  if ($user != '') {
    $join_user = "
      LEFT JOIN of_usergroup as D ON D.of_usergroup_group_id=group_id
      LEFT JOIN UserObm as C ON D.of_usergroup_user_id = C.userobm_id";
    $where .= " AND C.userobm_lastname $like '".$obm_q->escape($user)."%' AND C.userobm_archive = 0";
  }
  // If a user domain indication has been specified, get it
  if (($domain_id != $c_all) && ($domain_id != '')) {
     $domain_id = sql_parse_id($domain_id, true);
     $where .= " AND group_domain_id $domain_id ";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND group_delegation $like '".$obm_q->escape($delegation)."%'";
  }
  if ($email != '') {
    list($left,$right) = split('@',$email);
    if ($right === null){
      $left = $obm_q->escape($left).'%';
      $right = $obm_q->escape($left);
      $condition = 'OR';
    } else {
      $condition = 'AND';
    }
    if (!$left) {
      $left = '%';
    }
    $right = $obm_q->escape($right).'%';
    $where .= " AND
      group_email != '' AND (( group_email #LIKE '$left') $condition ( domain_name #LIKE '$right' OR domain_alias #LIKE '$right' OR domain_alias #LIKE '$right\r\n%' OR domain_alias #LIKE '%\r\n$right\r\n%' OR domain_alias #LIKE '%\r\n$right' )) ";    
  }
  if ($privacy == $c_public) {
    $where .= ' AND group_privacy = 0';
  } elseif ($privacy == $c_private) {
    $where .= ' AND group_privacy = 1';
  }
  if (trim($desc) != '') {
    $where .= " AND group_desc $like '%".$obm_q->escape($desc)."%'";
  }
  if ($samba == 1) {
    $where .= " AND group_samba = $samba";
  }
  // only the one which are allowed (ie. publics )
  $where .=  ' AND ' .sql_obm_entity_privacy('group');

  // User defined data
  $userdata = of_userdata_query_search('Group', $group);
  if ($userdata['where'] != '') {
    $where .= ' AND '. $userdata['where'];
    $join_userdata = $userdata['join'];
  }

  $whereq = "WHERE $where $multidomain";

  // ORDER construction
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'group_name';
  $orderq .= " ORDER BY $order $sql_order_dir";

  $query = "SELECT
    group_id as id,
    group_id,
    group_domain_id,
    group_system,
    group_privacy,
    group_local,
    group_ext_id,
    group_samba,
    group_gid,
    group_delegation,
    group_manager_id,
    group_name,
    group_desc,
    group_email,
    $timecreate,
    $timeupdate,
    A.userobm_login as usercreate,
    B.userobm_login as userupdate,
    domain_label,
    domain_name,
    count(usergroup.of_usergroup_user_id) as group_nb_user
  FROM UGroup
    INNER JOIN Domain ON domain_id = group_domain_id
    $join_userdata
    LEFT JOIN of_usergroup usergroup ON of_usergroup_group_id=group_id
    LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
    LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
    $join_user
  $whereq
  GROUP BY group_id,
    group_domain_id,
    group_system,
    group_privacy,
    group_local,
    group_ext_id,
    group_samba,
    group_gid,
    group_delegation,
    group_manager_id,
    group_name,
    group_desc,
    group_email,
    timecreate,
    timeupdate,
    usercreate,
    userupdate,
    domain_label,
    domain_name
  $orderq
  $limit";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM UGroup INNER JOIN Domain ON domain_id = group_domain_id $join_userdata $join_user $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'run_query_group_search()');
    $obm_q->xquery($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Possible children Group Search query execution
// Parameters :
//   - $group[]      : list search criteria
//     keys used     : name, user
///////////////////////////////////////////////////////////////////////////////
function run_query_group_search_possible_children($group) {
  global $cdg_sql;

  $id = $group['ext_id'];
  $name = $group['name'];
  $delegation = sql_search_text_parse($group['delegation']);
  $email = $group['email'];
  $user = $group['user'];
  $sql_order_field = $group['sql_order_field'];
  $sql_order_dir = $group['sql_order_dir'];

  $parents = get_group_parents($id);

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $timeupdate = sql_date_format($db_type,'UGroup.group_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'UGroup.group_timecreate', 'timecreate');
  $multidomain = sql_multidomain('group');

  // WHERE Clause

  // only the one which are allowed (ie. publics )
  $where .=  ' AND ' .sql_obm_entity_privacy('group');
  if ($user != '') {
    $where .= " AND C.userobm_lastname $like '".$obm_q->escape($user)."%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND group_delegation $like '".$obm_q->escape($delegation)."%'";
  }
  if ($email != '') {
    $where .= " AND group_email $like '%".$obm_q->escape($email)."%'";
  }
  while (list ($key, $g_id) = each($parents)) {
    $g_id = sql_parse_id($g_id, true, true);
    $where .= " AND group_id $g_id ";
  }
  $id = sql_parse_id($id, true, true);
  $query = "SELECT DISTINCT UGroup.*, group_id as id,
  $timecreate,
  $timeupdate,
  A.userobm_login as usercreate,
  B.userobm_login as userupdate
  FROM UGroup
  LEFT JOIN UserObmGroup ON group_id=UserObmGroup_group_id
  LEFT JOIN UserObm as C ON UserObmGroup.userobmgroup_userobm_id=C.userobm_id
  LEFT JOIN UserObm as A ON UGroup.group_usercreate=A.userobm_id
  LEFT JOIN UserObm as B ON UGroup.group_userupdate=B.userobm_id
  WHERE group_name $like '$name%'
  AND group_id $id
  $where
  $multidomain";

  // ORDER construction

  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'group_name';
  $query .= " ORDER BY $order $sql_order_dir";

  display_debug_msg($query, $cdg_sql, 'run_query_group_search_possible_children()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Group detail query execution
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $id = sql_parse_id($id, true);
  $timeupdate = sql_date_format($db_type,'UGroup.group_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type,'UGroup.group_timecreate', 'timecreate');
  $multidomain = sql_multidomain('group');

  $query = "SELECT *,
  group_privacy as privacy,
  $timecreate,
  $timeupdate,
  c.userobm_login as usercreate,
  u.userobm_login as userupdate,
  m.userobm_lastname,
  m.userobm_firstname
  FROM UGroup
  LEFT JOIN UserObm as c ON group_usercreate=c.userobm_id
  LEFT JOIN UserObm as u ON group_userupdate=u.userobm_id
  LEFT JOIN UserObm as m ON group_manager_id=m.userobm_id
  WHERE group_id $id
  $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_group_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : group insertion
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_group_insert($group) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $id = $group['group_id'];
  $system = sql_parse_int($group['system']);
  $samba = sql_parse_int($group['samba']);
  $name = $group['name'];
  $desc = $group['desc'];
  $email = $group['email'];
  $priv = sql_parse_int($group['privacy']);
  $domain_id = sql_parse_id($obm['domain_id']);
  if ($priv == 1) {
    $samba = 0;
    $email = '';
    $gid = 'NULL';
  } else {
    $gid = sql_parse_int(get_first_group_free_gid());
  }

  // Delegation handling
  $q_delegation = of_delegation_query_insert_clauses('group_delegation', stripslashes($group['delegation']));

  $query = "INSERT INTO UGroup (
  group_timeupdate,
  group_timecreate,
  group_userupdate,
  group_usercreate,
  group_domain_id,
  group_system,
  group_samba,
  group_privacy,
  group_gid,
  group_name
  $q_delegation[field],
  group_desc,
  group_email
  ) VALUES (
  null,
  NOW(),
  null,
  $obm[uid],
  $domain_id,
  $system,
  $samba,
  $priv,
  $gid,
  '".$obm_q->escape($name)."'
  $q_delegation[value],
  '".$obm_q->escape($desc)."',
  '".$obm_q->escape($email)."'
  )";

  display_debug_msg($query, $cdg_sql, 'run_query_group_insert()');
  $retour = $obm_q->query($query);

  $g_id = run_query_group_id($group);

  if ($g_id > 0) {
    $entity_id = of_entity_insert($GLOBALS['module'], $g_id);  
    $ret = of_userdata_query_update('group', $g_id, $group);
  }

  return $g_id;
}


///////////////////////////////////////////////////////////////////////////////
// Group ID : Get the id of the group inserted
// Parameters:
//   - $group[] : group hash info : keys used : all
// Returns:
//   group Id if found else false
///////////////////////////////////////////////////////////////////////////////
function run_query_group_id($group) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $system = sql_parse_int($group['system']);
  $name = $group['name'];
  $desc = $group['desc'];
  $email = $group['email'];
  $priv = sql_parse_int($group['privacy']);
  $multidomain = sql_multidomain('group');

  $query = "SELECT MAX(group_id) as  group_id
  FROM UGroup
  WHERE group_system=$system
  AND group_name='".$obm_q->escape($name)."'
  AND group_privacy=$priv
  AND group_desc='".$obm_q->escape($desc)."'
  AND group_email='".$obm_q->escape($email)."'
  $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_group_id()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    $obm_q->next_record();
    $id = $obm_q->f('group_id');
  } else {
    $id = false;
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Group Update query execution
// Parameters:
//   - $group[] : group hash info : keys used : all
///////////////////////////////////////////////////////////////////////////////
function run_query_group_update($group) {
  global $obm, $cdg_sql;
  
  $obm_q = new DB_OBM;
  $id = sql_parse_id($group['group_id'], true);
  $name = $group['name'];
  $samba = ($group['samba'] == 1) ? 1 : 0;
  $desc = $group['desc'];
  $email = $group['email'];
  $manager = sql_parse_id($group['manager']);
  $priv = sql_parse_int($group['privacy']);
  $multidomain = sql_multidomain('group');
  if ($priv == 1) {
    $samba = 0;
    $email = '';
  }

  // Delegation handling
  $q_delegation = of_delegation_query_update_clause('group_delegation', stripslashes($group['delegation']));

  $query = "UPDATE UGroup SET
    group_timeupdate=NOW(),
    group_userupdate=$obm[uid],
    group_samba=$samba,
    group_privacy=$priv,
    group_name='".$obm_q->escape($name)."',
    group_desc='".$obm_q->escape($desc)."',
    group_email='".$obm_q->escape($email)."',
    group_manager_id=$manager
    $q_delegation
  WHERE group_id $id
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_group_update()');
  $retour = $obm_q->query($query);

  if ($id > 0) {
    $ret = of_userdata_query_update('group', $group['group_id'], $group);
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Deletion query execution
// Parameters:
//   - $p_id : group id
///////////////////////////////////////////////////////////////////////////////
function run_query_group_delete($p_id) {
  global $cdg_sql, $obm;

  $ret = of_userdata_query_delete('group', $p_id);

  $obm_q = new DB_OBM;
  // Delete the Group
  $multidomain = sql_multidomain('group');
  of_entity_delete($GLOBALS['module'], $p_id);
  $query = "DELETE FROM UGroup WHERE group_id=$p_id $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_group_delete(3)');
  $retour = $obm_q->query($query);
  $id = sql_parse_id($p_id, true);
  // If the Group exists in Prod table AND is public, Insert it in Deleted table
  $query = "SELECT group_id, group_delegation, group_domain_id FROM P_UGroup
  WHERE group_id $id AND group_privacy=0";
  display_debug_msg($query, $cdg_sql, 'run_query_group_delete(auto)');
  $obm_q->query($query);
  $obm_q->next_record();
  $delegation = $obm_q->f('group_delegation');
  $domain_id = $obm_q->f('group_domain_id');

  if ($obm_q->num_rows() > 0) {
    $uid = sql_parse_id($obm['uid']);
    $domain_id = sql_parse_id($domain_id);
    $id = sql_parse_id($p_id);

    $query = "INSERT INTO Deleted (
    deleted_domain_id,
    deleted_user_id,
    deleted_delegation,
    deleted_table,
    deleted_entity_id,
    deleted_timestamp)
    VALUES (
    $domain_id,
    $uid,
    '".$obm_q->escape($delegation)."',
    'UGroup',
    $p_id,
    NOW())";
    display_debug_msg($query, $cdg_sql, 'run_query_group_delete(auto)');
    $retour = $obm_q->query($query);
  }

  return $retour;
}


/**
 * Query execution : UserObmGroup conditionnal insertion
 *
 * @param mixed $group[] : group hash info : keys used : id, user_nb, userX
 * @result number of users inserted
 **/
function run_query_group_contactgroup_insert($group) {
  global $obm, $cdg_sql;

  $id = $group['group_id'];
  $sel_id = sql_parse_id($group['group_id'], true);
  $ins_id = sql_parse_id($group['group_id']);
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group['contact_nb']) {
    $cpt++;
    $c_id = sql_parse_id($group["contact$cpt"], true);
    $query = "SELECT * FROM contactgroup
    WHERE group_id $sel_id
    AND contact_id $c_id ";
    display_debug_msg($query, $cdg_sql, 'run_query_group_contactgroup_insert(1)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);

    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $c_id = sql_parse_id($group["contact$cpt"]);
      $query = "INSERT INTO contactgroup (
      group_id,
      contact_id)
      VALUES ($ins_id, $c_id)";

      display_debug_msg($query, $cdg_sql, 'run_query_group_contactgroup_insert(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_ins > 0) {
    of_contactgroup_update_group_hierarchy($id);
  }

  return $cpt_ins;
}

/**
 * run_query_group_contact_create 
 * 
 * @access public
 * @return void
 */
function run_query_group_contact_create($group) {
  $email = $group['contact_add'];
  $addressbooks = OBM_AddressBook::search();
  $addressbook = $addressbooks->getPublicAddressbook($email);
  $contacts = $addressbook->getContacts($email);
  if(!empty($contacts)) {
    return key($contacts);
  }
  if($addressbook->write) {
    list( $firstname, $lastname ) = guess_name_from_email($email);
    $data['lastname'] = $lastname;
    $data['firstname'] = $firstname;
    $data['emails'][0]['label'] = 'INTERNET';
    $data['emails'][0]['address'] = $email; 
    $contact = $addressbook->addContact($data);
    OBM_IndexingService::commit('contact');
    return $contact->id;
  }
  return false;
}
///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, contact_nb, contactX
// Return: number of contacts deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_contactgroup_delete($group, $domain_id) {
  global $cdg_sql;

  $g = get_group_info($group['group_id']);
  $gid = $g['gid'];

  $sql_id = sql_parse_id($group['group_id'], true);
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group['contact_nb']) {
    $cpt++;
    $c_id = $group["contact$cpt"];
    $sql_c_id = sql_parse_id($c_id, true);

    $query = "DELETE FROM contactgroup
    WHERE group_id $sql_id AND contact_id $sql_c_id ";
    display_debug_msg($query, $cdg_sql, 'run_query_group_contactgroup_delete()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_del > 0) {
    of_contactgroup_update_group_hierarchy($group['group_id']);
  }

  return $cpt_del;
}



/**
 * Query execution : UserObmGroup conditionnal insertion
 *
 * @param mixed $group[] : group hash info : keys used : id, user_nb, userX
 * @result number of users inserted
 **/
function run_query_group_usergroup_insert($group) {
  global $obm, $cdg_sql;

  $id = $group['group_id'];
  $sel_id = sql_parse_id($group['group_id'], true);
  $ins_id = sql_parse_id($group['group_id']);
  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $group['user_nb']) {
    $cpt++;
    $u_id = sql_parse_id($group["user$cpt"], true);

    $query = "SELECT * FROM UserObmGroup
    WHERE userobmgroup_group_id $sel_id
    AND userobmgroup_userobm_id $u_id ";
    display_debug_msg($query, $cdg_sql, 'run_query_group_usergroup_insert(1)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);

    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {
      $u_id = sql_parse_id($group["user$cpt"]);
      $query = "INSERT INTO UserObmGroup (
      userobmgroup_group_id,
      userobmgroup_userobm_id)
      VALUES ($ins_id, $u_id)";

      display_debug_msg($query, $cdg_sql, 'run_query_group_usergroup_insert(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_ins > 0) {
    of_usergroup_update_group_hierarchy($id);
  }

  return $cpt_ins;
}



///////////////////////////////////////////////////////////////////////////////
// Query execution : UserObmGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, user_nb, userX
// Return: number of users deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_usergroup_delete($group, $domain_id) {
  global $cdg_sql;

  $g = get_group_info($group['group_id']);
  $gid = $g['gid'];

  $sql_id = sql_parse_id($group['group_id'], true);
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group['user_nb']) {
    $cpt++;
    $u_id = $group["user$cpt"];
    $sql_u_id = sql_parse_id($u_id, true);

    $u = get_user_info($u_id);
    $u_gid = $u['gid'];

    // Prevent to remove an user if the group is his default group
    if ($u_gid != $gid) {
      $query = "DELETE FROM UserObmGroup
      WHERE userobmgroup_group_id $sql_id
      AND userobmgroup_userobm_id $sql_u_id ";
      display_debug_msg($query, $cdg_sql, 'run_query_group_usergroup_delete()');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      if ($retour) {
	$cpt_del++;
      }
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_del > 0) {
    of_usergroup_update_group_hierarchy($group['group_id']);
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : GroupGroup conditionnal insertion
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups inserted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group_insert($group) {
  global $cdg_sql;

  $sel_id = sql_parse_id($group['group_id'], true);
  $id = sql_parse_id($group['group_id']);

  $cpt = 0;
  $cpt_ins = 0;

  $parents = get_group_parents($id);
  $g = get_group_info($id);
  $privacy = sql_parse_int($g['privacy']);

  while ($cpt < $group['group_nb']) {
    $cpt++;
    $sel_g_id = sql_parse_id($group["group$cpt"], true);
    $g_id = sql_parse_id($group["group$cpt"]);

    $query = "SELECT group_privacy, groupgroup_child_id FROM
    UGroup
    LEFT JOIN GroupGroup ON groupgroup_parent_id $sel_id AND groupgroup_child_id = group_id
    WHERE group_id $sel_g_id ";
    display_debug_msg($query, $cdg_sql, 'run_query_group_group_insert(1)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    $test_q->next_record();
    // If the entry doesn't already exist, doesn't create a loop,
    // and child group is same privacy as parent, insert it
    if ( (!$test_q->f('groupgroup_child_id'))
    && (! in_array($g_id, $parents))
    && ($id != $g_id)
    && ($privacy == 1 || $test_q->f('group_privacy') != 1)
    ) {
      $query = "INSERT INTO GroupGroup (
      groupgroup_parent_id,
      groupgroup_child_id)
      VALUES ($id, $g_id)";

      display_debug_msg($query, $cdg_sql, 'run_query_group_group_insert(2)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);
      $cpt_ins++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_ins > 0) {
    of_usergroup_update_group_hierarchy($id);
    of_contactgroup_update_group_hierarchy($id);
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Query execution : GroupGroup deletion
// Parameters:
//   - $group[] : group hash info : keys used : id, group_nb, group_X
// Return: number of groups deleted
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group_delete($group) {
  global $cdg_sql;

  $id = sql_parse_id($group['group_id'], true);
  $cpt = 0;
  $cpt_del = 0;
  while ($cpt < $group['group_nb']) {
    $cpt++;
    $g_id = sql_parse_id($group["group$cpt"], true);

    $query = "DELETE FROM GroupGroup
    WHERE groupgroup_parent_id $id
    AND groupgroup_child_id $g_id ";

    display_debug_msg($query, $cdg_sql, 'run_query_group_group_delete()');
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
    }
  }

  // Update internal group membership direct mapping
  if ($cpt_del > 0) {
    of_usergroup_update_group_hierarchy($group['group_id']);
    of_contactgroup_update_group_hierarchy($group['group_id']);
  }

  return $cpt_del;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the users of the given group
// Parameters:
//   - $group[] : Group parameters
//   - $gid [optionnal] group gid, to compare with users one
///////////////////////////////////////////////////////////////////////////////
function run_query_group_user_group($group, $gid='') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type,'', '', 'group_user');

  $id = sql_parse_id($group['group_id'], true);
  $entity = $group['entity'];
  $sql_order_field = $group['sql_order_field'];
  $sql_order_dir = $group['sql_order_dir'];

  if (($entity == 'group_user') && (trim($sql_order_field) != '')) {
    $order = "ORDER BY $sql_order_field $sql_order_dir";
  } else {
    $order = 'ORDER BY userobm_lastname';
  }

  if ($gid != '') {
    // return the difference between the group gid and the user gid
    $field = "$gid != userobm_gid as gid_diff,";
  } else {
    $field = "TRUE as gid_diff,";
  }

  $query = "SELECT
    userobmgroup_userobm_id as group_user_id,
    userobmgroup_userobm_id as id,
    userobm_timeupdate as group_user_timeupdate,
    userobm_timecreate as group_user_timecreate,
    userobm_userupdate as group_user_userupdate,
    userobm_usercreate as group_user_usercreate,
    userobm_login as group_user_login,
    userobm_lastname as group_user_lastname,
    userobm_firstname as group_user_firstname,
    userobm_phone as group_user_phone,
    userobm_email as group_user_email,
    userobm_hidden,
    $field
    domain_name
  FROM UserObmGroup LEFT JOIN UserObm ON userobmgroup_userobm_id=userobm_id
  LEFT JOIN Domain ON domain_id = userobm_domain_id
  WHERE userobmgroup_group_id $id AND userobm_archive != 1
  $order
  $limit";

  display_debug_msg($query, $cdg_sql, 'run_query_group_user_group()');
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_group_group($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type,'', '','group_group');

  $id = sql_parse_id($group['group_id'], true);
  $entity = $group['entity'];
  $sql_order_field = $group['sql_order_field'];
  $sql_order_dir = $group['sql_order_dir'];

  if (($entity == 'group_group') && (trim($sql_order_field) != '')) {
    $order = "ORDER BY $sql_order_field $sql_order_dir";
  } else {
    $order = 'ORDER BY group_name';
  }

  $query = "SELECT
  groupgroup_child_id as child_id,
  groupgroup_child_id as id,
  group_timeupdate,
  group_timecreate,
  group_userupdate,
  group_usercreate,
  group_id,
  group_name,
  group_desc,
  group_email
  FROM GroupGroup LEFT JOIN UGroup ON groupgroup_child_id=group_id
  WHERE groupgroup_parent_id $id
  $order
  $limit";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_group_group()');
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Query Execution : Get the groups members of the given group
// Parameters:
//   - $group[] : Group parameters
///////////////////////////////////////////////////////////////////////////////
function run_query_group_contact($group) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $limit = sql_limit($db_type,'', '','group_contact');

  $id = sql_parse_id($group['group_id'], true);
  $entity = $group['entity'];
  $sql_order_field = $group['sql_order_field'];
  $sql_order_dir = $group['sql_order_dir'];

  if (($entity == 'group_contact') && (trim($sql_order_field) != '')) {
    $order = "ORDER BY $sql_order_field $sql_order_dir";
  } else {
    $order = 'ORDER BY contact_lastname';
  }

  $query = "SELECT
    Contact.contact_id as group_contact_id,
    Contact.contact_id as id,
    contact_timeupdate as  group_contact_timeupdate,
    contact_timecreate as  group_contact_timecreate,
    contact_userupdate as  group_contact_userupdate,
    contact_usercreate as  group_contact_usercreate,
    contact_lastname as  group_contact_lastname,
    contact_firstname as  group_contact_firstname,
    phone_number as  group_contact_phone,
    email_address as  group_contact_email
    FROM contactgroup 
    INNER JOIN UGroup ON contactgroup.group_id = UGroup.group_id
    INNER JOIN Contact ON Contact.contact_id = contactgroup.contact_id
    INNER JOIN ContactEntity ON contactentity_contact_id = Contact.contact_id
    LEFT JOIN Email ON email_entity_id = contactentity_entity_id AND email_label = 'INTERNET;X-OBM-Ref1'
    LEFT JOIN Phone ON phone_entity_id = contactentity_entity_id AND phone_label = 'CELL;VOICE;X-OBM-Ref1'
  WHERE contactgroup.group_id $id
  $order
  $limit";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql, 'run_query_group_contact()');
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Return the number of registered user in the group specified
// Parameters:
//   - $id : group id
///////////////////////////////////////////////////////////////////////////////
function get_group_nb_user($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT count(*) FROM UserObmGroup
  WHERE userobmgroup_group_id $id ";

  display_debug_msg($query, $cdg_sql, 'get_group_nb_user()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f(0);

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a group (except with id given) with the name given already exists
// Parameters:
//   - $name : name to search for
//   - $id   : group id to exclude
// Returns:
//   - true (if a group exists) or false
///////////////////////////////////////////////////////////////////////////////
function get_group_name_exists($name, $id='') {
  global $cdg_sql, $obm;
  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "and group_id $id ";
  }

  $multidomain = sql_multidomain('group'); 
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $query = "SELECT group_id, group_name
  FROM UGroup
  WHERE group_name $like '$name'
  AND (group_privacy=0 OR group_usercreate=$obm[uid])
  $where_id
  $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_group_name_exists()');
  $obm_q->query($query);

  if ($obm_q->num_rows() > 0) {
    return true;
  } else {
    return false;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Return the groups which matches the name or the desc
// except the one given (update mode)
// Parameters:
//   - $id   : group id
//   - $name : group name
//   - $desc : group desc
///////////////////////////////////////////////////////////////////////////////
function run_query_group_check($id, $name, $desc) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $multidomain = sql_multidomain('group');

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND group_id $id ";
  }

  // If name is short, we test equality, else similarity
  if (strlen($name) > 2) {
    $wname = "group_name $like '%$name%'";
  } else {
    $wname = "group_name = '$name'";
  }

  // If desc is short, we test equality, else similarity
  if (trim($desc) != '') {
    if (strlen($desc) > 2) {
      $wdesc = "group_desc $like '%$desc%'";
    } else {
      $wdesc = "group_desc = '$desc'";
    }
  }
  if ($wdesc != "") $wdesc = "or $wdesc";

  $query = "SELECT distinct group_id, group_name, group_desc
  FROM UGroup
  WHERE
  (group_privacy=0 OR group_usercreate=$obm[uid] )
  AND ($wname $wdesc)
  $where_id
  $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_group_check()');
  $obm_q->query($query);

  return $obm_q;
}

/**
 * Check if childs group can be added to a parent group.
 * This function return the number of groups addable and
 * number of groups that cannot be add in case of inserting
 * private group into a public group.
 *
 * @param array $params
 * @return array $nb_grp
 */
function run_query_group_group_checking($group) {
  $nb_group = array('add' => 0, 'diff_privacy' => 0);
  $id = sql_parse_id($group['group_id']);

  $child_nb = $group['group_nb'];

  $g = get_group_info($id);

  $childs_id = array();
  for ($i=1; $i<=$child_nb; $i++) {
    $childs_id[] = $group["group$i"];
  }

  $childs_privacy = array();
  if (count($childs_id) > 0) {
    $query = "SELECT group_privacy, group_id FROM UGroup where group_id IN (".implode(',', $childs_id).")";
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $childs_privacy[$obm_q->f('group_id')] = $obm_q->f('group_privacy');
    }

    for($i=1; $i<=$child_nb; $i++) {

      // Check if a child_group is not same as group
      $child_g_id = $group["group$i"];

      if ($child_g_id != $id) {

        // This child group is potentialy addable
        $nb_group['add']++;

        // If group is public and child group is private
        // we count it.
        if ($g['privacy'] == 0 && $childs_privacy[$child_g_id] == 1) {
          $nb_group['diff_privacy']++;
        }
      }
    }
  }
  return $nb_group;
}


///////////////////////////////////////////////////////////////////////////////
// Group context checking (same groups exists ?)
// Parameters:
//   - $id       : group id
//   - $group[]  : group values
//     keys used : name, desc, email
// Returns:
//   - Group Database object with group of similar groups
///////////////////////////////////////////////////////////////////////////////
function check_group_context($id, $group) {
  global $cdg_sql;

  $name = $group['name'];
  $desc = $group['desc'];

  // return the groups with same name or desc
  $g_q = run_query_group_check($id, $name, $desc);

  return $g_q;
}


/*
 * Group specific rights checking (eg : public group needs write_admin)
 * Parameters:
 * @param $params : parameters values
 * @param $g[]    : current group values
 * @return true if OK, else false
 */
function check_group_update_rights($params, $g='') {
  global $actions, $perm, $err, $cright_write_admin;
  global $cg_gid_smb_user, $cg_gid_smb_admin, $cg_gid_smb_guest, $cg_gid_user;
  global $l_cant_update_domain_group;
  global $l_err_public_rights, $l_error_permission, $l_error_delegation;

  // Hook : Pre
  if (function_exists('hook_pre_check_group_update_rights')) {
    if (! hook_pre_check_group_update_rights($params, $g)) {
      return false;
    }
  }

  $id = $params['group_id'];
  if ($id > 0) {
    // Get current group info if not given
    if (! is_array($g)) {
      $g = get_group_info($id);
    }

    // Check delegation update right
    if (! of_delegation_check_update_rights($params, $g['delegation'])) {
      $err['msg'] .= $l_error_delegation;
      return false;
    }

    // Check update right is set
    if (! $perm->check_right('group', $actions['group']['update']['Right'])) {
      $err['msg'] .= $l_error_permission;
      return false;
    }

    // Allow public group handling only if write_admin right
    if (($g['privacy'] != 1)
    && (! $perm->check_right('group', $cright_write_admin))) {
      $err['msg'] .= $l_err_public_rights;
      return false;
    }

    /*    // We forbid to update Window system groups
    $gid = $g['gid'];

    if (($gid == $cg_gid_user) || ($gid == $cg_gid_smb_user)
	|| ($gid == $cg_gid_smb_admin) || ($gid == $cg_gid_smb_guest)) {
      $err['msg'] .= $l_cant_update_domain_group;
      return false;
    }
    */
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Group Form Data checking and formatting
// Parameters:
//   - $group[]  : values checked
//     keys used : name, desc, email
///////////////////////////////////////////////////////////////////////////////
function check_group_data_form(&$group) {
  global $php_regexp_email_name, $php_regexp_email, $l_fill_name, $l_j_check_email;
  global $l_groupname_error, $php_regexp_groupname;
  global $l_group_exists, $l_email_exist;
  global $obm, $perm, $err, $action, $cright_write_admin,$cgp_use;
  global $l_j_check_email, $l_system_no_priv, $l_err_system_samba;
  global $l_only_owner_priv, $l_err_public_rights, $l_err_private_email;

  // Hook : Pre
  if (function_exists('hook_pre_check_group_data_form')) {
    if (! hook_pre_check_group_data_form($group)) {
      return false;
    }
  }

  $id = $group['group_id'];
  $name = $group['name'];
  $samba = $group['samba'];
  $desc = $group['desc'];
  $email = stripslashes($group['email']);
  $priv = $group['privacy'];
  $g = get_group_info($id);
  $usercreate = $g['usercreate'];
  $system = $g['system'];
  $gsamba = $g['samba'];


  // MANDATORY: Group name not empty
  if (trim($name) == '') {
    $err['msg'] = $l_fill_name;
    $err['field'] = 'name';
    return false;
  }

  if (! preg_match($php_regexp_groupname, $name)) {
    $err['msg'] = $l_groupname_error;
    $err['field'] = 'name';
    return false;
  }

  // MANDATORY: Group name unique
  if (get_group_name_exists($name, $id)) {
    $err['msg'] = "$l_group_exists ($name)";
    $err['field'] = 'name';
    return false;
  }

  if ($cgp_use['service']['mail']) {
    if ($email != '') {
      $um = array();
      $em = strtok($email, "\r\n");
      while ($em) {
        if (! preg_match($php_regexp_email_name, $em) && !preg_match($php_regexp_email, $em)) {
          $err['msg'] .= "$em : $l_j_check_email";
          $err['field'] = 'email';
          return false;
        }

        // Email address not already used by this user
        if (in_array($em, $um)) {
          $err['msg'] = "$l_email_exist : $em ";
          $err['field'] = 'email';
          return false;
        }
        $emails[] = $em;
        array_push($um, $em);
        $em = strtok("\r\n");
      }
      $mails = get_email_used($emails, '', $id);
      if(!empty($mails)) {
        $err['msg'] = "$l_email_exist : $mails ";
        $err['field'] = 'email';
        return false;
      }
    }
  } else {
    // OBM-Mail disabled, email must be full (user@domain)
    if (($email != '') && (preg_match($php_regexp_email, $email) == 0)) {
      $err['msg'] = $l_email_error.' :  '. $email;
      $err['field'] = 'email';
      return false;
    }
  }


  // Les contraintes sur la delegation ne sont pas valables pour le domaine global
  if ($cgp_use['property']['delegation']) {
    if(!of_delegation_check_data($group['delegation'])) {
      $err['field'] = 'delegation';
      return false;
    }
  } 


  // Allow public group handling only if write_admin right
  if (($priv != 1) && (! $perm->check_right('group', $cright_write_admin))) {
    $err['msg'] = "$l_err_public_rights";
    $err['field'] = 'privacy';
    return false;
  }

  // A public group can only be private if user = owner
  if ($priv && ($usercreate > 0) && ($usercreate != $obm['uid'])) {
    $err['msg'] = "$l_only_owner_priv";
    $err['field'] = 'privacy';
    return false;
  }

  // A private group can't have an email address
  if ($priv == 1 && ($email != '')) {
    $err['msg'] = "$l_err_private_email";
    $err['field'] = 'email';
    return false;
  }

  // A system group can't be private
  if ($system && $priv) {
    $err['msg'] = "$l_system_no_priv";
    $err['field'] = 'system';
    return false;
  }

  if ($action == 'update') {
    // MANDATORY: we do not allow to delete samba perms for a system user
    if (($system) && ($gsamba == '1') && ($samba != '1')) {
      $err['msg'] = "$l_err_system_samba";
      $err['field'] = 'samba';
      return false;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the group can be deleted
// Parameters:
//   - $p_id : group id
// Returns:
//   true if the group can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_group_can_delete($p_id) {
  global $obm, $perm, $err, $cright_write_admin, $ok_msg;
  global $l_warn_delete, $l_cant_delete_system, $l_err_public_rights;
  global $cg_gid_smb_user, $cg_gid_smb_admin, $cg_gid_smb_guest, $cg_gid_user;
  global $l_cant_delete_domain_group, $l_err_cant_delete_default_user_group;

  $delete_ok = true;

  // Hook : Pre
  if (function_exists('hook_pre_check_group_can_delete')) {
    if (! hook_pre_check_group_can_delete($p_id)) {
      return false;
    }
  }

  // Check that user has delegation rights for this group 
  if (! check_group_update_rights(array('group_id' =>$p_id))) {
    $delete_ok = false;
  }
  $lgroup = get_group_info($p_id);
  $gid = $lgroup['gid'];
  $domain_id = $lgroup['domain_id'];

  if ($lgroup['system'] != '0') {
    $err['msg'] .= "$l_cant_delete_system";
    $delete_ok = false;

  } else if (($lgroup['privacy'] == '0') &&
	     (! $perm->check_right('group', $cright_write_admin))) {
    $err['msg'] .= $l_err_public_rights;
    $err['field'] = 'privacy';
    $delete_ok = false;

  } else if (($gid == $cg_gid_user) || ($gid == $cg_gid_smb_user)
	     || ($gid == $cg_gid_smb_admin) || ($gid == $cg_gid_smb_guest)) {
    $err['msg'] .= $l_cant_delete_domain_group;
    $delete_ok = false;

  } else if ($gid != null && get_users_nb_from_gid($domain_id, $gid) > 0) {
    $err['msg'] .= $l_err_cant_delete_default_user_group;
    $delete_ok = false;

  } else {
    $nb_u = get_group_nb_user($p_id);
    $ok_msg .= "$nb_u $l_warn_delete";
  }

  return $delete_ok;
}


/**
 * Search group from a single field
 *
 * @param mixed $group
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function get_users_nb_from_gid($domain_id, $gid) {
  global $cdg_sql;

  $query = "SELECT count(*) as nb
    FROM UserObm
    WHERE userobm_gid='$gid' AND userobm_domain_id=$domain_id";

  display_debug_msg($query, $cdg_sql, 'get_users_nb_from_gid()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $nb = $obm_q->f('nb');

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get a group parent list
// Parameters:
//   - $id : group id
// Returns:
//   - array of parent groups ids
///////////////////////////////////////////////////////////////////////////////
function get_group_parents($id) {
  global $cdg_sql;
  $id = sql_parse_id($id, true);
  $query = "SELECT groupgroup_parent_id
  FROM GroupGroup
  WHERE groupgroup_child_id $id ";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  $parents = array();
  while ($obm_q->next_record()) {
    $g_id = $obm_q->f('groupgroup_parent_id');
    array_push($parents, $g_id);
    $child_parents = get_group_parents($g_id);
    $parents = array_merge($parents, $child_parents);
  }

  return array_unique($parents);
}


/**
 * Search group from a single field
 *
 * @param mixed $group
 * @access public
 * @return array of 'length' => DB query results nb, and 'datas' => DB Search results
 */
function run_query_group_ext_search($group) {
  global $obm, $c_all, $cdg_sql, $ctu_sql_limit;

  $pattern = $group['pattern'];
  $restriction = $group['restriction'];

  $obm_q = new DB_OBM;
  $multidomain = sql_multidomain('Group');
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = $group['limit'];
  if (isset($group['first_row'])) $first_row = $group['first_row'] - 1;
  else $first_row = 0;
  $uid = sql_parse_id($obm['uid'], true);
  $where_privacy = "AND (group_privacy = 0 OR (group_privacy = 1 AND group_usercreate $uid ))";

  if ($restriction == 'user') {
    $uid = $obm['uid'];
    $r_groups = OBM_Acl::getAllowedEntities($uid, 'group', 'read');
    $r_groups_ids = array_keys($r_groups);
    if ((is_array($r_groups_ids)) && (count($r_groups_ids) > 0)) {
      $where_restriction = " AND (group_id IN (";
      foreach($r_groups_ids as $r_id) {
        $where_restriction .= "$coma'$r_id'";
        $coma = ',';
      }
      $where_restriction .= "))";
    }
  } else if ($restriction == 'calendar') {
    $where_privacy = ' AND group_privacy=0';
  }

  $query = "SELECT
    group_id,
    group_name,
    group_desc
  FROM UGroup
  WHERE
    group_name $like '$pattern%'
    $where_privacy
    $multidomain
    $where_restriction
  ORDER BY group_name
  LIMIT 24 OFFSET 0";

  display_debug_msg($query, $cdg_sql, 'run_query_group_ext_search()');
  $obm_q->query($query);
  $resultsCount = $obm_q->nf();

  return array('length' => $resultsCount, 'datas' => $obm_q);
}


function get_json_user_group($gid) {
  global $display;

  $users = of_usergroup_get_group_users($gid);
  if (is_array($users)) {
    $data = array();
    foreach($users as $u_id) {
      $u = get_entity_info($u_id, 'user');
      $user = "{id:'$u_id',label:'".phpStringToJsString($u[0])."'}";
      array_push($data, $user);
    }
    $display['json'] = "users:[".implode(",", $data)."]";
  } else {
    $display['json'] = "users:[]";
  }
}

?>
