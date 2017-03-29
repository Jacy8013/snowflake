package io.jacy.common.utils;

/**
 * ID中心
 * Created by Jacy on 2016/11/18.
 */
public enum IdWorker {
    instance(Integer.parseInt(System.getProperty("instance.id")));                      // 识别配置中的节点ID

    private final long workerId;
    private final long epoch = 1454601600000L;                                          // 时间起始标记点(2016-02-05)
    private final long workerIdBits = 10L;                                              // 机器标识位数
    private final long maxWorkerId = -1L ^ -1L << this.workerIdBits;                    // 机器ID最大值: 1023
    private long sequence = 0L;                                                         // 0，同一时间戳并发起始值
    private final long sequenceBits = 12L;                                              // 毫秒内自增位

    private final long workerIdShift = this.sequenceBits;                               // 12
    private final long timestampLeftShift = this.sequenceBits + this.workerIdBits;      // 22
    private final long sequenceMask = -1L ^ -1L << this.sequenceBits;                   // 毫秒内自增最大值: 4095(12位)
    private long lastTimestamp = -1L;

    /**
     * 构造器
     *
     * @param workerId 节点编号
     */
    IdWorker(long workerId) {
        if (workerId > this.maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", this.maxWorkerId));
        }
        this.workerId = workerId;
    }

    /**
     * 返回ID
     *
     * @return
     * @throws Exception
     */
    public synchronized long getId() {
        long timestamp = timeGen();

        // 如果上一个timestamp与新产生的相等，则sequence加一(0-4095循环); 对新的timestamp，sequence从0开始
        if (this.lastTimestamp == timestamp) {
            this.sequence = this.sequence + 1 & this.sequenceMask;      // 保证不会超过4095
            if (this.sequence == 0) {
                timestamp = this.tilNextMillis(this.lastTimestamp);     // 重新生成timestamp
            }
        } else {
            this.sequence = 0;
        }

        // 系统时间倒退情况
        if (timestamp < this.lastTimestamp) {
            throw new RuntimeException(String.format("clock moved backwards.Refusing to generate id for %d milliseconds", (this.lastTimestamp - timestamp)));
        }

        this.lastTimestamp = timestamp;

        // 时间戳占用高42位, 节点占用10位(可部署1023个节点), 自增数字占用低12位(每毫秒同1节点最多可生成4095个ID)
        return timestamp - this.epoch << this.timestampLeftShift | this.workerId << this.workerIdShift | this.sequence;
    }

    /**
     * 获得系统当前毫秒数
     */
    private static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一个毫秒的到来, 保证返回的毫秒数在参数lastTimestamp之后
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}