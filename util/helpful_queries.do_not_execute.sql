-- Copyright (c) 2011 Skye Book
-- helpful_queries.do_not_execute.sql - A collection of queries for working with the Betaville database.
-- WARNING: DO NOT EXECUTE THIS SCRIPT AS IT WILL MAKE SEVERE CHANGES TO YOUR DATABASE!
-- Released under the FreeBSD license

-- Get all of the base designs in a city
SELECT * FROM design LEFT JOIN proposal ON design.designid=proposal.destinationid WHERE proposal.destinationid IS NULL AND cityid=2;

-- Delete all of the base designs in a city
UPDATE design LEFT JOIN proposal ON design.designid=proposal.destinationid set isalive=0 WHERE proposal.destinationid IS NULL AND cityid=2;