<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : menu.inc (standard)                                         //
//     - Desc  : Standard menu display File                                  //
// 2000-06-05 Frodo (mwalker@mandrakesoft.com) : Last Update 2000-10-10      //
// 2000-12-05 P. Carlier :                                                   //
//      Added session object to keep session without cookie                  //
// $Id$    //
///////////////////////////////////////////////////////////////////////////////

// ----------------------------------------------------------------------------
// Funtion to generate one menu blob
//   Frodo 19/05/2000
// ----------------------------------------------------------------------------
function menu_blob($contents)
{
    global $set_theme;
    $chemin = "../";

    echo "
<TABLE BORDER=0 CELLPADDING=0 CELLSPACING=0>
<TR>
<TD width=9><IMG SRC=$chemin/obminclude/themes/$set_theme/kick-ctl.gif WIDTH=9 HEIGHT=10 BORDER=0><BR></TD>
<TD colspan=2 BACKGROUND=$chemin/obminclude/themes/$set_theme/kick-bt.gif><IMG SRC=$chemin/obminclude/themes/$set_theme/spacer.gif WIDTH=1 HEIGHT=10 BORDER=0><BR></TD>
<TD width=10><IMG SRC=$chemin/obminclude/themes/$set_theme/kick-ctr.gif WIDTH=10 HEIGHT=10 BORDER=0><BR></TD>
</TR>
<TR VALIGN=top>
<TD BACKGROUND=$chemin/obminclude/themes/$set_theme/kick-bl.gif width=9><IMG SRC=$chemin/obminclude/themes/$set_theme/spacer.gif WIDTH=9 HEIGHT=1 BORDER=0><BR></TD>
<TD BGCOLOR=#EFEF52 width=1><FONT FACE=tahoma, verdana, arial, helvetica, sans-serif SIZE=2>$contents</FONT></TD>
<TD BACKGROUND=$chemin/obminclude/themes/$set_theme/kick-bg.gif width=1 VALIGN=top><BR></TD>
<TD BACKGROUND=$chemin/obminclude/themes/$set_theme/kick-br.gif width=10><IMG SRC=$chemin/obminclude/themes/$set_theme/gifs/spacer.gif WIDTH=10 HEIGHT=1 BORDER=0><BR></TD>
</TR>
<TR VALIGN=top><TD width=9><IMG SRC=$chemin/obminclude/themes/$set_theme/kick-cbl.gif WIDTH=9 HEIGHT=10 BORDER=0><BR></TD>
<TD colspan=2 BACKGROUND=$chemin/obminclude/themes/$set_theme/kick-bb.gif><IMG SRC=$chemin/obminclude/themes/$set_theme/spacer.gif WIDTH=1 HEIGHT=10 BORDER=0><BR></TD>
<TD width=10><IMG SRC=$chemin/obminclude/themes/$set_theme/kick-cbr.gif WIDTH=10 HEIGHT=10 BORDER=0><BR></TD>
</TR>
</TABLE>

    ";
}


///////////////////////////////////////////////////////////////////////////////
// Fonction qui genere l'en-tete (bandeau) de l'application                  //
// La variable globale $menu est utilisee pour savoir dans quelle partie     //
// de l'application on se trouve (societe, contact,...)                      //
// Cela permet de determiner les chemins relatifs, et la partie specifique   //
// de l'entete.                                                              //
///////////////////////////////////////////////////////////////////////////////
// Modified by Frodo (mwalker@mandrakesoft.com) for altername layout         //
///////////////////////////////////////////////////////////////////////////////
function generate_menu($menu) {
  global $set_lang, $set_theme, $set_lang_default, $set_theme_default, $my_id, $db_id, $dbname, $obm_version;
  global $perms_user, $perms_editor, $perms_admin;
  global $auth, $sess;

  if ($menu == "")
    $chemin = "";
  else
    $chemin = "../";

  $lang_file = "obminclude/lang/$set_lang/header.inc";
  if (file_exists($chemin.$lang_file)) {
    include ($lang_file);
  } else {
    include ("obminclude/lang/$set_lang_default/header.inc");
  }

  $theme_file = "obminclude/themes/$set_theme/theme.inc";
  if (file_exists($chemin.$theme_file)) {
    include ($theme_file);
  } else {
    include ("obminclude/themes/$set_theme_default/theme.inc");
  }

if($www_site=="") $www_site="www.aliacom.fr";
{
  echo "<TABLE>
            <TR>
                <TD>
                    <A HREF=\"http://$www_site/\"><IMG align=middle border=0 SRC=\"".$chemin."obminclude/themes/$set_theme/$ico_principal\"></A>
                </TD>
                <TD VALIGN=top>
                    <TABLE BORDER=0 WIDTH=100%>
                        <TR>
                            <TD VALIGN=top>";

  // --------------------------------------------------------------------------
  // COMPANY Submenu
  // --------------------------------------------------------------------------
  if ($menu == "COMPANY") {
    if ($auth->auth["perm"] != $perms_user) {
                                menu_blob("$l_header_company<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("company_index.php?action=new")."\" target=_top>".$l_header_new_f."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("company_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("company_index.php?action=admin")."\" target=_top>".$l_header_admin."</A>&nbsp;<BR>");
    } else {
                                menu_blob("$l_header_company<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new_f<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("company_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
    };
  } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."company/company_index.php?action=index")."\" target=_top>$l_header_company</A>");
  };

  echo "                    </TD>
                            <TD VALIGN=top>";

  // --------------------------------------------------------------------------
  // CONTACT Submenu
  // --------------------------------------------------------------------------
  if ($menu == "CONTACT") {
    if ($auth->auth["perm"] != $perms_user) {
                                menu_blob("$l_header_contact<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contact_index.php?action=new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contact_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contact_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
    } else {
                                menu_blob("$l_header_contact<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contact_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
    };
  } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."contact/contact_index.php?action=index")."\" target=_top>$l_header_contact</A>");
  };

  echo "                    </TD>
                            <TD VALIGN=top>";

  // --------------------------------------------------------------------------
  // DEAL Submenu
  // --------------------------------------------------------------------------
//  if ($auth->auth["perm"] == $perms_admin) {
      if ($menu == "DEAL") {
        if ($auth->auth["perm"] != $perms_user) {
                                    menu_blob("$l_header_deal<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=company_new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=parent_new")."\" target=_top>".$l_header_new_parent . "</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=index_parent")."\" target=_top>".$l_header_find_parent . "</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
        
	
	} else {
                                    menu_blob("$l_header_deal<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("deal_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>
				    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new_parent<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp; <A HREF=\"".
     $sess->url("deal_index.php?action=index_parent")."\" target=_top>".$l_header_find_parent . "</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;");

        };
      } else {
                                    menu_blob("<A HREF=\"".
     $sess->url($chemin."deal/deal_index.php?action=index")."\" target=_top>$l_header_deal</A>");
      };
//    } else {
//                                    menu_blob("$l_header_deal<BR>");
//    };

  echo "                    </TD>
                            <TD VALIGN=top>";
  // --------------------------------------------------------------------------
  // LIST Submenu
  // --------------------------------------------------------------------------
  
  if ($menu == "LIST") {
    if ($auth->auth["perm"] != $perms_user) {
      menu_blob("$l_header_list<BR>
            &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".$sess->url("list_index.php?action=new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
            &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".$sess->url("list_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
            &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".$sess->url("list_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
    } else {
      menu_blob("$l_header_deal<BR>
                 &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                 &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".$sess->url("list_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                 &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
    }
  } else {
    menu_blob("<A HREF=\"".$sess->url($chemin."list/list_index.php?action=index")."\" target=_top>$l_header_list</A>");
  };
//    } else {
//                                    menu_blob("$l_header_deal<BR>");
//    };

  echo "                    </TD>
                            <TD VALIGN=top>";



  // --------------------------------------------------------------------------
  // CALL Submenu
  // --------------------------------------------------------------------------
  if ($menu == "CALL") {
    if ($auth->auth["perm"] != $perms_user) {
                                menu_blob("$l_header_call<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("call_index.php?action=company_new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("call_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("call_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
    } else {
                                menu_blob("$l_header_call<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("call_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
    };
  } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."call/call_index.php?action=index")."\" target=_top>$l_header_call</A>");
  };

  echo "                    </TD>
                            <TD VALIGN=top>";
  // --------------------------------------------------------------------------
  // DEAL Submenu
  // --------------------------------------------------------------------------
  //  if ($auth->auth["perm"] == $perms_admin) {
  if ($menu == "CRV") {
        if ($auth->auth["perm"] != $perms_user) {
                                    menu_blob("$l_header_crv<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("crv_index.php?action=company_new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("crv_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("crv_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
        } else {
                                    menu_blob("$l_header_crv<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("crv_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
        };
      } else {
                                    menu_blob("<A HREF=\"".
     $sess->url($chemin."crv/crv_index.php?action=index")."\" target=_top>$l_header_crv</A>");
      };
//    } else {
//                                    menu_blob("$l_header_deal<BR>");
//    };

  echo "                    </TD>
                            <TD VALIGN=top>";

  // --------------------------------------------------------------------------
  // USER Submenu
  // --------------------------------------------------------------------------
  if ($auth->auth["perm"] == $perms_admin) {
    if ($menu == "USER") {
                                menu_blob("$l_header_user<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("user_index.php?action=new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("user_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("user_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
    } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."user/user_index.php?action=index")."\" target=_top>$l_header_user</A>");
    };
  } else {
                                menu_blob("$l_header_user<BR>");
  };


  echo "</TD><TD valign=top>";

  // --------------------------------------------------------------------------
  // CONTRACT  Submenu 
  // --------------------------------------------------------------------------
  if ($menu == "CONTRACT") {
    if ($auth->auth["perm"] != $perms_user) {
                                menu_blob("$l_header_contract<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=company_new")."\" target=_top>".$l_header_new_f."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=admin")."\" target=_top>".$l_header_admin."</A>&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=display")."\" target=_top>".$l_header_display."</A>
");
    } else {
                                menu_blob("$l_header_company<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new_f<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("contract_index.php?action=display")."\" target=_top>".$l_header_display."</A>
");
    };
  } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."contract/contract_index.php?action=index")."\" target=_top>$l_header_contract</A>");
  };

  echo "                    </TD>
                            <TD VALIGN=top>";

  // --------------------------------------------------------------------------
  // INCIDENT  Submenu
  // --------------------------------------------------------------------------
  if ($menu == "INCIDENT") {
    if ($auth->auth["perm"] != $perms_user) {
                                menu_blob("$l_header_incident<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("incident_index.php?action=contract_new")."\" target=_top>".$l_header_new_f."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("incident_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("incident_index.php?action=display")."\" target=_top>".$l_header_display."</A>
");
    } else {
                                menu_blob("$l_header_company<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new_f<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("incident_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>
                                &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("incident_index.php?action=display")."\" target=_top>".$l_header_display."</A>
");
    };
  } else {
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."incident/incident_index.php?action=index")."\" target=_top>$l_header_incident</A>");
  };

  echo "                    </TD>
                            <TD VALIGN=top>";


/////////////////////////////////////////////////////////////////////////////
  // Computer Submenu                                                            //
  /////////////////////////////////////////////////////////////////////////////
  if ($menu == "COMPUTER") {
        if ($auth->auth["perm"] != $perms_user) {
                                    menu_blob("$l_header_computer<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("computer_index.php?action=new")."\" target=_top>".$l_header_new."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("computer_index.php?action=index")."\" target=_top>".$l_header_find."</A>&nbsp;<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("computer_index.php?action=admin")."\" target=_top>".$l_header_admin . "</A>&nbsp;<BR>");
        } else {
                                    menu_blob("$l_header_computer<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_new<BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;<A HREF=\"".
     $sess->url("computer_index.php?action=index")."\" target=_top>".$l_header_find."</A><BR>
                                    &nbsp;&nbsp;<IMG SRC=$chemin/obminclude/themes/$set_theme/star.gif>&nbsp;$l_header_admin&nbsp;<BR>");
        };
      } else {
                                    menu_blob("<A HREF=\"".
     $sess->url($chemin."computer/computer_index.php?action=index")."\" target=_top>$l_header_computer</A>");
      };
   
//    } else {
//                                    menu_blob("$l_header_deal<BR>");
//    };



  echo "                    </TD>
                            <TD VALIGN=top>";
                               menu_blob("<A HREF=\"".
     $sess->url($chemin."settings/settings_index.php")."\" TARGET=_top>$l_header_settings</A>");
  echo"                     </TD>
                            <TD VALIGN=top>";

                            if ($auth->auth["perm"] == $perms_admin) {
                                menu_blob("<A HREF=\"" .
     $sess->url( $chemin . "admin/admin_index.php")."\"" . "TARGET=\"_top\">".$l_header_admin."</A>");
                            } else {
                                menu_blob("$l_header_admin");
                            };

  echo "                    </TD>
                            <TD VALIGN=top>";
                                menu_blob("<A HREF=\"".
     $sess->url($chemin."obm.php?action=logout")."\">".$auth->auth["uname"]." Logout</A>");
  echo "                    </TD>
                        </TR>
                    </TABLE>
                  </TD>";
																     
  echo " </TR>
        </TABLE><BR><HR><BR>";

};




}

