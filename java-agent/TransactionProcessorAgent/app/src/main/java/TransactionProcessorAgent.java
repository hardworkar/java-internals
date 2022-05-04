import java.lang.instrument.Instrumentation;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;

import javassist.CtClass;
import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.expr.ExprEditor;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.ClassPool;

public class TransactionProcessorAgent {
    public static void premain(String agentArgument, Instrumentation instrumentation) throws NotFoundException, CannotCompileException, IOException {
        System.out.println("Agent started");
		instrumentation.addTransformer(new ClassTransformer());
	}

    static class ClassTransformer implements ClassFileTransformer {
        private static int count = 0;

        @Override
        public byte[] transform(ClassLoader loader, 
                                String className,
                                Class<?> classBeingRedefined, 
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer) {
                // зовется при загрузке каждого класса
                System.out.println("load class: " + className.replaceAll("/", "."));
                System.out.println(String.format("loaded %s classes", ++count));


                /*
                ClassPool pool = ClassPool.getDefault();
                //pool.removeClassPath(new ClassClassPath(classBeingRedefined));
                pool.insertClassPath(new ByteArrayClassPath(className, classfileBuffer));
                CtClass currentCtClass = null;
                try {
                    currentCtClass = pool.getCtClass(className);
                    System.out.println("=== Found class: " + currentCtClass.getName());
                    CtConstructor [] constrs = currentCtClass.getConstructors();
                    Arrays.stream(constrs).forEach(x -> {
                        try {
                            x.insertAfter("System.out.println(\"" + className + "\");");
                        } catch (CannotCompileException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (NotFoundException e) {
                    throw new RuntimeException(e);
                }

                try {
                    return currentCtClass.toBytecode();
                } catch (IOException | CannotCompileException e) {
                    throw new RuntimeException(e);
                }
                */
            ClassPool cp = ClassPool.getDefault();
            CtClass ct;
            try {
                ct = cp.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtConstructor [] constrs = ct.getConstructors();
                Arrays.stream(constrs).forEach(x -> {
                    try {
                        x.insertAfter("System.out.print(\"called constructor of: \"); System.out.println(\"" + className + "\");");
                    } catch (CannotCompileException e) {
                        throw new RuntimeException(e);
                    }
                });


                if(className.contains("TransactionProcessor")){
                    CtMethod[] methods = ct.getDeclaredMethods();
                    Arrays.stream(methods).forEach(x -> {
                        try {
                            String field_name = "__start" + x.getName();
                            CtField field = new CtField(CtClass.longType, field_name, ct);
                            field.setModifiers(Modifier.PRIVATE | Modifier.STATIC);
                            ct.addField(field);

                            x.insertBefore(field_name + " = System.currentTimeMillis();");
                            String to_insert_after = """
                                    long finish = System.currentTimeMillis();
                                    long timeElapsed = finish - """ + field_name + ";" +
                                    "System.out.println(\"" + x.getName() + " finished, time: " + "\"+ timeElapsed + \"" + "\");";
                            x.insertAfter(to_insert_after);
                        } catch (CannotCompileException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
                }
                return ct.toBytecode();
            } catch (IOException | RuntimeException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
