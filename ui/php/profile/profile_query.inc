<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_query.inc                                            //
//     - Desc : Profile Query File                                           //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_query.inc,v 1.106 2007/02/26 16:25:43 pierre Exp $ //
///////////////////////////////////////////////////////////////////////////////


$cright_list = array('read', 'own', 'write', 'read_admin', 'write_admin');


///////////////////////////////////////////////////////////////////////////////
// Data Query Upper Layer

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
  global $cright_list; // right constants

  foreach ($cright_list as $right_name) { global ${'cright_'. $right_name}; }

  $obm_modules = get_obm_modules();
  
  $res = array();

  $profile_q = run_query_profile_detail($profile_id);
  $res = array(
    'id' => $profile_q->f('profile_id'),
    'name' => $profile_q->f('profile_name'),
  );

  $modules_q = run_query_profilemodule_list($profile_id);

  while ($modules_q->next_record()) {
    $module_name = $modules_q->f('profilemodule_module_name');
    $res['modules_right'][$module_name]['right'] = $modules_q->f('profilemodule_right');
    $res['modules_right'][$module_name]['default'] = false; 

    foreach ($cright_list as $right_name) {
      $res['modules_right'][$module_name][$right_name]
        = $modules_q->f('profilemodule_right') & ${'cright_'. $right_name};
    }
  }

  $sections_q = run_query_profilesection_list($profile_id);

  while ($sections_q->next_record()) {
    $res['sections_show'][$sections_q->f('profilesection_section_name')] = $sections_q->f('profilesection_show');
  }

  if (isset($res['sections_show']['default']) && $res['sections_show']['default'] == 1) {
    $res['sections_show']['default'] = true;
  }
  if (isset($res['sections_show']['default']) && $res['sections_show']['default'] == 0) {
    $res['sections_show']['default'] = false;
  }

  foreach ($obm_modules as $section_name => $modules) {
    foreach ($modules as $module_name) {
      if (!isset($res['modules_right'][$module_name])) {
        $res['modules_right'][$module_name]['default'] = true;
      } 
    }

    if (isset($res['sections_show'][$section_name]))
      $res['sections_show'][$section_name] = ($res['sections_show']['default'] ? false : true);
    else
      $res['sections_show'][$section_name] = ($res['sections_show']['default'] ? true : false);
  }


  // Load profile properties
  $properties_q = run_query_profileproperty_list($profile_id);

  while ($properties_q->next_record()) {
    if ($properties_q->f('profileproperty_readonly') != 1) {
      $res['properties'][$properties_q->f('profileproperty_name')]['value'] = $properties_q->f('profilepropertyvalue_property_value');
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Misc. query functions

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
    if ($section_name != 'false' && trim($section_name) != '')
      $obm_modules[$section_name][] = $module_name;
  }

  return $obm_modules;
}


///////////////////////////////////////////////////////////////////////////////
// SQL query functions

/**
 * Get Profile detail data
 *
 * @param Integer $profile_id
 * @param array $sql_ext SQL query extension (see internal_profile_select())
 * @return DB_OBM
 */
function run_query_profile_detail ($profile_id, $sql_ext = null) {
  $obm_q = internal_profile_select (array('profile_id' => $profile_id), 'run_query_profile_detail()');

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
  return internal_profile_select ($params, 'run_query_profile_search()');
}


/**
 * Retrieve profile modules concerning profile with $profile_id
 *
 * @param Integer $profile_id
 * @return DB_OBM
 */
function run_query_profilemodule_list ($profile_id) {
  global $cprofilemodule_sql_ext;
	
  $search_params = array('profile_id' => $profile_id);
  
	return internal_profile_joinselect($search_params, $cprofilemodule_sql_ext, 'run_query_profilemodule_list()');
}


/**
 * Retrieve profile section concerning profile with $profile_id
 *
 * @param Integer $profile_id
 * @return DB_OBM
 */
function run_query_profilesection_list ($profile_id) {
  global $cprofilesection_sql_ext;
  
  $search_params = array('profile_id' => $profile_id);
  
  return internal_profile_joinselect($search_params, $cprofilesection_sql_ext, 'run_query_profilesection_list()');
}


/**
 * Delete a profile
 */
function run_query_profile_delete($profile_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Delete Profile Section
  $ret = run_query_profilesection_delete($profile_id);

  // Delete Profile Module
  if ($ret) $ret = run_query_profilemodule_delete($profile_id);

  // Delete Profile property values
  if ($ret) $ret = run_query_profileproperties_delete($profile_id);

  // Delete Profile
  if ($ret) {
    $query = "DELETE FROM Profile WHERE profile_id=$profile_id";
    display_debug_msg($query, $cdg_sql, "run_query_profile_delete($profile_id)");
    $ret = $obm_q->query($query);
  }

  return $ret;
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

  run_query_profile_replace_properties($profile_id, $params);

  $profile = get_profile_full_data($profile_id);

  $params['profile_id'] = $profile_id;

  // default module settings
  run_query_profilemodule_insert($profile_id, 'default', 0);

  run_query_profile_update($params, $profile);

  return $profile_id;
}


///////////////////////////////////////////////////////////////////////////////
// profile insert query execution
// Parameters:
//   - $params[]   : entry values
// Returns:
//   $id : new profile id if ok, else false
///////////////////////////////////////////////////////////////////////////////
function run_query_profile_replace_properties($profile_id,$params) {
  global $cdg_sql;
  $p_id = sql_parse_id($profile_id, true);
  $query = "DELETE FROM ProfilePropertyValue WHERE profilepropertyvalue_profile_id $p_id ";
  display_debug_msg($query, $cdg_sql, 'run_query_profile_replace_properties(DELETE)');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $prop_q = run_query_profileproperty_list();
  while($prop_q->next_record()) {
    if ($prop_q->f('profileproperty_readonly') != 1) {
      $id = $prop_q->f('profileproperty_id');
      $key = $prop_q->f('profileproperty_name');
      $value = $params[$key];

      if ($value == '') {
        $value = $prop_q->f('profileproperty_default');
      }

      if ($value != '') {
        $profile_id = sql_parse_id($profile_id);
        $query = "INSERT INTO ProfilePropertyValue (
          profilepropertyvalue_profile_id,
          profilepropertyvalue_property_id,
          profilepropertyvalue_property_value)
          VALUES (
            $profile_id,
            $id,
            '$value'
          )";
        $obm_q->query($query);
        display_debug_msg($query, $cdg_sql, 'run_query_profile_replace_properties(INSERT)');
      }
    }
  }
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
        } else if (!$is_new_default) {
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
    $delete[] = $section_name;
    if($params['sections_show']['default'] && !$params['sections_show'][$section_name]) {
      $insert[] = $section_name;
    }
    if(!$params['sections_show']['default'] && $params['sections_show'][$section_name]) {
      $insert[] = $section_name;
    }
  }

  run_query_profilesection_delete($query_value['profile_id'], 'default');
  run_query_profilesection_insert($query_value['profile_id'], 'default', ($params['sections_show']['default'] ? 1 : 0));

  foreach ($delete as $section_name) {
    run_query_profilesection_delete($query_value['profile_id'], $section_name);
  }

  foreach ($insert as $section_name) {
    run_query_profilesection_insert($query_value['profile_id'], $section_name, ($params['sections_show']['default'] ? 0 : 1));
  }

  // end update profile sections
  // --------------------------

  // --------------------------
  // begin update profile properties

  run_query_profile_replace_properties($query_value['profile_id'],$params);

  // end update profile properties
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
function run_query_profilemodule_delete ($profile_id, $module_name = NULL) {
  global $obm, $cdg_sql;

  $profilemodule_multidomain = sql_multidomain('profilemodule'); 
  $obm_q = new DB_OBM;

  if ($module_name != NULL) {
    $where = " WHERE profilemodule_module_name = '$module_name'
      AND profilemodule_profile_id = '$profile_id'";
  } else {
    $where = " WHERE profilemodule_profile_id = '$profile_id'";
  }

  $query = "DELETE FROM ProfileModule
    $where
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
function run_query_profilesection_delete ($profile_id, $section_name = NULL) {
  global $obm, $cdg_sql;

  $profilesection_multidomain = sql_multidomain('profilesection'); 
  $obm_q = new DB_OBM;

  if ($section_name != NULL) {
    $where = " WHERE profilesection_section_name = '$section_name'
      AND profilesection_profile_id = '$profile_id'";
  } else {
    $where = " WHERE profilesection_profile_id = '$profile_id'";
  }

  $query = "DELETE FROM ProfileSection
    $where
    $profilesection_multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_profilesection_delete()');

  return $obm_q->query($query);
}

function run_query_profileproperties_delete($profile_id) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;

  $query = "DELETE FROM ProfilePropertyValue
    WHERE profilepropertyvalue_profile_id = $profile_id";

  display_debug_msg($query, $cdg_sql, 'run_query_profileproperties_delete()');

  return $obm_q->query($query);
}

/**
 * Get a profile properties
 *
 * @param $profile_id : profile_id
 * @return DB_OBM
 */
function run_query_profileproperty_list ($profile_id = NULL) {
  global $cdg_sql;

  if (!is_null($profile_id)) {
    $val = 'profilepropertyvalue_property_value,';
    $join = " LEFT JOIN ProfilePropertyValue
      ON profileproperty_id = profilepropertyvalue_property_id
      AND profilepropertyvalue_profile_id = '$profile_id'";
  }
  $query = "SELECT
    $val
    profileproperty_name,
    profileproperty_type,
    profileproperty_default,
    profileproperty_readonly,
    profileproperty_id
    FROM ProfileProperty $join";

  display_debug_msg($query, $cdg_sql, 'run_query_profileproperty_list()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Checking functions

function check_profile_data_form($params) {
  global $err;
  global $php_regexp_profile_name;
  global $l_profile_name_empty;
  global $l_profile_name_error;

  $return = true;

  // Check profile name
  if (strlen($params['name']) == 0) {
    $err['msg'] = $l_profile_name_empty;
    $err['field'] = 'name';
    $return = false;
  } else if (!preg_match($php_regexp_profile_name, $params['name'])) {
    $err['msg'] = $l_profile_name_error;
    $err['field'] = 'name';
    $return = false;
  }

  // check properties
  foreach($params['properties'] as $property_name => $property_value ) {

    if ($property_value['readonly'] != 1
      &&
      $property_value['default'] != $params[$property_name]) {

        global ${'php_regexp_profile_'.$property_name};

        if (strlen($params[$property_name]) == 0) {
          global ${'l_'.$property_name.'_empty'};

          $err['msg'] = ${'l_'.$property_name.'_empty'};
          $err['field'] = $property_name;
          $return = false;

        } else if ( !preg_match(${'php_regexp_profile_'.$property_name}, $params[$property_name]) ) {
          global ${'l_profile_'.$property_name.'_error'};

          $err['msg'] = ${'l_profile_'.$property_name.'_error'};
          $err['field'] = $property_name;
          $return = false;
        }
      }
  }

  // check default perm
  global $cright_list;

  $count = 0;

  foreach ($params as $k => $v) {
    $matches = array();
    if (preg_match('/default_right_(.+)/', $k)) {
      $count++;
    }
  }

  if ($count == 0) {
    global $l_profile_default_permission_empty;
    $err['msg'] = $l_profile_default_permission_empty;
    $return = false;
  }

  return $return;
}

/*
 * Check if profile is used by users
 */
function check_can_delete_profile($profile_id) {
  global $cdg_sql;
  $nb_users = 0;

  $query = "SELECT count(userobm_perms) as count
    FROM UserObm
    WHERE userobm_perms = (SELECT profile_name FROM Profile WHERE profile_id=$profile_id)";

  $obm_q = new DB_OBM;

  display_debug_msg($query, $cdg_sql, "check_can_delete_profile(id=$profile_id)");
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $nb_users = $obm_q->f('count');
  }

  return ($nb_users > 0 ? false : true);
}


///////////////////////////////////////////////////////////////////////////////
// Internal query functions
//
// Warning :
// it is not recommend to edit these functions
// better copy and paste internal code content in the standard OBM layer


// internal_profile_joinselect's data atom unity

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
 * Search Profile data with join extension
 *
 * @param array $params
 * @param array $sql_ext SQL query extension parameters
 *                       array('select' => string,
 *                             'join' => string,
 *                             'where' => string,
 *                             'next_record' => bool, // do next_record or not
 *                            )
 * @param String $displayed_debug_msg string displayed by display_debug_msg OBM function
 * 
 * @return DB_OBM
 */
function internal_profile_joinselect ($params, $sql_ext = null, $displayed_debug_msg = 'internal_profile_joinselect()') {
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

  display_debug_msg($query, $cdg_sql, $displayed_debug_msg);
  $obm_q->query($query);

  return $obm_q;
}


/**
 * Search Profile data
 *
 * @param array $params
 * @param String $displayed_debug_msg string displayed by display_debug_msg OBM function
 * 
 * @return DB_OBM
 */
function internal_profile_select ($params, $displayed_debug_msg = 'internal_profile_select()') {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $multidomain = sql_multidomain('profile');

  if (!empty($params['profile_id']))
    $where .= ' AND profile_id = '. sql_parse_id($params['profile_id']);
  if (!empty($params['name']))
    $where .= " AND profile_name #LIKE '$params[name]%'";

  $query = "SELECT 
    profile_id,
    profile_name,
    #TIMESTAMP(profile_timeupdate) as profile_timeupdate,
    #TIMESTAMP(profile_timecreate) as profile_timecreate,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate
    FROM Profile
    LEFT JOIN UserObm as c ON profile_usercreate=c.userobm_id
    LEFT JOIN UserObm as u ON profile_userupdate=u.userobm_id
    WHERE 1=1
    $where
    $multidomain";

  display_debug_msg($query, $cdg_sql, $displayed_debug_msg);
  $obm_q->xquery($query);

  return $obm_q;
}

?>
