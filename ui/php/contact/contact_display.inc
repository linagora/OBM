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

  else if (($fieldname == "contact_company_name") && $link_ok){
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("contact_company_id");
  }

  else if ($fieldname == "contact_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) $res["name"] ="X";
    else $res["name"] = "&nbsp;";
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
    }
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact search Form                                              //
// Parameters:
//   - $contact[] : default form values
//     keys used  : name, company
///////////////////////////////////////////////////////////////////////////////
function html_contact_search_form ($contact, $usr_q, $func_q) {
  global $l_contact_name, $l_phone, $l_from_company, $l_email, $l_archive;
  global $l_function, $l_market, $l_find, $popup, $display, $c_all, $l_all;

  $lname = stripslashes($contact["lname"]);
  $phone = stripslashes($contact["phone"]);
  $company = stripslashes($contact["company"]);
  $email = stripslashes($contact["email"]);
  $func = $contact["function"];
  $mark = $contact["marketing_manager"];
  $archive = ($contact["archive"] == "1" ? "checked = \"checked\"" : "");

  // Function select
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
      <input type=\"text\" name=\"tf_lname\" size=\"16\" value=\"$lname\" />
    </div>
    <div class=\"searchLabel\">$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"14\" maxlength=\"16\" value=\"$phone\" />
    </div>
    <div class=\"searchLabel\">$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"16\" value=\"$email\" />
    </div>
    <div class=\"searchLabel\">$l_from_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </div>
    <div class=\"searchLabel\">$l_function<br />
      $sel_func
    </div>
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
  $obm_q = run_query_search($contact, $new_order, $order_dir);
  $nb_contact = $obm_q->num_rows();
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
  $phone = urlencode(stripslashes($contact["phone"]));
  $company = urlencode(stripslashes($contact["company"]));
  $company_id = $contact["company_id"];

  $url = url_prepare("contact_index.php?action=search&amp;tf_lname=$lname&amp;tf_phone=$phone&amp;tf_company=$company&amp;cb_archive=$archive&amp;param_company=$company_id$url_ext");

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
// HTML Display Contact Consultation                                         //
// Parameters:
//   - $co_q : contact database result
///////////////////////////////////////////////////////////////////////////////
function html_contact_consult($co_q) {
  global $l_company,$l_lastname,$l_firstname,$l_kind,$l_function,$l_title;
  global $l_phone, $l_hphone,$l_mphone,$l_fax, $l_address,$l_postcode;
  global $l_market, $l_town,$l_country,$l_email,$l_mailing_ok,$l_expresspostal;
  global $l_public,$l_private,$l_archive,$l_visibility, $l_comment;
  global $l_update,$l_delete,$l_insert, $l_coord, $l_contact;
  global $c_yes, $c_no, $set_theme, $ico_mail;
  global $display, $path, $action, $auth;
  global $perms_user, $l_perms_user;

  $usercreate = $co_q->f("contact_usercreate");
  $c_id = $co_q->f("contact_company_id");
  $c_name = $co_q->f("company_name");
  $c_phone = $co_q->f("company_phone");
  $id = $co_q->f("contact_id");
  $kind = $co_q->f("kind_minilabel");
  $kind_header = $co_q->f("kind_header");
  if ($kind_header != $kind) $kind .= " ($kind_header)";
  $function = $co_q->f("function_label");
  $title = $co_q->f("contact_title");
  $market = $co_q->f("market_lname") . " " . $co_q->f("market_fname");
  $lname = $co_q->f("contact_lastname");
  $fname = $co_q->f("contact_firstname");
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
  $email = $co_q->f("contact_email");
  $mailok = ($co_q->f("contact_mailing_ok") == 1 ? $c_yes : $c_no);
  $com = nl2br($co_q->f("contact_comment"));
  $vis = $co_q->f("contact_visibility");
  $archive = ($co_q->f("contact_archive") == 1 ? $c_yes : $c_no);
  $display["link"] = html_contact_links($id);
  if ($usercreate == $auth->auth["uid"]) {
    if ($vis == 1) 
      $l_priv = $l_private;
    else 
      $l_priv = $l_public;  
    $l_priv = "</tr><tr>
      <td class=\"detailLabel\">$l_visibility</td>
      <td class=\"detailText\">$l_priv</td>";
  }

  if  ($email != "") {
    $link_email = "
      <a href=\"mailto:$email\">
      <img src=\"/images/$set_theme/$ico_mail\" alt=\"C\" />
      </a>";
  }
  
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
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">
      <a class=\"detailText\" href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id")."\"> $c_name</a>
      </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_function</td>
      <td class=\"detailText\">$function</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailText\">$title</td>
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
    </tr><tr>
      <td class=\"detailLabel\">$l_address 1</td>
      <td class=\"detailText\">$ad1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_address 2</td>
      <td class=\"detailText\">$ad2</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_address 3</td>
      <td class=\"detailText\">$ad3</td>
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
      <td class=\"detailLabel\">$l_mailing_ok</td>
      <td class=\"detailText\">$mailok</td>
    </tr>
    </table>

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
// Display: Contact links menu
// Parameters:
//   - $deal_q : DBO : information about the deal
//   - $inv_nb : invoices number
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contact_links($id) {
  // -- Themes
  global $set_theme,$ico_document,$ico_document_new;
  // -- Labels
  global $l_header_document,$l_contact;
  global $l_document_new,$l_document_add,$l_add_document;
  global $perms_user, $path, $auth,$popup_height,$popup_width;

  $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=Contact");
  $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=Contact");
  $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_document)."&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
  $nb_document = run_query_document_nb ($id,"Contact");

  $block = "<div class=\"link\">
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
  </ul>
       
 </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contact Form                                                      //
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, vis, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function dis_contact_form($action, $co_q, $contact) {
  global $display;

  $dsrc_q = run_query_datasource();
  $kind_q = run_query_kind();
  $func_q = run_query_function();
  if ($contact["marketing_manager"]) {
    $users = array($contact["marketing_manager"]);
  }
  $usr_q = run_query_userobm_active($users);
  $ctry_q = run_query_country();
  $block = html_contact_form($action, $co_q, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Contact Form
// Parameters:
//   - $action    : action called
//   - $co_q      : contact database result 
//   - $dsrc_q    : datasource database result 
//   - $func_q    : function database result 
//   - $kind_q    : kind database result 
//   - $usr_q     : active userobm database result 
//   - $ctry_q    : country database result 
//   - $contact[] : default values
//     keys used  : kind, lname, fname, ad1, ad2, ad3, zip, town, cdx,ctry,func
//                : phone, hpone, mphone, fax, email, com, vis, id, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $dsrc_q, $kind_q, $func_q, $usr_q, $ctry_q, $contact) {
  global $set_dsrc, $set_theme, $ico_company, $l_company;
  global $l_lastname,$l_firstname,$l_kind,$l_function,$l_title, $l_phone;
  global $l_hphone,$l_mphone,$l_fax,$l_address,$l_postcode,$l_expresspostal;
  global $l_market, $l_town,$l_country,$l_email,$l_mailing_ok,$l_comment;
  global $l_datasource, $l_archive, $l_private,$l_coord, $l_contact, $l_none;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $popup_width, $popup_height;
  global $display, $path, $auth, $l_none;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $c_id = $co_q->f("contact_company_id");
    $c_name = $co_q->f("company_name");
    $id = $co_q->f("contact_id");
    $usercreate = $co_q->f("contact_usercreate");
    $kind = $co_q->f("contact_kind_id");
    $mark = $co_q->f("contact_marketingmanager_id");
    $lname = $co_q->f("contact_lastname");
    $fname = $co_q->f("contact_firstname");
    $ad1 = $co_q->f("contact_address1");
    $ad2 = $co_q->f("contact_address2");
    $ad3 = $co_q->f("contact_address3");
    $zip = $co_q->f("contact_zipcode");
    $town = $co_q->f("contact_town");
    $cdx = $co_q->f("contact_expresspostal");
    $ctry = $co_q->f("contact_country_id");
    $func = $co_q->f("contact_function_id");
    $title = $co_q->f("contact_title");
    $phone = $co_q->f("contact_phone");
    $hphone = $co_q->f("contact_homephone");
    $mphone = $co_q->f("contact_mobilephone");
    $fax = $co_q->f("contact_fax");
    $email = $co_q->f("contact_email");
    $mailok = ($co_q->f("contact_mailing_ok") == 1 ? " checked" : "");
    $archive = ($co_q->f("contact_archive") == 1 ? " checked" : "");
    $vis = $co_q->f("contact_visibility");
    $com = $co_q->f("contact_comment");

  // New form and first time
  } elseif ($action == "new") {
    $dsrc = $set_dsrc;
    $mark = $auth->auth["uid"];
    // if company given
    if (is_object($co_q)) {
      // fill the phone with company one
      $phone = $co_q->f("company_phone");
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
  if (isset($contact["market"])) { $mark = $contact["market"]; }
  if (isset($contact["lname"])) { $lname = stripslashes($contact["lname"]); }
  if (isset($contact["fname"])) { $fname = stripslashes($contact["fname"]); }
  if (isset($contact["ad1"])) { $ad1 = stripslashes($contact["ad1"]); }
  if (isset($contact["ad2"])) { $ad2 = stripslashes($contact["ad2"]); }
  if (isset($contact["ad3"])) { $ad3 = stripslashes($contact["ad3"]); }
  if (isset($contact["zip"])) { $zip = stripslashes($contact["zip"]); }
  if (isset($contact["town"])) { $town = stripslashes($contact["town"]); }
  if (isset($contact["cdx"])) { $cdx = stripslashes($contact["cdx"]); }
  if (isset($contact["ctry"])) { $ctry = $contact["ctry"]; }
  if (isset($contact["function"])) { $func = $contact["function"]; }
  if (isset($contact["title"])) { $title = stripslashes($contact["title"]); }
  if (isset($contact["phone"])) { $phone = stripslashes($contact["phone"]); }
  if (isset($contact["hphone"])) { $hphone = stripslashes($contact["hphone"]); }
  if (isset($contact["mphone"])) { $mphone = stripslashes($contact["mphone"]); }
  if (isset($contact["fax"])) { $fax = stripslashes($contact["fax"]); }
  if (isset($contact["email"])) { $email = stripslashes($contact["email"]); }
  if (isset($contact["mailok"])) { $mailok = ($contact["mailok"] == 1 ? "checked" : ""); }
  if (isset($contact["com"])) { $com = stripslashes($contact["com"]); }
  if (isset($contact["vis"])) { $vis = stripslashes($contact["vis"]); }
  if (isset($contact["archive"])) { $archive = ($contact["archive"] == 1 ? "checked" : ""); }

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

  // kind select
  $sel_kind = "<select name=\"sel_kind\">";
  while ($kind_q->next_record()) {
    $k_id = $kind_q->f("kind_id");
    $k_lang = $kind_q->f("kind_lang");
    $k_label = $kind_q->f("kind_minilabel");
    $k_header = $kind_q->f("kind_header");
    $sel_kind .= "\n<option value=\"$k_id\"";
    if ($k_id == $kind) { $sel_kind .= "selected = \"selected\""; }
    $sel_kind .= ">$k_lang-$k_label-$k_header</option>";
  }
  $sel_kind .= "</select>";

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= ">". $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname") . "</option>\n";
  }
  $sel_market .= "</select>";

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

  // Country select
  $sel_ctry = "<select name=\"sel_ctry\" id=\"sel_ctry\">
    <option value=\"0\">$l_none</option>";
  while ($ctry_q->next_record()) {
    $ctry_id = $ctry_q->f("country_id");
    $sel_ctry .= "\n<option value=\"$ctry_id\"";
    if ($ctry_id == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= ">". $ctry_q->f("country_name") . "</option>\n";
  }
  $sel_ctry .= "</select>";

  // If new contact or contact update and user is owner, display visibility
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action=="detailupdate") || ($action=="update")) &&
         ($usercreate == $auth->auth["uid"]) ) ) {
    $display_private = "<input name=\"cb_vis\" type=\"checkbox\" value=\"1\" ";
    if ($vis == '1') $display_private .= " checked=\"checked\"";
    $display_private .= " />";
  }

  // UPDATE 
  if (($action=="detailupdate") || ($action=="update")) {
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

  $block = "
    <form method=\"post\" name=\"form_mod_contact\" onsubmit=\"if (check_contact(this)) return true; else return false;\" action=\"".url_prepare("contact_index.php")."\">

    <div class=\"detailHead\">$l_contact</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_lastname</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_lname\" size=\"30\" value=\"$lname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_firstname</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_fname\" size=\"30\" value=\"$fname\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\">$sel_kind</td>
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
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"/images/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
    </tr><tr>    
      <td class=\"detailLabel\">$l_function</td> 
      <td class=\"detailForm\">$sel_func</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_title</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_title\" size=\"30\" maxlength=\"64\" value=\"$title\" /></td>
    </tr><tr>    
      <td class=\"detailLabel\">$l_market</td> 
      <td class=\"detailForm\">$sel_market</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_coord</div>

    <table class=\"detail\">
    <tr>
    <td class=\"detailLabel\">$l_phone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_phone\" size=\"16\" maxlength=\"16\" value=\"$phone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_hphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_hphone\" size=\"16\" maxlength=\"16\" value=\"$hphone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_fax</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_fax\" size=\"16\" maxlength=\"16\" value=\"$fax\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_mphone</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_mphone\" size=\"16\" maxlength=\"16\" value=\"$mphone\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_address 1</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad1\" size=\"30\" value=\"$ad1\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_address 2</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad2\" size=\"30\" value=\"$ad2\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_address 3</td>
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_ad3\" size=\"30\" value=\"$ad3\" /></td>
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
    <td class=\"detailForm\"><input type=\"text\" name=\"tf_email\" size=\"52\" value=\"$email\" /></td>
    </tr><tr>
    <td class=\"detailLabel\">$l_mailing_ok</td>
    <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_mailok\" value=\"1\" $mailok /></td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
    <td colspan=\"2\" class=\"detailForm\"><textarea name=\"ta_com\" rows=\"6\" cols=\"60\">$com</textarea></td>
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
//                : ctry, func, phone, hphone, mphone, fax, email, com, vis
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
  $ad1 = stripslashes($contact["ad1"]);
  $ad2 = stripslashes($contact["ad2"]);
  $ad3 = stripslashes($contact["ad3"]);
  $zip = stripslashes($contact["zip"]);
  $town = stripslashes($contact["town"]);
  $cdx = stripslashes($contact["cdx"]);
  $ctry = $contact["ctry"];
  $func = $contact["function"];
  $title = stripslashes($contact["title"]);
  $phone = stripslashes($contact["phone"]);
  $hphone = stripslashes($contact["hphone"]);
  $mphone = stripslashes($contact["mphone"]);
  $fax = stripslashes($contact["fax"]);
  $email = stripslashes($contact["email"]);
  $com = stripslashes($contact["com"]);
  $vis = $contact["vis"];
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
    <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
    <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
    <input type=\"hidden\" name=\"tf_lname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_fname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"sel_func\" value=\"$func\" />
    <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
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
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"hidden\" name=\"cb_vis\" value=\"$vis\" />
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form id=\"form_back\" name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("contact_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_dsrc\" value=\"$dsrc\" />
    <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
    <input type=\"hidden\" name=\"tf_lname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_fname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"sel_func\" value=\"$func\" />
    <input type=\"hidden\" name=\"tf_title\" value=\"$title\" />
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
    <input type=\"hidden\" name=\"ta_com\" value=\"$com\" />
    <input type=\"hidden\" name=\"cb_vis\" value=\"$vis\" 
    <input type=\"hidden\" name=\"cb_archive\" value=\"$archive\" />
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
function dis_check_links($p_id) {
  global $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_deal, $l_link_deal_no, $l_link_contract, $l_link_contract_no;
  global $l_link_list, $l_link_list_no;
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
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : contact Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
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
function dis_admin_index() {

  $func_q = run_query_function();
  $kind_q = run_query_kind();
  $block = html_contact_kind_form($kind_q);
  $block .= "<p></p>";
  $block .= html_contact_function_form($func_q);

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
            <input type=\"text\" name=\"tf_label\" size=\"5\" maxlength=\"5\" />
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


</script>
