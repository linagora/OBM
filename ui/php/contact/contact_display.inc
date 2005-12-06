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
$fieldnames["kind_minilabel"] = $l_kind;
$fieldnames["kind_header"] = $l_header;
$fieldnames["kind_lang"] = $l_lang;
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
$fieldnames["contact_fax"] = $l_fax;
$fieldnames["contact_email"] = $l_email;
$fieldnames["contact_archive"] = $l_archive_first;
$fieldnames["contact_date"] = $l_date;
// Calculated fields
$fieldnames["company_name"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// Display Contact specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_contact(&$OD, $fieldname, $link_ok) {
  global $path, $cgp_show, $set_theme, $ico_mail;

  // We set an indicator to tell if the contact has an address
  if ( ($OD->data_set->f("contact_address1") != "")
         || ($OD->data_set->f("contact_address2") != "")
         || ($OD->data_set->f("contact_address3") != "")
         || ($OD->data_set->f("contact_zipcode") != "")
         || ($OD->data_set->f("contact_town") != "")
         || (trim($OD->data_set->f("contact_country_iso3166")) != "")
         || ($OD->data_set->f("contact_expresspostal") != "") ) {
    $contact_has_address = true;
  } else {
    $contact_has_address = false;
  }

  if (($fieldname == "contact_lastname") && $link_ok) {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  else if (($fieldname == "company_name") && $link_ok) {
    if ($cgp_show["module"]["company"]) {
      $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("contact_company_id");
    } else {
      $res["name"] = $OD->data_set->f("contact_company");
    }      
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
    if ($cgp_show["module"]["contact_function"]) {
      $res["name"] = $OD->data_set->f("contact_function");
    }
  }

  else if ($fieldname == "contact_title") {
    if ($cgp_show["module"]["contact_title"]) {
      $res["name"] = $OD->data_set->f("contact_title");
    }
  }

  else if ($fieldname == "contact_phone") {
    $phone = trim($OD->data_set->f("contact_phone"));
    $c_phone = trim($OD->data_set->f("company_phone"));
    if ( (($phone == "") && ($c_phone != ""))
         || ( ($phone != "") && ($phone == $c_phone)) ) {
      $phone = "($c_phone)";
    }
    $res["name"] = $phone;
  }

  else if ($fieldname == "contact_fax") {
    $fax = trim($OD->data_set->f("contact_fax"));
    $c_fax = trim($OD->data_set->f("company_fax"));
    if ( (($fax == "") && ($c_fax != ""))
         || ( ($fax != "") && ($fax == $c_fax)) ) {
      $fax = "($c_fax)";
    }
    $res["name"] = $fax;
  }

  else if ($fieldname == "contact_email") {
    $email = $OD->data_set->f("contact_email");
    if (strcmp($email,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"$email\" />";
      $res["txt_name"] = $email;
    }
  }

  else if ($fieldname == "contact_address") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_service") != "") {
	$res["name"] .= $OD->data_set->f("contact_service")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_service") . " ";
      }
      if ($OD->data_set->f("contact_address1") != "") {
	$res["name"] .= $OD->data_set->f("contact_address1")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_address1") . " ";
      }
      if ($OD->data_set->f("contact_address2") != "") {
	$res["name"] .= $OD->data_set->f("contact_address2")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_address2") . " ";
      }
      if ($OD->data_set->f("contact_address3") != "") {
	$res["name"] .= $OD->data_set->f("contact_address3")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_address3");
      }
      if ($OD->data_set->f("contact_zipcode") != "") {
	$res["name"] .= $OD->data_set->f("contact_zipcode")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_zipcode");
      }
      if ($OD->data_set->f("contact_town") != "") {
	$res["name"] .= $OD->data_set->f("contact_town")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_town");
      }
      if ($OD->data_set->f("contact_expresspostal") != "") {
	$res["name"] .= $OD->data_set->f("contact_expresspostal")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_expresspostal");
      }
      if ($OD->data_set->f("country_name") != "") {
	$res["name"] .= $OD->data_set->f("country_name")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("country_name");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_address1") != "") {
	$res["name"] .= $OD->data_set->f("company_address1")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_address1") . " ";
      }
      if ($OD->data_set->f("company_address2") != "") {
	$res["name"] .= $OD->data_set->f("company_address2")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_address2") . " ";
      }
      if ($OD->data_set->f("company_address3") != "") {
	$res["name"] .= $OD->data_set->f("company_address3")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_address3");
      }
      if ($OD->data_set->f("company_zipcode") != "") {
	$res["name"] .= $OD->data_set->f("company_zipcode")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_zipcode");
      }
      if ($OD->data_set->f("company_town") != "") {
	$res["name"] .= $OD->data_set->f("company_town")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_town");
      }
      if ($OD->data_set->f("company_expresspostal") != "") {
	$res["name"] .= $OD->data_set->f("company_expresspostal")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_expresspostal");
      }
      if ($OD->data_set->f("company_country") != "") {
	$res["name"] .= $OD->data_set->f("company_country")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("company_country");
      }
    }
  }

  else if ($fieldname == "contact_service") {
    $res["name"].= $OD->data_set->f("contact_service");
  }

  else if ($fieldname == "contact_address1") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_address1") != "") {
	$res["name"] .= $OD->data_set->f("contact_address1");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_address1") != "") {
	$res["name"] .= $OD->data_set->f("company_address1");
      }
    }
  }

  else if ($fieldname == "contact_address2") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_address2") != "") {
	$res["name"] .= $OD->data_set->f("contact_address2");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_address2") != "") {
	$res["name"] .= $OD->data_set->f("company_address2");
      }
    }
  }

  else if ($fieldname == "contact_address3") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_address3") != "") {
	$res["name"] .= $OD->data_set->f("contact_address3");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_address3") != "") {
	$res["name"] .= $OD->data_set->f("company_address3");
      }
    }
  }

  else if ($fieldname == "contact_town") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_town") != "") {
	$res["name"] .= $OD->data_set->f("contact_town");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_town") != "") {
	$res["name"] .= $OD->data_set->f("company_town");
      }
    }
  }

  else if ($fieldname == "contact_zipcode") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_zipcode") != "") {
	$res["name"] .= $OD->data_set->f("contact_zipcode");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_zipcode") != "") {
	$res["name"] .= $OD->data_set->f("company_zipcode");
      }
    }
  }

  else if ($fieldname == "contact_expresspostal") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_expresspostal") != "") {
	$res["name"] .= $OD->data_set->f("contact_expresspostal");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_expresspostal") != "") {
	$res["name"] .= $OD->data_set->f("company_expresspostal");
      }
    }
  }

  else if ($fieldname == "country_name") {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f("contact_country") != "") {
	$res["name"] .= $OD->data_set->f("contact_country");
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f("company_country") != "") {
	$res["name"] .= "(".$OD->data_set->f("company_country").")";
	$res["txt_name"] .= $OD->data_set->f("company_country");
      }
    }
  }

  else if ($fieldname == "contact_date") {
    if ($cgp_show["module"]["contact_date"]) {
      $date = $OD->data_set->f("contact_date");
      $res["name"] = ($date) ? date_format($date) : " ";
      $res["align"] = "center";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact search form
// Parameters:
//   - $contact[] : hash with contact values
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_form($contact="") {
  global $cgp_hide;

  if ($cgp_hide["contact"]["responsible"]) {
    $usr_q = "";
  } else {
    $usr_q = run_query_userobm_active();
  }
  $func_q = run_query_function();
  $ctry_q = run_query_country_for_lang();
  $cats1 = of_category_get_ordered("contact", "category1");
  $cats2 = of_category_get_ordered("contact", "category2");
  $cats3 = of_category_get_ordered("contact", "category3");
  $cats4 = of_category_get_ordered("contact", "category4");
  $block .= html_contact_search_form($contact, $usr_q, $func_q, $ctry_q, $cats1, $cats2, $cats3, $cats4);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Contact search Form
// Parameters:
//   - $contact[] : default form values
//     keys used  : name, company
//   - $usr_q     : database object with userobm list
//   - $func_q    : database object with function list
//   - $ctry_q    : database object with country list
//   - $cats1     : array with contact category 1 list
//   - $cats2     : array with contact category 2 list
//   - $cats3     : array with contact category 3 list
//   - $cats4     : array with contact category 4 list
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_form ($contact, $usr_q, $func_q, $ctry_q, $cats1, $cats2, $cats3, $cats4) {
  global $l_contact_name, $l_firstname, $l_phone, $l_from_company, $l_email;
  global $cgp_hide, $l_find, $popup, $display, $c_all, $l_all;
  global $l_function, $l_title, $l_market, $l_town, $l_postcode, $l_country;
  global $l_archive, $l_category1, $l_category2;

  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  $email = stripslashes($contact["email"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $ctry = $contact["country"];
  $func = $contact["function"];
  $title = $contact["title"];
  $mark = $contact["marketing_manager"];
  $cat1 = $contact["category1"];
  $cat2 = $contact["category2"];
  $cat3 = $contact["category3"];
  $cat4 = $contact["category4"];
  $mark = $contact["marketing_manager"];
  $archive = ($contact["archive"] == "1" ? "checked = \"checked\"" : "");

  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);
  // category1, category2, category3, category4   
  
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

  // Title Block
  if ($show_title) {
    $block_title = "
    <div class=\"searchLabel\">$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_town\" size=\"16\"
      value=\"$title\" />
    </div>";
  }

  // Marketing manager select
  if ($show_resp) {
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

    $block_resp = "
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
    </div>";
  }

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

  // Category 1, 2, 3, 4 select
  $block_cat1 = of_dis_block_select_category($cats1, $cat1, "contact", "category1");
  $block_cat2 = of_dis_block_select_category($cats2, $cat2, "contact", "category2");
  $block_cat3 = of_dis_block_select_category($cats3, $cat3, "contact", "category3");
  $block_cat4 = of_dis_block_select_category($cats4, $cat4, "contact", "category4");

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
  <form method=\"get\" name=\"f_search\" action=\"".url_prepare("contact_index.php")."\">
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
    $block_title
    $block_cat1
    $block_cat2
    $block_cat3
    $block_cat4
    $block_resp
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
// Display: Contact search result
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_contact_search_list($contact) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $popup = $contact["popup"];

  $prefs = get_display_pref($auth->auth["uid"], "contact");
  $obm_q = run_query_contact_search($contact, $new_order, $order_dir);
  $nb_contact = $obm_q->num_rows_total();
  if ($nb_contact == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_contact_search_list($obm_q, $prefs, $nb_contact, $contact, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters : 
//   - $obm_q     : list of the contacts to display 
//   - $prefs     : the fields which have to be displayed
//   - $nb_con    : nb contacts returned by the search query 
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_list($obm_q, $prefs, $nb_con, $contact, $popup) {
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
  $cat3 = $contact["category3"];
  $cat4 = $contact["category4"];

  $url = url_prepare("contact_index.php?action=search&amp;tf_lname=$lname&amp;tf_fname=$fname&amp;tf_phone=$phone&amp;tf_email=$email&amp;tf_zip=$zip&amp;tf_town=$town&amp;sel_ctry=$ctry&amp;tf_company=$company&amp;cb_archive=$archive&amp;param_company=$company_id&amp;sel_func=$func&amp;sel_market=$mark&amp;sel_category1=$cat1&amp;sel_category2=$cat2&amp;sel_category3=$cat3&amp;sel_category4=$cat4$url_ext");

  $dis_contact = new OBM_DISPLAY("DATA", $prefs, "contact");
  if ($popup) {
    $dis_contact->display_link = false;
    $dis_contact->data_cb_text = "X";
    $dis_contact->data_idfield = "contact_id";
    $dis_contact->data_cb_name = "cb_con";
    $dis_contact->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $dis_contact->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"param_ext\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $dis_contact->data_set = $obm_q;
  $dis_contact->data_url = $url;
  $dis_contact->data_header = "both";

  // --- HTML Template --------------------------------------------------------
  $display["msg"] .= display_info_msg("$nb_con $l_found");
  $block .= $dis_contact->display("dis_data_contact");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters:
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_contact_consult($contact) {
  global $display, $l_no_found, $path, $l_query_error;

  $view = $contact["view"];
  $id = $contact["id"];
  $con_q = run_query_contact_detail($id);
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg("$l_query_error  - $l_no_found !");
  }
  $display["detailInfo"] = display_record_info($con_q);
  $display["link"] = html_contact_links($id);
  $block .= html_contact_header($con_q);

  if ($view != "") {
    include("$path/$view/".$view."_display.inc");
    include("$path/$view/".$view."_query.inc");
    $block .= dis_subscription_external_list($contact);
  } else {
    $cats1 = of_entitycategories_get($con_q->f("contact_id"), "contact", "category1");
    $cats2 = of_entitycategories_get($con_q->f("contact_id"), "contact", "category2");
    $cats3 = of_entitycategories_get($con_q->f("contact_id"), "contact", "category3");
    $cats4 = of_entitycategories_get($con_q->f("contact_id"), "contact", "category4");
    $block .= html_contact_view_main($con_q, $cats1, $cats2, $cats3, $cats4);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_header($co_q) {
  global $l_company,$l_lastname,$l_firstname,$l_kind, $l_function, $l_title;
  global $l_contact, $l_market, $l_archive, $l_date, $l_no_date;
  global $l_public, $l_private, $l_visibility;
  global $cgp_show, $cgp_hide, $c_yes, $c_no, $set_theme;
  global $display, $path, $action, $auth;

  $c_id = $co_q->f("contact_company_id");
  $usercreate = $co_q->f("contact_usercreate");
  $c_name = $co_q->f("company_name");
  $id = $co_q->f("contact_id");
  $kind_minilabel = $co_q->f("kind_minilabel");
  $kind = "[" . $co_q->f("kind_lang") . "] $kind_minilabel";
  $kind_header = $co_q->f("kind_header");
  if ($kind_header != $kind) $kind .= " ($kind_header)";
  $function = $co_q->f("function_label");
  $title = $co_q->f("contact_title");
  $market = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
  $archive = ($co_q->f("contact_archive") == 1 ? $c_yes : $c_no);
  $priv = $co_q->f("contact_privacy");
  $date = $co_q->f("contact_date");
  $date = ($date != 0) ? date_format($date) : $l_no_date; 
 
 // Conditionnal fields
  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);
  $show_date = (! $cgp_hide["contact"]["contact_date"]);

  $display["title"] = "<div class=\"title\">$l_contact : $lname $fname</div>";

  // Company Display
  if ($cgp_show["module"]["company"]) {
    $dis_company = "<a class=\"detailText\" href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id")."\"> $c_name</a>";
  } else {
    $dis_company = $c_name;
  }

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

  // Responsible field
  if ($show_resp) {
    $block_resp = "
    </tr><tr>
      <td class=\"detailLabel\">$l_market</td>
      <td class=\"detailText\">$market</td>";
  }

  // Date field
  if ($show_date) {
    $block_date = "
    </tr><tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>";
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
      <td class=\"detailText\">$dis_company</td>
    $block_func
    $block_title
    $block_resp
    $block_date
    </tr>
    </table>"; 

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation : main info tab
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_view_main($co_q, $cats1, $cats2, $cats3, $cats4) {
  global $l_phone, $l_hphone, $l_mphone, $l_fax, $l_address, $l_postcode;
  global $l_town, $l_country, $l_email, $l_mailing_ok, $l_expresspostal;
  global $l_comment, $l_comment2, $l_comment3, $l_email_other, $l_service, $l_coord, $ico_clipboard;
  global $c_yes, $c_no, $set_theme, $ico_mail, $l_category1, $l_category2;
  global $display, $cgp_show, $cgp_hide;

  $id = $co_q->f("contact_id");
  $c_phone = $co_q->f("company_phone");
  $c_fax = $co_q->f("company_fax");
  $c_ad1 = $co_q->f("company_address1");
  $c_ad2 = $co_q->f("company_address2");
  $c_ad3 = $co_q->f("company_address3");
  $c_zip = $co_q->f("company_zipcode");
  $c_town = $co_q->f("company_town");
  $c_cdx = $co_q->f("company_expresspostal");
  $c_ctry_name = $co_q->f("company_country_name");
  $kind_minilabel = $co_q->f("kind_minilabel");
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
  if (($ad1 == "") && ($ad2 == "") && ($ad3 == "") && ($zip == "")
      && ($town == "") && ($cdx == "") && ($ctry_name == "")) {
    $ad1 = ("$c_ad1" != "" ? "( $c_ad1 )" : "");
    $ad2 = ("$c_ad2" != "" ? "( $c_ad2 )" : "");
    $ad3 = ("$c_ad3" != "" ? "( $c_ad3 )" : "");
    $zip = ("$c_zip" != "" ? "( $c_zip )" : "");
    $town = ("$c_town" != "" ? "( $c_town )" : "");
    $cdx = ("$c_cdx" != "" ? "( $c_cdx )" : "");
    $ctry_name = $c_ctry_name;
    $copy_addr_comp .= ($c_ad1 != "") ? addslashes($c_ad1) . $newline : "";
    $copy_addr_comp .= ($c_ad2 != "") ? addslashes($c_ad2) . $newline : "";
    $copy_addr_comp .= ($c_ad3 != "") ? addslashes($c_ad3) . $newline : "";
    $copy_addr_comp .= ($c_zip . $c_town != "") ? addslashes("$c_zip $c_town") . $newline : "";
    $copy_addr_comp .= ($c_cdx != "") ? addslashes($c_cdx) . $newline : "";
    $copy_addr_comp .= ($c_ctry_name != "") ? addslashes($c_ctry_name) . $newline : "";
  }
  $phone = trim($co_q->f("contact_phone"));
  if ( (($phone == "") && ($c_phone != "")) ||
       (($phone != "") && ($phone == $c_phone)) ) {
    $phone = "( $c_phone )";
  }
  $hphone = $co_q->f("contact_homephone");
  $mphone = $co_q->f("contact_mobilephone");
  $fax = $co_q->f("contact_fax");
  if ( (($fax == "") && ($c_fax != "")) ||
       (($fax != "") && ($fax == $c_fax)) ) {
    $fax = "( $c_fax )";
  }
  $email = $co_q->f("contact_email");
  $email2 = $co_q->f("contact_email2");
  $mailok = ($co_q->f("contact_mailing_ok") == 1 ? $c_yes : $c_no);
  $com  = beautify_comment(nl2br($co_q->f("contact_comment")));
  $com2 = beautify_comment(nl2br($co_q->f("contact_comment2")));
  $com3 = beautify_comment(nl2br($co_q->f("contact_comment3")));

  // Conditionnal fields
  // category1, category2, category3, category4
  $show_service = (! $cgp_hide["contact"]["contact_service"]);
  $show_ad3 = (! $cgp_hide["contact"]["contact_address3"]);
  $show_cdx = (! $cgp_hide["contact"]["contact_expresspostal"]);
  $show_comment2 = (! $cgp_hide["contact"]["contact_comment2"]);
  $show_comment3 = (! $cgp_hide["contact"]["contact_comment3"]);

  $copy = addslashes("$kind_minilabel $fname $lname") . $newline;
  if ($title != "") {
    $copy .= addslashes($title) . $newline;
  }
  if ($service != "") {
    $copy .= addslashes($service) . $newline;
  }
  if ($c_name != "") {
    $copy .= addslashes($c_name) . $newline;
  }
  // If contact address is empty, get the company one
  if ($copy_addr_comp != "") {
    $copy .= $copy_addr_comp;
  } else {
    if ($ad1 != "") {
      $copy .= addslashes($ad1) . $newline;
    }
    if ($ad2 != "") {
      $copy .= addslashes($ad2) . $newline;
    }
    if ($ad3 != "") {
      $copy .= addslashes($ad3) . $newline;
    }
    if ($zip != "" ) {
      $copy .= addslashes($zip) . " ";
    } else {
      $copy .= " ";
    }
    if ($town != "" ) {
      $copy .= addslashes($town);
    }
    if ($ctry_name != "" ) {
      $copy .= $newline . addslashes($ctry_name);
    }
  }

  // Service field
  if ($show_service) {
    $block_service = "
    </tr><tr>
      <td class=\"detailLabel\">$l_service</td>
      <td class=\"detailText\">$service</td>";
  }

  // Address3 field
  if ($show_ad3) {
    $block_ad3 = "
    </tr><tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailText\">$ad3</td>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailText\">$cdx</td>";
  }

  if ($email != "") {
    $link_email = "
      <a href=\"mailto:$email\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"C\" />
      </a>";
  }

  if ($email2 != "") {
    $link_email2 = "
      <a href=\"mailto:$email2\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"C\" />
      </a>";
  }
  
  // Category 1, 2, 3, 4 field
  $block_cat1 = of_dis_block_consult_category($cats1, "contact", "category1");
  $block_cat2 = of_dis_block_consult_category($cats2, "contact", "category2");
  $block_cat3 = of_dis_block_consult_category($cats3, "contact", "category3");
  $block_cat4 = of_dis_block_consult_category($cats4, "contact", "category4");

  if ($show_comment2) {
    $block_comment2 = "
    <div class=\"detailHead\">$l_comment2</div>
    <table class=\"detail\">
      <tr>
        <td class=\"detailText\" colspan=\"2\">$com2</td>
      </tr>
    </table>";
  }
  if ($show_comment3) {
    $block_comment3 = "
    <div class=\"detailHead\">$l_comment3</div>
    <table class=\"detail\">
      <tr>
        <td class=\"detailText\" colspan=\"2\">$com3</td>
      </tr>
    </table>";
  }


  // --- HTML Template --------------------------------------------------------

  $block = "
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
      <td class=\"detailLabel\">
      $l_address 1
      <a href=\"javascript: return false;\" onclick=\"copy_clip('$copy'); return false;\" title=\"Copy\" ><img alt=\"Copy\" src=\"/images/$set_theme/$ico_clipboard\" /></a>
      </td>
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
    $block_cdx
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
    $block_cat3
    $block_cat4

    <div class=\"detailHead\">$l_comment</div>
    <table class=\"detail\">
      <tr>
        <td class=\"detailText\" colspan=\"2\">$com</td>
      </tr>
    </table>
    $block_comment2 
    $block_comment3 
    ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact links menu
// Parameters:
//   - $id : contact id
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contact_links($id) {
  global $set_theme, $ico_document, $ico_document_new, $ico_deal,$ico_contract;
  global $ico_list, $ico_publication,$ico_publication_new;
  global $l_module_document, $l_contact;
  global $l_module_deal, $l_module_list, $l_module_contract;
  global $l_document_new,$l_document_add;
  global $l_subscription_new, $l_subscription, $l_subscription_list;
  global $path, $cgp_show, $popup_height,$popup_width;

  // Deal
  if ($cgp_show["module"]["deal"]) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;param_contact=$id");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;param_contact=$id&amp;cb_archive=1");
    $deal_active = get_linked_deal_nb($id, "contact1", false, "contact2");
    $deal_total = get_linked_deal_nb($id, "contact1", true, "contact2");
    $block_deal = "
  <div class=\"linkTitle\">:: $l_module_deal</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_deal_list\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" /></a>
        $l_module_deal (
        <a href=\"$url_deal_list\">$deal_active</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
  </ul>";
  }

  // List
  if ($cgp_show["module"]["list"]) {
    $url_list = url_prepare("$path/list/list_index.php?action=search&amp;param_contact=$id");
    $list_nb = get_linked_contact_list_nb($id);
    $block_list = "
  <div class=\"linkTitle\">:: $l_module_list</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_list\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_list\" /></a>
        $l_module_list ( <a href=\"$url_list\">$list_nb</a> )</li>
  </ul>";
  }

  // Contract
  if ($cgp_show["module"]["contract"]) {
    $contract_active = get_linked_contract_nb($id,"contact1",false,"contact2");
    $contract_total = get_linked_contract_nb($id,"contact1",true,"contact2");
    $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;param_contact=$id");
    $url_contract_total = url_prepare("$path/contract/contract_index.php?action=search&amp;param_contact=$id&amp;cb_archive=1");
    $block_contract = "
  <div class=\"linkTitle\">:: $l_module_contract</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_contract\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"\" /></a>
        $l_module_contract (
        <a href=\"$url_contract\">$contract_active</a> /
        <a href=\"$url_contract_total\">$contract_total</a> )</li>
  </ul>";
  }

  // Document  
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=contact");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=contact");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
    $nb_document = run_query_document_nb ($id, "contact");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_module_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Publication  
  if ($cgp_show["module"]["publication"]) {
    $nb_subscription = run_query_subscription_nb($id);
    $url_subscription = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;view=publication&amp;param_contact=$id");
    $url = urlencode($url_subscription);
    $url_subscription_new = url_prepare("$path/publication/publication_index.php?action=new_subscription&amp;param_contact=$id&amp;popup=1&amp;ext_url=$url");
    $block_publi = "
  <div class=\"linkTitle\">:: $l_subscription</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_subscription\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_publication\" alt=\"\" /></a>
        <a href=\"$url_subscription\">$l_subscription_list ($nb_subscription)</a></li>
    <li><a href=\"javascript: void(0);\"  onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_publication_new\" alt=\"\" /></a>
	<a href=\"javascript: void(0);\" onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"> $l_subscription_new</a></li>
  </ul>";
  }
  
  // Links Template  
  $block = "
<div class=\"link\">
  $block_deal
  $block_list
  $block_contract
  $block_doc
  $block_publi
</div>
 ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, priv, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function dis_contact_form($action, $co_q, $contact) {
  global $display, $cg_com, $cgp_hide, $uid;

  $cid = $contact["id"];
  if ($cid > 0) {
    $cats1=array();$cats2=array();$cats3=array();$cats4=array();
    $cats1 = of_category_get_ordered("contact", "category1", $cid);
    $cats2 = of_category_get_ordered("contact", "category2", $cid);
    $cats3 = of_category_get_ordered("contact", "category3", $cid);
    $cats4 = of_category_get_ordered("contact", "category4", $cid);
  }
  $dsrc_q = run_query_datasource();
  $kind_q = run_query_kind();
  $func_q = run_query_function();
  if (! $cgp_hide["contact"]["responsible"]) {
    if ($contact["marketing_manager"]) {
      $users = array($contact["marketing_manager"]);
    }
    $usr_q = run_query_all_users_from_group($cg_com, $users);
  } else {
    // We only get the connected user for comment user select
    $usr_q = run_query_all_users_from_group("", array($uid));
  }
  $ctry_q = run_query_country_for_lang();
  $block = html_contact_form($action, $co_q, $cats1, $cats2, $cats3, $cats4, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact);

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result (company detail on new)
//   - $cats1     : Contact category1 array
//   - $cats2     : Contact category2 array
//   - $cats3     : Contact category3 array
//   - $cats4     : Contact category4 array
//   - $dsrc_q    : datasource database result 
//   - $func_q    : function database result 
//   - $kind_q    : kind database result 
//   - $usr_q     : active userobm database result 
//   - $ctry_q    : country database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, priv, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $cats1, $cats2, $cats3, $cats4, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact) {
  global $set_dsrc, $set_theme, $ico_company, $l_company,$ico_add,$ico_crow;
  global $cgp_mailing_default, $ccontact_private_default;
  global $l_lastname,$l_firstname,$l_kind,$l_function,$l_title, $l_letter;
  global $l_phone, $l_hphone,$l_mphone,$l_fax,$l_postcode,$l_expresspostal;
  global $l_market, $l_address, $l_town, $l_country,$l_email,$l_mailing_ok, $l_date, $l_no_date;
  global $l_comment , $l_add_comment , $l_upd_comment ;
  global $l_comment2, $l_add_comment2, $l_upd_comment2;
  global $l_comment3, $l_add_comment3, $l_upd_comment3;
  global $l_service, $l_email_other;
  global $l_datasource, $l_archive, $l_private,$l_coord, $l_contact, $l_none;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $popup_width, $popup_height;
  global $ico_mini_cal;
  global $l_category1, $l_category2;
  global $display, $path, $auth, $cgp_show, $cgp_hide, $l_none;

  $cat1 = array();
  $cat2 = array();
  $cat3 = array();
  $cat4 = array();
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
    $date = $co_q->f("contact_date");
    $date = ($date != 0) ? date_format($date, 1) : "";
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
    $comment2 = $co_q->f("contact_comment2");
    $usercomment2 = $uid;
    $datecomment2 = isodate_format();
    $comment3 = $co_q->f("contact_comment3");
    $usercomment3 = $uid;
    $datecomment3 = isodate_format();
    $c_phone = $co_q->f("company_phone");
    $c_fax = $co_q->f("company_fax");
    $c_ad1 = $co_q->f("company_address1");
    $c_ad2 = $co_q->f("company_address2");
    $c_ad3 = $co_q->f("company_address3");
    $c_zip = $co_q->f("company_zipcode");
    $c_town = $co_q->f("company_town");
    $c_cdx = $co_q->f("company_expresspostal");
    $c_ctry = $co_q->f("company_country_iso3166");
    $c_ctry_name = $co_q->f("company_country_name");

  // New form and first time
  } elseif ($action == "new") {
    $dsrc = $set_dsrc;
    $mark = $auth->auth["uid"];
    $date = date("Y-m-d");
    $usercomment = $uid;
    $datecomment = isodate_format();
    $usercomment2 = $uid;
    $datecomment2 = isodate_format();
    $usercomment3 = $uid;
    $datecomment3 = isodate_format();
    if ($ccontact_private_default) {
      $private_c = " checked";
    }
    if ($cgp_mailing_default) {
      $mailok = " checked";
    }
    // if company given
    if (is_object($co_q)) {
      // fill the phone with company one
      $c_phone = $co_q->f("company_phone");
      $c_fax = $co_q->f("company_fax");
      $c_ad1 = $co_q->f("company_address1");
      $c_ad2 = $co_q->f("company_address2");
      $c_ad3 = $co_q->f("company_address3");
      $c_zip = $co_q->f("company_zipcode");
      $c_town = $co_q->f("company_town");
      $c_cdx = $co_q->f("company_expresspostal");
      $c_ctry_name = $co_q->f("company_country_name");
      $c_id = $co_q->f("company_id");
      $c_name = $co_q->f("company_name");
      $c_new_id = $c_id;
      $c_new_name = $c_name;
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
  if (isset($contact["date"])) { $mark = $contact["date"]; }
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
  if (isset($contact["comment2"])) { $comment2 = stripslashes($contact["com"]); }
  if (isset($contact["add_comment2"])) { $add_comment2 = stripslashes($contact["add_comment2"]); }
  if (isset($contact["usercomment2"])) { $usercomment2 = $contact["usercomment2"]; }
  if (isset($contact["datecomment2"])) { $datecomment2 = $contact["datecomment2"]; }
  if (isset($contact["comment3"])) { $comment3 = stripslashes($contact["com"]); }
  if (isset($contact["add_comment3"])) { $add_comment3 = stripslashes($contact["add_comment3"]); }
  if (isset($contact["usercomment3"])) { $usercomment3 = $contact["usercomment3"]; }
  if (isset($contact["datecomment3"])) { $datecomment3 = $contact["datecomment3"]; }
  if (isset($contact["category1"])) { $cat1 = $contact["category1"]; }
  if (isset($contact["category2"])) { $cat2 = $contact["category2"]; }  
  if (isset($contact["category2"])) { $cat3 = $contact["category3"]; }  
  if (isset($contact["category2"])) { $cat4 = $contact["category4"]; }  
  if (isset($contact["priv"])) { $priv = stripslashes($contact["priv"]); }
  if (isset($contact["archive"])) { $archive = ($contact["archive"] == 1 ? "checked" : ""); }

  // Conditionnal fields
  // category1,category2,category3,category4
  $show_func = (! $cgp_hide["contact"]["contact_function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);
  $show_date = (! $cgp_hide["contact"]["contact_date"]);
  $show_service = (! $cgp_hide["contact"]["contact_service"]);
  $show_ad3 = (! $cgp_hide["contact"]["contact_address3"]);
  $show_cdx = (! $cgp_hide["contact"]["contact_expresspostal"]);
  $show_comment2 = (! $cgp_hide["contact"]["contact_comment2"]);
  $show_comment3 = (! $cgp_hide["contact"]["contact_comment3"]);

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
    <tr>    
      <td class=\"detailLabel\">$l_function</td> 
      <td class=\"detailForm\">$sel_func</td>
    </tr>";
  }

  // Title field
  if ($show_title) {
    $block_title = "
    <tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_title\" size=\"30\" maxlength=\"64\" value=\"$title\" /></td>
    </tr>";
  }

  // Responsible field
  if ($show_resp) {
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

    $block_resp = "
    <tr>
      <td class=\"detailLabel\">$l_market</td>
      <td class=\"detailForm\">$sel_market</td>
    </tr>";
  }

  // Date field
  if ($show_date) {
    $block_date = "
    </tr><tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailForm\">
        <input type=\"text\" id=\"tf_date\" name=\"tf_date\" maxlength=\"10\" size=\"10\" value=\"$date\" />
        <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_contact.tf_date,'../agenda/agenda_index.php?action=calendar&popup=1') ;\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
        </a>
      </td>
    </tr>";
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

  // Category 1, 2, 3, 4 select field
  $block_cat1 = of_category_dis_entity_form($cats1, "contact", "category1");
  $block_cat2 = of_category_dis_entity_form($cats2, "contact", "category2");
  $block_cat3 = of_category_dis_entity_form($cats3, "contact", "category3");
  $block_cat4 = of_category_dis_entity_form($cats4, "contact", "category4");

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

  // Country select
  $sel_ctry = "<select name=\"sel_ctry\" id=\"sel_ctry\">
    <option value=\"\">$l_none</option>";
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
      <input type=\"text\" id=\"tf_ad3\" name=\"tf_ad3\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad3\" /> $c_ad3
      </td>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_cdx\" size=\"16\" maxlength=\"16\" value=\"$cdx\" /> $c_cdx</td>";
  }

  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $opt_usercomment = "";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $opt_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $opt_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment  = "<select name=\"sel_usercomment\">$opt_usercomment</select>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
    if ($show_comment2) {
      $dis_comment2 = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment2</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com2\" rows=\"6\" cols=\"78\">$comment2</textarea></td>
      ";
    }
    if ($show_comment3) {
      $dis_comment3 = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment3</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com3\" rows=\"6\" cols=\"78\">$comment3</textarea></td>
      ";
    }
  }

  if ($show_comment2) {
    $sel_usercomment2 = "<select name=\"sel_usercomment2\">$opt_usercomment</select>";
    $block_comment2 = "
    <div class=\"detailHead\">$l_comment2</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment2</td>
      <td class=\"detailForm\"><input name=\"tf_datecomment2\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment2\" />$sel_usercomment2</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment2\" rows=\"6\" cols=\"78\">$add_comment2</textarea></td>
      $dis_comment2
    </tr>
    </table>";
  }

  if ($show_comment3) {
    $sel_usercomment3 = "<select name=\"sel_usercomment3\">$opt_usercomment</select>";
    $block_comment3 = "
    <div class=\"detailHead\">$l_comment3</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment3</td>
      <td class=\"detailForm\"><input name=\"tf_datecomment3\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment3\" />$sel_usercomment3</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment3\" rows=\"6\" cols=\"78\">$add_comment3</textarea></td>
      $dis_comment3
    </tr>
    </table>";
  }

  // Company Display
  if ($cgp_show["module"]["company"]) {
    $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"param_company\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_contact.company_new_id&amp;ext_widget_text=f_contact.company_new_name','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"". C_IMAGE_PATH . "/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";
  } else {
    $dis_company = "<input type=\"text\" name=\"tf_company\" size=\"30\" maxlength=\"64\" value=\"$c_name\" />";
  }

  // If new contact or contact update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    if ($priv == '1') {
      $private_c = " checked";
    }
    $display_private = "<input name=\"cb_priv\" type=\"checkbox\" value=\"1\" $private_c />";
  }

  // UPDATE 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"param_contact\" value=\"$id\" />
      <input type=\"hidden\" name=\"hd_usercreate\" value=\"$usercreate\" />
      <input type=\"submit\" value=\"$l_update\" />";

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
      <td class=\"detailForm\">$dis_company</td>
    </tr>
    $block_func
    $block_title
    $block_resp
    $block_date
    $block_cat1
    $block_cat2
    $block_cat3
    $block_cat4
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_phone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$phone\" /> $c_phone</td>
    </tr><tr>
    <td class=\"detailLabel\">$l_hphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_hphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$hphone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_fax</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_fax\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$fax\" /> $c_fax</td>
    </tr><tr>
    <td class=\"detailLabel\">$l_mphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_mphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$mphone\" /></td>
    $block_service
    </tr><tr>
    <td class=\"detailLabel\">$l_address 1</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad1\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /> $c_ad1</td>
    </tr><tr>
    <td class=\"detailLabel\">$l_address 2</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad2\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /> $c_ad2</td>
    $block_ad3
    </tr><tr>
    <td class=\"detailLabel\">$l_postcode</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_zip\" size=\"8\" value=\"$zip\" /> $c_zip</td>
    </tr><tr>
    <td class=\"detailLabel\">$l_town</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_town\" size=\"24\" value=\"$town\" /> $c_town</td>
    $block_cdx
    </tr><tr>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailForm\">$sel_ctry $c_ctry_name</td>
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
    $block_comment2
    $block_comment3

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: context about a contact insertion or update
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
// Display the contact delete validation screen
// Parameters:
//   - $p_id : contact id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_contact($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("contact_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_contact\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_contact\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_delete</p>
    <p class=\"detailButtons\">$dis_back</p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index
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
    $cats1 = of_category_get_ordered("contact", "category1");
    $block .= "<hr />";
    $block .= of_html_admin_cat_form($cats1, "contact", "category1");
  }

  if (! $cgp_hide["contact"]["category2"]) {
    $cats2 = of_category_get_ordered("contact", "category2");
    $block .= "<hr />";  
    $block .= of_html_admin_cat_form($cats2, "contact", "category2");
  }

  if (! $cgp_hide["contact"]["category3"]) {
    $cats3 = of_category_get_ordered("contact", "category3");
    $block .= "<hr />";  
    $block .= of_html_admin_cat_form($cats3, "contact", "category3");
  }

  if (! $cgp_hide["contact"]["category4"]) {
    $cats4 = of_category_get_ordered("contact", "category4");
    $block .= "<hr />";  
    $block .= of_html_admin_cat_form($cats4, "contact", "category4");
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact Category2 section
// Parameters:
//   - $cats2 : Category2 array
///////////////////////////////////////////////////////////////////////////////
function html_contact_cat2_form($cats2) {
  global $l_cat2_manage,$l_cat2_exist,$l_code,$l_label;
  global $l_cat2_checkdelete,$l_cat2_update,$l_cat2_new,$l_cat2_insert;

  $indent = 10;
  $flabel = $indent + 1;
  $sel_cat2 = "<select name=\"sel_cat2\" onChange=\"
  mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf(' ');
    ext = mytext.substr(0, pos);
    mytext = mytext.substr($flabel); 
    forms.form_cat2_update.tf_cat2.value=mytext;
    forms.form_cat2_update.tf_code2.value=ext;
    \"
    size=\"6\" >";
  if (is_array($cats2)) {
    foreach ($cats2 as $one_cat) {
      $c_id = $one_cat["id"];
      $c_code = str_pad($one_cat["code"] . " ", $indent, ".");
      $c_label = htmlentities($one_cat["label"]);
      $sel_cat2 .= "\n<option value=\"$c_id\">$c_code $c_label</option>";
    }
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
// Display: Contact Function section
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
// Displays the function links
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
  global $l_label, $l_lang, $l_default, $l_header;

  if ($kind_q->num_rows() > 0) {

    // Kind select construction
    $sel_kind = "<select name=\"sel_kind\" onChange=\"
      mytext = this.options[this.selectedIndex].text;

      // Get Lang
      pos = mytext.indexOf('-');
      lang = mytext.substr(0, pos);

      // Get Default
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf('-');
      ldefault = mytext.substr(0,pos);

      // Get Label
      mytext = mytext.substr(pos+1);
      pos = mytext.indexOf('-');
      label = mytext.substr(0,pos);

      // Get header
      header = mytext.substr(pos+1);

      forms.form_kind_update.tf_lang.value=lang;
      if (ldefault == 'X') {
        forms.form_kind_update.cb_default.checked=true;
      } else {
        forms.form_kind_update.cb_default.checked=false;
      }
      forms.form_kind_update.tf_label.value=label;
      forms.form_kind_update.tf_header.value=header;\"
      size=\"8\">";

    while ($kind_q->next_record()) {
      $id = $kind_q->f("kind_id");
      $lang = $kind_q->f("kind_lang");
      $default = ($kind_q->f("kind_default") == 1 ? "X" : " ");
      $mlabel = $kind_q->f("kind_minilabel");
      $header = $kind_q->f("kind_header");
      $sel_kind .= "\n<option value=\"$id\">$lang-$default-$mlabel-$header</option>";
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
          <td class=\"adminLabel\">$l_default : </td>
          <td>
            <input type=\"checkbox\" name=\"cb_default\" value=\"1\" />
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
          <td class=\"adminLabel\">$l_default : </td>
          <td>
            <input type=\"checkbox\" name=\"cb_default\" value=\"1\"/>
	  </td>
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
// Display: Kind links
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
// Display the Contact Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_contact_display_pref($prefs) {
  global $l_contact_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
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
