<SCRIPT language="php">
//////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italian                                                 //
//     - File     : account.inc                                             //
// 01/08/2002 Augusto Fagioli                                               //
//////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label = "Etichetta";
$l_label_start = "Inizio Etichetta";
$l_last_account = "Ultimo account";
$l_bank = "Banca";
$l_number = "Numero";
$l_balance = "Saldo";
$l_initial_balance = "Creazione Saldo";
$l_comment = "Commento";
$l_choose_balance_date = "Data Saldo";
$l_date = "Data";
$l_expected_date = "Data Prevista";
$l_amount = "Totale";


$l_include_expected = "Inserisci le operazioni richieste?";
$l_in_total ="Nel  Totale";
$l_out_total = " Totale Fuori ";
$l_total ="Saldo Previsto ";
$l_old_balance = "Saldo odierno";
$l_nb_expected_used = " operazioni previste per il calcolo.";
$l_expected = "previsto";
$l_used_payments = "Pagamenti Considerati nel conteggio del saldo ";
$l_used_expected_payments = "Pagameni previsti usati nel calcolo del saldo";
// Titres
$l_account = "Account";

// Actions
$l_find = "Trova";
$l_insert= "Inserisci Account";
$l_update = "Aggiorna Account";
$l_delete = "Cancella Account";
$l_delete_confirm = "Confermi l'eliminazione dell'account ?";
$l_cancel = "Cancella";
$l_compute_balance = "Elabora Saldo";

// Messages
$l_no_display = "Riempi i campi di ricerca e premi  < Trova > per ricevere gli account";
$l_found="Account trovati.";
$l_no_found="Non sono presenti corrispondenze per gli account cercati ";
$l_delete_ok = "Account eliminato .";
$l_delete_error = "Eliminazione Account Fallita !";
$l_insert_ok = "Account inserito .";
$l_insert_error = "Inserimento account Fallito  !";
$l_update_ok = "Account Aggiornato .";
$l_update_error = "Aggiornamento account fallito !";
$l_query_error = "Problemi nell'esecuzione della ricerca...";
$l_impossible_deletion = " alcuni pagamenti sono associati a questo account. Impossibile cancellare";

// must be used with the number of payments leading...
// champs specifiques
// format de date et heure pour MySQL pour affichage dans ecran detail affaire
// Specifics fields
// MySQL Date and time format for display in the deal detail screen
$l_time_format = "%d-%m-%Y %H:%i:%s";
$l_date_format = "%d-%m-%Y %H:%i:%s";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_check_number = "\\nNumero account non valido ! ";
$lj_fill_number = " Inserisci prima il numero ! ";
$lj_fill_label = "Inserisci prima l'etichetta !";
$lj_fill_bank = "Inserisci prima la Banca !";
$lj_confirm_no_bank="Questo accoutn non è associato a banche. Click OK per confermare.";
$l_invalid_date="\\nForamto data non valido (GG/MM/AAAA or AAAA-MM-GG)";
$l_fill_date = "\\n Inserisci prima la data .";

// For the display parameters modification screen
$l_account_options = "Visualizza le opzioni per la lista di account ";
$l_payment_options = "Visualizza le opzioni per la lista di pagamenti ";
$l_options_dis = "Opzioni di visualizzazione " ;
//$l_subject = "Oggetto" ;
$l_query_display = "Visualizzato" ;
$l_order_dis = "Ordine di visualizzazione" ;
$l_action = "Azione" ;
$l_ch_to_dis_no = "non visualizzarlo come campo" ;
$l_ch_to_dis_no_help = "Indica che il relativo campo è visualizzato<br>Click qui se non vuoi visualizzare il campo" ;
$l_ch_to_dis_yes = "To display it fields" ;
$l_ch_to_dis_yes_help = "Indica che il relativo campo non è visualizzato<br>Click qui se vuoi visualizzare il campo" ;
$l_dis_obligatory = "Campo obbligatorio" ;
$l_dis_obligatory_help = "Il pulsante significa che non hai scelta. Il campo appropriato deve essere visualizzato" ;
$l_down_option = "scendi in bassoda un notch" ;
$l_down_option_help  = "Click qui per andare sotto il campo " ;
$l_up_option = "Per unire questa opzione di  un  notch" ;
$l_up_option_help = "Click qui per muovere sopra il campo " ;
$l_back_tolist = "Torna alla lista display" ;
$l_help_options = "Aiuto contestuale " ;
