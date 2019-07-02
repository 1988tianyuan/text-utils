package com.liugeng.strategy;

import static com.liugeng.common.RepetierConstants.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RepetierConvertStrategy extends FIleHandleStrategy {

    private double falseVELCP;
    private double trueVELCP;

    @Override
    public void handleFile(File oldFile, File newFile) throws Exception {
        falseVELCP = (double) data.get("falseVELCP");
        trueVELCP = (double) data.get("trueVELCP");
        try(BufferedReader reader = new BufferedReader(new FileReader(oldFile))) {
            List<String> filteredLines = reader.lines().filter(this::prefixFilter).collect(Collectors.toList());
            List<String> newLines = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(filteredLines)) {
                handle(filteredLines, newLines);
            }
            FileUtils.writeLines(newFile, Charset.forName("UTF-8").name(), newLines);
        }
    }
    
    private void handle(List<String> filteredLines, List<String> newLines) {
        LinkedList<String> zLineQueue = new LinkedList<>();
        LinkedList<String> codeQueue = new LinkedList<>();
        ListIterator<String> iterator = filteredLines.listIterator();
        int layerCount = 0;
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (isLayer(line)) {
                if (!codeQueue.isEmpty() && isZLine(codeQueue.getLast())) {
                    layerCount++;
                    handleLayer(codeQueue, newLines, layerCount);
                }
                codeQueue.clear();
                String zLine = zLineQueue.pollFirst();
                if (zLine != null) {
                    codeQueue.addFirst(zLine);
                } else {
                    throw new IllegalStateException("layer前面没有Z坐标代码，无法处理！");
                }
            } else if (isZLine(line)) {
                zLineQueue.addFirst(line);
            } else {
                codeQueue.addFirst(line);
            }
            if (!iterator.hasNext()) {
                if (!codeQueue.isEmpty() && isZLine(codeQueue.getLast())) {
                    layerCount++;
                    handleLayer(codeQueue, newLines, layerCount);
                }
            }
        }
    }
    
    private void handleLayer(LinkedList<String> codeQueue, List<String> newLines, int layerCount) {
        if (CollectionUtils.isNotEmpty(codeQueue) && codeQueue.size() > 2) {
            String zLine = codeQueue.pollLast();
            String zValue = parseZLine(zLine);
            String firstLine = codeQueue.pollLast();
            String endLine = codeQueue.pollFirst();
            String parsedFirstLine = parseG1Line(firstLine, zValue, false);
            String parsedEndLine = parseG1Line(endLine, zValue, false);
            String prefix = layerPrefixBuilder(parsedFirstLine, layerCount);
            newLines.add(prefix);
            newLines.add(parsedFirstLine);
            handleLayerLines(codeQueue, newLines, zValue);
            newLines.add(parsedEndLine);
            newLines.add("");
            newLines.add("");
            newLines.add("");
        }
    }
    
    private void handleLayerLines(LinkedList<String> codeQueue, List<String> newLines, String zValue) {
        LinkedList<String> tmpF7800 = new LinkedList<>();
        while (codeQueue.size() > 0) {
            String line = codeQueue.pollLast();
            if (isF7800Line(line)) {
                tmpF7800.addFirst(line);
            } else {
                if (!tmpF7800.isEmpty()) {
                    String f7800Line = tmpF7800.pollFirst();
                    handleF7800Line(newLines, f7800Line, zValue);
                    tmpF7800.clear();
                }
                String parsedLine = parseG1Line(line, zValue, true);
                newLines.add(parsedLine);
            }
        }
    }

    private void handleF7800Line(List<String> newLines, String f7800Line, String zValue) {
        String parsedF7800Line = parseG1Line(f7800Line, zValue, false);
        newLines.add(OUT_159_FALSE);
        newLines.add(parsedF7800Line);
        newLines.add(OUT_159_TRUE);
        newLines.add(parsedF7800Line);
    }

    private String parseZLine(String zLine) {
        return StringUtils.substringBetween(zLine, "Z", " ");
    }
    
    private String parseG1Line(String g1Line, String zValue, boolean needCVEL) {
        String xValue = null;
        String yValue = null;
        String[] g1LineStrs = StringUtils.split(g1Line, " ");
        for (String g1LineStr : g1LineStrs) {
            if (StringUtils.isBlank(g1LineStr)) {
                continue;
            }
            if (StringUtils.startsWith(g1LineStr, "X")) {
                xValue = StringUtils.substringAfter(g1LineStr, "X");
            } else if (StringUtils.startsWith(g1LineStr, "Y")) {
                yValue = StringUtils.substringAfter(g1LineStr, "Y");
            }
        }
        return repetierCodeBuilder(xValue, yValue, zValue, needCVEL);
    }
    
    private boolean isZLine(String line) {
        boolean isZLine = StringUtils.startsWith(line, "G1 Z");
        return StringUtils.isNotBlank(line) && isZLine;
    }

    private boolean isF7800Line(String line) {
        return StringUtils.contains(line, F7800);
    }
    
    private boolean isLayer(String line) {
        return StringUtils.startsWith(line, PREFIX_LAYER);
    }
    
    private boolean isG1(String line) {
        boolean beginWithG1 = StringUtils.startsWith(line, PREFIX_G1);
        boolean notHasX = !StringUtils.contains(line, "X");
        boolean notHasY = !StringUtils.contains(line, "Y");
        boolean notHasZ = !StringUtils.contains(line, "Z");
        boolean notValidG1 = notHasX && notHasY && notHasZ;
        return beginWithG1 && !notValidG1;
    }
    
    private boolean prefixFilter(String line) {
        return isLayer(line) || isG1(line);
    }
    
    private String layerPrefixBuilder(String code, int layerCount) {
        return OUT_159_FALSE + "\r\n"
            + ";" + PREFIX_LAYER + "=" + layerCount + "\r\n"
            + VEL_CP + "="+ falseVELCP + "\r\n"
            + code + "\r\n"
            + OUT_159_TRUE + "\r\n"
            + VEL_CP + "="+ trueVELCP;
    }
    
    private String repetierCodeBuilder(String xValue, String yValue, String zValue, boolean needCVEL) {
        StringBuilder builder = new StringBuilder();
        builder.append(LIN + "{");
        if (StringUtils.isNotBlank(xValue)) {
            String xStr = "X" + SPACE + xValue;
            builder.append(xStr).append(",");
        }
        if (StringUtils.isNotBlank(xValue)) {
            String yStr = "Y" + SPACE + yValue;
            builder.append(yStr).append(",");
        }
        builder.append("Z").append(SPACE).append(zValue).append("}");
        if (needCVEL) {
            return builder.append(C_VEL).toString();
        } else {
            return builder.toString();
        }
    }
}
