# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

Name: obm-solr
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: Solr indexing server for Open Business Management
Vendor: obm.org
URL: http://www.minig.org
Group: Applications/File
License: AGPLv3
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: %{name}-%{version}.tar.gz
Source1: solr.xml
Source2: obm-solr.cron.d

BuildArch:      noarch
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant
Requires(post): obm-tomcat-common-libs


%description
Solr is an indexing web service based on Lucene, used to index contacts and
events in OBM.

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
LANG=en_US.UTF-8 ant install

%install
rm -rf $RPM_BUILD_ROOT
mkdir -p $RPM_BUILD_ROOT%{_prefix}
cp -a debian/obm-solr/usr/* $RPM_BUILD_ROOT%{_prefix}

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}
cp -a debian/obm-solr/etc/* $RPM_BUILD_ROOT%{_sysconfdir}

mkdir -p $RPM_BUILD_ROOT%{_localstatedir}
cp -a debian/obm-solr/var/* $RPM_BUILD_ROOT%{_localstatedir}

mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-tomcat/applis
cp %{SOURCE1} $RPM_BUILD_ROOT%{_sysconfdir}/obm-tomcat/applis

mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-solr
cp obm_index_init.py $RPM_BUILD_ROOT%{_datadir}/obm-solr
cp obm_optimize_index.py $RPM_BUILD_ROOT%{_datadir}/obm-solr

mkdir $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/
install -p -m 644 %{SOURCE2} $RPM_BUILD_ROOT%{_sysconfdir}/cron.d/obm-solr

%files
%defattr(-,root,root,-)
%{_localstatedir}/solr
%{_sysconfdir}/solr
%{_datadir}/solr
%{_datadir}/obm-solr
%{_sysconfdir}/obm-tomcat/applis
%{_sysconfdir}/cron.d/obm-solr

%post
rm -rf /var/solr/default/conf
ln -sf /etc/solr/conf/default /var/solr/default/conf
rm -rf /var/solr/webmail/conf
ln -sf /etc/solr/conf/webmail /var/solr/webmail/conf
rm -rf /var/solr/event/conf
ln -sf /etc/solr/conf/event /var/solr/event/conf
rm -fr /var/solr/contact/conf
ln -s /etc/solr/conf/contact /var/solr/contact/conf

#update tomcat application
/usr/bin/obm-tomcat-trigger


%changelog
* Tue Apr 30 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.5.0-
- New upstream release.
* Mon Apr 29 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.5.0-0.rc3
- New upstream release.
* Fri Apr 26 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.5.0-0.rc2
- New upstream release.
* Tue Apr 23 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.5.0-0.rc1
- New upstream release.
* Fri Jan 18 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.5.0.0-0.alpha0
- New upstream release.
* Thu Jan 17 2013 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta7
- New upstream release.
* Wed Dec 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta6
- New upstream release.
* Tue Dec 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta5
- New upstream release.
* Mon Dec 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta4
- New upstream release.
* Tue Nov 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta3
- New upstream release.
* Wed Nov 07 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta2
- New upstream release.
* Mon Nov 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.beta1
- New upstream release.
* Tue Oct 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.alpha3
- New upstream release.
* Mon Oct 15 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.alpha2
- New upstream release.
* Wed Sep 26 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.2.0-0.alpha1
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1.1-0.rc2
- New upstream release.
* Fri Sep 14 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1.1-0.rc1
- New upstream release.
* Wed Sep 12 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1.1-0.beta1
- New upstream release.
* Mon Sep 03 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1.0-1
- New upstream release.
* Fri Aug 31 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-
- New upstream release.
* Thu Jul 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-beta2
- New upstream release.
* Mon Jul 16 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-beta2
- New upstream release.
* Fri Jul 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-beta1
- New upstream release.
* Tue Jun 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha11
- New upstream release.
* Mon May 21 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha10
- New upstream release.
* Fri Apr 20 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha9
- New upstream release.
* Thu Apr 19 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha8
- New upstream release.
* Tue Apr 17 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha7
- New upstream release.
* Thu Apr 05 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha6
- New upstream release.
* Fri Mar 02 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha5
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha4
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha3
- New upstream release.
* Wed Jan 18 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha2
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.1-alpha1
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc14
- New upstream release.
* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia@obm.org> 1.4-5
- Initial version
