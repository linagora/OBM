<SCRIPT language="php">
//////////////////////////////////////////////////////////////////////////////
// OBM - Language : French                                                  //
//     - File     : invoice.inc                                             //
// 2001-08-02 Nicolas Roman                                              //
//////////////////////////////////////////////////////////////////////////////

// Labels et textes
// payment only stuff
$l_label = "Label";
$l_label_start = "Début du Label";
$l_last_payment = "Dernier Paiement";
$l_number = "Numéro";
$l_amount = "Montant"; 
$l_comment = "Commentaire";
$l_date = "Date";
$l_expected_date = "Date Prévisionnelle";
$l_date_apres = "Ayant la date après";
$l_date_avant = "Ayant la date avant";
$l_account = "Compte";
$l_all = "Tout";
$l_all_f = "Toutes";
$l_type = "Type du Paiement";
$l_inout = "Type";
$l_both = "Both"; 
$l_client = "Client";
$l_fournisseur = "Fournisseur";
$l_pick_paymentkind = "Choisissez un type de Paiement";
$l_kind = "Type";
$l_pick_account = "Choisissez un Compte";
$l_payment_account = "Compte";
$l_status = "Etat";
$l_paid = "Payé";
$l_unpaid = "Non Payé";
$l_choose_csv = "Choisir ce fichier";
$l_include_checked = "Inclure les Paiements pointés";

// invoice connected : 
$l_add_invoices = "Associer à des Factures";
$l_invoice_label = "Label";
$l_invoice_date = "Date";
$l_invoice_TTC = "Montant TTC";
$l_invoice_HT = "Montant HT";
$l_invoice_number = "Numéro"; 
$l_invoice_status = "Etat";
$l_invoice_not_paid_yet = "Montant impayé AVANT";
$l_invoice_still_not_paid = "Montant impayé MAINTENANT";
$l_invoice_share = "Part Affectée";
$l_invoice_inout = "Type";
$l_invoice_paid = "Montant déjà payé";
$l_already_used_share = "Montant du paiement déjà utilisé ";
$l_invoice_company = "reliée à la Companie" ;
$l_invoice_deal = "Affaire";
$l_deal = "Affaire";
$l_company = "Société";

// Titres 
$l_payment = "Paiement";
$l_payment_infos = "Informations concernant le Paiement";
$l_entrytemp_date = "Date";
$l_entrytemp_type = "Type";
$l_entrytemp_amount = "Montant";
$l_entrytemp_realdate = "Date de Valeur";
$l_entrytemp_label = "Libellé";
$l_entrytemp_comment = "Commentaire";

// Actions 
$l_find = "Chercher";
$l_insert= "Insérer le  Paiement";
$l_update = "Mettre à jour le Paiement";
$l_delete = "Supprimer le Paiement";
$l_bank = "Encaisser le Paiement";
$l_delete_confirm = "Etes vous sur de vouloir supprimer ce Paiement ?";
$l_error_which = "Erreur : la variable which est mal positionnée... Contactez votre administrateur..." ;
$l_payment_infos = "Voir le Paiement";
$l_payment_mod = "Modifier le Paiement";
$l_search_invoice ="Chercher des Factures";
$l_select_invoice = "Utiliser";
$l_affect_invoices = "Affecter des Factures à ce Paiement";
$l_duplicate = "Dupliquer ce Paiement";
$l_break_asso = "Supprimer des associations entre ce paiement et des factures";
$l_break_asso_strong_confirm = "Attention !! Ce paiement est déjà encaissé et ".
"lié à plusieurs factures.<br> Si vous confirmez, ce paiement et TOUTES ".
"les factures seront considérées comme impayées !";
$l_break_asso_soft_confirm = "Attention !! Ce payment est déjà encaissé <br>".
"Si vous confirmez, ce paiement et la facture associée seront considérés ".
"comme impayés !";
$l_break_asso_easy_confirm = "cas simple";
$l_break_asso_go = "Continuer";
$l_payment_break_asso = "Sélection";
$l_refresh = "Calculer";

// Messages 
$l_no_display = "Remplissez les critères de recherche et appuyez sur <Chercher> pour lancer la recherche";
$l_found=" Paiements trouvés.";
$l_no_found="Aucun Paiement ne correspond à votre requête";
$l_no_invoices_connected = "Aucune Facture ne correspond à ce Paiement";
$l_invoices_connected = "Facture(s) associée(s) à ce Paiement";
$l_no_invoices = "Aucune facture trouvée";
$l_invoices_found = " Facture(s) corresponde(nt) à votre recherche";
$l_delete_ok = "Le Paiement a bien été supprimé.";
$l_delete_error = "La suppression du Paiement a échoué !";
$l_insert_ok = "Le Paiement a bien été inséré ";
$l_insert_error = "L'insertion du Paiement a échoué !";
$l_update_ok = "Le Paiement a bien été mis à jour.";
$l_update_error = "La mise à jour du Paiement a échoué";
$l_query_error = "Problème durant l'exécution de la requête...";
$l_insert_paymentinvoice_ok = "L'association Paiement/Facture s'est bien passé";

// Specifics fields
// MySQL Date and time format for display in the detail screen
$l_time_format = "%Y-%d-%m %H:%i:%s";
$l_date_format = "%Y-%d-%m %H:%i:%s";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_no_number = "\\nLe numérod u Paiement est invalide !";
$l_check_amount = "\\Le montant est invalide !";
$l_check_inout = "\\nVous deve choisir entre Paiement Client ou Paiement Fournisseur !";
$l_fill_label = "\\n Vous devez fournir un label ";
$l_invalid_date="\\n Format de Date Invalide (MM/JJ/AAAA ou AAAA-MM-JJ)";
$l_check_paymentkind = "Vous devez choisir un type de Paiement !";
$l_check_account = "Vous devez choisir un Compte !";
$l_no_amount = "Vous devez entrer un montant pour ce Paiement ";
$l_fill_date = "You have to fill this field";
$l_invalid_amount = "Le montant entré est invalide ";

// For the display parameters modification screen
$l_payment_options = "Options d'affichage pour les listes de Paiements";
$l_invoice_options = "Options d'affichage pour les listes de Factures";
$l_et_options = "Options d'affichage pour les listes d'Opérations Temporaires";
$l_options_dis = "Options d'affichage" ;


// banking check messages : 
$l_not_enough = "Vous n'avez pas utilisé la totalité du montant du Paiement !<br>";
$l_too_much = "Vous avez utilisé plus d'argent que le Paiement vous y autorise !<br>";
$l_trouble_invoice = "Au moins une Facture a reçu plus d'argent que son reste à payer. ".
"Elle va passer en état 'trouble'.<br> Etes vous sur de vouloir continuer ?";
$l_bank_anyway = "Encaisser ce Paiement";
$l_bank_again = "Recommencer l'encaissement depuis le début";

// reconcile messages :
$l_reconcile = "P";
$l_nb_payments_to_reconcile = " Paiements à pointer";
$l_nb_entrytemp_to_reconcile = " Opérations à pointer";
$l_file_error = "Erreur pendant l'ouverture du fichier ";
$l_write_error = "Erreur lors de l'écriture du fichier";
$l_read_error = "Erreur en lecture sur le fichier csv";
$l_remains_entrytemps = "Il reste des Opérations à pointer";
$l_restart_reconcile = "recommencer le pointage";
$l_restart_import = "recommencer l'import du fichier csv";
$l_import_ok = "continuer le pointage avec ces données";
$l_reconcile_errors = "Erreurs dans le pointage. ".
"Les dernières modifications ont été annulées";

//------------------------------------//
// Fields that appear in result lists //
//------------------------------------//
// Direct fields
$fieldnames["payment_label"] = $l_label;
$fieldnames["payment_amount"] = $l_amount;
$fieldnames["payment_date"] = $l_date;
$fieldnames["payment_expect_date"] = $l_expected_date;
$fieldnames["payment_number"] = $l_number;
$fieldnames["payment_account_lbl"] = $l_payment_account;
$fieldnames["invoice_label"] = $l_invoice_label;
$fieldnames["invoice_date"] = $l_invoice_date;
$fieldnames["invoice_amount_TTC"] = $l_invoice_TTC;
$fieldnames["invoice_amount_HT"] = $l_invoice_HT;
$fieldnames["invoice_number"] = $l_invoice_number;
$fieldnames["invoice_paid"] = $l_invoice_paid;
$fieldnames["invoice_company"] = $l_invoice_company;
$fieldnames["invoice_status"] = $l_invoice_status;
$fieldnames["invoice_deal"] = $l_invoice_deal;
// for the temp. entries :
$fieldnames["entrytemp_date"] = $l_entrytemp_date ;
$fieldnames["entrytemp_type"] = $l_entrytemp_type ;
$fieldnames["entrytemp_amount"] = $l_entrytemp_amount ;
$fieldnames["entrytemp_realdate"] = $l_entrytemp_realdate ;
$fieldnames["entrytemp_label"] = $l_entrytemp_label ;
$fieldnames["entrytemp_comment"] = $l_entrytemp_comment;