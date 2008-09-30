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
include("../../obminclude/global.inc");
include("$path/admin_data/admin_data_display.inc");
include("$path/admin_data/admin_data_query.inc");

echo "**** OBM : data migration 2.1 -> 2.2 : DB $obmdb_db ($obmdb_host)\n";

// Get domain list
$d = get_domain_list();
$dp = get_domainproperty_list();
process_domain_list($d, $dp);
update_domain_groups();

dis_admin_data_group('data_update', 'txt');

check_insert_admin0();

make_profiles();



///////////////////////////////////////////////////////////////////////////////
// Process the domain list
///////////////////////////////////////////////////////////////////////////////
function process_domain_list($d, $dp) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Loop through Domains
  foreach ($d as $d_id) {

    echo "\nDomain $d_id :";

    // Loop through Properties
    foreach ($dp as $key => $prop) {

      $query = "SELECT * FROM DomainPropertyValue
         WHERE domainpropertyvalue_domain_id='$d_id'
           AND domainpropertyvalue_property_key='$key'";
      $obm_q->query($query);

      // If property not found in the domain, insert it with default valuevalue
      if ($obm_q->num_rows() == 0) {
	$default = $prop['default'];
	$type = $prop['type'];
	if ($type == 'integer') {
	  $val = "$default";
	} else {
	  $val = "'$default'";
	}
	echo " $key($val)";
	$q2 = "INSERT INTO DomainPropertyValue (
            domainpropertyvalue_domain_id,
            domainpropertyvalue_property_key,
            domainpropertyvalue_value)
          VALUES ('$d_id','$key', $val)";
	$obm_q->query($q2);
      }
    }
  }

  echo "\n";
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Get the domain list
///////////////////////////////////////////////////////////////////////////////
function get_domain_list() {
  global $cdg_sql;

  $d = array();
  $query = "SELECT * FROM Domain";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $id = $obm_q->f("domain_id");
    $d[$id] = $id;
  }

  return $d;
}


///////////////////////////////////////////////////////////////////////////////
// Get the domain propertieslist
///////////////////////////////////////////////////////////////////////////////
function get_domainproperty_list() {
  global $cdg_sql;

  $d = array();
  $query = "SELECT * FROM DomainProperty";

  $obm_q = new DB_OBM;
  $obm_q->query($query);
  while ($obm_q->next_record()) {
    $key = $obm_q->f("domainproperty_key");
    $type = $obm_q->f("domainproperty_type");
    $default = $obm_q->f("domainproperty_default");
    $d[$key] = array ('type' => $type, 'default' => $default);
  }

  return $d;
}


///////////////////////////////////////////////////////////////////////////////
// Update the groups domain associations
///////////////////////////////////////////////////////////////////////////////
function update_domain_groups() {
  global $cdg_sql, $cg_adm, $cg_com, $cg_prod;

  $obm_q = new DB_OBM;

  // Admin group
  if (! ($cg_adm > 0)) { $cg_adm = 1; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_adm
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_admin'";
  $obm_q->query($q);
  echo "\nDomain group_admin : $cg_adm";

  // Commercial group
  if (! ($cg_com > 0)) { $cg_com = 2; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_com
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_com'";
  $obm_q->query($q);
  echo "\nDomain group_com : $cg_com";
 
 // Production group
  if (! ($cg_prod > 0)) { $cg_prod = 3; }
  $q = "UPDATE DomainPropertyValue
    SET domainpropertyvalue_value=$cg_prod
    WHERE domainpropertyvalue_domain_id=1
      AND domainpropertyvalue_property_key='group_prod'";
  $obm_q->query($q);
  echo "\nDomain group_prod : $cg_prod";
}


///////////////////////////////////////////////////////////////////////////////
// Insert an admin0 if not present
///////////////////////////////////////////////////////////////////////////////
function check_insert_admin0() {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  // Check if adomain 0 admin is present
  $q = "Select userobm_id FROM UserObm WHERE userobm_domain_id=0";
  $obm_q->query($q);
  if ($obm_q->num_rows() > 0) {
    echo "\nDomain 0 admin : yes\n";
  } else {
    echo "\nDomain 0 admin : no : insertion...";

    $q = "INSERT INTO UserObm (
        userobm_domain_id,
        userobm_login,
        userobm_password,
        userobm_password_type,
        userobm_perms,
        userobm_lastname,
        userobm_firstname,
        userobm_uid,
        userobm_gid)
      VALUES (
        0,
       'admin0',
       'admin',
       'PLAIN',
       'admin',
       'Admin Lastname',
       'Firstname',
       '1000',
       '512')";

    $obm_q->query($q);
    echo "OK\n";
  }

}

/**
 * Insert profiles into DB From static $profiles
 */
function make_profiles() {
  global $profiles, $cdg_sql;
  global $obm;
    
    $query = "SELECT domain_id from Domain where domain_name='global.virtual'";
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();
    $global_domain_id = $obm_q->f('domain_id');
    
    foreach($profiles as $profile_name => $one_profile) {
      
      // Vérifier que le profil n'est pas déjà dans la base

      $query = "SELECT count(*) as count FROM Profile WHERE profile_name='$profile_name' and  profile_domain_id=$global_domain_id";
      $obm_q->query($query);
      
      $nbProfiles = 0;
      while($obm_q->next_record()) {
        $nbProfiles = $obm_q->f('count');
      }
      
      if ($nbProfiles == 0) {
        
        // Insert Profile
        $now = date('Y-m-d H:i:s');

		$query = "SELECT userobm_id from UserObm where userobm_name='admin0'";
		$obm_q->query($query);
		$obm_q->next_record();
		
        $uid = $obm_q->f('userobm_id');
		        
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
        	$global_domain_id,
        	'$profile_name'
        )";
        
        $profile_q = new DB_OBM;
        $profile_q->query($query);
        
        $query = "SELECT profile_id
      FROM Profile
      WHERE
        profile_timeupdate = '$now'
        AND profile_timecreate = '$now'
        AND profile_userupdate = $uid
        AND profile_usercreate = $uid
        AND profile_domain_id = $global_domain_id
        AND profile_name = '$profile_name'
    ";
        $profile_q->query($query);
        $profile_q->next_record();
        
        $profile_id = $profile_q->f('profile_id');
        
        // Insert ProfileProperties
        $prop_q = run_of_query_property_default_list();
        
        while($prop_q->next_record()) {
          $id = $prop_q->f('profileproperty_id');
          $key = $prop_q->f('profileproperty_name');
          $default = $prop_q->f('profileproperty_default');
          
          foreach ($one_profile as $property_name => $property_value) {
            if ($key == $property_name || ($key == 'admin_realm' && $property_name == 'properties')) {
              if (isset($property_value['admin_realm']) && is_array($property_value['admin_realm'])) {
                $property_value = implode($property_value['admin_realm'],' ');
              }
              $query = "INSERT INTO ProfilePropertyValue (
              	profilepropertyvalue_profile_id,
              	profilepropertyvalue_property_id,
              	profilepropertyvalue_property_value)
              	VALUES (
              		$global_profile_id,
              		$id,
              		'$property_value')";
              		
              $profile_q->query($query);
            }
          }
        }
        
        // Insert Sections
        foreach ($one_profile['section'] as $section_name => $section_value) {
          $query = "INSERT INTO ProfileSection (
          	profilesection_section_name,
          	profilesection_domain_id,
          	profilesection_profile_id,
          	profilesection_show) VALUES (
          		'$section_name',
          		$global_domain_id,
          		$profile_id,
          		$section_value)";
          
          $obm_q->query($query);
        }
        
        // Insert Modules
        foreach ($one_profile['module'] as $module_name => $module_value) {
          $module_value = hexdec($module_value);
          $query = "INSERT INTO ProfileModule (
          	profilemodule_module_name,
          	profilemodule_domain_id,
          	profilemodule_profile_id,
          	profilemodule_right) VALUES (
          		'$module_name',
          		$global_domain_id,
          		$profile_id,
          		$module_value)";
          		
          $obm_q->query($query);
        }
      }
    }
}

function run_of_query_property_default_list() {
  global $cdg_sql;
  
  $query = "SELECT
  	profileproperty_name,
    profileproperty_default,
    profileproperty_id
  FROM ProfileProperty
  WHERE !profileproperty_readonly";
  
  $obm_q = new DB_OBM;
  
  $obm_q->query($query);
  
  return $obm_q;
}


</script>
