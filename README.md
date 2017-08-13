# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-unused-stubs-extension.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-unused-stubs-extension)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-unused-stubs-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-unused-stubs-extension)

wiremock-unused-stubs-extension is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/__admin/unused_mapping`, for finding and removing stub mappings that have not matched any requests in the journal. This is useful in conjunction with [Record and Playback](http://wiremock.org/docs/record-playback-legacy/) for pruning generated stub mappings.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```sh
java -jar build/libs/wiremock-unused-stubs-extension-0.1a.jar
```

With WireMock standalone JAR:
```sh
java \
        -cp wiremock-standalone.jar:build/libs/wiremock-unused-stubs-extension-0.1a.jar \
        com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
        --extensions="com.github.masonm.wiremock.UnusedStubsAdminExtension"
```

Programmatically in Java:
```java
new WireMockServer(wireMockConfig()
    .extensions("com.github.masonm.wiremock.UnusedStubsAdminExtension"))
```

# Usage

Call `GET /__admin/unused_mappings` to retrieve an array of stub mappings that have not matched any requests in the request journal. Call `DELETE /__admin/mappings` to remove all such stub mappings.


