package com.github.masonm.wiremock;

import com.github.tomakehurst.wiremock.admin.Router;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.AdminApiExtension;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                okForJson(getUnusedMappings(admin).toArray())
        );

        router.add(DELETE, "/unused_mappings",
            (Admin admin, Request request, PathParams pathParams) -> {
                getUnusedMappings(admin).forEach(admin::removeStubMapping);
                return ok();
            }
        );
    }

    /**
     * Finds all stub mappings that haven't matched any of the ServeEvents in the request journal.
     *
     * @param admin Wiremock-provided Admin object
     * @return Stream of unmatched StubMappings
     */
    private Stream<StubMapping> getUnusedMappings(Admin admin) {
        final Set<UUID> servedStubIds = admin
            .getServeEvents()
            .getRequests()
            .stream()
            .filter(event -> event.getStubMapping() != null)
            .map(event -> event.getStubMapping().getId())
            .collect(Collectors.toSet());

        return admin
            .listAllStubMappings()
            .getMappings()
            .stream()
            .filter(stub -> !servedStubIds.contains(stub.getId()));
    }
}
