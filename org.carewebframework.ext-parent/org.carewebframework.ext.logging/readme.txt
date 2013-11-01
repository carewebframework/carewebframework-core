This project implements some log4j extensions meant to be used in conjunction with the perf4j performance logging framework.  It enables selectively displaying
performance measures within the CareWeb UI.  It does this through the use of a custom appender that can transform perf4j-formatted log entries into a form
suitable for display in the UI.  The appender uses a subclassed PatternLayout and PatternParser to perform this function.  In addition, a custom regex pattern-
based filter is provided to selectively filter which log entries are displayed.
