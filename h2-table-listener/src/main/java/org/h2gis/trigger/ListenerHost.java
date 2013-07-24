package org.h2gis.trigger;

import org.h2.api.Trigger;

/**
 * Listener host contains instances of Triggers, theses triggers are released when the listener is removed or
 * when the listener host is released.
 * @author Nicolas Fortin
 */
public interface ListenerHost {
    void addListener(Trigger trigger, String table);
    void removeListener(Trigger trigger);
}
