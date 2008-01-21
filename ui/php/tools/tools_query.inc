<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : tools_query.inc                                              //
//     - Desc : Tools query file                                             //
// 2007-05-26 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Remote access detail query execution
// Returns:
//   - remote_access state (1 = active or 0)
///////////////////////////////////////////////////////////////////////////////
function get_tools_remote_access() {
  global $cdg_sql;

  $query = "SELECT obminfo_name, obminfo_value
    FROM ObmInfo
    WHERE obminfo_name = 'remote_access'";

  display_debug_msg($query, $cdg_sql, 'get_tools_remote_access()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $ra = $obm_q->f('obminfo_value');

  return $ra;
}


///////////////////////////////////////////////////////////////////////////////
// Remote access update query execution
// Parameters:
//   - $tools[]  : entry values
//     keys used : remote_access
///////////////////////////////////////////////////////////////////////////////
function run_query_tools_remote_update($tools) {
  global $cdg_sql;

  $remote_access = $tools['remote_access'];

  $query = "UPDATE ObmInfo SET
    obminfo_value = '$remote_access'
  WHERE obminfo_name = 'remote_access'";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_remote_update()');
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update system configuration execution
// @param $tools[] : entry values
///////////////////////////////////////////////////////////////////////////////
function exec_tools_update_update($tools) {
  global $obm, $cdg_sql, $cdg_exe, $cmd_update;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $domain_id = $tools['domain_id'];

  $state = get_update_state($domain_id);
  if ($state != 1) {
    return false;
  }     

  $realm = $tools['realm'];
  $update = $tools['update'];
  $update_type = $tools['update_type'];

  // Force global update from Domain 0
  if ($obm['domain_id'] == '0') {
    $update_type == 'global';
  }

  // Update command creation
  $cmd = $cmd_update." --$update_type --domain $domain_id";
  
  if ($realm == 'user') {
    $cmd .= " --user $uid";
  } else if ($realm == 'delegation') {
    $cmd .= " --delegation '$delegation'";
  }

  $cmd .= " >/dev/null 2>&1 &";

  // Mise a jour de l'indicateur de modifications
  set_update_state('0'); // XXXXX A virer. qd remet-on l'indic???

  display_debug_msg($cmd, $cdg_exe, 'exec_tools_update_update()');
  unset($tmp);
  exec($cmd, $tmp, $ret);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a system configuration execution is possible
// @param $tools[] paramateres values
// @param $params : parameters hash
// @return (true | false) : true if ok else false
///////////////////////////////////////////////////////////////////////////////
function check_tools_update_context_ok($params) {
  global $obm, $profiles, $display, $cmd_check_update, $err;
  global $l_error_permission, $l_upd_already_running;

  $update_ok = true;

  $domain_id = $params['domain_id'];
  $update_type = $params['update_type'];
  $admin_realm = $profiles[$obm['profile']]['properties']['admin_realm'];

  // Check user admin rights (default to domain if not set)
  if ((! isset($admin_realm)) || ($admin_realm == '')) {
    $admin_realm = array('domain');
  }

  // if trying to exec an update
  // Check update type is allowed (global need domain 0 or 'domain' realm)
  if (($update_type == 'global')
     && (($domain_id != 0)
	 && (! in_array('domain', $admin_realm)))) {
    $err['msg'] = $l_error_permission;
    return false;
  }
  
  // Check if update is locked
  $update_lock = get_update_lock();
  if ($update_lock == '1') {
    $err['msg'] = $l_upd_already_running;
    return false;
  }

  // An update must not already running
  $ps = shell_exec("$cmd_check_update");

  if ($ps) {
    $err['msg'] = $l_upd_already_running;
    return false;
  }

  return $update_ok;
}


/******************************************************************************
 * Check if a domain has been updated so update must be global
 * check on Domain and Samba table, on fields impacting users
 * @param $domain_id : Domain id
 * @return (true | false) : true if global update needed
 */
function check_tools_update_need_global_update($domain_id=0) {
  global $cdg_sql, $obm;

  $e1 = array();
  $e2 = array();

  $obm_q = new DB_OBM;

  if ($domain_id != 0) {
    $where = "WHERE domain_id = '$domain_id'";
  }

  // Domain table check
  $select = "SELECT domain_id,
      domain_name,
      domain_alias,
      domain_mail_server_id";

  // Get infos from Update table
  $query = "$select FROM Domain $where";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('0');  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // keep only column name (remove column number entries)
      if ((int)($fname) !== $fname) {
	$e1[$id][$fname] = $fvalue;
      }
    }
  }

  // Get infos from Production table
  $query = "$select FROM P_Domain $where";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('0');  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // keep only column name (remove column number entries)
      if ((int)($fname) !== $fname) {
	$e2[$id][$fname] = $fvalue;
      }
    }
  }

  // Check for differences
  foreach ($e1 as $id => $row) {
    if (! array_key_exists($id, $e2)) {
      return true;
    } else {
      foreach($row as $fname => $fvalue) {
	if ($e2[$id][$fname] != $fvalue) {
	  return true;
	}
      }
    }
  }

  // Samba table check
  if ($domain_id != 0) {
    $where = "WHERE samba_domain_id = '$domain_id'";
  }

  // Get infos from Update table
  $query = "SELECT * FROM Samba $where ORDER BY samba_domain_id, samba_name";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('0');  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // keep only column name (remove column number entries)
      if ((int)($fname) !== $fname) {
	$e1[$id][$fname] = $fvalue;
      }
    }
  }

  // Get infos from Production table
  $query = "SELECT * FROM P_Samba $where ORDER BY samba_domain_id, samba_name";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f('0');  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // keep only column name (remove column number entries)
      if ((int)($fname) !== $fname) {
	$e2[$id][$fname] = $fvalue;
      }
    }
  }

  // Check for differences
  foreach ($e1 as $id => $row) {
    if (! array_key_exists($id, $e2)) {
      return true;
    } else {
      foreach($row as $fname => $fvalue) {
	if ($e2[$id][$fname] != $fvalue) {
	  return true;
	}
      }
    }
  }

  return false;
}


/**
 * Get production or update infos for an entity
 * @param $target : 'prod' | 'upd'
 * @param $domain_id : domain id to check
 * @param $table  : table where to check
 * @param $field  : field prefix (if not from table name) to check
 * @param $realm  : realm space to check updates (user | domain | delegation)
 * @param $exclude_fields : fields not to take in account for update check
 * @return array $e filled with update or production values
 **/
function get_update_entity_infos($target='both', $domain_id, $table, $field='', $realm, $exclude_fields=array()) {
  global $cdg_sql, $cdg_exe, $obm;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($field != '') {
    $f = $field;
    $uc = "${field}_usercreate";
    $uu = "${field}_userupdate";
    $dg = "${field}_delegation";
    $table_prefix = "${field}_";
  } else {
    $f = strtolower($table);
    $uc = "${f}_usercreate";
    $uu = "${f}_userupdate";
    $dg = "${f}_delegation";
    $table_prefix = "${f}_";
  }

  $multidomain = "AND (${f}_domain_id='$domain_id')";

  // Specific condition according to table (eg handle only public groups)
  if ($table == 'UGroup') {
    $where_spec = 'AND group_privacy=0';
  }

  // Restrict updates to the realm selected (user, domain, delegation)
  if ($realm == 'user') {
    $where_upd = "WHERE ($uu=$uid OR ($uc=$uid AND $uu is NULL)) $where_spec $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  } else if ($realm == 'delegation') {
    $where_upd = "WHERE ($dg $like '$delegation%') $where_spec $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  } else {
    $where_upd = "WHERE 1=1 $where_spec $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  }

  // Select fields construct
  $obm_qm = new DB_OBM;
  $fields = $obm_qm->metadata($table, false);
  $pos = strlen($table_prefix);
  $coma = '';
  foreach ($fields as $one_field) {
    $field_name = substr($one_field['name'], $pos);
    if ($exclude_fields[$field_name] != 1) {
      $select_fields .= "$coma
      $one_field[name] as $field_name";
      $coma = ',';
    }
  }

  // Debug perf info
  /*  $da = date('i:s:u');
  $f = exec("ps auxwk-size | grep apache | head -1 | cut -c 25-32", $tmp, $ret);
  display_debug_msg("<br>$da : mem=$f, realm=$realm, $table", $cdg_exe, 'Perf');
  echo "<br>$da : mem=$f, realm=$realm, $table";
  */

  if ($target == 'upd') {
    // Get infos from Update table
    $query = "SELECT $select_fields FROM $table $where_upd";
  } else if ($target == 'prod') {
    $table_prod = "P_$table";
    $query = "SELECT $select_fields FROM $table_prod $where_prod";
  }

  display_debug_msg($query, $cdg_sql, "get_update_entity_infos($table)");
  $retour = $obm_q->query($query);

  $e = array();
  while($obm_q->next_record('BD_ASSOC')) {
    $id = $obm_q->f('id');
    $er = $obm_q->Record;
    foreach ($er as $fname => $fvalue) {
      //    foreach ($obm_q->Record as $fname => $fvalue) {
      // keep only column name (remove column number entries and exclude fields)
      //      if (((int)($fname) !== $fname) && !($exclude_fields[$fname] == 1)) {
      if ( !($exclude_fields[$fname] == 1)) {
	$e[$id][$fname] = $fvalue;
	//	$e1[$obm_q->f('id')][$fname] = $fvalue;
      }
    }
  }

  // Debug perf info
  /*  $da = date('i:s:u');
  $f = exec("ps auxwk-size | grep apache | head -1 | cut -c 25-32", $tmp, $ret);
  display_debug_msg("<br>$da : mem=$f, $table : after $target store", $cdg_exe, 'Perf');
  echo "<br>$da : mem=$f, $table : after $target store";
  */
  $obm_q->free();
  return $e;
}


///////////////////////////////////////////////////////////////////////////////
// Process update infos : Calculate entries differences for an entity
// Parameters:
//   - $table  : table to get numbers
//   - $realm  : realm space to check updates (user | domain | delegation)
// Returns:
//   - differences in an array 'created', 'updated', 'deleted', 'move'
///////////////////////////////////////////////////////////////////////////////
function process_update_numbers($e1, $e2, $domain_id, $table, $realm) {
  global $obm, $cdg_sql, $cdg_exe;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  // Process infos for differences (created and updated entries)
  foreach ($e1 as $id => $row) {
    if (! array_key_exists($id, $e2)) {
      $nb_add++;
      display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; $table : new : $id");
    } else {
      foreach($row as $fname => $fvalue) {
	if ($e2[$id][$fname] != $fvalue) {
	  $nb_upd++;
	  $prod_value = $e2[$id][$fname];
	  display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; $table ; updated id=$id ; $fname (current value=$prod_value, new value=$fvalue)");
	  break;
	}
      }
    }
  }

  // Get infos for deleted entities
  if ($realm == 'user') {

    // Get the delete from the Deleted table
    $query = "SELECT count(*) as nb FROM Deleted
     WHERE deleted_user_id=$uid
       AND deleted_table='$table'";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
    $retour = $obm_q->query($query);
    $obm_q->next_record();
    $nb_del = $obm_q->f('nb');
    display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $table : deleted : $nb_del entries in table Deleted");

  } else if ($realm == 'delegation') {

    // Get the delete from the Deleted table
    $query = "SELECT count(*) as nb FROM Deleted
     WHERE deleted_delegation $like '$delegation%'
       AND deleted_table='$table'
       AND deleted_domain_id='$domain_id'";
    display_debug_msg($query, $cdg_sql, 'run_query_user_delete(auto)');
    $retour = $obm_q->query($query);
    $obm_q->next_record();
    $nb_del = $obm_q->f('nb');
    display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $table : deleted : $nb_del entries in table Deleted");

  } else {
    // Case domain, just count lines present in prod, but not present in upd
    foreach ($e2 as $id => $row) {
      if (! array_key_exists($id, $e1)) {
	$nb_del++;
	display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $table : deleted : id=$id");
      }
    }
  }

  $nb['created'] = $nb_add;
  $nb['updated'] = $nb_upd;
  $nb['deleted'] = $nb_del;
  $nb['move'] = $nb_add + $nb_upd + $nb_del;

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get update types number for an entity
// Parameters:
//   - $e2     : entity prod infos
//   - $domain_id : domain id to check
//   - $table  : entity table
//   - $field  : field prefix (if not from table name) to check
//   - $realm  : realm space to check updates (user | domain | delegation)
//   - $exclude_fields : fields not to take in account for update check
///////////////////////////////////////////////////////////////////////////////
function get_update_entity_numbers($e2='', $domain_id, $table, $field='', $realm, $exclude_fields) {
  global $cdg_sql, $obm;

  $e1 = get_update_entity_infos('upd', $domain_id, $table, $field, $realm, $exclude_fields);
  if ($e2 == '') {
    // If not given, get the production infos
    $e2 = get_update_entity_infos('prod', $domain_id, $table, $field, $realm, $exclude_fields);
  }
  $nb = process_update_numbers($e1, $e2, $domain_id, $table, $realm);

  unset($e1);

  return $nb;
}


/******************************************************************************
 * Get Group links updates (from of_usergroup)
 *
 * @param $domain_id : domain id to check
 * @param $realm : realm space to check updates (user | domain | delegation)
 * @return link array : $link[$group_id] = $group_id
 */
function get_update_link_group($domain_id, $realm) {
  global $obm, $cdg_sql, $cdg_exe;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $e1 = array();
  $e2 = array();

  $eid = 'group_id';
  $uc = 'group_usercreate';
  $uu = 'group_userupdate';
  $dg = 'group_delegation';

  $multidomain = "AND (group_domain_id='$domain_id')";

  // Restrict updates to the realm selected (user, domain, delegation)
  if ($realm == 'user') {
    $where = "AND ($uu=$uid OR ($uc=$uid AND $uu is NULL))";
  } else if ($realm == 'delegation') {
    $where = "AND ($dg $like '$delegation%')";
  } else {
    $where = '';
  }

  // of_usergroup Link --------------------------------------------------------

  // Get infos from Update table
  $query = "SELECT of_usergroup_group_id, of_usergroup_user_id
    FROM of_usergroup
      LEFT JOIN UGroup ON of_usergroup_group_id=group_id
    WHERE group_privacy=0
      $where $multidomain
    ORDER BY of_usergroup_group_id, of_usergroup_user_id";
  display_debug_msg($query, $cdg_sql, "get_update_link_group($realm)");
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $g_id = $obm_q->f('of_usergroup_group_id');
    $u_id = $obm_q->f('of_usergroup_user_id');
    $e1[$g_id][] = "u$u_id";
  }

  // Get infos from Production table
  $query = "SELECT of_usergroup_group_id, of_usergroup_user_id
    FROM P_of_usergroup
      LEFT JOIN P_UGroup ON of_usergroup_group_id=group_id
    WHERE group_privacy=0
      $where $multidomain
    ORDER BY of_usergroup_group_id, of_usergroup_user_id";
  display_debug_msg($query, $cdg_sql, "get_update_link_group(prod $realm)");
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $g_id = $obm_q->f('of_usergroup_group_id');
    $u_id = $obm_q->f('of_usergroup_user_id');
    $e2[$g_id][] = "u$u_id";
  }

  // Process and mark each group modified
  foreach($e1 as $g_id => $users) {
    if (count($e1[$g_id]) != count($e2[$g_id])) {
      $res[$g_id] = $g_id;
      $nb_link = count($e1[$g_id]);
      $nb_link_prod = count($e2[$g_id]);
      display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; Group links ; updated id=$g_id ; current #links=$nb_link_prod, new #links=$nb_link");
    } else {
      foreach ($users as $u_id) {
	if (! in_array($u_id, $e2[$g_id])) {
	  $res[$g_id] = $g_id;
	  display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; Group links ; updated id=$g_id ; diff=$u_id");
	  break;
	}
      }
    }
  }

  // Get empty or deleted groups (not in $e1) that were not empty (in $e2)
  foreach($e2 as $g_id => $users) {
    if (! isset($e1[$g_id])) {
      $res[$g_id] = $g_id;
      $nb_link = 0;
      $nb_link_prod = count($e2[$g_id]);
      display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; Group links ; updated id=$g_id ; current #links=$nb_link_prod, new #links=$nb_link");
    }
  }

  return $res;
}


/******************************************************************************
 * Get EntityRight links updates for an entity
 *
 * @param $entity : entity
 * @param $table  : entity table
 * @param $field  : field prefix (if not from table name) to check
 * @param $realm : realm space to check updates (user | domain | delegation)
 * @return link array : $link[$entity_id] = $entity_id
 */
function get_update_link_entityright($domain_id, $entity, $table, $field, $realm) {
  global $obm, $cdg_sql, $cdg_exe;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  $e1 = array();
  $e2 = array();

  if ($field != '') {
    $f = $field;
    $f_id = "${field}_id";
    $uc = "${field}_usercreate";
    $uu = "${field}_userupdate";
    $dg = "${field}_delegation";
  } else {
    $f = strtolower($table);
    $f_id = "${f}_id";
    $uc = "${f}_usercreate";
    $uu = "${f}_userupdate";
    $dg = "${f}_delegation";
  }

  $multidomain = "AND (${f}_domain_id='$domain_id')";

  // Restrict updates to the realm selected (user, domain, delegation)
  if ($realm == 'user') {
    $where = "AND ($uu=$uid OR ($uc=$uid AND $uu is NULL)) $multidomain";
  } else if ($realm == 'delegation') {
    $where = "AND ($dg $like '$delegation%') $multidomain";
  } else {
    $where = "$multidomain";
  }

  // Get infos from Update table
  $query = "SELECT *
    FROM EntityRight
      LEFT JOIN $table ON entityright_entity_id=$f_id
    WHERE entityright_entity='$entity'
      $where
    ORDER BY entityright_entity_id, entityright_consumer, entityright_consumer_id";
  display_debug_msg($query, $cdg_sql, 'get_update_link_entityright()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $e_id = $obm_q->f('entityright_entity_id');
    $cons = $obm_q->f('entityright_consumer');
    $cons_id = $obm_q->f('entityright_consumer_id');
    $e1[$e_id][] = "$cons-$cons_id";
  }

  // Get infos from Production table
  $table_prod = "P_$table";
  $query = "SELECT *
    FROM P_EntityRight
      LEFT JOIN $table_prod ON entityright_entity_id=$f_id
    WHERE entityright_entity='$entity'
      $where
    ORDER BY entityright_entity_id, entityright_consumer, entityright_consumer_id";
  display_debug_msg($query, $cdg_sql, 'get_update_link_entityright()');
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $e_id = $obm_q->f('entityright_entity_id');
    $cons = $obm_q->f('entityright_consumer');
    $cons_id = $obm_q->f('entityright_consumer_id');
    $e2[$e_id][] = "$cons-$cons_id";
  }

  // Process and mark each entity modified
  foreach($e1 as $e_id => $consumers) {
    if (count($e1[$e_id]) != count($e2[$e_id])) {
      $res[$e_id] = $e_id;
      $nb_link = count($e1[$e_id]);
      $nb_link_prod = count($e2[$e_id]);
      display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $entity links ; updated id=$e_id ; current #links=$nb_link_prod, new #links=$nb_link");
    } else {
      foreach ($consumers as $c_id) {
	if (! in_array($c_id, $e2[$e_id])) {
	  $res[$e_id] = $e_id;
	  display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $entity links ; updated id=$e_id ; diff=$c_id");
	  break;
	}
      }
    }
  }

  // Get empty or deleted entries (not in $e1) that were not empty (in $e2)
  foreach($e2 as $e_id => $entry) {
    if (! isset($e1[$e_id])) {
      $res[$e_id] = $e_id;
      $nb_link = 0;
      $nb_link_prod = count($e2[$e_id]);
      display_debug_msg($cmd, $cdg_exe, "domain=$domain_id ; realm=$realm ; $entity links ; updated id=$e_id ; current #links=$nb_link_prod, new #links=$nb_link");
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get update types number for a link
// Parameters:
//   - $domain_id : domain id to check
//   - $entity : entity
//   - $field  : field prefix (if not from table name) to check
//   - $realm  : realm space to check updates (user | domain | delegation)
///////////////////////////////////////////////////////////////////////////////
function get_update_link_entity($domain_id, $entity, $realm) {
  global $cdg_sql, $obm;

  if ($entity == 'group') {
    $links = get_update_link_group($domain_id, $realm);
  } else if ($entity == 'mailshare') {
    $links = get_update_link_entityright($domain_id, $entity, 'MailShare', 'mailshare', $realm);
  } else if ($entity == 'user') {
    $links = get_update_link_entityright($domain_id, 'mailbox', 'UserObm', 'userobm', $realm);
  }

  return $links;
}


/******************************************************************************
 * Store update data in update tables
 *
 * @param $params : array parameters hash
 * @return true (has update), else false
 */
function store_update_data($params) {
  global $cgp_show, $entities;

  // If update type is global, we do not store Updated tables
  if ($params['update_type'] == 'global') {
    return true;
  }

  $domain_id = $params['domain_id'];
  $realm = $params['realm'];

  $has_update = false;

  // Process entities

  foreach ($entities as $entity=>$db_info) {
    $table = $db_info[0];
    $field = $db_info[1];
    $exclude_fields = $db_info[2];
    if ($cgp_show['module']["$entity"]) {
      $e1 = get_update_entity_infos('upd', $domain_id, $table, $field, $realm, $exclude_fields);
      $e2 = get_update_entity_infos('prod', $domain_id, $table, $field, $realm, $exclude_fields);
      $nb = process_update_numbers($e1, $e2, $domain_id, $table, $realm);
      if ($nb['move'] > 0) {
        $has_update = true;
	$nb = store_update_entity_infos($e1, $e2, $domain_id, $entity, $table, $realm);
      }
    }
  }

  // Process links

  foreach ($entities as $entity=>$db_info) {
    $table = $db_info[0];
    $field = $db_info[1];
    if ($cgp_show['module']["$entity"]) {
      $links = get_update_link_entity($domain_id, $entity, $realm);
      $nb_links = count($links);
      if ($nb_links > 0) {
        $has_update = true;
	$ret = store_update_entity_links_infos($domain_id, $links, $entity);
      }
    }
  }

  return $has_update;
}


/**
 * Store update infos in Automate tables for an Entity
 *
 * @param $entity : entity to get numbers
 * @param $domain_id : domain id where to check
 * @param $table  : table where to check
 * @param $field  : field prefix (if not from table name) to check
 * @param $realm  : realm space to check updates (user | domain | delegation)
 */
function store_update_entity_infos($e1, $e2, $domain_id, $entity, $table, $realm) {
  global $cdg_sql, $obm;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;

  // Purge actual values
  if ($realm == 'user') {
    $where_purge = "AND updated_user_id='$uid'";
  } else if ($realm == 'delegation') {
    $where_purge = "AND updated_delegation like '$delegation%'";
  }
  $query = "DELETE FROM Updated
    WHERE updated_domain_id = '$domain_id'
      AND updated_table = '$table'
      $where_purge";
  display_debug_msg($query, $cdg_sql, 'store_update_entity_infos(purge)');
  $retour = $obm_q->query($query);


  // Process infos for created and updated entities
  foreach ($e1 as $id => $row) {
    if (! array_key_exists($id, $e2)) {
      // INSERT
      $query = "INSERT INTO Updated (
        updated_domain_id,
        updated_user_id,
        updated_delegation,
        updated_table,
        updated_entity_id,
        updated_type
      ) VALUES (
        '$domain_id',
        '$uid',
        '$delegation',
        '$table',
        '$id',
        'C')";
      display_debug_msg($query, $cdg_sql, 'store_update_entity_infos()');
      $retour = $obm_q->query($query);
      $nb_add++;
    } else {
      foreach($row as $fname => $fvalue) {
	if ($e2[$id][$fname] != $fvalue) {
	  // UPDATE
	  $query = "INSERT INTO Updated (
            updated_domain_id,
            updated_user_id,
            updated_delegation,
            updated_table,
            updated_entity_id,
            updated_type
          ) VALUES (
            '$domain_id',
            '$uid',
            '$delegation',
            '$table',
            '$id',
            'U')";
	  display_debug_msg($query, $cdg_sql, 'store_update_entity_infos()');
	  $retour = $obm_q->query($query);
	  $nb_upd++;
	  break;
	}
      }
    }
  }

  return $nb_add;
}


/**
 * Store update infos in Automate tables for Entity link
 *
 * @param $domain_id : domain id where to check
 * @param $links  : 
 * @param $entity : entity to get numbers
 */
function store_update_entity_links_infos($domain_id, $links, $entity) {
  global $cdg_sql, $obm;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;

  $uid = $obm['uid'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;

  if ($entity == 'group') {
    $table = 'UGroup';
  } else if ($entity == 'mailshare') {
    $table = 'MailShare';
  } else if ($entity == 'user') {
    $table = 'UserObm';
  }

  // Process infos for created and updated entities
  foreach ($links as $id) {
    // INSERT
    $query = "INSERT INTO Updatedlinks (
        updatedlinks_domain_id,
        updatedlinks_user_id,
        updatedlinks_delegation,
        updatedlinks_table,
        updatedlinks_entity,
        updatedlinks_entity_id
      ) VALUES (
        '$domain_id',
        '$uid',
        '$delegation',
        '$table',
        '$entity',
        '$id')";
    display_debug_msg($query, $cdg_sql, 'store_update_entity_links_infos()');
    $retour = $obm_q->query($query);
    $nb_add++;
  }

  return $nb_add;
}


</script>
