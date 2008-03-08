package OBM::Update::updateIncremental;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


require OBM::toolBox;
require OBM::imapd;
require OBM::dbUtils;
require OBM::Ldap::ldapEngine;
require OBM::Cyrus::cyrusEngine;
require OBM::Cyrus::sieveEngine;
require OBM::Cyrus::cyrusRemoteEngine;
require OBM::Entities::obmRoot;
require OBM::Entities::obmDomainRoot;
require OBM::Entities::obmNode;
require OBM::Entities::obmSystemUser;
require OBM::Entities::obmUser;
require OBM::Entities::obmHost;
require OBM::Entities::obmGroup;
require OBM::Entities::obmMailshare;
require OBM::Entities::obmMailServer;
require OBM::Entities::obmSambaDomain;
require OBM::Update::utils;
use OBM::Update::commonGlobalIncremental qw(_updateState _doRemoteConf _runEngines _doUser _doGroup _doMailShare _doHost);
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;


sub new {
    my $self = shift;
    my( $dbHandler, $parameters ) = @_;

    # Définition des attributs de l'objet
    my %updateAttr = (
        user => undef,
        user_name => undef,
        domain => undef,
        delegation => undef,
        global => undef,
        dbHandler => undef,
        domainList => undef,
        engine => undef
    );


    if( !defined($dbHandler) || !defined($parameters) ) {
        croak( "Usage: PACKAGE->new(DBHANDLER, PARAMLIST)" );
    }elsif( !exists($parameters->{"user"}) && !exists($parameters->{"domain"}) && !exists($parameters->{"delegation" }) ) {
        croak( "Usage: PARAMLIST: table de hachage avec les cles 'user', 'domain' et 'delegation'" );
    }

    # Initialisation de l'objet
    $updateAttr{"global"} = $parameters->{"global"};
    $updateAttr{"dbHandler"} = $dbHandler;

    # Identifiant utilisateur
    if( defined($parameters->{"user"}) ) {
        $updateAttr{"user"} = $parameters->{"user"};

        my $query = "SELECT userobm_login FROM UserObm WHERE userobm_id=".$updateAttr{"user"};
        my $queryResult;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

	    ( $updateAttr{"user_name"} ) = $queryResult->fetchrow_array();
	    $queryResult->finish();
    }

    # Identifiant de délégation
    if( defined($parameters->{"delegation"}) ) {
        $updateAttr{"delegation"} = $parameters->{"delegation"};
    }

    # Identifiant de domaine
    if( defined($parameters->{"domain"}) ) {
        $updateAttr{"domain"} = $parameters->{"domain"};
    }else {
        croak( "Le parametre domaine doit etre precise" );
    }


    # Obtention des informations sur les domaines nécessaires
    $updateAttr{"domainList"} = &OBM::Update::utils::getDomains( $updateAttr{"dbHandler"}, $updateAttr{"domain"} );


    # Obtention des serveurs LDAP par domaines
    &OBM::Update::utils::getLdapServer( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );


    # Initialisation du moteur LDAP
    $updateAttr{"engine"}->{"ldapEngine"} = OBM::Ldap::ldapEngine->new( $updateAttr{"domainList"} );
    if( !$updateAttr{"engine"}->{"ldapEngine"}->init( 1 ) ) {
        delete( $updateAttr{"engine"}->{"ldapEngine"} );
    }

    # Paramétrage des serveurs IMAP par domaine
    &OBM::Update::utils::getCyrusServers( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );
    if( !&OBM::imapd::getAdminImapPasswd( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} ) ) {
        return undef;
    }

    # Paramétrage des serveurs SMTP-in par domaine
    &OBM::Update::utils::getSmtpInServers( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );

    # Initialisation du moteur Cyrus
    $updateAttr{"engine"}->{"cyrusEngine"} = OBM::Cyrus::cyrusEngine->new( $updateAttr{"domainList"} );
    if( !$updateAttr{"engine"}->{"cyrusEngine"}->init() ) {
        delete( $updateAttr{"engine"}->{"cyrusEngine"} );
    }

    # Initialisation du moteur Sieve
    $updateAttr{"engine"}->{"sieveEngine"} = OBM::Cyrus::sieveEngine->new( $updateAttr{"domainList"} );
    if( !$updateAttr{"engine"}->{"sieveEngine"}->init() ) {
        delete( $updateAttr{"engine"}->{"sieveEngine"} );
    }


    bless( \%updateAttr, $self );
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

    $return = $self->_doIncremental();

    if( $return ) {
        $return = $self->_doRemoteConf();
    }

    if( $return ) {
        $return = $self->_updateState();
    }

    return $return;
}


sub _doIncremental {
    my $self = shift;
    my $return = 1;
    my $domainDesc;


    if( defined($self->{"domain"}) ) {
        $domainDesc = &OBM::Update::utils::findDomainbyId( $self->{"domainList"}, $self->{"domain"} );

        if( !defined($domainDesc) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: domaine d'identifiant '".$self->{"domain"}."' inexistant", "W" );
            return 0;
        }
    }

    my %sqlFilter;
    if( defined($self->{"user"}) ) {
        # Si le paramètre utilisateur est indiqué, on fait une MAJ incrémentale par
        # utilisateur
        &OBM::toolBox::write_log( "[Update::updateIncremental]: MAJ incrementale pour l'utilisateur '".$self->{"user_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
        $sqlFilter{"updated"}->[0] = "updated_user_id=".$self->{"user"};
        $sqlFilter{"updated"}->[1] = "updatedlinks_user_id=".$self->{"user"};
        $sqlFilter{"deleted"} = "deleted_user_id=".$self->{"user"};

    }elsif( defined($self->{"delegation"}) ) {
        # Si le paramètre délégation est indiqué, on fait une MAJ incrémentale
        # par délégation
        &OBM::toolBox::write_log( "[Update::updateIncremental]: MAJ incrementale pour la delegation '".$self->{"delegation"}."'", "W" );
        $sqlFilter{"updated"}->[0] = "updated_delegation LIKE '".$self->{"delegation"}."%'";
        $sqlFilter{"updated"}->[1] = "updatedlinks_delegation LIKE '".$self->{"delegation"}."%'";
        $sqlFilter{"deleted"} = "deleted_delegation LIKE '".$self->{"delegation"}."%'";
    
    }elsif( defined($self->{"domain"}) ) {
        # Si le paramètre domaine est indiqué, on fait une MAJ incrémentale
        # par domaine
        &OBM::toolBox::write_log( "[Update::updateIncremental]: MAJ incrementale pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );
        $sqlFilter{"updated"}->[0] = "updated_domain_id='".$self->{"domain"}."'";
        $sqlFilter{"updated"}->[1] = "updatedlinks_domain_id='".$self->{"domain"}."'";
        $sqlFilter{"deleted"} = "deleted_domain_id=".$self->{"domain"};
        
    }else {
        return 0;
    }

    # On traite les suppressions
    $return = $return && $self->_incrementalDelete( $sqlFilter{"deleted"} );
    # On traite les mises à jour
    $return = $return && $self->_incrementalUpdate( $sqlFilter{"updated"} );


    return $return;
}


sub _incrementalUpdate {
    my $self = shift;
    my( $sqlFilter ) = @_;
    my $globalReturn = 1;


    my $dbHandler = $self->{"dbHandler"};
    if( !defined($dbHandler) ) {
        return 0;
    }


    # Obtention des entités mises à jour
    my $sqlQuery = "SELECT updated_id, updated_table, updated_entity_id, updated_type FROM Updated";
    if( defined($sqlFilter->[0]) ) {
        $sqlQuery .= " WHERE ".$sqlFilter->[0];
    }

    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return 0;
    }

    while( my( $updatedId, $updatedTable, $updatedEntityId ) = $queryResult->fetchrow_array() ) {
        if( !defined($updatedId) || !defined($updatedTable) || !defined($updatedEntityId) ) {
            next;
        }

        # Doit-on mettre à jour les liens de l'entité
        my $query = "SELECT COUNT(*) FROM Updatedlinks WHERE updatedlinks_table='".$updatedTable."'";
        if( defined($sqlFilter->[1]) ) {
            $query .= " AND ".$sqlFilter->[1];
        }

        my $queryResult2;
        if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
            return 0;
        }
        my( $numRows ) = $queryResult2->fetchrow_array();
        $queryResult2->finish();

        my $object;
        SWITCH: {
            if( lc($updatedTable) eq "userobm" ) {
                if( $numRows ) {
                    # Mise à jour de l'entité avec ses liaisons
                    $object = $self->_doUser( 1, 0, $updatedEntityId );
                }else {
                    # Mise à jour de l'entité sans ses liaisons
                    $object = $self->_doUser( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "ugroup" ) {
                if( $numRows ) {
                    # Mise à jour de l'entité avec ses liaisons
                    $object = $self->_doGroup( 1, 0, $updatedEntityId );
                }else {
                    # Mise à jour de l'entité sans ses liaisons
                    $object = $self->_doGroup( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "mailshare" ) {
                if( $numRows ) {
                    # Mise à jour de l'entité avec ses liaisons
                    $object = $self->_doMailShare( 1, 0, $updatedEntityId );
                }else {
                    # Mise à jour de l'entité sans ses liaisons
                    $object = $self->_doMailShare( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "host" ) {
                if( $numRows ) {
                    # Mise à jour de l'entité avec ses liaisons
                    $object = $self->_doHost( 1, 0, $updatedEntityId );
                }else {
                    # Mise à jour de l'entité sans ses liaisons
                    $object = $self->_doHost( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            next;
        }

        my $return = $self->_runEngines( $object );

        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $return = $object->updateDbEntity( $self->{"dbHandler"} );
            if( $object->isLinks() ) {
                $return = !$return || $object->updateDbEntityLinks( $self->{"dbHandler"} );
            }

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


    # Obtention des entités dont seul les liens ont été mis à jour
    $sqlQuery = "SELECT updatedlinks_id, updatedlinks_table, updatedlinks_entity_id FROM Updatedlinks";
    if( defined($sqlFilter->[1]) ) {
        $sqlQuery .= " WHERE ".$sqlFilter->[1];
    }

    if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
        return 0;
    }

    while( my( $updatedlinksId, $updatedlinksTable, $updatedlinksEntityId ) = $queryResult->fetchrow_array() ) {
        if( !defined($updatedlinksId) || !defined($updatedlinksTable) || !defined($updatedlinksEntityId) ) {
            next;
        }

        my $object;
        SWITCH: {
            if( lc($updatedlinksTable) eq "userobm" ) {
                $object = $self->_doUser( 1, 0, $updatedlinksEntityId );
                last SWITCH;
            }

            if( lc($updatedlinksTable) eq "ugroup" ) {
                $object = $self->_doGroup( 1, 0, $updatedlinksEntityId );
                last SWITCH;
            }

            if( lc($updatedlinksTable) eq "mailshare" ) {
                $object = $self->_doMailShare( 1, 0, $updatedlinksEntityId );
                last SWITCH;
            }

            if( lc($updatedlinksTable) eq "host" ) {
                $object = $self->_doHost( 1, 0, $updatedlinksEntityId );
                last SWITCH;
            }

            next;
        }

        my $return = $self->_runEngines( $object );

        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $return = $object->updateDbEntityLinks( $self->{"dbHandler"} );

            if( $return ) {
                # MAJ de la BD de travail ok, on nettoie les tables de MAJ
                # incrémentales
                $return = $self->_updateIncrementalTable( "Updatedlinks", $updatedlinksId );
                
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


    # Traitement des entités à supprimer
    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $sqlQuery = "SELECT deleted_id, deleted_table, deleted_entity_id FROM Deleted";
    if( defined($sqlFilter) ) {
        $sqlQuery .= " WHERE ".$sqlFilter;
    }

    if( !&OBM::dbUtils::execQuery( $sqlQuery, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: ".$queryResult->err, "W" );
        }

        return 0;
    }

    while( my( $deletedId, $deletedTable, $deletedEntityId ) = $queryResult->fetchrow_array() ) {
        if( !defined($deletedId) || !defined($deletedTable) || !defined($deletedEntityId) ) {
            next;
        }

        my $object;
        SWITCH: {
            if( lc($deletedTable) eq "userobm" ) {
                $object = $self->_doUser( 0, 1, $deletedEntityId );
                last SWITCH;
            }

            if( lc($deletedTable) eq "ugroup" ) {
                $object = $self->_doGroup( 0, 1, $deletedEntityId );
                last SWITCH;
            }

            if( lc($deletedTable) eq "mailshare" ) {
                $object = $self->_doMailShare( 0, 1, $deletedEntityId );
                last SWITCH;
            }

            if( lc($deletedTable) eq "host" ) {
                $object = $self->_doHost( 0, 1, $deletedEntityId );
                last SWITCH;
            }

            next;
        }

        my $return = $self->_runEngines( $object );

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


sub _deleteDbEntity {
    my $self = shift;
    my ( $table, $id ) = @_;

    if( !defined($table) ) {
        return 0;
    }

    if( !defined($id) || ($id !~ /^\d+$/) )  {
        return 0;
    }


    # Gestion des exceptions
    my $columnPrefix = $self->_tableNamePrefix( $table );


    # On supprime les informations de l'entité de la table de travail
    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $query = "DELETE FROM P_".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: ".$queryResult->err, "W" );
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
        &OBM::toolBox::write_log( "[Update::updateIncremental]: probleme lors de l'execution de la requete", "W" );
        if( defined($deleteQueryResult) ) {
            &OBM::toolBox::write_log( "[Update::updateIncremental]: ".$deleteQueryResult->err, "W" );
        }

        return 0;
    }

    return 1;
}
