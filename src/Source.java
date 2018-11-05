//Jacob Zahn R11461858, Java SDK 8, Concepts of Programing Languages
//This program reads from a user provided file in the same folder as the program.
//The file must be in lines of expressions, each lines is treated as a separate expression.
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class Source{
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
    Token current;
    String expr;
    int place;
    Lexer(String exp){
        this.expr=exp;
        current=null;
        place=0;
        current=get();
    }
    public Token get()
    {
        for (;place<expr.length();) {
            switch (expr.charAt(place))
            {
                case '(':
                    current = new Token(Type.LPAREN, "(");
                    place++;
                    return current;
                case ')':
                    current = new Token(Type.RPAREN, ")");
                    place++;
                    return current;
                case '~':
                    current = new Token(Type.NOTTERM, "~");
                    place++;
                    return current;
                case '^':
                    current = new Token(Type.ANDTERM, "^");
                    place++;
                    return current;
                case 'v':
                    current = new Token(Type.ORTERM, "v");
                    place++;
                    return current;
                case '-':
                    current = new Token(Type.IMPLYTERM, "->");
                    place++;
                    place++;
                    return current;
                default:
                    if (Character.isWhitespace(expr.charAt(place))) {
                        place++;
                    } else {
                        String atom = getAtom(expr, place);
                        place += atom.length();
                        current = new Token(Type.ATOM, atom);
                        return current;
                    }
            }
        }
        current=new Token(Type.EOL,"");
        return current;
    }


    public enum Type{
        LPAREN, RPAREN, NOTTERM, ORTERM, ANDTERM, IMPLYTERM, ATOM, EOL
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
            return c;
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
}
/*class OperatorStack
{
    OperatorNode head;
    OperatorNode tail;
    int sz,cnt;
    OperatorStack()
}*/

class Node{
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
        if(b(lexed))
        {
            answer =ast.solve();
            System.out.println(answer);
        }


    }
    static boolean b(Lexer lex)
    {
        if(it(lex))
            return true;
        return false;
    }
    static boolean it(Lexer lex)
    {
        if(ot(lex)) {
            if (it_Tail(lex))
                return true;
            return false;
        }
        return false;
    }
    static boolean it_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.IMPLYTERM))
        {
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex.get();

            if(ot(lex)) {
                if (it_Tail(lex))
                    return true;
                return false;
            }
            return false;
        }
        if(lex.current.c.equals("")||lex.current.t.equals(Lexer.Type.RPAREN))
            return true;
        return false;
    }
    static boolean ot(Lexer lex)
    {
        if(at(lex)) {
            if (ot_Tail(lex))
                return true;
            else
                return false;
        }
        return false;
    }
    static boolean ot_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ORTERM))
        {
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex.get();

            if(at(lex)) {
                if (ot_Tail(lex))
                    return true;
            }
            return false;
        }
        if(lex.current.c.equals("")||lex.current.t.equals(Lexer.Type.IMPLYTERM)||lex.current.t.equals(Lexer.Type.RPAREN))
            return true;
        return false;
    }
    static boolean at(Lexer lex)
    {
        if(l(lex)) {
            if (at_Tail(lex))
                return true;
            else return false;
        }
        return false;
    }
    static boolean at_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ANDTERM)){
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            OperatorNode op2=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1,op2));
            lex.get();

            if(l(lex)){
                if(at_Tail(lex))
                    return true;
                return false;
            }
            return false;
        }
        if(lex.current.c.equals("")||lex.current.t.equals(Lexer.Type.ORTERM)||lex.current.t.equals(Lexer.Type.IMPLYTERM)||lex.current.t.equals(Lexer.Type.RPAREN))
            return true;
        return false;
    }
    static boolean l(Lexer lex)
    {
        if(a(lex))
            return true;
        if(lex.current.t.equals(Lexer.Type.NOTTERM)){
            String op=ast.operatorStack.pop().content;
            OperatorNode op1=new OperatorNode(ast.operandStack.pop().content);
            ast.operatorStack.push(new OperatorNode(op,op1));
            lex.get();

            if(l(lex))
                return true;
            return false;
        }
        return false;
    }
    static boolean a(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ATOM)){
            if(lex.current.c.equals("T")){
                ast.operandStack.push(new OperandNode(lex.current.c));
                lex.get();
                return true;
            }
            if(lex.current.c.equals("F")){
                ast.operandStack.push(new OperandNode(lex.current.c));
                lex.get();
                return true;
            }
            return false;
        }
        if(lex.current.t.equals(Lexer.Type.LPAREN)){
            ast.operatorStack.push(new OperatorNode(lex.current.c));
            lex.get();
            if(it(lex))
            {
                if (lex.current.t.equals(Lexer.Type.RPAREN))
                {
                    ast.operatorStack.push(new OperatorNode(lex.current.c));
                    lex.get();
                    return true;
                }
                return false;
            }
            return false;
        }
        return false;
    }
}
