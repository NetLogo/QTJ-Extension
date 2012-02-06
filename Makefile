ifeq ($(origin JAVA_HOME), undefined)
  JAVA_HOME=/usr
endif

ifeq ($(origin NETLOGO), undefined)
  NETLOGO=../..
endif

SRCS=$(wildcard src/*.java)

# why redirect like this? see readme
JAVAC = $(JAVA_HOME)/bin/javac 2> /dev/null

qtj.jar: $(SRCS) QTJava.jar manifest.txt
	mkdir -p classes
	$(JAVAC) -g -encoding us-ascii -source 1.5 -target 1.5 -classpath $(NETLOGO)/NetLogoLite.jar:QTJava.jar -d classes $(SRCS)
	jar cmf manifest.txt qtj.jar -C classes .

QTJava.jar:
	@if [ -f /System/Library/Java/Extensions/QTJava.zip ]; then \
	echo "copying QTJava.jar from /System/Library/Java/Extensions"; \
	cp /System/Library/Java/Extensions/QTJava.zip QTJava.jar; \
	elif [ -f ~/QTJava.jar ]; then \
	echo "copying QTJava.jar from home directory"; \
	cp ~/QTJava.jar QTJava.jar; \
	else \
	echo "QTJava.jar not found. Cannot build qtj extension."; \
	echo "Apple's license doesn't permit us to distribute this file."; \
	echo "You'll need to find QTJava.zip on a Mac or Windows system with QuickTime installed"; \
	echo "and copy it to" `pwd`"/QTJava.jar."; \
	echo "Note the change in the name from .zip to .jar."; \
	exit 1; \
	fi

qtj.zip: qtj.jar
	rm -rf qtj
	mkdir qtj
	cp -rp qtj.jar README.md Makefile src manifest.txt qtj
	zip -rv qtj.zip qtj
	rm -rf qtj
