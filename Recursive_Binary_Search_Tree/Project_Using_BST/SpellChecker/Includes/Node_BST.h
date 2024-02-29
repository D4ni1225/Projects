#ifndef NODE_BST_H
#define NODE_BST_H

template<class T>
class nodeBST
{
private:
    T data;
    nodeBST* right;
    nodeBST* left;

public:
    nodeBST();                  // Create empty node
    nodeBST(T);                 // Create node with data
    ~nodeBST();                 // Delete node
    
    T getValue();               // Returns the data of the node
    void setValue(T);           // Sets the data of the node

    nodeBST* getRight();        // Returns the right child of the node
    void setRight(nodeBST*);    // Sets the right child of the node
    nodeBST* getLeft();         // Returns the left child of the node
    void setLeft(nodeBST*);     // Sets the left child of the node

    bool isLeaf();              // Returns true if the node is a leaf, otherwise false
    bool hasLeft();             // Returns true if the node has a left child, otherwise false
    bool hasRight();            // Returns true if the node has a right child, otherwise false

    void print();               // Prints the data of the node
};

#include "Node_BST.cpp"

#endif