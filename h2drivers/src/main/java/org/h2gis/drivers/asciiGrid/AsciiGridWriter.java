/**
 * H2GIS is a library that brings spatial support to the H2 Database Engine
 * <http://www.h2database.com>.
 *
 * H2GIS is distributed under GPL 3 license. It is produced by CNRS
 * <http://www.cnrs.fr/>.
 *
 * H2GIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * H2GIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * H2GIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.h2gis.org/>
 * or contact directly: info_at_h2gis.org
 */
package org.h2gis.drivers.asciiGrid;

import it.geosolutions.imageio.plugins.arcgrid.AsciiGridsImageMetadata;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import org.h2.util.RasterUtils;
import org.h2gis.drivers.utility.PRJUtil;
import org.h2gis.h2spatialapi.ProgressVisitor;
import org.h2gis.utilities.JDBCUtilities;
import org.h2gis.utilities.SFSUtilities;
import org.h2gis.utilities.TableLocation;

/**
 *
 * @author Erwan Bocher
 */
public class AsciiGridWriter {
    private boolean isGrass;

    public AsciiGridWriter() {

    }

    public void write(Connection connection, String tableReference, File fileName, ProgressVisitor progress) throws SQLException, IOException {

        String filePath = fileName.getPath();
        final int dotIndex = filePath.lastIndexOf('.');
        String fileNameExtension = filePath.substring(dotIndex + 1).toLowerCase();
        String filePathWithoutExtension = filePath.substring(0, dotIndex);
        String imageFormat ;
        if (fileNameExtension.equals("asc")) {
            imageFormat = "Ascii ArcInfo";
            isGrass = false;
        } else if (fileNameExtension.equals("arx")) {
            imageFormat = "Ascii GRASS";
            isGrass=true;
        } else {
            throw new IOException("Cannot support this format : " + fileName.getAbsolutePath());
        }

        final boolean isH2 = JDBCUtilities.isH2DataBase(connection.getMetaData());
        int recordCount = JDBCUtilities.getRowCount(connection, tableReference);
        TableLocation location = TableLocation.parse(tableReference, isH2);
        List<String> rasterFieldNames = SFSUtilities.getRasterFields(connection, location);
        if (rasterFieldNames.isEmpty()) {
            throw new SQLException(String.format("The table %s does not contain a raster datatype", tableReference));
        }
        ProgressVisitor copyProgress = progress.subProcess(recordCount);

        Statement st = connection.createStatement();

        ResultSet res = st.executeQuery("SELECT " + rasterFieldNames.get(0) + " FROM " + location.toString());

        String imagePath = filePathWithoutExtension + "." + fileNameExtension;
        String prjPath = filePathWithoutExtension + ".prj";

        if (recordCount == 1) {
            res.next();
            writeRasterToFiles(connection, res.getBlob(1), imageFormat, imagePath, prjPath);
            progress.endStep();

        } else {
            int fileIt = 0;

            while (res.next()) {
                imagePath = filePathWithoutExtension + "_" + fileIt + "." + fileNameExtension;
                prjPath = filePathWithoutExtension + "_" + fileIt + ".prj";
                writeRasterToFiles(connection, res.getBlob(1), imageFormat, imagePath, prjPath);
                fileIt++;
                progress.endStep();

            }
        }
        res.close();
        st.close();
        copyProgress.endOfProgress();

    }

    /**
     * Write the georaster to an image using the imageio drivers
     *
     * @param connection
     * @param blob
     * @param imageFormat
     * @param imagePath
     * @param worldfilePath
     * @param prjPath
     * @throws IOException
     * @throws SQLException
     */
    private void writeRasterToFiles(Connection connection, Blob blob, String imageFormat, String imagePath, String prjPath) throws IOException, SQLException {
        ImageInputStream inputStream = ImageIO.createImageInputStream(blob);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
        ImageReader wkbReader = readers.next();
        wkbReader.setInput(inputStream);
        ImageWriter writer = (ImageWriter) ImageIO.getImageWritersByFormatName(imageFormat).next();
        ImageOutputStream stream = ImageIO.createImageOutputStream(new File(imagePath));
        writer.setOutput(stream);        
        RasterUtils.RasterMetaData met = RasterUtils.RasterMetaData
                .fetchMetaData(blob.getBinaryStream());        
        AsciiGridsImageMetadata gridMetadata = new AsciiGridsImageMetadata(
                met.width, met.height, met.scaleX, met.scaleY,
                met.ipX, met.ipY, 
                false, isGrass, met.bands[0].noDataValue);
        
        IIOImage iIOImage = new IIOImage(wkbReader.readAsRenderedImage(wkbReader.getMinIndex(), wkbReader.getDefaultReadParam()), null, gridMetadata);

        writer.write(iIOImage);                
        
        PRJUtil.writePRJ(connection, met.srid, new File(prjPath));

        stream.flush();
        stream.close();
        writer.dispose();
        inputStream.close();
    }

}
