<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : list.inc                                                 //
// 2001-02-16 Vincent MARGUERIT                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_name = "Nom";
$l_subject = "Sujet";
$l_email = "E-mail";
$l_query = "Requête";
$l_creator = "Créateur";
$l_date_creation = "Création";
$l_updater = "Mise à jour par" ;
$l_date_last_update = "Mise à jour" ;

// Labels et textes
$l_list = "Liste";
$l_yes = "oui";
$l_no = "non";
$l_nb_contact = "# Contacts";
$l_list_contact = "Ayant comme contact";
$l_list_display = "Options d'affichage des listes de listes";
$l_contact_display = "Options d'affichage des listes de contacts";
$l_list_set = "Listes cibles";
$l_export_format = "Formats d'export disponibles";
$l_desc = "Description";
$l_export_intro = "Sélectionner le format d'export souhaité";
$l_select_list = "Sélectionner les listes cibles de l'export";

// Contact
$l_lastname = "Nom";
$l_firstname = "Prénom";
$l_function = "Fonction";
$l_company = "Société";
$l_phone = "Tél";
$l_hphone = "Tél perso";
$l_town = "Ville";

// Actions
$l_insert = "Enregistrer la liste";
$l_update = "Modifier la liste";
$l_delete = "Supprimer la liste";
$l_confirm = "Confirmer l'action";
$l_add = "Ajouter ces listes";
$l_mail = "Mailer les membres";

// Messages
$l_insert_ok = "L'insertion de la liste a été correctement effectuée.";
$l_insert_error = "L'insertion de la liste a échoué !";
$l_update_ok = "La modification de la liste a été correctement effectuée.";
$l_update_error = "La modification de la liste a échoué !";
$l_delete_ok = "La suppression de la liste a été correctement effectuée.";
$l_delete_error = "La suppression de la liste a échoué !";
$l_name_list = "Le nom de la liste doit etre rempli" ;
$l_name_list_nomatch = "Le nom de la liste n'est pas valide" ;
$l_check_samelist = "Ces listes sembles similaires à la nouvelle !" ;
$l_list_exists = "Désolé mais cette liste existe déjà";
$l_j_check_email = "L'adresse e-mail n'est pas valide !";
$l_warn_delete = "contacts enregistrés dans cette liste";

$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir les listes correspondantes.";
$l_no_list = "Aucune liste ne correspond à ces critères." ;
$l_no_contact_found = "Aucun contact ne correspond à ces critères." ;
$l_found = "listes trouvées.";
$l_no_found = "Aucune liste ne correspond à ces critères !";
$l_add_contact = "Ajouter des contacts" ;
$l_del_contact_sel = "Enlever ces contacts";
$l_contact_added = "Contact(s) ajouté(s)";
$l_contact_removed = "Contact(s) enlevé(s)";
$l_con_found = "contacts enregistrés";
$l_no_contact = "Il n'y a pas de contact dans cette liste" ;

$l_fill_name = "Le nom de la liste doit être saisi !";
$l_invalid_query = "La requête saisie est incorrecte !";

//------------------------------------//
// Export Format informations         //
//------------------------------------//
// Format 0 : HTML Stickers
$c_export_format[0]["name"] = "Etiquettes HTML";
$c_export_format[0]["info"] = "Prévisualisation d'étiquettes en HTML";
// Format 1 : HTML Table
$c_export_format[1]["name"] = "Tableau HTML";
$c_export_format[1]["info"] = "Prévisualisation tableau HTML";
// Format 2 : RTF Stickers
$c_export_format[2]["name"] = "Etiquettes RTF";
$c_export_format[2]["info"] = "Génération d'étiquettes RTF";
// Format 3 : Excel data
$c_export_format[3]["name"] = "Tableau Excel";
$c_export_format[3]["info"] = "Tableau Excel pour publipostage";
