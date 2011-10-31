#!/bin/bash
#+-------------------------------------------------------------------------+
#|   Copyright (c) 1997-2009 OBM.org project members team                  |
#|                                                                         |
#|  This program is free software; you can redistribute it and/or          |
#|  modify it under the terms of the GNU General Public License            |
#|  as published by the Free Software Foundation; version 2                |
#|  of the License.                                                        |
#|                                                                         |
#|  This program is distributed in the hope that it will be useful,        |
#|  but WITHOUT ANY WARRANTY; without even the implied warranty of         |
#|  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the          |
#|  GNU General Public License for more details.                           | 
#+-------------------------------------------------------------------------+
#|  http://www.obm.org                                                     |
#+-------------------------------------------------------------------------+
function gencertif {
  host=`hostname -f`
  cat >> /etc/obm-locator/${host}_ssl.cnf <<EOF
[ req ]
default_bits = 2048
encrypt_key = yes
distinguished_name = req_dn
x509_extensions = cert_type
prompt = no

[ req_dn ]
O=obm.org
OU=locator
CN=${host}

[ cert_type ]
nsCertType = server
EOF
  openssl req -new -x509 -days 3650 -passout pass:password -config /etc/obm-locator/${host}_ssl.cnf \
-out /etc/obm-locator/${host}_cert.pem -keyout /etc/obm-locator/${host}_key.pem

  rm -f /etc/obm-locator/${host}_ssl.cnf
  rm -f /etc/obm-locator/locator.p12
  openssl pkcs12  -password pass:password -export -in /etc/obm-locator/${host}_cert.pem -inkey /etc/obm-locator/${host}_key.pem -out /etc/obm-locator/locator.p12 -passin pass:password -name "locator"
}


gencertif

exit 0


