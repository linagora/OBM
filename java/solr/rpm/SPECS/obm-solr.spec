Name: obm-solr
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: OBM solr
Vendor: obm.org
URL: http://www.minig.org
Group: Applications/File
License: GPLv2
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: %{name}-%{version}.tar.gz
Source1: solr.xml
BuildArch:      noarch
BuildRequires:  java-devel >= 1.6.0
BuildRequires:  ant
Requires:        obm-tomcat


%description
Solr for OBM Minig.

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
* Wed Feb 25 2009 Sylvain Garcia <sylvain.garcia@obm.org> 1.4-5
- Initial version
