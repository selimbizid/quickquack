package de.tudortmund.webtech2.quickquack.rest.exceptionmapper;

import de.tudortmund.webtech2.quickquack.rest.exception.QuackBoundaryException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps boundary exceptions the its corresponding response codes and content.
 * @author salim
 */
@Provider
public class QuackBoundaryExceptionMapper implements ExceptionMapper<QuackBoundaryException> {
    @Override
    public Response toResponse(QuackBoundaryException exception) {
        return Response.status(exception.getHttpErrorCode())
                .entity(exception.getMessage())
                .build();
    }
}
