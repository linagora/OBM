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
$fieldnames["company_number"] = $l_number;
$fieldnames["company_vat"] = $l_vat;
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
$fieldnames["companytype_label"] = $l_kind;
$fieldnames["companyactivity_label"] = $l_activity;
$fieldnames["companynafcode_code"] = $l_nafcode;
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
  global $path, $company, $set_theme, $ico_mail, $ico_web, $ico_contact_new;

  $ext_url = $company["ext_url"];

  if ($fieldname == "company_name") {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("company_id").",'".addslashes($OD->data_set->f("company_name"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f("company_id").");";
    } else {
      $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
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
      $res["name"] = "<img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\" />";
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
      $res["name"] = "<img src=\"/images/$set_theme/$ico_web\" alt=\"$web\" />";
      $res["txt_name"] = $web;
    }
  }

  else if ($fieldname == "company_contact_number") {
    $res["name"] = $OD->data_set->f("company_contact_number");
    if (($res["name"] > 0) && $link_ok) {
      $res["url"] = "$path/contact/contact_index.php?action=search&amp;param_company=".$OD->data_set->f("company_id")."&amp;tf_company=".urlencode("".$OD->data_set->f("company_name"));
    }
    $res["align"] = "center";
  }

  else if (($fieldname == "company_new_contact") && $link_ok){
    $res["align"] = "center";
    $market = $OD->data_set->f("company_marketingmanager_id");
    $email = $OD->data_set->f("company_email");
    $ctry = $OD->data_set->f("company_country_iso3166");
    $res["url"] = "$path/contact/contact_index.php?action=new&amp;param_company=".$OD->data_set->f("company_id")."&amp;sel_market=$market&amp;sel_ctry=$ctry&amp;tf_email=$email";
    $res["name"] = "<img src=\"/images/$set_theme/$ico_contact_new\" alt=\"\" />";
    $res["txt_name"] = " ";
  }

  else if ($fieldname == "company_deal_number") {
    $res["name"] = $OD->data_set->f("company_deal_number") . " / ";
    $res["name"] .= $OD->data_set->f("company_deal_total");

    $c_id = $OD->data_set->f("company_id");
    if (($res["name"] != "0 / 0") && $link_ok) {
      $res["url"] = "$path/deal/deal_index.php?action=search&amp;param_company=$c_id";
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
// Display Company search form                                               //
// Parameters:
//   - $company[] : hash with company values
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_form($company="") {

  $type_q = run_query_companytype();
  $cats = get_ordered_companycat();
  $act_q = run_query_companyactivity();
  $naf_q = run_query_companynafcode();
  $usr_q = run_query_userobm_active();
  $ctry_q = run_query_country_for_lang();
  $dsrc_q = run_query_datasource();
  $block .= html_company_search_form($type_q, $cats, $act_q, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company search Form
// Parameters:
//   - $kind_q    : database object with kind list
//   - $cats      : array of company categories
//   - $act_q     : database object with activity list
//   - $naf_q     : database object with nafcode list
//   - $usr_q     : database object with userobm list
//   - $ctry_q    : database object with country list
//   - $dsrc_q    : database object with datasource list
//   - $company[] : default form values
//     keys used  : archive, name, kind, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form($kind_q, $cats, $act_q, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company) {
  global $l_company_name, $l_postcode, $l_kind, $l_archive, $l_phone;
  global $l_market, $l_activity, $l_country, $l_town, $l_after, $l_before; 
  global $l_all, $l_find, $l_fuzzy, $l_datasource, $l_nafcode, $l_category;
  global $display, $c_all, $cgp_hide, $l_select_company;

  $popup = $company["popup"];
  $archive = ($company["archive"] == "1" ? "checked = \"checked\"" : "");
  $name = stripslashes($company["name"]);
  $phone = stripslashes($company["phone"]);
  $town = stripslashes($company["town"]);
  $kind = $company["kind"];
  $cat = $company["cat"];
  $act = $company["activity"];
  $naf = $company["nafcode"];
  $ctry = $company["country"];
  $mark = $company["marketing_manager"];
  $dsrc = $company["datasource"];
  $zip = stripslashes($company["zip"]);
  $dateafter = stripslashes($company["dateafter"]);
  $datebefore = stripslashes($company["datebefore"]);
  $fuzzy = ($company["fuzzy"] == "1" ? "checked = \"checked\"" : "");

  $show_kind = (! $cgp_hide["company"]["companytype_label"]);
  $show_cat = (! $cgp_hide["company"]["category"]);
  $show_act = (! $cgp_hide["company"]["companyactivity_label"]);
  $show_naf = (! $cgp_hide["company"]["companynafcode_code"]);

  // Kind select
  if ($show_kind) {
    $sel_kind = "<select id=\"sel_kind\" name=\"sel_kind\">
    <option value=\"$c_all\">$l_all</option>";
    while ($kind_q->next_record()) {
      $k_id = $kind_q->f("companytype_id");
      $sel_kind .= "\n<option value=\"$k_id\"";
      if ($k_id == $kind) { $sel_kind .= " selected=\"selected\""; }
      $sel_kind .= ">". $kind_q->f("companytype_label") . "</option>\n";
    }
    $sel_kind .= "</select>";

    $block_kind = "
    <div class=\"searchLabel\">$l_kind<br />
      $sel_kind
    </div>";
  }

  // Category select
  if ($show_cat) {
    $sel_cat = "<select id=\"sel_cat\" name=\"sel_cat\">
    <option value=\"$c_all\">$l_all</option>";
    if (is_array($cats)) {
      foreach ($cats as $one_cat) {
	$c_id = $one_cat["id"];
	$c_code = str_pad($one_cat["code"], 8, ".");
	$c_label = $one_cat["label"];
	$sel_cat .= "\n<option value=\"$c_id\"";
	if ($c_id == $cat) { $sel_cat .= " selected=\"selected\""; }
	$sel_cat .= ">$c_code $c_label</option>\n";
      }
    }
    $sel_cat .= "</select>";

    $block_cat = "
    <div class=\"searchLabel\">$l_category<br />
      $sel_cat
    </div>";
  }

  // Activity select
  if ($show_act) {
    $sel_act = "<select id=\"sel_act\" name=\"sel_act\">
    <option value=\"$c_all\">$l_all</option>";
    while ($act_q->next_record()) {
      $a_id = $act_q->f("companyactivity_id");
      $sel_act .= "\n<option value=\"$a_id\"";
      if ($a_id == $act) { $sel_act .= " selected=\"selected\""; }
      $sel_act .= ">". $act_q->f("companyactivity_label") . "</option>\n";
    }
    $sel_act .= "</select>";

    $block_act = "
    <div class=\"searchLabel\">$l_activity<br />
      $sel_act
    </div>";
  }

  // Naf Code select
  if ($show_naf) {
    $sel_naf = dis_select_companynafcode($naf_q, "<option value=\"$c_all\">$l_all</option>", $naf);

    $block_naf = "
    <div class=\"searchLabel\">$l_nafcode<br />
      $sel_naf
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
  <form method=\"get\" name=\"f_societe\" id=\"f_societe\" action=\"$url\">
  <div class=\"search\">
    <div class=\"searchLabel\">$l_company_name<br />
      <input type=\"text\" name=\"tf_name\" id=\"tf_name\" size=\"24\"
      value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" id=\"tf_phone\" size=\"10\"
      value=\"$phone\" />
    </div>
    $block_kind
    $block_cat
    $block_act
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
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
    </div>
    <div class=\"searchLabel\">$l_datasource<br />
      $sel_dsrc
    </div>
    <div class=\"searchLabel\">$l_after<br />
      <input name=\"tf_dateafter\" size=\"10\" maxlength=\"10\"
        value=\"$dateafter\" />
    </div>
    <div class=\"searchLabel\">$l_before<br />
      <input name=\"tf_datebefore\" size=\"10\" maxlength=\"10\"
        value=\"$datebefore\" />
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
// Display the Company search result                                         //
// Parameters:
//   - $company[] : company search criteria
//     keys used  : name, kind, zip
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_list($company) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $popup = $company["popup"];
  $pref_q = run_query_display_pref($auth->auth["uid"], "company");
  $obm_q = run_query_search($company, $new_order, $order_dir);
  $nb_company = $obm_q->num_rows_total();
  if ($nb_company == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_company_search_list($obm_q,$pref_q,$nb_company,$company,$popup);
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
function html_company_search_list($comp_q, $pref_q, $nb_company,$company,$popup) {
  global $display, $l_found,$l_close;

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
  $kind = $company["kind"];
  $cat = $company["cat"];
  $act = $company["activity"];
  $naf = $company["nafcode"];
  $zip = urlencode(stripslashes($company["zip"]));
  $market = $company["marketing_manager"];
  $dsrc = $company["datasource"];
  $dateafter = stripslashes($company["dateafter"]);
  $datebefore = stripslashes($company["datebefore"]);

  $url = url_prepare("company_index.php?action=search&amp;tf_name=$name&amp;tf_phone=$phone&amp;sel_kind=$kind&amp;sel_cat=$cat&amp;sel_act=$act&amp;sel_naf=$naf&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_market=$market&amp;tf_town=$town&amp;sel_ctry=$ctry&amp;sel_dsrc=$dsrc&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore$url_ext");

  $comp_d = new OBM_DISPLAY("DATA", $pref_q, "company");
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
  $display["msg"] = display_info_msg("$nb_company $l_found");
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
  global $ico_invoice;
  global $l_new, $l_header_contact, $l_header_project, $l_header_deal;
  global $l_header_contract, $l_header_invoice, $l_header_document;
  global $l_document_add;
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
    $url_con_list = url_prepare("$path/contact/contact_index.php?action=search&amp;param_company=$id&amp;tf_company=$uname");
    $url_con_new = url_prepare("$path/contact/contact_index.php?action=new&amp;param_company=$id&amp;sel_market=$market&amp;sel_ctry=$ctry&amp;tf_email=$email");
    $block_contact = "
  <div class=\"linkTitle\">:: $l_header_contact</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_con_list\"><img src=\"/images/$set_theme/$ico_contact\" /></a>
        <a href=\"$url_con_list\">$l_header_contact ($con_num)</a></li>
    <li><a href=\"$url_con_new\"><img src=\"/images/$set_theme/$ico_contact_new\" /></a>
        <a href=\"$url_con_new\">$l_new</a></li>
  </ul>";
  }

  // Deal
  if ($cgp_show["module"]["deal"]) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;param_company=$id&amp;tf_company_name=$uname");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;param_company=$id&amp;tf_company_name=$uname&amp;cb_archive=1");
    $url_deal_new = url_prepare("$path/deal/deal_index.php?action=new&amp;param_company=$id&amp;tf_company_name=$uname");
    $block_deal = "
  <div class=\"linkTitle\">:: $l_header_deal</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_deal_list\"><img src=\"/images/$set_theme/$ico_deal\" /></a>
        $l_header_deal (
        <a href=\"$url_deal_list\">$deal_num</a> /
        <a href=\"$url_deal_total\">$deal_total</a> )</li>
    <li><a href=\"$url_deal_new\"><img src=\"/images/$set_theme/$ico_deal_new\" /></a>
        <a href=\"$url_deal_new\">$l_new</a></li>
  </ul>";
  }

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=company");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=company");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/company/company_index.php")."&amp;ext_id=$id&amp;ext_target=$l_company";
    $nb_document = run_query_document_nb ($id, "company");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_header_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"/images/$set_theme/$ico_document\" /></a>
        <a href=\"$url_doc\">$l_header_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_company'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Project
  if ($cgp_show["module"]["project"]) {
    $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;param_company=$id");
    $url_pro_tot = url_prepare("$path/project/project_index.php?action=search&amp;param_company=$id&amp;cb_archive=1");
    $nb_pro = run_query_linked_project_nb($id, "company");
    $nb_pro_total = run_query_linked_project_nb ($id, "company", 1);
    $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;param_company=$id&amp;tf_company_name=$uname");
    $block_project = "
  <div class=\"linkTitle\">:: $l_header_project</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_pro\"><img src=\"/images/$set_theme/$ico_project\" /></a>
        $l_header_project ( <a href=\"$url_pro\">$nb_pro</a> /
        <a href=\"$url_pro_tot\">$nb_pro_total</a> )</li>
    <li><a href=\"$url_pro_new\"><img src=\"/images/$set_theme/$ico_project\" /></a>
        <a href=\"$url_pro_new\">$l_new</a></li>
  </ul>";
  }

  // Contract
  if ($cgp_show["module"]["contract"]) {
    $nb_ctra = run_query_linked_contract_nb($id, "company");
    $url_ctra = url_prepare("$path/contract/contract_index.php?action=search&amp;param_company=$id");
    $url_ctra_new = url_prepare("$path/contract/contract_index.php?action=new&amp;param_company=$id&amp;tf_company_name=$uname");

     $block_contract = "
  <div class=\"linkTitle\">:: $l_header_contract</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_ctra\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_ctra\">$l_header_contract ($nb_ctra)</a></li>
    <li><a href=\"$url_ctra_new\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_ctra_new\">$l_new</a></li>
  </ul>";
  }

  // Invoice
  if ($cgp_show["module"]["invoice"]) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_company=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_company=$id&amp;cb_archive=1");
    $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;param_company=$id&amp;company_name=$uname");
    $nb_inv_tot = run_query_linked_invoice_nb($id, "company", 1);
    $nb_inv = run_query_linked_invoice_nb($id, "company");
    $block_invoice = "
  <div class=\"linkTitle\">:: $l_header_invoice</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_inv\"><img src=\"/images/$set_theme/$ico_invoice\" alt=\"\" /></a>
        $l_header_invoice ( <a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a> )</li>
    <li><a href=\"$url_inv_new\"><img src=\"/images/$set_theme/$ico_invoice\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>
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
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Company Consultation                                         //
// Parameters:
//   - $co_q : company database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_consult($co_q, $cat_q) {
  global $set_theme, $ico_contact, $ico_contact_new, $ico_web, $ico_mail;
  global $l_name, $l_aka, $l_number, $l_kind, $l_activity, $l_nafcode, $l_vat;
  global $l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal;
  global $l_town,$l_country,$l_email,$l_web,$l_comment,$l_market, $l_archive;
  global $l_insert,$l_update, $l_company, $l_coord,$l_category;
  global $display, $c_yes, $c_no, $cgp_hide;

  $id = $co_q->f("company_id");
  $num = $co_q->f("company_number");
  $vat = $co_q->f("company_vat");
  $archive = ($co_q->f("company_archive") == 1 ? $c_yes : $c_no);
  $name = $co_q->f("company_name");
  $aka = $co_q->f("company_aka");
  $kind = $co_q->f("companytype_label");
  $act = $co_q->f("companyactivity_label");
  $naf = $co_q->f("companynafcode_code")." - ".$co_q->f("companynafcode_label");
  $mark = $co_q->f("company_marketingmanager_id");
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
  $con_num = $co_q->f("company_contact_number");
  $deal_num = $co_q->f("company_deal_number");
  $com = beautify_comment(nl2br($co_q->f("company_comment")));

  if ($mark > 0)
    $lmarket = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  else
    $lmarket = "&nbsp;";


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
  $show_number = (! $cgp_hide["company"]["company_number"]);
  $show_vat = (! $cgp_hide["company"]["company_vat"]);
  $show_kind = (! $cgp_hide["company"]["companytype_label"]);
  $show_act = (! $cgp_hide["company"]["companyactivity_label"]);
  $show_naf = (! $cgp_hide["company"]["companynafcode_code"]);
  $show_cat = (! $cgp_hide["company"]["category"]);
  $show_ad3 = (! $cgp_hide["company"]["company_address3"]);

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

  // Kind field
  if ($show_kind) {
    $block_kind = "
    <tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailText\">$kind</td>
    </tr>";
  }

  // Activity field
  if ($show_act) {
    $block_act = "
    <tr> 
      <td class=\"detailLabel\">$l_activity</td>
      <td class=\"detailText\">$act</td>
    </tr>";
  }

  // Naf code field
  if ($show_naf) {
    $block_naf = "
    <tr> 
      <td class=\"detailLabel\">$l_nafcode</td>
      <td class=\"detailText\">$naf</td>
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

  // Email link
  if ($email != "") {
    $link_email = "<a href=\"mailto:$email\">
      <img border=\"0\" src=\"/images/$set_theme/$ico_mail\"
      alt=\"$email\" /></a>";
  } else {
    $link_email = "&nbsp;";
  }

  $cat_disp = "<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>";
  if ( ($show_cat) && ($cat_q->nf() > 0) ) {
    $cat_a = get_ordered_category($cat_q);
    $i = 0;
    foreach($cat_a as $cat_info) {
      if ($i == 0) {
	$cat_disp .= "<tr>";
      }
      $label = $cat_info["code"];
      $label .= "-".$cat_info["label"];    
      $i++;
      $cat_disp .= "<td style=\"border: 1px solid\" class=\"detailText\"> $label </td>";
      if ($i==3) {
	$cat_disp .= "</tr>";
	$i = 0;
      }    
    }
  }
  // Category field
  if ($show_cat) {
    $block_cat = "
<div class=\"detailHead\">$l_category</div>
  
  <table class=\"detail\">
    $cat_disp
  </table>";
  }  
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
      <img border=\"0\" src=\"/images/$set_theme/$ico_web\"
      alt=\"$web\" /></a>";
  } else {
    $link_web = "&nbsp;";
  }

  $display["link"] = html_company_links($co_q);
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
//   - $dsrc_q    : datasource database result 
//   - $kind_q    : kind database result 
//   - $act_q     : activity database result 
//   - $naf_q     : nafcode database result 
//   - $usr_q     : userobm database result 
//   - $cats      : ordered array of category
//   - $compcat   : array of category referenced by the company
//   - $ctry_q    : country database result 
//   - $company[] : default values
//     keys used  : id, num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email ,com
///////////////////////////////////////////////////////////////////////////////
function html_company_form($action, $co_q, $dsrc_q, $kind_q, $act_q, $naf_q, $usr_q, $cats, $compcat, $ctry_q, $company) {
  global $display, $auth, $cgp_hide, $set_dsrc, $l_none, $ico_add;
  global $set_theme,$ico_web,$ico_mail,$l_category,$popup_height,$popup_width;
  global $l_datasource, $l_name, $l_aka, $l_kind, $l_activity, $l_nafcode;
  global $l_phone,$l_fax, $l_address, $l_postcode,$l_expresspostal,$l_town;
  global $l_country,$l_email,$l_web,$l_comment,$l_add_comment,$l_upd_comment;
  global $l_number, $l_vat, $l_market, $l_coord;
  global $l_insert, $l_update, $l_internal, $l_company, $l_archive;

  $uid = $auth->auth["uid"];
  $lang = get_lang();

  // if update mode and first display values are taken from database
  if ($action == "detailupdate") {
    $id = $co_q->f("company_id");
    $num = $co_q->f("company_number");
    $vat = $co_q->f("company_vat");
    $archive = ($co_q->f("company_archive") == 1 ? " checked" : "");
    $name = $co_q->f("company_name");
    $aka = $co_q->f("company_aka");
    $dsrc = $co_q->f("company_datasource_id");
    $kind = $co_q->f("company_type_id");
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
  } else  if ($action == "new") {
    $mark = $auth->auth["uid"];
    $dsrc = $set_dsrc;
    $usercomment = $uid;
    $datecomment = isodate_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($company["id"])) { $id = $company["id"]; }
  if (isset($company["num"])) { $num = stripslashes($company["num"]); }
  if (isset($company["vat"])) { $vat = stripslashes($company["vat"]); }
  if (isset($company["archive"])) { $archive = ($company["archive"] == 1 ? "checked" : ""); }
  if (isset($company["name"])) { $name = stripslashes($company["name"]); }
  if (isset($company["aka"])) { $aka = stripslashes($company["aka"]); }
  if (isset($company["datasource"])) { $dsrc = $company["datasource"]; }
  if (isset($company["kind"])) { $kind = $company["kind"]; }
  if (isset($company["activity"])) { $act = $company["activity"]; }
  if (isset($company["nafcode"])) { $naf = $company["nafcode"]; }
  if (isset($company["marketing_manager"])) { $mark = $company["marketing_manager"]; }
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
  if (isset($company["com"])) { $comment = stripslashes($company["com"]); }
  if (isset($company["add_comment"])) { $add_comment = stripslashes($company["add_comment"]); }
  if (isset($company["usercomment"])) { $usercomment = $company["usercomment"]; }
  if (isset($company["datecomment"])) { $datecomment = $company["datecomment"]; }
  if (isset($company["cat"])) { $compcat = $company["cat"]; }

  // Conditionnal fields
  $show_number = (! $cgp_hide["company"]["company_number"]);
  $show_vat = (! $cgp_hide["company"]["company_vat"]);
  $show_kind = (! $cgp_hide["company"]["companytype_label"]);
  $show_act = (! $cgp_hide["company"]["companyactivity_label"]);
  $show_naf = (! $cgp_hide["company"]["companynafcode_code"]);
  $show_cat = (! $cgp_hide["company"]["category"]);
  $show_ad3 = (! $cgp_hide["company"]["company_address3"]);

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

  // Kind select field
  if ($show_kind) {
    $sel_kind = "<select name=\"sel_kind\" id=\"sel_kind\">
    <option value=\"0\">$l_none</option>";
    while ($kind_q->next_record()) {
      $k_id = $kind_q->f("companytype_id");
      $sel_kind .= "\n<option value=\"$k_id\"";
      if ($k_id == $kind) { $sel_kind .= " selected=\"selected\""; }
      $sel_kind .= ">". $kind_q->f("companytype_label") . "</option>\n";
    }
    $sel_kind .= "</select>";
    $block_kind = "
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\">$sel_kind</td>";
  }

  // Cat select field
  if ($show_cat) {
    $sel_cat = "<select multiple=\"multiple\" name=\"sel_cat[]\" id=\"sel_cat\"  style=\"font-family:monospace;font-weight:400;\">";
    $j = 0;
    if (is_array($cats)) {
      foreach ($cats as $cat_info) {
	$c_id = $cat_info["id"];
	$c_code = $cat_info["code"];
	$c_label = $cat_info["label"];
	if ($compcat[$j]["id"] == $c_id) { 
	  $sel_cat .= "\n<option value=\"$c_id\"";
	  $sel_cat .= " selected=\"selected\""; 
	  $j++;
	  $length = 8 - strlen($c_code);
	  $label = $c_code;
	  for ($i=0;$i<$length;$i++) {
	    $label .= "&nbsp;";
	  }
	  $label .= "$c_label";
	  $sel_cat .= ">$label</option>";
	}
      }
    }
    $sel_cat .= "</select>";

    $url = "company_index.php?action=ext_get_cat_ids&amp;popup=1&amp;ext_target=sel_cat"; 
    $block_cat = "
    </tr><tr>
      <td class=\"detailLabel\">$l_category</td>
      <td class=\"detailForm\">    
       <table>
	<tr><td>
	  $sel_cat 
	</td><td>
	  <a href=\"javascript: return false;\" style=\"float:right;\"
	   onclick=\"window.open('$url','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	   <img src=\"/images/$set_theme/$ico_add\" /></a>
	</td></tr>
       </table>
      </td>";
  }

  // Activity select field
  if ($show_act) {
    $sel_act = "<select name=\"sel_act\" id=\"sel_act\">
    <option value=\"0\">$l_none</option>";
    while ($act_q->next_record()) {
      $a_id = $act_q->f("companyactivity_id");
      $sel_act .= "\n<option value=\"$a_id\"";
      if ($a_id == $act) { $sel_act .= " selected=\"selected\""; }
      $sel_act .= ">". $act_q->f("companyactivity_label") . "</option>\n";
    }
    $sel_act .= "</select>";

    $block_act = "
    </tr><tr>      
      <td class=\"detailLabel\">$l_activity</td>
      <td class=\"detailForm\">$sel_act</td>";
  }

  // Naf code select field
  if ($show_naf) {
    $sel_naf = dis_select_companynafcode($naf_q, "<option value=\"0\">$l_none</option>", $naf);

    $block_naf = "
    </tr><tr>      
      <td class=\"detailLabel\">$l_nafcode</td>
      <td class=\"detailForm\">$sel_naf</td>";
  }

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">
    <option value=\"0\">$l_none</option>";
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

  // Country select
  $sel_ctry = "<select name=\"sel_ctry\" id=\"sel_ctry\" onChange=\"
      if (trim(this.form.tf_phone.value) == '') {
        this.form.tf_phone.value = country_phone[this.options[this.selectedIndex].value];
      }
    \">
    <option value=\"0\">$l_none</option>";

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
        <img src=\"/images/$set_theme/$ico_mail\" alt=\"$email\" /></a>";
    } else { $link_email = "&nbsp;"; }
  } else { $link_email = "&nbsp;"; }

  // Web link
  if (($action=="detailupdate") || ($action=="update")) {
    if ($web != "") {
      $link_web = "<a target=\"_blank\" href=\"$web\">
        <img src=\"/images/$set_theme/$ico_web\" alt=\"$web\" /></a>";
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
    $js_ctry

    <form method=\"post\" name=\"form_company\"
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
    </tr><tr>
      <td class=\"detailLabel\">$l_datasource</td> 
      <td class=\"detailForm\">$sel_dsrc</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
    $block_kind
    $block_cat
    $block_act
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
    </tr><tr>
      <td class=\"detailLabel\">$l_expresspostal</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_cdx\" name=\"tf_cdx\" maxlength=\"8\" size=\"8\" value=\"$cdx\" /></td>
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
// Display: Display and check the company links (and delete form if ok)
// Parameters:
//   - $p_id : company id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contact, $l_link_contact_no;
  global $l_link_contract, $l_link_contract_no, $l_link_project, $l_link_project_no;
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
        <a class=\"detailLabel\" href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$cid") . "\">$cname</a>
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
        <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" .url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_linkd = "
      <tr>
        <td class=\"detailHead\">$l_link_deal_no</td>
      </tr>";
  }

  // Links from Contracts
  $obm_q = run_query_company_contract_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_link_cr = "<tr><td class=\"detailHead\">$l_link_contract</td></tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $display_link_cr .= "
      <tr>
        <td class=\"detailLabel\">
          <a class=\"detailLabel\" href=\"".url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$cid")."\">
          $clabel</a>
        </td>
      </tr>";
    }
  } else {
    $display_link_cr = "<tr><td class=\"detailHead\">$l_link_contract_no</td></tr>";
  }

  // Links from Project
  $obm_q = run_query_company_project_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkp ="<tr><td class=\"detailHead\">$l_link_project</td></tr>";
    while ($obm_q->next_record()) {
      $pid = $obm_q->f("project_id");
      $pname = $obm_q->f("project_name");
      $display_linkp .= "
    <tr>
      <td class=\"detailLabel\">
        <a class=\"detailLabel\" href=\"" . url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=$pid") . "\">$pname</a>
      </td>
    </tr>";
    }
  } else {
    $display_linkp = "
    <tr>
      <td class=\"detailHead\">$l_link_project_no</td>
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
   $display_link_cr
   $display_linkp
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
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
/////////////////////////////////////////////////////////////////////////////
function dis_company_warn_insert($cid, $co_q, $company) {
  global $display, $l_check_samecompany, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $num = $company["num"];
  $vat = $company["vat"];
  $archive = ($company["archive"] == 1 ? "1" : "0");
  $name = $company["name"];
  $kind = $company["kind"];
  $dsrc = $company["datasource"];
  $act = $company["activity"];
  $naf = $company["nafcode"];
  $mark = $company["marketing_manager"];
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
  $com = $company["com"];
  $compcat = $company["cat"]; 
  $display["msg"] .= display_warn_msg($l_check_samecompany);

  while ($co_q->next_record()) {
    $id = $co_q->f("company_id");
    $samename = $co_q->f("company_name");
    $samezip = $co_q->f("company_zip");
    $dis_same_comp .= "
      <tr><td colspan =\"2\" class=\"detailText\">
         <a class=\"detail\" href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$id") . "\">
          $samename ($samezip)</a>
      </td></tr>";
  }
  if (is_array($compcat)) {
    foreach ($compcat as $cat) {
    $dis_cat .= "<input type=\"hidden\" name=\"sel_cat[]\" value=\"$cat\" />";
    }
  }
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
    <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
    <input type=\"hidden\" name=\"tf_vat\" value=\"$vat\" />
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
    <input type=\"hidden\" name=\"sel_ctry\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_web\" value=\"$web\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    $dis_cat
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("company_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
    <input type=\"hidden\" name=\"tf_vat\" value=\"$vat\" />
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
    <input type=\"hidden\" name=\"sel_ctry\" value=\"$ctry\" />
    <input type=\"hidden\" name=\"tf_phone\" value=\"$phone\" />
    <input type=\"hidden\" name=\"tf_fax\" value=\"$fax\" />
    <input type=\"hidden\" name=\"tf_web\" value=\"$web\" />
    <input type=\"hidden\" name=\"tf_email\" value=\"$email\" />
    $dis_cat
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

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
    <input type=\"hidden\" name=\"param_company\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Display the company administration index                                  //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {
  global $cgp_hide;

  if (! $cgp_hide["company"]["companytype_label"]) {
    $kind_q = run_query_companytype();
    $block .= html_company_kind_form($kind_q);
  }

  if (! $cgp_hide["company"]["category"]) {
    $cats = get_ordered_companycat();
    $block .= html_company_cat_form($cats);
  }

  if (! $cgp_hide["company"]["companyactivity_label"]) {
    $act_q = run_query_companyactivity();
    $block .= html_company_activity_form($act_q);
  }

  if (! $cgp_hide["company"]["companynafcode_code"]) {
    $naf_q = run_query_companynafcode();
    $block .= html_company_nafcode_form($naf_q);
  }

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
    </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind links                                                   //
// Parameters:
//   - $company : company hash info : keys used : kind
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($company) {
  global $display, $l_back, $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;

  $delete_ok = true;
  $id = $company["kind"];
  $label = get_kind_label($id);
  $obm_q = run_query_kind_links($id);

  $nb_kind = $obm_q->num_rows();
  if ($nb_kind > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_kind_link_company ($nb_kind)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_kind) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_kind_link_company_no</td>
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
    $display["msg"] .= display_ok_msg($l_kind_can_delete);
    $dis_del = "<form name=\"form_kind_delete\".
          method=\"post\" action=\"" . url_prepare("company_index.php") . "\">
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
// Display: Company Category section
// Parameters:
//   - $cats : Category ordered array
///////////////////////////////////////////////////////////////////////////////
function html_company_cat_form($cats) {
  global $l_cat_manage,$l_cat_exist,$l_cat_code,$l_cat_label,$l_cat_no;
  global $l_cat_checkdelete,$l_cat_update,$l_cat_new,$l_cat_insert;

  $sel_cat = "<select name=\"sel_cat\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('&nbsp;');
    pos2 = mytext.indexOf(' ');
    ext = mytext.substr(0, pos);
    mytext = mytext.substr(pos2+1); 
    forms.form_cat_update.tf_cat.value=mytext;
    forms.form_cat_update.tf_cat_code.value=ext;\"
    size=\"10\" style=\"font-family:monospace;font-weight:400;\">";

  if (is_array($cats)) {
    foreach ($cats as $one_cat) {
      $c_id = $one_cat["id"];
      $code_length = strlen($one_cat["code"]);
      $c_code = $one_cat["code"];
      for ($i=$code_length; $i<8; $i++) {
        $c_code .= "&nbsp;";
      }
      $c_label = $one_cat["label"];
      $sel_cat .= "\n<option value=\"$c_id\"";
      if ($c_id == $cat) { $sel_cat .= " selected=\"selected\""; }
      $sel_cat .= ">$c_code $c_label</option>\n";
    }
    $sel_cat .= "</select>";
  }
  else {
    $sel_cat = $l_cat_no;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_cat_manage
      </td>
    </tr><tr>
      <td>

<!-- Category Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_cat_delete\" method=\"post\"
              action=\"" . url_prepare("company_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_cat_exist</td>
              <td class=\"adminLabel\">$sel_cat</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"cat_checklink\" />
              <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_checkdelete\" onclick=\"if (check_cat_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Category Update Section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <form name=\"form_cat_update\" method=\"get\"
              action=\"" . url_prepare("company_index.php") . "\">
            <input type=\"hidden\" name=\"sel_cat\" value=\"\" />
	    <input type=\"hidden\" name=\"action\" value=\"cat_update\" />
	    <input type=\"text\" size=\"8\" name=\"tf_cat_code\" />
            <input type=\"text\" name=\"tf_cat\" />
            <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_update\"
              onclick=\"if (check_cat_upd(this.form,document.form_cat_delete)) return true; else return false;\" />
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Category Section -->

        <form name=\"form_cat_new\" method=\"get\"
          action=\"" . url_prepare("company_index.php") . "\">
        <table>
        <tr>
         <td class=\"adminLabel\">$l_cat_label
	   <input type=\"text\" name=\"tf_cat\" />
	 </td>
	</tr>
	<tr>
	 <td class=\"adminLabel\">$l_cat_code :
	  <input type=\"text\" name=\"tf_cat_code\" size=\"8\" />
	 </td>
        </tr>
	<tr>
          <td class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"cat_insert\" />
            <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_insert\"
              onclick=\"if (check_cat_new(this.form)) return true; else return false;\" />
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
// Displays the cat links                                                   //
// Parameters:
//   - $company : company hash info : keys used : cat
///////////////////////////////////////////////////////////////////////////////
function dis_cat_links($company) {
  global $display, $l_back, $l_cat_link_company, $l_cat_link_company_no;
  global $l_cat_delete, $l_cat_can_delete, $l_cat_cant_delete;

  $delete_ok = true;
  $id = $company["cat"];
  $label = get_cat_label($id);
  $obm_q = run_query_cat_links($id);

  $nb_cat = $obm_q->num_rows();
  if ($nb_cat > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_cat_link_company ($nb_cat)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_cat) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_cat_link_company_no</td>
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
    $display["msg"] .= display_ok_msg($l_cat_can_delete);
    $dis_del = "<form name=\"form_cat_delete\".
          method=\"post\" action=\"" . url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"cat_delete\" />
        <input type=\"hidden\" name=\"sel_cat\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cat_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }


  return $block;
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
  global $display, $l_back, $l_act_link_company, $l_act_link_company_no;
  global $l_act_delete, $l_act_can_delete, $l_act_cant_delete;

  $delete_ok = true;
  $id = $company["activity"];
  $label = get_activity_label($id);
  $obm_q = run_query_activity_links($id);

  $nb_act = $obm_q->num_rows();
  if ($nb_act > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_act_link_company ($nb_act)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("company_id");
      $cname = $obm_q->f("company_name");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_act) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_act_link_company_no</td>
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
    $display["msg"] .= display_ok_msg($l_act_can_delete);
    $dis_del = "<form name=\"form_act_delete\".
          method=\"post\" action=\"" . url_prepare("company_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"activity_delete\" />
        <input type=\"hidden\" name=\"sel_act\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_act\" value=\"$l_act_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_act_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
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
              <input type=\"submit\" name=\"sub_naf\" value=\"$l_naf_checkdelete\" onclick=\"if (check_naf_checkdel(this.form)) return true; else return false;\" />
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
              onclick=\"if (check_naf_upd(this.form,document.form_naf_delete)) return true; else return false;\" />
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
              onclick=\"if (check_naf_new(this.form)) return true; else return false;\" />
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
function dis_nafcode_links($company) {
  global $display, $l_back, $l_naf_link_company, $l_naf_link_company_no;
  global $l_naf_delete, $l_naf_can_delete, $l_naf_cant_delete;

  $delete_ok = true;
  $id = $company["nafcode"];
  $label = get_nafcode_label($id);
  $obm_q = run_query_nafcode_links($id);

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
        <a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;param_company=$cid") . "\">$cname</a>
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
// Display the Company Naf Code select
// Parameters:
//   - $naf_q     : database object with nafcode list
///////////////////////////////////////////////////////////////////////////////
function dis_select_companynafcode($naf_q, $line1, $selected) {
  global $cgp_hide;

  $show_naf = (! $cgp_hide["company"]["companynafcode_code"]);

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


///////////////////////////////////////////////////////////////////////////////
// Display: category Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_category_tree($company) {
  global $l_close,$l_add,$l_cat_no;

  $popup = $company["popup"];
  $obm_d = run_query_get_tree();
  if ($popup) {
    $link = "<a class=\"categoryRepository\" href=\"\" onclick=\"fill_ext_form('/');return false;\">";
    $link_end ="</a>";
  }
  $name = '/';
  $kind = "0";
  $realpath = $path.$name;
  
  $block.= "
  <div class=\"categoryTree\">
   <form onsubmit=\"fill_ext_form2(this);window.close();return false;\">
   ";
   $cat_a = get_ordered_category($obm_d);
   if(is_array($cat_a)) {
     foreach($cat_a as $cat_info) {
       $hash_code = explode(".",$cat_info["code"]);
       $deep = count($hash_code);
       switch ($deep) {
	 case 1:
	   $tree[$hash_code[0]] = $cat_info;
	   break;
	 case 2:
	   $tree[$hash_code[0]][$hash_code[1]] = $cat_info;
	   break;
	 case 3:
	   $tree[$hash_code[0]][$hash_code[1]][$hash_code[2]] = $cat_info;
	   break;
	 }
    }
    foreach($tree as $level => $info) { 
      $block .= @display_popup_tree($info,0);
    }
  }
  else {
    $block.= "$l_cat_no";
  }
  $block.= "
  <br />
  <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
  <input name=\"popup\" type=\"hidden\" value=\"1\" />
  <input type=\"submit\" value=\"$l_add\" /> &nbsp;&nbsp;&nbsp; <input type=\"button\" value=\"$l_close\" onclick=\"window.close();\"/>   
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Document Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function display_popup_tree($tree,$indent) {
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory,$set_theme;
  global $popup;

  $code = $tree["code"];
  $label = $tree["label"];
  $id = $tree["id"]; 
  if($code != "") {
    $is_valid = true;
    $indent_file = $indent + 15;
  }
  else {
    $is_valid = false;
  }
    
  foreach($tree as $level => $info) {
    if("$level" != "code" && "$level" != "label" && "$level" != "id") {    
       $content.= display_popup_tree($info,$indent_file);
      $bool = true;
    }
  }
 
  if($is_valid) {
    if($bool == true) {
      $block= 
      "<table class=\"category\">
	 <tr>
	  <td class=\"categoryRepository\" style=\"padding-left:".$indent."px;text-align:left;\">
	   <a href=\"\" onclick=\"show_hide_repo('$code');return false;\"> 
	    <img id=\"img_$code\"  src=\"/images/$set_theme/$ico_directory_close\" alt=\"\"/>
	   </a>
	  <input type=\"checkbox\" name=\"$id\" /><span id=\"data$id\">$code - $label</span>
	  </td>
	 </tr>
	</table>
	<div id='div_$code' style=\"display:none;\" >
	 $content
	</div>";
    }
    else {
      $block= 
      "<table class=\"category\">
	 <tr>
	  <td class=\"categoryRepository\" style=\"padding-left:".$indent."px;text-align:left;\">
	    <img id=\"img_$code\"  src=\"/images/$set_theme/$ico_directory\" alt=\"\"/>
	     <input type=\"checkbox\" name=\"$id\" /><span id=\"data$id\">$code - $label</span>
	  </td>
	 </tr>
	</table>";
    }
  }
  else {
    $block = $content;
  }
    
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: category Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_category_code_tree($category) {
  global $l_close,$l_add,$l_cat_no;

  $popup = $category["popup"];
  $obm_d = run_query_get_tree();
  if ($popup) {
    $link = "<a class=\"categoryRepository\" href=\"\" onclick=\"fill_ext_form('/');return false;\">";
    $link_end ="</a>";
  }
  $name = '/';
  $kind = "0";
  $realpath = $path.$name;
  
  $block.= "
  <div class=\"categoryTree\">
   ";
   $cat_a = get_ordered_category($obm_d);
   if(is_array($cat_a)) {
     foreach($cat_a as $cat_info) {
       $hash_code = explode(".",$cat_info["code"]);
       $deep = count($hash_code);
       switch ($deep) {
	 case 1:
	   $tree[$hash_code[0]] = $cat_info;
	   break;
	 case 2:
	   $tree[$hash_code[0]][$hash_code[1]] = $cat_info;
	   break;
	 case 3:
	   $tree[$hash_code[0]][$hash_code[1]][$hash_code[2]] = $cat_info;
	   break;
	 }
    }
    foreach($tree as $level => $info) { 
      $block .= @display_popup_code_tree($info,0);
    }
  }
  else {
    $block.= "$l_cat_no";
  }
  $block.= "
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Category Tree
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function display_popup_code_tree($tree,$indent) {
  global $ico_file,$ico_directory_close,$ico_directory_open,$ico_directory;
  global $popup, $set_theme;

  $code = $tree["code"];
  $label = $tree["label"];
  $id = $tree["id"]; 
  if ($code != "") {
    $is_valid = true;
    $indent_file = $indent + 15;
  }
  else {
    $is_valid = false;
  }
    
  foreach($tree as $level => $info) {
    if("$level" != "code" && "$level" != "label" && "$level" != "id") {    
       $content.= display_popup_code_tree($info,$indent_file);
      $bool = true;
    }
  }
 
  if ($is_valid) {
    if ($bool == true) {
      $block= 
      "<table class=\"category\">
	 <tr>
	  <td class=\"categoryRepository\" style=\"padding-left:".$indent."px;text-align:left;\">
	   <a href=\"\" onclick=\"show_hide_repo('$code');return false;\" > 
	    <img id=\"img_$code\"  src=\"/images/$set_theme/$ico_directory_close\" alt=\"\"/>
	   </a>
	   <a href=\"javascript: void(0);\" onclick=\"fill_ext_code_form('$code');return false;\"><span id=\"data$id\">$code - $label</span></a>
	  </td>
	 </tr>
	</table>
	<div id='div_$code' style=\"display:none;\" >
	 $content
	</div>";
    } else {
      $block= 
      "<table class=\"category\">
	 <tr>
	  <td class=\"categoryRepository\" style=\"padding-left:".$indent."px;text-align:left;\">
	    <img id=\"img_$code\"  src=\"/images/$set_theme/$ico_directory\" alt=\"\"/>
	     <a href=\"javascript: void(0);\" onclick=\"fill_ext_code_form('$code');return false;\"><span id=\"data$id\">$code - $label</span></a>
	  </td>
	 </tr>
	</table>";
    }
  }
  else {
    $block = $content;
  }
    
  return $block;
}

</script>
