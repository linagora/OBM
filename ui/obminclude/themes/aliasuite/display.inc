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

  if (($display["link"] != "") || ($display["detailInfo"] != "")) {
    $right_panel = true;
  } else {
    $right_panel = false;
  }

  echo $display["head"] . $display["header"] . "

    <!-- left panel -->
    <div class=\"leftpanel\">" .
      $display["last"] . "<p />" .
      $display["todo"] . 
      $display["features"] ."
    </div>
    <!-- left panel end -->

  ";
 
  if ($right_panel) {
    echo "
    <!-- right panel -->
    <div class=\"rightpanel\">" .
      $display["detailInfo"] . "<p />" .
      $display["link"] . "<p />
    </div>
    <!-- right panel end -->

    <!-- center zone -->
    <div class=\"middlepanel\">
    ";
  }
  else {
    echo "<div class=\"detailpanel\">";
  }
  echo
    $display["msg"] .
    $display["title"] .
    $display["search"] .
    $display["result"] .
    $display["detail"] . "
    
    </div>
    <!-- center zone end -->
  ";

  if ($right_panel) {
    echo "</div>";
  }

  echo
    $display["end"];

}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Head
///////////////////////////////////////////////////////////////////////////////
function display_head($p_module) { 
  global $l_obm_title, $set_theme, $obminclude, $extra_css, $extra_js;

  if (isset($extra_css))
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/$extra_css\" />";

  $headers_e = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  return "$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />

  <link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/style.css\" /> 
  <title>$l_obm_title - $p_module</title>
  $dis_extra_css
  $extra_js
  
</head>

<body onload=\"if (document.f_search) { document.f_search.submit.focus();} \">
";
}


///////////////////////////////////////////////////////////////////////////////
// Function which perform the menu
// Parametres:
//   - $tab    : active tab 
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function generate_menu($module, $section) {
  global $set_lang, $set_lang_default, $set_theme, $set_theme_default;
  global $popup_width, $popup_height, $l_home;
  global $auth, $obminclude, $display; 
  global $path, $obm_version;
  global $aliamin_link, $aliacms_link, $blogs_link;
  
  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];

  $display["section"] = display_sections($section);
  $display["menu"] = display_menus($section, $module);
  $display["action"] = display_menu_bar($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todo_toplist();

  if($aliamin_link) {
    $dis_aliamin = "<li>&nbsp;<a href=\"$aliamin_link\">Aliamin</a>&nbsp;</li>";
  }
  if($aliacms_link) {
    $dis_aliacms = "<li>&nbsp;<a href=\"$aliacms_link\">Aliacms</a>&nbsp;</li>";
  }
  if($blogs_link) {
    $dis_blogs = "<li>&nbsp;<a href=\"$blogs_link\">Blogs</a>&nbsp;</li>";
  }
  
  $block = "
<!-- Bandeau -->
<a href=\"$path/obm.php\">
<img src=\"/images/$set_theme/logo_obm.png\" alt=\"Welcome\" id='logo' />
</a>
<ul class=\"header\">
  <li class=\"first\">&nbsp;
  <a href=\"$path/obm.php\">$l_home</a>&nbsp;</li>
  $dis_aliacms  
  $dis_aliamin
  $dis_blogs
  <li class=\"login\">&nbsp;Login : $login&nbsp;</li>
  <li class=\"profil\">&nbsp;Profil : $profil&nbsp;</li>
  <li class=\"logout\">&nbsp;<a href='".$path."/obm.php?action=logout'>Logout</a>&nbsp;</li>
</ul>
" . $display["section"] ."
<p style=\"clear:both\" />
" . $display["menu"] ."
<!-- Fin Bandeau -->

" . $display["action"];

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {
  global $sections, $perm;
  
  $ret = "<ul class='section'>\n";
  foreach ($sections as $key => $value) {
    $section_name = $value["Name"];
    $section_right = $perm->get_section_rights($key); 
    if (($section_right & $value["Right"]) == $value["Right"]) {
      if ($key == $section) {
	$ret .= "<li>$section_name</li>\n";
      }	else {
	$ret .= "<li><a href='".$value["Url"]."'>$section_name</a></li>\n";
      }
    }
  }
  $ret .= "</ul>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section menus display block
// Parameters:
//   - $section : selected section
///////////////////////////////////////////////////////////////////////////////
function display_menus($section, $module) {
  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $menus, $perm;

  if (($set_menu == $cme_ico) || ($set_menu == $cme_both))
    $fico = true;
  else $fico = false;

  if (($set_menu == $cme_txt) || ($set_menu == $cme_both))
    $ftext = true;
  else $ftext = false;

  if (is_array($menus[$section])) {
    $block .= "<ul class='menu'>\n";

    foreach ($menus[$section] as $menu => $value) {
      $module_name = $value["Name"];
      $module_right = $perm->get_module_rights($menu); 
      if (($module_right & $value["Right"]) == $value["Right"]) {
	if($menu == $module) {
          $block .= "<li class=\"activeTab\">";
	} else {
	  $block .= "<li class=\"inactiveTab\">";
	}
	if ($fico) {
	  $ico = $value["Ico"];
	  if ($ico) {
	    $block .= "<a href='".$value["Url"]."' class='ico'><img src='/images/".$set_theme."/".$ico."' alt='$module_name' /></a> ";
	  }
	}
	$block .= "<a href='".$value["Url"]."'>";
	if ($ftext) $block .= $module_name;
        $block .= "</a></li>\n";
      }
    }
    $block .= "</ul>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the action display block of the selected module
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_menu_bar($section, $module) {
  global $set_theme, $menus, $action, $popup_height;
  global $actions, $popup_width, $perm;

  $name = $menus[$section][$module]["Name"];
  $ico = $menus[$section][$module]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"/images/$set_theme/$ico\" alt=\"\" />&nbsp;";
  else
    $dico = "";

  $ret = "
  <ul class=\"menuBar\">
  ";
  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    foreach ($actions[$module] as $key => $value) {
      // If the current action has this action in target
      if (in_array("all", $value["Condition"])
	  || in_array($action, $value["Condition"])) {
	if (($module_right & $value["Right"]) == $value["Right"]) {
	  $ret .= "<li>";
	  if ($value["Popup"] == 1) {
	    if ($value["Target"]) {
	      $wtarget = "window.name='" . $value['Target'] . "';";
	    }
	    $ret .= "
              <a href=\"\" class=\"menuBar\" 
              onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
	  } else {
	    $ret .= "
               <a class=\"menuBar\" href=\"".$value["Url"]."\">".$value["Name"]."</a> &nbsp;";
	  }
	  $ret .= "</li>";
	}
      }
    }
  }
   
  $ret .= "
  </ul>
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
          <li class=\"lastList\"><a href=\"$parentdeal_url\"><img src=\"/images/$set_theme/${$l_ico}\" alt=\"\" /></a>
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
        <li class=\"lastList\"><a href=\"$mod_url\"><img src=\"/images/$set_theme/${$l_ico}\" alt=\"\" /></a>
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
  global $todos, $l_header_todo;

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
    <div class=\"lastTitle\">:: $l_header_todo</div>
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

/////////////////////////////////////////////////////////////////////////////
// Display an error message
/////////////////////////////////////////////////////////////////////////////
function display_err_msg($msg) {
  return "<p class=\"messageError\">".htmlspecialchars($msg)."</p>";
}


/////////////////////////////////////////////////////////////////////////////
// Display a warning message
/////////////////////////////////////////////////////////////////////////////
function display_warn_msg($msg) {
  return "<p class=\"messageWarning\">".htmlspecialchars($msg)."</p>";
}


////////////////////////////////////////////////////////////////////////////
// Display an OK message
////////////////////////////////////////////////////////////////////////////
function display_ok_msg($msg) {
  return "<p class=\"messageOk\">$msg</p>";
}

////////////////////////////////////////////////////////////////////////////
// Display an Info message
////////////////////////////////////////////////////////////////////////////
function display_info_msg($msg) {
  return "<p class=\"messageInfo\">".htmlspecialchars($msg)."</p>";
}
