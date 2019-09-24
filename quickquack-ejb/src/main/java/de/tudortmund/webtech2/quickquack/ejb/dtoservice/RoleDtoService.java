package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.dto.PermissionDto;
import de.tudortmund.webtech2.quickquack.ejb.dto.RoleDto;
import de.tudortmund.webtech2.quickquack.ejb.entity.Permission;
import de.tudortmund.webtech2.quickquack.ejb.entity.Role;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RoleDtoService implements DtoService<Role, RoleDto> {

    @Inject
    private PermissionDtoService permService;

    @Override
    public Role convertDtoToEntity(RoleDto dto) throws QuackServiceException {
        Role r = new Role(dto.getId());
        r.setName(dto.getName());
        if (dto.getPermissions() != null) {
            Collection<Permission> perms = new ArrayList<>();
            dto.getPermissions().forEach(p -> {
                try {
                    perms.add(permService.convertDtoToEntity(p));
                } catch (QuackServiceException e) { }
            });
            r.setPermissions(perms);
        }
        return r;
    }

    @Override
    public RoleDto convertEntityToDto(Role entity) throws QuackServiceException {
        RoleDto dto = new RoleDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        if (entity.getPermissions() != null) {
            Collection<PermissionDto> perms = new ArrayList<>();
            entity.getPermissions().forEach(p -> {
                try {
                    perms.add(permService.convertEntityToDto(p));
                } catch (QuackServiceException e) { }
            });
            dto.setPermissions(perms);
        }
        return dto;
    }
}
