package OBM::Update::update;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


require OBM::toolBox;
require OBM::ldap;
require OBM::imapd;
require OBM::Update::utils;
require OBM::Ldap::ldapEngine;
require OBM::Cyrus::cyrusEngine;
require OBM::Cyrus::sieveEngine;
require OBM::Postfix::smtpInRemoteEngine;
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
use OBM::Parameters::common;
use OBM::Parameters::ldapConf;


sub new {
    my $self = shift;
    my( $dbHandler, $parameters ) = @_;

    # Definition des attributs de l'objet
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
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
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
    if( defined($updateAttr{"domain"}) ) {
        $updateAttr{"domainList"} = &OBM::Update::utils::getDomains( $updateAttr{"dbHandler"}, $updateAttr{"domain"} );
    }else {
        $updateAttr{"domainList"} = &OBM::Update::utils::getDomains( $updateAttr{"dbHandler"}, undef );
    }

    # initialisation des moteurs

    # Obtention des serveurs LDAP par domaines
    &OBM::Update::utils::getLdapServer( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );

    $updateAttr{"engine"}->{"ldapEngine"} = OBM::Ldap::ldapEngine->new( $updateAttr{"domainList"} );
    if( !$updateAttr{"engine"}->{"ldapEngine"}->init() ) {
        delete( $updateAttr{"engine"}->{"ldapEngine"} );
    }

    # Parametrage des serveurs IMAP par domaine
    &OBM::Update::utils::getCyrusServers( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );
    if( !&OBM::imapd::getAdminImapPasswd( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} ) ) {
        return undef;
    }

    # Parametrage des serveurs SMTP-in par domaine
    &OBM::Update::utils::getSmtpInServers( $updateAttr{"dbHandler"}, $updateAttr{"domainList"} );

    $updateAttr{"engine"}->{"cyrusEngine"} = OBM::Cyrus::cyrusEngine->new( $updateAttr{"domainList"} );
    if( !$updateAttr{"engine"}->{"cyrusEngine"}->init() ) {
        delete( $updateAttr{"engine"}->{"cyrusEngine"} );
    }

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

    if( $self->{"global"} ) {
        # On traite les suppressions
        $return = $return && $self->_doGlobalDelete();
        # On traite les mises à jour
        $return = $self->_doGlobalUpdate();
    }else {
        $return = $self->_doIncremental();
    }

    if( $return ) {
        $return = $self->_doRemoteConf();
    }

    if( $return ) {
        $return = $self->_updateState();
    }

    return $return;
}


sub _doGlobalUpdate {
    my $self = shift;
    my $queryResult;
    my $globalReturn = 1;

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Update::update]: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainDesc = $self->_findDomainbyId( $self->{"domain"} );

    if( !defined($domainDesc) ) {
        &OBM::toolBox::write_log( "[Update::update]: domaine d'identifiant '".$self->{"domain"}."' inexistant", "W" );
        return 0;
    }


    &OBM::toolBox::write_log( "[Update::update]: MAJ totale pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );

    # MAJ des informations de domaine
    $globalReturn = $self->_updateDbDomain();

    # Uniquement pour le metadomaine
    if( $self->{"domain"} == 0 ) {
        # Traitement des entités de type 'utilisateur système'
        my $query = "SELECT usersystem_id FROM UserSystem";
        if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
            return 0;
        }

        while( my( $systemUserId ) = $queryResult->fetchrow_array() ) {
            my $object = $self->_doSystemUser( 1, 0, $systemUserId );

            my $return = $self->_runEngines( $object );
            if( $return ) {
                # La MAJ de l'entité c'est bien passée, on met a jour la BD de
                # travail
                $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
            }
        }
    }

    # Pour tous les domaines, sauf le metadomaine
    if( $self->{"domain"} != 0 ) {
        # Mise a jour des partitions Cyrus
        my $updateMailSrv = OBM::Cyrus::cyrusRemoteEngine->new( $self->{"domainList"} );
        if( $updateMailSrv->init() ) {
            $globalReturn = $globalReturn && $updateMailSrv->update( "add" );
        }
        $updateMailSrv->destroy();

        # Si tout c'est bien passé, il faut rétablir les connexions à Cyrus
        if( $globalReturn  && defined($self->{"engine"}->{"cyrusEngine"}) ) {
            if( !$self->{"engine"}->{"cyrusEngine"}->init() ) {
                delete( $self->{"engine"}->{"cyrusEngine"} );
            }
        }
    }

    # Si on a déjà rencontré une erreur, on s'arrête
    if( !$globalReturn ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de la mise a jour des partitions Cyrus du domaine '".$self->{"domain"}."' - Operation annulee !", "W" );
        return $globalReturn;
    }


    # Pour tous les domaines
    # Traitement des entités de type 'obmMailServer'
    my $object = $self->_doMailServer( 1, 0 );
    my $return = $self->_runEngines( $object );

    if( $return ) {
        # La MAJ de l'entité c'est bien passée, on met a jour la BD de
        # travail
        $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
    }


    # Traitement des entités de type 'hote'
    my $query = "SELECT host_id FROM Host WHERE host_domain_id=".$self->{"domain"};
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my $hostId = $queryResult->fetchrow_array() ) {
        my $object = $self->_doHost( 1, 0, $hostId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
        }
    }


    # Traitement des entités de type 'utilisateur'
    $query = "SELECT userobm_id FROM UserObm WHERE userobm_domain_id=".$self->{"domain"};
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $userId ) = $queryResult->fetchrow_array() ) {
        $object = $self->_doUser( 1, 0, $userId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
        }
    }


    # Traitement des entités de type 'groupe'
    $query = "SELECT group_id FROM UGroup WHERE group_privacy=0 AND group_domain_id=".$self->{"domain"};
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $groupId ) = $queryResult->fetchrow_array() ) {
        $object = $self->_doGroup( 1, 0, $groupId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
        }
    }


    # Traitement des entités de type 'mailshare'
    $query = "SELECT mailshare_id FROM MailShare WHERE mailshare_domain_id=".$self->{"domain"};
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $mailshareId ) = $queryResult->fetchrow_array() ) {
        $object = $self->_doMailShare( 1, 0, $mailshareId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $object->updateDbEntity( $self->{"dbHandler"} );
        }
    }

    return $globalReturn; 
}


sub _doGlobalDelete {
    my $self = shift;
    my $queryResult;
    my $globalReturn = 1;

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Update::update]: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainDesc = $self->_findDomainbyId( $self->{"domain"} );

    if( !defined($domainDesc) ) {
        &OBM::toolBox::write_log( "[Update::update]: domaine d'identifiant '".$self->{"domain"}."' inexistant", "W" );
        return 0;
    }


    &OBM::toolBox::write_log( "[Update::update]: detection des suppressions en BD pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );


    # Traitement des entités de type 'hote'
    my $query = "SELECT host_id FROM P_Host WHERE host_domain_id=".$self->{"domain"}." AND host_id NOT IN (SELECT host_id FROM Host WHERE host_domain_id=".$self->{"domain"}.")";
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my $hostId = $queryResult->fetchrow_array() ) {
        my $object = $self->_doHost( 1, 1, $hostId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $self->_deleteDbEntity( "Host", $hostId );
        }
    }


    # Traitement des entités de type 'utilisateur'
    $query = "SELECT userobm_id FROM P_UserObm WHERE userobm_domain_id=".$self->{"domain"}." AND userobm_id NOT IN (SELECT userobm_id FROM UserObm WHERE userobm_domain_id=".$self->{"domain"}.")";
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $userId ) = $queryResult->fetchrow_array() ) {
        my $object = $self->_doUser( 1, 1, $userId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $self->_deleteDbEntity( "UserObm", $userId );
        }
    }


    # Traitement des entités de type 'groupe'
    $query = "SELECT group_id FROM P_UGroup WHERE group_domain_id=".$self->{"domain"}." AND group_privacy=0 AND group_id NOT IN (SELECT group_id FROM UGroup WHERE group_domain_id=".$self->{"domain"}." AND group_privacy=0)";
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $groupId ) = $queryResult->fetchrow_array() ) {
        my $object = $self->_doGroup( 1, 1, $groupId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $self->_deleteDbEntity( "UGroup", $groupId );
        }
    }


    # Traitement des entités de type 'mailshare'
    $query = "SELECT mailshare_id FROM P_MailShare WHERE mailshare_domain_id=".$self->{"domain"}." AND mailshare_id NOT IN (SELECT mailshare_id FROM MailShare WHERE mailshare_domain_id=".$self->{"domain"}.")";
    if( !&OBM::dbUtils::execQuery( $query, $self->{"dbHandler"}, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution d'une requete SQL : ".$self->{"dbHandler"}->err, "W" );
        return 0;
    }

    while( my( $mailshareId ) = $queryResult->fetchrow_array() ) {
        my $object = $self->_doMailShare( 1, 1, $mailshareId );

        my $return = $self->_runEngines( $object );
        if( $return ) {
            # La MAJ de l'entité c'est bien passée, on met a jour la BD de
            # travail
            $globalReturn = $globalReturn && $self->_deleteDbEntity( "MailShare", $mailshareId );
        }
    }


    return $globalReturn;
}


sub _doIncremental {
    my $self = shift;
    my $return = 1;
    my $domainDesc;


    if( defined($self->{"domain"}) ) {
        $domainDesc = $self->_findDomainbyId( $self->{"domain"} );

        if( !defined($domainDesc) ) {
            &OBM::toolBox::write_log( "[Update::update]: domaine d'identifiant '".$self->{"domain"}."' inexistant", "W" );
            return 0;
        }
    }

    my %sqlFilter;
    if( defined($self->{"user"}) ) {
        # Si le paramètre utilisateur est indiqué, on fait une MAJ incrémentale par
        # utilisateur
        &OBM::toolBox::write_log( "[Update::update]: MAJ incrementale pour l'utilisateur '".$self->{"user_name"}."', domaine '".$domainDesc->{"domain_label"}."'", "W" );
        $sqlFilter{"updated"}->[0] = "updated_user_id=".$self->{"user"};
        $sqlFilter{"updated"}->[1] = "updatedlinks_user_id=".$self->{"user"};
        $sqlFilter{"deleted"} = "deleted_user_id=".$self->{"user"};

    }elsif( defined($self->{"delegation"}) ) {
        # Si le paramètre délégation est indiqué, on fait une MAJ incrémentale
        # par délégation
        &OBM::toolBox::write_log( "[Update::update]: MAJ incrementale pour la delegation '".$self->{"delegation"}."'", "W" );
        $sqlFilter{"updated"}->[0] = "updated_delegation LIKE '".$self->{"delegation"}."%'";
        $sqlFilter{"updated"}->[1] = "updatedlinks_delegation LIKE '".$self->{"delegation"}."%'";
        $sqlFilter{"deleted"} = "deleted_delegation LIKE '".$self->{"delegation"}."%'";
    
    }elsif( defined($self->{"domain"}) ) {
        # Si le paramètre domaine est indiqué, on fait une MAJ incrémentale
        # par domaine
        &OBM::toolBox::write_log( "[Update::update]: MAJ incrementale pour le domaine '".$domainDesc->{"domain_label"}."'", "W" );
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
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
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
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
            return 0;
        }
        my( $numRows ) = $queryResult2->fetchrow_array();
        $queryResult2->finish();

        my $object;
        SWITCH: {
            if( lc($updatedTable) eq "userobm" ) {
                if( $numRows ) {
                    $object = $self->_doUser( 1, 0, $updatedEntityId );
                }else {
                    $object = $self->_doUser( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "ugroup" ) {
                if( $numRows ) {
                    $object = $self->_doGroup( 1, 0, $updatedEntityId );
                }else {
                    $object = $self->_doGroup( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "mailshare" ) {
                if( $numRows ) {
                    $object = $self->_doMailShare( 1, 0, $updatedEntityId );
                }else {
                    $object = $self->_doMailShare( 0, 0, $updatedEntityId );
                }
                last SWITCH;
            }

            if( lc($updatedTable) eq "host" ) {
                if( $numRows ) {
                    $object = $self->_doHost( 1, 0, $updatedEntityId );
                }else {
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
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$queryResult->err, "W" );
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
            $return = $object->updateDbEntity( $self->{"dbHandler"} );

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
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::update]: ".$queryResult->err, "W" );
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
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::update]: ".$queryResult->err, "W" );
        }

        return 0;
    }

    while( my $hashResult = $queryResult->fetchrow_hashref() ) {
        my $updateQuery = "UPDATE P_".$table." SET ";

        my $first = 1;
        while( my( $key, $value ) = each(%{$hashResult}) ) {
            if( !$first ) {
                $updateQuery .= ", ";
            }else {
                $first = 0;
            }

            $updateQuery .= $key."=".$dbHandler->quote($value);
        }

        $updateQuery .= " WHERE ".$columnPrefix."_id=".$id;

        # On exécute la requête
        my $updateQueryResult;
        if( !&OBM::dbUtils::execQuery( $updateQuery, $dbHandler, \$updateQueryResult ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete", "W" );
            if( defined($updateQueryResult) ) {
                &OBM::toolBox::write_log( "[Update::update]: ".$updateQueryResult->err, "W" );
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


    # Gestion des exceptions
    my $columnPrefix = $self->_tableNamePrefix( $table );


    # On supprime les informations de l'entité de la table de travail
    my $dbHandler = $self->{"dbHandler"};
    my $queryResult;
    my $query = "DELETE FROM P_".$table." WHERE ".$columnPrefix."_id=".$id;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete", "W" );
        if( defined($queryResult) ) {
            &OBM::toolBox::write_log( "[Update::update]: ".$queryResult->err, "W" );
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
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete", "W" );
        if( defined($deleteQueryResult) ) {
            &OBM::toolBox::write_log( "[Update::update]: ".$deleteQueryResult->err, "W" );
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
    my( $links, $delete, $systemUserId ) = @_;
    my $return = 1;

    if( !defined($systemUserId) || $systemUserId !~ /^\d+$/ ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    my $systemUserObject = OBM::Entities::obmSystemUser->new( $links, $delete, $systemUserId );
    $return = $systemUserObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }


    return $systemUserObject;
}


sub _doUser {
    my $self = shift;
    my( $links, $delete, $userId ) = @_;
    my $return = 1;

    if( !defined($userId) || ($userId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $userObject = OBM::Entities::obmUser->new( $links, $delete, $userId );
    $return = $userObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $userObject;
}


sub _doGroup {
    my $self = shift;
    my( $links, $delete, $groupId ) = @_;
    my $return = 1;

    if( !defined($groupId) || ($groupId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $groupObject = OBM::Entities::obmGroup->new( $links, $delete, $groupId );
    $return = $groupObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $groupObject;
}


sub _doMailShare {
    my $self =shift;
    my( $links, $delete, $mailshareId ) = @_;
    my $return = 1;

    if( !defined($mailshareId) || ($mailshareId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $mailShareObject = OBM::Entities::obmMailshare->new( $links, $delete, $mailshareId );
    $return = $mailShareObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $mailShareObject;
}


sub _doHost {
    my $self = shift;
    my( $links, $delete, $hostId ) = @_;
    my $return = 1;

    if( !defined($hostId) || ($hostId !~ /^\d+$/) ) {
        return 0;
    }

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $hostObject = OBM::Entities::obmHost->new( $links, $delete, $hostId );
    $return = $hostObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $hostObject;
}


sub _doMailServer {
    my $self = shift;
    my( $links, $delete ) = 0;
    my $return = 1;

    if( !defined($links) ) {
        $links = 0;
    }

    if( !defined($delete) ) {
        $delete = 0;
    }

    my $mailServerObject = OBM::Entities::obmMailServer->new( $links, $delete );
    $mailServerObject->getEntity( $self->{"dbHandler"}, $self->_findDomainbyId( $self->{"domain"} ) );
    if( !$return ) {
        return undef;
    }

    return $mailServerObject;
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
        if( defined( $engine ) ) {
            $return = $engine->update( $object );
        }
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


sub _updateDbDomain {
    my $self = shift;

    if( !defined($self->{"dbHandler"}) ) {
        return 0;
    }
    my $dbHandler = $self->{"dbHandler"};

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Update::update]: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainId = $self->{"domain"};

    # MAJ des informations de domaine
    my $query = "SELECT * FROM Domain WHERE domain_id=".$domainId;

    # On execute la requete
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return 0;
    }

    while( my $hashResult = $queryResult->fetchrow_hashref() ) {
        if( $hashResult->{"domain_id"} != $domainId ) {
            next;
        }
        
        $query = "DELETE FROM P_Domain WHERE Domain_id=".$domainId;
        my $queryResult2;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

        $query = "INSERT INTO P_Domain SET ";
        my $first = 1;
        while( my( $column, $value ) = each(%{$hashResult}) ) {
            if( !$first ) {
                $query .= ", ";
            }else {
                $first = 0;
            }

            $query .= $column."=".$dbHandler->quote($value);
        }

        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

    }


    # MAJ des informations de serveur mail du domaine
    $query = "SELECT i.mailserver_id, i.mailserver_host_id FROM MailServer i, Host j WHERE i.mailserver_host_id=j.host_id AND (j.host_domain_id=".$domainId." OR j.host_domain_id=0)";
    # On execute la requete
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return 0;
    }

    while( my( $mailServerId, $hostId ) = $queryResult->fetchrow_array() ) {
        my $queryResult2;

        # Les hôtes serveurs de mails
        $query = "DELETE FROM P_MailServer WHERE mailserver_id=".$mailServerId;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

        $query = "SELECT * FROM MailServer WHERE mailserver_id=".$mailServerId;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

        while( my $hashResult = $queryResult2->fetchrow_hashref() ) {
            $query = "INSERT INTO P_MailServer SET ";

            my $queryResult3;
            my $first = 1;
            while( my( $column, $value ) = each(%{$hashResult}) ) {
                if( !$first ) {
                    $query .= ", ";
                }else {
                    $first = 0;
                }

                $query .= $column."=".$dbHandler->quote($value);
            }

            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult3 ) ) {
                &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
                return 0;
            }
        }

        # Les informations associées aux hôtes serveurs de mails
        $query = "DELETE FROM P_MailServerNetwork WHERE mailservernetwork_host_id=".$hostId;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

        $query = "SELECT * FROM MailServerNetwork WHERE mailservernetwork_host_id=".$hostId;
        if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult2 ) ) {
            &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
            return 0;
        }

        while( my $hashResult = $queryResult2->fetchrow_hashref() ) {
            $query = "INSERT INTO P_MailServerNetwork SET ";

            my $queryResult3;
            my $first = 1;
            while( my( $column, $value ) = each(%{$hashResult}) ) {
                if( !$first ) {
                    $query .= ", ";
                }else {
                    $first = 0;
                }

                $query .= $column."=".$dbHandler->quote($value);
            }

            if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult3 ) ) {
                &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
                return 0;
            }
        }
    }


    return 1;
}


sub _updateState {
    my $self = shift;

    if( !defined($self->{"dbHandler"}) ) {
        return 0;
    }
    my $dbHandler = $self->{"dbHandler"};

    if( !defined($self->{"domain"}) || ($self->{"domain"} !~ /^\d+$/) ) {
        &OBM::toolBox::write_log( "[Update::update]: pas de domaine indique pour la MAJ totale", "W" );
        return 0;
    }
    my $domainId = $self->{"domain"};
   
    my $query = "UPDATE DomainPropertyValue SET domainpropertyvalue_value=0 WHERE domainpropertyvalue_property_key='update_state' AND domainpropertyvalue_domain_id=".$domainId;
    my $queryResult;
    if( !&OBM::dbUtils::execQuery( $query, $dbHandler, \$queryResult ) ) {
        &OBM::toolBox::write_log( "[Update::update]: probleme lors de l'execution de la requete : ".$dbHandler->err, "W" );
        return 0;
    }

    return 1;
}


sub _doRemoteConf {
    my $self = shift;
    my $return = 1;

    # MAJ des map Postfix sur les serveurs entrant
    my $updateMailSrv = OBM::Postfix::smtpInRemoteEngine->new( $self->{"domainList"} );
    if( $updateMailSrv->init() ) {
        $return = $updateMailSrv->update();
    }

    $updateMailSrv->destroy();

    return $return;
}
