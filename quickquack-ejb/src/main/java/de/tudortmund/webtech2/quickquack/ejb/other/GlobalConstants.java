package de.tudortmund.webtech2.quickquack.ejb.other;

/**
 * Contains global constants such as role and permission names
 * @author selimbizid
 */
public class GlobalConstants {
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_SUPERADMIN = "SuperAdmin";
    public static final String PERM_MANAGE_USERS = "MANAGE_USERS";
    public static final String PERM_MANAGE_QUACKS = "MANAGE_QUACKS";
    public static final String PERM_MANAGE_ROLES = "MANAGE_ROLES";
    public static final String PERM_RESET_DATABASE = "RESET_DATABASE";
    public static final int SCOPE_PRIVATE = 0;
    public static final int SCOPE_FOLLOWERS = 1;
    public static final int SCOPE_PUBLIC = 2;
}
