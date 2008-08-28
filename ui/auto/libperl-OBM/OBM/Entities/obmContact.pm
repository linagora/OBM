package OBM::Entities::obmContact;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Entities::commonEntities qw(_log);
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;
require OBM::Ldap::utils;
require OBM::Tools::obmDbHandler;
use URI::Escape;


sub new {
    my $self = shift;
    my( $links, $deleted, $contactId, $domainId ) = @_;

    my %obmContactAttr = (
        type => undef,
        typeDesc => undef,
        links => undef,
        toDelete => undef,
        sieve => undef,
        contactId => undef,
        domainId => undef,
        contactDesc => undef,
        contactDbDesc => undef
    );

    if( !defined($links) || !defined($deleted) || !defined($contactId) ) {
        $self->_log( 'Usage: PACKAGE->new(LINKS, DELETED, CONTACTID)', 1 );
        return undef;
    }elsif( $contactId !~ /^\d+$/ ) {
        $self->_log( "[Entities::obmContact]: Identifiant de contact incorrect", 2 );
        return undef;
    }else {
        $obmContactAttr{"contactId"} = $contactId;
    }

    if( defined($domainId) && ($domainId !~ /^\d+$/) ) {
        $self->_log( "[Entities::obmContact]: Identifiant de domaine incorrect", 2 );
        return undef;
    }elsif( defined($domainId) ) {
        $obmContactAttr{"domainId"} = $domainId;
    }

    $obmContactAttr{"links"} = $links;
    $obmContactAttr{"toDelete"} = $deleted;
    $obmContactAttr{"sieve"} = 0;

    $obmContactAttr{"type"} = $CONTACTS;
    $obmContactAttr{"typeDesc"} = $attributeDef->{$obmContactAttr{"type"}};
    $obmContactAttr{"contactDesc"}->{"contact_uid"} = $contactId;

    bless( \%obmContactAttr, $self );
}


sub getEntity {
    my $self = shift;
    my( $domainDesc ) = @_;
    my $contactId = $self->{"contactId"};


    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( "[Entities::obmContact]: connecteur a la base de donnee invalide", 3 );
        return 0;
    }

    if( !defined($domainDesc->{"domain_id"}) || ($domainDesc->{"domain_id"} !~ /^\d+$/) ) {
        $self->_log( "[Entities::obmContact]: description de domaine OBM incorrecte", 3 );
        return 0;

    }else {
        # On positionne l'identifiant du domaine de l'entité
        $self->{"domainId"} = $domainDesc->{"domain_id"};
    }


    my $query = "SELECT COUNT(*) FROM Contact WHERE contact_id=".$contactId;

    my $queryResult;
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    my( $numRows ) = $queryResult->fetchrow_array();
    $queryResult->finish();

    if( $numRows == 0 ) {
        $self->_log( '[Entities::obmContact]: pas de contact d\'identifiant : '.$contactId, 3 );
        return 0;
    }elsif( $numRows > 1 ) {
        $self->_log( "[Entities::obmContact]: plusieurs contacts d'identifiant : ".$contactId." ???", 3 );
        return 0;
    }

    # La requête a exécuter - obtention des informations sur l'hôte
    $query = "SELECT * FROM Contact LEFT JOIN Company ON contact_company_id=company_id WHERE contact_id=".$contactId;

    # On exécute la requête
    if( !defined($dbHandler->execQuery( $query, \$queryResult )) ) {
        return 0;
    }

    # On range les resultats dans la structure de donnees des resultats
    my $dbContactDesc = $queryResult->fetchrow_hashref();
    $queryResult->finish();


    # Obtention du nom complet du contact
    my $contactCommonName = "";
    if( defined($dbContactDesc->{"contact_firstname"}) && $dbContactDesc->{"contact_firstname"} ne "" ) {
        $contactCommonName .= $dbContactDesc->{"contact_firstname"};
    }
    
    if( defined($dbContactDesc->{"contact_lastname"}) && $dbContactDesc->{"contact_lastname"} ne "" ) {
        if( $contactCommonName ne "" ) {
            $contactCommonName .= " ";
        }
        $contactCommonName .= $dbContactDesc->{"contact_lastname"};
    }

    # Action effectuée
    if( $self->getDelete() ) {
        $self->_log( '[Entities::obmContact]: gestion du contact supprime \''.$contactCommonName.'\', domaine \''.$domainDesc->{"domain_label"}.'\'', 1 );
    }else {
        $self->_log( '[Entities::obmContact]: gestion du contact \''.$contactCommonName.'\', domaine \''.$domainDesc->{'domain_label'}.'\', ID \''.$contactId.'\'', 1 );
    }

    # On stocke les informations utiles dans la structure de donnees des
    # resultats
    $self->{"contactDesc"} = $dbContactDesc;
    $self->{"contactDesc"}->{"contact_uid"} = $contactId;
    $self->{"contactDesc"}->{"contact_common_name"} = $contactCommonName;


    # Si nous ne sommes pas en mode incrémental, on charge aussi les liens de
    # cette entité
    if( $self->isLinks() ) {
        $self->getEntityLinks( $domainDesc );
    }

    return 1;
}


sub updateDbEntity {
    my $self = shift;

    return 1;
}


sub getEntityLinks {
    my $self = shift;
    my( $domainDesc ) = @_;

    return 1;
}


sub getEntityDescription {
    my $self = shift;
    my $dbEntry = $self->{contactDbDesc};
    my $entryProp = $self->{contactDesc};
    my $description = "";

    # Obtention du nom complet du contact
    if( ($description ne "") && defined($entryProp->{"contact_uid"}) ) {
        $description .= "identifiant '".$entryProp->{"contact_uid"}."'";
    }

    if( ($description ne "") && defined($entryProp->{"contact_common_name"}) ) {
        $description .= "common name '".$entryProp->{"contact_common_name"}."'";
    }

    if( ($description ne "") && defined($self->{type}) ) {
        $description .= ", type '".$self->{type}."'";
    }

    if( $description ne "" ) {
        return $description;
    }

    if( defined($self->{contactId}) ) {
        $description .= "ID BD '".$self->{contactId}."'";
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

    return 0;
}


sub isLinks {
    my $self = shift;

    return $self->{"links"};
}


sub getLdapDnPrefix {
    my $self = shift;
    my $dnPrefix = undef;

    if( defined($self->{"typeDesc"}->{"dn_prefix"}) && defined($self->{"contactDesc"}->{"contact_uid"}) ) {
        $dnPrefix = $self->{"typeDesc"}->{"dn_prefix"}."=".$self->{"contactDesc"}->{"contact_uid"};
    }

    return $dnPrefix;
}


sub createLdapEntry {
    my $self = shift;
    my ( $ldapEntry ) = @_;
    my $entry = $self->{"contactDesc"};

    # Les paramètres nécessaires
    if( $entry->{'contact_uid'} ) {
        $ldapEntry->add(
            objectClass => $self->{'typeDesc'}->{'objectclass'},
            uid => $entry->{'contact_uid'},
            cn => $entry->{'contact_common_name'},
            displayName => $entry->{'contact_common_name'}
        );

    }else {
        return 0;
    }

    # Le prénom
    if( $entry->{'contact_firstname'} ) {
        $ldapEntry->add( givenName => $entry->{'contact_firstname'} );
    }

    # Le nom
    if( $entry->{'contact_lastname'} ) {
        $ldapEntry->add( sn => $entry->{"contact_lastname"} );
    }

    # Le titre
    if( $entry->{'contact_title'} ) {
        $ldapEntry->add( title => $entry->{'contact_title'} );
    }

    # La description
    if( $entry->{'contact_comment'} ) {
        $ldapEntry->add( description => $entry->{'contact_comment'} );
    }

    # Le téléphone
    if( $entry->{'contact_phone'} ) {
        $ldapEntry->add( telephoneNumber => $entry->{'contact_phone'} );
    }

    # Le fax
    if( $entry->{'contact_fax'} ) {
        $ldapEntry->add( facsimileTelephoneNumber => $entry->{'contact_fax'} );
    }

    # Le mobile
    if( $entry->{'contact_mobilephone'} ) {
        $ldapEntry->add( mobile => $entry->{'contact_mobilephone'} );
    }

    # La société
    if( $entry->{'company_name'} ) {
        $ldapEntry->add( o => $entry->{'company_name'} );
    }elsif($entry->{'contact_company'}) {
        $ldapEntry->add( o => $entry->{'contact_company'} );
    }

    # Le service
    if( $entry->{'contact_service'} ) {
        $ldapEntry->add( ou => $entry->{'contact_service'} );
    }

    # L'e-mail
    if( $entry->{'contact_email'} ) {
        $ldapEntry->add( mail => $entry->{'contact_email'} );
    }

    # L'adresse
    if( $entry->{'contact_address1'} || $entry->{'contact_address2'} || $entry->{'contact_address3'} ) {
        my $address = '';

        if( defined( $entry->{'contact_address1'} ) ) {
            $address .= $entry->{'contact_address1'};
        }

        if( defined( $address ) ) {
            $address .= "\r\n";
        }

        if( defined( $entry->{'contact_address2'} ) ) {
            $address .= $entry->{'contact_address2'};
        }

        if( defined( $address ) ) {
            $address .= "\r\n";
        }

        if( defined( $entry->{'contact_address3'} ) ) {
            $address .= $entry->{'contact_address3'};
        }   

        # Thunderbird, IceDove... : ne comprennent que cet attribut
        $ldapEntry->add( street => $address );
        # Outlook : ne comprend que cet attribut
        # Outlook Express : préfère celui-là à 'street'
        $ldapEntry->add( postalAddress => $address );

    }

    # Le code postal
    if( $entry->{'contact_zipcode'} ) {
        $ldapEntry->add( postalCode => $entry->{'contact_zipcode'} );
    }

    # La ville
    if( $entry->{'contact_town'} ) {
        $ldapEntry->add( l => $entry->{"contact_town"} );
    }

    return 1;
}


sub updateLdapEntryDn {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $dbContactDesc = $self->{'contactDbDesc'};
    my $update = 0;

    if( !defined($ldapEntry) ) {
        return 0;
    }

    return $update;
}


sub updateLdapEntry {
    my $self = shift;
    my( $ldapEntry ) = @_;
    my $entry = $self->{'contactDesc'};


    if( !defined($ldapEntry) ) {
        return undef;
    }
    

    require OBM::Entities::entitiesUpdateState;
    my $update = OBM::Entities::entitiesUpdateState->new();


    # Le nom commun
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_common_name"}, $ldapEntry, "cn" ) ) {
        $update->setUpdate();
    }

    # Le nom d'affichage
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_common_name"}, $ldapEntry, "displayName" ) ) {
        $update->setUpdate();
    }

    # Le prénom
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_firstname"}, $ldapEntry, "givenName" ) ) {
        $update->setUpdate();
    }

    # Le nom
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_lastname"}, $ldapEntry, "sn" ) ) {
        $update->setUpdate();
    }

    # Le titre
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_title"}, $ldapEntry, "title" ) ) {
        $update->setUpdate();
    }

    # La description
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_comment"}, $ldapEntry, "description" ) ) {
        $update->setUpdate();
    }

    # Le téléphone
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_phone"}, $ldapEntry, "telephoneNumber" ) ) {
        $update->setUpdate();
    }

    # Le fax
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_fax"}, $ldapEntry, "facsimileTelephoneNumber" ) ) {
        $update->setUpdate();
    }

    # Le mobile
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_mobilephone"}, $ldapEntry, "mobile" ) ) {
        $update->setUpdate();
    }

    # La société
    if( $entry->{"company_name"} ) {
        if( &OBM::Ldap::utils::modifyAttr( $entry->{"company_name"}, $ldapEntry, "o" ) ) {
            $update->setUpdate();
        }
        
    }elsif($entry->{"contact_company"}) {
        if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_company"}, $ldapEntry, "o" ) ) {
            $update->setUpdate();
        }
    }

    # Le service
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_service"}, $ldapEntry, "ou" ) ) {
        $update->setUpdate();
    }

    # L'e-mail
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_email"}, $ldapEntry, "mail" ) ) {
        $update->setUpdate();
    }

    # L'adresse
    if( $entry->{"contact_address1"} || $entry->{"contact_address2"} || $entry->{"contact_address3"} ) {
        my $address;

        if( defined( $entry->{"contact_address1"} ) ) {
            $address .= $entry->{"contact_address1"};
        }

        if( defined( $entry->{"contact_address2"} ) ) {
            if( defined( $address ) ) {
                $address .= "\r\n";
            }

            $address .= $entry->{"contact_address2"};
        }

        if( defined( $entry->{"contact_address3"} ) ) {
            if( defined( $address ) ) {
                $address .= "\r\n";
            }

            $address .= $entry->{"contact_address3"};
        }
        
        if( &OBM::Ldap::utils::modifyAttr( $address, $ldapEntry, "street" ) ) {
            &OBM::Ldap::utils::modifyAttr( $address, $ldapEntry, "postalAddress" );
            $update->setUpdate();
        }
    }

    # Le code postal
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_zipcode"}, $ldapEntry, "postalCode" ) ) {
        $update->setUpdate();
    }

    # La ville
    if( &OBM::Ldap::utils::modifyAttr( $entry->{"contact_town"}, $ldapEntry, "l" ) ) {
        $update->setUpdate();
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

    return return $self->{"sieve"};
}
