package OBM::Cyrus::cyrusEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use OBM::Parameters::cyrusConf;
use Cyrus::IMAP::Admin;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %cyrusEngineAttr = (
        domainList => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $cyrusEngineAttr{"domainList"} = $domainList;
    }

    bless( \%cyrusEngineAttr, $self );
}


sub init {
    my $self = shift;

    # Etablissement des connexions
    $self->_cyrusSrvsConn( 1 );

    return 1;
}


sub destroy {
    my $self = shift;

    return $self->_cyrusSrvsConn( 0 );
}


sub dump {
    my $self = shift;
    my @desc;

    push( @desc, $self );

    require Data::Dumper;
    print Data::Dumper->Dump( \@desc );

    return 1;
}


sub _cyrusSrvsConn {
    my $self = shift;
    my( $connect ) = @_;
    my $domainsDesc = $self->{"domainList"};

    for( my $i=0; $i<=$#$domainsDesc; $i++ ) {
        my $currentDomainDesc = $domainsDesc->[$i];

        if( $currentDomainDesc->{"meta_domain"} ) {
            next;
        }

        if( !defined($currentDomainDesc->{"imap_servers"}) ) {
            next;
        }

        my $domainSrvList = $currentDomainDesc->{"imap_servers"};
        if( $#$domainSrvList < 0 ) {
            next;
        }

        &OBM::toolBox::write_log( "cyrusEngine: gestion des serveurs IMAP du domaine '".$currentDomainDesc->{"domain_name"}."'", "W" );
        for( my $j=0; $j<=$#$domainSrvList; $j++ ) {
            if( $connect ) {
                $self->_connectCyrusSrv( $domainSrvList->[$j] );
            }else {
                $self->_disconnectCyrusSrv( $domainSrvList->[$j] );
            }
        }
    }

    return 1;
}


sub _connectCyrusSrv {
    my $self = shift;
    my( $srvDesc ) = @_;

    if( !defined($srvDesc->{"imap_server_login"}) ) {
        return 0;
    }elsif( !defined($srvDesc->{"imap_server_passwd"}) ) {
        return 0;
    }elsif( !defined($srvDesc->{"imap_server_ip"}) ) {
        return 0;
    }

    &OBM::toolBox::write_log( "cyrusEngine: connexion au serveur IMAP '".$srvDesc->{"imap_server_name"}."' en tant que '".$srvDesc->{"imap_server_login"}."'", "W" );

    $srvDesc->{"imap_server_conn"} = Cyrus::IMAP::Admin->new($srvDesc->{"imap_server_ip"});

    if( !defined( $srvDesc->{"imap_server_conn"} ) ) {
        &OBM::toolBox::write_log( "cyrusEngine: probleme lors de la connexion au serveur IMAP", "W" );
        $self->_disconnectCyrusSrv( $srvDesc );
        return 0;

    }else {
        if( $srvDesc->{"imap_server_conn"}->authenticate( -user=>$srvDesc->{"imap_server_login"}, -password=>$srvDesc->{"imap_server_passwd"}, -mechanism=>"login" ) ) {
            &OBM::toolBox::write_log( "cyrusEngine: connexion au serveur IMAP etablie", "W" );

        }else {
            &OBM::toolBox::write_log( "cyrusEngine: echec de connexion au serveur IMAP", "W" );
            $self->_disconnectCyrusSrv( $srvDesc );
            return 0;

        }
    }

    return 1;
}


sub _disconnectCyrusSrv {
    my $self = shift;
    my( $srvDesc ) = @_;

    if( defined($srvDesc->{"imap_server_conn"}) ) {
        &OBM::toolBox::write_log( "cyrusEngine: deconnexion du serveur '".$srvDesc->{"imap_server_name"}."'", "W" );
        undef $srvDesc->{"imap_server_conn"};
    }

    return 1;
}


sub _findDomainbyId {
    my $self = shift;
    my( $domainId ) = @_;
    my $domainDesc = undef;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;
    }

    for( my $i=0; $i<=$#{$self->{"domainList"}}; $i++ ) {
        if( $self->{"domainList"}->[$i]->{"domain_id"} == $domainId ) {
            $domainDesc = $self->{"domainList"}->[$i];
            last;
        }
    }

    return $domainDesc;
}


sub _findCyrusSrvbyId {
    my $self = shift;
    my( $domainId, $cyrusSrvId ) = @_;

    if( !defined($domainId) || ($domainId !~ /^\d+$/) ) {
        return undef;

    }elsif(!defined($cyrusSrvId) || ($cyrusSrvId !~ /^\d+$/) ) {
        return undef;

    }

    my $domainDesc = $self->_findDomainbyId( $domainId );
    if( !defined($domainDesc) ) {
        return undef;
    }

    if( !defined($domainDesc->{"imap_servers"}) || ($#{$domainDesc->{"imap_servers"}} < 0) ) {
        return undef;
    }

    my $cyrusSrvList = $domainDesc->{"imap_servers"};
    my $cyrusSrv = undef;
    for( my $i=0; $i<=$#$cyrusSrvList; $i++ ) {
        if( $cyrusSrvList->{"imap_server_id"} == $cyrusSrvId ) {
            $cyrusSrv = $cyrusSrvList;
            last;
        }
    }

    return $cyrusSrv;
}


sub _doWork {
    my $self = shift;
    my( $dn, $object ) = @_;

    return 1;
}


sub update {
    my $self = shift;
    my( $object ) = @_;

    if( !defined($object) ) {
        return 0;
    }

    # Récupération du nom de la boîte à traiter
    my $mailBoxName = $object->getMailboxName();
    if( !defined($mailBoxName) ) {
        return 0;
    }

    # Récupération des identifiant du serveur de la boîte à traiter
    my $mailBoxDomainId;
    my $mailBoxServerId;
    $object->getMailServerRef( \$mailBoxDomainId, \$mailBoxServerId );

    # Récupération de la description du serveur de la boîte à traiter
    my $cyrusSrv = $self->_findCyrusSrvbyId( $mailBoxDomainId, $mailBoxServerId );
    if( !defined($cyrusSrv) ) {
        return 0;
    }
    # Est-on connecté à ce serveur
    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $srvConn = $cyrusSrv->{"imap_server_conn"};

    &OBM::toolBox::write_log( "cyrusEngine: gestion de la boite '".$mailBoxName."', du serveur '".$cyrusSrv->{"imap_server_ip"}."'", "W" );

    # La bal existe ?
    my %srvBalDesc;
    my $isExist = $self->isMailboxExist( $srvConn, $object->getMailboxPrefix(), $mailBoxName, \%srvBalDesc );
    if( !defined($isExist) ) {
        &OBM::toolBox::write_log( "cyrusEngine: probleme lors de l'obtention des informations de la boite sur le serveur", "W" );
        return 0;

    }elsif( $isExist && $object->getDelete() ) {
        # On la supprime

    }elsif( $isExist ) {  
        # On met à jour

    }else {
        # On cré

    }

    return 1;
}


sub isMailboxExist {
    my $self = shift;
    my( $cyrusSrvConn, $mailboxPrefix, $mailBoxName, $srvBalDesc ) = @_;

    if( !defined($cyrusSrvConn) ) {
        return undef;
    }

    if( !defined($mailBoxName) ) {
        return undef;
    }

    my @mailBox = $cyrusSrvConn->listmailbox( $mailBoxName, $mailboxPrefix );
    if( $cyrusSrvConn->error ) {
        return undef;
    }

    if( $#mailBox < 0 ) {
        return 0;
    }elsif( $#mailBox > 0 ) {
        return undef;
    }

    $srvBalDesc->{"box_name"} = $mailBox[0][0];
    $srvBalDesc->{"box_login"} = $mailBox[0][0];
    $srvBalDesc->{"box_login"} =~ s/^$mailboxPrefix//;
    $srvBalDesc->{"box_quota"} =~ $self->getMailboxQuota( $cyrusSrvConn, $srvBalDesc->{"box_name"} );
    #### ICI ####
    
    return 0;
}


sub getMailboxQuota {
    my $self = shift;
    my( $cyrusSrvConn, $boxName ) = @_;
    my $mailBoxQuota = 0;

    if( !defined($cyrusSrvConn) || !defined($boxName) ) {
        return undef;
    }

    my @quotaDesc = $cyrusSrvConn->listquotaroot( $boxName );
    if( $cyrusSrvConn->error ) {
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuota = $quotaDesc[2][1];
    }

    return $mailBoxQuota;
}
