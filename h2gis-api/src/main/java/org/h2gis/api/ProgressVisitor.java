/*
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>. H2GIS is developed by CNRS
 * <http://www.cnrs.fr/>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; 
 * you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation;
 * version 3.0 of the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */

package org.h2gis.api;

import java.beans.PropertyChangeListener;

/**
 * Progression information.
 *
 * @author Nicolas Fortin
 */
public interface ProgressVisitor {


    String PROPERTY_CANCELED = "CANCELED";

    /***
     * Create a sub process. When this sub process finish it will count as a single step in this process.
     * @param stepCount Number of step into the sub-process.
     * @return
     */
    ProgressVisitor subProcess(int stepCount);

    /**
     * Same as {@link ProgressVisitor#setStep(int)} with currentStep++
     */
    void endStep();

    /**
     * @param idStep Set the current step, must be in [0-stepCount]
     */
    void setStep(int idStep);

    /**
     * @return The step count of this progress
     */
    int getStepCount();

    /**
     * Same thing as call {@link ProgressVisitor#setStep(int)} with step count.
     */
    void endOfProgress();

    /**
     * Get the step progression which belong to [0,1]
     *
     * @return This step progression [O-1], take account sub process progression.
     */
    double getProgression();

    /**
     * Return true if the process hes been canceled, false otherwise.
     *
     * @return True if the process has been canceled.
     */
    boolean isCanceled();

    /**
     * Call this method to cancel the operation.
     */
    void cancel();

    /**
     * Add the specified PropertyChangeListener for this visitor specified property.
     *
     * @param property Property name one of {@link #PROPERTY_CANCELED}.
     * @param listener PropertyChangeListener instance.
     */
    void addPropertyChangeListener(String property, PropertyChangeListener listener);

    /**
     * Remove the specified PropertyChangeListener.
     *
     * @param listener PropertyChangeListener instance.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
