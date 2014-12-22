ALTER TABLE EventTemplate ADD COLUMN eventtemplate_notify boolean DEFAULT true;

ALTER TABLE mail_archive ADD COLUMN mail_archive_main_folder TEXT NOT NULL DEFAULT 'ARCHIVE';
