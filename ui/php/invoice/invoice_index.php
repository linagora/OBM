<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : invoice_index.inc
//     - Desc : Invoice Main file
// 2001-07-30 Aliacom - Nicolas Roman
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Actions :
// - index (default)    -- search fields  -- show the invoice search form
// - search             -- search fields  -- show the result set of search
// - new                -- $params        -- show the new invoice form
// - detailconsult      -- $param_invoice -- show the invoice detail
// - detailupdate       -- $param_invoice -- show the invoice detail form
// - insert             -- form fields    -- insert the invoice
// - update             -- form fields    -- update the invoice
// - check_delete       -- $param_invoice -- check links before delete
// - delete             -- $param_invoice -- delete the invoice
// - display            --                -- display and set display parameters
// - dispref_display    --                -- update one field display value
// - dispref_level      --                -- update one field display position 
// - duplicate
// - updatearchive
///////////////////////////////////////////////////////////////////////////////

$path = "..";
$section = "COMPTA";
$menu = "INVOICE";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
include("$obminclude/global.inc");
page_open(array("sess" => "OBM_Session", "auth" => "OBM_Challenge_Auth", "perm" => "OBM_Perm"));
include("$obminclude/global_pref.inc");
require("invoice_display.inc");
require("invoice_query.inc");

$uid = $auth->auth["uid"];

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
///////////////////////////////////////////////////////////////////////////////
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

} elseif ($action == "check_delete")  {
///////////////////////////////////////////////////////////////////////////////
  require("invoice_js.inc");
  $display["detail"] = dis_check_invoice_links($invoice["id"]);

} elseif ($action == "delete")  { // delete means delete an invoice 
///////////////////////////////////////////////////////////////////////////////
  // are there any payments (paid) connected to this invoice ?
  $payments_connected = run_query_invoice_payment($invoice["id"], -1);
  // if yes, we delete all associations
  if ($payments_connected->nf() == 0) {
    run_query_delete($invoice["id"]); 
    $display["msg"] .= display_ok_msg($l_delete_ok);
  } else {
    $display["msg"] .= display_err_msg ($l_delete_error."<br>".$l_payments_exist);
  }
  require ("invoice_js.inc");
  $display["search"] = dis_invoice_search_form($invoice);

} elseif ($action == "duplicate") {
///////////////////////////////////////////////////////////////////////////////
  // we give the user the traditionnal form to modify this invoice :
  require("invoice_js.inc"); 
  $display["detail"] = dis_invoice_form($action, $invoice);
  
} elseif ($action == "display") {
///////////////////////////////////////////////////////////////////////////////
  $pref_q = run_query_display_pref($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref ($pref_q);
  
} else if($action == "dispref_display") {
  /////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_update ($entity, $fieldname, $disstatus) ;
  $pref_q = run_query_display_pref ($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref ($pref_q);
  
} else if($action == "dispref_level") {
///////////////////////////////////////////////////////////////////////////////
  run_query_display_pref_level_update ($entity, $new_level, $fieldorder) ;
  $pref_q = run_query_display_pref($uid, "invoice", 1);
  $display["detail"] = dis_invoice_display_pref ($pref_q);
}
  

///////////////////////////////////////////////////////////////////////////////
// Display
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
  global $param_company, $company_name, $company_new_name, $company_new_id;
  global $param_deal, $deal_label, $deal_new_label, $deal_new_id;
  global $param_project, $project_name, $project_new_name, $project_new_id;
  global $set_debug, $cdg_param, $action;

  if (isset ($param_invoice)) $invoice["id"] = $param_invoice;
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
  if (isset ($cb_archive)) $invoice["archive"] = $cb_archive;

  // Company params
  if (isset ($param_company)) $invoice["company_id"] = $param_company;
  if (isset ($tf_company)) $invoice["company"] = $tf_company;
  if (isset ($company_name)) $invoice["company_name"] = $company_name;
  if (isset ($company_new_name)) $invoice["comp_new_name"] = $company_new_name;
  if (isset ($company_new_id)) $invoice["comp_new_id"] = $company_new_id;

  // Deal params
  if (isset ($param_deal)) $invoice["deal_id"]= $param_deal; 
  if (isset ($deal_label)) $invoice["deal_label"] = $deal_label;
  if (isset ($deal_new_label)) $invoice["deal_new_label"] = $deal_new_label;
  if (isset ($deal_new_id)) $invoice["deal_new_id"] = $deal_new_id;

  // Project params
  if (isset ($param_project)) $invoice["project_id"]= $param_project; 
  if (isset ($project_name)) $invoice["project_name"] = $project_name;
  if (isset ($project_new_name)) $invoice["proj_new_name"] = $project_new_name;
  if (isset ($project_new_id)) $invoice["proj_new_id"] = $project_new_id;

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
    'Url'      => "$path/invoice/invoice_index.php?action=detailconsult&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_read,
    'Condition'=> array ('detailupdate') 
                                   );

// Duplicate
  $actions["INVOICE"]["duplicate"] = array (
    'Name'     => $l_header_dupplicate,
    'Url'      => "$path/invoice/invoice_index.php?action=duplicate&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult') 
                                     	   );

// Detail Update
  $actions["INVOICE"]["detailupdate"] = array (
    'Name'     => $l_header_update,
    'Url'      => "$path/invoice/invoice_index.php?action=detailupdate&amp;param_invoice=".$invoice["id"]."",
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

// Check Delete
  $actions["INVOICE"]["check_delete"] = array (
    'Name'     => $l_header_delete,
    'Url'      => "$path/invoice/invoice_index.php?action=check_delete&amp;param_invoice=".$invoice["id"]."",
    'Right'    => $cright_write,
    'Condition'=> array ('detailconsult', 'detailupdate', 'update') 
                                     	      );

// Delete
  $actions["INVOICE"]["delete"] = array (
    'Url'      => "$path/invoice/invoice_index.php?action=delete&amp;param_invoice=".$invoice["id"],
    'Right'    => $cright_write,
    'Condition'=> array ('None') 
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
