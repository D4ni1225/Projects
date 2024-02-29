#ifndef BST_H
#define BST_H

#include "Node_BST.h"

template<class T>
class BST
{
private:

    // The root of the tree
    nodeBST<T>* root;
    
    // Private helper functions, which are recursive
    nodeBST<T>* insertHelper(nodeBST<T>*, T);
    nodeBST<T>* removeHelper(nodeBST<T>*, T);
    bool searchHelper(nodeBST<T>*, T);
    void inOrderHelper(nodeBST<T>*);
    void preOrderHelper(nodeBST<T>*);
    void postOrderHelper(nodeBST<T>*);
    nodeBST<T>* getMinHelper(nodeBST<T>*);
    nodeBST<T>* getMaxHelper(nodeBST<T>*);
    int getHeightHelper(nodeBST<T>*);
    int getNumberOfNodesHelper(nodeBST<T>*);
    nodeBST<T>* getSmallerHelper(nodeBST<T>*, T);
    nodeBST<T>* getLargerHelper(nodeBST<T>*, T);
    nodeBST<T>* getKthSmallestHelper(nodeBST<T>*, int, int&);
    int getRankHelper(nodeBST<T>*, T);
    void clearHelper(nodeBST<T>*);



public:
    BST();      // Creates an empty tree
    ~BST();     // Deletes the tree

    nodeBST<T>* getRoot();      // Returns the root of the tree

    // Every function is using a private helper function, which is recursive
    
    void insert(T);     // Inserts a new node into the tree, with the given value
    void remove(T);     // Removes a node from the tree, with the given value, if not found does nothing

    bool search(T);     // Returns true if the given value is in the tree, false otherwise

    void inOrder();     // Prints the tree in inOrder traversal
    void preOrder();    // Prints the tree in preOrder traversal
    void postOrder();   // Prints the tree in postOrder traversal

    nodeBST<T>* getMin();         // Returns a pointer to the smallest value in the tree
    nodeBST<T>* getMax();         // Returns a pointer to the largest value in the tree

    int getHeight();    // Returns the height of the tree
    int getNumberOfNodes();     // Returns the number of nodes in the tree

    nodeBST<T>* getSmaller(T);    // Returns a pointer to the largest value in the tree, which is smaller than the given value
    nodeBST<T>* getLarger(T);     // Returns a pointer to the smallest value in the tree, which is larger than the given value
    nodeBST<T>* getKthSmallest(int);    // Returns a pointer to the kth smallest value in the tree
    int getRank(T);     // Returns the rank of the given value in the tree

    void clear();       // Clears the tree, so the tree will be empty
    void print();       // Prints the tree in inOrder traversal
};

#include "BST.cpp"

#endif