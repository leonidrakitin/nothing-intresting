package ru.sushi.delivery.kds.domain.persist.holder;

import ru.sushi.delivery.kds.domain.persist.entity.Identifiable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractInMemoryHolder<E extends Identifiable<I>, I> implements Holder<E, I> {

    private Map<I, E> content = new ConcurrentHashMap<>(); //TODO must be final

    @Override
    public Collection<E> findAll() {
        return content.values();
    }

    @Override
    public Optional<E> get(I id) {
        return Optional.ofNullable(content.get(id));
    }

    public E getOrThrow(I id) {
        return Optional.ofNullable(content.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Invalid id: " + id));
    }

    @Override
    public E save(E entity) {
        content.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(E entity) {
        content.remove(entity.getId());
    }

    @Override
    public void deleteById(I id) {
        content.remove(id);
    }

    public void load(List<E> content) {
        this.content = new ConcurrentHashMap<>(
                content.stream().collect(Collectors.toUnmodifiableMap(
                        Identifiable::getId,
                        Function.identity()
                ))
        );
    }
}
