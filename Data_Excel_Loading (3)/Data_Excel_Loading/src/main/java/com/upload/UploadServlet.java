package com.upload;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.sql.*;
import java.util.Iterator;
import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/employee";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "root";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        int successCount = 0;
        int skipCount = 0;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            Part filePart = request.getPart("fileUpload");
            if (filePart == null || filePart.getSize() == 0) {
                response.getWriter().write("<div id='error-messages'>No file uploaded.</div>");
                return;
            }

            InputStream inputStream = filePart.getInputStream();

            try (Workbook workbook = new XSSFWorkbook(inputStream)) {
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                if (rowIterator.hasNext()) {
                    rowIterator.next(); // Skip header row
                }

                try (Connection conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                    String insertQuery = "INSERT INTO register (firstname, lastname, email, dob, password, phno, emptype, address, gender, state, city, hobbies, role, employee_exit_date) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    String checkEmailQuery = "SELECT COUNT(*) FROM register WHERE email = ?";

                    try (PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailQuery)) {
                        while (rowIterator.hasNext()) {
                            Row row = rowIterator.next();

                            String firstname = getCellValue(row.getCell(0));
                            String lastname = getCellValue(row.getCell(1));
                            String dob = getCellValue(row.getCell(2));
                            String password = getCellValue(row.getCell(3));
                            String phno = getCellValue(row.getCell(4));
                            String emptype = getCellValue(row.getCell(5)).trim().toLowerCase();
                            String address = getCellValue(row.getCell(6));
                            String gender = getCellValue(row.getCell(7));
                            String state = getCellValue(row.getCell(8));
                            String city = getCellValue(row.getCell(9));
                            String hobbies = getCellValue(row.getCell(10));
                            String role = getCellValue(row.getCell(11)).trim();
                            
                         // Regex for password validation
                            String passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

                            // Validate password complexity
                            if (!Pattern.matches(passwordPattern, password)) {
                                response.getWriter().write("<div id='error-messages'>"+ firstname + " " + lastname + ":- Password must be at least 8 characters long and include one uppercase, one lowercase, one number, and one special character. Skipping this row.<br></div>");
                                skipCount++; // Increment skipped count
                                continue;
                            }


                            // Validate emptype
                            if (!emptype.equals("contract") && !emptype.equals("permanent")) {
                                response.getWriter().write(firstname + " " + lastname + " :- " + "Invalid employment type:- " + " " + "you entered:-" + " " +(emptype) + " " + "Skipping this row.<br>");
                                skipCount++; // Increment skipped count
                                continue;
                            }

                            // Construct email
                            String email;
                            if (firstname != null && !lastname.isEmpty()) {
                                email = firstname.toLowerCase() + "." + lastname.toLowerCase() + "@contexio.co.in";
                            } else {
                                email = firstname.toLowerCase() + "@contexio.co.in";
                            }

                          if (!role.trim().equalsIgnoreCase("P1") && !role.trim().equalsIgnoreCase("P2")) {
                        	  response.getWriter().write(firstname + " " + lastname + " :- " + "Invalid role :- " + " " + "you entered:-" + " " +(role) + " " + "Skipping this row.<br>");
                        	  skipCount++; // Increment skipped count
                                 continue;
                             }

                             if (!phno.matches("\\d{10}")) {
                            	    if (!phno.matches("\\d+")) {
                            	        response.getWriter().write(firstname + " " + lastname + ":- Only numbers are allowed, not alphabets. Skipping this row.<br>");
                            	    } else {
                            	        response.getWriter().write(firstname + " " + lastname + ":- Contact number must be exactly 10 digits long. Skipping this row.<br>");
                            	        skipCount++; // Increment skipped count
                            	    }
                            	    continue;
                            	}
                             
                             if (!phno.matches("\\d{10}")) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Contact number must be exactly 10 digits long. Skipping this row.<br>");
                                 skipCount++; // Increment skipped count
                                 continue;
                             }
                             
 
                             if (!gender.equalsIgnoreCase("male") && !gender.equalsIgnoreCase("female") && !gender.equalsIgnoreCase("other")) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Only 'male', 'female', or 'other' are allowed. Skipping this row.<br>");
                                 skipCount++; // Increment skipped count
                                 continue;
                             }
 
                             if (firstname != null && !firstname.isEmpty() && password.toLowerCase().contains(firstname.toLowerCase())) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Password is weak as it contains first name. Skipping this row.<br>");
                                 skipCount++; // Increment skipped count
                                 continue;
                             }
 
                             if (lastname != null && !lastname.isEmpty() && password.toLowerCase().contains(lastname.toLowerCase())) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Password is weak as it contains last name. Skipping this row.<br>");
                                 skipCount++; // Increment skipped count
                                 continue;
                             }
                             
                          // Check if password contains "Contexio", date of joining, or contact number (Medium Password)
                             boolean isMediumPassword = false;
                             if (password.toLowerCase().contains("contexio")) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Warning: Password is medium as it contains 'Contexio' from email ID.<br>");
                                 skipCount++; // Increment skipped count
                                 isMediumPassword = true;
                             }
                             if (dob != null && !dob.isEmpty() && password.contains(dob)) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Warning: Password is medium as it contains date of joining.<br>");
                                 skipCount++; // Increment skipped count
                                 isMediumPassword = true;
                             }
                             
                             if (phno != null && !phno.isEmpty() && password.contains(phno)) {
                                 response.getWriter().write(firstname + " " + lastname + ":- Warning: Password is medium as it contains contact number.<br>");
                                 skipCount++; // Increment skipped count
                                 isMediumPassword = true;
                             }
                            checkEmailStmt.setString(1, email);
                            try (ResultSet rs = checkEmailStmt.executeQuery()) {
                                if (rs.next() && rs.getInt(1) > 0) {
                                    response.getWriter().write("User with email " + email + " is already registered.<br>");
                                    skipCount++; // Increment skipped count
                                    continue;
                                }
                            }

                            // Hash password
                            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

                            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                                stmt.setString(1, firstname);
                                stmt.setString(2, lastname);
                                stmt.setString(3, email);
                                stmt.setString(4, dob);
                                stmt.setString(5, hashedPassword);
                                stmt.setString(6, phno);
                                stmt.setString(7, emptype);
                                stmt.setString(8, address);
                                stmt.setString(9, gender);
                                stmt.setString(10, state);
                                stmt.setString(11, city);
                                stmt.setString(12, hobbies);
                                stmt.setString(13, role);
                                stmt.setNull(14, Types.DATE);

                                stmt.executeUpdate();
                                successCount++;
                            } catch (SQLException e) {
                                response.getWriter().write("Error inserting data for: " + email + ". Reason: " + e.getMessage() + "<br>");
                                skipCount++;
                            }
                        }
                    }
                    response.getWriter().write("File uploaded and data saved successfully!<br>");
                    response.getWriter().write("Total employees inserted: " + successCount + "<br>");
                    response.getWriter().write("Total rows skipped: " + skipCount + "<br>");
                } catch (SQLException e) {
                    response.getWriter().write("Database error: " + e.getMessage());
                    e.printStackTrace();
                }
            } catch (Exception e) {
                response.getWriter().write("Error processing file: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: 
                if (cell.getColumnIndex() == 3) { // Assuming column 3 is phno
                    return String.valueOf((long) cell.getNumericCellValue());
                }
                return DateUtil.isCellDateFormatted(cell) ?
                        new java.text.SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue()) :
                        String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
