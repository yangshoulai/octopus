#!/usr/bin/env bash

# set -x
set -e

MAIN_CLASS="com.octopus.executor.App"

JVM_OPTS="-Xms1024m -Xmx1024m"

APP_HOME="$(
  cd "$(
    cd "$(dirname "$0")" || exit
    pwd
  )/.." || exit
  pwd
)"

if [[ $# -le 0 || $# -gt 1 ]]; then
  echo "Usage: $0 site.yaml"
  exit 1
fi

site=$(basename "$1" | cut -d. -f1)

if [[ -e "${APP_HOME}/logs/$site.pid" ]]; then
  pid=$(cat "${APP_HOME}/logs/$site.pid")
  if [[ -e "/proc/$pid" ]]; then
    echo "Site $site is running, pid=$pid"
    exit 1
  fi
fi

CLASSPATH="$APP_HOME/conf"
for i in "$APP_HOME"/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

if [[ -d "${JAVA_HOME}" ]]; then
  JAVA="${JAVA_HOME}/bin/java"
else
  JAVA="java"
fi

nohup $JAVA -DAPP_HOME="$APP_HOME" $JVM_OPTS -classpath "$CLASSPATH" $MAIN_CLASS "$APP_HOME" "$1" > $APP_HOME/logs/$site.log 2>&1 &

pid=$!

echo $pid > $APP_HOME/logs/$site.pid