import java.sql.*;
import java.time.LocalDate;
import java.util.*;

// Main class to run the system
public class SchoolManagementSystem {
    public static void main(String[] args) {
        School school = new School("ABC School");
        school.run();
    }
}

// School class that manages the entire system
class School {
    private final String name;
    private final Scanner scanner;
    private Connection connection;
    
    public School(String name) {
        this.name = name;
        this.scanner = new Scanner(System.in);
        
        // Initialize database connection
        initializeDatabase();
        
        // Add some sample data if tables are empty
        initializeSampleData();
    }
    
    private void initializeDatabase() {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Database connection parameters
            String url = "jdbc:mysql://localhost:3306/school_db";
            String username = "root";
            String password = "password"; // Change to your MySQL password
            
            // Establish connection
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database successfully!");
            
            // Create tables if they don't exist
            createTables();
            
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found. Please add the driver to your classpath.");
            System.exit(1);
        } catch (SQLException e) {
            System.out.println("Failed to connect to database: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void createTables() throws SQLException {
        // Create students table
        String createStudentsTable = "CREATE TABLE IF NOT EXISTS students (" +
                "id VARCHAR(10) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100), " +
                "grade VARCHAR(20), " +
                "enrollment_date DATE)";
        
        // Create teachers table
        String createTeachersTable = "CREATE TABLE IF NOT EXISTS teachers (" +
                "id VARCHAR(10) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(100), " +
                "department VARCHAR(50), " +
                "salary DECIMAL(10, 2))";
        
        // Create courses table
        String createCoursesTable = "CREATE TABLE IF NOT EXISTS courses (" +
                "code VARCHAR(10) PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "description TEXT)";
        
        // Create attendance table
        String createAttendanceTable = "CREATE TABLE IF NOT EXISTS attendance (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_id VARCHAR(10), " +
                "course_code VARCHAR(10), " +
                "date DATE, " +
                "is_present BOOLEAN, " +
                "FOREIGN KEY (student_id) REFERENCES students(id), " +
                "FOREIGN KEY (course_code) REFERENCES courses(code))";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createStudentsTable);
            stmt.execute(createTeachersTable);
            stmt.execute(createCoursesTable);
            stmt.execute(createAttendanceTable);
            System.out.println("Tables created or already exist.");
        }
    }
    
    private void initializeSampleData() {
        // Check if tables are empty and add sample data if needed
        try {
            // Check if students table is empty
            String checkStudents = "SELECT COUNT(*) FROM students";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkStudents)) {
                
                if (rs.next() && rs.getInt(1) == 0) {
                    // Add sample students
                    String[] studentData = {
                        "('S001', 'Alice Brown', 'alice@school.com', '10th Grade', '2023-09-01')",
                        "('S002', 'Bob Wilson', 'bob@school.com', '10th Grade', '2023-09-01')",
                        "('S003', 'Charlie Davis', 'charlie@school.com', '11th Grade', '2022-09-01')"
                    };
                    
                    for (String data : studentData) {
                        stmt.executeUpdate("INSERT INTO students VALUES " + data);
                    }
                    
                    // Add sample teachers
                    String[] teacherData = {
                        "('T001', 'John Smith', 'jsmith@school.com', 'Mathematics', 50000.00)",
                        "('T002', 'Jane Doe', 'jdoe@school.com', 'English', 48000.00)",
                        "('T003', 'Robert Johnson', 'rjohnson@school.com', 'Science', 52000.00)"
                    };
                    
                    for (String data : teacherData) {
                        stmt.executeUpdate("INSERT INTO teachers VALUES " + data);
                    }
                    
                    // Add sample courses
                    String[] courseData = {
                        "('MATH101', 'Mathematics', 'Basic Mathematics')",
                        "('ENG201', 'English', 'Advanced English')",
                        "('SCI301', 'Science', 'General Science')"
                    };
                    
                    for (String data : courseData) {
                        stmt.executeUpdate("INSERT INTO courses VALUES " + data);
                    }
                    
                    System.out.println("Sample data added to database.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error initializing sample data: " + e.getMessage());
        }
    }
    
    public void run() {
        boolean exit = false;
        
        while (!exit) {
            System.out.println("\n=== " + name + " Management System ===");
            System.out.println("1. Student Management");
            System.out.println("2. Teacher Management");
            System.out.println("3. Course Management");
            System.out.println("4. Attendance Management");
            System.out.println("5. Generate Reports");
            System.out.println("6. Exit");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> studentManagement();
                case 2 -> teacherManagement();
                case 3 -> courseManagement();
                case 4 -> attendanceManagement();
                case 5 -> generateReports();
                case 6 -> {
                    exit = true;
                    try {
                        if (connection != null && !connection.isClosed()) {
                            connection.close();
                            System.out.println("Database connection closed.");
                        }
                    } catch (SQLException e) {
                        System.out.println("Error closing database connection: " + e.getMessage());
                    }
                    System.out.println("Exiting system. Goodbye!");
                }
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void studentManagement() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n=== Student Management ===");
            System.out.println("1. Add New Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student Information");
            System.out.println("4. Remove Student");
            System.out.println("5. Back to Main Menu");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewAllStudents();
                case 3 -> updateStudent();
                case 4 -> removeStudent();
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void addStudent() {
        System.out.println("\n--- Add New Student ---");
        System.out.print("Enter Student ID: ");
        String id = scanner.nextLine();
        
        // Check if student ID already exists
        try {
            String checkSql = "SELECT id FROM students WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Student with this ID already exists.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        System.out.print("Enter Student Name: ");
        String studentName = scanner.nextLine();
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Grade: ");
        String grade = scanner.nextLine();
        
        // Insert student into database
        try {
            String sql = "INSERT INTO students (id, name, email, grade, enrollment_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.setString(2, studentName);
                pstmt.setString(3, email);
                pstmt.setString(4, grade);
                pstmt.setDate(5, java.sql.Date.valueOf(LocalDate.now()));
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Student added successfully!");
                } else {
                    System.out.println("Failed to add student.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void viewAllStudents() {
        System.out.println("\n--- All Students ---");
        
        try {
            String sql = "SELECT * FROM students ORDER BY id";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (!rs.isBeforeFirst()) {
                    System.out.println("No students found.");
                    return;
                }
                
                while (rs.next()) {
                    System.out.println("ID: " + rs.getString("id") +
                            ", Name: " + rs.getString("name") +
                            ", Email: " + rs.getString("email") +
                            ", Grade: " + rs.getString("grade") +
                            ", Enrollment Date: " + rs.getDate("enrollment_date"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void updateStudent() {
        System.out.println("\n--- Update Student Information ---");
        System.out.print("Enter Student ID to update: ");
        String id = scanner.nextLine();
        
        // First check if student exists
        try {
            String checkSql = "SELECT * FROM students WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Student with ID " + id + " not found.");
                        return;
                    }
                    
                    // Display current values
                    System.out.println("Current Name: " + rs.getString("name"));
                    System.out.println("Current Email: " + rs.getString("email"));
                    System.out.println("Current Grade: " + rs.getString("grade"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        // Get new values
        System.out.print("Enter new Name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        
        System.out.print("Enter new Email (press Enter to keep current): ");
        String newEmail = scanner.nextLine();
        
        System.out.print("Enter new Grade (press Enter to keep current): ");
        String newGrade = scanner.nextLine();
        
        // Update student in database
        try {
            StringBuilder sql = new StringBuilder("UPDATE students SET ");
            List<Object> params = new ArrayList<>();
            
            if (!newName.isEmpty()) {
                sql.append("name = ?, ");
                params.add(newName);
            }
            
            if (!newEmail.isEmpty()) {
                sql.append("email = ?, ");
                params.add(newEmail);
            }
            
            if (!newGrade.isEmpty()) {
                sql.append("grade = ?, ");
                params.add(newGrade);
            }
            
            // Remove trailing comma and space
            if (params.size() > 0) {
                sql.setLength(sql.length() - 2);
                sql.append(" WHERE id = ?");
                params.add(id);
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                    
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Student information updated successfully!");
                    } else {
                        System.out.println("Failed to update student information.");
                    }
                }
            } else {
                System.out.println("No changes made.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void removeStudent() {
        System.out.println("\n--- Remove Student ---");
        System.out.print("Enter Student ID to remove: ");
        String id = scanner.nextLine();
        
        try {
            // First delete attendance records for this student to maintain referential integrity
            String deleteAttendanceSql = "DELETE FROM attendance WHERE student_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAttendanceSql)) {
                pstmt.setString(1, id);
                pstmt.executeUpdate();
            }
            
            // Then delete the student
            String deleteStudentSql = "DELETE FROM students WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteStudentSql)) {
                pstmt.setString(1, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Student removed successfully!");
                } else {
                    System.out.println("Student with ID " + id + " not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void teacherManagement() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n=== Teacher Management ===");
            System.out.println("1. Add New Teacher");
            System.out.println("2. View All Teachers");
            System.out.println("3. Update Teacher Information");
            System.out.println("4. Remove Teacher");
            System.out.println("5. Back to Main Menu");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> addTeacher();
                case 2 -> viewAllTeachers();
                case 3 -> updateTeacher();
                case 4 -> removeTeacher();
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void addTeacher() {
        System.out.println("\n--- Add New Teacher ---");
        System.out.print("Enter Teacher ID: ");
        String id = scanner.nextLine();
        
        // Check if teacher ID already exists
        try {
            String checkSql = "SELECT id FROM teachers WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Teacher with this ID already exists.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        System.out.print("Enter Teacher Name: ");
        String teacherName = scanner.nextLine();
        
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        
        System.out.print("Enter Department: ");
        String department = scanner.nextLine();
        
        System.out.print("Enter Salary: ");
        double salary = getDoubleInput();
        
        // Insert teacher into database
        try {
            String sql = "INSERT INTO teachers (id, name, email, department, salary) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                pstmt.setString(2, teacherName);
                pstmt.setString(3, email);
                pstmt.setString(4, department);
                pstmt.setDouble(5, salary);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Teacher added successfully!");
                } else {
                    System.out.println("Failed to add teacher.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void viewAllTeachers() {
        System.out.println("\n--- All Teachers ---");
        
        try {
            String sql = "SELECT * FROM teachers ORDER BY id";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (!rs.isBeforeFirst()) {
                    System.out.println("No teachers found.");
                    return;
                }
                
                while (rs.next()) {
                    System.out.println("ID: " + rs.getString("id") +
                            ", Name: " + rs.getString("name") +
                            ", Email: " + rs.getString("email") +
                            ", Department: " + rs.getString("department") +
                            ", Salary: $" + rs.getDouble("salary"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void updateTeacher() {
        System.out.println("\n--- Update Teacher Information ---");
        System.out.print("Enter Teacher ID to update: ");
        String id = scanner.nextLine();
        
        // First check if teacher exists
        try {
            String checkSql = "SELECT * FROM teachers WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Teacher with ID " + id + " not found.");
                        return;
                    }
                    
                    // Display current values
                    System.out.println("Current Name: " + rs.getString("name"));
                    System.out.println("Current Email: " + rs.getString("email"));
                    System.out.println("Current Department: " + rs.getString("department"));
                    System.out.println("Current Salary: " + rs.getDouble("salary"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        // Get new values
        System.out.print("Enter new Name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        
        System.out.print("Enter new Email (press Enter to keep current): ");
        String newEmail = scanner.nextLine();
        
        System.out.print("Enter new Department (press Enter to keep current): ");
        String newDepartment = scanner.nextLine();
        
        System.out.print("Enter new Salary (press Enter to keep current): ");
        String salaryInput = scanner.nextLine();
        Double newSalary = null;
        if (!salaryInput.isEmpty()) {
            try {
                newSalary = Double.parseDouble(salaryInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid salary format. Salary not updated.");
            }
        }
        
        // Update teacher in database
        try {
            StringBuilder sql = new StringBuilder("UPDATE teachers SET ");
            List<Object> params = new ArrayList<>();
            
            if (!newName.isEmpty()) {
                sql.append("name = ?, ");
                params.add(newName);
            }
            
            if (!newEmail.isEmpty()) {
                sql.append("email = ?, ");
                params.add(newEmail);
            }
            
            if (!newDepartment.isEmpty()) {
                sql.append("department = ?, ");
                params.add(newDepartment);
            }
            
            if (newSalary != null) {
                sql.append("salary = ?, ");
                params.add(newSalary);
            }
            
            // Remove trailing comma and space
            if (params.size() > 0) {
                sql.setLength(sql.length() - 2);
                sql.append(" WHERE id = ?");
                params.add(id);
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                    
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Teacher information updated successfully!");
                    } else {
                        System.out.println("Failed to update teacher information.");
                    }
                }
            } else {
                System.out.println("No changes made.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void removeTeacher() {
        System.out.println("\n--- Remove Teacher ---");
        System.out.print("Enter Teacher ID to remove: ");
        String id = scanner.nextLine();
        
        try {
            String sql = "DELETE FROM teachers WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, id);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Teacher removed successfully!");
                } else {
                    System.out.println("Teacher with ID " + id + " not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void courseManagement() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n=== Course Management ===");
            System.out.println("1. Add New Course");
            System.out.println("2. View All Courses");
            System.out.println("3. Update Course Information");
            System.out.println("4. Remove Course");
            System.out.println("5. Back to Main Menu");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> addCourse();
                case 2 -> viewAllCourses();
                case 3 -> updateCourse();
                case 4 -> removeCourse();
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void addCourse() {
        System.out.println("\n--- Add New Course ---");
        System.out.print("Enter Course Code: ");
        String code = scanner.nextLine();
        
        // Check if course code already exists
        try {
            String checkSql = "SELECT code FROM courses WHERE code = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, code);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Course with this code already exists.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        System.out.print("Enter Course Name: ");
        String courseName = scanner.nextLine();
        
        System.out.print("Enter Course Description: ");
        String description = scanner.nextLine();
        
        // Insert course into database
        try {
            String sql = "INSERT INTO courses (code, name, description) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, code);
                pstmt.setString(2, courseName);
                pstmt.setString(3, description);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Course added successfully!");
                } else {
                    System.out.println("Failed to add course.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void viewAllCourses() {
        System.out.println("\n--- All Courses ---");
        
        try {
            String sql = "SELECT * FROM courses ORDER BY code";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (!rs.isBeforeFirst()) {
                    System.out.println("No courses found.");
                    return;
                }
                
                while (rs.next()) {
                    System.out.println("Code: " + rs.getString("code") +
                            ", Name: " + rs.getString("name") +
                            ", Description: " + rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void updateCourse() {
        System.out.println("\n--- Update Course Information ---");
        System.out.print("Enter Course Code to update: ");
        String code = scanner.nextLine();
        
        // First check if course exists
        try {
            String checkSql = "SELECT * FROM courses WHERE code = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkSql)) {
                pstmt.setString(1, code);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Course with code " + code + " not found.");
                        return;
                    }
                    
                    // Display current values
                    System.out.println("Current Name: " + rs.getString("name"));
                    System.out.println("Current Description: " + rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        // Get new values
        System.out.print("Enter new Name (press Enter to keep current): ");
        String newName = scanner.nextLine();
        
        System.out.print("Enter new Description (press Enter to keep current): ");
        String newDescription = scanner.nextLine();
        
        // Update course in database
        try {
            StringBuilder sql = new StringBuilder("UPDATE courses SET ");
            List<Object> params = new ArrayList<>();
            
            if (!newName.isEmpty()) {
                sql.append("name = ?, ");
                params.add(newName);
            }
            
            if (!newDescription.isEmpty()) {
                sql.append("description = ?, ");
                params.add(newDescription);
            }
            
            // Remove trailing comma and space
            if (params.size() > 0) {
                sql.setLength(sql.length() - 2);
                sql.append(" WHERE code = ?");
                params.add(code);
                
                try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setObject(i + 1, params.get(i));
                    }
                    
                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Course information updated successfully!");
                    } else {
                        System.out.println("Failed to update course information.");
                    }
                }
            } else {
                System.out.println("No changes made.");
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void removeCourse() {
        System.out.println("\n--- Remove Course ---");
        System.out.print("Enter Course Code to remove: ");
        String code = scanner.nextLine();
        
        try {
            // First delete attendance records for this course to maintain referential integrity
            String deleteAttendanceSql = "DELETE FROM attendance WHERE course_code = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteAttendanceSql)) {
                pstmt.setString(1, code);
                pstmt.executeUpdate();
            }
            
            // Then delete the course
            String deleteCourseSql = "DELETE FROM courses WHERE code = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteCourseSql)) {
                pstmt.setString(1, code);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Course removed successfully!");
                } else {
                    System.out.println("Course with code " + code + " not found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void attendanceManagement() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n=== Attendance Management ===");
            System.out.println("1. Mark Attendance");
            System.out.println("2. View Attendance Records");
            System.out.println("3. Back to Main Menu");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> markAttendance();
                case 2 -> viewAttendanceRecords();
                case 3 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void markAttendance() {
        System.out.println("\n--- Mark Attendance ---");
        
        // Display students
        viewAllStudents();
        
        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine();
        
        // Check if student exists
        try {
            String checkStudentSql = "SELECT id FROM students WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkStudentSql)) {
                pstmt.setString(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Student not found.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        // Display courses
        viewAllCourses();
        
        System.out.print("Enter Course Code: ");
        String courseCode = scanner.nextLine();
        
        // Check if course exists
        try {
            String checkCourseSql = "SELECT code FROM courses WHERE code = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkCourseSql)) {
                pstmt.setString(1, courseCode);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Course not found.");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return;
        }
        
        System.out.print("Enter Date (YYYY-MM-DD) or press Enter for today: ");
        String dateInput = scanner.nextLine();
        LocalDate date;
        
        if (dateInput.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateInput);
            } catch (Exception e) {
                System.out.println("Invalid date format. Using today's date.");
                date = LocalDate.now();
            }
        }
        
        System.out.print("Is the student present? (Y/N): ");
        String presentInput = scanner.nextLine();
        boolean isPresent = presentInput.equalsIgnoreCase("Y");
        
        // Insert attendance record into database
        try {
            String sql = "INSERT INTO attendance (student_id, course_code, date, is_present) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, courseCode);
                pstmt.setDate(3, java.sql.Date.valueOf(date));
                pstmt.setBoolean(4, isPresent);
                
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Attendance recorded successfully!");
                } else {
                    System.out.println("Failed to record attendance.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void viewAttendanceRecords() {
        System.out.println("\n--- Attendance Records ---");
        
        try {
            String sql = "SELECT a.*, s.name as student_name, c.name as course_name " +
                         "FROM attendance a " +
                         "JOIN students s ON a.student_id = s.id " +
                         "JOIN courses c ON a.course_code = c.code " +
                         "ORDER BY a.date DESC, a.student_id";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (!rs.isBeforeFirst()) {
                    System.out.println("No attendance records found.");
                    return;
                }
                
                while (rs.next()) {
                    System.out.println("Student: " + rs.getString("student_name") +
                            ", Course: " + rs.getString("course_name") +
                            ", Date: " + rs.getDate("date") +
                            ", Present: " + (rs.getBoolean("is_present") ? "Yes" : "No"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void generateReports() {
        boolean back = false;
        
        while (!back) {
            System.out.println("\n=== Generate Reports ===");
            System.out.println("1. Student Report");
            System.out.println("2. Teacher Report");
            System.out.println("3. Course Report");
            System.out.println("4. Attendance Report");
            System.out.println("5. Back to Main Menu");
            System.out.print("Please select an option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1 -> generateStudentReport();
                case 2 -> generateTeacherReport();
                case 3 -> generateCourseReport();
                case 4 -> generateAttendanceReport();
                case 5 -> back = true;
                default -> System.out.println("Invalid option. Please try again.");
            }
        }
    }
    
    private void generateStudentReport() {
        System.out.println("\n--- Student Report ---");
        viewAllStudents();
        
        try {
            String sql = "SELECT COUNT(*) as total FROM students";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    System.out.println("Total Students: " + rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void generateTeacherReport() {
        System.out.println("\n--- Teacher Report ---");
        viewAllTeachers();
        
        try {
            String countSql = "SELECT COUNT(*) as total FROM teachers";
            String salarySql = "SELECT SUM(salary) as total_salary FROM teachers";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet countRs = stmt.executeQuery(countSql);
                 ResultSet salaryRs = stmt.executeQuery(salarySql)) {
                
                if (countRs.next()) {
                    System.out.println("Total Teachers: " + countRs.getInt("total"));
                }
                
                if (salaryRs.next()) {
                    System.out.println("Total Salary Expenditure: $" + salaryRs.getDouble("total_salary"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void generateCourseReport() {
        System.out.println("\n--- Course Report ---");
        viewAllCourses();
        
        try {
            String sql = "SELECT COUNT(*) as total FROM courses";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    System.out.println("Total Courses: " + rs.getInt("total"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    private void generateAttendanceReport() {
        System.out.println("\n--- Attendance Report ---");
        
        try {
            String sql = "SELECT " +
                         "COUNT(*) as total_records, " +
                         "SUM(CASE WHEN is_present = TRUE THEN 1 ELSE 0 END) as present_count, " +
                         "SUM(CASE WHEN is_present = FALSE THEN 1 ELSE 0 END) as absent_count " +
                         "FROM attendance";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    int total = rs.getInt("total_records");
                    int present = rs.getInt("present_count");
                    int absent = rs.getInt("absent_count");
                    
                    double attendanceRate = total > 0 ? (double) present / total * 100 : 0;
                    
                    System.out.println("Total Attendance Records: " + total);
                    System.out.println("Present: " + present);
                    System.out.println("Absent: " + absent);
                    System.out.printf("Attendance Rate: %.2f%%\n", attendanceRate);
                } else {
                    System.out.println("No attendance records found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    
    // Utility method to get integer input
    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
    
    // Utility method to get double input
    private double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }
}