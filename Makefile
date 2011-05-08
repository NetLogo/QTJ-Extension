ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

ifeq ($(origin SCALA_HOME), undefined)
  SCALA_HOME=../..
endif

SRCS=$(wildcard src/*.java)

# even if we add -nowarn it still prints:
#   Note: Some input files use or override a deprecated API.
#   Note: Recompile with -Xlint:deprecation for details.
# and http://bugs.sun.com/view_bug.do?bug_id=6460147 makes it impossible
# to make it completely go away.  We really don't want to see these lines
# every time we build extensions, especially since we can't see which
# extension is producing it (since they build in parallel), so we suppress
# them as follows.  Yeah, it's a kludge. - ST 5/5/10
JAVAC = $(JAVA_HOME)/bin/javac 2> /dev/null

QTJava.jar:
	echo You need to get QTJava.zip from your QuickTime installation,
	echo copy it here, and rename it QTJava.jar.
	exit 1

qtj.jar: $(SRCS) QTJava.jar manifest.txt
	mkdir -p classes
	$(JAVAC) -g -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogo.jar:$(SCALA_JAR):QTJava.jar -d classes $(SRCS)
	jar cmf manifest.txt qtj.jar -C classes .

qtj.zip: qtj.jar
	rm -rf qtj
	mkdir qtj
	cp -rp qtj.jar README.md Makefile src manifest.txt qtj
	zip -rv qtj.zip qtj
	rm -rf qtj
