#################################################################################
# Copyright (C) 2011-2014 Linagora
#
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
#################################################################################


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
        $self->_log( 'domaine père incorrect', 1 );
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
            'group_users_dn',
            'group_mailboxes',
            'group_samba_users',
            'group_contacts_list'
        ]
    };

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'parent'} = undef;
}


# Needed
# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmgroup when
# adding new value into description
sub _init {
    my $self = shift;
    my( $groupDesc ) = @_;

    if( !defined($groupDesc) || (ref($groupDesc) ne 'HASH') ) {
        $self->_log( 'description du groupe incorrect', 1 );
        return 1;
    }

    # L'ID du groupe
    if( !defined($groupDesc->{'group_id'}) ) {
        $self->_log( 'ID du groupe non défini', 1 );
        return 1;
    }elsif( $groupDesc->{'group_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$groupDesc->{'group_id'}.'\' incorrect', 1 );
        return 1;
    }

    # Le nom du groupe
    if( !defined($groupDesc->{'group_name'}) ) {
        $self->_log( 'Nom du groupe non défini', 1 );
        return 1;
    }
    
    $groupDesc->{'group_name_new'} = $groupDesc->{'group_name'};
    if( $groupDesc->{'group_name_new'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
        $self->_log( 'Nom du groupe \''.$groupDesc->{'group_name'}.'\' incorrect', 1 );
        return 1;
    }

    # Le nom actuel du groupe, si définit
    if( defined($groupDesc->{'group_name_current'}) ) {
        $groupDesc->{'group_name_current'} = $groupDesc->{'group_name_current'};
        if( !$groupDesc->{'group_name_current'} || $groupDesc->{'group_name_current'} !~ /$OBM::Parameters::regexp::regexp_groupname/ ) {
            $self->_log( 'Nom actuel du groupe \''.$groupDesc->{'group_name_current'}.'\' incorrect', 1 );
            return 1;
        }
    }else {
        $groupDesc->{'group_name_current'} = $groupDesc->{'group_name_new'};
    }

    # Le GID du groupe
    if( !defined($groupDesc->{'group_gid'}) ) {
        $self->_log( 'GID du groupe \''.$groupDesc->{'group_name'}.'\' non défini', 1 );
        return 1;
    }elsif( $groupDesc->{'group_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID \''.$groupDesc->{'group_gid'}.'\' incorrect', 1 );
        return 1;
    }

    # Le droit de messagerie
    ( $groupDesc->{'group_main_email'}, $groupDesc->{'group_alias_email'} ) = $self->_makeEntityEmail( $groupDesc->{'group_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
    if( !defined($groupDesc->{'group_main_email'}) ) {
        if( $groupDesc->{'group_mailperms'} ) {
            $self->_log( 'droit mail du groupe \''.$groupDesc->{'group_name'}.'\' annulé, pas d\'adresses mails valides', 2 );
        }
        $groupDesc->{'group_mailperms_access'} = 'REJECT';
        delete($groupDesc->{'group_main_email'});
        delete($groupDesc->{'group_alias_email'});
        $groupDesc->{'group_mailperms'} = 0;
    }else {
        $groupDesc->{'group_mailperms_access'} = 'PERMIT';
        $groupDesc->{'group_mailperms'} = 1;
    }

    # Is mail right was updated ?
    my $groupMailpermCurrent = 0;
    my($currentGroupMainEmail, $currentGroupAliasEmail) = $self->_makeEntityEmail($groupDesc->{'group_email_current'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
    if(defined($currentGroupMainEmail)) {
        $groupMailpermCurrent = 1;
    }

    if($groupDesc->{'group_mailperms'} != $groupMailpermCurrent) {
        $self->setForceLoadEntityLinks();
    }

    # Le SID du domaine
    my $domainSid = $self->{'parent'}->getDesc('samba_sid');
    if( !$domainSid && $groupDesc->{'group_samba'} ) {
        $self->_log( 'pas de SID associé au domaine '.$self->{'parent'}->getDescription(), 1 );
        $self->_log( 'droit samba annulé', 3 );
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

    $self->_log( 'chargement : '.$self->getDescription(), 3 );

    return 0;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmgroup when
# adding new value into description
sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'HASH' ) {
        $self->_log( 'pas de liens définis', 1 );
        return 0;
    }

    if( ref($links->{'members'}) eq 'ARRAY' ) {
        my $members = $links->{'members'};
    
        for( my $i=0; $i<=$#{$members}; $i++ ) {
            my $current = $members->[$i];
    
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

        if( !$self->_getGroupUsersDn() ) {
            return 1;
        }
    
    }

    if( ref($links->{'contacts'}) eq 'ARRAY' ) {
        my $contacts = $links->{'contacts'};

        for( my $i=0; $i<=$#{$contacts}; $i++ ) {
            push( @{$self->{'entityDesc'}->{'group_contacts_list'}}, $contacts->[$i]->{'contact_email_address'} );
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
        $self->_log( 'description du domaine parent incorrecte', 1 );
        return 1;
    }

    $self->{'parent'} = $parent;

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
        # Don't update RDN attribute. Done by ldapEngine.
        my $attrsMapping = $ldapMapping->getAllAttrsMapping( $self, \@exceptions, 1 );

        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            my $ldapValue = $self->getDesc($attrsMapping->[$i]->{'desc'}->{'name'});
            if(!defined($ldapValue) && defined($attrsMapping->[$i]->{'desc'}->{'default'})) {
                $ldapValue = $attrsMapping->[$i]->{'desc'}->{'default'};
            }

            if( $self->_modifyAttr($ldapValue, $entry, $attrsMapping->[$i]->{'ldap'}->{'name'}) ) {
                $update = 1;
            }
        }
    }

    if( $self->getUpdateLinks() ) {
        my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateLinks'} );
        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            my $ldapValue = $self->getDesc($attrsMapping->[$i]->{'desc'}->{'name'});
            if(!defined($ldapValue) && defined($attrsMapping->[$i]->{'desc'}->{'default'})) {
                $ldapValue = $attrsMapping->[$i]->{'desc'}->{'default'};
            }

            if( $self->_modifyAttr($ldapValue, $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
                $update = 1;
            }
        }
    }

    return $update;
}


sub updateLinkedEntities {
    my $self = shift;
    my( $updateType ) = @_;

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
        $self->_log( 'liste des utilisateurs supprimés incorrecte', 1 );
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


sub _getGroupUsersDn {
    my $self = shift;

    if( !defined($self->{'entityDesc'}->{'group_users'}) ) {
        $self->_log( 'Pas de membres dans le groupe '.$self->getDescription(), 4 );
        return 1;
    }

    if( ref($self->{'entityDesc'}->{'group_users'}) ne 'ARRAY' ) {
        $self->_log( 'Listes des membres du groupe invalide', 0 );
        return 0;
    }

    my $userRootDn = $self->{'parent'}->getDnPrefix( 'OBM::Entities::obmUser' );
    if( !defined($userRootDn) || !defined($userRootDn->[0]) ) {
        $self->_log( 'Pas de root DN associé aux entités de type \'OBM::Entities::obmUser\'', 0 );
        return 0;
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $rdnMapping = $ldapMapping->getRdn('OBM::Entities::obmUser');
    if( !defined($rdnMapping) ) {
        $self->_log( 'mapping du RDN de l\'entité '.$self->getDescription().' incorrect', 0 );
        return 0;
    }

    for( my $i=0; $i<=$#{$self->{'entityDesc'}->{'group_users'}}; $i++ ) {
        my $userLogin = $self->{'entityDesc'}->{'group_users'}->[$i];

        push( @{$self->{'entityDesc'}->{'group_users_dn'}}, $rdnMapping->{'ldap'}->{'name'}.'='.$userLogin.','.$userRootDn->[0] );
    }

    return 1;
}
