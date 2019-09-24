package de.tudortmund.webtech2.quickquack.ejb.dtoservice;

import de.tudortmund.webtech2.quickquack.ejb.exception.QuackServiceException;
import javax.validation.constraints.NotNull;

interface DtoService<E, D> {
    E convertDtoToEntity(@NotNull D dto) throws QuackServiceException;
    D convertEntityToDto(@NotNull E entity) throws QuackServiceException;;
}
