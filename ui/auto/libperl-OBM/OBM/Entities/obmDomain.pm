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


package OBM::Entities::obmDomain;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require OBM::Parameters::regexp;


sub new {
    my $class = shift;
    my( $domainDesc ) = @_;

    my $self = bless { }, $class;

    if( $self->_init( $domainDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du domaine', 1 );
        return undef;
    }

    $self->{'parent'} = undef;

    $self->{'ldapBranchDesc'} = {
        'sysusers' => 'System users',
        'hosts' => 'Hosts description',
        'users' => 'Users account',
        'groups' => 'System Groups',
        'mailShare' => 'Share Directory',
        'servicesConfiguration' => 'Services configuration',
        'contacts' => 'Publics contacts'
    };

    require OBM::Parameters::common;
    $self->{'ldapServerId'} = $OBM::Parameters::common::ldapServerId;
    $self->{'objectclass'} = [ 'dcObject', 'organization' ];

    return $self;
}


# Needed
sub _init {
    my $self = shift;
    my( $domainDesc ) = @_;

    if( !defined($domainDesc) || (ref($domainDesc) ne 'HASH') ) {
        $self->_log( 'description de domaine incorrecte', 1 );
        return 1;
    }

    # L'Id du domaine
    if( !defined($domainDesc->{'domain_id'}) ) {
        $self->_log( 'ID du domaine non défini', 1 );
        return 1;
    }elsif( $domainDesc->{'domain_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainDesc->{'domain_id'}.'\' incorrect', 1 );
        return 1;
    }

    # Le nom du domaine
    if( !defined($domainDesc->{'domain_name'}) ) {
        $self->_log( 'nom de domaine principal de messagerie non défini', 1 );
        return 1;
    }elsif( $domainDesc->{'domain_name'} !~ /$OBM::Parameters::regexp::regexp_domain/ ) {
        $self->_log( 'nom de domaine \''.$domainDesc->{'domain_name'}.'\' incorrect', 1 );
        return 1;
    }

    # Les alias du domaine
    my $domainAlias = $domainDesc->{'domain_alias'};
    $domainDesc->{'domain_alias'} = [];
    if( $domainAlias ) {
        my @aliases = split( /\r\n/, $domainAlias );
        for( my $i=0; $i<=$#aliases; $i++ ) {
            my $alias = $aliases[$i];
        	
            if( $alias !~ /$OBM::Parameters::regexp::regexp_domain/ ) {
                $self->_log( 'alias de domaine \''.$alias.'\' incorrect', 1 );
            } elsif ( grep(/^$alias$/, @{$domainDesc->{'domain_alias'}}) ) {
            	$self->_log( 'l\' alias de domaine \'' . $alias . '\' existe déjà, le doublon sera ignoré', 3 );
            } else {
                push( @{$domainDesc->{'domain_alias'}}, $alias );
            }
        }
    }

    # Est-il Global ?
    $self->{'global'} = $domainDesc->{'domain_global'};

    $self->{'entityDesc'} = $domainDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 4 );

    return 0;
}


sub getDescription {
    my $self = shift;
    my $domaindesc = $self->{'entityDesc'};
    
    my $description = 'domaine d\'ID \''.$self->getId().'\'';

    if( $domaindesc->{'domain_label'} ) {
        $description .= ', \''.$domaindesc->{'domain_label'}.'\'';
    }

    return $description;
}


sub getDomainId {
    my $self = shift;

    return $self->getId();
}


sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'domain_id'};
}


sub getLdapServerId {
    my $self = shift;

    return $self->{'ldapServerId'};
}


sub isGlobal {
    my $self = shift;

    return $self->{'global'};
}


# Needed by : LdapEngine
sub _getParentDn {
    my $self = shift;
    my $parentDn = '';

    if( defined($self->{'parent'}) ) {
        $parentDn = $self->{'parent'}->_getRootDnPrefix();
    }

    return $parentDn;
}


sub _getRootDnPrefix {
    my $self = shift;
    my $rootDnPrefix = undef;

    $rootDnPrefix = $self->_getParentDn();

    if( $self->isGlobal() ) {
        require OBM::Parameters::common;
        if( !$OBM::Parameters::common::ldapRoot ) {
            $self->( 'DIT de l\'annuaire LDAP non défini', 1 );
            return undef;
        }
    
        my @root = split( /,/, $OBM::Parameters::common::ldapRoot );
        while( my $part = pop(@root) ) {
            if( $rootDnPrefix ) {
                $rootDnPrefix = ','.$rootDnPrefix;
            }

            $rootDnPrefix = 'dc='.$part.$rootDnPrefix;
        }
    }else {
        $rootDnPrefix = 'dc='.$self->{'entityDesc'}->{'domain_name'}.','.$self->_getParentDn();
    }

    return $rootDnPrefix;
}


# Needed by : LdapEngine
sub getDnPrefix {
    my $self = shift;
    my( $entity ) = @_;
    my $rootDn;
    my @dnPrefixes;

    my $entityName = $entity;
    if( ref($entity) ) {
        $entityName = ref($entity);
    }

    if( !($rootDn = $self->_getRootDnPrefix()) ) {
        $self->_log( 'DN de la racine du domaine non déterminée', 1 );
        return undef;
    }

    if( !defined( $entity ) ) {
        push( @dnPrefixes, $rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( !defined( $entity ) || $entityName eq 'OBM::Entities::obmSystemUser' ) {
        push( @dnPrefixes, 'ou=sysusers,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( !defined( $entity ) || $entityName eq 'OBM::Entities::obmHost' ) {
        push( @dnPrefixes, 'ou=hosts,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmUser') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=users,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmGroup') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=groups,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmMailshare') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=mailShare,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmMailServer') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=servicesConfiguration,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmObmSettings') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=servicesConfiguration,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmObmBackup') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=servicesConfiguration,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || $entityName eq 'OBM::Entities::obmContact') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=contacts,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmContactService') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=servicesConfiguration,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    return \@dnPrefixes;
}


# Needed by : LdapEngine
sub getCurrentDnPrefix {
    my $self = shift;
    my( $entity ) = @_;

    return $self->getDnPrefix( $entity );
}


# Needed by : LdapEngine
sub createLdapEntry {
    my $self = shift;
    my( $entryDn, $entry ) = @_;

    if( !$entryDn ) {
        return 1;
    }

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        return 1;
    }

    my $ldapBranchDesc = $self->{'ldapBranchDesc'};

    # Analyse du DN
    my @dn = split( ',', $entryDn );
    for( my $i=0; $i<=$#dn; $i++ ) {
        my @dnSplit = split( '=', $dn[$i] );
        $dn[$i] = \@dnSplit;
    }

    SWITCH: {
        if( lc($dn[0]->[0]) eq 'dc' ) {
            $entry->add(
                objectClass => $self->{'objectclass'},
                dc => $dn[0]->[1],
                o => $self->{'entityDesc'}->{'domain_label'}
            );

            if( $self->{'entityDesc'}->{'domain_description'} ) {
                $entry->add( description => $self->{'entityDesc'}->{'domain_description'} );
            }

            last SWITCH;
        }

        if( lc($dn[0]->[0]) eq 'ou' ) {
            $entry->add(
                objectClass => [ 'organizationalUnit' ],
                ou => $dn[0]->[1]
            );

            if( $ldapBranchDesc->{$dn[0]->[1]} ) {
                $entry->add( description => $ldapBranchDesc->{$dn[0]->[1]} );
            }

            last SWITCH;
        }

        return 1;
    }

    return 0;
}


# Needed by : LdapEngine
sub updateLdapEntry {
    my $self = shift;
    my( $entry, $objectclassDesc ) = @_;
    my $update = 0;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        return $update;
    }

    my $ldapBranchDesc = $self->{'ldapBranchDesc'};

    # Analyse du DN
    my @dn = split( ',', $entry->dn() );
    for( my $i=0; $i<=$#dn; $i++ ) {
        my @dnSplit = split( '=', $dn[$i] );
        $dn[$i] = \@dnSplit;
    }

    SWITCH: {
        if( lc($dn[0]->[0]) eq 'dc' ) {
            if( $self->_modifyAttr( $self->{'entityDesc'}->{'domain_label'}, $entry, 'o' ) ) {
                $update = 1;
            }

            if( $self->_modifyAttr( $self->{'entityDesc'}->{'domain_description'}, $entry, 'description' ) ) {
                $update = 1;
            }
            last SWITCH;
        }

        if( lc($dn[0]->[0]) eq 'ou' ) {
            if( $self->_modifyAttr( $ldapBranchDesc->{$dn[0]->[1]}, $entry, 'description' ) ) {
                $update = 1;
            }
            last SWITCH;
        }

        return 0;
    }
    
    return $update;
}

# Needed by : hostFactory
sub isSambaDomain {
	my $self = shift;

	if ( defined($self->{'entityDesc'}->{'samba_sid'}) ){
		return 1;
	}else{
		return 0;
	}
}
