<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : list.inc                                                 //
// 01-08-2002 Augusto Fagioli                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_subject = "Oggetto";
$l_email = "E-mail";
$l_query = "Query";
$l_creator = "Creato da";
$l_date_creation = "Creazione";
$l_updater = "Ultima modificatore";
$l_date_last_update = "Ultima Modifica";

// Labels and texts
$l_list = "Lista";
$l_nb_contact = "N° di contatti";
$l_list_contact = "con contatto : ";
$l_list_display = "Opzioni visualizzazione dell'elenco liste";
$l_contact_display = "Opzioni visualizzazione dell'elenco contatti";
$l_list_set = "Imposta Lista";
$l_export_format = "Formati di esportazione";
$l_desc = "Descrizione";
$l_export_intro = "Seleziona il formato di esportazione";
$l_select_list = "Select the lists to be exported";

// Contact
$l_lastname = "Cognome";
$l_firstname = "Nome";
$l_function = "Funzione";
$l_company = "Azienda";
$l_hphone = "Tel. casa";
$l_town = "Città";

// Actions
$l_insert = "Inserisci lista";
$l_update = "Modifica lista";
$l_delete = "Elimina lista";
$l_add = "Aggiungi queste liste";

// Messages
$l_insert_ok = "Lista inserita.";
$l_insert_error = "Inserimento Lista fallito!";
$l_update_ok = "Lista modificata.";
$l_update_error = "Modifica lista fallito!";
$l_delete_ok = "Lista eliminata.";
$l_delete_error = "Eliminazione lista fallita!";
$l_fill_name = "Inserisci nome lista";
$l_name_list_nomatch = "Nome lista non valido";
$l_list_exists = "Lista già esistente";
$l_check_samelist = "Liste simili alla nuova!";
$l_j_check_email = "Email non valida.";
$l_warn_delete = "contatti in lista";

$l_no_display = "Scrivi i campi e click < TROVA > per la ricerca";
$l_found = "liste trovate.";
$l_no_found = "Nessuna lista trovata." ;

$l_add_contact = "Inserisci contatti" ;
$l_del_contact_sel = "Elimina questi contatti";
$l_contact_added = "Contatti inseriti";
$l_contact_removed = "Contatti eliminati";

$l_fill_subject = "Oggeto non valido";
$l_invalid_query = "The query is invalid !";
$l_banned_word = "Banned word";

$l_static_con_found = "Static Contatti registrati";
$l_no_static_contact = "Non sono presenti static contatti registrati";
$l_dyn_con_found = "Dynamic Contatti registrati";
$l_no_dyn_contact = "Non sono presenti dynamic contatti registrati";

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
