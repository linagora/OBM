<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : group.inc                                                //
// 2003-08-22 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_smb = "Windows";
$l_system = "Systeme";
$l_mailing = "Liste de diffusion";

// Labels et textes
$l_nb_user = "# Utilisateurs";
$l_group_user = "Ayant comme utilisateur";
$l_group_display = "Options d'affichage des listes de groupes";
$l_user_display = "Options d'affichage des listes d'utilisateurs";

// Contact
$l_lastname = "Nom";
$l_firstname = "Prénom";
$l_function = "Fonction";

$l_contacts = "Membres externes";

// Actions
$l_add = "Ajouter";

// Messages
$l_fill_name = "Le nom du groupe doit etre rempli";
$l_group_exists = "Ce groupe existe déjà !";
$l_j_check_email = "L'adresse e-mail n'est pas valide !";
$l_warn_delete = "utilisateurs enregistrés dans ce groupe";
$l_email_exist = "L'adresse E-mail saisie est déjà utilisée !";
$l_system_no_priv = "Un groupe système ne peut être privé !";
$l_only_owner_priv = "Seul le propriétaire d'un groupe peut le définir privé !";
$l_err_private_email = "Les groupes privés ne peuvent pas avoir d'adresse E-mail !";
$l_warn_group_diff_privacy = "Certains groupes n'ont pas été ajoutés. Vous essayez d'ajouter des groupes privés dans le groupe publique !";
$l_groupname_error = "Le nom du groupe est incorrect ! Seuls les lettres et chiffres sont autorisés, ainsi que les caractères '.', '-', '_' et espace";

$l_no_display = "Renseigner les critères souhaités et cliquer sur < Chercher > pour obtenir les groupes correspondants.";
$l_no_user = "Il n'y a pas d'utilisateurs membres.";
$l_no_contact = "Il n'y a pas de membres externes.";
$l_no_group_group = "Il n'y a pas de groupes membres.";
$l_found = "groupes trouvés.";
$l_user_member = "utilisateurs membres";
$l_contact_member = "membres externes";
$l_group_member = "groupes membres";
$l_no_found = "Aucun groupe ne correspond à ces critères !";
$l_add_user = "Ajouter des utilisateurs";
$l_add_contact = "Ajouter des membres externes";
$l_add_group = "Ajouter des groupes (parmi la liste des groupes possibles)";
$l_del_user_sel = "Enlever ces utilisateurs";
$l_del_contact_sel = "Enlever ces membres  externes";
$l_del_group_sel = "Enlever ces groupes";
$l_user_added = "Utilisateur(s) ajouté(s)";
$l_contact_added = "Membres(s) externes(s) ajouté(s)";
$l_user_removed = "Utilisateur(s) enlevé(s)";
$l_contact_removed = "Membres(s) externes(s) enlevé(s)";
$l_group_added = "Groupe(s) ajouté(s)";
$l_group_removed = "Groupe(s) enlevé(s)";
$l_no_user_added = "Pas d'utilisateurs à ajouter";
$l_no_contact_added = "Pas de membres externes à ajouter";
$l_no_user_deleted = "Pas d'utilisateurs à supprimer";
$l_no_contact_deleted = "Pas de membres externes à supprimer";
$l_no_group_added = "Pas de groupes à ajouter";
$l_no_group_deleted = "Pas de groupes à supprimer";
$l_check_samegroup = "Ces groupes sembles similaires au groupe saisi. Voulez-vous confirmer la création de ce groupe?";
$l_cant_delete_system = "Les groupes système ne peuvent être supprimés !";
$l_can_delete = "Le groupe peut être supprimé.";
$l_cant_delete = "Le groupe ne peut être supprimé !";
$l_err_system_samba = "La modification des droits windows d'un groupe système n'est pas autorisée.";
$l_err_cant_delete_default_user_group = "Le groupe est le groupe par défaut d'utilisateurs, il ne peut donc pas être supprimé !";
$l_cant_delete_domain_group = "Les groupes de domaine sont gérés automatiquement par OBM et ne peuvent être supprimés.";
$l_cant_update_domain_group = "Les groupes de domaine par défaut sont gérés automatiquement par OBM et ne peuvent être modifiés.";
$l_add_groups = "Ajouter des groupes";

$l_public_book_not_writable = "Le carnet d'adresses public doit être accessible en écriture pour effectuer cette action.";
