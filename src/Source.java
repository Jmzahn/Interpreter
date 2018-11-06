//Jacob Zahn R11461858, Java SDK 8, Concepts of Programing Languages
//This program reads from a user provided file in the same folder as the program.
//The file must be in lines of expressions, each lines is treated as a separate expression.
//No period needed for endln
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;

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
    private String expr;
    private int place;
    Lexer(String exp){
        this.expr=exp;
        current=null;
        place=0;
        current=get();
    }
    Token get()
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
        final Type t;
        final String c;

        Token(Type t, String c) {
            this.t = t;
            this.c = c;
        }
        Token(Token token)
        {
            this.t=token.t;
            this.c=token.c;
        }
        public String toString() {
            if (t == Type.ATOM) {
                return "ATOM<" + c + ">";
            }
            return c;
        }
        int precedence()
        {
            if(this.t==Type.ATOM)
                return 10;
            if(this.t==Type.NOTTERM)
                return 9;
            if(this.t==Type.ANDTERM)
                return 8;
            if(this.t==Type.ORTERM)
                return 7;
            if(this.t==Type.IMPLYTERM)
                return 6;
            else
                return 0;
        }
    }
    private static String getAtom(String s,int i){
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

class AST{
    LinkedList<Lexer.Token> operatorStack;
    LinkedList<Lexer.Token> operandStack;

    AST()
    {
        this.operandStack=new LinkedList<>();
        this.operatorStack=new LinkedList<>();
    }
    static Lexer.Token solve(Lexer.Token op, Lexer.Token op1, Lexer.Token op2)
    {
        if(op.t.equals(Lexer.Type.ANDTERM))
        {
            String r = op1.c;
            String l = op2.c;
            boolean ll, rr;
            ll=l.equals("T");
            rr=r.equals("T");
            if (ll && rr)
                op=new Lexer.Token(Lexer.Type.ATOM,"T");
            else
                op=new Lexer.Token(Lexer.Type.ATOM,"F");
            return op;
        }
        if(op.t.equals(Lexer.Type.ORTERM))
        {
            String r = op1.c;
            String l = op2.c;
            boolean ll, rr;
            ll=l.equals("T");
            rr=r.equals("T");
            if (ll || rr)
                op=new Lexer.Token(Lexer.Type.ATOM,"T");
            else
                op=new Lexer.Token(Lexer.Type.ATOM,"F");
            return op;
        }
        if(op.t.equals(Lexer.Type.NOTTERM))
        {
            String r = op1.c;
            boolean rr;
            rr=r.equals("T");
            if (!rr)
                op=new Lexer.Token(Lexer.Type.ATOM,"T");
            else
                op=new Lexer.Token(Lexer.Type.ATOM,"F");
            return op;
        }
        if(op.t.equals(Lexer.Type.IMPLYTERM))
        {
            String r = op1.c;
            String l = op2.c;
            boolean ll, rr;
            ll=l.equals("T");
            rr=r.equals("T");
            if (ll==true && rr==false)
                op=new Lexer.Token(Lexer.Type.ATOM,"F");
            else
                op=new Lexer.Token(Lexer.Type.ATOM,"T");
            return op;
        }
        else
            return null;
    }
}

class Interpreter
{
    private static AST ast;
    static void interpret(String exp)
    {
        Lexer lex=new Lexer(exp);
        ast=new AST();
        boolean answer=false;
        if(parse(lex))
        {
            while(!ast.operatorStack.isEmpty())
            {
                if(ast.operatorStack.peek().t.equals(Lexer.Type.NOTTERM))
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,null));
                }
                else
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    Lexer.Token v2 = ast.operandStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,v2));
                }
            }
            if(ast.operandStack.peek().c.equals("T"))
                answer=true;
            System.out.println(answer);
        }
        else
            System.err.println("Parser failed.");

    }
    private static boolean parse(Lexer lex){return b(lex);}
    private static boolean b(Lexer lex)
    {
        return it(lex);
    }
    private static boolean it(Lexer lex)
    {
        if(!ot(lex))
            return false;
        return it_Tail(lex);
    }
    private static boolean it_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.IMPLYTERM)) {
            if(ast.operatorStack.isEmpty())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else if(lex.current.precedence()>ast.operatorStack.peek().precedence())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else
            {
                if(ast.operatorStack.peek().t.equals(Lexer.Type.NOTTERM))
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,null));
                }
                else
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    Lexer.Token v2 = ast.operandStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,v2));
                }
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            lex.get();

            if (!ot(lex))
                return false;
            return it_Tail(lex);
        }

        return (lex.current.t.equals(Lexer.Type.EOL)||lex.current.t.equals(Lexer.Type.RPAREN));
    }
    private static boolean ot(Lexer lex)
    {
        if(!at(lex))
            return false;
        return ot_Tail(lex);

    }
    private static boolean ot_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ORTERM))
        {
            if(ast.operatorStack.isEmpty())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else if(lex.current.precedence()>ast.operatorStack.peek().precedence())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else
            {
                if(ast.operatorStack.peek().t.equals(Lexer.Type.NOTTERM))
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,null));
                }
                else
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    Lexer.Token v2 = ast.operandStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,v2));
                }
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            lex.get();

            if(!at(lex))
                return false;
            return ot_Tail(lex);

        }
        return (lex.current.t.equals(Lexer.Type.EOL)||lex.current.t.equals(Lexer.Type.IMPLYTERM)||lex.current.t.equals(Lexer.Type.RPAREN));
    }
    private static boolean at(Lexer lex)
    {
        if(!l(lex))
            return false;
        return at_Tail(lex);
    }
    private static boolean at_Tail(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ANDTERM))
        {
            if(ast.operatorStack.isEmpty())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else if(lex.current.precedence()>ast.operatorStack.peek().precedence())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else
            {
                if(ast.operatorStack.peek().t.equals(Lexer.Type.NOTTERM))
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,null));
                }
                else
                {
                    Lexer.Token v1 = ast.operandStack.pop();
                    Lexer.Token op = ast.operatorStack.pop();
                    Lexer.Token v2 = ast.operandStack.pop();
                    ast.operandStack.push(AST.solve(op,v1,v2));
                }
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            lex.get();

            if(l(lex))
                return at_Tail(lex);
            return false;
        }
        return (lex.current.t.equals(Lexer.Type.EOL)||lex.current.t.equals(Lexer.Type.ORTERM)||lex.current.t.equals(Lexer.Type.IMPLYTERM)||lex.current.t.equals(Lexer.Type.RPAREN));
    }
    private static boolean l(Lexer lex)
    {
        if(a(lex))
            return true;
        if(lex.current.t.equals(Lexer.Type.NOTTERM)){

            if(ast.operatorStack.isEmpty())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else if(lex.current.precedence()>ast.operatorStack.peek().precedence())
            {
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            else
            {
                Lexer.Token v1 = ast.operandStack.pop();
                Lexer.Token op = ast.operatorStack.pop();
                ast.operandStack.push(AST.solve(op,v1,null));
                ast.operatorStack.push(new Lexer.Token(lex.current));
            }
            lex.get();

            return l(lex);
        }
        return false;
    }
    private static boolean a(Lexer lex)
    {
        if(lex.current.t.equals(Lexer.Type.ATOM)){
            if(lex.current.c.equals("T")){
                ast.operandStack.push(new Lexer.Token(lex.current));
                lex.get();
                return true;
            }
            else if(lex.current.c.equals("F")){
                ast.operandStack.push(new Lexer.Token(lex.current));
                lex.get();
                return true;
            }
            return false;
        }
        if(lex.current.t.equals(Lexer.Type.LPAREN))
        {
            ast.operatorStack.push(new Lexer.Token(lex.current));
            lex.get();
            if(!it(lex))
                return false;
            if(lex.current.t.equals(Lexer.Type.RPAREN))
            {
                while(!ast.operatorStack.peek().t.equals(Lexer.Type.LPAREN))
                {
                    if(ast.operatorStack.peek().t.equals(Lexer.Type.NOTTERM))
                    {
                        Lexer.Token v1 = ast.operandStack.pop();
                        Lexer.Token op = ast.operatorStack.pop();
                        ast.operandStack.push(AST.solve(op,v1,null));
                    }
                    else
                    {
                        Lexer.Token v1 = ast.operandStack.pop();
                        Lexer.Token op = ast.operatorStack.pop();
                        Lexer.Token v2 = ast.operandStack.pop();
                        ast.operandStack.push(AST.solve(op,v1,v2));
                    }
                }
                ast.operatorStack.pop();
                lex.get();
                return true;
            }
        }
        return false;
    }
}
