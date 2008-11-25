package OBM::Entities::obmDomain;

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
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 4 );

    $self->{'parent'} = undef;
}


# Needed
sub _init {
    my $self = shift;
    my( $domainDesc ) = @_;

    if( !defined($domainDesc) || (ref($domainDesc) ne 'HASH') ) {
        $self->_log( 'description de domaine incorrecte', 4 );
        return 1;
    }

    # L'Id du domaine
    if( !defined($domainDesc->{'domain_id'}) ) {
        $self->_log( 'ID du domaine non défini', 3 );
        return 1;
    }elsif( $domainDesc->{'domain_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$domainDesc->{'domain_id'}.'\' incorrect', 4 );
        return 1;
    }

    # Le nom du domaine
    if( !defined($domainDesc->{'domain_name'}) ) {
        $self->_log( 'nom de domaine - domaine principal de messagerie - non défini', 3 );
        return 1;
    }elsif( $domainDesc->{'domain_name'} !~ /$OBM::Parameters::regexp::regexp_domain/ ) {
        $self->_log( 'nom de domaine \''.$domainDesc->{'domain_name'}.'\' incorrect', 4 );
        return 1;
    }

    # Les alias du domaine
    if( $domainDesc->{'domain_alias'} ) {
        my @aliases = split( /\r\n/, $domainDesc->{'domain_alias'} );
        for( my $i; $i<=$#aliases; $i++ ) {
            if( $aliases[$i] !~ /$OBM::Parameters::regexp::regexp_domain/ ) {
                $self->_log( 'alias de domaine \''.$aliases[$i].'\' incorrect', 4 );
            }else {
                push( @{$domainDesc->{'domain_alias'}}, $aliases[$i] );
            }
        }
    }

    $self->{'domainDesc'} = $domainDesc;

    # Est-il Global ?
    $self->{'global'} = $domainDesc->{'domain_global'};

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $domaindesc = $self->{'domainDesc'};
    
    my $description = 'domaine d\'ID \''.$domaindesc->{'domain_id'}.'\'';

    if( $domaindesc->{'domain_label'} ) {
        $description .= ', \''.$domaindesc->{'domain_label'}.'\'';
    }

    return $description;
}


# Needed
sub getDesc {
    my $self = shift;
    my( $desc ) = @_;

    if( $desc && !ref($desc) ) {
        return $self->{'domainDesc'}->{$desc};
    }

    return undef;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->getId();
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'domainDesc'}->{'domain_id'};
}


# Needed
sub getLdapServerId {
    my $self = shift;

    return $self->{'ldapServerId'};
}


sub isGlobal {
    my $self = shift;

    return $self->{'global'};
}


# Needed
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
        $rootDnPrefix = 'dc='.$self->{'domainDesc'}->{'domain_name'}.','.$self->_getParentDn();
    }

    return $rootDnPrefix;
}


# Needed by : LdapEngine
sub getDnPrefix {
    my $self = shift;
    my( $entity ) = @_;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getRootDnPrefix()) ) {
        $self->_log( 'DN de la racine du domaine non déterminée', 3 );
        return undef;
    }

    if( !defined( $entity ) ) {
        push( @dnPrefixes, $rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( !defined( $entity ) || ref($entity) eq 'OBM::Entities::obmSystemUser' ) {
        push( @dnPrefixes, 'ou=sysusers,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( !defined( $entity ) || ref($entity) eq 'OBM::Entities::obmHost' ) {
        push( @dnPrefixes, 'ou=hosts,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmUser') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=users,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmGroup') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=groups,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmMailshare') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=mailShare,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmMailServer') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=servicesConfiguration,'.$rootDn );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$#dnPrefixes], 4 );
    }

    if( (!defined( $entity ) || ref($entity) eq 'OBM::Entities::obmContact') && !$self->isGlobal() ) {
        push( @dnPrefixes, 'ou=contacts,'.$rootDn );
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
                o => $self->{'domainDesc'}->{'domain_label'}
            );

            if( $self->{'domainDesc'}->{'domain_description'} ) {
                $entry->add( description => $self->{'domainDesc'}->{'domain_description'} );
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
            if( $self->_modifyAttr( $self->{'domainDesc'}->{'domain_label'}, $entry, 'o' ) ) {
                $update = 1;
            }

            if( $self->_modifyAttr( $self->{'domainDesc'}->{'domain_description'}, $entry, 'description' ) ) {
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
