# Solidbase Maven Plugin

Configure [Solidbase](https://github.com/gitbucket/solidbase/) migrations in pom.xml and run migrations from mvn.

## POM Configuraiton

```XML
<plugin>
    <groupId>io.github.goncha</groupId>
    <artifactId>solidbase-maven-plugin</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- include JDBC driver
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>${postgresql.version}</version>
        </dependency>
        -->
    </dependencies>

    <configuration>
        <!-- JDBC connection parameters -->
        <jdbc>
            <driver>org.postgresql.Driver</driver>
            <url>jdbc:postgresql://localhost:5432/demo</url>
            <user>postgres</user>
            <password>postgres</password>
        </jdbc>
        <!-- Database class in package liquibase.database.core  -->
        <database>PostgresDatabase</database>
        <!-- Modules parameters -->
        <modules>
            <module>
                <name>core</name>
                <versions>
                    <version>
                        <no>1.0.0</no>
                        <migrations>
                            <param>db/core_1.0.0.xml</param>
                            <param>db/core_1.0.0.sql</param>
                        </migrations>
                    </version>
                </versions>
            </module>
            <module>
            <!-- another module -->
            </module>
        </modules>
    </configuration>
</plugin>
```