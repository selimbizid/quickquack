package de.tudortmund.webtech2.quickquack.rest.service;

import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.UserDto;
import de.tudortmund.webtech2.quickquack.ejb.dtoservice.UserDtoService;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import de.tudortmund.webtech2.quickquack.rest.exception.QuackBoundaryException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.shiro.SecurityUtils;
import de.tudortmund.webtech2.quickquack.ejb.service.UserService;
import de.tudortmund.webtech2.quickquack.rest.config.PredefinedHttpCodes;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;
import org.apache.shiro.codec.Base64;

@Path("/user")
public class UserREST {

    @Inject
    private UserDao dao;
    @Inject
    private UserDtoService dtoService;
    @Inject
    private UserService userService;

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDto get(@QueryParam("id") int id) throws QuackBoundaryException {
        try {
            User x = dao.findById(id);
            return dtoService.convertEntityToDto(x);
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Benutzer kann nicht gefunden werden");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }

    @GET
    @Path("/find")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> find(@QueryParam("containing") String containing, @QueryParam("from") @DefaultValue("0") int offset) throws QuackBoundaryException {
        try {
            List<User> ls = dao.findByAliasOrEmail(containing, offset, 10);
            final List<UserDto> result = new ArrayList<>();
            ls.forEach(u -> {
                try {
                    result.add(dtoService.convertEntityToDto(u));
                } catch (QuackServiceException e) {
                }
            });
            return result;
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Benutzer kann nicht gefunden werden");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserDto> getAll() throws QuackBoundaryException {
        final List<UserDto> dtos = new ArrayList<>();
        try {
            dao.findAll().forEach(u -> {
                try {
                    dtos.add(dtoService.convertEntityToDto(u));
                } catch (QuackServiceException e) {
                }
            });
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Benutzerliste kann nicht geladen werden");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return dtos;
    }

    @GET
    @Path("/following")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<UserDto> getFollowing() throws QuackBoundaryException {
        final List<UserDto> dtos = new ArrayList<>();
        try {
            Collection<User> following = dao.getFollowing(SecurityUtils.getSubject().getPrincipal().toString());
            if (following != null) {
                following.forEach(e -> {
                    try {
                        dtos.add(dtoService.convertEntityToDto(e));
                    } catch (QuackServiceException ex) {
                    }
                });
            }
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Benutzerliste kann nicht geladen werden");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return dtos;
    }

    @GET
    @Path("/followed")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<UserDto> getFollowed() throws QuackBoundaryException {
        final List<UserDto> dtos = new ArrayList<>();
        try {
            Collection<User> followed = dao.getFollowed(SecurityUtils.getSubject().getPrincipal().toString());
            if (followed != null) {
                followed.forEach(e -> {
                    try {
                        dtos.add(dtoService.convertEntityToDto(e));
                    } catch (QuackServiceException ex) {
                    }
                });
            }
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Benutzerliste kann nicht geladen werden");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
        return dtos;
    }

    @POST
    @Path("/block")
    @Produces(MediaType.TEXT_PLAIN)
    public Response block(@FormParam("id") int userId, @FormParam("block") boolean block) throws QuackBoundaryException {
        try {
            dao.block(SecurityUtils.getSubject().getPrincipal().toString(), userId, block);
            return generateResponse("User " + (block ? "" : "de") + "blocked");
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }

    @POST
    @Path("/register")
    @Produces(MediaType.TEXT_PLAIN)
    public Response register(@FormParam("email") String email, @FormParam("password") String plainPassword) throws QuackBoundaryException {
        try {
            User u = userService.registerUser(email, plainPassword);
            if (u != null) {
                return generateResponse("Registrierung erfolgreich.");
            } else {
                return generateResponse("Benutzer kann nicht registriert werden", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @POST
    @Path("/registerJson")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(UserDto dto) throws QuackBoundaryException {
        if (dto != null) {
            return register(dto.getEmail(), dto.getPassword());
        } else throw new QuackBoundaryException("eMail und Passwort fehlen");
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDto login(@FormParam("email") String email, @FormParam("password") String pass) throws QuackBoundaryException {
        try {
            User u = userService.tryLogin(SecurityUtils.getSubject(), email, pass);
            if (u != null) {
                UserDto dto = dtoService.convertEntityToDto(u);
                dto.setAuthToken(getAuthToken(email, pass));
                return dto;
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage(), PredefinedHttpCodes.BAN_LOGIN_REQUEST);
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten.");
        }
        throw new QuackBoundaryException("Login fehlgeschlagen.", PredefinedHttpCodes.BAN_LOGIN_REQUEST);
    }
    
    @POST
    @Path("/loginJson")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public UserDto login(UserDto dto) throws QuackBoundaryException {
        if (dto != null) {
            return login(dto.getEmail(), dto.getPassword());
        }  else throw new QuackBoundaryException("eMail und Passwort fehlen");
    }

    private String getAuthToken(String username, String password) {
        String auth = username + ":" + password;
        byte[] encoded = Base64.encode(auth.getBytes());
        return String.format("Basic %s", new String(encoded));
    }

    @POST
    @Path("/changePassword")
    @Produces(MediaType.TEXT_PLAIN)
    public Response changePassword(@FormParam("old") String oldPass, @FormParam("new") String newPass) throws QuackBoundaryException {
        try {
            String principal = SecurityUtils.getSubject().getPrincipal().toString();
            if (userService.changePassword(principal, oldPass, newPass)) {
                return generateResponse(getAuthToken(principal, newPass));
            } else {
                return generateResponse("Passwort konnte nicht geändert werden.", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException(e.getMessage());
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }

    @POST
    @Path("/changeAlias")
    @Produces(MediaType.TEXT_PLAIN)
    public Response changeAlias(@FormParam("alias") String newAlias) throws QuackBoundaryException {
        try {
            if (userService.changeAlias(SecurityUtils.getSubject().getPrincipal().toString(), newAlias)) {
                return generateResponse("Benutzername erfolgreich geändert.");
            } else {
                return generateResponse("Benutzername kann nicht geändert werden", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException("Der gewählte Benutzername existiert bereits.");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }

    @POST
    @Path("/follow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response toggleFollow(@FormParam("id") int toBeFollowed) throws QuackBoundaryException {
        try {
            if (userService.follow(SecurityUtils.getSubject().getPrincipal().toString(), toBeFollowed)) {
                return generateResponse("Sie folgen nun dem Benutzer.");
            } else {
                return generateResponse("Sie folgen dem Benutzer nicht mehr");
            }
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException("Fehler beim folgen des benutzers");
        }
    }
    
    @GET
    @Path("/follow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response follow(@QueryParam("id") int toBeFollowed) throws QuackBoundaryException {
        try {
            userService.follow(SecurityUtils.getSubject().getPrincipal().toString(), toBeFollowed, true);
            return generateResponse("Sie folgen nun dem Benutzer.");
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException("Fehler beim folgen des benutzers");
        }
    }
    
    @GET
    @Path("/unfollow")
    @Produces(MediaType.TEXT_PLAIN)
    public Response unfollow(@QueryParam("id") int toBeFollowed) throws QuackBoundaryException {
        try {
            userService.follow(SecurityUtils.getSubject().getPrincipal().toString(), toBeFollowed, false);
            return generateResponse("Sie folgen nun dem Benutzer nicht mehr.");
        } catch (QuackServiceException e) {
            throw new QuackBoundaryException("Fehler beim folgen des benutzers");
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.TEXT_PLAIN)
    public void logout() {
        SecurityUtils.getSubject().logout();
    }

    private static Response generateResponse(Object info) {
        return Response.ok(info).build();
    }

    private static Response generateResponse(Object info, PredefinedHttpCodes code) {
        return Response.status(code.getCode()).entity(info).build();
    }
}
