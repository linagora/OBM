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


package OBM::Entities::obmMailshare;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;


sub new {
    my $class = shift;
    my( $parent, $mailshareDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 1 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $mailshareDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation du mailshare', 1 );
        return undef;
    }

    $self->{'ldapMappingScope'} = {
        'updateLinks' => []
    };

    return $self;
}


# Needed
sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'parent'} = undef;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmmailshare when
# adding new value into description
sub _init {
    my $self = shift;
    my( $mailshareDesc ) = @_;

    if( !defined($mailshareDesc) || (ref($mailshareDesc) ne 'HASH') ) {
        $self->_log( 'description du mailshare incorrect', 1 );
        return 1;
    }

    # L'ID du mailshare
    if( !defined($mailshareDesc->{'mailshare_id'}) ) {
        $self->_log( 'ID du mailshare non défini', 1 );
        return 1;
    }elsif( $mailshareDesc->{'mailshare_id'} !~ /$OBM::Parameters::regexp::regexp_id/ ) {
        $self->_log( 'ID \''.$mailshareDesc->{'mailshare_id'}.'\' incorrect', 1 );
        return 1;
    }

    # Le nom du mailshare
    if( !defined($mailshareDesc->{'mailshare_name'}) ) {
        $self->_log( 'Nom du mailshare non défini', 1 );
        return 1;
    }
    
    $mailshareDesc->{'mailshare_name_new'} = lc($mailshareDesc->{'mailshare_name'});
    if( $mailshareDesc->{'mailshare_name_new'} !~ /$OBM::Parameters::regexp::regexp_mailsharename/ ) {
        $self->_log( 'Nom du mailshare \''.$mailshareDesc->{'mailshare_name'}.'\' incorrect', 1 );
        return 1;
    }

    # Le nom actuel du mailshare, si définit
    if( defined($mailshareDesc->{'mailshare_name_current'}) ) {
        $mailshareDesc->{'mailshare_name_current'} = lc($mailshareDesc->{'mailshare_name_current'});
        if( !$mailshareDesc->{'mailshare_name_current'} || $mailshareDesc->{'mailshare_name_current'} !~ /$OBM::Parameters::regexp::regexp_mailsharename/ ) {
            $self->_log( 'Nom actuel du mailshare \''.$mailshareDesc->{'mailshare_name_current'}.'\' incorrect', 1 );
            return 1;
        }
    }else {
        $mailshareDesc->{'mailshare_name_current'} = $mailshareDesc->{'mailshare_name_new'};
    }

    # OBM Domain
    if( defined($self->{'parent'}) ) {
        $mailshareDesc->{'mailshare_obm_domain'} = $self->{'parent'}->getDesc('domain_name');
    }

    SWITCH: {
        require OBM::Cyrus::cyrusServers;
        my $cyrusSrvList = OBM::Cyrus::cyrusServers->instance();
        if( !(my $cyrusHostName = $cyrusSrvList->getCyrusServerIp( $mailshareDesc->{'mailshare_mail_server_id'}, $mailshareDesc->{'mailshare_domain_id'} ) ) ) {
            if( $mailshareDesc->{'mailshare_mail_perms'} ) {
                $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.' annulé, serveur Cyrus inconnu', 2 );
            }
            $mailshareDesc->{'mailshare_mail_perms'} = 0;
            $mailshareDesc->{'mailshare_mailperms_access'} = 'REJECT';
            last SWITCH;

        }else {
            $mailshareDesc->{'mailshare_mailperms_access'} = 'PERMIT';
            $mailshareDesc->{'mailshare_mail_server'} = 'lmtp:'.$cyrusHostName.':24';
            $mailshareDesc->{'mailshare_mail_perms'} = 1;
        }

        ( $mailshareDesc->{'mailshare_main_email'}, $mailshareDesc->{'mailshare_alias_email'} ) = $self->_makeEntityEmail( $mailshareDesc->{'mailshare_email'}, $self->{'parent'}->getDesc('domain_name'), $self->{'parent'}->getDesc('domain_alias') );
        if( !defined($mailshareDesc->{'mailshare_main_email'}) ) {
            $self->_log( 'droit mail du répertoire partagé d\'ID '.$mailshareDesc->{'mailshare_id'}.' annulé, pas d\'adresses mails valides', 2 );
            $mailshareDesc->{'mailshare_mail_perms'} = 1;
            $mailshareDesc->{'mailshare_mailperms_access'} = 'REJECT';
            delete($mailshareDesc->{'mailshare_main_email'});
            delete($mailshareDesc->{'mailshare_alias_email'});

            last SWITCH;
        }
    }

    # LDAP BAL destination
    $mailshareDesc->{'mailshare_ldap_mailbox'} = '+'.$mailshareDesc->{'mailshare_name_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Cyrus BAL destination
    $mailshareDesc->{'mailshare_cyrus_mailbox'} = $mailshareDesc->{'mailshare_name_new'}.'@'.$self->{'parent'}->getDesc('domain_name');
    # Current Cyrus BAL destination
    $mailshareDesc->{'current_mailshare_cyrus_mailbox'} = $mailshareDesc->{'mailshare_name_current'}.'@'.$self->{'parent'}->getDesc('domain_name');

    # Cyrus partition
    if( $OBM::Parameters::common::cyrusDomainPartition ) {
        $mailshareDesc->{'mailshare_cyrus_partition'} = $self->{'parent'}->getDesc('domain_name');
        $mailshareDesc->{'mailshare_cyrus_partition'} =~ s/\./_/g;
        $mailshareDesc->{'mailshare_cyrus_partition'} =~ s/-/_/g;
    }

    # Cyrus quota
    $mailshareDesc->{'mailshare_cyrus_quota'} = 0 if !defined( $mailshareDesc->{'mailshare_cyrus_quota'} );
    $mailshareDesc->{'mailshare_cyrus_quota'} = $mailshareDesc->{'mailshare_quota'}*1024;

    # BAL sub-folders
    if( defined($OBM::Parameters::common::shareMailboxDefaultFolders) ) {
        foreach my $folderTree ( split( ',', $OBM::Parameters::common::shareMailboxDefaultFolders ) ) {
            if( $folderTree !~ /(^[",]$)|(^$)/ ) {
                my $folderName = $mailshareDesc->{'mailshare_name_new'};
                foreach my $folder ( split( '/', $folderTree ) ) {
                    $folder =~ s/^\s+//;

                    $folderName .= '/'.$folder;
                    push( @{$mailshareDesc->{mailbox_folders}}, $folderName.'@'.$self->{'parent'}->getDesc('domain_name') );
                }
            }
        }
    }

    $self->{'entityDesc'} = $mailshareDesc;

    $self->_log( 'chargement : '.$self->getDescription(), 3 );

    return 0;
}


# Update http://obm.org/doku.php?id=specification:auto:ldapmapping:obmmailshare when
# adding new value into description
sub setLinks {
    my $self = shift;
    my( $links ) = @_;

    if( !defined($links) || ref($links) ne 'HASH' ) {
        $self->_log( 'pas de liens définis', 1 );
        return 0;
    }

    $self->{'entityDesc'}->{'mailshare_acl'} = $links;

    return 0;
}


# Needed
sub getDescription {
    my $self = shift;
    my $mailshareDesc = $self->{'entityDesc'};

    my $description = 'mailshare d\'ID \''.$mailshareDesc->{'mailshare_id'}.'\', nom \''.$mailshareDesc->{'mailshare_name'}.'\'';

    return $description;
}


# Needed
sub getDesc {
    my $self = shift;
    my( $desc ) = @_;

    if( $desc && !ref($desc) ) {
        return $self->{'entityDesc'}->{$desc};
    }

    return undef;
}


# Needed
sub getDomainId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_domain_id'};
}


# Needed
sub getId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_id'};
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

    return $update;
}


# Needed
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

    return $self->{'entityDesc'}->{'mailshare_mail_perms'};
}


# Needed : cyrusEngine
sub getMailboxName {
    my $self = shift;
    my( $which ) = @_;
    my $mailShareName = undef;

    if( lc($which) =~ /^new$/ ) {
        $mailShareName = $self->{'entityDesc'}->{'mailshare_cyrus_mailbox'};
    }elsif( lc($which) =~ /^current$/ ) {
        $mailShareName = $self->{'entityDesc'}->{'current_mailshare_cyrus_mailbox'};
    }

    return $mailShareName;
}


# Needed : cyrusEngine
sub getMailServerId {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_mail_server_id'};
}


# Needed : cyrusEngine
sub getMailboxPrefix {
    my $self = shift;

    return '';
}


# Needed : cyrusEngine
sub getMailboxQuota {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_cyrus_quota'};
}


# Needed : cyrusEngine
sub getMailboxAcl {
    my $self = shift;
    my $mailBoxAcl = undef;

    if( !$self->getArchive() && $self->isMailActive() ) {
        $mailBoxAcl = $self->{'entityDesc'}->{'mailshare_acl'};
    }

    return $mailBoxAcl;
}


# Needed : cyrusEngine
sub getMailboxPartition {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailshare_cyrus_partition'};
}


# Needed : cyrusEngine
sub getMailboxDefaultFolders {
    my $self = shift;

    return $self->{'entityDesc'}->{'mailbox_folders'};
}


sub smtpInUpdateMap {
    my $self = shift;

    # If entity is not updated (but only links)
    if( !$self->getUpdateEntity() ) {
        return 0;
    }

    return 1;
}
