package org.h2gis.tests;

import org.h2.util.StringUtils;
import org.h2gis.functions.factory.H2GISDBFactory;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SQLScriptTests {

    private static Connection connection;
    private Statement st;

    @BeforeAll
    public static void tearUp() throws Exception {
        // Keep a connection alive to not close the DataBase on each unit test
        connection = H2GISDBFactory.createSpatialDataBase(SQLScriptTests.class.getSimpleName());
    }

    @AfterAll
    public static void tearDown() throws Exception {
        connection.close();
    }

    @BeforeEach
    public void setUpStatement() throws Exception {
        st = connection.createStatement();
        st.execute("DROP TABLE IF EXISTS WATERNETWORK");
        final String path = StringUtils.quoteStringSQL(SQLScriptTests.class.getResource("waternetwork.gz").getPath());
        st.execute("CALL GeoJsonRead(" + path + ", 'WATERNETWORK');");
    }

    @AfterEach
    public void tearDownStatement() throws Exception {
        st.close();
    }

    @Test
    public void runSQLScript(){
        File directoryPath = new File(SQLScriptTests.class.getResource(".").getPath());
            FilenameFilter sqlFilter = (dir, name) -> {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".sql")) {
                    return true;
                } else {
                    return false;
                }
            };
        File[] filesList = directoryPath.listFiles(sqlFilter);
        for(File fileName : filesList) {
            assertDoesNotThrow(() -> {
                st.execute("RUNSCRIPT FROM '" + fileName.getAbsolutePath() + "'");
            });
        }
    }

}
