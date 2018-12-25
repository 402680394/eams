FROM openjdk:8

# apm插件，可以在下面地址下载
# ADD https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/1.2.0/elastic-apm-agent-1.2.0.jar /apm/elastic-apm-agent-1.2.0.jar
COPY ./elastic-apm-agent-1.2.0.jar /apm/elastic-apm-agent-1.2.0.jar

COPY ./font/* /usr/share/fonts/fallback/

COPY ./entrypoint.sh /entrypoint.sh

# openoffice插件，可以在下面地址下载
# curl -LO https://ayera.dl.sourceforge.net/project/openofficeorg.mirror/4.1.6/binaries/zh-CN/Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
COPY ./Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz /tmp

RUN cd /tmp && \
tar -xzf Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
rm -f Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz && \
dpkg -i zh-CN/DEBS/*.deb && \
rm -Rf zh-US && \
chmod +x /entrypoint.sh && \
fc-cache

WORKDIR /code

EXPOSE 8092

ENTRYPOINT [ "/entrypoint.sh" ]