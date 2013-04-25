<?php
set_include_path(get_include_path() . PATH_SEPARATOR . "../../obminclude/lib/CAS");
/**
 * CAS Authentication configuration file
 *
 */

// whether to force all users to use CAS to authenticate. If set to true,
//     all users trying to load the login form will be redirected to
//     the CAS login URL. This means nobody will ever see the RC login page.
//$rcmail_config['cas_force'] = true;

// whether to act as a CAS proxy. If set to true, a proxy ticket will be
//     retrieved from the CAS server to be used as password for logging into
//     the IMAP server. This is the preferred method of authenticating
//     to the IMAP backend.
//     If set to false, the IMAP password specified below will be used.
//$rcmail_config['cas_proxy'] = true;

// phpCAS debug file
$rcmail_config['cas_debug'] = false;
$rcmail_config['cas_debug_file'] = '/tmp/cas_debug.log';

// directory where PGTs will be temporarily stored. Will only be used if
//     cas_proxy is set to true.
$rcmail_config['cas_pgt_dir'] = '/tmp';

// name of the IMAP service. Will only be used if cas_proxy is set to true.
//     This service name must be authorized to be used with the CAS server.
//$rcmail_config['cas_imap_name'] = 'imap://obm-imap-server-name';

// name of the SMTP service. Will only be used if cas_proxy is set to true.
//     This service name must be authorized to be used with the CAS server.
//$rcmail_config['cas_smtp_name'] = 'imap://obm-imap-server-name';

// whether the IMAP server caches proxy tickets it has received for subsequent
//    requests. Will only be used if cas_proxy is set to true. If set to true,
//    proxy tickets will be reused to connect to the IMAP server until an IMAP
//    connection fails, after which a new proxy ticket will be retrieved. If
//    set to false, a new proxy ticket will be retrieved before each IMAP
//    request. Setting this to true and enabling caching on the IMAP server
//    significantly reduces the number of requests made to the CAS server.
//$rcmail_config['cas_imap_caching'] = true;

// password for logging into the IMAP server. Will only be used if cas_proxy
//     is set to false. The IMAP backend must accept this password for all
//     authorized users.
//$rcmail_config['cas_imap_password'] = '';

// CAS server host name.
//$rcmail_config['cas_hostname'] = 'centos6.obm.team.services.par.lng';

// CAS server port number.
//$rcmail_config['cas_port'] = 443;

// CAS service URI on the CAS server.
//$rcmail_config['cas_uri'] = '/sso/cas/';

// CAS server SSL validation: 'self' for self-signed certificate, 'ca' for
//     certificate from a CA, empty for no SSL validation.
//$rcmail_config['cas_validation'] = '';

// CAS server certificate in PEM format, used when CAS validation is set to
//     'self' or 'ca'.
//$rcmail_config['cas_cert'] = '/var/lib/obm-ca/cacert.pem';

// CAS service login URL.
//$rcmail_config['cas_login_url'] = '';

// CAS service logout URL.
//$rcmail_config['cas_logout_url'] = 'https://somewhere/';
?>
