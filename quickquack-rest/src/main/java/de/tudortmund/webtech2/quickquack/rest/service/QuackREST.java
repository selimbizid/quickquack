package de.tudortmund.webtech2.quickquack.rest.service;

import de.tudortmund.webtech2.quickquack.ejb.dao.QuackDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.CommentDto;
import de.tudortmund.webtech2.quickquack.ejb.dto.QuackDto;
import de.tudortmund.webtech2.quickquack.ejb.dtoservice.CommentDtoService;
import de.tudortmund.webtech2.quickquack.ejb.dtoservice.QuackDtoService;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import de.tudortmund.webtech2.quickquack.ejb.other.GlobalConstants;
import de.tudortmund.webtech2.quickquack.ejb.service.QuackService;
import de.tudortmund.webtech2.quickquack.rest.config.PredefinedHttpCodes;
import de.tudortmund.webtech2.quickquack.rest.exception.QuackBoundaryException;
import de.tudortmund.webtech2.quickquack.rest.interceptor.ClientDtoInterceptor;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.SecurityUtils;

@Path("/quack")
public class QuackREST {
    
    @Inject
    private QuackService service;
    @Inject
    private QuackDao dao;
    @Inject
    private QuackDtoService dtoService;
    @Inject
    private CommentDtoService commDtoService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<QuackDto> get(@QueryParam("from") @DefaultValue("0") int from, @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("loadComments") @DefaultValue("0") int loadComments, @QueryParam("since") @DefaultValue("0") int since) throws QuackBoundaryException {
        List<QuackDto> ls = new ArrayList<>();
        try {
            if (from < 0) {
                from = 0;
            }
            if (limit < 0) {
                limit = -limit;
            }
            if (limit > 10) {
                limit = 10;
            }
            if (SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS)) {
                dao.findAll(from, limit, since).forEach(e -> {
                    try {
                        ls.add(dtoService.convertEntityToDto(e, loadComments));
                    } catch (QuackServiceException ex) {
                    }
                });
            } else {
                service.get(SecurityUtils.getSubject().getPrincipal().toString(), from, limit, since).forEach(e -> {
                    try {
                        ls.add(dtoService.convertEntityToDto(e, loadComments > 5 ? 5 : loadComments)); // Load at most 5 comments
                    } catch (QuackServiceException ex) {
                    }
                });
            }
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return ls;
    }
    
    @GET
    @Path("/comment")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CommentDto> getComments(@QueryParam("quackId") int quackId, @QueryParam("from") @DefaultValue("0") int offset) throws QuackBoundaryException {
        try {
            List<CommentDto> result = new ArrayList<>();
            dao.getComments(quackId, offset, 5).forEach(c -> {
                try {
                    result.add(commDtoService.convertEntityToDto(c));
                } catch (QuackServiceException e) {
                }
            });
            return result;
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Kommentare können nicht geladen werden");
        }
    }
    
    @PUT
    @Path("/comment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Interceptors(ClientDtoInterceptor.class)
    public CommentDto putComment(CommentDto comment) throws QuackBoundaryException {
        try {
            return commDtoService.convertEntityToDto(service.post(commDtoService.convertDtoToEntity(comment)));
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        }
    }
    
    @DELETE
    @Path("/comment")
    @Interceptors(ClientDtoInterceptor.class)
    public Response deleteComment(@QueryParam("id") int id) throws QuackBoundaryException {
        try {
            return generateResponse(dao.deleteComment(SecurityUtils.getSubject().getPrincipal().toString(), id));
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.getMessage());
        }
    }
    
    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public List<QuackDto> search(@QueryParam("tag") String tag, @QueryParam("from") @DefaultValue("0") int from, @QueryParam("limit") @DefaultValue("10") int limit) throws QuackBoundaryException {
        List<QuackDto> ls = new ArrayList<>();
        try {
            if (from < 0) {
                from = 0;
            }
            if (limit < 0) {
                limit = -limit;
            }
            if (limit > 10) {
                limit = 10;
            }
            dao.findByHashtag(SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS) ? null : SecurityUtils.getSubject().getPrincipal().toString(), tag, from, limit).forEach(e -> {
                try {
                    ls.add(dtoService.convertEntityToDto(e));
                } catch (QuackServiceException ex) {
                }
            });
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return ls;
    }
    
    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public QuackDto getById(@QueryParam("id") int id) throws QuackBoundaryException {
        try {
            return dtoService.convertEntityToDto(service.getSingle(SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS) ? null : SecurityUtils.getSubject().getPrincipal().toString(), id), 10);
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public List<QuackDto> getByUserId(@QueryParam("id") int id, @QueryParam("from") @DefaultValue("0") int from, @QueryParam("since") @DefaultValue("0") int since) throws QuackBoundaryException {
        List<QuackDto> ls = new ArrayList<>();
        try {
            if (from < 0) {
                from = 0;
            }
            dao.findByUserId(SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS) ? null : SecurityUtils.getSubject().getPrincipal().toString(), id, from, 10, since)
                    .forEach(q -> {
                        try {
                            ls.add(dtoService.convertEntityToDto(q));
                        } catch (QuackServiceException e) {
                        }
                    });
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            t.addSuppressed(t);
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return ls;
    }
    
    @PUT
    @Path("/put")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Interceptors(ClientDtoInterceptor.class)
    public QuackDto put(QuackDto dto) throws QuackBoundaryException {
        try {
            return dtoService.convertEntityToDto(service.post(dtoService.convertDtoToEntity(dto)));
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @POST
    @Path("/edit")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Interceptors(ClientDtoInterceptor.class)
    public Response edit(QuackDto dto) throws QuackBoundaryException {
        try {
            if (dto != null && service.update(SecurityUtils.getSubject().getPrincipal().toString(), dto.getId(), dto.getContent())) {
                return generateResponse("Quack aktualisiert");
            } else {
                return generateResponse("Quack nicht aktualisiert", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public Response remove(@QueryParam("id") int quackId) throws QuackBoundaryException {
        try {
            if (service.remove(SecurityUtils.getSubject().isPermitted(GlobalConstants.PERM_MANAGE_QUACKS) ? null : SecurityUtils.getSubject().getPrincipal().toString(), quackId)) {
                return generateResponse("Quack gelöscht");
            } else {
                return generateResponse("Quack nicht gelöscht", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @POST
    @Path("/like")
    @Produces(MediaType.TEXT_PLAIN)
    public Response like(@FormParam("id") int quackId, @FormParam("yes") @DefaultValue("true") boolean like) throws QuackBoundaryException {
        try {
            service.setLike(SecurityUtils.getSubject().getPrincipal().toString(), quackId, like);
            return generateResponse("Quack liked");
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @POST
    @Path("/scope")
    @Produces(MediaType.TEXT_PLAIN)
    public Response changeScope(@FormParam("quackId") int quackId, @FormParam("scope") short newScope) throws QuackBoundaryException {
        try {
            if (service.changeScope(SecurityUtils.getSubject().getPrincipal().toString(), quackId, newScope)) {
                return generateResponse("Scope erfolgreich geändert!");
            } else {
                return generateResponse("Sie sind nicht berechtigt, den Scope dieses Quacks zu ändern!", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException("Scope kann nicht geändert werden");
        }
    }
    
    private static Response generateResponse(Object info) {
        return Response.ok(info).build();
    }
    
    private static Response generateResponse(Object info, PredefinedHttpCodes code) {
        return Response.status(code.getCode()).entity(info).build();
    }
}
