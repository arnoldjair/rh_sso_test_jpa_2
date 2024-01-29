package com.gorgosoft.jpa.user;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import java.util.Objects;

@AutoService(UserStorageProviderFactory.class)
@Slf4j
public class UserStorageDBProviderFactory implements UserStorageProviderFactory<UserStorageDBProvider> {

    private static final Logger logger = Logger.getLogger(UserStorageDBProviderFactory.class);
    public static final String PROVIDER_ID = " users_db_jpa_2";

    @Override
    public UserStorageDBProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        log.info("keycloakSession, is null?: {}", Objects.isNull(keycloakSession));
        log.info("componentModel, is null?=: {}", Objects.isNull(componentModel));
        try {
            InitialContext ctx = new InitialContext();
            NamingEnumeration<NameClassPair> list = ctx.list("");
            while (list.hasMore()) {
                log.info(list.next().getName());
            }
            UserStorageDBProvider provider = (UserStorageDBProvider)ctx.lookup("java:global/users_db_jpa_2/" + UserStorageDBProvider.class.getSimpleName());
            //java:global/users_db_jpa_2/UserStorageDBProvider
            log.info("Getting the provider, is null? {}", Objects.isNull(provider));
            provider.setModel(componentModel);
            provider.setSession(keycloakSession);
            return provider;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
