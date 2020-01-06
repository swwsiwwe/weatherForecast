
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

public class Main {
    public static void main(String... args) throws Exception {
        char flag = 'a';
        Date d = new Date();
        System.out.println("当前时间："+d);
        BufferedReader bf= new BufferedReader(new InputStreamReader(System.in));
        System.out.println("**-----欢迎使用天气查询-----**");
        System.out.println("加载中....请耐心等待");
        WeatherForecast weatherForecast = new WeatherForecast();
        System.out.println("加载成功!");
        while(flag!='q'){
            String c,t;
            weatherForecast.getAlist();
            System.out.println("选择你所查找的城市及天数：");
            System.out.print("城市（城市前序号）:");
            c = bf.readLine();
            if(Integer.parseInt(c)>34||Integer.parseInt(c)<1){
                System.out.println("输入错误，请输入1~34的数字");continue;
            }
            System.out.print("天数（1~4）:");
            t = bf.readLine();
            if(Integer.parseInt(t)>4||Integer.parseInt(t)<1){
                System.out.println("输入错误，请输入1~4的数字");continue;
            }
            WeatherForecast.query(Integer.parseInt(c),Integer.parseInt(t));
            System.out.println("按q退出，按其他键继续");
            flag = (char) bf.read();
        }
        bf.close();
    }
}
