<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_index.php                                            //
//     - Desc : payment display File                                         //
// 2002-07-16 Pierre Baudracco (from Nicolas Roman)                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// to see tables during dev
$border=($set_debug>0)? 5 : 0;
$cellspacing=($set_debug>0)?5:1;

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_amount"] = $l_amount;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_account_lbl"] = $l_payment_account;
$fieldnames["invoice_label"] = $l_invoice_label;
$fieldnames["invoice_date"] = $l_invoice_date;
$fieldnames["invoice_amount_TTC"] = $l_invoice_TTC;
$fieldnames["invoice_amount_HT"] = $l_invoice_HT;
$fieldnames["invoice_number"] = $l_invoice_number;
$fieldnames["invoice_paid"] = $l_invoice_paid;
$fieldnames["invoice_company"] = $l_invoice_company;
$fieldnames["invoice_status"] = $l_invoice_status;
$fieldnames["invoice_deal"] = $l_deal;
// for the temp. entries :
$fieldnames["entrytemp_date"] = $l_entrytemp_date ;
$fieldnames["entrytemp_type"] = $l_entrytemp_type ;
$fieldnames["entrytemp_amount"] = $l_entrytemp_amount ;
$fieldnames["entrytemp_realdate"] = $l_entrytemp_realdate ;
$fieldnames["entrytemp_label"] = $l_entrytemp_label ;
$fieldnames["entrytemp_comment"] = $l_entrytemp_comment;


///////////////////////////////////////////////////////////////////////////////
// Display Payment specific dataset fields                                   //
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_data_payment(&$OD, $fieldname, $link_ok) {
  global $path, $set_theme, $col_frs, $col_client;

  if ($fieldname == "payment_label") {
    $res["url"] = "$path/payment/payment_index.php?action=detailconsult&amp;param_payment=".$OD->data_set->f("payment_id");
  }

  elseif ($fieldname == "payment_account_lbl") {
    $res["url"] = "$path/account/account_index.php?action=detailconsult&amp;param_account=".$OD->data_set->f("account_id");
  }

  elseif ($fieldname == "pay_inv_label") {
    $res["url"] = "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$OD->data_set->f("invoice_id");
  }

  elseif ($fieldname == "pay_inv_company") {
    $res["url"] = "$path/company/company_index.php?action=detailconsult&amp;param_company=".$OD->data_set->f("company_id");
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
  }
  
  else if ($fieldname == "payment_date") {
    $res["name"] = date_format($OD->data_set->f("date"));
  }
   
  else if ($fieldname == "payment_expect_date") {
    $res["name"] = date_format($OD->data_set->f("expect_date"));
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// creating/updating payment form
// $q_payment if update, data about that payment
// $q_paymentkind data about all existing payment kinds
///////////////////////////////////////////////////////////////////////////////
function html_payment_form($p_action, $q_payment, $q_paymentkind, $q_account) {
  // debug :
  global $border, $cellspacing;
  // themes :
  global $set_theme, $col_label, $coltextem, $col_ok, $c_all;
  // labels :
  global $display;
  global $l_insert, $l_update, $l_delete, $l_bank, $l_add_invoices;
  global $l_label, $l_amount, $l_type, $l_date, $l_expected_date;
  global $l_number, $l_comment, $l_inout, $l_client, $l_fournisseur;
  global $l_account, $l_pick_paymentkind, $l_pick_account;

  // we use those vars to skip tests on the action value
  // for each form field...
  // depending on the action 
  if ($p_action == "new") {
    $number = $label = $amount = $comment = "";
    $date = $expected_date = $inout = $type = "";
  } else {
    // we fill in the form
    $number = $q_payment->f("payment_number");
    $label = $q_payment->f("payment_label");
    $amount = $q_payment->f("payment_amount");
    $comment = $q_payment->f("payment_comment");
    $date = isodate_format($q_payment->f("date"));
    $expected_date = isodate_format($q_payment->f("expect_date"));
    $type = $q_payment->f("payment_paymentkind_id");
    $inout = $q_payment->f("payment_inout");
    $account = $q_payment->f("payment_account_id");
  }

  // if we duplicate, we have to modify some fields :
  if ($p_action =="duplicate") {
    $comment = "duplicata de <$label>\n".$comment;
    $label .= " - ".date ("Y-m-d");
    $number = $date = $date_expected = "";
  }
  
  // if the payment has been banked, or during banking,
  // some fields are no more updatable...
  if (($p_action == "detailupdate") && ($q_payment->f("payment_paid")))
    $readonly = " disabled " ;
  else
    $readonly = ""; // useless, because php would have understood "" by itself,

  // Kind select
  $sel_kind = "
    <select name=\"sel_kind\" $readonly>
     <option value=\"$c_all\">$l_pick_paymentkind</option>\n";

  while ($q_paymentkind->next_record()) {
    $p_id = $q_paymentkind->f("paymentkind_id");
    $p_label = $q_paymentkind->f("paymentkind_longlabel");

    $pselect = ($type == $p_id) ? "selected" : "";
    $sel_kind .= "<option value=\"$p_id\" $pselect>$p_label</option>\n";
  };

  $sel_kind .= "</select>";

  // Account select
  $sel_account ="
    <select name=\"sel_account\" $readonly>
     <option value=\"$c_all\">$l_pick_account</option>\n";

  while ($q_account->next_record()) {
    $a_id = $q_account->f("account_id");
    $a_label = $q_account->f("account_label");

    $aselect = ($a_id == $account) ? "selected" : "";
    $sel_account .= "<option value=\"$a_id\" $aselect>$a_label</option>";
  }
  $sel_account .= "</select>";

  // Type radio
  if ($p_action=="update" || $p_action == "detailupdate" || $p_action == "add_invoices") {
    $pcheck = ($q_payment->f("payment_inout")=='+') ? "checked" : "";
    $mcheck = ($q_payment->f("payment_inout")=='-') ? "checked" : "";
  }

  $rd_type = "
    <input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck $readonly/>$l_client &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck $readonly/>$l_fournisseur &nbsp;
    ";

  // various form actions :
  $url_banking = url_prepare("payment_index.php?action=check_banking");
  $url_creating = url_prepare("payment_index.php?action=insert");
  $url_deleting = url_prepare("payment_index.php?action=delete");
  $url_updating = url_prepare("payment_index.php?action=update");

  // we do different check if we are creating/updating or banking the payment :
  if (($p_action == "new") || ($p_action == "duplicate") ) {
    // creating/duplicating
    $block .=  "
  <form name=\"form_create_payment\" method=\"post\" action=\"$url_creating\" onsubmit=\"if (check_payment (this)) return true; else return false ;\">";
  } elseif ($p_action == "detailupdate") {
      $block .=  "
  <form name=\"form_create_payment\" method=\"post\" action=\"$url_updating\" onsubmit=\"if (check_payment (this)) return true; else return false ;\">";
  } elseif ($p_action == "add_invoices") {
      // banking
      $block .=  "
  <FORM name=\"form_add_invoices\" method=\"post\" action=\"$url_banking\" onsubmit=\"if (check_payment_banking (this)) return true; else return false ;\">";
  } else {
    $display["msg"] .= display_err_msg ("unhandled action value '".$p_action."' in html_payment_form ()");
  }

  $block .= "
    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailForm\">
       <input type=\"text\" name=\"tf_label\" size=40 value=\"$label\">
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailForm\">
       <input type=\"text\" name=\"tf_number\" size=10 value=\"$number\" $readonly>
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_amount</td>
      <td class=\"detailForm\">
       <input type=\"text\" name=\"tf_amount\" size=10 value=\"$amount\" $readonly>
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_expected_date</td>
      <td class=\"detailForm\">
       <input type=\"text\" name=\"tf_expected_date\" size=10 value=\"$expected_date\">
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailForm\">
       <input type=\"text\" name=\"tf_date\" size=10 value=\"$date\" $readonly>
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailForm\">
       $sel_kind
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_account</td>
      <td class=\"detailForm\">
       $sel_account
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_inout</td>
      <td class=\"detailForm\">
       $rd_type
      </td>
     </tr><tr>
      <td class=\"detailLabel\">$l_comment</td>
      <td class=\"detailForm\">
       <textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">$comment</textarea>
      </td>
     <tr>
    </table>

    <div class=\"detailButton\">
     <p class=\"detailButtons\">$dis_button</p>
    </div>
    ";

  // bcontins obsolete partie a nico
  ///////////////////////////////////////////////////////////  
  //   $block .= "
  //   <table border=\"$border\" cellspacing=\"$cellspacing\">
  //    <tr><td>
  //     <font color=\"#$col_label\">$l_label</font><br>
  //     <input type=\"text\" name=\"tf_label\" size=40 value=\"".$label."\">
  //    </td></tr>
  //    <tr><td>
  //     <FONT COLOR=\"#$col_label\">$l_number</FONT><BR>
  //     <INPUT TYPE=\"text\" NAME=\"tf_number\" size=10 value=\"".$number."\" $readonly>
  //    </TD></TR>
  //    <TR><TD>
  //     <FONT COLOR=\"#$col_label\">$l_amount</FONT><BR>
  //     <INPUT TYPE=\"text\" NAME=\"tf_amount\" size=10 value=\"".$amount."\" $readonly>
  //    </TD></TR>
  //    <TR><TD>
  //     <TABLE WIDTH=\"100%\" BORDER=$border CELLSPACING=$cellspacing>
  //      <TR><TD>
  //       <FONT COLOR=\"#$col_label\">$l_expected_date</FONT><BR>
  //       <INPUT TYPE=\"text\" NAME=\"tf_expected_date\" size=10 value=\"".$expected_date."\">
  //      </TD><TD>
  //       <FONT COLOR=\"#$col_label\">$l_date</FONT><BR>
  //       <INPUT TYPE=\"text\" NAME=\"tf_date\" size=10 value=\"$date\" $readonly>
  //      </TD></TR>
  //     </TABLE>
  //    </TD></TR>
  //    <TR>
  //   <td>
  //     <table width=\"100%\" cellspacing=$cellspacing border=$border>
  //      <tr><td>
  //      <font color=\"#$col_label\">$l_type</font><br>
  //      <select name=\"sel_kind\" $readonly>
  //       <option value=\"-1\"";
  //   if ($p_action == "new") {
  //     $block .=  " selected";
  //   }
  //   $block .= ">$l_pick_paymentkind";
  //   while ($q_paymentkind->next_record()) {
  //     $block .=  "\n<OPTION value=\"".$q_paymentkind->f("paymentkind_id")."\"";
  //     if ($type == $q_paymentkind->f("paymentkind_id")){
  //       $block .=  " selected";
  //     }
  //     $block .=  ">".$q_paymentkind->f("paymentkind_longlabel");
  //   }
  
  //   $block .=  "
  //      </select>
  //     </td>    <td>
  //      <font color=\"#$col_label\">$l_account</font><br>
  //      <select name=\"sel_account\" $readonly>
  //       <option value=\"-1\"";
  //   if ($p_action == "new") {
  //     $block .= " selected";
  //   }
  //   $block .= ">$l_pick_account";
  //   while ($q_account->next_record()) {
  //     $block .= "\n<option value=\"".$q_account->f("account_id")."\"";
  //     if ($account == $q_account->f("account_id")) {
  //       $block .= "selected";
  //     }
  //     $block .= ">".$q_account->f("account_label");
  //   }
  //  $block .= "
  //      </select>
  //     </td>
  //    </tr>
  //    </table>";
  //   // radio buttons inout
  //   // not needed when banking...
  //   if ($p_action != "add_invoices") {
  //     $block .= "<tr>
  //       <td><font color=\"#$col_label\">$l_inout</font>
  //         <br>
  //         <input type=\"radio\" name=\"rd_inout\" value=\"+\"";
  //     if ($p_action=="update" || $p_action == "detailupdate" || $p_action == "add_invoices") {
  //       if ($q_payment->f("payment_inout")=='+') {
  // 	$block .= " checked";
  //       }
  //     }
  //     $block .= " $readonly>$l_client &nbsp;
  //       <input type=\"radio\" name=\"rd_inout\" value=\"-\"";
  //     if ($p_action=="update" || $p_action == "detailupdate" || $p_action == "add_invoices"){
  //       if ($q_payment->f("payment_inout")=='-'){
  // 	$block .= " checked";
  //       }
  //     }
  //     $block .= " $readonly>$l_fournisseur &nbsp;
  //       </td></tr>";
  //   }
  //   if (($p_action != "new" && ($p_action != "new_with_invoices"))) {
  //     // we need that, but not shown 
  //     $block .= "
  //   <input type=hidden name=\"hd_param_payment\" ".
  //       " value=\"".$q_payment->f("payment_id")."\">";
  //   }
  //   // comment :
  //  $block .= "
  //    <tr><td>
  //     <font color=\"#$col_label\">$l_comment</font><br>
  //     <textarea name=ta_comment rows=6 cols=72>".$comment."</textarea>
  //    </td></tr>"; 

  //   // buttons, number and function depending on the current action
  //   $block .= "
  //    <tr><td align=\"center\">
  //     <br>";
  //   if ( ($p_action =="new") || ($p_action =="duplicate") ) {
  //     $block .= "
  //       <table border=$border cellspacing=$cellspacing>
  //       <tr><td>
  //        <input type=\"submit\" value=\"$l_insert\">
  //       </td><td>";
  //     $new_action = url_prepare("payment_index.php?action=new_with_invoices");
  //     $block .= "
  //       <input type=\"button\" value=\"$l_add_invoices\" ".
  //       "onClick=\"if (check_payment(this.form)) {this.form.action='$new_action'; this.form.submit();} else { return false; }\">
  //     </td></tr>
  //     </table>
  //   </table>  
  // ";
  //   } elseif ($p_action =="detailupdate") { // two buttons : update/delete
  //     $block .= "
  //      <table cellspacing=\"$cellspacing\" border=\"$border\">
  //       <tr><td>
  //        <input type=\"submit\" value=\"$l_update\">
  //            </form> 
  //       </td><td valign=\"top\"> 
  //        <form method=\"post\" name=\"form_payment_delete\" onSubmit=\"return (valider_suppression());\" action=\"$url_deleting\">
  //         <input type=\"submit\" value=\"$l_delete\" $readonly>
  //         <input type=\"hidden\" name=\"hd_param_payment\" value=\"".$q_payment->f("payment_id")."\">
  //       </td></tr>
  //      </table>
  //   </table>  
  //   </form>";
  //   } elseif ($p_action == "add_invoices") {
  //     // no buttons, we got invoices to check
  //     // below this form (cf. html_payment_affect_invoices ())
  //   }
  //   $block .= "</table>
  // </center>";   
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// payment search form
///////////////////////////////////////////////////////////////////////////////
function html_payment_search_form ($p_action, $q_paymentkind, $q_account, $payment) {
  global $l_all, $c_all;
  global $l_label_start, $l_number, $l_amount, $l_date_apres;
  global $l_date_avant, $l_inout, $l_kind, $l_account;
  global $l_deal, $l_company, $l_include_checked;
  global $l_find, $l_all, $l_both, $l_client, $l_fournisseur;
  global $l_status, $l_paid, $l_unpaid;
  global $cellspacing,$border;

  $label = $payment["label"];
  $number = $payment["number"];
  $amount = $payment["amount"];
  $date_after = $payment["date_after"];
  $date_before = $payment["date_before"];
  $inout = $payment["inout"];
  $kind = $payment["kind"];
  $account = $payment["account"];
  $paid = $payment["paid"];
  $deal = $payment["deal"];
  $company = $payment["company"];
  $ichecked = ($payment["checked"]) ? "checked" : "" ;

  // Inout
  $bcheck = (($inout == "_TOUT_")||($inout=="")) ? "checked" : "";
  $pcheck = ($inout == "+") ? "checked" : "";
  $mcheck = ($inout == "-") ? "checked" : "";

  $rd_inout = "
    <input type=\"radio\" name=\"rd_inout\" value=\"_TOUT_\" $bcheck />$l_both &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck />$l_client &nbsp;
    <input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck />$l_fournisseur &nbsp;
    ";

  // Paid
  $bcheck = (($paid == "_TOUT_")||($paid=="")) ? "checked" : "";
  $ycheck = ($paid == "y") ? "checked" : "";
  $ncheck = ($paid == "n") ? "checked" : "";

  $rd_status = "
    <input type=\"radio\" name=\"rd_paid\" value=\"_TOUT_\" $bcheck />$l_both &nbsp;
    <input type=\"radio\" name=\"rd_paid\" value=\"y\" $ycheck />$l_paid &nbsp;
    <input type=\"radio\" name=\"rd_paid\" value=\"n\" $ncheck />$l_unpaid &nbsp;
    ";

  // Account select
  $sel_account ="
    <select name=\"sel_account\">
     <option value=\"$c_all\">$l_all</option>\n";

  while ($q_account->next_record()) {
    $a_id = $q_account->f("account_id");
    $a_label = $q_account->f("account_label");

    $aselect = ($a_id == $account) ? "selected" : "";
    $sel_account .= "<option value=\"$a_id\" $aselect>$a_label</option>";
  }
  $sel_account .= "</select>";
  
  // Kind select
  $sel_kind ="
    <select name=\"sel_kind\">
     <option value=$c_all>$l_all</option>";

  while ($q_paymentkind->next_record()) {
    $p_id = $q_paymentkind->f("paymentkind_id");
    $p_label = $q_paymentkind->f("paymentkind_longlabel");

    $pselect = ($p_id == $kind) ? "selected" : "";
    $sel_kind .= "<option value=\"$p_id\" $aselect>$p_label</option>";
  }
  $sel_kind .= "</select>";

  $block = "
    <form method=\"get\" name=\"form_search_payment\" action=\"payment_index.php?action=search\">
     <div class=\"search\">

      <div class=\"searchLabel\">$l_label_start<br />
       <input type=text name=tf_label size=20 value=\"$label\">
      </div>

      <div class=\"searchLabel\">$l_number<br />
       <input type=text name=tf_number size=12 value=\"$number\">
      </div>

      <div class=\"searchLabel\">$l_amount<br />
       <input type=text name=tf_amount size=12 value=\"$amount\">
      </div>

      <div class=\"searchLabel\">$l_date_apres<br />
       <input type=text name=tf_date_after size=12 value=\"$date_after\">
      </div>

      <div class=\"searchLabel\">$l_date_avant<br />
       <input type=text name=tf_date_before size=12 value=\"$date_before\">
      </div>

      <div class=\"searchLabel\">$l_inout<br />
       $rd_inout
      </div>

      <div class=\"searchLabel\">$l_status<br />
       $rd_status
      </div>

      <div class=\"searchLabel\">$l_deal<br />
       <input type=\"text\" name=\"tf_deal\" value=\"$deal\" />
      </div>

      <div class=\"searchLabel\">$l_company<br />
       <input type=\"text\" name=\"tf_company\" value=\"$company\" />
      </div>

      <div class=\"searchLabel\"$l_account><br />
       $sel_account
      </div>

      <div class=\"searchLabel\">$l_kind<br />
       $sel_kind
      </div>

      <div class=\"searchLabel\">$l_include_checked<br />
       <input type=\"checkbox\" name=\"cb_checked\" value=\"y\" $ichecked>
      </div>

      <div class=\"searchLabel\"><br />
       <input type=hidden name=action value=\"search\">
       <input type=submit value=$l_find>  
      </div>

      <div class=\"searchEnd\"></div>
     </div>
    </form>
    ";

  // bcontins : ancienne version à Nico
  /////////////////////////////////////////////////////////////////
  //   $block = "
  //     <form method=\"get\" name=\"form_search_payment\" onSubmit=\"if (check_search_form(this) == false) return false ; else return true;\" action=\"".url_prepare("payment_index.php?action=search")."\">
  //      <table border=$border cellspacing=$cellspacing>
  //       <tr><td>
  //       <table border=$border cellspacing=$cellspacing width=\"100%\">
  //        <tr><td>
  // 	<table border=$border cellspacing=$cellspacing width=\"100%\">
  //          <tr>
  //           <td>
  //            <font size=-2>$l_label_start</font><br>
  //            <input type=text name=tf_label size=20 value=\"$label\">
  //           </td>
  //           <td>
  //            <font size=-2>$l_number</font><br>
  //            <input type=text name=tf_number size=12 value=\"$number\">
  //           </td>
  //           <td>
  //            <font size=-2>$l_amount</font><br>
  //            <input type=text name=tf_amount size=12 value=\"$amount\">
  //           </td>
  //          </tr>
  //         </table>
  //        </td></tr>
  //        <tr><td>
  //         <table cellspacing=$cellspacing border=$border width=\"100%\">
  //          <tr>
  //          <td>
  //           <font size=-2>$l_date_apres</font><br>
  //          </td>
  //          <td>
  //           <font size=-2>$l_date_avant</font><br>
  //          </td>
  //         <td>
  //           <font size=-2>$l_inout</font><br>
  //           <input type=\"radio\" name=\"rd_inout\" value=\"_TOUT_\"";
  //   if (($inout == "_TOUT_")||($inout=="")){
  //     $block .= "checked";
  //   }
  //   $block .= ">$l_both &nbsp;
  //     <input type=\"radio\" name=\"rd_inout\" value=\"+\"";
  //   if ($inout == "+"){
  //     $block .= "checked";
  //   }
  //   $block .= ">$l_client &nbsp;
  //           <input type=\"radio\" name=\"rd_inout\" value=\"-\"";
  //   if ($inout == "-"){
  //     $block .= "checked";
  //   }
  //   $block .= ">$l_fournisseur &nbsp;
  //         </td></tr>";
  //   // Paid or unpaid payments ?
  //   $block .= "
  //         <tr><td>
  //           <font size=-2>$l_status</font>
  //           <br>
  //           <input type=\"radio\" name=\"rd_paid\" value=\"_TOUT_\"";
  //   if (($paid == "_TOUT_")||($paid=="")){
  //     $block .= "checked"; 
  //   }
  //   $block .= ">$l_both &nbsp;
  //           <input type=\"radio\" name=\"rd_paid\" value=\"y\"";
  //   if ($paid == "y"){
  //     $block .= "checked";
  //   }
  //   $block .= ">$l_paid &nbsp;
  //           <input type=\"radio\" name=\"rd_paid\" value=\"n\"";
  //   if ($paid == "n"){
  //    $block .= "checked";
  //   }
  //   $block .=">$l_unpaid &nbsp;";
  //   // deal :
  //   $block .="
  //         </td><td>
  //          <font size=\"-2\">$l_deal</font><br>
  //          <input type=\"text\" name=\"tf_deal\" value=\"$deal\">
  // 	 </td>
  // 	 <td>
  //          <font size=\"-2\">$l_company</font><br>
  //          <input type=\"text\" name=\"tf_company\" value=\"$company\">
  //         </td></tr>
  //        </table>
  //         </td></tr>
  //         <tr><td>
  //          <table width=\"100%\" border=$border cellspacing=$cellspacing>
  //           <tr><td>
  //            <font size=-2>$l_account</font><br>
  //            <select name=\"sel_account\">
  //            <option value=\"-1\">$l_all";
  //   while ($q_account->next_record()) {
  //    $block .= "\n\t<option value=\"".$q_account->f("account_id")."\"";
  //     if ($q_account->f("account_id") == $account) {
  //       $block .= " selected ";
  //     }
  //     $block .=">".$q_account->f("account_label");
  //   }
  //  $block .= "
  //            </select>";
  //   // kind of the payment (paymentkind db table)
  //     $block .="
  //           </td><td>
  //            <font size=-2>$l_kind</font><br>
  //            <select name=\"sel_kind\">
  //             <option value=-1>$l_all";
  //   while ($q_paymentkind->next_record()) {
  //     $block .="
  //             <option value=\"".$q_paymentkind->f("paymentkind_id")."\"";
  //     if ($q_paymentkind->f("paymentkind_id") == $kind) {
  //       $block .=" selected ";
  //     }
  //     $block .=">".$q_paymentkind->f("paymentkind_longlabel");
  //   }
  //   $block .=" 
  //            </select> 
  // 	   </td>
  // 	   <td>
  //            <font size=\"-2\">".$l_include_checked."</font><br>
  //            <input type=\"checkbox\" name=\"cb_checked\" value=\"y\" $checked>
  //           </td></tr>
  //          </table>  
  //         </td></tr>
  //        </table>
  //       </td><td valign=center>
  //         <input type=hidden name=action value=\"search\">
  //         <input type=submit value=".$l_find.">
  //       </td></tr>
  //      </table>
  //     </form><hr>
  //    </center>";

   return $block;
}// html_payment_search_form


///////////////////////////////////////////////////////////////////////////////
// matching payment found display
///////////////////////////////////////////////////////////////////////////////
function html_payment_search_list ($payment_q, $pref_q, $nb_payment, $payment){
  global $col_frs, $col_client;
  global $l_found;
  global $display;
  // get payment data :
  $label = urlencode($payment["label"]);
  $number = urlencode($payment["number"]);
  $amount = urlencode($payment["amount"]);
  $date_after = $payment["date_after"];
  $date_before = $payment["date_before"];
  $inout = $payment["inout"];
  $kind = $payment["kind"];
  $account = $payment["account"];

  $url = url_prepare("payment_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount=$amount".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_date_after=$date_after".
		    "&amp;rd_inout=$inout");

  $dis_payment = new OBM_DISPLAY("DATA", $pref_q, "payment");
  $dis_payment->data_set = $payment_q;
  $dis_payment->data_header = "top";
  $dis_payment->data_url = $url;

  $display["msg"] .= display_info_msg("$nb_payment $l_found");
  $block = $dis_payment->display ("dis_data_payment");
  return $block;
}

// bcontins !! faudra refaire la requete qui renvoit les details pour récuperer
// bcontins !! les label des types de paiement et autre parce que c'est crade
// bcontins !!
// bcontins !! while ($q_paymentkind->next_record()) {
// bcontins !!   if ($q_paymentkind->f("paymentkind_id") == $q_payment->f("payment_paymentkind_id")) {
// bcontins !!	   $block .= $q_paymentkind->f("paymentkind_longlabel");
// bcontins !!	   break; // dès qu'on a le bon, on sort
// bcontins !! } }

///////////////////////////////////////////////////////////////////////////////
// display of payment details 
///////////////////////////////////////////////////////////////////////////////
function html_payment_consult ($action, $q_payment, $q_paymentkind,$q_account, $q_invoices, $invoices_options) {
  // -- Themes
  global $set_theme,$col_label,$col_ok, $col_client, $col_frs;
  // - Labels
  global $l_update,$l_delete,$l_insert,$l_bank,$l_payment_infos,$l_duplicate;
  global $l_label,$l_comment,$l_number, $l_date, $l_amount, $l_expected_date;
  global $l_account, $l_kind, $l_type, $l_payment;
  global $l_no_invoices_connected, $l_invoices_connected, $l_break_asso;
  global $param_payment;
  global $border, $cellspacing;

  // amount display color
  if ($q_payment->f("payment_inout") == '+') {
    $col_montant = $col_client;
  }elseif($q_payment->f("payment_inout") == '-') {
    $col_montant = $col_frs;
  }

  // various actions performed by the form :
  $url_update = url_prepare("payment_index.php?action=detailupdate&amp;param_payment=".$q_payment->f("payment_id"));
  $url_duplicate = url_prepare("payment_index.php?action=duplicate&amp;param_payment=".$q_payment->f("payment_id"));
  $url_bank = url_prepare("payment_index.php?action=search_invoice&amp;param_payment=".$q_payment->f("payment_id"));
  
  $label = $q_payment->f("payment_label");
  $number = $q_payment->f("payment_number");
  $amount = $q_payment->f("payment_amount");
  $expect_date = date_format($q_payment->f("expect_date"));
  $date = ($q_payment->f("date") == "") ? "&nbsp;" : date_format($q_payment->f("date"));
  $comment = $q_payment->f("payment_comment");

  // Kind (bcontins a changer avec la requete)
  if ($q_payment->f("payment_paymentkind_id") == "") {
    $kind = "&nbsp;";
  } else {
    while ($q_paymentkind->next_record()) {
      if ($q_paymentkind->f("paymentkind_id") == $q_payment->f("payment_paymentkind_id")) {
	$kind = $q_paymentkind->f("paymentkind_longlabel");
	break; // dès qu'on a le bon, on sort
      }
    }
  }

  // Account
  if ($q_payment->f("payment_account_id") == "") {
    $account =  "&nbsp;";
  } else {
    while ($q_account->next_record()) {
      if ($q_account->f("account_id") == $q_payment->f("payment_account_id")) {
	$account =  $q_account->f("account_label");
	break; // dès qu'on a le bon, on sort
      }
    }
  }  

  $display["title"] = "<div class=\"title\">$l_payment : $label</div>";

  $block = "
    <div class=\"detailHead\">$title</div>

    <table class=\"detail\">
     <tr>
      <td class=\"detailLabel\">$l_label</td>
      <td class=\"detailText\">$label</td>
     </tr>
     <tr>
      $dis_company
      <td class=\"detailLabel\">$l_number</td>
      <td class=\"detailText\">$number</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_amount</td>
      <td class=\"detailText\">$amount</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_expected_date</td>
      <td class=\"detailText\">$expect_date</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_date</td>
      <td class=\"detailText\">$date</td>
     </tr>
     <tr>
      <td class=\"detailLabel\">$l_type</td>
      <td class=\"detailText\">$kind</td>
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
 	
    </div>
    ";

  // bcontins version obsolete de nico
  ////////////////////////////////////////////////////////////////
  //   $block =  "
  //     <table border=$border cellspacing=$cellspacing>
  //      <tr>
  //      <td>
  //       <font color=\"#$col_label\">$l_label</font><br>".$q_payment->f("payment_label")."
  //      </td></tr>
  //      <tr><td>
  //       <font color=\"#$col_label\">$l_number</font><br>".$q_payment->f("payment_number")."
  //      </td></tr>
  //      <tr><td>
  //        <font color=\"#$col_label\">$l_amount</font><br>
  //        <font color=\"#$col_montant\">".$q_payment->f("payment_amount")."</font>
  //      </td></tr>
  //      <tr><td>
  //       <table width=\"100%\" border=$border cellspacing=$cellspacing>
  //        <tr><td>
  //         <font color=\"#$col_label\">$l_expected_date</font><br>".date_format($q_payment->f("expect_date"))."
  //        </td><td>
  //         <font color=\"#$col_label\">$l_date</font><br>".
  //     ($q_payment->f("date") == "") ? "&nbsp;" : date_format($q_payment->f("date"))   ."
  //        </td></tr>
  //       </table>
  //      </td></tr>
  //      <tr><td>
  //       <font color=\"#$col_label\">$l_type</font><br>\n";
  //   if ($q_payment->f("payment_paymentkind_id") == "") {
  //     $block .= "&nbsp;";
  //   } else {
  //     while ($q_paymentkind->next_record()) {
  //       if ($q_paymentkind->f("paymentkind_id") == $q_payment->f("payment_paymentkind_id")) {
  // 	$block .= $q_paymentkind->f("paymentkind_longlabel");
  // 	break; // dès qu'on a le bon, on sort
  //       }
  //     }
  //   }
  //   $block .= "
  //      </td></tr>
  //      <tr><td>
  //       <font color=\"#$col_label\">$l_account</font><br>\n";
  //   // no account yet 
  //   if ($q_payment->f("payment_account_id") == "") {
  //     $block .=  "&nbsp;";
  //   } else {
  //     while ($q_account->next_record()) {
  //       if ($q_account->f("account_id") == $q_payment->f("payment_account_id")) {
  // 	$block .=  $q_account->f("account_label");
  // 	break; // dès qu'on a le bon, on sort
  //       }
  //     }
  //   }  
  //   $block .=  "
  //      </td></tr>
  //      <tr><td>
  //       <font color=\"#$col_label\">$l_comment</font><br>\n".
  //     $q_payment->f("payment_comment")."
  //      </td></tr>"; 
  //   // boutons update/bank...
  //   if ($action == "detailconsult") {
  //     // if $action == "search_invoice", we don't need those buttons...
  //     $block .=  "
  //       <tr><td>
  //        <table border=$border cellspacing=$cellspacing width=\"100%\">
  //         <tr>
  //        <form method=post name=form_payment_update action=\"\">
  //          <td align=\"center\">";
  //     // we do not need that button if the payment is checked :
  //     if ( ! $q_payment->f("payment_checked")) {
  //       $block .=  "
  //           <input type=button value=\"$l_update\" onClick=\"form_payment_update.action='$url_update'; form_payment_update.submit();\">";
  //     }
  //     // this button is used to handle periodic payments.
  //     // when clicked, it allows the user to duplicate the current
  //     // payment, so he can keep some fields and only update those who need
  //     // to change :
  //     $block .=  "
  //           <input type=\"button\" value=\"$l_duplicate\" onClick=\"form_payment_update.action='$url_duplicate'; form_payment_update.submit();\">";
  //     // we display the "bank" button only if payment has not been banked yet
  //     if ($q_payment->f("payment_paid") == "n"){
  //       // if the user clicks on bank, we jump to the invoice search page
  //       $block .=  "
  //           <input type=button value=\"$l_bank\" onClick=\"form_payment_update.action='$url_bank';form_payment_update.submit();\">"; 
  //     } 
  //     // we hide payment_inout because we need to have it,
  //   // but not to show it :
  //     $block .=  "
  //   <input type=hidden name=\"hd_inout\" ".
  //       " value=\"".$q_payment->f("payment_inout")."\">
  //         </td>
  //        </form>
  //      </tr></table>
  //      </td></tr>
  // ";
  //   }
  //   $block .=  "
  //      </td></tr>
  //     </table><br><br>";
  //   // we display the invoice connected to that payment (if not banking)
  //   if ($action == "detailconsult" || $action == "search_invoice") {
  //     if ($q_invoices->num_rows() == 0){
  //       $display["msg"] .= display_ok_msg("$l_no_invoices_connected<br>");
  //     } else {

  //       $display["msg"] .= display_ok_msg ($l_invoices_connected);
      
  //       $url = url_prepare("payment_index.php?action=detailconsult".
  // 			"&amp;param_payment=".$q_payment->f("payment_id"));
      
  //       $dis_invoice = new OBM_DISPLAY("DATA", $invoices_options, "payment");
  //       $dis_invoice->data_set = $q_invoices;
  //       $dis_invoice->data_header = "top";
  //       $dis_invoice->data_url = $url;
  //       $dis_invoice->data_order = false;

  //       $dis_invoice->display ();
  //       //and the button to break invoices-payments associations :

  //       if ($action=="detailconsult" && $q_payment->f("payment_checked")=="n") {
  // 	$action_break = url_prepare("payment_index.php?action=break_asso&amp;param_payment=".$q_payment->f("payment_id"));
  // 	$block .=  "
  // <center>
  //  <form name=\"break_asso\" method=\"post\" action=\"$action_break\">
  //   <input type=\"submit\" value=\"$l_break_asso\">
  //  </form>
  // </center>";    
  //       }
  //     }
  //   }
  //   $block .=  "<br><br> 
  //  </center>";

  return $block;
}

///////////////////////////////////////////////////////////////////////////
// html_chose_invoice_form ()
// used to search invoices that will be connected to the payment.
// params :
//   - $payment : payment to connect invoices to
//   - $dis_options_invoice : display options (invoice only)
//   - $q_invoice : invoices found in db using label as a search criteria
///////////////////////////////////////////////////////////////////////////
function html_chose_invoices_form ($p_action, $payment, $dis_options_invoice, $q_invoice) {
  global $col_label, $l_invoice_label, $l_search_invoice;
  global $cellspacing, $border;
  // display stuff 
  global $l_no_invoices, $l_invoices_found;
  //  global $col_frs, $col_client;
  // Labels
  global $l_found, $l_select_invoice, $l_affect_invoices;
  global $l_label, $l_number, $l_amount, $l_kind, $l_date, $l_expected_date;
  global $l_invoice_label, $l_invoice_date, $l_invoice_TTC,$l_invoice_status;
  global $l_invoice_company;
  global $cdg_param;
  
  if ($p_action == "search_invoice") {
    $url = url_prepare("payment_index.php?action=search_invoice&amp;param_payment=".$payment["payment"]);
  } elseif ($p_action == "new_with_invoices") {
    $url = url_prepare("payment_index.php?action=search_invoice_new&amp;param_payment=".$payment["payment"]);
  }
  
  // invoice search form  :
   $block .= "
<center>
 <form name=\"invoice_search\" method=\"post\" action=\"$url\">
  <table cellspacing=$cellspacing border=$border>
   <tr><td>
    <font color=\"#$col_label\">$l_invoice_label</font><br>
    <input type=\"text\" name=\"tf_invoice_label\" size=40 value=\"".$payment["invoice_label"]."\">
   </td><td>
    <font color=\"#$col_label\">$l_invoice_company</font><br>
    <input type=\"text\" name=\"tf_invoice_company\" size=40 value=\"".$payment["invoice_company"]."\">
   </td>
   <td>
    <input type=submit value=\"$l_search_invoice\">
   </td</tr>
  </table>
  <input type=hidden name=hd_inout value=\"".$payment["inout"]."\">
 </form>
</center>";
 // we display invoice data only if search has been done
 if ((!is_object ($q_invoice)) || ($q_invoice->num_rows() == 0)) {
   // pas de résultats :
   $display["msg"] .= display_ok_msg ($l_no_invoices);
 } else {

   $url = url_prepare("payment_index.php?action=search_invoice".
		     "&amp;param_payment=".$payment["payment"].
		     "&amp;tf_invoice_label=$tf_invoice_label");
   
   $dis_invoice = new OBM_DISPLAY("DATA", $dis_options_invoice, "payment");
   $dis_invoice->data_set = $q_invoice;
   $dis_invoice->data_header = "top";
   $dis_invoice->data_url = $url;
   $dis_invoice->data_order = false;

   // checkbox to select invoices
   $dis_invoice->data_cb_text = $l_select_invoice;
   $dis_invoice->data_idfield = "invoice_id";
   $dis_invoice->data_cb_side = "left";
   $dis_invoice->data_cb_name = "use_";

   if ($p_action == "search_invoice") {
     $url = url_prepare("payment_index.php?action=add_invoices".
		       "&amp;param_payment=".$payment["payment"]);
   }
   elseif (($p_action == "new_with_invoices") 
	   || ($p_action == "search_invoice_new")) {
     $url = url_prepare("payment_index.php?action=insert_with_invoices".
		       "&amp;param_payment=".$payment["payment"]);
   }
   if (true) {
     $display["msg"] .= display_debug_msg("FIXME : permissions", $cdg_param); 
     $block .= "<form method=post action=\"$url\">";
   }
   $block .= $dis_invoice->display ();

   if (true) {
     $display["msg"] .= display_debug_msg("FIXME : permissions", $cdg_param); 
     $block .=  "
    <center>
     <input type=submit value=\"$l_affect_invoices\">
    </center>
   </form>
 ";   
     
   } 
 }
 return $block;
}


//////////////////////////////////////////////////////////////////////////
// on that page, the user can split the amount of the payment
// between all selected invoices
// - $payment : hash_table describing the payment (key payment_id)
// - $tab_invoices_selected : array of invoice_id selected by the user
//   on the previous screen
//////////////////////////////////////////////////////////////////////////
function html_payment_affect_invoices ($payment, $tab_invoices_selected){
  global $l_invoice_label, $l_invoice_date, $l_invoice_TTC;
  global $l_invoice_number, $l_invoice_not_paid_yet;
  global $l_invoice_still_not_paid,$l_invoice_share;
  global $l_already_used_share;
  global $l_affect_invoices;
  global $border, $cellspacing;
  global $l_refresh;
  $cellspacing=0;

  // size (in chars) of text fields :
  $size = 8;

  // first form : the user can update the payment
  // (type in number, amount kind, account...)
  $q_payment = run_query_detail ($payment["payment"]);
  html_payment_form ("add_invoices", $q_payment,
		     run_query_paymentkind(), run_query_account());
  // ATTENTION : peut-être un problème de formulaire dans html_payment_form()
  // réglé à priori..
  
  // repartition part of the form :
  // for each invoice, we give a textfield in which the user can
  // input the part of the payment amount to be affected
  // ALL CHECKS WILL BE PERFORMED ON THE NEXT PAGE...
  $block .=  "  
 <br><br>
  <table border=\"$border\" cellspacing=\"2\" cellpadding=\"3\">";
  // let's put the headers :
  $block .=  "
   <tr><th>$l_invoice_number</th><th>$l_invoice_date</th>".
    "<th>$l_invoice_label</th><th>$l_invoice_TTC</th>".
    "<th>$l_invoice_not_paid_yet</th><th>$l_invoice_still_not_paid</th>".
    "<th>$l_invoice_share</th></tr>";
  // we need this $i later on...
  $i = 0;
  // we need to compute for each invoice the amount remainig to pay
  foreach ($tab_invoices_selected as $invoice_id) {
    $q_invoice = run_query_invoice_detail ($invoice_id);
    $q_invoice->next_record ();
    // we put all that things in the form :
    $block .=  "\n<tr>\n".
      // we keep hidden the invoice_id :
      "<input type=hidden name=\"hd_invoice_id_$i\" value=\"".
      $q_invoice->f("invoice_id")."\">\n".
      "<td>".$q_invoice->f("invoice_number")."</td>\n".
      "<td>".$q_invoice->f("invoice_date")."</td>\n".
      "<td>".$q_invoice->f("invoice_label")."</td>\n".
      "<td>".$q_invoice->f("invoice_amount_TTC")."</td>\n".
      "<td><input type=text readonly size=\"$size\" name=\"tf_not_paid_yet_$i\"".
      " value=\"".$q_invoice->f("invoice_not_paid_yet")."\"></td>\n".
      "<td><input type=text readonly size=\"$size\" name=\"tf_still_not_paid_$i\"".
      " value=\"".$q_invoice->f("invoice_not_paid_yet")."\"></td>\n".
      "<td><input type=text size=\"$size\" name=\"tf_shares_$i\" value=\"0\"".
      // each time this value is updated by the user, we call a javascript to
      // update in turn all other computed fields
      "onChange=\"update_values (tf_shares_$i, hd_old_shares_$i, tf_not_paid_yet_$i,".
      " tf_still_not_paid_$i, tf_instant_value)\"></td>\n".
      // we also need to keep the previous value, see
      // function update_values () in payment_js.inc for details
      "<input type=hidden name=hd_old_shares_$i value=\"0\">".
      "</tr>\n";
    $i++;
  }
  // we give the user the instant total of money already used :
  $block .=  "\n<tr>\n".
    "<td colspan=6 align=right>$l_already_used_share</td>\n".
    "<td><input type=text readonly size=\"$size\" name=\"tf_instant_value\"".
    "value=\"0\"></td>\n".
    "</tr>\n
    <input type=hidden name=hd_nb_invoices value=\"".
    count($tab_invoices_selected)."\">\n
   <tr><td colspan=6 align=center valign=bottom>
    <input type=submit value=\"$l_affect_invoices\">
   </td><td align=\"center\">
    <input type=\"button\" value=\"$l_refresh\">
   </td></tr>\n
  </table>
 </form>
</center>
<br><br>";
return $block;
}

//////////////////////////////////////////////////////////////////////////
// warns the user about errors that happened during a banking
// $p_error : identifies the error 
// $p_payment : payment banked
// $p_invoices : invoices involved in the banking
//////////////////////////////////////////////////////////////////////////
function html_banking_problem ($p_error, $p_payment, $p_invoices) {
  global $l_too_much, $l_not_enough, $l_trouble_invoice;
  global $l_bank_again, $l_bank_anyway;
  global $display;

  $display["msg"] .= display_err_msg ($p_error);
  // where the user goes if he clicks on "carry on"
  $url_go = url_prepare("payment_index.php?action=check_banking");
  // and if he clicks on "do it again"
  $url_again = url_prepare("payment_index.php?action=search_invoice");
  
  if (($p_error == $l_not_enough) || ($p_error == $l_too_much)){
    // banking has to be done again :
    $block .=  "do it again
<center>
 <form name=\"form_do_it_again\" method=post action=\"$url_again\">$url
  <input type=hidden name=\"hd_param_payment\" ".
      " value=\"".$p_payment["payment"]."\">
  <input type=hidden name=\"hd_inout\" ".
      " value=\"".$p_payment["inout"]."\">
  <input type=submit value=\"$l_bank_again\">
 </form>
</center>";
  } elseif ($p_error == $l_trouble_invoice) {
    // this is maybe a user mistake, maybe not. Better ask him..
    // we need to "remember" all payment & invoices data...
    $block .=  "
<center>
 <form name=\"form_are_you_sure\" method=post action=\"$url_go\">
  <input type=hidden name=check_done value=1>
  <input type=hidden name=hd_param_payment value=\"".$p_payment["payment"]."\">
  <input type=hidden name=hd_amount value=\"".$p_payment["amount"]."\">
  <input type=hidden name=hd_used_amount value=\"".$p_payment["used_amount"]."\">
  <input type=hidden name=hd_account value=\"".$p_payment["account"]."\">
  <input type=hidden name=hd_number value=\"".$p_payment["number"]."\">
  <input type=hidden name=hd_kind value=\"".$p_payment["kind"]."\">
  <input type=hidden name=hd_nb_invoices value=\"".$p_payment["nb_invoices"]."\">
  <input type=hidden name=hd_inout value=\"".$p_payment["inout"]."\">
";
    
    // then for each invoice :
    // id, amount paid by this payment, amount left to be paid...
    $i=0;
    foreach ($p_invoices as $tmp_invoice) {
      $block .=  "
   <input type=hidden name=hd_invoice_id_".$i." value=\"".$tmp_invoice["invoice_id"]."\">
   <input type=hidden name=hd_still_not_paid_".$i." value=\"".$tmp_invoice["still_not_paid"]."\">
   <input type=hidden name=hd_shares_".$i." value=\"".$tmp_invoice["shares"]."\">";
      $i++;
    }
    // here we need 2 submit buttons : 
    // one to do it again, one to carry on anyway
    // this means 2 forms or javascript. Say 2 forms is ok...
    $block .=  "
  <input type=button value=\"$l_bank_anyway\" onClick=\"form_are_you_sure.action='$url_go';form_are_you_sure.submit();\">
  <input type=button value=\"$l_bank_again\" onClick=\"form_are_you_sure.action='$url_again';form_are_you_sure.submit();\">
 </form>
</center>";
  } else {
    // unknow (impossible ?) error
    $block .=  "How did you get there ?";
  }
  return $block;
}

//////////////////////////////////////////////////////////////////////////
// checking associations between a payment and invoices connected
// 
//////////////////////////////////////////////////////////////////////////
function html_breaking_associations ($p_action,$q_payment, $q_connected_invoices, $invoices_display) {
  global $l_break_asso_strong_confirm, $l_break_asso_go;
  global $l_break_asso_soft_confirm, $l_break_asso_easy_confirm;
  global $l_payment_break_asso;
  global $display;
  // we display payment infos :
  html_payment_consult ($p_action, $q_payment, run_query_paymentkind(),
  run_query_account(), $q_connected_invoices, $invoices_display);

  // 3 checks to make :
  // - payment is paid and connected to more than 1 invoice
  // - payment is paid and connected to 1 invoice
  // - payment not paid...
  if ( ($q_payment->f("payment_paid") == "y") 
       && ($q_connected_invoices->nf() > 1) ) {
    // no choice left for the user, if he carries on,
    // we "un-pay" that payment and all invoices connected to it
    // (because payments must be paid in one time)
    $situation = "strong";
    $msg = $l_break_asso_strong_confirm;
  } elseif ($q_payment->f("payment_paid") == "y") {
    // only one invoice connected to the payment. 
    $situation = "soft";
    $msg = $l_break_asso_soft_confirm;
  } else {
    // we don't care how many invoices are connected here
    $situation = "easy";
    $msg = $l_break_asso_easy_confirm;
  }
  // where the form leads :
  $url_break = url_prepare("payment_index.php?action=do_break_asso&amp;".
			   "param_payment=".$q_payment->f("payment_id").
			   "&amp;situation=$situation");

  // invoices connected to that payment :
  $display["msg"] .= display_ok_msg ($msg);

  $url = url_prepare("payment_index.php?action=break_asso".
		    "&amp;param_payment=".$q_payment->f("payment_id"));

  $dis_invoices = new OBM_DISPLAY ("DATA", $invoices_display, "payment");
  $dis_invoices->data_set = $q_connected_invoices;
  $dis_invoices->data_header = "top";
  $dis_invoices->data_url = $url;
  $dis_invoices->data_order = false;

  // we provide a checkbox if payment is not paid...
  $block .=  "<form method=\"post\" name=\"break_asso\" action=\"$url_break\">\n";
  if ($situation == "easy") {
    $dis_invoices->data_cb_text = $l_payment_break_asso;
    $dis_invoices->data_idfield = "invoice_id";
    $dis_invoices->data_cb_side = "left";
    $dis_invoices->data_cb_name = "break_";
  }
  // here display stuff...

  $block .= $dis_invoices->display ();
  $block .=  "<br>
 <input type=\"submit\" value=\"$l_break_asso_go\" align=\"center\">
</form>";
  return $block;

} //   html_breaking_associations ()

///////////////////////////////////////////////////////////////////////////////
// display stuff below
///////////////////////////////////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////
// Display the screen used to change the display parameters of payments
//////////////////////////////////////////////////////////////////////////
function dis_payment_display_pref ($payment_options, $invoices_options, $et_options) {
  // Label
   global $l_payment_options, $l_invoice_options, $l_et_options;
  //Image
  global $set_theme ;
 
  $block .=  "<center>\n";
  $block .=  "<table><tr valign=\"top\">";
 
  $block .=  "<td>";
  $dis_pref = new OBM_DISPLAY("PREFERENCES",$payment_options) ;
  $dis_pref->pref_title = $l_payment_options;
  $dis_pref->display_entity = "payment" ;
  $dis_pref->pref_dis_help = 0 ; // help is displayed after et options

  $block .= $dis_pref->display() ;

  $block .=  "</td><td>" ;

  $invoice_prefs = new OBM_DISPLAY("PREFERENCES",$invoices_options) ;
  $invoice_prefs->display_entity = "payment"; 
  $invoice_prefs->pref_title = $l_invoice_options;
  $invoice_prefs->pref_dis_help = 0 ;

  $invoice_prefs->display() ;
 
  $block .=  "</td><td>" ;

  $et_prefs = new OBM_DISPLAY("PREFERENCES",$et_options) ;
  $et_prefs->display_entity = "payment"; 
  $et_prefs->pref_title = $l_et_options;
  $et_prefs->pref_dis_help =  0;

  $et_prefs->display() ;
 
  $block .=   "</td></tr></table>";
  $et_prefs->dis_pref_help ();
  $block .=  "</center>" ;
 return $block;
}
//
///
////
///////////////////////////////////////////////////////////////////////////////
// check of bank data below :
///////////////////////////////////////////////////////////////////////////////
////
///
//

///////////////////////////////////////////////////////////////////////////////
// displays a form to choose the file containing cvs data from the bank
///////////////////////////////////////////////////////////////////////////////
function html_choose_csv_file () {
  global $border, $cellspacing;
  global $l_choose_csv;

  $url = url_prepare("payment_index.php?action=reconcile_import");

  $block .=  "
<center>
<form method=\"post\" enctype=\"multipart/form-data\" action=\"$url\">
 <table cellspacing=\"$cellspacing\" border=\"$border\">
  <tr><td>
   <input type=\"hidden\" name=\"MAX_FILE_SIZE\" value=\"200000\">
   <input type=\"file\" name=\"fichier_csv\">
  </td><td>
   <input type=\"submit\" value=\"$l_choose_csv\" >
  </td></tr>
 </table>
</form>
</center>
 
";
return $block;
}

///////////////////////////////////////////////////////////////////////////////
// display all $q_entrytemp and asks the user if everything is ok
// $q_et_prefs display prefs for EntryTemp
///////////////////////////////////////////////////////////////////////////////
function html_reconcile_import ($q_entrytemp, $q_et_prefs) {
  global $l_import_ok, $l_restart_import;

  // il faut displayer les entrytemp, puis un form de confirm..
  $block .=  "<center><font color=\"#FF00FF\">";
  $block .=  $q_entrytemp->nf(). " opérations temporaires";
  $block .=  "</FONT><BR></CENTER>";

  $url_dm = url_prepare("payment_index.php?action=reconcile_import".
		       "&amp;import_file=no");

  $et_dis = new OBM_DISPLAY ("DATA", $q_et_prefs, "payment");
  $et_dis->data_set = $q_entrytemp;
  $et_dis->data_header = "top";
  $et_dis->data_order=false;
  $et_dis->data_url = $url_dm;

  $block .= $et_dis->display ();

  // confirmation form :
  $url_ok = url_prepare ("payment_index.php?action=select_reconcile");
  $url_again = url_prepare ("payment_index.php?action=reconcile");
  $block .=  "
<br>
<center>
 <form method=\"post\" action=\"\">
  <table>
   <tr><td>
    <input type=\"button\" value=\"$l_import_ok\" ".
     " onClick=\"this.form.action='$url_ok';this.form.submit()\">
   </td><td>
    <input type=\"button\" value=\"$l_restart_import\" ".
     " onClick=\"this.form.action='$url_again';this.form.submit()\">
   </td></tr>
  </table>
 </form>
</center>";  
return $block;
} // html_reconcile_import ()

///////////////////////////////////////////////////////////////////////////////
// page to check payments
///////////////////////////////////////////////////////////////////////////////
function html_select_reconcile ($q_payments, $q_et, $q_pay_pref, $q_et_pref) {
  global $border, $cellspacing;
  global $set_rows;
  global $l_reconcile, $l_nb_payments_to_reconcile;
  global $l_nb_entrytemp_to_reconcile;
  
  $url = url_prepare ("payment_index.php?action=select_reconcile");

  // preparing the payments display :
  $pay_dis = new OBM_DISPLAY("DATA", $q_pay_pref, "payment");
  $pay_dis->data_set = $q_payments;
  $pay_dis->data_header = "both";
  $pay_dis->data_order = false; // 2 simultaneous displays => trouble on sight
  $pay_dis->data_url = $url;

  // checkbox to select payments :
  $pay_dis->data_cb_text = $l_reconcile;
  $pay_dis->data_idfield = "payment_id";
  $pay_dis->data_cb_side = "right";
  $pay_dis->data_cb_name = "check_pay_";
  
  // preparing the entrytemp display :
  $et_dis = new OBM_DISPLAY ("DATA", $q_et_pref, "payment");
  $et_dis->data_set = $q_et;
  $et_dis->data_header = "both";
  $et_dis->data_order=false;
  $et_dis->data_url = $url;

  // checkbox to select entrytemps :
  $et_dis->data_cb_text = $l_reconcile;
  $et_dis->data_idfield = "entrytemp_id";
  $et_dis->data_cb_side = "left";
  $et_dis->data_cb_name = "check_et_";
  
  // if one of payments or ETs has more than $set_rows rows and the other 
  // doesn't, we add 2 <br>s to align vertically the 2 tables
  // by "compensating" the page number lines...
  if ($q_payments->nf() > $set_rows && $q_et->nf () < $set_rows) {
    $br_et = "<br><br>";
  } elseif ($q_payments->nf() < $set_rows && $q_et->nf() > $set_rows) {
    $br_pay = "<br><br>";
  }

  // we put the payments and prefs in a 2 cols table :
  $block .=  "
<center>
 <form method=\"post\" action=\"\">
 <table width=\"100%\" cellspacing=\"$cellspacing\" border=\"$border\">
  <tr>";
  // we put the number of payments/entrytemp in the header :
  $block .=  "<th>".$q_payments->nf().$l_nb_payments_to_reconcile."</th>";
  $block .= "<th>".$q_et->nf().$l_nb_entrytemp_to_reconcile."</th>";
  $block .= "</tr><tr>";
  // payments on the left :
  $block .="<td width=\"50%\" align=\"center\" valign=\"top\">";
  $block .= $br_pay;
  $pay_dis->display ();
  $block .= "
  </td>";
  // entrytemp on the right :
  $block .= "<td width=\"50%\" align=\"center\" valign=\"top\">";
  $block .= $br_et;
  $et_dis->display ();
  $block .= "
  </td>";

  $block .= "</tr>
 </table>
  <br/><br/>";
  // the buttons to validate this page...
  $url_go_on = url_prepare("payment_index.php?action=select_reconcile");
  $url_over = url_prepare("payment_index.php?action=do_reconcile");

  $block .= "
  <input type=\"button\" value=\"Valider les lignes cochées\" ".
    " onClick=\"this.form.action='$url_go_on' ; this.form.submit ();\">";
  $block .= "
  <input type=\"button\" value=\"Finir le Rapprochement\" ";
  // that button is only available when there are no more ETs unchecked
  if($q_et->nf() != 0) {
    $block .= "disabled "; // disabled doesn't seem to work with Netscape
  }
  $block .= " onClick=\"this.form.action='$url_over' ; this.form.submit ();\">";
  $block .= "
 </form>
</center>";
 return $block;
} // html_select_reconcile () 

///////////////////////////////////////////////////////////////////////////////
// displays an error message and gives the user a link to click on
// to restart the reconcile procedure...
///////////////////////////////////////////////////////////////////////////////
function html_reconcile_error_page ($error_msg) {
  global $l_restart_reconcile;
  global $display;

  $display["msg"] .= display_err_msg ($error_msg);
  $block .= "<br><a href=\"payment_index.php?action=select_reconcile\">".
    $l_restart_reconcile.
    "</a><br>";
  return $block;
} // html_reconcile_error_page ()

</SCRIPT>
