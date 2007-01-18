<?php
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
$fieldnames["function_label"] = $l_function;
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
$fieldnames["contact_mailing_ok"] = $l_mailing_ok_first;
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

  $show_date = (! $cgp_hide["contact"]["contact_date"]);

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
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$OD->data_set->f("contact_id");
  }

  else if (($fieldname == "company_name") && $link_ok) {
    if ($cgp_show["module"]["company"]) {
      $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f("contact_company_id");
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

  else if ($fieldname == "contact_mailing_ok") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
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
      $res["name"] = "<img src=\"$ico_mail\" alt=\"$email\" />";
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
    if ($show_date) {
      $date = $OD->data_set->f("contact_date");
      $res["name"] = ($date) ? of_date_format($date) : " ";
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
  $funcs = of_category_get_ordered("contact", "function");
  $ctry_q = run_query_global_country_for_lang();
  $dsrc_q = run_query_global_datasource();
  $block .= html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Contact search Form
// Parameters:
//   - $contact[] : default form values
//     keys used  : name, company
//   - $usr_q     : database object with userobm list
//   - $funcs     : array with contact function list
//   - $ctry_q    : database object with country list
//   - $dsrc_q    : database object with datasource list
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q) {
  global $action, $cgp_hide, $l_find, $popup, $display, $c_all, $l_all;
  global $csearch_advanced_default, $l_fuzzy;
  global $l_title, $l_market, $l_town, $l_postcode, $l_country, $l_datasource;
  global $l_after, $l_before, $l_mailing_ok_only, $l_archive;
  global $l_contact_name, $l_firstname, $l_phone, $l_from_company, $l_email;

  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  $email = stripslashes($contact["email"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $ctry = $contact["country"];
  $dsrc = $contact["datasource"];
  $func = $contact["function"];
  $title = $contact["title"];
  $mark = $contact["market"];
  $dateafter = stripslashes($contact["date_after"]);
  $datebefore = stripslashes($contact["date_before"]);
  $mailing_ok_only = ($contact["mailing_ok_only"] == "1" ? "checked = \"checked\"" : "");
  $archive = ($contact["archive"] == "1" ? "checked = \"checked\"" : "");
  if (($contact["fuzzy"] == "1") ||
      ($csearch_advanced_default && $action != "search") ) {
    $fuzzy = "checked = \"checked\"";
  } else {
    $fuzzy = "";
  }
  $company_id = $contact["company_id"];

  $show_func = (! $cgp_hide["contact"]["function"]);
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);

  // User defined data
  $block_userdata .= of_userdata_dis_search("contact", $contact);
  
  // Function select
  $block_function = of_category_dis_search_select("contact", "function", $funcs, $func);

  // Data source select
  $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  // Title Block
  if ($show_title) {
    $block_title = "
    <label>$l_title<br />
      <input type=\"text\" name=\"tf_title\" id=\"tf_title\" size=\"16\"
      value=\"$title\" />
    </label>";
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
    <label>$l_market<br />
      $sel_market
    </label>";
  }

  // Country select
  $sel_ctry = "<select id=\"sel_country\" name=\"sel_country\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f("country_iso3166");
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">". $ctry_q->f("country_name") . "</option>\n";
  }
  $sel_ctry .= "</select>";

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
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"contact_index.php\">
    <label>$l_contact_name<br />
      <input type=\"text\" name=\"tf_lname\" size=\"12\" value=\"$lname\" />
    </label>
    <label>$l_firstname<br />
      <input type=\"text\" name=\"tf_fname\" size=\"12\" value=\"$fname\" />
    </label>
    <label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"12\" maxlength=\"24\" value=\"$phone\" />
    </label>
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" value=\"$email\" />
    </label>
    <label>$l_from_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </label>
    <label>$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" id=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </label>
    <label>$l_town<br />
      <input type=\"text\" name=\"tf_town\" id=\"tf_town\" size=\"8\"
      value=\"$town\" />
    </label>
    <label>$l_country<br />
      $sel_ctry
    </label>
    <label>$l_datasource<br />
      $sel_dsrc
    </label>
    $block_function
    $block_title
    $block_resp
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" class=\"datePicker\" value=\"$dateafter\" />
    </label>
    <label>$l_before<br />
      <input type=\"text\" name=\"tf_date_before\" class=\"datePicker\" value=\"$datebefore\" />
    </label>
    <label>$l_mailing_ok_only<br />
      <input type=\"checkbox\" name=\"cba_mailing_ok_only\" value=\"1\" $mailing_ok_only />
    </label>
    $block_userdata
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>$l_fuzzy<br />
      <input type=\"checkbox\" name=\"cba_fuzzy\" value=\"1\" $fuzzy />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"hd_company_id\" type=\"hidden\" value=\"$company_id\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext &nbsp;
    </label>
    <p class=\"CL\" />
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
  global $display, $l_found, $l_no_found;
  global $auth;

  $popup = $contact["popup"];

  $prefs = get_display_pref($auth->auth["uid"], "contact");
  $obm_q = run_query_contact_search($contact);
  $nb_contact = $obm_q->num_rows_total();
  if ($nb_contact == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_contact $l_found");
    $block = html_contact_search_list($obm_q, $prefs, $contact, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search result
// Parameters : 
//   - $obm_q     : list of contacts to display 
//   - $prefs     : the fields which have to be displayed
//   - $contact[] : contact search criteria
//     keys used  : company_id, name, company
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_list($obm_q, $prefs, $contact, $popup) {
  global $l_add, $l_close;

  if ($popup) {
    $ext_action = $contact["ext_action"];
    $ext_url = $contact["ext_url"];
    $ext_id = $contact["ext_id"];
    $ext_target = $contact["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  // we urlencode to avoid breakage for space char
  $archive = $contact["archive"];
  $advanced = $contact["fuzzy"];
  $mailng_ok_only = $contact["mailng_ok_only"];
  $lname = urlencode(stripslashes($contact["lname"]));
  $fname = stripslashes($contact["fname"]);
  $phone = urlencode(stripslashes($contact["phone"]));
  $email = stripslashes($contact["email"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $ctry = $contact["country"];
  $dateafter = stripslashes($contact["date_after"]);
  $datebefore = stripslashes($contact["date_before"]);
  $company = urlencode(stripslashes($contact["company"]));
  $company_id = $contact["company_id"];
  $func = $contact["function"];
  $title = urlencode(stripslashes($contact["title"]));
  $mark = $contact["market"];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params("contact", $contact);

  $url = url_prepare("contact_index.php?action=search$url_userdata&amp;tf_lname=$lname&amp;tf_fname=$fname&amp;tf_phone=$phone&amp;tf_email=$email&amp;tf_zip=$zip&amp;tf_town=$town&amp;sel_ctry=$ctry&amp;tf_company=$company&amp;cba_archive=$archive&amp;cba_fuzzy=$advanced&amp;cba_mailing_ok_only=$mailing_ok_only&amp;company_id=$company_id&amp;sel_function=$func&amp;tf_title=$title&amp;sel_market=$mark$url_ext");

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
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
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
  $block .= $dis_contact->display("dis_data_contact");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact detail
// Parameters:
//   - $contact[] : contact values (id)
///////////////////////////////////////////////////////////////////////////////
function dis_contact_consult($contact) {
  global $display, $l_no_found, $path, $l_err_reference;

  $view = $contact["view"];
  $id = $contact["contact_id"];
  $con_q = run_query_contact_detail($id);
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  $display["detailInfo"] = display_record_info($con_q);
  $display["link"] = html_contact_links($id);
  $funcs = of_category_get_entitycategories("contact", "function", $id, "mono");
  $block .= html_contact_header($con_q, $funcs);

  if ($view != "") {
    include("$path/$view/".$view."_display.inc");
    include("$path/$view/".$view."_query.inc");
    $block .= dis_publication_subscription_external_list($contact);
  } else {
    $block .= html_contact_view_main($con_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation
// Parameters:
//   - $co_q : contact database result
//   - $funcs     : array with contact function list
///////////////////////////////////////////////////////////////////////////////
function html_contact_header($co_q, $funcs) {
  global $l_company, $l_lastname, $l_firstname, $l_aka, $l_kind, $l_title;
  global $l_contact, $l_market, $l_archive, $l_date, $l_no_date;
  global $cgp_show, $cgp_hide, $c_yes, $c_no, $set_theme;
  global $display, $path, $action, $auth;

  $c_id = $co_q->f("contact_company_id");
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
  $aka = $co_q->f("contact_aka");
  $archive = ($co_q->f("contact_archive") == 1 ? $c_yes : $c_no);
  $date = $co_q->f("contact_date");
  $date = ($date != 0) ? of_date_format($date) : $l_no_date; 
 
  // Display Aka only if non empty
  if ($aka != "") {
    $dis_aka = "
    <tr> 
      <th>$l_aka</th>
      <td>$aka</td>
    </tr>";
  } else {
    $dis_aka = "";
  }

  // Conditionnal fields
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);
  $show_date = (! $cgp_hide["contact"]["contact_date"]);

  $display["title"] = "<h1 class=\"title\">$l_contact : $lname $fname</h1>";

  // Function
  $block_function = of_category_dis_block_consult("contact", "function", $funcs, "mono");

  // Company Display
  if ($cgp_show["module"]["company"]) {
    $dis_company = "<a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\"> $c_name</a>";
  } else {
    $dis_company = $c_name;
  }

  // Title field
  if ($show_title) {
    $block_title = "
    <tr>
      <th>$l_title</th>
      <td>$title</td>";
  }

  // Responsible field
  if ($show_resp) {
    $block_resp = "
    </tr><tr>
      <th>$l_market</th>
      <td>$market</td>";
  }

  // Date field
  if ($show_date) {
    $block_date = "
    </tr><tr>
      <th>$l_date</th>
      <td>$date</td>";
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detail infos\">
    <h1>$l_contact</h1>
    
    <table>
    <tr>
      <th>$l_lastname</th>
      <td>$lname</td>
    </tr><tr>
      <th>$l_firstname</th>
      <td>$fname</td>
    </tr>
    $dis_aka
    <tr>
      <th>$l_kind</th>
      <td>$kind</td>
    </tr><tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr><tr>
      <th>$l_company</th>
      <td>$dis_company</td>
    $block_function
    $block_title
    $block_resp
    $block_date
    </tr>
    </table>
    </div>"; 

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Consultation : main info tab
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_view_main($co_q) {
  global $l_phone, $l_hphone, $l_mphone, $l_fax, $l_address, $l_postcode;
  global $l_town, $l_country, $l_email, $l_mailing_ok, $l_expresspostal;
  global $l_comment1, $l_comment2, $l_comment3, $l_email_other;
  global $l_service, $l_coord, $ico_clipboard;
  global $c_yes, $c_no, $set_theme, $ico_mail;
  global $display, $cgp_show, $cgp_hide, $cclipboard_address;

  $id = $co_q->f("contact_id");
  $title = $co_q->f("contact_title");
  $c_name = $co_q->f("company_name");
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
  $phone = trim($co_q->f("contact_phone"));
  $hphone = $co_q->f("contact_homephone");
  $mphone = $co_q->f("contact_mobilephone");
  $fax = $co_q->f("contact_fax");
  $email = $co_q->f("contact_email");
  $email2 = $co_q->f("contact_email2");
  $mailok = ($co_q->f("contact_mailing_ok") == 1 ? $c_yes : $c_no);
  $com  = beautify_comment(nl2br($co_q->f("contact_comment")));
  $com2 = beautify_comment(nl2br($co_q->f("contact_comment2")));
  $com3 = beautify_comment(nl2br($co_q->f("contact_comment3")));

  $newline = "\\r\\n";
  if (($ad1 == "") && ($ad2 == "") && ($ad3 == "") && ($zip == "")
      && ($town == "") && ($cdx == "") && ($ctry_name == "")) {
    $ad1 = ("$c_ad1" != "" ? "( $c_ad1 )" : "");
    $ad2 = ("$c_ad2" != "" ? "( $c_ad2 )" : "");
    $ad3 = ("$c_ad3" != "" ? "( $c_ad3 )" : "");
    $zip = ("$c_zip" != "" ? "( $c_zip )" : "");
    $town = ("$c_town" != "" ? "( $c_town )" : "");
    $cdx = ("$c_cdx" != "" ? "( $c_cdx )" : "");
    $ctry_name = $c_ctry_name;
    if (($c_ad1 != "") && (in_array("address1", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad1).$newline;
    }
    if (($c_ad2 != "") && (in_array("address2", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad2).$newline;
    }
    if (($c_ad3 != "") && (in_array("address3", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad3).$newline;
    }
    if (($c_zip != "") && (in_array("zipcode", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_zip);
      $copy_zip = true;
    }
    if (($c_town != "") && (in_array("town", $cclipboard_address))) {
      $copy_addr_comp .= ($copy_zip ? " " : "") . addslashes($c_town);
      $copy_town = true;
    }
    if ($copy_zip || $copy_town) {
      $copy_addr_comp .= $newline;
    }
    if (($c_cdx != "") && (in_array("expresspostal", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_cdx).$newline;
    }
    if (($c_ctry_name != "") && (in_array("country", $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ctry_name).$newline;
    }
  }
  if ( (($phone == "") && ($c_phone != "")) ||
       (($phone != "") && ($phone == $c_phone)) ) {
    $phone = "( $c_phone )";
  }
  if ( (($fax == "") && ($c_fax != "")) ||
       (($fax != "") && ($fax == $c_fax)) ) {
    $fax = "( $c_fax )";
  }

  // Conditionnal fields
  $show_service = (! $cgp_hide["contact"]["contact_service"]);
  $show_ad3 = (! $cgp_hide["contact"]["contact_address3"]);
  $show_cdx = (! $cgp_hide["contact"]["contact_expresspostal"]);
  $show_comment2 = (! $cgp_hide["contact"]["contact_comment2"]);
  $show_comment3 = (! $cgp_hide["contact"]["contact_comment3"]);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult("contact", $id);

  $copy = addslashes("$kind_minilabel $fname $lname") . $newline;
  if (($title != "")  && (in_array("title", $cclipboard_address))) {
    $copy .= addslashes($title) . $newline;
  }
  if ($c_name != "") {
    $copy .= addslashes($c_name) . $newline;
  }
  if ($service != "") {
    $copy .= addslashes($service) . $newline;
  }
  // If contact address is empty, get the company one
  if ($copy_addr_comp != "") {
    $copy .= $copy_addr_comp;
  } else {
    if (($ad1 != "") && (in_array("address1", $cclipboard_address))) {
      $copy .= addslashes($ad1).$newline;
    }
    if (($ad2 != "") && (in_array("address2", $cclipboard_address))) {
      $copy .= addslashes($ad2).$newline;
    }
    if (($ad3 != "") && (in_array("address3", $cclipboard_address))) {
      $copy .= addslashes($ad3).$newline;
    }
    $copy_zip = false;
    $copy_town = false;
    if (($zip != "") && (in_array("zipcode", $cclipboard_address))) {
      $copy .= addslashes($zip);
      $copy_zip = true;
    }
    if (($town != "") && (in_array("town", $cclipboard_address))) {
      $copy .= ($copy_zip ? " " : "") . addslashes($town);
      $copy_town = true;
    }
    if ($copy_zip || $copy_town) {
      $copy .= $newline;
    }
    if (($ctry_name != "") && (in_array("country", $cclipboard_address))) {
      $copy .= addslashes($ctry_name).$newline;
    }
  }
  if (($phone != "") && (in_array("phone", $cclipboard_address))) {
    $copy .= "$l_phone : " . addslashes($phone).$newline;
  }
  if (($fax != "") && (in_array("fax", $cclipboard_address))) {
    $copy .= "$l_fax : " . addslashes($fax).$newline;
  }
  if (($mphone != "") && (in_array("mobile", $cclipboard_address))) {
    $copy .= "$l_mphone : " . addslashes($mphone).$newline;
  }
  if (($email != "") && (in_array("email", $cclipboard_address))) {
    $copy .= "$l_email : " . addslashes($email).$newline;
  }

  // Service field
  if ($show_service) {
    $block_service = "
    </tr><tr>
      <th>$l_service</th>
      <td>$service</td>";
  }

  // Address3 field
  if ($show_ad3) {
    $block_ad3 = "
    </tr><tr>
      <th>$l_address 3</th>
      <td>$ad3</td>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    </tr><tr>
      <th>$l_expresspostal</th>
      <td>$cdx</td>";
  }

  if ($email != "") {
    $link_email = "
      <a href=\"mailto:$email\">
      <img src=\"$ico_mail\" alt=\"C\" />
      </a>";
  }

  if ($email2 != "") {
    $link_email2 = "
      <a href=\"mailto:$email2\">
      <img src=\"$ico_mail\" alt=\"C\" />
      </a>";
  }
  
  if ($show_comment2) {
    $block_comment2 = "
    <div class=\"detail extra\"> 
    <h1>$l_comment2</h1>
    $com2
    </div>";
  }
  if ($show_comment3) {
    $block_comment3 = "
    <div class=\"detail extra\">
    <h1>$l_comment3</h1>
    $com3
    </div>";
  }


  // --- HTML Template --------------------------------------------------------

  $block = "
    <div class=\"detail infos\">
    <h1>$l_coord</h1>

    <table>
    <tr>
      <th>$l_phone</th>
      <td>$phone</td>
    </tr><tr>
      <th>$l_hphone</th>
      <td>$hphone</td>
    </tr><tr>
      <th>$l_fax</th>
      <td>$fax</td>
    </tr><tr>
      <th>$l_mphone</th>
      <td>$mphone</td>
    $block_service
    </tr><tr>
      <th>
      $l_address 1
      <a href=\"javascript: return false;\" onclick=\"contact_copy_clip('$copy'); return false;\" title=\"Copy\" ><img alt=\"Copy\" src=\"$ico_clipboard\" /></a>
      </th>
      <td>$ad1</td>
    </tr><tr>
      <th>$l_address 2</th>
      <td>$ad2</td>
    $block_ad3
    </tr><tr>
      <th>$l_postcode</th>
      <td>$zip</td>
    </tr><tr>
      <th>$l_town</th>
      <td>$town</td>
    $block_cdx
    </tr><tr>
      <th>$l_country</th>
      <td>$ctry_name</td>
    </tr><tr>
      <th>$l_email</th>
      <td>$link_email $email</td>
    </tr><tr>
      <th>$l_email_other</th>
      <td>$link_email2 $email2</td>
    </tr><tr>
      <th>$l_mailing_ok</th>
      <td>$mailok</td>
    </tr>
    </table>
    </div>

    $block_userdata
    <div class=\"detail extra\">
    <h1>$l_comment1</h1>
    $com
    </div>
    $block_comment2 
    $block_comment3 ";

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
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id&amp;cba_archive=1");
    $deal_active = get_global_linked_deal_nb($id, "contact1", false, "contact2");
    $deal_total = get_global_linked_deal_nb($id, "contact1", true, "contact2");
    $block_deal = "
  <div  class=\"portlet\" id=\"contactDeal\">
  <h1>$l_module_deal</h1>
  <ul>
    <li><a href=\"$url_deal_list\"><img src=\"$ico_deal\" /></a>
        $l_module_deal (
        <a href=\"$url_deal_list\">$deal_active</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
  </ul>
  </div>";
  }

  // List
  if ($cgp_show["module"]["list"]) {
    $url_list = url_prepare("$path/list/list_index.php?action=search&amp;contact_id=$id");
    $list_nb = get_linked_contact_list_nb($id);
    $block_list = "
  <div  class=\"portlet\" id=\"contactList\">  
  <h1>$l_module_list</h1>
  <ul>
    <li><a href=\"$url_list\"><img src=\"$ico_list\" /></a>
        $l_module_list ( <a href=\"$url_list\">$list_nb</a> )</li>
  </ul>
  </div>";
  }

  // Contract
  if ($cgp_show["module"]["contract"]) {
    $contract_active = get_global_linked_contract_nb($id,"contact1",false,"contact2");
    $contract_total = get_global_linked_contract_nb($id,"contact1",true,"contact2");
    $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;contact_id=$id");
    $url_contract_total = url_prepare("$path/contract/contract_index.php?action=search&amp;contact_id=$id&amp;cba_archive=1");
    $block_contract = "
  <div  class=\"portlet\" id=\"contactContract\">  
  <h1>$l_module_contract</h1>
  <ul>
    <li><a href=\"$url_contract\"><img src=\"$ico_contract\" alt=\"\" /></a>
        $l_module_contract (
        <a href=\"$url_contract\">$contract_active</a> /
        <a href=\"$url_contract_total\">$contract_total</a> )</li>
  </ul>
  </div>";
  }

  // Document  
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=contact");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=contact");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
    $nb_document = run_query_global_document_nb ($id, "contact");
    $block_doc = "
  <div  class=\"portlet\" id=\"contactDocument\">  
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_document_new\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"$ico_document_new\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Publication  
  if ($cgp_show["module"]["publication"]) {
    $nb_subscription = run_query_contact_subscription_nb($id);
    $url_subscription = url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;view=publication&amp;contact_id=$id");
    $url = urlencode($url_subscription);
    $url_subscription_new = url_prepare("$path/publication/publication_index.php?action=new_subscription&amp;contact_id=$id&amp;popup=1&amp;ext_url=$url");
    $block_publi = "
  <div  class=\"portlet\" id=\"contactPublication\">  
  <h1>$l_subscription</h1>
  <ul>
    <li><a href=\"$url_subscription\"><img src=\"$ico_publication\" alt=\"\" /></a>
        <a href=\"$url_subscription\">$l_subscription_list ($nb_subscription)</a></li>
    <li><a href=\"javascript: void(0);\"  onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"><img src=\"$ico_publication_new\" alt=\"\" /></a>
	<a href=\"javascript: void(0);\" onclick=\"window.name='$l_contact'; window.open('$url_subscription_new','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\"> $l_subscription_new</a></li>
  </ul>
  </div>";
  }
  
  // Links Template  
  $block = "
  $block_deal
  $block_list
  $block_contract
  $block_doc
  $block_publi
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

  $cid = $contact["contact_id"];
  $dsrc_q = run_query_global_datasource();
  $kind_q = run_query_contact_kind();
  $funcs = of_category_get_ordered("contact", "function");

  $users = array($uid);

  if ($contact["market"] > 0) {
    $users[] = $contact["market"];
  }
  if ($contact["contact_id"] > 0) {
    $users[] = $co_q->f("contact_marketingmanager_id");
  }
  $usr_q = run_query_all_users_from_group($cg_com, $users);
  $ctry_q = run_query_global_country_for_lang();
  $block = html_contact_form($action, $co_q, $dsrc_q, $kind_q, $funcs, $usr_q, $ctry_q, $contact);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result (company detail on new)
//   - $dsrc_q    : datasource database result 
//   - $funcs     : Contact function array 
//   - $kind_q    : kind database result 
//   - $usr_q     : active userobm database result 
//   - $ctry_q    : country database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, priv, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $dsrc_q, $kind_q, $funcs, $usr_q, $ctry_q, $contact) {
  global $set_dsrc, $set_theme, $ico_company, $l_company,$ico_add,$ico_crow;
  global $cgp_mailing_default, $ccontact_private_default;
  global $l_lastname, $l_firstname, $l_aka, $l_kind, $l_title, $l_letter;
  global $l_phone, $l_hphone,$l_mphone,$l_fax,$l_postcode,$l_expresspostal;
  global $l_market, $l_address, $l_town, $l_country,$l_email,$l_mailing_ok, $l_date, $l_no_date;
  global $l_comment1, $l_add_comment1, $l_upd_comment1;
  global $l_comment2, $l_add_comment2, $l_upd_comment2;
  global $l_comment3, $l_add_comment3, $l_upd_comment3;
  global $l_service, $l_email_other;
  global $l_datasource, $l_archive, $l_private,$l_coord, $l_contact;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $popup_width, $popup_height;
  global $display, $path, $auth, $cgp_show, $cgp_hide, $c_undef, $l_undef;

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
    $date = ($date != 0) ? of_date_format($date, 1) : "";
    $lname = $co_q->f("contact_lastname");
    $fname = $co_q->f("contact_firstname");
    $aka = $co_q->f("contact_aka");
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
    $priv = $co_q->f("privacy");
    $comment = $co_q->f("contact_comment");
    $usercomment = $uid;
    $datecomment = of_isodate_format();
    $comment2 = $co_q->f("contact_comment2");
    $usercomment2 = $uid;
    $datecomment2 = of_isodate_format();
    $comment3 = $co_q->f("contact_comment3");
    $usercomment3 = $uid;
    $datecomment3 = of_isodate_format();
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
    $datecomment = of_isodate_format();
    $usercomment2 = $uid;
    $datecomment2 = of_isodate_format();
    $usercomment3 = $uid;
    $datecomment3 = of_isodate_format();
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
  if (isset($contact["company_new_id"])) { $c_new_id = $contact["company_new_id"]; }
  if (isset($contact["company_new_name"])) { $c_new_name = $contact["company_new_name"]; }
  if (isset($contact["contact_id"])) { $id = $contact["contact_id"]; }
  if (isset($contact["usercreate"])) { $usercreate = $contact["usercreate"]; }
  if (isset($contact["datasource"])) { $dsrc = $contact["datasource"]; }
  if (isset($contact["kind"])) { $kind = $contact["kind"]; }
  if (isset($contact["market"])) { $mark = $contact["market"]; }
  if (isset($contact["date"])) { $mark = $contact["date"]; }
  if (isset($contact["lname"])) { $lname = stripslashes($contact["lname"]); }
  if (isset($contact["fname"])) { $fname = stripslashes($contact["fname"]); }
  if (isset($contact["aka"])) { $aka = stripslashes($contact["aka"]); }
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
  if (isset($contact["comment"])) { $comment = stripslashes($contact["comment"]); }
  if (isset($contact["add_comment"])) { $add_comment = stripslashes($contact["add_comment"]); }
  if (isset($contact["usercomment"])) { $usercomment = $contact["usercomment"]; }
  if (isset($contact["datecomment"])) { $datecomment = $contact["datecomment"]; }
  if (isset($contact["comment2"])) { $comment2 = stripslashes($contact["comment2"]); }
  if (isset($contact["add_comment2"])) { $add_comment2 = stripslashes($contact["add_comment2"]); }
  if (isset($contact["usercomment2"])) { $usercomment2 = $contact["usercomment2"]; }
  if (isset($contact["datecomment2"])) { $datecomment2 = $contact["datecomment2"]; }
  if (isset($contact["comment3"])) { $comment3 = stripslashes($contact["comment3"]); }
  if (isset($contact["add_comment3"])) { $add_comment3 = stripslashes($contact["add_comment3"]); }
  if (isset($contact["usercomment3"])) { $usercomment3 = $contact["usercomment3"]; }
  if (isset($contact["datecomment3"])) { $datecomment3 = $contact["datecomment3"]; }
  if (isset($contact["privacy"])) { $priv = stripslashes($contact["privacy"]); }
  if (isset($contact["archive"])) { $archive = ($contact["archive"] == 1 ? "checked" : ""); }

  // Conditionnal fields
  $show_title = (! $cgp_hide["contact"]["contact_title"]);
  $show_resp = (! $cgp_hide["contact"]["responsible"]);
  $show_date = (! $cgp_hide["contact"]["contact_date"]);
  $show_service = (! $cgp_hide["contact"]["contact_service"]);
  $show_ad3 = (! $cgp_hide["contact"]["contact_address3"]);
  $show_cdx = (! $cgp_hide["contact"]["contact_expresspostal"]);
  $show_comment2 = (! $cgp_hide["contact"]["contact_comment2"]);
  $show_comment3 = (! $cgp_hide["contact"]["contact_comment3"]);

  // User defined data
  $block_userdata = of_userdata_dis_entity_form("contact", $id, $contact);

  // some constants
  $csize_add = "48";
  $cmax_add = "64";
  $csize_phone = "24";

  // Function field
  $block_function = of_category_dis_entity_form("contact", "function", $funcs, "mono", $func, "none");

  // Title field
  if ($show_title) {
    $block_title = "
    <tr>
      <th>$l_title</th>
      <td><input type=\"text\" name=\"tf_title\" size=\"30\" maxlength=\"64\" value=\"$title\" /></td>
    </tr>";
  }

  // Responsible field
  if ($show_resp) {
    // Marketing manager select
    $sel_market = "<select name=\"sel_market\" id=\"sel_market\">
      <option value=\"$c_undef\">$l_undef</option>";
    while ($usr_q->next_record()) {
      $u_id = $usr_q->f("userobm_id");
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
    }
    $sel_market .= "</select>";

    $block_resp = "
    <tr>
      <th>$l_market</th>
      <td>$sel_market</td>
    </tr>";
  }

  // Date field
  if ($show_date) {
    $block_date = "
    <tr>
      <th>$l_date</th>
      <td>
        <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
      </td>
    </tr>";
  }

  // data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"$c_undef\">$l_undef</option>";
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

  $i=0;
  $sel_kind_lang = "<select name=\"sel_kind_lang\"
  onchange=\"fill_contact_select_label(this)\">
  <option value=\"$c_undef\">$l_undef</option>";
  $sel_kind_label = "<select name=\"sel_kind_label\" 
  onchange=\"fill_contact_select_header(this)\">
  <option value=\"$c_undef\">$l_undef</option>";
  $sel_kind_header = "<select name=\"sel_kind\">
  <option value=\"$c_undef\">$l_undef</option>";
  foreach ($k_kind as $k_lang => $k_kind_label) {
    $sel_kind_lang .= "\n<option value=\"$k_lang\"";
    if ($k_lang == $kind_lang) {
      foreach ($k_kind_label as $k_label => $k_kind_header) {
	$sel_kind_label .= "\n<option value=\"$k_label\"";
	if ($k_label == $kind_label) {
	  foreach ($k_kind_header as $k_header => $k_id) {
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
  $sel_ctry = "<select name=\"sel_country\" id=\"sel_country\">
    <option value=\"$c_undef\">$l_undef</option>";
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
      <th>$l_service</th>
      <td>
      <input type=\"text\" id=\"tf_service\" name=\"tf_service\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$service\" />
      </td>";
  }

  // Address 3 field
  if ($show_ad3) {
    $block_ad3 = "
    </tr><tr>
      <th>$l_address 3</th>
      <td>
      <input type=\"text\" id=\"tf_ad3\" name=\"tf_ad3\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad3\" /> $c_ad3
      </td>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    </tr><tr>
      <th>$l_expresspostal</th>
      <td><input type=\"text\" name=\"tf_cdx\" size=\"16\" maxlength=\"16\" value=\"$cdx\" /> $c_cdx</td>";
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
      $opt_usercomment .= " selected = \"selected\"";
    }
    $opt_usercomment .= ">$cname</option>\n";
  }

  $sel_usercomment = "<select name=\"sel_usercomment\">$opt_usercomment</select>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
        $l_upd_comment1
        <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>
    ";
    if ($show_comment2) {
      $dis_comment2 = "
        $l_upd_comment2
        <textarea name=\"ta_comment2\" rows=\"6\" cols=\"78\">$comment2</textarea>
      ";
    }
    if ($show_comment3) {
      $dis_comment3 = "
        $l_upd_comment3
        <textarea name=\"ta_comment3\" rows=\"6\" cols=\"78\">$comment3</textarea>
      ";
    }
  }

  if ($show_comment2) {
    $sel_usercomment2 = "<select name=\"sel_usercomment2\">$opt_usercomment</select>";
    $block_comment2 = "
    <fieldset class=\"detail extra\">
    <legend>$l_comment2</legend>
      $l_add_comment2
      <input type=\"text\" name=\"tf_datecomment2\" value=\"$datecomment2\" class=\"datePicker\"/>
      $sel_usercomment2
      <textarea name=\"ta_add_comment2\" rows=\"6\" cols=\"78\">$add_comment2</textarea>
      $dis_comment2
    </fieldset>";
  }

  if ($show_comment3) {
    $sel_usercomment3 = "<select name=\"sel_usercomment3\">$opt_usercomment</select>";
    $block_comment3 = "
    <fieldset class=\"detail extra\">
    <legend>$l_comment3</legend>
      $l_add_comment3
      <input type=\"text\" name=\"tf_datecomment3\" value=\"$datecomment3\" class=\"datePicker\"/>
      $sel_usercomment3
      <textarea name=\"ta_add_comment3\" rows=\"6\" cols=\"78\">$add_comment3</textarea>
      $dis_comment3
    </fieldset>";
  }

  // Company Display
  if ($cgp_show["module"]["company"]) {
    $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
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
    $display_private = "<input name=\"cba_privacy\" type=\"checkbox\" value=\"1\" $private_c />";
  }

  // UPDATE 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"contact_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"hd_usercreate\" value=\"$usercreate\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display["title"] = "<h1 class=\"title\">$l_contact : $lname $fname</h1>";

  // --- HTML Template --------------------------------------------------------
  $block = "$script";

  $block .= "
    <form method=\"get\" name=\"f_entity\" onsubmit=\"if (check_contact(this)) return true; else return false;\" action=\"".url_prepare("contact_index.php")."\">

    <fieldset class=\"detail infos\">
    <legend>$l_contact</legend>

    <table>
    <tr>
      <th>$l_lastname</th>
      <td><input type=\"text\" name=\"tf_lname\" size=\"30\" maxlength=\"64\" value=\"$lname\" /></td>
    </tr><tr>
      <th>$l_firstname</th>
      <td><input type=\"text\" name=\"tf_fname\" size=\"30\" maxlength=\"64\" value=\"$fname\" /></td>
    </tr><tr>
      <th>$l_aka</th>
      <td><input type=\"text\" id=\"tf_aka\" name=\"tf_aka\" maxlength=\"255\" size=\"50\" value=\"$aka\" /></td>
    </tr><tr>
      <th>$l_kind</th>
      <td> $sel_kind_lang $sel_kind_label $l_letter : $sel_kind_header </td>
    </tr><tr>
      <th>$l_datasource</th> 
      <td>$sel_dsrc</td>
    </tr><tr>
      <th>$l_private</th>
      <td>$display_private</td>
    </tr><tr>
      <th>$l_archive</th>
      <td><input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /></td>
    </tr><tr>
      <th>$l_company</th>
      <td>$dis_company</td>
    </tr>
    $block_function
    $block_title
    $block_resp
    $block_date
    $block_userdata
    </table>
    </fieldset>
   
    <fieldset class=\"detail infos\">
    <legend>$l_coord</legend>

    <table>
    <tr>
    <th>$l_phone</th>
    <td><input type=\"text\" name=\"tf_phone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$phone\" /> $c_phone</td>
    </tr><tr>
    <th>$l_hphone</th>
    <td><input type=\"text\" name=\"tf_hphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$hphone\" /></td>
    </tr><tr>
    <th>$l_fax</th>
    <td><input type=\"text\" name=\"tf_fax\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$fax\" /> $c_fax</td>
    </tr><tr>
    <th>$l_mphone</th>
    <td><input type=\"text\" name=\"tf_mphone\" size=\"$csize_phone\" maxlength=\"$csize_phone\" value=\"$mphone\" /></td>
    $block_service
    </tr><tr>
    <th>$l_address 1</th>
    <td><input type=\"text\" name=\"tf_ad1\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad1\" /> $c_ad1</td>
    </tr><tr>
    <th>$l_address 2</th>
    <td><input type=\"text\" name=\"tf_ad2\" size=\"$csize_add\" maxlength=\"$cmax_add\" value=\"$ad2\" /> $c_ad2</td>
    $block_ad3
    </tr><tr>
    <th>$l_postcode</th>
    <td><input type=\"text\" name=\"tf_zip\" size=\"8\" value=\"$zip\" /> $c_zip</td>
    </tr><tr>
    <th>$l_town</th>
    <td><input type=\"text\" name=\"tf_town\" size=\"24\" value=\"$town\" /> $c_town</td>
    $block_cdx
    </tr><tr>
    <th>$l_country</th>
    <td>$sel_ctry $c_ctry_name</td>
    </tr><tr>
    <th>$l_email</th>
    <td><input type=\"text\" name=\"tf_email\" size=\"52\" maxlength=\"128\" value=\"$email\" /></td>
    </tr><tr>
    <th>$l_email_other</th>
    <td><input type=\"text\" name=\"tf_email2\" size=\"52\" maxlength=\"128\" value=\"$email2\" /></td>
    </tr><tr>
    <th>$l_mailing_ok</th>
    <td><input type=\"checkbox\" name=\"cba_mailok\" value=\"1\" $mailok /></td>
    </tr>
    </table>
    </fieldset>

    <fieldset class=\"detail extra\">
    <legend>$l_comment1</legend>
      $l_add_comment1
      
      <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>$sel_usercomment
      <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>
      $dis_comment
    </fieldset>
    $block_comment2
    $block_comment3

    <fieldset class=\"buttons\">
    $dis_button
    </fieldset>
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

  $comp_id = ($contact["company_new_id"] ? $contact["company_new_id"] : $contact["company_id"]);
  $lname = stripslashes($contact["lname"]);
  $fname = stripslashes($contact["fname"]);
  $kind = $contact["kind"];
  $dsrc = $contact["datasource"];
  $privacy = $contact["privacy"];
  $func = $contact["function"];
  $title = stripslashes($contact["title"]);
  $mark = $contact["market"];
  $service = stripslashes($contact["service"]);
  $ad1 = stripslashes($contact["ad1"]);
  $ad2 = stripslashes($contact["ad2"]);
  $ad3 = stripslashes($contact["ad3"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $cdx = stripslashes($contact["cdx"]);
  $ctry = $contact["country"];
  $phone = stripslashes($contact["phone"]);
  $hphone = stripslashes($contact["hphone"]);
  $mphone = stripslashes($contact["mphone"]);
  $fax = stripslashes($contact["fax"]);
  $email = stripslashes($contact["email"]);
  $email2 = stripslashes($contact["email2"]);
  $mailok = $contact["mailok"];
  $com = stripslashes($contact["comment"]);
  $com2 = stripslashes($contact["comment2"]);
  $com3 = stripslashes($contact["comment3"]);
  $archive = $contact["archive"];

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
          <a class=\"detail\" href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$id") . "\">
          $samename ($company)</a>
        </td>
      </tr>";

  }

  // User defined data
  $userdata = of_userdata_dis_hidden_fields("contact", $contact);

  $hidden = "
    <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"tf_lname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_fname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
    <input type=\"hidden\" name=\"cba_privacy\" value=\"$privacy\" />
    <input type=\"hidden\" name=\"sel_function\" value=\"$func\" />
    <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
    <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
    <input type=\"hidden\" name=\"tf_service\" value=\"$service\" />
    <input type=\"hidden\" name=\"tf_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"tf_ad2\" value=\"$ad2\" />
    <input type=\"hidden\" name=\"tf_ad3\" value=\"$ad3\" />
    <input type=\"hidden\" name=\"tf_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"tf_town\" value=\"$town\" />
    <input type=\"hidden\" name=\"tf_cdx\" value=\"$cdx\" />
    <input type=\"hidden\" name=\"sel_country\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_hphone\" value=\"$hphone\" />
    <input type=\"hidden\" name=\"tf_mphone\" value=\"$mphone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    <input type=\"hidden\" name=\"tf_email2\" value=\"$email2\" />
    <input type=\"hidden\" name=\"cba_mailok\" value=\"1\" $mailok />
    <input type=\"hidden\" name=\"ta_comment\" value=\"$com\" />
    <input type=\"hidden\" name=\"ta_comment2\" value=\"$com2\" />
    <input type=\"hidden\" name=\"ta_comment3\" value=\"$com3\" />
    <input type=\"hidden\" name=\"cba_archive\" value=\"$archive\" />
";

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
    <input type=\"hidden\" name=\"contact_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"contact_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_contact_del(this.form)) return true; else return false;\" />
    </form>";

  $display["msg"] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
      $dis_delete
      $dis_back
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the contact administration index
///////////////////////////////////////////////////////////////////////////////
function dis_contact_admin_index() {
  global $cgp_hide;

  $kind_q = run_query_contact_kind();
  $block .= html_contact_kind_form($kind_q);

  if (! $cgp_hide["contact"]["function"]) {
    $funcs = of_category_get_ordered("contact", "function");
    $block .= of_category_dis_admin_form("function", $funcs);
  }

  // User defined data
  $block .= of_userdata_dis_admin_form("contact");

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
  global $l_label, $l_lang, $l_default, $l_header,$ico_ok,$ico_delete,$l_c_new;

/*  if ($kind_q->num_rows() > 0) {

    // Kind select construction
    $kind_list = "<select name=\"sel_kind\" onChange=\"
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
        forms.form_kind_update.cba_default.checked=true;
      } else {
        forms.form_kind_update.cba_default.checked=false;
      }
      forms.form_kind_update.tf_label.value=label;
      forms.form_kind_update.tf_header.value=header;\"
      size=\"8\">";*/


  $char_title = '=';
  $key = 0;
  while ($kind_q->next_record()) {
    $id = $kind_q->f("kind_id");
    $lang = $kind_q->f("kind_lang");
    $default = ($kind_q->f("kind_default") == 1 ? "X" : " ");
    $mlabel = $kind_q->f("kind_minilabel");
    $header = $kind_q->f("kind_header");
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = "";
    }
    $c_label = htmlentities($one_cat["label"]);
    $kind_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"kind_$id\" value=\"$id\" />
    <a href=\"?action=kind_checklink&amp;category=kind&amp;kind=$id\">
    <img src=\"$ico_delete\" alt=\"$l_kind_checkdelete\" title=\"$l_kind_checkdelete\" />
    </a></td>
    <td><input type=\"checkbox\" id=\"cba_kind_default_$id\" value=\"1\" /></td>
    <td><input type=\"text\" id=\"tf_kind_lang_$id\" value=\"$lang\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_kind_label_$id\" value=\"$mlabel\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_kind_header_$id\" value=\"$header\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'kind');\">
    <img src=\"$ico_ok\" alt=\"$l_kind_update\" title=\"$l_kind_update\" />
    </a>
    </td>
    </tr>";
  }


 // --- HTML Template --------------------------------------------------------

  $block = "

 <div class=\"detail admin\" >
  <form id=\"form_kind_update\" action=\"?action=kind_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_kind_lang\" value=\"\" />
  <input type=\"hidden\" name=\"cba_kind_default\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_header\" value=\"\" />
  <input type=\"hidden\" name=\"kind\" value=\"\" />
  </form>    
  <h1>$l_kind_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_default</td>
  <td>$l_lang</td>
  <td>$l_label</td>
  <td>$l_header</td>
  <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $kind_list
  </tbody>
  </table>
  </div>  

  <form id=\"form_kind_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category_new(this.tf_${cat}_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"4\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_default</td>
  <td>$l_lang</td>
  <td>$l_label</td>    
  <td>$l_header</td>
  </tr>
  <tr>
  <td><input type=\"checkbox\" name=\"cba_kind_default\" value=\"1\" /></td>
  <td><input type=\"text\" name=\"tf_kind_lang\" value=\"\" size=\"6\" /></td>
  <td><input type=\"text\" name=\"tf_kind_label\" value=\"\" size=\"6\" /></td>
  <td><input type=\"text\" name=\"tf_kind_header\" value=\"\" /></td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"4\"><input type=\"submit\" value=\"$l_kind_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"kind_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"kind\" />
  </form>
  </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Kind links
// Parameters:
//   - $ref : kind hash info : keys used : id, name
///////////////////////////////////////////////////////////////////////////////
function dis_contact_kind_links($ref) {
  global $display, $l_back, $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_link_contact, $l_kind_link_contact_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;

  $delete_ok = true;
  $id = $ref["kind"];
  $name = get_contact_kind_label($id);

  // Contacts Links
  $obm_q = run_query_contact_kind_links($id);
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
        <a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$cid") . "\">$cname</a>
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
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "contact");
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
function dis_contact_vcard_export($contact) {
  global $display, $path, $l_err_reference;
  global $auth;

  $contact_id = $contact["contact_id"];
  $con_q = run_query_contact_detail($contact_id);
  $com_q = run_query_contact_company($con_q->f("company_id"));
  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_err_reference);
  }
  export_contact_vcard($con_q,$com_q);
}


///////////////////////////////////////////////////////////////////////////////
// Vcard Export
// Parameters:
//   - $con_q : : DBO : information about the contact
///////////////////////////////////////////////////////////////////////////////
function export_contact_vcard($co_q,$com_q) {

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


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About Contact number evolution by date
// Parameters:
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_html_contact_date_graph_stats($entity) {
  global $l_contact_date_evolution_graph;

  $url = url_prepare("$path/statistic/statistic_index.php?action=contact_date_evolution_graph");

  $block = "<div class=\"detailHead\">".ucfirst($l_contact_date_evolution_graph)."</div>
   <table class=\"detail\" >
     <tr>
       <td class=\"detailLabel\" class=\"detailForm\"><img src=\"$url\" /></td>
     </tr>
   </table>";
 
  return $block; 
}


///////////////////////////////////////////////////////////////////////////////
// Display Statistics About Contact categories 
// Parameters:
//   - $params[] : parameters hash
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_category_contact_stats($params) {
  global $cgp_user, $display, $l_stats, $l_function;
  global $l_contact_date_evolution_graph;

  $order = $params["order"];
  $stat = $params["stat"];

  if (($stat == "") || ($stat == "index")) {

  } elseif ($stat == "function") {
    $cats = of_category_query_category_per_entity("contact","function","mono");
    $block_stat = of_category_dis_entity_stats("contact", $stat, $cats,$order);
  } elseif ($stat == "contact_date_evolution") {
    $block_stat = dis_html_contact_date_graph_stats("contact"); 
  } else {
    $cats = of_category_user_get_category_distribution("contact", $stat);
    $block_stat = of_category_dis_entity_stats("contact", $stat, $cats,$order);
  }

  if (is_array($cgp_user["contact"]["category"])) {
    foreach($cgp_user["contact"]["category"] as $cat_name => $one_cat) {
      global ${"l_$cat_name"};
      $l_cat = ${"l_$cat_name"};
      $block_contact .= "<div class=\"detailText\"><a href=\"?action=statistics&amp;stat=$cat_name\">$l_cat</a></div>";
    }
  }


  $block = "
    <div class=\"detailText\"><a href=\"?action=statistics&amp;stat=function\">$l_function</a></div>
    <div class=\"detailText\"><a href=\"?action=statistics&amp;stat=contact_date_evolution\">$l_contact_date_evolution_graph;</a></div>
    $block_contact
    <p />
    $block_stat
";

  return $block;
}

?>
