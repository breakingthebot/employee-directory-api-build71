/*
 * utils/CsvHelper.java
 * Utility helper class for parsing CSV input streams and formatting CSV export output.
 * Connects to: dto/EmployeeDTO.java, services/impl/EmployeeServiceImpl.java
 * Created: 2026-08-08
 */
package com.employee.directory.utils;

import com.employee.directory.dto.EmployeeDTO;
import com.employee.directory.enums.EmployeeStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class offering CSV read/write capabilities for employee records.
 */
public class CsvHelper {

    public static final String TYPE = "text/csv";
    private static final String[] HEADER = {"id", "firstName", "lastName", "email", "department", "jobTitle", "salary", "hireDate", "status"};

    /**
     * Checks if the uploaded file has a valid CSV MIME type or file extension.
     * 
     * @param file Uploaded file.
     * @return true if CSV, false otherwise.
     */
    public static boolean hasCsvFormat(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return (contentType != null && (contentType.equals("text/csv") || contentType.equals("application/vnd.ms-excel")))
                || (filename != null && filename.toLowerCase().endsWith(".csv"));
    }

    /**
     * Reads a CSV InputStream and parses each data row into an EmployeeDTO object.
     * 
     * @param is InputStream of CSV content.
     * @return List of parsed EmployeeDTO objects.
     * @throws IOException If stream reading fails.
     */
    public static List<EmployeeDTO> parseCsvToEmployeeDtos(InputStream is) throws IOException {
        List<EmployeeDTO> dtos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip blank lines
                }

                String[] values = parseCsvLine(line);

                if (isHeader) {
                    isHeader = false;
                    // Check if first line is header
                    if (values.length > 0 && values[0].equalsIgnoreCase("id") || values[0].equalsIgnoreCase("firstName")) {
                        continue;
                    }
                }

                if (values.length >= 7) {
                    EmployeeDTO dto = new EmployeeDTO();
                    dto.setFirstName(getSafeValue(values, 0));
                    dto.setLastName(getSafeValue(values, 1));
                    dto.setEmail(getSafeValue(values, 2));
                    dto.setDepartment(getSafeValue(values, 3));
                    dto.setJobTitle(getSafeValue(values, 4));

                    String salaryStr = getSafeValue(values, 5);
                    if (!salaryStr.isEmpty()) {
                        try {
                            dto.setSalary(new BigDecimal(salaryStr));
                        } catch (NumberFormatException e) {
                            dto.setSalary(null);
                        }
                    }

                    String hireDateStr = getSafeValue(values, 6);
                    if (!hireDateStr.isEmpty()) {
                        try {
                            dto.setHireDate(LocalDate.parse(hireDateStr));
                        } catch (Exception e) {
                            dto.setHireDate(null);
                        }
                    }

                    String statusStr = getSafeValue(values, 7);
                    if (!statusStr.isEmpty()) {
                        try {
                            dto.setStatus(EmployeeStatus.valueOf(statusStr.toUpperCase()));
                        } catch (Exception e) {
                            dto.setStatus(EmployeeStatus.ACTIVE);
                        }
                    } else {
                        dto.setStatus(EmployeeStatus.ACTIVE);
                    }

                    dtos.add(dto);
                }
            }
        }
        return dtos;
    }

    /**
     * Writes employee records to a Writer stream in standard CSV format.
     * 
     * @param writer Target Writer output stream.
     * @param employees List of EmployeeDTO records to export.
     * @throws IOException If writing fails.
     */
    public static void writeEmployeeDtosToCsv(Writer writer, List<EmployeeDTO> employees) throws IOException {
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println(String.join(",", HEADER));

        for (EmployeeDTO emp : employees) {
            List<String> row = new ArrayList<>();
            row.add(emp.getId() != null ? String.valueOf(emp.getId()) : "");
            row.add(escapeCsvField(emp.getFirstName()));
            row.add(escapeCsvField(emp.getLastName()));
            row.add(escapeCsvField(emp.getEmail()));
            row.add(escapeCsvField(emp.getDepartment()));
            row.add(escapeCsvField(emp.getJobTitle()));
            row.add(emp.getSalary() != null ? emp.getSalary().toString() : "0.00");
            row.add(emp.getHireDate() != null ? emp.getHireDate().toString() : "");
            row.add(emp.getStatus() != null ? emp.getStatus().name() : "ACTIVE");

            printWriter.println(String.join(",", row));
        }
        printWriter.flush();
    }

    private static String getSafeValue(String[] array, int index) {
        if (index < array.length && array[index] != null) {
            return array[index].trim();
        }
        return "";
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }
}
