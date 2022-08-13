import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.util.*;

public class PoolangInterpreter extends PoolangBaseVisitor<Object> {

    private static class ScopeData {
//        public final String methodID;
        private final Map<String, Integer> intScope = new HashMap<>();
        private final Map<String, Float> floatScope = new HashMap<>();
        private final Map<String, Boolean> boolScope = new HashMap<>();

//        public ScopeInfo(String methodID) {
//            this.methodID = methodID;
//        }
        public ScopeData putInt(String id, int val){
            intScope.put(id, val);
            return this;
        }
        public ScopeData putBool(String id, boolean val){
            boolScope.put(id, val);
            return this;
        }
        public ScopeData putFloat(String id, float val){
            floatScope.put(id, val);
            return this;
        }
        public Integer getInt(String id){return intScope.get(id);}
        public Float getFloat(String id){return floatScope.get(id);}
        public Boolean getBool(String id){return boolScope.get(id);}
    }
    private static class FunctionData{
        public final ParseTree prototypeTree;
        public final String returnType;
        private final Map<Integer, String> positionalMapping = new HashMap<>();
        private final Map<String, String> typeMapping = new HashMap<>();

        public FunctionData(String returnType, ParseTree prototypeTree) {
            this.returnType = returnType;
            this.prototypeTree = prototypeTree;
        }

        public String getIdFromPos(int pos){
            return positionalMapping.get(pos);
        }

        public FunctionData putMapping(int pos, String id, String type){
            positionalMapping.put(pos, id);
            typeMapping.put(id, type);
            return this;
        }
    }

    private Map<String, FunctionData> functions = new HashMap<>();
    private Stack<ScopeData> scopes = new Stack<>();

    private Integer getInt(String id){
//        return ints.get(parameterScopes.peek() + "." + id);
        return scopes.peek().intScope.get(id);
    }
    private Boolean getBool(String id){
//        return bools.get(scopes.peek() + "." + id);
        return scopes.peek().boolScope.get(id);
    }
    private Float getFloat(String id){
//        return floats.get(scopes.peek() + "." + id);
        return scopes.peek().floatScope.get(id);
    }

    private void setInt(String id, int val){
//        ints.put(scopes.peek() + "." + id, val);
        scopes.peek().intScope.put(id, val);
    }
    private void setBool(String id, boolean val){
//        bools.put(scopes.peek() + "." + id, val);
        scopes.peek().boolScope.put(id, val);
    }
    private void setFloat(String id, float val){
//        floats.put(scopes.peek() + "." + id, val);
        scopes.peek().floatScope.put(id, val);
    }

//    @Override
//    public Object visitNumber(PoolangParser.NumberContext ctx) {
//        return Float.parseFloat(ctx.getText());
//    }


    @Override
    public Object visitFloat(PoolangParser.FloatContext ctx) {
        return Float.parseFloat(ctx.getText());
    }

    @Override
    public Object visitInteger(PoolangParser.IntegerContext ctx) {
        return Integer.parseInt(ctx.getText());
    }

    @Override
    public Object visitAdditionOrSubtraction(PoolangParser.AdditionOrSubtractionContext ctx) {
        Object left = visit(ctx.left);
        Object right = visit(ctx.right);
        boolean leftFloat = left instanceof Float;
        boolean rightFloat = right instanceof Float;
        switch(ctx.operator.getText().intern()){
            case "+":
                if(leftFloat && rightFloat)return (float)left + (float)right;
                else if(!leftFloat && rightFloat) return (int)left + (float)right;
                else if(leftFloat && !rightFloat) return (float)left + (int)right;
                else if(!leftFloat && !rightFloat) return (int)left + (int)right;
                break;
            case "-":
                if(leftFloat && rightFloat)return (float)left - (float)right;
                else if(!leftFloat && rightFloat) return (int)left - (float)right;
                else if(leftFloat && !rightFloat) return (float)left - (int)right;
                else if(!leftFloat && !rightFloat) return (int)left - (int)right;
                break;
        }
        return -1.0f;
    }

    @Override
    public Object visitMultiplicationOrDivision(PoolangParser.MultiplicationOrDivisionContext ctx) {
        Object left = visit(ctx.left);
        Object right = visit(ctx.right);
        boolean leftFloat = left instanceof Float;
        boolean rightFloat = right instanceof Float;
        switch(ctx.operator.getText().intern()){
            case "*":
                if(leftFloat && rightFloat)return (float)left * (float)right;
                else if(!leftFloat && rightFloat) return (int)left * (float)right;
                else if(leftFloat && !rightFloat) return (float)left * (int)right;
                else if(!leftFloat && !rightFloat) return (int)left * (int)right;
                break;
            case "/":
                if(leftFloat && rightFloat)return (float)left / (float)right;
                else if(!leftFloat && rightFloat) return (int)left / (float)right;
                else if(leftFloat && !rightFloat) return (float)left / (int)right;
                else if(!leftFloat && !rightFloat) return (int)left / (int)right;
                break;
        }
        return -1.0f;
    }

    @Override
    public Object visitPower(PoolangParser.PowerContext ctx) {
        Object left = visit(ctx.left);
        Object right = visit(ctx.right);
        boolean leftFloat = left instanceof Float;
        boolean rightFloat = right instanceof Float;
        switch(ctx.operator.getText().intern()){
            case "^":
                if(leftFloat && rightFloat)return Math.pow((float)left, (float)right);
                else if(!leftFloat && rightFloat) return Math.pow((int)left, (float)right);
                else if(leftFloat && !rightFloat) return Math.pow((float)left, (int)right);
                else if(!leftFloat && !rightFloat) return Math.pow((int)left, (int)right);
                break;
        }
        return -1.0f;
    }

    @Override
    public Object visitParentheses(PoolangParser.ParenthesesContext ctx) {
        return visit(ctx.inner);
    }


    @Override
    public Object visitBooleanAssign(PoolangParser.BooleanAssignContext ctx) {
        String ID = ctx.identifier().getText().intern();
        boolean value = (boolean) visit(ctx.cond_expr());
//        bools.put(ID, value);
        setBool(ID, value);
        return value;
    }

    @Override
    public Object visitIntegerAssign(PoolangParser.IntegerAssignContext ctx) {
        String ID = ctx.identifier().getText().intern();
//        System.out.println(ctx.arith_expr().getText());
        int value = (int) (float)visit(ctx.arith_expr());
//        ints.put(ID, value);
        setInt(ID, value);
        return value;
    }

    @Override
    public Object visitFloatAssign(PoolangParser.FloatAssignContext ctx) {
        String ID = ctx.identifier().getText().intern();
        float value = (float) visit(ctx.arith_expr());
//        floats.put(ID, value);
        setFloat(ID, value);
        return value;
    }

    @Override
    public Object visitUnsafeAssign(PoolangParser.UnsafeAssignContext ctx) {
        String ID = ctx.identifier().getText().intern();
        Object value = visit(ctx.expression());
        switch(ctx.primitive_types().getText().intern()){
            //just assume variable type is correct
            case "float":
//                floats.put(ID, (float)value);
                setFloat(ID, (float)value);
                break;
            case "int":
//                ints.put(ID, (int)value);
                setInt(ID, (int)value);
                break;
            case "bool":
//                bools.put(ID, (boolean)value);
                setBool(ID, (boolean)value);
                break;
            default:
                break;
        }
        return value;
    }

    //  main(int a, float dababy){}
    @Override
    public Object visitFunc_decl(PoolangParser.Func_declContext ctx) {
        String returnType = ctx.primitive_types(0).getText();
        FunctionData functionData = new FunctionData(returnType, ctx.block());
        for(int i=1; i<ctx.primitive_types().size(); i++){
            String paramType = ctx.primitive_types(i).getText();
            String paramID = ctx.identifier(i).getText();
            functionData.putMapping(i-1, paramID, paramType);
        }
        functions.put(ctx.identifier(0).getText().intern(), functionData);
        return null;
    }

    @Override
    public Object visitNormalFuncCall(PoolangParser.NormalFuncCallContext ctx) {
        String functionID = ctx.funcCall_expr().identifier().getText();
//        scopes.push(functionID);
        ScopeData scope = new ScopeData();
        FunctionData functionData = functions.get(functionID);
        for(int i=0;i<ctx.funcCall_expr().expression().size();i++){
            ParseTree paramExpr = ctx.funcCall_expr().expression(i);
            Object paramVal = visit(paramExpr);
            String paramID = functionData.getIdFromPos(i);
            if(paramVal instanceof Float){
                scope.putFloat(paramID, (float)paramVal);
                System.out.printf("assigned %s to %f\n", paramID, paramVal);
            }else if(paramVal instanceof Integer){
                scope.putInt(paramID, (int)paramVal);
                System.out.printf("assigned %s to %d\n", paramID, paramVal);
            }else if(paramVal instanceof Boolean){
                scope.putBool(paramID, (boolean)paramVal);
                System.out.printf("assigned %s to %b\n", paramID, paramVal);
            }

        }
        scopes.push(scope);
        Object value = visit(functions.get(functionID).prototypeTree);
        scopes.pop();
        return value;
    }

    @Override
    public Object visitPrintStringCall(PoolangParser.PrintStringCallContext ctx) {
        String value = ctx.stringLiteral().getText();
        value = value.substring(1, value.length()-1);//chop off quotations
//        System.out.println("PRINTED");
        System.out.print(value);
        return value;
    }

    @Override
    public Object visitPrintArithCall(PoolangParser.PrintArithCallContext ctx) {
        float value = (float)visit(ctx.arith_expr());
//        System.out.println("PRINTED");
        System.out.print(value);
        return value;
    }

    @Override
    public Object visitPrintCondCall(PoolangParser.PrintCondCallContext ctx) {
        boolean value = (boolean)visit(ctx.cond_expr());
        System.out.print(value);
        return value;
    }

    @Override
    public Object visitPrintReferenceCall(PoolangParser.PrintReferenceCallContext ctx) {
        String ID = ctx.identifier().getText();
        String type = ctx.primitive_types().getText();
        switch(type.intern()){
            case "int":
                System.out.print(getInt(ID));
                break;
            case "float":
                System.out.print(getFloat(ID));
                break;
            case "bool":
                System.out.print(getBool(ID));
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitNot(PoolangParser.NotContext ctx) {
        return !(boolean)visit(ctx.right);
    }

    @Override
    public Object visitTrueOrFalse(PoolangParser.TrueOrFalseContext ctx) {
        return Boolean.valueOf(ctx.getText());
    }

    @Override
    public Object visitOr(PoolangParser.OrContext ctx) {
        return (boolean)visit(ctx.left) || (boolean)visit(ctx.right);
    }

    @Override
    public Object visitAnd(PoolangParser.AndContext ctx) {
        return (boolean)visit(ctx.left) && (boolean)visit(ctx.right);
    }

    @Override
    public Object visitRelational(PoolangParser.RelationalContext ctx) {
        String operation = ctx.operation.getText().intern();
        Object left = visit(ctx.left);
        Object right = visit(ctx.right);
        switch(operation){
            case ">": return (float)left > (float)right;
            case ">=":return (float)left >= (float)right;
            case "<":return (float)left < (float)right;
            case "<=":return (float)left <= (float)right;
            case "==":return (float)left == (float)right;
            case "!=":return (float)left != (float)right;
            default: return -1;
        }
    }

//    @Override
//    public Object visitIf_stmt(CalculatorParser.If_stmtContext ctx) {
//        boolean value = (boolean)visit(ctx.cond_expr());
//        if(value){
//            visit(ctx.ifblock);
//        }else{
//            visit(ctx.elseblock);
//        }
//        return value;
//    }


    @Override
    public Object visitIf(PoolangParser.IfContext ctx) {
        boolean value = (boolean)visit(ctx.cond_expr());
        if(value){
            visit(ctx.ifblock);
        }
        return value;
    }

    @Override
    public Object visitElse(PoolangParser.ElseContext ctx) {
        boolean value = (boolean)visit(ctx.cond_expr());
        if(value){
            visit(ctx.ifblock);
        }else{
            visit(ctx.elseblock);
        }
        return value;
    }

    @Override
    public Object visitIfElif(PoolangParser.IfElifContext ctx) {
        if((boolean)visit(ctx.cond_expr(0))){
            visit(ctx.ifblock);
        }else{
            for(int i=1;i<ctx.cond_expr().size();i++){//check elifs
                if((boolean)visit(ctx.cond_expr(i))){
                    visit(ctx.block(i));
                    break;//dont visit other stuff
                }
            }
        }
        return null;
    }

    @Override
    public Object visitIfElifElse(PoolangParser.IfElifElseContext ctx) {
        if((boolean)visit(ctx.cond_expr(0))){
            visit(ctx.ifblock);
        }else{
            for(int i=1;i<ctx.cond_expr().size();i++){//check elifs
                if((boolean)visit(ctx.cond_expr(i))){
                    visit(ctx.block(i));
                    return null;//dont visit other stuff
                }
            }
            //otherwise do else block
            visit(ctx.elseblock);
        }
        return null;
    }

    @Override
    public Object visitNumericReference(PoolangParser.NumericReferenceContext ctx) {
        String type = ctx.numeric_t().getText().intern();
        String id = ctx.identifier().getText().intern();
        switch(type){
            case "int":
//                return ints.get(id);
                return getInt(id);
            case "float":
//                return floats.get(id);
                return getFloat(id);
            default:
                return null;
        }
    }

    @Override
    public Object visitBooleanReference(PoolangParser.BooleanReferenceContext ctx) {
//        return bools.get(ctx.identifier().getText().intern());
        return getBool(ctx.identifier().getText().intern());
    }

    public static void main(String[] args) throws IOException {
        String beenWonderingWhatThisIs;

//        Scanner scanner = new Scanner(System.in);
//        beenWonderingWhatThisIs = scanner.nextLine();

        beenWonderingWhatThisIs =
                "int main(float a){\n" +
                "    float b = 2.0;\n" +
                "    print(a:float + b:float);\n" +
                "}\n" +
                "main(3.0): int;";

//        System.out.printf("entered '%s'\n", beenWonderingWhatThisIs);
        CharStream input = CharStreams.fromString(beenWonderingWhatThisIs);
//        System.out.printf("entered '%s'\n", input);

        PoolangLexer lexer = new PoolangLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PoolangParser parser = new PoolangParser(tokens);
        PoolangInterpreter poolangInterpreter = new PoolangInterpreter();
        ParseTree tree = parser.start();
//        ParseTree mainCall =
        {
//            List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
//            String prettyTree = TreeUtils.toPrettyTree(tree, ruleNamesList);
//            System.out.println(prettyTree);

//            double result = (double)calculatorVisitorr.visit(tree);
            poolangInterpreter.visit(tree);
//            poolangInterpreter.visit(poolangInterpreter.functions.get("main"));
//            System.out.printf("%s = %f\n", input, result);
        }
    }
}
