import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
/*数据库工具类*/
public class JDBCUtils {
    private static final String CONNECTIONURL =
            "jdbc:mysql://localhost:3306/myjdbc?useUnicode-true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&characterEncoding=UTF-8";
    private static final String username = "root";
    private static final String password = "root";
    private static ArrayList<Connection> connections = new ArrayList<>();

    static {
        connections = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            connections.add(createConnection());
        }
    }

    public static Connection getConnection() {
        if (!connections.isEmpty()) {
            Connection con = connections.get(0);
            connections.remove(0);
            return con;
        } else {
            return createConnection();
        }
    }

    public static Connection createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return DriverManager.getConnection(CONNECTIONURL, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void close(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (st != null) st.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            connections.add(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}