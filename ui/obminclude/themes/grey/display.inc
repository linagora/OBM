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
// Display : Function that display the page menu
// Parameters:
//   - $module : module selected
///////////////////////////////////////////////////////////////////////////////
function display_menu($module) {
  global $path, $auth, $display, $obm_version, $cgp_show,$set_lang, $set_theme;
  global $l_login, $l_logout, $l_profile;

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];
  $section = $cgp_show["module"][$module];

  $display["section"] = display_sections($section);
  $display["module"] = display_modules($section, $module);
  $display["action"] = display_actions($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todos();


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
    <div class=\"module\">
    ". $display["module"] ."
    </div>
    <!-- Fin zone Menu -->

    ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific :Display the section block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections_theme($section) {
  global $sections, $perm;
  global $perm_admin;

  foreach ($sections as $key => $value) {
    $section_name = $value["Name"];
    $section_right = $perm->get_section_rights($key); 
    if ($section_right) {
      if ($key == $section) {
        $ret .= display_active_tab($value);
      }	else {
        $ret .= display_inactive_tab($value);
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
// Theme specific : Display the module block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
//function display_modules_theme($section, $module) {


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the action block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions_theme($section, $module) {
  global $set_theme, $action, $popup_height, $perm;
  global $actions, $popup_width;

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
// Theme specific : Display the last visisted block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
//function display_last_visited_theme($path="..") {


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the todo block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
//function display_todos_theme($path="..") {
