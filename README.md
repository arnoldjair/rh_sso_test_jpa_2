# rh_sso_test_jpa_2

## Launch a posgresql docker instance

To create a postgres instance with docker, follow the instructions available at [postgresql/guide.docx](postgresql/guide.docx).

## Configure a datasource using jboss-cli

To set up the datasource using jboss-cli, locate the folder in which the rh-sso folder resides (for example C:\rh-sso-7.6.0-server-dist\rh-sso-7.6.0) and move to the bin folder. In that folder there should be an executable file named jboss-cli.bat (windows). In a terminal located in the bin folder run the following:

```bash
.\jboss-cli.bat --connect --file=<<Full path to this repo>>\extensions\sso-extensions_users.cli
```

sso-extensions_users.cli points to a file called postgresql-42.6.0.jar in the tmp folder, this file must be downloaded from [postgresql driver](https://jdbc.postgresql.org/download/)

The above command modifies the standalone.xml file and adds the datasource entry. It also creates the module with the driver.

```xml
<datasource jndi-name="java:jboss/datasources/PasswordDB" pool-name="PasswordDB" enabled="true" use-java-context="true">
    <connection-url>jdbc:postgresql://localhost:5433/users?ssl=true;sslfactory=org.postgresql.ssl.NonValidatingFactory</connection-url>
    <driver>postgres</driver>
    <security>
        <user-name>user</user-name>
        <password>password</password>
    </security>
</datasource>
```

## Configure persistence unit
Create a file named persistence.xml and add the following content:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence version="2.0"
             xmlns="http://java.sun.com/xml/ns/persistence" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="
        http://java.sun.com/xml/ns/persistence
        http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd">
    <persistence-unit name="user-store"> <!-- The persistence unit name is defined here -->
        <jta-data-source>java:jboss/datasources/PasswordDB</jta-data-source> <!-- Here goes the jndi-name of the resource, you can find it in the datasource definitions in standalone.xml -->
        <class>com.gorgosoft.jpa.user.UserEntity</class> <!-- Here goes the list of classes we are working with (package and class name) -->
        <properties> <!--Hibernate properties-->
            <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
            <property name="hibernate.hbm2ddl.auto" value="none"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.temp.use_jdbc_metadata_defaults" value="false"/>
        </properties>
    </persistence-unit>
</persistence>
```

In each line of interest there is a commentary explaining its utility.
This file must be located in src/main/resources/META-INF

## Configure the classes to be mapped in the database

Each class to be mapped in the database must be decorated with the following annotations

- @Entity:
  - JPA (Java Persistence API) annotation used to mark a Java class as an entity, meaning it will be mapped to a database table.
  - Entities are typically used to represent tables in a relational database.
  - When an entity class is annotated with @Entity, it signifies that instances of this class can be managed by the JPA EntityManager and can be persisted to the database.
- @Table
  - JPA annotation used to provide metadata about the mapping of a class to a database table.
  - It allows you to specify details such as the name of the table to which the entity is mapped, the schema, indexes, etc.
- @NameQuery 
  - JPA annotation used to define a named query.
  - Named queries allow you to define a query with a name within an entity class or at the entity manager level, which can then be referenced in your code using the specified name.
  - This annotation typically takes two parameters: name and query, where name is the name by which the query will be referred to in your code, and query is the JPQL (Java Persistence Query Language) query string.
  - Named queries are useful for defining commonly used queries in a centralized location and promoting code reuse. They can also improve performance by allowing the JPA provider to optimize and cache queries.

```java
package com.gorgosoft.jpa.user;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@NamedQueries({
        @NamedQuery(name = "getUserByUsername", query = "select u from UserEntity u where u.username = :username"),
        @NamedQuery(name = "getUserByEmail", query = "select u from UserEntity u where u.email = :email"),
        @NamedQuery(name = "getUserCount", query = "select count(u) from UserEntity u"),
        @NamedQuery(name = "getAllUsers", query = "select u from UserEntity u"),
        @NamedQuery(name = "searchForUser", query = "select u from UserEntity u where " +
                "( lower(u.username) like :search or u.email like :search ) order by u.username"),
})
@Entity(name = "UserEntity")
@Table(name = "db_user")
@Data
public class UserEntity {

    @Id
    private String id;


    private String username;
    private String email;
    private String password;
    private String name;

}
```

## Configure the data access

In this project the data access is configured in the UserStorageDBProvider class that implements UserStorageProvider, UserLookupProvider, UserQueryProvider and CredentialInputValidator. **It is advisable to decouple the SPI from specific implementations, since it is possible that at some point you may need to change databases or invoke third-party services**.

This class has the next annotations:

- @Stateless:
  - EJB (Enterprise JavaBeans) annotation used to declare a session bean as stateless.
  - Statelessness implies that the bean instances do not maintain any conversational state with the client across method invocations.
  - Each method call on a stateless session bean is independent of any previous calls, and instances can be pooled and reused by the EJB container to serve multiple clients concurrently.
  - Statelessness is advantageous for scalability, as it allows the container to manage a pool of bean instances efficiently without the overhead of maintaining conversational state.  
- @Local(UserStorageDBProvider.class):
  - EJB annotation used to define the local business interface of an EJB.
  - Local interfaces are used for local EJB invocations within the same JVM (Java Virtual Machine). They provide direct Java method calls without the overhead of remote communication, making them more efficient for intra-application communication.
  - Using @Local with a specific class name allows you to specify the exact type of the local business interface provided by the EJB.

The next 3 object are needed in the storage provider:

- EntityManager em:
  - Annotated with @PersistenceContext:
        - Is a JPA (Java Persistence API) annotation used to inject an EntityManager into a Java EE managed component, such as an EJB, servlet, or CDI bean.
        - The EntityManager interface is the primary interface used by applications to interact with the persistence context, which manages a set of entity instances.
        - When an EntityManager is injected using @PersistenceContext, it allows the component to perform CRUD (Create, Read, Update, Delete) operations on entities, as well as execute JPQL (Java Persistence Query Language) queries and manage transactions.
        - The EntityManager obtained through @PersistenceContext is typically associated with a specific persistence unit defined in the persistence.xml configuration file.
    - EntityManager is the primary interface used by JPA applications to interact with the persistence context.
    - With this EntityManager instance, the component can perform various database operations such as persisting, merging, removing, and querying entities.
- ComponentModel model:
  - Instances of ComponentModel represent the configuration and metadata associated with a component within the Keycloak system.
  - Components in Keycloak can include various elements such as realms, clients, users, roles, etc.
- KeycloakSession session:
  - KeycloakSession typically represents a session or context within which authentication and authorization operations are performed in a Keycloak-enabled application.
  - Provides methods and utilities for managing users, roles, permissions, authentication flows, and other security-related functionalities within the Keycloak realm.

```Java
package com.gorgosoft.jpa.user;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ejb.Local;
import javax.ejb.Remove;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.keycloak.component.ComponentModel;
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

import lombok.extern.slf4j.Slf4j;

@Stateless
@Local(UserStorageDBProvider.class)
@Slf4j
public class UserStorageDBProvider implements UserStorageProvider, UserLookupProvider, UserQueryProvider, CredentialInputValidator {

    @PersistenceContext
    protected EntityManager em;
    protected ComponentModel model;
    protected KeycloakSession session;
```

## Making database queries using namedQueries

The next code snippet shows how to make database queries using namedQueries (defined in entity classes)

```java
@Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        TypedQuery<UserEntity> query = em.createNamedQuery("getUserByEmail", UserEntity.class);
        query.setParameter("email", email);
        List<UserEntity> result = query.getResultList();
        if (result.isEmpty()) return null;
        return new UserAdapter(session, realm, model, result.get(0));
    }
```

## Working with multiple datasource

For solving the next error

```bash
Adding multiple last resources is disallowed
```

Add the following in the standalone.xml configuration file:

```xml
<server xmlns="urn:jboss:domain:16.0">
...
    <system-properties>
        <property name="com.arjuna.ats.arjuna.allowMultipleLastResources" value="true"/>
    </system-properties>
```

