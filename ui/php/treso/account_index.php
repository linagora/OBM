<SCRIPT language=php>
// account_index.php
// creation, deletion and update of bank accounts

///////////////////////////////////////////////////////////////////////////////
// Session,Auth,Perms Management                                             //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("admin");

$menu="ACCOUNT";
include("$obminclude/global.inc");
include("$obminclude/global_pref.inc");
require("account_display.inc");
require("account_query.inc");

// bookmark 
if ( ($param_account == $last_account) && (strcmp($action,"delete")==0) ) {
  $last_account=$last_account_default;
} elseif  ( ($param_account > 0) && ($last_account != $param_account) ) {
    $last_account=$param_account;
    run_query_set_options_user($auth->auth["uid"],"last_account",$param_account);
    $last_account_name = run_query_global_account_label($last_account);

}

page_close();

// $account is a hash table containing, for each form field set 
// in the calling page, a couple var_name, var_value...
$account = get_param_account();


///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_account);  // Head & Body
generate_menu($menu);      // Menu
display_bookmarks();

///////////////////////////////////////////////////////////////////////////////
// Programme principal                                                       //
///////////////////////////////////////////////////////////////////////////////

if ($action == "index") {
//////////////////////////////////////////////////////////////////////////////
  require("account_js.inc");
  html_account_search_form ($action, $account);
  if ($set_display == "yes") {

    $obm_q=run_query_search($account, $new_order, $new_order2, $order_dir);
    $nb_accounts = $obm_q->num_rows();
    if ($nb_accounts == 0) {
      display_warn_msg($l_no_found);
   } else {
     $display_prefs = run_query_display_pref ($auth->auth["uid"], "account");
     //run_query_display_options($auth->auth["uid"], "account");

     html_account_search_list($obm_q, $display_prefs, $nb_accounts, $account);
   }
     
  }else {
    display_ok_msg($l_no_display);
  }
} elseif ($action == "search")  {
///////////////////////h//////////////////////////////////////////////////////
  require("account_js.inc");
  html_account_search_form($action, $account);
  
  $obm_q=run_query_search($account, $new_order, $new_order2, $order_dir);

  $nb_accounts = $obm_q->num_rows();
  if ($nb_accounts == 0) {
    display_warn_msg($l_no_found);
  } else {
    //    $obm_q_options_display_accounts=run_query_display_options ($auth->auth["uid"], "account");
    $display_pref = run_query_display_pref ($auth->auth["uid"], "account");
    
    html_account_search_list($obm_q, $display_pref, $nb_accounts, $account);
  }
} elseif ($action == "new") {
//////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {
    require("account_js.inc");
    html_account_form($obm_q_accounts, $action);
  }
  else {
    display_error_permission();
  }
} elseif ($action == "insert")  {
///////////////////////h//////////////////////////////////////////////////////
  run_query_insert($account);
   
  display_ok_msg($l_insert_ok);

  require("account_js.inc");
  html_account_search_form($action, $account);

} elseif ($action == "detailconsult")  {
///////////////////////h//////////////////////////////////////////////////////

  require("account_js.inc");
  if ($account["account"] > 0) {    
    $obm_q_account=run_query_detail($account["account"]);
    $obm_q_account->next_record();
    display_record_info($obm_q_account->f("account_usercreate"),$obm_q_account->f("account_userupdate"),$obm_q_account->f("timecreate"),$obm_q_account->f("timeupdate"));
    
    html_account_consult($obm_q_account, $action);
  };
} elseif ($action == "detailupdate")  {
///////////////////////h//////////////////////////////////////////////////////
  if ($account["account"] > 0) {
     
    $obm_q_account=run_query_detail($account["account"]);
    $obm_q_account->next_record();    
    require("account_js.inc");
    display_record_info($obm_q_account->f("account_usercreate"),$obm_q_account->f("account_userupdate"),$obm_q_account->f("timecreate"),$obm_q_account->f("timeupdate"));

    html_account_form($obm_q_account,$action);
  };

} elseif ($action == "update")  {
///////////////////////h//////////////////////////////////////////////////////
  run_query_update($account);
     
  display_ok_msg($l_update_ok);
  
  require("account_js.inc");
  html_account_search_form($action, $account);
  
} elseif ($action == "delete")  {
///////////////////////h//////////////////////////////////////////////////////
  // checking that no payment is linked to this account
  $q_related_payments = run_query_search_payments ($account["account"]);
  if ($q_related_payments->nf() != 0){
    html_impossible_deletion ($account["account"], $q_related_payments);
    // maybe a confirmation from the user would be enough...
  }else{
    run_query_delete($account["account"]);
    display_ok_msg($l_delete_ok);
    require("account_js.inc");
    html_account_search_form($action,'');
  }
} elseif ($action == "compute_balance") {
///////////////////////h////////////////////////////////////////////////////////
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
    display_error_permission();
  }
  */
  if (true) {
    // account_js.inc needed to check date input by user...
    require ("account_js.inc");
    display_ok_msg ("FIXME PERMISSIONS");
    //$q_account = run_query_detail ($account["account"]);
    //$q_account->next_record();
    //    $payments_options = run_query_display_options ($auth->auth["uid"],"payment");
    $payments_prefs = run_query_display_pref ($auth->auth["uid"], "payment");

    html_compute_balance ($account["account"], $payments_prefs, $tf_balance_date);

  } else {
    display_error_permission();
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

}elseif ($action == "admin")  {
///////////////////////h////////////////////////////////////////////////////////
   if ($auth->auth["perm"] != $perms_user) {  
    	echo "<CENTER><FONT color=\"#$col_error\">";
    	echo "To come...";
    	echo "</FONT></CENTER><BR>";
   }
   else {
	display_error_permission();
   }	

/////////////////////////////////////////////////////////////////////////
// display stuff below :
/////////////////////////////////////////////////////////////////////////
    

/////////////////////////////////////////////////////////////////////////
} elseif ($action =="display") {
/////////////////////////////////////////////////////////////////////////
  $account_options=run_query_display_pref($auth->auth["uid"], "account",1);
  $payment_options=run_query_display_pref ($auth->auth["uid"], "payment",1);
  dis_account_display_pref ($account_options, $payment_options); 

} else if($action =="dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update($entity, $fieldname, $display);
  $pref_account_q = run_query_display_pref($uid,"account",1);
  $pref_payment_q = run_query_display_pref($uid,"payment",1);
  dis_account_display_pref($pref_account_q, $pref_payment_q);

} else if($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update($entity, $new_level, $fieldorder);
  $pref_account_q = run_query_display_pref($uid,"account",1);
  $pref_payment_q = run_query_display_pref($uid,"payment",1);
  dis_account_display_pref($pref_account_q, $pref_payment_q);

}
///////////////////////////////////////////////////////////////////////////////
// Display end of page
///////////////////////////////////////////////////////////////////////////////
display_end();


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
    echo "<BR>action = $action";
    if ( $account ) {
      while ( list( $key, $val ) = each( $account ) ) {
        echo "<BR>account[$key]=$val";
      }
    }
  }

  return $account;
}

</SCRIPT>
