package OBM::ObmSatellite::cyrusPartitions;

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
    if( !&OBM::ObmSatellite::utils::connectLdapSrv( $daemonRef->{ldap_server} ) ) {
        $daemonRef->logMessage( "Echec: connexion a l'annuaire LDAP" );
        return undef;
    }


    for( my $i=0; $i<=$#{$domainList}; $i++ ) {
        my $ldapFilter = "(&(objectclass=dcObject)(o=".$domainList->[$i]."))";
        my $ldapAttributes = [ 'dc' ];

        my @ldapEntries;
        if( &OBM::ObmSatellite::utils::ldapSearch( $daemonRef->{ldap_server}, \@ldapEntries, $ldapFilter, $ldapAttributes ) ) {
            $daemonRef->logMessage( "Echec: lors de l'obtention du domaine de messagerie principal du domaine OBM '".$domainList->[$i]."'" );
            next;
        }

        if( $#ldapEntries != 0 ) {
            $daemonRef->logMessage( "Echec: probleme dans la description LDAP du domaine OBM '".$domainList->[$i]."' !!!" );
            next;
        }

        my $referenceList = $ldapEntries[0]->get_value( $ldapAttributes->[0], asref=>1 );
        if( $#{$referenceList} < 0 ) {
            $daemonRef->logMessage( "Echec: domaine de messagerie principal absent pour le domaine OBM '".$domainList->[$i]."' !!!" );
            next;
        }

        # On remplace les caractères spéciaux par des '_'.
        $referenceList->[0] =~ s/\./_/g;
        $referenceList->[0] =~ s/-/_/g;

        # On mémorise l'association 'domain_label'=>'cyrus_domain_partition_name'
        $cyrusPartitionAttr{"domainList"}->{$domainList->[$i]} = $referenceList->[0];

    }

    if( !defined($cyrusPartitionAttr{"domainList"}) ) {
        $daemonRef->logMessage( "Echec: pas de partition Cyrus a traiter" );
        return undef;
    }

    $daemonRef->logMessage( "Deconnexion de l'annuaire LDAP" );
    &OBM::ObmSatellite::utils::disconnectLdapSrv( $daemonRef->{ldap_server} );

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
    #   - creer un modele sans definition de partitions
    #   - reperer les partitions déjà définies.
    my %currentPartitions;
    my @template;
    for( my $i=0; $i<=$#file; $i++ ) {
        if( $file[$i] =~ /^partition-(.+):(.+)$/ ) {
            $currentPartitions{$1} = $2;
        }else {
            push( @template, $file[$i] );
        }
    }

    while( my( $domainLabel, $domainCyrusPartition ) = each(%{$self->{"domainList"}}) ) {
        if( !exists($currentPartitions{$domainCyrusPartition}) ) {
            $self->{"daemonRef"}->logMessage( "Ajout de la partition du domaine '".$domainLabel."'" );
            $currentPartitions{$domainCyrusPartition} = $self->{cyrusPartitionRoot}."/".$domainCyrusPartition;
        }
    }

    # On vide le fichier d'origine
    @file = undef;

    # On parcours le modèle pour remettre la définition des partitions
    $self->{"daemonRef"}->logMessage( "Re-ecriture du fichier de configuration du service Cyrus Imapd '".$self->{"cyrusConfFile"}."'" );
    open( FIC, ">".$self->{"cyrusConfFile"} ) or return 1;
    for( my $i=0; $i<=$#template; $i++ ) {
        if( $template[$i] =~ /^defaultpartition/ ) {
            while( my( $partitionName, $partitionPath ) = each(%currentPartitions) ) {
                print FIC "partition-".$partitionName.": ".$partitionPath."\n";
            }
        }

        print FIC $template[$i]."\n";
    }
    close(FIC);

    return 0;
}
