<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : list.inc                                                 //
// 01-08-2002 Augusto Fagioli                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////


// Database fields
$l_name = "Nome";
$l_subject = "Oggetto";
$l_email = "E-mail";
$l_creator = "Creato da";
$l_date_creation = "Creazione";
$l_updater = "Ultima modificatore";
$l_date_last_update = "Ultima Modifica";

// Labels and texts
$l_last_deal="Ultimo Impegno";
$l_last_company = "Ultima azienda";
$l_last_contact = "Ultimo contatto";
$l_list = "Lista";
$l_yes = "si";
$l_no = "no";
$l_nb_contact = "N° di contatti";
$l_list_contact = "con contatto : ";
$l_list_display = "Opzioni visualizzazione dell'elenco liste";
$l_contact_display = "Opzioni visualizzazione dell'elenco contatti";
$l_list_set = "Imposta Lista";
$l_export_format = "Formati di esportazione";
$l_desc = "Descrizione";
$l_export_intro = "Seleziona il formato di esportazione";

// Contact
$l_lastname = "Cognome";
$l_firstname = "Nome";
$l_function = "Funzione";
$l_company = "Azienda";
$l_phone = "Tel.";
$l_hphone = "Tel. casa";
$l_town = "Città";

// Actions
$l_insert = "Inserisci lista";
$l_update = "Modifica lista";
$l_delete = "Elimina lista";
$l_change_auth = "Cambia auth";
$l_confirm = "Conferma azione";
$l_add = "Aggiungi queste liste";
$l_mail = "Invia email alla lista" ;

// Messages
$l_insert_ok = "Lista inserita.";
$l_insert_error = "Inserimento Lista fallito!";
$l_update_ok = "Lista modificata.";
$l_update_error = "Modifica lista fallito!";
$l_delete_ok = "Lista eliminata.";
$l_delete_error = "Eliminazione lista fallita!";
$l_fill_name = "Inserisci nome lista";
$l_name_list_nomatch = "Nome lista non valido";
$l_subject_list_empty = "Oggeto non valido";
$l_list_exists = "Lista già esistente";
$l_check_samelist = "Liste simili alla nuova!";
$l_j_check_email = "Email non valida.";
$l_warn_delete = "contatti in lista";

$l_no_display = "Scrivi i campi e click < TROVA > per la ricerca";
$l_no_list = "Nessuna lista trovata." ;
$l_found = "liste trovate.";
$l_no_found = "Nessuna lista trovata." ;

$l_add_contact = "Inserisci contatti" ;
$l_del_contact_sel = "Elimina questi contatti";
$l_contact_added = "Contatti inseriti";
$l_contact_removed = "Contatti eliminati";

//------------------------------------//
// Export Format informations         //
//------------------------------------//
// Format 0 : HTML Stickers
$c_export_format[0]["name"] = "HTML Stickers";
$c_export_format[0]["info"] = "HTML Preview for stickers";
// Format 1 :
$c_export_format[1]["name"] = "HTML Table";
$c_export_format[1]["info"] = "HTML Preview for table";
// Format 2 : RTF Stickers
$c_export_format[2]["name"] = "RTF Stickers";
$c_export_format[2]["info"] = "RTF Sticker creation";
// Format 3 : Excel data
$c_export_format[3]["name"] = "Excel Table";
$c_export_format[3]["info"] = "Excel table data";


$l_new_contact1 = "NC" ;
$l_list_contact1 = "C" ;
$l_list_infos = "Info Lista" ;
$l_list_infos_short = "Info Lista" ;
$l_list_noinfo = "Info non presenti" ;

$l_list_contacts = "Contatti registrati";
$l_con_found = "Contatti registrati";
$l_no_contact = "Non sono presenti contatti registrati";
$l_list_contacts_todel = "Qui modifica lista contatti." ;
$l_list_del_contact = "Elimina" ;
$l_list_del_contact_chosen = "Elimina i contatti selezionati" ;
$l_nb_contact_deleted = "N° dei contatti eliminati" ;
$l_name_contact = "Nome" ;
$l_name_company = "Azienda" ;
$l_list_add_contact = "Inserisci" ;
$l_list_add_contact_chosen = "Inserisci i contati selezionati" ;
$l_nb_contact_added = "N° dei contatti inseriti" ;
$l_delete_confirm = "Do you comfirm removal of list" ;
$l_complete_mail_form = "Please fill mail fields to sent mail" ;
$l_mail_sender = "Sender" ;
$l_mail_rcpt = "Reply to" ;
$l_mail_subject = "Mail subject" ;
$l_mail_body = "Mail body" ;
$l_send_mail = "Send mail" ;
$l_confirm_send = "Confirm to send mail" ;
$l_name_empty = "You must fill sender name" ;
$l_mail_empty = "The sender mail must be set" ;
$l_mail_false = "The sender mail must be valid" ;
$l_mail_same = "No used to inform this mail if it's the same than the sender mail" ;
$l_mail_rcpt_false = "The 'Reply to' mail must be valid'" ;
$l_subject_empty = "Subject of mail is required" ;
$l_body_empty = "Mail body is required" ;
$l_name_obligatory = "Your name is required" ;
$l_mail_obligatory = "Your e-mail is required" ;
$l_mail_rcpt_obli = "If diffrenet from sender e-mail" ;
$l_subject_obli = "The subject of mail is required" ;
$l_nb_message_send = "Number of mail(s) sent : " ;

//Error:
$l_error_which = "Error::: The which variable is NOT CORRECTLY set... Please Check your administrator" ;
$l_error_authentification = "Errore ::: Autentificazione interrotta ... Prego contattare, l'amministratore di sistema" ;
