package OBM::Entities::obmMailshare;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::toolBox;
require OBM::dbUtils;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $incremental, $mailShareId ) = @_;

    my %obmMailshareAttr = (
        type => undef,
        typeDesc => undef,
        incremental => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        mailShareId => undef,
        domainId => undef,
        mailShareDesc => undef
    );


    if( !defined($mailShareId) ) {
        croak( "Usage: PACKAGE->new(INCR, MAILSHAREID)" );

    }elsif( $mailShareId !~ /^\d+$/ ) {
        &OBM::toolBox::write_log( "obmMailshare: identifiant de BAL partagee incorrect", "W" );
        return undef;

    }else {
        $obmMailshareAttr{"mailShareId"} = $mailShareId;

    }

    if( $incremental ) {
        $obmMailshareAttr{"incremental"} = 1;
        $obmMailshareAttr{"links"} = 0;
    }else {
        $obmMailshareAttr{"incremental"} = 0;
        $obmMailshareAttr{"links"} = 1;
    }

    $obmMailshareAttr{"type"} = $MAILSHARE;
    $obmMailshareAttr{"typeDesc"} = $attributeDef->{$obmMailshareAttr{"type"}};
    $obmMailshareAttr{"toDelete"} = 0;
    $obmMailshareAttr{"archive"} = 0;
    $obmMailshareAttr{"sieve"} = 0;

    bless( \%obmMailshareAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $mailShareId = $self->{"mailShareId"};


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "obmMailshare: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "obmMailshare: description de domaine OBM incorrecte", "W" );
        return 0;
    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $query = "SELECT COUNT(*) FROM ".&OBM::dbUtils::getTableName("MailShare", $self->{"incremental"})." WHERE mailshare_id=".$mailShareId;

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return undef;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        &OBM::toolBox::write_log( "obmMailshare: pas de BAL partage d'identifiant : ".$mailShareId, "W" );
        return undef;
    }elsif( $numRows > 1 ) {
        &OBM::toolBox::write_log( "obmMailshare: plusieurs BAL partages d'identifiant : ".$mailShareId." ???", "W" );
        return undef;
    }


    # La requete a executer - obtention des informations sur l'utilisateur
    $query = "SELECT mailshare_name, mailshare_description, mailshare_email, mailshare_quota, mailshare_mail_server_id FROM ".&OBM::dbUtils::getTableName("MailShare", $self->{"incremental"})." WHERE mailshare_id=".$mailShareId;

    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "obmMailshare: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
        return undef;
    }

    # On range les resultats dans la structure de donnees des resultats
    my( $mailshare_name, $mailshare_description, $mailshare_email, $mailshare_quota, $mailshare_mail_server_id ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    &OBM::toolBox::write_log( "obmMailshare: gestion de la BAL partagee : '".$mailshare_name."', domaine '".$domainDesc->{"domain_label"}."'", "W" );

    # On range les resultats dans la structure de donnees des resultats
    $self->{"mailShareDesc"}->{"mailshare_name"} = $mailshare_name;
    $self->{"mailShareDesc"}->{"mailshare_mailbox"} = "+".$mailshare_name."@".$domainDesc->{"domain_name"};
    $self->{"mailShareDesc"}->{"mailshare_description"} = $mailshare_description;
    $self->{"mailShareDesc"}->{"mailshare_domain"} = $domainDesc->{"domain_label"};

    if( $mailshare_email ) {
        my $localServerIp = &OBM::toolBox::getHostIpById( $dbHandler, $mailshare_mail_server_id );

        if( !defined($localServerIp) ) {
            &OBM::toolBox::write_log( "obmMailshare: droit mail du repertoire partage : '".$mailshare_name."' annule - Serveur inconnu !", "W" );
            $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 0;

        }else {
            $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 1;
            push( @{$self->{"mailShareDesc"}->{"mailshare_mail"}}, $mailshare_email."@".$domainDesc->{"domain_name"} );

            for( my $j=0; $j<=$#{$domainDesc->{"domain_alias"}}; $j++ ) {
                push( @{$self->{"mailShareDesc"}->{"mailshare_mail_alias"}}, $mailshare_email."@".$domainDesc->{"domain_alias"}->[$j] );
            }

            # Gestion de la BAL destination
            $self->{"mailShareDesc"}->{"mailShare_mailbox"} = $self->{"mailShareDesc"}->{"mailshare_name"}."@".$domainDesc->{"domain_name"};

            # On ajoute le serveur de mail associé
            $self->{"mailShareDesc"}->{"mailShare_mailLocalServer"} = "lmtp:".$localServerIp.":24";

            # Gestion du serveur de mail
            $self->{"mailShareDesc"}->{"mailShare_server"} = $mailshare_mail_server_id;

            # Gestion du quota
            $self->{"mailShareDesc"}->{"user_mailbox_quota"} = $mailshare_quota*1000;
        }

    }else {
        $self->{"mailShareDesc"}->{"mailshare_mailperms"} = 0;

    }

    # On positionne l'identifiant du domaine de l'entité
    $self->{"domainId"} = $domainDesc->{"domain_id"};

    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->{"links"} ) {
        $self->getEntityLinks( $dbHandler, $domainDesc );
    }


    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

    $self->_getEntityMailShareAcl( $dbHandler, $domainDesc );

    # On précise que les liens de l'entité sont aussi à mettre à jour.
    $self->{"links"} = 1;

    return 1;
}


sub setDelete {
    my $self = shift;

    $self->{"toDelete"} = 1;

    return 1;
}


sub getDelete {
    my $self = shift;

    return $self->{"toDelete"};
}


sub getArchive {
    my $self = shift;

    return $self->{"archive"};
}


sub isIncremental {
    my $self = shift;

    return $self->{"incremental"};
}


sub _getEntityMailShareAcl {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;
    my $mailShareId = $self->{"mailShareId"};

    if( !$self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $self->{"mailShareDesc"}->{"user_mailshare_acl"} = undef;
    }else {

        my $entityType = "mailshare";
        my %rightDef;

        $rightDef{"read"}->{"compute"} = 1;
        $rightDef{"read"}->{"sqlQuery"} = "SELECT i.userobm_login FROM ".&OBM::dbUtils::getTableName("UserObm", $self->isIncremental())." i, ".&OBM::dbUtils::getTableName("EntityRight", $self->isIncremental())." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=0 AND j.entityright_read=1 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"writeonly"}->{"compute"} = 1;
        $rightDef{"writeonly"}->{"sqlQuery"} = "SELECT i.userobm_login FROM ".&OBM::dbUtils::getTableName("UserObm", $self->isIncremental())." i, ".&OBM::dbUtils::getTableName("EntityRight", $self->isIncremental())." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=0 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"write"}->{"compute"} = 1;
        $rightDef{"write"}->{"sqlQuery"} = "SELECT i.userobm_login FROM ".&OBM::dbUtils::getTableName("UserObm", $self->isIncremental())." i, ".&OBM::dbUtils::getTableName("EntityRight", $self->isIncremental())." j WHERE i.userobm_id=j.entityright_consumer_id AND j.entityright_write=1 AND j.entityright_read=1 AND j.entityright_entity_id=".$mailShareId." AND j.entityright_entity='".$entityType."'";

        $rightDef{"public"}->{"compute"} = 0;
        $rightDef{"public"}->{"sqlQuery"} = "SELECT entityright_read, entityright_write FROM ".&OBM::dbUtils::getTableName("EntityRight", $self->isIncremental())." WHERE entityright_entity_id=".$mailShareId." AND entityright_entity='".$entityType."' AND entityright_consumer_id=0";

        # On recupere la definition des ACL
        $self->{"mailShareDesc"}->{"user_mailshare_acl"} = &OBM::toolBox::getEntityRight( $dbHandler, $domainDesc, \%rightDef, $mailShareId );
    }

    return 1;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"mailShareDesc"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"mailShareDesc"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"mailShareDesc"};

    # Les parametres necessaires
    if( $entry->{"mailshare_name"} ) {
        $ldapEntry->add(
            objectClass => $self->{"typeDesc"}->{"objectclass"},
            cn => $entry->{"mailshare_name"},
            mailBox => $entry->{"mailshare_mailbox"}
        );

    }else {
        return 0;
    }

    if( $entry->{"mailshare_description"} ) {
        $ldapEntry->add( description => to_utf8({ -string => $entry->{"mailshare_description"}, -charset => $defaultCharSet }) );
    }

    # Le serveur de BAL local
    if( $entry->{"mailShare_mailLocalServer"} ) {
        $ldapEntry->add( mailBoxServer => $entry->{"mailShare_mailLocalServer"} );
    }

    # Les adresses mails
    if( $entry->{"mailshare_mail"} ) {
        $ldapEntry->add( mail => $entry->{"mailshare_mail"} );
    }

    # Les adresses mails secondaires
    if( $entry->{"mailshare_mail_alias"} ) {
        $ldapEntry->add( mailAlias => $entry->{"mailshare_mail_alias"} );
    }

    # L'acces mail
    if( $entry->{"mailshare_mailperms"} ) {
        $ldapEntry->add( mailAccess => "PERMIT" );
    }else {
        $ldapEntry->add( mailAccess => "REJECT" );
    }

    # Le domaine
    if( $entry->{"mailshare_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"mailshare_domain"}, -charset => $defaultCharSet }) );
    }


    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"mailShareDesc"};
    my $update = 0;

    # Le nom de la BAL
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_mailbox"}, $ldapEntry, "mailbox" ) ) {
        $update = 1;
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_description"}, $ldapEntry, "description" ) ) {
        $update = 1;
    }

    # Le cas des alias mails
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail"}, $ldapEntry, "mail" ) ) {
        $update = 1;
    }

    # Le cas des alias mails secondaires
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"mailshare_mail_alias"}, $ldapEntry, "mailAlias" ) ) {
        $update = 1;
    }

    # L'acces au mail
    if( $entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }elsif( !$entry->{"mailshare_mailperms"} && (&OBM::Ldap::utils::modifyAttr( "PERMIT", $ldapEntry, "mailAccess" )) ) {
        $update = 1;

    }

    # Le serveur de BAL local
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailShare_mailLocalServer"}, $ldapEntry, "mailBoxServer" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"mailshare_domain"}, $ldapEntry, "obmDomain" ) ) {
        $update = 1;
    }

    return $update;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub getMailServerRef {
    my $self = shift;
    my( $domainId, $mailServerId ) = @_;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $$domainId = $self->{"domainId"};
        $$mailServerId = $self->{"mailShareDesc"}->{"mailShare_server"};

    }else {
        $$domainId = undef;
        $$mailServerId = undef;

    }

    return 1;
}


sub getMailboxPrefix {
    my $self = shift;

    return "";
}


sub getMailboxName {
    my $self = shift;
    my $mailShareName = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareName = $self->{"mailShareDesc"}->{"mailShare_mailbox"};
    }

    return $mailShareName;
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}


sub getMailboxQuota {
    my $self = shift;
    my $mailShareQuota = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareQuota = $self->{"mailShareDesc"}->{"user_mailbox_quota"};
    }

    return $mailShareQuota;
}

sub getMailboxAcl {
    my $self = shift;
    my $mailShareAcl = undef;

    if( $self->{"mailShareDesc"}->{"mailshare_mailperms"} ) {
        $mailShareAcl = $self->{"mailShareDesc"}->{"user_mailshare_acl"};
    }

    return $mailShareAcl;
}
