package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.CommentDto;
import de.tudortmund.webtech2.quickquack.ejb.dto.QuackDto;
import de.tudortmund.webtech2.quickquack.ejb.entity.Comment;
import de.tudortmund.webtech2.quickquack.ejb.entity.Quack;
import de.tudortmund.webtech2.quickquack.ejb.entity.User;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackDataAccessException;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.joda.time.DateTime;

@ApplicationScoped
public class QuackDtoService implements DtoService<Quack, QuackDto> {

    @Inject
    private UserDao userDao;
    @Inject
    private CommentDtoService commService;

    @Override
    public Quack convertDtoToEntity(QuackDto dto) throws QuackServiceException {
        Quack q = new Quack();
        q.setId(dto.getId());
        q.setContent(dto.getContent());
        q.setPostDate(DateTime.now());
        q.setScope(dto.getScope());
        try {
            q.setAuthor(userDao.findById(dto.getAuthorId()));
        } catch (QuackDataAccessException e) {
        }
        return q;
    }

    @Override
    public QuackDto convertEntityToDto(Quack entity) throws QuackServiceException {
        return convertEntityToDto(entity, 0);
    }

    public QuackDto convertEntityToDto(Quack entity, int loadedComments) throws QuackServiceException {
        QuackDto dto = new QuackDto();
        String alias = entity.getAuthor().getAlias();
        dto.setId(entity.getId());
        dto.setAuthorName(alias != null ? alias : entity.getAuthor().getEmail());
        dto.setAuthorId(entity.getAuthor().getId());
        dto.setContent(entity.getContent());
        dto.setPostDate(entity.getPostDate().toString("dd.MM.yyyy HH:mm"));
        dto.setScope(entity.getScope());
        dto.setCommentsCount(entity.getComments() == null ? 0 : entity.getComments().size());
        Collection<CommentDto> comms = new ArrayList<>();
        if (entity.getComments() != null && !entity.getComments().isEmpty() && loadedComments > 0) {
            Iterator<Comment> it = entity.getComments().iterator();
            for (int i = 0, s = entity.getComments().size(); i < s && i < loadedComments; i++) {
                comms.add(commService.convertEntityToDto(it.next()));
            }
        }
        dto.setComments(comms);
        try {
            User auth = userDao.findByEmail(SecurityUtils.getSubject().getPrincipal().toString());
            dto.setTotalLikes(entity.getLikers().size());
            dto.setLiked(auth.getLikedQuacks().contains(entity));
            dto.setAuthorFollowed(auth.getFollowedUsers().contains(entity.getAuthor()));
            dto.setAuthorActive(entity.getAuthor().getAccountActive() == 1);
            dto.setAuthorBlocked(auth.getBlockedUsers().contains(entity.getAuthor()));
        } catch (Throwable t) { }
        return dto;
    }
}
