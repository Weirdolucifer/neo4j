package org.intermine.neo4j.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Describe a Neo4j graph model.  Gives access to NodeDescriptor & RelationshipDescriptor
 * descriptors that exist within the model.
 *
 * @author Yash Sharma
 */
public class Model {

    private Set<NodeDescriptor> nodes;
    private Set<RelationshipDescriptor> relationships;

    // Maps Relationship Type to the associated Sets of Node Labels
    private Map<String, Set<Set<String>>> startNodes;
    private Map<String, Set<Set<String>>> endNodes;

    // Stores incoming & outgoing relationships of each NodeType
    private Map<Set<String>, Set<String>> incomingRelationships;
    private Map<Set<String>, Set<String>> outgoingRelationships;

    public Model(Set<NodeDescriptor> nodes, Set<RelationshipDescriptor> relationships, Map<String, Set<Set<String>>> startNodes, Map<String, Set<Set<String>>> endNodes) {
        this.nodes = nodes;
        this.relationships = relationships;
        this.startNodes = startNodes;
        this.endNodes = endNodes;

        incomingRelationships = new HashMap<>();
        outgoingRelationships = new HashMap<>();

        for (String key : startNodes.keySet()){
            for (Set<String> set : startNodes.get(key)){
                Set<String> outRelationships;
                if(outgoingRelationships.containsKey(set)){
                    outRelationships = outgoingRelationships.get(set);
                }
                else{
                    outRelationships = new HashSet<>();
                }
                outRelationships.add(key);
                outgoingRelationships.put(set, outRelationships);
            }
        }

        for (String key : endNodes.keySet()){
            for (Set<String> set : endNodes.get(key)){
                Set<String> inRelationships;
                if(incomingRelationships.containsKey(set)){
                    inRelationships = incomingRelationships.get(set);
                }
                else{
                    inRelationships = new HashSet<>();
                }
                inRelationships.add(key);
                incomingRelationships.put(set, inRelationships);
            }
        }
    }

    public Model() {
        this.nodes = null;
        this.relationships = null;
        this.startNodes = null;
        this.endNodes = null;
        this.incomingRelationships = null;
        this.outgoingRelationships = null;
    }

    @Override
    public String toString(){
        return "\nNodes :\n" + nodes.toString() + "\n\n" +
                "Relationships :\n" + relationships.toString() + "\n\n" +
                "Start Nodes :\n" + startNodes.toString() + "\n\n" +
                "End Nodes :\n" + endNodes.toString() + "\n\n" +
                "Incoming Relationships :\n" + incomingRelationships.toString() + "\n\n" +
                "Outgoing Relationships :\n" + outgoingRelationships.toString() + "\n\n";
    }

    public boolean hasNodeLabel(String label){
        for (NodeDescriptor node : nodes){
            if(node.hasLabel(label)){
                return true;
            }
        }
        return false;
    }

    public boolean hasRelationshipType(String type){
        for (RelationshipDescriptor relationship : relationships){
            if(relationship.hasType(type)){
                return true;
            }
        }
        return false;
    }

    public boolean labelHasProperty(String label, String property){
        for (NodeDescriptor node : nodes){
            if(node.hasLabel(label) && node.hasProperty(property)){
                return true;
            }
        }
        return false;
    }

    public boolean relationshipHasProperty(String type, String property){
        for (RelationshipDescriptor relationship : relationships){
            if(relationship.hasType(type) && relationship.hasProperty(property)){
                return true;
            }
        }
        return false;
    }

    public Set<String> getIncomingRelationships(String label){
        Set<String> relationships = new HashSet<>();
        for (Set<String> nodeLabels : incomingRelationships.keySet()){
            if(nodeLabels.contains(label)){
                for (String str : incomingRelationships.get(nodeLabels)){
                    relationships.add(str);
                }
            }
        }
        return relationships;
    }

    public Set<String> getOutgoingRelationships(String label){
        Set<String> relationships = new HashSet<>();
        for (Set<String> nodeLabels : outgoingRelationships.keySet()){
            if(nodeLabels.contains(label)){
                for (String str : outgoingRelationships.get(nodeLabels)){
                    relationships.add(str);
                }
            }
        }
        return relationships;
    }

    public Set<String> getLabelProperties(String label){
        Set<String> properties = new HashSet<>();
        for (NodeDescriptor node : nodes){
            if(node.hasLabel(label)){
                for (String str : node.getProperties()){
                    properties.add(str);
                }
            }
        }
        return properties;
    }

    public Set<String> getRelationshipProperties(String type){
        Set<String> properties = new HashSet<>();
        for (RelationshipDescriptor relationship : relationships){
            if(relationship.hasType(type)){
                for (String str : relationship.getProperties()){
                    properties.add(str);
                }
                break;
            }
        }
        return properties;
    }
}