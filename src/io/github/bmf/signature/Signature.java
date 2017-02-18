package io.github.bmf.signature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.bmf.mapping.Mapping;
import io.github.bmf.util.Box;

public abstract class Signature {
    protected Map<String, Box<String>> genericLabelMap;
    protected SigArg type;
    
    public abstract String toSignature();

    public static void main(String[] args) {
        // methods
        //
        // String in =
        // "<V:Ljava/lang/Object;K:Ljava/lang/Object;>(TV;TK;Ljava/lang/String;)TV;";
        // String in = "()Ljava/util/Map<Ljava/lang/String;Ljava/util/Map<Ljava/lang/Integer;Ljava/lang/Float;>;>;";
        // String in = "<V:Ljava/lang/Object;K:Ljava/lang/Object;>(TV;TK;)TV;";
        // String in = "<V:Ljava/lang/Object;>(TV;)TV;";
        //
        //
        // fields/variables
        //
        // String in = "Ljava/util/Map<TT;TZ;>;";
        String in = "<V:Ljava/lang/Object;K:Ljava/lang/Object;>(TV;TK;)TV;";
        Mapping mapping = new Mapping();
        System.out.println(in);
        System.out.println("======================");
        try {
            System.out.println(method(mapping, in).toSignature());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("======================");
    }

    public static Signature method(Mapping mapping, String sig) {
        Map<String, Box<String>> genericLabelMap = null;
        // Independent generic
        if (sig.startsWith("<")) {
            int end = sig.lastIndexOf(">");
            genericLabelMap = new HashMap<>();
            String sub = sig.substring(1, end - 1);
            String split[] = sub.split(";");
            for (String s : split) {
                String split2[] = s.split(":");
                genericLabelMap.put(split2[0], mapping.getClassName(split2[1].substring(1)));
            }
            sig = sig.substring(end + 1);
        }
        int argEndIndex = sig.indexOf(')');
        String strArgs = sig.substring(1, argEndIndex);
        String strRet = sig.substring(argEndIndex + 1);
        List<SigArg> parameters = readSigArgs(mapping, strArgs);
        SigArg retType = readSigClass(mapping, strRet);
        return new MethodSignature(genericLabelMap, parameters, retType);
    }

    public static Signature variable(Mapping mapping, String sig) {
        SigArg type = readSigClass(mapping, sig);
        return new TypeSignature(type);
    }

    private static List<SigArg> readSigArgs(Mapping mapping, String argStr) {
        char[] carr = argStr.toCharArray();
        List<SigArg> parameters = new ArrayList<>();
        // Skip if no args
        if (argStr.length() > 0) {
            int i = 0;
            int array = 0;
            while (i < carr.length) {
                char c = carr[i];
                SigArg arg = null;
                if (c == '[') {
                    array++;
                } else if (c == 'T') {
                    arg = (new SigArgGeneric(Character.toString(carr[i + 1])));
                    i += 2;
                } else if (c == 'L') {
                    String type = argStr.substring(i);
                    // Get the end of the type
                    int typeEndPos = type.indexOf(";");
                    // Generics are insertted between the type and the ; at the
                    // end.
                    // Need to recalculate the ';' position if there are
                    // generics in the way.
                    if (type.contains("<") && typeEndPos > type.indexOf("<")) {
                        int cutPos = type.indexOf("<");
                        String cut = type.substring(cutPos, type.lastIndexOf(">") + 1);
                        typeEndPos = cut.length() + 1;
                    } else {
                        type = type.substring(0, typeEndPos + 1);
                    }
                    arg = readSigClass(mapping, type);
                    i += type.length() - 1;
                } else {
                    arg = (new SigArgPrimitive(Character.toString(c)));
                }
                if (arg != null) {
                    // Wrap in array if needed
                    while (array > 0) {
                        arg = new SigArgArray(arg);
                        array--;
                    }
                    parameters.add(arg);
                }
                i++;
            }
        }
        return parameters;
    }

    private static SigArg readSigClass(Mapping mapping, String type) {
        if (type.contains("<")) {
            int aa = type.indexOf("<");
            int bb = type.lastIndexOf(">");
            String typeCopy = type.substring(1, aa) + type.substring(bb + 1, type.length() - 1);
            List<SigArg> args = readSigArgs(mapping, type.substring(aa + 1, bb));
            return new SigArgClass(mapping.getClassName(typeCopy), args);

        } else {
            char firstChar = type.charAt(0);
            if (firstChar == 'T') {
                return new SigArgGeneric(type.substring(1, type.length() - 1));
            } else {
                return new SigArgClass(mapping.getClassName(type.substring(1, type.length() - 1)), null);
            }
        }
    }

}