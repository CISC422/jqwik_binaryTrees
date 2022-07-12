/* CISC/CMPE 422/835
 * Generating binary trees in Jqwik using a generator composition and recursion
 */

import net.jqwik.api.*;
        import net.jqwik.api.arbitraries.IntegerArbitrary;
        import net.jqwik.time.api.Dates;
        import net.jqwik.web.api.Web;
        import org.assertj.core.api.Assertions;

        import java.time.LocalDate;
        import java.util.ArrayList;
        import java.util.HashSet;
        import java.util.List;
        import java.util.stream.IntStream;

        import static java.lang.Math.max;
        import static java.lang.Math.pow;

public class BinTreeProperties {

    public boolean checkSorted(BinTree bt) {
        if (bt == null)
            return true;
        else if (bt.left == null && bt.right == null)
            return true;
        else if (bt.left == null)
            return (bt.key <= bt.right.key && checkSorted(bt.right));
        else
            return (bt.key >= bt.left.key && bt.key < bt.right.key
                    && checkSorted(bt.left)
                    && checkSorted(bt.right));
    }

    public boolean checkAcyclic(BinTree bt) {
        return checkAcyclic(bt, new ArrayList<>());
    }

    public boolean checkAcyclic(BinTree bt, List<BinTree> visited) {
        if (visited.contains(bt))
            return false;
        else {
            visited.add(bt);
            if (bt.left != null) {
                if (!checkAcyclic(bt.left, visited))
                    return false;
            }
            if (bt.right != null) {
                return checkAcyclic(bt.right, visited);
            }
        }
        return true;
    }

    public int numNodes(BinTree bt) {
        if (bt == null)
            return 0;
        else
            return 1 + numNodes(bt.left) + numNodes(bt.right);
    }

    public int height(BinTree bt) {
        if (bt == null)
            return 0;
        else
            return 1 + max(height(bt.left), height(bt.right));
    }

    public List<Integer> keyList(BinTree bt) {
        List<Integer> res = new ArrayList<>();
        if (bt == null)
            return res;
        else {
            res.addAll(keyList(bt.left));
            res.add(bt.key);
            res.addAll(keyList(bt.right));
            return res;
        }
    }

    // PROPERTIES: to test the generators ============================

    // property to manually inspect generated trees
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeNoChildrenGenerator1(@ForAll("binTreesNoChildren") BinTree bt) {
        System.out.println(bt);
    }

    // property to manually inspect generated trees
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeWithChildrenGenerator1(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
    }

    // generated trees always have exactly 7 nodes (fails)
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeGenerator5(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
        Assertions.assertThat(numNodes(bt)).isEqualTo(7);
    }

    // generated trees are always 'full' (succeeds)
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeWithChildrenGenerator2(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
        Assertions.assertThat(numNodes(bt)).isEqualTo((int) pow(2,height(bt))-1);
    }

    // generated trees have unique keys
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeWithChildrenGenerator3(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
        List<Integer> keyL = keyList(bt);
        int numNodes = keyL.size();
        int numDiffKeys = (new HashSet<Integer>(keyL)).size();
        Assertions.assertThat(numDiffKeys).isEqualTo(numNodes);
    }

    // generated trees satisfy search tree property
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeWithChildrenGenerator4(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
        if (bt.left != null)
            Assertions.assertThat(bt.key >= bt.left.key).isTrue();
        if (bt.right != null)
            Assertions.assertThat(bt.key >= bt.right.key).isTrue();
    }

    // generated trees are acyclic
    @Property
    @Report(Reporting.GENERATED)
    void propertyCheckBinTreeGenerator6(@ForAll("binTreesWithChildren") BinTree bt) {
        System.out.println(bt);
        Assertions.assertThat(checkAcyclic(bt)).isTrue();
    }

    // GENERATORS ================================================
    @Provide
    Arbitrary<BinTree> binTreesNoChildren() {
        return Combinators.combine(keys(), values())
                .as((i,v) -> new BinTree(i, v,null,null));
    }

    @Provide
    Arbitrary<BinTree> binTreesWithChildren() {
        int maxHeight = 2;
        Arbitrary<Integer> count = Arbitraries.integers().between(0,maxHeight);
        return count.flatMap((numLevels -> binTreesWithChildren(numLevels, binTreesNoChildren())));
    }

    @Provide
    Arbitrary<BinTree> binTreesWithChildren(int levelsLeft, Arbitrary<BinTree> binTreesWithChildren) {
        if (levelsLeft == 0)
            return binTreesWithChildren;   // reached bottom, return constructed generator
        else {                             // not reached bottom, extend generator constructed so far
            Arbitrary<BinTree> childrenAdded =
                    Combinators.combine(binTreesNoChildren(), binTreesWithChildren, binTreesWithChildren)
                            .as((btNoCs, leftBt, rightBt) -> {
                                btNoCs.left = leftBt;
                                btNoCs.right = rightBt;
                                return btNoCs;
                            });
            return binTreesWithChildren(levelsLeft-1, childrenAdded);
        }
    }

    @Provide
    Arbitrary<Integer> keys() {
//        final int maxKey = 0;
        final int maxKey = 5;
        return Arbitraries.integers().between(0,maxKey);
    }

    @Provide
    Arbitrary<String> values() {
        return Arbitraries.of("a");
//        return Arbitraries.strings()
//                .alpha()
//                .ofLength(1)
//                .map(s -> s = s.substring(0).toLowerCase());
    }

    @Provide
    Arbitrary<LocalDate> dates() {
        return Dates
                .dates()
                .atTheEarliest(LocalDate.of(1900, 1, 1))
                .atTheLatest(LocalDate.of(2021, 12, 31));
    }

}
