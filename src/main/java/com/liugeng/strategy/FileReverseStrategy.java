package com.liugeng.strategy;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

@Slf4j
public class FileReverseStrategy extends FIleHandleStrategy {

    @Override
    public void handleFile(File oldFile, File newFile) throws Exception {
        List<String> lines = FileUtils.readLines(oldFile, Charset.forName("UTF-8"));
        LinkedList<String> stack = new LinkedList<>();
        for (String line : lines) {
            stack.addFirst(line);
        }
        while (stack.size() > 0) {
            FileUtils.write(newFile, stack.pollFirst() + "\n", Charset.forName("UTF-8"), true);
        }
    }
}
