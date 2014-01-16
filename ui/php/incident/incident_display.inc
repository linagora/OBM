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
// OBM - File :incident_display.php                                         //
//     - Desc : Incident File                                                //
// 2002-03-14 : Mehdi Rande                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["incident_status"] = $l_status;
$fieldnames["incident_label"] = $l_label;
$fieldnames["incident_reference"] = $l_reference;
$fieldnames["contract_label"] = $l_contract;
$fieldnames["incident_priority"] = $l_priority;
$fieldnames["incident_date"] = $l_date;
$fieldnames["incident_duration"] = $l_duration;
$fieldnames["incident_archive"] = $l_archive;
$fieldnames["resolutiontype_label"] = $l_resolutiontype;

// Calculate fields
$fieldnames["timeupdate"] = $l_lastupdate;
$fieldnames["incident_owner_lastname"] = $l_owner;
$fieldnames["incident_logger_lastname"] = $l_logger;
$fieldnames["incident_company_name"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// Display Incident specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_incident(&$OD, $fieldname, $link_ok) {
  global $path;
  global $col_inc_redhot, $col_inc_hot, $col_inc_closed;

  if ($fieldname == "incident_label") {
    $res["url"] = "$path/incident/incident_index.php?action=detailconsult&amp;incident_id=".$OD->data_set->f("incident_id");
  }

  else if ($fieldname == "incident_owner_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("incident_owner");
  }

  else if ($fieldname == "incident_logger_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;user_id=".$OD->data_set->f("incident_logger");
  }

  else if ($fieldname == "incident_duration") {
    $res["align"] = "center";
  }

  else if ($fieldname == "incident_date") {
    $res["name"] = of_date_format($OD->data_set->f("date"));
  }

  else if ($fieldname == "incident_priority") {
    $pri = $OD->data_set->f($fieldname);
    $col = $OD->data_set->f("incidentpriority_color");
    $res["name"] = $pri;
    $res["style"] = "style=\"color: #$col;\"";
  }

  else if ($fieldname == "incident_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }  else if ($fieldname == "incident_status") {
    $res["align"] = "center";
    $s_id = $OD->data_set->f('incident_status');
    $sta = get_incident_status();
    $res["name"] = $sta[$s_id];
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident search form
// Parameters:
//   - $incident[] 
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_form($incident) {

  $archive = $incident["archive"];

  $owner_q = run_query_incident_owner($archive);
  $logger_q = run_query_incident_logger($archive);

  $pri = of_category_get_ordered("incident", "priority");
  $sta = get_incident_status();
  $reso = of_category_get_ordered("incident", "resolutiontype","multi",0,"IncidentResolutionType");
  $block = html_incident_search_form($owner_q, $logger_q, $pri, $sta, $reso, $incident);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident search Form
// Parameters:
//   - $owner_q    : DB result object (userobm list)
//   - $logger_q   : DB result object (userobm list)
//   - $pri        : array with incident priority list
//   - $sta        : array with incident status list
//   - $reso       : array with incident resolutiontype list
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form($owner_q,$logger_q, $pri, $sta, $reso, $incident) {
  global $c_all, $l_all, $l_text, $l_label, $l_id, $l_find;
  global $l_owner, $l_logger,$l_company,$l_contract, $l_status;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_new,$l_open,$l_call,$l_standby,$l_closed;
  global $l_date_after,$l_date_before,$l_archive,$l_reference;

  $text = stripslashes($incident["text"]);
  $lincident = stripslashes($incident["lincident"]);
  $reference = stripslashes($incident["reference"]);
  $i_id = $incident["incident_id"];
  $contract_id = $incident["contract_id"];
  $lcontract = stripslashes($incident["lcontract"]);
  $priority = $incident["priority"];
  $company = $incident["company"];
  $status = $incident["status"];
  $resolutiontype = $incident["resolutiontype"];
  $owner = $incident["owner"];
  $dateafter = of_date_upd_format($incident["date_after"],true);
  $datebefore = of_date_upd_format($incident["date_before"],true);
  $archive = ($incident["archive"] == "1" ? "checked=\"checked\"" : "");

  // User defined data
  $block_userdata .= of_userdata_dis_search("incident", $incident);

  $dis_sel_owner="<select name=\"sel_owner\">
    <option value=\"$c_all\">$l_all</option>";
  while($owner_q->next_record()) {
    $id = $owner_q->f("userobm_id");
    $dis_sel_owner.="\n<option value=\"$id\"";
    if ($id == $owner) 
      $dis_sel_owner.=" selected=\"selected \"";
    $dis_sel_owner.=">". $owner_q->f("userobm_lastname") . " " . $owner_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $dis_sel_logger="<select name=\"sel_logger\">
    <option value=\"$c_all\">$l_all</option>";
  while($logger_q->next_record()) {
    $id = $logger_q->f("userobm_id");
    $dis_sel_logger.="\n<option value=\"$id\"";
    if ($id == $logger) 
      $dis_sel_logger .=" selected=\"selected \"";
    $dis_sel_logger.=">". $logger_q->f("userobm_lastname") . " " . $logger_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_logger .= "</select>";

  $sel_status_all = ($status == $c_all ? " selected = \"selected\"" : '');
  $sel_status = "<select name=\"sel_status\">
      <option value=\"$c_all\"$sel_status_all>$l_all</option>";
  if (is_array($sta)) {
    foreach ($sta as $s_id => $s_label) {
      $sel_status .= "<option value=\"$s_id\"";
      if ($status == "$s_id") {
	$sel_status .= " selected = \"selected\"";
      }
      $sel_status .= ">$s_label</option>";
    }
  }
  $sel_status .= "</select>";


  // Categories priority, resolutiontype
  $block_priority = of_category_dis_search_select("incident", "priority", $pri, $priority);
  $block_resolutiontype = of_category_dis_search_select("incident", "resolutiontype", $reso, $resolutiontype);

  // --- html template --------------------------------------------------------

  $block = "
  <form  class=\"search\" method=\"get\" name=\"f_search\" action=\"".url_prepare("incident_index.php")."\">

    <label>$l_text<br />
      <input type=\"text\" name=\"tf_text\" size=\"16\" value=\"$text\" />
    </label>
    <label>$l_label<br />
      <input type=\"text\" name=\"tf_lincident\" size=\"16\" value=\"$lincident\" />
    </label>
    <label>$l_reference<br />
      <input type=\"text\" name=\"tf_reference\" size=\"16\" value=\"$reference\" />
    </label>
    <label>$l_id<br />
      <input type=\"text\" name=\"tf_incident_id\" size=\"10\" value=\"$i_id\" />
    </label>
    <label>$l_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
    </label>
    <label>$l_contract<br />
      <input type=\"text\" name=\"tf_lcontract\" size=\"16\" value=\"$lcontract\" />
    </label>
    <label>$l_date_after<br />
      <input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
    </label>
    <label>$l_date_before<br />
    <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
    </label>
    $block_priority

    <label>$l_status<br />
      $sel_status
    </label>


    $block_resolutiontype
    <label>$l_logger<br />
      $dis_sel_logger
    </label>
    <label>$l_owner<br />
      $dis_sel_owner
    </label>
    $block_userdata
    <label>$l_archive<br />
      <input name=\"cba_archive\" type=\"checkbox\" size=\"10\" value=\"1\" $archive />
    </label>
    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"contract_id\" id=\"contract_id\" type=\"hidden\" value=\"$contract_id\" />     
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
     </label>
     <p class=\"CL\" />
   </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident search result
// Parameters:
//   - $incident[] : incident search criteria
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_list($incident) {
  global $obm, $display, $l_found, $l_no_found;

  $prefs = get_display_pref($obm["uid"], "incident");
  $obm_q = run_query_incident_search($incident);
  $nb_incident = $obm_q->num_rows_total();
  if ($nb_incident == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $display["msg"] = display_info_msg("$nb_incident $l_found");
    $block = html_incident_search_list($obm_q, $prefs, $incident);
  }
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result
// Parameters : 
//   - $in_q        : list of incidents
//   - $prefs       : the fields which have to be displayed
//   - $incident[]  : incidents search criteria
//     keys used    : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_list($in_q, $prefs, $incident) {

  $text = urlencode($incident['text']);
  $lincident = urlencode($incident["lincident"]);
  $lcontract = urlencode($incident["lcontract"]);
  $company = urlencode($incident["company"]);
  $priority = $incident["priority"];
  $status = $incident["status"];
  if (is_array($status) && (count($status) > 0)) {
    foreach ($status as $s_id) {
      $param_status .= "&amp;sel_status[]=$s_id";
    }
  } else if ($status != '') {
    $param_status .= "&amp;sel_status=$status";
  }  
  $reso = $incident["resolutiontype"];
  $contract_id = $incident["contract_id"];
  $owner = $incident["owner"];
  $logger = $incident["logger"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  // User defined data
  $url_userdata = of_userdata_get_url_search_params("incident", $incident);

  $url = url_prepare("incident_index.php?action=search$url_userdata&amp;tf_text=$text&amp;tf_lincident=$lincident&amp;tf_company=$company&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority$param_status&amp;sel_resolutiontype=$reso&amp;contract_id=$contract_id&amp;tf_date_after=$dateafter&amp;tf_date_before=$datebefore&amp;sel_owner=$owner&amp;sel_logger=$logger&amp;cba_archive=$archive");

  $dis_incidents_list=new OBM_DISPLAY("DATA", $prefs, "incident");
  $dis_incidents_list->data_set = $in_q;
  $dis_incidents_list->data_url = $url;
  $dis_incidents_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  $block = $dis_incidents_list->display("dis_data_incident");
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident consult
// Parameters:
//   - $incident[]    
///////////////////////////////////////////////////////////////////////////////
function dis_incident_consult($incident) {
  global $display, $l_err_reference;

  $id = $incident["incident_id"];

  if ($id > 0) {
    $inc_q = run_query_incident_detail($id);
    if ($inc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($inc_q);
      $display["link"] = html_incident_links($inc_q);
      $block = html_incident_consult($inc_q);
    } else {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display HTML Incident Consult
// Parameters:
//   - $inc_q : DBO : incident result
///////////////////////////////////////////////////////////////////////////////
function html_incident_consult($inc_q) {
  global $display, $path, $l_yes, $l_no, $cmail_incident;
  global  $ico_contract, $ico_company, $ico_mail;
  global $l_incident, $l_contract, $l_company;
  global $l_label, $l_logger, $l_owner, $l_priority, $l_status, $l_archive;
  global $l_duration, $l_solution, $l_date,$l_reference;
  global $l_client_manager;
  global $l_comment;

  $id = $inc_q->f("incident_id");
  $com = beautify_comment(nl2br($inc_q->f("incident_comment")));
  $label = htmlentities($inc_q->f("incident_label"), ENT_COMPAT, 'UTF-8');
  $reference = $inc_q->f("incident_reference");
  $priority = $inc_q->f("incidentpriority_label");
  $status_id = $inc_q->f("incident_status_id");
  $status = get_incident_status();
  $status_label = $status[$status_id];  
  $date = of_datetime_format($inc_q->f("date"), 1);
  $inc_id = $inc_q->f("incident_id");
  $inc_dur = nl2br($inc_q->f("incident_duration"));
  $resolution = $inc_q->f("incident_resolution");
  $inc_reso = nl2br(htmlentities($resolution, ENT_COMPAT, 'UTF-8'));
  $con_id = $inc_q->f("contract_id");
  $comp_id = $inc_q->f("contract_company_id");
  $comp_name = $inc_q->f("company_name");
  $con_status = $inc_q->f("contractstatus_label");
  $con_label = $inc_q->f("contract_label");
  $con_num = $inc_q->f("contract_number");
  $con_debut = of_date_format($inc_q->f("datebegin"));
  $con_exp = of_date_format($inc_q->f("dateexp"));
  $logger = $inc_q->f("lname1") . " " . $inc_q->f("fname1");
  $owner = $inc_q->f("lname2") . " " . $inc_q->f("fname2");
  $dis_con1 = $inc_q->f("lname_c1") . " " . $inc_q->f("fname_c1") . " - " . $inc_q->f("phone_c1");
  $dis_con2 = $inc_q->f("lname_c2") . " " . $inc_q->f("fname_c2") . " - " . $inc_q->f("phone_c2");
  $archive = ($inc_q->f("incident_archive") == 1 ? $l_yes : $l_no);
  $email_con1 = $inc_q->f("email_c1");
  $email_con2 = $inc_q->f("email_c2");
  $email_con = ($email_con1 != "" ? $email_con1 : $email_con2);

  // Categories
  $resos = of_category_get_entitycategories("incident", "resolutiontype", $inc_id, "mono","IncidentResolutionType");
  $block_reso = of_category_dis_block_consult("incident", "resolutiontype", $resos, "mono");

  // User defined data
  $block_userdata .= of_userdata_dis_entity_consult("incident", $id);

  if ($inc_q->f("incident_archive") == 1) {
    $dis_lab_archive ="<tr><th colspan=\"2\">$l_archive_consult</th></tr>";
  }
  $display["title"] = $label;

  // --- HTML Template --------------------------------------------------------

  $block = "
  
  <div class=\"detail infos\">
  <h1>$l_contract</h1>
  <table>
  <tr>
    <th>$l_company
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id")."\">
      <img src=\"$ico_company\" alt=\"[Company]\" /></a>
    </th>
    <td>$comp_name</td>
  </tr><tr>
    <th>$l_client_manager 1</th>
    <td>$dis_con1</td>
  </tr><tr>
    <th>$l_client_manager 2</th>
    <td>$dis_con2</td>
  </tr><tr>
    <th>
      <a href=\"". url_prepare("$path/contact/contact_index.php?action=consult&amp;id=$con_cli2")."\"></a> 
      $con_label
      <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;contract_id=$con_id") . "\">
      <img src=\"$ico_contract\" alt=\"[Contract]\" /></a>&nbsp;&nbsp;
    </th>
    <td>
      # $con_num ($con_debut - $con_exp)
    </td>
  </tr><tr>
    <th>$l_status</th>
    <td>$con_status</td>
  </tr>
  </table>
  </div>

<div class=\"detail infos\">
  <h1>$l_incident (# $id)</h1>
  <table> 
  <tr>
    <th>$l_label</th>
    <td>$label</td>
  </tr><tr>
    <th>$l_reference</th>
    <td>$reference</td>
  </tr><tr>
    <th>$l_logger</th>
    <td>$logger</td>
  </tr><tr>
    <th>$l_owner</th>
    <td>$owner</td>
  </tr><tr>
    <th>$l_priority</th>
    <td>$priority</td>
  </tr><tr>
    <th>$l_status</th>
    <td>$status_label</td>
  </tr><tr>
    <th>$l_date</th>
    <td>$date</td>
  </tr>
  $block_reso
  <tr>
    <th>$l_duration</th>
    <td>$inc_dur</td>
  </tr><tr>
    <th>$l_archive</th>
    <td>$archive</td>
  </tr>
  </table>
</div>
  $block_userdata

<div class=\"detail extra\">
  <h1>$l_solution <a href=\"mailto:$email_con?subject=$l_incident $label : $l_solution&cc=$cmail_incident&body=$inc_reso\"><img src=\"$ico_mail\" alt=\"[Mail]\" /></a>
</h1>
    $inc_reso
 </div>

<div class=\"detail extra\">
  <h1>$l_comment</h1>
    $com
</div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Incident links block
// Parameters:
//   - $inc_q : incident database result 
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_incident_links($inc_q) {
  global  $ico_document, $ico_add;
  global $l_new, $l_incident, $l_module_document, $l_document_add;
  global $path, $cgp_show, $perm;

  $id = $inc_q->f("incident_id");
  $entity = "incident";

  // Document
  if ($perm->check_module_rights("document")) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=$entity");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=$entity");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/incident/incident_index.php")."&amp;ext_id=$id&amp;ext_target=$l_incident";
    $nb_document = run_query_global_document_nb ($id, "incident");
    $block_doc = "
  <div class=\"portlet\" id=\"incidentDocument\">
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" /></a>
	<a href=\"\" onclick=\"window.name='$l_incident'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul></div>";
  }

  // Links Template  
  $block = " $block_doc ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Form
// Parameters:
//   - $action    : action called
//   - $incident[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_incident_form($action, $incident) {
  global $display, $obm;

  $id = $incident ["incident_id"];
  if ($action == "new") {
    $inc_q = "";
    $users = "";
  }
  if ($action == "detailupdate") {
    if ($id > 0) {
      $inc_q = run_query_incident_detail($id);
      $users = array($inc_q->f("incident_owner"),$inc_q->f("incident_logger"));
      $display["detailInfo"] = display_record_info($inc_q);
    }
  }

  if ( ($action == "insert") || ($action == "update") ) {
    $users = array($incident["owner"], $incident["logger"]);
    $inc_q = "";
  }

  $usr_q = run_query_userobm_active($users);
  $users_p = of_usergroup_get_group_users($obm['group_prod'], true, $users, 'userobm_lastname');

  $pri = of_category_get_ordered("incident", "priority");
  $sta = get_incident_status();
  $resos = of_category_get_ordered("incident", "resolutiontype","multi",0,"IncidentResolutionType");

  $block = html_incident_form($action, $incident, $inc_q, $usr_q, $users_p, $pri, $sta, $resos);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Incident Form
// Parameters:
//   - $action     : action called
//   - $incident[] : default values
//   - $inc_q      : incident database result 
//   - $usr_q      : userobm database result 
//   - $users_p    : userobm list from domain group Production
//   - $pri        : array with incident priority list
//   - $sta        : array with incident status list
//   - $resos      : array with incident resolution types
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $incident, $inc_q, $usr_q, $users_p, $pri, $sta, $resos) {
  global $display, $path, $obm, $ico_contract, $l_header_new;
  global $c_php_isodate_format;
  global $l_label,$l_contract,$l_status, $l_find,$l_reference;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_new,$l_standby,$l_closed;
  global $l_logger,$l_owner,$l_priority,$l_incident;
  global $l_update, $l_insert;
  global $l_duration,$l_solution,$l_date, $l_archive_new,$l_res_duration;
  global $l_select_contract,$l_comment,$l_upd_comment,$l_add_comment;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $param_contract = $inc_q->f("incident_contract_id");
    $lincident = $inc_q->f("incident_label");
    $reference = $inc_q->f("incident_reference");
    $owner = $inc_q->f("incident_owner");
    $logger = $inc_q->f("incident_logger");
    $duration = $inc_q->f("incident_duration");
    $comment = $inc_q->f("incident_comment");
    $solu = $inc_q->f("incident_resolution");
    $priority = $inc_q->f("incident_priority_id");
    $status = $inc_q->f("incident_status_id");
    $reso = $inc_q->f("incident_resolutiontype_id");
    $datecomment = of_date_upd_format();
    $date = of_date_upd_format($inc_q->f("date"));
    $hour = of_date_get_hour($inc_q->f("date"));
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked=\"checked\"" : "");
    $id = $inc_q->f("incident_id");
    $c_label = $inc_q->f("contract_label");
    $dis_title = $lincident;
  } elseif ($action == "new") {
    // We pre-fill date with current date
    $date = of_date_upd_format();
    $datecomment = of_date_upd_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($incident["contract_id"])) { $param_contract = $incident["contract_id"]; }
  if (isset($incident["lincident"])) { $lincident = stripslashes($incident["lincident"]); }
  if (isset($incident["owner"])) { $owner = $incident["owner"]; }
  if (isset($incident["logger"])) { $logger = $incident["logger"]; }
  if (isset($incident["duration"])) { $duration = $incident["duration"]; }
  if (isset($incident["solution"])) { $solu = stripslashes($incident["solution"]); }
  if (isset($incident["priority"])) { $priority = $incident["priority"]; }
  if (isset($incident["status"])) { $status = $incident["status"]; }
  if (isset($incident["reso"])) { $reso = $incident["resolutiontype"]; }
  if (isset($incident["date"])) { $date = of_date_upd_format($incident["date"]); }
  if (isset($incident["hour"])) { $hour = $incident["hour"]; }
  if (isset($incident["archive"])) { $archive = ($incident["archive"] == 1 ? "checked" : ""); }
  if (isset($incident["lcontract"])) { $c_label = stripslashes($incident["lcontract"]); }
  if (isset($incident["comment"])) { $comment = stripslashes($incident["comment"]); }
  if (isset($incident["add_comment"])) { $add_comment = stripslashes($incident["add_comment"]); }
  if (isset($incident["usercomment"])) { $usercomment = $incident["usercomment"]; }
  if (isset($incident["datecomment"])) { $datecomment = of_date_upd_format($incident["datecomment"]); }
  if (isset($incident["reference"])) { $reference = $incident["reference"]; }

  // User defined data
  $block_userdata = of_userdata_dis_entity_form("incident", $id, $incident);

  // Owner select
  if (! isset($owner)) { $owner = $obm["uid"]; }
  $dis_sel_owner="<select name=\"sel_owner\">";
  if (is_array($users_p)) {
    foreach ($users_p as $u_id => $one_user) {
      $dis_sel_owner.="\n<option value=\"$u_id\"";
      if ($u_id == $owner) {
        $dis_sel_owner.=" selected=\"selected\"";
      }
      $dis_sel_owner .= '>'. $one_user['lastname'].' '.$one_user['firstname'].'</option>';
    }
  }
  $dis_sel_owner .= "</select>";


  if (! isset($logger)) { $logger = $obm["uid"]; }
// FIXME: please, think to clean this !
  while ($usr_q->next_record()) {
    if ($usr_q->f("userobm_id") == $logger) 
      $logger_name = $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname");
  }

  $dis_sel_logger = "<input type=\"text\" name=\"\" value=\"$logger_name\" id=\"tf_logger\" autocomplete=\"off\" />
                     <input type=\"hidden\" name=\"sel_logger\" value=\"$logger\" id=\"sel_logger\" />";


  // Status select
  $sel_status = "<select name=\"sel_status\">"; 
  foreach ($sta as $s_id => $s_label) {
    $sel_status .= "<option value=\"$s_id\""; 
    if ($s_id == $status) {
      $sel_status .= " selected ";
    }
    $sel_status .= ">$s_label</option>"; 
  }
  $sel_status .= "</select>";

  $block_priority = of_category_dis_entity_form("incident", "priority", $pri, "mono", $priority);
  $block_reso = of_category_dis_entity_form("incident", "resolutiontype", $resos, "mono", $reso);

  // Hour select
  $sel_hour = "<select name=\"sel_hour\" style=\"width:4em;\">";
  for ($cpt = 0; $cpt < 24; $cpt++) {
    $sel_hour .= "\n<option value=\"$cpt\"";
    if ($cpt == $hour) 
      $sel_hour .= " selected=\"selected\"";
    $sel_hour .= ">$cpt</option>";
  }
  $sel_hour .= "</select>";

  // Duration select 2
  $sel_dur = "<select name=\"sel_add_duration\">";
  for ($cpt = 0.25; $cpt < 2; $cpt += 0.25) {
    $sel_dur .= "\n<option value=\"$cpt\"";
    $sel_dur .= ">$cpt</option>";
  }
  for ($cpt = 2; $cpt < 17; $cpt += 1) {
    $sel_dur .= "\n<option value=\"$cpt\"";
    $sel_dur .= ">$cpt</option>";
  }
  $sel_dur .= "</select>";

  // User comment select construction
  if ($usr_q->nf()>0 ) {
    $usr_q->seek(0);
  }
  if (! isset($usercomment)) { $usercomment = $obm["uid"]; }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    if ($usercomment == $cid) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      </tr><tr>
        <th>$l_upd_comment</th>
        <td>&nbsp;</td>
      </tr><tr>
        <td colspan=\"2\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea></td>";
  }

  // update 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"incident_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // insert
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $dis_button .="<input type=\"submit\" value=\"$l_insert\" />";
  }
  $display["title"] = $dis_title;

  // --- html template --------------------------------------------------------

  $block = "
  <form method=\"post\" name=\"f_entity\" onsubmit=\"if (check_incident(this)) return true; else return false;\" action=\"". url_prepare("incident_index.php")."\">
      
  <fieldset class=\"detail infos\">  
    <legend>$l_contract</legend>
    <table>
    <tr>
      <th>$l_contract</th>
      <td>
      <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;contract_id=$param_contract")."\">$c_label</a>
      <input type=\"hidden\" name=\"contract_id\" value=\"$param_contract\" />
      <input type=\"hidden\" name=\"contract_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"popup('$path/contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_contract)."&amp;ext_widget=f_entity.contract_new_id&amp;ext_widget_text=f_entity.contract_new_name','contract'); return false;\">
      <img src=\"$ico_contract\" alt=\"[Contract]\" /></a>
      <input type=\"text\" name=\"contract_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </td>
    </tr>
    </table>
  </fieldset>
      
  <fieldset class=\"detail infos\">  
    <legend>$l_incident</legend>
    <table>
    <tr>
      <th>$l_label</th>
      <td><input name=\"tf_lincident\" size=\"30\" value=\"$lincident\" /></td>
    </tr><tr>
      <th>$l_reference</th>
      <td><input name=\"tf_reference\" size=\"10\" value=\"$reference\" /></td>
    </tr><tr>
      <th>$l_logger</th>
      <td>$dis_sel_logger</td>
    </tr><tr>
      <th>$l_owner</th>
      <td>$dis_sel_owner</td>
    </tr><tr>
      <th><label for=\"cba_archive\">$l_archive_new</label></th>
      <td><input id=\"cba_archive\" name=\"cba_archive\" type=\"checkbox\"size=\"30\" value=\"1\" $archive /></td>
    </tr>
    $block_priority
    <tr>
      <th>$l_status</th>
      <td>$sel_status</td>
    </tr>
    $block_reso
    $block_userdata
    <tr>
      <th>$l_date</th>
      <td><input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>  $sel_hour H</td>
    </tr><tr>
      <th>$l_duration</th>
      <td><input type=\"text\" name=\"tf_duration\" size=\"6\" maxlength=\"6\" value=\"$duration\" readonly=\"readonly\" />H</td>
    </tr>
    </table>
  </fieldset>

  <fieldset class=\"detail extra\">  
    <legend>$l_solution</legend>
    <table>
    <tr>
      <td colspan=\"2\"><textarea name=\"ta_solution\" rows=\"6\" cols=\"60\">$solu</textarea></td>
    </tr>
    </table>
  </fieldset>

  <fieldset class=\"detail extra\">  
    <legend>$l_comment</legend>
    $l_add_comment
    <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
    $sel_usercomment
    $l_res_duration
    $sel_dur 
    <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>
    $dis_comment
  </fieldset>

  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>
  </form>
  <script type=\"text/javascript\">
    obm.initialize.chain(function () {
      new obm.AutoComplete.Search('$path/user/user_index.php?action=ext_search', 'sel_logger', 'tf_logger', {mode: 'mono', locked: true});
    });
  </script>
";

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the incident delete validation screen
// Parameters:
//   - $p_id : incident id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_incident($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("incident_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"incident_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"incident_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\" />
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
// Display the Incident administration index                                 //
///////////////////////////////////////////////////////////////////////////////
function dis_incident_admin_index() {

  $block = "";

  $pri = of_category_get_ordered("incident", "priority");
  $block .= of_category_dis_admin_form("priority", $pri);

  $resos = of_category_get_ordered("incident", "resolutiontype","multi",0,"IncidentResolutionType");
  $block .= of_category_dis_admin_form("resolutiontype", $resos);

  // User defined data
  $block .= of_userdata_dis_admin_form("incident");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_incident_display_pref($prefs) {
  global $l_incident_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "incident");
  $dis_pref->pref_title = $l_incident_display;
  $dis_pref->pref_dis_help = 1;
 
 // --- HTML Template --------------------------------------------------------

  return $dis_pref->display();
}

?>
