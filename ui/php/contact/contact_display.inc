<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contact_display.php                                          //
//     - Desc : Contact Display File                                         //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
// Parameters:
//   - $contact[] : default form values
//     keys used  : name, company
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_form ($contact) {
  global $l_contact_name, $l_phone, $l_from_company, $l_find;
  global $sess, $popup;

  $lname = stripslashes($contact["lname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  if ($popup) {
    $ext_action = $contact["ext_action"];
    $ext_url = $contact["ext_url"];
    $ext_id = $contact["ext_id"];
    $ext = "<INPUT NAME=ext_action TYPE=hidden VALUE=\"$ext_action\">
      <INPUT NAME=ext_id TYPE=hidden VALUE=\"$ext_id\">
      <INPUT NAME=ext_url TYPE=hidden VALUE=\"$ext_url\">";
  }

  echo "<CENTER>
     <FORM METHOD=GET NAME=f_contact ACTION=\"" .
     $sess->url("contact_index.php") . "\">
     <TABLE BORDER=0><TR>
       <TD><FONT SIZE=-2>$l_contact_name</FONT><BR>
         <INPUT TYPE=text NAME=tf_lname size=16 value=\"$lname\"></TD>
       <TD>&nbsp;&nbsp;</TD>
       <TD><FONT SIZE=-2>$l_phone</FONT><BR>
         <INPUT TYPE=text NAME=tf_phone size=14 value=\"$phone\"></TD>
       <TD>&nbsp;&nbsp;</TD>
       <TD align=left><FONT SIZE=-2> $l_from_company </FONT><BR>
         <INPUT TYPE=text NAME=tf_company size=16 value=\"$company\"></TD>
       <TD VALIGN=bottom>&nbsp;&nbsp;
         <INPUT NAME=\"hd_company_id\" TYPE=hidden VALUE=\"\">
         <INPUT NAME=\"popup\" TYPE=hidden VALUE=\"$popup\">
         <INPUT NAME=\"action\" TYPE=hidden VALUE=\"search\">
         <INPUT NAME=\"submit\" TYPE=submit VALUE=\"$l_find\">
         $ext&nbsp;
       </TD>
     </TR>
     </TABLE>
     </FORM><HR>
   </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact search result                                         //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_list($contact, $popup=false) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "contact");
  $obm_q = run_query_search($contact, $new_order, $order_dir);
  $nb_contact = $obm_q->num_rows();
  if ($nb_contact == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_contact_search_list($obm_q,$pref_q,$nb_contact,$contact,$popup);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Contact search result                                    //
// Parameters : 
//   - $obm_q     : list of the contacts to display 
//   - $pref_q    : the fields which have to be displayed
//   - $nb_con    : nb contacts returned by the search query 
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_list($obm_q,$pref_q,$nb_con,$contact,$popup) {
  global $sess, $col_textem, $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $contact["ext_action"];
    $ext_url = $contact["ext_url"];
    $ext_id = $contact["ext_id"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }

  // we urlencode to avoid breakage for space char
  $lname = stripslashes($contact["lname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  $company_id = $contact["company_id"];

  echo "<center><font color=\"#$col_textem\">$nb_con $l_found</font><br>";

  $url = $sess->url("contact_index.php?action=search&amp;tf_lname=".urlencode($lname)."&amp;tf_phone=".urlencode($phone)."&amp;tf_company=".urlencode($company)."&amp;param_company=$company_id$url_ext");

  $dis_contact_list = new OBM_DISPLAY("DATA",$pref_q);
  if ($popup) {
    $dis_contact_list->display_link = false;
    $dis_contact_list->data_cb_text = "X";
    $dis_contact_list->data_idfield = "contact_id";
    $dis_contact_list->data_cb_name = "cb_con";
    echo "\n<FORM TARGET=root METHOD=POST action=\"$ext_url\">";
  }
  $dis_contact_list->data_set = $obm_q;
  $dis_contact_list->data_header = "both";
  $dis_contact_list->data_url = $url;
  $dis_contact_list->display();

  if ($popup) {
    echo "<INPUT TYPE=submit value=\"$l_add\">
      <INPUT TYPE=hidden name=param_list value=\"$ext_id\">
      <INPUT TYPE=hidden name=action value=\"$ext_action\">
      </FORM>
      <p align=right>
      <a href=\"\" onclick='window.close();'>$l_close</a>";
  }

  echo "</center>";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation                                         //
// Parameters:
//   - $co_q      : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_consult($co_q) {
  global $l_lastname,$l_firstname,$l_kind,$l_function,$l_company,$l_phone,$l_hphone,$l_mphone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_comment,$l_public,$l_private,$l_visibility;
  global $l_update,$l_delete,$l_insert;
  global $set_theme, $col_label, $ico_mail;
  global $action, $auth, $sess;
  global $perms_user,$l_perms_user;

  $usercreate = $co_q->f("contact_usercreate");
  $c_id = $co_q->f("contact_company_id");
  $c_name = $co_q->f("company_name");
  $c_phone = $co_q->f("company_phone");
  $id = $co_q->f("contact_id");
  $kind = $co_q->f("kind_minilabel");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
  $ad1 = $co_q->f("contact_address1");
  $ad2 = $co_q->f("contact_address2");
  $zip = $co_q->f("contact_zipcode");
  $town = $co_q->f("contact_town");
  $cdx = $co_q->f("contact_expresspostal");
  $ctry = $co_q->f("contact_country");
  $func = $co_q->f("contact_function");
  $phone = $co_q->f("contact_phone");
  if ( (trim($phone) != "") && ($phone == $c_phone)) {
    $phone = "( $phone )";
  }
  $hphone = $co_q->f("contact_homephone");
  $mphone = $co_q->f("contact_mobilephone");
  $fax = $co_q->f("contact_fax");
  $email = $co_q->f("contact_email");
  $com = $co_q->f("contact_comment");
  $vis = $co_q->f("contact_visibility");

  echo "<CENTER>
    <TABLE BORDER=0>
    <TR><TD>

  <TABLE BORDER=0>
  <TR><TD>

    <TABLE BORDER=0>";
  if ($usercreate == $auth->auth["uid"]) {
    echo "<TR>
      <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_visibility</FONT></TD>
      <TD>";
    if ($vis == 1) 
	echo $l_private;
    else 
	echo $l_public;    	
    echo "</TD>
    </TR>";
  }
  echo "<TR><TD>
    <FONT COLOR=\"#$col_label\" SIZE=-2>$l_lastname</FONT></TD>
    <TD>$lname</TD>
    </TR><TR>
    <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_firstname</FONT> </TD>
    <TD>$fname</TD>
    </TR><TR>
    <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_function</FONT> </TD>
    <TD>$func</TD>
    </TR><TR>
    <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_company</FONT> </TD>";
  echo "
    <TD><A HREF=\"".
      $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$c_id") ."\"> $c_name</A>
    </TR><TR>
    <TD colspan=2>
      <TABLE BORDER=0><TR>
      <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_phone</FONT> </TD>
      <TD>$phone</TD>
      <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_hphone</FONT> </TD>
      <TD>$hphone</TD>
      </TR><TR>
      <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_fax</FONT> </TD>
      <TD>$fax</TD>
      <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_mphone</FONT> </TD>
      <TD>$mphone</TD>
      <TR></TABLE>
    </TD>
    </TR></TABLE>
  </TD>
  <TD>&nbsp;&nbsp;</TD>
  <TD valign=top>
    <TABLE BORDER=0>
    <TR>
    <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_kind&nbsp;</FONT></TD>
    <TD>$kind</TD>
    </TR><TR>
    <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_address&nbsp;1&nbsp;&nbsp;</FONT></TD>
    <TD>$ad1</TD>
    </TR><TR>
    <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_address&nbsp;2&nbsp;&nbsp;</FONT></TD>
    <TD>$ad2</TD>
    </TR><TR>
     <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_postcode&nbsp;&nbsp;</FONT></TD>
     <TD>$zip &nbsp;&nbsp;&nbsp;
     <FONT COLOR=\"#$col_label\" SIZE=-2>$l_expresspostal</FONT>
     &nbsp; $cdx</TD>
     </TR><TR>
     <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_town&nbsp;&nbsp;</FONT></TD>
     <TD>$town</TD>
     </TR><TR>
     <TD ALIGN=right><FONT COLOR=\"#$col_label\" SIZE=-2>$l_country&nbsp;&nbsp;</FONT></TD>
     <TD>$ctry</TD>
    </TR></TABLE>
  </TD>
  </TR></TABLE>
  </TD></TR>
  <TR><TD>
  <TABLE BORDER=0>
  <TR>
  <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_email&nbsp;&nbsp;&nbsp;</FONT></TD>
  <TD><a href=\"mailto:$email\"><img border=0 src=\"/images/$set_theme/$ico_mail\" ALT=C></a> $email</TD>
  </TR>
  </TABLE>
  <TABLE BORDER=0>
  <TR>
  <TD><FONT COLOR=\"#$col_label\" SIZE=-2>$l_comment</FONT><BR>";
  echo nl2br($com);
  echo "</TD>
  </TR>
  </TABLE>
  </TD></TR>

  <TR><TD align=middle>";

  echo "<CENTER>
  <TABLE BORDER=0>
  <TR><TD valign=top>";

  if ($auth->auth["perm"] != $perms_user) {
    echo "<FORM METHOD=GET NAME=form_mod_contact 
        ACTION=\"".$sess->url("contact_index.php")."\">
      <INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"detailupdate\">
      <INPUT TYPE=\"hidden\" NAME=\"param_contact\" VALUE=$id>
      <INPUT TYPE=submit value=\"$l_update\">
       </FORM>";
  }

  echo "&nbsp;</TD></TR></TABLE>
  </CENTER>
  </FORM>
  </TD></TR></TABLE>
  </CENTER>";
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $kind_q    : contact kind database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, zip, town, cdx, ctry, func
//                : phone, hpone, mphone, fax, email, com, vis, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $kind_q, $contact) {
  global $set_theme, $ico_mail, $col_table;
  global $l_lastname,$l_firstname,$l_kind,$l_function,$l_company,$l_phone,$l_hphone,$l_mphone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_comment,$l_private;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $popup_width, $popup_height;
  global $sess, $auth;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $c_id = $co_q->f("contact_company_id");
    $c_name = $co_q->f("company_name");
    $id = $co_q->f("contact_id");
    $usercreate = $co_q->f("contact_usercreate");
    $kind = $co_q->f("contact_kind_id");
    $lname = $co_q->f("contact_lastname");
    $fname = $co_q->f("contact_firstname");
    $ad1 = $co_q->f("contact_address1");
    $ad2 = $co_q->f("contact_address2");
    $zip = $co_q->f("contact_zipcode");
    $town = $co_q->f("contact_town");
    $cdx = $co_q->f("contact_expresspostal");
    $ctry = $co_q->f("contact_country");
    $func = $co_q->f("contact_function");
    $phone = $co_q->f("contact_phone");
    $hphone = $co_q->f("contact_homephone");
    $mphone = $co_q->f("contact_mobilephone");
    $fax = $co_q->f("contact_fax");
    $email = $co_q->f("contact_email");
    $com = $co_q->f("contact_comment");
    $vis = $co_q->f("contact_visibility");

  // New form and first time
  } elseif ($action == "new") {
    // if company given
    if (is_object($co_q)) {
      // fill the phone with company one
      $phone = $co_q->f("company_phone");
      $c_id = $co_q->f("company_id");
      $c_name = $co_q->f("company_name");
      $c_new_id = $c_id;
      $c_new_name = $c_name;
    }

  } else {
    $c_id = $contact["company_id"];
    $c_name = $contact["company_name"];
    $c_new_id = $contact["comp_new_id"];
    $c_new_name = $contact["comp_new_name"];
    $id = $contact["id"];
    $usercreate = $contact["usercreate"];
    $kind = $contact["kind"];
    $lname = stripslashes($contact["lname"]);
    $fname = stripslashes($contact["fname"]);
    $ad1 = stripslashes($contact["ad1"]);
    $ad2 = stripslashes($contact["ad2"]);
    $zip = stripslashes($contact["zip"]);
    $town = stripslashes($contact["town"]);
    $cdx = stripslashes($contact["cdx"]);
    $ctry = stripslashes($contact["ctry"]);
    $func = stripslashes($contact["func"]);
    $phone = stripslashes($contact["phone"]);
    $hphone = stripslashes($contact["hphone"]);
    $mphone = stripslashes($contact["mphone"]);
    $fax = stripslashes($contact["fax"]);
    $email = stripslashes($contact["email"]);
    $com = stripslashes($contact["com"]);
    $vis = $contact["vis"];
  }

  // kind select
  $sel_kind = "<SELECT name=sel_kind>";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("kind_id");
    $sel_kind .= "\n<OPTION value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected"; }
    $sel_kind .= ">". $kind_q->f("kind_minilabel") . "\n";
  }
  $sel_kind .= "</SELECT>";

  echo "<CENTER>
    <TABLE BORDER=0>
    <FORM METHOD=GET NAME=form_mod_contact onSubmit=\"if (check_contact(this)) return true; else return false;\" ACTION=\"".$sess->url("contact_index.php")."\">
    <TR><TD>

  <TABLE BORDER=0>
  <TR><TD>
    <TABLE BORDER=0>
    <TR>
    <TD><FONT SIZE=-2>$l_lastname</FONT></TD>
    <TD><INPUT NAME=tf_lname size=30 value=\"$lname\"></TD>
    </TR>
    <TR>
    <TD><FONT SIZE=-2>$l_firstname</FONT> </TD>
    <TD><INPUT NAME=tf_fname size=30 value=\"$fname\"></TD>
    </TR><TR>
    <TD><FONT SIZE=-2>$l_function</FONT> </TD>
    <TD><INPUT NAME=tf_func size=30 value=\"$func\"></TD>
    </TR><TR>
    <TD><FONT SIZE=-2>$l_company</FONT> </TD>
    <TD><A HREF=\"".
      $sess->url("../company/company_index.php?action=detailconsult&amp;param_company=$c_id") . "\">$c_name";
  echo "</A><INPUT TYPE=hidden NAME=param_company value=\"$c_id\">
      <INPUT TYPE=hidden NAME=company_name value=\"$c_name\">
      <INPUT TYPE=hidden NAME=company_new_id value=\"$c_new_id\">
      <a href=\"\" onclick=\"window.open('../company/company_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_contact_select_company)."','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_mail\"></a>
      <INPUT TYPE=text NAME=company_new_name value=\"$c_new_name\" style=\"border:none\" READONLY onfocus=\"this.blur();\">
    </TD>
    </TR><TR>
    <TD colspan=2>
      <TABLE BORDER=0><TR>     
      <TD><FONT SIZE=-2>$l_phone</FONT> </TD>
      <TD><INPUT NAME=tf_phone size=16 value=\"$phone\"></TD>
      <TD><FONT SIZE=-2>$l_hphone</FONT> </TD>
      <TD><INPUT NAME=tf_hphone size=16 value=\"$hphone\"></TD>
      </TR><TR>
      <TD><FONT SIZE=-2>$l_fax</FONT> </TD>
      <TD><INPUT NAME=tf_fax size=16 value=\"$fax\"></TD>
      <TD><FONT SIZE=-2>$l_mphone</FONT> </TD>
      <TD><INPUT NAME=tf_mphone size=16 value=\"$mphone\"></TD>
      <TR></TABLE>
    </TD>
    </TR></TABLE>
  </TD>
  <TD>&nbsp;&nbsp;</TD>
  <TD valign=top>

    <TABLE BORDER=0>
    <TR>
    <TD ALIGN=right><FONT SIZE=-2>$l_kind &nbsp;</FONT></TD>
    <TD>$sel_kind</TD>
  </TR><TR>
    <TD ALIGN=right><FONT SIZE=-2>$l_address&nbsp;&nbsp;<BR>1&nbsp;&nbsp;</FONT></TD>
    <TD><INPUT NAME=tf_ad1 size=30 VALUE=\"$ad1\"></TD>
  </TR><TR>
    <TD ALIGN=right><FONT SIZE=-2>$l_address&nbsp;&nbsp;<BR>2&nbsp;&nbsp;</FONT></TD>
    <TD><INPUT NAME=tf_ad2 size=30 VALUE=\"$ad2\"></TD>
    </TR><TR>
     <TD ALIGN=right><FONT SIZE=-2>$l_postcode&nbsp;&nbsp;</FONT></TD>
     <TD><INPUT NAME=tf_zip size=6 VALUE=\"$zip\">&nbsp;&nbsp;&nbsp;
       <FONT SIZE=-2>$l_expresspostal</FONT> &nbsp;
       <INPUT NAME=tf_cdx size=8 VALUE=\"$cdx\">
     </TD>
     </TR><TR>
     <TD ALIGN=right><FONT SIZE=-2>$l_town&nbsp;&nbsp;</FONT></TD>
     <TD><INPUT NAME=tf_town size=24 VALUE=\"$town\"></TD>
     </TR><TR>
     <TD ALIGN=right><FONT SIZE=-2>$l_country&nbsp;&nbsp;</FONT></TD>
     <TD><INPUT NAME=tf_ctry size=20 VALUE=\"$ctry\"></TD>
    </TR></TABLE>
  </TD>
  </TR></TABLE>

</TD></TR>
<TR><TD>
  <TABLE BORDER=0>
  <TR>
  <TD><FONT SIZE=-2>$l_email&nbsp;&nbsp;&nbsp;</FONT></TD>
  <TD><INPUT NAME=tf_email size=52 value=\"$email\"></TD>";
  
  // If new contact or contact update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action=="detailupdate") || ($action=="update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    echo "<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD><TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
      <TD>
      <TABLE border=2 bgcolor=\"#$col_table\">
      <TR><TD>&nbsp; $l_private &nbsp;
      <INPUT NAME=cb_vis TYPE=\"checkbox\" value=\"1\" ";
    if ($vis == '1') echo " checked";
    echo "></TD></TR></TABLE>
      </TD>";
  }

  echo "</TR>
  </TABLE>
  <TABLE BORDER=0>
  <TR>
  <TD><FONT SIZE=-2>$l_comment</FONT><BR>
  <TEXTAREA NAME=ta_com ROWS=6 COLS=60>$com</TEXTAREA></TD>
  </TR>
  </TABLE>
  </TD></TR>

  <TR><TD align=middle>";
  echo "<CENTER><TABLE BORDER=0>
  <TR><TD valign=top>";

  // UPDATE 
  if (($action=="detailupdate") || ($action=="update")) {
    echo "<INPUT TYPE=hidden NAME=action VALUE=update>
      <INPUT TYPE=hidden NAME=param_contact VALUE=\"$id\">
      <INPUT TYPE=hidden NAME=hd_usercreate VALUE=\"$usercreate\">
      <INPUT TYPE=submit value=\"$l_update\">
    </FORM></TD>";

  // CHECK DELETE
  echo "<TD valign=top>
        <FORM METHOD=GET NAME=f_del_con  
        ACTION=\"" . $sess->url("contact_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=check_delete>
    <INPUT TYPE=hidden NAME=param_contact VALUE=$id>
    <INPUT TYPE=submit value=\"$l_checkdelete\">\n";

  // INSERT
  } elseif (($action=="new") || ($action=="insert")) {
    echo "<INPUT TYPE=\"hidden\" NAME=\"action\" VALUE=\"insert\">
    <INPUT TYPE=submit value=\"$l_insert\">";
  }

  echo "
    </TD></TR>
    </TABLE>
    </CENTER>
    </FORM>
    </TD></TR></TABLE>
    </CENTER>";

}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a contact insertion or update                   //
// When similar contacts exist we show these and ask confirmation
// Parameters:
//   - $cid       : contact id
//   - $co_q      : contact database result (at least 1 row)
//   - $contact[] : values for insertion/update (if confirmation)
//     keys used  : company_id, kind, lname, fname ad1, ad2, zip, town, cdx
//                : ctry, func, phone, hphone, mphone, fax, email, com, vis
/////////////////////////////////////////////////////////////////////////////
function dis_contact_warn_insert($cid, $co_q, $contact) {
  global $l_check_samecontact, $l_confirm, $l_back;
  global $sess, $c_yes, $c_no;

  $cid = $contact["company_id"];
  $kind = $contact["kind"];
  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $ad1 = stripslashes($contact["ad1"]);
  $ad2 = stripslashes($contact["ad2"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $cdx = stripslashes($contact["cdx"]);
  $ctry = stripslashes($contact["ctry"]);
  $func = stripslashes($contact["func"]);
  $phone = stripslashes($contact["phone"]);
  $hphone = stripslashes($contact["hphone"]);
  $mphone = stripslashes($contact["mphone"]);
  $fax = stripslashes($contact["fax"]);
  $email = stripslashes($contact["email"]);
  $com = $contact["com"];
  $vis = ($contact["vis"] == 1 ? 1 : 0);

  echo $l_check_samecontact;
  while ($co_q->next_record()) {
    $id = $co_q->f("contact_id");
    $samename = $co_q->f("contact_lastname")." ". $co_q->f("contact_firstname");
    $company = $co_q->f("company_name");
    echo "<BR><A HREF=\"" .$sess->url("contact_index.php?action=detailconsult&amp;param_contact=$id") . "\">$samename ($company)</A>";
  }

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=POST NAME=form_insert
    ACTION=\"" .$sess->url("contact_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=insert>
    <INPUT TYPE=hidden NAME=hd_confirm VALUE=\"$c_yes\">
    <INPUT TYPE=hidden NAME=param_company VALUE=\"$cid\">
    <INPUT TYPE=hidden NAME=sel_kind VALUE=\"$kind\">
    <INPUT TYPE=hidden NAME=tf_lname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_fname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_func VALUE=\"$func\">
    <INPUT TYPE=hidden NAME=tf_ad1 VALUE=\"$ad1\">
    <INPUT TYPE=hidden NAME=tf_ad2 VALUE=\"$ad2\">
    <INPUT TYPE=hidden NAME=tf_zip VALUE=\"$zip\">
    <INPUT TYPE=hidden NAME=tf_town VALUE=\"$town\">
    <INPUT TYPE=hidden NAME=tf_cdx VALUE=\"$cdx\">
    <INPUT TYPE=hidden NAME=tf_ctry VALUE=\"$ctry\">
    <INPUT TYPE=hidden NAME=tf_phone VALUE=\"$phone\">
    <INPUT TYPE=hidden NAME=tf_hphone VALUE=\"$hphone\">
    <INPUT TYPE=hidden NAME=tf_mphone VALUE=\"$mphone\">
    <INPUT TYPE=hidden NAME=tf_fax VALUE=\"$fax\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <INPUT TYPE=hidden NAME=ta_com VALUE=\"$com\">
    <INPUT TYPE=hidden NAME=cb_vis VALUE=\"$vis\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_confirm\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=POST
    ACTION=\"" .$sess->url("contact_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=new>
    <INPUT TYPE=hidden NAME=param_company VALUE=\"$cid\">
    <INPUT TYPE=hidden NAME=sel_kind VALUE=\"$kind\">
    <INPUT TYPE=hidden NAME=tf_lname VALUE=\"$lname\">
    <INPUT TYPE=hidden NAME=tf_fname VALUE=\"$fname\">
    <INPUT TYPE=hidden NAME=tf_func VALUE=\"$func\">
    <INPUT TYPE=hidden NAME=tf_ad1 VALUE=\"$ad1\">
    <INPUT TYPE=hidden NAME=tf_ad2 VALUE=\"$ad2\">
    <INPUT TYPE=hidden NAME=tf_zip VALUE=\"$zip\">
    <INPUT TYPE=hidden NAME=tf_town VALUE=\"$town\">
    <INPUT TYPE=hidden NAME=tf_cdx VALUE=\"$cdx\">
    <INPUT TYPE=hidden NAME=tf_ctry VALUE=\"$ctry\">
    <INPUT TYPE=hidden NAME=tf_phone VALUE=\"$phone\">
    <INPUT TYPE=hidden NAME=tf_hphone VALUE=\"$hphone\">
    <INPUT TYPE=hidden NAME=tf_mphone VALUE=\"$mphone\">
    <INPUT TYPE=hidden NAME=tf_fax VALUE=\"$fax\">
    <INPUT TYPE=hidden NAME=tf_email VALUE=\"$email\">
    <INPUT TYPE=hidden NAME=ta_com VALUE=\"$com\">
    <INPUT TYPE=hidden NAME=cb_vis VALUE=\"$vis\">
    <INPUT TYPE=submit VALUE=\"$l_back\">
    </FORM>
    </TD></TR>
    </TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the contact links (and delete form if ok)              //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_list, $l_link_list_no;
  global $sess;

  $delete_ok = true;

  // Links from deals
  $obm_q = run_query_contact_deal_links($p_id);
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

  // Links from lists
  $obm_q = run_query_contact_list_links($p_id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    echo "$l_link_list";
    while ($obm_q->next_record()) {
      $lid = $obm_q->f("list_id");
      $lname = $obm_q->f("list_name");
      echo "<BR><A HREF=\"" .$sess->url("../list/list_index.php?action=detailconsult&amp;param_list=$lid") . "\">$lname</A>\n";
    }
    echo "<P>";
  } else {
    echo  $l_link_list_no . "<P>";
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
   ACTION=\"" . $sess->url("contact_index.php") ."\">
   <INPUT TYPE=hidden NAME=action VALUE=detailupdate>
   <INPUT TYPE=hidden NAME=param_contact VALUE=\"$p_id\">
   <INPUT TYPE=submit VALUE=\"$l_back\">
   </FORM>";
  echo "</TR></TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;
  global $sess;

  echo "\n<FORM name=\"form_delete\" " .
      "METHOD=post action=\"" . $sess->url("contact_index.php") . "\">
    <INPUT TYPE=\"hidden\" name=\"action\" value=\"delete\">
    <INPUT TYPE=\"hidden\" name=\"param_contact\" value=\"$p_id\">
    <INPUT TYPE=\"submit\" value=\"$l_delete\"
    onClick=\"if (confirm_del(this.form)) return true; else return false;\">";
  echo "</FORM>\n";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Display preference screen                             //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_contact_display_pref($display_pref_q) {
  global $l_contact_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "contact";
  $dis_pref->pref_title = $l_contact_display;
  $dis_pref->pref_dis_help = 1;

  echo "<center>";
  $dis_pref->display();
  echo "<br></center>";
}


</SCRIPT>
