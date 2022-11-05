package cn.byyddyh.dataModel;

import cn.byyddyh.utils.GpsConstants;

import java.util.Arrays;
import java.util.List;

public class UtcTime {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int sec;

    public UtcTime() {
    }

    public UtcTime(int year, int month, int day, int hour, int minute, int sec) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.sec = sec;
    }

    /**
     * Convert GPS time (week & seconds), or Full Cycle Time (seconds) to UTC
     */
    public static UtcTime gps2Utc(long fctSeconds) {
        // fct at 2100/1/1 00:00:00, not counting leap seconds:
        long fct2100 = 6260 * GpsConstants.WEEKSEC + 432000;
        if (fct2100 < 0 || fctSeconds >= fct2100) {
            throw new Error("gpsTime must be in this range: [0,0] <= gpsTime < [6260, 432000]");
        }

        // 应用算法处理跳跃秒
        // 1. convert gpsTime to time = [yyyy,mm,dd,hh,mm,ss] (with no leap seconds)
        UtcTime utcTime = fct2Ymdhms(fctSeconds);
        System.out.println("utcTime: " + utcTime);
        // 2. look up leap seconds for time: ls = LeapSeconds(time)
        int ls = leapSeconds(utcTime);
        System.out.println("ls \t" + ls);
        // 3. convert gpsTime-ls to timeMLs
        UtcTime timeMLs = fct2Ymdhms(fctSeconds - ls);
        // 4. look up leap seconds: ls1 = LeapSeconds(timeMLs);
        int ls1 = leapSeconds(timeMLs);
        // 5. if ls1~=ls, convert (gpsTime-ls1) to UTC Time
        if (ls == ls1) {
            utcTime = timeMLs;
        } else {
            utcTime = fct2Ymdhms(fctSeconds-ls1);
        }
        return utcTime;
    }

    private final static long HOURSEC = 3600;
    private final static long MINSEC = 60;
    private final static long[] monthDays = {31,28,31,30,31,30,31,31,30,31,30,31};

    /**
     * 工具类
     * 将GPS全周期时间转换为[yyyy，mm，dd，hh，mm，ss.s]格式
     */
    private static UtcTime fct2Ymdhms(long fctSeconds) {
        // days since 1980/1/1
        long days = fctSeconds / GpsConstants.DAYSEC + 6;
        System.out.println("days: " + days);
        System.out.println("fctSeconds: " + fctSeconds);
        int years = 1980;

        // 每次递减一年的天数，直到我们计算出年份
        int leap = 1;       // 1980年为闰年
        while (days > (leap + 365)) {
            days = days - (leap + 365);
            years += 1;
            // leap = 1 on a leap year, 0 otherwise
            // This works from 1901 till 2099, 2100 isn't a leap year (2000 is).
            // Calculate the year, ie time(1)
            leap = years % 4 == 0? 1: 0;
        }

        UtcTime time = new UtcTime();
        time.year = years;

        // 递减每一月的天数，直到我们计算出月份
        int month = 0;
        if (years % 4 == 0) {
            monthDays[1] = 29;
        } else {
            monthDays[1] = 28;
        }
        while (days > monthDays[month]) {
            days -= monthDays[month];
            month++;
        }

        time.month = month + 1;
        time.day = (int) days;

        long sinceMidnightSeconds = fctSeconds % GpsConstants.DAYSEC;
        System.out.println("sinceMidnightSeconds: \t\t" + sinceMidnightSeconds);
        time.hour = (int) (sinceMidnightSeconds / HOURSEC);

        long lastHourSeconds = sinceMidnightSeconds % HOURSEC;
        System.out.println("lastHourSeconds: \t\t" + lastHourSeconds);
        time.minute = (int) (lastHourSeconds / MINSEC);
        time.sec = (int) (lastHourSeconds % MINSEC);
        return time;
    }

    // UTC table contains UTC times (in the form of [year,month,day,hours,mins,secs])
    // At each of these times a leap second had just occurred
    // TODO 需要更新
    //  when a new leap second is announced in IERS Bulletin C
    //  update the table with the UTC time right after the new leap second
    private static UtcTime[] utcTable = {
            new UtcTime(1982, 1, 1, 0, 0, 0),
            new UtcTime(1982, 7, 1, 0, 0, 0),
            new UtcTime(1983, 7, 1, 0, 0, 0),
            new UtcTime(1985, 7, 1, 0, 0, 0),
            new UtcTime(1988, 1, 1, 0, 0, 0),
            new UtcTime(1990, 1, 1, 0, 0, 0),
            new UtcTime(1991, 1, 1, 0, 0, 0),
            new UtcTime(1992, 7, 1, 0, 0, 0),
            new UtcTime(1993, 7, 1, 0, 0, 0),
            new UtcTime(1994, 7, 1, 0, 0, 0),
            new UtcTime(1996, 1, 1, 0, 0, 0),
            new UtcTime(1997, 7, 1, 0, 0, 0),
            new UtcTime(1999, 1, 1, 0, 0, 0),
            new UtcTime(2006, 1, 1, 0, 0, 0),
            new UtcTime(2009, 1, 1, 0, 0, 0),
            new UtcTime(2012, 7, 1, 0, 0, 0),
            new UtcTime(2015, 7, 1, 0, 0, 0),
            new UtcTime(2017, 1, 1, 0, 0, 0)
    };

    /**
     * days since GPS Epoch
     */
    private static double[] tableJDays;
    private static double[] tableSeconds;

    static  {
        tableJDays = julianDay(utcTable);
        tableSeconds = new double[utcTable.length];

        for (int i = 0; i < tableJDays.length; i++) {
            // days since GPS Epoch
            tableJDays[i] -= GpsConstants.GPSEPOCHJD;

            // tableSeconds = tableJDays*GpsConstants.DAYSEC + utcTable(:,4:6)*[3600;60;1];
            // NOTE: JulianDay returns a realed value number, corresponding to days and fractions thereof, so we multiply it by DAYSEC to get the full time in seconds
            // JulianDay返回一个实数值，对应于天数及其分数，因此我们将其乘以DAYSEC，得到以秒为单位的完整时间
            tableSeconds[i] = tableJDays[i] * GpsConstants.DAYSEC + utcTable[i].hour * 3600 + utcTable[i].minute * 60 + utcTable[i].sec;
        }
    }

    /**
     * find the number of leap seconds since the GPS Epoch
     */
    private static int leapSeconds(UtcTime utcTime) {
        double[] jDay = julianDay(utcTime);
        double[] timeSeconds = new double[jDay.length];

        // tableSeconds和timeSeconds现在包含自GPS历元以来的秒数
        for (int i = 0; i < jDay.length; i++) {
            jDay[i] = jDay[i] - GpsConstants.GPSEPOCHJD;
            timeSeconds[i] = jDay[i] * GpsConstants.DAYSEC;
        }
        System.out.println("jDay: \t" + Arrays.toString(jDay));
        System.out.println("jDay: \t" + Arrays.toString(timeSeconds));

        int leapSecs = 0;
        for (double tableSecond : tableSeconds) {
            if (tableSecond <= timeSeconds[0]) {
                leapSecs++;
            }
        }

        return leapSecs;
    }

    /**
     * days since GPS Epoch
     */
    private static double[] julianDay(UtcTime... utcTimeList) {
        UtcTime[] temp = new UtcTime[utcTimeList.length];
        double[] tableJDays = new double[utcTimeList.length];

        for (int i = 0; i < temp.length; i++) {
            UtcTime data = new UtcTime();
            if (utcTimeList[i].month <= 2) {
                data.month = utcTimeList[i].month + 12;
                data.year = utcTimeList[i].year - 1;
            } else {
                data.month = utcTimeList[i].month;
                data.year = utcTimeList[i].year;
            }
            data.day = utcTimeList[i].day;
            data.hour = utcTimeList[i].hour;
            temp[i] = data;

            System.out.println(temp[i]);

            tableJDays[i] = Math.floor(365.25 * temp[i].year) + Math.floor(30.6001 * (temp[i].month + 1))  - 15 + 1720996.5 + temp[i].day + temp[i].hour / 24.0;
        }

        return tableJDays;
    }

    @Override
    public String toString() {
        return "UtcTime{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", hour=" + hour +
                ", minute=" + minute +
                ", sec=" + sec +
                '}';
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(tableJDays));
        System.out.println(Arrays.toString(tableSeconds));
    }
}
