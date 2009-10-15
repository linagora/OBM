<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : invoice.inc                                              //
// 2001-08-02 Pierre Baudracco - Augusto Fagioli - augusto@fagioli.biz       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-11T17:44:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:27:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:32:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:34:03+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:38:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:38:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:39:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:39:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:51:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T09:54:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:01:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:06:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:08:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:10:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:12:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:14:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:16:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:23:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:25:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:28:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:35:56+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:37:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:37:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:47:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T10:59:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:03:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:15:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:18:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:18:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:21:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:24:41+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:25:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:29:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:29:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:35:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:46:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:47:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:48:24+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:48:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:48:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:49:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:50:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:50:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:51:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:52:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:53:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:54:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:56:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:57:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:58:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:58:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:59:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T11:59:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:00:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:01:03+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:01:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:02:03+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:02:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:03:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:04:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:05:25+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:07:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:07:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:08:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:10:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:10:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:10:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:11:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:12:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:13:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:14:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:15:03+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:15:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:16:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:16:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:17:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:19:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:19:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:20:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:20:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:21:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:21:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:21:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:22:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:22:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:23:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:25:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:26:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:27:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:27:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:27:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:28:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:30:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T12:33:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:15:38+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:17:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:20:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:23:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:25:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:27:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:27:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:27:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:30:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:32:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:32:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:37:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:40:11+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:41:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:45:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:51:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:53:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T14:55:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:02:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:12:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:14:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:17:04+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:22:29+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:22:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:26:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:26:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:27:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:44:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:46:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T15:59:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T16:01:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T16:08:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T16:27:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T16:31:21+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T17:09:15+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T17:27:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T17:27:47+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T17:46:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T18:12:05+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T18:12:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-16T18:22:05+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:38:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:39:32+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:40:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:41:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:42:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:42:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:45:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:45:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:47:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T11:49:24+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:01:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:02:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:05:02+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:06:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:10:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:14:37+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:15:03+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:16:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:17:35+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:18:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:21:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:21:56+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T12:23:57+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:24:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:25:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:30:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:32:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:34:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:35:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:37:10+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:37:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:39:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:41:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:41:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:41:58+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:43:50+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:54:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:54:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:55:40+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:55:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:57:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:57:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:58:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:58:54+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T14:59:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T15:00:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T15:03:14+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T15:06:26+02:00 using OBMtranslation (0.4.1)

$l_label_start = "Inizio Etichetta";
$l_amount = "Importo";
$l_amount_ht = "Importo netto";
$l_amount_ttc = "Importo Lordo";
$l_expiration_date = "Data di scadenza";
$l_payment_date = "Data pagamento";
$l_paid = "Pagato";
$l_waiting = "In attesa";
$l_pick_deal = "Scegli un affare";
$l_no_deal = "Nessun affare collegato a questa fattura";
$l_no_payment = "Nessun pagamento associato a questa fattura";
$l_list_payment = "Pagamenti relativi a questa fattura";
$l_inout = "Tipo";
$l_both = "Entrambi";
$l_invoice_infos = "Informazioni sulla fattura";
$l_remaining_amount = "Importo non usato";
$l_to_use_amount = "Importo associato a questa fattura"; 
$l_affect = "Relativo";
$l_select_nb_deals = "Quanti affari sono legati a questa nuova fattura?";
$l_select_deals = "Scegli un numero di affari";
$l_insert= "Inserisci fattura";
$l_update = "Aggiorna Fattura";
$l_delete = "Elimina Fattura";
$l_delete_confirm = "Confermi l'eliminazione di questa fattura?";
$l_no_number_confirm = "Stai per creare una fattura senza numero.\\nPremi OK per confermare.";
$l_error_which = "Err.5463 : var non impostata correttamente, ... prego, contattare l'amministratore sistema" ;
$l_invoice_del_deal = "Elimina";
$l_invoice_del_payment = "Elimina";
$l_invoice_infos = "Vedi fattura";
$l_add_payment = "Aggiungi pagamento";
$l_add_payment_chosen = "Aggiungi i pagamenti selezionati";
$l_check_payment_chosen = "Controlla pagamenti selezionati";
$l_del_payment = "Elimina pagamento";
$l_del_payment_chosen = "Elimina pagamenti selezionati";
$l_payments_todel = "Pagamenti associati a questa fattura";
$l_invoice_mod = "Modifica Fattura";
$l_invalid_payment = "L'importo preso da un pagamento è maggiore del suo totale";
$l_too_much_money = "il totale dei pagamenti è superiore all'importo complessivo della fattura. Se continui, questa fattura risulterà allo stato 'problema'";
$l_error_try_again = "Ci sono errori !! ripeti l'operazione!";
$l_try_again = "Prova ancora !";
$l_no_display = "Riempi i campi di ricerca e premi < CERCA> per ottenere l'elenco delle fatture.";
$l_no_deal_display = "Riempi i campi di ricerca e premi < TROVA > per la lista degli affari";
$l_no_payment_display = "Riempi i campi di ricerca e premi < TROVA > per la lista dei pagamenti";
$l_found = "Fattura(e) trovata(e).";
$l_no_found = "Nessuna fattura non corrisponde alla ricerca";
$l_no_deal_found = "Nessun affare non corrisponde alla ricerca";
$l_no_payment_found = "Nessun pagamento non corrisponde alla ricerca";
$lj_check_number = "\\nNumero fattura non valido!";
$lj_check_money_ht = "\\nTotale senza spese non valido";
$lj_check_money_ttc = "\\nTotale spese incluse non valido";
$lj_check_amounts = "\\nIl totale spese incluse è inferiore al totale senza spese";
$lj_check_inout = "\\nScelgi un tipo prima di inviare la richiesta";
$lj_changing_inout = "Non puoi cambiare il tipo di fattura  \\n se sono presenti affari collegati";
$lj_no_number="\\nInserisci un numero fattura";
$lj_fill_date = "\\n Inserisci una data";
$lj_fill_label = "\\n Inserisci un etichetta";
$l_invalid_date="\\nFormato Data non valido (MM/GG/AAAA or AAAA-MM-GG)";
$lj_check_deal = "Seleziona un affare.";
$lj_select_deals = "Seleziona un numero di affari";
$l_invoice_display = "Opzioni visualizzazione dell'elenco Fatture"; 
$default_invoice_numbering = "Y";
$l_invoicestatus_paid = "Pagato";
$l_ht_nan = "L'importo netto non è valido !";
$l_invoicestatus_cancelled = "Annulato";
$l_check_inout = "\\nScegli un tipo !";
$l_compute_tax = "Calcolare l'importo lordo";
$l_check_amounts = "\\nL'importo lordo deve essere superiore all'importo netto!"; 
$l_check_money_ttc = "\\nL'importo lordo della Fattura non è corretto!";
$l_link_payment_no = "La fattura non corrisponde a nessun pagamento";
$l_fill_amount = "Gli importi devono essere inseriti correttamente!";
$l_j_select_invoice = "Scegli una fattura!";
$l_invoicestatus_conflict = "In conflitto";
$l_select_invoice = "Seleziona la fattura";
$l_fill_label = "Riempi l'etichetta!";
$l_err_status_received_customer = "Lo stato $l_invoicestatus_received non è valido per le fatture di tipo $l_client !";
$l_tax_rate = "IVA";
$l_invoicestatus_sent = "Inviato";
$l_rate_nan = "L'IVA non è valida!";
$l_include_archive = "Includi gli archivi";
$l_credit_memo = "Credito";
$l_cant_delete = "La fattura non può essere eliminata !";
$l_fill_number = "Riempi il numero (per lo stato selezionato) !";
$l_payments_exist = "Ci sono pagamenti associati a questa fattura !";
$l_invoicestatus_ppaid = "Parzialmente Pagata";
$l_link_payment = "Questi pagamenti sono associati a questa fattura";
$l_invoicestatus_loss = "Persa";
$l_err_paid_status = "La fattura è stata pagata, lo stato deve essere '$l_invoicestatus_paid' !";
$l_expiration = "Scadenza";
$l_err_ppaid_status = "La fattura è stata parzialmente pagata, lo stato deve essere '$l_invoicestatus_ppaid' o '$l_invoicestatus_conflict' !";
$l_check_money_ht = "\\n L'importo netto della Fattura non è corretto!";
$l_changing_inout = "Non puoi cambiare il tipo della Fattura se questa è associata a degli Affari."; 
$l_invoice_del_paiement = "Elimina";
$l_error_number_exist = "Il numero di fattura è stato già attribuito!";
$l_can_delete = "La fattura non può essere eliminata.";
$l_amount_remaining = "Restante di";
$l_err_no_payment_status = "La fattura non ha pagamenti, lo stato non può essere  '$l_invoicestatus_paid' o '$l_invoicestatus_ppaid' !";
$l_fill_company = "Inserisci Azienda!";
$l_fill_inout = "Il tipo deve essere selezionato  : $l_client o $l_supplier !";
$l_fill_date = "Inserisci data ( per lo stato scelto)!";
$l_year_to_year = "Confronto anni";
$l_no_number = "\\nLa fattura deve avere un numero!";
$l_invoicestatus_received = "Ricevuto";
$l_check_number = "\\nIl Numero della Fattura non è valido!";
$l_invoicestatus_tocreate = "Da Creare";
$l_duplicate = "Duplicare questa Fattura";
$l_invoicestatus_factor = "Giornaliero ";

