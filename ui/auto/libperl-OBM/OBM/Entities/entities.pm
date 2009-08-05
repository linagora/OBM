package OBM::Entities::entities;

$VERSION = '1.0';

use OBM::Tools::commonMethods;
use OBM::Ldap::utils;
use OBM::Samba::utils;
use OBM::Password::passwd;
use OBM::Entities::Interfaces::entitiesSmtpIn;
@ISA = ('OBM::Tools::commonMethods', 'OBM::Ldap::utils', 'OBM::Samba::utils', 'OBM::Password::passwd', 'OBM::Entities::Interfaces::entitiesSmtpIn');

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

    $self->_log( 'suppression de l\'objet', 4 );

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
        $self->_log( 'description du domaine parent incorrecte', 3 );
        return 1;
    }

    $self->{'parent'} = $parent;

    return 0;
}


sub _getParentDn {
    my $self = shift;

    return undef;
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
        $self->_log( 'DN de la racine du domaine parent non déterminée', 3 );
        return undef;
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $rdnMapping = $ldapMapping->getRdn($self);
    if( !defined($rdnMapping) ) {
        $self->_log( 'mapping du RDN de l\'entité '.$self->getDescription().' incorrect', 2 );
        return undef;
    }

    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, $rdnMapping->{'ldap'}->{'name'}.'='.$self->getDesc( $rdnMapping->{'desc'}->{'name'} ).','.$rootDn->[$i] );
        $self->_log( 'nouveau DN de l\'entité : '.$dnPrefixes[$i], 4 );
    }

    return \@dnPrefixes;
}


sub getCurrentDnPrefix {
    my $self = shift;
    my $rootDn;
    my @dnPrefixes;

    if( !($rootDn = $self->_getParentDn()) ) {
        $self->_log( 'DN de la racine du domaine parent non déterminée', 3 );
        return undef;
    }

    require OBM::Ldap::ldapMapping;
    my $ldapMapping = OBM::Ldap::ldapMapping->instance();
    my $rdnMapping = $ldapMapping->getCurrentRdn($self);
    if( !defined($rdnMapping) ) {
        $self->_log( 'mapping du RDN de l\'entité '.$self->getDescription().' incorrect', 2 );
        return undef;
    }


    for( my $i=0; $i<=$#{$rootDn}; $i++ ) {
        push( @dnPrefixes, $rdnMapping->{'ldap'}->{'name'}.'='.$self->getDesc( $rdnMapping->{'desc'}->{'name'} ).','.$rootDn->[$i] );
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


sub createLdapEntry {
    my $self = shift;

    return undef;
}


sub updateLdapEntry {
    my $self = shift;

    return undef;
}


sub _makeEntityEmail {
    require OBM::Parameters::regexp;
    my $self = shift;
    my( $mailAddress, $mainDomain, $domainAlias ) = @_;
    my $totalEmails = 0;
    my %emails;
    my %emailsAlias;

    if( !$mailAddress ) {
        $self->_log( 'pas d\'adresses mails définis', 3 );
        return $totalEmails;
    }

    if( !$mainDomain ) {
        $self->_log( 'pas de domaine principal défini', 3 );
        return $totalEmails;
    }

    if( ref($domainAlias) ne 'ARRAY' ) {
        $self->_log( 'pas d\'alias de domaine définis', 3 );
        $domainAlias = undef;
    }

    my @email = split( /\r\n/, $mailAddress );
    
    for( my $i=0; $i<=$#email; $i++ ) {
        $email[$i] = lc($email[$i]);

        SWITCH: {
            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email/ ) {
                $emails{$email[$i]} = 1;
                $totalEmails++;
                last SWITCH;
            }

            if( $email[$i] =~ /$OBM::Parameters::regexp::regexp_email_left/ ) {
                $emails{$email[$i].'@'.$mainDomain} = 1;
                $totalEmails++;

                for( my $j=0; $j<=$#{$domainAlias}; $j++ ) {
                    $emailsAlias{$email[$i].'@'.$domainAlias->[$j]} = 1;
                    $totalEmails++;
                }

                last SWITCH;
            }
        }
    }

    my @emails = keys(%emails);
    if( $#emails >= 0 ) {
        $self->{'email'} = \@emails;
    }

    my @emailsAlias = keys(%emailsAlias);
    if( $#emailsAlias >= 0 ) {
        $self->{'emailAlias'} = \@emailsAlias;
    }

    return $totalEmails;
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
