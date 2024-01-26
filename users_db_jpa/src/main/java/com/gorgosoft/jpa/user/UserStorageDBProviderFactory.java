package com.gorgosoft.jpa.user;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import java.util.Objects;

@AutoService(UserStorageProviderFactory.class)
@Slf4j
public class UserStorageDBProviderFactory implements UserStorageProviderFactory<UserStorageDBProvider> {

    private static final Logger logger = Logger.getLogger(UserStorageDBProviderFactory.class);
    public static final String PROVIDER_ID = "user-storage-db-2";

    @Override
    public UserStorageDBProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        log.info("keycloakSession null: {}", Objects.isNull(keycloakSession));
        log.info("componentModel null: {}", Objects.isNull(componentModel));
        return new UserStorageDBProvider(keycloakSession, componentModel);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getHelpText() {
        return "JPA Example User Storage Provider";
    }

    @Override
    public void close() {
        logger.info("<<<<<< Closing factory");
    }
}
