package ObmSatellite::cyrusPartitions;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub new {
    my $self = shift;
    my( $daemonRef, $domainList ) = @_;

    $daemonRef->logMessage( "Creation de l'objet 'cyrusPartitions'" );

    my %cyrusPartitionAttr = (
        daemonRef => undef,
        cyrusConfFile => undef,
        cyrusStartupScript => undef,
        cyrusPartitionRoot => undef,
        domainList => undef
    );

    if( !defined($daemonRef) ) {
        croak( "Usage: PACKAGE->new(DAEMONREF, DOMAINLIST)" );
    }else {
        $cyrusPartitionAttr{"daemonRef"} = $daemonRef;
    }

    if( !defined($domainList) || (ref($domainList) ne "ARRAY" ) ) {
        croak( "Usage: PACKAGE->new(DAEMONREF, DOMAINLIST)" );
    }

    # Fichier de configuration du service
    $cyrusPartitionAttr{"cyrusConfFile"} = $daemonRef->{cyrus}->{cyrus_imap_conf};

    if( !-w $cyrusPartitionAttr{"cyrusConfFile"} ) {
        $daemonRef->logMessage( "Echec: le fichier de configuration '".$cyrusPartitionAttr{"cyrusConfFile"}."' n'est pas accessible en ecriture. MAJ impossible !" );
        return undef;
    }

    # Script de gestion du service
    $cyrusPartitionAttr{"cyrusStartupScript"} = $daemonRef->{cyrus}->{cyrus_service};
    if( !-x $cyrusPartitionAttr{"cyrusStartupScript"} ) {
        $daemonRef->logMessage( "Echec: le script de gestion du service Cyrus '".$cyrusPartitionAttr{"cyrusStartupScript"}."' n'est pas executable.  MAJ impossible !" );
        return undef;
    }

    # Racine des partitions Cyrus Imapd
    $cyrusPartitionAttr{"cyrusPartitionRoot"} = $daemonRef->{cyrus}->{cyrus_partition_root};

    # LDAP connection
    $daemonRef->logMessage( "Connexion anonyme a l'annuaire LDAP" );
    if( !&ObmSatellite::utils::connectLdapSrv( $daemonRef->{ldap_server} ) ) {
        $daemonRef->logMessage( "Echec: connexion a l'annuaire LDAP" );
        return undef;
    }


    for( my $i=0; $i<=$#{$domainList}; $i++ ) {
	# On mémorise l'association 'domain_name'=>'cyrus_domain_partition_name'
	$cyrusPartitionAttr{"domainList"}->{$domainList->[$i]} = $domainList->[$i];

        # On remplace les caractères spéciaux par des '_'.
        $cyrusPartitionAttr{"domainList"}->{$domainList->[$i]} =~ s/\./_/g;
        $cyrusPartitionAttr{"domainList"}->{$domainList->[$i]} =~ s/-/_/g;
    }

    if( !defined($cyrusPartitionAttr{"domainList"}) ) {
        $daemonRef->logMessage( "Echec: pas de partition Cyrus a traiter" );
        return undef;
    }

    $daemonRef->logMessage( "Deconnexion de l'annuaire LDAP" );
    &ObmSatellite::utils::disconnectLdapSrv( $daemonRef->{ldap_server} );

    $daemonRef->logMessage( "Objet 'cyrusPartitions' cree" );

    bless( \%cyrusPartitionAttr, $self );
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );
    
    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub addPartitions {
    my $self = shift;
    
    $self->{"daemonRef"}->logMessage( "Chargement du fichier '".$self->{"cyrusConfFile"}."'" );
    if( !open( FIC, $self->{"cyrusConfFile"} ) ) {
        $self->{"daemonRef"}->logMessage( "Problème lors du chargement du fichier '".$self->{"cyrusConfFile"}."'" );
        return 1;
    }

    open( FIC, $self->{"cyrusConfFile"} );
    my $line = <FIC>;
    close(FIC);
    my @file = split( '\n', $line );
    $line = undef;

    # Parcours du fichier de configuration pour :
    #   - créer un modèle sans définition de partitions
    #   - repérer les partitions déjà définies.
    my %currentPartitions;
    my $defaultPartitionName;
    my @template;
    for( my $i=0; $i<=$#file; $i++ ) {
        if( $file[$i] =~ /^partition-(.+)\s*:(.+)$/ ) {
            my $partitionName = $1;
            $currentPartitions{$partitionName} = $2;
            $currentPartitions{$partitionName} =~ s/^\s+//;

            $self->{"daemonRef"}->logMessage( "Chargement de la partition '".$partitionName."', repertoire '".$currentPartitions{$partitionName}."' depuis le fichier '".$self->{"cyrusConfFile"}."'" );
        }elsif( $file[$i] =~ /^defaultpartition\s*:(.+)$/ ) {
            $defaultPartitionName = $1;
            $defaultPartitionName =~ s/^\s+//;

            if( $defaultPartitionName !~ /^[a-zA-Z0-9]+$/ ) {
                $self->{"daemonRef"}->logMessage( "Le nom de la partition par defaut '".$defaultPartitionName."' est incorrect. Il doit etre compose uniquement de caracteres alpha-numeriques" );
                $defaultPartitionName = undef;
            }else {
                $self->{"daemonRef"}->logMessage( "Chargement de la partition par defaut '".$defaultPartitionName."' depuis le fichier '".$self->{"cyrusConfFile"}."'" );
            }
        }else {
            push( @template, $file[$i] );
        }
    }

    if( !defined($defaultPartitionName) ) {
        $self->{"daemonRef"}->logMessage( "Pas de partition par defaut definie" );
    }

    if( defined($defaultPartitionName) && !defined($currentPartitions{$defaultPartitionName}) ) {
        $self->{"daemonRef"}->logMessage( "Pas de definition de la partition par defaut '".$defaultPartitionName."'" );
        $defaultPartitionName = undef;
    }

    while( my( $domainLabel, $domainCyrusPartition ) = each(%{$self->{"domainList"}}) ) {
        if( !exists($currentPartitions{$domainCyrusPartition}) ) {
            $currentPartitions{$domainCyrusPartition} = $self->{cyrusPartitionRoot}."/".$domainCyrusPartition;
            $self->{"daemonRef"}->logMessage( "Ajout de la partition '".$domainCyrusPartition."', repertoire '".$currentPartitions{$domainCyrusPartition}."', du domaine '".$domainLabel."'" );
        }
    }

    # On vide le fichier d'origine
    @file = undef;

    # On parcours le modèle pour remettre la définition des partitions
    $self->{"daemonRef"}->logMessage( "Re-ecriture du fichier de configuration du service Cyrus Imapd '".$self->{"cyrusConfFile"}."'" );
    my $partitionsDone = 0;
    open( FIC, ">".$self->{"cyrusConfFile"} ) or return 1;

    # Definition des partitions Cyrus
    while( my( $partitionName, $partitionPath ) = each(%currentPartitions) ) {
        print FIC "partition-".$partitionName.": ".$partitionPath."\n";
    }

    if( defined($defaultPartitionName) ) {
        print FIC "defaultpartition: ".$defaultPartitionName."\n";
    }else {
    	$self->{"daemonRef"}->logMessage( "[ATTENTION] Pas de partition par defaut definie, si vous souhaitez en definir une, ajoutez la directive 'defaultpartition' dans le fichier '".$self->{"cyrusConfFile"}."', et re-demarrez le service Cyrus" );
    }

    for( my $i=0; $i<=$#template; $i++ ) {
        print FIC $template[$i]."\n";
    }
    close(FIC);

    return 0;
}
