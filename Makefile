JFLAGS=-g:none
JC=javac

CP=lib/json-simple-1.1.1.jar:lib/mysql-connector-java-5.1.26-bin.jar

SRC_DIR=./src/
BIN_DIR=./bin/

JAVA_FILES := $(shell find $(SRC_DIR) -name '*.java')

all: bindir
	$(JC) $(JFLAGS) -d $(BIN_DIR) -cp $(CP) $(JAVA_FILES)
	chmod a+x JAgoraComputation

bindir:
	mkdir -p $(BIN_DIR)

.PHONY: clean

clean:
	rm -rf $(BIN_DIR)
