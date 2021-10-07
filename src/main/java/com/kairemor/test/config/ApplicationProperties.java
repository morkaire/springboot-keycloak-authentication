package com.kairemor.test.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Accountservice.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String domain;
    private final Keycloak keycloak = new Keycloak();

    public static class Keycloak {
        private final Mobile mobile = new Mobile();

        public static class Mobile {
            private String resource;

            private final Credentials credentials = new Credentials();

            public String getResource() {
                return resource;
            }

            public void setResource(String resource) {
                this.resource = resource;
            }

            public static class Credentials {
                private String secret;

                public String getSecret() {
                    return secret;
                }

                public void setSecret(String secret) {
                    this.secret = secret;
                }
            }

            public Credentials getCredentials() {
                return credentials;
            }
        }

        public Mobile getMobile() {
            return mobile;
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Keycloak getKeycloak() {
        return keycloak;
    }
}
