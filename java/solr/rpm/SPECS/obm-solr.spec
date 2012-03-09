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
BuildArch:      noarch
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant
Requires:        obm-tomcat


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
sleep 5
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

%files
%defattr(-,root,root,-)
%{_localstatedir}/solr
%{_sysconfdir}/solr
%{_datadir}/solr
%{_datadir}/obm-solr
%{_sysconfdir}/obm-tomcat/applis

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
* Fri Mar 09 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.1-
- New upstream release.
* Fri Mar 09 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.1-rc1
- New upstream release.
* Fri Jan 13 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-
- New upstream release.
* Wed Jan 04 2012 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc16
- New upstream release.
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-solr-2.4.0.0-rc14
- New upstream release.
* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia@obm.org> 1.4-5
- Initial version
