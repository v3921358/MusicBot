/*
 * Copyright 2022 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * 除非符合授權條款，否則不得使用此檔案。
 * 你可以從以下網址取得授權：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或書面同意，否則依授權提供的程式碼是「原樣提供」，不附任何保證。
 */
package com.jagrosh.jmusicbot.settings;

import com.jagrosh.jmusicbot.queue.AbstractQueue;
import com.jagrosh.jmusicbot.queue.FairQueue;
import com.jagrosh.jmusicbot.queue.LinearQueue;
import com.jagrosh.jmusicbot.queue.Queueable;
import com.jagrosh.jmusicbot.queue.QueueSupplier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Wolfgang Schwendtbauer
 */
public enum QueueType {
    LINEAR("\u23E9", "線性", LinearQueue::new),     // ⏩
    FAIR("\uD83D\uDD22", "公平", FairQueue::new);   // 🔢

    // 使用者友善名稱（中文顯示用）
    private final String userFriendlyName;
    // 對應表情符號
    private final String emoji;
    // 隊列供應器
    private final QueueSupplier supplier;

    QueueType(final String emoji, final String userFriendlyName, QueueSupplier supplier) {
        this.userFriendlyName = userFriendlyName;
        this.emoji = emoji;
        this.supplier = supplier;
    }

    // 取得所有隊列型態名稱（小寫）
    public static List<String> getNames() {
        return Arrays.stream(QueueType.values())
                .map(type -> type.name().toLowerCase())
                .collect(Collectors.toList());
    }

    // 建立新隊列實例，可傳入之前的隊列
    public <T extends Queueable> AbstractQueue<T> createInstance(AbstractQueue<T> previous) {
        return supplier.apply(previous);
    }

    // 取得使用者友善名稱
    public String getUserFriendlyName() {
        return userFriendlyName;
    }

    // 取得隊列對應的表情符號
    public String getEmoji() {
        return emoji;
    }
}
