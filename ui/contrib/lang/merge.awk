# merge.awk / Fink / 2000-02
# merge two language files
# usage: awk -f merge.awk file1 file2
#-----------------------------------------------------------------
# 2001-07-20 / Fink / added 'not found in file1'
#                     added FS
#-----------------------------------------------------------------
BEGIN { ii = 0 
FS=" ="
}

{
    if (filename != FILENAME)
    {
###		first line of first file (filename is empty)
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
	    table[tindex[$1]] = $0
	}
    else
	{
		ii++
###		first file goes directly into table so that the sequence remains
	    if (fileno == 1)
		{
		    table[ii] = $0
		}
		else
		{
		    table[ii] = "not found in file1: " $0
		}
	}
}

###	make index of keys (variables) from first file

/^\$/ {   if (fileno == 1)	tindex[$1] = ii }


END {
    iimax = ii
    for (ii = 1; ii <= iimax; ii++)
	print table[ii]
}
