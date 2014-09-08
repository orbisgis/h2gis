package org.h2gis.h2spatialapi;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * A progress visitor that do nothing.
 * @author Nicolas Fortin
 */
public class EmptyProgressVisitor implements ProgressVisitor {
    private boolean canceled = false;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    @Override
    public void endOfProgress() {

    }

    @Override
    public ProgressVisitor subProcess(int stepCount) {
        return this;
    }

    @Override
    public void endStep() {

    }

    @Override
    public void setStep(int idStep) {

    }

    @Override
    public int getStepCount() {
        return 0;
    }

    @Override
    public double getProgression() {
        return 0;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public void cancel() {
        boolean oldValue = canceled;
        canceled = true;
        propertyChangeSupport.firePropertyChange(PROPERTY_CANCELED, oldValue, true);
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(property, listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
