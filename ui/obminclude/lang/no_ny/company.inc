<SCRIPT language="php">
//////////////////////////////////////////////////////////////////////////////
// OBM - Language : Norwegian                                               //
//     - File     : company.inc                                             //
// 2000-07-20 Magni Onsoien                                                 //
//////////////////////////////////////////////////////////////////////////////

// Labels and texts
$l_last_company = "Last company";
$l_last_contact = "Last contact";
$l_last_deal="Last deal";
$l_company_name = "Firmanamn";
$l_company_kind = "Firmakategori";
$l_orgnr = "Organisasjonssnummer";
$l_postcode = "Postnummer";
$l_all = "Alle";
$l_find = "Finn";
$l_mailing_first = "M";
$l_name = "Namn";
$l_contact_first = "K";
$l_newcontact_first = "NK";
$l_listdoc_first = "A";
$l_newdoc_first = "NA";
$l_kind = "Kategori";
$l_address = "Adresse";
$l_postcode_first = "ZIP";
$l_expresspostal = "Ekspresspost";
$l_town = "By";
$l_country = "Land";
$l_phone = "Telefon";
$l_fax = "Fax";
$l_email = "E-post";
$l_web = "Web";
$l_comment = "Kommentar";
$l_contact_list = "Kontaktliste";
$l_contact_new = "Ny kontakt";
$l_deal_list = "Avtaleliste";
$l_deal_new = "Ny avtale";
$l_kind_manage = "Firmakategori";
$l_kind_exist = "Eksisterande kategoriar";
$l_kind_new = "Ny kategori";
$l_invalid_data = "Ugyldig data";
$l_internal = "Internal";
$l_number_soc = "Number";

// Titres
$l_company = "Firma";
$l_company_detail = "Firmadetaljer";
$l_company_new = "Nytt firma";
$l_company_insert = "Legg til firma";
$l_company_update = "Oppdater firma";
$l_company_delete = "Slett firma";
$l_company_admin = "Administrer firma";

// Actions
$l_cancel = "Cancel";
$l_insert = "Legg til firma";
$l_update = "Oppdater firma";
$l_delete = "Slett firma";
$l_mailing_update = "Update Mailing";
$l_mailing_reset = "Reset Mailing";
$l_kind_update = "Oppdater kategori";
$l_kind_delete = "Slett kategori";
$l_kind_insert = "Legg til kategori";

// Messages
$l_company_del_verif_error = " deal(s) are attached to this company : it can't be deleted";
$l_error_permission = "You have not the rights to access this page";
$l_no_display = "Skriv inn søkekriteria og trykk < Finn > for å finne firma";
$l_found = "found (to translate) !";
$l_no_found = "Ingen firma som passa til søket !";
$l_delete_ok = "Firmaet er sletta.";
$l_delete_error = "Firmaet kunne ikkje slettast!";
$l_insert_ok = "Firmaet er lagt til.";
$l_insert_error = "Firmaet kunne ikkje leggast til!";
$l_update_ok = "Firmaet er oppdatert.";
$l_update_error = "Firmaet kunne ikkje oppdaterast !";
$l_query_error = "Problem med å utføre søket.";
$l_mailing_ok = "The mailing has been updated successfully.";
$l_mailing_nb = "companies registered in the mailing.";
$l_mailing_error = "Error in the registration in the mailing of the company with code";
$l_mailing_reset_error = "The reset of the mailing has failed !";
$l_mailing_reset_ok = "The reset of the mailing has been completed successfully.";
$l_kind_insert_ok = "Kategori lagt til.";
$l_kind_insert_error = "Kategori kunne ikkje leggast til!";
$l_kind_delete_ok = "Kategorien er sletta.";
$l_kind_delete_error = "Kategorien kunne ikkje slettast.";
$l_kind_update_ok = "Kategorien er oppdatert.";
$l_kind_update_error = "Kategorien kunne ikkje oppdaterast.";
$l_kind_del_verif_error = "(ar) av denne typen finst.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "\\n Du må fylle inn namn før du legg inn.";
$l_fill_number = "\\n You have to fill the Number before submitting.";
$l_fill_postcode = "\\n Du må fylle inn postnummer før du legg inn.";
$l_fill_address = "\\n Du må fylle inn adressa før du legg inn.";
$l_fill_phone = "\\n Du må fylle inn telefonnummer før du legg inn.";
$l_delete_confirm = "Slett firmaet?"; 
$lj_check_zip = "Postnummeret er ugyldig.";
$lj_check_phone = "Telefonnummeret er ugyldig.";
$lj_check_pphone = "Privatnummeret er ugyldig.";
$lj_check_mphone = "Mobiltelefonnummeret er ugyldig.";
$lj_check_fax = "Faksnummeret er ugyldig.";
$lj_check_email = "E-post-adressa er ugyldig.";
$lj_check_web = "Webadressa er ugyldig.";
$mj_kind_delete = "Du må velge ein kategori for å slette den!";
$mj_kind_update = "Du må velge ein kategori og legge inn eit nytt namn for å oppdatere den!";
$mj_kind_create = "Du må legge inn eit nytt kategorinamn for å opprette det!"; 
$mj_kind_delete_confirm = "Slett kategori?";

// Specifics fields
// MySQL Date and time format for display in the deal detail screen
$l_time_format = "%Y-%m-%d %H:%i:%s";
$l_date_format = "%Y-%m-%d %H:%i:%s";

// For the display parameters modification screen
$l_company_options = "Display options for companies list"; 
$l_subject = "Subject" ;
$l_options_dis = "options of display" ;
$l_query_display = "Displayed" ;
$l_order_dis = "Order of display" ;
$l_action = "Action" ;
$l_ch_to_dis_no = "Not to display it fields" ;
$l_ch_to_dis_no_help = "That indicates the appropriate field is displayed<br>Click on this button if you don't want to display the field" ;
$l_ch_to_dis_yes = "To display it fields" ;
$l_ch_to_dis_yes_help = "That indicates the appropriate field is not displayed<br>Click on this button if you want to display the field" ;
$l_dis_obligatory = "Display of this fields is mandatory" ;
$l_dis_obligatory_help = "This button means you don't have choice. The field appropriate field is be displayed" ;
$l_down_option = "To descend this option from a notch" ;
$l_down_option_help  = "Click on this button to move down the field" ;
$l_up_option = "To assemble this option of a notch" ;
$l_up_option_help = "Click on this button to move up the field" ;
$l_back_tolist = "Back to list display" ;
$l_help_options = "Contextual help" ;
$l_option_contacts = "Options of contacts list display" ;
