<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
$fieldnames['company_name'] = $l_name;
$fieldnames['company_aka'] = $l_aka;
$fieldnames['company_number'] = $l_number;
$fieldnames['company_vat'] = $l_vat;
$fieldnames['company_siret'] = $l_siret;
$fieldnames['company_archive'] = $l_archive_first;
$fieldnames['address_street'] = $l_address;
$fieldnames['company_address'] = $l_address;
$fieldnames['address_zipcode'] = $l_postcode;
$fieldnames['address_town'] = $l_town;
$fieldnames['address_expresspostal'] = $l_expresspostal;
$fieldnames['country_name'] = $l_country;
$fieldnames['FaxPhone.phone_number'] = $l_phone_labels['WORK_FAX'];
$fieldnames['WorkPhone.phone_number'] = $l_phone_labels['WORK_VOICE'];
$fieldnames['email_address'] = $l_email;
$fieldnames['country_name'] = $l_country;
$fieldnames['website_url'] = $l_web;
// Calculated or indirect fields
$fieldnames['type_label'] = $l_type;
$fieldnames['activity_label'] = $l_activity;
$fieldnames['nafcode_code'] = $l_nafcode;
$fieldnames['company_contact_number'] = "<img src=\"$ico_contact\" alt=\"Contact\" />";
$fieldnames['company_new_contact'] = "<img src=\"$ico_add\" alt=\"New Contact\" />";
$fieldnames['company_deal_number'] = "<img src=\"$ico_deal\" alt=\"Deal\" />";


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
  global $path, $params, $ico_mail, $ico_web, $ico_add;

  $ext_url = $params['ext_url'];

  if ($fieldname == 'company_name') {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_company_get_id('.$OD->data_set->f('company_id').",'".addslashes($OD->data_set->f('company_name'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_company_get_id_url('$ext_url',".$OD->data_set->f('company_id').');';
    } else {
      $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('company_id');
    }
  }

  else if ($fieldname == 'company_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if (($fieldname == 'email_address') && $link_ok) {
    $email = $OD->data_set->f('email_address');
    if (strcmp($email,'') != 0) {
      $res['url'] = "mailto:$email";
      $res['name'] = "<img src=\"$ico_mail\" alt=\"$email\" />";
      $res['txt_name'] = $email;
    }
  }

  else if (($fieldname == 'website_url') && $link_ok) {
    $web = $OD->data_set->f('website_url');
    if (strcmp($web,'') != 0) {
      if ( (strcmp(substr($web,0,7), 'http://') == 0)
           || (strcmp(substr($web,0,8), 'https://') == 0) ) {
	$link_web = '';
      } else {
	$link_web = 'http://';
      }
      $link_web .= "$web";
      $res['url'] = $link_web;
      $res['window'] = true;
      $res['popup_width'] = 800;
      $res['popup_height'] = 600;
      $res['name'] = "<img src=\"$ico_web\" alt=\"$web\" />";
      $res['txt_name'] = $web;
    }
  }

  else if ($fieldname == 'company_contact_number') {
    $res['name'] = $OD->data_set->f('company_contact_number');
    if (($res['name'] > 0) && $link_ok) {
      $res['url'] = "$path/contact/contact_index.php?action=search&amp;searchpattern=".urlencode('companyId:'.$OD->data_set->f('company_id'));
    }
    $res['align'] = 'center';
  }

  else if (($fieldname == 'company_new_contact') && $link_ok){
    $res['align'] = 'center';
    $market = $OD->data_set->f('company_marketingmanager_id');
    $email = $OD->data_set->f('company_email');
    $ctry = $OD->data_set->f('company_country_iso3166');
    $res['url'] = "$path/contact/contact_index.php?action=updateContact&amp;company_id=".$OD->data_set->f('company_id')."&amp;market=$market";
    $res['name'] = "<img src=\"$ico_add\" title=\"Add contact\" alt=\"Add Contact\" />";
    $res['txt_name'] = ' ';
  }

  else if ($fieldname == 'company_deal_number') {
    $res['name'] = $OD->data_set->f('company_deal_number') . ' / ';
    $res['name'] .= $OD->data_set->f('company_deal_total');

    $c_id = $OD->data_set->f('company_id');
    if (($res['name'] != '0 / 0') && $link_ok) {
      $res['url'] = "$path/deal/deal_index.php?action=search&amp;company_id=$c_id";
    }
    $res['align'] = 'center';
  }
  
  else if ($fieldname == 'company_address') {
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

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company search form
// Parameters:
//   - $company[] : hash with company values
///////////////////////////////////////////////////////////////////////////////
function dis_company_search_form($company='') {

  $types = of_category_get_ordered('company', 'type');
  $acts = of_category_get_ordered('company', 'activity');
  $naf_q = run_query_companynafcode();
  $usr_q = run_query_userobm_responsable('Company',array('company_marketingmanager_id'));
  $ctry_q = run_query_global_country_for_lang();
  $dsrc_q = run_query_global_datasource();
  $block .= html_company_search_form($types, $acts, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company search Form
// Parameters:
//   - $types     : array of company types
//   - $acts      : array of company activities 
//   - $naf_q     : database object with nafcode list
//   - $usr_q     : database object with userobm list
//   - $ctry_q    : database object with country list
//   - $dsrc_q    : database object with datasource list
//   - $company[] : default form values
//     keys used  : archive, name, kind, zip, mm
///////////////////////////////////////////////////////////////////////////////
function html_company_search_form($types, $acts, $naf_q, $usr_q, $ctry_q, $dsrc_q, $company) {
  global $action, $display, $c_all, $cgp_hide;
  global $csearch_advanced_default, $l_fuzzy;
  global $l_company_name, $l_postcode, $l_town, $l_archive, $l_phone, $l_number;
  global $l_market, $l_country, $l_after, $l_before, $l_nafcode; 
  global $l_all, $l_find, $l_datasource, $l_select_company;

  $popup = $company['popup'];
  $archive = ($company['archive'] == '1' ? "checked = \"checked\"" : '');
  $name = stripslashes($company['name']);
  $number = stripslashes($company['number']);
  $phone = stripslashes($company['phone']);
  $town = stripslashes($company['town']);
  $type = $company['type'];
  $act = $company['activity'];
  $naf = $company['naf'];
  $ctry = $company['country'];
  $mark = $company['market'];
  $dsrc = $company['datasource'];
  $zip = stripslashes($company['zip']);
  $dateafter = of_date_upd_format(stripslashes($company['date_after']),true);
  $datebefore = of_date_upd_format(stripslashes($company['date_before']),true);
  if (($company['fuzzy'] == '1') ||
      ($csearch_advanced_default && $action != 'search') ) {
    $fuzzy = "checked = \"checked\"";
  } else {
    $fuzzy = '';
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('company', $company);

  // Type, activity select
  $block_type = of_category_dis_search_select('company', 'type', $types, $type);
  $block_activity = of_category_dis_search_select('company', 'activity', $acts, $act);

  // Naf Code select
  $show_naf = (! $cgp_hide['company']['nafcode']);
  if ($show_naf) {
    $sel_naf = dis_select_company_nafcode($naf_q, "<option value=\"$c_all\">$l_all</option>", $naf);

    $block_naf = "
    <label>$l_nafcode<br />
      $sel_naf
    </label>";
  }

  // Country select
  $sel_ctry = "<select name=\"sel_country\">
    <option value=\"$c_all\">$l_all</option>";
  while ($ctry_q->next_record()) {
    $ctry_iso3166 = $ctry_q->f('country_iso3166');
    $sel_ctry .= "\n<option value=\"$ctry_iso3166\"";
    if ($ctry_iso3166 == $ctry) { $sel_ctry .= " selected=\"selected\""; }
    $sel_ctry .= '>'. htmlentities($ctry_q->f('country_name'), ENT_COMPAT, 'UTF-8') . "</option>\n";
  }
  $sel_ctry .= '</select>';

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\">
    <option value=\"$c_all\">$l_all</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f('userobm_id');
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= '>'. $usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname') . "</option>\n";
  }
  $sel_market .= '</select>';

  // Data source select
  $sel_dsrc = "<select name=\"sel_datasource\">
    <option value=\"$c_all\">$l_all</option>";
  while ($dsrc_q->next_record()) {
    $d_id = $dsrc_q->f('datasource_id');
    $sel_dsrc .= "\n<option value=\"$d_id\"";
    if ($d_id == $dsrc) { $sel_dsrc .= " selected=\"selected\""; }
    $sel_dsrc .= '>'. $dsrc_q->f('datasource_name') . "</option>\n";
  }
  $sel_dsrc .= '</select>';

  $url = url_prepare('company_index.php');

  if ($popup) {
    $ext_action = $company['ext_action'];
    $ext_url = $company['ext_url'];
    $ext_id = $company['ext_id'];
    $ext_title = ($company['ext_title'] ? $company['ext_title'] : $l_select_company);
    $ext_target = $company['ext_target'];
    $ext_widget = $company['ext_widget'];
    $ext_widget_text = $company['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
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
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"$url\">
    <label>$l_company_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"24\"
      value=\"$name\" />
    </label>
    <label>$l_number<br />
      <input type=\"text\" name=\"tf_number\" size=\"16\"
      value=\"$number\" />
    </label>
    <label>$l_phone<br />
      <input type=\"text\" name=\"tf_phone\" size=\"10\"
      value=\"$phone\" />
    </label>
    <label>$l_postcode<br />
      <input type=\"text\" name=\"tf_zip\" size=\"6\"
      value=\"$zip\" />
    </label>
    <label>$l_town<br />
      <input type=\"text\" name=\"tf_town\" size=\"8\"
      value=\"$town\" />
    </label>
    <label>$l_country<br />
      $sel_ctry
     </label>
    $block_type
    $block_activity
    $block_naf
    <label>$l_datasource<br />
      $sel_dsrc
    </label>
    <label>$l_market<br />
      $sel_market
    </label>
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
    </label>
    <label>$l_before<br />
      <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
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
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      $ext
    </label>
    <p class=\"CL\" />
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
  global $display, $l_found, $l_no_found, $obm;

  $popup = $company['popup'];
  $prefs = get_display_pref($obm['uid'], 'company');
  $obm_q = run_query_company_search($company);
  $nb_company = $obm_q->num_rows_total();
  if ($nb_company == 0) {
    $display['msg'] = display_warn_msg($l_no_found);
  } else {
    $display['msg'] = display_info_msg("$nb_company $l_found");
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
    $ext_action = $company['ext_action'];
    $ext_url = urlencode($company['ext_url']);
    $ext_id = $company['ext_id'];
    $ext_target = $company['ext_target'];
    $ext_widget = $company['ext_widget'];
    $ext_widget_text = $company['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }
  // we urlencode to avoid breakage for space char
  $name = urlencode(stripslashes($company['name']));
  $number = urlencode(stripslashes($company['number']));
  $phone = urlencode(stripslashes($company['phone']));
  $town = stripslashes($company['town']);
  $ctry = $company['country'];
  $type = $company['type'];
  $act = $company['activity'];
  $naf = $company['naf'];
  $zip = urlencode(stripslashes($company['zip']));
  $market = $company['market'];
  $dsrc = $company['datasource'];
  $dateafter = stripslashes($company['date_after']);
  $datebefore = stripslashes($company['date_before']);
  $fuzzy = $company['fuzzy'];
  $archive = $company['archive'];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('company', $company);

  $url = url_prepare("company_index.php?action=search&amp;tf_name=$name&amp;tf_number=$number&amp;tf_phone=$phone&amp;sel_type=$type&amp;sel_activity=$act&amp;sel_naf=$naf&amp;tf_zip=$zip&amp;cba_archive=$archive&amp;sel_market=$market&amp;tf_town=$town&amp;sel_country=$ctry&amp;sel_datasource=$dsrc&amp;tf_date_after=$dateafter&amp;tf_date_before=$datebefore&amp;cba_fuzzy=$fuzzy$url_userdata$url_ext");

  $comp_d = new OBM_DISPLAY('DATA', $prefs, 'company');
  if ($popup) {
    $comp_d->display_link = false;
    if ($ext_url != '') {
      $comp_d->display_ext = 'get_id_url';
    } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $comp_d->display_ext = 'get_id';
    }
    $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
  }

  $comp_d->data_set = $comp_q;
  $comp_d->data_url = $url;
  $comp_d->data_header = 'both';
  $block = $comp_d->display('dis_data_company');
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
  global $ico_document,$ico_add, $ico_project, $ico_contract, $ico_lead;
  global $ico_contact, $ico_invoice, $ico_payment, $ico_deal;
  global $l_new, $l_module_contact, $l_module_project, $l_module_deal;
  global $l_module_lead, $l_module_contract, $l_module_invoice;
  global $l_module_document, $l_module_payment, $l_document_add;
  global $l_company, $l_public, $path, $cgp_show, $perm;

  $id = $co_q->f('company_id');
  $name = $co_q->f('company_name');
  $market = $co_q->f('company_marketingmanager_id');
  $email = $co_q->f('company_email');
  $ctry = $co_q->f('company_country_iso3166');
  $con_num = $co_q->f('company_contact_number');
  $deal_num = $co_q->f('company_deal_number');
  $deal_total = $co_q->f('company_deal_total');

  $uname = urlencode($name);

  // Contact
  if ($perm->check_module_rights('contact')) {
    $url_con_list = url_prepare("$path/contact/contact_index.php?action=search&amp;&amp;searchpattern=".urlencode('companyId:'.$id));
    $url_con_new = url_prepare("$path/contact/contact_index.php?action=updateContact&amp;company_id=$id&amp;market=$market");
    $block_contact = "
<div class=\"portlet\" id=\"companyContact\">
  <h1>$l_module_contact</h1>
  <ul>
  <li><a href=\"$url_con_list\"><img src=\"$ico_contact\" /></a>
      <a href=\"$url_con_list\">$l_public : ($con_num)</a></li>
  <li><a href=\"$url_con_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_con_new\">$l_new</a></li>
      </ul>
</div>";
  }

  // Deal
  if ($perm->check_module_rights('deal')) {
    $url_deal_list = url_prepare("$path/deal/deal_index.php?action=search&amp;company_id=$id&amp;tf_company_name=$uname");
    $url_deal_total = url_prepare("$path/deal/deal_index.php?action=search&amp;company_id=$id&amp;tf_company_name=$uname&amp;cba_archive=1");
    $url_deal_new = url_prepare("$path/deal/deal_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");
    $block_deal = "
<div class=\"portlet\" id=\"companyDeal\">
<h1>$l_module_deal</h1>
<ul>
  <li><a href=\"$url_deal_list\"><img src=\"$ico_deal\" /></a>
      $l_public : (
      <a href=\"$url_deal_list\">$deal_num</a> /
      <a href=\"$url_deal_total\">$deal_total</a> )</li>
  <li><a href=\"$url_deal_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_deal_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Lead
  if ($perm->check_module_rights('lead')) {
    $nb_lead = get_global_linked_element_nb('Lead', $id, 'company', 1, true);
    $url_lead_list = url_prepare("$path/lead/lead_index.php?action=search&amp;company_id=$id&amp;company_id=$id");
    $url_lead_new = url_prepare("$path/lead/lead_index.php?action=new&amp;company_id=$id&amp;company_id=$id&amp;company_name=$name");
    $block_lead = "
<div class=\"portlet\" id=\"companyLead\">
<h1>$l_module_lead</h1>
<ul>
  <li><a href=\"$url_lead_list\"><img src=\"$ico_lead\" /></a>
      $l_module_lead (<a href=\"$url_lead_list\">$nb_lead</a>)</li>
  <li><a href=\"$url_lead_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_lead_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Document
  if ($perm->check_module_rights('document')) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=company");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=company");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path.'/company/company_index.php')."&amp;ext_id=$id&amp;ext_target=$l_company";
    $nb_document = run_query_global_document_nb ($id, 'company');
    $block_doc = "
<div class=\"portlet\" id=\"companyDocument\">
<h1>$l_module_document</h1>
<ul>
  <li><a href=\"$url_doc\"><img src=\"$ico_document\" /></a>
      <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
  <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_doc_new\">$l_new</a></li>
  <li><a href=\"\" 
       onclick=\"popup('$url_doc_add',''); return false;
       \"><img src=\"$ico_add\" /></a>
      <a href=\"\" onclick=\"window.name='$l_company'; popup('$url_doc_add',''); return false;\">
       $l_document_add</a></li>
</ul>
</div>";
  }

  // Project
  if ($perm->check_module_rights('project')) {
    $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;company_id=$id");
    $url_pro_tot = url_prepare("$path/project/project_index.php?action=search&amp;company_id=$id&amp;cba_archive=1");
    $nb_pro = get_global_linked_project_nb($id, 'company');
    $nb_pro_total = get_global_linked_project_nb ($id, 'company', 1);
    $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");
    $block_project = "
<div class=\"portlet\" id=\"companyProject\">
<h1>$l_module_project</h1>
<ul>
  <li><a href=\"$url_pro\"><img src=\"$ico_project\" /></a>
      $l_module_project ( <a href=\"$url_pro\">$nb_pro</a> /
      <a href=\"$url_pro_tot\">$nb_pro_total</a> )</li>
  <li><a href=\"$url_pro_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_pro_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Contract
  if ($perm->check_module_rights('contract')) {
    $nb_ctra = get_global_linked_contract_nb($id, 'company');
    $nb_ctra_total = get_global_linked_contract_nb($id, 'company', 1);
    $url_ctra = url_prepare("$path/contract/contract_index.php?action=search&amp;company_id=$id");
    $url_ctra_total = url_prepare("$path/contract/contract_index.php?action=search&amp;company_id=$id&amp;cba_archive=1");
    $url_ctra_new = url_prepare("$path/contract/contract_index.php?action=new&amp;company_id=$id&amp;tf_company_name=$uname");
    
    $block_contract = "
<div class=\"portlet\" id=\"companyContract\">
<h1>$l_module_contract</h1>
<ul>
  <li><a href=\"$url_ctra\"><img src=\"$ico_contract\" /></a>
      $l_module_contract ( <a href=\"$url_ctra\">$nb_ctra</a> /
      <a href=\"$url_ctra_total\">$nb_ctra_total</a> )</li>
  <li><a href=\"$url_ctra_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_ctra_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Invoice
  if ($perm->check_module_rights('invoice')) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;company_id=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;company_id=$id&amp;cba_archive=1");
    $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;company_id=$id&amp;company_name=$uname");
    $nb_inv_tot = get_global_linked_invoice_nb($id, 'company', 1);
    $nb_inv = get_global_linked_invoice_nb($id, 'company');
    $block_invoice = "
<div class=\"portlet\" id=\"companyInvoice\">
<h1>$l_module_invoice</h1>
<ul>
  <li><a href=\"$url_inv\"><img src=\"$ico_invoice\" alt=\"\" /></a>
      $l_module_invoice ( <a href=\"$url_inv\">$nb_inv</a> /
      <a href=\"$url_inv_tot\">$nb_inv_tot</a> )</li>
  <li><a href=\"$url_inv_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_inv_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Payment
  if ($perm->check_module_rights('payment')) {
    $url_pay = url_prepare("$path/payment/payment_index.php?action=search&amp;company_id=$id");
    $url_pay_new = url_prepare("$path/payment/payment_index.php?action=new&amp;company_id=$id&amp;company_name=$uname");
    $nb_pay = get_global_linked_payment_nb($id, 'company');
    $block_payment = "
<div class=\"portlet\" id=\"companyPayment\">
<h1>$l_module_payment</h1>
<ul>
  <li><a href=\"$url_inv\"><img src=\"$ico_payment\" alt=\"\" /></a>
      $l_module_payment ( <a href=\"$url_pay\">$nb_pay</a> )</li>
  <li><a href=\"$url_pay_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
      <a href=\"$url_pay_new\">$l_new</a></li>
</ul>
</div>";
  }

  // Links Template  
  $block = "
$block_contact
$block_lead
$block_deal
$block_project
$block_contract
$block_doc
$block_invoice
$block_payment";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Detail
// Parameters:
//   - $params[] : company criteria
///////////////////////////////////////////////////////////////////////////////
function dis_company_consult($params) {
  global $path, $display, $obminclude, $l_err_reference;

  $cid = $params['company_id'];
  $comp_q = run_query_company_detail($cid);
  if ($comp_q->num_rows() != 1) {
    $display['msg'] .= display_err_msg($l_err_reference);
    return '';
  }
  $display['detailInfo'] = display_record_info($comp_q);
  $display['link'] = html_company_links($comp_q);
  
  $block .= html_company_header($comp_q);
  
  $view = $params['view'];
  if ($view != '') {
    global $fieldnames;
    include("$obminclude/lang/".$_SESSION['set_lang'].'/global.inc');
    include("$obminclude/lang/".$_SESSION['set_lang']."/$view.inc");
    include("$path/$view/".$view.'_display.inc');
    include("$path/$view/".$view.'_query.inc');
    $ext_func = "ext_dis_${view}_list";
    $block .= $ext_func($params, 'company', $cid);
  } else {
	  $coords = run_query_company_coords($cid);
	  $block .= html_company_view_main($comp_q, $coords);
  }
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Company Consultation
// Parameters:
//   - $co_q : company database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_header($co_q) {
  global $ico_contact, $ico_contact_new, $ico_web, $ico_mail;
  global $l_name, $l_aka, $l_number, $l_nafcode, $l_vat, $l_siret, $l_market;
  global $l_company, $l_archive;
  global $display, $l_yes, $l_no, $cgp_hide;
  
  $id = $co_q->f('company_id');
  $number = $co_q->f('company_number');
  $vat = $co_q->f('company_vat');
  $siret = $co_q->f('company_siret');
  $archive = ($co_q->f('company_archive') == 1 ? $l_yes : $l_no);
  $name = $co_q->f('company_name');
  $aka = $co_q->f('company_aka');
  $type = $co_q->f('companytype_label');
  $act = $co_q->f('companyactivity_label');
  $naf = $co_q->f('companynafcode_code').' - '.$co_q->f('companynafcode_label');
  $mark = $co_q->f('company_marketingmanager_id');
  
  $types = array(array('label' => $type));
  $acts = array(array('label' => $act));
  
  if ($mark > 0) {
    $lmarket = $co_q->f('market_lname') . ' ' . $co_q->f('market_fname');
  } else {
    $lmarket = '&nbsp;';
  }

  // Display Aka only if non empty
  if ($aka != '') {
    $dis_aka = "
  <tr> 
    <th>$l_aka :</th>
    <td>$aka</td>
  </tr>";
  } else {
    $dis_aka = '';
  }
  
  // Conditionnal fields
  // of: activity, type, activity
  $show_number = (! $cgp_hide['company']['company_number']);
  $show_vat = (! $cgp_hide['company']['company_vat']);
  $show_siret = (! $cgp_hide['company']['company_siret']);
  $show_naf = (! $cgp_hide['company']['nafcode']);

  // Number field
  if ($show_number) {
    $block_num = "
  <tr>
    <th>$l_number</th>
    <td>$number</td>
  </tr>";
  }

  // VAT field
  if ($show_vat) {
    $block_vat = "
  <tr>
    <th>$l_vat :</th>
    <td>$vat</td>
  </tr>";
  }

  // SIRET field
  if ($show_siret) {
    $block_siret = "
  <tr>
    <th>$l_siret :</th>
    <td>$siret</td>
  </tr>";
  }

  // Type, activity fields
  $block_type = of_category_dis_block_consult('company', 'type', $types, 'mono');
  $block_activity = of_category_dis_block_consult('company', 'activity', $acts, 'mono');

  // Naf code field
  if ($show_naf) {
    $block_naf = "
  <tr> 
    <th>$l_nafcode :</th>
    <td>$naf</td>
  </tr>";
  }

  $display['title'] = $name;

// --- HTML Template --------------------------------------------------------

  $block = "
<div class=\"detail infos\">
  <h1>$l_company</h1>
  <table>
    <tr>
      <th>$l_name :</th><td>$name</td>
    </tr>
    $dis_aka
    $block_num
    $block_vat
    $block_siret 
    <tr> 
      <th>$l_archive :</th>
      <td>$archive</td>
    </tr>
    $block_type
    $block_activity
    $block_naf      
    <tr>
      <th>$l_market :</th>
      <td>$lmarket</td>
    </tr>
    
  </table>
</div>

";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Company Consultation : main info tab
// Parameters:
//   - $co_q : company database result 
///////////////////////////////////////////////////////////////////////////////
function html_company_view_main($co_q, $coords) {
  global $ico_contact, $ico_contact_new, $ico_google;
  global $l_name, $l_phone,$l_fax,$l_address,$l_postcode,$l_expresspostal;
  global $l_town,$l_country,$l_email,$l_web,$l_comment;
  global $l_company, $l_coord,$ico_mail,$ico_web;
  global $display, $l_yes, $l_no, $l_copy, $ico_clipboard;
  
  $id = $co_q->f('company_id');
  $archive = ($co_q->f('company_archive') == 1 ? $l_yes : $l_no);
  $name = $co_q->f('company_name');
  $com = beautify_comment(nl2br($co_q->f('company_comment')));

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult('company', $id);
  
  foreach($coords as $kind => $row) {
    $coords = '';
    $GLOBALS["l_${kind}_labels"]['COMPANY'] = $l_company;
    foreach($row as $values) {
      $datas = array();
      foreach($values as $label => $value) {
        if($label == 'label' || $label == 'protocol') {
          if(isset($GLOBALS["l_${kind}_labels"][str_replace(';', '_', $value)])) {
            $header = $GLOBALS["l_${kind}_labels"][str_replace(';', '_', $value)];
          } else {
            $header = $value;
          }
        } elseif(!empty($value)) {
          $datas[] = nl2br($value);
        }
      }
      $template='<tr><th>%s</th><td>%s</td></tr>';
      switch ($kind) {
        case 'website' :
          $data = ((strpos($datas[0],'://') === false)?'http://'.$datas[0]:$datas[0]);
          $data = "<a target='_blank' href=\"$data\">$data</a>";
          break;
        case 'email' :
          $data = "<a href=\"mailto:$datas[0]\">$datas[0]</a>";
          break;
        case 'address' :
          $data = implode('<br />', $datas);
          $template = '<tr><th>%s</th><td>%s</td></tr><tr><td colspan="2">&nbsp;</td></tr>';
          $map_address = urlencode(strip_tags(implode(' ', $datas)));
          foreach ($datas as $k => $v) {
            $datas[$k] = addslashes($v);
          }
          $clipboard = implode('\r\n', $datas);
          $clipboard = phpStringToJsString($clipboard);
          $header.= "&nbsp;<a href=\"javascript: return false;\" onclick=\"company_copy_clip('$clipboard'); return false;\" title=\"$l_copy\" ><img alt=\"$l_copy\" src=\"$ico_clipboard\" /></a>";
          $header.= "&nbsp;<a target=\"_blank\" href=\"http://maps.google.fr/maps?f=q&amp;hl=fr&amp;q=$map_address&amp;t=h&amp;z=14&amp;source=embed\"><img alt=\"Map\" src=\"$ico_google\"></a>";
          break;
        default :
          $data = implode('<br />', $datas);
      }
      $coords .= sprintf($template, $header, $data);
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
  
  $block.= "
    $block_userdata
    <div class=\"detail extra\">
      <h1>$l_comment</h1>
      $com
    </div>";
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Company Form
// Parameters:
//   - $action    : action called
//   - $company[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_company_form($action, $company) {
  global $display, $cgp_hide, $obm, $l_err_reference;

  // of: type, activity
  $show_naf = (! $cgp_hide['company']['nafcode']);

  $c_id = $company['company_id'];
  if ($c_id > 0) {
    $comp_q = run_query_company_detail($c_id);
    if ($comp_q->num_rows() == 1) {
      $display['detailInfo'] = display_record_info($comp_q);
    } else {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
  }
  $dsrc_q = run_query_global_datasource();
  $types = of_category_get_ordered('company', 'type');
  $acts = of_category_get_ordered('company', 'activity');

  if ($show_naf) {
    $naf_q = run_query_companynafcode();
  }
 
  $uids = array($obm['uid']);
  if ($company['market'] > 0) {
    $uids[] = $company['market'];
  }
  if ($company['company_id'] > 0) {
    $uids[] = $comp_q->f('company_marketingmanager_id');
  }
  $users = of_usergroup_get_group_users($obm['group_com'], true, $uids, 'userobm_lastname');
  $ctry_q = run_query_global_country_for_lang();
  if ($c_id > 0) {
    $coords = run_query_company_coords($c_id, true);
  }
  $block = html_company_form($action, $comp_q, $dsrc_q, $types, $acts, $naf_q, $users, $ctry_q, $company, $coords);

  return $block;
}


/**
 * XHTML Display Company Form
 *
 * @param mixed $action : action called
 * @param mixed $co_q      : company database result 
 * @param mixed $dsrc_q    : datasource database result 
 * @param mixed $types     : array of types referenced by the company 
 * @param mixed $acts      : array of activities referenced by the company
 * @param mixed $naf_q     : nafcode database result 
 * @param mixed $users     : array : userobm 
 * @param mixed $ctry_q    : country database result 
 * @param mixed $company[] : default values
 * @param mixed $coords    : company secondary info
 * @return string XHTML company form
 */
function html_company_form($action, $co_q, $dsrc_q, $types, $acts, $naf_q, $users, $ctry_q, $company, $coords) {
  global $display, $obm, $cgp_hide, $c_undef, $l_undef;
  global $ico_web,$ico_mail,$ico_add, $l_header_new_f;
  global $l_datasource, $l_name, $l_aka, $l_kind, $l_activity, $l_nafcode;
  global $l_phone,$l_fax, $l_address, $l_postcode,$l_expresspostal,$l_town;
  global $l_country,$l_email,$l_web,$l_comment,$l_add_comment,$l_upd_comment;
  global $l_number, $l_vat, $l_siret, $l_market, $l_coord, $l_website;
  global $l_add, $l_insert, $l_update, $l_remove, $l_internal, $l_company, $l_archive;
  global $l_phone_labels, $l_email_labels, $l_address_labels,$l_website_labels;

  $uid = $obm['uid'];
  $lang = get_lang();
  
  $GLOBALS['js']['vars']['labels']['label'] = $l_label;
  $GLOBALS['js']['vars']['labels']['remove'] = $l_remove;
  $GLOBALS['js']['vars']['labels']['phoneLabel'] = $l_phone_labels;
  $GLOBALS['js']['vars']['labels']['emailLabel'] = $l_email_labels;
  $GLOBALS['js']['vars']['labels']['addressLabel'] = $l_address_labels;
  $GLOBALS['js']['vars']['labels']['websiteLabel'] = $l_website_labels;
  $GLOBALS['js']['vars']['labels']['phoneNumber'] = $l_number;
  $GLOBALS['js']['vars']['labels']['emailAddress'] = $l_email;
  $GLOBALS['js']['vars']['labels']['websiteUrl'] = $l_website;
  $GLOBALS['js']['vars']['labels']['addressStreet'] = $l_street;
  $GLOBALS['js']['vars']['labels']['addressTown'] = $l_town;
  $GLOBALS['js']['vars']['labels']['addressZipcode'] = $l_postcode;
  $GLOBALS['js']['vars']['labels']['addressExpressPostal'] = $l_expresspostal; 
  $GLOBALS['js']['vars']['labels']['addressCountry'] = $l_country;


  $GLOBALS['js']['vars']['labels']['countries']['none'] = '---';
  while($ctry_q->next_record()) {
    $GLOBALS['js']['vars']['labels']['countries'][$ctry_q->f('country_iso3166')] = $ctry_q->f('country_name');
  }

  // if update mode and first display values are taken from database
  if ($action == 'detailupdate') {
    $id = $co_q->f('company_id');
    $number = $co_q->f('company_number');
    $vat = $co_q->f('company_vat');
    $siret = $co_q->f('company_siret');
    $archive = ($co_q->f('company_archive') == 1 ? ' checked' : '');
    $name = $co_q->f('company_name');
    $aka = $co_q->f('company_aka');
    $dsrc = $co_q->f('company_datasource_id');
    $type = $co_q->f('company_type_id');
    $act = $co_q->f('company_activity_id');
    $naf = $co_q->f('company_nafcode_id');
    $mark = $co_q->f('company_marketingmanager_id');
    $con_num = $co_q->f('company_contact_number');
    $deal_num = $co_q->f('company_deal_number');
    $comment = $co_q->f('company_comment');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $dis_title = $name;
    $phones = $coords['phone'];
    $addresses = $coords['address'];
    $emails = $coords['email'];
    $websites = $coords['website'];
  } else if ($action == 'new') {
    $mark = $obm['uid'];
    $dsrc = $_SESSION['set_dsrc'];
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($company['company_id'])) { $id = $company['company_id']; }
  if (isset($company['number'])) { $number = stripslashes($company['number']); }
  if (isset($company['vat'])) { $vat = stripslashes($company['vat']); }
  if (isset($company['siret'])) { $siret = stripslashes($company['siret']); }
  if (isset($company['archive'])) { $archive = ($company['archive'] == 1 ? 'checked' : ''); }
  if (isset($company['name'])) { $name = stripslashes($company['name']); }
  if (isset($company['aka'])) { $aka = stripslashes($company['aka']); }
  if (isset($company['datasource'])) { $dsrc = $company['datasource']; }
  if (isset($company['type'])) { $kind = $company['type']; }
  if (isset($company['activity'])) { $act = $company['activity']; }
  if (isset($company['naf'])) { $naf = $company['naf']; }
  if (isset($company['market'])) { $mark = $company['market']; }
  if (isset($company['phones'])) { $phones = $company['phones'];}
  if (isset($company['addresses'])) { $addresses = $company['addresses'];}
  if (isset($company['websites'])) { $websites = $company['websites'];}
  if (isset($company['emails'])) { $emails = $company['emails'];}
  if (isset($company['comment'])) { $comment = stripslashes($company['comment']); }
  if (isset($company['add_comment'])) { $add_comment = stripslashes($company['add_comment']); }
  if (isset($company['usercomment'])) { $usercomment = $company['usercomment']; }
  if (isset($company['datecomment'])) { $datecomment = of_date_upd_format($company['datecomment']); }
  
  // Conditionnal fields
  $show_number = (! $cgp_hide['company']['company_number']);
  $show_vat = (! $cgp_hide['company']['company_vat']);
  $show_siret = (! $cgp_hide['company']['company_siret']);
  $show_naf = (! $cgp_hide['company']['nafcode']);

  // User defined data
  $block_userdata = of_userdata_dis_entity_form('company', $id, $company);

  // some constants
  $csize_add = '48';
  $cmax_add = '64';
  $csize_phone = '24';
  
  // Number field
  if ($show_number) {
    $block_num = "
  </tr><tr>
    <th>$l_number</th>
    <td><input type=\"text\" id=\"tf_number\" name=\"tf_number\" value=\"$number\" size=\"30\" maxlength=\"32\" /></td>";
  }

  // Vat field
  if ($show_vat) {
    $block_vat = "
  </tr><tr>
    <th>$l_vat</th>
    <td><input type=\"text\" id=\"tf_vat\" name=\"tf_vat\" value=\"$vat\" size=\"20\" maxlength=\"20\" /></td>";
  }

  // Siret field
  if ($show_siret) {
    $block_siret = "
  </tr><tr>
    <th>$l_siret</th>
    <td><input type=\"text\" id=\"tf_siret\" name=\"tf_siret\" value=\"$siret\" size=\"14\" maxlength=\"14\" /></td>";
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

  $block_type = of_category_dis_entity_form('company', 'type', $types, 'mono', $type, 'none');
  $block_activity = of_category_dis_entity_form('company', 'activity', $acts, 'mono', $act, 'none');

  // Naf code select field
  if ($show_naf) {
    $sel_naf = dis_select_company_nafcode($naf_q, "<option value=\"$c_undef\">$l_undef</option>", $naf);

    $block_naf = "
  </tr><tr>      
    <th>$l_nafcode</th>
    <td>$sel_naf</td>";
  }

  // Marketing manager select
  $sel_market = "<select name=\"sel_market\" id=\"sel_market\">
  <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($users)) {
    foreach($users as $u_id => $one_user) {
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
      $sel_market .= '>'.$one_user['lastname'].' '.$one_user['firstname']."</option>";
    }
  }
  $sel_market .= "</select>";

  // User comment select construction
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  if (is_array($users)) {
    foreach($users as $cid => $one_user) {
      $cname = $one_user['lastname'].' '.$one_user['firstname'];
      $sel_usercomment .= "<option value=\"$cname\"";
      // First time default to uid of connected user (uid) then set to name
      if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
        $sel_usercomment .= " selected = \"selected\"";
      }
      $sel_usercomment .= ">$cname</option>\n";
    }
  }
  $sel_usercomment .= '</select>';
  
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_comment = "
    $l_upd_comment
    <textarea name=\"ta_comment\" rows=\"6\">$comment</textarea>
  ";
  }

  // Buttons
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
    <!-- Update button -->
    <input type=\"hidden\" name=\"company_id\" id=\"company_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"action\" id=\"action\" value=\"update\" />
    <input type=\"submit\" value=\"$l_update\" />";
  } elseif (($action == 'new') || ($action == 'insert')) {
    $dis_button = "
    <input type=\"hidden\" id=\"action\" name=\"action\" value=\"insert\" />
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
        $js .=  "new Obm.Company.PhoneWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.Company.PhoneWidget();\n";
  // ADDRESSES 
  if(is_array($addresses)) {
    foreach($addresses as $address) {
      if(trim($address['street']) || trim($address['town']) || trim($address['country']) || trim($address['expresspostal']) || trim ($address['zipcode'])) {
        $params = array();
        foreach($address as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.Company.AddressWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.Company.AddressWidget();\n";
  // EMAILS 
  if(is_array($emails)) {
    foreach($emails as $email) {
      if(trim($email['address'])) {
        $params = array();
        foreach($email as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.Company.EmailWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.Company.EmailWidget();\n";
  // WEBSITES 
  if(is_array($websites)) {
    foreach($websites as $website) {
      if(trim($website['url'])) {
        $params = array();
        foreach($website as $field  => $value) {
          $value = phpStringToJsString($value);
          $params[] = "$field: {value : '$value'}";
        }
        $js .=  "new Obm.Company.WebsiteWidget({".implode(',',$params)."});\n";
      }
    }
  }
  $js .= "new Obm.Company.WebsiteWidget();\n";

  $display['title'] = $dis_title;

  // --- HTML Template --------------------------------------------------------

  $block = "
  $js_ctry

  <form method=\"post\" name=\"f_entity\"
  onsubmit=\"if (check_company(this)) return true; else return false;\"
    action=\"".url_prepare('company_index.php')."\">
  <fieldset class=\"detail infos\">  
  <legend>$l_company</legend>

  <table>
  <tr>
    <th>$l_name :</th>
    <td><input type=\"text\" id=\"tf_name\" name=\"tf_name\" maxlength=\"96\" size=\"50\" value=\"$name\" /></td>
  </tr><tr>
    <th>$l_aka :</th>
    <td><input type=\"text\" id=\"tf_aka\" name=\"tf_aka\" maxlength=\"255\" size=\"50\" value=\"$aka\" /></td>
  $block_num
  $block_vat
  $block_siret
  </tr><tr>
    <th>$l_datasource :</th> 
    <td>$sel_dsrc</td>
  </tr><tr>
    <th><label for=\"lbl_arch\">$l_archive :</label></th>
    <td><input type=\"checkbox\" id=\"lbl_arch\" name=\"cba_archive\" value=\"1\" $archive /></td>
  $block_type
  $block_userdata
  $block_activity
  $block_naf
  </tr><tr>    
    <th>$l_market :</th> 
    <td>$sel_market</td>
  </tr>
  </table>
  </fieldset>
  
  <fieldset class=\"detail infos\">
    <legend>$l_phone</legend>
    <div id='phoneHolder'>
    </div>
    <p><a class='B' href='' onclick=\"new Obm.Company.PhoneWidget();return false;\">$l_add</a></p>    
    </fieldset>
    <fieldset class=\"detail infos\">
    <legend>$l_address</legend>
    <div id='addressHolder'>
    </div>
    <p><a class='B' href='' onclick=\"new Obm.Company.AddressWidget();return false;\">$l_add</a></p>    
    </fieldset>
    <fieldset class=\"detail infos\">
    <legend>$l_email</legend>
    <div id='emailHolder'>
    </div>
    <p><a class='B' href='' onclick=\"new Obm.Company.EmailWidget();return false;\">$l_add</a></p>    
    </fieldset>
    <fieldset class=\"detail infos\">
    <legend>$l_website</legend>
    <div id='websiteHolder'>
    </div>
    <p><a class='B' href='' onclick=\"new Obm.Company.WebsiteWidget();return false;\">$l_add</a></p>    
    </fieldset>
    <script type='text/javascript'>
      $js
    </script>
    
  <fieldset class=\"detail extra\">    
  <legend>$l_comment</legend>
    $l_add_comment
    <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
    $sel_usercomment
    <textarea name=\"ta_add_comment\" rows=\"6\">$add_comment</textarea>
  $dis_comment
  </fieldset>
  <fieldset class=\"buttons\">$dis_button</fieldset>
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
  global $display,$path;
  
  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
  <div class=\"buttons\">
  <a href=\"$path/company/company_index.php?action=delete&amp;company_id=$p_id\" onclick=\"return confirm_company_del(this.form);\">
  $l_delete 
  </a>
  <a href=\"$path/company/company_index.php?action=detailconsult&amp;company_id=$p_id\">$l_back</a>
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

  $number = $company['number'];
  $vat = $company['vat'];
  $siret = $company['siret'];
  $archive = ($company['archive'] == 1 ? '1' : '0');
  $name = $company['name'];
  $kind = $company['kind'];
  $dsrc = $company['datasource'];
  $act = $company['activity'];
  $naf = $company['naf'];
  $mark = $company['market'];
  $com = $company['comment'];

  $phones = '';
  $param_phones = $company['phones'];
  foreach ($param_phones as $nb => $one_phone) {
    $number = $one_phone['number'];
    $label = $one_phone['label'];
    $phones .= "
    <input type=\"hidden\" name=\"phones[$nb][number]\" value=\"$number\" />
    <input type=\"hidden\" name=\"phones[$nb][label]\" value=\"$label\" />";
  }
  $emails = '';
  $param_emails = $company['emails'];
  foreach ($param_emails as $nb => $one_email) {
    $address = $one_email['address'];
    $label = $one_email['label'];
    $emails .= "
    <input type=\"hidden\" name=\"emails[$nb][address]\" value=\"$address\" />
    <input type=\"hidden\" name=\"emails[$nb][label]\" value=\"$label\" />";
  }
  $addresses = '';
  $param_ad = $company['addresses'];
  foreach ($param_ad as $nb => $one_ad) {
    $street = $one_ad['street'];
    $label = $one_ad['label'];
    $zipcode = $one_ad['zipcode'];
    $town = $one_ad['town'];
    $expresspostal = $one_ad['expresspostal'];
    $country = $one_ad['country'];
    $addresses .= "
    <input type=\"hidden\" name=\"addresses[$nb][street]\" value=\"$street\" />
    <input type=\"hidden\" name=\"addresses[$nb][label]\" value=\"$label\" />
    <input type=\"hidden\" name=\"addresses[$nb][zipcode]\" value=\"$zipcode\" />
    <input type=\"hidden\" name=\"addresses[$nb][town]\" value=\"$town\" />
    <input type=\"hidden\" name=\"addresses[$nb][expresspostal]\" value=\"$expresspostal\" />
    <input type=\"hidden\" name=\"addresses[$nb][country]\" value=\"$country\" />
";
  }
  $websites = '';
  $param_web = $company['websites'];
  foreach ($param_web as $nb => $one_web) {
    $url = $one_web['url'];
    $label = $one_web['label'];
    $websites .= "
    <input type=\"hidden\" name=\"websites[$nb][url]\" value=\"$url\" />
    <input type=\"hidden\" name=\"websites[$nb][label]\" value=\"$label\" />";
  }

  $display['msg'] .= display_warn_msg($l_check_samecompany);

  while ($co_q->next_record()) {
    $id = $co_q->f('company_id');
    $samename = $co_q->f('company_name');
    $samestreet = $co_q->f('address_street');
    $samezip = $co_q->f('address_zipcode');
    $sametown = $co_q->f('address_town');
    $dis_same_comp .= "
    <li><a href=\"" .url_prepare("company_index.php?action=detailconsult&amp;company_id=$id") . "\">
        <b>$samename</b> ($samestreet $samezip $sametown)</a>
    </li>";
  }

  // User defined data
  $userdata = of_userdata_dis_hidden_fields('company', $company);

  $hidden_fields = "
  $userdata
  <input type=\"hidden\" name=\"tf_number\" value=\"$number\" />
  <input type=\"hidden\" name=\"tf_vat\" value=\"$vat\" />
  <input type=\"hidden\" name=\"tf_siret\" value=\"$siret\" />
  <input type=\"hidden\" name=\"cba_archive\" value=\"$archive\" />
  <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
  <input type=\"hidden\" name=\"sel_kind\" value=\"$kind\" />
  <input type=\"hidden\" name=\"sel_datasource\" value=\"$datasource\" />
  <input type=\"hidden\" name=\"sel_act\" value=\"$act\" />
  <input type=\"hidden\" name=\"sel_naf\" value=\"$naf\" />
  <input type=\"hidden\" name=\"sel_market\" value=\"$mark\" />
  <input type=\"hidden\" name=\"ta_comment\" value=\"$com\" />
    $phones
    $emails
    $addresses
    $websites
";

  $block = "
<div class=\"detail\">
<ul>
$dis_same_comp
</ul>
</div>
<div class=\"buttons\">
  <form method=\"post\" name=\"form_insert\" action=\"company_index.php\">
  <input type=\"hidden\" name=\"action\" value=\"insert\" />
  <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
  $hidden_fields
  <input type=\"submit\"  value=\"$l_confirm\" />
  </form>
  <form name=\"form_back\" method=\"post\" action=\"company_index.php\">
  <input type=\"hidden\" name=\"action\" value=\"new\" />
  $hidden_fields
  <input type=\"submit\" value=\"$l_back\" />
  </form>
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the company administration index
///////////////////////////////////////////////////////////////////////////////
function dis_company_admin_index() {
  global $cgp_hide;

  if (! $cgp_hide['company']['type']) {
    $types = of_category_get_ordered('company', 'type');
    $block .= of_category_dis_admin_form('type', $types);
  }

  if (! $cgp_hide['company']['activity']) {
    $acts = of_category_get_ordered('company', 'activity');
    $block .= of_category_dis_admin_form('activity', $acts);
  }
  
  if (! $cgp_hide['company']['nafcode']) {
    $naf_q = run_query_companynafcode();
    $block .= html_company_nafcode_form($naf_q);
  }

  // User defined data
  $block .= of_userdata_dis_admin_form('company');

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
  global $ico_delete,$ico_ok;

  $char_title = '=';
  $key = 0;
  while ($naf_q->next_record()) {
    $id = $naf_q->f('companynafcode_id');
    $title = ($naf_q->f('companynafcode_title'))?"checked=\"checked\"":'';
    if ($title) {
      $pre = "$char_title";
    } else {
      $pre = '.';
    }
    $code = $naf_q->f('companynafcode_code');
    $label = $naf_q->f('companynafcode_label');
    
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $key ++;
    $c_label = htmlentities($one_cat['label'], ENT_COMPAT, 'UTF-8');
    $nafcode_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"naf_$id\" value=\"$id\" />
    <a href=\"?action=nafcode_checklink&amp;category=nafcode&amp;naf=$id\">
    <img src=\"$ico_delete\" alt=\"$l_naf_checkdelete\" title=\"$l_naf_checkdelete\" />
    </a></td>
    <td> <input type=\"checkbox\" $title id=\"cba_naf_title_$id\" value=\"1\"/></td>
    <td> <input type=\"text\" id=\"tf_naf_code_$id\" value=\"$code\" size=\"6\"/></td>
    <td> <input type=\"text\" id=\"tf_naf_label_$id\" value=\"$label\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'nafcode');\">
    <img src=\"$ico_ok\" alt=\"$l_naf_update\" title=\"$l_naf_update\" />
    </a>
    </td>
    </tr>";
  }

  // --- HTML Template --------------------------------------------------------
  $block = "
  <div class=\"detail admin\" >
  <form id=\"form_nafcode_update\" action=\"?action=nafcode_update\" method=\"post\">
  <input type=\"hidden\" name=\"cba_naf_title\" value=\"\" />
  <input type=\"hidden\" name=\"tf_naf_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_naf_label\" value=\"\" />
  <input type=\"hidden\" name=\"naf\" value=\"\" />
  </form>    
  <h1>$l_naf_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_naf_istitle</td>
  <td>$l_naf_code</td>
  <td>$l_naf_label</td>
  <td>&nbsp;</td>        
  </tr>    
  </thead>
  <tbody>
  $nafcode_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_nafcode_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_company_naf_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  </thead>
  <tbody>   
  <tr>
  <td>$l_naf_istitle</td>    
  <td>$l_naf_code</td>
  <td>$l_naf_label</td>
  </tr>       
  <tr>
  <td><input type=\"checkbox\" name=\"cba_naf_title\" value=\"1\" /></td>
  <td><input type=\"text\" name=\"tf_naf_code\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_naf_label\" value=\"\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_naf_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"nafcode_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"nafcode\" />
  </form>
  </div>";

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
  $id = $company['naf'];
  $label = get_company_nafcode_label($id);
  $obm_q = run_query_company_nafcode_links($id);
  
  $nb_naf = $obm_q->num_rows();
  if ($nb_naf > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1>$label : $l_naf_link_company ($nb_naf)</h1>";
    
    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f('company_id');
      $cname = $obm_q->f('company_name');
      $dis_link .= "<li><a href=\"company_index.php?action=detailconsult&amp;company_id=$cid\">$cname</a></li>";
    }
    if ($cpt < $nb_naf) {
      $dis_link .= '<li>...</li>';
    }

  } else {
    $dis_link_head = "<h1>$label : $l_naf_link_company_no</h1>";
  }

  $block .= "
    <div class=\"detail\">
    $dis_link_head
    <ul>
    $dis_link
    </ul>
    </div>";

  $dis_back = "<a href=\"$path/company/company_index.php?action=admin\">$l_back</a>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_naf_can_delete);
    $dis_del = "<a href=\"$path/company/company_index.php?action=nafcode_delete&amp;sel_naf=$id\" >$l_naf_delete</a>";
    $block .= "
      <div class=\"buttons\">
      $dis_del
      $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_naf_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
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

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'company');
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

  $show_naf = (! $cgp_hide['company']['nafcode']);

  if ($show_naf) {
    $sel_naf = "<select name=\"sel_naf\">
    $line1";
    while ($naf_q->next_record()) {
      $n_id = $naf_q->f('companynafcode_id');
      $n_disabled = $naf_q->f('companynafcode_title');
      $n_code = $naf_q->f('companynafcode_code');
      $n_label = $naf_q->f('companynafcode_label');
      $sel_naf .= "\n<option value=\"$n_id\"";
      if ($n_disabled == '1') {
	$sel_naf .= " disabled=\"disabled\"";
      } else {
	$n_code = "&nbsp;&nbsp;&nbsp; $n_code";
      }
      if ($n_id == $selected) { $sel_naf .= " selected=\"selected\""; }
      $sel_naf .= ">$n_code $n_label</option>\n";
    }
    $sel_naf .= '</select>';
  }

  return $sel_naf;
}

/**
 * Build a JSON array with all search results 
 * 
 * @param $results array of 'length' => DB company results nb, and 'datas' => DB User search query results
 * @access public
 * @return void
 */
function json_search_companies($company,$results) {
  global $display, $module, $cright_write_admin, $perm;

  $company_q = $results['datas'];

  $companies = array();
  foreach($company_q as $name => $data) {
    $companies[] = array(
      "id" => $data['company_id'],
      "label" => $data['company_name'],
      "extra" => $data['company_aka'] ? $data['company_aka'] : ""
    );
  }
  $search_results = array(
    "length" => $results['length'],
    "datas" => $companies
  );
  $display['json'] = json_encode($search_results);
}




?>
