# common.awk / Fink / 2001-07
# remove common phrases from language file
# usage: awk -f common.awk commonfile languagefile
#-----------------------------------------------------------------
#-----------------------------------------------------------------
BEGIN { ii = 0 
FS=" ="
}

{
    if (filename != FILENAME)
    {
###		first line in first file (filename is empty)
	if (filename == "")
	{
	    filename = FILENAME
	    fileno = 1
	}
	else
	{
	    filename = FILENAME
	    fileno++
	}
    }
}

{
    if ($1 in tindex)
	{
###	   this is a common phrase 
	}
    else
	{
###		first file goes directly into table
	    if (fileno == 1)
		{
##			nothing to do
		}
		else
		{
			print $0
		}
	}
}

###	make index of keys (variables) from common file

/^\$/ {   if (fileno == 1)	tindex[$1] = ii }


END {
}
