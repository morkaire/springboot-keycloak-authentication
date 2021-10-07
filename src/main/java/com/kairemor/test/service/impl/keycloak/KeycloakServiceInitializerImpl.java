package com.kairemor.test.service.impl.keycloak;

import com.kairemor.test.config.ApplicationProperties;
import com.kairemor.test.properties.AccountServiceProperties;
import com.kairemor.test.service.keycloak.KeycloakServiceInitializer;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.AuthzClient;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;

@Service
public class KeycloakServiceInitializerImpl implements KeycloakServiceInitializer {
    private final AccountServiceProperties accountServiceProperties;
    private final ApplicationProperties applicationProperties;

    public KeycloakServiceInitializerImpl(AccountServiceProperties accountServiceProperties, ApplicationProperties applicationProperties) {
        this.accountServiceProperties = accountServiceProperties;
        this.applicationProperties = applicationProperties;
    }

    public UsersResource getUsersResource() {
        return org.keycloak.admin.client.KeycloakBuilder.builder()
            .serverUrl(accountServiceProperties.getIssuerUrl())
            .realm(accountServiceProperties.getRealm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(accountServiceProperties.getClientId())
            .clientSecret(accountServiceProperties.getClientSecret())
            .build()
            .realm(accountServiceProperties.getRealm())
            .users();
    }

    public AuthzClient getAuthzClient(){
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", accountServiceProperties.getClientSecret());
        clientCredentials.put("grant_type", "password");
        return AuthzClient.create(new org.keycloak.authorization.client.Configuration(accountServiceProperties.getIssuerUrl(), accountServiceProperties.getRealm(), accountServiceProperties.getClientId(), clientCredentials, null));
    }

    @Override
    public AuthzClient getAuthzMobileClient() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", applicationProperties.getKeycloak().getMobile().getCredentials().getSecret());
        clientCredentials.put("grant_type", "password");
        return AuthzClient.create(new org.keycloak.authorization.client.Configuration(accountServiceProperties.getIssuerUrl(), accountServiceProperties.getRealm(), applicationProperties.getKeycloak().getMobile().getResource(), clientCredentials, null));
    }

    public RealmResource getRealmResource(){
        return org.keycloak.admin.client.KeycloakBuilder.builder()
            .serverUrl(accountServiceProperties.getIssuerUrl())
            .realm(accountServiceProperties.getRealm())
            .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
            .clientId(accountServiceProperties.getClientId())
            .clientSecret(accountServiceProperties.getClientSecret())
            .build()
            .realm(accountServiceProperties.getRealm());
    }

    public MultiValueMap<String, String> getRefreshTokenConfig() {
        String url = accountServiceProperties.getIssuerUrl() + "/realms/" + accountServiceProperties.getRealm() + "/protocol/openid-connect/token";
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("url",url);
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("client_id", accountServiceProperties.getClientId());
        requestBody.add("client_secret", accountServiceProperties.getClientSecret());
        return requestBody;
    }

    @Override
    public MultiValueMap<String, String> getMobileRefreshTokenConfig() {
        String url = accountServiceProperties.getIssuerUrl() + "/realms/" + accountServiceProperties.getRealm() + "/protocol/openid-connect/token";
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<String, String>();
        requestBody.add("url",url);
        requestBody.add("grant_type", "refresh_token");
        requestBody.add("client_id", applicationProperties.getKeycloak().getMobile().getResource());
        requestBody.add("client_secret", applicationProperties.getKeycloak().getMobile().getCredentials().getSecret());
        return requestBody;
    }

}
