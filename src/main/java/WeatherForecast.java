import com.google.gson.Gson;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/*天气预告类*/
public class WeatherForecast {
    private static ArrayList alist = new ArrayList<String>();
    private static URL u;
    private static String filename1 = new String("src/main/java/adcode.txt");
    private static String filename2 = new String("src/main/java/log.txt");
    private static String spec =
            "https://restapi.amap.com/v3/weather/weatherInfo?extensions=all&key=e4c1c5a1126f8674ac219ca3694cf18c&city=";
    static{
        //初始化数据库，若数据库不存在就创建，若已经存在就更新数据
        Connection conn = JDBCUtils.getConnection();
        PreparedStatement pst=null;
        try{
            pst = conn.prepareStatement("CREATE TABLE weather (\n" +
                    "  id int(11) AUTO_INCREMENT,\n" +
                    "  dayweather varchar(15) ,\n" +
                    "  nightweather varchar(15),\n" +
                    "  daytemp int ,\n" +
                    "  nighttemp int ,\n" +
                    "  daywind varchar(15),\n" +
                    "  nightwind varchar(15),\n" +
                    "  daypower varchar(10) ,\n" +
                    "  nightpower varchar(10) ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ")  DEFAULT CHARSET=utf8;");
            pst.execute();
            System.out.println("数据库第一次加载，请耐心等待！！");
            createFile(filename1,filename2);
            insert(filename2);
        }catch (Exception e){
            System.out.println("数据库已存在");
            try(BufferedReader br = new BufferedReader(new FileReader("src/main/java/city.txt"))){
                while(true){
                    String s=br.readLine();
                    if(s==null)
                        break;
                    alist.add(s);
                }
                update();
            }catch (IOException ie){
                ie.printStackTrace();
            }
        }
        JDBCUtils.close(null,pst,conn);
    }
    /*输出城市列表*/
    public void getAlist(){
        int cnt=0;
        for(Object str:alist){
            System.out.println(++cnt+"."+(String) str);
        }
    }
    /*天气查询*/
    public static void query(int m, int n) {
        int ct = (m-1)*4;
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        String sql = "select dayweather,nightweather,daytemp,nighttemp,daywind,nightwind,daypower,nightpower from weather where id = ?";
        try {
            conn = JDBCUtils.getConnection();
            if (conn == null) {
                throw new NullPointerException();
            }
            pst = conn.prepareStatement(sql);
            System.out.println(alist.get(m-1)+"未来"+n+"日天气:");
            for(int i = 0;i<n;i++) {
                pst.setInt(1, ct + i+1);
                rs = pst.executeQuery();
                while (rs.next()) {
                    System.out.print("白天天气："+rs.getString(1)+",");
                    System.out.print("夜晚天气："+rs.getString(2)+",");
                    System.out.print("白天温度："+rs.getInt(3)+",");
                    System.out.print("夜晚温度："+rs.getInt(4)+",");
                    System.out.print("白天风向："+rs.getString(5)+",");
                    System.out.print("夜晚风向："+rs.getString(6)+",");
                    System.out.print("白天风力："+rs.getString(7)+",");
                    System.out.println("夜晚风力："+rs.getString(8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JDBCUtils.close(rs,pst,conn);
    }
    /*天气更新*/
    public static void update(){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename2)))) {
            Gson gson = new Gson();
            Forecast f1,f2;
            f1 = gson.fromJson(in.readLine(), Forecast.class);
            String s1 = f1.forecasts.get(0).casts.get(0).date;
            StringBuilder tp = new StringBuilder();
            Class<?>[] types = new Class[3];
            types[0] = InputStream.class;
            types[1] = Reader.class;
            types[2] = String.class;
            try {
                u = new URL(spec+"110000");
                Object o1 = u.getContent(types);
                if (o1 instanceof String) {
                    tp.append((String)o1);
                }
                if (o1 instanceof Reader) {
                    int c;
                    Reader r = (Reader) o1;
                    while ((c = r.read()) != -1) {
                        tp.append((char)c);
                    }
                    r.close();
                }
                if (o1 instanceof InputStream) {
                    int c;
                    InputStream i = (InputStream) o1;
                    while ((c = i.read()) != -1) {
                        tp.append((char)c);
                    }
                    i.close();
                }
                f2 = gson.fromJson(tp.toString(), Forecast.class);
                String s2 = f2.forecasts.get(0).casts.get(0).date;
                if(!s1.equals(s2)){
                    System.out.println("检测到新的更新，请耐心等待更新！");
                    createFile(filename1,filename2);
                    updateAll(filename2);
                }
            } catch (MalformedURLException e) {
                System.err.println("it isn't a URL");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (IOException ie){
            ie.printStackTrace();
        }
        System.out.println("更新完成!");
    }
    /*在数据库中插入数据*/
    private static void insert(String filename) {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        String sql = "insert weather(dayweather,nightweather,daytemp,nighttemp,daywind,nightwind,daypower,nightpower) values(?,?,?,?,?,?,?,?)";
        try (BufferedReader read = new BufferedReader(new FileReader(filename))) {
            conn = JDBCUtils.getConnection();
            if (conn == null) {
                throw new NullPointerException();
            }
            Gson gson = new Gson();
            Forecast f;
            /*初始化数据库*/
            while (true) {
                String s = read.readLine();
                if(s==null)
                    break;
                f = gson.fromJson(s, Forecast.class);
                alist.add(f.forecasts.get(0).city);
                for (int i = 0; i < 4; i++) {
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, f.forecasts.get(0).casts.get(i).dayweather);
                    pst.setString(2, f.forecasts.get(0).casts.get(i).nightweather);
                    pst.setInt(3, f.forecasts.get(0).casts.get(i).daytemp);
                    pst.setInt(4, f.forecasts.get(0).casts.get(i).nighttemp);
                    pst.setString(5, f.forecasts.get(0).casts.get(i).daywind);
                    pst.setString(6, f.forecasts.get(0).casts.get(i).nightwind);
                    pst.setString(7, f.forecasts.get(0).casts.get(i).daypower);
                    pst.setString(8, f.forecasts.get(0).casts.get(i).nightpower);
                    pst.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JDBCUtils.close(rs,pst,conn);
    }
    private static void createFile(String filename1,String filename2) {
        String s;
        StringBuilder stringBuilder = new StringBuilder(WeatherForecast.spec);
        int n = WeatherForecast.spec.length();
        try(BufferedReader reader = new BufferedReader(new FileReader(filename1));OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(filename2))){
            while (true) {
                s = reader.readLine();
                if (s == null) {
                    break;
                }
                stringBuilder.append(s);
                //借鉴think in java 对getContent(type)的使用
                Class<?>[] types = new Class[3];
                types[0] = InputStream.class;
                types[1] = Reader.class;
                types[2] = String.class;
                try {
                    u = new URL(stringBuilder.toString());
                    Object o1 = u.getContent(types);
                    if (o1 instanceof String) {
                        out.write((String) o1);
                        out.write("\n");
                    }
                    if (o1 instanceof Reader) {
                        int c;
                        Reader r = (Reader) o1;
                        while ((c = r.read()) != -1) {
                            out.write((char) c);
                        }
                        out.write("\n");
                        r.close();
                    }
                    if (o1 instanceof InputStream) {
                        int c;
                        InputStream in = (InputStream) o1;
                        Reader r = new InputStreamReader(in, "UTF-8");
                        while ((c = r.read()) != -1) {
                            out.write((char) c);
                        }
                        out.write("\n");
                        in.close();
                    }
                } catch (MalformedURLException e) {
                    System.err.println("it isn't a URL");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stringBuilder.delete(n, n + 6);
            }
        }catch (IOException ie){
            ie.printStackTrace();
        }
    }
    private static void updateAll(String filename) {
        Connection conn = null;
        PreparedStatement pst = null;
        String sql = "update weather set dayweather = ?,nightweather = ?,daytemp = ?,nighttemp = ?,daywind = ?,nightwind = ?,daypower = ?,nightpower = ? where id = ?";
        try (BufferedReader read = new BufferedReader(new FileReader(filename))) {
            conn = JDBCUtils.getConnection();
            if (conn == null) {
                throw new NullPointerException();
            }
            Gson gson = new Gson();
            Forecast f;
            /*初始化数据库*/
            int cnt=1;
            while (true) {
                String s = read.readLine();
                if(s==null)
                    break;
                f = gson.fromJson(s, Forecast.class);
                if(alist.get(alist.size()-1).equals(f.forecasts.get(0).reporttime)){
                    break;
                }
                for (int i = 0; i < 4; i++) {
                    pst = conn.prepareStatement(sql);
                    pst.setString(1, f.forecasts.get(0).casts.get(i).dayweather);
                    pst.setString(2, f.forecasts.get(0).casts.get(i).nightweather);
                    pst.setInt(3, f.forecasts.get(0).casts.get(i).daytemp);
                    pst.setInt(4, f.forecasts.get(0).casts.get(i).nighttemp);
                    pst.setString(5, f.forecasts.get(0).casts.get(i).daywind);
                    pst.setString(6, f.forecasts.get(0).casts.get(i).nightwind);
                    pst.setString(7, f.forecasts.get(0).casts.get(i).daypower);
                    pst.setString(8, f.forecasts.get(0).casts.get(i).nightpower);
                    pst.setInt(9,cnt++);
                    pst.execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        JDBCUtils.close(null,pst,conn);
    }
}