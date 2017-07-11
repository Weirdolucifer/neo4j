package org.intermine.neo4j.cypher.tree;

import org.intermine.neo4j.cypher.Helper;
import org.intermine.neo4j.cypher.OntologyConverter;
import org.intermine.pathquery.OuterJoinStatus;
import org.intermine.pathquery.PathQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes a Path Tree.
 *
 * @author Yash Sharma
 */
public class PathTree {

    private TreeNode root;

    public PathTree(PathQuery pathQuery){
        Boolean DEBUG = false;

        Set<String> paths = Helper.getAllPaths(pathQuery);

        if(DEBUG){
            System.out.println(paths);
        }

        // Initialize root node with null, since no node currently exists in the PathTree.
        root = null;

        // Create tree using all the paths
        for (String path : paths){
            List<String> tokens = Helper.getTokensFromPath(path);

            // Start variable name as an empty string. Gradually add each token for each node.
            String variableName = "";

            // List used to keep track of what portion of the path we have traversed.
            // At start, no portion of the path is traversed. So it is an empty list.
            List<String> pathSoFar = new ArrayList<>(tokens.size());

            if(DEBUG){
                System.out.println(path + "\n");
            }

            // We will create a TreeNode for each token of the Path.
            for (String token : tokens){
                // Since we have traversed till this token, add it to pathSoFar.
                pathSoFar.add(token);

                // TO DO : Use another way of creating variable names.
                // Underscore separated names may get unnecessarily large.

                // Add token to variable name
                variableName += (token.toLowerCase());

                // TO DO - Set TreeNodeType of TreeNode using methods from Ontology Converter
                if(this.root == null){
                    // Create the root TreeNode. It is always represents a Graph Node.
                    // Parent is null for root.
                    if (DEBUG) {
                        System.out.println("Root created " + variableName);
                    }
                    this.root = new TreeNode(variableName,
                                            token,
                                            TreeNodeType.NODE,
                                            null,
                                            OuterJoinStatus.INNER);
                }
                else{
                    // Traverse through the tree to reach the required leaf node
                    TreeNode treeNode = root;
                    for (String str : pathSoFar){
                        if (pathSoFar.indexOf(str) == 0){
                            // Skip the root
                            continue;
                        }
                        // Get current TreeNode's child
                        TreeNode child = treeNode.getChild(str);
                        TreeNodeType treeNodeType = OntologyConverter.getTreeNodeType(str);

                        // If child does not exist, create a new one.
                        // If it exists already, then this path prefix was also found in a previous path,
                        // so tokens for that have already been created. In this case we can simply do nothing
                        // and wait till we encounter a token for which TreeNode is not yet created.
                        if(child == null){
                            treeNode.addChild(str, new TreeNode(variableName,
                                                                str,
                                                                treeNodeType,
                                                                treeNode,
                                                                OuterJoinStatus.INNER));
                            break;
                        }
                        treeNode = child;
                    }
                }
                // Add an underscore in the variable name for the next child.
                variableName += "_";
            }

            setOuterJoinInPathTree(pathQuery);

            if(DEBUG){
                System.out.println();
            }
        }
    }

    private void setOuterJoinInPathTree(PathQuery pathQuery){
        Map<String, OuterJoinStatus> outerJoinStatusMap = pathQuery.getOuterJoinStatus();
        for (String path : outerJoinStatusMap.keySet()){
            TreeNode treeNode = getTreeNode(path);
            if (treeNode != null && outerJoinStatusMap.get(path) == OuterJoinStatus.OUTER){
                // TO DO : Ponder whether we should set outer join for all children or just for
                // the current node.

                treeNode.setOuterJoinStatus(OuterJoinStatus.OUTER);
                //setOuterJoinInChildren(getTreeNode(path));
            }
        }
    }

    private void setOuterJoinInChildren(TreeNode treeNode){
        if (treeNode == null){
            return;
        }
        treeNode.setOuterJoinStatus(OuterJoinStatus.OUTER);
        for (String key : treeNode.getChildrenKeys()){
            setOuterJoinInChildren(treeNode.getChild(key));
        }
    }

    /**
     * Gets the root TreeNode of the PathTree
     *
     * @return the root TreeNode
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Gets the path of the Root Node
     *
     * @return the root TreeNode
     */
    public String getRootPath() {
        return root.getName();
    }

    /**
     * Traverses the PathTree as per the given path and returns the TreeNode found
     *
     * @param path the given path
     * @return the TreeNode object if it is found, null otherwise
     */
    public TreeNode getTreeNode(String path){
        if(root == null){
            return null;
        }
        List<String> nodes = Helper.getTokensFromPath(path);
        TreeNode treeNode = root;
        for (String node: nodes){
            if (nodes.indexOf(node) == 0){
                continue;
            }
            treeNode = treeNode.getChild(node);
            if(treeNode == null){
                return null;
            }
        }
        return treeNode;
    }

    /**
     * Prints names of all the nodes of a tree serially, separated by a closing parenthesis.
     */
    public void serialize(){
        System.out.print("Serializing PathTree :\n");
        if (root == null){
            return;
        }
        System.out.print(root.getVariableName()+ ":" + root.getTreeNodeType().name() + " ");
        for (String key : root.getChildrenKeys()){
            serializeUtil(root.getChild(key));
        }
        System.out.println(")");
    }

    /**
     * Utility method which does recursion for the serialize method.
     */
    private void serializeUtil(TreeNode treeNode){
        if (treeNode == null){
            return;
        }
        System.out.print(treeNode.getName()+ ":" + treeNode.getTreeNodeType().name() + " ");
        for (String key : treeNode.getChildrenKeys()){
            serializeUtil(treeNode.getChild(key));
        }
        System.out.print(")");
    }

}
