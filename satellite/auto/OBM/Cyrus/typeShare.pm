package OBM::Cyrus::typeShare;

require Exporter;

use URI::Escape;
use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;


sub getBdValues {
    my( $dbHandler, $domain, $srvId ) = @_;
    my $domainId = $domain->{"domain_id"};
    my $sharePrefix = $OBM::Parameters::cyrusConf::boxTypeDef->{"SHARE"}->{"prefix"};
    my $shareSeparator = $OBM::Parameters::cyrusConf::boxTypeDef->{"SHARE"}->{"separator"};

    # La requete a executer - obtention des informations sur les utilisateurs
    # mails de l'organisation.
    my $query = "SELECT mailshare_id, mailshare_name, mailshare_quota FROM MailShare WHERE mailshare_domain_id=".$domainId." AND mailshare_mail_server_id=".$srvId ;

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "Probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return undef;
    }

    # On tri les resultats dans le tableau
    my $shares = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::listImapBox);
    while( my( $shareId, $shareLogin, $shareQuota ) = $queryResult->fetchrow_array ) {
        my $shareDesc = &OBM::toolBox::cloneStruct(OBM::Parameters::cyrusConf::imapBox);

        $shareDesc->{"box_login"} = $shareLogin."@".$domain->{"domain_name"};
        $shareDesc->{"box_name"} = $sharePrefix.$shareSeparator.$shareDesc->{"box_login"};

        if( defined($shareQuota) && ($shareQuota ne "") ) {
            $shareDesc->{"box_quota"} = $shareQuota*1000;
        }

        # On recupere la definition des ACL
        $shareDesc->{"box_acl"} = &OBM::toolBox::getEntityRight( $dbHandler, $domain, initRight( $shareId ), $shareId );


        if( !exists($shares->{$shareDesc->{"box_login"}}) ) {
            $shares->{$shareDesc->{"box_login"}} = $shareDesc;
        }
    }

    return $shares;
}


sub initRight {
    my( $shareId ) = @_;
    my $entityType = "mailshare";
    my %rightDef;

    $rightDef{"read"}->{"compute"} = 1;
    $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_login FROM UserObm i, EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$shareId." AND j.entityright_entity='".$entityType."'";
        
    $rightDef{"writeonly"}->{"compute"} = 1;
    $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_login FROM UserObm i, EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$shareId." AND j.entityright_entity='".$entityType."'";

    $rightDef{"write"}->{"compute"} = 1;
    $rightDef{"write"}->{"sqlQuery"} = "SELECT i.userobm_login FROM UserObm i, EntityRight j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=1 AND j.entityright_entity_id=".$shareId." AND j.entityright_entity='".$entityType."'";
        
    $rightDef{"public"}->{"compute"} = 0;
    $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM EntityRight WHERE entityright_entity_id=".$shareId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

    return \%rightDef;
}
