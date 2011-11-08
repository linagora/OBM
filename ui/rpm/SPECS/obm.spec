Name:           obm
Version:        %{obm_version}
Release:        %{obm_release}%{?dist}
Summary:        Open Business Management

Group:          Development/Languages
License:        AGPLv3
URL:            http://www.obm.org
Source0:        %{name}-%{version}.tar.gz
#Source1:        %{name}-httpd.conf
Source2:	%{name}-core.cron.d
Source3:	%{name}-admin.sh
Source4:	%{name}-config.sh
Source5:	%{name}-ldap.sh
Source6:	%{name}-mysql.sh
#Source7:	%{name}-satellite.sh
Source8:	mysql.sql
Source9:	%{name}-core.sh
Source10:	mysql-obm-2.2.sql
#Source11:	pgsql.sql
Source12:	%{name}-cyrus.sh
Source13:	%{name}-postfix.sh
Source14:       %{name}-pgsql.sh
#Source15:       pgsql-obm-2.2.sql
Source16:	%{name}-sysusers.sh
Source17:	%{name}-ui.sh
Source18:	pgadmin.sh
Source19:	myadmin.sh


BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)

BuildARch:      noarch
#BuildRequires:  usermode
#Provides:	obm-core
Requires:       %{name}-config = %{version}-%{release}
#Requires:       %{name}-MySQL = %{version}-%{release} OR %{name}-PostgreSQL = %{version}-%{release}
Requires:       %{name}-DataBase = %{version}-%{release} 
Requires:	%{name}-services = %{version}-%{release}
Requires:	%{name}-ldap = %{version}-%{release}
Requires:	%{name}-postfix = %{version}-%{release}
Requires:	%{name}-cyrus = %{version}-%{release}
Requires:	%{name}-ui = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}
Requires:	%{name}-support = %{version}-%{release}
Requires:       %{name}-ca
Requires:       %{name}-cert
Requires:       %{name}-solr

#Requires(post):	vixie-cron

%package        full
Summary:        obm full installation
Group:          Development/Tools
Requires:       %{name}-config = %{version}-%{release}
#Requires:       %{name}-MySQL = %{version}-%{release} OR %{name}-PostgreSQL = %{version}-%{release}
Requires:       %{name}-PostgreSQL = %{version}-%{release}
Requires:       %{name}-services = %{version}-%{release}
Requires:       %{name}-ldap = %{version}-%{release}
Requires:       %{name}-postfix = %{version}-%{release}
Requires:       %{name}-cyrus = %{version}-%{release}
Requires:       %{name}-ui = %{version}-%{release}
Requires:       %{name}-core = %{version}-%{release}
Requires:       %{name}-support = %{version}-%{release}
Requires:       %{name}-ca
Requires:       %{name}-cert
Requires:       %{name}-solr
Requires:       %{name}-sync
#Requires:       %{name}-caldav
Requires:       opush
#Requires:       minig
#Requires:       minig-conf
#Requires:       minig-storage
#Requires:       minig-backend

%description    full
The %{name}-full package is full installation
for all in one server.


%description
OBM is a global Groupware, Messaging, CRM and Project application.
It is mainly used as an Exchange Or Notes/Domino Mail and groupware replacement
but also as simple contact databases to full CRM.
OBM advanced shared calendar and contacts has connectors with MS Outlook,
Thunderbird/Lightning or PDA.


%package	config
Summary:	OBM common configuration
Group:		Development/Tools

%description	config
The %{name}-config package contains OBM configuration files
for %{name} related packages.

%package        core
Summary:        OBM common 
Group:          Development/Tools
Requires:       %{name}-config = %{version}-%{release}
Requires(post):        vixie-cron
Requires:       php >= 5.2, php-xml, php-mysql, php-gd, php-cli, php-pgsql, php-ldap

%description    core  
The %{name}-core package contains OBM files
for %{name} related packages.

%package	MySQL
Summary:	MySQL common files for OBM
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	%{name}-core = %{version}-%{release}
Requires:	obm-locator
Requires:	mysql-server >= 5.0
Conflicts:	%{name}-PostgreSQL
Provides:	%{name}-DataBase = %{version}-%{release}

%description	MySQL
The %{name}-MySQL package contains MySQL schemes and configuration
file for %{name} package.

%package	PostgreSQL
Summary:	PostgreSQL common files for OBM
Group:		Development/Tools

Requires:       %{name}-core = %{version}-%{release}
Requires:       %{name}-config = %{version}-%{release}
Requires:       %{name}-PostgreSQL-virtual
Conflicts:      %{name}-MySQL
Provides:       %{name}-DataBase = %{version}-%{release}

%description	PostgreSQL
The %{name}-PostgreSQL package contains PostgreSQL schemas and configuration
files for %{name} package.

%package	PostgreSQL91
Summary:	PostgreSQL 9.1 common files for OBM
Group:		Development/Tools

Requires:       %{name}-core = %{version}-%{release}
Requires:       %{name}-config = %{version}-%{release}
Requires:       postgresql91-server
Conflicts:      %{name}-PostgreSQL83
Provides:       %{name}-PostgreSQL-virtual

%description	PostgreSQL91
The %{name}-PostgreSQL91 package contains PostgreSQL 9.1 schemas and configuration
files for %{name} package. Support for PostgreSQL 9.1 is experimental, use at your own risk.

%package	PostgreSQL83
Summary:	PostgreSQL (8.3+) common files for OBM
Group:		Development/Tools

Requires:	%{name}-core = %{version}-%{release}
Requires:	%{name}-config = %{version}-%{release}
Requires:	postgresql-server >= 8.3
Conflicts:      %{name}-PostgreSQL91
Provides:       %{name}-PostgreSQL-virtual

%description	PostgreSQL83
The %{name}-PostgreSQL83 package contains PostgreSQL (8.3+) schemas and configuration
file for %{name} package.

%package        ui
Summary:        apache2 configuration for OBM
Group:          Development/Tools

Requires:       %{name}-core = %{version}-%{release}
Requires:	obm-locator
Requires: 	php >= 5.2, php-xml, php-mysql, php-gd, php-cli, php-pgsql, php-ldap, php-mbstring
Requires(pre):  httpd

%description    ui
The %{name}-ui package configure Apache OBM
file for %{name} package.

%package	services
Summary:	OBM script to manage services
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	perl-OBM = %{version}-%{release}
Requires:	perl-DBD-MySQL 
Requires:	perl-DBD-Pg
Requires:	perl-LDAP
Requires:	perl-XML-Simple

%description	services
The %{name}-services package contains perl script to manage services.

%package	-n perl-OBM
Summary:	Perl libraries for OBM
Group:		Development/Libraries
License:        GPL+ or Artistic
#Requires:       %{name} = %{version}-%{release}


BuildARch:      noarch
BuildRequires:	perl(ExtUtils::MakeMaker)
Requires:	perl(:MODULE_COMPAT_5.8.8)
Requires:	perl-Class-Singleton
Requires:       perl-Digest-SHA

%description	-n perl-OBM
perl-OBM package contains OBM libs for perl

#%package	-n perl-ObmSatellite
#Summary:        Perl libraries for OBM-Satellite
#Group:          Development/Libraries
#License:        GPL+ or Artistic
#
#BuildARch:	noarch
#BuildRequires:  perl(ExtUtils::MakeMaker)
#Requires:       perl(:MODULE_COMPAT_%(eval "`%{__perl} -V:version`"; echo $version))
#Requires:	perl-LDAP
#Requires:	perl-Net-CIDR
#Requires:	perl-Net-Server
#
#%description    -n perl-ObmSatellite
#perl-ObmSatellite package contains OBM-Satellite libraries for perl


%package	ldap
Summary:	OBM ldap configuration
Group:		Development/Tools

Requires:	%{name}-config = %{version}-%{release}
Requires:	openldap-servers
Requires:	openldap-clients

%description	ldap
Configuration of OpenLDAP

#%package        Satellite
#Summary:        OBM Satellite configuration
#Group:          Development/Tools
#
#Requires:       %{name}-config = %{version}-%{release}
#Requires:	perl-ObmSatellite = %{version}-%{release}
#Requires(post):	chkconfig
#Requires(preun):chkconfig
#Requires(preun):initscripts
#
#%description    Satellite
#The package %{name}-Satellite contains satellite configuration
#files for OBM.

%package	postfix
Summary:        OBM Postfix installation
Group:          Development/Tools

Requires:       %{name}-config = %{version}-%{release}
Requires:	obm-satellite
Requires:	postfix
Conflicts:	sendmail

%description    postfix
The package %{name}-postfix contains postfix configuration
files for OBM.

%package	cyrus
Summary:        OBM Cyrus installation
Group:          Development/Tools

Requires:       %{name}-config = %{version}-%{release}
Requires:	obm-satellite
Requires:	cyrus-imapd
Requires:	cyrus-imapd-utils

%description    cyrus
The package %{name}-cyrus contains cyrus configuration
files for OBM.

%package        support
Summary:        OBM Support addons
Group:          Development/Tools

Requires:       vim-enhanced
Requires:       less
Requires:       telnet
Requires:       binutils
Requires:       bind-utils
Requires:       lsof
Requires:       strace
Requires:       tcpdump
Requires:       lynx
Requires:       mutt
Requires:       screen
Requires:       openldap-clients

%description    support
The package %{name}-support contains dependencies
for OBM support

%prep
%setup -q -n %{name}-%{version} 

# Add default config
mv conf/obm_conf.inc.sample conf/obm_conf.inc
mv conf/obm_conf.ini.sample conf/obm_conf.ini

# Fix encoding, excluded sql schemas for now to avoid trouble interact
#for file in `ls scripts/2.1/*.txt` `ls doc/*/*/*.txt`; do
#	iconv -f ISO88591 -t UTF-8 $file -o file.tmp
#	mv file.tmp $file
#done

%build
cd auto/libperl-OBM
# Pass perl_vendorlib explicitly, this allows us to redefine it when we're not
# building on RedHat/CentOS
perl Makefile.PL INSTALLDIRS=vendor INSTALLVENDORLIB=%{perl_vendorlib}
cd -

%install
#ALL
rm -rf $RPM_BUILD_ROOT

#libperl stuff
mkdir -p $RPM_BUILD_ROOT%{perl_vendorlib}
cd auto/libperl-OBM
make install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT
cd -

#Remove unneeded stuff
find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} ';'

#obm-config
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE4} $RPM_BUILD_ROOT%{_bindir}/%{name}-config
install -p -m 755 %{SOURCE16} $RPM_BUILD_ROOT%{_bindir}/%{name}-sysusers
install -p -m 755 %{SOURCE3} $RPM_BUILD_ROOT%{_bindir}/%{name}-admin
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
cp -a conf/* $RPM_BUILD_ROOT%{_sysconfdir}/%{name}

#ghost stuff
touch $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/%{name}-rpm.conf


#obm-core
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/modules
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/themes/images
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/backup
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/0
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/1
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/2
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/3
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/4
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/5
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/6
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/7
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/8
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm/documents/9
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}

cp -apR contrib cron obminclude php resources tests scripts locale views app lib \
	$RPM_BUILD_ROOT%{_datadir}/%{name}
cp conf/obm_conf.inc $RPM_BUILD_ROOT%{_sysconfdir}/%{name}
# Fix permissions
chmod 0664 \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/admin_code/admin_code_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/admin_lang/admin_lang_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/cron/Logger.class.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/time/time_index.php \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/php/time/*.inc \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/resources/*/*/*/*.css \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/resources/*/*/*.inc \
	$RPM_BUILD_ROOT%{_datadir}/%{name}/%{name}include/*/of_session.inc

ln -s %{_datadir}/%{name}/resources $RPM_BUILD_ROOT%{_datadir}/%{name}/php/images

cd $RPM_BUILD_ROOT%{_sysconfdir}
ln -s %{_sysconfdir}/%{name} $RPM_BUILD_ROOT%{_datadir}/%{name}/conf
cd -
mkdir $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/
install -p -m 644 %{SOURCE2} $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/obm-core

cp conf/modules/module.inc $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/modules
cp conf/themes/login.html.sample $RPM_BUILD_ROOT%{_sysconfdir}/%{name}/themes
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE9} $RPM_BUILD_ROOT%{_bindir}/obm-core

#obm-ui
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/httpd/conf.d
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE17} $RPM_BUILD_ROOT%{_bindir}/%{name}-ui
install -p -m 644 doc/conf/apache-virtualhost_obm.conf.sample $RPM_BUILD_ROOT%{_sysconfdir}/httpd/conf.d/%{name}.conf

# obm-services
mkdir -p $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-services/updates
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/%{name}-services
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
#chmod g+s $RPM_BUILD_ROOT%{_localstatedir}/log/%{name}
cp -apR auto/testObmSatellite.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/updateCyrusAcl.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/update.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/updateSieve.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR auto/changePasswd.pl $RPM_BUILD_ROOT%{_datadir}/%{name}/auto
cp -apR scripts/2.3/update-2.2-2.3.ldap.pl $RPM_BUILD_ROOT%{_datadir}/obm-services/updates
install -p -m 640 doc/conf/logrotate.obm-services.sample $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/obm-services

# obm-ldap
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema
install -p -m 644 doc/conf/ldap_samba.schema.3.0.24.sample $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema/samba.schema
install -p -m 644 doc/conf/ldap_obm.schema.sample $RPM_BUILD_ROOT%{_sysconfdir}/openldap/schema/obm.schema
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE5} $RPM_BUILD_ROOT%{_bindir}/obm-ldap

#obm-postfix
mkdir -p $RPM_BUILD_ROOT%{_bindir}

install -p -m 755 %{SOURCE13} $RPM_BUILD_ROOT%{_bindir}/obm-postfix

# obm-mysql
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-MySQL-%{version}
install -p -m 755 %{SOURCE6} $RPM_BUILD_ROOT%{_bindir}/%{name}-mysql
install -p -m 755 %{SOURCE19} $RPM_BUILD_ROOT%{_bindir}/myadmin.lib
#install -p -m 644 %{SOURCE8} $RPM_BUILD_ROOT%{_docdir}/%{name}-MySQL-%{version}/mysql.sql
#install -p -m 644 %{SOURCE10} $RPM_BUILD_ROOT%{_docdir}/%{name}-MySQL-%{version}/mysql-obm-2.2.sql

# obm-pgsql
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_docdir}/%{name}-PostgreSQL-%{version}
install -p -m 755 %{SOURCE14} $RPM_BUILD_ROOT%{_bindir}/%{name}-pgsql
install -p -m 755 %{SOURCE18} $RPM_BUILD_ROOT%{_bindir}/pgadmin.lib
#install -p -m 644 %{SOURCE11} $RPM_BUILD_ROOT%{_docdir}/%{name}-PostgreSQL-%{version}/pgsql.sql
#install -p -m 644 %{SOURCE15} $RPM_BUILD_ROOT%{_docdir}/%{name}-PostgreSQL-%{version}/pgsql-obm-2.2.sql

# obm-cyrus
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE12} $RPM_BUILD_ROOT%{_bindir}/%{name}-cyrus

## obm-stallite stuff
#mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-satellite
#mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/init.d
#mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/%{name}-satellite
#mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/%{name}
#install -p -m 755 auto/obmSatellite/obmSatellite.pl $RPM_BUILD_ROOT%{_datadir}/obm-satellite
#install -p -m 755 auto/obmSatellite/init-obmSatellite.sample $RPM_BUILD_ROOT%{_sysconfdir}/init.d/obmSatellite
#sed -i -e "s#^OBMPATH.*#OBMPATH=\"%{_datadir}/%{name}-satellite\"#" $RPM_BUILD_ROOT%{_sysconfdir}/init.d/obmSatellite
#sed -i -e "s#^daemon_path.*#daemon_path=\"\$OBMPATH/obmSatellite.pl\"#" $RPM_BUILD_ROOT%{_sysconfdir}/init.d/obmSatellite
#install -p -m 600 auto/obmSatellite/obmSatellite.cf.sample $RPM_BUILD_ROOT%{_sysconfdir}/%{name}-satellite/%{name}Satellite.cf
#sed -i -e "s#^cyrus_service.*#cyrus_service	%{_sysconfdir}/init.d/cyrus-imapd#" $RPM_BUILD_ROOT%{_sysconfdir}/%{name}-satellite/%{name}Satellite.cf
#sed -i -e "s#cyrus_partition_root.*#cyrus_partition_root	%{_localstatedir}/spool/imap#" $RPM_BUILD_ROOT%{_sysconfdir}/%{name}-satellite/%{name}Satellite.cf
#install -p -m 755 %{SOURCE7} $RPM_BUILD_ROOT%{_bindir}/obm-satellite
## deplacer dans le postinst
#pushd $RPM_BUILD_ROOT%{_sysconfdir}/%{name}-satellite/
#ln -s %{_sysconfdir}/%{name}-satellite/%{name}Satellite.cf $RPM_BUILD_ROOT%{_datadir}/obm-satellite/%{name}Satellite.cf
#popd

# Add self auth on wrapper script
#ln -sf consolehelper $RPM_BUILD_ROOT%{_bindir}/%{name}-config

# Remove version-controle-internal file ---> useless
find $RPM_BUILD_ROOT -name '.cvsignore' -exec rm -rf {} ';'

%clean
rm -rf $RPM_BUILD_ROOT

#%post		-n %{name}-Satellite
#/sbin/chkconfig --add obmSatellite
#
#%preun		-n %{name}-Satellite
#if [ $1 = 0 ] ; then
#/sbin/service obmSatellite stop >/dev/null 2>&1
#/sbin/chkconfig --del obmSatellite
#fi

%post		-n %{name}-ui
sed -i -e "s/^# chkconfig: - 85 15$/# chkconfig: 2345 85 15/" /etc/init.d/httpd
chkconfig --del httpd
chkconfig --add httpd

%files
%defattr(-,root,root,-)

%files	-n %{name}-core
%{_bindir}/%{name}-core
%defattr(-,root,root,-)
%doc doc/* LICENSE README 
%attr(-,apache,apache) %dir %{_datadir}/%{name}
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/backup
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/0
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/1
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/2
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/3
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/4
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/5
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/6
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/7
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/8
%attr(-,apache,apache) %dir %{_localstatedir}/lib/%{name}/documents/9
%{_datadir}/%{name}/conf
%{_datadir}/%{name}/contrib
%{_datadir}/%{name}/cron
%{_datadir}/%{name}/obminclude
%{_datadir}/%{name}/php
%{_datadir}/%{name}/resources
%{_datadir}/%{name}/tests
%{_datadir}/%{name}/scripts
%{_datadir}/%{name}/views
%{_datadir}/%{name}/app
%{_datadir}/%{name}/lib
%{_datadir}/%{name}/locale
%{_sysconfdir}/%{name}/hooks
%{_sysconfdir}/%{name}/themes
%{_sysconfdir}/%{name}/modules
%{_sysconfdir}/cron.d/obm-core

%files		-n perl-OBM
%defattr(-,root,root,-)
%{perl_vendorlib}/OBM
%{_mandir}/man3/*.3pm*

#%files          -n perl-ObmSatellite
#%defattr(-,root,root,-)
#%{perl_vendorlib}/ObmSatellite

%files		-n %{name}-ui
%config(noreplace) %{_sysconfdir}/httpd/conf.d/*.conf
%{_bindir}/%{name}-ui

%files          -n %{name}-full
#no files in this package

%files		-n %{name}-config
%defattr(-,root,root,-)
%{_bindir}/%{name}-config
%{_bindir}/%{name}-admin
%{_bindir}/%{name}-sysusers
%{_bindir}/pgadmin.lib
%dir %{_sysconfdir}/%{name}
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/%{name}_conf.ini
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/%{name}_conf.inc
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/%{name}-rpm.conf
%verify(not size,not md5) %config(noreplace) %{_sysconfdir}/%{name}/automateLdapMapping.xml
#%ghost %{_sysconfdir}/%{name}/%{name}-rpm.conf
#%config(noreplace) %{_sysconfdir}/pam.d/%{name}
#%config(noreplace) %{_sysconfdir}/security/console.apps/%{name}

%files		-n %{name}-PostgreSQL91

%files		-n %{name}-PostgreSQL83

%files		-n %{name}-MySQL
%defattr(-,root,root,-)
#%doc %{_docdir}/%{name}-MySQL-%{version}/mysql.sql
#%doc %{_docdir}/%{name}-MySQL-%{version}/mysql-obm-2.2.sql
%{_bindir}/obm-mysql
%{_bindir}/myadmin.lib

%post           -n %{name}-MySQL
if [ "$1" = "2" ]; then
        echo "Finish upgrade Database, check /usr/share/obm/script upgrade script"
fi

%files          -n %{name}-PostgreSQL
%defattr(-,root,root,-)
#%doc %{_docdir}/%{name}-PostgreSQL-%{version}/pgsql.sql
#%doc %{_docdir}/%{name}-PostgreSQL-%{version}/pgsql-obm-2.2.sql
%{_bindir}/obm-pgsql

%post           -n %{name}-PostgreSQL
if [ "$1" = "2" ]; then
        echo "Finish upgrade Database, check /usr/share/obm/script upgrade script"
fi

%files		-n %{name}-services
%{_datadir}/%{name}/auto
%{_datadir}/obm-services/updates
%config(noreplace) %{_sysconfdir}/logrotate.d/obm-services
%attr(750, apache, apache) %{_localstatedir}/log/%{name}-services


%post           -n %{name}-services
if [ "$1" = "2" ]; then
  /usr/share/obm/auto/update.pl --global --domain-global
  echo "!!!!! Be careful !!!!! If you upgrade 2.2.x to 2.3.x please run update script in /usr/share/obm-services/updates/ "
fi



%files 		-n %{name}-ldap
%doc doc/conf/ldap_slapd.conf.sample
%{_sysconfdir}/openldap/schema/obm.schema
%{_sysconfdir}/openldap/schema/samba.schema
%{_bindir}/%{name}-ldap

#%files		-n %{name}-Satellite
#%defattr(-,root,root,-)
#%{_bindir}/%{name}-satellite
#%{_datadir}/%{name}-satellite
#%{_localstatedir}/log/%{name}
#%config(noreplace) %{_sysconfdir}/%{name}-satellite/%{name}Satellite.cf
#%{_sysconfdir}/init.d/obmSatellite

%files		-n %{name}-postfix
%{_bindir}/%{name}-postfix
%doc 	doc/conf/postfix_main.cf_-_SMTPin.2.3.x.sample
%doc 	doc/conf/postfix_main.cf_-_SMTPout.2.3.x.sample


%post           -n %{name}-postfix
if [ "$1" = "1" ]; then
  echo -n "[obm-postfix] activate obm-satellite postfix module..."
  /usr/sbin/osenmod postfixSmtpInMaps
  /etc/init.d/obmSatellite restart
fi

%postun         -n %{name}-postfix
if [ "$1" = "0" ]; then
  echo -n "[obm-postfix] remove obm-satellite postfix module..."
  if [ -e /usr/sbin/osdismod ]; then
    /usr/sbin/osdismod postfixSmtpInMaps
  fi
fi


%files		-n %{name}-cyrus
%{_bindir}/%{name}-cyrus
%doc 	doc/conf/cyrus_cyrus.conf.sample
%doc 	doc/conf/cyrus_imapd.conf.sample
%doc 	doc/conf/cyrus_saslauthd.conf.sample
%doc 	doc/conf/cyrus_saslauthd.sample

%post           -n %{name}-cyrus
if [ "$1" = "1" ]; then
  echo -n "[obm-cyrus] activate obm-satellite cyrus module..."
  /usr/sbin/osenmod cyrusPartition
  /etc/init.d/obmSatellite restart
fi

if [ "$1" = "2" ]; then
  imapd_file="/etc/imapd.conf"
  saslauthd_file="/etc/saslauthd.conf"
  SINGLE_NAME_SPACE="OK"
  grep -i '^virtdomains.*userid' ${imapd_file} && SINGLE_NAME_SPACE="NOK"
  if [ $SINGLE_NAME_SPACE = 'OK' ]; then
    echo -n "[OBM-CYRUS] update imapd.conf ... "

    cp ${imapd_file} ${imapd_file}_BEFORE_OBM_2.3_UPDATE
    LDAPSERVER=`cat /etc/obm/obm_conf.ini | grep -i "ldapServer" | cut -d'=' -f2 | sed -e "s/ //g"|sed -e "s#ldap://##"`

    DEFAULT_DOMAIN=`ldapsearch -x -h ${LDAPSERVER} '(dc=*)' | grep dn | grep -v 'dn: dc=local' | sed -e 's/dn: //' | sed -e 's/\,dc=local//' | cut -d"=" -f2`

    sed -i -e 's/\(^virtdomains.*\)/###COMMENT_BY_UPGRADE##\1/' ${imapd_file}

    echo "virtdomains: userid" >> ${imapd_file}
    echo "defaultdomain: ${DEFAULT_DOMAIN}" >> ${imapd_file}
    echo "DONE."

    echo -n "[OBM-CYRUS] update saslauthd.conf ... "
    cp ${saslauthd_file} ${saslauthd_file}_BEFORE_OBM_2.3_UPDATE
    sed -i -e 's/\(^ldap_filter.*\)/###COMMENT_BY_UPGRADE##\1/' $saslauthd_file
    ldap_filter="(|(&(|(mailBox=%U@%d)(mailBox=%U@<singleDomainName>))(objectClass=obmUser)(mailAccess=PERMIT))(&(uid=%U)(cn=Administrator Cyrus*)(objectClass=posixAccount)))"
    echo "# Use this filter when migrating from an SingleNameSpace installation" >> ${saslauthd_file}
    echo "# IMPORTANT : see 'defaultdomain' into '/etc/imapd.conf' too" >> ${saslauthd_file}
    echo "# ie: migrating a single namespace installation, domain : foo.com" >> ${saslauthd_file}
    echo "#     replace '<singleDomainName>' by 'foo.com'" >> ${saslauthd_file}
    echo "ldap_filter: ${ldap_filter}" >> ${saslauthd_file}
    echo "DONE."
  fi
fi


%postun         -n %{name}-cyrus
if [ "$1" = "0" ]; then
  echo -n "[obm-cyrus remove obm-satellite cyrus module..."
  if [ -e /usr/sbin/osdismod ]; then
    /usr/sbin/osdismod cyrusPartition
  fi
fi

%files          -n %{name}-support
#empty package :(

%changelog
* Tue Jul 21 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.7-2
- remove php-magik require
* Sun Apr 22 2009 Ronan Lanore <ronan.lanore[at]obm.org> - 2.2.1-2
- fix update conffile

* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- New upstream version and first stable rpm release

* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-3
- bump version

* Mon Feb 23 2009 Ronan Lanore <ronan.lanore[at]obm.org>
+ obm-2.2.0-2
- Add Dep perl-Class-Singleton to obm-services

* Fri Feb 20 2009 Ronan Lanore <ronan.lanore[at]obm.org> - 2.2.0-2
- Change dep php to php >= 5.2
- Add dep perl-DBD-Pg for obm-service
- Add dep php-ldap in obm-core and obm-ui
- Add dep php-pgsql for obm-ui and obm-core

* Tue Dec 30 2008 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-1
- Bump version.
* Thu Jun 19 2008 Sylvain Garcia <sylvain.garcia[at]aliasource.fr> - 2.1.10-1
- New upstream.
- add obm-ldap.
* Mon Apr 21 2008 Xavier Lamien <lxtnow[at]gmail.com> - 2.1.9-1
- Initial RPM Release.
