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

  $update = $tools["update"];
  $scope = update_scope();

  // Si pas de modifications trouvées ! (on ne devrait pas arriver ici !)
  if ( (! is_array($scope)) || (count($scope) == 0) )
    return false;

  // Creation de la commande d'execution de l'automate de mise a jour
  $cmd = $cmd_update;

  if ($scope["all"]) {
    $cmd .= " --all";
  } else {
    if ($scope["ldap"]) $cmd .= " --ldap";
    if ($scope["cyrus"]) $cmd .= " --cyrus";
    if ($scope["postfix"]) $cmd .= " --postfix";
    if ($scope["amavis"]) $cmd .= " --amavis";
    if ($scope["alias"]) $cmd .= " --alias";
    if ($scope["squid"]) $cmd .= " --squid";
    if ($scope["dns"]) $cmd .= " --dns";
    if ($scope["network"]) $cmd .= " --network";
    if ($scope["vpn"]) $cmd .= " --vpn";
    if ($scope["firewall"]) $cmd .= " --firewall";
  }

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

</script>
