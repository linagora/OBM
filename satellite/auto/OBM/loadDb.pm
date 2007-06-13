package OBM::loadDb;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


require OBM::toolBox;
require OBM::ldap;
require OBM::imapd;
require OBM::Ldap::ldapEngine;
require OBM::Cyrus::cyrusEngine;
require OBM::Cyrus::sieveEngine;
require OBM::Entities::obmRoot;
require OBM::Entities::obmDomainRoot;
require OBM::Entities::obmNode;
require OBM::Entities::obmSystemUser;
require OBM::Entities::obmUser;
require OBM::Entities::obmGroup;
require OBM::Entities::obmMailshare;
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;


sub new {
    my $self = shift;
    my( $dbHandler, $parameters ) = @_;

    # Definition des attributs de l'objet
    my %loadDbAttr = (
        filter => undef,
        user => undef,
        domain => undef,
        delegation => undef,
        all => undef,
        dbHandler => undef,
        domainList => undef,
        engine => {
            ldapEngine => undef,
            cyrusEngine => undef,
            sieveEngine => undef
        }
    );


    if( !defined($dbHandler) || !defined($parameters) ) {
        croak( "Usage: PACKAGE->new(DBHANDLER, PARAMLIST)" );
    }elsif( !exists($parameters->{"user"}) && !exists($parameters->{"domain"}) && !exists($parameters->{"delegation" }) ) {
        croak( "Usage: PARAMLIST: table de hachage avec les cle 'user', 'domain' et 'delegation'" );
    }

    # Initialisation de l'objet
    $loadDbAttr{"all"} = $parameters->{"all"};
    $loadDbAttr{"dbHandler"} = $dbHandler;

    SWITCH: {
        if( defined($parameters->{"user"}) ) {
            $loadDbAttr{"user"} = $parameters->{"user"};

            # On recupere l'id du domaine de l'utilisateur
            my $queryResult;
            my $query = "SELECT userobm_domain_id FROM P_UserObm WHERE userobm_id=".$loadDbAttr{"user"};
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
                &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
                if( defined($queryResult) ) {
                    &OBM::toolBox::write_log( $queryResult->err, "W" );
                }
            }else {
                my $results = $queryResult->fetchall_arrayref();
                if( $#$results != 0 ) {
                    &OBM::toolBox::write_log( "lodaDb: utilisateur inexistant", "W" );
                    return undef;
                }else {
                    $loadDbAttr{"domain"} = $results->[0]->[0];
                }
            }

            $loadDbAttr{"filter"} = "user";
            last SWITCH;
        }

        if( defined($parameters->{"domain"}) ) {
            $loadDbAttr{"domain"} = $parameters->{"domain"};
            $loadDbAttr{"filter"} = "domain";
            last SWITCH;
        }

        if( defined($parameters->{"delegation"}) ) {
            $loadDbAttr{"delegation"} = $parameters->{"delegation"};
            $loadDbAttr{"filter"} = "delegation";
            last SWITCH;
        }
    }

    # Obtention des informations sur les domaines nécessaires
    if( defined($loadDbAttr{"domain"}) ) {
        $loadDbAttr{"domainList"} = &OBM::toolBox::getDomains( $loadDbAttr{"dbHandler"}, $loadDbAttr{"domain"} );
    }else {
        $loadDbAttr{"domainList"} = &OBM::toolBox::getDomains( $loadDbAttr{"dbHandler"}, undef );
    }

    # Obtention des serveurs LDAP par domaines
    &OBM::ldap::getServerByDomain( $loadDbAttr{"dbHandler"}, $loadDbAttr{"domainList"} );

    # Parametrage des serveurs IMAP par domaine
    &OBM::imapd::getServerByDomain( $loadDbAttr{"dbHandler"}, $loadDbAttr{"domainList"} );
    if( !&OBM::imapd::getAdminImapPasswd( $loadDbAttr{"dbHandler"}, $loadDbAttr{"domainList"} ) ) {
        exit;
    }

    # initialisation des moteurs nécessaires
    if( $OBM::Parameters::common::obmModules->{"ldap"} || $OBM::Parameters::common::obmModules->{"web"} ) {
        $loadDbAttr{"engine"}->{"ldapEngine"} = OBM::Ldap::ldapEngine->new( $loadDbAttr{"domainList"} );
        $loadDbAttr{"engine"}->{"ldapEngine"}->init();
    }

    if( $OBM::Parameters::common::obmModules->{"mail"} ) {
        $loadDbAttr{"engine"}->{"cyrusEngine"} = OBM::Cyrus::cyrusEngine->new( $loadDbAttr{"domainList"} );
        $loadDbAttr{"engine"}->{"cyrusEngine"}->init();

        $loadDbAttr{"engine"}->{"sieveEngine"} = OBM::Cyrus::sieveEngine->new( $loadDbAttr{"domainList"} );
        $loadDbAttr{"engine"}->{"sieveEngine"}->init();
    }


    bless( \%loadDbAttr, $self );
}


sub destroy {
    my $self = shift;

    my $engines = $self->{"engine"};
    while( my( $engineType, $engine ) = each(%{$engines}) ) {
        if( defined($engine) ) {
            $engine->destroy();
        }
    }
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub update {
    my $self = shift;

    if( $self->{"all"} ) {
        $self->_doAll();
    }
}


sub _doAll {
    my $self = shift;
    my $queryResult;

    if( !defined($self->{"domain"}) ) {
        &OBM::toolBox::write_log( "loadDb: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }

    # Traitement des entités de type 'utilisateur système'
    my $query = "SELECT usersystem_id FROM UserSystem";
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $systemUserId ) = $queryResult->fetchrow_array() ) {
        $self->_dosystemUser( 0, $systemUserId );
    }


    if( $self->{"domain"} != 0 ) {
        # Traitemtent des entités de type 'utilisateur'
        $query = "SELECT userobm_id FROM UserObm WHERE userobm_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $userId ) = $queryResult->fetchrow_array() ) {
            $self->_doUser( 0, $userId );
        }

        # Traitement des entités de type 'groupe'
        $query = "SELECT group_id FROM UGroup WHERE group_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $groupId ) = $queryResult->fetchrow_array() ) {
            $self->_doGroup( 0, $groupId );
        }

        # Traitement des entités de type 'mailshare'
        $query = "SELECT mailshare_id FROM MailShare WHERE mailshare_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $mailshareId ) = $queryResult->fetchrow_array() ) {
            $self->_doMailShare( 0, $mailshareId );
        }
    }

    return 1;
}


sub _findDomainbyId {
    my $self = shift;
    my( $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$self->{"domainList"}}; $i++ ) {
        if( $self->{"domainList"}->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $self->{"domainList"}->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub _dosystemUser {
    my $self = shift;
    my( $incremental, $systemUserId ) = @_;

    if( !defined($systemUserId) || $systemUserId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $systemUserObject = OBM::Entities::obmSystemUser->new( $incremental, $systemUserId );
    $systemUserObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    $self->_runEngines( $systemUserObject );

    return 1;
}


sub _doUser {
    my $self = shift;
    my( $incremental, $userId ) = @_;

    if( !defined($userId) || $userId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $userObject = OBM::Entities::obmUser->new( $incremental, $userId );
    $userObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    $self->_runEngines( $userObject );

    return 1;
}


sub _doGroup {
    my $self = shift;
    my( $incremental, $groupId ) = @_;

    if( !defined($groupId) || $groupId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $groupObject = OBM::Entities::obmGroup->new( $incremental, $groupId );
    $groupObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    $self->_runEngines( $groupObject );

    return 1;
}


sub _doMailShare {
    my $self =shift;
    my( $incremental, $mailshareId ) = @_;

    if( !defined($mailshareId) || $mailshareId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $mailShareObject = OBM::Entities::obmMailshare->new( $incremental, $mailshareId );
    $mailShareObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    $self->_runEngines( $mailShareObject );

    return 1;
}


sub _runEngines {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        return 0;
    }

    my $engines = $self->{"engine"};
    while( my( $engineType, $engine ) = each(%{$engines}) ) {
        $engine->update( $object );
    }

    return 1;
}
