Name:           obm-satellite
Version:        %{obm_version}
Release:        %{obm_release}%{?dist}
Summary:        integration of OBM with Cyrus and Postfix

Group:          Development/Languages
License:        GPLv2
URL:            http://www.obm.org
Source0:        %{name}-%{version}.tar.gz
Source1:        obm-satellite.sh
Source2:        cyrusPartition
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildARch:      noarch
Requires:       obm-config
Requires:       perl-ObmSatellite = %{version}-%{release}
Requires(post): chkconfig
Requires(preun):chkconfig
Requires(preun):initscripts
Requires:       obm-cert


%description
This package contains a daemon which manages Cyrus partitions and Postfix maps
for OBM. This package needs to be installed on each server containing
obm-postfix or obm-cyrus.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.



%package        -n perl-ObmSatellite
Summary:        library for the integration of OBM with Cyrus and Postfix
Group:          Development/Libraries
License:        GPL2

BuildArch:      noarch
BuildRequires:  perl(ExtUtils::MakeMaker)
Requires:	    perl(:MODULE_COMPAT_5.8.8)
Requires:       perl-LDAP
Requires:       perl-Net-CIDR
Requires:       perl-Net-Server
Requires:       perl-Class-Singleton
Requires:       perl-Digest-SHA

%description    -n perl-ObmSatellite
This package contains the library used by obm-satellite to interact with Cyrus
and Postfix.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.



%prep
%setup -q -n %{name}-%{version}

%build
cd libperl-ObmSatellite
# Pass perl_vendorlib explicitly, this allows us to redefine it when we're not
# building on RedHat/CentOS
perl Makefile.PL INSTALLDIRS=vendor INSTALLVENDORLIB=%{perl_vendorlib}
cd -

%install
#ALL
rm -rf $RPM_BUILD_ROOT

#libperl stuff
cd libperl-ObmSatellite
make install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT
cd -

#Remove unneeded stuff
find $RPM_BUILD_ROOT -type f -name .packlist -exec rm -f {} ';'

#obm-satellite
mkdir -p $RPM_BUILD_ROOT%{_bindir}
mkdir -p $RPM_BUILD_ROOT%{_sbindir}
mkdir -p $RPM_BUILD_ROOT%{_datadir}/obm-satellite
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/init.d
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite/mods-available
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite/mods-enabled
mkdir -p $RPM_BUILD_ROOT%{_localstatedir}/log/obm-satellite
install -p -m 755 obmSatellite.pl $RPM_BUILD_ROOT%{_datadir}/obm-satellite
install -p -m 755 init-obmSatellite.sample $RPM_BUILD_ROOT%{_sysconfdir}/init.d/obmSatellite
install -p -m 600 obmSatellite.ini.sample $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite/obmSatellite.ini
install -p -m 755 %{SOURCE1} $RPM_BUILD_ROOT%{_bindir}/obm-satellite
install -p -m 755 osdismod $RPM_BUILD_ROOT%{_sbindir}/
install -p -m 755 osenmod  $RPM_BUILD_ROOT%{_sbindir}/
mkdir -p $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d
install -p -m 640 doc/conf/logrotate.obm-satellite.sample $RPM_BUILD_ROOT%{_sysconfdir}/logrotate.d/obm-satellite


#postfix configuration file
install -p -m 755 mods-available/postfixSmtpInMaps $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite/mods-available
#Cyrus configuration file
install -p -m 755 %{SOURCE2} $RPM_BUILD_ROOT%{_sysconfdir}/obm-satellite/mods-available


%clean
rm -rf $RPM_BUILD_ROOT

%post           -n obm-satellite
/sbin/chkconfig --add obmSatellite

%preun          -n obm-satellite
if [ $1 = 0 ] ; then
/sbin/service obmSatellite stop >/dev/null 2>&1
/sbin/chkconfig --del obmSatellite
fi

%post           -n perl-ObmSatellite
perl -MXML::SAX -e "XML::SAX->add_parser(q(XML::SAX::PurePerl))->save_parsers()"

%files          -n perl-ObmSatellite
%defattr(-,root,root,-)
%{perl_vendorlib}/ObmSatellite
%{_mandir}/man3/ObmSatellite*

%files          -n obm-satellite
%defattr(-,root,root,-)
%{_bindir}/obm-satellite
%{_sbindir}/osdismod
%{_sbindir}/osenmod
%{_datadir}/obm-satellite
%{_localstatedir}/log/obm-satellite
%config(noreplace) %{_sysconfdir}/obm-satellite/obmSatellite.ini
%config(noreplace) %{_sysconfdir}/obm-satellite/mods-available/postfixSmtpInMaps
%config(noreplace) %{_sysconfdir}/obm-satellite/mods-available/cyrusPartition
%{_sysconfdir}/obm-satellite/mods-enabled
%{_sysconfdir}/init.d/obmSatellite
%config(noreplace) %{_sysconfdir}/logrotate.d/obm-satellite

%changelog
* Thu Dec 08 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-satellite-2.4.0.0-rc15
- New upstream release.
* Wed Nov 30 2011 Thomas Sarboni <tsarboni@linagora.com> - obm-satellite-2.4.0.0-rc14
- New upstream release.
* Tue Aug 25 2009 Sylvain Garcia <sylvain.garcia[at]obm.org> - 2.3.0-1
- initial release
