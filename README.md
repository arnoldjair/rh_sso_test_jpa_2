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

