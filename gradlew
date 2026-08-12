#!/usr/bin/env sh
# -----------------------------------------------------------------------------
# Gradle start up script for UN*X
# -----------------------------------------------------------------------------
# Copyright 2013 the original author or authors.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# -----------------------------------------------------------------------------

##############################################################################
# Environment variables
##############################################################################

# Allow this script to be symlinked without breaking functionality
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' >/dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/""$link""
  fi
done

PRGDIR=`dirname "$PRG"`

# Only set GRADLE_HOME if not already set
if [ -z "$GRADLE_HOME" ]; then
  GRADLE_HOME="$PRGDIR/gradle/wrapper"
fi

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

CLASSPATH="$GRADLE_HOME/gradle-wrapper.jar"
APPEND=""

# Determine Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
  _RUNJAVA="$JAVA_HOME/bin/java"
else
  _RUNJAVA="java"
fi

exec "$_RUNJAVA" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
