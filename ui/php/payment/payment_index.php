<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : payment_index.php 
//     - Desc : payment Index File
// 2001-08-21 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////
// Session Management                                                        //
///////////////////////////////////////////////////////////////////////////////
$path = "..";
$section = "COMPTA";
$menu = "PAYMENT";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
require("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
require("$obminclude/global_pref.inc");
require("payment_display.inc");
require("payment_query.inc");
require("payment_js.inc");

update_last_visit("payment", $param_payment, $action);

page_close();

// $payment is a hash table containing, for each form field set 
// in the calling page, a couple var_name => var_value...
if($action == "")  $action = "index";
$payment = get_param_payment();
get_payment_action();
$perm->check_permissions($menu, $action);

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("$l_treso");
$display["header"] = generate_menu($menu, $section);

///////////////////////////////////////////////////////////////////////////////
// ACTIONS :
///////////////////////////////////////////////////////////////////////////////

if ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  if (true) {
    $display["msg"] = display_debug_msg("FIXME PERMISSIONS", $cdg_param);
    $display["detail"] = html_payment_form($action,"",run_query_paymentkind(), run_query_account());
  } else {
   $display["msg"] =  display_err_msg($l_error_permission);
  }

} elseif ($action == "new_with_invoices") {
///////////////////////////////////////////////////////////////////////////////
  if (true) {
    run_query_insert ($payment);
    $display["msg"] = display_ok_msg($l_insert_ok);

    // we re-load all values :
    $pay_q = run_query_search ($payment);
    $pay_q->next_record();
    $payment["payment"] = $pay_q->f("payment_id");

    $pay_q = run_query_detail ($payment["payment"]);
    $inv_q = run_query_search_connectable_invoices ($payment);
    $pref_inv_q = run_query_display_pref($auth->auth["uid"], "invoice");

    // we display all data concerning our freshly created payment
    $display["detail"] = html_payment_consult ($action, $pay_q, run_query_paymentkind (), run_query_account (), $inv_q, $pref_inv_q);

    // and we give the user a form to find invoices :
    $display["detail"] = html_chose_invoices_form ($action, $payment, $pref_inv_q, $inv_q);
  }
  else {
    $display["msg"] = display_err_msg($l_error_permission);
  }

} elseif ($action == "insert_with_invoices") {
///////////////////////////////////////////////////////////////////////////////
  // we don't seem to need checks here...
  // just know which invoices to connect to the payment

  // first, we need to kown which invoices have been selected :
  reset($HTTP_POST_VARS);

  while (list($key) = each ($HTTP_POST_VARS)) {
    if (strcmp(substr($key,0,4),"use_") == 0) {
      run_query_link_payment_to_invoice($payment, substr($key, 4));
    }
  }
  $display["msg"] = display_ok_msg($l_insert_paymentinvoice_ok);
  // then back to the search screen :
  $display["detail"] = html_payment_search_form($action,run_query_paymentkind(), run_query_account(), $payment);

} elseif ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  $display["search"] = html_payment_search_form($p_action, run_query_paymentkind(), run_query_account(), $payment);
  if ($set_display == "yes") {
    $pay_q = run_query_search($payment);
    $nb_payments = $pay_q->num_rows();
    if ($nb_payments == 0){
      $display["msg"] = display_warn_msg($l_no_found);
    } else {
      $pref_pay_q = run_query_display_pref($auth->auth["uid"],"payment");
      $display["result"] = html_payment_search_list($pay_q, $pref_pay_q, $nb_payments, $payment);
    }
  } else {
    $display["msg"] = display_ok_msg($l_no_display);
  }

} elseif ($action == "search")  { 
//////////////////////////////////////////////////////////////////////////////
  require("payment_js.inc");
  $display["search"] = html_payment_search_form($p_action, run_query_paymentkind(), run_query_account(), $payment);
  $pay_q = run_query_search($payment);
  $nb_payments = $pay_q->num_rows();
  if ($nb_payments == 0){
    $display["msg"] = display_warn_msg($l_no_found);
  } else {
    $pref_pay_q = run_query_display_pref ($auth->auth["uid"],"payment");
    $display["result"] =  html_payment_search_list ($pay_q, $pref_pay_q, $nb_payments, $payment);
  }

} elseif ($action == "detailconsult") {
///////////////////////////////////////////////////////////////////////////////
  if ($payment["payment"] > 0) {
    $pay_q = run_query_detail($payment["payment"]);
    $inv_q = run_query_search_connected_invoices($payment["payment"]);
    $pref_inv_q = run_query_display_pref($auth->auth["uid"], "invoice");
    $display["detailInfo"] = display_record_info($pay_q);
    $display["detail"] = html_payment_consult($action, $pay_q, run_query_paymentkind(), run_query_account(), $inv_q, $pref_inv_q);
  }

} elseif ($action == "detailupdate") {
  // detailupdate means changing the label or that kind of things...
  // nothing to do with invoice or account stuff
///////////////////////////////////////////////////////////////////////////////
  if ($payment["payment"] > 0) {
    $pay_q = run_query_detail($payment["payment"]);
    $display["detailInfo"] = display_record_info($pay_q);
    $display["detail"] = html_payment_form ($action, $pay_q, run_query_paymentkind(), run_query_account());
  }

///////////////////////////////////////////////////////////////////////////////
} /*
    never used ?
    
    elseif ($action == "bank") {
  // bank means that the payment is being received 
  // and must be validated, including connecting it to one or more invoice
  // and checking its amount, date, bank account concerned, etc.
///////////////////////////////////////////////////////////////////////////////
   if ($payment["payment"] > 0) {
     display_ok_msg (" action == bank");
     $pay_q = run_query_detail ($payment["payment"]);
     $display["detailInfo"] = display_record_info ($pay_q);
     html_payment_form ($action, $pay_q, run_query_paymentkind(), run_query_account());
  } 
///////////////////////////////////////////////////////////////////////////////
}

  */
elseif (($action == "search_invoice") || ($action == "search_invoice_new")) {
  // when banking a payment, we first look for invoices,
  // then we add the chosen ones (action == $add_invoices),
  // then we ask the amount of the payment, and we affect this amount
  // to all connected invoices.
  // last, we check that repartition and update the db if everything is ok,
  // or go back to the beginning if anything is ko...
///////////////////////////////////////////////////////////////////////////////
  $display["msg"] = display_debug_msg("FIXME : permissions", $cdg_param);
  // options d'affichage pour les invoices
  $pref_inv_q = run_query_display_pref($auth->auth["uid"],"invoice");
  // recherche des invoices selon label si la recherche a déjà été lancée,
  $inv_q = run_query_search_connectable_invoices($payment);
  // get invoices already connected to that payment :
  $q_invoices_connected = run_query_search_connected_invoices($payment["payment"]);
  //  $q_invoices_connected->next_record ();
  // first, we display payment info :
  $pay_q = run_query_detail($payment["payment"]);
  $display["detail"] = html_payment_consult($action, $pay_q, run_query_paymentkind(), run_query_account(), $q_invoices_connected, $pref_inv_q);
  
  // then the invoices search form
  $pref_inv_q = run_query_display_pref($auth->auth["uid"], "invoice");
  $display["detail"] .= html_chose_invoices_form($action, $payment, $pref_inv_q, $inv_q);

} elseif ($action == "add_invoices") {
///////////////////////////////////////////////////////////////////////////////
  // first, we need to kown which invoices have been selected :
  reset($HTTP_POST_VARS);

  $invoices_selected = array();
  while (list($key) = each ($HTTP_POST_VARS)){
    if (strcmp(substr($key,0,4),"use_")==0){
      $invoices_selected[] = substr($key,4);
    }
  }

  // then we need to get from base invoices already connected to 
  // that payment, but not paid yet...
  $invoices_connected = run_query_search_connected_invoices ($payment["payment"], 1);
  while ($invoices_connected->next_record()) {
    $invoices_selected[] = $invoices_connected->f("invoice_id");
  }

  // we got everything, let's begin affectation...
  html_payment_affect_invoices ($payment, $invoices_selected);
  
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "check_banking") {
///////////////////////////////////////////////////////////////////////////////
  // this is where we check user input made in (action == "add_invoices")...
  // we need to consider payments not connected to invoices,
  // like salaries or account/bank stuff...
  // we retrieve invoices data and put everything in an array :
  $all_invoices = get_invoices_data ($payment["nb_invoices"]);

  if (! isset ($check_done)){
    // now, let's check what the user typed in...
    $error = check_banking_data ($payment, $all_invoices);
  } else {
    $error = 0;
  }
    
  if ($error) {
   $display["detail"] = html_banking_problem ($error, $payment, $all_invoices);
  } else { 
    // everything is OK :
    run_query_do_banking ($payment, $all_invoices);
    $display["search"] = html_payment_search_form($action,run_query_paymentkind(), run_query_account (), $payment);
  }

} elseif ($action == "duplicate") {
///////////////////////////////////////////////////////////////////////////////
  $pay_q = run_query_detail($payment["payment"]);
  // we give the user the traditionnal form to modify his payment :
  $display["detail"] = html_payment_form ($action, $pay_q, run_query_paymentkind(), run_query_account());

} elseif ($action == "insert") {
///////////////////////////////////////////////////////////////////////////////
  run_query_insert ($payment);
  $display["msg"] = display_ok_msg ($l_insert_ok);
  $display["search"] = html_payment_search_form($action,run_query_paymentkind(), run_query_account (), $payment); 

///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "update") {
///////////////////////////////////////////////////////////////////////////////
  run_query_update ($payment); 
  $display["msg"] = display_ok_msg ($l_update_ok);

  $display["search"] = html_payment_search_form($action,run_query_paymentkind(), run_query_account(), $payment); 

///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "delete") {
///////////////////////////////////////////////////////////////////////////////
  $success = run_query_delete ($payment["payment"]);
  if ($success) {
    $display["msg"] = display_ok_msg ($l_delete_ok);
  } else {
    $display["msg"] = display_err_msg ($l_delete_error);
  }
  $display["search"] = html_payment_search_form ($action,run_query_paymentkind(),run_query_account(),'','','');
  
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "break_asso") {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_admin) {
    $display["msg"] = display_err_msg($l_error_permission);
  } else {
    // first, we get payment details :
    $pay_q = run_query_detail($payment["payment"]);
    // then invoices connected to that payment
    $inv_q = run_query_search_connected_invoices ($payment["payment"]);
    //$q_connected_invoices->next_record ();
    // display stuff :
    $pref_inv_q = run_query_display_pref ($auth->auth["uid"], "invoice");
    // at last, we can work for real :
   $display["detail"] = html_breaking_associations ($action, $pay_q, $inv_q, $pref_inv_q);
  }

} elseif ($action == "do_break_asso") {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_admin) {
    $display["msg"] = display_err_msg($l_error_permission);
  } else {
  // this is where we actually break associations, 
  // once we've checked the situation...
  // * situation == strong => payment is already paid and connected to 
  //   several invoices
  // * situation == soft => payment is already paid and connected to a 
  //   single invoice
  // * situation == easy => payment is unpaid, we expect the list of 
  //   selected invoices via HTTP_POST_VARS...
  if ($situation == "strong" || $situation =="soft") {
    // same work to do :
    $display["msg"] = display_debug_msg("situation = $situation", $cdg_param);
    // we get the list of connected invoices (only one if situation is soft)
    $invoices = run_query_search_connected_invoices ($payment["payment"]);
    while ($invoices->next_record ()) {
      // unpay the invoice
      payment_unpay_invoice ($invoices->f("invoice_id"));
      // unlink payment and invoice
      payment_invoice_unlink ($payment["payment"], $invoices->f("invoice_id"));
    }
    // unpay the payment
    payment_unpay ($payment["payment"]);

  } else {//$situation == "easy"
    $display["msg"] = display_debug_msg("situation = $situation", $cdg_param);
    // we delete the link between the payment and each selected invoice
    reset ($HTTP_POST_VARS);
    while (list($key) = each ($HTTP_POST_VARS)) {
      if (strcmp(substr($key,0,6), "break_") == 0) {
	$display["msg"] .= display_debug_msg ("cle $key<br>", $cdg_param);
	payment_invoice_unlink ($payment["payment"], substr($key,6));
      }
    }
  }

  // once it's done, we go back to payment search page...
  $display["search"] = html_payment_search_form($p_action, run_query_paymentkind(), run_query_account(), $payment);
  $display["msg"] = display_ok_msg($l_no_display);
  }
  
///////////////////////////////////////////////////////////////////////////////
} elseif ($action == "admin") {
///////////////////////////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {  
    $display["msg"] =  "<center>Nothing here for now</center><br />";
  }
  else {
    $display["msg"] = display_err_msg($l_error_permission);
  }	
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
elseif ($action =="reconcile") {
///////////////////////////////////////////////////////////////////////////////
  // display a form to choose the file containing
  // data from the bank in the csv format.
   $display["detail"] = html_choose_csv_file ();
}

///////////////////////////////////////////////////////////////////////////////
elseif ($action =="reconcile_import") {
///////////////////////////////////////////////////////////////////////////////
// we check the file uploaded, meaning we insert it in the table
// EntryTemp, and display it to the user.
// he can then choose to carry on or change the file...
// import_file = no means we already have filled entrytemp and
// we don't need to do it again...
  if ($import_file != "no") {
    if (!is_uploaded_file ($fichier_csv)) {
      $display["msg"] = display_err_msg( "$fichier_csv n'est pas un fichier uploadé !<br>");
       $display["detail"] = html_choose_csv_file ();
    }else { 
      // the dest file must be readable by everybody if you want mysql import
      $mon_fichier = tempnam ("/tmp/","csv_");
      exec ("chmod 604 $mon_fichier");
      // we have to register the name of the file to be able to delete it :
      $sess->register ("mon_fichier");
      //      copy ($fichier_csv, $mon_fichier);
      // we have to modify a little bit the file received, 
      // some lines to remove
      // Aliacom specific function ! 
      // edit to fit your needs...
      get_csv_ready ($fichier_csv, $mon_fichier);
      run_query_import_csv ($mon_fichier);
      $q_entrytemp = run_query_get_entrytemp ();
      $q_et_prefs = run_query_display_pref ($auth->auth['uid'], "entrytemp");
    
       $display["detail"] = html_reconcile_import ($q_entrytemp, $q_et_prefs);
    }
  } else {
    // we only display 
    $q_entrytemp = run_query_get_entrytemp ();
    $q_et_prefs = run_query_display_pref ($auth->auth['uid'], "entrytemp");
    
     $display["detail"] = html_reconcile_import ($q_entrytemp, $q_et_prefs);
  }
}

///////////////////////////////////////////////////////////////////////////////
elseif ($action == "select_reconcile") {
///////////////////////////////////////////////////////////////////////////////
  // when we arrive here the entrytemp table is filled up
  // with reasonably good values...
  echo "action == $action<br>";


  // we search if there are some payments or entrytemps checked
  // hanging around i $HTTP_POST_VARS :
  reset ($HTTP_POST_VARS);
//  echo "<br>";  var_dump ($HTTP_POST_VARS);
//  echo "<br>";

  $et_array = array ();
  $pay_array = array ();
  while (list($key) = each ($HTTP_POST_VARS)){
    if (strcmp(substr($key,0,9),"check_et_")==0) {
      // we had this id to our list : 
      $et_array[] = substr($key,9);
      // we have an entryTemp to update
      // run_query_check_entrytemp (substr($key,9));
    } elseif (strcmp(substr($key,0,10),"check_pay_")==0) {
      // we had this id to our list : 
      $pay_array[] = substr($key,10);
      // we have a payment to check
      // run_query_check_payment (substr($key,10));
    }
  }
  // if we have any elements in any of our 2 arrays, we have to
  // check what the user selected...
  if (count($et_array) != 0 || count($pay_array) != 0) {
    if (!check_reconcile ($pay_array, $et_array)) {
      $display["msg"] = display_err_msg ($l_reconcile_errors);
    }
  }

  // get all payments not checked yet :
  $q_payments = run_query_get_payments_to_reconcile ();
  $q_payment_pref = run_query_display_pref ($auth->auth["uid"], "payment");

  // then we get all EntryTemp not checked yet : 
  $q_et = run_query_get_entrytemp ();
  $q_et_pref = run_query_display_pref ($auth->auth["uid"], "entrytemp");
  
  // for the moment, we suppose that when all entrytemps of the table
  // are checked, reconcile is over...
  // so the button 'finished' is disabled until that moment

  // and we display the form to check them...
   $display["detail"] = html_select_reconcile ($q_payments, $q_et, $q_payment_pref, $q_et_pref);
}

///////////////////////////////////////////////////////////////////////////////
elseif ($action == "do_reconcile") {
///////////////////////////////////////////////////////////////////////////////
// at that step we check what payments/ets have been checked 
// and we update the real base, emptying the 2 temp tables...
  $display["msg"] = display_debug_msg ("action = $action", $cdg_param);

  // we check that no unchecked ET remains in the EntryTemp table.
  // if yes, we display an error page including a link to 
  // the select_reconcile action
  if (remains_entrytemps_in_table ()) {
     $display["detail"] = html_reconcile_error_page ($l_remains_entrytemps);
  } else {
    // we have checked that all payments and entrytemps were valid
    // time by time, so we don't check the sum of them now...

    // to update the db, all we have to do is get the list of all payments ids
    // that are checked in the PaymentTemp table and check them in the payment 
    // official table...
    $pay_id_list = run_query_get_checked_payments();
    $display["msg"] = display_debug_msg ("liste des ids : [$pay_id_list]", $cdg_param);

    // then we check all that payments :
    run_query_check_payments ($pay_id_list);
    // and we look for invoices that become checked to check them =)
    run_query_update_invoice_status ();
    // we can, at last, dele te the csv file :
    //    $sess->thaw ();
    if ($sess->is_registered("mon_fichier")) {
      unlink ($mon_fichier);
      $sess->unregister ("mon_fichier");
    } else {
      // should not happen...
    }

  }
}
///////////////////////////////////////////////////////////////////////////////
// Display stuff below :
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $payment_options=run_query_display_pref($auth->auth["uid"],"payment",1);
  $invoices_options = run_query_display_pref ($auth->auth["uid"], "invoice",1);
  $et_options = run_query_display_pref ($auth->auth["uid"], "entrytemp",1);
  $display["detail"] = dis_payment_display_pref ($payment_options, $invoices_options, $et_options); 

///////////////////////////////////////////////////////////////////////////////
}elseif ($action == "dispref_display") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update ($entity, $fieldname, $disstatus) ;
  $payment_options = run_query_display_pref($auth->auth["uid"],"payment",1);
  $invoices_options = run_query_display_pref ($auth->auth["uid"], "invoice",1);
  $et_options = run_query_display_pref ($auth->auth["uid"], "entrytemp",1);
   $display["detail"] = dis_payment_display_pref ($payment_options, $invoices_options, $et_options); 

///////////////////////////////////////////////////////////////////////////////
}elseif ($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update ($entity, $new_level, $fieldorder) ;
  $payment_options = run_query_display_pref($auth->auth["uid"],"payment",1);
  $invoices_options = run_query_display_pref ($auth->auth["uid"], "invoice",1);
  $et_options = run_query_display_pref ($auth->auth["uid"], "entrytemp",1);
   $display["detail"] = dis_payment_display_pref ($payment_options, $invoices_options, $et_options); 
}

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
$display["end"] = display_end();
//echo $display["end"];
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Payment parameters transmitted in $payment hash
// returns : $payment hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_payment() {
  global $tf_label, $tf_number, $tf_amount, $tf_date_after, $tf_invoice_label;
  global $tf_date_before, $rd_inout, $hd_inout, $sel_kind, $sel_account;
  global $tf_expected_date, $tf_date, $ta_comment;
  global $tf_deal, $tf_company;
  global $param_payment, $hd_param_payment;
  global $hd_amount, $hd_used_amount, $hd_account, $hd_number, $hd_kind;
  global $hd_nb_invoices, $tf_instant_value, $tf_invoice_company;
  global $rd_paid, $cb_checked;
  global $set_debug , $action;

  if (isset ($tf_amount)) $p_payment["amount"] = $tf_amount;
  if (isset ($hd_amount)) $p_payment["amount"] = $hd_amount;
  if (isset ($tf_label)) $p_payment["label"] = $tf_label;
  if (isset ($tf_number)) $p_payment["number"] = $tf_number;
  if (isset ($hd_number)) $p_payment["number"] = $hd_number;
  if (isset ($tf_date_after)) $p_payment["date_after"] = $tf_date_after;
  if (isset ($tf_date_before)) $p_payment["date_before"] = $tf_date_before;
  if (isset ($rd_inout)) $p_payment["inout"] = $rd_inout;
  if (isset ($hd_inout)) $p_payment["inout"] = $hd_inout;
  if (isset ($sel_kind)) $p_payment["kind"] = $sel_kind;
  if (isset ($hd_kind)) $p_payment["kind"] = $hd_kind;
  if (isset ($sel_account)) $p_payment["account"] = $sel_account;
  if (isset ($hd_account)) $p_payment["account"] = $hd_account;
  if (isset ($tf_expected_date)) $p_payment["expected_date"]=$tf_expected_date;
  if (isset ($tf_date)) $p_payment["date"] = $tf_date;
  if (isset ($param_payment)) $p_payment["payment"] = $param_payment;
  if (isset ($hd_param_payment)) $p_payment["payment"] = $hd_param_payment;
  if (isset ($ta_comment)) $p_payment["comment"] = $ta_comment;
  if (isset ($tf_invoice_label)) $p_payment["invoice_label"] = $tf_invoice_label;
  if (isset ($tf_invoice_company)) $p_payment["invoice_company"] = $tf_invoice_company;
  if (isset ($tf_instant_value)) $p_payment["used_amount"] = $tf_instant_value;
  if (isset ($hd_used_amount)) $p_payment["used_amount"] = $hd_used_amount;
  if (isset ($hd_nb_invoices)) $p_payment["nb_invoices"] = $hd_nb_invoices;
  if (isset ($rd_paid)) $p_payment["paid"] = $rd_paid;
  if (isset ($tf_deal)) $p_payment["deal"] = $tf_deal;
  if (isset ($tf_company)) $p_payment["company"] = $tf_company;
  if (isset ($cb_checked)) $p_payment["checked"] = $cb_checked;

  if ($set_debug > 0) {
    echo "<br />action = $action";
    if ( $p_payment ) {
      while ( list( $key, $val ) = each( $p_payment ) ) {
        echo "<br />payment[$key]=$val";
      }
    }
  }

  return $p_payment;
}
///////////////////////////////////////////////////////////////////////////////
// Stores Invoice data in a hash table.
// All data come from the banking form
// returns : an array containing a hash per line,
// each corresponding to an invoice...
// $nb is the total number of invoices
///////////////////////////////////////////////////////////////////////////////
function get_invoices_data ($nb) {
  global $HTTP_POST_VARS;
  global $set_debug, $cdg_param;

  $p_all_invoices = array ();
  for ($i = 0 ; $i < $nb; $i++) {
    $p_invoice = array();
    // all data concerning this invoice end with _$i in the form
    while (list ($key,$value) = each ($HTTP_POST_VARS)) {
      // if $cle is a number, we are interested
      $pos = strrpos($key, "_");
      $cle = substr($key, $pos);
      //   echo "invoice = $i ; key = $key<br>";
      if ($cle == "_$i") {
	// each $key is of the form "prefix_name_number"
	// we use the part "name" as a key in our new hash table
	// throw "_number"
	$my_key = substr($key,0,$pos);
	// throw "prefix_"
	$my_key = substr($my_key, strpos($my_key,"_")+1);
	$p_invoice[$my_key] = $value;
      }
      $p_all_invoices[$i] = $p_invoice;
    }
    /*    if ($set_debug > 0) {
       echo "<BR>i = $i";
       if ( $p_invoice ) {
	 while ( list( $key, $val ) = each( $p_invoice ) ) {
	   echo "<BR>p_invoice[$key]=$val";
	 }
       }
     }
    */
    
    reset ($HTTP_POST_VARS);
  }
  
  return $p_all_invoices;
}

//////////////////////////////////////////////////////////////////////////////
// Invoice actions
//////////////////////////////////////////////////////////////////////////////
function get_payment_action() {
  global $payment, $actions, $path;
  global $l_header_find,$l_header_new,$l_header_update,$l_header_delete;
  global $l_header_display,$l_header_reconcile,$l_header_admin;
  global $l_header_duplicate;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

//Index
  $actions["PAYMENT"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/payment/payment_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// New With Invoice
  $actions["PAYMENT"]["new_with_invoice"] = array (
    'Url'      => "$path/payment/payment_index.php?action=new_whith_invoice",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                        );

// New With Invoice
  $actions["PAYMENT"]["insert_with_invoice"] = array (
    'Url'      => "$path/payment/payment_index.php?action=new_whith_invoice",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                        );

// Search
  $actions["PAYMENT"]["search"] = array (
    'Url'      => "$path/payment/payment_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// New
  $actions["PAYMENT"]["new"] = array (
    'Name'     => $l_header_new,
    'Url'      => "$path/payment/payment_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('search','admin','index','detailconsult','display') 
                                     );

// Reconcile
  $actions["PAYMENT"]["reconcile"] = array (
    'Name'     => $l_header_reconcile,
    'Url'      => "$path/payment/payment_index.php?action=reconcile",
    'Right'    => $cright_write,
    'Condition'=> array ('admin','search','index','detailconsult','display') 
                                           );

// Duplicate
  $actions["PAYMENT"]["duplicate"] = array (
     'Name'     => $l_header_duplicate,
     'Url'      => "$path/payment/payment_index.php?action=duplicate&amp;param_payment=".$payment["payment"]."",
     'Right'    => $cright_write,
     'Condition'=> array ('detailconsult') 
                                           );

// Detail Consult
  $actions["PAYMENT"]["detailconsult"] = array (
    'Url'      => "$path/payment/payment_index.php?action=detailconsult",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// Detail Update
  $actions["PAYMENT"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/payment/payment_index.php?action=detailupdate&amp;param_payment=".$payment["payment"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	      );

// Search Invoice
  $actions["PAYMENT"]["search_invoice"] = array (
    'Url'      => "$path/payment/payment_index.php?action=search_invoice",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                                );

// Search Invoice New
  $actions["PAYMENT"]["search_invoice_new"] = array (
    'Url'      => "$path/payment/payment_index.php?action=search_invoice_new",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Add Invoices
  $actions["PAYMENT"]["add_invoices"] = array (
    'Url'      => "$path/payment/payment_index.php?action=add_invoices",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Check banking
  $actions["PAYMENT"]["check_banking"] = array (
    'Url'      => "$path/payment/payment_index.php?action=check_banking",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Insert
  $actions["PAYMENT"]["insert"] = array (
    'Url'      => "$path/payment/payment_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Update
  $actions["PAYMENT"]["update"] = array (
    'Url'      => "$path/payment/payment_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                                );

// Delete
  $actions["PAYMENT"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/payment/payment_index.php?action=delete&amp;param_payment=".$payment["payment"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	 );

// Break Association
  $actions["PAYMENT"]["break_asso"] = array (
    'Url'      => "$path/payment/payment_index.php?action=break_asso",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                             );

// Do Break Association
  $actions["PAYMENT"]["do_break_asso"] = array (
    'Url'      => "$path/payment/payment_index.php?action=do_break_asso",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                             );

// Admin
  $actions["PAYMENT"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/payment/payment_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Import Reconcile
  $actions["PAYMENT"]["reconcile_import"] = array (
    'Url'      => "$path/payment/payment_index.php?action=reconcile_import",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      		 );

// Select Reconcile
  $actions["PAYMENT"]["select_reconcile"] = array (
    'Url'      => "$path/payment/payment_index.php?action=select_reconcile",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      		 );

// Do Reconcile
  $actions["PAYMENT"]["do_reconcile"] = array (
    'Url'      => "$path/payment/payment_index.php?action=do_reconcile",
    'Right'    => $cright_write_admin,
    'Condition'=> array ('None') 
                                      		 );

// Display
  $actions["PAYMENT"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/payment/payment_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                      	 );

// Display Préférences
  $actions["PAYMENT"]["display_dispref"] = array (
    'Url'      => "$path/payment/payment_index.php?action=display_dispref",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	 );

// Display Level
  $actions["PAYMENT"]["display_level"] = array (
    'Url'      => "$path/payment/payment_index.php?action=display_level",
    'Right'    => $cright_read, 
    'Condition'=> array ('None') 
                                      	        );

}

</script>
