package org.dromara.system.service.impl;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.domain.vo.*;
import org.dromara.system.service.ISysMonitorService;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 系统信息监控服务实现
 *
 * @author JunoYi
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysMonitorServiceImpl implements ISysMonitorService {

    private final Environment environment;

    /**
     * 获取系统监控信息（系统、服务器、Java、内存、磁盘）。
     *
     * @return 系统监控信息
     */
    @Override
    public SystemMonitorVo getSystemMonitorInfo() {
        SystemMonitorVo vo = new SystemMonitorVo();
        vo.setSystemInfo(getSystemBasicInfo());
        vo.setServerInfo(getServerInfo());
        vo.setJavaInfo(getJavaInfo());
        vo.setMemoryInfo(getMemoryInfo());
        vo.setDiskInfo(getDiskInfo());
        return vo;
    }

    /**
     * 获取系统基本信息。
     *
     * @return 系统基本信息
     */
    private SystemBasicInfoVo getSystemBasicInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        SystemBasicInfoVo vo = new SystemBasicInfoVo();
        vo.setName(environment.getProperty("spring.application.name", "RuoYi-Vue-Plus"));
        vo.setVersion(environment.getProperty("project.version", "-"));
        vo.setFrameworkVersion("Spring Boot " + SpringBootVersion.getVersion());
        vo.setEnvironment(getEnvironment());
        vo.setStartTime(DateUtil.formatDateTime(new Date(runtimeMXBean.getStartTime())));
        vo.setUptime(formatUptime(runtimeMXBean.getUptime()));
        return vo;
    }

    /**
     * 获取服务器信息。
     *
     * @return 服务器信息
     */
    private ServerInfoVo getServerInfo() {
        String hostName = "Unknown";
        String hostAddress = "Unknown";
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostName = localHost.getHostName();
            hostAddress = localHost.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取主机信息失败", e);
        }

        ServerInfoVo vo = new ServerInfoVo();
        vo.setName(hostName);
        vo.setOs(System.getProperty("os.name") + " " + System.getProperty("os.version"));
        vo.setArch(System.getProperty("os.arch"));
        vo.setCpuCores(Runtime.getRuntime().availableProcessors());
        vo.setIp(hostAddress);
        vo.setTime(DateUtil.formatDateTime(new Date()));
        return vo;
    }

    /**
     * 获取 Java 运行环境信息。
     *
     * @return Java 信息
     */
    private JavaInfoVo getJavaInfo() {
        JavaInfoVo vo = new JavaInfoVo();
        vo.setVersion(System.getProperty("java.version"));
        vo.setVendor(System.getProperty("java.vendor"));
        vo.setHome(System.getProperty("java.home"));
        vo.setJvmName(System.getProperty("java.vm.name"));
        vo.setJvmVersion(System.getProperty("java.vm.version"));
        vo.setArgs(String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
        return vo;
    }

    /**
     * 获取内存信息。
     *
     * @return 内存信息
     */
    private MemoryInfoVo getMemoryInfo() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        long jvmMax = heap.getMax();
        long jvmUsed = heap.getUsed();
        long jvmFree = jvmMax - jvmUsed;

        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        MemoryInfoVo vo = new MemoryInfoVo();
        vo.setTotal(formatBytes(total));
        vo.setUsed(formatBytes(used));
        vo.setFree(formatBytes(free));
        vo.setUsedPercent(percent(used, total));
        vo.setJvmTotal(formatBytes(jvmMax));
        vo.setJvmUsed(formatBytes(jvmUsed));
        vo.setJvmFree(formatBytes(jvmFree));
        vo.setJvmUsedPercent(percent(jvmUsed, jvmMax));
        return vo;
    }

    /**
     * 获取磁盘信息。
     *
     * @return 磁盘信息列表
     */
    private List<DiskInfoVo> getDiskInfo() {
        List<DiskInfoVo> list = new ArrayList<>();
        File[] roots = File.listRoots();
        if (roots == null) {
            return list;
        }
        for (File root : roots) {
            long total = root.getTotalSpace();
            long free = root.getFreeSpace();
            long used = total - free;

            DiskInfoVo vo = new DiskInfoVo();
            vo.setPath(root.getAbsolutePath());
            // JDK 标准 API 无法直接获取文件系统类型
            vo.setType("Unknown");
            vo.setTotal(formatBytes(total));
            vo.setUsed(formatBytes(used));
            vo.setFree(formatBytes(free));
            vo.setUsedPercent(percent(used, total));
            list.add(vo);
        }
        return list;
    }

    /**
     * 获取当前运行环境。
     *
     * @return 环境名称
     */
    private String getEnvironment() {
        String[] profiles = environment.getActiveProfiles();
        String env = profiles.length > 0 ? profiles[0] : "default";
        return switch (env.toLowerCase()) {
            case "prod", "production" -> "Production";
            case "dev", "development" -> "Development";
            case "test" -> "Test";
            case "local" -> "Local";
            default -> StringUtils.isBlank(env) ? "default" : env;
        };
    }

    /**
     * 计算占比百分比。
     *
     * @param used  已用量
     * @param total 总量
     * @return 百分比整数
     */
    private Integer percent(long used, long total) {
        return total > 0 ? (int) ((double) used / total * 100) : 0;
    }

    /**
     * 字节数格式化为可读文本。
     *
     * @param bytes 字节数
     * @return 可读文本
     */
    private String formatBytes(long bytes) {
        if (bytes < 0) {
            return "Unknown";
        }
        DecimalFormat df = new DecimalFormat("#.##");
        double kb = 1024.0;
        double mb = kb * 1024;
        double gb = mb * 1024;
        double tb = gb * 1024;
        if (bytes >= tb) {
            return df.format(bytes / tb) + " TB";
        } else if (bytes >= gb) {
            return df.format(bytes / gb) + " GB";
        } else if (bytes >= mb) {
            return df.format(bytes / mb) + " MB";
        } else if (bytes >= kb) {
            return df.format(bytes / kb) + " KB";
        }
        return bytes + " B";
    }

    /**
     * 运行时长格式化。
     *
     * @param uptimeMillis 运行毫秒数
     * @return 可读文本
     */
    private String formatUptime(long uptimeMillis) {
        Duration duration = Duration.ofMillis(uptimeMillis);
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("天 ");
        }
        if (hours > 0 || days > 0) {
            sb.append(hours).append("小时 ");
        }
        sb.append(minutes).append("分钟");
        return sb.toString();
    }

}
