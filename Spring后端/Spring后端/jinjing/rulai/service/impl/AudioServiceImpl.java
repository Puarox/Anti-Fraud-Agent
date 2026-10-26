package rulai.service.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import rulai.service.IAudioService;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * UDP 语音 服务实现类
 * </p>
 *
 * @author LanZhan-Harmony
 * @since 2026-1-13
 */
@Service
public class AudioServiceImpl implements IAudioService {
    private static final int DISCOVERY_PORT = 8888; // 广播端口
    private static final int AUDIO_PORT = 9999; // 语音数据端口
    private static final int PHONE_HEADER_LIMIT = 11; // 手机号长度

    // 使用调度线程池
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "audio-discovery-scheduler");
        t.setDaemon(true);
        return t;
    });

    /**
     * 初始化方法，在服务启动时调用
     */
    @PostConstruct
    public void init() {
        addFirewallRules(); // 启动时自动配置防火墙
        scheduler.scheduleAtFixedRate(this::broadcastTask, 0, 2, TimeUnit.SECONDS); // 每2秒广播一次

        // 接收语音流（由于是阻塞式 receive，仍需独立线程）
        Thread t2 = new Thread(this::handleAudioStream, "audio-handler");
        t2.setDaemon(true);
        t2.start();
    }

    /**
     * 广播服务器 IP 地址
     */
    private void broadcastTask() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            String ip = getLocalIp();
            byte[] buf = ("SERVER_IP:" + ip).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(packet);
        } catch (Exception ignored) {
        }
    }

    /**
     * 停止方法，在服务关闭时调用
     */
    @PreDestroy
    public void stop() {
        scheduler.shutdown();
        removeFirewallRules(); // 停止时移除防火墙规则
    }

    /**
     * 处理音频流
     */
    private void handleAudioStream() {
        try (DatagramSocket socket = new DatagramSocket(AUDIO_PORT)) {
            byte[] buffer = new byte[2048];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                if (packet.getLength() >= PHONE_HEADER_LIMIT) {
                    // 前11字节为手机号
                    String phone = new String(packet.getData(), 0, PHONE_HEADER_LIMIT, StandardCharsets.UTF_8);

                    // 后面的内容是实际音频数据或控制信号
                    String content = new String(packet.getData(), PHONE_HEADER_LIMIT, packet.getLength() - PHONE_HEADER_LIMIT, StandardCharsets.UTF_8);

                    if ("BYE".equals(content.trim())) {
                        System.out.println("收到挂断信号，停止处理。");
                        // 这里可以清理 session 或发送确认包回安卓
                        continue;
                    }

                    // 正常回显音频
                    DatagramPacket echoPacket = new DatagramPacket(packet.getData(), packet.getLength(), packet.getAddress(), packet.getPort());
                    socket.send(echoPacket);
                }
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 主动挂断（服务器想挂断时调用）
     */
    public void serverHangUp(String clientIp, int clientPort, String phone) {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] msg = (phone + "BYE").getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, InetAddress.getByName(clientIp), clientPort);
            socket.send(packet);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取本机局域网 IP 地址
     *
     * @return 局域网 IP 地址
     */
    private String getLocalIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();

                // 过滤掉：未启动、回环(127.0.0.1)、虚拟网卡(如虚拟机的桥接网卡)
                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }

                // 排除含有常见虚拟化关键词的网卡名
                String name = iface.getDisplayName().toLowerCase();
                if (name.contains("virtual") || name.contains("vmware") || name.contains("hyper-v") || name.contains("vbox")) {
                    continue;
                }

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();

                    // 必须是 IPv4和私有局域网地址 (isSiteLocalAddress 会自动判断 10.x, 172.x, 192.x)
                    if (addr instanceof Inet4Address && addr.isSiteLocalAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "0.0.0.0"; // 如果实在找不到，返回一个非空的占位
    }

    /**
     * 添加防火墙规则，允许 UDP 端口通过（仅限 Windows）
     */
    private void addFirewallRules() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String ruleName = "Deeputy_Audio_UDP";
            String ports = DISCOVERY_PORT + "," + AUDIO_PORT;

            if (isFirewallRuleExisted(ruleName)) {
                System.out.println("Windows 防火墙规则已存在 (" + ruleName + ")，跳过添加。");
                return;
            }

            try {
                // 添加新规则，允许 UDP 端口
                ProcessBuilder pb = new ProcessBuilder(
                        "netsh", "advfirewall", "firewall", "add", "rule",
                        "name=" + ruleName,
                        "dir=in",
                        "action=allow",
                        "protocol=UDP",
                        "localport=" + ports
                );
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Windows 防火墙规则已自动添加 (UDP " + ports + ")");
                } else {
                    System.err.println("自动添加防火墙规则失败 (退出码: " + exitCode + ")。");
                    System.err.println("请以管理员权限运行程序，或手动执行以下命令：");
                    System.err.println("netsh advfirewall firewall add rule name=\"" + ruleName + "\" dir=in action=allow protocol=UDP localport=" + ports);
                }
            } catch (Exception e) {
                System.err.println("配置防火墙时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 移除防火墙规则（仅限 Windows）
     */
    private void removeFirewallRules() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String ruleName = "Deeputy_Audio_UDP";
            try {
                if (!isFirewallRuleExisted(ruleName)) {
                    return;
                }
                // 删除防火墙规则
                ProcessBuilder pb = new ProcessBuilder(
                        "netsh", "advfirewall", "firewall", "delete", "rule",
                        "name=" + ruleName
                );
                Process process = pb.start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Windows 防火墙规则已自动移除 (" + ruleName + ")");
                }
            } catch (Exception e) {
                System.err.println("移除防火墙规则时出错: " + e.getMessage());
            }
        }
    }

    /**
     * 检查防火墙规则是否已存在
     *
     * @param ruleName 规则名称
     * @return 是否存在
     */
    private boolean isFirewallRuleExisted(String ruleName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("netsh", "advfirewall", "firewall", "show", "rule", "name=" + ruleName);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}