<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Romanian                                                   //
//     - File     : invoice.inc                                              //
// 2001-08-02 - AliaSource - Pierre Baudracco                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: invoice.inc 3612 2009-01-08 11:18:59Z pierre $ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-21T16:58:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:00:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:00:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:08:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:15:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:31:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:34:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:40:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:41:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:41:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:42:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:50:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:51:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:53:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:53:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T17:58:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-21T18:00:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:38:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:40:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:40:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:42:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:42:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:43:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:43:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:44:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:48:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:50:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:50:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:57:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:57:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:09:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:16:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:22:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:23:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:25:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:26:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:30:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:32:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:31:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:32:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:35:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:48+02:00 using OBMtranslation (0.4.1)

$l_label_start = "Începutul Etichetei";
$l_amount = "Sumă";
$l_amount_ht = "Sumă netă";
$l_amount_ttc = "Sumă brută";
$l_expiration_date = "Dată de scadență";
$l_payment_date = "Dată de plată";
$l_expiration = "Scadență";
$l_credit_memo = "Credit";
$l_paid = "Plătit";
$l_waiting = "În așteptare";
$l_pick_deal = "Alegeți o afacere";
$l_inout = "Tip";
$l_both = "Ambele";
$l_tax_rate = "Taxa TVA";
$l_compute_tax = "Calculați suma brută";
$l_include_archive = "Includeți arhivele";
$l_amount_remaining = "Restant din";
$l_select_invoice = "Selectați factura";
$l_invoicestatus_tocreate = "De Creat";
$l_invoicestatus_sent = "Trimisă";
$l_invoicestatus_received = "Primită";
$l_invoicestatus_ppaid = "Plătită Parțial";
$l_invoicestatus_paid = "Plătită";
$l_invoicestatus_conflict = "În conflict";
$l_invoicestatus_cancelled = "Anulată";
$l_invoicestatus_loss = "Pierdută";
$l_invoicestatus_factor = "Zilnic";
$l_no_payment = "Nicio plată nu este asociată acestei facturi"; 
$l_list_payment = "Plăți asociate acestei facturi";
$l_invoice_infos = "Consultați Factura"; 
$l_remaining_amount = "Sumă nefolosită";
$l_to_use_amount = "Sumă de asociat acestei Facturi.";
$l_affect = "Asociere";
$l_year_to_year = "Comparație anuală";
$l_insert = "Introduceți Factura";
$l_update = "Actualizare Factură";
$l_duplicate = "Reproduceți această Factură"; 
$l_delete = "Ștergeți Factura";
$l_delete_confirm = "Sunteți sigur(ă) că doriți să ștergeți această factură?"; 
$l_no_number_confirm = "Veți crea o factură fără Număr. Apăsați butonul OK pentru a continua.";
$l_invoice_del_deal = "Ștergeți asocierea";
$l_invoice_del_paiement = "Ștergeți asocierea";
$l_invoice_infos = "Voir la Facture";
$l_add_payment = "Adăugați plata"; 
$l_add_payment_chosen = "Adăugați plățile selectate";
$l_check_payment_chosen = "Verificați plătile selectate";
$l_del_payment = "Ștergeți plata"; 
$l_del_payment_chosen = "Ștergeți plățile selectate";
$l_payments_todel = "Plăți asociate acestei facturi";
$l_invoice_mod = "Modificați Factura";
$l_no_display = "Completați spațiile și apăsați butonul  <Caută > pentru a lansa căutarea";
$l_found = "Factură(i) găsită(e)";
$l_no_found = "Nicio Factură nu corespunde căutării"; 
$l_no_payment_found = "Nicio Plată nu corespunde cererii dumneavoastră";
$l_payments_exist = "Există plăți asociate acestei facturi!";
$l_invalid_payment = "Suma parțial plătită este mai mare decât totalul"; 
$l_link_payment = "Aceste plăți sunt asociate acestei facturi";
$l_link_payment_no = "Factura nu este asociată niciunei plăți";
$l_cant_delete = "Factura nu poate fi ștearsă!";
$l_can_delete = "Factura poate fi ștearsă";
$l_fill_label = "Introduceți eticheta!";
$l_fill_number = "Introduceți numărul (pentru statutul ales)!"; 
$l_fill_company = "Introduceți firma!";
$l_fill_date = "Introduceți data (pentru statutul ales)!"; 
$l_fill_inout = "Selecționați tipul:  $l_client sau $l_supplier !"; 
$l_fill_amount = "Sumele trebuie introduse corect!";
$l_error_number_exist = "Acest număr de factură este deja atribuit!";
$l_err_paid_status = "Factura a fost plătită, statutul ar trebui să fie  '$l_invoicestatus_paid' !"; 
$l_err_ppaid_status = "Factura a fost plătită parțial, statutul ar trebui să fie '$l_invoicestatus_ppaid' sau '$l_invoicestatus_conflict' !"; 
$l_err_no_payment_status = "Factura nu are nicio plată, statutul nu poate fi '$l_invoicestatus_paid' sau  '$l_invoicestatus_ppaid' !"; 
$l_err_status_received_customer = "Statutul $l_invoicestatus_received nu este valabil pentru facturile de tipul $l_client !"; 
$l_check_number = "\\nNumărul Facturii nu este valabil!";
$l_check_money_ht = "Suma netă a Facturii nu este corectă!"; 
$l_check_money_ttc = "\\n Suma brută a facturii este incorectă!";
$l_check_amounts = "\\n Suma brută trebuie să fie superioară celei nete!";
$l_check_inout = "\\nAlegeți un tip!";
$l_changing_inout = "Nu puteți schimba tipul de factură dacă aceasta este asociată anumitor Afaceri";
$l_no_number = "\\nO factură trebuie să aibă un număr!";
$l_rate_nan = "Taxa TVA nu este valabilă!";
$l_ht_nan = "Suma netă nu este valabilă!"; 
$l_invoice_display = "Opțiuni de vizualizare a listelor de Facturi";
$default_invoice_numbering = "Y";
$l_j_select_invoice = "Selectați o factură!";

