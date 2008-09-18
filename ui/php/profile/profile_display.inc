<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_display.inc                                          //
//     - Desc : Profile Display File                                         //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $          //
///////////////////////////////////////////////////////////////////////////////


//FIXME To put in lang files
$l_module_name = "Module";
//$l_name = "Nom";
$l_right_default = "Par défaut";
$l_right_read = "Lecture";
$l_right_write = "Ecriture";
$l_right_own = "Proprio";
$l_right_read_admin = "Lecture Adm";
$l_right_write_admin = "Ecriture Adm";
$l_default_permissions = "Permissions par défaut";
$l_general_properties = "Propriétés principal";
//FIXME

/**
 * Produce HTML Profile List content
 *
 * @param DB_OBM $profile_q
 * @return string xHTML
 */
function html_profile_list ($profile_q) {
	$block = '';
	
	$block .= "<div class=\"result\"><table>
    <thead><tr><td>Nom</td></tr></thead><tbody>";
	
	while ($profile_q->next_record()) {
		$profile_name = $profile_q->f('profile_name');
		if ($profile_name == '')
		  $profile_name = '-';
		
		$url_consult = url_prepare("profile_index.php?action=detailconsult&amp;profile_id=". $profile_q->f('profile_id'));
		$block .= "<tr>
		  <td><a href=\"$url_consult\">$profile_name</a></td>
		</tr>";
	}
	
	$block .= "</tbody></table></div>";
	
	return $block;
}

/**
 * Produce a HTML Profile consult detail display
 *
 * @param array $params
 * @param array $profile
 * @return string xHTML
 */
function html_profile_consult($params, $profile) {
  return html_profile_superform($params, 'consult', $profile);
}

function html_profile_form($action, $params, $profile = array()) {
  if ($action == 'new') { $action = 'insert'; }
  
  if ($action == 'detailupdate' && ($profile['profile_id'] > 0 || $params['profile_id'])) {
    $action = 'update';
  } else if ($action == 'detailupdate') {
    $action = 'insert';
  }
	
  return html_profile_superform($params, $action, $profile);
}

/**
 * Produce a HTML Profile edit form/consult detail display  
 *
 * @param array $params
 * @param array $action consult/insert/update
 * @param array $profile full data profile array
 * @return string xHTML
 */
function html_profile_superform($params, $action, $profile = array()) {
  
  /////////// Init /////////////////////////////////////////////// BEGIN
  
  global $cright_list;
	
  // labels
  global $l_right_default, $l_name, $l_module_name, $l_properties;
  
  // titles
  global $l_default_permissions, $l_general_properties;
  
  // table column names
  foreach ($cright_list as $right_name) { global ${'l_right_'. $right_name}; }
	
  $obm_modules = get_obm_modules();
	
  // init $value with database parameters
  if ($action == 'consult' || $action == 'update') {
	$value['name']          = $profile['name'];
    $value['modules_right'] = $profile['modules_right'];
    $value['sections_show'] = $profile['sections_show'];
    $value['properties']    = $profile['properties'];
  }
  
  // init $value with passed parameters  
  if ( ($action == 'insert' || $action == 'update') &&
       ($params['action'] == 'insert' || $params['action'] == 'update')
     ) {
	$value['name']          = $params['name'];
	$value['modules_right'] = $params['modules_right'];
    $value['sections_show'] = $params['sections_show'];
    $value['properties']    = $params['properties'];
  } else if ($action == 'insert') {
    $value['properties']    = $params['properties'];
  }
  /////////// Init /////////////////////////////////////////////// END

  /////////// Prepare fields ///////////////////////////////////// BEGIN
  
  if ($action == 'insert' || $action == 'update') {
    
    // Profile name
    $field['name'] = "<input type=\"text\" name=\"tf_name\" maxlength=\"64\" size=\"32\" value=\"$value[name]\" />";
    
    // Profile properties
    foreach ($value['properties'] as $pp_name => $pp_value) {
      $field[$pp_name] = "<input type=\"text\" name=\"tf_$pp_name\" value=\"$pp_value[value]\" />";
    }
    
  } else if ($action == 'consult') {
    
    // Profile name
    $field['name'] = $value['name'];
    
    // Profile properties
    foreach ($value['properties'] as $pp_name => $pp_value) {
      $field[$pp_name] = $pp_value['value'];
    }
  }
  
  // make $function_make_checkbox_field
  $checked_condition = 'if ($checked)';
  $parameters_make_checkbox_field = '$name, $checked, $ext';
  
  if ($action == 'consult') {
    $function_make_checkbox_field = create_function($parameters_make_checkbox_field,
      $checked_condition. ' return \'X\';
      return \'&nbsp;\';');
  } else {
    $function_make_checkbox_field = create_function($parameters_make_checkbox_field,
      $checked_condition. ' $checked = "checked=\"checked\""; 
      return "<label for=\"cbx_$name\">
      <input type=\"checkbox\" name=\"cba_$name\" id=\"cbx_$name\" value=\"1\" $checked $ext/></label>";');
  }
  /////////// Prepare fields ///////////////////////////////////// END
  
  /////////// Make HTML blocks /////////////////////////////////// BEGIN
  
  // Custom CSS styles
  $block_style = "
  <style>
    table.perms label {
      display:block;
      border:1px solid #D2D2D2;
    }
    .default_perm label {
      text-align:right;
      border:0px;
    }
  </style>";
  
  // Profile name
  $block_name = "
  <div class=\"detail infos\">
    <h1>$l_general_properties</h1>
    <table>
      <tbody>
        <tr><th>$l_name</th><td>$field[name]</td></tr>
      </tbody>
    </table>
  </div>";
  
  // Profile properties
  $block_properties = "
  <div class=\"detail infos\">
    <h1>$l_properties</h1>
    <table>
      <tbody>";
      foreach ($value['properties'] as $pp_name => $pp_value) {
        global ${'l_'.$pp_name};
        $block_properties .= "<tr><th>".${'l_'.$pp_name}."</th><td>".$field[$pp_name]."</td></tr>";
      }
  $block_properties .= "
  	  </tbody>
  	</table>
  </div>";
  
  // Profile default permissions
  $block_defaultPerm = "
  <div class=\"detail infos\">
    <h1>$l_default_permissions</h1>
    <table class=\"default_perm\">
      <tbody>";
      foreach ($cright_list as $right_name) {
  	    $ename = 'default_right_'. $right_name;
  	    $right_value = $value['modules_right']['default'][$right_name];
  	    $block_defaultPerm .= '<tr><td class=\"C\">'. $function_make_checkbox_field($ename, $right_value, '')
  	      . "</td><td><label for=\"cbx_$ename\">". ${'l_right_'. $right_name}. "</a></td></tr>";
      }
  $block_defaultPerm .= "
      </tbody>
    </table>
  </div>";
  
  // Profile section/modules
  $block_sectionsModules .= "<div class=\"result extra\">";
  
  $block_rights_header = '';
  foreach ($cright_list as $right_name) {
    $block_rights_header .= "<td>". ${'l_right_'. $right_name}. "</td>";
  }
  
  foreach ($obm_modules as $section_name => $modules) {
    $block_table = '';
    //$block_table .= "<input type=\"checkbox\" /> Désactiver la section"; //FIXME truc à compléter
    
	  $block_table .= "<table class=\"perms\"><thead>
	      <tr><td>$l_module_name</td>
          <td>$l_right_default</td>
	        $block_rights_header
	      </tr>
	    </thead><tbody>";
	  
	  $i = 0;
  	foreach ($modules as $module_name) {
      $block_table .= "<tr". ($i%2 == 1 ? ' class="pair"':''). "><td>". $GLOBALS['l_module_'. $module_name]. "</td>";
      
      $checked = !isset($value['modules_right']) || $value['modules_right'][$module_name]['default'];
      $block_table .= "<td class=\"C\">". $function_make_checkbox_field($module_name. '_default',
        $checked, "onclick=\"select_default('$module_name')\""). "</td>";
      
      foreach ($cright_list as $right_name) {
      	$checked = $value['modules_right'][$module_name][$right_name];
      	$block_table .= "<td class=\"C\">". $function_make_checkbox_field($module_name. '_right_'. $right_name,
      	  $checked, "onclick=\"select_right('$module_name')\""). "</td>";
      }
      
      $block_table .= "</tr>";
      $i += 1;
  	}
  	
    $block_table .= "</tbody></table>";
    
    $checked = $value['sections_show'][$section_name];
    $block_sectionsModules .= "<div class=\"detail extra\"\">
      <h1>". $GLOBALS['l_section_'. $section_name]. "</h1>
      <table>
        <tbody>
        <tr><td>". $function_make_checkbox_field($section_name. '_show',
          $checked, ''). "Désactiver</td><td></td></tr>
      <tbody></table>
      $block_table</div>";
  }
  
  $block_sectionsModules .= "</div>";
  
  // Concatenate blocks
  $block =  $block_style
           .$block_name
           .$block_properties
           .$block_defaultPerm
           .$block_sectionsModules;
  
  // Build profile form
  if ($action != 'consult') {
    $form_block = '';  
	$form_block .= "<form action=\"\" method=\"post\" onsubmit=\"if (check_profile(this)) return true; else return false;\">";
	$form_block .= $block;
    $form_block .= "<input type=\"hidden\" name=\"action\" value=\"$action\"/>";
    $form_block .= "<input type=\"hidden\" name=\"profile_id\" value=\"$params[profile_id]\"/>";
    $form_block .= "<div class=\"buttons\">";
    $form_block .= "<input type=\"submit\" />"; // FIXME label bouton
    $form_block .= "</div>";
	$form_block .= "</form>";
  
    $block = $form_block;
  }
  /////////// Make HTML blocks /////////////////////////////////// END
  return $block;
}

/**
 * Display the Project Display preference screen
 *
 * @param array $prefs
 * @return string
 */
function dis_profile_display_pref($prefs) {
  global $l_profile_display;

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'profile');
  $dis_pref->pref_title = $l_profile_display;

  $block .= '<td>';
  $block .= $dis_pref->display();
  $block .= '</td></tr></table>';

  $block .= $dis_pref->dis_pref_help();
  return $block;
}

/**
 * Display profile properties options
 * 
 * @param params
 * @param array of properties
 * @return String
 */
function html_properties_consult($params, $properties_q) {
global $l_properties;
  
  while($properties_q->next_record()) {
    $readonly = $properties_q->f('profileproperty_readonly');
    if ($readonly != '1') {
      $name = $properties_q->f('profileproperty_name');
      $value = $properties_q->f('profilepropertyvalue_property_value');
      $default = $properties_q->f('profileproperty_default');
      echo "name : $name, value : $value, default_value : $default";
      if (empty($value)) {
        $value = $default;
      }
      $plabel = $GLOBALS["l_$name"];
      
      $properties_block .= "
      	<tr>
      	<th>$plabel</th>
      	<td>$value</td>
      	</tr>";
    }
  }
  if ($properties_block != '') {
    $properties_block = "
    	<fieldset class=\"detail infos\">
    	<legend>$l_properties</legend>
    	<table>
    	$properties_block
    	</table>
    	</fieldset>";
  }
  
  return $properties_block;
}

/**
 * Produce a profile properties form
 * 
 * @param $action
 * @param $params
 * @param $properties
 */
function html_properties_form($action, $params, $properties_q) {
  global $l_properties;
  
  while($properties_q->next_record()) {
    $readonly = $properties_q->f('profileproperty_readonly');
    if ($readonly != '1') {
      $name = $properties_q->f('profileproperty_name');
      $value = $properties_q->f('profilepropertyvalue_property_value');
      $default = $properties_q->f('profileproperty_default');
      echo "name : $name, value : $value, default_value : $default";
      if (empty($value)) {
        $value = $default;
      }
      $plabel = $GLOBALS["l_$name"];
      
      $properties_block .= "
      	<tr>
      	<th>$plabel</th>
      	<td><input type=\"text\" name=\"$name\" value=\"$value\" /></td>
      	</tr>";
    }
  }
  if ($properties_block != '') {
    $properties_block = "
    	<fieldset class=\"detail infos\">
    	<legend>$l_properties</legend>
    	<table>
    	$properties_block
    	</table>
    	</fieldset>";
  }
  
  $form_block = "";
  
  $form_block .= "<form action=\"\" method=\"post\">";
	  $form_block .= $properties_block;
    $form_block .= "<input type=\"hidden\" name=\"action\" value=\"$action\"/>";
    $form_block .= "<input type=\"hidden\" name=\"profile_id\" value=\"$params[profile_id]\"/>";
    $form_block .= "<div class=\"buttons\">";
    $form_block .= "<input type=\"submit\" />"; // FIXME label bouton
    $form_block .= "</div>";
	  $form_block .= "</form>";
  
    $block = $form_block;
    
    return $block;
}
?>
