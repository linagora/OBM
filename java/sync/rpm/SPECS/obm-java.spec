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
Source2: jetty.xml.sample

BuildArch:      noarch
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant
Requires: java-devel >= 1.6.0
Requires: obm-tomcat
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

%package -n opush
Summary: Active Sync server for Open Business Management
Group:	Development/Tools
Requires: java-devel >= 1.6.0
Requires: obm-config
Requires: obm-jetty

%description -n opush
This package synchronizes a Jetty web application to synchronize OBM data with
PDA and smartphones.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-locator
Summary: Locator for Open Business Management
Group:	Development/Tools
Requires: java-devel >= 1.6.0
Requires: obm-config
Requires: obm-jetty

%description -n obm-locator
This package is a J2E web service, which allows can be queried to retrieve
the location of an OBM component.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%package -n obm-jetty
Summary: configuration for Jetty for Open Business Management
Group:	Development/Tools
Requires: jetty6

%description -n obm-jetty
It allows Jetty Server to start after its install and changes the port.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%prep
%setup -q -n obm-java-%{version}

%build
LANG=en_US.UTF-8 mvn clean install

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_prefix}
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT/%{_datadir}
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}
# install obm-sync
mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-sync
# copie du web-inf de obm-sync
WEB_INF=`find services/target -name WEB-INF `
cp -r ${WEB_INF} $RPM_BUILD_ROOT%{_datadir}/obm-sync
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/postgresql-9.0-801.jdbc4.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/slf4j-api-*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/logback*.jar
rm -f $RPM_BUILD_ROOT%{_datadir}/obm-sync/WEB-INF/lib/jta-1.1.jar
# postinst pour obm-sync
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/obm-sync
# sample jetty
mkdir -p $RPM_BUILD_ROOT%{_docdir}/obm-jetty
install -p -m 755 %{SOURCE2} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty.xml.sample

# install opush
#mkdir -p $RPM_BUILD_ROOT/%{_datadir}/jetty/webapps/Microsoft-Server-ActiveSync
mkdir -p $RPM_BUILD_ROOT/srv/jetty6/webapps/Microsoft-Server-ActiveSync
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/log/opush
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/opush
cp opush/config-sample/sync_perms.ini $RPM_BUILD_ROOT/%{_sysconfdir}/opush/
cp opush/config-sample/ldap_conf.ini $RPM_BUILD_ROOT/%{_sysconfdir}/opush/
cp opush/config-sample/mail_conf.ini $RPM_BUILD_ROOT/%{_sysconfdir}/opush/
# copie du web-inf
cd opush
WEB_INF=`find push/target -name WEB-INF `
#cp -r ${WEB_INF} $RPM_BUILD_ROOT/%{_datadir}/jetty/webapps/Microsoft-Server-ActiveSync
cp -r ${WEB_INF} $RPM_BUILD_ROOT/srv/jetty6/webapps/Microsoft-Server-ActiveSync
cd -

# install obm-locator
mkdir -p $RPM_BUILD_ROOT/srv/jetty6/webapps/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_sysconfdir}/obm-locator
mkdir -p $RPM_BUILD_ROOT/%{_localstatedir}/log/obm-locator
# copie du web-inf
WEB_INF=`find obm-locator/target -name WEB-INF `
#cp -r ${WEB_INF} $RPM_BUILD_ROOT/%{_datadir}/jetty/webapps/obm-locator
cp -r ${WEB_INF} $RPM_BUILD_ROOT/srv/jetty6/webapps/obm-locator


%files -n obm-sync
%defattr(-,root,root,-)
%{_bindir}/obm-sync
%{_datadir}/obm-sync

%files -n opush
%defattr(-,root,root,-)
#%{_datadir}/jetty/webapps/Microsoft-Server-ActiveSync
/srv/jetty6/webapps/Microsoft-Server-ActiveSync
%{_localstatedir}/log/opush
%config(noreplace) %{_sysconfdir}/opush/sync_perms.ini
%config(noreplace) %{_sysconfdir}/opush/ldap_conf.ini
%config(noreplace) %{_sysconfdir}/opush/mail_conf.ini

%files -n obm-locator
%defattr(-,root,root,-)
#%{_datadir}/jetty/webapps/obm-locator
/srv/jetty6/webapps/obm-locator
%{_localstatedir}/log/obm-locator

%files -n obm-jetty
%defattr(-,root,root,-)
%{_docdir}/obm-jetty/jetty.xml.sample

%post -n obm-jetty
/etc/init.d/jetty6 stop > /dev/null 2>&1 || :
if [ -e /etc/jetty6/jetty.xml ]; then
	cp /etc/jetty6/jetty.xml /etc/jetty6/jetty.xml.orig
fi
cp /usr/share/doc/obm-jetty/jetty.xml.sample /etc/jetty6/jetty.xml
/etc/init.d/jetty6 start > /dev/null 2>&1 || :

%post -n opush
[ ! -f %{_sysconfdir}/opush/logback.xml ] && echo "<included/>" > %{_sysconfdir}/opush/logback.xml
/sbin/service jetty6 restart >/dev/null 2>&1 || :

%postun -n opush
if [ "$1" -ge "1" ] ; then
    /sbin/service jetty6 restart >/dev/null 2>&1 || :
fi

%post -n obm-locator
/sbin/service jetty restart >/dev/null 2>&1 || :

%postun -n obm-locator
if [ "$1" -ge "1" ] ; then
    /sbin/service jetty restart >/dev/null 2>&1 || :
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
