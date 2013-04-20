activemq-stats-web
==================

An interactive web site for embedding into an ActiveMQ and / or Camel server

Fork the project and build it using maven

Copy the amqstats.war file to the ActiveMq webapps directory and add the following handler to the "handler" property in the jetty.xml file in the ActiveMq conf directory:

                        <bean class="org.eclipse.jetty.webapp.WebAppContext">
                            <property name="contextPath" value="/amqstats" />
                            <property name="war" value="${activemq.home}/webapps/amqstats.war" />
                            <property name="logUrlOnStart" value="true" />
                        </bean>

Restart ActiveMQ and go to the ActiveMQ web url, with a context of "amqstats", e.g.:

	http://localhost:8161/amqstats/

