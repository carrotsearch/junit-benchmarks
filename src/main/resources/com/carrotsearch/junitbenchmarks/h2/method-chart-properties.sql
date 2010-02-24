
SELECT 
  ID AS "Run ID",
  TSTAMP AS "Run timestamp",
  JVM AS "JVM",
  OS AS "OS"
FROM RUNS R
WHERE R.ID = ?
