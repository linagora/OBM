<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : contract.inc                                             //
// 02/08/2002  Augusto Fagioli                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields

// Labels et textes
$l_contract_num="Numero";
$l_contract_label="Contract etichetta";
$l_contract_company="Company";
$l_contract_contracttype="Tipo di contract";
$l_contract_expiration="Expiration";
$l_date_alarm_after = "Scadenza date Dopo";
$l_date_alarm_before = "Scadenza date Prima";
$l_after = "Scadenza data Dopo";
$l_before = "Scadenza data Prima";
$l_incident_list = "List di the Incidents";
$l_incident_new = "Aggiungi a nuovo Incident";

$fieldnames["contract_label"] = $l_contract_label;
$fieldnames["contract_company_name"] = $l_contract_company;
$fieldnames["contract_number"] = $l_contract_num;
$fieldnames["contract_expiration"] = $l_contract_expiration;
$fieldnames["contracttype_label"] = $l_contract_contracttype;

$l_minus="-";
$l_plus="+";
$l_order="Ordrer";
$l_last_contract = "Ultimo support contract";
$l_last_contact = "Ultimo contatto";
$l_last_company = "Ultima azienda";
$l_todo = "To Do";
$l_label = "Label";
$l_label_libelle = "Key-word di the etichetta";
$l_label_start = "numero";
$l_kind = "Tipo";
$l_category = "Categoria";
$l_state = "Stato";
$l_marketing_manager = "Marketing Resp.";
$l_technical_manager = "Tecnico Resp.";
$l_client_manager = "Client Resp.";
$l_manager= "Responsabile";
$l_contacts = "Contatti";
$l_clause = "Clauses";
$l_company = "None dell'Azienda";
$l_date = "Data";
$l_date_init = "Data inizio";
$l_date_expir = "Data scadenza ";
$l_date_alarm = "Data Avviso";
$l_last_update = "Ultimo aggiornamento";
$l_at = "&agrave;";
$l_number = "Numero";
$l_private = "Privato";
$l_public = "Pubblico";
$l_visibility ="Accesso";

$l_type_manage = "Supporto Contratto Tipo management";
$l_type_exist = "Tipo esistenti";
$l_type_new = "Nuovo Tipo";
$l_category_label="Etichetta";
$l_category_minilabel="Mini Etichetta";
$l_select_contract = "Scegli il contratto";

// Titres
$l_deal = "Supporto Contratto";
$l_deal_detail = "Dettagli";
$l_deal_new = "Nuovo Contratto";
$l_deal_insert = "Inserimento Contratto";
$l_deal_update = "Contratto aggiornamento";
$l_deal_delete = "Contratto eliminazione";

// Actions
$l_insert = "Inserisci Contratto";
$l_update = "Aggiorna Contratto";
$l_delete = "Elimina Contratto";
$l_select_company = "Scesli azienda";

$l_type_delete = "Cancella Tipo";
$l_type_update = "Aggiorna Tipo";
$l_type_insert = "Crea Tipo";
$l_status_update = "Aggiorna Status";
$l_status_delete = "Cancella Status";
$l_status_insert = "Crea Status";
$l_category_delete = "Cancella Categoria";
$l_category_update = "Aggiorna Categoria";
$l_category_insert = "Crea Categoria";


// Messages

$l_error_permission = "Acceso Riservato alla pagina";
$l_error_visibility = "Acceso Riservato all'informazione";
$l_no_display = "Riempi il campo di ricerca e premi < TROVA > per travare il Contratto";
$l_found = "contratti trovati.";
$l_no_found = "Non sono stati trovati Contratto !";
$l_delete_ok = "Contratto cancellato .";
$l_delete_error = "Eliminazione Contratto fallita !";
$l_insert_ok = "Contratto inserito.";
$l_insert_error = "Inserimento Contratto fallito !";
$l_update_ok = "Contratto modificato .";
$l_update_error = "Modifica Contratto fallita !";
$l_query_error = "Errori durante la ricerca.";

$l_type_insert_ok = "Tipo has been inserted .";
$l_type_insert_error = " insertion di the Tipo fallito !";
$l_type_update_ok = " Tipo has been updated successfully";
$l_type_update_error = " update di the Tipo fallito !";
$l_type_delete_error = "La suppression du type a &eacute;chou&eacute;e !";
$l_type_del_verif_error = "(s)  sont de ce type.";
$l_type_delete_ok = " Tipo has been cancellato.";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_label = "\\n You have to fill the Label before submitting.";
$l_fill_number = "\\n You have to fill the Number before submitting.";
$l_j_select_company = "You have to select a azienda !";
$l_fill_date = "You have to fill the Date before submitting..";
$l_invalid_date = "Invalid date format (DD/MM/YYYY or YYYY-MM-DD).";
$l_delete_confirm = "Confermi eliminazione the Contratto ?";

$mj_type_delete_confirm = "Confermi eliminazione the Tipo ?";
$mj_type_delete = "You have to select a Tipo to eliminazione it !";
$mj_status_delete_confirm = "Confermi eliminazione the state ?";
$mj_status_delete = "You have to select a state to eliminazione it !";
$mj_category_delete_confirm = "Confermi eliminazione the categoy di deal ?";
$mj_category_delete = "You have to select a categoryto eliminazione it !";
$mj_type_update = "You have to select a Tipo and enter a modification to submit !";
$mj_status_update = "You have to select a state and enter a modification to submit !";
$mj_category_update = "You have to select a category and enter a modification to submit !";
$mj_type_create = "You have to enter a nuovo Tipo name to create it !";
$mj_status_create = "You have to enter a nuovo satus name to create it !";
$mj_category_create = "You have to enter a nuovo category name to create it !";
$l_length_label = "  Etichetta troppo lunga";
$l_length_number = " Nummero Contratto troppo lungo ";

// For the display preferences modification screen
$l_contract_display = "Opzioni visualizzazione per Supporto contratti";
$l_updater = "Ultima modifica di " ;
$l_date_last_update = "Data ultima modifica" ;
