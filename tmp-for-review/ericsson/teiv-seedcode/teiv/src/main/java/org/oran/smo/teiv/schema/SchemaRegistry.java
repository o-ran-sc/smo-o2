/*
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2024 Ericsson
 *  Modifications Copyright (C) 2024 OpenInfra Foundation Europe
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.oran.smo.teiv.schema;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.experimental.UtilityClass;

import org.oran.smo.teiv.exception.TiesException;

@UtilityClass
public class SchemaRegistry {
    @Getter
    private static Map<String, Module> moduleRegistry;
    private static Map<String, EntityType> entityTypeRegistry;
    private static Map<String, RelationType> relationTypeRegistry;

    //Modules section

    /**
     * Initializes the modules. Once set cannot be overridden.
     *
     * @param moduleTypes-
     *     module types
     */
    static void initializeModules(Map<String, Module> moduleTypes) {
        moduleRegistry = Collections.unmodifiableMap(moduleTypes);
    }

    /**
     * Gets the module by the given name.
     *
     * @param name
     *     - module name
     * @return the {@link Module}
     */
    public static Module getModuleByName(String name) {
        return Optional.ofNullable(moduleRegistry.get(name)).orElseThrow(() -> TiesException.unknownModule(name));
    }

    /**
     * Gets the module by the given domain name.
     *
     * @param domain
     *     - name of the domain
     * @return the {@link Module}
     */
    public static Module getModuleByDomain(String domain) {
        return moduleRegistry.values().stream().filter(module -> module.getDomain().equals(domain)).findFirst().orElseThrow(
                () -> TiesException.unknownDomain(domain, getDomains()));
    }

    /**
     * Gets the included domain names for the given domain name.
     *
     * @param domain
     *     - name of the domain
     * @return the list of included domains
     */
    public static List<String> getIncludedDomains(String domain) {
        return getModuleByDomain(domain).getIncludedModuleNames().stream().map(moduleName -> getModuleByName(moduleName)
                .getDomain()).toList();
    }

    /**
     * Gets all the supported domains based on the modules in the module registry.
     *
     * @return the set of domains
     */
    @Cacheable("availableDomains")
    public static Set<String> getDomains() {
        return moduleRegistry.values().stream().map(Module::getDomain).collect(Collectors.toCollection(TreeSet::new));
    }

    //Entities section
    /**
     * Initializes the entity types. Once set cannot be overridden.
     *
     * @param entityTypes
     *     - entity types
     */
    static void initializeEntityTypes(Map<String, EntityType> entityTypes) {
        entityTypeRegistry = Collections.unmodifiableMap(entityTypes);
    }

    /**
     * Gets the list of supported {@link EntityType} from the entity type registry.
     *
     * @return the list of {@link EntityType}
     */
    public static List<EntityType> getEntityTypes() {
        return entityTypeRegistry.values().stream().toList();
    }

    /**
     * Gets all the supported entity names from the entity type registry.
     *
     * @return the set of entity names
     */
    public static Set<String> getEntityNames() {
        return entityTypeRegistry.keySet();
    }

    /**
     * Gets the {@link EntityType} by the given name.
     *
     * @param name
     *     - entity name
     * @return the {@link EntityType}
     */
    public static EntityType getEntityTypeByName(String name) {
        return entityTypeRegistry.get(name);
    }

    /**
     * Gets the list of {@link EntityType} by the given domain.
     *
     * @param domain
     *     - name of the domain
     * @return the list of {@link EntityType}
     */
    public static List<EntityType> getEntityTypesByDomain(String domain) {
        List<String> domains = getIncludedDomains(domain);
        return entityTypeRegistry.values().stream().filter(entityType -> {
            String entityDomain = entityType.getModule().getDomain();
            return domains.contains(entityDomain) || entityDomain.equals(domain);
        }).toList();
    }

    /**
     * Gets the list of entity names by the given domain.
     *
     * @param domain
     *     - name of the domain
     * @return the list of entity names
     */
    @Cacheable("entityTypesByDomain")
    public static List<String> getEntityNamesByDomain(String domain) {
        return getEntityTypesByDomain(domain).stream().map(EntityType::getName).sorted().toList();
    }

    public static boolean isValidEntityName(String entityName) {
        return entityTypeRegistry.containsKey(entityName);
    }

    public static boolean hasDirectHopBetween(String entityName1, String entityName2) {
        return hasDirectHopBetween(entityName1, entityName2, null);
    }

    public static boolean hasDirectHopBetween(String entity1, String entity2, List<String> relationships) {
        return getRelationTypes().stream().anyMatch(relationType -> (relationships == null || relationships
                .isEmpty() || relationships.contains(relationType.getName())) && ((relationType.getASide().getName().equals(
                        entity1) && relationType.getBSide().getName().equals(entity2)) || (relationType.getASide().getName()
                                .equals(entity2) && relationType.getBSide().getName().equals(entity1))));
    }

    /**
     * Gets all managed objects with cmId attribute
     *
     * @return a list of managed objects
     */
    public static List<EntityType> getEntityTypesWithCmId() {
        return entityTypeRegistry.values().stream().filter(entityType -> entityType.getFields().containsKey("cmId"))
                .toList();
    }

    //Relations section
    /**
     * Initializes the relation types. Once set cannot be overridden.
     *
     * @param relationTypes
     *     - relation types
     */
    static void initializeRelationTypes(Map<String, RelationType> relationTypes) {
        relationTypeRegistry = Collections.unmodifiableMap(relationTypes);
    }

    /**
     * Gets all the supported {@link RelationType} from the relation type registry.
     *
     * @return the list of {@link RelationType}
     */
    public static List<RelationType> getRelationTypes() {
        return relationTypeRegistry.values().stream().toList();
    }

    /**
     * Gets all the supported relation names from the relation type registry.
     *
     * @return the set of relation names.
     */
    public static Set<String> getRelationNames() {
        return relationTypeRegistry.keySet();
    }

    /**
     * Gets the relation type by the given name.
     *
     * @param relationName
     *     - relation relationName
     * @return the {@link RelationType}
     */
    @Nullable
    public static RelationType getRelationTypeByName(String relationName) {
        return relationTypeRegistry.get(relationName);
    }

    /**
     * Gets all the relation types for the given entity name.
     *
     * @param entityName
     *     - entity name
     * @return the list of the {@link RelationType}
     */
    public static List<RelationType> getRelationTypesByEntityName(final String entityName) {
        return relationTypeRegistry.values().stream().filter(relationType -> relationType.getASide().getName().equals(
                entityName) || relationType.getBSide().getName().equals(entityName)).toList();
    }

    public static List<String> getRelationNamesByEntityName(final String entityName) {
        return getRelationTypesByEntityName(entityName).stream().map(RelationType::getName).toList();
    }

    public static List<String> getAssociationNamesByEntityName(final String entityName) {
        return getRelationTypesByEntityName(entityName).stream().map(relationType -> {
            RelationshipDataLocation relDataLocation = relationType.getRelationshipStorageLocation();
            if (relDataLocation.equals(RelationshipDataLocation.A_SIDE)) {
                return relationType.getASideAssociation().getName();
            } else {
                return relationType.getBSideAssociation().getName();
            }
        }).toList();
    }

    /**
     * Gets the relation types by the given domain.
     *
     * @param domain
     *     - name of the domain
     * @return the list of {@link RelationType}
     */
    public static List<RelationType> getRelationTypesByDomain(String domain) {
        List<String> includedDomains = getIncludedDomains(domain);
        return relationTypeRegistry.values().stream().filter(relationType -> {
            String relDomain = relationType.getModule().getDomain();
            return includedDomains.contains(relDomain) || relDomain.equals(domain);
        }).toList();
    }

    /**
     * Gets the relation names by the given domain.
     *
     * @param domain
     *     - name of the domain
     * @return the list of relation names
     */
    @Cacheable("relationTypesByDomain")
    public static List<String> getRelationNamesByDomain(String domain) {
        return getRelationTypesByDomain(domain).stream().map(RelationType::getName).sorted().toList();
    }

    public static boolean doesEntityContainsAttribute(String entityName, String attributeName) {
        if (entityName == null) {
            return false;
        }
        EntityType entityType = getEntityTypeByName(entityName);
        if (entityType == null) {
            return false;
        }
        return entityType.getAttributeNames().contains(attributeName);
    }

    public static List<RelationType> getRelationTypesBetweenEntities(String entity1, String entity2,
            List<String> relationships) {
        return getRelationTypes().stream().filter(relationType -> (relationships == null || relationships
                .isEmpty() || relationships.contains(relationType.getName())) && ((relationType.getASide().getName().equals(
                        entity1) && relationType.getBSide().getName().equals(entity2)) || (relationType.getBSide().getName()
                                .equals(entity1) && relationType.getASide().getName().equals(entity2)))).toList();
    }

    public static Boolean isEntityPartOfRelationships(String entity, List<String> relationships) {
        return getRelationTypes().stream().anyMatch(relationType -> relationships.isEmpty() || (relationships.contains(
                relationType.getName()) && (relationType.getASide().getName().equals(entity) || relationType.getBSide()
                        .getName().equals(entity))));
    }

    public static String getRelationNameByAssociationName(String associationName) {
        return getRelationTypes().stream().filter(relationType -> relationType.getASideAssociation().getName().equals(
                associationName) || relationType.getBSideAssociation().getName().equals(associationName)).map(
                        RelationType::getName).findFirst().get();
    }
}
