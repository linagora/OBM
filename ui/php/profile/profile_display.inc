<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : profile_display.inc                                          //
//     - Desc : Profile Display File                                         //
// 2008-09-10 Christophe LIOU KEE ON                                         //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile_display.inc,v 1.198 2007/02/20 15:16:10 mehdi Exp $          //
///////////////////////////////////////////////////////////////////////////////

$fieldnames['profile_name'] = $l_name;

///////////////////////////////////////////////////////////////////////////////
// Display Profile specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_profile(&$OD, $fieldname, $link_ok) {
  global $path, $params;

  if ($fieldname == 'profile_name') {
    $profile_name = $OD->data_set->f('profile_name');
    $res['url'] = "$path/profile/profile_index.php?action=detailconsult&amp;profile_id=".$OD->data_set->f('profile_id');
    $res['name'] = $profile_name;
  }
  return $res;
}
///////////////////////////////////////////////////////////////////////////////
// Display: Profile search result
// Parameters:
//   - $profile[] : profile search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_profile_search_list($profile) {
  global $display, $l_found, $l_no_found, $obm;

  $popup = $profile['popup'];

  $prefs = get_display_pref($obm['uid'], 'profile');
  $obm_q = run_query_profile_search($profile);
  $nb_profile = $obm_q->num_rows_total();
  if ($nb_profile == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_profile $l_found");
    $block = html_profile_search_list($obm_q, $prefs, $profile, $popup);
  }

  return $block;
}

/**
 * Display Profile search form
 *
 * @param $profile[] : hash with profile values
 **/
function dis_profile_search_form($profile='') {
  global $cgp_hide;

  $block = html_profile_search_form($profile);

  return $block;
}


/**
 * Display: XHTML Profile search Form
 *
 * @param $profile[] : default form values
 **/
function html_profile_search_form($profile) {
  global $action, $cgp_hide, $l_find, $popup, $display;
  global $l_name;

  $name = stripslashes($profile['name']);

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"profile_index.php\">
    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"12\" value=\"$name\" />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Profile search result
// Parameters : 
//   - $obm_q     : list of profiles to display
//   - $prefs     : the fields which have to be displayed
//   - $profile[] : profile search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_profile_search_list($obm_q, $prefs, $profile, $popup) {
  global $l_add, $l_close;

  $name = urlencode(stripslashes($profile['name']));

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('profile', $profile);

  $url = url_prepare("profile_index.php?action=search&amp;tf_name=$name");

  $dis_profile = new OBM_DISPLAY('DATA', $prefs, 'profile');

  $dis_profile->data_set = $obm_q;
  $dis_profile->data_url = $url;
  $dis_profile->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_profile->display('dis_data_profile');
  $block .= $display_popup_end;

  return $block;
}



/**
 * html_profile_consult 
 * 
 * @param mixed $params 
 * @param mixed $profile 
 * @access public
 * @return void
 */
function html_profile_consult($profile) {
  global $l_user, $l_editor, $l_admin, $l_default;
  global $l_default_settings, $l_specific_settings, $l_rights;
  global $l_modules_right, $l_sections_right, $l_section_enabled;
  global $l_name, $l_properties, $l_informations;
  $i = 0;
  foreach($profile['module'] as $name => $rights) {
    $i++;
    $class = ($i%2 == 0)? 'class="pair"':'';
    if($name == 'default') {
      $dis_modules_default .= "
        <tr $class>
        <td>$l_default</td>
        <td>".dis_module_status('user', $rights, $name)."</td>
        <td>".dis_module_status('editor', $rights, $name)."</td>
        <td>".dis_module_status('admin', $rights, $name)."</td>
        </tr>";
    } else {
      $dis_modules .= "
        <tr $class>
        <td>".$GLOBALS["l_module_$name"]."</td>
        <td>".dis_module_status('user', $rights, $name)."</td>
        <td>".dis_module_status('editor', $rights, $name)."</td>
        <td>".dis_module_status('admin', $rights, $name)."</td>
        </tr>";
    }
  }

  $i = 0;
  foreach($profile['section'] as $name => $show) {
    $i++;
    $class = ($i%2 == 0)? 'class="pair"':'';    
    if($name == 'default') {
      $dis_section_default .= "
        <tr $class>
        <td>$l_default</td>
        <td>".dis_section_status($show, $name)."</td>
        </tr>";
    } else {
      $dis_section .= "
        <tr $class>
        <td>".$GLOBALS["l_section_$name"]."</td>
        <td>".dis_section_status($show, $name)."</td>
        </tr>";
    }
  }
  foreach($profile['property'] as $name => $value) {
    $dis_properties .= "
      <tr $class>
      <th><dfn title='".$GLOBALS["l_help_$name"]."' class='helpTooltip'>".$GLOBALS["l_$name"]."</dfn></th>
      <td>".dis_property_value($name, $value)."</td>
      </tr>";
  }  
  $block = "
    <div class=\"detail infos\">
    <h1>$l_informations</h1>
    <table>
    <tr><th>$l_name</th><td>$profile[name]</td>
    </table>
    </div>
    <div class=\"detail infos\">
    <h1>$l_properties</h1>
    <table>
    $dis_properties
    </table>
    </div>    
    <div class=\"detail extra\">
    <h1>$l_modules_right</h1>
    <table class='spreadSheet'>
    <tr><th>$l_default_settings</th><th>$l_user</th><th>$l_editor</th><th>$l_admin</th></tr>
    $dis_modules_default
    <tr><th>$l_specific_settings</th><th>$l_user</th><th>$l_editor</th><th>$l_admin</th></tr>
    $dis_modules
    </table>
    </div>    
    <div class=\"detail extra\">
    <h1>$l_sections_right</h1>
    <table class='spreadSheet'>
    <tr><th>$l_default_settings</th><th>$l_section_enabled</th></tr>
    $dis_section_default
    <tr><th>$l_specific_settings</th><th>$l_section_enabled</th></tr>
    $dis_section
    </table>
    </div>        
    ";
  return $block;
}


/**
 * html_profile_form 
 * 
 * @param mixed $params 
 * @param array $profile 
 * @access public
 * @return void
 */
function html_profile_form($profile) {
  global $l_user, $l_editor, $l_admin, $l_default;
  global $l_default_settings, $l_specific_settings, $l_rights;
  global $l_modules_right, $l_sections_right, $l_section_enabled;
  global $l_name, $l_properties, $l_informations, $action;
  global $l_add_module_rights, $l_add,$l_add_section_enabled ;
  global $l_insert, $l_update;
  if($action == 'new' || $action == 'insert') {
    $dis_buttons = '
      <input type="hidden" name="action" value="insert" />
      <input type="submit" value="'.$l_insert.'" />
      ';
    $dis_name = "<input type='text' name='tf_name' value='$profile[name]' />";
  } else {
    $dis_name = $profile['name']."<input type='hidden' name='tf_name' value='$profile[name]' />";;
    $dis_buttons = '
      <input type="hidden" name="action" value="update" />
      <input type="submit" value="'.$l_update.'" />
      <input type="hidden" name="profile_id" value="'.$profile['profile_id'].'"
      ';
  }

  $i = 0;
  if(is_array($profile['module']))
  foreach($profile['module'] as $name => $rights) {
    $i++;
    $class = ($i%2 == 0)? 'class="pair"':'';
    if($name != 'default') {
      $dis_modules .= "
        <tr $class id='module_$name'>
        <td>
        <a onclick=\"$('module_$name').dispose();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>                        
        <input type='hidden' name='cb_rights[$name][]' value='0' />
        ".$GLOBALS["l_module_$name"]."
        </td>
        <td>".dis_module_status('user',$rights, $name, true)."</td>
        <td>".dis_module_status('editor',$rights, $name, true)."</td>
        <td>".dis_module_status('admin',$rights, $name, true)."</td>
        </tr>";
    }
  }

  $i = 0;
  if(is_array($profile['section']))
  foreach($profile['section'] as $name => $show) {
    $i++;
    $class = ($i%2 == 0)? 'class="pair"':'';    
    if($name != 'default') {
      $dis_section .= "
        <tr $class id='section_$name'>
        <td>
        <a onclick=\"$('section_$name').dispose();return false\" href=\"\">
         <img src=\"$GLOBALS[ico_delete]\" alt=\"[Delete]\">
        </a>                
        ".$GLOBALS["l_section_$name"]."
        <input type='hidden' name='cb_enabled[$name]' value='0' />
        </td>
        <td>".dis_section_status($show, $name, true)."</td>
        </tr>";
    }
  }
  foreach($profile['property'] as $name => $value) {
    $dis_properties .= "
      <tr >
      <th><dfn title='".$GLOBALS["l_help_$name"]."' class='helpTooltip'>".$GLOBALS["l_$name"]."</dfn></th>
      <td>".dis_property_value($name, $value, true)."</td>
      </tr>";
  }  

  $js_modules = "var modules = new Hash();\n";
  foreach($GLOBALS['cgp_show']['module'] as $module => $section) {
    $label = phpStringToJsString($GLOBALS["l_module_$module"]);
    $js_modules .= "modules.set('$module', '$label');\n";
  }
  $js_sections = "var sections = new Hash();\n";
  foreach($GLOBALS['cgp_show']['section'] as $section => $enabled) {
    $label = phpStringToJsString($GLOBALS["l_section_$section"]);
    $js_sections .= "sections.set('$section','$label');\n";
  }  
  $block = "
    <script type='text/javascript'>
    $js_modules
    $js_sections
    </script>
    <form method='POST' action='profile_index.php'>
    <fieldset class=\"detail infos\">
    <legend>$l_informations</legend>
    <table>
    <tr><th>$l_name</th><td>$dis_name</td>
    </table>
    </fieldset>
    <fieldset class=\"detail infos\">
    <legend>$l_properties</legend>
    <table>
    $dis_properties
    </table>
    </fieldset>    
    <fieldset class=\"detail extra\">
    <legend>$l_modules_right</legend>
    $l_add_module_rights : <input name='' type='text' id='addModuleInput' value=''/>
    <a style='display:none;' id='addModuleButton' href=\"\" onclick=\"addModule($('moduleSheet').getElement('tbody'), $('addModuleInput').value);return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[$l_add_module_rights]\" /></a>
    <script type='text/javascript'>
      new Obm.Profile.Autocompleter('addModuleInput', modules, {
      }).addEvent('change', function () {
        if(this.isSelected) {
          $('addModuleButton').setStyle('display','inline');
        } else {
          $('addModuleButton').setStyle('display','none');
        }
      });
    </script>      
    <table class='spreadSheet' id='moduleSheet'>
    <tr><th>$l_default_settings</th><th>$l_user</th><th>$l_editor</th><th>$l_admin</th></tr>
    <tr $class id='module_default'>
    <td>$l_default <input type='hidden' name='cb_rights[default][]' value='0' /></td>
    <td>".dis_module_status('user',$profile['module']['default'], 'default', true)."</td>
    <td>".dis_module_status('editor',$profile['module']['default'], 'default', true)."</td>
    <td>".dis_module_status('admin',$profile['module']['default'], 'default', true)."</td></tr>
    <tr><th>$l_specific_settings</th><th>$l_user</th><th>$l_editor</th><th>$l_admin</th></tr>
    $dis_modules
    </table>
    </fieldset>    
    <fieldset class=\"detail extra\">
    <legend>$l_sections_right</legend>
    $l_add_section_enabled : <input type='text' id='addSectionInput' value=''/>
    <a style='display:none;' id='addSectionButton' onclick=\"addSection($('sectionSheet').getElement('tbody'), $('addSectionInput').value);return false;\"><img src=\"$GLOBALS[ico_add]\" alt=\"[$l_add_section_rights]\" /></a>
    <script type='text/javascript'>
      new Obm.Profile.Autocompleter('addSectionInput', sections, {
      }).addEvent('change', function () {
        if(this.isSelected) {
          $('addSectionButton').setStyle('display','');
        } else {
          $('addSectionButton').setStyle('display','none');
        }
      });
    </script>    
    <table class='spreadSheet' id='sectionSheet'>
    <tr><th>$l_default_settings</th><th>$l_section_enabled</th></tr>
    <tr $class id='section_default'>
    <td>$l_default <input type='hidden' name='cb_enabled[default]' value='0' /></td><td>".dis_section_status($profile['section']['default'], 'default', true)."</td></tr>
    <tr><th>$l_specific_settings</th><th>$l_section_enabled</th></tr>
    $dis_section
    </table>
    </fieldset> 
    <fieldset class='buttons'>
    $dis_buttons
    </fieldset>    
    ";
  return $block;

}



/**
 * dis_property_value 
 * 
 * @param mixed $property 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_value($property, $value, $update=false) {
  if(function_exists('dis_property_'.$property.'_value')) {
    return call_user_func('dis_property_'.$property.'_value', $value, $update);
  }
}

/**
 * dis_property_level_value 
 * 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_level_value($value, $update=false) {
  if($update) {
    if(is_numeric($value)) {
      ${'_'.$value} = 'selected=\"selected\"';
    }
    $return = "<select name='property[level]'><option value='0' $_0 >$GLOBALS[l_higher]</option>";
    for($i=1; $i<10; $i++) {
      $return .= '<option value="'.$i.'" '.${'_'.$i}.'>'.$i.'</option>';
    }
    $return .= "<option value='0' $_0 >$GLOBALS[l_lower]</option></select>";
    return $return;
  } else {
    return ($value)?$value:0;
  }
}

/**
 * dis_property_level_managepeers_value 
 * 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_level_managepeers_value($value, $update=false) {
  if($update) {
    $checked = ($value == 1)?'checked=\"checked\"':'';
    return '<input type="hidden" name="property[level_managepeers]" value="0"/><input type="checkbox" name="property[level_managepeers]" value="1" '.$checked.'/>';
  } else {
    return ($value == 1)?$GLOBALS['l_yes']:$GLOBALS['l_no'];
  }
}

/**
 * dis_property_access_restriction_value 
 * 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_access_restriction_value($value, $update=false) {
  if($update) {
    if($value == 'ALLOW_ALL' || $value == 'DENY_ALL') {
      $$value = 'selected="selected"';
    }
    return "<select name='property[access_restriction]'>
      <option value='ALLOW_ALL' $ALLOW_ALL>$GLOBALS[l_allow_connect]</option>
      <option value='DENY_ALL' $DENY_ALL>$GLOBALS[l_deny_connect]</option>
      </select>";    
  } else {
    return ($value == 'DENY_ALL')?$GLOBALS['l_deny_connect']:$GLOBALS['l_allow_connect'];
  }
}

/**
 * dis_property_access_exceptions_value 
 * 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_access_exceptions_value($value, $update=false) {
  if($update) {
    if(is_array($value)) $value = implode(',',$value);
    return "<div><input name='property[access_exceptions]' id='accessExceptionsInput' value='$value' />
      <script type='text/javascript'>
      new Autocompleter.Local('accessExceptionsInput', ['127.0.0.1', '10.0.0.0/8', '172.16.0.0/12', '192.168.0.0/16'], {
        multiple: true,
        selectMode :'type-ahead',
        minLength : 1,
        relative: false,
        overflow: true
      });      
      </script></div>
    ";
  } else {
    if(is_array($value) && count($value) > 1) {
      return implode(',', $value);
    }
    return ($value)?$value:$GLOBALS['l_none_f'];
  }
}

/**
 * dis_property_admin_realm_value 
 * 
 * @param mixed $value 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_property_admin_realm_value($value, $update=false) {
  global $cgp_use;
  if($update) {
    if(is_array($value)) {
      foreach($value as $realm) 
        $$realm = 'selected="selected"';
    } else {
      $domain = 'selected="selected"';
    }
    if($cgp_use['property']['delegation']) $delegation = "<option value='delegation' $delegation>$GLOBALS[l_realm_delegation]</option>";
    return "<select multiple='multiple' name='property[admin_realm][]'>
      <option value='user' $user>$GLOBALS[l_realm_user]</option>
      $delegation
      <option value='domain' $domain>$GLOBALS[l_realm_domain]</option>
      </select>";
  } else {
    if(is_array($value) && count($value) > 1) {
      foreach($value as $realm) {
        $realms[] = $GLOBALS["l_realm_$realm"];
      }
      return implode(', ', $realms);
    }
    return ($GLOBALS["l_realm_$value"])?$GLOBALS["l_realm_$value"]:$GLOBALS['l_all'];
  }
}

/**
 * dis_module_status 
 * 
 * @param mixed $type 
 * @param mixed $right 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_module_status($type, $right, $module, $update=false) {
  if($update) {
    $checked = ((hexdec($GLOBALS["perm_$type"]) & $right) == hexdec($GLOBALS["perm_$type"]))?'checked="checked"':'';
    return "<input type='checkbox' name='cb_rights[$module][]' value='".$GLOBALS["perm_$type"]."' $checked />";
  } else {
    return ((hexdec($GLOBALS["perm_$type"]) & $right) == hexdec($GLOBALS["perm_$type"]))? 'X' : '';
  }
}

/**
 * dis_section_status 
 * 
 * @param mixed $show 
 * @param mixed $section 
 * @param mixed $update 
 * @access public
 * @return void
 */
function dis_section_status($show, $section, $update=false) {
  global $l_enabled, $l_disabled;
  if($update) {
    $checked = ($show == 1)?'checked="checked"':'';
    return "<input type='checkbox' name='cb_enabled[$section]' value='1' $checked />";
  } else {
    return ($show == 1)? $l_enabled : $l_disabled;
  }
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
