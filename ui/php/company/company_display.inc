<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : company_display.inc                                          //
//     - Desc : Company Display File                                         //
// 2000-01-20 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
 

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["company_name"] = $l_name;
$fieldnames["company_archive"] = $l_archive_first;
$fieldnames["company_address1"] = $l_address;
$fieldnames["company_address2"] = $l_address;
$fieldnames["company_phone"] = $l_phone;
$fieldnames["company_fax"] = $l_fax;
$fieldnames["company_email"] = $l_email;
$fieldnames["company_web"] = $l_web;
// Calculated or indirect fields
$fieldnames["companytype_label"] = $l_kind;
$fieldnames["companyactivity_label"] = $l_activity;
$fieldnames["company_contact_number"] = "<img src=\"/images/$set_theme/$ico_contact\" alt=\"C\" />";
$fieldnames["company_new_contact"] = "<img src=\"/images/$set_theme/$ico_contact_new\" alt=\"NC\" />";
$fieldnames["company_deal_number"] = "<img src=\"/images/$set_theme/$ico_deal\" alt=\"D\" />";


///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_company(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  if ($fieldname == "company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
  }

  else if ($fieldname == "company_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) $res["name"] = "X";
    else $res["name"] = "&nbsp;";
  }

  else if ($fieldname == "company_email") {
    $email = $OD->data_set->f("company_email");
    if (strcmp($email,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\" />";
    }
  } 

  else if ($fieldname == "company_web") {
    $web = $OD->data_set->f("company_web");
    if (strcmp($web,"") != 0) {
      if (strcmp(substr($web,0,5), "http:") != 0) {
	$res["url"] = "http://";
      }
      $res["url"] .= $web;
      $res["name"] = "<img src=\"/images/$set_theme/$ico_web\" alt=\"$web\" />";
    }
  }

  else if ($fieldname == "company_contact_number") {
    $res["name"] = $OD->data_set->f("company_contact_number");
    if ($res["name"] > 0) {
      $res["url"] = "$path/contact/contact_index.php?action=search&amp;param_company=".$OD->data_set->f("company_id")."&amp;tf_company=".urlencode("".$OD->data_set->f("company_name"));
    }
    $res["align"] = "center";
  }

  else if ($fieldname == "company_new_contact") {
    $res["align"] = "center";
    $res["url"] = "$path/contact/contact_index.php?action=new&amp;param_company=".$OD->data_set->f("company_id");
    $res["name"] = "<img src=\"/images/$set_theme/$ico_contact_new\" alt=\"\" />";
  }

  else if ($fieldname == "company_deal_number") {
    $res["name"] = $OD->data_set->f("company_deal_number") . " / ";
    $res["name"] .= $OD->data_set->f("company_deal_total");
    $c_id = $OD->data_set->f("company_id");
    if ($res["name"] != "0 / 0") {
      $res["url"] = "$path/deal/deal_index.php?action=search&amp;param_company=$c_id";
    }
    $res["align"] = "center";
  }
  
  else if ($fieldname == "company_address1") {
    if ($OD->data_set->f("company_address1") != "") {
      $res["name"] .= $OD->data_set->f("company_address1")."<br />\n";
    }
    if ($OD->data_set->f("company_address2") != "") {
      $res["name"] .= $OD->data_set->f("company_address2")."<br />\n";
    }
    if ($OD->data_set->f("company_zipcode") != "" ) {
      $res["name"] .= $OD->data_set->f("company_zipcode") . "&nbsp;&nbsp;";
    } else {
      $res["name"] .= "&nbsp;&nbsp;";
    }
    if ($OD->data_set->f("company_town") != "" ) {
      $res["name"] .= $OD->data_set->f("company_town");
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
// Parameters:
//   - $kind_q    : database object with kind list
//   - $act_q     : database object with activity list
//   - $usr_q     : database object with userobm list
//   - $company[] : default form values
//     keys used  : archive, name, kind, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form ($kind_q, $act_q, $usr_q, $company) {
  global $l_company_name, $l_company_kind, $l_postcode, $l_archive, $l_phone;
  global $l_all, $l_find, $l_market, $l_activity; 
  global $c_all;

  $archive = ($company["archive"] == "1" ? "checked = \"checked\"" : "");
  $name = stripslashes($company["name"]);
  $phone = stripslashes($company["phone"]);
  $kind = $company["kind"];
  $act = $company["activity"];
  $mark = $company["marketing_manager"];
  $zip = stripslashes($company["zip"]);

  // kind select
  $sel_kind = "<select id=\"sel_kind\" name=\"sel_kind\">
    <option value=\"$c_all\">$l_all</option>";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("companytype_id");
    $sel_kind .= "\n<option value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected=\"selected\""; }
    $sel_kind .= ">". $kind_q->f("companytype_label") . "</option>\n";
  }
  $sel_kind .= "</select>";

  // activity select
  $sel_act = "<select id=\"sel_act\" name=\"sel_act\">
    <option value=\"$c_all\">$l_all</option>";
  while ($act_q->next_record()) {
    $a_id = $act_q->f("companyactivity_id");
    $sel_act .= "\n<option value=\"$a_id\"";
    if ($a_id == $act) { $sel_act .= " selected=\"selected\""; }
    $sel_act .= ">". $act_q->f("companyactivity_label") . "</option>\n";
  }
  $sel_act .= "</select>";

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  $url = url_prepare("company_index.php");

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_societe\" id=\"f_societe\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_company_name<br />
      <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"16\"
      value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" id=\"tf_phone\" size=\"8\"
      value=\"$phone\" />
    </div>
    <p class=\"searchLabel\">$l_company_kind<br />
      $sel_kind
    </p>
    <p class=\"searchLabel\">$l_activity<br />
      $sel_act
    </p>
    <p class=\"searchLabel\">$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" id=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </p>
    <p class=\"searchLabel\">$l_market<br />
      $sel_market
    </p>
    <p class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </p>
    <p class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
    </p>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company search result                                         //
// Parameters:
//   - $company[] : company search criteria
//     keys used  : name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_list($company) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "company");
  $obm_q = run_query_search($company, $new_order, $order_dir);
  $nb_company = $obm_q->num_rows();
  if ($nb_company == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_company_search_list($obm_q,$pref_q,$nb_company,$company);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $comp_q     : list of companies
//   - $pref_q     : the fields which have to be displayed
//   - $nb_company : nb companies returned by the search query
//   - $company[]  : company search criteria
//     keys used   : archive, name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function html_company_search_list($comp_q, $pref_q, $nb_company,$company) {
  global $display, $l_found;

  // we urlencode to avoid breakage for space char
  $archive = $company["archive"];
  $name = urlencode(stripslashes($company["name"]));
  $phone = urlencode(stripslashes($company["phone"]));
  $kind = $company["kind"];
  $act = $company["activity"];
  $zip = urlencode(stripslashes($company["zip"]));
  $market = $company["marketing_manager"];
  
  $url = url_prepare("company_index.php?action=search&amp;tf_name=$name&amp;tf_phone=$phone&amp;sel_kind=$kind&amp;sel_act=$act&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_market=$market");

  $comp_q->seek($first_row);

  $comp_d = new OBM_DISPLAY("DATA", $pref_q, "company");
  $comp_d->data_set = $comp_q;
  $comp_d->data_url = $url;
  $comp_d->data_header = "both";
  $display["msg"] = display_info_msg("$nb_company $l_found");
  $block = $comp_d->display("dis_data_company");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Company links menu                                               //
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
  global $set_theme,$ico_contact,$ico_contact_new,$ico_deal,$ico_deal_new;
  // -- Labels
  global $l_contact_list,$l_contact_new,$l_deal_list,$l_deal_new; 
  global $perms_user, $path, $auth;

  $uname = urlencode($name);
  $url_con_list = url_prepare("$path/contact/contact_index.php?action=search&amp;param_company=$p_id&amp;tf_company=$uname");
  $url_con_new = url_prepare("$path/contact/contact_index.php?action=new&amp;param_company=$p_id");
  $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;param_company=$p_id&amp;tf_company_name=$uname");
  $url_deal_new = url_prepare("$path/deal/deal_index.php?action=new&amp;param_company=$p_id");

  $block = "<div class=\"link\"> 
  <div class=\"linkTitle\">:: Contacts associes</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_con_list\"><img src=\"/images/$set_theme/$ico_contact\" /></a>
        <a href=\"$url_con_list\">$l_contact_list ($con_num)</a></li>
    <li><a href=\"$url_con_new\"><img src=\"/images/$set_theme/$ico_contact_new\" /></a>
        <a href=\"$url_con_new\">$l_contact_new</a></li>
  </ul>
  <div class=\"linkTitle\">:: Affaires associees</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_deal_list\"><img src=\"/images/$set_theme/$ico_deal\" /></a>
        <a href=\"$url_deal_list\">$l_deal_list ($deal_num)</a></li>
    <li><a href=\"$url_deal_new\"><img src=\"/images/$set_theme/$ico_deal_new\" /></a>
        <a href=\"$url_deal_new\">$l_deal_new</a></li>
  </ul>
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Company Consultation                                         //
// Parameters:
//   - $co_q   : company database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_consult($co_q) {
  // - Themes
  global $set_theme, $ico_contact, $ico_contact_new, $ico_web, $ico_mail;
  // - Labels
  global $l_contact_list,$l_contact_new,$l_deal_list,$l_deal_new;
  global $l_name,$l_number,$l_kind,$l_activity,$l_archive;
  global $l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal;
  global $l_town,$l_country,$l_email,$l_web,$l_comment,$l_market;
  global $l_insert,$l_update,$l_delete, $l_company, $l_coord;
  global $display, $c_yes, $c_no;

  $id = $co_q->f("company_id");
  $num = $co_q->f("company_number");
  $archive = ($co_q->f("company_archive") == 1 ? $c_yes : $c_no);
  $name = $co_q->f("company_name");
  $kind = $co_q->f("companytype_label");
  $act = $co_q->f("companyactivity_label");
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

  if ($mark > 0)
    $lmarket = get_userobm_name($mark);
  else
    $lmarket = "&nbsp;";

  // Email link
  if ($email != "") {
    $link_email = "<a href=\"mailto:$email\">
      <img border=\"0\" src=\"/images/$set_theme/$ico_mail\"
      alt=\"$email\" /></a>";
  } else {
    $link_email = "&nbsp;";
  }

  // Web link
  if ($web != "") {
    $link_web = "<a target=\"_blank\" href=\"";
    if (strcmp(substr($web,0,5), "http:") != 0) { $link_web .= "http://"; }
    $link_web .= "$web\">
      <img border=\"0\" src=\"/images/$set_theme/$ico_web\"
      alt=\"$web\" /></a>";
  } else {
    $link_web = "&nbsp;";
  }

  $lcom = nl2br($com);
  $display["link"] = html_company_links($id, $name, $con_num, $deal_num);
  $display["title"] = "<div class=\"title\">$l_company : $name</div>";


  // --- HTML Template --------------------------------------------------------
  
  $template = get_template("company_consult.tpl");
  eval ("\$template = \"$template\";");
  $block = $template;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : company database result 
//   - $kind_q    : kind database result 
//   - $usr_q     : userobm database result 
//   - $company[] : default values
//     keys used  : id, num, archive, name, kind, ad1, ad2, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_company_form($action, $co_q, $kind_q, $act_q, $usr_q, $company) {
  global $display;
  global $set_theme,$ico_web,$ico_mail;
  global $l_name,$l_kind,$l_activity,$l_archive,$l_phone,$l_fax,$l_address;
  global $l_postcode,$l_expresspostal,$l_town;
  global $l_country,$l_email,$l_web,$l_comment,$l_number, $l_market, $l_coord;
  global $l_insert,$l_update,$l_checkdelete,$l_internal, $l_company;

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("company_id");
    $num = $co_q->f("company_number");
    $archive = ($co_q->f("company_archive") == 1 ? " checked" : "");
    $name = $co_q->f("company_name");
    $kind = $co_q->f("company_type_id");
    $act = $co_q->f("company_activity_id");
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
  }

  // If parameters have been given, they supercede the default action value
  if (isset($company["id"])) { $id = $company["id"]; }
  if (isset($company["num"])) { $num = stripslashes($company["num"]); }
  if (isset($company["archive"])) { $archive = ($company["archive"] == 1 ? "checked" : ""); }
  if (isset($company["name"])) { $name = stripslashes($company["name"]); }
  if (isset($company["kind"])) { $kind = $company["kind"]; }
  if (isset($company["activity"])) { $act = $company["activity"]; }
  if (isset($company["marketing_manager"])) { $mark = $company["marketing_manager"]; }
  if (isset($company["ad1"])) { $ad1 = stripslashes($company["ad1"]); }
  if (isset($company["ad2"])) { $ad2 = stripslashes($company["ad2"]); }
  if (isset($company["zip"])) { $zip = stripslashes($company["zip"]); }
  if (isset($company["town"])) { $town = stripslashes($company["town"]); }
  if (isset($company["cdx"])) { $cdx = stripslashes($company["cdx"]); }
  if (isset($company["ctry"])) { $ctry = stripslashes($company["ctry"]); }
  if (isset($company["phone"])) { $phone = stripslashes($company["phone"]); }
  if (isset($company["fax"])) { $fax = stripslashes($company["fax"]); }
  if (isset($company["web"])) { $web = stripslashes($company["web"]); }
  if (isset($company["email"])) { $email = stripslashes($company["email"]); }
  if (isset($company["com"])) { $com = stripslashes($company["com"]); }

  // kind select
  $sel_kind = "<select name=\"sel_kind\" id=\"sel_kind\">";
  while($kind_q->next_record()) {
    $k_id = $kind_q->f("companytype_id");
    $sel_kind .= "\n<option value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= " selected=\"selected\""; }
    $sel_kind .= ">". $kind_q->f("companytype_label") . "</option>\n";
  }
  $sel_kind .= "</select>";

  // activity select
  $sel_act = "<select name=\"sel_act\" id=\"sel_act\">";
  while($act_q->next_record()) {
    $a_id = $act_q->f("companyactivity_id");
    $sel_act .= "\n<option value=\"$a_id\"";
    if ($a_id == $act) { $sel_act .= " selected=\"selected\""; }
    $sel_act .= ">". $act_q->f("companyactivity_label") . "</option>\n";
  }
  $sel_act .= "</select>";

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">";
  while($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  // Email link
  if (($action=="detailupdate") || ($action=="update")) { 
    if ($email != "") {
      $link_email = "<a href=\"mailto:$email\">
        <img src=\"/images/$set_theme/$ico_mail\"
        alt=\"$email\" /></a>";
    } else { $link_email = "&nbsp;"; }
  } else { $link_email = "&nbsp;"; }

  // Web link
  if (($action=="detailupdate") || ($action=="update")) {
    if ($web != "") {
      $link_web = "<a target=\"_blank\" href=\"";
      if (strcmp(substr($web,0,5), "http:") != 0) { $link_web .= "http://"; }
      $link_web .= $web . "\">
        <img src=\"/images/$set_theme/$ico_web\"
        alt=\"$web\" /></a>";
    } else { $link_web = "&nbsp;"; }
  } else { $link_web = "&nbsp;"; }

  if (($action == "detailupdate") || ($action == "update")) {
    $links = html_company_links($id, $name, $con_num, $deal_num);
  }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"param_company\" id=\"param_company\" value=\"$id\" />
      <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
      <input type=\"submit\" value=\"$l_update\" /> 
      ";
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button = "
      <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />
      ";
  }

  $display["title"] = "<div class=\"title\">$l_company : $name</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <form method=\"post\" name=\"f_mod_societe\"
      onsubmit=\"if (check_company(this)) return true; else return false;\"
      action=\"".url_prepare("company_index.php")."\">

    <div class=\"detailHead\">$l_company</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_name\" name=\"tf_name\" maxlength=\"50\" size=\"30\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_num\" name=\"tf_num\" value=\"$num\" size=\"30\" maxlength=\"32\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\">$sel_kind</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_activity</td>
      <td class=\"detailForm\">$sel_act</td>
    </tr><tr>    
      <td class=\"detailLabel\">$l_market</td> 
      <td class=\"detailForm\">$sel_market</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_address 1</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ad1\" name=\"tf_ad1\" maxlength=\"30\" size=\"30\" value=\"$ad1\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_address 2</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ad2\" name=\"tf_ad2\" maxlength=\"30\" size=\"30\" value=\"$ad2\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_postcode</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_zip\" name=\"tf_zip\" maxlength=\"14\" size=\"6\" value=\"$zip\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_town</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_town\" name=\"tf_town\" maxlength=\"24\" size=\"24\" value=\"$town\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_cdx\" name=\"tf_cdx\" maxlength=\"8\" size=\"8\" value=\"$cdx\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_country</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ctry\" name=\"tf_ctry\" maxlength=\"24\" size=\"24\" value=\"$ctry\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_phone</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_phone\" name=\"tf_phone\" maxlength=\"16\" size=\"16\" value=\"$phone\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_fax</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_fax\" name=\"tf_fax\" maxlength=\"16\" size=\"16\" value=\"$fax\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_email\" name=\"tf_email\" maxlength=\"64\" size=\"45\" value=\"$email\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_web</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_web\" name=\"tf_web\" maxlength=\"52\" size=\"45\" value=\"$web\" /></td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea id=\"ta_com\" name=\"ta_com\" rows=\"8\" cols=\"80\">$com</textarea></td>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>
   ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Display and check the company links (and delete form if ok)
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $path, $display;

  $delete_ok = true;

  // Links from Contact
  $obm_q = run_query_company_contact_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkc ="
      <tr>
        <td class=\"detailHead\">$l_link_contact</td>
      </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname") . " " . $obm_q->f("contact_firstname");
      $display_linkc .= "
      <tr>
        <td class=\"detailLabel\">
        <a class=\"detail\" href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
        </td>
      </tr>";
    }
  } else {
    $display_linkc = "
      <tr>
        <td class=\"detailHead\">$l_link_contact_no</td>
      </tr>";
  }

  // Links from deals
  $obm_q = run_query_company_deal_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkd = "
      <tr>
        <td class=\"detailHead\">$l_link_deal</td>
      </tr>";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $display_linkd .="<tr>
        <td class=\"detailText\">
        <a class=\"detail\" href=\"" .url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_linkd = "
      <tr>
        <td class=\"detailHead\">$l_link_deal_no</td>
      </tr>";
  }

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("company_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_company\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $block .= "<table class=\"detail\">
   $display_linkc
   $display_linkd
   </table>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_can_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">" . dis_delete_form($p_id) . "</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a company insertion or update                   //
// When similar companies exists we show these and ask confirmation
// Parameters:
//   - $cid       : company id
//   - $co_q      : company database result (at least 1 row)
//   - $company[] : default values
//     keys used  : num, archive, name, kind, ad1, ad2, zip, town, cdx, ctry
//                : phone, fax, web, email, com
/////////////////////////////////////////////////////////////////////////////
function dis_company_warn_insert($cid, $co_q, $company) {
  global $display, $l_check_samecompany, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $num = $company["num"];
  $archive = ($company["archive"] == 1 ? "1" : "0");
  $name = $company["name"];
  $kind = $company["kind"];
  $act = $company["activity"];
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

  $display["msg"] .= display_warn_msg($l_check_samecompany);

  while ($co_q->next_record()) {
    $id = $co_q->f("company_id");
    $samename = $co_q->f("company_name");
    $samezip = $co_q->f("company_zip");
    $disp_same_comp .= "
          <tr><td colspan =\"2\" class=\"detailText\">
             <a class=\"detail\" href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$id") . "\">
              $samename ($samezip)</a>
          </td></tr>";
  }

  $block = "
    <table class=\"detail\">
    $disp_same_comp
    <tr><td>
    <form method=\"post\" id=\"form_insert\" name=\"form_insert\"
    action=\"" .url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_act\" value=\"$act\" />
    <input type=\"hidden\" name=\"tf_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"tf_ad2\" value=\"$ad2\" />
    <input type=\"hidden\" name=\"tf_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"tf_town\" value=\"$town\" />
    <input type=\"hidden\" name=\"tf_cdx\" value=\"$cdx\" />
    <input type=\"hidden\" name=\"tf_ctry\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_web\" value=\"$web\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </td><td>
    <form id=\"form_back\" name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_act\" value=\"$act\" />
    <input type=\"hidden\" name=\"tf_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"tf_ad2\" value=\"$ad2\" />
    <input type=\"hidden\" name=\"tf_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"tf_town\" value=\"$town\" />
    <input type=\"hidden\" name=\"tf_cdx\" value=\"$cdx\" />
    <input type=\"hidden\" name=\"tf_ctry\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_web\" value=\"$web\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </td></tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : company Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"hd_company_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display the company administration index                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $kind_q = run_query_companytype();
  $act_q = run_query_companyactivity();
  $block = html_company_kind_form($kind_q);
  $block .= html_company_activity_form($act_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Company Kind section                                             //
// Parameters:
//   - $kind_q : Kind list database object
///////////////////////////////////////////////////////////////////////////////
function html_company_kind_form($kind_q) {
  global $l_kind_manage,$l_kind_exist;
  global $l_kind_checkdelete,$l_kind_update,$l_kind_new,$l_kind_insert;

  $sel_kind = "<select name=\"sel_kind\" onChange=\"
    forms.form_kind_update.tf_kind.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($kind_q->next_record()) {
    $id = $kind_q->f("companytype_id");
    $label = $kind_q->f("companytype_label");
    $sel_kind .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_kind .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_kind_manage
      </td>
    </tr><tr>
      <td>

<!-- Kind Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_kind_delete\" method=\"post\"
              action=\"" . url_prepare("company_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_kind_exist</td>
              <td class=\"adminLabel\">$sel_kind</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"kind_checklink\" />
              <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_checkdelete\" onclick=\"if (check_kind_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Kind Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_kind_update\" method=\"get\"
              action=\"" . url_prepare("company_index.php") . "\">
            <input type=\"hidden\" name=\"sel_kind\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"kind_update\" />
            <input type=\"text\" name=\"tf_kind\" />
            <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_update\"
              onclick=\"if (check_kind_upd(this.form,document.form_kind_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Kind Section -->

        <form name=\"form_kind_new\" method=\"get\"
          action=\"" . url_prepare("company_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_kind_new :
            <input name=\"tf_kind\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"kind_insert\" />
            <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_insert\"
              onclick=\"if (check_kind_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind links                                                   //
// Parameters:
//   - $company : company hash info : keys used : kind
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($company) {
  global $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;
  global $l_back;

  // $id = get_kind_id($label);
  $id = $company["kind"];
  $label = get_kind_label($id);
  $obm_q = run_query_kind_links($id);

  if ($obm_q->num_rows() > 0) {
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"messageWarning\">$label : $l_kind_cant_delete</td>
    </tr><tr>
      <td>&nbsp;</td>
    </tr><tr>
      <td class=\"detailHead\">$l_kind_link_company $label</td>
    </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .="
    <tr>
      <td class=\"detailText\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"messageOk\">
      $l_kind_link_company_no '$label' : $l_kind_can_delete</td>
    </tr><tr>
      <td>
        <form name=\"form_kind_delete\"
          method=post action=\"" . url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"kind_delete\">
        <input type=\"hidden\" name=\"sel_kind\" value=\"$id\">
        <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_delete\">
        </form>
      </td>
      <td>
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </td>
    </tr>
    </table>";
  }

  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Company Activity section                                         //
// Parameters:
//   - $act_q : Activity list database object
///////////////////////////////////////////////////////////////////////////////
function html_company_activity_form($act_q) {
  global $l_act_manage,$l_act_exist;
  global $l_act_checkdelete,$l_act_update,$l_act_new,$l_act_insert;

  $sel_act = "<select name=\"sel_act\" onChange=\"
    forms.form_act_update.tf_act.value=this.options[this.selectedIndex].text;\"
    size=\"8\">";
  while ($act_q->next_record()) {
    $id = $act_q->f("companyactivity_id");
    $label = $act_q->f("companyactivity_label");
    $sel_act .= "\n<option value=\"$id\">$label</option>";
  }
  $sel_act .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_act_manage
      </td>
    </tr><tr>
      <td>

<!-- Activity Check Delete Section -->
        <table>
        <tr>
          <td colspan=\"3\">
          <form name=\"form_admin_act_delete\" method=\"post\"
            action=\"" . url_prepare("company_index.php") . "\">
          <table>
          <tr>
            <td class=\"adminLabel\">$l_act_exist</td>
            <td class=\"adminLabel\">$sel_act</td>
            <td>
              <input type=\"hidden\" name=\"action\" value=\"activity_checklink\" />
              <input type=\"submit\" name=\"sub_act\" value=\"$l_act_checkdelete\"
                onclick=\"if (check_act_checkdel(this.form)) return true;
                else return false;\" />
            </td>
          </tr>
          </table>
          </form>
          </td>
        </tr>

<!-- Activity Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_act_update\" method=\"get\"
              action=\"" . url_prepare("company_index.php") . "\">
            <input type=\"hidden\" name=\"sel_act\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"activity_update\" />
            <input type=\"text\" name=\"tf_act\" />
            <input type=\"submit\" name=\"sub_act\" value=\"$l_act_update\"
              onclick=\"if (check_act_upd(this.form,document.form_admin_act_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Activity Section -->
        <form name=\"form_act_new\" method=\"get\"
          action=\"" . url_prepare("company_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_act_new :
            <input name=\"tf_act\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"activity_insert\" />
            <input type=\"submit\" name=\"sub_act\" value=\"$l_act_insert\"
              onclick=\"if (check_act_new(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the activity links                                               //
// Parameters:
//   - $company : company hash info : keys used : activity
///////////////////////////////////////////////////////////////////////////////
function dis_activity_links($company) {
  global $l_act_link_company, $l_act_link_company_no;
  global $l_act_delete, $l_act_can_delete, $l_act_cant_delete;
  global $l_back;

  $id = $company["activity"];
  $label = get_activity_label($id);
  $obm_q = run_query_activity_links($id);

  if ($obm_q->num_rows() > 0) {
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"messageWarning\">$label : $l_act_cant_delete</td>
    </tr><tr>
      <td>&nbsp;</td>
    </tr><tr>
      <td class=\"detailHead\">$label : $l_act_link_company</td>
    </tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .= "
    <tr>
      <td class=\"detailText\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"messageOk\">
        $l_act_link_company_no '$label' : $l_act_can_delete</td>
    </tr><tr>
      <td>
        <form name=\"form_act_delete\"
          method=post action=\"" . url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"activity_delete\">
        <input type=\"hidden\" name=\"sel_act\" value=\"$id\">
        <input type=\"submit\" name=\"sub_act\" value=\"$l_act_delete\">
        </form>
      </td>
      <td>
        <form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
      </td>
    </tr>
    </table>";
  }

  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Company Display preference screen                            //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_company_display_pref($display_pref_q) {
  global $l_company_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "company"; 
  $dis_pref->pref_title = $l_company_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Select Form                                               //
// Parameters:
//   - $comp_q : company database result
//   - $title  : Title to be displayed
//   - $url    : URL to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_company($comp_q, $title, $url="") {
  global $l_select_company, $l_close;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }

  // Company Select
  $sel_company = "
    <select name=\"param_company\" size=\"20\" style=\"font-family:fixed\">";
  while($comp_q->next_record()) {
    $id = $comp_q->f("company_id");
    $name = $comp_q->f("company_name");
    $name = str_pad($name, 40, ".");
    $town = $comp_q->f("company_town");
    $sel_company .= "\n<option value=\"$id\">".htmlspecialchars($name)." | ".htmlspecialchars($town)."</option>";
  }
  $sel_company .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">
        <form method=\"get\" name=\"form_comp\"
          onsubmit=\"if ($jsfunc) return true; else return false;\"
          action=\"\">
         $title<p />
         $sel_company<br />
         <input type=\"hidden\" name=\"action\" value=\"new\" /><br />
         <input type=\"submit\" value=\"$l_select_company\" />
        </form>
      </td>
    </tr>
  </table>

  <a href=\"\" onclick='window.close();'>$l_close</a>
    ";

  return $block;
}


</SCRIPT>
