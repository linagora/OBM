// ===============================================================================
// README
// Last update: 24 Déc 2009
// Author(s): Thomas Chemineau - thomas.chemineau@gmail.com
// ===============================================================================

To activate this functionnality, please insert the following lines into the file
/etc/obm/obm_conf.inc on your system, if you install OBM through packages.

8<--------
$auth_kind = 'LemonLDAP';

$lemonldap_config = Array(

    //
    // Indicate if OBM updates on user data are performed. If true,
    // informations send by LemonLDAP::NG in HTTP header will be synchronized
    // with user data in OBM, through the header map defined below. The synchronization
    // is performed only if user data should be updated in OBM.
    // Default: false
    //
    "auto_update" => true,

    //
    // Force update on user data, even if there are no available updates.
    // The option "auto_update" must be actived.
    // Default: false
    //
    "auto_update_force_user"  => true,

    //
    // Force update on groups, even if there are no available updates.
    // The option "auto_update" must be actived.
    // Default: false
    //
    "auto_update_force_group" => true,

    //
    // The URL catched by LemonLDAP::NG to perform SLO. This URL should be
    // also configured in rules of the virtual host of OBM in the LemonLDAP::NG
    // manager.
    //
    "url_logout" => "http://obm.example.com/logout",

    //
    // The server IP of the LemonLDAP::NG proxy. This will allow the connector
    // to check if the HTTP request provides from the right proxy.
    //
    "server_ip_address" => "localhost",

    //
    // Indicate if checks are performed on the HTTP request. These checks
    // include the one that verify the proxy server. This option is not
    // obligatory if OBM and LemonLDAP::NG are installed on the same server.
    // Default: false
    //
    "server_ip_server_check" => false,

    //
    // Activate debug or not.
    // Default: false
    // !! OBSOLETE !!
    //
    "debug" => true,

    //
    // The file where debug informations will be written.
    // Default: "/tmp/obm-lemonldapng.log"
    // !! OBSOLETE !!
    //
    "debug_filepath" => "/tmp/obm-lemonldapng.log",

    //
    // Fixe debug level.
    // Logs will be send to Apache error files.
    // Values are: DEBUG, INFO, WARN, ERROR or NONE.
    // Default is: NONE.
    //
    "debug_level" => "NONE",

    //
    // The HTTP header which identifies a user, to trace login and logout.
    // Default: "HTTP_OBM_UID"
    //
    "debug_header_name" => "HTTP_OBM_UID",

    //
    // The HTTP header which contains LDAP groups. Each groups are
    // separated by the character ';'.
    // Default: "HTTP_OBM_GROUPS"
    //
    "group_header_name" => "HTTP_OBM_GROUPS",

    //
    // You could personalize HTTP headers names that match OBM SQL fields. For
    // informations there is a convention: HTTP headers follow names of LDAP
    // attributes, in uppercase, and prefixed by the term HTTP_OBM. It is not
    // recommended to modify these values. Instead, name HTTP headers as they
    // are defined here.
    //
    "headers_map"      => Array(
       "userobm_gid"                   => "HTTP_OBM_GIDNUMBER",
       //"userobm_domain_id"           => ,
       "userobm_login"                 => "HTTP_OBM_UID",
       "userobm_password"              => "HTTP_OBM_USERPASSWORD",
       //"userobm_password_type"       => ,
       "userobm_perms"                 => "HTTP_OBM_PERMS",
       //"userobm_kind"                => ,
       "userobm_lastname"              => "HTTP_OBM_SN",
       "userobm_firstname"             => "HTTP_OBM_GIVENNAME",
       "userobm_title"                 => "HTTP_OBM_TITLE",
       "userobm_email"                 => "HTTP_OBM_MAIL",
       "userobm_datebegin"             => "HTTP_OBM_DATEBEGIN",
       //"userobm_account_dateexp"     => ,
       //"userobm_delegation_target"   => ,
       //"userobm_delegation"          => ,
       "userobm_description"           => "HTTP_OBM_DESCRIPTION",
       //"userobm_archive"             => ,
       //"userobm_hidden"              => ,
       //"userobm_status"              => ,
       //"userobm_local"               => ,
       //"userobm_photo_id"            => ,
       "userobm_phone"                 => "HTTP_OBM_TELEPHONENUMBER",
       //"userobom_phone2"             => ,
       //"userobm_mobile"              => ,
       "userobm_fax"                   => "HTTP_OBM_FACSIMILETELEPHONENUMBER",
       //"userobm_fax2"                => ,
       "userobm_company"               => "HTTP_OBM_O",
       //"userobm_direction"           => ,
       "userobm_service"               => "HTTP_OBM_OU",
       "userobm_address1"              => "HTTP_OBM_POSTALADDRESS",
       //"userobm_address2"            => ,
       //"userobm_address3"            => ,
       "userobm_zipcode"               => "HTTP_OBM_POSTALCODE",
       "userobm_town"                  => "HTTP_OBM_L",
       //"userobm_expresspostal"       => ,
       //"userobm_host_id"             => ,
       //"userobm_web_perms"           => ,
       //"userobm_web_list"            => ,
       //"userobm_web_all"             => ,
       //"userobm_mail_perms"          => ,
       //"userobm_mail_ext_perms"      => ,
       //"userobm_mail_server_id"      => ,
       //"userobm_mail_server_hostname" => ,
       "userobm_mail_quota"            => "HTTP_OBM_MAILQUOTA",
       //"userobm_nomade_perms"        => ,
       //"userobm_nomade_enable"       => ,
       //"userobm_nomade_local_copy"   => ,
       //"userobm_email_nomade"        => ,
       //"userobm_vacation_enable"     => ,
       //"userobm_vacation_datebegin"  => ,
       //"userobm_vacation_dateend"    => ,
       //"userobm_vacation_message"    => ,
       //"userobm_samba_perms"         => ,
       //"userobm_samba_home"          => ,
       //"userobm_samba_home_drive"    => ,
       //"userobm_samba_logon_script"  => ,
       // ---- Unused values ? ----
       "userobm_ext_id"                => "HTTP_OBM_SERIALNUMBER",
       //"userobm_system"              => ,
       //"userobm_nomade_datebegin"    => ,
       //"userobm_nomade_dateend"      => ,
       //"userobm_location"            => ,
       //"userobm_education"           => ,
     ),
  );
8<--------

