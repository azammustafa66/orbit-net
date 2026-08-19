package com.orbitet.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Data
@Builder
public class Person {

    @Id
    @GeneratedValue
    private Long id; // ID created and managed by Neo4J to function

    private Long userId; // Id stored in PostgreSQL
    private String name;
}
