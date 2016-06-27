package io.github.goncha.solidbase.maven;


import io.github.gitbucket.solidbase.Solidbase;
import io.github.gitbucket.solidbase.migration.LiquibaseMigration;
import io.github.gitbucket.solidbase.migration.Migration;
import io.github.gitbucket.solidbase.migration.SqlMigration;
import liquibase.database.AbstractJdbcDatabase;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Solidbase Migration Plugin
 *
 * @author Gang Chen
 */
@Mojo(
        name="migrate",
        requiresDependencyCollection = ResolutionScope.TEST
)
public class Plugin extends AbstractMojo {

    /**
     * The Maven project
     */
    @Parameter(
            property = "project",
            required = true,
            readonly = true
    )
    private MavenProject project;


    @Parameter(
            property = "jdbc",
            required = true
    )
    private Jdbc jdbc;

    @Parameter(
            property = "database",
            required = true
    )
    private String database;

    @Parameter(
            property = "modules",
            required = true
    )
    private List<Module> modules;


    List<io.github.gitbucket.solidbase.model.Module> getModules() {
        List<io.github.gitbucket.solidbase.model.Module> baseModules =
                new ArrayList<io.github.gitbucket.solidbase.model.Module>();
        for (Module m : modules) {
            List<io.github.gitbucket.solidbase.model.Version> versions = getVersions(m.getVersions());
            io.github.gitbucket.solidbase.model.Module baseModule =
                    new io.github.gitbucket.solidbase.model.Module(m.getName(), versions);
            baseModules.add(baseModule);
        }
        return baseModules;
    }

    List<io.github.gitbucket.solidbase.model.Version> getVersions(List<ModuleVersion> versions) {
        List<io.github.gitbucket.solidbase.model.Version> baseVersions =
                new ArrayList<io.github.gitbucket.solidbase.model.Version>();
        for (ModuleVersion v: versions) {
            List<Migration> migrations = new ArrayList<Migration>();
            for (String m : v.getMigrations()) {
                if (m.endsWith(".xml")) {
                    migrations.add(new LiquibaseMigration(m));
                } else if (m.endsWith(".sql")) {
                    migrations.add(new SqlMigration(m));
                }
            }
            baseVersions.add(new io.github.gitbucket.solidbase.model.Version(v.getNo(), migrations));
        }
        return baseVersions;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (jdbc.getDriver() == null) {
            getLog().error("jdbc/driver cannot be empty");
            throw new MojoExecutionException("jdbc/driver cannot be empty");
        }
        if (jdbc.getUrl() == null) {
            getLog().error("jdbc/url cannot be empty");
            throw new MojoExecutionException("jdbc/url cannot be empty");
        }

        try {
            Class.forName(jdbc.getDriver());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to load jdbc driver", e);
        }

        String databaseClassName = "liquibase.database.core." + database;
        AbstractJdbcDatabase jdbcDatabase = null;
        try {
            Class clazz = Class.forName(databaseClassName);
            jdbcDatabase = (AbstractJdbcDatabase) clazz.newInstance();
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Failed to create instance of %s", databaseClassName), e);
        }

        Connection connection = null;
        try {
            if (jdbc.getUser() != null && jdbc.getPassword() != null) {
                connection = DriverManager.getConnection(jdbc.getUrl());
            } else {
                connection = DriverManager.getConnection(jdbc.getUrl(), jdbc.getUser(), jdbc.getPassword());
            }
        } catch (SQLException e) {
            throw new MojoExecutionException(String.format("Failed to connect to database: %s", jdbc.getUrl()), e);
        }

        Solidbase solidbase = new Solidbase();
        ClassLoader classLoader = getClassLoader();
        try {
            for (io.github.gitbucket.solidbase.model.Module m : getModules()) {
                solidbase.migrate(connection, classLoader, jdbcDatabase, m);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute migration", e);
        }

        try {
            connection.commit();
        } catch (SQLException e) {
            throw new MojoExecutionException("Failed to commit", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ClassLoader getClassLoader() throws MojoExecutionException {
        try {
            List<String> classpathElements = project.getRuntimeClasspathElements();
            URL urls[] = new URL[classpathElements.size()];

            for (int i = 0; i < urls.length; i++) {
                urls[i] = new File(classpathElements.get(i)).toURI().toURL();
            }

            return new URLClassLoader(urls, getClass().getClassLoader());
        }
        catch (Exception e) {
            throw new MojoExecutionException("Couldn't create a classloader.", e);
        }
    }

}
