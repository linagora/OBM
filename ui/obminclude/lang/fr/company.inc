<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : company.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Numéro";
$l_name = "Nom";
$l_company_name = "Nom Société";
$l_kind = "Type";
$l_market = "Responsable du compte";
$l_company_kind = "Type Société";
$l_address = "Adresse";
$l_postcode = "Code Postal";
$l_postcode_first = "CP";
$l_town = "Ville";
$l_expresspostal = "Cedex";
$l_country = "Pays";
$l_phone = "Tel";
$l_fax = "Fax";
$l_web = "W";
$l_email = "@M";

// Labels et textes
$l_mailing_first = "M";
$l_comment = "Commentaire";
$l_contact_list = "Liste Contacts";
$l_contact_new = "Nouveau Contact";
$l_deal_list = "Liste Affaires";
$l_deal_new = "Nouvelle Affaire";
$l_kind_manage = "Gestion des Types de Société";
$l_kind_exist = "Types existants";
$l_kind_new = "Nouveau Type";
$l_internal = "Interne";

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
$fieldnames["company_contact_number"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_list\" ALT=\"C\">";
$fieldnames["company_new_contact"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_contact_new\" ALT=\"NC\">";
$fieldnames["company_deal_number"] = "<IMG BORDER=0 SRC=\"/images/$set_theme/$ico_deal_list\" ALT=\"D\">";

// Titres
$l_company = "Société";

// Actions
$l_insert = "Enregistrer la Société";
$l_update = "Modifier la Société";
$l_checkdelete = "Vérifier ou Supprimer la Société";
$l_delete = "Supprimer la Société";
$l_mailing_update = "Mise à jour Mailing";
$l_mailing_reset = "Mise à zéro Mailing";
$l_kind_update = "Modifier le Type";
$l_kind_checkdelete = "Vérifier ou Supprimer le Type";
$l_kind_delete = "Supprimer le Type";
$l_kind_insert = "Créer le Type";
$l_confirm = "Confirmer l'action";
$l_select_company = "Sélectionner la société";

// Messages
$l_error_permission = "Vous n'avez pas les droits d'acces à cette page.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des sociétés correspondantes.";
$l_found = "société trouvées.";
$l_no_found = "Aucune société ne correspond à ces critères !";
$l_delete_ok = "La suppression de la société a été correctement effectuée.";
$l_delete_error = "La suppression de la société a échoué !";
$l_insert_ok = "L'insertion de la société a été correctement effectuée.";
$l_insert_error = "L'insertion de la société a échoué !";
$l_check_samecompany = "Ces sociétés semblent similaires à cette nouvelle entrée !";
$l_update_ok = "La modification de la société a été correctement effectuée";
$l_update_error = "La modification de la société a échoué !";
$l_query_error = "Problème en executant la requete";
$l_mailing_ok = "La mise à jour du mailing s'est bien effectuée.";
$l_mailing_nb = "sociétés enregistrées dans le mailing.";
$l_mailing_reset_ok = "La mise à zéro du mailing s'est bien effectuée";
$l_link_deal = "La société est référencée par ces affaires";
$l_link_deal_no = "La société n'est pas référencée dans les affaires";
$l_link_contact = "Ces contacts appartiennent à cette société";
$l_link_contact_no = "La société n'a pas de contact";
$l_cant_delete = "La société ne peut être supprimée !";
$l_can_delete = "La société peut être supprimée.";
$l_kind_delete_ok = "La suppression du type a été correctement effectuée.";
$l_kind_delete_error = "La suppression du type a échoué !";
$l_kind_insert_ok = "L'insertion du type a été correctement effectuée.";
$l_kind_insert_error = "L'insertion du type a échoué !";
$l_kind_update_ok = "La modification du type a été correctement effectuée";
$l_kind_update_error = "La modification du type a &eacute;chou&eacute;e !";
$l_kind_link_company = "Ces sociétés sont du type";
$l_kind_link_company_no = "Pas de sociétés du type";
$l_kind_cant_delete = "Le type ne peut être supprimé !";
$l_kind_can_delete = "Le type peut être supprimé.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Really delete the company ?";
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
$l_j_select_company = "Vous devez sélectionner une société !";

// For the display preferences modification screen 
$l_company_display = "Options d'affichage des listes de sociétés";
