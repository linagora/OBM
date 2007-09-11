package OBM::Cyrus::cyrusRemoteEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use Net::Telnet;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %cyrusRemoteEngineAttr = (
        domainList => undef,
        cyrusMailServerList => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }

    $cyrusRemoteEngineAttr{"domainList"} = $domainList;

    bless( \%cyrusRemoteEngineAttr, $self );
}


sub init {
    my $self = shift;
    my $domainsDesc = $self->{"domainList"};

    if( !$OBM::Parameters::common::obmModules->{"mail"} ) {
        # Pas de support de la messagerie
        return 0;
    }

    &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: initialisation du moteur", "W" );

    if( !$OBM::Parameters::common::cyrusDomainPartition ) {
        # Pas de support des partitions Cyrus par domaine
        return 0;
    }

    # Obtention de la liste des serveurs entrant à mettre à jour
    for( my $i=0; $i<=$#$domainsDesc; $i++ ) {
        my $currentDomainDesc = $domainsDesc->[$i];

        if( $currentDomainDesc->{"meta_domain"} ) {
            next;
        }

        if( !defined($currentDomainDesc->{"imap_servers"}) ) {
            next;
        }

        my $domainSrvList = $currentDomainDesc->{"imap_servers"};
        for( my $j=0; $j<=$#$domainSrvList; $j++ ) {
            $self->{"cyrusMailServerList"}->{$domainSrvList->[$j]->{"imap_server_name"}} = $domainSrvList->[$j];
        }
    }

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: arret du moteur", "W" );

    return 1;
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub update {
    my $self = shift;
    my( $action ) = @_;
    my $srvList = $self->{"cyrusMailServerList"};
    my $globalReturn = 1;

    if( !defined($action) || ( $action !~ /^add|del$/ ) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: Erreur: vous devez indiquer une action [add|del]", "W" );
        return 0;
    }

    while( my( $serverName, $serverDesc ) = each(%{$srvList}) ) {
        &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: connexion au serveur : '".$serverName."'", "W" );
        my $srvCon = new Net::Telnet(
            Host => $serverDesc->{"imap_server_ip"},
            Port => 30000,
            Timeout => 60,
            errmode => "return"
        );

        if( !defined($srvCon) || !$srvCon->open() ) {
            &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: echec de connexion au serveur : ".$serverName, "W" );
            $globalReturn = 0;
            next;
        }

        while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: reponse : '".$line."'", "W" );
        }

        my $cmd = "cyrusPartitions: ".$action.":".$serverName;
        &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: envoie de la commande : '".$cmd."'", "W" );
        $srvCon->print( $cmd );
        if( (!$srvCon->eof()) && (my $line = $srvCon->getline()) ) {
            chomp($line);
            &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: reponse : '".$line."'", "W" );
        }

        &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: deconnexion du serveur : '".$serverName."'", "W" );
        $srvCon->print( "quit" );
        while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "[Cyrus::cyrusRemoteEngine]: reponse : '".$line."'", "W" );
        }

    }

    return $globalReturn;
}
