package com.liugeng.strategy;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.Charset;

public class RepetierConvertStrategy extends FIleHandleStrategy {

    @Override
    public void handleFile(File oldFile, File newFile) throws Exception {
        double falseVELCP = (double) data.get("falseVELCP");
        double trueVELCP = (double) data.get("trueVELCP");
        System.out.println("当前falseVELCP是" + falseVELCP);
        System.out.println("当前trueVELCPValue是" + trueVELCP);
        FileUtils.writeStringToFile(newFile, "当前falseVELCP是" + falseVELCP + "并且当前trueVELCPValue是" + trueVELCP, Charset.forName("UTF-8"));
    }
}
