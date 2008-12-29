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
  if ($obm['domain_global'] === true) {
    $update_type == 'global';
  }

  // Update command creation
  //$cmd = $cmd_update." --$update_type --domain $domain_id";
  // Modifying during test period - global mode is always done
  $cmd = $cmd_update." --global --domain $domain_id";
  
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
  if ($update_type == 'global' && isset($domain_id)) {
    $domain = of_domain_get_domain_infos($domain_id);
    if (($domain['global'] === false) && (! in_array('domain', $admin_realm))) {
      $err['msg'] = $l_error_permission;
      return false;
    }
  }
  
  // Check if update is locked
  $update_lock = get_update_lock();
  if ($update_lock == '1') {
    $err['msg'] = $l_upd_already_running;
    return false;
  }

  // An update must not already running
  $ps = shell_exec($cmd_check_update);

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
function check_tools_update_need_global_update($domain_id = NULL, $domain_global = false) {
  global $cdg_sql, $obm;

  $e1 = array();
  $e2 = array();

  $obm_q = new DB_OBM;

  if (!is_null($domain_id) && $domain_global === false) {
    $where = "AND Domain.domain_id = '$domain_id'";
  }

  // Domain table check
  $query = "SELECT 
    count(*) as diff
    FROM Domain LEFT JOIN P_Domain ON P_Domain.domain_id = Domain.domain_id
    WHERE ( Domain.domain_name != P_Domain.domain_name 
        OR (Domain.domain_name IS NULL AND P_Domain.domain_name IS NOT NULL)
        OR (Domain.domain_name IS NOT NULL AND P_Domain.domain_name IS NULL)
        OR Domain.domain_alias != P_Domain.domain_alias
        OR (Domain.domain_alias IS NULL AND P_Domain.domain_alias IS NOT NULL) 
        OR (Domain.domain_alias IS NOT NULL AND P_Domain.domain_alias IS NULL) 
        )
    $where";
  
  $obm_q->query($query);
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $obm_q->next_record();
  if($obm_q->f('diff') > 0) {
    return true;
  }
  // Samba table check
  if (!is_null($domain_id) && !$domain_global) {
    $did = sql_parse_id($domain_id, true);
    $where = "AND domainentity_domain_id $did ";
  }
  
  // Get infos from Update table;
  $query = " SELECT
    count(*) as diff
    FROM ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    LEFT JOIN P_ServiceProperty ON ServiceProperty.serviceproperty_id = P_ServiceProperty.serviceproperty_id
    WHERE ServiceProperty.serviceproperty_service = 'samba' AND 
    ((ServiceProperty.serviceproperty_value != P_ServiceProperty.serviceproperty_value 
    OR (ServiceProperty.serviceproperty_value IS NULL AND P_ServiceProperty.serviceproperty_value IS NOT NULL)
    OR (P_ServiceProperty.serviceproperty_value IS NOT NULL AND ServiceProperty.serviceproperty_value IS NULL)))
    $where
  ";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  $obm_q->next_record();
  if($obm_q->f('diff') > 0) {
    return true;
  }

    // Samba table check
  if (!is_null($domain_id) && !$domain_global) {
    $did = sql_parse_id($domain_id, true);
    $where = "AND domainentity_domain_id $did ";
  }

  // Get infos from Update table;
  $query = " SELECT
    count(*) as diff
    FROM P_ServiceProperty 
    INNER JOIN DomainEntity ON domainentity_entity_id = serviceproperty_entity_id
    LEFT JOIN ServiceProperty ON ServiceProperty.serviceproperty_id = P_ServiceProperty.serviceproperty_id
    WHERE ServiceProperty.serviceproperty_service = 'samba' AND 
    ((ServiceProperty.serviceproperty_value != P_ServiceProperty.serviceproperty_value 
    OR (ServiceProperty.serviceproperty_value IS NULL AND P_ServiceProperty.serviceproperty_value IS NOT NULL)
    OR (P_ServiceProperty.serviceproperty_value IS NOT NULL AND ServiceProperty.serviceproperty_value IS NULL)))
    $where
  ";
  display_debug_msg($query, $cdg_sql, 'check_tools_update_need_global_updated()');
  $retour = $obm_q->query($query);

  $obm_q->next_record();
  if($obm_q->f('diff') > 0) {
    return true;
  }

  return false;
}

/**
 * exec_tools_cancel_update 
 * 
 * @param mixed $params 
 * @access public
 * @return void
 */
function exec_tools_cancel_update($params) {
  global $obminclude;
  
  $entity = $params['entity'];
  $state  = $params['state'];

  switch ($state) {
    case 'created':
      require "$obminclude/of/of_category.inc";
      require "../{$entity}/{$entity}_query.inc";
      $function = "run_query_{$entity}_delete";
      return $function($params['id']);
    case 'updated':
      return run_query_tools_cancel_update($entity, $params['id']);
    case 'deleted':
      return run_query_tools_cancel_delete($entity, $params['id']); 
  }
}

/**
 * run_query_tools_cancel_update 
 * 
 * @param string $entity
 * @param string $id
 * @access public
 * @return void
 */
function run_query_tools_cancel_update($entity, $id) {
  global $entities;

  $obm_qm = new DB_OBM;
  $metadata = $entities[$entity];
  $query = "SELECT * FROM P_$metadata[table] WHERE $metadata[prefix]_id = '$id'";
  $obm_qm->query($query);
  $obm_qm->next_record();
  $old_values = $obm_qm->Record;

  $diffs = array();
  $fields = $obm_qm->metadata($metadata['table'], false);
  foreach ($fields as $field) {
    if (strpos(strtolower($field['flags']),'primary') === false
        && !isset($metadata['exclude'][str_replace($metadata['prefix'].'_','',$field['name'])])) {
      $old_value = ($old_values[$field['name']] === null) ? 'NULL' : "'".$old_values[$field['name']]."'";
      $diffs[] = "$field[name] = $old_value";
    }
  }
  $query = "UPDATE $metadata[table] SET ".implode(',', $diffs)." WHERE $metadata[prefix]_id = '$id'";
  return $obm_qm->query($query);
}

/**
 * run_query_tools_cancel_delete 
 * 
 * @param string $entity
 * @param string $id
 * @access public
 * @return void
 */
function run_query_tools_cancel_delete($entity, $id) {
  global $entities;

  $obm_qm = new DB_OBM;
  $metadata = $entities[$entity];
  $query = "INSERT INTO $metadata[table] SELECT * FROM P_$metadata[table] WHERE $metadata[prefix]_id = '$id'";
  if (!$obm_qm->query($query)) return false;
  $query = "DELETE FROM Deleted WHERE deleted_table = '$metadata[table]' AND deleted_entity_id = '$id'";
  return $obm_qm->query($query);
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
function run_query_tools_perform_scope(&$metadata, $realm, $domain, $global=false, $update = false) {
  global $obm, $obmdb_dbtype;
  $rules = array('update' => array('1=1'), 'delete' => array('1=1'), 'purge' => array('1=1'));
  $del_rules = array('1=1'); 
  $like = sql_casei_like($obmdb_dbtype); 
  $uid = sql_parse_id($obm['uid'], true);
  $did = sql_parse_id($domain, true); 
  switch ($realm) {
    case 'user':
      $rules['update'][] = "(Current.$metadata[prefix]_userupdate $uid OR 
                  (Current.$metadata[prefix]_userupdate IS NULL AND Current.$metadata[prefix]_usercreate $uid))"; 
      $rules['delete'][] = "Current.deleted_user_id $uid"; 
      $rules['purge'][] = "updated_user_id $uid"; 
      $rules['purgelinks'][] = "updatedlinks_user_id $uid"; 
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
  if (!$global) {
    $rules['update'][] = "Current.$metadata[prefix]_domain_id $did"; 
    $rules['delete'][] = "Current.deleted_domain_id $did"; 
    $rules['purge'][] = "updated_domain_id $did"; 
    $rules['purgelinks'][] = "updatedlinks_domain_id $did"; 
  }

  if(is_array($metadata['rules'])) {
    foreach($metadata['rules'] as $field => $value) {
      if(is_array($value)) {
        $rules['update'][] ="Current.$metadata[prefix]_$field IN ('".implode("','", $value)."')";
      } else {
        $rules['update'][] ="Current.$metadata[prefix]_$field = '$value'";
      }
    }   
  }
  $rules['link'] = $rules['update'];

  if(is_array($metadata['link']['rules'])) {
    foreach($metadata['link']['rules'] as $field => $value) {
      if(is_array($value)) {
        $rules['link'][] ="Link.$field IN ('".implode("','", $value)."')";
      } else {
        $rules['link'][] ="Link.$field = '$value'";
      }
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
  
  $current_columns = array("Current.$metadata[prefix]_id");
  $prod_columns = array("Production.$metadata[prefix]_id");
  foreach ($metadata['display'] as $column) {
    $current_columns[] = "Current.$metadata[prefix]_$column";
    $prod_columns[] = "Production.$metadata[prefix]_$column";
  }
  $current_columns = implode(', ', $current_columns);
  $prod_columns = implode(', ', $prod_columns);
  /**
   * PERFORM UPDATED ENTITIES 
   */
  
  $fields = $obm_qm->metadata($metadata['table'], false);
  foreach ($fields as $field) {
    if(strpos(strtolower($field['flags']),'primary') === false) {
      if(!isset($metadata['exclude'][str_replace($metadata['prefix'].'_','',$field['name'])])) { 
        $diff[] = "Production.$field[name] != Current.$field[name]";
        $diff[] = "(Production.$field[name] IS NULL AND Current.$field[name] IS NOT NULL)";
        $diff[] = "(Production.$field[name] IS NOT NULL AND Current.$field[name] IS NULL)";
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
      SELECT DISTINCT $current_columns
      FROM $metadata[table] as Current
      JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).")";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(updated)');
  $obm_qm->query($query);
  
  $return['updated'] = 0;
  while ($obm_qm->next_record()) {
    $return['updated_entities'][] = get_record_representation($metadata, $obm_qm);
    $return['updated']++;
  }
  $return['diff'] += $return['updated'];

  /**
   * PERFORM CREATED ENTITIES 
   */

  $query = "
      SELECT $current_columns
      FROM $metadata[table] as Current
      LEFT JOIN P_$metadata[table] as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['update']).") 
      AND $primaries[production] IS NULL";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(created)');
  $obm_qm->query($query);
  
  $return['created'] = 0;
  while ($obm_qm->next_record()) {
    $return['created_entities'][] = get_record_representation($metadata, $obm_qm);
    $return['created']++;
  }
  $return['diff'] += $return['created'];

  /**
   * PERFORM DELETED ENTITIES 
   */

  $query = "
    SELECT $prod_columns as deleted FROM Deleted as Current
    JOIN P_$metadata[table] as Production ON deleted_entity_id = $primaries[production] AND deleted_table = '$metadata[table]'
    AND (".implode(' AND ',$rules['delete']).")
    ";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(deleted)');
  $obm_qm->query($query);
  
  $return['deleted'] = 0;
  while ($obm_qm->next_record()) {
    $return['deleted_entities'][] = get_record_representation($metadata, $obm_qm);
    $return['deleted']++;
  }
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
    if(strpos(strtolower($field['flags']),'primary') === false) {
      $diff[] = "Production.$field[name] != Link.$field[name]";
        $diff[] = "(Production.$field[name] IS NULL AND Link.$field[name] IS NOT NULL)";
        $diff[] = "(Production.$field[name] IS NOT NULL AND Link.$field[name] IS NULL)";
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Link.$field[name] IS NULL";
      $join[] = "Link.$field[name] = Production.$field[name]";
    }
  }
  if($metadata['link']['join']) {
    $linkTable = '(
      SELECT '.$metadata['link']['table'].'.*, '.$metadata['link']['id'].' 
      FROM '.$metadata['link']['table'].' 
      INNER JOIN '.$metadata['link']['join']['table'].' ON '.$metadata['link']['join']['id'] .' = '.$metadata['link']['join']['joinId'].')';
    $productionTable = '(
      SELECT P_'.$metadata['link']['table'].'.*, '.$metadata['link']['id'].' 
      FROM P_'.$metadata['link']['table'].' 
      INNER JOIN P_'.$metadata['link']['join']['table'].' ON '.$metadata['link']['join']['id'] .' = '.$metadata['link']['join']['joinId'].')'; 
  } else {
    $linkTable = $metadata['link']['table'];
    $productionTable = "P_".$metadata['link']['table'];
  }
  // Union to get all changes ! (eg: links that have been deleted)
  $query = "
    SELECT count(distinct Link.".$metadata['link']['id'].") as links 
      FROM $linkTable as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN $productionTable as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")
  UNION
      SELECT count(distinct Link.".$metadata['link']['id'].") as links 
      FROM $productionTable as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN $linkTable as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")";

  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(links, '.$metadata['link']['table'].')');
  $obm_qm->query($query);
  $obm_qm->next_record();
  $return['links'] = $obm_qm->f('links');
  // Add Union result (bi directionnal => 2 results)
  $obm_qm->next_record();
  $return['links'] += $obm_qm->f('links');
  $return['diff'] += $return['links'];

  return $return;
}

/**
 * get_record_representation 
 * 
 * @param mixed $metadata 
 * @param mixed $obm_qm
 * @access public
 * @return void
 */
function get_record_representation(&$metadata, &$obm_qm) {
  global $l_unspecified;
  
  $values = array();
  foreach ($metadata['display'] as $column) {
    $value = $obm_qm->f("$metadata[prefix]_$column");
    if (empty($value)) {
      $value = "[{$column} {$l_unspecified}]";
    }
    $values[] = $value;
  }
  return array(
    'id' => $obm_qm->f("$metadata[prefix]_id"),
    'repr' => vsprintf($metadata['display_format'], $values)
  );
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
  $rules['purgelinks'][] = '1=1';
  /**
  * PURGE 
  */
  
  $query = "DELETE FROM Updated WHERE updated_domain_id = $userdata[domain]
      AND updated_table = '$metadata[table]' AND ".implode(' AND ', $rules['purge'])."";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(purge)');
  $obm_qm->query($query);
  $query = "DELETE FROM Updatedlinks WHERE updatedlinks_domain_id = $userdata[domain]
      AND updatedlinks_table = '$metadata[table]' AND ".implode(' AND ', $rules['purgelinks'])."";
  display_debug_msg($query, $cdg_sql, 'run_query_tools_get_scope(purge)');
  $obm_qm->query($query);

  /**
   * PERFORM UPDATED ENTITIES 
   */
  
  $fields = $obm_qm->metadata($metadata['table'], false);
  foreach ($fields as $field) {
    if(strpos(strtolower($field['flags']),'primary') === false) {
      if(!isset($metadata['exclude'][str_replace($metadata['prefix'].'_','',$field['name'])])) { 
        $diff[] = "Production.$field[name] != Current.$field[name]";
        $diff[] = "(Production.$field[name] IS NULL AND Current.$field[name] IS NOT NULL)";
        $diff[] = "(Production.$field[name] IS NOT NULL AND Current.$field[name] IS NULL)";
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
    if(strpos(strtolower($field['flags']),'primary') === false) {
      $diff[] = "(Production.$field[name] != Link.$field[name])";
      $diff[] = "(Production.$field[name] IS NULL AND Link.$field[name] IS NOT NULL)";
      $diff[] = "(Production.$field[name] IS NOT NULL AND Link.$field[name] IS NULL)";
    } else {
      $diff[] = "Production.$field[name] IS NULL";
      $diff[] = "Link.$field[name] IS NULL";
      $join[] = "Link.$field[name] = Production.$field[name]";
    }
  }

  if($metadata['link']['join']) {
    $linkTable = '(
      SELECT '.$metadata['link']['table'].'.*, '.$metadata['link']['id'].' 
      FROM '.$metadata['link']['table'].' 
      INNER JOIN '.$metadata['link']['join']['table'].' ON '.$metadata['link']['join']['id'] .' = '.$metadata['link']['join']['joinId'].')';
    $productionTable = '(
      SELECT P_'.$metadata['link']['table'].'.*, '.$metadata['link']['id'].' 
      FROM P_'.$metadata['link']['table'].' 
      INNER JOIN P_'.$metadata['link']['join']['table'].' ON '.$metadata['link']['join']['id'] .' = '.$metadata['link']['join']['joinId'].')'; 
  } else {
    $linkTable = $metadata['link']['table'];
    $productionTable = "P_".$metadata['link']['table'];
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
      '$metadata[prefix]',
      Link.".$metadata['link']['id']." 
    FROM $linkTable as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN $productionTable as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")
      GROUP BY Link.".$metadata['link']['id']."
  UNION
      SELECT 
      $userdata[domain],
      $userdata[uid],
      '$userdata[delegation]',
      '$metadata[table]',
      '$metadata[prefix]',
      Link.".$metadata['link']['id']." 
    FROM $productionTable as Link 
      JOIN $metadata[table] as Current ON $primaries[current] = Link.".$metadata['link']['id']."
      LEFT JOIN $linkTable as Production ON ".implode(' AND ', $join)."
      WHERE (".implode(' OR ',$diff).") AND (".implode(' AND ', $rules['link']).")
      GROUP BY $primaries[current], Link.".$metadata['link']['id'];

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
  $d = of_domain_get_domain_infos($domain_id);
  $has_update = false;

  // Process entities
  foreach ($entities as $entity=>$db_info) {
    $table = $db_info[0];
    $field = $db_info[1];
    $exclude_fields = $db_info[2];
    if ($cgp_show['module']["$entity"]) {
      $nb = run_query_tools_perform_scope($db_info,$realm,$domain_id, $d['global'], true); 
      if($nb > 0) {
        $has_update = true;
      }
    }
  }

  return $has_update;
}


</script>
