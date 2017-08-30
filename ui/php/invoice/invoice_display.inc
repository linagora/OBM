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
// OBM - File : invoice_display.inc
//     - Desc : Invoice Display file
// 2001-07-30 - AliaSource
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//---------------------------------------------------------------------------//
// Fields that appear in result lists                                        //
//---------------------------------------------------------------------------//
// Direct fields
$fieldnames['invoice_archive'] = $l_archive;
$fieldnames['invoice_number'] = $l_number;
$fieldnames['invoice_credit_memo'] = $l_credit_memo;
$fieldnames['invoice_company'] = $l_company;
$fieldnames['invoice_deal'] = $l_deal;
$fieldnames['invoice_project'] = $l_project;
$fieldnames['invoice_label'] = $l_label;
$fieldnames['invoice_amount_ht'] = $l_amount_ht;
$fieldnames['invoice_amount_ttc'] = $l_amount_ttc;
$fieldnames['invoice_status'] = $l_status;
$fieldnames['invoice_inout'] = $l_inout;
$fieldnames['invoice_date'] = $l_date;
$fieldnames['invoice_expiration_date'] = $l_expiration_date;
$fieldnames['invoice_payment_date'] = $l_payment_date;
$fieldnames['invoice_paid'] = $l_paid;
$fieldnames['payment_label'] = $l_label;
$fieldnames['payment_number'] = $l_number;
$fieldnames['payment_date'] = $l_date;
$fieldnames['payment_amount'] = $l_amount;

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
  global $path, $col_frs, $col_client, $l_supplier, $l_client;
  global $params;

  if ($fieldname == 'invoice_deal') {
    $res['url'] = "$path/deal/deal_index.php?action=detailconsult&amp;deal_id=".$OD->data_set->f('deal_id');
  }

  elseif ($fieldname == 'invoice_project') {
    $res['url'] = "$path/project/project_index.php?action=detailconsult&amp;project_id=".$OD->data_set->f('project_id');
  }

  elseif (($fieldname == 'invoice_label')) {
    if ($OD->display_ext == 'get_id') {
      $res['url'] = "javascript:check_get_id(".$OD->data_set->f('invoice_id').",'".addslashes($OD->data_set->f('invoice_label'))."');";
    } else if ($OD->display_ext == 'get_id_url') {
      $ext_url = $params['ext_url'];
      $res['url'] = "javascript:check_get_id_url('$ext_url',".$OD->data_set->f('invoice_id').");";
    } elseif($link_ok) {
      $res['url'] = "$path/invoice/invoice_index.php?action=detailconsult&amp;invoice_id=".$OD->data_set->f('invoice_id');
    }    
  }

  elseif ($fieldname == 'invoice_company') {
    $res['url'] = "$path/company/company_index.php?action=detailconsult&amp;company_id=".$OD->data_set->f('company_id');
  }

  elseif ($fieldname == 'invoice_inout') {
    if ($OD->data_set->f($fieldname) == '+') {
      $res['name'] = $l_client;
    } else {
      $res['name'] = $l_supplier;
    }
  }
  
  elseif ($fieldname == 'invoice_pay_label') {
    $res['url'] = "$path/payment/payment_index.php?action=detailconsult&amp;payment_id=".$OD->data_set->f('invoice_payment_id');
  }

  else if ($fieldname == 'invoice_credit_memo') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  else if ($fieldname == 'invoice_status') {
    $res['align'] = 'center';
    $s_id = $OD->data_set->f('invoice_status_id');
    $sta = get_invoice_status($s_id);
    $res['name'] = $sta[$s_id];
  }

  else if ($fieldname == 'invoice_archive') {
    $res['align'] = 'center';
    if ($OD->data_set->f($fieldname)) {
      $res['name'] = 'X';
    } else {
      $res['name'] = '&nbsp;';
      $res['txt_name'] = ' ';
    }
  }

  // amount
  elseif (($fieldname == 'invoice_amount_ht') 
	  || ($fieldname == 'invoice_amount_ttc')
	  || ($fieldname == 'invoice_paid')) {
    $amount = $OD->data_set->f($fieldname);
    if ($OD->data_set->f('invoice_inout') == '+') {
      $mul = 1;
    } else {
      $mul = -1;
    }
    if ($OD->data_set->f('invoice_credit_memo') == '1') {
      $mul = $mul * -1;
    }
    $res['align'] = 'right';
    $res['name'] = number_format($mul * $amount, 2);
  }

  else if ($fieldname == 'invoice_date') {
    $res['name'] = of_date_format($OD->data_set->f($fieldname), 1);
  }

  else if ($fieldname == 'invoice_expiration_date') {
    $res['name'] = of_date_format($OD->data_set->f($fieldname), 1);
  }

  else if ($fieldname == 'invoice_payment_date') {
    $date = of_date_format($OD->data_set->f($fieldname), 1);
    $res['name'] = $date;
  }

  else if ($fieldname == 'payment_amount') {
    $amount = $OD->data_set->f('payment_amount');
    $pi_amount = $OD->data_set->f('paymentinvoice_amount');
    if ($amount == $pi_amount) {
      $res['name'] = $amount;
    } else {
      $res['name'] = "$pi_amount ($amount)";
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Display Invoice specific dataset fields
// Parameters:
//   - $OD        : OBM_DISPLAY object (passed by reference)
//   - $fieldname : field to display
//   - $link_ok   : true if links must be displayed
// Returns:
//   - $res : hash with 'name', 'url', 'align' values
///////////////////////////////////////////////////////////////////////////////
function dis_footer_invoice(&$OD, $fieldname=null, $link_ok=null) {

  if ($OD->data_set->f('invoice_inout') == '-') {
    $multiplicator = -1;
  } else {
    $multiplicator = 1;
  }
  if ($OD->data_set->f('invoice_credit_memo') == '1') {
    $multiplicator = -1 * $multiplicator;
  } else {
    $multiplicator = 1 * $multiplicator;
  }

  if ($fieldname == 'invoice_amount_ht') {
    $OD->footer_data[$fieldname] += $OD->data_set->f($fieldname) * $multiplicator;
  } elseif ($fieldname == 'invoice_amount_ttc') {
    $OD->footer_data[$fieldname] += $OD->data_set->f($fieldname) * $multiplicator;
  }
}


///////////////////////////////////////////////////////////////////////////////
// Display Invoice search form
// Parameters:
//   - $invoice[] : hash with invoice values
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_search_form($invoice='') {
  global $ctt_sales;

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice['tasktype'];
  if (! is_array($tt_ids) && ($tt_ids == '')) {
    $tt_ids = '0';
  }

  $sta = get_invoice_status();
  $tts = get_global_tasktype($ctt_sales, $tt_ids);
  $regions = of_category_get_ordered('', 'region');
  $block .= html_invoice_search_form($invoice, $sta, $tts, $regions);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// invoice search Form
// Parameters :
//   - $invoice : invoice hash parameters/
//   - $sta     : Status array
//   - $tts     : Task Type array
//   - $regions : Regions array
//////////////////////////////////////////////////////////////////////////////
function html_invoice_search_form($invoice, $sta, $tts, $regions) {
  global $obm, $display, $path, $popup, $c_all, $l_all, $ctt_sales, $l_invoice;
  global $l_label_start, $l_number, $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_tt, $l_date, $l_after, $l_before, $l_inout;
  global $l_credit_memo, $l_company, $l_include_archive, $l_select_invoice;
  global $l_find, $l_both, $l_client, $l_supplier, $l_expiration, $l_payment;
  global $l_module_invoice;

  $label = $invoice['label'];
  $number = $invoice['number'];
  $ht = $invoice['amount_ht'];
  $ttc = $invoice['amount_ttc'];
  $status = $invoice['status'];
  $date_field = ($invoice['date_field'] ? $invoice['date_field'] : 'date');
  $date_after = of_date_upd_format($invoice['date_after'], true);
  $date_before = of_date_upd_format($invoice['date_before'], true);
  $credit_memo = $invoice['credit_memo'];
  $inout = $invoice['inout'];
  $tt = $invoice['tt'];
  $deal_id = $invoice['deal_id'];
  $company = stripslashes($invoice['company']);
  $comp_id = $invoice['company_id'];
  $project_new_id = $invoice['project_id'];
  $archive = ($invoice['archive']) ? 'checked' : '' ;
  $region = $invoice['region'];
  if (($invoice['action'] != 'search') && (! isset($invoice['delegation']))) { 
    $delegation = $obm['delegation'];
  }
  if ($invoice['delegation'] != '') {
    $delegation = stripslashes($invoice['delegation']);
  }

  $url_form = url_prepare('invoice_index.php');
  $sel_status_all = ($status == $c_all ? " selected = \"selected\"" : '');
  $sel_status = "<select name=\"sel_status\">
      <option value=\"$c_all\"$sel_status_all>$l_all</option>";
  if (is_array($sta)) {
    foreach ($sta as $s_id => $s_label) {
      $sel_status .= "<option value=\"$s_id\"";
      if ($status == "$s_id") {
	$sel_status .= " selected = \"selected\"";
      }
      $sel_status .= ">$s_label</option>";
    }
  }
  $sel_status .= '</select>';

  //     $block_status = of_select_dis_search_select($sta, 'status', $url_tt);

  if (($inout == "$c_all") || ($inout == '')) {
    $rb_both = 'checked';
  }
  if ($inout == '+') {
    $rb_customer = ' checked ';
  }
  if ($inout == '-') {
    $rb_supplier = ' checked ';
  }

  if (($credit_memo == "$c_all") || ($credit_memo == '')) {
    $rb_cm_both = 'checked';
  }
  if ($credit_memo == '0') {
    $rb_cm_invoice = ' checked ';
  }
  if ($credit_memo == '1') {
    $rb_cm_credit_memo = ' checked ';
  }

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, 'tt', $url_tt);

  if ($popup) {
    $ext_action = $invoice['ext_action'];
    $ext_type = $invoice['ext_type'];
    $ext_url = $invoice['ext_url'];
    $ext_id = $invoice['ext_id'];
    $ext_title = ($invoice['ext_title'] ? $invoice['ext_title'] : $l_select_invoice);
    $ext_target = $invoice['ext_target'];
    $ext = "<input name=\"ext_action\" type=\"hidden\" value=\"$ext_action\">
            <input name=\"ext_type\" type=\"hidden\" value=\"$ext_type\">
            <input name=\"ext_id\" type=\"hidden\" value=\"$ext_id\">
            <input name=\"ext_title\" type=\"hidden\" value=\"$ext_title\">
            <input name=\"ext_target\" type=\"hidden\" value=\"$ext_target\">
            <input name=\"ext_url\" type=\"hidden\" value=\"$ext_url\">";
    $display['title'] = $ext_title;
  }

  // Selected date field
  $s_date_field[$date_field] = "selected=\"selected\"";

  // Region select construction
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  $dis_delegation = of_delegation_dis_search_form($delegation);

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

    $block_region
    $dis_delegation

    <label>$l_amount_ht<br />
      <input type=\"text\" name=\"tf_amount_ht\" size=\"15\" value=\"$ht\" />
    </label>

    <label>$l_amount_ttc<br />
      <input type=\"text\" name=\"tf_amount_ttc\" size=\"15\" value=\"$ttc\" />
    </label>

    <label>$l_status<br />
      $sel_status
    </label>

    <label>$l_date<br />
      <select name=\"sel_date_field\">
        <option value=\"date\" $s_date_field[date]>$l_date</option>
        <option value=\"expiration_date\" $s_date_field[expiration_date]>$l_expiration</option>
        <option value=\"payment_date\" $s_date_field[payment_date]>$l_payment</option>
      </select>
    </label>

    <label>$l_date $l_after<br />
    <input type=\"text\" name=\"tf_date_after\" value=\"$date_after\" class=\"datePicker\"/>
    </label>

    <label>$l_date $l_before<br />
    <input type=\"text\" name=\"tf_date_before\" value=\"$date_before\" class=\"datePicker\"/>
    </label>

    <label>$l_include_archive<br />
      <input type=\"checkbox\" name=\"cba_archive\" value=\"1\" $archive /> 
    </label>

    <label>$l_inout<br />
      <input type=\"radio\" name=\"rd_inout\" value=\"$c_all\" $rb_both />
      $l_both &nbsp;
    </label>
    <label><br />
      <input type=\"radio\" name=\"rd_inout\" value=\"+\" $rb_customer />
      $l_client &nbsp;
    </label>
    <label><br />
      <input type=\"radio\" name=\"rd_inout\" value=\"-\" $rb_supplier />
      $l_supplier &nbsp;
    </label>

    <label>$l_module_invoice<br />
      <input type=\"radio\" name=\"rd_credit_memo\" value=\"$c_all\" $rb_cm_both />
      $l_both &nbsp;
    </label>
    <label><br />
      <input type=\"radio\" name=\"rd_credit_memo\" value=\"0\" $rb_cm_invoice />
      $l_invoice &nbsp;
    </label>
    <label><br />
      <input type=\"radio\" name=\"rd_credit_memo\" value=\"1\" $rb_cm_credit_memo />
      $l_credit_memo &nbsp;
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
  global $obm, $display, $l_found, $l_no_found;

  $prefs = get_display_pref($obm['uid'], 'invoice');
  $inv_q = run_query_invoice_search($invoice);
  $nb_inv = $inv_q->num_rows_total();
  if ($nb_inv == 0) {
    $display['msg'] = display_warn_msg($l_no_found);
  } else {
    $display['msg'] .= display_info_msg("$nb_inv $l_found");
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
  global $l_close, $l_add;

  $popup = $invoice['popup'];
  if ($popup) {
    $ext_action = $invoice['ext_action'];
    $ext_type = $invoice['ext_type'];
    $ext_url = $invoice['ext_url'];
    $ext_id = $invoice['ext_id'];
    $ext_target = $invoice['ext_target'];
    $ext_type = $invoice['ext_type'];
    $url_ext = "&amp;ext_action=$ext_action&amp;ext_url=$ext_url&amp;ext_id=$ext_id&amp;ext_target=$ext_target&amp;ext_type=$ext_type";
  }

  $label = urlencode($invoice['label']);
  $number = urlencode($invoice['number']);
  $ht = urlencode($invoice['amount_ht']);
  $ttc = urlencode($invoice['amount_ttc']);
  $status = $invoice['status'];
  if (is_array($status) && (count($status) > 0)) {
    foreach ($status as $s_id) {
      $param_status .= "&amp;sel_status[]=$s_id";
    }
  } else if ($status != '') {
    $param_status .= "&amp;sel_status=$status";
  }
  $date_field = ($invoice['date_field'] ? $invoice['date_field'] : 'date');
  $date_after = of_date_upd_format($invoice['date_after'], true);
  $date_before = of_date_upd_format($invoice['date_before'], true);
  $inout = urlencode($invoice['inout']);
  $credit_memo = $invoice['credit_memo'];
  $company = urlencode($invoice['company']);
  $archive = $invoice['archive'];
  $deal_id = $invoice['deal_id'];
  $comp_id = $invoice['company_id'];
  $project_new_id = $invoice['project_id'];
  $region = $invoice['region'];
  $delegation = $invoice['delegation'];

  $url = url_prepare("invoice_index.php?action=search".
		    "&amp;tf_label=$label".
		    "&amp;tf_number=$number".
		    "&amp;tf_amount_ht=$ht". 
		    "&amp;tf_amount_ttc=$ttc".
		    "$param_status".
		    "&amp;date_field=$date_field".
		    "&amp;tf_date_after=$date_after".
		    "&amp;tf_date_before=$date_before".
		    "&amp;rd_inout=$inout".
		    "&amp;credit_memo=$credit_memo".
		    "&amp;company_id=$comp_id".
		    "&amp;deal_id=$deal_id".
		    "&amp;project_id=$project_new_id".
		    "&amp;tf_company=$company".
		    "&amp;cba_archive=$archive".
		    "&amp;sel_region=$region".
		    "&amp;tf_delegation=$delegation".
                    "$url_ext"); 

  $dis_invoice = new OBM_DISPLAY ('DATA', $prefs, 'invoice');
  if ($popup) {
    $dis_invoice->display_link = false;
    if ($invoice['ext_type'] == 'multi') {
      $dis_invoice->data_cb_text = 'X';
      $dis_invoice->data_idfield = 'invoice_id';
      $dis_invoice->data_cb_name = 'cb_inv-';
      $dis_invoice->data_form_head = "
      <form target=\"$ext_target\" method=\"post\" action=\"$ext_url\">";
      $dis_invoice->data_form_end = "
      <fieldset class=\"buttons\">
        <input type=\"submit\" value=\"$l_add\" />
        <input type=\"hidden\" name=\"ext_id\" value=\"$ext_id\" />
        <input type=\"hidden\" name=\"action\" value=\"$ext_action\" />
      </fieldset>
      </form>";

    $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    } else {

      if ($ext_url != '') {
	$dis_invoice->display_ext = 'get_id_url';
      } else if ( ($ext_widget != '') && ($ext_widget_text != '') ) { 
	$dis_invoice->display_ext = 'get_id';
      }
      $display_popup_end = "
      <p>
      <a href=\"\" onclick='window.close();'>$l_close</a>
      </p>";
    }
  }

  $dis_invoice->data_set = $inv_q;
  $dis_invoice->data_header = 'top';
  $dis_invoice->data_url = $url;
  $dis_invoice->dis_data_footer = 'dis_footer_invoice';
  $block .= $dis_invoice->display('dis_data_invoice');
  $block .= $display_popup_end;  

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the Invoice detail screen
// Parameters:
//   - $invoice[] : invoice hash parameters
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_consult($invoice) {
  global $obm, $display, $l_no_found, $l_err_reference;

  $id = $invoice['invoice_id'];
  $uid = $obm['uid'];
  if ($id > 0) {
    $inv_q = run_query_invoice_detail($id);
    if ($inv_q->num_rows() != 1) {
      $display['msg'] .= display_err_msg($l_err_reference);
    }
    $pays = get_invoice_payment($id);
    $display['detailInfo'] = display_record_info($inv_q);
    $block = html_invoice_consult($inv_q, $pays);
  } else {
    $display['msg'] .= display_err_msg($l_err_reference);
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
  global $path, $display ;
  global $l_delete, $l_invoice, $l_payment, $l_no_payment, $l_total;
  global $l_list_payment, $l_company, $l_date, $l_number, $l_amount, $l_inout;
  global $l_credit_memo, $l_amount_remaining, $l_received, $l_emitted;

  $i_id = $inv_q->f('invoice_id');
  $amount_ttc = $inv_q->f('invoice_amount_ttc');
  $amount_remaining = $amount_ttc;
  $inv_inout = $inv_q->f('invoice_inout');
  $credit_memo = $inv_q->f('invoice_credit_memo');

  $block = dis_invoice_detail ($inv_q);

  if (count($pays) > 0) {
    $l_payment_title = $l_list_payment;
    $block_payment = "
      <table class=\"spreadSheet\">
      <thead>
      <tr>
        <th>$l_date</th>
        <th>$l_company</th>
        <th>$l_number</th>
        <th>$l_amount</th>
        <th>$l_inout</th>
        <th>$l_amount / $l_invoice</th>
      </tr>
      </thead>
      <tbody>";
    $k = 0;
    $affected_amount_total = 0;
    foreach ($pays as $id => $one_pay) {
      if ($id != 'total') {
        $affected_amount = $one_pay['affected_amount'];
        $amount = $one_pay['amount'];
        $inout = $one_pay['inout'];
        $company = $one_pay['company'];
        $number = $one_pay['number'];
        $date = $one_pay['date'];
        $k ++;
        ($k % 2 == 0)?$class='pair':$class='';

        if ($inout == '+') {
	  $dis_inout = $l_received;
	  if ($inv_inout == '+') {
	    $affected_amount_total += $affected_amount;
	  } else {
	    $affected_amount_total -= $affected_amount;
	  }
        } else {
	  $dis_inout = $l_emitted;
	  if ($inv_inout == '+') {
	    $affected_amount_total -= $affected_amount;
	  } else {
	    $affected_amount_total += $affected_amount;
	  }
        }
      
        $block_payment .= "
      <tr class=\"$class\">
        <td><a href=\"$path/payment/payment_index.php?action=detailconsult&amp;payment_id=$id\">$date</a></td>
        <td>$company</td>
        <td><a href=\"$path/payment/payment_index.php?action=detailconsult&amp;payment_id=$id\">$number</a></td>
        <td>$amount </td>
        <td>$dis_inout </td>
        <td>$affected_amount </td>
      </tr>";
      }
    }

    $amount_remaining = $amount_ttc - $affected_amount_total;
    if ($affected_amount_total == $amount_ttc) {
      $class_r = "class=\"ok\"";
    } else {
      $class_r = "class=\"error\"";
    }

    if ($amount_remaining == 0) {
      $dis_remaining = $amount_remaining;
    } else {
      $dis_remaining = "$amount_remaining &nbsp;/&nbsp; $amount_ttc";
    }

    $block_payment .= "
      <tr class=\"highlight\">
        <th colspan=\"5\">$l_total</th>
        <td><b>$affected_amount_total</b></td>
      </tr>
      <tr class=\"highlight\">
        <th colspan=\"5\">$l_amount_remaining</th>
        <td $class_r><b>$dis_remaining</td>
      </tr>
      </tbody>
    </table>";

  } else {
    $l_payment_title = $l_no_payment;
  }

  //  payments data :
  $block .= "<div class=\"detail extra\">
    <h1>$l_payment_title</h1>
    $block_payment
    </div>";

  $display['link'] = html_invoice_links($inv_q, $amount_remaining);

  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display: Invoice links menu
// Parameters:
//   - $inv_q            : invoice database result 
//   - $amount_remaining : [optionnal]amount remaining to pay
// Returns:
//   $r : string with XHTML code
///////////////////////////////////////////////////////////////////////////////
function html_invoice_links($inv_q, $amount_remaining='') {
  global $ico_document, $ico_add, $ico_payment;
  global $l_invoice, $l_new, $l_module_document, $l_module_payment;
  global $l_document_add;
  global $path, $cgp_show;

  $id = $inv_q->f('invoice_id');
  $name = $inv_q->f('invoice_label');
  $company_id = $inv_q->f('invoice_company_id');
  $company_name = urlencode($inv_q->f('company_name'));
  if ($amount_remaining > 0) {
    $amount = $amount_remaining;
  } else {
    $amount = $inv_q->f('invoice_amount_ttc');
  }

  $uname = urlencode($name);

  // Document
  if ($cgp_show['module']['document']) {
    $url_doc = url_prepare("$path/document/document_index.php?action=search&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_new = url_prepare("$path/document/document_index.php?action=new&amp;param_entity=$id&amp;entity=invoice");
    $url_doc_add = "$path/document/document_index.php?action=ext_get_ids&amp;popup=1&amp;ext_action=document_add&amp;ext_url=".urlencode($path."/invoice/invoice_index.php")."&amp;ext_id=$id&amp;ext_target=$l_invoice";
    $nb_document = run_query_global_document_nb ($id, 'invoice');
    $block_doc = "
  <div class=\"portlet\" id=\"invoiceDocument\">
  <h1>$l_module_document</h1>
  <ul>
    <li><a href=\"$url_doc\"><img src=\"$ico_document\" alt=\"\" /></a>
        <a href=\"$url_doc\">$l_module_document ($nb_document)</a></li>
    <li><a href=\"$url_doc_new\"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
        <a href=\"$url_doc_new\">$l_new</a></li>
    <li><a href=\"\" 
	 onclick=\"popup('$url_doc_add',''); return false;
	 \"><img src=\"$ico_add\" alt=\"[Add]\" /></a>
	<a href=\"\" onclick=\"window.name='$l_invoice'; popup('$url_doc_add',''); return false;\">
	 $l_document_add</a></li>
  </ul>
  </div>";
  }

  // Payment
  if ($cgp_show['module']['payment']) {
    $url_pay = url_prepare("$path/payment/payment_index.php?action=search&amp;sel_invoice_id=$id");
    $url_pay_new = url_prepare("$path/payment/payment_index.php?action=new&amp;invoice_id=$id&amp;invoice_name=$uname&amp;company_id=$company_id&amp;company_name=$company_name&amp;tf_amount=$amount");
    $nb_pay = get_global_linked_payment_nb($id, 'invoice');
    $block_payment = "
  <div class=\"portlet\" id=\"invoicePayment\">
  <h1>$l_module_payment</h1>
  <ul>
    <li><a href=\"$url_inv\"><img src=\"$ico_payment\" alt=\"\" /></a>
        $l_module_payment ( <a href=\"$url_pay\">$nb_pay</a> )</li>
    <li><a href=\"$url_pay_new\"><img src=\"$ico_add\" alt=\"\" /></a>
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
  global $display,  $path, $ico_company, $ico_deal, $ico_project;
  global $l_company, $l_deal, $l_project,$l_amount_ht, $l_amount_ttc;
  global $l_date, $l_label, $l_number, $l_inout, $l_supplier, $l_client;
  global $l_archive, $l_status, $l_comment, $l_invoice, $l_credit_memo;
  global $l_expiration_date, $l_payment_date;
  global $l_bank, $l_model;
  global $l_export_to_odf;
  global $l_reminder;
  global $l_yes, $l_no;

  $label = $inv_q->f('invoice_label');
  $num = $inv_q->f('invoice_number');
  $date = of_date_format($inv_q->f('date'), 1);
  $expiration_date = of_date_format($inv_q->f('expiration_date'), 1);
  $payment_date = of_date_format($inv_q->f('payment_date'), 1);
  $status_id = $inv_q->f('invoice_status_id');
  $status = get_invoice_status();
  $status_label = $status[$status_id];
  $inout = ($inv_q->f('invoice_inout') == '-') ? $l_supplier : $l_client;
  $credit_memo = ($inv_q->f('invoice_credit_memo') == '0') ? $l_invoice : $l_credit_memo;
  $title = $inout;
  $ht = $inv_q->f('invoice_amount_ht');
  $ttc = $inv_q->f('invoice_amount_ttc');
  $comment = beautify_comment(nl2br($inv_q->f('invoice_comment')));
  $comp_id = $inv_q->f('invoice_company_id');
  $company = $inv_q->f('company_name');
  $deal_id = $inv_q->f('invoice_deal_id');
  $deal = $inv_q->f('deal_label');
  $project_id = $inv_q->f('invoice_project_id');
  $project = $inv_q->f('project_name');
  $archive = ($inv_q->f('invoice_archive') == 1 ? $l_yes : $l_no);

  $display['title'] = $label;

  $url_comp = url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$comp_id");
  if ($deal_id > 0) {
    $url_deal = url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$deal_id");
    $deal_link = "<a href=\"$url_deal\"><img src=\"$ico_deal\" alt=\"\" /></a>";
  }
  if ($project_id > 0) {
    $url_proj = url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$project_id");
    $project_new_link = "<a href=\"$url_proj\"><img src=\"$ico_project\" alt=\"\" /></a>";
  }

  $qaccount = run_query_invoice_account($inv_q->f('invoice_id'));
  $accounts = "<option value=\"0\">-- $l_bank</option>";
  while ($qaccount->next_record()) {
    $accounts .= "<option value=\"{$qaccount->f('account_id')}\">{$qaccount->f('account_label')}</option>\n";
  }
  
  $odf_models = glob('model_*.odt');
  $odf_models_opts = "<option value=\"0\">-- {$l_model}</option>\n";
  foreach ($odf_models as $odf_model) {
    if (preg_match("/model_(.*).odt/", $odf_model, $odf_model_name)) {
      $odf_models_opts .= "<option value=\"{$odf_model}\">{$odf_model_name[1]}</option>\n";
    }
  }

  $export_form = '
       <form action="invoice_index.php?action=detailconsult&invoice_id=' . $inv_q->f('invoice_id') . '" method="post">
         <select name="sel_account" class="width_auto">
         ' . $accounts . '
         </select>
         <select name="sel_odf_model" class="width_auto">
         ' . $odf_models_opts . '
         </select>
         <input name="export_odf" type="submit" value="ok" class="width_auto" />
       </form>
  ';


$odf_relance = glob('relance_*.odt');

list( $year_o, $month_o, $day_o ) = explode( ',', date('Y,n,j', strtotime($inv_q->f('invoice_expiration_date'))));
list( $year_c, $month_c, $day_c ) = explode( ',', date('Y,n,j'));

$duration =  (mktime(0,0,0, $month_c, $day_c, $year_c) - mktime(0,0,0, $month_o, $day_o, $year_o));

$duration_day = (int) ( $duration / (60 * 60 * 24));


$reminder_form ="";
$label_r="";

if($inv_q->f('invoice_status_id')=='1' && $duration_day > 15){
	
	$label_r = $l_reminder;

	$reminder_form ='<form action="invoice_index.php?action=reminder&invoice_id=' . $inv_q->f('invoice_id') . '"  method="post"><input name="relance_odf" type="submit" value="Relance 1" class="width_auto" />';
  
	}
	

if($inv_q->f('invoice_status_id')=='1' && $duration_day > 45){
	$reminder_form .= ' <input name="relance2_odf" type="submit" value="Relance 2" class="width_auto" />
        </form>';
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
    <tr>
      <th>$l_inout</th>
      <td>$inout</td>
    </tr>
    <tr>
      <th>$l_invoice / $l_credit_memo</th>
      <td>$credit_memo</td>
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
      <td>$status_label</td>
    </tr>
    <tr>
      <th>$l_amount_ht</th>
      <td>$ht</td>
    </tr>
    <tr>
      <th>$l_amount_ttc</th>
      <td>$ttc</td>
    </tr>
    <tr>
      <th>$l_export_to_odf :</th>
      <td>$export_form</td>
    </tr>
<tr>
      <th>$label_r</th>
      <td>$reminder_form</td>
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
//   - $field : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_form($action, $invoice, $field='') {
  global $display, $l_no_found;

  $id = $invoice['invoice_id'];
  if ($id > 0) {
    $inv_q = run_query_invoice_detail($id);
    $display['detailInfo'] = display_record_info($inv_q);
  } else {
    $inv_q = '';
  }
  $stat = get_invoice_status();
  $usr_q = run_query_userobm_active();
  $block = html_invoice_form($action, $invoice, $inv_q, $stat, $usr_q, $field);

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
//   - $field     : [optionnal] field in error
///////////////////////////////////////////////////////////////////////////////
function html_invoice_form($action, $invoice, $inv_q, $stat, $usr_q, $field='') {
  global $obm, $path, $display;
  global $ico_company, $ico_deal, $ico_project, $l_header_new_f;
  global $l_label, $l_number, $l_comment, $l_add_comment, $l_upd_comment;
  global $l_amount_ht, $l_amount_ttc;
  global $l_status, $l_date, $l_inout, $l_credit_memo, $l_client, $l_supplier;
  global $l_insert, $l_update, $l_archive, $l_expiration_date;
  global $l_invoice, $l_company, $l_deal, $l_project, $l_payment_date;
  global $default_invoice_numbering, $c_php_isodate_format;
  global $cdefault_tax, $l_tax_rate, $l_compute_tax; 

  $uid = $obm['uid'];

  // if update mode and first time values are taken from database
  if (($action == 'detailupdate') || ($action == 'duplicate')) {
    $id = $inv_q->f('invoice_id');
    $label = $inv_q->f('invoice_label');
    $number = $inv_q->f('invoice_number');
    $archive = ($inv_q->f('invoice_archive') == 1 ? ' checked' : '');
    $comment = $inv_q->f('invoice_comment');
    $status = $inv_q->f('invoice_status_id');
    $date = of_date_upd_format($inv_q->f('date'), 1);
    $expiration_date = of_date_upd_format($inv_q->f('expiration_date'), 1);
    $payment_date = of_date_upd_format($inv_q->f('payment_date'), 1);
    $inout = $inv_q->f('invoice_inout');
    $credit_memo = $inv_q->f('invoice_credit_memo');
    $ht = $inv_q->f('invoice_amount_ht');
    $ttc = $inv_q->f('invoice_amount_ttc');
    $c_id = $inv_q->f('invoice_company_id');
    $c_name = $inv_q->f('company_name');
    $d_id = $inv_q->f('invoice_deal_id');
    $d_label = $inv_q->f('deal_label');
    $p_id = $inv_q->f('invoice_project_id');
    $p_name = $inv_q->f('project_name');
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $dis_title = $label;

  // New form and first time
  } elseif ($action == 'new') {
    $date = of_date_upd_format(); 
    $expiration_date = $date;
    $payment_date = $date;
    $number = date ($default_invoice_numbering);
    // when creating from a deal, default to use same 'inout' as the dealtype
    if ($d_id >= 1) {
      $inout = run_query_invoice_deal_get_inout ($d_id);
    }
    $usercomment = $uid;
    $datecomment = of_date_upd_format();
    $inout = '+';
    $credit_memo = 0;
  }

  // if we duplicate we have to modify some fields :
  if ($action == 'duplicate') {
    $comment = "duplicata de <$label>\n".$comment;
    $date = of_date_upd_format();
    $label .= '-'.date('Y-m-d');
  }

  // If parameters have been given, they supercede the default action value
  if (isset($invoice['invoice_id'])) { $id = $invoice['invoice_id']; }
  if (isset($invoice['label'])) { $label = stripslashes($invoice['label']); }
  if (isset($invoice['number'])) { $number = $invoice['number']; }
  if (isset($invoice['archive'])) { $archive = ($invoice['archive'] == 1 ? 'checked' : ''); }
  if (isset($invoice['date'])) { $date = of_date_upd_format($invoice['date']); }
  if (isset($invoice['expiration_date'])) { $expiration_date = of_date_upd_format($invoice['expiration_date']); }
  if (isset($invoice['payment_date'])) { $payment_date = of_date_upd_format($invoice['payment_date']); }
  if (isset($invoice['status'])) { $status = $invoice['status']; }
  if (isset($invoice['inout'])) { $inout = $invoice['inout']; }
  if (isset($invoice['credit_memo'])) { $credit_memo = $invoice['credit_memo']; }
  if (isset($invoice['tax'])) { $tax = $invoice['tax']; }
  if (isset($invoice['amount_ht'])) { $ht = $invoice['amount_ht']; }
  if (isset($invoice['amount_ttc'])) { $ttc = $invoice['amount_ttc']; }
  if (isset($invoice['comment'])) { $comment = stripslashes($invoice['comment']); }
  if (isset($invoice['add_comment'])) { $add_comment = stripslashes($invoice['add_comment']); }
  if (isset($invoice['usercomment'])) { $usercomment = $invoice['usercomment']; }
  if (isset($invoice['datecomment'])) { $datecomment = of_date_upd_format($invoice['datecomment']); }

  if (isset($invoice['company_id'])) { $c_id = $invoice['company_id']; }
  if (isset($invoice['company_name'])) { $c_name = stripslashes($invoice['company_name']); }
  if (isset($invoice['company_new_id'])) { $c_new_id = $invoice['company_new_id']; }
  if (isset($invoice['company_new_name'])) { $c_new_name = stripslashes($invoice['company_new_name']); }
  if (isset($invoice['deal_id'])) { $d_id = $invoice['deal_id']; }
  if (isset($invoice['deal_label'])) { $d_label = stripslashes($invoice['deal_label']); }
  if (isset($invoice['deal_new_id'])) { $d_new_id = $invoice['deal_new_id']; }
  if (isset($invoice['deal_new_label'])) { $d_new_label = stripslashes($invoice['deal_new_label']); }
  if (isset($invoice['project_id'])) { $p_id = $invoice['project_id']; }
  if (isset($invoice['project_name'])) { $p_name = stripslashes($invoice['project_name']); }
  if (isset($invoice['project_new_id'])) { $p_new_id = $invoice['project_new_id']; }
  if (isset($invoice['project_new_name'])) { $p_new_name = stripslashes($invoice['project_new_name']); }

  $title = ($inout == '-') ? $l_supplier : $l_client;

  // Mark the error field
  if ($field != '') {
    $class[$field] = 'error';
  }

  // Status select
  $sel_status = "<select name=\"sel_status\">"; 
  foreach ($stat as $s_id => $s_label) {
    $sel_status .= "<option value=\"$s_id\""; 
    if ($s_id == $status) {
      $sel_status .= ' selected ';
    }
    $sel_status .= ">$s_label</option>"; 
  }
  $sel_status .= '</select>';

  $inout_c = ($inout == '+') ? 'checked' : '';
  $inout_s = ($inout == '-') ? 'checked' : '';

  $dis_inout = "
    <th>$l_inout</th>
    <td>
      <label><input type=\"radio\" name=\"rd_inout\" value=\"+\" $inout_c /> $l_client</label>
      <label><input type=\"radio\" name=\"rd_inout\" value=\"-\" $inout_s /> $l_supplier</label>
    </td>";

  $cm_c = ($credit_memo == '1') ? 'checked' : '';
  $cm_i = ($credit_memo == '0') ? 'checked' : '';

  $dis_credit_memo = "
    <th>$l_invoice / $l_credit_memo</th>
    <td>
      <label><input type=\"radio\" name=\"rd_credit_memo\" value=\"0\" $cm_i /> $l_invoice</label>
      <label><input type=\"radio\" name=\"rd_credit_memo\" value=\"1\" $cm_c /> $l_credit_memo</label>
    </td>";

  // tax select
  if (is_array($cdefault_tax)) {
    $taxes = $cdefault_tax;
  } else {
    $taxes = array($cdefault_tax);
  }
  $sel_tax = "<select name=\"sel_tax\" onChange=\"compute_tax(this.form, true);\">"; 
  foreach ($taxes as $tax_label => $tax_value) {
    $sel_tax .= "<option value=\"$tax_value\""; 
    if ($tax_value == $tax) {
      $sel_tax .= ' selected ';
    }
    $sel_tax .= ">$tax_label</option>"; 
  }
  $sel_tax .= '</select>';
  

  if (($action == 'detailupdate') || ($action == 'update')) {
    $dis_comment = "
      $l_upd_comment
      <textarea name=\"ta_comment\" rows=\"6\" cols=\"78\">$comment</textarea>";
  }

  // User comment select construction
  if ($usr_q->nf()>0) {
    $usr_q->seek(0);
  }
  $sel_usercomment = "<select name=\"sel_usercomment\">";
  while ($usr_q->next_record()) {
    $cid = $usr_q->f('userobm_id');
    $cname = $usr_q->f('userobm_lastname').' '.$usr_q->f('userobm_firstname');
    $sel_usercomment .= "<option value=\"$cname\"";
    // First time default to uid of connected user (uid) then set to name
    if ( ($usercomment == $cid) || ($usercomment == $cname) ) {
      $sel_usercomment .= " selected = \"selected\"";
    }
    $sel_usercomment .= ">$cname</option>\n";
  }
  $sel_usercomment .= '</select>';

  // Buttons
  if (($action == 'update') || ($action == 'detailupdate')) {
    $dis_button .="<input type=\"hidden\" name=\"action\" value=\"update\" />
    <input type=\"hidden\" name=\"invoice_id\" value=\"$id\" />
    <input type=\"hidden\" name=\"hd_rd_inout\" value=\"$inout\" />
    <input type=\"submit\" value=\"$l_update\"
       onClick=\"if (check_invoice(this.form)) return true; else return false;\" />";
  } elseif (($action == 'new') || ($action == 'insert')
            || ($action == 'duplicate')) {
    $dis_button .= "
    <input type=\"submit\" value=\"$l_insert\" 
      onClick=\"if(check_invoice(this.form)) return true ; else return false;\" />
    <input type=\"hidden\" name=\"action\" value=\"insert\" />";
  }

  $display['title'] = $dis_title;

  $block = "
  <form method=\"post\" name=\"f_entity\" action=\"invoice_index.php\">
<fieldset class=\"detail infos\">
  <legend>$title</legend>

  <table>
  <tr>
    <th class=\"$class[company]\">$l_company</th>
    <td>
      <a href=\"". url_prepare("$path/company/company_index.php?action=detailconsult&amp;company_id=$c_id") . "\">
      $c_name
      </a><input type=\"hidden\" name=\"company_id\" value=\"$c_id\" />
      <input type=\"hidden\" name=\"company_name\" value=\"$c_name\" />
      <input type=\"hidden\" name=\"company_new_id\" value=\"$c_new_id\" />
      <a href=\"\" onclick=\"popup('$path/company/company_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.company_new_id&amp;ext_widget_text=f_entity.company_new_name','Company'); return false;\">
      <img src=\"$ico_company\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"company_new_name\" value=\"$c_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <th class=\"$class[deal]\">$l_deal</th>
    <td>
      <a href=\"". url_prepare("$path/deal/deal_index.php?action=detailconsult&amp;deal_id=$d_id") . "\">
      $d_label
      </a><input type=\"hidden\" name=\"deal_id\" value=\"$d_id\" />
      <input type=\"hidden\" name=\"deal_label\" value=\"$d_label\" />
      <input type=\"hidden\" name=\"deal_new_id\" value=\"$d_new_id\" />
      <a href=\"\" onclick=\"popup('$path/deal/deal_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.deal_new_id&amp;ext_widget_text=f_entity.deal_new_label','Deal'); return false;\">
      <img src=\"$ico_deal\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"deal_new_label\" value=\"$d_new_label\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr><tr>
    <th class=\"$class[project]\">$l_project</th>
    <td>
      <a href=\"". url_prepare("$path/project/project_index.php?action=detailconsult&amp;project_id=$d_id") . "\">
      $p_name
      </a><input type=\"hidden\" name=\"project_id\" value=\"$p_id\" />
      <input type=\"hidden\" name=\"project_name\" value=\"$p_name\" />
      <input type=\"hidden\" name=\"project_new_id\" value=\"$p_new_id\" />
      <a href=\"\" onclick=\"popup('$path/project/project_index.php?action=ext_get_id&amp;popup=1&amp;ext_widget=f_entity.project_new_id&amp;ext_widget_text=f_entity.project_new_name','Project'); return false;\">
      <img src=\"$ico_project\" alt=\"\" /></a>
      <input type=\"text\" size=\"32\" name=\"project_new_name\" value=\"$p_new_name\" readonly=\"readonly\" onfocus=\"this.blur();\" />
    </td>
  </tr>
  <tr>
    $dis_inout
  </tr>
  <tr>
    $dis_credit_memo
  </tr>
  </table>
  </fieldset>

<fieldset class=\"detail infos\">
  <legend>$l_invoice</legend>

  <table>
  <tr>
    <th class=\"$class[label]\">$l_label</th>
    <td>
      <input name=\"tf_label\" type=\"text\" size=\"40\" value=\"$label\" />
    </td>
  </tr><tr>
    <th class=\"$class[number]\">$l_number</th>
    <td>
      <input name=\"tf_number\" type=\"text\" size=\"11\" value=\"$number\" />
    </td>
  </tr><tr>
    <th class=\"$class[archive]\"><label for=\"cba_archive\">$l_archive</label></th>
    <td><input type=\"checkbox\" id=\"cba_archive\" name=\"cba_archive\" value=\"1\" $archive /></td>
  </tr><tr>
    <th class=\"$class[date]\">$l_date</th>
    <td>
      <input type=\"text\" name=\"tf_date\" value=\"$date\" class=\"datePicker\"/>
    </td>
  </tr><tr>
    <th class=\"$class[expiration_date]\">$l_expiration_date</th>
    <td>
      <input type=\"text\" name=\"tf_expiration_date\" value=\"$expiration_date\" class=\"datePicker\"/>
    </td>
  </tr><tr>
    <th class=\"$class[payment_date]\">$l_payment_date</th>
    <td>
    <input type=\"text\" name=\"tf_payment_date\" value=\"$payment_date\" class=\"datePicker\"/>
    </td>
  </tr>
  <tr>
    <th class=\"$class[status]\">$l_status</th>
    <td>$sel_status</td>
  </tr>
  <tr>
    <th class=\"$class[amount_ht]\">$l_amount_ht</th>
    <td>
      <input name=\"tf_amount_ht\" type=\"text\" size=\"15\" value=\"$ht\" onChange=\"compute_tax(this.form, true);\" />
    </td>
  </tr><tr>
    <th class=\"$class[tax_rate]\">$l_tax_rate</th>
    <td>$sel_tax</td>
  </tr><tr>
    <th class=\"$class[amount_ttc]\">$l_amount_ttc</th>
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

  $url = url_prepare('invoice_index.php');

  $dis_back = "<form name=\"form_back\" method=\"post\" action=\"$url\">
    <input type=\"hidden\" name=\"action\" value=\"detailconsult\" />
    <input type=\"hidden\" name=\"invoice_id\" value=\"$p_id\" />
    <input type=\"submit\" value=\"$l_back\" />
    </form>";

  $dis_delete = "<form name=\"form_delete\" method=\"post\" action=\"$url\" onsubmit=\"return confirm_del()\">
    <input type=\"hidden\" name=\"action\" value=\"delete\" />
    <input type=\"hidden\" name=\"invoice_id\" value=\"$p_id\" />
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
// Display the invoice dashboard Index
// Parameters:
//   - $invoice : invoice hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_dashboard_index($invoice) {
  global $display, $path;
  global $l_year_detail, $l_year_to_year, $l_client, $l_supplier;

  $view = $invoice['dash_view'];
  $url_client_year_detail = url_prepare("invoice_index.php?action=dashboard&amp;dash_view=year_detail&amp;dash_target=client");
  $url_client_year_to_year = url_prepare("invoice_index.php?action=dashboard&amp;dash_view=year_to_year&amp;dash_target=client");
  $url_supplier_year_detail = url_prepare("invoice_index.php?action=dashboard&amp;dash_view=year_detail&amp;dash_target=supplier");
  $url_supplier_year_to_year = url_prepare("invoice_index.php?action=dashboard&amp;dash_view=year_to_year&amp;dash_target=supplier");

  $block = "
    <fieldset class=\"detail infos\">
      <legend><a href=\"$url_client_year_detail\">$l_year_detail</a></legend>
      <ul>
        <li><a href=\"$url_client_year_detail\">$l_client</a></li>
        <li><a href=\"$url_supplier_year_detail\">$l_supplier</a></li>
      </ul>
    </fieldset>
    <fieldset class=\"detail infos\">
      <legend><a href=\"$url_client_year_to_year\">$l_year_to_year</a></legend>
      <ul>
        <li><a href=\"$url_client_year_to_year\">$l_client</a></li>
        <li><a href=\"$url_supplier_year_to_year\">$l_supplier</a></li>
      </ul>
    </fieldset>
";

  if (isset($invoice['year'])) {
    $year = $invoice['year'];
  } else {
    $year = date('Y');
  }
  $year_prev = $year - 1;

  $label_target = ${"l_$invoice[dash_target]"};

  if (($view == '') || ($view == 'client_year_detail')) {
    $title = "$l_client : $year";
    $block .= dis_invoice_dashboard($invoice);
  } else if ($view == 'year_detail') {
    $title = "$label_target : $year";
    $block .= dis_invoice_dashboard($invoice);
  } else if ($view == 'year_to_year') {
    $title = "$label_target : $year_prev / $year";
    $block .= dis_invoice_dashboard_year_to_year($invoice);
  }

  $display['title'] = $title;
 
  return $block;
}


///////////////////////////////////////////////////////////////////////////////
// Display the invoice dashboard
// Parameters:
//   - $invoice : invoice hash infos
///////////////////////////////////////////////////////////////////////////////
function dis_invoice_dashboard($invoice) {
  global $obm, $display, $path, $ctt_sales, $cinv_status;
  global $l_status, $l_total, $l_invoice, $l_date, $l_amount_ht, $l_amount_ttc;
  global $l_billed, $l_order_no_bill, $l_potential, $l_monthsofyear;
  global $l_payment, $l_amount_ht, $l_amount_ttc, $l_paid, $l_waiting;
  global $l_execute, $l_year_detail, $ico_next, $ico_previous;

  if (isset($invoice['year'])) {
    $year = $invoice['year'];
  } else {
    $year = date('Y');
  }
  $year_prev = $year - 1;
  $year_next = $year + 1;
  $date_ranges = array(array("$year-01-01", "$year-12-31"));

  if (! isset($invoice['submit'])) { 
    $delegation = $obm['delegation'];
  }
  if ($invoice['delegation'] != '') {
    $delegation = stripslashes($invoice['delegation']);
  }
  $dis_delegation = of_delegation_dis_search_form($delegation);

  $regions = of_category_get_ordered('', 'region');
  $region = $invoice['region'];
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  if ($invoice['dash_target'] == 'supplier') {
    $inout = '-';
  } else {
    $inout = '+';
  }
  $url_inout = urlencode($inout);

  $inv = run_query_invoice_amounts($date_ranges, 'invoice_date', $invoice['tasktype'], $inout, $region, $delegation);
  $url_prev = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_prev");
  $url_next = url_prepare("invoice_index.php?action=dashboard&amp;year=$year_next");

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice['tasktype'];
  if (! is_array($tt_ids) && ($tt_ids == '')) {
    $tt_ids = '0';
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, 'tt', $url_tt);

  // Table header
  for ($month = 1; $month < 13; $month++) {
    $label = $l_monthsofyear[$month - 1];
    $dis_row_head .= "
      <th>$label</th>";
  }

  // Billed infos -------------------------------------------------------------

  $status_pay = get_invoice_status('payment');

  // Loop through status (rows)
  foreach ($status_pay as $s_id => $s_label) {
    // Get the status list to query for total detail
    $total_status_param .= "&amp;sel_status[]=$s_id";
    
    $total_amount_ht = 0;
    $cpt++;
    $data = 'data' . ($cpt % 2);

    $dis_bil_row .= "<tr class=\"pair\"><th>$s_label</th>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice created
      if (is_array($inv["$year_month"]['billed']["$s_id"])) {
	$status_info = $inv["$year_month"]['billed']["$s_id"];
	$amount_ht = $status_info['amount_ht'];
	$nb = $status_info['nb'];
	$total_amount_ht += $amount_ht;
	$dis_amount_ht = number_format($amount_ht, 2, ',', ' ');
  $month_start = "$year_month-01";
	$month_end = date('Y-m-d', mktime(12,0,0,$month+1, 0, $year));
        $dis_bil_row .= "<td class=\"money\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;inout=$url_inout&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;sel_region=$region&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ht</a></td>";
      } else {
        $dis_bil_row .= '<td></td>';
      }
    }
    $f_total_amount_ht = number_format($total_amount_ht, 2, ',', ' ');
    $dis_bil_row .= "<td class=\"money\">&nbsp;$f_total_amount_ht</td></tr>";
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
    $status_info = $inv["$year_month"]['billed']['total'];
    $amount_ht = $status_info['amount_ht'];
    $total_amount_ht += $amount_ht;
    $dis_amount_ht = number_format($amount_ht, 2, ',', ' ');
    if ($amount_ht > 0) {
      $month_start = "$year_month-01";
      $month_end = date('Y-m-d', mktime(0,0,0,$month+1, 0, $year));
      $dis_bil_row_total .= "
          <td class=\"money\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search$total_status_param&amp;inout=$url_inout&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;sel_region=$region&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ht</a></td>";
    } else {
      $dis_bil_row_total .= "
          <td class=\"money\">&nbsp;$dis_amount_ht</td>";
    }
  }
  $f_total_amount_ht = number_format($total_amount_ht, 2, ',', ' ');
  $dis_bil_row_total .= "
      <td class=\"money\">&nbsp;$f_total_amount_ht</td>";

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $status_info = $inv["$year_month"]['billed']['total'];
    $amount_ht = $status_info['amount_ht'];
    $values[$month-1] = $amount_ht;
    $labels[$month-1] = number_format($amount_ht, 0, ',', ' ');
    $xlabels[$month-1] = $l_monthsofyear[$month-1];
    $values_pot[$month-1] = $amount_ht + $inv["$year_month"]['potential']['total']['amount_ht'];
    $labels_pot[$month-1] = number_format($values_pot[$month-1], 0, ',', ' ');
  }
  $title = "$l_billed $year";

  $chart_bil_params .= "&amp;title=".urlencode($title)."&amp;values=".urlencode(serialize($values)) . "&amp;labels=".urlencode(serialize($labels)). "&amp;xlabels=".urlencode(serialize($xlabels));

  // Potential chart ----------------------------------------------------------

  $status_toc = get_invoice_status('tocreate');

  // Loop through status (rows)
  foreach ($status_toc as $s_id => $s_label) { 
    $total_amount_ht = 0;
    $cpt++;
    $data = 'data' . ($cpt % 2);

    $dis_pot_row .= "<tr class=\"pair\"><th>$s_label</th>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice not created
      if (is_array($inv["$year_month"]['potential']["$s_id"])) {
	$status_info = $inv["$year_month"]['potential']["$s_id"];
	$amount_ht = $status_info['amount_ht'];
	$nb = $status_info['nb'];
	$total_amount_ht += $amount_ht;
	$f_amount_ht = number_format($amount_ht, 2, ',', ' ');
	$month_start = "$year_month-01";
	$month_end = date('Y-m-d', mktime(12,0,0,$month+1, 0, $year));
        $dis_pot_row .= "
          <td class=\"money\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;inout=$url_inout&amp;cba_archive=1&amp;tf_date_after=$month_start&amp;sel_region=$region&amp;tf_date_before=$month_end$url_sel_tt\">$f_amount_ht</a></td>";
      } else {
        $dis_pot_row .= '
          <td> </td>';
      }
      $labels[$month-1] = number_format($amount_ht, 0, ',', ' ');
    }
    $f_total_amount_ht = number_format($total_amount_ht, 2, ',', ' ');
    $dis_pot_row .= "<td class=\"money\">&nbsp;$f_total_amount_ht</td>
        </tr>";
  }
  $title = "$l_potential $year";
  $plots['new_bar'] = array(1, 0);
  $plots['legends'] = array($l_order_no_bill, $l_billed);
  $plots['values'] = array($values_pot, $values);
  $plots['labels'] = array($labels_pot);

  $chart_pot_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots))."&amp;xlabels=".urlencode(serialize($xlabels));

  // Payment infos ------------------------------------------------------------

  $date_ranges = array(array("$year-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, 'invoice_expiration_date', $invoice['taskttype'], $inout, $region, $delegation, true);

  $status_pay = get_invoice_status('payment');

  // Loop through status (rows)
  foreach ($status_pay as $s_id => $s_label) { 
    $total_amount_ttc = 0;
    $cpt++;
    $data = 'data' . ($cpt % 2);

    $dis_pay_row .= "<tr class=\"pair\"><th>$s_label</th>";

    // Loop through each month (cols)
    for ($month = 1; $month < 13; $month++) {

      // left pad the month
      if ($month > 9) {
	$year_month = "$year-$month";
      } else {
	$year_month = "$year-0$month";
      }
      // Invoice created
      if (is_array($inv["$year_month"]['billed']["$s_id"])) {
	$status_info = $inv["$year_month"]['billed']["$s_id"];
	$amount_ttc = $status_info['amount_ttc'];
	$nb = $status_info['nb'];
	$total_amount_ttc += $amount_ttc;
	$dis_amount_ttc = number_format($amount_ttc, 2, ',', ' ');
	$month_start = "$year_month-01";
	$month_end = date('Y-m-d', mktime(12,0,0,$month+1, 0, $year));
        $dis_pay_row .= "
          <td class=\"money\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;sel_status=$s_id&amp;inout=$url_inout&amp;cba_archive=1&amp;date_field=expiration_date&amp;tf_date_after=$month_start&amp;sel_region=$region&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ttc</a></td>";
      } else {
        $dis_pay_row .= '
          <td></td>';
      }
    }
    $f_total_amount_ttc = number_format($total_amount_ttc, 2, ',', ' ');
    $dis_pay_row .= "<td class=\"money\">&nbsp;$f_total_amount_ttc</td>
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
    $status_info = $inv["$year_month"]['billed']['total'];
    $amount_ttc = $status_info['amount_ttc'];
    $total_amount_ttc += $amount_ttc;
    $dis_amount_ttc = number_format($amount_ttc, 2, ',', ' ');
    if ($amount_ttc > 0) {
      $month_start = "$year_month-01";
      $month_end = date('Y-m-d', mktime(0,0,0,$month+1, 0, $year));
      $dis_pay_row_total .= "
        <td class=\"money\">&nbsp;<a href=\"$path/invoice/invoice_index.php?action=search&amp;inout=$url_inout&amp;cba_archive=1&amp;date_field=expiration_date&amp;tf_date_after=$month_start&amp;sel_region=$region&amp;tf_date_before=$month_end$url_sel_tt\">$dis_amount_ttc</a></td>";
    } else {
      $dis_pay_row_total .= "
        <td class=\"money\">&nbsp;$dis_amount_ttc</td>";
    }
  }
  $f_total_amount_ttc = number_format($total_amount_ttc, 2, ',', ' ');
  $dis_pay_row_total .= "
    <td class=\"money\">&nbsp;$f_total_amount_ttc</td>";

  // Artichow Chart
  for ($month = 1; $month < 13; $month++) {
    // left pad the month
    if ($month > 9) {
      $year_month = "$year-$month";
    } else {
      $year_month = "$year-0$month";
    }
    $xlabels[$month-1] = $l_monthsofyear[$month-1];
    $values[$month-1] = $inv["$year_month"]['paid']['total']['amount_ttc'];
    $values[$month-1] = $inv["$year_month"]['billed'][$cinv_status['paid']]['amount_ttc'];
    $values_pay[$month-1] = $inv["$year_month"]['billed']['total']['amount_ttc'];
    $labels[$month-1] = number_format($values_pay[$month-1], 0, ',', ' ');
  }
  $plots['new_bar'] = array(1, 0);
  $plots['legends'] = array($l_waiting, $l_paid);
  $title = "$l_payment $year ($l_amount_ttc)";
  $plots['values'] = array($values_pay, $values);
  $plots['labels'] = array($labels);

  $chart_pay_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots))."&amp;xlabels=".urlencode(serialize($xlabels));


  $block = "
  <form class=\"search\" method=\"post\" name=\"f_search\" action=\"$url_form\">
  <fieldset class=\"detail extra\">
  <legend>$l_year_detail</legend>
  <label>$l_date<br />
    <a href=\"javascript: return false;\"
       onclick=\"yearfield=window.document.getElementById('year');
                  newyear=Number(yearfield.innerHTML)-1;
                  yearfield.innerHTML=newyear;
                  document.forms.f_search.year.value=newyear;
                  return false;\">
    <img border=\"0\" src=\"$ico_previous\" alt=\"\" /></a>
    <span id=\"year\">$year</span>
    <a href=\"javascript: return false;\"
       onclick=\"yearfield=window.document.getElementById('year');
                  newyear=Number(yearfield.innerHTML)+1;
                  yearfield.innerHTML=newyear;
                  document.forms.f_search.year.value=newyear;
                  return false;\">
    <img border=\"0\" src=\"$ico_next\" alt=\"\" /></a>
  </label>
    $block_region
    $dis_delegation
    $block_tt
    &nbsp;<br />
  </fieldset>
  <fieldset class=\"buttons\">
    <input type=\"hidden\" name=\"year\" value=\"$year\" />
    <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
    <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
  </fieldset>
  </form>

  <div class=\"detail extra\">
  <h1>$l_order_no_bill ($l_amount_ht)</h1>
  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>$l_status</th>
    $dis_row_head
    <th>$l_total $year</th>
  </tr>
  </thead>
  <tbody>
  $dis_pot_row
  </tbody>
  </table>
  <p>&nbsp;</p>

  <h1>$l_billed ($l_amount_ht)</h1>
  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>$l_status</th>
    $dis_row_head
    <th>$l_total $year</th>
  </tr>
  </thead>
  <tbody>
  $dis_bil_row
  <tr class=\"highlight\">
    <th>$l_total</th>
    $dis_bil_row_total
  </tr>
  </tbody>
  </table>
  <p>&nbsp;</p> 

  <h1>$l_payment ($l_amount_ttc)</h1>
  <table class=\"spreadSheet\">
  <thead>
  <tr>
    <th>$l_status</th>
    $dis_row_head
    <th>$l_total $year</th>
  </tr>
  </thead>
  <tbody>
  $dis_pay_row
  <tr class=\"highlight\">
    <th>$l_total</th>
    $dis_pay_row_total
  </tr>
  </tbody>
  </table>
  <p>&nbsp;</p>
  </div>

  <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar_multiple$chart_pot_params\" alt=\"\" />
  <br />
  <img border=\"0\" src=\"$path/chart/chart_index.php?action=bar$chart_bil_params\" alt=\"\" />
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
  global $obm, $display, $path, $ctt_sales;
  global $l_status, $l_total, $l_invoice, $l_date, $l_amount_ht, $l_amount_ttc;
  global $l_billed, $l_order_no_bill, $l_potential, $l_monthsofyear;
  global $l_payment, $l_amount_ht, $l_amount_ttc, $l_paid, $l_waiting;
  global $l_execute,$ico_previous,$ico_next;

  if (isset($invoice['year'])) {
    $year = $invoice['year'];
  } else {
    $year = date('Y');
  }
  $year_prev = $year - 1;
  $year_next = $year + 1;

  if ($invoice['dash_target'] == 'supplier') {
    $inout = '-';
  } else {
    $inout = '+';
  }

  if (! isset($invoice['submit'])) { 
    $delegation = $obm['delegation'];
  }
  if ($invoice['delegation'] != '') {
    $delegation = stripslashes($invoice['delegation']);
  }
  $dis_delegation = of_delegation_dis_search_form($delegation);

  $regions = of_category_get_ordered('', 'region');
  $region = $invoice['region'];
  $block_region = of_category_dis_search_select('', 'region', $regions, $region);

  // If no tt selected set tt_ids to 0 for get_global_tasktype to return 0 lines
  $tt_ids = $invoice['tasktype'];
  if (! is_array($tt_ids) && ($tt_ids == '')) {
    $tt_ids = '0';
  } else {
    foreach ($tt_ids as $tt_id) {
      $url_sel_tt .= "&amp;sel_tt[]=$tt_id";
    }
  }

  $tts = get_global_tasktype($ctt_sales, $tt_ids);

  $url_tt = "$path/admin_ref/admin_ref_index.php?action=tt_ext_get_ids&amp;popup=1&amp;ext_element=sel_tt&amp;tt_target=$ctt_sales";
  $block_tt = of_select_dis_search_select($tts, 'tt', $url_tt);

  // Billed infos Year-1 and Year ---------------------------------------------

  $date_ranges = array(array("$year_prev-01-01", "$year-12-31"));
  $inv = run_query_invoice_amounts($date_ranges, 'invoice_date', $invoice['tasktype'], $inout, $region, $delegation);

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

    $status_info_prev = $inv["$year_month_prev"]['billed']['total'];
    $amount_ht = $status_info_prev['amount_ht'];
    $values_prev[$month-1] = $amount_ht;
    $labels_prev[$month-1] = number_format($amount_ht/1000, 0, ',', ' ');

    $status_info = $inv["$year_month"]['billed']['total'];
    $amount_ht = $status_info['amount_ht'];
    $values[$month-1] = $amount_ht;
    $labels[$month-1] = number_format($amount_ht/1000, 0, ',', ' ');

    $status_info_pot = $inv["$year_month"]['potential']['total'];
    $amount_ht_pot = $status_info_pot['amount_ht'] + $values[$month-1];
    $values_pot[$month-1] = $amount_ht_pot;
    $labels_pot[$month-1] = number_format($amount_ht_pot/1000, 0, ',', ' ');
  }
  $title = "$l_billed (k€) $year_prev / $year";
  $plots['new_bar'] = array(1, 1);
  $plots['legends'] = array("$year_prev", "$year");
  $plots['values'] = array($values_prev, $values);
  $plots['labels'] = array($labels_prev, $labels);

  $chart_ytoy_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots)) . "&amp;xlabels=".urlencode(serialize($xlabels));

  $title = "$l_potential (k€) $year_prev / $year";
  $plots['values'] = array($values_prev, $values_pot);
  $plots['labels'] = array($labels_prev, $labels_pot);

  $chart_ytoyp_params .= "&amp;title=".urlencode($title)."&amp;plots=".urlencode(serialize($plots)) . "&amp;xlabels=".urlencode(serialize($xlabels));

  $block = "
  <form class=\"search\" method=\"post\"  name=\"f_search\" action=\"$url_form\">
  <fieldset class=\"detail extra\">
  <legend>$l_year_to_year</legend>
  <label>$l_date<br />
    <a href=\"javascript: return false;\"
       onclick=\"yearfield=window.document.getElementById('year');
                  newyear=Number(yearfield.innerHTML)-1;
                  yearfield.innerHTML=newyear;
                  document.forms.f_search.year.value=newyear;
                  return false;\">
    <img border=\"0\" src=\"$ico_previous\" alt=\"\" /></a>
    <span id=\"year\">$year</span>
    <a href=\"javascript: return false;\"
       onclick=\"yearfield=window.document.getElementById('year');
                  newyear=Number(yearfield.innerHTML)+1;
                  yearfield.innerHTML=newyear;
                  document.forms.f_search.year.value=newyear;
                  return false;\">
    <img border=\"0\" src=\"$ico_next\" alt=\"\" /></a>
  </label>
    $block_region
    $dis_delegation
    $block_tt
    &nbsp;<br />
    <div class=\"buttons\">
      <input type=\"hidden\" name=\"year\" value=\"$year\" />
      <input name=\"action\" id=\"action\" type=\"hidden\" value=\"dashboard\" />
      <input name=\"submit\" type=\"submit\" value=\"$l_execute\" />
    </div>
  </div>
  </form>

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
 
  $dis_pref = new OBM_DISPLAY('PREFERENCES', $prefs, 'invoice');
  $dis_pref->pref_title = $l_invoice_display;
  $dis_pref->pref_dis_help = 1;

  // --- HTML Template --------------------------------------------------------

  $block = $dis_pref->display();

  return $block;
}

function dis_invoice_all_reminder($inv_q) {

	global $l_company,$l_expiration;
	$block="<table class=\"spreadSheet\">
<tr><th>$l_company</th>
<th>Nombre de relances</th>
<th>$l_expiration</th></tr>";

	while($inv_q->next_record()) {
		list( $year_o, $month_o, $day_o ) = explode( ',', date('Y,n,j', strtotime($inv_q->f('expiration_date'))));
		$date_expiration=date('d/m/y',mktime(0,0,0, $month_o, $day_o, $year_o));
	
		$block.='<tr><td>'.$inv_q->f('company_name').'</td>
		     <td><a href=invoice_index.php?action=reminder_company&amp;company_id='.$inv_q->f('company_id').'>'.$inv_q->f('nb_relance').' relance(s)</a></td> 
		     <td>'.$date_expiration.'</td>  </tr>';

	}
	return $block;
}

function dis_invoice_reminder_company($inv_q) {

	$i=0;
	$info = array($i=>array());

	while($inv_q->next_record()) {
	
		list( $year_o, $month_o, $day_o ) = explode( ',', date('Y,n,j', strtotime($inv_q->f('invoice_expiration_date'))));
		list( $year_c, $month_c, $day_c ) = explode( ',', date('Y,n,j'));
		$duration =  (mktime(0,0,0, $month_c, $day_c, $year_c) - mktime(0,0,0, $month_o, $day_o, $year_o));
		$duration_day = (int) ( $duration / (60 * 60 * 24));

		$info[$i]['invoice_number']=$inv_q->f('invoice_number');
        $info[$i]['invoice_id']=$inv_q->f('invoice_id');
		$info[$i]['invoice_date']=$inv_q->f('invoice_date');
		$info[$i]['invoice_expiration_date']=$inv_q->f('invoice_expiration_date');
		$info[$i]['invoice_amount_ttc']=$inv_q->f('invoice_amount_ttc');
		$info[$i]['invoice_label']=$inv_q->f('invoice_label');
		$info[$i]['invoice_comment']=$inv_q->f('invoice_comment');
		$info[$i]['company_name']=$inv_q->f('company_name');
		$info[$i]['company_id']=$inv_q->f('company_id');
		$info[$i]['company_comment']=$inv_q->f('company_comment');
	
		$i++;
		if($duration_day > 45)
		$type_odf = 2;
      	else
		$type_odf = 1;	
	} 

 	if($type_odf==1)
	$odf = new ODF('relance_1_multi.odt');
	else
	$odf = new ODF('relance_2_multi.odt');
	
	$res = $odf->export_reminder_by_company($info);
	return "";
}
?>
