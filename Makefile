ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

ifeq ($(origin SCALA_JAR), undefined)
  SCALA_JAR=$(NETLOGO)/lib/scala-library.jar
endif

SRCS=$(wildcard src/*.java)

# why redirect like this? see readme
JAVAC = $(JAVA_HOME)/bin/javac 2> /dev/null

qtj.jar: $(SRCS) QTJava.jar manifest.txt
	mkdir -p classes
	$(JAVAC) -g -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogo.jar:$(SCALA_JAR):QTJava.jar -d classes $(SRCS)
	jar cmf manifest.txt qtj.jar -C classes .

QTJava.jar:
	echo QTJava.jar missing -- see readme
	exit 1

qtj.zip: qtj.jar
	rm -rf qtj
	mkdir qtj
	cp -rp qtj.jar README.md Makefile src manifest.txt qtj
	zip -rv qtj.zip qtj
	rm -rf qtj
