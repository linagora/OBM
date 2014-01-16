<?php
/*
# Copyright (C) 2011-2014 Linagora
# 
# This program is free software: you can redistribute it and/or modify it under
# the terms of the GNU Affero General Public License as published by the Free
# Software Foundation, either version 3 of the License, or (at your option) any
# later version, provided you comply with the Additional Terms applicable for OBM
# software by Linagora pursuant to Section 7 of the GNU Affero General Public
# License, subsections (b), (c), and (e), pursuant to which you must notably (i)
# retain the displaying by the interactive user interfaces of the “OBM, Free
# Communication by Linagora” Logo with the “You are using the Open Source and
# free version of OBM developed and supported by Linagora. Contribute to OBM R&D
# by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
# links between OBM and obm.org, between Linagora and linagora.com, as well as
# between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
# from infringing Linagora intellectual property rights over its trademarks and
# commercial brands. Other Additional Terms apply, see
# <http://www.linagora.com/licenses/> for more details.
#
# This program is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
# PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License and
# its applicable Additional Terms for OBM along with this program. If not, see
# <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
# version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
# applicable to the OBM software.
*/
?>
<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - File : install_document_2.1.php                                     //
//     - Desc : OBM install : create default Document repository             //
// 2005-08-14 Pierre Baudracco                                               //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////

$obminclude = getenv("OBM_INCLUDE_VAR");
$path = "../../php";

if ($obminclude == "") $obminclude = "../../obminclude";
include("$obminclude/global.inc");
include("../../php/admin_data/admin_data_query.inc");

// Check Document path is set
if (! isset($cdocument_root)) {
  echo "The document repository root is not set ! (\$cdocument_root)\n";
  exit(1);
}

// If document root does not exist, try to create it
if (! is_dir($cdocument_root)) {
  echo "The document repository root does not exist. Trying to create it\n";
  if (! mkdir($cdocument_root)) {
    echo "The document repository root can not be created. Check the path\n";
    exit(1);
  }
}

// Check that repository root is writable
$files = get_admin_data_file_list($cdocument_root);
$nb = count($files);
if (! is_writable($cdocument_root)) {
  echo "Document repository root, is not writable. Check user and access rights !\n";
  exit(1);
}


// Check that repository is empty, else alert and exit
$files = get_admin_data_file_list($cdocument_root);
$nb = count($files);
if ($nb > 0) {
  echo "Document repository is not empty ! Install stopped\n";
  exit(1);
}


// Populate the repository root with storage dirs
for ($dir_num = 0; $dir_num < 10; $dir_num++) {
  $sdir = "$cdocument_root/$dir_num";
  if (! mkdir($sdir)) {
    echo "Error creating the storage directory : $sdir !\n";
    exit(1);
  } else {
    chmod($sdir, 0777);
  }
}

echo "Success : Document repository created and initialized\n";
exit(0);
</script>
