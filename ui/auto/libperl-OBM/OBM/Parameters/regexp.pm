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


#########################################################################
# OBM       - file : OBM::Parameters::regexp (Perl Module)              #
#           - Desc : librairie des expressions rationnelles Perl pour   #
#                    OBM                                                #
#########################################################################
package OBM::Parameters::regexp;

require Exporter;
require OBM::Parameters::common;


@ISA = qw(Exporter);
@EXPORT_regexp = qw(
    $regexp_id
    $regexp_domain
    $regexp_email
    $regexp_email_left
    $regexp_email_right
    $regexp_rootLdap
    $regexp_login
    $regexp_passwd
    $regexp_ip
    $regexp_server_id
    $regexp_uid
    $regexp_hostname
    $regexp_groupname
    $regexp_mailsharename
    );
@EXPORT = (@EXPORT_regexp);


# Regexp generic
$regexp_id = '^[0-9]+$';

# Domain regexp
$regexp_domain = '^([a-z0-9-]+\.)+[a-z]{2,12}$';

# Email
$regexp_email = '^[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*@([a-z0-9-]+\.)+[a-z]{2,12}$';
$regexp_email_left = '^[a-z0-9!#$%&\'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&\'*+/=?^_`{|}~-]+)*$';
$regexp_email_right = $regexp_domain;

# LDAP root
$regexp_rootLdap = "^dc=(.+),dc=.+\$";

# Login regexp
$regexp_login = "^([a-z0-9][a-z0-9-._]{0,256})\$";
if( $OBM::Parameters::common::obmModules->{'samba'} ) {
    $regexp_login = "^([a-z0-9][a-z0-9-._]{0,31})\$";
}

# Passwd regexp
$regexp_passwd = '^[-\$\\\&~#\{\(\[\|_`\^@\);\]+=\}%!:\/\.,?<>"\w0-9]{4,20}$';
if( $OBM::Parameters::common::obmModules->{'samba'} ) {
    $regexp_passwd = '^[-\$\\\&~#\{\(\[\|_`\^@\);\]+=\}%!:\/\.,?<>"\w0-9]{4,12}$';
}

# Les adresses IP
$regexp_ip = '^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$';

# Server regexp
$regexp_server_id = '^[0-9]+$';

# User regexp
$regexp_uid = '^[0-9]+$';

# Host
$regexp_hostname = '^[A-Za-z0-9][A-Za-z0-9-]{0,30}[A-Za-z0-9]$';

# Group
$regexp_groupname = '^[\W\w0-9]([\W\w0-9-._ ]{0,252}[\W\w0-9]){0,1}$';

# Mailshare
$regexp_mailsharename = $regexp_login;
