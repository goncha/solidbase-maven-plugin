package io.github.goncha.solidbase.maven;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping class for configuration.
 *
 * @author Gang Chen
 */
public class Module {

    private String name;

    private List<ModuleVersion> versions = new ArrayList<ModuleVersion>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ModuleVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<ModuleVersion> versions) {
        this.versions = versions;
    }

}
