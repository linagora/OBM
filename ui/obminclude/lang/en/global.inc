<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English for global                                       //
//     - File     : global.inc                                               //
// 2001-12-27 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Common actions
$l_find = "Find";
$l_back = "Back";
$l_cancel = "Cancel";
$l_close = "Close the window";
$l_validate = "Validate";
$l_execute = "Execute action";
$l_confirm = "Confirm";

// Common messages
$l_yes = "Yes";
$l_no = "No";
$l_all = "All";
$l_all_f = "All";
$l_none = "---";
$l_comment = "Comment";
$l_archive = "Archive";
$l_archive_first = "Arc";
$l_date = "Date";
$l_name = "Name";
$l_label = "Label";
$l_invalid_data = "Invalid Data";
$l_error_permission = "You are not allowed to execute this action";
$l_error_visibility = "You can't access this information, it is private.";
$l_query_error = "Error while executing the query";
$l_color = "Color";
$l_phone = "Phone";
$l_address = "Address";
$l_postcode = "Zip Code";
$l_town = "Town";
$l_priority = "Priority";
$l_datasource = "Data source";
$l_after = "After";
$l_before = "Before";
$l_fuzzy = "Advanced";

// OBM_DISPLAY
$l_entries = "Entries";
$l_page_res = "Result pages";
$l_export = "Export";
$l_export_all = "Export All";
$l_field = "Field";
$l_displayed = "Displayed";
$l_order = "Order";
$l_move = "Move";
$l_mandatory = "Display of this field is mandatory";
$l_display_no = "Stop displaying this field";
$l_display_yes = "Display this field";
$l_down = "Move the field down one row";
$l_up = "Move the field up one row";

// OBM_DISPLAY help screen
$l_help = "Help";
$l_help_display_yes = "The field is displayed. Click on this button if you want the field not to be displayed";
$l_help_display_no = "The field is not displayed. Click on this button if you want the field to be displayed";
$l_help_mandatory = "This field is mandatory";
$l_help_down  = "Move the field down";
$l_help_up = "Move the field up";
$l_document_added = " added documents";
$l_no_document_added = "No documents added";

// Javascript common messages
$l_fill_label = "You have to fill the label";
$l_fill_date = "The date must be filled";
$l_invalid_date = "Invalid date format (MM/JJ/AAAA ou AAAA-MM-JJ)";

//used by many modules
//tasktype groups
$l_task_fac = "Factured Tasks";
$l_task_int = "Internal Projects";
$l_task_div = "Not Factured Tasks";

$l_monthsofyear = array ('January','February', 'March','April','May','June',
                         'July','August','September','October','November',
			 'December');
$l_monthsofyearshort = array ('jan','feb','mar','apr','may','jun','jul',
			      'aug','sep','oct','nov','dec');

$l_daysofweekreallyshort = array ('S','M','T','W','T','F','S');
$l_invalid_perm = "Invalid permissions.";

////////////////////////////////////////////////////////
////// Theme headers lang files ////////////////////////
////////////////////////////////////////////////////////

$l_header_com    = "COMMERCIAL";
$l_header_prod   = "PRODUCTION";
$l_header_compta = "ACCOUTANCY";
$l_header_users  = "USERS";
$l_header_admins = "ADMINISTRATION";
$l_header_aliamin = "ALIAMIN";

$l_header_company = "Companies";
$l_header_contact = "Contacts";
$l_header_deal = "Deals";
$l_header_list = "Lists";
$l_header_agenda = "Calendar";
$l_header_todo    = "Todo";
$l_header_publication = "Publication";

$l_header_time = "Time management";
$l_header_project = "Projects";
$l_header_contract = "Contracts";
$l_header_incident = "Incidents";
$l_header_document = "Documents";

$l_header_account = "Accounts";
$l_header_invoice = "Invoices";
$l_header_payment = "Payments";

$l_header_settings = "Settings";
$l_header_user = "Users";
$l_header_group = "Groups";

$l_header_admin = "Administration";
$l_header_admin_lang = "Lang";
$l_header_admin_data = "Data";
$l_header_admin_pref = "Prefs";
$l_header_admin_code = "Code";
$l_header_admin_ref = "Referential";
$l_header_import = "Import";

$l_header_new_f = "New";
$l_header_new = "New";
$l_header_find = "Find";
$l_header_display = "Display";
$l_header_global_export = "Export";
$l_header_export = "Export";
$l_header_consult = "Consult";

$l_header_update = "Update";
$l_header_delete = "Delete";
$l_header_add_contact = "Add contact";
$l_header_add_user = "Add user";
$l_header_add_group = "Add group";
$l_header_add_deal = "Add deal";
$l_header_todo_list = "List";

$l_header_new_child = "New linked Deal";
$l_header_new_parent = "New ParentDeal";

$l_header_reconcile = "Reconcile";
$l_header_compute_balance = "Compute balance";
$l_header_dupplicate = "Dupplicate";

$l_header_add_contact = "Add contact";


// Project headers
$l_header_man_task = "Tasks";
$l_header_man_member = "Members";
$l_header_man_advance = "Progress";
$l_header_man_affect = "Allocation";
$l_header_add_member = "Add member";

// Admin headers
$l_header_index = "Main";
$l_header_help = "Help";
$l_header_amp = "Show amp";
$l_header_func_unused = "Functions";
$l_header_clear_sess = "Clear sessions";
$l_header_pref_update = "Reset users preferences";
$l_header_reset = "Default Preferences";
$l_header_country = "Country";
$l_header_datasource = "Data Source";
$l_header_tasktype = "Task Type";

$l_header_year = "Year";
$l_header_day = "Day";
$l_header_week = "Week";
$l_header_month = "Month";
$l_header_new_event = "New appointment";

//time headers
$l_header_stats = "Statistics";
$l_header_statsmonth = "Monthly stats";
$l_header_statsuser = "User stats";
$l_header_weeklyview = "Week";
$l_header_monthlyview = "Month";
$l_header_globalview = "Validation";

//document headers
$l_document_new = "New document";
$l_document_add = "Affect a document";

//Portal labels
$l_waiting_events = "Waiting events";
$l_unfilled = "Days unfilled";
$l_youre_agenda = "Your calendar";
$l_youre_time = "Your time management";
