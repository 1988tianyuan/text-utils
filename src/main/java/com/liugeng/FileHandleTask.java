package com.liugeng;

import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.IOUtils.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileHandleTask implements Supplier<FileHandleTask.HandleResult> {
	
	private File file;
	private File newFile;
	public FileHandleTask(File file, File newFile) {
		this.file = file;
		this.newFile = newFile;
	}
	@Override
	public HandleResult get() {
		String result;
		boolean isSuccess;
		try (RandomAccessFile rdFile = new RandomAccessFile(file, "r")) {
			long len = rdFile.length();
			long start = rdFile.getFilePointer();
			long nextEnd = start + len - 1;
			rdFile.seek(nextEnd);
			int pointer;
			String readStr = null;
			while (nextEnd > start) {
				pointer = rdFile.read();
				if (pointer == '\n' || pointer == '\r') {
					readStr = writeFile(rdFile, readStr);
					nextEnd--;
				}
				nextEnd--;
				if(nextEnd != -1) {
					rdFile.seek(nextEnd);
				}
				if (nextEnd == 0) {
					readStr = writeFile(rdFile, readStr);
				}
			}
			result = "你的文件已经处理完了，地址位于：" + newFile.getPath();
			isSuccess = true;
		} catch (Exception e) {
			log.error("读取文件:[{}]失败！", file.getPath(), e);
			result = "处理失败，报错信息：" + e.getMessage();
			isSuccess = false;
		}
		return HandleResult.builder().result(result).success(isSuccess).build();
	}
	
	private String writeFile(RandomAccessFile rdFile, String readStr) throws Exception {
		String readOrigin = rdFile.readLine();
		if (readOrigin != null) {
			readStr = new String(readOrigin.getBytes(ISO_8859_1), UTF_8) + LINE_SEPARATOR;
			FileUtils.write(newFile, readStr, UTF_8, true);
		}
		return readStr;
	}
	@Data
	@Builder
	static class HandleResult {
		private String result;
		private boolean success;
	}
}
