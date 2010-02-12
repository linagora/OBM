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

    $self->{'ldapMappingScope'} = {
        'updateLinks' => []
    };

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'parent'} = undef;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmmailshare when
# adding new value into description
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
    
    $mailshareDesc->{'mailshare_name_new'} = lc($mailshareDesc->{'mailshare_name'});
    if( $mailshareDesc->{'mailshare_name_new'} !~ /$OBM::Parameters::regexp::regexp_mailsharename/ ) {
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
    }else {
        $mailshareDesc->{'mailshare_name_current'} = $mailshareDesc->{'mailshare_name_new'};
    }

    # OBM Domain
    if( defined($self->{'parent'}) ) {
        $mailshareDesc->{'mailshare_obm_domain'} = $self->{'parent'}->getDesc('domain_name');
    }

    SWITCH: {
        require OBM::Cyrus::cyrusServers;
        my $cyrusSrvList = OBM::Cyrus::cyrusServers->instance();
        if( !(my $cyrusHostName = $cyrusSrvList->getCyrusServerIp( $mailshareDesc->{'mailshare_mail_server_id'}, $mailshareDesc->{'mailshare_domain_id'} ) ) ) {
            $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.' annulé, serveur inconnu', 2 );
            $mailshareDesc->{'mailshare_mail_perms'} = 0;
            $mailshareDesc->{'mailshare_mailperms_access'} = 'REJECT';
            last SWITCH;

        }else {
            $mailshareDesc->{'mailshare_mailperms_access'} = 'PERMIT';

            $mailshareDesc->{'mailshare_mail_server'} = 'lmtp:'.$cyrusHostName.':24';
        }

        ( $mailshareDesc->{'mailshare_main_email'}, $mailshareDesc->{'mailshare_alias_email'} ) = $self->_makeEntityEmail( $mailshareDesc->{'mailshare_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
        if( !defined($mailshareDesc->{'mailshare_main_email'}) ) {
            $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.'annulé, pas d\'adresses mails valides', 2 );
            $mailshareDesc->{'mailshare_mail_perms'} = 0;
            delete($mailshareDesc->{'mailshare_main_email'});
            delete($mailshareDesc->{'mailshare_alias_email'});

            last SWITCH;
        }

        $mailshareDesc->{'mailshare_mail_perms'} = 1;
    }

    # LDAP BAL destination
    $mailshareDesc->{'mailshare_ldap_mailbox'} = '+'.$mailshareDesc->{'mailshare_name_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Cyrus BAL destination
    $mailshareDesc->{'mailshare_cyrus_mailbox'} = $mailshareDesc->{'mailshare_name_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Current Cyrus BAL destination
    $mailshareDesc->{'current_mailshare_cyrus_mailbox'} = $mailshareDesc->{'mailshare_name_current'}.'@'.$self->{'parent'}->getDesc('domain_name');

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
                my $folderName = $mailshareDesc->{'mailshare_name_new'};
                foreach my $folder ( split( '/', $folderTree ) ) {
                    $folder =~ s/^\s+//;

                    $folderName .= '/'.$folder;
                    push( @{$mailshareDesc->{mailbox_folders}}, $folderName.'@'.$self->{'parent'}->getDesc('domain_name') );
                }
            }
        }
    }

    $self->{'entityDesc'} = $mailshareDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmmailshare when
# adding new value into description
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

    $entry->add( objectClass => $self->_getLdapObjectclass() );

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();

    my $attrsMapping = $ldapMapping->getAllAttrsMapping( $self );
    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} );
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


    # Vérification des objectclass
    my $deletedObjectclass;
    my $currentObjectclass = $self->_getLdapObjectclass( $entry->get_value('objectClass', asref => 1), \$deletedObjectclass);
    if( $self->_modifyAttr( $currentObjectclass, $entry, 'objectClass' ) ) {
        $update = 1;
    }

    if( $#{$deletedObjectclass} >= 0 ) {
        # Pour les schémas LDAP supprimés, on détermine les attributs à
        # supprimer.
        # Uniquement ceux qui ne sont pas utilisés par d'autres objets.
        my $deleteAttrs = $self->_diffObjectclassAttrs($deletedObjectclass, $currentObjectclass, $objectclassDesc);

        for( my $i=0; $i<=$#$deleteAttrs; $i++ ) {
            if( $self->_modifyAttr( undef, $entry, $deleteAttrs->[$i] ) ) {
                $update = 1;
            }
        }
    }
    

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();

    if( $self->getUpdateEntity() ) {
        my @exceptions;
        push( @exceptions, @{$self->{'ldapMappingScope'}->{'updateLinks'}} );
        my $attrsMapping = $ldapMapping->getAllAttrsMapping( $self, \@exceptions );

        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            if( $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
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


sub smtpInUpdateMap {
    my $self = shift;

    # If entity is not updated (but only links)
    if( !$self->getUpdateEntity() ) {
        return 0;
    }

    return 1;
}
