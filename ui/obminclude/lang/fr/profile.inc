<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : profile.inc                                              //
// 2008-09-12 Vincent Bernard                                                //
///////////////////////////////////////////////////////////////////////////////
// $Id: profile.inc 2972 2008-07-02 09:42:54Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////



// Messages d'erreurs
$l_profile_name_error = "Le nom du profil est invalide, veuillez saisir de 1 à 64 caractères alphanumériques ou '_' '.' '-' !";
$l_profile_name_empty = "Vous devez saisir un nom de profil : 1 à 64 caractères alphanumériques ou '_' '.' '-' !";

$l_level_empty = "Le champ Niveau est vide. Veuillez le remplir avec un entier positif !";

$l_profile_delete_warning = "Suppression du profil non permise ! Au moins un utilisateur est affecté au profil.";

// Labels de formulaires
$l_module_name = "Module";
$l_right_default = "Par défaut";
$l_right_read = "Lecture";
$l_right_write = "Ecriture";
$l_right_own = "Proprétaire";
$l_right_read_admin = "Lecture Adm";
$l_right_write_admin = "Ecriture Adm";
$l_default_permissions = "Permissions par défaut";
$l_general_properties = "Propriété principale";
$l_section_disable = "Désactiver";
$l_section_enable = "Activer";
$l_active_section = "Activer sections";
$l_section_all = "Toutes";
$l_section_none = "Aucune";
$l_found = "profils trouvés.";
$l_no_found = "Aucun profil ne correspond à ces critères !";

$l_user = "Lecture";
$l_editor = "Écriture";
$l_admin = "Administration";

$l_quota_none = "aucun";

$l_modules_right = "Droits sur les modules";
$l_sections_right = "Affichage des sections";
$l_default_settings = "Réglages par défauts";
$l_specific_settings = "Réglages spécifiques";
$l_section_enabled = "Etat de la section";

$l_informations = "Informations";

$l_add_module_rights = "Ajouter des droits spécifiques à un module";
$l_add_section_enabled = "Ajouter un réglage pour une section";

$l_default_right = "Statut par défaut de la case à cocher 'Publique' dans l'écran de gestion des droits";
$l_checked = 'Cochée';
$l_unchecked = 'Décochée';
$l_checked_disabled = 'Cochée désactivée';
// Labels des propriétés
$l_level = "Niveau de gestion";
$l_help_level = "Un utilisateur avec ce profil ne pourra modifier que les utilisateurs ayant un profil de niveau supérieur à celui de son profil";
$l_level_managepeers = "Gestion d'utilisateurs de même niveau";
$l_help_level_managepeers = "Un utilisateur avec ce profil pourra modifier les utilisateurs ayant un profil de même niveau que le sien si cette option est activée";
$l_access_restriction = "Politique de sécurité des connexions";
$l_help_access_restriction = "Définit la politique de sécurité par défaut de connexion des utilisateurs de ce profil.";
$l_user_results_limit = "Nombre maximum de résultats dans la recherche d'utilisateurs";
$l_help_results_limit = "Limite le nombre de résultats d'une recherche d'utilisateurs, laisser vide pour supprimer la limite";
$l_access_exceptions = "Exceptions à la politique de sécurité";
$l_help_access_exceptions = "Définit les ip ou plages ip faisant exceptions à la politique de sécurité.";
$l_admin_realm = "Étendue de l'application de l'outil de mise à jour";
$l_help_admin_realm = "Lorsque un utilisateur de ce profil exécutera la mise à jour système, ne seront pris en compte que les modifications répondant à ces critères.";
$l_deny_connect = 'Refuser la connexion par défaut';
$l_allow_connect = 'Autoriser la connexion par défaut';
$l_none_f = 'Aucune';
$l_lower = 'Le plus faible';
$l_higher = 'Le plus fort';
$l_realm_user = 'Ses propres modifications';
$l_realm_delegation = 'Les modifications sur sa délégation';
$l_realm_domain = 'Toutes les modifications du domaine';
$l_profile_exists= "Ce nom de profil est déjà utilisé";
$l_mail_quota_max= "Quota maximum pour la boite mail";
$l_help_mail_quota_max = "Définit la valeur maximum du quota des adresses mails des utilisateurs de ce profil, la valeur 0 correspond à aucune quota.";
$l_mail_quota_default= "Quota par défaut pour la boite mail";
$l_help_mail_quota_default = "Définit la valeur par défaut du quota des adresses mails des utilisateurs de ce profil, la valeur 0 correspond à aucun quota.";
$l_mail_quota_error = "Le quota par défaut ne peux pas être supèrieur au quota maximum";
$l_max_quota = 'Valeur max';

$l_profile_exists= "Ce nom de profil est déjà utilisé";

$l_profile_public_access="Accès public";
$l_profile_public_read="Lecture publique";
$l_profile_public_write="Écriture publique";
