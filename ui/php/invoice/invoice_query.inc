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
  $deal_id = $invoice["param_deal"];

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
  if ($company != "") {
    $where .= " and company_name $like '$company%'";
  }
  if ($deal != "") {
    $where .= " and deal_label $like '$deal%'";
  }
  if ($deal_id != "") {
    $where .= " and deal_id = '$deal_id'";
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
      invoicestatus_id, invoicestatus_label as invoice_status
    from Invoice
      left outer join DealInvoice on dealinvoice_invoice_id = invoice_id
      left outer join Deal on dealinvoice_deal_id = deal_id
      left outer join Company on deal_company_id = company_id
      left join InvoiceStatus on invoice_status_id = invoicestatus_id
    $whereq
    $orderq
    $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Invoice $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
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
  $datemodif = sql_date_format($db_type, "invoice_timeupdate", "datemodif");
  $timeupdate = sql_date_format($db_type, "invoice_timeupdate", "timeupdate");
  $timecreate = sql_date_format($db_type, "invoice_timecreate", "timecreate");

  // XXXXX ???? exporter le calcul des paiements a par (pour eviter group by)
  $query = "select Invoice.*,
      $date,
      $datemodif,
      $timeupdate,
      $timecreate,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate,
      sum(paymentinvoice_amount) as invoice_paid
    from Invoice
      left outer join PaymentInvoice on invoice_id=paymentinvoice_invoice_id
      left join UserObm as c on invoice_usercreate=c.userobm_id
      left join UserObm as u on invoice_userupdate=u.userobm_id
    where invoice_id='$p_invoice_id'
    group by Invoice.invoice_id, invoice_timeupdate, invoice_timecreate,
      invoice_userupdate, invoice_usercreate, invoice_number, invoice_label,
      invoice_amount_ht, invoice_amount_ttc, invoice_status_id, invoice_date,
      invoice_comment, invoice_inout, invoice_archive,
      c.userobm_login, u.userobm_login";

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
  $inout = $invoice["inout"];
  $deal = $invoice["param_deal"];

  if ($status == ""){
    $status = "0";
  }

  $query = "insert into Invoice (
      invoice_timeupdate,
      invoice_timecreate,
      invoice_userupdate,
      invoice_usercreate,
      invoice_number,
      invoice_label,
      invoice_amount_ht,
      invoice_amount_ttc,
      invoice_status_id,
      invoice_comment,
      invoice_date,
      invoice_inout
    ) values (
      null,
     '$now',
     null,
     '$uid',
     '$number',
     '$label',
     '$ht', 
     '$ttc',
     '$status',
     '$comment',
     '$date',
     '$inout')"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  // following code is only when creating an invoice from a deal :
  if (isset($deal) && $deal != "") {
    // we need to re-get the id of the invoice freshly inserted
    $new_invoice = run_query_search ($invoice, $new_order, $order_dir);
    $new_invoice->next_record();
    $newid = $new_invoice->f("invoice_id");
    
    $query = "insert into DealInvoice (
        dealinvoice_deal_id,
        dealinvoice_invoice_id,
        dealinvoice_timecreate,
        dealinvoice_usercreate
      ) values (
        '$deal',
        '$newid',
        '$now',
        '$uid'
      )";
    display_debug_msg ($query, $cdg_sql, "asso deal-invoice");
    $obm_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// invoice update 
///////////////////////////////////////////////////////////////////////////////
function run_query_update ($invoice) {
  global $auth, $cdg_sql;

  $now = date("Y-m-d H:i:s");
  $uid = $auth->auth["uid"];

  $label = $invoice["label"];
  $number = $invoice["number"];
  $ht = $invoice["ht"];
  $ttc = $invoice["ttc"];
  $status = $invoice["status"];
  $comment = $invoice["comment"];
  $date = $invoice["date"];
  $inout = $invoice["inout"];
  $invoice_id = $invoice["invoice"];
  if ($status == ""){
    $status="0";
  }

  $query = "update Invoice set
      invoice_timeupdate='$now',
      invoice_userupdate='$uid',
      invoice_number='$number',
      invoice_label='$label',
      invoice_amount_ht='$ht',
      invoice_amount_ttc='$ttc',
      invoice_status_id='$status',
      invoice_comment='$comment',
      invoice_date='$date',
      invoice_inout='$inout'
    where invoice_id='$invoice_id'"; 

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
     order by invoicestatus_id asc";
  display_debug_msg($query, $cdg_sql, "run_query_invoicestatus()");
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// data of deals connecte to $p_invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_search_deal_invoice ($p_invoice) {
  global $cdg_sql;

  $query = "select ".
     " deal_label ".
     ", deal_id ".
     ", tasktype_label ".
     ", company_id as deal_company_id ".
     ", company_name as deal_company_name ".
     ", deal_todo ".
     ", dealtype_label ".
     ", dealtype_label ".
     ", deal_datealarm ".
     ", dealstatus_label ".
     " from Deal, DealInvoice, Invoice , TaskType, DealType, DealStatus ". 
     " , Company".
     " where dealinvoice_invoice_id = invoice_id ".
     " and deal_status_id = dealstatus_id ".
     " and deal_type_id = dealtype_id ".
     " and deal_tasktype_id = tasktype_id ".
     " and deal_id = dealinvoice_deal_id ".
     " and deal_company_id = company_id ".
     " and invoice_id = ".$p_invoice ;

  display_debug_msg("<b>run_query_search_deal_invoice : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// getting all deals of the same kind as $q_invoice,
// but not connected to any invoice
// and not archived (!)
// $p_deal_label = label of the deal to search
// $p_deal_company = deals connected to that company
// $p_deal_archive = 1 means look into archived deals as well
///////////////////////////////////////////////////////////////////////////////
function run_query_deal($q_invoice, $p_deal_label="",$p_deal_company="", $p_deal_archive = 0) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
    
  if (is_object ($q_invoice)) {
    // list all deals of this invoice
    $query = "select
        dealinvoice_deal_id
      from DealInvoice
      where dealinvoice_invoice_id = '".$q_invoice->f("invoice_id")."'";
    display_debug_msg("<b>run_query_deal() : </b>$query", $cdg_sql);
    $obm_q->query($query);
    
    if ($obm_q->nf() > 0){
      $obm_q->next_record();
      $deals_to_skip = $obm_q->f("dealinvoice_deal_id");
      while ($obm_q->next_record ()){
	$deals_to_skip .= ",".$obm_q->f("dealinvoice_deal_id");
      } 
    }
  }

  $query = "select ".
     " deal_id ".
     ", deal_label ".
     ", company_id ".
     ", company_name as deal_company_name ".
     ", deal_todo ".
     ", dealtype_label ".
     ", deal_datealarm ".
     ", dealstatus_label ".
     ", tasktype_label ".
     " from Deal,Company,TaskType,DealType,DealStatus ".
     " where ".
     " deal_tasktype_id = tasktype_id ".
     " and dealtype_id = deal_type_id".
     " and deal_status_id = dealstatus_id ".
     " and dealtype_inout = '".$q_invoice->f("invoice_inout")."' ".
     " and deal_company_id = company_id ";
  $query .= ($p_deal_label == "")? "":"and deal_label $like '$p_deal_label%' ";
  $query .= ($deals_to_skip == "")?"":"and deal_id not in ('$deals_to_skip')";
  if ($p_deal_company != "") {
    $query .= " and company_name $like '$p_deal_company%' ";
  }
  if ($p_deal_archive != 0) {
    $query .= " and deal_archive in ('1','0') ";
  } else {
    $query .= " and deal_archive = 0 ";
  }

  display_debug_msg ("<b>run_query_deal :</b>$query", $cdg_sql);
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
function run_query_search_payment_invoice ($p_invoice, $not_paid=0) {
  // trouble with date format with MySQL
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $connect = new DB_OBM;
  $query = "select ".
     " payment_label ".
     " ,payment_number ";
  if ($connect->type==$db_type_pgsql) {     
    $query .= " ,payment_date ";
  }
  elseif ($connect->type ==$db_type_mysql) {
    $query .= " , UNIX_TIMESTAMP(payment_date) ";
  }
  $query.= " ,payment_amount ".
     " ,payment_id ".
     ", payment_number ".
     ", payment_expected_date as payment_expect_date ".
     " ,paymentinvoice_amount ".
     " from Invoice, PaymentInvoice, Payment ".
     " where invoice_id = paymentinvoice_invoice_id ".
     " and payment_id = paymentinvoice_payment_id ".
     " and invoice_id = ".$p_invoice;
  if ($not_paid == 1) {
    $query .= " and paymentinvoice_amount = 0.0 ";
  } elseif ($not_paid == -1) {
    $query .= " and paymentinvoice_amount <> 0.0 ";
  }
  
  display_debug_msg("<b>run_query_search_payment_invoice</b>$query", $cdg_sql);
  $connect->query($query);
  return $connect;
}


///////////////////////////////////////////////////////////////////////////////
// add a deal to an invoice 
///////////////////////////////////////////////////////////////////////////////
function run_query_add_deal($p_invoice_id, $p_deal_id) {
  global $cdg_sql;

  $query = "insert into DealInvoice (dealinvoice_invoice_id, dealinvoice_deal_id) values('$p_invoice_id', '$p_deal_id')";
  display_debug_msg($query, $cdg_sql);
  $db = new DB_OBM;
  $db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// delete a connection between a deal and an invoice.
// if $p_deal_id == "", delete all data about that invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_remove_deal($p_invoice_id, $p_deal_id="") {
  global $cdg_sql;

  $db = new DB_OBM;
  $query = "delete from DealInvoice where dealinvoice_invoice_id = '$p_invoice_id'";
  if ($p_deal_id != ""){
    $query .= "and  dealinvoice_deal_id='$p_deal_id'";
  }
  display_debug_msg ($query, $cdg_sql);
  $db->query($query);
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
// Delete query construction                                                 //
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($p_invoice_id) {
  global $cdg_sql;
  
  $connect_db=new DB_OBM;
  // suppression des asso avec des deals :
  run_query_remove_deal ($p_invoice_id);
  $query = "delete from Invoice where invoice_id='$p_invoice_id'";
  display_debug_msg($query, $cdg_sql);
  $connect_db->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// get all details about a payment
///////////////////////////////////////////////////////////////////////////////
function run_query_detail_payment ($p_payment_id) {
  global $cdg_sql;

  $db = new DB_OBM;
  $query = "select *, paymentkind_longlabel from Payment,PaymentKind where payment_paymentkind_id = paymentkind_id and payment_id='$p_payment_id'";
  // FIXME : besoin du compte aussi ?
  display_debug_msg($query, $cdg_sql);
  $db->query($query);

  return $db;
}


///////////////////////////////////////////////////////////////////////////////
// returns the details about invoices connected to a particular payment
///////////////////////////////////////////////////////////////////////////////
function run_query_search_invoice_payment ($p_payment_id) {
  global $cdg_sql;

  $db = new DB_OBM;
  $query = "select * from Invoice, PaymentInvoice
      where paymentinvoice_invoice_id=invoice_id
        and paymentinvoice_payment_id='$p_payment_id'";
  display_debug_msg($query, $cdg_sql);
  $db->query($query);
  return  $db;
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


</script>
