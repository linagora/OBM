<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_display.inc                                          //
//     - Desc : Profile Display File                                         //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $          //
///////////////////////////////////////////////////////////////////////////////


global $l_module_name;
global $l_right_default;
global $l_right_read;
global $l_right_write;
global $l_right_own;
global $l_right_read_admin;
global $l_right_write_admin;
global $l_default_permissions;
global $l_general_properties;


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
  global $l_section_disable;
  
  // titles
  global $l_default_permissions, $l_general_properties;
  
  // table column names
  foreach ($cright_list as $right_name) { global ${'l_right_'. $right_name}; }
	
  $obm_modules = get_obm_modules();
	
  // init $value with database parameters
  if ($action == 'consult' || $action == 'update') {
	$value['name']          = $profile['name'];
    $value['modules_right'] = $profile['modules_right'];
    $value['default_section_show'] = $profile['sections_show']['default'];
    $value['sections_show'] = $profile['sections_show'];
    $value['properties']    = $profile['properties'];
  }
  
  // init $value with passed parameters  
  if ( ($action == 'insert' || $action == 'update') &&
       ($params['action'] == 'insert' || $params['action'] == 'update')
     ) {
	$value['name']          = $params['name'];
	$value['modules_right'] = $params['modules_right'];
	$value['default_section_show'] = $params['sections_show']['default'];
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
  
  // Default Sections show
  $checked_rd1 = "";
  $checked_rd2 = "";
  if (!isset($value['default_section_show']) || $value['default_section_show']) {
    $checked_rd1 .= "checked=\"checked\"";
  } else {
    $checked_rd2 .= "checked=\"checked\"";
  }
  
  if ($action != 'consult') {
    $field['default_section_show_true'] = "<input type=\"radio\" name=\"rd_default_section_show\" value=\"1\" $checked_rd1 onchange=\"setSectionState('active')\" />";
    $field['default_section_show_false'] = "<input type=\"radio\" name=\"rd_default_section_show\" value=\"0\" $checked_rd2 onchange=\"setSectionState('deactive')\" />";
  } else {
    $field['default_section_show_true'] = ($checked_rd1 == "checked=\"checked\"" ? "X" : ""); 
    $field['default_section_show_false'] = ($checked_rd2 == "checked=\"checked\"" ? "X" : "");
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
      return "<input type=\"checkbox\" name=\"cba_$name\" id=\"cbx_$name\" value=\"1\" $checked $ext/>";');
  }
  /////////// Prepare fields ///////////////////////////////////// END
  
  /////////// Make HTML blocks /////////////////////////////////// BEGIN
  
  // Custom JavaScript
  $block_js = "<script type=\"text/javascript\" language=\"JavaScript\">";
  if (!isset($value['default_section_show']) || $value['default_section_show']) {
    $sectionAction = "active";
    } else {
    $sectionAction = "deactive";
  }
  $block_js .= "setSectionState('$sectionAction');
  	</script>";
  
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
    .blockRight {
    	text-align: left;
    	float: right;
    }
    table.allSections {
    	text-align: center;
    	width: auto;
   	}
   	table.allSections tr td {
   		vertical-align: top;
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
          /*if ($action != "consult" && $pp_name == "admin_realm") {
            global ${'l_'.$pp_name."_info"};
            $block_properties .= "<tr><td colspan=\"2\"><div class=\"blockRight\">".${'l_'.$pp_name."_info"}."</div></td></tr>";
          }*/
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
  global $l_active_section, $l_section_all, $l_section_none;
  $block_allSections = "<div class=\"detail\" style=\"clear: left\">";
  $block_allSections .= "<h1>$l_active_section</h1>
    <table class=\"allSections\"><tbody>
      <tr><td><label>$l_section_all<br />$field[default_section_show_true]</label></td>
          <td><label>$l_section_none<br />$field[default_section_show_false]</label></td>
      </tr>
    </tbody></table>
  </div>";
  
  $block_sectionsModules = "<div class=\"result\">";
  
  $block_rights_header = '';
  foreach ($cright_list as $right_name) {
    $block_rights_header .= "<td>". ${'l_right_'. $right_name}. "</td>";
  }
  
  foreach ($obm_modules as $section_name => $modules) {
    $block_table = '';
    
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
    
    if ($value['sections_show']['default']) {
      $checked = ($value['sections_show'][$section_name] ? false : true);
    } else {
      $checked = ($value['sections_show'][$section_name] ? true : false);
    }
    $block_sectionsModules .= "<div class=\"detail extra\"\">
      <h1>". $GLOBALS['l_section_'. $section_name]. "</h1>";
    

     if ($action != 'consult' || ($action == 'consult' && ($value['sections_show']['default'] && $checked))
                              || ($action == 'consult' && (!$value['sections_show']['default'] && $checked))) {
     
       $block_sectionsModules .= "<div><label for=\"cbx_$section_name\" class=\"activeSection\"></label>"
                                 .$function_make_checkbox_field($section_name. '_show', $checked, '')
                                 ."</div>";
     }

     if ($action != 'consult' || ($action == 'consult' && ($value['sections_show']['default'] && !$checked))
                              || ($action == 'consult' && (!$value['sections_show']['default'] && $checked))) {
       $block_sectionsModules .= $block_table;
     }
    
    $block_sectionsModules .= "</div>";
  }
  
  $block_sectionsModules .= "</div>";
  
  // Concatenate blocks
  $block =  $block_style
           .$block_name
           .$block_properties
           .$block_defaultPerm
           .$block_allSections
           .$block_sectionsModules
           .$block_js;
  
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

?>
