<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_query.inc
//     - Desc : invoice query File
// 2001-07-30 - Aliacom - Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Invoice search query :
///////////////////////////////////////////////////////////////////////////////
function run_query_search ($invoice, $p_new_order, $p_order_dir) {
  global $auth, $cdg_sql, $c_all, $ctu_sql_limit;

  $label = sql_search_text_parse($invoice["label"]);
  $number = sql_search_text_parse($invoice["number"]);
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $company = sql_search_text_parse($invoice["company"]);
  $deal = sql_search_text_parse($invoice["deal"]);
  $archive = $invoice["archive"];
  $comp_id = $invoice["company_id"];
  $deal_id = $invoice["deal_id"];
  $proj_id = $invoice["project_id"];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $date = sql_date_format($db_type, "invoice_date", "date");

  $where = "1=1";
  if ($label != ""){
    $where .= " and invoice_label $like '%$label%'";
  }
  if ($number != ""){
    $where .= " and invoice_number $like '%$number%'";
  }
  if ($ht != ""){
    $where .= " and invoice_amount_ht = '$ht'";
  }
  if ($ttc != ""){
    $where .= " and invoice_amount_ttc = '$ttc'";
  }
  if (($status != '') && ($status != "$c_all")){
    $where .= " and invoice_status_id = '$status'";
  }
  if ($date_after != ""){
    $where .= " and invoice_date >= '$date_after'";
  }
  if ($date_before != ""){
    $where .= " and invoice_date <= '$date_before'";
  }
  if (($inout != "") && ($inout != "$c_all")){
    $where .= " and invoice_inout = '$inout'";
  }
  if ($comp_id != "") {
    $where .= " and invoice_company_id = '$comp_id'";
  }
  if ($company != "") {
    $where .= sql_company_name_advanced_search($company, $like);
    $join_comp = "LEFT JOIN Company ON invoice_company_id=company_id";
  }
  if ($deal != "") {
    $where .= " and deal_label $like '$deal%'";
    $join_deal = "left join Deal on invoice_deal_id=deal_id";
  }
  if ($deal_id != "") {
    $where .= " and invoice_deal_id = '$deal_id'";
  }
  if ($proj_id != "") {
    $where .= " and invoice_project_id = '$proj_id'";
  }
  if ($archive != "1") {
    $where .= " and invoice_archive = '0'";
  }
  $whereq = "where $where";

  $order = (strcmp($p_new_order,"") != 0) ? $p_new_order : "invoice_date";
  $orderq .= "order by $order $order_dir";

  // XXXXXX ???? Display archive
  $query = "select Invoice.*,
      invoice_id as id,
      $date,
      company_id, company_name as invoice_company,
      deal_id, deal_label as invoice_deal,
      project_id, project_name as invoice_project,
      invoicestatus_id, invoicestatus_label as invoice_status
    from Invoice
      left outer join Company on invoice_company_id = company_id
      left outer join Deal on invoice_deal_id = deal_id
      left outer join Project on invoice_project_id = project_id
      left join InvoiceStatus on invoice_status_id = invoicestatus_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Invoice $join_comp $join_deal $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg ($query, $cdg_sql, "run_query_search()"); 
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice: Select query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_invoice_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $date = sql_date_format($db_type, "Invoice.invoice_date", "date");
  $edate = sql_date_format($db_type, "Invoice.invoice_expiration_date", "expiration_date");
  $pdate = sql_date_format($db_type, "Invoice.invoice_payment_date", "payment_date");
  $datemodif = sql_date_format($db_type, "invoice_timeupdate", "datemodif");
  $timeupdate = sql_date_format($db_type, "invoice_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "invoice_timecreate", "timecreate");

  // XXXXX ???? exporter le calcul des paiements a par (pour eviter group by)
  $query = "SELECT Invoice.*,
      $date,
      $edate,
      $pdate,
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
      LEFT JOIN Company on invoice_company_id=company_id
      LEFT JOIN Deal on invoice_deal_id=deal_id
      LEFT JOIN Project on invoice_project_id=project_id
      LEFT JOIN InvoiceStatus on invoice_status_id=invoicestatus_id
      LEFT OUTER join PaymentInvoice on invoice_id=paymentinvoice_invoice_id
      LEFT JOIN UserObm as c on invoice_usercreate=c.userobm_id
      LEFT JOIN UserObm as u on invoice_userupdate=u.userobm_id
    WHERE invoice_id='$p_invoice_id'
    GROUP BY Invoice.invoice_id, invoice_timeupdate, invoice_timecreate,
      invoice_userupdate, invoice_usercreate, invoice_company_id,
      invoice_deal_id, invoice_project_id, invoice_number, invoice_label,
      invoice_amount_ht, invoice_amount_ttc, invoice_status_id, invoice_date,
      invoice_expiration_date, invoice_payment_date, invoice_comment,
      invoice_inout, invoice_archive,
      invoicestatus_label,
      c.userobm_login, u.userobm_login, company_name, deal_label, project_name";

  display_debug_msg ($query, $cdg_sql, "run_query_detail()"); 
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($invoice) {
  global $auth, $cdg_sql, $new_order, $order_dir;

  $now = date("Y-m-d H:i:s");
  $uid = $auth->auth["uid"];

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date = $invoice["date"];
  $edate = $invoice["edate"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
  $add_comment = $invoice["add_comment"];
  if ($add_comment != "") {
    $datecomment = $invoice["datecomment"];
    $usercomment = $invoice["usercomment"];
    $comment = "$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice["comp_new_id"];
  if ($comp_id < 1) { $comp_id = $invoice["company_id"]; }
  if ($comp_id == "") { $comp_id = "0"; }
  $deal_id = $invoice["deal_new_id"];
  if ($deal_id < 1) { $deal_id = $invoice["deal_id"]; }
  if ($deal_id == "") { $deal_id = "0"; }
  $proj_id = $invoice["proj_new_id"];
  if ($proj_id < 1) { $proj_id = $invoice["project_id"]; }
  if ($proj_id == "") { $proj_id = "0"; }

  if ($status == ""){
    $status = "0";
  }
  $arch = (isset($invoice["archive"]) ? $invoice["archive"] : '0');

  $query = "INSERT INTO Invoice (
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
      invoice_archive)
    VALUES (
      null,
     '$now',
     null,
     '$uid',
     '$comp_id',
     '$deal_id',
     '$proj_id',
     '$number',
     '$label',
     '$ht', 
     '$ttc',
     '$status',
     '$comment',
     '$date',
     '$edate',
     '$pdate',
     '$inout',
     '$arch')"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $ret = $obm_q->query($query);

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// invoice update 
///////////////////////////////////////////////////////////////////////////////
function run_query_update ($invoice) {
  global $auth, $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $uid = $auth->auth["uid"];

  $id = $invoice["id"];
  $label = $invoice["label"];
  $number = $invoice["number"];
  $arch = (isset($invoice["archive"]) ? $invoice["archive"] : '0');
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date = $invoice["date"];
  $edate = $invoice["edate"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
  if ($status == ""){
    $status = "0";
  }
  $comment = $invoice["comment"];
  $add_comment = $invoice["add_comment"];
  if ($add_comment != "") {
    $datecomment = $invoice["datecomment"];
    $usercomment = $invoice["usercomment"];
    $comment .= "\n$datecomment:$usercomment:$add_comment";
  }
  $comp_id = $invoice["comp_new_id"];
  if ($comp_id < 1) { $comp_id = $invoice["company_id"]; }
  if ($comp_id == "") { $comp_id = "0"; }
  $deal_id = $invoice["deal_new_id"];
  if ($deal_id < 1) { $deal_id = $invoice["deal_id"]; }
  if ($deal_id == "") { $deal_id = "0"; }
  $proj_id = $invoice["proj_new_id"];
  if ($proj_id < 1) { $proj_id = $invoice["project_id"]; }
  if ($proj_id == "") { $proj_id = "0"; }

  $query = "UPDATE Invoice SET
      invoice_timeupdate='$now',
      invoice_userupdate='$uid',
      invoice_company_id='$comp_id',
      invoice_deal_id='$deal_id',
      invoice_project_id='$proj_id',
      invoice_number='$number',
      invoice_label='$label',
      invoice_amount_ht='$ht',
      invoice_amount_ttc='$ttc',
      invoice_status_id='$status',
      invoice_comment='$comment',
      invoice_date='$date',
      invoice_expiration_date='$edate',
      invoice_payment_date='$pdate',
      invoice_inout='$inout',
      invoice_archive='$arch'
    WHERE invoice_id='$id'";

  display_debug_msg($query, $cdg_sql, "run_query_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
} 


///////////////////////////////////////////////////////////////////////////////
// Get invoice status list with coditions
// Parameters:
//   - $target : "payment" : get status where invoice should have a payment
// Returns:
//   - $status : array of status [id][label]
///////////////////////////////////////////////////////////////////////////////
function get_invoicestatus($target="") {
  global $cdg_sql;

  if ($target == "payment") {
    $wtarget = "WHERE invoicestatus_payment = 1";
  }

  $query = "SELECT invoicestatus_id,
      invoicestatus_label
    FROM InvoiceStatus
    $wtarget
    ORDER BY invoicestatus_archive, invoicestatus_payment";

  display_debug_msg($query, $cdg_sql, "get_invoicestatus()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f("invoicestatus_id");
    $label = $obm_q->f("invoicestatus_label");
    $status[$id] = $label;
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
function run_query_invoice_payment ($p_invoice, $not_paid=0) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $date = sql_date_format($db_type, "payment_date", "payment_date");
  $exp_date = sql_date_format($db_type, "payment_expected_date", "payment_expect_date");

  $query = "SELECT
      paymentinvoice_amount,
      payment_id,
      payment_label,
      payment_number,
      $date,
      $exp_date,
      payment_amount
    FROM PaymentInvoice
         LEFT JOIN Payment ON paymentinvoice_payment_id=payment_id
    WHERE paymentinvoice_invoice_id = '$p_invoice'";

  if ($not_paid == 1) {
    $query .= " and paymentinvoice_amount = 0.0 ";
  } elseif ($not_paid == -1) {
    $query .= " and paymentinvoice_amount <> 0.0 ";
  }
  
  display_debug_msg($query, $cdg_sql, "run_query_invoice_payment");
  $obm_q->query($query);
  return $obm_q;
}


/*
///////////////////////////////////////////////////////////////////////////////
// add a payment to an invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_add_payment ($p_invoice_id, $p_payment_id, $p_amount){
  global $auth, $cdg_sql;
  $db = new DB_OBM;
  $query = "insert into PaymentInvoice (paymentinvoice_invoice_id, paymentinvoice_payment_id, paymentinvoice_amount, paymentinvoice_usercreate) values('".$p_invoice_id."', '".$p_payment_id."', '".$p_amount."', '".$auth->auth["uid"]."')";

  display_debug_msg ($query, $cdg_sql);
  $db->query($query);

  // we update the payment too, the payment_amount_used has changed...
  $query = "update Payment set payment_amount_used = (payment_amount_used + ".$p_amount.") where payment_id = '".$p_payment_id."'";
}
*/

/*
should be considered deprecated...
///////////////////////////////////////////////////////////////////////////////
// delete a connection between a payment and an invoice.
// if $p_payment_id == "", delete all data about that invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_remove_payment ($p_invoice_id, $p_payment_id=""){
  global $cdg_sql;
  $db = new DB_OBM;
  $query = "delete from PaymentInvoice where paymentinvoice_invoice_id = '$p_invoice_id'";
  if ($p_payment_id != ""){
    $query .= "and  paymentinvoice_payment_id='$p_payment_id'";
  }
  display_debug_msg ($query, $cdg_sql);
  $db->query($query);
}
*/


///////////////////////////////////////////////////////////////////////////////
// Invoice Deletion query execution
// Parameters:
//   - $p_id : invoice id
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_delete($p_id) {
  global $cdg_sql;

  $query = "DELETE FROM Invoice WHERE invoice_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  if ($retour) {
    run_query_delete_document_links($p_id, "invoice");    
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

  $query = "SELECT distinct payment_id, payment_label, payment_amount 
    FROM Payment
         LEFT JOIN PaymentInvoice ON payment_id=paymentinvoice_payment_id
    WHERE paymentinvoice_invoice_id='$p_id'";

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// returns the value of the inout attribute of the given deal
///////////////////////////////////////////////////////////////////////////////
function run_query_deal_get_inout ($p_deal_id) {
  global $cdg_sql;

  $query = "SELECT dealtype_inout
    FROM Deal, DealType
    WHERE deal_id = '$p_deal_id'
      AND deal_type_id = dealtype_id";

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

  $query = "SELECT invoice_id
    FROM Invoice
    WHERE invoice_number = '$number'
      $where_id";

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
function get_invoicestatus_info($p_id) {
  global $cdg_sql;

  if ($p_id > 0) {
    $query = "SELECT *
      FROM InvoiceStatus
      WHERE invoicestatus_id='$p_id'";

    display_debug_msg($query, $cdg_sql);
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
// Returns:
//     array : array[date]([billed]|[potential])([status]|[total])[$infos]
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_amounts($date_ranges) {
  global $cdg_sql, $cdg_param;
  global $l_total;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $da = sql_date_format($db_type, "invoice_date", "date");
  $de = sql_date_format($db_type, "invoice_expiration_date", "expiration_date");

  if (is_array($date_ranges)) {
    $w_date = "AND ";
    foreach ($date_ranges as $range) {
      $d1 = $range[0];
      $d2 = $range[1];
      $w_date .= " $or (invoice_date > '$d1' AND invoice_date < '$d2')";
      $or = "OR";
    }
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
      invoicestatus_payment
    FROM Invoice
      LEFT JOIN InvoiceStatus on invoice_status_id=invoicestatus_id
    WHERE invoice_inout = '+' 
      AND (invoicestatus_payment=1 OR invoicestatus_created=0)
      $w_date 
    ORDER BY invoice_status_id
";
  
  display_debug_msg($query, $cdg_sql, "run_query_invoice_amounts()");
  $obm_q->query($query);
  
  while ($obm_q->next_record()) {
    $ah = $obm_q->f("invoice_amount_ht");
    $at = $obm_q->f("invoice_amount_ttc");
    $inout = $obm_q->f("invoice_inout");
    $date = $obm_q->f("date");
    $month = substr($date, 0, 7);
    $status = $obm_q->f("invoice_status_id");
    $statuslabel = $obm_q->f("invoicestatus_label");
    $payment = $obm_q->f("invoicestatus_payment");

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
//     keys used  : label, number, inout, company_id, comp_new_id
///////////////////////////////////////////////////////////////////////////////
function check_invoice_data_form($id, $invoice) {
  global $php_regexp_isodate;
  global $l_fill_label, $l_fill_company, $l_fill_number, $l_error_number_exist;
  global $l_fill_inout, $l_fill_amount, $l_fill_date, $l_date, $l_invalid_date;
  global $l_expiration_date, $l_payment_date;
  global $err_msg, $l_exist_error;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $date = $invoice["date"];
  $edate = $invoice["edate"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $c_id = $invoice["company_id"];
  $c_new_id = $invoice["comp_new_id"];
  $status = $invoice["status"];
  $istatus = get_invoicestatus_info($status);
  $status_created = $istatus["created"];

  // MANDATORY: Invoice label
  if (trim($label) == "") {
    $err_msg = $l_fill_label;
    return false;
  }

  // MANDATORY: Invoice inout must be set
  if (($inout != "+") && ($inout != "-")) {
    $err_msg = $l_fill_inout;
    return false;
  }

  // Date check
  if ($date == "") {
    // If invoice in a status "created", exp date must be set
    if ($status_created) {
      $err_msg = $l_fill_date;
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $date) == 0) {
    $err_msg = "$l_date : $date : $l_invalid_date"; 
    return false;
  }

  // Expiration Date check
  if ($edate == "") {
    // If invoice in a status "created", exp date must be set
    if ($status_created) {
      $err_msg = "$l_expiration_date : $l_fill_date";
      return false;
    }
  } else if (preg_match($php_regexp_isodate, $edate) == 0) {
    $err_msg = "$l_expiration_date : $edate : $l_invalid_date"; 
    return false;
  }

  // Payment Date check
  if (($pdate != "") && (preg_match($php_regexp_isodate, $pdate) == 0)) {
    $err_msg = "$l_payment_date : $pdate : $l_invalid_date"; 
    return false;
  }

  // MANDATORY: Invoice inout type must be set
  if (($inout != "+") && ($inout != "-")) {
    $err_msg = $l_fill_inout;
    return false;
  }

  // MANDATORY: a company must be set
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err_msg = $l_fill_company;
    return false;
  }

  // MANDATORY: Amount must be set
  if ( !($ht > 0) || !($ttc > 0)) {
    $err_msg = $l_fill_amount;
    return false;
  }

  // MANDATORY: Invoice number, if state not "to be created"
  if ($status_created) {
    if (trim($number) == "") {
      $err_msg = $l_fill_number;
      return false;
    } else {
      $nb = get_invoice_nb_having_number($number, $id);
      if ($nb > 0) {
	$err_msg = $l_error_number_exist;
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
  global $err_msg, $ok_msg;
  global $l_link_payment, $l_link_payment_no;

  $delete_ok = true;

  // Links from Payment
  $obm_q = run_query_linked_invoice_payment($p_id);
  $nb = $obm_q->num_rows();
  if ($nb > 0) {
    $delete_ok = false;
    $err_msg .= "$l_link_payment";
  } else {
    $ok_msg .= "$l_link_payment_no";
  }

  return $delete_ok;
}


</script>
