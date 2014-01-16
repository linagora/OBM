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
// OBM - File : account_query.inc                                            //
//     - Desc : account query File                                           //
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////////
// Account search query
// Parameters:
//   - $account     : account current infos (after insert, update)
//
// Problem here : we get from dbase the balance at the account creation.
// We want to display the balance 'today'. But, as we are looking for accounts,
// it's hard to compute the account at the same time...
//////////////////////////////////////////////////////////////////////////////
function run_query_account_search ($account) {
  global $cdg_sql, $ctu_sql_limit;

  $sql_order_dir = $account['sql_order_dir'];
  $sql_order_field = $account['sql_order_field'];

  $label = sql_search_text_parse($account['label']);
  $bank = sql_search_text_parse($account['bank']);
  $number = sql_search_text_parse($account['number']);
  $multidomain = sql_multidomain('account');

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $like = sql_casei_like($db_type);
  $limit = sql_limit($db_type);

  $where = '1=1';
  if ($label != '') {
    $where .= " and account_label $like '".$obm_q->escape($label)."%'";
  }
  if ($bank != '') {
    $where .= " and account_bank $like '%".$obm_q->escape($bank)."%'";
  }
  if ($number != '') {
    $where .= " and account_number $like '%".$obm_q->escape($number)."%'";
  }
  $whereq = "where $where $multidomain";

  $order = (strcmp($sql_order_field,'') != 0) ? $sql_order_field : 'account_bank';
  $orderq = " order by $order $sql_order_dir";

  $query = "select *,
    account_id as id
    from Account
    $whereq
    $orderq
    $limit
    ";

  if ($ctu_sql_limit) {
    $count = get_query_count("select count(*) from Account $whereq");
    $obm_q->set_num_rows_total($count);
  }

  if (($count > 0)  || (! $ctu_sql_limit)) {
    display_debug_msg($query, $cdg_sql,'run_query_account_search()');
    $obm_q->query($query);
  }

  return ($obm_q);
}


///////////////////////////////////////////////////////////////////////////////
// Account: Select query construction
// the balance returned by this query is the true balance at date == today... 
// Parameters:
//   - $id : account id
///////////////////////////////////////////////////////////////////////////////
function run_query_account_detail($id) {
  global $cdg_sql;

  $obm_q = new DB_OBM;
  $db_type = $obm_q->type;
  $datemodif = sql_date_format($db_type, 'account_timeupdate', 'datemodif');
  $timeupdate = sql_date_format($db_type, 'account_timeupdate', 'timeupdate');
  $timecreate = sql_date_format($db_type, 'account_timecreate', 'timecreate');
  $multidomain = sql_multidomain('account');
  $id = sql_parse_id(sql_parse_id( $id), true);

  $query = "select *,
    $datemodif,
    $timeupdate,
    $timecreate,
    c.userobm_login as usercreate,
    u.userobm_login as userupdate
    from Account
    LEFT JOIN UserObm as c on account_usercreate=c.userobm_id
    LEFT JOIN UserObm as u on account_userupdate=u.userobm_id
    where account_id  $id $multidomain";

  display_debug_msg ($query, $cdg_sql, 'run_query_account_detail()');
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q;
}


//////////////////////////////////////////////////////////////////////////////
// Account insert query execution
// Parameters:
//   - $account[] : Entry's values
//////////////////////////////////////////////////////////////////////////////
function run_query_account_insert($account) {
  global $cdg_sql, $obm;

  $obm_q = new DB_OBM;
  $now = date('Y-m-d H:i:s');
  $uid = sql_parse_id($obm['uid']);
  $domain_id = sql_parse_id($obm['domain_id']);

  $label = $account['label'];
  $bank = $account['bank'];
  $number = $account['number'];
  $balance = sql_parse_int($account['balance']);
  $comment = $account['comment'];

  $query = "INSERT INTO Account (
    account_timeupdate,
    account_timecreate,
    account_userupdate,
    account_usercreate,
    account_domain_id,
    account_bank,
    account_label,
    account_number,
    account_balance,
    account_today,
    account_comment
  ) values (
    null,
    NOW(),
    null,
    $uid,
    $domain_id,
    '".$obm_q->escape($bank)."',
    '".$obm_q->escape($label)."',
    '".$obm_q->escape($number)."',
    '".$obm_q->escape($balance)."',
    $balance,
    '".$obm_q->escape($comment)."')";

  display_debug_msg($query, $cdg_sql, 'run_query_account_insert()');
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// Account update query execution
// Parameters:
//   - $account[] : Entry's values
///////////////////////////////////////////////////////////////////////////////
function run_query_account_update($account) {
  global $obm, $cdg_sql;

  $uid = sql_parse_id( $obm['uid']);
  $id =$id;
  $label = $account['label'];
  $bank = $account['bank'];
  $number = $account['number'];
  $balance = sql_parse_int( $account['balance']);
  $comment = $account['comment'];
  $account_id = sql_parse_id( $account['account_id'], true);
  $multidomain = sql_multidomain('account');

  //Get initial value
  $query = "SELECT account_today,
            account_balance
            FROM Account
            WHERE account_id $account_id $multidomain";
   
  display_debug_msg ($query, $cdg_sql, 'run_query_account_update(0)');

  $obm_q = new DB_OBM;
  $obm_q->query($query);  
  $obm_q->next_record();
  $init_balance=$obm_q->f('account_balance');
  $today=$obm_q->f('account_today');
  
  $diff = $balance - $init_balance;
  $today = $today + $diff;

  $query = "UPDATE Account SET 
    account_label='".$obm_q->escape($label)."',
    account_bank='".$obm_q->escape($bank)."',
    account_number='".$obm_q->escape($number)."',
    account_today='$today',
    account_balance=$balance,
    account_comment='".$obm_q->escape($comment)."',
    account_userupdate=$uid,
    account_timeupdate=NOW()
    WHERE account_id $account_id $multidomain";  

  display_debug_msg ($query, $cdg_sql, 'run_query_account_update(1)');
  
  $retour = $obm_q->query($query);  

  return $retour;
}


///////////////////////////////////////////////////////////////////////////////
// returns all payments connected to the account
// if $p_date_before is set, matching payments must have 
//          payment_date < $p_date_before
///////////////////////////////////////////////////////////////////////////////
function run_query_account_search_payments ($p_account_id, $p_date_before = '') {
  global $cdg_sql;

  $multidomain = sql_multidomain('payment');
  $p_account_id = sql_parse_id($p_account_id, true);

  $query = "select *, payment_id as id,
    payment_amount
    from Payment
    where payment_account_id $p_account_id $multidomain";

  if ($p_date_before != '') {
    $query .= " and payment_date <= '$p_date_before'";
  }

  display_debug_msg($query, $cdg_sql, 'run_query_account_search_payments()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// returns all expected payments connected to the account
// if $p_date_before is set, matching payments must have 
//          payment_expected_date < $p_date_before
///////////////////////////////////////////////////////////////////////////////
function run_query_account_search_expected_payments($p_account_id, $p_date_before='') {
  global $cdg_sql;

  $multidomain = sql_multidomain('payment');
  $p_account_id = sql_parse_id($p_account_id, true);

  $query = "SELECT *, payment_id as id
    FROM Payment
    WHERE payment_account_id $p_account_id $multidomain";
  if ($p_date_before != '') {
    $query .= " and payment_date <= '$p_date_before'";
  }
  $query .= " and payment_checked = 0 "; // expected payments only

  display_debug_msg ($query, $cdg_sql, 'run_query_account_search_expected_payments()');
  $obm_q = new DB_OBM; 
  $obm_q->query($query);

  return $obm_q;
}


///////////////////////////////////////////////////////////////////////////////
// Delete query construction                                                 //
// Parameters:
//   - $a_id : account Id
///////////////////////////////////////////////////////////////////////////////
function run_query_account_delete($a_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('account');
  $a_id = sql_parse_id( $a_id, true);

  $query = "DELETE FROM Account WHERE account_id $a_id $multidomain";
  display_debug_msg ("<b>run_query_account_delete() : </b>$query", $cdg_sql);
  $obm_q = new DB_OBM;
  $obm_q->query($query);
}


///////////////////////////////////////////////////////////////////////////////
// given an account id $p_account_id and a date $p_date, returns a hash table :
// if $p_date == '', we use today as compute date
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
function account_compute_balance ($p_account_id, $p_date='') {
  global $cdg_sql;

  $date = ($p_date=='')?date('Y-m-d'):$p_date;
  $total_in_real = $total_out_real = 0 ; // payments banked
  $total_in_exp = $total_out_exp = 0; // payments expected
  $result = array();

  // we get the balance at creation time of this account :
  $balance  = account_creation_balance ($p_account_id);

  // first we get all payments connected to this account
  $q_payments = run_query_account_search_payments ($p_account_id, $date);
  $q_expected = run_query_account_search_expected_payments ($p_account_id, $date);


  // FIXME : faire en une fois, avec une seule requete qui r�cup�re tous
  // les payments qqu'ils soient ?
  // lets compute total ins and outs :
  while ($q_payments->next_record()) {
    if ($q_payments->f('payment_inout') == '+') {
      $total_in_real += $q_payments->f('payment_amount');
    } else {
      $total_out_real += $q_payments->f('payment_amount');
    }
  }

  while ($q_expected->next_record()) {
    if ($q_expected->f('payment_inout') == '+') {
      $total_in_exp += $q_expected->f('payment_amount');
    } else {
      $total_out_exp += $q_expected->f('payment_amount');
    }
  }

  $today   = $balance + $total_in_real - $total_out_real ;
  $balance = $today   + $total_in_exp - $total_out_exp ;

  // now we create the hash table we will return :
  $result['in_real'] = $total_in_real;
  $result['out_real'] = $total_out_real;
  $result['in_expected'] = $total_in_exp;
  $result['out_expected'] = $total_out_exp;
  $result['payments'] = $q_payments;
  $result['expected'] = $q_expected;
  $result['balance'] = $balance;
  $result['today'] = $today;

  return $result;
}


///////////////////////////////////////////////////////////////////////////////
// returns the balance of $p_account_id account at its creation
///////////////////////////////////////////////////////////////////////////////
function account_creation_balance ($p_account_id) {
  global $cdg_sql;

  $multidomain = sql_multidomain('account');

  $query = "SELECT account_balance
    FROM Account
    WHERE account_id ='$p_account_id' $multidomain";
  display_debug_msg ($query, $cdg_sql, 'account_creation_balance()');
  $obm_q = new DB_OBM;
  $obm_q->query($query);
  $obm_q->next_record();

  return $obm_q->f('account_balance');
}

?>
