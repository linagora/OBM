<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_display.php                                             //
//     - Desc : List Display File                                            //
// 2000-01-20 Vincent MARGUERIT                                              //
// 2001-08-23 jlb : add list_comment, add export for letter mailing
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Display List search Form                                                  //
// Parameters : 
//   - $list[]        : default form values
//     keys used      : name, contact
///////////////////////////////////////////////////////////////////////////////
function html_list_search_form ($list) {
  global $l_name, $l_list_contact, $l_email, $l_find;
  global $sess, $popup;

  if ($popup) {
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext = "<input name=ext_action type=hidden value=\"$ext_action\">
      <input name=ext_id type=hidden value=\"$ext_id\">
      <input name=ext_url type=hidden value=\"$ext_url\">";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($list["name"]);
  $email = stripslashes($list["email"]);
  $contact = stripslashes($list["contact"]);

  // --- HTML Page display ----------------------------------------------------

  echo "
    <center>
    <form method=get name=f_list action=\"".url_prepare("list_index.php")."\">
      <table border=0>
        <tr>
          <td>
            $l_name
            <br>
            <input type=text name=tf_name size=16 value=\"$name\">
          </td>
          <td>&nbsp;&nbsp;</td>
          <td align=left>$l_list_contact<br>
            <input type=text name=tf_contact size=16 value=\"$contact\">
          </td>
          <td>&nbsp;&nbsp;</td>
          <td align=left>$l_email<br>
            <input type=text name=tf_email size=20 value=\"$email\">
          </td>
          <td valign=bottom>&nbsp;&nbsp;
            <input name=\"h_company_id\" type=hidden value=\"\">
            <input name=\"action\" type=hidden value=\"search\">
            <input name=\"popup\" type=hidden value=\"$popup\">
            <input name=\"submit\" type=submit value=\"$l_find\">
            $ext
          </td>
        </tr>
      </table>
    </form><hr>
    </center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the List search result                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_list_search_list($list, $popup=false) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "list");
  $obm_q = run_query_search($list, $new_order, $order_dir);
  $nb_list = $obm_q->num_rows();
  if ($nb_list == 0) {
    display_warn_msg($l_no_found);
  } else {
    html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup);
  }
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the List search result                                       //
// Parameters : 
//   - $obm_q      : list of the contacts to display 
//   - $pref_q     : the fields which have to be displayed
//   - $nb_contact            : nb contacts returned by the search query 
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_list_search_list($obm_q, $pref_q, $nb_list, $list, $popup) {
  //-- Themes
  global $set_theme,$col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Labels
  global $l_found, $l_add, $l_close;
  //-- Search parameters
  global $perm, $action, $sess;

  if ($popup) {
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id";
  }

  $name = urlencode(stripslashes($list["name"]));
  $contact = urlencode(stripslashes($list["contact"]));

  echo "<center><FONT color=\"#$col_textem\">";
  echo  "$nb_list $l_found";
  echo "</FONT><BR>";

  $url = url_prepare("list_index.php?action=search&amp;tf_name=$namet&amp;tf_contact=$contact$url_ext");
  
  $dis_list = new OBM_DISPLAY("DATA",$pref_q);
  if ($popup) {
    $dis_list->display_link = false;
    $dis_list->data_cb_text = "X";
    $dis_list->data_idfield = "list_id";
    $dis_list->data_cb_name = "cb_list";
    echo "<form target=root method=get action=\"$ext_url\">";
  }
  $dis_list->data_set = $obm_q;
  $dis_list->data_header = "both";
  $dis_list->data_url = $url;
  $dis_list->display();

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
// Display List Form                                                         //
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_form($action, $list_q, $list) {
  global $l_name, $l_subject, $l_email, $l_insert, $l_update, $l_back;
  global $sess;
  global $col_tableh, $col_textem;
  
  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $list_q->f("list_id");
    $usercreate = $list_q->f("list_usercreate");
    $name = $list_q->f("list_name");
    $subject = $list_q->f("list_subject");
    $email = $list_q->f("list_email");
  } else {
    // Values are taken from parameters
    $id = $list["id"];
    $name = stripslashes($list["name"]);
    $subject = stripslashes($list["subject"]);
    $email = stripslashes($list["email"]);
  }

  echo "
  <form method=get name=f_new action=\"".url_prepare("list_index.php")."\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">
  <table width=\"50%\" border=0 cellpadding=0 cellspacing=0>
  <tr>
    <td align=right>$l_name </td>
    <td align=left><input type=text name=tf_name value=\"$name\"></td>
  </tr>
  <tr>
    <td align=right>$l_subject </td>
    <td align=left><input name=tf_subject size=30 value=\"$subject\"></td>
  </tr>
  <tr>
    <td align=right>$l_email </td>
    <td align=left><input name=tf_email size=30 value=\"$email\"></td>
  </tr>
  <tr>
    <td colspan=2 align=middle>
          <br>";

  if (($action == "detailupdate") || ($action == "update")) {
    echo "<table><tr><td>
      <input type=hidden name=param_list value=\"$id\">
      <input type=hidden name=action value=update>
      <input type=submit value=\"$l_update\">
      </form>
      </td><td>
      <form method=get name=f_back action=\"".url_prepare("list_index.php")."\">
      <input type=hidden name=action value=detailconsult>
      <input type=hidden name=param_list value=\"$id\">
      <input type=submit value=\"$l_back\">
      </form>
      </td></tr></table>";
  } else {
    echo "<input type=hidden name=action value=insert>
      <input type=submit value=\"$l_insert\">
      </form>";
  }

  echo "
    </td>
  </tr>
  </table>";
}


///////////////////////////////////////////////////////////////////////////////
// Display List links menu                                                   //
// Parameters:
//   - $id   : list id
//   - $name : company name
///////////////////////////////////////////////////////////////////////////////
function html_list_links($id, $list_name, $l_action="") {
  global $sess, $perm;
  global $l_add_contact, $l_delete, $l_mail, $l_update, $l_export;
  global $popup_width, $popup_contact_height;

  $url_update = url_prepare("list_index.php?action=detailupdate&amp;param_list=$id");
  $url_delete = url_prepare("list_index.php?action=check_delete&amp;hd_list_id=$id");

  echo "<table cellpadding=0 cellspacing=0 border=0>
    <tr>
      <td><a href=\"$url_update\">$l_update</a></td>
    </tr>";
  if($perm->have_perm("editor")) {
    echo "<tr><td><a href=\"$url_delete\">$l_delete</a></td></tr>";
  }
  if ($perm->have_perm("editor")) {
    $ext_url = url_prepare("../list/list_index.php");
    echo "<tr><td><a href=\"\" onclick=\"window.name='root';window.open('" .url_prepare("../contact/contact_index.php?action=ext_get_ids&amp;popup=1&amp;title=" . urlencode($l_add_contact) . "&amp;ext_action=contact_add&amp;ext_url=$ext_url&amp;ext_id=$id") . "','Company','height=$popup_height,width=$popup_width'); return false;\">$l_add_contact</a></td></tr>";
  }
  if ($perm->have_perm("editor")) {
    echo "<tr><td><a href=\"".url_prepare("list_index.php?action=export_add&amp;cb_list$id=$id")."\">$l_export</a></td></tr>";
  }
  echo "
            </td>
          </tr>
        </table>";
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display List Consultation                                            //
// Parameters:
//   - $list_q : company database result 
//   - $con_q  : contact database result 
///////////////////////////////////////////////////////////////////////////////
function html_list_consult($list_q, $pref_con_q, $con_q) {
  //-- Themes
  global $col_label, $col_tableh,$col_textem,$col_table,$ico_mail;
  //-- Label
  global $l_name, $l_subject, $l_email, $l_list_infos, $l_no_contact, $l_list_infos_short;
  global $sess, $perm, $action;
  global $l_del_contact_sel, $l_con_found, $nb_contact_added, $nb_contact_deleted;

  $id = $list_q->f("list_id");
  $usercreate = $list_q->f("list_usercreate");
  $userupdate = $list_q->f("list_userupdate");
  $timecreate = $list_q->f("timecreate");
  $timeupdate = $list_q->f("timeupdate");
  $name = $list_q->f("list_name");
  $subject = $list_q->f("list_subject");
  $email = $list_q->f("list_email");

  display_record_info($usercreate, $userupdate, $timecreate, $timeupdate);

  echo "<CENTER>
    <TABLE BORDER=1 WIDTH=95%>
    <TR><TD VALIGN=top>\n";

  html_list_links($id, $name);

  echo "</TD><TD>
      <table border=0>      
      <tr>
        <td align=right><font color=\"#$col_label\" size=-2>$l_name
          </font></td>
        <td>$name</td>
      </tr><tr>
        <td align=right><font color=\"#$col_label\" size=-2>$l_subject
          </font></td>
        <td>$subject</td>
      </tr><tr>
        <td align=right><font color=\"#$col_label\" size=-2>$l_email
          </font></td>
        <td>$email</td>
      </tr>
      </table>
      </TD>
    </TR>
    </table>";

  $nb_con = $con_q->num_rows();
  if ($nb_con == 0) {
    $message = $l_no_contact;
  } else {
    $message = "$nb_con $l_con_found";
  }

  echo "<FONT color=\"#$col_textem\">";
  echo "$message";
  echo "</FONT><BR>";

  if ($nb_con != 0) {

    $url= url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $pref_con_q);
    $dis_infos->data_set = $con_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = "both";

    if ($perm->have_perm("editor")) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "list_contact_id";
      $dis_infos->data_cb_name = "cb_con";
      $dis_infos->data_cb_field = "";
      echo "\n<FORM METHOD=GET action=\"list_index.php\">";
    }

    $dis_infos->display();

    if ($perm->have_perm("editor")) {
      echo "<INPUT TYPE=submit value=\"$l_del_contact_sel\"> 
        <INPUT TYPE=hidden name=param_list value=\"$id\"> 
        <INPUT TYPE=hidden name=action value=\"contact_del\"> 
        </FORM>";
    }
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display the List Display preference screen                                //
// Parameters:
//   - $pref_list_q    : DBO : List Field list to display
//   - $pref_contact_q : DBO : Contact Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_list_display_pref($pref_list_q, $pref_contact_q) {
  global $l_list_display, $l_contact_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_list_q);
  $dis_pref->display_entity = "list"; 
  $dis_pref->pref_title = $l_list_display;
  $dis_pref->pref_dis_help = 0;

  echo "<center>
    <table><tr><td valign=top>";
  $dis_pref->display();

  $dis_pref->display_pref = $pref_contact_q;
  $dis_pref->display_module = "list";
  $dis_pref->display_entity = "list_contact";
  $dis_pref->pref_title = $l_contact_display;

  echo "</td>
    <td valign=top>";
  $dis_pref->display();

  echo "</td></tr></table>";
  $dis_pref->dis_pref_help();
  echo "</center>";
}


///////////////////////////////////////////////////////////////////////////////
// Display warning before a list deletion                                    //
// We ask confirmation or cancel
// Parameters:
//   - $l_id     : list id
/////////////////////////////////////////////////////////////////////////////
function dis_warn_delete($l_id) {
  global $l_warn_delete, $l_delete, $l_back;

  $nb_con = get_list_nb_contact($l_id);
  echo "<P>$nb_con $l_warn_delete";

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=GET NAME=form_delete
    ACTION=\"" .url_prepare("list_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=delete>
    <INPUT TYPE=hidden NAME=hd_list_id VALUE=\"$l_id\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_delete\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=GET
    ACTION=\"" .url_prepare("list_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=detailconsult>
    <INPUT TYPE=hidden NAME=param_list VALUE=\"$l_id\">
    <INPUT TYPE=submit VALUE=\"$l_back\">
    </FORM>
    </TD></TR>
    </TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a list insertion or update                      //
// When similar lists exist we show these and ask confirmation
// Parameters:
//   - $l_id     : list id
//   - $l_q      : list database result (at least 1 row)
//   - $list[]   : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_list_warn_insert($l_id, $l_q, $list) {
  global $l_check_samelist, $l_confirm, $l_back;
  global $sess, $c_yes, $c_no;

  $name = $list["name"];
  $subject = $list["subject"];

  echo $l_check_samelist;
  while ($l_q->next_record()) {
    $id = $l_q->f("list_id");
    $samename = $l_q->f("list_name");
    $samesubject = $l_q->f("list_subject");
    echo "<BR><A HREF=\"" .url_prepare("list_index.php?action=detailconsult&amp;param_list=$id") . "\">$samename ($samesubject)</A>";
  }

  echo "<P>
    <TABLE><TR><TD>
    <FORM METHOD=POST NAME=form_insert
    ACTION=\"" .url_prepare("list_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=insert>
    <INPUT TYPE=hidden NAME=hd_confirm VALUE=\"$c_yes\">
    <INPUT TYPE=hidden NAME=tf_name VALUE=\"$name\">
    <INPUT TYPE=hidden NAME=tf_subject VALUE=\"$subject\">
    <INPUT TYPE=submit NAME=submit VALUE=\"$l_confirm\">
    </FORM>
    </TD><TD>
    <FORM NAME=form_back METHOD=POST
    ACTION=\"" .url_prepare("list_index.php") . "\">
    <INPUT TYPE=hidden NAME=action VALUE=new>
    <INPUT TYPE=hidden NAME=tf_name VALUE=\"$name\">
    <INPUT TYPE=hidden NAME=tf_subject VALUE=\"$subject\">
    <INPUT TYPE=submit VALUE=\"$l_back\">
    </FORM>
    </TD></TR>
    </TABLE>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the export format form                                            //
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
///////////////////////////////////////////////////////////////////////////////
function dis_export_form($list) {
  global $col_label, $col_tableh,$col_textem,$col_table,$ico_mail;
  global $c_export_format, $l_no_found, $l_list_set, $l_export_format;
  global $l_export_intro, $l_name, $l_desc, $l_help;
  global $set_theme, $ses_list;

  display_ok_msg($l_export_intro);

  $nb_list = $list["list_nb"];
  $cpt = 1;
  $d_list = "<table border=0>
    <tr><td bgcolor=\"#$col_tableh\">
      <font color=\"$col_textem\">$l_list_set</font>
      </td>
    </tr>";
  $param_list = "&amp;list=";
  $first = true;
  while ( list( $key, $val ) = each( $ses_list ) ) {
    if ($first == true) {
      $param_list .= $val;
    } else {
      $param_list .= ",$val";
    }
    $first = false;
    $name = get_list_name($val);
    $d_list .= "<tr><td align=center bgcolor=\"#$col_table\">
        <a href=\"list_index.php?action=detailconsult&amp;param_list=$val\">$name</a></td></tr>";
  }

  $d_list .= "</table>";

  $cpt = 0;
  $d_format = "<table border=0>
    <tr><td colspan=4 align=center bgcolor=\"#$col_tableh\">
      <font color=\"$col_textem\">$l_export_format</font>
      </td>
    </tr>
    <tr><td colspan=2 align=center bgcolor=\"#$col_tableh\">
      <font color=\"$col_textem\">$l_name</font></td>
      <td align=center bgcolor=\"#$col_tableh\">
      <font color=\"$col_textem\">$l_desc</font></td>
      <td align=center bgcolor=\"#$col_tableh\">
      <font color=\"$col_textem\">$l_help</font></td>
    </tr>";
  while ($c_export_format[$cpt]["name"]) {
    $name = $c_export_format[$cpt]["name"];
    $info = $c_export_format[$cpt]["info"];
    $url = url_prepare($c_export_format[$cpt]["url"] . $param_list);
    $icon = $c_export_format[$cpt]["icon"];
    $url_help = "export_format_$cpt.html";
    $d_format .= "<tr>
        <td bgcolor=\"#$col_table\"><a target=export href=\"$url\">$name</a></td>
        <td bgcolor=\"#$col_table\"><img src=\"/images/$set_theme/$icon\" alt=I></td>
        <td bgcolor=\"#$col_table\">$info</td>
        <td bgcolor=\"#$col_table\"><a target=doc href=\"$url_help\">X</a></td>
      </tr>";
    $cpt++;
  }
  $d_format .= "</table>";

  echo "<table border=0>
    <tr>
      <td>$d_list
      </td>
      <td>$d_format
      </td>
    </tr>
    </table>";

}


</SCRIPT>
