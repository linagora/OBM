# Forcing using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

%global jetty_home /usr/share/jetty6
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

BuildArch:      noarch
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant
Requires: java-devel >= 1.6.0
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

%package -n opush
Summary: Active Sync server for Open Business Management
Group:	Development/Tools
Requires: java-devel >= 1.6.0
Requires: obm-config
Requires(post): obm-jetty-common-libs = %{version}-%{release}

%description -n opush
This package synchronizes a Jetty web application to synchronize OBM data with
PDA and smartphones.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n spushnik
Summary: Opush probe
Group:  Development/Tools
Requires: java-devel >= 1.6.0
Requires(post): obm-jetty-common-libs = %{version}-%{release}

%description -n spushnik
This package contains a Java webapp probe able to check the status of an Opush server instance.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-locator
Summary: Locator for Open Business Management
Group:	Development/Tools
Requires: java-devel >= 1.6.0
Requires: obm-config
Requires(post): obm-jetty-common-libs = %{version}-%{release}

%description -n obm-locator
This package is a J2E web service, which allows can be queried to retrieve
the location of an OBM component.

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

%package -n obm-jetty-common-libs
Summary: Jetty common libs for Open Business Management
Group:  Development/Tools
Requires: obm-jetty

%description -n obm-jetty-common-libs
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
WEB_INF=`find services/target -name WEB-INF`
cp -r ${WEB_INF} $RPM_BUILD_ROOT%{_datadir}/obm-sync
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/postgresql-9.0-801.jdbc4.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/slf4j-api-*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/logback*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/jta-1.1.jar
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/obm-sync

# opush

mkdir -p $RPM_BUILD_ROOT%{jetty_home}/webapps/opush
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/opush
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/lib/opush
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/opush
cp opush/config-sample/sync_perms.ini $RPM_BUILD_ROOT%{_sysconfdir}/opush/
cp opush/config-sample/ldap_conf.ini $RPM_BUILD_ROOT%{_sysconfdir}/opush/
cp opush/config-sample/mail_conf.ini $RPM_BUILD_ROOT%{_sysconfdir}/opush/
cp opush/config-sample/remote_console.ini $RPM_BUILD_ROOT%{_sysconfdir}/opush/
cp -r opush/push/target/opush/* $RPM_BUILD_ROOT/%{jetty_home}/webapps/opush/

# spushnik
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/spushnik
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/spushnik
cp opush/spushnik/target/spushnik.war $RPM_BUILD_ROOT/%{jetty_home}/webapps/

# obm-locator

mkdir -p $RPM_BUILD_ROOT/%{jetty_home}/webapps/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/log/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/lib/obm-locator
cp -r obm-locator/target/obm-locator/* $RPM_BUILD_ROOT/%{jetty_home}/webapps/obm-locator/

# obm-jetty-common-libs
mkdir -p $RPM_BUILD_ROOT/%{jetty_home}/lib/ext
cp -p webapp-common-dependencies/target/jetty/*.jar \
  $RPM_BUILD_ROOT/%{jetty_home}/lib/ext

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

%files -n opush
%defattr(-,root,root,-)
%{jetty_home}/webapps/opush
%attr(0775,jetty,jetty) %{_localstatedir}/log/opush
%attr(0775,jetty,jetty) %{_localstatedir}/lib/opush
%config(noreplace) %{_sysconfdir}/opush/sync_perms.ini
%config(noreplace) %{_sysconfdir}/opush/ldap_conf.ini
%config(noreplace) %{_sysconfdir}/opush/mail_conf.ini
%config(noreplace) %{_sysconfdir}/opush/remote_console.ini

%files -n spushnik
%defattr(-,root,root,-)
%{jetty_home}/webapps/spushnik.war
%attr(0775,jetty,jetty) %{_localstatedir}/log/spushnik

%files -n obm-locator
%defattr(-,root,root,-)
%{jetty_home}/webapps/obm-locator
%attr(0775,jetty,jetty) %{_localstatedir}/log/obm-locator
%attr(0775,jetty,jetty) %{_localstatedir}/lib/obm-locator

%files -n obm-tomcat-common-libs
%defattr(-,root,root,-)
%{_datadir}/tomcat/lib/*.jar

%files -n obm-jetty-common-libs
%defattr(-,root,root,-)
%{jetty_home}/lib/ext/*.jar

%post -n opush
[ ! -f %{_sysconfdir}/opush/logback.xml ] && echo "<included/>" > %{_sysconfdir}/opush/logback.xml
/sbin/service jetty6 restart >/dev/null 2>&1 || :

%postun -n opush
if [ "$1" -ge "1" ] ; then
    /sbin/service jetty6 restart >/dev/null 2>&1 || :
fi

%post -n spushnik
[ ! -f %{_sysconfdir}/spushnik/logback.xml ] && echo "<included/>" > %{_sysconfdir}/spushnik/logback.xml
/sbin/service jetty6 restart >/dev/null 2>&1 || :

%postun -n spushnik
if [ "$1" -ge "1" ] ; then
    /sbin/service jetty6 restart >/dev/null 2>&1 || :
fi

%post -n obm-locator
/sbin/service jetty6 restart >/dev/null 2>&1 || :

%postun -n obm-locator
if [ "$1" -ge "1" ] ; then
    /sbin/service jetty6 restart >/dev/null 2>&1 || :
fi

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


%changelog
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
