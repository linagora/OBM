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


package OBM::Entities::obmPublicContacts;

$VERSION = '1.0';

use OBM::Entities::entities;
@ISA = ('OBM::Entities::entities');

$debug = 1;

use 5.006_001;
require Exporter;
use strict;

use OBM::Parameters::common;

use constant LDAPDN => 'mailServer';


# Needed
sub new {
    my $class = shift;
    my( $parent, $mailServerDesc ) = @_;

    my $self = bless { }, $class;

    if( ref($parent) ne 'OBM::Entities::obmDomain' ) {
        $self->_log( 'domaine père incorrect', 1 );
        return undef;
    }
    $self->setParent( $parent );

    if( $self->_init( $mailServerDesc ) ) {
        $self->_log( 'problème lors de l\'initialisation de la configuration des serveurs de courriers', 1 );
        return undef;
    }

    $self->{'objectclass'} = [ 'obmMailServer' ];

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
    my( $mailServerDesc ) = @_;

    if( !defined($mailServerDesc) || (ref($mailServerDesc) ne 'ARRAY') ) {
        $self->_log( 'description des serveurs de courriers incorrect', 1 );
        return 1;
    }

    push( @{$self->{'entityDesc'}->{'mailDomains'}}, $self->{'parent'}->getDesc('domain_name') );
    my $domainAlias = $self->{'parent'}->getDesc('domain_alias');
    for( my $i=0; $i<=$#$domainAlias; $i++ ) {
        push( @{$self->{'entityDesc'}->{'mailDomains'}}, $domainAlias->[$i] );
    }

    my %imapServer;
    my %imapServerId;
    my %smtpInServer;
    my %smtpInServerId;
    my %smtpOutServer;
    my %smtpOutServerId;
    for( my $i=0; $i<=$#$mailServerDesc; $i++ ) {
        my $currentSrv = $mailServerDesc->[$i];

        SWITCH: {
            if( $currentSrv->{'server_role'} =~ /^imap$/i ) {
                $imapServerId{$currentSrv->{'server_id'}} = '';
                $imapServer{$currentSrv->{'server_name'}} = '';
                last SWITCH;
            }

            if( $currentSrv->{'server_role'} =~ /^smtp_in$/i ) {
                $smtpInServerId{$currentSrv->{'server_id'}} = '';
                $smtpInServer{$currentSrv->{'server_name'}} = '';
                last SWITCH;
            }

            if( $currentSrv->{'server_role'} =~ /^smtp_out$/i ) {
                $smtpOutServerId{$currentSrv->{'server_id'}} = '';
                $smtpOutServer{$currentSrv->{'server_name'}} = '';
                last SWITCH;
            }
        }
    }

    @{$self->{'entityDesc'}->{'imapServerId'}} = keys(%imapServerId);
    @{$self->{'entityDesc'}->{'imapServer'}} = keys(%imapServer);
    if( $#{$self->{'entityDesc'}->{'imapServerId'}} < 0 ) {
        delete( $self->{'entityDesc'}->{'imapServerId'} );
        delete( $self->{'entityDesc'}->{'imapServer'} );
    }

    @{$self->{'entityDesc'}->{'smtpInServerId'}} = keys(%smtpInServerId);
    @{$self->{'entityDesc'}->{'smtpInServer'}} = keys(%smtpInServer);
    if( $#{$self->{'entityDesc'}->{'smtpInServerId'}} < 0 ) {
        delete( $self->{'entityDesc'}->{'smtpInServerId'} );
        delete( $self->{'entityDesc'}->{'smtpInServer'} );
    }

    @{$self->{'entityDesc'}->{'smtpOutServerId'}} = keys(%smtpOutServerId);
    @{$self->{'entityDesc'}->{'smtpOutServer'}} = keys(%smtpOutServer);
    if( $#{$self->{'entityDesc'}->{'smtpOutServerId'}} ) {
        delete( $self->{'entityDesc'}->{'smtpOutServerId'} );
        delete( $self->{'entityDesc'}->{'smtpOutServer'} );
    }

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

    my $description = 'configuration des serveurs de courriers du domaine '.$self->{'parent'}->getDesc('domain_name');

    return $description;
}


# Needed
sub getDomainId {
    my $self = shift;

    return 0;
}


# Needed
sub getId {
    my $self = shift;

    return 0;
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
        push( @dnPrefixes, 'cn='.LDAPDN.','.$rootDn->[$i] );
        $self->_log( 'DN de l\'entité : '.$dnPrefixes[$i], 3 );
    }

    return \@dnPrefixes;
}


# Needed by : LdapEngine
sub getCurrentDnPrefix {
    my $self = shift;

    return $self->getDnPrefix();
}


sub _getLdapObjectclass {
    my $self = shift;
    my ($objectclass, $deletedObjectclass) = @_;
    my %realObjectClass;

    if( !defined($objectclass) || (ref($objectclass) ne "ARRAY") ) {
        $objectclass = $self->{'objectclass'};
    }

    for( my $i=0; $i<=$#$objectclass; $i++ ) {
        $realObjectClass{$objectclass->[$i]} = 1;
    }

    my @realObjectClass = keys(%realObjectClass);

    return \@realObjectClass;
}


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

    $entry->add(
        objectClass => $self->_getLdapObjectclass(),
        cn => LDAPDN
    );

    # Mail domains
    if( $self->{'entityDesc'}->{'mailDomains'} ) {
        $entry->add( myDestination => $self->{'entityDesc'}->{'mailDomains'} );
    }

    # IMAP servers
    if( $self->{'entityDesc'}->{'imapServer'} ) {
        $entry->add( imapHost => $self->{'entityDesc'}->{'imapServer'} );
    }

    # SMTP-in servers
    if( $self->{'entityDesc'}->{'smtpInServer'} ) {
        $entry->add( smtpInHost => $self->{'entityDesc'}->{'smtpInServer'} );
    }

    # SMTP-out servers
    if( $self->{'entityDesc'}->{'smtpOutServer'} ) {
        $entry->add( smtpOutHost => $self->{'entityDesc'}->{'smtpOutServer'} );
    }

    # OBM domain
    if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
        $entry->add( obmDomain => $domainName );
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
        if( $self->_modifyAttrList( $currentObjectclass, $entry, 'objectClass' )) {
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

        # Mail domains
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'mailDomains'}, $entry, 'myDestination' ) ) {
            $update = 1;
        }

        # IMAP servers
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'imapServer'}, $entry, 'imapHost' ) ) {
            $update = 1;
        }

        # SMTP-in servers
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'smtpInServer'}, $entry, 'smtpInHost' ) ) {
            $update = 1;
        }

        # SMTP-out servers
        if( $self->_modifyAttrList( $self->{'entityDesc'}->{'smtpOutServer'}, $entry, 'smtpOutHost' ) ) {
            $update = 1;
        }

        # OBM domain
        if( defined($self->{'parent'}) && (my $domainName = $self->{'parent'}->getDesc('domain_name')) ) {
            if( $self->_modifyAttr( $domainName, $entry, 'obmDomain' ) ) {
                $update = 1;
            }
        }
    }

    return $update;
}


sub getImapServersIds {
    my $self = shift;

    return $self->{'entityDesc'}->{'imapServerId'};
}


sub getSmtpInServersIds {
    my $self = shift;

    return $self->{'entityDesc'}->{'smtpInServerId'};
}


sub getSmtpOutServersIds {
    my $self = shift;

    return $self->{'entityDesc'}->{'smtpOutServerId'};
}


sub smtpInUpdateMap {
    my $self = shift;

    # If entity is not updated (but only links)
    if( !$self->getUpdateEntity() ) {
        return 0;
    }

    return 1;
}
