package OBM::Cyrus::cyrusEngine;

$VERSION = "1.0";

$debug = 1;

use 5.006_001;
require Exporter;
use Unicode::MapUTF8 qw(to_utf8 from_utf8 utf8_supported_charset);
use strict;

use OBM::Parameters::common;
use Cyrus::IMAP::Admin;


sub new {
    my $self = shift;
    my( $domainList ) = @_;

    # Definition des attributs de l'objet
    my %cyrusEngineAttr = (
        domainList => undef,
        rightDefinition => undef
    );


    if( !defined($domainList) ) {
        croak( "Usage: PACKAGE->new(DOMAINLIST)" );
    }else {
        $cyrusEngineAttr{"domainList"} = $domainList;
    }

    # Definition des droits
    $cyrusEngineAttr{"rightDefinition"} = {
        none => "none",
        read => "lrs",
        writeonly => "li",
        write => "lrswicd",
        admin => "dc",
        post => "p"
    };

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
        if( $cyrusSrvList->[$i]->{"imap_server_id"} == $cyrusSrvId ) {
            $cyrusSrv = $cyrusSrvList->[$i];
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

    # Récupération des identifiants du serveur de la boîte à traiter
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
    my $isExist = $self->isMailboxExist( $srvConn, $object, \%srvBalDesc );
    if( !defined($isExist) ) {
        &OBM::toolBox::write_log( "cyrusEngine: probleme lors de l'obtention des informations de la boite sur le serveur", "W" );
        return 0;

    }elsif( $isExist && $object->getDelete() ) {
        # On la supprime
        &OBM::toolBox::write_log( "cyrusEngine: suppression de la boite", "W" );
        if( !$self->_deleteBox( $cyrusSrv, $object ) ) {
            &OBM::toolBox::write_log( "cyrusEngine: echec lors de la suppression de la boite", "W" );
        }

    }elsif( $isExist && !$object->getDelete() ) {  
        # On met à jour
        &OBM::toolBox::write_log( "cyrusEngine: MAJ de la boite", "W" );

    }elsif( !$isExist && !$object->getDelete() ) {
        # On la cré
        &OBM::toolBox::write_log( "cyrusEngine: creation de la boite", "W" );

        if( !$self->_createMailbox( $cyrusSrv, $object ) ) {
            &OBM::toolBox::write_log( "cyrusEngine: echec lors de la creation de la boite", "W" );
        }

    }

    return 1;
}


sub isMailboxExist {
    my $self = shift;
    my( $cyrusSrvConn, $object, $srvBalDesc ) = @_;
    my $mailboxPrefix = $object->getMailboxPrefix();
    my $mailBoxName = $object->getMailboxName();

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


    # Si la boîte existe on charge ses caractéristiques
    $srvBalDesc->{"box_name"} = $mailBox[0][0];
    $srvBalDesc->{"box_login"} = $mailBox[0][0];
    $srvBalDesc->{"box_login"} =~ s/^$mailboxPrefix//;
    $srvBalDesc->{"box_quota"} = $self->getMailboxQuota( $cyrusSrvConn, $object );


    return 1;
}


sub getMailboxQuota {
    my $self = shift;
    my( $cyrusSrvConn, $object ) = @_;
    my $mailBoxQuota = 0;

    if( !defined($cyrusSrvConn) ) {
        return undef;
    }

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName();
    my $boxPrefix = $object->getMailboxPrefix();


    my @quotaDesc = $cyrusSrvConn->listquotaroot( $boxPrefix.$boxName );
    if( $cyrusSrvConn->error ) {
        return undef;
    }

    if( defined( $quotaDesc[2][1] ) ) {
        $mailBoxQuota = $quotaDesc[2][1];
    }

    return $mailBoxQuota;
}


sub _imapSetMailboxQuota {
    my $self = shift;
    my( $cyrusSrvConn, $object ) = @_;

    if( !defined($cyrusSrvConn) ) {
        return 0;
    }

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName();
    my $boxPrefix = $object->getMailboxPrefix();
    my $boxQuota = $object->getMailboxQuota();

    if( !$boxQuota ) {
        $cyrusSrvConn->setquota( $boxPrefix.$boxName );
    }else {
        $cyrusSrvConn->setquota( $boxPrefix.$boxName, "STORAGE", $boxQuota );
    }

    if( $cyrusSrvConn->error ) {
        return 0;
    }

    return 1;
}


sub _deleteBox {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName();
    my $boxPrefix = $object->getMailboxPrefix();

    if( !$self->_imapSetMailboxAcl( $cyrusSrv, $object, $cyrusSrv->{"imap_server_login"}, "admin" ) ) {
        return 0;
    }

    $cyrusSrvConn->delete($boxPrefix.$boxName);
    if( $cyrusSrvConn->error() ) {
        return 0;
    }

    return 1;
}


sub _createMailbox {
    my $self = shift;
    my( $cyrusSrv, $object ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName();
    my $boxPrefix = $object->getMailboxPrefix();

    # Création de la boîte
    $cyrusSrvConn->create( $boxPrefix.$boxName );
    if( $cyrusSrvConn->error() ) {
        return 0;
    }

    # Positionnement du quota
    if( !$self->_imapSetMailboxQuota() ) {
        ##### Probleme ICI le quota n'est pas positionne a la creation
        return 0;
    }

    return 1;
}


sub _imapSetMailboxAcl {
    my $self = shift;
    my( $cyrusSrv, $object, $boxRightUser, $boxRight ) = @_;

    if( !defined($cyrusSrv->{"imap_server_conn"}) ) {
        return 0;
    }
    my $cyrusSrvConn = $cyrusSrv->{"imap_server_conn"};

    if( !defined($object) ) {
        return 0;
    }

    my $boxName = $object->getMailboxName();
    my $boxPrefix = $object->getMailboxPrefix();


    # Préparation du droit à assigner
    my $rights = $self->{"rightDefinition"};
    if( !defined($rights->{$boxRight}) ) {
        return 0;
    }

    my $imapRight = $rights->{$boxRight};
    if( ($boxRightUser eq "anyone") && ($boxRight ne "none") ) {
        $imapRight .= $rights->{"post"};
    }

    $cyrusSrvConn->setaclmailbox( $boxPrefix.$boxName, $boxRightUser => $imapRight );
    if( $cyrusSrvConn->error() ) {
        return 0;
    }

    return 1;
}
