package OBM::Ldap::sambaUtils;

require Exporter;

use OBM::Parameters::common;
use strict;


sub getUserSID {
    my( $SID, $userUID ) = @_;

    if( !defined($SID) || !$SID ) {
        return undef;
    }

    my $userSID;

    SWITCH: {
        # Si nouvelle génération du SID et UID=0
        if( !$sambaOldSidMapping && ($userUID == 0) ) {
            $userSID = $SID."-500";
            last SWITCH;
        }

        # Si nouvelle génération du SID
        if( !$sambaOldSidMapping ) {
            $userSID = $SID."-".$userUID;
            last SWITCH;
        }

        # Si ancienne génération du SID et UID=0
        if( $sambaOldSidMapping && ($userUID == 0) ) {
            $userSID = $SID."-2996";
            last SWITCH;
        }

        # Si ancienne génération du SID
        if( $sambaOldSidMapping ) {
            $userSID = $SID."-".(2*$userUID+1000);
            last SWITCH;
        }

        $userSID = undef;
    }

    return $userSID;
}


sub getGroupSID {
    my( $SID, $groupGID ) = @_;

    if( !defined( $SID ) || !$SID ) {
        return undef;
    }

    my $groupSID = "";

    SWITCH: {
        # Groupe des administrateurs
        if( $groupGID == 512 ) {
            $groupSID = $SID."-512";
            last SWITCH;
        }

        # Groupe des utilisateurs
        if( $groupGID == 513 ) {
            $groupSID = $SID."-513";
            last SWITCH;
        }

        # Groupe des invites
        if( $groupGID == 514 ) {
            $groupSID = $SID."-514";
            last SWITCH;
        }

        # Si nouvelle génération du SID
        if( !$sambaOldSidMapping ) {
            $groupSID = $SID."-".$groupGID;
        }

        # Si ancienne génération du SID
        if( $sambaOldSidMapping ) {
            $groupSID = $SID."-".(2*$groupGID+1001);
        }

        $groupSID = undef;
    }

    return $groupSID;
}
