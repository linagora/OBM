#!/usr/bin/perl -w -T

package mailMakePostfixMaps;

use vars qw(@ISA);
use Net::Server::PreForkSimple;
use URI::Escape;
require OBM::Parameters::mailMakePostfixMapsConf;
use FindBin qw($Bin);
use strict;

@ISA = qw(Net::Server::PreForkSimple);

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};

mailMakePostfixMaps->run();
exit;

$|=1;

### Ecriture des fonctionnalités du service

sub configure_hook {
    my $self = shift;

    # Initalisation des maps Postfix
    if( defined($OBM::Parameters::mailMakePostfixMapsConf::postfixMapsDesc) ) {
        $self->{postfix_maps} = $OBM::Parameters::mailMakePostfixMapsConf::postfixMapsDesc;
    }else {
        print "Erreur: aucune map Postfix n'est a gerer !\n";
        exit 1;
    }

    $self->{server}->{name} = "mailMakePostfixMaps";                        # Nom du service
    $self->{server}->{conf_file} = $Bin."/".$self->{server}->{name}.".cf";  # Définition du fichier de configuration du service


    # Lectrure des paramètres spécifiques
    my $daemonOptions = {
        ldap_server => [],
        max_badrequest => [],
        log_level => [],
        user => [],
        group => [],
        postmap_cmd => [],
        process_mailbox => [],
        mailbox_map => [],
        process_alias => [],
        alias_map => [],
        process_transport => [],
        transport_map => [],
        process_domain => [],
        domain_map => []
    };
    $self->configure( $daemonOptions );

    # Option chargées depuis le fichier de configuration
    if( defined($daemonOptions->{ldap_server}->[0]) && (($daemonOptions->{ldap_server}->[0] =~ /^([1-2]?[0-9]{1,2}\.){3}[1-2]?[0-9]{1,2}$/) || ($daemonOptions->{ldap_server}->[0] =~ /^[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,6}$/)) ) {
        $self->{ldap_server}->{server} = $daemonOptions->{ldap_server}->[0];
    }else {
        print "Vous devez renseigner l'adresse du serveur LDAP !\n";
        exit 1;
    }

    if( defined($daemonOptions->{postmap_cmd}->[0]) ) {
        $self->{postmap_cmd} = $daemonOptions->{postmap_cmd}->[0];
    }else {
        print "Vous devez indiquer le chemin d'acces a la commande 'postmap'\n";
        exit 1;
    }

    if( defined($daemonOptions->{max_badrequest}->[0]) && ($daemonOptions->{max_badrequest}->[0] =~ /^\d+$/) ) {
        $self->{server}->{max_badrequest} = $daemonOptions->{max_badrequest}->[0];
    }else {
        $self->{server}->{max_badrequest} = "5";    # Nombre maximal de mauvaises requêtes durant une même session
    }

    if( defined($daemonOptions->{log_level}->[0]) && ($daemonOptions->{log_level}->[0] =~ /^\d$/) ) {
        $self->{server}->{log_level} = $daemonOptions->{log_level}->[0];
    }else {
        $self->{server}->{log_level} = 0;
    }

    # La table des BALs
    if( $daemonOptions->{process_mailbox}->[0] =~ /^[01]$/ ) {
        $self->{postfix_maps}->{mailbox}->{postfix_map_process} = $daemonOptions->{process_mailbox}->[0];
    }
    if( defined($daemonOptions->{mailbox_map}->[0]) ) {
        $self->{postfix_maps}->{mailbox}->{postfix_map} = $daemonOptions->{mailbox_map}->[0];
    }

    # La table des alias
    if( $daemonOptions->{process_alias}->[0] =~ /^[01]$/ ) {
        $self->{postfix_maps}->{alias}->{postfix_map_process} = $daemonOptions->{process_alias}->[0];
    }
    if( defined($daemonOptions->{alias_map}->[0]) ) {
        $self->{postfix_maps}->{alias}->{postfix_map} = $daemonOptions->{alias_map}->[0];
    }

    # La table de transport
    if( $daemonOptions->{process_transport}->[0] =~ /^[01]$/ ) {
        $self->{postfix_maps}->{transport}->{postfix_map_process} = $daemonOptions->{process_transport}->[0];
    }
    if( defined($daemonOptions->{transport_map}->[0]) ) {
        $self->{postfix_maps}->{transport}->{postfix_map} = $daemonOptions->{transport_map}->[0];
    }

    # La table des domaines
    if( $daemonOptions->{process_domain}->[0] =~ /^[01]$/ ) {
        $self->{postfix_maps}->{domain}->{postfix_map_process} = $daemonOptions->{process_domain}->[0];
    }
    if( defined($daemonOptions->{domain_map}->[0]) ) {
        $self->{postfix_maps}->{domain}->{postfix_map} = $daemonOptions->{domain_map}->[0];
    }


    $self->{server}->{user} = $daemonOptions->{user}->[0];
    $self->{server}->{group} = $daemonOptions->{group}->[0];


    # Lecture des paramètres de configurations standart
    $self->configure;

    # Mise en place des valeurs non modifiables depuis le fichier de
    # configuration
    $self->{server}->{port}  = [ '30000' ];     # Port d'écoute
    $self->{server}->{chdir}  = '/tmp';         # chdir to root

    if( !defined($self->{server}->{user}) ) {
        $self->{server}->{user}   = 'postfix';      # Utilisateur d'exécution du service
    }
    if( !defined($self->{server}->{group}) ) {
        $self->{server}->{group}  = 'postfix';      # Groupe d'exécution du service
    }

    $self->{server}->{setsid} = 1;              # Mode service
    $self->{server}->{no_close_by_child} = 1;   # Le service ne peut-être stoppé par un process fils
    $self->{server}->{max_servers} = 1;         # Nombre maximum de process
    $self->{server}->{max_requests} = 5;        # Nombre maximum de requêtes par process
    $self->{server}->{conn_timeout} = 5;        # Délai entre 2 requêtes
    $self->{server}->{version} = "1.0";         # Serveur version
   

    # Securisation du service
    $self->{server}->{commandline}->[0] = $Bin."/".$self->{server}->{name}.".pl";

}

sub pre_loop_hook {
    my $self = shift;
}

#
# Process after a new connection was established end before the requests was
# process
#
sub post_accept_hook {
    my $self = shift;

    $self->logMessage( "Connexion de : ".$self->{server}->{client}->peerhost()." on port ".$self->{server}->{client}->sockport() );
    $self->sendMessage( "HELLO" );

}

#
# Process the request
#
sub process_request {
    my $self = shift;

    eval {

        # Configuration de l'alarme
        local $SIG{ALRM} = sub {
            $self->sendMessage( "TIMEOUT" );
            die "timeOut";
        };

        #
        # Nombre courant de mauvaises requêtes
        my $current_badrequest = 0;

        #
        # Mise en place du délai de réception d'une requête
        alarm( $self->{server}->{conn_timeout} );
        while( my $currentRequest = <STDIN> ) {
            # Suppression du délai de réception d'une requête durant son
            # traitement
            alarm(0);
            $self->logMessage( $currentRequest );

            if( !$self->checkClientRequest( \$currentRequest ) ) {
                if( $currentRequest =~ /^quit$/i ) {
                    last;
                }

                if( $currentRequest =~ /^domains: (.+:)*.+$/i ) {
                    my $domains = $currentRequest;
                    $domains =~ s/^domains: //i;

                    $self->process_domains( $domains );
                }
            }else {
                $self->sendMessage( "BADREQUEST" );
                $current_badrequest++;
                if( $current_badrequest >= $self->{server}->{max_badrequest} ) {
                    $self->sendMessage( "BADREQUESTS" );
                    die "badRequest";
                }
            }

            # Re-initialisation du délai de réception d'une requête
            alarm( $self->{server}->{conn_timeout} );
        }

        # Suppression du délai de réception d'une requête
        alarm(0);
    };

    if( $@ =~ /^timeOut/ ) {
        $self->logMessage( "Timed Out" );
        return;
    }elsif( $@ =~ /^badRequest/ ) {
        $self->logMessage( "Trop de mauvaises requetes" );
        return;
    }

}

#
# Process after all request was process and before ending connection
sub post_process_request_hook {
    my $self = shift;

    $self->sendMessage( "BYE" );

    my $peer = $self->{server}->{client}->peerhost();
    my $logMessage = "Deconnexion";

    if( defined($peer) ) {
        $logMessage .= " de : ".$peer;
    }

    $self->logMessage( $logMessage );
}

#
# Log message into log file
#
sub logMessage {
    my( $self, $msg ) = @_;

    my $peer = $self->{server}->{client}->peerhost();

    if( defined($peer) ) {
        $self->log( 2, $self->log_time ." - ".$peer." - ".$msg );
    }else {
        $self->log( 2, $self->log_time ." - ".$msg );
    }
}

#
# Send message to client
#
sub sendMessage {
    my( $self, $msg_code ) = @_;
    
    SWITCH: {
        if( $msg_code !~ /^HELLO|TIMEOUT|BADREQUEST|BADREQUESTS|BYE$/ ) {
            $self->logMessage( "Code erreur inconnu : ".$msg_code );
            return 0;
        }

        if( $msg_code =~ /^TIMEOUT$/ ) {
            print "Timed Out!\n";
        }

        if( $msg_code =~ /^HELLO$/ ) {
            print $self->{server}->{name}." v.".$self->{server}->{version}."\n";
        }

        if( $msg_code =~ /^BADREQUEST$/ ) {
            print "Mauvaise requete\n";
        }

        if( $msg_code =~ /^BADREQUESTS$/ ) {
            print "Trop de mauvaises requetes !\n";
        }

        if( $msg_code =~ /^BYE$/ ) {
            print "Bye !\n";
        }
    }

    return 1;
}

#
# Check the request syntax
#
sub checkClientRequest {
    my $self = shift;
    my( $request ) = @_;

    $/="\r\n";
    chomp($$request);
    SWITCH: {
        if( $$request =~ /^quit$/i ) {
            return 0;
        }
        
        if( $$request =~ /^domains: (.+:)*.+$/i ) {
            return 0;
        }
    }

    return 1;

}


#
# Process 'domains: ' request
#
sub process_domains {
    my $self = shift;
    my ( $domains ) = @_;
    use OBM::MakePostfixMaps::utils;

    my $errors = 0;
    my @domainList = split( /:/, $domains );

    # LDAP connection
    $self->logMessage( "Connexion anonyme a l'annuaire LDAP" );
    if( !&OBM::MakePostfixMaps::utils::connectLdapSrv( $self->{ldap_server} ) ) {
        $self->logMessage( "Echec: connexion a l'annuaire LDAP" );
        print "Echec: Impossible de se connecter a l'annuaire LDAP\n";
        return 1;
    }

    foreach my $map ( keys(%{$self->{postfix_maps}}) ) {
        if( ($self->{postfix_maps}->{$map}->{postfix_map_process}) && defined($self->{postfix_maps}->{$map}->{make_map}) ) {
            $self->logMessage( "Traitement de la map de type : '".$map."'" );

            if( &{$self->{postfix_maps}->{$map}->{make_map}}( $self, $self->{postfix_maps}->{$map}, \@domainList ) ) {
                $self->logMessage( "Echec lors de la creation du fichier plat de la map : '".$map."'" );

                $errors = 1;
                last;
            }
        }else {
            $self->{postfix_maps}->{$map}->{postfix_map_postmap} = 0;
        }
    }

    $self->logMessage( "Deconnexion de l'annuaire LDAP" );
    &OBM::MakePostfixMaps::utils::disconnectLdapSrv( $self->{ldap_server} );

    if( !$errors ) {
        my $postmapCmd = $self->{postmap_cmd};
        $self->logMessage( "Generation des maps" );

        foreach my $map ( keys(%{$self->{postfix_maps}}) ) {
            if( $self->{postfix_maps}->{$map}->{postfix_map_postmap} ) {
                my $mapName = $self->{postfix_maps}->{$map}->{postfix_map};
                my $mapType = $self->{postfix_maps}->{$map}->{postfix_map_type};

                $self->logMessage( "Generation de la map '".$map."' de type '".$mapType."', fichier '".$mapName."'" );

                my $cmd = $self->{postmap_cmd}." ".$mapType.":".$mapName;
                $self->logMessage( "Execution de : '".$cmd."'" );
                my $ret = 0xffff & system $cmd;

                if( $ret ) {
                    $errors = 2;
                    last;
                }

            }
        }
    }else {
        $self->logMessage( "Generation des maps annulee" );
    }

    if( $errors == 0 ) {
        print "OK\n";
    }elsif( $errors == 1 ) {
        print "Echec: lors de la creation des fichiers plats\n";
    }elsif( $errors == 2 ) {
        print "Echec: lors de la generation des map Postfix !\n"
    }else {
        print "Echec: erreur inconnue !\n";
    }

    $self->logMessage( "Fin du traitement" );

    return 0;
}

__END__
