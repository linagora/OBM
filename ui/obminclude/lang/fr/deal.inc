<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : deal.inc                                                 //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//for list
$l_task_fac = "Taches Facturées";
$l_task_int = "Taches Internes";

// Database fields
$l_number = "Numéro";
$l_label = "Label";
$l_date_init = "Date Debut";
$l_parentdeal = "SurAffaire";
$l_kind = "Type";
$l_category = "Catégorie";
$l_company = "Nom Société";
$l_contacts = "Contacts";
$l_marketing_manager = "Resp Commercial";
$l_technical_manager = "Resp Technique";
$l_date_proposal = "Date de proposition";
$l_amount = "Montant";
$l_state = "Etat";
$l_date_alarm = "Date Alarme";
$l_comment = "Commentaire";
$l_todo = "A faire";
$l_visibility ="Accès";
$l_invoice_deal = "Affaire";
$l_archived_invoices_connected = " Facture(s) archivée(s) connectée(s) à cette Affaire";
$l_see_archived_invoices = "Voir aussi les factures archivées";
// Labels et textes
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordre";
$l_amount_in = "Montant des entrées";
$l_amount_out = "Montant des sorties";
$l_label_start = "Nom Affaire";
$l_manager= "Responsable";
$l_proposition = "Proposition";
$l_show_archive = "Voir les archives";
$l_archive_first = "Arc";
$l_date_alarm_after = "Date Alarme Après";
$l_date_alarm_before = "Date Alarme Avant";
$l_after = "Date Alarme Après";
$l_before = "Date Alarme Avant";
$l_last_update = "Dernière modification le";
$l_at = "à";
$l_deal_select_company = "Sélectionner la société avec laquelle l'affaire doit être renseignée.";
$l_deal_select_parentdeal = "Sélectionner la surAffaire à laquelle l'affaire doit être affectée";
$l_private = "Privé";
$l_public = "Publique";
$l_affect_deal = "Affecter à une surAffaire";
$l_invoices_connected = "Facture(s) connectée(s) à cette Affaire";
$l_no_invoices_connected = "Aucune Facture connectée à cette Affaire";
$l_create_invoice = "Créer une facture liée à cette affaire";
$l_kind_manage = "Gestion des Types d'Affaires";
$l_kind_exist = "Types existants";
$l_kind_new = "Nouveau Type";
$l_status_manage = "Gestion des Etats d'Affaires";
$l_status_exist = "Etats existants";
$l_status_new = "Nouvel Etat";
$l_category_manage = "Gestion des Catégories d'Affaires";
$l_category_exist = "Catégories existantes";
$l_category_new = "Nouvelle Catégorie";
$l_category_label = "Label";
$l_category_minilabel = "Mini Label";
$l_invoice_number = "Numéro";
$l_invoice_label = "Label";
$l_invoice_HT = "Montant HT";
$l_invoice_TTC = "Montant TTC";
$l_invoice_status = "Etat";
$l_invoice_date = "Date";
$l_add_contrat = "Ajouter un contrat lié à cette affaire";
$l_contract_list = "Liste des contrats lié à cette affaire";
//----------------------------------------------------------//
// Fields that appear in result lists (used by OBM_DISPLAY) //
//----------------------------------------------------------//
// Direct fields
$fieldnames["deal_label"] = $l_label;
$fieldnames["deal_datealarm"] = $l_date_alarm;
$fieldnames["deal_archive"] = $l_archive_first;
$fieldnames["deal_todo"] = $l_todo;
$fieldnames["parentdeal_label"] = $l_label;
$fieldnames["parentdeal_comment"] = $l_comment;
// Calculated or indirect fields
$fieldnames["dealstatus_label"] = $l_state;
$fieldnames["dealtype_label"] = $l_kind;
$fieldnames["tasktype_label"] = $l_category;
$fieldnames["deal_company_name"] = $l_company;
$fieldnames["parentdeal_marketing_lastname"] = $l_marketing_manager;
$fieldnames["parentdeal_technical_lastname"] = $l_technical_manager;
$fieldnames["parentdeal_status"] = $l_state;
$fieldnames["parentdeal_dateproposal"] = $l_date_proposal;
$fieldnames["parentdeal_datealarm"] = $l_date_alarm;
$fieldnames["parentdeal_amountprovider"] = $l_amount_out;
$fieldnames["parentdeal_amountcustomer"] = $l_amount_in;
// fields linked to invoices
$fieldnames["invoice_number"] = $l_invoice_number;
$fieldnames["invoice_date"] = $l_invoice_date;
$fieldnames["invoice_label"] = $l_invoice_label;
$fieldnames["invoice_amount_HT"] = $l_invoice_HT;
$fieldnames["invoice_amount_TTC"] = $l_invoice_TTC;
$fieldnames["invoice_status"] = $l_invoice_status;
$fieldnames["invoice_deal"] = $l_invoice_deal;
$fieldnames["invoice_company"] = $l_company;

// Titres
$l_deal = "Affaire";
$l_parentdeal_deals = "Affaires appartenant a la SurAffaire";

// Actions
$l_select_deal = "Sélectionner l'Affaire";
$l_insert = "Enregistrer l'Affaire";
$l_update = "Modifier l'Affaire";
$l_delete = "Supprimer l'Affaire";
$l_archive_update = "Mise à jour Archives";
$l_select_company = "Sélectionner la société";
$l_affect = "Affecter";

$l_insert_parent = "Enregistrer SurAffaire";
$l_update_parent = "Modifier SurAffaire";
$l_delete_parent = "Supprimer SurAffaire";
$l_parentdeal_deal_new = "Créer nouvelle affaire";

$l_kind_checkdelete = "Vérifier ou Supprimer le Type";
$l_kind_delete = "Supprimer le Type";
$l_kind_can_delete = "Le type peut être supprimé";
$l_kind_cant_delete = "Le type ne peut être supprimé";
$l_kind_link_deal = "Le type est référencé par ces affaires";
$l_kind_update = "Modifier le Type";
$l_kind_insert = "Créer le Type";
$l_status_checkdelete = "Vérifier ou Supprimer l'état";
$l_status_delete = "Supprimer l'état";
$l_status_can_delete = "L'état peut être supprimé";
$l_status_cant_delete = "L'état ne peut être supprimé";
$l_status_link_deal = "L'état est référencé par ces affaires";
$l_status_update = "Modifier l'état";
$l_status_insert = "Créer l'état";
$l_cat_checkdelete = "Vérifier ou Supprimer la catégorie";
$l_cat_delete = "Supprimer la catégorie";
$l_cat_can_delete = "La catégorie peut être supprimée";
$l_cat_cant_delete = "La catégorie ne peut être supprimée";
$l_cat_link_deal = "La catégorie est référencée par ces affaires";
$l_cat_update = "Modifier la catégorie";
$l_cat_insert = "Créer la catégorie";

// Messages
$l_err_parent_has_deal = "Cette suraffaire contient des affaires, elle ne peut être supprimée !";
$l_err_label_empty = "Le label doit être renseigné !";
$l_err_kind_empty = "Le type d'affaire doit être renseigné !";
$l_err_task_empty = "La catégorie d'affaire doit être renseignée !";
$l_err_market_empty = "Le responsable commercial doit être renseigné !";
$l_err_tech_empty = "Le responsable technique doit être renseigné !";
$l_updateaffect_ok = "L'affaire a bien été affectée a une surAffaire.";
$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_error_visibility = "Les informations contenues dans cette page sont privées. Vous ne pouvez pas les consulter";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des affaires correspondantes.";
$l_found = "affaire(s) trouvée(s).";
$l_no_found = "Aucune affaire ne correspond à ces critères !";
$l_delete_ok = "La suppression de l'affaire a été correctement effectuée.";
$l_delete_error = "La suppression de l'affaire a échoué !";
$l_insert_ok = "L'insertion de l'affaire a été correctement effectuée.";
$l_insert_error = "L'insertion de l'affaire a échoué !";
$l_update_ok = "La modification de l'affaire a été correctement effectuée";
$l_update_error = "La modification de l'affaire a échoué !";
$l_query_error = "Problème en exécutant la requête";
$l_archive_ok = "La mise à jour des archives s'est bien effectuée.";
$l_archive_error = "Erreur lors de l'archivage de l'affaire de code ";
$l_archive_number = "affaires enregistrées dans les archives.";
$l_kind_insert_ok = "L'insertion du type a été correctement effectuée.";
$l_kind_insert_error = "L'insertion du type a échoué !";
$l_kind_update_ok = "La modification du type a été correctement effectuée";
$l_kind_update_error = "La modification du type a échoué !";
$l_kind_delete_error = "La suppression du type a échoué !";
$l_kind_delete_ok = "La suppression du type a été correctement effectuée.";
$l_status_insert_ok = "L'insertion de l'état a été correctement effectuée.";
$l_status_insert_error = "L'insertion de l'état a échoué !";
$l_status_update_ok = "La modification de l'état a été correctement effectuée";
$l_status_update_error = "La modification de l'état a échoué !";
$l_status_delete_error = "La suppression de l'état a échoué !";
$l_status_delete_ok = "La suppression de l'état a été correctement effectuée.";
$l_cat_insert_ok = "L'insertion de la catégorie a été correctement effectuée.";
$l_cat_insert_error = "L'insertion de la catégorie a échoué !";
$l_cat_update_ok = "La modification de la catégorie a été correctement effectuée";
$l_cat_update_error = "La modification de la catégorie a échoué !";
$l_cat_delete_ok = "La suppression de la catégorie a été correctement effectuée !";
$l_cat_delete_error = "La suppression de la catégorie a échoué !";
$l_found_parent = "SurAffaires trouvees";
$l_no_found_parent = "Aucune SurAffaire ne correspond à ces critères !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_parentdeal = "Vous devez sélectionner une surAffaire avant de valider.";
$l_fill_label = "Vous devez renseigner le Label avant de valider.";
$l_fill_number = "Vous devez renseigner le Numero avant de valider.";
$l_fill_datealarm = "Vous devez renseigner la date alarme avant de valider.";
$l_j_select_company = "Vous devez sélectionner une société !";
$l_invalid_date = "Format de date invalide (JJ/MM/AAAA ou AAAA-MM-JJ).";
$l_delete_confirm = "Confirmer la suppression de l'affaire ?";

$l_j_kind_delete = "Vous devez sélectionner un type pour vérifier ou supprimer !";
$l_j_status_delete = "Vous devez sélectionner un état pour le vérifier ou supprimer !";
$l_j_category_delete = "Vous devez sélectionner une catégorie pour la vérifier ou supprimer !";
$l_j_kind_update = "Vous devez sélectionner un type et entrer une modification avant de valider !";
$l_j_status_update = "Vous devez sélectionner un état et entrer une modification avant de valider !";
$l_j_category_update = "Vous devez sélectionner une catégorie et entrer une modification avant de valider !";
$l_j_kind_create = "Vous devez saisir un nom de type pour le créer !";
$l_j_status_create = "Vous devez saisir un nom d'état pour le créer !";
$l_j_category_create = "Vous devez saisir un nom de catégorie pour le créer !";


// For the display preferences modification screen 
$l_deal_display = "Options d'affichage des listes d'affaires";
$l_parentdeal_display = "Options d'affichage des listes de suraffaires";
$l_invoice_display = "Options d'affichage des listes de factures";
