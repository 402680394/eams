FROM openjdk:8

ADD https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.2.0/elastic-apm-agent-1.2.0.jar /apm/elastic-apm-agent-1.2.0.jar

WORKDIR /code

EXPOSE 8092

ENTRYPOINT [ "java", "-javaagent:/apm/elastic-apm-agent-1.2.0.jar", "-Delastic.apm.service_name=eams", "-Delastic.apm.application_packages=com.ztdx", "-Delastic.apm.server_urls=http://apm-server:8200", "-Xms512m", "-Xmx512m", "-jar", "/code/eams-1.0-SNAPSHOT-docker.jar" ]