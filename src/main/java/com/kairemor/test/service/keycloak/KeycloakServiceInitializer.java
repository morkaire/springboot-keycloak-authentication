package com.kairemor.test.service.keycloak;

import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.springframework.util.MultiValueMap;

public interface KeycloakServiceInitializer {
    UsersResource getUsersResource();

    AuthzClient getAuthzClient();

    AuthzClient getAuthzMobileClient();

    RealmResource getRealmResource();

    MultiValueMap<String, String> getRefreshTokenConfig();

    MultiValueMap<String, String> getMobileRefreshTokenConfig();
}

