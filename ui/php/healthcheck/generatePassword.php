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
	$output = '[authentication] <br/> login='.$_POST['login'].' <br/> password='.$crypted_password;
	
	if (!empty($_POST['testUserLogin']) && !empty($_POST['testUserPassword'])) {
	  $output .= '<br /> <br /> [test-user] <br /> login=' . $_POST['testUserLogin'] . ' <br /> password=' . $_POST['testUserPassword'];
	}
	
	echo $output;
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
		#validator, #validatorTestUser {font-size:2.6em;color:#A00;font-weight:bold;}
		
		#introduction p{text-align:justify;}
		
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
	$.healthcheck.encryptionRequest = function(username, password, testUserLogin, testUserPassword){
		$.ajax({
			url: window.location,
			type: "POST",
			data: {"login": username, "password": password, "testUserLogin": testUserLogin, "testUserPassword": testUserPassword },
			error: function(jqXhr, errorType) {
				var status = jqXhr.status;
				if ( status == 0 || errorType) {
					status = 500;
				}
				$("#configurationFile").append(status);
			},
			success: function(response){
				$("#configurationFile").addClass("showConfig");
				$("#configurationFile")
					.html("; content of the /etc/obm/healthcheck.ini file <br /> <br />")
					.append(response);
			}
		});
	};

	$.healthcheck.validateForm = function() {
		var password = $.trim($("#password").val());
		var passwordConfirm = $.trim($("#passwordConfirm").val());
		var testUserPassword = $.trim($("#testUserPassword").val());
		var testUserPasswordConfirm = $.trim($("#testUserPasswordConfirm").val());
		var authPasswordsMatch = password == passwordConfirm;
		var testUserPasswordsMatch = testUserPassword == testUserPasswordConfirm;

		$("#validator").css("display", authPasswordsMatch ? "none" : "inline");
		$("#validatorTestUser").css("display", testUserPasswordsMatch ? "none" : "inline");
		
		if(authPasswordsMatch && testUserPasswordsMatch && password.length > 0 && testUserPassword.length > 0){
			$("#submitFormButton").prop('disabled', false).addClass("btn-success");
		} else {
			$("#submitFormButton").prop('disabled', true).removeClass("btn-success");
		}
	}

	$(document).ready( function(){
		$("#validator").css("display", "none");
		$("#validatorTestUser").css("display", "none");
		$("#submitFormButton").prop('disabled', true);

		$("#formSubmit").submit( function(event) {

			var username = $("#login").val();
			var password = $("#password").val();
			var testUserLogin = $("#testUserLogin").val();
			var testUserPassword = $("#testUserPassword").val();
			var passwordConfirm = $("#passwordConfirm").val();

			if ( password == passwordConfirm ) {
				$("#validator").css("display", "none");
				$.healthcheck.encryptionRequest(username, password, testUserLogin, testUserPassword);
			}
			event.preventDefault();
		});

		$("#password").keyup($.healthcheck.validateForm);
		$("#passwordConfirm").keyup($.healthcheck.validateForm);
		$("#testUserPassword").keyup($.healthcheck.validateForm);
		$("#testUserPasswordConfirm").keyup($.healthcheck.validateForm);

	});
</script>

<!-- End of Javascript -->

<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container">
			<a class="brand" href="" style="margin-left:10px;">OBM Health Check: First Access Configuration Page</a>
		</div>
	</div>
</div>

<div class="container" style="margin-top:50px">

	<div class="span7 alert alert-info" id="introduction">
		<h2>
			Welcome in OBM Health Check System
		</h2>
		<h3>Authentication configuration</h3>
		<p>
			To use OBM Health Check, you need a <strong>unique user and password</strong> in a configuration file.
			So please complete this form to generate your login and hashed password and paste the result in a file <strong>/etc/obm/healthcheck.ini</strong> on your Apache web server.
		</p>
		<h3>Using a test OBM user for checks</h3>
		<p>
		    Some checks require a test OBM user to run properly (e.g.: a complete obm-sync synchronization, IMAP checks, etc.).
		</p>
		<p>
		    To configure one, you 'll need a <b>[test-user]</b> section in the same configuration file.
		    This section should have two entries <i>login</i> and <i>password</i>.
		    You can also use the provided form to generate the configuration file snippet directly.
		</p>
		<h3>When you're done </h3>
		<p> <a href="index.php" class="btn btn-primary">Click here to access the Health Check</a> </p>
	</div>

	<div class="span3 alert alert-info">
		<h3>Generation Form</h3>
		<form id="formSubmit" class="form">
		    <b>Authentication</b>
			<fieldset>
				<input id="login" type="text" class="input" placeholder="Login">
				<input id="password" type="password" class="input" placeholder="Password">
				<input id="passwordConfirm" type="password" class="input" placeholder="Verify Password"><span id="validator">&times;</span>
			</fieldset>
			<b>Test User</b>
			<fieldset>
				<input id="testUserLogin" type="text" class="input" placeholder="Login">
				<input id="testUserPassword" type="password" class="input" placeholder="Password">
				<input id="testUserPasswordConfirm" type="password" class="input" placeholder="Verify Password"><span id="validatorTestUser">&times;</span>
			</fieldset>
			<button id="submitFormButton" type="submit" class="btn">Generate</button>
		</form>
	</div>

	<div class="span11">
		<div id="configurationFile">
		</div>
	</div>

</div>
</body>
</html>
