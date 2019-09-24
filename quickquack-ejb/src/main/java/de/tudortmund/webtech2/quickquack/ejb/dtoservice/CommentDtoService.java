package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.dao.QuackDao;
import de.tudortmund.webtech2.quickquack.ejb.dao.UserDao;
import de.tudortmund.webtech2.quickquack.ejb.dto.CommentDto;
import de.tudortmund.webtech2.quickquack.ejb.entity.Comment;
import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CommentDtoService implements DtoService<Comment, CommentDto> {

    @Inject
    private UserDao uDao;

    @Inject
    private QuackDao qDao;

    @Override
    public Comment convertDtoToEntity(CommentDto dto) throws QuackServiceException {
        Comment c = new Comment();
        try {
            c.setContent(dto.getContent());
            c.setAuthor(uDao.findById(dto.getAuthorId()));
            c.setQuack(qDao.findById(null, dto.getQuackId()));
        } catch (Throwable t) { }
        return c;
    }

    @Override
    public CommentDto convertEntityToDto(Comment entity) throws QuackServiceException {
        CommentDto dto = new CommentDto();
        try {
            dto.setId(entity.getId());
            String alias = entity.getAuthor().getAlias();
            dto.setAuthorName(alias != null ? alias : entity.getAuthor().getEmail());
            dto.setAuthorId(entity.getAuthor().getId());
            dto.setQuackId(entity.getQuack().getId());
            dto.setContent(entity.getContent());
            dto.setPostDate(entity.getPostDate().toString("dd.MM.yyyy HH:mm"));
        } catch (Throwable t) { }
        return dto;
    }

}
