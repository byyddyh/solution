package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSRaw;
import cn.byyddyh.dataModel.GnssGpsEph;
import cn.byyddyh.dataModel.Iono;
import cn.byyddyh.dataModel.UtcTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NasaEphemeris {

    private static GnssGpsEph gnssGpsEph;

    static {
        gnssGpsEph = new GnssGpsEph();
    }

    public static void getNasaHourlyEphemeris(UtcTime utcTime, String dirName) throws IOException {
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

        List<String> nasaLines = Files.readAllLines(Paths.get(fullEphFilename), StandardCharsets.UTF_8);
        if (nasaLines.size() == 0) {
            throw new Error(fullEphFilename + " has no data");
        }

        /* new int[]{numEph / 8, numHdrLines} */
        int[] rinexNav = readRinexNav(nasaLines);
        System.out.println(Arrays.toString(rinexNav));

        /*用于读取标题行并查找iono参数*/
        Iono iono = readIono(nasaLines, rinexNav[1]);
        System.out.println(iono);
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
    private static int[] readRinexNav(List<String> nasaLines) {
        int numEph = 0;
        int numHdrLines = 0;

        // 读Header
        String line = "";
        int index = 0;
        try {
            // 处理标题头
            boolean bFoundHeader = false;
            while (true) {
                numHdrLines++;
                line = nasaLines.get(index++);
                if (line == null) {
                    break;
                }
                if (line.contains("END OF HEADER")) {
                    bFoundHeader = true;
                    break;
                }
            }
            if (!bFoundHeader) {
                throw new Error("Error reading file: Expected RINEX header not found");
            }

            // 读取内容
            while (index < nasaLines.size()) {
                numEph = numEph + 1;
                line = nasaLines.get(index++);
                if (line == null) {
                    break;
                } else if (line.length() != 79) {
                    throw new Error("Incorrect line length encountered in RINEX file");
                }
            }

            // check that we read the expected number of lines
            if (numEph % 8 != 0) {
                throw new Error("'Number of nav lines in should be divisible by 8");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new int[]{numEph / 8, numHdrLines};
    }

    /**
     * utility function to read thru the header lines, and find iono parameters
     */
    private static Iono readIono(List<String> nasaLines, int numHdrLines) {
        Iono iono = new Iono();
        boolean bIonoAlpha = false;
        boolean bIonoBeta = false;

        // Look for iono parameters, and read them in
        String line = "";
        for (int i = 0; i < numHdrLines; i++) {
            line = nasaLines.get(i);

            if (line.contains("ION ALPHA")) {
                int i1 = line.indexOf("ION ALPHA");
                String[] strings = line.substring(0, i1).split(" ");

                for (String value : strings) {
                    if (value.length() != 0) {
                        iono.alpha.add(value);
                    }
                }
                bIonoAlpha = iono.alpha.size() == 4;
            } else if (line.contains("ION BETA")) {
                int i1 = line.indexOf("ION BETA");
                String[] strings = line.substring(0, i1).split(" ");

                for (String value : strings) {
                    if (value.length() != 0) {
                        iono.beta.add(value);
                    }
                }
                bIonoBeta = iono.beta.size() == 4;
            }
        }

        if (!(bIonoAlpha && bIonoBeta)) {
            return null;
        }

        return iono;
    }

    public static void main(String[] args) {
        String str = "    0.5588D-08  0.1490D-07 -0.5960D-07 -0.1192D-06          ION ALPHA";
        int i1 = str.indexOf("ION ALPHA");
        System.out.println("i1: " + i1);

        String substring = str.substring(0, i1);
        System.out.println(substring);
        String[] s = substring.split(" ");
        System.out.println(Arrays.toString(s));

        List<String> list = new ArrayList<>();
        for (String value : s) {
            if (value.length() != 0) {
                System.out.println(value);
                list.add(value);
            }
        }
        System.out.println(list);
    }
}
