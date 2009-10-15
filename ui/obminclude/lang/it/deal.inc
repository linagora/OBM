<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Italiano                                                 //
//     - File     : deal.inc                                                 //
// 2002-08-02 Augusto Fagioli - augusto@fagioli.biz                          //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-11T17:44:00+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T10:04:52+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T10:05:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-17T10:07:56+02:00 using OBMtranslation (0.4.1)

$l_date_init = "Data Inizio"; 
$l_parentdeal = "Sotto-Affare"; 
$l_contacts = "Contatti";
$l_marketing_manager = "Respondabile Commerciale"; 
$l_technical_manager = "Resp. Tecnico "; 
$l_amount = "Importo"; 
$l_margin = "Margine"; 
$l_hitrate = "% di Successo"; 
$l_expected = "Obiettivo"; 
$l_quarter = "Q";
$l_date_comment = "Data Commento"; 
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordine";
$l_amount_in = "Totale entrate";
$l_amount_out = "Totale uscite"; 
$l_potential = "Potenziale pesato";
$l_label_start = "Nome Affare"; 
$l_proposal = "Proposta";
$l_deal_select_company = "Scegli l'azienda relativa all'affare."; 
$l_deal_select_parentdeal = "Scegli il Sotto-Affare a cui è associato l'affare."; 
$l_affect_deal = "Assegna Sotto-Affare"; 
$l_no_invoices = "Non sono presenti fatture collegate";
$l_type_manage = "Gestione Tipo Affare "; 
$l_type_exist = "Tipi esistenti"; 
$l_type_new = "Nuovo Tipo";
$l_status_manage = "Gestione Stato Affari"; 
$l_status_exist = "Stati esistenti";
$l_status_new = "Nuovo Stato";
$l_parentdeal_deals = "Affari appartenenti al Sotto-Affare"; 
$l_insert = "Inserisci Affare"; 
$l_update = "Modifica Affare"; 
$l_delete = "Elimina Affare"; 
$l_select_company = "Scegli azienda";
$l_insert_parent = "Inserisci Sotto-Affare"; 
$l_update_parent = "Modifica Sotto-Affare"; 
$l_delete_parent = "Elimina Sotto-Affare"; 
$l_affect = "Associare"; 
$l_link_contract = "L'affare è associato a questi contratti"; 
$l_link_contract_no = "L'affare non è associato a nessun contratto"; 
$l_link_project = "L'affare è associato a questi progetti"; 
$l_link_project_no = "L'affare non è associato a nessun progetto"; 
$l_link_invoice = "L'affare è associato a queste fatture"; 
$l_link_invoice_no = "L'affare non è associato a nessuna fattura"; 
$l_cant_delete = "L'affare non può essere eliminato!"; 
$l_can_delete = "L'affare può essere eliminato"; 
$l_type_checkdelete = "Controlla o Elimina Tipo";
$l_type_delete = "Elimina Tipo";
$l_type_can_delete = "Il Tipo può essere eliminato!"; 
$l_type_cant_delete = "Il Tipo non può essere eliminato!"; 
$l_type_link_deal = "Il Tipo è richiesto da questi affari"; 
$l_type_link_deal_no = "Il tipo non è associato agli affari"; 
$l_type_update = "Modifica Tipo";
$l_type_insert = "Inserisci Tipo";
$l_status_update = "Modifica Stato"; 
$l_status_checkdelete = "Controlla o Elimina lo Stato"; 
$l_status_delete = "Elimina Stato";
$l_status_can_delete = "Lo Stato può essere eliminato"; 
$l_status_cant_delete = "Lo Stato non può essere eliminato"; 
$l_status_link_deal = "Lo stato è collegato a questi affari"; 
$l_status_link_deal_no = "Lo stato non è associato agli affari"; 
$l_status_insert = "Inserisci Stato"; 
$l_err_parent_has_deal = "L'Impegno ne ha affari collegati, non può essere eliminato!";
$l_err_label_empty = "Inserisci l'etichetta!"; 
$l_err_type_empty = "Inserisci il tipo di affare!"; 
$l_err_task_empty = "Inserisci il tipo di operazione dell'affare!"; 
$l_err_market_empty = "Inserisci il responsabile commerciale!"; 
$l_err_tech_empty = "Inserisci il responsabile tecnico!"; 
$l_err_hitrate = "La percentuale deve essere tra 0 e 100!"; 
$l_updateaffect_ok = "L'affare è associato a un Sotto-Affare."; 
$l_no_display = "Riemi i campi di ricerca e premi  < Cerca > per ottenere l'elenco degli affari."; 
$l_found = "Affare(i) trovato(i)."; 
$l_no_found = "Non sono stati trovati affari!"; 
$l_found_parent = "Sotto-Affari trovati"; 
$l_no_display_parent ="Riempi i campi e premi <TROVA> per effettuare la ricerca";
$l_no_found_parent = "Nessun Sotto-Affare trovato!"; 
$l_fill_parentdeal = "Seleziona un Sotto-Affare prima di validare."; 
$l_fill_label = "Scrivi prima l'Etichetta."; 
$l_fill_number = "Inserisci il Numero prima di validare."; 
$l_fill_datealarm = "Scrivi prima Data Avviso."; 
$l_fill_datebegin = "Inserisci la data prima di validare."; 
$l_confirm_datealarm = "La Data Avviso è passata ! Confermi azione?"; 
$l_invalid_date = "Formato data non valido (GG/MM/AAAA or AAAA-MM-GG)."; 
$l_delete_confirm = "Confermi eliminazione affare?"; 
$l_j_type_delete = "Inserisci un Tipo da verificare o eliminare!"; 
$l_j_status_delete = "Inserisci uno Stato da verificare o eliminare!"; 
$l_j_type_update = "Inserisci un tipo ed aggiorna prima di validare!"; 
$l_j_status_update = "Inserisci uno Stato ed aggiorna prima di validare!"; 
$l_j_type_create = "Inserisci un nome per il tipo da creare!"; 
$l_j_status_create = "Inserisci il nome per uno stato da creare!"; 
$l_deal_display = "Opzioni visualizzazione dell'elenco Affari"; 
$l_parentdeal_display = "Opzioni visualizzazione dell'elenco Sotto-Affari"; 
$l_invoice_display = "Opzioni visualizzazione dell'elenco fatture"; 
$l_invoice_ttc = "Importo lordo"; 
$l_role = "Ruolo"; 
$l_cant_delete_parent = "Il sotto-affare non può essere eliminato!"; 
$l_code = "Codice "; 
$l_invoice_number = "Numero"; 
$l_todo = "Da fare"; 
$l_potential_balanced = "Potenziale ponderato"; 
$l_commission_balanced = "Commissione ponderata"; 
$l_category1 = "Categoria"; 
$l_sale_delay = "Ciclo di vendita"; 
$l_number = "Numero"; 
$l_invoice_date = "Data"; 
$l_type_label = "Tipo"; 
$l_no_invoice = "Nessuna fattura"; 
$l_commission_p = "% Commissione"; 
$l_commission = "Commissione"; 
$l_deal_panel = "Margine d'affare"; 
$l_link_deal = "Il Sotto-Affare ha degli affari"; 
$l_label = "Etichetta"; 
$l_invoice_ht = "Importo netto"; 
$l_other_relation = "Altri rapporti"; 
$l_forecast = "Preventivo"; 
$l_add_cats = "Aggiungi queste categorie"; 
$l_invoice_status = "Stato"; 
$l_select_deal = "Seleziona Affare"; 
$l_infos = "Date ed Importi"; 
$l_invoice_label = "Etichetta"; 
$l_help_deal_forecast = "Il preventivo raggruppa gli affari chi rispondono a questi criteri: affare non archiviato, con un importo valutato ad una % di successo > 0 e < 100."; 

