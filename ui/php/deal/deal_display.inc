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
// OBM - File : deal_display.php                                             //
//     - Desc : Deal Display File                                            //
// 2000-01-20 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['deal_label'] = $l_label;
$fieldnames['deal_number'] = $l_number;
$fieldnames['deal_datebegin'] = $l_begin;
$fieldnames['deal_dateproposal'] = $l_proposal;
$fieldnames['deal_dateexpected'] = $l_expected;
$fieldnames['expected_quarter'] = "$l_expected $l_quarter";
$fieldnames['sale_delay'] = $l_sale_delay;
$fieldnames['deal_dateend'] = $l_end;
$fieldnames['end_quarter'] = "$l_end $l_quarter";
$fieldnames['deal_datealarm'] = $l_alarm;
$fieldnames['deal_archive'] = $l_archive_first;
$fieldnames['deal_todo'] = $l_todo;
$fieldnames['deal_marketingmanager'] = $l_marketing_manager;
$fieldnames['deal_source'] = $l_source;
$fieldnames['deal_technicalmanager'] = $l_technical_manager;
$fieldnames['deal_commission'] = $l_commission_p;
$fieldnames['deal_amount'] = $l_amount;
$fieldnames['deal_margin'] = $l_margin;
$fieldnames['deal_hitrate'] = $l_hitrate;
$fieldnames['deal_relation'] = $l_relation;
$fieldnames['parentdeal_label'] = $l_label;
$fieldnames['parentdeal_comment'] = $l_comment;
// Calculated or indirect fields
$fieldnames['dealstatus_label'] = $l_status;
$fieldnames['dealtype_label'] = $l_type;
$fieldnames['tasktype_label'] = $l_tt;
$fieldnames['region_label'] = $l_region;
$fieldnames['deal_company_name'] = $l_company;
$fieldnames['deal_company_zipcode'] = $l_postcode;
$fieldnames['parentdeal_marketing_lastname'] = $l_marketing_manager;
$fieldnames['parentdeal_technical_lastname'] = $l_technical_manager;
$fieldnames['parentdeal_archive'] = $l_archive;
$fieldnames['parentdeal_status'] = $l_status;
$fieldnames['parentdeal_dateproposal'] = $l_proposal;
$fieldnames['parentdeal_datealarm'] = $l_alarm;
$fieldnames['parentdeal_amountprovider'] = $l_amount_out;
$fieldnames['parentdeal_amountcustomer'] = $l_amount_in;


///////////////////////////////////////////////////////////////////////////////
// Display Deal specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_deal(&$OD, $fieldname, $link_ok) {
  global $path, $deal, $col_ok, $col_error, $col_client, $col_frs, $l_quarter;

  $ext_url = $deal['ext_url'];

  if ($fieldname == 'deal_label') {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = 'javascript:check_get_id('.$OD->data_set->f('deal_id').",'".addslashes($OD->data_set->f('deal_label'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $res['url'] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f('deal_id').');';
    } else {
      $res['url'] = "$path/deal/deal_index.php?action=detailconsult&amp;deal_id=".$OD->data_set->f('deal_id');
    }
  }

  else if ($fieldname == 'parentdeal_label') {
    $res['url'] = "$path/deal/deal_index.php?action=parent_detailconsult&amp;parentdeal_id=".$OD->data_set->f('parentdeal_id');
  }

  else if ($fieldname == 'parentdeal_marketing_lastname') {
    $res['url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f('parentdeal_marketingmanager_id');
  }

  else if ($fieldname == 'parentdeal_technical_lastname') {
    $res['url'] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f('parentdeal_technicalmanager_id');
  }

  else if ($fieldname == 'deal_source') {
    $label = $OD->data_set->f('leadsource_label');
    $source = $OD->data_set->f("$fieldname");
    if (($label != '') || ($source != '')) {
      $res['name'] = "$label : $source";
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'deal_commission') {
    $res['align'] = 'center';
  }

  else if (($fieldname == 'deal_archive') || ($fieldname == 'parentdeal_archive')) {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if (($fieldname == 'deal_amount')
	   || ($fieldname == 'deal_margin')) {
    $res['align'] = 'center';
  }

  else if ($fieldname == 'deal_hitrate') {
    $res['align'] = 'center';
    $res['name'] = $OD->data_set->f($fieldname) . ' %';
  }

  else if (($fieldname == 'deal_company_name') && $link_ok) {
    $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('deal_company_id');
  }
  
  else if ($fieldname == 'deal_datebegin') {
    $date = of_date_format( $OD->data_set->f($fieldname), 1);
    $res['name'] = $date;
  }

  else if ($fieldname == 'deal_dateproposal') {
    $date = of_date_format($OD->data_set->f($fieldname), 1);
    $res['name'] = $date;
  }

  else if ($fieldname == 'deal_dateexpected') {
    $date = of_date_format($OD->data_set->f($fieldname), 1);
    $res['name'] = $date;
  }

  else if ($fieldname == 'expected_quarter') {
    $date = $OD->data_set->f('deal_dateexpected');
    if ($date != '') {
      $year = of_date_get_year($date);
      $month = of_date_get_month($date);
      $q = floor(($month-1) / 3) + 1;
      $quarter = "$year$l_quarter$q";
      $res['align'] = 'center';
      $res['name'] = $quarter;
    }
  }

  else if ($fieldname == 'sale_delay') {
    $datebegin = $OD->data_set->f('deal_datebegin');
    $dateexp = $OD->data_set->f('deal_dateexpected');
    if (($datebegin != '') && ($dateexp != '')) {
      $sale_delay = floor(($dateexp - $datebegin) / 86400);
      $res['align'] = 'center';
      $res['name'] = $sale_delay;
    }
  }

  else if ($fieldname == 'deal_dateend') {
    $date = of_date_format($OD->data_set->f($fieldname), 1);
    $res['name'] = $date;
  }

  else if ($fieldname == 'end_quarter') {
    $date = $OD->data_set->f('deal_dateend');
    if ($date != '') {
      $month = of_date_get_month($date);
      $q = floor(($month-1) / 3) + 1;
      $quarter = "$l_quarter$q";
      $res['align'] = 'center';
      $res['name'] = $quarter;
    }
  }

  else if ($fieldname == 'deal_datealarm') {
    $date = $OD->data_set->f($fieldname);
    $datealarm = of_date_format($date);
    $date = date('Ymd',$OD->data_set->f($fieldname));
    $time = date('Ymd');
    if ($time > $date) {
      $col = 'error';
    } else if ($time == $date) {
      $col = 'highlight';
    }
    $res['style'] = "class=\"$col\"";
    $res['name'] = $datealarm;
  }

  else if ($fieldname == 'deal_relation') {
    $d_id = $OD->data_set->f('deal_id');
    $dc = get_deal_dealcompany_info($d_id);
    $nl = '';
    if (is_array($dc)) {
      foreach ($dc as $relation) {
	$name .= $nl . $relation['role_label'].' : '.$relation['company_name'];
	$nl = '<br />';
      }
    }
    $res['name'] = $name;
  }
  

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal index page
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_index($deal='') {
  global $display, $ctt_sales;

  $archive = $deal['archive'];

  $usr_q = run_query_deal_manager($archive);
  $types = get_global_deal_types('dealtype_label');
  $regions = of_category_get_ordered('', 'region');
  $tts = get_global_tasktype($ctt_sales);
  $roles = of_category_get_ordered('DealCompany', 'role');
  $status_q = run_query_deal_status_list();
  $block .= html_deal_parentdeal_search_form($deal, $usr_q);
  $block .= html_deal_search_form($deal, $types, $regions, $tts, $roles, $status_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Search page
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_form($deal='') {
  global $display, $ctt_sales;

  $archive = $deal['archive'];

  $usr_q = run_query_deal_manager($archive);
  $types = get_global_deal_types('dealtype_label');
  $regions = of_category_get_ordered('', 'region');
  $tts = get_global_tasktype($ctt_sales);
  $roles = of_category_get_ordered('DealCompany', 'role');
  $status_q = run_query_deal_status_list();
  $block .= html_deal_search_form($deal, $types, $regions, $tts, $roles, $status_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Deal search form
// Parameters : 
//   - $deal[]     : default form values
//   - $types      : Deal types list
//   - $regions    : regions array
//   - $tts        : tasktype array
//   - $roles      : array with dealcompany roles list 
//   - $stat_q     : DB result object (deal status list)
//   - $manager_q  : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_form($deal, $types, $regions, $tts, $roles, $stat_q, $manager_q) {
  global $l_deal, $l_label_start, $l_company, $l_type, $l_tt, $l_status;
  global $l_after, $l_before, $l_show_archive, $l_relation;
  global $l_manager, $l_marketing_manager,$l_technical_manager;
  global $l_number, $l_postcode, $l_region;
  global $l_date, $l_begin, $l_end, $l_proposal, $l_expected, $l_alarm;
  global $l_all, $l_all_f, $l_find, $c_all, $l_select_deal;
  global $obm, $display;

  // --- Var preparation ------------------------------------------------------

  $popup = $deal['popup'];
  $parent = $deal['parentdeal_id'];
  $label = stripslashes($deal['label']);
  $name = stripslashes($deal['company_name']);
  $zip = stripslashes($deal['zip']);
  $archive = ($deal['archive'] == '1' ? "checked = \"checked\"" : '');
  $type = $deal['type_id'];
  $number = $deal['number'];
  $region = $deal['region'];
  $tt = $deal['tasktype'];
  $role = $deal['role'];
  $status = $deal['status_id'];
  $manager = $deal['manager'];
  $manager_type = $deal['manager_type'];
  $date_field = ($deal['date_field'] ? $deal['date_field'] : 'datealarm');
  $date_after = of_date_upd_format(stripslashes($deal['date_after']),true);
  $date_before = of_date_upd_format(stripslashes($deal['date_before']),true);
  $company_id = $deal['company_id'];
  $role_company = $deal['role_company'];
  if (($deal['action'] != 'search') && (! isset($deal['delegation']))) { 
    $delegation = $obm['delegation'];
  }
  if ($deal['delegation'] != '') {
    $delegation = stripslashes($deal['delegation']);
  }

  // User defined data
  $block_userdata .= of_userdata_dis_search('deal', $deal);

  // Type select construction
  $sel_type = "<select name=\"sel_type_id\">
     <option value=\"$c_all\">$l_all</option>";  
  foreach($types as $t_id => $one_type) {
    $tlabel = $one_type['label'];
    $sel_type .= "<option value=\"$t_id\"";
    if ($type == $t_id) $sel_type .= " selected = \"selected\"";
    $sel_type .= ">$tlabel</option>";
  }
  $sel_type .= '</select>';

  // TaskType select construction
  $sel_tt = "<select name=\"sel_tasktype\">
              <option value=\"$c_all\">$l_all</option>";
  if (is_array($tts)) {
    foreach ($tts as $tt_id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $internal = $one_tt['internal'];
      $sel_tt .= "\n<option value=\"$tt_id\"";
      if ($tt == $tt_id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">$tt_label</option>\n";
    }
    $sel_tt .= '</select>';
  }

  // Role select construction
  $block_role = of_category_dis_search_select('DealCompany', 'role', $roles, $role);

  // Region select construction
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  // Status select construction
  $sel_status = "<select name=\"sel_status_id\">
     <option value=\"$c_all\">$l_all</option>";
  while ($stat_q->next_record()) {
    $s_id = $stat_q->f('dealstatus_id');
    $slabel = $stat_q->f('dealstatus_label');
    $sel_status .= "<option value=\"$s_id\"";
    if ($status == $s_id) $sel_status .= "selected =\" selected\"";
    $sel_status .= ">$slabel</option>";
  }
  $sel_status .= '</select>';

  // Manager select construction
  if ($manager_q->num_rows() > 0) {
    $manager_q->seek(0);
  }
  $sel_manager = "<select name=\"sel_manager\">
     <option value=\"$c_all\">$l_all</option>";
  while ($manager_q->next_record()) {
    $m_id = $manager_q->f('userobm_id');
    $lname = $manager_q->f('userobm_lastname');
    $sel_manager .= "<option value=\"$m_id\"";
    if ($manager == $m_id) $sel_manager .= "selected =\"selected\"";
    $sel_manager .= ">$lname</option>";
  }
  $sel_manager .= '</select>';

  // Manager type select construction
  if ($manager_type == "$c_all") $sel_all = "selected =\"selected\"";
  if ($manager_type == "marketing") $sel_market = "selected =\"selected\"";
  if ($manager_type == "technical") $sel_tech = "selected =\"selected\"";
  $sel_manager_type = "<select name=\"sel_manager_type\">
     <option value=\"$c_all\" $sel_all>$l_all</option>
     <option value=\"marketing\" $sel_market>$l_marketing_manager</option>
     <option value=\"technical\" $sel_tech>$l_technical_manager</option>
     </select>";

  // Selected date field
  $s_date_field[$date_field] = "selected=\"selected\"";

  $dis_delegation = of_delegation_dis_search_form($delegation);

  $url = url_prepare('deal_index.php');

  if ($popup) {
    $ext_action = $deal['ext_action'];
    $ext_url = $deal['ext_url'];
    $ext_id = $deal['ext_id'];
    $ext_title = ($deal['ext_title'] ? $deal['ext_title'] : "$l_select_deal");
    $ext_target = $deal['ext_target'];
    $ext_widget = $deal['ext_widget'];
    $ext_widget_text = $deal['ext_widget_text'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\" />
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\" />
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\" />
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\" />
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\" />
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\" />
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\" />";
    $display['title'] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" action=\"$url\">
  <fieldset class=\"detail extra\">
  <legend>$l_deal</legend>
  </fieldset>
    <label>$l_label_start<br />
      <input name=\"tf_label\" size=\"16\" maxlength=\"128\" value=\"$label\" />
    </label>
    <label>$l_company<br />
      <input name=\"tf_company_name\" size=\"12\" maxlength=\"16\" value=\"$name\" />
    </label>
    $dis_delegation
    <label>$l_postcode<br />
      <input name=\"tf_zip\" size=\"12\" value=\"$zip\" />
    </label>
    <label>$l_number<br />
      <input name=\"tf_number\" size=\"16\" maxlength=\"128\" value=\"$number\" />
    </label>
    <label>$l_date<br />
      <select name=\"sel_date_field\">
        <option value=\"datealarm\" $s_date_field[datealarm]>$l_alarm</option>
        <option value=\"datebegin\" $s_date_field[datebegin]>$l_begin</option>
        <option value=\"dateexpected\" $s_date_field[dateexpected]>$l_expected</option>
        <option value=\"dateend\" $s_date_field[dateend]>$l_end</option>
      </select>
    </label>
    <label>$l_after<br />
      <input type=\"text\" name=\"tf_date_after\" value=\"$date_after\" class=\"datePicker\"/>
    </label>
    <label>$l_before<br />
     <input type=\"text\" name=\"tf_date_before\" value=\"$date_before\" class=\"datePicker\"/>
    </label>
    <label>$l_type<br />
      $sel_type
    </label>
    <label>$l_tt<br />
      $sel_tt
    </label>
    $block_region 
    $block_userdata 
    $block_role 
    <label>$l_relation<br />
      <input name=\"tf_role_company\" size=\"12\" value=\"$role_company\" />
    </label>
    <label>$l_status<br />
      $sel_status
    </label>
    <label>$l_manager<br />
      $sel_manager
      $sel_manager_type
    </label>
    <label>$l_show_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"parentdeal_id\" type=\"hidden\" value=\"$parent\" />
      <input name=\"company_id\" type=\"hidden\" value=\"$company_id\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />      
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal search form
// Parameters : 
//   - $deal[]      : default form values
//     keys used    plabel, parchive, pmanager
//   - $pmanager_q  : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_deal_parentdeal_search_form($deal, $pmanager_q) {
  global $l_parentdeal, $l_label_start, $l_all, $l_all_f, $l_manager;
  global $l_find,$l_show_archive; 

  // --- Var preparation ------------------------------------------------------

  $plabel = stripslashes($deal['plabel']);
  $parchive = ($deal['parchive'] == '1' ? 'checked' : '');
  $pmanager = $deal['pmanager'];

  // Manager select construction
  if ($pmanager_q->num_rows() > 0) {
    $pmanager_q->seek(0);
  }
  $sel_pmanager = "<select name=\"sel_pmanager\">
     <option value=\"$c_all\">$l_all</option>";
  while($pmanager_q->next_record()) {
    $m_id = $pmanager_q->f('userobm_id');
    $lname = $pmanager_q->f('userobm_lastname');
    $sel_pmanager .= "<option value=\"$m_id\"";
    if ($pmanager == $m_id) $sel_pmanager .= "selected = \"selected\"";
    $sel_pmanager .= ">$lname</option>";
  }
  $sel_pmanager .= '</select>';

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" class=\"search\" name=\"form_search_parentdeal\"
    action=\"" . url_prepare("deal_index.php") . "\">
  <fieldset class=\"detail extra\">
  <legend>$l_parentdeal</legend>
  </fieldset>
    <label>$l_label_start<br />
      <input type=\"text\" name=\"tf_plabel\" size=\"20\" value=\"$plabel\" />
    </label>
    <label>$l_manager<br />
      $sel_pmanager
    </label>
    <label>$l_show_archive<br />
      <input type=\"checkbox\" name=\"cba_parchive\" value=\"1\" $parchive />
    </label>
      <input name=\"action\" type=\"hidden\" value=\"parent_search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    <p class=\"CL\" />
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal search result
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_list($deal) {
  global $display, $obm, $l_found, $l_no_found;

  $obm_q = run_query_deal_search($deal, 0);
  $nb_deal = $obm_q->num_rows_total();
  if ($nb_deal == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_deal $l_found");
    $prefs = get_display_pref($obm['uid'], 'deal', 0);
    $block = html_deal_search_list($obm_q, $prefs, $deal);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Deal Search result
// Parameters:
//   - $deal_q   : list of deals
//   - $prefs    : fields to display in the list of deals 
//   - $deal[]   : deal search criteria
//////////////////////////////////////////////////////////////////////////////
function html_deal_search_list($deal_q, $prefs, $deal) {
  global $l_close;

  $popup = $deal['popup'];
  $parent = $deal['parentdeal_id'];
  $label = urlencode(stripslashes($deal['label']));
  $name = urlencode(stripslashes($deal['company_name']));
  $zip = urlencode(stripslashes($deal['zip']));
  $archive = $deal['archive'];
  $type = $deal['type_id'];
  $number = $deal['number'];
  $region = $deal['region'];
  $tt = $deal['tasktype'];
  $role = $deal['role'];
  $role_company = urlencode(stripslashes($deal['role_company']));
  $status = $deal['status_id'];
  $manager = $deal['manager'];
  $manager_type = $deal['manager_type'];
  $date_after = stripslashes($deal['date_after']);
  $date_before = stripslashes($deal['date_before']);
  $param_comp = $deal['company_id'];
  $region = $deal['region'];
  $delegation = $deal['delegation'];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params('deal', $deal);
  
  if ($popup) {
    $ext_action = $deal['ext_action'];
    $ext_url = $deal['ext_url'];
    $ext_id = $deal['ext_id'];
    $ext_target = $deal['ext_target'];
    $ext_widget = $deal['ext_widget'];
    $ext_widget_text = $deal['ext_widget_text'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;tf_zip=$zip&amp;tf_number=$number&amp;cba_archive=$archive&amp;sel_type_id=$type&amp;sel_region=$region&amp;sel_status_id=$status&amp;sel_tasktype=$tt&amp;sel_role=$role&amp;tf_role_company=$role_company&amp;sel_manager=$manager&amp;manager_type=$manager_type&amp;tf_date_after=$date_after&amp;tf_date_before=$date_before&amp;parentdeal_id=$parent&company_id=$param_comp&amp;sel_region=$region&amp;tf_delegation=$delegation$url_userdata$url_ext");

  $deal_d = new OBM_DISPLAY('DATA', $prefs, 'deal');
  if ($popup) {
    $deal_d->display_link = false;
    if ($ext_url != '') {
      $deal_d->display_ext = 'get_id_url';
    } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
      $deal_d->display_ext = 'get_id';
    }
   $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }

  $deal_d->data_set = $deal_q;
  $deal_d->data_url = $url;
  $deal_d->data_header = 'both';
  $block = $deal_d->display('dis_data_deal');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Deal links menu
// Parameters:
//   - $deal_q : DBO : information about the deal
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_deal_links($deal_q) {
  global $ico_pdeal, $ico_deal, $ico_contract;
  global $ico_add, $ico_invoice, $ico_project;
  global $l_new, $l_parentdeal, $l_affect_deal, $l_module_contract;
  global $l_module_project, $l_module_invoice;
  global $l_module_document,$ico_document, $l_deal;
  global $l_document_add, $perm;
  global $path, $cgp_show;

  $id = $deal_q->f('deal_id');
  $pid = $deal_q->f('deal_parentdeal_id');
  $cid = $deal_q->f('deal_company_id');
  $type_id = $deal_q->f('deal_type_id');
  $tt_id = $deal_q->f('deal_tasktype_id');
  $cname = $deal_q->f('company_name');
  $uname = urlencode($cname);
  $label = $deal_q->f('deal_label');
  $ulabel = urlencode($label);
  $con1 = $deal_q->f('deal_contact1_id');
  $con2 = $deal_q->f('deal_contact2_id');
  $hitrate = $deal_q->f('deal_hitrate');
  $project = $deal_q->f('deal_project_status');
  $market = $deal_q->f('deal_marketingmanager_id');
  $tech = $deal_q->f('deal_technicalmanager_id');
  $number = $deal_q->f('deal_number');

  // parent deal
  if ($pid > 0) {
    $plabel = get_deal_parent_label($pid);
    $url_pdeal = url_prepare("$path/deal/deal_index.php?action=parent_detailconsult&amp;parentdeal_id=$pid");
    $dis_pdeal = "
    <li><a href=\"$url_pdeal\"><img src=\"$ico_pdeal\" alt=\"\" /></a>
        <a href=\"$url_pdeal\">$l_parentdeal ($plabel)</a></li>";
  }
  $url_pdeal_upd = url_prepare("$path/deal/deal_index.php?action=affect&amp;parentdeal_id=$pid&amp;deal_id=$id");

  // Project
  if ($perm->check_module_rights('project')) {
    $nb_pro = get_global_linked_project_nb ($id, 'deal');
    $nb_pro_arch = get_global_linked_project_nb ($id, 'deal', 1);
    $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;deal_id=$id");
    $url_pro_arch = url_prepare("$path/project/project_index.php?action=search&amp;deal_id=$id&amp;cba_archive=1");
    $dis_ico_project = "
        <li><a href=\"$url_pro\"><img src=\"$ico_project\" alt=\"\" /></a>
            $l_module_project (<a href=\"$url_pro\">$nb_pro</a> / <a href=\"$url_pro_arch\">$nb_pro_arch</a>)</li>";

    // New project allowed only if hitrate = 100
    if ($hitrate == '100') {
      $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;deal_id=$id&amp;deal_label=$label&amp;company_id=$cid&amp;tf_company_name=$cname&amp;sel_tt=$tt_id&amp;sel_type_id=$type_id&amp;shortname=$number");
      $dis_ico_project .= "
        <li><a href=\"$url_pro_new\"><img src=\"$ico_add\" alt=\"\" /></a>
            <a href=\"$url_pro_new\">$l_new</a></li>";
    }
    
    $block_project = "
    <div id=\"dealProject\" class=\"portlet\">
     <h1>$l_module_project</h1>
     <ul>
       $dis_ico_project
     </ul></div>";
  }

  // Contract
  if ($perm->check_module_rights('contract')) {
    $contract_active = get_global_linked_contract_nb($id, 'deal');
    $contract_total = get_global_linked_contract_nb($id, 'deal', 1);
    $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;deal_id=$id");
    $url_contract_total = url_prepare("$path/contract/contract_index.php?action=search&amp;deal_id=$id&amp;cba_archive=1");
    $dis_contract = "
    <li><a href=\"$url_contract\"><img src=\"$ico_contract\" alt=\"\" /></a>
        $l_module_contract (
        <a href=\"$url_contract\">$contract_active</a> /
        <a href=\"$url_contract_total\">$contract_total</a> )</li>";

    // New Contracts allowed only if hitrate = 100
    if ($hitrate == '100') {
      $url_contract_new = url_prepare("$path/contract/contract_index.php?action=new&amp;deal_id=$id&amp;company_id=$cid&amp;deal_label=$label&amp;sel_con1=$con1&amp;sel_con2=$con2&amp;sel_market=$market&amp;sel_tech=$tech");
      $dis_contract .= "
      <li><a href=\"$url_contract_new\"><img src=\"$ico_add\" alt=\"\" /></a>
        <a href=\"$url_contract_new\">$l_new</a></li>";
    }
    $block_contract = "
  <div id=\"dealContract\" class=\"portlet\">
     <h1> $l_module_contract</h1>
     <ul>
       $dis_contract
     </ul></div>";
  }

  // Invoice
  if ($perm->check_module_rights('invoice')) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;deal_id=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;deal_id=$id&amp;cba_archive=1");
    $nb_inv_tot = get_global_linked_invoice_nb($id, 'deal', 1);
    $nb_inv = get_global_linked_invoice_nb($id, 'deal');
    $dis_invoice = "
    <li><a href=\"$url_inv\"><img src=\"$ico_invoice\" alt=\"\" /></a>
        $l_module_invoice (<a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a>)</li>";

    // New Invoices allowed only if hitrate = 100
    if ($hitrate == '100') {
      $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;deal_id=$id&amp;deal_label=$ulabel&amp;company_id=$cid&amp;company_name=$uname");
      $dis_invoice .= "
    <li><a href=\"$url_inv_new\"><img src=\"$ico_add\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>";
    }
    $block_invoice = "
  <div id=\"dealInvoice\" class=\"portlet\">
  <h1> $l_module_invoice</h1>
  <ul>
    $dis_invoice
  </ul></div>";
  }

  // Document
  if ($perm->check_module_rights('document')) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=deal");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=deal");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/deal/deal_index.php")."&amp;ext_id=$id&amp;ext_target=$l_deal";
    $nb_document = run_query_global_document_nb($id, 'deal');
    $block_doc = "
  <div id=\"dealDocument\" class=\"portlet\">
  <h1> $l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
	<li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_deal'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul></div>";
  }

  // Links Template  
  $block = "
  <div id=\"dealParentdeal\" class=\"portlet\">
  <h1> $l_parentdeal</h1>
  <ul>
    $dis_pdeal
    <li><a href=\"$url_pdeal_upd\"><img src=\"$ico_deal\" alt=\"\" /></a>
        <a href=\"$url_pdeal_upd\">$l_affect_deal</a></li>
  </ul>
  </div>
  $block_project
  $block_contract
  $block_invoice
  $block_doc
 ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal Detail screen
// Parameters:
//   - $deal[] : deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_consult($deal) {
  global $display, $l_err_reference;

  if ($deal['deal_id'] > 0) {
    $deal_q = run_query_deal_detail($deal['deal_id']);
    $dc = get_deal_dealcompany_info($deal['deal_id']);
    $display['detailInfo'] = display_record_info($deal_q);
    $block = html_deal_consult($deal_q, $dc);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
  }
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display: Deal Consult
// Parameters :
//   - $deal_q  : DBO : information about the deal
//   - $dc      : array DealCompany infos
///////////////////////////////////////////////////////////////////////////////
function html_deal_consult($deal_q, $dc='') {
  global $ico_company, $ico_contact, $display;
  global $l_dates, $l_company, $l_contact, $l_other_relation, $l_relation;
  global $l_label, $l_type,$l_region,$l_marketing_manager,$l_technical_manager;
  global $l_amount, $l_margin, $l_hitrate, $l_commission_p, $l_sale_delay;
  global $l_number, $l_tt, $l_status,$l_archive,$l_comment;
  global $l_begin, $l_end, $l_proposal, $l_expected, $l_alarm, $l_quarter;
  global $l_parentdeal,$l_todo, $l_deal, $l_source;
  global $display, $path, $l_yes, $l_no;

  $id = $deal_q->f('deal_id');
  $pid = $deal_q->f('deal_parentdeal_id');
  $usercreate = $deal_q->f('deal_usercreate');
  $label = $deal_q->f('deal_label');
  $number = $deal_q->f('deal_number');
  $datebegin = of_date_format($deal_q->f('datebegin'), 1);
  $dateexpected = of_date_format($deal_q->f('dateexpected'), 1);
  if ($dateexpected != '') {
    $month_exp = of_date_get_month($deal_q->f('dateexpected'));
    $year_exp = of_date_get_year($deal_q->f('dateexpected'));
    $q = floor(($month_exp-1) / 3) + 1;
    $quarter_exp = "($year_exp$l_quarter$q)";
  }
  if (($datebegin != '') && ($dateexpected != '')) {
    $sale_delay = floor(($deal_q->f('dateexpected') - $deal_q->f('datebegin')) / 86400);
  }
  $dateend = of_date_format($deal_q->f('dateend'), 1);
  if ($dateend != '') {
    $month_end = of_date_get_month($deal_q->f('dateend'));
    $year_end = of_date_get_year($deal_q->f('dateend'));
    $q = floor(($month_end-1) / 3) + 1;
    $quarter_end = "($year_end$l_quarter$q)";
  }
  $type = $deal_q->f('dealtype_label');
  $tt_label = $deal_q->f('tasktype_label');
  $region_label = $deal_q->f('region_label');
  $market = $deal_q->f('lname1') . ' ' . $deal_q->f('fname1');
  $tech = $deal_q->f('lname2') . ' ' . $deal_q->f('fname2');
  $source_label = $deal_q->f('leadsource_label');
  $source = $deal_q->f('deal_source');
  $commission = $deal_q->f('deal_commission');
  $datepro = of_date_format($deal_q->f('dateproposal'), 1);
  $amount = $deal_q->f('deal_amount');
  $margin = $deal_q->f('deal_margin');
  $hitrate = $deal_q->f('deal_hitrate');
  $datealarm = of_date_format($deal_q->f('datealarm'), 1);
  $status = $deal_q->f('dealstatus_label');
  $archive = ($deal_q->f('deal_archive') == 1 ? $l_yes : $l_no);
  $todo = $deal_q->f('deal_todo');
  $com = beautify_comment(nl2br($deal_q->f('deal_comment')));
  $comp_id = $deal_q->f('deal_company_id');
  $comp = $deal_q->f('company_name');
  if (strlen($comp) > 25) {
    $comp = substr($comp, 0, 24) . '...';
  }
  $ad1 = $deal_q->f('company_address1');
  $zip = $deal_q->f('company_zipcode');
  $town = $deal_q->f('company_town');
  $comp_phone = $deal_q->f('company_phone');
  $comp_phone = (($comp_phone != '') ? "($comp_phone)" : '');
  $con1 = $deal_q->f('deal_contact1_id');
  $dis_con1 = $deal_q->f('lnamec1') . ' ' . $deal_q->f('fnamec1');
  $phone_con1 = ($deal_q->f('phonec1') ? $deal_q->f('phonec1') : $comp_phone);
  $con2 = $deal_q->f('deal_contact2_id');
  $dis_con2 = $deal_q->f('lnamec2') . ' ' . $deal_q->f('fnamec2');
  $phone_con2 = ($deal_q->f('phonec2') ? $deal_q->f('phonec2') : $comp_phone);

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult('deal', $id);

  $display['link'] = html_deal_links($deal_q);
  $display['title'] = $label;

  // DealCompany infos
  if ( (is_array($dc)) && (count($dc) > 0) ) {
    foreach ($dc as $one_dc) {
      $dc_cid = $one_dc['company_id'];
      $dc_cname = $one_dc['company_name'];
      $dc_role = $one_dc['role_label'];
      $dis_dc .= "
        <div>
          <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$dc_cid")."\">
        <img src=\"$ico_company\" alt=\"[details]\" /></a> $dc_cname : $dc_role
        </div>";
    }
  }


  $block = "
  <div class=\"detail infos\">
  <h1>$l_deal</h1>

    <table>
    <tr>
      <th>$l_label</th>
      <td>$label</td>
    </tr>
    <tr>
      <th>$l_number</th>
      <td>$number</td>
    </tr>
    <tr>
      <th>$l_type</th>
      <td>$type</td>
    </tr>
    <tr>
      <th>$l_tt</th>
      <td>$tt_label</td>
    </tr>
    <tr>
      <th>$l_region</th>
      <td>$region_label</td>
    </tr>
    <tr>
      <th>$l_source</th>
      <td>$source_label : $source</td>
    </tr>
    <tr>
      <th>$l_commission_p</th>
      <td>$commission</td>
    </tr><tr>
      <th>$l_marketing_manager</th>
      <td>$market</td>
    </tr><tr>
      <th>$l_technical_manager</th>
      <td>$tech</td>
    </tr>
    </table>
    </div>

  <div class=\"detail infos\">
  <h1>$l_dates</h1>

    <table>
    <tr>
      <th>$l_begin</th>
      <td>$datebegin</td>
    </tr><tr>
      <th>$l_proposal</th>
      <td>$datepro</td>
    </tr><tr>
      <th>$l_expected</th>
      <td>$dateexpected $quarter_exp</td>
    </tr>
    <tr>
      <th>$l_end</th>
      <td>$dateend $quarter_end</td>
    </tr>
    <tr>
      <th>$l_sale_delay</th>
      <td>$sale_delay</td>
    </tr>
    </table>
    </div>

   <div class=\"detail infos\">
   <h1>$l_relation</h1>

    <table>
    <tr>
      <th>$l_company</th>
      <td><a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id")."\">
      <img src=\"$ico_company\" alt=\"[details]\" /></a> $comp<br />$ad1<br/>$zip $town</td>
    </tr><tr>
      <th>$l_contact 1</th>
      <td><a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con1") . "\">
      <img src=\"$ico_contact\" alt=\"[details]\" /></a> $dis_con1 - $phone_con1
      </td>
    </tr><tr>
      <th>$l_contact 2</th>
      <td><a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con2") . "\">
      <img src=\"$ico_contact\" alt=\"[details]\" /></a> $dis_con2 - $phone_con2
      </td>
    </tr>
    <tr>
      <th>$l_other_relation</th>
      <td>$dis_dc</td>
    </tr>
    </table>
    </div>

   <div class=\"detail infos\">
   <h1>$l_status</h1>

    <table>
    <tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr><tr>
      <th>$l_alarm</th>
      <td>$datealarm</td>
    </tr><tr>
      <th>$l_status</th>
      <td>$status</td>
    </tr><tr>
      <th>$l_hitrate</th>
      <td>$hitrate %</td>
    </tr><tr>
      <th>$l_amount</th>
      <td>$amount</td>
    </tr><tr>
      <th>$l_margin</th>
      <td>$margin</td>
    </tr><tr>
      <th>$l_todo</th>
      <td>$todo</td>
    </tr>
    </table>
   </div>

   <div class=\"detail infos\"> 
   $block_userdata
   </div>
    
  <div class=\"detail extra\">
  <h1>$l_comment</h1>
    $com
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal form
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_form($deal) {
  global $display, $obm, $action, $ctt_sales;

  // Hook : Pre
  if (function_exists('hook_pre_dis_deal_form')) {
    $deal = hook_pre_dis_deal_form($deal);
  }

  if ($action == 'detailupdate') {
    $deal_q = run_query_deal_detail($deal['deal_id']);
    $display['detailInfo'] = display_record_info($deal_q);
    $deal['company_id'] = $deal_q->f('deal_company_id');
    $users = array($obm['uid'], $deal_q->f('deal_marketingmanager_id'), $deal_q->f('deal_technicalmanager_id'), );
    $dc = get_deal_dealcompany_info($deal['deal_id']);
  } else if ($action == 'duplicate') {
    $deal_q = run_query_deal_detail($deal['deal_id']);
    $deal['company_id'] = $deal_q->f('deal_company_id');
    $users = array($obm['uid'], $deal_q->f('deal_marketingmanager_id'), $deal_q->f('deal_technicalmanager_id'), );
    $dc = get_deal_dealcompany_info($deal['deal_id']);
  } else if ($action == 'update') {
    $users = array($obm['uid'], $deal['market'], $deal['tech']);
    $deal_q = '';
  } else {
    $deal_q = '';
    $users = array($obm['uid']);
    $dc = array();
  }
  
  if (($action == 'new') && ($deal['lead_id'] > 0)) {
    $lead = get_lead_info($deal['lead_id']);
    $deal['company_id'] = $lead['company_id'];
    $deal['contact1'] = $lead['contact_id'];
    $deal['market'] = $lead['manager'];
    $deal['source_id'] = $lead['source_id'];
    $deal['label'] = $lead['name'];
    $deal['datebegin'] = $lead['date'];
    $deal['datealarm'] = $lead['datealarm'];
    $deal['todo'] = $lead['todo'];
    $deal['comment'] = $lead['comment'];
  }

  if (($action == 'new') || ($action == 'insert')) {
    $comp_q = run_query_deal_company_info($deal['company_id']);
  } else {
    $comp_q = '';
  }

  $types = get_global_deal_types('dealtype_label');
  $tts = get_global_tasktype($ctt_sales);
  $sources = get_global_lead_sources();
  $users_c = of_usergroup_get_group_users($obm['group_com'], true, $users, 'userobm_lastname');
  $users_p = of_usergroup_get_group_users($obm['group_prod'], true, $users, 'userobm_lastname');
  $sta_q = run_query_deal_status_list();
  $con_q = run_query_deal_contact($deal['company_id']);
  $regions = of_category_get_ordered('', 'region');
  $roles = of_category_get_ordered('DealCompany', 'role');

  $block = html_deal_form($action, $deal_q, $types, $regions, $tts, $sources, $users_c, $users_p, $comp_q, $con_q, $dc, $roles, $sta_q, $deal);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Deal Form                   
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $types     : Deal Types list
//   - $regions   : Regions array
//   - $tts       : TaskType array
//   - $sources   : Lead Sources array
//   - $users_c   : array list of userobm from domain group Commercial
//   - $users_p   : array list of userobm from domain group Production
//   - $comp_q    : DBO : deal company infos (name, ad1, zip, town when new)
//   - $con_q     : DBO : list of contact from the company 
//   - $dc        : array of related companies
//   - $roles     : array of DealCompanyRoles
//   - $sta_q     : DBO : list of deal status
//   - $comp_id   : id of the company concerned by the deal
//   - deal[]     : default or transmitted values
///////////////////////////////////////////////////////////////////////////////
function html_deal_form($action,$deal_q,$types,$regions,$tts,$sources, $users_c,$users_p,$comp_q, $con_q, $dc, $roles, $sta_q, $deal) {
  global $ico_company,$ico_contact,$ico_add, $l_header_new_f, $l_aduplicate;
  global $c_php_isodate_format,$l_deal,$l_update,$l_insert, $l_private;
  global $l_dates, $l_company, $l_other_relation, $l_relation, $l_label;
  global $l_number, $l_type, $l_region, $l_todo;
  global $l_tt, $l_marketing_manager, $l_technical_manager, $l_commission_p;
  global $l_amount, $l_margin, $l_hitrate, $l_contact, $l_status, $l_archive;
  global $l_begin, $l_end, $l_proposal, $l_expected, $l_alarm, $l_source;
  global $l_comment,$l_add_comment,$l_upd_comment, $l_mail_comment;
  global $l_no, $l_members, $l_group;
  global $path, $display, $action, $obm, $popup;
  global $c_all, $l_all, $c_undef, $l_undef;

  $ext_target = $deal['ext_target'];
  $uid = $obm['uid'];
  $comp_id = $deal['company_id'];

  // if update mode and first time, values are taken from db
  if (($action == 'detailupdate') || ($action == 'duplicate')) {
    $id = $deal_q->f('deal_id');
    $pid = $deal_q->f('deal_parentdeal_id');
    $usercreate = $deal_q->f('deal_usercreate');
    $label = $deal_q->f('deal_label');
    $number = $deal_q->f('deal_number');
    $datebegin = of_date_upd_format($deal_q->f('datebegin'), true);
    $dateend = of_date_upd_format($deal_q->f('dateend'), true);
    $dateprop = of_date_upd_format($deal_q->f('dateproposal'), true);
    $dateexpected = of_date_upd_format($deal_q->f('dateexpected'), true);
    $datealarm = of_date_upd_format($deal_q->f('datealarm'), true);
    $type = $deal_q->f('deal_type_id');
    $region = $deal_q->f('deal_region_id');
    $tt = $deal_q->f('deal_tasktype_id');
    $priv = ($deal_q->f('privacy') == 0 ? '0' : '1');
    $market = $deal_q->f('deal_marketingmanager_id');
    $tech = $deal_q->f('deal_technicalmanager_id');
    $source_id = $deal_q->f('deal_source_id');
    $source = $deal_q->f('deal_source');
    $commission = $deal_q->f('deal_commission');
    $amount = $deal_q->f('deal_amount');
    $margin = $deal_q->f('deal_margin');
    $hitrate = $deal_q->f('deal_hitrate');
    $con1 = $deal_q->f('deal_contact1_id');
    $con2 = $deal_q->f('deal_contact2_id');
    $status = $deal_q->f('deal_status_id');
    $archive = ($deal_q->f('deal_archive') == 1 ? ' checked' : '');
    $todo = $deal_q->f('deal_todo');
    $comment = $deal_q->f('deal_comment');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $comp = $deal_q->f('company_name');
    $ad1 = $deal_q->f('company_address1');
    $zip = $deal_q->f('company_zipcode');
    $town = $deal_q->f('company_town');
    if ($action == 'detailupdate') {
      $dis_title = $label;
    } else if ($action == 'duplicate') {
      $dis_title = "$label ($l_aduplicate)";
    }
  } elseif (($action == 'new') || ($action == 'insert')) {
    $comp = $comp_q->f('company_name');
    $ad1 = $comp_q->f('company_address1');
    $zip = $comp_q->f('company_zipcode');
    $town = $comp_q->f('company_town');
    $market = $comp_q->f('company_marketingmanager_id');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    // We pre-fill datebegin and datealarm with current date
    $datebegin = of_date_upd_format(); 
    $datealarm = $datebegin;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($deal['deal_id'])) { $id = $deal['deal_id']; }
  if (isset($deal['parentdeal_id'])) { $pid = $deal['parentdeal_id']; }
  if (isset($deal['usercreate'])) { $usercreate = $deal['usercreate']; }
  if (isset($deal['number'])) { $number = stripslashes($deal['number']); }
  if (isset($deal['label'])) { $label = stripslashes($deal['label']); }
  if (isset($deal['privacy'])) { $priv = $deal['privacy']; }
  if (isset($deal['datebegin'])) { $datebegin = of_date_upd_format(stripslashes($deal['datebegin']),true); }
  if (isset($deal['dateend'])) { $dateend = of_date_upd_format(stripslashes($deal['dateend']),true); }
  if (isset($deal['dateproposal'])) { $dateprop = of_date_upd_format(stripslashes($deal['dateproposal']),true); }
  if (isset($deal['dateexpected'])) { $dateexpected = of_date_upd_format(stripslashes($deal['dateexpected']),true); }
  if (isset($deal['datealarm'])) { $datealarm = of_date_upd_format(stripslashes($deal['datealarm']),true); }
  if (isset($deal['type_id'])) { $type = $deal['type_id']; }
  if (isset($deal['region'])) { $region = $deal['region']; }
  if (isset($deal['tasktype'])) { $tt = $deal['tasktype']; }
  if (isset($deal['market'])) { $market = $deal['market']; }
  if (isset($deal['tech'])) { $tech = $deal['tech']; }
  if (isset($deal['source_id'])) { $source_id = $deal['source_id']; }
  if (isset($deal['source'])) { $source = $deal['source']; }
  if (isset($deal['commission'])) { $commission = $deal['commission']; }
  if (isset($deal['amount'])) { $amount = stripslashes($deal['amount']); }
  if (isset($deal['margin'])) { $margin = stripslashes($deal['margin']); }
  if (isset($deal['hitrate'])) { $hitrate = stripslashes($deal['hitrate']); }
  if (isset($deal['contact1'])) { $con1 = $deal['contact1']; }
  if (isset($deal['contact2'])) { $con2 = $deal['contact2']; }
  if (isset($deal['status_id'])) { $status = $deal['status_id']; }
  if (isset($deal['timeupdate'])) { $timeupdate = $deal['timeupdate']; }
  if (isset($deal['archive'])) { $archive = ($deal['archive'] == 1 ? 'checked' : ''); }
  if (isset($deal['todo'])) { $todo = stripslashes($deal['todo']); }
  if (isset($deal['comment'])) { $comment = stripslashes($deal['comment']); }
  if (isset($deal['add_comment'])) { $add_comment = stripslashes($deal['add_comment']); }
  if (isset($deal['usercomment'])) { $usercomment = $deal['usercomment']; }
  if (isset($deal['datecomment'])) { $datecomment = of_date_upd_format($deal['datecomment']); }
  if (isset($deal['mail_comment'])) { $mail_comment = $deal['mail_comment']; }
  if (isset($deal['company_name'])) { $comp = stripslashes($deal['company_name']); }
  if (isset($deal['company_ad1'])) { $ad1 = stripslashes($deal['company_ad1']); }
  if (isset($deal['company_zip'])) { $zip = stripslashes($deal['company_zip']); }
  if (isset($deal['company_town'])) { $town = stripslashes($deal['company_town']); }
  if (isset($deal['ext_target'])) { $ext_target = $deal['ext_target']; }

  // User defined data
  $block_userdata = of_userdata_dis_entity_form('deal', $id, $deal);
 
  // Type select construction
  $sel_type = "<select name=\"sel_type_id\">
     <option value=\"$c_undef\">$l_undef</option>";  
  foreach($types as $t_id => $one_type) {
    $tlabel = $one_type['label'];
    $sel_type .= "<option value=\"$t_id\"";
    if ($type == $t_id) $sel_type .= " selected = \"selected\"";
    $sel_type .= ">$tlabel</option>";
  }
  $sel_type .= '</select>';

  $sel_region = of_category_html_select('', 'region', $regions, $region, '');

  // Sources select
  $sel_source = "
    <select name=\"sel_source_id\">
     <option value=\"$c_undef\">$l_undef</option>\n";
  if (is_array($sources) && (count($sources) > 0)) {
    foreach($sources as $s_id => $one_source) {
      $slabel = $one_source['label'];
      $sselect = ($s_id == $source_id) ? 'selected' : '';
      $sel_source .= "<option value=\"$s_id\" $sselect>$slabel</option>";
    }
  }
  $sel_source .= '</select>';

  // Task type select construction
  $sel_tt = "<select name=\"sel_tasktype\">
    <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($tts)) {
    foreach ($tts as $tt_id=>$one_tt) {
      $tt_label = $one_tt['label'];
      $internal = $one_tt['internal'];
      $sel_tt .= "\n<option value=\"$tt_id\"";
      if ($tt == $tt_id) $sel_tt .= "selected =\"selected\"";
      $sel_tt .= ">$tt_label</option>\n";
    }
    $sel_tt .= '</select>';
  }
 
  // Marketing manager select construction
  $sel_market = "<select name=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  foreach ($users_c as $cid => $one_user) {
    $cname = $one_user['lastname'] . ' ' . $one_user['firstname'];
    $sel_market .= "\n<option value=\"$cid\"";
    if ($market == $cid) $sel_market .= " selected = \"selected\""; 
    $sel_market .= ">$cname</option>";
  }
  $sel_market .= '</select>';

  // Technical manager select construction
  $sel_tech = "<select name=\"sel_tech\">
    <option value=\"$c_undef\">$l_undef</option>";
  foreach ($users_p as $cid => $one_user) {
    $cname = $one_user['lastname'] . ' ' . $one_user['firstname'];
    $sel_tech .= "<option value=\"$cid\"";
    if ($tech == $cid) $sel_tech .= " selected = \"selected\""; 
    $sel_tech .= ">$cname</option>\n";
  }
  $sel_tech .= '</select>';

  // contact 1 select construction
  $sel_con1 = "<select name=\"sel_contact1\">";
  if(is_object($con_q)){
    while ($con_q->next_record()) {
      $cid = $con_q->f('contact_id');
      $cinfo = $con_q->f('contact_lastname') . ' ' .
               $con_q->f('contact_firstname') . ' - ' .
               $con_q->f('contact_phone');
      $sel_con1 .= "<option value=\"$cid\"";
      if ($con1 == $cid) $sel_con1 .= "  selected = \"selected\""; 
      $sel_con1 .= ">$cinfo</option>\n";
    }
   }
  $sel_con1 .= '</select>';

  // contact 2 select construction
  if(is_object($con_q)){
      if ($con_q->nf()>0) {
      $con_q->seek(0);
    }
  }
  $sel_con2 = "<select name=\"sel_contact2\">";
  if (is_object($con_q)){
    while ($con_q->next_record()) {
      $cid = $con_q->f('contact_id');
      $cinfo = $con_q->f('contact_lastname') . ' ' .
               $con_q->f('contact_firstname') . ' - ' .
               $con_q->f('contact_phone');
      $sel_con2 .= "<option value=\"$cid\"";
      if ($con2 == $cid) $sel_con2 .= " selected = \"selected\""; 
      $sel_con2 .= ">$cinfo</option>\n";
    }
  }
  $sel_con2 .= '</select>';

  $cpt = 0;
  // Others companies relations
  if ( (is_array($dc)) && (count($dc) > 0) ) {
    foreach ($dc as $cpt => $one_dc) {
      $dc_cid = $one_dc['company_id'];
      $dc_cname = $one_dc['company_name'];
      $dc_roleid = $one_dc['role_id'];
      $dis_role = of_category_html_select('DealCompany', "role$cpt", $roles, $dc_roleid, 'none');
      $dis_dc .= "
      <div>
      $dis_role
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$dc_cid") . "\">$dc_cname</a>
      <input type=\"hidden\" name=\"data_dc_$cpt\" value=\"$cpt\" />
      <input type=\"hidden\" name=\"data_dccid_$cpt\" value=\"$dc_cid\" />
      <input type=\"hidden\" name=\"data_dccname_$cpt\" value=\"$dc_cname\" />
      <input type=\"hidden\" name=\"data_dccnewid_$cpt\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"popup('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.data_dccnewid_$cpt&amp;ext_widget_text=f_entity.data_dccnewname_$cpt','Company'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"data_dccnewname_$cpt\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </div>";
    }
    $cpt++;
  }
  $dis_role = of_category_html_select('DealCompany', "role$cpt", $roles, '', 'none');
  $dis_dc .= "
      <div>
      $dis_role
      <input type=\"hidden\" name=\"data_dc_$cpt\" value=\"$cpt\" />
      <input type=\"hidden\" name=\"data_dccid_$cpt\" value=\"\" />
      <input type=\"hidden\" name=\"data_dccname_$cpt\" value=\"\" />
      <input type=\"hidden\" name=\"data_dccnewid_$cpt\" value=\"\" />
      <a href=\"\" onclick=\"popup('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.data_dccnewid_$cpt&amp;ext_widget_text=f_entity.data_dccnewname_$cpt','Company'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"data_dccnewname_$cpt\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </div>";


  // status select construction
  $sel_status = "<select name=\"sel_status_id\" onChange=\"auto_set_hitrate(this, this.form.tf_hitrate,this.selectedIndex);\">";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f('dealstatus_id');
    $slabel = $sta_q->f('dealstatus_label');
    $sel_status .= "<option value=\"$sid\"";
    if ($status == $sid) $sel_status .= " selected = \"selected\""; 
    $sel_status .= ">$slabel</option>\n";
  }
  $sel_status .= '</select>';
  if($sta_q->nf() > 0) {
    $sta_q->seek(0);
  }
  while ($sta_q->next_record()) {
    $sid = $sta_q->f('dealstatus_id');
    $shitrate = $sta_q->f('dealstatus_hitrate');
    if (($shitrate != '') && (($shitrate >= 0) && ($shitrate <= 100))) {
      $js_status_loop .= "
        if (select.options[pos].value == $sid) {
          targetField.value = $shitrate;
        }";
    }
  }

  $js_status = "
<script type=\"text/javascript\">
<!--
function auto_set_hitrate(select, targetField, pos) {
$js_status_loop

  return true;
}
-->
</script>
";

// User comment select construction
  $sel_usercomment .= "<select name=\"sel_usercomment\">";
  foreach ($users_c as $cid => $one_user) {
    $cname = $one_user['lastname'].' '.$one_user['firstname'];
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= '</select>';

  // Mail comment radio
  if (($mail_comment == '') || ($mail_comment == "$l_no")) {
    $rd_mail_no_c = 'checked';
  } else if ($mail_comment == "$l_members") {
    $rd_mail_members_c = 'checked';
  } else if ($mail_comment == "$l_group") {
    $rd_mail_group_c = 'checked';
  }

  //  if (($action == 'detailupdate') || ($action == 'update')) {
  if ($comment != '') {
    $dis_comment = "
    $l_upd_comment
    <textarea name=\"ta_comment\" rows=\"20\" cols=\"78\">$comment</textarea>";
  }

  if ( ($action == 'new') || ($action == 'insert') ||
       ( (($action == 'detailupdate') || ($action == 'update')) && 
         ($usercreate == $uid) ) ) {
     $dis_button1 = "
      <input type=\"hidden\" name=\"lead_id\" value=\"$deal[lead_id]\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert \" />";
    $dis_priv = "
      <th><label for=\"lbl_priv\">$l_private</label></th>
      <td><input id=\"lbl_priv\" name=\"cba_privacy\" type=\"checkbox\" value=\"1\" ";
    if ($priv == 1) $dis_priv .= "checked = \"checked\"";
    $dis_priv .= ' /></td></tr><tr>';
  }

  if ($action == 'detailupdate') {
    $dis_contact_2 = "<a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con2")."\">
             <img src=\"$ico_contact\" alt=\"[details]\" />
             </a>";
    $dis_contact_1 = "<a href=\"" . url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con1")."\">
             <img src=\"$ico_contact\" alt=\"[details]\" />
             </a>";
  }
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button1 = "<input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"parentdeal_id\" value=\"$pid\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"deal_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";
  } else {
    $dis_button1 = "<input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"lead_id\" value=\"$deal[lead_id]\" />
      <input type=\"hidden\" name=\"parentdeal_id\" value=\"$pid\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display['title'] = $dis_title;

  $block = "
  $js_status
  <form method=\"post\" name=\"f_entity\" 
    onsubmit=\"if (check_form(this)) return true; else return false;\"
    action=\"".url_prepare('deal_index.php')."\">

  <fieldset class=\"detail infos\">
  <legend>$l_relation</legend>
    <table>
    <tr>
      <th>
      $l_company
      </th>
      <td>
        <a href=\"" . url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id") . "\">
        <img src=\"$ico_company\" alt=\"[details]\" /></a>
        $comp
        $ad1
        <br />$zip $town 
      </td>
    </tr><tr>
      <th>$l_contact 1</th>
      <td>$dis_contact_1 $sel_con1</td>
    </tr><tr>
      <th>$l_contact 2</th>
      <td>$dis_contact_2 $sel_con2</td>
    </tr>
    <tr>
      <th>$l_other_relation <a href=\"\" onclick=\"add_DC_line();return false\">+</a></th>
      <td id=\"dc_container\">$dis_dc</td>
    </tr>
    </table>
    </fieldset>
    
  <fieldset class=\"detail infos\">
  <legend>$l_deal</legend>

    <table>
    <tr>
      $dis_priv
      <th>$l_label</th>
      <td><input name=\"tf_label\" type=\"text\" maxlength=\"128\" size=\"40\" value=\"$label\" /></td>
      </tr><tr>
        <th>$l_number</th>
        <td><input type=\"text\" name=\"tf_number\" size=\"40\" maxlength=\"32\" value=\"$number\" /></td>
    </tr><tr>
      <th>$l_type</th>
      <td>$sel_type</td>
    </tr>
    <tr>
      <th>$l_tt</th>
      <td>$sel_tt</td>
    </tr>
    <tr>
      <th>$l_region</th>
      <td>$sel_region</td>
    </tr>
    <tr>
      <th>$l_source</th>
      <td>$sel_source <input type=\"text\" name=\"tf_source\" size=\"40\" maxlength=\"64\" value=\"$source\" /></td>
    </tr>
    <tr>
      <th>$l_commission_p</th>
      <td><input name=\"tf_commission\" type=\"text\" size=\"5\" value=\"$commission\" />%</td>
    </tr>
    <tr>
      <th>$l_marketing_manager</th>
      <td>$sel_market</td>
    </tr>
    <tr>
      <th>$l_technical_manager</th>
      <td>$sel_tech</td>
    </tr>
    </table>
    </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_dates</legend>

    <table>
    <tr>
      <th>$l_begin</th>
      <td><input type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_proposal</th>
      <td><input type=\"text\" name=\"tf_dateproposal\" value=\"$dateprop\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_expected</th>
      <td><input type=\"text\" name=\"tf_dateexpected\" value=\"$dateexpected\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_end</th>
      <td>
      <input type=\"text\" name=\"tf_dateend\" value=\"$dateend\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_amount</th>
      <td><input name=\"tf_amount\" type=\"text\" size=\"10\" value=\"$amount\" /></td>
    </tr><tr>
      <th>$l_margin</th>
      <td><input name=\"tf_margin\" type=\"text\" size=\"10\" value=\"$margin\" /></td>
    </tr><tr>
      <th>$l_hitrate</th>
      <td><input name=\"tf_hitrate\" type=\"text\" size=\"3\" value=\"$hitrate\" />%</td>
    </tr>
     $block_userdata
    </table>
   </fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_status</legend>

    <table>
    <tr>
      <th>$l_alarm</th>
      <td><input type=\"text\" name=\"tf_datealarm\" value=\"$datealarm\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_status</th>
      <td>$sel_status</td>
    </tr><tr>
      <th>$l_todo</th>
      <td><input type=\"text\" name=\"tf_todo\" size=\"60\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr>
    <tr>
      <th><label for=\"lbl_arch\">$l_archive</label></th>
      <td><input type=\"checkbox\" id=\"lbl_arch\" name=\"cba_archive\" value=\"1\" $archive /></td>
    </tr>
    </table>
    </fieldset>

   <fieldset class=\"detail extra\">
   <legend>$l_comment</legend>

      $l_add_comment
      <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
      $sel_usercomment
      $l_mail_comment
        <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no</label>
        <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members</label>
        <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group</label>
     <textarea name=\"ta_add_comment\" rows=\"12\" cols=\"78\">$add_comment</textarea>
      $dis_comment
  </fieldset>
 
  <fieldset class=\"buttons\">
    $dis_button1   
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal quick form
// Parameters:
//   - $deal[]    : default or transmitted values
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_quick_form($deal) {
  global $display, $obm, $action;

  if ($deal['deal_id'] > 0) {
    $deal_q = run_query_deal_detail($deal['deal_id']);
    $display['detailInfo'] = display_record_info($deal_q);
    $users_c = of_usergroup_get_group_users($obm['group_com'], true, array($obm['uid']));
    $sta_q = run_query_deal_status_list();
    $block = html_deal_quick_form($action, $deal_q, $users_c, $sta_q, $deal);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Form
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $users_c   : array list of userobm from domain group Commercial
//   - $sta_q     : DBO : list of deal status
//   - deal[]     : default or transmitted values
///////////////////////////////////////////////////////////////////////////////
function html_deal_quick_form($action, $deal_q, $users_c, $sta_q, $deal) {
  global $c_php_isodate_format, $l_update;
  global $l_deal, $l_label;
  global $l_expected, $l_alarm, $l_status, $l_hitrate, $l_todo;
  global $l_comment,$l_add_comment,$l_upd_comment, $l_mail_comment;
  global $l_no, $l_members, $l_group;
  global $path, $display, $action, $obm;

  $uid = $obm['uid'];

  $id = $deal_q->f('deal_id');
  $label = $deal_q->f('deal_label');
  $dis_title = $label;

  // if update mode and first time, values are taken from db
  if ($action == 'quick_detail') {
    $dateexp = of_date_upd_format($deal_q->f('dateexpected'), true);
    $datealarm = of_date_upd_format($deal_q->f('datealarm'), true);
    $status = $deal_q->f('deal_status_id');
    $hitrate = $deal_q->f('deal_hitrate');
    $todo = $deal_q->f('deal_todo');
    $comment = $deal_q->f('deal_comment');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
  } elseif ($action == 'quick_update') {
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($deal['datealarm'])) { $datealarm = of_date_upd_format(stripslashes($deal['datealarm'])); }
  if (isset($deal['dateexpected'])) { $dateexp = of_date_upd_format(stripslashes($deal['dateexpected'])); }
  if (isset($deal['status_id'])) { $status = $deal['status_id']; }
  if (isset($deal['hitrate'])) { $hitrate = stripslashes($deal['hitrate']); }
  if (isset($deal['todo'])) { $todo = stripslashes($deal['todo']); }
  if (isset($deal['comment'])) { $comment = stripslashes($deal['comment']); }
  if (isset($deal['add_comment'])) { $add_comment = stripslashes($deal['add_comment']); }
  if (isset($deal['usercomment'])) { $usercomment = $deal['usercomment']; }
  if (isset($deal['datecomment'])) { $datecomment = of_date_upd_format($deal['datecomment']); }
  if (isset($deal['mail_comment'])) { $mail_comment = $deal['mail_comment']; }
 
  // status select construction
  $sel_status = "<select name=\"sel_status_id\" onChange=\"auto_set_hitrate(this, this.form.tf_hitrate,this.selectedIndex);\">";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f('dealstatus_id');
    $slabel = $sta_q->f('dealstatus_label');
    $sel_status .= "<option value=\"$sid\"";
    if ($status == $sid) $sel_status .= " selected = \"selected\""; 
    $sel_status .= ">$slabel</option>\n";
  }
  $sel_status .= '</select>';
  if ($sta_q->nf() > 0) {
    $sta_q->seek(0);
    while ($sta_q->next_record()) {
      $sid = $sta_q->f('dealstatus_id');
      $shitrate = $sta_q->f('dealstatus_hitrate');
      if (($shitrate != '') && (($shitrate >= 0) && ($shitrate <= 100))) {
        $js_status_loop .= "
          if (select.options[pos].value == $sid) {
            targetField.value = $shitrate;
          }";
      }
    }
  }

  $js_status = "
<script type=\"text/javascript\">
<!--
function auto_set_hitrate(select, targetField, pos) {
$js_status_loop

  return true;
}
-->
</script>
";

  // User comment select construction
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  foreach ($users_c as $cid => $one_user) {
    $cname = $one_user['lastname'].' '.$one_user['firstname'];
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= '</select>';

  // Mail comment radio
  if (($mail_comment == '') || ($mail_comment == "$l_no")) {
    $rd_mail_no_c = 'checked';
  } else if ($mail_comment == "$l_members") {
    $rd_mail_members_c = 'checked';
  } else if ($mail_comment == "$l_group") {
    $rd_mail_group_c = 'checked';
  }

  $display['title'] = $dis_title;
 
  $block = "
    $js_status
    <form method=\"post\" name=\"f_entity\" 
      onsubmit=\"if (check_quick_form(this)) return true; else return false;\"
      action=\"".url_prepare("deal_index.php")."\">

  <fieldset class=\"detail extra\">
  <legend>$l_status</legend>

    <table>
    <tr>
      <th>$l_alarm</th>
      <td><input type=\"text\" name=\"tf_datealarm\" value=\"$datealarm\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_status</th>
      <td>$sel_status</td>
    </tr><tr>
      <th>$l_todo</th>
      <td><input type=\"text\" name=\"tf_todo\" size=\"60\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr><tr>
      <th>$l_hitrate</th>
      <td><input name=\"tf_hitrate\" type=\"text\" size=\"3\" value=\"$hitrate\" /> %</td>
    </tr>
    <tr>
      <th>$l_expected</th>
      <td><input type=\"text\" name=\"tf_dateexp\" value=\"$dateexp\" class=\"datePicker\"/></td>
    </tr>
    </table>
    </fieldset>

  <fieldset class=\"detail extra\">
  <legend>$l_comment</legend>

  $l_add_comment
    <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
    $sel_usercomment
    $l_mail_comment
    <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no</label>
    <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members</label>
    <label><input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group</label>
    <textarea name=\"ta_add_comment\" rows=\"12\" cols=\"78\">$add_comment</textarea>
    $l_upd_comment
    <textarea name=\"ta_comment\" rows=\"20\" cols=\"78\">$comment</textarea>
  </fieldset>

    <fieldset class=\"buttons\">
      <input type=\"hidden\" name=\"action\" value=\"quick_update\" />
      <input type=\"hidden\" name=\"deal_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />
    </fieldset>
    </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the deal delete validation screen
// Parameters:
//   - $p_id : deal id
///////////////////////////////////////////////////////////////////////////////
function dis_deal_can_delete_deal($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare('deal_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"deal_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"deal_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
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
// Form used to affect a deal to a parentdeal chosen in the list of  
// the existing parentdeals.
// Parameters :
//   - $parent_q  : the list of the existing parentdeals
//   - $p_deal_id : the id of the deal we want to affect to a parentdeal
///////////////////////////////////////////////////////////////////////////////
function html_deal_affect($parent_q, $p_deal_id) {
    global $display, $l_affect, $l_deal_select_parentdeal;  

    $sel_parent = "<select name=\"sel_parent\" size=\"10\">";
    while ($parent_q->next_record()) {
	$sel_parent .= "<option value=\"".$parent_q->f('parentdeal_id')."\">".$parent_q->f('parentdeal_label').'</option>';
    }
    $sel_parent .= '</select>';

    $display['msg'] .= display_info_msg($l_deal_select_parentdeal);
    $block = "
      <form name=\"form_affect_deal\" method=\"get\"
        action=\"" . url_prepare("deal_index.php") . "\">

      <fieldset class=\"detail extra\">
      <table>
      <tr>
        <th>
        $sel_parent
        <input type=\"hidden\" name=\"action\" value=\"affect_update\" />
        <input type=\"hidden\" name=\"deal_id\" value=\"$p_deal_id\" />
        </th>
      </tr>
      </table>
      <fieldset>

      <fieldset class=\"buttons\">
        <input type=\"submit\" name=\"sub_affect_deal\" value=\"$l_affect\" />
      </fieldset>
      </form>";

    return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the deal dashboard Index
// Parameters:
//   - $deal : deal hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_deal_dashboard_index($deal) {
  global $display, $path;
  global $l_deal, $l_header_dashboard, $l_potential, $l_year_detail, $l_deal_panel;

  $view = $deal['dash_view'];
  $url_total = url_prepare('deal_index.php?action=dashboard');
  $url_year = url_prepare('deal_index.php?action=dashboard&amp;dash_view=year_detail');
  $url_panel = url_prepare('deal_index.php?action=dashboard&amp;dash_view=deal_panel');

  $display['title'] = $l_header_dashboard;

  $block = "
    <ul>
    <li><a href=\"$url_total\">$l_potential</a></li>
    <li><a href=\"$url_year\">$l_year_detail</a></li>
    <li><a href=\"$url_panel\">$l_deal_panel</a></li>
    </ul>
";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the deal dashboard view
// Parameters:
//   - $deal : deal hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_deal_dashboard_view($deal) {
  global $display;

  $view = $deal['dash_view'];

  if ($view != '') {
    global ${"l_$view"};
    $display['title'] = ${"l_$view"};
    $func = "dis_deal_dashboard_$view";
    $block .= $func($deal);
  } else {
    $block .= dis_deal_dashboard_potential($deal);
  }
 
  return $block;
}


/**
 * Display the deal dashboard : potential
 *
 * @param mixed $deal : deal hash infos
 **/
function dis_deal_dashboard_potential($deal) {
  global $display, $l_deal, $l_module_deal, $l_total, $l_deal_total, $l_margin;
  global $l_forecast, $l_deal_balanced, $l_commission, $l_commission_balanced;
  global $l_marketing_manager, $l_technical_manager, $l_help_deal_forecast;
  global $l_delegation;

  $display['title'] = $l_forecast;

  if (is_array($deal['manager'])) {
    $users_id = $deal['manager'];
  } else if ($deal['manager'] != '') {
    $users_id = array($deal['manager']);
  } else {
    $users_id = '';
  }

  $cpt = 1;
  $potentials = run_query_deal_potential($users_id);

  $old_delegation = 'OBM_first_loop';
  if (is_array($potentials['market'])) {
    foreach($potentials['market'] as $u_id => $pu) {
      if (($u_id != '0') && ($u_id > 0)) {

	$delegation = $pu['delegation'];

	// Delegation total (if changing delegation)
	if (($old_delegation != $delegation) && ($old_delegation != 'OBM_first_loop')) {
	  $del_am = number_format($potentials['market']["$old_delegation"]['amount']);
	  $del_ma = number_format($potentials['market']["$old_delegation"]['margin']);
	  $del_ab = number_format($potentials['market']["$old_delegation"]['amount_balanced']);
	  $del_cm = number_format($potentials['market']["$old_delegation"]['commission']);
	  $del_cb = number_format($potentials['market']["$old_delegation"]['commission_balanced']);
	  $del_nb = $potentials['market']["$old_delegation"]['number'];
	  $dis_market_pot .= "
      <tr>
        <th>$old_delegation</th>
        <th>$l_total</th>
        <th>$del_nb</th>
        <th class=\"money\">$del_am&nbsp;</th>
        <th class=\"money\">$del_ma&nbsp;</th>
        <th class=\"money\">$del_ab&nbsp;</th>
        <th class=\"money\">$del_cm&nbsp;</th>
        <th class=\"money\">$del_cb&nbsp;</th>
      </tr>";

	}
        $cpt++;
        if ($cpt % 2 == 0) {
          $class = "class=\"pair\"";
        } else {
          $class = '';
        }

	// One user entry
	$amount = number_format($pu['amount']);
	$margin = number_format($pu['margin']);
	$balanced = number_format($pu['amount_balanced']);
	$commission = number_format($pu['commission']);
	$commission_balanced = number_format($pu['commission_balanced']);
	$nb_potential = $pu['number'];
	$user = $pu['name'];
	$dis_market_pot .= "
      <tr $class>
        <td>$delegation</td>
        <td>$user</td>
        <td>$nb_potential&nbsp;</td>
        <td class=\"money\">$amount&nbsp;</td>
        <td class=\"money\">$margin&nbsp;</td>
        <td class=\"money\">$balanced&nbsp;</td>
        <td class=\"money\">$commission&nbsp;</td>
        <td class=\"money\">$commission_balanced&nbsp;</td>
      </tr>";

	$old_delegation = $delegation;
      }
    }

    // Last delegation total
    $del_am = number_format($potentials['market']["$old_delegation"]['amount']);
    $del_ma = number_format($potentials['market']["$old_delegation"]['margin']);
    $del_ab = number_format($potentials['market']["$old_delegation"]['amount_balanced']);
    $del_cm = number_format($potentials['market']["$old_delegation"]['commission']);
    $del_cb = number_format($potentials['market']["$old_delegation"]['commission_balanced']);
    $del_nb = $potentials['market']["$old_delegation"]['number'];
    $dis_market_pot .= "
      <tr>
        <th>$old_delegation</th>
        <th>$l_total</th>
        <th>$del_nb</th>
        <th class=\"money\">$del_am&nbsp;</th>
        <th class=\"money\">$del_ma&nbsp;</th>
        <th class=\"money\">$del_ab&nbsp;</th>
        <th class=\"money\">$del_cm&nbsp;</th>
        <th class=\"money\">$del_cb&nbsp;</th>
      </tr>";

    // Marketing totals
    $m_amount = number_format($potentials['market']['0']['amount']);
    $m_margin = number_format($potentials['market']['0']['margin']);
    $m_balanced = number_format($potentials['market']['0']['amount_balanced']);
    $m_commission = number_format($potentials['market']['0']['commission']);
    $m_commission_balanced = number_format($potentials['market']['0']['commission_balanced']);
    $m_nb_potential = $potentials['market']['0']['number'];
  }

  // ---- Technical table

  $old_delegation = 'OBM_first_loop';
  if (is_array($potentials['tech'])) {
    foreach($potentials['tech'] as $u_id => $pu) {
      if (($u_id != '0') && ($u_id > 0)) {

	$delegation = $pu['delegation'];

	// Delegation total (if changing delegation)
	if (($old_delegation != $delegation) && ($old_delegation != 'OBM_first_loop')) {
	  $del_am = number_format($potentials['tech']["$old_delegation"]['amount']);
	  $del_ma = number_format($potentials['tech']["$old_delegation"]['margin']);
	  $del_ab = number_format($potentials['tech']["$old_delegation"]['amount_balanced']);
	  $del_cm = number_format($potentials['tech']["$old_delegation"]['commission']);
	  $del_cb = number_format($potentials['tech']["$old_delegation"]['commission_balanced']);
	  $del_nb = $potentials['tech']["$old_delegation"]['number'];
	  $dis_tech_pot .= "
      <tr>
        <th>$old_delegation</th>
        <th>$l_total</th>
        <th>$del_nb</th>
        <th class=\"money\">$del_am&nbsp;</th>
        <th class=\"money\">$del_ma&nbsp;</th>
        <th class=\"money\">$del_ab&nbsp;</th>
        <th class=\"money\">$del_cm&nbsp;</th>
        <th class=\"money\">$del_cb&nbsp;</th>
      </tr>";

	}
	$cpt++;
        if ($cpt % 2 == 0) {
          $class = "class=\"pair\"";
        } else {
          $class = '';
        }

	// One user entry
	$delegation = $pu['delegation'];
	$amount = number_format($pu['amount']);
	$margin = number_format($pu['margin']);
	$balanced = number_format($pu['amount_balanced']);
	$commission = number_format($pu['commission']);
	$commission_balanced = number_format($pu['commission_balanced']);
	$nb_potential = $pu['number'];
	$user = $pu['name'];
	$dis_tech_pot .= "
      <tr $class>
        <td>$delegation</td>
        <td>$user</td>
        <td>$nb_potential&nbsp;</td>
        <td class=\"money\">$amount&nbsp;</td>
        <td class=\"money\">$margin&nbsp;</td>
        <td class=\"money\">$balanced&nbsp;</td>
        <td class=\"money\">$commission&nbsp;</td>
        <td class=\"money\">$commission_balanced&nbsp;</td>
      </tr>";

	$old_delegation = $delegation;
      }
    }

    // Last delegation total
    $del_am = number_format($potentials['tech']["$old_delegation"]['amount']);
    $del_ma = number_format($potentials['tech']["$old_delegation"]['margin']);
    $del_ab = number_format($potentials['tech']["$old_delegation"]['amount_balanced']);
    $del_cm = number_format($potentials['tech']["$old_delegation"]['commission']);
    $del_cb = number_format($potentials['tech']["$old_delegation"]['commission_balanced']);
    $del_nb = $potentials['tech']["$old_delegation"]['number'];
    $dis_tech_pot .= "
      <tr>
        <th>$old_delegation</th>
        <th>$l_total</th>
        <th>$del_nb</th>
        <th class=\"money\">$del_am&nbsp;</th>
        <th class=\"money\">$del_ma&nbsp;</th>
        <th class=\"money\">$del_ab&nbsp;</th>
        <th class=\"money\">$del_cm&nbsp;</th>
        <th class=\"money\">$del_cb&nbsp;</th>
      </tr>";

    // Tech totals
    $t_amount = number_format($potentials['tech']['0']['amount']);
    $t_margin = number_format($potentials['tech']['0']['margin']);
    $t_balanced = number_format($potentials['tech']['0']['amount_balanced']);
    $t_commission = number_format($potentials['tech']['0']['commission']);
    $t_commission_balanced = number_format($potentials['tech']['0']['commission_balanced']);
    $t_nb_potential = $potentials['tech']['0']['number'];
  }

  $dis_potential = "
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>$l_delegation</th>
      <th>$l_marketing_manager</th>
      <th># $l_module_deal</th>
      <th>$l_deal_total</th>
      <th>$l_margin</th>
      <th>$l_deal_balanced</th>
      <th>$l_commission</th>
      <th>$l_commission_balanced</th>
    </tr>
    </thead>
    $dis_market_pot
    <tfoot>
    <tr>
      <td></td>
      <td>$l_total</td>
      <td>$m_nb_potential&nbsp;</td>
      <td>$m_amount&nbsp;</td>
      <td>$m_margin&nbsp;</td>
      <td>$m_balanced&nbsp;</td>
      <td>$m_commission&nbsp;</td>
      <td>$m_commission_balanced&nbsp;</td>
    </tr>
    </tfoot>
    </table>
    <p />
    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>$l_delegation</th>
      <th>$l_technical_manager</th>
      <th># $l_module_deal</th>
      <th>$l_deal_total</th>
      <th>$l_margin</th>
      <th>$l_deal_balanced</th>
      <th>$l_commission</th>
      <th>$l_commission_balanced</th>
    </tr>
    </thead>
    $dis_tech_pot
    <tfoot>
    <tr>
      <th></th>
      <td>$l_total</td>
      <td>$t_nb_potential&nbsp;</td>
      <td>$t_amount&nbsp;</td>
      <td>$t_margin&nbsp;</td>
      <td>$t_balanced&nbsp;</td>
      <td>$t_commission&nbsp;</td>
      <td>$t_commission_balanced&nbsp;</td>
    </tr>
    </tfoot>
    </table>";
  
  $msg = display_info_msg($l_help_deal_forecast);

  $block = "$msg
    $dis_potential
";

  return $block;
}


/**
 * Display the deal dashboard year detail
 *
 * @param mixed $deal : deal hash infos
 **/
function dis_deal_dashboard_year_detail($deal) {
  global $obm, $display, $path, $ctt_sales;
  global $l_total, $l_date, $l_amount_ht, $l_amount_ttc, $l_margin, $l_region;
  global $l_year_detail, $l_potential, $l_expected, $l_monthsofyearshort;
  global $l_amount, $l_deal_balanced, $l_commission, $l_commission_balanced;
  global $l_execute,$ico_previous,$ico_next;

  $users_id = $deal['manager'];
  if (! isset($deal['submit'])) { 
    $delegation = $obm['delegation'];
  }
  if ($deal['delegation'] != '') {
    $delegation = stripslashes($deal['delegation']);
  }
  $dis_delegation = of_delegation_dis_search_form($delegation);

  $regions = of_category_get_ordered('', 'region');
  $region = $deal['region'];
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  if (isset($deal['year'])) {
    $year = $deal['year'];
  } else {
    $year = date('Y');
  }
  $year_prev = $year - 1;
  $year_next = $year + 1;
  $date_range = array("$year-01-01", "$year-12-31");
  $url_prev = url_prepare("deal_index.php?action=dashboard&amp;dash_view=year_detail&amp;year=$year_prev");
  $url_next = url_prepare("deal_index.php?action=dashboard&amp;dash_view=year_detail&amp;year=$year_next");

  $d = get_deal_dashboard_year_detail($date_range, 'deal_dateexpected', '', $deal['tasktype'], $region, $delegation);

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $deal['tasktype'];
  if (! is_array($tt_ids) && ($tt_ids == '')) {
    $tt_ids = '0';
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, 'tt', $url_tt);

  // Table header
  for ($month = 1; $month < 13; $month++) {
    $label = $l_monthsofyearshort[$month - 1];
    $dis_row_head .= "
      <th>$label</th>";
  }

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $users = $users_id;
  } else {
    $users = $d['users_id'];
  }
  if ((is_array($users)) && (count($users) > 0)) {
    foreach ($users as $m_id) {
      $url_sel_manager .= "&amp;sel_manager[]=$m_id";
    }
  } else if ($users != '') {
    $url_sel_manager .= "&amp;sel_manager[]=$users";
  }

  // prepare columns (no_date, past, monthes 1-12, future)
  $cols = array ('no_date' => 'no_date', 'past' => 'past');
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $cols[$month] = "$year_month";
   }
  $cols['future'] = 'future';
  

  // Loop through users (rows)
  foreach ($users as $u_id) {
    $u_name = $d['name'][$u_id];
    $tam = 0;
    $tma = 0;
    $tab = 0;
    $tca = 0;
    $tcb = 0;

    $dis_user_row = "<tr>
      <th colspan=\"17\">$u_name</th>
    </tr>";
    $dis_amount_row = "<tr class=\"pair\">
      <td>$l_amount</td>";
    $dis_margin_row = "<tr>
      <td>$l_margin</td>";
    $dis_balanced_row = "<tr class=\"pair\">
      <td>$l_deal_balanced</td>";
    $dis_commission_row = "<tr>
      <td>$l_commission</td>";
    $dis_commission_balanced_row = "<tr class=\"pair\">
      <td>$l_commission_balanced</td>";

    // Loop through each cols
    foreach ($cols as $month => $col) {

      if (is_array($d["$col"][$u_id])) {
	$am = $d["$col"][$u_id]['amount'];
	$ma = $d["$col"][$u_id]['margin'];
	$ab = $d["$col"][$u_id]['amount_balanced'];
	$ca = $d["$col"][$u_id]['commission'];
	$cb = $d["$col"][$u_id]['commission_balanced'];

	// 'past', 'future' and 'no date' not in Total
	if (is_numeric($month)) {
	  $tam += $am;
	  $tma += $ma;
	  $tab += $ab;
	  $tca += $ca;
	  $tcb += $cb;
	}

	$dis_am = number_format($am, 2, ',', ' ');
	$dis_ma = number_format($ma, 2, ',', ' ');
	$dis_ab = number_format($ab, 2, ',', ' ');
	$dis_ca = number_format($ca, 2, ',', ' ');
	$dis_cb = number_format($cb, 2, ',', ' ');

	if ($am >= 0) {
	  if ($month  == 'no_date') {
	    $url_date = "&amp;tf_date_before=0000-00-00";
	  } else if ($month == 'past') {
	    $month_start = "$year-01-01";
	    $url_date = "&amp;tf_date_before=$month_start";
	  } else if ($month == 'future') {
	    $month_end = "$year-12-31";
	    $url_date = "&amp;tf_date_after=$month_end";
	  } else {
	    $month_start = "$col-01";
	    $month_end = date('Y-m-d', mktime(0,0,0,$month+1, 0, $year));
	    $url_date = "&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end";
	  }
	  $dis_am = "<a href=\"$path/deal/deal_index.php?action=dashboard_list&amp;sel_manager[]=$u_id&amp;cba_archive=1&amp;sel_region=$region&amp;delegation=$delegation$url_date$url_sel_tt\">$dis_am</a>";
	}

	$dis_amount_row .= "
          <td class=\"money\">$dis_am</td>";
	$dis_margin_row .= "
          <td class=\"money\">$dis_ma</td>";
	$dis_balanced_row .= "
          <td class=\"money\">$dis_ab</td>";
	$dis_commission_row .= "
          <td class=\"money\">$dis_ca</td>";
	$dis_commission_balanced_row .= "
          <td class=\"money\">$dis_cb</td>";
      } else {
	$dis_amount_row .= "<td $class_am> </td>";
	$dis_margin_row .= "<td $class_ma> </td>";
	$dis_balanced_row .= "<td $class_ab> </td>";
	$dis_commission_row .= "<td $class_ca> </td>";
	$dis_commission_balanced_row .= "<td $class_cb> </td>";
      }
    }

    // Total columns
    $dis_tam = number_format($tam, 2, ',', ' ');
    $dis_tma = number_format($tma, 2, ',', ' ');
    $dis_tab = number_format($tab, 2, ',', ' ');
    $dis_tca = number_format($tca, 2, ',', ' ');
    $dis_tcb = number_format($tcb, 2, ',', ' ');

    $month_start = "$year-01-01";
    $month_end = "$year-12-31";
    $url_date = "&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end";

    $dis_tam = "<a href=\"$path/deal/deal_index.php?action=dashboard_list&amp;sel_manager[]=$u_id&amp;cba_archive=1&amp;sel_region=$region&amp;delegation=$delegation$url_date$url_sel_tt\">$dis_tam</a>";

    $dis_amount_row .= "<td class=\"money\">$dis_tam</td>
      </tr>";
    $dis_margin_row .= "<td class=\"money\">$dis_tma</td>
      </tr>";
    $dis_balanced_row .= "<td class=\"money\">$dis_tab</td>
      </tr>";
    $dis_commission_row .= "<td class=\"money\">$dis_tca</td>
      </tr>";
    $dis_commission_balanced_row .= "<td class=\"money\">$dis_tcb</td>
      </tr>";

    $dis_year_detail .= "
      $dis_user_row
      $dis_amount_row
      $dis_margin_row
      $dis_balanced_row
      $dis_commission_row
      $dis_commission_balanced_row";
  }

  // Total row
  $tam = 0;
  $tma = 0;
  $tab = 0;
  $tca = 0;
  $tcb = 0;
  // Loop through each cols
  foreach ($cols as $month => $col) {

    $am = $d[$col][0]['amount'];
    $ma = $d[$col][0]['margin'];
    $ab = $d[$col][0]['amount_balanced'];
    $ca = $d[$col][0]['commission'];
    $cb = $d[$col][0]['commission_balanced'];

    // 'past', 'future' and 'no date' not in Total
    if (is_numeric($month)) {
      $tam += $am;
      $tma += $ma;
      $tab += $ab;
      $tca += $ca;
      $tcb += $cb;
    }

    if ($am > 0) {

      $dis_am = number_format($am, 2, ',', ' ');
      $dis_ma = number_format($ma, 2, ',', ' ');
      $dis_ab = number_format($ab, 2, ',', ' ');
      $dis_ca = number_format($ca, 2, ',', ' ');
      $dis_cb = number_format($cb, 2, ',', ' ');
    
      if ($month  == 'no_date') {
	$url_date = "&amp;tf_date_before=0000-00-00";
      } else if ($month == 'past') {
	$month_start = "$year-01-01";
	$url_date = "&amp;tf_date_before=$month_start";
      } else if ($month == 'future') {
	$month_end = "$year-12-31";
	$url_date = "&amp;tf_date_after=$month_end";
      } else {
	$month_start = "$col-01";
	$month_end = date('Y-m-d', mktime(0,0,0,$month+1, 0, $year));
	$url_date = "&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end";
      }
      $dis_am = "<a href=\"$path/deal/deal_index.php?action=dashboard_list$url_sel_manager&amp;cba_archive=1&amp;sel_region=$region&amp;delegation=$delegation$url_date$url_sel_tt\">$dis_am</a>";
    } else {
      $dis_am = '';
      $dis_ma = '';
      $dis_ab = '';
      $dis_ca = '';
      $dis_cb = '';
    }

    $dis_total_row .= "
      <td class=\"money\">$dis_am</td>";
    $dis_total_margin_row .= "
      <td class=\"money\">$dis_ma</td>";
    $dis_total_balanced_row .= "
      <td class=\"money\">$dis_ab</td>";
    $dis_total_commission_row .= "
      <td class=\"money\">$dis_ca</td>";
    $dis_total_commission_balanced_row .= "
      <td class=\"money\">$dis_cb</td>";
  }
  $dis_tam = number_format($tam, 2, ',', ' ');
  $dis_tma = number_format($tma, 2, ',', ' ');
  $dis_tab = number_format($tab, 2, ',', ' ');
  $dis_tca = number_format($tca, 2, ',', ' ');
  $dis_tcb = number_format($tcb, 2, ',', ' ');
  $dis_total_row .= "
      <td class=\"money\">&nbsp;$dis_tam</td>";
  $dis_total_margin_row .= "
      <td class=\"money\">&nbsp;$dis_tma</td>";
  $dis_total_balanced_row .= "
      <td class=\"money\">&nbsp;$dis_tab</td>";
  $dis_total_commission_row .= "
      <td class=\"money\">&nbsp;$dis_tca</td>";
  $dis_total_commission_balanced_row .= "
      <td class=\"money\">&nbsp;$dis_tcb</td>";

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $am = $d["$year_month"]['0']['amount'];
    $ab = $d["$year_month"]['0']['amount_balanced'];
    $values[$month-1] = $am;
    $labels[$month-1] = number_format($am/1000, 0, ',', ' ');
    $xlabels[$month-1] = $l_monthsofyearshort[$month-1];
    $values_ab[$month-1] = $ab;
    $labels_ab[$month-1] = number_format($ab/1000, 0, ',', ' ');
  }

  $title = "$l_potential $year";
  $plots['new_bar'] = array(1, 1);
  $plots['legends'] = array($l_amount, $l_deal_balanced);
  $plots['values'] = array($values, $values_ab);
  $plots['labels'] = array($labels, $labels_ab);

  $chart_pot_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots))."&amp;xlabels=".urlencode(serialize($xlabels));

  $url_form = url_prepare('deal_index.php');

  $block = "
    <form class=\"search\" method=\"post\" name=\"f_search\" action=\"$url_form\">
    <fieldset class=\"detail\">
    <legend>$l_year_detail</legend>
      <label>$l_date<br />
      <a href=\"javascript: return false;\"
          onclick=\"yearfield=\$('year');
                    newyear=Number(yearfield.innerHTML)-1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
      <img src=\"$ico_previous\" alt=\"\" /></a>
        <span id=\"year\">$year</span>
      <a href=\"javascript: return false;\"
          onclick=\"yearfield=\$('year');
                    newyear=Number(yearfield.innerHTML)+1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img src=\"$ico_next\" alt=\"\" /></a>
      </label>
      $block_region
      $dis_delegation
      $block_tt
      </fieldset>
      <fieldset class=\"buttons\">
        <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"year\" value=\"$year\" />
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
        <input name=\"dash_view\" id=\"dash_view\" type=\"hidden\" value=\"year_detail\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
      </fieldset>
    </form>

    <h1>$l_potential - $l_date $l_expected</h1>

    <table class=\"spreadSheet\">
    <colgroup span=\"16\" />
    <colgroup class=\"highlight\" />
    <thead>
    <tr>
      <th> </th>
      <th> ? </th>
      <th> &lt; </th>
      $dis_row_head
      <th> &gt; </th>
      <th>$l_total $year</th>
    </tr>
    </thead>
    $dis_year_detail
    <tr>
      <th colspan=\"17\">----- $l_total -----</th>
    </tr>
    <tr class=\"highlight\">
      <td>$l_total</td>
      $dis_total_row
    </tr>
    <tr class=\"highlight\">
      <td>$l_margin</td>
      $dis_total_margin_row
    </tr>
    <tr class=\"highlight\">
      <td>$l_deal_balanced</td>
      $dis_total_balanced_row
    </tr>
    <tr class=\"highlight\">
      <td>$l_commission</td>
      $dis_total_commission_row
    </tr>
    <tr class=\"highlight\">
      <td>$l_commission_balanced</td>
      $dis_total_commission_balanced_row
    </tr>
    </table>
    <p />

    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_pot_params\" alt=\"\" />
";

  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display The Deal stats detail
// Parameters:
//   - $deal[] : hash values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_dashboard_list($deal) {
  global $display, $obm;
  global $l_module_deal;

  $date_after = $deal['date_after'];
  $date_before = $deal['date_before'];

  if (is_array($deal['manager'])) {
    $users_id = $deal['manager'];
    foreach ($deal['manager'] as $m_id) {
      $url_sel_manager .= "&amp;sel_manager[]=$m_id";
    }
  } else if ($deal['manager'] != '') {
    $users_id = array($deal['manager']);
    $m_id = $deal['manager'];
    $url_sel_manager .= "&amp;sel_manager=$m_id";
  } else {
    $users_id = '';
  }

  $block .= "
    <div class=\"detail extra\">
    <h1>$l_module_deal</h1>
    </div>";

  $deal_q = run_query_deal_dashboard_list($deal, $users_id);
  $prefs = get_display_pref($obm['uid'], 'deal', 0);

  $url = url_prepare("deal_index.php?action=dashboard_list&amp;cba_archive=1&amp;tf_date_after=$date_after&amp;tf_date_before=$date_before$url_sel_manager");
    
  $dis_deal = new OBM_DISPLAY('DATA', $prefs, 'deal');
  $dis_deal->data_set = $deal_q;
  $dis_deal->data_url = $url;
  $dis_deal->data_order = true;
  $dis_deal->data_header = 'both';
  $block .= $dis_deal->display('dis_data_deal');

  return $block;
}


/**
 * Display the deal dashboard deal panel
 *
 * @param mixed $deal : deal hash infos
 **/
function dis_deal_dashboard_deal_panel($deal) {
  global $display, $path, $ctt_sales;
  global $l_total, $l_date, $l_hitrate, $l_marketing_manager, $l_deal;
  global $l_company, $l_amount, $l_amount_ht, $l_margin;
  global $l_deal_panel, $l_region;
  global $l_amount, $l_deal_balanced, $l_commission, $l_commission_balanced;
  global $l_execute,$ico_previous,$ico_next;

  $users_id = $deal['manager'];
  if (! isset($deal['submit'])) { 
    $delegation = $obm['delegation'];
  }
  if ($deal['delegation'] != '') {
    $delegation = stripslashes($deal['delegation']);
  }
  $dis_delegation = of_delegation_dis_search_form($delegation);

  $regions = of_category_get_ordered('', 'region');
  $region = $deal['region'];
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  if (isset($deal['year'])) {
    $year = $deal['year'];
  } else {
    $year = date('Y');
  }
  $year_prev = $year - 1;
  $year_next = $year + 1;
  $date_range = array("$year-01-01", "$year-12-31");
  $d = get_deal_dashboard_panel($date_range, 'deal_dateexpected', '', $deal['tasktype'], $region, $delegation);
  $url_prev = url_prepare("deal_index.php?action=dashboard&amp;dash_view=deal_panel&amp;year=$year_prev");
  $url_next = url_prepare("deal_index.php?action=dashboard&amp;dash_view=deal_panel&amp;year=$year_next");

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $deal['tasktype'];
  if (! is_array($tt_ids) && ($tt_ids == '')) {
    $tt_ids = '0';
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, 'tt', $url_tt);

  // Table header
  $dis_row_head .= "
    <th>$l_hitrate</th>
    <th>$l_marketing_manager</th>
    <th>$l_company</th>
    <th>$l_date</th>
    <th>$l_deal</th>
    <th>$l_amount</th>
    <th>$l_margin</th>";


  // --- XXXXX to check here

  if ((is_array($users_id)) && (count($users_id) > 0)) {
    $users = $users_id;
  } else {
    $users = $d['users_id'];
  }
  if ((is_array($users)) && (count($users) > 0)) {
    foreach ($users as $m_id) {
      $url_sel_manager .= "&amp;sel_manager[]=$m_id";
    }
  } else if ($users != '') {
    $url_sel_manager .= "&amp;sel_manager[]=$users";
  }

  // -----------
  

  // Loop through hitrates
  foreach ($d as $hr => $one_hr) {

    // Skip user name and users_id columns
    if (($hr == 'name') || ($hr == 'total') || ($hr == 'users_id')) {
      continue;
    }

    // Loop through users of the hitrate
    $nb_hr_rows = 0;
    $dis_user_row = '';
    $dis_first_user_row = '';
    foreach ($one_hr as $u_id=>$deals) {

      // Skip total column
      if ($u_id == '0') {
	continue;
      }
      $u_name = $d['name'][$u_id];
      $nb_user_deal = count($deals) - 1;
      $nb_user_rows = $nb_user_deal + 1;
      $nb_hr_rows += $nb_user_rows;

      // Loop through deals of the user of the hitrate
      $dis_first_deal_row = '';
      $dis_deal_rows = '';
      $row = 0;
      foreach ($deals as $d_id=>$one_deal) {

	// Skip user total column
	if ($d_id == 'total') {
	  continue;
	}
	$company = $one_deal['company'];
	$quarter = $one_deal['quarter'];
	$label = $one_deal['label'];
	$amount = number_format($one_deal['amount'], 2, ',', ' ');
	$margin = number_format($one_deal['margin'], 2, ',', ' ');
	if ($row%2 == 1) {
	  $class = "class=\"pair\"";
	} else {
	  $class = '';
	}
	$row++;

	// First deal row : table row already initiated
	if ($dis_first_deal_row == '') {
	  $dis_first_deal_row = "<td>$company</td>
        <td>$quarter</td>
        <td>$label</td>
        <td class=\"money\">$amount</td>
        <td class=\"money\">$margin</td>";
	} else {
	  $dis_deal_rows .= "<tr $class>
        <td>$company</td>
        <td>$quarter</td>
        <td>$label</td>
        <td class=\"money\">$amount</td>
        <td class=\"money\">$margin</td>
      </tr>";
	}
      }
      // First user row : table row already initiated
      $total_d_am = number_format($deals['total']['amount'], 2, ',', ' ');
      $total_d_mar = number_format($deals['total']['margin'], 2, ',', ' ');
      if ($dis_first_user_row == '') {
	$dis_first_user_row = "
        <td rowspan=\"$nb_user_deal\">$u_name</td>
        $dis_first_deal_row
        </tr>
        $dis_deal_rows
        <tr class=\"highlight\">
          <td colspan=\"4\">$u_name</td>
          <td class=\"money\">$total_d_am</td>
          <td class=\"money\">$total_d_mar</td>
        </tr>";
      } else {
	$dis_user_row .= "<tr>
        <td rowspan=\"$nb_user_deal\">$u_name</td>
        $dis_first_deal_row
        </tr>
        $dis_deal_rows
        <tr class=\"highlight\">
          <td colspan=\"4\">$u_name</td>
          <td class=\"money\">$total_d_am</td>
          <td class=\"money\">$total_d_mar</td>
        </tr>";
      }
    }

    $total_hr_am = number_format($one_hr[0]['amount'], 2, ',', ' ');
    $total_hr_mar = number_format($one_hr[0]['margin'], 2, ',', ' ');
    $dis_hr_rows .= "<tr>
      <td rowspan=\"$nb_hr_rows\">$hr</td>
      $dis_first_user_row
    </tr>
    $dis_user_row
    <tr>
      <th colspan=\"5\">$l_total $hr %</th>
      <th class=\"money\">$total_hr_am</th>
      <th class=\"money\">$total_hr_mar</th>
    </tr>
";
  }

  $total_amount = number_format($d['total']['amount'], 2, ',', ' ');
  $total_margin = number_format($d['total']['margin'], 2, ',', ' ');

  $url_form = url_prepare('deal_index.php');

  // Synthesis
  foreach ($d as $hr => $one_hr) {

    // Skip user name and users_id columns
    if (($hr == 'name') || ($hr == 'total') || ($hr == 'users_id')) {
      continue;
    }
    $s_am_total = number_format($one_hr['0']['amount'], 2, ',', ' ');
    $s_ma_total = number_format($one_hr['0']['margin'], 2, ',', ' ');
    $s_row .= "<tr>
      <td>$hr</td>
      <td class=\"money\">$s_am_total</td>
      <td class=\"money\">$s_ma_total</td>
    </tr>";
  }

  $block = "
    <form class=\"search\" method=\"post\" name=\"f_search\" action=\"$url_form\">
    <fieldset class=\"detail\">
    <legend>$l_deal_panel</legend>
      <label>
      <a href=\"javascript: return false;\"
          onclick=\"yearfield=\$('year');
                    newyear=Number(yearfield.innerHTML)-1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
      <img src=\"$ico_previous\" alt=\"\" /></a>
        <span id=\"year\">$year</span>
      <a href=\"javascript: return false;\"
          onclick=\"yearfield=\$('year');
                    newyear=Number(yearfield.innerHTML)+1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img src=\"$ico_next\" alt=\"\" /></a>
      </label>
      $block_region
      $dis_delegation
      <label>
      $block_tt
      </label>
      </fieldset>
      <fieldset class=\"buttons\">
        <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"year\" value=\"$year\" />
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
        <input name=\"dash_view\" id=\"dash_view\" type=\"hidden\" value=\"deal_panel\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
      </fieldset>
    </form>

    <h1>$l_deal_panel</h1>

    <table class=\"spreadSheet\">
    <thead>
    <tr>
      <th>$l_hitrate</th>
      <th>$l_amount</th>
      <th>$l_margin</th>
    </tr>
    </thead>
    $s_row
    <tr class=\"highlight\">
      <td>$l_total</td>
      <td class=\"money\">$total_amount</td>
      <td class=\"money\">$total_margin</td>
    </tr>
    </table>
    <p />

    <table class=\"spreadSheet\">
    <thead>
    <tr>
      $dis_row_head
    </tr>
    </thead>
    $dis_hr_rows
    <tr class=\"highlight\">
      <td colspan=\"5\">$l_total</td>
      <td class=\"money\">$total_amount</td>
      <td class=\"money\">$total_margin</td>
    </tr>
    </table>
    <p />
";

  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display Deal Administration Index
///////////////////////////////////////////////////////////////////////////////
function dis_deal_admin_index() {

  $types = get_global_deal_types();
  $status_q = run_query_deal_status_list();

  $block = html_deal_admin_form($types, $status_q);

  $roles = of_category_get_ordered('DealCompany', 'role');
  $block .= of_category_dis_admin_form('role', $roles);

  // User defined data
  $block .= of_userdata_dis_admin_form('deal');

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Deal Administration Forms
// Parametres:
//   - $types    : Deal Type list
//   - $status_q : DBO : Deal Status list
///////////////////////////////////////////////////////////////////////////////
function html_deal_admin_form($types, $status_q) {
  global $l_code, $l_type_manage,$l_type_new,$l_type_exist;
  global $l_status_manage,$l_status_exist,$l_status_new,$l_c_new;
  global $l_type_label,$l_minus,$l_plus,$l_order,$ico_delete,$ico_ok;
  global $l_type_checkdelete, $l_type_update, $l_type_insert;
  global $l_status_checkdelete,$l_status_update,$l_status_insert, $l_hitrate;



  $char_title = '=';
  $key = 0;
  foreach ($types as $id => $one_type) {
    $key++;
    $code = $one_type['code'];
    $inout_plus = ($one_type['inout'])== '+' ? "selected=\"selected\"" : '';
    $inout_minus = ($one_type['inout'])== '-' ? "selected=\"selected\"" : '';
    $label = $one_type['label'];
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $c_label = htmlentities($one_cat['label'], ENT_COMPAT, 'UTF-8');
    $type_list .= "
    <tr $class>
    <td>
      <input type=\"hidden\" id=\"type_$id\" value=\"$id\" />
      <a href=\"?action=type_checklink&amp;category=type&amp;type=$id\">
      <img src=\"$ico_delete\" alt=\"$l_type_checkdelete\" title=\"$l_type_checkdelete\" />
      </a>
    </td>
    <td>
      <select id=\"rd_type_inout_$id\">
      <option value=\"-\" $inout_minus />$l_minus</option>
      <option value=\"+\" $inout_plus />$l_plus</option>
      </select>
    </td>
    <td><input type=\"text\" id=\"tf_code_$id\" size=\"10\" maxlength=\"10\" value=\"$code\" /></td>
    <td><input type=\"text\" id=\"tf_type_label_$id\" value=\"$label\" /></td>
    <td>
      <a href=\"#\" onclick=\"return updateLine($id,'type');\">
      <img src=\"$ico_ok\" alt=\"$l_type_update\" title=\"$l_type_update\" />
      </a>
    </td>
    </tr>";
  }

  $char_title = '=';
  $key = 0;
  while ($status_q->next_record()) {

    $id = $status_q->f('dealstatus_id');
    $order = $status_q->f('dealstatus_order');
    $label = $status_q->f('dealstatus_label');
    $hitrate = $status_q->f('dealstatus_hitrate');
   
    if ($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = '';
    }
    $c_label = htmlentities($one_cat['label'], ENT_COMPAT, 'UTF-8');
    $sta_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"status_$id\" value=\"$id\" />
    <a href=\"?action=status_checklink&amp;category=status&amp;status=$id\">
    <img src=\"$ico_delete\" alt=\"$l_status_checkdelete\" title=\"$l_status_checkdelete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_status_order_$id\" value=\"$order\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_status_label_$id\" value=\"$label\" /></td>
    <td><input type=\"text\" id=\"tf_status_hitrate_$id\" value=\"$hitrate\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'status');\">
    <img src=\"$ico_ok\" alt=\"$l_status_update\" title=\"$l_status_update\" />
    </a>
    </td>
    </tr>";
  }

  $block = "

  <div class=\"detail admin\" >
  <form id=\"form_type_update\" action=\"?action=type_update\" method=\"post\">
  <input type=\"hidden\" name=\"rd_type_inout\" value=\"\" />
  <input type=\"hidden\" name=\"tf_type_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_code\" value=\"\" />
  <input type=\"hidden\" name=\"type\" value=\"\" />
  </form>    
  <h1>$l_type_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
    <td>&nbsp;</td>
    <td>$l_minus / $l_plus</td>
    <td>$l_code</td>
    <td>$l_type_label</td>
    <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $type_list
  </tbody>
  </table>
  </div>  

  <form id=\"form_type_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_type_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>
  <tr>
  <td>
  &nbsp;
  </td>
  <td>$l_code</td>
  <td>$l_type_label</td>
  </tr>
  <tr>
  <td>
  <input type=\"radio\" name=\"rd_type_inout\" value=\"-\" checked=\"checked\"/>$l_minus
  <input type=\"radio\" name=\"rd_type_inout\" value=\"+\" />$l_plus
  </td>
  <td>
  <input type=\"text\" name=\"tf_code\" size=\"10\" maxlength=\"10\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_type_label\" value=\"\" />
  </td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_type_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"type_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"type\" />
  </form>
  </div>




 <div class=\"detail admin\" >
  <form id=\"form_status_update\" action=\"?action=status_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_status_order\" value=\"\" />
  <input type=\"hidden\" name=\"tf_status_label\" value=\"\" />
  <input type=\"hidden\" name=\"tf_status_hitrate\" value=\"\" />
  <input type=\"hidden\" name=\"status\" value=\"\" />
  </form>    
  <h1>$l_status_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_order</td>
  <td>$l_status_exist</td>
  <td>$l_hitrate</td>
  <td>&nbsp;</td>  
  </tr>    
  </thead>
  <tbody>
  $sta_list
  </tbody>
  </table>
  </div>  

  <form id=\"form_status_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_category(this.tf_status_label,'$jslabel')\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>
  $l_order
  </td>
  <td>
  $l_status_new
  </td>
  <td>
  $l_hitrate
  </td> 
  </tr>
  <tr>
  <td>
  <input type=\"text\" name=\"tf_status_order\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_status_label\" value=\"\" />
  </td>
  <td>
  <input type=\"text\" name=\"tf_status_hitrate\" value=\"\" />
  </td>
  </tr>
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_status_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"status_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"status\" />
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the type links
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function dis_deal_type_links($id) {
  global $display, $l_type_link_deal, $l_type_link_deal_no, $l_back;
  global $l_type_can_delete, $l_type_cant_delete, $l_type_delete;

  $delete_ok = true;
  $label = get_deal_type_label($id);
  $obm_q = run_query_deal_type_links($id);

  $nb_type = $obm_q->num_rows();
  if ($nb_type > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1>$label : $l_type_link_deal ($nb_type)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f('deal_id');
      $dlabel = $obm_q->f('deal_label');
      $dis_link = "<li><a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;deal_id=$did") . "\">$dlabel</a></li>";
    }
    if ($cpt < $nb_type) {
      $dis_link .= '<li>...</li>';
    }

  } else {
    $dis_link_head = "<h1>$l_type_link_deal_no</h1>";
  }

  $block = "<div class=\"detail\">
   $dis_link_head
   <ul>
   $dis_link
   </ul>
   </div>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
        action=\"" .url_prepare('deal_index.php') . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_type_can_delete);
    $dis_del = "<form name=\"form_type_delete\".
          method=\"post\" action=\"" . url_prepare('deal_index.php') . "\">
        <input type=\"hidden\" name=\"action\" value=\"type_delete\" />
        <input type=\"hidden\" name=\"sel_type_id\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\" />
        </form>";
    $block .= "
      <div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_type_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the stabtus links
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function dis_deal_status_links($id) {
  global $display, $l_status_link_deal, $l_status_link_deal_no, $l_back;
  global $l_status_can_delete, $l_status_cant_delete, $l_status_delete;

  $delete_ok = true;
  $label = get_deal_status_label($id);
  $obm_q = run_query_deal_status_links($id);

  $nb_stat = $obm_q->num_rows();
  if ($nb_stat > 0) {
    $delete_ok = false;
    $dis_link_head = "<h1>$label : $l_status_link_deal ($nb_stat)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f('deal_id');
      $dlabel = $obm_q->f('deal_label');
      $dis_link.= "<li><a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;deal_id=$did") . "\">$dlabel</a></li>";
    }
    if ($cpt < $nb_stat) {
      $dis_link .= '<li>...</li>';
    }

  } else {
    $dis_link_head = "<h1>$l_status_link_deal_no</h1>";
  }

  $block .= "<div class=\"detail\">
   $dis_link_head
   <ul>
   $dis_link
   </ul>
   </div>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare('deal_index.php') . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display['msg'] .= display_ok_msg($l_status_can_delete);
    $dis_del = "<form name=\"form_type_delete\".
        method=\"post\" action=\"" . url_prepare('deal_index.php') . "\">
      <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
      <input type=\"hidden\" name=\"sel_status_id\" value=\"$id\" />
      <input type=\"submit\" name=\"sub_status\" value=\"$l_status_delete\" />
      </form>";

    $block .= "<div class=\"buttons\">
        $dis_del
        $dis_back
      </div>";
  } else {
    $display['msg'] .= display_warn_msg($l_status_cant_delete);
    $block .= "
      <div class=\"buttons\">
        $dis_back
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal Display preference screen
// Parameters:
//   - $prefs        : Deal Display preferences
//   - $prefs_parent : Parent Deal Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_deal_display_pref($prefs, $prefs_parent) {
  global $l_deal_display, $l_parentdeal_display;
 
  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'deal');
  $dis_pref->pref_title = $l_deal_display;
  $dis_pref->pref_dis_help = 1;
  
  $block = $dis_pref->display();

  $dis_pref->display_pref = $prefs_parent;
  $dis_pref->display_entity = 'parentdeal';
  $dis_pref->pref_title = $l_parentdeal_display;


  $block .= $dis_pref->display();
  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// -- ParentDeal's Section                                                   //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display: Parent Deal search result
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_parentdeal_search_list($deal) {
  global $l_no_found_parent;
  global $display, $obm;

  $obm_q = run_query_deal_search_parentdeal($deal);
  
  $nb_parent = $obm_q->num_rows_total();
  if ($nb_parent == 0) {
    $display['msg'] .= display_warn_msg($l_no_found_parent);
  } else {
    $prefs = get_display_pref($obm['uid'], 'parentdeal');
    $block = html_deal_parentdeal_search_list($obm_q, $prefs, $nb_parent, $deal);
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Parent Deal Search result
// Parameters:
//   - $obm_q        : list of parent deals
//   - $prefs        : fields to display in the list of deals 
//   - $nb_pdeal     : nb parent deals found
//   - $deal[]       : parent deal search criteria
//     keys used     : plabel, pmanager, parchive
// Display the results of the parentdeal search
///////////////////////////////////////////////////////////////////////////////
function html_deal_parentdeal_search_list($obm_q, $prefs, $nb_parentdeals, $deal) {
  global $display, $l_found_parent; 

  $plabel = urlencode(stripslashes($deal['plabel']));
  $pmanager = $deal['pmanager'];
  $parchive = $deal['parchive'];
  $display['msg'] .= display_info_msg($nb_parentdeals.' '.$l_found_parent);

  $url = url_prepare("deal_index.php?action=parent_search&amp;tf_plabel=$plabel&amp;sel_pmanager=$pmanager&amp;cba_parchive=$parchive");

  $parentdeal_d = new OBM_DISPLAY('DATA', $prefs, 'deal', 'parentdeal');
  $parentdeal_d->data_set = $obm_q;
  $parentdeal_d->data_url = $url;
  $parentdeal_d->data_header = 'both';
  $parentdeal_d->data_idfield = 'parentdeal_id';
  $parentdeal_d->data_set->seek($first_row);
  $block = $parentdeal_d->display('dis_data_deal');

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Detail
// Parameters :
//   - deal[]    : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
///////////////////////////////////////////////////////////////////////////////
function dis_deal_parentdeal_consult($deal) {
  global $ctt_sales, $obm;
  
  $archive = $deal['archive'];

  $obm_q = run_query_deal_parentdeal_detail($deal['parentdeal_id']);
  $prefs = get_display_pref ($obm['uid'], 'deal');
  $deal_q = run_query_deal_search($deal, 0);
  $num_rows = $deal_q->num_rows();
  $types = get_global_deal_types('dealtype_label');
  $tts = get_global_tasktype($ctt_sales);
  $status_q = run_query_deal_status_list();
  $usr_q = run_query_deal_manager($archive);

  $block = html_deal_parentdeal_consult($obm_q, $deal_q, $deal, $prefs, $num_rows, $types, $tts, $status_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Parent Deal Detail
// Parameters :
//   - $parent_q : DBO : information about the parent deal
//   - $deal_q   : DBO : parentdeal deal list
//   - deal[]    : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
//   - $prefs    : display preferences for the deal list
//   - $nbdeals  : number of deals in the list
//     //---> used by the deal search form :
//   - $types    : list of deal types available
//   - $tts      : TaskType array
//   - $status_q : list of deal status available 
//   - $usr_q    : DBO : list of userobm
///////////////////////////////////////////////////////////////////////////////
function html_deal_parentdeal_consult($parent_q,$deal_q,$deal,$prefs,$nb_deals,$types,$tts,$status_q,$usr_q) {
  global $display, $path;
  global $l_parentdeal, $l_parentdeal_deals,$l_label,$l_technical_manager;
  global $l_archive,$l_proposal,$l_alarm,$l_amount_in,$l_amount_out;
  global $l_potential_balanced,$l_comment,$l_marketing_manager,$l_status;
  global $l_yes, $l_no, $l_infos, $l_deal_select_company;

  $parent = $parent_q->f('parentdeal_id');
  $plabel = $parent_q->f('parentdeal_label');
  $pmarket = $parent_q->f('parentdeal_marketingmanager_id');
  $ptech = $parent_q->f('parentdeal_technicalmanager_id');
  $pcomment = nl2br($parent_q->f('parentdeal_comment'));
  $archive = ($parent_q->f('parentdeal_archive') == 1 ? $l_yes : $l_no);

  $pmarket_name = $parent_q->f('lname1') . ' ' . $parent_q->f('fname1');
  $ptech_name = $parent_q->f('lname2') . ' ' . $parent_q->f('fname2');
  $p_status = get_deal_parent_status($parent);
  $p_dateproposal = of_date_format(get_deal_parent_dateproposal($parent));
  $p_datealarm = of_date_format(get_deal_parent_datealarm($parent));
  $p_expense = get_deal_parent_expense($parent);
  $p_income = get_deal_parent_income($parent);
  $p_potential = get_deal_parent_potential($parent);

  $block = "
  <div class=\"detail infos\">
    <h1>$l_parentdeal</h1>
    <table>
    <tr>
      <th>$l_label</th>
      <td>$plabel</td>
    </tr>
    <tr>
      <th>$l_marketing_manager</th>
      <td>$pmarket_name</td>
    </tr>
    <tr>
      <th>$l_technical_manager</th>
      <td>$ptech_name</td>
    </tr>
    <tr>
      <th>$l_proposal</th>
      <td>$p_dateproposal</td>
    </tr>
    <tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr>
    </table>
  </div>

  <div class=\"detail infos\">
    <h1>$l_infos</h1>
    <table>
    <tr>
      <th>$l_status :</th>
      <td>$p_status</td>
    </tr>
    <tr>
      <th>$l_alarm</th>
      <td>$p_datealarm</td>
    </tr>
    <tr>
      <th>$l_amount_out</th>
      <td>$p_expense</td>
    </tr>
    <tr>
      <th>$l_amount_in</th>
      <td>$p_income</td>
    </tr>
    <tr>
      <th>$l_potential_balanced</th>
      <td>$p_potential</td>
    </tr>
    </table>
  </div>
     
  <div class=\"detail extra\">
    <h1>$l_comment</h1>
$pcomment
  </div>
";
    
  $block .= display_info_msg($l_parentdeal_deals);

  //----- deal search form 
  $block .= html_deal_search_form($deal, $types, $regions, $tts, $roles, $status_q, $usr_q);

  //-----> deal list
  // if no record found, display it and stop
  if ($nb_deals == 0) {
    return $block;
  }

  $label = urlencode(stripslashes($deal['label']));
  $name = urlencode(stripslashes($deal['company_name']));
  $archive = $deal['archive'];
  $type = $deal['type_id'];
  $tt = $deal['tasktype'];
  $status = $deal['status_id'];
  $manager = $deal['manager'];
  $date_after = stripslashes($deal['date_after']);
  $date_before = stripslashes($deal['date_before']);

  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;cba_archive=$archive&amp;sel_type_id=$type&amp;sel_status_id=$status&amp;sel_tt=$tt&amp;sel_manager=$manager&amp;tf_date_after=$date_after&amp;tf_date_before=$date_before&amp;parentdeal_id=$parent");

  // Display the list of deals attached to the parentdeal
  $dis_list_deal = new OBM_DISPLAY('DATA', $prefs, 'deal');
  $dis_list_deal->data_set = $deal_q;
  $dis_list_deal->data_url = $url;
  $dis_list_deal->data_header = 'both';
  $dis_list_deal->data_idfield = 'deal_id'; 
  $dis_list_deal->data_set->seek($first_row);

  $display['result'] = $dis_list_deal->display('dis_data_deal');

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Form
// Parameters :
//   - $action   : action called
//   - $deal[]   : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
///////////////////////////////////////////////////////////////////////////////
function dis_deal_parentdeal_form($action, $deal) {
  global $display, $ctt_sales, $obm, $l_no_found;

  $users = array();

  if ($deal['parentdeal_id'] > 0) {
    $obm_q = run_query_deal_parentdeal_detail($deal['parentdeal_id']);
    if ($obm_q->num_rows() != 1) {
      $display['msg'] .= display_err_msg($l_no_found);
      return;
    } else {
      $users = array($obm['uid'], $obm_q->f('parentdeal_marketingmanager_id'), $obm_q->f('parentdeal_technicalmanager_id'));
      $display['detailInfo'] = display_record_info($obm_q);
    }
  }

  $users_c = of_usergroup_get_group_users($obm['group_com'], true, $users);
  $users_p = of_usergroup_get_group_users($obm['group_prod'], true, $users);
  $block = html_deal_parentdeal_form($action,$obm_q, $users_c, $users_p, $deal);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Parent Deal Form
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the parent deal (null for new deal)
//   - $users_c   : array user list from domain group Commercial
//   - $users_p   : array user list from domain group Production
//   - deal[]     : default or transmitted values
//     keys used  : parent_id, plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function html_deal_parentdeal_form($action, $deal_q, $users_c, $users_p, $deal) {
  global $l_update_parent,$l_insert_parent,$l_label,$l_marketing_manager;
  global $l_parentdeal,$l_technical_manager,$l_comment, $l_archive;

  // if update mode and first time, values are taken from db
  if (($action == 'parent_detailupdate') || ($action == 'parent_delete')) {
    $parent = $deal_q->f('parentdeal_id');
    $plabel = $deal_q->f('parentdeal_label');
    $pmarket = $deal_q->f('parentdeal_marketingmanager_id');
    $ptech = $deal_q->f('parentdeal_technicalmanager_id');
    $archive = ($deal_q->f('parentdeal_archive') == 1 ? ' checked' : '');
    $pcomment = $deal_q->f('parentdeal_comment');
  } else {
    // Values are taken from parameters
    $parent = $deal['parentdeal_id'];
    $plabel = stripslashes($deal['plabel']);
    $pmarket = $deal['pmarket'];
    $ptech = $deal['ptech'];
    $pcomment = stripslashes($deal['pcom']);
  }

  if (isset($deal['parchive'])) { $archive = ($deal['parchive'] == 1 ? 'checked' : ''); }

  // Marketing manager select
  $sel_market = "<select name=\"sel_pmarket\">";
  if (is_array($users_c)) {
    foreach ($users_c as $cid => $one_user) {
      $sel_market .= "<option value=\"$cid\"";
      if ($cid == $pmarket) $sel_market .= " selected= \" selected \"";
      $sel_market .= '>' . $one_user['lastname'].' '.$one_user['firstname'].'</option>';
    }
  }
  $sel_market .= '</select>';

  // Technical manager select
  $sel_tech = "<select name=\"sel_ptech\">";
  if (is_array($users_p)) {
    foreach ($users_p as $cid => $one_user) {
      $sel_tech .= "<option value=\"$cid\"";
      if ($cid == $ptech) $sel_tech .= "selected= \" selected \"";
      $sel_tech .= '>' . $one_user['lastname'].' '.$one_user['firstname'].'</option>';
    }
  }
  $sel_tech .= '</select>';

  if (($action == 'parent_detailupdate') || ($action == 'parent_update')) {
    $dis_button = "
        <input type=\"hidden\" name=\"action\" value=\"parent_update\" />
        <input type=\"hidden\" name=\"parentdeal_id\" value=\"$parent\" />
        <input type=\"submit\" value=\"$l_update_parent\" />";
  } else {
     $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"parent_insert\" />
      <input type=\"submit\" value=\"$l_insert_parent\" />";
  }

  $block = "
  <form method=\"post\" name=\"f_entity\"
      onsubmit=\"if (check_parentdeal(this)) return true; else return false;\"
      action=\"" . url_prepare('deal_index.php') . "\">
  
  <fieldset class=\"detail extra\">
  <legend>$l_parentdeal</legend>

    <table>
    <tr>
      <th>$l_label</th>
      <td><input name=\"tf_plabel\" type=\"text\" size=\"40\" value=\"$plabel\" /></td>
    </tr><tr>
      <th>$l_marketing_manager</th>
      <td>$sel_market</td>
    </tr><tr>
      <th>$l_technical_manager</th>
      <td>$sel_tech</td>
    </tr>
    <tr>
      <th><label for=\"lbl_arch\">$l_archive</label></th>
      <td><input type=\"checkbox\" id=\"lbl_arch\" name=\"cba_parchive\" value=\"1\" $archive /></td>
    </tr>
    </table>
  </fieldset>

  <fieldset class=\"detail extra\">
  <legend>$l_comment</legend>
  <textarea name=\"ta_pcom\" rows=\"20\" cols=\"72\">$pcomment</textarea>
  </fieldset>  

  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// TEST :: (from company, see view)
// Deprecated will be handled by ajax
// Display the deal list from an externam module
///////////////////////////////////////////////////////////////////////////////
function ext_dis_deal_list($params, $entity, $id) {
  global $display, $obm, $l_no_found;
  global $path;

  $params["$entity"] = $id;
  $block = dis_deal_search_list($params);

  $deal_q = run_query_deal_search($deal, 0);
  $nb_deal = $deal_q->num_rows_total();
  if ($nb_deal == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
    return;
  }

  $prefs = get_display_pref($obm['uid'], 'deal', 0);

  $url = url_prepare("$path/$entity/${entity}_index.php?action=detailconsult&amp;view=deal&amp;param_$entity=$id");

  $deal_d = new OBM_DISPLAY('DATA', $prefs, 'deal');
  $deal_d->data_set = $deal_q;
  $deal_d->data_url = $url;
  $deal_d->data_header = 'both';
  $display['msg'] .= display_info_msg("$nb_deal $l_found");
  $block = $deal_d->display('dis_data_deal');

  return $block;
}


?>
