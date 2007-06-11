<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : invoice.inc                                              //
// 2001-08-02 - AliaSource - Pierre Baudracco                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label_start = "Label start";
$l_amount = "Amount";
$l_amount_ht = "HT amount";
$l_amount_ttc = "TTC Amount";
$l_expiration_date = "Expiration date";
$l_payment_date = "Payment date";
$l_expiration = "Expiration";
$l_credit_memo = "Credit memo";
$l_paid = "Paid";
$l_waiting = "Waiting";
$l_pick_deal = "Choose a deal";
$l_inout = "Type";
$l_both = "All";
$l_tax_rate = "Tax rate";
$l_compute_tax = "Calculate the taxed amount";
$l_include_archive = "Include archives";
$l_amount_remaining = "Amount remaining";

$l_select_invoice = "Select the invoice";

// payment
$l_no_payment = "no payments related to this invoice";
$l_list_payment = "payments related to this invoice";


$l_invoice_infos = "Informations about the invoice";
$l_affect = "Affect";
$l_year_to_year = "Years comparison";

// Actions 
$l_insert= "Insert Invoice";
$l_update = "Update Invoice";
$l_delete = "Delete Invoice";
$l_delete_confirm = "Confirm the deletion of this invoice ?";


// Messages 
$l_no_display = "Fill the search fields and press < Find > to retrieve the invoices";
$l_no_payment_display = "Fill the search fields and press < Find > to retrieve the payments";
$l_found = "invoices found";
$l_no_found="No invoice corresponding to your query";
$l_no_payment_found ="No payment corresponding to your query";

$l_link_payment = "These payments are connected to this invoice";
$l_link_payment_no = "The invoice hasn't any payment";
$l_cant_delete = "The invoice can't be deleted !";
$l_can_delete = "The invoice can be deleted.";

$l_fill_label = "The label must be filled !";
$l_fill_number = "The number must be filled (for this status) !";
$l_fill_company = "The company must be filled !";
$l_fill_inout = "You must select the kind : $l_client or $l_supplier !";
$l_fill_date = "The date must be filled (for this status) !";
$l_fill_amount = "Tha amounts must be correctly filled !";
$l_error_number_exist = "This invoice number is already used !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_check_number = "\\nThe invoice number is not valid!";
$lj_check_money_ht = "\\nThe amount without charges is not valid!";
$lj_check_money_ttc = "\\nThe amount charges included is not valid!";
$lj_check_amounts = "\\nThe amount charges included should be greater than the amount without charges!";
$lj_check_inout = "\\nYou have to choose a kind before submitting";
$lj_changing_inout = "You cannot change an invoice kind if there are \\n deals linked to it...";
$lj_no_number="\\nInvoice must have a number!";
$lj_fill_date = "\\n You have to fill the Date before submitting.";
$lj_fill_label = "\\n You have to fill the Label before submitting.";
$l_invalid_date="\\nInvalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ)";
$lj_check_deal = "You have to select a Deal before submitting";
$lj_select_deals = "You have to select a number of deals";

// For the display parameters modification screen
$l_invoice_display = "Display options for invoices list"; 

// default invoice numbering policy :
$default_invoice_numbering = "Y"; // we use only year
