FROM openjdk:8

ADD https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.2.0/elastic-apm-agent-1.2.0.jar /apm/elastic-apm-agent-1.2.0.jar

RUN cd /tmp && \
curl -LO https://ayera.dl.sourceforge.net/project/openofficeorg.mirror/4.1.6/binaries/zh-CN/Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
tar -xzf Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
rm -f Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
dpkg -i zh-CN/DEBS/*.deb && \
rm -Rf zh-US

RUN mkdir -p /usr/share/fonts/fallback

COPY ./font/* /usr/share/fonts/fallback/

RUN fc-cache

ADD ./entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

WORKDIR /code

EXPOSE 8092

ENTRYPOINT [ "/entrypoint.sh" ]