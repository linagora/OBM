<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : invoice.inc                                              //
// 2001-08-02 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label = "Label";
$l_label_start = "Label start";
$l_last_invoice = "Last invoice";
$l_number = "Number";
$l_amount = "Amount";
$l_amount_HT = "HT amount";
$l_amount_TTC = "TTC Amount";
$l_deal="Deal";
$l_status="Status";
$l_date="Date";
$l_date_apres = "After";
$l_date_avant = "Before";
$l_paid = "Paid TTC Amount";
$l_pick_deal = "Choose a deal";
$l_no_deal = "no deals related to this invoice";
$l_list_deal = "deals related to this invoice";
$l_no_payment = "no payments related to this invoice";
$l_list_payment = "payments related to this invoice";
$l_invoice_deal_label = "Deal Label";
$l_invoice_deal_number = "Deal Number";
$l_invoice_deal_category_label = "Deal Category";
$l_invoice_company = "Deal Company";
$l_invoice_dealtodo = "Deal Todo";
$l_invoice_payment_label = "Payment Label";
$l_invoice_payment_number = "Payment Number";
$l_invoice_payment_date = "Payment Date";
$l_invoice_payment_amount = "Payment Amount";
$l_inout = "Kind";
$l_both = "Both"; 
$l_client = "Customer";
$l_fournisseur = "Supplier";
$l_no_deals_related_error = "You cannot add payments to this invoice because it has no deals connected";
$l_last_deal_remove_error = "You cannot remove more deal : there remains only one related to this invoice, and there are still payments...<br>Remove payments first.";
// Titres
$l_invoice = "Invoice";
$l_invoice_infos = "Informations about the invoice";
$l_remaining_amount = "Amount not used";
$l_to_use_amount = "Amount to affect to this invoice";
$l_affect = "Affect";

// Actions 
$l_select_nb_deals = "How many deals will be linked to the new invoice ?";
$l_select_deals = "choose number of deals";
$l_insert= "Insert Invoice";
$l_update = "Update Invoice";
$l_delete = "Delete Invoice";
$l_delete_confirm = "Confirm the deletion of this invoice ?";
$l_no_number_confirm = "You are about to create an invoice without a number.\\nClick OK to confirm.";
$l_error_which = "Error : The which variable is NOT CORRECTLY set... Please Check your administrator" ;
$l_invoice_del_deal = "Remove";
$l_invoice_del_payment = "Remove";
$l_invoice_infos = "View Invoice";
$l_add_deal = "Add deal";
$l_add_deal_chosen = "Add chosen deals";
$l_del_deal = "Remove deal";
$l_del_deal_chosen = "Delete chosen deals";
$l_deals_todel = "Here withdrawable deals of this invoice" ;
$l_add_payment = "Add payment";
$l_add_payment_chosen = "Add chosen payments";
$l_check_payment_chosen = "Check chosen payments";
$l_del_payment = "Remove payment";
$l_del_payment_chosen = "Delete chosen payments";
$l_payments_todel = "Here withdrawable payments of this invoice" ;
$l_invoice_mod = "Modify Invoice";
$l_invalid_payment = "The amount taken from a payment is bigger than its total";
$l_too_much_money = "The total amount of payments is bigger than the invoice total. If you carry on, this invoice will reach status 'trouble'";
$l_error_try_again = "There are errors !! Please restart from the beginning";
$l_try_again = "Try Again !";

// Messages 
$l_no_display = "Fill the search fields and press < Find > to retrieve the invoices";
$l_no_deal_display = "Fill the search fields and press < Find > to retrieve the deals";
$l_no_payment_display = "Fill the search fields and press < Find > to retrieve the payments";
$l_found="invoices found.";
$l_no_found="No invoice corresponding to your query";
$l_no_deal_found ="No deal corresponding to your query";
$l_no_payment_found ="No payment corresponding to your query";
$l_delete_ok = "The invoice has been deleted successfully.";
$l_delete_error = "The deletion of the invoice has failed !";
$l_remove_deal_ok = "The removal of the deal(s) has been done successfully!";
$l_remove_deal_error = "The removal of the deal(s) has failed!";
$l_insert_ok = "The invoice has been inserted successfully.";
$l_insert_error = "The insertion of the invoice has failed !";
$l_insert_deal_ok = "The insertion of the deal(s) has been done successfully!";
$l_insert_deal_error = "The insertion of the deal(s) has failed!";
$l_update_ok = "The invoice has been updated successfully.";
$l_update_error = "The update of the invoice has failed !";
$l_query_error = "Trouble executing the query...";

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
$l_invoice_options = "Display options for invoices list"; 

// default invoice numbering policy :
$default_invoice_numbering = "Y"; // we use only year