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

CLASSPATH="$APP_HOME/conf"
for i in "$APP_HOME"/lib/*.jar; do
   CLASSPATH="$CLASSPATH":"$i"
done

if [[ -d "${JAVA_HOME}" ]]; then
  JAVA="${JAVA_HOME}/bin/java"
else
  JAVA="java"
fi

$JAVA -DAPP_HOME="$APP_HOME" $JVM_OPTS -classpath "$CLASSPATH" $MAIN_CLASS "$APP_HOME" "$@"