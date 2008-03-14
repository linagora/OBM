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
 * run_query_tools_perform_scope 
 * 
 * @param mixed $metadata 
 * @param mixed $realm 
 * @param mixed $domain 
 * @access public
 * @return void
 */
function run_query_tools_perform_scope(&$metadata, $realm, $domain, $update = false) {
  global $obm, $obmdb_dbtype;
  $rules = array('update' => array('1=1'), 'delete' => array('1=1'), 'purge' => array('1=1'));
  $del_rules = array('1=1'); 
  $like = sql_casei_like($obmdb_dbtype); 
  
  switch ($realm) {
    case 'user':
      $rules['update'][] = "(Current.$metadata[prefix]_userupdate = $obm[uid] OR 
                  (Current.$metadata[prefix]_userupdate IS NULL AND Current.$metadata[prefix]_usercreate = $obm[uid]))";
      $rules['delete'][] = "Current.deleted_user_id = $obm[uid]";
      $rules['purge'][] = "updated_user_id = $obm[uid]";
      $rules['purgelinks'][] = "updatedlinks_user_id = $obm[uid]";
      break;
    case 'delegation':
      $rules['update'][] = "Current.$metadata[prefix]_delegation $like '$obm[delegation]%'" ;
      $rules['delete'][] = "Current.deleted_delegation $like '$obm[delegation]%'";  
      $rules['purge'][] = "updated_delegation $like '$obm[delegation]%'";
      $rules['purgelinks'][] = "updatedlinks_delegation $like '$obm[delegation]%'";
      break;
    default:
      break;
  }
  if ($domain != 0) {
    $rules['update'][] = "Current.$metadata[prefix]_domain_id = $domain";
    $rules['delete'][] = "Current.deleted_domain_id = $domain";
    $rules['purge'][] = "updated_domain_id = $domain";
    $rules['purgelinks'][] = "updatedlinks_domain_id = $domain";
  }

  if(is_array($metadata['rules'])) {
    foreach($metadata['rules'] as $field => $value) {
      $rules['update'][] ="Current.$metadata[prefix]_$field = '$value'";
    }   
  }
  $rules['link'] = $rules['update'];

  if(is_array($metadata['link']['rules'])) {
    foreach($metadata['link']['rules'] as $field => $value) {
      $rules['link'][] ="Link.$field = '$value'";
    }   
  }
  if($update) {
    $userdata = array('uid' =>  $obm['uid'], 'delegation' => $obm[delegation], 'domain' => $domain );
    $return = run_query_tools_set_scope($metadata, $rules,$userdata);
  } else {
    $return = run_query_tools_get_scope($metadata, $rules);
  }
  
  return $return;
    
}

/**
 * run_query_tools_get_scope 
 * 
 * @param mixed $metadata 
 * @param mixed $rules 
 * @access public
 * @return void
 */
function run_query_tools_get_scope(&$metadata, &$rules) {
  global $obm, $cdg_sql;

  $obm_qm = new DB_OBM;
  $db_type = $obm_qm->type;
  $like = sql_casei_like($db_type); 

  $diff = array();
  $primaries = array();
  $join = array();
  
  $return = array('created' => 0, 'deleted' => 0, 'updated' => 0, 'links' => 0, 'diff' => 0);
  $ids = array('0');
  /**
   * PERFORM UPDATED ENTITIES 
   */
  
  $fields = $obm_qm->metadata($metadata['table'], false);
  foreach ($fields as $field) {
    if(strpos($field['flags'],'primary_key') === false) {
      if(!isset($metadata['exclude'][str_replace($metadata['prefix'].'_','',$field['name'])])) { 
        $diff[] = "Production.$field[name] != Current.$field[name]";
      }
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Current.$field[name] IS NULL";
      $primaries['production'] = "Production.$field[name]";
      $primaries['current'] = "Current.$field[name]";
      $join[] = "Current.$field[name] = Production.$field[name]";
    }
  }

  $query = "
      SELECT count(*) as updated
      FROM $metadata[table] as Current
      JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).")";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(updated)');
  $obm_qm->query($query);
  $obm_qm->next_record();
  
  $return['updated'] = $obm_qm->f('updated');
  $return['diff'] += $return['updated'];

  /**
   * PERFORM CREATED ENTITIES 
   */

  $query = "
      SELECT count(*) as created 
      FROM $metadata[table] as Current
      LEFT JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).") 
      AND $primaries[production] IS NULL";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(created)');
  $obm_qm->query($query);
  $obm_qm->next_record();

  $return['created'] = $obm_qm->f('created');
  $return['diff'] += $return['created'];

  /**
   * PERFORM DELETED ENTITIES 
   */

  $query = "
    SELECT count(*) as deleted FROM Deleted as Current
    JOIN P_$metadata[table] as Production ON deleted_entity_id = $primaries[production] AND deleted_table = '$metadata[table]'
    AND (".implode(' AND ',$rules['delete']).")
    ";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(deleted)');
  $obm_qm->query($query);
  $obm_qm->next_record();
  $return['deleted'] = $obm_qm->f('deleted');
  $return['diff'] += $return['deleted'];
  
  if(!isset($metadata['link'])) {
    return $return;
  }
  /**
   * PERFORM UPDATED LINKS 
   */
  $diff = array();
  $join = array();
  $fields = $obm_qm->metadata($metadata['link']['table'], false);
  foreach ($fields as $field) {
    if(strpos($field['flags'],'primary_key') === false) {
      $diff[] = "Production.$field[name] != Link.$field[name]";
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Link.$field[name] IS NULL";
      $join[] = "Link.$field[name] = Production.$field[name]";
    }
  }
  // Union to get all changes ! (eg: links that have been deleted)
  $query = "
    SELECT count(distinct Link.".$metadata['link']['id'].") as links 
      FROM ".$metadata['link']['table']." as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN P_".$metadata['link']['table']." as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")
  UNION
      SELECT count(distinct Link.".$metadata['link']['id'].") as links 
      FROM P_".$metadata['link']['table']." as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN ".$metadata['link']['table']." as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(links, '.$metadata['link']['table'].')');
  $obm_qm->query($query);
  $obm_qm->next_record();
  $return['links'] = $obm_qm->f('links');
  // Add Union result (bi directionnal => 2 results)
  $obm_qm->next_record();
  $return['links'] .= $obm_qm->f('links');
  $return['diff'] += $return['links'];

  return $return;
}

/**
 * run_query_tools_get_scope 
 * 
 * @param mixed $metadata 
 * @param mixed $rules 
 * @access public
 * @return void
 */
function run_query_tools_set_scope(&$metadata, &$rules, &$userdata) {
  global $obm, $cdg_sql;

  $obm_qm = new DB_OBM;
  $db_type = $obm_qm->type;
  $like = sql_casei_like($db_type); 

  $diff = array();
  $primaries = array();
  $join = array();

  $return = 0;

  /**
  * PURGE 
  */
  
  $query = "DELETE FROM Updated WHERE updated_domain_id = '$userdata[domain]'
      AND updated_table = '$metadata[table]' AND ".implode(' AND ', $rules['purge'])."";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(purge)');
  $obm_qm->query($query);
  $query = "DELETE FROM Updatedlinks WHERE updatedlinks_domain_id = '$userdata[domain]'
      AND updatedlinks_table = '$metadata[table]' AND ".implode(' AND ', $rules['purgelinks'])."";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(purge)');
  $obm_qm->query($query);

  /**
   * PERFORM UPDATED ENTITIES 
   */
  
  $fields = $obm_qm->metadata($metadata['table'], false);
  foreach ($fields as $field) {
    if(strpos($field['flags'],'primary_key') === false) {
      if(!isset($metadata['exclude'][str_replace($metadata['prefix'].'_','',$field['name'])])) { 
        $diff[] = "Production.$field[name] != Current.$field[name]";
      }
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Current.$field[name] IS NULL";
      $primaries['production'] = "Production.$field[name]";
      $primaries['current'] = "Current.$field[name]";
      $join[] = "Current.$field[name] = Production.$field[name]";
    }
  }
  
  $query = "
    INSERT INTO Updated (
        updated_domain_id,
        updated_user_id,
        updated_delegation,
        updated_table,
        updated_entity_id,
        updated_type
    ) SELECT 
      $userdata[domain],
      $userdata[uid],
      '$userdata[delegation]',
      '$metadata[table]',
      $primaries[current], 
      'U'
    FROM $metadata[table] as Current
      JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).")";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(updated)');
  $obm_qm->query($query);
  $return += $obm_qm->affected_rows();

  /**
   * PERFORM CREATED ENTITIES 
   */

  $query = "
    INSERT INTO Updated  (
        updated_domain_id,
        updated_user_id,
        updated_delegation,
        updated_table,
        updated_entity_id,
        updated_type
    ) SELECT 
      $userdata[domain],
      $userdata[uid],
      '$userdata[delegation]',
      '$metadata[table]',
      $primaries[current], 
      'C'
      FROM $metadata[table] as Current
      LEFT JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).") 
      AND $primaries[production] IS NULL";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(created)');
  $obm_qm->query($query);
  $return += $obm_qm->affected_rows();
  
  if(!isset($metadata['link'])) {
    return $return;
  }
  $diff = array();
  $join = array();
  $fields = $obm_qm->metadata($metadata['link']['table'], false);
  foreach ($fields as $field) {
    if(strpos($field['flags'],'primary_key') === false) {
      $diff[] = "Production.$field[name] != Link.$field[name]";
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Link.$field[name] IS NULL";
      $join[] = "Link.$field[name] = Production.$field[name]";
    }
  }
  $query = "
    INSERT INTO Updatedlinks (
        updatedlinks_domain_id,
        updatedlinks_user_id,
        updatedlinks_delegation,
        updatedlinks_table,
        updatedlinks_entity,
        updatedlinks_entity_id            
      )
      SELECT 
      $userdata[domain],
      $userdata[uid],
      '$userdata[delegation]',
      '$metadata[table]',
      $primaries[current],
      Link.".$metadata['link']['id']." 
    FROM ".$metadata['link']['table']." as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN P_".$metadata['link']['table']." as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")
      GROUP BY Link.".$metadata['link']['id']." ";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(links)');
  $obm_qm->query($query);
  $obm_qm->next_record();
  $return += $obm_qm->affected_rows();

  return $return;
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
      $nb = run_query_tools_perform_scope($db_info,$realm,$domain_id,true); 
      if($nb > 0) {
        $has_update = true;
      }
    }
  }

  return $has_update;
}


</script>
