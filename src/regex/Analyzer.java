package regex;

import java.util.ArrayList;

/**
 * @author SuperMaxine
 */
public class Analyzer {

    private static ArrayList<Integer> captureGroupsLen;

    public Analyzer(Pattern p){
        captureGroupsLen = new ArrayList<>(p.capturingGroupCount);
    }

    private static class snapshotInLenCalc{
        int len;
        snapshotInLenCalc(){
            len = 0;
        }
    }

    public static int calcLen(Pattern.Node root){
        ArrayList<Pattern.Node> stack = new ArrayList<>();
        ArrayList<snapshotInLenCalc> snapshotStack = new ArrayList<>();
        stack.add(root);
        snapshotStack.add(new snapshotInLenCalc());

        while(stack.size()!=0){
            // 递归终点，空返回0
            if (root == null) {
                return 0;
            }
        }

        return 0;
    }
    public static int calcLenDG(Pattern.Node root){
        // 递归终点，空返回0
        if (root == null) {
            return 0;
        }

        // 根据len表格进行不同情况处理
        int len = 0;

        // <char> 和 <set> 返回1
        if (root instanceof Pattern.CharProperty)
        {
            len = 1;
        }
        else if (root instanceof Pattern.SliceNode)
        {
            len = ((Pattern.SliceNode) root).buffer.length;
        }
        else if (root instanceof Pattern.BnM)
        {
            len = ((Pattern.BnM) root).buffer.length;
        }

        // lookaround返回0
        else if (root instanceof Pattern.Pos ||
                 root instanceof Pattern.Neg ||
                 root instanceof Pattern.Behind ||
                 root instanceof Pattern.NotBehind)
        {
            len = 0;
        }

        // 分支返回最大值
        else if (root instanceof Pattern.Branch)
        {
            for (int i = 0 ; i < ((Pattern.Branch) root).atoms.length; i++) {
                int tmp = calcLen(((Pattern.Branch) root).atoms[i]);
                if (tmp > len) {
                    len = tmp;
                }
            }
        }

        // 返回Len(r1)，即不做任何操作继续递归
        else if (root instanceof Pattern.Ques)
        {
            len = calcLen(((Pattern.Ques) root).atom);
        }
        else if (root instanceof Pattern.BackRef)
        {
            // TODO: 需要记录对应捕获组的len
            // 问题，如果捕获组还没解析完怎么办？
        }
        else if(root instanceof Pattern.Dollar ||
                root instanceof Pattern.Bound ||
                root instanceof Pattern.GroupHead)
        {
            // 不做任何操作，后面的节点在next中
        }

        else if (root instanceof Pattern.GroupTail)
        {
            captureGroupsLen.set(((Pattern.GroupTail) root).groupIndex / 2, len);
        }

        // 返回Len(r1) * m 或 Len(r2) * (m+1) 或 Len(r3) * 2
        else if (root instanceof Pattern.Curly)
        {
            if(((Pattern.Curly) root).cmin == ((Pattern.Curly) root).cmax)
            {
                len = calcLen(((Pattern.Curly) root).atom) * ((Pattern.Curly) root).cmax;
            }
            else
            {
                len = calcLen(((Pattern.Curly) root).atom) * (((Pattern.Curly) root).cmax + 1);
            }
        }

        // 没有预料到的节点，抛出异常
        else{
            throw new RuntimeException("unexpected node type: " + root.getClass().getName());
        }

        // 拼接返回相加
        if (root.next != null) {
            len += calcLen(root.next);
        }
        return len;
    }

    public static int returnPath(Pattern.Node root){

    }
}
