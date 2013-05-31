#!/usr/bin/env python
import pwd
import os
from distutils.core import setup

setup(name='obm healthcheck',
      version=''.join([line for line in open('version', 'r')]).strip(),
      description='local (server-side) healthcheck library for OBM components',
      author='OBM Core Team',
      author_email='obm@list.obm.org',
      url='http://www.obm.org',
      packages=['obm','obm.healthcheck'],
      license='Affero GPL 3'
     )
