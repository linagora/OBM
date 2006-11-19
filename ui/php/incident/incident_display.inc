<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : incident_display.php                                         //
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
$fieldnames["incident_priority"] = $l_priority;
$fieldnames["incident_date"] = $l_date;
$fieldnames["incident_duration"] = $l_duration;
$fieldnames["category1_label"] = $l_category1;
$fieldnames["category2_label"] = $l_category2;

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
  global $path, $set_theme;
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
    $res["style"] = "style=\"color: #$col; font-family: sans-serif; font-size: 10px; background-color: #ffffff;\"";
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

  $cats1 = of_category_get_ordered("incident", "category1");
  $cats2 = of_category_get_ordered("incident", "category2");
  $pri = of_category_get_ordered("incident", "priority");
  $sta = of_category_get_ordered("incident", "status");
  $block = html_incident_search_form($owner_q, $logger_q, $pri, $sta, $cats1, $cats2, $incident);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident search Form
// Parameters:
//   - $owner_q    : DB result object (userobm list)
//   - $logger_q   : DB result object (userobm list)
//   - $pri        : array with incident priority list
//   - $sta        : array with incident status list
//   - $cats1      : array with incident category 1 list
//   - $cats2      : array with incident category 2 list
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form($owner_q,$logger_q, $pri, $sta, $cats1, $cats2, $incident) {
  global $c_all, $l_all, $l_text, $l_label, $l_id, $l_find;
  global $l_owner, $l_logger,$l_company,$l_contract;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
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
  $cat1 = $incident["category1"];
  $cat2 = $incident["category2"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = ($incident["archive"] == "1" ? "checked=\"checked\"" : "");

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
      $dis_sel_logger.=" selected=\"selected \"";
    $dis_sel_logger.=">". $logger_q->f("userobm_lastname") . " " . $logger_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_logger.="</select>";

  // Categories priority, status, category1
  $block_priority = of_category_dis_search_select("incident", "priority", $pri, $priority);
  $block_status = of_category_dis_search_select("incident", "status", $sta, $status);
  $block_category1 = of_category_dis_search_select("incident", "category1", $cats1, $cat1);
  $block_category2 = of_category_dis_search_select("incident", "category2", $cats2, $cat2);

  // --- html template --------------------------------------------------------

  $block = "
    <form method=\"get\" name=\"f_search\" action=\"" .
    url_prepare("incident_index.php") . " \">

    <div class=\"search\">
       <div class=\"searchLabel\">$l_text<br />
         <input type=\"text\" name=\"tf_text\" size=\"16\" value=\"$text\" />
       </div>
       <div class=\"searchLabel\">$l_label<br />
         <input type=\"text\" name=\"tf_lincident\" size=\"16\" value=\"$lincident\" />
       </div>
       <div class=\"searchLabel\">$l_reference<br />
         <input type=\"text\" name=\"tf_reference\" size=\"16\" value=\"$reference\" />
       </div>
       <div class=\"searchLabel\">$l_id<br />
         <input type=\"text\" name=\"tf_incident_id\" size=\"10\" value=\"$i_id\" />
       </div>
       <div class=\"searchLabel\">$l_company<br />
         <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
       </div>
       <div class=\"searchLabel\">$l_contract<br />
         <input type=\"text\" name=\"tf_lcontract\" size=\"16\" value=\"$lcontract\" />
       </div>
       <div class=\"searchLabel\">$l_date_after<br />
        <script>calendar('tf_date_after','$dateafter')</script>
       </div>
       <div class=\"searchLabel\">$l_date_before<br />
        <script>calendar('tf_date_before','$datebefore')</script>
       </div>
       $block_priority
       $block_status
       $block_category1
       $block_category2
       <div class=\"searchLabel\">$l_logger<br />
         $dis_sel_logger
       </div>
       <div class=\"searchLabel\">$l_owner<br />
         $dis_sel_owner
       </div>
       <div class=\"searchLabel\">$l_archive<br />
         <input name=\"cba_archive\" type=\"checkbox\" size=\"10\" value=\"1\" $archive />
       </div>
       <div class=\"searchLabel\">&nbsp;<br />
         <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />     
         <input name=\"contract_id\" id=\"contract_id\" type=\"hidden\" value=\"$contract_id\" />     
         <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
       </div>
       <div class=\"searchEnd\"></div>
       </div>
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
  global $display, $l_found, $l_no_found;
  global $auth;

  $prefs = get_display_pref($auth->auth["uid"], "incident");
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

  $lincident = urlencode($incident["lincident"]);
  $lcontract = urlencode($incident["lcontract"]);
  $company = urlencode($incident["company"]);
  $priority = $incident["priority"];
  $status = $incident["status"];
  $cat1 = $incident["category1"];
  $cat2 = $incident["category2"];
  $contract_id = $incident["contract_id"];
  $owner = $incident["owner"];
  $logger = $incident["logger"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  $url = url_prepare("incident_index.php?action=search&amp;tf_lincident=$lincident&amp;tf_company=$company&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority&amp;sel_status=$status&amp;sel_category1=$cat1&amp;sel_category2=$cat2&amp;contract_id=$contract_id&amp;tf_date_after=$dateafter&amp;tf_date_before=$datebefore&amp;sel_owner=$owner&amp;sel_logger=$logger&amp;cba_archive=$archive");

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
  global $set_theme, $ico_contract, $ico_company, $ico_mail;
  global $l_incident, $l_contract, $l_company;
  global $l_label, $l_logger, $l_owner, $l_priority, $l_status, $l_archive;
  global $l_duration, $l_solution, $l_date,$l_reference;
  global $l_client_manager;
  global $l_comment;

  $id = $inc_q->f("incident_id");
  $com = beautify_comment(nl2br($inc_q->f("incident_comment")));
  $label = htmlentities($inc_q->f("incident_label"));
  $reference = $inc_q->f("incident_reference");
  $priority = $inc_q->f("incidentpriority_label");
  $status = $inc_q->f("incidentstatus_label");
  $date = of_datetime_format($inc_q->f("date"), 1);
  $inc_id = $inc_q->f("incident_id");
  $inc_dur = nl2br($inc_q->f("incident_duration"));
  $resolution = $inc_q->f("incident_resolution");
  $inc_reso = nl2br(htmlentities($resolution));
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
  $cats1 = of_category_get_entitycategories("incident", "category1", $inc_id, "mono");
  $block_category1 = of_category_dis_block_consult("incident", "category1", $cats1, "mono");
  $cats2 = of_category_get_entitycategories("incident", "category2", $inc_id, "mono");
  $block_category2 = of_category_dis_block_consult("incident", "category2", $cats2, "mono");

  if ($inc_q->f("incident_archive") == 1) {
    $dis_lab_archive ="<tr><td class=\"detailLabel\" colspan=\"2\">$l_archive_consult</td></tr>";
  }
  $display["title"] = "<div class=\"title\">$l_incident : $label</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <div class=\"detailHead\">$l_contract</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_company
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id")."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
    </td>
    <td class=\"detailText\">$comp_name</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 1</td>
    <td class=\"detailText\">$dis_con1</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 2</td>
    <td class=\"detailText\">$dis_con2</td>
  </tr><tr>
    <td class=\"detailLabel\">
      <a href=\"". url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;contract_id=$con_cli2")."\"></a> 
      $con_label
      <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;contract_id=$con_id") . "\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>&nbsp;&nbsp;
    </td>
    <td class=\"detailText\">
      # $con_num ($con_debut - $con_exp)
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_status</td>
    <td class=\"detailText\">$con_status</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_incident (# $id)</div>
  <table class=\"detail\"> 
  <tr>
    <td class=\"detailLabel\">$l_label</td>
    <td class=\"detailText\">$label</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_reference</td>
    <td class=\"detailText\">$reference</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_logger</td>
    <td class=\"detailText\">$logger</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_owner</td>
    <td class=\"detailText\">$owner</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_priority</td>
    <td class=\"detailText\">$priority</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_status</td>
    <td class=\"detailText\">$status</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_date</td>
    <td class=\"detailText\">$date</td>
  </tr>
  $block_category1
  $block_category2
  <tr>
    <td class=\"detailLabel\">$l_duration</td>
    <td class=\"detailText\">$inc_dur</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_archive</td>
    <td class=\"detailText\">$archive</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_solution <a href=\"mailto:$email_con?subject=$l_incident $label : $l_solution&cc=$cmail_incident&body=$inc_reso\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_mail\" alt=\"[Mail]\" /></a>
</div>
  <table class=\"detail\"> 
  <tr>
    <td colspan=\"2\" class=\"detailText\">$inc_reso</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_comment</div>
  <table class=\"detail\">
  <tr>
    <td colspan=\"2\" class=\"detailText\">$com</td>
  </tr>
  </table>
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
  global $set_theme, $ico_document, $ico_document_new;
  global $l_new, $l_incident, $l_module_document, $l_document_add;
  global $path, $auth, $cgp_show, $popup_height, $popup_width;

  $id = $inc_q->f("incident_id");
  $entity = "incident";

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;entity_id=$id&amp;entity=$entity");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;entity_id=$id&amp;entity=$entity");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/incident/incident_index.php")."&amp;ext_id=$id&amp;ext_target=$l_incident";
    $nb_document = run_query_global_document_nb ($id, "incident");
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
	<a href=\"\" onclick=\"window.name='$l_incident'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Links Template  
  $block = "
<div class=\"link\"> 
  $block_doc
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Form
// Parameters:
//   - $action    : action called
//   - $incident[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_incident_form($action, $incident) {
  global $display, $cg_com, $cg_prod;

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
  $usrp_q = run_query_all_users_from_group($cg_prod,$users);

  $pri = of_category_get_ordered("incident", "priority");
  $sta = of_category_get_ordered("incident", "status");
  $cats1 = of_category_get_ordered("incident", "category1");
  $cats2 = of_category_get_ordered("incident", "category2");

  $block = html_incident_form($action, $incident, $inc_q, $usr_q, $usrp_q, $pri, $sta, $cats1, $cats2);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Incident Form
// Parameters:
//   - $action     : action called
//   - $incident[] : default values
//   - $inc_q      : incident database result 
//   - $usr_q      : userobm database result 
//   - $usrp_q     : userobm list from system group Production
//   - $pri        : array with incident priority list
//   - $sta        : array with incident status list
//   - $cats1      : array with incident category 1 list
//   - $cats2      : array with incident category 2 list
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $incident, $inc_q, $usr_q, $usrp_q, $pri, $sta, $cats1, $cats2) {
  global $display, $path, $uid, $set_theme, $ico_contract;
  global $c_php_isodate_format;
  global $l_label,$l_contract,$l_status, $l_find,$l_reference;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
  global $l_logger,$l_owner,$l_priority,$l_incident;
  global $l_update, $l_insert;
  global $l_duration,$l_solution,$l_date, $l_archive_new,$l_res_duration;
  global $popup_height, $popup_width, $l_select_contract,$l_comment,$l_upd_comment,$l_add_comment;

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
    $cat1 = $inc_q->f("incident_category1_id");
    $cat2 = $inc_q->f("incident_category2_id");
    $datecomment = of_isodate_format();
    $date = of_isodate_format($inc_q->f("date"));
    $hour = of_date_get_hour($inc_q->f("date"));
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked=\"checked\"" : "");
    $param_incident = $inc_q->f("incident_id");
    $c_label = $inc_q->f("contract_label");
  } elseif ($action == "new") {
    // We pre-fill date with current date
    $date = date($c_php_isodate_format);
    $datecomment = of_isodate_format();
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
  if (isset($incident["cat1"])) { $cat1 = $incident["cat1"]; }
  if (isset($incident["cat2"])) { $cat2 = $incident["cat2"]; }
  if (isset($incident["date"])) { $date = $incident["date"]; }
  if (isset($incident["hour"])) { $hour = $incident["hour"]; }
  if (isset($incident["archive"])) { $archive = ($incident["archive"] == 1 ? "checked" : ""); }
  if (isset($incident["lcontract"])) { $c_label = stripslashes($incident["lcontract"]); }
  if (isset($incident["comment"])) { $comment = stripslashes($incident["comment"]); }
  if (isset($incident["add_comment"])) { $add_comment = stripslashes($incident["add_comment"]); }
  if (isset($incident["usercomment"])) { $usercomment = $incident["usercomment"]; }
  if (isset($incident["datecomment"])) { $datecomment = $incident["datecomment"]; }
  if (isset($incident["reference"])) { $reference = $incident["reference"]; }

  // Owner select
  if (! isset($owner)) { $owner = $uid; }
  $dis_sel_owner="<select name=\"sel_owner\">";
  while ($usrp_q->next_record()) {
    $dis_sel_owner.="\n<option value=\"".$usrp_q->f("userobm_id")."\"";
    if ($usrp_q->f("userobm_id") == $owner) {
      $dis_sel_owner.=" selected=\"selected\"";
    }
    $dis_sel_owner.=">". $usrp_q->f("userobm_lastname") . " " . $usrp_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner .= "</select>";

  // Logger select
  if (! isset($logger)) { $logger = $uid; }
  $dis_sel_logger="<select name=\"sel_logger\">";
  while ($usr_q->next_record()) {
    $dis_sel_logger.="\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $logger) 
      $dis_sel_logger.=" selected=\"selected\"";
    $dis_sel_logger.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_logger.="</select>";

  $block_priority = of_category_dis_entity_form("incident", "priority", $pri, "mono", $priority);
  $block_status = of_category_dis_entity_form("incident", "status", $sta, "mono", $status);
  $block_category1 = of_category_dis_entity_form("incident", "category1", $cats1, "mono", $cat1);
  $block_category2 = of_category_dis_entity_form("incident", "category2", $cats2, "mono", $cat2);

  // Hour select
  $sel_hour = "<select name=\"sel_hour\">";
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
  if (! isset($usercomment)) { $usercomment = $uid; }
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
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
  }

  // update 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"incident_id\" value=\"$param_incident\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // insert
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $dis_button .="<input type=\"submit\" value=\"$l_insert\" />";
  }
  $display["title"] = "<div class=\"title\">$l_incident : $lincident</div>";

  // --- html template --------------------------------------------------------

  $block = "
    <form method=\"post\" name=\"f_entity\" onsubmit=\"if (check_incident(this)) return true; else return false;\" action=\"". url_prepare("incident_index.php")."\">
      <div class=\"detailHead\">$l_incident</div>
        <table class=\"detail\">
          <tr>
            <td class=\"detailLabel\">$l_label</td>
            <td class=\"detailForm\"><input name=\"tf_lincident\" size=\"30\" value=\"$lincident\" /></td>
          </tr><tr>
            <td class=\"detailLabel\">$l_reference</td>
            <td class=\"detailForm\"><input name=\"tf_reference\" size=\"10\" value=\"$reference\" /></td>
          </tr><tr>
            <td class=\"detailLabel\">$l_logger</td>
            <td class=\"detailForm\">$dis_sel_logger</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_owner</td>
            <td class=\"detailForm\">$dis_sel_owner</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_archive_new</td>
            <td class=\"detailForm\"><input name=\"cba_archive\" type=\"checkbox\"size=\"30\" value=\"1\" $archive /></td>
          </tr><tr>
            <td class=\"detailLabel\">$l_contract</td>
            <td class=\"detailForm\">
              <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;contract_id=$param_contract")."\">$c_label</a>
              <input type=\"hidden\" name=\"contract_id\" value=\"$param_contract\" />
              <input type=\"hidden\" name=\"contract_new_id\" value=\"$c_new_id\" />
              <a href=\"\" onclick=\"window.open('$path/contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_contract)."&amp;ext_widget=f_entity.contract_new_id&amp;ext_widget_text=f_entity.contract_new_name','contract','height=$popup_height,width=$popup_width'); return false;\">
              <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>
              <input type=\"text\" name=\"contract_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
            </td>
          </tr>
          $block_priority
          $block_status
          $block_category1
          $block_category2
          <tr>
            <td class=\"detailLabel\">$l_date</td>
            <td class=\"detailForm\">
            <script>calendar('tf_date','$date')</script>
                H $sel_hour
            </td>
          </tr><tr>
            <td class=\"detailLabel\">$l_duration</td>
            <td class=\"detailForm\"><input type=\"text\" name=\"tf_duration\" size=\"6\" maxlength=\"6\" value=\"$duration\" readonly=\"readonly\" />H</td>
          </tr>
        </table>
      <div class=\"detailHead\">$l_solution</div>
        <table class=\"detail\">
          <tr>
            <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_solution\" rows=\"6\" cols=\"60\">$solu</textarea></td>
          </tr>
        </table>
      <div class=\"detailHead\">$l_comment</div>
        <table class=\"detail\">
          <tr>
            <td class=\"detailLabel\">$l_add_comment</td>
            <td class=\"detailForm\">
              <script>calendar('tf_datecomment','$datecomment')</script>$sel_usercomment
            </td> 
          </tr><tr>
            <td class=\"detailLabel\">$l_res_duration</td>
            <td class=\"detailForm\">$sel_dur</td> 
          </tr><tr>
            <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
              $dis_comment
          </tr>
        </table>
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_button</p>
      </div>
    </form>
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
    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_delete</p>
    <p class=\"detailButtons\">$dis_back</p>
    </div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident administration index                                 //
///////////////////////////////////////////////////////////////////////////////
function dis_incident_admin_index() {
  global $cgp_hide;

  $block = "";

  $pri = of_category_get_ordered("incident", "priority");
  $block .= "<hr />";
  $block .= of_category_dis_admin_form("priority", $pri);

  $sta = of_category_get_ordered("incident", "status");
  $block .= "<hr />";
  $block .= of_category_dis_admin_form("status", $sta);

  if (! $cgp_hide["incident"]["category1"]) {
    $cats1 = of_category_get_ordered("incident", "category1");
    $block .= "<hr />";
    $block .= of_category_dis_admin_form("category1", $cats1);
  }

  if (! $cgp_hide["incident"]["category2"]) {
    $cats2 = of_category_get_ordered("incident", "category2");
    $block .= "<hr />";
    $block .= of_category_dis_admin_form("category2", $cats2);
  }

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
