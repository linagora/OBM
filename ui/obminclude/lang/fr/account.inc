<SCRIPT language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Frenc                                                    //
//     - File     : account.inc                                              //
// 2001-07-31 Nicolas Roman                                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label = "Label";
$l_label_start = "Début du Label";
$l_last_account = "Dernier Compte";
$l_bank = "Banque"; 
$l_number = "Numéro";
$l_balance = "Solde";
$l_initial_balance = "Solde Initial";
$l_comment = "Commentaire";
$l_choose_balance_date = "Date de calcul du Solde";
$l_date = "Date";
$l_expected_date = "Date Prévisionnelle";
$l_amount = "Montant";


$l_include_expected = "Inclure les opérations prévisionnelles ?";
$l_in_total ="Total des entrées";
$l_out_total = "Total des sorties";
$l_total ="Solde Estimé";
$l_old_balance = "Solde Actuel";
$l_nb_expected_used = " opérations prévisionnelles utilisées pour le calcul.";
$l_expected = "prévisionnel";
$l_used_payments = "Paiement utilisés pour calculer le solde";
$l_used_expected_payments = "Paiement prévisionnels utilisés pour calculer le solde";
$l_expected_payments = "Paiements prévisionnels";
$l_payments = "Paiements";
$l_totals = "Totaux";

// Titres
$l_account = "Comptes";

// Actions
$l_insert= "Insérer le Compte";
$l_update = "Mettre à jour le Compte";
$l_delete = "Supprimer le Compte";
$l_delete_confirm = "Etes vous sur de vouloir supprimer ce Compte ?";
$l_compute_balance = "Calculer le solde";

// Messages 
$l_no_display = "Remplissez les critères de recherche et appuyez sur <$l_find> pour lancer la recherche";
$l_found="Compte(s) trouvé(s).";
$l_no_found="Aucun Compte ne correspond à votre requête";
$l_delete_ok = "Le compte a bien été supprimé.";
$l_delete_error = "La suppression du compte a échoué !";
$l_insert_ok = "Le Compte a bien été inséré.";
$l_insert_error = "L'insertion du compte a échoué !";
$l_update_ok = "Le compte a bien été mis à jour.";
$l_update_error = "La mise à jour du compte a échoué !";
$l_query_error = "Problème durant l'exécution d'une requête...";
$l_impossible_deletion = " paiements sont liés à ce compte. La suppression est impossible"; // must be used with the number of payments leading...

$l_time_format = "%Y-%m-%d %H:%i:%s";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_check_number = "\\nLe numéro du compte n\\'est pas valide !";
$l_fill_number = "Vous devez fournir un numéro de compte !";
$l_fill_label = "Vous devez fournir un label !";
$l_fill_bank = "Vous devez renseigner le champ banque !";
$l_confirm_no_bank="Ce Compte n\\'est associé à aucune banque. Cliquez sur OK pour confirmer .";
$l_invalid_date="\\n Format de date invalide (MM/JJ/AAAA or AAAA-MM-JJ)";
$l_fill_date = "\\n Vous devez fournir une date.";

// For the display parameters modification screen
$l_account_options = "Options d'affichage pour les listes de Comptes"; 
$l_payment_options = "Options d'affichage pour les listes de Paiements"; 
$l_options_dis = "Options d'affichage" ;

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["account_bank"] = $l_bank;
$fieldnames["account_number"] = $l_number;
$fieldnames["account_label"] = $l_label;
$fieldnames["account_balance"] = $l_initial_balance;
$fieldnames["account_today"] = $l_balance;
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_amount"] = $l_amount;
