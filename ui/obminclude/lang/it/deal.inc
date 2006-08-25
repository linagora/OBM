<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : deal.inc                                                 //
// 2002-08-02 Augusto Fagioli - augusto@fagioli.biz                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_date_init = "Data inizio";
$l_parentdeal="Impegno";
$l_kind = "Tipo";
$l_contacts = "Contatti";
$l_marketing_manager = "Resp. Marketing";
$l_technical_manager = "Resp. Tecnico ";
$l_amount = "Totale";
$l_hitrate = "Hit Rate";
$l_expected = "Objetivo";
$l_quarter = "Q";
$l_date_comment = "Data Commento";

// Labels and texts
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordine";
$l_amount_in = "Totale entrate";
$l_amount_out = "Totale uscite";
$l_potential = "Potenziale pesato";
$l_label_start = "Nome inizio";
$l_proposal = "Proposta";
$l_show_archive = "Mostra archivi";
$l_deal_select_company = "Scegli l'azienda relativa al impegno";
$l_deal_select_parentdeal = "Scegli il Sub-impegno al quale l'impegno deve essere collegato";
$l_affect_deal= "Cambia Sub-Impegno ";
// about invoice...
$l_no_invoices = "Non sono presenti fatture collegate";
$l_kind_manage = "Gestione Tipo Impegno ";
$l_kind_exist = "Tipo esistenti";
$l_kind_new = "Nuovo Tipo";
$l_status_manage = "Gestione stato Impegno ";
$l_status_exist = "Stati esistenti";
$l_status_new = "Nuovo Stato";
$l_parentdeal_deals = "Impegni appartengono a questo impegno";

// Actions
$l_insert = "Inserisci Impegno";
$l_update = "Modifica  Impegno";
$l_delete = "Elimina Impegno";
$l_select_company = "Scegli azienda";
$l_insert_parent = "Inserisci Impegno";
$l_update_parent = "Modifica Impegno";
$l_delete_parent = "Elimina Impegno";
$l_affect = "Affect";

$l_link_contract = "L'affare è collegato a questi contratti";
$l_link_contract_no = "L'affare non è collegato a nessun contratto";
$l_link_project = "L'affare è collegato a questi progetti";
$l_link_project_no = "L'affare è non collegato a nessun progetti";
$l_link_invoice = "L'affare è collegato a queste fatture";
$l_link_invoice_no = "L'affare è non collegato a nessun fatture";
$l_cant_delete = "L'affare non può essere eliminato";
$l_can_delete = "L'affare può essere eliminato";

$l_kind_checkdelete = "Controlla o Elimina Tipo";
$l_kind_delete = "Elimina Tipo";
$l_kind_can_delete = "Tipo può essere eliminato!";
$l_kind_cant_delete = "Tipo non può essere eliminato!";
$l_kind_link_deal = "Tipo è richiesto in questi Impegni";
$l_kind_link_deal_no = "Tipo no è richiesto in Impegni";
$l_kind_update = "Modifica Tipo";
$l_kind_insert = "Inserisci Tipo";
$l_status_update = "Modifica State";
$l_status_checkdelete = "Controlla o Elimina lo Stato";
$l_status_delete = "Elimina Stato";
$l_status_can_delete = "Stato può essere eliminato";
$l_status_cant_delete = "Stato non può essere eliminato";
$l_status_link_deal = "Stato necessario in questi Impegni";
$l_status_link_deal_no = "Stato no è richiesto in Impegni";
$l_status_insert = "Inserisci Stato";

// Messages
$l_err_parent_has_deal = "L'Impegno ne ha altri collegati, non può essere eliminato!";
$l_err_label_empty = "Scrivi nome impegno!";
$l_err_kind_empty = "Scrivi impegno tipo!";
$l_err_task_empty = "Scrivi impegno categoria!";
$l_err_market_empty = "Scrivi responsabile marketing!";
$l_err_tech_empty = "Scrivi responsabile tecnico!";
$l_err_hitrate = "Hit rate deve essere un valore da 0 a 100";

$l_updateaffect_ok = "Impegno collegato all'impegno";
$l_no_display = "Scrivi nei campi e click su < TROVA > per la ricerca";
$l_found = "Impegni trovati.";
$l_no_found = "Non sono stati trovati impegni!";
$l_found_parent = "Sub-Impegni trovati";
$l_no_display_parent ="Scrivi nei campi click su   <TROVA> per la ricerca";
$l_no_found_parent ="Nessun impegno trovato!";

// JavaScript Messages (characters like \ have to be backslashed -> \\)
$l_fill_parentdeal = "Inserisci prima un Sub-impegno.";
$l_fill_label = "Scrivi prima nome.";
$l_fill_number = "Scrivi prima numero.";
$l_fill_datealarm = "Scrivi prima Data Avviso.";
$l_fill_datebegin = "Scrivi prima Data inizio.";
$l_confirm_datealarm = "Data Avviso e passata ! Valida ?";
$l_j_select_company = "Scegli una azienda!";
$l_invalid_date = "Formato data non valido (MM/GG/AAAA or AAAA-MM-GG).";
$l_delete_confirm = "Confermi impegno Eliminazione ?";
$l_j_kind_delete = "Inserisci un Tipo per controllo o eliminazione!";
$l_j_status_delete = "Inserisci uno Stato per controllo o eliminazione!";
$l_j_kind_update = "Inserisci un tipo e scrivi un nuovo nome per la modifica!";
$l_j_status_update = "Inserisci uno Stato e scrivi un nuovo nome per la modifica!";
$l_j_kind_create = "Inserisci il nome per un nuovo tipo per crearlo!";
$l_j_status_create = "Inserisci il nome per un nuovo Stato per crearlo!";

// For display parameters modification screen
$l_deal_display = "Visualizza opzioni per la lista di Impegni";
$l_parentdeal_display = "Visualizza opzioni per la lista di Impegni";
$l_invoice_display = "Visualizza opzioni per la lista di fatture";
