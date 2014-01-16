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


package OBM::Password::passwd;


$debug = 1;


use 5.006_001;
use strict;
use vars qw( @EXPORT_OK $VERSION );
use base qw(Exporter);

use MIME::Base64;
use Digest::SHA;
use Digest::MD5;
use Crypt::SmbHash;

$VERSION = "1.0";
@EXPORT_OK = qw(
                    _convertPasswd
                    _getNTLMPasswd
               );



sub _md5sumToMd5 {
    my $self = shift;
    my( $passwdMd5sum ) = @_;

    return encode_base64( pack( "H*", $passwdMd5sum ) );
}

sub _toSsha {
    my $self = shift;
    my( $passwdPlain ) = @_;

    my $salt = join '', ('a'..'z')[rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26,rand 26];
    my $cryptPass = Digest::SHA->new;

    $cryptPass->add( $passwdPlain );
    $cryptPass->add( $salt );

    return encode_base64($cryptPass->digest . $salt,'');
}


sub _convertPasswd {
    my $self = shift;
    my( $passwdType, $passwd ) = @_;
    my $userPasswd = undef;

    if( !defined($passwdType) || !defined($passwd) ) {
        return undef;
    }

    SWITCH: {
        if( uc($passwdType) eq 'PLAIN' ) {
            $userPasswd = '{SSHA}'.$self->_toSsha( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq 'MD5SUM' ) {
            $userPasswd = '{MD5}'.$self->_md5sumToMd5( $passwd );
            last SWITCH;
        }

        if( uc($passwdType) eq 'CRYPT' ) {
            $userPasswd = '{CRYPT}'.$passwd;
            last SWITCH;
        }
    }

    return $userPasswd;
}


sub _getNTLMPasswd {
    my $self = shift;
    my( $plainPasswd, $lmPasswd, $ntPasswd ) = @_;

    if( !defined($plainPasswd) ) {
        return 1;
    }

    ( $$lmPasswd, $$ntPasswd ) = ntlmgen( $plainPasswd );

    return 0;
}
