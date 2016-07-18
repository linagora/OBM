# Forcing using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name: obm-sync
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: synchronization application for Open Business Management
Vendor: obm.org
URL: http://www.obm.org
Group: Applications/File
License: AGPLv3
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: obm-java-%{version}.tar.gz
Source1: obm-sync.postinst
Source2: obm-autoconf.xml

BuildArch:      noarch
BuildRequires:  java-1.7.0-openjdk-devel >= 1.7.0
BuildRequires:  ant
Requires(post): obm-tomcat-common-libs = %{version}-%{release}
Requires: obm-config

%description
This package contains a J2E web application used to synchronize OBM data with
Icedove/Thunderbird, Outlook, etc.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%define _lib                   lib
%define _libdir                %{_exec_prefix}/%{_lib}
%define __jar_repack %{nil}

%package -n obm-locator
Summary: Locator for Open Business Management
Group:	Development/Tools
Requires: java-1.7.0-openjdk >= 1.7.0
Requires: obm-config

%description -n obm-locator
This package is a J2E web service, which can be queried to retrieve
the location of an OBM component.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-imap-archive
Summary: OBM component that perform email archiving
Group:  Development/Tools
Requires: java-1.7.0-openjdk >= 1.7.0
Requires: obm-config

%description -n obm-imap-archive
obm-imap-archive is an http server exposing webservices to perform email archiving.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-provisioning
Summary: Provisioning API for Open Business Management
Group:  Development/Tools
Requires: java-1.7.0-openjdk >= 1.7.0
Requires: obm-config
Requires: cyrus-sasl-plain

%description -n obm-provisioning
This package is an HTTP web service, which can be queried to provision an OBM
server.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-autoconf
Summary: Locator for Open Business Management
Group:	Development/Tools
Requires: obm-config
Requires: obm-tomcat

%description -n obm-autoconf
This package contains a J2E web service which can be queried to retrieve a
configuration as xml to autoconfigure Thunderbird.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-tomcat-common-libs
Summary: Tomcat common libs for Open Business Management
Group:  Development/Tools
Requires: obm-tomcat

%description -n obm-tomcat-common-libs
This package contains the library used by obm webapps deployed in tomcat.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%prep
%setup -q -n obm-java-%{version}

%build
[ -z "%{obm_nocompile}" ] || [ %{obm_nocompile} != "1" ] && LANG=en_US.UTF-8 mvn install

%install

# obm-sync

mkdir -p $RPM_BUILD_ROOT%{_prefix}
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_datadir}
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}
mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-sync
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm-sync
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm-sync/jms
WEB_INF=`find services-webapp/target -name WEB-INF`
cp -r ${WEB_INF} $RPM_BUILD_ROOT%{_datadir}/obm-sync
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/postgresql-*.jdbc4.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/slf4j-api-*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/logback*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/jta-1.1.jar
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/obm-sync

# obm-locator

mkdir -p $RPM_BUILD_ROOT/var/run/obm-locator
mkdir -p $RPM_BUILD_ROOT/usr/share/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/log/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/lib/obm-locator
cp -r obm-locator/target/obm-locator.jar $RPM_BUILD_ROOT/usr/share/obm-locator/
cp -r obm-locator/target/lib $RPM_BUILD_ROOT/usr/share/obm-locator/
cp -r obm-locator/obm-locator-start.sh $RPM_BUILD_ROOT/usr/share/obm-locator/
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
cp -a obm-locator/obm-locator.centos.sh $RPM_BUILD_ROOT%{_initrddir}/obm-locator

# obm-imap-archive

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-imap-archive
mkdir -p $RPM_BUILD_ROOT%{_datarootdir}/obm-imap-archive
mkdir -p $RPM_BUILD_ROOT%{_docdir}/obm-imap-archive
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/obm-imap-archive
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm-imap-archive
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/run/obm-imap-archive
cp -r imap-archive/target/imap-archive.jar $RPM_BUILD_ROOT%{_datarootdir}/obm-imap-archive/
cp -r imap-archive/target/lib $RPM_BUILD_ROOT%{_datarootdir}/obm-imap-archive/
cp -r imap-archive/src/main/rpm/imap-archive-start.sh $RPM_BUILD_ROOT%{_datarootdir}/obm-imap-archive/
cp -r imap-archive/target/generated-docs/* $RPM_BUILD_ROOT%{_docdir}/obm-imap-archive/
cp -r imap-archive/logback-include.xml $RPM_BUILD_ROOT%{_sysconfdir}/obm-imap-archive/logback.xml
cp -r imap-archive/obm-imap-archive.ini $RPM_BUILD_ROOT%{_sysconfdir}/obm-imap-archive/obm-imap-archive.ini
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
cp -a imap-archive/src/main/rpm/imap-archive.sh $RPM_BUILD_ROOT%{_initrddir}/obm-imap-archive

# obm-provisioning

mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/run/obm-provisioning
mkdir -p $RPM_BUILD_ROOT%{_datarootdir}/obm-provisioning
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-provisioning
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/obm-provisioning
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/obm-provisioning
cp -r provisioning-server/target/provisioning-server.jar $RPM_BUILD_ROOT%{_datarootdir}/obm-provisioning/
cp -r provisioning-server/target/lib $RPM_BUILD_ROOT%{_datarootdir}/obm-provisioning/
cp -r provisioning-server/provisioning-start.sh $RPM_BUILD_ROOT%{_datarootdir}/obm-provisioning/
cp -r provisioning-server/logback-include.xml $RPM_BUILD_ROOT%{_sysconfdir}/obm-provisioning/logback.xml
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
cp -a provisioning-server/provisioning.centos.sh $RPM_BUILD_ROOT%{_initrddir}/obm-provisioning

# obm-autoconf

mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-autoconf
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-tomcat/applis
WEB_INF=`find autoconf/target -name WEB-INF`
cp -r ${WEB_INF} $RPM_BUILD_ROOT%{_datadir}/obm-autoconf
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-autoconf/WEB-INF/lib/postgresql-9.0-801.jdbc4.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-autoconf/WEB-INF/lib/slf4j-api-*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-autoconf/WEB-INF/lib/logback*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-autoconf/WEB-INF/lib/jta-1.1.jar
cp %{SOURCE2} $RPM_BUILD_ROOT%{_sysconfdir}/obm-tomcat/applis/

# obm-tomcat-common-libs

mkdir -p $RPM_BUILD_ROOT%{_datadir}/tomcat/lib
cp -p webapp-common-dependencies/target/tomcat/*.jar \
  $RPM_BUILD_ROOT%{_datadir}/tomcat/lib/


%files -n obm-sync
%defattr(-,root,root,-)
%{_bindir}/obm-sync
%{_datadir}/obm-sync
%{_localstatedir}/lib/obm-sync
%{_localstatedir}/lib/obm-sync/jms

%files -n obm-locator
%defattr(-,root,root,-)
%{_datarootdir}/obm-locator
%{_initrddir}/obm-locator
%attr(0775,locator,root) %{_localstatedir}/log/obm-locator
%attr(0775,locator,root) %{_localstatedir}/lib/obm-locator
%attr(0775,locator,root) %{_localstatedir}/run/obm-locator
%attr(0775,locator,root) %{_datarootdir}/obm-locator/obm-locator-start.sh
%attr(0775,root,root) %{_initrddir}/obm-locator

%files -n obm-imap-archive
%defattr(-,root,root,-)
%{_sysconfdir}/obm-imap-archive
%{_datarootdir}/obm-imap-archive
%{_docdir}/obm-imap-archive
%{_initrddir}/obm-imap-archive
%attr(0775,imap-archive,root) %{_localstatedir}/log/obm-imap-archive
%attr(0775,imap-archive,root) %{_localstatedir}/lib/obm-imap-archive
%attr(0775,imap-archive,root) %{_localstatedir}/run/obm-imap-archive
%attr(0775,root,root) %{_datarootdir}/obm-imap-archive
%attr(0775,root,root) %{_docdir}/obm-imap-archive
%attr(0775,root,root) %{_initrddir}/obm-imap-archive
%config(noreplace) %{_sysconfdir}/obm-imap-archive/logback.xml
%config(noreplace) %{_sysconfdir}/obm-imap-archive/obm-imap-archive.ini

%files -n obm-provisioning
%defattr(-,root,root,-)
%{_datarootdir}/obm-provisioning
%{_initrddir}/obm-provisioning
%attr(0775,provisioning,root) %{_localstatedir}/log/obm-provisioning
%attr(0775,provisioning,root) %{_localstatedir}/lib/obm-provisioning
%attr(0775,provisioning,root) %{_localstatedir}/run/obm-provisioning
%attr(0775,root,root) %{_datarootdir}/obm-provisioning/provisioning-start.sh
%attr(0755,root,root) %{_initrddir}/obm-provisioning
%config(noreplace) %{_sysconfdir}/obm-provisioning/logback.xml

%files -n obm-autoconf
%defattr(-,root,root,-)
%{_datadir}/obm-autoconf
%config(noreplace) %{_sysconfdir}/obm-tomcat/applis/obm-autoconf.xml

%files -n obm-tomcat-common-libs
%defattr(-,root,root,-)
%{_datadir}/tomcat/lib/*.jar

%pre -n obm-locator
# Create locator user if it doesn't exist
id locator >/dev/null 2>&1
if [ $? -eq 1 ]; then
  useradd --system --gid adm locator
fi

%post -n obm-locator
chown -R locator:adm %{_localstatedir}/log/obm-locator
chown -R locator:adm %{_localstatedir}/lib/obm-locator
chown -R locator:adm %{_localstatedir}/run/obm-locator
/sbin/service obm-locator restart >/dev/null 2>&1 || :

%pre -n obm-imap-archive
# Create imap-archive user if it doesn't exist
id imap-archive >/dev/null 2>&1
if [ "$?" = "1" ]; then
  useradd --system --gid adm imap-archive
fi

%post -n obm-imap-archive
/sbin/service obm-imap-archive restart >/dev/null 2>&1 || :

%pre -n obm-provisioning
# Create provisioning user if it doesn't exist
id provisioning >/dev/null 2>&1
if [ $? -eq 1 ]; then
  useradd --system --gid adm provisioning
fi

%post -n obm-provisioning
/sbin/service obm-provisioning restart >/dev/null 2>&1 || :

%post -n obm-sync
if [ "$1" = "1" ]; then
	echo "Finish installation with obm-admin command."
fi
if [ "$1" = "2" ]; then
        /usr/bin/obm-sync
fi

%postun -n obm-sync
if [ "$1" = "0" ]; then
  rm -f /etc/obm-tomcat/applis/obm-sync.xml
  rm -f /usr/share/tomcat/conf/Catalina/localhost/obm-sync.xml
fi

%post -n obm-autoconf
/usr/bin/obm-tomcat-trigger

%postun -n obm-autoconf
if [ "$1" = "0" ]; then
  rm -f %{_sysconfdir}/obm-tomcat/applis/obm-autoconf.xml
fi


%changelog
* Mon Jul 18 2016 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.2.1-0.alpha0
- New upstream release.
* Mon Jul 20 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-2
- New upstream release.
* Thu Jul 16 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-1
- New upstream release.
* Fri Jul 10 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-0.rc5
- New upstream release.
* Thu Jul 09 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-0.rc4
- New upstream release.
* Wed Jul 08 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-0.rc3
- New upstream release.
* Tue Jul 07 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-0.rc2
- New upstream release.
* Mon Jul 06 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.6-0.rc1
- New upstream release.
* Wed May 27 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.5-1
- New upstream release.
* Thu May 21 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.5-0.rc4
- New upstream release.
* Wed May 20 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.5-0.rc3
- New upstream release.
* Wed May 13 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.5-0.rc2
- New upstream release.
* Tue May 12 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.5-0.rc1
- New upstream release.
* Mon Mar 30 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.4-1
- New upstream release.
* Thu Mar 26 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.4-0.rc1
- New upstream release.
* Mon Mar 02 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.3-
- New upstream release.
* Wed Feb 25 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.3-0.rc2
- New upstream release.
* Thu Feb 19 2015 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.3-0.rc1
- New upstream release.
* Tue Nov 25 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.0-0.rc1
- New upstream release.
* Wed Nov 19 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.1.0-0.alpha0
- New upstream release.
* Tue Jul 22 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.0.0-1
- New upstream release.
* Tue Jun 10 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.0.0-0.rc2
- New upstream release.
* Thu May 22 2014 Thomas Sarboni <tsarboni@linagora.com> - obm-java-3.0.0-0.rc1
- New upstream release.
* Tue Apr 30 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.5.0-
- New upstream release.
* Mon Apr 29 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.5.0-0.rc3
- New upstream release.
* Fri Apr 26 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.5.0-0.rc2
- New upstream release.
* Tue Apr 23 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.5.0-0.rc1
- New upstream release.
* Fri Jan 18 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.5.0.0-0.alpha0
- New upstream release.
* Thu Jan 17 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta7
- New upstream release.
* Wed Dec 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta6
- New upstream release.
* Tue Dec 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta5
- New upstream release.
* Mon Dec 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta4
- New upstream release.
* Tue Nov 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta3
- New upstream release.
* Wed Nov 07 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta2
- New upstream release.
* Mon Nov 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.beta1
- New upstream release.
* Tue Oct 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.alpha3
- New upstream release.
* Mon Oct 15 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.alpha2
- New upstream release.
* Wed Sep 26 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.2.0-0.alpha1
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1.1-0.rc2
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1.1-0.rc1
- New upstream release.
* Wed Sep 12 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1.1-0.beta1
- New upstream release.
* Mon Sep 03 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1.0-1
- New upstream release.
* Fri Aug 31 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-
- New upstream release.
* Thu Jul 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-beta2
- New upstream release.
* Mon Jul 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-beta2
- New upstream release.
* Fri Jul 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-beta1
- New upstream release.
* Tue Jun 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha11
- New upstream release.
* Mon May 21 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha10
- New upstream release.
* Fri Apr 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha9
- New upstream release.
* Thu Apr 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha8
- New upstream release.
* Tue Apr 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha7
- New upstream release.
* Thu Apr 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha6
- New upstream release.
* Fri Mar 02 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha5
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha4
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha3
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha2
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.1-alpha1
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-java-2.4.0.0-rc14
- New upstream release.
* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- New upstream version and first stable rpm release
* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-4
- bump version
* Mon Nov 24 2008 Thomas Cataldo <thomas.cataldo@obm.org>
- Change dependency to EPEL openjdk-devel
* Tue Dec 11 2007 Christophe Marteau <christophe.marteau@aliasource.fr>
- First draft of the spec file
