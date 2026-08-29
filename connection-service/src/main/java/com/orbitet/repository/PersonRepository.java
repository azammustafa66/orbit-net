package com.orbitet.repository;

import com.orbitet.entities.Person;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends Neo4jRepository<Person, Long> {

    Optional<Person> findByUserId(Long userId);

    /**
     * Idempotent by design: Kafka delivers at least once, and a plain {@code save()}
     * would create a second node for the same user on a redelivery or rebalance. The
     * first-degree query has no {@code DISTINCT}, so a duplicate node would surface as
     * the same person appearing twice in every connection list.
     */
    @Query("""
        MERGE (person:Person {userId: $userId})
        SET person.name = $name
        RETURN person
        """)
    Person upsertByUserId(Long userId, String name);

    /**
     * Outbound {@code CONNECTED_TO} edges only, one hop out — an inbound-only edge or a
     * second-degree connection is not returned. No {@code DISTINCT}, so a duplicated edge
     * (e.g. from a redelivered accept) surfaces the same person twice.
     */
    @Query("""
        MATCH (personA:Person)-[:CONNECTED_TO]->(personB:Person)
        WHERE personA.userId = $userId
        RETURN personB
        """)
    List<Person> getFirstDegreeConnections(Long userId);

    /** Directional: a pending request only counts against the sender who raised it. */
    @Query("MATCH (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "RETURN count(r) > 0")
    boolean connectionRequestExists(Long senderId, Long receiverId);

    /**
     * The {@code -[r:CONNECTED_TO]-} pattern is undirected, unlike
     * {@link #getFirstDegreeConnections}, since it only needs to know a connection exists
     * between the pair, regardless of who originally sent the request.
     */
    @Query("MATCH (p1:Person)-[r:CONNECTED_TO]-(p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "RETURN count(r) > 0")
    boolean alreadyConnected(Long senderId, Long receiverId);

    /** Assumes both {@code Person} nodes already exist; a missing node means no edge is created. */
    @Query("MATCH (p1:Person), (p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "CREATE (p1)-[:REQUESTED_TO]->(p2)")
    void addConnectionRequest(Long senderId, Long receiverId);

    /**
     * Promotes the sender-to-receiver {@code REQUESTED_TO} edge to {@code CONNECTED_TO} in
     * one traversal. If no such pending request matches (including a reversed sender/receiver
     * pair), the MATCH finds nothing and this is a silent no-op.
     */
    @Query("MATCH (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r " +
            "CREATE (p1)-[:CONNECTED_TO]->(p2)")
    void acceptConnectionRequest(Long senderId, Long receiverId);

    /** Only deletes the sender-to-receiver pending request; a reverse request is untouched. */
    @Query("MATCH (p1:Person)-[r:REQUESTED_TO]->(p2:Person) " +
            "WHERE p1.userId = $senderId AND p2.userId = $receiverId " +
            "DELETE r")
    void rejectConnectionRequest(Long senderId, Long receiverId);

}