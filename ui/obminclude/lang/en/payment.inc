<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : payment.inc                                              //
// 2001-08-02 Nicolas Roman                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes

$l_label = "Label";
$l_label_start = "Label start";
$l_number = "Number";
$l_amount = "Amount"; 
$l_comment = "Comment";
$l_date = "Date";
$l_expected_date = "Expected Date";
$l_date_apres = "Date After";
$l_date_avant = "Date Before";
$l_account = "Account";
$l_type = "Kind";
$l_inout = "Kind";
$l_both = "Both"; 
$l_client = "Customer";
$l_fournisseur = "Supplier";
$l_pick_paymentkind = "choose a payment kind";
$l_kind = "Kind";
$l_pick_account = "choose an account";
$l_payment_account = "Account ";
$l_account = "Account" ; 
$l_status = "Status";
$l_paid = "Paid";
$l_unpaid = "Not Paid";
// invoice connected :
$l_invoice_label = "Invoice Label";
$l_invoice_date = "Invoice Date";
$l_invoice_TTC = "Invoice Amount";
$l_invoice_number = "Invoice Number"; 
$l_invoice_not_paid_yet = "Amount not paid BEFORE";
$l_invoice_still_not_paid = "Amount not paid NOW";
$l_invoice_share = "Share affected";
$l_invoice_inout = "Invoice Kind";
$l_invoice_paid = "Paid TTC Amount";
$l_already_used_share = "Part of the Payment already used ";
// Titres
$l_payment = "Payment";
$l_payment_infos = "Informations about the payment ";

// Actions 
$l_insert= "Insert Payment";
$l_update = "Update Payment";
$l_delete = "Delete Payment";
$l_delete_confirm = "Confirm the deletion of this payment ?";
$l_error_which = "Error : The which variable is NOT CORRECTLY set... Please Check your administrator" ;
$l_payment_infos = "View Payment";
$l_payment_mod = "Modify Payment";
$l_search_invoice ="Search Invoices";
$l_select_invoice = "Use";
$l_affect_invoices = "Affect Invoices to that Payment";
$l_duplicate = "Duplicate that Payment";

// Messages 
$l_no_display = "Fill the search fields and press < Find > to retrieve the payments";
$l_found="payments found.";
$l_no_found="No payment corresponding to your query";
$l_no_invoices_connected = "No Invoices connected to that Payment";
$l_delete_ok = "The payment has been deleted successfully.";
$l_delete_error = "The deletion of the payment has failed !";
$l_insert_ok = "The payment has been inserted successfully.";
$l_insert_error = "The insertion of the payment has failed !";
$l_update_ok = "The payment has been updated successfully.";
$l_update_error = "The update of the payment has failed !";
$l_query_error = "Trouble executing the query...";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_no_number = "\\nThe payment number is not valid!";
$lj_check_amount = "\\nThe amount is not valid!";
$lj_check_inout = "\\nYou have to decide wether it's a payment from a customer or to a supplier before submitting";
$lj_check_paymentkind = "You have to choose a payment kind before submitting!";
$lj_check_account = "You have to choose an account before submitting!";
$l_no_amount = "You have to enter an amount for the payment";
$l_invalid_amount = "The last amount you entered is not valid";
// For the display parameters modification screen
$l_payment_options = "Display options for payments list"; 

// banking check messages : 
$l_not_enough = "You have not used all the payment amount !<BR>";
$l_too_much = "You have used more money than you actually have !<BR>";
$l_trouble_invoice = "At least one invoice has received too much money.<BR>Its status will become \"trouble\".<BR>Are you sure you want to do that ?<BR>";
$l_bank_anyway = "Bank that Payment";
$l_bank_again = "Restart that banking from the beginning";
