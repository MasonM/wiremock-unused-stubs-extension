# DEPRECATED

A modified version of this extension was [integrated into WireMock v3.13.0](https://github.com/wiremock/wiremock/releases/tag/3.13.0).

# Overview

[![Build Status](https://travis-ci.org/MasonM/wiremock-unused-stubs-extension.svg?branch=master)](https://travis-ci.org/MasonM/wiremock-unused-stubs-extension)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-unused-stubs-extension/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.masonm/wiremock-unused-stubs-extension)

wiremock-unused-stubs-extension is an admin extension for [WireMock](http://wiremock.org) that adds a new endpoint, `/__admin/unused_mappings`, for finding and removing stub mappings that have not matched any requests in the journal. This is useful in conjunction with [Record and Playback](http://wiremock.org/docs/record-playback-legacy/) for pruning generated stub mappings.

# Building

Run `gradle jar` to build the JAR without dependencies or `gradle fatJar` to build a standalone JAR.
These will be placed in `build/libs/`.

# Running

Standalone server:
```sh
java -jar build/libs/wiremock-unused-stubs-extension-0.3-standalone.jar
```

With WireMock standalone JAR:
```sh
wget -nc http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.14.0/wiremock-standalone-2.14.0.jar
java \
        -cp wiremock-standalone-2.14.0.jar:build/libs/wiremock-unused-stubs-extension-0.3.jar \
        com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
        --extensions="com.github.masonm.wiremock.UnusedStubsAdminExtension"
```

Programmatically in Java:
```java
new WireMockServer(wireMockConfig()
    .extensions("com.github.masonm.wiremock.UnusedStubsAdminExtension"))
```

# Usage

* Call `GET /__admin/unused_mappings` to retrieve an array of stub mappings that have not matched any requests in the request journal.
* Call `DELETE /__admin/unused_mappings` to remove all such stub mappings. By default, any body files used by the stub mappings (typically stored in the "__files" directory) will preserved. To remove those too, pass "remove_files" in the query, i.e. `DELETE /__admin/unused_mappings?remove_files`


