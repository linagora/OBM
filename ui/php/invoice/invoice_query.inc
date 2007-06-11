<?php
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

  $sql_order_dir = $invoice["sql_order_dir"];
  $sql_order_field = $invoice["sql_order_field"];

  $label = sql_search_text_parse($invoice["label"]);
  $number = sql_search_text_parse($invoice["number"]);
  $ht = $invoice["amount_ht"];
  $ttc = $invoice["amount_ttc"];
  $status = $invoice["status"];
  $sel_tt = $invoice["tasktype"];
  $date_field = ($invoice["date_field"] ? $invoice["date_field"] : "date");
  $date_after = of_isodate_convert($invoice["date_after"],true);
  $date_before = of_isodate_convert($invoice["date_before"],true);
  $inout = $invoice["inout"];
  $credit_memo = $invoice['credit_memo'];
  $company = sql_search_text_parse($invoice["company"]);
  $archive = $invoice["archive"];
  $comp_id = $invoice["company_id"];
  $deal_id = $invoice["deal_id"];
  $project_new_id = $invoice["project_id"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "invoice_date", "invoice_date");
  $dateexp = sql_date_format($db_type, "invoice_expiration_date", "invoice_expiration_date");
  $datepay = sql_date_format($db_type, "invoice_payment_date", "invoice_payment_date");
  $multidomain = sql_multidomain("invoice");
	
  $where = "1=1 $multidomain";
  
  if ($label != "") {
    $where .= " AND invoice_label $like '%$label%'";
  }
  if ($number != "") {
    $where .= " AND invoice_number $like '%$number%'";
  }
  if ($ht != "") {
    $where .= " AND invoice_amount_ht = '$ht'";
  }
  if ($ttc != "") {
    $where .= " AND invoice_amount_ttc = '$ttc'";
  }
  if (is_array($status) && (count($status) > 0)) {
    $where .= " AND (";
    $or = "";
    foreach ($status as $s_id) {
      $where .= "$or invoice_status_id='$s_id'";
      $or = " OR";
    }
    $where .= ")";
  } else if (($status != '') && ($status != "$c_all")) {
    $where .= " AND invoice_status_id = '$status'";
  }
  if ($date_after != "") {
    $where .= " AND invoice_$date_field >= '$date_after'";
  }
  if ($date_before != "") {
    $where .= " AND invoice_$date_field <= '$date_before'";
  }
  if (($inout != "") && ($inout != "$c_all")) {
    $where .= " AND invoice_inout = '$inout'";
  }
  if (($credit_memo != '') && ($credit_memo != "$c_all")) {
    $where .= " AND invoice_credit_memo = '$credit_memo'";
  }
  if ($comp_id != "") {
    $where .= " AND invoice_company_id = '$comp_id'";
  }
  if ($company != "") {
    $where .= sql_global_company_name_advanced_search($company, $like);
    $join_comp = "LEFT JOIN Company ON invoice_company_id=company_id";
  }
  if ($deal_id != "") {
    $where .= " AND (invoice_deal_id = '$deal_id'
                     OR project_deal_id = '$deal_id')";
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
  }
  if ($project_new_id != "") {
    $where .= " AND invoice_project_id = '$project_new_id'";
  }
  if (is_array($sel_tt)) {
    $where .= " AND (";
    $or = "";
    foreach ($sel_tt as $tt_id) {
      $where .= "$or project_tasktype_id='$tt_id' OR deal_tasktype_id='$tt_id'";
      $or = " OR";
    }
    $where .= ")";
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
    $join_deal = "LEFT JOIN Deal ON invoice_deal_id=deal_id";
  }
  if ($archive != "1") {
    $where .= " AND invoice_archive = '0'";
  }
  $whereq = "WHERE $where";

  $order = (strcmp($sql_order_field,"") != 0) ? $sql_order_field : "invoice_date";
  $orderq .= "ORDER BY $order $sql_order_dir";

  // XXXXXX ???? Display archive
  $query = "SELECT Invoice.*,
      invoice_id as id,
      $date,
      $dateexp,
      $datepay,
      company_id,
      company_name as invoice_company,
      deal_id,
      deal_label as invoice_deal,
      project_id,
      project_name as invoice_project,
      invoicestatus_id,
      invoicestatus_label as invoice_status
    FROM Invoice
      LEFT JOIN InvoiceStatus ON invoice_status_id = invoicestatus_id
      LEFT OUTER JOIN Company ON invoice_company_id = company_id
      LEFT OUTER JOIN Deal ON invoice_deal_id = deal_id
      LEFT JOIN Project on invoice_project_id=project_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Invoice $join_comp $join_deal $join_project $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg ($query, $cdg_sql, "run_query_invoice_search()"); 
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
  $date = sql_date_format($db_type, "Invoice.invoice_date", "date");
  $expiration_date = sql_date_format($db_type, "Invoice.invoice_expiration_date", "expiration_date");
  $payment_date = sql_date_format($db_type, "Invoice.invoice_payment_date", "payment_date");
  $datemodif = sql_date_format($db_type, "invoice_timeupdate", "datemodif");
  $timeupdate = sql_date_format($db_type, "invoice_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "invoice_timecreate", "timecreate");
  $multidomain = sql_multidomain("invoice");
	
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
      invoicestatus_label,
      sum(paymentinvoice_amount) as invoice_paid
    FROM Invoice
      LEFT JOIN Company ON invoice_company_id=company_id
      LEFT JOIN Deal ON invoice_deal_id=deal_id
      LEFT JOIN Project ON invoice_project_id=project_id
      LEFT JOIN InvoiceStatus ON invoice_status_id=invoicestatus_id
      LEFT OUTER join PaymentInvoice ON invoice_id=paymentinvoice_invoice_id
      LEFT JOIN UserObm as c ON invoice_usercreate=c.userobm_id
      LEFT JOIN UserObm as u ON invoice_userupdate=u.userobm_id
    WHERE invoice_id='$p_invoice_id' $multidomain
    GROUP BY Invoice.invoice_id, invoice_timeupdate, invoice_timecreate,
      invoice_userupdate, invoice_usercreate, invoice_company_id,
      invoice_deal_id, invoice_project_id, invoice_number, invoice_label,
      invoice_amount_ht, invoice_amount_ttc, invoice_status_id, invoice_date,
      invoice_expiration_date, invoice_payment_date, invoice_comment,
      invoice_inout, invoice_credit_memo, invoice_archive,
      invoicestatus_label,
      c.userobm_login, u.userobm_login, company_name, deal_label, project_name";

  display_debug_msg ($query, $cdg_sql, "run_query_invoice_detail()"); 
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_insert($invoice) {
  global $cdg_sql, $obm;

  $now = date("Y-m-d H:i:s");
  $uid = $obm["uid"];
  $domain_id = $obm["domain_id"];
	
  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["amount_ht"];
  $ttc = $invoice["amount_ttc"];
  $status = $invoice["status"];
  $date = ($invoice["date"] ? "'".of_isodate_convert($invoice["date"])."'" : "null");
  $expiration_date = ($invoice["expiration_date"] ? "'".of_isodate_convert($invoice["expiration_date"])."'" : "null");
  $payment_date = ($invoice["payment_date"] ? "'".of_isodate_convert($invoice["payment_date"])."'" : "null");
  $inout = $invoice["inout"];
  $credit_memo = $invoice['credit_memo'];
  $add_comment = $invoice["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($invoice["datecomment"]);
    $usercomment = $invoice["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice["company_new_id"];
  if ($comp_id < 1) { $comp_id = $invoice["company_id"]; }
  if ($comp_id == "") { $comp_id = "0"; }
  $deal_id = $invoice["deal_new_id"];
  if ($deal_id < 1) { $deal_id = $invoice["deal_id"]; }
  if ($deal_id == "") { $deal_id = "0"; }
  $project_new_id = $invoice["project_new_id"];
  if ($project_new_id < 1) { $project_new_id = $invoice["project_id"]; }
  if ($project_new_id == "") { $project_new_id = "0"; }

  if ($status == ""){
    $status = "0";
  }
  $arch = ($invoice["archive"] == "1" ? "1" : "0");

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
     '$domain_id',
      null,
     '$now',
     null,
     '$uid',
     '$comp_id',
     '$deal_id',
     '$project_new_id',
     '$number',
     '$label',
     '$ht', 
     '$ttc',
     '$status',
     '$comment',
     $date,
     $expiration_date,
     $payment_date,
     '$inout',
     '$credit_memo',
     '$arch')"; 

  display_debug_msg($query, $cdg_sql, "run_query_invoice_insert()");
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// invoice update 
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_update($invoice) {
  global $obm, $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $uid = $obm["uid"];

  $id = $invoice["invoice_id"];
  $label = $invoice["label"];
  $number = $invoice["number"];
  $arch = ($invoice["archive"] == "1" ? "1" : "0");
  $ht = $invoice["amount_ht"];
  $ttc = $invoice["amount_ttc"];
  $status = $invoice["status"];
  $date = ($invoice["date"] ? "'".of_isodate_convert($invoice["date"])."'" : "null");
  $expiration_date = ($invoice["expiration_date"] ? "'".of_isodate_convert($invoice["expiration_date"])."'" : "null");
  $payment_date = ($invoice["payment_date"] ? "'".of_isodate_convert($invoice["payment_date"])."'" : "null");
  $inout = $invoice["inout"];
  $credit_memo = $invoice['credit_memo'];
  if ($status == ""){
    $status = "0";
  }
  $comment = $invoice["comment"];
  $add_comment = $invoice["add_comment"];
  if ($add_comment != "") {
    $datecomment = of_isodate_convert($invoice["datecomment"]);
    $usercomment = $invoice["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice["company_new_id"];
  if ($comp_id < 1) { $comp_id = $invoice["company_id"]; }
  if ($comp_id == "") { $comp_id = "0"; }
  $deal_id = $invoice["deal_new_id"];
  if ($deal_id < 1) { $deal_id = $invoice["deal_id"]; }
  if ($deal_id == "") { $deal_id = "0"; }
  $project_new_id = $invoice["project_new_id"];
  if ($project_new_id < 1) { $project_new_id = $invoice["project_id"]; }
  if ($project_new_id == "") { $project_new_id = "0"; }

  $multidomain = sql_multidomain("invoice");

  $query = "UPDATE Invoice SET
      invoice_timeupdate='$now',
      invoice_userupdate='$uid',
      invoice_company_id='$comp_id',
      invoice_deal_id='$deal_id',
      invoice_project_id='$project_new_id',
      invoice_number='$number',
      invoice_label='$label',
      invoice_amount_ht='$ht',
      invoice_amount_ttc='$ttc',
      invoice_status_id='$status',
      invoice_comment='$comment',
      invoice_date=$date,
      invoice_expiration_date=$expiration_date,
      invoice_payment_date=$payment_date,
      invoice_inout='$inout',
      invoice_credit_memo='$credit_memo',
      invoice_archive='$arch'
    WHERE invoice_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, "run_query_invoice_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
} 


///////////////////////////////////////////////////////////////////////////////
// Get invoice status list with conditions
// Parameters:
//   - $target : "payment" : get status where invoice should have a payment
//             : "tocreate" : get status where invoice ahs not been cretated
// Returns:
//   - $status : array of status [id] [[label][created][payment][archive]]
///////////////////////////////////////////////////////////////////////////////
function get_invoicestatus($target="") {
  global $cdg_sql;

  $multidomain = sql_multidomain("invoicestatus");
	
  if ($target == "payment") {
    $wtarget = "WHERE invoicestatus_payment='1' $multidomain";
  } else if ($target == "tocreate") {
    $wtarget = "WHERE invoicestatus_created='0' $multidomain";
  }


  $query = "SELECT invoicestatus_id,
      invoicestatus_label,
      invoicestatus_payment,
      invoicestatus_created,
      invoicestatus_archive
    FROM InvoiceStatus
    $wtarget
    ORDER BY invoicestatus_archive, invoicestatus_payment";

  display_debug_msg($query, $cdg_sql, "get_invoicestatus()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f("invoicestatus_id");
    $label = $obm_q->f("invoicestatus_label");
    $payment = $obm_q->f("invoicestatus_payment");
    $created = $obm_q->f("invoicestatus_created");
    $archive = $obm_q->f("invoicestatus_archive");
    $status[$id] = array ("label" => $label,
			  "payment" => $payment,
			  "created" => $created,
			  "archive" => $archive);
  }

  return $status;
}


///////////////////////////////////////////////////////////////////////////////
// getting all payments of invoice $p_invoice 
// if $not_paid = 1, we search only for not paid payments,
// ie. lines having paymentinvoice_amount = 0.0
// if $not_paid == -1, we return only paid payments
// ie. lines having paymetinvoice_amoount != 0.0
///////////////////////////////////////////////////////////////////////////////
function get_invoice_payment($p_invoice, $not_paid=0) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, "payment_date", "payment_date");
  $multidomain = sql_multidomain("payment");
	
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
    WHERE paymentinvoice_invoice_id = '$p_invoice'
      $multidomain";

  if ($not_paid == 1) {
    $query .= " AND paymentinvoice_amount = 0.0 ";
  } elseif ($not_paid == -1) {
    $query .= " AND paymentinvoice_amount <> 0.0 ";
  }
  
  display_debug_msg($query, $cdg_sql, "get_invoice_payment");
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f("payment_id");
    $number = $obm_q->f("payment_number");
    $company = $obm_q->f("company_name");
    $date = of_date_format($obm_q->f("payment_date"));
    $amount = $obm_q->f("payment_amount");
    $inout = $obm_q->f("payment_inout");
    $affected_amount = $obm_q->f("paymentinvoice_amount");
    $p[$id] = array ("id" => $id,
		     "date" => $date,
		     "number" => $number,
		     "company" => $company,
		     "amount" => $amount,
		     "inout" => $inout,
		     "affected_amount" => $affected_amount);
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
  
	$multidomain = sql_multidomain("invoice");
  $query = "DELETE FROM Invoice 
		WHERE invoice_id='$p_id' $multidomain";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    run_query_global_delete_document_links($p_id, "invoice");    
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Return all the payments attached to the invoice
// Parameters:
//   - $p_id : invoice Id
///////////////////////////////////////////////////////////////////////////////
function run_query_linked_invoice_payment($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("payment");
  $query = "SELECT distinct payment_id 
    FROM Payment
      LEFT JOIN PaymentInvoice ON payment_id=paymentinvoice_payment_id
    WHERE paymentinvoice_invoice_id='$p_id' $multidomain";

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

  $multidomain = sql_multidomain("deal");

  $query = "SELECT dealtype_inout
    FROM Deal, DealType
    WHERE deal_id = '$p_deal_id'
      AND deal_type_id = dealtype_id $multidomain";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("dealtype_inout");
}


///////////////////////////////////////////////////////////////////////////////
// Check if an invoice number is already used
// Parameters:
//   - $number : invoice number to check
//   - $id     : invoice id to exclude
// Returns: $nb used
///////////////////////////////////////////////////////////////////////////////
function get_invoice_nb_having_number ($number, $id="") {
  global $cdg_sql;

  if ($id != "") {
    $where_id = "AND invoice_id != '$id'";
  }
	
	$multidomain = sql_multidomain("invoice");

  $query = "SELECT invoice_id
    FROM Invoice
    WHERE invoice_number = '$number'
      $where_id $multidomain";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $nb = $obm_q->num_rows();

  return $nb;
}


///////////////////////////////////////////////////////////////////////////////
// Get the InvoiceStatus info
// Parameters:
//   - $p_id : invoicestatus_id
// Returns: array of invoicestatus infos
///////////////////////////////////////////////////////////////////////////////
function get_invoice_status_info($p_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain("invoicestatus");

  if ($p_id > 0) {
    $query = "SELECT *
      FROM InvoiceStatus
      WHERE invoicestatus_id='$p_id' $multidomain";

    display_debug_msg($query, $cdg_sql, "get_invoice_status_info()");
    $obm_q = new DB_OBM;
    $obm_q->query($query);
    $obm_q->next_record();

    $res["label"] = $obm_q->f("invoicestatus_label");
    $res["payment"] = $obm_q->f("invoicestatus_payment");
    $res["created"] = $obm_q->f("invoicestatus_created");
    $res["archive"] = $obm_q->f("invoicestatus_archive");
  } else {
    $res = "";
  }

  return $res;
}


///////////////////////////////////////////////////////////////////////////////
// Get global invoices infos
// Parameters:
//   - $date_ranges : array [range1] : [date_start, date_end]
//   - $target_date : invoice_date, invoice_expiration_date : date to sort
//   - $sel_tt      : [optionnal] tasktype array filter
//   - $inout       : [optionnal] (+ || -) to get client or supplier invoices
// Returns:
//   $res : array[date]([billed]|[paid]|[potential])([status]|[total])[$infos]
//            $infos = label, amount_ht, amount_ttc, nb
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_amounts($date_ranges, $target_date="invoice_date", $sel_tt="", $inout="+") {
  global $cdg_sql, $cdg_param;
  global $l_total;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $da = sql_date_format($db_type, "invoice_date", "date");
  $de = sql_date_format($db_type, "invoice_expiration_date", "expiration_date");
  $multidomain = sql_multidomain("invoice");
	
  if (is_array($date_ranges)) {
    $w_date = "AND ";
    foreach ($date_ranges as $range) {
      $d1 = $range[0];
      $d2 = $range[1];
      $w_date .= " $or ($target_date >= '$d1' AND $target_date <= '$d2')";
      $or = "OR";
    }
  }

  // Tasktype filter
  if (is_array($sel_tt)) {
    $w_tt .= " AND (";
    $or = "";
    foreach ($sel_tt as $tt_id) {
      $w_tt .= "$or project_tasktype_id='$tt_id' OR deal_tasktype_id='$tt_id'";
      $or = " OR";
    }
    $w_tt .= ")";
    $join_project = "LEFT JOIN Project ON invoice_project_id=project_id";
    $join_deal = "LEFT JOIN Deal ON invoice_deal_id=deal_id";
  }

  // Factured (invoicestatus_payment = 1) OR potential (status_created = 0)
  $query = "SELECT
      $da,
      $de,
      invoice_amount_ht,
      invoice_amount_ttc,
      invoice_inout,
      invoice_status_id,
      invoicestatus_label,
      invoicestatus_payment,
      invoicestatus_archive
    FROM Invoice
      LEFT JOIN InvoiceStatus on invoice_status_id=invoicestatus_id
      $join_project
      $join_deal
    WHERE invoice_inout = '$inout' 
      AND (invoicestatus_payment=1 OR invoicestatus_created=0)
      $w_date
      $w_tt 
      $multidomain
    ORDER BY invoice_status_id
";
  
  display_debug_msg($query, $cdg_sql, "run_query_invoice_amounts()");
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $ah = $obm_q->f("invoice_amount_ht");
    $at = $obm_q->f("invoice_amount_ttc");
    $inout = $obm_q->f("invoice_inout");
    $date = of_isodate_format($obm_q->f("date"));
    $exp_date = of_isodate_format($obm_q->f("expiration_date"));
    if ($target_date == "invoice_expiration_date") {
      $month = substr($exp_date, 0, 7);
    } else {
      $month = substr($date, 0, 7);
    }
    $status = $obm_q->f("invoice_status_id");
    $statuslabel = $obm_q->f("invoicestatus_label");
    $payment = $obm_q->f("invoicestatus_payment");
    $archivable = $obm_q->f("invoicestatus_archive");

    // Invoice created
    if ($payment == "1") {
      $res[$month]["billed"]["$status"]["label"] = $statuslabel;
      $res[$month]["billed"]["$status"]["amount_ht"] += $ah;
      $res[$month]["billed"]["$status"]["amount_ttc"] += $at;
      $res[$month]["billed"]["$status"]["nb"] ++;
      $res[$month]["billed"]["total"]["label"] = $l_total;
      $res[$month]["billed"]["total"]["amount_ht"] += $ah;
      $res[$month]["billed"]["total"]["amount_ttc"] += $at;
      $res[$month]["billed"]["total"]["nb"] ++;
      // Il the invoice that need a payment can be archived, it has been paid
      if ($archivable == "1") {
	$res[$month]["paid"]["total"]["amount_ht"] += $ah;
	$res[$month]["paid"]["total"]["amount_ttc"] += $at;
	$res[$month]["paid"]["total"]["nb"] ++;
      }	
    } else {
      // Invoice to create
      $res[$month]["potential"]["$status"]["label"] = $statuslabel;
      $res[$month]["potential"]["$status"]["amount_ht"] += $ah;
      $res[$month]["potential"]["$status"]["amount_ttc"] += $at;
      $res[$month]["potential"]["$status"]["nb"] ++;
      $res[$month]["potential"]["total"]["label"] = $l_total;
      $res[$month]["potential"]["total"]["amount_ht"] += $ah;
      $res[$month]["potential"]["total"]["amount_ttc"] += $at;
      $res[$month]["potential"]["total"]["nb"] ++;
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
  global $err, $l_exist_error;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $date = of_isodate_convert($invoice["date"],true);
  $expiration_date = of_isodate_convert($invoice["expiration_date"],true);
  $payment_date = of_isodate_convert($invoice["payment_date"],true);
  $inout = $invoice["inout"];
  $ht = $invoice["amount_ht"];
  $ttc = $invoice["amount_ttc"];
  $c_id = $invoice["company_id"];
  $c_new_id = $invoice["company_new_id"];
  $status = $invoice["status"];
  $istatus = get_invoice_status_info($status);
  $status_created = $istatus["created"];
	
  // MANDATORY: Invoice label
  if (trim($label) == "") {
    $err["msg"] = $l_fill_label;
    return false;
  }

  // MANDATORY: Invoice inout must be set
  if (($inout != "+") && ($inout != "-")) {
    $err["msg"] = $l_fill_inout;
    return false;
  }

  // Date check
  if ($date == "") {
    // If invoice in a status "created", exp date must be set
    if ($status_created) {
      $err["msg"] = $l_fill_date;
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $date) == 0) {
    $err["msg"] = "$l_date : $date : $l_invalid_date"; 
    return false;
  }

  // Expiration Date check
  if ($expiration_date == "") {
    // If invoice in a status "created", exp date must be set
    if ($status_created) {
      $err["msg"] = "$l_expiration_date : $l_fill_date";
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $expiration_date) == 0) {
    $err["msg"] = "$l_expiration_date : $expiration_date : $l_invalid_date"; 
    return false;
  }

  // Payment Date check
  if (($payment_date != "") && (preg_match($php_regexp_isodate, $payment_date) == 0)) {
    $err["msg"] = "$l_payment_date : $payment_date : $l_invalid_date"; 
    return false;
  }

  // MANDATORY: a company must be set
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err["msg"] = $l_fill_company;
    return false;
  }

  // MANDATORY: Amount must be set
  if ( !($ht > 0) || !($ttc > 0)) {
    $err["msg"] = $l_fill_amount;
    return false;
  }

  // MANDATORY: Invoice number, if state not "to be created"
  if ($status_created) {
    if (trim($number) == "") {
      $err["msg"] = $l_fill_number;
      return false;
    } else {
      $nb = get_invoice_nb_having_number($number, $id);
      if ($nb > 0) {
	$err["msg"] = $l_error_number_exist;
	return false;
      }
    }
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
    $err["msg"] .= "$l_link_payment";
  } else {
    $ok_msg .= "$l_link_payment_no";
  }

  return $delete_ok;
}


?>
