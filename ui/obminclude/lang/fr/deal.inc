<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : deal.inc                                                 //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numéro";
$l_label = "Label";
$l_date_init = "Date Debut";
$l_parentdeal = "SurAffaire";
$l_marketing_manager = "Commercial";
$l_technical_manager = "Technique";
$l_amount = "Montant";
$l_margin = "Marge";
$l_hitrate = "% Réussite";
$l_commission = "Commission";
$l_commission_p = "% Commission";
$l_commission_balanced = "Commission pondérée";
$l_proposal = "Proposition";
$l_expected = "Prévue";
$l_quarter = "T";
$l_sale_delay = "Cycle de vente";
$l_date_comment = "Date Commentaire";
$l_todo = "A faire";
$l_category1 = "Catégorie";
$l_code = "Code";
$l_role = "Rôle";

// Labels et textes
$l_other_relation = "Autres relations";
$l_infos = "Dates et montants";
$l_minus = "-";
$l_plus = "+";
$l_order = "Ordre";
$l_amount_in = "Montant des entrées";
$l_amount_out = "Montant des sorties";
$l_potential_balanced = "Potentiel pondéré";
$l_label_start = "Nom Affaire";
$l_deal_select_company = "Sélectionner la société avec laquelle l'affaire doit être renseignée.";
$l_deal_select_parentdeal = "Sélectionner la surAffaire à laquelle l'affaire doit être affectée";
$l_affect_deal = "Affecter à une surAffaire";
$l_no_invoice = "Pas de factures";
$l_type_manage = "Gestion des Types d'Affaires";
$l_type_exist = "Types existants";
$l_type_new = "Nouveau Type";
$l_type_label = "Type";
$l_status_manage = "Gestion des Etats d'Affaires";
$l_status_exist = "Etats existants";
$l_status_new = "Nouvel Etat";
$l_invoice_number = "Numéro";
$l_invoice_label = "Label";
$l_invoice_ht = "Montant HT";
$l_invoice_ttc = "Montant TTC";
$l_invoice_status = "Etat";
$l_invoice_date = "Date";
$l_parentdeal_deals = "Affaires appartenant à la SurAffaire";
$l_forecast = "Prévisionnel";
$l_deal_panel = "Volant d'affaires";

// Actions
$l_select_deal = "Sélectionner l'Affaire";
$l_insert = "Enregistrer l'Affaire";
$l_update = "Modifier l'Affaire";
$l_delete = "Supprimer l'Affaire";
$l_select_company = "Sélectionner la société";
$l_affect = "Affecter";

$l_insert_parent = "Enregistrer SurAffaire";
$l_update_parent = "Modifier SurAffaire";
$l_delete_parent = "Supprimer SurAffaire";

$l_type_checkdelete = "Vérifier ou Supprimer le Type";
$l_type_delete = "Supprimer le Type";
$l_type_update = "Modifier le Type";
$l_type_insert = "Créer le Type";
$l_status_checkdelete = "Vérifier ou Supprimer l'état";
$l_status_delete = "Supprimer l'état";
$l_status_update = "Modifier l'état";
$l_status_insert = "Créer l'état";
$l_add_cats = "Ajouter ces categories";

// Messages
$l_link_contract = "L'affaire est référencée par des contrats";
$l_link_contract_no = "L'affaire n'est pas référencée dans les contrats";
$l_link_project = "L'affaire est référencée par des projets";
$l_link_project_no = "L'affaire n'est pas référencée dans les projets";
$l_link_invoice = "L'affaire est référencée par des factures";
$l_link_invoice_no = "L'affaire n'est pas référencée dans les factures";
$l_can_delete = "L'affaire peut être supprimée.";
$l_cant_delete = "L'affaire ne peut être supprimée !";

$l_link_deal = "La suraffaire possède des affaires";
$l_cant_delete_parent = "La suraffaire ne peut être supprimée !";

$l_err_label_empty = "Le label doit être renseigné !";
$l_err_type_empty = "Le type d'affaire doit être renseigné !";
$l_err_task_empty = "Le type de tâche de l'affaire doit être renseigné !";
$l_err_market_empty = "Le responsable commercial doit être renseigné !";
$l_err_tech_empty = "Le responsable technique doit être renseigné !";
$l_err_hitrate = "Le pourcentage de réussite doit être entre 0 et 100 !";
$l_updateaffect_ok = "L'affaire a bien été affectée a une surAffaire.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des affaires correspondantes.";
$l_found = "affaire(s) trouvée(s).";
$l_no_found = "Aucune affaire ne correspond à ces critères !";
$l_found_parent = "SurAffaires trouvées";
$l_no_found_parent = "Aucune SurAffaire ne correspond à ces critères !";

$l_type_can_delete = "Le type peut être supprimé";
$l_type_cant_delete = "Le type ne peut être supprimé";
$l_type_link_deal = "Le type est référencé par ces affaires";
$l_type_link_deal_no = "Le type n'est pas référencé dans les affaires";
$l_status_can_delete = "L'état peut être supprimé";
$l_status_cant_delete = "L'état ne peut être supprimé";
$l_status_link_deal = "L'état est référencé par ces affaires";
$l_status_link_deal_no = "L'état n'est pas référencé dans les affaires";

$l_help_deal_forecast = "Le prévisionnel regroupe les affaires répondant aux critères : affaire non archivée, avec une estimation de montant et un % de réussite > 0 et < 100.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_parentdeal = "Vous devez sélectionner une surAffaire avant de valider.";
$l_fill_label = "Vous devez renseigner le Label avant de valider.";
$l_fill_number = "Vous devez renseigner le Numero avant de valider.";
$l_fill_datealarm = "Vous devez renseigner la date alarme avant de valider.";
$l_fill_datebegin = "Vous devez renseigner la date de début avant de valider.";
$l_confirm_datealarm = "La date Alarme est dans le passé ! Confirmer l'action ?";
$l_invalid_date = "Format de date invalide (JJ/MM/AAAA ou AAAA-MM-JJ).";
$l_delete_confirm = "Confirmer la suppression de l'affaire ?";

$l_j_type_create = "Vous devez saisir un nom de type pour le créer !";
$l_j_type_update = "Vous devez sélectionner un type et entrer une modification avant de valider !";
$l_j_type_delete = "Vous devez sélectionner un type pour vérifier ou supprimer !";
$l_j_status_create = "Vous devez saisir un nom d'état pour le créer !";
$l_j_status_update = "Vous devez sélectionner un état et entrer une modification avant de valider !";
$l_j_status_delete = "Vous devez sélectionner un état pour le vérifier ou supprimer !";

// For the display preferences modification screen 
$l_deal_display = "Options d'affichage des listes d'affaires";
$l_parentdeal_display = "Options d'affichage des listes de suraffaires";
$l_invoice_display = "Options d'affichage des listes de factures";
