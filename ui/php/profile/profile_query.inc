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
    'join' => 'LEFT JOIN ProfileModule ON profilesection_profile_id = profile_id '. sql_multidomain('profilesection'),
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
  
  $query = "SELECT 
        profile_id,
        $sql_ext[select]
        1
      FROM Profile
        $sql_ext[join]
      WHERE 1=1
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
  global $cprofilsection_sql_ext;
  global $cright_list; // right constants
  foreach ($cright_list as $right_name) { global ${'cright_'. $right_name}; }
  
  $search_params = array('profile_id' => $profile_id);
  $res = array();
  
  $profile_q = run_query_profile_search($search_params);
  $res = array(
    'id' => $profile_q->f('profile_id'),
    'name' => $profile_q->f('profile_name'),
    );
	
  $modules = array();
  
  $modules_q = run_query_profile_join_search($search_params, $cprofilemodule_sql_ext);
  
  while ($modules_q->next_record()) {
  	$modules[$modules_q->f('profilemodule_module_name')]['right'] = $modules_q->f('profilemodule_right');
  	
    foreach ($cright_list as $right_name) {
    	$res['modules_right'][$modules_q->f('profilemodule_module_name')][$right_name]
    	  = $modules_q->f('profilemodule_right') & ${'cright_'. $right_name};
    }
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
	return true; // TODO
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
  run_query_profilemodule_insert($query_value['profile_id'], 'default', 0);
  
  return run_query_profile_update($params, $profile);
}

/**
 * Parse rights passed by parameters array
 *
 * @param array $params
 * @return array ($module_name => $right_value)
 */
function params_parse_right ($params) {
  global $cright_list;
  foreach ($cright_list as $right_name) { global ${'cright_'. $right_name}; }
  
  $rights = array();

  foreach ($params as $k => $v) {
    $matches = array();
    if (preg_match('/(.+)_right_(.+)/', $k, $matches)) {
    	$module_name = $matches[1];
    	$right_name = $matches[2];
    	
      if (!isset($rights[$module_name]))
        $rights[$module_name] = 0;
      $rights[$module_name] += ${'cright_'. $right_name};
    }
  }
  
  return $rights;
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
	
	$form_rights = params_parse_right($params);
	
  $insert = array();
  $delete = array();
  $update = array();
   
  foreach ($obm_modules as $section_name => $modules) {
  	foreach ($modules as $module_name) {
  		$is_in_db = isset($initial_profile['modules_right'][$module_name]['right']);
  		$is_in_form = isset($form_rights[$module_name]);
  		
  		if ($is_in_db && $is_in_form) {
  			// so check if need update
  			if (($initial_profile['modules_right'][$module_name]['right']) != ($form_rights[$module_name]))
  			  $update[] = $module_name;
  		} else if ($is_in_db && !$is_in_form) {
        // delete in db
        $delete[] = $module_name;
  		} else if (!$is_in_db && $is_in_form) {
        // insert in db
        $insert[] = $module_name;
  		}
  		
      $is_default = isset($params["${module_name}_default"]);
      
      if (!$is_default) {
      	if ($is_in_db) {
      		if ($form_rights[$module_name] == 0)
      		  $update[] = $module_name;
      	} else {
      	  $insert[] = $module_name;
      	}
      }
  	}
  }
  
  if (!isset($initial_profile['modules_right']['default']['right']))
    $insert[] = 'default';
  else if (($initial_profile['modules_right']['default']['right']) != ($form_rights['default'])) {
    $update[] = 'default';
  } 
  
  foreach ($delete as $module_name) {
    run_query_profilemodule_delete($query_value['profile_id'], $module_name);
  }
  
  foreach ($insert as $module_name) {
    run_query_profilemodule_insert($query_value['profile_id'], $module_name,
      (isset($form_rights[$module_name]) ? $form_rights[$module_name] : 0));
  }
  
  foreach ($update as $module_name) {
  	run_query_profilemodule_update($query_value['profile_id'], $module_name,
  	  (isset($form_rights[$module_name]) ? $form_rights[$module_name] : 0));
  }
  
  // end update profile rights
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

?>
