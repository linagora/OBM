<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : deal.inc                                                 //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

//for list
//$l_task_fac = "Taches Facturées";
//$l_task_int = "Taches Internes";

// Database fields
$l_number = "Numéro";
$l_label = "Label";
$l_date_init = "Date Debut";
$l_parentdeal = "SurAffaire";
$l_kind = "Type";
$l_category = "Catégorie";
$l_company = "Société";
$l_contact = "Contact";
$l_marketing_manager = "Resp Commercial";
$l_technical_manager = "Resp Technique";
$l_date_proposal = "Date de proposition";
$l_amount = "Montant";
$l_hitrate = "% Réussite";
$l_state = "Etat";
$l_date_alarm = "Date Alarme";
$l_comment = "Commentaire";
$l_add_comment = "Ajouter un commentaire";
$l_upd_comment = "Modifier le commentaire";
$l_todo = "A faire";
$l_visibility ="Accès";
$l_project ="Projet";

// Labels et textes
$l_private = "Privé";
$l_public = "Publique";
$l_infos = "Informations";
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordre";
$l_amount_in = "Montant des entrées";
$l_amount_out = "Montant des sorties";
$l_potential = "Potentiel pondéré";
$l_label_start = "Nom Affaire";
$l_manager= "Responsable";
$l_proposition = "Proposition";
$l_show_archive = "Voir les archives";
$l_date_alarm_after = "Date Alarme Après";
$l_date_alarm_before = "Date Alarme Avant";
$l_zipcode = "Code Postal";
$l_after = "Date Alarme Après";
$l_before = "Date Alarme Avant";
$l_last_update = "Dernière modification le";
$l_at = "à";
$l_deal_select_company = "Sélectionner la société avec laquelle l'affaire doit être renseignée.";
$l_deal_select_parentdeal = "Sélectionner la surAffaire à laquelle l'affaire doit être affectée";
$l_affect_deal = "Affecter à une surAffaire";
$l_create_project = "Créer le projet associé";
$l_create_short_project = "Création simplifiée";
$l_view_project = "Voir le projet associé";
$l_invoice_new = "Nouvelle facture";
$l_no_invoice = "Pas de factures";
$l_archived_invoices = "Factures archivées";
$l_see_archived_invoices = "Voir aussi les factures archivées";
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
$l_contract_new = "Nouveau contrat";

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

$l_kind_checkdelete = "Vérifier ou Supprimer le Type";
$l_kind_delete = "Supprimer le Type";
$l_kind_update = "Modifier le Type";
$l_kind_insert = "Créer le Type";
$l_status_checkdelete = "Vérifier ou Supprimer l'état";
$l_status_delete = "Supprimer l'état";
$l_status_update = "Modifier l'état";
$l_status_insert = "Créer l'état";
$l_cat_checkdelete = "Vérifier ou Supprimer la catégorie";
$l_cat_delete = "Supprimer la catégorie";
$l_cat_update = "Modifier la catégorie";
$l_cat_insert = "Créer la catégorie";

// Messages
$l_link_contract = "L'affaire est référencée par ces contrats";
$l_link_contract_no = "L'affaire n'est pas référencée dans les contrats";
$l_cant_delete = "L'affaire ne peut être supprimée !";
$l_can_delete = "L'affaire peut être supprimée.";

$l_err_parent_has_deal = "Cette suraffaire contient des affaires, elle ne peut être supprimée !";
$l_err_label_empty = "Le label doit être renseigné !";
$l_err_kind_empty = "Le type d'affaire doit être renseigné !";
$l_err_task_empty = "La catégorie d'affaire doit être renseignée !";
$l_err_market_empty = "Le responsable commercial doit être renseigné !";
$l_err_tech_empty = "Le responsable technique doit être renseigné !";
$l_err_hitrate = "Le pourcentage de réussite doit être entre 0 et 100 !";
$l_updateaffect_ok = "L'affaire a bien été affectée a une surAffaire.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des affaires correspondantes.";
$l_found = "affaire(s) trouvée(s).";
$l_no_found = "Aucune affaire ne correspond à ces critères !";
$l_found_parent = "SurAffaires trouvées";
$l_no_found_parent = "Aucune SurAffaire ne correspond à ces critères !";

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
$l_kind_can_delete = "Le type peut être supprimé";
$l_kind_cant_delete = "Le type ne peut être supprimé";
$l_kind_link_deal = "Le type est référencé par ces affaires";
$l_kind_link_deal_no = "Le type n'est pas référencé dans les affaires";
$l_status_insert_ok = "L'insertion de l'état a été correctement effectuée.";
$l_status_insert_error = "L'insertion de l'état a échoué !";
$l_status_update_ok = "La modification de l'état a été correctement effectuée";
$l_status_update_error = "La modification de l'état a échoué !";
$l_status_delete_error = "La suppression de l'état a échoué !";
$l_status_delete_ok = "La suppression de l'état a été correctement effectuée.";
$l_status_can_delete = "L'état peut être supprimé";
$l_status_cant_delete = "L'état ne peut être supprimé";
$l_status_link_deal = "L'état est référencé par ces affaires";
$l_status_link_deal_no = "L'état n'est pas référencé dans les affaires";
$l_cat_insert_ok = "L'insertion de la catégorie a été correctement effectuée.";
$l_cat_insert_error = "L'insertion de la catégorie a échoué !";
$l_cat_update_ok = "La modification de la catégorie a été correctement effectuée";
$l_cat_update_error = "La modification de la catégorie a échoué !";
$l_cat_delete_ok = "La suppression de la catégorie a été correctement effectuée !";
$l_cat_delete_error = "La suppression de la catégorie a échoué !";
$l_cat_can_delete = "La catégorie peut être supprimée";
$l_cat_cant_delete = "La catégorie ne peut être supprimée";
$l_cat_link_deal = "La catégorie est référencée par ces affaires";
$l_cat_link_deal_no = "La catégorie n'est pas référencée dans les affaires";
$l_cat_link_project = "La catégorie est référencée par ces projets";
$l_cat_link_project_no = "La catégorie n'est pas référencée dans les projets";
$l_cat_link_timetask = "La catégorie est utilisée dans la gestion des temps";
$l_cat_link_timetask_no = "La catégorie n'est pas utilisée dans la gestion des temps";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_parentdeal = "Vous devez sélectionner une surAffaire avant de valider.";
$l_fill_label = "Vous devez renseigner le Label avant de valider.";
$l_fill_number = "Vous devez renseigner le Numero avant de valider.";
$l_fill_datealarm = "Vous devez renseigner la date alarme avant de valider.";
$l_fill_datebegin = "Vous devez renseigner la date de début avant de valider.";
$l_confirm_datealarm = "La date Alarme est dans le passé ! Confirmer l'action ?";
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
