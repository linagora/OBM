<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : display.inc (standard)                                       //
//     - Desc : Default global display File                                  //
// 2005-11-08 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display : Function that display each page
// Parameters:
//   - $display : (by ref) $display hash with each content object
///////////////////////////////////////////////////////////////////////////////
function display_page(&$display) {
  global $output_target;
  global $ico_arrow_left;

  // If print activated, display only the detail
  if ($output_target == "print") {
    echo $display["head"] . "
      <!-- left panel -->
      <div  id=\"portlets\">
" .
      $display["features"] ."
      </div>
      <div id=\"mainPanel\"
      
" .
      $display["title"] .
      $display["msg"] .
      "<div id=\"mainContent\">".
      $display["detail"] . "
      </div>
      </div>
      <script type=\"text/javascript\">
        obm.initialize.chain(function() {
          obm.portlets = new Obm.Portlets();
        });
      </script>
      " .
      $display["end"];
    return;
  }

  // If header is null, display only the detail (Popup windows)
  if ($display["header"] == "") {
    echo $display["head"] . 
      $display["title"] .
      $display["msg"] . 
      "<div id=\"mainContent\">".
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
      <img id='portletsHandler' src='$ico_arrow_left' alt='Hide this bar' />
      <div id=\"portlets\">
        " .
        $display["last"] .
        $display["features"] .
        $display["link"] .
        $display["detailInfo"] ." 
      </div>
    </div>
    <!-- left panel end -->
    <div id=\"mainPanel\" >".
    $display["title"].
    $display["action"] .
    $display["msg"] .
    "<div id=\"mainContent\">".
    $display["search"] .
    $display["detail"] . "
    <p class=\"LC\" />
    </div>
     ".$display["result"] ."
    </div>
    <script type=\"text/javascript\">
      obm.initialize.chain(function() {
        obm.portlets = new Obm.Portlets();
      });
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
  global $path, $auth, $display, $obm_version, $cgp_show,$set_lang, $set_theme;
  global $l_logout, $l_profile,$ico_logout,$modules,$ico_login, $ico_obm;

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];
  $domain = $auth->auth["domain_label"];
  $section = $cgp_show["module"][$module];
  $display["section"] = display_sections($section);
//  $display["module"] = display_modules($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todos();
  $display["action"] = display_actions($section, $module);
  if ($display["print_url"] != "") {
    $dis_printer = "<li><a target=\"top\" href=\"".$display["print_url"]."&amp;output_target=print\"><img src=\"".C_IMAGE_PATH."/$set_theme/ico_printer.gif\" alt=\"Print\" id='print' /></a></li>";
  }

  $block = "
<!-- Bandeau -->
<div id='banner'>
<a href=\"$path/obm.php\"><img src='$ico_obm' alt='Logo OBM'/></a>
".
$display["section"]
."
<ul id='information'>
<li> $login ($domain)</li>
<li>$l_profile : $profil</li>
<li><a href='".$path."/obm.php?action=logout'><img src='$ico_logout' alt='$l_logout' /> $l_logout</a></li>
$dis_printer
</ul>
</div>
&gt;".$modules[$module]["Name"].
$display["module"]
;

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
  foreach ($cgp_show["section"] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value["Name"];
      $section_right = $perm->get_section_rights($key);
      if ($section_right) {
        $module_block = display_modules_theme($key);        
        $mods .= $module_block;
        $js .= "obm.menu.addItem('section-$key');\n";
        $ret .= "<li id=\"section-$key\">$section_name</li>\n";
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
  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $perm, $modules, $cgp_show;

  if (($set_menu == $cme_ico) || ($set_menu == $cme_both))
    $fico = true;
  else $fico = false;

  if (($set_menu == $cme_txt) || ($set_menu == $cme_both))
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
      $module_right = $perm->get_module_rights($mod); 
      if (($module_right & $modules[$mod]["Right"]) == $modules[$mod]["Right"]) {
        $block .= "<li>";
        if ($fico) {
          $ico = $modules[$mod]["Ico"];
          if ($ico) {
            $block .= "<a href='".$modules[$mod]["Url"]."' class='ico'><img src='".C_IMAGE_PATH."/".$set_theme."/".$ico."' alt='$module_name' /></a> ";
          }
        }
        $block .= "<a href='".$modules[$mod]["Url"]."'>";
        if ($ftext) $block .= $module_name;
        $block .= "</a></li>\n";
      }
    }
    $block .= "</ul></div>";
  }

  return $block;
}


///////////////////////////////////////div////////////////////////////////////////
// Theme specific : Display the action block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions_theme($section, $module) {
  global $set_theme, $modules, $action, $popup_height;
  global $actions, $popup_width, $perm;


  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    $block = "<ul id=\"action\">"; 
    foreach ($actions[$module] as $key => $value) {

      // If the current action has this action in target
      if (!in_array("!$action",$value["Condition"]) 
        && (in_array("all", $value["Condition"])
	|| in_array($action, $value["Condition"]))) {
	if (($module_right & $value["Right"]) == $value["Right"]) {

	  if ($value["Popup"] == 1) {
	    if ($value["Target"]) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $content = "
              <a href=\"\" 
              onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
	  } else {
	    $content = "
               <a href=\"".$value["Url"]."\">".$value["Name"]."</a> &nbsp;";
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
  global $l_obm_title, $extra_css, $extra_js_include;
  global $ico_arrow_up,$ico_arrow_down,$ico_arrow_left,$ico_arrow_right;
  global $ico_datepicker,$set_date_upd,$l_monthsofyear,$l_daysofweekfirst;
  global $l_monthsofyearshort,$l_daysofweek,$l_daysofweekshort;
  global $l_today, $l_close, $css_obm, $popup_height,$popup_width;

  // Else
  if (isset($extra_css)) {
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"$extra_css\" />";
  }
  $monthShort =  phpArrayToJsArray($l_monthsofyearshort);
  $monthLong = phpArrayToJsArray($l_monthsofyear);
  $daysShort = phpArrayToJsArray($l_daysofweekfirst);
  $dayMedium = phpArrayToJsArray($l_daysofweekshort);
  $dayLong =   phpArrayToJsArray($l_daysofweek);

  $headers_e = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  if (is_array($extra_js_include)) {
    foreach($extra_js_include as $js_file) {
      $js_include = "<script type=\"text/javascript\" src=\"$js_file\" ></script>\n";
    }
  }

  return "$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />
  <title>$l_obm_title - $p_module</title>
  <link rel=\"stylesheet\" type=\"text/css\" href=\"$css_obm\" />
  $dis_extra_css
  <script type=\"text/javascript\" src=\"".C_IMAGE_PATH."/../js/mootools.js\" ></script>
  <script type=\"text/javascript\" src=\"".C_IMAGE_PATH."/../js/datepicker.js\" ></script>
  <script type=\"text/javascript\" src=\"".C_IMAGE_PATH."/../js/obm.js\" ></script>
  <script type=\"text/javascript\" src=\"".C_IMAGE_PATH."/../js/calendar.js\" ></script>
  $js_include
  <script type=\"text/javascript\">
  obm.vars.images.upArrow = '$ico_arrow_up';
  obm.vars.images.downArrow = '$ico_arrow_down';
  obm.vars.images.leftArrow = '$ico_arrow_left';
  obm.vars.images.rightArrow = '$ico_arrow_right';
  obm.vars.images.datePicker = '$ico_datepicker';
  obm.vars.regexp.dateFormat = '$set_date_upd';
  obm.vars.consts.popupHeight = '$popup_height';
  obm.vars.consts.popupWidth = '$popup_width';
  obm.vars.labels.monthsShort = $monthShort;
  obm.vars.labels.months = $monthLong;
  obm.vars.labels.dayShort = $daysShort;
  obm.vars.labels.dayMedium = $dayMedium;
  obm.vars.labels.dayLong = $dayLong;
  obm.vars.labels.today = '$l_today';


  obm.initialize.chain(datePickerGenerator);
  Window.onDomReady( function () {
    while(obm.initialize.chains.length > 0) {
      obm.initialize.callChain();
    }
  });

//<!--
$extra_js
//-->
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
