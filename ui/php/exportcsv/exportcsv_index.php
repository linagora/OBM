<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : exportcsv_index.php                                          //
//     - Desc : OBM CSV export Index File (used by OBM_DISPLAY)              //
// 2003-07-23 - PB - Aliacom                                                 //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Session, Auth, Perms Management                                           //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$module = "exportcsv";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
$params = get_param_export();
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main program                                                              //
//    Action : (index, document)                                             //
///////////////////////////////////////////////////////////////////////////////
if (($action == "index") || ($action == "")) {
  echo "Action incorrecte";
  dis_end();

} elseif ($action == "message")  {
///////////////////////////////////////////////////////////////////////////////
  dis_head("Téléchargement");        // Head & Body
  echo "Téléchargement en cours";
  dis_end();

} elseif ($action == "export_page")  {
///////////////////////////////////////////////////////////////////////////////
  $emodule = $params["module"];
  if ($emodule != "") {
    require_once("$obminclude/lang/$set_lang/${emodule}.inc");
    require_once("$path/$emodule/${emodule}_display.inc");
  } else {
    $emodule = "obm";
  }
  $entity = $params["entity"];
  if ($entity == "") {
    $entity = $emodule;
  }
  $first_row = $params["first_row"];
  $nb_rows = $params["nb_rows"];
  $query = stripslashes($params["query"]);
  if ($ctu_sql_limit && ($nb_rows > $set_rows)) {
    // We remove the limit clause
    $query = preg_replace("/(limit .*)$/i", "", $query);
  }
  
  $prefs = get_display_pref($auth->auth["uid"], $entity);
  
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // Set separator (if not set in setting => ;)
  if (($set_csv_sep != $ccsvd_sc) && ($set_csv_sep != $ccsvd_tab)) {
    $sep = ";";
  } else if ($set_csv_sep == $ccsvd_tab) {
    $sep = "\t";
  } else {
    $sep = $set_csv_sep;
  }

  $export_d = new OBM_DISPLAY("DATA", $prefs, $emodule);
  $export_d->display_entity = "$entity";
  $export_d->data_set = $obm_q;
  header("Content-Type: text/comma-separated-values");
  header("Content-Disposition: attachment; filename=\"$entity.csv\"");
  $export_d->dis_data_file($first_row, $nb_rows, $sep, $params["function"]);
}


///////////////////////////////////////////////////////////////////////////////
// Stores Export parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_export() {
  global $first_row, $nb_rows, $query, $call_module, $entity, $func_data;
  global $cdg_param;

  if (isset ($first_row)) $params["first_row"] = $first_row;
  if (isset ($nb_rows)) $params["nb_rows"] = $nb_rows;
  if (isset ($query)) $params["query"] = $query;
  if (isset ($call_module)) $params["module"] = $call_module;
  if (isset ($entity)) $params["entity"] = $entity;
  if (isset ($func_data)) $params["function"] = $func_data;

  return $params;
}

?>