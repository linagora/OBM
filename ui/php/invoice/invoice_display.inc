<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_display.inc
//     - Desc : Invoice Display file
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// to see tables during dev
$border= ($set_debug>0)? 2 : 0;
$cellspacing = ($set_debug>0)? 5 : 1;

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["invoice_number"] = $l_number;
$fieldnames["invoice_deal"] = $l_deal;
$fieldnames["invoice_company"] = $l_company;
$fieldnames["invoice_label"] = $l_label;
$fieldnames["invoice_amount_HT"] = $l_amount_HT;
$fieldnames["invoice_amount_TTC"] = $l_amount_TTC;
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
  elseif (($fieldname == "invoice_amount_HT") 
	  || ($fieldname == "invoice_amount_TTC")
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
    $res["name"] = $OD->data_set->f("date");
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// invoice search Form
// Parameters :
//  $p_action : current action in the application
//  $q_status : infos concernant les != status
///////////////////////////////////////////////////////////////////////////////
function html_invoice_search_form ($p_action,$q_status, $invoice) {
  global $l_label_start, $l_number, $l_amount_HT, $l_amount_TTC;
  global $l_status, $l_deal, $l_date_apres, $l_date_avant, $l_inout;
  global $l_deal, $l_company, $l_include_archive;
  global $l_find, $l_all_f, $l_both, $l_client, $l_fournisseur;
  global $cellspacing, $border;

  $cellspacing = 10;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = $invoice["deal"];
  $company = $invoice["company"];
  $archive = ($invoice["archive"]) ? "checked" : "" ;

  $url_form = url_prepare("invoice_index.php?action=search");

  $sel_status = "<select name=\"sel_status\">
     <option value=\"_TOUT_\">$l_all_f</option>";
  while ($q_status->next_record()) {
    $id = $q_status->f("invoicestatus_id");
    $label = $q_status->f("invoicestatus_label");
    $sel_status .= "<option value=\"$id\"";
    if ($status == $id) $sel_status .= " selected = \"selected\"";
    $sel_status .= ">$label</option>";
  }
  $sel_status .= "</select>";

  if (($inout == "_tout_") || ($inout=="")) {
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
        onSubmit=\"if (check_form(this) == false) return false ;
        else return true;\" action=\"$url_form\">

  <table class=\"detail\">
  <tr>
    <td>

      <table>
      <tr>
        <td>
        <table>
        <tr>
          <td class=\"textLabelForm\">$l_label_start
            <br />
            <input type=\"text\" name=\"tf_label\" size=\"20\"
              value=\"$label\" />
          </td>
          <td class=\"textLabelForm\">$l_number
            <br />
            <input type=\"text\" name=\"tf_number\" size=\"12\"
              value=\"$number\" />
          </td>
          <td class=\"textLabelForm\">$l_amount_HT
            <br />
            <input type=\"text\" name=\"tf_amount_HT\" size=\"15\"
              value=\"$amount_ht\" />
          </td>
          <td class=\"textLabelForm\">$l_amount_TTC
            <br />
            <input type=\"text\" name=\"tf_amount_TTC\" size=\"15\"
              value=\"$amount_ttc\" />
          </td>
        </tr>
        </table>
        </td>
      </tr>

      <tr>
        <td>
        <table>
        <tr>
          <td class=\"textLabelForm\">$l_status
            <br />
            $sel_status
          </td>
          <td class=\"textLabelForm\">$l_date_apres
            <br />
            <input type=\"text\" name=\"tf_date_after\" size=\"12\"
              value=\"$date_after\" />
          </td>
          <td class=\"textLabelForm\">$l_date_avant
            <br />
            <input type=\"text\" name=\"tf_date_before\" size=\"12\"
              value=\"$date_before\" />
          </td>
          <td class=\"textLabelForm\">$l_include_archive
            <br />
            <input type=\"checkbox\" name=\"cb_archive\" value=\"y\" $archive> 
          </td>
        </tr>
        </table>
        </td>
      </tr>

      <tr>
        <td>
        <table>
        <tr>
          <td class=\"textLabelForm\">$l_inout
            <br />$l_both &nbsp;
            <input type=\"radio\" name=\"rd_inout\" value=\"_TOUT_\" $rb_both />
            $l_client &nbsp;
            <input type=\"radio\" name=\"rd_inout\" value=\"+\" $rb_client />
            $l_fournisseur &nbsp;
            <input type=\"radio\" name=\"rd_inout\" value=\"-\" $rb_four />
          </td>
          <td class=\"textLabelForm\">$l_deal
            <br />
            <input type=\"text\" name=\"tf_deal\" value=\"$deal\" />
          </td>
          <td class=\"textLabelForm\">$l_company
            <br />
            <input type=\"text\" name=\"tf_company\" value=\"$company\" />
          </td>
        </tr>
        </table>
        </td>
      </tr>
      </table>
    </td>

    <td class=\"textLabelForm\"> 
      <input name=\"submit\" type=\"submit\" value=\"$l_find\" />
    </td>
  </tr>
  </table>

  </form>
  <hr />
";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Invoice Form              
// params :
//  $q_invoice : if updating, contains the invoice current info
//  $action : action 
//  $q_status : id et label des != status
///////////////////////////////////////////////////////////////////////////////
// formulaire de création de facture :
function html_invoice_form($q_invoice, $action, $q_status, $p_nb_deals = 0, $p_deal_linked="") {
  // - Themes :
  global $set_theme,$col_label,$col_ok,$ico_company,$ico_contact;
  // -- Labels
  global $l_label, $l_number, $l_comment, $l_deal,
    $l_amount_HT, $l_amount_TTC, $l_status, $l_date, $l_pick_deal, $l_inout, $l_client, $l_fournisseur;
  global $l_insert,$l_update, $l_delete;
  global $default_invoice_numbering;
  global $q_company;
  global $default_tax, $l_tax_rate, $l_compute_tax; 

  global $cellspacing,$border;

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
    $inout = $amount_HT = $amount_TTC = "";
    $number = date ($default_invoice_numbering);
    // when creating from a deal, we must use the same "inout" as
    // the deal...
    if ($p_deal_linked != "") {
      $inout = run_query_deal_get_inout ($p_deal_linked);
      //      $readonly = "readonly"; doesn't work @#$!
    }
  } else {
    $label = $q_invoice->f("invoice_label");
    $number = $q_invoice->f("invoice_number");
    $comment = $q_invoice->f("invoice_comment");
    $invoice_status = $q_invoice->f("invoice_invoicestatus_id");
    $date = $q_invoice->f("invoice_date");
    $inout = $q_invoice->f("invoice_inout");
    $amount_HT = $q_invoice->f("invoice_amount_HT");
    $amount_TTC = $q_invoice->f("invoice_amount_TTC");
    $invoice = $q_invoice->f("invoice_id");
  }

  // if we duplicate we have to modify some fields :
  if ($action == "duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $date = "";
    $label .= "-".date("Y-m-d");
  }

  $sel_status = "<select name=\"sel_status\">"; 
  while ($q_status->next_record()) {
    $s_id = $q_status->f("invoicestatus_id");
    $s_label = $q_status->f("invoicestatus_label");
    $sel_status .= "<option value=\"$s_id\""; 
    if ($s_id == $status) {
      $sel_status .= " selected ";
    }
    $sel_status .= ">$s_label</option>"; 
  }
  $sel_status .= "</select>";

  $block = "
<center>
 <form method=\"post\" action=\"\">
  <table border=\"$border\" cellspacing=\"$cellspacing\">
  <tr>
   <td>
    <table border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">
     <tr>
      <td width=\"20%\"><font color=\"#$col_label\">$l_number</font><br>
       <input name=tf_number type=text size=11 value=\"$number\">
      </td>
      <td width=\"60%\"><font color=\"#$col_label\">$l_label</font><br>
       <input name=tf_label type=text size=40 value=\"$label\">
      </td>
      <td width=\"20%\"><font color=\"#$col_label\">$l_date</font><br>
       <input name=tf_date type=text size=12 value=\"$date\">
      </td>
     </tr>
    </table>
   </td>
  </tr><tr>
   <td>
    <table border=$border cellspacing=$cellspacing width=\"100%\">
     <tr>
      <td><font color=\"#$col_label\">$l_status</font><br>
       $sel_status
      </td>"; 

  // inout : customer or provider
  // not to be display if creating an invoice from a deal
  if ($action == "new" && $p_deal_linked != "") {
    // but we need to keep the inout...
    $block .= "
      <td>
       <input type=\"hidden\" name=\"hd_inout\" value=\"$inout\">
      </td>";
  } else {
    $block .= "
      <td>\n
       <font color=\"#$col_label\">".$l_inout."</font><br>
    $l_client &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"+\"";
    if ($inout == "+") {
      $block .= " checked";
    }
    $block .= ">\n
    $l_fournisseur &nbsp;<input type=\"radio\" name=\"rd_inout\" value=\"-\"";
    if ($inout == '-') {
      $block .= " checked";
    }
    $block .= ">\n
      </td>";
  }
  $block .= "
     </tr>
<!--  amounts and auto-compute stuff -->
     <tr><td colspan=\"2\">
      <table border=\"$border\" cellspacing=\"$cellspacing\" width=\"100%\">
<!--  amount_HT ==> HT : without charges -->
       <tr>
        <td><font color=\"#$col_label\">$l_amount_HT</font><br>
         <input name=\"tf_amount_HT\" type=text size=15 value=\"$amount_HT\">
        </td>
        <td><font color=\"#$col_label\">$l_tax_rate</font></br>
         <input name=\"tf_tax_rate\" type=\"text\" value=\"$default_tax\" size=\"8\" >
        </td>
<!--  amount_TTC ==> TTC : charges included -->
        <td><font color=\"#$col_label\">$l_amount_TTC</font><br> 
         <input name=\"tf_amount_TTC\" type=text size=15 value=\"$amount_TTC\">
        </td>
        <td>
         <input type=\"button\" name=\"b_compute\" value=\"$l_compute_tax\" ".
    "onClick=\"compute_tax(this.form);\">
        </td>
       </tr>
      </table>
     </td></tr>
    </table>
   </td>
  </tr>
  <tr>
   <td>
     <font color=\"#$col_label\">$l_comment</font><br>
     <textarea name=ta_comment rows=6 cols=72>$comment</textarea>
   </td>
  </tr>
  <tr>
   <td>
    <br><center>
     <input type=\"hidden\" name=\"hd_nb_deals\" value=\"$p_nb_deals\">";
  if (($action=="update") || ($action =="detailupdate")) {
    $block .= "<input type=\"hidden\" name=\"hd_rd_inout\" value=\"".$q_invoice->f("invoice_inout")."\">";
  }
  if ($action=="detailupdate") {
    $url_update = url_prepare("invoice_index.php?action=update&amp;".
			      "param_invoice=".$q_invoice->f("invoice_id"));
    $block .= "
     <table cellspacing=\"$cellspacing\" border=\"$border\">
      <tr>
       <td>
        <input type=\"button\" value=\"$l_update\"
        onClick=\"if (check_invoice (this.form)) {
          this.form.action='$url_update';
          this.form.submit ();
          } \">
       </td>";
    // we can delete only if not paid
    $payments_connected = run_query_search_payment_invoice($invoice, -1, $new_order, $order_dir);
    $url_delete = url_prepare ("invoice_index.php?action=delete".
			   "&amp;param_invoice=".$q_invoice->f("invoice_id"));
    if ($payments_connected->nf() == 0) {
      $block .= "
       <td>
         <input type=\"button\" value=\"$l_delete\"
	 onClick=\"if (valider_suppression (this.form)) {
	   this.form.action='$url_delete';
	   this.form.submit ();
	   }\">
       </td>";
    }
    $block .= "
      </tr>
     </table>"; 
  } elseif (($action == "duplicate") || ($action == "new")) {
    if ($action == "new") {
      $url = url_prepare("invoice_index.php?action=insert");
    } elseif ($action == "duplicate") {
      $url = url_prepare("invoice_index.php?action=insert".
			 "&amp;param_invoice=".$q_invoice->f("invoice_id"));
    }
    // inserting or duplicating
    $block .= "
     <input type=\"hidden\" name=\"hd_deal_linked\" value=\"".$p_deal_linked."\">
     <input type=\"button\" value=\"$l_insert\"
       onClick=\"if ($js_fct_invoice(this.form)) {
        this.form.action='$url';
        this.form.submit ();
        }\">";
  }
  $block .= "
    </center>
   </td>
  </tr>
 </table>
 </form>
</center>
";

  return $block;
}



///////////////////////////////////////////////////////////////////////////////
// Display Search result (if it has to be done                               //
// if ($set_display != "no") or if it comes from the submit bouton           //
///////////////////////////////////////////////////////////////////////////////
// Parameters :
// $q_invoice : list of invoices
// $q_display_options : fields to display in the list of invoices
// $nb_invoices : number of invoices
//
function html_invoice_search_list ($q_invoices, $q_display_options, $nb_invoice, $invoice) {
  //-- Themes
  global $col_frs, $col_client;
  //-- Labels
  global $l_found, $l_archive_invoices, $l_archive_update;
  global $l_label, $l_number, $l_amount_HT, $l_amount_TTC, $l_date, $l_status, $l_deal;
  global $display, $auth, $perms_user,$l_perms_user, $perms_admin;

  $label = urlencode($invoice["label"]);
  $number = urlencode($invoice["number"]);
  $amount_HT = urlencode($invoice["HT"]);
  $amount_TTC = urlencode($invoice["TTC"]);
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $deal = urlencode($invoice["deal"]);
  $company = urlencode($invoice["company"]);


  $url = url_prepare("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_HT=$amount_HT". 
		    "&amp;tf_amount_TTC=$amount_TTC".
		    "&amp;sel_status=$status".
		    "&amp;tf_deal=$deal".
		    "&amp;rd_inout=$inout"); 

  $dis_invoice = new OBM_DISPLAY ("DATA", $q_display_options, "invoice");
  $dis_invoice->data_set = $q_invoices;
  $dis_invoice->data_header = "top";
  $dis_invoice->data_url = $url;

  // checkbox to allow archiving..
  if ($auth->auth["perm"] == $perms_admin) {
    $url_archive = url_prepare("invoice_index.php?action=updatearchive");
    $block .= "<form method=\"post\" action=\"$url_archive\">";
    $dis_invoice->data_cb_text = $l_archive_invoices;
    $dis_invoice->data_idfield = "invoice_id";
    $dis_invoice->data_cb_side = "left";
    $dis_invoice->data_cb_name = "archive_";
  }

  $display["msg"] .= display_info_msg("$nb_invoice $l_found");
  $block .= $dis_invoice->display("dis_data_invoice");
  if ($auth->auth["perm"] == $perms_admin) {
    $block .= "<input type=\"submit\" value=\"$l_archive_update\">
      </form>";
  }

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
  global $display, $set_theme,$col_label,$col_ok;
  // - Labels
  global $l_update,$l_delete,$l_insert, $l_invoice_infos,$l_deals_todel;
  global $l_invoice_del_deal, $l_del_deal_chosen, $l_list_deal,$l_no_deal;
  global $l_list_payment, $l_no_payment, $l_payments_todel, $l_invoice_del_payment, $l_del_payment_chosen;
  global $l_add_deal, $l_del_deal;
  global $l_label,$l_comment,$l_number, $l_date, $l_amount_HT, $l_amount_TTC, $l_deal, $l_status;
  global $perms_user,$l_perms_user;
  global $perm, $param_invoice;
  global $border, $cellspacing;

  $block = dis_invoice_detail ($action,$q_invoice, $q_status);
  
  // to add/remove deals
  $action_add = url_prepare("invoice_index.php?action=add_deal".
			   "&amp;param_invoice=".
			   $q_invoice->f("invoice_id"));
  $action_del = url_prepare("invoice_index.php?action=del_deal".
			   "&amp;param_invoice=".
			   $q_invoice->f("invoice_id"));
  // deals data 
  if ($q_deal->nf() == 0) {
    // no deals :
    $display["msg"] .= display_ok_msg ($l_no_deal);
    // form to add some :
    $block .= "
<table cellspacing=\"0\" border=\"0\">
 <tr><td valign=\"top\">
  <form method=\"post\" action=\"\">
   <input type=\"button\" value=\"$l_add_deal\" ".
" onClick=\"this.form.action='".$action_add."' ; this.form.submit ();\">
 </td></tr>
</table>";
  } else {
    $display["msg"] .= display_ok_msg(($action == ("del_deal")? $l_deals_todel : $l_list_deal));

    if ($action == "del_deal") {
      $url = url_prepare("invoice_index.php?action=del_deal&amp;param_invoice=".$q_invoice->f('invoice_id'));
    } else {
      $url = url_prepare("invoice_index.php?action=detailconsult&amp;param_invoice=".$q_invoice->f('invoice_id'));
    }
    
    $dis_deal = new OBM_DISPLAY("DATA", $options_deals, "invoice");
    $dis_deal->data_set = $q_deal;
    $dis_deal->data_header = "top";
    $dis_deal->data_url = $url;
    $dis_deal->data_order = false;

    if (($action == "del_deal") && ($perm->have_perm("admin"))) {
      $url_submit = url_prepare("invoice_index.php?action=del_deal_chosen&amp;param_invoice=".$q_invoice->f('invoice_id'));
      $display["msg"] .= display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
      $block .= "<form method=\"post\" name=\"del_deal\" action=\"$url_submit\">";
      $dis_deal->data_cb_text = $l_invoice_del_deal;
      $dis_deal->data_idfield = "deal_id";
      $dis_deal->data_cb_side = "left";
      $dis_deal->data_cb_name = "del_";
    }
    
    $block .= $dis_deal->display("dis_data_invoice");
    if ($perm->have_perm("admin")) {
      if ($action == "del_deal" ) {
	$display["msg"] .= display_debug_msg("FIXME PERMISSIONS", $cdg_param);
	$block .= "<br>
         <input align=\"center\" type=\"submit\" value=\"$l_del_deal_chosen\" />
        </form>";
      } else {
	// we display the button to add some deals or remove some...
	$block .= "
<br>
<table cellspacing=\"$cellspacing\" border=\"$border\">
 <tr><td valign=\"top\">
  <form method=\"post\" action=\"\">
   <input type=\"button\" value=\"$l_add_deal\"
     onClick=\"this.form.action='$action_add' ; this.form.submit ();\">
 </td><td valign=\"top\">
   <input type=\"button\" value=\"$l_del_deal\"
     onClick=\"this.form.action='$action_del' ; this.form.submit ();\">
  </form>
 </td></tr>
</table>";
      }
    }
  }
  //  payments data :
  if ($q_payment->nf() == 0) {
    // no payment
    $display["msg"] .= display_ok_msg ($l_no_payment);
  } else {
    $display["msg"] .= display_ok_msg ($l_list_payment);

    $url = url_prepare("invoice_index.php?action=detailconsult".
		      "&amp;param_invoice=".$q_invoice->f("invoice_id"));

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
// form to add deals to an invoice
/////////////////////////////////////////////////////////////////////////////
function dis_search_deal_form ($p_action, $q_invoice, $dis_option_deal, $q_deal=0,$p_deal_label="", $p_deal_company="", $p_deal_archive = 0) {
  global $l_search_deal, $l_invoice_deal_label, $l_invoice_infos, $l_find;
  global $l_invoice_deal_archive ;
  global $l_add_deal, $l_add_deal_chosen, $l_no_deal_found, $l_no_deal_display;
  global $cellspacing, $border;
  global $display, $action, $perm, $col_label;
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
        <table border=\"$border\" cellspacing=\"$cellspacing\">
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

    if (($action == "search_deal")&&($perm->have_perm("admin"))) {
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
      
    if (($action=="search_deal")&&($perm->have_perm("admin"))) {
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


/////////////////////////////////////////////////////////////////////////////
// displays an invoice details :
/////////////////////////////////////////////////////////////////////////////
function dis_invoice_detail ($p_action, $q_invoice, $q_status){
  global $param_invoice;
  global $border, $cellspacing;
  global $l_update, $l_comment,$l_amount_HT, $l_amount_TTC, $l_status, $l_date, $l_label, $col_label,$l_number, $l_inout, $l_fournisseur, $l_client, $l_duplicate;

  $date = date_format($q_invoice->f("date"));
  $label = $q_invoice->f("invoice_label");
  $num = $q_invoice->f("invoice_number");

  $block = "<center>
    <table border=$border cellspacing=$cellspacing>
    <tr>
      <td>
      <table border=$border cellspacing=$cellspacing width=\"100%\">
      <tr>
        <td width=\"30%\"><font color=\"#$col_label\">$l_number</font>
          <br>$num
        </td> 
        <td width=\"60%\"><font color=\"#$col_label\">$l_label</font>
          <br>$label
        </td>
        <td><font color=\"#$col_label\">$l_date</font>
          <br>$date
        </td>
      </tr>
      </table>
      </td>
    </tr>
    <tr><td>
   <table border=$border cellspacing=$cellspacing width=\"100%\">
   <tr>
    <td width=\"40%\">
    <font color=\"#$col_label\">$l_status</font><br>";
  while ($q_status->next_record()) {
    if ($q_status->f("invoicestatus_id")==$q_invoice->f("invoice_invoicestatus_id")){
      $block .= $q_status->f("invoicestatus_label");
      break; // as soon as we catch the one we leave
    }
  }
  $block .= "</td>
  <td>
  <font color=\"#$col_label\">$l_inout</font><br>";
  $block .= ($q_invoice->f("invoice_inout")=='-')?$l_fournisseur:$l_client;
  $block .= "</td></tr>
  <tr>
   <td><font color=\"#$col_label\">$l_amount_HT</font><br>".
    $q_invoice->f("invoice_amount_HT")."</td>
    <td><font color=\"#$col_label\">$l_amount_TTC</font><br>".
    $q_invoice->f("invoice_amount_TTC")."</td><br>
  </tr></table>
  </td></tr><tr>
   <td><font color=\"#$col_label\">$l_comment</font><br>".
   nl2br($q_invoice->f("invoice_comment"))."</td>
  </tr>";

  // buttons to update or duplicate an invoice,
  if ($p_action == "detailconsult" || $p_action == "add_deal" || $p_action == "del_deal") {
    // actions of the form :
    $action_up = url_prepare("invoice_index.php?action=detailupdate".
			     "&amp;param_invoice=".
			     $q_invoice->f("invoice_id"));
    $action_dup = url_prepare("invoice_index.php?action=duplicate".
			     "&amp;param_invoice=". 
			     $q_invoice->f("invoice_id"));

    $block .= " 
      <tr><td align=\"center\" valign=\"middle\">
       <table border=\"$border\" cellspacing=\"$cellspacing\">
        <tr><td valign=\"top\">
         <form method=\"post\" action=\"\">
          <input type=\"button\" value=\"$l_duplicate\"
      onClick=\"this.form.action='".$action_dup."'; this.form.submit ();\">
        </td><td align=\"top\">
          <input type=\"button\" value=\"$l_update\"
      onClick=\"this.form.action='".$action_up."'; this.form.submit ();\">
        </td></tr>
         </form>
       </table>
      </td></tr>";
  }
  $block .= "</table><br>";

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

</SCRIPT>
