/*
 *  @(#)Location.java
 * 
 *  Copyright 2014 Diego Rani Mazine. All rights reserved.
 */
package org.sample.astar.domain.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * This class represents a location.
 * 
 * @author Diego Rani Mazine
 */
public class Location {

	/** Name property key. */
	public static final String PROPERTY_NAME = "NAME";

	/** Distance property key. */
	public static final String PROPERTY_DISTANCE = "DISTANCE";

	/** Represents the relationship type between locations. */
	public static enum LocationRelationshipType implements RelationshipType {
		ROUTE
	}

	/** The underlying node. */
	private Node node = null;

	/**
	 * Creates a Location object.
	 * 
	 * @param name
	 *            the location name.
	 * @throws IllegalArgumentException
	 *             if node is null.
	 */
	public Location(Node node) {
		setNode(node);
	}

	/**
	 * Creates a Location object.
	 * 
	 * @param graphDatabase
	 *            the graph database to use.
	 * @param name
	 *            the name to set.
	 * @throws IllegalArgumentException
	 *             if graphDatabase is null.
	 * @throws IllegalArgumentException
	 *             if name is null.
	 */
	public Location(GraphDatabaseService graphDatabase, String name) {
		if (graphDatabase == null) {
			throw new IllegalArgumentException("graphDatabase is null");
		}

		// Creates the underlying node
		setNode(graphDatabase.createNode());
		setName(name);
	}

	/**
	 * Gets the underlying node attached to this entity.
	 * 
	 * @return the underlying node attached to this entity.
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * Sets the underlying node attached to this entity.
	 * 
	 * @param node
	 *            the node to set.
	 * @throws IllegalArgumentException
	 *             if node is null.
	 */
	public void setNode(Node node) {
		if (node == null) {
			throw new IllegalArgumentException("node is null");
		}
		this.node = node;
	}

	/**
	 * Gets the unique id of this entity.
	 * 
	 * @return the unique id of this entity.
	 */
	public long getId() {
		return node.getId();
	}

	/**
	 * Gets the location name.
	 * 
	 * @return the location name.
	 */
	public String getName() {
		return (String) node.getProperty(PROPERTY_NAME);
	}

	/**
	 * Sets the location name.
	 * 
	 * @param name
	 *            the name to set.
	 * @throws IllegalArgumentException
	 *             if name is null.
	 */
	private void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("name is null");
		}
		node.setProperty(PROPERTY_NAME, name);
	}

	/**
	 * Adds a route to the specified destination.
	 * 
	 * @param destination
	 *            the destination.
	 * @param distance
	 *            the distance between the locations.
	 * @throws IllegalArgumentException
	 *             if destination is null.
	 * @throws IllegalArgumentException
	 *             if distance is negative.
	 */
	public void addRoute(Location destination, double distance) {
		if (destination == null) {
			throw new IllegalArgumentException("destination is null");
		}
		if (distance < 0) {
			throw new IllegalArgumentException("distance is negative");
		}

		// Creates a relationship between the locations
		final Relationship relationship = node.createRelationshipTo(
				destination.node, LocationRelationshipType.ROUTE);
		relationship.setProperty(PROPERTY_DISTANCE, distance);
	}

	/**
	 * Finds the shortest path to the destination.
	 * 
	 * @param destination
	 *            the destination
	 * @return the shortest path to the destination.
	 */
	public Path findShortestPathTo(Location destination) {
		// TODO: Implement this. Note that more than one shortest path may
		// exist!
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final Location rhs = (Location) obj;
		return new EqualsBuilder().append(getId(), rhs.getId()).isEquals();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}

}
