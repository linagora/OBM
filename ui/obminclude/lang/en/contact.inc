<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : English                                                  //
//     - File     : contact.inc                                              //
// 1999-01-24 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$ //
///////////////////////////////////////////////////////////////////////////////

$l_middlename = 'Middle Name';
$l_mname = 'Middle Name';
$l_commonname = 'Common name';
$l_suffix = 'Suffix';
$l_market = "Person in charge";
$l_function = "Function";
$l_title = "Job Title";
$l_minihphone = "(Home)";
$l_minimphone = "(Mobile)";
$l_no_name = "No name";
$l_email = "E-Mail";
$l_email_other = "Other E-Mail";
$l_mailing_ok = "Mailing enabled";
$l_mailok = "Mailing enabled";
$l_mailing_ok_only = "Mailing enabled only";
$l_private_only = "Private contacts only";
$l_newsletter = "Newsletter";
$l_news = "News";
$l_code = "Code";
$l_letter = "Letterhead"; 
$l_comment1 = "Comment";
$l_comment2 = "Comment 2";
$l_comment3 = "Comment 3";
$l_date_after = "Inscription date after";
$l_date_before = "Inscription date before";
$l_mailing_ok_first = "Mail.";
$l_other_infos = "Other Information"; 
$l_birthday = "Birthday"; 
$l_anniversary = 'Anniversary';
$l_assistant = 'Assistant';
$l_spouse = 'Spouse';
$l_synchronisation = "Subscription";
$l_contact_name = "Contact Name";
$l_from_company = "From Company";
$l_telother = "Other tel";
$l_contact_select_company = "Select company for contact."; 
$l_kind_manage = "Contact Kind Management";
$l_kind_exist = "Existing Kinds";
$l_kind_no = "No kind defined";
$l_kind_new = "New Kind";
$l_header = "Header";
$l_subscription = "Subscriptions";
$l_subscription_list = "Subscriptions";
$l_subscription_new= "New subscription";
$l_insert = "Insert Contact"; 
$l_update = "Update the Contact";
$l_checkdelete = "Check or Delete Contact"; 
$l_delete = "Delete the Contact";
$l_kind_update = "Update Kind";
$l_kind_checkdelete = "Check or Delete Kind";
$l_kind_delete = "Delete Kind";
$l_kind_insert = "Insert Kind";
$l_add_comment1 = "Add a comment";
$l_upd_comment1 = "Update comment";
$l_add_comment2 = "Add a comment 2";
$l_upd_comment2 = "Update comment 2";
$l_add_comment3 = "Add a comment 3";
$l_upd_comment3 = "Update comment 3";
$l_copy = "Copy";
$l_exportation = "Export";
$l_download_contact_card = "Download contact information"; 
$l_vcard = "Vcard";
$l_sync_contact = "PDA synchronisation";
$l_sync = "Subscirbe for synchronisation";
$l_desync = "Unsubscribe for synchronisation";

// $l_toggle_public_access = "This will allow everybody to invite this user to a meeting. Do you want to continue ?";
$l_toggle_public_read = "This will make the addressbook readable by everybody. Do you want to continue ?";
$l_toggle_public_write = "This will allow everybody to update this addressbook. Do you want to continue ?";
$l_toggle_public_admin = "This will allow everybody to update this addressbook rights. Do you want to continue ?";


// Address books
$l_sync_ad = "Subscribe";
$l_desync_ad = "Unsubscribe";
$l_delete_ad = "Delete";
$l_update_ad = "Update";
$l_confirm_delete_ad = "Remove address book ";


// Messages
$l_check_samecontact = "These contacts seem similar to this new entry !";
$l_link_deal = "The contact is involved in some deals";
$l_link_deal_no = "The contact is not involved in any deal"; 
$l_link_contract = "The contact is involved in some contracts";
$l_link_contract_no = "The contact is not involved in any contract"; 
$l_link_list = "The contact is registered in some lists";
$l_link_list_no = "The contact is not registered in any list"; 
$l_link_publication = "The contact has some subscriptions";
$l_link_publication_no = "The contact has no subscription"; 
$l_cant_delete = "The contact cannot be deleted !"; 
$l_can_delete = "The contact can be deleted.";
$l_no_display = "Fill in the search fields and press < Find > to retrieve the contacts"; 
$l_found = "contacts found.";
$l_no_found = "No contact matching the query has been found!"; 
$l_no_export = "No contact to export !";
$l_kind_link_company = "These companies are referenced as";
$l_kind_link_company_no = "No company referenced as";
$l_kind_cant_delete = "The Kind cannot be deleted !"; 
$l_kind_can_delete = "The Kind can be deleted.";
$l_birthday_event_title = "%s's birthday";
$l_anniversary_event_title = "%s's anniversary";
$l_fill_lastname = "You have to fill in the Last name before submitting."; 
$l_fill_company = "You have to select a company before submitting.";
$l_fill_phone = "You have to fill in the Phone number before submitting."; 
$l_delete_confirm = "Confirm contact deletion?"; 
$l_j_check_phone = "The phone number is not valid.";
$l_j_check_hphone = "The personal phone number is not valid.";
$l_j_check_mphone = "The mobile phone number is not valid.";
$l_j_check_fax = "The fax number is not valid.";
$l_j_check_email = "The email address is not valid. Verify domain."; 
$l_j_select_contact = "You have to select a contact !";
$l_j_kind_delete = "You have to select a kind to check or delete it !";
$l_j_kind_update = "You have to select a kind and enter a new name to update it !";
$l_j_kind_create = "You have to fill in all kind information to create it !"; 
$l_contact_display = "Display options for contact lists"; 
$l_timecreate = "Creation";
$l_timeupdate = "Last update";
$l_publication_title = "Title";
$l_renewal = "Automatic renewal";
$l_recept = "Reception mode";
$l_quantity = "Quantity";
$l_no_found_subscription = "No subscription found for this contact !";
$l_vcf_file = "VCF file to import";
$l_phone_labels = array ('HOME_VOICE' => 'Home phone', 'WORK_VOICE' => 'Work phone', 'CELL_VOICE' => 'Mobile phone', 'HOME_FAX' => 'Home fax', 'WORK_FAX' => 'Work fax', 'PAGER' => 'Pager', 'OTHER' => 'Other phone');
$l_email_labels = array ('INTERNET' => 'Home', 'OTHER' => 'Other');
$l_address_labels = array ('HOME' => 'Home', 'WORK' => 'Work', 'OTHER' => 'Other');
$l_website_labels = array ('URL' => 'Homepage', 'BLOG' => 'Blog',  'CALURI' => 'Calendar', 'OTHER' => 'Other');
$l_im_labels = array('XMPP' => 'Jabber', 'X_GTALK' => 'Google Talk', 'AIM' => 'AIM', 'YMSGR' => 'Yahoo', 'MSN' => 'MSN', 'X_ICQ' => 'ICQ', 'OTHER' => 'Other');
$l_company_phone_labels = array ('COMPANY_WORK_VOICE' => 'Company\'s phone', 'COMPANY_CELL_VOICE' => 'Company\'s mobile', 'COMPANY_WORK_FAX' => 'Company\'s fax', 'COMPANY_OTHER' => 'Company\'s other phone');
$l_company_email_labels = array ('COMPANY_INTERNET' => 'Company\'s mail', 'COMPANY_OTHER' => 'Company\'s other');
$l_company_address_labels = array ('COMPANY_HQ' => 'Company\'s headquarters', 'COMPANY_OTHER' => 'Company\'s other');
$l_company_website_labels = array ('COMPANY_URL' => 'Company\'s website', 'COMPANY_OTHER' => 'Company\'s other');
// Error Messages
$l_err_length_express_postal = "Field 'EXPRESS POSTAL' has 16 characters max";
$l_err_length_express_postal = "Field 'ZIP CODE' has 14 characters max";
$l_add_fields =  'Add more fields';
