package name.ulbricht.dlx.config;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;

/// An in-memory implementation of [java.util.prefs.Preferences] for testing.
public final class InMemoryPreferences extends AbstractPreferences {

    private final Map<String, String> store = new HashMap<>();
    private final Map<String, InMemoryPreferences> children = new HashMap<>();

    /// Creates a new in-memory preferences node.
    ///
    /// @param parent the parent node, or `null` for a root node
    /// @param name   the name of this node
    public InMemoryPreferences(final AbstractPreferences parent, final String name) {
        super(parent, name);
    }

    @Override
    protected void putSpi(final String key, final String value) {
        this.store.put(key, value);
    }

    @Override
    protected String getSpi(final String key) {
        return this.store.get(key);
    }

    @Override
    protected void removeSpi(final String key) {
        this.store.remove(key);
    }

    @Override
    protected String[] keysSpi() {
        return this.store.keySet().toArray(String[]::new);
    }

    @Override
    protected String[] childrenNamesSpi() {
        return this.children.keySet().toArray(String[]::new);
    }

    @Override
    protected AbstractPreferences childSpi(final String name) {
        return this.children.computeIfAbsent(name, n -> new InMemoryPreferences(this, n));
    }

    @Override
    protected void removeNodeSpi() {
        this.store.clear();
        this.children.clear();
    }

    @Override
    protected void flushSpi() {
        // no-op
    }

    @Override
    protected void syncSpi() {
        // no-op
    }
}
