<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : contact_display.php                                          //
//     - Desc : Contact Display File                                         //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["contact_lastname"] = $l_lastname;
$fieldnames["contact_firstname"] = $l_firstname;
$fieldnames["contact_function"] = $l_function;
$fieldnames["contact_title"] = $l_title;
$fieldnames["contact_address"] = $l_address;
$fieldnames["contact_service"] = $l_service;
$fieldnames["contact_address1"] = "$l_address 1";
$fieldnames["contact_address2"] = "$l_address 2";
$fieldnames["contact_address3"] = "$l_address 3";
$fieldnames["contact_zipcode"] = $l_postcode;
$fieldnames["contact_town"] = $l_town;
$fieldnames["contact_expresspostal"] = $l_expresspostal;
$fieldnames["country_name"] = $l_country;
$fieldnames["contact_address"] = $l_address;
$fieldnames["contact_phone"] = $l_phone;
$fieldnames["contact_homephone"] = $l_hphone;
$fieldnames["contact_mobilephone"] = $l_mphone;
$fieldnames["contact_email"] = $l_email;
$fieldnames["contact_archive"] = $l_archive_first;
// Calculated fields
$fieldnames["contact_company_name"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// Display Contact specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_contact(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $ico_mail;

  if (($fieldname == "contact_lastname") && $link_ok) {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  else if (($fieldname == "contact_company_name") && $link_ok) {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("contact_company_id");
  }

  else if ($fieldname == "contact_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "contact_function") {
    $res["name"] = $OD->data_set->f("contact_function");
  }

  else if ($fieldname == "contact_phone") {
    $phone = $OD->data_set->f("contact_phone");
    $c_phone = $OD->data_set->f("company_phone");
    if ( (trim($phone) != "") && ($phone == $c_phone)) {
      $phone = "($phone)";
    }
    $res["name"] = $phone;
  }

  else if ($fieldname == "contact_email") {
    $email = $OD->data_set->f("contact_email");
    if (strcmp($email,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\" />";
      $res["txt_name"] = $email;
    }
  }

  else if ($fieldname == "contact_address") {
    if ($OD->data_set->f("contact_service") != "") {
      $res["name"] .= $OD->data_set->f("contact_service")."<br />\n";
    }
    if ($OD->data_set->f("contact_address1") != "") {
      $res["name"] .= $OD->data_set->f("contact_address1")."<br />\n";
    }
    if ($OD->data_set->f("contact_address2") != "") {
      $res["name"] .= $OD->data_set->f("contact_address2")."<br />\n";
    }
    if ($OD->data_set->f("contact_address3") != "") {
      $res["name"] .= $OD->data_set->f("contact_address3")."<br />\n";
    }
    if ($OD->data_set->f("contact_zipcode") != "" ) {
      $res["name"] .= $OD->data_set->f("contact_zipcode") . " ";
    } else {
      $res["name"] .= " ";
    }
    if ($OD->data_set->f("contact_town") != "" ) {
      $res["name"] .= $OD->data_set->f("contact_town");
    }
    if ($OD->data_set->f("country_name") != "" ) {
      $res["name"] .= " " . $OD->data_set->f("country_name");
    }
    $res["txt_name"] = trim($OD->data_set->f("contact_service") . " " .
                       $OD->data_set->f("contact_address1") . " " .
                       $OD->data_set->f("contact_address2") . " " .
                       $OD->data_set->f("contact_address3") . " " .
                       $OD->data_set->f("contact_zipcode") . " " .
                       $OD->data_set->f("contact_town") . " " .
		       $OD->data_set->f("country_name"));
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact search form                                               //
// Parameters:
//   - $contact[] : hash with contact values
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_form($contact="") {

  $usr_q = run_query_userobm_active();
  $func_q = run_query_function();
  $ctry_q = run_query_country_for_lang();
  $cat1_q = run_query_contactcategory1();
  $cat2_q = run_query_contactcategory2();
  $block .= html_contact_search_form($contact, $usr_q, $func_q, $ctry_q, $cat1_q, $cat2_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Contact search Form                                        //
// Parameters:
//   - $contact[] : default form values
//     keys used  : name, company
//   - $usr_q     : database object with userobm list
//   - $func_q    : database object with function list
//   - $ctry_q    : database object with country list
//   - $cat1_q    : database object with contact category 1 list
//   - $cat2_q    : database object with contact category 2 list
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_form ($contact, $usr_q, $func_q, $ctry_q, $cat1_q, $cat2_q) {
  global $l_contact_name, $l_firstname, $l_phone, $l_from_company, $l_email;
  global $cgp_hide, $l_find, $popup, $display, $c_all, $l_all;
  global $l_function, $l_market, $l_town, $l_postcode, $l_country, $l_archive;
  global $l_category1, $l_category2;

  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  $email = stripslashes($contact["email"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $ctry = $contact["country"];
  $func = $contact["function"];
  $mark = $contact["marketing_manager"];
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];
  $mark = $contact["marketing_manager"];
  $archive = ($contact["archive"] == "1" ? "checked = \"checked\"" : "");

  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_cat1 = (! $cgp_hide["contact"]["category1"]);
  $show_cat2 = (! $cgp_hide["contact"]["category2"]);

  // Function select
  if ($show_func) {
    $sel_func = "<select id=\"sel_func\" name=\"sel_func\">
    <option value=\"$c_all\">$l_all</option>";
    while ($func_q->next_record()) {
      $f_id = $func_q->f("function_id");
      $f_label = $func_q->f("function_label");
      $sel_func .= "\n<option value=\"$f_id\"";
      if ($f_id == $func) { $sel_func .= " selected=\"selected\""; }
      $sel_func .= ">$f_label</option>";
    }
    $sel_func .= "</select>";

    $block_func = "
    <div class=\"searchLabel\">$l_function<br />
      $sel_func
    </div>";
  }

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $u_name = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">$u_name</option>";
  }
  $sel_market .= "</select>";

  // Country select
  $sel_ctry = "<select id=\"sel_ctry\" name=\"sel_ctry\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f("country_iso3166");
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">". $ctry_q->f("country_name") . "</option>\n";
  }
  $sel_ctry .= "</select>";

  // Category 1 select
  if ($show_cat1) {
    $sel_cat1 = "<select id=\"sel_cat1\" name=\"sel_cat1\">
    <option value=\"$c_all\">$l_all</option>";
    if (is_object($cat1_q)) {
      while ($cat1_q->next_record()) {
        $c1_id = $cat1_q->f("contactcategory1_id");
        $c1_code = $cat1_q->f("contactcategory1_code");
        $c1_label = $cat1_q->f("contactcategory1_label");
        $sel_cat1 .= "\n<option value=\"$c1_id\"";
        if ($c1_id == $cat1) { $sel_cat1 .= " selected=\"selected\""; }
        $sel_cat1 .= ">$c1_label</option>";
      }
    }
    $sel_cat1 .= "</select>";

    $block_cat1 = "
    <div class=\"searchLabel\">$l_category1<br />
      $sel_cat1
    </div>";
  }

  // Category 2 select
  if ($show_cat2) {
    $sel_cat2 = "<select id=\"sel_cat2\" name=\"sel_cat2\">
    <option value=\"$c_all\">$l_all</option>";
    if (is_object($cat2_q)) {
      while ($cat2_q->next_record()) {
        $c2_id = $cat2_q->f("contactcategory2_id");
        $c2_code = $cat2_q->f("contactcategory2_code");
        $c2_label = $cat2_q->f("contactcategory2_label");
        $sel_cat2 .= "\n<option value=\"$c2_id\"";
        if ($c2_id == $cat2) { $sel_cat2 .= " selected=\"selected\""; }
        $sel_cat2 .= ">$c2_label</option>";
      }
    }
    $sel_cat2 .= "</select>";

    $block_cat2 = "
    <div class=\"searchLabel\">$l_category2<br />
      $sel_cat2
    </div>";
  }


  if ($popup) {
    $ext_action = $contact["ext_action"];
    $ext_url = $contact["ext_url"];
    $ext_id = $contact["ext_id"];
    $ext_title = $contact["ext_title"];
    $ext_target = $contact["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <form method=\"get\" name=\"f_contact\" action=\"".url_prepare("contact_index.php")."\">
  <div class=\"search\">

    <div class=\"searchLabel\">$l_contact_name<br />
      <input type=\"text\" name=\"tf_lname\" size=\"12\" value=\"$lname\" />
    </div>
    <div class=\"searchLabel\">$l_firstname<br />
      <input type=\"text\" name=\"tf_fname\" size=\"12\" value=\"$fname\" />
    </div>
    <div class=\"searchLabel\">$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"12\" maxlength=\"24\" value=\"$phone\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" value=\"$email\" />
    </div>
    <div class=\"searchLabel\">$l_from_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </div>
    <div class=\"searchLabel\">$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" id=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </div>
    <div class=\"searchLabel\">$l_town<br />
      <input type=\"text\" name=\"tf_town\" id=\"tf_town\" size=\"8\"
      value=\"$town\" />
    </div>
    <div class=\"searchLabel\">$l_country<br />
      $sel_ctry
    </div>
    $block_func
    $block_cat1
    $block_cat2
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
    </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"hd_company_id\" type=\"hidden\" value=\"\" />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext &nbsp;
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result                                            //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_list($contact) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $popup = $contact["popup"];

  $pref_q = run_query_display_pref($auth->auth["uid"], "contact");
  $obm_q = run_query_contact_search($contact, $new_order, $order_dir);
  $nb_contact = $obm_q->num_rows_total();
  if ($nb_contact == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_contact_search_list($obm_q, $pref_q, $nb_contact, $contact, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters : 
//   - $obm_q     : list of the contacts to display 
//   - $pref_q    : the fields which have to be displayed
//   - $nb_con    : nb contacts returned by the search query 
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_list($obm_q, $pref_q, $nb_con, $contact, $popup) {
  global $display, $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $contact["ext_action"];
    $ext_url = $contact["ext_url"];
    $ext_id = $contact["ext_id"];
    $ext_target = $contact["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  // we urlencode to avoid breakage for space char
  $archive = $contact["archive"];
  $lname = urlencode(stripslashes($contact["lname"]));
  $fname = stripslashes($contact["fname"]);
  $phone = urlencode(stripslashes($contact["phone"]));
  $email = stripslashes($contact["email"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $ctry = $contact["country"];
  $company = urlencode(stripslashes($contact["company"]));
  $company_id = $contact["company_id"];
  $func = $contact["function"];
  $mark = $contact["marketing_manager"];
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];

  $url = url_prepare("contact_index.php?action=search&amp;tf_lname=$lname&amp;tf_fname=$fname&amp;tf_phone=$phone&amp;tf_email=$email&amp;tf_zip=$zip&amp;tf_town=$town&amp;sel_ctry=$ctry&amp;tf_company=$company&amp;cb_archive=$archive&amp;param_company=$company_id&amp;sel_func=$func&amp;sel_market=$mark&amp;sel_cat1=$cat1&amp;sel_cat2=$cat2$url_ext");

  $dis_contact_list = new OBM_DISPLAY("DATA", $pref_q, "contact");
  if ($popup) {
    $dis_contact_list->display_link = false;
    $dis_contact_list->data_cb_text = "X";
    $dis_contact_list->data_idfield = "contact_id";
    $dis_contact_list->data_cb_name = "cb_con";
    $display_popup_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $display_popup_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"param_ext\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $dis_contact_list->data_set = $obm_q;
  $dis_contact_list->data_url = $url;
  $dis_contact_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_info_msg("$nb_con $l_found");
  $block = $display_popup_head;
  $block .= $dis_contact_list->display("dis_data_contact");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result                                            //
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_contact_consult($contact) {
  global $auth, $display, $l_no_found, $path, $l_query_error;

  $view = $contact["view"];
  $param_contact = $contact["id"];
  $con_q = run_query_contact_detail($param_contact);
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg("$l_query_error  - $l_no_found !");
  }
  $display["detailInfo"] = display_record_info($con_q);
  if ($view != "") {
    $block .= html_contact_header($con_q);
    include("$path/$view/".$view."_display.inc");
    include("$path/$view/".$view."_query.inc");
    $block .= dis_subscription_external_list($contact);
  } else {
    $cat1_q = run_query_get_contactcategory1_label($con_q->f("contact_id"));
    $cat2_q = run_query_get_contactcategory2_label($con_q->f("contact_id"));
    $block .= html_contact_body($con_q,$cat1_q,$cat2_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation                                         //
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_body($co_q,$cat1_q,$cat2_q) {
  global $l_company,$l_lastname,$l_firstname,$l_kind,$l_function,$l_title;
  global $l_phone, $l_hphone,$l_mphone,$l_fax, $l_address,$l_postcode;
  global $l_market, $l_town,$l_country,$l_email,$l_mailing_ok,$l_expresspostal;
  global $l_public,$l_private,$l_archive,$l_visibility, $l_comment;
  global $l_email_other, $l_service;
  global $l_update,$l_delete,$l_insert, $l_coord, $l_contact;
  global $c_yes, $c_no, $set_theme, $ico_mail,$l_category1,$l_category2;
  global $display, $path, $action, $auth, $cgp_hide;

  $usercreate = $co_q->f("contact_usercreate");
  $c_id = $co_q->f("contact_company_id");
  $c_name = $co_q->f("company_name");
  $c_phone = $co_q->f("company_phone");
  $c_fax = $co_q->f("company_fax");
  $id = $co_q->f("contact_id");
  $kind = "[" . $co_q->f("kind_lang") . "] " . $co_q->f("kind_minilabel");
  $kind_header = $co_q->f("kind_header");
  if ($kind_header != $kind) $kind .= " ($kind_header)";
  $function = $co_q->f("function_label");
  $title = $co_q->f("contact_title");
  $market = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
  $service = $co_q->f("contact_service");
  $ad1 = $co_q->f("contact_address1");
  $ad2 = $co_q->f("contact_address2");
  $ad3 = $co_q->f("contact_address3");
  $zip = $co_q->f("contact_zipcode");
  $town = $co_q->f("contact_town");
  $cdx = $co_q->f("contact_expresspostal");
  $ctry_name = $co_q->f("country_name");
  $func = $co_q->f("contact_function");
  $phone = $co_q->f("contact_phone");
  if ( (trim($phone) != "") && ($phone == $c_phone)) {
    $phone = "( $phone )";
  }
  $hphone = $co_q->f("contact_homephone");
  $mphone = $co_q->f("contact_mobilephone");
  $fax = $co_q->f("contact_fax");
  if ( (trim($fax) != "") && ($fax == $c_fax)) {
    $fax = "( $fax )";
  }
  $email = $co_q->f("contact_email");
  $email2 = $co_q->f("contact_email2");
  $mailok = ($co_q->f("contact_mailing_ok") == 1 ? $c_yes : $c_no);
  $com = beautify_comment(nl2br($co_q->f("contact_comment")));
  $priv = $co_q->f("contact_privacy");
  $archive = ($co_q->f("contact_archive") == 1 ? $c_yes : $c_no);

  $display["link"] = html_contact_links($id);
  // Conditionnal fields
  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_cat1 = (! $cgp_hide["contact"]["category1"]);
  $show_cat2 = (! $cgp_hide["contact"]["category2"]);
  $show_service = (! $cgp_hide["contact"]["service"]);
  $show_ad3 = (! $cgp_hide["contact"]["address3"]);

  // Function field
  if ($show_func) {
    $block_func = "
    </tr><tr>
      <td class=\"detailLabel\">$l_function</td>
      <td class=\"detailText\">$function</td>";
  }

  // Title field
  if ($show_title) {
    $block_title = "
    </tr><tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailText\">$title</td>";
  }

  // Service field
  if ($show_service) {
    $block_service = "
    <tr>
      <td class=\"detailLabel\">$l_service</td>
      <td class=\"detailText\">$service</td>
    </tr>";
  }

  // Address3 field
  if ($show_ad3) {
    $block_ad3 = "
    <tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailText\">$ad3</td>
    </tr>";
  }

  if ($usercreate == $auth->auth["uid"]) {
    if ($priv == 1) 
      $l_priv = $l_private;
    else 
      $l_priv = $l_public;  
    $l_priv = "</tr><tr>
      <td class=\"detailLabel\">$l_visibility</td>
      <td class=\"detailText\">$l_priv</td>";
  }

  if ($email != "") {
    $link_email = "
      <a href=\"mailto:$email\">
      <img src=\"/images/$set_theme/$ico_mail\" alt=\"C\" />
      </a>";
  }

  if ($email2 != "") {
    $link_email2 = "
      <a href=\"mailto:$email2\">
      <img src=\"/images/$set_theme/$ico_mail\" alt=\"C\" />
      </a>";
  }
  
  $display["title"] = "<div class=\"title\">$l_contact : $lname $fname</div>";


  // Category 1 field
  if ($show_cat1) {
    $nb_col_cat1 = 3;
    $dis_cat1 = "<tr>";
    for ($i=0; $i<$nb_col_cat1; $i++) {
      $dis_cat1 .= "<td>&nbsp;</td>";
    }
    $dis_cat1 .= "</tr>";
    $dis_cat1 = "";

    $i = 0;
    while ($cat1_q->next_record()) {
      if ($i==0) {
	$dis_cat1 .= "<tr>";
      }
      $label = $cat1_q->f("contactcategory1_label");
      $i++;
      $dis_cat1 .= "<td style=\"border: 1px solid\" class=\"detailText\"> $label </td>";
      if ($i==$nb_col_cat1) {
	$dis_cat1 .= "</tr>";
	$i = 0;
      }    
    }
    $block_cat1 = "
    <div class=\"detailHead\">$l_category1</div>
    <table class=\"detail\">
    $dis_cat1
    </table>";
  }

  // Category 2 field
  if ($show_cat2) {
    $nb_col_cat2 = 3;
    $dis_cat2 = "<tr>";
    for ($i=0; $i<$nb_col_cat2; $i++) {
      $dis_cat2 .= "<td>&nbsp;</td>";
    }
    $dis_cat2 .= "</tr>";
    $dis_cat2 = "";
    $i = 0;
    while ($cat2_q->next_record()) {
      if ($i==0) {
	$dis_cat2 .= "<tr>";
      }
      $label = $cat2_q->f("contactcategory2_label");      
      $i++;
      $dis_cat2 .= "<td style=\"border: 1px solid\" class=\"detailText\"> $label </td>";
      if ($i==$nb_col_cat2) {
	$dis_cat2 .= "</tr>";
	$i = 0;
      }    
    }
    $block_cat2 = "
    <div class=\"detailHead\">$l_category2</div>
    <table class=\"detail\">
    $dis_cat2
    </table>";
  }


  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detailHead\">$l_contact</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailText\">$lname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailText\">$fname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailText\">$kind</td>
    $l_priv
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">
      <a class=\"detailText\" href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id")."\"> $c_name</a>
      </td>
    $block_func
    $block_title
    </tr><tr>
      <td class=\"detailLabel\">$l_market</td>
      <td class=\"detailText\">$market</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_phone</td>
      <td class=\"detailText\">$phone</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_hphone</td>
      <td class=\"detailText\">$hphone</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_fax</td>
      <td class=\"detailText\">$fax</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_mphone</td>
      <td class=\"detailText\">$mphone</td>
    $block_service
    </tr><tr>
      <td class=\"detailLabel\">$l_address 1</td>
      <td class=\"detailText\">$ad1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_address 2</td>
      <td class=\"detailText\">$ad2</td>
    $block_ad3
    </tr><tr>
      <td class=\"detailLabel\">$l_postcode</td>
      <td class=\"detailText\">$zip</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_town</td>
      <td class=\"detailText\">$town</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailText\">$cdx</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_country</td>
      <td class=\"detailText\">$ctry_name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$link_email $email</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email_other</td>
      <td class=\"detailText\">$link_email2 $email2</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_mailing_ok</td>
      <td class=\"detailText\">$mailok</td>
    </tr>
    </table>

    $block_cat1
    $block_cat2

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailText\" colspan=\"2\">
      $com
      </td>
    </tr>
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation                                         //
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_header($co_q) {
  global $l_company,$l_lastname,$l_firstname,$l_kind;
  global $l_update,$l_delete,$l_insert,$l_contact;
  global $c_yes, $c_no, $set_theme;
  global $display, $path, $action, $auth;

  $usercreate = $co_q->f("contact_usercreate");
  $c_id = $co_q->f("contact_company_id");
  $c_name = $co_q->f("company_name");
  $id = $co_q->f("contact_id");
  $kind = $co_q->f("kind_minilabel");
  $kind_header = $co_q->f("kind_header");
  if ($kind_header != $kind) $kind .= " ($kind_header)";
  $title = $co_q->f("contact_title");
  $market = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
  $display["link"] = html_contact_links($id);
  
  $display["title"] = "<div class=\"title\">$l_contact : $lname $fname</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detailHead\">$l_contact</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailText\">$lname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailText\">$fname</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailText\">$kind</td>
    $l_priv
    </tr><tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">
      <a class=\"detailText\" href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id")."\"> $c_name</a>
      </td>
    </tr>
    </table>"; 

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact links menu
// Parameters:
//   - $deal_q : DBO : information about the deal
//   - $inv_nb : invoices number
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contact_links($id) {
  global $set_theme,$ico_document,$ico_document_new,$ico_publication,$ico_publication_new;
  global $l_header_document,$l_contact;
  global $l_document_new,$l_document_add;
  global $l_subscription_new, $l_subscription, $l_subscription_list;
  global $path, $auth, $cgp_show, $popup_height,$popup_width;

  // Document  
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=contact");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=contact");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
    $nb_document = run_query_document_nb ($id, "contact");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_header_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"/images/$set_theme/$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_header_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"/images/$set_theme/$ico_document_new\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"/images/$set_theme/$ico_document_new\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Publication  
  if ($cgp_show["module"]["publication"]) {
    $nb_subscription = run_query_subscription_nb($id);
    $url_subscription = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;view=publication&amp;param_contact=$id");
    $url_subscription_new = url_prepare("$path/publication/publication_index.php?action=new_subscription&amp;param_contact=$id&amp;popup=1");
    $block_publi = "
  <div class=\"linkTitle\">:: $l_subscription</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_subscription\"><img src=\"/images/$set_theme/$ico_publication\" alt=\"\" /></a>
        <a href=\"$url_subscription\">$l_subscription_list ($nb_subscription)</a></li>
    <li><a  href=\"javascript: void(0);\"  onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"/images/$set_theme/$ico_publication_new\" alt=\"\" /></a>
	<a href=\"javascript: void(0);\" onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"> $l_subscription_new</a></li>
  </ul>";
  }
  
  // Links Template  
  $block = "
<div class=\"link\">
  $block_doc
  $block_publi
</div>
 ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, priv, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function dis_contact_form($action, $co_q, $contact) {
  global $display, $cg_com;

  $cat1_q = run_query_contactcategory1();
  $cat2_q = run_query_contactcategory2();  
  $dsrc_q = run_query_datasource();
  $kind_q = run_query_kind();
  $func_q = run_query_function();
  if ($contact["marketing_manager"]) {
    $users = array($contact["marketing_manager"]);
  }
  $usr_q = run_query_all_users_from_group($cg_com, $users);
  $ctry_q = run_query_country_for_lang();
  $block = html_contact_form($action, $co_q, $cat1_q, $cat2_q, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result (company detail on new)
//   - $cat1_q    : Contact category1 database result 
//   - $cat2_q    : Contact category2 database result 
//   - $dsrc_q    : datasource database result 
//   - $func_q    : function database result 
//   - $kind_q    : kind database result 
//   - $usr_q     : active userobm database result 
//   - $ctry_q    : country database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, priv, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $cat1_q, $cat2_q, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact) {
  global $set_dsrc, $cgp_mailing_default, $set_theme, $ico_company, $l_company,$ico_add;
  global $l_lastname,$l_firstname,$l_kind,$l_function,$l_title, $l_letter;
  global $l_phone, $l_hphone,$l_mphone,$l_fax,$l_postcode,$l_expresspostal;
  global $l_market, $l_address, $l_town, $l_country,$l_email,$l_mailing_ok;
  global $l_comment, $l_add_comment, $l_upd_comment;
  global $l_service, $l_email_other;
  global $l_datasource, $l_archive, $l_private,$l_coord, $l_contact, $l_none;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $popup_width, $popup_height;
  global $l_category1, $l_category2;
  global $display, $path, $auth, $cgp_hide, $l_none;

  $cat1 = array();
  $cat2 = array();
  $uid = $auth->auth["uid"];

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("contact_id");
    $c_id = $co_q->f("contact_company_id");
    $c_name = $co_q->f("company_name");
    $usercreate = $co_q->f("contact_usercreate");
    $dsrc = $co_q->f("contact_datasource_id");
    $kind = $co_q->f("contact_kind_id");
    $mark = $co_q->f("contact_marketingmanager_id");
    $lname = $co_q->f("contact_lastname");
    $fname = $co_q->f("contact_firstname");
    $service = $co_q->f("contact_service");
    $ad1 = $co_q->f("contact_address1");
    $ad2 = $co_q->f("contact_address2");
    $ad3 = $co_q->f("contact_address3");
    $zip = $co_q->f("contact_zipcode");
    $town = $co_q->f("contact_town");
    $cdx = $co_q->f("contact_expresspostal");
    $ctry = $co_q->f("contact_country_iso3166");
    $func = $co_q->f("contact_function_id");
    $title = $co_q->f("contact_title");
    $phone = $co_q->f("contact_phone");
    $hphone = $co_q->f("contact_homephone");
    $mphone = $co_q->f("contact_mobilephone");
    $fax = $co_q->f("contact_fax");
    $email = $co_q->f("contact_email");
    $email2 = $co_q->f("contact_email2");
    $mailok = ($co_q->f("contact_mailing_ok") == 1 ? " checked" : "");
    $archive = ($co_q->f("contact_archive") == 1 ? " checked" : "");
    $priv = $co_q->f("contact_privacy");
    $comment = $co_q->f("contact_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();
    $cat1 = run_query_get_contactcategory1($id);
    $cat2 = run_query_get_contactcategory2($id);

  // New form and first time
  } elseif ($action == "new") {
    $dsrc = $set_dsrc;
    $mark = $auth->auth["uid"];
    $usercomment = $uid;
    $datecomment = isodate_format();
    if ($cgp_mailing_default) {
      $mailok = " checked";
    }
    // if company given
    if (is_object($co_q)) {
      // fill the phone with company one
      $phone = $co_q->f("company_phone");
      $fax = $co_q->f("company_fax");
      $ad1 = $co_q->f("company_address1");
      $ad2 = $co_q->f("company_address2");
      $ad3 = $co_q->f("company_address3");
      $zip = $co_q->f("company_zipcode");
      $town = $co_q->f("company_town");
      $cdx = $co_q->f("company_expresspostal");
      $ctry = $co_q->f("company_country_iso3166");
      $c_id = $co_q->f("company_id");
      $c_name = $co_q->f("company_name");
      $c_new_id = $c_id;
      $c_new_name = $c_name;
      $mark = $contact["marketing_manager"];
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($contact["company_id"])) { $c_id = $contact["company_id"]; }
  if (isset($contact["company_name"])) { $c_name = $contact["company_name"]; }
  if (isset($contact["comp_new_id"])) { $c_new_id = $contact["comp_new_id"]; }
  if (isset($contact["comp_new_name"])) { $c_new_name = $contact["comp_new_name"]; }
  if (isset($contact["id"])) { $id = $contact["id"]; }
  if (isset($contact["usercreate"])) { $usercreate = $contact["usercreate"]; }
  if (isset($contact["datasource"])) { $dsrc = $contact["datasource"]; }
  if (isset($contact["kind"])) { $kind = $contact["kind"]; }
  if (isset($contact["marketing_manager"])) { $mark = $contact["marketing_manager"]; }
  if (isset($contact["lname"])) { $lname = stripslashes($contact["lname"]); }
  if (isset($contact["fname"])) { $fname = stripslashes($contact["fname"]); }
  if (isset($contact["service"])) { $service = stripslashes($contact["service"]); }
  if (isset($contact["ad1"])) { $ad1 = stripslashes($contact["ad1"]); }
  if (isset($contact["ad2"])) { $ad2 = stripslashes($contact["ad2"]); }
  if (isset($contact["ad3"])) { $ad3 = stripslashes($contact["ad3"]); }
  if (isset($contact["zip"])) { $zip = stripslashes($contact["zip"]); }
  if (isset($contact["town"])) { $town = stripslashes($contact["town"]); }
  if (isset($contact["cdx"])) { $cdx = stripslashes($contact["cdx"]); }
  if (isset($contact["ctry"])) { $ctry = $contact["country"]; }
  if (isset($contact["function"])) { $func = $contact["function"]; }
  if (isset($contact["title"])) { $title = stripslashes($contact["title"]); }
  if (isset($contact["phone"])) { $phone = stripslashes($contact["phone"]); }
  if (isset($contact["hphone"])) { $hphone = stripslashes($contact["hphone"]); }
  if (isset($contact["mphone"])) { $mphone = stripslashes($contact["mphone"]); }
  if (isset($contact["fax"])) { $fax = stripslashes($contact["fax"]); }
  if (isset($contact["email"])) { $email = stripslashes($contact["email"]); }
  if (isset($contact["email2"])) { $email2 = stripslashes($contact["email2"]); }
  if (isset($contact["mailok"])) { $mailok = ($contact["mailok"] == 1 ? "checked" : ""); }
  if (isset($contact["comment"])) { $comment = stripslashes($contact["com"]); }
  if (isset($contact["add_comment"])) { $add_comment = stripslashes($contact["add_comment"]); }
  if (isset($contact["usercomment"])) { $usercomment = $contact["usercomment"]; }
  if (isset($contact["datecomment"])) { $datecomment = $contact["datecomment"]; }
  if (isset($company["cat"])) { $compcat = $company["cat"]; }  
  if (isset($contact["category1"])) { $cat1 = $contact["category1"]; }
  if (isset($contact["category2"])) { $cat2 = $contact["category2"]; }  
  if (isset($contact["priv"])) { $priv = stripslashes($contact["priv"]); }
  if (isset($contact["archive"])) { $archive = ($contact["archive"] == 1 ? "checked" : ""); }

  // Conditionnal fields
  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_cat1 = (! $cgp_hide["contact"]["category1"]);
  $show_cat2 = (! $cgp_hide["contact"]["category2"]);
  $show_service = (! $cgp_hide["contact"]["service"]);
  $show_ad3 = (! $cgp_hide["contact"]["address3"]);

  // some constants
  $csize_add = "48";
  $cmax_add = "64";
  $csize_phone = "24";

  // Function field
  if ($show_func) {
    // Function select
    $sel_func = "<select id=\"sel_func\" name=\"sel_func\">
    <option value=\"0\">$l_none</option>";
    while ($func_q->next_record()) {
      $f_id = $func_q->f("function_id");
      $f_label = $func_q->f("function_label");
      $sel_func .= "\n<option value=\"$f_id\"";
      if ($f_id == $func) { $sel_func .= " selected=\"selected\""; }
      $sel_func .= ">$f_label</option>";
    }
    $sel_func .= "</select>";
    $block_func = "
    </tr><tr>    
      <td class=\"detailLabel\">$l_function</td> 
      <td class=\"detailForm\">$sel_func</td>";
  }

  // Title field
  if ($show_title) {
    $block_title = "
    </tr><tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_title\" size=\"30\" maxlength=\"64\" value=\"$title\" /></td>";
  }

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"0\">$l_none</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";
  if ($kind_q->nf()>0) {
    $i=0;
    $j=0;
    $k=0;
    $k_kind_js .= "lang_array = new Array ();\n";
    while ($kind_q->next_record()) {      
      $k_id = $kind_q->f("kind_id");
      $k_lang = $kind_q->f("kind_lang");
      $k_label = $kind_q->f("kind_minilabel");
      $k_header = $kind_q->f("kind_header");
      if ($k_lang != $k_old_lang) {
	$k_kind_js .= "lang_array[\"$k_lang\"] = new Array ();\n";
	$k_old_lang = $k_lang;
      }     
      if ($k_label != $k_old_label) {
	$k_kind_js .= "lang_array[\"$k_lang\"][\"$k_label\"] = new Array ();\n";
	$k_old_label = $k_label;
      }
      $k_kind_js .= "lang_array[\"$k_lang\"][\"$k_label\"][\"$k_header\"] = '$k_id';\n";
      $k_kind[$k_lang][$k_label][$k_header] = $k_id;
      if ($k_id == $kind) {
	$kind_lang = $k_lang;
	$kind_label = $k_label;
	$kind_header = $k_header;
      }
    }
  }
  // kind select
  $sel_kind_header = "<select name=\"sel_header\">";
  $sel_kind_lang = "<select name=\"sel_lang\">";
  $sel_kind_label = "<select name=\"sel_label\">";

  // Category 1 select field
  if ($show_cat1) {
    $sel_cat1 = "<select id=\"sel_cat1\" name=\"sel_cat1[]\" multiple=\"multiple\" style=\"font-family:monospace;font-weight:400;\">";
    while ($cat1_q->next_record()) {
      $c1_id = $cat1_q->f("contactcategory1_id");
      $sel_cat1 .= "\n<option value=\"$c1_id\"";
      if (in_array($c1_id, $cat1)) { $sel_cat1 .= " selected=\"selected\""; }
      $sel_cat1 .= ">". $cat1_q->f("contactcategory1_label") . "</option>\n";
    }
    $sel_cat1 .= "</select>";
    $url = "contact_index.php?action=ext_get_cat1_ids&amp;popup=1&amp;ext_target=sel_cat1";
    $block_cat1 = "
    </tr><tr>
      <td class=\"detailLabel\">$l_category1</td>
      <td class=\"detailForm\">
       <table>
	<tr><td>
	  $sel_cat1
	</td><td>
	  <a href=\"javascript: return false;\" style=\"float:right;\"
	   onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	   <img src=\"/images/$set_theme/$ico_add\" /></a>
	</td></tr>
       </table>      
      </td>";
  }

  // Category 2 select field
  if ($show_cat2) {
    $sel_cat2 = "<select id=\"sel_cat2\" name=\"sel_cat2[]\" multiple=\"multiple\" style=\"font-family:monospace;font-weight:400;\">";
    while ($cat2_q->next_record()) {
      $c2_id = $cat2_q->f("contactcategory2_id");
      $sel_cat2 .= "\n<option value=\"$c2_id\"";
      if (in_array($c2_id, $cat2)) { $sel_cat2 .= " selected=\"selected\""; }
      $sel_cat2 .= ">". $cat2_q->f("contactcategory2_label") . "</option>\n";
    }
    $sel_cat2 .= "</select>";
    $url = "contact_index.php?action=ext_get_cat2_ids&amp;popup=1&amp;ext_target=sel_cat2";
    $block_cat2 = "
    </tr><tr>
      <td class=\"detailLabel\">$l_category2</td>
      <td class=\"detailForm\">
      <table>
	<tr><td>
	  $sel_cat2
	</td><td>
	  <a href=\"javascript: return false;\" style=\"float:right;\"
	   onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	   <img src=\"/images/$set_theme/$ico_add\" /></a>
	</td></tr>
       </table>      
       </td>";
  }

  $i=0;
  $sel_kind_lang = "<select name=\"sel_kind_lang\"
  onchange=\"fill_select_label(this)\">
  <option value=\"0\">$l_none</option>";
  $sel_kind_label = "<select name=\"sel_kind_label\" 
  onchange=\"fill_select_header(this)\">
  <option value=\"0\">$l_none</option>";
  $sel_kind_header = "<select name=\"sel_kind\">
  <option value=\"0\">$l_none</option>";
  foreach( $k_kind as $k_lang =>  $k_kind_label){
    $sel_kind_lang .= "\n<option value=\"$k_lang\"";
    if($k_lang == $kind_lang) {
      foreach($k_kind_label as $k_label =>  $k_kind_header) {
	$sel_kind_label .= "\n<option value=\"$k_label\"";
	if($k_label == $kind_label) {
	  foreach($k_kind_header as $k_header => $k_id) {
	    $sel_kind_header .= "\n<option value=\"$k_id\"";
	    if ($k_id == $kind) { 
	      $sel_kind_lang .= "selected = \"selected\"";
	      $sel_kind_label .= "selected = \"selected\"";
	      $sel_kind_header .= "selected = \"selected\""; 
	    }
	    $sel_kind_header .= ">$k_header</option>";
	  }
	}
	$sel_kind_label .= ">$k_label</option>";
      }
    }
    $sel_kind_lang .= ">$k_lang</option>";
  }
  $sel_kind_label .= "</select>";
  $sel_kind_lang .= "</select>";
  $sel_kind_header .= "</select>";
  $script = "
    <script type=\"text/javascript\">
      $k_kind_js
    </script>\n";

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">
    <option value=\"0\">$l_none</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  // Country select
  $sel_ctry = "<select name=\"sel_ctry\" id=\"sel_ctry\">
    <option value=\"0\">$l_none</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f("country_iso3166");
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">". $ctry_q->f("country_name") . "</option>\n";
  }
  $sel_ctry .= "</select>";

  // Service field
  if ($show_service) {
    $block_service = "
    </tr><tr>
      <td class=\"detailLabel\">$l_service</td>
      <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_service\" name=\"tf_service\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$service\" />
      </td>";
  }

  // Address 3 field
  if ($show_ad3) {
    $block_ad3 = "
    </tr><tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailForm\">
      <input type=\"text\" id=\"tf_ad3\" name=\"tf_ad3\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad3\" />
      </td>";
  }

  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
  }


  // If new contact or contact update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action=="detailupdate") || ($action=="update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    $display_private = "<input name=\"cb_priv\" type=\"checkbox\" value=\"1\" ";
    if ($priv == '1') $display_private .= " checked=\"checked\"";
    $display_private .= " />";
  }

  // UPDATE 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"param_contact\" value=\"$id\" />
      <input type=\"hidden\" name=\"hd_usercreate\" value=\"$usercreate\" />
      <input type=\"submit\" value=\"$l_update\" />
      ";

  // INSERT
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display["title"] = "<div class=\"title\">$l_contact : $lname $fname</div>";

  // --- HTML Template --------------------------------------------------------
  $block = "$script";

  $block .= "
    <form method=\"get\" name=\"f_contact\" onsubmit=\"if (check_contact(this)) return true; else return false;\" action=\"".url_prepare("contact_index.php")."\">

    <div class=\"detailHead\">$l_contact</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_lname\" size=\"30\" maxlength=\"64\" value=\"$lname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_fname\" size=\"30\" maxlength=\"64\" value=\"$fname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\"> $sel_kind_lang $sel_kind_label $l_letter : $sel_kind_header </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_datasource</td> 
      <td class=\"detailForm\">$sel_dsrc</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailForm\">$display_private</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailForm\">
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"param_company\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_contact.company_new_id&amp;ext_widget_text=f_contact.company_new_name','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
    $block_func
    $block_title
    </tr><tr>    
      <td class=\"detailLabel\">$l_market</td> 
      <td class=\"detailForm\">$sel_market</td>
    $block_cat1
    $block_cat2
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_phone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$phone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_hphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_hphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$hphone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_fax</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_fax\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$fax\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_mphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_mphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$mphone\" /></td>
    $block_service
    </tr><tr>
    <td class=\"detailLabel\">$l_address 1</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad1\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_address 2</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad2\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /></td>
    $block_ad3
    </tr><tr>
    <td class=\"detailLabel\">$l_postcode</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_zip\" size=\"8\" value=\"$zip\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_town</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_town\" size=\"24\" value=\"$town\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_expresspostal</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_cdx\" size=\"8\" value=\"$cdx\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailForm\">$sel_ctry</td>
    </tr><tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_email\" size=\"52\" maxlength=\"128\" value=\"$email\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_email_other</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_email2\" size=\"52\" maxlength=\"128\" value=\"$email2\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_mailing_ok</td>
    <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_mailok\" value=\"1\" $mailok /></td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\"><input name=\"tf_datecomment\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment\" />$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
      $dis_comment
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: context about a contact insertion or update                      //
// When similar contacts exist we show these and ask confirmation
// Parameters:
//   - $cid       : contact id
//   - $co_q      : contact database result (at least 1 row)
//   - $contact[] : values for insertion/update (if confirmation)
//     keys used  : company_id, kind, lname, fname ad1, ad2, ad3, zip, town,cdx
//                : ctry, func, phone, hphone, mphone, fax, email, com, priv
///////////////////////////////////////////////////////////////////////////////
function dis_contact_warn_insert($cid, $co_q, $contact) {
  global $l_check_samecontact, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $comp_id = ($contact["comp_new_id"] ? $contact["comp_new_id"] : $contact["company_id"]);
  $kind = $contact["kind"];
  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $dsrc = $contact["datasource"];
  $mark = $contact["marketing_manager"];
  $service = stripslashes($contact["service"]);
  $ad1 = stripslashes($contact["ad1"]);
  $ad2 = stripslashes($contact["ad2"]);
  $ad3 = stripslashes($contact["ad3"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $cdx = stripslashes($contact["cdx"]);
  $ctry = $contact["country"];
  $func = $contact["function"];
  $title = stripslashes($contact["title"]);
  $phone = stripslashes($contact["phone"]);
  $hphone = stripslashes($contact["hphone"]);
  $mphone = stripslashes($contact["mphone"]);
  $fax = stripslashes($contact["fax"]);
  $email = stripslashes($contact["email"]);
  $email2 = stripslashes($contact["email2"]);
  $com = stripslashes($contact["com"]);
  $priv = $contact["priv"];
  $archive = ($contact["archive"] == 1 ? "1" : "0");

  $disp_same_cont = "
    <tr>
      <td colspan =\"2\" class=\"detailHead\">$l_check_samecontact</td>
    </tr>";
  while ($co_q->next_record()) {
    $id = $co_q->f("contact_id");
    $samename = $co_q->f("contact_lastname")." ". $co_q->f("contact_firstname");
    $company = $co_q->f("company_name");
    $disp_same_cont .= "
      <tr>
        <td colspan =\"2\" class=\"detailLabel\">
          <a class=\"detail\" href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$id") . "\">
          $samename ($company)</a>
        </td>
      </tr>";

  }

  // --- HTML Template --------------------------------------------------------

  $hidden = "
    <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
    <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
    <input type=\"hidden\" name=\"tf_lname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_fname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"sel_func\" value=\"$func\" />
    <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
    <input type=\"hidden\" name=\"tf_service\" value=\"$service\" />
    <input type=\"hidden\" name=\"tf_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"tf_ad2\" value=\"$ad2\" />
    <input type=\"hidden\" name=\"tf_ad3\" value=\"$ad3\" />
    <input type=\"hidden\" name=\"tf_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"tf_town\" value=\"$town\" />
    <input type=\"hidden\" name=\"tf_cdx\" value=\"$cdx\" />
    <input type=\"hidden\" name=\"sel_ctry\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_hphone\" value=\"$hphone\" />
    <input type=\"hidden\" name=\"tf_mphone\" value=\"$mphone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"tf_email2\" value=\"$email2\" />
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"hidden\" name=\"cb_priv\" value=\"$priv\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />";

  $block = "
    <table class=\"detail\">
    $disp_same_cont
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" id=\"form_insert\" name=\"form_insert\"
    action=\"" .url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form id=\"form_back\" name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the contact links (and delete form if ok)              //
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function dis_check_contact_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contract, $l_link_contract_no;
  global $l_link_list, $l_link_list_no, $l_link_publication, $l_link_publication_no;
  global $path, $display;

  $delete_ok = true;

  // Links from deals
  $obm_q = run_query_contact_deal_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_d = "<tr><td class=\"detailHead\">$l_link_deal</td></tr>";
    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $display_link_d .= "
      <tr>
        <td class=\"detailLabel\">
          <a class=\"detailLabel\" href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$did")."\">
          $dlabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_link_d = "<tr><td class=\"detailHead\">$l_link_deal_no</td></tr>";
  }

  // Links from Contracts
  $obm_q = run_query_contact_contract_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_c = "<tr><td class=\"detailHead\">$l_link_contract</td></tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $display_link_c .= "
      <tr>
        <td class=\"detailLabel\">
          <a class=\"detailLabel\" href=\"".url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$cid")."\">
          $clabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_link_c = "<tr><td class=\"detailHead\">$l_link_contract_no</td></tr>";
  }

  // Links from lists
  $obm_q = run_query_contact_list_links($p_id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_l = "<tr><td class=\"detailHead\">$l_link_list</td></tr>";
    while ($obm_q->next_record()) {
      $lid = $obm_q->f("list_id");
      $lname = $obm_q->f("list_name");
      $display_link_l .= "
        <tr>
          <td class=\"detailLabel\">
            <a class=\"detailLabel\" href=\"".url_prepare("$path/list/list_index.php?action=detailconsult&amp;param_list=$lid")."\">
            $lname</a>
          </td>
        </tr>";
    }
  } else {
    $display_link_l = "<tr><td class=\"detailHead\">$l_link_list_no</td></tr>";
  }

  // Links from Publications (Subscriptions)
  $obm_q = run_query_contact_publication_links($p_id);
  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_p = "<tr><td class=\"detailHead\">$l_link_publication</td></tr>";
    while ($obm_q->next_record()) {
      $pid = $obm_q->f("publication_id");
      $ptitle = $obm_q->f("publication_title");
      $display_link_p .= "
        <tr>
          <td class=\"detailLabel\">
            <a class=\"detailLabel\" href=\"".url_prepare("$path/publication/publication_index.php?action=detailconsult&amp;param_publication=$pid")."\">
            $ptitle</a>
          </td>
        </tr>";
    }
  } else {
    $display_link_p = "<tr><td class=\"detailHead\">$l_link_publication_no</td></tr>";
  }

  $dis_back = "<form name=\"form_back\" method=\"post\"
     action=\"".url_prepare("contact_index.php") ."\">
     <input type=\"hidden\" name=\"action\" value=\"detailupdate\" />
     <input type=\"hidden\" name=\"param_contact\" value=\"$p_id\" />
     <input type=\"submit\" value=\"$l_back\" />
     </form>";

  $block .= "<table class=\"detail\">
    $display_link_d
    $display_link_c
    $display_link_l
    $display_link_p
    </table>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_can_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">" . dis_contact_delete_form($p_id) . "</p>
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
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function dis_contact_delete_form($p_id) {
  global $l_delete;

  // --- HTML Template --------------------------------------------------------
  return "
    <form name=\"form_delete\" method=\"post\" action=\"" . url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_contact\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
  </form>";
}


///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_contact_admin_index() {
  global $cgp_hide;

  $kind_q = run_query_kind();
  $block .= html_contact_kind_form($kind_q);

  if (! $cgp_hide["contact"]["contact_function"]) {
    $block .= "<hr />";
    $func_q = run_query_function();
    $block .= html_contact_function_form($func_q);
  }

  if (! $cgp_hide["contact"]["category1"]) {
    $cat1_q = run_query_contactcategory1();
    $block .= "<hr />";
    $block .= html_contact_cat1_form($cat1_q);
  }

  if (! $cgp_hide["contact"]["category2"]) {
    $cat2_q = run_query_contactcategory2();
    $block .= "<hr />";  
    $block .= html_contact_cat2_form($cat2_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact Category1 section
// Parameters:
//   - $cat1_q : Category1 list database object
///////////////////////////////////////////////////////////////////////////////
function html_contact_cat1_form($cat1_q) {
  global $l_cat1_manage,$l_cat1_exist,$l_code,$l_lalbel ;
  global $l_cat1_checkdelete,$l_cat1_update,$l_cat1_new,$l_cat1_insert;

  $sel_cat1 = "<select name=\"sel_cat1\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf(' ');
    ext = mytext.substr(0, pos);
    mytext = mytext.substr(pos+1); 
    forms.form_cat1_update.tf_cat1.value=mytext;
    forms.form_cat1_update.tf_code1.value=ext;
    \"
    size=\"4\" >";
  while ($cat1_q->next_record()) {
    $id = $cat1_q->f("contactcategory1_id");
    $label = $cat1_q->f("contactcategory1_label");
    $code = $cat1_q->f("contactcategory1_code");
    $sel_cat1 .= "\n<option value=\"$id\">$code $label</option>";
  }
  $sel_cat1 .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_cat1_manage
      </td>
    </tr><tr>
      <td>

<!-- Category1 Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_cat1_delete\" method=\"post\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_cat1_exist</td>
              <td class=\"adminLabel\">$sel_cat1</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"cat1_checklink\" />
              <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_checkdelete\" onclick=\"if (check_cat1_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category1 Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_cat1_update\" method=\"get\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <input type=\"hidden\" name=\"sel_cat1\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"cat1_update\" />
	    <input type=\"text\" size=\"2\" name=\"tf_code1\" />
   	    <input type=\"text\" name=\"tf_cat1\" />
            <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_update\"
              onclick=\"if (check_cat1_upd(this.form,document.form_cat1_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category1 Section -->

        <form name=\"form_cat1_new\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <table>
	<tr>
	<td class=\"adminLabel\" colspan=\"2\">$l_cat1_new</td>
	</tr>
        <tr>
	    <td class=\"adminLabel\">$l_code :</td>
            <td >
            <input type=\"text\" size=\"2\" name=\"tf_code1\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_lalbel :</td>
            <td >
            <input type=\"text\" name=\"tf_cat1\" /></td>
        </tr><tr>
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"action\" value=\"cat1_insert\" />
            <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_insert\"
              onclick=\"if (check_cat1_new(this.form)) return true; else return false;\" />
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
// Display: Contact Category2 section                                             //
// Parameters:
//   - $cat2_q : Category2 list database object
///////////////////////////////////////////////////////////////////////////////
function html_contact_cat2_form($cat2_q) {
  global $l_cat2_manage,$l_cat2_exist,$l_code,$l_label;
  global $l_cat2_checkdelete,$l_cat2_update,$l_cat2_new,$l_cat2_insert;

  $sel_cat2 = "<select name=\"sel_cat2\" onChange=\"
  mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf(' ');
    ext = mytext.substr(0, pos);
    mytext = mytext.substr(pos+1); 
    forms.form_cat2_update.tf_cat2.value=mytext;
    forms.form_cat2_update.tf_code2.value=ext;
    \"
    size=\"4\" >";
  while ($cat2_q->next_record()) {
    $id = $cat2_q->f("contactcategory2_id");
    $label = $cat2_q->f("contactcategory2_label");
    $code = $cat2_q->f("contactcategory2_code");
    $sel_cat2 .= "\n<option value=\"$id\">$code $label</option>";
  }
  $sel_cat2 .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_cat2_manage
      </td>
    </tr><tr>
      <td>

<!-- Category2 Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_cat2_delete\" method=\"post\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_cat2_exist</td>
              <td class=\"adminLabel\">$sel_cat2</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"cat2_checklink\" />
              <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_checkdelete\" onclick=\"if (check_cat2_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category2 Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_cat2_update\" method=\"get\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <input type=\"hidden\" name=\"sel_cat2\" value=\"\" />
	    <input type=\"text\" size=\"2\" name=\"tf_code2\" />	    
            <input type=\"hidden\" name=\"action\" value=\"cat2_update\" />
            <input type=\"text\" name=\"tf_cat2\" />
            <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_update\"
              onclick=\"if (check_cat2_upd(this.form,document.form_cat2_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category2 Section -->

        <form name=\"form_cat2_new\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <table>
	<tr>
	<td class=\"adminLabel\" colspan=\"2\">$l_cat2_new</td>
	</tr>
        <tr>
	    <td class=\"adminLabel\">$l_code :</td>
            <td ><input type=\"text\" size=\"2\" name=\"tf_code2\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_label :</td>
            <td ><input type=\"text\" name=\"tf_cat2\" /></td>
        </tr><tr>
          <td class=\"adminLabel\" colspan=\"2\">
            <input type=\"hidden\" name=\"action\" value=\"cat2_insert\" />
            <input type=\"submit\" name=\"sub_cat2\" value=\"$l_cat2_insert\"
              onclick=\"if (check_cat2_new(this.form)) return true; else return false;\" />
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
// Display: Contact Function section                                         //
// Parameters:
//   - $func_q : Function list database object
///////////////////////////////////////////////////////////////////////////////
function html_contact_function_form($func_q) {
  global $l_func_manage, $l_func_exist, $l_func_no;
  global $l_func_checkdelete, $l_func_update, $l_func_new, $l_func_insert;

  if ($func_q->num_rows() > 0) {

    // Function select construction
    $sel_func = "<select name=\"sel_func\" onChange=\"
      forms.form_func_update.tf_func.value=this.options[this.selectedIndex].text;\"
      size=\"8\">";
    while ($func_q->next_record()) {
      $id = $func_q->f("function_id");
      $label = $func_q->f("function_label");
      $sel_func .= "\n<option value=\"$id\">$label</option>";
    }
    $sel_func .= "</select>";

    $dis_update_delete = "
<!-- Function Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_func_delete\" method=\"post\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_func_exist</td>
            </tr>
            <tr>
              <td class=\"adminLabel\">$sel_func</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"function_checklink\" />
              <input type=\"submit\" name=\"sub_func\" value=\"$l_func_checkdelete\" onclick=\"if (check_func_checkdel(this.form.sel_func)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Function Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_func_update\" method=\"get\"
              action=\"" . url_prepare("contact_index.php") . "\">
            <input type=\"hidden\" name=\"sel_func\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"function_update\" />
            <input type=\"text\" name=\"tf_func\" />
            <input type=\"submit\" name=\"sub_func\" value=\"$l_func_update\"
              onclick=\"if (check_func_upd(this.form,document.form_func_delete.sel_func)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>";

  } else {
    $dis_update_delete = $l_func_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_func_manage
      </td>
    </tr><tr>
      <td>
        $dis_update_delete
      </td>
      <td>

<!-- New Function Section -->

        <form name=\"form_func_new\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_func_new :
            <input name=\"tf_func\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"function_insert\" />
            <input type=\"submit\" name=\"sub_func\" value=\"$l_func_insert\"
              onclick=\"if (check_func_new(this.form)) return true; else return false;\" />
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
// Display: the cat1 links
// Parameters:
//   - $contact : contact hash info : keys used : cat1
///////////////////////////////////////////////////////////////////////////////
function dis_cat1_links($contact) {
  global $l_cat1_link_contact, $l_cat1_link_contact_no;
  global $l_cat1_delete, $l_cat1_can_delete, $l_cat1_cant_delete;
  global $l_back,$display;

  $id = $contact["category1"];
  $label = get_cat1_label($id);
  $obm_q = run_query_cat1_links($id);

  $nb_cat = $obm_q->num_rows();
  if ($nb_cat > 0) {
    $display["msg"] .= display_warn_msg($l_cat1_cant_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat1_link_contact ($nb_cat)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_cat) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .="
    </table>
      <div class=\"detailButton\">
        <p class=\"detailButtons\">
	  <form name=\"form_back\" method=\"get\"
	    action=\"" .url_prepare("contact_index.php") . "\">
	  <input type=\"hidden\" name=\"action\" value=\"admin\" />
	  <input type=\"submit\" value=\"$l_back\" />
	  </form>
	</p>
      </div>";

  } else {
    $display["msg"] .= display_ok_msg($l_cat1_can_delete);

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $label : $l_cat1_link_contact_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">    
      <form name=\"form_act_delete\"
	method=post action=\"" . url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"cat1_delete\">
      <input type=\"hidden\" name=\"sel_cat1\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_cat1_delete\">
      </form>
      
      <form name=\"form_back\" method=\"get\"
	action=\"" .url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"admin\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  }

  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the cat2 links                                               //
// Parameters:
//   - $contact : contact hash info : keys used : cat2
///////////////////////////////////////////////////////////////////////////////
function dis_cat2_links($contact) {
  global $l_cat2_link_contact, $l_cat2_link_contact_no;
  global $l_cat2_delete, $l_cat2_can_delete, $l_cat2_cant_delete;
  global $l_back,$display;

  $id = $contact["category2"];
  $label = get_cat2_label($id);
  $obm_q = run_query_cat2_links($id);

  $nb_cat = $obm_q->num_rows();
  if ($nb_cat > 0) {
    $display["msg"] .= display_warn_msg($l_cat2_cant_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat2_link_contact ($nb_cat)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_title");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a  href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_cat) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .="
    </table>
      <div class=\"detailButton\">
        <p class=\"detailButtons\">
	  <form name=\"form_back\" method=\"get\"
	    action=\"" .url_prepare("contact_index.php") . "\">
	  <input type=\"hidden\" name=\"action\" value=\"admin\" />
	  <input type=\"submit\" value=\"$l_back\" />
	  </form>
	</p>
      </div>";

  } else {
    $display["msg"] .= display_ok_msg($l_cat2_can_delete);

    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
        $label : $l_cat2_link_contact_no </td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">    
      <form name=\"form_act_delete\"
	method=post action=\"" . url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"cat2_delete\">
      <input type=\"hidden\" name=\"sel_cat2\" value=\"$id\">
      <input type=\"submit\" name=\"sub_act\" value=\"$l_cat2_delete\">
      </form>
      
      <form name=\"form_back\" method=\"get\"
	action=\"" .url_prepare("contact_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"admin\" />
      <input type=\"submit\" value=\"$l_back\" />
      </form>
    </p>
  </div>";

  }

  return $dis_link;
}



///////////////////////////////////////////////////////////////////////////////
// Displays the function links                                               //
// Parameters:
//   - $contact : contact hash info : keys used : function
///////////////////////////////////////////////////////////////////////////////
function dis_function_links($contact) {
  global $display, $l_back, $l_func_link_contact, $l_func_link_company_no;
  global $l_func_delete, $l_func_can_delete, $l_func_cant_delete;

  $delete_ok = true;
  $id = $contact["function"];
  $label = get_function_label($id);
  $obm_q = run_query_function_links($id);

  $nb_func = $obm_q->num_rows();
  if ($nb_func > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_func_link_contact ($nb_func)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname") . " " . $obm_q->f("contact_firstname");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_func) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_func_link_contact_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("contact_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_func_can_delete);
    $dis_del = "<form name=\"form_func_delete\".
          method=\"post\" action=\"" . url_prepare("contact_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"function_delete\" />
        <input type=\"hidden\" name=\"sel_func\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_func\" value=\"$l_func_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_func_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  $block .= "<p>&nbsp;</p>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Kind section
// Parameters:
//   - $kind_q : Kind list database object
///////////////////////////////////////////////////////////////////////////////
function html_contact_kind_form($kind_q) {
  global $l_kind_manage, $l_kind_exist, $l_kind_no;
  global $l_kind_checkdelete, $l_kind_update, $l_kind_new, $l_kind_insert;
  global $l_label, $l_lang, $l_header;

  if ($kind_q->num_rows() > 0) {

    // Kind select construction
    $sel_kind = "<select name=\"sel_kind\" onChange=\"
      mytext = this.options[this.selectedIndex].text;

      // Get Lang
      pos = mytext.indexOf('-');
      lang = mytext.substr(0, pos);

      // Get Label
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf('-');
      label = mytext.substr(0,pos);

      // Get header
      header = mytext.substr(pos+1);

      forms.form_kind_update.tf_lang.value=lang;
      forms.form_kind_update.tf_label.value=label;
      forms.form_kind_update.tf_header.value=header;\"
      size=\"8\">";
    while ($kind_q->next_record()) {
      $id = $kind_q->f("kind_id");
      $lang = $kind_q->f("kind_lang");
      $mlabel = $kind_q->f("kind_minilabel");
      $header = $kind_q->f("kind_header");
      $sel_kind .= "\n<option value=\"$id\">$lang-$mlabel-$header</option>";
    }
    $sel_kind .= "</select>";

    $dis_update_delete = "
<!-- Kind Check Delete Section -->

    <table>
    <tr>
      <td colspan=\"3\">
      <form name=\"form_kind_delete\" method=\"post\"
        action=\"" . url_prepare("contact_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_kind_exist</td>
      </tr>
      <tr>
        <td class=\"adminLabel\">$sel_kind</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"kind_checklink\" />
          <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_checkdelete\"
            onclick=\"if (check_kind_checkdel(this.form.sel_kind)) return true;
            else return false;\" />
        </td>
      </tr>
      </table>
      </form>
      </td>
    </tr>

<!-- Kind Update Section -->

    <tr>
      <td colspan=\"2\">
        <form name=\"form_kind_update\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <input type=\"hidden\" name=\"sel_kind\" value=\"\" />
        <input type=\"hidden\" name=\"action\" value=\"kind_update\" />

        <table>
        <tr>
          <td class=\"adminLabel\">$l_lang : </td>
          <td><input type=\"text\" name=\"tf_lang\" size=\"2\" maxlength=\"2\"/></td>
          <td rowspan=\"3\">
          <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_update\"
            onclick=\"if (check_kind_upd(this.form,document.form_kind_delete.sel_kind)) return true; else return false;\" />
          </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_label : </td>
          <td>
            <input type=\"text\" name=\"tf_label\" />
	  </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_header : </td>
          <td>
            <input type=\"text\" name=\"tf_header\" size=\"24\" maxlength=\"64\" />
          </td>
        </tr>
        </table>

        </form>
      </td>
      <td>&nbsp;</td>
    </tr>
    </table>";
    
  } else {
    $dis_update_delete = $l_kind_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_kind_manage</td>
    </tr>
    <tr>
      <td>
      $dis_update_delete

      </td>
      <td>

<!-- New Kind Section -->

        <form name=\"form_kind_new\" method=\"get\"
          action=\"" . url_prepare("contact_index.php") . "\">
        <table>
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">$l_kind_new</td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_lang : </td>
          <td><input name=\"tf_lang\" size=\"2\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_label : </td>
          <td><input type=\"text\" name=\"tf_label\" size=\"5\" maxlength=\"5\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_header : </td>
          <td><input type=\"text\" name=\"tf_header\" size=\"24\" maxlength=\"64\" /></td>
        </tr>
        <tr>
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
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Kind links                                                //
// Parameters:
//   - $ref : kind hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($ref) {
  global $display, $l_back, $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_link_contact, $l_kind_link_contact_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;

  $delete_ok = true;
  $id = $ref["kind"];
  $name = get_kind_label($id);

  // Contacts Links
  $obm_q = run_query_kind_links($id);
  $nb_kind = $obm_q->num_rows();
  if ($nb_kind > 0) {
    $delete_ok = false;
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_kind_link_contact ($nb_kind)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contact_id");
      $cname = $obm_q->f("contact_lastname")." ".$obm_q->f("contact_firstname");
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_kind) {
      $dis_link_cont .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link_cont = "
    <tr>
      <td class=\"detailHead\">$name : $l_kind_link_contact_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link_cont
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("contact_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_kind_can_delete);
    $dis_del = "<form name=\"form_act_delete\".
          method=\"post\" action=\"" . url_prepare("contact_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"kind_delete\" />
        <input type=\"hidden\" name=\"sel_kind\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_kind_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }


  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display: category1 List
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_category1_list($contact) {
  global $l_close, $l_add_cats1, $l_cat_no;

  $popup = $contact["popup"];
  $cat_q = run_query_contactcategory1();

  $block.= "
  <div class=\"categoryTree\">
    <form onsubmit=\"fill_ext_form2(this);window.close();return false;\">";
  while ($cat_q->next_record()) {
    $label = $cat_q->f("contactcategory1_label");
    $id = $cat_q->f("contactcategory1_id"); 
    $block .= "
    <div class=\"category\">
      <input type=\"checkbox\" name=\"$id\" /><span id=\"data$id\">$label</span>
    </div>
    ";
  } 
  
  $block.= "
  <br />
  <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
  <input name=\"popup\" type=\"hidden\" value=\"1\" />
  <input type=\"submit\" value=\"$l_add_cats1\" /> &nbsp;&nbsp;&nbsp; <input type=\"button\" value=\"$l_close\" onclick=\"window.close();\"/>   
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: category 2 List
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_category2_list($contact) {
  global $l_close,$l_add_cats2,$l_cat_no;

  $popup = $contact["popup"];
  $cat_q = run_query_contactcategory2();
  if ($popup) {
    $link = "<a class=\"categoryRepository\" href=\"\" onclick=\"fill_ext_form('/');return false;\">";
    $link_end ="</a>";
  }
  $name = '/';
  $kind = "0";
  $realpath = $path.$name;
  
  $block .= "
  <div class=\"categoryTree\">
   <form onsubmit=\"fill_ext_form2(this);window.close();return false;\">
   ";
  while ($cat_q->next_record()) {
    $label = $cat_q->f("contactcategory2_label");
    $id = $cat_q->f("contactcategory2_id"); 
    $block .= "<div class=\"category\">
      <input type=\"checkbox\" name=\"$id\" /><span id=\"data$id\">$label</span>
    </div>
    ";
  } 
  
  $block .= "
  <br />
  <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
  <input name=\"popup\" type=\"hidden\" value=\"1\" />
  <input type=\"submit\" value=\"$l_add_cats2\" /> &nbsp;&nbsp;&nbsp; <input type=\"button\" value=\"$l_close\" onclick=\"window.close();\"/>   
  </form>
  </div>";

  return $block;
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

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Vcard Export
// Parameters:
//   - $contact[] : contact id
//     keys used  : contact_id
///////////////////////////////////////////////////////////////////////////////
function dis_vcard_export($contact) {
  global $display, $path, $l_query_error;
  global $auth;

  $param_contact = $contact["id"];
  $con_q = run_query_contact_detail($param_contact);
  $com_q = run_query_contact_company($con_q->f("company_id"));
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
  }
  export_vcard($con_q,$com_q);
}


///////////////////////////////////////////////////////////////////////////////
// Vcard Export
// Parameters:
//   - $con_q : : DBO : information about the contact
///////////////////////////////////////////////////////////////////////////////
function export_vcard($co_q,$com_q) {
  global $obm_version,$set_weekstart_default;

  $id = $co_q->f("contact_id");
  $kind_header = $co_q->f("kind_header");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
  $service = $co_q->f("contact_service");
  
  $c_name = $co_q->f("company_name");
  $c_phone = $co_q->f("company_phone");
  
  if ($c_phone != "")
    $vcard_c_phone = "TEL;WORK;VOICE:$c_phone\n"; 

  $c_fax = $co_q->f("company_fax");
  if ($c_fax != "")
    $vcard_c_fax = "TEL;WORK;FAX:$c_fax\n";

  $function = $co_q->f("function_label");
  if ($function != "")
    $vcard_function = "ROLE:$func\n";

  $title = $co_q->f("contact_title");
  if ($title != "")
    $vcard_title = "TITLE:$title\n";  
    
  $ad1 = $co_q->f("contact_address1");
  $ad2 = $co_q->f("contact_address2");
  $ad3 = $co_q->f("contact_address3");
  $zip = $co_q->f("contact_zipcode");
  $town = $co_q->f("contact_town");
  $cdx = $co_q->f("contact_expresspostal");
  $ctry_name = $co_q->f("country_name");
  if ($ad1 || $ad2 || $ad3 || $zip || $town || $cdx ||$ctry_name) {
    $vcard_contaddr = "ADR;HOME:$cdx;";
    if ($ad2)
      $vcard_contaddr .= "$ad2\\n";
    if ($ad3)
      $vcard_contaddr .= "$ad3";
    $vcard_contaddr .= ";" ;
    if ($ad1)
      $vcard_contaddr .= "$ad1\\n";
    $vcard_contaddr .= ";$town;;$zip;$ctry_name\n";
  }
  $ad1 = $com_q->f("company_address1");
  $ad2 = $com_q->f("company_address2");
  $ad3 = $com_q->f("company_address3");
  $zip = $com_q->f("company_zipcode");
  $town = $com_q->f("company_town");
  $cdx = $com_q->f("company_expresspostal");
  $ctry_name = $com_q->f("country_name");    
  if ($ad1 || $ad2 || $ad3 || $zip || $town || $cdx ||$ctry_name) {
    $vcard_compaddr = "ADR;WORK:$cdx;";
    if ($ad2)
      $vcard_compaddr .= "$ad2\\n";
    if ($ad3)
      $vcard_compaddr .= "$ad3";
    $vcard_compaddr .= ";" ;
    if ($ad1)
     $vcard_compaddr .= "$ad1\\n";
    $vcard_compaddr .= ";$town;;$zip;$ctry_name\n";
  }  

  $phone = $co_q->f("contact_phone");
  if ($phone != "")
    $vcard_phone =  "TEL;WORK;VOICE:$phone\n"; 

  $hphone = $co_q->f("contact_homephone");
  if ($hphone != "")
    $vcard_hphone =  "TEL;HOME;VOICE:$hphone\n";

  $mphone = $co_q->f("contact_mobilephone");
  if ($mphone != "")
    $vcard_mphone = "TEL;CELL:$mphone\n";  

  $fax = $co_q->f("contact_fax");
  if ($fax != "")
    $vcard_fax = "TEL;HOME;FAX:$fax\n"; 

  $email = $co_q->f("contact_email");    
  if ($email != "")
    $vcard_email = "EMAIL;WORK;INTERNET:$email\n";  

  $email2 = $co_q->f("contact_email2");    
  if ($email2 != "")
    $vcard_email2 = "EMAIL;OTHER;INTERNET:$email2\n";      
  
  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=".str_replace(" ","_",$lname).",_".str_replace(" ","_",$fname).".vcf");
  header("charset=utf-8"); 

  $vcard = "BEGIN:VCARD\n";
  $vcard .= "VERSION:3.0\n";
  $vcard .= "$vcard_title";
  $vcard .= "$vcard_function";
  $vcard .= "ORG:$c_name;$service;\n";
  $vcard .= "FN:$kind_header $lname $fname\n";
  $vcard .= "N:$lname;$fname;;$kind_header\n";
  $vcard .= "$vcard_contaddr";
  $vcard .= "$vcard_compaddr";
  $vcard .= "UID:OBM-$id-$fname\n";
  $vcard .= "$vcard_email";
  $vcard .= "$vcard_email2";
  $vcard .= "$vcard_phone";
  $vcard .= "$vcard_hphone";
  $vcard .= "$vcard_mphone";
  $vcard .= "$vcard_fax";
  $vcard .= "$vcard_c_fax";
  $vcard .= "END:VCARD";
  echo iconv("ISO-8859-15","UTF-8",$vcard);
}

</script>
