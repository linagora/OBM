#!/bin/sh

PERL='/usr/bin/perl'
MAKE='/usr/bin/make'
TMP=$1
DIRPM=$2

cd ${DIRPM}

case "$3" in
	makefile-pl)
	${PERL} Makefile.PL INSTALLDIRS=vendor INSTALLVENDORARCH=/usr/share/perl5/ VENDORARCHEXP=/usr/share/perl5/
	;;
	make)
	${MAKE}
	;;
	make-install)
	${MAKE} install DESTDIR=$1 PREFIX=/usr
	;;
	make-clean)
	${MAKE} clean
	;;
esac


#cd toto
#${PERL} Makefile.PL INSTALLDIRS=vendor INSTALLVENDORARCH=/usr/share/perl5/ VENDORARCHEXP=/usr/share/perl5/ && ${MAKE} 

exit 0
 
