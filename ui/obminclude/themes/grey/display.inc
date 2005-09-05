<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : display.inc (standard)                                       //
//     - Desc : Standard global display File                                 //
// 2003-08-07 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display: Function that display each page
// Parameters:
//   - $display : (by ref) $display hash with each content object
///////////////////////////////////////////////////////////////////////////////
function display_page(&$display) {

  // If header is null, display only the detail (Popup windows)
  if ($display["header"] == "") {
    echo $display["head"] . "
      <div class=\"detailpanelalone\">" .
      $display["title"] .
      $display["msg"] .
      $display["search"] .
      $display["result"] .
      $display["detail"] . "
      </div>" .
      $display["end"];
    return;
  }

  if (($display["link"] != "") || ($display["detailInfo"] != "" || $display["features"] != "")) {
    $right_panel = true;
  } else {
    $right_panel = false;
  }

  echo $display["head"] . $display["header"] . "

    <!-- left panel -->  
    <div class=\"leftpanel\">

    <!-- zone Action -->
    ". $display["action"] ."
    <!-- Fin zone Action -->
    <!-- zone Last -->
    ". $display["last"] . "
    <!-- Fin zone Last -->
    <!-- zone Todo -->
    ". $display["todo"] . "
    <!-- Fin zone Todo -->

    </div>

    <!-- left panel end --> 

    <div class=\"detailpanel\">";
 
  if ($right_panel) {
    echo "
    <!-- right panel -->
    <div class=\"rightpanel\">
     ". $display["detailInfo"] . "<p /> 
     ". $display["features"] ."
    ".  $display["link"] ."
    </div>
    <!-- right panel end --> 

    <!-- center zone --> 
    <div class=\"middlepanel\">";
  }
  
  echo 
    $display["msg"] .
    $display["title"] .
    $display["search"] .
    $display["result"] .
    $display["detail"] . "
    
    <!-- center zone end -->
    </div>
    ";
  
  if ($right_panel)
    echo "</div>";
  
  echo
  $display["end"];

}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Head
///////////////////////////////////////////////////////////////////////////////
function display_head($p_module) { 
  global $l_obm_title, $set_theme, $obminclude, $extra_css, $extra_js;

  if (isset($extra_css))
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"".C_IMAGE_PATH."/$set_theme/$extra_css\" />";

  $headers_e = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  return "$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />
  <title>$l_obm_title - $p_module</title>
  <link rel=\"stylesheet\" type=\"text/css\" href=\"".C_IMAGE_PATH."/$set_theme/style.css\" />
  $dis_extra_css
<script type=\"text/javascript\">
<!--
  $extra_js
// -->
</script>
</head>

<body onload=\"if (document.f_search) { document.f_search.submit.focus();} \">
";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the menu
// Parametres:
//   - $tab    : active tab 
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function generate_menu($module, $section) {
  global $path, $auth, $display, $obm_version, $cgp_show,$set_lang, $set_theme;
  global $l_login, $l_logout, $l_profile;

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];

  $display["section"] = display_sections($section);
  $display["menu"] = display_menus($section);
  $display["action"] = display_actions($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todo_toplist();


  $block = "
    <div class=\"header\">
     <div class=\"logo\">
     <a href=\"$path/obm.php\"><img src=\"".C_IMAGE_PATH."/$set_theme/logo_obm.png\" alt=\"Welcome\" id='logo' /></a>
     </div>

     <!-- zone Login -->
     <div> 
      OBM v. $obm_version Aliacom<br />
      $l_login&nbsp;: $login&nbsp;&nbsp;<br />

      $l_profile&nbsp;: $profil&nbsp; <br />
      <a href=\"$path/obm.php?action=logout\">$l_logout</a>
     </div>
    </div>
    <!-- Fin zone Login -->

    <!-- zone Onglets -->
    <div class=\"section\">
     ". $display["section"] ."
    </div>
    <!-- Fin zone Onglets -->

    <!-- zone Menu -->
    <div class=\"menu\">
    ". $display["menu"] ."
    </div>
    <!-- Fin zone Menu -->

    ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {
  global $sections, $perm;
  global $perm_admin;

  foreach ($sections as $key => $value) {
    $section_name = $value["Name"];
    $section_right = $perm->get_section_rights($key); 
    if (($section_right & $value["Right"]) == $value["Right"]) {
      if ($key == $section) {
        $ret .=  display_active_tab($value);
      }	else {
        $ret .=  display_inactive_tab($value);
      }
    }
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the active tab display
// Parameters:
//   - $tab : selected menu
///////////////////////////////////////////////////////////////////////////////
function display_active_tab($tab) {
  global $set_theme;

  $block = "
    <font color=\"#FFCF00\">".$tab["Name"]."</font>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <img src=\"".C_IMAGE_PATH."/$set_theme/barre_verti.png\" width=\"2\" height=\"14\" alt=\"\" />
    &nbsp;&nbsp;&nbsp;&nbsp;";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the active tab display
// Parameters:
//   - $tab : selected menu
///////////////////////////////////////////////////////////////////////////////
function display_inactive_tab($tab) {
  global $set_theme;

  $block = "
    <a class=\"sections\" href=\"".$tab["Url"]."\">".$tab["Name"]."</a>
    &nbsp;&nbsp;&nbsp;&nbsp;
    <img src=\"".C_IMAGE_PATH."/$set_theme/barre_verti.png\" width=\"2\" height=\"14\" alt=\"\" />
    &nbsp;&nbsp;&nbsp;&nbsp;";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section menus display block
// Parameters:
//   - $section : selected section
///////////////////////////////////////////////////////////////////////////////
function display_menus($section) {
  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $menus, $perm, $auth;

  if (($set_menu == $cme_ico) || ($set_menu == $cme_both))
    $fico = true;
  else $fico = false;

  if (($set_menu == $cme_txt) || ($set_menu == $cme_both))
    $ftext = true;
  else $ftext = false;

  if (is_array($menus[$section])) {
    foreach ($menus[$section] as $module => $value) {
      $module_right = $perm->get_module_rights($module); 
      if (($module_right & $value["Right"]) == $value["Right"]) {
	if ($fico) {
	  $ico = $value["Ico"];
	  if ($ico)
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico\" alt=\"\" /></a>\n";
	  else
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"".C_IMAGE_PATH."/$set_theme/puce.gif\" alt=\"\" /></a>\n";
	}

	if ($ftext) {
	  $dis_text = "<a class=\"menus\" href=\"".$value["Url"]."\">".$value["Name"]."</a>";
	}

	$block .= "$dis_ico $dis_text";
      }
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the action display block of the selected module         //
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions($section, $module) {
  global $set_theme, $menus, $action, $popup_height, $perm;
  global $actions, $popup_width;

  $name = $menus[$section][$module]["Name"];
  $ico = $menus[$section][$module]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico\" alt=\"\" />&nbsp;";
  else
    $dico = "";

  $ret = "
    <div class=\"action\"> 
     <div class=\"actionTitle\">&nbsp;:: Action</div>
    ";

   if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 

    foreach ($actions[$module] as $key => $value){
      if (in_array("all",$value["Condition"])
           || in_array($action,$value["Condition"])) {
	if (($module_right & $value["Right"]) == $value["Right"]) {
	  if ($value["Popup"] == 1) {
	    if ($value["Target"]) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $ret .= "
               <div class=\"actionLine\">&nbsp;
               <a href=\"\" 
                 onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">".
	      $value["Name"]."</a></div>";
	  } else {
	    $ret .= "
               <div class=\"actionLine\">&nbsp;
                 <a href=\"".$value["Url"]."\">".
	      $value["Name"]."</a></div>";
           }
         }
       }
     }
   }
   
   $ret .= "
     </div>
     ";

   return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_last_visited($path="..") {
  global $path, $cgp_show, $last_v, $set_theme, $l_last_visit;

  $block = "
  <div class=\"last\"> 
  <div class=\"lastTitle\">:: $l_last_visit</div>
  <ul class=\"lastList\">";

  foreach ($cgp_show["module"] as $module => $val) {

    // Specific case : deal module has two entities (2 last visited entries)
    if ($module == "deal") {
      if (($cgp_show["module"]["deal"]) && ($last_v["parentdeal"]["text"] != "")) {
	$id = $last_v["parentdeal"]["id"];
	$text = substr($last_v["parentdeal"]["text"], 0, 30);
	$l_ico = "ico_parentdeal";
	global ${$l_ico};
	$parentdeal_url = url_prepare("$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=$id");
	$block .= "
          <li class=\"lastList\"><a href=\"$parentdeal_url\"><img src=\"".C_IMAGE_PATH."/$set_theme/${$l_ico}\" alt=\"\" /></a>
            <a href=\"$parentdeal_url\">$text</a></li>";
      }
    }

    if (($cgp_show["module"][$module]) && ($last_v[$module]["text"] != "")) {
      $id = $last_v[$module]["id"];
      $text = substr($last_v[$module]["text"], 0, 30);
      $l_ico = "ico_$module";
      global ${$l_ico};
      $mod_url = url_prepare("$path/$module/${module}_index.php?action=detailconsult&amp;param_${module}=$id");
      $block .= "
        <li class=\"lastList\"><a href=\"$mod_url\"><img src=\"".C_IMAGE_PATH."/$set_theme/${$l_ico}\" alt=\"\" /></a>
          <a href=\"$mod_url\">$text</a></li>";
    }
  }

  $block .= "
    </ul>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the Todo display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_todo_toplist($path="..") {
  global $cgp_show, $todos, $l_module_todo;

  if (! $cgp_show["module"]["todo"]) {
    return "";
  }

  $nb = count($todos);

  for ($i = 1; $i <= $nb; $i++) {
    $t_id = $todos[$i]["id"];
    $t_title = $todos[$i]["title"];
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=$t_id");
    $todo_title = substr($t_title, 0, 30);
    $block_todo .= "
      <li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($nb >= 1) {
    $block = "
    <div class=\"last\"> 
    <div class=\"lastTitle\">:: $l_module_todo</div>
      <ul class=\"lastList\">
      $block_todo
     </ul>
    </div>";
  } else {
    $block = "";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: end of an HTML page
///////////////////////////////////////////////////////////////////////////////
function display_end() { 
  return "
    </body>
    </html>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display: Page title
///////////////////////////////////////////////////////////////////////////////
function display_title($title) { 

  return "<div class=\"title\">$title</div>";
}


///////////////////////////////////////////////////////////////////////////////
// Display an error message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageError\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display a warning message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg, $special=true) {
  
  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageWarning\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display an OK message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageOk\">$msg</p>";
  }
  
  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display an Info message
// Parameters:
//   - $msg     : message to display
//   - $special : if true (default), html special chars just displayed
///////////////////////////////////////////////////////////////////////////////
function display_info_msg($msg, $special=true) {

  if ($msg != "") {
    if ($special) {
      $msg = htmlspecialchars($msg);
    }
    $ret = "<p class=\"messageInfo\">$msg</p>";
  }
  
  return $ret;
}
