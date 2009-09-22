-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.11-2.2.12.pgsql.sql
-- 2009-09-22 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////


UPDATE ObmInfo SET obminfo_value = '2.2.13-pre' WHERE obminfo_name = 'db_version';
--
-- Name: event_domain_id_fkey; Type: INDEX; Schema: public; Owner: -; Tablespace:
--

CREATE INDEX event_domain_id_fkey ON event (event_domain_id);

--
-- Name: event_domain_id_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY event
    ADD CONSTRAINT event_domain_id_domain_id_fkey FOREIGN KEY (event_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

CREATE INDEX entityright_admin_key ON EntityRight (entityright_admin) ; 
CREATE INDEX entityright_read_key ON EntityRight (entityright_read) ; 
CREATE INDEX entityright_access_key ON EntityRight (entityright_access) ; 
CREATE INDEX entityright_write_key ON EntityRight (entityright_write) ;

UPDATE ObmInfo SET obminfo_value = '2.2.13' WHERE obminfo_name = 'db_version';

