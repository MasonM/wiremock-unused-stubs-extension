package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;
import static com.github.tomakehurst.wiremock.http.RequestMethod.DELETE;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.ok;
import static com.github.tomakehurst.wiremock.http.ResponseDefinition.okForJson;

public class UnusedStubsAdminExtension implements AdminApiExtension {
    @Override
    public String getName() {
        return getClass().getName();
    }

    /**
     * Adds the GET and DELETE route for /unused_mappings, using anonymous classes for the AdminTask
     *
     * @param router The Wiremock-provided router
     */
    @Override
    public void contributeAdminApiRoutes(Router router) {
        // Can't use "/mappings/unused" as the route because that'll match "/mappings/{id}"
        router.add(GET, "/unused_mappings",
            (Admin admin, Request request, PathParams pathParams) ->
                okForJson(getStubMappingsPartitionedByUsage(admin).get(false).toArray())
        );

        router.add(DELETE, "/unused_mappings",
            (Admin admin, Request request, PathParams pathParams) -> {
                deleteUnusedStubMappings(admin, request);
                return ok();
            }
        );
    }

    /**
     * Delete stub mappings that haven't matched any requests in the request journal
     * @param admin Wiremock-provided Admin object
     * @param request Request object
     */
    private void deleteUnusedStubMappings(Admin admin, Request request) {
        boolean removeFiles = request.queryParameter("remove_files").isPresent();
        final Map<Boolean, List<StubMapping>> mappings = getStubMappingsPartitionedByUsage(admin);
        mappings.get(false).forEach(admin::removeStubMapping);

        if (removeFiles) {
            final FileSource root = admin.getOptions().filesRoot().child(FILES_ROOT);
            final Set<String> bodyFilesForMatchedStubs = mappings
                .get(true)
                .stream()
                .map(stub -> stub.getResponse().getBodyFileName())
                .collect(Collectors.toSet());
            mappings
                .get(false)
                .stream()
                .map(stub -> stub.getResponse().getBodyFileName())
                // Since stub mappings can share body files, we have to check to
                // see if any of the remaining stubs are using it
                .filter(fileName -> fileName != null && !bodyFilesForMatchedStubs.contains(fileName))
                .forEach(root::deleteFile);
        }
    }

    /**
     * Return set of stub mapping UUIDs that have matched at least one request in the journal
     *
     * @param admin Wiremock-provided Admin object
     * @return Set of UUIDs
     */
    private Set<UUID> getServedStubIds(Admin admin) {
        return admin
            .getServeEvents()
            .getRequests()
            .stream()
            .filter(event -> event.getStubMapping() != null)
            .map(event -> event.getStubMapping().getId())
            .collect(Collectors.toSet());
    }

    /**
     * Finds all stub mappings that haven't matched any of the ServeEvents in the request journal.
     *
     * @param admin Wiremock-provided Admin object
     * @return Map of StubMappings partitioned by whether they match any events in the journal
     */
    private Map<Boolean, List<StubMapping>> getStubMappingsPartitionedByUsage(Admin admin) {
        Set<UUID> servedStubIds = getServedStubIds(admin);
        return admin
            .listAllStubMappings()
            .getMappings()
            .stream()
            .collect(Collectors.partitioningBy(stub -> servedStubIds.contains(stub.getId())));
    }
}
