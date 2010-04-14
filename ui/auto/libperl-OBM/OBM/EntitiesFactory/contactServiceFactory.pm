package OBM::EntitiesFactory::contactServiceFactory;

$VERSION = '1.0';

use OBM::EntitiesFactory::factory;
@ISA = ('OBM::EntitiesFactory::factory');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
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
        $self->_log( 'description du domaine père indéfini', 3 );
        return undef;
    }

    if( ref($parentDomain) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine père incorrecte', 3 );
        return undef;
    }
    $self->{'parentDomain'} = $parentDomain;
    
    $self->{'domainId'} = $parentDomain->getId();
    if( ref($self->{'domainId'}) || ($self->{'domainId'} !~ /$regexp_id/) ) {
        $self->_log( 'identifiant de domaine \''.$self->{'domainId'}.'\' incorrect', 3 );
        return undef;
    }

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'entitiesDescList'} = undef;


    return $self;
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 3 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    if( defined($self->{'entitiesDescList'}) && (my $contactServiceDesc = shift(@{$self->{'entitiesDescList'}})) ) {
        require OBM::Entities::obmContactService;
        if( $self->{'currentEntity'} = OBM::Entities::obmContactService->new( $self->{'parentDomain'}, $contactServiceDesc ) ) {
            SWITCH: {
                if( $self->{'updateType'} eq 'UPDATE_ALL' ) {
                    if( $self->_loadContactServiceLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();

                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'UPDATE_LINKS' ) {
                    if( $self->_loadContactServiceLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'updateType'}.'\'', 0 );
                return undef;
            }

            $self->{'entitiesDescList'} = undef;
            return $self->{'currentEntity'};
        }
    }

    $self->{'currentEntity'} = undef;

    return undef;
}


sub _loadEntities {
    my $self = shift;

    $self->_log( 'chargement de la configuration du service contact du domaine '.$self->{'parentDomain'}->getDescription().'\'', 2 );

    my @dateTime = localtime(time);
    push( @{$self->{'entitiesDescList'}}, {
        currentDate => eval {
                return 1900+$dateTime[5];
            }.'-'.eval {
                $dateTime[4]+=1;
                if($dateTime[4]<10) {
                    return '0'.$dateTime[4];
                }else {
                    return $dateTime[4];
                }
            }.'-'.eval {
                if($dateTime[3]<10) {
                    return '0'.$dateTime[3];
                }else {
                    return $dateTime[3];
                }
            }.' '.eval {
                if($dateTime[2]<10) {
                    return '0'.$dateTime[2].':00:00';
                }else {
                    return $dateTime[2].':00:00';
                }
            }
        } );

    return 0;
}


sub _loadContactServiceLinks {
    my $self = shift;

    return 0;
}
