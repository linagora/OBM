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
$l_creator = "Créateur";
$l_date_creation = "Création";
$l_updater = "Mise à jour par" ;
$l_date_last_update = "Mise à jour" ;

// Labels et textes
$l_list_list = "Liste";
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
$l_export = "Exporter";

// Messages
$l_insert_ok = "L'insertion de la liste a été correctement effectuée.";
$l_insert_error = "L'insertion de la liste a échoué !";
$l_update_ok = "La modification de la liste a été correctement effectuée.";
$l_update_error = "La modification de la liste a échoué !";
$l_delete_ok = "La suppression de la liste a été correctement effectuée.";
$l_delete_error = "La suppression de la liste a échoué !";
$l_name_list = "Le nom de la liste doit etre rempli" ;
$l_name_list_nomatch = "Le nom de la liste n'est pas valide" ;
$l_name_list_nomatch = "Le sujet de la liste doit etre rempli" ;
$l_list_exists = "Désolé mais cette liste existe déjà";
$l_j_check_email = "L'adresse e-mail n'est pas valide !";
$l_warn_delete = "contacts enregistrés dans cette liste";

$l_no_display = "Renseigner les crit&egrave;res souhait&eacute;s et cliquer sur < Chercher > pour obtenir les listes correspondantes.";
$l_no_list = "Aucune liste ne correspond à ces critères." ;
$l_no_contact_found = "Aucun contact ne correspond à ces critères." ;
$l_found = "listes trouvées.";
$l_no_found = "Aucune liste ne correspond à ces critères !";
$l_add_contact = "Ajouter des contacts" ;
$l_del_contact_sel = "Enlever ces contacts";
$l_contact_added = "Contact(s) ajouté(s)";
$l_contact_removed = "Contact(s) enlevé(s)";

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


$l_new_contact1 = "NC" ;
$l_list_contact1 = "C" ;
$l_list_infos = "Les informations sur la liste" ;
$l_list_infos_short = "Informations sur la liste" ;
$l_list_noinfo = "Auncune information n'est disponible" ;

$l_list_contacts = "Les contacts appartenant à cette liste" ;
$l_con_found = "contacts enregistrés";
$l_no_contact = "Il n'y a pas de contact dans cette liste" ;
$l_list_contacts_todel = "Liste des contacts" ;
$l_list_del_contact = "Enlever" ;
$l_list_del_contact_chosen = "Enlever les contacts sélectionnés" ;
$l_nb_contact_deleted = "Nombre de contact(s) enlevé(s)" ;
$l_name_contact = "Nom" ;
$l_name_company = "Société" ;
$l_list_add_contact = "Ajouter" ;
$l_list_add_contact_chosen = "Ajouter les contacts sélectionnés" ;
$l_nb_contact_added = "Nombre de contact(s) ajouté(s)" ;
$l_delete_list = "Supprimer cette liste" ;
$l_modify_list = "Modifer la liste" ;
$l_delete_confirm = "Confirmer la suppression de la liste ?" ;
$l_complete_mail_form = "Veuillez remplir les champs du mail à envoyer" ;
$l_mail_sender = "Expéditeur" ;
$l_mail_rcpt = "Répondre à" ;
$l_mail_subject = "Sujet du mail" ;
$l_mail_body = "Corps du mail" ;
$l_send_mail = "Envoyer le mail" ;
$l_confirm_send = "Confirmer l'envoie du mail ?" ;
$l_name_empty = "Vous devez renseigner le nom de l'expéditeur" ;
$l_mail_empty = "Le mail de l'expéditeur doit être rempli" ;
$l_mail_false = "Le mail de l'expéditeur doit être valide" ;
$l_mail_same = "Inutile de remplir ce mail si c'est le même que le mail de l'expéditeur" ;
$l_mail_rcpt_false = "Le mail pour recevoir vos réponses doit être valide" ;
$l_subject_empty = "Le sujet du message est obligatoire" ;
$l_body_empty = "Le corps du message est obligatoire" ;
$l_name_obligatory = "Votre nom est obligatoire" ;
$l_mail_obligatory = "Votre e-mail est obligatoire" ;
$l_mail_rcpt_obli = "Seulement si c'est différent de votre e-mail" ;
$l_subject_obli = "Le sujet du mail est obligatoire" ;
$l_nb_message_send = "Nombre de message(s) envoyé(s) : " ;


//Error:
$l_error_which = "Error::: The which variable is NOT CORRECTLY set... Please Check your administrator" ;
$l_error_authentification = "Error::: Wrong Authentification... Please Check your administrator " ;

// Champs specifiques
// Format de date et heure pour MySQL pour affichage dans ecran detail affaire
$l_time_format = "%d-%m-%Y %H:%i";
