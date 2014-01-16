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
$fieldnames['list_name'] = $l_name;
$fieldnames['list_subject'] = $l_subject;
$fieldnames['list_email'] = $l_email;
$fieldnames['list_info_publication'] = $l_info_pub;
$fieldnames['list_mode'] = $l_mode;
$fieldnames['usercreate'] = $l_creator;
$fieldnames['userupdate'] = $l_updater;
$fieldnames['timecreate'] = $l_date_creation;
$fieldnames['timeupdate'] = $l_date_last_update;
// Calculated fields
$fieldnames['list_nb_contact'] = "$l_module_contact : $l_static / $l_query";
$fieldnames['contact_lastname'] = $l_lastname;
$fieldnames['contact_firstname'] = $l_firstname;
$fieldnames['function_label'] = $l_function;
$fieldnames['contact_title'] = $l_title;
$fieldnames['kind_minilabel'] = $l_status;
$fieldnames['kind_header'] = $l_letter;
$fieldnames['kind_lang'] = $l_lang;
$fieldnames['company_name'] = $l_company;
$fieldnames['address'] = $l_address;
$fieldnames['service'] = $l_service;
$fieldnames['street'] = $l_street;
$fieldnames['zipcode'] = $l_postcode;
$fieldnames['town'] = $l_town;
$fieldnames['expresspostal'] = $l_expresspostal;
$fieldnames['country_name'] = $l_country;
$fieldnames['contact_phone'] = $l_phone;
$fieldnames['contact_fax'] = $l_fax;
$fieldnames['contact_homephone'] = $l_hphone;
$fieldnames['contact_mobilephone'] = $l_mphone;
$fieldnames['contact_email'] = $l_email;
$fieldnames['subscription_quantity'] = $l_quantity;
$fieldnames['publication_title'] = $l_publication.' '.$l_title;
$fieldnames['publication_lang'] = $l_publication.' '.$l_lang;

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
  global $path, $params,  $clist_mode_normal, $clist_mode_expert;
  global $l_normal, $l_expert;

  // We set an indicator to tell if the contact has an address
  if ( ($OD->data_set->f('contact_street') != '')
         || ($OD->data_set->f('contact_zipcode') != '')
         || ($OD->data_set->f('contact_town') != '')
         || (trim($OD->data_set->f('contact_country_iso3166')) != '')
         || ($OD->data_set->f('contact_expresspostal') != '') ) {
    $contact_has_address = true;
  } else {
    $contact_has_address = false;
  }

  if (($fieldname == 'list_name')) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_get_id('.$OD->data_set->f('list_id').",'".addslashes($OD->data_set->f('list_name'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $ext_url = $params['ext_url'];
      $res['url'] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f('list_id').");";
    } elseif($link_ok) {
      $res['url'] = "list_index.php?action=detailconsult&amp;list_id=".$OD->data_set->f('list_id');
    }    
  }
    
  else if ($fieldname == 'function_label') {
    $res['name'] = $OD->data_set->f('contactfunction_label');
  }

  else if ($fieldname == 'list_nb_contact') {
    $res['name'] = $OD->data_set->f('list_static_nb'). ' / ' .$OD->data_set->f('list_query_nb');
    $res['align'] = 'center';
  }

  else if ($fieldname == 'list_info_publication') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'list_mode') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname) == "$clist_mode_expert") {
      $res['name'] = "$l_expert";
    } else {
      $res['name'] = "$l_normal";
    }
  }

  // For contacts urls (link):
  else if ($fieldname == 'contact_lastname') {
    $res['url'] = "$path/contact/contact_index.php?action=consult&amp;id=".$OD->data_set->f('contact_id');
  }

  else if ($fieldname == 'company_name') {
    if ($OD->data_set->f('company_id') > 0) {
      $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('company_id');
    } else {
      $res['name'] .= $OD->data_set->f('contact_company');
    }
  }

  else if ($fieldname == 'address') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_service') != '') {
	$res['name'] .= $OD->data_set->f('contact_service')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_service') . ' ';
      }
      if ($OD->data_set->f('contact_street') != '') {
	$res['name'] .= $OD->data_set->f('contact_street')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_street') . ' ';
      }
      if ($OD->data_set->f('contact_zipcode') != '') {
	$res['name'] .= $OD->data_set->f('contact_zipcode')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_zipcode') . ' ';
      }
      if ($OD->data_set->f('contact_town') != '') {
	$res['name'] .= $OD->data_set->f('contact_town')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_town') . ' ';
      }
      if ($OD->data_set->f('contact_expresspostal') != '') {
	$res['name'] .= $OD->data_set->f('contact_expresspostal')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_expresspostal') . ' ';
      }
      if ($OD->data_set->f('contact_country') != '') {
	$res['name'] .= $OD->data_set->f('contact_country')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('contact_country');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_street') != '') {
	$res['name'] .= $OD->data_set->f('company_street')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('company_street') . ' ';
      }
      if ($OD->data_set->f('company_zipcode') != '') {
	$res['name'] .= $OD->data_set->f('company_zipcode')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('company_zipcode') . ' ';
      }
      if ($OD->data_set->f('company_town') != '') {
	$res['name'] .= $OD->data_set->f('company_town')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('company_town') . ' ';
      }
      if ($OD->data_set->f('company_expresspostal') != '') {
	$res['name'] .= $OD->data_set->f('company_expresspostal')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('company_expresspostal') . ' ';
      }
      if ($OD->data_set->f('company_country') != '') {
	$res['name'] .= $OD->data_set->f('company_country')."<br />\n";
	$res['txt_name'] .= $OD->data_set->f('company_country');
      }
    }
  }

  else if ($fieldname == 'service') {
    $res['name'].= $OD->data_set->f('contact_service');
  }

  else if ($fieldname == 'street') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_street') != '') {
	$res['name'] .= $OD->data_set->f('contact_street');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_street') != '') {
	$res['name'] .= $OD->data_set->f('company_street');
      }
    }
  }

  else if ($fieldname == 'zipcode') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_zipcode') != '') {
	$res['name'] .= $OD->data_set->f('contact_zipcode');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_zipcode') != '') {
	$res['name'] .= $OD->data_set->f('company_zipcode');
      }
    }
  }

  else if ($fieldname == 'town') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_town') != '') {
	$res['name'] .= $OD->data_set->f('contact_town');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_town') != '') {
	$res['name'] .= $OD->data_set->f('company_town');
      }
    }
  }

  else if ($fieldname == 'expresspostal') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_expresspostal') != '') {
	$res['name'] .= $OD->data_set->f('contact_expresspostal');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_expresspostal') != '') {
	$res['name'] .= $OD->data_set->f('company_expresspostal');
      }
    }
  }

  else if ($fieldname == 'country_name') {
    // If a contact address is present we get it
    if ( $contact_has_address ) {
      if ($OD->data_set->f('contact_country') != '') {
	$res['name'] .= $OD->data_set->f('contact_country');
      }
      // else we get the contact Company address
    } else {
      if ($OD->data_set->f('company_country') != '') {
	$res['name'] .= $OD->data_set->f('company_country');
      }
    }
  }

  else if ($fieldname == 'contact_email') {
    $res['name'] .= $OD->data_set->f('email_address');
  }

  else if ($fieldname == 'contact_phone') {
    // If a contact phone is present we get it
    if ($OD->data_set->f('contact_phone') != '') {
      $res['name'] .= $OD->data_set->f('contact_phone');
      // else we get the contact Company phone
    } else {
      if ($OD->data_set->f('company_phone') != '') {
	$res['name'] .= '('.$OD->data_set->f('company_phone').')';
      }
    }
  }

  else if ($fieldname == 'contact_fax') {
    // If a contact fax is present we get it
    if ($OD->data_set->f('contact_fax') != '') {
      $res['name'] .= $OD->data_set->f('contact_fax');
      // else we get the contact Company fax
    } else {
      if ($OD->data_set->f('company_fax') != '') {
	$res['name'] .= '('.$OD->data_set->f('company_fax').')';
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
function dis_list_search_form($list='') {

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
    $ext_action = $list['ext_action'];
    $ext_url = $list['ext_url'];
    $ext_id = $list['ext_id'];
    $ext_target = $list['ext_target'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
      <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
      <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
      <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />";
  }
  
  // --- Var preparation ------------------------------------------------------

  $name = stripslashes($list['name']);
  $email = stripslashes($list['email']);
  $contact = stripslashes($list['contact']);
  $mark = $list['marketing_manager'];

  // Marketing manager select
  $sel_market = "<select id=\"sel_marketing_manager\" name=\"sel_marketing_manager\">
    <option value=\"$c_all\">$l_all</option>";
  while ($usr_q->next_record()) {
    $u_id = $usr_q->f('userobm_id');
    $sel_market .= "\n<option value=\"$u_id\"";
    if ($u_id == $mark) { $sel_market .= " selected=\"selected\""; }
    $sel_market .= '>'. $usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname') . "</option>\n";
  }
  $sel_market .= '</select>';

  // --- HTML Page display ----------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\"
    action=\"".url_prepare('list_index.php')."\">

    <label>$l_name<br />
      <input type=\"text\" name=\"tf_name\" size=\"16\" value=\"$name\" />
    </label>
    <label>$l_list_contact<br />
      <input type=\"text\" name=\"tf_contact\" size=\"16\" value=\"$contact\" />
    </label>
    <label>$l_email<br />
      <input type=\"text\" name=\"tf_email\" size=\"20\" value=\"$email\" />
    </label>
    <label>$l_market<br />
      $sel_market
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\" />
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
  global $l_found, $l_no_found;
  global $display, $obm;

  $prefs = get_display_pref($obm['uid'], 'list');
  $obm_q = run_query_list_search($list);
  $nb_list = $obm_q->num_rows_total();
  if ($nb_list == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_list $l_found");
    $block = html_list_search_list($obm_q, $prefs, $list, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the List search result
// Parameters : 
//   - $obm_q    : list of the lists to display 
//   - $prefs    : fields that have to be displayed
//   - $list[]   : list search criteria
//     keys used : name, contact, ext_id, ext_url, ext_action
//   - $popup    : if true, doesn't display links and display checkbox
///////////////////////////////////////////////////////////////////////////////
function html_list_search_list($obm_q, $prefs, $list, $popup) {
  global $l_add, $l_close;

  if ($popup) {
    $ext_action = $list['ext_action'];
    $ext_url = $list['ext_url'];
    $ext_id = $list['ext_id'];
    $ext_target = $list['ext_target'];
    $ext_widget = $list['ext_widget'];
    $ext_widget_text = $list['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }
  $name = urlencode(stripslashes($list['name']));
  $email = urlencode(stripslashes($list['email']));
  $contact = urlencode(stripslashes($list['contact']));
  $mark = $list['marketing_manager'];

  $url = url_prepare("list_index.php?action=search&amp;tf_name=$name&amp;tf_contact=$contact&amp;tf_email=$email&amp;sel_marketing_manager=$mark$url_ext");
  
  $dis_list = new OBM_DISPLAY('DATA', $prefs, 'list');
  if ($popup) {
    $dis_list->display_link = false;
    if ($ext_url != '') {
      $dis_list->display_ext = 'get_id_url';
    } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $dis_list->display_ext = 'get_id';
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  
  $dis_list->data_set = $obm_q;
  $dis_list->data_header = 'both';
  $dis_list->data_url = $url;
  $block .= $dis_list->display('dis_data_list');
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display List Consultation
// Parameters:
//   - $list[] : list parameters
///////////////////////////////////////////////////////////////////////////////
function dis_list_consult($list) {
  global $obm, $l_err_reference;

  $id = $list['list_id'];

  if ($id > 0) {
    $list_q = run_query_list_detail($id);
    $prefs_con = get_display_pref($obm['uid'], 'list_contact');
    $con_q = run_query_list_contacts_list($list, 'list_contact');
    $block = html_list_consult($list_q, $prefs_con, $con_q);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }

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
  global $l_no,$l_yes;
  global $display, $perm, $module, $cright_write, $ctu_sql_limit, $cdg_sql;
  global $l_contact, $l_no_static_contact, $l_static_con_found;
  global $l_no_dyn_contact, $l_dyn_con_found, $l_company, $l_invalid_query;
  global $l_del_contact_sel, $nb_contact_added, $nb_contact_deleted;
  global $l_mailing, $l_contact_archive, $l_info_pub;
  global $l_query, $l_expert, $l_normal;
  global $cgp_show, $clist_mode_normal, $clist_mode_expert;

  $id = $list_q->f('list_id');
  $usercreate = $list_q->f('list_usercreate');
  $userupdate = $list_q->f('list_userupdate');
  $timecreate = $list_q->f('timecreate');
  $timeupdate = $list_q->f('timeupdate');
  $dis_mailing = ($list_q->f('list_mailing_ok') == 0 ? $l_no : $l_yes);
  $dis_con_arch = ($list_q->f('list_contact_archive') == 0 ? $l_no : $l_yes);
  $dis_info_pub = ($list_q->f('list_info_publication') == 0 ? $l_no : $l_yes);
  $name = $list_q->f('list_name');
  $subject = $list_q->f('list_subject');
  $email = $list_q->f('list_email');
  $mode = $list_q->f('list_mode');
  $static_nb = $list_q->f('list_static_nb');
  $structure = $list_q->f('list_structure');

  if ($cgp_show['module']['publication']) {
    $dis_pub = "
    <tr>
      <th>$l_info_pub</th><td>$dis_info_pub</td>
    </tr>";
  }

  if ($structure != '') {
    $criteria = unserialize($structure);
  } else {
    $criteria = '';
  }

  if ($mode == $clist_mode_normal) {
    $lmode = $l_normal;
    if ($criteria != '') {
      $dynlist = make_list_query_from_db($list_q);
      $query = $dynlist['query'];
    }
  } else {
    $lmode = $l_expert;
    $query = stripslashes($list_q->f('list_query'));
    $dynlist['query'] = $query;
  }

  if ($dynlist['query'] != '') {
    $query_nb = get_list_query_num_rows($dynlist, $list_q);
    if ($query_nb >= 0) {
      run_query_list_update_num_rows($id, $query_nb);
    } else {
      $query_nb = $l_invalid_query;
    }

    if ($cgp_show['module']['company']) {
      // Company query nb
      $comp_query_nb = get_list_dynamic_company_nb($dynlist);
    }

    // Contact query nb (= query_nb if no supplement info needed as publi)
    if ($dis_info_pub == $l_yes) {
      $con_query_nb = get_list_dynamic_contact_nb($dynlist);
    } else {
      $con_query_nb = $query_nb;
    }
  } else {
    $query_nb = '0';
    $comp_query_nb = '0';
  }

  if ($cgp_show['module']['company']) {
    $comp_static_nb = get_list_static_company_nb($id);
    $dis_comp_stat = "
  <tr>
    <th># $l_company : $l_static / $l_query</th>
    <td>$comp_static_nb / $comp_query_nb</td>
  </tr>";
  }

  // If expert mode, we display the query (empty or manual)
  if ($mode == $clist_mode_expert) {
    $d_query = "<div class=\"O\" style=\"max-height:8em;_height:8em;\" >".nl2br($query)."</div>";
    $dis_query = "<tr>
      <th>$l_query</th>
      <td>$d_query</td>
    </tr>";
  }


  $display['detailInfo'] = display_record_info($list_q);
  $display['title'] = $name;

  $block = "
<div class=\"detail extra\">
  <h1>$l_list</h1>
  <table>
  <tr>
    <th>$l_name</th><td>$name</td>
  </tr><tr>
    <th>$l_mailing</th><td>$dis_mailing</td>
  </tr><tr>
    <th>$l_contact_archive</th><td>$dis_con_arch</td>
  </tr>
    $dis_pub
  <tr>
    <th>$l_subject</th><td>$subject</td>
  </tr><tr>
    <th>$l_email</th><td>$email</td>
  </tr><tr>
    <th>$l_query</th><td>$lmode</td>
  </tr>
  <tr>
    <th># $l_contact : $l_static / $l_query</th>
    <td>$static_nb / $con_query_nb</td>
  </tr>
  $dis_comp_stat
    $dis_query
  </table>
</div>";

  // Static Contacts
  $nb_con = $con_q->num_rows_total();
  if ($nb_con == 0) {
    $message = $l_no_static_contact;
  } else {
    $message = "$nb_con $l_static_con_found";
  }

  $block .= display_info_msg($message);

  if ($nb_con != 0) {

    $url=url_prepare("list_index.php?action=detailconsult&amp;list_id=$id");
    
    $dis_infos = new OBM_DISPLAY('DATA', $prefs_con, 'list', 'list_contact', 'sta');
    $dis_infos->data_set = $con_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_header = 'both';

    if ($perm->check_right($module, $cright_write)) {
      $dis_infos->data_cb_text = 'X';
      $dis_infos->data_idfield = 'contact_id';
      $dis_infos->data_cb_name = 'cb_con';
      $dis_infos->data_cb_field = '';
      $dis_infos->data_form_head = "
      <form method=\"get\" action=\"list_index.php\">";

      $dis_infos->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_del_contact_sel\" />
        <input type=\"hidden\" name=\"list_id\" value=\"$id\" />
        <input type=\"hidden\" name=\"action\" value=\"contact_del\" />
        </p>
      </p>
      </form>";
    }

    $block .= $dis_infos->display('dis_data_list');

  }

  // Dynamic Contacts
  if ($query_nb != 0) {
    $message = "$query_nb $l_dyn_con_found";
    $block .= display_info_msg($message);

    $url=url_prepare("list_index.php?action=detailconsult&amp;list_id=$id");
    
    $conq_q = new DB_OBM;
    $db_type = $obm_q->type;
    $limit = sql_limit($db_type, '', '', 'dyn');

    if ($ctu_sql_limit) {
      $conq_q->set_num_rows_total($query_nb);
    }

    $query_limit = "$query $limit";
    display_debug_msg($query_limit, $cdg_sql, 'html_list_consult(dynamic)');
    $conq_q->query("$query_limit");
    $dis_infos = new OBM_DISPLAY('DATA', $prefs_con, 'list', 'list_contact', 'dyn');
    $dis_infos->data_set = $conq_q;
    $dis_infos->data_url = $url;
    $dis_infos->data_order = false;
    $dis_infos->data_header = 'both';
    $block .= $dis_infos->display('dis_data_list');
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

  $url = url_prepare('list_index.php');
  $block = "<form method=\"post\" name=\"f_entity\" action=\"$url\"
    onSubmit=\"if (check_name_list(this)) return true; else return false;\">";

  $block .= html_list_form($action, $list_q, $list);

  if (($action == 'detailupdate') || ($action == 'update')) {
     $dis_but .= "
      <input type=\"hidden\" name=\"action\" value=\"update\"/>
      <input type=\"submit\" value=\"$l_update\" />";

   } else {
     $id_dup = $list['id_duplicated'];
     if ($id_dup > 0) {
       $dis_but .= "<input type=\"hidden\" name=\"id_duplicated\" value=\"$id_dup\" />";
     }
     $dis_but .= "<input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block .= "<fieldset class=\"buttons\">
    $dis_but 
    </fieldset>
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
  global $l_query, $l_list, $l_private, $l_header_new_f;
  global $cgp_show,$clist_mode_normal, $clist_mode_expert, $l_expert,$l_normal;
  global $action, $display, $obm;

  // if update mode and first time, values are taken from db
  if (($action == 'detailupdate') || ($action == 'detailduplicate')) {
    $id = $list_q->f('list_id');
    $usercreate = $list_q->f('list_usercreate');
    $name = $list_q->f('list_name');
    $subject = $list_q->f('list_subject');
    $email = $list_q->f('list_email');
    $mode = $list_q->f('list_mode');
    $structure = $list_q->f('list_structure');
    $criteria = unserialize($structure);
    $query = stripslashes($list_q->f('list_query'));
    $priv = ($list_q->f('privacy') == 0 ? '0' : '1');
    $mailing_ok = ($list_q->f('list_mailing_ok') == 0 ? '0' : '1');
    $contact_arch = ($list_q->f('list_contact_archive') == 0 ? '0' : '1');
    $info_pub = ($list_q->f('list_info_publication') == 0 ? '0' : '1');
    $dis_title = $name;
  }
  
  // If parameters have been given, they supercede the default action value
  if (isset($list['list_id'])) { $id = $list['list_id']; }
  if (isset($list['privacy'])) { $priv = $list['privacy']; }
  if (isset($list['name'])) { $name = stripslashes($list['name']); }
  if (isset($list['subject'])) { $subject = stripslashes($list['subject']); }
  if (isset($list['email'])) { $email = stripslashes($list['email']); }
  if (isset($list['mode'])) { $mode = $list['mode']; }
  if (isset($list['query'])) { $query = stripslashes($list['query']); }
  if (isset($list['mailing_ok'])) { $mailing_ok = $list['mailing_ok']; }
  if (isset($list['contact_archive'])) { $contact_arch = $list['contact_archive']; }
  if (isset($list['info_pub'])) { $info_pub = $list['info_pub']; }
  if (isset($list['criteria'])) { $criteria = $list['criteria']; }
	
  if ( ($action == 'new') || ($action == 'insert')
       || ($action == 'detailduplicate')
       || ( (($action == 'detailupdate') || ($action == 'update')) && 
	    ($usercreate == $obm['uid']) ) ) {
    $dis_priv = "
      <th>$l_private</th>
      <td><input name=\"cba_privacy\" type=\"checkbox\" value=\"1\" ";
    if ($priv == 1) $dis_priv .= "checked = \"checked\"";
    $dis_priv .= " /></td>
  </tr><tr>";
  }

  if ($mailing_ok == 1) $dis_mailing .= "checked = \"checked\""; 
  if ($contact_arch == 1) $dis_con_arch .= "checked = \"checked\""; 
  if ($info_pub == 1) $dis_info_pub .= "checked = \"checked\""; 

  if ($cgp_show['module']['publication']) {
    $dis_pub = "
    <tr>
      <th>$l_info_pub</th>
      <td><input type=\"checkbox\" $dis_info_pub name=\"cba_info_pub\" value=\"1\" /></td>
    </tr>";
  }


  $url = url_prepare('list_index.php');
  $display['title'] = $dis_title;

  // If criteria are set, we display the criteria form, else the query
  if ($mode == $clist_mode_expert) {
    $expert_checked = " checked=\"checked\" ";
    $style_query = '';
  } else {
    $normal_checked = " checked=\"checked\" ";
    $style_query = "style=\"display:none;\"";
  }
  $dis_query = html_list_normal_criteria_form($action, $list_q, $list);

  $block = "
  <fieldset class=\"detail extra\">
  <legend>$l_list</legend>
  <table>
  <tr>
    <th>$l_name</th>
    <td>
      <input type=\"text\" name=\"tf_name\" size=\"32\" maxlength=\"32\" value=\"$name\" /></td>
  </tr>
  <tr>
  $dis_priv
    <th>$l_subject</th>
    <td>
      <input name=\"tf_subject\" size=\"30\" value=\"$subject\" /></td>
  </tr>
  <tr>
    <th>$l_email</th>
    <td>
      <input name=\"tf_email\" size=\"30\" value=\"$email\" /></td>
  </tr>
  <tr>
    <th>$l_mailing</th>
    <td>
      <input type=\"checkbox\" $dis_mailing name=\"cba_mailing_ok\" value=\"1\" /></td>
  </tr>  
  <tr>
    <th>$l_contact_archive</th>
    <td>
      <input type=\"checkbox\" $dis_con_arch name=\"cba_contact_archive\" value=\"1\" /></td>
  </tr>  
  $dis_pub
  </table>
  </fieldset>
  
  <fieldset class=\"detail extra\">
  <legend>$l_query</legend>  
    <table>
    <tr>
    <th>
    $l_expert
    </th>
    <td>
    <input type=\"radio\" $expert_checked name=\"rd_mode\" value=\"$clist_mode_expert\" onclick=\"return show_query_field('','none',this);\" />
    </td>
    </tr>
    <tr>
    <th>
    $l_normal
    </th>
    <td>
    <input type=\"radio\" $normal_checked name=\"rd_mode\" value=\"$clist_mode_normal\" onclick=\"return show_query_field('none','',this);\" />
    </td>
    </tr>
  </table>
  <div class=\"O FW\">
  <textarea id=\"ta_query\" $style_query name=\"ta_query\" cols=\"68\" rows=\"16\">$query</textarea>
  $dis_query
  </div>
  </fieldset>";

  if (($action == 'detailupdate') || ($action == 'update')) {
    $block .= "<input type=\"hidden\" name=\"list_id\" value=\"$id\" />";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Graphical (normal) List criteria Form
// Parameters :
//   - $action    : action called
//   - $list_q    : DBO : information about the list (null for new list)
//   - $list[]    : default or transmitted values
//     keys used  : name, subject, usercreate
///////////////////////////////////////////////////////////////////////////////
function html_list_normal_criteria_form($action, $list_q, $list) {
  global $cgp_user,$ico_update,$ico_delete;
  global $l_add_criterion, $l_criterion;
  global $l_and, $l_or, $l_not, $l_empty, $l_exclude;
  global $l_company, $l_country,$l_postcode,$l_town;
  global $l_contact,$l_lastname,$l_firstname;
  global $l_timeafter,$l_timebefore,$l_market, $l_datasource, $l_function;
  global $l_publication,$l_title,$l_lang,$l_year, $l_after, $l_before;
  global $l_update,$l_delete,$l_recept,$l_renew;
  global $clist_mode_expert, $clist_mode_normal;
  
  // if update mode and first time, values are taken from db
  if (($action == 'detailupdate') || ($action == 'detailduplicate')) {
    $id = $list_q->f('list_id');
    $mode = $list_q->f('list_mode');
    $query = stripslashes($list_q->f('list_query'));
    $criteria = unserialize($list_q->f('list_structure'));
    $boolean_criterion = $criteria['logical']['NOT'];
    $logical_criterion = $criteria['logical']['AND'];      
    $criteria = $criteria['modules'];
  }

  // If parameters have been given, they supercede the default action value
  if (isset($list['list_id'])) { $id = $list['list_id']; }
  if (isset($list['mode'])) { $mode = $list['mode']; }
  if (isset($list['query'])) { $query = stripslashes($list['query']); }
  if (isset($list['criteria'])) { 
    $criteria = $list['criteria'];
    if (!is_array($criteria)) {
      $criteria = unserialize($criteria);
    }
    $boolean_criterion = $criteria['logical']['NOT'];
    $logical_criterion = $criteria['logical']['AND'];      
    $criteria = $criteria['modules']; 
  }

  $field_kind = array(
      'company_name' => 'tf',
      'company_country_iso3166' => 'sel',
      'company_timeafter' => 'tf',
      'company_zipcode' => 'tf',
      'company_marketingmanager_id' => 'sel',
      'company_timebefore' => 'tf',
      'company_town' => 'tf',
      'company_datasource_id' => 'sel',
      'contact_firstname' => 'tf',
      'contact_country_iso3166' => 'sel',
      'contact_timeafter' => 'tf',
      'contact_lastname' => 'tf',
      'contact_marketingmanager_id' => 'sel',
      'contact_timebefore' => 'tf',
      'contact_datasource_id' => 'sel',
      'contact_town' => 'tf',
      'contact_zipcode' => 'tf',
      'contact_function_id' => 'sel',
      'kind_lang' => 'sel',
      'subscription_publication_id' => 'sel',
      'publication_lang' => 'tf',
      'publication_year' => 'tf',
      'subscription_reception_id' => 'sel',
      'subscription_renewal' => 'tf',
      'subscription_timeafter' => 'tf',
      'subscription_timebefore' => 'tf'
  );

  // User data categories handling
  if (is_array($cgp_user['company']['category'])) {
    foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
      $cat_id = "${cat_name}";
      $cat_tree = "${cat_name}_tree";
      $field_kind[$cat_id] = 'of_sel';
      $cat_label = "l_$cat_name";
      global $$cat_label;
      $fields_label[$cat_id] = "$l_company : ${$cat_label}";
    }
  }
  if (is_array($cgp_user['contact']['category'])) {
    foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
      $cat_id = "${cat_name}";
      $cat_tree = "${cat_name}_tree";
      $field_kind[$cat_id] = 'of_sel';
      $cat_label = "l_$cat_name";
      global $$cat_label;
      $fields_label[$cat_id] = "$l_contact : ${$cat_label}";
    }
  }

  $fields_label['company_name'] = $l_company;
  $fields_label['company_country_iso3166'] = "$l_company: $l_country";
  $fields_label['company_zipcode'] = "$l_company: $l_postcode";
  $fields_label['company_town'] = "$l_company: $l_town";
  $fields_label['company_timeafter'] = "$l_company: $l_timeafter";
  $fields_label['company_timebefore'] = "$l_company: $l_timebefore";
  $fields_label['company_marketingmanager_id'] = "$l_company: $l_market";
  $fields_label['company_datasource_id'] = "$l_company: $l_datasource";
  $fields_label['contact_lastname'] = "$l_contact: $l_lastname";
  $fields_label['contact_firstname'] = "$l_contact: $l_firstname";
  $fields_label['contact_country_iso3166']= "$l_contact: $l_country";
  $fields_label['contact_zipcode'] = "$l_contact: $l_postcode";
  $fields_label['contact_town'] = "$l_contact: $l_town";
  $fields_label['contact_timeafter'] = "$l_contact: $l_timeafter";
  $fields_label['contact_timebefore'] = "$l_contact: $l_timebefore";
  $fields_label['contact_marketingmanager_id'] = "$l_contact: $l_market";
  $fields_label['contact_datasource_id'] = "$l_contact: $l_datasourceid";
  $fields_label['kind_lang'] = "$l_contact: $l_lang";
  $fields_label['contact_function_id'] = "$l_contact: $l_function";
  $fields_label['subscription_publication_id'] = "$l_publication: $l_title";
  $fields_label['publication_lang'] = "$l_publication: $l_lang";
  $fields_label['publication_year'] = "$l_publication: $l_year";
  $fields_label['subscription_reception_id'] = "$l_publication: $l_recept";
  $fields_label['subscription_renewal'] = "$l_publication: $l_renew";
  $fields_label['subscription_timeafter'] = "$l_publication: $l_after";
  $fields_label['subscription_timebefore'] = "$l_publication: $l_before";

  if (is_array($criteria)) {
    $cty_q = run_query_global_country_for_lang();
    $dsr_q = run_query_global_datasource();
    $usr_q = run_query_userobm_active();
    $fct_q = run_query_global_contactfunction();
    $rec_q = run_query_list_subscriptionreception();
    $lag_q = run_query_list_language();
    $pub_q = run_query_list_publication();

    $field_data = array(
      'company_country_iso3166' => array($cty_q,'country_iso3166','country_name'),
      'company_marketingmanager_id' => array($usr_q,'userobm_id','userobm_lastname'),
      'company_datasource_id' => array($dsr_q,'datasource_id','datasource_name'),
      'contact_country_iso3166' => array($cty_q,'country_iso3166','country_name'),
      'contact_marketingmanager_id' => array($usr_q,'userobm_id','userobm_lastname'),
      'contact_datasource_id' => array($dsr_q,'datasource_id','datasource_name'),
      'contact_function_id' => array($fct_q,'contactfunction_id','contactfunction_label'),
      'kind_lang' => array($lag_q,'kind_lang','kind_lang'),
      'subscription_publication_id' => array($pub_q,'publication_id','publication_title'),
      'subscription_reception_id' => array($rec_q,'subscriptionreception_id','subscriptionreception_label')
    );

    // User data categories handling
    if (is_array($cgp_user['company']['category'])) {
      foreach($cgp_user['company']['category'] as $cat_name => $one_cat) {
	$cat_id = "${cat_name}";
	$cat_tree = "${cat_name}_tree";
        $cats = of_category_user_get_ordered($cat_name);
	$field_data[$cat_id] = array('', '', '', $cats, 'id');
      }
    }
    if (is_array($cgp_user['contact']['category'])) {
      foreach($cgp_user['contact']['category'] as $cat_name => $one_cat) {
	$cat_id = "${cat_name}";
	$cat_tree = "${cat_name}_tree";
        $cats = of_category_user_get_ordered($cat_name);
	$field_data[$cat_id] = array('', '', '', $cats, 'id');
      }
    }

    foreach($criteria as $module) {
      foreach($module as $field => $criterion) {
	foreach($criterion as $line => $value) {
	  if ($value != '') {
	    $input = '';

	    // text field handling
	    if ($field_kind[$field] == 'tf') {
	      $input = "<input type=\"text\" name=\"tf_".$field."[]\" readonly=\"readonly\" value=\"$value\" />";

	    // Select field handling
	    } elseif ($field_kind[$field] == 'sel') {

	      $vals = explode(',',$value);
	      $size = count($vals);
	      $data = $field_data[$field];
	      $option = '';
	      // $txt : select title (to have full info in roll over)
	      $txt = '';

	      // if data given as array
	      if (is_array($data[3])) {
		$datas = $data[3];
		// category column (id by default, could be id)
		$column = ($data[4] != '' ? $data[4] : 'id');
		foreach ($datas as $one_data) {
		  foreach($vals as $key) {
		    if ("$key" == $one_data[$column]) {
		      if ($option == '') {
			$option = "<option selected=\"selected\" value=\"$value\" selected=\"selected\">".$one_data['label']."($size)</option>";
		      } else {
			$txt .= ',';
		      }
		      $txt .= $one_data['label'];
		    }
		  }
		}

		// data given as DBO
	      } else {
		$data_q = $data[0];
		while ($data_q->next_record()) {
		  foreach($vals as $key) {
		    if ("$key" == $data_q->f($data[1])) {
		      if ($option == '') {
			$option = "<option selected=\"selected\" value=\"$value\" selected=\"selected\">".$data_q->f($data[2])."($size)</option>";
		      } else {
			$txt .= ',';
		      }
		      $txt .= $data_q->f($data[2]);
		    }
		  }
		}
		$data_q->seek(0);
	      }
	      $input = "<select title=\"$txt\" name=\"sel_".$field."[]\" readonly=\"true\" onFocus=\"this.blur();\">$option</select>";

	    } elseif ($field_kind[$field] == 'of_sel') {
	      // special of_select (handle a checkbox first)

	      $vals = explode(',',$value);
	      $size = count($vals);
	      $data = $field_data[$field];
	      $option = '';
	      // $txt : select title (to have full info in roll over)
	      $txt = '';

	      // if data given as array (always with of_select)
	      if (is_array($data[3])) {
		$datas = $data[3];
		// category column (id by default, could be id)
		$column = ($data[4] != '' ? $data[4] : 'id');
		foreach ($datas as $one_data) {
		  foreach($vals as $key) {
		    if ("$key" == $one_data[$column]) {
		      if ($option == '') {
			$option = "<option selected=\"selected\" value=\"$value\" selected=\"selected\">".$one_data['label']."($size)</option>";
		      } else {
			$txt .= ',';
		      }
		      $txt .= $one_data['label'];
		    }
		  }
		}
	      }

	      // We add the cb input first
	      $input = "<input type=\"text\" name=\"sel_".$field."_tree[]\" readonly=\"readonly\" value=\"".$module["${field}_tree"][$line]."\" />";
	      $input .= "<select title=\"$txt\" name=\"sel_".$field."[]\" readonly=\"true\" onFocus=\"this.blur();\">$option</select>";
	    }

	    // Exclude fields that have no value (eg: category_tree)
	    if ($input != '') {
	      $structure[$line] .= "<td>$input</td>";
	      $labels[$line] .= '<th>'.$fields_label[$field].'</th>';
	    }
	  } else {
	    // No value (no data), we display hidden input

	    // handle special of_select case : prefix is 'sel', add checkbox
	    if ($field_kind[$field] == 'of_sel') {
	      $prefix = 'sel';
	      $hid_cell[$line] .= "<input type=\"hidden\" name=\"sel_${field}_tree[]\" value=\"\" />";
	    } else {
	      $prefix = $field_kind[$field];
	    }

	    $structure[$line] .= '';
	    $hid_cell[$line] .= "<input type=\"hidden\" name=\"${prefix}_${field}[]\" value=\"\" />";
	  }
	}
      }
    }

    $del_cell = "<td><a href=\"javascript:void(0);\" onclick=\"del_row(this.parentNode.parentNode);return false;\">
    <img src=\"$ico_delete\" alt=\"Delete\" /></a></td>";
    $upd_cell = "<td><a href=\"javascript:void(0);\" onclick=\"open_window(this.parentNode.parentNode);return false;\">
    <img src=\"$ico_update\" alt=\"Update\" /></a></td>"; 

    if (is_array($structure)) {
      foreach($structure as $key=>$line) {
	if ($boolean_criterion[$key] == "NOT") {
	  $not = "selected =\"true\"";
	  $exclude = '';
	  $empty = '';
	} else if ($boolean_criterion[$key] == 'Exclude') {
	  $exclude = "selected =\"true\"";
	  $not = '';
	  $empty = '';
	} else {
	  $empty = "selected =\"true\"";
	  $exclude = '';
	  $not = '';
	}
	$logical_criterion;      
	$dis_no_boolean = "<td><select name=\"sel_log_not[]\">
        <option value=\"\" $empty >$l_empty</option>
        <option value=\"NOT\" $not>$l_not</option>
        <option value=\"Exclude\" $exclude>$l_exclude</option>
      </select></td>";  
      
       $dis_criteria .= "
	<tr><td class=\"head\" colspan=\"5\"></td>".$labels[$key].'</tr>';
	if ($logical_criterion[$key] == 'AND') {
	  $and = "selected =\"true\"";
	  $or = '';
	} else {
	  $or = "selected =\"true\"";
	  $and = '';
	}	
	$dis_logical = "<td><select name=\"sel_log_and[]\"><option value=\"OR\" $or>$l_or</option><option value=\"AND\" $and>$l_and</option></select></td>";
	$dis_criteria .= "<tr><td>".$hid_cell[$key]."</td>$del_cell $upd_cell $dis_logical $dis_no_boolean $line</tr>";      
      }
    }
  }

  $url2 = "list_index.php?action=new_criterion&amp;popup=1&amp;ext_element=forms[0].elements[5]";

  if ($mode == $clist_mode_expert) {
    $display = "style=\"display:none\"";
  }
  
  $block = "
  <table id=\"list_criterion\" class=\"spreadSheet\">
  <tbody>
   $dis_criteria
  </tbody>
  </table>
  <div id=\"di_query\" $display class=\"buttons\">
  <input type=\"button\" value=\"$l_add_criterion\" onclick=\"popup('$url2',''); return false;\"/>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Form to add a Criterion
// Parameters:
//   - $list[] : default or transmitted values
///////////////////////////////////////////////////////////////////////////////
function dis_list_add_criterion_form($list) {
  global $cgp_show, $l_list_display, $l_contact_display,$l_add_criterion;

  $popup = $list['popup'];
  if (isset($list['row_index'])) {
    $row_index = $list['row_index'];
  } else {
    $row_index =-1;
  }
  $cty_q = run_query_global_country_for_lang();
  $usr_q = run_query_userobm_active();
  $dsr_q = run_query_global_datasource();
  $fct_q = run_query_global_contactfunction();
  $lag_q = run_query_list_language();
    
  $block .= "<form method=\"get\" name=\"f_new_criterion\" action=\"$url\" onSubmit=\"list_add_criteria_line(this,$row_index);window.close();return false;\">";

  if ($cgp_show['module']['company']) {
    $block .= html_list_company_criterion($cty_q,$usr_q,$dsr_q);
  }
  $block .= html_list_contact_criterion($cty_q,$usr_q,$dsr_q,$fct_q,$lag_q);

  if ($cgp_show['module']['publication']) {
    $rec_q = run_query_list_subscriptionreception();
    $pub_q = run_query_list_publication();
    $block .= html_list_publication_criterion($rec_q,$pub_q);
  }

  $block .= "
  <fieldset class=\"buttons\">
  <input type=\"submit\" value=\"$l_add_criterion\"/>
  </fieldset>

  <input type=\"hidden\" name=\"ext_element\" value=\"$ext_element\" />
</form>";

  if ($row_index != -1) {
    $block .= "
    <script type=\"text/javascript\">
      get_list_critria_from_query(window.document.f_new_criterion);
    </script>
";
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Form to add a Criterion
// Parameters:
//   - $list[]    : default or transmitted values
///////////////////////////////////////////////////////////////////////////////
function dis_list_add_criterion_userdata_form($entity) {
  global $cgp_user,  $ico_add, $l_all;
  global $ico_add, $ico_crow, $l_none;

  // User data categories handling
  if (is_array($cgp_user["$entity"]['category'])) {
    foreach($cgp_user["$entity"]['category'] as $cat_name => $one_cat) {
      global $$cat_label;
      $cats = array();
      $sel_name = "sel_${cat_name}";
      $url_cat = "$GLOBALS[path]/$entity/${entity}_index.php?action=ext_get_category_codes&amp;category=$cat_name&amp;popup=1&amp;ext_element=$sel_name";
      $block_cat .= of_select_dis_search_select($cats, "$cat_name", $url_cat, true);
    }
  }

  return $block_cat;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Company Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_list_company_criterion($cty_q,$usr_q,$dsr_q) {
  global $l_company,$l_country,$l_postcode,$l_town,$l_timebefore,$l_timeafter;
  global $l_all,$l_market,$l_datasource, $l_category;
  global $ico_mini_cal,$ico_add;

  $sel_cty .= "\n<option value=\"\">$l_all</option>";
  while ($cty_q->next_record()) {
    $sel_cty .= "\n<option value=\"".$cty_q->f('country_iso3166')."\">".$cty_q->f("country_name").'</option>';
  }
  if ($cty_q->nf()!=0) {
    $cty_q->seek(0);
  }
  $sel_usr .= "\n<option value=\"\">$l_all</option>";
  while ($usr_q->next_record()) {
    $sel_usr .= "\n<option value=\"".$usr_q->f('userobm_id')."\">".$usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname').'</option>';
  }
  if ($usr_q->nf()!=0)
    $usr_q->seek(0);
  $sel_dsr .= "\n<option value=\"\">$l_all</option>";
  while($dsr_q->next_record()) {
    $sel_dsr .= "\n<option value=\"".$dsr_q->f('datasource_id')."\">".$dsr_q->f('datasource_name').'</option>';
  }
  if ($dsr_q->nf()!=0)
    $dsr_q->seek(0);

  // Userdata
  $dis_cat = dis_list_add_criterion_userdata_form('company');

  $block .= "
  <fieldset class=\"detail infos\">
  <legend>$l_company</legend>
  <table>
  <tr>
    <th>$l_company</th>
    <td><input type=\"text\" size=\"17\" name=\"tf_company_name\" value=\"\" /></td>
  </tr>
  <tr>  
    <th>$l_country</th>
    <td><select name=\"sel_company_country_iso3166\">$sel_cty</select></td>
  </tr>
  <tr>   
    <th>$l_timeafter</th>
    <td><input type=\"text\" name=\"tf_company_timeafter\" value=\"$company_timeafter\" class=\"datePicker\"/></td>
  </tr>
  <tr>
    <th>$l_postcode</th>
    <td><input type=\"text\" size=\"10\" name=\"tf_company_zipcode\" value=\"\" /></td>
  </tr>
  <tr>
    <th>$l_market</th>
    <td><select name=\"sel_company_marketingmanager_id\">$sel_usr</select></td>
  </tr>
  <tr>
    <th>$l_timebefore</th>
      <td><input type=\"text\" name=\"tf_company_timebefore\" value=\"$company_timebefore\" class=\"datePicker\"/></td>
  </tr>
  
  <tr>
    <th>$l_town</th>
    <td><input type=\"text\" size=\"17\" name=\"tf_company_town\" value=\"\" /></td>
  </tr>
  <tr>
    <th>$l_category</th>
    <td>$dis_cat</td>
  </tr>
  <tr>
    <th>$l_datasource</th>
    <td><select name=\"sel_company_datasource_id\">$sel_dsr</select></td>
  </tr>
  </table>
  </fieldset>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_list_contact_criterion($cty_q,$usr_q,$dsr_q,$fct_q,$lag_q) {
  global $l_lastname,$l_firstname,$l_country,$l_postcode,$l_town,$l_contact;
  global $l_all,$l_timebefore,$l_timeafter,$l_market,$l_datasource;  
  global $l_function,$l_lang, $l_category;
  global $l_yes,$l_no;
  global $ico_mini_cal;  

  $sel_cty .= "\n<option value=\"\">$l_all</option>";
  while ($cty_q->next_record()) {
    $sel_cty .= "\n<option value=\"".$cty_q->f('country_iso3166')."\">".$cty_q->f('country_name').'</option>';
  }
  if ($cty_q->nf() != 0) {
    $cty_q->seek(0);
  }

  $sel_usr .= "\n<option value=\"\">$l_all</option>";
  while ($usr_q->next_record()) {
    $sel_usr .= "\n<option value=\"".$usr_q->f('userobm_id')."\">".$usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname').'</option>';
  }
  if ($usr_q->nf() != 0) {
    $usr_q->seek(0);
  }

  $sel_dsr .= "\n<option value=\"\">$l_all</option>";
  while ($dsr_q->next_record()) {
    $sel_dsr .= "\n<option value=\"".$dsr_q->f('datasource_id')."\">".$dsr_q->f('datasource_name').'</option>';
  }
  if ($dsr_q->nf() != 0) {
    $dsr_q->seek(0);
  }

  if ($fct_q != "") {
    $sel_fct .= "\n<option value=\"\">$l_all</option>";
    while ($fct_q->next_record()) {
      $sel_fct .= "\n<option value=\"".$fct_q->f('contactfunction_id')."\">".$fct_q->f('contactfunction_label').'</option>';
    }
    if ($fct_q->nf() != 0) {
      $fct_q->seek(0);
    }
  }

  $sel_lag .= "\n<option value=\"\">$l_all</option>";
  while($lag_q->next_record()) {
    $sel_lag .= "\n<option value=\"".$lag_q->f('kind_lang')."\">".$lag_q->f('kind_lang').'</option>';
  }   
  if ($lag_q->nf()!=0) {
    $lag_q->seek(0);
  }

  // Userdata
  $dis_cat = dis_list_add_criterion_userdata_form('contact');
 
  $block = "
  <fieldset class=\"detail infos\">
  <legend>$l_contact</legend>
  <table>
  <tr>
    <th>$l_lastname</th>
    <td><input type=\"text\" size=\"17\" name=\"tf_contact_lastname\" value=\"\" /></td>
  </tr><tr>  
    <th>$l_country</th>
    <td><select name=\"sel_contact_country_iso3166\">$sel_cty</select></td>
  </tr><tr>  
    <th>$l_timeafter</th>
    <td><input type=\"text\" name=\"tf_contact_timeafter\" value=\"$contact_timeafter\" class=\"datePicker\"/></td>
  </tr>
  
   <tr>
    <th>$l_firstname</th>
    <td><input type=\"text\" size=\"17\" name=\"tf_contact_firstname\" value=\"\" /></td>
   </tr><tr>
    <th>$l_market</th>
   <td><select name=\"sel_contact_marketingmanager_id\">$sel_usr</select></td>
   </tr><tr>
    <th>$l_timebefore</th>
    <td>
      <input type=\"text\" name=\"tf_contact_timebefore\" value=\"$contact_timebefore\" class=\"datePicker\"/>
    </td>
  </tr>
  
  <tr>
    <th>$l_datasource</th>
    <td><select name=\"sel_contact_datasource_id\">$sel_dsr</select></td>
  </tr><tr>
    <th>$l_town</th>
    <td><input type=\"text\" size=\"17\" name=\"tf_contact_town\" value=\"\" /></td>
  </tr><tr>
    <th>$l_postcode</th>
    <td><input type=\"text\" size=\"10\" name=\"tf_contact_zipcode\" value=\"\" /></td>
  </tr>
  
  <tr>
    <th>$l_function</th>
    <td><select name=\"sel_contact_function_id\">$sel_fct</select></td>
  </tr>
  <tr>
    <th>$l_category</th>
    <td>$dis_cat</td> 
  </tr>
  <tr> 
    <th>$l_lang</th>
    <td><select name=\"sel_kind_lang\">$sel_lag</select></td>  
  </tr>
  </table>
  </fieldset>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contact Criterion Form
// Parameters:
///////////////////////////////////////////////////////////////////////////////
function html_list_publication_criterion($rec_q,$pub_q) {
  global $path, $l_all, $l_undef, $l_yes, $l_no,  $ico_mini_cal;
  global $l_publication, $l_title, $l_lang, $l_year;
  global $l_renew,$l_recept, $l_after, $l_before;

  if ($rec_q->nf() !=0 ) {
    $rec_q->seek(0);
  }
  $sel_rec .= "\n<option value=\"\">$l_all</option>";
  while ($rec_q->next_record()) {
    $sel_rec .= "\n<option value=\"".$rec_q->f('subscriptionreception_id')."\">".$rec_q->f('subscriptionreception_label').'</option>';
  }

  if ($pub_q->nf() !=0) {
    $pub_q->seek(0);
  }
  $sel_pub .= "\n<option value=\"\">$l_all</option>";
  while ($pub_q->next_record()) {
    $sel_pub .= "\n<option value=\"".$pub_q->f('publication_id')."\">".$pub_q->f('publication_title').'</option>';
  }   
  
  $block = "
  <fieldset class=\"detail infos\">
  <legend>$l_publication</legend>    
  <table>
  <tr>
    <th>$l_title</th>
    <td><select name=\"sel_subscription_publication_id\">$sel_pub</select></td>
  </tr><tr>
    <th>$l_lang</th>
    <td><input type=\"text\" size=\"10\" name=\"tf_publication_lang\" value=\"\" /></td>
  </tr><tr>
    <th>$l_year</th>
    <td><input type=\"text\" size=\"10\" name=\"tf_publication_year\" value=\"\" /></td>
  </tr><tr>
    <th>$l_after</th> 
    <td><input type=\"text\" name=\"tf_subscription_timeafter\" value=\"$subscription_timeafter\" class=\"datePicker\"/></td>  
  </tr>

             
  <tr>
    <th>$l_renew</th>
     <td>
      <select name=\"tf_subscription_renewal\">
       <option value=\"\">$l_undef</option>
       <option value=\"1\">$l_yes</option>
       <option value=\"0\">$l_no</option>
      </select>
    </td>
  </tr><tr>
    <th>$l_recept</th>
    <td><select name=\"sel_subscription_reception_id\">$sel_rec</select></td>
  </tr><tr>
     <th>$l_before</th>
     <td><input type=\"text\" name=\"tf_subscription_timebefore\" value=\"$subscription_timebefore\" class=\"datePicker\"/></td>
  </tr>
  </table>
  </fieldset>
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

  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'list');
  $dis_pref->pref_title = $l_list_display;
  $dis_pref->pref_dis_help = 1;

  $block .= $dis_pref->display();

  $dis_pref->display_pref = $prefs_con;
  $dis_pref->display_entity = 'list_contact';
  $dis_pref->pref_title = $l_contact_display;

  $block .= $dis_pref->display();


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the list delete validation screen
// Parameters:
//   - $p_id : list id
///////////////////////////////////////////////////////////////////////////////
function dis_list_can_delete($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $dis_back = "<form name=\"form_back\" method=\"get\"
    action=\"".url_prepare('list_index.php') ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"list_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"form\"
      action=\"" . url_prepare('list_index.php') . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"list_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
    </form>";

  $display['msg'] .= display_ok_msg($l_can_delete);

  $block .= "
    <div class=\"buttons\">
      $dis_delete
      $dis_back
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

  $name = $list['name'];
  $subject = $list['subject'];
  $id_duplicated = $list['id_duplicated'];
  $mailing_ok = ($list['mailing_ok'] == '1' ? '1' : '0');
  $contact_arch = ($list['contact_archive'] == '1' ? '1' : '0');
  $info_pub = ($list['info_pub'] == '1' ? '1' : '0');
  $mode = ($list['mode'] == '1' ? '1' : '0');
  $query = stripslashes($list['query']);
  $email = $list['email'];
  if (isset($list['criteria'])) {
    $criteria = urlencode(serialize($list['criteria']));
  }
  $display['msg'] .= display_warn_msg($l_check_samelist);
  while ($list_q->next_record()) {
    $id = $list_q->f('list_id');
    $samename = $list_q->f('list_name');
    $samesubject = $list_q->f('list_subject');
    $dis_same_list .= "
      <li>
        <a class=\"detail\" href=\"" .url_prepare("list_index.php?action=detailconsult&amp;list_id=$id") . "\">$samename ($samesubject)</a>
      </li>";
  }

  $hidden = "
      <input type=\"hidden\" name=\"tf_name\" value=\"$name\" />
      <input type=\"hidden\" name=\"tf_subject\" value=\"$subject\" />
      <input type=\"hidden\" name=\"ta_query\" value=\"$query\" />
      <input type=\"hidden\" name=\"cba_mailing_ok\" value=\"$mailing_ok\" />
      <input type=\"hidden\" name=\"cba_contact_archive\" value=\"$contact_arch\" />
      <input type=\"hidden\" name=\"cba_info_pub\" value=\"$info_pub\" />
      <input type=\"hidden\" name=\"rd_mode\" value=\"$mode\" />
      <input type=\"hidden\" name=\"email\" value=\"$email\" />
      <input type=\"hidden\" name=\"se_criteria\" value=\"$criteria\" />
      <input type=\"hidden\" name=\"id_duplicated\" value=\"$id_duplicated\" />";

  $block = "
  <div class=\"detail\">
  <ul>
  $dis_same_list
  </ul>
  </div>

  <div class=\"buttons\">
    <form method=\"post\" name=\"form_insert\"
      action=\"" .url_prepare('list_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      <input type=\"submit\" name=\"submit\" value=\"$l_confirm\" />
      $hidden
    </form>
    <form name=\"form_back\" method=\"post\"
      action=\"" .url_prepare('list_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      <input type=\"submit\" value=\"$l_back\" />
      $hidden
    </form>
  </div>";

  return $block;
}


?>
