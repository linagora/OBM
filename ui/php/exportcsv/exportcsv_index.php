<script language="php">
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
$section = "";
$module = "exportcsv";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

page_close();

$params = get_param_export();

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
  $first_row = $params["first_row"];
  $nb_rows = $params["nb_rows"];
  $query = stripslashes($params["query"]);
  if ($ctu_sql_limit && ($nb_rows > $set_rows)) {
    // We remove the limit clause
    $query = preg_replace("/(limit .*)$/i", "", $query);
  }
  $query_pref = stripslashes($params["query_pref"]);
  $module = $params["module"];
  if ($module != "") {
    require_once("$obminclude/lang/$set_lang/${module}.inc");
    require_once("$path/$module/${module}_display.inc");
  } else {
    $module = "obm";
  }
  
  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  display_debug_msg($query_pref, $cdg_sql);
  $pref_q = new DB_OBM;
  $pref_q->query($query_pref);

  // Set separator (if not set in setting => ;)
  if (($set_csv_sep != $ccsvd_sc) && ($set_csv_sep != $ccsvd_tab)) {
    $sep = ";";
  } else if ($set_csv_sep == $ccsvd_tab) {
    $sep = "\t";
  } else {
    $sep = $set_csv_sep;
  }

  $export_d = new OBM_DISPLAY("DATA", $pref_q);
  $export_d->data_set = $obm_q;
  $export_d->display_module = $module;
  header("Content-Type: text/comma-separated-values");
  header("Content-Disposition: attachment; filename=\"$module.csv\"");
  $export_d->dis_data_file($first_row, $nb_rows, $sep, $params["function"]);
}


///////////////////////////////////////////////////////////////////////////////
// Stores Export parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_export() {
  global $first_row, $nb_rows, $query, $query_pref, $call_module, $func_data;
  global $cdg_param;

  if (isset ($first_row)) $params["first_row"] = $first_row;
  if (isset ($nb_rows)) $params["nb_rows"] = $nb_rows;
  if (isset ($query)) $params["query"] = $query;
  if (isset ($query_pref)) $params["query_pref"] = $query_pref;
  if (isset ($call_module)) $params["module"] = $call_module;
  if (isset ($func_data)) $params["function"] = $func_data;

  if ($debug > 0) {
    if ( $params ) {
      while ( list( $key, $val ) = each( $params ) ) {
        echo "<br />param[$key]=$val";
      }
      echo "<br />";
    }
  }

  return $params;
}

</script>
