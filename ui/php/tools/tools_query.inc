<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OB - File : tools_query.inc                                               //
//    - Desc : Tools query & db File                                         //
// 2002-09-26 Pierre Baudracco                                               //
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

  display_debug_msg($query, $cdg_sql, "get_tools_remote_access()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $ra = $obm_q->f("obminfo_value");

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

  $remote_access = $tools["remote_access"];

  $query = "UPDATE ObmInfo SET
    obminfo_value = '$remote_access'
  WHERE obminfo_name = 'remote_access'";

  display_debug_msg($query, $cdg_sql, "run_query_tools_remote_update()");
  $usr_q = new DB_OBM;
  $retour = $usr_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Update system configuration execution
// Parameters:
//   - $tools[]  : entry values
//     keys used : update
///////////////////////////////////////////////////////////////////////////////
function run_query_tools_update_update($tools) {
  global $cdg_sql, $cdg_exe, $cmd_update, $cmd_db_update;

  $state = get_update_state();
  if ($state != 1) {
    return false;
  }     
  
  $update = $tools['update'];

  // Creation de la commande d'execution de l'automate de mise a jour
  $cmd = $cmd_update.' --all';
  
  //  $cmd .= " 2>/dev/null >&- <&- >/dev/null &";
  $cmd .= " >/dev/null 2>&1 &";
  
  // Basculement des données de mise à jour en production
  display_debug_msg($cmd_db_update, $cdg_exe, 'run_query_tools_update_update(1)');
  $ret = exec($cmd_db_update);

  // Mise a jour de l'indicateur de modifications
  update_update_state();

  display_debug_msg($cmd, $cdg_exe, 'run_query_tools_update_update(2)');
  unset($tmp);
  exec($cmd, $tmp, $ret);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Update system configuration execution
// Parameters:
//   - $tools[]  : entry values
//     keys used : update
///////////////////////////////////////////////////////////////////////////////
function exec_tools_update_update($tools) {
  global $obm, $cdg_sql, $cdg_exe, $cmd_update, $cmd_db_update;

  $state = get_update_state();
  if ($state != 1) {
    return false;
  }     

  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];
  $delegation = $obm['delegation'];

  $update_type = $tools['update_type'];
  $realm = $tools['realm'];
  $update = $tools['update'];

  // Creation de la commande d'execution de l'automate de mise a jour
  $cmd = $cmd_update." --incremental --domain $domain_id";
  
  if ($realm == 'user') {
    $cmd .= " --user $uid";
  } else if ($realm == 'delegation') {
    $cmd .= " --delegation '$delegation'";
  }

  $cmd .= " >/dev/null 2>&1 &";
  
  // Mise a jour de l'indicateur de modifications
  update_update_state();

  display_debug_msg($cmd, $cdg_exe, 'exec_tools_update_update()');
  unset($tmp);
  exec($cmd, $tmp, $ret);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Cancel update system configuration
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function run_query_tools_update_cancel() {
  global $cdg_sql, $cdg_exe, $cmd_db_cancel;

  // Basculement des données de mise à jour en production
  display_debug_msg($cmd_db_cancel, $cdg_exe, "run_query_tools_update_cancel()");
  unset($tmp);
  exec($cmd_db_cancel, $tmp, $ret);

  // Mise a jour de l'indicateur de modifications
  update_update_state();

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Check if a global system configuration execution is possible
// Parameters:
//   - $tools[]  : entry values
//     keys used : update
// Returns:
//   - (true | false) : true if ok else false
///////////////////////////////////////////////////////////////////////////////
function check_tools_update_context() {
  global $cmd_check_update;

  $update_ok = true;

  // Il ne faut pas qu'une mise à jour soit déjà en cours
  $ps = shell_exec("$cmd_check_update");

  if ($ps)
    $update_ok = false;
  
  return $update_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Get update types number for an entity
// Parameters:
//   - $table  : table where to check
//   - $field  : field prefix (if not from table name) to check
//   - $realm  : realm space to check updates (user | domain | delegation)
///////////////////////////////////////////////////////////////////////////////
function get_update_infos(&$e1, &$e2, $table, $field="", $realm) {
  global $cdg_sql, $obm;

  $uid = $obm['uid'];

  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  if ($field != "") {
    $f = $field;
    $uc = "${field}_usercreate";
    $uu = "${field}_userupdate";
    $dg = "${field}_delegation";
  } else {
    $f = strtolower($table);
    $uc = "${f}_usercreate";
    $uu = "${f}_userupdate";
    $dg = "${f}_delegation";
  }

  $multidomain = sql_multidomain("$f");

  // Restrict updates to the realm selected (user, domain, delegation)
  if ($realm == "user") {
    $where_upd = "WHERE ($uu=$uid OR ($uc=$uid AND $uu is NULL)) $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  } else if ($realm == "delegation") {
    $where_upd = "WHERE ($dg $like '$delegation%') $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  } else {
    $where_upd = "WHERE 1=1 $multidomain";
    $where_prod = "WHERE 1=1 $multidomain";
  }

  // Get infos from Update table
  $query = "SELECT * FROM $table $where_upd";
  display_debug_msg($query, $cdg_sql, "get_update_numbers()");
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f("0");  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // optimiser : verifier que fname pas un chiffre ?
      $e1[$id]["$fname"] = $fvalue;
    }
  }

  // Get infos from Production table
  $table_prod = "P_$table";
  $query = "SELECT * FROM $table_prod $where_prod";
  display_debug_msg($query, $cdg_sql, "get_update_numbers()");
  $retour = $obm_q->query($query);

  while($obm_q->next_record()) {
    $id = $obm_q->f("0");  // Id
    $e = $obm_q->Record;
    foreach ($e as $fname => $fvalue) {
      // optimiser : verifier que fname pas un chiffre ?
      $e2[$id]["$fname"] = $fvalue;
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Process update infos to get number for an entity
// Parameters:
//   - $entity : entity to get numbers
//   - $realm  : realm space to check updates (user | domain | delegation)
///////////////////////////////////////////////////////////////////////////////
function process_update_numbers($e1, $e2, $entity, $realm) {
  global $cdg_sql, $obm;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;

  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);

  // Process infos for created and updated entities
  foreach ($e1 as $id => $row) {
    if (! array_key_exists($id, $e2)) {
      $nb_add++;
    } else {
      foreach($row as $fname => $fvalue) {
	if ($e2[$id][$fname] != $fvalue) {
	  $nb_upd++;
	  break;
	}
      }
    }
  }

  // Get infos for deleted entities
  if ($realm == "user") {

    // Get the delete from the Deleted table
    $query = "SELECT count(*) as nb FROM Deleted
     WHERE deleted_user_id=$uid
       AND deleted_entity='$entity'";
    display_debug_msg($query, $cdg_sql, "run_query_user_delete(auto)");
    $retour = $obm_q->query($query);
    $obm_q->next_record();
    $nb_del = $obm_q->f("nb");

  } else if ($realm == "delegation") {

    // Get the delete from the Deleted table
    $query = "SELECT count(*) as nb FROM Deleted
     WHERE deleted_delegation $like '$delegation%'
       AND deleted_entity='$entity'
       AND deleted_domain_id='$domain_id'";
    display_debug_msg($query, $cdg_sql, "run_query_user_delete(auto)");
    $retour = $obm_q->query($query);
    $obm_q->next_record();
    $nb_del = $obm_q->f("nb");

  } else {
    // Case domain, just count lines present in prod, but not present in upd
    foreach ($e2 as $id => $row) {
      if (! array_key_exists($id, $e1)) {
	$nb_del++;
      }
    }
  }
  
  $nb["created"] = $nb_add;
  $nb["updated"] = $nb_upd;
  $nb["deleted"] = $nb_del;
  $nb["move"] = $nb_add + $nb_upd + $nb_del;

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get update types number for an entity
// Parameters:
//   - $entity : entity to get numbers
//   - $table  : table where to check
//   - $field  : field prefix (if not from table name) to check
//   - $realm  : realm space to check updates (user | domain | delegation)
///////////////////////////////////////////////////////////////////////////////
function get_update_entity_numbers($entity, $table, $field="", $realm) {
  global $cdg_sql, $obm;

  $e1 = array();
  $e2 = array();

  get_update_infos($e1, $e2, $table, $field, $realm);
  $nb = process_update_numbers($e1, $e2, $entity, $realm);

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Validate update for one entity
// Parameters:
//   - $entity : entity to get numbers
//   - $table  : table where to check
//   - $field  : field prefix (if not from table name) to check
//   - $realm  : realm space to check updates (user | domain | delegation)
///////////////////////////////////////////////////////////////////////////////
function validate_update_entity($entity, $table, $field="", $realm) {
  global $cdg_sql, $obm;

  $e1 = array();
  $e2 = array();

  get_update_infos($e1, $e2, $table, $field, $realm);
  $nb = store_update_infos($e1, $e2, $entity, $table,  $realm);

  return $nb;
}


/**
 * Store update infos in Automate tables for an Entity
 *
 * @param $entity : entity to get numbers
 * @param $table  : table where to check
 * @param $field  : field prefix (if not from table name) to check
 * @param $realm  : realm space to check updates (user | domain | delegation)
 */
function store_update_infos($e1, $e2, $entity, $table, $realm) {
  global $cdg_sql, $obm;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;

  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];
  $delegation = $obm['delegation'];
  $obm_q = new DB_OBM;

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
	  $retour = $obm_q->query($query);
	  $nb_upd++;
	  break;
	}
      }
    }
  }

  return $nb_add;
}


</script>
