/**
 * h2spatial is a library that brings spatial support to the H2 Java database.
 *
 * h2spatial is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
 *
 * h2patial is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * h2spatial is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * h2spatial. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */

package org.h2gis.h2spatialext.function.spatial.predicates;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.noding.SegmentString;
import com.vividsolutions.jts.operation.buffer.BufferParameters;
import com.vividsolutions.jts.operation.buffer.OffsetCurveBuilder;
import com.vividsolutions.jts.operation.buffer.OffsetCurveSetBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.h2gis.h2spatialapi.DeterministicScalarFunction;

/**
 *
 * @author Erwan Bocher
 */
public class ST_OffSetCurve extends DeterministicScalarFunction{

    @Override
    public String getJavaStaticMethod() {
        return "offsetCurve";
    }
    
    public static Geometry offsetCurve(Geometry geometry, double offset){
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeom = geometry.getGeometryN(i);
            if(subGeom.getDimension()==1){
                lineStringOffSetCurve(lineStrings, (LineString) subGeom, offset);
            }
            else{
                geometryOffSetCurve(lineStrings, geometry, offset);
            }
        }
        if (!lineStrings.isEmpty()) {
            if (lineStrings.size() == 1) {
                return lineStrings.get(0);
            } else {
                return geometry.getFactory().createMultiLineString(lineStrings.toArray(new LineString[lineStrings.size()]));
            }
        }
        return null;
    }
    
    public static void lineStringOffSetCurve(ArrayList<LineString> list, LineString lineString, double offset){
        list.add(lineString.getFactory().createLineString(new OffsetCurveBuilder(lineString.getPrecisionModel(),new BufferParameters()).getOffsetCurve(lineString.getCoordinates(), offset)));
    }
    
    public static void geometryOffSetCurve(ArrayList<LineString> list, Geometry geometry, double offset) {
        final List curves = new OffsetCurveSetBuilder(geometry, offset, new OffsetCurveBuilder(geometry.getFactory().getPrecisionModel(), new BufferParameters())).getCurves();
        final Iterator<SegmentString> iterator = curves.iterator();
        while (iterator.hasNext()) {
            list.add(geometry.getFactory().createLineString(iterator.next().getCoordinates()));
        }
    }
    
    
}
