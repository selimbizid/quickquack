package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.UserDto;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;

@ApplicationScoped
public class UserDtoService implements DtoService<User, UserDto> {

    @Inject
    private UserDao userDao;

    @Override
    public UserDto convertEntityToDto(User user) throws QuackServiceException {
        UserDto u = new UserDto();
        int id = user.getId();
        u.setId(id);
        u.setEmail(user.getEmail());
        u.setAlias(user.getAlias() != null ? user.getAlias() : user.getEmail());
        u.setActive(user.getAccountActive() == 1);
        if (user.getLastestActivity() != null) {
            u.setLastestActivity(user.getLastestActivity().toString("dd.MM.yyyy HH:mm"));
        }
        try {
            u.setFollowed(user.getFollowedUsers().size());
            u.setFollowing(user.getFollowingUsers().size());
            if (SecurityUtils.getSubject().isAuthenticated()) {
                User auth = userDao.findByEmail(SecurityUtils.getSubject().getPrincipal().toString());
                u.setFollowedFromSelf(auth.getFollowedUsers().contains(user));
                u.setBlockedFromSelf(auth.getBlockedUsers().contains(user));
            }
            u.setPostsCount(user.getQuacks().size());
            final Set<String> perms = new HashSet<>(), roles = new HashSet<>();
            user.getRoles().forEach(r -> {
                roles.add(r.getName());
                r.getPermissions().forEach(p -> perms.add(p.getIdentifier()));
            });
            u.setPermissions(perms);
            u.setRoles(roles);
        } catch (Exception e) {
        }
        return u;
    }

    @Override
    public User convertDtoToEntity(UserDto dto) throws QuackServiceException {
        User u = new User();
        u.setId(dto.getId());
        u.setAlias(dto.getAlias());
        u.setEmail(dto.getEmail());
        //u.setLastestActivity(DateTime.parse(dto.getLastestActivity()get)); // not needed here
        u.setAccountActive((short) (dto.isActive() ? 1 : 0));
        return u;
    }
}
