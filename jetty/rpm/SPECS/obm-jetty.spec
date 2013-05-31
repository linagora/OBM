# Define python sitelib location and pyver if not exists
%{!?python_sitelib: %define python_sitelib %(%{__python} -c "from distutils.sysconfig import get_python_lib; print get_python_lib()")}
%{!?pyver: %define pyver %(%{__python} -c "import sys ; print sys.version[:3]")}

# Force using the same RPM properties as EL5
%global _source_filedigest_algorithm 1
%global _binary_filedigest_algorithm 1
%global _binary_payload w9.gzdio
%global _source_payload w9.gzdio

# Define obm-jetty configuration
%global obmjettyconf %{_sysconfdir}/jetty6

Name: obm-jetty
Version: %{obm_version}
Release: %{obm_release}%{?dist}
Summary: configuration for Jetty for Open Business Management
Vendor: obm.org
URL: http://www.obm.org
Group: Development/Tools
License: GPLv2+
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0: obm-jetty-%{version}.tar.gz
Source1: jetty.xml.sample
Source2: jetty-logging.xml.sample
Source3: jetty-sysconfig
Source4: jetty-init
Source5: local-healthcheck
BuildArch: noarch
Requires(post): jetty6
Requires: python-json >= 3.4, python-setuptools >= 0.6
BuildRequires: python-nose >= 0.11, python-minimock >= 1.2.5, python-devel python-setuptools-devel

%description
It allows Jetty Server to start after its install and changes the port.

OBM is a global groupware, messaging and CRM application. It is intended to
be an Exchange Or Notes/Domino Mail replacement, but can also be used as a
simple contact database. OBM also features integration with PDAs, smartphones,
Mozilla Thunderbird/Lightning and Microsoft Outlook via specific connectors.

%prep
%setup -q -n obm-jetty-%{version}
echo -n %{version} > version 

%build
CFLAGS="$RPM_OPT_FLAGS" %{__python} setup.py build 

%install
rm -rf $RPM_BUILD_ROOT
%{__python}  setup.py install -O1 --skip-build --root $RPM_BUILD_ROOT \
        --install-data=%{_datadir} --install-purelib=%{python_sitelib}

install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/obm-local-healthcheck
install -d -m 755 $RPM_BUILD_ROOT%{_bindir}
install -p -m 755 bin/local-healthcheck $RPM_BUILD_ROOT%{_bindir}/obm-local-healthcheck

install -d -m 755 $RPM_BUILD_ROOT%{_docdir}/obm-jetty
install -p -m 644 %{SOURCE1} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty.xml.sample
install -p -m 644 %{SOURCE2} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-logging.xml.sample
install -p -m 644 %{SOURCE3} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-sysconfig.sample
install -p -m 755 %{SOURCE4} $RPM_BUILD_ROOT%{_docdir}/obm-jetty/jetty-init.sample

install -d -m 755 $RPM_BUILD_ROOT%{_datadir}/obm-local-healthcheck/020-Jetty
install -p -m 644 %{SOURCE5}/*.py $RPM_BUILD_ROOT%{_datadir}/obm-local-healthcheck/020-Jetty/

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root,-)
%{_docdir}/obm-jetty/jetty.xml.sample
%{_docdir}/obm-jetty/jetty-logging.xml.sample
%{_docdir}/obm-jetty/jetty-sysconfig.sample
%{_docdir}/obm-jetty/jetty-init.sample
%{python_sitelib}/obm_healthcheck-%{version}.egg-info
%{python_sitelib}/obm
%{_datadir}/obm-local-healthcheck
%{_bindir}/obm-local-healthcheck

%post
for f in jetty.xml jetty-logging.xml ; do
  if [ -e %{obmjettyconf}/${f} ] ; then
    if [ %{obmjettyconf}/${f} -nt %{_docdir}/obm-jetty/${f}.sample ] ; then
      cp -p %{obmjettyconf}/${f} %{obmjettyconf}/${f}.orig
    fi
  fi
  cp -p %{_docdir}/obm-jetty/${f}.sample %{obmjettyconf}/${f}
done

cp -p %{_docdir}/obm-jetty/jetty-sysconfig.sample %{_sysconfdir}/sysconfig/jetty6
cp -p %{_docdir}/obm-jetty/jetty-init.sample %{_sysconfdir}/init.d/jetty6

service jetty6 restart || :
