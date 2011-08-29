
SELECT 
  NAME AS "method",
  ROUND(ROUND_AVG, 2) AS "Average time [s]", 
  ROUND(ROUND_STDDEV, 2) AS "StdDev",
  ROUND(GC_AVG, 2) AS "GC average [s]",
  ROUND(GC_TIME, 2) AS "GC time [s]",
  ROUND(GC_STDDEV, 2) AS "StdDev",
  GC_INVOCATIONS AS "GC calls",
  BENCHMARK_ROUNDS AS "benchmark rounds",
  WARMUP_ROUNDS AS "warmup rounds",
  ROUND(TIME_BENCHMARK, 2) AS "Total benchmark time",
  ROUND(TIME_WARMUP, 2) AS "Total warmup time"
FROM TESTS T, RUNS R
WHERE R.ID = ?
  AND T.RUN_ID = R.ID
  AND CLASSNAME = ?
