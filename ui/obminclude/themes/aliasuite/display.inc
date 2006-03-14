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
  global $l_login, $l_logout, $l_profile, $l_home;
  global $aliamin_link, $aliacms_link, $blogs_link;
  
  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];
  $section = $cgp_show["module"][$module];
  $display["section"] = display_sections($section);
  $display["module"] = display_modules($section, $module);
  $display["action"] = display_actions($section, $module);
  $display["last"] = display_last_visited();
  $display["todo"] = display_todos();

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
" . $display["module"] ."
<!-- Fin Bandeau -->

" . $display["action"];

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Theme specific : Display the section block
// Parameters:
//   - $section : selected section
///////////////////////////////////////////////////////////////////////////////
function display_sections_theme($section) {
  global $cgp_show, $sections, $perm;
  
  $ret = "<ul class='section'>\n";
  foreach ($cgp_show["section"] as $key => $show) {
    if ($show) {
      $value = $sections[$key];
      $section_name = $value["Name"];
      $section_right = $perm->get_section_rights($key); 
      if ($section_right) {
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
//function display_actions_theme($section, $module) {

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
