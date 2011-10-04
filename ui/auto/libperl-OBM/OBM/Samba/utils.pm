package OBM::Samba::utils;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);

use OBM::Parameters::common;

$VERSION = "1.0";
@EXPORT_OK = qw(    _getUserSID
                    _getGroupSID
               );


sub _getUserSID {
    my $self = shift;
    my( $domainSid, $userUID ) = @_;

    if( !$domainSid || !defined($userUID) ) {
        return undef;
    }

    my $userSID;

    SWITCH: {
        # Si nouvelle génération du SID
        if( !$OBM::Parameters::common::sambaOldSidMapping ) {
            $userSID = $domainSid.'-'.$userUID;
            last SWITCH;
        }

        # Si ancienne génération du SID
        if( $OBM::Parameters::common::sambaOldSidMapping ) {
            $userSID = $domainSid.'-'.(2*$userUID+1000);
            last SWITCH;
        }

        $userSID = undef;
    }

    return $userSID;
}


sub _getGroupSID {
    my $self = shift;
    my( $domainSid, $groupGID ) = @_;

    if( !$domainSid || !defined($groupGID) ) {
        return undef;
    }

    my $groupSID = '';

    SWITCH: {
        # Groupe des administrateurs
        if( $groupGID == 512 ) {
            $groupSID = $domainSid.'-512';
            last SWITCH;
        }

        # Groupe des utilisateurs
        if( $groupGID == 513 ) {
            $groupSID = $domainSid.'-513';
            last SWITCH;
        }

        # Groupe des invites
        if( $groupGID == 514 ) {
            $groupSID = $domainSid.'-514';
            last SWITCH;
        }

        # Groupe des hôtes
        if( $groupGID == 515 ) {
            $groupSID = $domainSid.'-515';
            last SWITCH;
        }

        # Si nouvelle génération du SID
        if( !$OBM::Parameters::common::sambaOldSidMapping ) {
            $groupSID = $domainSid.'-'.$groupGID;
            last SWITCH;
        }

        # Si ancienne génération du SID
        if( $OBM::Parameters::common::sambaOldSidMapping ) {
            $groupSID = $domainSid.'-'.(2*$groupGID+1001);
            last SWITCH;
        }

        $groupSID = undef;
    }

    return $groupSID;
}
