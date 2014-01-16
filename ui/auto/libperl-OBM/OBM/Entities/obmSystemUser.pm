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


package OBM::Entities::obmSystemUser;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

require OBM::Parameters::regexp;


# Needed
sub new {
    my $class = shift;
    my( $parent, $systemUserDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 1 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $systemUserDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation de l\'utilisateur système', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'person', 'posixAccount', 'obmSystemUser' ];

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'parent'} = undef;
}


# Needed
sub _init {
    my $self = shift;
    my( $systemUserDesc ) = @_;

    if( !defined($systemUserDesc) || (ref($systemUserDesc) ne 'HASH') ) {
        $self->_log( 'description de l\'utilisateur système incorrecte', 1 );
        return 1;
    }

    # L'Id du l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_id'}) ) {
        $self->_log( 'ID de l\'utilisateur système non défini', 1 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$systemUserDesc->{'usersystem_id'}.'\' incorrect', 1 );
        return 1;
    }

    # Le login de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_login'}) ) {
        $self->_log( 'Login de l\'utilisateur système non défini', 1 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_login'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
        $self->_log( 'Login \''.$systemUserDesc->{'usersystem_login'}.'\' incorrect', 1 );
        return 1;
    }

    # L'UID de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_uid'}) ) {
        $self->_log( 'UID de l\'utilisateur système non défini', 1 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_uid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'UID \''.$systemUserDesc->{'usersystem_uid'}.'\' incorrect', 1 );
        return 1;
    }

    # Le GID de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_gid'}) ) {
        $self->_log( 'GID de l\'utilisateur système non défini', 1 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID \''.$systemUserDesc->{'usersystem_gid'}.'\' incorrect', 1 );
        return 1;
    }

    $self->{'entityDesc'} = $systemUserDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 3 );

    return 0;
}


sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $userSystemDesc = $self->{'entityDesc'};

    my $description = 'utilisateur système d\'ID \''.$userSystemDesc->{'usersystem_id'}.'\', login \''.$userSystemDesc->{'usersystem_login'}.'\'';

    if( defined($userSystemDesc->{'usersystem_lastname'}) ) {
        $description .= ' '.$userSystemDesc->{'usersystem_lastname'};
    }

    if( defined($userSystemDesc->{'usersystem_firstname'}) ) {
        $description .= ' '.$userSystemDesc->{'usersystem_firstname'};
    }

    return $description;
}


# Needed
sub getDomainId {
    my $self = shift;

    my $parent = $self->getParent();

    return $parent->getId();
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'usersystem_id'};
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


# Needed by : LdapEngine
sub getDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 1 );
        return undef;
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'uid='.$self->{'entityDesc'}->{'usersystem_login'}.','.$rootDn->[$i] );
        $self->_log( 'nouveau DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


# Needed by : LdapEngine
sub getCurrentDnPrefix {
    my $self = shift;

    return $self->getDnPrefix();
}


# Needed by : LdapEngine
sub createLdapEntry {
    my $self = shift;
    my( $entryDn, $entry ) = @_;

    if( !$entryDn ) {
        $self->_log( 'DN non défini', 1 );
        return 1;
    }

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 1 );
        return 1;
    }

    if( !$self->{'entityDesc'}->{'usersystem_password'} ) {
        $self->_log( 'pas de mot de passe défini', 1 );
        return 1;
    }

    my $cn = $self->{'entityDesc'}->{'usersystem_login'};
    SWITCH: {
        if( $self->{'entityDesc'}->{'usersystem_firstname'} && $self->{'entityDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_firstname'}.' '.$self->{'entityDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }

        if( $self->{'entityDesc'}->{'usersystem_firstname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_firstname'};
            last SWITCH;
        }

        if( $self->{'entityDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }
    }

    my $sn = $self->{'entityDesc'}->{'usersystem_login'};
    if( $self->{'entityDesc'}->{'usersystem_lastname'} ) {
        $sn = $self->{'entityDesc'}->{'usersystem_lastname'};
    }

    my $homeDirectory = $self->{'entityDesc'}->{'usersystem_homedir'};
    if( !$homeDirectory ) {
        $homeDirectory = '/home/'.$self->{'entityDesc'}->{'usersystem_login'};
    }

    $entry->add(
        objectClass => $self->{'objectclass'},
        uid => $self->{'entityDesc'}->{'usersystem_login'},
        uidNumber => $self->{'entityDesc'}->{'usersystem_uid'},
        gidNumber => $self->{'entityDesc'}->{'usersystem_gid'},
        homeDirectory => $homeDirectory,
        cn => $cn,
        sn => $sn,
        userpassword => $self->{'entityDesc'}->{'usersystem_password'},
        loginShell => '/bin/bash'
    );

    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
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

    if( $self->{'entityDesc'}->{'usersystem_password'} ) {
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'usersystem_password'}, $entry, 'userpassword' ) ) {
            $update = 1;
        }
    }

    my $cn = $self->{'entityDesc'}->{'usersystem_login'};
    SWITCH: {
        if( $self->{'entityDesc'}->{'usersystem_firstname'} && $self->{'entityDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_firstname'}.' '.$self->{'entityDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }

        if( $self->{'entityDesc'}->{'usersystem_firstname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_firstname'};
            last SWITCH;
        }

        if( $self->{'entityDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'entityDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }
    }
    if( $self->_modifyAttr( $cn, $entry, 'cn' ) ) {
        $update = 1;
    }

    my $sn = $self->{'entityDesc'}->{'usersystem_login'};
    if( $self->{'entityDesc'}->{'usersystem_lastname'} ) {
        $sn = $self->{'entityDesc'}->{'usersystem_lastname'};
    }
    if( $self->_modifyAttr( $sn, $entry, 'sn' ) ) {
        $update = 1;
    }

    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        if( $self->_modifyAttr( $domainName, $entry, 'obmDomain' ) ) {
            $update = 1;
        }
    }


    return $update;
}
