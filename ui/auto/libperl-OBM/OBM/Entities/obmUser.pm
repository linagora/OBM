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


package OBM::Entities::obmUser;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use URI::Escape;
use OBM::Parameters::common;


# Needed
sub new {
    my $class = shift;
    my( $parent, $userDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 1 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $userDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation de l\'utilisateur', 1 );
        return undef;
    }

    $self->{'ldapMappingScope'} = {
        'updateLinks' => [],
        'updateUnixPasswd' => [
            'userobm_password',
            'userobm_password_crypt'
        ],
        'updateSambaPasswd' => [
            'userobm_samba_lm_password',
            'userobm_samba_nt_password',
            'userobm_pwd_lastset_time'
        ],
        'updateEnableSamba' => [
            'userobm_samba_flags'
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
# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmuser when
# adding new value into description
sub _init {
    my $self = shift;
    my( $userDesc ) = @_;

    if( !defined($userDesc) || (ref($userDesc) ne 'HASH') ) {
        $self->_log( 'description de l\'utilisateur incorrecte', 1 );
        return 1;
    }

    # User ID
    if( !defined($userDesc->{'userobm_id'}) ) {
        $self->_log( 'ID de l\'utilisateur non défini', 1 );
        return 1;
    }elsif( $userDesc->{'userobm_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$userDesc->{'userobm_id'}.'\' incorrect', 1 );
        return 1;
    }

    # User login
    if( !defined($userDesc->{'userobm_login'}) ) {
        $self->_log( 'login de l\'utilisateur non défini', 1 );
        return 1;
    }
    
    $userDesc->{'userobm_login_new'} = lc($userDesc->{'userobm_login'});
    if( $userDesc->{'userobm_login_new'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
        $self->_log( 'login de l\'utilisateur \''.$userDesc->{'userobm_login_new'}.'\' incorrect', 1 );
        return 1;
    }

    # Current user name, if define
    if( defined($userDesc->{'userobm_login_current'}) ) {
        $userDesc->{'userobm_login_current'} = lc($userDesc->{'userobm_login_current'});
        if( !$userDesc->{'userobm_login_current'} || $userDesc->{'userobm_login_current'} !~ /$OBM::Parameters::regexp::regexp_login/ ) {
            $self->_log( 'login actuel de l\'utilisateur \''.$userDesc->{'userobm_login_current'}.'\' incorrect', 1 );
            return 1;
        }
    }else {
        $userDesc->{'userobm_login_current'} = $userDesc->{'userobm_login_new'};
    }


    # Archive flag
    if( $userDesc->{'userobm_archive'} ) {
        $self->setArchive();
    }

    # User UID
    if( !defined($userDesc->{'userobm_uid'}) ) {
        $self->_log( 'UID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' non défini', 1 );
        return 1;
    }elsif( $userDesc->{'userobm_uid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'UID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' incorrect', 1 );
        return 1;
    }

    # User GID
    if( !defined($userDesc->{'userobm_gid'}) ) {
        $self->_log( 'GID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' non défini', 1 );
        return 1;
    }elsif( $userDesc->{'userobm_gid'} !~ /$OBM::Parameters::regexp::regexp_uid/ ) {
        $self->_log( 'GID de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' incorrect', 1 );
        return 1;
    }

    # User shell
    $userDesc->{'userobm_loginshell'} = '/bin/bash';

    # User home directory
    $userDesc->{'userobm_homedirectory'} = '/home/'.$userDesc->{'userobm_login_new'};

    # User password
    $userDesc->{'userobm_password_crypt'} = $self->_convertPasswd( $userDesc->{'userobm_password_type'}, $userDesc->{'userobm_password'} );

    # User fullname
    $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_firstname'};
    if( $userDesc->{'userobm_fullname'} && $userDesc->{'userobm_lastname'} ) {
        $userDesc->{'userobm_fullname'} .= ' '.$userDesc->{'userobm_lastname'};
    }elsif( $userDesc->{'userobm_lastname'} ) {
        $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_lastname'};
    }else {
        $userDesc->{'userobm_fullname'} = $userDesc->{'userobm_login'};
        $userDesc->{'userobm_lastname'} = $userDesc->{'userobm_login'};
    }

    # User Address
    my @fullAddress;
    foreach my $field(('userobm_address1', 'userobm_address2',
            'userobm_address3')) {
        push @fullAddress, $userDesc->{$field} if
            ($userDesc->{$field});
    }
    $userDesc->{userobm_full_address} = join('$', @fullAddress);

    # User registered address
    my @registeredAddress;
    push( @registeredAddress, $userDesc->{'userobm_address1'} ) if $userDesc->{'userobm_address1'};
    push( @registeredAddress, $userDesc->{'userobm_address2'} ) if $userDesc->{'userobm_address2'};
    push( @registeredAddress, $userDesc->{'userobm_address3'} ) if $userDesc->{'userobm_address3'};
    my $town = $userDesc->{'userobm_town'};
    if( $town && $userDesc->{'userobm_zipcode'} ) {
        $town .= ', ';
    }
    $town .= $userDesc->{'userobm_zipcode'} if $userDesc->{'userobm_zipcode'};
    push( @registeredAddress, $town ) if $town;
    push( @registeredAddress, $userDesc->{'userobm_expresspostal'} ) if $userDesc->{'userobm_expresspostal'};
    $userDesc->{'userobm_registered_address'} = join( '$', @registeredAddress ) if $#registeredAddress >= 0;

    # User phone
    my %phoneList;
    if( $userDesc->{'userobm_phone'} ) {
        $phoneList{$userDesc->{'userobm_phone'}} = 1;
    }

    if( $userDesc->{'userobm_phone2'} ) {
        $phoneList{$userDesc->{'userobm_phone2'}} = 1;
    }

    my @phoneList = keys(%phoneList);
    if( $#phoneList >= 0 ) {
        $userDesc->{'userobm_phone_list'} = \@phoneList;
    }

    # User fax
    my %faxList;
    if( $userDesc->{'userobm_fax'} ) {
        $faxList{$userDesc->{'userobm_fax'}} = 1;
    }

    if( $userDesc->{'userobm_fax2'} ) {
        $faxList{$userDesc->{'userobm_fax2'}} = 1;
    }

    my @faxList = keys(%faxList);
    if( $#faxList >= 0 ) {
        $userDesc->{'userobm_fax_list'} = \@faxList;
    }

    # User jpeg
    if( $userDesc->{'userobm_photo_id'} ) {
        my @jpeg;
        my $pathJpeg = substr( $userDesc->{'userobm_photo_id'}, -1 ,1 );

        $pathJpeg = $OBM::Parameters::common::documentRoot.$OBM::Parameters::common::documentDefaultPath.$pathJpeg.'/'.$userDesc->{'userobm_photo_id'};
        if( -f $pathJpeg && -r $pathJpeg ) {
            open( JPEG, $pathJpeg );
            @jpeg = <JPEG>;
            close JPEG;

            $userDesc->{'userobm_photo'} = join( '', @jpeg );
        }else {
            $self->_log( 'fichier jpeg '.$pathJpeg.' de '.$userDesc->{'userobm_login'}.' introuvable', 2 );
        }
    }

    # User accout expiration date
    if( $userDesc->{'userobm_account_dateexp'} ) {
        require Time::Local;
        my @date_exp = split(/-/,$userDesc->{'userobm_account_dateexp'});
        SWITCH: {
            if( $date_exp[2] < 1 || $date_exp[2] > 31 ) {
                $self->_log( 'date d\'expiration de '.$userDesc->{'userobm_login'}.' incorrecte. Le jour doit être dans l\'intervale [1..31]. Date non prise en compte.', 2 );
                last SWITCH;
            }

            if( $date_exp[1] < 1 || $date_exp[1] > 12 ) {
                $self->_log( 'date d\'expiration de '.$userDesc->{'userobm_login'}.' incorrecte. Le mois doit être dans l\'intervale [1..12]. Date non prise en compte.', 2 );
                last SWITCH;
            }

            if( $date_exp[0] < 1970 || $date_exp[0] > 2037 ) {
                $self->_log( 'date d\'expiration de '.$userDesc->{'userobm_login'}.' incorrecte. L\'année doit être dans l\'intervale [1970..2037]. Date non prise en compte.', 2 );
                last SWITCH;
            }

            $userDesc->{'userobm_account_dateexp_timestamp'} = Time::Local::timelocal(0,0,0,$date_exp[2],$date_exp[1]-1,$date_exp[0]);
        }
    }

    # User e-mails
    ( $userDesc->{'userobm_main_email'}, $userDesc->{'userobm_alias_email'} ) = $self->_makeEntityEmail( $userDesc->{'userobm_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
    if( !defined($userDesc->{'userobm_main_email'}) ) {
        if( $userDesc->{'userobm_mail_perms'} ) {
            $self->_log( 'droit mail de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\' annulé, pas d\'adresses mails valides', 2 );
        }
        $userDesc->{'userobm_mail_perms'} = 0;
        delete($userDesc->{'userobm_main_email'});
        delete($userDesc->{'userobm_alias_email'});
    }

    # User mail permission
    if( $userDesc->{'userobm_mail_perms'} ) {
        $userDesc->{'userobm_mail_access'} = 'PERMIT';
    }else {
        $userDesc->{'userobm_mail_access'} = 'REJECT';
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
    $userDesc->{'userobm_ldap_mailbox'} = $userDesc->{'userobm_login_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Cyrus BAL destination
    $userDesc->{'userobm_cyrus_mailbox'} = $userDesc->{'userobm_login_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Current Cyrus BAL destination
    $userDesc->{'current_userobm_cyrus_mailbox'} = $userDesc->{'userobm_login_current'}.'@'.$self->{'parent'}->getDesc('domain_name');

    # Cyrus partition
    if( $OBM::Parameters::common::cyrusDomainPartition ) {
        $userDesc->{'userobm_cyrus_partition'} = $self->{'parent'}->getDesc('domain_name');
        $userDesc->{'userobm_cyrus_partition'} =~ s/\./_/g;
        $userDesc->{'userobm_cyrus_partition'} =~ s/-/_/g;
    }

    # Cyrus quota
    $userDesc->{'userobm_mail_quota'} = 0 if !defined( $userDesc->{'userobm_mail_quota'} );
    $userDesc->{'userobm_mail_quota'} = $userDesc->{'userobm_mail_quota'}*1024;

    # BAL sub-folders
    if( defined($userMailboxDefaultFolders) ) {
        foreach my $folderTree ( split( ',', $userMailboxDefaultFolders ) ) {
            if( $folderTree !~ /(^[",]$)|(^$)/ ) {
                my $folderName = $userDesc->{'userobm_login_new'};
                foreach my $folder ( split( '/', $folderTree ) ) {
                    $folder =~ s/^\s+//;

                    $folderName .= '/'.$folder;
                    push( @{$userDesc->{'mailbox_folders'}}, $folderName.'@'.$self->{'parent'}->getDesc('domain_name') );
                }
            }
        }
    }

    # Vacation message
    $userDesc->{'userobm_vacation_message'} = uri_unescape($userDesc->{'userobm_vacation_message'});

    # Hidden user
    if( $userDesc->{'userobm_hidden'} ) {
        $userDesc->{'userobm_hidden_access'} = 'TRUE';
    }else {
        $userDesc->{'userobm_hidden_access'} = 'FALSE';
    }

    # OBM Domain
    if( defined($self->{'parent'}) ) {
        $userDesc->{'userobm_obm_domain'} = $self->{'parent'}->getDesc('domain_name');
    }

    # OBM Domain alias
    my $domainAlias = $self->{'parent'}->getDesc('domain_alias');
    for(my $i=0; $i<=$#$domainAlias; $i++) {
        $userDesc->{'userobm_obm_domain_alias_'.($i+1)} = $domainAlias->[$i];
    }

    # Domain SID
    my $domainSid = $self->{'parent'}->getDesc('samba_sid');
    if( !$domainSid  && $userDesc->{'userobm_samba_perms'} ) {
        $self->_log( 'pas de SID associé au domaine '.$self->{'parent'}->getDescription(), 1 );
        $self->_log( 'droit samba annulé', 2 );
        $userDesc->{'userobm_samba_perms'} = 0;
    }

    # Web perms
    if( !$OBM::Parameters::common::obmModules->{web} ) {
        delete( $userDesc->{'userobm_web_perms'} );
    }

    # User web permission
    if( $userDesc->{'userobm_web_perms'} ) {
        $userDesc->{'userobm_web_access'} = 'PERMIT';
    }else {
        $userDesc->{'userobm_web_access'} = 'REJECT';
    }

    # Samba perms
    if( $OBM::Parameters::common::obmModules->{'samba'} && $userDesc->{'userobm_samba_perms'} ) {
        SWITCH: {
            # User Samba password
            if( $userDesc->{'userobm_password_type'} ne 'PLAIN' ) {
                $self->_log( 'le mot de passe doit être en \'PLAIN\' pour un utilisateur Samba, droit samba annulé pour \''.$userDesc->{'userobm_login'}.'\'', 1 );
                $userDesc->{'userobm_samba_perms'} = 0;
                last SWITCH;
            }

            my $error = $self->_getNTLMPasswd( $userDesc->{'userobm_password'}, \$userDesc->{'userobm_samba_lm_password'}, \$userDesc->{'userobm_samba_nt_password'} );
            if( $error ) {
                $self->_log( 'probleme lors de la generation du mot de passe windows de l\'utilisateur \''.$userDesc->{'userobm_login'}.'\', droit samba annulé', 1 );
                $userDesc->{'userobm_samba_perms'} = 0;
                last SWITCH;
            }else {
                $userDesc->{'userobm_pwd_lastset_time'} = time();
            }

            # User SID
            $userDesc->{'userobm_samba_sid'} = $self->_getUserSID( $domainSid, $userDesc->{'userobm_uid'} );
            # Group SID
            $userDesc->{'userobm_samba_group_sid'} = $self->_getGroupSID( $domainSid, $userDesc->{'userobm_gid'} );

            # Specific user UID
            if( $userDesc->{'group_gid'} ) {
                $userDesc->{'userobm_uid'} = 0;
            }

            # User Samba flags
            $userDesc->{'userobm_samba_flags'} = '[UX]';

            # User home share
            if( $userDesc->{'userobm_samba_home_drive'} ) {
                $userDesc->{'userobm_samba_home_drive'} =~ s/\s//g;
                if( $userDesc->{'userobm_samba_home_drive'} !~ /^[D-Z]$/ ) {
                    delete( $userDesc->{'userobm_samba_home_drive'} );
                    $self->_log( 'lettre du lecteur windows personnel incorrecte pour l\'utilisateur '.$userDesc->{'userobm_login'}, 1 );
                }
            }

            if( !$userDesc->{'userobm_samba_home_drive'} || !$userDesc->{'userobm_samba_home'} ) {
                delete( $userDesc->{'userobm_samba_home_drive'} );
                delete( $userDesc->{'userobm_samba_home'} );
            }else {
                $userDesc->{'userobm_samba_home_drive'} .= ':';
            }

            # W2k+ profil share (for older version, see smb.conf)
            if( $self->{'parent'}->getDesc('samba_user_profile') ) {
                $userDesc->{'userobm_samba_profile'} = $self->{'parent'}->getDesc('samba_user_profile');
                $userDesc->{'userobm_samba_profile'} =~ s/\%u/$userDesc->{'userobm_login_new'}/g;
            }
        }
    }else {
        $userDesc->{'userobm_samba_perms'} = 0;
    }

    if(!$userDesc->{'userobm_samba_perms'}) {
        delete( $userDesc->{'userobm_samba_home_drive'} );
        delete( $userDesc->{'userobm_samba_home'} );
        delete( $userDesc->{'userobm_samba_logon_script'} );
    }


    $self->{'entityDesc'} = $userDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 3 );

    return 0;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmuser when
# adding new value into description
sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'HASH' ) {
        $self->_log( 'pas de liens définis', 1 );
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

    if( $self->getUpdateEntity() ) {
        # Vérification des objectclass
        my $deletedObjectclass;
        my $currentObjectclass = $self->_getLdapObjectclass( $entry->get_value('objectClass', asref => 1), \$deletedObjectclass );
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

        if( defined($self->{'entityDesc'}->{'userobm_samba_sid'}) ) {
            my @currentLdapUserSambaSid = $entry->get_value( 'sambaSID', asref => 1 );
            if( $#currentLdapUserSambaSid < 0 ) {
                # This updates must be done only if samba user right is
                # re-enable.

                # If LDAP sambaSID attribute isn't set, but 'userobm_samba_sid'
                # is defined in current user description, samba perms is
                # re-enable, we must put 'sambaLMPassword' and
                # 'sambaNTPassword'...
                if( $self->setLdapSambaPasswd( $entry ) ) {
                    $update = 1;
                }

                # ...and 'updateEnableSamba' scope...
                my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateEnableSamba'} );
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
        }

        my @exceptions;
        push( @exceptions, @{$self->{'ldapMappingScope'}->{'updateEnableSamba'}}, @{$self->{'ldapMappingScope'}->{'updateSambaPasswd'}}, @{$self->{'ldapMappingScope'}->{'updateUnixPasswd'}}, @{$self->{'ldapMappingScope'}->{'updateLinks'}});
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

    return 'user/';
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
        $self->_log( $self->getDescription().' : message d\'absence désactivé', 3 );
        return undef;
    }

    # If no vacation message, then no vacation message...
    if( !$self->{'entityDesc'}->{'userobm_vacation_message'} ) {
        $self->_log( $self->getDescription().' : message d\'absence vide !', 2 );
        return undef;
    }

    my $boxEmails = $self->{'entityDesc'}->{'userobm_main_email'};
    my $boxEmailsAlias = $self->{'entityDesc'}->{'userobm_alias_email'};

    # If no mail addess, then no vacation message
    if( ($#{$boxEmails} < 0) && ($#{$boxEmailsAlias} < 0) ) {
        $self->_log( $self->getDescription().' : pas d\'adresses mails défini, message d\'absence désactivé', 2 );
        return undef;
    }

    # does not answer vacation for mailing lists or SPAMS
    my $vacationMsg = 'if allof (
not header :contains "Precedence" ["bulk","list"],
not header :contains "X-Spam-Flag" "YES"
) {
  vacation :addresses [ ';

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

    $vacationMsg .= ' ] "' . $self->_escapeSieveVacationMessage($self->{'entityDesc'}->{'userobm_vacation_message'}) . '";
}';

    return $vacationMsg;
}

sub _escapeSieveVacationMessage() {
    my ($self, $message) = @_;

    $message =~ s/(?<!\\)\"/\\"/g;
    $message =~ s/(?<!\\)\\(?![\\\"])/\\\\/g;

    return $message;
}

sub getSieveNomade {
    my $self = shift;

    # If nomade not available, then no redirection
    if( !$self->{'entityDesc'}->{'userobm_nomade_perms'} ) {
        $self->_log( $self->getDescription().' : redirection de messagerie non autorisée', 3 );
        return undef;
    }

    # If nomade not enable, then no redirection
    if( !$self->{'entityDesc'}->{'userobm_nomade_enable'} ) {
        $self->_log( $self->getDescription().' : redirection de messagerie non activée', 3 );
        return undef;
    }

    # If redirection email isn't defined, then no redirection
    my @nomadeEmails = split(/\r\n/, $self->{'entityDesc'}->{'userobm_email_nomade'});
    my @validNomadeEmails;
    my $nomadeMsg;
    for( my $i=0; $i<=$#nomadeEmails; $i++ ) {
        if( $nomadeEmails[$i] =~ /$OBM::Parameters::regexp::regexp_email/i) {
            $nomadeMsg .= 'redirect "'.lc($nomadeEmails[$i]).'";'."\n";
        }
    }
    if(!$nomadeMsg) {
        $self->_log( $self->getDescription().' : adresse mail de redirection non définie', 2 );
        return undef;
    }


    if( !$self->{'entityDesc'}->{'userobm_nomade_local_copy'} ) {
        $nomadeMsg .= 'discard;';
        $nomadeMsg .= "\n".'stop;';
    }else {
        $nomadeMsg .= 'keep;';
    }

    return $nomadeMsg;
}


sub setLdapUnixPasswd {
    my $self = shift;
    my( $entry, $plainPasswd ) = @_;
    my $update = 0;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 1 );
        return undef;
    }

    if( !$OBM::Parameters::common::obmModules->{'ldap'} ) {
        $self->_log( 'module Ldap désactivé', 2 );
        return 0;
    }

    if( defined($plainPasswd) ) {
        $self->{'entityDesc'}->{'userobm_password'} = $plainPasswd;
        $self->{'entityDesc'}->{'userobm_password_crypt'} = $self->_convertPasswd( 'PLAIN', $self->{'entityDesc'}->{'userobm_password'} );
        if( !$self->{'entityDesc'}->{'userobm_password_crypt'} ) {
            $self->_log( 'echec de conversion du mot de passe Unix', 1 );
            return undef;
        }
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateUnixPasswd'} );
    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        my $ldapValue = $self->getDesc($attrsMapping->[$i]->{'desc'}->{'name'});
        if(!defined($ldapValue) && defined($attrsMapping->[$i]->{'desc'}->{'default'})) {
            $ldapValue = $attrsMapping->[$i]->{'desc'}->{'default'};
        }

        if( $self->_modifyAttr($ldapValue, $entry, $attrsMapping->[$i]->{'ldap'}->{'name'}) ) {
            $update = 1;
        }
    }

    return $update;
}


sub setLdapSambaPasswd {
    my $self = shift;
    my( $entry, $plainPasswd ) = @_; 
    my $update = 0;

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 1 );
        return undef;
    }

    if( !$OBM::Parameters::common::obmModules->{'samba'} ) {
        $self->_log( 'module Samba désactivé', 2 );
        return 0;
    }

    if( !$self->{'entityDesc'}->{'userobm_samba_perms'} ) {
        $self->_log( $self->getDescription().' n\'a pas le droit samba', 3 );
        return 0;
    }

    my $lmPasswd;
    my $ntPasswd;
    if( defined($plainPasswd) ) {
        if( $self->_getNTLMPasswd( $plainPasswd, \$lmPasswd, \$ntPasswd ) ) {
            $self->_log( 'probleme lors de la generation du mot de passe windows de \''.$self->getDescription().'\', droit samba annulé', 1 );
            return undef;
        }
    }else {
        $lmPasswd = $self->{'entityDesc'}->{'userobm_samba_lm_password'};
        $ntPasswd = $self->{'entityDesc'}->{'userobm_samba_nt_password'};
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $attrsMapping = $ldapMapping->getAttrsMapping( $self, $self->{'ldapMappingScope'}->{'updateSambaPasswd'} );
    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        my $ldapValue = $self->getDesc($attrsMapping->[$i]->{'desc'}->{'name'});
        if(!defined($ldapValue) && defined($attrsMapping->[$i]->{'desc'}->{'default'})) {
            $ldapValue = $attrsMapping->[$i]->{'desc'}->{'default'};
        }

        if( $self->_modifyAttr($ldapValue, $entry, $attrsMapping->[$i]->{'ldap'}->{'name'} ) ) {
            $update = 1;
        }
    }

    return $update;
}


sub updateLinkedEntities {
    my $self = shift;
    my( $updateType ) = @_;

    # Update linked entities on login update
    if( $self->{'entityDesc'}->{'userobm_login_current'} && ($self->{'entityDesc'}->{'userobm_login_new'} ne $self->{'entityDesc'}->{'userobm_login_current'}) ) {
        $self->_log( 'le login a été modifié, les entités liées doivent être mises à jour', 3 );
        return 1;
    }

    # Update linked entities on archive update
    if( !$self->getArchive() != !$self->{'entityDesc'}->{'user_obm_archive_current'} ) {
        $self->_log( 'changement d\'état d\'archivage de '.$self->getDescription().', les entités liées doivent être mises à jour', 3 );
        return 1;
    }

    if( !$self->{'entityDesc'}->{'userobm_mail_perms'} != !$self->{'entityDesc'}->{'userobm_mail_perms_current'} ) {
        $self->_log( 'changement d\'état du droit Mail de '.$self->getDescription().', les entités liées doivent être mises à jour', 3 );
        return 1;
    }

    if( !$self->{'entityDesc'}->{'userobm_samba_perms'} != !$self->{'entityDesc'}->{'userobm_samba_perms_current'} ) {
        $self->_log( 'changement d\'état du droit Samba de '.$self->getDescription().', les entités liées doivent être mises à jour', 3 );
        return 1;
    }

    $self->_log( 'pas de mise à jour des entités liés nécessaire pour '.$self->getDescription(), 4 );
    return 0;
}


sub smtpInUpdateMap {
    my $self = shift;

    # If entity is not updated (but only links)
    if( !$self->getUpdateEntity() ) {
        return 0;
    }

    return 1;
}
