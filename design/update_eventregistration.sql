-- Add columns for food preferences to EventRegistration table
ALTER TABLE mod4db.EventRegistration
ADD COLUMN wants_food BOOLEAN DEFAULT false,
ADD COLUMN dietary_restrictions TEXT;

