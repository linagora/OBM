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
//   - $usr_q     : database object with userobm list
//   - $company[] : default form values
//     keys used  : state, name, kind, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form ($kind_q, $usr_q, $company) {
  global $l_company_name, $l_company_kind, $l_postcode, $l_internal, $l_phone;
  global $l_all, $l_find, $l_market; 
  global $c_all, $sess;

  $state = $company["state"];
  $name = stripslashes($company["name"]);
  $phone = stripslashes($company["phone"]);
  $kind = $company["kind"];
  $mark = $company["marketing_manager"];
  $zip = stripslashes($company["zip"]);

  // kind select
  $sel_kind = "<select name=sel_kind>
    <option value=\"$c_all\">$l_all";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("companytype_id");
    $sel_kind .= "\n<option value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected"; }
    $sel_kind .= ">". $kind_q->f("companytype_label") . "\n";
  }
  $sel_kind .= "</select>";

  // Marketing manager select
  $sel_market = "<select name=sel_market>
    <option value=\"$c_all\">$l_all";
  while($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected"; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "\n";
  }
  $sel_market .= "</select>";

  if ($state == '1') $cstate = " checked";

  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <form method=get name=f_societe
      action=\"" . $sess->url("company_index.php") . "\">
    <table border=0>
    <tr>
      <td><font size=-2>$l_company_name</font><br>
        <input type=text name=tf_name size=20 value=\"$name\">
      <td>&nbsp;&nbsp;</td>
      <td><font size=-2>$l_phone</font><br>
        <input type=text name=tf_phone size=14 value=\"$phone\"></td>
      <td>&nbsp;&nbsp;</td>
      <td><font size=-2>$l_company_kind</font><br>
        $sel_kind
      </td>
      <td>&nbsp;&nbsp;</td>
      <td><font size=-2>$l_postcode</font><br>
        <input type=text name=tf_zip size=6 value=\"$zip\"></td>
      <td>&nbsp;&nbsp;</td>
      <td><font size=-2>$l_market</font><br>
        $sel_market
      <td>&nbsp;&nbsp;</td>
      <td align=center><font size=-2>$l_internal</font><br>
        <input type=\"checkbox\" name=\"cb_state\" value=\"1\" $cstate></td>
      <td align=center>&nbsp;&nbsp;&nbsp;&nbsp;
        <input name=\"action\" type=\"hidden\" value=\"search\">
        <input name=\"submit\" type=\"submit\" value=\"$l_find\">
      </td>
    </tr>
    </table>
    </form><hr>";
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
// Return the Display Company links menu                                     //
// Parameters:
//   - $p_id     : company id
//   - $name     : company name
//   - $con_num  : contact number
//   - $deal_num : deal number
// Returns:
//   $r : string with HTML code
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
  $r = "<a href=\"" . $sess->url("../contact/contact_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company=$uname\"><img border=0
          src=\"/images/$set_theme/$ico_contact_list\"></a>
        <font size=-1><a href=\"" . $sess->url("../contact/contact_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company=$uname\">$l_contact_list</a> ($con_num)</font>";

  // -------| Menu Nouveau Contact
  if ($auth->auth["perm"] != $perms_user) {
    $r .= "<br>
      <a href=\"" . $sess->url("../contact/contact_index.php?action=new&amp;param_company=$p_id") . "\"><img border=0
        src=\"/images/$set_theme/$ico_contact_new\"></a>
      <font size=-1><a href=\"" . $sess->url("../contact/contact_index.php?action=new&amp;param_company=$p_id") . "\">$l_contact_new</a></font>";
  }

  // -------| Menu Liste Affaires
  $r .= "<br>
    <a href=\"" . $sess->url("../deal/deal_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company_name=$uname\"><img border=0
      src=\"/images/$set_theme/$ico_contact_list\"></a>
    <font size=-1><a href=\"".$sess->url("../deal/deal_index.php?action=search&amp;param_company=$p_id") . "&amp;tf_company_name=$uname\">$l_deal_list</a> ($deal_num)</font>";

  // -------| Menu Nouvelle Affaire
  if ($auth->auth["perm"] != $perms_user) {
    $r .= "<br>
      <a href=\"".$sess->url("../deal/deal_index.php?action=new&amp;param_company=$p_id") . "\"><img border=0
        src=\"/images/$set_theme/$ico_contact_new\"></a>
      <font size=-1><a href=\"".$sess->url("../deal/deal_index.php?action=new&amp;param_company=$p_id") . "\">$l_deal_new</a></font>\n";
  }

  return $r;
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
  global $l_name,$l_kind,$l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_web,$l_comment,$l_number, $l_market;
  global $l_insert,$l_update,$l_delete;
  global $perms_user,$l_perms_user;
  global $auth, $sess;

  $id = $co_q->f("company_id");
  $num = $co_q->f("company_number");
  $state = $co_q->f("company_state");
  $name = $co_q->f("company_name");
  $kind = $co_q->f("company_type_id");
  $mark = $co_q->f("company_marketingmanager_id");
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

  if ($mark > 0)
    $lmarket = get_userobm_name($mark);
  else
    $lmarket = "&nbsp;";

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

  $links = html_company_links($id, $name, $con_num, $deal_num);
  $lcom = nl2br($com);

  if ($auth->auth["perm"] != $perms_user) {
    // -------| BITBTN - Update Mode 
    $dis_update = "<form method=get name=f_mod_societe
                         action=\"".$sess->url("company_index.php")."\">
      <input type=hidden name=param_company value=\"$id\">
      <input type=\"hidden\" name=\"action\" value=\"detailupdate\">
      <input type=\"submit\" value=\"$l_update\">
      </form>";
  }

  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <table border=0>
    <tr>
      <td valign=top>
      $links
      </td>
      <td>
      <table border=0>
      <tr>
        <td valign=top>
        <table border=0>
        <tr>
  <!-- NUMBER -->
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_number &nbsp;&nbsp;</font>
          </td>
          <td>$num</td>
  <!-- NAME -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_name &nbsp;&nbsp;</font>
          </td>
          <td>$name</td>
  <!-- KIND -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_kind &nbsp;&nbsp;</font>
          </td>
          <td>$lkind</td>
  <!-- Marketing manage -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_market &nbsp;&nbsp;</font>
          </td>
          <td>$lmarket</td>
  <!-- EMPTY -->
        </tr><TR>
          <td>&nbsp;</td>
          <td>&nbsp;</td>
  <!-- PHONE -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_phone &nbsp;</font>
          </td>
          <td>$phone</td>
  <!-- FAX -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_fax &nbsp;</font>
          </td>
          <td>$fax</td>
        </tr>
        </table>
        </td>

        <td>&nbsp;&nbsp;</td>
        <td valign=top>

        <table border=0>
        <tr>
  <!-- ADDRESS 1 -->
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_address &nbsp;1&nbsp;</font>
          </td>
          <td>$ad1</td>
  <!-- ADDRESS 2 -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_address &nbsp;2&nbsp;</font>
          </td>
          <td>$ad2</td>
  <!-- ZIP CODE + Express POSTAL -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_postcode &nbsp;</font>
          </td>
          <td>$zip &nbsp;&nbsp;&nbsp;
            <font color=\"#$col_label\" size=-2>$l_expresspostal &nbsp;$cdx</font>
          </td>
  <!-- TOWN -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_town &nbsp;</font>
          </td>
          <td>$town</td>
  <!-- COUNTRY -->
        </tr><tr>
          <td align=right>
            <font color=\"#$col_label\" size=-2>$l_country &nbsp;</font>
          </td>
          <td>$ctry</td>
        </tr>
        </table>

        </td>
      </tr>
      </table>

      <table border=0>
      <tr>
  <!-- EMAIL + LINK -->
        <td align=right>
          <font color=\"#$col_label\" size=-2>$l_email &nbsp;</font>
        </td>
        <td>$email</td>
        <td align=center>$link_email</td>
      </tr><tr>
  <!-- WEB + LINK -->
        <td align=right>
          <font color=\"#$col_label\" size=-2>$l_web &nbsp;</font>
        </td>
        <td>$web</td>
        <td align=center>$link_web</td>
      </tr>
      </table>

  <!-- COMMENTS -->
      <p>
      <font color=\"#$col_label\" size=-2>$l_comment</font>
      <br>
      $lcom

      $dis_update
      </td>
    </tr>
    </table>
    </center>";

}


///////////////////////////////////////////////////////////////////////////////
// Display Company Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : company database result 
//   - $kind_q    : kind database result 
//   - $usr_q     : userobm database result 
//   - $company[] : default values
//     keys used  : id, num, state, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_company_form($action, $co_q, $kind_q, $usr_q, $company) {
  // -- Themes
  global $set_theme,$ico_web,$ico_mail;
  // -- Labels
  global $l_name,$l_kind,$l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal,$l_town,$l_country,$l_email,$l_web,$l_comment,$l_number, $l_market;
  global $l_insert,$l_update,$l_checkdelete,$l_internal;
  global $sess;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("company_id");
    $num = $co_q->f("company_number");
    $state = $co_q->f("company_state");
    $name = $co_q->f("company_name");
    $kind = $co_q->f("company_type_id");
    $mark = $co_q->f("company_marketingmanager_id");
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
    $mark = $company["marketing_manager"];
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
  $sel_kind = "<select name=sel_kind>";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("companytype_id");
    $sel_kind .= "\n<option value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected"; }
    $sel_kind .= ">". $kind_q->f("companytype_label") . "\n";
  }
  $sel_kind .= "</select>";

  // Marketing manager select
  $sel_market = "<select name=sel_market>";
  while($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected"; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "\n";
  }
  $sel_market .= "</select>";

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

  if (($action == "detailupdate") || ($action == "update")) {
    $links = html_company_links($id, $name, $con_num, $deal_num);
  }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=hidden name=param_company value=\"$id\">
      <input type=\"hidden\" name=\"action\" value=\"update\">
      <input type=\"submit\" value=\"$l_update\"> 
      </form>
      </td>

      <!-- Delete button -->
      <td><form method=post name=f_sup_societe
      onSubmit=\"if (valider_suppression()) return true; else return false;\"
      action=\"" . $sess->url("company_index.php") . "\"> 
      <input type=hidden name=action value=check_delete>
      <input type=hidden name=param_company value=\"$id\">
      <input type=submit value=\"$l_checkdelete\">
      </form>";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "<input type=hidden name=\"action\" value=\"insert\">
      <input type=submit value=\"$l_insert\">
      </form>";
  }


  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <form method=post name=f_mod_societe
      onSubmit=\"if (check_company(this)) return true; else return false;\"
      action=\"".$sess->url("company_index.php")."\">

    <table border=0>
    <tr>
      <td valign=top>
       $links
      </td>
      <td>
      <table border=0>
      <tr>
        <td valign=top>
        <table border=0>
  <!-- NAME -->
        <tr>
          <td align=right>
            <font size=-2>$l_name &nbsp;&nbsp;</font>
          </td>
          <td><input name=tf_name size=30 maxlength=50 value=\"$name\"></td>
  <!-- NUMBER -->
        </tr><tr>
          <td><font size=-2>$l_number</font></td>
          <td><input name=tf_num value=\"$num\" size=30 maxlength=32></td>
  <!-- COMPANY KIND -->
        </tr><tr>
          <td align=right><font size=-2>$l_kind &nbsp;&nbsp;</font></td>
          <td>$sel_kind</td>
  <!-- Marketing Manager -->
        </tr><tr>
          <td align=right><font size=-2>$l_market &nbsp;&nbsp;</font></td>
          <td>$sel_market</td>
  <!-- STATE -->
        </tr><tr>
          <td><font size=-2>$l_internal</font></td>
          <td><input type=\"checkbox\" name=\"cb_state\" value=1 $lstate></td>
  <!-- PHONE -->
        </tr><tr>
          <td align=right><font size=-2>$l_phone &nbsp;&nbsp;</font></td>
          <td><input name=tf_phone size=16 maxlength=16 value=\"$phone\"></td>
  <!-- FAX -->
        </tr><tr>
          <td align=right><font size=-2>$l_fax &nbsp;&nbsp;</font></td>
          <td><input name=tf_fax size=16 maxlength=16 value=\"$fax\"></td>
        </tr>
        </table>

        </td>
        <td>&nbsp;&nbsp;</td>
        <td valign=top>

        <table border=0>
  <!-- ADDRESS 1 -->
        <tr>
          <td align=right>
            <font size=-2>$l_address &nbsp;<br>1 &nbsp;</font>
          </td>
          <td><input name=tf_ad1 size=30 maxlength=64 value=\"$ad1\"></td>
  <!-- ADDRESSE 2 -->
        </tr><tr>
          <td align=right>
            <font SIZE=-2>$l_address &nbsp;<br>2 &nbsp;</font>
          </td>
          <td><input name=tf_ad2 size=30 maxlength=64 value=\"$ad2\"></td>
  <!-- ZIP CODE + CEDEX -->
        </tr><tr>
          <td align=right><font size=-2>$l_postcode &nbsp;&nbsp;</font></td>
          <td>
            <input name=tf_zip size=6 maxlength=14 value=\"$zip\"> &nbsp;&nbsp;
            <font size=-2>$l_expresspostal</font> &nbsp;
            <input name=tf_cdx size=8 maxlength=8 value=\"$cdx\">
          </td>
  <!-- TOWN -->
        </tr><tr>
          <td align=right><font size=-2>$l_town &nbsp;&nbsp;</font></td>
          <td><input name=tf_town size=30 maxlength=64 value=\"$town\"></td>
  <!-- COUNTRY -->
        </tr><tr>
          <td align=right><font size=-2>$l_country &nbsp;&nbsp;</font></td>
          <td><input name=tf_ctry size=20 maxlength=24 value=\"$ctry\"></td>
        </tr>
        </table>
        </td>
      </tr>
      </table>

      <table border=0>
      <tr>
  <!-- EMAIL + LINK -->
        <td align=right><font size=-2>$l_email &nbsp;&nbsp;</font></td>
        <td><input name=tf_email size=45 maxlength=64 value=\"$email\"></td>
        <td align=center>$link_email</td>
      </tr><tr>
  <! -- WEB + LINK -->
        <td align=right><font size=-2>$l_web &nbsp;&nbsp;</font></td>
        <td><input name=tf_web size=45 maxlength=64 value=\"$web\"></td>
        <td align=center>$link_web</td>
      </tr>
      </table>

      <table border=0>
      <tr>

  <!-- COMMENT -->
        <td><font size=-2>$l_comment &nbsp;&nbsp;</font><br>
          <textarea name=ta_com rows=6 cols=72>$com</textarea>
        </td>
      </tr><tr>
        <td align=center>
        <table border=0>
        <tr>
          <td>
          $dis_button
          </td>
        </tr>
        </table>
        </td>
      </tr>
      </table>
      </td>
    </tr>
    </table>
  </center>";
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
//                : phone, fax, web, email, com
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

  $sel_kind = "<select name=\"sel_kind\" size=4 >";
  while ($kind_q->next_record()) {
    $label = $kind_q->f("companytype_label");
    $sel_kind .= "\n<option value=\"$label\">$label</option>";
  }
  $sel_kind .= "</select></td>";

  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <form name=\"form_admin_kind_delete\"
      method=post action=\"" . $sess->url("company_index.php") . "\">
    <table width=90% border=1 bgcolor=\"#$col_a_table\">
    <tr>
      <td colspan=5 bgcolor=\"#$col_a_tableh\">
        <i><font color=\"#$col_textem\">$l_kind_manage</font></i>
      </td>
    </tr><tr>
      <td>

<!-- Kind Check Delete Section -->

        <table border=0>
        <tr>
          <td>$l_kind_exist</td>
          <td>
            $sel_kind
          </td>
          <td>
            <input type=hidden name=\"action\" value=\"kind_checklink\">
            <input type=submit name=sub_kind value=\"$l_kind_checkdelete\"
              onClick=\"if (check_kind_checkdel(this.form)) return true;
              else return false;\">
          </td>
        </tr>
    </form>

<!-- Kind Update Section -->

    <form name=\"form_admin_kind_update\"
      method=get action=\"" . $sess->url("company_index.php") . "\">
        <tr>
          <td colspan=2 align=center>
            <input type=hidden name=sel_kind value=\"\">
            <input type=hidden name=action value=\"kind_update\">
            <input type=text name=tf_kind_upd>
          </td><td>
            <input type=submit name=sub_kind value=\"$l_kind_update\"
              onClick=\"if (check_kind_upd(this.form,document.form_admin_kind_delete)) return true; else return false;\">

          </td>
        </tr>
        </table>
      </td>
      <td>
    </form>

<!-- New Kind Section -->

    <form name=\"form_admin_kind_new\"
      method=get action=\"" . $sess->url("company_index.php") . "\">
      <table>
      <tr>
        <td align=right>$l_kind_new : <input name=\"tf_kind_new\"></td>
      </tr><tr>
        <td align=center>
          <input type=hidden name=action value=\"kind_insert\">
          <input type=submit name=sub_kind value=\"$l_kind_insert\"
            onClick=\"if (check_kind_new(this.form)) return true; else return false;\">
        </td>
      </tr>
      </table>
      </td>
    </tr>
    </table>
    </form>
  </center>";

}


///////////////////////////////////////////////////////////////////////////////
// Return the kind delete display form (button)                              //
// Parameters:
//   - $label : kind label
// Retuns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function dis_kind_delete_form($label) {
  global $l_kind_delete;
  global $sess;

  $r = "<form name=\"form_kind_delete\"
          method=post action=\"" . $sess->url("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"kind_delete\">
        <input type=\"hidden\" name=\"hd_kind_label\" value=\"$label\">
        <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_delete\">
        </form>";

  return $r;
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
  $del_form = dis_kind_delete_form($label);

  if ($obm_q->num_rows() > 0) {
    echo "$l_kind_link_company $label";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      echo "<BR><A HREF=\"" .$sess->url("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</A>\n";
    }
    echo "<P>$label : $l_kind_cant_delete";
  } else {
    echo "$l_kind_link_company_no '$label' : $l_kind_can_delete
      <table>
      <tr>
        <td>
        $del_form
        </td>
        <td>
        <form name=form_back method=get
        action=\"" .$sess->url("company_index.php") . "\">
        <input type=hidden name=action value=admin>
        <input type=submit value=\"$l_back\">
        </form>
        </td>
      </tr>
      </table>";
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

  // Company Select
  $sel_company = "<font size=2 face=courier>
    <select name=param_company size=10 style=\"font-family:fixed,courier\">";
  while($comp_q->next_record()) {
    $id = $comp_q->f("company_id");
    $name = $comp_q->f("company_name");
    $name = str_pad($name, 40, ".");
    $town = $comp_q->f("company_town");
    $sel_company .= "\n<option value=\"$id\">$name | $town</option>";
  }
  $sel_company .= "</select></font>";

  // --- HTML Template --------------------------------------------------------

  echo "<center>
    <form method=get name=form_comp
          onSubmit=\"if ($jsfunc) return true; else return false;\"
          action=\"\">
    $title
    <p>
    $sel_company
    <p>
    <input type=\"hidden\" name=\"action\" value=\"new\">
    <input type=submit value=\"$l_select_company\">
    </form>
    </center>";
}


</SCRIPT>
