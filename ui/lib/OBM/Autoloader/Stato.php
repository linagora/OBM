<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/


/**
 * Stato loader 
 * 
 * @package OBM_Autoloader 
 * @version $Id:$
 * @copyright Copyright Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 */

class OBM_Autoloader_Stato {

  const sabstractadapter		    = 'orm/lib/adapters/abstract.php';
  const sabstractbackend		    = 'i18n/lib/backend/abstract.php';
  const sabstractserializer		    = 'webflow/lib/serializers.php';
  const sacceptitem			    = 'webflow/lib/mime_type.php';
  const sactioncachefilter		    = 'webflow/lib/action_view.php';
  const sactioncontroller 		    = 'webflow/lib/action_controller.php';
  const sactionview			    = 'webflow/lib/action_view.php';
  const sactiverecord 			    = 'orm/lib/active_record.php';
  const sactiverecorddecorator		    = 'orm/lib/decorators/base_decorator.php';
  const sactiverecordform 		    = 'webflow/lib/form/active_record_form.php';
  const sadapternotfound 		    = 'orm/lib/active_record.php';
  const sadapternotspecified 		    = 'orm/lib/active_record.php';
  const sapacheformatter		    = 'common/lib/logger.php';
  const saroundfilter			    = './webflow/lib/filters.php';
  const sassertionerror 		    = 'orm/lib/query_set.php';
  const sassociation			    = 'orm/lib/associations/association.php';
  const sassociationmanager		    = 'orm/lib/associations/association.php';
  const sassociationmeta		    = 'orm/lib/associations/association.php';
  const sassociationtypemismatch 	    = 'orm/lib/active_record.php';
  const sauthexception 			    = 'webflow/lib/cookie.php';
  const sbase64				    = 'common/lib/base64.php';
  const sbasicformatter			    = 'common/lib/logger.php';
  const sbelongstomanager 		    = 'orm/lib/associations/belongs_to_association.php';
  const sbelongstometa 			    = 'orm/lib/associations/belongs_to_association.php';
  const sbooleanfield 			    = 'webflow/lib/form/field.php';
  const sboundfield			    = 'webflow/lib/form.php';
  const scachestore			    = 'webflow/lib/caching.php';
  const scharfield 			    = 'webflow/lib/form/field.php';
  const scheckboxinput 			    = 'webflow/lib/form/input.php';
  const scheckboxmultipleselect 	    = 'webflow/lib/form/input.php';
  const schoicefield 			    = 'webflow/lib/form/field.php';
  const scodegenerator			    = 'cli/lib/code_generator.php';
  const scollectionchoicefield 		    = 'webflow/lib/form/active_record_form.php';
  const scollectionmultiplechoicefield 	    = 'webflow/lib/form/active_record_form.php';
  const scolumn				    = 'orm/lib/column.php';
  const scommand			    = 'cli/lib/command.php';
  const scomponent			    = 'webflow/lib/routes.php';
  const sconfiguration			    = 'common/lib/initializer.php';
  const sconsoleexception 		    = 'cli/lib/console_utils.php';
  const sconsoleutils			    = 'cli/lib/console_utils.php';
  const scontrollercomponent 		    = 'webflow/lib/routes.php';
  const scookie				    = 'webflow/lib/cookie.php';
  const scsv				    = 'orm/lib/csv.php';
  const scsviterator 			    = 'orm/lib/csv.php';
  const scsvstream			    = 'orm/lib/csv.php';
  const scsvwriter			    = 'orm/lib/csv.php';
  const scycle				    = 'webflow/lib/helpers/text_helper.php';
  const sdate				    = 'common/lib/date.php';
  const sdateconstructexception 	    = 'common/lib/date.php';
  const sdateexception 			    = 'common/lib/date.php';
  const sdateinput 			    = 'webflow/lib/form/input.php';
  const sdateparsingexception 		    = 'common/lib/date.php';
  const sdatetime 			    = 'common/lib/date.php';
  const sdatetimefield 			    = 'webflow/lib/form/field.php';
  const sdatetimeinput 			    = 'webflow/lib/form/input.php';
  const sdblibrarywrapper		    = './orm/lib/adapters/abstract.php';
  const sdependencies			    = 'common/lib/dependencies.php';
  const sdependencynotfound 		    = 'common/lib/dependencies.php';
  const sdir 				    = 'common/lib/dir.php';
  const sdispatchexception 		    = 'webflow/lib/dispatcher.php';
  const sdispatcher			    = 'webflow/lib/dispatcher.php';
  const sdoublerenderexception 		    = 'webflow/lib/action_controller.php';
  const sduplicateversionmigrationexception = 'orm/lib/migration.php';
  const sdynamiccomponent 		    = 'webflow/lib/routes.php';
  const semailfield 			    = 'webflow/lib/form/field.php';
  const sencryption			    = 'common/lib/encryption.php';
  const sfield				    = 'webflow/lib/form/field.php';
  const sfilefield 			    = 'webflow/lib/form/field.php';
  const sfileinput 			    = 'webflow/lib/form/input.php';
  const sfilestore 			    = 'webflow/lib/caching.php';
  const sfilledqueryset 		    = 'orm/lib/query_set.php';
  const sfilterchain			    = 'webflow/lib/filters.php';
  const sfixture			    = 'orm/lib/fixture.php';
  const sflash 				    = 'webflow/lib/flash.php';
  const sfloatfield 			    = 'webflow/lib/form/field.php';
  const sform 				    = 'webflow/lib/form.php';
  const sformbuilder			    = 'webflow/lib/helpers/form_builder.php';
  const sformerrors 			    = 'webflow/lib/form.php';
  const sformexception 			    = 'webflow/lib/form.php';
  const shasmanymanager 		    = 'orm/lib/associations/has_many_association.php';
  const shasmanymeta 			    = 'orm/lib/associations/has_many_association.php';
  const shasmanythroughexception 	    = 'orm/lib/associations/has_many_through_association.php';
  const shasmanythroughmanager 		    = 'orm/lib/associations/has_many_through_association.php';
  const shasonemanager 			    = 'orm/lib/associations/has_one_association.php';
  const shasonemeta 			    = 'orm/lib/associations/has_one_association.php';
  const shiddeninput 			    = 'webflow/lib/form/input.php';
  const shttp404 			    = 'webflow/lib/action_controller.php';
  const shttpmethodnotimplemented 	    = 'webflow/lib/resource.php';
  const si18n				    = 'i18n/lib/i18n.php';
  const si18nexception 			    = 'i18n/lib/i18n.php';
  const sidispatchable			    = './webflow/lib/dispatcher.php';
  const sifielddecorator		    = './webflow/lib/form.php';
  const sifilterable			    = './webflow/lib/filters.php';
  const simailtransport			    = './mailer/lib/mail.php';
  const siserializable			    = './webflow/lib/serializers.php';
  const sinflection			    = 'common/lib/inflection.php';
  const sinitializer			    = 'common/lib/initializer.php';
  const sinput				    = 'webflow/lib/form/input.php';
  const sintegerfield 			    = 'webflow/lib/form/field.php';
  const sinvalidhttpstatuscode 		    = 'webflow/lib/response.php';
  const sinvalidstatementexception 	    = 'orm/lib/adapters/abstract.php';
  const sipfield 			    = 'webflow/lib/form/field.php';
  const sirreversiblemigrationexception     = 'orm/lib/migration.php';
  const sjsonserializer 		    = 'webflow/lib/serializers.php';
  const slistdecorator 			    = 'orm/lib/decorators/list_decorator.php';
  const slogger				    = 'common/lib/logger.php';
  const smail 				    = 'mailer/lib/mail.php';
  const smailexception 			    = 'mailer/lib/mail.php';
  const smailer				    = 'mailer/lib/mailer.php';
  const smanager			    = 'orm/lib/manager.php';
  const smanyassociationmanager		    = 'orm/lib/associations/association.php';
  const smanytomanymanager 		    = 'orm/lib/associations/many_to_many_association.php';
  const smanytomanymeta 		    = 'orm/lib/associations/many_to_many_association.php';
  const smapper				    = 'orm/lib/mapper.php';
  const smemcachestore 			    = 'webflow/lib/caching.php';
  const smigration			    = 'orm/lib/migration.php';
  const smigrator			    = 'orm/lib/migration.php';
  const smime				    = 'mailer/lib/mime/mime.php';
  const smimeattachment 		    = 'mailer/lib/mime/attachment.php';
  const smimeentity			    = 'mailer/lib/mime/entity.php';
  const smimeexception 			    = 'mailer/lib/mime/mime.php';
  const smimemultipart 			    = 'mailer/lib/mime/multipart.php';
  const smimepart 			    = 'mailer/lib/mime/part.php';
  const smimetype			    = 'webflow/lib/mime_type.php';
  const smissingtemplateexception 	    = 'webflow/lib/action_view.php';
  const smultiplechoicefield 		    = 'webflow/lib/form/field.php';
  const smultipleselect 		    = 'webflow/lib/form/input.php';
  const smysqladapter 			    = 'orm/lib/adapters/mysql.php';
  const smysqllibrarywrapper 		    = 'orm/lib/adapters/library_wrappers/mysql.php';
  const snamedroutes			    = 'webflow/lib/routes.php';
  const sobservable			    = 'orm/lib/observable.php';
  const sobserver			    = './orm/lib/observable.php';
  const soptionshash			    = 'common/lib/initializer.php';
  const spaginationhelper		    = 'webflow/lib/helpers/pagination_helper.php';
  const spaginator			    = 'orm/lib/paginator.php';
  const spasswordinput 			    = 'webflow/lib/form/input.php';
  const spathcomponent 			    = 'webflow/lib/routes.php';
  const spdomysqllibrarywrapper 	    = 'orm/lib/adapters/library_wrappers/pdo_mysql.php';
  const sphpsession 			    = 'webflow/lib/session.php';
  const sphpsessionhandler		    = './webflow/lib/session.php';
  const spresenter			    = 'webflow/lib/presenter.php';
  const squeryset 			    = 'orm/lib/query_set.php';
  const sradioselect 			    = 'webflow/lib/form/input.php';
  const srecordnotfound 		    = 'orm/lib/query_set.php';
  const srecordnotsaved 		    = 'orm/lib/active_record.php';
  const srequest			    = 'webflow/lib/request.php';
  const srequestfiles 			    = 'webflow/lib/request.php';
  const srequestparams 			    = 'webflow/lib/request.php';
  const srescue				    = 'webflow/lib/rescue.php';
  const sresource 			    = 'webflow/lib/resource.php';
  const sresponse			    = 'webflow/lib/response.php';
  const sroute				    = 'webflow/lib/routes.php';
  const srouteset			    = 'webflow/lib/routes.php';
  const sroutes				    = 'webflow/lib/routes.php';
  const sroutingexception 		    = 'webflow/lib/routes.php';
  const sselect 			    = 'webflow/lib/form/input.php';
  const ssendmailtransport 		    = 'mailer/lib/transports/sendmail.php';
  const ssimplebackend 			    = 'i18n/lib/backend/simple.php';
  const ssmtptransport 			    = 'mailer/lib/transports/smtp.php';
  const ssmtptransportexception 	    = 'mailer/lib/transports/smtp.php';
  const sstaticcomponent 		    = 'webflow/lib/routes.php';
  const stable				    = 'orm/lib/table.php';
  const stablemap			    = 'orm/lib/table_map.php';
  const stempfile			    = 'common/lib/tempfile.php';
  const stextfield 			    = 'webflow/lib/form/field.php';
  const stextinput 			    = 'webflow/lib/form/input.php';
  const stextarea 			    = 'webflow/lib/form/input.php';
  const stimeinput 			    = 'webflow/lib/form/input.php';
  const streedecorator 			    = 'orm/lib/decorators/tree_decorator.php';
  const sunknownactionexception 	    = 'webflow/lib/action_controller.php';
  const sunknowncontrollerexception 	    = 'webflow/lib/action_controller.php';
  const sunknownhttpmethod 		    = 'webflow/lib/request.php';
  const sunknownprotocolexception 	    = 'webflow/lib/action_controller.php';
  const sunknownresourceexception 	    = 'webflow/lib/resource.php';
  const sunknownserviceexception 	    = 'webflow/lib/action_controller.php';
  const sunkownformat 			    = 'webflow/lib/serializers.php';
  const suploadedfile			    = 'webflow/lib/request.php';
  const surl				    = 'webflow/lib/routes.php';
  const surlfield 			    = 'webflow/lib/form/field.php';
  const surlrewriter			    = 'webflow/lib/url_rewriter.php';
  const svalidation			    = 'orm/lib/validation.php';
  const svalidationerror 		    = 'webflow/lib/form/field.php';
  const svaluesqueryset 		    = 'orm/lib/query_set.php';
  const sxliffbackend 			    = 'i18n/lib/backend/xliff.php';
  const sxmlserializer 			    = 'webflow/lib/serializers.php';
  const syamlbackend 			    = 'i18n/lib/backend/yaml.php';
  const scaffoldcommand 		    = 'webflow/script/scaffold.php';
  const serializableerror		    = 'webflow/lib/serializers.php';

  /**
   * Attempt to autoload a class
   *
   * @param  string $class
   * @return mixed False if not matched, otherwise result if include operation
   */
  public function autoload($class) {
    $classPath = $this->getClassPath($class);
    if ($classPath !== false) {
      return include $classPath;
    }
    return false;
  }  

  /**
   * Helper method to calculate the correct class path
   *
   * @param string $class
   * @return False if not matched other wise the correct path
   */
  public function getClassPath($class) {
    if(defined('self::'.strtolower($class))) {
      return 'Stato/'.constant('self::'.strtolower($class));
    }
    return false;
  }
}
