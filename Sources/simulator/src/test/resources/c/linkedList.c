// A ASM version with debug prints at /assembler/linkedList.r5

#include <stddef.h>

// Define the Node structure
struct Node {
    int data;
    struct Node* next;
};

#define MAX_NODES 100 // Maximum number of nodes the list can hold
#define MAX_NODE_SIZE sizeof(struct Node) // Maximum size of each node

// Define a custom allocator
char memory[MAX_NODES * MAX_NODE_SIZE];
int memory_index = 0;

// Allocate memory from the custom allocator
void* custom_alloc(size_t size) {
    if (memory_index + size <= MAX_NODES * MAX_NODE_SIZE) {
        void* ptr = &memory[memory_index];
        memory_index += size;
        return ptr;
    } else {
        return NULL; // Out of memory
    }
}

// Free memory (not used in this implementation)
void custom_free(void* ptr) {
    // Memory is deallocated only when the program exits
}

// Initialize the head of the linked list
struct Node* head = NULL;

// Function to insert a new node at the beginning of the linked list
void insert(int data) {
    struct Node* newNode = custom_alloc(sizeof(struct Node));
    if (newNode == NULL) {
        return;
    }
    newNode->data = data;
    newNode->next = head;
    head = newNode;
}

// Function to print the linked list
void printList() {
    struct Node* temp = head;
    while (temp != NULL) {
        int volatile data = temp->data;
        temp = temp->next;
    }
}

int main() {
    // Insert some elements into the linked list
    insert(1);
    insert(2);
    insert(3);
    insert(4);
    insert(5);

    // Print the linked list
    printList();

    return 0;
}