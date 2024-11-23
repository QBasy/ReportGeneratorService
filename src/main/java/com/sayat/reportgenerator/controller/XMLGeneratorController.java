package com.sayat.reportgenerator.controller;

import com.sayat.reportgenerator.service.XMLGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/xml-generator")
public class XMLGeneratorController {

    @Autowired
    private XMLGeneratorService xmlGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateXML(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String[] columns,
            @RequestBody(required = false) String sqlQuery) {
        try {
            File jrxmlFile;
            if (sqlQuery != null && !sqlQuery.isBlank()) {
                jrxmlFile = xmlGeneratorService.generateJRXMLFromSQL(sqlQuery);
            } else if (tableName != null && !tableName.isBlank()) {
                jrxmlFile = xmlGeneratorService.generateJRXMLFromTable(tableName, columns);
            } else {
                throw new IllegalArgumentException("Either SQL query or table name must be provided.");
            }

            byte[] fileContent = java.nio.file.Files.readAllBytes(jrxmlFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + jrxmlFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating JRXML: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/sendXML")
    public ResponseEntity<byte[]> sendXML(
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String[] columns,
            @RequestBody(required = false) String sqlQuery) {
        try {
            File jrxmlFile;
            if (sqlQuery != null && !sqlQuery.isBlank()) {
                jrxmlFile = xmlGeneratorService.generateJRXMLFromSQL(sqlQuery);
            } else if (tableName != null && !tableName.isBlank()) {
                jrxmlFile = xmlGeneratorService.generateJRXMLFromTable(tableName, columns);
            } else {
                throw new IllegalArgumentException("Either SQL query or table name must be provided.");
            }

            byte[] fileContent = java.nio.file.Files.readAllBytes(jrxmlFile.toPath());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + jrxmlFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/xml");

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error sending JRXML: " + e.getMessage()).getBytes());
        }
    }
}

