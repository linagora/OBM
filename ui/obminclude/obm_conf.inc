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

// OBM host (for mail links)
$cgp_host = "http://obm-panthere/";
define("C_IMAGE_PATH",$cgp_host."images");

// Use specific site includes
$cgp_site_include = true;

// Use Database sessions
//$cgp_sess_db = true;

// is Mail enabled ? (agenda)
$cgp_mail_enabled = true;

// Generic E-mail for Incident module
//$cmail_incident = "support@mydomain";

// is mailing enabled by default for a new user
$cgp_mailing_default = true;

// Session Cookie lifetime (in seconds, 0=session cookie)
$cs_lifetime = 7200;

// OBM System groups (groups id)
$cg_adm = 1;
$cg_com = 2;
$cg_prod = 3;

// Root of the document repository
$document_path = "/var/www/obmdocuments";

// Auto format fields
$caf_company_name = true;
$caf_town = true;

// Allow * in search field
$cgp_sql_star = true;

// Tuning constants
$ctu_sql_limit = true;

// Todo lines to display
$cgp_todo_nb = 5;

//-----------------//
// Displayed Infos //
//-----------------//
// sections -- Default is true
$cgp_show["section"] = ""; // Needed if module order to change
$cgp_show["section"]["com"] = "$path/agenda/agenda_index.php";
$cgp_show["section"]["user"] = "$path/user/user_index.php";
$cgp_show["section"]["prod"] = "$path/document/document_index.php";
$cgp_show["section"]["settings"] = "$path/settings/settings_index.php";
$cgp_show["section"]["admin"] = "$path/admin/admin_index.php?mode=html";

// modules -- false to hide, otherwise section
$cgp_show["module"] = ""; // Needed if module order to change
$cgp_show["module"]["company"] = false;
$cgp_show["module"]["agenda"] = "com";
$cgp_show["module"]["todo"] = "com";
$cgp_show["module"]["contact"] = "com";
$cgp_show["module"]["deal"] = false;
$cgp_show["module"]["list"] = false;
$cgp_show["module"]["publication"] = false;
$cgp_show["module"]["statistic"] = false;
$cgp_show["module"]["user"] = "user";
$cgp_show["module"]["group"] = "user";
$cgp_show["module"]["project"] = false;
$cgp_show["module"]["contract"] = false;
$cgp_show["module"]["document"] = "prod";
$cgp_show["module"]["settings"] = "SETTINGS";
$cgp_show["module"]["admin"] = "ADMIN";
$cgp_show["module"]["admin_code"] = "ADMIN";
$cgp_show["module"]["admin_pref"] = "ADMIN";
$cgp_show["module"]["admin_data"] = "ADMIN";
$cgp_show["module"]["admin_lang"] = "ADMIN";
$cgp_show["module"]["admin_ref"] = "ADMIN";
$cgp_show["module"]["import"] = "ADMIN";

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
//$cgp_hide["contact"]["category1"] = true;
//$cgp_hide["contact"]["category2"] = true;
//$cgp_hide["contact"]["contact_service"] = true;

// Deal module
$cgp_hide["deal"]["category1"] = true;

// authentification : 'CAS' or 'standalone' : defaults to 'standalone'
//$auth_kind="CAS";

// CAS specific parameters. useless in standalone mode
// Authentication used :
$cas_server = "sso.aliacom.local";
$cas_server_port = 8443;
$cas_server_uri = "";

//Set if there is a link for Aliamin, Blogs and Aliacms
//$aliamin_link = "http://aliamin.aliacom.local/";
//$aliacms_link = "http://cms.aliacom.local/";
//$blogs_link = "http://blogs.aliacom.local/";


</script>
