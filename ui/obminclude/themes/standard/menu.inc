<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// SECURINET - File : menu.inc (standard)                                    //
//           - Desc : Standard menu display File                             //
// 2002-07-16 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the menu                   //
// Parametres:
//   - $tab : active tab 
//   - $menu   : menu selected
///////////////////////////////////////////////////////////////////////////////
function generate_menu($menu, $section) {
  global $set_lang, $set_lang_default, $set_theme, $set_theme_default;
  global $perms_user, $perms_editor, $perms_admin;
  global $popup_width, $popup_height;
  global $auth, $sess, $securinet_version; 
  global $cong_app, $cong_usr, $cong_adm;
  global $col_clear_grey, $col_dark_grey, $col_white;
  global $font_version, $font_fonction,$obm_version;

  if ($menu == "")
    $path = "";
  else 
    $path = "..";

  $lang_file = "secinclude/lang/$set_lang/header.inc";
  if (file_exists("$path/../$lang_file")) {
    include ($lang_file);
  } else {
    include ("secinclude/lang/$set_lang_default/header.inc");
  }

  $theme_file = "secinclude/themes/$set_theme/theme.inc";
  if (file_exists("$path/../$theme_file")) {
    include ($theme_file);
  } else {
    include ("secinclude/themes/$set_theme_default/theme.inc");
  }

  $uid = $auth->auth["uid"];
  $login = $auth->auth["uname"];
  $profil = $auth->auth["perm"];

  echo "
<table valign=top cellpadding=0 cellspacing=0 width=100% border=0>
<tr>
<!------------ tableau du logo et identification société --------------------->
  <td valign=top width=180>
    <table cellspacing=0 cellpadding=0 border=0 width=180 valign=top bgcolor=\"#$col_clear_grey\">
    <tr>
      <td colspan=4><img src=\"/images/$set_theme/logo_obm.gif\"></td>
    </tr><tr>
      <td><img src=\"/images/$set_theme/logo_g.gif\"></td>
      <td bgcolor=\"#$col_white\"><img src=\"/images/$set_theme/logo_obm2.gif\"></td>
      <td bgcolor=\"#$col_white\" align=right>
        $font_version<b>v.$obm_version</b></font></td>
      <td><img src=\"/images/$set_theme/logo_d.gif\"></td>
    </tr><tr>
      <td valign=top><img src=\"/images/$set_theme/logo_bg.gif\"></td>  
      <td colspan=2><img src=\"/images/$set_theme/logo_bm.gif\"></td>
      <td valign=top><img src=\"/images/$set_theme/logo_bd.gif\"></td>
    </tr>
    </table>
  </td>
<!-------------- fin du tableau du logo et identification société ------------>

<!--------- Tableau partie droite du bandeau (Login, Onglets, menus) --------->

  <td valign=top>
    <table border=0 cellspacing=0 cellpadding=0 valign=top width=100%>
    <tr>
    <!--------------------------- zone Login --------------------------------->

      <td height=21 valign=top bgcolor=\"#$col_dark_grey\" width=100% nowrap>
        <img src=\"/images/$set_theme/login.gif\" align=middle>
        $font_fonction<b>Login&nbsp;: $login&nbsp;&nbsp;</b></font>
        <img src=\"/images/$set_theme/profil.gif\" align=middle>
        $font_fonction<b>Profil&nbsp;: $profil&nbsp;</b></font>
        <img src=\"/images/$set_theme/logout.gif\" align=middle>
        <a href=\"$path/obm.php?action=logout\">$font_fonction<b>Logout</b></font></a>
      </td>
      <td height=21 align=right bgcolor=\"#$col_dark_grey\" nowrap>
        $font_fonction<b>Société Aliacom</b></font>
      </td>
      <td bgcolor=\"$col_dark_grey\">&nbsp;</td>
    <!--------------------------- Fin zone Login ----------------------------->
    </tr><tr>

    <!--------------------------- zone Onglets ------------------------------->
      <td colspan=2>";
  display_tabs($section);
  echo "
      </td>
    <!-------------------------- Fin zone Onglets ---------------------------->

    </tr><tr>
    <!-------------------------- zone Menu ----------------------------------->
      <td colspan=2 bgcolor=\"#$col_white\" height=25>";
  display_menus($section);
  echo "
      </td>
    <!-------------------------- Fin zone Menu ------------------------------->
    </tr>
    </table>
  </td>
</tr>
</table>
<br>";

  display_menu_bar($section, $menu);
  echo "<br>";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction whicjh perform the item of the selected module                      //
// Parameters:
//   - $menu   : selected menu 
///////////////////////////////////////////////////////////////////////////////
function display_menu_bar($section, $menu) {
  global $set_theme, $menus, $action ;
  global $col_barre, $font_fonction,$actions;

  echo "
<table width=90% cellspacing=0 cellpadding=0 align=center>
  <tr>
    <td align=right><img src=\"/images/$set_theme/barre_g.gif\"></td>
    <td bgcolor=\"#$col_barre\">$font_fonction<b>".$menus[$section][$menu]["Name"]."</b></font></td>
    <td bgcolor=\"#$col_barre\" align=right>
";
   if(is_array($actions[$menu])){
     foreach($actions[$menu] as $key => $value){
       if(in_array("all",$value["Condition"]) || in_array($action,$value["Condition"])) {
         echo "
          <a href=\"".$value["Url"]."\">$font_fonction<b>".$value["Name"]."</b></font></a> &nbsp;
         ";
       }
     }
   }  
    echo "
    </td>
    <td><img src=\"/images/$set_theme/barre_d.gif\"></td>
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
  global $set_theme, $cong_app, $cong_usr, $cong_adm;
  global $path, $col_menu, $font_menu, $menus;

    echo "
        <table cellspacing=0 cellpadding=0 border=0>
        <tr>
    ";
  if(is_array($menus[$section])) {
    foreach($menus[$section] as $key => $value) {
      if($key != "Name" && $key != "Url") {
        echo "
            <td>&nbsp;&nbsp;</td>
            <td nowrap><img src=\"/images/$set_theme/puce.gif\">&nbsp;
            <a href=\"".$value["Url"]."\">$font_menu".$value["Name"]."</font></a></td>
        ";
      }
    }
  }
  echo "
          <td>&nbsp;&nbsp;&nbsp;&nbsp;</td>
          <td align=right width=100%>";
 //   display_bookmarks() ;
    echo "</td>
        </tr>  
        </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction which perform the tabs                                        //
// Parameters:
//   - $section : acrtive section 
///////////////////////////////////////////////////////////////////////////////
function display_tabs($section) {
  global $col_green, $menus,$col_blue;

  echo "
        <table cellspacing=0 cellpadding=0 border=0>
        <tr>
          <td>";
  foreach($menus as $key => $value) {
    echo "<td>";
    if ($key == $section) {
      display_active_tab($value);
    } else {
      display_inactive_tab($value);
    }
    echo "</td>";
  }
     echo "
          <!------- Remplissage apres tabs -------------------------------->
	  <td height=100% width=100%>
            <table cellpadding=0 cellspacing=0 border=0 width=100% height=100%>
            <tr>
              <td bgcolor=\"#$col_blue\" height=32></td>
	    </tr><tr>
              <td height=1 bgcolor=\"#$col_green\" width=100%></td>  
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
  global $set_theme, $col_white, $col_black;

  $font_tab_a = "<font face=\"verdana,arial,helvetica,sand-serif\" color=\"#$col_black\" size=-2>";

  echo "
    <table cellpadding=0 cellspacing=0 border=0>
    <tr>
      <td><img src=\"/images/$set_theme/tab_actif_hg.gif\"></td>
      <td><img src=\"/images/$set_theme/tab_actif_h.gif\"></td>
      <td><img src=\"/images/$set_theme/tab_actif_hd.gif\"></td>
    </tr><tr>
      <td><img src=\"/images/$set_theme/tab_actif_g.gif\"></td>  
      <td bgcolor=\"#$col_white\" align=center>$font_tab_a<b>".$tab["Name"]."</b></font></td>
      <td><img src=\"/images/$set_theme/tab_actif_d.gif\"></td>  
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
  global $set_theme, $col_green, $col_black, $col_tab, $col_tab_texte;

  $font_tab = "<font face=\"verdana,arial,helvetica,sand-serif\" color=\"#$col_tab_texte\" size=1>";

  echo "
    <table cellpadding=0 cellspacing=0 border=0>
    <tr>
      <td><img src=\"/images/$set_theme/tab_inactif_hg.gif\"></td>
      <td><img src=\"/images/$set_theme/tab_inactif_h.gif\"></td>
      <td><img src=\"/images/$set_theme/tab_inactif_hd.gif\"></td>
    </tr><tr>
      <td rowspan=2><img src=\"/images/$set_theme/tab_inactif_g.gif\"></td>
      <td bgcolor=\"#$col_tab\" align=center height=19><a href=\"".$tab["Url"]."\">$font_tab<b>".$tab["Name"]."</b></a></font></td>
      <td rowspan=2><img src=\"/images/$set_theme/tab_inactif_d.gif\"></td>
    </tr><tr>
      <td height=1 bgcolor=\"#$col_green\"></td>
    </tr>  
    </table>
";
}

