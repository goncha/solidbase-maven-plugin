package io.github.goncha.solidbase.maven;

/**
 * Mapping class for configuration
 *
 * @author Gang Chen
 */
public class ModuleVersion {

    private String no;

    private String[] migrations;

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String[] getMigrations() {
        return migrations;
    }

    public void setMigrations(String[] migrations) {
        this.migrations = migrations;
    }
}
