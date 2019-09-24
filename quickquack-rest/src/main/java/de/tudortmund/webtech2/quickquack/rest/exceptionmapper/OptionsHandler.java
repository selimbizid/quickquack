/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.tudortmund.webtech2.quickquack.rest.exceptionmapper;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.jboss.resteasy.spi.DefaultOptionsMethodException;

/**
 *
 * @author selimbizid
 */
@Provider
public class OptionsHandler implements ExceptionMapper<DefaultOptionsMethodException> {

    private static final String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_ANYONE = "*";

    @Context
    private HttpHeaders httpHeaders;

    @Override
    public Response toResponse(DefaultOptionsMethodException exception) {

        final ResponseBuilder response = Response.ok();
        String requestHeaders = httpHeaders.getHeaderString(ACCESS_CONTROL_REQUEST_HEADERS);
        String requestMethods = httpHeaders.getHeaderString(ACCESS_CONTROL_REQUEST_METHOD);
        if (requestHeaders != null) {
            response.header(ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders);
        }
        if (requestMethods != null) {
            response.header(ACCESS_CONTROL_ALLOW_METHODS, requestMethods);
        }
        // TODO: development only, too permissive
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN, ACCESS_CONTROL_ALLOW_ORIGIN_ANYONE);
        
        return response.build();
    }
}
