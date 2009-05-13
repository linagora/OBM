package OBM::Entities::obmMailshare;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;


sub new {
    my $class = shift;
    my( $parent, $mailshareDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 3 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $mailshareDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du mailshare', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'obmMailShare' ];

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'parent'} = undef;
}


sub _init {
    my $self = shift;
    my( $mailshareDesc ) = @_;

    if( !defined($mailshareDesc) || (ref($mailshareDesc) ne 'HASH') ) {
        $self->_log( 'description du mailshare incorrect', 4 );
        return 1;
    }

    # L'ID du mailshare
    if( !defined($mailshareDesc->{'mailshare_id'}) ) {
        $self->_log( 'ID du mailshare non défini', 0 );
        return 1;
    }elsif( $mailshareDesc->{'mailshare_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$mailshareDesc->{'mailshare_id'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom du mailshare
    if( !defined($mailshareDesc->{'mailshare_name'}) ) {
        $self->_log( 'Nom du mailshare non défini', 0 );
        return 1;
    }
    
    $mailshareDesc->{'system_mailshare_name'} = lc($mailshareDesc->{'mailshare_name'});
    if( $mailshareDesc->{'system_mailshare_name'} !~ /$OBM::Parameters::regexp::regexp_mailsharename/ ) {
        $self->_log( 'Nom du mailshare \''.$mailshareDesc->{'mailshare_name'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom actuel du mailshare, si définit
    if( defined($mailshareDesc->{'mailshare_name_current'}) ) {
        $mailshareDesc->{'mailshare_name_current'} = lc($mailshareDesc->{'mailshare_name_current'});
        if( !$mailshareDesc->{'mailshare_name_current'} || $mailshareDesc->{'mailshare_name_current'} !~ /$OBM::Parameters::regexp::regexp_mailsharename/ ) {
            $self->_log( 'Nom actuel du mailshare \''.$mailshareDesc->{'mailshare_name_current'}.'\' incorrect', 0 );
            return 1;
        }
    }

    SWITCH: {
        require OBM::Cyrus::cyrusServers;
        my $cyrusSrvList = OBM::Cyrus::cyrusServers->instance();
        if( !(my $cyrusHostName = $cyrusSrvList->getCyrusServerIp( $mailshareDesc->{'mailshare_mail_server_id'}, $mailshareDesc->{'mailshare_domain_id'} ) ) ) {
            $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.' annulé, serveur inconnu', 2 );
            $mailshareDesc->{'mailshare_mail_perms'} = 0;
            last SWITCH;
        }else {
            $mailshareDesc->{'mailshare_mail_server'} = 'lmtp:'.$cyrusHostName.':24';
        }

        if( !($self->_makeEntityEmail( $mailshareDesc->{'mailshare_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') )) ) {
            $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.'annulé, pas d\'adresses mails valides', 2 );
            $mailshareDesc->{'mailshare_mail_perms'} = 0;
            last SWITCH;
        }

        $mailshareDesc->{'mailshare_mail_perms'} = 1;
    }

    # LDAP BAL destination
    $mailshareDesc->{'mailshare_ldap_mailbox'} = '+'.$mailshareDesc->{'system_mailshare_name'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Cyrus BAL destination
    $mailshareDesc->{'mailshare_cyrus_mailbox'} = $mailshareDesc->{'system_mailshare_name'};
    # Current Cyrus BAL destination
    $mailshareDesc->{'current_mailshare_cyrus_mailbox'} = $mailshareDesc->{'mailshare_name_current'};
    if( !$OBM::Parameters::common::singleNameSpace ) {
        $mailshareDesc->{'mailshare_cyrus_mailbox'} .= '@'.$self->{'parent'}->getDesc('domain_name');
        $mailshareDesc->{'current_mailshare_cyrus_mailbox'} .= '@'.$self->{'parent'}->getDesc('domain_name');
    }

    # Cyrus partition
    if( $OBM::Parameters::common::cyrusDomainPartition ) {
        $mailshareDesc->{'mailshare_cyrus_partition'} = $self->{'parent'}->getDesc('domain_name');
        $mailshareDesc->{'mailshare_cyrus_partition'} =~ s/\./_/g;
        $mailshareDesc->{'mailshare_cyrus_partition'} =~ s/-/_/g;
    }

    # Cyrus quota
    $mailshareDesc->{'mailshare_cyrus_quota'} = 0 if !defined( $mailshareDesc->{'mailshare_cyrus_quota'} );
    $mailshareDesc->{'mailshare_cyrus_quota'} = $mailshareDesc->{'mailshare_quota'}*1024;

    # BAL sub-folders
    if( defined($OBM::Parameters::common::shareMailboxDefaultFolders) ) {
        foreach my $folderTree ( split( ',', $OBM::Parameters::common::shareMailboxDefaultFolders ) ) {
            if( $folderTree !~ /(^[",]$)|(^$)/ ) {
                my $folderName = $mailshareDesc->{'system_mailshare_name'};
                foreach my $folder ( split( '/', $folderTree ) ) {
                    $folder =~ s/^\s+//;

                    $folderName .= '/'.$folder;
                    if( !$singleNameSpace ) {
                        push( @{$mailshareDesc->{mailbox_folders}}, $folderName.'@'.$self->{'parent'}->getDesc('domain_name') );
                    }else {
                        push( @{$mailshareDesc->{mailbox_folders}}, $folderName );
                    }
                }
            }
        }
    }

    $self->{'entityDesc'} = $mailshareDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'HASH' ) {
        $self->_log( 'pas de liens définis', 3 );
        return 0;
    }

    $self->{'entityDesc'}->{'mailshare_acl'} = $links;

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $mailshareDesc = $self->{'entityDesc'};

    my $description = 'mailshare d\'ID \''.$mailshareDesc->{'mailshare_id'}.'\', nom \''.$mailshareDesc->{'mailshare_name'}.'\'';

    return $description;
}


# Needed
sub getDesc {
    my $self = shift;
    my( $desc ) = @_;

    if( $desc && !ref($desc) ) {
        return $self->{'entityDesc'}->{$desc};
    }

    return undef;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_domain_id'};
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_id'};
}


# Needed by : LdapEngine
sub getLdapServerId {
    my $self = shift;

    if( defined($self->{'parent'}) ) {
        return $self->{'parent'}->getLdapServerId();
    }

    return undef;
}


# Needed by : LdapEngine
sub setParent {
    my $self = shift;
    my( $parent ) = @_;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine parent incorrecte', 3 );
        return 1;
    }

    $self->{'parent'} = $parent;

    return 0;
}


# Needed by : LdapEngine
sub _getParentDn {
    my $self = shift;
    my $parentDn = undef;

    if( defined($self->{'parent'}) ) {
        $parentDn = $self->{'parent'}->getDnPrefix($self);
    }

    return $parentDn;
}



# Needed by : LdapEngine
sub getDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 3 );
        return undef;
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'cn='.$self->{'entityDesc'}->{'system_mailshare_name'}.','.$rootDn->[$i] );
        $self->_log( 'nouveau DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


# Needed by : LdapEngine
sub getCurrentDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 3 );
        return undef;
    }

    my $currentMailshareName = $self->{'entityDesc'}->{'system_mailshare_name'};
    if( $self->{'entityDesc'}->{'mailshare_name_current'} && ($currentMailshareName ne $self->{'entityDesc'}->{'mailshare_name_current'}) ) {
        $currentMailshareName = $self->{'entityDesc'}->{'mailshare_name_current'};
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'cn='.$currentMailshareName.','.$rootDn->[$i] );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub _getLdapObjectclass {
    my $self = shift;
    my ($objectclass, $deletedObjectclass) = @_;

    return $self->{'objectclass'};
}


sub createLdapEntry {
    my $self = shift;
    my ( $entryDn, $entry ) = @_;

    if( !$entryDn ) {
        $self->_log( 'DN non défini', 3 );
        return 1;
    }

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 3 );
        return 1;
    }

    $entry->add(
        objectClass => $self->_getLdapObjectclass(),
        cn => $self->{'entityDesc'}->{'system_mailshare_name'}
    );

    if( $self->{'entityDesc'}->{'mailshare_ldap_mailbox'} ) {
        $entry->add( mailBox => $self->{'entityDesc'}->{'mailshare_ldap_mailbox'} );
    }

    if( $self->{'entityDesc'}->{'mailshare_description'} ) {
        $entry->add( description => $self->{'entityDesc'}->{'mailshare_description'} );
    }

    if( $self->{'entityDesc'}->{'mailshare_mail_server'} ) {
        $entry->add( mailBoxServer => $self->{'entityDesc'}->{'mailshare_mail_server'} );
    }

    if( $self->{'email'} ) {
        $entry->add( mail => $self->{'email'} );
    }

    if( $self->{'emailAlias'} ) {
        $entry->add( mailAlias => $self->{'emailAlias'} );
    }

    if( $self->{'entityDesc'}->{'mailshare_mail_perms'} ) {
        $entry->add( mailAccess => 'PERMIT' );
    }else {
        $entry->add( mailAccess => 'REJECT' );
    }

    # Le domaine OBM
    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
    }

    return 0;
}


sub updateLdapEntry {
    my $self = shift;
    my( $entry, $objectclassDesc ) = @_;
    my $update = 0;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        return $update;
    }


    if( $self->getUpdateEntity() ) {
        # Vérification des objectclass
        my @deletedObjectclass;
        my $currentObjectclass = $self->_getLdapObjectclass( $entry->get_value('objectClass', asref => 1), \@deletedObjectclass );
        if( $self->_modifyAttrList( $currentObjectclass, $entry, 'objectClass' ) ) {
            $update = 1;
        }

        if( $#deletedObjectclass >= 0 ) {
            # Pour les schémas LDAP supprimés, on détermine les attributs à
            # supprimer.
            # Uniquement ceux qui ne sont pas utilisés par d'autres objets.
            my $deleteAttrs = $self->_diffObjectclassAttrs(\@deletedObjectclass, $currentObjectclass, $objectclassDesc);

            for( my $i=0; $i<=$#$deleteAttrs; $i++ ) {
                if( $self->_modifyAttrList( undef, $entry, $deleteAttrs->[$i] ) ) {
                    $update = 1;
                }
            }
        }

        if( $self->_modifyAttr( $self->{'entityDesc'}->{'mailshare_ldap_mailbox'}, $entry, 'mailbox' ) ) {
            $update = 1;
        }

        if( $self->_modifyAttr( $self->{'entityDesc'}->{'mailshare_description'}, $entry, 'description' ) ) {
            $update = 1;
        }

        if( $self->_modifyAttr( $self->{'entityDesc'}->{'mailshare_mail_server'}, $entry, 'mailBoxServer' ) ) {
            $update = 1;
        }

        if( $self->_modifyAttrList( $self->{'email'}, $entry, 'mail' ) ) {
            $update = 1;
        }

        if( $self->_modifyAttrList( $self->{'emailAlias'}, $entry, 'mailAlias' ) ) {
            $update = 1;
        }

        if( $self->{'entityDesc'}->{'mailshare_mail_perms'} && $self->_modifyAttr( 'PERMIT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }elsif( !$self->{'entityDesc'}->{'mailshare_mail_perms'} && $self->_modifyAttr( 'REJECT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }

        if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
            if( $self->_modifyAttr( $domainName, $entry, 'obmDomain' ) ) {
                $update = 1;
            }
        }
    }

    return $update;
}


# Needed
sub isMailAvailable {
    my $self = shift;

    if( defined($self->{'parent'}) && !$self->{'parent'}->isGlobal() ) {
        return 1;
    }

    return 0;
}


# Needed : cyrusEngine
sub isMailActive {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_mail_perms'};
}


# Needed : cyrusEngine
sub getMailboxName {
    my $self = shift;
    my( $which ) = @_;
    my $mailShareName = undef;

    if( lc($which) =~ /^new$/ ) {
        $mailShareName = $self->{'entityDesc'}->{'mailshare_cyrus_mailbox'};
    }elsif( lc($which) =~ /^current$/ ) {
        $mailShareName = $self->{'entityDesc'}->{'current_mailshare_cyrus_mailbox'};
    }

    return $mailShareName;
}


# Needed : cyrusEngine
sub getMailServerId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_mail_server_id'};
}


# Needed : cyrusEngine
sub getMailboxPrefix {
    my $self = shift;

    return '';
}


# Needed : cyrusEngine
sub getMailboxQuota {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_cyrus_quota'};
}


# Needed : cyrusEngine
sub getMailboxAcl {
    my $self = shift;
    my $mailBoxAcl = undef;

    if( !$self->getArchive() && $self->isMailActive() ) {
        $mailBoxAcl = $self->{'entityDesc'}->{'mailshare_acl'};
    }

    return $mailBoxAcl;
}


# Needed : cyrusEngine
sub getMailboxPartition {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_cyrus_partition'};
}


# Needed : cyrusEngine
sub getMailboxDefaultFolders {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailbox_folders'};
}
