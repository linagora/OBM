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
$fieldnames["invoice_deal"] = $l_deal;
$fieldnames["invoice_company"] = $l_company;
$fieldnames["invoice_label"] = $l_label;
$fieldnames["invoice_amount_ht"] = $l_amount_ht;
$fieldnames["invoice_amount_ttc"] = $l_amount_ttc;
$fieldnames["invoice_status"] = $l_status;
$fieldnames["invoice_date"] = $l_date;
$fieldnames["invoice_paid"] = $l_paid;
$fieldnames["deal_label"] = $l_invoice_deal_label;
$fieldnames["deal_number"] = $l_invoice_deal_number;
$fieldnames["tasktype_label"] = $l_invoice_deal_category_label;
$fieldnames["deal_company_name"] = $l_invoice_company;
$fieldnames["deal_todo"] = $l_invoice_dealtodo;
$fieldnames["dealtype_label"] = $l_invoice_dealtype_label;
$fieldnames["dealstatus_label"] = $l_invoice_dealstatus ;
$fieldnames["deal_datealarm"] = $l_invoice_deal_datealarm;
$fieldnames["payment_label"] = $l_invoice_payment_label;
$fieldnames["payment_number"] = $l_invoice_payment_number;
$fieldnames["payment_expect_date"] = $l_invoice_payment_expect_date;
$fieldnames["payment_date"] = $l_invoice_payment_date;
$fieldnames["payment_amount"] = $l_invoice_payment_amount;


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
    <form method=\"post\" name=\"form_invoice\"
        onSubmit=\"if (check_form(this) == false) return false;
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
      $l_both &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $rb_both />
      $l_client &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $rb_client />
      $l_supplier &nbsp;
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $rb_four />
    </div>

    <div class=\"searchLabel\">$l_deal<br />
      <input type=\"text\" name=\"tf_deal\" value=\"$deal\" />
    </div>

    <div class=\"searchLabel\">$l_company<br />
      <input type=\"text\" name=\"tf_company\" value=\"$company\" />
    </div>

    <div class=\"searchLabel\">&nbsp;<br>
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
// Display the Invoice search result
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
// Display Invoice Form              
// Parameters:
//   - $action    : action called
//   - $invoice[] : invoice hash parameters
//   - $inv_q     : if updating, contains the invoice current info
//   - $stat_q    : Database result with status list
///////////////////////////////////////////////////////////////////////////////
function html_invoice_form($action, $invoice, $inv_q, $stat_q) {
  global $display, $set_theme, $ico_company,$ico_contact;
  global $l_label, $l_number, $l_comment, $l_deal, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_date, $l_pick_deal, $l_inout, $l_client, $l_supplier;
  global $l_insert,$l_update, $l_delete, $l_invoice;
  global $default_invoice_numbering;
  global $q_company;
  global $default_tax, $l_tax_rate, $l_compute_tax; 

  if ($action == "new" && $p_deal_linked != "") 
    $js_fct_invoice = "check_invoice_link";
  else 
    $js_fct_invoice = "check_invoice";
  // depending on the action, we fill or not the form 
  // we use those vars to skip tests on the action value
  // for each form field...

  if ($action == "new") {
    $invoice = "";
    $label = $date = $comment = $invoice_status = "";
    $inout = $ht = $ttc = "";
    $number = date ($default_invoice_numbering);
    // when creating from a deal, default to use same "inout" as the dealtype
    if ($p_deal_linked != "") {
      $inout = run_query_deal_get_inout ($p_deal_linked);
    }
  } else {
    $label = $inv_q->f("invoice_label");
    $number = $inv_q->f("invoice_number");
    $comment = $inv_q->f("invoice_comment");
    $invoice_status = $inv_q->f("invoice_status_id");
    $date = $inv_q->f("invoice_date");
    $inout = $inv_q->f("invoice_inout");
    $ht = $inv_q->f("invoice_amount_ht");
    $ttc = $inv_q->f("invoice_amount_ttc");
    $invoice = $inv_q->f("invoice_id");
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = "";
    $label .= "-".date("Y-m-d");
  }

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
       $l_client &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"+\" $inout_c />
       $l_supplier &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"-\" $inout_s />
      </td>";

  // Buttons
  if (($action == "update") || ($action == "detailupdate")) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"update\"/>
    <input type=\"hidden\" name=\"param_invoice\" value=\"$invoice\" />
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
  <form method=\"post\" action=\"invoice_index.php\">

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
      <input name=\"tf_date\" type=\"text\" size=\"12\" value=\"$date\">
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
    <td class=\"detailForm\"><input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" size=\"8\" /></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_compute_tax</td>
    <td class=\"detailForm\">
      <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" onClick=\"compute_tax(this.form);\"></td>
  </tr><tr>
    <td class=\"detailLabel\">$l_amount_ttc</td>
    <td class=\"detailForm\">
      <input name=\"tf_amount_ttc\" type=\"text\" size=\"15\" value=\"$ttc\">
    </td>
  </tr><tr>
    <td class=\"detailLabel\">$l_comment</td>
    <td class=\"detailForm\">
      <textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">$comment</textarea>
    </td>
  </tr>
  </table>

  <div class=\"detailButton\">
    <p class=\"detailButtons\">$dis_button</p>
  </div>

  </form>";

  return $block;
}



//////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of invoices
//////////////////////////////////////////////////////////////////////////
// Parameters :
//    $q_options_display : list of fields to display or not
///////////////////////////////////////////////////////////////////////////
function dis_invoice_display_pref ($pref_inv_q, $pref_deal_q) {
  // Label
  global $l_invoice_options, $l_deal_options;
  //  global $set_theme;
   
  $block = "<center>
    <table>
    <tr valign=\"top\">
      <td>";
  // invoice :
  $dis_pref = new OBM_DISPLAY("PREFERENCES", $pref_inv_q);
  $dis_pref->display_entity = "invoice";
  $dis_pref->pref_title = $l_invoice_options;
  $dis_pref->pref_dis_help = 0 ;// we display help only once...

  $block .= $dis_pref->display();

  $block .= "</td><td>";

  // deals :
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$pref_deal_q);
  $dis_pref->display_entity = "invoice";
  $dis_pref->pref_title = $l_deal_options;
  $dis_pref->pref_dis_help = 0;

  $block .= $dis_pref->display();

  $block .= "</td></tr></table>";
  $block .= $dis_pref->dis_pref_help();
  
  $block .= "</center>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Parameters :
// ------------
//  $action = action that brought us here
//  $q_invoice : information about the invoice
//  $q_status : all kind of status available for an invoice
//  $q_deal/$q_payment : deals/payments connected to that invoice
//  $options_{deals,payments} : display pref for deals/payments
//
// screen to access if you want to add/remove deals linked to an invoice
///////////////////////////////////////////////////////////////////////////////
function html_invoice_consult($action, $q_invoice, $q_status, $q_deal, $options_deals, $q_payment, $options_payments) {
  global $display, $set_theme;
  // - Labels
  global $l_update,$l_delete,$l_insert, $l_invoice_infos,$l_deals_todel;
  global $l_invoice_del_deal, $l_del_deal_chosen, $l_list_deal,$l_no_deal;
  global $l_list_payment, $l_no_payment, $l_payments_todel, $l_invoice_del_payment, $l_del_payment_chosen;
  global $l_add_deal, $l_del_deal;
  global $l_label,$l_comment,$l_number, $l_date, $l_deal, $l_status;
  global $perms_user,$l_perms_user;
  global $perm, $param_invoice, $menu, $cright_write_admin;

  $i_id = $q_invoice->f('invoice_id');

  $block = dis_invoice_detail ($action,$q_invoice, $q_status);
  
  // bcontins : ?
  // to add/remove deals
  //   $action_add = url_prepare("invoice_index.php?action=add_deal".
  // 			   "&amp;param_invoice=".
  // 			   $q_invoice->f("invoice_id"));
  //   $action_del = url_prepare("invoice_index.php?action=del_deal".
  // 			   "&amp;param_invoice=".
  // 			   $q_invoice->f("invoice_id"));

  // deals data 
  $block .= "<div class=\"detailHead\">$l_list_payment</div>";

  if ($q_deal->nf() == 0) {
    // no deals :
    $block .= "
     <table class=\"detail\">
      <tr>
       <td class=\"detailLabel\">$l_no_payment</td>
      </tr>
     </table>";

    // bcontins : mettre la lien ajouter dans la barre de menu
    //     // form to add some :
    //     $block .= "
    // <table cellspacing=\"0\" border=\"0\">
    //  <tr><td valign=\"top\">
    //   <form method=\"post\" action=\"\">
    //    <input type=\"button\" value=\"$l_add_deal\" ".
    // " onClick=\"this.form.action='".$action_add."' ; this.form.submit ();\">
    //  </td></tr>
    // </table>";

  } else {

    // bcontins
    //     if ($action == "del_deal") {
    //       $url = url_prepare("invoice_index.php?action=del_deal&amp;param_invoice=$i_id");
    //     } else {
    //       $url = url_prepare("invoice_index.php?action=detailconsult&amp;param_invoice=$i_id");
    //     }
    
    $dis_deal = new OBM_DISPLAY("DATA", $options_deals, "invoice");
    $dis_deal->data_set = $q_deal;
    $dis_deal->data_header = "top";
    $dis_deal->data_url = $url;
    $dis_deal->data_order = false;

    if (($action == "del_deal") && ($perm->check_right($menu, $cright_write_admin))) {
      $url_submit = url_prepare("invoice_index.php?action=del_deal_chosen&amp;param_invoice=".$q_invoice->f('invoice_id'));
      $display["msg"] .= display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
      $block .= "<form method=\"post\" name=\"del_deal\" action=\"$url_submit\">";
      $dis_deal->data_cb_text = $l_invoice_del_deal;
      $dis_deal->data_idfield = "deal_id";
      $dis_deal->data_cb_side = "left";
      $dis_deal->data_cb_name = "del_";
    }
    
    $block .= $dis_deal->display("dis_data_invoice");

    if ($perm->check_right($menu, $cright_write_admin)) {
      if ($action == "del_deal" ) {
	// $block .= "<br>
	//  <input align=\"center\" type=\"submit\" value=\"$l_del_deal_chosen\" />
	//  </form>";
      } else {

	// bcontins
	// we display the button to add some deals or remove some...
	// 	$block .= "
	// <table>
	//  <tr><td valign=\"top\">
	//   <form method=\"post\" action=\"\">
	//    <input type=\"button\" value=\"$l_add_deal\"
	//      onClick=\"this.form.action='$action_add' ; this.form.submit ();\">
	//  </td><td valign=\"top\">
	//    <input type=\"button\" value=\"$l_del_deal\"
	//      onClick=\"this.form.action='$action_del' ; this.form.submit ();\">
	//   </form>
	//  </td></tr>
	// </table>";
      }
    }
  }

  //  payments data :
  $block .= "<div class=\"detailHead\">$l_list_payment</div>";

  if ($q_payment->nf() == 0) {
    // no payment
    $block .= "
     <table class=\"detail\">
      <tr>
       <td class=\"detailLabel\">$l_no_payment</td>
      </tr>
     </table>";

  } else {
    $url = url_prepare("invoice_index.php?action=detailconsult&amp;param_invoice=$i_id");

    $dis_payment = new OBM_DISPLAY("DATA",$options_payments, "invoice");
    $dis_payment->data_set = $q_payment;
    $dis_payment->data_header = "top";
    $dis_payment->data_url = $url;
    $dis_payment->data_order = false;
    
    $block .= $dis_payment->display("dis_data_invoice");
  }

  return $block;
}


/////////////////////////////////////////////////////////////////////////////
// displays an invoice details :
/////////////////////////////////////////////////////////////////////////////
function dis_invoice_detail ($p_action, $q_invoice, $q_status){
  global $param_invoice, $display;
  global $l_update, $l_comment,$l_amount_ht, $l_amount_ttc, $l_status;
  global $l_date, $l_label, $l_number, $l_inout, $l_supplier;
  global $l_client, $l_duplicate, $l_invoice, $l_geninvoice;

  $label = $q_invoice->f("invoice_label");
  $num = $q_invoice->f("invoice_number");
  $date = date_format($q_invoice->f("date"));
  $status_id = $q_invoice->f("invoice_status_id");

  while ($q_status->next_record()) {
    if ($q_status->f("invoicestatus_id") == $status_id){
      $status .= $q_status->f("invoicestatus_label");
      break; // as soon as we catch the one we leave
    }
  }

  $inout = ($q_invoice->f("invoice_inout")=='-') ? $l_supplier : $l_client;
  $ht = $q_invoice->f("invoice_amount_ht");
  $ttc = $q_invoice->f("invoice_amount_ttc");
  $comment = nl2br($q_invoice->f("invoice_comment"));

  $display["title"] = "<div class=\"title\">$l_invoice : $label</div>";

  $block .= "
    <div class=\"detailHead\">$l_geninvoice</div>

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


/////////////////////////////////////////////////////////////////////////////
// form to add deals to an invoice
/////////////////////////////////////////////////////////////////////////////
function dis_search_deal_form ($p_action, $q_invoice, $dis_option_deal, $q_deal=0,$p_deal_label="", $p_deal_company="", $p_deal_archive = 0) {
  global $l_search_deal, $l_invoice_deal_label, $l_invoice_infos, $l_find;
  global $l_invoice_deal_archive ;
  global $l_add_deal, $l_add_deal_chosen, $l_no_deal_found, $l_no_deal_display;
  global $display, $action, $perm, $menu, $cright_write_admin, $col_label;
  //$param_invoice;
  global $l_invoice_deal_company;

  // invoice data display
  $block = dis_invoice_detail ($p_action, $q_invoice, run_query_invoicestatus());

  if ($p_deal_archive != 0) {
    $is_checked = " checked ";
  }

  $url = url_prepare("invoice_index.php?action=search_deal".
		     "&amp;param_invoice=".$q_invoice->f("invoice_id"));
  // searching deals form 
  $block .= "<center><form method=\"post\" action=\"$url\">
        <table>
         <tr><td>
          <font color=\"$col_label\"> $l_invoice_deal_label</font></br>
          <input type=\"text\" name=tf_deal_label value=\"$p_deal_label\">
         </td><td>
          <font color=\"$col_label\"> $l_invoice_deal_company</font></br>
          <input type=\"text\" name=tf_deal_company value=\"$p_deal_company\">
         </td><td>
          <font color=\"$col_label\"> $l_invoice_deal_archive </fnot></br>
          <input type=\"checkbox\" name=\"cb_deal_archive\" value=\"1\" $is_checked>
         </td><td>
          <input type=\"submit\" name=\"submit\" value=\"$l_find\">
         </td></tr>
        </table></form></center>";
  // search results, if performed
  if (is_object ($q_deal)) {
    if ($q_deal->nf()>0) {

      $url = url_prepare("invoice_index.php?action=search_deal".
		      "&amp;param_invoice=".$q_invoice->f("invoice_id").
		      "&amp;tf_deal_label=$p_deal_label".
		      "&amp;tf_deal_company=$p_deal_company");
    
    $dis_deal = new OBM_DISPLAY ("DATA", $dis_option_deal, "invoice");
    $dis_deal->data_set = $q_deal;
    $dis_deal->data_header = "top";
    $dis_deal->data_url = $url;
    $dis_deal->data_order = false;

    if (($action == "search_deal") && ($perm->check_right($menu, $cright_write_admin))) {
      $display["msg"] .= display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
      $url = url_prepare("invoice_index.php?action=add_deal_chosen".
			"&amp;param_invoice=".$q_invoice->f("invoice_id").
			"&amp;tf_deal_label=$p_deal_label".
			"&amp;tf_deal_company=$p_deal_company");
    
      $url_form = url_prepare("invoice_index.php?action=add_deal_chosen".
	 "&amp;param_invoice=".$q_invoice->f("invoice_id"));
      $block .= "action = $action
        <form method=\"post\" name=\"add_deal\" action=\"$url_form\">";
    
      $dis_deal->data_cb_text = $l_add_deal;
      $dis_deal->data_idfield = "deal_id";
      $dis_deal->data_cb_side = "left";
      $dis_deal->data_cb_name = "add_";
    }

    $block .= $dis_deal->display("dis_data_invoice");
      
    if (($action == "search_deal") && ($perm->check_right($menu, $cright_write_admin))) {
      $dislay["msg"] .= display_debug_msg("FIXME PERMISSIONS", $cdg_param);
      $block .= "<br>
          <input type=\"submit\" value=\"$l_add_deal_chosen\">
        </form>" ; 
    }

    } else {// no matches found :
      $block .= "<hr>";
      $display["msg"] .= display_ok_msg($l_no_deal_found);
    }      
  } else  { // search not done :
    $block .= "<hr>";
    $display["msg"] .= display_ok_msg($l_no_deal_display);
  }

  return $block;
}


//////////////////////////////////////////////////////////////////////////
// error screen : adding payments to an invoice not connected to a deal
//////////////////////////////////////////////////////////////////////////
function html_error_no_deals_related ($q_invoice) {
  global $display, $l_no_deals_related_error;
  
  $display["msg"] .= display_err_msg  ($l_no_deals_related_error);
}

//////////////////////////////////////////////////////////////////////////
// error screen : deleting the last deal from an invoice 
// while there are still payments...
//////////////////////////////////////////////////////////////////////////
function html_error_last_deal_remove ($q_invoice) {
  global $display, $l_last_deal_remove_error;
  
  $display["msg"] .= display_err_msg  ($l_last_deal_remove_error);
}

</script>
