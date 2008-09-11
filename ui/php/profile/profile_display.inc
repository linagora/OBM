<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_display.inc                                          //
//     - Desc : Profile Display File                                         //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $          //
///////////////////////////////////////////////////////////////////////////////


//FIXME
$l_module_name = "Module";
//$l_name = "Nom";
$l_right_default =
$l_right_read = "Lecture";
$l_right_write = "Ecriture";
$l_right_own = "Proprio";
$l_right_read_admin = "Lecture Adm";
$l_right_write_admin = "Ecriture Adm";
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
	if ($action == 'new')
	  $action = 'insert';
	  
  if ($action == 'detailupdate')
    $action = 'update';
	
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
	global $cright_list;
	
	// labels
  foreach ($cright_list as $right_name) { global ${'l_right_'. $right_name}; }
	global $l_right_default, $l_name, $l_module_name;
	
	$obm_modules = get_obm_modules();
	
  // --------------------------
  // begin init $value with database parameters
	
  if ($action == 'consult' || $action == 'update') {
	  $value['name'] = $profile['name'];
	  $value['rights'] = $profile['modules_right'];
  }
  
  // end init $value with database parameters
  // --------------------------
	
  // --------------------------
  // begin init $value with passed parameters
  
	if ($action == 'insert' || $action == 'update') {
	  if (isset($params['name'])) {
	    $value['name'] = $params['name'];
		    
		  foreach ($profile['modules_right'] as $module_name => $rights) {
		  	foreach ($rights as $right_name => $right_value) {
		  		if ($right_name != 'right') {
			  		if (isset($params["${module_name}_right_${$right_name}"]))
			  		  $value['rights'][$module_name][$right_name] = true;
			  		else
		          $value['rights'][$module_name][$right_name] = false;
		      }
		  	}
		  }
    }
	}

  // end init $value with passed parameters
  // --------------------------
  
  // --------------------------
  // begin prepare fields
  
	
  if ($action == 'insert' || $action == 'update') {
    $field['name'] = "<input type=\"text\" name=\"tf_name\" maxlength=\"32\" size=\"32\" value=\"$value[name]\" />";
  } else if ($action == 'consult') {
    $field['name'] = $value['name'];
  }
	
     // --------------------------
     // begin make $function_make_checkbox_field
     
  $checked_condition = 'if ($checked)';
  $parameters_make_checkbox_field = '$name, $checked, $ext';
  
  if ($action == 'consult') {
    $function_make_checkbox_field = create_function($parameters_make_checkbox_field,
      $checked_condition. ' return \'X\';
      return \'&nbsp;\';');
  } else {
    $function_make_checkbox_field = create_function($parameters_make_checkbox_field,
      $checked_condition. ' $checked = "checked=\"checked\""; 
      return "<label for=\"cbx_$name\" class=\"block\">
      <input type=\"checkbox\" name=\"cba_$name\" id=\"cbx_$name\" $checked $ext/></label>";');
  }
  
      // end make $function_make_checkbox_field 
      // --------------------------
  
  // end prepare fields
	// --------------------------
  
	
  // --------------------------
  // begin init $block
  
  $block = '';
  $block = "
  <style>
    label.block {
      display:block;
      border:1px solid #D2D2D2;
    }
  </style>
  <div class=\"detail infos\">
    <table>
      <tbody>
        <tr><th>$l_name</th><td>$field[name]</td></tr>
      </tbody>
    </table>
  </div>";
  
  $block_rights_header = '';
  foreach ($cright_list as $right_name) {
    $block_rights_header .= "<td>". ${'l_right_'. $right_name}. "</td>";
  }
  
  $block .= "<div class=\"result extra\">
    <h1>Permissions par défaut</h1>
    <table><thead>
      <tr>$block_rights_header</tr>
      </thead>
      <tbody>
      <tr class=\"C\">";
  
  foreach ($cright_list as $right_name) {
  	$right_value = isset($value['rights']['default']) && $value['rights']['default'][$right_name];
  	$block .= '<td>'. $function_make_checkbox_field('default_right_'. $right_name, $right_value, ''). '</td>';
  }
  
  $block .= "</tr>
      </tbody>
    </table>
  </div>";
  
  
  $block .= "<div class=\"result extra\">";
  
  foreach ($obm_modules as $section_name => $modules) {
    $block .= "<h1>". $GLOBALS['l_section_'. $section_name]. "</h1>";
    $block .= "<input type=\"checkbox\" /> Désactiver la section"; //FIXME truc à compléter
    
    //FIXME labels
	  $block .= "<table><thead>
	      <tr><td>$l_module_name</td>
          <td>$l_right_default</td>
	        $block_rights_header
	      </tr>
	    </thead><tbody>";
	  
	  $i = 0;
  	foreach ($modules as $module_name) {
      $block .= "<tr". ($i%2 == 1 ? ' class="pair"':''). "><td>". $GLOBALS['l_module_'. $module_name]. "</td>";
      
      $block .= "<td class=\"C\">". $function_make_checkbox_field($module_name. '_default',
        !isset($value['rights'][$module_name]), "onclick=\"select_default('$module_name')\""). "</td>";
      
      foreach ($cright_list as $right_name) {
      	$checked = isset($value['rights'][$module_name][$right_name]) && $value['rights'][$module_name][$right_name];
      	$block .= "<td class=\"C\">". $function_make_checkbox_field($module_name. '_right_'. $right_name,
      	  $checked, "onclick=\"select_right('$module_name')\""). "</td>";
      }
      
      $block .= "</tr>";
      $i += 1;
  	}
  	
    $block .= "</tbody></table>";
  }
  
  $block .= "</div>";
  
  // end init $block
  // --------------------------
  
  
  // --------------------------
  // begin make form
  
  if ($action != 'consult') {
	  $form_block = '';
	  
	  $form_block .= "<form action=\"\" method=\"post\">";
	  $form_block .= $block;
    $form_block .= "<input type=\"hidden\" name=\"action\" value=\"$action\"/>";
    $form_block .= "<input type=\"hidden\" name=\"profile_id\" value=\"$params[profile_id]\"/>";
    $form_block .= "<div class=\"buttons\">";
    $form_block .= "<input type=\"submit\" />"; // FIXME label bouton
    $form_block .= "</div>";
	  $form_block .= "</form>";
  
    $block = $form_block;
  }
  
  // end make form
  // --------------------------
  
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
