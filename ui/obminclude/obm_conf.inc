<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : obm_conf.inc                                                 //
//     - Desc : OBM specific site configuration file                         //
// 2003-10-17 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


// Database infos
$obmdb_host = "localhost";
$obmdb_dbtype = "MYSQL"; // (MYSQL || PGSQL)
$obmdb_dbtype = "PGSQL"; // (MYSQL || PGSQL)
$obmdb_db = "obm";
$obmdb_user = "obm";
$obmdb_password = "obm";

// Global parameters --------------------------------------------------------//

// OBM host (for mail links)
$cgp_host = "http://obm-panthere/";
define("C_IMAGE_PATH",$cgp_host."images");

// Use specific site includes
$cgp_site_include = true;

// Todo lines to display
$cgp_todo_nb = 5;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 2;
$cg_prod = 3;

// Allow * in search field
$cgp_sql_star = true;

// Tuning constants
$ctu_sql_limit = true;

// is Mail enabled for OBM ? (eg: agenda)
$cgp_mail_enabled = true;

// Session Cookie lifetime (in seconds, 0=session cookie)
$cs_lifetime = 7200;

// Use Database sessions
//$cgp_sess_db = true;

// authentification : 'CAS' (SSO AliaSuite) or 'standalone' (default)
//$auth_kind="CAS";
//$cas_server = "sso.aliacom.local";
//$cas_server_port = 8443;
//$cas_server_uri = "";

// Shall the login page encrypt authentication password
$auth_encryption = 0;

// encyption used to store password : "plain", "md5" or "crypt"
$password_encryption = "plain";

// Modules specific parameters ----------------------------------------------//

// Generic E-mail for Incident module
//$cmail_incident = "support@mydomain";

// is mailing enabled by default for a new contact
$cgp_mailing_default = true;

// is a new contact private by default
$ccontact_private_default = true;

// Root of the document repository
$document_path = "/var/www/obmdocuments";
$default_path = "/Default";                  // Default directory

// Auto format fields
$caf_company_name = true;
$caf_town = true;

//-----------------//
// Displayed Infos //
//-----------------//
// sections -- Default is true
$cgp_show["section"] = ""; // Needed if module order to change
$cgp_show["section"]["com"]["url"] = "$path/agenda/agenda_index.php";
$cgp_show["section"]["user"]["url"] = "$path/user/user_index.php";
$cgp_show["section"]["prod"]["url"] = "$path/document/document_index.php";
$cgp_show["section"]["settings"]["url"] = "$path/settings/settings_index.php";
$cgp_show["section"]["admin"]["url"] = "$path/admin/admin_index.php?mode=html";
$cgp_show["section"]["admin"]["right"] = "$cright_read";

// modules -- false to hide, otherwise section
$cgp_show["module"] = ""; // Needed if module order to change
$cgp_show["module"]["company"] = false;
$cgp_show["module"]["agenda"] = "com";
$cgp_show["module"]["todo"] = false;
$cgp_show["module"]["contact"] = "com";
$cgp_show["module"]["deal"] = false;
$cgp_show["module"]["list"] = "com";
$cgp_show["module"]["publication"] = false;
$cgp_show["module"]["statistic"] = false;
$cgp_show["module"]["user"] = "user";
$cgp_show["module"]["group"] = "user";
$cgp_show["module"]["project"] = false;
$cgp_show["module"]["contract"] = "prod";
$cgp_show["module"]["incident"] = "prod";
$cgp_show["module"]["document"] = "prod";
$cgp_show["module"]["settings"] = "settings";
$cgp_show["module"]["admin"] = "admin";
$cgp_show["module"]["admin_code"] = "admin";
$cgp_show["module"]["admin_pref"] = "admin";
$cgp_show["module"]["admin_data"] = "admin";
$cgp_show["module"]["admin_lang"] = "admin";
$cgp_show["module"]["admin_ref"] = "admin";
$cgp_show["module"]["import"] = "admin";

// Company module
$cgp_hide["company"]["company_number"] = true;
$cgp_hide["company"]["company_vat"] = true;
//$cgp_hide["company"]["companytype_label"] = true;
//$cgp_hide["company"]["companyactivity_label"] = true;
$cgp_hide["company"]["companynafcode_code"] = true;
//$cgp_hide["company"]["category"] = true;
//$cgp_hide["company"]["company_address3"] = true;
//$cgp_hide["company"]["company_expresspostal"] = true;

// Contact module
$cgp_hide["contact"]["contact_function"] = true;
//$cgp_hide["contact"]["contact_title"] = true;
$cgp_hide["contact"]["responsible"] = true;
//$cgp_hide["contact"]["category1"] = true;
$cgp_hide["contact"]["category2"] = true;
//$cgp_hide["contact"]["contact_service"] = true;

// Deal module
$cgp_hide["deal"]["category1"] = true;

<<<<<<< obm_conf.inc
=======
// authentification : 'CAS' or 'standalone' : defaults to 'standalone'

// authentification 
// 'CAS' or 'standalone'
// defaults to 'standalone'
// $auth_kind="CAS"; 
// CAS specific parameters.
// useless in standalone mode
// Authentication used :
$cas_server = "sso.aliacom.local";
$cas_server_port = 8443;
$cas_server_uri = "";

>>>>>>> 1.113
//Set if there is a link for Aliamin, Blogs and Aliacms
//$aliamin_link = "http://aliamin.aliacom.local/";
//$aliacms_link = "http://cms.aliacom.local/";
//$blogs_link = "http://blogs.aliacom.local/";


</script>
