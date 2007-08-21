<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_query.inc 
//     - Desc : payment query File
// 2001-08-21 Aliacom
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Payment search query execution
// Parametes:
//   - $payment[]    : payment search criteria
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_search($payment) {
  global $cdg_sql, $c_all, $ctu_sql_limit;

  $sql_order_dir = $payment['sql_order_dir'];
  $sql_order_field = $payment['sql_order_field'];

  $comment = sql_search_text_parse($payment['comment']);
  $number = sql_search_text_parse($payment['number']);
  $amount = $payment['amount'];
  $date_after = of_isodate_convert($payment['date_after'], true);
  $date_before = of_isodate_convert($payment['date_before'], true);
  $inout = $payment['inout'];
  $kind = $payment['kind'];
  $account = $payment['account'];
  $company = sql_search_text_parse($payment['company']);
  $company_id = $payment['company_id'];
  $checked = $payment['checked'];

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);
  $multidomain = sql_multidomain('payment');
	
  $date = sql_date_format($db_type, 'payment_date', 'payment_date');

  // search criterias :
  $where = '1=1';
  $and = 'AND';
  if ($comment != '') {
    $where .= "$and payment_comment $like '%$comment%'";
    $and = 'AND';
  }
  if ($number) {
    $where .= " $and payment_number $like '%$number%'";
    $and = 'AND';
  }
  if ($amount) {
    $where .= " $and payment_amount $like '$amount%'";
    $and = 'AND';
  }
  if ($date_after) {
    $where .= " $and payment_date >= '$date_after'";
    $and = 'AND';
  }
  if ($date_before) {
    $where .= " $and payment_date <= '$date_before'";
    $and = 'AND';
  }
  if (($inout != '') && ($inout != "$c_all")) {
    $where .= " $and payment_inout ='$inout'";
    $and = 'AND';
  }
  if (($kind != '') && ($kind != "$c_all")) {
    $where .= " $and payment_paymentkind_id ='$kind'";
    $and = 'AND';
  }
  if (($account != '') && ($account != "$c_all")) {
    $where .= " $and payment_account_id ='$account'";
    $and = 'AND';
  }
  if ($company != '') {
    $where .= " $and company_name $like '$company%'";
    $join_comp = "LEFT JOIN Company ON payment_company_id=company_id";
    $and = 'AND';
  }
  if ($company_id != '') {
    $where .= " $and payment_company_id = '$company_id'";
    $and = 'AND';
  }
  if ($checked != 'y') {
    $where .= " $and payment_checked = '0'";
    $and = 'AND';
  }

  $whereq = "WHERE $where $multidomain";

  // Order by clause
  $field = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'payment_date';
  $orderq = " ORDER BY $field $sql_order_dir";

  $query = "SELECT
    payment_id,
    payment_company_id,
    payment_number,
    $date,
    payment_amount,
    payment_comment,
    payment_inout,
    payment_checked,
    company_name,
    account_label,
    paymentkind_label
  FROM Payment
    LEFT JOIN Company ON payment_company_id=company_id
    LEFT JOIN PaymentKind ON payment_paymentkind_id=paymentkind_id
    LEFT JOIN Account ON payment_account_id=account_id
  $whereq
  $orderq
  $limit
";

  if ($ctu_sql_limit) {
    $count = get_query_count("SELECT count(*) FROM Payment $join_comp $whereq");
    $obm_q->set_num_rows_total($count);
  }
  if (($count > 0) || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql, 'payment_search()');
    $obm_q->query($query);
  }

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Detail query execution
// Parameters :
//   - $p_id  : payment id
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_detail($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;

  $timeupdate = sql_date_format($db_type, 'payment_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'payment_timecreate', 'timecreate');
  $date = sql_date_format($db_type, 'payment_date', 'date');
  $multidomain = sql_multidomain('payment');
	
  $query = "SELECT Payment.*,
      $timeupdate,
      $timecreate,
      $date,
      paymentkind_label,
      account_label,
      company_name, company_address1, company_zipcode, company_town,
      company_phone,
      c.userobm_login as usercreate,
      u.userobm_login as userupdate
    FROM Payment
         LEFT JOIN PaymentKind ON payment_paymentkind_id=paymentkind_id
         LEFT JOIN Company ON payment_company_id=company_id
         LEFT JOIN Account ON payment_account_id=account_id
         LEFT JOIN UserObm as c ON payment_usercreate=c.userobm_id
         LEFT JOIN UserObm as u ON payment_userupdate=u.userobm_id
    WHERE payment_id='$p_id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_payment_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Insertion query execution
// Parameters:
//   - $params[]: Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_insert($params) {
  global $obm, $cdg_sql;

  $now = date('Y-m-d H:i:s');
  $uid = $obm['uid'];
  $domain_id = $obm['domain_id'];

  $company_id = $params['company_new_id'];
  if ($company_id < 1) {
    $company_id = $params['company_id'];
  }

  $account_id = $params['account'];
  $invoice_id = $params['invoice_id'];
  $kind = $params['kind'];
  $number = $params['number'];
  $date = ($params['date'] ? "'".of_isodate_convert($params['date'])."'" : 'null');
  $amount = $params['amount'];
  $amount_invoice = $params['amount_invoice'];
  $inout = $params['inout'];
  $checked = ($params['checked'] == '1' ? '1' : '0');
  $comment = $params['comment'];
	
  $query = "INSERT INTO Payment (
    payment_domain_id,
    payment_timeupdate,
    payment_timecreate,
    payment_userupdate,
    payment_usercreate,
    payment_company_id,
    payment_account_id,
    payment_paymentkind_id,    
    payment_amount,
    payment_date,
    payment_inout,
    payment_number,
    payment_checked,
    payment_comment)
   VALUES (
    '$domain_id',
    null,
    '$now',
    null,
    '$uid',
    '$company_id',
    '$account_id',
    '$kind',
    '$amount',
    $date,
    '$inout',
    '$number',
    '$checked',
    '$comment')";
  
  display_debug_msg($query, $cdg_sql, 'run_query_payment_insert()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  $datep = ($params['date'] ? "='".of_isodate_convert($params['date'])."'" : 'is null');

  // Get the payment id inserted
  $query = "SELECT payment_id
    FROM Payment
    WHERE payment_company_id = '$company_id'
      AND payment_paymentkind_id = '$kind'
      AND payment_date $datep
      AND payment_amount = '$amount'
      AND payment_comment = '$comment'";

  display_debug_msg($query, $cdg_sql, 'run_query_payment_insert(2)');
  $obm_q->query($query);
  $obm_q->next_record();
  $id = $obm_q->f('payment_id');

  // insert the payment-invoice link, if invoice_id set
  if ($id > 0 && isset($invoice_id)) {
    $query = "INSERT INTO PaymentInvoice (
      paymentinvoice_invoice_id,
      paymentinvoice_payment_id,
      paymentinvoice_amount,
      paymentinvoice_timecreate,
      paymentinvoice_usercreate
    ) VALUES (
      '$invoice_id',
      '$id',
      '$amount_invoice',
      '$now',
      '$uid')";

    display_debug_msg($query, $cdg_sql, 'run_query_payment_insert(3)');
    $obm_q->query($query);
  }

  return $id;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Update query execution
// Parameters:
//   - $pid       : payment id
//   - $params[]  : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_update($pid, $params) {
  global $obm, $cdg_sql;

  $now = date('Y-m-d H:i:s');
  $uid = $obm['uid'];

  $company_id = $params['company_new_id'];
  if ($company_id < 1) {
    $company_id = $params['company_id'];
  }

  $account_id = $params['account'];
  $kind = $params['kind'];
  $number = $params['number'];
  $date = ($params['date'] ? "'".of_isodate_convert($params['date'])."'" : 'null');
  $amount = $params['amount'];
  $inout = $params['inout'];
  $checked = ($params['checked'] == '1' ? '1' : '0');
  $comment = $params['comment'];
  $multidomain = sql_multidomain('payment');
	
  $query = "UPDATE Payment SET
    payment_timeupdate='$now',
    payment_userupdate='$uid',
    payment_company_id='$company_id',
    payment_account_id='$account_id',
    payment_paymentkind_id='$kind',
    payment_amount='$amount',
    payment_date=$date,
    payment_inout='$inout',
    payment_number='$number',
    payment_checked='$checked',
    payment_comment='$comment'
  WHERE payment_id='$pid'
    $multidomain";

  display_debug_msg($query, $cdg_sql, 'run_query_payment_update()');
  $obm_q = new DB_OBM;
  $retour = $obm_q->query($query);

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get Payment infos
// Parameters
//   - $id : payment id
// Returns:
//   $ret[] : [date][label][amount][company]
///////////////////////////////////////////////////////////////////////////////
function get_payment_info($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $date = sql_date_format($db_type, 'payment_date', 'payment_date');
  $multidomain = sql_multidomain('payment');

  $query = "SELECT
      payment_amount,
      $date,
      payment_number,
      payment_inout,
      company_name
    FROM Payment
         LEFT JOIN Company on payment_company_id=company_id
    WHERE payment_id='$id'
      $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_payment_info()');
  $obm_q->query($query);
  $obm_q->next_record();

  $ret['amount'] = $obm_q->f('payment_amount');
  $ret['company'] = $obm_q->f('company_name');
  $ret['date'] = $obm_q->f('payment_date');
  $ret['inout'] = $obm_q->f('payment_inout');
  $ret['number'] = $obm_q->f('payment_number');

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get Payment Invoices infos
// Parameters
//   - $id : payment id
// Returns:
//   $ret[] : [inv id]=[payment amount][amount][label][company][label]
///////////////////////////////////////////////////////////////////////////////
function get_payment_invoices($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('invoice');

  $query = "SELECT
    invoice_id,
    PI1.paymentinvoice_amount,
    invoice_company_id,
    invoice_amount_ttc,
    invoice_number,
    invoice_label,
    invoice_date,
    company_name,
    P2.payment_inout,
    SUM(PI2.paymentinvoice_amount) as invoice_already_paid
  FROM PaymentInvoice as PI1
    LEFT JOIN Invoice on PI1.paymentinvoice_invoice_id=invoice_id
    LEFT JOIN Company on invoice_company_id=company_id
    LEFT JOIN PaymentInvoice as PI2 on invoice_id=PI2.paymentinvoice_invoice_id
              and PI2.paymentinvoice_payment_id != '$id'
    LEFT JOIN Payment as P2 on PI2.paymentinvoice_payment_id=P2.payment_id
  WHERE PI1.paymentinvoice_payment_id='$id'
    $multidomain
  GROUP BY
    invoice_id,
    paymentinvoice_amount,
    invoice_company_id,
    invoice_amount_ttc,
    invoice_number,
    invoice_label,
    invoice_date,
    company_name,
    P2.payment_inout
  ORDER BY invoice_date";

  display_debug_msg($query, $cdg_sql, 'get_payment_invoices()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('invoice_id');
    $amount = $obm_q->f('paymentinvoice_amount');
    $company = $obm_q->f('company_name');
    $number = $obm_q->f('invoice_number');
    $inv_amount = $obm_q->f('invoice_amount_ttc');
    $inout = $obm_q->f('payment_inout');
    if (isset($ret[$id]['already_paid'])) {
      $ap = $ret[$id]['already_paid'];
    } else {
      $ap = 0;
    }
    if ($inout == '+') {
      $already_paid = $ap + $obm_q->f('invoice_already_paid');
    } else {
      $already_paid = $ap - $obm_q->f('invoice_already_paid');
    }
    $label = $obm_q->f('invoice_label');
    $date = $obm_q->f('invoice_date');
    $ret[$id] = array('id' => $id,
		      'affected_amount' => $amount,
		      'invoice_amount' => $inv_amount,
		      'already_paid' => $already_paid,
		      'company' => $company,
		      'number' => $number,
		      'label' => $label,
		      'date' => $date);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get Payment amount infos (payment amount + affected to invoices)
// Parameters
//   - $id : payment id
// Returns:
//   $ret[] : [inv id]=[payment amount][amount][label][company][label]
///////////////////////////////////////////////////////////////////////////////
function get_payment_amount_info($id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('payment');

  $query = "SELECT
    payment_amount,
    payment_inout
  FROM Payment
  WHERE payment_id='$id' $multidomain";

  display_debug_msg($query, $cdg_sql, 'get_payment_amount_info(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  $amount = $obm_q->f('payment_amount');
  $ret['paid_amount'] = $amount;

  $invs = get_payment_invoices($id);
  $ret['invoices'] = $invs;

  $total_used_amount = 0;
  if (is_array($invs) && (count($invs) > 0)) {
    foreach ($invs as $one_inv) {
      $used_amount = $one_inv['affected_amount'];
      $total_used_amount += $used_amount;
    }
  }
  $ret['total_used_amount'] = $total_used_amount;
  $ret['total_unused_amount'] = $amount - $total_used_amount;

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Invoice link Insertion query execution
// Parameters:
//   - $params[]: Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_invoice_insert($params) {
  global $cdg_sql, $obm;

  $now = date('Y-m-d H:i:s');
  $uid = $obm['uid'];
  $id = $params['payment_id'];
  $amount ='0';

  // We check the payment still exist (in case popup windows here for long...)
  $multidomain = sql_multidomain('payment');
  $query = "SELECT payment_id FROM Payment WHERE payment_id='$id' $multidomain";
  display_debug_msg($query, $cdg_sql,'run_query_payment_invoice_insert(1)');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  if ($obm_q->num_rows() != 1) {
    return -1;
  }

  $cpt = 0;
  $cpt_ins = 0;
  while ($cpt < $params['inv_nb']) {
    $cpt++;
    $inv_id = $params["invo$cpt"];

    $query = "SELECT * FROM PaymentInvoice
      WHERE paymentinvoice_invoice_id='$inv_id'
        AND paymentinvoice_payment_id='$id'";
    display_debug_msg($query, $cdg_sql,'run_query_payment_invoice_insert(2)');
    $test_q = new DB_OBM;
    $retour = $test_q->query($query);
    
    // If the entry doesn't already exist, we insert it
    if ($test_q->num_rows() == 0) {

      $query = "INSERT INTO PaymentInvoice (
      paymentinvoice_invoice_id,
      paymentinvoice_payment_id,
      paymentinvoice_timeupdate,
      paymentinvoice_timecreate,
      paymentinvoice_userupdate,
      paymentinvoice_usercreate,
      paymentinvoice_amount)
     VALUES (
      '$inv_id',
      '$id',
      null,
      '$now',
      null,
      '$uid',
      '$amount')";

      display_debug_msg($query, $cdg_sql,'run_query_payment_invoice_insert(3)');
      $obm_q = new DB_OBM;
      $retour = $obm_q->query($query);

      $cpt_ins++;
    }
  }

  return $cpt_ins;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Invoice link Update query execution
// Parameters:
//   - $pid      : payment id
//   - $params[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_invoice_update($pid, $params) {
  global $obm, $cdg_sql;

  $invs = $params['invoices'];
  $now = date('Y-m-d H:i:s');
  $uid = $obm['uid'];

  $obm_q = new DB_OBM;
  $cpt = 0;
  $cpt_del = 0;
  $invoices_del = array();

  // Delete the associations checked
  while ($cpt < $params['inv_nb']) {
    $cpt++;
    $inv_id = $params["invo$cpt"];

    $query = "DELETE FROM PaymentInvoice
      WHERE paymentinvoice_payment_id='$pid'
        AND paymentinvoice_invoice_id='$inv_id'";
    display_debug_msg($query, $cdg_sql, 'run_query_payment_invoice_update(delete)');
    $retour = $obm_q->query($query);
    if ($retour) {
      $cpt_del++;
      $invoices_del[$inv_id] = $inv_id;
    }
  }

  // Update the associations
  foreach($invs as $i_id=>$amount) {
    if (! in_array($i_id, $invoices_del)) {
      $query = "UPDATE PaymentInvoice SET
      paymentinvoice_timeupdate = '$now',
      paymentinvoice_userupdate = '$uid',
      paymentinvoice_amount = '$amount'
    WHERE
      paymentinvoice_payment_id='$pid'
      AND paymentinvoice_invoice_id='$i_id'";
    
      display_debug_msg($query, $cdg_sql, 'run_query_payment_invoice_update(update)');
      $retour = $obm_q->query($query);
    }
  }

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// Get Payment kinds infos
// Returns:
//   $ret[] : [id]=[id][label],[shortlabel]
///////////////////////////////////////////////////////////////////////////////
function get_payment_kinds() {
  global $cdg_sql;

  $multidomain = sql_multidomain('paymentkind');

  $query = "SELECT * FROM PaymentKind WHERE 1=1 $multidomain ORDER BY paymentkind_label";

  display_debug_msg($query, $cdg_sql, 'get_payment_kinds()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('paymentkind_id');
    $label = $obm_q->f('paymentkind_label');
    $shortlabel = $obm_q->f('paymentkind_shortlabel');
    $ret[$id] = array('id' => $id, 'label' => $label, 'shortlabel' => $shortlabel);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Get Accounts infos
// Returns:
//   $ret[] : [id]=[label]
///////////////////////////////////////////////////////////////////////////////
function get_accounts() {
  global $cdg_sql;

  $multidomain = sql_multidomain('account');

  $query = "SELECT * FROM Account WHERE 1=1 $multidomain ORDER BY account_label";

  display_debug_msg($query, $cdg_sql, 'get_payment_accounts()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  while ($obm_q->next_record()) {
    $id = $obm_q->f('account_id');
    $label = $obm_q->f('account_label');
    $ret[$id] = array('label' => $label);
  }

  return $ret;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Form Data checking and formatting
// Parameters:
//   - $id       : payment id (empty on insertion)
//   - $params[] : values to check
///////////////////////////////////////////////////////////////////////////////
function check_payment_data_form($id, $params) {
  global $php_regexp_isodate;
  global $l_fill_company, $l_fill_kind, $l_fill_number, $l_error_number_exist;
  global $l_fill_inout, $l_fill_amount, $l_fill_date, $l_date, $l_invalid_date;
  global $err, $l_exist_error;

  $number = $params['number'];
  $date = of_isodate_convert($params['date']);
  $inout = $params['inout'];
  $amount = $params['amount'];
  $c_id = $params['company_id'];
  $c_new_id = $params['company_new_id'];
  $kind = $params['kind'];

  // MANDATORY: a company must be set
  if (($c_id < 1) && ($c_new_id < 1)) {
    $err['msg'] = $l_fill_company;
    return false;
  }

  // MANDATORY: Amount must be set
  if (!($amount > 0)) {
    $err['msg'] = $l_fill_amount;
    return false;
  }

  // Date check
  if (preg_match($php_regexp_isodate, $date) == 0) {
    $err['msg'] = "$l_date : $date : $l_invalid_date"; 
    return false;
  }

  // MANDATORY: inout must be set
  if (($inout != '+') && ($inout != '-')) {
    $err['msg'] = $l_fill_inout;
    return false;
  }

  // MANDATORY: kind must be set
  if (! ($kind > 0)) {
    $err['msg'] = $l_fill_kind;
    return false;
  }

  // MANDATORY: Invoice number, if state not "to be created"
  if ($status_created) {
    if (trim($number) == '') {
      $err['msg'] = $l_fill_number;
      return false;
    } else {
      $nb = get_invoice_nb_having_number($number, $id);
      if ($nb > 0) {
	$err['msg'] = $l_error_number_exist;
	return false;
      }
    }
  }

  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Payment Invoice Form Data checking and formatting
// Parameters:
//   - $params[] : values to check
///////////////////////////////////////////////////////////////////////////////
function check_payment_invoice_data_form($params) {
  global $php_regexp_amount;
  global $l_fill_amount, $l_invalid_amount, $l_err_total_payment_affected;
  global $err, $l_err_affected_invoice;

  $id = $params['payment_id'];
  $invs = $params['invoices'];

  // check that each amount are correctly filled
  foreach($invs as $i_id=>$amount) {
    // Do not check if association is to be deleted
    if (! isset($params["inv-$i_id"])) {
      if (preg_match($php_regexp_amount, $amount) == 0) {
	$err['msg'] = "$amount : $l_invalid_amount"; 
	return false;
      } elseif (! ($amount > 0)) {
	$err['msg'] = "$amount : $l_fill_amount";
	return false;
      }
    }
  }

  $pinfos = get_payment_amount_info($id);

  // Total affected amount to invoice must be <= paid amount
  $paid_amount = $pinfos['paid_amount'];
  $total_affected_amount = 0;
  foreach($invs as $i_id=>$affected_amount) {
    $total_affected_amount += $affected_amount;
  }
  if (round($paid_amount,2) < round($total_affected_amount,2)) {
    $err['msg'] = "$l_err_total_payment_affected ($total_amount > $paid_amount)";
    return false;
  }

  // For each invoice check that affected amount <= remaining amount to be paid
  foreach($invs as $i_id=>$affected_amount) {
    $already_paid = $pinfos['invoices']["$i_id"]['already_paid'];
    $invoice_amount = $pinfos['invoices']["$i_id"]['invoice_amount'];
    $remaining_amount = $invoice_amount - $already_paid;
    if ($affected_amount > $remaining_amount) {
      $err['msg'] = "$l_err_affected_invoice ($affected_amount > $remaining_amount)";
      return false;
    }
  }



  return true;
}


///////////////////////////////////////////////////////////////////////////////
// Check if the payment can be deleted
// Parameters:
//   - $id : payment id
// Returns:
//   true if the payment can be deleted, else false
///////////////////////////////////////////////////////////////////////////////
function check_can_delete_payment($id) {
  global $ok_msg;

  $delete_ok = true;

  return $delete_ok;
}


///////////////////////////////////////////////////////////////////////////////
// Payment deletion query execution
// Parameters:
//   - $p_id : payment id
///////////////////////////////////////////////////////////////////////////////
function run_query_payment_delete($p_id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;

  $query = "DELETE FROM PaymentInvoice
    WHERE paymentinvoice_payment_id='$p_id'";
  display_debug_msg($query, $cdg_sql, 'run_query_payment_delete(invoice link)');
  $obm_q->query($query);

  $multidomain = sql_multidomain('payment');
  $query = "DELETE FROM Payment WHERE payment_id='$p_id' $multidomain";
  display_debug_msg($query, $cdg_sql, 'run_query_payment_delete()');
  $retour = $obm_q->query($query);

  return $retour;
}


?>
