package org.jfrog.build.extractor.npm.types;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.jfrog.build.api.producerConsumer.ProducerConsumerItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Objects;

import static org.jfrog.build.extractor.BuildInfoExtractorUtils.createMapper;

public class NpmPackageInfo implements Serializable, ProducerConsumerItem {
    private static final long serialVersionUID = 1L;

    private String name;
    private String version;
    private String scope;
    /**
     * A path-to-root dependency list that directly depends on this dependency.
     * The structure of each dependency in the list is 'dependency-name:dependency-version'
     * Used for 'RequestedBy' in {@link org.jfrog.build.api.Dependency}.
     */
    private String[] pathToRoot;

    @SuppressWarnings("unused")
    public NpmPackageInfo() {
    }

    public NpmPackageInfo(String name, String version, String scope, String[] pathToRoot) {
        this.name = name;
        this.version = version;
        this.scope = scope;
        this.pathToRoot = pathToRoot;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    void splitScopeFromName() {
        if (StringUtils.startsWith(name, "@") && StringUtils.contains(name, "/")) {
            String[] splitValues = StringUtils.split(name, "/");
            scope = splitValues[0];
            name = splitValues[1];
        }
    }

    public void readPackageInfo(InputStream inputStream) throws IOException {
        ObjectMapper mapper = createMapper();

        NpmPackageInfo npmPackageInfo = mapper.readValue(inputStream, NpmPackageInfo.class);

        setVersion(npmPackageInfo.getVersion());

        setName(npmPackageInfo.getName());
        splitScopeFromName();
        setPathToRoot(npmPackageInfo.getPathToRoot());
    }

    public String getModuleId() {
        String nameBase = String.format("%s:%s", name, version);
        if (StringUtils.isBlank(scope)) {
            return nameBase;
        }
        return String.format("%s:%s", scope.replaceFirst("^@", ""), nameBase);
    }

    public String getDeployPath() {
        String deployPath = String.format("%s/-/%s-%s.tgz", name, name, version);
        if (StringUtils.isBlank(scope)) {
            return deployPath;
        }
        return String.format("%s/%s", scope, deployPath);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != getClass()) {
            return false;
        }
        NpmPackageInfo other = (NpmPackageInfo) obj;
        return Objects.equals(name, other.getName()) && Objects.equals(version, other.getVersion()) && Objects.equals(scope, other.getScope());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, scope);
    }

    @Override
    public String toString() {
        return name + ":" + version;
    }

    public String[] getPathToRoot() {
        return pathToRoot;
    }

    public void setPathToRoot(String[] pathToRoot) {
        this.pathToRoot = pathToRoot;
    }
}
