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

  $usr_q = run_query_userobm();
  $type_q = run_query_cat1();
  $prio_q = run_query_priority();
  $stat_q = run_query_status();
  $block = html_incident_search_form($usr_q,$prio_q,$stat_q,$incident,$type_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident search Form
// Parameters:
//   - $usr_q      : DB result object (userobm list)
//   - $pri_q      : DB result object (incident priority list)
//   - $sta_q      : DB result object (incident status list)
//   - $cat1_q     : DB result object (incident category1 list)
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form ($usr_q, $pri_q, $sta_q, $incident, $cat1_q) {
  global $l_label,$l_company,$l_contract,$l_priority,$l_status,$l_find,$l_owner;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed,$c_all,$l_all;
  global $l_date_after,$l_date_before,$l_archive;

  $lincident = stripslashes($incident["lincident"]);
  $lcontract = stripslashes($incident["lcontract"]);
  $priority = $incident["priority"];
  $company = $incident["company"];
  $status = $incident["status"];
  $cat1 = $incident["cat1"];
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
    <form method=\"get\" name=\"f_search\" action=\"" .
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
// Display the Incident search result
// Parameters:
//   - $incident[] : incident search criteria
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_list($incident) {
  global $display;
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $prefs = get_display_pref($auth->auth["uid"], "incident");
  $obm_q = run_query_search($incident, $new_order, $order_dir);
  $nb_incident = $obm_q->num_rows_total();
  if ($nb_incident == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_incident_search_list($obm_q,$prefs,$nb_incident,$incident);
  }
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Search result
// Parameters : 
//   - $in_q        : list of incidents
//   - $prefs       : the fields which have to be displayed
//   - $nb_incident : nb incidents to return by the query search
//   - $company[]   : incidents search criteria
//     keys used    : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_list($in_q, $prefs, $nb_incident, $incident) {
  global $display;
  global $l_found, $l_no_found;

  $lincident = urlencode($incident["lincident"]);
  $lcontract = urlencode($incident["lcontract"]);
  $company = urlencode($incident["company"]);
  $priority = $incident["priority"];
  $status = $incident["status"];
  $cat1 = $incident["cat1"];
  $param_contract = $incident["contract_id"];
  $owner = $incident["owner"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  $url = url_prepare("incident_index.php?action=search&amp;tf_lincident=$lincident&amp;tf_company=$company&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority&amp;sel_status=$status&amp;sel_cat1=$cat1&amp;param_contract=$param_contract&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;sel_owner=$owner&amp;cb_archive=$archive");

  $in_q->seek($first_row);
  $dis_incidents_list=new OBM_DISPLAY("DATA", $prefs, "incident");
  $dis_incidents_list->data_set = $in_q;
  $dis_incidents_list->data_url = $url;
  $dis_incidents_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  $display["msg"] = display_info_msg("$nb_incident $l_found");
  $block = $dis_incidents_list->display("dis_data_incident");
  
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident consult
// Parameters:
//   - $incident[]    
///////////////////////////////////////////////////////////////////////////////
function dis_incident_consult($incident) {
  global $display, $l_query_error;

  $id = $incident["id"];

  if ($id > 0) {
    $inc_q = run_query_detail($id);
    if ($inc_q->num_rows() == 1) {
      $display["detailInfo"] = display_record_info($inc_q);
      $block = html_incident_consult($inc_q);
    } else {
      $display["msg"] .= display_err_msg($l_query_error . " - " . $inc_q->query . " !");
    }
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display HTML Incident Consult
// Parameters:
//   - $inc_q : DBO : incident result
///////////////////////////////////////////////////////////////////////////////
function html_incident_consult($inc_q) {
  global $display, $path, $l_yes, $l_no;
  global $set_theme,$ico_contract,$ico_company;
  global $l_incident, $l_contract, $l_company;
  global $l_label, $l_logger, $l_owner, $l_priority, $l_status, $l_archive;
  global $l_duration,$l_solution,$l_date;
  global $l_client_manager,$l_cat1;
  global $l_comment;

  $com = beautify_comment(nl2br($inc_q->f("incident_comment")));
  $label = $inc_q->f("incident_label");
  $priority = $inc_q->f("incidentpriority_label");
  $status = $inc_q->f("incidentstatus_label");
  $cat1 = $inc_q->f("incidentcategory1_label");
  $date = datetime_format($inc_q->f("date"), 1);
  $inc_id = $inc_q->f("incident_id");
  $inc_dur = nl2br($inc_q->f("incident_duration"));
  $inc_reso = nl2br(htmlentities($inc_q->f("incident_resolution")));
  $con_id = $inc_q->f("contract_id");
  $comp_id = $inc_q->f("contract_company_id");
  $comp_name = $inc_q->f("company_name");
  $con_status = $inc_q->f("contractstatus_label");
  $con_label = $inc_q->f("contract_label");
  $con_num = $inc_q->f("contract_number");
  $con_debut = date_format($inc_q->f("datebegin"));
  $con_exp = date_format($inc_q->f("dateexp"));
  $logger = $inc_q->f("lname1") . " " . $inc_q->f("fname1");
  $owner = $inc_q->f("lname2") . " " . $inc_q->f("fname2");
  $dis_con1 = $inc_q->f("lname_c1") . " " . $inc_q->f("fname_c1") . " - " . $inc_q->f("phone_c1");
  $dis_con2 = $inc_q->f("lname_c2") . " " . $inc_q->f("fname_c2") . " - " . $inc_q->f("phone_c2");
  $archive = ($inc_q->f("incident_archive") == 1 ? $l_yes : $l_no);

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
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
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
      <a href=\"". url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con_cli2")."\"></a> 
      $con_label
      <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$con_id") . "\">
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
    <td class=\"detailLabel\">$l_cat1</td>
    <td class=\"detailText\">$cat1</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_duration</td>
    <td class=\"detailText\">$inc_dur</td>
  </tr><tr>
    <td class=\"detailLabel\">$l_archive</td>
    <td class=\"detailText\">$archive</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_solution</div>
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
// Display Incident Form
// Parameters:
//   - $action    : action called
//   - $incident[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_incident_form($action,$incident) {
  global $display, $cg_com, $cg_prod;

  $id = $incident ["id"];
  if ($action == "new") {
    $inc_q = "";
    $users = "";
  }
  if ($action == "detailupdate") {
    if ($id > 0) {
      $inc_q = run_query_detail($id);
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
  $prio_q = run_query_priority();
  $stat_q = run_query_status();
  $cat1_q = run_query_cat1();

  $block = html_incident_form($action, $incident, $inc_q, $usr_q, $usrp_q, $prio_q, $stat_q, $cat1_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Incident Form
// Parameters:
//   - $action    : action called
//   - $incident[]: default values
//   - $inc_q     : incident database result 
//   - $usr_q     : userobm database result 
//   - $usrp_q    : userobm list from system group Production
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $incident, $inc_q, $usr_q, $usrp_q, $pri_q, $sta_q, $cat1_q) {
  global $display, $path, $uid, $set_theme, $ico_contract;
  global $c_php_isodate_format;
  global $l_label,$l_contract,$l_status,$l_cat1, $l_find;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
  global $l_logger,$l_owner,$l_priority,$l_incident;
  global $l_update, $l_checkdelete, $l_insert;
  global $l_duration,$l_solution,$l_date, $l_archive_new,$l_res_duration;
  global $popup_height, $popup_width, $l_select_contract,$l_comment,$l_upd_comment,$l_add_comment;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $param_contract = $inc_q->f("incident_contract_id");
    $lincident = $inc_q->f("incident_label");
    $owner = $inc_q->f("incident_owner");
    $logger = $inc_q->f("incident_logger");
    $duration = $inc_q->f("incident_duration");
    $comment = $inc_q->f("incident_comment");
    $solu = $inc_q->f("incident_resolution");
    $priority = $inc_q->f("incident_priority_id");
    $status = $inc_q->f("incident_status_id");
    $cat1 = $inc_q->f("incident_cat1_id");
    $datecomment = isodate_format();
    $date = isodate_format($inc_q->f("date"));
    $hour = get_hour($inc_q->f("date"));
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked=\"checked\"" : "");
    $param_incident = $inc_q->f("incident_id");
    $c_label = $inc_q->f("contract_label");
  } elseif ($action == "new") {
    // We pre-fill date with current date
    $date = date($c_php_isodate_format);
    $datecomment = isodate_format();
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
  if (isset($incident["date"])) { $date = $incident["date"]; }
  if (isset($incident["hour"])) { $hour = $incident["hour"]; }
  if (isset($incident["archive"])) { $archive = ($incident["archive"] == 1 ? "checked" : ""); }
  if (isset($incident["lcontract"])) { $c_label = stripslashes($incident["lcontract"]); }
  if (isset($incident["com"])) { $comment = stripslashes($incident["com"]); }
  if (isset($incident["add_comment"])) { $add_comment = stripslashes($incident["add_comment"]); }
  if (isset($incident["usercomment"])) { $usercomment = $incident["usercomment"]; }
  if (isset($incident["datecomment"])) { $datecomment = $incident["datecomment"]; }
  
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

  // Category1 select
  $sel_cat1 = "<select name=\"sel_cat1\">";
  while ($cat1_q->next_record()) {
    $id = $cat1_q->f("incidentcategory1_id");
    $label = $cat1_q->f("incidentcategory1_label");
    $sel_cat1 .= "\n<option value=\"$id\"";
    if ($id == $cat1) {
      $sel_cat1 .= " selected=\"selected \"";
    }
    $sel_cat1 .= ">$label</option>";
  }
  $sel_cat1 .= "</select>";

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
  $sel_dur = "<select name=\"sel_dur\">";
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
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
  }

  // update 
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"param_incident\" value=\"$param_incident\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // insert
  } elseif (($action == "new") || ($action == "insert")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $dis_button .="<input type=\"submit\" value=\"$l_insert\" />";
  }
  $display["title"] = "<div class=\"title\">$l_incident : $lincident</div>";

  // --- html template --------------------------------------------------------

  $block = "
    <form method=\"get\" name=\"form_incident\" onsubmit=\"if (check_incident(this)) return true; else return false;\" action=\"". url_prepare("incident_index.php")."\">
      <div class=\"detailHead\">$l_incident</div>
        <table class=\"detail\">
          <tr>
            <td class=\"detailLabel\">$l_label</td>
            <td class=\"detailForm\"><input name=\"tf_lincident\" size=\"30\" value=\"$lincident\" /></td>
          </tr><tr>
            <td class=\"detailLabel\">$l_logger</td>
            <td class=\"detailForm\">$dis_sel_logger</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_owner</td>
            <td class=\"detailForm\">$dis_sel_owner</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_archive_new</td>
            <td class=\"detailForm\"><input name=\"cb_archive\" type=\"checkbox\"size=\"30\" value=\"1\" $archive /></td>
          </tr><tr>
            <td class=\"detailLabel\">$l_contract</td>
            <td class=\"detailForm\">
              <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$param_contract")."\">$c_label</a>
              <input type=\"hidden\" name=\"param_contract\" value=\"$param_contract\" />
              <input type=\"hidden\" name=\"contract_new_id\" value=\"$c_new_id\" />
              <a href=\"\" onclick=\"window.open('$path/contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_select_contract)."&amp;ext_widget=form_incident.contract_new_id&amp;ext_widget_text=form_incident.contract_new_name','contract','height=$popup_height,width=$popup_width'); return false;\">
              <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>
              <input type=\"text\" name=\"contract_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
            </td>
          </tr><tr>
            <td class=\"detailLabel\">$l_priority</td>
            <td class=\"detailForm\">$sel_pri</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_status</td>
            <td class=\"detailForm\">$sel_sta</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_cat1</td>
            <td class=\"detailForm\"> $sel_cat1</td>
          </tr><tr>
            <td class=\"detailLabel\">$l_date</td>
            <td class=\"detailForm\">
              <input name=\"tf_date\" size=\"10\" maxlength=\"10\" value=\"$date\" />
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
            <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_solu\" rows=\"6\" cols=\"60\">$solu</textarea></td>
          </tr>
        </table>
      <div class=\"detailHead\">$l_comment</div>
        <table class=\"detail\">
          <tr>
            <td class=\"detailLabel\">$l_add_comment</td>
            <td class=\"detailForm\">
              <input name=\"tf_datecomment\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment\" />$sel_usercomment
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
// Display the Incident administration index                                 //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $pri_q = run_query_priority();
  $sta_q = run_query_status();
  $cat1_q = run_query_cat1();
  return html_priority_form($pri_q) . "<hr />" . html_status_form($sta_q)."<hr />". html_cat1_form($cat1_q);
}


///////////////////////////////////////////////////////////////////////////////
// Incident Priority section
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
              <input type=\"submit\" name=\"8888\" value=\"$l_pri_update\"
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
// Incident Category1 section
// Parameters:
//   - $cat1_q : Category1 list database object
///////////////////////////////////////////////////////////////////////////////
function html_cat1_form($cat1_q) {
  global $l_label, $l_order;
  global $l_cat1_manage,$l_cat1_exist,$l_cat1_new;
  global $l_cat1_checkdelete, $l_cat1_update, $l_cat1_insert;

  // Status select construction
  $sel_cat1 = "<select name=\"sel_cat1\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('-');
    forms.form_cat1_update.tf_cat1.value=mytext.substr(pos+1);
    forms.form_cat1_update.tf_order.value=mytext.substr(0,pos);\"
   size=\"4\">";
  while ($cat1_q->next_record()) {
    $id = $cat1_q->f("incidentcategory1_id");
    $order = $cat1_q->f("incidentcategory1_order");
    $label = $cat1_q->f("incidentcategory1_label");
    $sel_cat1 .= "<option value=\"$id\">$order-$label</option>\n";
  }
  $sel_sta .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $r = "
<!-- Category1 section -->

    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_cat1_manage</td>
    </tr>
    <tr>
      <td>

<!-- Category1 delete section -->

        <table>
        <tr>
         <td colspan=\"3\">
          <form name=\"form_cat1_delete\"
          method=\"post\" action=\"" . url_prepare("incident_index.php")."\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_cat1_exist</td>
        <td class=\"adminLabel\">$sel_cat1</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"cat1_checklink\" />
          <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_checkdelete\"
          onclick=\"if (check_cat1_checkdel(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
         </form>
        </td>
      </tr>

<!-- Category1 update section -->

      <tr>
        <td colspan=\"2\">
          <form name=\"form_cat1_update\" method=\"get\"
            action=\"" . url_prepare("incident_index.php") . "\">
          <input type=\"hidden\" name=\"sel_cat1\" value=\"\" />
          <input type=\"hidden\" name=\"action\" value=\"cat1_update\" />
        <table>
        <tr>
          <td class=\"adminLabel\">$l_label : </td>
          <td><input type=\"text\" name=\"tf_cat1\" /></td>
          <td rowspan=\"2\">
            <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_update\"
            onclick=\"if (check_cat1_upd(this.form, document.form_cat1_delete)) return true; else return false;\" />
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

<!-- new Category1 section -->

      <form name=\"form_cat1_new\"
        method=\"post\" action=\"" . url_prepare("incident_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_cat1_new : </td>
          <td><input type=\"text\" name=\"tf_cat1\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_order : </td>
          <td><input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
          <input type=\"hidden\" name=\"action\" value=\"cat1_insert\" />
          <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_insert\"
            onclick=\"if (check_cat1_new(this.form)) return true; " .
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
          <form name=\"form_pri_delete\" method=post action=\"" . url_prepare("incident_index.php") . "\">
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
// Displays the status links                                                //
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
// Displays the Category1 links
// Parameters:
//   - $id : cat1 id
///////////////////////////////////////////////////////////////////////////////
function dis_cat1_links($id) {
  global $l_back,$display, $l_cat1_link_incident,$l_cat1_link_incident_no;
  global $l_cat1_can_delete, $l_cat1_cant_delete, $l_cat1_delete;

  $label = get_cat1_label($id);
  $obm_q = run_query_cat1_links($id);
  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("incident_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  $nb_cat1 = $obm_q->num_rows();
  if ($nb_cat1 > 0) {
    $display["msg"] = display_warn_msg($l_cat1_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_cat1_link_incident ($nb_cat1)</td>
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
    if ($cpt < $nb_cat1) {
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
    $display["msg"] = display_ok_msg($l_cat1_can_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td colspan=\"2\" class=\"detailHead\">$label : $l_cat1_link_incident_no</td>
    </tr>
    </table>
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form name=\"form_kind_delete\".
          method=\"post\" action=\"" . url_prepare("incident_index.php") . "\">
          <input type=\"hidden\" name=\"action\" value=\"cat1_delete\" />
          <input type=\"hidden\" name=\"sel_cat1\" value=\"$id\" />
          <input type=\"submit\" name=\"sub_cat1\" value=\"$l_cat1_delete\" />
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
// Display the Incidnet Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_incident_display_pref($prefs) {
  global $l_incident_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
  $dis_pref->display_entity = "incident";
  $dis_pref->pref_title = $l_incident_display;
  $dis_pref->pref_dis_help = 1;
 
 // --- HTML Template --------------------------------------------------------

  return $dis_pref->display();
}
