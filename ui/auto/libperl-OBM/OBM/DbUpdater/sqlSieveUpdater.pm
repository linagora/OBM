package OBM::DbUpdater::sqlSieveUpdater;

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


    my $query = 'UPDATE P_UserObm
                    SET     userobm_nomade_perms = (SELECT userobm_nomade_perms FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_enable = (SELECT userobm_nomade_enable FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_local_copy = (SELECT userobm_nomade_local_copy FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_datebegin = (SELECT userobm_nomade_datebegin FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_nomade_dateend = (SELECT userobm_nomade_dateend FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_email_nomade = (SELECT userobm_email_nomade FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_enable = (SELECT userobm_vacation_enable FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_datebegin = (SELECT userobm_vacation_datebegin FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_dateend = (SELECT userobm_vacation_dateend FROM UserObm WHERE userobm_id='.$entity->getId().'),
                            userobm_vacation_message = (SELECT userobm_vacation_message FROM UserObm WHERE userobm_id='.$entity->getId().')
                    WHERE userobm_id='.$entity->getId();

    my $sth;
    if( !defined($self->{'dbHandler'}->execQuery( $query, \$sth )) ) {
        $self->_log( 'échec de mise à jour des informations Sieve en BD', 0 );
        return 1;
    }

    $self->_log( 'mise à jour de la BD terminée avec succès', 2 );

    return 0;
}
