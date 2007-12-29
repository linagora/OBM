package OBM::Entities::obmMailServer;

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
use URI::Escape;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);


sub new {
    my $self = shift;
    my( $links, $deleted ) = @_;

    my %obmMailServerConfAttr = (
        type => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        archive => undef,
        sieve => undef,
        domainId => undef,
        postfixConf => undef
    );


    if( !defined($links) || !defined($deleted) ) {
        croak( "Usage: PACKAGE->new(LINKS)" );

    }

    $obmMailServerConfAttr{"links"} = $links;
    $obmMailServerConfAttr{"toDelete"} = $deleted;

    $obmMailServerConfAttr{"type"} = $MAILSERVER;
    $obmMailServerConfAttr{"typeDesc"} = $attributeDef->{$obmMailServerConfAttr{"type"}};

    bless( \%obmMailServerConfAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;


    if( !defined($dbHandler) ) {
        &OBM::toolBox::write_log( "[Entities::obmMailServer]: connecteur a la base de donnee invalide", "W" );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Entities::obmMailServer]: description de domaine OBM incorrecte", "W" );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    &OBM::toolBox::write_log( "[Entities::obmMailServer]: gestion de la configuration de postfix, domaine '".$domainDesc->{"domain_label"}."'", "W" );

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
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Entities::obmUser]: probleme lors de l'execution d'une requete SQL : ".$dbHandler->err, "W" );
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

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $dbHandler, $domainDesc ) = @_;

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


sub isLinks {
    my $self = shift;

    return $self->{"links"};
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"postfixConf"}->{$self->{"typeDesc"}->{"dn_value"}}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"postfixConf"}->{$self->{"typeDesc"}->{"dn_value"}};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"postfixConf"};


    if( !defined($entry->{"postfixconf_name"}) ) {
        return 0;
    }

    # On construit la nouvelle entree
    #
    # Les parametres nécessaires
    $ldapEntry->add(
        objectClass => $self->{"typeDesc"}->{"objectclass"},
        cn => to_utf8({ -string => $entry->{"postfixconf_name"}, -charset => $defaultCharSet })
    );

    # Les domaines de messagerie
    if( $entry->{"postfixconf_mail_domains"} ) {
        $ldapEntry->add( myDestination => $entry->{"postfixconf_mail_domains"} );
    }

    # Le domaine
    if( $entry->{"postfixconf_domain"} ) {
        $ldapEntry->add( obmDomain => to_utf8({ -string => $entry->{"postfixconf_domain"}, -charset => $defaultCharSet }) );
    }

    # Les hôtes IMAP
    if( $entry->{"postfixconf_imap_srv"} ) {
        $ldapEntry->add( imapHost => $entry->{"postfixconf_imap_srv"} );
    }

    # Les hôtes SMTP-in
    if( $entry->{"postfixconf_smtpin_srv"} ) {
        $ldapEntry->add( smtpInHost => $entry->{"postfixconf_smtpin_srv"} );
    }

    return 1;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{"postfixConf"};
    my $update = 0;

    # Les domaines de messagerie
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_mail_domains"}, $ldapEntry, "myDestination" ) ) {
        $update = 1;
    }

    # Le domaine
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"postfixconf_domain"}, $ldapEntry, "obmDomain") ) {
        $update = 1;
    }

    # Les hôtes IMAP
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_imap_srv"}, $ldapEntry, "imapHost" ) ) {
        $update = 1;
    }

    # Les hôtes SMTP-in
    if( &OBM::Ldap::utils::modifyAttrList( $entry->{"postfixconf_smtpin_srv"}, $ldapEntry, "smtpInHost" ) ) {
        $update = 1;
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
