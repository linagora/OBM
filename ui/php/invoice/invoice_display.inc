<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_display.inc
//     - Desc : Invoice Display file
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["invoice_number"] = $l_number;
$fieldnames["invoice_company"] = $l_company;
$fieldnames["invoice_deal"] = $l_deal;
$fieldnames["invoice_project"] = $l_project;
$fieldnames["invoice_label"] = $l_label;
$fieldnames["invoice_amount_ht"] = $l_amount_ht;
$fieldnames["invoice_amount_ttc"] = $l_amount_ttc;
$fieldnames["invoice_status"] = $l_status;
$fieldnames["invoice_date"] = $l_date;
$fieldnames["invoice_paid"] = $l_paid;
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_amount"] = $l_amount;


///////////////////////////////////////////////////////////////////////////////
// Display Invoice specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_invoice(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_frs, $col_client;

  if ($fieldname == "invoice_deal") {
    $res["url"] = "$path/deal/deal_index.php?action=detailconsult&amp;param_deal=".$OD->data_set->f("deal_id");
  }

  elseif ($fieldname == "invoice_project") {
    $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;param_project=".$OD->data_set->f("project_id");
  }

  elseif ($fieldname == "invoice_label") {
    $res["url"] = "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$OD->data_set->f("invoice_id");
  }

  elseif ($fieldname == "invoice_company") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
  }
  
  elseif ($fieldname == "invoice_pay_label") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$OD->data_set->f("invoice_payment_id");
  }

  // amount column color 
  elseif (($fieldname == "invoice_amount_ht") 
	  || ($fieldname == "invoice_amount_ttc")
	  || ($fieldname == "invoice_paid")) {
    $amount = $OD->data_set->f($fieldname);
    if ($OD->data_set->f("invoice_inout") == '+') {
      $couleur = $col_client;
    } else {
      $couleur = $col_frs;
    }
    $res["name"] = "$amount";
  }

  else if ($fieldname == "invoice_date") {
    $res["name"] = date_format($OD->data_set->f("date"));
  }

  else if ($fieldname == "payment_date") {
    $res["name"] = date_format($OD->data_set->f("payment_date"));
  }

  else if ($fieldname == "payment_expect_date") {
    $res["name"] = date_format($OD->data_set->f("payment_expect_date"));
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Invoice search form
// Parameters:
//   - $invoice[] : hash with invoice values
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_search_form($invoice="") {

  $sta_q = run_query_invoicestatus();
  $block .= html_invoice_search_form($sta_q, $invoice);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// invoice search Form
// Parameters :
//  $sta_q   : DBO : status info
//  $invoice : invoice hash parameters
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_form($sta_q, $invoice) {
  global $c_all, $l_all_f;
  global $l_label_start, $l_number, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_deal, $l_date_apres, $l_date_avant, $l_inout;
  global $l_deal, $l_company, $l_include_archive;
  global $l_find, $l_both, $l_client, $l_supplier;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = $invoice["deal"];
  $deal_id = $invoice["param_deal"];
  $company = $invoice["company"];
  $archive = ($invoice["archive"]) ? "checked" : "" ;

  $url_form = url_prepare("invoice_index.php?action=search");

  $sel_status = "<select name=\"sel_status\">
     <option value=\"$c_all\">$l_all_f</option>";
  while ($sta_q->next_record()) {
    $s_id = $sta_q->f("invoicestatus_id");
    $s_label = $sta_q->f("invoicestatus_label");
    $sel_status .= "<option value=\"$s_id\"";
    if ($status == $s_id) $sel_status .= " selected = \"selected\"";
    $sel_status .= ">$s_label</option>";
  }
  $sel_status .= "</select>";

  if (($inout == "$c_all") || ($inout == "")) {
    $rb_both = "checked";
  }
  if ($inout == "+") {
    $rb_client = " checked ";
  }
  if ($inout == "-") {
    $rb_four = " checked ";
  }

  $block = "
    <form method=\"get\" name=\"form_invoice\"
        onSubmit=\"if (check_search_form(this) == false) return false;
        else return true;\" action=\"$url_form\">
      <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />

    <div class=\"search\">

    <div class=\"searchLabel\">$l_label_start<br />
      <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"$label\" />
    </div>

    <div class=\"searchLabel\">$l_number<br />
      <input type=\"text\" name=\"tf_number\" size=\"12\" value=\"$number\" />
    </div>

    <div class=\"searchLabel\">$l_amount_ht<br />
      <input type=\"text\" name=\"tf_amount_ht\" size=\"15\" value=\"$ht\" />
    </div>

    <div class=\"searchLabel\">$l_amount_ttc<br />
      <input type=\"text\" name=\"tf_amount_ttc\" size=\"15\" value=\"$ttc\" />
    </div>

    <div class=\"searchLabel\">$l_status<br />
      $sel_status
    </div>

    <div class=\"searchLabel\">$l_date_apres<br />
      <input type=\"text\" name=\"tf_date_after\" size=\"12\" value=\"$date_after\" />
    </div>

    <div class=\"searchLabel\">$l_date_avant<br />
      <input type=\"text\" name=\"tf_date_before\" size=\"12\" value=\"$date_before\" />
    </div>

    <div class=\"searchLabel\">$l_include_archive<br />
      <input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive> 
    </div>

    <div class=\"searchLabel\">$l_inout<br />
      <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $rb_both />
      $l_both &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $rb_client />
      $l_client &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $rb_four />
      $l_supplier &nbsp;
    </div>

    <div class=\"searchLabel\">$l_deal<br />
      <input type=\"text\" name=\"tf_deal\" value=\"$deal\" />
    </div>

    <div class=\"searchLabel\">$l_company<br />
      <input type=\"text\" name=\"tf_company\" value=\"$company\" />
    </div>

    <div class=\"searchLabel\">&nbsp;<br>
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
// Display the Invoice search result
// Parameters:
//   - $invoice[] : invoice search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_search_list($invoice) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $pref_q = run_query_display_pref($auth->auth["uid"], "invoice");
  $inv_q = run_query_search($invoice, $new_order, $order_dir);
  $nb_inv = $inv_q->num_rows_total();
  if ($nb_inv == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_invoice_search_list($inv_q,$pref_q,$nb_inv,$invoice);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice search result
// Paratemers:
// $inv_q  : list of invoices
// $pref_q : fields to display
// $nb_invoices : number of invoices
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_list ($inv_q, $pref_q, $nb_invoice, $invoice) {
  global $col_frs, $col_client;
  global $l_found, $l_archive_invoices, $l_archive_update, $l_deal;
  global $l_label, $l_number, $l_amount_ht, $l_amount_ttc, $l_date, $l_status;
  global $display, $perm, $menu, $cright_write_admin;

  $label = urlencode($invoice["label"]);
  $number = urlencode($invoice["number"]);
  $ht = urlencode($invoice["ht"]);
  $ttc = urlencode($invoice["ttc"]);
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = urlencode($invoice["deal"]);
  $company = urlencode($invoice["company"]);
  $archive = $invoice["archive"];

  $url = url_prepare("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_ht=$ht". 
		    "&amp;tf_amount_ttc=$ttc".
		    "&amp;sel_status=$status".
		    "&amp;tf_deal=$deal".
		    "&amp;rd_inout=$inout".
		    "&amp;cb_archive=$archive"); 

  $dis_invoice = new OBM_DISPLAY ("DATA", $pref_q, "invoice");
  $dis_invoice->data_set = $inv_q;
  $dis_invoice->data_header = "top";
  $dis_invoice->data_url = $url;

  // checkbox to allow archiving..
  if ($perm->check_right($menu, $cright_write_admin)) {
    $url_archive = url_prepare("invoice_index.php?action=updatearchive");
    $block .= "<form method=\"post\" action=\"$url_archive\">";
    $dis_invoice->data_cb_text = $l_archive_invoices;
    $dis_invoice->data_idfield = "invoice_id";
    $dis_invoice->data_cb_side = "left";
    $dis_invoice->data_cb_name = "archive_";
  }

  $display["msg"] .= display_info_msg("$nb_invoice $l_found");
  $block .= $dis_invoice->display("dis_data_invoice");
  if ($perm->check_right($menu, $cright_write_admin)) {
    $block .= "<input type=\"submit\" value=\"$l_archive_update\">
      </form>";
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice detail screen
// Parameters:
//   - $invoice[] : invoice hash parameters
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_consult($invoice) {
  global $auth, $display, $l_no_found, $l_query_error;

  $id = $invoice["id"];
  $uid = $auth->auth["uid"];

  $inv_q = run_query_detail($id);
  if ($inv_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg("$l_query_error  - $l_no_found !");
  }
  if ($id > 0) {
    $pay_q = run_query_invoice_payment($id);
    $pref_pay_q = run_query_display_pref($uid, "payment");
    $display["detailInfo"] = display_record_info($inv_q);
    $block = html_invoice_consult($inv_q, $pay_q, $pref_pay_q);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display invoice detail screen
// Parameters:
//   - $inv_q     : DBO invoice info
//   - $pay_q     : DBO : payments for the invoice
//   - pref_pay_q : display pref for payments
///////////////////////////////////////////////////////////////////////////////
function html_invoice_consult($inv_q, $pay_q, $pref_pay_q) {
  global $display, $set_theme;
  global $l_delete, $l_invoice_infos;
  global $l_list_payment, $l_no_payment, $l_payments_todel, $l_invoice_del_payment, $l_del_payment_chosen;
  global $perm, $menu, $cright_write_admin;

  $i_id = $inv_q->f('invoice_id');

  $block = dis_invoice_detail ($inv_q);
  
  //  payments data :
  $block .= "<div class=\"detailHead\">$l_list_payment</div>";

  if ($pay_q->nf() == 0) {
    // no payment
    $block .= "
     <table class=\"detail\">
      <tr>
       <td class=\"detailLabel\">$l_no_payment</td>
      </tr>
     </table>";

  } else {
    $url = url_prepare("invoice_index.php?action=detailconsult&amp;param_invoice=$i_id");

    $dis_payment = new OBM_DISPLAY("DATA", $pref_pay_q, "invoice");
    $dis_payment->data_set = $pay_q;
    $dis_payment->data_header = "top";
    $dis_payment->data_url = $url;
    $dis_payment->data_order = false;
    
    $block .= $dis_payment->display("dis_data_invoice");
  }

  return $block;
}


//////////////////////////////////////////////////////////////////////////////
// XHTML Display invoice detail info part
//////////////////////////////////////////////////////////////////////////////
function dis_invoice_detail($inv_q) {
  global $display;
  global $l_company, $l_deal, $l_project,$l_amount_ht, $l_amount_ttc;
  global $l_date, $l_label, $l_number, $l_inout, $l_supplier, $l_client;
  global $l_status, $l_comment, $l_invoice;

  $label = $inv_q->f("invoice_label");
  $num = $inv_q->f("invoice_number");
  $date = date_format($inv_q->f("date"));
  $status = $inv_q->f("invoicestatus_label");
  $inout = ($inv_q->f("invoice_inout") == '-') ? $l_supplier : $l_client;
  $title = $inout;
  $ht = $inv_q->f("invoice_amount_ht");
  $ttc = $inv_q->f("invoice_amount_ttc");
  $comment = nl2br($inv_q->f("invoice_comment"));
  $company = $inv_q->f("company_name");
  $deal = $inv_q->f("deal_label");
  $project = $inv_q->f("project_name");

  $display["title"] = "<div class=\"title\">$l_invoice : $label</div>";

  $block .= "
    <div class=\"detailHead\">$title</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailText\">$company</td>
    </tr>
    <tr>
      $dis_company
      <td class=\"detailLabel\">$l_deal</td>
      <td class=\"detailText\">$deal</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_project</td>
      <td class=\"detailText\">$project</td>
    </tr>
    </table>
 
    <div class=\"detailHead\">$l_invoice</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailText\">$label</td>
    </tr>
    <tr>
      $dis_company
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$num</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_status</td>
      <td class=\"detailText\">$status</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_inout</td>
      <td class=\"detailText\">$inout</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_amount_ht</td>
      <td class=\"detailText\">$ht</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_amount_ttc</td>
      <td class=\"detailText\">$ttc</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailText\">$comment</td>
    </tr>
    </table>";

  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice Form
// Parameters:
//   - $action    : action called
//   - $invoice[] : invoice hash parameters
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_form($action, $invoice) {
  global $display, $l_no_found;
  global $auth, $new_order, $order_dir;

  $id = $invoice["id"];
  if ($id > 0) {
    $inv_q = run_query_detail($id);
    $display["detailInfo"] = display_record_info($inv_q);
  } else {
    $inv_q = "";
  }
  $stat_q = run_query_invoicestatus();
  $block = html_invoice_form($action, $invoice, $inv_q, $stat_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Invoice Form              
// Parameters:
//   - $action    : action called
//   - $invoice[] : invoice hash parameters
//   - $inv_q     : if updating, contains the invoice current info
//   - $stat_q    : Database result with status list
///////////////////////////////////////////////////////////////////////////////
function html_invoice_form($action, $invoice, $inv_q, $stat_q) {
  global $path, $display, $set_theme, $ico_company, $ico_deal, $ico_project;
  global $l_label, $l_number, $l_comment, $l_deal, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_date, $l_pick_deal, $l_inout, $l_client, $l_supplier;
  global $l_insert, $l_update;
  global $l_invoice, $l_company, $l_deal, $l_project;
  global $default_invoice_numbering;
  global $default_tax, $l_tax_rate, $l_compute_tax; 

  // if update mode and first time values are taken from database
  if ($action == "detailupdate") {
    $id = $inv_q->f("invoice_id");
    $label = $inv_q->f("invoice_label");
    $number = $inv_q->f("invoice_number");
    $comment = $inv_q->f("invoice_comment");
    $invoice_status = $inv_q->f("invoice_status_id");
    $date = $inv_q->f("invoice_date");
    $inout = $inv_q->f("invoice_inout");
    $ht = $inv_q->f("invoice_amount_ht");
    $ttc = $inv_q->f("invoice_amount_ttc");
    $c_id = $inv_q->f("invoice_company_id");
    $c_name = $inv_q->f("company_name");
    $d_id = $inv_q->f("invoice_deal_id");
    $d_label = $inv_q->f("deal_label");
    $p_id = $inv_q->f("invoice_project_id");
    $p_name = $inv_q->f("project_name");

  // New form and first time
  } elseif ($action == "new") {
    $number = date ($default_invoice_numbering);
    // when creating from a deal, default to use same "inout" as the dealtype
    if ($p_deal_linked != "") {
      $inout = run_query_deal_get_inout ($p_deal_linked);
    }
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = "";
    $label .= "-".date("Y-m-d");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($invoice["company_id"])) { $c_id = $invoice["company_id"]; }
  if (isset($invoice["company_name"])) { $c_name = $invoice["company_name"]; }
  if (isset($invoice["comp_new_id"])) { $c_new_id = $invoice["comp_new_id"]; }
  if (isset($invoice["comp_new_name"])) { $c_new_name = $invoice["comp_new_name"]; }
  if (isset($invoice["id"])) { $id = $invoice["id"]; }


  $title = ($inout == '-') ? $l_supplier : $l_client;


  // Status select
  $sel_status = "<select name=\"sel_status\">"; 
  while ($stat_q->next_record()) {
    $s_id = $stat_q->f("invoicestatus_id");
    $s_label = $stat_q->f("invoicestatus_label");
    $sel_status .= "<option value=\"$s_id\""; 
    if ($s_id == $status) {
      $sel_status .= " selected ";
    }
    $sel_status .= ">$s_label</option>"; 
  }
  $sel_status .= "</select>";

  $inout_c = ($inout == "+") ? "checked" : "";
  $inout_s = ($inout == "-") ? "checked" : "";

  $dis_inout = "
      <td class=\"detailLabel\">$l_inout</td>
      <td class=\"detailForm\">
       $l_client <input type=\"radio\" name=\"rd_inout\" value=\"+\" $inout_c />
       $l_supplier <input type=\"radio\" name=\"rd_inout\" value=\"-\" $inout_s />
      </td>";

  // Buttons
  if (($action == "update") || ($action == "detailupdate")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"update\"/>
    <input type=\"hidden\" name=\"param_invoice\" value=\"$id\" />
    <input type=\"hidden\" name=\"hd_rd_inout\" value=\"$inout\">"; 
    $dis_button .= "
      <input type=\"submit\" value=\"$l_update\"
        onClick=\"if (check_invoice (this.form)) return true; else return false;\" />";
  } elseif ($action == "new" || $action == "insert"){
    $dis_button .= "
      <input type=\"submit\" value=\"$l_insert\" 
        onClick=\"if(check_invoice(this.form)) return true ; else return false;\" />";
    $dis_button .="
      <input type=\"hidden\" name=\"action\" value=\"insert\" />";
    $dis_button .= "
      <input type=\"hidden\" name=\"hd_param_deal\" value=\"$p_deal_linked\">";
  }

  $display["title"] = "<div class=\"title\">$l_invoice : $label</div>";

  $block = "
  <form method=\"post\" name=\"f_invoice\" action=\"invoice_index.php\">

  <div class=\"detailHead\">$title</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_company</td>
    <td class=\"detailForm\">
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"param_company\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_invoice.company_new_id&amp;ext_widget_text=f_invoice.company_new_name','Company','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"/images/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_deal</td>
    <td class=\"detailForm\">
      <a href=\"". url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$d_id") . "\">
      $d_label
      </a><input type=\"hidden\" name=\"param_deal\" value=\"$d_id\" />
      <input type=\"hidden\" name=\"deal_label\" value=\"$d_label\" />
      <input type=\"hidden\" name=\"deal_new_id\" value=\"$d_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_invoice.deal_new_id&amp;ext_widget_text=f_invoice.deal_new_label','Deal','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"/images/$set_theme/$ico_deal\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"deal_new_label\" value=\"$d_new_label\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_project</td>
    <td class=\"detailForm\">
      <a href=\"". url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=$d_id") . "\">
      $p_name
      </a><input type=\"hidden\" name=\"param_project\" value=\"$p_id\" />
      <input type=\"hidden\" name=\"project_name\" value=\"$p_name\" />
      <input type=\"hidden\" name=\"project_new_id\" value=\"$p_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/project/project_index.php?action=ext_get_id&amp;popup=1&amp;ext_title=".urlencode($l_contact_select_company)."&amp;ext_widget=f_invoice.project_new_id&amp;ext_widget_text=f_invoice.project_new_name','Project','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"/images/$set_theme/$ico_project\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"project_new_name\" value=\"$p_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_invoice</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_label</td>
    <td class=\"detailForm\">
      <input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_number</td>
    <td class=\"detailForm\">
      <input name=\"tf_number\" type=\"text\" size=\"11\" value=\"$number\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_date</td>
    <td class=\"detailForm\">
      <input name=\"tf_date\" type=\"text\" size=\"10\" maxlength=\"10\" value=\"$date\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_status</td>
    <td class=\"detailForm\">$sel_status</td>
  </tr><tr>
    $dis_inout
  </tr><tr>
    <td class=\"detailLabel\">$l_amount_ht</td>
    <td class=\"detailForm\">
      <input name=\"tf_amount_ht\" type=\"text\" size=\"15\" value=\"$ht\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_tax_rate</td>
    <td class=\"detailForm\"><input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" size=\"8\" />
      <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" onClick=\"compute_tax(this.form);\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_amount_ttc</td>
    <td class=\"detailForm\">
      <input name=\"tf_amount_ttc\" type=\"text\" size=\"15\" value=\"$ttc\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_comment</td>
    <td class=\"detailForm\">
      <textarea name=\"ta_comment\" rows=\"3\" cols=\"64\">$comment</textarea>
    </td>
  </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
  </div>

  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice Display preference screen                             //
// Parameters:
//   - $display_pref_q : DBO : Field list to display
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_display_pref($display_pref_q) {
  global $l_invoice_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $display_pref_q);
  $dis_pref->display_entity = "invoice";
  $dis_pref->pref_title = $l_invoice_display;
  $dis_pref->pref_dis_help = 1;

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


</script>
