package OBM::DbUpdater::mailshareUpdater;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(_log dump);


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub update {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmMailshare' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }

    if( $self->_delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du groupe '.$entity->getDescription(), 2 );
        return 1;
    }


    my $query;
    if( !$entity->getDelete() && $entity->getUpdateEntity() ) {
        $query = 'INSERT INTO P_MailShare
                    (   mailshare_id,
                        mailshare_domain_id,
                        mailshare_timecreate,
                        mailshare_userupdate,
                        mailshare_usercreate,
                        mailshare_name,
                        mailshare_archive,
                        mailshare_quota,
                        mailshare_mail_server_id,
                        mailshare_delegation,
                        mailshare_description,
                        mailshare_email
                    ) SELECT    mailshare_id,
                                mailshare_domain_id,
                                mailshare_timecreate,
                                mailshare_userupdate,
                                mailshare_usercreate,
                                mailshare_name,
                                mailshare_archive,
                                mailshare_quota,
                                mailshare_mail_server_id,
                                mailshare_delegation,
                                mailshare_description,
                                mailshare_email
                      FROM MailShare
                      WHERE mailshare_id='.$entity->getId();
    
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour '.$entity->getDescription(), 2 );
            return 1;
        }


        $query = 'INSERT INTO P_MailshareEntity
                    (   mailshareentity_entity_id,
                        mailshareentity_mailshare_id
                    ) SELECT    mailshareentity_entity_id,
                                mailshareentity_mailshare_id
                      FROM MailshareEntity
                      WHERE mailshareentity_mailshare_id = '.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour des liens '.$entity->getDescription() );
            return 1;
        }
    }

    if( !$entity->getDelete() && $entity->getUpdateLinks() ) {
        $query = 'INSERT INTO P_EntityRight
                    (   entityright_id,
                        entityright_entity_id,
                        entityright_consumer_id,
                        entityright_read,
                        entityright_write,
                        entityright_admin,
                        entityright_access
                    ) SELECT    entityright_id,
                                entityright_entity_id,
                                entityright_consumer_id,
                                entityright_read,
                                entityright_write,
                                entityright_admin,
                                entityright_access
                      FROM EntityRight
                      WHERE entityright_entity_id=(SELECT mailshareentity_entity_id
                                                    FROM MailshareEntity
                                                    WHERE mailshareentity_mailshare_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour des liens '.$entity->getDescription() );
            return 1;
        }
    }


    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( !$entity->getDelete() ) {
        $self->_log( 'l\'entité '.$entity->getDescription().' n\'est pas à supprimer. Suppression annulée', 3 );
        return 0;
    }

    return $self->_delete( $entity );
}


sub _delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmMailshare' ) {
        $self->_log( 'entité incorrecte, traitement impossible', 3 );
        return 1;
    }

    require OBM::Tools::obmDbHandler;
    my $dbHandler;
    my $sth;
    if( !($dbHandler = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 3 );
        return 1;
    }


    my $query;
    if( $entity->getDelete() || $entity->getUpdateLinks() ) {
        $query = 'DELETE FROM P_EntityRight
                    WHERE entityright_entity_id=(SELECT mailshareentity_entity_id
                                                        FROM P_MailshareEntity
                                                        WHERE mailshareentity_mailshare_id = '.$entity->getId().')';
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD de liens '.$entity->getDescription(), 2 );
            return 1;
        }
    }


    if( $entity->getDelete() || $entity->getUpdateEntity() ) {
        $query = 'DELETE FROM P_MailshareEntity WHERE mailshareentity_mailshare_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }

        $query = 'DELETE FROM P_MailShare WHERE mailshare_id='.$entity->getId();
        if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
            $self->_log( 'problème à la mise à jour BD '.$entity->getDescription(), 2 );
            return 1;
        }
    }

    return 0;
}
