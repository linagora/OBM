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
$cgp_host = "http://obm/";

// Use specific site includes
//$cgp_site_include = true;

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
//$cgp_show["section"]["prod"] = false;
//$cgp_show["section"]["compta"] = false;
$cgp_show["section"]["aliamin"] = false;

// modules -- Default is true
//$cgp_show["module"]["contact"] = false;
//$cgp_show["module"]["deal"] = false;
//$cgp_show["module"]["agenda"] = false;
//$cgp_show["module"]["todo"] = false;
//$cgp_show["module"]["user"] = false;
//$cgp_show["module"]["group"] = false;
//$cgp_show["module"]["project"] = false;
//$cgp_show["module"]["contract"] = false;
//$cgp_show["module"]["document"] = false;
//$cgp_show["module"]["account"] = false;
//$cgp_show["module"]["invoice"] = false;
//$cgp_show["module"]["payment"] = false;
//$cgp_show["module"]["time"] = false;
//$cgp_show["module"]["statistic"] = false;

// Company module
$cgp_hide["company"]["company_number"] = true;
$cgp_hide["company"]["company_vat"] = true;
//$cgp_hide["company"]["companytype_label"] = true;
//$cgp_hide["company"]["companyactivity_label"] = true;
//$cgp_hide["company"]["companynafcode_code"] = true;
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


</script>
