
public class InitializeDatabase {
	
	  try {
          System.out.print("Enter the name of the database: ");
          String dbName = scanner.nextLine();
          String url = "jdbc:mysql://localhost:3306/";
          String user = "root";
          String password = "root";
          conn = DriverManager.getConnection(url + dbName, user, password);
          currentDB = dbName;
          
          Statement stmt = conn.createStatement();
          String sql = "CREATE TABLE IF NOT EXISTS countries (" +
                  "id INT(11) NOT NULL AUTO_INCREMENT," +
                  "name VARCHAR(255) NOT NULL," +
                  "PRIMARY KEY (id)" +
                  ")";
          stmt.executeUpdate(sql);
          System.out.println("Database initialized successfully.");
      } 
	  
	  catch (Exception e) {
          System.out.println("Error initializing database: " + e.getMessage());
      }
}
