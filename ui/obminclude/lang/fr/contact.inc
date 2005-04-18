<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : contact.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_lastname = "Nom";
$l_firstname = "Prénom";
$l_kind = "Genre";
$l_market = "Responsable";
$l_function = "Fonction";
$l_title = "Titre";
$l_service = "Service";
$l_minihphone = "(Perso)";
$l_minimphone = "(Mobile)";
$l_email = "@M";
$l_email_other = "Autre Mail";
$l_mailing_ok = "Activé pour mailing";
$l_coord = "Coordonnées";
$l_contact_name = "Nom Contact";
$l_from_company = "&nbsp;&nbsp;Société";
$l_telother = "Autres tel";
$l_contact_select_company = "Sélectionner la société du Contact.";
$l_func_manage = "Gestion des Fonctions";
$l_func_exist = "Fonctions existantes";
$l_func_no = "Pas de fonctions définies";
$l_func_new = "Nouvelle Fonction";
$l_kind_manage = "Gestion des Genre";
$l_kind_exist = "Genre existants";
$l_kind_new = "Nouveau Genre";
$l_header = "En-tête";
$l_code = "Code";
$l_label = "Label";
$l_category1 = "Categorie 1";
$l_category2 = "Categorie 2";
$l_letter = "En-tête de Lettre";

// Actions
$l_cat1_delete_ok = "La suppression de la categorie 1 a été correctement effectuée.";
$l_cat1_delete_error = "La suppression de la categorie 1 a échoué !";
$l_cat1_insert_ok = "L'insertion de la categorie 1 a été correctement effectuée.";
$l_cat1_insert_error = "L'insertion de la categorie 1 a échoué !";
$l_cat1_update_ok = "La modification de la categorie 1 a été correctement effectuée";
$l_cat1_update_error = "La modification de la categorie 1 a &eacute;chou&eacute;e !";
$l_cat1_link_contact = "La catégorie 1 est référencée par ces contacts";
$l_cat1_link_contact_no = "La catégorie 1 n'est référencée dans aucun contacts";
$l_cat1_can_delete = " La catégorie 1 peut être supprimée"; 
$l_cat1_cant_delete = " La catégorie 1 ne peut pas etre supprimée";
$l_cat2_link_contact = "La catégorie 2 est référencée par ces contacts";
$l_cat2_link_contact_no = "La catégorie 2 n'est référencée dans aucun contacts";
$l_cat2_can_delete = " La catégorie 2 peut etre supprimée"; 
$l_cat2_cant_delete = " La catégorie 2 ne peut pas etre supprimée";
$l_cat2_checkdelete = "Vérifier ou supprimer la catégorie 2"; 
$l_cat2_update = "Mettre à jour la catégorie 2"; 
$l_cat2_insert = "Insérer une catégorie 2";
$l_cat2_delete_ok = "La suppression de la categorie 2 a été correctement effectuée.";
$l_cat2_delete_error = "La suppression de la categorie 2  a échoué !";
$l_cat2_insert_ok = "L'insertion de la categorie 2 a été correctement effectuée.";
$l_cat2_insert_error = "L'insertion de la categorie 2 a échoué !";
$l_cat2_update_ok = "La modification de la categorie 2 a été correctement effectuée";
$l_cat2_update_error = "La modification de la categorie 2 a &eacute;chou&eacute;e !";
$l_j_cat1_create = "Vous devez renseigner le label de la categorie 1";
$l_j_cat2_create = "Vous devez renseigner le label de la categorie 2";
$l_j_cat1_update = "Vous devez renseigner le label de la categorie 1";
$l_j_cat2_update = "Vous devez renseigner le label de la categorie 2";
$l_j_cat1_delete = "Vous devez sélectionner une categorie 1";
$l_j_cat2_delete = "Vous devez sélectionner une categorie 2";
$l_insert = "Enregistrer le Contact";
$l_update = "Modifier le Contact";
$l_checkdelete = "Vérifier ou Supprimer le Contact";
$l_delete = "Supprimer le Contact";
$l_func_update = "Modifier la Fonction";
$l_func_checkdelete = "Vérifier ou Supprimer la Fonction";
$l_func_delete = "Supprimer la Fonction";
$l_func_insert = "Créer la Fonction";
$l_kind_update = "Modifier le Genre";
$l_kind_checkdelete = "Vérifier ou Supprimer le Genre";
$l_kind_delete = "Supprimer le Genre";
$l_kind_insert = "Créer le Genre";
$l_add = "Ajouter ces contacts";
$l_add_cats1 = "Ajouter ces categories 1";
$l_add_cats2 = "Ajouter ces categories 2";
$l_subscription = "Abonnements";
$l_subscription_list = "Abonnements";
$l_subscription_new= "Nouvel abonnement";

// Messages
$l_cat1_manage = "Gestion des catégories 1";
$l_cat1_exist = "Categories 1 existantes";
$l_cat1_new = "Nouvelle categorie 1";
$l_cat1_delete =  "Supprimer";
$l_cat2_manage = "Gestion des catégories 2";
$l_cat2_exist = "Categories 2 existantes";
$l_cat2_new = "Nouvelle categorie 2";
$l_cat2_delete =  "Supprimer";
$l_cat1_checkdelete = "Vérifier ou supprimer la catégorie 1"; 
$l_cat1_update = "Mettre à jour la catégorie 1"; 
$l_cat1_insert = "Insérer une catégorie 1"; 

$l_check_samecontact = "Ces contacts semblent similaires à cette nouvelle entrée !";
$l_link_deal = "Le contact est référencé par ces affaires";
$l_link_deal_no = "Le contact n'est pas référencé dans les affaires";
$l_link_contract = "Le contact est référencé par ces contrats";
$l_link_contract_no = "Le contact n'est pas référencé dans les contrats";
$l_link_list = "Le contact est enregistré dans ces listes";
$l_link_list_no = "Le contact n'est pas référencé dans les listes";
$l_link_publication = "Le contact est abonné à ces publications";
$l_link_publication_no = "Le contact n'est pas abonné à des publications";
$l_cant_delete = "Le contact ne peut être supprimé !";
$l_can_delete = "Le contact peut être supprimé.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des contacts correspondants.";
$l_found = "contacts trouvés.";
$l_no_found = "Aucun contact ne correspond à ces critères !";
$l_delete_ok = "La suppression du contact a été correctement effectuée.";
$l_delete_error = "La suppression du contact a échoué !";
$l_insert_ok = "L'insertion du contact a été correctement effectuée.";
$l_insert_error = "L'insertion du contact a échoué !";
$l_update_ok = "La modification du contact a été correctement effectuée";
$l_update_error = "La modification du contact a échoué !";
// Function messages
$l_func_delete_ok = "La suppression de la fonction a été correctement effectuée.";
$l_func_delete_error = "La suppression de la fonction a échoué !";
$l_func_insert_ok = "L'insertion de la fonction a été correctement effectuée.";
$l_func_insert_error = "L'insertion de la fonction a échoué !";
$l_func_update_ok = "La modification de la fonction a été correctement effectuée";
$l_func_update_error = "La modification de la fonction a échoué !";
$l_func_link_contact = "Ces contacts ont cette fonction";
$l_func_link_contact_no = "Pas de contacts de la fonction";
$l_func_cant_delete = "La fonction ne peut être supprimée !";
$l_func_can_delete = "La fonction peut être supprimée.";
// Kind messages
$l_kind_delete_ok = "La suppression du genre a été correctement effectuée.";
$l_kind_delete_error = "La suppression du genre a échoué !";
$l_kind_insert_ok = "L'insertion du genre a été correctement effectuée.";
$l_kind_insert_error = "L'insertion du genre a échoué !";
$l_kind_update_ok = "La modification du genre a été correctement effectuée";
$l_kind_update_error = "La modification du genre a échoué !";
$l_kind_link_contact = "Ces contacts sont du genre";
$l_kind_link_contact_no = "Pas de contacts du genre";
$l_kind_cant_delete = "Le contact ne peut être supprimé !";
$l_kind_can_delete = "Le contact peut être supprimé.";


// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_lastname = "Vous devez renseigner le Nom avant de valider.";
$l_fill_company = "Vous devez sélectionner une société avant de valider.";
$l_fill_phone = "Vous devez renseigner le telephone avant de valider.";
$l_delete_confirm = "Confirmer la suppression du contact ?";
$l_j_select_company = "Vous devez sélectionner une société !";
$l_j_check_phone = "Le numéro de téléphone n'est pas correct.";
$l_j_check_hphone = "Le numéro de téléphone personnel n'est pas correct.";
$l_j_check_mphone = "Le numéro de téléphone mobile n'est pas correct.";
$l_j_check_fax = "Le numéro de fax n'est pas correct.";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
// JavaScript Messages - Functions
$l_j_func_delete = "Sélectionnez une fonction pour la vérifier ou supprimer !";
$l_j_func_update = "Vous devez sélectionner une fonction et entrer une modification avant de valider !";
$l_j_func_create = "Vous devez saisir un label de fonction pour la créer !";
// JavaScript Messages - Kind
$l_j_kind_delete = "Sélectionnez un genre pour le vérifier ou supprimer !";
$l_j_kind_update = "Vous devez sélectionner un genre et entrer une modification avant de valider !";
$l_j_kind_create = "Vous devez saisir toutes les informations d'un genre pour le créer !";

// For the display preferences modification screen 
$l_contact_display = "Options d'affichage des listes de contacts";


// external Fields 
$l_timecreate = "Date de Création";
$l_timeupdate = "Date de Mise à jour";
$l_publication_title = "Titre";
$l_year = "Année";
$l_renewal = "Renouvellement automatique";
$l_quantity = "Quantité";
$l_recept = "Mode de réception";
$l_quantity = "Quantité";
$l_quantity = "Quantité";
$l_no_found_subscription = "Aucun abonnement lié à ce contact !";
