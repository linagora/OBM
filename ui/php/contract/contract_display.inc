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
// OBM - File : contract_display.php                                         //
//     - Desc : Contract Support Display File                                //
// 2002-01-08 : Mehdi RANDE                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
$fieldnames["contract_label"] = $l_label;
$fieldnames["contract_company_name"] = $l_company;
$fieldnames["contract_number"] = $l_number;
$fieldnames["contracttype_label"] = $l_contract_contracttype;
$fieldnames["contract_priority"] = $l_priority;
$fieldnames["contract_kind"] = $l_contract_kind;
$fieldnames["contract_format"] = $l_format;
$fieldnames["contract_datebegin"] = $l_begin;
$fieldnames["contract_dateexp"] = $l_exp;
$fieldnames["contract_status"] = $l_status;
$fieldnames["contract_techmanager"] = $l_technical_manager;
$fieldnames["contract_marketmanager"] = $l_marketing_manager;
$fieldnames["contract_archive"] = $l_archive_first;


///////////////////////////////////////////////////////////////////////////////
// Display Contract specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_contract(&$OD, $fieldname, $link_ok) {
  global $path,  $contract;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  global $cck_customer, $cck_supplier, $l_cck_customer, $l_cck_supplier;

  $ext_url = $contract["ext_url"];

  if ($fieldname == "contract_label") {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_contract_get_id(".$OD->data_set->f("contract_id").",'".addslashes($OD->data_set->f("contract_label"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_contract_get_id_url('$ext_url',".$OD->data_set->f("contract_id").");";
    } else {
      $res["url"] = "$path/contract/contract_index.php?action=detailconsult&amp;contract_id=".$OD->data_set->f("contract_id");
    }
  }

  else if (($fieldname == "contract_company_name") && $link_ok) {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f("contract_company_id");
  }

  else if ($fieldname == "contract_marketmanager") {
    if ($link_ok) {
      $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("contract_marketmanager_id");
    }
    $res["name"] = $OD->data_set->f("lmarket") . " " . $OD->data_set->f("fmarket");
  }

  else if ($fieldname == "contract_techmanager") {
    if ($link_ok) {
      $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("contract_techmanager_id");
    }
    $res["name"] = $OD->data_set->f("ltech") . " " . $OD->data_set->f("ftech");
  }

  else if ($fieldname == "contract_kind") {
    $kind = $OD->data_set->f($fieldname);
    if ($kind == $cck_customer) {
      $res["name"] = $l_cck_customer;
    } else {
      $res["name"] = $l_cck_supplier;
    }
  }

  else if ($fieldname == "contract_format") {
    $format = $OD->data_set->f($fieldname);
    if ($format == $ccf_period) {
      $res["name"] = $l_ccf_period;
    } else if ($format == $ccf_ticket){
      $res["name"] = $l_ccf_ticket;
    } else {
      $res["name"] = $l_ccf_duration;
    }
  }

  else if ( ($fieldname == "contract_datebegin")
	    || ($fieldname == "contract_dateexp") ) {
    $date = $OD->data_set->f($fieldname);
    $res["name"] = of_date_format($date);
  }

  else if ($fieldname == "contract_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }
 
 else if ($fieldname == "contract_priority") {
    $pri = $OD->data_set->f($fieldname);
    $col = $OD->data_set->f("contractpriority_color");
    $res["name"] = $pri;
    $res["style"] = "style=\"color: #$col;\"";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract search form
// Parameters:
//   - $contract[] 
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_form($contract) {

  $usr_q = run_query_contract_manager();
  $type_q = run_query_contract_type();
  $prio_q = run_query_contract_priority();
  $stat_q = run_query_contract_status();

  $block .= html_contract_search_form($contract, $usr_q, $type_q, $prio_q, $stat_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract search form
// Parameters : 
//   - $contract[] : default form values
//     keys used      : 
//   - $usr_q      : DB result object (userobm list)
//   - $type_q : DB result object (contract kind list)
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_form($contract, $usr_q, $type_q, $pri_q,$sta_q) {
  global $l_number,$l_label_libelle,$l_company,$l_type,$l_after,$l_before;
  global $l_find, $l_manager, $l_priority, $l_archive, $l_status;
  global $cck_customer, $cck_supplier, $l_contract_kind,$l_cck_customer,$l_cck_supplier;
  global $obm, $c_all, $l_all;
 
  $popup = $contract["popup"];
  $status = $contract["status"];
  $priority = $contract["priority"];
  $label = $contract["label"];
  $comp = $contract["company"];
  $type_id = $contract["type"];
  $dateafter = of_date_upd_format($contract["date_after"], true);
  $datebefore = of_date_upd_format($contract["date_before"], true);
  $manager = $contract["manager"];
  $num = $contract["number"]; 
  $archive = ($contract["archive"] == "1" ? "checked=\"checked\"" : ""); 
  $kind = $contract["kind"]; 
  if (($contract['action'] != 'search') && (! isset($contract['delegation']))) { 
    $delegation = $obm['delegation'];
  }
  if ($contract['delegation'] != '') {
    $delegation = stripslashes($contract['delegation']);
  }
  
  $dis_cont_typ  = "<select name=\"sel_type\">
    <option value=\"$c_all\">$l_all</option>";
  while($type_q->next_record()) {
    $dis_cont_typ .= "\n<option value=\"".$type_q->f("contracttype_id")."\"";
    if ($type_id == $type_q->f("contracttype_id")) 
      $dis_cont_typ .= " selected=\"selected\"";
    $dis_cont_typ .= ">" . $type_q->f("contracttype_label")."</option>";
    }
    $dis_cont_typ .= "</select>";

  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  
  $sel_pri = "<select name=\"sel_priority\">
    <option value=\"$c_all\">$l_all</option>";
  while ($pri_q->next_record()) {
    $idp = $pri_q->f("contractpriority_id");
    $labelp = $pri_q->f("contractpriority_label");
    $sel_pri .= "\n<option value=\"$idp\"";
    if ($idp == $priority) 
      $sel_pri .= " selected=\"selected \"";
    $sel_pri .= ">$labelp</option>";
  }
  $sel_pri .= "</select>";

  $sel_sta = "<select name=\"sel_status\">
    <option value=\"$c_all\">$l_all</option>";
  while ($sta_q->next_record()) {
    $iiid = $sta_q->f("contractstatus_id");
    $slabel = $sta_q->f("contractstatus_label");
    $sel_sta .= "\n<option value=\"$iiid\"";
    if ($iiid == $status) 
      $sel_sta .= " selected=\"selected \"";
    $sel_sta .= ">$slabel</option>";
  }
  $sel_sta .= "</select>";

  $sel_kind = "<select name=\"rd_kind\">
              <option value=\"$c_all\">$l_all</option>
              <option value=\"$cck_customer\" ";
  if ($kind === "$cck_customer") {
    $sel_kind .= " selected=\"selected \" ";
  }
  $sel_kind .= ">$l_cck_customer</option>";
  $sel_kind .= "<option value=\"$cck_supplier\" ";
  if ($kind == $cck_supplier) { 
    $sel_kind .= " selected=\"selected \"  ";
  }
  $sel_kind .= " >$l_cck_supplier</option>";
  $sel_kind .= "</select>";

  $sel_manager = "<select name=\"sel_manager\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $u_id = $usr_q->f("userobm_id");
    $sel_manager .= "\n<option value=\"$u_id\"";
    if ($u_id == $manager) 
      $sel_manager .= " selected=\"selected\"";
    $sel_manager .= ">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")." </option>";
  }
  $sel_manager .= "</select>";

  $dis_delegation = of_delegation_dis_search_form($delegation);

  if ($popup) {
    $ext_action = $contract["ext_action"];
    $ext_url = $contract["ext_url"];
    $ext_id = $contract["ext_id"];
    $ext_title = ($contract["ext_title"] ? $contract["ext_title"] : $l_select_contract);
    $ext_target = $contract["ext_target"];
    $ext_widget = $contract["ext_widget"];
    $ext_widget_text = $contract["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">
            <input name=\"popup\" type=\"hidden\" value=\"$popup\" />";
    $display["title"] = $ext_title;
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form class=\"search\" method=\"get\" name=\"f_search\" 
  action=\"".url_prepare("contract_index.php")."\">

    <label>$l_number<br />
      <input type=\"text\" name=\"tf_number\" size=\"20\" value=\"$num\" />
    </label>
    <label>$l_company<br />
      <input name=\"tf_company\" size=\"20\" maxlength=\"20\" value=\"$comp\" />
    </label>
    $dis_delegation
    <label>$l_after<br />
    <input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
   </label>
    <label>$l_before<br />
    <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
    </label>
    <label>$l_label_libelle<br />
      <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"$label\" />
    </label>
    <label>$l_type<br />
      $dis_cont_typ
    </label>
    <label>$l_manager<br />
      $sel_manager
    </label>
    <label>$l_priority<br />
      $sel_pri
    </label>
    <label>$l_status<br />
      $sel_sta
     </label>
    <label>$l_contract_kind<br />
      $sel_kind
     </label>
    <label>$l_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\" />
 </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// display the contract search result
// parameters:
//   - $contract[] : contract search criteria
//     keys used   : label,...of_isodate_convert
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_list($contract) {
  global $l_found, $l_no_found;
  global $display, $obm;
  
  $popup = $contract["popup"];
  $prefs = get_display_pref($obm["uid"], "contract");
  $obm_q = run_query_contract_search($contract);
  $nb_contract = $obm_q->num_rows_total();
  if ($nb_contract == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg($nb_contract." ".$l_found);
    $block = html_contract_search_list($obm_q, $prefs, $contract, $popup);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display the Contract Search result
// Parameters :
//   - $contract_q : contracts list
//   - $prefs      : fields to display in the contracts list
//   - $contract[] : contract search criteria
//     keys used   : label, type, dateafter, datebefore, company, market
//   - $popup      : popup flag tell if external parameters to handle
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_list($contract_q, $prefs, $contract, $popup) {
  global $l_close;
  
  if ($popup) {
    $ext_action = $contract["ext_action"];
    $ext_url = $contract["ext_url"];
    $ext_id = $contract["ext_id"];
    $ext_target = $contract["ext_target"];
    $ext_widget = $contract["ext_widget"];
    $ext_widget_text = $contract["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $num = urlencode($contract["number"]); 
  $label = urlencode($contract["label"]);
  $type = $contract["type"];
  $dateafter = $contract["date_after"];
  $datebefore = $contract["date_before"];
  $company = urlencode($contract["company"]);
  $manager = $contract["manager"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $kind = $contract["kind"];
  $format = $contract["format"];
  $delegation = $project['delegation'];
  $archive = ($contract['archive'] == '1' ? '1' : '0'); 

  $url = url_prepare("contract_index.php?action=search&amp;tf_label=$label&amp;tf_num=$num&amp;tf_company=$company&amp;sel_type=$type&amp;sel_priority=$priority&amp;sel_status=$status&amp;rd_kind=$kind&amp;rd_format=$format&amp;sel_manager=$manager&amp;delegation=$delegation&amp;tf_date_after=$dateafter&amp;tf_date_before=$datebefore&amp;cba_archive=$archive$url_ext");
  
  $contract_q->seek($first_row);
  $contract_d = new OBM_DISPLAY("DATA", $prefs, "contract");
  if ($popup) {
    $contract_d->display_link = false;
    if ($ext_url != "") {
      $contract_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $contract_d->display_ext = "get_id";
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }

  $contract_d->data_set = $contract_q;
  $contract_d->data_url = $url;
  $contract_d->data_header = "both";

  $block = $contract_d->display("dis_data_contract");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contract links menu
// Parameters:
//   - $id         : contract id
//   - $con_label  : contract label
//   - $tech_id    : contract technical manager id
//   - $deal_id    : deal id
//   - $inc_nb     : incident number
//   - $last       : last incident id
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contract_links($id, $con_label, $tech_id, $deal_id, $inc_nb, $last) {
  global  $ico_incident, $ico_deal, $ico_document,$ico_add;
  global $l_module_incident, $l_incident_list, $l_incident_new, $l_last;
  global $l_module_deal, $l_module_document, $l_contract;
  global $l_document_new,$l_document_add,$l_add_document,$l_deal;
  global $path, $cgp_show, $perm;
  
  $url_inc = url_prepare("$path/incident/incident_index.php?action=search&amp;contract_id=$id");
  $url_inc_new = url_prepare("$path/incident/incident_index.php?action=new&amp;contract_id=$id&amp;sel_owner=$tech_id&amp;tf_lcontract=$con_label");
  
  if ($last > 0) {
    $url_inc_last = url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;contract_id=$id&amp;incident_id=$last");
    $dis_last = "<li>
    <a href=\"$url_inc_last\"><img src=\"$ico_incident\" /></a>
    <a href=\"$url_inc_last\">$l_last</a></li>";
  }

  // Deal
  if ($perm->check_module_rights("deal") && $deal_id > 0) {
    $url_deal = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal_id");
    $block_deal = "
    <div  class=\"portlet\" id=\"contractDeal\">
    <h1>$l_module_deal</h1>
    <ul>
    <li>
      <a href=\"$url_deal\"><img src=\"$ico_deal\" /></a>
      <a href=\"$url_deal\">$l_deal</a>
    </li>
    </ul>
    </div>";
  }

  // Document  
  if ($perm->check_module_rights("document")) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=Contract");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=Contract");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_document)."&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contract/contract_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contract";

    $nb_document = run_query_global_document_nb ($id,"Contract");
    $block_doc = "
    <div  class=\"portlet\" id=\"contractDocument\">
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contract'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  $block = "
  <div  class=\"portlet\" id=\"contractIncident\">
  <h1>$l_module_incident</h1>
  <ul>
  <li>
    <a href=\"$url_inc\"><img src=\"$ico_incident\" /></a>
    <a href=\"$url_inc\">$l_incident_list ($inc_nb)</a>
  </li>
    $dis_last
  <li>
    <a href=\"$url_inc_new\"><img src=\"$ico_add\" alt=\"[Alt]\" /></a>
    <a href=\"$url_inc_new\">$l_incident_new</a></li>
  </ul>
  </div>
  $block_deal
  $block_doc
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract consult
// Parameters:
//   - $contract[]    
///////////////////////////////////////////////////////////////////////////////
function dis_contract_consult($contract) {
  global $display, $l_err_reference;

  $id = $contract["contract_id"];
  if ($id > 0) {
    $contract_q = run_query_contract_detail($id);
    $display["detailInfo"] = display_record_info($contract_q);
    $inc_q = run_query_contract_incident($id);
    $inc_nb = $inc_q->num_rows();
    $duration_used = 0;
    while ($inc_q->next_record()) {
      $last_inc = $inc_q->f("incident_id");
      $dur = $inc_q->f("incident_duration");
      $duration_used += $dur;
    }
    
    $block = html_contract_consult($contract_q, $contract, $inc_nb, $duration_used, $last_inc);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Consult
// Parameters:
//   - $contract_q    : information about the contract
//   - $contract      : Contract hash infos
//   - $inc_nb        : Incident number
//   - $duration_used : duration spent on this contract
//   - $last_inc      : Last incident id created
///////////////////////////////////////////////////////////////////////////////
function html_contract_consult($contract_q, $contract, $inc_nb, $duration_used, $last_inc) {
  global $path, $display,  $ico_company,$ico_contact, $ico_deal;
  global $l_yes, $l_no, $l_contract, $l_company, $l_deal;
  global $l_client_manager,$l_marketing_manager,$l_technical_manager;
  global $l_manager, $l_label, $l_comment, $l_number, $l_clause, $l_archive;
  global $l_dates, $l_signature,$l_begin,$l_exp,$l_renew,$l_date_cancel;
  global $l_autorenewal, $l_type, $l_priority, $l_status, $l_contract_kind;
  global $l_format,$l_ticket_nb, $l_duration, $l_duration_used;
  global $cck_customer, $cck_supplier, $l_cck_customer, $l_cck_supplier;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  
  $usercreate = $contract_q->f("contract_usercreate");
  $autorenewal = ($contract_q->f("contract_autorenewal")==0 ? $l_no : $l_yes);
  $priority = $contract_q->f("contractpriority_label");
  $status = $contract_q->f("contractstatus_label");
  $id = $contract_q->f("contract_id");
  $label = $contract_q->f("contract_label");
  $number = $contract_q->f("contract_number");
  $market = $contract_q->f("lname1") . " " . $contract_q->f("fname1");
  $tech = $contract_q->f("lname2") . " " . $contract_q->f("fname2");
  $tech_id = $contract_q->f("contract_techmanager_id");
  $com = beautify_comment(nl2br($contract_q->f("contract_comment")));
  $clause = nl2br($contract_q->f("contract_clause"));
  $datebegin = of_date_format($contract_q->f("datebegin"), 1);
  $dateexp = of_date_format($contract_q->f("dateexp"), 1);
  $daterenew = of_date_format($contract_q->f("daterenew"), 1);
  $datecancel = of_date_format($contract_q->f("datecancel"), 1);
  $datesignature = of_date_format($contract_q->f("datesignature"), 1);
  $comp_id = $contract_q->f("contract_company_id");
  $deal_id = $contract_q->f("contract_deal_id");
  $deal_label = $contract_q->f("deal_label");
  $type = $contract_q->f("contracttype_label");
  $ticket_nb = $contract_q->f("contract_ticketnumber");
  $format = $contract_q->f("contract_format");
  $duration = $contract_q->f("contract_duration");
  $comp_name = $contract_q->f("company_name");
  $comp_address1 = $contract_q->f("company_address1");
  $comp_zip = $contract_q->f("company_zipcode");
  $comp_town = $contract_q->f("company_town");
  $con1_id = $contract_q->f("contract_contact1_id");
  $con2_id = $contract_q->f("contract_contact2_id");
  $archive = $contract_q->f("contract_archive");
  $kind = $contract_q->f("contract_kind");

  if ($archive == 1) {
    $archive = $l_yes;
  } else {
    $archive= $l_no;
  }

  if ($kind == "$cck_customer") {
    $dis_kind = $l_cck_customer;
  } elseif ($kind == "$cck_supplier") {
    $dis_kind = $l_cck_supplier;
  }

  if ($format == $ccf_period) {
    $label_format = $l_ccf_period;
    $format_data = "";
  } else if ($format == $ccf_ticket) {
    $label_format = $l_ccf_ticket;
    $format_data = "<th>$l_ticket_nb</th>
      <td>$ticket_nb</td>";
  } else {
    $label_format = $l_ccf_duration;
    $format_data = "<th>$l_duration</th>
      <td>$duration</td>";
  }
  
  $dis_format = "
      <th>$l_format</th>
      <td>$label_format</td>
    
      $format_data";

  $con1 = $contract_q->f("clname1") . " " . $contract_q->f("cfname1") .
          " - " . $contract_q->f("cphone1"); 
  $con2 = $contract_q->f("clname2") . " " . $contract_q->f("cfname2") .
          " - " . $contract_q->f("cphone2"); 


  $dis_update = "<form method=\"get\" name=\"f_entity\"
      action=\"".url_prepare("contract_index.php")."\">
    <input type=\"hidden\" name=\"action\" value=\"detailupdate\">
    <input type=\"hidden\" name=\"contract_id\" value=\"$id\">
    <input type=\"submit\" value=\"$l_update\">
  </form>";


  // --- HTML Template --------------------------------------------------------

  //$display["link"] = html_contract_links($id, $label, $tech_id, $deal_id, $inc_nb, $last_inc);
  $display["title"] = $label;


  $block = "
    <div class=\"detail infos\">
      <h1>$l_company</h1>
      <table>
      <tr>
        <th>$l_company
          <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id")."\">
          <img src=\"$ico_company\" alt=\"[Company]\" /></a>
        </th>
        <td>$comp_name</td>
      </tr><tr>
        <th>$l_client_manager 1
          <a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con1_id") . "\">
          <img src=\"$ico_contact\" alt=\"[Contact]\" /></a>
        </th>
        <td>$con1</td> 
      </tr><tr>
        <th>$l_client_manager 2
          <a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con2_id") . "\">
          <img src=\"$ico_contact\" alt=\"[Contact]\" /></a>
        </th>
        <td>$con2</td>
      </tr>
      </table>   

    </div>

<div class=\"detail infos\">
      <h1>$l_manager</h1>
      <table>
      <tr>
        <th>$l_marketing_manager</th>
        <td>$market</td>
      </tr><tr>
        <th>$l_technical_manager</th>
        <td>$tech</td>
      </tr>
      </table>
      </div>
<div class=\"detail infos\">
  <h1>$l_status</h1>
  <table>
  <tr>
    <th>$l_status</th>
    <td>$status</td>
  </tr><tr>
    $dis_format
  </tr><tr>
    <th>$l_duration_used</th>
    <td>$duration_used</td>
  </tr>
  </table>
</div>      
<div class=\"detail infos\">
    <h1>$l_contract</h1>
    <table>
    <tr>
      <th>$l_label</th>
      <td>$label</td>
    </tr><tr>
      <th>$l_autorenewal</th>
      <td>$autorenewal</td>
    </tr><tr>      
      <th>$l_number</th>
      <td>$number</td>
    </tr><tr>
      <th>$l_type</th>
      <td>$type</td>
    </tr><tr>
      <th>$l_priority</th>
      <td>$priority</td>
    </tr><tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr>
    </table>
</div>
<div class=\"detail infos\">
    <h1>$l_dates</h1>
    <table>
    <tr>
      <th>$l_signature</th>
      <td>$datesignature</td>
    </tr><tr>
      <th>$l_begin</th>
      <td>$datebegin</td>
    </tr><tr>
      <th>$l_exp</th>
      <td>$dateexp</td>
    </tr><tr>
      <th>$l_renew</th>
      <td>$daterenew</td>
    </tr><tr>
      <th>$l_date_cancel</th>
      <td>$datecancel</td>
    </tr>
    </table>
</div>

<div class=\"detail extra\">
 <h1>$l_clause</h1>
  <table>
  <tr>
    <td>$clause</td>
  </tr> 
  </table>
</div>
<div class=\"detail extra\">
  <h1>$l_comment</h1>
    $com
</div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Form
// Parameters:
//   - $action    : action called
//   - $contract[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_contract_form($action,$contract,$param_company) {
  global $display, $obm;

  if ($action == "detailupdate") {
    $contract_q = run_query_contract_detail($contract["contract_id"]);
    $comp_id = $contract_q->f("contract_company_id"); 
    $users = array($contract_q->f("contract_marketmanager_id"), $contract_q->f("contract_techmanager_id"));
    $display["detailInfo"] = display_record_info($contract_q);
  } else {
    $contract_q = new DB_OBM;
    if ($action == "new") {
      $comp_id = $contract["company_id"];
      $company_q = run_query_company_info($comp_id);
    }
    if ($action == "insert" || $action == "update" ) {
      $users = array($contract["market"], $contract["tech"]);
    } else {
      $users = "";
    }
  }

  $contact_q = run_query_contact_contract($comp_id);
  $prio_q = run_query_contract_priority();
  $status_q = run_query_contract_status();
  $users_c = of_usergroup_get_group_users($obm['group_com'], true, $users, 'userobm_lastname');
  $users_p = of_usergroup_get_group_users($obm['group_prod'], true, $users, 'userobm_lastname');
  $type_q = run_query_contract_type();
 
  $block = html_contract_form($action,$contract_q,$type_q, $users_c, $users_p, $company_q, $contact_q, $contract, $prio_q, $status_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Contract Form
// Parameters :
//   - $action     : action called
//   - $contract_q : DBO : information about the contract (null for new)
//   - $type_q     : DBO : contract type list
//   - $users_c    : array : list of userobm from Commercial group
//   - $users_p    : array : list of userobm from Production group
//   - $comp_q     : DBO : company infos (name, ad1, zip, town when new)
//   - $contact_q  : DBO : list of contact from the company 
//   - contract[]  : default or transmitted values
//     keys used   : all
///////////////////////////////////////////////////////////////////////////////
function html_contract_form($action,$contract_q,$type_q,$users_c,$users_p,$comp_q,$contact_q, $contract,$pri_q,$sta_q){
  // - Themes
  global $display, $cgp_show, $obm;
  global $ico_mail,$ico_company,$ico_deal,$ico_contact, $l_header_new;
  global $path, $c_php_isodate_format, $c_undef, $l_undef, $l_update,$l_insert;
  global $l_label, $l_begin, $l_type, $l_marketing_manager, $l_archive;
  global $l_signature, $l_begin, $l_exp, $l_renew,$l_date_cancel,$l_dates;
  global $l_technical_manager,$l_manager;
  global $l_number,$l_clause,$l_priority, $l_status;
  global $l_comment,$l_add_comment,$l_upd_comment;
  global $l_contract_select_deal,$l_client_manager,$l_deal,$l_contract,$l_company;
  global $l_contract_kind,$l_format,$l_autorenewal;
  global $l_ticket_nb,$l_private, $l_duration; 
  global $cck_customer, $cck_supplier, $l_cck_customer,$l_cck_supplier;
  global $ccf_period, $ccf_ticket, $ccf_duration, $cgp_show;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $contract_q->f("contract_id");
    $label = $contract_q->f("contract_label");
    $comp_id = $contract_q->f("contract_company_id");
    $number = $contract_q->f("contract_number");
    $priority = $contract_q->f("contract_priority_id");
    $vis = ($contract_q->f("privacy") == 0 ? "0" : "1");
    $usercreate = $contract_q->f("contract_usercreate");
    $status = $contract_q->f("contract_status_id");
    $kind = $contract_q->f("contract_kind");
    $ticket_nb = $contract_q->f("contract_ticketnumber");
    $duration = $contract_q->f("contract_duration");
    $format = $contract_q->f("contract_format");
    $autorenewal = $contract_q->f("contract_autorenewal");
    $datebegin = of_date_upd_format($contract_q->f("datebegin"), true);
    $dateexp = of_date_upd_format($contract_q->f("dateexp"), true);
    $daterenew = of_date_upd_format($contract_q->f("daterenew"), true);
    $datecancel = of_date_upd_format($contract_q->f("datecancel"), true);
    $datesignature = of_date_upd_format($contract_q->f("datesignature"), true);
    $type_id = $contract_q->f("contract_type_id");
    $con1_id = $contract_q->f("contract_contact1_id");
    $con2_id = $contract_q->f("contract_contact2_id");
    $market_id = $contract_q->f("contract_marketmanager_id");
    $tech = $contract_q->f("contract_techmanager_id");
    $clause = $contract_q->f("contract_clause");
    $comment = $contract_q->f("contract_comment");
    $usercomment = $obm["uid"];
    $datecomment = of_date_upd_format();
    $archive = $contract_q->f("contract_archive");
   
    $comp_name = $contract_q->f("company_name");
    $ad1 = $contract_q->f("company_address1");
    $zip = $contract_q->f("company_zipcode");
    $town = $contract_q->f("company_town");   
    $deal_id = $contract_q->f("deal_id");
    $deal_label = $contract_q->f("deal_label");

    $dis_title = $label;
  } elseif ($action == "new") {
    $comp_name = $comp_q->f("company_name");
    $ad1 = $comp_q->f("company_address1");
    $zip = $comp_q->f("company_zipcode");
    $town = $comp_q->f("company_town");
    $market = $comp_q->f("company_marketingmanager_id");
    $format = $ccf_period;
    $kind = $cck_customer;
        
    // We pre-fill date with current date     
    $datebegin = of_date_upd_format();
    $dateexp = of_date_upd_format(time() + 31536000);

    $usercomment = $obm["uid"];
    $datecomment = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($contract["deal_id"])) { $deal_id = $contract["deal_id"]; }
  if (isset($contract["deal_label"])) { $deal_label = stripslashes($contract["deal_label"]); }
  if (isset($contract["deal_new_id"])) { $d_new_id = $contract["deal_new_id"]; }
  if (isset($contract["contract_id"])) { $id = $contract["contract_id"]; }
  if (isset($contract["label"])) { $label = stripslashes($contract["label"]); }
  if (isset($contract["company_id"])) { $comp_id = $contract["company_id"]; }
  if (isset($contract["number"])) { $number = $contract["number"]; }
  if (isset($contract["priority"])) { $priority = $contract["priority"]; }
  if (isset($contract["privacy"])) { $vis = $contract["privacy"]; }
  if (isset($contract["status"])) { $status = $contract["status"]; }
  if (isset($contract["datebegin"])) { $datebegin = of_date_upd_format($contract["datebegin"]); }
  if (isset($contract["dateexp"])) { $dateexp = of_date_upd_format($contract["dateexp"]); }
  if (isset($contract["daterenew"])) { $daterenew = of_date_upd_format($contract["daterenew"]); }
  if (isset($contract["datecancel"])) { $datecancel = of_date_upd_format($contract["datecancel"]); }
  if (isset($contract["datesignature"])) { $datesignature = of_date_upd_format($contract["datesignature"]); } 
  if (isset($contract["type"])) { $type_id = $contract["type"]; }
  if (isset($contract["autorenew"])) { $autorenewal = $contract["autorenew"]; }
  if (isset($contract["format"])) { $format = $contract["format"]; }
  if (isset($contract["ticket_nb"])) { $ticket_nb = $contract["ticket_nb"]; }
  if (isset($contract["duration"])) { $duration = $contract["duration"]; }
  if (isset($contract["kind"])) { $kind = $contract["kind"]; }
  if (isset($contract["con1"])) { $con1_id = $contract["con1"]; }
  if (isset($contract["con2"])) { $con2_id = $contract["con2"]; }
  if (isset($contract["market"])) { $market_id = $contract["market"]; }
  if (isset($contract["tech"])) { $tech = $contract["tech"]; }
  if (isset($contract["clause"])) { $clause = stripslashes($contract["clause"]); }
  if (isset($contract["comment"])) { $comment = stripslashes($contract["comment"]); }
  if (isset($contract["add_comment"])) { $add_comment = stripslashes($contract["add_comment"]); }
  if (isset($contract["usercomment"])) { $usercomment = $contract["usercomment"]; }
  if (isset($contract["datecomment"])) { $datecomment = of_date_upd_format($contract["datecomment"]); }
  if (isset($contract["company"])) { $comp_name = stripslashes($contract["company"]); }
  if (isset($contract["company_ad1"])) { $ad1 = stripslashes($contract["company_ad1"]); }
  if (isset($contract["company_zip"])) { $zip = $contract["company_zip"]; }
  if (isset($contract["company_town"])) { $town = stripslashes($contract["company_town"]); }
  if (isset($contract["archive"])) { $archive = $contract["archive"]; }

  if ($archive == 1) {
    $archive_display = " checked=\"checked\"";
  }
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) && 
         ($usercreate == $obm["uid"]) ) ) {
    $dis_vis = "<tr>
      <th><label for=\"cba_vis\">$l_private</label></th>
      <td><input id=\"cba_vis\" name=\"cba_vis\" type=\"checkbox\" value=\"1\"";
    if ($vis == 1) $dis_vis .= " checked=\"checked\"";
      $dis_vis .= " /></td></tr>";
  }

  if ($cgp_show["module"]["deal"] && $deal_id > 0) {
    $block_deal = "
    <tr>
      <th>$l_deal
        <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"deal_new_id\" value=\"$d_new_id\" />
      </th>
      <td>
        <a href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal_id")."\">$deal_label</a>
        <a href=\"\" onclick=\"popup('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contract_select_deal)."&amp;ext_widget=f_entity.deal_new_id&amp;ext_widget_text=f_entity.deal_label&amp;comp_id=$comp_id','contract'); return false;\">
        <img src=\"$ico_deal\" alt=\"$l_contract_select_deal\" /></a>
        <input type=\"text\" name=\"deal_label\" value=\"$deal_label\" size =\"40\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </td>
    </tr>";
  }

  // Priority select
  $sel_pri = "<select name=\"sel_priority\">";
  while ($pri_q->next_record()) {
    $id_2 = $pri_q->f("contractpriority_id");
    $label_p = $pri_q->f("contractpriority_label");
    $sel_pri .= "\n<option value=\"$id_2\"";
    if ($id_2 == $priority) 
      $sel_pri .= " selected=\"selected \"";
    $sel_pri .= ">$label_p</option>";
  }
  $sel_pri .= "</select>";

  // Status select
  $sel_sta = "<select name=\"sel_status\">";
  while ($sta_q->next_record()) {
    $id_3 = $sta_q->f("contractstatus_id");
    $label_s = $sta_q->f("contractstatus_label");
    $sel_sta .= "\n<option value=\"$id_3\"";
    if ($id_3 == $status) 
      $sel_sta .= " selected=\"selected \"";
    $sel_sta .= ">$label_s</option>";
  }
  $sel_sta .= "</select>";

  // Type select
  $dis_type="<select name=\"sel_type\">
    <option value=\"$c_undef\">$l_undef</option>";
  while($type_q->next_record()) {
    $dis_type.="\n<option value=\"".$type_q->f("contracttype_id")."\"";
    if ($type_id == $type_q->f("contracttype_id"))
       $dis_type.=" selected=\"selected\""; 
    $dis_type.=">".$type_q->f("contracttype_label")."</option>";
  }
  $dis_type.="</select>";
 
  // Marketing manager select
  $sel_market = "<select name=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($users_c)) {
    foreach ($users_c as $u_id => $one_user) {
      $sel_market .= "\n<option value=\"$u_id\"";
      if ($u_id == $market_id) 
        $sel_market .= " selected=\"selected\"";
      $sel_market .= '>'. $one_user['lastname'].' '.$one_user['firstname'].'</option>';
    }
  }
  $sel_market.="</select>";
 
  // Technical manager select
  $sel_tech = "<select name=\"sel_tech\">
    <option value=\"$c_undef\">$l_undef</option>";
  if (is_array($users_p)) {
    foreach ($users_p as $u_id => $one_user) {
     $sel_tech .= "\n<option value=\"$u_id\"";
     if ($u_id == $tech) 
        $sel_tech.=" selected=\"selected\"";
     $sel_tech .= '>'. $one_user['lastname'].' '.$one_user['firstname'].'</option>';
    }
  }
  $sel_tech .= "</select>";

  if ($autorenewal == 1) { 
    $autorenew_c = "checked=\"checked\"";
  }
  $autorenewal_display = "<input type=\"checkbox\" id=\"cba_autorenew\" name=\"cba_autorenew\" value=\"1\" $autorenew_c />";

  // Contract kind : customer or supplier
  if ($kind == "$cck_customer") {
    $kind_customer_c = "checked=\"checked\"";
  } else {
    $kind_supplier_c = "checked=\"checked\"";
  }
  
  $radio_kind = "<label><input type=\"radio\" name=\"rd_kind\" value=\"$cck_customer\" $kind_customer_c/> $l_cck_customer</label>";
  $radio_kind .= "<label><input type=\"radio\" name=\"rd_kind\" value=\"$cck_supplier\" $kind_supplier_c/> $l_cck_supplier</label><br /> ";

  if ($format == $ccf_period) {
    $cb_period_c = "checked=\"checked\"";
    $style_ticket = "style=\"display:none;\"";
    $style_duration = "style=\"display:none;\"";
  } else if ($format == $ccf_ticket) {
    $cb_ticket_c = "checked=\"checked\"";
    $style_ticket = "style=\"display:block;\"";
    $style_duration = "style=\"display:none;\"";
  } else {
    $cb_duration_c = "checked=\"checked\"";
    $style_ticket = "style=\"display:none;\"";
    $style_duration = "style=\"display:block;\"";
  }

  $dis_format = "
    <label>
    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_period\" $cb_period_c
      onclick=\"
          this.form.tf_ticket_nb.style.display = 'none';
          this.form.tf_duration.style.display = 'none';
	  document.getElementById('label_ticket').style.display = 'none';
	  document.getElementById('label_duration').style.display = 'none';
      \" /> $l_ccf_period
    </label>
    <label>
    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_duration\" $cb_duration_c onclick=\"this.form.tf_ticket_nb.style.display = 'none';
	  document.getElementById('label_ticket').style.display = 'none';
	  document.getElementById('label_duration').style.display = 'block';
	  this.form.tf_duration.style.display = 'block';
        \" /> $l_ccf_duration
    </label>
    <label>
    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_ticket\" $cb_ticket_c onclick=\"
          this.form.tf_ticket_nb.style.display = 'block';
          document.getElementById('label_ticket').style.display = 'block';
          document.getElementById('label_duration').style.display = 'none';
          this.form.tf_duration.style.display = 'none';
         \" /> $l_ccf_ticket
    </label>";

  $dis_format_detail = "
    <tr>
      <th>
        <div id=\"label_ticket\" $style_ticket>$l_ticket_nb</div>
        <div id=\"label_duration\" $style_duration>$l_duration</div>
      </th>
      <td>
        <input type=\"textbox\" $style_ticket name=\"tf_ticket_nb\" value =\"$ticket_nb\" />
        <input type=\"textbox\" $style_duration name=\"tf_duration\" value =\"$duration\" />
      </td>
    </tr>
";

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con1 = "<select name=\"sel_con1\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($contact_q->next_record()) {
    $lc_id = $contact_q->f("contact_id");
    $lc_text = $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone");
    $dis_sel_con1.="\n<option value=\"$lc_id\"";
    if ($lc_id == $con1_id) {
      $dis_sel_con1.=" selected=\"selected\"";
    }
    $dis_sel_con1 .= ">$lc_text</option>";
  }
  $dis_sel_con1 .= "</select>";
  
  if ($action == "detailupdate") {
    if ($con1_id > 0) {
      $dis_con1 = "
      <a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=".$con1_id)."\"><img src=\"$ico_contact\" alt=\"[contact\" /></a>";    
    }
  }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con2 = "<select name=\"sel_con2\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($contact_q->next_record()) {
    $lc_id = $contact_q->f("contact_id");
    $lc_text = $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone");
    $dis_sel_con2 .= "\n<option value=\"$lc_id\"";
    if ($lc_id == $con2_id) {
      $dis_sel_con2.=" selected=\"selected\"";
    }
    $dis_sel_con2 .= ">$lc_text</option>";
  }
  $dis_sel_con2.="</select>";

  // User comment select construction
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  if (is_array($users_c)) {
    foreach($users_c as $u_id => $one_user) {
      $cname = $one_user['lastname'].' '.$one_user['firstname'];
      $sel_usercomment .= "<option value=\"$cname\"";
      // First time default to uid of connected user (uid) then set to name
      if ( ($usercomment == $u_id) || ($usercomment == $cname) ) {
        $sel_usercomment .= " selected = \"selected\"";
      }
      $sel_usercomment .= ">$cname</option>\n";
    }
  }
  $sel_usercomment .= "</select>";

  if ($action == "detailupdate") {
    if ($con2_id > 0) {
      $dis_con2 = "
      <a href=\"".url_prepare("$path/contact/contact_index.php?action=consult&amp;id=".$con2_id)."\"><img src=\"$ico_contact\" alt=\"[contact]\" /></a>";    
    }
  }

  $dis_sub = "
    <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"tf_company\" value=\"$comp_name\" />
    <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_sub .= "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"contract_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";
    $dis_comment = "
        $l_upd_comment
        <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>
    ";
  } else {
    $dis_sub .= "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  // --- html template --------------------------------------------------------
  if ($cgp_show["module"]["deal"] && $deal_id > 0) {
    $block_deal = "
    <tr>
      <th>$l_deal
        <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"deal_new_id\" value=\"$d_new_id\" />
      </th>
      <td>
        <a href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal_id")."\">$deal_label</a>
        <a href=\"\" onclick=\"popup('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contract_select_deal)."&amp;ext_widget=f_entity.deal_new_id&amp;ext_widget_text=f_entity.deal_label&amp;comp_id=$comp_id','contract'); return false;\">
        <img src=\"$ico_deal\" alt=\"$l_contract_select_deal\" /></a>
        <input type=\"text\" name=\"deal_label\" value=\"$deal_label\" size =\"40\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </td>
    </tr>";
  }

  $display["title"] = $dis_title;

  $block = "
  <form method=\"post\" name=\"f_entity\"
    onsubmit=\"if (check_contract(this)) return true; else return false;\" 
    action=\"".url_prepare("contract_index.php")."\">

<fieldset class=\"detail infos\">
  <legend>$l_company</legend>
    <table>
    <tr>
      <th>
        <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id"). "\">
        <img src=\"$ico_company\" alt=\"[Company]\" /></a>
        $comp_name
      </th>
      <td>$ad1
        <br />$zip $town
      </td> 
    </tr><tr>
      <th>$l_client_manager 1 $dis_con1</th>
      <td>$dis_sel_con1</td>
    </tr><tr>
      <th>$l_client_manager 2 $dis_con2</th>
      <td>$dis_sel_con2</td>
    </tr>
    $block_deal
    </table>
</fieldset>

<fieldset class=\"detail infos\">
  <legend>$l_manager</legend>
  <table>
  <tr>   
  <th>$l_marketing_manager</th>
      <td>$sel_market</td>
    </tr><tr>
      <th>$l_technical_manager</th>
      <td>$sel_tech</td>    
   </tr>
   </table>
</fieldset>

<fieldset class=\"detail infos\">
<legend>$l_status</legend>

<table>
<tr>
 <th>$l_status</th>
 <td>$sel_sta</td>
 </tr><tr>
 <th>$l_format</th>
 <td>$dis_format</td>
</tr>$dis_format_detail
  </table>
</fieldset>

  <fieldset class=\"detail infos\">
  <legend>$l_contract</legend>
    <table>
    <tr>
      <th>$l_label</th>
      <td><input name=\"tf_label\" type=\"text\" size=\"40\" maxlength=\"40\" value=\"$label\" /></td>
    </tr><tr>
      <th>$l_number</th>
      <td><input name=\"tf_number\" type=\"text\" size=\"20\" maxlength=\"20\" value=\"$number\" /></td>
    </tr>
      $dis_vis
   <tr>
      <th>$l_type</th>
      <td>$dis_type</td>
    </tr><tr>
      <th>$l_priority</th>
      <td>$sel_pri</td> 
    </tr><tr>
      <th><label for=\"cba_autorenew\">$l_autorenewal</label></th>
      <td>$autorenewal_display</td>
    </tr><tr>
      <th><label for=\"cba_archive\">$l_archive</label></th>
      <td><input id=\"cba_archive\" name=\"cba_archive\" type=\"checkbox\"  size=\"40\" value=\"1\" $archive_display/></td>
    </tr><tr>
      <th>$l_contract_kind</th>
      <td>$radio_kind</td>
    </tr>
    </table>
</fieldset>


  <fieldset class=\"detail infos\">
  <legend>$l_dates</legend>
 
 <table>
 <tr>
      <th>$l_signature</th>
    <td><input type=\"text\" name=\"tf_datesignature\" value=\"$datesignature\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_begin</th>
     <td><input type=\"text\" name=\"tf_datebegin\" value=\"$datebegin\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_exp</th>
   <td><input type=\"text\" name=\"tf_dateexp\" value=\"$dateexp\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_renew</th>
    <td><input type=\"text\" name=\"tf_daterenew\" value=\"$daterenew\" class=\"datePicker\"/></td>
    </tr><tr>
      <th>$l_date_cancel</th>
   <td><input type=\"text\" name=\"tf_datecancel\" value=\"$datecancel\" class=\"datePicker\"/></td>
    </tr>
  </table>
</fieldset>



<fieldset class=\"detail extra\">
  <legend>$l_clause</legend>
    <table>
    <tr>
      <td><textarea name=\"ta_clause\" rows=\"6\" cols=\"72\">$clause</textarea></td>
    </tr>
    </table>
</fieldset>


<fieldset class=\"detail extra\">
  <legend>$l_comment</legend>
      $l_add_comment
      <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
      <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>
      $dis_comment
</fieldset>

  <fieldset class=\"buttons\">
    $dis_sub
  </fieldset>
  </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Export
// Parameters:
//   - $contract[] : contract id
//     keys used  : contract_id
///////////////////////////////////////////////////////////////////////////////
function dis_contract_export($param_contract) {
  global $display, $path, $l_err_reference;

  $con_q = run_query_contract_detail($param_contract);
  $company_id = $con_q->f("company_id");

  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  export_contract($con_q);
}


///////////////////////////////////////////////////////////////////////////////
// Contract Export
// Parameters:
//   - $con_q : : DBO : information about the contact
///////////////////////////////////////////////////////////////////////////////
function export_contract($co_q) {
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  global $cck_customer, $cck_supplier;
  global $l_dates, $l_signature, $l_begin, $l_exp, $l_renew, $l_date_cancel;
  global $l_label,$l_number,$l_clause,$l_comment;
  global $l_company,$l_status,$l_priority,$l_type,$l_cck_customer;
  global $l_cck_supplier,$l_ticket_nb,$l_total_ticket;
  global $l_disp_ticket, $l_duration; 
  global $l_company_town,$l_company_zipcode,$l_company_expresspostalcode,$l_company_phonenumber;
  global $l_company_fax,$l_company_web,$l_company_email;

  $id = $co_q->f("contract_id");
  $datebegin = $co_q->f("contract_datebegin");
  $dateexp = $co_q->f("contract_dateexp");
  $daterenew = $co_q->f("contract_daterenew");
  $datecancel = $co_q->f("contract_datecancel");
  $datesignature = $co_q->f("contract_datesignature");
  $export_contractdate = "Contract $l_dates: \n";
  if ($datesignature)
    $export_contractdate .= "$l_signature: $datesignature\n";
  if ($datebegin)
    $export_contractdate .= "$l_begin: $datebegin\n";
  if ($dateexp)
    $export_contractdate .= "$l_exp: $dateexp\n";
  if ($daterenew)
    $export_contractdate .= "$l_renew: $daterenew\n";
  if ($datecancel)
    $export_contractdate .= "$l_cancel: $datecancel\n";
  $export_contract= "CONTRACT \n"; 
  $label = $co_q->f("contract_label");
  $export_contract .= "$l_label: $label\n";
  $number = $co_q->f("contract_number");
  if ($number != "")
    $export_contract .= "$l_number: $number\n";
  $export_contract .= "$export_contractdate";   
  $export_clause .= "CLAUSE \n";
  $clause = $co_q->f("contract_clause");  
  if ($clause != "")
    $export_clause .= "$l_clause: $clause\n"; 
  $export_comment .= "COMMENT \n";
  $comment = $co_q->f("contract_comment");
  if ($comment != "")
    $export_comment .= "$l_comment: $comment\n"; 
  $export_company = "COMPANY \n";
  $companyname = $co_q->f("company_name");
  if ($companyname != "")
    $export_company .= "$l_company: $companyname\n";
  $companytown = $co_q->f("company_town");
  if ($companytown != "")
    $export_company .= "$l_company_town: $companytown\n";
  $companyzipcode = $co_q->f("company_zipcode");
  if ($companyzipcode != "")
    $export_company .= "$l_company_zipcode: $companyzipcode\n";
  $companyexpresspostal = $co_q->f("company_expresspostal");
  if ($companyexpresspostal != "")
    $export_company .= "$l_company_expresspostalcode: $companyexpresspostal\n";
  $companyphone = $co_q->f("company_phone");
  if ($companyphone != "")
    $export_company .= "$l_company_phonenumber: $companyphone\n";
  $companyfax = $co_q->f("company_fax");
  if ($companyfax != "")
    $export_company .= "$l_company_fax: $companyfax\n";
  $export_status .= "STATUS \n";
  $status = $co_q->f("contractstatus_label");
  if ($status != "")
    $export_status .= "$l_status: $status\n";
  $priority1 = $co_q->f("contractpriority_label");
  if ($priority1 != "")
    $export_status .= "$l_priority: $priority1\n";
  $type = $co_q->f("contracttype_label");
  if ($type != "")
    $export_status .= "$l_type: $type\n";
  $kind = $co_q->f("contract_kind");
  if ($kind == "$cck_customer")
    $export_status .= "Kind: $l_cck_customer\n";
  if ($kind == "$cck_supplier")
    $export_status .= "Kind: $l_cck_supplier\n";
  $format = $co_q->f("contract_format");
  if ($format == $ccf_perdio) {
    $export_status .= "Format: $l_ccf_period\n";
  } elseif ($format == $ccf_ticket) {
    $export_status .= "Format: $l_ccf_ticket\n";
    $ticketnumber = $co_q->f("contract_ticketnumber");
    $export_status .= "$l_ticket_nb: $ticketnumber\n";
  } else {
    $export_status .= "Format: $l_ccf_duration\n";
    $duration = $co_q->f("contract_duration");
    $export_status .= "$l_duration: $duration h\n";
  }

  header("Content-Type: text/x-vCard");
  header("Content-Disposition: inline; filename=".str_replace(" ","_",$label).",_".str_replace(" ","_",$number).".txt");
  header('Cache-Control: maxage=3600'); 
  header('Pragma: public');  
  header("charset=utf-8"); 

  $export = "BEGIN:\n\n";
  $export .= "$export_company\n";
  $export .= "$export_contract\n";
  $export .= "$export_status\n";
  $export .= "$export_comment\n";
  $export .= "$export_clause\n";
  $export .= "END:";
  echo $export;
}


///////////////////////////////////////////////////////////////////////////////
// Display the context about a contract insertion or update
// When similar contracts exists we show these and ask confirmation
// Parameters:
//   - $c_id       : contract id
//   - $co_q      : contract database result (at least 1 row)
//   - $contract[] : default values
//     keys used  : num, archive, name, kind, ad1, ad2, ad3, zip, town, cdx
//                : ctry, phone, fax, web, email, com
/////////////////////////////////////////////////////////////////////////////
function dis_contract_warn_insert($c_id, $co_q, $contract) {
  global $display, $l_check_samecontract, $l_confirm, $l_back;
  global $c_yes, $c_no;

  $name = $contract["label"];
  $comp_id = $contract["company_id"];
  $num = $contract["number"];
  $ta_clause = $contract["clause"];
  $datebegin = $contract["datebegin"];
  $dateexp = $contract["dateexp"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $kind = $contract["kind"];
  $format =$contract["format"];
  $ticket_nb =$contract["ticket_nb"];
  $duration = $contract["duration"];
  $autorenewal =$contract["autorenew"];
  $daterenew = $contract["daterenew"];
  $datecancel = $contract["datecancel"];
  $datesignature = $contract["datesignature"];
  $sel_type = $contract["type"];
  $ta_comment = $contract["comment"];
  $con1 = ($contract["con1"] ? $contract["con1"] : "0");
  $con2 = ($contract["con2"] ? $contract["con2"] : "0");
  $sel_tech  = $contract["tech"];
  $sel_market = $contract["market"];
  $deal_id = ($contract["deal_new_id"] ? $contract["deal_new_id"] : "0");
  $deal_label = $contract["deal_label"];
  $archiv = ($contract["archive"] ? $contract["archive"] : "0");
  $vis = $contract["privacy"];

  $display["msg"] .= display_warn_msg($l_check_samecontract);

  while ($co_q->next_record()) {
    $id = $co_q->f("contract_id");
    $samename = $co_q->f("contract_label");
   
    $dis_same_cont .= "
      <li>
         <a class=\"detail\" href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;contract_id=$id") . "\">
          $samename </a>
      </li>";
  }

  $hidden_fields = "<input type=\"hidden\" name=\"tf_label\" value=\"$name\" />
      <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"tf_num\" value=\"$num\" />
      <input type=\"hidden\" name=\"ta_clause\" value=\"$ta_clause\" />
      <input type=\"hidden\" name=\"tf_datebegin\" value=\"$datebegin\" />
      <input type=\"hidden\" name=\"tf_dateexp\" value=\"$dateexp\" />
      <input type=\"hidden\" name=\"sel_priority\" value=\"$priority\" />
      <input type=\"hidden\" name=\"sel_status\" value=\"$status\" />
      <input type=\"hidden\" name=\"rd_kind\" value=\"$kind\" />
      <input type=\"hidden\" name=\"rd_format\" value=\"$format\" />      
      <input type=\"hidden\" name=\"tf_ticket_nb\" value=\"$ticket_nb\" />
      <input type=\"hidden\" name=\"tf_duration\" value=\"$duration\" />
      <input type=\"hidden\" name=\"cba_autorenew\" value=\"$autorenewal\" /> 
      <input type=\"hidden\" name=\"tf_daterenew\" value=\"$daterenew\" />
      <input type=\"hidden\" name=\"tf_datecancel\" value=\"$datecancel\" />
      <input type=\"hidden\" name=\"tf_datesignature\" value=\"$datesignature\" />
      <input type=\"hidden\" name=\"sel_type\" value=\"$sel_type\" />
      <input type=\"hidden\" name=\"ta_comment\" value=\"$ta_comment\" />
      <input type=\"hidden\" name=\"sel_con1\" value=\"$con1\" />
      <input type=\"hidden\" name=\"sel_con2\" value=\"$con2\" />
      <input type=\"hidden\" name=\"sel_tech\" value=\"$sel_tech\" />
      <input type=\"hidden\" name=\"sel_market\" value=\"$sel_market\" />
      <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
      <input type=\"hidden\" name=\"deal_label\" value=\"$deal_label\" />
      <input type=\"hidden\" name=\"cba_archive\" value=\"$archiv\" />
      <input type=\"hidden\" name=\"cba_vis\" value=\"$vis\" />
";

  $block = "
  <div class=\"detail\">
  <ul>
    $dis_same_cont
  </ul>
  </div>
  <div class=\"buttons\">
  <form method=\"post\" name=\"form_insert\" action=\"" .url_prepare("contract_index.php") . "\">
  <input type=\"hidden\" name=\"action\" value=\"insert\" />
  <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
  $hidden_fields
  <input type=\"submit\"  value=\"$l_confirm\" />
  </form>
  <form name=\"form_back\" method=\"post\" action=\"" .url_prepare("contract_index.php") . "\">
  <input type=\"hidden\" name=\"action\" value=\"new\" />
  $hidden_fields
  <input type=\"submit\" value=\"$l_back\" />
  </form>
  </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the contract delete validation screen
// Parameters:
//   - $p_id : contract id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_contract($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("contract_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"contract_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"contract_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_contract_del(this.form)) return true; else return false;\" />
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
// Incident Priority section
// Parameters:
//   - $pri_q : Priority list database object
///////////////////////////////////////////////////////////////////////////////
function html_contract_priority_form($pri_q) {
  global $l_pri_manage,$l_pri_exist, $l_label, $l_code, $l_color;
  global $l_pri_checkdelete, $l_pri_update, $l_pri_new, $l_pri_insert,$ico_ok,$ico_delete,$l_c_new;

 
  $char_title = '=';
  $key = 0;
  while ($pri_q->next_record()) {
    $id = $pri_q->f("contractpriority_id");
    $code = $pri_q->f("contractpriority_code");
    $label = $pri_q->f("contractpriority_label");
    $color = $pri_q->f("contractpriority_color");

    if($key%2 == 0) {
      $class = "class=\"pair\"";
    } else {
      $class = "";
    }
    $c_label = htmlentities($one_cat["label"], ENT_COMPAT, 'UTF-8');
    $priority_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"priority_$id\" value=\"$id\" />
    <a href=\"?action=priority_checklink&amp;category=priority&amp;priority=$id\">
    <img src=\"$ico_delete\" alt=\"$l_pri_checkdelete\" title=\"$l_pri_checkdelete\" />
    </a></td>
    <td><input type=\"text\" id=\"tf_priority_color_$id\" value=\"$color\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_priority_code_$id\" value=\"$code\" size=\"6\"/></td>
    <td><input type=\"text\" id=\"tf_priority_label_$id\" value=\"$label\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'priority');\">
    <img src=\"$ico_ok\" alt=\"$l_pri_update\" title=\"$l_pri_update\" />
    </a>
    </td>
    </tr>
    ";    
  }

// --- HTML Template --------------------------------------------------------
  $r = "
  <div class=\"detail admin\" >
  <form id=\"form_priority_update\" action=\"?action=priority_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_priority_color\" value=\"\" />
  <input type=\"hidden\" name=\"tf_priority_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_priority_label\" value=\"\" />
  <input type=\"hidden\" name=\"priority\" value=\"\" />
  </form>    
  <h1>$l_pri_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_pri_color</td>
  <td>$l_pri_code</td>
  <td>$l_pri_label</td>
  <td>&nbsp;</td>        
  </tr>    
  </thead>
  <tbody>
  $priority_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_priority_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_contract_priority_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_color</td>
  <td>$l_code</td>
  <td>$l_label</td>    
  </tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_priority_color\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_priority_code\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_priority_label\" value=\"\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_pri_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"priority_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"priority\" />
  </form>
  </div>

";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the priority links
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function dis_contract_priority_links($id) {
  global $l_pri_link_contract, $l_pri_link_contract_no;
  global $l_pri_delete, $l_pri_can_delete, $l_pri_cant_delete;
  global $l_back,$display;

  $label = get_contract_priority_label($id);
  $obm_q = run_query_contract_priority_links($id);

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";
	
  $nb_pri = $obm_q->num_rows();
  if ($nb_pri > 0) {
    $display["msg"] = display_warn_msg($l_pri_cant_delete); 
    $dis_link = "<h1>$label : $l_pri_link_contract ($nb_pri)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contract_id");
      $cname = $obm_q->f("contract_label");
      $dis_link .="<li><a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;contract_id=$cid") . "\">$cname</a></li>";
    }
    if ($cpt < $nb_pri) {
      $dis_link .= "<li>...</li>";
    }

    $dis_link .="<div class=\"buttons\">$dis_back</div>";

  } else {
    $display["msg"] = display_ok_msg($l_pri_can_delete);
    $dis_link = "<h1>$label : $l_pri_link_contract_no</h1>
     <div class=\"buttons\">
         <form name=\"form_pri_delete\" method=post action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"priority_delete\">
        <input type=\"hidden\" name=\"sel_priority\" value=\"$id\">
        <input type=\"submit\" name=\"sub_pri\" value=\"$l_pri_delete\">
        </form>
        $dis_back
     </div>";

  }

  $block = "
 <div class=\"detail\">
    <ul>
    $dis_link
    </ul>
    </div>
  ";

  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Contract Status section
// Parameters:
//   - $sta_q : Status list database object
///////////////////////////////////////////////////////////////////////////////
function html_contract_status_form($sta_q) {
  global $l_label, $l_code;
  global $l_sta_manage,$l_sta_exist,$l_sta_new;
  global $l_sta_checkdelete, $l_sta_update, $l_sta_insert,$l_c_new,$ico_ok,$ico_delete;



$char_title = '=';
$key = 0;
while ($sta_q->next_record()) {
  $id = $sta_q->f("contractstatus_id");
  $code = $sta_q->f("contractstatus_code");
  $label = $sta_q->f("contractstatus_label");

  if($key%2 == 0) {
    $class = "class=\"pair\"";
  } else {
    $class = "";
  }
  $c_label = htmlentities($one_cat["label"], ENT_COMPAT, 'UTF-8');
  $sta_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"status_$id\" value=\"$id\" />
    <a href=\"?action=status_checklink&amp;category=status&amp;status=$id\">
    <img src=\"$ico_delete\" alt=\"$l_sta_checkdelete\" title=\"$l_sta_checkdelete\" />
    </a></td>
    <td> <input type=\"text\" id=\"tf_status_code_$id\" value=\"$code\" size=\"6\"/></td>
    <td> <input type=\"text\" id=\"tf_status_label_$id\" value=\"$label\" /></td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($id,'status');\">
    <img src=\"$ico_ok\" alt=\"$l_sta_update\" title=\"$l_sta_update\" />
    </a>
    </td>
    </tr>
    ";    
}

  // --- HTML Template --------------------------------------------------------

  $r = "
  <div class=\"detail admin\" >
  <form id=\"form_status_update\" action=\"?action=status_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_status_code\" value=\"\" />
  <input type=\"hidden\" name=\"tf_status_label\" value=\"\" />
  <input type=\"hidden\" name=\"status\" value=\"\" />
  </form>    
  <h1>$l_sta_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_code</td>
  <td>$l_label</td>
  <td>&nbsp;</td>        
  </tr>    
  </thead>
  <tbody>
  $sta_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_status_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_contract_status_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_code</td>
  <td>$l_label</td>    
  </tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_status_code\" value=\"\" size=\"6\"/></td>
  <td><input type=\"text\" name=\"tf_status_label\" value=\"\" /></td>    
  </tr>  
  </tbody>
  <tfoot>
  <tr>
  <td colspan=\"3\"><input type=\"submit\" value=\"$l_sta_insert\" /></td>
  </tr>
  </tfoot>
  </table>
  <input type=\"hidden\" name=\"action\" value=\"status_insert\" />
  <input type=\"hidden\" name=\"category\" value=\"status\" />
  </form>
  </div>
";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the status links
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function dis_contract_status_links($id) {
  global $l_sta_link_contract,$l_sta_link_contract_no, $l_sta_can_delete, $l_sta_cant_delete;
  global $l_sta_delete, $l_back,$display;

  $label = get_contract_status_label($id);
  $obm_q = run_query_contract_status_links($id);
  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  $nb_stat = $obm_q->num_rows();
  if ($nb_stat > 0) {
    $display["msg"] = display_warn_msg($l_sta_cant_delete);
    $dis_link = "<h1>$label : $l_sta_link_contract ($nb_stat)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $stid = $obm_q->f("contract_id");
      $stlabel = $obm_q->f("contract_label");
      $dis_link.= "<li><a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;contract_id=$stid") . "\">$stlabel</a></li>";
    }
    if ($cpt < $nb_stat) {
      $dis_link .= "<li>...</li>";
    }

    $dis_link .="<div class=\"buttons\">$dis_back</div>";

  } else {
    $display["msg"] = display_ok_msg($l_sta_can_delete); 
    $dis_link = "<h1>$label : $l_sta_link_contract_no</h1>
    
       <div class=\"buttons\">
       
        <form name=\"form_status_delete\" method= \"post\" action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
        <input type=\"hidden\" name=\"sel_status\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_delete\" />
        </form>
        $dis_back	
    </div>";

  }
  $block = "
 <div class=\"detail\">
    <ul>
    $dis_link
    </ul>
    </div>
  ";

  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract admin index
///////////////////////////////////////////////////////////////////////////////
function dis_contract_admin_index() {

  $pri_q = run_query_contract_priority();
  $sta_q = run_query_contract_status();
  
  return html_contract_priority_form($pri_q) . html_contract_status_form($sta_q) .html_contract_admin_form(run_query_contract_type());
}



///////////////////////////////////////////////////////////////////////////////
// Contract Administration Forms
// Parametres:
//   - $type : DBO : Contract types list
///////////////////////////////////////////////////////////////////////////////
function html_contract_admin_form($type_q) {
  global $l_type_manage, $l_type_new, $l_type_exist;
  global $l_type_checkdelete, $l_type_delete, $l_type_update, $l_type_insert;
  global $l_type_insert_ok, $l_type_insert_error, $l_type_update_ok;
  global $l_type_update_error, $l_type_delete_error, $l_type_delete_ok,$l_c_new,$ico_ok,$ico_delete,$l_label;

$char_title = '=';
$key = 0;
while ($type_q->next_record()) {
  $id = $type_q->f("contracttype_id");
  $label = $type_q->f("contracttype_label");

  if($key%2 == 0) {
    $class = "class=\"pair\"";
  } else {
    $class = "";
  }
  $c_label = htmlentities($one_cat["label"], ENT_COMPAT, 'UTF-8');
  $type_list .= "
    <tr $class>
    <td>
    <input type=\"hidden\" id=\"type_$id\" value=\"$id\" />
    <a href=\"?action=type_checklink&amp;category=type&amp;type=$id\">
<img src=\"$ico_delete\" alt=\"$l_type_checkdelete\" title=\"$l_type_checkdelete\" />
</a></td>
<td> <input type=\"text\" id=\"tf_type_label_$id\" value=\"$label\" /></td>
<td>
<a href=\"#\" onclick=\"return updateLine($id,'type');\">
    <img src=\"$ico_ok\" alt=\"$l_type_update\" title=\"$l_type_update\" />
    </a>
    </td>
    </tr>
    ";    
}

// --- HTML Template --------------------------------------------------------
$block = "
  <div class=\"detail admin\" >
  <form id=\"form_type_update\" action=\"?action=type_update\" method=\"post\">
  <input type=\"hidden\" name=\"tf_type_label\" value=\"\" />
  <input type=\"hidden\" name=\"type\" value=\"\" />
  </form>    
  <h1>$l_type_manage</h1>
  <div class=\"overflow\">
  <table>
  <thead>
  <tr>
  <td>&nbsp;</td>
  <td>$l_label</td>
  <td>&nbsp;</td>        
  </tr>    
  </thead>
  <tbody>
  $type_list
  </tbody>
  </table>
  </div>    
  <form id=\"form_type_new\" action=\"\" method=\"post\"
  onsubmit=\"return check_contract_type_new(this)\">
  <table>
  <thead>
  <tr>
  <td colspan=\"3\">$l_c_new</td>
  </tr>
  <tbody>   
  <tr>
  <td>$l_label</td>    
  </tr>       
  <tr>
  <td><input type=\"text\" name=\"tf_type_label\" value=\"\" /></td>    
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
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the type links
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function dis_contract_type_links($id) {
  global $l_type_link_contract, $l_type_can_delete, $l_type_cant_delete;
  global $l_type_delete, $l_back,$l_type_link_contract_no;
  global $display;


  $label = get_contract_type_label($id);
  $obm_q = run_query_contract_type_links($id);
  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  $nb_type = $obm_q->num_rows();
  if ($nb_type > 0) {
    $display["msg"] = display_warn_msg($l_type_cant_delete);
    $dis_link = "<h1>$label : $l_type_link_contract ($nb_type)</h1>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $typeid = $obm_q->f("contract_id");
      $typelabel = $obm_q->f("contract_label");
      $dis_link.= "<li><a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;contract_id=$typeid") . "\">$typelabel</a></li>";
    }
    if ($cpt < $nb_type) {
      $dis_link .= "...";
    }

    $dis_link .="<div class=\"buttons\">$dis_back</div>";

  } else {
    $display["msg"] = display_ok_msg($l_type_can_delete); 
    $dis_link = "<h1>$label : $l_type_link_contract_no</h1>
    
       <div class=\"buttons\">
       
        <form name=\"form_status_delete\" method= \"post\" action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"type_delete\" />
        <input type=\"hidden\" name=\"sel_type\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\" />
        </form>
	$dis_back
    </div>";

  }
  $block = "
 <div class=\"detail\">
    <ul>
    $dis_link
    </ul>
    </div>
  ";

  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_contract_display_pref($prefs) {
  global $l_contract_display;
  
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "contract");
  $dis_pref->pref_title = $l_contract_display;
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display contract select form
// parameters:
//   - $comp_q : company database result
//   - $title  : title to be displayed
//   - $url    : url to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_contract($con_q, $title, $url="") {
  global $l_select_contract, $l_close, $display;

  if ($url != "") {
    $jsfunc = "check_contract_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_contract_get_id(this)";
  }

  $display_cont = "
    <select name=\"contract_id\" size=\"10\" style=\"font-family:fixed,courier\">";

  while ($con_q->next_record()) {
    $id = $con_q->f("contract_id");
    $name = $con_q->f("contract_label");
    $company = $con_q->f("company_name");
    $display_cont .= "\n<option value=\"$id\">$company - $name</option> ";
  }
  $display_cont.="</select>";

  $display["title"] = $title;

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_entity\"
    onsubmit=\"if ($jsfunc) return true; else return false;\" action=\"\">

  <table>
  <tr>
    <th>
    $display_cont
    </th>
  </tr>
  </table>

  <div class=\"buttons\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_select_contract\" />
  </div>
  </form>

  <a href=\"\" onclick='window.close();'>$l_close</a>
";

  return $block;
}
 
?>
