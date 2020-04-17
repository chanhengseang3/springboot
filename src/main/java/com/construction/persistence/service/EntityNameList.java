package com.construction.persistence.service;

import org.atteo.evo.inflector.English;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EntityNameList {

    private Set<String> names;
    private Map<String, String> namesByPluralForm;
    private Set<EntityType<?>> entityTypes;

    public EntityNameList(final EntityManager entityManager) {
        this.names = entityManager.getMetamodel().getEntities().stream()
                .map(EntityType::getName)
                .collect(Collectors.toSet());
        this.entityTypes = entityManager.getMetamodel().getEntities();
        this.namesByPluralForm = names.stream().collect(Collectors.toMap(English::plural, v -> v));
    }

    public String get(final String name) {
        return names.contains(name) ?
                name :
                namesByPluralForm.get(name);
    }

    public EntityType<?> getEntityType(final String name) {
        return entityTypes.stream()
                .filter(it -> it.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
