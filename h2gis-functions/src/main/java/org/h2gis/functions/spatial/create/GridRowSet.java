/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <a href="http://www.h2database.com">http://www.h2database.com</a>. H2GIS is developed by CNRS
 * <a href="http://www.cnrs.fr/">http://www.cnrs.fr/</a>.
 *
 * This code is part of the H2GIS project. H2GIS is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; version 3.0 of
 * the License.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details <http://www.gnu.org/licenses/>.
 *
 *
 * For more information, please consult: <a href="http://www.h2gis.org/">http://www.h2gis.org/</a>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.functions.spatial.create;

import org.h2.tools.SimpleResultSet;
import org.h2.tools.SimpleRowSource;
import org.h2gis.utilities.TableLocation;
import org.h2gis.utilities.dbtypes.DBUtils;
import org.locationtech.jts.geom.*;

import java.sql.*;
import org.cts.util.UTMUtils;
import org.h2gis.utilities.GeographyUtilities;
import static org.h2gis.utilities.GeographyUtilities.computeLongitudeDistance;
import org.h2gis.utilities.GeometryMetaData;
import org.h2gis.utilities.GeometryTableUtilities;
import org.h2gis.utilities.Tuple;

/**
 * GridRowSet is used to populate a result set with all grid cells. A cell could
 * be represented as a polygon or its center point.
 *
 * @author Erwan Bocher
 */
public class GridRowSet implements SimpleRowSource {

    private static final GeometryFactory GF = new GeometryFactory();
    private static int cellI = 0;
    private static int cellJ = 0;
    private int maxI, maxJ;
    private double deltaX, deltaY;
    private double minX, minY, maxY;
    private int id = 0;
    private final Connection connection;
    private boolean firstRow = true;
    private Envelope envelope;
    private boolean isTable;
    private String tableName;
    private boolean isCenterCell = false;
    private int srid;
    private boolean isRowColumnNumber =false;
    private boolean upperCornerOrder=false;

    /**
     * The grid will be computed according a table stored in the database
     *
     * @param connection database
     * @param deltaX x size
     * @param deltaY y size
     * @param tableName table name
     */
    public GridRowSet(Connection connection, double deltaX, double deltaY, String tableName) {
        this.connection = connection;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.tableName = tableName;
        this.isTable = true;
    }

    /**
     * The grid will be computed according the envelope of a geometry
     *
     * @param connection database
     * @param deltaX x size
     * @param deltaY y size
     * @param geometry {@link Geometry}
     */
    public GridRowSet(Connection connection, double deltaX, double deltaY, Geometry geometry) {
        this.connection = connection;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.srid = geometry.getSRID();
        this.envelope = geometry.getEnvelopeInternal();
        this.isTable = false;
    }

    @Override
    public Object[] readRow() throws SQLException {
        if (firstRow) {
            reset();
        }
        if (cellI == maxI) {
            cellJ++;
            cellI = 0;
        }
        if (cellJ >= maxJ) {
            cellJ = 0;
            return null;
        }
        if (isCenterCell) {
            if(upperCornerOrder){
                return new Object[]{getCellPointUpper(), id++, cellI, cellJ + 1};
            }else {
                return new Object[]{getCellPoint(), id++, cellI, cellJ + 1};
            }
        }
        if(upperCornerOrder){
            return new Object[]{getCellPolygonUpper(), id++, cellI, cellJ + 1};
        }
        return new Object[]{getCellPolygon(), id++, cellI, cellJ + 1};
    }

    @Override
    public void close() {
    }

    @Override
    public void reset() throws SQLException {
        cellI = 0;
        cellJ = 0;
        firstRow = false;
        //We compute the extend according the first input value
        if (isTable) {
            Statement statement = connection.createStatement();
            //Find the SRID
            Tuple<String, GeometryMetaData> geomMetadata = GeometryTableUtilities.getFirstColumnMetaData(connection, TableLocation.parse(tableName, DBUtils.getDBType(connection)));
            srid = geomMetadata.second().SRID;
            try (ResultSet rs = statement.executeQuery("select ST_Extent(" + geomMetadata.first() + ")  from " + tableName)) {
                rs.next();
                Geometry geomExtend = (Geometry) rs.getObject(1);
                if (geomExtend == null) {
                    throw new SQLException("The envelope cannot be null.");
                } else {
                    envelope = geomExtend.getEnvelopeInternal();
                    initParameters();
                }

            }
        } else {
            if (envelope == null || envelope.isNull()) {
                throw new SQLException("The input geometry used to compute the grid cannot be null.");
            }
            else {
                initParameters();
            }
        }
    }

    /**
     * Compute the polygon corresponding to the cell
     *
     * @return Polygon of the cell
     */
    private Polygon getCellPolygon() {
        final Coordinate[] summits = new Coordinate[5];
        double x1 = minX + cellI * deltaX;
        double y1 = minY + cellJ * deltaY;
        double x2 = minX + (cellI + 1) * deltaX;
        double y2 = minY + (cellJ + 1) * deltaY;
        summits[0] = new Coordinate(x1, y1);
        summits[1] = new Coordinate(x2, y1);
        summits[2] = new Coordinate(x2, y2);
        summits[3] = new Coordinate(x1, y2);
        summits[4] = new Coordinate(x1, y1);
        final LinearRing g = GF.createLinearRing(summits);
        final Polygon gg = GF.createPolygon(g, null);
        cellI++;
        gg.setSRID(srid);
        return gg;
    }

    /**
     * Compute the polygon corresponding to the cell starting
     * to the upper corner
     *
     * @return Polygon of the cell
     */
    private Polygon getCellPolygonUpper() {
        final Coordinate[] summits = new Coordinate[5];
        double x1 = minX + cellI * deltaX;
        double y1 = maxY - cellJ * deltaY;
        double x2 = minX + (cellI + 1) * deltaX;
        double y2 = maxY - (cellJ + 1) * deltaY;
        summits[0] = new Coordinate(x1, y1);
        summits[1] = new Coordinate(x2, y1);
        summits[2] = new Coordinate(x2, y2);
        summits[3] = new Coordinate(x1, y2);
        summits[4] = new Coordinate(x1, y1);
        final LinearRing g = GF.createLinearRing(summits);
        final Polygon gg = GF.createPolygon(g, null);
        cellI++;
        gg.setSRID(srid);
        return gg;
    }


    /**
     * Compute the point of the cell according the upper corner
     *
     * @return Center point of the cell
     */
    private Point getCellPointUpper() {
        double x1 = (minX + cellI * deltaX) + (deltaX / 2d);
        double y1 = (maxY - cellJ * deltaY) - (deltaY / 2d);
        cellI++;
        Point gg = GF.createPoint(new Coordinate(x1, y1));
        gg.setSRID(srid);
        return gg;
    }


    /**
     * Compute the point of the cell
     *
     * @return Center point of the cell
     */
    private Point getCellPoint() {
        double x1 = (minX + cellI * deltaX) + (deltaX / 2d);
        double y1 = (minY + cellJ * deltaY) + (deltaY / 2d);
        cellI++;
        Point gg = GF.createPoint(new Coordinate(x1, y1));
        gg.setSRID(srid);
        return gg;
    }

    /**
     * Return true is cell is represented as point, false as a polygon
     *
     * @return trie if the grid is computed at center cell
     */
    public boolean isCenterCell() {
        return isCenterCell;
    }

    /**
     * Set if the cell must be represented as a point or a polygon
     *
     * @param isCenterCell true to compute on the center cell
     */
    public void setCenterCell(boolean isCenterCell) {
        this.isCenterCell = isCenterCell;
    }


    /**
     * Set true to define the delta x and y as number of columns and rows
     * @param isRowColumnNumber true if row/col number
     */
    public void setIsRowColumnNumber(boolean isRowColumnNumber){
        this.isRowColumnNumber =isRowColumnNumber;
    }

    /**
     * Return if the delta x and y must be expressed as number of columns and rows
     * @return true is the number of row and columns are fixed
     */
    public boolean isRowColumnNumber(){
        return this.isRowColumnNumber;
    }

    /**
     * Compute the parameters need to create each cells
     *
     */
    private void initParameters() throws SQLException {
        this.minX = envelope.getMinX();
        this.minY = envelope.getMinY();
        this.maxY=envelope.getMaxY();
        if(isRowColumnNumber()){
            if(deltaX<1 || deltaY<1){
                throw new SQLException("The number of columns and rows must be greater or equals than 1.");
            }
            this.minX = envelope.getMinX();
            this.minY = envelope.getMinY();
            this.maxY=envelope.getMaxY();
            double dx = envelope.getMaxX()-minX;
            double dy = envelope.getMaxY()-minY;
            this.maxI = (int) deltaX;
            this.maxJ = (int) deltaY;
            deltaX = dx/deltaX;
            deltaY = dy/deltaY;
        }
        else {
            if (this.srid == 4326) {
                if(deltaX<=0 || deltaY<=0){
                    throw new SQLException("The delta x and y of cell size must be greater than 0.");
                }
                double maxLon = envelope.getMaxX();
                double maxLat = envelope.getMaxY();
                //Check if the envelope has latitude, longitude co-ordinates
                if (!UTMUtils.isValidLatitude((float) minY)) {
                    throw new IllegalArgumentException("Invalid min latitude");
                }
                if (!UTMUtils.isValidLatitude((float) maxLat)) {
                    throw new IllegalArgumentException("Invalid max latitude");
                }
                if (!UTMUtils.isValidLongitude((float) minX)) {
                    throw new IllegalArgumentException("Invalid min longitude");
                }
                if (!UTMUtils.isValidLongitude((float) maxLon)) {
                    throw new IllegalArgumentException("Invalid max longitude");
                }
                deltaY = GeographyUtilities.computeLatitudeDistance(deltaY);
                deltaX = computeLongitudeDistance(deltaX, maxLat);
                double cellWidth = envelope.getWidth();
                double cellHeight = envelope.getHeight();
                this.maxI = (int) Math.ceil(cellWidth / deltaX);
                this.maxJ = (int) Math.ceil(cellHeight / deltaY);
            } else {
                if(deltaX<=0 || deltaY<=0){
                    throw new SQLException("The delta x and y of cell size must be greater than 0.");
                }
                double cellWidth = envelope.getWidth();
                double cellHeight = envelope.getHeight();
                this.maxI = (int) Math.ceil(cellWidth
                        / deltaX);
                this.maxJ = (int) Math.ceil(cellHeight
                        / deltaY);
            }
        }
    }

    /**
     * Give the regular grid
     *
     * @return ResultSet
     */
    public ResultSet getResultSet() throws SQLException {
        SimpleResultSet srs = new SimpleResultSet(this);
        srs.addColumn("THE_GEOM", Types.OTHER, "GEOMETRY", 0, 0);
        srs.addColumn("ID", Types.INTEGER, 10, 0);
        srs.addColumn("ID_COL", Types.INTEGER, 10, 0);
        srs.addColumn("ID_ROW", Types.INTEGER, 10, 0);
        return srs;
    }

    public void setUpperOrder(boolean upperOrder) {
        this.upperCornerOrder=upperOrder;
    }
}
