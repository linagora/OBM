DROP INDEX _userpattern_pattern_idx; 

-- For some reason this increases performance on some setups...
ALTER TABLE _userpattern ALTER COLUMN pattern TYPE TEXT;

CREATE INDEX _userpattern_pattern_idx ON _userpattern (pattern text_pattern_ops);
