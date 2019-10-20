package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.*;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RemoveUnusedMappingsAcceptanceTest extends UnusedMappingsAcceptanceTestBase {
    @Test
    public void withNoRequests() {
        assertThat(testClient.delete(ADMIN_URL).statusCode(), is(HTTP_OK));
    }

    @Test
    public void withOneUnusedMapping() {
        stubFor(get(urlEqualTo("/stub-1")));
        assertThat(listAllStubMappings().getMappings().size(), is(1));

        assertThat(testClient.delete(ADMIN_URL).statusCode(), is(HTTP_OK));
        assertThat(listAllStubMappings().getMappings().size(), is(0));
    }

    @Test
    public void withOneUsedMapping() {
        stubFor(get(urlEqualTo("/stub-1")));
        assertThat(testClient.get("/stub-1").statusCode(), is(HTTP_OK));

        assertThat(testClient.delete(ADMIN_URL).statusCode(), is(HTTP_OK));
        assertThat(listAllStubMappings().getMappings().size(), is(1));
    }

    @Test
    public void withOneUnusedMappingAndRemoveFiles() {
        stubFor(get(urlEqualTo("/stub-1")));

        assertThat(testClient.delete(ADMIN_URL_REMOVE_FILES).statusCode(), is(HTTP_OK));
        assertThat(listAllStubMappings().getMappings().size(), is(0));
    }

    @Test
    public void withMultipleMappings() {
        UUID id1 = UUID.randomUUID();
        StubMapping stub1 = stubFor(get(urlEqualTo("/stub-1")).withId(id1));
        stubFor(get(urlEqualTo("/stub-2")).withId(UUID.randomUUID()));

        assertThat(testClient.get("/stub-1").statusCode(), is(HTTP_OK));

        assertThat(testClient.delete(ADMIN_URL).statusCode(), is(HTTP_OK));
        assertThat(getSingleStubMapping(id1), is(stub1));
        assertThat(listAllStubMappings().getMappings().size(), is(1));
    }

    @Test
    public void withMultipleMappingsWithSeparateFilesAndRemoveFiles() {
        FileSource fileSource = wireMockServer.getOptions().filesRoot().child(FILES_ROOT);
        fileSource.createIfNecessary();
        fileSource.writeTextFile("mapping-one", "should be removed");
        fileSource.writeTextFile("mapping-two", "should be removed");

        stubFor(get(urlEqualTo("/stub-1")).willReturn(aResponse().withBodyFile("mapping-one")));
        stubFor(get(urlEqualTo("/stub-2")).willReturn(aResponse().withBodyFile("mapping-two")));

        assertThat(testClient.delete(ADMIN_URL_REMOVE_FILES).statusCode(), is(HTTP_OK));

        assertThat(listAllStubMappings().getMappings().size(), is(0));
        assertThat(fileSource.listFilesRecursively(), hasExactly());
    }

    @Test
    public void withMultipleMappingsWithSharedFilesAndRemoveFiles() {
        FileSource fileSource = wireMockServer.getOptions().filesRoot().child(FILES_ROOT);
        fileSource.createIfNecessary();
        fileSource.writeTextFile("shared-mapping", "should be preserved");

        UUID id1 = UUID.randomUUID();
        StubMapping stub1 = stubFor(
            get(urlEqualTo("/stub-1"))
                .withId(id1)
                .willReturn(aResponse().withBodyFile("shared-mapping"))
        );
        assertThat(testClient.get("/stub-1").statusCode(), is(HTTP_OK));

        stubFor(
            get(urlEqualTo("/stub-2"))
                .withId(UUID.randomUUID())
                .willReturn(aResponse().withBodyFile("shared-mapping"))
        );

        assertThat(testClient.delete(ADMIN_URL_REMOVE_FILES).statusCode(), is(HTTP_OK));

        assertThat(getSingleStubMapping(id1), is(stub1));
        assertThat(listAllStubMappings().getMappings().size(), is(1));
        // Should not have remove file since it's shared with an existing mapping
        assertThat(fileSource.listFilesRecursively(), hasExactly(
            fileNamed(stub1.getResponse().getBodyFileName())
        ));
    }
}
