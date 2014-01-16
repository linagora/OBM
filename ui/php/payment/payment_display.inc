<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
$fieldnames['payment_date'] = $l_date;
$fieldnames['company_name'] = $l_company;
$fieldnames['payment_amount'] = $l_amount;
$fieldnames['payment_number'] = $l_number;
$fieldnames['paymentkind_label'] = $l_kind;
$fieldnames['payment_comment'] = $l_comment;

$fieldnames['invoice_label'] = $l_invoice_label;
$fieldnames['invoice_date'] = $l_invoice_date;
$fieldnames['invoice_amount_ttc'] = $l_invoice_ttc;
$fieldnames['invoice_amount_ht'] = $l_invoice_ht;
$fieldnames['invoice_number'] = $l_invoice_number;
$fieldnames['invoice_paid'] = $l_invoice_paid;
$fieldnames['invoice_company'] = $l_invoice_company;
$fieldnames['invoice_status'] = $l_invoice_status;
$fieldnames['invoice_deal'] = $l_deal;


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
  global $path,  $col_frs, $col_client;

  if ($fieldname == 'payment_date') {
    $date = $OD->data_set->f($fieldname);
    $date = of_date_format($date);
    $res['url'] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f('payment_id');
    $res['name'] = $date;
  }

  elseif ($fieldname == 'payment_comment') {
    $res['url'] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f('payment_id');
  }

  elseif ($fieldname == 'company_name') {
    $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('payment_company_id');
  }

  // amount column color 
  elseif (($fieldname == 'payment_amount')
	  || ($fieldname == 'pay_inv_paid')) {
    $amount = $OD->data_set->f($fieldname);
    if ( ($OD->data_set->f('payment_inout') == '+') // for a payment
	 // for an invoice
	 || ($OD->data_set->f('invoice_inout') == '+') ) {
      $couleur = $col_client;
    } else {
      $couleur = $col_frs;
    }
    $res['name'] = "<font color=\"#$couleur\">$amount</font>";
    $res['txt_name'] = $amount;
  }
  
  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Payment search form
// Parameters:
//   - $params[] : hash with parameters values
///////////////////////////////////////////////////////////////////////////////
function dis_payment_search_form($params='') {
  global $cgp_hide, $display, $l_module_payment;

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

  $comment = $params['comment'];
  $number = $params['number'];
  $amount = $params['amount'];
  $date_after = of_date_upd_format($params['date_after']);
  $date_before = of_date_upd_format($params['date_before']);
  $inout = $params['inout'];
  $kind = $params['kind'];
  $account = $params['account'];
  $company = $params['company'];
  $ichecked = ($params['checked']) ? 'checked' : '' ;

  // Inout
  $bcheck = (($inout == "$c_all")||($inout=='')) ? 'checked' : '';
  $pcheck = ($inout == '+') ? 'checked' : '';
  $mcheck = ($inout == '-') ? 'checked' : '';

  $rd_inout = "
    <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $bcheck />$l_both &nbsp;
    </label>
    <label><br />
    <input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck />(+) $l_received &nbsp;
    </label><label><br />
    <input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck />(-) $l_emitted &nbsp;
";

  // Account select
  $sel_account = "
    <select name=\"sel_account\">
     <option value=\"$c_all\">$l_all</option>\n";
  if (is_array($accs)) {
    foreach($accs as $a_id => $one_acc) {
      $alabel = $one_acc['label'];
      $aselect = ($a_id == $account) ? 'selected' : '';
      $sel_account .= "<option value=\"$a_id\" $aselect>$alabel</option>";
    }
  }
  $sel_account .= '</select>';
  
  // Kind select
  $sel_kind ="
    <select name=\"sel_kind\">
     <option value=\"$c_all\">$l_all</option>";
  if (is_array($kinds)) {
    foreach($kinds as $k_id => $one_kind) {
      $klabel = $one_kind['label'];
      $kselect = ($k_id == $kind) ? 'selected' : '';
      $sel_kind .= "<option value=\"$k_id\" $kselect>$klabel</option>";
    }
  }
  $sel_kind .= '</select>';

  $block = "
    <form method=\"get\" name=\"f_search\" action=\"payment_index.php?action=search\" class=\"search\">

      <label>$l_comment<br />
       <input name=\"tf_comment\" size=\"16\" value=\"$comment\" />
      </label>

      <label>$l_number<br />
       <input name=\"tf_number\" size=\"16\" maxlength=\"24\" value=\"$number\" />
      </label>

      <label>$l_amount<br />
       <input name=\"tf_amount\" size=\"12\" value=\"$amount\" />
      </label>

      <label>$l_date $l_after<br />
	<input type=\"text\" name=\"tf_date_after\" value=\"$dateafter\" class=\"datePicker\"/>
      </label>

      <label>$l_date $l_before<br />
       <input type=\"text\" name=\"tf_date_before\" value=\"$datebefore\" class=\"datePicker\"/>
      </label>

      <label>$l_inout<br />
       $rd_inout
      </label>

      <label>$l_company<br />
       <input type=\"text\" name=\"tf_company\" size=\"16\" value=\"$company\" />
      </label>

      <label>$l_account<br />
       $sel_account
      </label>

      <label>$l_kind<br />
       $sel_kind
      </label>

      <label>$l_include_checked<br />
       <input type=\"checkbox\" name=\"cb_checked\" value=\"y\" $ichecked />
      </label>

      <label><br />
       <input type=\"hidden\" name=\"action\" value=\"search\" />
       <input type=\"submit\" name=\"submit\" value=\"$l_find\" />  
      </label>
    <p class=\"CL\" />
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
  global $obm, $display, $l_found, $l_no_found, $l_payment;

  $prefs = get_display_pref($obm['uid'], 'payment');
  $obm_q = run_query_payment_search($params);
  $nb_payment = $obm_q->num_rows_total();
  if ($nb_payment == 0) {
    $display['msg'] .= display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_payment $l_found");
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

  $popup = $params['popup'];
  $comment = urlencode($params['comment']);
  $number = urlencode($params['number']);
  $amount = urlencode($params['amount']);
  $date_after = of_date_format($params['date_after'], true);
  $date_before = of_date_format($params['date_before'], true);
  $inout = $params['inout'];
  $kind = $params['kind'];
  $account = $params['account'];
  $company = urlencode($params['company']);

  if ($popup) {
    $ext_action = $params['ext_action'];
    $ext_url = $params['ext_url'];
    $ext_id = $params['ext_id'];
    $ext_target = $params['ext_target'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target";
  }

  $url = url_prepare("payment_index.php?action=search".
		    "&amp;tf_comment=$comment".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount=$amount".
		    "&amp;company=$company".
		    "&amp;tf_date_before=$date_before".
		    "&amp;tf_date_after=$date_after".
		    "&amp;sel_kind=$kind".
		    "&amp;sel_account=$account".
		    "&amp;rd_inout=$inout".
		    "$url_ext");

  $dis_p = new OBM_DISPLAY('DATA', $prefs, 'payment');
  if ($popup) {
    $dis_p->display_link = false;
    $dis_p->data_cb_text = 'X';
    $dis_p->data_idfield = 'payment_id';
    $dis_p->data_cb_name = 'cb_pay';
    $dis_p->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
    $dis_p->data_form_end = "
      <div class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </div>
      </form>";

    $display_popup_end = "<a href=\"\" onclick='window.close();'>$l_close</a>";
  }
  $dis_p->data_set = $obm_q;
  $dis_p->data_url = $url;
  $dis_p->data_header = 'both';

  // --- HTML Template --------------------------------------------------------
  $block .= $dis_p->display('dis_data_payment');
  $block .= $display_popup_end;

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Payment detail
// Parameters:
//   - $params[] : payment values (id)
///////////////////////////////////////////////////////////////////////////////
function dis_payment_consult($params) {
  global $display, $l_no_found, $path, $l_err_reference;

  $view = $params['view'];
  $id = $params['payment_id'];

  if ($id > 0) {
    $pay_q = run_query_payment_detail($id);
    $invs = get_payment_invoices($id);
    $display['detailInfo'] = display_record_info($pay_q);
    $block = html_payment_consult($pay_q, $invs);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
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
  global $path, $display, $ico_company;
  global $l_payment, $l_invoice, $l_company, $l_received, $l_emitted, $l_kind;
  global $l_comment, $l_number, $l_amount, $l_date, $l_label, $l_no_invoice;
  global $l_account, $l_inout, $l_total, $l_invoice_list, $l_credit_memo;

  $id = $p_q->f('payment_id');
  $c_id = $p_q->f('payment_company_id');
  $c_name = $p_q->f('company_name');
  $kind = $p_q->f('paymentkind_label');
  $date = of_date_format($p_q->f('date'));
  $number = $p_q->f('payment_number');
  $amount = $p_q->f('payment_amount');
  $inout = $p_q->f('payment_inout');
  $checked = $p_q->f('payment_checked');
  $comment = $p_q->f('payment_comment');
  $ad1 = $p_q->f('company_address1');
  $zip = $p_q->f('company_zipcode');
  $town = $p_q->f('company_town');
  $account = $p_q->f('account_label');

  if ($inout == '+') {
    $dis_inout = $l_received;
  } else {
    $dis_inout = $l_emitted;
  }

  if (count($invs) > 0) {
    $l_invoice_title = $l_invoice_list;
    $block_invoice = "
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_date</th>
        <th>$l_company</th>
        <th>$l_number</th>
        <th>$l_invoice</th>
        <th>$l_label</th>
        <th>$l_amount</th>
        <th>$l_payment : $l_amount</th>
      </tr>
      </thead> 
      <tbody>";
    $affected_amount_total = 0;
    $k = 0;
    foreach ($invs as $one_inv) {
      $i_id = $one_inv['id'];
      $i_affected_amount = $one_inv['affected_amount'];
      $i_inv_amount = $one_inv['invoice_amount'];
      $i_company = $one_inv['company'];
      $i_number = $one_inv['number'];
      $i_label = $one_inv['label'];
      $i_date = $one_inv['date'];
      $i_credit_memo = $one_inv['credit_memo'];
      $k ++;
      ($k % 2 == 0)?$class='pair':$class='';
      $affected_amount_total += $i_affected_amount;

      if ($i_credit_memo == '0') {
	$dis_credit_memo = $l_invoice;
      } else {
	$dis_credit_memo = $l_credit_memo;
      }

      $block_invoice .= "
      <tr class=\"$class\">
        <td>$i_date</td>
        <td>$i_company</td>
        <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$i_number</a></td>
        <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$dis_credit_memo</a></td>
        <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$i_label</a></td>
        <td>$i_inv_amount </td>
        <td>$i_affected_amount </td>
      </tr>";
    }

    $block_invoice .= "
      <tr class=\"highlight\">
        <th colspan=\"6\">$l_total</th>
        <td>$affected_amount_total</td>
      </tr>
      </tbody>
      </table>";

  } else {
    $l_invoice_title = $l_no_invoice;
  }

  $display['title'] = "<h1 class=\"title\">$l_payment : $date $c_name $amount</h1>";

  $block = "
<div class=\"detail infos\">
  <h1>$l_company</h1>
    <table>
    <tr>
      <th>$l_company 
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id")."\">
      <img src=\"$ico_company\" alt=\"[details]\" /></a>
      </th>
      <td>$c_name<br />$ad1<br/>$zip $town</td>
    </tr>
    </table>
</div>

<div class=\"detail infos\">
  <h1>$l_payment</h1>

  <table>
    <tr>
      <th>$l_amount</th>
      <td>$amount</td>
    </tr>
    <tr>
      <th>$l_date</th>
      <td>$date</td>
    </tr>
    <tr>
      <th>$l_inout</th>
      <td>$inout $dis_inout</td>
    </tr>
    <tr>
      <th>$l_kind</th>
      <td>$kind</td>
    </tr>
    <tr>
      <th>$l_number</th>
      <td>$number</td>
    </tr>
    <tr>
      <th>$l_account</th>
      <td>$account</td>
    </tr>

   </table>
</div>

<div class=\"detail extra\">
  <h1>$l_invoice_title</h1>
  $block_invoice
</div>
 
<div class=\"detail extra\">
  <h1>$l_comment</h1>
      $comment
</div>
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

  $id = $params['payment_id'];

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
  global $path, $display,  $l_update, $l_delete_invoice, $l_no_invoice;
  global $l_payment, $l_invoice, $l_company, $l_received, $l_emitted;
  global $l_comment, $l_number, $l_amount, $l_date, $l_label, $l_already_paid;
  global $l_total, $l_invoice_list;

  $id = $params['payment_id'];
  $pay = get_payment_info($id);
  $invs = $amounts['invoices'];

  if (count($invs) > 0) {
    $l_invoice_title = $l_invoice_list;
    $block_invoice = "
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_date</th>
        <th>$l_number</th>
        <th>$l_company</th>
        <th>$l_label</th>
        <th>$l_amount</th>
        <th>$l_already_paid</th>
        <th>$l_payment : $l_amount</th>
        <th>$l_delete_invoice</th>
      </tr>
      </thead>";
    $k = 0;
    $affected_amount_total = 0;
    foreach ($invs as $one_inv) {
      $i_id = $one_inv['id'];
      // if params sent (eg: invoice_update), take them else database infos
      if (isset($params['invoice']["$i_id"])) {
	$affected_amount = $params['invoice']["$i_id"];
      } else {
	$affected_amount = $one_inv['affected_amount'];
      }
      $i_inv_amount = $one_inv['invoice_amount'];
      $i_company = $one_inv['company'];
      $i_number = $one_inv['number'];
      $i_label = $one_inv['label'];
      $i_date = $one_inv['date'];
      $i_already_paid = $one_inv['already_paid'];

      $affected_amount_total += $affected_amount;
      $k++;
      ($k % 2 == 0) ? $class='pair' : $class='';
      $block_invoice .= "
        <tr class=\"$class\">
          <td>$i_date</td>
          <td>$i_company</td>
          <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$i_number</a></td>
          <td><a href=\"$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=$i_id\">$i_label</a></td>
          <td>$i_inv_amount </td>
          <td>$i_already_paid </td>
          <td><input type=\"text\" name=\"data-inv-$i_id\" value=\"$affected_amount\" /></td>
          <td><input type=\"checkbox\" name=\"cb_inv-$i_id\" value=\"1\" /></td>
        </tr>";
    }


    $block_invoice .= "
        <tr class=\"highlight\">
        <th colspan=\"6\">$l_total</th>
        <td>$affected_amount_total</td>
        <th></th>
        </tr>
        </tbody>
      </table>";

    $dis_invoice = "
  <form method=\"post\" name=\"f_inv\" action=\"payment_index.php\">
  $block_invoice

  <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"action\" value=\"invoice_update\" />
    <input type=\"hidden\" name=\"payment_id\" value=\"$id\" />
    <input type=\"submit\" value=\"$l_update\" />
  </fieldset>
  </form>";
  } else {
    $l_invoice_title = $l_no_invoice;
  }

  $display['title'] = "<h1 class=\"title\">$l_payment : $pay[date] $pay[company] $pay[amount]</h1>";

  $block = "
<div class=\"detail extra\">
  <h1>$l_invoice_title</h1>
  $dis_invoice
</div>";

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display Payment Form
// Parameters:
//   - $action    : action called
//   - $params[]  : parameters : default values
///////////////////////////////////////////////////////////////////////////////
function dis_payment_form($action, $params) {
  global $display, $l_aduplicate;

  $p_id = $params['payment_id'];
  if ($p_id > 0) {
    $p_q = run_query_payment_detail($p_id);
  } elseif ($params['id_duplicated'] > 0) {
    $p_q = run_query_payment_detail($params['id_duplicated']);
    $params['title'] = $p_q->f('payment_date') . ' - ' . $p_q->f('payment_amount') . ' - ' . $p_q->f('company_name') . " - $l_aduplicate";
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
  global $ico_company, $l_company,$ico_add,$ico_crow;
  global $l_payment, $l_invoice, $l_account, $l_kind, $l_date, $l_number;
  global $l_amount, $l_inout, $l_received, $l_emitted, $l_comment;
  global $l_update, $l_checkdelete, $l_insert;
  global $l_header_new, $ico_mini_cal;
  global $display, $path, $c_undef, $l_undef;

  // if update mode and first time values are taken from database
  if (($action == 'detailupdate') || ($action == 'detailduplicate')) {
    $id = $p_q->f('payment_id');
    $c_id = $p_q->f('payment_company_id');
    $c_name = $p_q->f('company_name');
    $kind = $p_q->f('payment_paymentkind_id');
    $date = $p_q->f('date');
    $dis_date = of_date_format($date);
    $date = ($date != 0) ? of_date_upd_format($date, 1) : '';
    $number = $p_q->f('payment_number');
    $amount = $p_q->f('payment_amount');
    $inout = $p_q->f('payment_inout');
    $checked = $p_q->f('payment_checked');
    $comment = $p_q->f('payment_comment');
    $title = $params['title'];
    $dis_title = "$dis_date $c_name $amount";

  // New form and first time
  } elseif ($action == 'new') {
    $dis_title = $l_header_new;
    $date = of_date_upd_format();
    $inout = '+';
  }

  // If parameters have been given, they supercede the default action value
  if (isset($params['company_id'])) { $c_id = $params['company_id']; }
  if (isset($params['company_name'])) { $c_name = $params['company_name']; }
  if (isset($params['company_new_id'])) { $c_new_id = $params['company_new_id']; }
  if (isset($params['company_new_name'])) { $c_new_name = urlencode($params['company_new_name']); }
  //  if (isset($params['company_new_name'])) { $c_new_name = $params['company_new_name']; }
  if (isset($params['payment_id'])) { $id = $params['payment_id']; }
  if (isset($params['invoice_id'])) { $invoice_id = $params['invoice_id']; }
  if (isset($params['invoice_name'])) { $invoice_name = $params['invoice_name']; }
  if (isset($params['account'])) { $account = $params['account']; }
  if (isset($params['kind'])) { $kind = $params['kind']; }
  if (isset($params['date'])) { $date = $params['date']; }
  if (isset($params['number'])) { $number = stripslashes($params['number']); }
  if (isset($params['amount'])) { $amount = stripslashes($params['amount']); }
  if (isset($params['inout'])) { $inout = stripslashes($params['inout']); }
  if (isset($params['checked'])) { $checked = stripslashes($params['checked']); }
  if (isset($params['comment'])) { $comment = stripslashes($params['comment']); }

  // Payment kind field
  $block_kind = of_category_dis_entity_form('payment', 'kind', $kinds, 'mono', $kind, 'none');

  // Invoice field (new from Invoice)
  if (isset($invoice_id)) {
    $dis_invoice = "
    <tr>
      <th>$l_invoice</th>
      <td>$invoice_name<input type=\"hidden\" id=\"invoice_name\" name=\"invoice_name\" value=\"$invoice_name\" /></td>
    </tr>
    <tr>
      <th>$l_invoice / $l_amount</th>
      <td><input type=\"text\" id=\"tf_amount_invoice\" name=\"tf_amount_invoice\" size=\"13\" maxlength=\"13\" value=\"$amount\" /></td>
    </tr>";
  }

  // Account select
  $sel_account ="
    <select name=\"sel_account\">
     <option value=\"$c_undef\">$l_undef</option>\n";
  if (is_array($accs))
    foreach($accs as $a_id => $one_acc) {
      $alabel = $one_acc['label'];
      $aselect = ($a_id == $account) ? 'selected' : '';
      $sel_account .= "<option value=\"$a_id\" $aselect>$alabel</option>";
    }
  $sel_account .= '</select>';

  // Type radio
  $pcheck = ($inout == '+') ? 'checked' : '';
  $mcheck = ($inout == '-') ? 'checked' : '';
  $rd_type = "
    <label><input type=\"radio\" name=\"rd_inout\" value=\"+\" $pcheck $readonly/>$l_received &nbsp;</label>
    <label><input type=\"radio\" name=\"rd_inout\" value=\"-\" $mcheck $readonly/>$l_emitted &nbsp;</label>
";

  // Company Display
  $dis_company = "<a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"popup('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company'); return false;\">
     <img src=\"$ico_add\" alt=\"\" /></a>
      <input type=\"text\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />";

  // UPDATE
  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_button = "
      <input type=\"hidden\" name=\"action\" value=\"update\" />
      <input type=\"hidden\" name=\"payment_id\" value=\"$id\" />
      <input type=\"submit\" value=\"$l_update\" />";

  // INSERT
  } elseif (($action=='new') || ($action=='insert') || ($action=='detailduplicate')) {
    $dis_button = "
      <input type=\"hidden\" name=\"invoice_id\" value=\"$invoice_id\" />
      <input type=\"hidden\" name=\"action\" value=\"insert\" />
      <input type=\"submit\" value=\"$l_insert\" />";
  }

  $display['title'] = "<h1 class=\"title\">$l_payment : $dis_title</h1>";

  // --- HTML Template --------------------------------------------------------
  $block .= "
    <form method=\"get\" name=\"f_entity\" onsubmit=\"if (check_payment(this)) return true; else return false;\" action=\"".url_prepare("payment_index.php")."\">
 
  <fieldset class=\"detail infos\"> 
    <legend>$l_company</legend>
  <table>
  <tr>
    <th>$l_company</th>
    <td>$dis_company</td>
  </tr>
  </table>
  </fieldset>

  <fieldset class=\"detail infos\"> 
    <legend>$l_payment</legend>
    <table>
    <tr>
      <th>$l_amount</th>
      <td><input type=\"text\" id=\"tf_amount\" name=\"tf_amount\" size=\"13\" maxlength=\"13\" value=\"$amount\" onChange=\"copyFieldValue(this, this.form.tf_amount_invoice)\" /></td>
    </tr>
    $dis_invoice
    <tr>
      <th>$l_date</th>
      <td>
       <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
      </td>
    </tr>
    <tr>
      <th>$l_inout</th>
      <td>$rd_type</td>
    </tr>
    $block_kind
    <tr>
      <th>$l_number</th>
      <td><input type=\"text\" name=\"tf_number\" size=\"24\" maxlength=\"24\" value=\"$number\" /></td>
    </tr>
    <tr>
      <th>$l_account</th> 
      <td>$sel_account</td>
    </tr>
    </table>
</fieldset>

<fieldset class=\"detail extra\"> 
    <legend>$l_comment</legend>
    <textarea name=\"ta_comment\" rows=\"6\" cols=\"72\">$comment</textarea>
</fieldset>

<fieldset class=\"buttons\">
    $dis_button
</fieldset>
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

  $url = url_prepare('payment_index.php');

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

  $display['msg'] .= display_ok_msg($l_can_delete);
  $block .= "
      <div class=\"buttons\">
        $dis_delete
        $dis_back
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
 
  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'payment');
  $dis_pref->pref_title = $l_payment_options;
  $dis_pref->pref_dis_help = 1;

  $block .= $dis_pref->display();

  return $block;
}

/**
 * Display the payment administration panel
 *
 * @return String $block
 */
function dis_payment_admin_index() {
  $kinds = get_payment_kinds();
  $block = dis_payment_kinds_form($kinds);
  return $block;
}

/**
 * Display the kinds of payment form
 *
 * @param array $kinds
 * @return String $block
 */
function dis_payment_kinds_form($kinds) {
  global $l_kind_manage;
  global $l_kind_label, $l_kind_short_label;
  global $l_kind_delete;
  global $ico_delete, $ico_ok;
  global $l_c_j_update, $l_c_insert, $l_c_new;
  
  // get current domain id
  $domain_id = $_SESSION['obm']['domain_id'];
  
  foreach($kinds as $kind) {
    $k_id = $kind['id'];
    $k_label = $kind['label'];
    $k_short_label = $kind['shortlabel'];
    
    $kinds_list .= "
    <tr>
    <td>
    <input type=\"hidden\" id=\"kind_$k_id\" value=\"$k_id\" />
    <a href=\"?action=kind_delete&amp;kind=$k_id\">
    <img src=\"$ico_delete\" alt=\"$l_kind_delete\" title=\"$l_kind_delete\" />
    </a>
    </td>
    <td>
    <input type=\"text\" id=\"tf_kind_label_$k_id\" value=\"$k_label\" maxlength=\"40\" />
    </td>
    <td>
    <input type=\"text\" id=\"tf_kind_short_label_$k_id\" value=\"$k_short_label\" maxlength=\"3\" />
    </td>
    <td>
    <a href=\"#\" onclick=\"return updateLine($k_id, 'kind')\">
    <img src=\"$ico_ok\" alt=\"$l_c_j_update\" title=\"$l_c_j_update\" />
    </a>
    </td>
    </tr>
    ";
    
  }
  
  $block = "
  	<div class=\"detail admin\">
 		<h1>$l_kind_manage</h1>
 		<form id=\"form_kind_update\" method=\"post\" action=\"?action=kind_update\">
 		<input type=\"hidden\" name=\"tf_kind_label\" value=\"\" />
 		<input type=\"hidden\" name=\"tf_kind_short_label\" value=\"\" />
 		<input type=\"hidden\" name=\"kind\" value=\"\" />
 		</form>
 		
 		<table>
 		<thead>
 		  <tr>
 		  <td colspan=\"4\">&nbsp;</td>
 		  </tr>
 		  <tr>
 		  <td colspan=\"2\">$l_kind_label</td>
 		  <td colspan=\"2\">$l_kind_short_label</td>
 		  </tr>
 		</thead>
 		<tbody>$kinds_list</tbody>
 		</table>
 		
 		<form name=\"form_kind_new\" method=\"post\" action=\"\" onsubmit=\"return check_kind_new(this)\">
 		<table>
 		<thead>
 		<tr>
 		<td colspan=\"2\">$l_c_new</td>
 		</tr>
 		</thead>
 		<tbody>
 		<tr>
 		<td>$l_kind_label <br /> <input type=\"text\" name=\"tf_kind_label\" maxlength=\"40\" /></td>
 		<td>$l_kind_short_label <br /> <input type=\"text\" name=\"tf_kind_short_label\" maxlength=\"3\" /></td>
 		</tr>
 		</tbody>
 		<tfoot>
 		<tr>
 		<td colspan=\"2\">
 		<input type=\"submit\" name=\"sub_kind\" value=\"$l_c_insert\" />
 		</td>
 		</tr>
 		</tfoot>
 		</table>
 		<input type=\"hidden\" name=\"action\" value=\"kind_insert\" />
 		</form>
 	</div>
  ";
 		
  return $block;
}

?>
