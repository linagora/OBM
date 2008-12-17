package OBM::Entities::obmUser;

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
use OBM::Tools::passwd qw(
        _toMd5
        _toSsha
        _convertPasswd
        _getNTLMPasswd
        );
use URI::Escape;
use OBM::Parameters::common;


# Needed
sub new {
    my $class = shift;
    my( $parent, $userDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 3 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $userDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation di l\'utilisateur', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'posixAccount', 'shadowAccount', 'inetOrgPerson', 'obmUser', 'sambaSamAccount' ];

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
    my( $userDesc ) = @_;

    if( !defined($userDesc) || (ref($userDesc) ne 'HASH') ) {
        $self->_log( 'description de l\'utilisateur incorrecte', 4 );
        return 1;
    }

    # User ID
    if( !defined($userDesc->{'userobm_id'}) ) {
        $self->_log( 'ID de l\'utilisateur non défini', 3 );
        return 1;
    }elsif( $userDesc->{'userobm_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$userDesc->{'userobm_id'}.'\' incorrect', 4 );
        return 1;
    }

    # User name
    if( !defined($userDesc->{'userobm_login'}) ) {
        $self->_log( 'Nom de l\'utilisateur non défini', 3 );
        return 1;
    }elsif( $userDesc->{'userobm_login'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
        $self->_log( 'Nom de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' incorrect', 4 );
        return 1;
    }

    # Current user name, if define
    if( $userDesc->{'userobm_login_current'} && $userDesc->{'userobm_login_current'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
        $self->_log( 'Nom actuel de l\'utilisateur \''.$userDesc->{'userobm_login_current'}.'\' incorrect', 4 );
        return 1;
    }

    # Archive flag
    if( $userDesc->{'userobm_archive'} ) {
        $self->setArchive();
    }

    # User UID
    if( !defined($userDesc->{'userobm_uid'}) ) {
        $self->_log( 'UID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' non défini', 3 );
        return 1;
    }elsif( $userDesc->{'userobm_uid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'UID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' incorrect', 4 );
        return 1;
    }

    # User GID
    if( !defined($userDesc->{'userobm_gid'}) ) {
        $self->_log( 'GID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' non défini', 3 );
        return 1;
    }elsif( $userDesc->{'userobm_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' incorrect', 4 );
        return 1;
    }

    # User fullname
    $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_firstname'};
    if( $userDesc->{'userobm_fullname'} && $userDesc->{'userobm_lastname'} ) {
        $userDesc->{'userobm_fullname'} .= ' '.$userDesc->{'userobm_lastname'};
    }elsif( $userDesc->{'userobm_lastname'} ) {
        $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_lastname'};
    }else {
        $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_login'};
    }

    # User Address
    if( $userDesc->{'userobm_address1'} ) {
        $userDesc->{'userobm_full_address'} = $userDesc->{'userobm_address1'};
    }

    if( $userDesc->{'userobm_address2'} ) {
        $userDesc->{'userobm_full_address'} .= '\r\n' if $userDesc->{'userobm_full_address'};
        $userDesc->{'userobm_full_address'} .= $userDesc->{'userobm_address2'};
    }

    if( $userDesc->{'userobm_address3'} ) {
        $userDesc->{'userobm_full_address'} .= '\r\n' if $userDesc->{'userobm_full_address'};
        $userDesc->{'userobm_full_address'} .= $userDesc->{'userobm_address3'};
    }

    # User phone
    if( $userDesc->{'userobm_phone'} ) {
        push( @{$userDesc->{'userobm_phone_list'}}, $userDesc->{'userobm_phone'} );
    }

    if( $userDesc->{'userobm_phone2'} ) {
        push( @{$userDesc->{'userobm_phone_list'}}, $userDesc->{'userobm_phone2'} );
    }

    # User fax
    if( $userDesc->{'userobm_fax'} ) {
        push( @{$userDesc->{'userobm_fax_list'}}, $userDesc->{'userobm_fax'} );
    }

    if( $userDesc->{'userobm_fax2'} ) {
        push( @{$userDesc->{'userobm_fax_list'}}, $userDesc->{'userobm_fax2'} );
    }

    # User e-mails
    $userDesc->{'userobm_mail_perms'} = 1;
    if( !($self->_makeEntityEmail( $userDesc->{'userobm_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') ) ) ) {
        $self->_log( 'droit mail de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' annulé, pas d\'adresses mails valides', 2 );
        $userDesc->{'userobm_mail_perms'} = 0;
    }

    # User mail right
    SWITCH: {
        if( !$userDesc->{'userobm_mail_perms'} ) {
            last SWITCH;
        }

        if( $self->{'parent'}->isGlobal() ) {
            $self->_log( 'droit mail de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' annulé, pas de droit mail dans le domaine global', 2 );
            $userDesc->{'userobm_mail_perms'} = 0;
            last SWITCH;
        }

        require OBM::Cyrus::cyrusServers;
        my $cyrusSrvList = OBM::Cyrus::cyrusServers->instance();
        if( !(my $cyrusHostName = $cyrusSrvList->getCyrusServerIp( $userDesc->{'userobm_mail_server_id'}, $userDesc->{'userobm_domain_id'}) ) ) {
            $self->_log( 'droit mail de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' annulé, serveur inconnu', 2 );
            $userDesc->{'userobm_mail_perms'} = 0;
            last SWITCH;
        }else {
            $userDesc->{'userobm_mailbox_server'} = 'lmtp:'.$cyrusHostName.':24';
        }

        $userDesc->{'userobm_mail_perms'} = 1;
    }

    # LDAP BAL destination
    $userDesc->{'userobm_ldap_mailbox'} = $userDesc->{'userobm_login'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Cyrus BAL destination
    $userDesc->{'userobm_cyrus_mailbox'} = $userDesc->{'userobm_login'};
    # Current Cyrus BAL destination
    $userDesc->{'current_userobm_cyrus_mailbox'} = $userDesc->{'userobm_login_current'};
    if( !$OBM::Parameters::common::singleNameSpace ) {
        $userDesc->{'userobm_cyrus_mailbox'} .= '@'.$self->{'parent'}->getDesc('domain_name');
        $userDesc->{'current_userobm_cyrus_mailbox'} .= '@'.$self->{'parent'}->getDesc('domain_name');
    }

    # Cyrus partition
    if( $OBM::Parameters::common::cyrusDomainPartition ) {
        $userDesc->{'userobm_cyrus_partition'} = $self->{'parent'}->getDesc('domain_name');
        $userDesc->{'userobm_cyrus_partition'} =~ s/\./_/g;
        $userDesc->{'userobm_cyrus_partition'} =~ s/-/_/g;
    }

    # Cyrus quota
    $userDesc->{'userobm_mail_quota'} = $userDesc->{'userobm_mail_quota'}*1024;

    # BAL sub-folders
    if( defined($userMailboxDefaultFolders) ) {
        foreach my $folderTree ( split( ',', $userMailboxDefaultFolders ) ) {
            if( $folderTree !~ /(^[",]$)|(^$)/ ) {
                my $folderName = $userDesc->{'userobm_login'};
                foreach my $folder ( split( '/', $folderTree ) ) {
                    $folder =~ s/^\s+//;

                    $folderName .= '/'.$folder;
                    if( !$OBM::Parameters::common::singleNameSpace ) {
                        push( @{$userDesc->{'mailbox_folders'}}, $folderName.'@'.$self->{'parent'}->getDesc('domain_name') );
                    }else {
                        push( @{$userDesc->{'mailbox_folders'}}, $folderName );
                    }
                }
            }
        }
    }

    # Vacation message
    $userDesc->{'userobm_vacation_message'} = uri_unescape($userDesc->{'userobm_vacation_message'});

    # Domain SID
    my $domainSid = $self->{'parent'}->getDesc('samba_sid');
    if( !$domainSid ) {
        $self->_log( 'pas de SID associé au domaine '.$self->{'parent'}->getDescription(), 3 );
        $self->_log( 'droit samba annulé', 2 );
        $userDesc->{'userobm_samba_perms'} = 0;
    }

    # Web perms
    if( !$OBM::Parameters::common::obmModules->{web} ) {
        delete( $userDesc->{'userobm_web_perms'} );
    }

    # Samba perms
    if( $OBM::Parameters::common::obmModules->{samba} && $userDesc->{'userobm_samba_perms'} ) {
        SWITCH: {
            # User Samba password
            if( $userDesc->{'userobm_password_type'} ne 'PLAIN' ) {
                $self->_log( 'le mot de passe doit être en \'PLAIN\' pour un utilisateur Samba, droit samba annulé pour \''.$userDesc->{'userobm_login'}.'\'', 2 );
                $userDesc->{'userobm_samba_perms'} = 0;
                last SWITCH;
            }

            my $error = $self->_getNTLMPasswd( $userDesc->{'userobm_password'}, \$userDesc->{'userobm_samba_lm_password'}, \$userDesc->{'userobm_samba_nt_password'} );
            if( $error ) {
                $self->_log( 'probleme lors de la generation du mot de passe windows de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\', droit samba annulé', 2 );
                $userDesc->{'userobm_samba_perms'} = 0;
                last SWITCH;
            }

            # Specific user UID
            if( lc($userDesc->{'userobm_perms'}) eq 'admin' ) {
                $userDesc->{'userobm_uid'} = 0;
            }

            # User SID
            $userDesc->{'userobm_samba_sid'} = $self->_getUserSID( $domainSid, $userDesc->{'userobm_uid'} );
            # Group SID
            $userDesc->{'userobm_samba_group_sid'} = $self->_getGroupSID( $domainSid, $userDesc->{'userobm_gid'} );

            # User Samba flags
            $userDesc->{'userobm_samba_flags'} = '[UX]';

            # User home share
            if( !$userDesc->{'userobm_samba_home_drive'} || !$userDesc->{'userobm_samba_home'} ) {
                delete( $userDesc->{'userobm_samba_home_drive'} );
                delete( $userDesc->{'userobm_samba_home'} );
            }else {
                $userDesc->{'userobm_samba_home_drive'} .= ':';
            }

            # W2k+ profil share (for older version, see smb.conf)
            if( $self->{'parent'}->getDesc('samba_user_profile') ) {
                $userDesc->{'userobm_samba_profile'} = $self->{'parent'}->getDesc('samba_user_profile');
                $userDesc->{'userobm_samba_profile'} =~ s/\%u/$userDesc->{'userobm_login'}/g;
            }
        }
    }else {
        $userDesc->{'userobm_samba_perms'} = 0;
    }


    $self->{'entityDesc'} = $userDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 1 );

    return 0;
}


sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'HASH' ) {
        $self->_log( 'pas de liens définis', 3 );
        return 0;
    }

    $self->{'entityDesc'}->{'mailbox_acl'} = $links;

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $userDesc = $self->{'entityDesc'};

    my $description = 'utilisateur \''.$userDesc->{'userobm_login'}.'\' (ID:'.$userDesc->{'userobm_id'}.'), nom \''.$userDesc->{'userobm_fullname'}.'\'';

    return $description;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->{'entityDesc'}->{'userobm_domain_id'};
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'userobm_id'};
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
        push( @dnPrefixes, 'uid='.$self->{'entityDesc'}->{'userobm_login'}.','.$rootDn->[$i] );
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

    my $currentUserLogin = $self->{'entityDesc'}->{'userobm_login_current'};
    if( !$currentUserLogin ) {
        $currentUserLogin = $self->{'entityDesc'}->{'userobm_login'};
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, 'uid='.$currentUserLogin.','.$rootDn->[$i] );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub _getLdapObjectclass {
    my $self = shift;
    my ($objectclass, $deletedObjectclass) = @_;
    my %realObjectClass;

    if( !defined($objectclass) || (ref($objectclass) ne 'ARRAY') ) {
        $objectclass = $self->{'objectclass'};
    }

    for( my $i=0; $i<=$#$objectclass; $i++ ) {
        if( (lc($objectclass->[$i]) eq 'sambasamaccount') && !$self->{'entityDesc'}->{'userobm_samba_perms'} ) {
            push( @{$deletedObjectclass}, $objectclass->[$i] );
            next;
        }

        $realObjectClass{$objectclass->[$i]} = 1;
    }

    # Si le droit Samba est actif, on s'assure de la présence des classes
    # nécessaires - nécessaires pour les MAJ
    if( $self->{'entityDesc'}->{'userobm_samba_perms'} ) {
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
        uid => $self->{'entityDesc'}->{'userobm_login'},
        uidNumber => $self->{'entityDesc'}->{'userobm_uid'},
        gidNumber => $self->{'entityDesc'}->{'userobm_gid'},
        loginShell => '/bin/bash'
    );

    # Full name
    $entry->add( cn => $self->{'entityDesc'}->{'userobm_fullname'} );
    $entry->add( displayName => $self->{'entityDesc'}->{'userobm_fullname'} );

    # Lastname
    if( $self->{'entityDesc'}->{'userobm_lastname'} ) {
        $entry->add( sn => $self->{'entityDesc'}->{'userobm_lastname'} );
    }

    # Firstname
    if( $self->{'entityDesc'}->{'userobm_firstname'} ) {
        $entry->add( givenName => $self->{'entityDesc'}->{'userobm_firstname'} );
    }

    # Home directory
    $entry->add( homeDirectory => '/home/'.$self->{'entityDesc'}->{'userobm_login'} );

    # User password
    if( my $userPasswd = $self->_convertPasswd( $self->{'entityDesc'}->{'userobm_password_type'}, $self->{'entityDesc'}->{'userobm_password'} ) ) {
        $entry->add( userPassword => $userPasswd );
    }

    # Phone numbers
    if( $self->{'entityDesc'}->{'userobm_phone_list'} ) {
        $entry->add( telephoneNumber => $self->{'entityDesc'}->{'userobm_phone_list'} );
    }

    # Fax number
    if( $self->{'entityDesc'}->{'userobm_fax_list'} ) {
        $entry->add( facsimileTelephoneNumber => $self->{'entityDesc'}->{'userobm_fax_list'} );
    }

    # Mobile number
    if( $self->{'entityDesc'}->{'userobm_mobile'} ) {
        $entry->add( mobile => $self->{'entityDesc'}->{'userobm_mobile'} );
    }

    # User title
    if( $self->{'entityDesc'}->{'userobm_title'} ) {
        $entry->add( title => $self->{'entityDesc'}->{'userobm_title'} );
    }

    # User service
    if( $self->{'entityDesc'}->{'userobm_service'} ) {
        $entry->add( ou => $self->{'entityDesc'}->{'userobm_service'} );
    }

    # User description
    if( $self->{'entityDesc'}->{'userobm_description'} ) {
        $entry->add( description => $self->{'entityDesc'}->{'userobm_description'} );
    }

    # User web permission
    if( $self->{'entityDesc'}->{'userobm_web_perms'} ) {
        $entry->add( webAccess => 'PERMIT' );
    }else {
        $entry->add( webAccess => 'REJECT' );
    }

    # User mailbox
    if( $self->{'entityDesc'}->{'userobm_ldap_mailbox'} ) {
        $entry->add( mailBox => $self->{'entityDesc'}->{'userobm_ldap_mailbox'} );
    }

    # User mailbox server
    if( $self->{'entityDesc'}->{'userobm_mailbox_server'} ) {
        $entry->add( mailBoxServer => $self->{'entityDesc'}->{'userobm_mailbox_server'} );
    }

    # User mail permission
    if( $self->{'entityDesc'}->{'userobm_mail_perms'} ) {
        $entry->add( mailAccess => 'PERMIT' );
    }else {
        $entry->add( mailAccess => 'REJECT' );
    }

    # User e-mails
    if( $self->{'email'} ) {
        $entry->add( mail => $self->{'email'} );
    }
    # User e-mails alias
    if( $self->{'emailAlias'} ) {
        $entry->add( mailAlias => $self->{'emailAlias'} );
    }

    # User address
    if( $self->{'entityDesc'}->{'userobm_full_address'} ) {
        # Thunderbird, Icedove... use only this attribute
        $entry->add( street => $self->{'entityDesc'}->{'userobm_full_address'} );
        # Outlook use only this attribute
        # Outlook express prefer this attribute
        $entry->add( postalAddress => $self->{'entityDesc'}->{'userobm_full_address'} );
    }

    # User zip code
    if( $self->{'entityDesc'}->{'userobm_zipcode'} ) {
        $entry->add( postalCode => $self->{'entityDesc'}->{'userobm_zipcode'} );
    }

    # User city
    if( $self->{'entityDesc'}->{'userobm_town'} ) {
        $entry->add( l => $self->{'entityDesc'}->{'userobm_town'} );
    }

    # Hidden user
    if( $self->{'entityDesc'}->{'userobm_hidden'} ) {
        $entry->add( hiddenUser => 'TRUE' );
    }else {
        $entry->add( hiddenUser => 'FALSE' );
    }

    # OBM domain
    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
    }

    # User SID
    if( $self->{'entityDesc'}->{'userobm_samba_sid'} ) {
        $entry->add( sambaSID => $self->{'entityDesc'}->{'userobm_samba_sid'} );
    }

    # User primary group SID
    if( $self->{'entityDesc'}->{'userobm_samba_group_sid'} ) {
        $entry->add( sambaPrimaryGroupSID => $self->{'entityDesc'}->{'userobm_samba_group_sid'} );
    }

    # Samba user flags
    if( $self->{'entityDesc'}->{'userobm_samba_flags'} ) {
        $entry->add( sambaAcctFlags => $self->{'entityDesc'}->{'userobm_samba_flags'} );
    }

    # Samba user passwords
    if( $self->{'entityDesc'}->{'userobm_samba_nt_password'} ) {
        $entry->add( sambaLMPassword => $self->{'entityDesc'}->{'userobm_samba_lm_password'} );
        $entry->add( sambaNTPassword => $self->{'entityDesc'}->{'userobm_samba_nt_password'} );
    }

    # Samba session script
    if( $self->{'entityDesc'}->{'userobm_samba_logon_script'} ) {
        $entry->add( sambaLogonScript => $self->{'entityDesc'}->{'userobm_samba_logon_script'} );
    }

    # Samba home drive
    if( $self->{'entityDesc'}->{'userobm_samba_home_drive'} && $self->{'entityDesc'}->{'userobm_samba_home'} ) {
        $entry->add( sambaHomeDrive => $self->{'entityDesc'}->{'userobm_samba_home_drive'} );
        $entry->add( sambaHomePath => $self->{'entityDesc'}->{'userobm_samba_home'} );
    }

    # Samba user W2k profile
    if( $self->{'entityDesc'}->{'userobm_samba_profile'} ) {
        $entry->add( sambaProfilePath => $self->{'entityDesc'}->{'userobm_samba_profile'} );
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
                if( $self->_modifyAttrList( undef, $entry, $deleteAttrs->[$i]  ) ) {
                    $update = 1;
                }
            }
        }

        # User UID number
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_uid'}, $entry, 'uidNumber' ) ) {
            $update = 1;
        }

        # User GID number
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_gid'}, $entry, 'gidNumber' ) ) {
            $update = 1;
        }

        # User shell
        if( $self->_modifyAttr( '/bin/bash', $entry, 'loginShell' ) ) {
            $update = 1;
        }

        # User full name
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_fullname'}, $entry, 'cn' ) ) {
            $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_fullname'}, $entry, 'displayName' );
            $update = 1;
        }

        # User lastname
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_lastname'}, $entry, 'sn' ) ) {
            $update = 1;
        }

        # User firstname
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_firstname'}, $entry, 'givenName' ) ) {
            $update = 1;
        }

        # User home directory
        if( $self->_modifyAttr( '/home/'.$self->{'entityDesc'}->{'userobm_login'}, $entry, 'homeDirectory' ) ) {
            $update = 1;
        }

        # Phone numbers
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'userobm_phone_list'}, $entry, 'telephoneNumber' ) ) {
            $update = 1;
        }

        # Fax numbers
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'userobm_fax_list'}, $entry, 'facsimileTelephoneNumber' ) ) {
            $update = 1;
        }

        # Mobile number
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'userobm_mobile'}, $entry, 'mobile' ) ) {
            $update = 1;
        }

        # User title
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_title'}, $entry, 'title' ) ) {
            $update = 1;
        }

        # User service
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_service'}, $entry, 'ou' ) ) {
            $update = 1;
        }

        # User description
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_description'}, $entry, 'description' ) ) {
            $update = 1;
        }

        # User web permission
        if( $self->{'entityDesc'}->{'userobm_web_perms'} && $self->_modifyAttr( 'PERMIT', $entry, 'webAccess' ) ) {
            $update = 1;
        }elsif( !$self->{'entityDesc'}->{'userobm_web_perms'} && $self->_modifyAttr( 'REJECT', $entry, 'webAccess' ) ) {
            $update = 1;
        }

        # User mailbox
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_ldap_mailbox'}, $entry, 'mailBox' ) ) {
            $update = 1;
        }

        # User mailbox server
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_mailbox_server'}, $entry, 'mailBoxServer' ) ) {
            $update = 1;
        }

        # User mail permission
        if( $self->{'entityDesc'}->{'userobm_mail_perms'} && $self->_modifyAttr( 'PERMIT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }elsif( !$self->{'entityDesc'}->{'userobm_mail_perms'} && $self->_modifyAttr( 'REJECT', $entry, 'mailAccess' ) ) {
            $update = 1;
        }

        # User e-mails
        if( $self->_modifyAttrList( $self->{'email'}, $entry, 'mail' ) ) {
            $update = 1;
        }
        # User e-mails alias
        if( $self->_modifyAttrList( $self->{'emailAlias'}, $entry, 'mailAlias' ) ) {
            $update = 1;
        }

        # User address
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_full_address'}, $entry, 'street' ) ) {
            $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_full_address'}, $entry, 'postalAddress' );
            $update = 1;
        }

        # User zip code
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_zipcode'}, $entry, 'postalCode' ) ) {
            $update = 1;
        }

        # User city
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_town'}, $entry, 'l' ) ) {
            $update = 1;
        }

        # Hidden user
        if( $self->{'entityDesc'}->{'userobm_hidden'} && $self->_modifyAttr( 'TRUE', $entry, 'hiddenUser' ) ) {
            $update = 1;
        }elsif( $self->_modifyAttr( 'FALSE', $entry, 'hiddenUser' ) ) {
            $update = 1;
        }

        # OBM domain
        if( defined($self->{'parent'}) && $self->_modifyAttr( $self->{'parent'}->getDesc('domain_name'), $entry, 'obmDomain' ) ) {
            $update = 1;
        }

        if( defined($self->{'entityDesc'}->{'userobm_samba_sid'}) ) {
            my @currentLdapUserSambaSid = $entry->get_value( 'sambaSID', asref => 1 );
            if( $#currentLdapUserSambaSid < 0 ) {
                # This updates must be done only if samba user right is
                # re-enable.

                # If LDAP sambaSID attribute isn't set, but 'userobm_samba_sid'
                # is defined in current user description, samba perms is
                # re-enable, we must put 'sambaLMPassword' and
                # 'sambaNTPassword'...
                if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_nt_password'}, $entry, 'sambaNTPassword' ) ) {
                    $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_lm_password'}, $entry, 'sambaLMPassword' );
                    $update = 1;
                }

                # ...and 'sambaAcctFlags' too.
                if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_flags'}, $entry, 'sambaAcctFlags' ) ) {
                    $update = 1;
                }
            }
        }

        # Samba user SID
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_sid'}, $entry, 'sambaSID' ) ) {
            $update = 1;
        }

        # Samba group SID
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_group_sid'}, $entry, 'sambaPrimaryGroupSID' ) ) {
            $update = 1;
        }

        # Samba session script
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_logon_script'}, $entry, 'sambaLogonScript' ) ) {
            $update = 1;
        }

        # Samba home drive
        if( $self->{'entityDesc'}->{'userobm_samba_home_drive'} && $self->{'entityDesc'}->{'userobm_samba_home'} ) {
            if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_home_drive'}, $entry, 'sambaHomeDrive' ) ) {
                $update = 1;
            }

            if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_home'}, $entry, 'sambaHomePath' ) ) {
                $update = 1;
            }
        }
        
        # Samba user W2k profile
        if( $self->_modifyAttr( $self->{'entityDesc'}->{'userobm_samba_profile'}, $entry, 'sambaProfilePath' ) ) {
            $update = 1;
        }
    }

    if( $self->getUpdateLinks() ) {
    }

    return $update;
}


# Needed : cyrusEngine
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

    return $self->{'entityDesc'}->{'userobm_mail_perms'};
}


# Needed : cyrusEngine
sub getMailboxName {
    my $self = shift;
    my( $which ) = @_;
    my $mailBoxName = undef;

    if( lc($which) =~ /^new$/ ) {
        $mailBoxName = $self->{'entityDesc'}->{'userobm_cyrus_mailbox'};
    }elsif( lc($which) =~ /^current$/ ) {
        $mailBoxName = $self->{'entityDesc'}->{'current_userobm_cyrus_mailbox'};
    }

    return $mailBoxName;
}


# Needed : cyrusEngine
sub getMailServerId {
    my $self = shift;

    return $self->{'entityDesc'}->{'userobm_mail_server_id'};
}


# Needed : cyrusEngine
sub getMailboxPrefix {
    my $self = shift;

    return 'user/'
}


# Needed : cyrusEngine
sub getMailboxQuota {
    my $self = shift;

    return $self->{'entityDesc'}->{'userobm_mail_quota'};
}


# Needed : cyrusEngine
sub getMailboxAcl {
    my $self = shift;
    my $mailBoxAcl = undef;

    if( !$self->getArchive() && $self->isMailActive() ) {
        $mailBoxAcl = $self->{'entityDesc'}->{'mailbox_acl'};
    }

    return $mailBoxAcl;
}


# Needed : cyrusEngine
sub getMailboxPartition {
    my $self = shift;

    return $self->{'entityDesc'}->{'userobm_cyrus_partition'};
}


# Needed : cyrusEngine
sub getMailboxDefaultFolders {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailbox_folders'};
}


# Needed : sieveEngine
sub isSieveAvailable {
    my $self = shift;

    if( defined($self->{'parent'}) && !$self->{'parent'}->isGlobal() ) {
        return 1;
    }

    return 0;
}


# Needed : sieveEngine
sub getSieveVacation {
    my $self = shift;

    # If vacation isn't enable, then no vacation message
    if( !$self->{'entityDesc'}->{'userobm_vacation_enable'} ) {
        $self->_log( $self->getDescription().' : message d\'absence désactivé', 4 );
        return undef;
    }

    # If no vacation message, then no vacation message...
    if( !$self->{'entityDesc'}->{'userobm_vacation_message'} ) {
        $self->_log( $self->getDescription().' : message d\'absence vide !', 4 );
        return undef;
    }

    my $boxEmails = $self->{'email'};
    my $boxEmailsAlias = $self->{'emailAlias'};

    # If no mail addess, then no vacation message
    if( ($#{$boxEmails} < 0) && ($#{$boxEmailsAlias} < 0) ) {
        $self->_log( $self->getDescription().' : pas d\'adresses mails défini', 4 );
        return undef;
    }

    my $vacationMsg = 'vacation :addresses [ ';
    my $firstAddress = 1;
    for( my $i=0; $i<=$#{$boxEmails}; $i++ ) {
        if( !$firstAddress ) {
            $vacationMsg .= ', ';
        }else {
            $firstAddress = 0;
        }

        $vacationMsg .= '"'.$boxEmails->[$i].'"';
    }

    for( my $i=0; $i<=$#{$boxEmailsAlias}; $i++ ) {
        if( !$firstAddress ) {
            $vacationMsg .= ', ';
        }else {
            $firstAddress = 0;
        }

        $vacationMsg .= '"'.$boxEmailsAlias->[$i].'"';
    }

    $vacationMsg .= ' ] "'.$self->{'entityDesc'}->{'userobm_vacation_message'}.'";';

    return $vacationMsg;
}


sub getSieveNomade {
    my $self = shift;

    # If nomade not available, then no redirection
    if( !$self->{'entityDesc'}->{'userobm_nomade_perms'} ) {
        $self->_log( $self->getDescription().' : redirection de messagerie non autorisée', 4 );
        return undef;
    }

    # If nomade not enable, then no redirection
    if( !$self->{'entityDesc'}->{'userobm_nomade_enable'} ) {
        $self->_log( $self->getDescription().' : redirection de messagerie non activée', 4 );
        return undef;
    }

    # If redirection email is non define, then no redirection
    if( !$self->{'entityDesc'}->{'userobm_email_nomade'} ) {
        $self->_log( $self->getDescription().' : adresse mail de redirection non définie', 4 );
        return undef;
    }

    my $nomadeMsg = 'redirect "'.$self->{'entityDesc'}->{'userobm_email_nomade'}.'";';

    if( !$self->{'entityDesc'}->{'userobm_nomade_local_copy'} ) {
        $nomadeMsg .= 'discard;';
        $nomadeMsg .= 'stop;';
    }else {
        $nomadeMsg .= 'keep;';
    }

    return $nomadeMsg;
}
