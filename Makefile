MAVEN=apache-maven-3.5.0-bin.tar.gz
MAVEN_URL=http://mirrors.ae-online.de/apache/maven/maven-3/3.5.0/binaries/
DEPS=.deps
MVN=$(DEPS)/apache-maven-3.5.0/bin/mvn
JAR=target/item-item-factorization.jar
JAVA=java -server -Xmx$(Xmx) -Xms$(Xms) -cp $(JAR)
Xmx=40g
Xms=2g
OPTIONS?=options.txt

all: build

.PHONY: build
build: $(JAR)

$(JAR): $(MVN) FORCE
	$(MVN) package

FORCE:

$(MVN): $(DEPS)/$(MAVEN)
	tar -C $(DEPS) -xzf $<
	touch $@

$(DEPS)/$(MAVEN): $(DEPS)
	curl -o $@ $(MAVEN_URL)/$(MAVEN)

$(DEPS):
	mkdir -p $(DEPS)

.PHONY: test
test: $(DEPS)/$(MAVEN)
	$(MVN) test

.PHONY: preprocess
preprocess: $(JAR)
	$(JAVA) com.demshape.factorization.application.Preprocess @$(OPTIONS)

.PHONY: train
train: $(JAR)
	$(JAVA) com.demshape.factorization.application.Train @$(OPTIONS)

.PHONY: predict
predict: $(JAR)
	$(JAVA) com.demshape.factorization.application.Predict @$(OPTIONS)

.PHONY: clean
clean: $(MVN)
	$(MVN) clean
	rm -rf $(DEPS)
