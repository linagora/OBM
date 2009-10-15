<script language="php">
///////////////////////////////////////////////////////////////////////////////
// OBM - Language : Romanian                                                   //
//     - File     : publication.inc                                          //
// 2004-01-29 Mehdi Rande                                                    //
///////////////////////////////////////////////////////////////////////////////
// $Id: publication.inc 2972 2008-07-02 09:42:54Z mehdi $ //
///////////////////////////////////////////////////////////////////////////////
// Modified on 2009-09-22T09:58:31+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:06:49+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:10:27+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:13:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:13:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:18:22+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:20:17+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:49:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T10:57:16+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:04:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:13:59+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:21:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:23:51+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:29:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:44:33+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-22T11:50:01+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T11:57:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:02:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:05:42+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:09:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:16:45+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:12+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:18:43+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:53+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:19:55+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:22:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:23:18+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:25:08+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:26:23+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:30:06+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T12:32:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:31:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:32:09+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:35:36+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:46+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T13:37:48+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:10:13+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:18:20+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:26+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:28+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:19:30+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:20:44+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:27:19+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:27:34+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:30:39+02:00 using OBMtranslation (0.4.1)
// Modified on 2009-09-23T17:30:41+02:00 using OBMtranslation (0.4.1)

$l_publication_title = "Titlul Publicației";
$l_type = "Tip";
$l_renew = "Reînnoire automată"; 
$l_recept = "Mod de recepționare";
$l_reception = "Mod de recepționare";
$l_publication_type = "Tip de Publicație";
$l_quantity = "Cantitate";
$l_header_new_auto = "Reînnoire";
$l_type_manage = "Gestionarea Tipurilor de Publicații";
$l_type_exist = "Tipuri existente";
$l_type_new = "Tip Nou";
$l_renew_manage = "Gestionarea Modurilor de reînnoire";
$l_renew_exist = "Moduri de reînnoire existente";
$l_renew_new = "Mod de reînnoire Nou";
$l_recept_manage = "Gestionarea Modurilor de recepționare";
$l_recept_exist = "Moduri de recepționare existente";
$l_recept_new = "Mod de recepționare nou";
$l_auto_sub = "Abonament automatic";
$l_contact_category1 = "Categorie contact 1";
$l_publication_orig = "Publicația originală";
$l_publication_dest = "Destinația Publicației";
$l_subscription = "Abonament";
$l_insert = "Introduceți Publicația";
$l_update = "Modificați Publicația";
$l_checkdelete = "Verificați sau Ștergeți Publicația";
$l_delete = "Ștergeți Publicația";
$l_insert_sub = "Introduceți Abonamentul";
$l_update_sub = "Modificați Abonamentul";
$l_checkdelete_sub = "Verificați sau Ștergeți Abonamentul";
$l_delete_sub = "Ștergeți Abonamentul";
$l_type_update = "Modificați Tipul";
$l_type_checkdelete = "Verificați sau Ștergeți Tipul";
$l_type_delete = "Ștergeți Tipul";
$l_type_insert = "Creați Tipul";
$l_renew_update = "Modificați Modul de reînnoire";
$l_renew_checkdelete = "Verificați sau Ștergeți Modul de reînnoire";
$l_renew_delete = "Ștergeți Modul de reînnoire";
$l_renew_insert = "Creați Modul de reînnoire";
$l_recept_update = "Modificați Modul de recepționare";
$l_recept_checkdelete = "Verificați sau Ștergeți Modul de recepționare";
$l_recept_delete = "Ștergeți Modul de recepționare";
$l_recept_insert = "Creați Modul de recepționare";
$l_no_display = "Completați spațiile și apăsați butonul  <Caută > pentru a obține lista publicațiilor corespunzătoare.";
$l_found = "publicații găsite.";
$l_no_found = "Nicio publicație nu corespunde acestor criterii!";
$l_check_samepublication = "Aceste publicații sunt asemănătoare celei noi introduse!";
$l_link_contact = "Există contacte abonate la această publicație";
$l_link_contact_no = "Niciun contact nu este abonat la această publicație";
$l_can_delete = "Publicația nu poate fi ștearsă.";
$l_cant_delete = "Publicația nu poate fi ștearsă.";
$l_type_link_publication = "Aceste publicații sunt asociate tipului";
$l_type_link_publication_no = "Nicio publicație nu este asociată tipului";
$l_type_cant_delete = "Tipul nu poate fi șters!";
$l_type_can_delete = "Tipul poate fi șters.";
$l_renew_link_publication = "Aceste publicații sunt asociate modului de reînnoire";
$l_renew_link_publication_no = "Nicio publicație nu este asociată modului de reînnoire";
$l_renew_cant_delete = "Modul de reînnoire nu poate fi șters!";
$l_renew_can_delete = "Modul de reînnoire poate fi șters.";
$l_recept_link_publication = "Aceste publicații sunt asociate modului de recepționare";
$l_recept_link_publication_no = "Nicio publicație nu este asociată modului de recepționare";
$l_recept_cant_delete = "Modul de recepționare nu poate fi șters!";
$l_recept_can_delete = "Modul de recepționare poate fi șters.";
$l_fill_title = "Introduceți Titlul înainte de a valida.";
$l_delete_confirm = "Confirmați ștergerea Publicației?";
$l_j_check_year = "Anul nu este corect.";
$l_j_fill_lang = "Introduceți Limba înainte de a valida;";
$l_j_check_lang = "Limba indicată nu este corectă";
$l_j_type_delete = "Selectați un tip pentru a fi verificat sau șters!";
$l_j_type_update = "Selectați un tip și actualizați înainte de a valida!";
$l_j_type_create = "Introduceți un nume de tip pentru a fi creat!";
$l_j_renew_delete = "Selectați modul de reînnoire pentru a fi verificat sau șters!";
$l_j_renew_update = "Selectați un mod de reînnoire și actualizați înainte de a valida!";
$l_j_renew_create = "Indicați un nume modului de reînnoire pentru a fi creat!";
$l_j_recept_delete = "Selectați un mod de recepționare pentru a fi verificat sau șters!";
$l_j_recept_update = "Selectați un mod de recepționare și actualizați înainte de a valida!";
$l_j_recept_create = "Introduceți un nume modului de recepționare perntru a fi creat!";
$l_j_select_publication = "Selectați o publicație!";
$l_j_fill_quantity = "Specificați o cantitate";
$l_j_check_quantity = "Indicați o cantitate valabilă";
$l_publication_display = "Opțiuni de vizualizare a listelor de publicații";

