<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : contract.inc                                             //
// 2001-08-20  Richard FILIPPI                                               //
///////////////////////////////////////////////////////////////////////////////
//  $Id  //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_contract_num="Numéro";
$l_contract_label="Label ";
$l_contract_company="Société";
$l_contract_contracttype="Type";
$l_contract_exp = "Expiration";
$l_date_alarm_after = "Date Expiration Apr&egrave;s";
$l_date_alarm_before = "Date Expiration Avant";
$l_after = "Date Expiration Apr&egrave;s";
$l_before = "Date Expiration Avant";
$l_incident_list = "Liste des incidents";
$l_incident_new = "Ajouter un incident";

$fieldnames["contract_label"] = $l_contract_label;
$fieldnames["contract_company_name"] = $l_contract_company;
$fieldnames["contract_number"] = $l_contract_num;
$fieldnames["contract_dateexp"] = $l_contract_exp ;
$fieldnames["contracttype_label"] = $l_contract_contracttype;

$l_minus="-";
$l_plus="+";
$l_order="Ordre";
$l_todo = "A faire";

$l_contract_select_deal = "Séléction de l'Affaire liée à un contrat.";
$l_deal_label = "Affaire";
$l_label = "Label";
$l_label_libelle = "Mot-clef du libellé";
$l_label_start = "Numéro";
$l_kind = "Type";
$l_category = "Cat&eacute;gorie";
$l_state = "Etat";
$l_marketing_manager = "Resp Commercial";
$l_technical_manager = "Resp Technique";
$l_client_manager = "Resp Client";
$l_manager= "Responsable";
$l_contacts = "Contacts";
$l_clause = "Clauses ";
$l_comment = "Commentaire";
$l_company = "Nom Soci&eacute;t&eacute;";
$l_date = "Date";
$l_date_init = "Date Debut";
$l_dateexp = "Date Expiration";
$l_date_alarm = "Date Alarme";
$l_last_update = "Derni&egrave;re modification le";
$l_at = "&agrave;";
$l_number = "Numéro";
$l_private = "Priv&eacute;";
$l_public = "Publique";
$l_visibility ="Acc&egrave;s";

$l_type_manage = "Gestion des Types";
$l_type_exist = "Types existants";
$l_type_new = "Nouveau Type";
$l_category_label="Label";
$l_category_minilabel="Mini Label";
$l_add_contract_deal = "Veuillez à présent remplire les champs du contrat lié à l'affaire";
// Titres
$l_deal = "Contrat de Support";
$l_deal_detail = "Detail Contrat";
$l_deal_new = "Nouveau Contrat";
$l_deal_insert = "Insertion Contrat";
$l_deal_update = "Modification Contrat";
$l_deal_delete = "Suppression Contrat";

// Actions
$l_insert = "Enregistrer le Contrat";
$l_update = "Modifier le Contrat";
$l_delete = "Supprimer le Contrat";
$l_select_company = "S&eacute;lectionner la soci&eacute;t&eacute; avec laquelle le contrat doit être renseigné.";

$l_type_delete = "Supprimer le Type"; 
$l_type_update = "Modifier le Type";
$l_type_insert = "Créer le Type";
$l_status_update = "Modifier l'etat";
$l_status_delete = "Supprimer l'etat";
$l_status_insert = "Créer l'etat";
$l_category_delete = "Supprimer la categorie"; 
$l_category_update = "Modifier la categorie";
$l_category_insert = "Créer la categorie";
$l_select_contract = "Selectionner le contrat";

// Messages

$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_error_visibility = "Les informations contenues dans cette page sont priv&eacute;es. Vous ne pouvez pas les consulter";
$l_no_display = "Renseigner les crit&egrave;res souhait&eacute;s et cliquer sur < Chercher > pour obtenir la liste des contrats de support correspondants.";
$l_found = "contrat(s) trouv&eacute;(s).";
$l_no_found = "Aucun Contrat ne correspond &agrave; ces crit&egrave;res !";
$l_delete_ok = "La suppression du Contrat a &eacute;t&eacute; correctement effectu&eacute;e.";
$l_delete_error = "La suppression du Contrat a &eacute;chou&eacute;e !";
$l_insert_ok = "L'insertion du Contrat a &eacute;t&eacute; correctement effectu&eacute;e.";
$l_insert_error = "L'insertion du Contrat a &eacute;chou&eacute;e !";
$l_update_ok = "La modification du Contrat a &eacute;t&eacute; correctement effectu&eacute;e";
$l_update_error = "La modification du Contrat a &eacute;chou&eacute;e !";
$l_query_error = "Probl&egrave;me en executant la requete";

$l_type_insert_ok = "L'insertion du type a &eacute;t&eacute; correctement effectu&eacute;e.";
$l_type_insert_error = "L'insertion du type a &eacute;chou&eacute;e !";
$l_type_update_ok = "La modification du type a &eacute;t&eacute; correctement effectu&eacute;e";
$l_type_update_error = "La modification du type a &eacute;chou&eacute;e !";
$l_type_delete_error = "La suppression du type a &eacute;chou&eacute;e !";
$l_type_del_verif_error = "(s)  sont de ce type.";
$l_type_delete_ok = "La suppression du type a &eacute;t&eacute; correctement effectu&eacute;e.";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_label = "\\n Vous devez renseigner le Label avant de valider.";
$l_fill_number = "\\n Vous devez renseigner le Numero avant de valider.";
$l_j_select_company = "Vous devez sélectionner une societe !";
$l_fill_date = "La date doit être renseignee.";
$l_invalid_date = "Format de date invalide (JJ/MM/AAAA ou AAAA-MM-JJ).";
$l_delete_confirm = "Confirmer la suppression du contrat ?";

$mj_type_delete_confirm = "Confirmer la suppression du type ?";
$mj_type_delete = "Vous devez selectionner un type pour le supprimer !";
$mj_type_update = "Vous devez selectionner un type et entrer une modification avant de valider !"; 
$mj_type_create = "Vous devez saisir un nom de type pour le créer !";
$l_length_label = " Le champ Label est trop long ";
$l_length_number = " Le champ Numéro de contrat est trop long";

// MySQL Date and time format for display in the deal detail screen
$l_time_format = "%Y-%m-%d %H:%i:%s";

// For the display preferences modification screen 
$l_contract_display = "Options d'affichage des Contrats de Support";
$l_updater = "Derniere mise à jour par" ;
$l_date_last_update = "Date de la dernière mise à jour" ;
