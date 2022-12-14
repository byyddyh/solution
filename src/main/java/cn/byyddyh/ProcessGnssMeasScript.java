package cn.byyddyh;

import cn.byyddyh.dataModel.GNSSMeas;
import cn.byyddyh.dataModel.GNSSRaw;
import cn.byyddyh.dataModel.GNSSGpsEph;
import cn.byyddyh.dataModel.UtcTime;
import cn.byyddyh.dataProcess.GNSSPosition;
import cn.byyddyh.dataProcess.NasaEphemeris;
import cn.byyddyh.dataProcess.PseudorangeProcess;
import cn.byyddyh.dataProcess.ReadGnssLogger;

public class ProcessGnssMeasScript {
    public static void main(String[] args) throws Exception {
        // 文件名
        String fileName = "pseudoranges_log_2021_08_23_16_23_40.txt";
        // 路径名
        String dirName = "D:\\Programs";

        // 读取记录信息
        GNSSRaw gnssRaw = ReadGnssLogger.ReadGnssLogger(dirName, fileName);
        if (gnssRaw.TimeNanos.size() == 0) {
            throw new Error("No data: gnssRaw.TimeNanos.size() = 0");
        }

        //  Get online ephemeris from Nasa ftp, first compute UTC Time from gnssRaw
        long fctSeconds = gnssRaw.allRxMillis.get(gnssRaw.allRxMillis.size() - 1) / 1000;
        System.out.println("fctSeconds: \t\t" + fctSeconds);
        UtcTime utcTime = UtcTime.gps2Utc(fctSeconds);
        System.out.println("utcTime: \t\t" + utcTime);

        // Get hourly ephemeris files
        GNSSGpsEph allGpsEph = NasaEphemeris.getNasaHourlyEphemeris(utcTime, dirName);
//        System.out.println(gnssGpsEph);

        // 处理原始测量值，计算伪距
        GNSSMeas gnssMeas = PseudorangeProcess.processGnssMeas(gnssRaw);

        // 计算WLS位置和速度
        GNSSPosition.gpsWlsPvt(gnssMeas, allGpsEph);
    }
}
