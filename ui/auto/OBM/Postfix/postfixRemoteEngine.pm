package OBM::Postfix::postfixRemoteEngine;

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
    my %postfixRemoteEngineAttr = (
        domainList => undef,
        incomingMailServerList => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }

    $postfixRemoteEngineAttr{"domainList"} = $domainList;

    bless( \%postfixRemoteEngineAttr, $self );
}


sub init {
    my $self = shift;
    my $domainsDesc = $self->{"domainList"};

    if( !$OBM::Parameters::common::obmModules->{"mail"} ) {
        return 0;
    }

    &OBM::toolBox::write_log( "postfixRemoteEngine: initialisation du moteur", "W" );

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
            $self->{"incomingMailServerList"}->{$domainSrvList->[$j]->{"imap_server_name"}} = $domainSrvList->[$j];
        }
    }

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "postfixRemoteEngine: arret du moteur", "W" );

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
    my $srvList = $self->{"incomingMailServerList"};
    my $globalReturn = 1;

    while( my( $serverName, $serverDesc ) = each(%{$srvList}) ) {
        &OBM::toolBox::write_log( "postfixRemoteEngine: connexion au serveur : '".$serverName."'", "W" );
        my $srvCon = new Net::Telnet(
            Host => $serverDesc->{"imap_server_ip"},
            Port => 30000,
            Timeout => 60,
            errmode => "return"
        );

        if( !defined($srvCon) || !$srvCon->open() ) {
            &OBM::toolBox::write_log( "postfixRemoteEngine: echec de connexion au serveur : ".$serverName, "W" );
            $globalReturn = 0;
            next;
        }

        while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixRemoteEngine: reponse : '".$line."'", "W" );
        }

        my $cmd = "postfixMaps: ".$serverName;
        &OBM::toolBox::write_log( "postfixRemoteEngine: envoie de la commande : '".$cmd."'", "W" );
        $srvCon->print( $cmd );
        if( (!$srvCon->eof()) && (my $line = $srvCon->getline()) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixRemoteEngine: reponse : '".$line."'", "W" );
        }

        &OBM::toolBox::write_log( "postfixRemoteEngine: deconnexion du serveur : '".$serverName."'", "W" );
        $srvCon->print( "quit" );
        while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixRemoteEngine: reponse : '".$line."'", "W" );
        }

    }

    return $globalReturn;
}
