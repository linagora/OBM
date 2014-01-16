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
// OBM - File : invoice_query.inc
//     - Desc : invoice query File
// 2001-07-30 - AliaSource
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Invoice search query :
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_search ($invoice) {
  global $cdg_sql, $c_all, $ctu_sql_limit;
  global $cgp_archive_only;
  $obm_q = new DB_OBM;

  $sql_order_dir = $invoice['sql_order_dir'];
  $sql_order_field = $invoice['sql_order_field'];

  $label = sql_search_text_parse($obm_q->escape($invoice['label']));
  $number = sql_search_text_parse($obm_q->escape($invoice['number']));
  $ht = $invoice['amount_ht'];
  $ttc = $invoice['amount_ttc'];
  $status = $invoice['status'];
  $sel_tt = $invoice['tasktype'];
  $date_field = ($invoice['date_field'] ? $invoice['date_field'] : 'date');
  $date_after = of_isodate_convert($invoice['date_after'],true);
  $date_before = of_isodate_convert($invoice['date_before'],true);
  $inout = $invoice['inout'];
  $credit_memo = $invoice['credit_memo'];
  $company = sql_search_text_parse($obm_q->escape($invoice['company']));
  $archive = $invoice['archive'];
  $comp_id = $invoice['company_id'];
  $deal_id = $invoice['deal_id'];
  $project_new_id = $invoice['project_id'];
  $region = $invoice['region'];
  $delegation = sql_search_text_parse($obm_q->escape($invoice['delegation']));

  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, 'invoice_date', 'invoice_date');
  $dateexp = sql_date_format($db_type, 'invoice_expiration_date', 'invoice_expiration_date');
  $datepay = sql_date_format($db_type, 'invoice_payment_date', 'invoice_payment_date');
  $multidomain = sql_multidomain('invoice');
	
  $where = "1=1 $multidomain";
  
  if ($label != '') {
    $where .= " AND invoice_label $like '%$label%'";
  }
  if ($number != '') {
    $where .= " AND invoice_number $like '%$number%'";
  }
  // If a delegation indication has been specified, get it
  if (trim($delegation) != '') {
    $where .= " AND userobm_delegation $like '$delegation%'";
    $join_user = "LEFT JOIN UserObm ON invoice_usercreate=userobm_id";
  }
  if (($region != $c_all) && ($region != '')) {
    $region = sql_parse_id($region, true);
    $where .= " AND deal_region_id $region"; 
    $join_deal = "LEFT OUTER JOIN Deal ON invoice_deal_id=deal_id";
  }
  if ($ht != '') {
    $where .= " AND invoice_amount_ht = '$ht'";
  }
  if ($ttc != '') {
    $where .= " AND invoice_amount_ttc = '$ttc'";
  }
  if (is_array($status) && (count($status) > 0)) {
    $where .= ' AND (';
    $or = '';
    foreach ($status as $s_id) {
      $s_id = sql_parse_int($s_id, true);
      $where .= "$or invoice_status_id = $s_id ";
      $or = ' OR';
    }
    $where .= ')';
  } else if (($status != '') && ($status != "$c_all")) {
    $status = sql_parse_int($status, true);
    $where .= " AND invoice_status_id = $status ";
  }
  if ($date_after != '') {
    $date_after = new Of_Date($date_after);
    $where .= " AND invoice_$date_field >= '".$date_after->getDateIso()."'";
  }
  if ($date_before != '') {
    $date_before = new Of_Date($date_before);
    $where .= " AND invoice_$date_field <= '".$date_before->getDateIso()."'";
  }
  if (($inout != '') && ($inout != "$c_all")) {
    $where .= " AND invoice_inout = '$inout'";
  }
  if (($credit_memo != '') && ($credit_memo != "$c_all")) {
    $where .= " AND invoice_credit_memo = '$credit_memo'";
  }
  if ($comp_id != '') {
    $comp_id = sql_parse_id($comp_id, true);
    $where .= " AND invoice_company_id $comp_id ";
  }
  if ($company != '') {
    $where .= sql_global_company_name_advanced_search($company, $like);
    $join_comp = "LEFT JOIN Company ON invoice_company_id=company_id";
  }
  if ($deal_id != '') {
    $deal_id = sql_parse_id($deal_id, true);
    $where .= " AND (invoice_deal_id $deal_id 
                     OR project_deal_id $deal_id )";
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
  }
  if ($project_new_id != '') {
    $project_new_id = sql_parse_id($project_new_id, true);
    $where .= " AND invoice_project_id $project_new_id ";
  }
  if (is_array($sel_tt)) {
    $where .= ' AND (';
    $or = '';
    foreach ($sel_tt as $tt_id) {
      $tt_id = sql_parse_id($tt_id, true);
      $where .= "$or project_tasktype_id $tt_id OR deal_tasktype_id $tt_id ";
      $or = ' OR';
    }
    $where .= ')';
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
    $join_deal = "LEFT JOIN Deal ON invoice_deal_id=deal_id";
  }
  if ($archive != '1') {
    $where .= " AND invoice_archive = 0";
  } elseif($cgp_archive_only) {
    $where .= " AND invoice_archive = 1";
  }
  $whereq = "WHERE $where";

  if ($sql_order_field == 'invoice_status') {
    $sql_order_field = 'invoice_status_id';
  }
  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'invoice_date';
  $orderq .= "ORDER BY $order $sql_order_dir";

  // XXXXXX ???? Display archive
  $query = "SELECT 
      invoice_id,
      invoice_id as id,
      invoice_domain_id,
      invoice_number,
      invoice_label,
      invoice_amount_ttc,
      invoice_amount_ht,
      invoice_archive,
      invoice_inout,
      invoice_credit_memo,
      invoice_status_id,
      $date,
      $dateexp,
      $datepay,
      company_id,
      company_name as invoice_company,
      deal_id,
      deal_label as invoice_deal,
      project_id,
      project_name as invoice_project
    FROM Invoice
      LEFT OUTER JOIN Company ON invoice_company_id = company_id
      LEFT OUTER JOIN Deal ON invoice_deal_id = deal_id
      LEFT JOIN Project on invoice_project_id=project_id
      $join_user
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Invoice $join_comp $join_deal $join_project $join_user $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg ($query, $cdg_sql, 'run_query_invoice_search()'); 
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice: Select query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_detail($p_invoice_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $date = sql_date_format($db_type, 'Invoice.invoice_date', 'date');
  $expiration_date = sql_date_format($db_type, 'Invoice.invoice_expiration_date', 'expiration_date');
  $payment_date = sql_date_format($db_type, 'Invoice.invoice_payment_date', 'payment_date');
  $datemodif = sql_date_format($db_type, 'invoice_timeupdate', 'datemodif');
  $timeupdate = sql_date_format($db_type, 'invoice_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'invoice_timecreate', 'timecreate');
  $multidomain = sql_multidomain('invoice');
  $p_invoice_id = sql_parse_id($p_invoice_id, true);	
  // XXXXX ???? exporter le calcul des paiements a par (pour eviter group by)
  $query = "SELECT Invoice.*,
      $date,
      $expiration_date,
      $payment_date,
      $datemodif,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      company_name,
      deal_label,
      project_name,
      sum(paymentinvoice_amount) as invoice_paid
    FROM Invoice
      LEFT JOIN Company ON invoice_company_id=company_id
      LEFT JOIN Deal ON invoice_deal_id=deal_id
      LEFT JOIN Project ON invoice_project_id=project_id
      LEFT OUTER join PaymentInvoice ON invoice_id=paymentinvoice_invoice_id
      LEFT JOIN UserObm as c ON invoice_usercreate=c.userobm_id
      LEFT JOIN UserObm as u ON invoice_userupdate=u.userobm_id
    WHERE invoice_id $p_invoice_id $multidomain
    GROUP BY Invoice.invoice_domain_id, invoice_id, invoice_timeupdate, invoice_timecreate,
      invoice_userupdate, invoice_usercreate, invoice_company_id,
      invoice_deal_id, invoice_project_id, invoice_number, invoice_label,
      invoice_amount_ht, invoice_amount_ttc, invoice_status_id, invoice_date,
      invoice_expiration_date, invoice_payment_date, invoice_comment,
      invoice_inout, invoice_credit_memo, invoice_archive,
      c.userobm_login, u.userobm_login, company_name, deal_label, project_name";

  display_debug_msg ($query, $cdg_sql, 'run_query_invoice_detail()'); 
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_insert($invoice) {
  global $cdg_sql, $obm;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id($obm['domain_id']);
	
  $label = $invoice['label'];
  $number = $invoice['number'];
  $ht = $invoice['amount_ht'];
  $ttc = $invoice['amount_ttc'];
  $status = sql_parse_int($invoice['status']);
  $date = ($invoice['date'] ? "'".of_isodate_convert($invoice['date'])."'" : 'null');
  $expiration_date = ($invoice['expiration_date'] ? "'".of_isodate_convert($invoice['expiration_date'])."'" : 'null');
  $payment_date = ($invoice['payment_date'] ? "'".of_isodate_convert($invoice['payment_date'])."'" : 'null');
  $inout = $invoice['inout'];
  $credit_memo = $invoice['credit_memo'];
  $add_comment = $invoice['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($invoice['datecomment']);
    $usercomment = $invoice['usercomment'];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice['company_new_id'];
  if ($comp_id < 1) { $comp_id = $invoice['company_id']; }
  if ($comp_id == '') { $comp_id = '0'; }
  $comp_id = sql_parse_id($comp_id);
  $deal_id = $invoice['deal_new_id'];
  if ($deal_id < 1) { $deal_id = $invoice['deal_id']; }
  if ($deal_id == '') { $deal_id = '0'; }
  $deal_id = sql_parse_id($deal_id);
  $project_new_id = $invoice['project_new_id'];
  if ($project_new_id < 1) { $project_new_id = $invoice['project_id']; }
  if ($project_new_id == '') { $project_new_id = '0'; }
  $project_new_id = sql_parse_id($project_new_id);
  $arch = sql_parse_int($invoice['archive']);

  $query = "INSERT INTO Invoice (
      invoice_domain_id,
      invoice_timeupdate,
      invoice_timecreate,
      invoice_userupdate,
      invoice_usercreate,
      invoice_company_id,
      invoice_deal_id,
      invoice_project_id,
      invoice_number,
      invoice_label,
      invoice_amount_ht,
      invoice_amount_ttc,
      invoice_status_id,
      invoice_comment,
      invoice_date,
      invoice_expiration_date,
      invoice_payment_date,
      invoice_inout,
      invoice_credit_memo,
      invoice_archive)
    VALUES (
     $domain_id,
      null,
     NOW(),
     null,
     $uid,
     $comp_id,
     $deal_id,
     $project_new_id,
     '".$obm_q->escape($number)."',
     '".$obm_q->escape($label)."',
     '".$obm_q->escape($ht)."', 
     '".$obm_q->escape($ttc)."',
     $status,
     '".$obm_q->escape($comment)."',
     $date,
     $expiration_date,
     $payment_date,
     '".$obm_q->escape($inout)."',
     '".$obm_q->escape($credit_memo)."',
     $arch)"; 

  display_debug_msg($query, $cdg_sql, 'run_query_invoice_insert()');
  $ret = $obm_q->query($query);
  $id = $obm_q->lastid();
  $entity_id = of_entity_insert($GLOBALS['module'], $id);  

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// invoice update 
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_update($invoice) {
  global $obm, $cdg_sql;
  $obm_q = new DB_OBM;

  $uid = sql_parse_id($obm['uid']);

  $id = sql_parse_id($invoice['invoice_id'], true);
  $label = $invoice['label'];
  $number = $invoice['number'];
  $arch = sql_parse_int($invoice['archive']);
  $ht = $invoice['amount_ht'];
  $ttc = $invoice['amount_ttc'];
  $status = sql_parse_int($invoice['status']);
  $date = ($invoice['date'] ? "'".of_isodate_convert($invoice['date'])."'" : 'null');
  $expiration_date = ($invoice['expiration_date'] ? "'".of_isodate_convert($invoice['expiration_date'])."'" : 'null');
  $payment_date = ($invoice['payment_date'] ? "'".of_isodate_convert($invoice['payment_date'])."'" : 'null');
  $inout = $invoice['inout'];
  $credit_memo = $invoice['credit_memo'];
  $comment = $invoice['comment'];
  $add_comment = $invoice['add_comment'];
  if ($add_comment != '') {
    $datecomment = of_isodate_convert($invoice['datecomment']);
    $usercomment = $invoice['usercomment'];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice['company_new_id'];
  if ($comp_id < 1) { $comp_id = $invoice['company_id']; }
  if ($comp_id == '') { $comp_id = '0'; }
  $comp_id = sql_parse_id($comp_id);
  $deal_id = $invoice['deal_new_id'];
  if ($deal_id < 1) { $deal_id = $invoice['deal_id']; }
  if ($deal_id == '') { $deal_id = '0'; }
  $deal_id = sql_parse_id($deal_id);
  $project_new_id = $invoice['project_new_id'];
  if ($project_new_id < 1) { $project_new_id = $invoice['project_id']; }
  if ($project_new_id == '') { $project_new_id = '0'; }
  $project_new_id = sql_parse_id($project_new_id);
  $multidomain = sql_multidomain('invoice');

  $query = "UPDATE Invoice SET
      invoice_timeupdate=NOW(),
      invoice_userupdate=$uid,
      invoice_company_id=$comp_id,
      invoice_deal_id=$deal_id,
      invoice_project_id=$project_new_id,
      invoice_number='".$obm_q->escape($number)."',
      invoice_label='".$obm_q->escape($label)."',
      invoice_amount_ht='".$obm_q->escape($ht)."',
      invoice_amount_ttc='".$obm_q->escape($ttc)."',
      invoice_status_id=$status,
      invoice_comment='".$obm_q->escape($comment)."',
      invoice_date=$date,
      invoice_expiration_date=$expiration_date,
      invoice_payment_date=$payment_date,
      invoice_inout='".$obm_q->escape($inout)."',
      invoice_credit_memo='".$obm_q->escape($credit_memo)."',
      invoice_archive='".$obm_q->escape($arch)."'
    WHERE invoice_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_invoice_update()');
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get invoice status list with conditions
// Parameters:
//   - $target : 'payment' : get status where invoice should have a payment
//             : 'tocreate' : get status where invoice has not been creatted
//             : 'dashboard' : valid invoices (remove cancelled,...)
//             : a number  : get status where status id = this number
// Returns:
//   - $sta : array of status [id] [label]
///////////////////////////////////////////////////////////////////////////////
function get_invoice_status($target='') {
  global $cdg_sql, $cinv_status;

  $sta = array();

  if ($target == 'payment') {
    $status_target['sent'] = $cinv_status['sent'];
    $status_target['factor'] = $cinv_status['factor'];
    $status_target['ppaid'] = $cinv_status['ppaid'];
    $status_target['paid'] = $cinv_status['paid'];
    $status_target['conflict'] = $cinv_status['conflict'];
  } else if ($target == 'tocreate') {
    $status_target['tocreate'] = $cinv_status['tocreate'];
  } else if ($target == 'dashboard') {
    $status_target['tocreate'] = $cinv_status['tocreate'];
    $status_target['sent'] = $cinv_status['sent'];
    $status_target['factor'] = $cinv_status['factor'];
    $status_target['ppaid'] = $cinv_status['ppaid'];
    $status_target['paid'] = $cinv_status['paid'];
    $status_target['conflict'] = $cinv_status['conflict'];
  } else if (is_numeric($target)) {
    foreach ($cinv_status as $label => $id) {
      if ($target == $id) {
	$status_target[$label] = $cinv_status[$label];
	break;
      }
    }
  } else if ($target != '') {
    $status_target[$target] = $cinv_status[$target];
  } else {
    $status_target = $cinv_status;
  }

  foreach ($status_target as $status => $s_id) {
    $s_label = "l_invoicestatus_$status";
    global $$s_label;
    $sta[$s_id] = $$s_label;
  }

  return $sta;
}


///////////////////////////////////////////////////////////////////////////////
// getting all payments of invoice $p_invoice 
// if $not_paid = 1, we search only for not paid payments,
// ie. lines having paymentinvoice_amount = 0.0
// if $not_paid == -1, we return only paid payments
// ie. lines having paymetinvoice_amount != 0.0
///////////////////////////////////////////////////////////////////////////////
function get_invoice_payment($p_invoice, $not_paid=0) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, 'payment_date', 'payment_date');
  $multidomain = sql_multidomain('payment');
  $p_invoice = sql_parse_id($p_invoice, true);
  $query = "SELECT
      paymentinvoice_amount,
      payment_id,
      payment_inout,
      payment_number,
      $date,
      payment_amount,
      company_name
    FROM PaymentInvoice
      LEFT JOIN Payment ON paymentinvoice_payment_id=payment_id
      LEFT JOIN Company ON payment_company_id=company_id
    WHERE paymentinvoice_invoice_id $p_invoice
      $multidomain";

  if ($not_paid == 1) {
    $query .= " AND paymentinvoice_amount = 0.0 ";
  } elseif ($not_paid == -1) {
    $query .= " AND paymentinvoice_amount <> 0.0 ";
  }
  
  display_debug_msg($query, $cdg_sql, 'get_invoice_payment()');
  $obm_q->query($query);
  $p = array();

  while ($obm_q->next_record()) {
    $id = $obm_q->f('payment_id');
    $number = $obm_q->f('payment_number');
    $company = $obm_q->f('company_name');
    $date = of_date_format($obm_q->f('payment_date'));
    $amount = $obm_q->f('payment_amount');
    $inout = $obm_q->f('payment_inout');
    $affected_amount = $obm_q->f('paymentinvoice_amount');
    $p[$id] = array ('id' => $id,
		     'date' => $date,
		     'number' => $number,
		     'company' => $company,
		     'amount' => $amount,
		     'inout' => $inout,
		     'affected_amount' => $affected_amount);
    $p['total']['amount'] += $amount;
    $p['total']['affected_amount'] += $affected_amount;
  }

  return $p;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice Deletion query execution
// Parameters:
//   - $p_id : invoice id
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_delete($p_id) {
  global $cdg_sql;
  of_entity_delete($GLOBALS['module'], $p_id);
  $id = sql_parse_id($p_id, true); 
  $multidomain = sql_multidomain('invoice');
  if ($retour) {
    run_query_global_delete_document_links($p_id, 'invoice');    
  }
  $query = "DELETE FROM Invoice 
		WHERE invoice_id $id $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_invoice_delete()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);


  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the payments attached to the invoice
// Parameters:
//   - $p_id : invoice Id
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_invoice_payment($p_id) {
  global $cdg_sql;
  $p_id = sql_parse_id($p_id, true);
  $multidomain = sql_multidomain('payment');
  $query = "SELECT distinct payment_id 
    FROM Payment
      LEFT JOIN PaymentInvoice ON payment_id=paymentinvoice_payment_id
    WHERE paymentinvoice_invoice_id $p_id $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// returns the value of the inout attribute of the given deal
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_deal_get_inout($p_deal_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('deal');
  $p_deal_id = sql_parse_id($p_deal_id, true);
  $query = "SELECT dealtype_inout
    FROM Deal, DealType
    WHERE deal_id $p_deal_id 
      AND deal_type_id = dealtype_id $multidomain";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f('dealtype_inout');
}


///////////////////////////////////////////////////////////////////////////////
// Check if an invoice number is already used
// Parameters:
//   - $number : invoice number to check
//   - $id     : invoice id to exclude
// Returns: $nb used
///////////////////////////////////////////////////////////////////////////////
function get_invoice_nb_having_number ($number, $id='') {
  global $cdg_sql;
  $obm_q = new DB_OBM;

  if ($id != '') {
    $id = sql_parse_id($id, true, true);
    $where_id = "AND invoice_id $id ";
  }
	
  $multidomain = sql_multidomain('invoice');

  $query = "SELECT invoice_id
    FROM Invoice
    WHERE invoice_number = '".$obm_q->escape($number)."'
      $where_id $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $nb = $obm_q->num_rows();

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get global invoices infos
// Parameters:
//   - $date_ranges : array [range1] : [date_start, date_end]
//   - $target_date : invoice_date, invoice_expiration_date : date to sort
//   - $sel_tt      : tasktype array filter
//   - $inout       : (+ || -) to get client or supplier invoices
//   - $region_id   : region_id of the deal linked to the invoices
//   - $delegation  : delegation of the user that has created the invoices
//   - $partial_pay : [optionnal] (true || false) get payments for ppaid status
// Returns:
//   $res : array[date]([billed]|[paid]|[potential])([status]|[total])[$infos]
//            $infos = label, amount_ht, amount_ttc, nb
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_amounts($date_ranges, $target_date='invoice_date', $sel_tt='', $inout='+', $region_id='', $delegation='', $partial_pay=false) {
  global $cdg_sql, $cdg_param, $cinv_status;
  global $l_total;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $da = sql_date_format($db_type, 'invoice_date', 'date');
  $de = sql_date_format($db_type, 'invoice_expiration_date', 'expiration_date');
  $delegation = sql_search_text_parse($delegation);
  $multidomain = sql_multidomain('invoice');
	
  if (is_array($date_ranges)) {
    $w_date = 'AND ';
    foreach ($date_ranges as $range) {
      $d1 = $range[0];
      $d2 = $range[1];
      $w_date .= " $or ($target_date >= '".$obm_q->escape($d1)."' AND $target_date <= '".$obm_q->escape($d2)."')";
      $or = 'OR';
    }
  }

  // Region filter
  if (($region_id != $c_all) && ($region_id > 0)) {
    $where_region = " AND deal_region_id=$region_id";
    $join_deal = "LEFT JOIN Deal ON invoice_deal_id=deal_id";
  }

  // Delegation filter
  if (trim($delegation) != '') {
    $where_delegation = " AND userobm_delegation $like '".$obm_q->escape($delegation)."%'";
    $join_user = "LEFT JOIN UserObm ON invoice_usercreate=userobm_id";
  }

  // Tasktype filter
  if (is_array($sel_tt)) {
    $w_tt .= ' AND (';
    $or = '';
    foreach ($sel_tt as $tt_id) {
      $w_tt .= "$or project_tasktype_id='$tt_id' OR deal_tasktype_id='$tt_id'";
      $or = ' OR';
    }
    $w_tt .= ')';
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
    $join_deal = "LEFT JOIN Deal ON invoice_deal_id=deal_id";
  }

  // Status filter Billed (status payment) OR potential (status created = 0)
  $status = get_invoice_status('dashboard');
  if (is_array($status)) {
    $w_st .= ' AND (';
    $or = '';
    foreach ($status as $s_id=>$s_label) {
      $w_st .= "$or invoice_status_id='".$obm_q->escape($s_id)."'";
      $or = ' OR';
    }
    $w_st .= ')';
  }

  $query = "SELECT
      invoice_id,
      $da,
      $de,
      invoice_amount_ht,
      invoice_amount_ttc,
      invoice_inout,
      invoice_credit_memo,
      invoice_status_id
    FROM Invoice
      $join_project
      $join_deal
      $join_user
    WHERE invoice_inout = '$inout' 
      $w_st
      $w_date
      $where_region
      $where_delegation
      $w_tt 
      $multidomain
    ORDER BY invoice_status_id
";
  
  display_debug_msg($query, $cdg_sql, 'run_query_invoice_amounts()');
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $id = $obm_q->f('invoice_id');
    $ah = $obm_q->f('invoice_amount_ht');
    $at = $obm_q->f('invoice_amount_ttc');
    $inout = $obm_q->f('invoice_inout');
    $credit = $obm_q->f('invoice_credit_memo');
    $date = of_isodate_format($obm_q->f('date'));
    $exp_date = of_isodate_format($obm_q->f('expiration_date'));
    if ($target_date == 'invoice_expiration_date') {
      $month = substr($exp_date, 0, 7);
    } else {
      $month = substr($date, 0, 7);
    }
    $status = $obm_q->f('invoice_status_id');
    $payment = ($status == $cinv_status['tocreate'] ? 0 : 1);

    // Invoice created
    if ($payment == '1') {
      // Case invoice is a credit memo
      if ($credit != '1') {
	$res[$month]['billed']["$status"]['amount_ht'] += $ah;
	$res[$month]['billed']["$status"]['amount_ttc'] += $at;
	$res[$month]['billed']['total']['amount_ht'] += $ah;
	$res[$month]['billed']['total']['amount_ttc'] += $at;
      } else {
	$res[$month]['billed']["$status"]['amount_ht'] -= $ah;
	$res[$month]['billed']["$status"]['amount_ttc'] -= $at;
	$res[$month]['billed']['total']['amount_ht'] -= $ah;
	$res[$month]['billed']['total']['amount_ttc'] -= $at;
      }
      $res[$month]['billed']["$status"]['nb'] ++;
      $res[$month]['billed']['total']['label'] = $l_total;
      $res[$month]['billed']['total']['nb'] ++;

      if ($partial_pay) {
        // Handle partially paid distribution for payment chart
        // Part paid set to 'paid'
        // Part not paid paid set to 'ppaid'
        if (($status == $cinv_status['ppaid']) || ($status == $cinv_status['factor'])) {
          $pays = get_invoice_payment($id);
          $one_pay = $pays['total'];
          if ($credit != '1') {
            // cancel the amount just added 
            $res[$month]['billed'][$cinv_status['ppaid']]['amount_ht'] -= $ah;
            $res[$month]['billed'][$cinv_status['ppaid']]['amount_ttc'] -= $at;
            // set the true partial payment done
            $res[$month]['billed'][$cinv_status['paid']]['amount_ttc'] += $one_pay['affected_amount'];
            // set the payment remaining value
            $res[$month]['billed'][$cinv_status['ppaid']]['amount_ttc'] += $at - $one_pay['affected_amount'];
          }  
        }
      }

    } else {
      // Invoice to create
      // Case invoice is a credit memo
      if ($credit != '1') {
	$res[$month]['potential']["$status"]['amount_ht'] += $ah;
	$res[$month]['potential']["$status"]['amount_ttc'] += $at;
	$res[$month]['potential']['total']['amount_ht'] += $ah;
	$res[$month]['potential']['total']['amount_ttc'] += $at;
      } else {
	$res[$month]['potential']["$status"]['amount_ht'] -= $ah;
	$res[$month]['potential']["$status"]['amount_ttc'] -= $at;
	$res[$month]['potential']['total']['amount_ht'] -= $ah;
	$res[$month]['potential']['total']['amount_ttc'] -= $at;
      }
      $res[$month]['potential']["$status"]['nb'] ++;
      $res[$month]['potential']['total']['label'] = $l_total;
      $res[$month]['potential']['total']['nb'] ++;
    }
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice Form Data checking and formatting
// Parameters:
//   - $id        : invoice id  (empty on insertion)
//   - $invoice[] : values checked
//     keys used  : label, number, inout, company_id, company_new_id
///////////////////////////////////////////////////////////////////////////////
function check_invoice_data_form($id, $invoice) {
  global $php_regexp_isodate;
  global $l_fill_label, $l_fill_company, $l_fill_number, $l_error_number_exist;
  global $l_fill_inout, $l_fill_amount, $l_fill_date, $l_date, $l_invalid_date;
  global $l_expiration_date, $l_payment_date;
  global $l_err_paid_status, $l_err_ppaid_status, $l_err_no_payment_status;
  global $err, $l_exist_error, $cinv_status, $l_err_status_received_customer;

  $label = $invoice['label'];
  $number = $invoice['number'];
  $date = of_isodate_convert($invoice['date'],true);
  $expiration_date = of_isodate_convert($invoice['expiration_date'],true);
  $payment_date = of_isodate_convert($invoice['payment_date'],true);
  $inout = $invoice['inout'];
  $ht = $invoice['amount_ht'];
  $ttc = $invoice['amount_ttc'];
  $c_id = $invoice['company_id'];
  $c_new_id = $invoice['company_new_id'];
  $status = $invoice['status'];
  if ($status == $cinv_status['tocreate']) {
    $status_created = false;
  } else {
    $status_created = true;
  }
	
  // MANDATORY: Invoice label
  if (trim($label) == '') {
    $err['msg'] = $l_fill_label;
    $err['field'] = 'label';
    return false;
  }

  // MANDATORY: Invoice inout must be set
  if (($inout != '+') && ($inout != '-')) {
    $err['msg'] = $l_fill_inout;
    $err['field'] = 'inout';
    return false;
  }

  // Date check
  if ($date == '') {
    // If invoice in a status 'created', exp date must be set
    if ($status_created) {
      $err['msg'] = $l_fill_date;
      $err['field'] = 'date';
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $date) == 0) {
    $err['msg'] = "$l_date : $date : $l_invalid_date"; 
    $err['field'] = 'date';
   return false;
  }

  // Expiration Date check
  if ($expiration_date == '') {
    // If invoice in a status 'created', exp date must be set
    if ($status_created) {
      $err['msg'] = "$l_expiration_date : $l_fill_date";
      $err['field'] = 'expiration_date';
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $expiration_date) == 0) {
    $err['msg'] = "$l_expiration_date : $expiration_date : $l_invalid_date"; 
    $err['field'] = 'expiration_date';
    return false;
  }

  // Payment Date check
  if (($payment_date != '') && (preg_match($php_regexp_isodate, $payment_date) == 0)) {
    $err['msg'] = "$l_payment_date : $payment_date : $l_invalid_date"; 
    $err['field'] = 'payment_date';
    return false;
  }

  // MANDATORY: a company must be set
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err['msg'] = $l_fill_company;
    $err['field'] = 'company';
    return false;
  }

  // MANDATORY: Amount must be set
  if ( !($ht > 0) || !($ttc > 0)) {
    $err['msg'] = $l_fill_amount;
    $err['field'] = 'amount_ht';
    return false;
  }

  // MANDATORY: Invoice number, if state not 'to be created'
  if ($status_created) {
    if (trim($number) == '') {
      $err['msg'] = $l_fill_number;
      $err['field'] = 'number';
      return false;
    } else {
      $nb = get_invoice_nb_having_number($number, $id);
      if ($nb > 0) {
	$err['msg'] = $l_error_number_exist;
	$err['field'] = 'number';
	return false;
      }
    }
  }

  // Status if invoice has payment
  $pays = get_invoice_payment($id);
  $total_pay_amount = $pays['total']['affected_amount'];

  // If invoice has been paid, status must be 'paid'
  if (($total_pay_amount == $ttc) && ($status != $cinv_status['paid'])) {
    $err['msg'] = $l_err_paid_status;
    $err['field'] = 'status';
    return false;
  }
  // If invoice partially paid, status must be 'ppaid', 'conflict' or 'factor'
  if (($total_pay_amount != $ttc) && ($total_pay_amount > 0)
      && ! (($status == $cinv_status['ppaid'])
	    || ($status == $cinv_status['conflict'])
	    || ($status == $cinv_status['factor']))) {
    $err['msg'] = $l_err_ppaid_status;
    $err['field'] = 'status';
    return false;
  }

  // If invoice has no payments, status must not be 'ppaid', 'paid'
  if (($total_pay_amount == 0)
      && (($status == $cinv_status['ppaid'])
	  || ($status == $cinv_status['paid']))) {
    $err['msg'] = $l_err_no_payment_status;
    $err['field'] = 'status';
    return false;
  }

  // If status is 'received', type must be supplier
  if (($status == $cinv_status['received']) && ($inout == '+')) {
    $err['field'] = 'status';
    $err['msg'] = $l_err_status_received_customer;
    return false;
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the invoice can be deleted
// Parameters:
//   - $p_id : invoice id
// Returns:
//   true if the invoice can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_invoice($p_id) {
  global $err, $ok_msg;
  global $l_link_payment, $l_link_payment_no;

  $delete_ok = true;

  // Links from Payment
  $obm_q = run_query_linked_invoice_payment($p_id);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $err['msg'] .= "$l_link_payment";
  } else {
    $ok_msg .= "$l_link_payment_no";
  }

  return $delete_ok;
}

///////////////////////////////////////////////////////////////////////////////
//
// Parameters:
//   - $p_id : invoice id
// Returns:
//
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_account($p_id) {
  $obm_q = new DB_OBM;

  $query = 'SELECT account_id, account_label FROM Account';
  $obm_q->query($query);

  return $obm_q;
}

function get_invoice_export_infos($id, $account) {
  $obm_q = new DB_OBM;

  $query = '
    SELECT    invoice_label, 
	          invoice_amount_ttc,
              account_number,
              account_bank,
              account_comment,
              invoice_number,
              invoice_date,
              invoice_amount_ht,
              invoice_comment,
              project_shortname,
              invoice_expiration_date,
              company_name,
              tasktype_label as dealtype_label,
              contact_company,
              contact_service,
              contact_lastname,
              contact_firstname,
              address_street as contact_address1,
              \'\' as contact_address2,
              address_zipcode as contact_zipcode,
              address_town as contact_town,
              address_expresspostal as contact_expresspostal  
    FROM      Account,
              Invoice
    LEFT JOIN Project   ON project_id      = invoice_project_id
    LEFT JOIN TaskType  ON project_tasktype_id = tasktype_id
    LEFT JOIN Company   ON company_id      = invoice_company_id,
              Deal
    LEFT JOIN Contact   ON contact_id      = deal_contact2_id
    LEFT JOIN ContactEntity on contactentity_contact_id=deal_contact2_id
    LEFT JOIN Address ON address_entity_id = contactentity_entity_id 
    WHERE     invoice_id = ' . $obm_q->escape($id) . '
    AND       account_id = ' . $obm_q->escape($account) . '
    AND       deal_id    = invoice_deal_id';

    $obm_q->query($query);
    $obm_q->next_record();

    return $obm_q;
}
function get_invoice_export_reminder_infos($id) {
  $obm_q = new DB_OBM;

  $query = '
    SELECT    invoice_label, 
	      invoice_amount_ttc,
	      account_number,
              account_bank,
              account_comment,
              invoice_number,
              invoice_date,
              invoice_amount_ht,
              invoice_comment,
              project_shortname,
              invoice_expiration_date,
              company_name,
              tasktype_label as dealtype_label,
              contact_company,
              contact_service,
              contact_lastname,
              contact_firstname,
              address_street as contact_address1,
              \'\' as contact_address2,
              address_zipcode as contact_zipcode,
              address_town as contact_town,
              address_expresspostal as contact_expresspostal  
    FROM      Account,
              Invoice
    LEFT JOIN Project   ON project_id      = invoice_project_id
    LEFT JOIN TaskType  ON project_tasktype_id = tasktype_id
    LEFT JOIN Company   ON company_id      = invoice_company_id,
              Deal
    LEFT JOIN Contact   ON contact_id      = deal_contact2_id
    LEFT JOIN ContactEntity on contactentity_contact_id=deal_contact2_id
    LEFT JOIN Address ON address_entity_id = contactentity_entity_id 
    WHERE     invoice_id = ' . $obm_q->escape($id) . '';
	
    $obm_q->query($query);
    $obm_q->next_record();

    return $obm_q;

}

function all_reminder() {

	$obm_q = new DB_OBM;
	$query='
	SELECT distinct(company_name),company_id,invoice_id,invoice_number,count(invoice_expiration_date)as nb_relance, MIN(invoice_expiration_date) as expiration_date
	from Company, Invoice
	where invoice_company_id = company_id
	and invoice_status_id = 1
	and DATEDIFF(current_date,invoice_expiration_date) > 15
	group by company_name';
	
    	$obm_q->query($query);
    
    	return $obm_q;
	
}

function reminder_by_company($company_id) {

	$obm_q = new DB_OBM;
	$query='
	select  distinct(invoice_id),
		company_name,
		company_id, 
		invoice_expiration_date, 
		invoice_number, 
		invoice_date,
		company_comment, 
		invoice_label,
		invoice_amount_ttc,
		invoice_comment
	from Company, Invoice,Contact
	where invoice_company_id = company_id
        and contact_company_id = company_id
	and invoice_status_id = 1
	and DATEDIFF(current_date,invoice_expiration_date) > 15
	and company_id ='.$obm_q->escape($company_id).'
	order by invoice_expiration_date desc';
	
    $obm_q->query($query);
    
    return $obm_q;
	
}

?>
