<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : planning_index.php                                           //
//     - Desc : Planning Index File                                          //
// 2008-10-14 Guillaume Lardon                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id: planning_index.php 16 2009-09-24 07:15:46Z glardon $
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default) --                -- show the planning for a month
// - import          --                -- import a planning from a spreadsheet
// - detailconsult   -- plannedtask_id -- show the planned task detail
// - detailupdate    -- plannedtask_id -- show the planned task detail form
// - insert          -- form fields    -- insert the planned task
// - update          -- form fields    -- update the planned task
// - delete          -- plannedtask    -- delete the planned task
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'planning';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_planning_params();
page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
update_planning_session_params();
include("$obminclude/global_pref.inc");
include('planning_display.inc');
include('planning_query.inc');

if ($action == '') $action = 'index';
get_planning_action();
$perm->check_permissions($module, $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////
 if (($action == 'index') || ($action == '')) {
  $display['detail'] .= dis_planning_view($params);
  //if ($perm->check_right('planning', $cright_read_admin)) 
  $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
}
else if ($action == 'import_fileselect') {
///////////////////////////////////////////////////////////////////////////////
  $display['title'] = $l_import;
  $display['detail'] = html_planning_import_fileselect_form($params);
} else if ($action == 'import_tabselect') {
///////////////////////////////////////////////////////////////////////////////
  $tabs = run_query_planning_extract_ods($params['file']);
  $display['title'] = $l_import;
  $display['detail'] = html_planning_import_tabselect_form($params,$tabs);
} else if ($action == 'import') {
///////////////////////////////////////////////////////////////////////////////
  $result = run_query_import_planning($params);
  $display['detail'] = html_planning_import_report($result);
} else if ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_plannedtask_form($action, '', $params);

} else if ($action == 'newform') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_planning_newmonth_form($action, '', $params);

} else if ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
  $display['detail'] = html_plannedtask_consult($obm_q);

} else if ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_planning_update_rights($params)) {
    $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
    $display['detail'] = html_plannedtask_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }

} else if ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_plannedtask_data_form($params,$conflict_id)) {
    
    $retour = run_query_plannedtask_insert($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_insert_ok);
    } else {
      $display['msg'] .= display_err_msg($l_insert_error);
    }
    $params['date'] = of_isodate_convert($params['datebegin']);
    $display['detail'] = dis_planning_view($params);
    $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
   
    // Form data are not valid
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $display['detail'].= html_plannedtask_form($action, '', $params, $err['field'],$conflict_id);
  }

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  if (check_user_defined_rules() && check_plannedtask_data_form($params,$conflict_id)) {
    $retour = run_query_plannedtask_update($params);
    if ($retour) {
      set_update_state();
      $display['msg'] .= display_ok_msg($l_update_ok);
    } else {
      $display['msg'] .= display_err_msg($l_update_error);
    }
    $params['date'] = of_isodate_convert($params['datebegin']);
    $display['detail'] = dis_planning_view($params);
    $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);
  } else {
    $display['msg'] .= display_err_msg($err['msg']);
    $obm_q = run_query_plannedtask_detail($params['plannedtask_id']);
    $display['detail'] = html_plannedtask_form($action, $obm_q, $params, $err['field'],$conflict_id);
  }

} else if ($action == 'detailuserupdate') {
///////////////////////////////////////////////////////////////////////////////
  if (check_planning_update_rights($params)) {
    $obm_q = run_query_plannedtask_user_detail($params['user_id'],$params['month']);
    $display['detail'] = html_plannedtask_user_form($action, $obm_q, $params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg']);
  }
}
elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  $retour = run_query_plannedtask_delete($params['plannedtask_id']);
  if ($retour) {
    set_update_state();
    $display['msg'] .= display_ok_msg($l_delete_ok);
  } else {
    $display['msg'] .= display_err_msg($l_delete_error);
  }
  $display['detail'] = dis_planning_view($params);
  $display['features'] .= dis_group_select($params, run_query_planning_groups(), 1);

} 
else
pt_assert(0,__FILE__,__LINE__,"unknown action: $action");

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
$display['head'] = display_head($l_planning);
$display['end'] = display_end();
// Update actions url in case some values have been updated (id after insert) 
if (! $params['popup']) {
  $display['header'] = display_menu($module);
}

display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores in $params hash, Planning parameters transmited
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_planning_params() {

  // Get global params
  $params = get_global_params('Planning');

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// Update session and parameters
///////////////////////////////////////////////////////////////////////////////
function update_planning_session_params() {
  global $params, $obm;
  
  // We retrieve the selected groups if any, else we get them from session
  if (isset ($params['group_id']))
  {
    $_SESSION['group_id'] = $params['group_id'];
  }
  else
  {
    if(isset($_SESSION['group_id']))
    $params['group_id'] = $_SESSION['group_id'];
    else
    $params['group_id'] = get_planning_default_displayed_groups();
  }
}

function get_planning_default_displayed_groups()
{
  global $cplanning_default_group ,$obm;
  // Use the default planning group if defined in configuration
  if(isset($cplanning_default_group))
  return array($cplanning_default_group);
  // Otherwise, use the production system group defined for the domain
  else
  return array($obm['group_prod']);
  
};

///////////////////////////////////////////////////////////////////////////////
// Clean up a string coming from a textarea : 
//  + remove empty lines
//  + remove space at the beginning of the lines
//  + remove space at the end of the lines
//  + remove first empty line and last \n of the string
// Parameters:
//   - $str String to be cleaned up
// Returns:
//   - The string cleaned up  
///////////////////////////////////////////////////////////////////////////////
function cleanup($str) {
  $result = '';
  $lines = explode('\r\n', $str);
  $i = 0;
  while ($i < count($lines)) {
    $currentline = trim($lines[$i]);
    if ($currentline != '') {
      $result .= $currentline.'\r\n';
    }
    $i++;
  }

  // Suppression du dernier retour chariot
  // (plus pratique pour les traitements type explode/tokenizer, on obtient pas de derniere chaine vide)
  $result = preg_replace('/\r\n$/', '', $result);
  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// Planning Action 
///////////////////////////////////////////////////////////////////////////////
function get_planning_action() {
  global $params, $actions, $path;
  global $l_planning, $l_reporting, $l_header_import, $l_header_consult;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions['planning']['index'] = array (
    'Name'     => $l_planning,
    'Url'      => "$path/planning/planning_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                    );
// Import
  $actions['planning']['import_fileselect'] = array (
    'Name'     => $l_header_import,
    'Url'      => "$path/planning/planning_index.php?action=import_fileselect",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('all') );

  $actions['planning']['reporting'] = array (
    'Name'     => $l_reporting,
    'Url'      => "$path/planning/planning_index.php?reportingmode=1",
    'Right'    => $cright_write,
    'Condition'=> array ('all') );

  $actions['planning']['import_tabselect'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
  $actions['planning']['import'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );

// New
  $actions['planning']['new'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                  );

// Detail Update
  $actions['planning']['detailupdate'] = array (
     'Right'    => $cright_write,
     'Condition'=> array ('None')
                              	   );

// Insert
  $actions['planning']['insert'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );
// Update
  $actions['planning']['update'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None')
                                     );

// Detail Update
  $actions['planning']['detailuserupdate'] = array (
     'Right'    => $cright_write,
     'Condition'=> array ('None')
                              	   );

// Delete
  $actions['planning']['delete'] = array (
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                     );
/*
// Search
  $actions['planning']['search'] = array (
    'Right'    => $cright_read,
    'Condition'=> array ('None')
                                  );
// New month
// TODO : ecrire la fonction
  $actions['planning']['new'] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/planning/planning_index.php?action=newform",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('search','index','admin', 'showlist','detailconsult','display', 'insert', 'update')
                                  );
*/
// Detail Consult
  $actions['planning']['detailconsult'] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/planning/planning_index.php?action=detailconsult",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                  );
}

</script>
