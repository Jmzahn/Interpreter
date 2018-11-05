//Jacob Zahn R11461858, Java SDK 8.1, Concepts of Programing Languages
//This program reads from a user provided file in the same folder as the program.
//The file must be in lines of expressions, each lines is treated as a separate expression.
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class source{
    public static void main(String[] a){

        //prompt user for filename
        Scanner in = new Scanner(System.in);
        System.out.print("Enter Filename:");
        String filename=in.nextLine();
        System.out.println();
        filename=filename.trim();

        if(Files.isReadable(Paths.get(filename))){
            try{
                List<String> expressions=Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
                for(int i=0;i<expressions.size();i++)
                {
                    String exp=expressions.get(i);
                    exp=exp.trim();
                    System.out.println(exp);
                    Interpreter.interpret(exp);
                }

            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("If your seeing this something went wrong refer to the exception above.");
                System.exit(0);//catastrophic failure
            }
        }
        else {
            System.out.println("file not read");
        }
    }
}

class Lexer{
    List<Token> l;
    Lexer(String exp){
        this.l=lex(exp);
    }
    Token get()
    {
        return l.get(0);
    }
    Token get(Token prev)
    {
        for (int i=0;i<this.l.size()-1;i++){
            if(this.l.get(i).equals(prev))
                return this.l.get(i+1);
        }
        return new Token(Type.ATOM,"");
    }

    public enum Type{
        LPAREN, RPAREN, NOTTERM, ORTERM, ANDTERM, IMPLYTERM, ATOM,
    }

    public static class Token {
        public final Type t;
        public final String c;

        public Token(Type t, String c) {
            this.t = t;
            this.c = c;
        }

        public String toString() {
            if (t == Type.ATOM) {
                return "ATOM<" + c + ">";
            }
            return t.toString();
        }
    }
    public static String getAtom(String s,int i){
        int j=i;
        while(j<s.length())
        {
            if(Character.isLetter(s.charAt(j)))
                j++;
            else
                return s.substring(i,j);
        }
        return s.substring(i,j);
    }
    public static List<Token> lex(String input){
        List<Token> result = new ArrayList<>();
        for (int i=0;i<input.length();)
        {
            switch (input.charAt(i)){
                case '(':
                    result.add(new Token(Type.LPAREN,"("));
                    i++;
                    break;
                case ')':
                    result.add(new Token(Type.RPAREN,")"));
                    i++;
                    break;
                case'~':
                    result.add(new Token(Type.NOTTERM,"~"));
                    i++;
                    break;
                case'^':
                    result.add(new Token(Type.ANDTERM,"^"));
                    i++;
                    break;
                case'v':
                    result.add(new Token(Type.ORTERM,"v"));
                    i++;
                    break;
                case'-':
                    result.add(new Token(Type.IMPLYTERM,"->"));
                    i++;
                    i++;
                    break;
                default:
                    if(Character.isWhitespace(input.charAt(i)))
                    {
                        i++;
                    }
                    else {
                        String atom = getAtom(input, i);
                        i+=atom.length();
                        result.add(new Token(Type.ATOM, atom));
                    }
                    break;
            }
        }
        return result;
    }

}
/*class OperatorStack
{
    OperatorNode head;
    OperatorNode tail;
    int sz,cnt;
    OperatorStack()
}*/

class Node extends AST{
    String content;
}

class OperandNode extends Node{
    OperandNode(String s)
    {
        this.content=s;
    }
}

class OperatorNode extends Node{
    OperatorNode op1;
    OperatorNode op2;
    OperatorNode(String s)
    {
        this.content=s;
        this.op1=null;
        this.op2=null;
    }
    OperatorNode(String s,OperatorNode o1)
    {
        this.content=s;
        this.op1=o1;
        this.op2=null;
    }
    OperatorNode(String s,OperatorNode o1, OperatorNode o2)
    {
        this.content=s;
        this.op1=o1;
        this.op2=o2;
    }
}

class AST{
    Stack<OperatorNode> operatorStack;
    Stack<OperandNode> operandStack;
    AST()
    {
        this.operandStack=new Stack<>();
        this.operatorStack=new Stack<>();
    }
    boolean solve()
    {
        return iterativePostOrder(this.operatorStack.pop());
    }
    boolean iterativePostOrder(OperatorNode n)
    {
        Stack<OperatorNode> s=new Stack<>();
        OperatorNode lastNodeVisited=null;
        while (!s.isEmpty()||n!=null)
        {
            if(n!=null){
                s.push(n);
                n=n.op1;
            }
            else {
                OperatorNode peekN=s.peek();
                if(peekN.op2!=null&&lastNodeVisited!=peekN.op2)
                    n=peekN.op2;
                else
                {
                    visit(peekN);
                    lastNodeVisited=s.pop();
                }
            }

        }
        if(lastNodeVisited.content.equals("T"))
            return true;
        else
            return false;
    }
    void visit(OperatorNode n)
    {
        if(n.content.equals("^"))
        {
            String l = n.op1.content;
            String r = n.op2.content;
            boolean ll, rr;
            if (l.equals("T"))
                ll = true;
            else
                ll = false;
            if (r.equals("T"))
                rr = true;
            else
                rr = false;
            if (ll && rr)
                n.content = "T";
            else
                n.content = "F";
            n.op1=null;
            n.op2=null;
        }
        if(n.content.equals("v")) {

            String l = n.op1.content;
            String r = n.op2.content;
            boolean ll, rr;
            if (l.equals("T"))
                ll = true;
            else
                ll = false;
            if (r.equals("T"))
                rr = true;
            else
                rr = false;
            if (ll || rr)
                n.content = "T";
            else
                n.content = "F";
            n.op1=null;
            n.op2=null;
        }
        if(n.content.equals("->")) {

            String l = n.op1.content;
            String r = n.op2.content;
            boolean ll, rr;
            if (l.equals("T"))
                ll = true;
            else
                ll = false;
            if (r.equals("T"))
                rr = true;
            else
                rr = false;
            if ((ll==false) && (rr==true))
                n.content = "F";
            else
                n.content = "T";
            n.op1=null;
            n.op2=null;
        }
        if(n.content.equals("~"))
        {
            String l = n.op1.content;
            if (l.equals("T"))
            {
                n.content="F";
                n.op1=null;
            }
            else
            {
                n.content="T";
                n.op1=null;
            }
        }


    }
}

class Interpreter{

    static Lexer lexed;
    static AST ast;
    public static void interpret(String exp){
        lexed=new Lexer(exp);
        ast=new AST();
        boolean answer;
        if(b(lexed.get()))
        {
            answer =ast.solve();
            System.out.println(answer);
        }


    }
    static boolean b(Lexer.Token lex)
    {
        if(it(lex))
            return true;
        return false;
    }
    static boolean it(Lexer.Token lex)
    {
        if(ot(lex)) {
            if (it_Tail(lex))
                return true;
            return false;
        }
        return false;
    }
    static boolean it_Tail(Lexer.Token lex)
    {
        if(lex.t.equals(Lexer.Type.IMPLYTERM))
        {
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex=lexed.get(lex);

            if(ot(lex)) {
                if (it_Tail(lex))
                    return true;
                return false;
            }
            return false;
        }
        if(lex.c.equals(""))
            return true;
        return false;
    }
    static boolean ot(Lexer.Token lex)
    {
        if(at(lex)) {
            if (ot_Tail(lex))
                return true;
            else
                return false;
        }
        return false;
    }
    static boolean ot_Tail(Lexer.Token lex)
    {
        if(lex.t.equals(Lexer.Type.ORTERM))
        {
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex=lexed.get(lex);

            if(at(lex)) {
                if (ot_Tail(lex))
                    return true;
            }
            return false;
        }
        if(lex.c.equals(""))
            return true;
        return false;
    }
    static boolean at(Lexer.Token lex)
    {
        if(l(lex)) {
            if (at_Tail(lex))
                return true;
            else return false;
        }
        return false;
    }
    static boolean at_Tail(Lexer.Token lex)
    {
        if(lex.t.equals(Lexer.Type.ANDTERM)){
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex=lexed.get(lex);

            if(l(lex)){
                if(at_Tail(lex))
                    return true;
                return false;
            }
            return false;
        }
        if(lex.c.equals(""))
            return true;
        return false;
    }
    static boolean l(Lexer.Token lex)
    {
        if(a(lex))
            return true;
        if(lex.t.equals(Lexer.Type.NOTTERM)){
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1));
            lex=lexed.get(lex);

            if(l(lex))
                return true;
            return false;
        }
        return false;
    }
    static boolean a(Lexer.Token lex)
    {
        if(lex.t.equals(Lexer.Type.ATOM)){
            if(lex.c.equals("T")){
                ast.operandStack.push(new OperandNode(lex.c));
                lex=lexed.get(lex);
                return true;
            }
            if(lex.c.equals("F")){
                ast.operandStack.push(new OperandNode(lex.c));
                lex=lexed.get(lex);
                return true;
            }
            return false;
        }
        if(lex.t.equals(Lexer.Type.LPAREN)){
            ast.operatorStack.push(new OperatorNode(lex.c));
            lex=lexed.get(lex);
            if(it(lex))
            {
                if (lex.t.equals(Lexer.Type.RPAREN))
                {
                    ast.operatorStack.push(new OperatorNode(lex.c));
                    lex=lexed.get(lex);
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
