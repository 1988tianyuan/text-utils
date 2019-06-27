package com.liugeng;

import static com.liugeng.strategy.FileConvertType.FILE_REVERSE;
import static com.liugeng.strategy.FileConvertType.REPIETER_CONVERT;
import static java.nio.charset.StandardCharsets.*;
import static org.apache.commons.io.IOUtils.*;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.liugeng.model.HandleResult;
import com.liugeng.strategy.FIleHandleStrategy;
import com.liugeng.strategy.FileConvertType;
import com.liugeng.strategy.FileReverseStrategy;
import com.liugeng.strategy.RepetierConvertStrategy;
import javafx.application.Platform;
import javafx.scene.control.Button;
import lombok.Builder;
import org.apache.commons.io.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileHandleTask implements Supplier<HandleResult> {

	private File oldFile;
	private File newFile;
	private Button openFileButton;
	private final FIleHandleStrategy strategy;
	private static final Map<FileConvertType, FIleHandleStrategy> strategyMap = new HashMap<>();

	static {
		strategyMap.put(FILE_REVERSE, new FileReverseStrategy());
		strategyMap.put(REPIETER_CONVERT, new RepetierConvertStrategy());
	}

	public FileHandleTask(FileConvertType convertType, Button openFileButton, File oldFile, File newFile, Map<String, Object> neededData) {
		this.openFileButton = openFileButton;
		this.oldFile = oldFile;
		this.newFile = newFile;
		strategyMap.get(convertType).setData(neededData);
		this.strategy = strategyMap.get(convertType);
	}

	@Override
	public HandleResult get() {
		setButtonDisable();
		String result;
		boolean isSuccess;
		try {
			strategy.handleFile(oldFile, newFile);
			result = "你的文件已经处理完了，地址位于：" + newFile.getPath();
			isSuccess = true;
		} catch (Exception e) {
			log.error("读取文件:[{}]失败！", oldFile.getPath(), e);
			result = "处理失败，报错信息：" + e.getMessage();
			isSuccess = false;
			newFile.delete();
		}
		return HandleResult.builder().result(result).success(isSuccess).build();
	}

	private void setButtonDisable() {
		Platform.runLater(() -> {
			openFileButton.setText("正在处理，请稍等...");
			openFileButton.setDisable(true);
		});
	}
}
