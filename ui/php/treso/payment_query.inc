<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_query.inc 
//     - Desc : payment query File
// 2001-08-21 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// search for the specified payment in the database
///////////////////////////////////////////////////////////////////////////////
function run_query_search($p_payment, $p_new_order="") {
  global $auth, $cdg_sql;

  $label = $p_payment["label"];
  $number = $p_payment["number"];
  $amount = $p_payment["amount"];
  $date_after = $p_payment["date_after"];
  $date_before = $p_payment["date_before"];
  $inout = $p_payment["inout"];
  $kind = $p_payment["kind"];
  $account = $p_payment["account"];
  $paid = $p_payment["paid"];
  $deal = $p_payment["deal"];
  $company = $p_payment["company"];
  $checked = $p_payment["checked"];

  $go = new DB_OBM;
  $order = (strcmp($p_new_order,"")!=0) ? $p_new_order : "payment_label";
  // attention : 
  // if any payment already in the database hase payment_paymentkind_id == -1 
  // or null, no payment kind is defined and the join between 
  // PaymentKind and Payment will return 0 rows
  // same problem with payment_account_id...
  // a payment not yet banked can have no kind, no account...
  // we do with an left outer join
  $query = "select Payment.payment_id,
    Payment.payment_label,
    Payment.payment_amount,
    Payment.payment_number, 
    Payment.payment_inout, 
    Payment.payment_date,
    UNIX_TIMESTAMP(Payment.payment_date) as date,
    UNIX_TIMESTAMP(Payment.payment_expected_date) as expect_date,
    Payment.payment_expected_date as payment_expect_date".
  // expect_date, without 'ed' because display_fieldname is varchar(20)...
  ", Payment.payment_paymentkind_id".
  ", paymentkind_shortlabel, paymentkind_longlabel ".
  ", account_id, account_label as payment_account_lbl"; //lbl == label...

  $query .= " from Payment ".
  " left outer join Account on (Payment.payment_account_id = account_id) ,".
  " Payment P2 ".
  " left outer join PaymentKind on (paymentkind_id = P2.payment_paymentkind_id) ";
  if ($deal != "" || $company != "") {
    $query .= ", Payment P4, PaymentInvoice, Invoice, DealInvoice, Deal ";
    if ($company != "") {
      $query .= ", Company ";
    }
  }
  $query .= " where  Payment.payment_id = P2.payment_id ";
  // search criterias :
  if ($label) {
    $query .= " and Payment.payment_label like '$label%'";
  }
  if ($number) {
    $query .= " and Payment.payment_number like '%$number%'";
  }
  if ($amount) {
    $query .= " and Payment.payment_amount ='$amount'";
  }
  if ($date_after) {
    $query .= " and Payment.payment_date >='$date_after'";
  }
  if ($date_before) {
    $query .= " and Payment.payment_date <='$date_before'";
  }
  if (($inout != "")&&($inout != "_TOUT_")) {
    $query .= " and Payment.payment_inout ='$inout'";
  }
  if (($paid != "")&&($paid != "_TOUT_")) {
    $query .= " and Payment.payment_paid = '$paid'";
  }
  if (($kind != "")&&($kind!="-1")) {
    $query .= " and Payment.payment_paymentkind_id ='$kind'";
  }
  if (($account != "")&&($account != "-1")) {
    $query .= " and Payment.payment_account_id ='$account'";
  }
  if ($deal != "" || $company != "" ) {
    $query .= " and dealinvoice_deal_id = deal_id ".
    " and dealinvoice_invoice_id = invoice_id ".
    " and paymentinvoice_invoice_id = invoice_id ".
    " and paymentinvoice_payment_id = P4.payment_id ".
    " and Payment.payment_id = P4.payment_id ".
    " and deal_label like '$deal%'";
    if ($company != "") {
      $query .= " and deal_company_id = company_id ".
	 " and company_name like '$company%'";
    }
  }
  if ($checked != 'y') {
    $query .= " and Payment.payment_checked = '0' ";
  }

  $query .= " order by ".$order;

  display_debug_msg ("<b>run_query_search() : </b>$query", $cdg_sql);

  $go->query($query);

  return ($go);
}


///////////////////////////////////////////////////////////////////////////////
// creation of a new payment : database insertion
// the new payment has payment_amount_used = 0 et is not connected to
// any invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_insert ($p_payment) {
  global $auth, $cdg_sql;

  $label = $p_payment["label"];
  $amount = $p_payment["amount"];
  $expected_date = $p_payment["expected_date"];
  $inout = $p_payment["inout"];
  $comment = $p_payment["comment"];
  // number == "" => no number so null...
  $number = ($p_payment["number"] == "") ? "null" : "'".$p_payment["number"]."'";
  // $payment["account"] == -1 means no account selected on insertion page
  $account = ($p_payment["account"] == "-1") ? "null" : "'".$p_payment["account"]."'" ;
  // $payment["kind"] == -1 means no kind selected on insertion page
  $kind = ($p_payment["kind"] == "-1") ? "null" : "'".$p_payment["kind"]."'" ;
  $date = ($p_payment["date"] == "") ? "null" : "'".$p_payment["date"]."'";

  $query = "insert into Payment (".
     "payment_timeupdate, payment_timecreate, ".
     "payment_userupdate, payment_usercreate, ".
     "payment_label, payment_number, ".
     "payment_amount, payment_expected_date, ".
     "payment_date, payment_paymentkind_id, ".
     "payment_comment, payment_inout, ".
     "payment_account_id, payment_paid) ".
     "values (".
     "0,'".date("Y-m-d H:i:s")."',".
     "null,'".$auth->auth["uid"]."',".
     "'".$label."',".$number.",".
     "'".$amount."','".$expected_date."',".
     $date.",".$kind.",".
     "'".$comment."','".$inout."',".$account.",".
     "'0')";
  
  display_debug_msg ("<b>run_query_insert() :</b>$query", $cdg_sql);
  
  $db = new DB_OBM;
  $db->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// retourne tous les types de payments :
///////////////////////////////////////////////////////////////////////////////
function run_query_paymentkind () {
  global $cdg_sql;

  $query = "select * from PaymentKind order by paymentkind_longlabel";

  display_debug_msg ("<b>run_query_paymentkind() :</b>$query", $cdg_sql);
  $db = new DB_OBM;
  $db->query($query);
  return $db;
}


///////////////////////////////////////////////////////////////////////////////
// retourne tous les comptes :
///////////////////////////////////////////////////////////////////////////////
function run_query_account () {
  global $cdg_sql;

  $query = "select * from Account order by account_label";

  display_debug_msg ("<b>run_query_account() : </b>$query", $cdg_sql);
  $db = new DB_OBM;
  $db->query($query);

  return $db;
}


///////////////////////////////////////////////////////////////////////////////
// récupération de tous les champs du payment
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_payment_id) {
  global $db_type_mysql, $db_type_pgsql, $cdg_sql;

  $go = new DB_OBM;

  if ($go->type == $db_type_mysql){
    $query = "select *,
      UNIX_TIMESTAMP(Payment.payment_date) as date,
      UNIX_TIMESTAMP(Payment.payment_expected_date) as expect_date,
      UNIX_TIMESTAMP(payment_timeupdate) as datemodif,
      UNIX_TIMESTAMP(payment_timeupdate) as timeupdate,
      UNIX_TIMESTAMP(payment_timecreate) as timecreate,
      sum(paymentinvoice_amount) as pay_inv_paid
    from Payment left outer join PaymentInvoice
         on (payment_id=paymentinvoice_payment_id)
    where payment_id = '$p_payment_id'";
  } elseif ($go->type == $db_type_pgsql) { 
    $query = "select * ".
       ", payment_timeupdate as datemodif ".
       ", payment_timeupdate as timeupdate ".
       ", payment_timecreate as timecreate ".
       ", sum(paymentinvoice_amount) as pay_inv_paid ".
       " from Payment  left outer join PaymentInvoice ".
       " on (payment_id=paymentinvoice_payment_id)".
       " where payment_id =".$p_payment_id;
  }

  $query .= " group by paymentinvoice_invoice_id ";

  display_debug_msg ("<b>run_query_detail() : </b>$query", $cdg_sql);

  $go->query($query);
  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// mise à jour de payments 
// if $banking == 1, we are banking and we set payment_paid to 1.
///////////////////////////////////////////////////////////////////////////////
function run_query_update ($p_payment, $banking=0) {
  global $auth, $cdg_sql;

  $label = $p_payment["label"];
  $amount = $p_payment["amount"];
  $date = $p_payment["date"];
  $expected_date = $p_payment["expected_date"];
  $inout = $p_payment["inout"];
  $comment = $p_payment["comment"];
  $payment = $p_payment["payment"];
  // number == "" => no number so null...
  $number = ($p_payment["number"] == "") ? "null" : "'".$p_payment["number"]."'" ;
  // $payment["account"] == -1 means no account selected on insertion page
  $account = ($p_payment["account"] == "-1") ? "null" : "'".$p_payment["account"]."'" ;
  // $payment["kind"] == -1 means no kind selected on insertion page
  $kind = ($p_payment["kind"] == "-1") ? "null" : "'".$p_payment["kind"]."'" ;
  $date = ($p_payment["date"] == "") ? "null" : "'".$p_payment["date"]."'";
  
  $go = new DB_OBM;
  
  $query = "update Payment set ".
     "payment_timeupdate='".date("Y-m-d H:i:s")."',".
     "payment_userupdate='".$auth->auth["uid"]."', ".
     "payment_label ='$label', ".
     "payment_amount ='".$amount."', ".
     "payment_number =".$number.", ".
     "payment_expected_date='".$expected_date."', ".
     "payment_date =".$date.", ".
     "payment_paymentkind_id =".$kind.", ".
     "payment_comment='".$comment."', ".
     "payment_account_id=".$account." ";
  if ($banking == 1) {
    $query.= ", payment_paid = '1' ";
  }
  
  $query .= "where payment_id = '".$payment."'";
  
  display_debug_msg ("<b>run_query_update() : </b>$query", $cdg_sql);
  
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// suppression du payment p_payment_id (et toutes les lignes le concernant
// dans la table PaymentInvoice
///////////////////////////////////////////////////////////////////////////////
function run_query_delete ($p_payment_id){
  global $cdg_sql;

  $go = new DB_OBM;
  // suppression des associations payment-Invoice
  $query = "delete from PaymentInvoice where paymentinvoice_payment_id = '".$p_payment_id."'";
  $go->query($query);
  $query = "delete from Payment where payment_id = '".$p_payment_id."'";
  $go->query($query);

  display_debug_msg ("<b>run_query_delete() : </b>$query", $cdg_sql);

  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// search and return all invoices of the same "inout" as $payment
// and which are not completely paid...
// $payment is the usual hash, contains all fields or almost all
// if $payment["invoice_label"] is set, we use it as a search criteria
// if $payment["invoice_company"] is set, it is also used
///////////////////////////////////////////////////////////////////////////////
function run_query_search_connectable_invoices ($payment) {
  global $cdg_sql;

  $go = new DB_OBM;

  // we must exclude every invoice already connected 
  // to that payment if they are paid :
  $query = "select paymentinvoice_invoice_id ".
     "from PaymentInvoice ".
     "where paymentinvoice_payment_id ='".$payment["payment"]."'".
     " and paymentinvoice_amount = '0.0'";
  display_debug_msg ("<b>run_query_search_connectable_invoices() : </b>$query", $cdg_sql);
  $go->query($query);

  if ($go->nf() > 0) {
    $go->next_record();
    $invoices_to_skip = $go->f("paymentinvoice_invoice_id");
    while ($go->next_record()) {
      $invoices_to_skip .= ",".$go->f("paymentinvoice_invoice_id");
    }
  }

  $query = "select invoice_id,
      invoice_label,
      invoice_number,
      invoice_amount_TTC,
      invoice_amount_HT,
      invoice_date,
      invoice_inout,
      invoice_invoicestatus_id,
      invoicestatus_label as invoice_status,
      UNIX_TIMESTAMP(invoice_date) as date,
      sum(paymentinvoice_amount),
      company_name as invoice_company,
      company_id,
      deal_id,
      deal_label as invoice_deal
    from 
      InvoiceStatus, DealInvoice, Deal, Company,
      Invoice left outer join PaymentInvoice
              on (invoice_id = paymentinvoice_invoice_id) ".
     // invoicestatus == 1 => not completely paid
     // invoicestatus == 4 => trouble on that one...
     " where invoice_invoicestatus_id in ('1','4') ".
     " and invoicestatus_id = invoice_invoicestatus_id ".
     " and invoice_id = dealinvoice_invoice_id ".
     " and deal_id = dealinvoice_deal_id ".
     " and company_id = deal_company_id ".
     " and invoice_inout = '".$payment["inout"]."'";
  if ($payment["invoice_company"] != "") {
    $query .= " and company_name like '".$payment["invoice_company"]."%'";
  }
  if ($payment["invoice_label"] != "") {
    $query.= " and invoice_label like '%".$payment["invoice_label"]."%'";
  }
  if ($go->nf() > 0) {
    $query .= " and invoice_id not in (".$invoices_to_skip.")";
  }
  
  $query .= " group by invoice_id ";

  display_debug_msg ("<b>run_query_search_connectable_invoices () : </b>$query", $cdg_sql);

  $go->query($query);

  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// returns all invoices connected to the $payment.
// if not_paid = 1, we return only not paid invoices
///////////////////////////////////////////////////////////////////////////////
function run_query_search_connected_invoices ($p_payment_id, $not_paid=0) {
  global $cdg_sql;

  $go = new DB_OBM;
  
  $query = " select invoice_id,
      invoice_label,
      invoice_number,
      invoice_amount_HT,
      invoice_amount_TTC,
      invoice_date,
      invoice_inout,
      invoice_invoicestatus_id,
      invoicestatus_label as invoice_status,
      UNIX_TIMESTAMP(invoice_date) as date,
      sum(paymentinvoice_amount),
      company_id,
      company_name as invoice_company,
      deal_id,
      deal_label as invoice_deal
   from PaymentInvoice, Company, Deal, InvoiceStatus, ".
     " Invoice left outer join DealInvoice ".
     " on invoice_id = dealinvoice_invoice_id".
     " where invoice_id = paymentinvoice_invoice_id ".
     " and invoice_invoicestatus_id = invoicestatus_id ".
     " and deal_id = dealinvoice_deal_id ".
     " and company_id = deal_company_id ".
     " and paymentinvoice_payment_id = '".$p_payment_id."'";
  if ($not_paid == 1){
    $query .= " and paymentinvoice_amount = '0.0' ";
  }
  $query.=" group by paymentinvoice_invoice_id ";

  display_debug_msg ("<b>run_query_search_connected_invoices() : </b>$query", $cdg_sql);

  $go->query($query);
  return $go;
}


///////////////////////////////////////////////////////////////////////////////
//  return all interesting data concerning a particular invoice
// including amount not paid yet
///////////////////////////////////////////////////////////////////////////////
function run_query_invoice_detail ($p_invoice_id) {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select invoice_id, invoice_number ".
     ", invoice_label, invoice_amount_TTC ".
     ", invoice_date, invoicestatus_label ".
     ", invoice_amount_TTC - sum(paymentinvoice_amount) as invoice_not_paid_yet ".
     " from InvoiceStatus, Invoice left outer join PaymentInvoice ".
     " on (invoice_id = paymentinvoice_invoice_id) ".
     " where invoice_invoicestatus_id = invoicestatus_id ".
     " and invoice_id = $p_invoice_id".
     " group by paymentinvoice_invoice_id ";

  display_debug_msg ("<b>run_query_invoice_detail() : </b>$query", $cdg_sql);
  $go->query($query);
  
  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// payment banking check :
// $p_payment : payment to bank
// $p_invoices : invoices involved in the banking
// return $error, error msg identifying the error
///////////////////////////////////////////////////////////////////////////////
function check_banking_data ($p_payment, $p_invoices) {
  global $l_too_much, $l_not_enough, $l_trouble_invoice;

  $error = "";
  // we do different checks, depending if the user selected 
  // invoices or not 
  if ($p_payment["nb_invoices"] != 0) {

    // are there any invoices that received too much money ?
    foreach ($p_invoices as $invoice) {
      if ($invoice["still_not_paid"] < 0) {
	$error = $l_trouble_invoice;
      }
    }

    // the whole amount of the payment must be used
    if ($p_payment["amount"] < $p_payment["used_amount"]) {
      $error = $l_too_much;
    } elseif ($p_payment["amount"] > $p_payment["used_amount"]) {
      $error = $l_not_enough;
    }
  } else {
    // no invoices selected, meaning no checks
    // the whole amount is used, and no update of Invoice
    // or PaymentInvoice needed...
    $error = 0;
  }

  return $error;
}


//////////////////////////////////////////////////////////////////////////
// run_query_do_banking :
// updates the dbase with data concerning the bnaking of $p_payment
// using all $p_invoices
//////////////////////////////////////////////////////////////////////////
function run_query_do_banking ($p_payment, $p_invoices) {

  // if no invoices connected to $p_payment,
  // we use the whole amount...
  if ($p_payment["nb_invoices"] == 0) {
    $p_payment["used_amount"] = $p_payment["amount"];
  } else {
    // to update the invoices, it's a little bit tricky
    foreach ($p_invoices as $invoice) {
      // first, we change Invoice data :
      // status and time/user update
      run_query_bank_invoice ($invoice);
      
      // then we add a line in PaymentInvoice
      run_query_bank_paymentinvoice ($p_payment, $invoice);
    }
  }

  // to update the payment, we use that :
  run_query_update ($p_payment,1);
  // now we update the balance of the connected account :
  run_query_bank_account ($p_payment);
  
}


///////////////////////////////////////////////////////////////////////////////
// update the invoice in the db after a payment banking...
// $invoice = data concerning the banking
///////////////////////////////////////////////////////////////////////////////
function run_query_bank_invoice($p_invoice) {
  global $auth, $cdg_sql;

  $go = new DB_OBM;
  
  if ($p_invoice["still_not_paid"] > 0) {
    $new_status = 1 ; // ie. created
  } elseif ($p_invoice["still_not_paid"] < 0) {
    $new_status = 4; //ie. trouble
  } else {
    $new_status = 2; //ie. paid
  }

  $query = "update Invoice set ".
     " invoice_invoicestatus_id = '".$new_status."',".
     " invoice_userupdate = '".$auth->auth["uid"]."', ".
     " invoice_timeupdate = '".date("Y-m-d H:i:s")."' ".
     " where invoice_id = '".$p_invoice["invoice_id"]."'";

  display_debug_msg ("<b>run_query_bank_invoice() :</b>$query", $cdg_sql);
  $go->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// add a line in PaymentInvoice concerning 
// $p_payment and $p_invoice
///////////////////////////////////////////////////////////////////////////////
function run_query_bank_paymentinvoice ($p_payment, $p_invoice) {
  // in fact, 2 possibilities :
  // payment and invoice are not connected yet => we insert
  // payment and invoice are already connected => we update
  // in the later anyway, amount can only be 0.0, so we
  // don't need any computation...

  global $auth, $cdg_sql;

  $go = new DB_OBM;

  if (payment_invoice_are_connected($p_payment["payment"], 
  $p_invoice["invoice_id"])) {
    $query = "update PaymentInvoice ".
       " set paymentinvoice_amount = '".$p_invoice["shares"]."'".
       ", paymentinvoice_userupdate ='".$auth->auth["uid"]."'".
       ", paymentinvoice_timeupdate = '".date("Y-m-d H:i:s")."'".
       " where paymentinvoice_payment_id = '".$p_payment["payment"]."'".
       " and paymentinvoice_invoice_id = '".$p_invoice["invoice_id"]."'";
  } else {
    $query = "insert into PaymentInvoice values (".
       " '".$p_invoice["invoice_id"]."'".
       ",'".$p_payment["payment"]."'".
       ",'".$p_invoice["shares"]."'".
       ",''". // timeupdate
       ",'".date("Y-m-d H:i:s")."' ".// timecreate
       ",'".$auth->auth["uid"]."'".
       ",''". // userupdate
       ");";
  }

  display_debug_msg ("<b>run_query_bank_paymentinvoice() :</b>$query", $cdg_sql);
  $go->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// update the account connected to the payment banked
///////////////////////////////////////////////////////////////////////////////
function run_query_bank_account ($p_payment) {
  global $cdg_sql;

  $go = new DB_OBM;
  
  if ($p_payment["inout"] == '+') {
    $operation = " + ".$p_payment["amount"];
  } else {
    $operation = " - ".$p_payment["amount"];
  }
  $query = " update Account ".
     " set account_today = account_today ".$operation.
     " where account_id = '".$p_payment["account"]."'";

  display_debug_msg ("<b>run_query_bank_account : </b>$query", $cdg_sql);
  $go->query($query);

}


///////////////////////////////////////////////////////////////////////////////
// run_query_link_payment_to_invoice ()
// connects payment to invoice, without any amount...
///////////////////////////////////////////////////////////////////////////////
function run_query_link_payment_to_invoice ($payment, $p_invoice_id) {
  global $auth, $cdg_sql;

  $go = new DB_OBM;

$query = "insert into PaymentInvoice ".
   "(paymentinvoice_invoice_id,".
   " paymentinvoice_payment_id, ".
   " paymentinvoice_amount,".
   " paymentinvoice_timeupdate,".
   " paymentinvoice_timecreate,".
   " paymentinvoice_usercreate,".
   " paymentinvoice_userupdate)".
   "values (".
   "'".$p_invoice_id."'," .
   "'".$payment["payment"]."',".
   "'0.0',".
   "'',".
   "'".date("Y-m-d H:i:s")."',".
   "'".$auth->auth['uid']."',".
   "'')";

  display_debug_msg ("<b>run_query_link_payment_to_invoice() :</b>$query", $cdg_sql);
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// returns true if $p_invoice_id and $p_payment_id are already connected,
// or false...
///////////////////////////////////////////////////////////////////////////////
function payment_invoice_are_connected ($p_payment_id, $p_invoice_id) {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select * from PaymentInvoice ".
     " where paymentinvoice_payment_id =".$p_payment_id.
     " and paymentinvoice_invoice_id =".$p_invoice_id;
  display_debug_msg ("<b>payment_invoice_are_connected() :</b>$query", $cdg_sql);
  $go->query($query);
  
  display_debug_msg ("payment $p_payment_id et facture $p_invoice_id ".$go->nf(), $cdg_sql);

  return ($go->nf() != 0);
}


///////////////////////////////////////////////////////////////////////////////
// delete the link between $p_payment_id and $p_invoice_id
///////////////////////////////////////////////////////////////////////////////
function payment_invoice_unlink ($p_payment_id, $p_invoice_id) {
  global $cdg_sql;

  display_debug_msg("unlinking payment <b>$p_payment_id</b> and invoice <b>$p_invoice_id</b>", $cdg_sql);

  $go = new DB_OBM;
  $query = "delete from PaymentInvoice ".
     " where ".
     " paymentinvoice_payment_id = '".$p_payment_id."' ".
     " and paymentinvoice_invoice_id = '".$p_invoice_id."'";

  display_debug_msg ("<b>payment_invoice_unlink :</b>$query", $cdg_sql);
  
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// unpays the invoice $p_invoice_id
///////////////////////////////////////////////////////////////////////////////
function payment_unpay_invoice ($p_invoice_id) {
  // unpay an invoice : 
  // set its status to "created"
  global $cdg_sql;

  $go = new DB_OBM;

  $query = " update Invoice ".
     "set invoice_invoicestatus_id = 1 ".
     " where invoice_id = $p_invoice_id";
  display_debug_msg ("<b>payment_unpay_invoice :</b>$query", $cdg_sql);

  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// unpays the payment $p_payment_id
///////////////////////////////////////////////////////////////////////////////
function payment_unpay ($p_payment_id) {
  // unpay a payment 
  // set its payment_paid to '0',
  // and its date to null
  // substract its amount from concerned account balance
  global $cdg_sql;

  $go = new DB_OBM;

  // fisrt, get the account :
  $query = "select payment_account_id, payment_amount from Payment ".
     " where payment_id = $p_payment_id";
  display_debug_msg ("<b>payment_unpay() :</b>$query", $cdg_sql);
  $go->query($query);
  $go->next_record();
  $amount = $go->f("payment_amount");
  $account = $go->f("payment_account_id");

  $query = "update Payment ".
     " set payment_paid = '0'".
     ", payment_date = null ".
     " where payment_id = $p_payment_id";
  display_debug_msg ("<b>payment_unpay() :</b>$query", $cdg_sql);
  $go->query($query);

  $query = "update Account ".
     " set account_today = account_today - $amount ".
     " where account_id = $account";
  display_debug_msg ("<b>payment_unpay() :</b>$query", $cdg_sql);
  $go->query($query);

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
// alters the file named $orig and put the result in a $dest named file.
// this function is Aliacom specific, edit it to fit your needs...
// if one of the files is not availabel for reading/writing, we die...
// 
// Actually, his function only suppresses lines starting with ;) or ;
// NON : en fait, on compte les champs et on vire les lignes qui n'en
// contiennent pas le nombre exact (ici, 5 chps, soit 5 ';')
///////////////////////////////////////////////////////////////////////////////
function get_csv_ready ($orig, $dest) {
  global $l_file_error, $l_write_error, $l_read_error;
  // size of the input buffer 
  $MAX_SIZE = 8192;
  // number of lines to skip at the top of the file
  $NB_LINES = 7;

  $fd_orig = fopen($orig, "r");
  if ($fd_orig == false) {
    die ($l_file_error.$orig."dead");
  }
  $fd_dest = fopen ($dest,"w");
  if ($fd_dest == false) {
    die ($l_file_error.$dest."dead");
  }

  // we skip as many lines as we were told :
  for ($cpt = 0; !feof ($fd_orig) && $cpt < $NB_LINES; $cpt++) {
    $trash = fgets ($fd_orig, $MAX_SIZE);
  }
  
  // we copy lines from the orig file to the dest, but
  // we replace date dd/mm/aaaa by aaaa-mm-dd
  // when we face lines that have less then 5 fields,
  // we cat them to the previous one
  $buffer = "";
  while (!feof ($fd_orig)) {
    // read a line
    $line = fgets ($fd_orig, $MAX_SIZE);
    // if it has 5 fields :
    if (substr_count ($line, ";") == 5) {
      // we write the previous line (if not empty) in the dest file :
      if ($buffer != "" ){
	fputs($fd_dest, $buffer);
	$buffer = "";
      }
      // we modify date format and keep result aside :
      $buffer=ereg_replace("([0-9][0-9])/([0-2][0-9])/([0-9][0-9][0-9][0-9])",
			   "\\3-\\2-\\1", $line);
    } else {
      // we delete "\r\n" at the end of $buffer, cause we want $buffer
      // to look like a single line :
      $buffer = rtrim ($buffer);
      // some of the lines we process start with ";)". we remove those signs :
      $line = str_replace (";)"," ", $line);
      // we replace ; with ' ' and cat to the current line :
      $buffer .= str_replace (";"," ",$line);
    }
  }
  // we don't forget the last line :
  fputs($fd_dest, $buffer);
  
  fclose ($fd_orig);
  fclose ($fd_dest);
} // get_csv_ready ()


///////////////////////////////////////////////////////////////////////////////
// import the bank data from the file named $file into the db table EntryTemp
// drops the temporary table used to store payments later....
///////////////////////////////////////////////////////////////////////////////
function run_query_import_csv ($file) {
  global $cdg_sql;

  $go = new DB_OBM;
  $go->query("delete from EntryTemp");
  $go->query("delete from PaymentTemp");

  $query = "LOAD DATA INFILE '$file'".
     " INTO TABLE EntryTemp ".
     " FIELDS TERMINATED BY ';' ".
     " ESCAPED BY '\\\\' ".
     //" OPTIONALLY ENCLOSED BY '\"' ".
     " LINES TERMINATED BY '\\r\\n' ".
     //     " IGNORE 7 LINES ".
     " (entrytemp_date, entrytemp_type, entrytemp_amount, ".
     " entrytemp_realdate, entrytemp_label, entrytemp_comment)";


  display_debug_msg ("<b>run_query_import_csv () : </b>$query", $cdg_sql);
  $go->query($query);
 
  // we also retrieve all payments concerned
  $another_go = new DB_OBM;
  $query_cp = " insert into PaymentTemp ".
     " select * from Payment ".
     " where payment_paid = '1' and payment_checked = '0'";
  display_debug_msg ("<b>run_query_import_csv() :</b>$query_cp", $cdg_sql);
  $another_go->query ($query_cp);
}


///////////////////////////////////////////////////////////////////////////////
// returns all entrytemp lines
///////////////////////////////////////////////////////////////////////////////
function run_query_get_entrytemp() {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select * from EntryTemp where entrytemp_checked = '0' ".
     " order by entrytemp_date ";
  display_debug_msg ("<b>run_query_get_entrytemp() :</b>$query", $cdg_sql);

  $go->query($query);
  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// returns all payments from the newly created table, needed ??
///////////////////////////////////////////////////////////////////////////////
function run_query_get_payments_to_reconcile() {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select * from PaymentTemp where payment_checked = '0' ".
     " order by payment_date ";
  display_debug_msg ("<b>run_query_get_payments_to_reconcile() :</b>$query", $cdg_sql);
  $go->query ($query);

  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// the 2 next functions check an entrytemp (resp. a payment) given its id
///////////////////////////////////////////////////////////////////////////////
function run_query_check_entrytemp ($et_id) {
  global $cdg_sql;

  $go = new DB_OBM;
  $query = " update EntryTemp set entrytemp_checked = '1' ".
     " where entrytemp_id = '".$et_id."'";
  display_debug_msg("<b>run_query_check_entrytemp():</b>$query", $cdg_sql);
  $go->query($query);
}


function run_query_check_payment ($pay_id) {
  global $cdg_sql;

  $go = new DB_OBM;
  $query = " update PaymentTemp set payment_checked = '1' 
             where payment_id='$pay_id'";
  display_debug_msg("<b>run_query_check_payment() : </b>$query", $cdg_sql);
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// this function checks the selected payments and entrytemp selected by the 
// user.
// if there are no problems, db is modified and true is returned
// else returns 0 and doesn't modify tables...
// $et_ids = array containing entrytemp_id of selected ones
// $pay_ids = same thing but for payments
///////////////////////////////////////////////////////////////////////////////
function check_reconcile ($pay_ids, $et_ids) {
  //string implode (string glue, array pieces)
  // first we turn the 2 arrays en 2 strings we'll be able to use
  // in a DB query :
  global $cdg_param;

  $pay_id_list = implode (",",$pay_ids);
  $et_id_list = implode (",", $et_ids);

  // than we retrieve data from the .... database ?
  $q_et = run_query_detail_et ($et_id_list);
  $q_pay = run_query_detail_payments ($pay_id_list);

  display_debug_msg ("pay_id_list=$pay_id_list<br>et_id_list=$et_id_list", $cdg_param);

  // il faut : faire la somme des P+, des P-, des et+ et et-,
  // les comparer 2 à 2 et vérifier qu'il y a égalité...
  $pay_in = $pay_out = 0.0;
  $et_in = $et_out = 0.0;
  while ($q_pay->next_record ()) {
    if ($q_pay->f("payment_inout") == '+'){
      $pay_in += $q_pay->f("payment_amount");
    } else {
      $pay_out += $q_pay->f("payment_amount");
    }
  }
  while ($q_et->next_record ()) {
    if ($q_et->f("entrytemp_amount") > 0.0) {
      $et_in += $q_et->f("entrytemp_amount");
    } else {
      // we substract a negative amount, so we obtain a positive sum
      $et_out -= $q_et->f("entrytemp_amount");
    }
  }

  if ($pay_in != $et_in) {
    return 0;
  }
  if ($pay_out != $et_out) {
    return 0;
  }
  
  // we can update the db... 
  foreach ($pay_ids as $current_id) {
    run_query_check_payment ($current_id);
  }
  foreach ($et_ids as $current_id) {
    run_query_check_entrytemp ($current_id);
  }
  
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// the next 2 functions return all data concerning the list of
// payments (resp. entrytemp) given as argument :
///////////////////////////////////////////////////////////////////////////////
function run_query_detail_payments ($pay_id_list) {
  global $cdg_sql;

  $go = new DB_OBM;
  
  $query = " select * from PaymentTemp ";
  if ($pay_id_list != "") {
    $query .= " where payment_id in ($pay_id_list)";
  }
  display_debug_msg ("<b>run_query_detail_payments() :</b>$query", $cdg_sql);
  $go->query($query);

  return $go;
}


function run_query_detail_et ($et_id_list) {
  global $cdg_sql;

  $go = new DB_OBM;
  
  $query = " select * from EntryTemp ";
  if ($et_id_list != "") {
     $query .= " where entrytemp_id in ($et_id_list)";
  }
  display_debug_msg ("<b>run_query_detail_et() :</b>$query", $cdg_sql);
  $go->query($query);

  return $go;
}


///////////////////////////////////////////////////////////////////////////////
// returns true if there are some Entrytemps not checked in the table,
// else false
///////////////////////////////////////////////////////////////////////////////
function remains_entrytemps_in_table () {
  global $cdg_sql;

  $go = new DB_OBM;
  
  $query = "select * from EntryTemp where entrytemp_checked = '0'";
  display_debug_msg ("<b>remains_entrytemps_in_table() :</b>$query", $cdg_sql);
  $go->query($query);

  if ($go->nf() == 0) {
    return false;
  }
  return true;
}


///////////////////////////////////////////////////////////////////////////////
// returns the lists of checked payments id as a string 
///////////////////////////////////////////////////////////////////////////////
function run_query_get_checked_payments () {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "select payment_id from PaymentTemp ".
     " where payment_checked = '1'";
  display_debug_msg ("<b>run_query_get_checked_payments() :</b>$query", $cdg_sql);
  $go->query ($query);

  $tab = array ();
  while ($go->next_record ()) {
    $tab[] = $go->f("payment_id");
  }

  return implode (",", $tab);
}


///////////////////////////////////////////////////////////////////////////////
// checks all payments corresponding to id given as parameter
///////////////////////////////////////////////////////////////////////////////
function run_query_check_payments ($id_list) {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = "update Payment ".
     " set payment_checked = '1' ".
     " where payment_id in (".$id_list.")";
  display_debug_msg ("<b>run_query_check_payments() :</b>$query", $cdg_sql);
  $go->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// search for invoices that are connected only to checked payments.
// if some exist, we update their status to checked IF IT WAS NOT TROUBLE
// if status was TROUBLE, we don't even select them at the first stage...
///////////////////////////////////////////////////////////////////////////////
function  run_query_update_invoice_status () {
  global $cdg_sql;

  $go = new DB_OBM;

  $query = " select invoice_id from Invoice, PaymentInvoice, Payment ".
     " where invoice_id = paymentinvoice_invoice_id ".
     " and payment_id = paymentinvoice_payment_id ".
     " and payment_checked = '1'".
     " group by invoice_id ";

  display_debug_msg ("<b>run_query_update_invoice_status(): </b>$query", $cdg_sql);
  $go->query($query);

  if ($go->nf() > 0) {
    $tab = array ();
    while ($go->next_record ()) {
      $tab[] = $go->f("invoice_id");
    }
    
    $my_invoices = implode (",", $tab);
    
    $query = " update Invoice ".
       " set invoice_invoicestatus_id = '3' ".// 3 == checked =)
       " where invoice_id in (".$my_invoices.")";
    display_debug_msg ("<b>run_query_update_invoice_status(): </b>$query", $cdg_sql);
    $go->query($query);
  }
}

</SCRIPT>
