package OBM::DbUpdater::sqlQuotaUsedUpdater;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );


sub new {
    my $class = shift;

    my $self = bless { }, $class;

    require OBM::Tools::obmDbHandler;
    if( !($self->{'dbHandler'} = OBM::Tools::obmDbHandler->instance()) ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return undef;
    }

    return $self;
}


sub update {
    my $self = shift;
    my( $entity ) = @_;


    if( !defined($entity) ) {
        $self->_log( 'entité non définie', 3 );
        return 1;
    }elsif( !ref($entity) ) {
        $self->_log( 'entité incorrecte', 3 );
        return 1;
    }elsif( ref($entity) ne 'OBM::Entities::obmUser' ) {
        $self->_log( 'type d\'entité \''.ref($entity).' non supporté', 0 );
        return 1;
    }


    SWITCH: {
        if( ref($entity) eq 'OBM::Entities::obmUser' ) {
            $self->_updateUserQuotaUsed( $entity );
            last SWITCH;
        }

        $self->_log( 'type d\'entité \''.ref($entity).' non supporté', 0 );
        return 1;
    }

    $self->_log( 'mise à jour de la BD terminée avec succès', 2 );

    return 0;
}


sub _updateUserQuotaUsed {
    my $self = shift;
    my( $entity ) = @_;

    my $query = 'UPDATE UserObm
                    SET userobm_mail_quota_use = '.$entity->getCyrusQuotaUsed().'
                    WHERE userobm_id='.$entity->getId();

    my $sth;
    if( !defined($self->{'dbHandler'}->execQuery( $query, \$sth )) ) {
        $self->_log( 'échec de mise à jour des informations Sieve en BD', 0 );
        return 1;
    }

    $query = 'UPDATE P_UserObm
                SET userobm_mail_quota_use = '.$entity->getCyrusQuotaUsed().'
                WHERE userobm_id='.$entity->getId();

    if( !defined($self->{'dbHandler'}->execQuery( $query, \$sth )) ) {
        $self->_log( 'échec de mise à jour des informations Sieve en BD', 0 );
        return 1;
    }

    $self->_log( 'mise à jour de la BD terminée avec succès', 2 );

    return 0;
}
