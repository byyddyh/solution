package cn.byyddyh.dataProcess;

import cn.byyddyh.dataModel.GNSSAnalysis;
import cn.byyddyh.dataModel.GNSSRaw;
import cn.byyddyh.utils.MathUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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

        String rawCsvFile = makeCsv(dirName, fileName);
    }

    private static boolean checkFileType(String fileName) {

        String fileNameType = fileName.substring(fileName.length() - 4);
        for (String str : allowFiles) {
            if (str.equals(fileNameType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * %% make csv file, if necessary.
     */
    private static String makeCsv(String dirName, String fileName) throws IOException {
        if (!("" + dirName.charAt(dirName.length() - 1)).equals(File.separator)) {
            dirName += File.separator;
        }

        String csvFileName = dirName + "raw.csv";

        if (fileName.substring(fileName.length() - 4).equals(".csv")) {
            // input file is a csv file, nothing more to do here
            return csvFileName;
        }

        String extendedFileName = dirName + fileName;
        System.out.println("ReadGnssLogger \t\textendedFileName:" + extendedFileName);

        // read version
        File file = new File(extendedFileName);
        if (!file.canRead()) {
            throw new Error("file " + extendedFileName + " not found");
        }

        // 一行一行读，来获取版本信息
        boolean versionFLag = false;
        String line = "";
        List<String> allLines = Files.readAllLines(Paths.get(extendedFileName), StandardCharsets.UTF_8);
        int index = 0;
        for (; index < allLines.size(); ++index) {
            if (allLines.get(index).toLowerCase(Locale.ROOT).contains("version")) {
                versionFLag = true;
                line = allLines.get(index);
                break;
            }
        }
        if (!versionFLag) {
            System.out.println("ReadGnssLogger \t\tCould not find Version" + extendedFileName + " in input file");
            return csvFileName;
        }

        // look for the beginning of the version number, e.g. 1.4.0.0
        // 遍历 line 找到前四个数字
        int count = 0;
        List<Integer> version = new ArrayList<>();
        for (int i = 0; i < line.length(); i++) {
            if (Character.isDigit(line.charAt(i))) {
                version.add(line.charAt(i) - '0');
                ++count;
                if (count >= 4) {
                    break;
                }
            }
        }

        while (version.size() > 4) {
            version.add(0);
        }

        // Now extract the platform
        int k = line.indexOf("Platform:");
        String sPlatform = "";
        if (k >= 0) {
            sPlatform = line.substring(k + 9);
        }

        if (!sPlatform.contains("N")) {
            System.out.println("This version of ReadGnssLogger supports Android");
            System.out.println("WARNING: did not find \"Platform\" type in log file, expected \"Platform: N\"");
            System.out.println("Please Update GnssLogger");
            sPlatform = "N";
        }

        List<Integer> versionCom = Arrays.asList(1, 4, 0, 0);
        String result = CompareVersions(version, versionCom);

        if ("before".equals(result)) {
            System.out.println("This version of ReadGnssLogger supports v1.4.0.0 onwards");
            throw new Error("Found " + line + " in log file");
        }

        // write csv file with header and numbers
        /*
        * We could use grep and sed to make a csv file
        % fclose(txtfileID);
        % system(['grep -e ''Raw,'' ',extendedFileName,...
        %     ' | sed -e ''s/true/1/'' -e ''s/false/0/'' -e ''s/# //'' ',...
        %     ' -e ''s/Raw,//'' ',... %replace "Raw," with nothing
        %     '-e ''s/(//g'' -e ''s/)//g'' > ',csvFileName]);
        %
        % On versions from v1.4.0.0 N:
        % grep on "Raw," replace alpha characters amongst the numbers,
        % remove parentheses in the header,
        % note use of /g for "global" so sed acts on every occurrence in each line
        % csv file "prs.csv" now contains a header row followed by numerical data
        %
        % But we'll do the same thing using Matlab, so people don't need grep/sed:
        * */
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(Paths.get(csvFileName)), StandardCharsets.UTF_8));
            for (; index < allLines.size(); ++index) {
                line = allLines.get(index);
                if (!line.contains("Raw,")) {
                    continue;
                }

                line = line.replace("Raw,", "").replace("#", "").replace(" ", "");
                out.write(line);
                out.newLine();
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return csvFileName;
    }

    private static String CompareVersions(List<Integer> version, List<Integer> versionCom) {
        if (version.size() != versionCom.size()) {
            throw new Error("The two inputs must be scalars or vectors of the same length");
        }

        List<Integer> result = MathUtils.listSub(version, versionCom);
        for (Integer in : result) {
            if (in > 0) {
                return "after";
            } else if (in < 0) {
                return "before";
            }
        }

        return "equal";
    }

    public static void main(String[] args) {
        File file = new File("D:\\Programs\\pseudoranges_log_2021_08_23_16_21_01.txt");
        System.out.println(file);
        System.out.println(file.canRead());

        String str = "# Version: v2.0.0.1 Platform: 10 Manufacturer: HUAWEI Model: TAS-AN00";
        Integer index = str.indexOf("Platformx");
        System.out.println(index);
    }
}
