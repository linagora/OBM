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

  if (($display["link"] != "") || ($display["detailInfo"] != "")) {
    $right_panel = true;
  } else {
    $right_panel = false;
  }

  echo $display["head"] . $display["header"] . "

    <!-- left panel -->  <div class=\"leftpanel\">" .
      $display["last"] . "<p>" .
      $display["last"] . "
    <!-- left panel end --> </div>

    <div class=\"detailpanel\">";
 
  if ($right_panel) {
    echo "
    <!-- right panel -->  <div class=\"rightpanel\">" .
      $display["detailInfo"] . "<p>" .
      $display["link"] . "
    <!-- right panel end --> </div>

    <!-- center zone --> <div class=\"middlepanel\">";
  }

  echo
    $display["msg"] .
    $display["title"] .
    $display["search"] .
    $display["result"] .
    $display["detail"] . "
    
    <!-- center zone end --> </div>
  ";

  echo
    $display["end"];

}




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
  global $auth, $obminclude, $display; 
  global $obm_version;

  if ($menu == "")
    $path = "";
  else 
    $path = "..";

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];

  $display["section"] = display_sections($section);
  $display["menu"] = display_menus($section);
  $display["action"] = display_menu_bar($section, $menu);
  $display["last"] = display_last_visited();


  $block = "
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
      <td colspan=\"2\">" . $display["section"] ."
      </td>
    <!-- Fin zone Onglets -->

    </tr><tr>
    <!-- zone Menu -->
      <td colspan=\"2\" class=\"headerMenu\">" . $display["menu"] ."
      </td>
    <!-- Fin zone Menu -->
    </tr>
    </table>
  </td>
</tr>
</table>
<p>
</p>" . $display["action"];

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the action display block of the selected module         //
// Parameters:
//   - $section : selected section
//   - $menu    : selected menu (module)
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

  $ret = "
<table cellpadding=\"0\" cellspacing=\"0\" class=\"menuBar\">
  <tr>
    <td><img class=\"menuBarIconG\" src=\"/images/$set_theme/barre_g.gif\" alt=\"\" /></td>
    <td class=\"menuBarLabel\">$dico$name</td>
    <td class=\"menuBarText\">";

   if (is_array($actions[$menu])) {
     foreach ($actions[$menu] as $key => $value){
       if (in_array("all",$value["Condition"])
           || in_array($action,$value["Condition"])) {
         if (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"]) {
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
         }
       }
     }
   }  

   $ret .= "
    </td>
    <td><img class=\"menuBarIconD\" src=\"/images/$set_theme/barre_d.gif\" alt=\"\" /></td>
  </tr>
  </table>
";

   return $ret;
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

  $block = "
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

        $block .= "
          <td>&nbsp;&nbsp;</td>
          <td class=\"menusLabel\">&nbsp; $dis_ico $dis_text
          </td>
        ";
      }
    }
  }
  $block .= "
          <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
          <td class=\"menusText\"></td>
        </tr>  
      </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {
  global $menus, $auth, $perm;
  global $perm_admin;

  $ret = "
        <table cellpadding=\"0\" cellspacing=\"0\"  class=\"sections\">
        <tr>
          ";
  foreach ($menus as $key => $value) {
    if (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"]) {
      if ($key == $section) {
        $ret .= "<td>" . display_active_tab($value) . "</td>";
      } else {
        $ret .= "<td>" . display_inactive_tab($value) . "</td>";
      }
    }
  }
  $ret .= "
        <!-- Remplissage apres tabs -->
	  <td class=\"sectionsfilling\">
          <table cellpadding=\"0\" cellspacing=\"0\" class=\"sectionsfilling\">
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

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_last_visited($path="..") {
  global $set_theme;
  global $ico_company, $ico_contact, $ico_parentdeal, $ico_deal, $ico_list;
  global $ico_contract, $ico_incident, $ico_account, $ico_invlice, $ico_payment;
  global $last_company,$last_contact,$last_deal,$last_parentdeal,$last_list;
  global $last_contract, $last_incident;
  global $last_account,$last_invoice, $last_payment;
  global $last_company_name, $last_contact_name, $last_deal_name;
  global $last_parentdeal_name, $last_list_name, $last_contract_name;
  global $last_incident_name;
  global $last_payment_name, $last_account_name, $last_invoice_name;

  $block = "
  <div class=\"last\"> 
  <div class=\"lastTitle\">:: Bookmarks</div>
  <ul class=\"lastList\">";

  if ($last_company_name != "") {
    $company_url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=".$last_company);
    $company_name = substr($last_company_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$company_url\"><img src=\"/images/$set_theme/$ico_company\" /></a>
        <a href=\"$company_url\">$company_name</a></li>";
  }

  if ($last_contact_name != "") {
    $contact_url = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$last_contact);
    $contact_name = substr($last_contact_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$contact_url\"><img src=\"/images/$set_theme/$ico_contact\" /></a>
        <a href=\"$contact_url\">$contact_name</a></li>";
  }

  if ($last_parentdeal_name != "") {
    $parentdeal_url = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_parentdeal=".$last_parentdeal);
    $parentdeal_name = substr($last_parentdeal_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$parentdeal_url\"><img src=\"/images/$set_theme/$ico_parentdeal\" /></a>
        <a href=\"$parentdeal_url\">$parentdeal_name</a></li>";
  }

  if ($last_deal_name != "") {
    $deal_url = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$last_deal);
    $deal_name = substr($last_deal_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$deal_url\"><img src=\"/images/$set_theme/$ico_deal\" /></a>
        <a href=\"$deal_url\">$deal_name</a></li>";
  }

  if ($last_list_name != "") {
    $list_url = url_prepare("$path/list/list_index.php?action=detailconsult&amp;param_list=".$last_list);
    $list_name = substr($last_list_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$list_url\"><img src=\"/images/$set_theme/$ico_list\" /></a>
        <a href=\"$list_url\">$list_name</a></li>";
  }

  if ($last_account_name != "") {
    $account_url = url_prepare("$path/account/account_index.php?action=detailconsult&amp;param_account=".$last_account);
    $account_name = substr($last_account_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$account_url\"><img src=\"/images/$set_theme/$ico_account\" /></a>
        <a href=\"$account_url\">$account_name</a></li>";
  }

  if ($last_invoice_name != "") {
    $invoice_url = url_prepare("$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$last_invoice);
    $invoice_name = substr($last_invoice_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$invoice_url\"><img src=\"/images/$set_theme/$ico_invoice\" /></a>
        <a href=\"$invoice_url\">$invoice_name</a></li>";
  }

  if ($last_payment_name != "") {
    $payment_url = url_prepare("$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$last_payment);
    $payment_name = substr($last_payment_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$payment_url\"><img src=\"/images/$set_theme/$ico_payment\" /></a>
        <a href=\"$payment_url\">$payment_name</a></li>";
  }

  if ($last_contract_name != "") {
    $contract_url = url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$last_contract);
    $contract_name = substr($last_contract_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$contract_url\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$contract_url\">$contract_name</a></li>";
  }

  if ($last_incident_name != "") {
    $incident_url = url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$last_incident);
    $incident_name = substr($last_incident_name,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$incident_url\"><img src=\"/images/$set_theme/$ico_incident\" /></a>
        <a href=\"$incident_url\">$incident_name</a></li>";
  }

  $block .= "
    </ul>
  </div>";

  return $block;
}