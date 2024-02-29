#include <iostream>

using namespace std;

template<class T>
nodeBST<T>::nodeBST()
{
    right = NULL;
    left = NULL;
}

template<class T>
nodeBST<T>::nodeBST(T a)
{
    data = a;
    right = NULL;
    left = NULL;
}

template<class T>
nodeBST<T>::~nodeBST()
{
    delete right;
    delete left;
}

template<class T>
T nodeBST<T>::getValue()
{
    return data;
}

template<class T>
void nodeBST<T>::setValue(T a)
{
    data = a;
}

template<class T>
nodeBST<T>* nodeBST<T>::getRight()
{
    return right;
}

template<class T>
void nodeBST<T>::setRight(nodeBST* a)
{
    right = a;
}

template<class T>
nodeBST<T>* nodeBST<T>::getLeft()
{
    return left;
}

template<class T>
void nodeBST<T>::setLeft(nodeBST* a)
{
    left = a;
}

template<class T>
bool nodeBST<T>::isLeaf()
{
    return (right == NULL && left == NULL);
}

template<class T>
bool nodeBST<T>::hasLeft()
{
    return (left != NULL);
}

template<class T>
bool nodeBST<T>::hasRight()
{
    return (right != NULL);
}

template<class T>
void nodeBST<T>::print()
{
    cout << data << endl;
}