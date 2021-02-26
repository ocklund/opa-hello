package com.example.opa.hello.voter;

import java.util.*;

import org.springframework.http.HttpEntity;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.client.RestTemplate;

public class OpaVoter implements AccessDecisionVoter<Object> {

    private final String opaUrl;

    public OpaVoter(String opaUrl) {
        this.opaUrl = opaUrl;
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    @Override
    public boolean supports(Class clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object obj, Collection<ConfigAttribute> attrs) {

        if (!(obj instanceof FilterInvocation)) {
            return ACCESS_ABSTAIN;
        }

        FilterInvocation filter = (FilterInvocation) obj;
        Map<String, String> headers = new HashMap<>();

        for (Enumeration<String> headerNames = filter.getRequest().getHeaderNames(); headerNames.hasMoreElements();) {
            String header = headerNames.nextElement();
            headers.put(header, filter.getRequest().getHeader(header));
        }

        String[] path = filter.getRequest().getRequestURI().replaceAll("^/|/$", "").split("/");

        Map<String, Object> input = new HashMap<>();
        input.put("method", filter.getRequest().getMethod());
        input.put("path", path);
        input.put("headers", headers);
        input.put("remoteipaddress", ((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress());

        System.out.println("*** REQUEST ***");
        printMap(input, "");

        RestTemplate client = new RestTemplate();
        HttpEntity<?> request = new HttpEntity<>(new OpaDataRequest(input));
        OpaDataResponse response = client.postForObject(this.opaUrl, request, OpaDataResponse.class);

        System.out.println("*** RESPONSE ***");
        System.out.println(response == null ? "null" : response.getResult());

        if (!Objects.requireNonNull(response).getResult()) {
            return ACCESS_DENIED;
        }

        return ACCESS_GRANTED;
    }

    private static void printMap(Map<String, Object> map, String parent) {
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                printMap((Map<String, Object>) value, key + ": ");
            } else if (value instanceof String[]) {
                System.out.println(parent + key + "[" + String.join(",", (String[]) value) + "]");
            } else {
                System.out.println(parent + key + " -> " + value);
            }
        });
    }
}