<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : menu.inc (standard)                                          //
//     - Desc : Standard menu display File                                   //
// 2000-07-01 Pierre Baudracco modified by Frodo                             //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Fonction qui genere l'en-tete (bandeau) de l'application                  //
// La variable globale $menu est utilisee pour savoir dans quelle partie     //
// de l'application on se trouve (societe, contact,...)                      //
// Cela permet de determiner les chemins relatifs, et la partie specifique   //
// de l'entete.                                                              //
///////////////////////////////////////////////////////////////////////////////
function generate_menu($menu) {
  global $obminclude, $set_lang, $set_theme, $set_lang_default, $set_theme_default;
  global $perms_user, $perms_editor, $perms_admin;
  global $popup_width, $popup_height;
  global $auth, $sess, $obm_version; 
  global $l_deal_select_company, $l_select_company;
  if ($menu == "")
    $path = "";
  else 
    $path = "../";

  //Ici j'ai ajouté des feuilles de styles, à changer d'endroit...
  /*  echo "
<style>
<!--
a.header {color:#FFFF00;text-decoration: none;}
a:hover.header {color:#00CCCC;text-decoration: underline;}
a.header_white {color:#f7fffe;text-decoration: none;}
a:hover.header_white {color:#f7fffe;text-decoration: underline;}
a.header_green {color:#1e5059;text-decoration: none;}
a:hover.header_green {color:#1e5059;text-decoration: underline;}
a.header_black {color:#000000;text-decoration: none;}
a:hover.header_black {color:#000000;text-decoration: underline;}

// -->
</style>
" ;
  */
  $lang_file = "$obminclude/lang/$set_lang/header.inc";
  if (file_exists($path."../".$lang_file)) {
    include ($lang_file);
  } else {
    include ("$obminclude/lang/$set_lang_default/header.inc");
  }

  $theme_file = "$obminclude/themes/$set_theme/theme.inc";
  if (file_exists($path."../".$theme_file)) {
    include ($theme_file);
  } else {
    include ("$obminclude/themes/$set_theme_default/theme.inc");
  }

  if ($auth->auth["perm"] == "admin") {
    $ladmin = "(<a href=\"".url_prepare("$path" . "admin/admin_index.php?mode=html&amp;action=index")."\">
    <font color=black size=-1>$l_header_admin</font></a>)";
  }

  echo "
<center>
<br>
<div align=\"left\">
<table cellspacing=0 cellpadding=0 BORDER=0>
  <tr>
    <td colspan=2>
      <table width=\"100%\" cellspacing=0 cellpadding=0 BORDER=0>
        <tr>
          <td width=\"74\" align=\"left\" background=\"/images/$set_theme/bg-header1.jpg\">
            <img border=\"0\" src=/images/$set_theme/header0.jpg"."></td>
          <td width=\"282\" align=\"left\" background=\"/images/$set_theme/bg-header1.jpg\">
           <font color=\"black\" size=-1><b>User: </b>".$auth->auth["uname"]." $ladmin</font></td>
          <td width=\"282\" align=\"right\" background=\"/images/$set_theme/bg-header1.jpg\">
<b>
<a href=\"".url_prepare($path."obm.php?action=logout")."\"><font color=\"black\" size=-1>Logout</font></a> </b>&middot;<b> 
<a href=\"".url_prepare($path."settings/settings_index.php")."\"><font color=\"black\" size=-1>$l_header_settings</font></a></b></td>
          <td width=\"113\" align=\"right\" background=\"/images/$set_theme/bg-header1.jpg\">
            <img border=\"0\" src=/images/$set_theme/header1.jpg"."></td>
        </tr>
      </table>
    </td>
  </tr>
  <tr>
    <td bgcolor=\"#0d2932\" align=\"left\">&nbsp;<b>";

  if ($menu == "COMPANY") {
    echo "<font color=\"#b2ebe8\"> [$l_header_company]</font>"; 
  } else {
    echo "
        <a class=header_white href=\"".
    url_prepare($path."company/company_index.php?action=index")."\">$l_header_company</a>" ;
  }

  if ($menu == "CONTACT") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_contact]</font>" ;
  } else {
    echo "
   &middot; <a class=header_white href=\"".url_prepare($path."contact/contact_index.php?action=index")."\">$l_header_contact</a>" ;
  }

  if ($menu == "DEAL") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_deal]</font>" ;
  } else {
    echo "
    &middot; <a class=header_white href=\"".url_prepare($path."deal/deal_index.php?action=index")."\">$l_header_deal</a>" ;
  }

  if ($menu == "LIST") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_list]</font>" ;
  } else {
    echo "
    &middot; <a class=header_white href=\"".url_prepare($path."list/list_index.php?action=index")."\">$l_header_list</a>" ;
  }

  if ($menu == "AGENDA") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_agenda]</font>" ;
  } else {
    echo "
    &middot; <a class=header_white href=\"".url_prepare($path."agenda/agenda_index.php?action=index")."\">$l_header_agenda</a>" ;
  }

  if ($auth->auth["perm"] == $perms_admin) {
    if ($menu == "USER") {
      echo " &middot; <font color=\"#b2ebe8\">[$l_header_user]</font>" ;
    }else {
    echo "
    &middot; <a class=header_white href=\"".url_prepare($path."user/user_index.php?action=index")."\">".$l_header_user."</a>" ;
    }
  }
  echo "<BR>testing :";

  if ($menu == "COMPUTER") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_computer]</font>";
  } else {
    echo "
    <a class=header_white href=\"".url_prepare($path."computer/computer_index.php?action=index")."\">$l_header_computer</a>";
  }

  if ($menu == "CONTRACT") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_contract]</font>";
  } else {
    echo "
     &middot; <a class=header_white href=\"".url_prepare($path."contract/contract_index.php?action=index")."\">$l_header_contract</a>";
  }

  if ($menu == "INCIDENT") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_incident]</font>";
  } else {
    echo "
     &middot; <a class=header_white href=\"".url_prepare($path."incident/incident_index.php?action=index")."\">$l_header_incident</a>";
  }

  // TRESO STUFF :
  if ($auth->auth["perm"] == $perms_admin) {
    if ($menu =="ACCOUNT") {
      echo " &middot; <font color=\"#b2ebe8\">[$l_header_account]</font>" ;
    } else {
      echo " &middot; <a class=header_white href=\"".url_prepare($path."account/account_index.php?action=index")."\">$l_header_account</a>";
    }

    if ($menu =="INVOICE") {
      echo " &middot; <font color=\"#b2ebe8\">[$l_header_invoice]</font>" ;
    } else {
      echo " &middot; <a class=header_white href=\"".url_prepare($path."invoice/invoice_index.php?action=index")."\">".$l_header_invoice."</a>" ;
    }

    if ($menu =="PAYMENT") {
      echo " &middot; <font color=\"#b2ebe8\">[$l_header_payment]</font>" ;
    } else {
      echo " &middot; <a class=header_white href=\"".url_prepare($path."payment/payment_index.php?action=index")."\">".$l_header_payment."</a>" ;
    } 
  }

  // Menu TimeManager
  if ($menu == "TIME") {
    echo " &middot; <font color=\"#b2ebe8\">[$l_header_time]</font>";
  } else {
    echo "
    &middot; <a class=header_white href=\"".url_prepare($path."time/time_index.php?action=index")."\">$l_header_time</a>";
  }


  echo "
   </td>
    <td width=25 bgcolor=\"#0d2932\" align=\"right\"><img border=\"0\" src=/images/$set_theme/header2.jpg"."></td>
  </tr>
  <tr>
    <td colspan=2 align=left><img border=\"0\" src=/images/$set_theme/header3.jpg"."></td>
  </tr>
  <tr>
   <td colspan=2>
    <table width=\"100%\" cellspacing=0 cellpadding=0 BORDER=0>
      <tr>
        <td bgcolor=\"#b2ebe8\" align=\"left\"><img border=\"0\" src=/images/$set_theme/header4.jpg"."></td>
        <td bgcolor=\"#b2ebe8\" align=\"left\"><font size=-1 color=\"#1e5059\"><b>";

  /////////////////////////////////////////////////////////////////////////////
  // SETTINGS Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  if ($menu == "SETTINGS") {
    echo "$l_header_title_settings";
  }

  /////////////////////////////////////////////////////////////////////////////
  // ADMIN Submenu                                                           //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "ADMIN") {
    echo "<a href=\"".url_prepare("admin_code_index.php?mode=html&amp;action=index")."\">$l_header_admin_code</a> | ";
    echo "<a href=\"".url_prepare("admin_pref_index.php?mode=html&amp;action=index")."\">$l_header_admin_pref</a> | ";
    echo "<a href=\"".url_prepare("admin_data_index.php?mode=html&amp;action=index")."\">$l_header_admin_data</a> | ";
    echo "<a href=\"".url_prepare("admin_lang_index.php?mode=html&amp;action=index")."\">$l_header_admin_lang</a>";
  }

  /////////////////////////////////////////////////////////////////////////////
  // COMPANY Submenu                                                         //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "COMPANY") {
    echo "<a class=header_green href=\"".url_prepare("company_index.php?action=index")."\">$l_header_find</a>";
    if ($auth->auth["perm"] != $perms_user) {
      echo " | <a class=header_green href=\"".url_prepare("company_index.php?action=new")."\">$l_header_new_f</a> | 
            <a class=header_green href=\"".url_prepare("company_index.php?action=admin")."\">$l_header_admin</a>";
    }
    echo " | <a class=header_green href=\"".url_prepare("company_index.php?action=display")."\">$l_header_display</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // CONTACT Submenu                                                         //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "CONTACT") {
    echo "<a class=header_green href=\"".url_prepare("contact_index.php?action=index")."\">$l_header_find</a>";
    if ($auth->auth["perm"] != $perms_user) {
      echo " | <a class=header_green href=\"".url_prepare("contact_index.php?action=new")."\">$l_header_new</a> | 
            <a class=header_green href=\"".url_prepare("contact_index.php?action=admin")."\">$l_header_admin</a> "; 
    }
    echo " | <a class=header_green href=\"".url_prepare("contact_index.php?action=display")."\">$l_header_display</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // DEAL Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "DEAL") {
    echo "<a class=header_green href=\"".url_prepare("deal_index.php?action=index")."\"> $l_header_find</a> | ";
    if ($auth->auth["perm"] != $perms_user) {
      $ret_url = urlencode($path."deal/deal_index.php?action=new&amp;param_company=");
      $title = urlencode($l_deal_select_company);
      $url = url_prepare("../company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=$title&amp;url=$ret_url");
      echo "<a class=header_green href=\"\" onclick=\"window.open('$url','Company','height=$popup_height,width=$popup_width'); return false;\">$l_header_new_f</a> | 
            <a class=header_green href=\"".url_prepare("deal_index.php?action=parent_new")."\">$l_header_new_parent</a> | 
            <a class=header_green href=\"".url_prepare("deal_index.php?action=admin")."\">$l_header_admin</a> | ";
    }
    echo "<a class=header_green href=\"".url_prepare("deal_index.php?action=display")."\">$l_header_display</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // LIST Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "LIST") {
    echo "<a class=header_green href=\"".url_prepare("list_index.php?action=index")."\">$l_header_find</a> | ";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<a class=header_green href=\"".url_prepare("list_index.php?action=new")."\">$l_header_new_f</a> | 
            <a class=header_green href=\"".url_prepare("list_index.php?action=admin")."\">$l_header_admin</a> | "; 
    }
    if ($auth->auth["perm"] != $perms_user) {
      $ext_url = urlencode($path."list/list_index.php");
      $title = urlencode($l_deal_select_company);
      $url = url_prepare("../list/list_index.php?action=ext_get_ids&amp;popup=1&amp;title=$title&amp;ext_action=export_add&amp;ext_url=$ext_url");
      echo "<a class=header_green href=\"\" onclick=\"window.name='root';window.open('$url','List','height=$popup_height,width=$popup_width'); return false;\">$l_header_export</a> | ";
    }
    echo "<a class=header_green href=\"".url_prepare("list_index.php?action=display")."\">$l_header_display</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // Agenda Submenu                                                          //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "AGENDA") {
    echo "<a class=header_green href=\"".url_prepare("agenda_index.php?action=new")."\">$l_header_new_event</a>";
    echo " | <a class=header_green href=\"".url_prepare("agenda_index.php?action=view_week")."\">$l_header_week</a> | 
          <a class=header_green href=\"".url_prepare("agenda_index.php?action=view_day")."\">$l_header_day</a> | 
          <a class=header_green href=\"".url_prepare("agenda_index.php?action=view_month")."\">$l_header_month</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // User Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "USER") {
    echo "<a class=header_green href=\"".url_prepare("user_index.php?action=index")."\">$l_header_find</a>" ;
    if ($auth->auth["perm"] == $perms_admin) {
      echo " | <a class=header_green href=\"".url_prepare("user_index.php?action=new")."\">$l_header_new</a> | 
            <a class=header_green href=\"".url_prepare("user_index.php?action=admin")."\">$l_header_admin</a>"; 
    }else {echo "&nbsp;";}
  }
  /////////////////////////////////////////////////////////////////////////////
  // Contract Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "CONTRACT") {
    echo "<a class=header_green href=\"".url_prepare("contract_index.php?action=index")."\"> $l_header_find</a> | ";
      $ret_url = urlencode($path."contract/contract_index.php?action=new&amp;param_company=");
      $title = urlencode($l_select_company);
      $url = url_prepare("../company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=$title&amp;url=$ret_url");
      echo "<a class=header_green href=\"\" onclick=\"window.open('$url','Company','height=$popup_height,width=$popup_width'); return false;\">$l_header_new</a> | ";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<a class=header_green href=\"".
          url_prepare("contract_index.php?action=admin")."\" " .
           "target=_top>$l_header_admin</a> | ";
    }
    echo " <a class=header_green href=\"".url_prepare("contract_index.php?action=display")."\">$l_header_display</a>";

  }
  /////////////////////////////////////////////////////////////////////////////
  // Incident Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "INCIDENT") {
    echo "<a class=header_green href=\"".url_prepare("incident_index.php?action=index")."\"> $l_header_find</a> |" ;
      echo " <a href=\"". url_prepare("incident_index.php?action=new")."\" " .
           "target=_top>$l_header_new</a> | ";
      echo " <a class=header_green href=\"".url_prepare("incident_index.php?action=display")."\">$l_header_display</a>";

  }
  /////////////////////////////////////////////////////////////////////////////
  // Computer Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "COMPUTER") {
    echo "<a class=header_green href=\"".url_prepare("computer_index.php?action=index")."\"> $l_header_find</a>" ;
    if ($auth->auth["perm"] != $perms_user) {
      echo " | <a class=header_green href=\"". url_prepare("computer_index.php?action=new")."\">$l_header_new</a> | 
            <a class=header_green href=\"".url_prepare("computer_index.php?action=admin")."\">$l_header_admin</a>";
             
    }else {echo "&nbsp;" ;}
  }
  /////////////////////////////////////////////////////////////////////////////
  // ACCOUNT Submenu            
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu=="ACCOUNT") {
    // FIXME pas de permissions spéciales
     echo "<a class=header_green href=\"".url_prepare("account_index.php?action=index")."\"> $l_header_find</a>" ;
      echo " | <a class=header_green href=\"". url_prepare("account_index.php?action=new")."\">$l_header_new</a> | 
            <a class=header_green href=\"".url_prepare("account_index.php?action=admin")."\">$l_header_admin</a> | "; 
    echo "<a class=header_green href=\"".url_prepare("account_index.php?action=display")."\">$l_header_display</a>" ;
  }

  /////////////////////////////////////////////////////////////////////////////
  // INVOICE Submenu            
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu=="INVOICE") {
    // FIXME pas de permissions spéciales
     echo "<a class=header_green href=\"".url_prepare("invoice_index.php?action=index")."\"> $l_header_find</a>" ;
      echo " | <a class=header_green href=\"". url_prepare("invoice_index.php?action=new")."\">$l_header_new_f</a> | 
            <a class=header_green href=\"".url_prepare("invoice_index.php?action=admin")."\">$l_header_admin</a> | "; 
    echo "<a class=header_green href=\"".url_prepare("invoice_index.php?action=display")."\">$l_header_display</a>" ;
  }
  /////////////////////////////////////////////////////////////////////////////
  // PAYMENT Submenu            
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu=="PAYMENT") {
    // FIXME pas de permissions spéciales
     echo "<a class=header_green href=\"".url_prepare("payment_index.php?action=index")."\"> $l_header_find</a>" ;
      echo " | <a class=header_green href=\"". url_prepare("payment_index.php?action=new")."\">$l_header_new</a> | 
            <a class=header_green href=\"".url_prepare("payment_index.php?action=admin")."\">$l_header_admin</a> |
            <a class=header_green href=\"".url_prepare("payment_index.php?action=reconcile")."\">$l_header_reconcile</a> | "; 
    echo "<a class=header_green href=\"".url_prepare("payment_index.php?action=display")."\">$l_header_display</a>" ;
  } 

  /////////////////////////////////////////////////////////////////////////////
  // TIME Submenu            
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu=="TIME") {
    // FIXME pas de permissions spéciales
    if ($auth->auth["perm"] == $perms_admin) {
     echo "<a class=header_green href=\"".url_prepare("time_index.php?action=index")."\"> $l_header_base</a>" ;
     echo " | <a class=header_green href=\"".url_prepare("time_index.php?action=admin")."\"> $l_header_admin</a>" ;
     echo " | <a class=header_green href=\"".url_prepare("time_index.php?action=stats")."\"> $l_header_stats</a>" ;
    }   
  } else {
    echo "&nbsp;" ;
  }
  echo "</b></font></td>
        <td bgcolor=\"#b2ebe8\" align=\"right\"><img border=\"0\" border=\"0\" src=/images/$set_theme/header5.jpg"."></td>
     </tr>
    </table>
   </td>
  </tr>
  <tr>
    <td colspan=2 align=left><img border=\"0\" src=/images/$set_theme/header6.jpg"."></td>
  </tr>
</table>
</div>
";
}

