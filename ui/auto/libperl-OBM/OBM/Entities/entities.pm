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


package OBM::Entities::entities;

$VERSION = '1.0';

use OBM::Log::log;
use OBM::Ldap::utils;
use OBM::Samba::utils;
use OBM::Password::passwd;
use OBM::Entities::Interfaces::entitiesSmtpIn;
@ISA = ('OBM::Log::log', 'OBM::Ldap::utils', 'OBM::Samba::utils', 'OBM::Password::passwd', 'OBM::Entities::Interfaces::entitiesSmtpIn');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;


sub _init {
    my $self = shift;
    my( $entityDesc ) = @_;

    return 1;
}


sub DESTROY {
    my $self = shift;

    $self->_log( 'suppression de l\'objet', 5 );

    $self->{'parent'} = undef;
}


sub getDescription {
    my $self = shift;

    my $description = 'entity ID \''.$self->getId().'\'';

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


sub getDomainId {
    my $self = shift;

    return undef;
}


sub getId {
    my $self = shift;

    return undef;
}


sub getLdapServerId {
    my $self = shift;

    return undef;
}


# Needed
sub setParent {
    my $self = shift;
    my( $parent ) = @_;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'description du domaine parent incorrecte', 0 );
        return 1;
    }

    $self->{'parent'} = $parent;

    return 0;
}


sub _getParentDn {
    my $self = shift;

    my $parentDn = undef;

    if( defined($self->{'parent'}) ) {
        $parentDn = $self->{'parent'}->getDnPrefix($self);
    }

    return $parentDn;
}


sub setDelete {
    my $self = shift;

    $self->{'toDelete'} = 1;

    return 0;
}


sub getDelete {
    my $self = shift;

    return $self->{'toDelete'};
}


sub setArchive {
    my $self = shift;

    $self->{'archive'} = 1;

    return 0;
}


sub getArchive {
    my $self = shift;

    if( !defined($self->{'archive'}) ) {
        $self->{'archive'} = 0;
    }

    return $self->{'archive'};
}


sub getParent {
    my $self = shift;

    return $self->{'parent'};
}


# Set entity to be updated in BD if it's system update is ok
sub setBdUpdate {
    my $self = shift;

    $self->{'disableDbupdate'} = 0;

    return 0;
}


# Set entity to be updated in BD whatever it's system update is ok or not
sub unsetBdUpdate {
    my $self = shift;

    $self->{'disableDbupdate'} = 1;

    return 0;
}


# Is entity can be updated in BD ?
sub getBdUpdate {
    my $self = shift;

    return !$self->{'disableDbupdate'};
}


# Set that entity system update is ok
sub setUpdated {
    my $self = shift;

    $self->{'updated'} = 1;

    return 0;
}


# Set that entity system update isn't ok
sub unsetUpdated {
    my $self = shift;

    $self->{'updated'} = 0;

    return 0;
}


# Is entity system update ok or not ?
sub getUpdated {
    my $self = shift;

    return $self->{'updated'};
}


sub getDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 0 );
        return undef;
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $rdnMapping = $ldapMapping->getRdn($self);
    if( !defined($rdnMapping) ) {
        $self->_log( 'mapping du RDN de l\'entité '.$self->getDescription().' incorrect', 0 );
        return undef;
    }

    my $rdnDescValue = $self->getDesc( $rdnMapping->{'desc'}->{'name'} );
    return undef if !defined($rdnDescValue);

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, $rdnMapping->{'ldap'}->{'name'}.'='.$rdnDescValue.','.$rootDn->[$i] );
        $self->_log( 'nouveau DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub getCurrentDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 0 );
        return undef;
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $rdnMapping = $ldapMapping->getCurrentRdn($self);
    if( !defined($rdnMapping) ) {
        $self->_log( 'mapping du RDN de l\'entité '.$self->getDescription().' incorrect', 0 );
        return undef;
    }

    my $rdnDescValue = $self->getDesc( $rdnMapping->{'desc'}->{'name'} );
    if( !defined($rdnDescValue) ) {
        $rdnMapping = $ldapMapping->getRdn($self);
        $rdnDescValue = $self->getDesc( $rdnMapping->{'desc'}->{'name'} );
        return undef if !defined($rdnDescValue);
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, $rdnMapping->{'ldap'}->{'name'}.'='.$rdnDescValue.','.$rootDn->[$i] );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub _getLdapObjectclass {
    my $self = shift;
    my ($objectclass, $deletedObjectclass) = @_;

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();

    my $newObjectClass = $ldapMapping->getObjectClass($self, $objectclass);

    $$deletedObjectclass = $newObjectClass->{'deletedObjectclass'};
    return $newObjectClass->{'objectClass'};
}


# Needed by : LdapEngine
sub createLdapEntry {
    my $self = shift;
    my ( $entryDn, $entry ) = @_;

    if( !$entryDn ) {
        $self->_log( 'DN non défini', 1 );
        return 1;
    }

    if( ref($entry) ne 'Net::LDAP::Entry' ) {
        $self->_log( 'entrée LDAP incorrecte', 1 );
        return 1;
    }

    $entry->add( objectClass => $self->_getLdapObjectclass() );

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();

    my $attrsMapping = $ldapMapping->getAllAttrsMapping( $self );
    for( my $i=0; $i<=$#{$attrsMapping}; $i++ ) {
        my $ldapValue = $self->getDesc($attrsMapping->[$i]->{'desc'}->{'name'});
        if(!defined($ldapValue) && defined($attrsMapping->[$i]->{'desc'}->{'default'})) {
            $ldapValue = $attrsMapping->[$i]->{'desc'}->{'default'};
        }
        $self->_modifyAttr($ldapValue, $entry, $attrsMapping->[$i]->{'ldap'}->{'name'});
    }


    return 0;
}


sub updateLdapEntry {
    my $self = shift;

    return undef;
}


sub _makeEntityEmail {
    require OBM::Parameters::regexp;
    my $self = shift;
    my( $mailAddress, $mainDomain, $domainAlias ) = @_;
    my %emails;
    my %emailsAlias;

    if( !$mailAddress ) {
        $self->_log( 'pas d\'adresses mails définis', 4 );
        return (undef, undef);
    }

    if( !$mainDomain ) {
        $self->_log( 'pas de domaine principal défini', 2 );
        return (undef, undef);
    }

    if( ref($domainAlias) ne 'ARRAY' ) {
        $self->_log( 'pas d\'alias de domaine définis', 2 );
        $domainAlias = undef;
    }

    my @email = split( /\r\n/, $mailAddress );
    
    for( my $i=0; $i<=$#email; $i++ ) {
        $email[$i] = lc($email[$i]);

        SWITCH: {
            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email/ ) {
                if( $i == 0 ) {
                    $emails{$email[$i]} = 1;
                }else {
                    $emailsAlias{$email[$i]} = 1;
                }
                last SWITCH;
            }

            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email_left/ ) {
                if( $i == 0 ) {
                    $emails{$email[$i].'@'.$mainDomain} = 1;
                }else {
                    $emailsAlias{$email[$i].'@'.$mainDomain} = 1;
                }

                for( my $j=0; $j<=$#{$domainAlias}; $j++ ) {
                    $emailsAlias{$email[$i].'@'.$domainAlias->[$j]} = 1;
                }

                last SWITCH;
            }
        }
    }

    my @emails = keys(%emails);
    my $emails = undef;
    if( $#emails >= 0 ) {
        $emails = \@emails;
    }

    my @emailsAlias = keys(%emailsAlias);
    my $emailsAlias = undef;
    if( $#emailsAlias >= 0 ) {
        $emailsAlias = \@emailsAlias;
    }

    return ( $emails, $emailsAlias );
}


# Update entity informations
sub setUpdateEntity {
    my $self = shift;

    $self->{'update'}->{'entity'} = 1;
}


# Get update entity informations state
sub getUpdateEntity {
    my $self = shift;

    return $self->{'update'}->{'entity'};
}


# Set force links update flag
sub setForceLoadEntityLinks {
    my $self = shift;

    $self->{'forceLoadEntityLinks'} = 1;
}


# Get force links update flag
sub getForceLoadEntityLinks {
    my $self = shift;

    if(!$self->{'forceLoadEntityLinks'}) {
        return 0;
    }

    return 1;
}


# Update entity links
sub setUpdateLinks {
    my $self = shift;

    $self->{'update'}->{'links'} = 1;
}


# Get update entity links state
sub getUpdateLinks {
    my $self = shift;

    return $self->{'update'}->{'links'};
}


# Get if the entity can have mail permission
sub isMailAvailable {
    my $self = shift;

    return 0;
}


# Needed : sieveEngine
# Get if the entity can have mail permission
sub isSieveAvailable {
    my $self = shift;

    return 0;
}


# Set mailbox quota used
sub setCyrusQuotaUsed {
    my $self = shift;
    my( $quotaUsed ) = @_;

    if( $quotaUsed !~ /^\d+$/ ) {
        $self->_log( 'quota utilisé incorrect : '.$quotaUsed, 0 );
        return 1;
    }

    $self->{'mail_quota_used'} = $quotaUsed;

    return 0;
}


# Get mailbox quota used
sub getCyrusQuotaUsed {
    my $self = shift;
    my $quotaUsed = 0;

    if( defined($self->{'mail_quota_used'}) ) {
        $quotaUsed = $self->{'mail_quota_used'};
    }

    return $quotaUsed;
}


# Needed : cyrusEngine
sub getMailboxName {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailServerId {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailboxPrefix {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailboxQuota {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailboxAcl {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailboxPartition {
    my $self = shift;

    return undef;
}


# Needed : cyrusEngine
sub getMailboxDefaultFolders {
    my $self = shift;

    return undef;
}


# Needed : sieveEngine
sub getSieveVacation {
    my $self = shift;

    return undef;
}


# Needed : sieveEngine
sub getSieveNomade {
    my $self = shift;

    return undef;
}


sub setLdapUnixPasswd {
    my $self = shift;
    my( $entry, $plainPasswd ) = @_;

    return 0;
}


sub setLdapSambaPasswd {
    my $self = shift;
    my( $entry, $plainPasswd ) = @_; 

    return 0;
}


sub updateLinkedEntities {
    my $self = shift;

    return 0;
}


sub smtpInUpdateMap {
    my $self = shift;

    return 0;
}


# Needed to add entity extra description to the entity description.
# Extra description can be used on ldapMapping using it's category name.
# WARNING: if extra description is multi-valued, check that corresponding LDAP
# attribute is multi-valued in ldapMapping XML configuration file too !
#
#  - extraDescription : hash reference. Keys are extra description name, values is
#    an extra description values array reference
sub setExtraDescription {
    my $self = shift;
    my( $extraDescription ) = @_;

    if( ref($extraDescription) ne 'HASH' ) {
        $self->_log( 'Parametre \'extraDescription\' invalide. Doit être une référence à un HASH', 4 );
        return 0;
    }

    while( my($extraDescriptionName, $extraDescriptionValues) = each(%{$extraDescription}) ) {
        if( ref($extraDescriptionValues) ne 'ARRAY' ) {
            $self->_log( 'Valeurs incorrectes pour la catégorie \''.$extraDescriptionName.'\'', 2 );
            next;
        }

        if( exists($self->{'entityDesc'}->{$extraDescriptionName}) ) {
            $self->_log( 'Le nom de la catégorie \''.$extraDescriptionName.'\' est déjà utilisé dans la description de '.$self->getDescription(), 2 );
            next;
        }

        $self->_log( 'Definition de la catégorie \''.$extraDescriptionName.'\'', 5 );
        $self->{'entityDesc'}->{$extraDescriptionName} = $extraDescriptionValues;
    }

    return 1;
}
