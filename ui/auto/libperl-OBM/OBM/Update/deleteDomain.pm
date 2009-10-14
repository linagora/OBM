package OBM::Update::deleteDomain;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


use OBM::Tools::commonMethods qw(_log dump);
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $parameters ) = @_;

    my $self = bless { }, $class;

    if( !defined($parameters) ) {
        $self->_log( 'Usage: PACKAGE->new(PARAMLIST)', 4 );
        return undef;
    }

    # Domain identifier
    if( defined($parameters->{'domain-id'}) ) {
        $self->{'domainId'} = $parameters->{'domain-id'};
    }else {
        $self->_log( 'Le parametre domain-id doit etre precise', 0 );
        return undef;
    }


    $self->{'delete'} = $parameters->{'delete'};

    return $self;
}


sub update {
    my $self = shift;

    require OBM::Postfix::smtpInEngine;
    $self->_log( 'initialisation du SMTP-in maps updater', 2 );
    if( !($self->{'smtpInEngine'} = OBM::Postfix::smtpInEngine->new()) ) {
        $self->_log( 'echec de l\'initialisation du SMTP-in maps updater', 0 );
        return 1;
    }

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 2 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'PROGRAMMABLE', $self->{'domainId'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    require OBM::Ldap::ldapDeleteEngine;
    $self->_log( 'initialisation du moteur LDAP', 2 );
    $self->{'ldapEngine'} = OBM::Ldap::ldapDeleteEngine->new();
    if( !defined($self->{'ldapEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur LDAP', 1 );
        return 1;
    }elsif( !ref($self->{'ldapEngine'}) ) {
        $self->_log( 'moteur LDAP non démarré', 3 );
        $self->{'ldapEngine'} = undef;
    }

    require OBM::Cyrus::cyrusDeleteEngine;
    $self->_log( 'initialisation du moteur Cyrus', 2 );
    $self->{'cyrusEngine'} = OBM::Cyrus::cyrusDeleteEngine->new();
    if( !defined($self->{'cyrusEngine'}) ) {
        $self->_log( 'erreur à l\'initialisation du moteur Cyrus', 1 );
        return 1;
    }elsif( !ref($self->{'cyrusEngine'}) ) {
        $self->_log( 'moteur Cyrus non démarré', 3 );
        $self->{'cyrusEngine'} = undef;
    }

    my $error = 0;
    while( my $entity = $self->{'entitiesFactory'}->next() ) {
        $self->_log( 'suppression de '.$entity->getDescription(), 1 );

        if( !$error && defined($self->{'ldapEngine'}) ) {
            if($self->{'ldapEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement LDAP de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error && defined($self->{'cyrusEngine'}) ) {
            if($self->{'cyrusEngine'}->update($entity)) {
                $self->_log( 'problème lors du traitement Cyrus de l\'entité '.$entity->getDescription(), 1 );
                $error = 1;
            }
        }

        if( !$error && defined($self->{'smtpInEngine'}) ) {
            if( $self->{'smtpInEngine'}->updateByDomainId( [$entity->getId()] ) || $self->{'smtpInEngine'}->updateMaps() ) {
                $error = 1;
            }
        }

        if( !$error && $self->_purgeDbProdDatas($entity) ) {
            $self->_log( 'problème lors du nettoyage des données de production de la BD OBM', 0 );
            $error = 1;
        }
    }
}


sub _purgeDbProdDatas {
    my $self = shift;
    my( $entity ) = @_;
    my $errors = 0;

    my $domainId = $entity->getId();

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }


    # Purge P_Service table
    my $query = 'DELETE FROM P_Service
                    WHERE service_entity_id IN (
                        SELECT hostentity_entity_id
                        FROM P_HostEntity
                        WHERE hostentity_host_id IN (
                            SELECT '.$dbHandler->castAsInteger('serviceproperty_value').'
                            FROM P_ServiceProperty
                            INNER JOIN P_DomainEntity
                                ON domainentity_entity_id=serviceproperty_entity_id
                                WHERE domainentity_domain_id='.$domainId.'
                                AND serviceproperty_property IN (\'imap\', \'smtp_in\')
                        )
                    )';

    my $sth;
    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }

    $query = 'DELETE FROM P_Service
                WHERE service_entity_id IN (
                    SELECT hostentity_entity_id
                    FROM P_HostEntity
                    INNER JOIN P_Host
                        ON hostentity_host_id=host_id
                        WHERE host_domain_id='.$domainId.')';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }

    $query = 'DELETE FROM P_Service
                WHERE service_entity_id IN (
                    SELECT domainentity_entity_id
                    FROM P_DomainEntity
                    WHERE domainentity_domain_id='.$domainId.')';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_ServiceProperty table
    $query = 'DELETE FROM P_ServiceProperty
                WHERE serviceproperty_entity_id IN (
                    SELECT domainentity_entity_id
                    FROM P_DomainEntity
                    WHERE domainentity_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_EntityRight table
    $query = 'DELETE FROM P_EntityRight
                WHERE entityright_entity_id IN (
                    SELECT mailboxentity_entity_id
                    FROM P_MailboxEntity
                    INNER JOIN P_UserObm
                        ON mailboxentity_mailbox_id=userobm_id
                        WHERE userobm_domain_id='.$domainId.'
                    UNION
                    SELECT mailshareentity_entity_id
                    FROM P_MailshareEntity
                    INNER JOIN P_MailShare
                        ON mailshareentity_mailshare_id=mailshare_id
                        WHERE mailshare_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_of_usergroup table
    $query = 'DELETE FROM P_of_usergroup
                WHERE of_usergroup_group_id IN (
                    SELECT group_id
                    FROM P_UGroup
                    WHERE group_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }

    # Purge P_GroupEntity table
    $query = 'DELETE FROM P_GroupEntity
                WHERE groupentity_group_id IN (
                    SELECT group_id
                    FROM P_UGroup
                    WHERE group_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_UGroup table
    $query = 'DELETE FROM P_UGroup
                WHERE group_domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }

    # Purge P_HostEntity table
    $query = 'DELETE FROM P_HostEntity
                WHERE hostentity_host_id IN (
                    SELECT host_id
                    FROM P_Host
                    WHERE host_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_Host table
    $query = 'DELETE FROM P_Host
                WHERE host_domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_MailshareEntity table
    $query = 'DELETE FROM P_MailshareEntity
                WHERE mailshareentity_mailshare_id IN (
                    SELECT mailshare_id
                    FROM P_MailShare
                    WHERE mailshare_domain_id='.$domainId.')
                    ';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_MailShare table
    $query = 'DELETE FROM P_MailShare
                WHERE mailshare_domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_UserEntity table
    $query = 'DELETE FROM P_UserEntity
                WHERE userentity_user_id IN (
                    SELECT userobm_id
                    FROM P_UserObm
                    WHERE userobm_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_MailboxEntity table
    $query = 'DELETE FROM P_MailboxEntity
                WHERE mailboxentity_mailbox_id IN (
                    SELECT userobm_id
                    FROM P_UserObm
                    WHERE userobm_domain_id='.$domainId.'
                    )';

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_UserObm table
    $query = 'DELETE FROM P_UserObm
                WHERE userobm_domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_DomainEntity table
    $query = 'DELETE FROM P_DomainEntity
                WHERE domainentity_domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }


    # Purge P_Domain table
    $query = 'DELETE FROM P_Domain
                WHERE domain_id='.$domainId;

    if( !defined($dbHandler->execQuery( $query, \$sth )) ) {
        $errors++;
    }

    return 0;
}
