<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : display.inc (grey)                                           //
//     - Desc : Standard global display File                                 //
// 2003-09-18 Bastien Continsouzas                                           //
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
    ". $display["last"] ."
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
// Display: HTML Head
///////////////////////////////////////////////////////////////////////////////
function display_head($p_module) { 
  global $l_title, $set_theme, $obminclude, $extra_css, $extra_js;

  if (isset($extra_css))
    $dis_extra_css = "<link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/$extra_css\" />";

  $headers_e = "
<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"
\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">
<html xmlns=\"http://www.w3.org/1999/xhtml\"> ";

  $headers_et = "<html>";

  return "
$headers_e
<head>
  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-15\" />
  <title>$l_title - $p_module</title>
  <link rel=\"stylesheet\" type=\"text/css\" href=\"/images/$set_theme/style.css\" />
  $dis_extra_css
  $extra_js
</head>

<body>";
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
  $display["todo"] = display_todo_toplist();


  $block = "
    <div class=\"header\">
     <div class=\"logo\">&nbsp;</div>

     <!-- zone Login -->
     <div> 
      OBM v. $obm_version Société Aliacom<br />
      Login&nbsp;: $login&nbsp;&nbsp;<br />

      Profil&nbsp;: $profil&nbsp; <br />
      <a href=\"$path/obm.php?action=logout\">Logout</a>
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
    <div class=\"action\"> 
     <div class=\"actionTitle\">&nbsp;:: Action</div>
    ";

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
    foreach ($menus[$section] as $key => $value) {
      if (($key != "Name" && $key != "Url" && $key != "Right") 
          && 
         (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"])) {
	if ($fico) {
	  $ico = $value["Ico"];
	  if ($ico)
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"/images/$set_theme/$ico\" alt=\"\" /></a>\n";
	  else
	    $dis_ico = "<a class=\"menus\" href=\"".$value["Url"]."\"><img src=\"/images/$set_theme/puce.gif\" alt=\"\" /></a>\n";
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
// Display : Returns the section display block
// Parameters:
//   - $section : active section
///////////////////////////////////////////////////////////////////////////////
function display_sections($section) {
  global $menus, $auth, $perm;
  global $perm_admin;

  foreach ($menus as $key => $value) {
    if (($perm->permissions[$auth->auth["perm"]] & $value["Right"]) == $value["Right"]) {
      if ($key == $section) {
        $block .=  display_active_tab($value);
      } else {
        $block .=  display_inactive_tab($value);
      }
    }
  }

  return $block;
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
    <img src=\"/images/$set_theme/barre_verti.png\" width=\"2\" height=\"14\" alt=\"\" />
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
    <img src=\"/images/$set_theme/barre_verti.png\" width=\"2\" height=\"14\" alt=\"\" />
    &nbsp;&nbsp;&nbsp;&nbsp;";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_last_visited($path="..") {
  global $set_theme, $ico_project;
  global $ico_company, $ico_contact, $ico_parentdeal, $ico_deal, $ico_list;
  global $ico_contract, $ico_incident, $ico_account, $ico_invoice,$ico_payment;
  global $ico_user, $ico_group;
  global $last_company,$last_contact,$last_deal,$last_parentdeal,$last_list;
  global $last_contract, $last_incident, $last_project;
  global $last_account,$last_invoice, $last_payment;
  global $last_user, $last_group;
  global $last_company_name, $last_contact_name, $last_deal_name;
  global $last_parentdeal_name, $last_list_name, $last_contract_name;
  global $last_incident_name, $last_project_name;
  global $last_payment_name, $last_account_name, $last_invoice_name;
  global $last_user_name, $last_group_name;

  $block = "
  <div class=\"last\"> 
  <div class=\"lastTitle\">:: Bookmarks</div>
  <ul class=\"lastList\">";

  if ($last_company_name != "") {
    $company_url = url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=".$last_company);
    $company_name = substr($last_company_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$company_url\"><img src=\"/images/$set_theme/$ico_company\" alt=\"\" /></a>
        <a href=\"$company_url\">$company_name</a></li>";
  }

  if ($last_contact_name != "") {
    $contact_url = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$last_contact);
    $contact_name = substr($last_contact_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$contact_url\"><img src=\"/images/$set_theme/$ico_contact\" alt=\"\" /></a>
        <a href=\"$contact_url\">$contact_name</a></li>";
  }

  if ($last_parentdeal_name != "") {
    $parentdeal_url = url_prepare("$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$last_parentdeal);
    $parentdeal_name = substr($last_parentdeal_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$parentdeal_url\"><img src=\"/images/$set_theme/$ico_parentdeal\" alt=\"\" /></a>
        <a href=\"$parentdeal_url\">$parentdeal_name</a></li>";
  }

  if ($last_deal_name != "") {
    $deal_url = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$last_deal);
    $deal_name = substr($last_deal_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$deal_url\"><img src=\"/images/$set_theme/$ico_deal\" alt=\"\" /></a>
        <a href=\"$deal_url\">$deal_name</a></li>";
  }

  if ($last_list_name != "") {
    $list_url = url_prepare("$path/list/list_index.php?action=detailconsult&amp;param_list=".$last_list);
    $list_name = substr($last_list_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$list_url\"><img src=\"/images/$set_theme/$ico_list\" alt=\"\" /></a>
        <a href=\"$list_url\">$list_name</a></li>";
  }

  if ($last_account_name != "") {
    $account_url = url_prepare("$path/account/account_index.php?action=detailconsult&amp;param_account=".$last_account);
    $account_name = substr($last_account_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$account_url\"><img src=\"/images/$set_theme/$ico_account\" alt=\"\" /></a>
        <a href=\"$account_url\">$account_name</a></li>";
  }

  if ($last_invoice_name != "") {
    $invoice_url = url_prepare("$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$last_invoice);
    $invoice_name = substr($last_invoice_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$invoice_url\"><img src=\"/images/$set_theme/$ico_invoice\" alt=\"\" /></a>
        <a href=\"$invoice_url\">$invoice_name</a></li>";
  }

  if ($last_payment_name != "") {
    $payment_url = url_prepare("$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$last_payment);
    $payment_name = substr($last_payment_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$payment_url\"><img src=\"/images/$set_theme/$ico_payment\" alt=\"\" /></a>
        <a href=\"$payment_url\">$payment_name</a></li>";
  }

  if ($last_contract_name != "") {
    $contract_url = url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$last_contract);
    $contract_name = substr($last_contract_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$contract_url\"><img src=\"/images/$set_theme/$ico_contract\" alt=\"\" /></a>
        <a href=\"$contract_url\">$contract_name</a></li>";
  }

  if ($last_project_name != "") {
    $project_url = url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=".$last_project);
    $project_name = substr($last_project_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$project_url\"><img src=\"/images/$set_theme/$ico_project\" alt=\"\" /></a>
        <a href=\"$project_url\">$project_name</a></li>";
  }

  if ($last_incident_name != "") {
    $incident_url = url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$last_incident);
    $incident_name = substr($last_incident_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$incident_url\"><img src=\"/images/$set_theme/$ico_incident\" alt=\"\" /></a>
        <a href=\"$incident_url\">$incident_name</a></li>";
  }

  if ($last_user_name != "") {
    $user_url = url_prepare("$path/user/user_index.php?action=detailconsult&amp;param_user=".$last_user);
    $user_name = substr($last_user_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$user_url\"><img src=\"/images/$set_theme/$ico_user\" alt=\"\" /></a>
        <a href=\"$user_url\">$user_name</a></li>";
  }

  if ($last_group_name != "") {
    $group_url = url_prepare("$path/group/group_index.php?action=detailconsult&amp;param_group=".$last_group);
    $group_name = substr($last_group_name,0,20);
    $block .= "<li class=\"lastList\"><a href=\"$group_url\"><img src=\"/images/$set_theme/$ico_group\" alt=\"\" /></a>
        <a href=\"$group_url\">$group_name</a></li>";
  }

  $block .= "
    </ul>
  </div>";

  return $block;
}

// bcontins todo experiment (cette fontion)
///////////////////////////////////////////////////////////////////////////////
// Display : Returns the last visited display block
// Parameters:
//   - $path : path to the obm php root (for the first screen after login)
///////////////////////////////////////////////////////////////////////////////
function display_todo_toplist($path="..") {
  global $todo_1_id, $todo_1_title, $todo_2_id, $todo_2_title;
  global $todo_3_id, $todo_3_title, $todo_4_id, $todo_4_title;
  global $todo_5_id, $todo_5_title;

  $block = "
    <div class=\"last\"> 
     <div class=\"lastTitle\">:: Todo</div>
     <ul class=\"lastList\">";

  if ($todo_1_id) {
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=".$todo_1_id);
    $todo_title = substr($todo_1_title,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($todo_2_id) {
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=".$todo_2_id);
    $todo_title = substr($todo_2_title,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($todo_3_id) {
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=".$todo_3_id);
    $todo_title = substr($todo_3_title,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($todo_4_id) {
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=".$todo_4_id);
    $todo_title = substr($todo_4_title,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  if ($todo_5_id) {
    $todo_url = url_prepare("$path/todo/todo_index.php?action=detailconsult&amp;param_todo=".$todo_5_id);
    $todo_title = substr($todo_5_title,0,30);
    $block .= "<li class=\"lastList\"><a href=\"$todo_url\">$todo_title</a></li>";
  }

  $block .= "
     </ul>
    </div>";

  if (!($todo_1_id))
    $block = "";

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
  //  echo "<p class=\"messageOk\">".htmlspecialchars($msg)."</p>";
  return "<p class=\"messageOk\">$msg</p>";
}

////////////////////////////////////////////////////////////////////////////
// Display an Info message
////////////////////////////////////////////////////////////////////////////
function display_info_msg($msg) {
  return "<p class=\"messageInfo\">".htmlspecialchars($msg)."</p>";
}
