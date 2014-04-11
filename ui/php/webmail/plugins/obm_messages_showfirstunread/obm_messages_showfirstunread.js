/******************************************************************************
Copyright (C) 2014 Linagora

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

(function() {
    /**
     * Get the uid of the first unread item in the list.
     */
    function getFirstUnread(list) {
        var rows = list.rows;
        var minIndex = Number.MAX_VALUE;
        var minUid = 0;
        if (list.rowcount) {
            for (var n in rows) {
                var row = rows[n];
                if (row.obj.rowIndex < minIndex && row.unread) {
                    minIndex = row.obj.rowIndex;
                    minUid = n;
                }
            }
        }
        return minUid;
    }

    /**
     * Roundcube event listener to select the first unread message without
     * marking it as read.
     */
    function listUpdate(e) {
        var list = rcmail.message_list;
        var unread = getFirstUnread(list);
        if (unread) {
            // preview_pane_mark_read controls if the preview will be marked as
            // read automatically. Save the current value first.
            var oldValue = rcmail.env.preview_pane_mark_read;
            rcmail.env.preview_pane_mark_read = -1;

            // Now do the selection and revert the auto mark read to its last
            // value.  Roundcube uses rcmail.dblclick_time with a typical value
            // of 500, but the contact list code defaults to 200 and doesn't
            // use a constant. We double the timeout to make sure the preview
            // timer has a chance to fire.
            list.select(unread);
            setTimeout(function() {
                 rcmail.env.preview_pane_mark_read = oldValue;
            }, (rcmail.dblclick_time || 200) * 2);
        }

        // Finally, we only want to do this once on window load.
        rcmail.removeEventListener('listupdate', listUpdate);
    }

    // Initialize the event listener
    if (window.rcmail && rcmail.gui_objects.messagelist) {
        rcmail.addEventListener('listupdate', listUpdate);
    }
})();
