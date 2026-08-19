package com.orbitet.repository;

import com.orbitet.entities.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.neo4j.Neo4jContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Exercises the Cypher in {@link PersonRepository} against a real Neo4j instance —
 * a mock cannot tell us whether the query traverses the graph correctly.
 */
@DataNeo4jTest
@Testcontainers
class PersonRepositoryTest {

    private static final long ALICE = 1L;
    private static final long BOB = 2L;
    private static final long CAROL = 3L;

    @Container
    @ServiceConnection
    static Neo4jContainer neo4j = new Neo4jContainer("neo4j:5.26-community");

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private Neo4jClient neo4jClient;

    @BeforeEach
    void resetGraph() {
        neo4jClient.query("MATCH (n) DETACH DELETE n").run();
        givenPerson(ALICE, "Alice");
        givenPerson(BOB, "Bob");
        givenPerson(CAROL, "Carol");
    }

    @Test
    @DisplayName("returns the people the user is directly connected to, fully mapped")
    void returnsDirectConnections() {
        givenConnection(ALICE, BOB);
        givenConnection(ALICE, CAROL);

        List<Person> connections = personRepository.getFirstDegreeConnections(ALICE);

        assertThat(connections)
                .hasSize(2)
                .allSatisfy(person -> assertThat(person.getId()).isNotNull())
                .extracting(Person::getUserId, Person::getName)
                .containsExactlyInAnyOrder(
                        tuple(BOB, "Bob"),
                        tuple(CAROL, "Carol"));
    }

    @Test
    @DisplayName("stops at the first degree and does not walk further out the graph")
    void doesNotReturnSecondDegreeConnections() {
        givenConnection(ALICE, BOB);
        givenConnection(BOB, CAROL);

        assertThat(personRepository.getFirstDegreeConnections(ALICE))
                .extracting(Person::getName)
                .containsExactly("Bob");
    }

    @Test
    @DisplayName("never returns the user themselves, even in a connection cycle")
    void doesNotReturnTheUserThemselves() {
        givenConnection(ALICE, BOB);
        givenConnection(BOB, ALICE);

        assertThat(personRepository.getFirstDegreeConnections(ALICE))
                .extracting(Person::getUserId)
                .doesNotContain(ALICE);
    }

    @Test
    @DisplayName("follows CONNECTED_TO outward only: an inbound-only edge is not a connection")
    void ignoresIncomingRelationships() {
        givenConnection(BOB, ALICE);

        assertThat(personRepository.getFirstDegreeConnections(ALICE)).isEmpty();
    }

    @Test
    @DisplayName("ignores relationship types other than CONNECTED_TO")
    void ignoresOtherRelationshipTypes() {
        neo4jClient.query("""
                        MATCH (a:Person {userId: $from})
                        WITH a
                        MATCH (b:Person {userId: $to})
                        CREATE (a)-[:BLOCKED]->(b)
                        """)
                .bind(ALICE).to("from")
                .bind(BOB).to("to")
                .run();

        assertThat(personRepository.getFirstDegreeConnections(ALICE)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list for a user with no connections")
    void returnsEmptyForUnconnectedUser() {
        assertThat(personRepository.getFirstDegreeConnections(ALICE)).isEmpty();
    }

    @Test
    @DisplayName("returns an empty list for a userId that has no node at all")
    void returnsEmptyForUnknownUser() {
        assertThat(personRepository.getFirstDegreeConnections(404L)).isEmpty();
    }

    @Test
    @DisplayName("the query has no DISTINCT, so a duplicated edge yields the same person twice")
    void duplicateRelationshipsProduceDuplicateResults() {
        givenConnection(ALICE, BOB);
        givenConnection(ALICE, BOB);

        assertThat(personRepository.getFirstDegreeConnections(ALICE))
                .extracting(Person::getName)
                .containsExactly("Bob", "Bob");
    }

    private void givenPerson(long userId, String name) {
        neo4jClient.query("CREATE (:Person {userId: $userId, name: $name})")
                .bind(userId).to("userId")
                .bind(name).to("name")
                .run();
    }

    private void givenConnection(long fromUserId, long toUserId) {
        neo4jClient.query("""
                        MATCH (a:Person {userId: $from})
                        WITH a
                        MATCH (b:Person {userId: $to})
                        CREATE (a)-[:CONNECTED_TO]->(b)
                        """)
                .bind(fromUserId).to("from")
                .bind(toUserId).to("to")
                .run();
    }
}
