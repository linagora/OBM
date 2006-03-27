<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_display.inc
//     - Desc : Invoice Display file
// 2001-07-30 - Aliacom - Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["invoice_archive"] = $l_archive;
$fieldnames["invoice_number"] = $l_number;
$fieldnames["invoice_company"] = $l_company;
$fieldnames["invoice_deal"] = $l_deal;
$fieldnames["invoice_project"] = $l_project;
$fieldnames["invoice_label"] = $l_label;
$fieldnames["invoice_amount_ht"] = $l_amount_ht;
$fieldnames["invoice_amount_ttc"] = $l_amount_ttc;
$fieldnames["invoice_status"] = $l_status;
$fieldnames["invoice_inout"] = $l_inout;
$fieldnames["invoice_date"] = $l_date;
$fieldnames["invoice_expiration_date"] = $l_expiration_date;
$fieldnames["invoice_payment_date"] = $l_payment_date;
$fieldnames["invoice_paid"] = $l_paid;
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_amount"] = $l_amount;

///////////////////////////////////////////////////////////////////////////////
// Display Invoice specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_invoice(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_frs, $col_client, $l_supplier, $l_client;

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

  elseif ($fieldname == "invoice_inout") {
    if ($OD->data_set->f($fieldname) == '+') {
      $res["name"] = $l_client;
    } else {
      $res["name"] = $l_supplier;
    }
  }
  
  elseif ($fieldname == "invoice_pay_label") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$OD->data_set->f("invoice_payment_id");
  }

  else if ($fieldname == "invoice_archive") {
    $res["align"] = "center";
    if ($OD->data_set->f($fieldname)) {
      $res["name"] = "X";
    } else {
      $res["name"] = "&nbsp;";
      $res["txt_name"] = " ";
    }
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

  else if ($fieldname == "payment_amount") {
    $amount = $OD->data_set->f("payment_amount");
    $pi_amount = $OD->data_set->f("paymentinvoice_amount");
    if ($amount == $pi_amount) {
      $res["name"] = $amount;
    } else { 
      $res["name"] = "$pi_amount ($amount)";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Invoice search form
// Parameters:
//   - $invoice[] : hash with invoice values
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_search_form($invoice="") {
  global $ctt_sales;

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice["sel_tt"];
  if (! is_array($tt_ids) && ($tt_ids == "")) {
    $tt_ids = "0";
  }

  $sta = get_invoicestatus();
  $tts = get_global_tasktype($ctt_sales, $tt_ids);
  $block .= html_invoice_search_form($invoice, $sta, $tts);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// invoice search Form
// Parameters :
//   - $invoice : invoice hash parameters/
//   - $sta     : Status array
//   - $tts     : Task Type aray
//////////////////////////////////////////////////////////////////////////////
function html_invoice_search_form($invoice, $sta, $tts) {
  global $set_theme, $ico_crow, $ico_add, $c_all, $l_all, $ctt_sales;
  global $l_label_start, $l_number, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_tt, $l_date, $l_after, $l_before, $l_inout;
  global $l_deal, $l_company, $l_include_archive;
  global $l_find, $l_both, $l_client, $l_supplier;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $dateexp_after = $invoice["dateexp_after"];
  $dateexp_before = $invoice["dateexp_before"];
  $inout = $invoice["inout"];
  $tt = $invoice["tt"];
  $deal = $invoice["deal"];
  $deal_id = $invoice["deal_id"];
  $company = $invoice["company"];
  $comp_id = $invoice["company_id"];
  $proj_id = $invoice["project_id"];
  $archive = ($invoice["archive"]) ? "checked" : "" ;

  $url_form = url_prepare("invoice_index.php");

   $sel_status = "<select name=\"sel_status\">
      <option value=\"$c_all\">$l_all</option>";
   foreach ($sta as $s_id => $s_status) {
     $s_label = $s_status["label"];
     $sel_status .= "<option value=\"$s_id\"";
     if ($status == $s_id) $sel_status .= " selected = \"selected\"";
     $sel_status .= ">$s_label</option>";
   }
   $sel_status .= "</select>";


   //  $block_status = of_extmod_dis_search_select($sta, "status", $url_tt);

  if (($inout == "$c_all") || ($inout == "")) {
    $rb_both = "checked";
  }
  if ($inout == "+") {
    $rb_client = " checked ";
  }
  if ($inout == "-") {
    $rb_four = " checked ";
  }

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";

  $block_tt = of_extmod_dis_search_select($tts, "tt", $url_tt);

  $block = "
    <form method=\"post\" name=\"f_search\"
        onSubmit=\"if (check_search_form(this) == false) return false;
        else return true;\" action=\"$url_form\">
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

    <div class=\"searchLabel\">$l_date $l_after<br />
      <script>calendar('tf_date_after','$date_after')</script>
    </div>

    <div class=\"searchLabel\">$l_date $l_before<br />
      <script>calendar('tf_date_before','$date_before')</script>
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

    $block_tt

    <div class=\"searchLabel\">$l_deal<br />
      <input type=\"text\" name=\"tf_deal\" value=\"$deal\" />
    </div>

    <div class=\"searchLabel\">$l_company<br />
      <input type=\"text\" name=\"tf_company\" value=\"$company\" />
    </div>

    <div class=\"searchLabel\">&nbsp;<br>
      <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />
      <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"param_project\" value=\"$proj_id\" />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"search\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </div>

    <div class=\"searchEnd\"></div>

    </div>
    </form>";

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

  $prefs = get_display_pref($auth->auth["uid"], "invoice");
  $inv_q = run_query_invoice_search($invoice, $new_order, $order_dir);
  $nb_inv = $inv_q->num_rows_total();
  if ($nb_inv == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $block = html_invoice_search_list($inv_q,$prefs,$nb_inv,$invoice);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice search result
// Paratemers:
// $inv_q  : list of invoices
// $prefs  : fields to display
// $nb_invoices : number of invoices
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_list ($inv_q, $prefs, $nb_invoice, $invoice) {
  global $col_frs, $col_client;
  global $l_found, $l_deal;
  global $l_label, $l_number, $l_amount_ht, $l_amount_ttc, $l_date, $l_status;
  global $display;

  $label = urlencode($invoice["label"]);
  $number = urlencode($invoice["number"]);
  $ht = urlencode($invoice["ht"]);
  $ttc = urlencode($invoice["ttc"]);
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $dateexp_after = $invoice["dateexp_after"];
  $dateexp_before = $invoice["dateexp_before"];
  $inout = $invoice["inout"];
  $deal = urlencode($invoice["deal"]);
  $company = urlencode($invoice["company"]);
  $archive = $invoice["archive"];
  $deal_id = $invoice["deal_id"];
  $comp_id = $invoice["company_id"];
  $proj_id = $invoice["project_id"];

  $url = url_prepare("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_ht=$ht". 
		    "&amp;tf_amount_ttc=$ttc".
		    "&amp;sel_status=$status".
		    "&amp;tf_date_after=$date_after".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_date_exp_after=$dateexp_after".
		    "&amp;tf_date_exp_before=$dateexp_before".
		    "&amp;tf_deal=$deal".
		    "&amp;rd_inout=$inout".
		    "&amp;param_company=$comp_id".
		    "&amp;param_deal=$deal_id".
		    "&amp;param_project=$proj_id".
		    "&amp;cb_archive=$archive"); 

  $dis_invoice = new OBM_DISPLAY ("DATA", $prefs, "invoice");
  $dis_invoice->data_set = $inv_q;
  $dis_invoice->data_header = "top";
  $dis_invoice->data_url = $url;

  $display["msg"] .= display_info_msg("$nb_invoice $l_found");
  $block .= $dis_invoice->display("dis_data_invoice");

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

  $inv_q = run_query_invoice_detail($id);
  if ($inv_q->num_rows() != 1) {
    $display["msg"] .= display_err_msg("$l_query_error  - $l_no_found !");
  }
  if ($id > 0) {
    $pay_q = run_query_invoice_payment($id);
    $prefs_p = get_display_pref($uid, "payment");
    $display["detailInfo"] = display_record_info($inv_q);
    $block = html_invoice_consult($inv_q, $pay_q, $prefs_p);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display invoice detail screen
// Parameters:
//   - $inv_q  : DBO invoice info
//   - $pay_q  : DBO : payments for the invoice
//   - prefs_p : display pref for payments
///////////////////////////////////////////////////////////////////////////////
function html_invoice_consult($inv_q, $pay_q, $prefs_p) {
  global $display, $set_theme;
  global $l_delete, $l_invoice_infos;
  global $l_list_payment, $l_no_payment, $l_payments_todel;

  $i_id = $inv_q->f('invoice_id');

  $display["link"] = html_invoice_links($inv_q);
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

    $dis_payment = new OBM_DISPLAY("DATA", $prefs_p, "invoice");
    $dis_payment->data_set = $pay_q;
    $dis_payment->data_header = "top";
    $dis_payment->data_url = $url;
    $dis_payment->data_order = false;
    
    $block .= $dis_payment->display("dis_data_invoice");
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Invoice links menu
// Parameters:
//   - $p_q : invoice database result 
// Returns:
//   $r : string with XHTML code
///////////////////////////////////////////////////////////////////////////////
function html_invoice_links($inv_q) {
  global $set_theme, $ico_document, $ico_document_new;
  global $l_invoice, $l_new, $l_module_document;
  global $l_document_add;
  global $path, $auth, $cgp_show, $popup_height, $popup_width;

  $id = $inv_q->f("invoice_id");
  $name = $inv_q->f("invoice_label");

  $uname = urlencode($name);

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/invoice/invoice_index.php")."&amp;ext_id=$id&amp;ext_target=$l_invoice";
    $nb_document = run_query_global_document_nb ($id, "invoice");
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
	<a href=\"\" onclick=\"window.name='$l_invoice'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
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


//////////////////////////////////////////////////////////////////////////////
// XHTML Display invoice detail info part
//////////////////////////////////////////////////////////////////////////////
function dis_invoice_detail($inv_q) {
  global $display, $set_theme, $path, $ico_company, $ico_deal, $ico_project;
  global $l_company, $l_deal, $l_project,$l_amount_ht, $l_amount_ttc;
  global $l_date, $l_label, $l_number, $l_inout, $l_supplier, $l_client;
  global $l_archive, $l_status, $l_comment, $l_invoice;
  global $l_expiration_date, $l_payment_date;
  global $l_yes, $l_no;

  $label = $inv_q->f("invoice_label");
  $num = $inv_q->f("invoice_number");
  $date = date_format($inv_q->f("date"), 1);
  $expiration_date = date_format($inv_q->f("expiration_date"), 1);
  $payment_date = date_format($inv_q->f("payment_date"), 1);
  $status = $inv_q->f("invoicestatus_label");
  $inout = ($inv_q->f("invoice_inout") == '-') ? $l_supplier : $l_client;
  $title = $inout;
  $ht = $inv_q->f("invoice_amount_ht");
  $ttc = $inv_q->f("invoice_amount_ttc");
  $comment = beautify_comment(nl2br($inv_q->f("invoice_comment")));
  $comp_id = $inv_q->f("invoice_company_id");
  $company = $inv_q->f("company_name");
  $deal_id = $inv_q->f("invoice_deal_id");
  $deal = $inv_q->f("deal_label");
  $project_id = $inv_q->f("invoice_project_id");
  $project = $inv_q->f("project_name");
  $archive = ($inv_q->f("invoice_archive") == 1 ? $l_yes : $l_no);

  $display["title"] = "<div class=\"title\">$l_invoice : $label</div>";

  $url_comp = url_prepare("$path/company/company_index.php?action=detailconsult&amp;param_company=$comp_id");
  if ($deal_id > 0) {
    $url_deal = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;param_deal=$deal_id");
    $deal_link = "<a href=\"$url_deal\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"\" /></a>";
  }
  if ($project_id > 0) {
    $url_proj = url_prepare("$path/project/project_index.php?action=detailconsult&amp;param_project=$project_id");
    $proj_link = "<a href=\"$url_proj\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" alt=\"\" /></a>";
  }

  $block .= "
    <div class=\"detailHead\">$title</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company <a href=\"$url_comp\"><img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"\" /></a></td>
      <td class=\"detailText\">$company</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_deal $deal_link</td>
      <td class=\"detailText\">$deal</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_project $proj_link</td>
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
      <td class=\"detailLabel\">$l_archive</td>
      <td class=\"detailText\">$archive</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_expiration_date</td>
      <td class=\"detailText\">$expiration_date</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_payment_date</td>
      <td class=\"detailText\">$payment_date</td>
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
    $inv_q = run_query_invoice_detail($id);
    $display["detailInfo"] = display_record_info($inv_q);
  } else {
    $inv_q = "";
  }
  $stat = get_invoicestatus();
  $usr_q = run_query_userobm_active();
  $block = html_invoice_form($action, $invoice, $inv_q, $stat, $usr_q);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Invoice Form              
// Parameters:
//   - $action    : action called
//   - $invoice[] : invoice hash parameters
//   - $inv_q     : if updating, contains the invoice current info
//   - $stat      : Array of status list
//   - $usr_q     : userobm database result 
///////////////////////////////////////////////////////////////////////////////
function html_invoice_form($action, $invoice, $inv_q, $stat, $usr_q) {
  global $auth, $path, $display, $set_theme;
  global $ico_company, $ico_deal, $ico_project;
  global $l_label, $l_number, $l_comment, $l_add_comment, $l_upd_comment;
  global $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_date, $l_inout, $l_client, $l_supplier;
  global $l_insert, $l_update, $l_archive, $l_expiration_date;
  global $l_invoice, $l_company, $l_deal, $l_project, $l_payment_date;
  global $default_invoice_numbering, $c_php_isodate_format;
  global $default_tax, $l_tax_rate, $l_compute_tax; 

  $uid = $auth->auth["uid"];

  // if update mode and first time values are taken from database
  if (($action == "detailupdate") || ($action == "duplicate")) {
    $id = $inv_q->f("invoice_id");
    $label = $inv_q->f("invoice_label");
    $number = $inv_q->f("invoice_number");
    $archive = ($inv_q->f("invoice_archive") == 1 ? " checked" : "");
    $comment = $inv_q->f("invoice_comment");
    $status = $inv_q->f("invoice_status_id");
    $date = isodate_format($inv_q->f("date"), 1);
    $edate = isodate_format($inv_q->f("expiration_date"), 1);
    $pdate = isodate_format($inv_q->f("payment_date"), 1);
    $inout = $inv_q->f("invoice_inout");
    $ht = $inv_q->f("invoice_amount_ht");
    $ttc = $inv_q->f("invoice_amount_ttc");
    $c_id = $inv_q->f("invoice_company_id");
    $c_name = $inv_q->f("company_name");
    $d_id = $inv_q->f("invoice_deal_id");
    $d_label = $inv_q->f("deal_label");
    $p_id = $inv_q->f("invoice_project_id");
    $p_name = $inv_q->f("project_name");
    $usercomment = $uid;
    $datecomment = isodate_format();

  // New form and first time
  } elseif ($action == "new") {
    $date = date($c_php_isodate_format); 
    $edate = $date;
    $pdate = $date;
    $number = date ($default_invoice_numbering);
    // when creating from a deal, default to use same "inout" as the dealtype
    if ($d_id >= 1) {
      $inout = run_query_invoice_deal_get_inout ($d_id);
    }
    $usercomment = $uid;
    $datecomment = isodate_format();
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = isodate_format();
    $label .= "-".date("Y-m-d");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($invoice["id"])) { $id = $invoice["id"]; }
  if (isset($invoice["label"])) { $label = stripslashes($invoice["label"]); }
  if (isset($invoice["number"])) { $number = $invoice["number"]; }
  if (isset($invoice["archive"])) { $archive = ($invoice["archive"] == 1 ? "checked" : ""); }
  if (isset($invoice["date"])) { $date = $invoice["date"]; }
  if (isset($invoice["edate"])) { $edate = $invoice["edate"]; }
  if (isset($invoice["pdate"])) { $pdate = $invoice["pdate"]; }
  if (isset($invoice["status"])) { $status = $invoice["status"]; }
  if (isset($invoice["inout"])) { $inout = $invoice["inout"]; }
  if (isset($invoice["ht"])) { $ht = $invoice["ht"]; }
  if (isset($invoice["ttc"])) { $ttc = $invoice["ttc"]; }
  if (isset($invoice["comment"])) { $comment = stripslashes($invoice["comment"]); }
  if (isset($invoice["add_comment"])) { $add_comment = stripslashes($invoice["add_comment"]); }
  if (isset($invoice["usercomment"])) { $usercomment = $invoice["usercomment"]; }
  if (isset($invoice["datecomment"])) { $datecomment = $invoice["datecomment"]; }

  if (isset($invoice["company_id"])) { $c_id = $invoice["company_id"]; }
  if (isset($invoice["company_name"])) { $c_name = stripslashes($invoice["company_name"]); }
  if (isset($invoice["comp_new_id"])) { $c_new_id = $invoice["comp_new_id"]; }
  if (isset($invoice["comp_new_name"])) { $c_new_name = stripslashes($invoice["comp_new_name"]); }
  if (isset($invoice["deal_id"])) { $d_id = $invoice["deal_id"]; }
  if (isset($invoice["deal_label"])) { $d_label = stripslashes($invoice["deal_label"]); }
  if (isset($invoice["deal_new_id"])) { $d_new_id = $invoice["deal_new_id"]; }
  if (isset($invoice["deal_new_label"])) { $d_new_label = stripslashes($invoice["deal_new_label"]); }
  if (isset($invoice["project_id"])) { $p_id = $invoice["project_id"]; }
  if (isset($invoice["project_name"])) { $p_name = stripslashes($invoice["project_name"]); }
  if (isset($invoice["proj_new_id"])) { $p_new_id = $invoice["proj_new_id"]; }
  if (isset($invoice["proj_new_name"])) { $p_new_name = stripslashes($invoice["proj_new_name"]); }

  $title = ($inout == '-') ? $l_supplier : $l_client;


  // Status select
  $sel_status = "<select name=\"sel_status\">"; 
  foreach ($stat as $s_id => $s_status) {
    $s_label = $s_status["label"];
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
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $inout_c /> $l_client
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $inout_s /> $l_supplier
      </td>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      </tr><tr>
        <td class=\"detailLabel\">$l_upd_comment</td>
        <td>&nbsp;</td>
      </tr><tr>
        <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea></td>
    ";
  }

  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f("userobm_id");
    $cname = $usr_q->f("userobm_lastname")." ".$usr_q->f("userobm_firstname");
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= "</select>";

  // Buttons
  if (($action == "update") || ($action == "detailupdate")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"update\" />
    <input type=\"hidden\" name=\"param_invoice\" value=\"$id\" />
    <input type=\"hidden\" name=\"hd_rd_inout\" value=\"$inout\" />
    <input type=\"submit\" value=\"$l_update\"
       onClick=\"if (check_invoice(this.form)) return true; else return false;\" />";
  } elseif (($action == "new") || ($action == "insert")
            || ($action == "duplicate")) {
    $dis_button .= "
    <input type=\"submit\" value=\"$l_insert\" 
      onClick=\"if(check_invoice(this.form)) return true ; else return false;\" />
    <input type=\"hidden\" name=\"action\" value=\"insert\" />";
  }

  $display["title"] = "<div class=\"title\">$l_invoice : $label</div>";

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"invoice_index.php\">

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
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"\" /></a>
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
      <a href=\"\" onclick=\"window.open('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.deal_new_id&amp;ext_widget_text=f_entity.deal_new_label','Deal','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_deal\" alt=\"\" /></a>
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
      <a href=\"\" onclick=\"window.open('$path/project/project_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.project_new_id&amp;ext_widget_text=f_entity.project_new_name','Project','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_project\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"project_new_name\" value=\"$p_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr>
  </table>

  <div class=\"detailHead\">$l_invoice</div>

  <table class=\"detail\">
  <tr>
    <td class=\"detailLabel\">$l_label</td>
    <td class=\"detailForm\">
      <input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_number</td>
    <td class=\"detailForm\">
      <input name=\"tf_number\" type=\"text\" size=\"11\" value=\"$number\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_archive</td>
    <td class=\"detailForm\"><input type=\"checkbox\" name=\"cb_archive\" value=\"1\" $archive /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_date</td>
    <td class=\"detailForm\">
      <script>calendar('tf_date','$date')</script>
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_expiration_date</td>
    <td class=\"detailForm\">
      <script>calendar('tf_expiration_date','$edate')</script>
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_payment_date</td>
    <td class=\"detailForm\">
      <script>calendar('tf_payment_date','$pdate')</script>
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_status</td>
    <td class=\"detailForm\">$sel_status</td>
  </tr><tr>
    $dis_inout
  </tr><tr>
    <td class=\"detailLabel\">$l_amount_ht</td>
    <td class=\"detailForm\">
      <input name=\"tf_amount_ht\" type=\"text\" size=\"15\" value=\"$ht\" onChange=\"compute_tax(this.form, true);\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_tax_rate</td>
    <td class=\"detailForm\"><input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" size=\"8\" />
      <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" onClick=\"compute_tax(this.form);\" />
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_amount_ttc</td>
    <td class=\"detailForm\">
      <input name=\"tf_amount_ttc\" type=\"text\" size=\"15\" value=\"$ttc\" />
    </td>
  </tr>
  </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_add_comment</td>
      <td class=\"detailForm\"><script>calendar('tf_datecomment','$datecomment')</script>$sel_usercomment</td>
    </tr><tr>
      <td class=\"detailForm\" colspan=\"2\"><textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea></td>
      $dis_comment
    </tr>
    </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
  </div>

  </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the invoice delete validation screen
// Parameters:
//   - $p_id : invoice id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_invoice($p_id) {
  global $l_can_delete, $l_back, $l_delete;
  global $display;

  $url = url_prepare("invoice_index.php");

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"param_invoice\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"param_invoice\" value=\"$p_id\" />
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
// Display the invoice dashboard
// Parameters:
//   - $invoice : invoice hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_dashboard($invoice) {
  global $display, $set_theme, $path, $ctt_sales;
  global $l_status, $l_total, $l_invoice, $l_date, $l_amount_ht, $l_amount_ttc;
  global $l_billed, $l_order_no_bill, $l_potential, $l_monthsofyear;
  global $l_payment, $l_amount_ht, $l_amount_ttc, $l_paid, $l_waiting;
  global $l_execute;

  if (isset($invoice["year"])) {
    $year = $invoice["year"];
  } else {
    $year = date("Y");
  }
  $year_prev = $year - 1;
  $year_next = $year + 1;
  $date_ranges = array(array("$year-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, "invoice_date", $invoice["sel_tt"]);
  $url_prev = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_prev");
  $url_next = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_next");

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice["sel_tt"];
  if (! is_array($tt_ids) && ($tt_ids == "")) {
    $tt_ids = "0";
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_extmod_dis_search_select($tts, "tt", $url_tt);

  // Table header
  for ($month = 1; $month < 13; $month++) {
    $label = $l_monthsofyear[$month - 1];
    $dis_row_head .= "
      <td class=\"resultHead\">$label</td>";
  }

  // Billed infos -------------------------------------------------------------

  $status_pay = get_invoicestatus("payment");

  // Loop through status (rows)
  foreach ($status_pay as $s_id => $s_status) { 
    $s_label = $s_status["label"];
    $total_amount_ht = 0;
    $cpt++;
    $data = "data" . ($cpt % 2);

    $dis_bil_row .= "<tr class=\"$data\" style=\"text-align: right;\">
      <td style=\"text-align: left;\">$s_label</td>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice created
      if (is_array($inv["$year_month"]["billed"]["$s_id"])) {
	$status_info = $inv["$year_month"]["billed"]["$s_id"];
	$amount_ht = $status_info["amount_ht"];
	$nb = $status_info["nb"];
	$total_amount_ht += $amount_ht;
	$dis_amount_ht = number_format($amount_ht, 2, ",", " ");
	$month_start = "$year_month-01";
	$month_end = date("Y-m-d", mktime(12,0,0,$month+1, 0, $year));
        $dis_bil_row .= "
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cb_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end\">$dis_amount_ht</a></td>";
      } else {
        $dis_bil_row .= "
          <td class=\"$data\"> </td>";
      }
    }
    $f_total_amount_ht = number_format($total_amount_ht, 2, ",", " ");
    $dis_bil_row .= "<td class=\"data2\">&nbsp;$f_total_amount_ht</td>
        </tr>";
  }

  // Total row
  $total_amount_ht = 0;
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $status_info = $inv["$year_month"]["billed"]["total"];
    $amount_ht = $status_info["amount_ht"];
    $total_amount_ht += $amount_ht;
    $dis_amount_ht = number_format($amount_ht, 2, ",", " ");
    if ($amount_ht > 0) {
      $month_start = "$year_month-01";
      $month_end = date("Y-m-d", mktime(0,0,0,$month+1, 0, $year));
      $dis_bil_row_total .= "
          <td>&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;cb_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end\">$dis_amount_ht</a></td>";
    } else {
      $dis_bil_row_total .= "
          <td>&nbsp;$dis_amount_ht</td>";
    }
  }
  $f_total_amount_ht = number_format($total_amount_ht, 2, ",", " ");
  $dis_bil_row_total .= "
      <td>&nbsp;$f_total_amount_ht</td>";

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $status_info = $inv["$year_month"]["billed"]["total"];
    $amount_ht = $status_info["amount_ht"];
    $values[$month-1] = $amount_ht;
    $labels[$month-1] = number_format($amount_ht, 0, ",", " ");
    $xlabels[$month-1] = $l_monthsofyear[$month-1];
    $values_pot[$month-1] = $amount_ht + $inv["$year_month"]["potential"]["total"]["amount_ht"];
    $labels_pot[$month-1] = number_format($values_pot[$month-1], 0, ",", " ");
  }
  $title = "$l_billed $year";

  $chart_bil_params .= "&amp;title=".urlencode($title)."&amp;values=".urlencode(serialize($values)) . "&amp;chart_labels=".urlencode(serialize($labels)). "&amp;chart_xlabels=".urlencode(serialize($xlabels));

  // Potential chart ----------------------------------------------------------

  $status_toc = get_invoicestatus("tocreate");

  // Loop through status (rows)
  foreach ($status_toc as $s_id => $s_status) { 
    $s_label = $s_status["label"];
    $total_amount_ht = 0;
    $cpt++;
    $data = "data" . ($cpt % 2);

    $dis_pot_row .= "<tr class=\"$data\" style=\"text-align: right;\">
      <td style=\"text-align: left;\">$s_label</td>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice not created
      if (is_array($inv["$year_month"]["potential"]["$s_id"])) {
	$status_info = $inv["$year_month"]["potential"]["$s_id"];
	$amount_ht = $status_info["amount_ht"];
	$nb = $status_info["nb"];
	$total_amount_ht += $amount_ht;
	$f_amount_ht = number_format($amount_ht, 2, ",", " ");
	$month_start = "$year_month-01";
	$month_end = date("Y-m-d", mktime(12,0,0,$month+1, 0, $year));
        $dis_pot_row .= "
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cb_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end\">$f_amount_ht</a></td>";
      } else {
        $dis_pot_row .= "
          <td class=\"$data\"> </td>";
      }
      $labels[$month-1] = number_format($amount_ht, 0, ",", " ");
    }
    $f_total_amount_ht = number_format($total_amount_ht, 2, ",", " ");
    $dis_pot_row .= "<td class=\"data2\">&nbsp;$f_total_amount_ht</td>
        </tr>";
  }
  $plots["new_bar"] = array(1, 0);
  $plots["legends"] = array($l_order_no_bill, $l_billed);
  $title = "$l_potential $year";

  $chart_pot_params .= "&amp;title=".urlencode($title)."&amp;values=".urlencode(serialize(array($values_pot, $values)))."&amp;chart_plots=".urlencode(serialize($plots))."&amp;chart_labels=".urlencode(serialize($labels_pot)). "&amp;chart_xlabels=".urlencode(serialize($xlabels));

  // Payment infos ------------------------------------------------------------

  $date_ranges = array(array("$year-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, "invoice_expiration_date", $invoice["sel_tt"]);

  $status_pay = get_invoicestatus("payment");

  // Loop through status (rows)
  foreach ($status_pay as $s_id => $s_status) { 
    $s_label = $s_status["label"];
    //    // If the status tell invoice can be archived, invoice has been paid 
    //    $status_paid = $s_status["archive"] == 1 ? true : false;
    $total_amount_ttc = 0;
    $cpt++;
    $data = "data" . ($cpt % 2);

    $dis_pay_row .= "<tr class=\"$data\" style=\"text-align: right;\">
      <td style=\"text-align: left;\">$s_label</td>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice created
      if (is_array($inv["$year_month"]["billed"]["$s_id"])) {
	$status_info = $inv["$year_month"]["billed"]["$s_id"];
	$amount_ttc = $status_info["amount_ttc"];
	$nb = $status_info["nb"];
	$total_amount_ttc += $amount_ttc;
	$dis_amount_ttc = number_format($amount_ttc, 2, ",", " ");
	$month_start = "$year_month-01";
	$month_end = date("Y-m-d", mktime(12,0,0,$month+1, 0, $year));
        $dis_pay_row .= "
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cb_archive=1&amp;tf_date_exp_after=$month_start&amp;tf_date_exp_before=$month_end\">$dis_amount_ttc</a></td>";
      } else {
        $dis_pay_row .= "
          <td class=\"$data\"> </td>";
      }
    }
    $f_total_amount_ttc = number_format($total_amount_ttc, 2, ",", " ");
    $dis_pay_row .= "<td class=\"data2\">&nbsp;$f_total_amount_ttc</td>
        </tr>";
  }

  // Total row
  $total_amount_ttc = 0;
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $status_info = $inv["$year_month"]["billed"]["total"];
    $amount_ttc = $status_info["amount_ttc"];
    $total_amount_ttc += $amount_ttc;
    $dis_amount_ttc = number_format($amount_ttc, 2, ",", " ");
    if ($amount_ttc > 0) {
      $month_start = "$year_month-01";
      $month_end = date("Y-m-d", mktime(0,0,0,$month+1, 0, $year));
      $dis_pay_row_total .= "
          <td>&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;cb_archive=1&amp;tf_date_exp_after=$month_start&amp;tf_date_exp_before=$month_end\">$dis_amount_ttc</a></td>";
    } else {
      $dis_pay_row_total .= "
          <td>&nbsp;$dis_amount_ttc</td>";
    }
  }
  $f_total_amount_ttc = number_format($total_amount_ttc, 2, ",", " ");
  $dis_pay_row_total .= "
      <td>&nbsp;$f_total_amount_ttc</td>";

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $xlabels[$month-1] = $l_monthsofyear[$month-1];
    $values[$month-1] = $inv["$year_month"]["paid"]["total"]["amount_ttc"];
    $values_pay[$month-1] = $inv["$year_month"]["billed"]["total"]["amount_ttc"];
    $labels[$month-1] = number_format($values_pay[$month-1], 0, ",", " ");
  }
  $plots["new_bar"] = array(1, 0);
  $plots["legends"] = array($l_waiting, $l_paid);
  $title = "$l_payment $year ($l_amount_ttc)";

  $chart_pay_params .= "&amp;title=".urlencode($title)."&amp;values=".urlencode(serialize(array($values_pay, $values)))."&amp;chart_plots=".urlencode(serialize($plots))."&amp;chart_labels=".urlencode(serialize($labels)). "&amp;chart_xlabels=".urlencode(serialize($xlabels));


  $block = "
    <form method=\"post\" name=\"f_search\" action=\"$url_form\">

    <div class=\"detailHead\">

      <table>
      <tr>
        <td>
          <a href=\"$url_prev\">
          <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/previous.png\" alt=\"\" />
          </a>
        </td>
        <td class=\"timePeriod\">
          $l_invoice $year
        </td>
        <td>
          <a href=\"$url_next\">
           <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/next.png\" alt=\"\" />
          </a>
        </td>
      </tr>
      </table>
    </div>

    <div class=\"search\">
      <div class=\"detailText\">$block_tt</div>
      <div class=\"searchLabel\">&nbsp;<br>
        <input type=\"hidden\" name=\"param_deal\" value=\"$deal_id\" />
        <input type=\"hidden\" name=\"param_company\" value=\"$comp_id\" />
        <input type=\"hidden\" name=\"param_project\" value=\"$proj_id\" />
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
      </div>

      <div class=\"searchEnd\"></div>

    </div>

    <div class=\"detailHead\">$l_billed ($l_amount_ht)</div>

    <table class=\"resultTable\">
    <tr style=\"text-align: right;\">
      <td class=\"resultHead\" style=\"text-align: left;\">$l_status</td>
      $dis_row_head
      <td class=\"resultHead\" style=\"text-align: center;\">$l_total $year</td>
    </tr>
    $dis_bil_row
    <tr class=\"data2\" style=\"text-align: right;\">
      <td style=\"text-align: left;\">$l_total</td>
      $dis_bil_row_total
    </tr>
    </table>
    <p />

    <div class=\"detailHead\">$l_order_no_bill ($l_amount_ht)</div>

    <table class=\"resultTable\">
    <tr style=\"text-align: right;\">
      <td class=\"resultHead\" style=\"text-align: left;\">$l_status</td>
      $dis_row_head
      <td class=\"resultHead\" style=\"text-align: center;\">$l_total $year</td>
    </tr>
    $dis_pot_row
    </table>
    <p />

    <div class=\"detailHead\">$l_payment ($l_amount_ttc)</div>

    <table class=\"resultTable\">
    <tr style=\"text-align: right;\">
      <td class=\"resultHead\" style=\"text-align: left;\">$l_status</td>
      $dis_row_head
      <td class=\"resultHead\" style=\"text-align: center;\">$l_total $year</td>
    </tr>
    $dis_pay_row
    <tr class=\"data2\" style=\"text-align: right;\">
      <td style=\"text-align: left;\">$l_total</td>
      $dis_pay_row_total
    </tr>
    </table>
    <p />

    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar$chart_bil_params\" alt=\"\" />
    <br />
    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_pot_params\" alt=\"\" />
    <br />
    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_pay_params\" alt=\"\" />

";


  return $block;
} 


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_display_pref($prefs) {
  global $l_invoice_display;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "invoice");
  $dis_pref->pref_title = $l_invoice_display;
  $dis_pref->pref_dis_help = 1;

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}


?>
