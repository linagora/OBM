<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : menu.inc (standard)                                          //
//     - Desc : Standard menu display File                                   //
// 2002-07-16 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the menu
// Parametres:
//   - $tab  : active tab 
//   - $menu : menu selected
///////////////////////////////////////////////////////////////////////////////
function generate_menu($menu, $section) {
  global $set_lang, $set_lang_default, $set_theme, $set_theme_default;
  global $perms_user, $perms_editor, $perms_admin;
  global $popup_width, $popup_height;
  global $auth, $obminclude; 
  global $obm_version;

  if ($menu == "")
    $path = "";
  else 
    $path = "..";

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];

  echo "
<table cellpadding=\"0\" cellspacing=\"0\" class=\"global\">
<tr>
<!-- Logo OBM -->
  <td class=\"globalLogo\">
    <img src=\"/images/$set_theme/logo_obm.png\" alt=\"\" />
  </td>

<!-- Tableau partie droite du bandeau (Login, Onglets, menus) -->

  <td class=\"globalHeader\">
    <table cellpadding=\"0\" cellspacing=\"0\" class=\"header\">
    <tr>
    <!-- zone Login -->

      <td class=\"header\">
        <img class=\"header\" src=\"/images/$set_theme/login.gif\" alt=\"\" />
        Login&nbsp;: $login&nbsp;&nbsp;
        <img class=\"header\" src=\"/images/$set_theme/profil.gif\" alt=\"\" />
        Profil&nbsp;: $profil&nbsp;
        <img class=\"header\" src=\"/images/$set_theme/logout.gif\" alt=\"\" />
        <a class=\"header\" href=\"$path/obm.php?action=logout\">Logout</a>
      </td>
      <td class=\"headerRight\">OBM v. $obm_version &nbsp;
        Société Aliacom
      </td>
      <td>&nbsp;</td>
    <!-- Fin zone Login -->
    </tr><tr>

    <!-- zone Onglets -->
      <td colspan=\"2\">";
  display_tabs($section);
  echo "
      </td>
    <!-- Fin zone Onglets -->

    </tr><tr>
    <!-- zone Menu -->
      <td colspan=\"2\" class=\"headerMenu\">";
  display_menus($section);
  echo "
      </td>
    <!-- Fin zone Menu -->
    </tr>
    </table>
  </td>
</tr>
</table>
<p>
</p>
";
  display_menu_bar($section, $menu);
echo "
<p>
</p>";
  display_bookmarks();
}


///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the item of the selected module                    //
// Parameters:
//   - $menu : selected menu 
///////////////////////////////////////////////////////////////////////////////
function display_menu_bar($section, $menu) {
  global $set_theme, $menus, $action, $popup_height, $perm ;
  global $actions, $popup_width, $auth;

  $name = $menus[$section][$menu]["Name"];
  $ico = $menus[$section][$menu]["Ico"];
  if ($ico != "")
    $dico = "<img src=\"/images/$set_theme/$ico\" alt=\"\" />&nbsp;";
  else
    $dico = "";

  echo "
<table cellpadding=\"0\" cellspacing=\"0\" class=\"menuBar\">
  <tr>
    <td><img class=\"menuBarIconG\" src=\"/images/$set_theme/barre_g.gif\" alt=\"\" /></td>
    <td class=\"menuBarLabel\">$dico$name</td>
    <td class=\"menuBarText\">";

   if (is_array($actions[$menu])) {
     foreach($actions[$menu] as $key => $value){
       if (in_array("all",$value["Condition"])
           || in_array($action,$value["Condition"])) {
         if (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"]) {
           if ($value["Popup"] == 1) {
	     if ($value["Target"]) {
	       $wtarget = "window.name='" . $value['Target'] . "';";
	     }
             echo "
              <a href=\"\" class=\"menuBar\" 
              onclick=\"$wtarget window.open('".$value['Url']."','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	      ".$value["Name"]."</a> &nbsp;";
           } else {
            echo "
               <a class=\"menuBar\" href=\"".$value["Url"]."\">".$value["Name"]."</a> &nbsp;";
           }
         }
       }
     }
   }  

   echo "
    </td>
    <td><img class=\"menuBarIconD\" src=\"/images/$set_theme/barre_d.gif\" alt=\"\" /></td>
  </tr>
  </table>
";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the item of the active tab                         //
// Parameters:
//   - $section : active section
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

  echo "
    <table cellpadding=\"0\" cellspacing=\"0\"  class=\"menus\">
      <tr>
    ";
  if (is_array($menus[$section])) {
    foreach ($menus[$section] as $key => $value) {
      if (($key != "Name" && $key != "Url" && $key != "Right") 
          && 
         (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"])) {
	if ($fico) {
	  $ico = $value["Ico"];
	  if ($ico)
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"/images/$set_theme/$ico\" alt=\"\" /></a>";
	  else
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"/images/$set_theme/puce.gif\" alt=\"\" /></a>";
	}

	if ($ftext) {
	  $dis_text = "<a class=\"menus\" href=\"".$value["Url"]."\">".$value["Name"]."</a>";
	}

        echo "
          <td>&nbsp;&nbsp;</td>
          <td class=\"menusLabel\">&nbsp; $dis_ico $dis_text
          </td>
        ";
      }
    }
  }
  echo "
          <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
          <td class=\"menusText\"></td>
        </tr>  
      </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the tabs                                        //
// Parameters:
//   - $section : acrtive section 
///////////////////////////////////////////////////////////////////////////////
function display_tabs($section) {
  global $menus, $auth, $perm;
  global $perm_admin;

  echo "
        <table cellpadding=\"0\" cellspacing=\"0\"  class=\"sections\">
        <tr>
          ";
  foreach($menus as $key => $value) {
    if(($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"]) {
      if ($key == $section) {
        echo "<td>";
        display_active_tab($value);
        echo "</td>";
      } else {
        echo "<td>";
        display_inactive_tab($value);
        echo "</td>";
      }
    }
  }
  echo "
          <!-- Remplissage apres tabs -->
	  <td class=\"sectionsfilling\">
            <table cellpadding=\"0\" cellspacing=\"0\"  class=\"sectionsfilling\">
            <tr>
              <td></td>
	    </tr><tr>
              <td class=\"sectionsLine\"></td>  
            </tr>	 
            </table>
          </td>
	  
        </tr>
        </table>
";

}


///////////////////////////////////////////////////////////////////////////////
// Fonction qui affiche un tab actif                                      //
// Parameters:
//   - $tab : tab's parameters
///////////////////////////////////////////////////////////////////////////////
function display_active_tab($tab) {
  global $set_theme;

  echo "
    <table cellpadding=\"0\" cellspacing=\"0\" class=\"sections\">
      <tr>
        <td><img src=\"/images/$set_theme/tab_actif_hg.gif\" alt=\"\" /></td>
        <td><img src=\"/images/$set_theme/tab_actif_h.gif\" alt=\"\" /></td>
        <td><img src=\"/images/$set_theme/tab_actif_hd.gif\" alt=\"\" /></td>
      </tr><tr>
        <td><img class=\"sections\" src=\"/images/$set_theme/tab_actif_g.gif\" alt=\"\" /></td>  
        <td class=\"sectionsActifLabel\">".$tab["Name"]."</td>
        <td><img class=\"sections\" src=\"/images/$set_theme/tab_actif_d.gif\" alt=\"\" /></td>  
      </tr>	 
    </table>	 
";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction qui affiche un tab inactif                                    //
// Parameters:
//   - $tab : tab's parameters
///////////////////////////////////////////////////////////////////////////////
function display_inactive_tab($tab) {
  global $set_theme;


  echo "
    <table cellpadding=\"0\" cellspacing=\"0\"  class=\"sections\">
      <tr>
        <td><img class=\"sections\" src=\"/images/$set_theme/tab_inactif_hg.gif\" alt=\"\" /></td>
        <td><img class=\"sections\" src=\"/images/$set_theme/tab_inactif_h.gif\" alt=\"\" /></td>
        <td><img class=\"sections\" src=\"/images/$set_theme/tab_inactif_hd.gif\" alt=\"\" /></td>
      </tr><tr>
        <td rowspan=\"2\"><img src=\"/images/$set_theme/tab_inactif_g.gif\" alt=\"\" /></td>
        <td class=\"sectionsInactifLabel\"><a class=\"sections\" href=\"".$tab["Url"]."\">".$tab["Name"]."</a></td>
        <td rowspan=\"2\"><img class=\"sections\" src=\"/images/$set_theme/tab_inactif_d.gif\" alt=\"\" /></td>
      </tr><tr>
        <td class=\"sectionsLine\"></td>
      </tr>  
    </table>
";
}


///////////////////////////////////////////////////////////////////////////////
// Display the links to the last visited company, contact and deal
// we need $path to be able to display bookmarks
// from the first screen after login...
///////////////////////////////////////////////////////////////////////////////
function display_bookmarks($path="..") {
  global $set_theme;
  global $last_company,$last_contact,$last_deal,$last_parentdeal,$last_list;
  global $last_contract, $last_incident;
  global $last_account,$last_invoice, $last_payment;
  global $last_company_name, $last_contact_name, $last_deal_name;
  global $last_parentdeal_name, $last_list_name, $last_contract_name;
  global $last_incident_name;
  global $last_payment_name, $last_account_name, $last_invoice_name;

  $book = "
  <table class=\"bookmarks\">
  <tr>";

  if ($last_company_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=".$last_company)."\">".substr($last_company_name,0,30)."</a></td>";

  if ($last_contact_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$last_contact)."\">".substr($last_contact_name,0,30)."</a></td>";

  if ($last_parentdeal_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$last_parentdeal)."\">".substr($last_parentdeal_name,0,30)."</a></td>";

  if ($last_deal_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$last_deal")."\">".substr($last_deal_name,0,30)."</a></td>";

  if ($last_list_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/list/list_index.php?action=detailconsult&amp;param_list=$last_list")."\">".substr($last_list_name,0,30)."</a></td>";

  if ($last_account_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/account/account_index.php?action=detailconsult&amp;param_account=".$last_account)."\">".substr($last_account_name,0,30)."</a></td>";

  if ($last_invoice_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$last_invoice)."\">".substr($last_invoice_name,0,30)."</a></td>";

  if ($last_payment_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$last_payment)."\">".substr($last_payment_name,0,30)."</a></td>";

  if ($last_contract_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$last_contract)."\">".substr($last_contract_name,0,30)."</a></td>";

  if ($last_incident_name != "")
    $book .= "
    <td class=\"bookmarks\">
      <img alt=\"\" src=\"/images/$set_theme/puce.gif\" />
      <a class=\"bookmarks\" href=\"".url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$last_incident)."\">".substr($last_incident_name,0,30)."</a></td>";

  $book .= "
   </tr>
   </table>
   <hr />";

  echo $book;
}
