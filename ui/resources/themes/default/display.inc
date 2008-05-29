<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : display.inc                                                  //
//     - Desc : Default global display File                                  //
// 2006-11-08 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display : Function that display each page
// Parameters:
//   - $display : (by ref) $display hash with each content object
///////////////////////////////////////////////////////////////////////////////
function display_page(&$display) {
  global $output_target, $ico_nav_close;
  global $ico_nav_open, $l_hide, $l_show;
  global $l_print, $l_cancel, $params;

  // If print activated, display only the detail
  if ($params["output_target"] == "print") {
    echo $display["head"] . "
" .
      $display["title"] .
      $display["msg"] .
      "
      <div id=\"ajaxMessage\"></div>
      &nbsp;".
      $display["detail"] . "
      </div>
      " .
      $display["end"];
    return;
  }

  // If header is null, display only the detail (Popup windows)
  if ($display["header"] == "") {
    echo $display["head"] . 
      $display["title"] .
      $display["msg"] . 
      "<div id=\"mainContent\">
      <div id=\"ajaxMessage\"></div>
      &nbsp;".
      $display["search"] .
      $display["detail"]." 
       <p class=\"LC\" />
       </div>
      ".
      $display["result"] . 
      $display["end"];

    return;
  }

  echo $display["head"] . $display["header"] . "
    <!-- left panel -->
    <div id=\"portletsPanel\"> 
      <div id='portletsHandler'>
       <span>
        <img src='$ico_nav_close' alt='Hide this bar' />
       </span>
       <span id='portletsHandlerShow'>
        <img src='$ico_nav_open' alt='Show this bar' />
       </span>
      </div>         
     <div id=\"portlets\">          
      $display[last]
      $display[todo]
      $display[features] 
      $display[link] 
      $display[detailInfo] 
     </div>
    </div>
    <!-- left panel end -->
    <div id=\"mainPanel\" >  
    $display[title]
    $display[action]
    $display[msg]" ;
    if($display["search"] != "" || $display["detail"] != "") {
      echo  
        "<div id=\"mainContent\">
         <div id=\"ajaxMessage\"></div>
        &nbsp;".
      $display["search"] .
      $display["detail"] . "
      <p class=\"LC\" />
      </div>";
    }
    echo $display["result"] ."
    </div>
    <script type=\"text/javascript\">
      obm.portlets = new Obm.Portlets();
    </script>  
    <!-- center zone end -->  
    ".$display["end"];

}


///////////////////////////////////////////////////////////////////////////////
// Display : Function that display the page menu
// Parameters:
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function display_menu($module) {
  global $path, $obm, $display, $obm_version, $cgp_show, $modules;
  global $ico_logout, $ico_login, $ico_obm, $ico_print, $ico_scope;
  global $l_logout, $l_profile, $l_scope_update;
  global $perm, $cright_write_admin;

  $obm['uid'];
  $login = $obm['login'];
  $profil = $obm['profile'];
  $p = isset($GLOBALS["l_perm_$profil"]) ? $GLOBALS["l_perm_$profil"] : $profil;
  $domain = $obm['domain_label'];
  $domain_id = $obm['domain_id'];
  $section = $cgp_show['module'][$module];
  if (empty($section)) {
    $section = $module;
  }
  $display['section'] = display_sections($section);
//  $display["module"] = display_modules($section, $module);
  $display['last'] = display_last_visited();
  $display['todo'] = display_todos();
  $display['action'] = display_actions($section, $module);
  $update_state = get_update_state($domain_id);
  if (($update_state) && $cgp_show['module']['tools'] && ($perm->check_right('tools', $cright_write_admin))) {
    $dis_scope = "<li><a href=\"$path/tools/tools_index.php?action=update_index\" ><img src=\"$ico_scope\" title=\"$l_scope_update\" alt=\"Commit update\" /></a></li>";
  }
  if ($display['print_url'] != "") {
    $url = $display['print_url']."&amp;output_target=print";
    $dis_printer = "<li><a href=\"#\" onclick=\"popup('$url');\" ><img src=\"$ico_print\" alt=\"Print\" id='print' /></a></li>";
  }
  $block = "
<!-- Bandeau -->
<div id='banner'>
<a href=\"$path/obm.php\" id=\"obmLogo\"><img src='$ico_obm' alt='Logo OBM'/></a>
".
$display['section']
."
<ul id='information'>
$dis_scope
<li> $login ($domain)</li>
<li>$l_profile : $p</li>
<li><a href='".$path."/obm.php?action=logout'><img src='$ico_logout' alt='$l_logout' /> $l_logout</a></li>
$dis_printer
</ul>
</div>
".$display['module']
;

//&gt;".$modules[$module]["Name"].
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the section block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections_theme($section) {
  global $cgp_show, $sections, $perm;

  $ret = "<ul id='section'>\n";
  foreach ($cgp_show['section'] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value['Name'];
      $section_target = $value['Target'];
      $section_right = $perm->get_section_rights($key);
      if ($section_right) {
        $section_modules = array_keys($cgp_show["module"],$key);
        if (count($section_modules) > 0) {
	  $module_block = display_modules_theme($key);
	  $mods .= $module_block;
	  $js .= "obm.menu.addItem('section-$key');";
	} else {
	  if (isset($section_target)) {
	    $target = "target='$section_target'";
	  } else {
            $target = "";
          }
          $section_name = "<a $target href=\"$show[url]\">$section_name</a>";
        }
        if ($key == $section) {
          $ret .= "<li class=\"current\" id=\"section-$key\">$section_name</li>\n";
        } else {
          $ret .= "<li class=\"sectionItem\" id=\"section-$key\">$section_name</li>\n";
        }
      }
    }
  }
  $ret .= "</ul>";
  $ret .= "
    $mods
    <script type=\"text/javascript\">
      obm.menu = new Obm.Menu();
      $js
    </script>
";

  return $ret;
}

///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the module block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_modules_theme($section) {
  global $cme_txt, $cme_ico, $cme_both;
  global $path, $perm, $modules, $cgp_show;

  if (($_SESSION['set_menu'] == $cme_ico) || ($_SESSION['set_menu'] == $cme_both))
    $fico = true;
  else $fico = false;

  if (($_SESSION['set_menu'] == $cme_txt) || ($_SESSION['set_menu'] == $cme_both))
    $ftext = true;
  else $ftext = false;

  $section_link = array_keys($cgp_show["module"],$section);
  if (is_array($section_link)) {
    $id = $section."Items";
    $block .= "
      <div class='module' id='section-$section-items-wrapper'>
      <ul id='section-$section-items'>
";
    foreach ($section_link as $mod) {
      $module_name = $modules[$mod]["Name"];
      $module_tooltip = $modules[$mod]["Tooltip"];
      $module_right = $perm->get_module_rights($mod); 
      if (($module_right & $modules[$mod]["Right"]) == $modules[$mod]["Right"]) {
	// XXX specifier une target au lien du module (non configurable
	// actuellement car cgp_show[module][webmail] = valeur et non tableau
	// Si tableau pb avec section_link = array_keys(..) 10 l plus haut
	$module_target = $modules[$mod]['Target'];
	if (isset($module_target)) {
	  $target = " target='$module_target'";
	} else {
          $target = "";
        }
        $block .= "<li>";
        if ($fico) {
          $ico = $modules[$mod]['Ico'];
          if ($ico) {
            $block .= "<a$target href='".$modules[$mod]["Url"]."' class='ico' title='$module_tooltip'><img src='$ico' alt='$module_name' /></a> ";
          }
        }
        $block .= "<a$target href='".$modules[$mod]["Url"]."' title='$module_tooltip'>";
        if ($ftext) $block .= $module_name;
        $block .= "</a></li>\n";
      }
    }
    $block .= "</ul></div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the action block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions_theme($section, $module) {
  global $modules, $action, $display;
  global $actions, $perm, $display;

  $mod_title = $GLOBALS["l_module_$module"];
  if(!empty($actions[$module][$action]["Title"])) {
    $act_title = '- '.$actions[$module][$action]["Title"].' ';
  } elseif(!empty($actions[$module][$action]["Name"])) {
    $act_title = '- '.$actions[$module][$action]["Name"].' ';
  }


  if (empty($display['title'])) {
    $display['title'] = "<h1 class=\"title\">$mod_title $act_title</h1>";
  } else if ($display['title'] == strip_tags($display['title'])) {
    $display['title'] = "<h1 class=\"title\">$mod_title $act_title: $display[title]</h1>";
  } 

  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    $block = "<ul id=\"action\">"; 
    foreach ($actions[$module] as $key => $value) {

      // If the current action has this action in target
      if (!in_array("!$action",$value["Condition"]) 
        && (in_array("all", $value["Condition"])
	|| in_array($action, $value["Condition"]))) {
	if (($module_right & $value["Right"]) == $value["Right"]) {

          $tooltip = $value["Tooltip"];

	  if ($value["Popup"] == 1) {
	    if ($value["Target"]) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $content = "
              <a href=\"\" 
              title=\"".$value["tooltip"]."\" 
              onclick=\"$wtarget popup('".$value['Url']."',''); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
	  } else {
	    $content = "
               <a href=\"".$value["Url"]."\" title=\"".$tooltip."\" >".$value["Name"]."</a> &nbsp;";
          }
          $block .= "<li onmouseover=\"this.className='hover'\" onmouseout=\"this.className=''\" >$content</li>";
	}
      }
    }
    $block .= "</ul>"; 
    
  }

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Head
// Parameters:
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_head_theme($p_module) { 
  global $l_obm_title, $extra_css, $extra_js, $extra_js_include,$ico_minus;
  global $ico_datepicker,$l_monthsofyear,$l_daysofweekfirst,$ico_colorpicker;
  global $l_monthsofyearshort,$l_daysofweek,$l_daysofweekshort,$ico_plus, $c_week_first_day;
  global $l_today, $l_close, $css_obm, $popup_height,$popup_width,$resources_path;
  global $css_print_obm, $ico_delete, $ico_update, $output_target,$l_transparent;
  global $l_autocomplete_field, $l_fatal_server_err;

  $css = $css_obm;

  if ($output_target == "print") {
   $css = $css_print_obm;
  }

  $monthShort =  phpArrayToJsArray($l_monthsofyearshort);
  $monthLong = phpArrayToJsArray($l_monthsofyear);
  $daysShort = phpArrayToJsArray($l_daysofweekfirst);
  $dayMedium = phpArrayToJsArray($l_daysofweekshort);
  $dayLong =   phpArrayToJsArray($l_daysofweek);

  $headers_e = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  if (is_array($extra_css)) {
    foreach($extra_css as $css_file) {
      $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"$css_file\" />";
    }
  } elseif (isset($extra_css)) {
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"$extra_css\" />";
  }

  if (is_array($extra_js_include)) {
    foreach($extra_js_include as $js_file) {
      $js_include .= "<script type=\"text/javascript\" src=\"$resources_path/js/$js_file\" ></script>\n";
    }
  }

  return "$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />
  <title>$l_obm_title - $p_module</title>
  <link rel=\"stylesheet\" type=\"text/css\" href=\"$css\" />
  $dis_extra_css
  <script type=\"text/javascript\" src=\"$resources_path/js/mootools.js\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/datepicker.js\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/utils.js\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/obm.js\" ></script>
  <script type=\"text/javascript\" src=\"$resources_path/js/autocomplete.js\" ></script>

  $js_include
  <script type=\"text/javascript\">
  //<![CDATA[
  obm.vars.images.minus = '$ico_minus';
  obm.vars.images.plus = '$ico_plus';
  obm.vars.images.datePicker = '$ico_datepicker';
  obm.vars.images.colorPicker = '$ico_colorpicker';
  obm.vars.images.update = '$ico_update';
  obm.vars.images.del = '$ico_delete';
  obm.vars.regexp.dateFormat = '$_SESSION[set_date_upd]';
  obm.vars.consts.popupHeight = '$popup_height';
  obm.vars.consts.popupWidth = '$popup_width';
  obm.vars.consts.weekStart = $c_week_first_day;
  obm.vars.labels.monthsShort = $monthShort;
  obm.vars.labels.months = $monthLong;
  obm.vars.labels.dayShort = $daysShort;
  obm.vars.labels.dayMedium = $dayMedium;
  obm.vars.labels.dayLong = $dayLong;
  obm.vars.labels.today = '".phpStringToJsString($l_today)."';
  obm.vars.labels.transparent = '".phpStringToJsString($l_transparent)."';
  obm.vars.labels.autocompleteField = '".phpStringToJsString($l_autocomplete_field)."'
  obm.vars.labels.fatalServerErr = '".phpStringToJsString($l_fatal_server_err)."';
  
  window.onresize= function() {
    while(obm.resize.chains && obm.resize.chains.length > 0) {
      obm.resize.callChain();
    }
  }

  obm.initialize.chain(datePickerGenerator);
  window.onDomReady( function () {
    while(obm.initialize.chains && obm.initialize.chains.length > 0) {
      obm.initialize.callChain();
    }
  });

  $extra_js
  // ]]>
  </script>
</head>

<body onload=\"if (document.f_search) {
    document.f_search.elements[0].focus();
  } else if (document.f_entity) {
    document.f_entity.elements[0].focus();
  } \">

"
.display_debug()
;
}
