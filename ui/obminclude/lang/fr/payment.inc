<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : payment.inc                                              //
// 2001-08-02 AliaSource                                                     //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label_start = "Début du Label";
$l_amount = "Montant"; 
$l_expected_date = "Date Prévisionnelle";
$l_type = "Type du Paiement";
$l_inout = "Type";
$l_both = "Both"; 
$l_pick_paymentkind = "Choisissez un type de Paiement";
$l_kind = "Mode";
$l_pick_account = "Choisissez un Compte";
$l_payment_account = "Compte";
$l_paid = "Payé";
$l_unpaid = "Non Payé";
$l_already_paid = "Déjà payé";
$l_choose_csv = "Choisir ce fichier";
$l_include_checked = "Inclure les Paiements pointés";

$l_received = "Reçu";
$l_emitted = "Emis";

// invoice
$l_no_invoice = "Aucune facture associée à ce paiement";
$l_invoice_list = "Factures associées à ce paiement";

// Actions 
$l_insert = "Insérer le Paiement";
$l_update = "Mettre à jour le Paiement";
$l_delete = "Supprimer le Paiement";
$l_delete_confirm = "Etes vous sur de vouloir supprimer ce Paiement ?";
$l_duplicate = "Dupliquer ce Paiement";

// Messages 
$l_no_display = "Remplissez les critères de recherche et appuyez sur <Chercher> pour lancer la recherche";
$l_found = "Paiements trouvés.";
$l_no_found = "Aucun Paiement ne correspond à votre requête";
$l_no_invoice = "Aucune Facture liée à ce paiment";
$l_cant_delete = "Le paiement ne peut être supprimé !";
$l_can_delete = "Le paiement peut être supprimé.";
$l_invoice_added = "L'association Paiement/Facture a été correctement créée";
$l_no_invoice_added = "L'association Paiement/Facture a échoué !";


$l_fill_company = "La société doit être renseignée !";
$l_fill_amount = "Le montant doit être correctement renseigné !";
$l_fill_inout = "Le type doit être sélectionné : (+) $l_received ou (-) $l_emitted !";
$l_fill_kind = "Le mode de paiment doit être sélectionné !";
$l_invalid_amount = "Le montant entré est invalide !";
$l_err_total_payment_affected = "Le montant total affecté est supérieur au paiement !";
$l_err_affected_invoice = "Le montant affecté à la facture est supérieur au montant restant à payer pour la facture !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_no_number = "\\nLe numérod u Paiement est invalide !";
$l_check_amount = "\\Le montant est invalide !";
$l_check_inout = "\\nVous deve choisir entre Paiement Client ou Paiement Fournisseur !";
$l_check_paymentkind = "Vous devez choisir un type de Paiement !";
$l_check_account = "Vous devez choisir un Compte !";
$l_no_amount = "Vous devez entrer un montant pour ce Paiement ";

// For the display parameters modification screen
$l_payment_options = "Options d'affichage pour les listes de Paiements";
$l_invoice_options = "Options d'affichage pour les listes de Factures";
