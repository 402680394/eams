FROM openjdk:8

ADD https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.2.0/elastic-apm-agent-1.2.0.jar /apm/elastic-apm-agent-1.2.0.jar

RUN cd /tmp && \
curl -LO https://ayera.dl.sourceforge.net/project/openofficeorg.mirror/4.1.6/binaries/zh-CN/Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
tar -xzf Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
rm -f Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
dpkg -i zh-CN/DEBS/*.deb && \
rm -Rf zh-US

WORKDIR /code

EXPOSE 8092

CMD ["/opt/openoffice4/program/soffice.bin", "-headless" "-accept=socket,host=127.0.0.1,port=8100;urp;" "-nofirststartwizard"]

ENTRYPOINT [ "java", "-javaagent:/apm/elastic-apm-agent-1.2.0.jar", "-Delastic.apm.service_name=eams", "-Delastic.apm.application_packages=com.ztdx", "-Delastic.apm.server_urls=http://apm-server:8200", "-Xms512m", "-Xmx512m", "-jar", "/code/eams-1.0-SNAPSHOT-docker.jar" ]