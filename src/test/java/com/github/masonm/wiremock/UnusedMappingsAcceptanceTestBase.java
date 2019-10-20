package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.AcceptanceTestBase;
import org.junit.BeforeClass;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class UnusedMappingsAcceptanceTestBase extends AcceptanceTestBase {
    protected static final String ADMIN_URL = "/__admin/unused_mappings";
    protected static final String ADMIN_URL_REMOVE_FILES = ADMIN_URL + "?remove_files";

    @BeforeClass
    public static void setupServer() {
        setupServer(wireMockConfig()
            .dynamicPort()
            .extensions(UnusedStubsAdminExtension.class)
            .withRootDirectory(setupTempFileRoot().getAbsolutePath())
        );
    }
}
