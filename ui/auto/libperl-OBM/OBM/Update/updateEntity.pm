package OBM::Update::updateEntity;

$VERSION = '1.0';

use OBM::Update::update;
use OBM::Entities::entityIdGetter;
@ISA = ('OBM::Update::update', 'OBM::Entities::entityIdGetter');

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

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();
    if( !defined($dbHandler) ) {
        $self->_log( 'connecteur a la base de donnee invalide', 3 );
        return undef;
    }

    # Updater initialization
    $self->{'entity'} = $parameters->{'entity'};

    # Domain identifier
    if( defined($parameters->{'domain-id'}) ) {
        $self->{'domainId'} = $parameters->{'domain-id'};
    }else {
        $self->_log( 'Le parametre domain-id doit etre precise', 0 );
        return undef;
    }

    $self->_ckeckEntity( $parameters->{'updateEntityList'} );
    if( !defined($self->{'entity'}) ) {
        $self->_log( 'Aucune entités indiquée ou valides. Au moins une entité à mettre à jour doit être indiquée', 0 );
        return undef;
    }


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );
}


sub _ckeckEntity {
    my $self = shift;
    my( $entityList ) = @_;

    if( ref($entityList) ne 'ARRAY' ) {
        return 1;
    }elsif( $#{$entityList} < 0 ) {
        return 1;
    }

    for( my $i=0; $i<=$#{$entityList}; $i++ ) {
        if( $entityList->[$i] !~ /^(user|mailshare|group|host):(.+)$/ ) {
            $self->_log( 'Entrée invalide : '.$entityList->[$i], 2 );
            next;
        }

        SWITCH: {
            if( ($1 eq 'user') ) {
                my $id =  $self->_getUserIdFromUserLoginDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'USER'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'mailshare') ) {
                my $id = $self->_getMailshareIdFromMailshareNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'MAILSHARE'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'group') ) {
                my $id = $self->_getGroupIdFromGroupNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'GROUP'}}, $id ) if $id;
                last SWITCH;
            }

            if( ($1 eq 'host') ) {
                my $id = $self->_getHostIdFromHostNameDomain( $2, $self->{'domainId'} );
                push( @{$self->{'entities'}->{'HOST'}}, $id ) if $id;
                last SWITCH;
            }

        }
    }
}


sub _updateInitFactory {
    my $self = shift;

    require OBM::entitiesFactory;
    $self->_log( 'initialisation de l\'entity factory', 2 );
    if( !($self->{'entitiesFactory'} = OBM::entitiesFactory->new( 'PROGRAMMABLE', $self->{'domainId'} )) ) {
        $self->_log( 'echec de l\'initialisation de l\'entity factory', 0 );
        return 1;
    }

    while( my($entityType, $entitiesIds) = each(%{$self->{'entities'}}) ) {
        require OBM::EntitiesFactory::factoryProgramming;
        my $factoryProgramming = OBM::EntitiesFactory::factoryProgramming->new();

        if( $factoryProgramming->setEntitiesType( $entityType ) ||
            $factoryProgramming->setUpdateType( 'UPDATE_ALL' ) ||
            $factoryProgramming->setEntitiesIds( $entitiesIds ) ||
            $factoryProgramming->setUpdateLinkedEntities() ||
            $self->{'entitiesFactory'}->loadEntities( $factoryProgramming ) ) {
            $self->_log( 'Impossible de programmer la factory pour les entités de type \''.$entityType.'\'', 0 );
            next;
        }
    }

    return 0;
}
