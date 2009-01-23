<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : profile.inc                                              //
// 2008-09-12 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 2972 2008-07-02 09:42:54Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////

// Labels des propriétés
$l_level = "Niveau";
$l_level_managepeers = "Gestion d'utilisateurs de même niveau";
$l_access_restriction = "Restriction d'accès";
$l_access_exeptions = "Exceptions de restriction d'accès";
$l_admin_realm = "Étendue de l'administration";

// Messages d'erreurs
$l_profile_name_error = "Le nom du profil est invalide, veuillez saisir de 1 à 64 caractères alphanumériques ou '_' '.' '-' !";
$l_profile_name_empty = "Vous devez saisir un nom de profil : 1 à 64 caractères alphanumériques ou '_' '.' '-' !";

$l_level_empty = "Le champ Niveau est vide. Veuillez le remplir avec un entier positif !";
$l_level_managepeers_empty = "Le champ Gestion d'utilisateurs de même niveau est vide. Veuillez le remplir avec 0 ou 1 !";
$l_access_restriction_empty = "Le champ Restriction d'accès est vide. Veuillez le remplir avec ALLOW_ALL ou DENY_ALL !";

$l_profile_level_error = "Erreur sur le champ Niveau. Veuillez le remplir avec un entier positif !";
$l_profile_level_managepeers_error = "Erreur sur le champ Niveau de gestion pairs. Les valeurs acceptés sont 0 ou 1 !";
$l_profile_access_restriction_error = "Erreur sur le champ Restriction d'accès. Les valeurs acceptés sont ALLOW_ALL ou DENY_ALL !";
$l_profile_admin_realm_error = "Erreur sur le champ Étendue de l'administration, veuillez saisir de 1 à 64 caractères alphanumériques ou '_' '.' '-' séparés par un espace!";

$l_profile_default_permission_empty = "Erreur sur les permissions par défaut. Veuillez cocher au moins une permission !";

// Infos
$l_admin_realm_info = "* Les valeurs acceptés pour Étendue de l'administration sont :<blockquote><i>user</i>, <i>delegation</i>, <i>domain</i></blockquote>ou bien une combinaison de ces valeurs séparés par un espace.";

$l_profile_delete_warning = "Suppression du profil non permise ! Au moins un utilisateur est affecté au profil.";

// Labels de formulaires
$l_module_name = "Module";
$l_right_default = "Par défaut";
$l_right_read = "Lecture";
$l_right_write = "Ecriture";
$l_right_own = "Proprio";
$l_right_read_admin = "Lecture Adm";
$l_right_write_admin = "Ecriture Adm";
$l_default_permissions = "Permissions par défaut";
$l_general_properties = "Propriété principale";
$l_section_disable = "Désactiver";
$l_section_enable = "Activer";
$l_active_section = "Activer sections";
$l_section_all = "Toutes";
$l_section_none = "Aucune";
$l_found = "profiles trouvés.";
$l_no_found = "Aucun profile ne correspond à ces critères !";

$l_user = "Lecture";
$l_editor = "Écriture";
$l_admin = "Administration";

$l_modules_right = "Droits sur les modules";
$l_sections_right = "Affichage des sections";
$l_default_settings = "Réglages par défauts";
$l_specific_settings = "Réglages spécifiques";
$l_section_enabled = "Etat de la section";

$l_informations = "Informations";

$l_add_module_rights = "Ajouter des droits spécifiques à un module";
$l_add_section_enabled = "Ajouter un réglage pour une section";
