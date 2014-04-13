/*
 *  @(#)Main.java
 * 
 *  Copyright 2014 Diego Rani Mazine. All rights reserved.
 */
package org.sample.astar;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.ReadableIndex;
import org.sample.astar.domain.model.Location;

/**
 * A* search algorithm using Neo4j.
 * 
 * @author Diego Rani Mazine
 */
public class Main {

	// TODO: Test with only 1 node, using only 2 nodes,
	// assert(d(a,b) == d(b,a)), assert(d(a,a) == 0).
	// Incluir a mesma rota com valores diferentes.
	// Incluir a rotas diferentes com a mesma distancia.
	// Testar excecoes, distancia negativa e valores nulos

	/**
	 * Application entry point.
	 * 
	 * @param args
	 *            the command line arguments.
	 */
	public static void main(String[] args) {
		// Graph database service
		GraphDatabaseService graphDatabase = null;

		try {
			// Creates the database
			// graphDatabase = new GraphDatabaseFactory()
			// .newEmbeddedDatabase("target/graphDb");
			graphDatabase = new GraphDatabaseFactory()
					.newEmbeddedDatabaseBuilder("target/graphDb")
					.setConfig(GraphDatabaseSettings.node_keys_indexable,
							Location.PROPERTY_NAME)
					.setConfig(
							GraphDatabaseSettings.relationship_keys_indexable,
							Location.PROPERTY_DISTANCE)
					.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
					.setConfig(
							GraphDatabaseSettings.relationship_auto_indexing,
							"true").newGraphDatabase();

			try (Transaction transaction = graphDatabase.beginTx()) {
				// Loads routes into the database
				loadRoutes(graphDatabase);
				
				// Commits the transaction
				transaction.success();
			}

			try (Transaction transaction = graphDatabase.beginTx()) {
				// Finds the shortest path two locations.
				findShortestPath(graphDatabase, "A", "D");

				// Commits the transaction
				transaction.success();
			}

			// Um exemplo de entrada seria, origem A, destino D, autonomia 10,
			// valor do litro 2,50; a resposta seria a rota A B D com custo de
			// 6,75.
		} catch (Exception e) {
			// Debug
			e.printStackTrace();
		} finally {
			// Shuts down the database
			if (graphDatabase != null) {
				graphDatabase.shutdown();
			}
		}
	}

	/**
	 * Loads routes into the database
	 * 
	 * @param graphDatabase
	 *            the graph database to use.
	 * @throws IllegalArgumentException
	 *             if graphDatabase is null.
	 */
	private static void loadRoutes(GraphDatabaseService graphDatabase) {
		if (graphDatabase == null) {
			throw new IllegalArgumentException("graphDatabase is null");
		}

		// TODO: Search for existing nodes - Do not forget of creating
		// indexes!
		final Location a = new Location(graphDatabase, "A");
		final Location b = new Location(graphDatabase, "B");
		final Location c = new Location(graphDatabase, "C");
		final Location d = new Location(graphDatabase, "D");
		final Location e = new Location(graphDatabase, "E");

		a.addRoute(b, 10);
		b.addRoute(d, 15);
		a.addRoute(c, 20);
		c.addRoute(d, 30);
		b.addRoute(e, 50);
		d.addRoute(e, 30);
	}

	/**
	 * Finds the shortest path two locations.
	 * 
	 * @param graphDatabase
	 *            the graph database to use.
	 * @param origin
	 *            the origin.
	 * @param destination
	 *            the destination.
	 * @throws IllegalArgumentException
	 *             if graphDatabase is null.
	 * @throws IllegalArgumentException
	 *             if graphDatabase, origin or destination is null.
	 */
	public static void findShortestPath(GraphDatabaseService graphDatabase,
			String origin, String destination) {
		if (graphDatabase == null) {
			throw new IllegalArgumentException("graphDatabase is null");
		}
		if (origin == null) {
			throw new IllegalArgumentException("origin is null");
		}
		if (destination == null) {
			throw new IllegalArgumentException("destination is null");
		}

		//
		final CostEvaluator<Double> costEvaluator = CommonEvaluators
				.doubleCostEvaluator(Location.PROPERTY_DISTANCE);

		// Gets an PathFinder which uses the Dijkstra algorithm to find
		// the cheapest path between two nodes
		final PathFinder<WeightedPath> pathFinder = GraphAlgoFactory.dijkstra(
				PathExpanders.allTypesAndDirections(), costEvaluator);

		//
		final Path path = pathFinder.findSinglePath(
				findLocation(graphDatabase, origin).getNode(),
				findLocation(graphDatabase, destination).getNode());
		
		//
		System.out.println("Nodes: ");
		for (Node node : path.nodes()) {
			System.out.println(node.getProperty(Location.PROPERTY_NAME));
		}

		//
		System.out.println("Distances: ");
		for (Relationship relationship : path.relationships()) {
			System.out.println(relationship.getProperty(Location.PROPERTY_DISTANCE));
		}
	}

	/**
	 * 
	 * @param graphDatabase
	 * @param name
	 * @return
	 */
	public static Location findLocation(GraphDatabaseService graphDatabase,
			String name) {
		if (graphDatabase == null) {
			throw new IllegalArgumentException("graphDatabase is null");
		}
		if (name == null) {
			throw new IllegalArgumentException("name is null");
		}

		// Gets the auto index used by the auto indexer
		final ReadableIndex<Node> nodeIndex = graphDatabase.index()
				.getNodeAutoIndexer().getAutoIndex();

		// Gets the first and only item from the result iterator, or null if
		// there was none
		return new Location(nodeIndex.get(Location.PROPERTY_NAME, name)
				.getSingle());
	}

}
