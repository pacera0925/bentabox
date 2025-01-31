package com.paulcera.bentabox.security.util;

public class UnitTestProps {

    public static String jwtSecretKey() {
        return "c29tZSBzZWNyZXQgdGhhdCBpcyBhdCBsZWFzdCAyNTYgYml0yyBsb25n";
    }

    public static long accessTokenExpiration() {
        return 86400000L;
    }

    public static long refreshTokenExpiration() {
        return 31536000000L;
    }

}
