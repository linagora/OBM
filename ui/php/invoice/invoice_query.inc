<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_query.inc
//     - Desc : invoice query File
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Account search query :
///////////////////////////////////////////////////////////////////////////////
function run_query_search ($invoice, $p_new_order, $p_order_dir) {
  global $auth, $cdg_sql;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $date_after = $invoice["date_after"];
  $date_before = $invoice["date_before"];
  $inout = $invoice["inout"];
  $company = $invoice["company"];
  $deal = $invoice["deal"];
  $archive = $invoice["archive"];

  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "invoice_date";
  
  $query = "select Invoice.*, Invoice.invoice_id as Id,
      UNIX_TIMESTAMP(Invoice.invoice_date) as date,
      company_id, company_name as invoice_company,
      deal_id, deal_label as invoice_deal,
      invoicestatus_id, invoicestatus_label as invoice_status,
      sum(paymentinvoice_amount) as invoice_paid
    from InvoiceStatus,
      Invoice left outer join PaymentInvoice
        on (Invoice.invoice_id = paymentinvoice_invoice_id),
      Invoice I2 left outer join DealInvoice
        on (dealinvoice_invoice_id = I2.invoice_id)
        left outer join Deal on dealinvoice_deal_id = deal_id
        left outer join Company on deal_company_id = company_id";
  
  $query .= " where Invoice.invoice_invoicestatus_id = invoicestatus_id ".
  " and Invoice.invoice_id = I2.invoice_id ";
  //" and deal_id = dealinvoice_deal_id ";
  //" and deal_company_id = company_id ";

  // fields to use 
  if ($label != ""){
    $query .= " and Invoice.invoice_label like'%$label%' ";
  }
  if ($number != ""){
    $query .= " and Invoice.invoice_number like '%$number%' ";
  }
  if ($amount_HT != ""){
    $query .= " and Invoice.invoice_amount_HT ='$amount_HT' ";
  }
  if ($amount_TTC != ""){
    $query .= " and Invoice.invoice_amount_TTC ='$amount_TTC' ";
  }
  if (($status != '') && ($status != "_TOUT_")){
    $query .= " and invoicestatus_id ='$status' ";
  }
  if ($date_after != ""){
    $query .= " and Invoice.invoice_date >='$date_after' ";
  }
  if ($date_before != ""){
    $query .= " and Invoice.invoice_date <='$date_before' ";
  }
  if (($inout != "")&&($inout!="_TOUT_")){
    $query .= " and Invoice.invoice_inout ='$inout' ";
  }
  if ($company != "") {
    $query .= " and company_name like '$company%' ";
  }
  if ($deal != "") {
    $query .= " and deal_label like '$deal%' ";
  }
  if ($archive != 'y') {
    $query .= " and Invoice.invoice_archive = '0' ";
  }

  $query .= " group by invoice_id ";
  $query .= "order by $order $order_dir";
  
  display_debug_msg ("<b>run_query_search() :</b>$query", $cdg_sql); 
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q;
} 


///////////////////////////////////////////////////////////////////////////////
// Invoice: Select query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_invoice_id) {
  global $cdg_sql, $db_type_mysql,$db_type_pgsql;  

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select *,
       UNIX_TIMESTAMP(Invoice.invoice_date) as date,
       UNIX_TIMESTAMP(invoice_timeupdate) as datemodif,
       UNIX_TIMESTAMP(invoice_timeupdate) as timeupdate,
       UNIX_TIMESTAMP(invoice_timecreate) as timecreate,
       sum(paymentinvoice_amount) as invoice_paid
     from Invoice left outer join PaymentInvoice
          on (invoice_id = paymentinvoice_invoice_id)
     where invoice_id='$p_invoice_id'
     group by paymentinvoice_invoice_id";

  } else if ($connect_db->type == $db_type_pgsql) {
    $query = "select * ".
       ", invoice_timeupdate as datemodif ".
       ", invoice_timeupdate as timeupdate ".
       ", invoice_timecreate as timecreate ".
       ", sum(paymentinvoice_amount) as invoice_paid ".
       " from Invoice left outer join PaymentInvoice ".
       " on (invoice_id = paymentinvoice_invoice_id) ".
       " where invoice_id=".$p_invoice_id.
       " group by paymentinvoice_invoice_id";
  }

  display_debug_msg ("<b>run_query_detail() :</b>$query", $cdg_sql); 
  $connect_db->query($query);
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Insertion query construction
///////////////////////////////////////////////////////////////////////////////
function run_query_insert($invoice) {
  global $auth, $cdg_sql, $new_order, $order_dir;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $comment = $invoice["comment"];
  $date = $invoice["date"];
  $inout = $invoice["inout"];

  if ($status == ""){
    $status = "0";
  }

  $query = "insert into Invoice ".
     "(invoice_timeupdate,".
     " invoice_timecreate,".
     " invoice_userupdate,".
     " invoice_usercreate,".
     " invoice_number,".
     " invoice_label,".
     " invoice_amount_HT,".
     " invoice_amount_TTC,".
     " invoice_invoicestatus_id,".
     " invoice_comment,".
     " invoice_date,".
     " invoice_inout".
     ") values (null,".
     "'".date("Y-m-d H:i:s") ."',".
     "null,".
     $auth->auth["uid"].",".
     "'".$number."',".
     "'".$label."',".
     "'".$amount_HT."',". 
     "'".$amount_TTC."',".
     "'".$status."',".
     "'".$comment."',".
     "'".$date."',".
     "'".$inout."')"; 

  display_debug_msg($query, $cdg_sql);
  $obm_q=new DB_OBM;
  $obm_q->query($query);
  // following code is only when creating an invoice from a deal :
  if (isset($invoice["param_deal"]) && $invoice["param_deal"] != "") {
    // we need to re-get the id of the invoice freshly inserted
    $new_invoice = run_query_search ($invoice, $new_order, $order_dir);
    $new_invoice->next_record();
    $new_invoice_id = $new_invoice->f("invoice_id");
    
    $query = "insert into DealInvoice ".
       "(dealinvoice_deal_id,".
       "dealinvoice_invoice_id,".
       "dealinvoice_timecreate,".
       "dealinvoice_usercreate)".
       "values ".
       "('".$invoice["param_deal"]."',".
       "'".$new_invoice_id."',".
       "'".date("Y-m-d H:i:s")."',".
       "'".$auth->auth["uid"]."')";
    display_debug_msg ("<b>asso deal-invoice :</b>$query", $cdg_sql);
    $obm_q->query($query);
  }
}


///////////////////////////////////////////////////////////////////////////////
// invoice update 
///////////////////////////////////////////////////////////////////////////////
function run_query_update ($invoice) {
  global $auth, $cdg_sql;

  $label = $invoice["label"];
  $number = $invoice["number"];
  $amount_HT = $invoice["HT"];
  $amount_TTC = $invoice["TTC"];
  $status = $invoice["status"];
  $comment = $invoice["comment"];
  $date = $invoice["date"];
  $inout = $invoice["inout"];
  $invoice_id = $invoice["invoice"];
  // FIXME PARAMS :
  if ($status == ""){
    $status="0";
  }

  $query = "update Invoice set ".
     "invoice_timeupdate='".date("Y-m-d H:i:s")."', ".
     "invoice_userupdate='".$auth->auth["uid"]."', ".
     "invoice_number='".$number."', ".
     "invoice_label='".$label."', ".
     "invoice_amount_HT='".$amount_HT."', ".
     "invoice_amount_TTC='".$amount_TTC."', ".
     "invoice_invoicestatus_id='".$status."', ".
     "invoice_comment='".$comment."', ".
     "invoice_date='".$date."', ".
     "invoice_inout='".$inout."' ".
     "where invoice_id='".$invoice_id."'"; 

  display_debug_msg("<b>run_query_update():</b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
} 


///////////////////////////////////////////////////////////////////////////////
// getting invoice status data
///////////////////////////////////////////////////////////////////////////////
function run_query_invoicestatus() {
  global $cdg_sql;

  $query = "select invoicestatus_id, ".
     "invoicestatus_label ".
     "from InvoiceStatus ".
     "order by invoicestatus_id asc";
  display_debug_msg("<b>run_query_invoicestatus():</b>$query", $cdg_sql);
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

  $go = new DB_OBM;
    
  if (is_object ($q_invoice)) {
    // list all deals of this invoice
    $query = "select ".
    " dealinvoice_deal_id ".
    " from DealInvoice ".
    " where dealinvoice_invoice_id = '".$q_invoice->f("invoice_id")."'";
    display_debug_msg("<b>run_query_deal() : </b>$query", $cdg_sql);
    $go->query($query);
    
    if ($go->nf() > 0){
      $go->next_record();
      $deals_to_skip = $go->f("dealinvoice_deal_id");
      while ($go->next_record ()){
	$deals_to_skip .= ",".$go->f("dealinvoice_deal_id");
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
  $query .= ($p_deal_label == "")? "":"and deal_label like '$p_deal_label%' ";
  $query .= ($deals_to_skip == "")?"":"and deal_id not in ('$deals_to_skip')";
  if ($p_deal_company != "") {
    $query .= " and company_name like '$p_deal_company%' ";
  }
  if ($p_deal_archive != 0) {
    $query .= " and deal_archive in ('1','0') ";
  } else {
    $query .= " and deal_archive = 0 ";
  }

  display_debug_msg ("<b>run_query_deal :</b>$query", $cdg_sql);
  $go->query($query);
  
  return $go;
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

/*
// récupération de tous les paiements de même type que $q_invoice mais
// non entièrement concernés par une facture :
// getting all payments of the same kind as $q_invoice, but
// not completly connected with invoices
function run_query_payment ($q_invoice, $p_payment_label="") { 
  // trouble with date format with MySQL 
  global $db_type_mysql, $db_type_pgsql, $cdg_sql; 

  $go = new DB_OBM; 

  if (is_object($q_invoice)) { 
    // listing all invoice payments 
    $query = "select ". 
       "paymentinvoice_payment_id from PaymentInvoice ". 
       " where paymentinvoice_invoice_id = '".$q_invoice->f("invoice_id")."'"; 
    $go->query($query); 

    // les payments à ne pas ramener sont ceux qui sont
    // entièrement payés!! donc cette requête n'est pas bonne...
    if ($go->nf()>0) { 
      $go->next_record (); 
      // pour chaque payment de la facture,
      // calculer la somme des morceaux répartis sur des factures,
      // récupérer le montant total du payment
      // si les deux sont !=, on garde, 
      // sinon on vire...
      $payments_to_skip = $go->f("paymentinvoice_payment_id"); 
      while ($go->next_record ()) { 
	$payments_to_skip .= ",".$go->f("paymentinvoice_payment_id"); 
      } 
    } 
  } 
  $query = "select ". 
     " payment_id as invoice_pay_id". 
     ",payment_label as invoice_pay_label". 
     ",payment_number as invoice_pay_number"; 
  if ($go->type==$db_type_pgsql) {      
    $query .= " ,payment_date as invoice_pay_date "; 
      } 
  elseif ($go->type ==$db_type_mysql) { 
    $query .= " ,UNIX_TIMESTAMP(payment_date) as invoice_pay_date "; 
  } 
  $query.= ",payment_amount as invoice_pay_amount". 
     " from Payment where ". 
     " payment_inout = '".$q_invoice->f("invoice_inout")."'";
  $query .= ($p_payment_label == "")? "":"and payment_label like '".$p_payment_label."%' ";
  $query .= ($payments_to_skip == "")?"":"and payment_id not in (".$payments_to_skip.")";

  display_debug_msg ($query, $cdg_sql);
  $go->query($query);

  return $go;
}// run_query_payment
*/


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


</SCRIPT>
