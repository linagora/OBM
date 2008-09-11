<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_display.inc                                          //
//     - Desc : Profile Display File                                         //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $          //
///////////////////////////////////////////////////////////////////////////////
 
//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['profile_name'] = 'Nom';

/**
 * Display profile specific dataset fields
 *
 * @param OBM_DISPLAY $OD
 * @param string $fieldname : field to display
 * @param bool $link_ok : true if links must be displayed
 * @return array : hash with 'name', 'url', 'align' values
 */
function dis_data_profile(&$OD, $fieldname, $link_ok) {
  global $path;
  
  if ($fieldname == 'profile_name') {
  	if ($OD->data_set->f($fieldname) == '') {
  		$res['name'] = '-';
  	}
  	
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_get_id('.$OD->data_set->f('profile_id').",'".addslashes($OD->data_set->f('profile_name'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f('profile_id').');';
    } else if ($OD->display_ext == 'get_id_cv') {
      $res['url'] = 'javascript:check_get_id_cv('.$OD->data_set->f('profile_id').",'".addslashes($OD->data_set->f('profile_name'))."');";
    } else {
      $res['url'] = "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=".$OD->data_set->f('profile_id');
    }
  }

  else if (in_array($fieldname, array('profile_timecreate', 'profile_timecreate'))) {
    $date = $OD->data_set->f($fieldname);
    $res['name'] = of_date_format($date);
  }
  
  /*
  else if ($fieldname == 'profile_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }
*/
  return $res;
}

/**
 * XHTML Display the profile Search result
 *
 * @param array $obm_q list of profiles
 * @param unknown_type $prefs fields to display in the list of profiles 
 * @param array $profile profile search criteria
 * @return string xHTML
 */
function html_profile_search_list($obm_q, $prefs, $profile) {
  global $l_close;

  $popup = $profile['popup'];
  $archive = $profile['archive'];
  
  if ($popup) {
    $ext_action = $profile["ext_action"];
    $ext_url = $profile["ext_url"];
    $ext_id = $profile["ext_id"];
    $ext_target = $profile["ext_target"];
    $ext_widget = $profile["ext_widget"];
    $ext_widget_text = $profile["ext_widget_text"];
    $ext_element = $profile["ext_element"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_element=$ext_element&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  $url = url_prepare("user_index.php?action=search&amp;tf_name=$profile[name]&amp;cba_archive=$archive$url_ext");
  
  $profiles_d = new OBM_DISPLAY("DATA", $prefs, "user");
  if ($popup) {
    //    if ($ext_action == "ext_get_ids") {
    $profiles_d->display_link = false;
    $profiles_d->data_cb_text = "X";
    $profiles_d->data_idfield = "userobm_id";
    $profiles_d->data_cb_name = "data-u-";
    if ($ext_element != "") {
      $profiles_d->data_cb_name = "";
      $profiles_d->data_form_head = "
      <form onsubmit=\"of_select_fill_from_checkbox(this); return false;\">";
    } elseif ($ext_widget != "") {
      $profiles_d->data_form_head = "
      <form onsubmit=\"fill_ext_form(this); return false;\">";
    } else {
      $profiles_d->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    }
    $profiles_d->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </div>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $profiles_d->data_set = $obm_q;
  $profiles_d->data_header = "both";
  $profiles_d->data_url = $url;

  // --- HTML Template --------------------------------------------------------
  $block .= $profiles_d->display("dis_data_profile");
  $block .= $display_popup_end;

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
     // begin make $function_make_right_field
  
  $checked_condition = 'if (isset($rights[$module_name][$right_name])
      && $rights[$module_name][$right_name])';
  $parameters_make_right_field = '$rights, $module_name, $right_name';
  
  if ($action == 'consult') {
    $function_make_right_field = create_function($parameters_make_right_field,
      $checked_condition. ' return \'X\';
      return \'\';');
  } else {
    $function_make_right_field = create_function($parameters_make_right_field,
      $checked_condition. ' $checked = "checked=\"checked\""; 
      return "<label for=\"cbx_${module_name}_${right_name}\" class=\"block\">
      <input type=\"checkbox\" name=\"cba_${module_name}_right_${right_name}\"
      id=\"cbx_${module_name}_${right_name}\" $checked /></label>";');
  }
  
      // end make $function_make_right_field 
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
        <tr><th>Nom</th><td>$field[name]</td></tr>
      </tbody>
    </table>
  </div>";
  
  $block .= "<div class=\"result extra\">";
  
  foreach ($obm_modules as $section_name => $modules) {
    $block .= "<h1>". $GLOBALS['l_section_'. $section_name]. "</h1>";
    $block .= "<input type=\"checkbox\" /> Désactiver la section";
    
	  $block .= "<table><thead>
	      <tr><td>Nom module</td>
	        <td>Lecture</td>
	        <td>Ecriture</td>
	        <td>Lecture admin</td>
	        <td>Ecriture admin</td>
	        <td>Propriétaire</td>
	      </tr>
	    </thead><tbody>";
	  
	  $i = 0;
  	foreach ($modules as $module_name) {
      $block .= "<tr". ($i%2 == 1 ? ' class="pair"':''). "><td>". $GLOBALS['l_module_'. $module_name]. "</td>"
        . "<td class=\"C\">". $function_make_right_field($value['rights'], $module_name, 'read'). "</td>"
        . "<td class=\"C\">". $function_make_right_field($value['rights'], $module_name, 'write'). "</td>"
        . "<td class=\"C\">". $function_make_right_field($value['rights'], $module_name, 'read_admin'). "</td>"
        . "<td class=\"C\">". $function_make_right_field($value['rights'], $module_name, 'write_admin'). "</td>"
        . "<td class=\"C\">". $function_make_right_field($value['rights'], $module_name, 'own'). "</td>"
        . "</tr>";
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
	  
	  $form_block .= "<form action=\"\" method=\"get\">";
	  $form_block .= $block;
    $form_block .= "<input type=\"hidden\" name=\"action\" value=\"$action\"/>";
    $form_block .= "<input type=\"hidden\" name=\"profile_id\" value=\"$params[profile_id]\"/>";
    $form_block .= "<div class=\"buttons\">";
    $form_block .= "<input type=\"submit\" />";
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
