<SCRIPT language="php">
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
$fieldnames["deal_label"] = $l_label;
$fieldnames["deal_datealarm"] = $l_date_alarm;
$fieldnames["deal_archive"] = $l_archive_first;
$fieldnames["deal_todo"] = $l_todo;
$fieldnames["deal_marketingmanager"] = $l_marketing_manager;
$fieldnames["deal_amount"] = $l_amount;
$fieldnames["deal_hitrate"] = $l_hitrate;
$fieldnames["parentdeal_label"] = $l_label;
$fieldnames["parentdeal_comment"] = $l_comment;
// Calculated or indirect fields
$fieldnames["dealstatus_label"] = $l_state;
$fieldnames["dealtype_label"] = $l_kind;
$fieldnames["tasktype_label"] = $l_category;
$fieldnames["deal_company_name"] = $l_company;
$fieldnames["deal_company_zipcode"] = $l_zipcode;
$fieldnames["parentdeal_marketing_lastname"] = $l_marketing_manager;
$fieldnames["parentdeal_technical_lastname"] = $l_technical_manager;
$fieldnames["parentdeal_status"] = $l_state;
$fieldnames["parentdeal_dateproposal"] = $l_date_proposal;
$fieldnames["parentdeal_datealarm"] = $l_date_alarm;
$fieldnames["parentdeal_amountprovider"] = $l_amount_out;
$fieldnames["parentdeal_amountcustomer"] = $l_amount_in;
// fields linked to invoices
$fieldnames["invoice_number"] = $l_invoice_number;
$fieldnames["invoice_date"] = $l_invoice_date;
$fieldnames["invoice_label"] = $l_invoice_label;
$fieldnames["invoice_amount_HT"] = $l_invoice_HT;
$fieldnames["invoice_amount_TTC"] = $l_invoice_TTC;
$fieldnames["invoice_status"] = $l_invoice_status;
$fieldnames["invoice_deal"] = $l_deal;
$fieldnames["invoice_company"] = $l_company;


///////////////////////////////////////////////////////////////////////////////
// Display Deal specific dataset fields                                      //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_deal(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_ok, $col_error, $col_client, $col_frs;

  if ($fieldname == "parentdeal_label") {
    $res["url"] = "$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$OD->data_set->f("parentdeal_id");
  }

  else if ($fieldname == "parentdeal_marketing_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("parentdeal_marketingmanager_id");
  }

  else if ($fieldname == "parentdeal_technical_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("parentdeal_technicalmanager_id");
  }

  else if ($fieldname == "deal_label") {
    $res["name"] = "";
    $res["url"] = "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$OD->data_set->f("deal_id");
  }

  else if ($fieldname == "deal_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) $res["name"] = "X";
    else $res["name"] = "&nbsp;";
  }

  else if ($fieldname == "deal_hitrate") {
    $res["align"] = "center";
    $res["name"] = $OD->data_set->f($fieldname) . " %";
  }

  else if ($fieldname == "deal_company_name") {
    $res["name"] = "";
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("deal_company_id");
  }
  
  else if ($fieldname == "deal_datealarm") {
    $date = $OD->data_set->f($fieldname);
    $datealarm = date_format($date);
    $time = time();
    if ($time > ($date + 86400)) {
      $col = $col_error;
    } else if ($time > $date) {
      $col = $col_ok;
    }
    $res["style"] = "style=\"color: #$col; font-family: sans-serif; font-size: 10px; background-color: #ffffff;\"";
    $res["name"] = "$datealarm";
  }

  // Invoice fields ?????? USED ??????
  else if ($fieldname == "deal_invoice_label") {
    $res["url"] = "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$OD->data_set->f("invoice_id");
  }

  else if ( ($fieldname == "deal_invoice_HT") 
	   || ($fieldname == "deal_invoice_TTC") ) {
    $amount = $OD->data_set->f($fieldname);
    if ($OD->data_set->f("invoice_inout") == '+') {
      $col = $col_client;
    }
    else {
      $col = $col_frs;
    }
    $res["style"] = "style=\"color: #$col; font-family: sans-serif; font-size: 10px; background-color: #ffffff;\"";
    $res["name"] = "AAAAAA $amount";
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal index page                                                   //
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_index($deal="") {
  global $display;

  $usr_q = run_query_userobm();
  $block .= html_parentdeal_search_form($deal, $usr_q);
  $block .= html_deal_search_form($deal, run_query_dealtype(), run_query_deal_tasktype(), run_query_dealstatus(), $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal search form                                                  //
// Parameters : 
//   - $deal[]        : default form values
//     keys used      : parent, label, company_name, archive, kind, cat, state,
//                      manager, dateafter, datebefore
//   - $obm_q_type    : DB result object (deal kind list)
//   - $obm_q_cat     : DB result object (deal categories list)
//   - $obm_q_state   : DB result object (deal status list)
//   - $obm_q_manager : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_form($deal, $obm_q_type, $obm_q_cat, $obm_q_state, $obm_q_manager) {
  global $l_deal, $l_label_start, $l_company, $l_kind, $l_category, $l_state;
  global $l_after, $l_before, $l_manager, $l_show_archive, $l_zipcode;
  global $l_all, $l_all_f, $l_find, $c_all;

  // --- Var preparation ------------------------------------------------------

  $parent = $deal["parent"];
  $label = stripslashes($deal["label"]);
  $name = stripslashes($deal["company_name"]);
  $zip = stripslashes($deal["company_zip"]);
  $archive = ($deal["archive"] == "1" ? "checked = \"checked\"" : "");
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);
  $company_id = $deal["company"];

  // Kind select construction
  $sel_kind = "<select name=\"sel_kind\">
     <option value=\"$c_all\">$l_all</option>";
  while($obm_q_type->next_record()) {
    $id = $obm_q_type->f("dealtype_id");
    $klabel = $obm_q_type->f("dealtype_label");
    $sel_kind .= "<option value=\"$id\"";
    if ($kind == $id) $sel_kind .= " selected = \"selected\"";
    $sel_kind .= ">$klabel</option>";
  }
  $sel_kind .= "</select>";


  //TaskType select construction
  $sel_cat = "<select name=\"sel_cat\">
              <option value=\"$c_all\">$l_all</option>";
  while ($obm_q_cat->next_record()) {
    //put menu titles
    $id = $obm_q_cat->f("tasktype_id");
    $tt_label = $obm_q_cat->f("tasktype_label");
    $sel_cat .= "\n<option value=\"$id\"";
    if ($cat == $id) $sel_cat .= "selected =\"selected\"";
    $sel_cat .= ">$tt_label</option>\n";
  }
  $sel_cat .= "</select>";

  // State select construction
  $sel_state = "<select name=\"sel_state\">
     <option value=\"$c_all\">$l_all</option>";
  while($obm_q_state->next_record()) {
    $id = $obm_q_state->f("dealstatus_id");
    $slabel = $obm_q_state->f("dealstatus_label");
    $sel_state .= "<option value=\"$id\"";
    if ($state == $id) $sel_state .= "selected =\" selected\"";
    $sel_state .= ">$slabel</option>";
  }
  $sel_state .= "</select>";

  // Manager select construction
  if ($obm_q_manager->num_rows() > 0) {
    $obm_q_manager->seek(0);
  }
  $sel_manager = "<select name=\"sel_manager\">
     <option value=\"$c_all\">$l_all</option>";
  while($obm_q_manager->next_record()) {
    $id = $obm_q_manager->f("userobm_id");
    $lname = $obm_q_manager->f("userobm_lastname");
    $sel_manager .= "<option value=\"$id\"";
    if ($manager == $id) $sel_manager .= "selected =\"selected\"";
    $sel_manager .= ">$lname</option>";
  }
  $sel_manager .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"form_deal\"
  onsubmit=\"if (check_search_form(this) == false) return false; else return true;\"
  action=\"" . url_prepare("deal_index.php") . "\">
  <div class=\"detailHead\">$l_deal</div>
  <div class=\"search\">
    <div class=\"searchLabel\">$l_label_start<br />
      <input name=\"tf_label\" size=\"16\" maxlength=\"128\" value=\"$label\" />
    </div>
    <div class=\"searchLabel\">$l_company<br />
      <input name=\"tf_company_name\" size=\"12\" maxlength=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_zipcode<br />
      <input name=\"tf_zip\" size=\"14\" value=\"$zip\" />
    </div>
    <div class=\"searchLabel\">$l_after<br />
      <input name=\"tf_dateafter\" size=\"10\" maxlength=\"10\"
        value=\"$dateafter\" />
    </div>
    <div class=\"searchLabel\">$l_before<br />
      <input name=\"tf_datebefore\" size=\"10\" maxlength=\"10\"
        value=\"$datebefore\" />
    </div>
    <div class=\"searchLabel\">$l_kind<br />
      $sel_kind
    </div>
    <div class=\"searchLabel\">$l_category<br />
      $sel_cat
    </div>
    <div class=\"searchLabel\">$l_state<br />
      $sel_state
    </div>
    <div class=\"searchLabel\">$l_manager<br />
      $sel_manager
    </div>
    <div class=\"searchLabel\">$l_show_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive />
    </div>
    <div class=\"searchButton\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"param_parent\" type=\"hidden\" value=\"$parent\" />
      <input name=\"param_company\" type=\"hidden\" value=\"$company_id\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal search form                                           //
// Parameters : 
//   - $deal[]         : default form values
//     keys used       plabel, parchive, pmanager
//   - $obm_q_pmanager : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_search_form($deal, $obm_q_pmanager) {
  global $l_parentdeal, $l_label_start,$l_all,$l_all_f,$l_manager;
  global $l_find,$l_show_archive; 

  // --- Var preparation ------------------------------------------------------

  $plabel = stripslashes($deal["plabel"]);
  $parchive = ($deal["parchive"] == "1" ? "checked" : "");
  $pmanager = $deal["pmanager"];

  // Manager select construction
  if ($obm_q_pmanager->num_rows() > 0) {
    $obm_q_pmanager->seek(0);
  }
  $sel_pmanager = "<select name=\"sel_pmanager\">
     <option value=\"$c_all\">$l_all</option>";
  while($obm_q_pmanager->next_record()) {
    $id = $obm_q_pmanager->f("userobm_id");
    $lname = $obm_q_pmanager->f("userobm_lastname");
    $sel_pmanager .= "<option value=\"$id\"";
    if ($pmanager == $id) $sel_pmanager .= "selected = \"selected\"";
    $sel_pmanager .= ">$lname</option>";
  }
  $sel_pmanager .= "</select>";

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"form_search_parentdeal\"
    action=\"" . url_prepare("deal_index.php") . "\">
  <div class=\"detailHead\">$l_parentdeal</div>
  <div class=\"search\">
    <div class=\"searchLabel\">$l_label_start<br />
      <input type=\"text\" name=\"tf_plabel\" size=\"20\" value=\"$plabel\" />
    </div>
    <div class=\"searchLabel\">$l_manager<br />
      $sel_pmanager
    </div>
    <div class=\"searchLabel\">$l_show_archive<br />
      <input type=\"checkbox\" name=\"cb_parchive\" value=\"1\" $parchive />
    </div>
    <div class=\"searchButton\">&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"parent_search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </div>
    <div class=\"searchEnd\"></div>
  </div>
  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal search result                                            //
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_list($deal) {
  global $l_no_found;
  global $display, $auth, $new_order, $order_dir;

  $obm_q = run_query_search($deal, 0, $new_order, $order_dir);
  $nb_deal = $obm_q->num_rows();
  if ($nb_deal == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $pref_q = run_query_display_pref($auth->auth["uid"], "deal", 0);
    $block = html_deal_search_list($obm_q, $pref_q, $nb_deal, $deal);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Deal Search result                                       //
// Parameters:
//   - $obm_q_deals : list of deals
//   - $pref_q      : fields to display in the list of deals 
//   - $nb_deal     : nb deals found
//   - $deal[]      : deal search criteria
//     keys used    : parent, label, company_name, archive, kind, cat, state,
//                    manager, dateafter, datebefore
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_list($obm_q_deals, $pref_q, $nb_deal, $deal) {
  //-- Labels
  global $l_archive_first,$l_label,$l_company,$l_kind,$l_category,$l_state;
  global $l_date_alarm, $l_archive_update, $l_found;
  global $display, $perms_user,$l_perms_user;

  $parent = $deal["parent"];
  $label = urlencode(stripslashes($deal["label"]));
  $name = urlencode(stripslashes($deal["company_name"]));
  $zip = urlencode(stripslashes($deal["company_zip"]));
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);
  $param_comp = $deal["company"];
  $obm_q_deals->seek($first_row);
  
  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_state=$state&amp;sel_cat=$cat&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent&param_company=$param_comp");

  $dis_deals_list=new OBM_DISPLAY("DATA", $pref_q, "deal");
  $dis_deals_list->data_set = $obm_q_deals;
  $dis_deals_list->data_url = $url;
  $dis_deals_list->data_header = "both";

  // --- HTML Template --------------------------------------------------------

  $display["msg"] .= display_info_msg($nb_deal." ".$l_found);
  $block = $dis_deals_list->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Parse and prepare the layout of a deal comment                            //
// Separe comment in section displayed with different colors
// The secion separator is a line beginning with "nnnn-nn-nn:aa:"
// Parameters :
//   - $com     : comment text to handle
//   - $p_order : Comment order (1 = reverse, else chronological)
//              : if not set, take $set_commentorder pref
///////////////////////////////////////////////////////////////////////////////
function beautify_deal_comment($com, $p_order="") {
  global $cco_chro, $cco_rev, $set_commentorder;

  $col0 = "FFFFFF";
  $col1 = "BBBBBB";

  if ($p_order != "") {
    $order = $p_order;
  } else {
    $order = $set_commentorder;
  }

  // Chronological Order
  if ($order == $cco_chro) {

    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)/U";
    $replace = "</td>
    </tr>
    <tr>
      <td bgcolor=\"#$col1\">\\2</td>
    </tr><tr>
      <td bgcolor=\"#$col0\">";

    $res = preg_replace($pattern, $replace, $com, 1);
    while (preg_match($pattern, $res)) {
      $nb++;
      $res = preg_replace($pattern, $replace, $res, 1);
    }
    
    $result = "
    <table border=\"1\" width=\"100%\">
    <tr>
      <td>$res</td>
    </tr>
    </table>";

  } else {
  // reverse Chronological Order
    // pattern modifiers :
    // U : Ungreedy
    // s : "." matches all chars even newlines
    // D : $ matches only end of text, not end of line
    $pattern = "/(^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)(.*)($|((^|\n)([12][0-9]{3}-[0-1][0-9]-[0-3][0-9]:.*:)))/UsD";
    $replace = "\\4";
    $tmp = $com;
    
    while (preg_match($pattern, $tmp, $matches)) {
      $nb++;
      $who = $matches[2];
      $text = $matches[3];
      $row = "
    <tr>
      <td bgcolor=\"#$col1\">$who</td>
    </tr><tr>
      <td bgcolor=\"#$col0\">$text</td>
    </tr>";
      $rows = $row . $rows;
      $tmp = preg_replace($pattern, $replace, $tmp, 1);
    }

    $result = "
    <table border=\"1\" width=\"100%\">
    $rows
    </table>";
  }

  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Deal links menu                                                  //
// Parameters:
//   - $deal_q : DBO : information about the deal
//   - $inv_nb : invoices number
// Returns:
//   $r : string with HTML code
///////////////////////////////////////////////////////////////////////////////
function html_deal_links($deal_q, $inv_nb) {
  // -- Themes
  global $set_theme, $ico_pdeal, $ico_deal, $ico_contract;
  global $ico_contract_new, $ico_invoice, $ico_invoice_new, $ico_project;
  // -- Labels
  global $l_parentdeal, $l_affect_deal, $l_header_contract, $l_contract_new;
  global $l_create_project, $l_view_project;
  global $l_header_document,$ico_document,$l_deal, $l_project;
  global $l_header_invoice, $l_archived_invoices, $l_invoice_new;
  global $l_document_new,$l_document_add,$ico_document_new;
  global $perms_user, $path, $auth,$popup_height,$popup_width;

  $id = $deal_q->f("deal_id");
  $pid = $deal_q->f("deal_parentdeal_id");
  $cid = $deal_q->f("deal_company_id");
  $label = $deal_q->f("deal_label");
  $con1 = $deal_q->f("deal_contact1_id");
  $con2 = $deal_q->f("deal_contact2_id");
  $hitrate = $deal_q->f("deal_hitrate");
  $project = $deal_q->f("deal_project_status");
  $market = $deal_q->f("deal_marketingmanager_id");
  $tech = $deal_q->f("deal_technicalmanager_id");

  // parent deal
  if ($pid > 0) {
    $plabel = get_parent_label($pid);
    $url_pdeal = url_prepare("$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=$pid");
    $dis_pdeal = "
    <li><a href=\"$url_pdeal\"><img src=\"/images/$set_theme/$ico_pdeal\" /></a>
        <a href=\"$url_pdeal\">$l_parentdeal ($plabel)</a></li>";
  }
  $url_pdeal_upd = url_prepare("$path/deal/deal_index.php?action=affect&amp;param_parent=$pid&amp;param_deal=$id");

  // project
  if ($project or ($hitrate == "100")) {
    if ($project) {
      $l_text_project = $l_view_project;
      $url_project = url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=$id");
    } else {
      $l_text_project = $l_create_project;
      $url_project = url_prepare("$path/project/project_index.php?action=init&amp;param_project=$id");
    }
    
    $dis_project_link ="
     <div class=\"linkTitle\">:: $l_project</div>
     <ul class=\"linkList\">
      <li><a href=\"$url_project\"><img src=\"/images/$set_theme/$ico_project\" /></a>
          <a href=\"$url_project\">$l_text_project</a></li>
     </ul>";
  }

  // contract
  $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;param_deal=$id");
  $url_contract_new = url_prepare("$path/contract/contract_index.php?action=new&amp;param_deal=$id&amp;param_company=$cid&amp;deal_label=$label&amp;sel_con1=$con1&amp;sel_con2=$con2&amp;sel_market=$market&amp;sel_tech=$tech");

  // invoice
  $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_deal=$id");
  $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;param_deal=$id");
  $nb_inv_arch = run_query_nb_archived_invoices ($id);
  $contract_nb = run_query_contract_nb ($id);

  // document
  $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=deal");
  $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=deal");
  $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_title=".urlencode($l_add_document)."&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/deal/deal_index.php")."&amp;ext_id=$id&amp;ext_target=$l_deal";
  $nb_document = run_query_document_nb ($id,"Deal");

  $block = "<div class=\"link\">
  <div class=\"linkTitle\">:: $l_parentdeal</div>
  <ul class=\"linkList\">
    $dis_pdeal
    <li><a href=\"$url_pdeal_upd\"><img src=\"/images/$set_theme/$ico_deal\" /></a>
        <a href=\"$url_pdeal_upd\">$l_affect_deal</a></li>
  </ul>
  $dis_project_link
  <div class=\"linkTitle\">:: $l_header_contract</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_contract\"><img src=\"/images/$set_theme/$ico_contract\" /></a>
        <a href=\"$url_contract\">$l_header_contract ($contract_nb)</a></li>
    <li><a href=\"$url_contract_new\"><img src=\"/images/$set_theme/$ico_contract_new\" /></a>
        <a href=\"$url_contract_new\">$l_contract_new</a></li>
  </ul>
  <div class=\"linkTitle\">:: $l_header_invoice</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_inv\"><img src=\"/images/$set_theme/$ico_invoice\" /></a>
        <a href=\"$url_inv\">$l_header_invoice ($inv_nb)</a></li>
    <li><a href=\"$url_inv\"><img src=\"/images/$set_theme/$ico_invoice\" /></a>
        <a href=\"$url_inv\">$l_archived_invoices ($nb_inv_arch)</a></li>
    <li><a href=\"$url_inv_new\"><img src=\"/images/$set_theme/$ico_invoice_new\" /></a>
        <a href=\"$url_inv_new\">$l_invoice_new</a></li>
  </ul>
  <div class=\"linkTitle\">:: $l_header_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"/images/$set_theme/$ico_document\" /></a>
        <a href=\"$url_doc\">$l_header_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_document_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"/images/$set_theme/$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_deal'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>
       
 </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Contact Consult                                                  //
// Parameters :
//   - $deal_q  : DBO : information about the deal
//   - $con_q   : DBO : list of contact from the company 
//   - $comp_id : id of the company concerned by the deal
///////////////////////////////////////////////////////////////////////////////
function html_deal_consult($deal_q,$con_q,$comp_id, $q_invoices, $invoices_options) {
  // -- Themes
  global $set_theme,$ico_company, $ico_contact;
  // - Labels
  global $l_update,$l_delete,$l_insert,$l_infos;
  global $l_company, $l_label,$l_date_init,$l_kind,$l_category,$l_marketing_manager,$l_technical_manager,$l_proposition;
  global $l_amount,$l_hitrate,$l_contact,$l_date_alarm,$l_state,$l_last_update,$l_archive,$l_comment,$l_at,$l_number;
  global $l_visibility,$l_public,$l_private,$l_parentdeal,$l_todo, $l_deal;
  global $l_invoices, $l_no_invoice, $l_see_archived_invoices, $incl_arch;
  global $display, $path, $auth, $c_yes, $c_no;
  global $perms_user, $perms_editor, $perms_admin;

  $id = $deal_q->f("deal_id");
  $pid = $deal_q->f("deal_parentdeal_id");
  $usercreate = $deal_q->f("deal_usercreate");
  $label = $deal_q->f("deal_label");
  $num = $deal_q->f("deal_number");
  $datebegin = date_format($deal_q->f("datebegin"));
  $kind = $deal_q->f("dealtype_label");
  $cat = $deal_q->f("tasktype_label");
  $lvis = ($deal_q->f("deal_visibility") == 0 ? $l_public : $l_private);
  $market = $deal_q->f("lname1") . " " . $deal_q->f("fname1");
  $tech = $deal_q->f("lname2") . " " . $deal_q->f("fname2");
  $datepro = ($deal_q->f("deal_dateproposal") != "0000-00-00" ? $deal_q->f("deal_dateproposal") : "");
  $amount = $deal_q->f("deal_amount");
  $hitrate = $deal_q->f("deal_hitrate");
  $datealarm = date_format($deal_q->f("datealarm"));
  $status = $deal_q->f("dealstatus_label");
  $dateu = date_format($deal_q->f("datemodif"));
  $timeu = date("H:i:s",$deal_q->f("datemodif"));
  $archive = ($deal_q->f("deal_archive") == 1 ? $c_yes : $c_no);
  $todo = $deal_q->f("deal_todo");
  $com = beautify_deal_comment(nl2br($deal_q->f("deal_comment")));
  $comp = $deal_q->f("company_name");
  $ad1 = $deal_q->f("company_address1");
  $zip = $deal_q->f("company_zipcode");
  $town = $deal_q->f("company_town");
  $comp_phone = $deal_q->f("company_phone");
  $comp_phone = (($comp_phone != "") ? "($comp_phone)" : "");
  $con1 = $deal_q->f("deal_contact1_id");
  $dis_con1 = $deal_q->f("lnamec1") . " " . $deal_q->f("fnamec1");
  $phone_con1 = ($deal_q->f("phonec1") ? $deal_q->f("phonec1") : $comp_phone);
  $con2 = $deal_q->f("deal_contact2_id");
  $dis_con2 = $deal_q->f("lnamec2") . " " . $deal_q->f("fnamec2");
  $phone_con2 = ($deal_q->f("phonec2") ? $deal_q->f("phonec2") : $comp_phone);

  if ($usercreate == $auth->auth["uid"]) {   
   $visibility = "<tr><td class=\"detailLabel\">$l_visibility</td>
                 <td class=\"detailText\">$lvis</td>";
  }

  $inv_nb = $q_invoices->num_rows ();
  $display["link"] = html_deal_links($deal_q, $inv_nb);

  $block = "
    <table width=\"100%\">
    <tr>
      <td valign=\"top\">

    <div class=\"detailHead\">$l_deal</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_label</td>
        <td class=\"detailText\">$label</td>
        $visibility
      </tr><tr>
        <td class=\"detailLabel\">$l_number</td>
        <td class=\"detailText\">$num</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_date_init</td>
        <td class=\"detailText\">$datebegin</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_kind</td>
        <td class=\"detailText\">$kind</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_category</td>
        <td class=\"detailText\">$cat</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_marketing_manager</td>
        <td class=\"detailText\">$market</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_technical_manager</td>
        <td class=\"detailText\">$tech</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_proposition date</td>
        <td class=\"detailText\">$datepro</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_amount</td>
        <td class=\"detailText\">$amount</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_hitrate</td>
        <td class=\"detailText\">$hitrate %</td>
      </tr>
      </table>
      </td>
      <td>

    <div class=\"detailHead\">$l_company</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$comp
        <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
        <img src=\"/images/$set_theme/$ico_company\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$ad1<br/>$zip $town</td>
      </tr><tr>
        <td class=\"detailLabel\">$dis_con1
        <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1") . "\">
        <img src=\"/images/$set_theme/$ico_contact\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$phone_con1
        </td>
      </tr><tr>
        <td class=\"detailLabel\">$dis_con2
        <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2") . "\">
        <img src=\"/images/$set_theme/$ico_contact\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$phone_con2
        </td>
      </tr>
      </table>

    <div class=\"detailHead\">$l_infos</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_date_alarm</td>
        <td class=\"detailText\">$datealarm</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_state</td>
        <td class=\"detailText\">$status</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_last_update</td>
        <td class=\"detailText\">$dateu $l_at $timeu</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_archive</td>
        <td class=\"detailText\">$archive</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_todo</td>
        <td class=\"detailText\">$todo</td>
      </tr>
      </table>

      </td>
    </tr><tr>
      <td colspan=\"2\">

    <div class=\"detailHead\">$l_comment</div>

      <table class=\"detail\">
      <tr>
        <td colspan=\"2\" class=\"detailText\">$com</td>
      </tr>
      </table>

      </td>
    </tr>
    </table>";

  // we display invoices list
  if ($inv_nb == 0) {
    $display["msg"] .= display_info_msg ($l_no_invoice);
  } else {
    $display["msg"] .= display_info_msg ($inv_nb." ".$l_invoice);

    $url = url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$id&amp;incl_arch=$incl_arch");
    
    $dis_invoice_list = new OBM_DISPLAY("DATA", $invoices_options, "deal");
    $dis_invoice_list->display_entity = "invoice";
    $dis_invoice_list->data_set = $q_invoices;
    $dis_invoice_list->data_header = "top";
    $dis_invoice_list->data_url = $url;

    $block .= $dis_invoice_list->display("dis_data_deal");
    // and a button to allow displaying archived invoices :
    $url = url_prepare ("deal_index.php?action=detailconsult".
		       "&amp;param_deal=$id".
		       "&amp;incl_arch=1");
    $block .= "
       <form method=\"post\" action=\"$url\">
        <table class=\"detail\">
	 <tr><td class=\"detailHead\">
	  <input type=\"submit\" value=\"$l_see_archived_invoices\" />
         </td></tr>
        </table>
       </form>
    ";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Form                                                         //
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $kind_q    : DBO : deal kind list
//   - $cat_q     : DBO : deal category list
//   - $usr_q     : DBO : list of userobm
//   - $comp_q    : DBO : deal company infos (name, ad1, zip, town when new)
//   - $con_q     : DBO : list of contact from the company 
//   - $sta_q     : DBO : list of deal status
//   - $comp_id   : id of the company concerned by the deal
//   - deal[]     : default or transmitted values
//     keys used  : id, usercreate, num, label
///////////////////////////////////////////////////////////////////////////////
function html_deal_form($action,$deal_q,$kind_q,$cat_q,$usr_q,$comp_q,$con_q,$sta_q,$comp_id,$deal) {
  global $set_theme,$ico_company,$ico_contact,$l_company;
  global $c_php_isodate_format, $l_update,$l_delete,$l_insert, $l_none;
  global $l_deal, $l_infos, $l_company,$l_label,$l_number,$l_date_init,$l_kind;
  global $l_category,$l_marketing_manager,$l_technical_manager,$l_private;
  global $l_proposition,$l_amount,$l_hitrate,$l_contact,$l_date_alarm,$l_state;
  global $l_last_update,$l_archive,$l_comment,$l_add_comment,$l_upd_comment;
  global $l_at,$l_todo;
  global $path, $action, $auth,$c_all,$l_all;

  $uid = $auth->auth["uid"];

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $deal_q->f("deal_id");
    $pid = $deal_q->f("deal_parentdeal_id");
    $usercreate = $deal_q->f("deal_usercreate");
    $label = $deal_q->f("deal_label");
    $num = $deal_q->f("deal_number");
    $datebegin = isodate_format($deal_q->f("datebegin"));
    $kind = $deal_q->f("deal_type_id");
    $cat = $deal_q->f("deal_tasktype_id");
    $vis = ($deal_q->f("deal_visibility") == 0 ? "0" : "1");
    $market = $deal_q->f("deal_marketingmanager_id");
    $tech = $deal_q->f("deal_technicalmanager_id");
    $dateprop = ($deal_q->f("deal_dateproposal") != "0000-00-00" ? $deal_q->f("deal_dateproposal") : "");
    $amount = $deal_q->f("deal_amount");
    $hitrate = $deal_q->f("deal_hitrate");
    $con1 = $deal_q->f("deal_contact1_id");
    $con2 = $deal_q->f("deal_contact2_id");
    $datealarm = isodate_format($deal_q->f("datealarm"));
    $status = $deal_q->f("deal_status_id");
    $archive = ($deal_q->f("deal_archive") == 1 ? " checked" : "");
    $todo = $deal_q->f("deal_todo");
    $comment = $deal_q->f("deal_comment");
    $comp = $deal_q->f("company_name");
    $ad1 = $deal_q->f("company_address1");
    $zip = $deal_q->f("company_zipcode");
    $town = $deal_q->f("company_town");
    $usercomment = $uid;
    $datecomment = isodate_format();
  } elseif ($action == "new") {
    $comp = $comp_q->f("company_name");
    $ad1 = $comp_q->f("company_address1");
    $zip = $comp_q->f("company_zipcode");
    $town = $comp_q->f("company_town");
    $market = $comp_q->f("company_marketingmanager_id");
    $usercomment = $uid;
    $datecomment = isodate_format();
    // We pre-fill datebegin and datealarm with current date
    $datebegin = date($c_php_isodate_format); 
    $datealarm = $datebegin;
  }

  // If parameters have been given, they supercede the default action value
  if (isset($deal["id"])) { $id = $deal["id"]; }
  if (isset($deal["parent"])) { $pid = $deal["parent"]; }
  if (isset($deal["usercreate"])) { $usercreate = $deal["usercreate"]; }
  if (isset($deal["num"])) { $num = stripslashes($deal["num"]); }
  if (isset($deal["label"])) { $label = stripslashes($deal["label"]); }
  if (isset($deal["vis"])) { $vis = $deal["vis"]; }
  if (isset($deal["datebegin"])) { $datebegin = stripslashes($deal["datebegin"]); }
  if (isset($deal["kind"])) { $kind = $deal["kind"]; }
  if (isset($deal["cat"])) { $cat = $deal["cat"]; }
  if (isset($deal["market"])) { $market = $deal["market"]; }
  if (isset($deal["tech"])) { $tech = $deal["tech"]; }
  if (isset($deal["dateprop"])) { $dateprop = stripslashes($deal["dateprop"]); }
  if (isset($deal["amount"])) { $amount = stripslashes($deal["amount"]); }
  if (isset($deal["hitrate"])) { $hitrate = stripslashes($deal["hitrate"]); }
  if (isset($deal["contact1"])) { $con1 = $deal["contact1"]; }
  if (isset($deal["contact2"])) { $con2 = $deal["contact2"]; }
  if (isset($deal["datealarm"])) { $datealarm = stripslashes($deal["datealarm"]); }
  if (isset($deal["state"])) { $status = $deal["state"]; }
  if (isset($deal["timeupdate"])) { $timeupdate = $deal["timeupdate"]; }
  if (isset($deal["archive"])) { $archive = ($deal["archive"] == 1 ? "checked" : ""); }
  if (isset($deal["todo"])) { $todo = stripslashes($deal["todo"]); }
  if (isset($deal["com"])) { $comment = stripslashes($deal["com"]); }
  if (isset($deal["add_comment"])) { $add_comment = stripslashes($deal["add_comment"]); }
  if (isset($deal["usercomment"])) { $usercomment = $deal["usercomment"]; }
  if (isset($deal["datecomment"])) { $datecomment = $deal["datecomment"]; }
  if (isset($deal["company_name"])) { $comp = stripslashes($deal["company_name"]); }
  if (isset($deal["company_ad1"])) { $ad1 = stripslashes($deal["company_ad1"]); }
  if (isset($deal["company_zip"])) { $zip = stripslashes($deal["company_zip"]); }
  if (isset($deal["company_town"])) { $town = stripslashes($deal["company_town"]); }

  // Kind select construction
  $sel_kind = "<select name=\"sel_kind\">
    <option value=\"0\">$l_none</option>";
  while ($kind_q->next_record()) {
    $kid = $kind_q->f("dealtype_id");
    $klabel = $kind_q->f("dealtype_label");
    $sel_kind .= "\n<option value=\"$kid\"";
    if ($kind == $kid) $sel_kind .= " selected = \"selected\""; 
    $sel_kind .= ">$klabel</option>";
  }
  $sel_kind .= "</select>";

  // Category select construction
  $sel_cat = "<select name=\"sel_cat\">
    <option value=\"0\">$l_none</option>";
  while($cat_q->next_record()) {
    $cid = $cat_q->f("tasktype_id");
    $tt_label = $cat_q->f("tasktype_label");
    $sel_cat .= "\n<option value=\"$cid\"";
    if ($cat == $cid) $sel_cat .= "selected = \"selected\""; 
    $sel_cat .= ">$tt_label</option>";
  }
  $sel_cat .= "</select>";
 
  // Marketing manager select construction
  $sel_market = "<select name=\"sel_market\">
    <option value=\"0\">$l_none</option>";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname") . " " .
             $usr_q->f("userobm_firstname");
    $sel_market .= "\n<option value=\"$cid\"";
    if ($market == $cid) $sel_market .= " selected = \"selected\""; 
    $sel_market .= ">$cname</option>";
  }
  $sel_market .= "</select>";

  // Technical manager select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_tech = "<select name=\"sel_tech\">
    <option value=\"0\">$l_none</option>";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname") . " " .
             $usr_q->f("userobm_firstname");
    $sel_tech .= "<option value=\"$cid\"";
    if ($tech == $cid) $sel_tech .= " selected = \"selected\""; 
    $sel_tech .= ">$cname</option>\n";
  }
  $sel_tech .= "</select>";

  // contact 1 select construction
  $sel_con1 = "<select name=\"sel_contact1\">";
  while ($con_q->next_record()) {
    $cid = $con_q->f("contact_id");
    $cinfo = $con_q->f("contact_lastname") . " " .
             $con_q->f("contact_firstname") . " - " .
             $con_q->f("contact_phone");
    $sel_con1 .= "<option value=\"$cid\"";
    if ($con1 == $cid) $sel_con1 .= "  selected = \"selected\""; 
    $sel_con1 .= ">$cinfo</option>\n";
  }
  $sel_con1 .= "</select>";

  // contact 2 select construction
  if ($con_q->nf()>0) {
    $con_q->seek(0);
  }
  $sel_con2 = "<select name=\"sel_contact2\">";
  while ($con_q->next_record()) {
    $cid = $con_q->f("contact_id");
    $cinfo = $con_q->f("contact_lastname") . " " .
             $con_q->f("contact_firstname") . " - " .
             $con_q->f("contact_phone");
    $sel_con2 .= "<option value=\"$cid\"";
    if ($con2 == $cid) $sel_con2 .= " selected = \"selected\""; 
    $sel_con2 .= ">$cinfo</option>\n";
  }
  $sel_con2 .= "</select>";

  // state select construction
  $sel_state = "<select name=\"sel_state\" onChange=\"auto_set_hitrate(this, this.form.tf_hitrate,this.selectedIndex);\">";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $slabel = $sta_q->f("dealstatus_label");
    $sel_state .= "<option value=\"$sid\"";
    if ($status == $sid) $sel_state .= " selected = \"selected\""; 
    $sel_state .= ">$slabel</option>\n";
  }
  $sel_state .= "</select>";

  $js_state = "
<script type=\"text/javascript\">
<!--

function auto_set_hitrate(select, targetField, pos) {
";

  $sta_q->seek(0);
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $shitrate = $sta_q->f("dealstatus_hitrate");
    if (($shitrate != "") && (($shitrate >= 0) && ($shitrate <= 100))) {
      $js_state .= "
        if (select.options[pos].value == $sid) {
          targetField.value = $shitrate;
        }";
    }
  }

  $js_state .= "  return true;
}
-->
</script>
";
  
  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cid\"";
    if ($usercomment == $cid) $sel_usercomment .= " selected = \"selected\""; 
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

  if ( ($action=="new") ||
       ( (($action=="detailupdate") || ($action=="update")) && 
         ($usercreate == $uid) ) ) {
    $dis_visibility = "
             <td class=\"detailLabel\">$l_private</td>
             <td class=\"detailForm\"><input name=\"cb_vis\" type=\"checkbox\" value=\"1\" ";
    if ($vis==1) $dis_visibility .= "checked = \"checked\"";
    $dis_visibility .= " /></td></tr><tr>";
  }

  if ($action == "detailupdate") {
    $dis_contact_2 = "<a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2")."\">
             <img src=\"/images/$set_theme/$ico_contact\" alt=\"[details]\" />
             </a>";
    $dis_contact_1 = "<a href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1")."\">
             <img src=\"/images/$set_theme/$ico_contact\" alt=\"[details]\" />
             </a>";
  }
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button1 = "<input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
        <input type=\"hidden\" name=\"param_parent\" value=\"$pid\" />
        <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp\" />
        <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
        <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
        <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
        <input type=\"hidden\" name=\"action\" value=\"update\" />
        <input type=\"hidden\" name=\"param_deal\" value=\"$id\" />
        <input type=\"submit\" value=\"$l_update\" />
      ";
  } else {
    $dis_button1 = "<input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"hidden\" name=\"param_parent\" value=\"$pid\" />
      <input type=\"hidden\" name=\"tf_company_name\" value=\"$comp\" />
      <input type=\"hidden\" name=\"hd_company_ad1\" value=\"$ad1\" />
      <input type=\"hidden\" name=\"hd_company_zip\" value=\"$zip\" />
      <input type=\"hidden\" name=\"hd_company_town\" value=\"$town\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $block = "
    $js_state
    <form method=\"post\" name=\"form_deal_update\" 
      onsubmit=\"if (check_form(this)) return true; else return false;\"
      action=\"".url_prepare("deal_index.php")."\">

  <div class=\"detailHead\">$l_company</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$comp&nbsp;
        <a href=\"" . url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id") . "\">
        <img src=\"/images/$set_theme/$ico_company\" alt=\"[details]\" /></a></td>
      <td class=\"detailText\">
        $ad1
        <br />$zip $town 
      </td>
    </tr><tr>
      <td class=\"detailLabel\">$l_contact 1 $dis_contact_1</td>
      <td class=\"detailForm\">$sel_con1</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_contact 2 $dis_contact_2</td>
      <td class=\"detailForm\">$sel_con2</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_deal</div>

    <table class=\"detail\">
    <tr>
      $dis_visibility
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailForm\"><input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" /></td>
      </tr><tr>
        <td class=\"detailLabel\">$l_number</td>
        <td class=\"detailForm\"><input type=\"text\" name=\"tf_num\" size=\"40\" maxlength=\"32\" value=\"$num\" /></td>
      </tr><tr>
        <td class=\"detailLabel\">$l_date_init</td>
        <td class=\"detailForm\"><input name=\"tf_datebegin\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datebegin\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailForm\">$sel_kind</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_category</td>
      <td class=\"detailForm\">$sel_cat</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_marketing_manager</td>
      <td class=\"detailForm\">$sel_market</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_technical_manager</td>
      <td class=\"detailForm\">$sel_tech</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_proposition date</td>
      <td class=\"detailForm\"><input name=\"tf_dateprop\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$dateprop\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_proposition $l_amount</td>
      <td class=\"detailForm\"><input name=\"tf_amount\" type=\"text\" size=\"10\" value=\"$amount\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_hitrate</td>
      <td class=\"detailForm\"><input name=\"tf_hitrate\" type=\"text\" size=\"3\" value=\"$hitrate\" /> %</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_infos</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_date_alarm</td>
      <td class=\"detailForm\"><input name=\"tf_datealarm\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datealarm\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_state</td>
      <td class=\"detailForm\">$sel_state</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_todo</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_todo\" size=\"60\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\"><input name=\"tf_datecomment\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment\" />$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
      $dis_comment
    </tr>
    </table>

    <div class=\"detailButton\">
      <p class=\"detailButtons\">$dis_button1</p>
    </div>
    </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays and check the deal links (and delete form if ok)                 //
// Parameters:
//   - $p_id : deal id
///////////////////////////////////////////////////////////////////////////////
function dis_check_links($p_id) {
  global $display, $path, $l_can_delete, $l_cant_delete, $l_back;
  global $l_link_contract, $l_link_contract_no;

  $delete_ok = true;

  // Links from Contract
  $obm_q = run_query_deal_contract_links($p_id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $display_linkc ="<tr><td class=\"detailHead\">$l_link_contract</td></tr>";
    while ($obm_q->next_record()) {
      $cid = $obm_q->f("contract_id");
      $clabel = $obm_q->f("contract_label");
      $display_linkc .= "
    <tr>
      <td class=\"detailLabel\">
        <a class=\"detail\" href=\"" . url_prepare("$path/contract/contract_index.php?action=detailconsult&amp;param_contract=$cid") . "\">$clabel</a>
      </td>
    </tr>";
    }
  } else {
    $display_linkc = "
    <tr>
      <td class=\"detailHead\">$l_link_contract_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
   $display_linkc
   </table>";

  $dis_back = "<form name=\"form_back\" method=\"post\"
    action=\"".url_prepare("deal_index.php") ."\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_deal\" value=\"$p_id\" />
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
      action=\"" . url_prepare("deal_index.php") . "\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_deal\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
    </form>";

  return $ret;
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
	$sel_parent .= "<option value=\"".$parent_q->f("parentdeal_id")."\">".$parent_q->f("parentdeal_label")."</option>";
    }
    $sel_parent .= "</select>";

    $display["msg"] .= display_info_msg($l_deal_select_parentdeal);
    $block = "
      <form name=\"form_affect_deal\" method=\"get\"
        action=\"" . url_prepare("deal_index.php") . "\">

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">
        $sel_parent
        <input type=\"hidden\" name=\"action\" value=\"affect_update\" />
        <input type=\"hidden\" name=\"param_deal\" value=\"$p_deal_id\" />
        </td>
      </tr>
      </table>

      <div class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" name=\"sub_affect_deal\" value=\"$l_affect\" />
        </p>
      </div>
      </form>";

    return $block;
}
         
	  
///////////////////////////////////////////////////////////////////////////////
// Deal Administration Forms
// Parametres:
//   - $kind_q   : DBO : Deal kind list
//   - $cat_q    : DBO : Deal TaskType list
//   - $status_q : DBO : Deal Status list
///////////////////////////////////////////////////////////////////////////////
function html_deal_admin_form($kind_q, $cat_q, $status_q) {
   // Labels : 
  global $l_kind_manage,$l_kind_new,$l_kind_exist;
  global $l_status_manage,$l_status_exist,$l_status_new;
  global $l_category_manage,$l_category_exist,$l_category_new;
  global $l_category_label,$l_category_minilabel,$l_label,$l_minus,$l_plus,$l_order;
  // Actions : 
  global $l_kind_checkdelete, $l_kind_update, $l_kind_insert;
  global $l_cat_checkdelete,$l_cat_update,$l_cat_insert;
  global $l_status_checkdelete,$l_status_update,$l_status_insert;
  global $l_hitrate, $l_task_fac, $l_task_int, $l_task_div;

  $task_list = array($l_task_fac, $l_task_int, $l_task_div);

  // Kind Select construction
  $sel_kind = "<select name=\"sel_kind\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf(' ');
    forms.form_kind_update.tf_kind.value=mytext.substr(pos+1);
    pos = 0;
    sign = mytext.substr(0,1);
    if (sign == '+') { pos = 1; }
    forms.form_kind_update.rd_kind_inout[pos].checked=true;\"
    size=\"4\">";
  while ($kind_q->next_record()) {
    $id = $kind_q->f("dealtype_id");
    $inout = $kind_q->f("dealtype_inout");
    $label = $kind_q->f("dealtype_label");
    $sel_kind .= "<option value=\"$id\">$inout $label</option>\n";
  }
  $sel_kind .= "</select>";

  // status select construction
  $sel_sta = "<select name=\"sel_state\" onChange=\"
    mytext = this.options[this.selectedIndex].text;
    pos = mytext.indexOf('(');
    hit = mytext.substr(pos+1);
    pos2 = hit.indexOf(')');
    hit = hit.substr(0,pos2);
    mytext = mytext.substr(0, pos);
    pos = mytext.indexOf('-');
    forms.form_status_update.tf_status.value=mytext.substr(pos+1);
    forms.form_status_update.tf_order.value=mytext.substr(0,pos);
    forms.form_status_update.tf_hitrate.value=hit;\"
    size=\"4\">";
  while ($status_q->next_record()) {
    $id = $status_q->f("dealstatus_id");
    $order = $status_q->f("dealstatus_order");
    $label = $status_q->f("dealstatus_label");
    $hitrate = $status_q->f("dealstatus_hitrate");
    $sel_sta .= "<option value=\"$id\">$order-$label ($hitrate)</option>\n";
  }
  $sel_sta .= "</select>";

  // category select construction
  $sel_cat = "<select name=\"sel_cat\" onChange=\"
    forms.form_cat_update.tf_cat.value=this.options[this.selectedIndex].text;\"
    size=\"4\" >";
  while ($cat_q->next_record()) {
    //put menu titles
    if (($cat_i = $cat_q->f("tasktype_internal")) != $type_prec) {
      $sel_cat .= "<option disabled=\"disabled\">".$task_list[$cat_i]." </option>";
      $type_prec = $cat_i;
    }
    $id = $cat_q->f("tasktype_id");
    $mlabel = $cat_q->f("tasktype_label");
    $sel_cat .= "<option value=\"$id\">&nbsp;&nbsp;$mlabel</option>\n";
  }
  $sel_cat .= "</select>";

  $block = "
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_kind_manage</td>
    </tr>
    <tr>
      <td>
        <table>
        <tr>
         <td colspan=\"3\">
    <form name=\"form_kind_delete\"
      method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">

<!-- kind delete section -->

       <table>
        <tr>
          <td class=\"adminLabel\">$l_kind_exist</td>
          <td class=\"adminLabel\">$sel_kind</td>
          <td>
            <input type=\"hidden\" name=\"action\" value=\"kind_checklink\" />
            <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_checkdelete\"
              onclick=\"if (check_kind_checkdel(this.form)) return true;
              else return false;\" />
          </td>
        </tr>
        </table>
    </form>
          </td>
        </tr>

<!-- kind update section -->

        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
    <form name=\"form_kind_update\"
      method=\"get\" action=\"" . url_prepare("deal_index.php") . "\">
       <table>
         <tr>
          <td>
            <input type=\"hidden\" name=\"sel_kind\" value=\"\" />
            <input type=\"hidden\" name=\"action\" value=\"kind_update\" />
            <input type=\"text\" name=\"tf_kind\" />
          </td>
           <td rowspan=\"2\">
            <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_update\"
              onclick=\"if (check_kind_upd(this.form,document.form_kind_delete)) return true; else return false;\" />
          </td>
        </tr>
        <tr>
          <td>
            <input type=\"radio\" name=\"rd_kind_inout\" value=\"-\" checked=\"checked\" />$l_minus
            <input type=\"radio\" name=\"rd_kind_inout\" value=\"+\" />$l_plus
          </td>
        </tr>   
       </table>
     </form>
      </td>
          <td>&nbsp;</td>
      </tr>
     </table>
      </td>

<!-- New Kind Section -->

      <td>
    <form name=\"form_kind_new\"
      method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_kind_new : <input name=\"tf_kind\" /></td>
        <td>
          <input type=\"radio\" name=\"rd_kind_inout\" value=\"-\" checked=\"checked\" />$l_minus<br />
          <input type=\"radio\" name=\"rd_kind_inout\" value=\"+\" />$l_plus
        </td>
      </tr><tr>
        <td class=\"adminLabel\">
          <input type=\"hidden\" name=\"action\" value=\"kind_insert\" />
          <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_insert\"
            onclick=\"if (check_kind_new(this.form)) return true; else return false;\" />
        </td>
      </tr>
      </table>
    </form>
      </td>
    </tr>
    </table>

<!-- status section -->

    <hr />

    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_status_manage</td>
    </tr>
    <tr>
      <td>

<!-- status delete section -->

        <table>
        <tr>
         <td colspan=\"3\">
          <form name=\"form_status_delete\"
          method=\"post\" action=\"" . url_prepare("deal_index.php")."\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_status_exist</td>
        <td class=\"adminLabel\">$sel_sta</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"status_checklink\" />
          <input type=\"submit\" name=\"sub_status\" value=\"$l_status_checkdelete\"
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
      <form name=\"form_status_update\"
        method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
          <input type=\"hidden\" name=\"sel_state\" value=\"\" />
          <input type=\"hidden\" name=\"action\" value=\"status_update\" />
          <table>
          <tr>
            <td class=\"adminLabel\">$l_label : </td>
            <td><input type=\"text\" name=\"tf_status\" /></td>
            <td rowspan=\"3\">
              <input type=\"submit\" name=\"sub_status\" value=\"$l_status_update\"
              onclick=\"if (check_status_upd(this.form, document.form_status_delete)) return true; else return false;\" />
            </td>
          </tr>
          <tr>
            <td class=\"adminLabel\">$l_order : </td>
            <td>
	      <input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" />
	    </td>
          </tr>
          <tr>
            <td class=\"adminLabel\">$l_hitrate : </td>
            <td>
	      <input type=\"text\" name=\"tf_hitrate\" size=\"3\" maxlength=\"3\" />
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
        method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
      <table>
      <tr>
        <td class=\"adminLabel\">$l_status_new : </td>
          <td><input type=\"text\" name=\"tf_status\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_order : </td>
          <td><input type=\"text\" name=\"tf_order\" size=\"3\" maxlength=\"2\" /></td>
        </tr>
        <tr>
          <td class=\"adminLabel\">$l_hitrate : </td>
          <td><input type=\"text\" name=\"tf_hitrate\" size=\"3\" maxlength=\"3\" /></td>
        </tr>
        <tr>
          <td colspan=\"2\" class=\"adminLabel\">
          <input type=\"hidden\" name=\"action\" value=\"status_insert\" />
          <input type=\"submit\" name=\"sub_status\" value=\"$l_status_insert\"
            onclick=\"if (check_status_new(this.form)) return true; " .
            "else return false;\" />
        </td>
      </tr>
     </table>
    </form>
    </td>
    </tr>
    </table>

<!--  category section  -->

    <hr />
    <table class=\"admin\">
    <tr>
      <td colspan=\"5\" class=\"adminHead\">$l_category_manage</td>
    </tr>
    <tr>
      <td>

<!-- category delete section -->

      <table>
        <tr>
         <td colspan=\"3\">
         <form name=\"form_category_delete\"
          method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
       <table>
      <tr>
        <td class=\"adminLabel\">$l_category_exist</td>
        <td class=\"adminLabel\">$sel_cat</td>
        <td>
          <input type=\"hidden\" name=\"action\" value=\"cat_checklink\" />
          <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_checkdelete\" 
          onclick=\"if (check_cat_checkdel(this.form)) return true; else return false;\" />
          </td>
        </tr>
        </table>
         </form>
        </td>
      </tr>

<!-- category update section -->

      <tr>
        <td colspan=\"3\">
          <form name=\"form_cat_update\"
          method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
          <input type=\"hidden\" name=\"sel_cat\" value=\"\" />
          <input type=\"hidden\" name=\"action\" value=\"cat_update\" />
	  <table>
          <tr>
            <td class=\"adminLabel\">$l_category_label : </td>
            <td>
              <input type=\"text\" name=\"tf_cat\" />
            </td>
           <td>
           <input type=\"submit\" name=\"sub_category\" value=\"$l_cat_update\"
           onclick=\"if (check_category_upd(this.form, document.form_category_delete)) return true; else return false;\" />
           </td>
          </tr>
         </table>
         </form>
        </td>
          <td>&nbsp;</td>
       </tr>
      </table>      
      </td>
      <td colspan=\"2\">

<!-- new category section -->

      <form name=\"form_category_new\"
        method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
      <table>
        <tr>
          <td class=\"adminLabel\">$l_category_new : </td>
          <td>
	    <input type=\"text\" name=\"tf_cat\" />
          </td>
        </tr>
      <tr>
        <td colspan=\"2\">
          <input type=\"radio\" name=\"rd_cat_internal\" value=\"0\" checked=\"checked\" />$l_task_fac<br />
          <input type=\"radio\" name=\"rd_cat_internal\" value=\"1\" />$l_task_int<br />
          <input type=\"radio\" name=\"rd_cat_internal\" value=\"2\" />$l_task_div
        </td>
      </tr>
      <tr>
        <td colspan=\"2\" class=\"adminLabel\">
          <input type=\"hidden\" name=\"action\" value=\"cat_insert\" />
          <input type=\"submit\" name=\"sub_category\" value=\"$l_cat_insert\"
            onclick=\"if (check_category_new(this.form)) return true; " .
            "else return false;\" />
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
// Displays the kind links                                                   //
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($id) {
  global $display, $l_kind_link_deal, $l_kind_link_deal_no, $l_back;
  global $l_kind_can_delete, $l_kind_cant_delete, $l_kind_delete;

  $delete_ok = true;
  $label = get_kind_label($id);
  $obm_q = run_query_kind_links($id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_kind_link_deal</td>
    </tr>";

    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$l_kind_link_deal_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
   $dis_link
   </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
        action=\"" .url_prepare("deal_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_kind_can_delete);
    $dis_del = "<form name=\"form_kind_delete\".
          method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"kind_delete\" />
        <input type=\"hidden\" name=\"sel_kind\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_kind\" value=\"$l_kind_delete\" />
        </form>";
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_kind_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the stabtus links                                                //
// Parameters:
//   - $id : status id
///////////////////////////////////////////////////////////////////////////////
function dis_status_links($id) {
  global $display, $l_status_link_deal, $l_status_link_deal_no, $l_back;
  global $l_status_can_delete, $l_status_cant_delete, $l_status_delete;

  $delete_ok = true;
  $label = get_status_label($id);
  $obm_q = run_query_status_links($id);

  if ($obm_q->num_rows() > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_status_link_deal</td>
    </tr>";

    while ($obm_q->next_record()) {
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $dis_link.= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
      </td>
    </tr>";
    }

  } else {
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$l_status_link_deal_no</td>
    </tr>";
  }

  $block .= "<table class=\"detail\">
   $dis_link
   </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("deal_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_status_can_delete);
    $dis_del = "<form name=\"form_kind_delete\".
        method=\"post\# action=\"" . url_prepare("deal_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
      <input type=\"hidden\" name=\"sel_state\" value=\"$id\" />
      <input type=\"submit\" name=\"sub_status\" value=\"$l_status_delete\" />
      </form>";

    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_status_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the category links                                               //
// Parameters:
//   - $id : category id
///////////////////////////////////////////////////////////////////////////////
function dis_cat_links($id) {
  global $display, $l_cat_link_deal, $l_cat_link_deal_no, $l_back;
  global $l_cat_link_project, $l_cat_link_project_no;
  global $l_cat_link_timetask, $l_cat_link_timetask_no;
  global $l_cat_can_delete, $l_cat_cant_delete, $l_cat_delete;

  $delete_ok = true;
  $cat_infos = get_cat_infos($id);
  $label = $cat_infos["label"];
  $intern = $cat_infos["intern"];

  $obm_q = run_query_cat_links($id, $intern);

  if ($intern == 0) {

    if ($obm_q->num_rows() > 0) {
      $delete_ok = false;
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_cat_link_deal</td>
    </tr>";

      while ($obm_q->next_record()) {
	$cid = $obm_q->f("deal_id");
	$clabel = $obm_q->f("deal_label");
	$dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$cid") . "\">$clabel</a>
      </td>
    </tr>";
      }

    } else {
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$l_cat_link_deal_no</td>
    </tr>";
    }

  } else if ($intern == 1) {

    if ($obm_q->num_rows() > 0) {
      $delete_ok = false;
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_cat_link_project</td>
    </tr>";

      while ($obm_q->next_record()) {
	$pid = $obm_q->f("deal_id");
	$plabel = $obm_q->f("deal_label");
	$dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("../project/project_index.php?action=detailconsult&amp;param_project=$pid") . "\">$plabel</a>
      </td>
    </tr>";
      }

    } else {
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$l_cat_link_project_no</td>
    </tr>";
    }

  } else {

    if ($obm_q->num_rows() > 0) {
      $delete_ok = false;
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_cat_link_timetask</td>
    </tr>";
      
    } else {
      $dis_link = "
    <tr>
      <td class=\"detailHead\">$l_cat_link_timetask_no</td>
    </tr>";
    }

  }

  $block .= "<table class=\"detail\">
   $dis_link
   </table>";

  $dis_back = "<form name=\"form_back\" method=\"get\"
          action=\"" .url_prepare("deal_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"admin\" />
        <input type=\"submit\" value=\"$l_back\" />
        </form>";

  if ($delete_ok == true) {
    $display["msg"] .= display_ok_msg($l_cat_can_delete);
    $dis_del = "<form name=\"form_cat_delete\".
          method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
        <input type=\"hidden\" name=\"action\" value=\"cat_delete\" />
        <input type=\"hidden\" name=\"sel_cat\" value=\"$id\" />
        <input type=\"submit\" name=\"sub_cat\" value=\"$l_cat_delete\" />
        </form>";

    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_del</p>
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  } else {
    $display["msg"] .= display_warn_msg($l_cat_cant_delete);
    $block .= "
      <div class=\"detailButton\">
        <p class=\"detailButtons\">$dis_back</p>
      </div>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal Display preference screen
// Parameters:
//   - $pref_q        : DBO : Deal Field list to display
//   - $pref_parent_q : DBO : ParentDeal Field list to display
//   - $pref_invoice  : DBO : Invoice Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_deal_display_pref($pref_q, $pref_parent_q, $pref_invoice) {
  global $l_deal_display, $l_parentdeal_display, $l_invoice_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_q);
  $dis_pref->display_entity = "deal";
  $dis_pref->pref_title = $l_deal_display;

  $block = "
    <table class=\"detail\"><tr><td>";
  $block .= $dis_pref->display();
  $block .= "
    </td>";

  $dis_pref->display_pref = $pref_parent_q;
  $dis_pref->display_module = "deal";
  $dis_pref->display_entity = "parentdeal";
  $dis_pref->pref_title = $l_parentdeal_display;

  $block .= "<td>";
  $block .= $dis_pref->display();
  $block .= "</td><td>";
  $dis_pref->display_pref = $pref_invoice;
  $dis_pref->display_entity = "invoice";
  $dis_pref->pref_title = $l_invoice_display;
  
  $block .= $dis_pref->display();
  
  $block .= "</td></tr></table>";
  $block .= $dis_pref->dis_pref_help();

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// -- ParentDeal's Section                                                   //
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Display: Parent Deal search result                                        //
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_parentdeal_search_list($deal) {
  global $l_no_found_parent;
  global $display, $auth, $new_order, $order_dir;

  $obm_q = run_query_search_parentdeal($deal, $new_order, $order_dir);
  
  $nb_parent = $obm_q->num_rows();
  if ($obm_q->num_rows() == 0) {
    $display["msg"] .= display_warn_msg($l_no_found_parent);
  } else {
    $pref_q = run_query_display_pref($auth->auth["uid"], "parentdeal");
    $block = html_parentdeal_search_list($obm_q, $pref_q, $nb_parent, $deal);
  }
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Parent Deal Search result                                  //
// Parameters:
//   - $obm_q        : list of parent deals
//   - $option_dis_q : fields to display in the list of deals 
//   - $nb_pdeal     : nb parent deals found
//   - $deal[]       : parent deal search criteria
//     keys used     : plabel, pmanager, parchive
// Display the results of the parentdeal search
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_search_list($obm_q, $pref_q, $nb_parentdeals, $deal) {
  global $display, $l_found_parent; 

  $plabel = urlencode(stripslashes($deal["plabel"]));
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];
  $display["msg"] .= display_info_msg($nb_parentdeals." ".$l_found_parent);

  $url = url_prepare("deal_index.php?action=parent_search&amp;tf_plabel=$plabel&amp;sel_pmanager=$pmanager&amp;cb_parchive=$parchive");

  $parentdeal_d = new OBM_DISPLAY("DATA", $pref_q, "deal");
  $parentdeal_d->data_set = $obm_q;
  $parentdeal_d->data_url = $url;
  $parentdeal_d->data_header = "both";
  $parentdeal_d->data_idfield = "parentdeal_id"; 
  $parentdeal_d->data_set->seek($first_row);
  $block = $parentdeal_d->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Form                                                  //
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the parent deal (null for new deal)
//   - $usr_q     : DBO : OBM user list
//   - deal[]     : default or transmitted values
//     keys used  : parent_id, plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_form($action, $deal_q, $usr_q, $deal) {
  // -- Themes
  global $ico_company,$ico_contact;
  // -- Labels
  global $l_update_parent,$l_delete_parent,$l_insert_parent,$l_label,$l_marketing_manager;
  global $l_parentdeal,$l_technical_manager,$l_comment;

  // if update mode and first time, values are taken from db
  if (($action == "parent_detailupdate") || ($action == "parent_delete")) {
    $parent = $deal_q->f("parentdeal_id");
    $plabel = $deal_q->f("parentdeal_label");
    $pmarket = $deal_q->f("parentdeal_marketingmanager_id");
    $ptech = $deal_q->f("parentdeal_technicalmanager_id");
    $pcomment = $deal_q->f("parentdeal_comment");
  } else {
    // Values are taken from parameters
    $parent = $deal["parent"];
    $plabel = stripslashes($deal["plabel"]);
    $pmarket = $deal["pmarket"];
    $ptech = $deal["ptech"];
    $pcomment = stripslashes($deal["pcom"]);
  }  

  // Marketing manager select
  $sel_market = "<select name=\"sel_pmarket\">";
  while($usr_q->next_record()) {
    $sel_market .= "<option value=\"" . $usr_q->f("userobm_id") . "\"";
    if ($usr_q->f("userobm_id") == $pmarket) $sel_market .= " selected= \" selected \"";
    $sel_market .= ">" . $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $sel_market .= "</select>";

  // Technical manager select
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_tech = "<select name=\"sel_ptech\">";
  while($usr_q->next_record()) {
    $sel_tech .= "<option value=\"" . $usr_q->f("userobm_id") . "\"";
    if ($usr_q->f("userobm_id") == $ptech) $sel_tech .= "selected= \" selected \"";
    $sel_tech .= ">" . $usr_q->f("userobm_lastname") . " " . $usr_q->f("userobm_firstname")."</option>";
  }
  $sel_tech .= "</select>";

  if (($action == "parent_detailupdate") || ($action == "parent_update")) {
    $dis_button = "<input type=\"hidden\" name=\"action\" value=\"parent_update\" />
        <input type=\"hidden\" name=\"param_parent\" value=\"$parent\" />
        <input type=\"submit\" value=\"$l_update_parent\" />
         ";
  } else {
     $dis_button = "<input type=\"hidden\" name=\"action\" value=\"parent_insert\" />
      <input type=\"submit\" value=\"$l_insert_parent\" />";
  }

  $block = "
  <form method=\"post\" name=\"form_parentdeal_update\"
      onsubmit=\"if (check_parentdeal(this)) return true; else return false;\"
      action=\"" . url_prepare("deal_index.php") . "\">

  <div class=\"detailHead\">$l_parentdeal</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailHead\">$l_label</td>
      <td class=\"detailForm\"><input name=\"tf_plabel\" type=\"text\" size=\"40\" value=\"$plabel\" /></td>
    </tr><tr>
      <td class=\"detailHead\">$l_marketing_manager</td>
      <td class=\"detailForm\">$sel_market</td>
    </tr><tr>
      <td class=\"detailHead\">$l_technical_manager</td>
      <td class=\"detailForm\">$sel_tech</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
     <td class=\"detailForm\"><textarea name=\"ta_pcom\" rows=\"8\" cols=\"72\">$pcomment</textarea></td>
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
// Display: Parent Deal Detail                                               //
// Parameters :
//   - $parent_q : DBO : information about the parent deal
//   - $deal_q   : DBO : parentdeal deal list
//   - deal[]    : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
//   - $pref_q   : display preferences for the deal list
//   - $nbdeals  : number of deals in the list
//     //---> used by the deal search form :
//   - $kind_q   : list of deal types available
//   - $cat_q    : list of deal categories available
//   - $state_q  : list of deal status available 
//   - $usr_q    : DBO : list of userobm
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_consult($parent_q,$deal_q,$deal,$pref_q,$nb_deals,$kind_q,$cat_q,$state_q,$usr_q) {
  global $display, $path, $perms_user;
  global $l_parentdeal_deals,$l_update_parent,$l_label,$l_technical_manager;
  global $l_parentdeal,$l_date_proposal,$l_date_alarm,$l_amount_in,$l_amount_out,$l_potential,$l_comment,$l_marketing_manager,$l_state;
  global $l_infos;
  global $popup_width, $popup_height, $l_deal_select_company;

  $parent = $parent_q->f("parentdeal_id");
  $plabel = $parent_q->f("parentdeal_label");
  $pmarket = $parent_q->f("parentdeal_marketingmanager_id");
  $ptech = $parent_q->f("parentdeal_technicalmanager_id");
  $pcomment = nl2br($parent_q->f("parentdeal_comment"));

  $pmarket_name = $parent_q->f("lname1") . " " . $parent_q->f("fname1");
  $ptech_name = $parent_q->f("lname2") . " " . $parent_q->f("fname2");
  $p_status = get_parent_status($parent);
  $p_dateproposal = get_parent_dateproposal($parent);
  $p_datealarm = get_parent_datealarm($parent);
  $p_expense = get_parent_expense($parent);
  $p_income = get_parent_income($parent);
  $p_potential = get_parent_potential($parent);

  $block = "
    <table width=\"100%\">
    <tr>
      <td valign=\"top\">

      <div class=\"detailHead\">$l_parentdeal</div>

	<table class=\"detail\">
	<tr>
	  <td class=\"detailLabel\">$l_label</td>
	  <td class=\"detailText\">$plabel</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_marketing_manager</td>
	  <td class=\"detailText\">$pmarket_name</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_technical_manager</td>
	  <td class=\"detailText\">$ptech_name</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_date_proposal</td>
	  <td class=\"detailText\">$p_dateproposal</td>
	</tr>
	</table>

      </td><td>

      <div class=\"detailHead\">$l_infos</div>

	<table class=\"detail\">
	<tr>
	  <td class=\"detailLabel\">$l_state :</td>
	  <td class=\"detailText\">$p_status</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_date_alarm</td>
	  <td class=\"detailText\">$p_datealarm</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_amount_out</td>
	  <td class=\"detailText\">$p_expense</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_amount_in</td>
	  <td class=\"detailText\">$p_income</td>
	</tr><tr>
	  <td class=\"detailLabel\">$l_potential</td>
	  <td class=\"detailText\">$p_potential</td>
	</tr>
	</table>

      </td>

    </tr><tr>

      <td colspan=\"2\">

      <div class=\"detailHead\">$l_comment</div>

	<table class=\"detail\">
	<tr>
          <td class=\"detailText\">$pcomment</td>
        </tr> 
        </table>

      </td>
    </tr><tr>
   </table>";
    
  $block .= display_info_msg($l_parentdeal_deals);

  //----- deal search form 
  $block .= html_deal_search_form($deal, $kind_q, $cat_q, $state_q, $usr_q);

  //-----> deal list
  // if no record found, display it and stop
  if ($nb_deals == 0) {
    return $block;
  }

  $label = urlencode(stripslashes($deal["label"]));
  $name = urlencode(stripslashes($deal["company_name"]));
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $cat = $deal["cat"];
  $state = $deal["state"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);

  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_state=$state&amp;sel_cat=$cat&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent");

  // Display the list of deals attached to the parentdeal
  $dis_list_deal = new OBM_DISPLAY("DATA", $pref_q, "deal");
  $dis_list_deal->data_set = $deal_q;
  $dis_list_deal->data_url = $url;
  $dis_list_deal->data_header = "both";
  $dis_list_deal->data_idfield = "deal_id"; 
  $dis_list_deal->data_set->seek($first_row);

  $block .= $dis_list_deal->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Select Form                                               //
// Parameters:
//   - $deal_q : deal database result
//   - $title  : Title to be displayed
//   - $url    : URL to call with parameter deal
///////////////////////////////////////////////////////////////////////////////
function html_select_deal($deal_q, $title, $url="") {
  global $l_select_deal, $l_close;

  if ($url != "") {
    $jsfunc = "check_get_id_url(this, '$url')";
  } else {
    $jsfunc = "check_get_id(this)";
  }
  $dis_select ="<select name=\"param_deal\" size=\"10\" style=\"font-family:fixed,courier\">";

  while ($deal_q->next_record()) {
    $id = $deal_q->f("deal_id");
    $name = $deal_q->f("deal_label");
    $dis_select .= "\n<option value=\"$id\">$name</option>";
  }
  $dis_select .= "</select>";

  echo "
  <div class=\"detailHead\">$title</div>

  <form method=\"get\" name=\"form_contr\"
    onsubmit=\"if ($jsfunc) return true; else return false;\" action=\"\">

  <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
      $dis_select
      </td>
    </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">
    <input type=\"hidden\" name=\"action\" value=\"new\" />
    <input type=\"submit\" value=\"$l_select_deal\" />
    </p>
  </div>
  </form>

  <a href=\"\" onclick='window.close();'>$l_close</a>
";
}
 

</SCRIPT>
