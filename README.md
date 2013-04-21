activemq-stats-web
==================

An interactive web site for embedding into an ActiveMQ and / or Camel server

Fork the project and build it using maven

Copy the amqstats.war file to the ActiveMq webapps directory and the statisticsbeans.jar to the ActiveMQ lib directory.

Add the following handler to the "handler" property in the jetty.xml file in the ActiveMq conf directory:

                        <bean class="org.eclipse.jetty.webapp.WebAppContext">
                            <property name="contextPath" value="/amqstats" />
                            <property name="war" value="${activemq.home}/webapps/amqstats.war" />
                            <property name="logUrlOnStart" value="true" />
                        </bean>

Restart ActiveMQ and go to the ActiveMQ web url, with a context of "amqstats", e.g.:

	http://localhost:8161/amqstats/

If you want to know the average message size of messages traversing your camel routes, wire in the MessageSize statistics bean into your route:


    <bean id="filetx-addSizeStatsBean" class="org.dsa.amq.amqstats.camel.AddMessageSizeToStats" >
	<constructor-arg ref="jmxCamel" />	
	<constructor-arg value="filetx" />
    </bean>
    <bean id="amq-route-addSizeStatsBean" class="org.dsa.amq.amqstats.camel.AddMessageSizeToStats" >
	<constructor-arg ref="jmxCamel" />	
	<constructor-arg value="amq-route" />
    </bean>

	<bean id="jmxCamel" class="org.dsa.amq.amqstats.jmx.JmxCamel">
	</bean>

<!--
	<bean id="loggingErrorHandler" class="org.apache.camel.builder.LoggingErrorHandlerBuilder">
	  <property name="logName" value="camel.name"/>
	  <property name="level" value="ERROR"/>
	</bean>

	<camelContext errorHandlerRef="loggingErrorHandler" id="camel" xmlns="http://camel.apache.org/schema/spring">
-->
	<camelContext id="camel" xmlns="http://camel.apache.org/schema/spring">

        <route id="amq-route">
            <description>Example Camel Route</description>
            <from uri="amq:example.A"/>
		<to uri="log:glib.app?showAll=true&amp;multiline=true" />
		<bean ref="amq-route-addSizeStatsBean" method="process" />
            <to uri="amq:example.B"/>
        </route>
	<route id="filetx" >
		<from uri="file:///home/danny/ActiveMQ/fromdir?readLock=rename&amp;delete=true" />
		<to uri="filetx-addSizeStatsBean" />
		<to uri="file:///home/danny/ActiveMQ/todir" />
	</route>
	<route id="remotefiletx" >

