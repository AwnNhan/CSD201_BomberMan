package algorithm;

import model.Bomb;
import java.util.ArrayList;

public class MinHeapQueue {
    private ArrayList<Bomb> heap;

    public MinHeapQueue() {
        this.heap = new ArrayList<>();
    }

    // Thêm bom vào hàng đợi và vun đống ngược lên (Heapify Up)
    public void enqueue(Bomb b) {
        heap.add(b);
        heapifyUp(heap.size() - 1);
    }

    // Lấy quả bom sắp nổ nhất ra và cân bằng lại đống (Heapify Down)
    public Bomb dequeue() {
        if (heap.isEmpty()) return null;

        Bomb root = heap.get(0);
        Bomb lastNode = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, lastNode);
            heapifyDown(0);
        }

        return root;
    }

    // Xem quả bom đầu hàng đợi xem đã đến giờ nổ chưa (không xóa)
    public Bomb peek() {
        if (heap.isEmpty()) return null;
        return heap.get(0);
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    private void heapifyUp(int index) {
        int parentIndex = (index - 1) / 2;
        while (index > 0 && heap.get(index).getTimeToExplode() < heap.get(parentIndex).getTimeToExplode()) {
            swap(index, parentIndex);
            index = parentIndex;
            parentIndex = (index - 1) / 2;
        }
    }

    private void heapifyDown(int index) {
        int smallest = index;
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;

        if (leftChild < heap.size() && heap.get(leftChild).getTimeToExplode() < heap.get(smallest).getTimeToExplode()) {
            smallest = leftChild;
        }

        if (rightChild < heap.size() && heap.get(rightChild).getTimeToExplode() < heap.get(smallest).getTimeToExplode()) {
            smallest = rightChild;
        }

        if (smallest != index) {
            swap(index, smallest);
            heapifyDown(smallest);
        }
    }

    private void swap(int i, int j) {
        Bomb temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}