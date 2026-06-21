/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package datastructure;

import model.GameObject;

/**
 *
 * @author Nguyen Minh Phat - CE201621
 */
public class CustomLinkedList {

    public class Node {

        public GameObject data;
        public Node next;
        public Node prev;

        public Node(GameObject data) {
            this.data = data;
            this.next = null;
            this.prev = null;
        }
    }
    public Node head = null;
    public Node tail = null;

    public void addLast(GameObject obj) {
        Node newNode = new Node(obj);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
    }

    public void removeNode(Node n) {
        if (n == null) {
            return;
        }

        if (n == head) {
            head = n.next;
        }
        if (n == tail) {
            tail = n.prev;
        }

        if (n.prev != null) {
            n.prev.next = n.next;
        }
        if (n.next != null) {
            n.next.prev = n.prev;
        }
    }
}
