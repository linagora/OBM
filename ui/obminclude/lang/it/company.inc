<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : company.inc                                              //
// 2002-08-01 Augusto Fagioli                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numero";
$l_name = "Nome";
$l_company_name = "Nome Azienda";
$l_kind = "Tipo";
$l_activity = "Activita";
$l_company_kind = "Tipo Azienda";
$l_address = "Indirizzo";
$l_postcode = "CAP";
$l_postcode_first = "CAP";
$l_town = "Citta";
$l_expresspostal = "CAP 2";
$l_country = "Nazione";
$l_phone = "Telefono";
$l_fax = "Fax";
$l_web = "Web";
$l_email = "E-Mail";

// Labels and texts
$l_last_company = "Ultima Azienda";
$l_last_contact = "Ultyimo Contatto";
$l_last_deal = "Ultimo Impegno";
$l_mailing_first = "M";
$l_listdoc_first = "D";
$l_newdoc_first = "ND";
$l_contact_list = "Lista Contatti";
$l_contact_new = "Nuovo Contatto";
$l_deal_list = "Lista Impegni";
$l_deal_new = "New Deal";
$l_kind_manage = "Azienda Tipo Management";
$l_kind_exist = "Tipi esistente";
$l_kind_new = "Nuovo Tipo";
$l_act_manage = "Azienda Activita Management";
$l_act_exist = "Activita esistente";
$l_act_new = "Nuova Activita";
$l_internal = "Interno";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["company_name"] = $l_name;
$fieldnames["company_address1"] = $l_address;
$fieldnames["company_address2"] = $l_address;
$fieldnames["company_phone"] = $l_phone;
$fieldnames["company_fax"] = $l_fax;
$fieldnames["company_email"] = $l_email;
$fieldnames["company_web"] = $l_web;
// Calculated or indirect fields
$fieldnames["companytype_label"] = $l_kind;
$fieldnames["companyactivity_label"] = $l_activity;
$fieldnames["company_contact_number"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_list\" ALT=C>";
$fieldnames["company_new_contact"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_new\" ALT=NC>";
$fieldnames["company_deal_number"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_deal_list\" ALT=D>";

// Titres
$l_company = "Azienda";

// Actions
$l_insert = "Inserisci Azienda";
$l_update = "Aggiorna Azienda";
$l_checkdelete = "Controlla o Elimina Azienda";
$l_delete = "Elimina Azienda";
$l_mailing_update = "Aggiorna  Mailing";
$l_mailing_reset = "Resetta Mailing";
$l_kind_update = "Aggiorna Tipo";
$l_kind_checkdelete = "Controlla o Elimina Tipo";
$l_kind_delete = "Elimina Tipo";
$l_kind_insert = "Inserisci Tipo";
$l_act_update = "Aggiorna Activita";
$l_act_checkdelete = "Controlla o Elimina Activita";
$l_act_delete = "Elimina Activita";
$l_act_insert = "Inserisci Activita";
$l_confirm = "Conferma azione";
$l_select_company = "Seleziona Azienda";

// Messages
$l_error_permission = "Pagina riservata";
$l_no_display = "Riempi i  campi  e premi < Trova> per la ricerca";
$l_found = "Aziende Trovate.";
$l_no_found = "Nessuna  Azienda trovata !";
$l_delete_ok = "Azienda eliminata .";
$l_delete_error = " Eliminazione Azienda fallita !";
$l_insert_ok = " Azienda inserita .";
$l_insert_error = "Inserimento Azienda fallito !";
$l_check_samecompany = "Queste Aziende sembra simili alla nuova ...";
$l_update_ok = "Azienda modificata.";
$l_update_error = "Modifica Azienda Fallita !";
$l_query_error = "Errore critico nell'esecuzione della query.";
$l_mailing_ok = "Mailing aggiornato.";
$l_mailing_nb = "Aziende registrate nel mailing.";
$l_mailing_reset_ok = "Reset Mailing completato con successo.";
$l_link_deal = "l'Azienda ha degli impegni ";
$l_link_deal_no = "l'Azienda non ha degli impegni ";
$l_link_contact = "Questi contatti sono di questa Azienda";
$l_link_contact_no = "l' Azienda non ha contatti";
$l_cant_delete = "Azienda non eliminabile !";
$l_can_delete = "Azienda eliminabile !";
$l_kind_insert_ok = "Tipo inserito.";
$l_kind_insert_error = "Inserimento tipo fallito !";
$l_kind_delete_ok = "Tipo eliminato.";
$l_kind_delete_error = "Eliminazione tipo fallita !";
$l_kind_update_ok = "Tipo modificato.";
$l_kind_update_error = "Modifica tipo fallito !";
$l_kind_link_company = " Aziende legate a  ";
$l_kind_link_company_no = "Azienda non legata";
$l_kind_cant_delete = "Tipo non eliminabile.";
$l_kind_can_delete = "Tipo eliminabile.";
$l_act_insert_ok = "Activita inserita.";
$l_act_insert_error = "Inserimento activita fallito !";
$l_act_delete_ok = "Activita eliminata.";
$l_act_delete_error = "Eliminazione activita fallita !";
$l_act_update_ok = "Activita modificata.";
$l_act_update_error = "Modifica activita fallito !";
$l_act_link_company = " Aziende legate a  ";
$l_act_link_company_no = "Azienda non legata";
$l_act_cant_delete = "Activita non eliminabile.";
$l_act_can_delete = "Activita eliminabile.";

// Java Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Conferma eliminazione  Azienda ?";
$l_fill_name = "Inserisci prima il Nome .";
$l_j_check_zip = "Zip code non valido.";
$l_j_check_phone = "Telefono non  valido.";
$l_j_check_fax = "Fax non  valido.";
$l_j_check_email = " Indirizzo Email non  valido.";
$l_j_check_web = " Indirizzo  Web non  valido.";
$l_j_kind_delete = "Scegli un tipo da controllare o eliminare !";
$l_j_kind_update = "Scegli un tipo e inserisci il nuovo nome per la modifica !";
$l_j_kind_create = "Inserisci nuovo tipo per crearlo  !";
$l_j_act_delete = "Scegli una activita da controllare o eliminare !";
$l_j_act_update = "Scegli una activita e inserisci il nuovo nome per la modifica !";
$l_j_act_create = "Inserisci nuova activita per crearla !";
$l_j_select_company = "Scegli  Azienda !";

// For the display parameters modification screen
$l_company_display = "Mostra opzioni lista  Aziende ";

</script>
