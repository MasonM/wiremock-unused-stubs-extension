package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalToJson;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GetUnusedMappingsAcceptanceTest extends UnusedMappingsAcceptanceTestBase{
    @Test
    public void withNoRequests() {
        assertThat(testClient.get(ADMIN_URL).content(), equalToJson("[]"));
    }

    @Test
    public void withOneMapping() {
        StubMapping stub1 = stubFor(get(urlEqualTo("/stub-1")));
        assertThat(testClient.get(ADMIN_URL).content(), equalToJson("[" + stub1.toString() + "]"));

        assertThat(testClient.get("/stub-1").statusCode(), is(HTTP_OK));
        // no longer unused
        assertThat(testClient.get(ADMIN_URL).content(), equalToJson("[]"));
    }

    @Test
    public void withMultipleMappings() {
        StubMapping stub1 = stubFor(get(urlEqualTo("/stub-1")));
        StubMapping stub2 = stubFor(get(urlEqualTo("/stub-2")));

        assertThat(testClient.get("/stub-1").statusCode(), is(HTTP_OK));
        assertThat(testClient.get(ADMIN_URL).content(), equalToJson("[" + stub2.toString() + "]"));

        assertThat(testClient.get("/stub-2").statusCode(), is(HTTP_OK));
        assertThat(testClient.get(ADMIN_URL).content(), equalToJson("[]"));
    }
}
