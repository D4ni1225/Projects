#include <iostream>

using namespace std;


template<class T>
BST<T>::BST()       // Creates an empty tree, by setting the root to NULL
{
    root = NULL;
}

template<class T>
BST<T>::~BST()      // Deletes the tree, using the clearHelper function
{
    if(root != NULL)
    {
        clearHelper(root);
    }
}

template<class T>
nodeBST<T>* BST<T>::insertHelper(nodeBST<T>* root, T data)      // Recursive helper function for insert
{
    if(root == NULL)                                            // Base case, create data new node
    {
        root = new nodeBST<T>(data);
        return root;
    }
    else if (data < root->getValue())                           // Recursive case, traverse left, if data is less than root
    {
        root->setLeft(insertHelper(root->getLeft(), data));
    }
    else if (data > root->getValue())                           // Recursive case, traverse right, if data is greater than root
    {
        root->setRight(insertHelper(root->getRight(), data));
    }
    else                                                        // Case, when data is equal to root, throw exception
    {
        throw "Duplicate value!";
    }
    return root;                                                // Return the root
}

template<class T>
void BST<T>::insert(T data)         // Inserts a new node into the tree, with the given value or throws exception if duplicate
{
    root = insertHelper(root, data);    // Call the recursive helper function, with the root and the value
}

template<class T>
nodeBST<T>* BST<T>::removeHelper(nodeBST<T>* root, T data)      // Recursive helper function for remove
{
    if(root == NULL)                                            // Base case, do nothing, just return NULL
    {
        return root;
    }
    else if (data < root->getValue())                           // Recursive case, traverse left, if data is less than root
    {
        root->setLeft(removeHelper(root->getLeft(), data));
    }
    else if (data > root->getValue())                           // Recursive case, traverse right, if data is greater than root
    {
        root->setRight(removeHelper(root->getRight(), data));
    }
    else                                                        // Cases if we found the node to be deleted
    {
        if(root->isLeaf())                                      // Case 1: Node is data leaf, then just delete it
        {
            root = NULL;
            return root;
        }
        else if(!root->hasLeft() && root->hasRight())           // Case 2: Node has only right child
        {
            nodeBST<T>* temp = root;                            // Copy the right child to the root
            root = root->getRight();
            temp->setRight(NULL);                               // Set the right child to NULL and delete the node
            delete temp;
        }
        else if(!root->hasRight() && root->hasLeft())           // Case 2: Node has only left child
        {
            nodeBST<T>* temp = root;                            // Copy the left child to the root
            root = root->getLeft();
            temp->setLeft(NULL);                                // Set the left child to NULL and delete the node
            delete temp;
        }
        else                                                    // Case 3: Node has two children
        {
            nodeBST<T>* temp = root->getRight();                // Find the smallest node in the right subtree
            while(temp->getLeft() != NULL)
            {
                temp = temp->getLeft();
            }
            root->setValue(temp->getValue());                   // Copy the value of the smallest node to the root
            root->setRight(removeHelper(root->getRight(), temp->getValue()));       // Delete the duplicate node, by calling the removeHelper function on the right subtree
        }
    }
    return root;
}

template<class T>
void BST<T>::remove(T data)     // Removes the node with the given value from the tree, if not found, returns NULL
{
    root = removeHelper(root, data);
}

template<class T>
bool BST<T>::searchHelper(nodeBST<T>* root, T data)     // Recursive helper function for search
{
    if(root == NULL)                                    // If the root is NULL, then we didn't find the node
    {
        return false;
    }
    if(root->getValue() == data)                        // If found, return true
    {
        return true;
    }
    else if(data < root->getValue())                    // If data is less than root, traverse left
    {
        return searchHelper(root->getLeft(), data);
    }
    else                                                // If data is greater than root, traverse right
    {
        return searchHelper(root->getRight(), data);
    }
}

template<class T>
bool BST<T>::search(T data)     // Searches for the node with the given value, returns true if found, false otherwise
{
    return searchHelper(root, data);
}

template<class T>
void BST<T>::inOrderHelper(nodeBST<T>* root)        // Recursive helper function for inOrder traversal
{
    if(root != NULL)
    {
        inOrderHelper(root->getLeft());
        cout << root->getValue() << " ";
        inOrderHelper(root->getRight());
    }
}

template<class T>
void BST<T>::preOrderHelper(nodeBST<T>* root)        // Recursive helper function for preOrder traversal
{
    if(root != NULL)
    {
        cout << root->getValue() << " ";
        preOrderHelper(root->getLeft());
        preOrderHelper(root->getRight());
    }
}

template<class T>
void BST<T>::postOrderHelper(nodeBST<T>* root)        // Recursive helper function for postOrder traversal
{
    if(root != NULL)
    {
        postOrderHelper(root->getLeft());
        postOrderHelper(root->getRight());
        cout << root->getValue() << " ";
    }
}

template<class T>
void BST<T>::inOrder()      // Prints the tree in inOrder traversal
{
    inOrderHelper(root);
}

template<class T>
void BST<T>::preOrder()     // Prints the tree in preOrder traversal
{
    preOrderHelper(root);
}

template<class T>
void BST<T>::postOrder()    // Prints the tree in postOrder traversal
{
    postOrderHelper(root);
}

template<class T>
nodeBST<T>* BST<T>::getMinHelper(nodeBST<T>* root)          // Recursive helper function for getMin, returns a pointer to the smallest value in the tree
{
    if(root->getLeft() == NULL)                             // Base case, leftmost node is the smallest
    {
        return root;
    }
    else
    {
        return getMinHelper(root->getLeft());               // Recursive case, traverse left to find the smallest
    }
}

template<class T>
nodeBST<T>* BST<T>::getMin()      // Returns a pointer to the smallest value in the tree or throws an exception if the tree is empty
{
    if(root != NULL)    // If the tree is not empty, then call the helper function
    {
        return getMinHelper(root);
    }
    else                // If the tree is empty, throw an exception
    {
        throw "Empty tree, no min value!";
    }
}

template<class T>
nodeBST<T>* BST<T>::getMaxHelper(nodeBST<T>* root)          // Recursive helper function for getMax, returns a pointer to the largest value in the tree
{
    if(root->getRight() == NULL)                            // Base case, rightmost node is the largest
    {
        return root;
    }
    else
    {
        return getMaxHelper(root->getRight());              // Recursive case, traverse right to find the largest
    }
}

template<class T>
nodeBST<T>* BST<T>::getMax()      // Returns a pointer to the largest value in the tree or throws an exception if the tree is empty
{
    if(root != NULL)    // If the tree is not empty, then call the helper function
    {
        return getMaxHelper(root);
    }
    else                // If the tree is empty, throw an exception
    {
        throw "Empty tree, no max value!";
    }
}

template<class T>
int BST<T>::getHeightHelper(nodeBST<T>* root)    // Recursive helper function for getHeight, returns the height of the tree
{
    if(root == NULL)                            // Base case, no nodes, then height is 0
    {
        return 0;
    }
    else
    {
        int leftHeight = getHeightHelper(root->getLeft());      // Recursive case, traverse left to find the height of the left subtree
        int rightHeight = getHeightHelper(root->getRight());    // Recursive case, traverse right to find the height of the right subtree
        if(leftHeight > rightHeight)                            // Return the larger height
        {
            return leftHeight + 1;
        }
        else
        {
            return rightHeight + 1;
        }
    }
}

template<class T>
int BST<T>::getHeight()     // Returns the height of the tree, 0 if empty
{
    return getHeightHelper(root);
}

template<class T>
int BST<T>::getNumberOfNodesHelper(nodeBST<T>* root)        // Recursive helper function for getNumberOfNodes, returns the number of nodes in the tree
{
    if(root == NULL)                                        // Base case, no nodes, then return 0
    {
        return 0;
    }
    else
    {
        return 1 + getNumberOfNodesHelper(root->getLeft()) + getNumberOfNodesHelper(root->getRight());        // Recursive case, count the nodes of the left and right subtrees and add 1 for the current node
    }
}

template<class T>
int BST<T>::getNumberOfNodes()      // Returns the number of nodes in the tree
{
    return getNumberOfNodesHelper(root);
}

template<class T>
nodeBST<T>* BST<T>::getSmallerHelper(nodeBST<T>* root, T data)          // Recursive helper function for getSmaller, returns a pointer to the largest value smaller than the given value
{
    if(root == NULL)                                                    // Base case, no smaller value, then return NULL
    {
        return root;
    }
    
    if(root->getValue() >= data)                                        // Move to the left subtree if the current node is greater or equal to target
    {
        return getSmallerHelper(root->getLeft(), data);
    }
    else                                                                // Else, update the best candidate and move to the right subtree
    {
        nodeBST<T>* candidate = root;
        nodeBST<T>* rightCandidate = getSmallerHelper(root->getRight(), data);              // Get the best candidate from the right subtree
        if(rightCandidate != NULL && rightCandidate->getValue() > candidate->getValue())    // If the right subtree has a best candidate, update the best candidate
        {
            candidate = rightCandidate;
        }

        return candidate;       // Return the best candidate
    }
}

template<class T>
nodeBST<T>* BST<T>::getSmaller(T data)          // Returns a pointer to the largest value smaller than the given value or throws an exception if the tree is empty
{
    if(root != NULL)                            // If the tree is not empty, then call the helper function
    {
        return getSmallerHelper(root, data);
    }
    else                                        // If the tree is empty, throw an exception
    {
        throw "Empty tree!";
    }
}

template<class T>
nodeBST<T>* BST<T>::getLargerHelper(nodeBST<T>* root, T data)       // Recursive helper function for getLarger, returns a pointer to the smallest value larger than the given value
{
    if(root == NULL)                                                // Base case, no larger value, then return neutral element
    {
        return root;
    }
    
    if(root->getValue() <= data)                                    // Recursive case, traverse right if the current node is smaller or equal to target
    {
        return getLargerHelper(root->getRight(), data);
    }
    else                                                            // Else, update the best candidate and traverse left
    {
        nodeBST<T>* candidate = root->getValue();
        nodeBST<T>* candidateLeft = getLargerHelper(root->getLeft(), data);                 // Get the best candidate from the left subtree
        if(candidateLeft != NULL && candidateLeft->getValue() < candidate->getValue())      // If the left subtree has a best candidate, update the best candidate
        {
            candidate = candidateLeft;
        }

        return candidate;       // Return the best candidate
    }
}

template<class T>
nodeBST<T>* BST<T>::getLarger(T data)       // Returns a pointer to the smallest value larger than the given value or throws an exception if the tree is empty
{
    if(root != NULL)
    {
        return getLargerHelper(root, data);
    }
    else
    {
        throw "Empty tree!";
    }
}

template<class T>
nodeBST<T>* BST<T>::getKthSmallestHelper(nodeBST<T>* root, int k, int& count)       // Recursive helper function for getKthSmallest, returns a pointer to the k'th smallest value in the tree
{
    if(root == NULL)                                                                // Base case, no k'th smallest value, then return NULL
    {
        return root;
    }
    else
    {
        nodeBST<T>* result = getKthSmallestHelper(root->getLeft(), k, count);       // Recursive case, traverse left
        if(result != NULL)                                                          // If the left subtree has a k'th smallest value, return it
        {
            return result;
        }
        count++;
        if(count == k)                                                              // If current element is k'th smallest, return it
        {
            return root;
        }
        return getKthSmallestHelper(root->getRight(), k, count);                    // Recursive case, traverse right, if the left subtree does not have a k'th smallest value and the current element is not the k'th smallest
    }
}

template<class T>
nodeBST<T>* BST<T>::getKthSmallest(int k)     // Returns a pointer to the k'th smallest value in the tree or throws an exception if the tree is empty
{
    int count = 0;
    return getKthSmallestHelper(root, k, count);
}

template<class T>
int BST<T>::getRankHelper(nodeBST<T>* root, T data)     // Recursive helper function for getRank, returns the rank of the given value, or -1 if the value is not in the tree
{
    if(root == NULL)                                    // Base case, value not in the tree, return -1
    {
        return -1;
    }

    if(data == root->getValue())                        // If the current node is the value, return the rank of the value in the left subtree
    {
        return getNumberOfNodesHelper(root->getLeft()) + 1;
    }
    else if(data < root->getValue())                    // If the current node is greater than the value, traverse left
    {
        return getRankHelper(root->getLeft(), data);
    }
    else
    {
        int leftSize = getNumberOfNodesHelper(root->getLeft());     // Get the number of nodes in the left subtree
        int rightSize = getRankHelper(root->getRight(), data);      // Get the rank of the value in the right subtree
        return (rightSize != -1) ? leftSize + rightSize + 1 : -1;   // If the value is in the right subtree, return the sum of the number of nodes in the left subtree, the rank of the value in the right subtree, and 1
    }
}

template<class T>
int BST<T>::getRank(T data)     // Returns the rank of the given value, or -1 if the value is not in the tree
{
    return getRankHelper(root, data);
}

template<class T>
void BST<T>::clearHelper(nodeBST<T>* root)      // Recursive helper function for clear, clears the tree
{
    if(root != NULL)                            // If the current node is not NULL, then traverse left and right, then delete the node
    {
        clearHelper(root->getLeft());           // Recursive case, traverse left
        root->setLeft(NULL);                    // Set the left child to NULL
        clearHelper(root->getRight());          // Recursive case, traverse right
        root->setRight(NULL);                   // Set the right child to NULL
        delete root;
    }
}

template<class T>
void BST<T>::clear()    // Clears the tree, sets the root to NULL
{
    clearHelper(root);
    root = NULL;
}

template<class T>
nodeBST<T>* BST<T>::getRoot()       // Returns the root of the tree
{
    return root;
}

template<class T>
void BST<T>::print()              // Prints the tree in order
{
    inOrder();
}