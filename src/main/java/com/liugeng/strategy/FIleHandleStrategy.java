package com.liugeng.strategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

public abstract class FIleHandleStrategy {

    protected final Map<String, Object> data;

    public FIleHandleStrategy() {
        this.data = new HashMap<>();
    }

    public abstract void handleFile(File oldFile, File newFile) throws Exception ;

    public void setData(Map<String, Object> data) {
        if (MapUtils.isNotEmpty(data)) {
            this.data.putAll(data);
        }
    }
}
