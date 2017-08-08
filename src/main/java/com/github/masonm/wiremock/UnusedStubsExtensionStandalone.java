package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.standalone.WireMockServerRunner;
import org.apache.commons.lang3.ArrayUtils;

public final class UnusedStubsExtensionStandalone {
    private UnusedStubsExtensionStandalone() {}

    // When WireMock is run in standalone mode, WireMockServerRunner.run() is the entry point,
    // so we just delegate to that, passing along a CSV string with each extension class to load
    public static void main(String... args) {
        args = ArrayUtils.add(args,
            "--extensions=" + UnusedStubsAdminExtension.class.getName()
        );
        new WireMockServerRunner().run(args);
    }
}
