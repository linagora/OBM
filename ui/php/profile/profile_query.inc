<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_query.inc                                            //
//     - Desc : Profile Query File                                           //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_query.inc,v 1.106 2007/02/26 16:25:43 pierre Exp $ //
///////////////////////////////////////////////////////////////////////////////


$cright_list = array('read', 'own', 'write', 'read_admin', 'write_admin');

$cprofilemodule_sql_ext = array(
    'select' => 'profilemodule_module_name,
                 profilemodule_right,
                 ',
    'join' => 'LEFT JOIN ProfileModule ON profilemodule_profile_id = profile_id '. sql_multidomain('profilemodule'),
    'where' => ''
    );
    
$cprofilesection_sql_ext = array(
    'select' => 'profilesection_section_name,
                 profilesection_show,
                 ',
    'join' => 'LEFT JOIN ProfileSection ON profilesection_profile_id = profile_id '. sql_multidomain('profilesection'),
    'where' => ''
    );
    
$cprofilepropertyvalue_sql_ext = array(
    'select' => 'profilepropertyvalue_value,
                 profileproperty_profil_id,
                 ',
    'join' => 'LEFT JOIN ProfilePropertyValue ON profileproperty_profil_id = profile_id '. sql_multidomain('profilepropertyvalue'),
    'where' => ''
    );

/**
 * Get Profile detail data
 *
 * @param Integer $profile_id
 * @param array $sql_ext SQL query extension (see run_query_profile_search())
 * @return DB_OBM
 */
function run_query_profile_detail ($profile_id, $sql_ext = null) {
  $obm_q = run_query_profile_search (array('profile_id' => $profile_id));
  
  $obm_q->next_record();
  
  return $obm_q;
}

/**
 * Search Profile data
 *
 * @param array $params
 * @return DB_OBM
 */
function run_query_profile_search ($params) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  $timeupdate = sql_date_format($db_type, 'profile_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'profile_timecreate', 'timecreate');
  $multidomain = sql_multidomain('profile');
  
  if (!empty($params['profile_id']))
    $where .= ' AND profile_id = '. sql_parse_id($params['profile_id']);
  
  $query = "SELECT 
        profile_id,
        profile_name,
        profile_timeupdate,
        profile_timecreate,
        
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      FROM Profile
        LEFT JOIN UserObm as c ON profile_usercreate=c.userobm_id
        LEFT JOIN UserObm as u ON profile_userupdate=u.userobm_id
      WHERE 1=1
        $where
        $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profile_detail()');
  $obm_q->query($query);

  return $obm_q;
}

/**
 * Search Profile data with join extension
 *
 * @param array $params
 * @param array $sql_ext SQL query extension parameters
 *                       array('select' => string,
 *                             'join' => string,
 *                             'where' => string,
 *                             'next_record' => bool, // do next_record or not
 *                            )
 * 
 * @return DB_OBM
 */
function run_query_profile_join_search ($params, $sql_ext = null) {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  $multidomain = sql_multidomain('profile');
  
  $where = '';
  
  if (!empty($params['profile_id']))
    $where .= ' AND profile_id = '. sql_parse_id($params['profile_id']);
  
  $query = "SELECT 
        profile_id,
        $sql_ext[select]
        1
      FROM Profile
        $sql_ext[join]
      WHERE 1=1
        $where
        $sql_ext[where]
        $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profile_join_search()');
  $obm_q->query($query);

  return $obm_q;
}


/**
 * Get profile modules right list
 *
 * @param Integer $profile_id
 * @return array (
 *    'name' => $profile_name,
 *    'modules_right' => array (
 *       $module_name => array (
 *          'right' => integer,
 *          'read' => bool,
 *          'write' => bool,
 *          'read_admin' => bool,
 *          'write_admin' => bool,
 *          'own' => bool,
 *       )
 *    )
 * )
 */
function get_profile_full_data ($profile_id) {
  global $cprofilemodule_sql_ext;
  global $cprofilesection_sql_ext;
  global $cright_list; // right constants
  foreach ($cright_list as $right_name) { global ${'cright_'. $right_name}; }
  
  $obm_modules = get_obm_modules();
  
  $search_params = array('profile_id' => $profile_id);
  $res = array();
  
  $profile_q = run_query_profile_search($search_params);
  $profile_q->next_record();
  $res = array(
    'id' => $profile_q->f('profile_id'),
    'name' => $profile_q->f('profile_name'),
    );
  
  $modules_q = run_query_profile_join_search($search_params, $cprofilemodule_sql_ext);
  
  while ($modules_q->next_record()) {
  	$module_name = $modules_q->f('profilemodule_module_name');
  	$res['modules_right'][$module_name]['right'] = $modules_q->f('profilemodule_right');
    $res['modules_right'][$module_name]['default'] = false; 
  	
    foreach ($cright_list as $right_name) {
    	$res['modules_right'][$module_name][$right_name]
    	  = $modules_q->f('profilemodule_right') & ${'cright_'. $right_name};
    }
  }
  
  $sections_q = run_query_profile_join_search($search_params, $cprofilesection_sql_ext);
  
  while ($sections_q->next_record()) {
    $res['sections_show'][$sections_q->f('profilesection_section_name')] = $sections_q->f('profilesection_show');
  }
  
  foreach ($obm_modules as $section_name => $modules) {
    foreach ($modules as $module_name) {
      if (!isset($res['modules_right'][$module_name])) {
        $res['modules_right'][$module_name]['default'] = true;
      } 
    }
    
    if (!isset($res['sections_show'][$section_name]))
      $res['sections_show'][$section_name] = false; 
  }
  
  return $res;
}

/**
 * Make global OBM array $profiles
 *
 * @return array
 */
function get_obm_profiles () {
	global $cprofilemodule_sql_ext;
	// TODO
	
	/*
  $obm_q = run_query_profile_join_search(array(), $cprofilemodule_sql_ext);
  
  $obm_profiles = array();
  
  $profiles['admin'] = array (
    'section' => array (
      'default' => 1),
    'module' => array (
      'default' => $perm_admin,
      'domain' => 0),
    'level' => 1,
    'level_managepeers' => 1,
    'access_restriction' => 'ALLOW_ALL',
    'access_exeptions' => array()
  );
  while ($obm_q->next_record()) {
  	$obm_profiles[$obm_q->f('profile_name')]['module'][$obm_q->f('profilemodule_module_name')]
  	  = $obm_q->f('profilemodule_right');
  }
  
  return $obm_profiles;
  */
}

function check_profile_data_form($params) {
	global $err;
	global $php_regexp_profile_name;
	global $l_profile_name_empty;
	global $l_profile_name_error;
	
	if (strlen($params['name']) == 0) {
		$err['msg'] = $l_profile_name_empty;
    	$err['field'] = 'name';
    	return false;
	} else if (!preg_match($php_regexp_profile_name, $params['name'])) {
		$err['msg'] = $l_profile_name_error;
    	$err['field'] = 'name';
		return false;
	} else {	
		return true;
	}
}

/**
 * Insert a profile in SQL database
 *
 * @param array $params
 * @return DB_OBM
 */
function run_query_profile_insert ($params) {
  global $cdg_sql, $obm;
  
  $obm_q = new DB_OBM;
	
  $now = date('Y-m-d H:i:s');
  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id( $obm['domain_id']);
  
  $query = "INSERT INTO Profile (
    profile_timeupdate,
    profile_timecreate,
    profile_userupdate,
    profile_usercreate,
    profile_domain_id,
    profile_name
  ) VALUES ('$now',
    '$now',
    $uid,
    $uid,
    $domain_id,
    '$params[name]'
  )";

  display_debug_msg($query, $cdg_sql, 'run_query_profile_insert(1)');
  $retour = $obm_q->query($query);
  
  $query = "SELECT profile_id
    FROM Profile
    WHERE
      profile_timeupdate = '$now'
      AND profile_timecreate = '$now'
      AND profile_userupdate = $uid
      AND profile_usercreate = $uid
      AND profile_domain_id = $domain_id
      AND profile_name = '$params[name]'
  ";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profile_insert(2)');
  $retour = $obm_q->query($query);
  $obm_q->next_record();
  
  $profile_id = $obm_q->f('profile_id');
  
  $profile = get_profile_full_data($profile_id);
  
  $params['profile_id'] = $profile_id;
  
  // default module settings
  run_query_profilemodule_insert($profile_id, 'default', 0);
  
  run_query_profile_update($params, $profile);
  
  return $profile_id;
}

/**
 * Update a profile in SQL database
 *
 * @param array $params
 * @param array $initial_profile
 */
function run_query_profile_update ($params, $initial_profile) {
	global $obm, $cdg_sql;
  
  $obm_q = new DB_OBM;
  
  // --------------------------
  // begin prepare data
  
  $domain_id = sql_parse_id($obm['domain_id']);
  $profile_multidomain = sql_multidomain('profile'); 
  
  $now = date('Y-m-d H:i:s');
  $uid = sql_parse_id($obm['uid']);
  
  // end prepare data
  // --------------------------
  
  // --------------------------
  // begin update profile main properties
  
  $query_value['name'] = $params['name'];
  $query_value['profile_id'] = $params['profile_id'] + 0;
  
  $query = "UPDATE Profile SET
      profile_timeupdate = '$now',
      profile_userupdate = $uid,
      profile_name = '$query_value[name]'
    WHERE profile_id = $query_value[profile_id]
      $profile_multidomain
  ";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profile_update(0)');
  $obm_q->query($query);
	
  // end update profile main properties
  // --------------------------
	
  
  // --------------------------
  // begin update profile rights
  
	$obm_modules = get_obm_modules();
	
  $insert = array();
  $delete = array();
  $update = array();
  
  foreach ($obm_modules as $section_name => $modules) {
  	foreach ($modules as $module_name) {
  		$exists_in_db = !$initial_profile['modules_right'][$module_name]['default'];
  		$new_right = $params['modules_right'][$module_name]['right'];
      $old_right = $initial_profile['modules_right'][$module_name]['right'];
      $is_new_default = $params['modules_right'][$module_name]['default'];
  		
  		if ($exists_in_db) {
  			if ($is_new_default) {
  				$delete[] = $module_name;
  			} else if ($new_right != $old_right) {
  				$update[] = $module_name;
  			}
  		} else {
  			if ($new_right != 0) {
  				$insert[] = $module_name;
  			}
  			
  			if (!$is_new_default) {
  				$insert[] = $module_name;
        }
  		}
  	}
  }
  
  if ($initial_profile['modules_right']['default']['default'])
    $insert[] = 'default';
  else if (($initial_profile['modules_right']['default']['right']) != ($params['modules_right']['default']['right'])) {
    $update[] = 'default';
  }
  
  foreach ($delete as $module_name) {
    run_query_profilemodule_delete($query_value['profile_id'], $module_name);
  }
  
  foreach ($insert as $module_name) {
    run_query_profilemodule_insert($query_value['profile_id'], $module_name,
     $params['modules_right'][$module_name]['right']);
  }
  
  foreach ($update as $module_name) {
  	run_query_profilemodule_update($query_value['profile_id'], $module_name,
  	  $params['modules_right'][$module_name]['right']);
  }
  
  // end update profile rights
  // --------------------------
  
  
  // --------------------------
  // begin update profile sections
  
  $insert = array();
  $delete = array();
  
  foreach ($obm_modules as $section_name => $modules) {
    $exists_in_db = $initial_profile['sections_show'][$section_name];
    $new_value = $params['sections_show'][$section_name];
    
    if ($exists_in_db && !$new_value) {
    	$delete[] = $section_name;
    }
    
    if (!$exists_in_db && $new_value) {
    	$insert[] = $section_name;
    }
  }
  
  foreach ($delete as $section_name) {
    run_query_profilesection_delete($query_value['profile_id'], $section_name);
  }
  
  foreach ($insert as $section_name) {
    run_query_profilesection_insert($query_value['profile_id'], $section_name, 1);
  }
  
  // end update profile sections
  // --------------------------
  
  return true;
}

/**
 * Insert a module right
 *
 * @param integer $profile_id
 * @param string $module_name
 * @param integer $right_value
 */
function run_query_profilemodule_insert ($profile_id, $module_name, $right_value) {
  global $obm, $cdg_sql;
  
  $domain_id = sql_parse_id( $obm['domain_id']);
  $obm_q = new DB_OBM;
  
  $query = "INSERT INTO ProfileModule (profilemodule_module_name,
    profilemodule_domain_id,
    profilemodule_profile_id,
    profilemodule_right) VALUES ('$module_name',
    $domain_id,
    $profile_id,
    $right_value
    )";
  display_debug_msg($query, $cdg_sql, 'run_query_profilemodule_insert()');
  
  return $obm_q->query($query);
}

/**
 * Update a module right
 *
 * @param integer $profile_id
 * @param string $module_name
 * @param integer $right_value
 */
function run_query_profilemodule_update ($profile_id, $module_name, $right_value) {
  global $obm, $cdg_sql;
  
  $profilemodule_multidomain = sql_multidomain('profilemodule'); 
  $obm_q = new DB_OBM;
  
  $query = "UPDATE ProfileModule SET profilemodule_right = $right_value
    WHERE profilemodule_module_name = '$module_name'
      AND profilemodule_profile_id = '$profile_id'
      $profilemodule_multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profilemodule_update()');
  
  return $obm_q->query($query);
}

/**
 * Delete a module right
 *
 * @param integer $profile_id
 * @param string $module_name
 */
function run_query_profilemodule_delete ($profile_id, $module_name) {
  global $obm, $cdg_sql;
  
  $profilemodule_multidomain = sql_multidomain('profilemodule'); 
  $obm_q = new DB_OBM;
  
  $query = "DELETE FROM ProfileModule WHERE profilemodule_module_name = '$module_name'
      AND profilemodule_profile_id = '$profile_id'
      $profilemodule_multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profilemodule_delete()');
  
  return $obm_q->query($query);
}

/**
 * Insert a section show
 *
 * @param integer $profile_id
 * @param string $section_name
 * @param integer $show_value
 */
function run_query_profilesection_insert ($profile_id, $section_name, $show_value) {
  global $obm, $cdg_sql;
  
  $domain_id = sql_parse_id( $obm['domain_id']);
  $obm_q = new DB_OBM;
  
  $query = "INSERT INTO ProfileSection (profilesection_section_name,
    profilesection_domain_id,
    profilesection_profile_id,
    profilesection_show) VALUES ('$section_name',
    $domain_id,
    $profile_id,
    $show_value
    )";
  display_debug_msg($query, $cdg_sql, 'run_query_profilesection_insert()');
  
  return $obm_q->query($query);
}

/**
 * Delete a section show
 *
 * @param integer $profile_id
 * @param string $section_name
 */
function run_query_profilesection_delete ($profile_id, $section_name) {
  global $obm, $cdg_sql;
  
  $profilesection_multidomain = sql_multidomain('profilesection'); 
  $obm_q = new DB_OBM;
  
  $query = "DELETE FROM ProfileSection WHERE profilesection_section_name = '$section_name'
      AND profilesection_profile_id = '$profile_id'
      $profilesection_multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profilesection_delete()');
  
  return $obm_q->query($query);
}

/**
 * Get Activated OBM Modules data array
 *
 * @return array
 * 
 *   array(
 *      $section_name =>
 *        array(
 *           $module_name,
 *           ...
 *        )
 *      ...
 *   )
 */
function get_obm_modules () {
  global $cgp_show;
  
  $obm_modules = array();
  
  foreach ($cgp_show['section'] as $section_name => $url) {
    $obm_modules[$section_name] = array();
  }
  
  foreach ($cgp_show['module'] as $module_name => $section_name) {
    if ($section_name && trim($section_name) != '')
      $obm_modules[$section_name][] = $module_name;
  }
  
  return $obm_modules;
}

/**
 * Get profile properties list
 *
 * @return DB_OBM
 */
function run_query_profileproperty_list () {
  global $cdg_sql;
  
  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  
  $timeupdate = sql_date_format($db_type, 'profileproperty_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'profileproperty_timecreate', 'timecreate');
  $multidomain = sql_multidomain('profile');
  
  if (!empty($params['profileproperty_id']))
    $where .= ' AND profileproperty_id = '. sql_parse_id($params['profileproperty_id']);
  
  $query = "SELECT 
        profileproperty_id,
        profileproperty_property_name,
        profileproperty_timeupdate,
        profileproperty_timecreate,
        
        c.userobm_login as usercreate,
        u.userobm_login as userupdate
      FROM ProfileProperty
        LEFT JOIN UserObm as c ON profileproperty_usercreate=c.userobm_id
        LEFT JOIN UserObm as u ON profileproperty_userupdate=u.userobm_id
      WHERE 1=1
        $where
        $multidomain";
  
  display_debug_msg($query, $cdg_sql, 'run_query_profileproperty_detail()');
  $obm_q->query($query);

  return $obm_q;
}

/**
 * Get profiles property data array
 *
 * @return array
 */
function get_profileproperty_list () {
	$res = array();
	
  $obm_q = run_query_profileproperty_list();
  
  while ($obm_q->next_record()) {
  	$res[]['property_name'] = $obm_q->f('profileproperty_name');
  }
  
  return $res;
}

?>
