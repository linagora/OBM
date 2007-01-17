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
  global $invoice;

  if ($fieldname == "invoice_deal") {
    $res["url"] = "$path/deal/deal_index.php?action=detailconsult&amp;deal_id=".$OD->data_set->f("deal_id");
  }

  elseif ($fieldname == "invoice_project") {
    $res["url"] = "$path/project/project_index.php?action=detailconsult&amp;project_id=".$OD->data_set->f("project_id");
  }

  elseif (($fieldname == "invoice_label")) {
    if ($OD->display_ext == "get_id") {
      $res["url"] = "javascript:check_get_id(".$OD->data_set->f("invoice_id").",'".addslashes($OD->data_set->f("invoice_label"))."');";
    } else if ($OD->display_ext == "get_id_url") {
      $ext_url = $invoice["ext_url"];
      $res["url"] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f("invoice_id").");";
    } elseif($link_ok) {
      $res["url"] = "$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=".$OD->data_set->f("invoice_id");
    }    
  }

  elseif ($fieldname == "invoice_company") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f("company_id");
  }

  elseif ($fieldname == "invoice_inout") {
    if ($OD->data_set->f($fieldname) == '+') {
      $res["name"] = $l_client;
    } else {
      $res["name"] = $l_supplier;
    }
  }
  
  elseif ($fieldname == "invoice_pay_label") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f("invoice_payment_id");
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
    $res["name"] = of_date_format($OD->data_set->f("date"));
  }

  else if ($fieldname == "payment_date") {
    $res["name"] = of_date_format($OD->data_set->f("payment_date"));
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
  $tt_ids = $invoice["tasktype"];
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
  global $display, $path, $set_theme, $popup, $c_all, $l_all, $ctt_sales;
  global $l_label_start, $l_number, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_tt, $l_date, $l_after, $l_before, $l_inout;
  global $l_company, $l_include_archive, $l_select_invoice;
  global $l_find, $l_both, $l_client, $l_supplier;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["amount_ht"];
  $ttc = $invoice["amount_ttc"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $dateexp_after = $invoice["dateexp_after"];
  $dateexp_before = $invoice["dateexp_before"];
  $inout = $invoice["inout"];
  $tt = $invoice["tt"];
  $deal_id = $invoice["deal_id"];
  $company = $invoice["company"];
  $comp_id = $invoice["company_id"];
  $project_new_id = $invoice["project_id"];
  $archive = ($invoice["archive"]) ? "checked" : "" ;

  $url_form = url_prepare("invoice_index.php");

  $sel_status = "<select name=\"sel_status\">
      <option value=\"$c_all\">$l_all</option>";
  if (is_array($sta)) {
    foreach ($sta as $s_id => $s_status) {
      $s_label = $s_status["label"];
      $sel_status .= "<option value=\"$s_id\"";
      if ($status == $s_id) $sel_status .= " selected = \"selected\"";
      $sel_status .= ">$s_label</option>";
    }
  }
  $sel_status .= "</select>";

  //     $block_status = of_select_dis_search_select($sta, "status", $url_tt);

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
  $block_tt = of_select_dis_search_select($tts, "tt", $url_tt);

  if ($popup) {
    $ext_action = $invoice["ext_action"];
    $ext_url = $invoice["ext_url"];
    $ext_id = $invoice["ext_id"];
    $ext_title = ($invoice["ext_title"] ? $invoice["ext_title"] : $l_select_invoice);
    $ext_target = $invoice["ext_target"];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display["title"] = "<div class=\"title\">$ext_title</div>";
  }

  $block = "
    <form method=\"post\" name=\"f_search\"
        onSubmit=\"if (check_search_form(this) == false) return false;
        else return true;\" action=\"$url_form\" class=\"search\">

    <label>$l_label_start<br />
      <input type=\"text\" name=\"tf_label\" size=\"20\" value=\"$label\" />
    </label>

    <label>$l_number<br />
      <input type=\"text\" name=\"tf_number\" size=\"12\" value=\"$number\" />
    </label>

    <label>$l_amount_ht<br />
      <input type=\"text\" name=\"tf_amount_ht\" size=\"15\" value=\"$ht\" />
    </label>

    <label>$l_amount_ttc<br />
      <input type=\"text\" name=\"tf_amount_ttc\" size=\"15\" value=\"$ttc\" />
    </label>

    <label>$l_status<br />
      $sel_status
    </label>

    <label>$l_date $l_after<br />
    <input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
    </label>

    <label>$l_date $l_before<br />
    <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
    </label>

    <label>$l_include_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /> 
    </label>

    <label>$l_inout<br />
      <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $rb_both />
      $l_both &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $rb_client />
      $l_client &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $rb_four />
      $l_supplier &nbsp;
    </label>

    $block_tt

    <label>$l_company<br />
      <input type=\"text\" name=\"tf_company\" value=\"$company\" />
    </label>

    <label>&nbsp;<br />
      <input name=\"action\" type=\"hidden\" value=\"search\" />
      <input type=\"hidden\" name=\"deal_id\" value=\"$deal_id\" />
      <input type=\"hidden\" name=\"company_id\" value=\"$comp_id\" />
      <input type=\"hidden\" name=\"project_id\" value=\"$project_new_id\" />
      <input name=\"popup\" type=\"hidden\" value=\"$popup\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
      $ext
    </label>
    <p class=\"CL\" />
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice search result
// Parameters:
//   - $invoice[] : invoice search criteria
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_search_list($invoice) {
  global $display, $l_found, $l_no_found;
  global $auth;

  $prefs = get_display_pref($auth->auth["uid"], "invoice");
  $inv_q = run_query_invoice_search($invoice);
  $nb_inv = $inv_q->num_rows_total();
  if ($nb_inv == 0) {
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_inv $l_found");
    $block = html_invoice_search_list($inv_q, $prefs, $invoice);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice search result
// Paratemers:
//   - $inv_q     : list of invoices
//   - $prefs     : fields to display
//   - $invoice[] : invoice search criteria
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_list ($inv_q, $prefs, $invoice) {
  global $l_close;

  $popup = $invoice["popup"];
  if ($popup) {
    $ext_action = $invoice["ext_action"];
    $ext_url = $invoice["ext_url"];
    $ext_id = $invoice["ext_id"];
    $ext_target = $invoice["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  $label = urlencode($invoice["label"]);
  $number = urlencode($invoice["number"]);
  $ht = urlencode($invoice["amount_ht"]);
  $ttc = urlencode($invoice["amount_ttc"]);
  $status = $invoice["status"];
  if (is_array($status) && (count($status) > 0)) {
    foreach ($status as $s_id) {
      $param_status .= "&amp;sel_status[]=$s_id";
    }
  } else if ($status != '') {
    $param_status .= "&amp;sel_status=$status";
  }
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $dateexp_after = $invoice["dateexp_after"];
  $dateexp_before = $invoice["dateexp_before"];
  $inout = $invoice["inout"];
  $company = urlencode($invoice["company"]);
  $archive = $invoice["archive"];
  $deal_id = $invoice["deal_id"];
  $comp_id = $invoice["company_id"];
  $project_new_id = $invoice["project_id"];

  $url = url_prepare("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_ht=$ht". 
		    "&amp;tf_amount_ttc=$ttc".
		    "$param_status".
		    "&amp;tf_date_after=$date_after".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_dateexp_after=$dateexp_after".
		    "&amp;tf_dateexp_before=$dateexp_before".
		    "&amp;rd_inout=$inout".
		    "&amp;company_id=$comp_id".
		    "&amp;deal_id=$deal_id".
		    "&amp;project_id=$project_new_id".
		    "&amp;tf_company=$company".
		    "&amp;cba_archive=$archive".
                    "$url_ext"); 

  $dis_invoice = new OBM_DISPLAY ("DATA", $prefs, "invoice");
  if ($popup) {
    $dis_invoice->display_link = false;
    if ($ext_url != "") {
      $dis_invoice->display_ext = "get_id_url";
    } else if ( ($ext_widget != "") && ($ext_widget_text != "") ) { 
      $dis_invoice->display_ext = "get_id";
    }
    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }

  $dis_invoice->data_set = $inv_q;
  $dis_invoice->data_header = "top";
  $dis_invoice->data_url = $url;
  $block .= $dis_invoice->display("dis_data_invoice");
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice detail screen
// Parameters:
//   - $invoice[] : invoice hash parameters
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_consult($invoice) {
  global $auth, $display, $l_no_found, $l_err_reference;

  $id = $invoice["invoice_id"];
  $uid = $auth->auth["uid"];
  if ($id > 0) {
    $inv_q = run_query_invoice_detail($id);
    if ($inv_q->num_rows() != 1) {
      $display["msg"] .= display_err_msg($l_err_reference);
    }
    $pays = get_invoice_payment($id);
    $display["detailInfo"] = display_record_info($inv_q);
    $block = html_invoice_consult($inv_q, $pays);
  } else {
    $display["msg"] .= display_err_msg($l_err_reference);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display invoice detail screen
// Parameters:
//   - $inv_q : DBO invoice info
//   - $pays  : array : payments for the invoice
///////////////////////////////////////////////////////////////////////////////
function html_invoice_consult($inv_q, $pays) {
  global $path, $display, $set_theme;
  global $l_delete, $l_invoice, $l_payment, $l_no_payment, $l_total;
  global $l_list_payment, $l_company, $l_date, $l_number, $l_amount, $l_payments_todel;

  $i_id = $inv_q->f('invoice_id');

  $display["link"] = html_invoice_links($inv_q);
  $block = dis_invoice_detail ($inv_q);
  
  if (count($pays) > 0) {
    $l_payment_title = $l_list_payment;
    $block_payment = "
<table class=\"resultTable\">
<tr>
  <td class=\"resultHead\">$l_date</td>
  <td class=\"resultHead\">$l_company</td>
  <td class=\"resultHead\">$l_number</td>
  <td class=\"resultHead\">$l_amount</td>
  <td class=\"resultHead\">$l_invoice : $l_amount</td>
</tr>";
    $affected_amount_total = 0;
    foreach ($pays as $one_pay) {
      $id = $one_pay["invoice_id"];
      $affected_amount = $one_pay["affected_amount"];
      $amount = $one_pay["amount"];
      $company = $one_pay["company"];
      $number = $one_pay["number"];
      $date = $one_pay["date"];
      
      $affected_amount_total += $affected_amount;

      $block_payment .= "
<tr class=\"data0\">
  <td style=\"text-align:center;\"><a href=\"$path/payment/payment_index.php?action=detailconsult&amp;payment_id=$id\">$date</a></td>
  <td style=\"text-align:center;\">$company</td>
  <td><a href=\"$path/payment/payment_index.php?action=detailconsult&amp;payment_id=$id\">$number</a></td>
  <td style=\"text-align:right;\">$amount </td>
  <td class=\"data2\" style=\"text-align:right;\">$affected_amount </td>
</tr>";
    }

    $block_payment .= "
<tr class=\"data0\">
  <td colspan=\"4\" style=\"text-align:center;\">$l_total</td>
  <td class=\"resultHead\" style=\"text-align:right;\">$affected_amount_total</td>
</tr>
</table>";

  } else {
    $l_payment_title = $l_no_payment;
  }

  //  payments data :
  $block .= "<div class=\"detailHead\">$l_payment_title</div>
  $block_payment";

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
  global $set_theme, $ico_document, $ico_document_new, $ico_payment;
  global $l_invoice, $l_new, $l_module_document, $l_module_payment;
  global $l_document_add;
  global $path, $auth, $cgp_show, $popup_height, $popup_width;

  $id = $inv_q->f("invoice_id");
  $name = $inv_q->f("invoice_label");
  $company_id = $inv_q->f("invoice_company_id");
  $company_name = urlencode($inv_q->f("company_name"));
  $amount = $inv_q->f("invoice_amount_ttc");

  $uname = urlencode($name);

  // Document
  if ($cgp_show["module"]["document"]) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/invoice/invoice_index.php")."&amp;ext_id=$id&amp;ext_target=$l_invoice";
    $nb_document = run_query_global_document_nb ($id, "invoice");
    $block_doc = "
  <div class=\"portlet\" id=\"invoiceDocument\">
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_document_new\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;
	 \"><img src=\"$ico_document_new\" /></a>
	<a href=\"\" onclick=\"window.name='$l_invoice'; window.open('$url_doc_add','','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Payment
  if ($cgp_show["module"]["payment"]) {
    $url_pay = url_prepare("$path/payment/payment_index.php?action=search&amp;sel_invoice=$id");
    $url_pay_new = url_prepare("$path/payment/payment_index.php?action=new&amp;sel_invoice=$id&amp;invoice_name=$uname&amp;company_id=$company_id&amp;company_name=$company_name&amp;tf_amount=$amount");
    $nb_pay = get_global_linked_payment_nb($id, "invoice");
    $block_payment = "
  <div class=\"portlet\" id=\"invoicePayment\">
  <h1>$l_module_payment</h1>
  <ul>
    <li><a href=\"$url_inv\"><img src=\"$ico_payment\" alt=\"\" /></a>
        $l_module_payment ( <a href=\"$url_pay\">$nb_pay</a> )</li>
    <li><a href=\"$url_pay_new\"><img src=\"$ico_payment\" alt=\"\" /></a>
        <a href=\"$url_pay_new\">$l_new</a></li>
  </ul>
  </div>";
  }

  // Links Template  
  $block = "
  $block_doc
  $block_payment";

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
  $date = of_date_format($inv_q->f("date"), 1);
  $expiration_date = of_date_format($inv_q->f("expiration_date"), 1);
  $payment_date = of_date_format($inv_q->f("payment_date"), 1);
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

  $display["title"] = "<h1 class=\"title\">$l_invoice : $label</h1>";

  $url_comp = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id");
  if ($deal_id > 0) {
    $url_deal = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal_id");
    $deal_link = "<a href=\"$url_deal\"><img src=\"$ico_deal\" alt=\"\" /></a>";
  }
  if ($project_id > 0) {
    $url_proj = url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$project_id");
    $project_new_link = "<a href=\"$url_proj\"><img src=\"$ico_project\" alt=\"\" /></a>";
  }

  $block .= "
<div class=\"detail infos\">
    <h1>$title</h1>

    <table>
    <tr>
      <th>$l_company <a href=\"$url_comp\"><img src=\"$ico_company\" alt=\"\" /></a></th>
      <td>$company</td>
    </tr>
    <tr>
      <th>$l_deal $deal_link</th>
      <td>$deal</td>
    </tr>
    <tr>
      <th>$l_project $project_new_link</th>
      <td>$project</td>
    </tr>
    </table>
</div>

<div class=\"detail infos\">
    <h1>$l_invoice</h1>

    <table>
    <tr>
      <th>$l_label</th>
      <td>$label</td>
    </tr>
    <tr>
      $dis_company
      <th>$l_number</th>
      <td>$num</td>
    </tr>
    <tr>
      <th>$l_archive</th>
      <td>$archive</td>
    </tr>
    <tr>
      <th>$l_date</th>
      <td>$date</td>
    </tr>
    <tr>
      <th>$l_expiration_date</th>
      <td>$expiration_date</td>
    </tr>
    <tr>
      <th>$l_payment_date</th>
      <td>$payment_date</td>
    </tr>
    <tr>
      <th>$l_status</th>
      <td>$status</td>
    </tr>
    <tr>
      <th>$l_inout</th>
      <td>$inout</td>
    </tr>
    <tr>
      <th>$l_amount_ht</th>
      <td>$ht</td>
    </tr>
    <tr>
      <th>$l_amount_ttc</th>
      <td>$ttc</td>
    </tr>
    </table>
  </div>
    
  <div class=\"detail extra\">
     <h1>$l_comment</h1>
      $comment
  </div>";

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
  global $auth;

  $id = $invoice["invoice_id"];
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
    $date = of_isodate_format($inv_q->f("date"), 1);
    $expiration_date = of_isodate_format($inv_q->f("expiration_date"), 1);
    $payment_date = of_isodate_format($inv_q->f("payment_date"), 1);
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
    $datecomment = of_isodate_format();

  // New form and first time
  } elseif ($action == "new") {
    $date = date($c_php_isodate_format); 
    $expiration_date = $date;
    $payment_date = $date;
    $number = date ($default_invoice_numbering);
    // when creating from a deal, default to use same "inout" as the dealtype
    if ($d_id >= 1) {
      $inout = run_query_invoice_deal_get_inout ($d_id);
    }
    $usercomment = $uid;
    $datecomment = of_isodate_format();
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = of_isodate_format();
    $label .= "-".date("Y-m-d");
  }

  // If parameters have been given, they supercede the default action value
  if (isset($invoice["invoice_id"])) { $id = $invoice["invoice_id"]; }
  if (isset($invoice["label"])) { $label = stripslashes($invoice["label"]); }
  if (isset($invoice["number"])) { $number = $invoice["number"]; }
  if (isset($invoice["archive"])) { $archive = ($invoice["archive"] == 1 ? "checked" : ""); }
  if (isset($invoice["date"])) { $date = $invoice["date"]; }
  if (isset($invoice["expiration_date"])) { $expiration_date = $invoice["expiration_date"]; }
  if (isset($invoice["payment_date"])) { $payment_date = $invoice["payment_date"]; }
  if (isset($invoice["status"])) { $status = $invoice["status"]; }
  if (isset($invoice["inout"])) { $inout = $invoice["inout"]; }
  if (isset($invoice["ht"])) { $ht = $invoice["ht"]; }
  if (isset($invoice["amount_ttc"])) { $ttc = $invoice["amount_ttc"]; }
  if (isset($invoice["comment"])) { $comment = stripslashes($invoice["comment"]); }
  if (isset($invoice["add_comment"])) { $add_comment = stripslashes($invoice["add_comment"]); }
  if (isset($invoice["usercomment"])) { $usercomment = $invoice["usercomment"]; }
  if (isset($invoice["datecomment"])) { $datecomment = $invoice["datecomment"]; }

  if (isset($invoice["company_id"])) { $c_id = $invoice["company_id"]; }
  if (isset($invoice["company_name"])) { $c_name = stripslashes($invoice["company_name"]); }
  if (isset($invoice["company_new_id"])) { $c_new_id = $invoice["company_new_id"]; }
  if (isset($invoice["company_new_name"])) { $c_new_name = stripslashes($invoice["company_new_name"]); }
  if (isset($invoice["deal_id"])) { $d_id = $invoice["deal_id"]; }
  if (isset($invoice["deal_label"])) { $d_label = stripslashes($invoice["deal_label"]); }
  if (isset($invoice["deal_new_id"])) { $d_new_id = $invoice["deal_new_id"]; }
  if (isset($invoice["deal_new_label"])) { $d_new_label = stripslashes($invoice["deal_new_label"]); }
  if (isset($invoice["project_id"])) { $p_id = $invoice["project_id"]; }
  if (isset($invoice["project_name"])) { $p_name = stripslashes($invoice["project_name"]); }
  if (isset($invoice["project_new_id"])) { $p_new_id = $invoice["project_new_id"]; }
  if (isset($invoice["project_new_name"])) { $p_new_name = stripslashes($invoice["project_new_name"]); }

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
    <th>$l_inout</th>
    <td>
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $inout_c /> $l_client
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $inout_s /> $l_supplier
      </td>";

  if (($action == "detailupdate") || ($action == "update")) {
    $dis_comment = "
      $l_upd_comment
      <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>
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
    <input type=\"hidden\" name=\"invoice_id\" value=\"$id\" />
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

  $display["title"] = "<h1 class=\"title\">$l_invoice : $label</h1>";

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"invoice_index.php\">
<fieldset class=\"detail infos\">
  <legend>$title</legend>

  <table>
  <tr>
    <th>$l_company</th>
    <td>
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <th>$l_deal</th>
    <td>
      <a href=\"". url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$d_id") . "\">
      $d_label
      </a><input type=\"hidden\" name=\"deal_id\" value=\"$d_id\" />
      <input type=\"hidden\" name=\"deal_label\" value=\"$d_label\" />
      <input type=\"hidden\" name=\"deal_new_id\" value=\"$d_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.deal_new_id&amp;ext_widget_text=f_entity.deal_new_label','Deal','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"$ico_deal\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"deal_new_label\" value=\"$d_new_label\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <th>$l_project</th>
    <td>
      <a href=\"". url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$d_id") . "\">
      $p_name
      </a><input type=\"hidden\" name=\"project_id\" value=\"$p_id\" />
      <input type=\"hidden\" name=\"project_name\" value=\"$p_name\" />
      <input type=\"hidden\" name=\"project_new_id\" value=\"$p_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/project/project_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.project_new_id&amp;ext_widget_text=f_entity.project_new_name','Project','height=$popup_height,width=$popup_width,scrollbars=yes'); return false;\">
      <img src=\"$ico_project\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"project_new_name\" value=\"$p_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr>
  </table>
  </fieldset>

<fieldset class=\"detail infos\">
  <legend>$l_invoice</legend>

  <table>
  <tr>
    <th>$l_label</th>
    <td>
      <input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" />
    </td>
  </tr><tr>
    <th>$l_number</th>
    <td>
      <input name=\"tf_number\" type=\"text\" size=\"11\" value=\"$number\" />
    </td>
  </tr><tr>
    <th>$l_archive</th>
    <td><input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /></td>
  </tr><tr>
    <th>$l_date</th>
    <td>
      <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
    </td>
  </tr><tr>
    <th>$l_expiration_date</th>
    <td>
      <input type=\"text\" name=\"tf_expiration_date\" value=\"$expiration_date\" class=\"datePicker\"/>
    </td>
  </tr><tr>
    <th>$l_payment_date</th>
    <td>
    <input type=\"text\" name=\"tf_payment_date\" value=\"$payment_date\" class=\"datePicker\"/>
    </td>
  </tr><tr>
    <th>$l_status</th>
    <td>$sel_status</td>
  </tr><tr>
    $dis_inout
  </tr><tr>
    <th>$l_amount_ht</th>
    <td>
      <input name=\"tf_amount_ht\" type=\"text\" size=\"15\" value=\"$ht\" onChange=\"compute_tax(this.form, true);\" />
    </td>
  </tr><tr>
    <th>$l_tax_rate</th>
    <td><input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" size=\"8\" />
      <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" onClick=\"compute_tax(this.form);\" />
    </td>
  </tr><tr>
    <th>$l_amount_ttc</th>
    <td>
      <input name=\"tf_amount_ttc\" type=\"text\" size=\"15\" value=\"$ttc\" />
    </td>
  </tr>
  </table>
</fieldset>

<fieldset class=\"detail extra\">

    <legend>$l_comment</legend>
      $l_add_comment
      <input type=\"text\" name=\"tf_datecomment\" value=\"$datecomment\" class=\"datePicker\"/>
      $sel_usercomment
      <textarea name=\"ta_add_comment\" rows=\"6\" cols=\"78\">$add_comment</textarea>
      $dis_comment
</fieldset>

  <fieldset class=\"buttons\">
    $dis_button
  </fieldset>

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
    <input type=\"hidden\" name=\"invoice_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"invoice_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_delete\"
    onclick=\"if (confirm_del(this.form)) return true; else return false;\" />
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
// Display the invoice dashboard Index
// Parameters:
//   - $invoice : invoice hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_dashboard_index($invoice) {
  global $display, $set_theme, $path;
  global $l_year_detail, $l_year_to_year;

  $view = $invoice["dash_view"];
  $url_year_detail = url_prepare("invoice_index.php?action=dashboard");
  $url_year_to_year = url_prepare("invoice_index.php?action=dashboard&amp;dash_view=year_to_year");

  $block = "
    <div class=\"detailText\"><a href=\"$url_year_detail\">$l_year_detail</a></div>
    <div class=\"detailText\"><a href=\"$url_year_to_year\">$l_year_to_year</a></div>
";

  if ($view != "") {
    $func = "dis_invoice_dashboard_$view";
    $block .= $func($invoice);
  } else {
    $block .= dis_invoice_dashboard($invoice);
  }
 
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
  $inv = run_query_invoice_amounts($date_ranges, "invoice_date", $invoice["tasktype"]);
  $url_prev = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_prev");
  $url_next = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_next");

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice["tasktype"];
  if (! is_array($tt_ids) && ($tt_ids == "")) {
    $tt_ids = "0";
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, "tt", $url_tt);

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
    // Get the status list to query for total detail
    $total_status_param .= "&amp;sel_status[]=$s_id";
    
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
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ht</a></td>";
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
          <td>&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search$total_status_param&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ht</a></td>";
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

  $chart_bil_params .= "&amp;title=".urlencode($title)."&amp;values=".urlencode(serialize($values)) . "&amp;labels=".urlencode(serialize($labels)). "&amp;xlabels=".urlencode(serialize($xlabels));

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
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;tf_date_before=$month_end$url_sel_tt\">$f_amount_ht</a></td>";
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
  $title = "$l_potential $year";
  $plots["new_bar"] = array(1, 0);
  $plots["legends"] = array($l_order_no_bill, $l_billed);
  $plots["values"] = array($values_pot, $values);
  $plots["labels"] = array($labels_pot);

  $chart_pot_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots))."&amp;xlabels=".urlencode(serialize($xlabels));

  // Payment infos ------------------------------------------------------------

  $date_ranges = array(array("$year-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, "invoice_expiration_date", $invoice["taskttype"]);

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
          <td class=\"$data\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;cba_archive=1&amp;tf_dateexp_after=$month_start&amp;tf_dateexp_before=$month_end$url_sel_tt\">$dis_amount_ttc</a></td>";
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
          <td>&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;cba_archive=1&amp;tf_dateexp_after=$month_start&amp;tf_dateexp_before=$month_end$url_sel_tt\">$dis_amount_ttc</a></td>";
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
  $plots["values"] = array($values_pay, $values);
  $plots["labels"] = array($labels);

  $chart_pay_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots))."&amp;xlabels=".urlencode(serialize($xlabels));


  $block = "
    <form method=\"post\" name=\"f_search\" action=\"$url_form\">

    <div class=\"search\">
      <div class=\"searchLabel\">
        <a href=\"javascript: return false;\"
          onclick=\"yearfield=window.document.getElementById('year');
                    newyear=Number(yearfield.innerHTML)-1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/previous.png\" alt=\"\" /></a>
      </div>
      <div class=\"searchLabel\" id=\"year\">$year</div>
      <div class=\"searchLabel\">
        <a href=\"javascript: return false;\"
          onclick=\"yearfield=window.document.getElementById('year');
                    newyear=Number(yearfield.innerHTML)+1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/next.png\" alt=\"\" /></a>
      </div>

      <div class=\"detailText\">$block_tt</div>
      <div class=\"searchLabel\">&nbsp;<br>
        <input type=\"hidden\" name=\"year\" value=\"$year\" />
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
      </div>

      <div class=\"searchEnd\"></div>

    </div>
    </form>

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
// Display the invoice dashboard year to year comparison
// Parameters:
//   - $invoice : invoice hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_dashboard_year_to_year($invoice) {
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
  $inv = run_query_invoice_amounts($date_ranges, "invoice_date", $invoice["tasktype"]);

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice["tasktype"];
  if (! is_array($tt_ids) && ($tt_ids == "")) {
    $tt_ids = "0";
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, "tt", $url_tt);

  // Billed infos Year-1 and Year ----------------------------------------------

  $date_ranges = array(array("$year_prev-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, "invoice_date", $invoice["tasktype"]);

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month_prev = "$year_prev-$month";
      $year_month = "$year-$month";
    } else {
      $year_month_prev = "$year_prev-0$month";
      $year_month = "$year-0$month";
    }
    $xlabels[$month-1] = $l_monthsofyear[$month-1];

    $status_info_prev = $inv["$year_month_prev"]["billed"]["total"];
    $amount_ht = $status_info_prev["amount_ht"];
    $values_prev[$month-1] = $amount_ht;
    $labels_prev[$month-1] = number_format($amount_ht/1000, 0, ",", " ");

    $status_info = $inv["$year_month"]["billed"]["total"];
    $amount_ht = $status_info["amount_ht"];
    $values[$month-1] = $amount_ht;
    $labels[$month-1] = number_format($amount_ht/1000, 0, ",", " ");

    $status_info_pot = $inv["$year_month"]["potential"]["total"];
    $amount_ht_pot = $status_info_pot["amount_ht"] + $values[$month-1];
    $values_pot[$month-1] = $amount_ht_pot;
    $labels_pot[$month-1] = number_format($amount_ht_pot/1000, 0, ",", " ");
  }
  $title = "$l_billed (k€) $year_prev / $year";
  $plots["new_bar"] = array(1, 1);
  $plots["legends"] = array("$year_prev", "$year");
  $plots["values"] = array($values_prev, $values);
  $plots["labels"] = array($labels_prev, $labels);

  $chart_ytoy_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots)) . "&amp;xlabels=".urlencode(serialize($xlabels));

  $title = "$l_potential (k€) $year_prev / $year";
  $plots["values"] = array($values_prev, $values_pot);
  $plots["labels"] = array($labels_prev, $labels_pot);

  $chart_ytoyp_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots)) . "&amp;xlabels=".urlencode(serialize($xlabels));

  $block = "
    <form method=\"post\" name=\"f_search\" action=\"$url_form\">

    <div class=\"search\">
      <div class=\"searchLabel\">
        <a href=\"javascript: return false;\"
          onclick=\"yearfield=window.document.getElementById('year');
                    newyear=Number(yearfield.innerHTML)-1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/previous.png\" alt=\"\" /></a>
      </div>
      <div class=\"searchLabel\" id=\"year\">$year</div>
      <div class=\"searchLabel\">
        <a href=\"javascript: return false;\"
          onclick=\"yearfield=window.document.getElementById('year');
                    newyear=Number(yearfield.innerHTML)+1;
                    yearfield.innerHTML=newyear;
                    document.forms.f_search.year.value=newyear;
                    return false;\">
        <img border=\"0\" src=\"".C_IMAGE_PATH."/$set_theme/next.png\" alt=\"\" /></a>
      </div>

      <div class=\"detailText\">$block_tt</div>
      <div class=\"searchLabel\">&nbsp;<br>
        <input type=\"hidden\" name=\"year\" value=\"$year\" />
        <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
        <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
      </div>

      <div class=\"searchEnd\"></div>
    </div>

    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_ytoy_params\" alt=\"\" />
    <br />
    <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_ytoyp_params\" alt=\"\" />
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
