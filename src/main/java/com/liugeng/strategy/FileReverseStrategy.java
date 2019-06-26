package com.liugeng.strategy;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.RandomAccessFile;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.IOUtils.LINE_SEPARATOR;

@Slf4j
public class FileReverseStrategy extends FIleHandleStrategy {

    @Override
    public void handleFile(File oldFile, File newFile) throws Exception {
        try (RandomAccessFile rdFile = new RandomAccessFile(oldFile, "r")) {
            long len = rdFile.length();
            Preconditions.checkState(len > 0, "不要选择一个空文件！");
            long start = rdFile.getFilePointer();
            long nextEnd = start + len - 1;
            rdFile.seek(nextEnd);
            int pointer;
            String readStr = null;
            while (nextEnd > start) {
                pointer = rdFile.read();
                if (pointer == '\n' || pointer == '\r') {
                    readStr = writeFile(rdFile, readStr, newFile);
                    nextEnd--;
                }
                nextEnd--;
                if (nextEnd != -1) {
                    rdFile.seek(nextEnd);
                }
                if (nextEnd == 0) {
                    readStr = writeFile(rdFile, readStr, newFile);
                }
            }
        }
    }

    private String writeFile(RandomAccessFile rdFile, String readStr, File newFile) throws Exception {
        String readOrigin = rdFile.readLine();
        if (readOrigin != null) {
            readStr = new String(readOrigin.getBytes(ISO_8859_1), UTF_8) + LINE_SEPARATOR;
            FileUtils.write(newFile, readStr, UTF_8, true);
        }
        return readStr;
    }
}
