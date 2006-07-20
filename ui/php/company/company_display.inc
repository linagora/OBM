<?php
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
$fieldnames["company_number"] = $l_number;
$fieldnames["company_vat"] = $l_vat;
$fieldnames["company_siret"] = $l_siret;
$fieldnames["company_archive"] = $l_archive_first;
$fieldnames["company_address"] = $l_address;
$fieldnames["company_address1"] = "$l_address 1";
$fieldnames["company_address2"] = "$l_address 2";
$fieldnames["company_address3"] = "$l_address 3";
$fieldnames["company_zipcode"] = $l_postcode;
$fieldnames["company_town"] = $l_town;
$fieldnames["company_expresspostal"] = $l_expresspostal;
$fieldnames["country_name"] = $l_country;
$fieldnames["company_phone"] = $l_phone;
$fieldnames["company_fax"] = $l_fax;
$fieldnames["company_email"] = $l_email;
$fieldnames["company_web"] = $l_web;
// Calculated or indirect fields
$fieldnames["type_label"] = $l_type;
$fieldnames["activity_label"] = $l_activity;
$fieldnames["nafcode_code"] = $l_nafcode;
$fieldnames["company_contact_number"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"C\" />";
$fieldnames["company_new_contact"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact_new\" alt=\"NC\" />";
$fieldnames["company_deal_number"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"D\" />";


///////////////////////////////////////////////////////////////////////////////
// Display Company specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_company(&$OD, $fieldname, $link_ok) {
  global $path, $params, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  $ext_url = $params["ext_url"];

  if ($fieldname == "company_name") {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_company_get_id(".$OD->data_set->f("company_id").",'".addslashes($OD->data_set->f("company_name"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_company_get_id_url('$ext_url',".$OD->data_set->f("company_id").");";
    } else {
      $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f("company_id");
    }
  }

  else if ($fieldname == "company_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if (($fieldname == "company_email") && $link_ok) {
    $email = $OD->data_set->f("company_email");
    if (strcmp($email,"") != 0) {
      $res["url"] = "mailto:$email";
      $res["name"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"$email\" />";
      $res["txt_name"] = $email;
    }
  }

  else if (($fieldname == "company_web") && $link_ok) {
    $web = $OD->data_set->f("company_web");
    if (strcmp($web,"") != 0) {
      if ( (strcmp(substr($web,0,7), "http://") == 0)
           || (strcmp(substr($web,0,8), "https://") == 0) ) {
	$link_web = "";
      } else {
	$link_web = "http://";
      }
      $link_web .= "$web";
      $res["url"] = $link_web;
      $res["window"] = true;
      $res["popup_width"] = 800;
      $res["popup_height"] = 600;
      $res["name"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_web\" alt=\"$web\" />";
      $res["txt_name"] = $web;
    }
  }

  else if ($fieldname == "company_contact_number") {
    $res["name"] = $OD->data_set->f("company_contact_number");
    if (($res["name"] > 0) && $link_ok) {
      $res["url"] = "$path/contact/contact_index.php?action=search&amp;company_id=".$OD->data_set->f("company_id")."&amp;tf_company=".urlencode("".$OD->data_set->f("company_name"));
    }
    $res["align"] = "center";
  }

  else if (($fieldname == "company_new_contact") && $link_ok){
    $res["align"] = "center";
    $market = $OD->data_set->f("company_marketingmanager_id");
    $email = $OD->data_set->f("company_email");
    $ctry = $OD->data_set->f("company_country_iso3166");
    $res["url"] = "$path/contact/contact_index.php?action=new&amp;company_id=".$OD->data_set->f("company_id")."&amp;sel_market=$market&amp;sel_country=$ctry&amp;tf_email=$email";
    $res["name"] = "<img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact_new\" alt=\"\" />";
    $res["txt_name"] = " ";
  }

  else if ($fieldname == "company_deal_number") {
    $res["name"] = $OD->data_set->f("company_deal_number") . " / ";
    $res["name"] .= $OD->data_set->f("company_deal_total");

    $c_id = $OD->data_set->f("company_id");
    if (($res["name"] != "0 / 0") && $link_ok) {
      $res["url"] = "$path/deal/deal_index.php?action=search&amp;company_id=$c_id";
    }
    $res["align"] = "center";
  }
  
  else if ($fieldname == "company_address") {
    if ($OD->data_set->f("company_address1") != "") {
      $res["name"] .= $OD->data_set->f("company_address1")."<br />\n";
    }
    if ($OD->data_set->f("company_address2") != "") {
      $res["name"] .= $OD->data_set->f("company_address2")."<br />\n";
    }
    if ($OD->data_set->f("company_address3") != "") {
      $res["name"] .= $OD->data_set->f("company_address3")."<br />\n";
    }
    if ($OD->data_set->f("company_zipcode") != "" ) {
      $res["name"] .= $OD->data_set->f("company_zipcode") . " ";
    } else {
      $res["name"] .= " ";
    }
    if ($OD->data_set->f("company_town") != "" ) {
      $res["name"] .= $OD->data_set->f("company_town");
    }
    if ($OD->data_set->f("country_name") != "" ) {
      $res["name"] .= " " . $OD->data_set->f("country_name");
    }
    $res["txt_name"] = trim($OD->data_set->f("company_address1") . " " .
                       $OD->data_set->f("company_address2") . " " .
                       $OD->data_set->f("company_address3") . " " .
                       $OD->data_set->f("company_zipcode") . " " .
                       $OD->data_set->f("company_town") . " " .
		       $OD->data_set->f("country_name"));
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company search form
// Parameters:
//   - $company[] : hash with company values
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_form($company="") {

  $types = of_category_get_ordered("company", "type");
  $cats1 = of_category_get_ordered("company", "category1");
  $acts = of_category_get_ordered("company", "activity");
  $naf_q = run_query_companynafcode();
  $usr_q = run_query_userobm_active();
  $ctry_q = run_query_global_country_for_lang();
  $dsrc_q = run_query_global_datasource();
  $block .= html_company_search_form($types, $cats1, $acts, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company search Form
// Parameters:
//   - $types     : array of company types
//   - $cats1     : array of company categories
//   - $acts      : array of company activities 
//   - $naf_q     : database object with nafcode list
//   - $usr_q     : database object with userobm list
//   - $ctry_q    : database object with country list
//   - $dsrc_q    : database object with datasource list
//   - $company[] : default form values
//     keys used  : archive, name, kind, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form($types, $cats1, $acts, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company) {
  global $action, $display, $c_all, $cgp_hide;
  global $csearch_advanced_default, $l_fuzzy;
  global $l_company_name, $l_postcode, $l_town, $l_archive, $l_phone;
  global $l_market, $l_country, $l_after, $l_before, $l_nafcode; 
  global $l_all, $l_find, $l_datasource, $l_cat_tree, $l_select_company;

  $popup = $company["popup"];
  $archive = ($company["archive"] == "1" ? "checked = \"checked\"" : "");
  $name = stripslashes($company["name"]);
  $phone = stripslashes($company["phone"]);
  $town = stripslashes($company["town"]);
  $type = $company["type"];
  $cat1 = $company["category1"];
  $cat_code = $company["category1_code"];
  $cat_tree = ($company["category1_tree"] == "1" ? "checked = \"checked\"" : "");
  $act = $company["activity"];
  $naf = $company["naf"];
  $ctry = $company["country"];
  $mark = $company["market"];
  $dsrc = $company["datasource"];
  $zip = stripslashes($company["zip"]);
  $dateafter = stripslashes($company["dateafter"]);
  $datebefore = stripslashes($company["datebefore"]);
  if (($company["fuzzy"] == "1") ||
      ($csearch_advanced_default && $action != "search") ) {
    $fuzzy = "checked = \"checked\"";
  } else {
    $fuzzy = "";
  }

  // of: activity, type
  $show_naf = (! $cgp_hide["company"]["nafcode"]);
  $show_cat = (! $cgp_hide["company"]["category1"]);

  // Type, category1, activity select
  $block_type = of_category_dis_search_select("company", "type", $types, $type);
  $block_activity = of_category_dis_search_select("company", "activity", $acts, $act);

  if ($show_cat) {
    $sel_append = "onchange=\"if (this.options[this.selectedIndex].value != '$c_all') {
          mytext = this.options[this.selectedIndex].text;
          pos = mytext.indexOf(' ');
          code = mytext.substr(0, pos);
          this.form.tf_category1_code.value = code + '.';
       } \"";

    $block_category1 = of_category_dis_search_select("company", "category1", $cats1, $cat1, array("all", "none"), $sel_append);

    $block_cat = "
    $block_category1
    <div class=\"searchLabel\">$l_cat_tree<br />
      <input type=\"checkbox\" name=\"cb_category1_tree\" value=\"1\" $cat_tree />
      <input type=\"hidden\" name=\"tf_category1_code\" value=\"$cat_code\" />
    </div>";
  }

  // Naf Code select
  if ($show_naf) {
    $sel_naf = dis_select_company_nafcode($naf_q, "<option value=\"$c_all\">$l_all</option>", $naf);

    $block_naf = "
    <div class=\"searchLabel\">$l_nafcode<br />
      $sel_naf
    </div>";
  }

  // Country select
  $sel_ctry = "<select id=\"sel_country\" name=\"sel_country\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f("country_iso3166");
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">". htmlentities($ctry_q->f("country_name")) . "</option>\n";
  }
  $sel_ctry .= "</select>";

  // Marketing manager select
  $sel_market = "<select id=\"sel_market\" name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

  // Data source select
  $sel_dsrc = "<select name=\"sel_dsrc\" id=\"sel_dsrc\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f("datasource_id");
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= ">". $dsrc_q->f("datasource_name") . "</option>\n";
  }
  $sel_dsrc .= "</select>";

  $url = url_prepare("company_index.php");

  if ($popup) {
    $ext_action = $company["ext_action"];
    $ext_url = $company["ext_url"];
    $ext_id = $company["ext_id"];
    $ext_title = ($company["ext_title"] ? $company["ext_title"] : $l_select_company);
    $ext_target = $company["ext_target"];
    $ext_widget = $company["ext_widget"];
    $ext_widget_text = $company["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" id=\"f_search\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_company_name<br />
      <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"24\"
      value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" id=\"tf_phone\" size=\"10\"
      value=\"$phone\" />
    </div>
    $block_type
    $block_cat
    $block_activity
    $block_naf
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
    <div class=\"searchLabel\">$l_datasource<br />
      $sel_dsrc
    </div>
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
    </div>
    <div class=\"searchLabel\">$l_after<br />
      <script>calendar('tf_dateafter','$dateafter')</script>
    </div>
    <div class=\"searchLabel\">$l_before<br />
      <script>calendar('tf_datebefore','$datebefore')</script>
    </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </div>
    <div class=\"searchLabel\">$l_fuzzy<br />
      <input type=\"checkbox\" name=\"cb_fuzzy\" value=\"1\" $fuzzy />
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" id=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company search result
// Parameters:
//   - $company[] : company search criteria
//     keys used  : name, type, zip
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_list($company) {
  global $display, $l_found, $l_no_found;
  global $auth;

  $popup = $company["popup"];
  $prefs = get_display_pref($auth->auth["uid"], "company");
  $obm_q = run_query_company_search($company);
  $nb_company = $obm_q->num_rows_total();
  if ($nb_company == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $display["msg"] = display_info_msg("$nb_company $l_found");
    $block = html_company_search_list($obm_q, $prefs, $company, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Returns the XHTML result display
// Parameters : 
//   - $comp_q    : list of companies
//   - $prefs     : Display preferences
//   - $company[] : company search criteria
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_company_search_list($comp_q, $prefs, $company, $popup) {
  global $l_close;

  if ($popup) {
    $ext_action = $company["ext_action"];
    $ext_url = $company["ext_url"];
    $ext_id = $company["ext_id"];
    $ext_target = $company["ext_target"];
    $ext_widget = $company["ext_widget"];
    $ext_widget_text = $company["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }
  // we urlencode to avoid breakage for space char
  $archive = $company["archive"];
  $name = urlencode(stripslashes($company["name"]));
  $phone = urlencode(stripslashes($company["phone"]));
  $town = stripslashes($company["town"]);
  $ctry = $company["country"];
  $type = $company["type"];
  $cat1 = $company["category1"];
  $cat_code = $company["category1_code"];
  $cat_tree = $company["category1_tree"];
  $act = $company["activity"];
  $naf = $company["naf"];
  $zip = urlencode(stripslashes($company["zip"]));
  $market = $company["market"];
  $dsrc = $company["datasource"];
  $dateafter = stripslashes($company["dateafter"]);
  $datebefore = stripslashes($company["datebefore"]);
  $fuzzy = $company["fuzzy"];

  $url = url_prepare("company_index.php?action=search&amp;tf_name=$name&amp;tf_phone=$phone&amp;sel_type=$type&amp;sel_category1=$cat1&amp;tf_category1_code=$cat_code&amp;cb_category1_tree=$cat_tree&amp;sel_activity=$act&amp;sel_naf=$naf&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_market=$market&amp;tf_town=$town&amp;sel_country=$ctry&amp;sel_dsrc=$dsrc&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;cb_fuzzy=$fuzzy$url_ext");

  $comp_d = new OBM_DISPLAY("DATA", $prefs, "company");
  if ($popup) {
    $comp_d->display_link = false;
    if ($ext_url != "") {
      $comp_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $comp_d->display_ext = "get_id";
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $comp_d->data_set = $comp_q;
  $comp_d->data_url = $url;
  $comp_d->data_header = "both";
  $block = $comp_d->display("dis_data_company");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Company links menu
// Parameters:
//   - $co_q : company database result 
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_company_links($co_q) {
  global $set_theme,$ico_contact,$ico_contact_new,$ico_deal,$ico_deal_new;
  global $ico_document,$ico_document_new, $ico_project, $ico_contract;
  global $ico_invoice, $ico_payment;
  global $l_new, $l_module_contact, $l_module_project, $l_module_deal;
  global $l_module_contract, $l_module_invoice, $l_module_document;
  global $l_module_payment, $l_document_add;
  global $l_company, $path, $auth, $cgp_show, $popup_height,$popup_width;

  $id = $co_q->f("company_id");
  $name = $co_q->f("company_name");
  $market = $co_q->f("company_marketingmanager_id");
  $email = $co_q->f("company_email");
  $ctry = $co_q->f("company_country_iso3166");
  $con_num = $co_q->f("company_contact_number");
  $deal_num = $co_q->f("company_deal_number");
  $deal_total = $co_q->f("company_deal_total");

  $uname = urlencode($name);

  // Contact
  if ($cgp_show["module"]["contact"]) {
    $url_con_list = url_prepare("$path/contact/contact_index.php?action=search&amp;company_id=$id&amp;tf_company=$uname");
    $url_con_new = url_prepare("$path/contact/contact_index.php?action=new&amp;company_id=$id&amp;sel_market=$market&amp;sel_country=$ctry&amp;tf_email=$email");
    $block_contact = "
  <div class=\"linkTitle\">:: $l_module_contact</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_con_list\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" /></a>
        <a href=\"$url_con_list\">$l_module_contact ($con_num)</a></li>
    <li><a href=\"$url_con_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact_new\" /></a>
        <a href=\"$url_con_new\">$l_new</a></li>
  </ul>";
  }

  // Deal
  if ($cgp_show["module"]["deal"]) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;company_id=$id&amp;tf_company_name=$uname");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;company_id=$id&amp;tf_company_name=$uname&amp;cb_archive=1");
    $url_deal_new = url_prepare("$path/deal/deal_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");
    $block_deal = "
  <div class=\"linkTitle\">:: $l_module_deal</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_deal_list\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" /></a>
        $l_module_deal (
        <a href=\"$url_deal_list\">$deal_num</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
    <li><a href=\"$url_deal_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal_new\" /></a>
        <a href=\"$url_deal_new\">$l_new</a></li>
  </ul>";
  }

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=company");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=company");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/company/company_index.php")."&amp;ext_id=$id&amp;ext_target=$l_company";
    $nb_document = run_query_global_document_nb ($id, "company");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_module_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_company'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Project
  if ($cgp_show["module"]["project"]) {
    $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;company_id=$id");
    $url_pro_tot = url_prepare("$path/project/project_index.php?action=search&amp;company_id=$id&amp;cb_archive=1");
    $nb_pro = get_global_linked_project_nb($id, "company");
    $nb_pro_total = get_global_linked_project_nb ($id, "company", 1);
    $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");
    $block_project = "
  <div class=\"linkTitle\">:: $l_module_project</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_pro\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" /></a>
        $l_module_project ( <a href=\"$url_pro\">$nb_pro</a> /
        <a href=\"$url_pro_tot\">$nb_pro_total</a> )</li>
    <li><a href=\"$url_pro_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" /></a>
        <a href=\"$url_pro_new\">$l_new</a></li>
  </ul>";
  }

  // Contract
  if ($cgp_show["module"]["contract"]) {
    $nb_ctra = get_global_linked_contract_nb($id, "company");
    $nb_ctra_total = get_global_linked_contract_nb($id, "company", 1);
    $url_ctra = url_prepare("$path/contract/contract_index.php?action=search&amp;company_id=$id");
    $url_ctra_total = url_prepare("$path/contract/contract_index.php?action=search&amp;company_id=$id&amp;cb_archive=1");
    $url_ctra_new = url_prepare("$path/contract/contract_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");

     $block_contract = "
  <div class=\"linkTitle\">:: $l_module_contract</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_ctra\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" /></a>
        $l_module_contract ( <a href=\"$url_ctra\">$nb_ctra</a> /
        <a href=\"$url_ctra_total\">$nb_ctra_total</a> )</li>
    <li><a href=\"$url_ctra_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_ctra_new\">$l_new</a></li>
  </ul>";
  }

  // Invoice
  if ($cgp_show["module"]["invoice"]) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;company_id=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;company_id=$id&amp;cb_archive=1");
    $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;company_id=$id&amp;company_name=$uname");
    $nb_inv_tot = get_global_linked_invoice_nb($id, "company", 1);
    $nb_inv = get_global_linked_invoice_nb($id, "company");
    $block_invoice = "
  <div class=\"linkTitle\">:: $l_module_invoice</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_inv\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        $l_module_invoice ( <a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a> )</li>
    <li><a href=\"$url_inv_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>
  </ul>";
  }

  // Payment
  if ($cgp_show["module"]["payment"]) {
    $url_pay = url_prepare("$path/payment/payment_index.php?action=search&amp;company_id=$id");
    $url_pay_new = url_prepare("$path/payment/payment_index.php?action=new&amp;company_id=$id&amp;company_name=$uname");
    $nb_pay = get_global_linked_payment_nb($id, "company");
    $block_payment = "
  <div class=\"linkTitle\">:: $l_module_payment</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_inv\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_payment\" alt=\"\" /></a>
        $l_module_payment ( <a href=\"$url_pay\">$nb_pay</a> )</li>
    <li><a href=\"$url_pay_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_payment\" alt=\"\" /></a>
        <a href=\"$url_pay_new\">$l_new</a></li>
  </ul>";
  }

  // Links Template  
  $block = "
<div class=\"link\"> 
  $block_contact
  $block_deal
  $block_project
  $block_contract
  $block_doc
  $block_invoice
  $block_payment
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Detail
// Parameters:
//   - $params[] : company criteria
///////////////////////////////////////////////////////////////////////////////
function dis_company_consult($params) {
  global $path, $display, $obminclude, $set_lang, $l_query_error;

  $cid = $params["company_id"];

  if ($cid > 0) {
    $comp_q = run_query_company_detail($cid);
    if ($comp_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($comp_q);
      $display["link"] = html_company_links($comp_q);
      $block .= html_company_consult($comp_q);
      $view = $params["view"];

      if ($view != "") {
	global $fieldnames;
       	include("$obminclude/lang/$set_lang/global.inc");
	include("$obminclude/lang/$set_lang/$view.inc");
	include("$path/$view/".$view."_display.inc");
	include("$path/$view/".$view."_query.inc");
	$ext_func = "ext_dis_${view}_list";
	$block .= $ext_func($params, "company", $cid);
      } else {
	$cats1 = of_category_get_ordered("company", "category1", "multi", $cid);
	$block .= html_company_consult_address($comp_q, $cats1);
      }
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $comp_q->query . " !");
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company Consultation
// Parameters:
//   - $co_q : company database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_consult($co_q) {
  global $set_theme, $ico_contact, $ico_contact_new, $ico_web, $ico_mail;
  global $l_name, $l_aka, $l_number, $l_nafcode, $l_vat, $l_siret, $l_market;
  global $l_company, $l_archive;
  global $display, $l_yes, $l_no, $cgp_hide;

  $id = $co_q->f("company_id");
  $num = $co_q->f("company_number");
  $vat = $co_q->f("company_vat");
  $siret = $co_q->f("company_siret");
  $archive = ($co_q->f("company_archive") == 1 ? $l_yes : $l_no);
  $name = $co_q->f("company_name");
  $aka = $co_q->f("company_aka");
  $type = $co_q->f("companytype_label");
  $act = $co_q->f("companyactivity_label");
  $naf = $co_q->f("companynafcode_code")." - ".$co_q->f("companynafcode_label");
  $mark = $co_q->f("company_marketingmanager_id");

  $types = array(array("label" => $type));
  $acts = array(array("label" => $act));
    
  if ($mark > 0) {
    $lmarket = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  } else {
    $lmarket = "&nbsp;";
  }

  // Display Aka only if non empty
  if ($aka != "") {
    $dis_aka = "
    <tr> 
      <td class=\"detailLabel\">$l_aka</td>
      <td class=\"detailText\">$aka</td>
    </tr>";
  } else {
    $dis_aka = "";
  }

  // Conditionnal fields
  // of: activity, type, category1, activity
  $show_number = (! $cgp_hide["company"]["company_number"]);
  $show_vat = (! $cgp_hide["company"]["company_vat"]);
  $show_siret = (! $cgp_hide["company"]["company_siret"]);
  $show_naf = (! $cgp_hide["company"]["nafcode"]);

  // Number field
  if ($show_number) {
    $block_num = "
    <tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$num</td>
    </tr>";
  }

  // VAT field
  if ($show_vat) {
    $block_vat = "
    <tr>
      <td class=\"detailLabel\">$l_vat</td>
      <td class=\"detailText\">$vat</td>
    </tr>";
  }

  // SIRET field
  if ($show_siret) {
    $block_siret = "
    <tr>
      <td class=\"detailLabel\">$l_siret</td>
      <td class=\"detailText\">$siret</td>
    </tr>";
  }

  // Type, activity fields
  $block_type = of_category_dis_block_consult("company", "type", $types, "mono");
  $block_activity = of_category_dis_block_consult("company", "activity", $acts, "mono");

  // Naf code field
  if ($show_naf) {
    $block_naf = "
    <tr> 
      <td class=\"detailLabel\">$l_nafcode</td>
      <td class=\"detailText\">$naf</td>
    </tr>";
  }
  
  $display["title"] = "<div class=\"title\">$l_company : $name</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
<div class=\"detailHead\">$l_company</div>

  <table class=\"detail\">
    <tr> 
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr>
    $dis_aka
    $block_num
    $block_vat
    $block_siret
    <tr> 
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr>
    $block_type
    $block_activity
    $block_naf
    <tr>
      <td class=\"detailLabel\">$l_market</td>
      <td class=\"detailText\">$lmarket</td>
    </tr>
  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company Consultation : Address sub view
// Parameters:
//   - $co_q : company database result 
//   - $cats1 : array of compant categories1
///////////////////////////////////////////////////////////////////////////////
function html_company_consult_address($co_q, $cats1) {
  global $set_theme, $ico_contact, $ico_contact_new, $ico_web, $ico_mail;
  global $l_name;
  global $l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal;
  global $l_town,$l_country,$l_email,$l_web,$l_comment;
  global $l_company, $l_coord;
  global $display, $l_yes, $l_no, $cgp_hide,$ico_clipboard,$cclipboard_address;

  $id = $co_q->f("company_id");
  $archive = ($co_q->f("company_archive") == 1 ? $l_yes : $l_no);
  $name = $co_q->f("company_name");
  $ad1 = $co_q->f("company_address1");
  $ad2 = $co_q->f("company_address2");
  $ad3 = $co_q->f("company_address3");
  $zip = $co_q->f("company_zipcode");
  $town = $co_q->f("company_town");
  $cdx = $co_q->f("company_expresspostal");
  $ctry_name = $co_q->f("country_name");
  $phone = $co_q->f("company_phone");
  $fax = $co_q->f("company_fax");
  $web = $co_q->f("company_web");
  $email = $co_q->f("company_email");
  $com = beautify_comment(nl2br($co_q->f("company_comment")));

  $cats1 = of_category_get_entitycategories("company", "category1", $id);
    
  // Conditionnal fields
  $show_ad3 = (! $cgp_hide["company"]["company_address3"]);
  $show_cdx = (! $cgp_hide["company"]["company_expresspostal"]);

  $newline = "\\r\\n";
  $copy = addslashes($name).$newline;
  if (($ad1 != "") && (in_array("address1", $cclipboard_address))) {
    $copy .= addslashes($ad1).$newline;
  }
  if (($ad2 != "") && (in_array("address2", $cclipboard_address))) {
    $copy .= addslashes($ad2).$newline;
  }
  if (($ad3 != "") && (in_array("address3", $cclipboard_address))) {
    $copy .= addslashes($ad3).$newline;
  }
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
  if (($cdx != "") && (in_array("expresspostal", $cclipboard_address))) {
    $copy .= addslashes($cdx).$newline;
  }
  if (($ctry_name != "") && (in_array("country", $cclipboard_address))) {
    $copy .= addslashes($ctry_name).$newline;
  }
  if (($phone != "") && (in_array("phone", $cclipboard_address))) {
    $copy .= "$l_phone : " . addslashes($phone).$newline;
  }
  if (($fax != "") && (in_array("fax", $cclipboard_address))) {
    $copy .= "$l_fax : " . addslashes($fax).$newline;
  }
  if (($email != "") && (in_array("email", $cclipboard_address))) {
    $copy .= "$l_email : " . addslashes($email).$newline;
  }
  if (($web != "") && (in_array("web", $cclipboard_address))) {
    $copy .= "$l_web : " . addslashes($web).$newline;
  }
  $copy = "'$copy'"; 

  // Address3 field
  if ($show_ad3) {
    $block_ad3 = "
    <tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailText\">$ad3</td>
    </tr>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    <tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailText\">$cdx</td>
    </tr>";
  }

  // Email link
  if ($email != "") {
    $link_email = "<a href=\"mailto:$email\">
      <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\"
      alt=\"$email\" /></a>";
  } else {
    $link_email = "&nbsp;";
  }

  // Category1 field
  $block_cat1 = of_category_dis_block_consult("company", "category1", $cats1);

  // Web link
  if ($web != "") {
    $link_web = "<a target=\"_blank\" href=\"";
    if ( (strcmp(substr($web,0,7), "http://") == 0)
       || (strcmp(substr($web,0,8), "https://") == 0) ){
      $link_web .= "";
    } else {
      $link_web .= "http://";
    }
    $link_web .= "$web\">
      <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/$ico_web\"
      alt=\"$web\" /></a>";
  } else {
    $link_web = "&nbsp;";
  }

  // --- HTML Template --------------------------------------------------------

  $template = get_template("company_consult.tpl");
  eval ("\$template = \"$template\";");
  $block = $template;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Form
// Parameters:
//   - $action    : action called
//   - $company[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_company_form($action, $company) {
  global $display, $cg_com, $cgp_hide, $uid, $l_query_error;

  // of: type, activity, category1
  $show_naf = (! $cgp_hide["company"]["nafcode"]);

  $c_id = $company["company_id"];
  if ($c_id > 0) {
    $comp_q = run_query_company_detail($c_id);
    if ($comp_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($comp_q);
      $cats1 = of_category_get_ordered("company", "category1", "multi", $c_id);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $comp_q->query . " !");
    }
  }
  $dsrc_q = run_query_global_datasource();
  $types = of_category_get_ordered("company", "type");
  $acts = of_category_get_ordered("company", "activity");

  if ($show_naf) {
    $naf_q = run_query_companynafcode();
  }

  $users = array($uid);
  if ($company["market"] > 0) {
    $users[] = $company["market"];
  }
  if ($company["company_id"] > 0) {
    $users[] = $comp_q->f("company_marketingmanager_id");
  }
  $usr_q = run_query_all_users_from_group($cg_com, $users);
  $ctry_q = run_query_global_country_for_lang();

  $block = html_company_form($action, $comp_q, $dsrc_q, $types, $acts, $naf_q, $usr_q, $cats1, $ctry_q, $company);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company Form
// Parameters:
//   - $action    : action called
//   - $co_q      : company database result 
//   - $dsrc_q    : datasource database result 
//   - $types     : array of types referenced by the company 
//   - $acts      : array of activities referenced by the company
//   - $naf_q     : nafcode database result 
//   - $usr_q     : userobm database result 
//   - $cats1     : array of category referenced by the company
//   - $ctry_q    : country database result 
//   - $company[] : default values
//     keys used  : id, num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_company_form($action, $co_q, $dsrc_q, $types, $acts, $naf_q, $usr_q, $cats1, $ctry_q, $company) {
  global $display, $auth, $cgp_hide, $set_dsrc, $c_undef, $l_undef, $ico_add;
  global $set_theme,$ico_web,$ico_mail,$popup_height,$popup_width;
  global $l_datasource, $l_name, $l_aka, $l_kind, $l_activity, $l_nafcode;
  global $l_phone,$l_fax, $l_address, $l_postcode,$l_expresspostal,$l_town;
  global $l_country,$l_email,$l_web,$l_comment,$l_add_comment,$l_upd_comment;
  global $l_number, $l_vat, $l_siret, $l_market, $l_coord;
  global $l_insert, $l_update, $l_internal, $l_company, $l_archive;

  $uid = $auth->auth["uid"];
  $lang = get_lang();

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("company_id");
    $num = $co_q->f("company_number");
    $vat = $co_q->f("company_vat");
    $siret = $co_q->f("company_siret");
    $archive = ($co_q->f("company_archive") == 1 ? " checked" : "");
    $name = $co_q->f("company_name");
    $aka = $co_q->f("company_aka");
    $dsrc = $co_q->f("company_datasource_id");
    $type = $co_q->f("company_type_id");
    $act = $co_q->f("company_activity_id");
    $naf = $co_q->f("company_nafcode_id");
    $mark = $co_q->f("company_marketingmanager_id");
    $ad1 = $co_q->f("company_address1");
    $ad2 = $co_q->f("company_address2");
    $ad3 = $co_q->f("company_address3");
    $zip = $co_q->f("company_zipcode");
    $town = $co_q->f("company_town");
    $cdx = $co_q->f("company_expresspostal");
    $ctry = $co_q->f("company_country_iso3166");
    $phone = $co_q->f("company_phone");
    $fax = $co_q->f("company_fax");
    $web = $co_q->f("company_web");
    $email = $co_q->f("company_email");
    $con_num = $co_q->f("company_contact_number");
    $deal_num = $co_q->f("company_deal_number");
    $comment = $co_q->f("company_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();
  } else if ($action == "new") {
    $mark = $auth->auth["uid"];
    $dsrc = $set_dsrc;
    $usercomment = $uid;
    $datecomment = isodate_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($company["company_id"])) { $id = $company["company_id"]; }
  if (isset($company["num"])) { $num = stripslashes($company["num"]); }
  if (isset($company["vat"])) { $vat = stripslashes($company["vat"]); }
  if (isset($company["siret"])) { $siret = stripslashes($company["siret"]); }
  if (isset($company["archive"])) { $archive = ($company["archive"] == 1 ? "checked" : ""); }
  if (isset($company["name"])) { $name = stripslashes($company["name"]); }
  if (isset($company["aka"])) { $aka = stripslashes($company["aka"]); }
  if (isset($company["datasource"])) { $dsrc = $company["datasource"]; }
  if (isset($company["type"])) { $kind = $company["type"]; }
  if (isset($company["activity"])) { $act = $company["activity"]; }
  if (isset($company["naf"])) { $naf = $company["naf"]; }
  if (isset($company["market"])) { $mark = $company["market"]; }
  if (isset($company["ad1"])) { $ad1 = stripslashes($company["ad1"]); }
  if (isset($company["ad2"])) { $ad2 = stripslashes($company["ad2"]); }
  if (isset($company["ad3"])) { $ad3 = stripslashes($company["ad3"]); }
  if (isset($company["zip"])) { $zip = stripslashes($company["zip"]); }
  if (isset($company["town"])) { $town = stripslashes($company["town"]); }
  if (isset($company["cdx"])) { $cdx = stripslashes($company["cdx"]); }
  if (isset($company["country"])) { $ctry = $company["country"]; }
  if (isset($company["phone"])) { $phone = stripslashes($company["phone"]); }
  if (isset($company["fax"])) { $fax = stripslashes($company["fax"]); }
  if (isset($company["web"])) { $web = stripslashes($company["web"]); }
  if (isset($company["email"])) { $email = stripslashes($company["email"]); }
  if (isset($company["comment"])) { $comment = stripslashes($company["comment"]); }
  if (isset($company["add_comment"])) { $add_comment = stripslashes($company["add_comment"]); }
  if (isset($company["usercomment"])) { $usercomment = $company["usercomment"]; }
  if (isset($company["datecomment"])) { $datecomment = $company["datecomment"]; }
  if (isset($company["category1"])) { $cat1 = $company["category1"]; }

  // Conditionnal fields
  // category1, activity, type
  $show_number = (! $cgp_hide["company"]["company_number"]);
  $show_vat = (! $cgp_hide["company"]["company_vat"]);
  $show_siret = (! $cgp_hide["company"]["company_siret"]);
  $show_naf = (! $cgp_hide["company"]["nafcode"]);
  $show_ad3 = (! $cgp_hide["company"]["company_address3"]);
  $show_cdx = (! $cgp_hide["company"]["company_expresspostal"]);

  // some constants
  $csize_add = "48";
  $cmax_add = "64";
  $csize_phone = "24";

  // Number field
  if ($show_number) {
    $block_num = "
    </tr><tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_num\" name=\"tf_num\" value=\"$num\" size=\"30\" maxlength=\"32\" /></td>";
  }

  // Vat field
  if ($show_vat) {
    $block_vat = "
    </tr><tr>
      <td class=\"detailLabel\">$l_vat</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_vat\" name=\"tf_vat\" value=\"$vat\" size=\"20\" maxlength=\"20\" /></td>";
  }

  // Siret field
  if ($show_siret) {
    $block_siret = "
    </tr><tr>
      <td class=\"detailLabel\">$l_siret</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_siret\" name=\"tf_siret\" value=\"$siret\" size=\"14\" maxlength=\"14\" /></td>";
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

  $block_type = of_category_dis_entity_form("company", "type", $types, "mono", $type, "none");
  $block_activity = of_category_dis_entity_form("company", "activity", $acts, "mono", $act, "none");
  // Category select field
  $block_cat1 = of_category_dis_entity_form("company", "category1", $cats1);

  // Naf code select field
  if ($show_naf) {
    $sel_naf = dis_select_company_nafcode($naf_q, "<option value=\"$c_undef\">$l_undef</option>", $naf);

    $block_naf = "
    </tr><tr>      
      <td class=\"detailLabel\">$l_nafcode</td>
      <td class=\"detailForm\">$sel_naf</td>";
  }

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>";
  }
  $sel_market .= "</select>";

  // Address 3 field
  if ($show_ad3) {
    $block_ad3 = "
    </tr><tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ad3\" name=\"tf_ad3\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad3\" /></td>";
  }

  // Cedex field
  if ($show_cdx) {
    $block_cdx = "
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_cdx\" name=\"tf_cdx\" maxlength=\"16\" size=\"16\" value=\"$cdx\" /></td>";
  }

  // Country select
  $sel_ctry = "<select name=\"sel_country\" id=\"sel_country\" onChange=\"
      if (trim(this.form.tf_phone.value) == '') {
        this.form.tf_phone.value = country_phone[this.options[this.selectedIndex].value];
      }
    \">
    <option value=\"$c_undef\">$l_undef</option>";

  $js_ctry = "<script type=\"text/javascript\">
      var country_phone = new Array ();
      country_phone[0] = '';
      ";
  while ($ctry_q->next_record()) {
    $c_iso3166 = $ctry_q->f("country_iso3166");
    $c_name = $ctry_q->f("country_name");
    $c_phone = $ctry_q->f("country_phone");
    $sel_ctry .= "\n<option value=\"$c_iso3166\"";
    if ($c_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">$c_name</option>";
    $js_ctry .= "country_phone['$c_iso3166'] = '$c_phone';\n";
  }
  $sel_ctry .= "</select>";
  $js_ctry .= "
    </script>";

  // Email link
  if (($action=="detailupdate") || ($action=="update")) { 
    if ($email != "") {
      $link_email = "<a href=\"mailto:$email\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"$email\" /></a>";
    } else { $link_email = "&nbsp;"; }
  } else { $link_email = "&nbsp;"; }

  // Web link
  if (($action=="detailupdate") || ($action=="update")) {
    if ($web != "") {
      $link_web = "<a target=\"_blank\" href=\"$web\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_web\" alt=\"$web\" /></a>";
    } else { $link_web = "&nbsp;"; }
  } else { $link_web = "&nbsp;"; }

  if (($action == "detailupdate") || ($action == "update")) {
    $links = html_company_links($co_q);
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

  // Buttons
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <!-- Update button -->
      <input type=\"hidden\" name=\"company_id\" id=\"company_id\" value=\"$id\" />
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
    $js_ctry

    <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_company(this)) return true; else return false;\"
      action=\"".url_prepare("company_index.php")."\">
    <div class=\"detailHead\">$l_company</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_name\" name=\"tf_name\" maxlength=\"96\" size=\"50\" value=\"$name\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_aka</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_aka\" name=\"tf_aka\" maxlength=\"255\" size=\"50\" value=\"$aka\" /></td>
    $block_num
    $block_vat
    $block_siret
    </tr><tr>
      <td class=\"detailLabel\">$l_datasource</td> 
      <td class=\"detailForm\">$sel_dsrc</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
    $block_type
    $block_cat1
    $block_activity
    $block_naf
    </tr><tr>    
      <td class=\"detailLabel\">$l_market</td> 
      <td class=\"detailForm\">$sel_market</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_address 1</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ad1\" name=\"tf_ad1\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad1\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_address 2</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_ad2\" name=\"tf_ad2\" maxlength=\"$cmax_add\" size=\"$csize_add\" value=\"$ad2\" /></td>
    $block_ad3
    </tr><tr>
      <td class=\"detailLabel\">$l_postcode</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_zip\" name=\"tf_zip\" maxlength=\"14\" size=\"6\" value=\"$zip\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_town</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_town\" name=\"tf_town\" maxlength=\"24\" size=\"24\" value=\"$town\" /></td>
    $block_cdx
    </tr><tr>
      <td class=\"detailLabel\">$l_country</td>
      <td class=\"detailForm\">$sel_ctry</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_phone</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_phone\" name=\"tf_phone\" maxlength=\"$csize_phone\" size=\"$csize_phone\" value=\"$phone\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_fax</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_fax\" name=\"tf_fax\" maxlength=\"24\" size=\"$csize_phone\" value=\"$fax\" /></td>
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
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\">
      <script>calendar('tf_datecomment','$datecomment')</script>$sel_usercomment</td>
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
// Display the company delete validation screen
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_company($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("company_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"company_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"company_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_company_del(this.form)) return true; else return false;\" />
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
// Display the context about a company insertion or update
// When similar companies exists we show these and ask confirmation
// Parameters:
//   - $cid       : company id
//   - $co_q      : company database result (at least 1 row)
//   - $company[] : default values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
/////////////////////////////////////////////////////////////////////////////
function dis_company_warn_insert($cid, $co_q, $company) {
  global $display, $l_check_samecompany, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $num = $company["num"];
  $vat = $company["vat"];
  $siret = $company["siret"];
  $archive = ($company["archive"] == 1 ? "1" : "0");
  $name = $company["name"];
  $kind = $company["kind"];
  $dsrc = $company["datasource"];
  $act = $company["activity"];
  $naf = $company["naf"];
  $mark = $company["market"];
  $ad1 = $company["ad1"];
  $ad2 = $company["ad2"];
  $ad3 = $company["ad3"];
  $zip = $company["zip"];
  $town = $company["town"];
  $cdx = $company["cdx"];
  $ctry = $company["country"];
  $phone = $company["phone"];
  $fax = $company["fax"];
  $web = $company["web"];
  $email = $company["email"];
  $com = $company["comment"];
  $compcat = $company["cat"]; 
  $display["msg"] .= display_warn_msg($l_check_samecompany);

  while ($co_q->next_record()) {
    $id = $co_q->f("company_id");
    $samename = $co_q->f("company_name");
    $samezip = $co_q->f("company_zip");
    $dis_same_comp .= "
      <tr><td colspan =\"2\" class=\"detailText\">
         <a class=\"detail\" href=\"" .url_prepare("company_index.php?action=detailconsult&amp;company_id=$id") . "\">
          $samename ($samezip)</a>
      </td></tr>";
  }
  if (is_array($compcat)) {
    foreach ($compcat as $cat) {
    $dis_cat .= "<input type=\"hidden\" name=\"sel_cat[]\" value=\"$cat\" />";
    }
  }

  $hidden_fields = "
    <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
    <input type=\"hidden\" name=\"tf_vat\" value=\"$vat\" />
    <input type=\"hidden\" name=\"tf_siret\" value=\"$siret\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
    <input type=\"hidden\" name=\"sel_act\" value=\"$act\" />
    <input type=\"hidden\" name=\"sel_naf\" value=\"$naf\" />
    <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
    <input type=\"hidden\" name=\"tf_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"tf_ad2\" value=\"$ad2\" />
    <input type=\"hidden\" name=\"tf_ad3\" value=\"$ad3\" />
    <input type=\"hidden\" name=\"tf_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"tf_town\" value=\"$town\" />
    <input type=\"hidden\" name=\"tf_cdx\" value=\"$cdx\" />
    <input type=\"hidden\" name=\"sel_country\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_web\" value=\"$web\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    $dis_cat
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
";

  $block = "
  <table class=\"detail\">
    $dis_same_comp
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" .url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden_fields
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden_fields
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the company administration index
///////////////////////////////////////////////////////////////////////////////
function dis_company_admin_index() {
  global $cgp_hide;

  if (! $cgp_hide["company"]["type"]) {
    $types = of_category_get_ordered("company", "type");
    $block .= of_category_dis_admin_form("company", "type", $types);
  }

  if (! $cgp_hide["company"]["category1"]) {
    $cats1 = of_category_get_ordered("company", "category1");
    $block .= of_category_dis_admin_form("company", "category1", $cats1);
  }

  if (! $cgp_hide["company"]["activity"]) {
    $acts = of_category_get_ordered("company", "activity");
    $block .= of_category_dis_admin_form("company", "activity", $acts);
  }

  if (! $cgp_hide["company"]["nafcode"]) {
    $naf_q = run_query_companynafcode();
    $block .= html_company_nafcode_form($naf_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Company Naf Code section
// Parameters:
//   - $naf_q : Naf Code list database object
///////////////////////////////////////////////////////////////////////////////
function html_company_nafcode_form($naf_q) {
  global $l_naf_manage,$l_naf_exist,$l_naf_code,$l_naf_label,$l_naf_istitle;
  global $l_naf_checkdelete,$l_naf_update,$l_naf_new,$l_naf_insert,$l_naf_no;

  $char_title = '=';

  if ($naf_q->num_rows() > 0) {
    $sel_naf = "<select name=\"sel_naf\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    title = mytext.substr(0, 1);
    pos = mytext.indexOf('-');
    code = mytext.substr(1, pos-1);
    mytext = mytext.substr(pos+2); 
    if (title == '$char_title') {
     forms.form_naf_update.cb_naf_title.checked=true;
    } else {
     forms.form_naf_update.cb_naf_title.checked=false;
    }
    forms.form_naf_update.tf_naf_label.value=mytext;
    forms.form_naf_update.tf_naf_code.value=code;\"
    size=\"10\" style=\"font-family:monospace;font-weight:400;\">";
    while ($naf_q->next_record()) {
      $id = $naf_q->f("companynafcode_id");
      $title = $naf_q->f("companynafcode_title");
      if ($title) {
	$pre = "$char_title";
      } else {
	$pre = ".";
      }
      $code = $naf_q->f("companynafcode_code");
      $label = $naf_q->f("companynafcode_label");
      $sel_naf .= "\n<option value=\"$id\">$pre$code - $label</option>";
    }
    $sel_naf .= "</select>";
  } else {
    $sel_naf = $l_naf_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_naf_manage
      </td>
    </tr><tr>
      <td>

<!-- Naf Code Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_naf_delete\" method=\"post\"
              action=\"" . url_prepare("company_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_naf_exist</td>
              <td class=\"adminLabel\">$sel_naf</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"nafcode_checklink\" />
              <input type=\"submit\" name=\"sub_naf\" value=\"$l_naf_checkdelete\" onclick=\"if (check_company_naf_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Naf Code Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_naf_update\" method=\"get\"
              action=\"" . url_prepare("company_index.php") . "\">
            <input type=\"hidden\" name=\"sel_naf\" value=\"\" />
	    <input type=\"hidden\" name=\"action\" value=\"nafcode_update\" />
            <input type=\"checkbox\" name=\"cb_naf_title\" />
 	    <input type=\"text\" size=\"4\" maxlength=\"4\" name=\"tf_naf_code\" />
            <input type=\"text\" name=\"tf_naf_label\" />
            <input type=\"submit\" name=\"sub_naf\" value=\"$l_naf_update\"
              onclick=\"if (check_company_naf_upd(this.form,document.form_naf_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Naf Code Section -->

        <form name=\"form_naf_new\" method=\"get\"
          action=\"" . url_prepare("company_index.php") . "\">
        <table>
	<tr>
	 <td class=\"adminLabel\">$l_naf_istitle :
	  <input type=\"checkbox\" name=\"cb_naf_title\" value=\"1\"/>
	 </td>
        </tr>
	<tr>
	  <td class=\"adminLabel\">$l_naf_code :
	  <input type=\"text\" name=\"tf_naf_code\" size=\"4\" maxlength=\"4\" />
	  </td>
        </tr>
        <tr>
         <td class=\"adminLabel\">$l_naf_label
	   <input type=\"text\" name=\"tf_naf_label\" />
	 </td>
	</tr>
	<tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"nafcode_insert\" />
            <input type=\"submit\" name=\"sub_naf\" value=\"$l_naf_insert\"
              onclick=\"if (check_company_naf_new(this.form)) return true; else return false;\" />
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
// Display: the Naf Code links
// Parameters:
//   - $company : company hash info : keys used : nafcode
///////////////////////////////////////////////////////////////////////////////
function dis_company_nafcode_links($company) {
  global $display, $l_back, $l_naf_link_company, $l_naf_link_company_no;
  global $l_naf_delete, $l_naf_can_delete, $l_naf_cant_delete;

  $delete_ok = true;
  $id = $company["naf"];
  $label = get_company_nafcode_label($id);
  $obm_q = run_query_company_nafcode_links($id);

  $nb_naf = $obm_q->num_rows();
  if ($nb_naf > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_naf_link_company ($nb_naf)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;company_id=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_naf) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_naf_link_company_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
    $dis_link
    </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_naf_can_delete);
    $dis_del = "<form name=\"form_naf_delete\".
          method=\"post\" action=\"" . url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"nafcode_delete\" />
        <input type=\"hidden\" name=\"sel_naf\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_naf\" value=\"$l_naf_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_naf_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: the Company Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_company_display_pref($prefs) {
  global $l_company_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "company");
  $dis_pref->pref_title = $l_company_display;
  $dis_pref->pref_dis_help = 1;

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company Naf Code select
// Parameters:
//   - $naf_q     : database object with nafcode list
///////////////////////////////////////////////////////////////////////////////
function dis_select_company_nafcode($naf_q, $line1, $selected) {
  global $cgp_hide;

  $show_naf = (! $cgp_hide["company"]["nafcode"]);

  if ($show_naf) {
    $sel_naf = "<select id=\"sel_naf\" name=\"sel_naf\">
    $line1";
    while ($naf_q->next_record()) {
      $n_id = $naf_q->f("companynafcode_id");
      $n_disabled = $naf_q->f("companynafcode_title");
      $n_code = $naf_q->f("companynafcode_code");
      $n_label = $naf_q->f("companynafcode_label");
      $sel_naf .= "\n<option value=\"$n_id\"";
      if ($n_disabled == '1') {
	$sel_naf .= " disabled=\"disabled\"";
      } else {
	$n_code = "&nbsp;&nbsp;&nbsp; $n_code";
      }
      if ($n_id == $selected) { $sel_naf .= " selected=\"selected\""; }
      $sel_naf .= ">$n_code $n_label</option>\n";
    }
    $sel_naf .= "</select>";
  }

  return $sel_naf;
}


?>