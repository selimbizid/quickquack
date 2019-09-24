package de.tudortmund.webtech2.quickquack.rest.config;

import javax.ws.rs.core.Response;

/**
 * Convention over HTTP response codes that should be sent to the frond-end.
 * @author salim
 */
public enum PredefinedHttpCodes {
    VALID_REQUEST(Response.Status.OK.getStatusCode()),
    INVALID_REQUEST(Response.Status.BAD_REQUEST.getStatusCode()),
    BAN_LOGIN_REQUEST(Response.Status.FORBIDDEN.getStatusCode()),
    SERVER_ERROR(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()),
    NOT_ALLOWED(Response.Status.UNAUTHORIZED.getStatusCode());

    private int code;

    private PredefinedHttpCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
