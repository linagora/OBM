<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/
?>
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
$path = '..';
$module = 'exportcsv';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_export_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");
page_close();

///////////////////////////////////////////////////////////////////////////////
// Main program                                                              //
//    Action : (index, document)                                             //
///////////////////////////////////////////////////////////////////////////////
if (($action == 'index') || ($action == '')) {
  echo $l_invalid_action;
  dis_end();

} elseif ($action == 'message')  {
///////////////////////////////////////////////////////////////////////////////
  dis_head($l_download);        // Head & Body
  echo $l_download_active;
  dis_end();

} elseif ($action == 'export_page')  {
///////////////////////////////////////////////////////////////////////////////
  $emodule = $params['module'];
  if ($emodule != '') {
    require_once("$obminclude/lang/".$_SESSION['set_lang']."/${emodule}.inc");
    require_once("$path/$emodule/${emodule}_display.inc");
    require_once("$path/$emodule/${emodule}_query.inc");
  } else {
    $emodule = 'obm';
  }
  $entity = $params['entity'];
  if ($entity == '') {
    $entity = $emodule;
  }
  $first_row = $params['first_row'];
  $nb_rows = $params['nb_rows'];
  $query = stripslashes(html_entity_decode($params['query']));

  if ($ctu_sql_limit && ($nb_rows > $_SESSION['set_rows'])) {
    // We remove the limit clause
    $query = preg_replace("/(limit .*)$/i", '', $query);
  }
  
  // Check if banned words are included
  $excluded_words = array ('insert', 'update', 'delete', 'create', 'alter', 'drop', 'lock', 'userobm_password');
  while ( list($key, $value) = each($excluded_words) ) {
    if (preg_match("/\b$value\b/i", $query)) {
      $err['msg'] = "$l_err_query_banned $l_banned_word : $value";
      echo $err['msg'];
      return false;
    }
  }

  $prefs = get_display_pref($obm['uid'], $entity);
  
  display_debug_msg($query, $cdg_sql, 'export()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // Set separator (if not set in setting => ;)
  if (($_SESSION['set_csv_sep'] != $ccsvd_sc) && ($_SESSION['set_csv_sep'] != $ccsvd_tab)) {
    $sep = ';';
  } else if ($_SESSION['set_csv_sep'] == $ccsvd_tab) {
    $sep = "\t";
  } else {
    $sep = $_SESSION['set_csv_sep'];
  }

  $export_d = new OBM_DISPLAY('DATA', $prefs, $emodule);
  $export_d->display_entity = "$entity";
  $export_d->data_set = $obm_q;
  header('Content-Type: text/comma-separated-values');
  header("Content-Disposition: attachment; filename=\"$entity.csv\"");
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');
  $export_d->dis_data_file($first_row, $nb_rows, $sep, $params['function']);
}


///////////////////////////////////////////////////////////////////////////////
// Stores Export parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_export_params() {

  $params = get_global_params('ExportCSV');

  if (isset ($params['call_module'])) $params['module'] = $params['call_module'];
  if (isset ($params['func_data'])) $params['function'] = $params['func_data'];

  return $params;
}

?>
