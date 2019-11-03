ALTER TABLE todo ADD COLUMN completed BOOLEAN;
UPDATE todo set completed=false;
ALTER TABLE todo ALTER COLUMN completed SET NOT NULL;