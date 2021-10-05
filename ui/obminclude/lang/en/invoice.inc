<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : invoice.inc                                              //
// 2001-08-02 - AliaSource - Pierre Baudracco                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:22:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:31:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:35:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:41:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:48:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:50:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T09:55:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:05:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:25:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:26:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:32:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:33:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:33:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:34:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:35:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:36:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:40:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:42:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:52:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:56:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:56:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:57:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:59:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T10:59:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T11:07:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T11:11:16+02:00 using OBMtranslation (0.4.1)

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
$l_amount_remaining = "Remaining amount"; 
$l_select_invoice = "Select invoice"; 
$l_invoicestatus_tocreate = 'To create';
$l_invoicestatus_sent = 'Sent';
$l_invoicestatus_received = 'Received';
$l_invoicestatus_ppaid = 'Partially paid';
$l_invoicestatus_paid = 'Paid';
$l_invoicestatus_conflict = 'Conflict';
$l_invoicestatus_cancelled = 'Cancelled';
$l_invoicestatus_loss = 'Lost';
$l_invoicestatus_factor = 'Factor';
$l_no_payment = "no payments related to this invoice";
$l_list_payment = "payments related to this invoice";
$l_invoice_infos = "Invoice information"; 
$l_affect = "Affect";
$l_year_to_year = "Years comparison";
$l_insert= "Insert Invoice";
$l_update = "Update Invoice";
$l_delete = "Delete Invoice";
$l_delete_confirm = "Confirm the deletion of this invoice ?";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the invoices"; 
$l_no_payment_display = "Fill the search fields and press < Find > to retrieve the payments";
$l_found = "invoices found";
$l_no_found="No invoice corresponding to your query";
$l_no_payment_found ="No payment corresponding to your query";
$l_link_payment = "These payments are connected to this invoice";
$l_link_payment_no = "The invoice has no payment"; 
$l_cant_delete = "The invoice cannot be deleted !"; 
$l_can_delete = "The invoice can be deleted.";
$l_fill_label = "The label must be filled in !"; 
$l_fill_number = "The number must be filled in (for this status) !"; 
$l_fill_company = "The company must be filled in !"; 
$l_fill_inout = "You must select the kind : $l_client or $l_supplier !";
$l_fill_date = "The date must be filled in (for this status) !"; 
$l_fill_amount = "Tha amounts must be correctly filled in !"; 
$l_error_number_exist = "This invoice number is already used !";
$l_err_paid_status = "The invoice has been paid, status must be '$l_invoicestatus_paid' !";
$l_err_ppaid_status = "The invoice has been partially paid, status must be '$l_invoicestatus_ppaid', '$l_invoicestatus_received' or '$l_invoicestatus_conflict' !";
$l_err_no_payment_status = "The invoice has no payments, status cannot be '$l_invoicestatus_paid' or '$l_invoicestatus_ppaid' !"; 
$l_err_status_received_customer = "The invoice status $l_invoicestatus_received is not valid for $l_client invoices !"; 
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
$l_invoice_display = "Display options for invoices list";
$default_invoice_numbering = "Y";
$l_check_money_ttc = "The tax included amount of the invoice is incorrect !"; 
$l_check_amounts = "The tax included amount has to be higher than the tax exclusion amount !"; 
$l_check_inout = "You have to choose a  type !"; 
$l_ht_nan = "Invalid tax exclusion amount !"; 
$l_check_payment_chosen = "Check chosen payments "; 
$l_j_select_invoice = "You have to select an invoice !"; 
$l_remaining_amount = "Remaining amount"; 
$l_add_payment_chosen = "Add chosen paiements"; 
$l_rate_nan = "Invamid VAT number !"; 
$l_no_number_confirm = "You are about to create an invoice without Number. Press OK to continue."; 
$l_payments_exist = "There are payments linked to this invoice !"; 
$l_to_use_amount = "Invoice amount"; 
$l_del_payment_chosen = "Delete chosen payments"; 
$l_invoice_mod = "Modify Invoice"; 
$l_invalid_payment = "The amount taken from a payment is bigger than its total"; 
$l_check_money_ht = "The tax exclusion amount of the invoice is incorrect !"; 
$l_changing_inout = "You cannot change an invoice kind if there are Deals linked to it"; 
$l_invoice_del_paiement = "Delete invoice payment "; 
$l_invoice_del_deal = "Delete invoice Deal"; 
$l_del_payment = "Delete payment"; 
$l_payments_todel = "Payments linked to this invoice "; 
$l_no_number = "An invoice must have a number ! "; 
$l_add_payment = "Add payment"; 
$l_check_number = "Invalid Invoice Number! "; 
$l_duplicate = "Duplicate Invoice "; 

