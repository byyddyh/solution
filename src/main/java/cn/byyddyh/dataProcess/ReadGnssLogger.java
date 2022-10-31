package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSAnalysis;
import cn.byyddyh.dataModel.GNSSRaw;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ReadGnssLogger {
    private GNSSRaw gnssRaw;
    private GNSSAnalysis gnssAnalysis;

    private static List<String> allowFiles = Arrays.asList(".txt", ".csv");

    public ReadGnssLogger() {
        gnssRaw = new GNSSRaw();
        gnssAnalysis = new GNSSAnalysis();
        gnssAnalysis.setGnssClockErrors("GnssClock Errors.");
        gnssAnalysis.setGnssMeasurementErrors("GnssMeasurement Errors.");
    }

    public static void ReadGnssLogger(String dirName, String fileName) throws Exception {
        String extension = fileName.substring(fileName.length() - 3, fileName.length());

        if (!checkFileType(fileName)) {
            throw new Exception("Expecting file name of the form \"*.txt\", or \"*.csv");
        }


    }

    private static boolean checkFileType(String fileName) {

        String fileNameType = fileName.substring(fileName.length() - 4);
        for (String str :allowFiles) {
            if (str.equals(fileNameType)) {
                return true;
            }
        }

        return false;
    }

    private static String makeCsv(String dirName, String fileName) {
        if (!("" + dirName.charAt(dirName.length() - 1)).equals(File.separator)) {
            dirName += File.separator;
        }

        String csvFileName = dirName + ".csv";

        return "";
    }
}
