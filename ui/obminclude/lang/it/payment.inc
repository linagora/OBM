<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : payment.inc                                              //
// 2002-08-01 Augusto Fagioli                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes

$l_label = "Etichetta";
$l_label_start = "Etichetta inizio";
$l_last_payment = "Ultimo pagamento";
$l_number = "Numero";
$l_amount = "Importo";
$l_date = "Data";
$l_expected_date = "Scadenza";
$l_date_apres = "Data Prima";
$l_date_avant = "Data Dopo";
$l_account = "Account";
$l_type = "Tipo";
$l_inout = "Tipo";
$l_both = "Entrambi";
$l_client = "Cliente";
$l_fournisseur = "Fornitore";
$l_pick_paymentkind = "Scegli un tipo di pagamento";
$l_kind = "Tipo";
$l_pick_account = "Scegli un account";
$l_payment_account = "Account ";
$l_account = "Account" ;
$l_status = "Status";
$l_paid = "Pagato";
$l_unpaid = "Non Pagato";
// invoice connected :
$l_invoice_label = "Etichetta Fattura";
$l_invoice_date = "Data Fattura";
$l_invoice_TTC = "Importo Fattura";
$l_invoice_number = "Numero Fattura";
$l_invoice_not_paid_yet = "Importo non pagato PRIMA";
$l_invoice_still_not_paid = "Import non pagato ORA";
$l_invoice_share = "Share affected";
$l_invoice_inout = "Tipo fattura";
$l_invoice_paid = "Paid TTC Amount";
$l_already_used_share = "Part of the Pagamento already used ";
// Titres
$l_payment = "Pagamenti";
$l_payment_infos = "Informazioni sui pagamenti";

// Actions
$l_insert= "Inserisci pagamento";
$l_update = "Aggiorna pagamento";
$l_delete = "Elimina pagamento";
$l_bank = "Banca";
$l_delete_confirm = "Confermi eliminazione del pagamento ?";
$l_error_which = "Errore : The which variable is NOT CORRECTLY set... Please Check your administrator" ;
$l_payment_infos = "Vedi";
$l_payment_mod = "Modifica";
$l_search_invoice ="Cerca fattura";
$l_select_invoice = "Usa";
$l_affect_invoices = "Fatture del pagamento";
$l_duplicate = "Duplica il pagamento";

// Messages
$l_no_display = "Riempi i campi e click su < TROVA > per la ricerca";
$l_found="pagamenti trovati.";
$l_no_found="Nessun pagamento corrispondente";
$l_no_invoices_connected = "Nessuna fattura collegata al Pagamento";
$l_delete_ok = "Pagamento eliminato.";
$l_delete_error = "Eliminazione pagamento fallita!";
$l_insert_ok = "Pagamento inserito.";
$l_insert_error = "Inserimento pagamento fallito !";
$l_update_ok = "Pagamento modificato.";
$l_update_error = "Modifica pagamento fallita!";
$l_query_error = "Problemi durante la query.....";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_no_number = "\\nNumero pagamento non valido!";
$lj_check_amount = "\\nImporto non valido!";
$lj_check_inout = "\\nSeleziona prima se uscita o entrata";
$lj_check_paymentkind = "Scegli prima il tipo di pagamento!";
$lj_check_account = "Scegli prima un account !";
$l_no_amount = "Inserisci prima importo del pagamento";
$l_invalid_amount = "Ultimo importo inserto non valido";

// For the display parameters modification screen
$l_payment_options = "Opzioni di visualizzazione per elenco dei pagamenti";

// banking check messages :
$l_not_enough = "Non hai usato tutto l'importo del pagamento  !<BR>";
$l_too_much = "Il pagamento va oltre la disponibilità di cassa!<BR>";
$l_trouble_invoice = "Almeno una fattura ha ricevuto troppi soldi....<BR>Lo status è  \"trouble\".<BR>Confermi? <BR>";
$l_bank_anyway = "Banca del  Pagamento";
$l_bank_again = "Riparti il banking dall'inizio.";
