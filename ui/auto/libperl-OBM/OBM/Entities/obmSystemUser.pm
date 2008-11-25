package OBM::Entities::obmSystemUser;

$VERSION = "1.0";

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
        getUpdated
        isMailAvailable
        isSieveAvailable
        );
use OBM::Ldap::utils qw(
        _modifyAttr
        _modifyAttrList
        _diffObjectclassAttrs
        );
use OBM::Tools::passwd qw(
        _toMd5
        _toSsha
        _convertPasswd
        );
require OBM::Parameters::regexp;


# Needed
sub new {
    my $class = shift;
    my( $parent, $systemUserDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 3 );
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

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'parent'} = undef;
}


# Needed
sub _init {
    my $self = shift;
    my( $systemUserDesc ) = @_;

    if( !defined($systemUserDesc) || (ref($systemUserDesc) ne 'HASH') ) {
        $self->_log( 'description de l\'utilisateur système incorrecte', 4 );
        return 1;
    }

    # L'Id du l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_id'}) ) {
        $self->_log( 'ID de l\'utilisateur système non défini', 3 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$systemUserDesc->{'usersystem_id'}.'\' incorrect', 4 );
        return 1;
    }

    # Le login de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_login'}) ) {
        $self->_log( 'Login de l\'utilisateur système non défini', 3 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_login'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
        $self->_log( 'Login \''.$systemUserDesc->{'usersystem_login'}.'\' incorrect', 4 );
        return 1;
    }

    # L'UID de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_uid'}) ) {
        $self->_log( 'UID de l\'utilisateur système non défini', 3 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_uid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'UID \''.$systemUserDesc->{'usersystem_uid'}.'\' incorrect', 4 );
        return 1;
    }

    # Le GID de l'utilisateur système
    if( !defined($systemUserDesc->{'usersystem_gid'}) ) {
        $self->_log( 'GID de l\'utilisateur système non défini', 3 );
        return 1;
    }elsif( $systemUserDesc->{'usersystem_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID \''.$systemUserDesc->{'usersystem_gid'}.'\' incorrect', 4 );
        return 1;
    }

    $self->{'userSystemDesc'} = $systemUserDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $userSystemDesc = $self->{'userSystemDesc'};

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
sub getDesc {
    my $self = shift;
    my( $desc ) = @_;

    if( $desc && !ref($desc) ) {
        return $self->{'userSystemDesc'}->{$desc};
    };

    return undef;
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

    return $self->{'userSystemDesc'}->{'usersystem_id'};
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
        push( @dnPrefixes, 'uid='.$self->{'userSystemDesc'}->{'usersystem_login'}.','.$rootDn->[$i] );
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
        $self->_log( 'DN non défini', 3 );
        return 1;
    }

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 3 );
        return 1;
    }

    my $userPasswd = $self->_convertPasswd( 'PLAIN', $self->{'userSystemDesc'}->{'usersystem_password'} );
    if( !$userPasswd ) {
        $self->_log( 'pas de mot de passe défini', 3 );
        return 1;
    }

    my $cn = $self->{'userSystemDesc'}->{'usersystem_login'};
    SWITCH: {
        if( $self->{'userSystemDesc'}->{'usersystem_firstname'} && $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_firstname'}.' '.$self->{'userSystemDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }

        if( $self->{'userSystemDesc'}->{'usersystem_firstname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_firstname'};
            last SWITCH;
        }

        if( $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }
    }

    my $sn = $self->{'userSystemDesc'}->{'usersystem_login'};
    if( $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
        $sn = $self->{'userSystemDesc'}->{'usersystem_lastname'};
    }

    my $homeDirectory = $self->{'userSystemDesc'}->{'usersystem_homedir'};
    if( !$homeDirectory ) {
        $homeDirectory = '/home/'.$self->{'userSystemDesc'}->{'usersystem_login'};
    }

    $entry->add(
        objectClass => $self->{'objectclass'},
        uid => $self->{'userSystemDesc'}->{'usersystem_login'},
        uidNumber => $self->{'userSystemDesc'}->{'usersystem_uid'},
        gidNumber => $self->{'userSystemDesc'}->{'usersystem_gid'},
        homeDirectory => $homeDirectory,
        cn => $cn,
        sn => $sn,
        userpassword => $userPasswd,
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

    my $userPasswd = $self->_convertPasswd( 'PLAIN', $self->{'userSystemDesc'}->{'usersystem_password'} );
    if( $userPasswd ) {
        if( $self->_modifyAttr( $userPasswd, $entry, 'userpassword' ) ) {
            $update = 1;
        }
    }

    my $cn = $self->{'userSystemDesc'}->{'usersystem_login'};
    SWITCH: {
        if( $self->{'userSystemDesc'}->{'usersystem_firstname'} && $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_firstname'}.' '.$self->{'userSystemDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }

        if( $self->{'userSystemDesc'}->{'usersystem_firstname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_firstname'};
            last SWITCH;
        }

        if( $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
            $cn = $self->{'userSystemDesc'}->{'usersystem_lastname'};
            last SWITCH;
        }
    }
    if( $self->_modifyAttr( $cn, $entry, 'cn' ) ) {
        $update = 1;
    }

    my $sn = $self->{'userSystemDesc'}->{'usersystem_login'};
    if( $self->{'userSystemDesc'}->{'usersystem_lastname'} ) {
        $sn = $self->{'userSystemDesc'}->{'usersystem_lastname'};
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
