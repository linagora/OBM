package OBM::Entities::obmGroup;

$VERSION = '1.0';

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::Entities::commonEntities qw(
        setDelete
        getDelete
        getArchive
        setArchive
        getParent
        setUpdated
        unsetUpdated
        getUpdated
        getDesc
        _makeEntityEmail
        setUpdateEntity
        getUpdateEntity
        setUpdateLinks
        getUpdateLinks
        isMailAvailable
        isSieveAvailable
        );
use OBM::Ldap::utils qw(
        _modifyAttr
        _modifyAttrList
        _diffObjectclassAttrs
        );
use OBM::Samba::utils qw(
        _getUserSID
        _getGroupSID
        );
use OBM::Parameters::common;


# Needed
sub new {
    my $class = shift;
    my( $parent, $groupDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 3 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $groupDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du groupe', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'posixGroup', 'obmGroup', 'sambaGroupMapping' ];

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'parent'} = undef;
}


# Needed
sub _init {
    my $self = shift;
    my( $groupDesc ) = @_;

    if( !defined($groupDesc) || (ref($groupDesc) ne 'HASH') ) {
        $self->_log( 'description du groupe incorrect', 4 );
        return 1;
    }

    # L'ID du groupe
    if( !defined($groupDesc->{'group_id'}) ) {
        $self->_log( 'ID du groupe non défini', 3 );
        return 1;
    }elsif( $groupDesc->{'group_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$groupDesc->{'group_id'}.'\' incorrect', 4 );
        return 1;
    }

    # Le nom du groupe
    if( !defined($groupDesc->{'group_name'}) ) {
        $self->_log( 'Nom du groupe non défini', 3 );
        return 1;
    }elsif( $groupDesc->{'group_name'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
        $self->_log( 'Nom du groupe \''.$groupDesc->{'group_name'}.'\' incorrect', 4 );
        return 1;
    }

    # Le GID du groupe
    if( !defined($groupDesc->{'group_gid'}) ) {
        $self->_log( 'GID du groupe non défini', 3 );
        return 1;
    }elsif( $groupDesc->{'group_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID \''.$groupDesc->{'group_gid'}.'\' incorrect', 4 );
        return 1;
    }

    # Le nom actuel du groupe, si définit
    if( $groupDesc->{'group_name_current'} && $groupDesc->{'group_name_current'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
        $self->_log( 'Nom actuel du groupe \''.$groupDesc->{'group_name_current'}.'\' incorrect', 4 );
        return 1;
    }

    # Le droit de messagerie
    if( !$self->_makeEntityEmail( $groupDesc->{'group_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') ) ) {
        $groupDesc->{'group_mailperms'} = 0;
    }else {
        $groupDesc->{'group_mailperms'} = 1;
    }

    # Le SID du domaine
    my $domainSid = $self->{'parent'}->getDesc('samba_sid');
    if( !$domainSid ) {
        $self->_log( 'pas de SID associé au domaine '.$self->{'parent'}->getDescription(), 3 );
        $self->_log( 'droit samba annulé', 2 );
        $groupDesc->{'group_samba'} = 0;
    }

    # Les informations Samba
    if( $OBM::Parameters::common::obmModules->{'samba'} && $groupDesc->{'group_samba'} ) {
        $groupDesc->{'group_samba_sid'} = $self->_getGroupSID( $domainSid, $groupDesc->{'group_gid'} );
        $groupDesc->{'group_samba_type'} = 2;
        $groupDesc->{'group_samba_name'} = $groupDesc->{'group_name'};
    }else {
        $groupDesc->{'group_samba'} = 0;
    }

    $self->{'entityDesc'} = $groupDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'ARRAY' ) {
        $self->_log( 'pas de liens définis', 3 );
        return 0;
    }

    for( my $i=0; $i<=$#{$links}; $i++ ) {
        my $current = $links->[$i];

        push( @{$self->{'entityDesc'}->{'group_users'}}, $current->{'userobm_login'} );

        if( $self->{'entityDesc'}->{'group_mailperms'} ) {
            push( @{$self->{'entityDesc'}->{'group_mailboxes'}}, $current->{'userobm_login'}.'@'.$self->{'parent'}->getDesc('domain_name') );
        }

        if( $self->{'entityDesc'}->{'group_samba'} ) {
            push( @{$self->{'entityDesc'}->{'group_samba_users'}}, $self->_getUserSID( $self->{'parent'}->getDesc('samba_sid'), $current->{'userobm_uid'} ) );
        }
    }

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $groupDesc = $self->{'entityDesc'};

    my $description = 'groupe d\'ID \''.$groupDesc->{'group_id'}.'\', nom \''.$groupDesc->{'group_name'}.'\'';

    return $description;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->{'entityDesc'}->{'group_domain_id'};
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'group_id'};
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
        push( @dnPrefixes, 'cn='.$self->{'entityDesc'}->{'group_name'}.','.$rootDn->[$i] );
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

    my $currentGroupName = $self->{'entityDesc'}->{'group_name_current'};
    if( !$currentGroupName ) {
        $currentGroupName = $self->{'entityDesc'}->{'group_name'};
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'cn='.$currentGroupName.','.$rootDn->[$i] );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub _getLdapObjectclass {
    my $self = shift;
    my ($objectclass, $deletedObjectclass) = @_;
    my %realObjectClass;

    if( !defined($objectclass) || (ref($objectclass) ne "ARRAY") ) {
        $objectclass = $self->{'objectclass'};
    }

    for( my $i=0; $i<=$#$objectclass; $i++ ) {
        if( (lc($objectclass->[$i]) eq "sambagroupmapping") && !$self->{'entityDesc'}->{'group_samba'} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $self->{'entityDesc'}->{'group_samba'} ) {
        $realObjectClass{sambaGroupMapping} = 1;
    }

    my @realObjectClass = keys(%realObjectClass);
    return \@realObjectClass;
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
        cn => $self->{'entityDesc'}->{'group_name'},
        gidNumber => $self->{'entityDesc'}->{'group_gid'}
    );

    # La description
    if( $self->{'entityDesc'}->{'group_desc'} ) {
        $entry->add( description => $self->{'entityDesc'}->{'group_desc'} );
    }

    # L'accès mail
    if( $self->{'entityDesc'}->{'group_mailperms'} ) {
        $entry->add( mailAccess => 'PERMIT' );
    }else {
        $entry->add( mailAccess => 'REJECT' );
    }

    # Les adresses mails
    if( $self->{'email'} ) {
        $entry->add( mail => $self->{'email'} );
    }

    # Les adresses mails secondaires
    if( $self->{'emailAlias'} ) {
        $entry->add( mailAlias => $self->{'emailAlias'} );
    }

    # Le domaine OBM
    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
    }

    # Le SID du groupe
    if( $self->{'entityDesc'}->{'group_samba_sid'} ) {
        $entry->add( sambaSID => $self->{'entityDesc'}->{'group_samba_sid'} );
    }

    # Le type du groupe
    if( $self->{'entityDesc'}->{'group_samba_type'} ) {
        $entry->add( sambaGroupType => $self->{'entityDesc'}->{'group_samba_type'} );
    }

    # Le nom du groupe
    if( $self->{'entityDesc'}->{'group_samba_name'} ) {
        $entry->add( displayName => $self->{'entityDesc'}->{'group_samba_name'} );
    }

    # Les membres
    if( $self->{'entityDesc'}->{'group_users'} ) {
        $entry->add( memberUid => $self->{'entityDesc'}->{'group_users'} );
    }

    # Les contacts
    if( $self->{'entityDesc'}->{'group_mailboxes'} ) {
        $entry->add( mailBox => $self->{'entityDesc'}->{'group_mailboxes'} );
    }

    # La liste des utilisateurs Samba
    if( $self->{'entityDesc'}->{'group_samba_users'} ) {
        $entry->add( sambaSIDList => $self->{'entityDesc'}->{'group_samba_users'} );
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
    
    
        # Le GID
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'group_gid'}, $entry, 'gidNumber' ) ) {
            $update = 1;
        }
    
        # La description
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'group_desc'}, $entry, 'description' ) ) {
            $update = 1;
        }
    
        # L'acces au mail
        if( $self->{'entityDesc'}->{'group_mailperms'}  && $self->_modifyAttr( 'PERMIT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }elsif( !$self->{'entityDesc'}->{'group_mailperms'} && $self->_modifyAttr( 'REJECT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }
    
        # Les mails
        if( $self->_modifyAttrList( $self->{'email'}, $entry, 'mail' ) ) {
            $update = 1;
        }
    
        # Les alias mails
        if( $self->_modifyAttrList( $self->{'emailAlias'}, $entry, 'mailAlias' ) ) {
            $update = 1;
        }
    
        # Le domaine
        if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
            if( $self->_modifyAttr( $domainName, $entry, 'obmDomain' ) ) {
                $update = 1;
            }
        }
    
        # Le SID du groupe
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'group_samba_sid'}, $entry, 'sambaSID' ) ) {
            $update = 1;
        }
    
        # Le type du groupe
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'group_samba_type'}, $entry, 'sambaGroupType' ) ) {
            $update = 1;
        }
    
        # Le nom du groupe
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'group_samba_name'}, $entry, 'displayName' ) ) {
            $update = 1;
        }
    }

    if( $self->getUpdateLinks() ) {
        # Les membres du groupe
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'group_users'}, $entry, 'memberUid' ) ) {
            $update = 1;
        }

        # Le cas des contacts
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'group_mailboxes'}, $entry, 'mailBox' ) ) {
            $update = 1;
        }

        # La liste des utilisateurs Samba
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'group_samba_users'}, $entry, 'sambaSIDList' ) ) {
            $update = 1;
        }
    }

    return $update;
}
