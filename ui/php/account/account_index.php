<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : account_query.inc                                            //
//     - Desc : account query File                                           //
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COMPTA";
$menu = "ACCOUNT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");

require("account_display.inc");
require("account_query.inc");

// bookmark 
if ( ($param_account == $last_account) && (strcmp($action,"delete")==0) ) {
  $last_account=$last_account_default;
} elseif  ( ($param_account > 0) && ($last_account != $param_account) ) {
  $last_account=$param_account;
  run_query_set_user_pref($auth->auth["uid"],"last_account",$param_account);
  $last_account_name = run_query_global_account_label($last_account);
}

page_close();

// $account is a hash table containing, for each form field set 
// in the calling page, a couple var_name, var_value...
if ($action == "") $action = "index";
$account = get_param_account();
get_account_action();
$perm->check();



///////////////////////////////////////////////////////////////////////////////
// Programme principal                                                       //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("account_js.inc");
  $display["search"] = html_account_search_form ($action, $account);
  if ($set_display == "yes") {
    $display["result"] = dis_account_search_list($account);
  } else {
    $display["msg"] = display_ok_msg($l_no_display);
  }

} elseif ($action == "search")  {
///////////////////////////////////////////////////////////////////////////////
  require("account_js.inc");
  $display["search"] = html_account_search_form($action, $account);
  $display["result"] = dis_account_search_list($account);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {
    require("account_js.inc");
    $display["detail"] = html_account_form($obm_q_accounts, $action);
  } else {
    $display["msg"] = display_err_msg($l_error_permission);
  }

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  run_query_insert($account);
  $display["msg"] = display_ok_msg($l_insert_ok);
  require("account_js.inc");
  $display["search"] = html_account_search_form($action, $account);

} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  require("account_js.inc");
  if ($account["account"] > 0) {
    $ac_q = run_query_detail($account["account"]);
    $ac_q->next_record();
    $display["detailInfo"] = display_record_info($ac_q);
    $display["detail"] = html_account_consult($ac_q, $action);
  }
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($account["account"] > 0) {
    $ac_q = run_query_detail($account["account"]);
    $ac_q->next_record();    
    require("account_js.inc");
    $display["detailInfo"] = display_record_info($ac_q);
    $display["detail"] = html_account_form($ac_q, $action);
  }

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  require("account_js.inc");
  run_query_update($account);

  if ($account["account"] > 0) {
    $ac_q = run_query_detail($account["account"]);
    $ac_q->next_record();
    $display["detailInfo"] = display_record_info($ac_q);
    $display["detail"] = html_account_consult($ac_q, $action);
  }
//   $display["msg"] = display_ok_msg($l_update_ok);
//   require("account_js.inc");
//   $display["search"] = html_account_search_form($action, $account);
  
} elseif ($action == "delete")  {
///////////////////////////////////////////////////////////////////////////////
  // checking that no payment is linked to this account
  $q_related_payments = run_query_search_payments ($account["account"]);
  if ($q_related_payments->nf() != 0){
    $display["detail"] = html_impossible_deletion ($account["account"], $q_related_payments);
    // maybe a confirmation from the user would be enough...
  } else {
    run_query_delete($account["account"]);
    $display["msg"] = display_ok_msg($l_delete_ok);
    require("account_js.inc");
    $display["search"] = html_account_search_form($action,'');
  }

} elseif ($action == "compute_balance") {
///////////////////////////////////////////////////////////////////////////////
  /*  if (true){
    display_ok_msg ("FIXME PERMISSIONS");
    require ("account_js.inc");
    $q_account = run_query_detail ($account["account"]);
    $q_account->next_record();
    // used to compute today balance :
    $q_payments = run_query_search_payments($account["account"], date ("Y-m-d"));
    // used to compute balance on $tf_balance_date :
    $q_expected_payments = run_query_search_expected_payments ($account["account"], $tf_balance_date);
    $payments_options = run_query_display_options ($auth->auth["uid"], "payment");
    $expected_payments_options = run_query_display_options ($auth->auth["uid"], "payment");
    html_compute_balance ($q_account, $q_payments, $q_expected_payments, $payments_options, $expected_payments_options, $tf_balance_date);
  } else{
    display_err_msg($l_error_permission);
  }
  */
  if (true) {
    // account_js.inc needed to check date input by user...
    require ("account_js.inc");
    $display["msg"] = display_ok_msg ("FIXME PERMISSIONS");
    //$q_account = run_query_detail ($account["account"]);
    //$q_account->next_record();
    //    $payments_options = run_query_display_options ($auth->auth["uid"],"payment");
    $payments_prefs = run_query_display_pref ($auth->auth["uid"], "payment");

    $display["detail"] = html_compute_balance ($account["account"], $payments_prefs, $tf_balance_date);

  } else {
    $display["msg"] = display_err_msg($l_error_permission);
  } 
  /*  
$q_account = run_query_detail ($account["account"]);
  $q_account->next_record(); 
  $today = account_compute_balance ($q_account); 
  $other = account_compute_balance ($q_account, $tf_balance_date);  
  echo "<br>calcul du solde pour aujourd'hui : <br>"; 
  var_dump($today); 
  echo "<br>calcul du solde pour le $tf_balance_date : <br>"; 
  var_dump ($other); 
  echo "<br><br><br>"; 
  */

} elseif ($action == "admin")  {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {  
    $display["msg"] = "<center>Nothing here for now</center><br />";
  } else {
    $display["msg"] = display_err_msg($l_error_permission);
  }	
    
} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $account_options=run_query_display_pref($auth->auth["uid"], "account",1);
  $payment_options=run_query_display_pref ($auth->auth["uid"], "payment",1);
  $display["detail"] = dis_account_display_pref ($account_options, $payment_options); 

} else if($action =="dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $disstatus);
  $pref_account_q = run_query_display_pref($uid,"account",1);
  $pref_payment_q = run_query_display_pref($uid,"payment",1);
  $display["detail"] = dis_account_display_pref($pref_account_q, $pref_payment_q);

} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_account_q = run_query_display_pref($uid,"account",1);
  $pref_payment_q = run_query_display_pref($uid,"payment",1);
  $display["detail"] = dis_account_display_pref($pref_account_q, $pref_payment_q);
}


///////////////////////////////////////////////////////////////////////////////
// Display HTML page
///////////////////////////////////////////////////////////////////////////////
$display["header"] = generate_menu($menu, $section);

$display["head"] = display_head("$l_account");
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Account parameters transmitted in $account hash
// returns : $account hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_account() {
  global $tf_label, $tf_number, $tf_balance, $tf_bank;
  global $ta_comment, $tf_balance_date, $param_account;
  global $hd_balance;
  global $set_debug, $cdg_param, $action;

  if (isset ($tf_label)) $account["label"] = $tf_label;
  if (isset ($tf_number)) $account["number"] = $tf_number;
  if (isset ($param_account)) $account["account"] = $param_account;
  if (isset ($tf_balance)) $account["balance"] = $tf_balance;
  if (isset ($hd_balance)) $account["balance"] = $hd_balance;
  if (isset ($tf_bank)) $account["bank"] = $tf_bank;
  if (isset ($ta_comment)) $account["comment"] = $ta_comment;
  
  if (($set_debug > 0) && (($set_debug & $cdg_param) == $cdg_param)) {
    echo "<br />action = $action";
    if ( $account ) {
      while ( list( $key, $val ) = each( $account ) ) {
        echo "<br />account[$key]=$val";
      }
    }
  }

  return $account;
}


///////////////////////////////////////////////////////////////////////////////
// Account actions
///////////////////////////////////////////////////////////////////////////////
function get_account_action() {
  global $account, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_display,$l_header_admin,$l_header_compute_balance;
  global $l_header_consult;
  global $account_read, $account_write, $account_admin_read, $account_admin_write;

// Index
  $actions["ACCOUNT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/account/account_index.php?action=index",
    'Right'    => $account_read,
    'Condition'=> array ('all') 
                                       );

// Search
  $actions["ACCOUNT"]["search"] = array (
    'Url'      => "$path/account/account_index.php?action=search",
    'Right'    => $account_read,
    'Condition'=> array ('None') 
                                       );

// New
  $actions["ACCOUNT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/account/account_index.php?action=new",
    'Right'    => $account_write,
    'Condition'=> array ('','search','index','detailconsult','display') 
     		                     );

// Insert
  $actions["ACCOUNT"]["insert"] = array (
    'Url'      => "$path/account/account_index.php?action=insert",
    'Right'    => $account_write,
    'Condition'=> array ('None') 
                                       );

// Detail Consult
  $actions["ACCOUNT"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/account/account_index.php?action=detailconsult&amp;param_account=".$account["account"]."",
    'Right'    => $account_read,
    'Condition'=> array ('compute_balance') 
                                       );

// Compute Balance
  $actions["ACCOUNT"]["compute_balance"] = array (
    'Name'     => $l_header_compute_balance,
    'Url'      => "$path/account/account_index.php?action=compute_balance&amp;param_account=".$account["account"]."",
    'Right'    => $account_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

// Detail Update
  $actions["ACCOUNT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/account/account_index.php?action=detailupdate&amp;param_account=".$account["account"]."",
    'Right'    => $account_write,
    'Condition'=> array ('detailconsult') 
                                     	      );

// Update
  $actions["ACCOUNT"]["update"] = array (
    'Url'      => "$path/account/account_index.php?action=update",
    'Right'    => $account_write,
    'Condition'=> array ('None') 
                                       );

// Delete
  $actions["ACCOUNT"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/account/account_index.php?action=delete&amp;param_account=".$account["account"]."",
    'Right'    => $account_write,
    'Condition'=> array ('detailconsult', 'detailupdate') 
                                     	);

// Admin
  $actions["ACCOUNT"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/account/account_index.php?action=admin",
    'Right'    => $account_admin_read,
    'Condition'=> array ('all') 
                                      );

// Display
  $actions["ACCOUNT"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=display",
    'Right'    => $account_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions["ACCOUNT"]["dispref_display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=dispref_display",
    'Right'    => $account_read,
    'Condition'=> array ('None') 
                                      	 );

// Display Preferences
  $actions["ACCOUNT"]["level_display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=level_display",
    'Right'    => $account_read,
    'Condition'=> array ('None') 
                                      	 );

}
</SCRIPT>
