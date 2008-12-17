<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : contact.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_market = "Responsable";
$l_function = "Fonction";
$l_minihphone = "(Perso)";
$l_minimphone = "(Mobile)";
$l_email = "@M";
$l_email_other = "Autre Mail";
$l_mailing_ok = "Activé pour mailing";
$l_mailing_ok_only = "Activé pour mailing seulement";
$l_private_only = "Contacts privé uniquement";
$l_newsletter = "Newsletter";
$l_news = "News";
$l_contact_name = "Nom Contact";
$l_from_company = "&nbsp;&nbsp;Société";
$l_telother = "Autres tel";
$l_contact_select_company = "Sélectionner la société du Contact.";
$l_kind_manage = "Gestion des Genre";
$l_kind_exist = "Genre existants";
$l_kind_new = "Nouveau Genre";
$l_header = "En-tête";
$l_code = "Code";
$l_label = "Label";
$l_letter = "En-tête de Lettre";
$l_date = "Date";
$l_no_date = "Non spécifiée";
$l_comment1 = "Commentaire";
$l_comment2 = "Commentaire 2";
$l_comment3 = "Commentaire 3";
$l_category3 = "catégorie 3";
$l_category4 = "catégorie 4";
$l_category5 = "catégorie 5";
$l_date_after = "Date Après";
$l_date_before = "Date Avant";
$l_mailing_ok_first = "Mail.";
$l_birthday = "Date de naissance";

// Actions
$l_j_check_date = "Remplissez la date avec une date valide";
$l_insert = "Enregistrer le Contact";
$l_update = "Modifier le Contact";
$l_checkdelete = "Vérifier ou Supprimer le Contact";
$l_delete = "Supprimer le Contact";
$l_kind_update = "Modifier le Genre";
$l_kind_checkdelete = "Vérifier ou Supprimer le Genre";
$l_kind_delete = "Supprimer le Genre";
$l_kind_insert = "Créer le Genre";
$l_subscription = "Abonnements";
$l_subscription_list = "Abonnements";
$l_subscription_new= "Nouvel abonnement";
$l_add_comment1 = "Ajouter un commentaire";
$l_upd_comment1 = "Modifier le commentaire";
$l_add_comment2 = "Ajouter un commentaire 2";
$l_upd_comment2 = "Modifier le commentaire 2";
$l_add_comment3 = "Ajouter un commentaire 3";
$l_upd_comment3 = "Modifier le commentaire 3";
$l_copy = "Copier";

// Messages
$l_check_samecontact = "Ces contacts semblent similaires à cette nouvelle entrée !";
$l_link_deal = "Le contact est référencé par des affaires";
$l_link_deal_no = "Le contact n'est pas référencé dans les affaires";
$l_link_contract = "Le contact est référencé par des contrats";
$l_link_contract_no = "Le contact n'est pas référencé dans les contrats";
$l_link_list = "Le contact est enregistré dans des listes";
$l_link_list_no = "Le contact n'est pas référencé dans les listes";
$l_link_publication = "Le contact est abonné à des publications";
$l_link_publication_no = "Le contact n'est pas abonné à des publications";
$l_cant_delete = "Le contact ne peut être supprimé !";
$l_can_delete = "Le contact peut être supprimé.";
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des contacts correspondants.";
$l_found = "contacts trouvés.";
$l_no_found = "Aucun contact ne correspond à ces critères !";
$l_no_export = "Aucun contact privé à exporter !";
// Kind messages
$l_kind_link_contact = "Ces contacts sont du genre";
$l_kind_link_contact_no = "Pas de contacts du genre";
$l_kind_cant_delete = "Le genre ne peut être supprimé !";
$l_kind_can_delete = "Le genre peut être supprimé.";

$l_birthday_event_title = "Anniversaire de %s";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_lastname = "Vous devez renseigner le Nom avant de valider.";
$l_fill_company = "Vous devez sélectionner une société avant de valider.";
$l_fill_phone = "Vous devez renseigner le telephone avant de valider.";
$l_delete_confirm = "Confirmer la suppression du contact ?";
$l_j_check_phone = "Le numéro de téléphone n'est pas correct.";
$l_j_check_hphone = "Le numéro de téléphone personnel n'est pas correct.";
$l_j_check_mphone = "Le numéro de téléphone mobile n'est pas correct.";
$l_j_check_fax = "Le numéro de fax n'est pas correct.";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
$l_j_select_contact = "Vous devez sélectionner un contact !";
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
$l_renewal = "Renouvellement automatique";
$l_quantity = "Quantité";
$l_recept = "Mode de réception";
$l_quantity = "Quantité";
$l_quantity = "Quantité";
$l_no_found_subscription = "Aucun abonnement lié à ce contact !";
$l_remove = 'Supprimer';
$l_street = 'Rue';
$l_vcf_file = "Fichier VCF à importer" ;

$l_im = "Messagerie Instantanée";
$l_website = "Site internet";

$l_phone_labels = array ('HOME_VOICE' => 'Téléphone (domicile)', 'WORK_VOICE' => 'Téléphone (bureau)', 'CELL_VOICE' => 'Téléphone portable', 'HOME_FAX' => 'Fax (domicile)','WORK_FAX' => 'Fax (bureau)', 'PAGER' => 'Pager', 'OTHER' => 'Autre téléphone');
$l_email_labels = array ('INTERNET' => 'Internet', 'OTHER' => 'Autre');
$l_address_labels = array ('HOME' => 'Domicile', 'WORK' => 'Bureau', 'OTHER' => 'Autre');
$l_website_labels = array ('URL' => 'Site web', 'BLOG' => 'Log internet', 'OTHER' => 'Autre');
