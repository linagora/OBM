<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_display.inc                                          //
//     - Desc : Company Display File                                         //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
 

///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
// Parameters:
//   - $kind_q    : database object with kind list
//   - $company[] : default form values
//     keys used  : state, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form ($kind_q, $company) {
  global $l_company_name, $l_company_kind, $l_postcode, $l_internal, $l_phone;
  global $l_all, $l_find; 
  global $c_all, $sess;

  $state = $company["state"];
  $name = stripslashes($company["name"]);
  $phone = stripslashes($company["phone"]);
  $kind = $company["kind"];
  $zip = stripslashes($company["zip"]);

  echo "<CENTER>
    <FORM METHOD=GET NAME=f_societe
      ACTION=\"" . $sess->url("company_index.php") . "\">
    <TABLE BORDER=0>
    <TR>
      <TD><FONT SIZE=-2>$l_company_name</FONT><BR>
        <INPUT TYPE=text NAME=tf_name size=20 value=\"$name\">
      <TD>&nbsp;&nbsp;</TD>
      <TD><FONT SIZE=-2>$l_phone</FONT><BR>
        <INPUT TYPE=text NAME=tf_phone size=14 value=\"$phone\"></TD>
      <TD>&nbsp;&nbsp;</TD>
      <TD><FONT size=-2>$l_company_kind</FONT><BR>
        <SELECT name=\"sel_kind\">\n<OPTION value=\"$c_all\">$l_all";
  while ($kind_q->next_record()) {
    $id = $kind_q->f("companytype_id");
    echo "\n<OPTION value=\"$id\"";
    if ($kind == $id) echo " selected";
    echo ">". $kind_q->f("companytype_label");
  }
  echo "</SELECT>
      </TD>
      <TD><FONT SIZE=-2>$l_postcode</FONT><BR>
        <INPUT TYPE=text NAME=tf_zip size=6 value=\"$zip\"></TD>
      <TD>&nbsp;&nbsp;</TD>
      <TD align=center><FONT size=-2>$l_internal</FONT><br>
        <INPUT TYPE=\"checkbox\" NAME=\"cb_state\" value=\"1\" ";
  if ($state == '1') echo " checked";
  echo "></TD>
      <TD align=center>&nbsp;&nbsp;&nbsp;&nbsp;
        <INPUT NAME=\"action\" TYPE=\"hidden\" VALUE=\"search\">
        <INPUT NAME=\"submit\" TYPE=\"submit\" VALUE=\"$l_find\">
      </TD>
    </TR>
    </TABLE>
    </FORM><HR>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company search result                                         //
// Parameters:
//   - $company[] : company search criteria
//     keys used  : state, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_list($company) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "company");
  $obm_q = run_query_search($company, $new_order, $order_dir);
  $nb_company = $obm_q->num_rows();
  if ($nb_company == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_company_search_list($obm_q,$pref_q,$nb_company,$company);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result                                                //
// Parameters : 
//   - $obm_q_comp : list of companies
//   - $pref_q     : the fields which have to be displayed
//   - $nb_company : nb companies returned by the search query
//   - $company[]  : company search criteria
//     keys used   : state, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_company_search_list($obm_q_comp, $pref_q, $nb_company,$company) {
  global $sess, $col_textem, $l_found;

  // we urlencode to avoid breakage for space char
  $state = $company["state"];
  $name = urlencode(stripslashes($company["name"]));
  $phone = urlencode(stripslashes($company["phone"]));
  $kind = $company["kind"];
  $zip = urlencode(stripslashes($company["zip"]));
  
  echo "<font color=\"#$col_textem\">$nb_company $l_found</font><br>";

  $url = $sess->url("company_index.php?action=search&amp;tf_name=$name&amp;tf_phone=$phone&amp;sel_kind=$kind&amp;tf_zip=$zip&amp;cb_state=$state");

  $obm_q_comp->seek($first_row);

  $dis_companies_list = new OBM_DISPLAY("DATA", $pref_q);
  $dis_companies_list->data_set = $obm_q_comp;
  $dis_companies_list->data_url = $url;
  $dis_companies_list->data_header = "both";

  $dis_companies_list->display();
}


///////////////////////////////////////////////////////////////////////////////
// Display Company links menu                                                //
// Parameters:
//   - $p_id     : company id
//   - $name     : company name
//   - $con_num  : contact number
//   - $deal_num : deal number
///////////////////////////////////////////////////////////////////////////////
function html_company_links($p_id, $name, $con_num, $deal_num) {
  // -- Themes
  global $set_theme,$ico_contact_list,$ico_contact_new;
  // -- Labels
  global $l_contact_list,$l_contact_new,$l_deal_list,$l_deal_new; 
  global $perms_user,$l_perms_user;  
  global $auth, $sess;

  $uname = urlencode($name);

  // -------| Menu Liste Contacts
  echo "<A HREF=\"" . $sess->url("../contact/contact_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company=$uname\"><IMG BORDER=0
          SRC=\"/images/$set_theme/$ico_contact_list\"></A>
        <FONT SIZE=-1><A HREF=\"" . $sess->url("../contact/contact_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company=$uname\">$l_contact_list</A> ($con_num)</FONT>";

  // -------| Menu Nouveau Contact
  if ($auth->auth["perm"] != $perms_user) {
    echo "<BR>
      <A HREF=\"" . $sess->url("../contact/contact_index.php?action=new&amp;param_company=$p_id") . "\"><IMG BORDER=0
        SRC=\"/images/$set_theme/$ico_contact_new\"></A>
      <FONT SIZE=-1><A HREF=\"" . $sess->url("../contact/contact_index.php?action=new&amp;param_company=$p_id") . "\">$l_contact_new</A></FONT>";
  }

  // -------| Menu Liste Affaires
  echo "<BR>
    <A HREF=\"" . $sess->url("../deal/deal_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company_name=$uname\"><IMG BORDER=0
      SRC=\"/images/$set_theme/$ico_contact_list\"></A>
    <FONT SIZE=-1><A HREF=\"".$sess->url("../deal/deal_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company_name=$uname\">$l_deal_list</A> ($deal_num)</FONT>";

  // -------| Menu Nouvelle Affaire
  if ($auth->auth["perm"] != $perms_user) {
    echo "<BR>
      <A HREF=\"".$sess->url("../deal/deal_index.php?action=new&amp;param_company=$p_id") . "\"><IMG BORDER=0
        SRC=\"/images/$set_theme/$ico_contact_new\"></A>
      <FONT SIZE=-1><A HREF=\"".$sess->url("../deal/deal_index.php?action=new&amp;param_company=$p_id") . "\">$l_deal_new</A></FONT>\n";
  }

}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Company Consultation                                         //
// Parameters:
//   - $co_q   : company database result 
//   - $kind_q : kind database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_consult($co_q, $kind_q) {
  // - Themes
  global $set_theme,$col_label;
  global $ico_contact_list,$ico_contact_new,$ico_web,$ico_mail;
  // - Labels
  global $l_contact_list,$l_contact_new,$l_deal_list,$l_deal_new;
  global $l_name,$l_kind,$l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_web,$l_comment,$l_number;
  global $l_insert,$l_update,$l_delete;
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $id = $co_q->f("company_id");
  $num = $co_q->f("company_number");
  $state = $co_q->f("company_state");
  $name = $co_q->f("company_name");
  $kind = $co_q->f("company_type_id");
  $ad1 = $co_q->f("company_address1");
  $ad2 = $co_q->f("company_address2");
  $zip = $co_q->f("company_zipcode");
  $town = $co_q->f("company_town");
  $cdx = $co_q->f("company_expresspostal");
  $ctry = $co_q->f("company_country");
  $phone = $co_q->f("company_phone");
  $fax = $co_q->f("company_fax");
  $web = $co_q->f("company_web");
  $email = $co_q->f("company_email");
  $con_num = $co_q->f("company_contact_number");
  $deal_num = $co_q->f("company_deal_number");
  $com = $co_q->f("company_comment");

  // Kind
  while($kind_q->next_record()) {
    if ($kind_q->f("companytype_id") == $kind)
      $lkind = $kind_q->f("companytype_label");
  }

  // Email link
  if ($email != "") {
    $link_email = "<A HREF=\"mailto:$email\">
      <IMG BORDER=0 SRC=\"/images/$set_theme/$ico_mail\"
      ALT=\"$email\"></A>";
  } else {
    $link_email = "&nbsp;";
  }

  // Web link
  if ($web != "") {
    $link_web = "<A TARGET=_blank HREF=\"";
    if (strcmp(substr($web,0,5), "http:") != 0) { $link_web .= "http://"; }
    $link_web .= "$web\">
      <IMG BORDER=0 SRC=\"/images/$set_theme/$ico_web\"
      ALT=\"$web\"></A>";
  } else {
    $link_web = "&nbsp;";
  }


  echo "<CENTER>
    <TABLE BORDER=0>
    <TR><TD VALIGN=top>\n";

  html_company_links($id, $name, $con_num, $deal_num);

  echo "</TD><TD>
      <TABLE BORDER=0>
      <TR>
        <TD VALIGN=top>
        <TABLE BORDER=0>
        <TR>
  <!-- NUMBER -->
          <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_number
          &nbsp;&nbsp;</FONT></TD>
          <TD>$num</TD>
  <!-- NAME -->
        </TR><TR>
          <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_name
          &nbsp;&nbsp;</FONT></TD>
          <TD>$name</TD>
  <!-- KIND -->
        </TR><TR>
          <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_kind
          &nbsp;&nbsp;</FONT></TD>
          <TD>$lkind</TD>
  <!-- EMPTY -->
      </TR><TR>
        <TD>&nbsp;</TD>
        <TD>&nbsp;</TD>
  <!-- PHONE -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_phone
        &nbsp;</FONT></TD>
        <TD>$phone</TD>
  <!-- FAX -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_fax
        &nbsp;</FONT></TD>
        <TD>$fax</TD>
      </TR>
      </TABLE>

      </TD>
      <TD>&nbsp;&nbsp;</TD>
      <TD VALIGN=top>

      <TABLE BORDER=0>
      <TR>
  <!-- ADDRESS 1 -->
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_address
        &nbsp;1&nbsp;&nbsp;</FONT></TD>
        <TD>$ad1</TD>
  <!-- ADDRESS 2 -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_address
        &nbsp;2&nbsp;&nbsp;</FONT></TD>
        <TD>$ad2</TD>
  <!-- ZIP CODE + Express POSTAL -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_postcode
        &nbsp;&nbsp;</FONT></TD>
        <TD>$zip &nbsp;&nbsp;&nbsp;
        <FONT COLOR=\"#$col_label\" SIZE=-2>$l_expresspostal</FONT> &nbsp;
        $cdx</TD>
  <!-- TOWN -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_town
        &nbsp;&nbsp;</FONT></TD>
        <TD>$town</TD>
  <!-- COUNTRY -->
      </TR><TR>
        <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_country
        &nbsp;&nbsp;</FONT></TD>
        <TD>$ctry</TD>
      </TR>
      </TABLE>

      </TD>
    </TR>
    </TABLE>

    <TABLE BORDER=0>

  <!-- EMAIL + LINK -->
    <TR>
      <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_email
      &nbsp;&nbsp;</FONT></TD>
      <TD>$email</TD>
      <TD ALIGN=center>$link_email</TD>
  <!-- WEB + LINK -->
    </TR><TR>
      <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_web
      &nbsp;&nbsp;</FONT></TD>
      <TD>$web</TD>
      <TD ALIGN=center>$link_web</TD>
    </TR>
    </TABLE>

  <!-- COMMENTS -->
    <P><FONT COLOR=\"#$col_label\" SIZE=-2>$l_comment</FONT><BR>" .
    nl2br($com);


  if ($auth->auth["perm"] != $perms_user) {
    // -------| BITBTN - Update Mode 
    echo "<FORM METHOD=GET NAME=f_mod_societe ACTION=\"".$sess->url("company_index.php")."\">
      <INPUT TYPE=hidden NAME=param_company VALUE=\"$id\">
      <INPUT TYPE=\"hidden\" name=\"action\" value=\"detailupdate\">
      <INPUT TYPE=\"submit\" value=\"$l_update\">
      </FORM>";
  }

  echo "</TD></TR></TABLE>
   </CENTER>";

}


///////////////////////////////////////////////////////////////////////////////
// Display Company Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : company database result 
//   - $kind_q    : kind database result 
//   - $company[] : default values
//     keys used  : id, num, state, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, mailing (deprecated) ,com
///////////////////////////////////////////////////////////////////////////////
function html_company_form($action, $co_q, $kind_q, $company) {
  // -- Themes
  global $set_theme,$ico_web,$ico_mail;
  // -- Labels
  global $l_name,$l_kind,$l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_web,$l_comment,$l_number;
  global $l_insert,$l_update,$l_checkdelete,$l_internal;
  global $sess;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("company_id");
    $num = $co_q->f("company_number");
    $state = $co_q->f("company_state");
    $name = $co_q->f("company_name");
    $kind = $co_q->f("company_type_id");
    $ad1 = $co_q->f("company_address1");
    $ad2 = $co_q->f("company_address2");
    $zip = $co_q->f("company_zipcode");
    $town = $co_q->f("company_town");
    $cdx = $co_q->f("company_expresspostal");
    $ctry = $co_q->f("company_country");
    $phone = $co_q->f("company_phone");
    $fax = $co_q->f("company_fax");
    $web = $co_q->f("company_web");
    $email = $co_q->f("company_email");
    $con_num = $co_q->f("company_contact_number");
    $deal_num = $co_q->f("company_deal_number");
    $com = $co_q->f("company_comment");
  } else {
    // Values are taken from parameters ($company hash)
    if (isset($company["id"])) { $id = $company["id"]; }
    $num = stripslashes($company["num"]);
    $state = $company["state"];
    $name = stripslashes($company["name"]);
    $kind = $company["kind"];
    $ad1 = stripslashes($company["ad1"]);
    $ad2 = stripslashes($company["ad2"]);
    $zip = stripslashes($company["zip"]);
    $town = stripslashes($company["town"]);
    $cdx = stripslashes($company["cdx"]);
    $ctry = stripslashes($company["ctry"]);
    $phone = stripslashes($company["phone"]);
    $fax = stripslashes($company["fax"]);
    $web = stripslashes($company["web"]);
    $email = stripslashes($company["email"]);
    $com = stripslashes($company["com"]);
  }

  // kind select
  $sel_kind = "<SELECT name=sel_kind>";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("companytype_id");
    $sel_kind .= "\n<OPTION value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected"; }
    $sel_kind .= ">". $kind_q->f("companytype_label") . "\n";
  }
  $sel_kind .= "</SELECT>";

  // cb state state
  if ($state == 1) $lstate = " checked";

  // Email link
  if (($action=="detailupdate") || ($action=="update")) { 
    if ($email != "") {
      $link_email = "<A HREF=\"mailto:$email\">
        <IMG BORDER=0 SRC=\"/images/$set_theme/$ico_mail\"
        ALT=\"$email\"></A>";
    } else { $link_email = "&nbsp;"; }
  } else { $link_email = "&nbsp;"; }

  // Web link
  if (($action=="detailupdate") || ($action=="update")) {
    if ($web != "") {
      $link_web = "<A TARGET=_blank HREF=\"";
      if (strcmp(substr($web,0,5), "http:") != 0) { $link_web .= "http://"; }
      $link_web .= $web . "\">
        <IMG BORDER=0 SRC=\"/images/$set_theme/$ico_web\"
        ALT=\"$web\"></A>";
    } else { $link_web = "&nbsp;"; }
  } else { $link_web = "&nbsp;"; }

  echo "<CENTER>
    <FORM METHOD=POST NAME=f_mod_societe
      onSubmit=\"if (check_company(this)) return true; else return false;\"
      ACTION=\"".$sess->url("company_index.php")."\">

    <TABLE BORDER=0>
      <TR><TD VALIGN=top>\n";
  if (($action == "detailupdate") || ($action == "update")) {
    html_company_links($id, $name, $con_num, $deal_num);
  }

  echo "</TD><TD>
      <TABLE BORDER=0>
      <TR>
        <TD VALIGN=top>
        <TABLE BORDER=0>
  <!-- NAME -->
        <TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_name &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_name size=30 value=\"$name\"></TD>
  <!-- NUMBER -->
        </TR><TR>
          <TD><FONT size=-2>$l_number</FONT></TD>
          <TD><INPUT NAME=tf_num value=\"$num\" size=30 maxlength=32></TD>
  <!-- COMPANY KIND -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_kind &nbsp;&nbsp;</FONT></TD>
          <TD>$sel_kind</TD>
  <!-- STATE -->
        </TR><TR>
          <TD><FONT SIZE=-2>$l_internal</FONT></TD>
          <TD><INPUT TYPE=\"checkbox\" NAME=\"cb_state\" value=1 $lstate></TD>
  <!-- PHONE -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_phone &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_phone size=16 value=\"$phone\"></TD>
  <!-- FAX -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_fax &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_fax size=16 value=\"$fax\"></TD>
        </TR>
        </TABLE>

        </TD>
        <TD>&nbsp;&nbsp;</TD>
        <TD VALIGN=top>

        <TABLE BORDER=0>
  <!-- ADDRESS 1 -->
        <TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_address
          &nbsp;&nbsp;<BR>1&nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_ad1 size=30 value=\"$ad1\"></TD>
  <!-- ADDRESSE 2 -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_address
          &nbsp;&nbsp;<BR>1&nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_ad2 size=30 value=\"$ad2\"></TD>
  <!-- ZIP CODE + CEDEX -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_postcode &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_zip size=6 value=\"$zip\">
          &nbsp;&nbsp;&nbsp; <FONT SIZE=-2>$l_expresspostal</FONT> &nbsp;
          <INPUT NAME=tf_cdx size=8 value=\"$cdx\"></TD>
  <!-- TOWN -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_town &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_town size=24 value=\"$town\"></TD>
  <!-- COUNTRY -->
        </TR><TR>
          <TD ALIGN=right><FONT SIZE=-2>$l_country &nbsp;&nbsp;</FONT></TD>
          <TD><INPUT NAME=tf_ctry size=20 value=\"$ctry\"></TD>
        </TR>
        </TABLE>
        </TD>
      </TR>
      </TABLE>

      <TABLE BORDER=0>
      <TR>
  <!-- EMAIL + LINK -->
        <TD ALIGN=right><FONT SIZE=-2>$l_email &nbsp;&nbsp;</FONT></TD>
        <TD><INPUT NAME=tf_email size=45 value=\"$email\"></TD>
        <TD ALIGN=center>$link_email </TD>
      </TR><TR>
  <! -- WEB + LINK -->
        <TD ALIGN=right><FONT SIZE=-2>$l_web &nbsp;&nbsp;</FONT></TD>
        <TD><INPUT NAME=tf_web size=45 value=\"$web\"></TD>
        <TD ALIGN=center>$link_web</TD>
      </TR>
      </TABLE>

      <TABLE BORDER=0>
      <TR>

  <!-- COMMENT -->
        <TD><FONT SIZE=-2>$l_comment &nbsp;&nbsp;</FONT><BR>
          <TEXTAREA NAME=ta_com ROWS=6 COLS=72>$com</TEXTAREA></TD>
      </TR><TR>
        <TD ALIGN=center>
        <TABLE border=0>
        <TR>
          <TD>\n";
  
  if (($action == "detailupdate") || ($action == "update")) {
    // -------| BITBTN 1 - VALIDER LES CHANGEMENTS
    echo "<INPUT TYPE=hidden NAME=param_company VALUE=\"$id\">
      <INPUT TYPE=\"hidden\" name=\"action\" value=\"update\">
      <INPUT TYPE=\"submit\" value=\"$l_update\"> 
      </FORM></TD>\n";

    // -------| BITBTN 2 - SUPPRMIER LA SOCIETE
    echo "<TD><FORM METHOD=POST NAME=f_sup_societe
      onSubmit=\"if (valider_suppression()) return true; else return false;\"
      ACTION=\"" . $sess->url("company_index.php") . "\">"; 
    echo "<INPUT TYPE=hidden name=action value=check_delete>
      <INPUT TYPE=hidden NAME=param_company VALUE=\"$id\">
      <INPUT TYPE=submit value=\"$l_checkdelete\">";
  } elseif (($action == "new") || ($action == "insert")) {
    echo "<INPUT TYPE=hidden name=\"action\" value=\"insert\">
      <INPUT TYPE=submit value=\"$l_insert\">";
  }

  echo "</FORM>
          </TD>
        </TR>
        </TABLE>
        </TD>
      </TR>
      </TABLE>
      </TD>
    </TR>
    </TABLE>
  </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the company links (and delete form if ok)              //
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $sess;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_company_contact_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    echo "$l_link_contact";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname") . " " . $obm_q->f("contact_firstname");
      echo "<BR><A HREF=\"" . $sess->url("../contact/contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</A>\n";
    }
    echo "<P>";
  } else {
    echo  $l_link_contact_no . "<P>";
  }

  // Links from deals
  $obm_q = run_query_company_deal_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    echo "$l_link_deal";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      echo "<BR><A HREF=\"" .$sess->url("../deal/deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</A>\n";
    }
    echo "<P>";
  } else {
    echo  $l_link_deal_no . "<P>";
  }

  if ($delete_ok == true) {
    display_ok_msg($l_can_delete);
    echo "<TABLE><TR><TD>";
    dis_delete_form($p_id);
    echo "</TD>";
  } else {
    echo "<P>";
    display_warn_msg($l_cant_delete);
    echo "<TABLE><TR>";
  }

  echo "<TD>";
  echo "<FORM NAME=form_back METHOD=POST
   ACTION=\"" . $sess->url("company_index.php") ."\">
   <INPUT TYPE=hidden NAME=action VALUE=detailupdate>
   <INPUT TYPE=hidden NAME=param_company VALUE=\"$p_id\">
   <INPUT TYPE=submit VALUE=\"$l_back\">
   </FORM>";
  echo "</TR></TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a company insertion or update                   //
// When similar companies exists we show these and ask confirmation
// Parameters:
//   - $cid       : company id
//   - $co_q      : company database result (at least 1 row)
//   - $company[] : default values
//     keys used  : num, state, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, mailing (deprecated), com
/////////////////////////////////////////////////////////////////////////////
function dis_company_warn_insert($cid, $co_q, $company) {
  global $l_check_samecompany, $l_confirm, $l_back;
  global $sess, $c_yes, $c_no;

  $num = $company["num"];
  $state = $company["state"];
  $name = $company["name"];
  $kind = $company["kind"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["ctry"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["com"];

  echo $l_check_samecompany;
  while ($co_q->next_record()) {
    $id = $co_q->f("company_id");
    $samename = $co_q->f("company_name");
    $samezip = $co_q->f("company_zip");
    echo "<BR><A HREF=\"" .$sess->url("company_index.php?action=detailconsult&amp;param_company=$id") . "\">$samename ($samezip)</A>";
  }

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=POST NAME=form_insert
    ACTION=\"" .$sess->url("company_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=insert>
    <INPUT TYPE=hidden NAME=hd_confirm VALUE=\"$c_yes\">
    <INPUT TYPE=hidden NAME=tf_num VALUE=\"$num\">
    <INPUT TYPE=hidden NAME=cb_state VALUE=\"$state\">
    <INPUT TYPE=hidden NAME=tf_name VALUE=\"$name\">
    <INPUT TYPE=hidden NAME=sel_kind VALUE=\"$kind\">
    <INPUT TYPE=hidden NAME=tf_ad1 VALUE=\"$ad1\">
    <INPUT TYPE=hidden NAME=tf_ad2 VALUE=\"$ad2\">
    <INPUT TYPE=hidden NAME=tf_zip VALUE=\"$zip\">
    <INPUT TYPE=hidden NAME=tf_town VALUE=\"$town\">
    <INPUT TYPE=hidden NAME=tf_cdx VALUE=\"$cdx\">
    <INPUT TYPE=hidden NAME=tf_ctry VALUE=\"$ctry\">
    <INPUT TYPE=hidden NAME=tf_phone VALUE=\"$phone\">
    <INPUT TYPE=hidden NAME=tf_fax VALUE=\"$fax\">
    <INPUT TYPE=hidden NAME=tf_web VALUE=\"$web\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <INPUT TYPE=hidden NAME=sel_mailing VALUE=\"$mailing\">
    <INPUT TYPE=hidden NAME=ta_com VALUE=\"$com\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_confirm\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=POST
    ACTION=\"" .$sess->url("company_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=new>
    <INPUT TYPE=hidden NAME=tf_num VALUE=\"$num\">
    <INPUT TYPE=hidden NAME=cb_state VALUE=\"$state\">
    <INPUT TYPE=hidden NAME=tf_name VALUE=\"$name\">
    <INPUT TYPE=hidden NAME=sel_kind VALUE=\"$kind\">
    <INPUT TYPE=hidden NAME=tf_ad1 VALUE=\"$ad1\">
    <INPUT TYPE=hidden NAME=tf_ad2 VALUE=\"$ad2\">
    <INPUT TYPE=hidden NAME=tf_zip VALUE=\"$zip\">
    <INPUT TYPE=hidden NAME=tf_town VALUE=\"$town\">
    <INPUT TYPE=hidden NAME=tf_cdx VALUE=\"$cdx\">
    <INPUT TYPE=hidden NAME=tf_ctry VALUE=\"$ctry\">
    <INPUT TYPE=hidden NAME=tf_phone VALUE=\"$phone\">
    <INPUT TYPE=hidden NAME=tf_fax VALUE=\"$fax\">
    <INPUT TYPE=hidden NAME=tf_web VALUE=\"$web\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <INPUT TYPE=hidden NAME=sel_mailing VALUE=\"$mailing\">
    <INPUT TYPE=hidden NAME=ta_com VALUE=\"$com\">
    <INPUT TYPE=submit VALUE=\"$l_back\">
    </FORM>
    </TD></TR>
    </TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;
  global $sess;

  echo "\n<FORM name=\"form_delete\" " .
      "METHOD=post action=\"" . $sess->url("company_index.php") . "\">
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"delete\">
    <INPUT TYPE=\"hidden\" name=\"hd_company_id\" value=\"$p_id\">
    <INPUT TYPE=\"submit\" value=\"$l_delete\"
    onClick=\"if (confirm_del(this.form)) return true; else return false;\">";
  echo "</FORM>\n";
}


///////////////////////////////////////////////////////////////////////////////
// Company Kind section                                                      //
// Parameters:
//   - $kind_q : Kind list database object
///////////////////////////////////////////////////////////////////////////////
function html_company_kind_form($kind_q) {
  global $col_a_table, $col_a_tableh, $col_textem;
  global $l_kind_manage,$l_kind_exist;
  global $l_kind_checkdelete,$l_kind_update,$l_kind_new,$l_kind_insert;
  global $sess;

  echo "<CENTER>
    <FORM name=\"form_admin_kind_delete\"
      METHOD=post action=\"" . $sess->url("company_index.php") . "\">
    <TABLE WIDTH=90% border=1 bgcolor=\"#$col_a_table\">
    <TR>
      <TD colspan=5 BGCOLOR=\"#$col_a_tableh\">
        <I><FONT COLOR=\"#$col_textem\">$l_kind_manage
        </FONT></I>
      </TD>
    </TR><TR>
      <TD>

<!-- Kind Check Delete Section -->

        <TABLE border=0>
        <TR>
          <TD>$l_kind_exist</TD>
          <TD><SELECT NAME=\"sel_kind\" SIZE=4 >";

  while ($kind_q->next_record()) {
    $label = $kind_q->f("companytype_label");
     echo "\n<OPTION value=\"$label\">$label</OPTION>";
  }
  echo "</SELECT></TD>
          <TD>
            <INPUT TYPE=hidden name=\"action\" value=\"kind_checklink\">
            <INPUT TYPE=submit name=sub_kind value=\"$l_kind_checkdelete\"
              onClick=\"if (check_kind_checkdel(this.form)) return true;
              else return false;\">
           </TD>
        </TR>
    </FORM>

<!-- Kind Update Section -->

    <FORM name=\"form_admin_kind_update\"
      METHOD=post action=\"" . $sess->url("company_index.php") . "\">
        <TR>
          <TD colspan=2 align=center>
            <INPUT TYPE=hidden name=sel_kind value=\"\">
            <INPUT TYPE=hidden name=action value=\"kind_update\">
            <INPUT TYPE=text name=tf_kind_upd>
          </TD><TD>
            <INPUT TYPE=submit name=sub_kind value=\"$l_kind_update\"
              onClick=\"if (check_kind_upd(this.form,document.form_admin_kind_delete)) return true; else return false;\">";

  // End of the company kind management left cell
  echo "</TD>
        </TR>
        </TABLE>
      </TD>
      <TD>
    </FORM>

<!-- New Kind Section -->

    <FORM name=\"form_admin_kind_new\"
      METHOD=post action=\"" . $sess->url("company_index.php") . "\">
      <TABLE>
      <TR>
        <TD align=right>$l_kind_new : <INPUT name=\"tf_kind_new\"></TD>
      </TR><TR>
        <TD align=center>
          <INPUT TYPE=hidden name=action value=\"kind_insert\">
          <INPUT type=submit name=sub_kind value=\"$l_kind_insert\"
            onClick=\"if (check_kind_new(this.form)) return true; else return false;\">
        </TD>
      </TR>
      </TABLE>
      </TD>
    </TR>
    </TABLE>
    </FORM>
  </CENTER>";

}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind delete form (button)                                    //
// Parameters:
//   - $label : kind label
///////////////////////////////////////////////////////////////////////////////
function dis_kind_delete_form($label) {
  global $l_kind_delete;
  global $sess;

  echo "\n<FORM name=\"form_kind_delete\" " .
      "METHOD=post action=\"" . $sess->url("company_index.php") . "\">
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"kind_delete\">
    <INPUT TYPE=\"hidden\" name=\"hd_kind_label\" value=\"$label\">
    <INPUT TYPE=\"submit\" name=\"sub_kind\" value=\"$l_kind_delete\">
    </FORM>\n";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind links                                                   //
// Parameters:
//   - $label : kind label
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($label) {
  global $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_can_delete, $l_kind_cant_delete;
  global $sess, $l_back;

  $id = get_kind_id($label);
  $obm_q = run_query_kind_links($id);

  if ($obm_q->num_rows() > 0) {
    echo "$l_kind_link_company $label";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      echo "<BR><A HREF=\"" .$sess->url("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</A>\n";
    }
    echo "<P>$label : $l_kind_cant_delete";
  } else {
    echo "$l_kind_link_company_no '$label' : $l_kind_can_delete";
    echo "<TABLE><TR><TD>";
    dis_kind_delete_form($label);
    echo "</TD><TD>";
    echo "<FORM NAME=form_back METHOD=POST
     ACTION=\"" .$sess->url("company_index.php") . "\">
     <INPUT TYPE=hidden NAME=action VALUE=admin>
     <INPUT TYPE=submit VALUE=\"$l_back\">
     </FORM>";
    echo "</TR></TABLE>";
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company Display preference screen                             //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_company_display_pref($display_pref_q) {
  global $l_company_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "company"; 
  $dis_pref->pref_title = $l_company_display;
  $dis_pref->pref_dis_help = 1;

  echo "<center>";
  $dis_pref->display();
  echo "<br></center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Select Form                                               //
// Parameters:
//   - $comp_q : company database result
//   - $title  : Title to be displayed
//   - $url    : URL to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_company($comp_q, $title, $url="") {
  global $l_select_company;
  global $sess;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }

  echo "<CENTER>
    <FORM METHOD=GET NAME=form_comp
          onSubmit=\"if ($jsfunc) return true; else return false;\"
          ACTION=\"\">";

  echo "$title
    <P><font size=2 face=Courier>
    <SELECT name=\"param_company\" size=10>";

  while($comp_q->next_record()) {
    $id = $comp_q->f("company_id");
    $name = $comp_q->f("company_name");
    $name = str_pad($name, 40, ".");
    $town = $comp_q->f("company_town");
    echo "\n<OPTION value=\"$id\">$name | $town";
  }

  echo "</SELECT></font>
    <P>
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"new\">
    <INPUT TYPE=submit value=\"$l_select_company\">
    </FORM>
    </CENTER>";
}


</SCRIPT>
