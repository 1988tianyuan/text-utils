package com.liugeng.strategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class FIleHandleStrategy {

    protected final Map<String, Object> data;

    public FIleHandleStrategy() {
        this.data = new HashMap<>();
    }

    public abstract void handleFile(File oldFile, File newFile) throws Exception ;

    public void setData(Map<String, Object> data) {
        this.data.putAll(data);
    }
}
