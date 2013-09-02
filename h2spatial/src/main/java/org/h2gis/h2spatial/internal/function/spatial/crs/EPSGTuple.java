/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.h2gis.h2spatial.internal.function.spatial.crs;

/**
 *
 * @author ebocher
 */
public class EPSGTuple {

    private int intputEPSG;
    private int targetEPSG;

    public EPSGTuple(int intputEPSG, int targetEPSG) {
        this.intputEPSG = intputEPSG;
        this.targetEPSG = targetEPSG;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.intputEPSG;
        hash = 67 * hash + this.targetEPSG;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EPSGTuple other = (EPSGTuple) obj;
        if (this.intputEPSG != other.intputEPSG) {
            return false;
        }
        if (this.targetEPSG != other.targetEPSG) {
            return false;
        }
        return true;
    }

    
    
}
