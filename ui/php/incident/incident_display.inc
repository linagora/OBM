<script language="php">
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
$fieldnames["incident_priority"] = $l_priority;
$fieldnames["incident_date"] = $l_date;
$fieldnames["incident_duration"] = $l_duration;
// Calculate fields
$fieldnames["timeupdate"] = $l_lastupdate;
$fieldnames["incident_owner_lastname"] = $l_owner;
$fieldnames["incident_logger_lastname"] = $l_logger;
$fieldnames["incident_company_name"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// Display Incident specific dataset fields                                  //
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
    $res["url"] = "$path/incident/incident_index.php?action=detailconsult&amp;param_incident=".$OD->data_set->f("incident_id");
  }

  else if ($fieldname == "incident_owner_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("incident_owner");
  }

  else if ($fieldname == "incident_logger_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("incident_logger");
  }

  else if ($fieldname == "incident_date") {
    $res["name"] = date_format($OD->data_set->f("date"));
    $res["txt_name"] = $res["name"];
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
// Display Incident search Form                                              //
// Parameters:
//   - $usr_q      : DB result object (userobm list)
//   - $pri_q      : DB result object (incident priority list)
//   - $sta_q      : DB result object (incident status list)
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form ($usr_q, $pri_q, $sta_q, $incident) {
  global $l_label,$l_company,$l_contract,$l_priority,$l_status,$l_find,$l_owner;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed,$c_all,$l_all;
  global $l_date_after,$l_date_before,$l_archive;

  $lincident = stripslashes($incident["lincident"]);
  $lcontract = stripslashes($incident["lcontract"]);
  $priority = $incident["priority"];
  $company = $incident["company"];
  $status = $incident["status"];
  $owner = $incident["owner"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];
  $archive = ($incident["archive"] == "1" ? "checked=\"checked\"" : "");

  $dis_sel_owner="<select name=\"sel_owner\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $id = $usr_q->f("userobm_id");
    $dis_sel_owner.="\n<option value=\"$id\"";
    if ($id == $owner) 
      $dis_sel_owner.=" selected=\"selected \"";
    $dis_sel_owner.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $sel_pri = "<select name=\"sel_priority\">
    <option value=\"$c_all\">$l_all</option>";
  while ($pri_q->next_record()) {
    $id = $pri_q->f("incidentpriority_id");
    $label = $pri_q->f("incidentpriority_label");
    $sel_pri .= "\n<option value=\"$id\"";
    if ($id == $priority) 
      $sel_pri .= " selected=\"selected \"";
    $sel_pri .= ">$label</option>";
  }
  $sel_pri .= "</select>";

  $sel_sta = "<select name=\"sel_status\">
    <option value=\"$c_all\">$l_all</option>";
  while ($sta_q->next_record()) {
    $id = $sta_q->f("incidentstatus_id");
    $label = $sta_q->f("incidentstatus_label");
    $sel_sta .= "\n<option value=\"$id\"";
    if ($id == $status) 
      $sel_sta .= " selected=\"selected \"";
    $sel_sta .= ">$label</option>";
  }
  $sel_sta .= "</select>";


  // --- html template --------------------------------------------------------

  $block = "
    <form method=\"get\" name=\"f_incident\" action=\"" .
    url_prepare("incident_index.php") . " \" onsubmit=\"if (check_search_inc(this)) return true; else return false;\">

    <div class=\"search\">
     <div class=\"searchLabel\">$l_label<br />
       <input type=\"text\" name=\"tf_lincident\" size=\"16\" value=\"$lincident\" />
     </div>
     <div class=\"searchLabel\">$l_company<br />
      <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
     </div>	    
     <div class=\"searchLabel\">$l_contract<br />
      <input type=\"text\" name=\"tf_lcontract\" size=\"16\" value=\"$lcontract\" />
     </div>
     <div class=\"searchLabel\">$l_date_after<br />
      <input name=\"tf_dateafter\" type=\"text\" size=\"10\" value=\"$dateafter\" />
     </div>
     <div class=\"searchLabel\">$l_date_before<br />
      <input name=\"tf_datebefore\" type=\"text\" size=\"10\" value=\"$datebefore\" />
     </div>
     <div class=\"searchLabel\">$l_priority<br />
      $sel_pri
     </div>
     <div class=\"searchLabel\">$l_status<br />
      $sel_sta
     </div>
     <div class=\"searchLabel\">$l_owner<br />
      $dis_sel_owner
     </div>
     <div class=\"searchLabel\">$l_archive<br />
      <input name=\"cb_archive\" type=\"checkbox\" size=\"10\" value=\"1\" $archive />
     </div>
     <div class=\"searchLabel\">&nbsp;<br />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />     
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
     </div>
     <div class=\"searchEnd\"></div>
    </div>
   </form>
";
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident search result                                        //
// Parameters:
//   - $incident[] : incident search criteria
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_list($incident) {
  global $display;
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "incident");
  $obm_q = run_query_search($incident, $new_order, $order_dir);
  $nb_incident = $obm_q->num_rows();
  if ($nb_incident == 0) {
    $display["msg"] = display_info_msg($l_no_found);
  } else {
    $block = html_incident_search_list($obm_q,$pref_q,$nb_incident,$incident);
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result                                                //
// Parameters : 
//   - $in_q        : list of incidents
//   - $pref_q      : the fields which have to be displayed
//   - $nb_incident : nb incidents to return by the query search
//   - $company[]   : incidents search criteria
//     keys used    : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_list($in_q, $pref_q, $nb_incident, $incident) {
  global $display;
  global $l_found, $l_no_found;

  $lincident = urlencode($incident["lincident"]);
  $lcontract = urlencode($incident["lcontract"]);
  $company = urlencode($incident["company"]);
  $priority = $incident["priority"];
  $status = $incident["status"];
  $param_contract = $incident["contract_id"];
  $owner = $incident["owner"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  $url = url_prepare("incident_index.php?action=search&amp;tf_lincident=$lincident&amp;tf_company=$company&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority&amp;sel_status=$status&amp;param_contract=$param_contract&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;sel_owner=$owner&amp;cb_archive=$archive");

  $in_q->seek($first_row);

  $dis_incidents_list=new OBM_DISPLAY("DATA", $pref_q, "incident");
  $dis_incidents_list->data_set = $in_q;
  $dis_incidents_list->data_url = $url;
  $dis_incidents_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  $display["msg"] = display_info_msg("$nb_incident $l_found");
  $block = $dis_incidents_list->display("dis_data_incident");
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Consult                                                  //
// Parameters:
//   - $inc_q : DBO : incident result
//   - $con_q : DBO : contract result
///////////////////////////////////////////////////////////////////////////////
function html_incident_consult($inc_q,$con_q) {
  global $display;
  // -- Themes
  global $set_theme,$ico_contract,$ico_company;
  // - Labels
  global $l_incident, $l_label, $l_status, $l_company, $l_num;
  global $l_logger, $l_owner, $l_priority;
  global $l_duration, $l_description,$l_solution,$l_date, $l_to, $l_from;
  global $l_from,$l_manager,$l_client_manager,$l_archive_consult,$l_contract;
  global $path;

  $label = $inc_q->f("incident_label");
  $priority = $inc_q->f("incidentpriority_label");
  $status = $inc_q->f("incidentstatus_label");
  $date = datetime_format($inc_q->f("date"));
  $inc_id = $inc_q->f("incident_id");
  $inc_desc = nl2br($inc_q->f("incident_description"));
  $inc_dur = nl2br($inc_q->f("incident_duration"));
  $inc_reso = nl2br($inc_q->f("incident_resolution"));
  $con_id = $con_q->f("contract_id");
  $comp_id = $con_q->f("contract_company_id");
  $con_label = $con_q->f("contract_label");
  $con_num = $con_q->f("contract_numero");
  $con_debut = $con_q->f("contract_datebegin");
  $con_exp = $con_q->f("contract_dateexp");
  $tech = $con_q->f("contract_techmanager_id");
  $market = $con_q->f("contract_marketmanager_id");
  $comp_name = $con_q->f("company_name");
  $comp_ad1 = $con_q->f("company_address1");
  $comp_zip = $con_q->f("company_zipcode");
  $comp_town = $con_q->f("company_town");

  $logger = $inc_q->f("lname1") . " " . $inc_q->f("fname1");
  $owner = $inc_q->f("lname2") . " " . $inc_q->f("fname2");
  $dis_market = $con_q->f("lname1") . " " . $con_q->f("fname1");
  $dis_tech = $con_q->f("lname2") . " " . $con_q->f("fname2");
  
  $dis_con1 = $con_q->f("lname_c1") . " " . $con_q->f("fname_c1") . " - " . $con_q->f("phone_c1");
  $dis_con2 = $con_q->f("lname_c2") . " " . $con_q->f("fname_c2") . " - " . $con_q->f("phone_c2");

  if ($inc_q->f("incident_archive") == 1) {
    $dis_lab_archive ="<tr><td class=\"detailLabel\" colspan=\"2\">$l_archive_consult</td></tr>";
  }
  $display["title"] = "<div class=\"title\">$l_incident : $label</div>";
  // --- HTML Template --------------------------------------------------------

  $block = "
  <div class=\"detailHead\">$l_company</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_company
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
      <img src=\"/images/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
    </td>
    <td class=\"detailText\">$comp_name
      <br />$comp_ad1
      <br />$comp_zip $comp_town
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 1</td>
    <td class=\"detailText\">$dis_con1</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_client_manager 2</td>
    <td class=\"detailText\">$dis_con2</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_contract</div>
  <table class=\"detail\"> 
  <tr>
    <td class=\"detailLabel\">
      <a href=\"". url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con_cli2")."\"></a> 
      $con_label
      <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$con_id") . "\">
      <img src=\"/images/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>&nbsp;&nbsp;
    </td>
    <td class=\"detailText\">
      $l_num : $con_num <br />
      $l_from $con_debut $l_to $con_exp
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_manager 1</td>
    <td class=\"detailText\">$dis_market</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_manager 2</td>
    <td class=\"detailText\">$dis_tech</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_incident</div>
  <table class=\"detail\"> 
  <tr>
    <td class=\"detailLabel\">$l_label</td>
    <td class=\"detailText\">$label</td>
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
  </tr><tr>
    <td class=\"detailLabel\">$l_duration</td>
    <td class=\"detailText\">$inc_dur</td>
  </tr>
  $dis_lab_archive
  </table>
 
  <div class=\"detailHead\">$l_description</div>
  <table class=\"detail\"> 
  <tr>
    <td colspan=\"2\" class=\"detailText\">$inc_desc</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_solution</div>
  <table class=\"detail\"> 
  <tr>
    <td colspan=\"2\" class=\"detailText\">$inc_reso</td>
  </tr>
  </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Form                                                     //
// Parameters:
//   - $action    : action called
//   - $inc_q     : incident database result 
//   - $contr_q   : contract database result 
//   - $usr_q     : userobm database result 
//   - $usrp_q    : userobm list from system group Production
//   - $incident[]: default values
//     keys used  : kind, lname, fname, ad1, ad2, zip, town, cdx, ctry, func
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $inc_q, $contr_q, $usr_q, $usrp_q, $pri_q, $sta_q, $incident) {
  global $display;
  global $path, $uid, $set_theme, $ico_contract, $c_php_isodate_format;
  global $l_label,$l_contract,$l_status, $l_find;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
  global $l_logger,$l_owner,$l_priority,$l_incident;
  global $l_num, $l_update, $l_checkdelete, $l_insert;
  global $l_duration, $l_description,$l_solution,$l_date, $l_archive_new;
  global $popup_height, $popup_width, $l_select_contract;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $param_contract = $inc_q->f("incident_contract_id");
    $lincident = $inc_q->f("incident_label");
    $owner = $inc_q->f("incident_owner");
    $logger = $inc_q->f("incident_logger");
    $desc = $inc_q->f("incident_description");
    $duration = $inc_q->f("incident_duration");
    $solu = $inc_q->f("incident_resolution");
    $priority = $inc_q->f("incident_priority_id");
    $status = $inc_q->f("incident_status_id");
    $date = isodate_format($inc_q->f("date"));
    $hour = get_hour($inc_q->f("date"));
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked=\"checked\"" : "");
    $param_incident = $inc_q->f("incident_id");
    $c_name = $contr_q->f("contract_label");
  } elseif ($action == "new") {
      // We pre-fill date with current date
    $date = date($c_php_isodate_format);
  }

  // If parameters have been given, they supercede the default action value
  if (isset($incident["contract_id"])) { $param_contract = $incident["contract_id"]; }
  if (isset($incident["lincident"])) { $lincident = stripslashes($incident["lincident"]); }
  if (isset($incident["owner"])) { $owner = $incident["owner"]; }
  if (isset($incident["logger"])) { $logger = $incident["logger"]; }
  if (isset($incident["description"])) { $desc = stripslashes($incident["description"]); }
  if (isset($incident["duration"])) { $duration = $incident["duration"]; }
  if (isset($incident["solution"])) { $solu = stripslashes($incident["solution"]); }
  if (isset($incident["priority"])) { $priority = $incident["priority"]; }
  if (isset($incident["status"])) { $status = $incident["status"]; }
  if (isset($incident["date"])) { $date = $incident["date"]; }
  if (isset($incident["hour"])) { $hour = $incident["hour"]; }
  if (isset($incident["archive"])) { $archive = ($incident["archive"] == 1 ? "checked" : ""); }
  if (isset($incident["lcontract"])) { $c_name = stripslashes($incident["lcontract"]); }


  // Owner select
  if (! isset($owner)) { $owner = $uid; }
  $dis_sel_owner="<select name=\"sel_owner\">";
  while ($usrp_q->next_record()) {
    $dis_sel_owner.="\n<option value=\"".$usrp_q->f("userobm_id")."\"";
    if ($usrp_q->f("userobm_id") == $owner) 
      $dis_sel_owner.=" selected=\"selected\"";
    $dis_sel_owner.=">". $usrp_q->f("userobm_lastname") . " " . $usrp_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

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

  // Priority select
  $sel_pri = "<select name=\"sel_priority\">";
  while ($pri_q->next_record()) {
    $id = $pri_q->f("incidentpriority_id");
    $label = $pri_q->f("incidentpriority_label");
    $sel_pri .= "\n<option value=\"$id\"";
    if ($id == $priority) 
      $sel_pri .= " selected=\"selected \"";
    $sel_pri .= ">$label</option>";
  }
  $sel_pri .= "</select>";

  // Status select
  $sel_sta = "<select name=\"sel_status\">";
  while ($sta_q->next_record()) {
    $id = $sta_q->f("incidentstatus_id");
    $label = $sta_q->f("incidentstatus_label");
    $sel_sta .= "\n<option value=\"$id\"";
    if ($id == $status) 
      $sel_sta .= " selected=\"selected \"";
    $sel_sta .= ">$label</option>";
  }
  $sel_sta .= "</select>";

  // Hour select
  $sel_hour = "<select name=\"sel_hour\">";
  for ($cpt = 0; $cpt < 24; $cpt++) {
    $sel_hour .= "\n<option value=\"$cpt\"";
    if ($cpt == $hour) 
      $sel_hour .= " selected=\"selected\"";
    $sel_hour .= ">$cpt</option>";
  }
  $sel_hour .= "</select>";

  // Duration select
  $sel_dur = "<select name=\"sel_dur\">";
  for ($cpt = 0.25; $cpt < 2; $cpt += 0.25) {
    $sel_dur .= "\n<option value=\"$cpt\"";
    if ($cpt == $duration) 
      $sel_dur .= " selected=\"selected\"";
    $sel_dur .= ">$cpt</option>";
  }
  for ($cpt = 2; $cpt < 17; $cpt += 1) {
    $sel_dur .= "\n<option value=\"$cpt\"";
    if ($cpt == $duration)
      $sel_dur .= " selected=\"selected\"";
    $sel_dur .= ">$cpt</option>";
  }
  $sel_dur .= "</select>";


  // update 
  if (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"param_incident\" value=\"$param_incident\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // insert
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $dis_button .="<input type=\"submit\" value=\"$l_insert\" />";
  }
  $display["title"] = "<div class=\"title\">$l_incident : $lincident</div>";

  // --- html template --------------------------------------------------------

  $block = "
<form method=\"get\" name=\"form_mod_incident\" onsubmit=\"if (check_incident(this)) return true; else return false;\" action=\"". url_prepare("incident_index.php")."\">
<div class=\"detailHead\">$l_incident</div>
<table class=\"detail\">
 <tr>
  <td class=\"detailLabel\">$l_label</td>
  <td><input name=\"tf_lincident\" size=\"30\" value=\"$lincident\" /></td>
 </tr><tr>
  <td class=\"detailLabel\">$l_logger</td>
  <td>$dis_sel_logger</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_owner</td>
  <td>$dis_sel_owner</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_archive_new</td>
  <td><input name=\"cb_archive\" type=\"checkbox\"size=\"30\" value=\"1\" $archive /></td>
 </tr><tr>
  <td class=\"detailLabel\">$l_contract</td>
  <td>
   <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$param_contract")."\">$c_name</a>
   <input type=\"hidden\" name=\"param_contract\" value=\"$param_contract\" />
   <input type=\"hidden\" name=\"contract_new_id\" value=\"$c_new_id\" />
   <a href=\"\" onclick=\"window.open('$path/contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_contract)."','contract','height=$popup_height,width=$popup_width'); return false;\">
   <img src=\"/images/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>
   <input type=\"text\" name=\"contract_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
  </td>
 </tr><tr>
  <td class=\"detailLabel\">$l_priority</td>
  <td>$sel_pri</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_status</td>
  <td>$sel_sta</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_date</td>
  <td>
    <input name=\"tf_date\" size=\"10\" maxlength=\"10\" value=\"$date\" />
    H $sel_hour
  </td>
 </tr><tr>
  <td class=\"detailLabel\">$l_duration</td>
  <td>$sel_dur H</td>
 </tr>
 </table>
 <div class=\"detailHead\">$l_description</div>
<table class=\"detail\">
<tr>
  <td colspan=\"2\"><textarea name=\"ta_desc\" rows=\"6\" cols=\"60\">$desc</textarea></td>
 </tr>
 </table>
  <div class=\"detailHead\">$l_solution</div>
 <table class=\"detail\">
<tr>
  <td colspan=\"2\"><textarea name=\"ta_solu\" rows=\"6\" cols=\"60\">$solu</textarea></td>
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
// Display the Incident administration index                                 //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $pri_q = run_query_priority();
  $sta_q = run_query_status();
  return html_priority_form($pri_q) . "<hr />" . html_status_form($sta_q);
}


///////////////////////////////////////////////////////////////////////////////
// Incident Priority section                                                 //
// Parameters:
//   - $pri_q : Priority list database object
///////////////////////////////////////////////////////////////////////////////
function html_priority_form($pri_q) {
  global $l_pri_manage,$l_pri_exist, $l_label, $l_order, $l_color;
  global $l_pri_checkdelete, $l_pri_update, $l_pri_new, $l_pri_insert;

  // Priority select construction
  $sel_pri = "<select name=\"sel_priority\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('(');
    col = mytext.substr(pos+1);
    pos2 = col.indexOf(')');
    col = col.substr(0,pos2);
    mytext = mytext.substr(0, pos);
    pos = mytext.indexOf('-');
    forms.form_pri_update.tf_pri.value=mytext.substr(pos+1);
    forms.form_pri_update.tf_order.value=mytext.substr(0,pos);
    forms.form_pri_update.tf_color.value=col;\"
    size=\"4\" >";
  while ($pri_q->next_record()) {
    $id = $pri_q->f("incidentpriority_id");
    $order = $pri_q->f("incidentpriority_order");
    $color = $pri_q->f("incidentpriority_color");
    $label = $pri_q->f("incidentpriority_label");
    $sel_pri .= "\n<option value=\"$id\">$order-$label ($color)</option>";
  }
  $sel_pri .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $r = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">
        $l_pri_manage
      </td>
    </tr><tr>
      <td>

<!-- Priority Check Delete Section -->

        <table>
        <tr>
          <td colspan=\"3\">
            <form name=\"form_pri_delete\" method=\"post\"
              action=\"" . url_prepare("incident_index.php") . "\">
            <table>
            <tr>
              <td class=\"adminLabel\">$l_pri_exist</td>
              <td class=\"adminLabel\">$sel_pri</td>
              <td>
              <input type=\"hidden\" name=\"action\" value=\"priority_checklink\" />
              <input type=\"submit\" name=\"sub_pri\" value=\"$l_pri_checkdelete\" onclick=\"if (check_priority_checkdel(this.form)) return true; else return false;\" />
              </td>
            </tr>
            </table>
            </form>
          </td>
        </tr>

<!-- Priority Update Section -->

        <tr>
          <td colspan=\"2\">
            <form name=\"form_pri_update\" method=\"get\"
              action=\"" . url_prepare("incident_index.php") . "\">
            <input type=\"hidden\" name=\"sel_priority\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"priority_update\" />
          <table>
          <tr>
            <td class=\"adminLabel\">$l_label : </td>
            <td><input type=\"text\" name=\"tf_pri\" /></td>
            <td rowspan=\"3\">
              <input type=\"submit\" name=\"sub_pri\" value=\"$l_pri_update\"
              onclick=\"if (check_priority_upd(this.form, document.form_pri_delete)) return true; else return false;\" />
            </td>
          </tr>
          <tr>
            <td class=\"adminLabel\">$l_order : </td>
            <td>
	      <input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" />
	    </td>
          </tr>
          <tr>
            <td class=\"adminLabel\">$l_color : </td>
            <td>
	      <input type=\"text\" name=\"tf_color\" size=\"6\" maxlength=\"6\" />
	    </td>
          </tr>
          </table>
            </form>
          </td>
          <td>&nbsp;</td>
        </tr>
        </table>
      </td>
      <td>

<!-- New Priority Section -->

        <form name=\"form_pri_new\" method=\"get\"
          action=\"" . url_prepare("incident_index.php") . "\">
        <table>
        <tr>
          <td class=\"adminLabel\">$l_pri_new : </td>
          <td><input name=\"tf_pri\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_order : </td>
          <td><input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" /></td>
        </tr><tr>
          <td class=\"adminLabel\">$l_color : </td>
          <td><input type=\"text\" name=\"tf_color\" size=\"6\" maxlength=\"6\" /></td>
        </tr><tr>
          <td colspan=\"2\" class=\"adminLabel\">
            <input type=\"hidden\" name=\"action\" value=\"priority_insert\" />
            <input type=\"submit\" name=\"sub_pri\" value=\"$l_pri_insert\"
              onclick=\"if (check_priority_new(this.form)) return true; else return false;\" />
          </td>

        </tr>
        </table>
        </form>
      </td>
    </tr>
    </table>
";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Incident Status section                                                   //
// Parameters:
//   - $sta_q : Status list database object
///////////////////////////////////////////////////////////////////////////////
function html_status_form($sta_q) {
  global $l_label, $l_order;
  global $l_sta_manage,$l_sta_exist,$l_sta_new;
  global $l_sta_checkdelete, $l_sta_update, $l_sta_insert;

  // Status select construction
  $sel_sta = "<select name=\"sel_status\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('-');
    forms.form_status_update.tf_status.value=mytext.substr(pos+1);
    forms.form_status_update.tf_order.value=mytext.substr(0,pos);\"
   size=\"4\">";
  while ($sta_q->next_record()) {
    $id = $sta_q->f("incidentstatus_id");
    $order = $sta_q->f("incidentstatus_order");
    $label = $sta_q->f("incidentstatus_label");
    $sel_sta .= "<option value=\"$id\">$order-$label</option>\n";
  }
  $sel_sta .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $r = "
<!-- Status section -->

    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_sta_manage</td>
    </tr>
    <tr>
      <td>

<!-- status delete section -->

        <table>
        <tr>
         <td colspan=\"3\">
          <form name=\"form_status_delete\"
          method=\"post\" action=\"" . url_prepare("incident_index.php")."\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_sta_exist</td>
        <td class=\"adminLabel\">$sel_sta</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"status_checklink\" />
          <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_checkdelete\"
          onclick=\"if (check_status_checkdel(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
         </form>
        </td>
      </tr>

<!-- status update section -->

      <tr>
        <td colspan=\"2\">
          <form name=\"form_status_update\" method=\"get\"
            action=\"" . url_prepare("incident_index.php") . "\">
          <input type=\"hidden\" name=\"sel_status\" value=\"\" />
          <input type=\"hidden\" name=\"action\" value=\"status_update\" />
        <table>
        <tr>
          <td class=\"adminLabel\">$l_label : </td>
          <td><input type=\"text\" name=\"tf_status\" /></td>
          <td rowspan=\"2\">
            <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_update\"
            onclick=\"if (check_status_upd(this.form, document.form_status_delete)) return true; else return false;\" />
          </td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_order : </td>
          <td>
	    <input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" />
	  </td>
        </tr>
        </table>
        </form>
        </td>
        <td>&nbsp;</td>
      </tr>
      </table>
      </td>
      <td>

<!-- new status section -->

      <form name=\"form_status_new\"
        method=\"post\" action=\"" . url_prepare("incident_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_sta_new : </td>
          <td><input type=\"text\" name=\"tf_status\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_order : </td>
          <td><input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
          <input type=\"hidden\" name=\"action\" value=\"status_insert\" />
          <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_insert\"
            onclick=\"if (check_status_new(this.form)) return true; " .
            "else return false;\" />
        </td>
      </tr>
     </table>
    </form>
    </td>
    </tr>
    </table>
";

  return $r;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the priority links                                               //
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function dis_priority_links($id) {
  global $l_pri_link_incident, $l_pri_link_incident_no;
  global $l_pri_delete, $l_pri_can_delete, $l_pri_cant_delete;
  global $l_back,$display;

  $label = get_priority_label($id);
  $obm_q = run_query_priority_links($id);

  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";
	
  $nb_pri = $obm_q->num_rows();
  if ($nb_pri > 0) {
    $display["msg"] = display_warn_msg($l_pri_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\"> $label : $l_pri_link_incident ($nb_pri)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("incident_id");
      $cname = $obm_q->f("incident_label");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("incident_index.php?action=detailconsult&amp;param_incident=$cid") . "\">$cname</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_pri) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .="
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  } else {
    $display["msg"] = display_ok_msg($l_pri_can_delete);
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">
      $l_pri_link_incident_no $label</td>
    </tr><tr>
      <td>
      </td>
    </table>
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form name=\"form_pri_delete\"
          method=post action=\"" . url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"priority_delete\">
        <input type=\"hidden\" name=\"sel_priority\" value=\"$id\">
        <input type=\"submit\" name=\"sub_pri\" value=\"$l_pri_delete\">
        </form>
        $dis_back
     </p>
    </div>";

  }
  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the stabtus links                                                //
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function dis_status_links($id) {
  global $l_sta_link_incident,$l_sta_link_incident_no, $l_sta_can_delete, $l_sta_cant_delete;
  global $l_sta_delete, $l_back,$display;

  $label = get_status_label($id);
  $obm_q = run_query_status_links($id);
  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  $nb_stat = $obm_q->num_rows();
  if ($nb_stat > 0) {
    $display["msg"] = display_warn_msg($l_sta_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_sta_link_incident ($nb_stat)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $iid = $obm_q->f("incident_id");
      $ilabel = $obm_q->f("incident_label");
      $dis_link.= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("incident_index.php?action=detailconsult&amp;param_incident=$iid") . "\">$ilabel</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_stat) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .="
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";

  } else {
    $display["msg"] = display_ok_msg($l_sta_can_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">$label : $l_sta_link_incident_no</td>
    </tr>
    </table>
    <div class=\"detailButton\">
        <p class=\"detailButtons\">
        <form name=\"form_kind_delete\".
          method=post action=\"" . url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
        <input type=\"hidden\" name=\"sel_status\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_delete\" />
        </form>
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	
	</p>
    </div>";

  }
  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incidnet Display preference screen                            //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_incident_display_pref($display_pref_q) {
  global $l_incident_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "incident";
  $dis_pref->pref_title = $l_incident_display;
  $dis_pref->pref_dis_help = 1;
 
 // --- HTML Template --------------------------------------------------------

  return $dis_pref->display();
}


