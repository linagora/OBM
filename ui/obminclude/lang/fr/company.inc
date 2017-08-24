<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : company.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numéro";
$l_company_name = "Nom Société";
$l_type = "Type";
$l_activity = "Secteur d'activité";
$l_nafcode = "Code NAF";
$l_market = "Responsable du compte";
$l_web = "W";
$l_email = "@M";
$l_coord = "Coordonnées";
$l_vat = "Numéro Intracommunautaire";
$l_siret = "SIRET";

// Labels et textes
$l_comment = "Commentaire";
$l_contact_new = "Nouveau Contact";
$l_deal_new = "Nouvelle Affaire";
$l_project_new = "Nouveau Projet";
$l_contract_new = "Nouveau Contrat";
$l_internal = "Interne";
$l_naf_manage = "Gestion des Codes NAF";
$l_naf_exist = "Codes NAF existants";
$l_naf_new = "Nouveau Code NAF";
$l_naf_label = "Label";
$l_naf_code = "Code";
$l_naf_istitle = "Est un titre";

// Actions
$l_add = "Ajouter";
$l_insert = "Enregistrer la Société";
$l_update = "Modifier la Société";
$l_checkdelete = "Vérifier ou Supprimer la Société";
$l_delete = "Supprimer la Société";
$l_naf_update = "Modifier le Code NAF";
$l_naf_checkdelete = "Vérifier ou Supprimer le Code NAF";
$l_naf_delete = "Supprimer le Code NAF";
$l_naf_insert = "Créer le Code NAF";
$l_select_company = "Sélectionner la société";

// Messages
$l_err_act_empty = "Le secteur d'activité doit être renseigné !";

$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des sociétés correspondantes.";
$l_found = "sociétés trouvées.";
$l_no_found = "Aucune société ne correspond à ces critères !";
$l_check_samecompany = "Ces sociétés semblent similaires à cette nouvelle entrée !";
$l_link_contact = "La société possède des contacts";
$l_link_contact_no = "La société n'a pas de contact";
$l_link_deal = "La société est référencée par des affaires";
$l_link_deal_no = "La société n'est pas référencée dans les affaires";
$l_link_contract = "La société est référencée par des contrats";
$l_link_contract_no = "La société n'est pas référencée dans les contrats";
$l_link_project = "La société est référencée par des projets";
$l_link_project_no = "La société n'est pas référencée dans les projets";
$l_link_invoice = "La société est référencée par des factures";
$l_link_invoice_no = "La société n'est pas référencée dans les factures";
$l_cant_delete = "La société ne peut être supprimée !";
$l_can_delete = "La société peut être supprimée.";
$l_naf_no = "Pas de codes NAF définis";
$l_naf_link_company = "Sociétés sont liées à ce code NAF";
$l_naf_link_company_no = "Pas de sociétés liées à ce code NAF";
$l_naf_cant_delete = "Le code NAF ne peut être supprimé !";
$l_naf_can_delete = "Le code NAF peut être supprimé";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_name = "Vous devez renseigner le Nom avant de valider.";
$l_delete_confirm = "Confirmer la suppression de la Société ?";
$l_j_check_zip = "Le code postal n'est pas correct.";
$l_j_check_phone = "Le numéro de téléphone n'est pas correct.";
$l_j_check_fax = "Le numéro de fax n'est pas correct.";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
$l_j_check_web = "L'adresse web n'est pas valide.";
$l_j_cat_deep = "Les catégories ne peuvent pas avoir plus de trois niveaux de profondeur";
$l_j_naf_delete = "Sélectionnez un code NAF pour le vérifier ou supprimer !";
$l_j_naf_update = "Vous devez sélectionner un code NAF et entrer une modification avant de valider !";
$l_j_naf_create = "Vous devez saisir un code NAF pour le créer !";
$l_j_select_company = "Vous devez sélectionner une société !";

// For the display preferences modification screen 
$l_company_display = "Options d'affichage des listes de sociétés";

$l_phone_labels = array ('WORK_VOICE' => 'Téléphone', 'CELL_VOICE' => 'Téléphone portable', 'WORK_FAX' => 'Fax', 'OTHER' => 'Autre téléphone');
$l_email_labels = array ('INTERNET' => 'Internet', 'OTHER' => 'Autre');
$l_address_labels = array ('HQ' => 'Siège', 'OTHER' => 'Autre');
$l_website_labels = array ('URL' => 'Site web', 'OTHER' => 'Autre');
