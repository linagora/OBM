<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_query.inc
//     - Desc : invoice query File
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Invoice search query :
///////////////////////////////////////////////////////////////////////////////
function run_query_search ($invoice, $p_new_order, $p_order_dir) {
  global $auth, $cdg_sql, $c_all, $ctu_sql_limit;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $company = $invoice["company"];
  $deal = $invoice["deal"];
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
    $where .= " and company_name $like '$company%'";
    $join_comp = "left join Company on invoice_company_id=company_id";
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
  $pdate = sql_date_format($db_type, "Invoice.invoice_payment_date", "payment_date");
  $datemodif = sql_date_format($db_type, "invoice_timeupdate", "datemodif");
  $timeupdate = sql_date_format($db_type, "invoice_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "invoice_timecreate", "timecreate");

  // XXXXX ???? exporter le calcul des paiements a par (pour eviter group by)
  $query = "select Invoice.*,
      $date,
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
    from Invoice
      left join Company on invoice_company_id=company_id
      left join Deal on invoice_deal_id=deal_id
      left join Project on invoice_project_id=project_id
      left join InvoiceStatus on invoice_status_id=invoicestatus_id
      left outer join PaymentInvoice on invoice_id=paymentinvoice_invoice_id
      left join UserObm as c on invoice_usercreate=c.userobm_id
      left join UserObm as u on invoice_userupdate=u.userobm_id
    where invoice_id='$p_invoice_id'
    group by Invoice.invoice_id, invoice_timeupdate, invoice_timecreate,
      invoice_userupdate, invoice_usercreate, invoice_company_id,
      invoice_deal_id, invoice_project_id, invoice_number, invoice_label,
      invoice_amount_ht, invoice_amount_ttc, invoice_status_id, invoice_date,
      invoice_payment_date, invoice_comment, invoice_inout, invoice_archive,
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
  $comment = $invoice["comment"];
  $date = $invoice["date"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
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
  $arch = (isset($project["archive"]) ? $project["archive"] : '0');

  $query = "insert into Invoice (
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
      invoice_payment_date,
      invoice_inout,
      invoice_archive
    ) values (
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
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $comment = $invoice["comment"];
  $date = $invoice["date"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
  if ($status == ""){
    $status = "0";
  }
  $arch = (isset($project["archive"]) ? $project["archive"] : '0');
  $comp_id = $invoice["comp_new_id"];
  if ($comp_id < 1) { $comp_id = $invoice["company_id"]; }
  if ($comp_id == "") { $comp_id = "0"; }
  $deal_id = $invoice["deal_new_id"];
  if ($deal_id < 1) { $deal_id = $invoice["deal_id"]; }
  if ($deal_id == "") { $deal_id = "0"; }
  $proj_id = $invoice["proj_new_id"];
  if ($proj_id < 1) { $proj_id = $invoice["project_id"]; }
  if ($proj_id == "") { $proj_id = "0"; }

  $query = "update Invoice set
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
      invoice_payment_date='$pdate',
      invoice_inout='$inout',
      invoice_archive='$arch'
    where invoice_id='$id'"; 

  display_debug_msg($query, $cdg_sql, "run_query_update()");
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
} 


///////////////////////////////////////////////////////////////////////////////
// getting invoice status data
///////////////////////////////////////////////////////////////////////////////
function run_query_invoicestatus() {
  global $cdg_sql;

  $query = "select invoicestatus_id,
      invoicestatus_label
    from InvoiceStatus
     order by invoicestatus_archive, invoicestatus_payment";
  display_debug_msg($query, $cdg_sql, "run_query_invoicestatus()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
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

  $query = "select
      paymentinvoice_amount,
      payment_id,
      payment_label,
      payment_number,
      $date,
      $exp_date,
      payment_amount
    from PaymentInvoice
         left join Payment on paymentinvoice_payment_id=payment_id
    where paymentinvoice_invoice_id = '$p_invoice'";

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
// Set an invoice to an "archived" state
// Parameters:
//   - $p_id : invoice Id
///////////////////////////////////////////////////////////////////////////////
function run_query_update_archive($p_id) {
  global $cdg_sql;

  $query = "update Invoice set invoice_archive='1' where invoice_id='$p_id'";
  display_debug_msg ("<b>run_query_update_archive : </b>$query", $cdg_sql);
  $go = new DB_OBM;
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Function to update the updater of an invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_update_updater ($auth, $param_invoice){
  global $cdg_sql;

  $connect_db = new DB_OBM ;
  $query = "update Invoice
      set invoice_userupdate='".$auth->auth["uid"]."',
        invoice_timeupdate='".date("Y-m-d H:i:s")."'
      where invoice_id='$param_invoice'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Invoice Deletion query execution
// Parameters:
//   - $p_id : invoice id
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_delete($p_id) {
  global $cdg_sql;

  $query = "delete from Invoice where invoice_id='$p_id'";

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
function run_query_invoice_payment_links($p_id) {
  global $cdg_sql;

  $query = "select distinct payment_id, payment_label, payment_amount 
    from Payment
         left join PaymentInvoice on payment_id=paymentinvoice_payment_id
    where paymentinvoice_invoice_id='$p_id'";

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

  $query = "select dealtype_inout from Deal,DealType
      where deal_id = '$p_deal_id'
        and deal_type_id = dealtype_id";

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
    $where_id = "and invoice_id != '$id'";
  }

  $query = "select invoice_id from Invoice
      where invoice_number = '$number'
        $where_id";

  $obm_q = new DB_OBM;
  display_debug_msg($query, $cdg_sql);
  $obm_q->query($query);
  $nb = $obm_q->num_rows();

  return $nb;
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
  global $err_msg, $l_exist_error;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $date = $invoice["date"];
  $pdate = $invoice["pdate"];
  $inout = $invoice["inout"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $c_id = $invoice["company_id"];
  $c_new_id = $invoice["comp_new_id"];

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
    $err_msg = $l_fill_date;
    return false;
  } else if (preg_match($php_regexp_isodate, $date) == 0) {
    $err_msg = "$l_date : $date : $l_invalid_date"; 
    return false;
  }

  // Payment Date check
  if ($pdate == "") {
    $err_msg = $l_fill_date;
    return false;
  } else if (preg_match($php_regexp_isodate, $pdate) == 0) {
    $err_msg = "$l_date : $pdate : $l_invalid_date"; 
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

  // XXXXX MANDATORY: Invoice number, if state not "to be created"
  if (true) {
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


</script>
