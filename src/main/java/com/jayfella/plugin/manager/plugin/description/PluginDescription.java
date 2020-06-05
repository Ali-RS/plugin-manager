package com.jayfella.plugin.manager.plugin.description;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jayfella.plugin.manager.exception.InvalidPluginDescriptionException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginDescription {

    /**
     * Must consist of all alphanumeric characters, underscores, hyphen, and period (a-z,A-Z,0-9, _.-).
     * Any other character will cause the plugin.json to fail loading.
     */
    // public static final Pattern ID_PATTERN = Pattern.compile("[a-z][a-z0-9-_]{0,63}");
    public static final Pattern ID_PATTERN = Pattern.compile("^[A-Za-z0-9 _.-]+$");


    private String main = null;
    private String type = null;

    private String id = null;
    private String version = null;
    private String description = null;

    private List<String> authors = null;
    private String website = null;
    private String prefix = null;

    private List<String> dependencies = new ArrayList<>();
    private List<String> softDependencies = new ArrayList<>();

    private boolean visible = true;

    public PluginDescription() {

    }

    public String getMain() { return main; }
    protected void setMain(String main) { this.main = main; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getId() { return id; }
    protected void setId(String id) {
        this.id = id;
    }

    public String getVersion() { return version; }
    protected void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    protected void setDescription(String description) { this.description = description; }

    public List<String> getAuthors() { return authors; }
    protected void setAuthors(List<String> authors) { this.authors = authors; }

    public String getWebsite() { return website; }
    protected void setWebsite(String website) { this.website = website; }

    public String getPrefix() { return prefix; }
    protected void setPrefix(String prefix) { this.prefix = prefix; }

    public List<String> getDependencies() { return dependencies; }
    protected void setDependencies(List<String> dependencies) { this.dependencies = dependencies; }

    public List<String> getSoftDependencies() { return softDependencies; }
    public void setSoftDependencies(List<String> softDependencies) { this.softDependencies = softDependencies; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    @NotNull
    @JsonIgnore
    public String getFullName() {
        return id + " v" + version;
    }

    /**
     * Validates the fields of the given JSON to ensure the required fields are set or that the
     * fields meet the requirements.
     * @throws InvalidPluginDescriptionException if the description is invalid.
     */
    public void validate() throws InvalidPluginDescriptionException {

        // id
        // validate that it exists and meets our requirements.
        try {

            if (!ID_PATTERN.matcher(id).matches()) {

                throw new InvalidPluginDescriptionException(
                        "Plugin ID '" + id + "' must match pattern '" + ID_PATTERN.pattern() + "'. "
                                + "It must consist of all alphanumeric characters, underscores, hyphen, and period (a-z,A-Z,0-9, _.-)"
                );
            }

            this.id = id.replace(' ', '_');
        } catch (NullPointerException e) {
            throw new InvalidPluginDescriptionException("id is not defined", e);
        } catch (ClassCastException e) {
            throw new InvalidPluginDescriptionException("id is of wrong type", e);
        }

        // main class
        if (main == null) {
            throw new InvalidPluginDescriptionException("main is not defined");
        }

    }

}
