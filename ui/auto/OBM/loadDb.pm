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
require OBM::Entities::obmPostfixConf;
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
            my $query = "SELECT userobm_domain_id, userobm_login FROM P_UserObm WHERE userobm_id=".$loadDbAttr{"user"};
            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
                &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
                if( defined($queryResult) ) {
                    &OBM::toolBox::write_log( $queryResult->err, "W" );
                }

                return 0;
            }else {
                my $results = $queryResult->fetchall_arrayref();
                if( $#$results != 0 ) {
                    &OBM::toolBox::write_log( "lodaDb: utilisateur inexistant", "W" );
                    return undef;
                }else {
                    $loadDbAttr{"domain"} = $results->[0]->[0];
                    $loadDbAttr{"user_name"} = $results->[0]->[1];
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
        if( !$loadDbAttr{"engine"}->{"ldapEngine"}->init() ) {
            delete( $loadDbAttr{"engine"}->{"ldapEngine"} );
        }
    }

    if( $OBM::Parameters::common::obmModules->{"mail"} ) {
        $loadDbAttr{"engine"}->{"cyrusEngine"} = OBM::Cyrus::cyrusEngine->new( $loadDbAttr{"domainList"} );
        if( !$loadDbAttr{"engine"}->{"cyrusEngine"}->init() ) {
            delete( $loadDbAttr{"engine"}->{"cyrusEngine"} );
        }

        $loadDbAttr{"engine"}->{"sieveEngine"} = OBM::Cyrus::sieveEngine->new( $loadDbAttr{"domainList"} );
        if( !$loadDbAttr{"engine"}->{"sieveEngine"}->init() ) {
            delete( $loadDbAttr{"engine"}->{"sieveEngine"} );
        }
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
    my $return = 1;

    if( $self->{"all"} ) {
        $return = $self->_doAll();
    }else {
        $return = $self->_doIncremental();
    }

    return $return;
}


sub _doAll {
    my $self = shift;
    my $queryResult;

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "loadDb: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainDesc = $self->_findDomainbyId( $self->{"domain"} );


    &OBM::toolBox::write_log( "loadDb: MAJ totale pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );

    if( $self->{"domain"} == 0 ) {
       # Traitement des entités de type 'utilisateur système'
       my $query = "SELECT usersystem_id FROM UserSystem";
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $systemUserId ) = $queryResult->fetchrow_array() ) {
            $self->_doSystemUser( 0, 0, $systemUserId );
        }
    }


    if( $self->{"domain"} != 0 ) {
        # Traitemtent des entités de type 'utilisateur'
        my $query = "SELECT userobm_id FROM UserObm WHERE userobm_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $userId ) = $queryResult->fetchrow_array() ) {
            $self->_doUser( 0, 0, $userId );
        }

        # Traitement des entités de type 'groupe'
        $query = "SELECT group_id FROM UGroup WHERE group_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $groupId ) = $queryResult->fetchrow_array() ) {
            $self->_doGroup( 0, 0, $groupId );
        }

        # Traitement des entités de type 'mailshare'
        $query = "SELECT mailshare_id FROM MailShare WHERE mailshare_domain_id=".$self->{"domain"};
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "loadDb: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $mailshareId ) = $queryResult->fetchrow_array() ) {
            $self->_doMailShare( 0, 0, $mailshareId );
        }

        # Traitement des entités de type 'postfixConf'
        $self->_doPostfixConf( 0, 0 );
    }

    return 1;
}


sub _doIncremental {
    my $self = shift;
    my $return = 1;
    my $domainDesc;


    if( defined($self->{"domain"}) ) {
        $domainDesc = $self->_findDomainbyId( $self->{"domain"} );
    }

    my %sqlFilter;
    if( defined($self->{"user"}) ) {
        # Si le paramètre utilisateur est indiqué, on fait une MAJ incrémentale par
        # utilisateur
        &OBM::toolBox::write_log( "loadDb: MAJ incrementale pour l'utilisateur '".$self->{"user_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
        $sqlFilter{"updated"} = "updated_user_id=".$self->{"user"};
        $sqlFilter{"deleted"} = "deleted_user_id=".$self->{"user"};

    }elsif( defined($self->{"delegation"}) ) {
        # Si le paramètre délégation est indiqué, on fait une MAJ incrémentale
        # par délégation
        &OBM::toolBox::write_log( "loadDb: MAJ incrementale pour la delegation '".$self->{"delegation"}."'", "W" );
        $sqlFilter{"updated"} = "updated_delegation='".$self->{"delegation"}."'";
        $sqlFilter{"deleted"} = "deleted_delegation=".$self->{"user"};
    
    }elsif( defined($self->{"domain"}) ) {
        # Si le paramètre domaine est indiqué, on fait une MAJ incrémentale
        # par domaine
        &OBM::toolBox::write_log( "loadDb: MAJ incrementale pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );
        $sqlFilter{"updated"} = "updated_domain_id='".$self->{"domain"}."'";
        $sqlFilter{"deleted"} = "deleted_domain_id=".$self->{"user"};
        
    }else {
        return 0;
    }

    # Mises à jour
    $return = $return && $self->_incrementalUpdate( $sqlFilter{"updated"} );
    # Suppression
    $return = $return && $self->_incrementalDelete( $sqlFilter{"deleted"} );


    return $return;
}


sub _incrementalUpdate {
    my $self = shift;
    my( $sqlFilter ) = @_;
    my $globalReturn = 1;


    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $sqlQuery = "SELECT updated_id, updated_table, updated_entity_id, updated_type FROM Updated";
    if( defined($sqlFilter) ) {
        $sqlQuery .= " WHERE ".$sqlFilter;
    }

    if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }

    while( my( $updatedId, $updatedTable, $updatedEntityId ) = $queryResult->fetchrow_array() ) {
        if( !defined($updatedId) || !defined($updatedTable) || !defined($updatedEntityId) ) {
            next;
        }

        my $return = 1;
        SWITCH: {
            if( lc($updatedTable) eq "userobm" ) {
                $return = $self->_doUser( 1, 0, $updatedEntityId );
                last SWITCH;
            }

            if( lc($updatedTable) eq "ugroup" ) {
                $return = $self->_doGroup( 1, 0, $updatedEntityId );
                last SWITCH;
            }

            if( lc($updatedTable) eq "mailshare" ) {
                $return = $self->_doMailShare( 1, 0, $updatedEntityId );
                last SWITCH;
            }

            next;
        }

        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $return = $self->_updateDbEntity( $updatedTable, $updatedEntityId );

            if( $return ) {
                # MAJ de la BD de travail ok, on nettoie les tables de MAJ
                # incrémentales
                $return = $self->_updateIncrementalTable( "Updated", $updatedId );
                
            }
        }

        if( !$return ) {
            $globalReturn = 0;
        }
    }

    return $globalReturn;
}


sub _incrementalDelete {
    my $self = shift;
    my( $sqlFilter ) = @_;
    my $globalReturn = 1;


    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $sqlQuery = "SELECT deleted_id, deleted_table, deleted_entity_id FROM Deleted";
    if( defined($sqlFilter) ) {
        $sqlQuery .= " WHERE ".$sqlFilter;
    }

    if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }

    while( my( $deletedId, $deletedTable, $deletedEntityId ) = $queryResult->fetchrow_array() ) {
        if( !defined($deletedId) || !defined($deletedTable) || !defined($deletedEntityId) ) {
            next;
        }

        my $return = 1;
        SWITCH: {
            if( lc($deletedTable) eq "userobm" ) {
                $return = $self->_doUser( 1, 1, $deletedEntityId );
                last SWITCH;
            }

            if( lc($deletedTable) eq "ugroup" ) {
                $return = $self->_doGroup( 1, 1, $deletedEntityId );
                last SWITCH;
            }

            if( lc($deletedTable) eq "mailshare" ) {
                $return = $self->_doMailShare( 1, 1, $deletedEntityId );
                last SWITCH;
            }

            next;
        }

        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $return = $self->_deleteDbEntity( $deletedTable, $deletedEntityId );

            if( $return ) {
                # MAJ de la BD de travail ok, on nettoie les tables de MAJ
                # incrémentales
                $return = $self->_updateIncrementalTable( "Deleted", $deletedId );
                
            }
        }

        if( !$return ) {
            $globalReturn = 0;
        }
    }

    return $globalReturn;
 }


sub _updateDbEntity {
    my $self = shift;
    my( $table, $id ) = @_;

    if( !defined($table) ) {
        return 0;
    }

    if( !defined($id) || ($id !~ /^\d+$/) )  {
        return 0;
    }


    # Gestion des ecxeptions
    my $columnPrefix = $self->_tableNamePrefix( $table );


    # On copie les informations de l'entité de la table de travail vers la table
    # de production
    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $query = "SELECT * FROM ".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }

    while( my $hashResult = $queryResult->fetchrow_hashref() ) {
        my $updateQuery = "UPDATE P_".$table." SET ";

        my $first = 1;
        while( my( $key, $value ) = each(%{$hashResult}) ) {
            if( !$first ) {
                $updateQuery .= ", ";
            }

            $updateQuery .= $key."=".$dbHandler->quote($value);

            $first = 0;
        }

        $updateQuery .= " WHERE ".$columnPrefix."_id=".$id;

        # On exécute la requête
        my $updateQueryResult;
        if( !&OBM::dbUtils::execQuery( $updateQuery, $dbHandler, \$updateQueryResult ) ) {
            &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
            if( defined($updateQueryResult) ) {
                &OBM::toolBox::write_log( $updateQueryResult->err, "W" );
            }

            return 0;
        }
    }

    return 1;
}


sub _deleteDbEntity {
    my $self = shift;
    my ( $table, $id ) = @_;

    if( !defined($table) ) {
        return 0;
    }

    if( !defined($id) || ($id !~ /^\d+$/) )  {
        return 0;
    }


    # Gestion des ecxeptions
    my $columnPrefix = $self->_tableNamePrefix( $table );


    # On supprime les informations de l'entité de la table de travail
    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $query = "DELETE FROM ".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }

    # et de la table de production
    $query = "DELETE FROM P_".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( $queryResult->err, "W" );
        }

        return 0;
    }


    return 1;
}


sub _updateIncrementalTable {
    my $self = shift;
    my( $table, $id ) = @_;

    if( !defined($table) ) {
        return 0;
    }

    if( !defined($id) || ($id !~ /^\d+$/) ) {
        return 0;
    }


    my $dbHandler = $self->{"dbHandler"};
    my $deleteQueryResult;
    my $query = "DELETE FROM ".$table." WHERE ".lc($table)."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$deleteQueryResult ) ) {
        &OBM::toolBox::write_log( "lodaDb: probleme lors de l'execution de la requete", "W" );
        if( defined($deleteQueryResult) ) {
            &OBM::toolBox::write_log( $deleteQueryResult->err, "W" );
        }

        return 0;
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


sub _doSystemUser {
    my $self = shift;
    my( $incremental, $delete, $systemUserId ) = @_;
    my $return = 1;

    if( !defined($systemUserId) || $systemUserId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $systemUserObject = OBM::Entities::obmSystemUser->new( $incremental, $systemUserId );
    $return = $systemUserObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( $return ) {
        if( $delete ) {
            $systemUserObject->setDelete();
        }

        $return = $self->_runEngines( $systemUserObject );
    }else {
        $return = 1;
    }


    return $return;
}


sub _doUser {
    my $self = shift;
    my( $incremental, $delete, $userId ) = @_;
    my $return = 1;

    if( !defined($userId) || ($userId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $userObject = OBM::Entities::obmUser->new( $incremental, $userId );
    $return = $userObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( $return ) {
        if( $delete ) {
            $userObject->setDelete();
        }

        $return = $self->_runEngines( $userObject );
    }else {
        $return = 1;
    }

    return $return;
}


sub _doGroup {
    my $self = shift;
    my( $incremental, $delete, $groupId ) = @_;
    my $return = 1;

    if( !defined($groupId) || $groupId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $groupObject = OBM::Entities::obmGroup->new( $incremental, $groupId );
    $return = $groupObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( $return ) {
        if( $delete ) {
            $groupObject->setDelete();
        }

        $return = $self->_runEngines( $groupObject );
    }else {
        $return = 1;
    }

    return $return;
}


sub _doMailShare {
    my $self =shift;
    my( $incremental, $delete, $mailshareId ) = @_;
    my $return = 1;

    if( !defined($mailshareId) || $mailshareId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $mailShareObject = OBM::Entities::obmMailshare->new( $incremental, $mailshareId );
    $return = $mailShareObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( $return ) {
        if( $delete ) {
            $mailShareObject->setDelete();
        }

        $return = $self->_runEngines( $mailShareObject );
    }else {
        $return = 1;
    }

    return $return;
}


sub _doPostfixConf {
    my $self = shift;
    my( $incremental, $delete ) = 0;
    my $return = 1;

    if( !defined($incremental) ) {
        $incremental = 0;
    }

    my $postfixConfObject = OBM::Entities::obmPostfixConf->new( $incremental );
    $postfixConfObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( $return ) {
        if( $delete ) {
            $postfixConfObject->setDelete();
        }

        $return = $self->_runEngines( $postfixConfObject );
    }else {
        $return = 1;
    }

    return $return;
}


sub _runEngines {
    my $self = shift;
    my( $object ) = @_;
    my $return = 1;

    if( !defined($object) ) {
        return 0;
    }

    my $engines = $self->{"engine"};
    while( (my( $engineType, $engine ) = each(%{$engines})) && $return ) {
        $return = $engine->update( $object );
    }

    if( !$return ) {
        keys(%{$engines});
    }

    return $return;
}


sub _tableNamePrefix {
    my $self = shift;
    my( $table ) = @_;
    my $columnPrefix;

    if( lc($table) eq "ugroup" ) {
        $columnPrefix = "group";
    }else {
        $columnPrefix = lc($table);
    }

    return $columnPrefix;
}
