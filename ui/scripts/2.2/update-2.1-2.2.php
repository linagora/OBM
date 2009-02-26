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
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : update-2.1-2.2.php                                           //
//     - Desc : Upgrade data from 2.1 to 2.2                                 //
// 2008-09-26 - AliaSource : PB                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$path = "../../php";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
$perm_reader = '01';
$perm_user = '03';
$perm_editor = '07';
$perm_admin = '1F';
//------------------------------//
// Default Profiles definitions //
// Copy of global.inc from 2.1  //
//------------------------------//
$profiles['user'] = array (
  'section' => array (
    'default' => 0,
    'com' => 1,
    'prod' => 1,
    'user' => 1,
    'my' => 1),
  'module' => array (
    'default' => $perm_user,
    'calendar' => $perm_editor),
  'level' => 3,
  'access_restriction' => 'ALLOW_ALL',
  'access_exeptions' => array()
);

$profiles['editor'] = array (
  'section' => array (
    'default' => 0,
    'com' => 1,
    'prod' => 1,
    'user' => 1,
    'my' => 1),
  'module' => array (
    'default' => $perm_editor),
  'level' => 2,
  'access_restriction' => 'ALLOW_ALL',
  'access_exeptions' => array()
);

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

include("../../obminclude/global.inc");
include("$path/admin_data/admin_data_display.inc");
include("$path/admin_data/admin_data_query.inc");

echo "**** OBM : data migration 2.1 -> 2.2 : DB $obmdb_db ($obmdb_host)\n";

make_profiles();

/**
 * Insert profiles into DB From static $profiles
 */
function make_profiles() {
  global $profiles, $cdg_sql, $cgp_show;
  global $obm, $perm_user, $perm_editor, $perm_admin;
  $c_profile_properties = array(
      'level' => 5,
      'level_managepeers' => 1,
      'access_restriction' => 'ALLOW_ALL',
      'access_exceptions' => '',
      'admin_realm' => ''
  );
  $query = "SELECT domain_id from Domain where domain_global=TRUE";
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $global_domain_id = $obm_q->f('domain_id');
  
  echo "Deleting profiles in database (multiple script execution)\n";
  if(!isset($profiles['admin'])) {
    $profiles['admin'] =  array ('section' => array ('default' => 1),'module' => array ('default' => $perm_admin,'domain' => 0));
  }
  if(count($profiles) > 0) {
    $query = "DELETE FROM Profile WHERE profile_name IN ('".implode("','",$profiles)."')";
  }
  
  echo "Parsing configuration files : ".count($profiles)." profiles found\n";
  foreach ($profiles as $name => $data) {
    $name = addslashes($name);
    echo "** Inserting profile : $name \n"; 
    $query = "INSERT INTO Profile (
        	profile_timecreate,
        	profile_domain_id,
        	profile_name
              ) VALUES (
                NOW(),
        	$global_domain_id,
        	'$name'
              )";
    $obm_q->query($query);
    $query = "SELECT MAX(profile_id) as profile_id FROM Profile";
    $obm_q->query($query);
    $obm_q->next_record();
    $profile_id = $obm_q->f('profile_id');
    // Insert Modules
    if(!is_array($data['module'])) {
      echo "No module founded, default value used";
      $data['module'] = array('default' => $perm_user, 'calendar' => $perm_editor);
    }
    foreach ($data['module'] as $module_name => $module) {
      $module = hexdec($module);
      if(($module & 1 == 1) && ($module & 2 != 2)) {
        $module += 2;
      }
      echo "**** Right on module $module_name : $module \n";
      $query = "INSERT INTO ProfileModule (
            profilemodule_module_name,
            profilemodule_domain_id,
            profilemodule_profile_id,
            profilemodule_right
          ) VALUES (
            '$module_name',
            $global_domain_id,
            $profile_id,
            $module 
          )";    
      $obm_q->query($query);
    }
    
    if(!is_array($data['section'])) {
      echo "No section founded, default value used";
      $data['section'] = array('default' => 0, 'com' => 1, 'prod' => 1, 'user' => 1, 'my' => 1);
    }
    foreach ($data['section'] as $section_name => $section) {
      echo "**** Right on section $section_name : $section \n";
      $query = "INSERT INTO ProfileSection (
            profilesection_section_name,
            profilesection_domain_id,
            profilesection_profile_id,
            profilesection_show
          ) VALUES (
            '$section_name',
            $global_domain_id,
            $profile_id,
            $section
          )";
      $obm_q->query($query);
    }

    foreach ($c_profile_properties as $property_name => $default) {
      if(isset($data['properties'][$property_name])) {
        $property = $data['properties'][$property_name];
        if(is_array($property))  {
          $property = implode(',', $property);
        }
      } else {
        $property = $default;
      }
      echo "**** Setting property $property_name : $property\n";
      $query = "INSERT INTO ProfileProperty (
              	profileproperty_profile_id,
              	profileproperty_name,
                profileproperty_value
              ) VALUES (
                $profile_id,
                '$property_name',
                '$property'
              )";
      $obm_q->query($query);
    }
  }
}


</script>
