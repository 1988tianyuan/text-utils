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

    @Override
    public void handleFile(File oldFile, File newFile) throws Exception {
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
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (isLayer(line)) {
                if (!codeQueue.isEmpty() && isZLine(codeQueue.getLast())) {
                    handleLayer(codeQueue, newLines);
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
                    handleLayer(codeQueue, newLines);
                }
            }
        }
    }
    
    private void handleLayer(LinkedList<String> codeQueue, List<String> newLines) {
        if (CollectionUtils.isNotEmpty(codeQueue) && codeQueue.size() > 2) {
            String zLine = codeQueue.pollLast();
            String zValue = parseZLine(zLine);
            String firstLine = codeQueue.pollLast();
            String endLine = codeQueue.pollFirst();
            String parsedFirstLine = parseG1Line(firstLine, zValue, false);
            String parsedEndLine = parseG1Line(endLine, zValue, false);
            String prefix = layerPrefixBuilder(parsedFirstLine);
            newLines.add(prefix);
            newLines.add(parsedFirstLine);
            handleLayerLines(codeQueue, newLines, zValue);
            newLines.add(parsedEndLine);
            newLines.add("");
        }
    }
    
    private void handleLayerLines(LinkedList<String> codeQueue, List<String> newLines, String zValue) {
        while (codeQueue.size() > 0) {
            String line = codeQueue.pollLast();
            String parsedLine = parseG1Line(line, zValue, true);
            newLines.add(parsedLine);
        }
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
    
    private boolean isLayer(String line) {
        return StringUtils.startsWith(line, PREFIX_LAYER);
    }
    
    private boolean isG1(String line) {
        return StringUtils.startsWith(line, PREFIX_G1);
    }
    
    private boolean prefixFilter(String line) {
        return isLayer(line) || isG1(line);
    }
    
    private String layerPrefixBuilder(String code) {
        double falseVELCP = (double) data.get("falseVELCP");
        double trueVELCP = (double) data.get("trueVELCP");
        return "$OUT[159]=FALSE\n"
            + "$VEL.CP=" + falseVELCP + "\n"
            + code + "\n"
            + "$OUT[159]=true\n"
            + "$VEL.CP="+ trueVELCP;
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
