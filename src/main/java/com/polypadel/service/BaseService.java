package com.polypadel.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.function.Function;

/**
 * Generic base service providing common CRUD operations.
 * @param <T> Entity type
 * @param <ID> Entity ID type
 * @param <R> Response DTO type
 */
public abstract class BaseService<T, ID, R> {

    protected abstract JpaRepository<T, ID> getRepository();
    protected abstract R toResponse(T entity);
    protected abstract String getEntityName();

    public List<R> findAll() {
        return getRepository().findAll().stream().map(this::toResponse).toList();
    }

    public R findById(ID id) {
        return toResponse(getRepository().findById(id)
            .orElseThrow(() -> notFound()));
    }

    public T getEntityById(ID id) {
        return getRepository().findById(id)
            .orElseThrow(() -> notFound());
    }

    public void deleteById(ID id) {
        T entity = getEntityById(id);
        validateDelete(entity);
        getRepository().delete(entity);
    }

    protected void validateDelete(T entity) {
        // Override in subclasses for custom validation
    }

    protected ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, getEntityName() + " non trouvé(e)");
    }

    protected ResponseStatusException conflict(String message) {
        return new ResponseStatusException(HttpStatus.CONFLICT, message);
    }

    protected ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Simple HTML tag sanitization for XSS prevention.
     */
    protected String sanitize(String input) {
        return input == null ? null : input.replaceAll("<[^>]*>", "").trim();
    }
}
