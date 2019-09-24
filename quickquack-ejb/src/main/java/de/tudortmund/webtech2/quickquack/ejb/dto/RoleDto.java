package de.tudortmund.webtech2.quickquack.ejb.dto;

import java.util.Collection;

public class RoleDto {
    private int id;
    private String name;
    private Collection<PermissionDto> permissions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PermissionDto> permissions) {
        this.permissions = permissions;
    }
}
