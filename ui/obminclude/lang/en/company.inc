<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : company.inc                                              //
// 1999-01-25 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

// Database fields
$l_number = "Number";
$l_company_name = "Company Name";
$l_type = "Type";
$l_activity = "Activity";
$l_nafcode = "Code";
$l_market = "Marketing manager";
$l_web = "Web";
$l_email = "E-Mail";
$l_coord = "Details";
$l_vat = "EU VAT";
$l_siret = "SIRET";

// Labels and texts
$l_contact_new = "New Contact";
$l_deal_new = "New Deal";
$l_project_new = "New Projet";
$l_contract_new = "New Contract";
$l_internal = "Internal";
$l_naf_manage = "Code Management";
$l_naf_exist = "Existing codes";
$l_naf_new = "New Code";
$l_naf_label = "Label";
$l_naf_code = "Code";
$l_naf_istitle = "Is title";

// Actions
$l_add = "Add";
$l_insert = "Insert the Company";
$l_update = "Update the Company";
$l_checkdelete = "Check or Delete the Company";
$l_delete = "Delete the Company";

$l_naf_update = "Update Code";
$l_naf_checkdelete = "Check or Delete Code";
$l_naf_delete = "Delete Code";
$l_naf_insert = "Insert Code";

$l_select_company = "Select the Company";

// Messages
$l_err_act_empty = "You must fill the company activity !";
$l_cat_no = "No categories defined";
$l_no_display = "Fill the search fields and press < Find > to retrieve the companies";
$l_found = "companies found.";
$l_no_found = "No company found matching the query !";
$l_check_samecompany = "These companies seem similar to this new entry !";
$l_link_contact = "The company has contacts";
$l_link_contact_no = "The company hasn't any contacts";
$l_link_deal = "The company is involved in some deals";
$l_link_deal_no = "The company isn't involved in any deal";
$l_link_contract = "The company is involved in some contracts";
$l_link_contract_no = "The company isn't involved in any contract";
$l_link_project = "The company is involved in some projects";
$l_link_project_no = "The company isn't involved in any projects";
$l_link_invoice = "The company is involved in some invoices";
$l_link_invoice_no = "The company isn't involved in any invoices";
$l_cant_delete = "The company can't be deleted !";
$l_can_delete = "The company can be deleted.";

$l_naf_no = "No Code defined";
$l_naf_link_company = "These companies are referenced from this code";
$l_naf_link_company_no = "No company from this code";
$l_naf_cant_delete = "Code can't be deleted !";
$l_naf_can_delete = "Code can be deleted.";

// JavaScript Messages (les caracteres comme \ doivent etre backslashes -> \\)
$l_delete_confirm = "Really delete the company ?";
$l_fill_name = "You have to fill the Name before submitting.";
$l_j_check_zip = "The zip code is not valid.";
$l_j_check_phone = "The phone number is not valid.";
$l_j_check_fax = "The fax number is not valid.";
$l_j_check_email = "The email address is not valid.";
$l_j_check_web = "The web address is not valid.";
$l_j_select_company = "You have to select a company !";
$l_j_naf_create = "You have to input a NAF code for create it !";

// For the display parameters modification screen
$l_company_display = "Display options for companies lists"; 

$l_phone_labels = array ('WORK_VOICE' => 'Phone', 'CELL_VOICE' => 'Mobile phone', 'WORK_FAX' => 'Fax', 'OTHER' => 'Other phone');
$l_email_labels = array ('INTERNET' => 'Home', 'OTHER' => 'Other');
$l_address_labels = array ('HOME' => 'Home', 'WORK' => 'Work', 'OTHER' => 'Other');
$l_website_labels = array ('URL' => 'Homepage', 'BLOG' => 'Blog', 'OTHER' => 'Other');
