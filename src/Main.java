import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

public class Main {

	private static final String DB_USER = null;

	private static final String DB_PASSWORD = null;

	private static final String DB_URL = null;

	private static final String DB_NAME = null;

	// Initializing Scanner
	private static Scanner sc = new Scanner(System.in);

//	Initializing Connection
	private static Connection conn;

	InitializeDatabase ID = new InitializeDatabase();

	public static void main(String[] args) {
		System.out.println("Welcome to the University Data Application!");
		boolean exit = false;

//      Initializing Menu with functions
		while (!exit) {
			System.out.println("Please select an option:");
			System.out.println("1. Initialize database");
			System.out.println("2. Enter database credentials");
			System.out.println("3. Select country");
			System.out.println("4. Print universities");
			System.out.println("5. Fetch data from API");
			System.out.println("6. Fetch data from database");
			System.out.println("7. Search data by attribute");
			System.out.println("8. Take database backup");
			System.out.println("9. Remove tables from database");
			System.out.println("10. Dump data to file");
			System.out.println("11. Retrieve data from file");
			System.out.println("12. Exit");

			int option = sc.nextInt();
			sc.nextLine(); // consume newline character

			switch (option) {
			case 1:
				initializeDatabase();
				break;
			case 2:
				enterDatabaseCredentials();
				break;
			case 3:
				selectCountry();
				break;
			case 4:
				printUniversities();
				break;
			case 5:
				fetchDataFromAPI();
				break;
			case 6:
				fetchDataFromDatabase();
				break;
			case 7:
				searchDataByAttribute();
				break;
			case 8:
				takeDatabaseBackup();
				break;
			case 9:
				removeTablesFromDatabase();
				break;
			case 10:
				dumpDataToFile();
				break;
			case 11:
				retrieveDataFromFile();
				break;
			case 12:
				exit = true;
				System.out.println("Goodbye!");
				break;
			default:
				System.out.println("Invalid option selected. Please try again.");
				break;
			}
		}
	}

	private static void initializeDatabase() {
		try {
			System.out.print("Enter the name of the database: ");
			String dbName = sc.nextLine();
			String url = "jdbc:mysql://localhost:3306/";
			String user = "root";
			String password = "root";
			conn = DriverManager.getConnection(url + dbName, user, password);
			String currentDB = dbName;

			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS countries (" + "id INT(11) NOT NULL AUTO_INCREMENT,"
					+ "name VARCHAR(255) NOT NULL," + "PRIMARY KEY (id)" + ")";
			stmt.executeUpdate(sql);
			System.out.println("Database initialized successfully.");
		} catch (Exception e) {
			System.out.println("Error initializing database: " + e.getMessage());
		}
	}

	private static void enterDatabaseCredentials() {
		try {
			System.out.print("Enter the database URL: ");
			String url = sc.nextLine();
			System.out.print("Enter the database username: ");
			String user = sc.nextLine();
			System.out.print("Enter the database password: ");
			String password = sc.nextLine();
			conn = DriverManager.getConnection(url, user, password);
			String currentDB = url.substring(url.lastIndexOf("/") + 1);
			System.out.println("Database credentials saved successfully.");
		} catch (Exception e) {
			System.out.println("Error entering database credentials: " + e.getMessage());
		}
	}

	public static void selectCountry() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter country name: ");
		String country = scanner.nextLine();

		String dbPassword = null;
		String dbUsername = null;
		String dbUrl = null;
		// assume universities are stored in a "universities" table in the database
		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM universities WHERE country = ?")) {
			stmt.setString(1, country);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				String state = rs.getString("state");
				String city = rs.getString("city");
				System.out.println(name + " (" + city + ", " + state + ")");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void printUniversities() {
		String dbUsername = null;
		String dbUrl = null;
		String dbPassword = null;
		try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
				PreparedStatement stmt = conn.prepareStatement("SELECT * FROM universities")) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("name");
				String country = rs.getString("country");
				String state = rs.getString("state");
				String city = rs.getString("city");
				System.out.println(name + " (" + city + ", " + state + ", " + country + ")");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void fetchDataFromAPI() {
		try {
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter the country code to fetch universities from (e.g. US, CA): ");
			String countryCode = scanner.nextLine();
			scanner.close();

			URL apiUrl = new URL("https://universities.hipolabs.com/search?country=" + countryCode);
			HttpURLConnection con = (HttpURLConnection) apiUrl.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

				JSONArray jsonArray = new JSONArray(response.toString());
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String name = jsonObject.getString("name");
					String country = jsonObject.getString("country");
					String state = jsonObject.optString("state-province");
					String city = jsonObject.optString("city");

					String dbUrl;
					String dbPassword;
					String dbUsername;
					try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
							PreparedStatement stmt = conn.prepareStatement(
									"INSERT INTO universities (name, country, state, city) VALUES (?, ?, ?, ?)")) {
						stmt.setString(1, name);
						stmt.setString(2, country);
						stmt.setString(3, state);
						stmt.setString(4, city);
						stmt.executeUpdate();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
				System.out.println("Successfully fetched and inserted university data from API.");
			} else {
				System.out.println("Failed to fetch university data from API. Response code: " + responseCode);
			}
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
	}

	public static void fetchDataFromDatabase() {
		try {
			String databasePassword = null;
			String databaseURL = null;
			String databaseUsername = null;
			// Create a connection to the database
			Connection connection = DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);

			// Create a prepared statement with the user's input as a parameter
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM universities WHERE country = ?");
			String selectedCountry = null;
			statement.setString(1, selectedCountry);

			// Execute the query and get the result set
			ResultSet resultSet = statement.executeQuery();

			// Iterate over the result set and print the universities
			while (resultSet.next()) {
				System.out.println(resultSet.getString("name"));
			}

			// Close the result set, statement, and connection
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void searchDataByAttribute() {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter the attribute you want to search by (name, state-province, website):");
		String attribute = scanner.nextLine();

		try {
			String databasePassword = null;
			String databaseURL = null;
			String databaseUsername = null;
			Connection connection = DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
			PreparedStatement statement = connection
					.prepareStatement("SELECT * FROM universities WHERE " + attribute + " LIKE ?");
			System.out.println("Enter the value you want to search for:");
			String value = scanner.nextLine();
			statement.setString(1, "%" + value + "%");
			ResultSet resultSet = statement.executeQuery();

			while (resultSet.next()) {
				System.out.println(resultSet.getString("name") + ", " + resultSet.getString("state_province") + ", "
						+ resultSet.getString("country") + ", " + resultSet.getString("website"));
			}

			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void takeDatabaseBackup() {
		try {
			// Create a new timestamp for the backup file
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date(0));

			// Create the backup file
			String backupFileName = "database_backup_" + timeStamp + ".sql";
			File backupFile = new File(backupFileName);
			backupFile.createNewFile();

			// Get the database connection
			Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

			// Execute the mysqldump command to backup the database to the file
			String[] executeCmd = new String[] { "mysqldump", "-u" + DB_USER, "-p" + DB_PASSWORD, DB_NAME,
					"-r" + backupFileName };
			Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
			int processComplete = runtimeProcess.waitFor();

			// Print the success or failure message
			if (processComplete == 0) {
				System.out.println("Backup taken successfully!");
			} else {
				System.out.println("Backup failed!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void removeTablesFromDatabase() {
		try {
			// Get the database connection
			Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

			// Get the list of tables in the database
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SHOW TABLES");

			// Print the list of tables to the console
			System.out.println("Tables in the database:");
			while (rs.next()) {
				System.out.println(rs.getString(1));
			}
			System.out.println();

			// Get the name of the table to remove from the user
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter the name of the table to remove: ");
			String tableName = scanner.nextLine();

			// Execute the DROP TABLE command to remove the table from the database
			stmt.executeUpdate("DROP TABLE " + tableName);

			// Print the success message
			System.out.println(tableName + " table removed from the database.");

			// Close the database connection and statement
			rs.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void dumpDataToFile() {
		try {
			// Get the database connection
			Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

			// Get the name of the table to dump from the user
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter the name of the table to dump: ");
			String tableName = scanner.nextLine();

			// Create a FileWriter object to write the data to a file
			FileWriter writer = new FileWriter(tableName + ".txt");

			// Execute the SELECT command to get the data from the table
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);

			// Get the ResultSetMetaData object to get the column names
			ResultSetMetaData metaData = rs.getMetaData();
			int numColumns = metaData.getColumnCount();

			// Write the column names to the file
			for (int i = 1; i <= numColumns; i++) {
				writer.write(metaData.getColumnName(i) + "\t");
			}
			writer.write("\n");

			// Write the data to the file
			while (rs.next()) {
				for (int i = 1; i <= numColumns; i++) {
					writer.write(rs.getString(i) + "\t");
				}
				writer.write("\n");
			}

			// Close the FileWriter, ResultSet, and statement objects
			writer.close();
			rs.close();
			stmt.close();
			conn.close();

			// Print the success message
			System.out.println("Data dumped to " + tableName + ".txt file.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void retrieveDataFromFile() {
		try {
			// Prompt user for file name
			System.out.print("Enter file name to retrieve data from: ");
			String fileName = sc.nextLine();

			// Read data from file
			FileInputStream fileInputStream = new FileInputStream(fileName);
			ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
			List<University> universities = (List<University>) objectInputStream.readObject();

			// Print data
			System.out.println("\nRetrieved universities:");
			for (University university : universities) {
				System.out.println(university.toString());
			}

			// Close streams
			objectInputStream.close();
			fileInputStream.close();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Error retrieving data from file: " + e.getMessage());
		}
	}

}