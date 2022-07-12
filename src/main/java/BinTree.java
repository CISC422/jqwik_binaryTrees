 /* CISC/CMPE 422/835
 * Simple implementation of binary trees
 */
public class BinTree {
    public int key;
    public final String value;
    public BinTree left = null;
    public BinTree right = null;

    public BinTree(int key, String val, BinTree l, BinTree r) {
        this.key = key;
        this.value = val;
        this.left = l;
        this.right = r;
    }

    @Override
    public String toString() {
        return String.format("[%d,%s: %s, %s]", this.key, this.value, this.left, this.right);
    }
}
