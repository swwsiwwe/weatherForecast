import java.util.List;

/*JSON转换类*/
public class Forecast {
    public String status;
    public String count;
    public String info;
    public String infocode;
    public List<Forecasts> forecasts;
    public static class Forecasts{
        public String city;
        public String adcode;
        public String province;
        public String reporttime;
        public List<Casts> casts;
    }

    public static class Casts{
        public String date;
        public String week;
        public String dayweather;
        public String nightweather;
        public int daytemp;
        public int nighttemp;
        public String daywind;
        public String nightwind;
        public  String daypower;
        public String nightpower;
    }
}
