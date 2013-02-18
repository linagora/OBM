<?php
/* ***** BEGIN LICENSE BLOCK *****  
 * Copyright (C) 2011-2012  Linagora  
 *  
 * This program is free software: you can redistribute it and/or modify it under  
 * the terms of the GNU Affero General Public License as published by the Free  
 * Software Foundation, either version 3 of the License, or (at your option) any  
 * later version, provided you comply with the Additional Terms applicable for OBM  
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public  
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)  
 * retain the displaying by the interactive user interfaces of the “OBM, Free  
 * Communication by Linagora” Logo with the “You are using the Open Source and  
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D  
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext  
 * links between OBM and obm.org, between Linagora and linagora.com, as well as  
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain  
 * from infringing Linagora intellectual property rights over its trademarks and  
 * commercial brands. Other Additional Terms apply, see  
 * <http://www.linagora.com/licenses/> for more details.  
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY  
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A  
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.  
 *  
 * You should have received a copy of the GNU Affero General Public License and  
 * its applicable Additional Terms for OBM along with this program. If not, see  
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License  
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms  
 * applicable to the OBM software.  
 * ***** END LICENSE BLOCK ***** */

require_once dirname(__FILE__) . '/auth/Authentication.php';

if ( !empty($_POST['login']) && !empty($_POST['password']) ) {
	$crypted_password = Authentication::getHasher()->hash($_POST['password']);

	echo '[authentication] <br/> login='.$_POST['login'].' <br/> password='.$crypted_password;
	exit();
}

?>
<!DOCTYPE html>
<head>
	<title>OBM Health Check: Configuration Page</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<link href="assets/css/bootstrap.css" rel="stylesheet">
	<link href="assets/css/bootstrap-responsive.css" rel="stylesheet">
	<style type="text/css">
		.showConfig{font-family:mono,sans-serif;background-color:#333;color:#FAFAFA;border-radius:5px;padding:15px;}
		#validator{font-size:2.9em;color:#A00;font-weight:bold;}
	</style>
</head>
<body>

<!-- Javascript -->

<script src="assets/js/jquery.js"></script>
<script src="assets/js/bootstrap.js"></script>
<script src="assets/js/bootstrap-tooltip.js"></script>
<script src="assets/js/bootstrap-popover.js"></script>
<script type="text/javascript">
	$.healthcheck = {};
	$.healthcheck.encryptionRequest = function(username, password){
		$.ajax({
			url: window.location,
			type: "POST",
			data: {"login": username, "password": password },
			error: function(jqXhr, errorType) {
				var status = jqXhr.status;
				if ( status == 0 || errorType) {
					status = 500;
				}
				$("#configurationFile").append(status);
			},
			success: function(response){
				$("#configurationFile").addClass("showConfig");
				$("#configurationFile").html(response);
			}
		});
	};

	$(document).ready( function(){
		$("#validator").css("display", "none");
		$("#submitFormButton").prop('disabled', true);

		$("#formSubmit").submit( function(event) {

			var username = $("#login").val();
			var password = $("#password").val();
			var passwordConfirm = $("#passwordConfirm").val();

			if ( password == passwordConfirm ) {
				$("#validator").css("display", "none");
				$.healthcheck.encryptionRequest(username, password);
			}
			event.preventDefault();
		});

		$("#passwordConfirm").keyup(function(){
			var password = $("#password").val();
			var passwordConfirm = $("#passwordConfirm").val();
			if(password == passwordConfirm){
				$("#validator").css("display", "none");
				$("#submitFormButton").prop('disabled', false).addClass("btn-success");
			} else {
				$("#validator").css("display", "inline");
				$("#submitFormButton").prop('disabled', true).removeClass("btn-success");
			}
		});

	});
</script>

<!-- End of Javascript -->

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="" style="margin-left:10px;">OBM Health Check: First Access Configration Page</a>
		</div>
	</div>
</div>

<div class="container" style="margin-top:50px">

	<div class="span7 alert alert-info" id="introduction">
		<h2>
			Welcome in OBM Health Check System
		</h2>
		<h3>It's your first access to Health Check OBM:</h3>
		<p>
			To use OBM healthcheck you need a <strong>unique user and password</strong> in a configuration file <strong>heathcheck.ini</strong> in <strong>/etc/obm</strong> you <strong>must be created</strong>. <br/><br/>
			So please complete this form to generate your login and hashed password to paste in <strong>heathcheck.ini</strong>.
		</p>
	</div>

	<div class="span3 alert alert-info">
		<h3>Generation Form</h3>
		<form id="formSubmit" class="form">
			<fieldset class="pull-right">
				<input id="login" type="text" class="input" placeholder="Login">
				<input id="password" type="text" class="input" placeholder="Password">
				<input id="passwordConfirm" type="text" class="input" placeholder="Verify Password"><span id="validator">&times;</span>
				<br/>
				<button id="submitFormButton" type="submit" class="btn">Hash my password</button>
			</fieldset>
		</form>
	</div>

	<div class="span11">
		<div id="configurationFile">
		</div>
	</div>

</div>
</body>
</html>