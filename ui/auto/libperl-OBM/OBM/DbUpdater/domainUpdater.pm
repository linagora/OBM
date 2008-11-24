package OBM::DbUpdater::domainUpdater;

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

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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

    if( $self->delete($entity) ) {
        $self->_log( 'problème à la mise à jour BD du domaine '.$entity->getDescription(), 2 );
        return 1;
    }


    my $query = 'INSERT INTO P_Domain
                (   domain_id,
                    domain_global,
                    domain_label,
                    domain_description,
                    domain_name,
                    domain_alias
                ) SELECT    domain_id,
                            domain_global,
                            domain_label,
                            domain_description,
                            domain_name,
                            domain_alias
                  FROM Domain
                  WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }


    $query = 'INSERT INTO P_Samba
                (   samba_domain_id,
                    samba_name,
                    samba_value
                ) SELECT    samba_domain_id,
                            samba_name,
                            samba_value
                  FROM Samba
                  WHERE samba_domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    return 0;
}


sub delete {
    my $self = shift;
    my( $entity ) = @_;

    if( ref($entity) ne 'OBM::Entities::obmDomain' ) {
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


    my $query = 'DELETE FROM P_Domain WHERE domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }


    $query = 'DELETE FROM P_Samba WHERE samba_domain_id='.$entity->getId();
    if( !defined( $dbHandler->execQuery( $query, \$sth ) ) ) {
        $self->_log( 'problème à la mise à jour BD du domaine d\'identifiant '.$entity->getId(), 2 );
        return 1;
    }

    return 0;
}
