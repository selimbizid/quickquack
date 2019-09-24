package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.dto.PermissionDto;
import de.tudortmund.webtech2.quickquack.ejb.entity.Permission;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PermissionDtoService implements DtoService<Permission, PermissionDto> {

    @Override
    public Permission convertDtoToEntity(PermissionDto dto) throws QuackServiceException {
        Permission p = new Permission(dto.getId());
        p.setIdentifier(dto.getIdentifier());
        return p;
    }

    @Override
    public PermissionDto convertEntityToDto(Permission entity) throws QuackServiceException {
        PermissionDto dto = new PermissionDto();
        dto.setId(entity.getId());
        dto.setIdentifier(entity.getIdentifier());
        return dto;
    }
    
}
