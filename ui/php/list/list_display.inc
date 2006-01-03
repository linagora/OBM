<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : list_display.php                                             //
//     - Desc : List Display File                                            //
// 2000-01-20 Aliacom                                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["list_name"] = $l_name;
$fieldnames["list_subject"] = $l_subject;
$fieldnames["list_email"] = $l_email;
$fieldnames["list_info_publication"] = $l_info_pub;
$fieldnames["list_mode"] = $l_mode;
$fieldnames["usercreate"] = $l_creator;
$fieldnames["userupdate"] = $l_updater;
$fieldnames["timecreate"] = $l_date_creation;
$fieldnames["timeupdate"] = $l_date_last_update;
// Calculated fields
$fieldnames["list_nb_contact"] = "$l_nb_contact : $l_static / $l_query";
$fieldnames["contact_lastname"] = $l_lastname;
$fieldnames["contact_firstname"] = $l_firstname;
$fieldnames["contactfunction_label"] = $l_function;
$fieldnames["contact_title"] = $l_title;
$fieldnames["kind_minilabel"] = $l_status;
$fieldnames["kind_header"] = $l_letter;
$fieldnames["kind_lang"] = $l_lang;
$fieldnames["company_name"] = $l_company;
$fieldnames["address"] = $l_address;
$fieldnames["service"] = $l_service;
$fieldnames["address1"] = "$l_address 1";
$fieldnames["address2"] = "$l_address 2";
$fieldnames["address3"] = "$l_address 3";
$fieldnames["zipcode"] = $l_postcode;
$fieldnames["town"] = $l_town;
$fieldnames["expresspostal"] = $l_expresspostal;
$fieldnames["country_name"] = $l_country;
$fieldnames["contact_phone"] = $l_phone;
$fieldnames["contact_fax"] = $l_fax;
$fieldnames["contact_homephone"] = $l_hphone;
$fieldnames["contact_mobilephone"] = $l_mphone;
$fieldnames["contact_email"] = $l_email;
$fieldnames["subscription_quantity"] = $l_quantity;
$fieldnames["publication_title"] = $l_publication." ".$l_title;
$fieldnames["publication_lang"] = $l_publication." ".$l_lang;

///////////////////////////////////////////////////////////////////////////////
// Display List specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_list(&$OD, $fieldname, $link_ok) {
  global $path, $list, $set_theme, $clist_mode_normal, $clist_mode_expert;
  global $l_normal, $l_expert;

  $ext_url = $list["ext_url"];
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

  if (($fieldname == "list_name")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("list_id").",'".addslashes($OD->data_set->f("list_name"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f("list_id").");";
    } elseif($link_ok) {
      $res["url"] = "list_index.php?action=detailconsult&amp;param_list=".$OD->data_set->f("list_id");
    }    
  }
  
  else if ($fieldname == "list_nb_contact") {
    $res["name"] = $OD->data_set->f("list_static_nb"). " / " .$OD->data_set->f("list_query_nb");
    $res["align"] = "center";
  }

  else if ($fieldname == "list_info_publication") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "list_mode") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname) == "$clist_mode_expert") {
      $res["name"] = "$l_expert";
    } else {
      $res["name"] = "$l_normal";
    }
  }

  // For contacts urls (link):
  else if ($fieldname == "contact_lastname") {
    $res["url"] = "$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$OD->data_set->f("contact_id");
  }

  else if ($fieldname == "company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
  }

  else if ($fieldname == "address") {
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
      if ($OD->data_set->f("contact_country") != "") {
	$res["name"] .= $OD->data_set->f("contact_country")."<br />\n";
	$res["txt_name"] .= $OD->data_set->f("contact_country");
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

  else if ($fieldname == "service") {
    $res["name"].= $OD->data_set->f("contact_service");
  }

  else if ($fieldname == "address1") {
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

  else if ($fieldname == "address2") {
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

  else if ($fieldname == "address3") {
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

  else if ($fieldname == "zipcode") {
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

  else if ($fieldname == "town") {
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

  else if ($fieldname == "expresspostal") {
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
	$res["name"] .= $OD->data_set->f("company_country");
      }
    }
  }

  else if ($fieldname == "contact_phone") {
    // If a contact phone is present we get it
    if ($OD->data_set->f("contact_phone") != "") {
      $res["name"] .= $OD->data_set->f("contact_phone");
      // else we get the contact Company phone
    } else {
      if ($OD->data_set->f("company_phone") != "") {
	$res["name"] .= "(".$OD->data_set->f("company_phone").")";
      }
    }
  }

  else if ($fieldname == "contact_fax") {
    // If a contact fax is present we get it
    if ($OD->data_set->f("contact_fax") != "") {
      $res["name"] .= $OD->data_set->f("contact_fax");
      // else we get the contact Company fax
    } else {
      if ($OD->data_set->f("company_fax") != "") {
	$res["name"] .= "(".$OD->data_set->f("company_fax").")";
      }
    }
  }

  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display List search form
// Parameters:
//   - $list[] : hash with list values
///////////////////////////////////////////////////////////////////////////////
function dis_list_search_form($list="") {

  $usr_q = run_query_userobm_active();
  $block .= html_list_search_form($list, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display List search Form
// Parameters : 
//   - $list[]    : default form values
//     keys used  : name, contact
//   - $usr_q     : database object with userobm list
///////////////////////////////////////////////////////////////////////////////
function html_list_search_form ($list, $usr_q) {
  global $c_all, $l_all, $l_name, $l_list_contact, $l_email, $l_market,$l_find;
  global $popup;

  if ($popup) {
    $title = $list["title"];
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($list["name"]);
  $email = stripslashes($list["email"]);
  $contact = stripslashes($list["contact"]);
  $mark = $list["marketing_manager"];

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

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\"
    action=\"".url_prepare("list_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_list_contact<br />
      <input type=\"text\" name=\"tf_contact\" size=\"16\" value=\"$contact\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"20\" value=\"$email\" />
    </div>
    <div class=\"searchLabel\">$l_market<br />
      $sel_market
    </div>
    <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the List search result
// Parameters:
//   - $list[]    : list search criteria
//     keys used  : name, subject, email
//   - $popup     : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function dis_list_search_list($list, $popup=false) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $prefs = get_display_pref($auth->auth["uid"], "list");
  $obm_q = run_query_list_search($list, $new_order, $order_dir);
  $nb_list = $obm_q->num_rows_total();
  if ($nb_list == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_list_search_list($obm_q, $prefs, $nb_list, $list, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the List search result
// Parameters : 
//   - $obm_q    : list of the lists to display 
//   - $prefs    : fields that have to be displayed
//   - $nb_list  : nb lists returned by the search query 
//   - $list[]   : list search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_list_search_list($obm_q, $prefs, $nb_list, $list, $popup) {
  global $display, $l_found, $l_add, $l_close;

  if ($popup) {
    $ext_action = $list["ext_action"];
    $ext_url = $list["ext_url"];
    $ext_id = $list["ext_id"];
    $ext_target = $list["ext_target"];
    $ext_widget = $list["ext_widget"];
    $ext_widget_text = $list["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }
  $name = urlencode(stripslashes($list["name"]));
  $contact = urlencode(stripslashes($list["contact"]));
  $mark = $list["marketing_manager"];

  $display["msg"] .= display_info_msg("$nb_list $l_found");

  $url = url_prepare("list_index.php?action=search&amp;tf_name=$name&amp;tf_contact=$contact&amp;sel_market=$mark$url_ext");
  
  $dis_list = new OBM_DISPLAY("DATA", $prefs, "list");
/*  if ($popup) {
    $dis_list->display_link = false;
    $dis_list->data_cb_text = "X";
    $dis_list->data_idfield = "list_id";
    $dis_list->data_cb_name = "cb_list";
    $block .= "<form target=\"$ext_target\" method=\"get\" action=\"$ext_url\">";
  }
  */
  if ($popup) {
    $dis_list->display_link = false;
    if ($ext_url != "") {
      $dis_list->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $dis_list->display_ext = "get_id";
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_list->data_set = $obm_q;
  $dis_list->data_header = "both";
  $dis_list->data_url = $url;
  $block .= $dis_list->display("dis_data_list");
  $block .= $display_popup_end;  
/*
  if ($popup) {
    $block .= "
    <p class=\"detailButton\">
    <p class=\"detailButtons\">
      <input type=\"submit\" value=\"$l_add\" /> 
      <input type=\"hidden\" name=\"param_list\" value=\"$ext_id\" /> 
      <input type=\"hidden\" name=\"action\" value=\"$ext_action\" /> 
    </p>
    </p>
    </form>
    <p align=\"right\">
    <a href=\"\" onclick='window.close();'>$l_close</a></p>";
  }*/

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display List Consultation
// Parameters:
//   - $list[] : list parameters
///////////////////////////////////////////////////////////////////////////////
function dis_list_consult($list) {
  global $display, $uid;

  $id = $list["id"];

  $list_q = run_query_list_detail($id);
  $prefs_con = get_display_pref($uid, "list_contact");
  $con_q = run_query_contacts_list($list, "list_contact");
  $block = html_list_consult($list_q, $prefs_con, $con_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display List Consultation
// Parameters:
//   - $list_q    : list database result 
//   - $prefs_con : contact display preferences database result 
//   - $con_q     : contact database result 
///////////////////////////////////////////////////////////////////////////////
function html_list_consult($list_q, $prefs_con, $con_q) {
  global $l_list, $l_name, $l_subject, $l_email, $l_static;
  global $l_visibility, $l_public, $l_private,$l_no,$l_yes;
  global $display, $perm, $module, $cright_write, $ctu_sql_limit, $cdg_sql;
  global $l_contact, $l_no_static_contact, $l_static_con_found;
  global $l_no_dyn_contact, $l_dyn_con_found, $l_company, $l_invalid_query;
  global $l_del_contact_sel, $nb_contact_added, $nb_contact_deleted;
  global $l_mailing, $l_contact_archive, $l_info_pub;
  global $l_query, $l_expert, $l_normal;
  global $clist_mode_normal, $clist_mode_expert;

  $id = $list_q->f("list_id");
  $usercreate = $list_q->f("list_usercreate");
  $userupdate = $list_q->f("list_userupdate");
  $timecreate = $list_q->f("timecreate");
  $timeupdate = $list_q->f("timeupdate");
  $lpriv = ($list_q->f("list_privacy") == 0 ? $l_public : $l_private);
  $dis_mailing = ($list_q->f("list_mailing_ok") == 0 ? $l_no : $l_yes);
  $dis_con_arch = ($list_q->f("list_contact_archive") == 0 ? $l_no : $l_yes);
  $dis_info_pub = ($list_q->f("list_info_publication") == 0 ? $l_no : $l_yes);
  $name = $list_q->f("list_name");
  $subject = $list_q->f("list_subject");
  $email = $list_q->f("list_email");
  $mode = $list_q->f("list_mode");
  $static_nb = $list_q->f("list_static_nb");
  $structure = $list_q->f("list_structure");

  if ($structure != "") {
    $criteria = unserialize($structure);
  } else {
    $criteria = "";
  }

  if ($mode == $clist_mode_normal) {
    $lmode = $l_normal;
    if ($criteria != "") {
      $dynlist = make_query_from_db($list_q);
      $query = $dynlist["query"];
    }
  } else {
    $lmode = $l_expert;
    $query = stripslashes($list_q->f("list_query"));
    $dynlist["query"] = $query;
  }

  if ($dynlist["query"] != "") {
    $query_nb = get_list_query_num_rows($dynlist, $list_q);
    if ($query_nb >= 0) {
      run_query_update_num_rows($id, $query_nb);
    } else {
      $query_nb = $l_invalid_query;
    }

    // Company query nb
    $comp_query_nb = get_list_dynamic_company_nb($dynlist);

    // Contact query nb (= query_nb if no supplement info needed as publi)
    if ($dis_info_pub == $l_yes) {
      $con_query_nb = get_list_dynamic_contact_nb($dynlist);
    } else {
      $con_query_nb = $query_nb;
    }
  } else {
    $query_nb = "0";
    $comp_query_nb = "0";
  }

  $comp_static_nb = get_list_static_company_nb($id);

  // If expert mode, we display the query (empty or manual)
  if ($mode == $clist_mode_expert) {
    $d_query = nl2br($query);
    $dis_query = "<tr>
      <td class=\"detailLabel\">$l_query</td>
      <td class=\"detailText\">$d_query</td>
    </tr>";
  }
  $display["detailInfo"] = display_record_info($list_q);
  $display["title"] = "<div class=\"title\">$l_list : $name</div>";

  $block = "
    <div class=\"detailHead\">$l_list</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_name</td>
      <td class=\"detailText\">$name</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_visibility</td>
      <td class=\"detailText\">$lpriv</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_mailing</td>
      <td class=\"detailText\">$dis_mailing</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_contact_archive</td>
      <td class=\"detailText\">$dis_con_arch</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_info_pub</td>
      <td class=\"detailText\">$dis_info_pub</td>
    </tr> <tr>
      <td class=\"detailLabel\">$l_subject</td>
      <td class=\"detailText\">$subject</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_email</td>
      <td class=\"detailText\">$email</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_query</td>
      <td class=\"detailText\">$lmode</td>
    </tr>
    <tr>
      <td class=\"detailLabel\"># $l_contact : $l_static / $l_query</td>
      <td class=\"detailText\">$static_nb / $con_query_nb</td>
    </tr>
      <td class=\"detailLabel\"># $l_company : $l_static / $l_query</td>
      <td class=\"detailText\">$comp_static_nb / $comp_query_nb</td>
    </tr>
    $dis_query
    </table>";

  // Static Contacts
  $nb_con = $con_q->num_rows_total();
  if ($nb_con == 0) {
    $message = $l_no_static_contact;
  } else {
    $message = "$nb_con $l_static_con_found";
  }

  $block .= display_info_msg($message);

  if ($nb_con != 0) {

    $url=url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_con, "list");
    $dis_infos->data_set = $con_q;
    $dis_infos->data_url = $url;
    $dis_infos->display_entity = "list_contact";
    $dis_infos->data_header = "both";

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = "X";
      $dis_infos->data_idfield = "contact_id";
      $dis_infos->data_cb_name = "cb_con";
      $dis_infos->data_cb_field = "";
      $dis_infos->data_form_head = "
      <form method=\"get\" action=\"list_index.php\">";

      $dis_infos->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_contact_sel\" />
        <input type=\"hidden\" name=\"param_list\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"contact_del\" />
        </p>
      </p>
      </form>";
    }

    $block .= $dis_infos->display("dis_data_list");

  }

  // Dynamic Contacts
  if ($query_nb != 0) {
    $message = "$query_nb $l_dyn_con_found";
    $block .= display_info_msg($message);

    $url=url_prepare("list_index.php?action=detailconsult&amp;param_list=$id");
    
    $conq_q = new DB_OBM;
    $db_type = $obm_q->type;
    $limit = sql_limit($db_type);

    if ($ctu_sql_limit) {
      $conq_q->set_num_rows_total($query_nb);
    }

    $query_limit = "$query $limit";
    display_debug_msg($query_limit, $cdg_sql, "dynamic list()");
    $conq_q->query("$query_limit");
    $dis_infos = new OBM_DISPLAY("DATA", $prefs_con, "list");
    $dis_infos->data_set = $conq_q;
    $dis_infos->data_url = $url;
    $dis_infos->display_entity = "list_contact_2";
    $dis_infos->data_order = false;
    $dis_infos->data_header = "both";
    $block .= $dis_infos->display("dis_data_list");
  } else {
    $message = $l_no_dyn_contact;
    $block .= display_info_msg($message);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display List Form (graphical or not)
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function dis_list_form($action, $list_q, $list) {
  global $l_insert, $l_update, $l_back;

  $url = url_prepare("list_index.php");
  $block = "<form method=\"post\" name=\"f_entity\" action=\"$url\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">";

  $block .= html_list_form($action, $list_q, $list);

  if (($action == "detailupdate") || ($action == "update")) {
     $dis_but .= "
      <input type=\"hidden\" name=\"action\" value=\"update\"/>
      <input type=\"submit\" value=\"$l_update\" />";

   } else {
    $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<div class=\"detailButton\">
    <p class=\"detailButtons\">
    $dis_but 
    </p>
    </div>
    </form>";
  return $block; 
}


///////////////////////////////////////////////////////////////////////////////
// Display List Form
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_form($action, $list_q, $list) {
  global $l_name,$l_subject,$l_email,$l_mailing,$l_contact_archive,$l_info_pub;
  global $l_query, $l_list, $l_private;
  global $clist_mode_normal, $clist_mode_expert, $l_expert, $l_normal;
  global $action, $display, $uid;

  // if update mode and first time, values are taken from db
  if (($action == "detailupdate") || ($action == "detailduplicate")) {
    $id = $list_q->f("list_id");
    $usercreate = $list_q->f("list_usercreate");
    $name = $list_q->f("list_name");
    $subject = $list_q->f("list_subject");
    $email = $list_q->f("list_email");
    $mode = $list_q->f("list_mode");
    $structure = $list_q->f("list_structure");
    $criteria = unserialize($structure);
    $query = stripslashes($list_q->f("list_query"));
    $priv = ($list_q->f("list_privacy") == 0 ? "0" : "1");
    $mailing_ok = ($list_q->f("list_mailing_ok") == 0 ? "0" : "1");
    $contact_arch = ($list_q->f("list_contact_archive") == 0 ? "0" : "1");
    $info_pub = ($list_q->f("list_info_publication") == 0 ? "0" : "1");
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($list["id"])) { $id = $list["id"]; }
  if (isset($list["priv"])) { $priv = $list["priv"]; }
  if (isset($list["name"])) { $name = stripslashes($list["name"]); }
  if (isset($list["subject"])) { $subject = stripslashes($list["subject"]); }
  if (isset($list["email"])) { $email = stripslashes($list["email"]); }
  if (isset($list["mode"])) { $mode = $list["mode"]; }
  if (isset($list["query"])) { $query = stripslashes($list["query"]); }
  if (isset($list["mailing_ok"])) { $mailing_ok = $list["mailing_ok"]; }
  if (isset($list["contact_arch"])) { $contact_arch = $list["contact_arch"]; }
  if (isset($list["info_pub"])) { $info_pub = $list["info_pub"]; }
  if (isset($list["criteria"])) { $criteria = $list["criteria"]; }
	
  if ( ($action == "new") || ($action == "insert")
       || ($action == "detailduplicate")
       || ( (($action == "detailupdate") || ($action == "update")) && 
	    ($usercreate == $uid) ) ) {
    $dis_priv = "
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailForm\"><input name=\"cb_priv\" type=\"checkbox\" value=\"1\" ";
    if ($priv == 1) $dis_priv .= "checked = \"checked\"";
    $dis_priv .= " /></td>
  </tr><tr>";
  }

  if ($mailing_ok == 1) $dis_mailing .= "checked = \"checked\""; 
  if ($contact_arch == 1) $dis_con_arch .= "checked = \"checked\""; 
  if ($info_pub == 1) $dis_info_pub .= "checked = \"checked\""; 

  $url = url_prepare("list_index.php");
  $display["title"] = "<div class=\"title\">$l_list : $name</div>";

  // If criteria are set, we display the criteria form, else the query
  if ($mode == $clist_mode_expert) {
    $expert_checked = " checked=\"checked\" ";
    $style_query = "";
  } else {
    $normal_checked = " checked=\"checked\" ";
    $style_query = "style=\"display:none;\"";
  }
  $dis_query = html_list_graphical_form($action, $list_q, $list);

  $block = "
  <div class=\"detailHead\">$l_list</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_name</td>
    <td class=\"detailForm\">
      <input type=\"text\" name=\"tf_name\" size=\"32\" maxlength=\"32\" value=\"$name\" /></td>
  </tr>
  <tr>
  $dis_priv
    <td class=\"detailLabel\">$l_subject</td>
    <td class=\"detailForm\">
      <input name=\"tf_subject\" size=\"30\" value=\"$subject\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_email</td>
    <td class=\"detailForm\">
      <input name=\"tf_email\" size=\"30\" value=\"$email\" /></td>
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_mailing</td>
    <td class=\"detailForm\">
      <input type=\"checkbox\" $dis_mailing name=\"cb_mailing_ok\" value=\"1\" /></td>
  </tr>  
  <tr>
    <td class=\"detailLabel\">$l_contact_archive</td>
    <td class=\"detailForm\">
      <input type=\"checkbox\" $dis_con_arch name=\"cb_contact_arch\" value=\"1\" /></td>
  </tr>  
  <tr>
    <td class=\"detailLabel\">$l_info_pub</td>
    <td class=\"detailForm\">
      <input type=\"checkbox\" $dis_info_pub name=\"cb_info_pub\" value=\"1\" /></td>
  </tr>  
  </table>
  <div class=\"detailHead\">$l_query</div>  
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
       <input type=\"radio\" $expert_checked name=\"rd_mode\" value=\"$clist_mode_expert\" onclick=\"return show_query_field('','none',this);\" />$l_expert
       <input type=\"radio\" $normal_checked name=\"rd_mode\" value=\"$clist_mode_normal\" onclick=\"return show_query_field('none','',this);\" />$l_normal
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">
      <textarea id=\"ta_query\" $style_query name=\"ta_query\" cols=\"68\" rows=\"8\">$query</textarea>
      $dis_query
      </td>
    </tr>
  </table>";

  if (($action == "detailupdate") || ($action == "update")) {
    $block .= "<input type=\"hidden\" name=\"param_list\" value=\"$id\" />";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Graphical List Form
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_graphical_form($action, $list_q, $list) {
  global $popup_height,$popup_width,$set_theme,$ico_updated,$ico_crow;
  global $l_add_criterion, $l_criterion;
  global $l_and, $l_or, $l_not, $l_empty, $l_exclude;
  global $l_company, $l_country,$l_postcode,$l_town, $l_company_cat1;
  global $l_contact,$l_lastname,$l_firstname;
  global $l_timeafter,$l_timebefore,$l_market, $l_datasource,$l_contact_cat1;
  global $l_contact_cat2,$l_function;
  global $l_publication,$l_title,$l_lang,$l_year, $l_after, $l_before;
  global $l_update,$l_delete,$l_recept,$l_renew;
  global $clist_mode_expert, $clist_mode_normal;
  
  // if update mode and first time, values are taken from db
  if (($action == "detailupdate") || ($action == "detailduplicate")) {
    $id = $list_q->f("list_id");
    $mode = $list_q->f("list_mode");
    $query = stripslashes($list_q->f("list_query"));
    $criteria = unserialize($list_q->f("list_structure"));
    $boolean_criterion = $criteria["logical"]["NOT"];
    $logical_criterion = $criteria["logical"]["AND"];      
    $criteria = $criteria["modules"];
  }

  // If parameters have been given, they supercede the default action value
  if (isset($list["id"])) { $id = $list["id"]; }
  if (isset($list["mode"])) { $mode = $list["mode"]; }
  if (isset($list["query"])) { $query = stripslashes($list["query"]); }
  if (isset($list["criteria"])) { 
    $criteria = $list["criteria"];
    if (!is_array($criteria)) {
      $criteria = unserialize($criteria);
    }
    $boolean_criterion = $criteria["logical"]["NOT"];
    $logical_criterion = $criteria["logical"]["AND"];      
    $criteria = $criteria["modules"]; 
  }

  $field_kind = array(
      "company_name" => "tf",
      "company_country_iso3166" => "sel",
      "company_timeafter" => "tf",
      "company_zipcode" => "tf",
      "company_marketingmanager_id" => "sel",
      "company_timebefore" => "tf",
      "company_town" => "tf",
      "company_datasource_id" => "sel",
      "companycategory1_code" => "sel",
      "contact_firstname" => "tf",
      "contact_country_iso3166" => "sel",
      "contact_timeafter" => "tf",
      "contact_lastname" => "tf",
      "contact_marketingmanager_id" => "sel",
      "contact_timebefore" => "tf",
      "contact_datasource_id" => "sel",
      "contact_town" => "tf",
      "contact_zipcode" => "tf",
      "contactcategory1link_category_id" => "sel",
      "contactcategory2link_category_id" => "sel",
      "contact_function_id" => "sel",
      "kind_lang" => "sel",
      "subscription_publication_id" => "sel",
      "publication_lang" => "tf",
      "publication_year" => "tf",
      "subscription_reception_id" => "sel",
      "subscription_renewal" => "tf",
      "subscription_timeafter" => "tf",
      "subscription_timebefore" => "tf"
  );
  $fields_label["company_name"] = $l_company; 
  $fields_label["company_country_iso3166"] = "$l_company: $l_country"; 
  $fields_label["company_zipcode"] = "$l_company: $l_postcode"; 
  $fields_label["company_town"] = "$l_company: $l_town"; 
  $fields_label["company_timeafter"] = "$l_company: $l_timeafter"; 
  $fields_label["company_timebefore"] = "$l_company: $l_timebefore"; 
  $fields_label["company_marketingmanager_id"] = "$l_company: $l_market";
  $fields_label["companycategory1_code"] = "$l_company: $l_company_cat1";   
  $fields_label["company_datasource_id"] = "$l_company: $l_datasource";
  $fields_label["contact_lastname"] = "$l_contact: $l_lastname";    
  $fields_label["contact_firstname"] = "$l_contact: $l_firstname";      
  $fields_label["contact_country_iso3166"]= "$l_contact: $l_country";  
  $fields_label["contact_zipcode"] = "$l_contact: $l_postcode";  
  $fields_label["contact_town"] = "$l_contact: $l_town"; 
  $fields_label["contact_timeafter"] = "$l_contact: $l_timeafter"; 
  $fields_label["contact_timebefore"] = "$l_contact: $l_timebefore"; 
  $fields_label["contact_marketingmanager_id"] = "$l_contact: $l_market"; 
  $fields_label["contact_datasource_id"] = "$l_contact: $l_datasourceid"; 
  $fields_label["contactcategory1link_category_id"] = "$l_contact: $l_contact_cat1"; 
  $fields_label["contactcategory2link_category_id"] = "$l_contact: $l_contact_cat2"; 
  $fields_label["kind_lang"] = "$l_contact: $l_lang"; 
  $fields_label["contact_function_id"] = "$l_contact: $l_function"; 
  $fields_label["subscription_publication_id"] = "$l_publication: $l_title"; 
  $fields_label["publication_lang"] = "$l_publication: $l_lang"; 
  $fields_label["publication_year"] = "$l_publication: $l_year"; 
  $fields_label["subscription_reception_id"] = "$l_publication: $l_recept"; 
  $fields_label["subscription_renewal"] = "$l_publication: $l_renew";
  $fields_label["subscription_timeafter"] = "$l_publication: $l_after";
  $fields_label["subscription_timebefore"] = "$l_publication: $l_before";

  if (is_array($criteria)) {
    $cty_q = run_query_country_for_lang();
    $dsr_q = run_query_datasource();
    $cat1_q = run_query_contactcategory1();
    $usr_q = run_query_userobm_active();  
    $cat2_q = run_query_contactcategory2();
    $fct_q = run_query_contactfunction();
    $cat_q = run_query_companycategory1();       
    $rec_q = run_query_list_subscriptionreception();  
    $lag_q = run_query_language();
    $pub_q = run_query_list_publication();

    $field_data = array(
      "company_country_iso3166" => array($cty_q,"country_iso3166","country_name"),
      "company_marketingmanager_id" => array($usr_q,"userobm_id","userobm_lastname"),
      "company_datasource_id" => array($dsr_q,"datasource_id","datasource_name"),
      "companycategory1_code" => array($cat_q,"companycategory1_code","companycategory1_label"),
      "contact_country_iso3166" => array($cty_q,"country_iso3166","country_name"),
      "contact_marketingmanager_id" => array($usr_q,"userobm_id","userobm_lastname"),
      "contact_datasource_id" => array($dsr_q,"datasource_id","datasource_name"),
      "contactcategory1link_category_id" => array($cat1_q,"contactcategory1_id","contactcategory1_label"),
      "contactcategory2link_category_id" => array($cat2_q,"contactcategory2_id","contactcategory2_label"),
      "contact_function_id" => array($fct_q,"contactfunction_id","contactfunction_label"),
      "kind_lang" => array($lag_q,"kind_lang","kind_lang"),
      "subscription_publication_id" => array($pub_q,"publication_id","publication_title"),
      "subscription_reception_id" => array($rec_q,"subscriptionreception_id","subscriptionreception_label")
    );

    foreach($criteria as $module)
    foreach($module as $field => $criterion) {
      foreach($criterion as $line => $value) {
        if ($value != "") {
	  if ($field_kind[$field] == "tf") {
	    $input = "<input type=\"text\" name=\"tf_".$field."[]\" readonly=\"readonly\" style=\"background-color:#B9C7CA;\" value=\"$value\" />";
	  } else {
	    $input = "<select name=\"sel_".$field."[]\" readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\">";
	    $data = $field_data[$field];
	    $data_q = $data[0];
	    while ($data_q->next_record()) {
	      if ("$value " == $data_q->f($data[1])." ") {
	        $input .= "<option selected=\"selected\" value=\"$value\" $selected>".$data_q->f($data[2])."</option>";
	      }
	    }
	    $input .= "</select>";
	    $data_q->seek(0);
	  }
	  $structure[$line] .= "<td>$input</td>";
	  $labels[$line] .= "<td>".$fields_label[$field]."</td>";
        }
        else {
	  $structure[$line] .= "";
	  $hid_cell[$line] .= "<input type=\"hidden\" name=\"".$field_kind[$field]."_".$field."[]\" value=\"\" />";
        }
      }
    }
    $del_cell= "<td><a href=\"javascript:void(0);\" onclick=\"del_row(this.parentNode.parentNode);return false;\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_crow\" alt=\"Delete\" /></a></td>";
 
    $upd_cell = "<td><a href=\"javascript:void(0);\" onclick=\"open_window(this.parentNode.parentNode);return false;\">
    <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_updated\" alt=\"Update\" /></a></td>"; 
    if (is_array($structure))
    foreach($structure as $key=>$line) {
      if ($boolean_criterion[$key] == "NOT") {
	$not = "selected =\"true\"";
	$exclude = "";
	$empty = "";
      } else if ($boolean_criterion[$key] == "Exclude") {
	$exclude = "selected =\"true\"";
	$not = "";
	$empty = "";
      } else {
	$empty = "selected =\"true\"";
	$exclude = "";
	$not = "";
      }
      $logical_criterion;      
      $dis_no_boolean = "<select name=\"sel_log_not[]\">
        <option value=\"\" $empty >$l_empty</option>
        <option value=\"NOT\" $not>$l_not</option>
        <option value=\"Exclude\" $exclude>$l_exclude</option>
      </select></td>";
      $dis_criteria .="<tr><td colspan=\"4\"> </td>".$labels[$key]."</tr>";
      if ($logical_criterion[$key] == "AND") {
	$and = "selected =\"true\"";
	$or = "";
      } else {
	$or = "selected =\"true\"";
	$and = "";
      }	
      $dis_logical = "<td><select name=\"sel_log_and[]\"><option value=\"OR\" $or>$l_or</option><option value=\"AND\" $and>$l_and</option></select>";
      $dis_criteria .="<tr><td>".$hid_cell[$key]."</td>$del_cell $upd_cell $dis_logical $dis_no_boolean $line</tr>";      
    }
  }
  $url2 = "list_index.php?action=new_criterion&amp;popup=1&amp;ext_target=forms[0].elements[5]";
  if ($mode == $clist_mode_expert) {
    $display = "style=\"display:none\"";
  }
  $block = "
  <div id=\"di_query\" $display>
  <table class=\"search\" id=\"list_criterion\">
   $dis_criteria
  </table>
  <input type=\"button\" value=\"$l_add_criterion\" 
  onclick=\"window.open('$url2','','height=650,width=$popup_width,scrollbars=yes,status=yes'); return false;\"/>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Form to add a Criterion
// Parameters:
//   - $list[]    : default or transmitted values
///////////////////////////////////////////////////////////////////////////////
function dis_add_criterion_form($list) {
  global $cgp_show, $l_list_display, $l_contact_display,$l_add_criterion;

  $popup = $list["popup"];
  if (isset($list["row_index"])) {
    $row_index = $list["row_index"];
  } else {
    $row_index =-1;
  }
  $cty_q = run_query_country_for_lang();
  $usr_q = run_query_userobm_active();
  $dsr_q = run_query_datasource();
  $cat1_q = run_query_contactcategory1();
  $cat2_q = run_query_contactcategory2();
  $fct_q = run_query_contactfunction();
  $cat_q = run_query_companycategory1();
  $lag_q = run_query_language();
    
  $block .= "<form method=\"get\" name=\"f_new_criterion\" action=\"$url\" onSubmit=\"add_criterion(this,$row_index);window.close();return false;\">";

  $block .= html_company_criterion($cty_q,$usr_q,$dsr_q,$cat_q);
  $block .= html_contact_criterion($cty_q,$usr_q,$dsr_q,$cat1_q,$cat2_q,$fct_q,$lag_q);

  if ($cgp_show["module"]["publication"]) {
    $rec_q = run_query_list_subscriptionreception();
    $pub_q = run_query_list_publication();
    $block .= html_publication_criterion($rec_q,$pub_q);
  }

  $block .= "
  <div class=\"detailButton\">
  <p class=\"detailButtons\">
  <input type=\"submit\" value=\"$l_add_criterion\"/>
  </p>
  </div>
";
  $block.="<input type=\"hidden\" name=\"ext_target\" value=\"$ext_target\" />";
  $block .= "</form>";
  if ($row_index != -1) {
    $block .= "
    <script type=\"text/javascript\">
      get_values(window.document.f_new_criterion);
    </script>
    ";
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_company_criterion($cty_q,$usr_q,$dsr_q,$cat_q){
  global $l_company,$l_country,$l_postcode,$l_town,$l_timebefore,$l_timeafter;
  global $l_all,$l_market,$l_datasource,$l_company_cat1;
  global $set_theme,$ico_mini_cal,$ico_add,$popup_height,$popup_width;

  $sel_cty .= "\n<option value=\"\">$l_all</option>";
  while ($cty_q->next_record()) {
    $sel_cty .= "\n<option value=\"".$cty_q->f("country_iso3166")."\">".$cty_q->f("country_name")."</option>";
  }
  if ($cty_q->nf()!=0) { 
    $cty_q->seek(0);
  }
  $sel_usr .= "\n<option value=\"\">$l_all</option>";
  while ($usr_q->next_record()) {
    $sel_usr .= "\n<option value=\"".$usr_q->f("userobm_id")."\">".$usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname")."</option>";
  }
  if($usr_q->nf()!=0) 
    $usr_q->seek(0);
  $sel_dsr .= "\n<option value=\"\">$l_all</option>";
  while($dsr_q->next_record()) {
    $sel_dsr .= "\n<option value=\"".$dsr_q->f("datasource_id")."\">".$dsr_q->f("datasource_name")."</option>";
  }  
  if($dsr_q->nf()!=0) 
    $dsr_q->seek(0);
  $sel_cat .= "\n<option value=\"\">$l_all</option>";
  while($cat_q->next_record()) {
    $cat_code = $cat_q->f("companycategory1_code");
    $cat_label = substr($cat_q->f("companycategory1_label"),0,30);
    $sel_cat .= "\n<option value=\"$cat_code\">$cat_code $cat_label</option>";
  }
  if($cat_q->nf()!=0) 
    $cat_q->seek(0);
  
  $block .= "
  <div class=\"detailHead\">$l_company</div>    
  <table class=\"search\">
  <tr>
    <td class=\"detailLabel\">$l_company</td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailLabel\">$l_timeafter</td>  
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_name\" value=\"\" /></td>
    <td class=\"detailForm\"><select name=\"sel_company_country_iso3166\">$sel_cty</select></td>
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
     <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_company_timeafter\" value=\"\" />
     <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_company_timeafter,'../agenda/agenda_index.php?action=calendar&popup=1');\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
    </td>    
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_postcode</td>
    <td class=\"detailLabel\">$l_market</td>
    <td class=\"detailLabel\">$l_timebefore</td>
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"10\" name=\"tf_company_zipcode\" value=\"\" /></td>
    <td class=\"detailForm\"><select name=\"sel_company_marketingmanager_id\">$sel_usr</select></td>    
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
     <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_company_timebefore\" value=\"\" />
     <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_company_timebefore,'../agenda/agenda_index.php?action=calendar&popup=1');\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
     </a>
    </td> 
  </tr>
  <tr>   
    <td class=\"detailLabel\">$l_town</td>
    <td class=\"detailLabel\">$l_company_cat1</td>    
    <td class=\"detailLabel\">$l_datasource</td>
  </tr>
  <tr>    
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_company_town\" value=\"\" /></td> 
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
    <select  readonly=\"true\" style=\"background-color:#B9C7CA;\" onFocus=\"this.blur();\" name=\"sel_companycategory1_code\">$sel_cat</select>
     <a href=\"javascript: void(0);\" onclick=\"window.open('../company/company_index.php?action=ext_get_category1_code&amp;popup=1&amp;ext_target=f_new_criterion.sel_companycategory1_code','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_add\" border=\"0\" />

    </td>     
    <td class=\"detailForm\"><select name=\"sel_company_datasource_id\">$sel_dsr</select></td>        
  </tr>  
  </table>
  ";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_contact_criterion($cty_q,$usr_q,$dsr_q,$cat1_q,$cat2_q,$fct_q,$lag_q) {
  global $l_lastname,$l_firstname,$l_country,$l_postcode,$l_town,$l_contact;
  global $l_all,$l_timebefore,$l_timeafter,$l_market,$l_datasource;  
  global $l_contact_cat1,$l_contact_cat2,$l_function,$l_lang;
  global $l_yes,$l_no;
  global $set_theme,$ico_mini_cal;  

  $sel_cty .= "\n<option value=\"\">$l_all</option>";
  while ($cty_q->next_record()) {
    $sel_cty .= "\n<option value=\"".$cty_q->f("country_iso3166")."\">".$cty_q->f("country_name")."</option>";
  }
  if ($cty_q->nf() != 0) {
    $cty_q->seek(0);
  }

  $sel_usr .= "\n<option value=\"\">$l_all</option>";
  while ($usr_q->next_record()) {
    $sel_usr .= "\n<option value=\"".$usr_q->f("userobm_id")."\">".$usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname")."</option>";
  }
  if ($usr_q->nf() != 0) {
    $usr_q->seek(0);
  }

  $sel_dsr .= "\n<option value=\"\">$l_all</option>";
  while ($dsr_q->next_record()) {
    $sel_dsr .= "\n<option value=\"".$dsr_q->f("datasource_id")."\">".$dsr_q->f("datasource_name")."</option>";
  }
  if ($dsr_q->nf() != 0) {
    $dsr_q->seek(0);
  }

  $sel_cat1 .= "\n<option value=\"\">$l_all</option>";
  while ($cat1_q->next_record()) {
    $sel_cat1 .= "\n<option value=\"".$cat1_q->f("contactcategory1_id")."\">".$cat1_q->f("contactcategory1_label")."</option>";
  }
  if ($cat1_q->nf() != 0) {
    $cat1_q->seek(0);
  }

  $sel_cat2 .= "\n<option value=\"\">$l_all</option>";
  while ($cat2_q->next_record()) {
    $sel_cat2 .= "\n<option value=\"".$cat2_q->f("contactcategory2_id")."\">".$cat2_q->f("contactcategory2_label")."</option>";
  }
  if ($cat2_q->nf() != 0) {
    $cat2_q->seek(0);
  }

  if ($fct_q != "") {
    $sel_fct .= "\n<option value=\"\">$l_all</option>";
    while ($fct_q->next_record()) {
      $sel_fct .= "\n<option value=\"".$fct_q->f("contactfunction_id")."\">".$fct_q->f("contactfunction_label")."</option>";
    }
    if ($fct_q->nf() != 0) {
      $fct_q->seek(0);
    }
  }

  $sel_lag .= "\n<option value=\"\">$l_all</option>";
  while($lag_q->next_record()) {
    $sel_lag .= "\n<option value=\"".$lag_q->f("kind_lang")."\">".$lag_q->f("kind_lang")."</option>";
  }   
  if($lag_q->nf()!=0) 
    $lag_q->seek(0);
 
  $block = "
  <div class=\"detailHead\">$l_contact</div>    
  <table class=\"search\">
  <tr>
    <td class=\"detailLabel\">$l_lastname</td>
    <td class=\"detailLabel\">$l_country</td>
    <td class=\"detailLabel\">$l_timeafter</td>
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_lastname\" value=\"\" /></td>
    <td class=\"detailForm\"><select name=\"sel_contact_country_iso3166\">$sel_cty</select></td>    
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
     <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_contact_timeafter\" value=\"\" />
     <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_contact_timeafter,'../agenda/agenda_index.php?action=calendar&popup=1');\">
     <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
    </td>    
  <tr>  
    <td class=\"detailLabel\">$l_firstname</td>
    <td class=\"detailLabel\">$l_market</td>
    <td class=\"detailLabel\">$l_timebefore</td>    
  </tr>
  <tr>
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_firstname\" value=\"\" /></td>
    <td class=\"detailForm\"><select name=\"sel_contact_marketingmanager_id\">$sel_usr</select></td>    
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
     <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_contact_timebefore\" value=\"\" />
     <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_contact_timebefore,'../agenda/agenda_index.php?action=calendar&popup=1');\">
       <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mini_cal\" border=\"0\" />
     </a>
    </td>     
  </tr>
  <tr>  
    <td class=\"detailLabel\">$l_datasource</td>
    <td class=\"detailLabel\">$l_town</td>   
    <td class=\"detailLabel\">$l_postcode</td>
  </tr>
  <tr>    
    <td class=\"detailForm\"><select name=\"sel_contact_datasource_id\">$sel_dsr</select></td>        
    <td class=\"detailForm\"><input type=\"text\" size=\"17\" name=\"tf_contact_town\" value=\"\" /></td>    
    <td class=\"detailForm\"><input type=\"text\" size=\"10\" name=\"tf_contact_zipcode\" value=\"\" /></td>
  </tr>  
  <tr>  
    <td class=\"detailLabel\">$l_contact_cat1</td>
    <td class=\"detailLabel\">$l_contact_cat2</td>   
    <td class=\"detailLabel\">$l_function</td>
  </tr> 
  <tr>  
    <td class=\"detailForm\"><select name=\"sel_contactcategory1link_category_id\">$sel_cat1</select></td>    
    <td class=\"detailForm\"><select name=\"sel_contactcategory2link_category_id\">$sel_cat2</select></td>    
    <td class=\"detailForm\"><select name=\"sel_contact_function_id\">$sel_fct</select></td>    
  </tr>
  <tr> 
    <td class=\"detailLabel\">$l_lang</td>
    <td></td>
    <td></td>
  </tr> 
  <tr>  
    <td class=\"detailForm\"><select name=\"sel_kind_lang\">$sel_lag</select></td>    
    <td></td>
    <td></td>
  </tr>

  </table>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_publication_criterion($rec_q,$pub_q) {
  global $path, $l_all, $l_undef, $l_yes, $l_no, $set_theme, $ico_mini_cal;
  global $l_publication, $l_title, $l_lang, $l_year;
  global $l_renew,$l_recept, $l_after, $l_before;

  if ($rec_q->nf() !=0 ) {
    $rec_q->seek(0);
  }
  $sel_rec .= "\n<option value=\"\">$l_all</option>";
  while ($rec_q->next_record()) {
    $sel_rec .= "\n<option value=\"".$rec_q->f("subscriptionreception_id")."\">".$rec_q->f("subscriptionreception_label")."</option>";
  }

  if ($pub_q->nf() !=0) {
    $pub_q->seek(0);
  }
  $sel_pub .= "\n<option value=\"\">$l_all</option>";
  while ($pub_q->next_record()) {
    $sel_pub .= "\n<option value=\"".$pub_q->f("publication_id")."\">".$pub_q->f("publication_title")."</option>";
  }   
  
  $block = "
  <div class=\"detailHead\">$l_publication</div>    
  <table class=\"search\">
  <tr>
    <td class=\"detailLabel\">$l_title</td>
    <td class=\"detailLabel\">$l_lang</td>
    <td class=\"detailLabel\">$l_year</td>
    <td class=\"detailLabel\">$l_after</td>  
  </tr>
  <tr>
    <td class=\"detailForm\"><select name=\"sel_subscription_publication_id\">$sel_pub</select></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"10\" name=\"tf_publication_lang\" value=\"\" /></td>
    <td class=\"detailForm\"><input type=\"text\" size=\"10\" name=\"tf_publication_year\" value=\"\" /></td>  
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
      <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_subscription_timeafter\" value=\"\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_subscription_timeafter,'$path/agenda/agenda_index.php?action=calendar&popup=1');\">
      <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
    </td>     
  </tr>
  <tr>
    <td class=\"detailLabel\">$l_renew</td>
    <td class=\"detailLabel\">$l_recept</td>
    <td class=\"detailLabel\">&nbsp;</td>  
    <td class=\"detailLabel\">$l_before</td>  
  </tr>
  <tr>
    <td class=\"detailForm\">
      <select name=\"tf_subscription_renewal\">
       <option value=\"\">$l_undef</option>
       <option value=\"1\">$l_yes</option>
       <option value=\"0\">$l_no</option>
      </select>
    </td>    
    <td class=\"detailForm\"><select name=\"sel_subscription_reception_id\">$sel_rec</select></td>
    <td class=\"detailForm\">&nbsp;</td>
    <td class=\"detailForm\" style=\"white-space: nowrap;\">
      <input readonly=\"true\" type=\"text\" size=\"10\" name=\"tf_subscription_timebefore\" value=\"\" />
      <a href=\"javascript: void(0);\" onclick=\"return getCalendar(document.f_new_criterion.tf_subscription_timebefore,'$path/agenda/agenda_index.php?action=calendar&popup=1');\">
      <img src=\"/images/$set_theme/$ico_mini_cal\" border=\"0\" />
      </a>
    </td>
  </tr>
  
  </table>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the List Display preference screen
// Parameters:
//   - $prefs     : List Display preferences
//   - $prefs_con : Contact list Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_list_display_pref($prefs, $prefs_con) {
  global $l_list_display, $l_contact_display;

  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
  $dis_pref->display_entity = "list"; 
  $dis_pref->pref_title = $l_list_display;
  $dis_pref->pref_dis_help = 0;

  $block = "<center><table><tr><td>";
  $block .= $dis_pref->display();

  $dis_pref->display_pref = $prefs_con;
  $dis_pref->display_module = "list";
  $dis_pref->display_entity = "list_contact";
  $dis_pref->pref_title = $l_contact_display;

  $block .= "</td><td>";
  $block .= $dis_pref->display();

  $block .= "</td></tr></table>";
  $block .= $dis_pref->dis_pref_help();
  $block .= "</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the list delete validation screen
// Parameters:
//   - $p_id : list id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_list($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"get\"
    action=\"".url_prepare("list_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_list\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"form\"
      action=\"" . url_prepare("list_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_list\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
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
// Display the context about a list insertion or update
// When similar lists exist we show these and ask confirmation
// Parameters:
//   - $list_q   : list database result (at least 1 row)
//   - $list[]   : values for insertion/update (if confirmation)
//     keys used : name, subject
/////////////////////////////////////////////////////////////////////////////
function dis_list_warn_insert($list_q, $list) {
  global $l_check_samelist, $l_confirm, $l_back;
  global $display, $c_yes, $c_no;

  $name = $list["name"];
  $subject = $list["subject"];
  $mailing_ok = ($list["mailing_ok"] == "1" ? "1" : "0");
  $contact_arch = ($list["contact_arch"] == "1" ? "1" : "0");
  $info_pub = ($list["info_pub"] == "1" ? "1" : "0");
  $mode = ($list["mode"] == "1" ? "1" : "0");
  $query = stripslashes($list["query"]);
  if (isset($list["criteria"])) {
    $criteria = urlencode(serialize($list["criteria"]));
  }
  $display["msg"] .= display_warn_msg($l_check_samelist);
  while ($list_q->next_record()) {
    $id = $list_q->f("list_id");
    $samename = $list_q->f("list_name");
    $samesubject = $list_q->f("list_subject");
    $dis_same_list .= "
      <tr><td class=\"detailLabel\">
        <a class=\"detail\" href=\"" .url_prepare("list_index.php?action=detailconsult&amp;param_list=$id") . "\">$samename ($samesubject)</a>
      </td></tr>";
  }

  $hidden = "
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
      <input type=\"hidden\" name=\"ta_query\" value=\"$query\" />
      <input type=\"hidden\" name=\"cb_mailing_ok\" value=\"$mailing_ok\" />
      <input type=\"hidden\" name=\"cb_contact_arch\" value=\"$contact_arch\" />
      <input type=\"hidden\" name=\"cb_info_pub\" value=\"$info_pub\" />
      <input type=\"hidden\" name=\"rd_mode\" value=\"$mode\" />
      <input type=\"hidden\" name=\"se_criteria\" value=\"$criteria\" />";

  $block = "<p />
  <table class=\"detail\">
  $dis_same_list
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare("list_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      $hidden
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
      action=\"" .url_prepare("list_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"submit\" value=\"$l_back\" />
      $hidden
    </form>
    </p>
  </div>";

  return $block;
}


</script>
