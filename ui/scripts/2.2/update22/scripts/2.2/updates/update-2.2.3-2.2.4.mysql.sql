-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.3-2.2.4.mysql.sql
-- 2009-05-18 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id:$
-- /////////////////////////////////////////////////////////////////////////////

ALTER TABLE UserObm MODIFY `userobm_vacation_datebegin` datetime;
ALTER TABLE UserObm MODIFY `userobm_vacation_dateend` datetime;
UPDATE UserObm SET userobm_vacation_datebegin = NULL, userobm_vacation_dateend = NULL WHERE userobm_vacation_dateend < NOW() AND userobm_vacation_enable = 0;

-- allow float in soldtime, estimatedtime
ALTER TABLE Project MODIFY project_estimatedtime float;
ALTER TABLE Project MODIFY project_soldtime float;
