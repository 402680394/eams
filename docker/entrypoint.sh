#!/usr/bin/env bash

set -e

nohup /opt/openoffice4/program/soffice.bin -headless -accept='socket,host=127.0.0.1,port=8100;urp;' -nofirststartwizard >/dev/null 2>&1 &

java -javaagent:/apm/elastic-apm-agent-1.2.0.jar \
    -Delastic.apm.service_name=eams \
    -Delastic.apm.application_packages=com.ztdx \
    -Delastic.apm.server_urls=http://apm-server:8200 \
    -Xms512m -Xmx512m \
    -jar /code/eams-1.0-SNAPSHOT.jar