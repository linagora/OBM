# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name: obm-tomcat
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: the Tomcat web application server for Open Business Management
Vendor: obm.org
URL: http://www.obm.org
Group: Applications/File
License: AGPLv3
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: %{name}-%{version}.tar.gz
Source1: obm-tomcat.init
Source2: obm-tomcat-trigger.sh
BuildArch:      noarch
BuildRequires:  sun-javadb-core == 1.6 or java-1.7.0-openjdk >= 1.7
BuildRequires:  ant
Requires(post): chkconfig
Requires(preun): chkconfig
Requires(preun): initscripts

%description -n obm-tomcat
This package contains the 6.0.20 version of the Apache Tomcat application
server for OBM.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%define _lib                   lib
%define _libdir                %{_exec_prefix}/%{_lib}

%prep
%setup -q -n %{name}-%{version}

%build
echo "java home: `ant -diagnostics|grep 'java.home'`"
LANG=en_US.UTF-8 ant dist

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_prefix}
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-tomcat/applis
# Fichiers obm-tomcat
cp -a debian/obm-tomcat/usr/* $RPM_BUILD_ROOT%{_prefix}
cp -a debian/obm-tomcat/var $RPM_BUILD_ROOT%{_localstatedir}
mkdir -p $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 %{SOURCE2} $RPM_BUILD_ROOT%{_bindir}/obm-tomcat-trigger
# init obm-tomcat
mkdir -p $RPM_BUILD_ROOT%{_initrddir}
cp -a %{SOURCE1} $RPM_BUILD_ROOT%{_initrddir}/obm-tomcat

%files -n obm-tomcat
%defattr(-,root,root,-)
%{_datadir}/apache-tomcat-6.0.20
%{_datadir}/tomcat
%{_localstatedir}/lib/obm-tomcat
%{_localstatedir}/log/obm-tomcat
%{_initrddir}/obm-tomcat
%{_sysconfdir}/obm-tomcat
%{_bindir}/obm-tomcat-trigger

%post -n obm-tomcat
if [ "$1" = "1" ]; then
	chkconfig --add obm-tomcat
fi
if [ -f %{_sysconfdir}/cron.daily/tmpwatch ] ; then
  _TOMCAT_TMPWATCH=$(grep hsperfdata %{_sysconfdir}/cron.daily/tmpwatch )
  [ -n "$_TOMCAT_TMPWATCH" ] || \
    sed -i -e "s|240 /tmp|-e '/tmp/hsperfdata_root' 240 /tmp|" \
      %{_sysconfdir}/cron.daily/tmpwatch
fi

%postun -n obm-tomcat
if [ "$1" = "0" ]; then
  service obm-tomcat stop
  chkconfig --del obm-tomcat
fi

%changelog
* Tue Apr 30 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.5.0-
- New upstream release.
* Mon Apr 29 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.5.0-0.rc3
- New upstream release.
* Fri Apr 26 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.5.0-0.rc2
- New upstream release.
* Tue Apr 23 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.5.0-0.rc1
- New upstream release.
* Fri Jan 18 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.5.0.0-0.alpha0
- New upstream release.
* Thu Jan 17 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta7
- New upstream release.
* Wed Dec 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta6
- New upstream release.
* Tue Dec 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta5
- New upstream release.
* Mon Dec 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta4
- New upstream release.
* Tue Nov 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta3
- New upstream release.
* Wed Nov 07 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta2
- New upstream release.
* Mon Nov 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.beta1
- New upstream release.
* Tue Oct 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.alpha3
- New upstream release.
* Mon Oct 15 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.alpha2
- New upstream release.
* Wed Sep 26 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.2.0-0.alpha1
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1.1-0.rc2
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1.1-0.rc1
- New upstream release.
* Wed Sep 12 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1.1-0.beta1
- New upstream release.
* Mon Sep 03 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1.0-1
- New upstream release.
* Fri Aug 31 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-
- New upstream release.
* Thu Jul 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-beta2
- New upstream release.
* Mon Jul 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-beta2
- New upstream release.
* Fri Jul 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-beta1
- New upstream release.
* Tue Jun 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha11
- New upstream release.
* Mon May 21 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha10
- New upstream release.
* Fri Apr 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha9
- New upstream release.
* Thu Apr 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha8
- New upstream release.
* Tue Apr 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha7
- New upstream release.
* Thu Apr 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha6
- New upstream release.
* Fri Mar 02 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha5
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha4
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha3
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha2
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.1-alpha1
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-tomcat-2.4.0.0-rc14
- New upstream release.
* Mon Jun 07 2010 Nicolas Chauvet <nchauvet@linagora.com> - 2.3.0-3
- Add an exeption to tmpwatch - Fix obmbz#1044
* Sun Apr 19 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.1-1
- New upstream version and first stable rpm release
* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.2.0-4
- bump version
* Mon Nov 24 2008 Thomas Cataldo <thomas.cataldo@obm.org>
- Change dependency to EPEL openjdk-devel
* Tue Dec 11 2007 Christophe Marteau <christophe.marteau@aliasource.fr>
- First draft of the spec file
