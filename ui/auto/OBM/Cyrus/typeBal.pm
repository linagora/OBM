package OBM::Cyrus::typeBal;

require Exporter;

use URI::Escape;
use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub getBdValues {
    my( $dbHandler, $domain, $srvId ) = @_;
    my $domainId = $domain->{"domain_id"};
    my $balPrefix = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"prefix"};
    my $balSeparator = $OBM::Parameters::cyrusConf::boxTypeDef->{"BAL"}->{"separator"};

    # La requete a executer - obtention des informations sur les utilisateurs
    # mails de l'organisation.
    my $query = "SELECT userobm_id, userobm_login, userobm_mail_quota, userobm_vacation_enable, userobm_vacation_message, userobm_email FROM UserObm WHERE userobm_mail_perms=1 AND userobm_domain_id=".$domainId." AND userobm_mail_server_id=".$srvId ;

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On tri les resultats dans le tableau
    my $users = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::listImapBox);
    while( my( $user_id, $user_login, $user_quota, $user_venable, $user_vmessage, $user_email ) = $queryResult->fetchrow_array ) {
        my $userDesc = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::imapBox);

        $userDesc->{"box_login"} = $user_login."@".$domain->{"domain_name"};
        $userDesc->{"box_name"} = $balPrefix.$balSeparator.$userDesc->{"box_login"};

        if( defined($user_quota) && ($user_quota ne "") ) {
            $userDesc->{"box_quota"} = $user_quota;
        }

        $userDesc->{"box_vacation_enable"} = $user_venable;
        $userDesc->{"box_vacation_message"} = uri_unescape( $user_vmessage );

        # Si le vacation est actif, on récupère toutes les adresses mails de
        # l'utilisateur
        if( $user_venable ) {
            $userDesc->{"box_email"} = &OBM::toolBox::makeEntityMailAddress( $user_email, $domain );
        }

        # On recupere la definition des ACL
        $userDesc->{"box_acl"} = &OBM::toolBox::getEntityRight( $user_id, $MAILBOXENTITY, $dbHandler );


        if( !exists($users->{$userDesc->{"box_login"}}) ) {
            $users->{$userDesc->{"box_login"}} = $userDesc;
        }
    }

    return $users;
}



