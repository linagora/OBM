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
//$obmdb_dbtype = "PGSQL"; // (MYSQL || PGSQL)
$obmdb_db = "obm";
$obmdb_user = "obm";
$obmdb_password = "obm";

// Global parameters --------------------------------------------------------//

// OBM host (for mail links)
$cgp_host = "http://obm-panthere/";
define("C_IMAGE_PATH",$cgp_host."images");

// Use specific site includes
//$cgp_site_include = true;

// Todo lines to display
$cgp_todo_nb = 5;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 1;
$cg_prod = 2;

// Allow * in search field
$cgp_sql_star = true;

// Tuning constants
$ctu_sql_limit = true;

// is Mail enabled for OBM ? (eg: agenda)
$cgp_mail_enabled = true;

// Session Cookie lifetime (in seconds, 0=session cookie)
$cs_lifetime = 0;

// Use Database sessions
//$cgp_sess_db = true;

// authentification : 'CAS' (SSO AliaSuite) or 'standalone' (default)
//$auth_kind="CAS";
//$cas_server = "sso.aliacom.local";
//$cas_server_port = 8443;
//$cas_server_uri = "";

// Shall the login page encrypt authentication password
$auth_encryption = 1;

// encyption used to store password : "plain", "md5" or "crypt"
$password_encryption = "md5";

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

//----------------------//
// Profiles definitions //
//----------------------//
$profiles["editor"] = array (
  'section' => array (
    'default' => 0,
    'com' => 1,
    'prod' => 1,
    'user' => 1,
    'settings' => 1),
  'module' => array (
    'default' => $perm_editor)
);

//-----------------//
// Displayed Infos //
//-----------------//
// sections -- Default is true
//$cgp_show["section"] = ""; // Needed if module order to change
//$cgp_show["section"]["com"]["url"] = "$path/agenda/agenda_index.php";

// modules -- false to hide, otherwise section
//$cgp_show["module"] = ""; // Needed if module order to change
//$cgp_show["module"]["company"] = "com";

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

//Set if there is a link for Aliamin, Blogs and Aliacms
//$aliamin_link = "http://aliamin.aliacom.local/";
//$aliacms_link = "http://cms.aliacom.local/";
//$blogs_link = "http://blogs.aliacom.local/";


</script>
