package OBM::Entities::obmGroup;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

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

    $self->{'ldapMappingScope'} = {
        'updateLinks' => [
            'group_users',
            'group_mailboxes',
            'group_samba_users'
        ]
    };

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
        $self->_log( 'ID du groupe non défini', 0 );
        return 1;
    }elsif( $groupDesc->{'group_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$groupDesc->{'group_id'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom du groupe
    if( !defined($groupDesc->{'group_name'}) ) {
        $self->_log( 'Nom du groupe non défini', 0 );
        return 1;
    }
    
    $groupDesc->{'group_name_new'} = $groupDesc->{'group_name'};
    if( $groupDesc->{'group_name_new'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
        $self->_log( 'Nom du groupe \''.$groupDesc->{'group_name'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom actuel du groupe, si définit
    if( defined($groupDesc->{'group_name_current'}) ) {
        $groupDesc->{'group_name_current'} = $groupDesc->{'group_name_current'};
        if( !$groupDesc->{'group_name_current'} || $groupDesc->{'group_name_current'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
            $self->_log( 'Nom actuel du groupe \''.$groupDesc->{'group_name_current'}.'\' incorrect', 4 );
            return 1;
        }
    }else {
        $groupDesc->{'group_name_current'} = $groupDesc->{'group_name_new'};
    }

    # Le GID du groupe
    if( !defined($groupDesc->{'group_gid'}) ) {
        $self->_log( 'GID du groupe \''.$groupDesc->{'group_name'}.'\' non défini', 0 );
        return 1;
    }elsif( $groupDesc->{'group_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID \''.$groupDesc->{'group_gid'}.'\' incorrect', 0 );
        return 1;
    }

    # Le droit de messagerie
    ( $groupDesc->{'group_main_email'}, $groupDesc->{'group_alias_email'} ) = $self->_makeEntityEmail( $groupDesc->{'group_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
    if( !defined($groupDesc->{'group_main_email'}) ) {
        $groupDesc->{'group_mailperms_access'} = 'REJECT';
        delete($groupDesc->{'group_main_email'});
        delete($groupDesc->{'group_alias_email'});
        $groupDesc->{'group_mailperms'} = 0;
    }else {
        $groupDesc->{'group_mailperms_access'} = 'PERMIT';
        $groupDesc->{'group_mailperms'} = 1;
    }

    # External contacts
    if( $groupDesc->{'group_contacts'} ) {
        my @externelContacts = split( /\r\n/, $groupDesc->{'group_contacts'} );
        my %externelContacts;
        my $return = 0;
        for( my $i=0; $i<=$#externelContacts; $i++ ) {
            my $lcExternalContact = lc($externelContacts[$i]);
            if( $lcExternalContact =~ /$OBM::Parameters::regexp::regexp_email/ ) {
                $externelContacts{$lcExternalContact} = undef;
            }else {
                $self->_log( 'adresse mail du contact externe \''.$externelContacts[$i].'\' incorrecte', 2 );
                $return++;
            }
        }

        if( $return ) {
            $self->_log( $return.' contacts externes ont une adresse mail invalide', 0 );
            return 1;
        }
        @{$groupDesc->{'group_contacts_list'}} = keys(%externelContacts);

        if( $#{$groupDesc->{'group_contacts_list'}} < 0 ) {
            delete($groupDesc->{'group_contacts_list'});
        }
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
        $groupDesc->{'group_samba_name'} = $groupDesc->{'group_name_new'};
    }else {
        $groupDesc->{'group_samba'} = 0;
    }

    # OBM Domain
    if( defined($self->{'parent'}) ) {
        $groupDesc->{'group_obm_domain'} = $self->{'parent'}->getDesc('domain_name');
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
            if( $current->{'userobm_mail_perms'} ) {
                push( @{$self->{'entityDesc'}->{'group_mailboxes'}}, $current->{'userobm_login'}.'@'.$self->{'parent'}->getDesc('domain_name') );
            }
        }

        if( $self->{'entityDesc'}->{'group_samba'} ) {
            if( $current->{'userobm_samba_perms'} ) {
                push( @{$self->{'entityDesc'}->{'group_samba_users'}}, $self->_getUserSID( $self->{'parent'}->getDesc('samba_sid'), $current->{'userobm_uid'} ) );
            }
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

    if( $self->getUpdateLinks() ) {
        my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateLinks'} );
        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            if( $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
                $update = 1;
            }
        }
    }

    return $update;
}


sub updateLinkedEntities {
    my $self = shift;
    my( $updateType ) = @_;

    if( $self->{'entityDesc'}->{'group_contacts'} ne $self->{'entityDesc'}->{'group_contacts_current'} ) {
        $self->_log( 'changement des contacts externes de '.$self->getDescription().', les groupes parents doivent être mis à jour', 3 );
        return 1;
    }

    if( ($updateType =~ /^(UPDATE_ALL|UPDATE_LINKS)$/) && ($self->{'entityDesc'}->{'group_gid'} == 512) ) {
        $self->_log( 'les membres de '.$self->getDescription().' ont été mis à jour, ils doivent être mis à jour', 3 );
        return 1;
    }

    $self->_log( 'pas de mise à jour des entités liés nécessaire pour '.$self->getDescription(), 3 );
    return 0;
}


sub setRemovedMembers {
    my $self = shift;
    my( $removedMembers ) = @_;

    if( ref($removedMembers) ne 'ARRAY' ) {
        $self->_log( 'liste des utilisateurs supprimés incorrecte', 3 );
        $self->{'entityDesc'}->{'group_removed_users_id'} = [];
        return 0;
    }

    $self->{'entityDesc'}->{'group_removed_users_id'} = $removedMembers;
    return 0;
}


sub getRemovedMembersId {
    my $self = shift;

    return $self->{'entityDesc'}->{'group_removed_users_id'};
}


sub smtpInUpdateMap {
    my $self = shift;

    return 1;
}
