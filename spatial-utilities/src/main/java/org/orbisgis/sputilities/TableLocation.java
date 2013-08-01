package org.orbisgis.sputilities;

/**
 * Just a class used to split Catalog Schema and Table. Theses components are a unique table identifier.
 * @author Nicolas Fortin
 */
public class TableLocation {
    private String catalog,schema,table;
    public TableLocation(String catalog, String schema, String table) {
        this.catalog = catalog;
        this.schema = schema;
        this.table = table;
    }
    public TableLocation(String schema, String table) {
        this("",schema,table);
    }

    public TableLocation(String table) {
        this("", table);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!catalog.isEmpty()) {
            sb.append(catalog);
            sb.append(".");
        }
        if(!schema.isEmpty()) {
            sb.append(schema);
            sb.append(".");
        }
        sb.append(table);
        return sb.toString();
    }

    /**
     * @return Table catalog name (database)
     */
    public String getCatalog() {
        return catalog;
    }

    /**
     * Convert catalog.schema.table, schema.table or table into TableLocation instance.
     * Not specified schema or catalog are converted into an empty string.
     * @param concatenatedTableLocation Table location [[Catalog.]Schema.]Table
     * @return Java beans for table location   Sample Text
     */
    public static TableLocation parse(String concatenatedTableLocation) {
        String[] values = concatenatedTableLocation.split("\\.");
        String catalog,schema,table;
        catalog = schema = table = "";
        switch (values.length) {
            case 1:
                table = values[0];
                break;
            case 2:
                schema = values[0];
                table = values[1];
                break;
            case 3:
                catalog = values[0];
                schema = values[1];
                table = values[2];
        }
        return new TableLocation(catalog,schema,table);
    }

    /**
     * @return Table schema name
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @return Table name
     */
    public String getTable() {
        return table;
    }
}
