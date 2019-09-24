package de.tudortmund.webtech2.quickquack.ejb.dao;

import de.tudortmund.webtech2.quickquack.ejb.entity.Permission;
import de.tudortmund.webtech2.quickquack.ejb.entity.Role;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.enterprise.context.Dependent;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Data access object that handles the roles and permissions. Provided by the
 * EJB container.
 */
@Dependent
public class RolePermissionDao {

    @PersistenceContext
    private EntityManager em;

    /**
     * @return List of available roles of the system
     * @throws QuackDataAccessException
     */
    public List<Role> getAllRoles() throws QuackDataAccessException {
        TypedQuery<Role> q = em.createNamedQuery("Role.findAll", Role.class);
        try {
            return q.getResultList();
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * @return List of available permissions of the system
     * @throws QuackDataAccessException
     */
    public List<Permission> getAllPermissions() throws QuackDataAccessException {
        TypedQuery<Permission> q = em.createNamedQuery("Permission.findAll", Permission.class);
        try {
            return q.getResultList();
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Adds a new role with the specified name to the system
     *
     * @param roleName Name of the role to be added.
     * @return The generated Role object
     * @throws QuackDataAccessException If a role with the same name exists or
     * whatever error occurs.
     * @see RolePermissionDao#removeRole(java.lang.String)
     */
    public Role addRole(String roleName) throws QuackDataAccessException {
        try {
            Role r = em.merge(new Role(roleName));
            protocol("Role " + roleName + " added");
            return r;
        } catch (ConstraintViolationException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Rolle existiert bereits!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Adds a new permission with the specified name to the system
     *
     * @param name Name of the permission to be added.
     * @return The generated Permission object
     * @throws QuackDataAccessException If a permission with the same name
     * exists or whatever error occurs.
     * @see RolePermissionDao#removePermission(java.lang.String)
     */
    public Permission addPermission(String name) throws QuackDataAccessException {
        try {
            Permission p = em.merge(new Permission(name));
            protocol("Permission " + name + " added");
            return p;
        } catch (ConstraintViolationException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Rolle existiert bereits!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Assigns the given permission to the given role.
     *
     * @param roleName Name of the role.
     * @param permissionName Name of the permission
     * @return The refreshed Role object
     * @throws QuackDataAccessException If whatever error occurs.
     * @see RolePermissionDao#unassign(java.lang.String, java.lang.String)
     */
    public Role assign(String roleName, String permissionName) throws QuackDataAccessException {
        try {
            Role r = em.createNamedQuery("Role.findByName", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            Permission p = em.createNamedQuery("Permission.findByIdentifier", Permission.class)
                    .setParameter("identifier", permissionName)
                    .getSingleResult();
            r.getPermissions().add(p);
            protocol("Permission " + permissionName + " assigned to role " + roleName);
            return r;
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Role oder Permission nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Grants a role to a given user.
     *
     * @param uid ID of the user that should get the new role.
     * @param roleName Name of the role to be assigned.
     * @param set true if the role should be set, or false if it should be unset
     * @throws QuackDataAccessException
     */
    public void assignRoleToUser(int uid, String roleName, boolean set) throws QuackDataAccessException {
        try {
            Role r = em.createNamedQuery("Role.findByName", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            User u = em.createNamedQuery("User.findById", User.class)
                    .setParameter("id", uid)
                    .getSingleResult();
            if (set) {
                if (!u.getRoles().contains(r)) {
                    u.getRoles().add(r);
                    protocol("Role " + roleName + " assigned to user #" + uid);
                }
            } else {
                u.getRoles().remove(r);
                protocol("Role " + roleName + " unassigned from user #" + uid);
            }
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Role oder Permission nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Unassigns the given permission from the given role.
     *
     * @param roleName Name of the role.
     * @param permissionName Name of the permission
     * @return The refreshed Role object
     * @throws QuackDataAccessException If whatever error occurs.
     * @see RolePermissionDao#assign(java.lang.String, java.lang.String)
     */
    public Role unassign(String roleName, String permissionName) throws QuackDataAccessException {
        try {
            Role r = em.createNamedQuery("Role.findByName", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            Permission p = em.createNamedQuery("Permission.findByIdentifier", Permission.class)
                    .setParameter("identifier", permissionName)
                    .getSingleResult();
            r.getPermissions().remove(p);
            protocol("Permission " + permissionName + " unassigned from role " + roleName);
            return r;
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Role oder Permission nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Removes a role from the system
     *
     * @param roleName Name of the role to be removed
     * @return true if role has been removed, otherwise false (no role with the
     * given name exists)
     * @see RolePermissionDao#addRole(java.lang.String)
     */
    public boolean removeRole(String roleName) {
        CriteriaBuilder b = em.getCriteriaBuilder();
        CriteriaDelete<Role> del = b.createCriteriaDelete(Role.class);
        Root<Role> root = del.from(Role.class);
        del.where(b.equal(root.get("name"), roleName));
        if (em.createQuery(del).executeUpdate() > 0) {
            protocol("Role " + roleName + " removed");
            return true;
        }
        return false;
    }

    /**
     * Removes a permission from the system
     *
     * @param permName Name of the permission to be removed
     * @return true if permission has been removed, otherwise false (no
     * permission with the given name exists)
     * @see RolePermissionDao#addPermission(java.lang.String)
     */
    public boolean removePermission(String permName) {
        CriteriaBuilder b = em.getCriteriaBuilder();
        CriteriaDelete<Permission> del = b.createCriteriaDelete(Permission.class);
        Root<Permission> root = del.from(Permission.class);
        del.where(b.equal(root.get("identifier"), permName));
        if (em.createQuery(del).executeUpdate() > 0) {
            protocol("Permission " + permName + " removed");
            return true;
        }
        return false;
    }

    /**
     * Returns the list of permissions of the given role
     *
     * @param roleName Name of the role
     * @return List of the permissions
     * @throws QuackDataAccessException
     */
    public Collection<Permission> getPermissions(String roleName) throws QuackDataAccessException {
        try {
            Role r = em.createNamedQuery("Role.findByName", Role.class).getSingleResult();
            return r.getPermissions();
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Role nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * List of the roles that has the given permission name
     *
     * @param permissionName Name of the permission
     * @return List of the found roles
     * @throws QuackDataAccessException
     */
    public Collection<Role> getRoles(String permissionName) throws QuackDataAccessException {
        try {
            Permission p = em.createNamedQuery("Permission.findByIdentifier", Permission.class).getSingleResult();
            return p.getRoles();
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Permission nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * List of the roles that has the given permission name
     *
     * @param offset Start offset
     * @param count Count of lines to be fetched
     * @return List of the found roles
     * @throws QuackDataAccessException
     */
    public Collection<String> getActivity(int offset, int count) throws QuackDataAccessException {
        try {
            List<Object[]> result = em.createNativeQuery("SELECT info, time FROM log ORDER BY time DESC LIMIT " + count + " OFFSET " + offset).<String>getResultList();
            List<String> formatted = new ArrayList<>();
            result.forEach((row) -> {
                formatted.add(String.format("%s: %s", row[1], row[0]));
            });
            return formatted;
        } catch (NoResultException e) {
            throw new QuackDataAccessException(Role.class, e.getCause(), "Permission nicht gefunden!");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Role.class, t);
        }
    }

    /**
     * Clears all data from database. SuperAdmin account and it's roles are
     * therefore kept.
     *
     * @throws QuackDataAccessException
     */
    public void resetDatabase() throws QuackDataAccessException {
        try {
            final String[][] tables = new String[][] {{"follow", "", "1"}, {"quacktag", "", "1"}, {"quacklike", "", "1"},
                {"hashtag", "", "1"}, {"comment", "", "1"}, {"quack", "", "1"},
                {"userrole", "user_id != 1 OR role_id != 1", "1"}, {"user", "id != 1", "2"}};
            for (String table[]: tables) {
                em.createNativeQuery("DELETE FROM " + table[0] + (table[1].isEmpty() ? table[1] : " WHERE " + table[1]))
                        .executeUpdate(); // Reset content
                em.createNativeQuery("ALTER TABLE " + table[0] + " AUTO_INCREMENT = " + table[2])
                        .executeUpdate(); // Reset primary key counters
            }
            protocol("Database reinitialized.");
        } catch (Throwable t) {
            throw new QuackDataAccessException(Permission.class, t);
        }
    }

    /**
     * Protocol activities and store them into database
     *
     * @param infos Activities to protocol
     * @return
     */
    private int protocol(String... infos) {
        if (infos != null && infos.length > 0) {
            StringBuilder baseSql = new StringBuilder("INSERT INTO log (info) VALUES");
            for (int i = 0; i < infos.length; i++) {
                if (i > 0) {
                    baseSql.append(",");
                }
                baseSql.append(" (:info")
                        .append(i)
                        .append(")");
            }
            Query q = em.createNativeQuery(baseSql.toString());
            for (int i = 0; i < infos.length; i++) {
                q.setParameter("info" + i, infos[i]);
            }
            return q.executeUpdate();
        }
        return 0;
    }
}
