<SCRIPT language="php">
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
$fieldnames["incident_state"] = $l_incident_status;
$fieldnames["incident_label"] = $l_incident_label;
$fieldnames["incident_priority"] = $l_incident_priority;
$fieldnames["incident_date"] = $l_incident_date;
// Calculate fields
$fieldnames["timeupdate"] = $l_incident_lastupdate;
$fieldnames["incident_owner_lastname"] = $l_incident_owner;
$fieldnames["incident_logger_lastname"] = $l_incident_logger;
$fieldnames["incident_company_name"] = $l_incident_company;


///////////////////////////////////////////////////////////////////////////////
// Display Company search Form                                               //
// Parameters:
//   - $usr_q      : DB result object (userobm list)
//   - $incident[] : default form values
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function html_incident_search_form ($usr_q, $incident) {
  global $l_incident_label,$l_contract_label,$l_incident_priority,$l_incident_status, $l_find,$l_incident_owner;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed,$c_all,$l_all;
  global $l_date_after,$l_date_before,$l_archive;

  $lincident = stripslashes($incident["lincident"]);
  $lcontract = stripslashes($incident["lcontract"]);
  $priority = $incident["priority"];
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

  $dis_sel_prio="<select name=\"sel_priority\">
    <option value=\"$c_all\">$l_all</option>
    <option value=\"redhot\"";
  if ($priority == "redhot") 
    $dis_sel_prio.= " selected=\"selected\"";
  $dis_sel_prio.=">$l_redhot</option>
    <option value=\"hot\"";
  if ($priority == "hot") 
    $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.=">$l_hot</option>
    <option value=\"normal\"";
  if ($priority == "normal") 
    $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.= ">$l_normal</option>
    <option value=\"low\"";
  if ($priority == "low") 
    $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.=">$l_low</option>
  </select>";

  $dis_sel_stat="<select name=\"sel_status\">\n<option value=\"$c_all\">$l_all</option>\n<option value=\"open\"";
  if ($status == "open") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.= ">$l_open</option>\n<option value=\"call\"";
  if ($status == "call") 
   $dis_sel_stat.=" selected";
  $dis_sel_stat.= ">$l_call</option>\n<option value=\"waitcall\"";
  if ($status == "waitcall") 
   $dis_sel_stat.= " selected=\"selected\"";
  $dis_sel_stat.=">$l_waitcall</option>\n<option value=\"paused\"";
  if ($status == "paused") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.=">$l_paused</option>\n<option value=\"closed\"";
  if ($status == "closed") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.=">$l_closed</option>\n</select>";
 
  // --- html template --------------------------------------------------------

  echo "
     <form method=\"get\" name=\"f_incident\" action=\"" .
     url_prepare("incident_index.php") . " \" onsubmit=\"if (verifier_inc(this)) return true; else return false;\">
     <table class=\"details\">
      <tr><td>
       <table>
        <tr>
         <td class=\"textLabelForm\">$l_incident_label</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_contract_label</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_archive</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_date_after</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_date_before</td>
        </tr><tr>
         <td><input type=\"text\" name=\"tf_lincident\" size=\"20\" value=\"$lincident\" /></td>
         <td>&nbsp;&nbsp;</td>
         <td><input type=\"text\" name=\"tf_lcontract\" size=\"20\" value=\"$lcontract\" /></td>
         <td>&nbsp;&nbsp;</td>
         <td><input name=\"cb_archive\" type=\"checkbox\" size=\"10\" value=\"1\" $archive /></td>
         <td>&nbsp;&nbsp;</td>
   	 <td><input name=\"tf_dateafter\" type=\"text\" size=\"10\" value=\"$dateafter\" /></td>
         <td>&nbsp;&nbsp;</td>
   	 <td><input name=\"tf_datebefore\" type=\"text\" size=\"10\" value=\"$datebefore\" /></td>
        </tr>
       </table>
      </td>
     </tr><tr>
      <td>
       <table class=\"details\">     	
        <tr>
         <td class=\"textLabelForm\">$l_incident_priority</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_incident_status</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$l_incident_owner</td>
        </tr><tr>
         <td class=\"textLabelForm\">$dis_sel_prio</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$dis_sel_stat</td>
         <td>&nbsp;&nbsp;</td>
         <td class=\"textLabelForm\">$dis_sel_owner</td>
         <td>&nbsp;&nbsp;</td>
         <td>
         <input name=\"action\" type=\"hidden\" value=\"search\" />
         <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
         </td>
        </tr>
       </table>
      </td>
     </tr>
    </table>
    </form>
    <hr />
";
}


///////////////////////////////////////////////////////////////////////////////
// Display the Incident search result                                        //
// Parameters:
//   - $incident[] : incident search criteria
//     keys used  : status, label incident, priority, label contract
///////////////////////////////////////////////////////////////////////////////
function dis_incident_search_list($incident) {
  global $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "incident");
  $obm_q = run_query_search($incident, $new_order, $order_dir);
  $nb_incident = $obm_q->num_rows();
  if ($nb_incident == 0) {
    display_info_msg($l_no_found);
  } else {
    html_incident_search_list($obm_q,$pref_q,$nb_incident,$incident);
  }
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
  global $set_theme, $l_found, $l_no_found;

  $lincident = $incident["lincident"];
  $lcontract = $incident["lcontract"];
  $priority = $incident["priority"];
  $status = $incident["status"];
  $param_contract = $incident["contract_id"];
  $owner = $incident["owner"];
  $archive = $incident["archive"];
  $dateafter = $incident["date_after"];
  $datebefore = $incident["date_before"];

  $url = url_prepare("incident_index.php?action=search&amp;tf_lincident=$lincident&amp;tf_lcontract=$lcontract&amp;sel_priority=$priority&amp;sel_status=$status&amp;param_contract=$param_contract&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;sel_owner=$owner&amp;cb_archive=$archive");

  $in_q->seek($first_row);

  $dis_incidents_list=new OBM_DISPLAY("DATA", $pref_q);
  $dis_incidents_list->data_set = $in_q;
  $dis_incidents_list->data_url = $url;
  $dis_incidents_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  display_info_msg("$nb_incident $l_found");
  $dis_incidents_list->display();
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Form                                                     //
// Parameters:
//   - $action    : action called
//   - $inc_q     : incident database result 
//   - $contr_q   : contract database result 
//   - $usr_q     : userobm database result 
//   - $incident[]: default values
//     keys used  : kind, lname, fname, ad1, ad2, zip, town, cdx, ctry, func
///////////////////////////////////////////////////////////////////////////////
function html_incident_form($action, $inc_q, $contr_q, $usr_q, $incident) {
  global $l_incident_label,$l_contract_label,$l_incident_prority,$l_incident_status, $l_find;
  global $l_redhot,$l_hot,$l_normal,$l_low,$l_open,$l_call,$l_waitcall,$l_paused,$l_closed;
  global $l_incident_logger,$l_incident_owner,$l_incident_priority,$l_incident_status,$l_incident;
  global $l_contract_label, $l_contract_num, $l_update, $l_checkdelete, $l_insert, $l_contract_date_init;
  global $l_contract_date_expir,$l_incident_description,$l_incident_solution,$l_incident_date, $l_archive_new;
  global $path, $auth,$popup_height,$popup_width, $set_theme, $ico_contract, $l_incident_select_contract,$l_contract_label ;

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $param_contract = $inc_q->f("incident_contract_id");
    $lincident = $inc_q->f("incident_label");
    $owner =  $inc_q->f("incident_owner");
    $logger =  $inc_q->f("incident_logger");
    $desc =  $inc_q->f("incident_description");
    $solu =  $inc_q->f("incident_resolution");
    $priority =  $inc_q->f("incident_priority");
    $status =  $inc_q->f("incident_state");
    $date =  date_format($inc_q->f("date"));
    $archive = ($inc_q->f("incident_archive") == "1" ? "checked=\"checked\"" : "");
    $param_incident = $inc_q->f("incident_id");
    $c_name = $contr_q->f("contract_label");
    $c_id = $param_contract;
  } else {
    $param_contract = $incident["contract_id"];
    $lincident = $incident["lincident"];
    $owner =  $incident["owner"];
    $logger =  $incident["logger"];
    $desc =  $incident["description"];
    $solu =  $incident["solution"];
    $priority =  $incident["priority"];
    $status =  $incident["status"];
    $date =  $incident["date"];
    $archive = ($incident["archive"] == "1" ? "checked" : "");
  }

  $dis_sel_owner="<select name=\"sel_owner\">";
  while($usr_q->next_record()) {
    $dis_sel_owner.="\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $owner) 
      $dis_sel_owner.=" selected=\"selected\"";
    $dis_sel_owner.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_owner.="</select>";

  $usr_q->seek();

  $dis_sel_logger="<select name=\"sel_logger\">";
  while($usr_q->next_record()) {
    $dis_sel_logger.="\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $logger) 
      $dis_sel_logger.=" selected=\"selected\"";
    $dis_sel_logger.=">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $dis_sel_logger.="</select>";

  $dis_sel_prio="<select name=\"sel_priority\" >\n<option value=\"redhot\"";
  if ($priority == "redhot") 
   $dis_sel_prio.= " selected=\"selected\"";
  $dis_sel_prio.=">$l_redhot</option>\n<option value=\"hot\"";
  if ($priority == "hot") 
   $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.=">$l_hot</option>\n<option value=\"normal\"";
  if ($priority == "normal") 
   $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.= ">$l_normal</option>\n<option value=\"low\"";
  if ($priority == "low") 
   $dis_sel_prio.=" selected=\"selected\"";
  $dis_sel_prio.=">$l_low</option>\n</select>";

  $dis_sel_stat="<select name=\"sel_status\" onchange=\"auto_archive(this.form)\">\n<option value=\"open\"";
  if ($status == "open") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.= ">$l_open</option>\n<option value=\"call\"";
  if ($status == "call") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.= ">$l_call</option>\n<option value=\"waitcall\"";
  if ($status == "waitcall") 
   $dis_sel_stat.= " selected=\"selected\"";
  $dis_sel_stat.=">$l_waitcall</option>\n<option value=\"paused\"";
  if ($status == "paused") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.=">$l_paused</option>\n<option value=\"closed\"";
  if ($status == "closed") 
   $dis_sel_stat.=" selected=\"selected\"";
  $dis_sel_stat.=">$l_closed</option>\n</select>";

  // update 
  if (($action=="detailupdate") || ($action=="update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"update\" />\n";
    $dis_button .="<input type=\"hidden\" name=\"param_incident\" value=\"$param_incident\" />\n";
    $dis_button .="<input type=\"submit\" value=\"$l_update\" />\n";

  // check delete
    $dis_delete .= "<form method=\"get\" name=\"f_del_con\"
        onsubmit=\"if (confirm_del()) return true; else return false;\"
        action=\"" . url_prepare("incident_index.php") . "\">
      <table class=\"details\">
      <tr><td>
        <input type=\"hidden\" name=\"action\" value=\"delete\" />
        <input type=\"hidden\" name=\"param_incident\" value=\"$param_incident\" />
        <input type=\"submit\" value=\"$l_checkdelete\" />
        </td>
      </tr>
      </table></form>";

  // insert
  } elseif (($action=="new") || ($action=="insert")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"insert\" />\n";
    $dis_button .="<input type=submit value=\"$l_insert\" />\n";
  }

  // --- html template --------------------------------------------------------

  echo "
<form method=\"get\" name=\"form_mod_incident\" onsubmit=\"if (check_incident(this)) return true; else return false;\" action=\"". url_prepare("incident_index.php")."\">
<table class=\"details\">
 <tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_label</td>
  <td><input name=\"tf_lincident\" size=\"30\" value=\"$lincident\" /></td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_logger</td>
  <td>$dis_sel_logger</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_owner</td>
  <td>$dis_sel_owner</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_archive_new</td>
  <td><input name=\"cb_archive\" type=\"checkbox\"size=\"30\" value=\"1\" $archive /></td>
 </tr><tr>
  <td class=\"detailLabel\">$l_contract_label</td>
  <td>
   <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$c_id")."\">$c_name</a>
   <input type=\"hidden\" name=\"param_contract\" value=\"$param_contract\" />
   <input type=\"hidden\" name=\"contract_new_id\" value=\"$c_new_id\" />
   <a href=\"\" onclick=\"window.open('$path/contract/contract_index.php?action=ext_get_id&amp;popup=1&amp;title=".urlencode($l_incident_select_contract)."','contract','height=$popup_height,width=$popup_width'); return false;\">
   <img src=\"/images/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>
   <input type=\"text\" name=\"contract_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
  </td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_priority</td>
  <td>$dis_sel_prio</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_status</td>
  <td>$dis_sel_stat</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_date</td>
  <td><input name=\"tf_date\" size=\"12\" value=\"$date\" /></td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident_description</td>
 </tr><tr>
  <td colspan=\"2\"><textarea name=\"ta_desc\" rows=\"6\" cols=\"60\">$desc</textarea></td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident_solution</td>
 </tr><tr>
  <td colspan=\"2\"><textarea name=\"ta_solu\" rows=\"6\" cols=\"60\">$solu</textarea></td>
 </tr>
</table>
<table class=\"details\">
 <tr>
  <td>
   $dis_button
  </td>
 </tr>
</table>
</form>
$dis_delete
";
}


///////////////////////////////////////////////////////////////////////////////
// Display Incident Consult                                                  //
// Parameters:
//   - $inc_q : DBO : incident result
//   - $con_q : DBO : contract result
///////////////////////////////////////////////////////////////////////////////
function html_incident_consult($inc_q,$con_q) {
  // -- Themes
  global $set_theme,$ico_contract,$ico_company;
  // - Labels
  global $l_incident_label,$l_incident_prority,$l_incident_status, $l_incident_company;
  global $l_incident_logger,$l_incident_owner,$l_incident_priority,$l_incident_status,$l_incident;
  global $l_update,$l_incident_description,$l_incident_solution,$l_incident_date,$l_contract_num;
  global $l_to, $l_from,$l_manager,$l_client_manager,$l_update,$l_archive_consult,$l_contract_label;
  global $path;

  $label = $inc_q->f("incident_label");
  $pri = $inc_q->f("incident_priority");
  $state = $inc_q->f("incident_state");
  $date = date_format($inc_q->f("date"));
  $inc_id = $inc_q->f("incident_id");
  $inc_desc = nl2br($inc_q->f("incident_description"));
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
  $dis_con1 = $con_q->f("lname_c1") . " " . $con_q->f("fname_c1");
  $dis_con2 = $con_q->f("lname_c2") . " " . $con_q->f("fname_c2");

  if ($inc_q->f("incident_archive") == 1) {
    $dis_lab_archive ="<tr><td class=\"detailLabel\" colspan=\"2\">$l_archive_consult</td></tr>";
  }

  // --- HTML Template --------------------------------------------------------

  echo "
<table class=\"details\">
 <tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident_company</td>
 </tr><tr>
  <td class=\"detailLabel\">
   $comp_name
   <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
   <img src=\"/images/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
  </td>
  <td class=\"detailText\">
   $comp_ad1<br />
   $comp_zip $comp_town
  </td>
 </tr><tr>
  <td class=\"detailLabel\">
   $l_client_manager 1
  </td>
  <td class=\"detailText\">
   $dis_con1
  </td>
 </tr><tr>
  <td class=\"detailLabel\">
   $l_client_manager 2
  </td>
  <td class=\"detailText\">
   $dis_con2
  </td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailHead\">$l_contract_label</td>
 </tr><tr>
  <td class=\"detailLabel\">
   <a href=\"". url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con_cli2")."\">
   </a> 
   $con_label
   <a href=\"". url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$con_id") . "\">
   <img src=\"/images/$set_theme/$ico_contract\" alt=\"[Contract]\" /></a>&nbsp;&nbsp;
  </td>
  <td class=\"detailText\">
   $l_contract_num : $con_num <br />
   $l_from $con_debut $l_to $con_exp
  </td>
 </tr><tr>
  <td class=\"detailLabel\">
   $l_manager 1
  </td>
  <td class=\"detailText\">$dis_market</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_manager 2</td>
  <td class=\"detailText\">$dis_tech</td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_label</td>
  <td class=\"detailText\">$label</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_priority</td>
  <td class=\"detailText\">$pri</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_status</td>
  <td class=\"detailText\">$state</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_date</td>
  <td class=\"detailText\">$date</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_logger</td>
  <td class=\"detailText\">$logger</td>
 </tr><tr>
  <td class=\"detailLabel\">$l_incident_owner</td>
  <td class=\"detailText\">$owner</td>
 </tr>
 $dis_lab_archive
 <tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident_description</td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailText\">$inc_desc</td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailHead\">$l_incident_solution</td>
 </tr><tr>
  <td colspan=\"2\" class=\"detailText\">$inc_reso</td>
 </tr>
</table>
";
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

  $dis_pref->display();
}


