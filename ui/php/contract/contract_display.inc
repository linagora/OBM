<script language="php">
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
  global $path, $set_theme;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  global $cck_customer, $cck_supplier, $l_cck_customer, $l_cck_supplier;

  if ($fieldname == "contract_label") {
    $res["url"] = "$path/contract/contract_index.php?action=detailconsult&amp;param_contract=".$OD->data_set->f("contract_id");
  }

  else if ($fieldname == "contract_company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("contract_company_id");
  }

  else if ($fieldname == "contract_marketmanager") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("contract_techmanager_id");
    $res["name"] = $OD->data_set->f("lmarket") . " " . $OD->data_set->f("fmarket");
  }

  else if ($fieldname == "contract_techmanager") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("contract_marketmanager_id");
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
    $res["name"] = date_format($date);
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
    $res["style"] = "style=\"color: #$col; font-family: sans-serif; font-size: 10px; background-color: #ffffff;\"";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract search form
// Parameters:
//   - $contract[] 
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_form($contract) {

  $usr_q = run_query_userobm();
  $type_q = run_query_contracttype();
  $prio_q = run_query_priority();
  $stat_q = run_query_status();

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
function html_contract_search_form($contract, $usr_q, $type_q,$pri_q,$sta_q) {
  global $l_number,$l_label_libelle,$l_company,$l_type,$l_after,$l_before;
  global $l_find,$l_manager,$l_priority, $l_archive, $l_status;
  global $cck_customer, $cck_supplier, $l_contract_kind,$l_cck_customer,$l_cck_supplier;
  global $c_all, $l_all;
 
  $status = $contract["status"];
  $priority = $contract["priority"];
  $label = $contract["label"];
  $comp = $contract["company_name"];
  $type_id = $contract["type"];
  $dateafter = $contract["dateafter"];
  $datebefore = $contract["datebefore"];
  $sel_market = $contract["market"];
  $sel_tech = $contract["tech"];
  $sel_manager = $contract["manager"];
  $num = $contract["number"]; 
  $archive = ($contract["archive"] == "1" ? "checked = \"checked\"" : ""); 
  $kind = $contract["kind"]; 
  
  $dis_cont_typ  = "<select name=\"sel_type\">
    <option value=\"$c_all\">$l_all</option>";
  while($type_q->next_record()) {
    $dis_cont_typ .= "\n<option value=\"".$type_q->f("contracttype_id")."\"";
    if ($type_id == $type_q->f("contracttype_id")) 
      $dis_cont_typ .= " selected=\"selected\"";
    $dis_cont_typ .= ">" . $type_q->f("contracttype_label")."</option>";
    }
    $dis_cont_typ .= "</select>";

  if ($usr_q->nf()>0){
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
  if ($kind == $cck_customer) {
    $sel_kind .= " selected=\"selected \" ";
  }
  $sel_kind .= ">$l_cck_customer</option>";
  $sel_kind .="<option value=\"$cck_supplier\" ";
  if ($kind == $cck_supplier) { 
    $sel_kind .= " selected=\"selected \"  ";
  }
  $sel_kind .= " >$l_cck_supplier</option>";
  $sel_kind .= "</select>";

  $dis_resp_tec = "<select name=\"sel_manager\">
    <option value=\"$c_all\">$l_all</option>";
  while($usr_q->next_record()) {
    $dis_resp_tec .= "\n<option value=\"".$usr_q->f("userobm_id")."\"";
    if ($usr_q->f("userobm_id") == $sel_manager) 
      $dis_resp_tec .= " selected=\"selected\"";
    $dis_resp_tec .= ">". $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")." </option>";
  }
  $dis_resp_tec .="</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\" 
  onsubmit=\"if (check_search_form(this) == false) return false;
  else return true;\" action=\"".url_prepare("contract_index.php")."\">

  <div class=\"search\">
    <div class=\"searchLabel\">$l_number<br />
      <input type=\"text\" name=\"tf_num\" size=\"20\" value=\"$num\" />
    </div>
    <div class=\"searchLabel\">$l_company<br />
      <input name=\"tf_company\" size=\"20\" maxlength=\"20\" value=\"$comp\" />
    </div>
    <div class=\"searchLabel\">$l_after<br />
      <input name=\"tf_dateafter\" size=\"10\" value=\"$dateafter\" />
   </div>
    <div class=\"searchLabel\">$l_before<br />
      <input type=\"text\" name=\"tf_datebefore\" size=\"10\" value=\"$datebefore\" />
    </div>
    <div class=\"searchLabel\">$l_label_libelle<br />
      <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"$label\" />
    </div>
    <div class=\"searchLabel\">$l_type<br />
      $dis_cont_typ
    </div>
    <div class=\"searchLabel\">$l_technical_manager<br />
    </div>
    <div class=\"searchLabel\">$l_manager<br />
      $dis_resp_tec
    </div>
    <div class=\"searchLabel\">$l_priority<br />
      $sel_pri
    </div>
    <div class=\"searchLabel\">$l_status<br />
      $sel_sta
     </div>
    <div class=\"searchLabel\">$l_contract_kind<br />
      $sel_kind
     </div>
    <div class=\"searchLabel\">$l_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </div>
    <div class=\"searchButton\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </div>
    <div class=\"searchEnd\"></div>
</div>
 </form>";

  return $block;
}



///////////////////////////////////////////////////////////////////////////////
// display the contract search result
// parameters:
//   - $contract[] : contract search criteria
//     keys used   : label,...
///////////////////////////////////////////////////////////////////////////////
function dis_contract_search_list($contract) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;
  
  $pref_q = run_query_display_pref($auth->auth["uid"], "contract");
  $obm_q = run_query_search($contract, $new_order, $order_dir);
  $nb_contract = $obm_q->num_rows_total();
  if ($nb_contract == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $block = html_contract_search_list($obm_q,$pref_q,$nb_contract,$contract);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Contract Search result                                   //
// Parameters :
//   - $contract_q : contracts list
//   - $pref_q     : fields to display in the contracts list
//   - $nb_con     : nb contract found
//   - $contract[] : contract search criteria
//     keys used   : label, type, dateafter, datebefore, company_name, market
///////////////////////////////////////////////////////////////////////////////
function html_contract_search_list($contract_q, $pref_q, $nb_con, $contract) {
  global $display, $l_found;

  $num = urlencode($contract["number"]); 
  $label = urlencode($contract["label"]);
  $type = $contract["type"];
  $dateafter = $contract["dateafter"];
  $datebefore = $contract["datebefore"];
  $company = urlencode($contract["company_name"]);
  $market = $contract["market"];
  $tech = $contract["tech"];
  $priority = $contract["priority"];
  $status = $contract["status"];
  $kind = $contract["kind"];
  $format = $contract["format"];

  $url = url_prepare("contract_index.php?action=search&amp;tf_label=$label&amp;tf_num=$num&amp;tf_company=$company&amp;sel_type=$type&amp;sel_priority=$priority&amp;sel_status=$status&amp;rd_kind=$kind&amp;rd_format=$format&amp;sel_tech=$tech&amp;sel_market=$market&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore");
  
  $contract_q->seek($first_row);
  $dis_contracts_list = new OBM_DISPLAY("DATA", $pref_q, "contract");
  $dis_contracts_list->data_set = $contract_q;
  $dis_contracts_list->data_url = $url;
  $dis_contracts_list->data_header = "both";

  // --- html template --------------------------------------------------------

  $display["msg"] .= display_info_msg($nb_con." ".$l_found);
  $block = $dis_contracts_list->display("dis_data_contract");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contract links menu
// Parameters:
//   - $id         : contract id
//   - $con_label  : contract label
//   - $deal_id    : deal id
//   - $inc_nb     : incident number
//   - $last       : last incident id
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_contract_links($id, $con_label, $deal_id, $inc_nb, $last) {
  global $set_theme, $ico_incident, $ico_deal, $ico_document,$ico_document_new;
  global $l_module_incident, $l_incident_list, $l_incident_new, $l_last;
  global $l_module_deal, $l_module_document, $l_contract;
  global $l_document_new,$l_document_add,$l_add_document,$l_deal;
  global $path, $auth, $cgp_show;

  $url_inc = url_prepare("$path/incident/incident_index.php?action=search&amp;param_contract=$id");
  $url_inc_new = url_prepare("$path/incident/incident_index.php?action=new&amp;param_contract=$id&amp;tf_lcontract=$con_label");
  
  $url_deal = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$deal_id");

  if ($last > 0) {
    $url_inc_last = url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_contract=$id&amp;param_incident=$last");
    $dis_last = "<li>
    <a href=\"$url_inc_last\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_incident\" /></a>
    <a href=\"$url_inc_last\">$l_last</a></li>";
  }

  $block = "<div class=\"link\">
  <div class=\"linkTitle\">:: $l_module_incident</div>
  <ul class=\"linkList\">
  <li>
    <a href=\"$url_inc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_incident\" /></a>
    <a href=\"$url_inc\">$l_incident_list ($inc_nb)</a>
  </li>
    $dis_last
  <li>
    <a href=\"$url_inc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_incident\" /></a>
    <a href=\"$url_inc_new\">$l_incident_new</a></li>
  </ul>

  <div class=\"linkTitle\">:: $l_module_deal</div>
  <ul class=\"linkList\">
  <li>
    <a href=\"$url_deal\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" /></a>
    <a href=\"$url_deal\">$l_deal</a>
  </li>
  </ul>";

  // Document  
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=Contract");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=Contract");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_document)."&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/contract/contract_index.php")."&amp;ext_id=$id&amp;ext_target=$l_contract";

    $nb_document = run_query_document_nb ($id,"Contract");
    $block .= "
  <div class=\"linkTitle\">:: $l_module_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_contact'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul> </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract consult
// Parameters:
//   - $contract[]    
///////////////////////////////////////////////////////////////////////////////
function dis_contract_consult($contract) {
  global $display, $l_consult_error;

  $id = $contract["id"];
  if ($id > 0) {
    $contract_q = run_query_detail($id);
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
  global $path, $display, $set_theme, $ico_company,$ico_contact, $ico_deal;
  global $l_yes, $l_no, $l_contract, $l_company, $l_deal, $l_private,$l_public;
  global $l_client_manager,$l_marketing_manager,$l_technical_manager;
  global $l_manager, $l_label, $l_comment, $l_number, $l_clause, $l_archive;
  global $l_date, $l_signature,$l_begin,$l_exp,$l_renew,$l_date_cancel;
  global $l_autorenewal, $l_type, $l_priority, $l_status, $l_contract_kind;
  global $l_format,$l_ticket_nb, $l_duration, $l_duration_used;
  global $cck_customer, $cck_supplier, $l_cck_customer, $l_cck_supplier;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;
  
  $usercreate = $contract_q->f("contract_usercreate");
  $cvis = ($contract_q->f("contract_privacy") == 0 ? $l_no : $l_yes);
  $autorenewal = ($contract_q->f("contract_autorenewal")==0 ? $l_no : $l_yes);
  $priority = $contract_q->f("contractpriority_label");
  $status = $contract_q->f("contractstatus_label");
  $id = $contract_q->f("contract_id");
  $label = $contract_q->f("contract_label");
  $number = $contract_q->f("contract_number");
  $market = $contract_q->f("lname1") . " " . $contract_q->f("fname1");
  $tech = $contract_q->f("lname2") . " " . $contract_q->f("fname2");
  $com = nl2br($contract_q->f("contract_comment"));
  $clause = nl2br($contract_q->f("contract_clause"));
  $datebegin = date_format($contract_q->f("datebegin"), 1);
  $dateexp = date_format($contract_q->f("dateexp"), 1);
  $daterenew = date_format($contract_q->f("daterenew"), 1);
  $datecancel = date_format($contract_q->f("datecancel"), 1);
  $datesignature = date_format($contract_q->f("datesignature"), 1);
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
    $format_data = "<td class=\"detailLabel\">$l_ticket_nb</td>
      <td class=\"detailText\">$ticket_nb</td>";
  } else {
    $label_format = $l_ccf_duration;
    $format_data = "<td class=\"detailLabel\">$l_duration</td>
      <td class=\"detailText\">$duration</td>";
  }
  
  $dis_format = "
      <td class=\"detailLabel\">$l_format</td>
      <td class=\"detailText\">$label_format</td>
    </tr><tr>
      $format_data";

  $con1 = $contract_q->f("clname1") . " " . $contract_q->f("cfname1") .
          " - " . $contract_q->f("cphone1"); 
  $con2 = $contract_q->f("clname2") . " " . $contract_q->f("cfname2") .
          " - " . $contract_q->f("cphone2"); 


  $dis_update = "<form method=\"get\" name=\"form_contract_update\"
      action=\"".url_prepare("contract_index.php")."\">
    <input type=\"hidden\" name=\"action\" value=\"detailupdate\">
    <input type=\"hidden\" name=\"param_contract\" value=\"$id\">
    <input type=\"submit\" value=\"$l_update\">
  </form>";


  // --- HTML Template --------------------------------------------------------

  $display["link"] = html_contract_links($id, $label, $deal_id, $inc_nb, $last_inc);
  $display["title"] = "<div class=\"title\">$l_contract : $label</div>";


  $block = "
  <table width=\"100%\">
  <tr>
    <td valign=\"top\" width=\"50%\">

      <div class=\"detailHead\">$dis_kind</div>
      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_company
          <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
        </td>
        <td class=\"detailText\">$comp_name</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_client_manager 1
          <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1_id") . "\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a>
        </td>
        <td class=\"detailText\">$con1</td> 
      </tr><tr>
        <td class=\"detailLabel\">$l_client_manager 2
          <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2_id") . "\">
          <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[Contact]\" /></a>
        </td>
        <td class=\"detailText\">$con2</td>
      </tr>
      </table>   

    </td>
    <td valign=\"top\" width=\"50%\">

      <div class=\"detailHead\">$l_manager</div>
      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_marketing_manager</td>
        <td class=\"detailText\">$market</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_technical_manager</td>
        <td class=\"detailText\">$tech</td>
      </tr>
      </table>

    </td>
  </tr>
  </table>

  <table width=\"100%\">
  <tr>
    <td valign=\"top\" width=\"50%\">

    <div class=\"detailHead\">$l_contract</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailText\">$label</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$number</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailText\">$type</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_priority</td>
      <td class=\"detailText\">$priority</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailText\">$cvis</td>
    </tr>
    </table>

    </td>
    <td valign=\"top\" width = 50%>

    <div class=\"detailHead\">$l_date</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_autorenewal</td>
      <td class=\"detailText\">$autorenewal</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_signature</td>
      <td class=\"detailText\">$datesignature</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_begin</td>
      <td class=\"detailText\">$datebegin</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_exp</td>
      <td class=\"detailText\">$dateexp</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_renew</td>
      <td class=\"detailText\">$daterenew</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_date_cancel</td>
      <td class=\"detailText\">$datecancel</td>
    </tr>
    </table>

    </td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_status</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_status</td>
    <td class=\"detailText\">$status</td>
  </tr><tr>
    $dis_format
  </tr><tr>
    <td class=\"detailLabel\">$l_duration_used</td>
    <td class=\"detailText\">$duration_used</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_comment</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailText\" colspan=\"2\">$com</td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_clause</div>
  <table class=\"detail\">
  <tr>
    <td class=\"detailText\" colspan=\"2\">$clause</td>
  </tr> 
  </table>";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Form
// Parameters:
//   - $action    : action called
//   - $contract[] : default values
///////////////////////////////////////////////////////////////////////////////
function dis_contract_form($action,$contract,$param_company) {
  global $display, $cg_com, $cg_prod;
 
  if ($action == "new" || $action == "insert" || $action == "update") {
    $contract_q = new DB_OBM; $users="";
  }
  if ($action == "detailupdate") {
    $contract_q = run_query_detail($contract["id"]);
    $users = array($contract_q->f("contract_marketmanager_id"), $contract_q->f("contract_techmanager_id"));
  } elseif ($action == "insert" || $action == "update" ) {
      $users = array($contract["market"], $contract["tech"]);
  } else {
      $users = "";
  }

  $comp_id = $contract_q->f("contract_company_id"); 
  if ($action == "new" || $action == "insert"){ 
    $company_q = run_query_company_info($param_company);
    $comp_id = $param_company;  
  }
    else {
      $company_q = run_query_company_info($comp_id);
    }
  
  if ($action == "detailupdate") {
    $display["detailInfo"] = display_record_info($contract_q);
  }
 
  $contact_q = run_query_contact_contract($comp_id);
  $prio_q = run_query_priority();
  $status_q = run_query_status();
  $usrc_q = run_query_all_users_from_group($cg_com, $users);
  $usrp_q = run_query_all_users_from_group($cg_prod, $users);
  $type_q = run_query_contracttype();
 
  $block = html_contract_form($action,$contract_q,$type_q, $usrc_q, $usrp_q, $company_q, $contact_q, $contract, $prio_q, $status_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Contract Form                                                     //
// Parameters :
//   - $action     : action called
//   - $contract_q : DBO : information about the contract (null for new)
//   - $type_q     : DBO : contract type list
//   - $u_com_q    : DBO : list of userobm from group Commercial
//   - $u_prod_q   : DBO : list of userobm from group Production
//   - $comp_q     : DBO : company infos (name, ad1, zip, town when new)
//   - $contact_q  : DBO : list of contact from the company 
//   - contract[]  : default or transmitted values
//     keys used   : all
///////////////////////////////////////////////////////////////////////////////
function html_contract_form($action,$contract_q,$type_q,$u_com_q,$u_prod_q,$comp_q,$contact_q, $contract,$pri_q,$sta_q){
  // - Themes
  global $set_theme,$ico_mail,$ico_company,$ico_deal,$ico_contact;
  global $path, $c_php_isodate_format, $l_update,$l_delete,$l_insert, $l_none;
  global $l_label,$l_begin,$l_type, $l_marketing_manager, $l_archive;
  global $l_signature, $l_begin, $l_exp, $l_renew,$l_date_cancel;
  global $l_technical_manager;
  global $popup_height,$popup_width,$l_comment,$l_number,$l_clause;
  global $l_priority,$l_status;
  global $l_contract_select_deal,$l_client_manager,$l_deal,$l_contract,$l_company;
  global $l_contract_kind,$l_format,$l_autorenewal;
  global $l_ticket_nb,$l_private,$uid,$l_duration; 
  global $cck_customer, $cck_supplier, $l_cck_customer,$l_cck_supplier;
  global $ccf_period, $ccf_ticket, $ccf_duration;
  global $l_ccf_period, $l_ccf_ticket, $l_ccf_duration;

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $contract_q->f("contract_id");
    $label = $contract_q->f("contract_label");
    $comp_id = $contract_q->f("contract_company_id");
    $number = $contract_q->f("contract_number");
    $priority = $contract_q->f("contract_priority_id");
    $vis = ($contract_q->f("contract_privacy") == 0 ? "0" : "1");
    $usercreate = $contract_q->f("contract_usercreate");
    $status = $contract_q->f("contract_status_id");
    $kind = $contract_q->f("contract_kind");
    $ticket_nb = $contract_q->f("contract_ticketnumber");
    $duration = $contract_q->f("contract_duration");
    $format = $contract_q->f("contract_format");
    $autorenewal = $contract_q->f("contract_autorenewal");
    $datebegin = isodate_format($contract_q->f("datebegin"), 1);
    $dateexp = isodate_format($contract_q->f("dateexp"), 1);
    $daterenew = isodate_format($contract_q->f("daterenew"), 1);
    $datecancel = isodate_format($contract_q->f("datecancel"), 1);
    $datesignature = isodate_format($contract_q->f("datesignature"), 1);
    $type_id = $contract_q->f("contract_type_id");
    $con1_id = $contract_q->f("contract_contact1_id");
    $con2_id = $contract_q->f("contract_contact2_id");
    $market_id = $contract_q->f("contract_marketmanager_id");
    $tech = $contract_q->f("contract_techmanager_id");
    $clause = $contract_q->f("contract_clause");
    $comment = $contract_q->f("contract_comment");
    $archive = $contract_q->f("contract_archive");
   
    // comp info below could be directly fetched from contract_q query XXXXX
   
    $comp_name = $comp_q->f("company_name");
    $ad1 = $comp_q->f("company_address1");
    $zip = $comp_q->f("company_zipcode");
    $town = $comp_q->f("company_town");   
    $deal_id = $contract_q->f("deal_id");
    $deal_label = $contract_q->f("deal_label");

  } elseif ($action == "new") {
    $comp_name = $comp_q->f("company_name");
    $ad1 = $comp_q->f("company_address1");
    $zip = $comp_q->f("company_zipcode");
    $town = $comp_q->f("company_town");
    $market = $comp_q->f("company_marketingmanager_id");
    $format = $ccf_period;
    $kind = $cck_customer;
        
    // We pre-fill date with current date     
    $datebegin = date($c_php_isodate_format);
    $dateexp = date($c_php_isodate_format, time() + 31536000);
  }

  // If parameters have been given, they supercede the default action value
  if (isset($contract["deal_id"])) { $deal_id = $contract["deal_id"]; }
  if (isset($contract["deal_label"])) { $deal_label = stripslashes($contract["deal_label"]); }
  if (isset($contract["id"])) { $id = $contract["id"]; }
  if (isset($contract["label"])) { $label = stripslashes($contract["label"]); }
  if (isset($contract["company_id"])) { $comp_id = $contract["company_id"]; }
  if (isset($contract["number"])) { $number = $contract["number"]; }
  if (isset($contract["priority"])) { $priority = $contract["priority"]; }
  if (isset($contract["privacy"])) { $vis = $contract["privacy"]; }
  if (isset($contract["status"])) { $status = $contract["status"]; }
  if (isset($contract["datebegin"])) { $datebegin = $contract["datebegin"]; }
  if (isset($contract["dateexp"])) { $dateexp = $contract["dateexp"]; }
  if (isset($contract["daterenew"])) { $daterenew = $contract["daterenew"]; }
  if (isset($contract["datecancel"])) { $datecancel = $contract["datecancel"]; }
  if (isset($contract["datesignature"])) { $datesignature = $contract["datesignature"]; } 
  if (isset($contract["type"])) { $type_id = $contract["type"]; }
  if (isset($contract["autorenewal"])) { $autorenewal = $contract["autorenewal"]; }
  if (isset($contract["format"])) { $format = $contract["format"]; }
  if (isset($contract["ticketnumber"])) { $ticket_nb = $contract["ticketnumber"]; }
  if (isset($contract["duration"])) { $duration = $contract["duration"]; }
  if (isset($contract["kind"])) { $kind = $contract["kind"]; }
  if (isset($contract["contact1"])) { $con1_id = $contract["contact1"]; }
  if (isset($contract["contact2"])) { $con2_id = $contract["contact2"]; }
  if (isset($contract["market"])) { $market_id = $contract["market"]; }
  if (isset($contract["tech"])) { $tech = $contract["tech"]; }
  if (isset($contract["clause"])) { $clause = stripslashes($contract["clause"]); }
  if (isset($contract["comment"])) { $comment = stripslashes($contract["comment"]); }
  if (isset($contract["company_name"])) { $comp_name = stripslashes($contract["company_name"]); }
  if (isset($contract["company_ad1"])) { $ad1 = stripslashes($contract["company_ad1"]); }
  if (isset($contract["company_zip"])) { $zip = $contract["company_zip"]; }
  if (isset($contract["company_town"])) { $town = stripslashes($contract["company_town"]); }
  if (isset($contract["archive"])) { $archive = $contract["archive"]; }

  if ($archive == 1) {
    $archive_display = " checked=\"checked\"";
  }
  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) && 
         ($usercreate == $uid) ) ) {
    $dis_vis = "<tr>
      <td class=\"detailLabel\">$l_private</td>
      <td class=\"detailForm\"><input name=\"cb_vis\" type=\"checkbox\" value=\"1\" ";
    if ($vis == 1) $dis_vis .= "checked = \"checked\"";
      $dis_vis .= " /></td></tr>";
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

  $dis_type="<select name=\"sel_type\">
    <option value=\"0\">$l_none</option>";
  while($type_q->next_record()) {
    $dis_type.="\n<option value=\"".$type_q->f("contracttype_id")."\"";
    if ($type_id==$type_q->f("contracttype_id"))
       $dis_type.=" selected=\"selected\""; 
    $dis_type.=">".$type_q->f("contracttype_label")."</option>";
  }
  $dis_type.="</select>";
 
  $sel_market = "<select name=\"sel_market\">
    <option value=\"0\">$l_none</option>";
  while ($u_com_q->next_record()) {
   $sel_market .= "\n<option value=\"".$u_com_q->f("userobm_id")."\"";
    if ($u_com_q->f("userobm_id") == $market_id) 
      $sel_market .= " selected=\"selected\"";
    $sel_market .= ">". $u_com_q->f("userobm_lastname") . " " . $u_com_q->f("userobm_firstname")."</option>";
  }
  $sel_market.="</select>";
 
  $sel_tech = "<select name=\"sel_tech\">
    <option value=\"0\">$l_none</option>";
  while ($u_prod_q->next_record()) {
   $sel_tech .= "\n<option value=\"".$u_prod_q->f("userobm_id")."\"";
   if ($u_prod_q->f("userobm_id") == $tech) 
      $sel_tech.=" selected=\"selected\"";
   $sel_tech.=">". $u_prod_q->f("userobm_lastname") . " " . $u_prod_q->f("userobm_firstname")."</option>";
  }
  $sel_tech .= "</select>";

  if ($autorenewal == 1) { 
    $autorenew_c = "checked";
  }
  $autorenewal_display = "<input type=\"checkbox\" name=\"cb_autorenew\" value=\"1\" $autorenew_c />";

  // gestion du type du contrat : client ou fournisseur
  if ($kind == "$cck_customer") {
    $kind_customer_c = "checked";
  } else {
    $kind_supplier_c = "checked";
  }
  
  $radio_kind = "<input type=\"radio\" name=\"rd_kind\" value=\"$cck_customer\" $kind_customer_c/> $l_cck_customer ";
  $radio_kind .= "<input type=\"radio\" name=\"rd_kind\" value=\"$cck_supplier\" $kind_supplier_c/> $l_cck_supplier<br /> ";

  if ($format == $ccf_period) {
    $cb_period_c = "checked";
    $style_ticket = "style=\"display:none;\"";
    $style_duration = "style=\"display:none;\"";
  } else if ($format == $ccf_ticket) {
    $cb_ticket_c = "checked";
    $style_ticket = "style=\"display:block;\"";
    $style_duration = "style=\"display:none;\"";
  } else {
    $cb_duration_c = "checked";
    $style_ticket = "style=\"display:none;\"";
    $style_duration = "style=\"display:block;\"";
  }

  $dis_format = "
    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_period\" $cb_period_c
      onclick=\"
          this.form.tf_ticket_nb.style.display = 'none';
          this.form.tf_duration.style.display = 'none';
	  document.getElementById('label_ticket').style.display = 'none';
	  document.getElementById('label_duration').style.display = 'none';
      \" /> $l_ccf_period

    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_duration\" $cb_duration_c onclick=\"this.form.tf_ticket_nb.style.display = 'none';
	  document.getElementById('label_ticket').style.display = 'none';
	  document.getElementById('label_duration').style.display = 'block';
	  this.form.tf_duration.style.display = 'block';
        \" /> $l_ccf_duration

    <input type=\"radio\" name=\"rd_format\" value=\"$ccf_ticket\" $cb_ticket_c onclick=\"
          this.form.tf_ticket_nb.style.display = 'block';
          document.getElementById('label_ticket').style.display = 'block';
          document.getElementById('label_duration').style.display = 'none';
          this.form.tf_duration.style.display = 'none';
         \" /> $l_ccf_ticket";

  $dis_format_detail = "
    <tr>
      <td class=\"detailLabel\">
        <div id=\"label_ticket\" $style_ticket>$l_ticket_nb</div>
        <div id=\"label_duration\" $style_duration>$l_duration</div>
      </td>
      <td class=\"detailForm\">
        <input type=\"textbox\" $style_ticket name=\"tf_ticket_nb\" value =\"$ticket_nb\" />
        <input type=\"textbox\" $style_duration name=\"tf_duration\" value =\"$duration\" />
      </td>
    </tr>
";

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con1 = "<select name=\"sel_con1\">
    <option value=\"0\">$l_none</option>";
  while ($contact_q->next_record()) {
    $lc_id = $contact_q->f("contact_id");
    $lc_text = $contact_q->f("contact_lastname")." ". $contact_q->f("contact_firstname") . " - " . $contact_q->f("contact_phone");
    $dis_sel_con1.="\n<option value=\"$lc_id\"";
    if ($lc_id == $con1_id) {
      $dis_sel_con1.=" selected=\"selected\"";
    }
    $dis_sel_con1 .= ">$lc_text</option>";
  }
  $dis_sel_con1.="</select>";
  
  if ($action == "detailupdate") {
    $dis_con1 = "
      <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=".$con1_id)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[contact\" /></a>";    
  }

  if ($contact_q->nf()>0) {
    $contact_q->seek(0);
  }
   
  $dis_sel_con2="<select name=\"sel_con2\">
    <option value=\"0\">$l_none</option>";
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

  if ($action == "detailupdate") {
    $dis_con2 = "
      <a href=\"".url_prepare("$path/contact_index.php?action=detailconsult&amp;param_contact=".$con2_id)."\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[contact\" /></a>";    
  }

  $dis_sub = "
    <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
    <input type=\"hidden\" name=\"tf_company\" value=\"$comp_name\" />
    <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
    <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
    <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />";


  if (($action == "detailupdate") || ($action == "update")) {
    $dis_sub .= "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"param_contract\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";
  } else {
    $dis_sub .= "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  // --- html template --------------------------------------------------------

  $block = "
  <form method=\"post\" name=\"form_contract\"
    onsubmit=\"if (check_contract(this)) return true; else return false;\" 
    action=\"".url_prepare("contract_index.php")."\">

  <div class=\"detailHead\">$l_company</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
        <a href=\"".url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id"). "\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[Company]\" /></a>
        $comp_name
      </td>
      <td class=\"detailText\">$ad1
        <br />$zip $town
      </td> 
    </tr><tr>
      <td class=\"detailLabel\">$l_client_manager 1 $dis_con1</td>
      <td class=\"detailForm\">$dis_sel_con1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_client_manager 2 $dis_con2</td>
      <td class=\"detailForm\">$dis_sel_con2</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_deal
        <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"deal_new_id\" value=\"$c_new_id\" />
      </td>
      <td class=\"detailForm\">
        <a href=\"".url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$deal_id")."\">$deal_label</a>
        <a href=\"\" onclick=\"window.open('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contract_select_deal)."&amp;ext_widget=form_contract.deal_new_id&amp;ext_widget_text=form_contract.deal_label&amp;comp_id=$comp_id','contract','height=$popup_height,width=$popup_width'); return false;\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"$l_contract_select_deal\" /></a>
        <input type=\"text\" name=\"deal_label\" value=\"$deal_label\" size =\"40\" readonly=\"readonly\" onfocus=\"this.blur();\" />
      </td>
    </tr>
    </table>
  <div class=\"detailHead\">$l_contract</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailForm\"><input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\"><input name=\"tf_num\" type=\"text\" size=\"20\" maxlength=\"40\" value=\"$number\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_signature</td>
      <td class=\"detailForm\"><input name=\"tf_datesignature\" type=\"text\" size=\"10\" value=\"$datesignature\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_begin</td>
      <td class=\"detailForm\"><input name=\"tf_datebegin\" type=\"text\" size=\"10\" value=\"$datebegin\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_exp</td>
      <td class=\"detailForm\"><input name=\"tf_dateexp\" type=\"text\" size=\"10\" value=\"$dateexp\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_renew</td>
      <td class=\"detailForm\"><input name=\"tf_daterenew\" type=\"text\" size=\"10\" value=\"$daterenew\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_date_cancel</td>
      <td class=\"detailForm\"><input name=\"tf_datecancel\" type=\"text\" size=\"10\" value=\"$datecancel\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailForm\">$dis_type</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_marketing_manager</td>
      <td class=\"detailForm\">$sel_market</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_technical_manager</td>
      <td class=\"detailForm\">$sel_tech</td>    
    </tr><tr>
      <td class=\"detailLabel\">$l_priority</td>
      <td class=\"detailForm\">$sel_pri</td> 
    </tr><tr>
      <td class=\"detailLabel\">$l_status</td>
      <td class=\"detailForm\">$sel_sta</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_autorenewal</td>
      <td class=\"detailForm\">$autorenewal_display</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input name=\"cb_archive\" type=\"checkbox\"  size=\"40\" value=\"1\" $archive_display/></td>
    </tr>
      $dis_vis
    <tr>
      <td class=\"detailLabel\">$l_contract_kind</td>
      <td class=\"detailForm\">$radio_kind</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_format</td>
      <td class=\"detailForm\">$dis_format</td>
    </tr>
$dis_format_detail
    </table>

  <div class=\"detailHead\">$l_comment</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailForm\"><textarea name=\"ta_com\" rows=\"6\" cols=\"72\">$comment</textarea></td>
    </tr>
    </table>
  <div class=\"detailHead\">$l_clause</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailForm\"><textarea name=\"ta_clause\" rows=\"6\" cols=\"72\">$clause</textarea></td>
    </tr>
    </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_sub</p>
  </div>
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
  global $display, $path, $l_query_error;
  global $auth;

  $con_q = run_query_detail($param_contract);
  $company_id = $con_q->f("company_id");

  if ($con_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg($l_query_error . " - " . $con_q->query . " !");
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
  global $l_date, $l_signature, $l_begin, $l_exp, $l_renew, $l_date_cancel;
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
  $export_contractdate = "Contract $l_date: \n";
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
  header("charset=utf-8"); 

  $export = "BEGIN:\n\n";
  $export .= "$export_company\n";
  $export .= "$export_contract\n";
  $export .= "$export_status\n";
  $export .= "$export_comment\n";
  $export .= "$export_clause\n";
  $export .= "END:";
  echo iconv("ISO-8859-15","UTF-8",$export);
}



///////////////////////////////////////////////////////////////////////////////
// Display the context about a contract insertion or update                  //
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
  $ticket_nb =$contract["ticketnumber"];
  $duration = $contract["duration"];
  $autorenewal =$contract["autorenewal"];
  $daterenew = $contract["daterenew"];
  $datecancel = $contract["datecancel"];
  $datesignature = $contract["datesignature"];
  $sel_type = $contract["type"];
  $ta_com = $contract["comment"];
  $con1 = ($contract["contact1"] ? $contract["contact1"] : "0");
  $con2 = ($contract["contact2"] ? $contract["contact2"] : "0");
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
      <tr><td colspan =\"2\" class=\"detailText\">
         <a class=\"detail\" href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;param_contract=$id") . "\">
          $samename </a>
      </td></tr>";
  }

  $hidden_fields = "<input type=\"hidden\" name=\"tf_label\" value=\"$name\" />
      <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
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
      <input type=\"hidden\" name=\"cb_autorenew\" value=\"$autorenewal\" /> 
      <input type=\"hidden\" name=\"tf_daterenew\" value=\"$daterenew\" />
      <input type=\"hidden\" name=\"tf_datecancel\" value=\"$datecancel\" />
      <input type=\"hidden\" name=\"tf_datesignature\" value=\"$datesignature\" />
      <input type=\"hidden\" name=\"sel_type\" value=\"$sel_type\" />
      <input type=\"hidden\" name=\"ta_com\" value=\"$ta_com\" />
      <input type=\"hidden\" name=\"sel_con1\" value=\"$con1\" />
      <input type=\"hidden\" name=\"sel_con2\" value=\"$con2\" />
      <input type=\"hidden\" name=\"sel_tech\" value=\"$sel_tech\" />
      <input type=\"hidden\" name=\"sel_market\" value=\"$sel_market\" />
      <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />
      <input type=\"hidden\" name=\"deal_label\" value=\"$deal_label\" />
      <input type=\"hidden\" name=\"cb_archive\" value=\"$archiv\" />
      <input type=\"hidden\" name=\"cb_vis\" value=\"$vis\" />
";

  $block = "
  <table class=\"detail\">
    $dis_same_cont
  </table>
  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <form method=\"post\" name=\"form_insert\"
    action=\"" .url_prepare("contract_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"hd_confirm\" value=\"$c_yes\" />
      $hidden_fields
      <input type=\"submit\"  value=\"$l_confirm\" />
    </form>
    </p>
    <p class=\"detailButtons\">
    <form name=\"form_back\" method=\"post\"
    action=\"" .url_prepare("contract_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"new\" />
      $hidden_fields
      <input type=\"submit\" value=\"$l_back\" />
    </form>
    </p>
  </div>";

  return $block;
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
    $id = $pri_q->f("contractpriority_id");
    $order = $pri_q->f("contractpriority_order");
    $color = $pri_q->f("contractpriority_color");
    $label = $pri_q->f("contractpriority_label");
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
              action=\"" . url_prepare("contract_index.php") . "\">
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
              action=\"" . url_prepare("contract_index.php") . "\">
           
            <input type=\"hidden\" name=\"action\" value=\"priority_update\" />
            <input type=\"hidden\" name=\"sel_priority\" value=\"\" />
          <table>
          <tr>
            <td class=\"adminLabel\">$l_label: </td>
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
          action=\"" . url_prepare("contract_index.php") . "\">
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
// Displays the priority links                                               //
// Parameters:
//   - $id : priority id
///////////////////////////////////////////////////////////////////////////////
function dis_priority_links($id) {
  global $l_pri_link_contract, $l_pri_link_contract_no;
  global $l_pri_delete, $l_pri_can_delete, $l_pri_cant_delete;
  global $l_back,$display;

  $label = get_priority_label($id);
  $obm_q = run_query_priority_links($id);

  $dis_back = "<form name=\"form_back\" method=\"post\"
        action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";
	
  $nb_pri = $obm_q->num_rows();
  if ($nb_pri > 0) {
    $display["msg"] = display_warn_msg($l_pri_cant_delete); 
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\"> $label : $l_pri_link_contract ($nb_pri)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contract_id");
      $cname = $obm_q->f("contract_label");
      $dis_link .="
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;param_contract=$cid") . "\">$cname</a>
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
      $l_pri_link_contract_no $label</td>
    </tr><tr>
      <td>
      </td>
    </table>
    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <form name=\"form_pri_delete\"
          method=post action=\"" . url_prepare("contract_index.php") . "\">
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
// Contract Status section                                                   //
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
    $id = $sta_q->f("contractstatus_id");
    $order = $sta_q->f("contractstatus_order");
    $label = $sta_q->f("contractstatus_label");
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
          method=\"post\" action=\"" . url_prepare("contract_index.php")."\">
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
            action=\"" . url_prepare("contract_index.php") . "\">
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
        method=\"post\" action=\"" . url_prepare("contract_index.php") . "\">
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
        action=\"" .url_prepare("contract_index.php") . "\">
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
      $stid = $obm_q->f("contract_id");
      $stlabel = $obm_q->f("contract_label");
      $dis_link.= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;param_incident=$stid") . "\">$stlabel</a>
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
          method=post action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
        <input type=\"hidden\" name=\"sel_status\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_status\" value=\"$l_sta_delete\" />
        </form>
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
	
	</p>
    </div>";

  }
  return $dis_link;
}



///////////////////////////////////////////////////////////////////////////////
// Displays and check the contract links (and delete form if ok)             //
// Parameters:
//   - $p_id : contract id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $display, $path, $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_incident, $l_link_incident_no;

  $delete_ok = true;

  // Links from Incident
  $obm_q = run_query_contract_incident_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linki ="<tr><td class=\"detailHead\">$l_link_incident</td></tr>";
    while ($obm_q->next_record()) {
      $iid = $obm_q->f("incident_id");
      $ilabel = $obm_q->f("incident_label");
      $display_linki .= "
      <tr>
        <td class=\"detailLabel\"><a class=\"detail\" href=\"" . url_prepare("$path/incident/incident_index.php?action=detailconsult&amp;param_incident=$iid") . "\">$ilabel</a></td>
      </tr>";
    }
  } else {
    $display_linki = "
      <tr>
        <td class=\"detailHead\">$l_link_incident_no</td>
      </tr>";
  }

  $block .= "<table class=\"detail\">
   $display_linki
   </table>";

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("contract_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_contract\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_can_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">" . dis_delete_form($p_id) . "</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the delete form (button)                                         //
// Parameters:
//   - $p_id : deal Id
///////////////////////////////////////////////////////////////////////////////
function dis_delete_form($p_id) {
  global $l_delete;

  $ret = "<form name=\"form_delete\" method=\"post\"
      action=\"" . url_prepare("contract_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_contract\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
}



///////////////////////////////////////////////////////////////////////////////
// Display the Contract admin index                                          //
///////////////////////////////////////////////////////////////////////////////
function dis_admin_index() {

  $pri_q = run_query_priority();
  $sta_q = run_query_status();
  
  return html_priority_form($pri_q) . "<hr />" .html_status_form($sta_q)."<hr />" .html_contract_admin_form(run_query_contracttype());
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
  global $l_type_update_error, $l_type_delete_error, $l_type_delete_ok;

  $dis_sel_type = "<select name=\"sel_type\" onChange=\"
    forms.form_type_update.tf_type.value=this.options[this.selectedIndex].text;\"
    size=\"4\">";
  while ($type_q->next_record()) {
    $dis_sel_type .= "\n<option value=\"".$type_q->f("contracttype_id")."\">".$type_q->f("contracttype_label") .
           "</option>";
  }
  $dis_sel_type .= "</select>";

  // --- html template --------------------------------------------------------

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_type_manage</td>
    </tr>
    <tr>
      <td>
<!-- type delete section -->
        <table>
        <tr>
         <td colspan=\"3\">
     <form name=\"form_type_delete\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
       <table>
        <tr>
          <td class=\"adminLabel\">$l_type_exist</td>
          <td class=\"adminLabel\">$dis_sel_type</td>
          <td>
            <input type=\"hidden\" name=\"action\" value=\"type_checklink\" />
            <input type=\"submit\" name=\"sub_type\" value=\"$l_type_checkdelete\"
              onclick=\"if (check_type_del(this.form)) return true;
              else return false;\" />
          </td>
         </tr>
        </table>
       </form>
      </td>
     </tr>
<!--  type update section -->
     <tr>
      <td colspan=\"2\" class=\"adminLabel\">
       <form name=\"form_type_update\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
       <table>
         <tr>
          <td>
	    <input type=\"hidden\" name=\"sel_type\" value=\"\" />
	    <input type=\"hidden\" name=\"action\" value=\"type_update\" />
            <input type=\"text\" name=\"tf_type\" />
	  </td>
	  <td rowspan=\"2\">
	    <input type=\"submit\" name=\"sub_type\" value=\"$l_type_update\"
	    onclick=\"if (check_type_upd(this.form,document.form_type_delete)) return true; else return false;\" />
	  </td>
	</tr>
       </table>
       </form>
      </td>
      <td>&nbsp;</td>
     </tr>
    </table>
<!-- new type section -->
   </td>
   <td>
    <form name=\"form_type_new\" method=\"post\" action=\"".url_prepare("contract_index.php")."\">
     <table>
      <tr>
       <td class=\"adminLabel\">
         $l_type_new : <input type=\"text\" name=\"tf_type\" />
       </td>
      </tr><tr>
       <td class=\"adminLabel\">
         <input type=\"hidden\" name=\"action\" value=\"type_insert\" />
         <input type=\"submit\" name=\"sub_type\" value=\"$l_type_insert\"
         onclick=\"if (check_type_new(this.form)) return true;else return false;\" />
       </td>
      </tr>
     </table>
    </form>
   </td>
  </tr>
 </table>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the type links                                                   //
// Parameters:
//   - $id : type id
///////////////////////////////////////////////////////////////////////////////
function dis_type_links($id) {
  global $l_type_link_contract, $l_type_can_delete, $l_type_cant_delete;
  global $l_type_delete, $l_back;
  global $display;

  $label = get_type_label($id);
  $obm_q = run_query_type_links($id);

  $nb_type = $obm_q->num_rows();
  if ($nb_type > 0) {
    $display["msg"] .= display_warn_msg("$label : $l_type_cant_delete");
    $dis_link = "
    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$label : $l_type_link_contract ($nb_type)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("contract_index.php?action=detailconsult&amp;param_contract=$cid") . "\">$clabel</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_type) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
    </tr>";
    }

    $dis_link .= "
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
    </p>
    </div>";

  } else {
    $display["msg"] .= display_ok_msg("$label : $l_type_can_delete");
    $dis_link = "
    <div class=\"detailButton\">
    <p class=\"detailButtons\">
        <form name=\"form_type_delete\".
          method=\"post\" action=\"" . url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"type_delete\" />
        <input type=\"hidden\" name=\"sel_type\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_type\" value=\"$l_type_delete\" />
        </form>
    </p>
    <p class=\"detailButtons\">
        <form name=\"form_back\" method=\"post\"
          action=\"" .url_prepare("contract_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>
    </p>
    </div>";
  }

  return $dis_link;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Contract Display preference screen
// Parameters:
//   - $pref_q : DBO : Contract Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_contract_display_pref($pref_q) {
  global $l_contract_display;
  
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "contract"; 
  $dis_pref->pref_title = $l_contract_display;
  $dis_pref->pref_dis_help = 1;

  // --- html template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// display contract select form                                              //
// parameters:
//   - $comp_q : company database result
//   - $title  : title to be displayed
//   - $url    : url to call with parameter company
///////////////////////////////////////////////////////////////////////////////
function html_select_contract($con_q, $title, $url="") {
  global $l_select_contract, $l_close, $display;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }

  $display_cont = "
    <select name=\"param_contract\" size=\"10\" style=\"font-family:fixed,courier\">";

  while ($con_q->next_record()) {
    $id = $con_q->f("contract_id");
    $name = $con_q->f("contract_label");
    $company = $con_q->f("company_name");
    $display_cont .= "\n<option value=\"$id\">$company - $name</option> ";
  }
  $display_cont.="</select>";

  $display["title"] = "<div class=\"title\">$title</div>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"form_contr\"
    onsubmit=\"if ($jsfunc) return true; else return false;\" action=\"\">

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">
    $display_cont
    </td>
  </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_select_contract\" />
    </p>
  </div>
  </form>

  <a href=\"\" onclick='window.close();'>$l_close</a>
";

  return $block;
}
 
</script>
