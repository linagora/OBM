<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : invoice.inc                                              //
// 2001-08-02 Pierre Baudracco - Augusto Fagioli - augusto@fagioli.biz       //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label_start = "Inizio Etichetta";
$l_amount = "Totale";
$l_amount_ht = "Totale HT ";
$l_amount_ttc = "TTC Totale";
$l_status = "Stato";
$l_expiration_date = "Data expiration";
$l_payment_date = "Data pagamento";
$l_date_apres = "Dopo";
$l_date_avant = "Prima";
$l_paid = "Pagato Totale TTC";
$l_pick_deal = "Scegli un affare";
$l_no_deal = "Nessun affare collegato a questa fattura";
$l_no_payment = "Nessun pagamento collegato a questa fattura";
$l_list_payment = "Pagamenti relativi a questa fattura";
$l_inout = "Tipo";
$l_both = "Entrambi";

$l_invoice_infos = "Informazioni sulla fattura";
$l_remaining_amount = "Totale non usato";
$l_to_use_amount = "Totale relativo a questa fattura";
$l_affect = "Relativo[Affect]";

// Actions
$l_select_nb_deals = "Quanti affari sono legati a questa nuova fattura?";
$l_select_deals = "Scegli un numero di affari";
$l_insert= "Inserisci fattura";
$l_update = "Aggiorna Fattura";
$l_delete = "Elimina Fattura";
$l_delete_confirm = "Confermi l'elimnazione di questa fattura?";
$l_no_number_confirm = "Stai per creare una fattura senza numero.\\nClick OK per confermare.";
$l_error_which = "Err.5463 : var non impostata correttamente, ... prego, contattare l'amministratore del sistema" ;
$l_invoice_del_deal = "Elimina";
$l_invoice_del_payment = "Elimina";
$l_invoice_infos = "Vedi fattura";
$l_add_deal = "Aggiungi affare";
$l_add_deal_chosen = "Aggiungi gli affari selezionati";
$l_del_deal = "Elimina affari";
$l_del_deal_chosen = "Elimina gli affari selezionati";
$l_deals_todel = "Qui  withdrawable affari per questa fattura" ;
$l_add_payment = "Aggiungi pagamento";
$l_add_payment_chosen = "Aggiungi selezionato pagamenti";
$l_check_payment_chosen = "Controlla pagamenti selezionati";
$l_del_payment = "Elimina pagamento";
$l_del_payment_chosen = "Elimina pagamenti selezionati";
$l_payments_todel = "Qui withdrawable pagamenti per questa fattura" ;
$l_invoice_mod = "Modifica Fattura";
$l_invalid_payment = "Il totale preso da un pagamento è maggiore del suo totale";
$l_too_much_money = "il totale dei pagamenti è superiore all'importo complessivo della fattura. Se continui, questa fattura risulterà allo stato 'problema'";
$l_error_try_again = "Ci sono errori !! prego , ripeto l'operzione dell'ìnizio";
$l_try_again = "Prova ancora !";

// Messages
$l_no_display = "Riempi i campi di ricerca e premi < TROVA> per la lista delle fatture";
$l_no_deal_display = "Riempi i campi di ricerca e premi < TROVA > per la lista degli affari";
$l_no_payment_display = "Riempi i campi di ricerca e premi < TROVA > per la lista dei pagamenti";
$l_found="Trovate fatture";
$l_no_found="Nessuna fattura corrisponde alla ricerca";
$l_no_deal_found ="Nessun affare corrisponde alla ricerca";
$l_no_payment_found ="Nessun pagamento corrisponde alla ricerca";
$l_delete_ok = "La fattura è stata eliminata";
$l_delete_error = "Eliminazione della fattura non completata";
$l_remove_deal_ok = "Gli affari sono stati eliminati";
$l_remove_deal_error = "Eliminazione degli affari non completata";
$l_insert_ok = "La fattura  è stata  registata";
$l_insert_error = "Registrazione fattura non eseguita";
$l_insert_deal_ok = "Gli affari sono stati inseriti";
$l_insert_deal_error = "Inserimento degli affari non completato";
$l_update_ok = "Fattura modificata";
$l_update_error = "Modifica della NON eseguita";
$l_query_error = "Err. 6b59d :Problemi nelle'esecuzione della richiesta";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_check_number = "\\nNumero fattura non valido!";
$lj_check_money_ht = "\\nTotale senza spese espese non valido";
$lj_check_money_ttc = "\\nTotale spese incluse non valido";
$lj_check_amounts = "\\nIl totale spse incluse è inferiore al totale senza spese";
$lj_check_inout = "\\nScelgi un tipo prima di inviare la richiesta";
$lj_changing_inout = "Non puoi cambiare il tipo di fattura  \\n se sono presenti affari collegati";
$lj_no_number="\\nInserisci un numero fattura";
$lj_fill_date = "\\n Inserisci una data";
$lj_fill_label = "\\n Inserisci un etichetta";
$l_invalid_date="\\nIFormato Data non valido (MM/GG/AAAA or AAAA-MM-GG)";
$lj_check_deal = "Seleziona un affare.";
$lj_select_deals = "Seleziona un numero di affari";

// For the display parameters modification screen
$l_invoice_display = "Mostra opzioni nella lista delle fatture";

// default invoice numbering policy :
$default_invoice_numbering = "Y"; // we use only year