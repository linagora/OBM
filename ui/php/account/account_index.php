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
$module = "account";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => $auth_class_name, "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("account_display.inc");
require("account_query.inc");

update_last_visit("account", $param_account, $action);

page_close();

// $account is a hash table containing, for each form field set 
// in the calling page, a couple var_name, var_value...
if ($action == "") $action = "index";
$account = get_param_account();
get_account_action();
$perm->check_permissions($module, $action);


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
    $display["detailInfo"] = display_record_info($ac_q);
    $display["detail"] = html_account_consult($ac_q, $action);
  }
} elseif ($action == "detailupdate")  {
///////////////////////////////////////////////////////////////////////////////
  if ($account["account"] > 0) {
    $ac_q = run_query_detail($account["account"]);
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
    //    $payments_options = run_query_display_options ($auth->auth["uid"],"payment");
    $payments_prefs = run_query_display_pref ($auth->auth["uid"], "payment");

    $display["detail"] = html_compute_balance ($account["account"], $payments_prefs, $tf_balance_date);

  } else {
    $display["msg"] = display_err_msg($l_error_permission);
  } 
  /*  
$q_account = run_query_detail ($account["account"]);
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
$display["header"] = generate_menu($module, $section);

$display["head"] = display_head("$l_account");
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Account parameters transmitted in $account hash
// returns : $account hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_account() {
  global $tf_label, $tf_number, $tf_balance, $tf_bank;
  global $ta_comment, $tf_balance_date, $hd_balance, $param_account;

  if (isset ($tf_label)) $account["label"] = $tf_label;
  if (isset ($tf_number)) $account["number"] = $tf_number;
  if (isset ($param_account)) $account["account"] = $param_account;
  if (isset ($tf_balance)) $account["balance"] = $tf_balance;
  if (isset ($hd_balance)) $account["balance"] = $hd_balance;
  if (isset ($tf_bank)) $account["bank"] = $tf_bank;
  if (isset ($ta_comment)) $account["comment"] = $ta_comment;
  
  display_debug_param($account);

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
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index
  $actions["account"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/account/account_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

// Search
  $actions["account"]["search"] = array (
    'Url'      => "$path/account/account_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                       );

// New
  $actions["account"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/account/account_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','display') 
     		                     );

// Insert
  $actions["account"]["insert"] = array (
    'Url'      => "$path/account/account_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Detail Consult
  $actions["account"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/account/account_index.php?action=detailconsult&amp;param_account=".$account["account"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('compute_balance', 'detailupdate') 
                                       );

// Compute Balance
  $actions["account"]["compute_balance"] = array (
    'Name'     => $l_header_compute_balance,
    'Url'      => "$path/account/account_index.php?action=compute_balance&amp;param_account=".$account["account"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     		 );

// Detail Update
  $actions["account"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/account/account_index.php?action=detailupdate&amp;param_account=".$account["account"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'update')
                                     	      );

// Update
  $actions["account"]["update"] = array (
    'Url'      => "$path/account/account_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                       );

// Delete
  $actions["account"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/account/account_index.php?action=delete&amp;param_account=".$account["account"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	);

// Admin
  $actions["account"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/account/account_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                      );

// Display
  $actions["account"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Preferences
  $actions["account"]["dispref_display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

// Display Preferences
  $actions["account"]["level_display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/account/account_index.php?action=level_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                      	 );

}

</script>
