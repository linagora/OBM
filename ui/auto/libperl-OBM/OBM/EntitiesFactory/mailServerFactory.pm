package OBM::EntitiesFactory::mailServerFactory;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::EntitiesFactory::commonFactory qw(
        _checkSource
        _checkUpdateType
        );
use OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $source, $updateType, $parentDomain ) = @_;

    my $self = bless { }, $class;

    $self->{'source'} = $source;
    if( !$self->_checkSource() ) {
        return undef;
    }

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
    $self->{'mailServerDescList'} = undef;


    return $self;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->_reset();
}


sub _reset {
    my $self = shift;

    $self->_log( 'factory reset', 3 );

    $self->{'running'} = undef;
    $self->{'currentEntity'} = undef;
    $self->{'mailServerDescList'} = undef;

    return 1;
}


sub isRunning {
    my $self = shift;

    if( $self->{'running'} ) {
        $self->_log( 'la factory est en cours d\'exécution', 4 );
        return 1;
    }

    $self->_log( 'la factory n\'est pas en cours d\'exécution', 4 );

    return 0;
}


sub _start {
    my $self = shift;

    $self->_log( 'debut de traitement', 2 );

    if( $self->_loadMailServer() ) {
        $self->_log( 'problème lors de l\'obtention de la description de la configuration des serveurs de courriers du '.$self->{'parentDomain'}->getDescription(), 3 );
        return 0;
    }

    $self->{'running'} = 1;
    return $self->{'running'};
}


sub next {
    my $self = shift;

    $self->_log( 'obtention de l\'entité suivante', 2 );

    if( !$self->isRunning() ) {
        if( !$self->_start() ) {
            $self->_reset();
            return undef;
        }
    }

    if( defined($self->{'mailServerDescList'}) && (my $mailServerDesc = $self->{'mailServerDescList'}->fetchall_arrayref({})) ) {
        require OBM::Entities::obmMailServer;
        if( my $current = OBM::Entities::obmMailServer->new( $self->{'parentDomain'}, $mailServerDesc ) ) {
            $self->{'currentEntity'} = $current;

            SWITCH: {
                if( $self->{'updateType'} eq 'ALL' ) {
                    if( $self->_loadMailServerLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour de l\'entité et des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    $self->{'currentEntity'}->setUpdateLinks();

                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'ENTITY' ) {
                    $self->_log( 'mise à jour de l\'entité, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateEntity();
                    last SWITCH;
                }

                if( $self->{'updateType'} eq 'LINKS' ) {
                    if( $self->_loadMailServerLinks() ) {
                        $self->_log( 'probleme au chargement des liens de l\'entité '.$self->{'currentEntity'}->getDescription(), 2 );
                        return undef;
                    }

                    $self->_log( 'mise à jour des liens, '.$self->{'currentEntity'}->getDescription(), 3 );
                    $self->{'currentEntity'}->setUpdateLinks();
                    last SWITCH;
                }

                $self->_log( 'type de mise à jour inconnu \''.$self->{'currentEntity'}.'\'', 0 );
                return undef;
            }

            $self->{'mailServerDescList'} = undef;
            return $self->{'currentEntity'};
        }
    }

    return undef;
}


sub _loadMailServer {
    my $self = shift;

    $self->_log( 'chargement de la configuration des serveurs de courriers du domaine '.$self->{'parentDomain'}->getDescription().'\'', 2 );

    require OBM::Tools::obmDbHandler;
    my $dbHandler = OBM::Tools::obmDbHandler->instance();

    if( !$dbHandler ) {
        $self->_log( 'connexion à la base de données impossible', 4 );
        return 1;
    }

    my $hostTable = 'Host';
    my $mailServerTable = 'MailServer';
    my $domainMailServerTable = 'DomainMailServer';
    if( $self->{'source'} =~ /^SYSTEM$/ ) {
        my $hostTable = 'P_Host';
        my $mailServerTable = 'P_MailServer';
        my $domainMailServerTable = 'P_DomainMailServer';
    }

    my $query = 'SELECT     hosts.host_name as server_name,
                            domainmailserver.domainmailserver_role as server_role
                 FROM   '.$hostTable.' hosts,
                        '.$mailServerTable.' mailserver,
                        '.$domainMailServerTable.' domainmailserver 
                 WHERE hosts.host_id=mailserver.mailserver_host_id AND mailserver.mailserver_id=domainmailserver.domainmailserver_mailserver_id AND domainmailserver.domainmailserver_domain_id='.$self->{'domainId'};

    if( !defined($dbHandler->execQuery( $query, \$self->{'mailServerDescList'} )) ) {
        $self->_log( 'chargement de la configuration des serveurs de courriers depuis la BD impossible', 3 );
        return 1;
    }

    return 0;
}


sub _loadMailServerLinks {
    my $self = shift;

    return 0;
}
