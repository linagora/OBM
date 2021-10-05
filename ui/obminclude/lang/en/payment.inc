<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : payment.inc                                              //
// 2001-08-02 Nicolas Roman                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-09T15:20:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:49:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:51:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:55:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T10:57:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:00:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:07:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:09:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:09:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:16:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:17:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:17:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:18:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:19:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:22:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:22:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:23:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:24:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:27:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:28:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:30:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:41:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:50:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:52:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:54:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T11:54:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:10:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:11:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:12:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:12:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:17:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-10T12:20:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:42:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:43:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:45:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:47:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:53:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T14:54:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:00:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-11T15:01:06+02:00 using OBMtranslation (0.4.1)

$l_label_start = "Label start";
$l_amount = "Amount";
$l_expected_date = "Expected Date";
$l_type = "Payment Type";
$l_inout = "Type";
$l_both = "Both";
$l_credit_memo = "Credit memo";
$l_pick_paymentkind = "Choose a Payment type";
$l_kind = "Kind";
$l_pick_account = "Choose an Account";
$l_payment_account = "Account ";
$l_paid = "Paid";
$l_unpaid = "Not Paid";
$l_already_paid = "Already paid";
$l_kind_manage = "Payment types management";
$l_kind_label = "Label";
$l_kind_short_label = "Short label";
$l_no_invoice = "No invoices related to this payment";
$l_invoice_list = "Invoices related to this payment";
$l_already_used_share = "Part of the Payment already used ";
$l_payment_infos = "Informations about the payment ";
$l_insert= "Insert Payment";
$l_update = "Update Payment";
$l_delete = "Delete Payment";
$l_delete_confirm = "Confirm the deletion of this payment ?";
$l_delete_invoice = "Delete";
$l_kind_delete = "Delete payment type";
$l_payment_infos = "View Payment";
$l_payment_mod = "Modify Payment";
$l_search_invoice ="Search Invoices";
$l_select_invoice = "Use";
$l_affect_invoices = "Affect Invoices to that Payment";
$l_duplicate = "Duplicate that Payment";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the payments";
$l_found = "Payments found.";
$l_no_found = "No Payment match your query";
$l_no_invoices_connected = "No Invoices connected to that Payment";
$l_cant_delete = "The payment cannot be deleted !";
$l_can_delete = "The payment can be deleted.";
$l_invoice_added = "The connection Payment/Invoice has been created";
$l_no_invoice_added = "The connection Payment/Invoice has failed !";
$l_fill_company = "A company must be selected !";
$l_err_kind = "Payment type has not been added !";
$l_err_del_kind = "Payment type has not been deleted !";
$l_fill_kind = "The payment type must be selected !";
$l_fill_kind_short_label = "The payment type short label has to be filled in !";
$lj_no_number = "\\nThe payment number is not valid!";
$lj_check_amount = "\\nThe amount is not valid!";
$lj_check_inout = "\\nYou have to decide wether it's a payment from a customer or to a supplier before submitting";
$lj_check_paymentkind = "You have to choose a payment kind before submitting!";
$lj_check_account = "You have to choose an account before submitting!";
$l_no_amount = "You have to enter an amount for the payment";
$l_invalid_amount = "The last amount you have entered is not valid !"; 
$l_payment_options = "Display options for Payment lists";
$l_not_enough = "You have not used all the payment amount !<BR>";
$l_too_much = "You have used more money than you actually have !<BR>";
$l_trouble_invoice = "At least one invoice has received too much money.<BR>Its status will become \"trouble\".<BR>Are you sure you want to do that ?<BR>";
$l_bank_anyway = "Bank that Payment";
$l_bank_again = "Restart that banking from the beginning";
$l_check_account = "You must choose an Account !";
$l_check_inout = "You must choose between Client Payment and Supplier Payment !";
$l_j_fill_kind_label = "The payment type name must be filled in !";
$l_fill_kind_label = "The payment type name must be filled in !";
$l_fill_amount = "The payment must be entered correctly !";
$l_choose_csv = "Choose file";
$l_j_fill_kind_short_label = "The payment type short label must be filled in !";
$l_err_affected_invoice = "The amount written on the invoice is higher than the remained amount to be paid";
$l_err_total_payment_affected = "The total amount to be paid is higher than the payment !";
$l_fill_inout = "The type must be selected : (+) $l_received ou (-) $l_emitted !";
$l_no_number = "Invalid Payment number !";
$l_invoice_options = "Display options for Invoice lists";
$l_check_amount = "Invalid amount !";
$l_include_checked = "Insert Payments"; 
$l_check_paymentkind = "You have to choose a Payment type !";

