<?php
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                   //
//     - File     : invoice.inc                                              //
// 2001-08-02 - AliaSource - Pierre Baudracco                                //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// invoice only 
$l_label_start = "Début de Label";
$l_amount = "Montant";
$l_amount_ht = "Montant HT";
$l_amount_ttc = "Montant TTC";
$l_expiration_date = "Date d'échéance";
$l_payment_date = "Date de paiment";
$l_expiration = "Echéance";
$l_credit_memo = "Avoir";
$l_paid = "Payé";
$l_waiting = "En attente";
$l_pick_deal = "Choisissez une affaire";
$l_inout = "Type";
$l_both = "Tous";
$l_tax_rate = "Taux TVA";
$l_compute_tax = "Calculer le montant TTC";
$l_include_archive = "Inclure les archives";
$l_amount_remaining = "Restant du";
$l_select_invoice = "Sélectionner la facture";
$l_bank = "Banque";
$l_model = "Modèle";
$l_reminder = "Relance";

// Invoice status
$l_invoicestatus_tocreate = 'A Créer';
$l_invoicestatus_sent = 'Envoyée';
$l_invoicestatus_received = 'Reçue';
$l_invoicestatus_ppaid = 'Partiellement Payée';
$l_invoicestatus_paid = 'Payée';
$l_invoicestatus_conflict = 'En conflit';
$l_invoicestatus_cancelled = 'Annulée';
$l_invoicestatus_loss = 'Perdue';
$l_invoicestatus_factor = 'Dailly';

// payment
$l_no_payment = "Aucun paiement connecté à cette facture";
$l_list_payment = " Paiements connectés à cette facture";

// Titres
$l_invoice_infos = "Informations à propos de la Facture";
$l_remaining_amount = "Montant non utilisé";
$l_to_use_amount = "Montant à affecter à cette Facture";
$l_affect = "Affecter";
$l_year_to_year = "Comparaison années";

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
$l_export_to_odf = "Exporter au format ODF";

// Messages 
$l_no_display = "Remplissez les critères de recherche et appuyez sur <Chercher> pour lancer la recherche";
$l_found = "Facture(s) trouvée(s).";
$l_no_found = "Aucune Facture ne correspond à votre requête";
$l_no_payment_found ="Auncun Paiement ne correspond à votre requête";
$l_payments_exist = "Il y a des payments reliés à cette facture !";
$l_invalid_payment = "The amount taken from a paiement is bigger than its total";

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

$l_err_paid_status = "La facture a été payée, l'état doit être à '$l_invoicestatus_paid' !";
$l_err_ppaid_status = "La facture a été partiellement payée, l'état doit être à '$l_invoicestatus_ppaid' ou '$l_invoicestatus_conflict' !";
$l_err_no_payment_status = "La facture n'a pas de paiement, l'état ne peut être '$l_invoicestatus_paid' ou '$l_invoicestatus_ppaid' !";
$l_err_status_received_customer = "L'état $l_invoicestatus_received n'est pas valide pour les factures de type $l_client !";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_check_number = "\\nLe Numéro de la Facture n'est pas valide !";
$l_check_money_ht = "\\n Le montant HT de la Facture est incorrect !";
$l_check_money_ttc = "\\n Le montant TTC de la Facture est incorrect !";
$l_check_amounts = "\\n Le montant TTC doit être supérieur au montant HT !";
$l_check_inout = "\\nVous devez choisir un type !";
$l_changing_inout = "You cannot change an invoice kind if there are \\n deals linked to it...Vous ne pouvez pas changer le type d'une Facture si elle est associée à des Affaires";
$l_no_number="\\nUne Facture doit avoir un numéro !";
$l_rate_nan = "Le taux TVA est invalide !";
$l_ht_nan = "Le montant Hors Taxes est invalide !";

// For the display parameters modification screen
$l_invoice_display = "Options d'affichage pour les listes de Factures"; 

// default invoice numbering policy :
// this is used as a date format
$default_invoice_numbering = "Y"; // we use only year

$l_j_select_invoice = "Vous devez sélectionner une facture !";

// ODF export error messages
$l_odf_load_err = "OBM a rencontré un problème lors du chargement du fichier.";
$l_odf_save_err = "OBM a rencontré un problème lors de la sauvegarde du fichier.";
$l_odf_choose_bank = "Veuillez choisir une banque afin d'exporter cette facture.";
$l_odf_choose_model = "Veuillez choisir un modèle afin d'exporter cette facture.";
