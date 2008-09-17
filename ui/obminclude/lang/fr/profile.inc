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

// Messages d'erreurs
$l_profile_name_error = "Le nom du profil est invalide, veuillez saisir de 1 à 64 caractères alphanumériques ou '_' '.' '-' !";
$l_profile_name_empty = "Vous devez saisir un nom de profil : 1 à 64 caractères alphanumériques ou '_' '.' '-' !";

$l_level_empty = "Le champ Niveau est vide. Veuillez le remplir avec un entier positif !";
$l_level_managepeers_empty = "Le champ Gestion d'utilisateurs de même niveau est vide. Veuillez le remplir avec 0 ou 1 !";
$l_access_restriction_empty = "Le champ Restriction d'accès est vide. Veuillez le remplir avec ALLOW_ALL ou DENY_ALL !";

$l_profile_level_error = "Erreur sur le champ Niveau. Veuillez le remplir avec un entier positif !";
$l_profile_level_managepeers_error = "Erreur sur le champ Niveau de gestion pairs. Les valeurs acceptés sont 0 ou 1 !";
$l_profile_access_restriction_error = "Erreur sur le champ Restriction d'accès. Les valeurs acceptés sont ALLOW_ALL ou DENY_ALL !";