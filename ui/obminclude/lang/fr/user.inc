<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : user.inc                                                 //
// 1999-01-24 Florent Goalabre                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_login = "Identifiant";
$l_gid = "Groupe principal";
$l_password = "Mot de passe";
$l_password_update = "Modification mot de passe";
$l_lastaccess = "Dernier accès";
$l_phone2 = "$l_phone 2";
$l_fax2 = "$l_fax 2";
$l_email = "@M";
$l_datebegin = "Date d'entrée";
$l_dateexp = "Date expiration";
$l_noexperie = "N'expire jamais";
$l_account_dateexp = "Date expiration";
$l_exp = "Jour avant expiration";
$l_perms_user = "Utilisateur";
$l_perms_editor = "Editeur";
$l_perms_admin = "Administrateur";
$l_infos = "Informations";
$l_check_usercreate = " entrées créées par cet utilisateur.";
$l_check_userupdate = " entrées modifiées par cet utilisateur.";
$l_no_user = "Pas de champs utilisateurs dans cette table";
$l_group = "Groupe";
$l_delegation_target = "Administration déléguée";
$l_photo = "Photo";
$l_delete_userobmphoto = "Supprimer la photo de l'utilisateur";

$l_quota = "Quota";
$l_max_quota = "Valeur max";
$l_administration = "Administration";
$l_serv_mail = "Serveur de mail";
$l_create_date = "Date d'entrée";

$l_hidden = 'BAL cachée';
$l_mail_ext = "Messagerie externe";
$l_mail_server = "Serveur de boîte à lettres";
$l_email_nomade = "Adresse E-mail nomade";
$l_nomade = "Accès E-mail nomade";
$l_access_nomade = "Accès nomade";
$l_perms = "Autorisé";
$l_local_copy = "Copie locale";
$l_vacation = "Message d'absence";
$l_vacation_message = "Message";
$l_vacation_datebegin = "Du";
$l_vacation_dateend = "Au";
$l_enable_auto_vacation = "Programmer l'activation automatique du message d'absence" ;
$l_forbidden = "Interdit";
$l_access = "Accès Internet";
$l_host = "Hôte";
$l_user_select_host = "Choix de la machine de l'utilisateur";
$l_host_enable = "Activé sur l'hote";
$l_web = "Accès Web";
$l_web_mode = "Accès web autorisés";
$l_web_list = "Liste d'accès";
$l_web_all = "Tous sauf liste";
$l_web_list_only = "Uniquement liste";
$l_web_consult_list = "Consulter la liste d'url";
$l_smb = "Réseaux Windows";
$l_smb_perms = "Utilisateur Windows";
$l_smb_home = "Répertoire home";
$l_smb_home_drive = "Lecteur home";
$l_smb_logon = "Script de logon";
$l_group_member = "Membre des groupes";
$l_password_type = "Chiffrage mdp";
$l_mailserver_auto = "Automatique";
$l_download_user_card = 'Télécharger la fiche utilisateur';
$l_create_using_userpattern = "Créer d'après un modèle";

// Actions
$l_insert = "Enregistrer l'utilisateur";
$l_update = "Modifier l'utilisateur";
$l_update_valid = "Modifier et Valider l'utilisateur";
$l_update_group = "Modifier les groupes de l'utilisateur";
$l_checkdelete = "Vérifier ou Supprimer l'utilisateur";
$l_reset = "Préférences par défaut";
$l_delete = "Supprimer l'utilisateur";
$l_add = "Ajouter ces utilisateurs";
$l_generate_passwd = "Générer";

$l_add_users = "Ajouter des utilisateurs";

$l_edit_users = "Éditer les propriétés utilisateurs";

$l_do_batch_processing = "Lancer le traitement par lots";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des utilisateurs correspondants.";
$l_found = "utilisateur(s) trouvé(s).";
$l_no_found = "Aucun utilisateur ne correspond à ces critères !";
$l_user_results_limit_message = "%d utilisateur(s) trouvé(s), seuls %d sont affichés conformément à la configuration du profil";
$l_reset_ok = "Les préférences de l'utilisateur ont été remises aux valeurs par défaut";
$l_can_delete = "L'utilisateur peut être supprimé.";
$l_cant_delete = "L'utilisateur ne peut être supprimé !";
$l_link_timetask = "L'utilisateur est référencé dans la gestion du temps sur des projets";
$l_link_timetask_no = "L'utilisateur n'est pas référencé dans la gestion du temps sur des projets";

$l_login_error = "Le login doit être correctement renseigné ! Il ne doit pas être vide ou contenir de caractères spéciaux";
$l_login_in_use = "Le login est déjà attribué à un autre utilisateur !";
if($GLOBALS['cgp_use']['service']['samba']) {
  $l_password_error = "Le mot de passe n'est pas valide : de 4 à 12 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
  $l_password_empty = "Le mot de passe doit être renseigné et valide - de 4 à 12 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
} else {
  $l_password_error = "Le mot de passe n'est pas valide : de 4 à 20 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
  $l_password_empty = "Le mot de passe doit être renseigné et valide - de 4 à 20 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
}
$l_profile_privilege_error = "Privilèges insuffisants pour manipuler ce niveau de profil !";
$l_last_admin_error = "Le profil du dernier administrateur ne peut être modifié !";
$l_mail_perms_error = "La messagerie interne doit être activée afin de bénéficier de la messagerie externe !";
$l_mail_server_error = "Le serveur de mail est invalide ou aucun serveur de mail n'est défini dans le domaine, veuillez contacter l'administrateur du domaine !";
$l_email_error = "L'adresse E-mail saisie n'est pas valide !";
$l_email_empty = "Vous devez saisir une adresse E-mail afin d'activer la messagerie !";
$l_email_exist = "L'adresse E-mail saisie est déjà utilisée !";
$l_uemail_exist = "L'adresse E-mail est saisie plusieurs fois !";
$l_quota_error = "Le quota de messagerie saisi n'est pas valide !";
$l_err_web_all = "Le mode d'accès Web n'a pas été précisé !";
$l_nomade_without_mail_perms = "L'E-mail nomade ne peut pas être autorisé si la messagerie n'est pas autorisée !";
$l_nomade_perms_error = "L'E-mail nomade ne peut pas être activé s'il n'est pas autorisé !";
$l_nomade_local_copy_error = "La copie locale ne peut être activée si l'adresse de redirection n'est pas activée";
$l_email_nomad_error = "L'E-mail nomade ne peut pas être activé si l'adresse E-mail nomade n'est pas renseignée !";
$l_email_nomad_error_loop = "L'E-mail nomade ne doit pas correspondre à une adresse email de l'utilisateur !";
$l_vacation_without_mail_perms = "Le message d'absence ne peut pas être activé si la messagerie n'est pas autorisée !";
$l_empty_vacation_message = "Le message d'absence ne peut être vide quand l'envoi est activé !" ;
$l_check_sameuser = "Ces utilisateurs semblent similaires à cette nouvelle entrée !";
$l_exist_error = "Un utilisateur portant ce nom existe déjà !";
$l_lname_error = "Le nom doit être correctement renseigné !";
$l_err_home = "Le répertoire Home Windows est incorrect !";
$l_err_drive = "Le lecteur Home WIndows est incorrect !";
$l_err_system_samba = "La modification des droits windows d'un utilisateur système n'est pas autorisée.";
$l_err_host_already_used_by = "La machine est déjà affectée à l'utilisateur ";
$l_image_type_error = "La photo doit être un fichier de type image (gif, jpeg ou png)";
$l_image_size_error = "La photo ne doit pas dépasser %spx x %spx et avoir un poids supérieur à %sko";
$l_err_profile = "Ce profil d'utilisateur n'existe pas";
$l_user_import_row_cols_error = "Ligne invalide. Nombre de colonnes incorrect !";
$l_batch_no_user_error = "Aucun utilisateur n'est sélectionné pour le traitement par lots";
$l_phone_error = "Le numéro de téléphone est incorrect";
$l_fax_error = "Le numéro de fax est incorrect";
$l_zip_error = "Le code postal est incorrect";
$l_expresspostal_error = "Cedex est incorrect";
$l_coord_same_phone_error = "Vous devez saisir des numéros de téléphone différents !";
$l_coord_same_fax_error = "Vous devez saisir des numéros de fax différents !";
$l_user_delete_himself_error = "Vous ne pouvez pas vous supprimer vous-même !";
$l_user_quota_error = "Le quota du mail est supérieur au quota max défini dans le profil !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_login = "Vous devez renseigner le login avant de valider.";
$l_fill_password = "Vous devez renseigner le mot de passe avant de valider.";
$l_delete_confirm = "ATTENTION : Vous allez supprimer un utilisateur ! Cette opération est irréversible.\\nVoulez-vous continuer ?";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
$l_change_login_confirm = "Vous avez modifié le login de l'utilisateur.\\nIl est *impératif* que l'utilisateur dont le login a été modifié soit déconnecté de toutes applications liées à OBM lors de la validation de cette modification (OBM, client de messagerie, webmail, connecteur).\\nConfirmer le changement d'identifiant ?";
$l_j_coord_same_phone_error = "Vous devez saisir des numéros de téléphone différents !";
$l_j_coord_same_fax_error = "Vous devez saisir des numéros de fax différents !";
$l_change_password_confirm = "Vous allez écraser le mot de passe déjà présent. <br /> Voulez-vous continuer ?";

$l_user_display = "Options d'affichage des listes d'utilisateurs";

$l_users_not_updatable_error = "Vous n'avez pas la permission de mettre à jour certains utilisateurs sélectionnés";
