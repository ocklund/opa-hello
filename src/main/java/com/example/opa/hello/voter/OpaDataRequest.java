package com.example.opa.hello.voter;

import java.util.Map;

public class OpaDataRequest {

    Map<String, Object> input;

    public OpaDataRequest(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getInput() {
        return this.input;
    }
}