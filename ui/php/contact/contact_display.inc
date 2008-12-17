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
$fieldnames['contact_lastname'] = $l_lastname;
$fieldnames['contact_firstname'] = $l_firstname;
$fieldnames['contact_aka'] = $l_aka;
$fieldnames['contact_privacy'] = $l_private;
$fieldnames['function_label'] = $l_function;
$fieldnames['contact_title'] = $l_title;
$fieldnames['kind_minilabel'] = $l_kind;
$fieldnames['kind_header'] = $l_header;
$fieldnames['kind_lang'] = $l_lang;
$fieldnames['address_street'] = $l_address;
$fieldnames['contact_address'] = $l_address;
$fieldnames['contact_service'] = $l_service;
$fieldnames['address_zipcode'] = $l_postcode;
$fieldnames['address_town'] = $l_town;
$fieldnames['address_expresspostal'] = $l_expresspostal;
$fieldnames['country_name'] = $l_country;
$fieldnames['phone_number'] = $l_phone;
$fieldnames['email_address'] = $l_email;
$fieldnames['contact_archive'] = $l_archive_first;
$fieldnames['contact_date'] = $l_date;
$fieldnames['contact_mailing_ok'] = $l_mailing_ok_first;
$fieldnames['contact_newsletter'] = $l_news;
// Calculated fields
$fieldnames['company_name'] = $l_company;
$fieldnames['company_aka'] = "$l_company $l_aka";


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
  global $path, $params, $cgp_show, $ico_mail;

  $show_date = (! $cgp_hide['contact']['contact_date']);
  $ext_url = $params['ext_url'];

  //  if (($fieldname == 'contact_lastname') && $link_ok) {
  if ($fieldname == 'contact_lastname') {
    if(trim($OD->data_set->f('contact_lastname')) == '') {
      $contact_lastname = '-';
      $res['align'] = 'center';
    } else {
      $contact_lastname = $OD->data_set->f('contact_lastname');
    }
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_contact_get_id(".$OD->data_set->f('contact_id').",'".phpStringToJsString($contact_lastname)."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_contact_get_id_url('$ext_url',".$OD->data_set->f('contact_id').");";
    } else {
      $res['url'] = "$path/contact/contact_index.php?action=detailconsult&amp;contact_id=".$OD->data_set->f('contact_id');
    }
    $res['name'] = $contact_lastname;
  }

  else if (($fieldname == 'company_name') && $link_ok) {
    if ($cgp_show['module']['company']) {
      $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('contact_company_id');
    } else {
      $res['name'] = $OD->data_set->f('contact_company');
    }      
  }

  else if ($fieldname == 'contact_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if (($fieldname == 'contact_mailing_ok')
	   || ($fieldname == 'contact_newsletter')) {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }
  else if ($fieldname == 'contact_privacy') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }
  else if ($fieldname == 'email_address') {
    $email = $OD->data_set->f('email_address');
    if (strcmp($email,'') != 0) {
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\" />";
      $res['txt_name'] = $email;
    }
  }

  else if ($fieldname == 'contact_address') {
    if ($OD->data_set->f('contact_service') != '') {
      $res['name'] .= $OD->data_set->f('contact_service')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('contact_service') . ' ';
    }
    if ($OD->data_set->f('address_street') != '') {
      $res['name'] .= $OD->data_set->f('address_street')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_street') . ' ';
    }
    if ($OD->data_set->f('address_zipcode') != '') {
      $res['name'] .= $OD->data_set->f('address_zipcode')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_zipcode');
    }
    if ($OD->data_set->f('address_town') != '') {
      $res['name'] .= $OD->data_set->f('address_town')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_town');
    }
    if ($OD->data_set->f('address_expresspostal') != '') {
      $res['name'] .= $OD->data_set->f('address_expresspostal')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('address_expresspostal');
    }
    if ($OD->data_set->f('country_name') != '') {
      $res['name'] .= $OD->data_set->f('country_name')."<br />\n";
      $res['txt_name'] .= $OD->data_set->f('country_name');
    }
  }

  else if ($fieldname == 'contact_service') {
    $res['name'].= $OD->data_set->f('contact_service');
  }

  else if ($fieldname == 'country_name') {
    if ($OD->data_set->f('country_name') != '') {
      $res['name'] .= $OD->data_set->f('country_name');
    }
  }

  else if ($fieldname == 'contact_date') {
    if ($show_date) {
      $date = $OD->data_set->f('contact_date');
      $res['name'] = ($date) ? of_date_format($date) : ' ';
      $res['align'] = 'center';
    }
  }

  return $res;
}


/**
 * Display Contact search form
 *
 * @param $contact[] : hash with contact values
 **/
function dis_contact_search_form($contact='') {
  global $cgp_hide;

  if ($cgp_hide['contact']['responsible']) {
    $usr_q = '';
  } else {
    $usr_q = run_query_userobm_responsable('Contact',array('contact_marketingmanager_id'));
  }
  $funcs = of_category_get_ordered('contact', 'function');
  $ctry_q = run_query_global_country_for_lang();
  $dsrc_q = run_query_global_datasource();
  $block .= html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q);

  return $block;
}


/**
 * Display: XHTML Contact search Form
 *
 * @param $contact[] : default form values
 * @param $usr_q     : database object with userobm list
 * @param $funcs     : array with contact function list
 * @param $ctry_q    : database object with country list
 * @param $dsrc_q    : database object with datasource list
 **/
function html_contact_search_form($contact, $usr_q, $funcs, $ctry_q, $dsrc_q) {
  global $action, $cgp_hide, $l_find, $popup, $display, $c_all, $l_all;
  global $csearch_advanced_default, $l_fuzzy, $l_date, $l_timeupdate;
  global $l_title, $l_market, $l_town, $l_postcode, $l_country, $l_datasource;
  global $l_after, $l_before, $l_mailing_ok_only, $l_newsletter, $l_archive;
  global $l_contact_name, $l_firstname, $l_phone, $l_from_company, $l_email;
  global $l_visibility;

  $lname = stripslashes($contact['lname']);
  $fname = stripslashes($contact['fname']);
  $phone = stripslashes($contact['phone']);
  $company = stripslashes($contact['company']);
  $email = stripslashes($contact['email']);
  $zip = stripslashes($contact['zip']);
  $town = stripslashes($contact['town']);
  $ctry = $contact['country'];
  $dsrc = $contact['datasource'];
  $func = $contact['function'];
  $title = $contact['title'];
  $mark = $contact['market'];
  $date_field = ($contact['date_field'] ? $contact['date_field'] : 'timeupdate');
  $dateafter = of_date_upd_format(stripslashes($contact['date_after']),true);
  $datebefore = of_date_upd_format(stripslashes($contact['date_before']),true);
  $mailing_ok_only = ($contact['mailing_ok_only'] == '1' ? "checked = \"checked\"" : '');
  $privacy = $contact['privacy'];
  $newsletter = ($contact['newsletter'] == '1' ? "checked = \"checked\"" : '');
  $archive = ($contact['archive'] == '1' ? "checked = \"checked\"" : '');
  if (($contact['fuzzy'] == '1') ||
      ($csearch_advanced_default && $action != 'search') ) {
    $fuzzy = "checked = \"checked\"";
  } else {
    $fuzzy = '';
  }
  $company_id = $contact['company_id'];

  $show_func = (! $cgp_hide['contact']['function']);
  $show_title = (! $cgp_hide['contact']['contact_title']);
  $show_resp = (! $cgp_hide['contact']['responsible']);
  $show_date = (! $cgp_hide['contact']['contact_date']);

  // User defined data
  $block_userdata .= of_userdata_dis_search('contact', $contact);
  $sel_privacy = dis_privacy_search_select($privacy);

  // Selected date field
  $s_date_field[$date_field] = "selected=\"selected\"";
  if ($show_date) {
    $sel_date_date = "<option value=\"date\" $s_date_field[date]>$l_date</option>";
  }
  
  // Function select
  $block_function = of_category_dis_search_select('contact', 'function', $funcs, $func);

  // Data source select
  $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f('datasource_id');
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= '>'. $dsrc_q->f('datasource_name') . "</option>\n";
  }
  $sel_dsrc .= '</select>';

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
      $u_id = $usr_q->f('userobm_id');
      $u_name = $usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname');
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= ">$u_name</option>";
    }
    $sel_market .= '</select>';

    $block_resp = "
    <label>$l_market<br />
      $sel_market
    </label>";
  }

  // Country select
  $sel_ctry = "<select id=\"sel_country\" name=\"sel_country\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f('country_iso3166');
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= '>'. $ctry_q->f('country_name') . "</option>\n";
  }
  $sel_ctry .= '</select>';

  if ($popup) {
    $ext_action = $contact['ext_action'];
    $ext_type = $contact['ext_type'];
    $ext_url = $contact['ext_url'];
    $ext_id = $contact['ext_id'];
    $ext_title = $contact['ext_title'];
    $ext_target = $contact['ext_target'];
    $ext_widget = $contact['ext_widget'];
    $ext_widget_text = $contact['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_type\" type=\"hidden\" value=\"$ext_type\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display['title'] = $ext_title;
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
    <label>$l_visibility<br />
      $sel_privacy
    </label>      
    <label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"12\" maxlength=\"24\" value=\"$phone\" />
    </label>
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"32\" value=\"$email\" />
    </label>
    <label>$l_from_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </label>
    <label>$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" id=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </label>
    <label>$l_town<br />
      <input type=\"text\" name=\"tf_town\" id=\"tf_town\" size=\"24\"
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
    <label>$l_date<br />
      <select name=\"sel_date_field\">
        <option value=\"timeupdate\" $s_date_field[timeupdate]>$l_timeupdate</option>
        $sel_date_date
      </select>
    </label>
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" class=\"datePicker\" value=\"$dateafter\" />
    </label>
    <label>$l_before<br />
      <input type=\"text\" name=\"tf_date_before\" class=\"datePicker\" value=\"$datebefore\" />
    </label>
    <label>$l_mailing_ok_only<br />
      <input type=\"checkbox\" name=\"cba_mailing_ok_only\" value=\"1\" $mailing_ok_only />
    </label>
    <label>$l_newsletter<br />
      <input type=\"checkbox\" name=\"cba_newsletter\" value=\"1\" $newsletter />
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
  global $display, $l_found, $l_no_found, $obm;

  $popup = $contact['popup'];

  $prefs = get_display_pref($obm['uid'], 'contact');
  $obm_q = run_query_contact_search($contact);
  $nb_contact = $obm_q->num_rows_total();
  if ($nb_contact == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_contact $l_found");
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
    $ext_action = $contact['ext_action'];
    $ext_type = $contact['ext_type'];
    $ext_url = $contact['ext_url'];
    $ext_id = $contact['ext_id'];
    $ext_target = $contact['ext_target'];
    $ext_widget = $contact['ext_widget'];
    $ext_widget_text = $contact['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_type=$ext_type&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text";
  }

  // we urlencode to avoid breakage for space char
  $datasource = $contact['datasource'];
  $archive = $contact['archive'];
  $advanced = $contact['fuzzy'];
  $mailing_ok_only = $contact['mailing_ok_only'];
  $privacy = $contact['privacy'];
  $lname = urlencode(stripslashes($contact['lname']));
  $fname = stripslashes($contact['fname']);
  $phone = urlencode(stripslashes($contact['phone']));
  $email = stripslashes($contact['email']);
  $zip = stripslashes($contact['zip']);
  $town = stripslashes($contact['town']);
  $ctry = $contact['country'];
  $dateafter = stripslashes($contact['date_after']);
  $datebefore = stripslashes($contact['date_before']);
  $company = urlencode(stripslashes($contact['company']));
  $company_id = $contact['company_id'];
  $func = $contact['function'];
  $title = urlencode(stripslashes($contact['title']));
  $mark = $contact['market'];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('contact', $contact);

  $url = url_prepare("contact_index.php?action=search$url_userdata&amp;datasource=$datasource&amp;tf_lname=$lname&amp;tf_fname=$fname&amp;tf_phone=$phone&amp;tf_email=$email&amp;tf_zip=$zip&amp;tf_town=$town&amp;sel_country=$ctry&amp;tf_company=$company&amp;cba_archive=$archive&amp;cba_fuzzy=$advanced&amp;sel_privacy=$privacy&amp;cba_mailing_ok_only=$mailing_ok_only&amp;cba_newsletter=$newsletter&amp;company_id=$company_id&amp;sel_function=$func&amp;tf_title=$title&amp;sel_market=$mark$url_ext");

  $dis_contact = new OBM_DISPLAY('DATA', $prefs, 'contact');
  if ($popup) {
    $dis_contact->display_link = false;
    if ($contact['ext_type'] == 'multi') {
      $dis_contact->data_cb_text = 'X';
      $dis_contact->data_idfield = 'contact_id';
      $dis_contact->data_cb_name = 'cb_con';
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
    } else {
      if ($ext_url != '') {
	$dis_contact->display_ext = 'get_id_url';
      } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
	$dis_contact->display_ext = 'get_id';
      }
      $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
    }
  }

  $dis_contact->data_set = $obm_q;
  $dis_contact->data_url = $url;
  $dis_contact->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_contact->display('dis_data_contact');
  $block .= $display_popup_end;

  return $block;
}


/**
 * Display: Contact detail
 *
 * @param $contact[] : contact values (id)
 **/
function dis_contact_consult($contact) {
  global $display, $l_no_found, $path, $l_err_reference;

  $view = $contact['view'];
  $id = $contact['contact_id'];
  $con_q = run_query_contact_detail($id);
  if ($con_q->num_rows() != 1) {
    $display['msg'] .= display_err_msg($l_err_reference);
  }
  $display['detailInfo'] = display_record_info($con_q);
  $display['link'] = html_contact_links($id);
  $funcs = of_category_get_entitycategories('contact', 'function', $id, 'mono');
  $block .= html_contact_header($con_q, $funcs);
  
  if ($view != '') {
    include("$path/$view/".$view.'_display.inc');
    include("$path/$view/".$view.'_query.inc');
    $block .= dis_publication_subscription_external_list($contact);
  } else {
    $coords = run_query_contact_coords($id);
    $block .= html_contact_view_main($con_q, $coords);
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
  global $l_contact, $l_market, $l_archive, $l_date, $l_no_date, $l_birthday;
  global $cgp_show, $cgp_hide, $l_yes, $l_no;
  global $display, $path, $action;

  $c_id = $co_q->f('contact_company_id');
  $c_name = $co_q->f('company_name');
  $id = $co_q->f('contact_id');
  $kind_minilabel = $co_q->f('kind_minilabel');
  if (!empty($kind_minilabel)) {
    $kind = "[" . $co_q->f('kind_lang') . "] $kind_minilabel";
    $kind_header = $co_q->f('kind_header');
    if ($kind_header != $kind) $kind .= " ($kind_header)";
  } else {
    $kind = '';
  }
  $function = $co_q->f('function_label');
  $title = $co_q->f('contact_title');
  $market = $co_q->f('market_lname') . ' ' . $co_q->f('market_fname');
  $lname = $co_q->f('contact_lastname');
  $fname = $co_q->f('contact_firstname');
  $aka = $co_q->f('contact_aka');
  $archive = ($co_q->f('contact_archive') == 1 ? $l_yes : $l_no);
  $date = $co_q->f('contact_date');
  $date = ($date != 0) ? of_date_format($date) : $l_no_date;
  
  if ($co_q->f('contact_birthday')) {
    $birthday = new Of_Date($co_q->f('contact_birthday'), 'GMT');
    $birthday = $birthday->getOutputDate();
  } else {
    $birthday = $l_no_date;
  }
  
  // Display Aka only if non empty
  if ($aka != '') {
    $dis_aka = "
    <tr> 
      <th>$l_aka</th>
      <td>$aka</td>
    </tr>";
  } else {
    $dis_aka = '';
  }

  // Conditionnal fields
  $show_title = (! $cgp_hide['contact']['contact_title']);
  $show_resp = (! $cgp_hide['contact']['responsible']);
  $show_date = (! $cgp_hide['contact']['contact_date']);
  $show_birthday = (! $cgp_hide['contact']['contact_birthday']);

  $display['title'] = "$lname $fname";

  // Function
  $block_function = of_category_dis_block_consult('contact', 'function', $funcs, 'mono');

  // Company Display
  if ($cgp_show['module']['company']) {
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
  
  // Birthday consult field
  if ($show_birthday) {
    $block_birthday = "
    <tr>
      <th>$l_birthday</th>
      <td>$birthday</td>
    ";
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
    $block_birthday
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
function html_contact_view_main($co_q, $coords) {
  global $l_phone, $l_hphone, $l_mphone, $l_fax, $l_address, $l_postcode;
  global $l_service, $l_coord, $l_town, $l_country;
  global $l_expresspostal, $l_comment1, $l_comment2, $l_comment3;
  global $l_email, $l_email_other, $l_mailing_ok, $l_newsletter;
  global $ico_clipboard, $l_copy, $l_yes, $l_no,  $ico_mail;
  global $display, $cgp_show, $cgp_hide, $cclipboard_address;
  global $l_company;

  $id = $co_q->f('contact_id');
  $title = $co_q->f('contact_title');
  $c_name = $co_q->f('company_name');
  $kind_minilabel = $co_q->f('kind_minilabel');
  $lname = $co_q->f('contact_lastname');
  $fname = $co_q->f('contact_firstname');
  $service = $co_q->f('contact_service');
  $mailok = ($co_q->f('contact_mailing_ok') == 1 ? $l_yes : $l_no);
  $newsletter = ($co_q->f('contact_newsletter') == 1 ? $l_yes : $l_no);
  $com  = beautify_comment(nl2br($co_q->f('contact_comment')));
  $com2 = beautify_comment(nl2br($co_q->f('contact_comment2')));
  $com3 = beautify_comment(nl2br($co_q->f('contact_comment3')));
  
  $newline = "\\r\\n";
  if (($ad1 == '') && ($ad2 == '') && ($ad3 == '') && ($zip == '')
      && ($town == '') && ($cdx == '') && ($ctry_name == '')) {
    if (($c_ad1 != '') && (in_array('address1', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad1).$newline;
    }
    if (($c_ad2 != '') && (in_array('address2', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad2).$newline;
    }
    if (($c_ad3 != '') && (in_array('address3', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ad3).$newline;
    }
    if (($c_zip != '') && (in_array('zipcode', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_zip);
      $copy_zip = true;
    }
    if (($c_town != '') && (in_array('town', $cclipboard_address))) {
      $copy_addr_comp .= ($copy_zip ? ' ' : '') . addslashes($c_town);
      $copy_town = true;
    }
    if ($copy_zip || $copy_town) {
      $copy_addr_comp .= $newline;
    }
    if (($c_cdx != '') && (in_array('expresspostal', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_cdx).$newline;
    }
    if (($c_ctry_name != '') && (in_array('country', $cclipboard_address))) {
      $copy_addr_comp .= addslashes($c_ctry_name).$newline;
    }
  }
  if ( (($phone == '') && ($c_phone != '')) ||
       (($phone != '') && ($phone == $c_phone)) ) {
    $phone = "( $c_phone )";
  }
  if ( (($fax == '') && ($c_fax != '')) ||
       (($fax != '') && ($fax == $c_fax)) ) {
    $fax = "( $c_fax )";
  }

  // Conditionnal fields
  $show_service = (! $cgp_hide['contact']['contact_service']);
  $show_cdx = (! $cgp_hide['contact']['contact_expresspostal']);
  $show_comment2 = (! $cgp_hide['contact']['contact_comment2']);
  $show_comment3 = (! $cgp_hide['contact']['contact_comment3']);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult('contact', $id);

  $copy = addslashes("$kind_minilabel $fname $lname") . $newline;
  if (($title != '')  && (in_array('title', $cclipboard_address))) {
    $copy .= addslashes($title) . $newline;
  }
  if ($c_name != '') {
    $copy .= addslashes($c_name) . $newline;
  }
  if ($service != '') {
    $copy .= addslashes($service) . $newline;
  }
  // If contact address is empty, get the company one
  if ($copy_addr_comp != '') {
    $copy .= $copy_addr_comp;
  } else {
    if (($ad1 != '') && (in_array('address1', $cclipboard_address))) {
      $copy .= addslashes($ad1).$newline;
    }
    if (($ad2 != '') && (in_array('address2', $cclipboard_address))) {
      $copy .= addslashes($ad2).$newline;
    }
    if (($ad3 != '') && (in_array('address3', $cclipboard_address))) {
      $copy .= addslashes($ad3).$newline;
    }
    $copy_zip = false;
    $copy_town = false;
    if (($zip != '') && (in_array('zipcode', $cclipboard_address))) {
      $copy .= addslashes($zip);
      $copy_zip = true;
    }
    if (($town != '') && (in_array('town', $cclipboard_address))) {
      $copy .= ($copy_zip ? ' ' : '') . addslashes($town);
      $copy_town = true;
    }
    if ($copy_zip || $copy_town) {
      $copy .= $newline;
    }
    if (($ctry_name != '') && (in_array('country', $cclipboard_address))) {
      $copy .= addslashes($ctry_name).$newline;
    }
  }
  if (($phone != '') && (in_array('phone', $cclipboard_address))) {
    $copy .= "$l_phone : " . addslashes($phone).$newline;
  }
  if (($fax != '') && (in_array('fax', $cclipboard_address))) {
    $copy .= "$l_fax : " . addslashes($fax).$newline;
  }
  if (($mphone != '') && (in_array('mobile', $cclipboard_address))) {
    $copy .= "$l_mphone : " . addslashes($mphone).$newline;
  }
  if (($email != '') && (in_array('email', $cclipboard_address))) {
    $copy .= "$l_email : " . addslashes($email).$newline;
  }
  
  // Service field
  if ($show_service) {
    $block_service = "
    </tr><tr>
      <th>$l_service</th>
      <td>$service</td>";
  }

  foreach($coords as $kind => $row) {
    $coords = '';
    $GLOBALS["l_${kind}_labels"]['COMPANY'] = $l_company;
    foreach($row as $values) {
      $datas = array();
      foreach($values as $label => $value) {
        if($label == 'label' || $label == 'protocol') {
          if(isset($GLOBALS["l_${kind}_labels"][$value])) {
            $header = $GLOBALS["l_${kind}_labels"][$value];
          } else {
            $header = $value;
          }
        } elseif(!empty($value)) {
          $datas[] = $value;
        }
      }
      //if($kind == 'address') {
      //  $l_address 1
      //  <a href=\"javascript: return false;\" onclick=\"contact_copy_clip('$copy'); return false;\" title=\"$l_copy\" ><img alt=\"$l_copy\" src=\"$ico_clipboard\" /></a>
      //}
      switch ($kind) {
      case 'website' :
        $data = "<a href=\"$datas[0]\">$datas[0]</a>";
        break;
      case 'email' :
        $data = "<a href=\"mailto:$datas[0]\">$datas[0]</a>";
        break;
      case 'address' :
        $data = implode('<br />', $datas);
        break;
      default :
        $data = implode('<br />', $datas);
      }
      $coords .= "<tr><th>$header</th><td>$data</td></tr>";
    }
    $block .= "
    <div class=\"detail infos\">
    <h1>".$GLOBALS["l_$kind"]."</h1>
    <table>
    $coords
    </table>
    </div>
    ";
  }
  // --- HTML Template --------------------------------------------------------

  $block .= "
    <div class=\"detail infos\">
    <h1>$l_coord</h1>

    <table>
    $block_service
    </tr><tr>
      <th>$l_mailing_ok</th>
      <td>$mailok</td>
    </tr><tr>
      <th>$l_newsletter</th>
      <td>$newsletter</td>
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
  global $ico_document, $ico_add, $ico_deal,$ico_contract;
  global $ico_list, $ico_publication;
  global $l_module_document, $l_contact;
  global $l_module_deal, $l_module_list, $l_module_contract;
  global $l_document_new,$l_document_add;
  global $l_subscription_new, $l_subscription, $l_subscription_list;
  global $path, $cgp_show;

  // Deal
  if ($cgp_show['module']['deal']) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;contact_id=$id&amp;cba_archive=1");
    $deal_active = get_global_linked_deal_nb($id, 'contact1', false, 'contact2');
    $deal_total = get_global_linked_deal_nb($id, 'contact1', true, 'contact2');
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
  if ($cgp_show['module']['list']) {
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
  if ($cgp_show['module']['contract']) {
    $contract_active = get_global_linked_contract_nb($id,'contact1',false,'contact2');
    $contract_total = get_global_linked_contract_nb($id,'contact1',true,'contact2');
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
  if ($cgp_show['module']['document']) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=contact");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=contact");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contact/contact_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contact";
    $nb_document = run_query_global_document_nb ($id, 'contact');
    $block_doc = "
  <div  class=\"portlet\" id=\"contactDocument\">  
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Publication
  if ($cgp_show['module']['publication']) {
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
    <li><a href=\"javascript: void(0);\"  onclick=\"window.name='$l_contact'; popup('$url_subscription_new',''); return false;\"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"javascript: void(0);\" onclick=\"window.name='$l_contact'; popup('$url_subscription_new',''); return false;\"> $l_subscription_new</a></li>
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


/**
 * Display Contact Form
 *
 * @param mixed $action action called
 * @param mixed $co_q contact database result 
 * @param mixed $contact[] : default values
 * @return string XHTML contact form
 */
function dis_contact_form($action, $co_q, $contact) {
  global $display, $cgp_hide, $obm;

  $cid = $contact['contact_id'];
  $dsrc_q = run_query_global_datasource();
  $kind_q = run_query_contact_kind();
  $funcs = of_category_get_ordered('contact', 'function');

  $uids = array($obm['uid']);

  if (is_object($co_q)) {
    if ($co_q->f('contact_marketingmanager_id') > 0) {
      $uids[] = $co_q->f('contact_marketingmanager_id');
    }
  }
  if ($contact['market'] > 0) {
    $uids[] = $contact['market'];
  }
  $users = of_usergroup_get_group_users($obm['group_com'], true, $uids, 'userobm_lastname');
  $ctry_q = run_query_global_country_for_lang();
  if($cid > 0) {
    $coords = run_query_contact_coords($cid, true);
  }
  $block = html_contact_form($action, $co_q, $dsrc_q, $kind_q, $funcs, $users, $ctry_q, $contact, $coords);

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
//   - $users     : userobm list
//   - $ctry_q    : country database result
//   - $contact[] : default values
///////////////////////////////////////////////////////////////////////////////
function html_contact_form($action, $co_q, $dsrc_q, $kind_q, $funcs, $users, $ctry_q, $contact, $coords) {
  global $ico_company, $l_company, $ico_add, $ico_crow, $l_header_new;
  global $cgp_mailing_default, $ccontact_private_default, $cright_write_admin;
  global $l_lastname, $l_firstname, $l_aka, $l_kind, $l_title, $l_letter;
  global $l_phone, $l_hphone,$l_mphone,$l_fax,$l_postcode,$l_expresspostal;
  global $l_address, $l_town, $l_country, $l_service,$l_date, $l_no_date;
  global $l_email, $l_email_other, $l_mailing_ok, $l_newsletter;
  global $l_comment1, $l_add_comment1, $l_upd_comment1;
  global $l_comment2, $l_add_comment2, $l_upd_comment2,$l_yes;
  global $l_comment3, $l_add_comment3, $l_upd_comment3,$l_lang;
  global $l_birthday, $l_address_labels,$l_website_labels, $l_add;
  global $l_datasource, $l_archive, $l_private, $l_coord, $l_contact, $l_market;
  global $l_update, $l_checkdelete, $l_insert, $l_contact_select_company;
  global $display, $path, $obm, $perm, $cgp_show, $cgp_hide, $c_undef, $l_undef;
  global $extra_js, $l_im, $l_website, $l_phone_labels, $l_email_labels, $l_number;
  global $l_remove;


  $uid = $obm['uid'];

  $GLOBALS['js']['vars']['labels']['label'] = $l_label;
  $GLOBALS['js']['vars']['labels']['value'] = $l_number;
  $GLOBALS['js']['vars']['labels']['remove'] = $l_remove;
  $GLOBALS['js']['vars']['labels']['phoneLabel'] = $l_phone_labels;
  $GLOBALS['js']['vars']['labels']['emailLabel'] = $l_email_labels;
  $GLOBALS['js']['vars']['labels']['addressLabel'] = $l_address_labels;
  $GLOBALS['js']['vars']['labels']['websiteLabel'] = $l_website_labels;
  
  // if update mode and first time values are taken from database
  if ($action == 'detailupdate') {
    $id = $co_q->f('contact_id');
    $c_id = $co_q->f('contact_company_id');
    $c_name = $co_q->f('company_name');
    $usercreate = $co_q->f('contact_usercreate');
    $dsrc = $co_q->f('contact_datasource_id');
    $kind = $co_q->f('contact_kind_id');
    $mark = $co_q->f('contact_marketingmanager_id');
    $date = $co_q->f('contact_date');
    $date = of_date_upd_format($date, true);
    
    if ($co_q->f('contact_birthday')) {
      $birthday = $co_q->f('contact_birthday');
      $birthday = new Of_Date($birthday);
      $birthday = $birthday->getOutputDate();
    }
    
    $lname = $co_q->f('contact_lastname');
    $fname = $co_q->f('contact_firstname');
    $aka = $co_q->f('contact_aka');
    $service = $co_q->f('contact_service');
    $func = $co_q->f('contact_function_id');
    $title = $co_q->f('contact_title');
    $mailok = ($co_q->f('contact_mailing_ok') == 1 ? ' checked' : '');
    $newsletter = ($co_q->f('contact_newsletter') == 1 ? ' checked' : '');
    $archive = ($co_q->f('contact_archive') == 1 ? ' checked' : '');
    $priv = $co_q->f('privacy');
    $comment = $co_q->f('contact_comment');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $comment2 = $co_q->f('contact_comment2');
    $usercomment2 = $uid;
    $datecomment2 = of_date_upd_format();
    $comment3 = $co_q->f('contact_comment3');
    $usercomment3 = $uid;
    $datecomment3 = of_date_upd_format();
    $c_phone = $co_q->f('company_phone');
    $c_fax = $co_q->f('company_fax');
    $c_ad1 = $co_q->f('company_address1');
    $c_ad2 = $co_q->f('company_address2');
    $c_ad3 = $co_q->f('company_address3');
    $c_zip = $co_q->f('company_zipcode');
    $c_town = $co_q->f('company_town');
    $c_cdx = $co_q->f('company_expresspostal');
    $c_ctry = $co_q->f('company_country_iso3166');
    $c_ctry_name = $co_q->f('company_country_name');
    $dis_title = "$lname $fname";
    $phones = $coords['phone'];
    $addresses = $coords['address'];
    $emails = $coords['email'];
    $websites = $coords['website'];
    $ims = $coords['im'];

  // New form and first time
  } elseif ($action == 'new') {
    $kind = get_contact_kind_default();
    $dsrc = $_SESSION['set_dsrc'];
    $mark = $obm['uid'];
    $date = of_date_upd_format();
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $usercomment2 = $uid;
    $datecomment2 = of_date_upd_format();
    $usercomment3 = $uid;
    $datecomment3 = of_date_upd_format();
    if ($ccontact_private_default) {
      $private_c = ' checked';
    }
    if ($cgp_mailing_default) {
      $mailok = ' checked';
      $newsletter = ' checked';
    }
    // if company given
    if (is_object($co_q)) {
      // fill the phone with company one
      $c_phone = $co_q->f('company_phone');
      $c_fax = $co_q->f('company_fax');
      $c_ad1 = $co_q->f('company_address1');
      $c_ad2 = $co_q->f('company_address2');
      $c_ad3 = $co_q->f('company_address3');
      $c_zip = $co_q->f('company_zipcode');
      $c_town = $co_q->f('company_town');
      $c_cdx = $co_q->f('company_expresspostal');
      $c_ctry_name = $co_q->f('company_country_name');
      $c_id = $co_q->f('company_id');
      $c_name = $co_q->f('company_name');
      $c_new_id = $c_id;
      $c_new_name = $c_name;
    }
  }

  // If parameters have been given, they supercede the default action value
  if (isset($contact['company_id'])) { $c_id = $contact['company_id']; }
  if (isset($contact['company_name'])) { $c_name = $contact['company_name']; }
  if (isset($contact['company_new_id'])) { $c_new_id = $contact['company_new_id']; }
  if (isset($contact['company_new_name'])) { $c_new_name = $contact['company_new_name']; }
  if (isset($contact['contact_id'])) { $id = $contact['contact_id']; }
  if (isset($contact['usercreate'])) { $usercreate = $contact['usercreate']; }
  if (isset($contact['datasource'])) { $dsrc = $contact['datasource']; }
  if (isset($contact['kind'])) { $kind = $contact['kind']; }
  if (isset($contact['market'])) { $mark = $contact['market']; }
  if (isset($contact['date'])) { $date = of_date_upd_format($contact['date'],true); }
  if (isset($contact['birthday'])) { $birthday = of_date_upd_format($contact['birthday'],true); }
  if (isset($contact['lname'])) { $lname = stripslashes($contact['lname']); }
  if (isset($contact['fname'])) { $fname = stripslashes($contact['fname']); }
  if (isset($contact['aka'])) { $aka = stripslashes($contact['aka']); }
  if (isset($contact['service'])) { $service = stripslashes($contact['service']); }
  if (isset($contact['function'])) { $func = $contact['function']; }
  if (isset($contact['title'])) { $title = stripslashes($contact['title']); }
  if (isset($contact['phones'])) { $phones = $contact['phones'];}
  if (isset($contact['addresses'])) { $addresses = $contact['addresses'];}
  if (isset($contact['ims'])) { $ims = $contact['ims'];}
  if (isset($contact['websites'])) { $websites = $contact['websites'];}
  if (isset($contact['emails'])) { $emails = $contact['emails'];}
  if (isset($contact['mphone'])) { $mphone = stripslashes($contact['mphone']); }
  if (isset($contact['fax'])) { $fax = stripslashes($contact['fax']); }
  if (isset($contact['mailok'])) { $mailok = ($contact['mailok'] == 1 ? 'checked' : ''); }
  if (isset($contact['comment'])) { $comment = stripslashes($contact['comment']); }
  if (isset($contact['add_comment'])) { $add_comment = stripslashes($contact['add_comment']); }
  if (isset($contact['usercomment'])) { $usercomment = $contact['usercomment']; }
  if (isset($contact['datecomment'])) { $datecomment = of_date_upd_format($contact['datecomment']); }
  if (isset($contact['comment2'])) { $comment2 = stripslashes($contact['comment2']); }
  if (isset($contact['add_comment2'])) { $add_comment2 = stripslashes($contact['add_comment2']); }
  if (isset($contact['usercomment2'])) { $usercomment2 = $contact['usercomment2']; }
  if (isset($contact['datecomment2'])) { $datecomment2 = of_date_upd_format($contact['datecomment2']); }
  if (isset($contact['comment3'])) { $comment3 = stripslashes($contact['comment3']); }
  if (isset($contact['add_comment3'])) { $add_comment3 = stripslashes($contact['add_comment3']); }
  if (isset($contact['usercomment3'])) { $usercomment3 = $contact['usercomment3']; }
  if (isset($contact['datecomment3'])) { $datecomment3 = of_date_upd_format($contact['datecomment3']); }
  if (isset($contact['privacy'])) { $priv = stripslashes($contact['privacy']); }
  if (isset($contact['archive'])) { $archive = ($contact['archive'] == 1 ? 'checked' : ''); }

  // Conditionnal fields
  $show_title = (! $cgp_hide['contact']['contact_title']);
  $show_resp = (! $cgp_hide['contact']['responsible']);
  $show_date = (! $cgp_hide['contact']['contact_date']);
  $show_service = (! $cgp_hide['contact']['contact_service']);
  $show_ad3 = (! $cgp_hide['contact']['contact_address3']);
  $show_cdx = (! $cgp_hide['contact']['contact_expresspostal']);
  $show_comment2 = (! $cgp_hide['contact']['contact_comment2']);
  $show_comment3 = (! $cgp_hide['contact']['contact_comment3']);
  $show_birthday = (! $cgp_hide['contact']['birthday']);

  // User defined data
  $block_userdata = of_userdata_dis_entity_form('contact', $id, $contact);

  // some constants
  $csize_add = '48';
  $cmax_add = '64';
  $csize_phone = '24';

  // Function field
  $block_function = of_category_dis_entity_form('contact', 'function', $funcs, 'mono', $func, 'none');

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
    if(is_array($users)) {
      foreach ($users as $u_id => $one_user) {
        $sel_market .= "\n<option value=\"$u_id\"";
        if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
        $sel_market .= '>'. $one_user['lastname'].' '.$one_user['firstname'] . "</option>\n";
      }
    }
    $sel_market .= '</select>';

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
  
  // Birthday form field
  if ($show_birthday) {
    $block_birthday = "
    <tr>
      <th>$l_birthday</th>
      <td><input type=\"text\" name=\"tf_birthday\" value=\"$birthday\" class=\"datePicker\"/></td>
    </tr>";
  }

  // data source select
  $sel_dsrc = "<select name=\"sel_datasource\" id=\"sel_datasource\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f('datasource_id');
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= '>'. $dsrc_q->f('datasource_name') . "</option>\n";
  }
  $sel_dsrc .= '</select>';
  if ($kind_q->nf()>0) {
    $i=0;
    $j=0;
    $k=0;
    $k_kind_js .= "lang_array = new Object();\n";
    while ($kind_q->next_record()) {
      $k_id = $kind_q->f('kind_id');
      $k_lang = $kind_q->f('kind_lang');
      $k_label = $kind_q->f('kind_minilabel');
      $k_header = $kind_q->f('kind_header');
      if ($k_lang != $k_old_lang) {
	$k_kind_js .= "lang_array[\"$k_lang\"] = new Object();\n";
	$k_old_lang = $k_lang;
      }     
      if ($k_label !== $k_old_label) {
	$k_kind_js .= "lang_array[\"$k_lang\"][\"$k_label\"] = new Object();\n";
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
  if(is_array($k_kind)) {
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
  }
  $sel_kind_label .= '</select>';
  $sel_kind_lang .= '</select>';
  $sel_kind_header .= '</select>';
  $script = "
    <script type=\"text/javascript\">
      $k_kind_js
    </script>\n";

  // Country select
  $sel_ctry = "<select name=\"sel_country\" id=\"sel_country\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f('country_iso3166');
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= '>'. $ctry_q->f('country_name') . "</option>\n";
  }
  $sel_ctry .= '</select>';

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
  $opt_usercomment = '';
  if(is_array($users)) {
    foreach ($users as $cid => $one_user) {
      $cname = $one_user['lastname'].' '.$one_user['firstname'];
      $opt_usercomment .= "<option value=\"$cname\"";
      // First time default to uid of connected user (uid) then set to name
      if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
        $opt_usercomment .= " selected = \"selected\"";
      }
      $opt_usercomment .= ">$cname</option>\n";
    }
  }


  $sel_usercomment = "<select name=\"sel_usercomment\">$opt_usercomment</select>";

  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_comment = "
        $l_upd_comment1
        <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>";
    if ($show_comment2) {
      $dis_comment2 = "
        $l_upd_comment2
        <textarea name=\"ta_comment2\" rows=\"6\" cols=\"78\">$comment2</textarea>";
    }
    if ($show_comment3) {
      $dis_comment3 = "
        $l_upd_comment3
        <textarea name=\"ta_comment3\" rows=\"6\" cols=\"78\">$comment3</textarea>";
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
  if ($cgp_show['module']['company']) {
    $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"popup('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";
  } else {
    $dis_company = "<input type=\"text\" name=\"tf_company\" size=\"30\" maxlength=\"64\" value=\"$c_name\" />";
  }

  // Check
  if (! $perm->check_right('contact', $cright_write_admin)) {
    $display_private = "<input name=\"cba_privacy\" type=\"hidden\" value=\"1\" />$l_yes";

  // If new contact or contact update and user is owner, display visibility
  } else if ( ($action == 'new') || ($action == 'insert') ||
       ( (($action == 'detailupdate') || ($action == 'update')) &&
         ($usercreate == $obm['uid']) ) ) {
    if ($priv == '1') {
      $private_c = ' checked';
    }
    $display_private = "<input name=\"cba_privacy\" type=\"checkbox\" value=\"1\" $private_c />";
  }

  // UPDATE 
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"contact_id\" value=\"$id\" />
      <input type=\"hidden\" name=\"hd_usercreate\" value=\"$usercreate\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action == 'new') || ($action == 'insert')) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  
  // PHONES
  if(is_array($phones)) {
    foreach($phones as $phone) {
      if(trim($phone['number'])) {
        $params = array();
        foreach($phone as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.PhoneWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.PhoneWidget();\n";
  // ADDRESSES 
  if(is_array($addresses)) {
    foreach($addresses as $address) {
      if(trim($address['street']) || trim($address['town']) || trim($address['country']) || trim($address['expresspostal']) || trim ($address['zipcode'])) {
        $params = array();
        foreach($address as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.AddressWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.AddressWidget();\n";
  // EMAILS 
  if(is_array($emails)) {
    foreach($emails as $email) {
      if(trim($email['address'])) {
        $params = array();
        foreach($email as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.EmailWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.EmailWidget();\n";
  // WEBSITES 
  if(is_array($websites)) {
    foreach($websites as $website) {
      if(trim($website['url'])) {
        $params = array();
        foreach($website as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.WebsiteWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.WebsiteWidget();\n";
  // IM 
  if(is_array($ims)) {
    foreach($ims as $im) {
      if(trim($im['address'])) {
        $params = array();
        foreach($im as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.IMWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.IMWidget();\n";

  $display['title'] = "$dis_title";

  // --- HTML Template --------------------------------------------------------
  $block = "$script";
  
  $block .= "
    <form method=\"get\" name=\"f_entity\" onsubmit=\"if (check_contact(this)) return true; else return false;\" action=\"".url_prepare('contact_index.php')."\">

    <fieldset class=\"detail infos\">
    <legend>$l_contact</legend>

    <table>
    <tr>
      <th>$l_lastname</th>
      <td><input type=\"text\" name=\"tf_lname\" size=\"30\" maxlength=\"64\" value=\"$lname\" /></td>
    </tr>
    <tr>
      <th>$l_firstname</th>
      <td><input type=\"text\" name=\"tf_fname\" size=\"30\" maxlength=\"64\" value=\"$fname\" /></td>
    </tr>
    <tr>
      <th>$l_aka</th>
      <td><input type=\"text\" id=\"tf_aka\" name=\"tf_aka\" maxlength=\"255\" size=\"50\" value=\"$aka\" /></td>
    </tr>
    <tr>
      <th>$l_lang</th>
      <td> $sel_kind_lang</td>
    </tr>
    <tr>
      <th>$l_kind</th>
      <td>$sel_kind_label</td>
    </tr>
    <tr>
      <th>$l_letter</th>
      <td>$sel_kind_header </td>
    </tr>
    <tr>
      <th>$l_datasource</th> 
      <td>$sel_dsrc</td>
    </tr>
    <tr>
      <th>$l_private</th>
      <td>$display_private</td>
    </tr>
    <tr>
      <th>$l_archive</th>
      <td><input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /></td>
    </tr>
    <tr>
      <th>$l_company</th>
      <td>$dis_company</td>
    </tr>
    $block_function
    $block_title
    $block_resp
    $block_date
    $block_userdata
    $block_birthday
    <tr>
    <th>$l_mailing_ok</th>
    <td><input type=\"checkbox\" name=\"cba_mailok\" value=\"1\" $mailok /></td>
    </tr><tr>
    <th>$l_newsletter</th>
    <td><input type=\"checkbox\" name=\"cba_newsletter\" value=\"1\" $newsletter /></td>
    </tr>    
    </table>
    </fieldset>

    <fieldset class=\"detail infos\" id='phoneHolder'>
    <legend>$l_phone <a href='' onclick=\"new Obm.PhoneWidget();return false;\">$l_add</a></legend>
    </fieldset>
    <fieldset class=\"detail infos\" id='addressHolder'>
    <legend>$l_address <a href='' onclick=\"new Obm.AddressWidget();return false;\">$l_add</a></legend>
    </fieldset>
    <fieldset class=\"detail infos\" id='emailHolder'>
    <legend>$l_email <a href='' onclick=\"new Obm.EmailWidget();return false;\">$l_add</a></legend>
    </fieldset>
    <fieldset class=\"detail infos\" id='imHolder'>
    <legend>$l_im <a href='' onclick=\"new Obm.IMWidget();return false;\">$l_add</a></legend>
    </fieldset>
    <fieldset class=\"detail infos\" id='websiteHolder'>
    <legend>$l_website <a href='' onclick=\"new Obm.WebsiteWidget();return false;\">$l_add</a></legend>
    </fieldset>
    <script type='text/javascript'>
      $js
    </script>

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

  $comp_id = ($contact['company_new_id'] ? $contact['company_new_id'] : $contact['company_id']);
  $lname = stripslashes($contact['lname']);
  $fname = stripslashes($contact['fname']);
  $kind = $contact['kind'];
  $dsrc = $contact['datasource'];
  $privacy = $contact['privacy'];
  $func = $contact['function'];
  $title = stripslashes($contact['title']);
  $mark = $contact['market'];
  $service = stripslashes($contact['service']);
  $ad1 = stripslashes($contact['ad1']);
  $ad2 = stripslashes($contact['ad2']);
  $ad3 = stripslashes($contact['ad3']);
  $zip = stripslashes($contact['zip']);
  $town = stripslashes($contact['town']);
  $cdx = stripslashes($contact['cdx']);
  $ctry = $contact['country'];
  $phone = stripslashes($contact['phone']);
  $birthday = $contact['birthday'];
  $hphone = stripslashes($contact['hphone']);
  $mphone = stripslashes($contact['mphone']);
  $fax = stripslashes($contact['fax']);
  $email = stripslashes($contact['email']);
  $email2 = stripslashes($contact['email2']);
  $mailok = $contact['mailok'];
  $com = stripslashes($contact['comment']);
  $com2 = stripslashes($contact['comment2']);
  $com3 = stripslashes($contact['comment3']);
  $archive = $contact['archive'];

  $disp_same_cont_head = "<h1>$l_check_samecontact</h1>";
  while ($co_q->next_record()) {
    $id = $co_q->f('contact_id');
    $samename = $co_q->f('contact_lastname').' '. $co_q->f('contact_firstname');
    $company = $co_q->f('company_name');
    $disp_same_cont .= "<li><a class=\"detail\" href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$id") . "\">
          $samename ($company)</a></li>";

  }

  // User defined data
  $userdata = of_userdata_dis_hidden_fields('contact', $contact);

  $hidden = "
    <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"tf_lname\" value=\"$lname\" />
    <input type=\"hidden\" name=\"tf_fname\" value=\"$fname\" />
    <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
    <input type=\"hidden\" name=\"sel_datasource\" value=\"$dsrc\" />
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
    <input type=\"hidden\" name=\"tf_birthday\" value=\"$birthday\" />
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
    <div class=\"detail\">
    $disp_same_cont_head 
    <ul>
    $disp_same_cont
    </ul>
    </div>

    <div class=\"buttons\">
    <form method=\"post\" id=\"form_insert\" name=\"form_insert\" action=\"" .url_prepare('contact_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"insert\" />
    <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
    $hidden
    <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    <form id=\"form_back\" name=\"form_back\" method=\"post\" action=\"" .url_prepare('contact_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    $hidden
    <input type=\"submit\" value=\"$l_back\" />
    </form>
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
  global $path, $display;

  $dis_back = "<a href=\"$path/contact/contact_index.php?action=detailconsult&amp;contact_id=$p_id\">$l_back</a>";

  $dis_delete = "
    <a href=\"$path/contact/contact_index.php?action=delete&amp;contact_id=$p_id\" onclick=\"return confirm_contact_del(this.form);\">
    $l_delete 
    </a>";

  $display['msg'] .= display_ok_msg($l_can_delete);

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

  if (! $cgp_hide['contact']['function']) {
    $funcs = of_category_get_ordered('contact', 'function');
    $block .= of_category_dis_admin_form('function', $funcs);
  }

  // User defined data
  $block .= of_userdata_dis_admin_form('contact');

  // Hook : Post
  if (function_exists('hook_post_dis_contact_admin_index')) {
    $block = hook_post_dis_contact_admin_index($block);
  }

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

  $char_title = '=';
  $key = 0;
  while ($kind_q->next_record()) {
    $id = $kind_q->f('kind_id');
    $lang = $kind_q->f('kind_lang');
    $default = ($kind_q->f('kind_default'))== 1 ? "checked=\"checked\"" : '';
    $mlabel = $kind_q->f('kind_minilabel');
    $header = $kind_q->f('kind_header');
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;
    $c_label = htmlentities($one_cat['label']);
    $kind_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"kind_$id\" value=\"$id\" />
    <a href=\"?action=kind_checklink&amp;category=kind&amp;kind=$id\">
    <img src=\"$ico_delete\" alt=\"$l_kind_checkdelete\" title=\"$l_kind_checkdelete\" />
    </a></td>
    <td><input type=\"checkbox\" id=\"cba_kind_default_$id\" value=\"1\" $default /></td>
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
  <input type=\"hidden\" name=\"cba_kind_default\" value=\"\" />
  <input type=\"hidden\" name=\"tf_kind_lang\" value=\"\" />
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
  onsubmit=\"return check_contact_kind_new(this)\">
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
  global $path, $display;
  global $l_back, $l_kind_link_company, $l_kind_link_company_no;
  global $l_kind_link_contact, $l_kind_link_contact_no;
  global $l_kind_delete, $l_kind_can_delete, $l_kind_cant_delete;

  $delete_ok = true;
  $id = $ref['kind'];
  $name = get_contact_kind_label($id);

  // Contacts Links
  $obm_q = run_query_contact_kind_links($id);
  $nb_kind = $obm_q->num_rows();
  if ($nb_kind > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1> $l_kind_link_contact :$name ($nb_kind)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f('contact_id');
      $cname = $obm_q->f('contact_lastname').' '.$obm_q->f('contact_firstname');
      $dis_link .= "<li><a href=\"" .url_prepare("contact_index.php?action=detailconsult&amp;contact_id=$cid") . "\">$cname</a></li>";
    }
    if ($cpt < $nb_kind) {
      $dis_link .= "<li>...</li>";
    }

  } else {
    $dis_link_head = "<h1> $l_kind_link_contact_no $name</h1>";
  }

  $block = "<div class=\"detail\">
             $dis_link_head
            <ul>
             $dis_link
            </ul>
            </div>";

  $dis_back = "<a href=\"$path/contact/contact_index.php?action=admin\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_kind_can_delete);
    $dis_del = "<a href=\"$path/contact/contact_index.php?action=kind_delete&amp;sel_kind=$id\" >$l_kind_delete</a>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_kind_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
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
 
  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'contact');
  $dis_pref->pref_title = $l_contact_display;
  $dis_pref->pref_dis_help = 1;

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the VCard import form
///////////////////////////////////////////////////////////////////////////////
function dis_vcard_import_form() {
  global $l_header_import, $l_vcf_file;
  // --- HTML Template --------------------------------------------------------
  $dis_button = "<input type=\"hidden\" name=\"action\" value=\"vcard_insert\">
          <input type=\"submit\" value=\"$l_header_import\">";

  $block = "
    <form method=\"post\" name=\"vcard_import\" enctype=\"multipart/form-data\"
      action=\"contact_index.php\">
    <fieldset class=\"detail extra\">
      <legend></legend>
      <table class=\"detail\">
      <tr>
        <th class=\"detailLabel\">$l_vcf_file</th>
        <td><input name=\"vcard_file\" type=\"file\" value=\"\"/></td>
      </tr>
      </table>
    </fieldset>
    <fieldset class=\"buttons\">
      $dis_button
    </fieldset>
    </form>
    <p class=\"LC\" />" ;

  return $block ;
}


///////////////////////////////////////////////////////////////////////////////
// Display Vcard Export
// Parameters:
//   - $contact[] : contact id
//     keys used  : contact_id
///////////////////////////////////////////////////////////////////////////////
function dis_contact_vcard_export($contact) {
  global $display, $path, $l_err_reference;

  $vcard = OBM_Contact::get($contact['contact_id'])->toVcard();
  
  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=".str_replace(' ','_',$vcard->name->family).',_'.str_replace(' ','_',$vcard->name->given).'.vcf');
  header('charset=utf-8');
  echo $vcard->__toString();
}

///////////////////////////////////////////////////////////////////////////////
// Display Export all private contacts to VCards
///////////////////////////////////////////////////////////////////////////////
function dis_contact_vcard_export_all($contacts) {
  global $obm;
  
  $vcards = array();
  foreach ($contacts as $c) {
    $vcards[] = $c->toVcard()->__toString();
  }
  
  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=contacts.vcf");
  header('charset=utf-8');
  echo implode("\n", $vcards);
}

///////////////////////////////////////////////////////////////////////////////
// Display Statistics About Contact number evolution by date
// Parameters:
// Returns:
///////////////////////////////////////////////////////////////////////////////
function dis_html_contact_date_graph_stats($entity) {
  global $path, $l_contact_date_evolution_graph;

  $url = url_prepare("$path/statistic/statistic_index.php?action=contact_date_evolution_graph");

  $block = "<div class=\"detail extra\">
   <h1>".ucfirst($l_contact_date_evolution_graph)."</h1>
       <img src=\"$url\" />
   </div>";
 
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

  $order = $params['order'];
  $stat = $params['stat'];

  if (($stat == '') || ($stat == 'index')) {

  } elseif ($stat == 'function') {
    $cats = of_category_query_category_per_entity('contact','function','mono');
    $block_stat = of_category_dis_entity_stats('contact', $stat, $cats,$order);
  } elseif ($stat == 'contact_date_evolution') {
    $block_stat = dis_html_contact_date_graph_stats('contact'); 
  } else {
    $cats = of_category_user_get_category_distribution('contact', $stat);
    $block_stat = of_category_dis_entity_stats('contact', $stat, $cats,$order);
  }

  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      global ${"l_$cat_name"};
      $l_cat = ${"l_$cat_name"};
      $block_contact .= "<li><a href=\"?action=statistics&amp;stat=$cat_name\">$l_cat</a></li>";
    }
  }


  $block = "
    <div class=\"detail extra\">
    <ul>
    <li><a href=\"?action=statistics&amp;stat=function\">$l_function</a></li>
    <li><a href=\"?action=statistics&amp;stat=contact_date_evolution\">$l_contact_date_evolution_graph;</a></li>
    $block_contact
    </ul>
    </div>
    $block_stat
";

  return $block;
}

?>
