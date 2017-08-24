<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : people.inc                                               //
// 2008-10-06 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: people.inc 3097 2008-09-22 14:22:46Z benoitc $ //
///////////////////////////////////////////////////////////////////////////////

$l_login = "Identifiant";
$l_password = "Mot de passe";
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
$l_delegation = 'Délégation';
$l_vacation = 'Absent';

$l_perms = "Autorisé";
$l_forbidden = "Interdit";
$l_group_member = "Membre des groupes";
$l_password_type = "Chiffrage mdp";

// Actions
$l_insert = "Enregistrer l'utilisateur";
$l_update = "Modifier l'utilisateur";
$l_update_group = "Modifier les groupes de l'utilisateur";
$l_checkdelete = "Vérifier ou Supprimer l'utilisateur";
$l_reset = "Préférences par défaut";
$l_delete = "Supprimer l'utilisateur";
$l_add = "Ajouter ces utilisateurs";

$l_add_users = "Ajouter des utilisateurs";

// Messages
$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir la liste des utilisateurs correspondants.";
$l_found = "utilisateurs trouvés.";
$l_no_found = "Aucun utilisateur ne correspond à ces critères !";
$l_reset_ok = "Les préférences de l'utilisateur ont été remises aux valeurs par défaut";
$l_can_delete = "L'utilisateur peut être supprimé.";
$l_cant_delete = "L'utilisateur ne peut être supprimé !";
$l_link_timetask = "L'utilisateur est référencé dans la gestion du temps sur des projets";
$l_link_timetask_no = "L'utilisateur n'est pas référencé dans la gestion du temps sur des projets";

$l_login_error = "Le login doit être correctement renseigné ! Il ne doit pas être vide ou contenir de caractères spéciaux";
$l_login_in_use = "Le login est déjà attribué à un autre utilisateur !";
$l_password_empty = "Le mot de passe doit être renseigné et valide - de 4 à 12 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
$l_password_error = "Le mot de passe n'est pas valide : de 4 à 12 caractères (alphanumériques ou -\$\&~#{([|_`^@);]+=}%!:/.,?<>\") !";
$l_profile_privilege_error = "Privilèges insuffisants pour manipuler ce niveau de profil !";
$l_last_admin_error = "Le profil du dernier administrateur ne peut être modifié !";
$l_email_error = "L'adresse E-mail saisie n'est pas valide !";
$l_email_empty = "Vous devez saisir une adresse E-mail afin d'activer la messagerie !";
$l_email_exist = "L'adresse E-mail saisie est déjà utilisée !";
$l_uemail_exist = "L'adresse E-mail est saisie plusieurs fois !";
$l_check_sameuser = "Ces utilisateurs semblent similaires à cette nouvelle entrée !";
$l_exist_error = "Un utilisateur portant ce nom existe déjà !";
$l_lname_error = "Le nom doit être correctement renseigné !";
$l_image_type_error = "La photo doit être un fichier de type image (gif, jpeg ou png)";
$l_image_size_error = "La photo ne doit pas dépasser 96px x 96px et avoir un poids supérieur à 100ko";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_fill_login = "Vous devez renseigner le login avant de valider.";
$l_fill_password = "Vous devez renseigner le mot de passe avant de valider.";
$l_delete_confirm = "Confirmer la suppression de l'utilisateur ?";
$l_j_check_email = "L'adresse email n'est pas valide. Vérifiez le domaine.";
$l_change_login_confirm = "Vous avez modifié le login de l'utilisateur.\\nIl est *impératif* que l'utilisateur dont le login a été modifié soit déconnecté de toutes applications liées à OBM lors de la validation de cette modification (OBM, client de messagerie, webmail, connecteur).\\nConfirmer le changement d'identifiant ?";


$l_people_display = "Options d'affichage des listes d'utilisateurs";
