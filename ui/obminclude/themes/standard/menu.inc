<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : menu.inc (standard)                                          //
//     - Desc : Standard menu display File                                   //
// 2000-07-01 Pierre Baudracco : Last Update 2000-10-10                      //
///////////////////////////////////////////////////////////////////////////////
// Modified by Frodo (mwalker@mandrakesoft.com) for altername layout         //
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
  global $set_lang, $set_theme, $set_lang_default, $set_theme_default, $my_id, $db_id, $dbname, $obm_version;
  global $perms_user, $perms_editor, $perms_admin;
  global $popup_width, $popup_height;
  global $auth, $sess; 
  global $l_select_company;

  if ($menu == "")
    $path = "";
  else 
    $path = "../";

  $lang_file = "obminclude/lang/$set_lang/header.inc";
  if (file_exists($path.$lang_file)) {
    include ($lang_file);
  } else {
    include ("obminclude/lang/$set_lang_default/header.inc");
  }

  $theme_file = "obminclude/themes/$set_theme/theme.inc";
  if (file_exists($path.$theme_file)) {
    include ($theme_file);
  } else {
    include ("obminclude/themes/$set_theme_default/theme.inc");
  }

  echo "\n<CENTER>";
  
  echo "<TABLE Width=100% BORDER=1><TR>\n";

  echo "<TD WIDTH=\"24%\">";
  echo "<TABLE><TR><TD>
<A HREF=\"http://www.aliacom.fr/\">
<IMG align=middle border=0 SRC=\"".$path."obminclude/themes/$set_theme/$ico_principal\">
</TD><TD>
</A>$obm_version<BR>";
  echo "<FONT COLOR=\"#$col_textem\">" . $auth->auth["uname"] . "&nbsp;</FONT><BR>";
  echo "<A HREF=\"".$sess->url($path."obm.php?action=logout")."\">Logout</A>";
  echo "</TD></TR></TABLE>";
  echo "</TD>\n";

  echo "\n<TD WIDTH=\"18%\" align=center>";
  
  echo "<A HREF=\"".
     $sess->url($path."company/company_index.php?action=index")."\"";
  echo "target=_top>$l_header_company</A><br>";
  echo "<A HREF=\"".
     $sess->url($path."contact/contact_index.php?action=index")."\"";
  echo "target=_top>$l_header_contact</A><br>";
  echo "<A HREF=\"".
     $sess->url($path."deal/deal_index.php?action=index")."\"";
  echo "target=_top>$l_header_deal</A><br>";
  echo "<A HREF=\"".$sess->url($path."list/list_index.php?action=index")."\"";
  echo "target=_top>$l_header_list</A><br>";

  /////////////////////////////////////////////////////////////////////////////
  // Incident Menu and Contract de support Menu                              //
  /////////////////////////////////////////////////////////////////////////////

  echo "<A HREF=\"".$sess->url($path."contract/contract_index.php?action=index")."\"";
  echo "target=_top>$l_header_contract </A><br>";
  echo "<A HREF=\"".$sess->url($path."contract/incident_index.php?action=index")."\"";
  echo "target=_top>$l_header_incident </A><br>";
  
  echo "</TD>";

  echo "\n<TD WIDTH=\"40%\" align=center>";

  /////////////////////////////////////////////////////////////////////////////
  // SETTINGS Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  if ($menu == "SETTINGS")
    echo "<P>$l_header_title_settings<P>";

  /////////////////////////////////////////////////////////////////////////////
  // COMPANY Submenu                                                         //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "COMPANY") {
    echo "<P>$l_header_title_company<P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"" . $sess->url("company_index.php?action=new") .
        "\" target=_top>" . $l_header_new_f . "</A> | ";
      echo "<A HREF=\"" . $sess->url("company_index.php?action=index") .
        "\" target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"" . $sess->url("company_index.php?action=admin") .
        "\" target=_top>" . $l_header_admin . "</A> | ";
      echo "<A HREF=\"" . $sess->url("company_index.php?action=display") .
        "\" target=_top>" . $l_header_display . "</A>";
    }
  }
  /////////////////////////////////////////////////////////////////////////////
  // CONTACT Submenu                                                         //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "CONTACT") {
    echo "<P>$l_header_title_contact<P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"" . $sess->url("contact_index.php?action=new") .
        "\" target=_top>" . $l_header_new   . "</A> | ";
      echo "<A HREF=\"" . $sess->url("contact_index.php?action=index") .
        "\" target=_top>" . $l_header_find  . "</A> | " ;
      echo "<A HREF=\"" . $sess->url("contact_index.php?action=admin") .
        "\" target=_top>" . $l_header_admin . "</A> | ";
      echo "<A HREF=\"" . $sess->url("contact_index.php?action=display") .
        "\" target=_top>" . $l_header_display . "</A>";
    }
  }
  /////////////////////////////////////////////////////////////////////////////
  // DEAL Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "DEAL") {
    echo "<P>$l_header_title_deal<P>";
    echo "<A HREF=\"" . $sess->url("deal_index.php?action=index") .
      "\" target=_top>" . $l_header_find  . "</A> | ";
    if ($auth->auth["perm"] != $perms_user) {
      $ret_url = urlencode($path."deal/deal_index.php?action=new&amp;param_company=");
      $title = urlencode($l_deal_select_company);
      $url = $sess->url("../company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=$title&amp;url=$ret_url");
      echo "<a href=\"\" onclick=\"window.open('$url','Company','height=$popup_height,width=$popup_width'); return false;\">$l_header_new_f</a> |";
    }
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"" . $sess->url("deal_index.php?action=parent_new") .
        "\" target=_top>" . $l_header_new_parent . "</A> | "; 
    }
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"" . $sess->url("deal_index.php?action=admin") .
        "\" target=_top>" . $l_header_admin . "</A> | "; 
    }
    echo "<A HREF=\"" . $sess->url("deal_index.php?action=display") .
      "\" target=_top>" . $l_header_display . "</A>  ";
  }
  /////////////////////////////////////////////////////////////////////////////
  // LIST Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "LIST") {
    echo "<P>$l_header_title_list<P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"".$sess->url("list_index.php?action=new")."\" " .
           "target=_top>" . $l_header_new_f . "</A> | "; 
      echo "<A HREF=\"".$sess->url("list_index.php?action=index")."\" " .
         "target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"".$sess->url("list_index.php?action=admin")."\" " .
         "target=_top>" . $l_header_admin . "</A> | "; 
    }
    echo "<a href=\"".$sess->url("list_index.php?action=letter_search_fmt")."\">$l_header_export</a> | ";
    echo "<a href=\"".$sess->url("list_index.php?action=display")."\">$l_header_display</a>";
  }
  /////////////////////////////////////////////////////////////////////////////
  // User Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "USER") {
    if ($auth->auth["perm"] == $perms_admin) {
      echo "<P>$l_header_title_user<P>";
      echo "<A HREF=\"".
     $sess->url("user_index.php?action=new")."\" " .
           "target=_top>" . $l_header_new . "</A> | ";
      echo "<A HREF=\"".
     $sess->url("user_index.php?action=index")."\" " .
           "target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"".
     $sess->url("user_index.php?action=admin")."\" " .
           "target=_top>" . $l_header_admin . "</A>"; 
    };
  }  
  /////////////////////////////////////////////////////////////////////////////
  // Computer Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "COMPUTER") {
    echo "<P>$l_header_title_computer<P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"".
          $sess->url("computer_index.php?action=new")."\" " .
           "target=_top>" . $l_header_new . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("computer_index.php?action=index")."\" " .
           "target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("computer_index.php?action=admin")."\" " .
           "target=_top>" . $l_header_admin . "</A>"; 
    }
  }
  /////////////////////////////////////////////////////////////////////////////
  // Contract Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "CONTRACT") {
    echo "<P>$l_header_title_contract <P>";
    if ($auth->auth["perm"] != $perms_user) {
      $ret_url = urlencode($path."contract/contract_index.php?action=new&amp;param_company=");
      $title = urlencode($l_select_company);
      $url = $sess->url("../company/company_index.php?action=ext_get_id_url&amp;popup=1&amp;title=$title&amp;url=$ret_url");
      echo "<a class=header_green href=\"\" onclick=\"window.open('$url','Company','height=$popup_height,width=$popup_width'      ); return false;\">$l_header_new</a> | ";
      echo "<A HREF=\"".
          $sess->url("contract_index.php?action=index")."\" " .
           "target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("contract_index.php?action=admin")."\" " .
           "target=_top>" . $l_header_admin . "</A> | ";
     echo "<A HREF=\"".
          $sess->url("contract_index.php?action=display")."\" " .
           "target=_top>" . $l_header_display . "</A>"; 
 
    }
  }
  /////////////////////////////////////////////////////////////////////////////
  // Incident Submenu                                                        //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "INCIDENT") {
    echo "<P>$l_header_title_incident <P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"".
          $sess->url("incident_index.php?action=new")."\" " .
           "target=_top>" . $l_header_new . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("incident_index.php?action=index")."\" " .
           "target=_top>" . $l_header_find  . "</A> | ";
     echo "<A HREF=\"".
          $sess->url("incident_index.php?action=display")."\" " .
           "target=_top>" . $l_header_display . "</A>"; 
 
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // Agenda Submenu                                                          //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "AGENDA") {
    echo "<P>$l_header_title_agenda<P>";
    if ($auth->auth["perm"] != $perms_user) {
      echo "<A HREF=\"".
          $sess->url("agenda_index.php?action=new")."\" " .
           "target=_top>" . $l_header_new_event . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("agenda_index.php?action=view_week")."\" " .
           "target=_top>" . $l_header_week  . "</A> | ";
      echo "<A HREF=\"".
          $sess->url("agenda_index.php?action=view_day")."\" " .
           "target=_top>" . $l_header_day . "</A> | "; 
      echo "<A HREF=\"".
          $sess->url("agenda_index.php?action=view_month")."\" " .
           "target=_top>" . $l_header_month . "</A>"; 
    }
  }
  /////////////////////////////////////////////////////////////////////////////
  // ACCOUNT Submenu            
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu=="ACCOUNT") {
    // FIXME pas de permissions spéciales
      echo "<P>$l_header_title_account<P>";
      echo "<A HREF=\"account_index.php?action=new\" " .
           "target=_top>" . $l_header_new . "</A> | ";
      echo "<A HREF=\"account_index.php?action=index\" " .
           "target=_top>" . $l_header_find  . "</A> | ";
      echo "<A HREF=\"account_index.php?action=admin\" " .
           "target=_top>" . $l_header_admin . "</A>"; 
  }

  /////////////////////////////////////////////////////////////////////////////
  // ADMIN Submenu                                                           //
  /////////////////////////////////////////////////////////////////////////////
  else if ($menu == "ADMIN") {
    if ($auth->auth["perm"] == $perms_admin) {
      echo "<P>$l_header_title_admin<P>";
      echo "<a href=\"".$sess->url("admin_lang_index.php?mode=html&amp;action=index")."\">$l_header_admin_lang</a> | ";
      echo "<a href=\"".$sess->url("admin_lang_index.php?mode=html&amp;action=help")."\">$l_header_admin_lang_help</a>";
    }
  }

  echo "</TD>";

  /////////////////////////////////////////////////////////////////////////////
  // User Menu                                                               //
  /////////////////////////////////////////////////////////////////////////////
  echo "<TD WIDTH=\"18%\" align=center>&nbsp;";
  if ($auth->auth["perm"] == $perms_admin) {
    echo "<a href=\"".
      $sess->url($path . "user/user_index.php?action=index").
      "\"". " TARGET=\"_top\">".$l_header_user."</A>";
  };
  
  echo "<br>";
  /////////////////////////////////////////////////////////////////////////////
  // Computer Menu                                                           //
  /////////////////////////////////////////////////////////////////////////////
  echo "<a href=\"".
	$sess->url($path . "computer/computer_index.php?action=index")."\"". " TARGET=\"_top\">".$l_header_computer."</A><br>" ;



  /////////////////////////////////////////////////////////////////////////////
  // Agenda Menu                                                             //
  /////////////////////////////////////////////////////////////////////////////
  echo "<a href=\"".
	$sess->url($path . "agenda/agenda_index.php?action=index")."\"". " TARGET=\"_top\">".$l_header_agenda."</A><br>";

  /////////////////////////////////////////////////////////////////////////////
  // Account Menu
  /////////////////////////////////////////////////////////////////////////////
  echo "<A HREF=\"".$path."treso/account_index.php?action=index\"";
  echo "target=_top>$l_header_account</A><BR>";  

  /////////////////////////////////////////////////////////////////////////////
  // Payment Menu
  /////////////////////////////////////////////////////////////////////////////
  echo "<A HREF=\"".$path."treso/payment_index.php?action=index\"";
  echo "target=_top>$l_header_payment</A><br>";  

  /////////////////////////////////////////////////////////////////////////////
  // Invoice Menu
  /////////////////////////////////////////////////////////////////////////////
  echo "<A HREF=\"".$path."treso/invoice_index.php?action=index\"";
  echo "target=_top>$l_header_invoice</A><br>";  

  /////////////////////////////////////////////////////////////////////////////
  // Time Menu
  /////////////////////////////////////////////////////////////////////////////
  echo "<A HREF=\"".$path."time/time_index.php?action=index\"";
  echo "target=_top>$l_header_time</A>";  
  echo "</TD>";

  echo "<TD WIDTH=\"18%\" align=right>";
  echo "<TABLE BORDER=O>" .
       "<TR>" .
       "<TD ALIGN=center><A HREF=\"".
     $sess->url($path . "obm.php")."\" " .
       "TARGET=\"_top\"><IMG BORDER=0 SRC=\"" . 
       $path . "obminclude/themes/" . $set_theme . "/" . $ico_obm . "\"></A></TD>" .
       "</TR><TR>" .
       "<TD ALIGN=center><FONT SIZE=-2><A HREF=\"".
     $sess->url( $path . "settings/settings_index.php")."\" " .
       "TARGET=\"_top\">" . $l_header_settings . "</A>" .
       "</FONT></TD>" . 
       "</TR>";
  if ($auth->auth["perm"] == $perms_admin) {
       echo "<TR><TD ALIGN=center><FONT SIZE=-2><A HREF=\"".
     $sess->url($path . "admin/admin_index.php?mode=html&amp;action=index")."\" " .
       "TARGET=\"_top\">" . $l_header_admin . "</A>" .
       "</FONT></TD></TR>";
  };
  echo "</TABLE></FONT></TD></TR>
</TABLE>
<CENTER><HR></CENTER>";
}
