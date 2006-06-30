<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : display.inc (standard)                                       //
//     - Desc : Standard global display File                                 //
// 2003-08-07 Pierre Baudracco                                               //
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

  // If print activated, display only the detail
  if ($output_target == "print") {
    echo $display["head"] . "
      <!-- left panel -->
      <div class=\"leftpanel\">
" .
      $display["features"] ."
      </div>
      <div class=\"detailpanelalone\">
" .
      $display["title"] .
      $display["msg"] .
      $display["detail"] . "
      </div>" .
      $display["end"];
    return;
  }

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

  if ($display["print_url"] != "") {
    $dis_printer = "<li><a target=\"top\" href=\"".$display["print_url"]."&amp;output_target=print\"><img src=\"".C_IMAGE_PATH."/$set_theme/ico_printer.gif\" alt=\"Print\" id='print' /></a></li>";
  }

  $block = "
<!-- Bandeau -->

<div class='header'>
<a href=\"$path/obm.php\">
<img src=\"".C_IMAGE_PATH."/$set_theme/logo_obm.png\" alt=\"Welcome\" id='logo' />
</a>
<span id='version'>OBM v.".$obm_version." Aliacom</span> 
<!-- Login -->
<ul class='login'>
<li><img src='".C_IMAGE_PATH."/".$set_theme."/login.gif' alt='' /> $l_login : $login</li>
<li><img src='".C_IMAGE_PATH."/".$set_theme."/profil.gif' alt='' /> $l_profile : $profil</li>
<li><img src='".C_IMAGE_PATH."/".$set_theme."/logout.gif' alt='' /> <a href='".$path."/obm.php?action=logout'>$l_logout</a></li>
$dis_printer
</ul>
<!-- Fin Login -->
<div class=\"entries\">
<!-- Onglets Section Module -->
" . $display["section"] ."" . $display["module"] ."
<!-- Fin Module -->
</div>
</div>
<p style=\"clear:both\" />
<!-- Fin Bandeau -->

" . $display["action"];

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific :Display the head block
// Parameters:
//   - $p_module : module selected
///////////////////////////////////////////////////////////////////////////////
// function display_head_theme($p_module) {


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the section block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
// function display_sections_theme($section) {


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the module block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
// function display_modules_theme($section, $module) {


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the action block
// Parameters:
//   - $section : selected section
//   - $module  : selected module
///////////////////////////////////////////////////////////////////////////////
function display_actions_theme($section, $module) {
  global $set_theme, $modules, $action, $popup_height;
  global $actions, $popup_width, $perm;

  $name = $modules[$module]["Name"];
  $ico = $modules[$module]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico\" alt=\"\" />&nbsp;";
  else
    $dico = "";

  if (is_array($actions[$module])) {
    $module_right = $perm->get_module_rights($module); 
    
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
	    $content .= "
              <a href=\"\" class=\"action\" 
              onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
	  } else {
	    $content .= "
               <a class=\"action\" href=\"".$value["Url"]."\">".$value["Name"]."</a> &nbsp;";
	  }
	}
      }
    }
  }

  if ($content != "") {
    $ret = "
<table cellpadding=\"0\" cellspacing=\"0\" class=\"action\">
  <tr>
    <td><img class=\"actionIconG\" src=\"".C_IMAGE_PATH."/$set_theme/barre_g.gif\" alt=\"\" /></td>
    <td class=\"actionLabel\">$dico$name</td>
    <td class=\"actionText\">
    $content
    </td>
    <td><img class=\"actionIconD\" src=\"".C_IMAGE_PATH."/$set_theme/barre_d.gif\" alt=\"\" /></td>
  </tr>
</table>
";
  } else {
    $ret = "";
  }

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
