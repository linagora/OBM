<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File  : account_query.inc                                           //
//     - Desc  : account query File                                          //
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
// Account search query :
// search for accounts in dbase, using various criteria :
// label, bank and number...
// $p_new_order and $p_new_order2 are related to display lists...
//
// Problem here : we get from dbase the balance at the account creation.
// We want to display the balance "today". But, as we are looking for accounts,
// it's hard to compute the account at the same time...
//////////////////////////////////////////////////////////////////////////////
function run_query_search ($account, $p_new_order, $p_new_order2, $p_order_dir) {
  global $auth, $cdg_sql;

  $label = $account["label"];
  $bank = $account["bank"];
  $number = $account["number"];

  $order=(strcmp($p_new_order,"") != 0) ? $p_new_order : "account_bank";
  
  $query = "select *, account_id as Id from Account ";
  $query .= " where 1 > 0 ";
  if ($label != "") {
    $query.=" and account_label like '$label%' ";
  }

  if ($bank != "") {
    $query.=$flag." and account_bank like '%$bank%' ";
  }
  if ($number != "") 
    $query.=" and account_number like '%$number%' ";

  $query.=" order by $order $p_order_dir";
  
  if (strcmp($p_new_order2,"") != 0) {
    $query .= ", ".$p_new_order2;
  }

  display_debug_msg("<b>run_query_search() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  
  return ($obm_q);
}

//////////////////////////////////////////////////////////////////////////////
// insertion query :
//////////////////////////////////////////////////////////////////////////////
function run_query_insert ($account) {
  global $auth, $cdg_sql;

  $label = $account["label"];
  $bank = $account["bank"];
  $number = $account["number"];
  $balance = $acount["balance"];
  $comment = $account["comment"];
  
  $query = "insert into Account (account_timeupdate, account_timecreate, account_userupdate, account_usercreate, account_bank, account_label, account_number, account_balance, account_today,  account_comment) values (null,'".date("Y-m-d H:i:s")."',null,'".$auth->auth["uid"]."','$bank','$label','$number','$balance','$balance','$comment')";

  display_debug_msg( "<b>run_query_insert () :</b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Account: Select query construction
// the balance returned by this query is the true balance at date == today... 
///////////////////////////////////////////////////////////////////////////////
function run_query_detail($p_account_id) {
  global $l_date_format, $cdg_sql; 
  global $db_type_mysql,$db_type_pgsql;  

  $connect_db = new DB_OBM;
  if ($connect_db->type == $db_type_mysql) {
    $query = "select * ".
       ", DATE_FORMAT(account_timeupdate,'$l_date_format') as datemodif ".
       ", DATE_FORMAT(account_timeupdate,'$l_date_format') as timeupdate ".
       ", DATE_FORMAT(account_timecreate,'$l_date_format') as timecreate ".
       " from Account where account_id =".$p_account_id;
  } else if ($connect_db->type == $db_type_pgsql) {
    $query = "select *,account_timeupdate as datemodif,account_timeupdate as timeupdate,account_timecreate as timecreate from Account where account_id=".$p_account_id;
  }

  display_debug_msg ("<b>run_query_detail() : </b>$query", $cdg_sql);
  $connect_db->query($query);
  return $connect_db;
}


///////////////////////////////////////////////////////////////////////////////
// Update query construction                                                 
///////////////////////////////////////////////////////////////////////////////
function run_query_update($account) {
  global $auth, $cdg_sql;

  $label = $account["label"];
  $bank = $account["bank"];
  $number = $account["number"];
  $balance = $acount["balance"];
  $comment = $account["comment"];
  $account_id = $account["account"];

 $query = "update Account set 
     account_label='$label',
     account_bank='$bank',
     account_number='$number',
     account_balance='$balance',
     account_comment='$comment',
     account_userupdate='".$auth->auth["uid"]."',
     account_timeupdate='".date("Y-m-d H:i:s")."'
   where account_id='$account_id'";  

  display_debug_msg ("<b>run_query_update() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);  
}


///////////////////////////////////////////////////////////////////////////////
// returns all payments connected to the account
// if $p_date_before is set, matching payments must have 
//          payment_date < $p_date_before
///////////////////////////////////////////////////////////////////////////////
function run_query_search_payments ($p_account_id, $p_date_before = "") {
  global $cdg_sql;

  $query = "select *, payment_id as Id,
      payment_expected_date as payment_expect_date,
      payment_amount
    from Payment
    where payment_account_id='$p_account_id'";

  if ($p_date_before != "") {
    $query .= " and payment_date <= '$p_date_before'";
  }
  $query .= " and payment_paid = '1'"; // payed payments only

  display_debug_msg("<b>run_query_search_payments() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// returns all expected payments connected to the account
// if $p_date_before is set, matching payments must have 
//          payment_expected_date < $p_date_before
///////////////////////////////////////////////////////////////////////////////
function run_query_search_expected_payments ($p_account_id, $p_date_before="") {
  global $cdg_sql;

  $query = "select *, payment_id as Id,
      payment_expected_date as payment_expect_date
      from Payment where payment_account_id='$p_account_id'";
  if ($p_date_before != "") {
    $query .= " and payment_expected_date <= '$p_date_before'";
  }
  $query .= " and payment_paid = '0' "; // expected payments only
  
  display_debug_msg ("<b>run_query_search_expected_payments() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM; 
  $obm_q->query($query);
  return $obm_q; 
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction                                                 //
// Parameters:
//   - $a_id : account Id
///////////////////////////////////////////////////////////////////////////////
function run_query_delete($a_id) {
  global $cdg_sql;

  $query = "delete from Account where account_id='$a_id'";
  display_debug_msg ("<b>run_query_delete() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// given an account id $p_account_id and a date $p_date, returns a hash table :
// if $p_date == "", we use today as compute date
//    key       |      value
//--------------+-------------------------------------------------------------
//  in_real     |  sum of incoming payments really paid
//  out_real    |  same as in for outgoing payments really paid
//  in_expected |  sum of incoming payments expected before $p_date
// out_expected |  sum of outgoing payments expected before $p_date
//  payments    |  database records containing payed payments used to compute
//  expected    |  same as payments, but expected payments ids only
//  today       |  balance today
//  balance     |  just guess ;)
///////////////////////////////////////////////////////////////////////////////
function account_compute_balance ($p_account_id, $p_date="") {
  global $cdg_sql;

  $date = ($p_date=="")?date("Y-m-d"):$p_date;
  $total_in_real = $total_out_real = 0 ; // payments banked
  $total_in_exp = $total_out_exp = 0; // payments expected
  $result = array();
  
  // we get the balance at creation time of this account :
  $balance  = account_creation_balance ($p_account_id);

  // first we get all payments connected to this account
  $q_payments = run_query_search_payments ($p_account_id, $date);
  $q_expected = run_query_search_expected_payments ($p_account_id, $date);


  // FIXME : faire en une fois, avec une seule requete qui récupère tous
  // les payments qqu'ils soient ?
  // lets compute total ins and outs :
  while ($q_payments->next_record()) {
    if ($q_payments->f("payment_inout") == '+') {
      $total_in_real += $q_payments->f("payment_amount");
    } else {
      $total_out_real += $q_payments->f("payment_amount");
    }
  }
  
  while ($q_expected->next_record()) {
    if ($q_expected->f("payment_inout") == '+') {
      $total_in_exp += $q_expected->f("payment_amount");
    } else {
      $total_out_exp += $q_expected->f("payment_amount");
    }
  }

  $today   = $balance + $total_in_real - $total_out_real ;
  $balance = $today   + $total_in_exp - $total_out_exp ;

  // now we create the hash table we will return :
  $result["in_real"] = $total_in_real;
  $result["out_real"] = $total_out_real;
  $result["in_expected"] = $total_in_exp;
  $result["out_expected"] = $total_out_exp;
  $result["payments"] = $q_payments;
  $result["expected"] = $q_expected;
  $result["balance"] = $balance;
  $result["today"] = $today;

  return $result;
}

///////////////////////////////////////////////////////////////////////////////
// returns the balance of $p_account_id account at its creation
///////////////////////////////////////////////////////////////////////////////
function account_creation_balance ($p_account_id) {
  global $cdg_sql;


  $query = "select account_balance
            from Account
            where account_id ='$p_account_id'";
  display_debug_msg ("<b>account_creation_balance() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();
  return $obm_q->f("account_balance");
}

</SCRIPT>

