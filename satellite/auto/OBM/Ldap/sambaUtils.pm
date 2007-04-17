package OBM::Ldap::sambaUtils;

require Exporter;

use Crypt::SmbHash;
use strict;


sub getUserSID {
    my( $SID, $userUID ) = @_;

    if( !defined($SID) || !$SID ) {
        return undef;
    }

    my $userSID = "";

    SWITCH: {
        if( $userUID == 0 ) {
            $userSID = $SID."-2996";
            last SWITCH;
        }

        $userSID = $SID."-".(2*$userUID+1000);
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

        $groupSID = $SID."-".(2*$groupGID+1001);
    }

    return $groupSID;
}


sub getNTLMPasswd {
    my( $plainPasswd, $lmPasswd, $ntPasswd ) = @_;

    if( !defined($plainPasswd) ) {
        return 1;
    }

    ( $$lmPasswd, $$ntPasswd ) = ntlmgen( $plainPasswd );

    return 0;
}
