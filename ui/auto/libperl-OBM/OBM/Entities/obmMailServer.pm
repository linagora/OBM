package OBM::Entities::obmMailServer;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(getType setDelete getDelete getArchive getLdapObjectclass isLinks getEntityId _log);
use OBM::Parameters::common;
require OBM::Parameters::ldapConf;
require OBM::Tools::obmDbHandler;
require OBM::Ldap::utils;
use URI::Escape;


sub new {
    my $self = shift;
    my( $links, $deleted ) = @_;

    my %obmMailServerConfAttr = (
        type => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        domainId => undef,
        postfixConf => undef,
        objectclass => undef,
        dnPrefix => undef,
        dnValue => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        $self->_log( 'Usage: PACKAGE->new(LINKS)', 1 );
        return undef;
    }

    $obmMailServerConfAttr{"links"} = $links;
    $obmMailServerConfAttr{"toDelete"} = $deleted;

    $obmMailServerConfAttr{"type"} = $OBM::Parameters::ldapConf::MAILSERVER;

    # Définition de la représentation LDAP de ce type
    $obmMailServerConfAttr{objectclass} = $OBM::Parameters::ldapConf::attributeDef->{$obmMailServerConfAttr{"type"}}->{objectclass};
    $obmMailServerConfAttr{dnPrefix} = $OBM::Parameters::ldapConf::attributeDef->{$obmMailServerConfAttr{"type"}}->{dn_prefix};
    $obmMailServerConfAttr{dnValue} = $OBM::Parameters::ldapConf::attributeDef->{$obmMailServerConfAttr{"type"}}->{dn_value};

    bless( \%obmMailServerConfAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $domainDesc ) = @_;


    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( '[Entities::obmMailServer]: connecteur a la base de donnee invalide', 3 );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( '[Entities::obmMailServer]: description de domaine OBM incorrecte', 3 );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    $self->_log( '[Entities::obmMailServer]: gestion de la configuration de postfix, domaine \''.$domainDesc->{'domain_label'}.'\'', 1 );

    $self->{"postfixConf"}->{"postfixconf_name"} = $domainDesc->{"domain_label"};
    $self->{"postfixConf"}->{"postfixconf_domain"} = $domainDesc->{"domain_label"};

    $self->{"postfixConf"}->{"postfixconf_mail_domains"} = [];
    push( @{$self->{"postfixConf"}->{"postfixconf_mail_domains"}}, $domainDesc->{"domain_name"} );
    for( my $i=0; $i<=$#{$domainDesc->{"domain_alias"}}; $i++ ) {
        push( @{$self->{"postfixConf"}->{"postfixconf_mail_domains"}}, $domainDesc->{"domain_alias"}->[$i] );
        
    }


    # Obtention des serveurs de mail du domaine
    my $query = "SELECT i.host_name, k.domainmailserver_role FROM Host i, MailServer j, DomainMailServer k WHERE i.host_id=j.mailserver_host_id AND j.mailserver_id=k.domainmailserver_mailserver_id AND k.domainmailserver_domain_id=".$self->{"domainId"};

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    while( my( $hostName, $srvRole ) = $queryResult->fetchrow_array() ) {
        SWITCH: {
            if( $srvRole =~ /^imap$/i ) {
                push( @{$self->{"postfixConf"}->{"postfixconf_imap_srv"}}, $hostName );
                last SWITCH;
            }

            if( $srvRole =~ /^smtp_in$/i ) {
                push( @{$self->{"postfixConf"}->{"postfixconf_smtpin_srv"}}, $hostName );
                last SWITCH;
            }

            if( $srvRole =~ /^smtp_out$/i ) {
                push( @{$self->{"postfixConf"}->{"postfixconf_smtpout_srv"}}, $hostName );
                last SWITCH;
            }
        }
    }


    return 1;
}


sub updateDbEntity {
    my $self = shift;
    # Pas de tables de production pour le type obmMailServer. Ces informations
    # font parties des informations de domaines

#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;
}


sub updateDbEntityLinks {
    my $self = shift;
    # Pas de tables de production pour le type obmMailServer. Ces informations
    # font parties des informations de domaines
    
#    my $dbHandler = OBM::Tools::obmDbHandler->instance();
#    if( !defined($dbHandler) ) {
#        return 0;
#    }

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $domainDesc ) = @_;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $entry = $self->{"postfixConf"};
    my $description = "";


    if( defined($entry->{postfixconf_domain}) ) {
        $description .= "domaine '".$entry->{postfixconf_domain}."'";
    }

    if( ($description ne "") && defined($self->{type}) ) {
        $description .= ", type '".$self->{type}."'";
    }

    if( $description ne "" ) {
        return $description;
    }

    if( defined($self->{domainId}) ) {
        $description .= "ID BD '".$self->{domainId}."'";
    }

    if( defined($self->{type}) ) {
        $description .= ",type '".$self->{type}."'";
    }

    return $description;
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"dnPrefix"}) && defined($self->{"postfixConf"}->{$self->{"dnValue"}}) ) {
        $dnPrefix = $self->{"dnPrefix"}."=".$self->{"postfixConf"}->{$self->{"dnValue"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{'postfixConf'};


    if( !defined($entry->{'postfixconf_name'}) ) {
        return 0;
    }

    # On construit la nouvelle entree
    #
    # Les parametres nécessaires
    $ldapEntry->add(
        objectClass => $self->{'objectclass'},
        cn => $entry->{'postfixconf_name'}
    );

    # Les domaines de messagerie
    if( $entry->{'postfixconf_mail_domains'} ) {
        $ldapEntry->add( myDestination => $entry->{'postfixconf_mail_domains'} );
    }

    # Le domaine
    if( $entry->{'postfixconf_domain'} ) {
        $ldapEntry->add( obmDomain => $entry->{'postfixconf_domain'} );
    }

    # Les hôtes IMAP
    if( $entry->{'postfixconf_imap_srv'} ) {
        $ldapEntry->add( imapHost => $entry->{'postfixconf_imap_srv'} );
    }

    # Les hôtes SMTP-in
    if( $entry->{'postfixconf_smtpin_srv'} ) {
        $ldapEntry->add( smtpInHost => $entry->{'postfixconf_smtpin_srv'} );
    }

    # Les hôtes SMTP-out
    if( $entry->{'postfixconf_smtpout_srv'} ) {
        $ldapEntry->add( smtpOutHost => $entry->{'postfixconf_smtpout_srv'} );
    }

    return 1;
}


sub updateLdapEntryDn {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;

    if( !defined($ldapEntry) ) {
        return 0;
    }

    return $update;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry, $objectclassDesc ) = @_;
    my $entry = $self->{"postfixConf"};

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    if( !defined($ldapEntry) ) {
        return undef;
    }


    # Les domaines de messagerie
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_mail_domains"}, $ldapEntry, "myDestination" ) ) {
        $update->setUpdate();
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"postfixconf_domain"}, $ldapEntry, "obmDomain") ) {
        $update->setUpdate();
    }

    # Les hôtes IMAP
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_imap_srv"}, $ldapEntry, "imapHost" ) ) {
        $update->setUpdate();
    }

    # Les hôtes SMTP-in
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_smtpin_srv"}, $ldapEntry, "smtpInHost" ) ) {
        $update->setUpdate();
    }

    # Les hôtes SMTP-out
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_smtpout_srv"}, $ldapEntry, "smtpOutHost" ) ) {
        $update->setUpdate();
    }


    if( $self->isLinks() ) {
        if( $self->updateLdapEntryLinks( $ldapEntry ) ) {
            $update->setUpdate();
        }
    }


    return $update;
}


sub updateLdapEntryLinks {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $update = 0;


    if( !defined($ldapEntry) ) {
        return 0;
    }


    return $update;
}


sub getMailboxName {
    my $self = shift;

    return undef;
}


sub getMailboxPartition {
    my $self = shift;

    return undef;
}


sub getMailboxSieve {
    my $self = shift;

    return $self->{"sieve"};
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}
