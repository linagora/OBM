package OBM::Postfix::postfixEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use Net::Telnet;

delete @ENV{qw(IFS CDPATH ENV BASH_ENV PATH)};


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %postfixEngineAttr = (
        domainList => undef,
        incomingMailDomain => undef,
        incomingMailServerList => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }

    $postfixEngineAttr{"domainList"} = $domainList;

    bless( \%postfixEngineAttr, $self );
}


sub init {
    my $self = shift;
    my $domainsDesc = $self->{"domainList"};

    &OBM::toolBox::write_log( "postfixEngine: initialisation du moteur", "W" );

    # Obtention de la liste des serveurs entrant à mettre à jour
    for( my $i=0; $i<=$#$domainsDesc; $i++ ) {
        my $currentDomainDesc = $domainsDesc->[$i];

        if( $currentDomainDesc->{"meta_domain"} ) {
            next;
        }

        if( !defined($currentDomainDesc->{"imap_servers"}) ) {
            next;
        }

        if( defined($self->{"incomingMailDomain"}) ) {
            $self->{"incomingMailDomain"} .= ":";
        }
        $self->{"incomingMailDomain"} .= $currentDomainDesc->{"domain_label"};

        my $domainSrvList = $currentDomainDesc->{"imap_servers"};
        for( my $j=0; $j<=$#$domainSrvList; $j++ ) {
            push( @{$self->{"incomingMailServerList"}}, $domainSrvList->[$j]->{"imap_server_ip"} );
        }
    }

    return 1;
}


sub destroy {
    my $self = shift;

    &OBM::toolBox::write_log( "postfixEngine: arret du moteur", "W" );

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
    my $cmd = "domains: ".$self->{"incomingMailDomain"};

    for( my $i=0; $i<=$#$srvList; $i++ ) {
        &OBM::toolBox::write_log( "postfixEngine: connexion au serveur : '".$srvList->[$i]."'", "W" );
        my $srvCon = new Net::Telnet(
            Host => $srvList->[$i],
            Port => 30000,
            Timeout => 60,
            errmode => "return"
        );

        if( !defined($srvCon) || !$srvCon->open() ) {
            &OBM::toolBox::write_log( "postfixEngine: echec de connexion au serveur : ".$srvList->[$i], "W" );
            $globalReturn = 0;
            next;
        }

        while( (!$srvCon->eof()) && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixEngine: reponse : '".$line."'", "W" );
        }

        &OBM::toolBox::write_log( "postfixEngine: envoie de la commande : '".$cmd."'", "W" );
        $srvCon->print( $cmd );
        if( (!$srvCon->eof()) && (my $line = $srvCon->getline()) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixEngine: reponse : '".$line."'", "W" );
        }

        &OBM::toolBox::write_log( "postfixEngine: deconnexion du serveur : '".$srvList->[$i]."'", "W" );
        $srvCon->print( "quit" );
        while( !$srvCon->eof() && (my $line = $srvCon->getline(Timeout => 1)) ) {
            chomp($line);
            &OBM::toolBox::write_log( "postfixEngine: reponse : '".$line."'", "W" );
        }

    }

    return $globalReturn;
}
