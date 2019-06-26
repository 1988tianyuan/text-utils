package com.liugeng.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HandleResult {

    private String result;
    private boolean success;
}
