This project implements performance monitoring capabilities within the CareWeb Framework by integrating perf4j with ZK Framework performance monitoring hooks.  The latter
enables the measurement of performance metrics at the request and event levels.  To enable performance monitoring, the PerformanceMonitor class must be declared as a
listener in the zk.xml configuration file as follows:

		<listener>
		     <description>Performance monitor</description>
		     <listener-class>org.carewebframework.logging.perf4j.PerformanceMonitor</listener-class>
		</listener>}
