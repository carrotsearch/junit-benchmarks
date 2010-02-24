
SELECT 
  NAME AS "method",
  ROUND(ROUND_AVG, 4) AS "Average time [s]", 
  ROUND(ROUND_STDDEV, 4) AS "StdDev",
  ROUND(GC_AVG, 4) AS "GC time [s]",
  ROUND(GC_STDDEV, 4) AS "StdDev",
  GC_INVOCATIONS AS "GC calls",
  BENCHMARK_ROUNDS AS "benchmark rounds",
  WARMUP_ROUNDS AS "warmup rounds",
  TIME_BENCHMARK AS "Total benchmark time",
  TIME_WARMUP AS "Total warmup time"
FROM TESTS T, RUNS R
WHERE R.ID = ?
  AND T.RUN_ID = R.ID
  AND CLASSNAME = ?
