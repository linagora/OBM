package OBM::Entities::obmHost;

$VERSION = '1.0';

use OBM::Entities::commonEntities;
@ISA = ('OBM::Entities::commonEntities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Tools::commonMethods qw(
        _log
        dump
        );
use OBM::Ldap::utils qw(
        _modifyAttr
        _modifyAttrList
        _diffObjectclassAttrs
        );
use OBM::Password::passwd qw(
        _getNTLMPasswd
        );
use OBM::Samba::utils qw(
        _getUserSID
        _getGroupSID
        );
use OBM::Parameters::common;
use OBM::Parameters::regexp;


# Needed
sub new {
    my $class = shift;
    my( $parent, $hostDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 3 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $hostDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation de l\'hôte', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'device', 'obmHost', 'sambaSamAccount' ];

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
    my( $hostDesc ) = @_;

    if( !defined($hostDesc) || (ref($hostDesc) ne 'HASH') ) {
        $self->_log( 'description de l\'hôte incorrect', 4 );
        return 1;
    }

    # L'ID de l'hôte
    if( !defined($hostDesc->{'host_id'}) ) {
        $self->_log( 'ID de l\'hôte non défini', 0 );
        return 1;
    }elsif( $hostDesc->{'host_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$hostDesc->{'host_id'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom de l'hôte
    if( !defined($hostDesc->{'host_name'}) ) {
        $self->_log( 'nom de l\'hôte non défini', 0 );
        return 1;
    }
    
    $hostDesc->{'system_host_name'} = lc($hostDesc->{'host_name'});
    if( $hostDesc->{'system_host_name'} !~ /$OBM::Parameters::regexp::regexp_hostname/ ) {
        $self->_log( 'nom de l\'hôte \''.$hostDesc->{'host_name'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom actuel de l'hôte, si définit
    $hostDesc->{'host_name_current'} = lc($hostDesc->{'host_name_current'});
    if( $hostDesc->{'host_name_current'} && $hostDesc->{'host_name_current'} !~ /$OBM::Parameters::regexp::regexp_hostname/ ) {
        $self->_log( 'nom actuel de l\'hôte \''.$hostDesc->{'host_name_current'}.'\' incorrect', 0 );
        return 1;
    }

    # L'adresse IP
    if( $hostDesc->{'host_ip'} && $hostDesc->{'host_ip'} !~ /$regexp_ip/ ) {
        $self->_log( 'ip de l\'hôte incorrecte. IP non prise en compte', 1 );
        delete( $hostDesc->{'host_ip'} );
    }

#    # Les informations Samba
#    if( $OBM::Parameters::common::obmModules->{'samba'} && $hostDesc->{'host_samba'} ) {
#        $hostDesc->{'host_login'} = $hostDesc->{'system_host_name'}.'$';
#        $hostDesc->{'host_samba_sid'} = $self->_getUserSID( $domainSid, $hostDesc->{'host_uid'} );
#        $hostDesc->{'host_samba_group_sid'} = $self->_getGroupSID( $domainSid, $hostDesc->{'host_gid'} );
#        $hostDesc->{'host_samba_flags'} = '[W]';
#
#        if( $self->_getNTLMPasswd( $hostDesc->{'system_host_name'}, \$hostDesc->{'host_lm_passwd'}, \$hostDesc->{'host_nt_passwd'} ) ) {
#            $self->_log( 'probleme lors de la generation du mot de passe windows de l\'hote : '.$self->getDescription(), 3 );
#            if( $hostDesc->{'host_samba'} ) {
#                $self->_log( 'droit samba annulé', 2 );
#                $hostDesc->{'host_samba'} = 0;
#            }
#        }
#    }else {
#        $hostDesc->{'host_samba'} = 0;
#    }

    $self->{'entityDesc'} = $hostDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


sub setLinks {
    my $self = shift;
    my( $links ) = @_;
    my $hostDesc = $self->{'entityDesc'};

    if( $OBM::Parameters::common::obmModules->{'samba'} && $links->{'host_samba'} ) {
        $hostDesc->{'host_samba'} = 1;
    }else {
        $hostDesc->{'host_samba'} = 0;
    }

    # Le SID du domaine
    my $domainSid = $self->{'parent'}->getDesc('samba_sid');
    if( !$domainSid ) {
        $self->_log( 'pas de SID associé au domaine '.$self->{'parent'}->getDescription(), 3 );
        if( $hostDesc->{'host_samba'} ) {
            $self->_log( 'droit samba annulé', 2 );
            $hostDesc->{'host_samba'} = 0;
        }
    }

    if( $hostDesc->{'host_samba'} ) {
        $hostDesc->{'host_login'} = $hostDesc->{'system_host_name'}.'$';
        $hostDesc->{'host_samba_sid'} = $self->_getUserSID( $domainSid, $hostDesc->{'host_uid'} );
        $hostDesc->{'host_samba_group_sid'} = $self->_getGroupSID( $domainSid, $hostDesc->{'host_gid'} );
        $hostDesc->{'host_samba_flags'} = '[W]';

        if( $self->_getNTLMPasswd( $hostDesc->{'system_host_name'}, \$hostDesc->{'host_lm_passwd'}, \$hostDesc->{'host_nt_passwd'} ) ) {
            $self->_log( 'probleme lors de la generation du mot de passe windows de l\'hote : '.$self->getDescription(), 3 );
            if( $hostDesc->{'host_samba'} ) {
                $self->_log( 'droit samba annulé', 2 );
                $hostDesc->{'host_samba'} = 0;
            }
        }
    }

    $self->_log( $hostDesc->{'host_samba'}, 0 );

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $hostDesc = $self->{'entityDesc'};

    my $description = 'hôte d\'ID \''.$hostDesc->{'host_id'}.'\', nom \''.$hostDesc->{'host_name'}.'\'';

    return $description;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->{'entityDesc'}->{'host_domain_id'};
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'host_id'};
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
        push( @dnPrefixes, 'cn='.$self->{'entityDesc'}->{'system_host_name'}.','.$rootDn->[$i] );
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

    my $currentHostName = $self->{'entityDesc'}->{'system_host_name'};
    if( $self->{'entityDesc'}->{'host_name_current'} && ($currentHostName ne $self->{'entityDesc'}->{'host_name_current'}) ) {
        $currentHostName = $self->{'entityDesc'}->{'host_name_current'};
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'cn='.$currentHostName.','.$rootDn->[$i] );
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
        if( (lc($objectclass->[$i]) eq 'sambasamaccount') && !$self->{'entityDesc'}->{'host_samba'} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $self->{'entityDesc'}->{'host_samba'} ) {
        $realObjectClass{'sambaSamAccount'} = 1;
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
        cn => $self->{'entityDesc'}->{'system_host_name'}
    );

    # L'IP
    if( $self->{'entityDesc'}->{'host_ip'} ) {
        $entry->add( ipHostNumber => $self->{'entityDesc'}->{'host_ip'} );
    }

    # La description
    if( $self->{'entityDesc'}->{'host_description'} ) {
        $entry->add( description => $self->{'entityDesc'}->{'host_description'} );
    }
    
    # Le domaine OBM
    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
    }

    # Le nom windows
    if( $self->{'entityDesc'}->{'host_login'} ) {
        $entry->add( uid => $self->{'entityDesc'}->{'host_login'} );
    }

    # Le SID de l'hôte
    if( $self->{'entityDesc'}->{'host_samba_sid'} ) {
        $entry->add( sambaSID => $self->{'entityDesc'}->{'host_samba_sid'} );
    }

    # Le groupe de l'hôte
    if( $self->{'entityDesc'}->{'host_samba_group_sid'} ) {
        $entry->add( sambaPrimaryGroupSID => $self->{'entityDesc'}->{'host_samba_group_sid'} );
    }

    # Les flags de l'hôte Samba
    if( $self->{'entityDesc'}->{'host_samba_flags'} ) {
        $entry->add( sambaAcctFlags => $self->{'entityDesc'}->{'host_samba_flags'} );
    }

    # Les mots de passes windows
    if( $self->{'entityDesc'}->{'host_lm_passwd'} ) {
        $entry->add( sambaLMPassword => $self->{'entityDesc'}->{'host_lm_passwd'} );
    }
    if( $self->{'entityDesc'}->{'host_nt_passwd'} ) {
        $entry->add( sambaNTPassword => $self->{'entityDesc'}->{'host_nt_passwd'} );
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
    my @deletedObjectclass;
    my $currentObjectclass = $self->_getLdapObjectclass( $entry->get_value('objectClass', asref => 1), \@deletedObjectclass);
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
    

    if( $self->getUpdateEntity() ) {
        # La description
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_description'}, $entry, 'description' ) ) {
            $update = 1;
        }
    
        # L'adresse IP
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_ip'}, $entry, 'ipHostNumber' ) ) {
            $update = 1;
        }
    
        # Le domaine OBM
        if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
            if( $self->_modifyAttr( $domainName, $entry, 'obmDomain' ) ) {
                $update = 1;
            }
        }
    }
    
    if( $self->getUpdateLinks() ) {
        # Le nom windows
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_login'}, $entry, 'uid' ) ) {
            $update = 1;
        }
    
        if( $self->{'entityDesc'}->{'host_samba_sid'} ) {
            my @currentLdapHostSambaSid = $entry->get_value( 'sambaSID', asref => 1 );
            if( $#currentLdapHostSambaSid < 0 ) {
                # Si le SID de l'hôte n'est pas actuellement dans LDAP mais est
                # dans la description de l'hôte, c'est qu'on vient de ré-activer
                # le droit samba de l'hôte. Il faut donc placer les mots de
                # passes.
                if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_lm_passwd'}, $entry, 'sambaLMPassword' ) ) {
                    $self->_modifyAttr( $self->{'entityDesc'}->{'host_nt_passwd'}, $entry, 'sambaNTPassword' );
                    $update = 1;
                }
            }
        }
    
        # Le SID de l'hôte
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_samba_sid'}, $entry, 'sambaSID' ) ) {
            $update = 1;
        }
    
        # Le groupe de l'hôte
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_samba_group_sid'}, $entry, 'sambaPrimaryGroupSID' ) ) {
            $update = 1;
        }
    
        # Les flags de l'hôte Samba
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'host_samba_flags'}, $entry, 'sambaAcctFlags' ) ) {
            $update = 1;
        }
    }

    return $update;
}


sub getBdUpdate {
    my $self = shift;

    if( $self->getUpdateEntity() || $self->getUpdateLinks() ) {
        return 1;
    }

    return 0;
}
