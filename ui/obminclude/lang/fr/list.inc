<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : list.inc                                                 //
// 2001-02-16 Vincent MARGUERIT                                              //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_subject = "Sujet";
$l_email = "E-mail";
$l_query = "Requête";
$l_creator = "Créateur";
$l_date_creation = "Création";
$l_updater = "Mise à jour par" ;
$l_date_last_update = "Mise à jour" ;

// Labels et textes
$l_list = "Liste";
$l_nb_contact = "# Contacts";
$l_list_contact = "Ayant comme contact";
$l_list_display = "Options d'affichage des listes de listes";
$l_contact_display = "Options d'affichage des listes de contacts";
$l_list_set = "Listes cibles";
$l_export_format = "Formats d'export disponibles";
$l_desc = "Description";
$l_export_intro = "Sélectionner le format d'export souhaité";
$l_select_list = "Sélectionner les listes cibles de l'export";
$l_list_wizard = "Recherche global";
// Contact
$l_lastname = "Nom";
$l_firstname = "Prénom";
$l_function = "Fonction";
$l_company = "Société";
$l_hphone = "Tél perso";
$l_town = "Ville";
$l_contact = "Contact";
$l_country = "Pays";
$l_zipcode = "Code Postal";

// Actions
$l_insert = "Enregistrer";
$l_update = "Modifier";
$l_delete = "Supprimer";
$l_add = "Ajouter ces listes";
$l_add_criterion = "Ajouter un critère";

// Messages
$l_insert_ok = "L'insertion de la liste a été correctement effectuée.";
$l_insert_error = "L'insertion de la liste a échoué !";
$l_update_ok = "La modification de la liste a été correctement effectuée.";
$l_update_error = "La modification de la liste a échoué !";
$l_delete_ok = "La suppression de la liste a été correctement effectuée.";
$l_delete_error = "La suppression de la liste a échoué !";
$l_name_list_nomatch = "Le nom de la liste n'est pas valide";
$l_check_samelist = "Ces listes sembles similaires à la nouvelle !";
$l_list_exists = "Cette liste existe déjà !";
$l_j_check_email = "L'adresse e-mail n'est pas valide !";
$l_warn_delete = "contacts enregistrés dans cette liste";

$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir les listes correspondantes.";
$l_found = "listes trouvées.";
$l_no_found = "Aucune liste ne correspond à ces critères !";
$l_add_contact = "Ajouter des contacts à la liste";
$l_del_contact_sel = "Enlever ces contacts";
$l_contact_added = "Contact(s) ajouté(s)";
$l_contact_removed = "Contact(s) enlevé(s)";
$l_static_con_found = "contacts statiques enregistrés";
$l_dyn_con_found = "contacts dynamiques enregistrés";
$l_no_static_contact = "Il n'y a pas de contact statique dans cette liste";
$l_no_dyn_contact = "Il n'y a pas de contact dynamique dans cette liste";

$l_fill_name = "Le nom de la liste doit être saisi !";
$l_fill_subject = "Le sujet doit être saisi !";
$l_invalid_query = "La requête saisie est incorrecte !";
$l_banned_word = "Mot interdit";
$l_hungry_query = "La requête est trop consommatrice de ressource !";

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
