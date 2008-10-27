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

make_profiles();

/**
 * Insert profiles into DB From static $profiles
 */
function make_profiles() {
  global $profiles, $cdg_sql;
  global $obm;

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
  $query = "SELECT *  FROM ProfileProperty";
  $obm_q->query($query);
  while($obm_q->next_record()) {
    $properties[$obm_q->f('profileproperty_name')] = array('default' => $obm_q->f('profileproperty_default'), 'id' => $obm_q->f('profileproperty_id'));
  }
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
        
    // Insert Modules
    foreach ($data['module'] as $module_name => $module) {
      $module_value = hexdec($module_value);
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
           ".hexdec($module)." 
          )";    
      $obm_q->query($query);
    }

    foreach ($properties as $property_name => $property_data) {
      if(isset($data['properties'][$property_name])) {
        $property = $data['properties'][$property_name];
        if($property_name == 'admin_realm')  {
          $property = implode(' ', $property);
        }
      } else {
        $property = $property_data['default'];
      }
      echo "**** Setting property $property_name : $property\n";
      $query = "INSERT INTO ProfilePropertyValue (
              	profilepropertyvalue_profile_id,
              	profilepropertyvalue_property_id,
                profilepropertyvalue_property_value
              ) VALUES (
                $profile_id,
                $property_data[id],
                '$property'
              )";
      $obm_q->query($query);
    }
  }
}


</script>
