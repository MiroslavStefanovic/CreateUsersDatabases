package postgresql;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class CreateScript {

	public static void main(String[] args) {
		Connection connection = null;
		JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jfc.setCurrentDirectory(
				new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads"));

		int returnValue = jfc.showOpenDialog(null);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			System.out.println(selectedFile.getAbsolutePath());
			FrmCreated frmCreated = new FrmCreated();
			frmCreated.setVisible(true);
			// Creating a Workbook from an Excel file (.xls or .xlsx)
			Workbook workbook;
			try {
				workbook = WorkbookFactory.create(new File(selectedFile.getAbsolutePath()));
				Sheet sheet = workbook.getSheetAt(0);
				DataFormatter dataFormatter = new DataFormatter();
				Iterator<Row> rowIterator = sheet.rowIterator();

				Class.forName("org.postgresql.Driver");
				connection = DriverManager.getConnection("jdbc:postgresql://192.168.100.251:5432/postgres", "postgres",
						"postgres");
				Statement st = connection.createStatement();
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();
					Iterator<Cell> cellIterator = row.cellIterator();

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						String cellValue = dataFormatter.formatCellValue(cell);
						if (cell.getColumnIndex() == 1 && cell.getRowIndex() >= 6) {
							String brIndexa = cellValue.toLowerCase().replaceAll("/", "g").replaceAll(" ", "").trim();

							try {
								st.execute("DROP DATABASE IF EXISTS " + brIndexa + ";\r\n" + "DROP USER IF EXISTS "
										+ brIndexa + ";");

								String createUser = "DROP USER IF EXISTS " + brIndexa + ";\r\n" + "  CREATE USER "
										+ brIndexa + " WITH\r\n" + "  LOGIN\r\n" + "  NOSUPERUSER\r\n"
										+ "  NOINHERIT\r\n" + "  NOCREATEDB\r\n" + "  NOCREATEROLE\r\n"
										+ "  NOREPLICATION\r\n" + "  CONNECTION LIMIT -1\r\n" + "  PASSWORD 'ftn';\r\n";
								st.execute(createUser);
								// System.out.println("Created user: " + brIndexa);
								frmCreated.dlm.addElement("Created user: " + brIndexa);

								String createDatabase = "CREATE DATABASE " + brIndexa + "\r\n" + " WITH \r\n"
										+ " OWNER = " + brIndexa + "\r\n" + " ENCODING = 'UTF8'\r\n"
										+ " LC_COLLATE = 'English_United States.1252'\r\n"
										+ " LC_CTYPE = 'English_United States.1252'\r\n"
										+ " TABLESPACE = pg_default\r\n" + " CONNECTION LIMIT = -1;\r\n"
										+ " REVOKE CONNECT ON DATABASE " + brIndexa + " FROM PUBLIC;";
								st.execute(createDatabase);
							} catch (Exception e) {
								e.printStackTrace();
								frmCreated.dlm.addElement("				ERROR: " + brIndexa + "\n");
								continue;

							}
							frmCreated.dlm.addElement("Created database: " + brIndexa + "\n");
						}
					}
				}
				JOptionPane.showMessageDialog(null, "Završeno!", "", JOptionPane.INFORMATION_MESSAGE);
			} catch (EncryptedDocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					connection.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}
}