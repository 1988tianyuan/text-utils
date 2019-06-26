package com.liugeng.model;

import com.liugeng.strategy.FileConvertType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandleData {

    private FileConvertType convertType;
    private Map<String, Object> data;
}
