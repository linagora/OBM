<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_index.inc
//     - Desc : Invoice Main file
// 2001-07-30 Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "COMPTA";
$menu="INVOICE";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("invoice_display.inc");
require("invoice_query.inc");

if ($action == "") $action = "index";
$invoice = get_param_invoice();
get_invoice_action();
$perm->check_permissions($menu, $action);

update_last_visit("invoice", $invoice["id"], $action);

page_close();

///////////////////////////////////////////////////////////////////////////////
// Main program
///////////////////////////////////////////////////////////////////////////////

if ($action == "index" || $action == "") {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 
  if ($set_display == "yes") { 
    $display["result"] = dis_invoice_search_list($invoice);
  } else { 
    $display["msg"] .= display_ok_msg($l_no_display); 
  } 

} elseif ($action == "search")  { 
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 
  $display["result"] = dis_invoice_search_list($invoice);
  
} elseif ($action == "new") {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc"); 
  $display["detail"] = dis_invoice_form($action, $invoice);

} elseif ($action == "insert")  {
///////////////////////////////////////////////////////////////////////////////
  run_query_insert($invoice);
  $display["msg"] .= display_ok_msg($l_insert_ok);
  require("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 
  
} elseif ($action == "detailconsult")  {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["detail"] = dis_invoice_consult($invoice);

} elseif ($action == "detailupdate")  { 
///////////////////////////////////////////////////////////////////////////////
    require("invoice_js.inc");
    $display["detail"] = dis_invoice_form($action, $invoice);

} elseif ($action == "update")  {
///////////////////////////////////////////////////////////////////////////////
  run_query_update($invoice); 
  $display["msg"] .= display_ok_msg($l_update_ok); 
  require("invoice_js.inc"); 
  $display["search"] = dis_invoice_search_form($invoice); 

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
  $display["msg"] .= display_ok_msg ($l_archive_ok);
  echo $nb_invoices . " " . $l_archive_number . "<br />\n";
  
  require ("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 

} /*elseif ($action =="add_payment") {
///////////////////////////////////////////////////////////////////////////////
  if (true) {
    $q_invoice = run_query_detail ($invoice["invoice"]);
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
    display_err_msg($l_error_permission);
  }

} elseif ($action =="search_payment") {
///////////////////////////////////////////////////////////////////////////////
  if (true) {
    display_ok_msg ("PERMISSIONS");
    $q_invoice = run_query_detail ($invoice["invoice"]);
    $dis_options_payment = run_query_display_option ($auth, "payment");
    $q_payment = run_query_payment ($q_invoice, $tf_payment_label);
    dis_search_payment_form ($q_invoice, $dis_options_payment, $q_payment);
  } else {
    display_err_msg($l_error_permission);
  }

} elseif ($action == "check_payment_chosen") {
///////////////////////////////////////////////////////////////////////////////
  
  html_check_payments ($invoice["invoice"], $hd_payments_used, $tf_to_use_amount, $hd_solde, $hd_invoice_reste_a_payer); 
  
}elseif ($action == "affect_payment") {
///////////////////////////////////////////////////////////////////////////////
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
    $q_payments_invoice = run_query_invoice_payment ($invoice["invoice"]);

    html_form_add_payments ($q_invoice, $q_payments_invoice, $payments_to_proceed, $nb_payments_added);

    if ($nb_payments_added!=0){
      run_query_update_updater ($auth, $invoice["invoice"]);
    }
  } else{
    display_err_msg($l_error_permission);
  }

} elseif ($action == "del_payment") {
////////////////////////h//////////////////////////////////////////////////////
  if (true) {
    $display["detail"] = dis_invoice_consult($invoice);
  }

} elseif ($action =="del_payment_chosen") {
////////////////////////h//////////////////////////////////////////////////////
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
    $display["detail"] = dis_invoice_consult($invoice);
  } else {
    display_err_msg($l_error_permission);
  }
  }*/

elseif ($action == "delete")  { // delete means delete an invoice 
///////////////////////h//////////////////////////////////////////////////////
  // are there any payments (paid) connected to this invoice ?
  $payments_connected = run_query_invoice_payment($invoice["invoice"], -1);
  // if yes, we delete all associations
  if ($payments_connected->nf() == 0) {
    run_query_delete($invoice["invoice"]); 
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg ($l_delete_error."<br>".$l_payments_exist);
  }
  require ("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice); 

} elseif ($action == "duplicate") {
///////////////////////h//////////////////////////////////////////////////////
  // we give the user the traditionnal form to modify this invoice :
  require("invoice_js.inc"); 
  $display["detail"] = dis_invoice_form($action, $invoice);
  
} elseif ($action == "display") {
/////////////////////////////////////////////////////////////////////////
  $invoice_options=run_query_display_pref ($auth->auth["uid"], "invoice",1);
  $deal_options = run_query_display_pref ($auth->auth["uid"], "deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options);
  
} else if($action == "dispref_display") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update ($entity, $fieldname, $disstatus) ;
  $invoice_options=run_query_display_pref ($auth->auth["uid"], "invoice", 1);
  $deal_options = run_query_display_pref ($auth->auth["uid"],"deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options); 
  
} else if($action == "dispref_level") {
/////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update ($entity, $new_level, $fieldorder) ;
  $invoice_options=run_query_display_pref($auth->auth["uid"], "invoice", 1);
  $deal_options=run_query_display_pref($auth->auth["uid"], "deal", 1);
  dis_invoice_display_pref ($invoice_options, $deal_options); 

} elseif ($action == "admin")  {
//////////////////////h////////////////////////////////////////////////////////
  if ($auth->auth["perm"] != $perms_user) {  
    $display["detail"] .= "Nothing here for now";
  } else {
    $display["msg"] .= display_err_msg($l_error_permission);
  }	
}
  

///////////////////////////////////////////////////////////////////////////////
// Display end of page                                                       //
///////////////////////////////////////////////////////////////////////////////
$display["head"] = display_head("$l_invoice");
$display["header"] = generate_menu($menu, $section);
$display["end"] = display_end();
display_page($display);


///////////////////////////////////////////////////////////////////////////////
// Stores Invoice parameters transmitted in $invoice hash
// returns : $invoice hash with parameters set
///////////////////////////////////////////////////////////////////////////////
function get_param_invoice() {
  global $tf_label, $tf_number, $tf_amount_ht, $tf_amount_ttc;
  global $ta_comment, $sel_status, $param_invoice, $tf_date;
  global $tf_date_after, $tf_date_before, $rd_inout, $hd_inout;
  global $tf_deal, $tf_company, $cb_archive;
  global $param_company, $param_deal, $param_project;
  global $set_debug, $cdg_param, $action;

  if (isset ($param_invoice)) $invoice["id"] = $param_invoice;
  if (isset ($param_company)) $invoice["company_id"]= $param_company;
  if (isset ($param_deal)) $invoice["deal_id"]= $param_deal; 
  if (isset ($param_project)) $invoice["project_id"]= $param_project; 
  if (isset ($tf_label)) $invoice["label"] = $tf_label;
  if (isset ($tf_number)) $invoice["number"] = $tf_number;
  if (isset ($tf_amount_ht)) $invoice["ht"] = $tf_amount_ht;
  if (isset ($tf_amount_ttc)) $invoice["ttc"] = $tf_amount_ttc;
  if (isset ($sel_status)) $invoice["status"] = $sel_status;
  if (isset ($tf_date)) $invoice["date"] = $tf_date;
  if (isset ($tf_date_after)) $invoice["date_after"] = $tf_date_after;
  if (isset ($tf_date_before)) $invoice["date_before"] = $tf_date_before;
  if (isset ($rd_inout)) $invoice["inout"] = $rd_inout;
  if (isset ($hd_inout)) $invoice["inout"] = $hd_inout;
  if (isset ($tf_balance)) $invoice["balance"] = $tf_balance;
  if (isset ($tf_bank)) $invoice["bank"] = $tf_bank;
  if (isset ($ta_comment)) $invoice["comment"] = $ta_comment;
  if (isset ($tf_deal)) $invoice["deal"] = $tf_deal;
  if (isset ($tf_company)) $invoice["company"] = $tf_company;
  if (isset ($cb_archive)) $invoice["archive"] = $cb_archive;

  if (($set_debug > 0) && (($set_debug & $cdg_param) == $cdg_param)) {
    echo "<br />action = $action";
    if ( $invoice ) {
      while ( list( $key, $val ) = each( $invoice ) ) {
        echo "<br />invoice[$key]=$val";
      }
    }
  }

  return $invoice;
}


///////////////////////////////////////////////////////////////////////////////
// Invoice actions
///////////////////////////////////////////////////////////////////////////////
function get_invoice_action() {
  global $invoice, $actions, $path;
  global $l_header_find,$l_header_new_f,$l_header_update,$l_header_delete;
  global $l_header_consult, $l_header_display,$l_header_dupplicate,$l_header_admin;
  global $l_header_add_deal, $invoice_admin_write;
  global $cright_read, $cright_write, $cright_read_admin, $cright_write_admin;

// Index 
  $actions["INVOICE"]["index"] = array (
    'Name'     => $l_header_find,
    'Url'      => "$path/invoice/invoice_index.php?action=index",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                       );

// Search
  $actions["INVOICE"]["search"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=search",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                   );

// New
  $actions["INVOICE"]["new"] = array (
    'Name'     => $l_header_new_f,
    'Url'      => "$path/invoice/invoice_index.php?action=new",
    'Right'    => $cright_write,
    'Condition'=> array ('','search','index','detailconsult','display') 
                                   );

//Insert
  $actions["INVOICE"]["insert"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=insert",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                   );

// Detail Consult
  $actions["INVOICE"]["detailconsult"] = array (
    'Name'     => $l_header_consult,
    'Url'      => "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$invoice["invoice"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
                                   );

// Duplicate
  $actions["INVOICE"]["duplicate"] = array (
    'Name'     => $l_header_dupplicate,
    'Url'      => "$path/invoice/invoice_index.php?action=duplicate&amp;param_invoice=".$invoice["invoice"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	   );

// Detail Update
  $actions["INVOICE"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/invoice/invoice_index.php?action=detailupdate&amp;param_invoice=".$invoice["invoice"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	       );

// Update
  $actions["INVOICE"]["update"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=update",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                        );

// Update Archive
  $actions["INVOICE"]["updatearchive"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=updatearchive",
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
                                        );

// Delete
  $actions["INVOICE"]["delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/invoice/invoice_index.php?action=delete&amp;param_invoice=".$invoice["invoice"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate') 
                                     	);
// Administration
  $actions["INVOICE"]["admin"] = array (
    'Name'     => $l_header_admin,
    'Url'      => "$path/invoice/invoice_index.php?action=admin",
    'Right'    => $cright_read_admin,
    'Condition'=> array ('all') 
                                       );

// Display
  $actions["INVOICE"]["display"] = array (
    'Name'     => $l_header_display,
    'Url'      => "$path/invoice/invoice_index.php?action=display",
    'Right'    => $cright_read,
    'Condition'=> array ('all') 
                                        );

// Display Preferences
  $actions["INVOICE"]["dispref_display"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_display",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

// Display Préférences
  $actions["INVOICE"]["dispref_level"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=dispref_level",
    'Right'    => $cright_read,
    'Condition'=> array ('None') 
                                        );

}

</script>
