package org.h2gis.h2spatialapi;

/**
 * A progress visitor that do nothing.
 * @author Nicolas Fortin
 */
public class EmptyProgressVisitor implements ProgressVisitor {
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
}
