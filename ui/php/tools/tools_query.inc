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
  
  $update = $tools["update"];

  // Creation de la commande d'execution de l'automate de mise a jour
  $cmd = $cmd_update." --all";
  
  //  $cmd .= " 2>/dev/null >&- <&- >/dev/null &";
  $cmd .= " >/dev/null 2>&1 &";
  
  // Basculement des données de mise à jour en production
  display_debug_msg($cmd_db_update, $cdg_exe, "run_query_tools_update_update(1)");
  $ret = exec($cmd_db_update);

  // Mise a jour de l'indicateur de modifications
  update_update_state();

  display_debug_msg($cmd, $cdg_exe, "run_query_tools_update_update(2)");
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
//   - $table  : table to check
//   - $target : target to check ("admin" | "domain")
///////////////////////////////////////////////////////////////////////////////
function get_update_numbers($table, $target="admin") {
  global $cdg_sql;

  $nb_add = 0;
  $nb_upd = 0;
  $nb_del = 0;
  $e1 = array();
  $e2 = array();
  $obm_q = new DB_OBM;

  // Update infos
  $query = "SELECT * FROM $table";
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

  // Prod infos
  $table_prod = "P_$table";
  $query = "SELECT * FROM $table_prod";
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

  
  $nb["created"] = $nb_add;
  $nb["updated"] = $nb_upd;
  $nb["deleted"] = $nb_del;
  $nb["move"] = $nb_add + $nb_upd + $nb_del;

  return $nb;
}


</script>
