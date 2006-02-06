<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : invoice.inc                                              //
// 2001-08-02 Aliacom - Nicolas Roman                                        //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Labels et textes
// invoice only 
$l_label_start = "Début de Label";
$l_amount = "Montant";
$l_amount_ht = "Montant HT";
$l_amount_ttc = "Montant TTC";
$l_expiration_date = "Date d'échéance";
$l_payment_date = "Date de paiment";
$l_date_apres = "Ayant la date après";
$l_date_avant = "Ayant la date avant";
$l_paid = "Montant TTC payé";
$l_pick_deal = "Choisissez une affaire";
$l_inout = "Type";
$l_both = "Tous";
$l_tax_rate = "Taux TVA";
$l_compute_tax = "Calculer le montant TTC";
$l_include_archive = "Inclure les archives";
$l_potential = "Potentiel";

//payment
$l_no_payment = "Aucun paiement connecté à cette facture";
$l_list_payment = " Paiements connectés à cette facture";
$l_expected_date = "Date Prévisionnelle";

// Titres
$l_invoice_infos = "Informations à propos de la Facture";
$l_remaining_amount = "Montant non utilisé";
$l_to_use_amount = "Montant à affecter à cette Facture";
$l_affect = "Affecter";

// Actions 
$l_insert = "Insérer la Facture";
$l_update = "Mettre à jour la Facture";
$l_duplicate = "Dupliquer cette Facture";
$l_delete = "Supprimer la Facture";
$l_delete_confirm = "Etes vous sur de vouloir supprimer cette facture ?";
$l_no_number_confirm = "Vous allez créer une facture sans Numéro. Cliquez sur OK pour continuer.";
$l_invoice_del_deal = "Supprimer l'association";
$l_invoice_del_paiement = "Supprimer l'association";
$l_invoice_infos = "Voir la Facture";
$l_add_payment = "Add paiement";
$l_add_payment_chosen = "Add chosen paiements";
$l_check_payment_chosen = "Check chosen paiements";
$l_del_payment = "Remove paiement";
$l_del_payment_chosen = "Delete chosen paiements";
$l_payments_todel = "Paiements associés à cette facture" ;
$l_invoice_mod = "Modifier la Facture";

// Messages 
$l_no_display = "Remplissez les critères de recherche et appuyez sur <Chercher> pour lancer la recherche";
$l_no_deal_display = "Remplissez les critères de recherche et appuyez sur <$l_find> pour lancer la recherche des Affaires";
$l_no_payment_display = "Remplissez les critères de recherche et appuyez sur <$l_find> pour lancer la recherche des ";
$l_found="Facture(s) trouvée(s).";
$l_no_found="Aucune Facture ne correspond à votre requête";
$l_no_deal_found ="Aucune Affaire ne correspond à votre requête";
$l_no_payment_found ="Auncun Paiement ne correspond à votre requête";
$l_delete_ok = "La Facture a bien été supprimée";
$l_delete_error = "La suppression de la Facture a échoué !";
$l_payments_exist = "Il y a des payments reliés à cette facture !";
$l_remove_deal_ok = "L'association Affaire Facture a bien été supprimé !";
$l_remove_deal_error = "La suppression de l'association Affaire Facture a échoué !";
$l_insert_ok = "La Facture a bien été insérée.";
$l_insert_error = "L'insertion de la Facture a échoué !";
$l_insert_deal_ok = "L'association Affaire Facture a bien été insérée !";
$l_insert_deal_error = "L'insertion de l'association Affaire Facture a échoué !";
$l_update_ok = "La Facture a bien été mise à jour.";
$l_update_error = "La mise à jour de la Facture a échoué !";
$l_query_error = "Problème durant l'exécution de la requête...";
$l_invalid_payment = "The amount taken from a paiement is bigger than its total";
$l_too_much_money = "The total amount of paiements is bigger than the invoice total.".
" If you carry on, this invoice will reach status 'trouble'";
$l_error_try_again = "There are errors !! Please restart from the beginning";
$l_try_again = "Try Again !";

$l_link_payment = "Ces paiments sont liés à cette facture";
$l_link_payment_no = "La facture n'est liée à aucun paiment";
$l_cant_delete = "La facture ne peut être supprimée !";
$l_can_delete = "La facture peut être supprimée.";

$l_fill_label = "Le label doit être renseigné !";
$l_fill_number = "Le numéro doit être renseigné (pour l'état choisi) !";
$l_fill_company = "La société doit être renseignée !";
$l_fill_date = "La date doit être renseignée (pour l'état choisi) !";
$l_fill_inout = "Le type doit être sélectionné : $l_client ou $l_supplier !";
$l_fill_amount = "Les montants doivent être correctement renseignés !";
$l_error_number_exist = "Ce numéro de facture est déjà attribué !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_check_number = "\\nLe Numéro de la Facture n'est pas valide !";
$l_check_money_ht = "\\n Le montant HT de la Facture est incorrect !";
$l_check_money_ttc = "\\n Le montant TTC de la Facture est incorrect !";
$l_check_amounts = "\\n Le montant TTC doit être supérieur au montant HT !";
$l_check_inout = "\\nVous devez choisir un type !";
$l_changing_inout = "You cannot change an invoice kind if there are \\n deals linked to it...Vous ne pouvez pas changer le type d'une Facture si elle est associée à des Affaires";
$l_no_number="\\nUne Facture doit avoir un numéro !";
$l_check_deal = "Vous devez sélectionner une Affaire";
$l_select_deals = "You have to select a number of deals";
$l_rate_nan = "Le taux TVA est invalide !";
$l_ht_nan = "Le montant Hors Taxes est invalide !";

// For the display parameters modification screen
$l_invoice_display = "Options d'affichage pour les listes de Factures"; 

// default invoice numbering policy :
// this is used as a date format
$default_invoice_numbering = "Y"; // we use only year

// default tax rate 
// used like that : TTC = HT * $default_tax
$default_tax= 1.196;
