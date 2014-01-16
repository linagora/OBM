<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_query.inc                                            //
//     - Desc : Profile Query File                                           //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_query.inc,v 1.106 2007/02/26 16:25:43 pierre Exp $ //
///////////////////////////////////////////////////////////////////////////////


$c_profile_properties = array(
    'level' ,
    'level_managepeers' ,
    'access_restriction' ,
    'access_exceptions' ,
    'user_results_limit',
    'admin_realm',
    'default_right', 
    'mail_quota_max', 
    'mail_quota_default' 
);

$c_perms_admin = 24;
$c_perms_write = 4;
$c_perms_read = 3;

/**
 * run_query_profile_details 
 * 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_profile_details($id) {
  global $cdg_sql, $obm, $c_profile_properties;

  $multidomain = sql_multidomain('profile');
  $obm_q = new DB_OBM;
  $query = "SELECT * FROM Profile WHERE profile_id = #INT($id) $multidomain";
  $obm_q->query($query);
  $obm_q->next_record();
  $profile['profile_id'] = $id;
  $profile['name'] = $obm_q->f('profile_name');
  $query = "SELECT * FROM Profile 
    INNER JOIN ProfileModule ON profilemodule_profile_id = profile_id
    WHERE profile_id = #INT($id) $multidomain";
  $obm_q->query($query);
  $profile['module'] = array();
  while($obm_q->next_record()) {
    $profile['module'][$obm_q->f('profilemodule_module_name')] = $obm_q->f('profilemodule_right');
  }
  $query = "SELECT * FROM Profile 
    INNER JOIN ProfileSection ON profilesection_profile_id = profile_id
    WHERE profile_id = #INT($id) $multidomain";
  $obm_q->query($query);
  $profile['section'] = array();
  while($obm_q->next_record()) {
    $profile['section'][$obm_q->f('profilesection_section_name')] = $obm_q->f('profilesection_show');
  }  
  $query = "SELECT * FROM Profile
    INNER JOIN ProfileProperty ON profileproperty_profile_id = profile_id
    WHERE profile_id = #INT($id) $multidomain";
  $obm_q->query($query);
  foreach($c_profile_properties as $property) {
    $profile['property'][$property] = false;
  }
  while($obm_q->next_record()) {
    $value = $obm_q->f('profileproperty_value');
    if(count(explode(',', $value)) > 1) {
      $profile['property'][$obm_q->f('profileproperty_name')] = explode(',', $value);
    } else {
      $profile['property'][$obm_q->f('profileproperty_name')] = $value;
    }
  }  
  return $profile;
  
}


/**
 * Update a profile in SQL database
 *
 * @param array $params
 * @param array $initial_profile
 */
function run_query_profile_update ($params) {
  global $obm, $cdg_sql;

  $obm_q = new DB_OBM;
  $profile_multidomain = sql_multidomain('profile'); 

  $uid = $obm['uid'];

  $name = $params['name'];
  $id = $params['profile_id'];

  $query = "UPDATE Profile SET
    profile_timeupdate = NOW(),
    profile_userupdate = #INT($uid)
    WHERE profile_id = #INT($id)
    $profile_multidomain
  ";
  display_debug_msg($query, $cdg_sql, 'run_query_profile_update(0)');
  $obm_q->query($query);
  run_query_profile_replace_dataset($params);
  return true;
}


/**
 * Update a profile in SQL database
 *
 * @param array $params
 * @param array $initial_profile
 */
function run_query_profile_replace_dataset($params) {
  global $obm, $cdg_sql, $c_profile_properties;

  $obm_q = new DB_OBM;

  $domain_id = $obm['domain_id'];
  $profile_multidomain = sql_multidomain('profile'); 

  
  $id = $params['profile_id'];
  $query = "DELETE FROM ProfileModule WHERE profilemodule_profile_id = #INT($id)";
  display_debug_msg($query, $cdg_sql, 'run_query_profile_replace_dataset(0)');
  $obm_q->query($query);
  if(is_array($params['module'])) {
    foreach($params['module'] as $module => $right) {
      $query = "INSERT INTO ProfileModule 
        (profilemodule_domain_id, profilemodule_profile_id, profilemodule_module_name, profilemodule_right)
        VALUES 
        (#INT($domain_id), #INT($id), '".$obm_q->escape($module)."', #INT($right))
        ";
        $obm_q->query($query);
    }
  }
  $query = "DELETE FROM ProfileSection WHERE profilesection_profile_id = #INT($id)";
  display_debug_msg($query, $cdg_sql, 'run_query_profile_replace_dataset(0)');
  $obm_q->query($query);
  if(is_array($params['section'])) {
    foreach($params['section'] as $section => $show) {
      $show = ($show == 1)? 'TRUE':'FALSE';
      $query = "INSERT INTO ProfileSection 
        (profilesection_domain_id, profilesection_profile_id, profilesection_section_name, profilesection_show)
        VALUES 
        (#INT($domain_id), #INT($id), '".$obm_q->escape($section)."', $show)
        ";
        $obm_q->query($query);
    }
  }

  $query = "DELETE FROM ProfileProperty WHERE profileproperty_profile_id = #INT($id)";
  display_debug_msg($query, $cdg_sql, 'run_query_profile_replace_dataset(0)');
  $obm_q->query($query);
  
  foreach($c_profile_properties as $property) {
    if(isset($params['property'][$property])) {
      $value = $params['property'][$property];
      if(is_array($value)) {
        $value = implode(',',$value);
      }
      $query = "INSERT INTO ProfileProperty
        (profileproperty_profile_id, profileproperty_name, profileproperty_value)
        VALUES 
        (#INT($id), '".$obm_q->escape($property)."', '".$obm_q->escape($value)."')
        ";
        $obm_q->query($query);      
    }
  } 

  return true;
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

  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];

  $query = "INSERT INTO Profile (
    profile_timeupdate,
    profile_timecreate,
    profile_userupdate,
    profile_usercreate,
    profile_domain_id,
    profile_name
  ) VALUES (
    NOW(),
    NOW(),
    #INT($uid),
    #INT($uid),
    #INT($domain_id),
    '".$obm_q->escape($params[name])."'
  )";

  display_debug_msg($query, $cdg_sql, 'run_query_profile_insert(1)');
  $retour = $obm_q->query($query);

  $profile_id = $obm_q->lastid(); 
  $entity_id = of_entity_insert($GLOBALS['module'], $profile_id);  

  $params['profile_id'] = $profile_id;

  run_query_profile_replace_dataset($params);
  return $profile_id;
}



/**
 * run_query_profile_delete 
 * 
 * @param mixed $profile_id 
 * @access public
 * @return void
 */
function run_query_profile_delete($profile_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Delete Profile
  of_entity_delete($GLOBALS['module'], $profile_id);
  $query = "DELETE FROM Profile WHERE profile_id=#INT($profile_id)";
  display_debug_msg($query, $cdg_sql, "run_query_profile_delete($profile_id)");
  $ret = $obm_q->query($query);

  return $ret;
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
 * Insert a module right
 *
 * @param integer $profile_id
 * @param string $module_name
 * @param integer $right_value
 */
function run_query_profilemodule_insert ($profile_id, $module_name, $right_value) {
  global $obm, $cdg_sql;

  $domain_id = $obm['domain_id'];
  $obm_q = new DB_OBM;

  $query = "INSERT INTO ProfileModule (profilemodule_module_name,
    profilemodule_domain_id,
    profilemodule_profile_id,
    profilemodule_right) VALUES ('".$obm_q->escape($module_name)."',
    #INT($domain_id),
    #INT($profile_id),
    #INT($right_value)
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

  $query = "UPDATE ProfileModule SET profilemodule_right = #INT($right_value)
    WHERE profilemodule_module_name = '".$obm_q->escape($module_name)."'
    AND profilemodule_profile_id = #INT($profile_id)
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
    $where = " WHERE profilemodule_profile_id = #INT($profile_id)";
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

  $domain_id = $obm['domain_id'];
  $obm_q = new DB_OBM;

  $query = "INSERT INTO ProfileSection (profilesection_section_name,
    profilesection_domain_id,
    profilesection_profile_id,
    profilesection_show) VALUES ('".$obm_q->escape($section_name)."',
    #INT($domain_id),
    #INT($profile_id),
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
    $where = " WHERE profilesection_profile_id = #INT($profile_id)";
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
    WHERE profilepropertyvalue_profile_id = #INT($profile_id)";

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
      AND profilepropertyvalue_profile_id = #INT($profile_id)";
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

/**
 * run_query_profile_exist 
 * 
 * @param mixed $name 
 * @param mixed $id 
 * @access public
 * @return void
 */
function run_query_profile_exist($name, $id) {
  global $obm;
  if($id) {
    $update = "AND profile_id != #INT($id)";
  }
  $query = "
    SELECT * FROM Profile INNER JOIN Domain ON  domain_id = profile_domain_id
    WHERE profile_name = '$name' and (domain_global = TRUE OR domain_id = #INT($obm[domain_id]))
    $update";
  display_debug_msg($query, $cdg_sql, 'run_query_profileproperty_list()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q->next_record();  
}

///////////////////////////////////////////////////////////////////////////////
// Checking functions

function check_profile_data_form($params) {
  global $err;
  global $php_regexp_profile_name;
  global $l_profile_name_empty;
  global $l_profile_name_error,$l_profile_exists;
  

  $return = true;
  $exists = run_query_profile_exist($params['name'], $params['profile_id']);
  if($exists === TRUE) {
    $err['msg'] = $l_profile_exists;
    $err['field'] = 'name';
    $return = false;
  }
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

  $max_quota = $params['property']['mail_quota_max'];
  $default_quota = $params['property']['mail_quota_default'];
  if(($max_quota < $default_quota && $max_quota != '0') 
    || ($max_quota > $default_quota && $default_quota == '0')) {
    $err['msg'] = $GLOBALS['l_mail_quota_error']." ($GLOBALS[l_max_quota] : $max_quota)";
    $return = false;
  }

  if(!isset($params['module']['default']) || !isset($params['section']['default'])) {
    $err['msg'] = $GLOBALs['l_profile_default_permission_empty'];
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
    WHERE userobm_perms = (SELECT profile_name FROM Profile WHERE profile_id=#INT($profile_id))";

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
    $where .= " AND profile_id = #INT($params[profile_id])";

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
    $where .= " AND profile_id = #INT($params[profile_id])";
  if (!empty($params['name']))
    $where .= " AND profile_name #LIKE '".$obm_q->escape($params[name])."%'";

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
