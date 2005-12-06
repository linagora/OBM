<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : company.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numéro";
$l_aka = "Aussi connue comme";
$l_company_name = "Nom Société";
$l_kind = "Type";
$l_category1 = "Catégories";
$l_category1_s = "Catégories";
$l_activity = "Secteur d'activité";
$l_nafcode = "Code NAF";
$l_market = "Responsable du compte";
$l_company_kind = "Type Société";
$l_web = "W";
$l_email = "@M";
$l_coord = "Coordonnées";
$l_vat = "Numéro Intracommunautaire";

// Labels et textes
$l_comment = "Commentaire";
$l_contact_new = "Nouveau Contact";
$l_deal_new = "Nouvelle Affaire";
$l_project_new = "Nouveau Projet";
$l_contract_new = "Nouveau Contrat";
$l_kind_manage = "Gestion des Types de Société";
$l_kind_exist = "Types existants";
$l_kind_new = "Nouveau Type";
$l_cat_tree = "Sous Catégories";
$l_cat_manage = "Gestion des Catégories de Société";
$l_cat_exist = "Catégories existantes";
$l_cat_new = "Nouvelle Catégorie";
$l_cat_label = "Label";
$l_cat_code = "Code ";
$l_internal = "Interne";
$l_act_manage = "Gestion des secteurs d'activité";
$l_act_exist = "Secteurs d'activité existants";
$l_act_new = "Nouveau secteur d'activité";
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
$l_kind_update = "Modifier le Type";
$l_kind_checkdelete = "Vérifier ou Supprimer le Type";
$l_kind_delete = "Supprimer le Type";
$l_kind_insert = "Créer le Type";
$l_cat_update = "Modifier la Catégorie";
$l_cat_checkdelete = "Vérifier ou Supprimer la Catégorie";
$l_cat_delete = "Supprimer la Catégorie";
$l_cat_insert = "Créer la Catégorie";
$l_act_update = "Modifier le secteur d'activité";
$l_act_checkdelete = "Vérifier ou Supprimer le secteur d'activité";
$l_act_delete = "Supprimer le secteur d'activité";
$l_act_insert = "Créer le secteur d'activité";
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
$l_delete_ok = "La suppression de la société a été correctement effectuée.";
$l_delete_error = "La suppression de la société a échoué !";
$l_insert_ok = "L'insertion de la société a été correctement effectuée.";
$l_insert_error = "L'insertion de la société a échoué !";
$l_check_samecompany = "Ces sociétés semblent similaires à cette nouvelle entrée !";
$l_update_ok = "La modification de la société a été correctement effectuée";
$l_update_error = "La modification de la société a échoué !";
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
$l_kind_delete_ok = "La suppression du type a été correctement effectuée.";
$l_kind_delete_error = "La suppression du type a échoué !";
$l_kind_insert_ok = "L'insertion du type a été correctement effectuée.";
$l_kind_insert_error = "L'insertion du type a échoué !";
$l_kind_update_ok = "La modification du type a été correctement effectuée";
$l_kind_update_error = "La modification du type a échoué !";
$l_kind_link_company = "Ces sociétés sont du type";
$l_kind_link_company_no = "Pas de sociétés du type";
$l_kind_cant_delete = "Le type ne peut être supprimé !";
$l_kind_can_delete = "Le type peut être supprimé.";
$l_cat_no = "Pas de catégories définies";
$l_cat_delete_ok = "La suppression de la catégorie a été correctement effectuée.";
$l_cat_delete_error = "La suppression de la catégorie a échoué !";
$l_cat_insert_ok = "L'insertion de la catégorie a été correctement effectuée.";
$l_cat_insert_error = "L'insertion de la catégorie a échoué !";
$l_cat_update_ok = "La modification de la catégorie a été correctement effectuée";
$l_cat_update_error = "La modification de la catégorie a échoué !";
$l_cat_link_company = "Ces sociétés sont de la catégorie";
$l_cat_link_company_no = "Pas de sociétés de la catégorie";
$l_cat_cant_delete = "La catégorie ne peut être supprimée !";
$l_cat_can_delete = "La catégorie peut être supprimée";
$l_act_delete_ok = "La suppression du secteur d'activité a été correctement effectuée.";
$l_act_delete_error = "La suppression du secteur d'activité a échoué !";
$l_act_insert_ok = "L'insertion du secteur d'activité a été correctement effectuée.";
$l_act_insert_error = "L'insertion du secteur d'activité a échoué !";
$l_act_update_ok = "La modification du secteur d'activité a été correctement effectuée";
$l_act_update_error = "La modification du secteur d'activité a échoué !";
$l_act_link_company = "Ces sociétés sont du secteur d'activité";
$l_act_link_company_no = "Pas de sociétés du secteur d'activité";
$l_act_cant_delete = "Le secteur d'activité ne peut être supprimé !";
$l_act_can_delete = "Le secteur d'activité peut être supprimé.";
$l_naf_no = "Pas de codes NAF définis";
$l_naf_delete_ok = "La suppression du code NAF a été correctement effectuée.";
$l_naf_delete_error = "La suppression du code NAF a échoué !";
$l_naf_insert_ok = "L'insertion du code NAF a été correctement effectuée.";
$l_naf_insert_error = "L'insertion du code NAF a échoué !";
$l_naf_update_ok = "La modification du code NAF a été correctement effectuée";
$l_naf_update_error = "La modification du code NAF a échoué !";
$l_naf_link_company = "Ces sociétés sont de code NAF";
$l_naf_link_company_no = "Pas de sociétés de code NAF";
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
$l_j_kind_delete = "Sélectionnez un type pour le vérifier ou supprimer !";
$l_j_kind_update = "Vous devez sélectionner un type et entrer une modification avant de valider !";
$l_j_kind_create = "Vous devez saisir un nom de type pour le créer !";
$l_j_cat_delete = "Sélectionnez une catégorie pour la vérifier ou supprimer !";
$l_j_cat_update = "Vous devez sélectionner une catégorie et entrer une modification avant de valider !";
$l_j_cat_create = "Vous devez saisir un nom de catégorie et un code pour la créer !";
$l_j_cat_deep = "Les catégories ne peuvent pas avoir plus de trois niveaux de profondeur";
$l_j_act_delete = "Sélectionnez un secteur d'activité pour le vérifier ou supprimer !";
$l_j_act_update = "Vous devez sélectionner un secteur d'activité et entrer une modification avant de valider !";
$l_j_act_create = "Vous devez saisir un nom de secteur d'activité pour le créer !";
$l_j_naf_delete = "Sélectionnez un code NAF pour le vérifier ou supprimer !";
$l_j_naf_update = "Vous devez sélectionner un code NAF et entrer une modification avant de valider !";
$l_j_naf_create = "Vous devez saisir un code NAF pour le créer !";
$l_j_select_company = "Vous devez sélectionner une société !";

// For the display preferences modification screen 
$l_company_display = "Options d'affichage des listes de sociétés";
