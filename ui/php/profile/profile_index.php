<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_index.php                                            //
//     - Desc : Profile Index File                                           //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_index.php,v 1.78 2007/02/19 14:32:51 mehdi Exp $ //
///////////////////////////////////////////////////////////////////////////////

$path = '..';
$module = 'profile';
$obminclude = getenv('OBM_INCLUDE_VAR');
if ($obminclude == '') $obminclude = 'obminclude';
include("$obminclude/global.inc");
$params = get_profile_params();

page_open(array('sess' => 'OBM_Session', 'auth' => $auth_class_name, 'perm' => 'OBM_Perm'));
include("$obminclude/global_pref.inc");

require('profile_display.inc');
require('profile_query.inc');
require('profile_js.inc');
include("$obminclude/of/of_category.inc");
$params = get_profile_params();

get_profile_action();
$perm->check_permissions($module, $action);

//update_last_visit('profile', $params['profile_id'], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main Program                                                              //
///////////////////////////////////////////////////////////////////////////////

$do_next = true;
$next_action = $action;

while ($do_next) {
$do_next = false;
$action = $next_action;

if ($action == 'index' || $action == '') {
///////////////////////////////////////////////////////////////////////////////

  //$prefs = get_display_pref($obm['uid'], 'profile', 0);
  $obm_q = run_query_profile_search($params);
  $count = $obm_q->num_rows_total();
  
  if ($count == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg($count.' '.$l_found);
    $display['result'] = html_profile_list($obm_q);
  }

} elseif ($action == 'new') {
///////////////////////////////////////////////////////////////////////////////
  $display['detail'] = html_profile_form($action, $params);

} elseif ($action == 'insert') {
///////////////////////////////////////////////////////////////////////////////
  $do_next = true;
  $its_ok = false;
  
  if(check_user_defined_rules() && check_profile_data_form($params)) {
    $params['profile_id'] = run_query_profile_insert($params);
    if($params['profile_id'] > 0) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_insert_ok");
      $its_ok = true;
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_insert_error");
    }
  } else {
    $display['msg'] = display_warn_msg($l_invalid_data . " : " . $err['msg']);
  }
  
  if ($its_ok)
    $next_action = 'detailconsult';
  else
    $next_action = 'detailupdate';
    
} elseif ($action == 'detailconsult') {
///////////////////////////////////////////////////////////////////////////////
  $profile = get_profile_full_data($params['profile_id']);
  $display['detail'] = html_profile_consult($params, $profile);

} else if ($action == 'userdetail') {
///////////////////////////////////////////////////////////////////////////////
  $user_id = $params['user_id'];
  $usr_q = run_query_userobm($user_id);
  if ($usr_q->next_record()) {
    profile_json_event($usr_q);
    echo "({".$display['json']."})";
    exit();
  } else {
    exit();
  }
  
} elseif ($action == 'detailupdate') {
///////////////////////////////////////////////////////////////////////////////
  $profile = get_profile_full_data($params['profile_id']);
  $display['detail'] = html_profile_form($action, $params, $profile);

} elseif ($action == 'update') {
///////////////////////////////////////////////////////////////////////////////
  $do_next = true;
  $its_ok = false;
  
  $initial_profile = get_profile_full_data($params['profile_id']);
  
  if(check_user_defined_rules() && check_profile_data_form($params)) {
    if (run_query_profile_update($params, $initial_profile)) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_update_ok");
      $its_ok = true;
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_update_error");
    }
  } else {
    $display['msg'] .= display_warn_msg($l_invalid_data . " : " . $err['msg']);
  }
  
  if ($its_ok)
    $next_action = 'detailconsult';
  else
    $next_action = 'detailupdate';

} else if ($action == 'properties_consult') {
///////////////////////////////////////////////////////////////////////////////
  $properties_q = run_query_profileproperty_list($params['profile_id']);
  $display['detail'] = html_properties_consult($params, $properties_q);
  
} else if ($action == 'properties_update') {
///////////////////////////////////////////////////////////////////////////////
  $properties_q = run_query_profileproperty_list($params['profile_id']);
  $display['detail'] = html_properties_form($action, $params, $properties_q);
  
} elseif ($action == 'check_delete') {
///////////////////////////////////////////////////////////////////////////////
  //TODO
  if (check_can_delete_profile($params['profile_id'])) {
    $display['msg'] .= display_info_msg($ok_msg, false);
    $display['detail'] = dis_can_delete_profile($params['profile_id']);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_profile_consult($params, $view);
  }

} elseif ($action == 'delete') {
///////////////////////////////////////////////////////////////////////////////
  //TODO
  if (check_can_delete_profile($params['profile_id'])) {
    $retour = run_query_profile_delete($params['profile_id']);
    if ($retour) {
      $display['msg'] .= display_ok_msg("$l_profile : $l_delete_ok");
    } else {
      $display['msg'] .= display_err_msg("$l_profile : $l_delete_error");
    }
    $display['search'] = dis_profile_search_form($params);
  } else {
    $display['msg'] .= display_warn_msg($err['msg'], false);
    $display['msg'] .= display_warn_msg($l_cant_delete, false);
    $display['detail'] = dis_profile_consult($params, $view);
  }

} elseif ($action == 'display') {
///////////////////////////////////////////////////////////////////////////////
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);

} else if ($action == 'dispref_display') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus);
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);

} else if ($action == 'dispref_level') {
///////////////////////////////////////////////////////////////////////////////
  update_display_pref($entity, $fieldname, $fieldstatus, $fieldorder);
  $prefs = get_display_pref($obm['uid'], 'profile', 1);
  $display['detail'] = dis_profile_display_pref($prefs);

}

} // end while

///////////////////////////////////////////////////////////////////////////////
// Display
///////////////////////////////////////////////////////////////////////////////
if (!$params['ajax']) {
  $display['head'] = display_head($l_profile);
  if (! $params['popup']) {
    update_profile_action();
    $display['header'] = display_menu($module);
  }
  $display['end'] = display_end();
}
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores User parameters transmited in $params hash
// returns : $params hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_profile_params() {
  // Get global params
  $params = get_global_params('profile');
  
  $action = $params['action'];
  
  if (function_exists('get_obm_modules')) {
    global $cright_list; // right constants
    foreach ($cright_list as $right_name) { global ${'cright_'. $right_name}; }
    
	  $obm_modules = get_obm_modules();
	  $params['modules_right'] = array();
    $params['sections_show'] = array();
	  
	  if ($action == 'insert' || $action == 'update') {
      foreach ($obm_modules as $section_name => $modules) {
        foreach ($modules as $module_name) {
          $params['modules_right'][$module_name]['default'] = isset($params["${module_name}_default"]);
          $params['modules_right'][$module_name]['right'] = 0;
        }
        
        if (isset($params["${section_name}_show"]))
          $params['sections_show'][$section_name] = true;
        else
          $params['sections_show'][$section_name] = false;
      }
      
		  foreach ($params as $k => $v) {
		    $matches = array();
		    if (preg_match('/(.+)_right_(.+)/', $k, $matches)) {
		      $module_name = $matches[1];
		      $right_name = $matches[2];
		      
		      if (!isset($params['modules_right'][$module_name])) {
		        $params['modules_right'][$module_name]['right'] = 0;
		      }
		        
		      $params['modules_right'][$module_name]['right'] += ${'cright_'. $right_name};
		      $params['modules_right'][$module_name][$right_name] = $params[$module_name.'_right_'.$right_name];
		    }
		  }
	  }
	}
	
	// Get profile properties params
	if (function_exists('run_query_profileproperty_list')) {
	  
	  $profile_id = NULL;
	  if (isset($params['profile_id'])) { $profile_id = $params['profile_id']; }
	  
	  $profile_properties_q = run_query_profileproperty_list($profile_id);
	  $params['properties'] = array();
	  
	  while ($profile_properties_q->next_record()) {
	    if ($profile_properties_q->f('profileproperty_readonly') != 1) {
    	  $property_name = $profile_properties_q->f('profileproperty_name');
    	  $default_value = $profile_properties_q->f('profileproperty_default');
    	  $readonly      = $profile_properties_q->f('profileproperty_readonly');
    	  $value         = $profile_properties_q->f('profilepropertyvalue_property_value');
    	  
    	  if (empty($value)) { $value = $default_value; }
    	  
    	  // get properties value from form when updating
    	  if ($action == 'update' || $action == 'insert') {
    	    $value = $params[$property_name];
    	  }

    	  $params['properties'][$property_name]['default']  = $default_value;
    	  $params['properties'][$property_name]['readonly'] = $readonly;
    	  $params['properties'][$property_name]['value']    = $value;
	    }
	  }
	}

  return $params;
}


///////////////////////////////////////////////////////////////////////////////
// User Action 
///////////////////////////////////////////////////////////////////////////////
function get_profile_action() {
  global $params, $actions, $path, $view;
  global $l_header_find, $l_header_new, $l_header_display, $l_header_consult, $l_header_properties;
  global $l_header_update, $l_header_delete, $l_ldif_export, $l_svg_export, $l_pdf_export;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

  $id = $params['profile_id'];
  $display_id = $params['display_id'];

  // Index
  $actions['profile']['index'] = array (
  	'Name'     => $l_header_find,
  	'Url'      => "$path/profile/profile_index.php?action=index",
  	'Right'    => $cright_read,
  	'Condition'=> array ('all') );
  
  // Search
//  $actions['profile']['search'] = array (
//  	'Url'      => "$path/profile/profile_index.php?action=search",
//  	'Right'    => $cright_read,
//  	'Condition'=> array ('None') );
  //FIXME
  
  // New
  $actions['profile']['new'] = array (
  	'Name'     => $l_header_new,
  	'Url'      => "$path/profile/profile_index.php?action=new",
  	'Right'    => $cright_write_admin,
  	'Condition'=> array ('search', 'index', 'detailconsult', 'insert', 'update', 'delete', 'display', 'export_ldif') );
  
  // Insert
    $actions['profile']['insert'] = array (
      'Url'      => "$path/profile/profile_index.php?action=insert",
      'Right'    => $cright_write_admin,
      'Condition'=> array ('None') );
  
  // Detail Consult
    $actions['profile']['detailconsult']  = array (
      'Name'     => $l_header_consult,
      'Url'      => "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=$id",
      'Right'    => $cright_read,
      'Condition'=> array ('detailupdate'),
    );
    
  // User Detail
    $actions['profile']['userdetail']  = array (
      'Url'      => "$path/profile/profile_index.php?action=userdetail",
      'Right'    => $cright_read,
      'Condition'=> array ('None') );


  // Detail Update
  $actions['profile']['detailupdate'] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/profile/profile_index.php?action=detailupdate&amp;profile_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'update') );  

  // Update
  $actions['profile']['update'] = array (
    'Url'      => "$path/profile/profile_index.php?action=update",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') );
  
  // Properties == Properties Consult
  $actions['profile']['properties'] = array (
  	'Name'		=> $l_header_properties,
  	'Url'		=> "$path/profile/profile_index.php?action=properties_consult&amp;profile_id=$id",
  	'Right'		=> $cright_read,
  	'Condition'	=> array('detailconsult') );
  
  // Properties Update
  $actions['profile']['properties_update'] = array (
  	'Name'		=> $l_header_update,
  	'Url'		=> "$path/profile/profile_index.php?action=properties_update&amp;profile_id=$id",
  	'Right'		=> $cright_write_admin,
  	'Condition'	=> array('properties_consult') );
  
  // Properties Consult
  $actions['profile']['properties_consult'] = array (
  	'Name'		=> $l_header_consult,
  	'Url'		=> "$path/profile/profile_index.php?action=properties_consult&amp;profile_id=$id",
  	'Right'		=> $cright_read,
  	'Condition' => array('properties_update') );
  	
  // Check Delete
  $actions['profile']['check_delete'] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/profile/profile_index.php?action=check_delete&amp;profile_id=$id",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update', 'export_ldif') );

  // Delete
  $actions['profile']['delete'] = array (
    'Url'      => "$path/profile/profile_index.php?action=delete",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') );

  //FIXME
//  // Display
//    $actions['profile']['display'] = array (
//      'Name'     => $l_header_display,
//      'Url'      => "$path/profile/profile_index.php?action=display",
//      'Right'    => $cright_read,
//      'Condition'=> array ('all') );
//  
//  // Display Preferences
//    $actions['profile']['dispref_display'] = array (
//      'Url'      => "$path/profile/profile_index.php?action=dispref_display",
//      'Right'    => $cright_read,
//      'Condition'=> array ('None') );
//  
//  // Display Level
//    $actions['profile']['dispref_level']  = array (
//      'Url'      => "$path/profile/profile_index.php?action=dispref_level",
//      'Right'    => $cright_read,
//      'Condition'=> array ('None') );
//FIXME
}


///////////////////////////////////////////////////////////////////////////////
// Organizational Chart Actions updates (after processing, before displaying menu)
///////////////////////////////////////////////////////////////////////////////
function update_profile_action() {
  global $params, $actions, $path;

  $id = $params['profile_id'];
  
  if ($id > 0) {
    // Detail Consult
    $actions['profile']['detailconsult']['Url'] = "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=$id";
    $actions['profile']['detailconsult']['Condition'][] = 'insert';

    // Detail Update
    $actions['profile']['detailupdate']['Url'] = "$path/profile/profile_index.php?action=detailupdate&amp;profile_id=$id";
    $actions['profile']['detailupdate']['Condition'][] = 'insert';

    // Check Delete
    $actions['profile']['check_delete']['Url'] = "$path/profile/profile_index.php?action=check_delete&amp;profile_id=$id";
    $actions['profile']['check_delete']['Condition'][] = 'insert';
  }
}
?>
