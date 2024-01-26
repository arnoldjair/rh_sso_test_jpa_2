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
