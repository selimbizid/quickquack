package de.tudortmund.webtech2.quickquack.rest.service;

import de.tudortmund.webtech2.quickquack.ejb.dao.RolePermissionDao;
import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.PermissionDto;
import de.tudortmund.webtech2.quickquack.ejb.dto.RoleDto;
import de.tudortmund.webtech2.quickquack.ejb.dtoservice.PermissionDtoService;
import de.tudortmund.webtech2.quickquack.ejb.dtoservice.RoleDtoService;
import de.tudortmund.webtech2.quickquack.ejb.entity.Permission;
import de.tudortmund.webtech2.quickquack.ejb.entity.Role;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import de.tudortmund.webtech2.quickquack.ejb.other.GlobalConstants;
import de.tudortmund.webtech2.quickquack.rest.config.PredefinedHttpCodes;
import de.tudortmund.webtech2.quickquack.rest.exception.QuackBoundaryException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
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
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;

/**
 * @author selimbizid
 */
@Path("/admin")
public class AdministrationREST {

    @Inject
    private UserDao uDao;
    @Inject
    private RolePermissionDao rpDao;
    @Inject
    private RoleDtoService roleService;
    @Inject
    private PermissionDtoService permService;

    @GET
    @Path("role")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions(GlobalConstants.PERM_MANAGE_ROLES)
    public List<RoleDto> getRoles() throws QuackBoundaryException {
        try {
            Collection<Role> roles = rpDao.getAllRoles();
            List<RoleDto> converted = new ArrayList<>();
            roles.forEach(r -> {
                try {
                    converted.add(roleService.convertEntityToDto(r));
                } catch (QuackServiceException e) {
                }
            });
            return converted;
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Rollen können nicht geladen werden");
        }
    }

    @GET
    @Path("permission")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresPermissions(GlobalConstants.PERM_MANAGE_ROLES)
    public List<PermissionDto> getPermissions() throws QuackBoundaryException {
        try {
            Collection<Permission> roles = rpDao.getAllPermissions();
            List<PermissionDto> converted = new ArrayList<>();
            roles.forEach(r -> {
                try {
                    converted.add(permService.convertEntityToDto(r));
                } catch (QuackServiceException e) {
                }
            });
            return converted;
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Berechtigungen können nicht geladen werden");
        }
    }

    @PUT
    @Path("role")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresRoles(GlobalConstants.ROLE_SUPERADMIN)
    public Response putRole(@QueryParam("name") String roleName) throws QuackBoundaryException {
        try {
            rpDao.addRole(roleName);
            return generateResponse("Rolle hinzugefügt");
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Rolle kann nicht hinzugefügt werden.");
        }
    }

    @PUT
    @Path("permission")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresRoles(GlobalConstants.ROLE_SUPERADMIN)
    public Response putPermission(@QueryParam("name") String permName) throws QuackBoundaryException {
        try {
            rpDao.addPermission(permName);
            return generateResponse("Berechtigung hinzugefügt");
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Berechtigung kann nicht hinzugefügt werden.");
        }
    }

    @DELETE
    @Path("role")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresRoles(GlobalConstants.ROLE_SUPERADMIN)
    public Response deleteRole(@QueryParam("name") String roleName) throws QuackBoundaryException {
        try {
            if (rpDao.removeRole(roleName)) {
                return generateResponse("Rolle gelöscht");
            } else {
                return generateResponse("Rolle existiert nicht", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (Throwable e) {
            throw new QuackBoundaryException("Rolle kann nicht gelöscht werden.");
        }
    }

    @DELETE
    @Path("permission")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresRoles(GlobalConstants.ROLE_SUPERADMIN)
    public Response deletePermission(@QueryParam("name") String permName) throws QuackBoundaryException {
        try {
            if (rpDao.removePermission(permName)) {
                return generateResponse("Berechtigung gelöscht");
            } else {
                return generateResponse("Berechtigung existiert nicht", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (Throwable e) {
            throw new QuackBoundaryException("Berechtigung kann nicht gelöscht werden.");
        }
    }

    @POST
    @Path("/assign")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresRoles(GlobalConstants.ROLE_SUPERADMIN)
    public Response assign(@FormParam("role") String roleName, @FormParam("permission") String permName, @FormParam("set") @DefaultValue("true") boolean set) throws QuackBoundaryException {
        try {
            if ((set ? rpDao.assign(roleName, permName) : rpDao.unassign(roleName, permName)) != null) {
                return generateResponse("Rolle aktualisiert");
            } else {
                return generateResponse("Rolle nicht aktualisiert", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.hasCustomMessage() ? e.getMessage() : "Rolle kann nicht aktualisiert werden");
        }
    }

    @POST
    @Path("/grantRole")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresPermissions(GlobalConstants.PERM_MANAGE_ROLES)
    public Response grant(@FormParam("userId") int id, @FormParam("roleName") String roleName, @FormParam("set") @DefaultValue("true") boolean set) throws QuackBoundaryException {
        try {
            rpDao.assignRoleToUser(id, roleName, set);
            return generateResponse("Benutzerrollen aktualisiert");
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException(e.hasCustomMessage() ? e.getMessage() : "Benutzerrollen kann nicht aktualisiert werden");
        }
    }

    @POST
    @Path("/ban")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresPermissions(GlobalConstants.PERM_MANAGE_USERS)
    public Response setActive(@FormParam("userId") int userId, @FormParam("active") @DefaultValue("false") boolean active) throws QuackBoundaryException {
        try {
            if (uDao.setAccountActivity(userId, active)) {
                return generateResponse("Konto aktualisiert");
            } else {
                return generateResponse("Konto nicht gefunden", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (AuthorizationException e) {
            throw new QuackBoundaryException("Sie sind nicht berechtigt, diese Funktionalität auszuführen.", PredefinedHttpCodes.NOT_ALLOWED);
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @GET
    @Path("/ban")
    @Produces(MediaType.TEXT_PLAIN)
    @RequiresPermissions(GlobalConstants.PERM_MANAGE_USERS)
    public Response setActiveGet(@QueryParam("userId") int userId, @QueryParam("active") @DefaultValue("false") boolean active) throws QuackBoundaryException {
        try {
            if (uDao.setAccountActivity(userId, active)) {
                return generateResponse("Konto aktualisiert");
            } else {
                return generateResponse("Konto nicht gefunden", PredefinedHttpCodes.INVALID_REQUEST);
            }
        } catch (AuthorizationException e) {
            throw new QuackBoundaryException("Sie sind nicht berechtigt, diese Funktionalität auszuführen.", PredefinedHttpCodes.NOT_ALLOWED);
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        } catch (Throwable t) {
            throw new QuackBoundaryException("Fehler aufgetreten");
        }
    }
    
    @GET
    @Path("/activity")
    @Produces(MediaType.APPLICATION_JSON)
    @RequiresRoles(value={"Admin", "SuperAdmin"}, logical=Logical.OR)
    public List<String> getActivity(@QueryParam("from") @DefaultValue("0") int offset, @QueryParam("count") @DefaultValue("100") int count) throws QuackBoundaryException {
        try {
            return new ArrayList<>(rpDao.getActivity(offset, count));
        } catch (QuackDataAccessException ex) {
            throw new QuackBoundaryException(ex.getMessage());
        }
    }
    
    @DELETE
    @Path("/reset")
    @RequiresPermissions(GlobalConstants.PERM_RESET_DATABASE)
    public Response resetDatabase() throws QuackBoundaryException {
        try {
            rpDao.resetDatabase();
            return generateResponse("Datenbank wiederhergestellt.");
        } catch (QuackDataAccessException e) {
            throw new QuackBoundaryException("Datenbank kann nicht reinitializiert werden.");
        }
    }
    
    private static Response generateResponse(Object info) {
        return Response.ok(info).build();
    }

    private static Response generateResponse(Object info, PredefinedHttpCodes code) {
        return Response.status(code.getCode()).entity(info).build();
    }
}
