<SCRIPT language=php>
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_index.inc
//     - Desc : Invoice Main file
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
$obminclude = getenv("OBM_INCLUDE_VAR");
require("$obminclude/phplib/obmlib.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
$perm->check("admin");

$menu="INVOICE";

include("$obminclude/global.inc");
require("invoice_display.inc");
require("invoice_query.inc");
require("$obminclude/global_query.inc"); 

// bookmark 
if ( ($param_invoice == $last_invoice) && (strcmp($action,"delete")==0) ) {
  $last_invoice=$last_invoice_default;
} elseif  ( ($param_invoice > 0) && ($last_invoice != $param_invoice) ) {
    $last_invoice=$param_invoice;
    run_query_set_options_user($auth->auth["uid"],"last_invoice",$param_invoice);
}

page_close();

// $invoice is a hash table containing, for each form field set 
// in the calling page, a couple var_name => var_value...
$invoice = get_param_invoice();

include("$obminclude/global_display.inc");

///////////////////////////////////////////////////////////////////////////////
// Beginning of HTML Page                                                    //
///////////////////////////////////////////////////////////////////////////////
display_head($l_payment);  // Head & Body
generate_menu($menu);      // Menu
display_bookmarks();

///////////////////////////////////////////////////////////////////////////////
// Programme principal                                                       //
///////////////////////////////////////////////////////////////////////////////
if ($action == "index") {
//////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  html_invoice_search_form ($action,run_query_invoicestatus(), $invoice); 
  if ($set_display == "yes") { 

    $obm_q=run_query_search($invoice, $new_order, $new_order2, $order_dir); 
    $nb_invoices = $obm_q->num_rows(); 
    if ($nb_invoices == 0) { 
      display_warn_msg($l_no_found);
   } else { 
     $obm_q_options_display_invoices=run_query_display_pref($auth->auth["uid"],"invoice");

     html_invoice_search_list($obm_q, $obm_q_options_display_invoices, $nb_invoices, $invoice); 
   } 
     
  }else { 
    display_ok_msg($l_no_display); 
  } 


} elseif ($action == "search")  { 
//////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  html_invoice_search_form($action, run_query_invoicestatus(), $invoice);
    
  $obm_q=run_query_search($invoice, $new_order, $new_order2, $order_dir);
  
  $nb_invoices = $obm_q->num_rows();
  
  if ($nb_invoices == 0) { 
    display_warn_msg($l_no_found);
  } else {
    $obm_q_options_display_invoices=run_query_display_pref($auth->auth["uid"],"invoice");
    
    html_invoice_search_list($obm_q,$obm_q_options_display_invoices,$nb_invoices,$invoice);
  }
  
} elseif ($action == "new") {
//////////////////////////////////////////////////////////////////////////////
// FIXME permissions 
  if ($auth->auth["perm"] != $perms_user) {
    require("invoice_js.inc"); 
    html_invoice_form($obm_q_invoice, $action, run_query_invoicestatus(),0, $deal_linked);
  }
  else {
    display_error_permission();
  }

}elseif ($action == "insert")  {
///////////////////////h//////////////////////////////////////////////////////
  run_query_insert($invoice, $hd_deal_linked);
  
  display_ok_msg($l_insert_ok);
  require("invoice_js.inc");

  html_invoice_search_form($action, run_query_invoicestatus(), $invoice);
  
} elseif ($action == "detailconsult")  {
///////////////////////h//////////////////////////////////////////////////////
  require("invoice_js.inc");
  if ($param_invoice > 0) {    
    $obm_q_invoice=run_query_detail($param_invoice);
    $obm_q_deals = run_query_search_deal_invoice($param_invoice);
    $obm_q_payment = run_query_search_payment_invoice ($param_invoice);

    $obm_q_invoice->next_record();

    $options_deal = run_query_display_pref($auth->auth["uid"],"deal");
    $options_payment = run_query_display_pref ($auth->auth["uid"], "payment");
    display_record_info($obm_q_invoice->f("invoice_usercreate"),$obm_q_invoice->f("invoice_userupdate"),$obm_q_invoice->f("timecreate"),$obm_q_invoice->f("timeupdate"));
    
    html_invoice_consult($action,$obm_q_invoice, run_query_invoicestatus(),$obm_q_deals, $options_deal,$obm_q_payment, $options_payment);
  };


} elseif ($action == "detailupdate")  { 
///////////////////////h//////////////////////////////////////////////////////
    if ($param_invoice > 0) {
     
    $obm_q_invoice=run_query_detail($param_invoice);
    $obm_q_invoice->next_record();    
    require("invoice_js.inc");
    display_record_info($obm_q_invoice->f("invoice_usercreate"),$obm_q_invoice->f("invoice_userupdate"),$obm_q_invoice->f("timecreate"),$obm_q_invoice->f("timeupdate"));
    $q_deals = run_query_search_deal_invoice ($obm_q_invoice->f("invoice_id"));
    html_invoice_form($obm_q_invoice,$action, run_query_invoicestatus(), $q_deals->nf());
    };

} elseif ($action == "update")  {
///////////////////////h//////////////////////////////////////////////////////
  run_query_update($invoice); 
     
  display_ok_msg($l_update_ok); 
   
  require("invoice_js.inc"); 
  html_invoice_search_form($action, run_query_invoicestatus(), $invoice);
  

} elseif ($action == "updatearchive")  {
///////////////////////h//////////////////////////////////////////////////////
  reset ($HTTP_POST_VARS);
  $nb_invoices = 0;

  while (list($key, $val) = each ($HTTP_POST_VARS)) {
       if(strcmp(substr($key, 0, 8),"archive_") == 0) {
	 run_query_update_archive (substr($key,8));
	 $nb_invoices++;
       }
  }
  display_ok_msg ($l_archive_ok);
  echo $nb_invoices . " " . $l_archive_number . "<BR>\n";
  
  require ("invoice_js.inc");
  html_invoice_search_form ($action, run_query_invoicestatus(), $invoice);

}elseif ($action=="add_deal"){
  /////////////////////////////////////////////////////////////////////////// 
  if (true){
    display_debug_msg("FIXME PERMISSION", $cdg_param);
    $obm_q_invoice = run_query_detail($invoice["invoice"]);
    $obm_q_invoice->next_record();
    $dis_options_deal = run_query_display_pref($auth->auth["uid"],"deal");

    dis_search_deal_form ($action, $obm_q_invoice, $dis_options_deal);
  }else{
    display_error_permissions();
  }
}elseif ($action == "search_deal"){
//////////////////////////////////////////////////////////////////////////////
  if (true){
    display_debug_msg("FIXME PERMISSION : ", $cdg_param);
    $obm_q_invoice = run_query_detail($invoice["invoice"]);
    $obm_q_invoice->next_record();
    $dis_options_deal = run_query_display_pref($auth->auth["uid"],"deal");
    $obm_q_deal = run_query_deal ($obm_q_invoice, $tf_deal_label, $tf_deal_company, $cb_deal_archive);
    dis_search_deal_form ($action, $obm_q_invoice, $dis_options_deal, $obm_q_deal,$tf_deal_label, $tf_deal_company, $cb_deal_archive);
  }else{
    display_error_permissions();
  }
}elseif ($action == "add_deal_chosen") {
  //////////////////////////////////////////////////////////////////////////////
  if (true){
    display_debug_msg ("FIXME : PERMISSIONS", $cdg_param);
    reset($HTTP_POST_VARS);
    $nb_deals_added = 0;
    while (list($key) = each ($HTTP_POST_VARS)){
      display_debug_msg ("traitement du deal : $key", $cdg_param);
      if (strcmp(substr($key,0,4),"add_")==0){
	run_query_add_deal ($invoice["invoice"], substr($key,4));
	$nb_deals_added++;
      }
    }

    if ($nb_deals_added!=0){
      run_query_update_updater ($auth, $invoice["invoice"]);
    }

    display_ok_msg ($l_insert_deal_ok);
    $action = "detailconsult";
    $obm_q_invoice = run_query_detail ($invoice["invoice"]);
    $obm_q_deals = run_query_search_deal_invoice($invoice["invoice"]);
    $obm_q_payment = run_query_search_payment_invoice ($param_invoice);
    $obm_q_invoice->next_record();

    $options_deal = run_query_display_pref($auth->auth["uid"],"deal");
    $options_payment = run_query_display_pref ($auth->auth["uid"], "payment");

    display_record_info($obm_q_invoice->f("invoice_usercreate"),$obm_q_invoice->f("invoice_userupdate"),$obm_q_invoice->f("timecreate"),$obm_q_invoice->f("timeupdate"));
    
    html_invoice_consult($action,$obm_q_invoice, run_query_invoicestatus(),$obm_q_deals, $options_deal,$obm_q_payment, $options_payment);

  }else{
    display_error_permissions();
  }
}elseif ($action == "del_deal") {
  ///////////////////////h//////////////////////////////////////////////////////
  if (true) {
    display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $q_invoice->next_record();
    $invoice_deals = run_query_search_deal_invoice($invoice["invoice"]);
    $q_payments = run_query_search_payment_invoice ($invoice["invoice"]);
    $deal_dis_options = run_query_display_pref ($auth->auth["uid"],"deal");
    $payments_options = run_query_display_pref ($auth->auth["uid"], "payment");
    html_invoice_consult ($action, $q_invoice, run_query_invoicestatus(),$invoice_deals, $deal_dis_options, $q_payments, $payments_options); 

  }else {
    display_error_permissions();
  }

}elseif ($action == "del_deal_chosen"){
  ///////////////////////h//////////////////////////////////////////////////////
  if (true){
    display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
    reset ($HTTP_POST_VARS);
    $deals_related = run_query_search_deal_invoice ($invoice["invoice"]);
    $q_payments = run_query_search_payment_invoice ($invoice["invoice"]);
    $nb_payments = $q_payments->nf();
    $nb_deals_remaining = $deals_related->nf ();
    $nb_deals_deleted  = 0;

    while (list($key) = each ($HTTP_POST_VARS)){
      display_debug_msg ("nb_deals_deleted = $nb_deals_deleted<br>nb_deals_remaining = $nb_deals_remaining<br>", $cdg_param);
      // no deletion of the last deal if there are still payments
      // we accept deletion of deals anytime...
      //if (($nb_deals_remaining == 1) && ($nb_payments != 0)) {
      //	html_error_last_deal_remove ($q_invoice);
      //break; // out of the "while"
      //} else {
	if (strcmp (substr($key,0,4), "del_") == 0){
	  run_query_remove_deal ($invoice["invoice"], substr($key,4));
	  $nb_deals_deleted++;
	  $nb_deals_remaining--;
	}
	//}
    }
    if ($nb_deals_deleted > 0) {
      // update the invoice update date
      run_query_update_updater ($auth, $invoice["invoice"]);
      display_ok_msg ($l_remove_deal_ok);
    }
    $action = "del_deal";
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $q_invoice->next_record();
    $q_deals = run_query_search_deal_invoice($invoice["invoice"]);
    $options_deal = run_query_display_pref($auth->auth["uid"], "deal");
    $q_payments = run_query_search_payment_invoice ($invoice["invoice"]);
    $payments_options = run_query_display_pref ($auth->auth["uid"], "payment");

    display_record_info($q_invoice->f("invoice_usercreate"),$q_invoice->f("invoice_userupdate"),$q_invoice->f("timecreate"),$q_invoice->f("timeupdate"));
    
    html_invoice_consult($action, $q_invoice, run_query_invoicestatus(),$q_deals, $options_deal, $q_payments, $payments_options);
  }else{
    display_error_permissions();
  }

} /*elseif ($action =="add_payment") {
  ///////////////////////h//////////////////////////////////////////////////////
  if (true) {
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $q_invoice->next_record();
    // an invoice must be connected to at least one deal to receive a payment
    $deals_related = run_query_search_deal_invoice ($invoice["invoice"]);
    if ($deals_related->nf() == 0){
      html_error_no_deals_related ($q_invoice);
    }
    else{
      display_ok_msg ("PERMISSIONS");
      $dis_options_payment = run_query_display_option ($auth, "payment");
      dis_search_payment_form ($q_invoice, $dis_options_payment);
    }
  } else {
    display_error_permissions();
  }
} elseif ($action =="search_payment") {
  ///////////////////////h//////////////////////////////////////////////////////
  if (true) {
    display_ok_msg ("PERMISSIONS");
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $q_invoice->next_record();
    $dis_options_payment = run_query_display_option ($auth, "payment");
    $q_payment = run_query_payment ($q_invoice, $tf_payment_label);
    dis_search_payment_form ($q_invoice, $dis_options_payment, $q_payment);
  } else {
    display_error_permissions();
  }
} elseif ($action == "check_payment_chosen") {
  //////////////////////////////////////////////////////////////////////////////
  
  html_check_payments ($invoice["invoice"], $hd_payments_used, $tf_to_use_amount, $hd_solde, $hd_invoice_reste_a_payer); 
  
}elseif ($action == "affect_payment") {
  //////////////////////////////////////////////////////////////////////////////
  if (true){
    display_debug_msg ("FIXME : PERMISSIONS", $cdg_param);
    reset($HTTP_POST_VARS);
    $nb_payments_added = 0;
    $payments_to_proceed = array();
    // putting in an array payments id to use 
    while (list($key) = each ($HTTP_POST_VARS)) {
      if (strcmp(substr($key,0,4),"add_")==0){
	$payments_to_proceed[] = substr($key,4);
      }
    }
    // let's go !
    // invoice data 
    $q_invoice = run_query_detail ($invoice["invoice"]);
    // already connected payments data 
    $q_payments_invoice = run_query_search_payment_invoice ($invoice["invoice"]);

    html_form_add_payments ($q_invoice, $q_payments_invoice, $payments_to_proceed, $nb_payments_added);

    if ($nb_payments_added!=0){
      run_query_update_updater ($auth, $invoice["invoice"]);
    }
  }else{
    display_error_permissions();
  }
}elseif ($action == "del_payment") {
  ///////////////////////h//////////////////////////////////////////////////////
  if (true) {
    display_debug_msg ("FIXME PERMISSIONS", $cdg_param);
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $q_invoice->next_record();
    
    $payment_dis_options = run_query_display_option ($auth,"payment");
    $invoice_payments = run_query_search_payment_invoice($invoice["invoice"]);
    $deals_options = run_query_display_option ($auth, "deal");
    $q_deals = run_query_search_deal_invoice ($invoice["invoice"]);
    html_invoice_consult ($action, $q_invoice, run_query_invoicestatus(),$q_deals, $deals_options, $invoice_payments, $payment_dis_options); 
  }else {
    display_error_permissions();
  }

} elseif ($action =="del_payment_chosen") {
  ///////////////////////h//////////////////////////////////////////////////////
  if (true) {
    display_ok_msg ("FIXME PERMISSIONS");
    reset($HTTP_POST_VARS);
    $nb_payments_deleted = 0;
    while (list($key) = each ($HTTP_POST_VARS)){
      if (strcmp(substr($key,0,4),"del_")==0){
	run_query_remove_payment ($invoice["invoice"], substr($key,4));
	$nb_payments_deleted++;
      }
    }
    if ($nb_payments_deleted!=0) {
      run_query_update_updater ($auth, $invoice["invoice"]);
    }

    $page = 0;
    $action = "search_payment";
    $obm_q_invoice = run_query_detail ($invoice["invoice"]);
    $obm_q_payments = run_query_search_payment_invoice($invoice["invoice"]);
    $obm_q_deals = run_query_search_deal_invoice ($invoice["invoice"]);
    $obm_q_invoice->next_record();

    $options_payment = run_query_display_option($auth,"payment");
    $options_deal = run_query_display_option ($auth, "deal");
    display_ok_msg ($l_insert_payment_ok);

    display_record_info($obm_q_invoice->f("invoice_usercreate"),$obm_q_invoice->f("invoice_userupdate"),$obm_q_invoice->f("timecreate"),$obm_q_invoice->f("timeupdate"));
    
    html_invoice_consult($action, $obm_q_invoice, run_query_invoicestatus(),$obm_q_deals, $options_deal, $obm_q_payments, $options_payment);
  } else {
    display_error_permissions();
  }
  }*/
///////////////////////h//////////////////////////////////////////////////////
elseif ($action == "delete")  { // delete means delete an invoice 
///////////////////////h//////////////////////////////////////////////////////
  // are there any payments (paid) connected to this invoice ?
  $payments_connected = run_query_search_payment_invoice ($invoice["invoice"], -1);
  // if yes, we delete all associations
  if ($payments_connected->nf() == 0) {
    run_query_delete($invoice["invoice"]); 
    display_ok_msg($l_delete_ok);
  } else {
    display_err_msg ($l_delete_error."<br>".$l_payments_exist);
  }
    require ("invoice_js.inc");
    html_invoice_search_form($action,run_query_invoicestatus(),$invoice);
///////////////////////h//////////////////////////////////////////////////////
} elseif ($action == "duplicate") {
///////////////////////h//////////////////////////////////////////////////////
  $q_invoice = run_query_detail ($invoice["invoice"]);
  $q_invoice->next_record ();
  require("invoice_js.inc"); 

  // we give the user the traditionnal form to modify this invoice :
  html_invoice_form ($q_invoice, $action, run_query_invoicestatus());
  
/////////////////////////////////////////////////////////////////////////
//
// display stuff below...
//
/////////////////////////////////////////////////////////////////////////
} elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $invoice_options=run_query_display_pref ($auth->auth["uid"], "invoice",1);
  $deal_options = run_query_display_pref ($auth->auth["uid"], "deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options);
  
} else if($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update ($entity, $fieldname, $display) ;
  $invoice_options=run_query_display_pref ($auth->auth["uid"], "invoice", 1);
  $deal_options = run_query_display_pref ($auth->auth["uid"],"deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options); 
  
} else if($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update ($entity, $new_level, $fieldorder) ;
  $invoice_options=run_query_display_pref($auth->auth["uid"], "invoice", 1);
  $deal_options=run_query_display_pref($auth->auth["uid"], "deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options); 

}elseif ($action == "admin")  {
//////////////////////h////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {  
    echo "<center><font color=\"#$col_error\">";
    echo "To come...";
    echo "</font></center><br>";
  }
  else {
    display_error_permission();
  }	
}
  

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
display_end();

///////////////////////////////////////////////////////////////////////////////
// Stores Invoice parameters transmitted in $invoice hash
// returns : $invoice hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_invoice() {
  global $tf_label, $tf_number, $tf_amount_HT, $tf_amount_TTC;
  global $ta_comment, $sel_status, $param_invoice, $tf_date;
  global $tf_date_after, $tf_date_before, $rd_inout, $hd_inout;
  global $tf_deal, $tf_company, $cb_archive;
  global $set_debug, $cdg_param, $action;

  if (isset ($tf_label)) $invoice["label"] = $tf_label;
  if (isset ($tf_number)) $invoice["number"] = $tf_number;
  if (isset ($tf_amount_HT)) $invoice["HT"] = $tf_amount_HT ;
  if (isset ($tf_amount_TTC)) $invoice["TTC"] = $tf_amount_TTC ;
  if (isset ($sel_status)) $invoice["status"] = $sel_status ;
  if (isset ($tf_date)) $invoice["date"] = $tf_date ;
  if (isset ($tf_date_after)) $invoice["date_after"] = $tf_date_after ;
  if (isset ($tf_date_before)) $invoice["date_before"] = $tf_date_before ;
  if (isset ($rd_inout)) $invoice["inout"] = $rd_inout ;
  if (isset ($hd_inout)) $invoice["inout"] = $hd_inout ;
  if (isset ($param_invoice)) $invoice["invoice"] = $param_invoice;
  if (isset ($tf_balance)) $invoice["balance"] = $tf_balance;
  if (isset ($tf_bank)) $invoice["bank"] = $tf_bank;
  if (isset ($ta_comment)) $invoice["comment"] = $ta_comment;
  if (isset ($tf_deal)) $invoice["deal"] = $tf_deal;
  if (isset ($tf_company)) $invoice["company"] = $tf_company;
  if (isset ($cb_archive)) $invoice["archive"] = $cb_archive;

  if (($set_debug > 0) && (($set_debug & $cdg_param) == $cdg_param)) {
    echo "<BR>action = $action";
    if ( $invoice ) {
      while ( list( $key, $val ) = each( $invoice ) ) {
        echo "<BR>invoice[$key]=$val";
      }
    }
  }

  return $invoice;
}

</SCRIPT>

