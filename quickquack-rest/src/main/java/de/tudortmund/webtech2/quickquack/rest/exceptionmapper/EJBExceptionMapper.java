package de.tudortmund.webtech2.quickquack.rest.exceptionmapper;

import de.tudortmund.webtech2.quickquack.rest.config.PredefinedHttpCodes;
import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class EJBExceptionMapper implements ExceptionMapper<EJBException> {
    @Override
    public Response toResponse(EJBException exception) {
        return Response.status(PredefinedHttpCodes.SERVER_ERROR.getCode())
                .entity("Serverfehler")
                .build();
    }
}
