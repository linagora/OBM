<SCRIPT language="php">
//////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : account.inc                                             //
// 2001-07-31 Nicolas Roman                                              //
//////////////////////////////////////////////////////////////////////////////

// Labels et textes
$l_label = "Label";
$l_label_start = "Label start";
$l_last_account = "Last account";
$l_bank = "Bank"; 
$l_number = "Number";
$l_balance = "Balance";
$l_initial_balance = "Creation Balance";
$l_comment = "Comment";
$l_choose_balance_date = "Balance compute Date";
$l_date = "Date";
$l_expected_date = "Expected Date";
$l_amount = "Amount";


$l_include_expected = "Include expected operations ?";
$l_in_total ="In Total";
$l_out_total = "Out Total";
$l_total ="Estimated Balance";
$l_old_balance = "Today Balance";
$l_nb_expected_used = " expected operations used during the computing.";
$l_expected = "expected";
$l_used_payments = "Payments used to compute balance ";
$l_used_expected_payments = "Expected Payments used to compute balance";
// Titres
$l_account = "Account";

// Actions
$l_find = "Find";
$l_insert= "Insert Account";
$l_update = "Update Account";
$l_delete = "Delete Account";
$l_delete_confirm = "Confirm the deletion of this account ?";
$l_cancel = "Cancel";
$l_compute_balance = "Compute Balance";

// Messages 
$l_no_display = "Fill the search fields and press < Find > to retrieve the accounts";
$l_found="accounts found.";
$l_no_found="No account corresponding to your query";
$l_delete_ok = "The account has been deleted successfully.";
$l_delete_error = "The deletion of the account has failed !";
$l_insert_ok = "The account has been inserted successfully.";
$l_insert_error = "The insertion of the account has failed !";
$l_update_ok = "The account has been updated successfully.";
$l_update_error = "The update of the account has failed !";
$l_query_error = "Trouble executing the query...";
$l_impossible_deletion = " payments are associated with this account. Deletion is impossible"; // must be used with the number of payments leading...
// champs specifiques
// format de date et heure pour MySQL pour affichage dans ecran detail affaire
// Specifics fields
// MySQL Date and time format for display in the deal detail screen
$l_time_format = "%Y-%d-%m %H:%i:%s";
$l_date_format = "%Y-%d-%m %H:%i:%s";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$lj_check_number = "\\nThe account number is not valid!";
$lj_fill_number = "You have to fill the number before submitting!";
$lj_fill_label = "You have to fill the label before submitting!";
$lj_fill_bank = "You have to fill the bank field before submitting!";
$lj_confirm_no_bank="This account has no associated bank. Click on OK to confirm.";
$l_invalid_date="\\nInvalid Date Format (MM/JJ/AAAA or AAAA-MM-JJ)";
$l_fill_date = "\\n You have to fill the Date before submitting.";

// For the display parameters modification screen
$l_account_options = "Display options for accounts list"; 
$l_payment_options = "Display options for payments list"; 
$l_options_dis = "options of display" ;
//$l_subject = "Subject" ;
$l_query_display = "Displayed" ;
$l_order_dis = "Order of display" ;
$l_action = "Action" ;
$l_ch_to_dis_no = "Not to display it fields" ;
$l_ch_to_dis_no_help = "That indicates the appropriate field is displayed<br>Click on this button if you don't want to display the field" ;
$l_ch_to_dis_yes = "To display it fields" ;
$l_ch_to_dis_yes_help = "That indicates the appropriate field is not displayed<br>Click on this button if you want to display the field" ;
$l_dis_obligatory = "Display of this fields is mandatory" ;
$l_dis_obligatory_help = "This button means you don't have choice. The field appropriate field is be displayed" ;
$l_down_option = "To descend this option from a notch" ;
$l_down_option_help  = "Click on this button to move down the field" ;
$l_up_option = "To assemble this option of a notch" ;
$l_up_option_help = "Click on this button to move up the field" ;
$l_back_tolist = "Back to list display" ;
$l_help_options = "Contextual help" ;
