<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_index.php                                            //
//     - Desc : payment display File                                         //
// 2002-07-16 Pierre Baudracco (from Nicolas Roman)                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["payment_date"] = $l_date;
$fieldnames["company_name"] = $l_company;
$fieldnames["payment_amount"] = $l_amount;
$fieldnames["payment_number"] = $l_number;
$fieldnames["paymentkind_label"] = $l_kind;
$fieldnames["payment_comment"] = $l_comment;

$fieldnames["invoice_label"] = $l_invoice_label;
$fieldnames["invoice_date"] = $l_invoice_date;
$fieldnames["invoice_amount_ttc"] = $l_invoice_ttc;
$fieldnames["invoice_amount_ht"] = $l_invoice_ht;
$fieldnames["invoice_number"] = $l_invoice_number;
$fieldnames["invoice_paid"] = $l_invoice_paid;
$fieldnames["invoice_company"] = $l_invoice_company;
$fieldnames["invoice_status"] = $l_invoice_status;
$fieldnames["invoice_deal"] = $l_deal;


///////////////////////////////////////////////////////////////////////////////
// Display Payment specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_payment(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_frs, $col_client;

  if ($fieldname == "payment_date") {
    $date = $OD->data_set->f($fieldname);
    $date = date_format($date);
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f("payment_id");
    $res["name"] = $date;
  }

  elseif ($fieldname == "payment_comment") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f("payment_id");
  }

  elseif ($fieldname == "company_name") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f("payment_company_id");
  }

  // amount column color 
  elseif (($fieldname == "payment_amount")
	  || ($fieldname == "pay_inv_paid")) {
    $amount = $OD->data_set->f($fieldname);
    if ( ($OD->data_set->f("payment_inout") == '+') // for a payment
	 // for an invoice
	 || ($OD->data_set->f("invoice_inout") == '+') ) {
      $couleur = $col_client;
    } else {
      $couleur = $col_frs;
    }
    $res["name"] = "<font color=\"#$couleur\">$amount</font>";
    $res["txt_name"] = $amount;
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Payment search form
// Parameters:
//   - $params[] : hash with parameters values
///////////////////////////////////////////////////////////////////////////////
function dis_payment_search_form($params="") {
  global $cgp_hide;

  $kinds = get_payment_kinds();
  $accs = get_accounts();
  $block .= html_payment_search_form($params, $kinds, $accs);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: XHTML Payment search Form
// Parameters:
//   - $params[] : default form values
//   - $kinds    : payment kinds infos
//   - $accs     : accounts infos
///////////////////////////////////////////////////////////////////////////////
function html_payment_search_form($params, $kinds, $accs) {
  global $l_find, $l_all, $c_all;
  global $l_comment, $l_number, $l_amount, $l_date, $l_after, $l_before;
  global $l_inout, $l_kind, $l_account;
  global $l_company, $l_include_checked;
  global $l_both, $l_received, $l_emitted;

  $comment = $params["comment"];
  $number = $params["number"];
  $amount = $params["amount"];
  $date_after = $params["date_after"];
  $date_before = $params["date_before"];
  $inout = $params["inout"];
  $kind = $params["kind"];
  $account = $params["account"];
  $company = $params["company"];
  $ichecked = ($params["checked"]) ? "checked" : "" ;

  // Inout
  $bcheck = (($inout == "$c_all")||($inout=="")) ? "checked" : "";
  $pcheck = ($inout == "+") ? "checked" : "";
  $mcheck = ($inout == "-") ? "checked" : "";

  $rd_inout = "
    <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $bcheck />$l_both &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck />(+) $l_received &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck />(-) $l_emitted &nbsp;
    ";

  // Account select
  $sel_account ="
    <select name=\"sel_account\">
     <option value=\"$c_all\">$l_all</option>\n";
  foreach($accs as $a_id => $one_acc) {
    $alabel = $one_acc["label"];
    $aselect = ($a_id == $account) ? "selected" : "";
    $sel_account .= "<option value=\"$a_id\" $aselect>$alabel</option>";
  }
  $sel_account .= "</select>";
  
  // Kind select
  $sel_kind ="
    <select name=\"sel_kind\">
     <option value=\"$c_all\">$l_all</option>";
  foreach($kinds as $k_id => $one_kind) {
    $klabel = $one_kind["label"];
    $kselect = ($k_id == $kind) ? "selected" : "";
    $sel_kind .= "<option value=\"$k_id\" $kselect>$klabel</option>";
  }
  $sel_kind .= "</select>";

  $block = "
    <form method=\"get\" name=\"f_search\" action=\"payment_index.php?action=search\">
     <div class=\"search\">

      <div class=\"searchLabel\">$l_comment<br />
       <input name=\"tf_comment\" size=\"16\" value=\"$comment\">
      </div>

      <div class=\"searchLabel\">$l_number<br />
       <input name=\"tf_number\" size=\"16\" maxlength=\"24\" value=\"$number\">
      </div>

      <div class=\"searchLabel\">$l_amount<br />
       <input name=\"tf_amount\" size=\"12\" value=\"$amount\">
      </div>

      <div class=\"searchLabel\">$l_date $l_after<br />
        <script>calendar('tf_date_after','$date_after')</script>
      </div>

      <div class=\"searchLabel\">$l_date $l_before<br />
        <script>calendar('tf_date_before','$date_before')</script>
      </div>

      <div class=\"searchLabel\">$l_inout<br />
       $rd_inout
      </div>

      <div class=\"searchLabel\">$l_company<br />
       <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
      </div>

      <div class=\"searchLabel\">$l_account<br />
       $sel_account
      </div>

      <div class=\"searchLabel\">$l_kind<br />
       $sel_kind
      </div>

      <div class=\"searchLabel\">$l_include_checked<br />
       <input type=\"checkbox\" name=\"cb_checked\" value=\"y\" $ichecked>
      </div>

      <div class=\"searchLabel\"><br />
       <input type=\"hidden\" name=\"action\" value=\"search\">
       <input type=\"submit\" name=\"submit\" value=\"$l_find\">  
      </div>

      <div class=\"searchEnd\"></div>
     </div>
    </form>
";

   return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Payment search result
// Parameters:
//   - $params[] : params (search criteria)
///////////////////////////////////////////////////////////////////////////////
function dis_payment_search_list($params) {
  global $display, $l_found, $l_no_found;
  global $auth;

  $prefs = get_display_pref($auth->auth["uid"], "payment");
  $obm_q = run_query_payment_search($params);
  $nb_payment = $obm_q->num_rows_total();
  if ($nb_payment == 0) {
    $display["msg"] .= display_warn_msg($l_no_found);
  } else {
    $display["msg"] .= display_info_msg("$nb_payment $l_found");
    $block = html_payment_search_list($obm_q, $prefs, $params);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Payment search result
// Parameters : 
//   - $obm_q     : list of payments to display 
//   - $prefs     : the fields which have to be displayed
//   - $nb        : nb payments returned by the search query 
///////////////////////////////////////////////////////////////////////////////
function html_payment_search_list($obm_q, $prefs, $params) {
  global $l_add, $l_close;

  $popup = $params["popup"];
  $comment = urlencode($payment["comment"]);
  $number = urlencode($payment["number"]);
  $amount = urlencode($payment["amount"]);
  $date_after = $payment["date_after"];
  $date_before = $payment["date_before"];
  $inout = $payment["inout"];
  $kind = $payment["kind"];
  $account = $payment["account"];
  if ($popup) {
    $ext_action = $params["ext_action"];
    $ext_url = $params["ext_url"];
    $ext_id = $params["ext_id"];
    $ext_target = $params["ext_target"];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  $url = url_prepare("payment_index.php?action=search".
		    "&amp;tf_comment=$comment".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount=$amount".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_date_after=$date_after".
		    "&amp;sel_kind=$kind".
		    "&amp;sel_account=$account".
		    "&amp;rd_inout=$inout".
		    "$url_ext");

  $dis_p = new OBM_DISPLAY("DATA", $prefs, "payment");
  if ($popup) {
    $dis_p->display_link = false;
    $dis_p->data_cb_text = "X";
    $dis_p->data_idfield = "payment_id";
    $dis_p->data_cb_name = "cb_pay";
    $dis_p->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $dis_p->data_form_end = "
      <p class=\"detailButton\">
        <p class=\"detailButtons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"param_ext\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
        </p>
      </p>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
  }
  $dis_p->data_set = $obm_q;
  $dis_p->data_url = $url;
  $dis_p->data_header = "both";

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_p->display("dis_data_payment");
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Payment detail
// Parameters:
//   - $params[] : payment values (id)
///////////////////////////////////////////////////////////////////////////////
function dis_payment_consult($params) {
  global $display, $l_no_found, $path, $l_query_error;

  $view = $params["view"];
  $id = $params["payment_id"];

  if ($id > 0) {
    $pay_q = run_query_payment_detail($id);
    $invs = get_payment_invoices($id);
    $display["detailInfo"] = display_record_info($pay_q);
    $block = html_payment_consult($pay_q, $invs);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Payment Consultation
// Parameters:
//   - $p_q  : payment database result
//   - $invs : array : invoices connected to the payment
///////////////////////////////////////////////////////////////////////////////
function html_payment_consult($p_q, $invs) {
  global $path, $display, $set_theme, $ico_company;
  global $l_payment, $l_invoice, $l_company, $l_received, $l_emitted;
  global $l_comment, $l_number, $l_amount, $l_date, $l_label, $l_no_invoice;
  global $l_account, $l_kind, $l_inout, $l_total;

  $id = $p_q->f("payment_id");
  $c_id = $p_q->f("payment_company_id");
  $c_name = $p_q->f("company_name");
  $kind = $p_q->f("paymentkind_label");
  $date = date_format($p_q->f("date"));
  $number = $p_q->f("payment_number");
  $amount = $p_q->f("payment_amount");
  $inout = $p_q->f("payment_inout");
  $checked = $p_q->f("payment_checked");
  $comment = $p_q->f("payment_comment");
  $ad1 = $p_q->f("company_address1");
  $zip = $p_q->f("company_zipcode");
  $town = $p_q->f("company_town");

  if ($inout == "+") {
    $dis_inout = $l_received;
  } else {
    $dis_inout = $l_emitted;
  }

  if (count($invs) > 0) {
    $l_invoice_title = $l_invoice;
    $block_invoice = "
<table class=\"resultTable\">
<tr>
  <td class=\"resultHead\">$l_date</td>
  <td class=\"resultHead\">$l_company</td>
  <td class=\"resultHead\">$l_label</td>
  <td class=\"resultHead\">$l_amount</td>
  <td class=\"resultHead\">$l_payment : $l_amount</td>
</tr>";
    $affected_amount_total = 0;
    foreach ($invs as $one_inv) {
      $id = $one_inv["id"];
      $affected_amount = $one_inv["affected_amount"];
      $inv_amount = $one_inv["invoice_amount"];
      $company = $one_inv["company"];
      $label = $one_inv["label"];
      $date = $one_inv["date"];
      
      $affected_amount_total += $affected_amount;

      $block_invoice .= "
<tr class=\"data0\">
  <td style=\"text-align:center;\">$date</td>
  <td style=\"text-align:center;\">$company</td>
  <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$id\">$label</a></td>
  <td style=\"text-align:right;\">$inv_amount </td>
  <td class=\"data2\" style=\"text-align:right;\">$affected_amount </td>
</tr>";
    }

    $block_invoice .= "
<tr class=\"data0\">
  <td colspan=\"4\" style=\"text-align:center;\">$l_total</td>
  <td class=\"resultHead\" style=\"text-align:right;\">$affected_amount_total</td>
</tr>
</table>";

  } else {
    $l_invoice_title = $l_no_invoice;
  }

  $display["title"] = "<div class=\"title\">$l_payment : $date $c_name $amount</div>";

  $block = "
<div class=\"detailHead\">$title</div>

  <div class=\"detailHead\">$l_company</div>
    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company 
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\">
      <img src=\"".C_IMAGE_PATH."/$set_theme/$ico_company\" alt=\"[details]\" /></a>
      </td>
      <td class=\"detailText\">$c_name<br />$ad1<br/>$zip $town</td>
    </tr>
    </table>

  <div class=\"detailHead\">$l_payment</div>

  <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_amount</td>
      <td class=\"detailText\">$amount</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_inout</td>
      <td class=\"detailText\">$inout $dis_inout</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_kind</td>
      <td class=\"detailText\">$kind</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$number</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_account</td>
      <td class=\"detailText\">$account</td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailText\">$comment</td>
    </tr>
   </table>

  <div class=\"detailHead\">$l_invoice_title</div>

  $block_invoice
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Payment invoice detail form
// Parameters:
//   - $params[] : payment values (id)
///////////////////////////////////////////////////////////////////////////////
function dis_payment_invoice($params) {
  global $display, $l_no_found, $path;

  $id = $params["payment_id"];

  if ($id > 0) {
    $amounts = get_payment_amount_info($id);
    $block = html_payment_invoice($amounts, $params);
  }

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// XHTML Display Payment Invoice detail Form
// Parameters:
//   - $amounts  : array : amounts infos with invoices infos
//   - $params[] : payment values
///////////////////////////////////////////////////////////////////////////////
function html_payment_invoice($amounts, $params) {
  global $display, $set_theme, $l_update, $l_no_invoice;
  global $l_payment, $l_invoice, $l_company, $l_received, $l_emitted;
  global $l_comment, $l_number, $l_amount, $l_date, $l_label, $l_already_paid;
  global $l_total;

  $id = $params["payment_id"];
  $invs = $amounts["invoices"];
  $paid = $amounts["paid_amount"];

  if (count($invs) > 0) {
    $l_invoice_title = $l_invoice;
    $block_invoice = "
<table class=\"resultTable\">
<tr>
  <td class=\"resultHead\">$l_date</td>
  <td class=\"resultHead\">$l_company</td>
  <td class=\"resultHead\">$l_label</td>
  <td class=\"resultHead\">$l_amount</td>
  <td class=\"resultHead\">$l_already_paid</td>
  <td class=\"resultHead\">$l_payment : $l_amount</td>
</tr>";
    $affected_amount_total = 0;
    foreach ($invs as $one_inv) {
      $i_id = $one_inv["id"];
      // if params sent (eg: invoice_update), take them else database infos
      if (isset($params["invoice"]["$i_id"])) {
	$affected_amount = $params["invoice"]["$i_id"];
      } else {
	$affected_amount = $one_inv["affected_amount"];
      }
      $inv_amount = $one_inv["invoice_amount"];
      $company = $one_inv["company"];
      $label = $one_inv["label"];
      $date = $one_inv["date"];
      $already_paid = $one_inv["alreadydate"];
      
      $affected_amount_total += $affected_amount;

      $block_invoice .= "
<tr class=\"data1\">
  <td style=\"text-align:center;\">$date</td>
  <td style=\"text-align:center;\">$company</td>
  <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$label</a></td>
  <td style=\"text-align:right;\">$inv_amount </td>
  <td style=\"text-align:right;\">$already_paid </td>
  <td class=\"data0\" style=\"text-align:right;\"><input type=\"text\" name=\"data-inv-$i_id\" value=\"$affected_amount\" /></td>
</tr>";
    }

    $block_invoice .= "
<tr class=\"data0\">
  <td colspan=\"5\" style=\"text-align:center;\">$l_total</td>
  <td class=\"resultHead\" style=\"text-align:right;\">$affected_amount_total</td>
</tr>
</table>";

    $dis_invoice = "
  <form method=\"post\" name=\"f_inv\" action=\"payment_index.php\">
  $block_invoice

  <div class=\"detailButton\">
  <p class=\"detailButtons\">
    <input type=\"hidden\" name=\"action\" value=\"invoice_update\" />
    <input type=\"hidden\" name=\"payment_id\" value=\"$id\" />
    <input type=\"submit\" value=\"$l_update\" />
  </p>
  </div>
  </form>
";
  } else {
    $l_invoice_title = $l_no_invoice;
  }

  $display["title"] = "<div class=\"title\">$l_payment : $date $c_name $paid</div>";

  $block = "
<div class=\"detailHead\">$title</div>

  <div class=\"detailHead\">$l_invoice_title</div>
  $dis_invoice
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Payment Form
// Parameters:
//   - $action    : action called
//   - $params[]  : parameters : default values
///////////////////////////////////////////////////////////////////////////////
function dis_payment_form($action, $params) {
  global $display, $cg_com, $l_aduplicate;

  $p_id = $params["payment_id"];
  if ($p_id > 0) {
    $p_q = run_query_payment_detail($p_id);
  } elseif ($params["id_duplicated"] > 0) {
    $p_q = run_query_payment_detail($params["id_duplicated"]);
    $params["title"] = $p_q->f("payment_date") . " - " . $p_q->f("payment_amount") . " - " . $p_q->f("company_name") . " - $l_aduplicate";
  }

  $kinds = get_payment_kinds();
  $accs = get_accounts();

  $block = html_payment_form($action, $p_q, $kinds, $accs, $params);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// HTML Display Payment Form
// Parameters:
//   - $action    : action called
//   - $p_q       : payment database result
//   - $kinds     : Payment kinds array 
//   - $accs      : Accouns array 
//   - $params[]  : parameters : form values
///////////////////////////////////////////////////////////////////////////////
function html_payment_form($action, $p_q, $kinds, $accs, $params) {
  global $set_theme, $ico_company, $l_company,$ico_add,$ico_crow;
  global $l_payment, $l_invoice, $l_account, $l_kind, $l_date, $l_number;
  global $l_amount, $l_inout, $l_received, $l_emitted, $l_comment;
  global $l_update, $l_checkdelete, $l_insert;
  global $popup_width, $popup_height;
  global $l_header_new, $ico_mini_cal;
  global $display, $path, $auth, $c_undef, $l_undef;

  $uid = $auth->auth["uid"];

  // if update mode and first time values are taken from database
  if (($action == "detailupdate") || ($action == "detailduplicate")) {
    $id = $p_q->f("payment_id");
    $c_id = $p_q->f("payment_company_id");
    $c_name = $p_q->f("company_name");
    $kind = $p_q->f("payment_paymentkind_id");
    $date = $p_q->f("date");
    $date = ($date != 0) ? date_format($date, 1) : "";
    $number = $p_q->f("payment_number");
    $amount = $p_q->f("payment_amount");
    $inout = $p_q->f("payment_inout");
    $checked = $p_q->f("payment_checked");
    $comment = $p_q->f("payment_comment");
    $title = $params["title"];

  // New form and first time
  } elseif ($action == "new") {
    $title = $l_header_new;
    $date = date("Y-m-d");
    $inout = '+';
  }

  // If parameters have been given, they supercede the default action value
  if (isset($params["company_id"])) { $c_id = $params["company_id"]; }
  if (isset($params["company_name"])) { $c_name = $params["company_name"]; }
  if (isset($params["comp_new_id"])) { $c_new_id = $params["comp_new_id"]; }
  if (isset($params["comp_new_name"])) { $c_new_name = $params["comp_new_name"]; }
  if (isset($params["payment_id"])) { $id = $params["payment_id"]; }
  if (isset($params["invoice_id"])) { $invoice_id = $params["invoice_id"]; }
  if (isset($params["account"])) { $account = $params["account"]; }
  if (isset($params["kind"])) { $kind = $params["kind"]; }
  if (isset($params["date"])) { $date = $params["date"]; }
  if (isset($params["number"])) { $number = stripslashes($params["number"]); }
  if (isset($params["amount"])) { $amount = stripslashes($params["amount"]); }
  if (isset($params["inout"])) { $inout = stripslashes($params["inout"]); }
  if (isset($params["checked"])) { $checked = stripslashes($params["checked"]); }
  if (isset($params["comment"])) { $comment = stripslashes($params["comment"]); }

  // Payment kind field
  $block_kind = of_category_dis_entity_form("payment", "kind", $kinds, "mono", $kind, "none");

  // Invoice field (new from Invoice)
  if (isset($invoice_id)) {
    $dis_invoice = "
    <tr>
      <td class=\"detailLabel\">$l_invoice</td>
      <td class=\"detailForm\">".$params["invoice_name"]."</td>
    </tr>";
  }

  // Account select
  $sel_account ="
    <select name=\"sel_account\">
     <option value=\"$c_undef\">$l_undef</option>\n";
  foreach($accs as $a_id => $one_acc) {
    $alabel = $one_acc["label"];
    $aselect = ($a_id == $account) ? "selected" : "";
    $sel_account .= "<option value=\"$a_id\" $aselect>$alabel</option>";
  }
  $sel_account .= "</select>";

  // Type radio
  $pcheck = ($inout == '+') ? "checked" : "";
  $mcheck = ($inout == '-') ? "checked" : "";
  $rd_type = "
    <input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck $readonly/>$l_received &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck $readonly/>$l_emitted &nbsp;
";

  // Company Display
  $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"window.open('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company','height=$popup_height,width=$popup_width'); return false;\">
      <img src=\"". C_IMAGE_PATH . "/$set_theme/$ico_company\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";

  // UPDATE
  if (($action == "detailupdate") || ($action == "update")) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"payment_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action=="new") || ($action=="insert") || ($action=="detailduplicate")) {
    $dis_button = "
      <input type=\"hidden\" name=\"sel_invoice\" value=\"$invoice_id\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display["title"] = "<div class=\"title\">$l_payment : $title</div>";

  // --- HTML Template --------------------------------------------------------
  $block .= "
    <form method=\"get\" name=\"f_entity\" onsubmit=\"if (check_payment(this)) return true; else return false;\" action=\"".url_prepare("payment_index.php")."\">

    <div class=\"detailHead\">$l_payment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_company</td>
      <td class=\"detailForm\">$dis_company</td>
    </tr>
    $dis_invoice
    <tr>
      <td class=\"detailLabel\">$l_amount</td>
      <td class=\"detailForm\"><input type=\"text\" id=\"tf_amount\" name=\"tf_amount\" size=\"13\" maxlength=\"13\" value=\"$amount\" /></td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailForm\">
        <script>calendar('tf_date','$date')</script>
      </td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_inout</td>
      <td class=\"detailForm\">$rd_type</td>
    </tr>
    $block_kind
    <tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\"><input type=\"text\" name=\"tf_number\" size=\"24\" maxlength=\"24\" value=\"$number\" /></td>
    </tr>
    <tr>
      <td class=\"detailLabel\">$l_account</td> 
      <td class=\"detailForm\">$sel_account</td>
    </tr>
    </table>

    <div class=\"detailHead\">$l_comment</div>

    <table class=\"detail\">
    <tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailForm\"><textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">$comment</textarea>
    </tr>
    </table>

    <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
    </div>
    </form>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the validation that the payment can be deleted, and the form
// Parameters:
//   - $p_id : payment id
///////////////////////////////////////////////////////////////////////////////
function dis_can_delete_payment($p_id) {
  global $display, $l_delete, $l_can_delete, $l_back;

  $url = url_prepare("payment_index.php");

  $dis_back = "<form name=\"form_back\" method=\"get\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"payment_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"payment_id\" value=\"$p_id\" />
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
// Display: the Payment Display preference screen
// Parameters:
//   - $prefs : Display preferences
///////////////////////////////////////////////////////////////////////////////
function dis_payment_display_pref ($prefs) {
  global $l_payment_options, $l_invoice_options, $l_et_options;
  global $set_theme ;
 
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $prefs, "payment");
  $dis_pref->pref_title = $l_payment_options;
  $dis_pref->pref_dis_help = 1;

  $block .= $dis_pref->display();

  return $block;
}


?>