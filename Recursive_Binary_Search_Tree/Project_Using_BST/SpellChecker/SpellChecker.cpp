#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>
#include <cstdlib>
#include "Includes/BST.h"

using namespace std;

class SpellChecker
{
private:
    BST<string> dictionary;             // dictionary, which is a binary search tree
    string language;                    // language of the dictionary
    bool setLanguageFlag = false;       // flag to check whether a language is set or not

    void displayMenu();                 // displays the menu of the SpellChecker

    void displayLanguage();             // displays the current language of the dictionary
    void setLanguage();                 // sets the language of the dictionary
    void loadDictionary(string);        // loads the dictionary from a file
    void checkSpelling(string);         // checks the spelling of a sentence/word
    void checkSpellingFile(string);     // checks the spelling of a file
    void printDictionary();             // prints the dictionary

    void mistakeMenu();                 // displays the menu of the mistake menu, which is used to add a new word or remove an existing one from the dictionary
    void foundAMistake();               // displays the mistake menu and handles the user input

    void removePunctuation(string& s)   // removes punctuation from a string
    {
        for(int i = 0; i < s.length(); i++)
        {
            // check whether parsing character is punctuation or not
            if (ispunct(s[i]))
            {
                s.erase(i, 1);
                i--;
            }
        }
    }
    void clearConsole()                 // clears the console
    {
        system("cls");
    }

public:

    SpellChecker();                     // creates a SpellChecker object
    ~SpellChecker();                    // destroys a SpellChecker object
    void runSpellChecker();             // runs the SpellChecker

};

SpellChecker::SpellChecker() {}

SpellChecker::~SpellChecker() {}

void SpellChecker::displayMenu()
{
    cout << "==== Welcome to SpellChecker! ====" << endl;
    cout << "| 1. Set language                |" << endl;
    cout << "| 2. Print dictionary            |" << endl;
    cout << "| 3. Check spelling of a sent    |" << endl;
    cout << "| 4. Check spelling of a file    |" << endl;
    cout << "| 5. Display current language    |" << endl;
    cout << "| 6. Found a dictionary mistake  |" << endl;
    cout << "| 7. Exit                        |" << endl;
    cout << "==================================" << endl;
}

void SpellChecker::runSpellChecker()
{
    int choice;                 // user input
    string sentence;            // sentence to check
    string filename;            // filename to check
    bool pause = true;          // flag to check whether to pause the program or not

    do
    {
        clearConsole();        // clears the console
        displayMenu();
        pause = true;
        cout << "Enter your choice: ";
        cin >> choice;

        switch (choice)
        {
        // set language
        case 1:
            cout << "Enter language (en/de): ";
            cin >> language;
            setLanguage();
            break;

        // print dictionary
        case 2:
            printDictionary();
            break;

        // check spelling of a user input sentence
        case 3:
            if(!setLanguageFlag)
            {
                cout << "Please set language first." << endl;
            }
            else
            {
                cout << "Enter a sentence: ";
                cin.ignore();
                getline(cin, sentence);
                checkSpelling(sentence);
            }
            break;

        // check spelling of a file
        case 4:
            if(!setLanguageFlag)
            {
                cout << "Please set language first." << endl;
            }
            else
            {
                cout << "Enter a filename: ";
                cin >> filename;
                checkSpellingFile(filename);
            }
            break;

        case 5:
            displayLanguage();
            break;

        // found a mistake
        case 6:
            if(!setLanguageFlag)
            {
                cout << "Please set a language first." << endl;
            }
            else
            {
                foundAMistake();
                pause = false;
            }
            break;

        // exit SpellChecker
        case 7:
            cout << "============ Goodbye! ============" << endl;
            break;

        // Wrong user input
        default:
            cout << "Invalid choice." << endl;
            break;
        }

        // pause the program if needed
        if(pause)
        {
            system("pause");
        }
    } while (choice != 7);
}

void SpellChecker::displayLanguage()
{
    if(!setLanguageFlag)
    {
        cout << "Please set language first." << endl;
        return;
    }
    if(language == "en")
    {
        cout << "Current language: English" << endl;
    }
    else if(language == "de")
    {
        cout << "Current language: German" << endl;
    }
    else
    {
        cout << "Current language: " << language << endl;
    }
}

void SpellChecker::setLanguage()
{
    if (language == "en")
    {
        loadDictionary("Dictionaries/English_words.txt");
        cout << "Dictionary loaded." << endl;
        setLanguageFlag = true;
    }
    else if (language == "de")
    {
        loadDictionary("Dictionaries/German_words.txt");
        cout << "Dictionary loaded." << endl;
        setLanguageFlag = true;
    }
    else
    {
        cout << "Language not supported. Please try again." << endl;
        setLanguageFlag = false;
    }
}

void SpellChecker::loadDictionary(string fileName)
{
    ifstream file(fileName);
    string word;

    dictionary.clear();

    while(file >> word)
    {
        try
        {
            dictionary.insert(word);
        }
        catch(const char* e)
        {
            cerr << e << endl;
        }
    }

    file.close();
}

void SpellChecker::printDictionary()
{
    // check whether the language is set or not
    if(!setLanguageFlag)
    {
        cout << "Please set language first." << endl;
        return;
    }
    // print the dictionary
    dictionary.inOrder();
    cout << endl;
}

void SpellChecker::checkSpelling(string senctence)
{
    stringstream ss(senctence);     // string stream to parse the sentence
    string word;
    bool wrong = false;             // flag to check whether there is a spelling mistake or not

    while(ss >> word)
    {
        removePunctuation(word);        // remove punctuation from the word

        if(!dictionary.search(word))    // check whether the word is in the dictionary or not
        {
            if(!wrong)
            {
                cout << "Spelling errors found:" << endl;
            }
            wrong = true;
            cout << word << endl;       // print the misspelled word
        }
    }
    if(!wrong)                          // if there is no spelling mistake
    {
        cout << "No spelling errors found." << endl;
    }
}

void SpellChecker::checkSpellingFile(string filename)
{
    ifstream file(filename);

    if(!file.is_open())     // check whether the file is open or not
    {
        cout << "File not found. Please try again." << endl;
        return;
    }

    string word;
    bool wrong = false;
    int counter = 0;

    while(file >> word)
    {
        removePunctuation(word);        // remove punctuation from the word

        if(!dictionary.search(word))
        {
            counter++;                  // increment the counter, if there is a spelling mistake
            wrong = true;
        }
    }
    // print the result
    if (!wrong)
    {
        cout << "No spelling errors found." << endl;
    }
    else
    {
        cout << "Total spelling errors: " << counter << endl;
    }

    file.close();
}

void SpellChecker::mistakeMenu()
{
    cout << "==== Select the mistake type! ====" << endl;
    cout << "| 1. Missing word!               |" << endl;
    cout << "| 2. Wrong word in dictionary!   |" << endl;
    cout << "| 3. Exit                        |" << endl;
    cout << "==================================" << endl;
}

void SpellChecker::foundAMistake()
{
    int mistake;
    string word;

    do
    {
        clearConsole();
        mistakeMenu();
        cout << "Enter mistake number: ";
        cin >> mistake;
        switch (mistake)
        {
        // there is a missing word from the dictionary
        case 1:
            cout << "Give the missing word: ";
            cin >> word;
            // try to insert the word to the dictionary
            try
            {
                dictionary.insert(word);
            }
            catch(const char* e)        // if the word is already in the dictionary
            {
                cerr << e << endl;
            }

            break;

        // there is a wrong word in the dictionary
        case 2:
            cout << "Give the wrongly spelled word: ";
            cin >> word;
            // try to remove the word from the dictionary
            dictionary.remove(word);

            break;

        // exit the menu
        case 3:
            cout << "Thank you for your contribution!" << endl;
            break;

        // wrong user input
        default:
            cout << "Invalid choice!" << endl;
            break;
        }
        system("pause");
    } while (mistake != 3);
    
}

int main()
{
    SpellChecker sc;
    sc.runSpellChecker();

    return 0;
}