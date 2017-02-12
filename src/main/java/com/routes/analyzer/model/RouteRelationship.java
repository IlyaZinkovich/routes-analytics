package com.routes.analyzer.model;

import org.neo4j.ogm.annotation.*;

@RelationshipEntity(type = "ROUTE")
public class RouteRelationship {

    @GraphId
    private Long relationshipId;
    @StartNode
    private PlaceNode from;
    @EndNode
    private PlaceNode to;
    @Property
    private String date;
    @Property
    private String source;

    public RouteRelationship() {
    }

    public RouteRelationship(PlaceNode from, PlaceNode to, String date, String source) {
        this.from = from;
        this.to = to;
        this.date = date;
        this.source = source;
    }

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    public PlaceNode getFrom() {
        return from;
    }

    public void setFrom(PlaceNode from) {
        this.from = from;
    }

    public PlaceNode getTo() {
        return to;
    }

    public void setTo(PlaceNode to) {
        this.to = to;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RouteRelationship routeRelationship = (RouteRelationship) o;

        if (from != null ? !from.equals(routeRelationship.from) : routeRelationship.from != null) return false;
        if (to != null ? !to.equals(routeRelationship.to) : routeRelationship.to != null) return false;
        if (date != null ? !date.equals(routeRelationship.date) : routeRelationship.date != null) return false;
        return source != null ? source.equals(routeRelationship.source) : routeRelationship.source == null;
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }
}
