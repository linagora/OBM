package OBM::Entities::obmHost;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

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

    $self->{'ldapMappingScope'} = {
        'updateLinks' => [
            'host_login',
            'host_samba_sid',
            'host_uidnumber',
            'host_gid',
            'host_homedirectory',
            'host_samba_group_sid',
            'host_samba_flags'
        ],
        'updateSambaPasswd' => [
            'host_lm_passwd',
            'host_nt_passwd'
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
# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmhost when
# adding new value into description
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

    #Le gidNumber de l'hôte
    $hostDesc->{'host_gid'} = undef;

    # Le nom de l'hôte
    if( !defined($hostDesc->{'host_name'}) ) {
        $self->_log( 'nom de l\'hôte non défini', 0 );
        return 1;
    }
    
    $hostDesc->{'host_name_new'} = lc($hostDesc->{'host_name'});
    if( $hostDesc->{'host_name_new'} !~ /$OBM::Parameters::regexp::regexp_hostname/ ) {
        $self->_log( 'nom de l\'hôte \''.$hostDesc->{'host_name'}.'\' incorrect', 0 );
        return 1;
    }

    # Le nom actuel de l'hôte, si définit
    if( defined($hostDesc->{'host_name_current'}) ) {
        $hostDesc->{'host_name_current'} = lc($hostDesc->{'host_name_current'});
        if( !$hostDesc->{'host_name_current'} || $hostDesc->{'host_name_current'} !~ /$OBM::Parameters::regexp::regexp_hostname/ ) {
            $self->_log( 'nom actuel de l\'hôte \''.$hostDesc->{'host_name_current'}.'\' incorrect', 0 );
            return 1;
        }
    }

    # L'adresse IP
    if( $hostDesc->{'host_ip'} && $hostDesc->{'host_ip'} !~ /$regexp_ip/ ) {
        $self->_log( 'ip de l\'hôte incorrecte. IP non prise en compte', 1 );
        delete( $hostDesc->{'host_ip'} );
    }

    # OBM Domain
    if( defined($self->{'parent'}) ) {
        $hostDesc->{'host_obm_domain'} = $self->{'parent'}->getDesc('domain_name');
    }

    $self->{'entityDesc'} = $hostDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmhost when
# adding new value into description
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
		$hostDesc->{'host_gid'} = 515;
		$hostDesc->{'host_login'} = $hostDesc->{'host_name_new'}.'$';
        $hostDesc->{'host_samba_sid'} = $self->_getUserSID( $domainSid, $hostDesc->{'host_uid'} );
        $hostDesc->{'host_samba_group_sid'} = $self->_getGroupSID( $domainSid, $hostDesc->{'host_gid'} );
        $hostDesc->{'host_samba_flags'} = '[W]';
        $hostDesc->{'host_homedirectory'} = '/dev/null';
        $hostDesc->{'host_uidnumber'} = $hostDesc->{'host_uid'};


        if( $self->_getNTLMPasswd( $hostDesc->{'host_name_new'}, \$hostDesc->{'host_lm_passwd'}, \$hostDesc->{'host_nt_passwd'} ) ) {
            $self->_log( 'probleme lors de la generation du mot de passe windows de l\'hote : '.$self->getDescription(), 3 );
            if( $hostDesc->{'host_samba'} ) {
                $self->_log( 'droit samba annulé', 2 );
                $hostDesc->{'host_samba'} = 0;
            }
        }
    }

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
        push( @exceptions, @{$self->{'ldapMappingScope'}->{'updateLinks'}}, @{$self->{'ldapMappingScope'}->{'updateSambaPasswd'}} );
        my $attrsMapping = $ldapMapping->getAllAttrsMapping( $self, \@exceptions );

        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            if( $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
                $update = 1;
            }
        }
    }

    
    if( $self->getUpdateLinks() ) {
        if( $self->{'entityDesc'}->{'host_samba_sid'} ) {
            my @currentLdapHostSambaSid = $entry->get_value( 'sambaSID', asref => 1 );
            if( $#currentLdapHostSambaSid < 0 ) {
                # Si le SID de l'hôte n'est pas actuellement dans LDAP mais est
                # dans la description de l'hôte, c'est qu'on vient de ré-activer
                # le droit samba de l'hôte. Il faut donc placer les mots de
                # passes.
                my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateSambaPasswd'} );
                for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
                    if( $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
                        $update = 1;
                    }
                }
            }
        }

        my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateLinks'} );
        for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
            if( $self->_modifyAttr( $self->getDesc( $attrsMapping->[$i]->{'desc'}->{'name'} ), $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
                $update = 1;
            }
        }
    }
    

    return $update;
}
