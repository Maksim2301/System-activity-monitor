package com.example.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SystemStats {

    private Integer id;
    private User user;
    private BigDecimal cpuLoad;
    private BigDecimal ramUsedMb;
    private BigDecimal ramTotalMb;
    private BigDecimal diskTotalGb;
    private BigDecimal diskFreeGb;
    private BigDecimal diskUsedGb;
    private String activeWindow;
    private Integer keyboardPresses;
    private Integer mouseClicks;
    private Long mouseMoves;
    private Long systemUptimeSeconds;
    private LocalDateTime recordedAt;

    public SystemStats() {}

    public SystemStats(
            User user,
            BigDecimal cpuLoad,
            BigDecimal ramUsedMb,
            BigDecimal ramTotalMb,
            String activeWindow,
            Integer keyboardPresses,
            Integer mouseClicks,
            Long mouseMoves,
            Long systemUptimeSeconds,
            BigDecimal diskTotalGb,
            BigDecimal diskFreeGb,
            BigDecimal diskUsedGb
    ) {
        this.user = user;
        this.cpuLoad = cpuLoad;
        this.ramUsedMb = ramUsedMb;
        this.ramTotalMb = ramTotalMb;
        this.activeWindow = activeWindow;
        this.keyboardPresses = keyboardPresses;
        this.mouseClicks = mouseClicks;
        this.mouseMoves = mouseMoves;
        this.systemUptimeSeconds = systemUptimeSeconds;
        this.diskTotalGb = diskTotalGb;
        this.diskFreeGb = diskFreeGb;
        this.diskUsedGb = diskUsedGb;
        this.recordedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getCpuLoad() { return cpuLoad; }
    public void setCpuLoad(BigDecimal cpuLoad) { this.cpuLoad = cpuLoad; }

    public BigDecimal getRamUsedMb() { return ramUsedMb; }
    public void setRamUsedMb(BigDecimal ramUsedMb) { this.ramUsedMb = ramUsedMb; }

    public BigDecimal getRamTotalMb() { return ramTotalMb; }
    public void setRamTotalMb(BigDecimal ramTotalMb) { this.ramTotalMb = ramTotalMb; }

    public BigDecimal getDiskTotalGb() { return diskTotalGb; }
    public void setDiskTotalGb(BigDecimal diskTotalGb) { this.diskTotalGb = diskTotalGb; }

    public BigDecimal getDiskFreeGb() { return diskFreeGb; }
    public void setDiskFreeGb(BigDecimal diskFreeGb) { this.diskFreeGb = diskFreeGb; }

    public BigDecimal getDiskUsedGb() { return diskUsedGb; }
    public void setDiskUsedGb(BigDecimal diskUsedGb) { this.diskUsedGb = diskUsedGb; }

    public String getActiveWindow() { return activeWindow; }
    public void setActiveWindow(String activeWindow) { this.activeWindow = activeWindow; }

    public Integer getKeyboardPresses() { return keyboardPresses; }
    public void setKeyboardPresses(Integer keyboardPresses) { this.keyboardPresses = keyboardPresses; }

    public Integer getMouseClicks() { return mouseClicks; }
    public void setMouseClicks(Integer mouseClicks) { this.mouseClicks = mouseClicks; }

    public Long getMouseMoves() { return mouseMoves; }
    public void setMouseMoves(Long mouseMoves) { this.mouseMoves = mouseMoves; }

    public Long getSystemUptimeSeconds() { return systemUptimeSeconds; }
    public void setSystemUptimeSeconds(Long systemUptimeSeconds) { this.systemUptimeSeconds = systemUptimeSeconds; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
