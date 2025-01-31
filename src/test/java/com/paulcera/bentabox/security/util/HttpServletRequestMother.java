package com.paulcera.bentabox.security.util;

import org.springframework.mock.web.MockHttpServletRequest;

public class HttpServletRequestMother {

    public static MockHttpServletRequest withBearerToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
