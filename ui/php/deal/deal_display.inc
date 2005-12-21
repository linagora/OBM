<script language="php">
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
$fieldnames["deal_dateexpected"] = $l_date_expected;
$fieldnames["deal_archive"] = $l_archive_first;
$fieldnames["deal_todo"] = $l_todo;
$fieldnames["deal_marketingmanager"] = $l_marketing_manager;
$fieldnames["deal_amount"] = $l_amount;
$fieldnames["deal_hitrate"] = $l_hitrate;
$fieldnames["parentdeal_label"] = $l_label;
$fieldnames["parentdeal_comment"] = $l_comment;
// Calculated or indirect fields
$fieldnames["dealstatus_label"] = $l_status;
$fieldnames["dealtype_label"] = $l_kind;
$fieldnames["tasktype_label"] = $l_tt;
$fieldnames["deal_company_name"] = $l_company;
$fieldnames["deal_company_zipcode"] = $l_postcode;
$fieldnames["parentdeal_marketing_lastname"] = $l_marketing_manager;
$fieldnames["parentdeal_technical_lastname"] = $l_technical_manager;
$fieldnames["parentdeal_status"] = $l_status;
$fieldnames["parentdeal_dateproposal"] = $l_date_proposal;
$fieldnames["parentdeal_datealarm"] = $l_date_alarm;
$fieldnames["parentdeal_amountprovider"] = $l_amount_out;
$fieldnames["parentdeal_amountcustomer"] = $l_amount_in;


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
  global $path, $deal, $col_ok, $col_error, $col_client, $col_frs;

  $ext_url = $deal["ext_url"];

  if ($fieldname == "deal_label") {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("deal_id").",'".addslashes($OD->data_set->f("deal_label"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $res["url"] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f("deal_id").");";
    } else {
      $res["url"] = "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$OD->data_set->f("deal_id");
    }
  }

  else if ($fieldname == "parentdeal_label") {
    $res["url"] = "$path/deal/deal_index.php?action=parent_detailconsult&amp;param_parent=".$OD->data_set->f("parentdeal_id");
  }

  else if ($fieldname == "parentdeal_marketing_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("parentdeal_marketingmanager_id");
  }

  else if ($fieldname == "parentdeal_technical_lastname") {
    $res["url"] = "$path/user/user_index.php?action=detailconsult&amp;param_user=".$OD->data_set->f("parentdeal_technicalmanager_id");
  }

  else if ($fieldname == "deal_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
  }

  else if ($fieldname == "deal_hitrate") {
    $res["align"] = "center";
    $res["name"] = $OD->data_set->f($fieldname) . " %";
  }

  else if (($fieldname == "deal_company_name") && $link_ok) {
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
    $res["name"] = $datealarm;
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal index page
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_index($deal="") {
  global $display, $ctt_sales;

  $archive = $deal["archive"];

  $usr_q = run_query_deal_manager($archive);
  $type_q = run_query_dealtype();
  $tt_q = run_query_tasktype($ctt_sales);
  $cats1 = of_category_get_ordered("deal", "category1");
  $status_q = run_query_dealstatus();
  $block .= html_parentdeal_search_form($deal, $usr_q);
  $block .= html_deal_search_form($deal, $type_q, $tt_q, $cats1, $status_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Search page
// Parameters:
//   - $deal[] : hash with deal and parent deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_form($deal="") {
  global $display, $ctt_sales;

  $archive = $deal["archive"];

  $usr_q = run_query_deal_manager($archive);
  $type_q = run_query_dealtype();
  $tt_q = run_query_tasktype($ctt_sales);
  $cats1 = of_category_get_ordered("deal", "category1");
  $status_q = run_query_dealstatus();
  $block .= html_deal_search_form($deal, $type_q, $tt_q, $cats1, $status_q, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Deal search form
// Parameters : 
//   - $deal[]     : default form values
//     keys used   : parent, label, company_name, archive, kind, cat, status,
//                   manager, dateafter, datebefore
//   - $kind_q     : DB result object (deal kind list)
//   - $tt_q       : DB result object (deal task type list)
//   - $cats1      : array with deal category 1 list 
//   - $stat_q     : DB result object (deal status list)
//   - $manager_q  : DB result object (userobm list)
///////////////////////////////////////////////////////////////////////////////
function html_deal_search_form($deal, $kind_q, $tt_q, $cats1, $stat_q, $manager_q) {
  global $l_deal, $l_label_start, $l_company, $l_kind, $l_tt, $l_status;
  global $l_after, $l_before, $l_manager, $l_show_archive, $l_postcode;
  global $l_all, $l_all_f, $l_find, $c_all;
  global $display, $cgp_hide, $l_select_deal;

  // --- Var preparation ------------------------------------------------------

  $popup = $deal["popup"];
  $parent = $deal["parent"];
  $label = stripslashes($deal["label"]);
  $name = stripslashes($deal["company_name"]);
  $zip = stripslashes($deal["company_zip"]);
  $archive = ($deal["archive"] == "1" ? "checked = \"checked\"" : "");
  $kind = $deal["kind"];
  $tt = $deal["tasktype"];
  $cat1 = $deal["category1"];
  $status = $deal["status"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);
  $company_id = $deal["company"];

  // show : category1

  // Kind select construction
  $sel_kind = "<select name=\"sel_kind\">
     <option value=\"$c_all\">$l_all</option>";  
  while ($kind_q->next_record()) {
    $id = $kind_q->f("dealtype_id");
    $klabel = $kind_q->f("dealtype_label");
    $sel_kind .= "<option value=\"$id\"";
    if ($kind == $id) $sel_kind .= " selected = \"selected\"";
    $sel_kind .= ">$klabel</option>";
  }
  $sel_kind .= "</select>";

  // TaskType select construction
  $sel_tt = "<select name=\"sel_tt\">
              <option value=\"$c_all\">$l_all</option>";
  while ($tt_q->next_record()) {
    //put menu titles
    $id = $tt_q->f("tasktype_id");
    $tt_label = $tt_q->f("tasktype_label");
    $sel_tt .= "\n<option value=\"$id\"";
    if ($tt == $id) $sel_tt .= "selected =\"selected\"";
    $sel_tt .= ">$tt_label</option>\n";
  }
  $sel_tt .= "</select>";

  // Category select construction
  $block_category1 = of_category_dis_search_select("deal", "category1", $cats1, $cat1);

  // Status select construction
  $sel_status = "<select name=\"sel_status\">
     <option value=\"$c_all\">$l_all</option>";
  while ($stat_q->next_record()) {
    $id = $stat_q->f("dealstatus_id");
    $slabel = $stat_q->f("dealstatus_label");
    $sel_status .= "<option value=\"$id\"";
    if ($status == $id) $sel_status .= "selected =\" selected\"";
    $sel_status .= ">$slabel</option>";
  }
  $sel_status .= "</select>";

  // Manager select construction
  if ($manager_q->num_rows() > 0) {
    $manager_q->seek(0);
  }
  $sel_manager = "<select name=\"sel_manager\">
     <option value=\"$c_all\">$l_all</option>";
  while ($manager_q->next_record()) {
    $id = $manager_q->f("userobm_id");
    $lname = $manager_q->f("userobm_lastname");
    $sel_manager .= "<option value=\"$id\"";
    if ($manager == $id) $sel_manager .= "selected =\"selected\"";
    $sel_manager .= ">$lname</option>";
  }
  $sel_manager .= "</select>";


  $url = url_prepare("deal_index.php");

  if ($popup) {
    $ext_action = $deal["ext_action"];
    $ext_url = $deal["ext_url"];
    $ext_id = $deal["ext_id"];
    $ext_title = ($deal["ext_title"] ? $deal["ext_title"] : "$l_select_deal");
    $ext_target = $deal["ext_target"];
    $ext_widget = $deal["ext_widget"];
    $ext_widget_text = $deal["ext_widget_text"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_widget\" type=\"hidden\" value=\"$ext_widget\">
            <input name=\"ext_widget_text\" type=\"hidden\" value=\"$ext_widget_text\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  // --- HTML Template --------------------------------------------------------

  $block = "
  <form method=\"get\" name=\"f_search\"
  onsubmit=\"if (check_search_form(this) == false) return false; else return true;\"
  action=\"$url\">
  <div class=\"detailHead\">$l_deal</div>
  <div class=\"search\">
    <div class=\"searchLabel\">$l_label_start<br />
      <input name=\"tf_label\" size=\"16\" maxlength=\"128\" value=\"$label\" />
    </div>
    <div class=\"searchLabel\">$l_company<br />
      <input name=\"tf_company_name\" size=\"12\" maxlength=\"16\" value=\"$name\" />
    </div>
    <div class=\"searchLabel\">$l_postcode<br />
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
    <div class=\"searchLabel\">$l_tt<br />
      $sel_tt
    </div>
    $block_category1 
    <div class=\"searchLabel\">$l_status<br />
      $sel_status
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
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />      
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </div>
    <div class=\"searchEnd\"></div>
  </div>
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
function html_parentdeal_search_form($deal, $pmanager_q) {
  global $l_parentdeal, $l_label_start, $l_all, $l_all_f, $l_manager;
  global $l_find,$l_show_archive; 

  // --- Var preparation ------------------------------------------------------

  $plabel = stripslashes($deal["plabel"]);
  $parchive = ($deal["parchive"] == "1" ? "checked" : "");
  $pmanager = $deal["pmanager"];

  // Manager select construction
  if ($pmanager_q->num_rows() > 0) {
    $pmanager_q->seek(0);
  }
  $sel_pmanager = "<select name=\"sel_pmanager\">
     <option value=\"$c_all\">$l_all</option>";
  while($pmanager_q->next_record()) {
    $id = $pmanager_q->f("userobm_id");
    $lname = $pmanager_q->f("userobm_lastname");
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
// Display the Deal search result
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_search_list($deal) {
  global $display, $auth, $l_no_found;

  $new_order = $deal["new_order"];
  $order_dir = $deal["order_dir"];

  $obm_q = run_query_search($deal, 0, $new_order, $order_dir);
  $nb_deal = $obm_q->num_rows_total();
  if ($nb_deal == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $prefs = get_display_pref($auth->auth["uid"], "deal", 0);
    $block = html_deal_search_list($obm_q, $prefs, $nb_deal, $deal);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display the Deal Search result
// Parameters:
//   - $deal_q   : list of deals
//   - $prefs    : fields to display in the list of deals 
//   - $nb_deal  : nb deals found
//   - $deal[]   : deal search criteria
//     keys used : parent, label, company_name, archive, kind, tt, status,
//                 manager, dateafter, datebefore
//////////////////////////////////////////////////////////////////////////////
function html_deal_search_list($deal_q, $prefs, $nb_deal, $deal) {
  global $display, $l_found, $l_close;

  $popup = $deal["popup"];
  $parent = $deal["parent"];
  $label = urlencode(stripslashes($deal["label"]));
  $name = urlencode(stripslashes($deal["company_name"]));
  $zip = urlencode(stripslashes($deal["company_zip"]));
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $tt = $deal["tasktype"];
  $cat1 = $deal["category1"];
  $status = $deal["status"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);
  $param_comp = $deal["company"];
  
  if ($popup) {
    $ext_action = $deal["ext_action"];
    $ext_url = $deal["ext_url"];
    $ext_id = $deal["ext_id"];
    $ext_target = $deal["ext_target"];
    $ext_widget = $deal["ext_widget"];
    $ext_widget_text = $deal["ext_widget_text"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_widget=$ext_widget&amp;ext_widget_text=$ext_widget_text&amp;popup=1";
  }

  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;tf_zip=$zip&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_status=$status&amp;sel_tt=$tt&amp;sel_category1=$cat1&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent&param_company=$param_comp$url_ext");

  $deal_d = new OBM_DISPLAY("DATA", $prefs, "deal");
  if ($popup) {
    $deal_d->display_link = false;
    if ($ext_url != "") {
      $deal_d->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $deal_d->display_ext = "get_id";
    }
   $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }

  $deal_d->data_set = $deal_q;
  $deal_d->data_url = $url;
  $deal_d->data_header = "both";
  $display["msg"] .= display_info_msg("$nb_deal $l_found");
  $block = $deal_d->display("dis_data_deal");
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
  global $set_theme, $ico_pdeal, $ico_deal, $ico_contract;
  global $ico_contract_new, $ico_invoice, $ico_invoice_new, $ico_project;
  global $l_new;
  global $l_parentdeal, $l_affect_deal, $l_module_contract;
  global $l_module_project, $l_module_invoice;
  global $l_module_document,$ico_document, $l_deal;
  global $l_document_add, $ico_document_new;
  global $path, $auth, $cgp_show, $popup_height,$popup_width;

  $id = $deal_q->f("deal_id");
  $pid = $deal_q->f("deal_parentdeal_id");
  $cid = $deal_q->f("deal_company_id");
  $tt_id = $deal_q->f("deal_tasktype_id");
  $cname = $deal_q->f("company_name");
  $uname = urlencode($cname);
  $label = $deal_q->f("deal_label");
  $ulabel = urlencode($label);
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
    <li><a href=\"$url_pdeal\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_pdeal\" alt=\"\" /></a>
        <a href=\"$url_pdeal\">$l_parentdeal ($plabel)</a></li>";
  }
  $url_pdeal_upd = url_prepare("$path/deal/deal_index.php?action=affect&amp;param_parent=$pid&amp;param_deal=$id");

  // Project
  if ($cgp_show["module"]["project"]) {
    $nb_pro = get_linked_project_nb ($id, "deal");
    $nb_pro_arch = get_linked_project_nb ($id, "deal", 1);
    $url_pro = url_prepare("$path/project/project_index.php?action=search&amp;param_deal=$id");
    $url_pro_arch = url_prepare("$path/project/project_index.php?action=search&amp;param_deal=$id&amp;cb_archive=1");
    $dis_ico_project = "
        <li><a href=\"$url_pro\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" alt=\"\" /></a>
            $l_module_project (<a href=\"$url_pro\">$nb_pro</a> / <a href=\"$url_pro_arch\">$nb_pro_arch</a>)</li>";

    // New project allowed only if hitrate = 100
    if ($hitrate == "100") {
      $url_pro_new = url_prepare("$path/project/project_index.php?action=new&amp;param_deal=$id&amp;deal_label=$label&amp;param_company=$cid&amp;tf_company_name=$cname&amp;sel_tt=$tt_id");
      $dis_ico_project .= "
        <li><a href=\"$url_pro_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" alt=\"\" /></a>
            <a href=\"$url_pro_new\">$l_new</a></li>";
    }
    
    $block_project = "
     <div class=\"linkTitle\">:: $l_module_project</div>
     <ul class=\"linkList\">
       $dis_ico_project
     </ul>";
  }

  // Contract
  if ($cgp_show["module"]["contract"]) {
    $contract_active = get_linked_contract_nb($id, "deal");
    $contract_total = get_linked_contract_nb($id, "deal", 1);
    $url_contract = url_prepare("$path/contract/contract_index.php?action=search&amp;param_deal=$id");
    $url_contract_total = url_prepare("$path/contract/contract_index.php?action=search&amp;param_deal=$id&amp;cb_archive=1");
    $dis_contract = "
    <li><a href=\"$url_contract\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"\" /></a>
        $l_module_contract (
        <a href=\"$url_contract\">$contract_active</a> /
        <a href=\"$url_contract_total\">$contract_total</a> )</li>";

    // New Contracts allowed only if hitrate = 100
    if ($hitrate == "100") {
      $url_contract_new = url_prepare("$path/contract/contract_index.php?action=new&amp;param_deal=$id&amp;param_company=$cid&amp;deal_label=$label&amp;sel_con1=$con1&amp;sel_con2=$con2&amp;sel_market=$market&amp;sel_tech=$tech");
      $dis_contract .= "
      <li><a href=\"$url_contract_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contract\" alt=\"\" /></a>
        <a href=\"$url_contract_new\">$l_new</a></li>";
    }
    $block_contract = "
     <div class=\"linkTitle\">:: $l_module_contract</div>
     <ul class=\"linkList\">
       $dis_contract
     </ul>";
  }

  // Invoice
  if ($cgp_show["module"]["invoice"]) {
    $url_inv = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_deal=$id");
    $url_inv_tot = url_prepare("$path/invoice/invoice_index.php?action=search&amp;param_deal=$id&amp;cb_archive=1");
    $nb_inv_tot = get_linked_invoice_nb($id, "deal", 1);
    $nb_inv = get_linked_invoice_nb($id, "deal");
    $dis_invoice = "
    <li><a href=\"$url_inv\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        $l_module_invoice (<a href=\"$url_inv\">$nb_inv</a> /
        <a href=\"$url_inv_tot\">$nb_inv_tot</a>)</li>";

    // New Invoices allowed only if hitrate = 100
    if ($hitrate == "100") {
      $url_inv_new = url_prepare("$path/invoice/invoice_index.php?action=new&amp;param_deal=$id&amp;deal_label=$ulabel&amp;param_company=$cid&amp;company_name=$uname");
      $dis_invoice .= "
    <li><a href=\"$url_inv_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_invoice\" alt=\"\" /></a>
        <a href=\"$url_inv_new\">$l_new</a></li>";
    }
    $block_invoice = "
  <div class=\"linkTitle\">:: $l_module_invoice</div>
  <ul class=\"linkList\">
    $dis_invoice
  </ul>";
  }

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=deal");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=deal");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/deal/deal_index.php")."&amp;ext_id=$id&amp;ext_target=$l_deal";
    $nb_document = run_query_document_nb($id, "deal");
    $block_doc = "
  <div class=\"linkTitle\">:: $l_module_document</div>
  <ul class=\"linkList\">
    <li><a href=\"$url_doc\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
	<li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_document_new\" alt=\"\" /></a>
	<a href=\"\" onclick=\"window.name='$l_deal'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>";
  }

  // Links Template  
  $block = "<div class=\"link\">
  <div class=\"linkTitle\">:: $l_parentdeal</div>
  <ul class=\"linkList\">
    $dis_pdeal
    <li><a href=\"$url_pdeal_upd\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"\" /></a>
        <a href=\"$url_pdeal_upd\">$l_affect_deal</a></li>
  </ul>
  $block_project
  $block_contract
  $block_invoice
  $block_doc
       
 </div>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Deal Detail screen
// Parameters:
//   - $deal[] : deal values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_consult($deal) {
  global $display;

  if ($deal["id"] > 0) {
    $deal_q = run_query_deal_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q);
    $block = html_deal_consult($deal_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display: Deal Consult
// Parameters :
//   - $deal_q  : DBO : information about the deal
///////////////////////////////////////////////////////////////////////////////
function html_deal_consult($deal_q) {
  global $set_theme, $ico_company, $ico_contact, $display;
  global $l_infos, $l_company, $l_contact, $l_label,$l_date_init,$l_kind;
  global $l_tt,$l_marketing_manager,$l_technical_manager,$l_date_proposal;
  global $l_number, $l_amount,$l_hitrate,$l_date_expected;
  global $l_date_alarm,$l_status,$l_last_update,$l_archive,$l_comment,$l_at;
  global $l_visibility,$l_public,$l_private,$l_parentdeal,$l_todo, $l_deal;
  global $incl_arch, $param_deal;
  global $display, $path, $auth, $cgp_hide, $c_yes, $c_no;

  $id = $deal_q->f("deal_id");
  $pid = $deal_q->f("deal_parentdeal_id");
  $usercreate = $deal_q->f("deal_usercreate");
  $label = $deal_q->f("deal_label");
  $num = $deal_q->f("deal_number");
  $datebegin = date_format($deal_q->f("datebegin"));
  $kind = $deal_q->f("dealtype_label");
  $tt_label = $deal_q->f("tasktype_label");
  $lpriv = ($deal_q->f("deal_privacy") == 0 ? $l_public : $l_private);
  $market = $deal_q->f("lname1") . " " . $deal_q->f("fname1");
  $tech = $deal_q->f("lname2") . " " . $deal_q->f("fname2");
  $datepro = date_format($deal_q->f("dateproposal"), 1);
  $amount = $deal_q->f("deal_amount");
  $hitrate = $deal_q->f("deal_hitrate");
  $dateexpected = date_format($deal_q->f("dateexpected"), 1);
  $datealarm = date_format($deal_q->f("datealarm"), 1);
  $status = $deal_q->f("dealstatus_label");
  $dateu = date_format($deal_q->f("timeupdate"));
  $timeu = date("H:i:s",$deal_q->f("timeupdate"));
  $archive = ($deal_q->f("deal_archive") == 1 ? $c_yes : $c_no);
  $todo = $deal_q->f("deal_todo");
  $com = beautify_comment(nl2br($deal_q->f("deal_comment")));
  $comp_id = $deal_q->f("deal_company_id");
  $comp = $deal_q->f("company_name");
  if (strlen($comp) > 25) {
    $comp = substr($comp, 0, 24) . "...";
  }
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
  $cats1 = of_entitycategories_get($deal_q->f("deal_id"), "deal", "category1");

  $display["link"] = html_deal_links($deal_q);
  $display["title"] = "<div class=\"title\">$l_deal : $label</div>";
   
  // show category1

  // Category 1 field
  $block_category1 = of_dis_block_consult_category($cats1, "deal", "category1");

  $block = "
    <table width=\"100%\">
    <tr>
      <td valign=\"top\">

    <div class=\"detailHead\">$l_deal</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_label</td>
        <td class=\"detailText\">$label</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_visibility</td>
        <td class=\"detailText\">$lpriv</td>
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
        <td class=\"detailLabel\">$l_tt</td>
        <td class=\"detailText\">$tt_label</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_marketing_manager</td>
        <td class=\"detailText\">$market</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_technical_manager</td>
        <td class=\"detailText\">$tech</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_date_proposal</td>
        <td class=\"detailText\">$datepro</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_amount</td>
        <td class=\"detailText\">$amount</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_hitrate</td>
        <td class=\"detailText\">$hitrate %</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_date_expected</td>
        <td class=\"detailText\">$dateexpected</td>
      </table>
      </td>
      <td>

    <div class=\"detailHead\">$l_company</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_company 
        <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id")."\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$comp<br />$ad1<br/>$zip $town</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_contact 1
        <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1") . "\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$dis_con1 - $phone_con1
        </td>
      </tr><tr>
        <td class=\"detailLabel\">$l_contact 2
        <a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2") . "\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[details]\" /></a>
        </td>
        <td class=\"detailText\">$dis_con2 - $phone_con2
        </td>
      </tr>
      </table>

    <div class=\"detailHead\">$l_infos</div>

      <table class=\"detail\">
      <tr>
        <td class=\"detailLabel\">$l_date_alarm</td>
        <td class=\"detailText\">$datealarm</td>
      </tr><tr>
        <td class=\"detailLabel\">$l_status</td>
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
      
      $block_category1
      
      <tr>
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

  return $block;
}

///////////////////////////////////////////////////////////////////////////////
// Display the Deal form
// Parameters:
//   - $deal[]    : deal search criteria
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_form($deal) {
  global $display, $uid, $action, $ctt_sales, $cg_com, $cg_prod;

  if ($action == "detailupdate") {
    $deal_q = run_query_deal_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q);
    $deal["company"] = $deal_q->f("deal_company_id");
    $users = array($uid, $deal_q->f("deal_marketingmanager_id"), $deal_q->f("deal_technicalmanager_id"), );
  } else if ($action == "update") {
    $users = array($uid, $deal["market"], $deal["tech"]);
    $deal_q = "";
  } else {
    $deal_q = "";
    $users = array($uid);
  }
  
  if (($action == "new") || ($action == "insert")) {
    $comp_q = run_query_company_info($deal["company"]);
  } else {
    $comp_q = "";
  }

  $type_q = run_query_dealtype();
  $tt_q = run_query_tasktype($ctt_sales);
  $usrc_q = run_query_all_users_from_group($cg_com, $users);
  $usrp_q = run_query_all_users_from_group($cg_prod, $users);
  $sta_q = run_query_dealstatus();
  $con_q = run_query_contact_deal($deal["company"]);
  
  $cats1 = of_category_get_ordered("deal", "category1", "multi", $did);

  $block = html_deal_form($action, $deal_q, $type_q, $tt_q, $cats1, $usrc_q, $usrp_q, $comp_q, $con_q, $sta_q, $deal);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Deal Form                   
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $kind_q    : DBO : deal kind list
//   - $tt_q      : DBO : deal task type list
//   - $cats1     : array with deal category 1 list 
//   - $usrc_q    : DBO : list of userobm from system group Commercial
//   - $usrp_q    : DBO : list of userobm from system group Production
//   - $comp_q    : DBO : deal company infos (name, ad1, zip, town when new)
//   - $con_q     : DBO : list of contact from the company 
//   - $sta_q     : DBO : list of deal status
//   - $comp_id   : id of the company concerned by the deal
//   - deal[]     : default or transmitted values
//     keys used  : id, usercreate, num, label
///////////////////////////////////////////////////////////////////////////////
function html_deal_form($action,$deal_q,$kind_q,$tt_q,$cats1, $usrc_q,$usrp_q,$comp_q, $con_q, $sta_q, $deal) {
  global $set_theme,$ico_company,$ico_contact,$ico_add;
  global $c_php_isodate_format, $l_update,$l_insert;
  global $l_deal, $l_infos, $l_company,$l_label,$l_number,$l_date_init,$l_kind;
  global $l_tt, $l_cat,$l_marketing_manager,$l_technical_manager,$l_private;
  global $l_date_proposal,$l_amount,$l_hitrate,$l_date_expected;
  global $l_contact,$l_date_alarm,$l_status;
  global $l_last_update, $l_archive, $l_at, $l_todo;
  global $l_comment,$l_add_comment,$l_upd_comment, $l_mail_comment;
  global $l_no, $l_members, $l_group;
  global $path, $action, $auth, $cgp_hide, $popup;
  global $c_all, $l_all, $c_undef, $l_undef;

  $ext_target = $deal["ext_target"];
  $uid = $auth->auth["uid"];
  $comp_id = $deal["company"];

  // if update mode and first time, values are taken from db
  if ($action == "detailupdate") {
    $id = $deal_q->f("deal_id");
    $pid = $deal_q->f("deal_parentdeal_id");
    $usercreate = $deal_q->f("deal_usercreate");
    $label = $deal_q->f("deal_label");
    $num = $deal_q->f("deal_number");
    $datebegin = isodate_format($deal_q->f("datebegin"), 1);
    $kind = $deal_q->f("deal_type_id");
    $tt = $deal_q->f("deal_tasktype_id");
    $priv = ($deal_q->f("deal_privacy") == 0 ? "0" : "1");
    $market = $deal_q->f("deal_marketingmanager_id");
    $tech = $deal_q->f("deal_technicalmanager_id");
    $dateprop = isodate_format($deal_q->f("dateproposal"), 1);
    $amount = $deal_q->f("deal_amount");
    $hitrate = $deal_q->f("deal_hitrate");
    $con1 = $deal_q->f("deal_contact1_id");
    $con2 = $deal_q->f("deal_contact2_id");
    $dateexpected = isodate_format($deal_q->f("dateexpected"), 1);
    $datealarm = isodate_format($deal_q->f("datealarm"), 1);
    $status = $deal_q->f("deal_status_id");
    $archive = ($deal_q->f("deal_archive") == 1 ? " checked" : "");
    $todo = $deal_q->f("deal_todo");
    $comment = $deal_q->f("deal_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();
    $comp = $deal_q->f("company_name");
    $ad1 = $deal_q->f("company_address1");
    $zip = $deal_q->f("company_zipcode");
    $town = $deal_q->f("company_town");
  } elseif (($action == "new") || ($action == "insert")) {
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
  if (isset($deal["priv"])) { $priv = $deal["priv"]; }
  if (isset($deal["datebegin"])) { $datebegin = stripslashes($deal["datebegin"]); }
  if (isset($deal["kind"])) { $kind = $deal["kind"]; }
  if (isset($deal["tasktype"])) { $tt = $deal["tasktype"]; }
  if (isset($contact["category1"])) { $cat1 = $contact["category1"]; }
  if (isset($deal["market"])) { $market = $deal["market"]; }
  if (isset($deal["tech"])) { $tech = $deal["tech"]; }
  if (isset($deal["dateprop"])) { $dateprop = stripslashes($deal["dateprop"]); }
  if (isset($deal["amount"])) { $amount = stripslashes($deal["amount"]); }
  if (isset($deal["hitrate"])) { $hitrate = stripslashes($deal["hitrate"]); }
  if (isset($deal["contact1"])) { $con1 = $deal["contact1"]; }
  if (isset($deal["contact2"])) { $con2 = $deal["contact2"]; }
  if (isset($deal["dateexpected"])) { $dateexpected = stripslashes($deal["dateexpected"]); }
  if (isset($deal["datealarm"])) { $datealarm = stripslashes($deal["datealarm"]); }
  if (isset($deal["status"])) { $status = $deal["status"]; }
  if (isset($deal["timeupdate"])) { $timeupdate = $deal["timeupdate"]; }
  if (isset($deal["archive"])) { $archive = ($deal["archive"] == 1 ? "checked" : ""); }
  if (isset($deal["todo"])) { $todo = stripslashes($deal["todo"]); }
  if (isset($deal["com"])) { $comment = stripslashes($deal["com"]); }
  if (isset($deal["add_comment"])) { $add_comment = stripslashes($deal["add_comment"]); }
  if (isset($deal["usercomment"])) { $usercomment = $deal["usercomment"]; }
  if (isset($deal["datecomment"])) { $datecomment = $deal["datecomment"]; }
  if (isset($deal["mail_comment"])) { $mail_comment = $deal["mail_comment"]; }
  if (isset($deal["company_name"])) { $comp = stripslashes($deal["company_name"]); }
  if (isset($deal["company_ad1"])) { $ad1 = stripslashes($deal["company_ad1"]); }
  if (isset($deal["company_zip"])) { $zip = stripslashes($deal["company_zip"]); }
  if (isset($deal["company_town"])) { $town = stripslashes($deal["company_town"]); }
  if (isset($deal["ext_target"])) { $ext_target = $deal["ext_target"]; }
 
  // Conditionnal fields
  // show category1

  // Kind select construction
  $sel_kind = "<select name=\"sel_kind\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($kind_q->next_record()) {
    $kid = $kind_q->f("dealtype_id");
    $klabel = $kind_q->f("dealtype_label");
    $sel_kind .= "\n<option value=\"$kid\"";
    if ($kind == $kid) $sel_kind .= " selected = \"selected\""; 
    $sel_kind .= ">$klabel</option>";
  }
  $sel_kind .= "</select>";

  // Task type select construction
  $sel_tt = "<select name=\"sel_tt\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($tt_q->next_record()) {
    $tt_id = $tt_q->f("tasktype_id");
    $tt_label = $tt_q->f("tasktype_label");
    $sel_tt .= "\n<option value=\"$tt_id\"";
    if ($tt == $tt_id) $sel_tt .= "selected = \"selected\""; 
    $sel_tt .= ">$tt_label</option>";
  }
  $sel_tt .= "</select>";
 
  // Marketing manager select construction
  $sel_market = "<select name=\"sel_market\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($usrc_q->next_record()) {
    $cid = $usrc_q->f("userobm_id");
    $cname = $usrc_q->f("userobm_lastname") . " " .
             $usrc_q->f("userobm_firstname");
    $sel_market .= "\n<option value=\"$cid\"";
    if ($market == $cid) $sel_market .= " selected = \"selected\""; 
    $sel_market .= ">$cname</option>";
  }
  $sel_market .= "</select>";

  // Technical manager select construction
  $sel_tech = "<select name=\"sel_tech\">
    <option value=\"$c_undef\">$l_undef</option>";
  while ($usrp_q->next_record()) {
    $cid = $usrp_q->f("userobm_id");
    $cname = $usrp_q->f("userobm_lastname") . " " .
             $usrp_q->f("userobm_firstname");
    $sel_tech .= "<option value=\"$cid\"";
    if ($tech == $cid) $sel_tech .= " selected = \"selected\""; 
    $sel_tech .= ">$cname</option>\n";
  }
  $sel_tech .= "</select>";

  // contact 1 select construction
  $sel_con1 = "<select name=\"sel_contact1\">";
  if(is_object($con_q)){
    while ($con_q->next_record()) {
      $cid = $con_q->f("contact_id");
      $cinfo = $con_q->f("contact_lastname") . " " .
               $con_q->f("contact_firstname") . " - " .
               $con_q->f("contact_phone");
      $sel_con1 .= "<option value=\"$cid\"";
      if ($con1 == $cid) $sel_con1 .= "  selected = \"selected\""; 
      $sel_con1 .= ">$cinfo</option>\n";
    }
   }
  $sel_con1 .= "</select>";

  // contact 2 select construction
  if(is_object($con_q)){
      if ($con_q->nf()>0) {
      $con_q->seek(0);
    }
  }
  $sel_con2 = "<select name=\"sel_contact2\">";
  if(is_object($con_q)){
    while ($con_q->next_record()) {
      $cid = $con_q->f("contact_id");
      $cinfo = $con_q->f("contact_lastname") . " " .
               $con_q->f("contact_firstname") . " - " .
               $con_q->f("contact_phone");
      $sel_con2 .= "<option value=\"$cid\"";
      if ($con2 == $cid) $sel_con2 .= " selected = \"selected\""; 
      $sel_con2 .= ">$cinfo</option>\n";
    }
  }
  $sel_con2 .= "</select>";

  // Category select field
  $block_category1 = of_category_dis_entity_form($cats1, "deal", "category1");

  // status select construction
  $sel_status = "<select name=\"sel_status\" onChange=\"auto_set_hitrate(this, this.form.tf_hitrate,this.selectedIndex);\">";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $slabel = $sta_q->f("dealstatus_label");
    $sel_status .= "<option value=\"$sid\"";
    if ($status == $sid) $sel_status .= " selected = \"selected\""; 
    $sel_status .= ">$slabel</option>\n";
  }
  $sel_status .= "</select>";

  $sta_q->seek(0);
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $shitrate = $sta_q->f("dealstatus_hitrate");
    if (($shitrate != "") && (($shitrate >= 0) && ($shitrate <= 100))) {
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
  if ($usrc_q->nf()>0) {
    $usrc_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usrc_q->next_record()) {
    $cid = $usrc_q->f("userobm_id");
    $cname = $usrc_q->f("userobm_lastname")." ".$usrc_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  // Mail comment radio
  if (($mail_comment == "") || ($mail_comment == "$l_no")) {
    $rd_mail_no_c = "checked";
  } else if ($mail_comment == "$l_members") {
    $rd_mail_members_c = "checked";
  } else if ($mail_comment == "$l_group") {
    $rd_mail_group_c = "checked";
  }

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
  }

  if ( ($action == "new") || ($action == "insert") ||
       ( (($action == "detailupdate") || ($action == "update")) && 
         ($usercreate == $uid) ) ) {
     $dis_button1 = "
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert \" />";
    $dis_priv = "
             <td class=\"detailLabel\">$l_private</td>
             <td class=\"detailForm\"><input name=\"cb_priv\" type=\"checkbox\" value=\"1\" ";
    if ($priv == 1) $dis_priv .= "checked = \"checked\"";
    $dis_priv .= " /></td></tr><tr>";
  }

  if ($action == "detailupdate") {
    $dis_contact_2 = "<a href=\"".url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con2")."\">
             <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[details]\" />
             </a>";
    $dis_contact_1 = "<a href=\"" . url_prepare("$path/contact/contact_index.php?action=detailconsult&amp;param_contact=$con1")."\">
             <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_contact\" alt=\"[details]\" />
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
  }
 
  else {
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
    $js_status
    <form method=\"post\" name=\"f_entity\" 
      onsubmit=\"if (check_form(this)) return true; else return false;\"
      action=\"".url_prepare("deal_index.php")."\">

  <div class=\"detailHead\">$l_company</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" . url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id") . "\">
        <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
        $comp
      </td>
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
      $dis_priv
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
      <td class=\"detailLabel\">$l_tt</td>
      <td class=\"detailForm\">$sel_tt</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_marketing_manager</td>
      <td class=\"detailForm\">$sel_market</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_technical_manager</td>
      <td class=\"detailForm\">$sel_tech</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_date_proposal</td>
      <td class=\"detailForm\"><input name=\"tf_dateprop\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$dateprop\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_amount</td>
      <td class=\"detailForm\"><input name=\"tf_amount\" type=\"text\" size=\"10\" value=\"$amount\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_hitrate</td>
      <td class=\"detailForm\"><input name=\"tf_hitrate\" type=\"text\" size=\"3\" value=\"$hitrate\" /> %</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_date_expected</td>
      <td class=\"detailForm\"><input name=\"tf_dateexpected\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$dateexpected\" /></td>
    </tr><tr>
     $block_category1
    </table>    
   </table>

  <div class=\"detailHead\">$l_infos</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_date_alarm</td>
      <td class=\"detailForm\"><input name=\"tf_datealarm\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datealarm\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_status</td>
      <td class=\"detailForm\">$sel_status</td>
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
      <td class=\"detailLabel\">$l_mail_comment</td>
      <td class=\"detailForm\">
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group
      </td>
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
// Display the Deal quick form
// Parameters:
//   - $deal[]    : default or transmitted values
//     keys used  : company_id, name, company
///////////////////////////////////////////////////////////////////////////////
function dis_deal_quick_form($deal) {
  global $display, $uid, $action;

  if ($deal["id"] > 0) {
    $deal_q = run_query_deal_detail($deal["id"]);
    $display["detailInfo"] = display_record_info($deal_q);
    $usrc_q = run_query_all_users_from_group($cg_com, array($uid));
    $sta_q = run_query_dealstatus();
    $block = html_deal_quick_form($action, $deal_q, $usrc_q, $sta_q, $deal);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Deal Form                   
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the deal (null for new deal)
//   - $usrc_q    : DBO : list of userobm from system group Commercial
//   - $sta_q     : DBO : list of deal status
//   - deal[]     : default or transmitted values
//     keys used  : id, usercreate, num, label
///////////////////////////////////////////////////////////////////////////////
function html_deal_quick_form($action, $deal_q, $usrc_q, $sta_q, $deal) {
  global $c_php_isodate_format, $l_update;
  global $l_deal, $l_infos, $l_label, $l_last_update;
  global $l_date_expected, $l_date_alarm, $l_status, $l_hitrate, $l_todo;
  global $l_comment,$l_add_comment,$l_upd_comment, $l_mail_comment;
  global $l_no, $l_members, $l_group;
  global $path, $display, $action, $auth;

  $uid = $auth->auth["uid"];

  $id = $deal_q->f("deal_id");
  $label = $deal_q->f("deal_label");

  // if update mode and first time, values are taken from db
  if ($action == "quick_detail") {
    $dateexpected = isodate_format($deal_q->f("dateexpected"), 1);
    $datealarm = isodate_format($deal_q->f("datealarm"), 1);
    $status = $deal_q->f("deal_status_id");
    $hitrate = $deal_q->f("deal_hitrate");
    $todo = $deal_q->f("deal_todo");
    $comment = $deal_q->f("deal_comment");
    $usercomment = $uid;
    $datecomment = isodate_format();
  } elseif ($action == "quick_update") {
    $usercomment = $uid;
    $datecomment = isodate_format();
  }

  // If parameters have been given, they supercede the default action value
  if (isset($deal["datealarm"])) { $datealarm = stripslashes($deal["datealarm"]); }
  if (isset($deal["dateexpected"])) { $dateexp = stripslashes($deal["dateexpected"]); }
  if (isset($deal["status"])) { $status = $deal["status"]; }
  if (isset($deal["hitrate"])) { $hitrate = stripslashes($deal["hitrate"]); }
  if (isset($deal["todo"])) { $todo = stripslashes($deal["todo"]); }
  if (isset($deal["com"])) { $comment = stripslashes($deal["com"]); }
  if (isset($deal["add_comment"])) { $add_comment = stripslashes($deal["add_comment"]); }
  if (isset($deal["usercomment"])) { $usercomment = $deal["usercomment"]; }
  if (isset($deal["datecomment"])) { $datecomment = $deal["datecomment"]; }
  if (isset($deal["mail_comment"])) { $mail_comment = $deal["mail_comment"]; }
 
  // status select construction
  $sel_status = "<select name=\"sel_status\" onChange=\"auto_set_hitrate(this, this.form.tf_hitrate,this.selectedIndex);\">";
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $slabel = $sta_q->f("dealstatus_label");
    $sel_status .= "<option value=\"$sid\"";
    if ($status == $sid) $sel_status .= " selected = \"selected\""; 
    $sel_status .= ">$slabel</option>\n";
  }
  $sel_status .= "</select>";

  $sta_q->seek(0);
  while ($sta_q->next_record()) {
    $sid = $sta_q->f("dealstatus_id");
    $shitrate = $sta_q->f("dealstatus_hitrate");
    if (($shitrate != "") && (($shitrate >= 0) && ($shitrate <= 100))) {
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
  if ($usrc_q->nf()>0) {
    $usrc_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usrc_q->next_record()) {
    $cid = $usrc_q->f("userobm_id");
    $cname = $usrc_q->f("userobm_lastname")." ".$usrc_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  // Mail comment radio
  if (($mail_comment == "") || ($mail_comment == "$l_no")) {
    $rd_mail_no_c = "checked";
  } else if ($mail_comment == "$l_members") {
    $rd_mail_members_c = "checked";
  } else if ($mail_comment == "$l_group") {
    $rd_mail_group_c = "checked";
  }

  $display["title"] = "<div class=\"title\">$l_deal : $label</div>";
 
  $block = "
    $js_status
    <form method=\"post\" name=\"f_entity\" 
      onsubmit=\"if (check_quick_form(this)) return true; else return false;\"
      action=\"".url_prepare("deal_index.php")."\">

  <div class=\"detailHead\">$l_infos</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_date_alarm</td>
      <td class=\"detailForm\"><input name=\"tf_datealarm\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datealarm\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_status</td>
      <td class=\"detailForm\">$sel_status</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_todo</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_todo\" size=\"60\" maxlength=\"128\" value=\"$todo\" /></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_hitrate</td>
      <td class=\"detailForm\"><input name=\"tf_hitrate\" type=\"text\" size=\"3\" value=\"$hitrate\" /> %</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date_expected</td>
      <td class=\"detailForm\"><input name=\"tf_dateexpected\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$dateexp\" /></td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\"><input name=\"tf_datecomment\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$datecomment\" />$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailLabel\">$l_mail_comment</td>
      <td class=\"detailForm\">
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_no\" $rd_mail_no_c />$l_no
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_members\" $rd_mail_members_c />$l_members
        <input name=\"rd_mail_comment\" type=\"radio\" value=\"$l_group\" $rd_mail_group_c />$l_group
      </td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"8\" cols=\"78\">$add_comment</textarea></td>
    </tr><tr>
      <td class=\"detailLabel\">$l_upd_comment</td>
      <td>&nbsp;</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_com\" rows=\"8\" cols=\"78\">$comment</textarea></td>
    </tr>
    </table>

    <div class=\"detailButton\">
      <p class=\"detailButtons\">
        <input type=\"hidden\" name=\"action\" value=\"quick_update\" />
        <input type=\"hidden\" name=\"param_deal\" value=\"$id\" />
        <input type=\"submit\" value=\"$l_update\" />
      </p>   
    </div>
    </form>
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the deal delete validation screen
// Parameters:
//   - $p_id : deal id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_deal($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("deal_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_deal\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_deal\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
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
// Display The Deal potential block
// Parameters:
//   - $deal[] : hash values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_potential($deal) {
  global $display, $set_theme, $l_total;
  global $l_deal, $l_forecast, $l_deal_total, $l_module_deal, $l_deal_balanced;
  global $l_marketing_manager, $l_technical_manager, $l_help_deal_forecast;

  $display["title"] = "<div class=\"title\">$l_deal : $l_forecast</div>";

  if (is_array($deal["manager"])) {
    $users_id = $deal["manager"];
  } else if ($deal["manager"] != "") {
    $users_id = array($deal["manager"]);
  } else {
    $users_id = "";
  }

  $cpt = 1;
  $potentials = run_query_deal_potential($users_id);
  if (is_array($potentials["market"])) {
    foreach($potentials["market"] as $u_id => $pu) {
      if ($u_id != "0") {
	$cpt++;
	$data = "data" . ($cpt % 2);
	$amount = number_format($pu["amount"]);
	$balanced = number_format($pu["amount_balanced"]);
	$nb_potential = $pu["number"];
	$user = $pu["name"];
	$dis_market_pot .= "
      <tr class=\"$data\">
        <td>$user</td>
        <td style=\"text-align: center;\">$nb_potential</td>
        <td style=\"text-align: right;\">$amount</td>
        <td style=\"text-align: right;\">$balanced</td>
      </tr>";
      }
    }

    $m_amount = number_format($potentials["market"]["0"]["amount"]);
    $m_balanced = number_format($potentials["market"]["0"]["amount_balanced"]);
    $m_nb_potential = $potentials["market"]["0"]["number"];
  }

  if (is_array($potentials["tech"])) {
    foreach($potentials["tech"] as $u_id => $pu) {
      if ($u_id != "0") {
	$cpt++;
	$data = "data" . ($cpt % 2);
	$amount = number_format($pu["amount"]);
	$balanced = number_format($pu["amount_balanced"]);
	$nb_potential = $pu["number"];
	$user = $pu["name"];
	$dis_tech_pot .= "
      <tr class=\"$data\">
        <td>$user</td>
        <td style=\"text-align: center;\">$nb_potential</td>
        <td style=\"text-align: right;\">$amount</td>
        <td style=\"text-align: right;\">$balanced</td>
      </tr>";
      }
    }

    $t_amount = number_format($potentials["tech"]["0"]["amount"]);
    $t_balanced = number_format($potentials["tech"]["0"]["amount_balanced"]);
    $t_nb_potential = $potentials["tech"]["0"]["number"];
  }

  $dis_potential = "
    <table class=\"resultTable\">
    <tr>
      <td class=\"resultHead\">$l_marketing_manager</td>
      <td class=\"resultHead\"># $l_module_deal</td>
      <td class=\"resultHead\">$l_deal_total</td>
      <td class=\"resultHead\">$l_deal_balanced</td>
    </tr>
    $dis_market_pot
    <tr class=\"data2\">
      <td>$l_total</td>
      <td style=\"text-align: center;\">$m_nb_potential</td>
      <td style=\"text-align: right;\">$m_amount</td>
      <td style=\"text-align: right;\">$m_balanced</td>
    </tr>
    </table>
    <p />
    <table class=\"resultTable\">
    <tr>
      <td class=\"resultHead\">$l_technical_manager</td>
      <td class=\"resultHead\"># $l_module_deal</td>
      <td class=\"resultHead\">$l_deal_total</td>
      <td class=\"resultHead\">$l_deal_balanced</td>
    </tr>
    $dis_tech_pot
    <tr class=\"data2\">
      <td>$l_total</td>
      <td style=\"text-align: center;\">$t_nb_potential</td>
      <td style=\"text-align: right;\">$t_amount</td>
      <td style=\"text-align: right;\">$t_balanced</td>
    </tr>
    </table>";

  if (count($deals) > 0) {
    foreach ($deals as $status => $nb) {
      $dis_status .= "
    <tr>
      <td>$status</td>
      <td class=\"number\">$nb</td>
    </tr>";
    }
  }

  $msg = display_info_msg($l_help_deal_forecast);

  $block = "$msg
    $dis_potential
  ";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display The Deal stats detail
// Parameters:
//   - $deal[] : hash values
///////////////////////////////////////////////////////////////////////////////
function dis_deal_stats($deal) {
  global $display, $set_theme, $uid;
  global $l_module_deal;

  if (is_array($deal["manager"])) {
    $users_id = $deal["manager"];
  } else if ($deal["manager"] != "") {
    $users_id = array($deal["manager"]);
  } else {
    $users_id = "";
  }

  $block .= "
  <div class=\"detailHead\">$l_module_deal</div>";

  $deal_q = run_query_deal_stats($deal, $users_id);
  $prefs = get_display_pref($uid, "deal", 0);

  $url = url_prepare("deal_index.php?action=stats&amp;sel_manager=".$deal["manager"]);
    
  $dis_deal = new OBM_DISPLAY("DATA", $prefs, "deal");
  $dis_deal->data_set = $deal_q;
  $dis_deal->data_url = $url;
  $dis_deal->display_entity = "deal";
  $dis_deal->data_order = true;
  $dis_deal->data_header = "both";
  $block .= $dis_deal->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Deal Administration Forms
// Parametres:
//   - $kind_q   : DBO : Deal kind list
//   - $status_q : DBO : Deal Status list
///////////////////////////////////////////////////////////////////////////////
function html_deal_admin_form($kind_q, $status_q) {
   // Labels : 
  global $l_kind_manage,$l_kind_new,$l_kind_exist;
  global $l_status_manage,$l_status_exist,$l_status_new;
  global $l_label,$l_minus,$l_plus,$l_order;
  // Actions : 
  global $l_kind_checkdelete, $l_kind_update, $l_kind_insert;
  global $l_status_checkdelete,$l_status_update,$l_status_insert, $l_hitrate;
  global $cgp_hide;

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
  $sel_sta = "<select name=\"sel_status\" onChange=\"
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
          <input type=\"hidden\" name=\"sel_status\" value=\"\" />
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

    </td>
  </tr>
  </table>";

  if (! $cgp_hide["deal"]["category1"]) {
    $cats1 = of_category_get_ordered("deal", "category1");
    $block .= "<hr />";
    $block .= of_html_admin_cat_form($cats1, "deal", "category1");
  }


  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Displays the kind links
// Parameters:
//   - $id : kind id
///////////////////////////////////////////////////////////////////////////////
function dis_kind_links($id) {
  global $display, $l_kind_link_deal, $l_kind_link_deal_no, $l_back;
  global $l_kind_can_delete, $l_kind_cant_delete, $l_kind_delete;

  $delete_ok = true;
  $label = get_kind_label($id);
  $obm_q = run_query_kind_links($id);

  $nb_kind = $obm_q->num_rows();
  if ($nb_kind > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_kind_link_deal ($nb_kind)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_kind) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
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

  $nb_stat = $obm_q->num_rows();
  if ($nb_stat > 0) {
    $delete_ok = false;
    $dis_link = "
    <tr>
      <td class=\"detailHead\">$label : $l_status_link_deal ($nb_stat)</td>
    </tr>";

    $cpt = 0;
    while ( ($obm_q->next_record()) && ($cpt < 10) ) {
      $cpt++;
      $did = $obm_q->f("deal_id");
      $dlabel = $obm_q->f("deal_label");
      $dis_link.= "
    <tr>
      <td class=\"detailLabel\">
        <a href=\"" .url_prepare("deal_index.php?action=detailconsult&amp;param_deal=$did") . "\">$dlabel</a>
      </td>
    </tr>";
    }
    if ($cpt < $nb_stat) {
      $dis_link .= "
    <tr>
      <td class=\"detailLabel\">...</td>
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
        method=\"post\" action=\"" . url_prepare("deal_index.php") . "\">
      <input type=\"hidden\" name=\"action\" value=\"status_delete\" />
      <input type=\"hidden\" name=\"sel_status\" value=\"$id\" />
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
// Display the Deal Display preference screen
// Parameters:
//   - $prefs        : Deal Display preferences
//   - $prefs_parent : Parent Deal Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_deal_display_pref($prefs, $prefs_parent) {
  global $l_deal_display, $l_parentdeal_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs);
  $dis_pref->display_entity = "deal";
  $dis_pref->pref_title = $l_deal_display;

  $block = "
    <table class=\"detail\">
    <tr>
      <td>";
  $block .= $dis_pref->display();
  $block .= "
    </td><td>";

  $dis_pref->display_pref = $prefs_parent;
  $dis_pref->display_module = "deal";
  $dis_pref->display_entity = "parentdeal";
  $dis_pref->pref_title = $l_parentdeal_display;

  $block .= $dis_pref->display();
  
  $block .= "</td></tr></table>";
  $block .= $dis_pref->dis_pref_help();

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
function dis_parentdeal_search_list($deal) {
  global $l_no_found_parent;
  global $display, $auth;

  $new_order = $deal["new_order"];
  $order_dir = $deal["order_dir"];

  $obm_q = run_query_search_parentdeal($deal, $new_order, $order_dir);
  
  $nb_parent = $obm_q->num_rows_total();
  if ($nb_parent == 0) {
    $display["msg"] .= display_warn_msg($l_no_found_parent);
  } else {
    $prefs = get_display_pref($auth->auth["uid"], "parentdeal");
    $block = html_parentdeal_search_list($obm_q, $prefs, $nb_parent, $deal);
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
function html_parentdeal_search_list($obm_q, $prefs, $nb_parentdeals, $deal) {
  global $display, $l_found_parent; 

  $plabel = urlencode(stripslashes($deal["plabel"]));
  $pmanager = $deal["pmanager"];
  $parchive = $deal["parchive"];
  $display["msg"] .= display_info_msg($nb_parentdeals." ".$l_found_parent);

  $url = url_prepare("deal_index.php?action=parent_search&amp;tf_plabel=$plabel&amp;sel_pmanager=$pmanager&amp;cb_parchive=$parchive");

  $parentdeal_d = new OBM_DISPLAY("DATA", $prefs, "deal");
  $parentdeal_d->data_set = $obm_q;
  $parentdeal_d->data_url = $url;
  $parentdeal_d->data_header = "both";
  $parentdeal_d->data_idfield = "parentdeal_id"; 
  $parentdeal_d->data_set->seek($first_row);
  $block = $parentdeal_d->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Detail
// Parameters :
//   - deal[]    : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
///////////////////////////////////////////////////////////////////////////////
function dis_parentdeal_consult($deal) {
  global $ctt_sales, $uid;

  $new_order = $deal["new_order"];
  $order_dir = $deal["order_dir"];
  $archive = $deal["archive"];

  $obm_q = run_query_deal_parentdeal_detail($deal["parent"]);
  $prefs = get_display_pref ($uid, "deal");
  $deal_q = run_query_search($deal, 0, $new_order, $order_dir);
  $num_rows = $deal_q->num_rows();
  $kind_q = run_query_dealtype();
  $tt_q = run_query_tasktype($ctt_sales);
  $cat_q = run_query_dealcategory1();
  $status_q = run_query_dealstatus();
  $usr_q = run_query_deal_manager($archive);

  $block = html_parentdeal_consult($obm_q, $deal_q, $deal, $prefs, $num_rows, $kind_q, $tt_q, $cat_q, $status_q, $usr_q);

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
//   - $kind_q   : list of deal types available
//   - $tt_q     : list of deal tasktype available
//   - $status_q : list of deal status available 
//   - $usr_q    : DBO : list of userobm
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_consult($parent_q,$deal_q,$deal,$prefs,$nb_deals,$kind_q,$tt_q,$cat_q,$status_q,$usr_q) {
  global $display, $path;
  global $l_parentdeal_deals,$l_label,$l_technical_manager;
  global $l_parentdeal,$l_date_proposal,$l_date_alarm,$l_amount_in,$l_amount_out,$l_potential,$l_comment,$l_marketing_manager,$l_status;
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
	  <td class=\"detailLabel\">$l_status :</td>
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
  $block .= html_deal_search_form($deal, $kind_q, $tt_q, $cat_q, $status_q, $usr_q);

  //-----> deal list
  // if no record found, display it and stop
  if ($nb_deals == 0) {
    return $block;
  }

  $label = urlencode(stripslashes($deal["label"]));
  $name = urlencode(stripslashes($deal["company_name"]));
  $archive = $deal["archive"];
  $kind = $deal["kind"];
  $tt = $deal["tasktype"];
  $status = $deal["status"];
  $manager = $deal["manager"];
  $dateafter = stripslashes($deal["dateafter"]);
  $datebefore = stripslashes($deal["datebefore"]);

  $url = url_prepare("deal_index.php?action=search&amp;tf_label=$label&amp;tf_company_name=$name&amp;cb_archive=$archive&amp;sel_kind=$kind&amp;sel_status=$status&amp;sel_tt=$tt&amp;sel_manager=$manager&amp;tf_dateafter=$dateafter&amp;tf_datebefore=$datebefore&amp;param_parent=$parent");

  // Display the list of deals attached to the parentdeal
  $dis_list_deal = new OBM_DISPLAY("DATA", $prefs, "deal");
  $dis_list_deal->data_set = $deal_q;
  $dis_list_deal->data_url = $url;
  $dis_list_deal->data_header = "both";
  $dis_list_deal->data_idfield = "deal_id"; 
  $dis_list_deal->data_set->seek($first_row);

  $block .= $dis_list_deal->display("dis_data_deal");

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Parent Deal Form
// Parameters :
//   - $action   : action called
//   - $deal[]   : default or transmitted values
//     keys used : forwarded to html_deal_search_form and $url for OBM_DISPLAY
///////////////////////////////////////////////////////////////////////////////
function dis_parentdeal_form($action, $deal) {
  global $display, $ctt_sales, $cg_com, $cg_prod, $uid, $l_no_found;

  $new_order = $deal["new_order"];
  $order_dir = $deal["order_dir"];
  $users = array();

  if ($deal["parent"] > 0) {
    $obm_q = run_query_deal_parentdeal_detail($deal["parent"]);
    if ($obm_q->num_rows() != 1) {
      $display["msg"] .= display_err_msg($l_no_found);
      return;
    } else {
      $users = array($uid, $obm_q->f("parentdeal_marketingmanager_id"), $obm_q->f("parentdeal_technicalmanager_id"));
      $display["detailInfo"] = display_record_info($obm_q);
    }
  }

  $usrc_q = run_query_all_users_from_group($cg_com, $users);
  $usrp_q = run_query_all_users_from_group($cg_prod, $users);
  $block = html_parentdeal_form($action,$obm_q, $usrc_q, $usrp_q, $deal);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Parent Deal Form
// Parameters :
//   - $action    : action called
//   - $deal_q    : DBO : information about the parent deal (null for new deal)
//   - $usrc_q    : DBO : OBM user list from system group Commercial
//   - $usrp_q    : DBO : OBM user list from system group Production
//   - deal[]     : default or transmitted values
//     keys used  : parent_id, plabel, pmarket, ptech, pcom
///////////////////////////////////////////////////////////////////////////////
function html_parentdeal_form($action, $deal_q, $usrc_q, $usrp_q, $deal) {
  global $l_update_parent,$l_insert_parent,$l_label,$l_marketing_manager;
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
  while ($usrc_q->next_record()) {
    $sel_market .= "<option value=\"" . $usrc_q->f("userobm_id") . "\"";
    if ($usrc_q->f("userobm_id") == $pmarket) $sel_market .= " selected= \" selected \"";
    $sel_market .= ">" . $usrc_q->f("userobm_lastname") . " " . $usrc_q->f("userobm_firstname")."</option>";
  }
  $sel_market .= "</select>";

  // Technical manager select
  $sel_tech = "<select name=\"sel_ptech\">";
  while ($usrp_q->next_record()) {
    $sel_tech .= "<option value=\"" . $usrp_q->f("userobm_id") . "\"";
    if ($usrp_q->f("userobm_id") == $ptech) $sel_tech .= "selected= \" selected \"";
    $sel_tech .= ">" . $usrp_q->f("userobm_lastname") . " " . $usrp_q->f("userobm_firstname")."</option>";
  }
  $sel_tech .= "</select>";

  if (($action == "parent_detailupdate") || ($action == "parent_update")) {
    $dis_button = "
        <input type=\"hidden\" name=\"action\" value=\"parent_update\" />
        <input type=\"hidden\" name=\"param_parent\" value=\"$parent\" />
        <input type=\"submit\" value=\"$l_update_parent\" />
         ";
  } else {
     $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"parent_insert\" />
      <input type=\"submit\" value=\"$l_insert_parent\" />";
  }

  $block = "
  <form method=\"post\" name=\"f_entity\"
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


</script>
