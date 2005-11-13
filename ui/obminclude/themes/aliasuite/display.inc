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
      <div class=\"detailpanelalone\">
" .
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
    <div class=\"leftpanel\">
" .
      $display["last"] . "<p />" .
      $display["todo"] . 
      $display["features"] ."
    </div>
    <!-- left panel end -->

  ";
 
  if ($right_panel) {
    echo "
    <!-- right panel -->
    <div class=\"rightpanel\">
" .
      $display["detailInfo"] . "<p />" .
      $display["link"] . "<p />
    </div>
    <!-- right panel end -->

    <!-- center zone -->
    <div class=\"middlepanel\">
    ";
  }
  else {
    echo "<div class=\"detailpanel\">
";
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
// Function which perform the menu
// Parametres:
//   - $tab    : active tab 
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function generate_menu($module) {
  global $path, $auth, $display, $obm_version, $cgp_show,$set_lang, $set_theme;
  global $l_login, $l_logout, $l_profile, $l_home;
  global $aliamin_link, $aliacms_link, $blogs_link;
  
  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];
  $section = $cgp_show["module"][$module];
  $display["section"] = display_sections($section);
  $display["menu"] = display_menus($section, $module);
  $display["action"] = display_actions($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todo_toplist();

  if ($aliamin_link) {
    $dis_aliamin = "<li>&nbsp;<a href=\"$aliamin_link\">Aliamin</a>&nbsp;</li>";
  }
  if ($aliacms_link) {
    $dis_aliacms = "<li>&nbsp;<a href=\"$aliacms_link\">Aliacms</a>&nbsp;</li>";
  }
  if ($blogs_link) {
    $dis_blogs = "<li>&nbsp;<a href=\"$blogs_link\">Blogs</a>&nbsp;</li>";
  }
  
  $block = "
<!-- Bandeau -->
<a href=\"$path/obm.php\">
<img src=\"".C_IMAGE_PATH."/$set_theme/logo_obm.png\" alt=\"Welcome\" id='logo' />
</a>
<ul class=\"header\">
  <li class=\"first\">&nbsp;
  <a href=\"$path/obm.php\">$l_home</a>&nbsp;</li>
  $dis_aliacms  
  $dis_aliamin
  $dis_blogs
  <li class=\"login\">&nbsp;$l_login : $login&nbsp;</li>
  <li class=\"profil\">&nbsp;$l_profile : $profil&nbsp;</li>
  <li class=\"logout\">&nbsp;<a href='".$path."/obm.php?action=logout'>$l_logout</a>&nbsp;</li>
  <li>OBM : $obm_version</li>
</ul>
" . $display["section"] ."
<p style=\"clear:both\" />
" . $display["menu"] ."
<!-- Fin Bandeau -->

" . $display["action"];

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific :Display the section block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections_theme($section) {
  global $cgp_show, $sections, $perm;
  
  $ret = "<ul class='section'>\n";
  foreach ($cgp_show["section"] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value["Name"];
      $section_right = $perm->get_section_rights($key); 
      if (($section_right & $value["Right"]) == $value["Right"]) {
        if ($key == $section) {
          $ret .= "<li>$section_name</li>\n";
        } else {
	  $ret .= "<li><a href='".$value["Url"]."'>$section_name</a></li>\n";
        }
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
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_menus($section, $module) {
  global $set_theme, $set_menu, $cme_txt, $cme_ico, $cme_both;
  global $path, $modules, $perm, $cgp_show;

  if (($set_menu == $cme_ico) || ($set_menu == $cme_both))
    $fico = true;
  else $fico = false;

  if (($set_menu == $cme_txt) || ($set_menu == $cme_both))
    $ftext = true;
  else $ftext = false;

  if ($section != "") {
    $section_link = array_keys($cgp_show["module"],$section);
    if (is_array($section_link)) {
      $block .= "<ul class='menu'>\n";

      foreach ($section_link as $mod) {
        $module_name = $modules[$mod]["Name"];
        $module_right = $perm->get_module_rights($mod); 
        if (($module_right & $modules[$mod]["Right"]) == $modules[$mod]["Right"]) {
          if ($mod == $module) {
            $block .= "<li class=\"activeTab\">";
	  } else {
	    $block .= "<li class=\"inactiveTab\">";
	  }
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
      $block .= "</ul>";
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the action display block of the selected module
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions($section, $module) {
  global $set_theme, $modules, $action, $popup_height;
  global $actions, $popup_width, $perm;

  $name = $modules[$module]["Name"];
  $ico = $modules[$module]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico\" alt=\"\" />&nbsp;";
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
