package com.usakar.jobs.demo.model;

import lombok.Data;

@Data
public class ServerResponse {

    private int statusCode;
    private Object data;
}
