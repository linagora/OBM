package OBM::EntitiesFactory::obmBackupFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
use OBM::Log::log;
@ISA = ('OBM::EntitiesFactory::factory', 'OBM::Log::log');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $updateType, $parentDomain ) = @_;

    my $self = bless { }, $class;

    $self->{'updateType'} = $updateType;
    if( !$self->_checkUpdateType() ) {
        return undef;
    }

    if( !defined($parentDomain) ) {
        $self->_log( 'description du domaine père indéfini', 1 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 1 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;
    
    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 1 );
        return undef;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'entitiesDescList'} = undef;


    return $self;
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 4 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    while( defined($self->{'entitiesDescList'}) && (my $obmBackupDesc = $self->{'entitiesDescList'}->fetchrow_hashref()) ) {
        require OBM::Entities::obmObmBackup;
        if( $self->{'currentEntity'} = OBM::Entities::obmObmBackup->new( $self->{'parentDomain'}, $obmBackupDesc ) ) {
            $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
            $self->{'currentEntity'}->setUpdateEntity();
            $self->{'currentEntity'}->setUpdateLinks();

            $self->{'entitiesDescList'} = undef;
            return $self->{'currentEntity'};
        }
    }

    $self->{'currentEntity'} = undef;

    return undef;
}


sub _loadEntities {
    my $self = shift;

    $self->_log( 'chargement de la description du service backup du domaine '.$self->{'parentDomain'}->getDescription().'\'', 3 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 1 );
        return 1;
    }

    my $tablePrefix = '';
    if( $self->{'updateType'} !~ /^(UPDATE_ALL|UPDATE_ENTITY)$/ ) {
        $tablePrefix = 'P_';
    }

    my $query = 'SELECT host_id,
                        host_name,
                        host_ip
                 FROM '.$tablePrefix.'Host
                 INNER JOIN '.$tablePrefix.'DomainEntity ON domainentity_domain_id='.$self->{'domainId'}.'
                 INNER JOIN '.$tablePrefix.'ServiceProperty ON serviceproperty_entity_id=domainentity_entity_id
                 WHERE
                 host_id='.$dbHandler->castAsInteger('serviceproperty_value').'
                    AND serviceproperty_service=\'backup\'';

    if( !defined($dbHandler->execQuery( $query, \$self->{'entitiesDescList'} )) ) {
        $self->_log( 'chargement de la description du service backup depuis la BD impossible', 1 );
        return 1;
    }

    return 0;
}
