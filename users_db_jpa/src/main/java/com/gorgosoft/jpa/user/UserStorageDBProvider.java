package com.gorgosoft.jpa.user;

import lombok.extern.slf4j.Slf4j;
import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Stateless
@Local(UserStorageDBProvider.class)
@Slf4j
public class UserStorageDBProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider, CredentialInputValidator {

    @PersistenceContext
    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;
    public static final String PASSWORD_CACHE_KEY = UserAdapter.class.getName() + ".password";

    /*public UserStorageDBProvider(KeycloakSession session, ComponentModel model) {
        this.model = model;
        this.session = session;
        log.infof("getProvider: %s", session.getProvider(JpaConnectionProvider.class, "user-store"));
        log.infof("getProvider: %s", session.getProvider(JpaConnectionProvider.class));
        var providers = session.getAllProviders(JpaConnectionProvider.class);
        providers.stream().forEach(item -> log.infof("Provider %s: ", item.toString()));
        //em = session.getProvider(JpaConnectionProvider.class, "user-store").getEntityManager();
        em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }*/

    public void setModel(ComponentModel model) {
        this.model = model;
    }

    public void setSession(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType) && getPassword(user) != null;
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel user, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType()) || !(credentialInput instanceof UserCredentialModel))
            return false;
        UserCredentialModel cred = (UserCredentialModel) credentialInput;
        String password = getPassword(user);
        return password != null && password.equals(cred.getValue());
    }

    @Remove
    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realmModel, String id) {
        String persistenceId = StorageId.externalId(id);
        UserEntity entity = em.find(UserEntity.class, persistenceId);
        if (entity == null) {
            log.info("could not find user by id: " + id);
            return null;
        }
        return new UserAdapter(session, realmModel, model, entity);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByUsername", UserEntity.class);
        query.setParameter("username", username);
        List<UserEntity> result = query.getResultList();
        if (result.isEmpty()) {
            log.info("could not find username: " + username);
            return null;
        }

        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();
        if (result.isEmpty()) return null;
        return new UserAdapter(session, realm, model, result.get(0));
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, String search, Integer firstResult, Integer maxResults) {

        log.info("Calling searchForUserStream");
        log.info("Realm: {}", realmModel.getName());
        log.info("Search: {}", search);
        log.info("FirstResult: {}", firstResult);
        log.info("MaxResults: {}", maxResults);
        TypedQuery<UserEntity> query = em.createNamedQuery("searchForUser", UserEntity.class);
        query.setParameter("search", "%" + search.toLowerCase() + "%");
        if (firstResult != -1) {
            query.setFirstResult(firstResult);
        }
        if (maxResults != -1) {
            query.setMaxResults(maxResults);
        }
        List<UserEntity> results = query.getResultList();
        List<UserModel> users = new LinkedList<>();
        for (UserEntity entity : results) users.add(new UserAdapter(session, realmModel, model, entity));
        return users.stream();
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
        return Stream.empty();
    }

    public String getPassword(UserModel user) {
        String password = null;
        if (user instanceof CachedUserModel) { //TODO: To be removed
            password = (String) ((CachedUserModel) user).getCachedWith().get(PASSWORD_CACHE_KEY);
        } else if (user instanceof UserAdapter) {
            password = ((UserAdapter) user).getPassword();
        }
        return password;
    }

}
