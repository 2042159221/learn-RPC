package com.ming.rpc.utils;

import java.nio.charset.StandardCharsets;

/**
 * MurmurHash 算法工具类
 * <p>
 * MurmurHash 是一种高性能、低碰撞的非加密哈希算法，由 Austin Appleby 于 2008 年创建。
 * 它非常适合用于哈希表、布隆过滤器以及需要将数据均匀分布的场景。
 * <p>
 * 特点：
 * 1.  **高性能**：计算速度快，对 CPU 友好。
 * 2.  **良好的分布性**：能够产生非常均匀的哈希值，有效减少哈希碰撞。
 * 3.  **雪崩效应**：输入的微小变化会导致输出的巨大不同。
 * <p>
 * 此实现是 MurmurHash3 的 32 位版本。
 */
public final class MurmurHashUtil {

    /**
     * 工具类私有构造函数
     */
    private MurmurHashUtil() {
    }

    /**
     * 计算给定字节数组的 MurmurHash3 32-bit 哈希值。
     *
     * @param data 要计算哈希的字节数组
     * @return 32-bit 哈希值 (int)
     */
    public static int hash32(byte[] data) {
        return hash32(data, data.length, 0x9747b28c); // 默认种子值
    }
    
    /**
     * 计算给定字符串的 MurmurHash3 32-bit 哈希值。
     * 字符串会先被转换为 UTF-8 编码的字节数组。
     * 
     * @param text 要计算哈希的字符串
     * @return 32-bit 哈希值 (int)
     */
    public static int hash32(String text) {
        final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return hash32(bytes, bytes.length, 0x9747b28c);
    }

    /**
     * 计算给定数据的 MurmurHash3 32-bit 哈希值。
     *
     * @param data   要计算哈希的字节数组
     * @param length 要处理的字节长度
     * @param seed   哈希计算的种子值，可以用于防止哈希冲突攻击
     * @return 32-bit 哈希值 (int)
     */
    public static int hash32(byte[] data, int length, int seed) {
        // MurmurHash3 的魔法常数
        final int m = 0x5bd1e995;
        final int r = 24;

        // 初始化哈希值
        int h = seed ^ length;

        int len_4 = length >> 2;

        // 每次处理 4 个字节
        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = data[i_4 + 3];
            k = k << 8;
            k = k | (data[i_4 + 2] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 1] & 0xff);
            k = k << 8;
            k = k | (data[i_4 + 0] & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // 处理最后几个字节 (少于 4 个字节)
        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0) {
            if (left >= 3) {
                h ^= (int) data[length - 3] << 16;
            }
            if (left >= 2) {
                h ^= (int) data[length - 2] << 8;
            }
            if (left >= 1) {
                h ^= (int) data[length - 1];
            }

            h *= m;
        }

        // Finalization mix - 进行最后的混合，增强雪崩效应
        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }
} 