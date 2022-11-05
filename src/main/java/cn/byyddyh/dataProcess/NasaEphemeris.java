package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GnssGpsEph;
import cn.byyddyh.dataModel.UtcTime;

import java.io.File;
import java.nio.file.Files;

public class NasaEphemeris {

    private static GnssGpsEph gnssGpsEph;

    static {
        gnssGpsEph = new GnssGpsEph();
    }

    public static void getNasaHourlyEphemeris(UtcTime utcTime, String dirName) {
        dirName = checkInputs(dirName);
        int yearNumber4Digit = utcTime.year;
        int yearNumber2Digit = yearNumber4Digit % 100;
        int dayNumber = dayOfYear(utcTime);

        String hourlyZFile = "hour" + dayNumber + "0." + yearNumber2Digit + "n.Z";
        System.out.println("hourlyZFile: " + hourlyZFile);
        String ephFilename = hourlyZFile.substring(0, hourlyZFile.length() - 2);
        System.out.println("ephFilename: " + ephFilename);

        String fullEphFilename = dirName + ephFilename;
        System.out.println("fullEphFilename: " + fullEphFilename);

        /**
         * check if ephemeris file already exists (e.g. you downloaded it 'by hand') and if there are fresh ephemeris for lotsa sats within 2 hours of fctSeconds
         */
        boolean bGotGpsEph = false;
        File file = new File(fullEphFilename);
        if (!file.isFile()) {
            throw new Error("Error in GetNasaHourlyEphemeris" + ephFilename);
        }

        System.out.println("Reading GPS ephemeris from " + ephFilename + " file in local directory");

    }

    /**
     * check we have the utcTime and right kind of dirName
     */
    private static String checkInputs(String dirName) {
        if (dirName == null || dirName.length() == 0) {
            throw new Error(dirName + " not found");
        }

        File file = new File(dirName);
        if (!file.isDirectory()) {
            throw new Error(dirName + " not found");
        }

        return dirName + File.separator;
    }

    /**
     * Return the day number of the year
     */
    private static int dayOfYear(UtcTime utcTime) {
        double[] jDay = UtcTime.julianDay(new UtcTime(utcTime.year, utcTime.month, utcTime.day, 0, 0, 0));
        double[] jDayJan1 = UtcTime.julianDay(new UtcTime(utcTime.year, 1, 1, 0, 0, 0));
        return (int) (jDay[0] - jDayJan1[0] + 1);
    }

    /**
     * Read GPS ephemeris and iono data from an ASCII formatted RINEX 2.10 Nav file.
     */
    private static void readRinexNav(String fileName) {

    }
}
