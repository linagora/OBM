<?php
////////////////////////
// include for rtf generation (2x8 labels on A4)
/////////////////////////
// reference: RTF-Spec-1_3.rtf (Microsoft web site)
// Max 10-sep-01
// jlb 23-sep-01

//  controls word beginning with n, r or t must be protected:\\row, \\red
//  length unit: 1 twips = 1/1440 inch

///////////////// RTF header
// paperXnnn = control words for paper size
// A4 size in twips 
$paperw=11906; //in twips
$paperh=16838;
$margl=284;//264;
$margr=284;//264;
$margt=284;
$margb=284;
$left_indent=284;
$font=20; // in mid-points
$font_for_ref=12;
$row_heigh=floor(($paperh-$margt-$margb-270)/8); //100 for end of page //1985;
$cell3=$paperw-$margl-$margr;//11377;
$cell1=floor($cell3/2)-71;//5610;
$cell2=$cell1+142; //5710;
$tab_for_ref=$cell1-284;//5103; // 9cm
// Normal style: \s= style number \fs= font size in half-points \snext= next style
// \li284=  left indent
// \tq     right tabs
// \tx5103 tabs position in twips from th left margin (9cm)
 
$this->_tag_doc_head1="{\\rtf1\\ansi\\deff0
{\\fonttbl{\\f1\\froman\\fprq2\\fcharset0 Times;}}
{\\colortbl\\red0\\green0\\blue0;\\red255\\green255\\blue255;\\red128\\green128\\blue128;}
{\\stylesheet{\\li".$left_indent."\\nowidctlpar\\tqr\\tx".$tab_for_ref."\\adjustright \\f1\\fs".$font." Normal;}
}
{\\info{\\title ";
$this->_tag_doc_head2="}{\\subject Mailing List Extraction}}\n
\\paperh".$paperh."\\paperw".$paperw."
\\margl".$margl."\\margr".$margr."\\margt".$margt."\\margb".$margb.
"\\deftab709\\headery0\\footery0\n{\\fs12\\par }";

/////////////////Table tags
// A table is a sequence of table rows
// <row>:= <tbldef><cell>+ \row
// <cell>:= <textpar>+\cell
// A row begins with \trowd (table row default) and ends with \row.
// Table properties can be inherited, so the repeat of \trowd and associated
// control words is not necessary
// \trbrdrv= vertically centered; trrh= row height; cellx= cell right boundary 
$this->_tag_page_head="";
$this->_tag_table_head="\n\\trowd\\trql\\trrh-".$row_heigh.
"\clvertalc\cltxlrtb \\cellx".$cell1."\clvertalc\cltxlrtb \\cellx".$cell2.
"\clvertalc\cltxlrtb \\cellx".$cell3.
"\n\\pard\\plain\\li".$left_indent."\\nowidctlpar\\intbl\\tqr\\tx".$tab_for_ref.
"\\adjustright \\f1\\fs".$font." ";
$this->_tag_lbl_head="{";
$this->_tag_lbl1_end="\n\\cell\\cell}";
$this->_tag_lbl2_end="\n\\cell\\row}";
$this->_tag_field_ref_head="{\\fs".$font_for_ref." \\tab ";
$this->_tag_field_ref_end="}";
$this->_tag_field_head="";
$this->_tag_field_end=" \n\\par ";
$this->_tag_table_end="\n\\pard\\plain\\li".$left_indent."\\nowidctlpar\\tqr\\tx".$tab_for_ref.
"\\adjustright \\f1\\fs".$font;
$this->_tag_page_end="\n{\\fs12\\page \\par }";
$this->_tag_doc_end="}";
?>
