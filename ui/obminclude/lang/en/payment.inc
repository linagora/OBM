<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : payment.inc                                              //
// 2001-08-02 Nicolas Roman                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label_start = "Label start";
$l_amount = "Amount"; 
$l_expected_date = "Expected Date";
$l_type = "Kind";
$l_inout = "Kind";
$l_both = "Both"; 
$l_credit_memo = "Credit memo";
$l_pick_paymentkind = "choose a payment kind";
$l_kind = "Kind";
$l_pick_account = "choose an account";
$l_payment_account = "Account ";
$l_paid = "Paid";
$l_unpaid = "Not Paid";
$l_already_paid = "Already paid";
$l_kind_manage = "Payment kinds management";
$l_kind_label = "Label";
$l_kind_short_label = "Short label";

// invoice
$l_no_invoice = "No invoices related to this payment";
$l_invoice_list = "Invoices related to this payment";

$l_already_used_share = "Part of the Payment already used ";

$l_payment_infos = "Informations about the payment ";

// Actions 
$l_insert= "Insert Payment";
$l_update = "Update Payment";
$l_delete = "Delete Payment";
$l_delete_confirm = "Confirm the deletion of this payment ?";
$l_delete_invoice = "Delete";
$l_kind_delete = "Delete payment kind";

$l_payment_infos = "View Payment";
$l_payment_mod = "Modify Payment";
$l_search_invoice ="Search Invoices";
$l_select_invoice = "Use";
$l_affect_invoices = "Affect Invoices to that Payment";
$l_duplicate = "Duplicate that Payment";

// Messages 
$l_no_display = "Fill the search fields and press < Find > to retrieve the payments";
$l_found = "payments found.";
$l_no_found = "No payment corresponding to your query";
$l_no_invoices_connected = "No Invoices connected to that Payment";
$l_cant_delete = "The payment can't be deleted !";
$l_can_delete = "The payment can be deleted.";
$l_invoice_added = "The connection with the invoice has been created";
$l_no_invoice_added = "The connection with the invoice has failed !";
$l_fill_company = "A company must be selected !";
$l_err_kind = "Payment kind has not been added !";
$l_err_del_kind = "Payment kind has not been deleted !";
$l_fill_kind = "The payment kind must be selected !";
$l_fill_kind_short_label = "The payment kind short label have to be filled !";

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
