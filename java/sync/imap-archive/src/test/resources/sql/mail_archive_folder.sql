CREATE TABLE mail_archive_folder (
	id		SERIAL PRIMARY KEY,
	folder	TEXT,

	CONSTRAINT mail_archive_folder_folder_ukey UNIQUE (folder)
);

