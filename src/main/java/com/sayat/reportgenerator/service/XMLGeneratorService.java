package com.sayat.reportgenerator.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;
import java.util.*;

@Service
public class XMLGeneratorService {

    public File generateJRXMLFromSQL(String sqlQuery) throws Exception {
        String tableName = "Dynamic_Report";
        try (Connection connection = connectToDatabase()) {
            Map<String, String> columns = getQueryStructure(connection, sqlQuery);
            return createJRXML(columns, tableName, sqlQuery);
        }
    }

    public File generateJRXMLFromTable(String tableName, String[] columnNames) throws Exception {
        try (Connection connection = connectToDatabase()) {
            String sqlQuery = buildSQLFromTable(tableName, columnNames);
            Map<String, String> columns = getTableStructure(connection, tableName, columnNames);
            return createJRXML(columns, tableName, sqlQuery);
        }
    }

    private Connection connectToDatabase() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/your_database";
        String username = "your_username";
        String password = "your_password";
        return DriverManager.getConnection(url, username, password);
    }

    private String buildSQLFromTable(String tableName, String[] columnNames) {
        String columnsPart = columnNames == null || columnNames.length == 0
                ? "*"
                : String.join(", ", columnNames);
        return "SELECT " + columnsPart + " FROM " + tableName;
    }

    private Map<String, String> getQueryStructure(Connection connection, String sqlQuery) throws SQLException {
        Map<String, String> columns = new LinkedHashMap<>();
        try (PreparedStatement stmt = connection.prepareStatement(sqlQuery + " LIMIT 1")) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                ResultSetMetaData metaData = resultSet.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    columns.put(metaData.getColumnName(i), mapDataType(metaData.getColumnTypeName(i)));
                }
            }
        }
        return columns;
    }

    private Map<String, String> getTableStructure(Connection connection, String tableName, String[] columnNames) throws SQLException {
        Map<String, String> columns = new LinkedHashMap<>();
        String query = "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, tableName);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String columnName = resultSet.getString("column_name");
                    String dataType = resultSet.getString("data_type");
                    if (columnNames == null || columnNames.length == 0 || containsIgnoreCase(columnNames, columnName)) {
                        columns.put(columnName, mapDataType(dataType));
                    }
                }
            }
        }
        return columns;
    }

    private boolean containsIgnoreCase(String[] array, String str) {
        for (String s : array) {
            if (s.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private String mapDataType(String dataType) {
        return switch (dataType.toLowerCase()) {
            case "varchar", "text", "character varying" -> "java.lang.String";
            case "integer", "int4" -> "java.lang.Integer";
            case "boolean" -> "java.lang.Boolean";
            default -> "java.lang.Object";
        };
    }

    private File createJRXML(Map<String, String> columns, String tableName, String sqlQuery)
            throws ParserConfigurationException, TransformerException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element rootElement = doc.createElement("jasperReport");
        doc.appendChild(rootElement);
        rootElement.setAttribute("xmlns", "https://jasperreports.sourceforge.net/jasperreports");
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xsi:schemaLocation", "https://jasperreports.sourceforge.net/jasperreports https://jasperreports.sourceforge.net/xsd/jasperreport.xsd");
        rootElement.setAttribute("name", tableName);
        rootElement.setAttribute("language", "groovy");

        Map<String, String> attributes = Map.of(
                "pageWidth", "595",
                "pageHeight", "842",
                "columnWidth", "555",
                "leftMargin", "20",
                "rightMargin", "20",
                "topMargin", "20",
                "bottomMargin", "20",
                "uuid", UUID.randomUUID().toString()
        );
        attributes.forEach(rootElement::setAttribute);

        Element queryString = doc.createElement("queryString");
        queryString.appendChild(doc.createCDATASection(sqlQuery));
        rootElement.appendChild(queryString);

        columns.forEach((name, type) -> {
            Element field = doc.createElement("field");
            field.setAttribute("name", name);
            field.setAttribute("class", type);
            rootElement.appendChild(field);
        });

        Element detail = doc.createElement("detail");
        Element detailBand = doc.createElement("band");
        detailBand.setAttribute("height", "30");
        int x = 0;
        for (String columnName : columns.keySet()) {
            Element textField = doc.createElement("textField");
            Element reportElement = doc.createElement("reportElement");
            reportElement.setAttribute("x", String.valueOf(x));
            reportElement.setAttribute("y", "0");
            reportElement.setAttribute("width", "100");
            reportElement.setAttribute("height", "20");
            textField.appendChild(reportElement);

            Element textFieldExpression = doc.createElement("textFieldExpression");
            textFieldExpression.appendChild(doc.createCDATASection("$F{" + columnName + "}"));
            textField.appendChild(textFieldExpression);

            detailBand.appendChild(textField);
            x += 100;
        }
        detail.appendChild(detailBand);
        rootElement.appendChild(detail);

        String fileName = tableName + "_report.jrxml";
        File outputFile = new File(System.getProperty("java.io.tmpdir"), fileName);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);

        return outputFile;
    }
}