package cn.byyddyh;

import cn.byyddyh.dataProcess.ReadGnssLogger;

public class ProcessGnssMeasScript {
    public static void main(String[] args) throws Exception {
        // 文件名
        String fileName = "pseudoranges_log_2021_08_23_16_21_00.txt";
        // 路径名
        String dirName = "D:\\Programs";
        ReadGnssLogger.ReadGnssLogger(dirName, fileName);
    }
}
