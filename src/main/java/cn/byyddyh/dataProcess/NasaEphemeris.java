package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSGpsEph;
import cn.byyddyh.dataModel.Iono;
import cn.byyddyh.dataModel.UtcTime;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NasaEphemeris {

    private static GNSSGpsEph gnssGpsEph;

    static {
        gnssGpsEph = new GNSSGpsEph();
    }

    public static GNSSGpsEph getNasaHourlyEphemeris(UtcTime utcTime, String dirName) throws IOException {
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

        /*初始化星历表结构*/
        int index = rinexNav[1];
        int num = 0;
        String line = "";
        for (int i = 0; i < rinexNav[0]; i++) {
            line = nasaLines.get(index++);
            gnssGpsEph.PRN.add(Integer.parseInt(line.substring(0, 2).trim()));
            int year = Integer.parseInt(line.substring(2, 6).trim());
            if (year < 80) {
                year += 2000;
            } else {
                year += 1900;
            }
            int month = Integer.parseInt(line.substring(6, 9).trim());
            int day = Integer.parseInt(line.substring(9, 12).trim());
            int hour = Integer.parseInt(line.substring(12, 15).trim());
            int minute = Integer.parseInt(line.substring(15, 18).trim());
            int second = (int) Double.parseDouble(line.substring(18, 22).trim());

            // convert Toc to gpsTime
            long[] gpsTime = UtcTime.utc2Gps(new UtcTime(year, month, day, hour, minute, second));
            gnssGpsEph.Toc.add((int) gpsTime[1]);

            // get all other parameters
            gnssGpsEph.af0.add(new BigDecimal(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.af1.add(new BigDecimal(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.af2.add(new BigDecimal(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.IODE.add((int) Double.parseDouble(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.Crs.add(Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.Delta_n.add(new BigDecimal(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.M0.add(Double.parseDouble(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.Cuc.add(new BigDecimal(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.e.add(Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.Cus.add(new BigDecimal(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.Asqrt.add(new BigDecimal(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.Toe.add((int) Double.parseDouble(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.Cic.add(new BigDecimal(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.OMEGA.add(Double.parseDouble(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.Cis.add(new BigDecimal(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.i0.add(Double.parseDouble(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.Crc.add(Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.omega.add(Double.parseDouble(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.OMEGA_DOT.add(new BigDecimal(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.IDOT.add(new BigDecimal(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.codeL2.add((int) Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.GPS_Week.add((int) Double.parseDouble(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.L2Pdata.add((int) Double.parseDouble(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.accuracy.add(Double.parseDouble(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.health.add((int) Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
            gnssGpsEph.TGD.add(new BigDecimal(line.substring(41, 60).replace('D', 'E').trim()));
            gnssGpsEph.IODC.add((int) Double.parseDouble(line.substring(60, 79).replace('D', 'E').trim()));

            line = nasaLines.get(index++);
            gnssGpsEph.ttx.add((int) Double.parseDouble(line.substring(3, 22).replace('D', 'E').trim()));
            gnssGpsEph.Fit_interval.add((int) Double.parseDouble(line.substring(22, 41).replace('D', 'E').trim()));
        }

        return gnssGpsEph;
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
                        iono.alpha.add(new BigDecimal(value.replace('D', 'E')));
                    }
                }
                bIonoAlpha = iono.alpha.size() == 4;
            } else if (line.contains("ION BETA")) {
                int i1 = line.indexOf("ION BETA");
                String[] strings = line.substring(0, i1).split(" ");

                for (String value : strings) {
                    if (value.length() != 0) {
                        iono.beta.add(new BigDecimal(value.replace('D', 'E')).longValue());
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
