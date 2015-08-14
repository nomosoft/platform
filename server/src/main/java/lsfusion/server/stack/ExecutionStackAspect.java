package lsfusion.server.stack;

import lsfusion.base.BaseUtils;
import lsfusion.base.ConcurrentWeakHashMap;
import lsfusion.base.col.ListFact;
import lsfusion.base.col.interfaces.immutable.ImList;
import lsfusion.base.col.interfaces.mutable.MList;
import lsfusion.server.context.ThreadLocalContext;
import lsfusion.server.data.HandledException;
import lsfusion.server.logics.ServerResourceBundle;
import lsfusion.server.logics.property.ActionProperty;
import lsfusion.server.logics.property.ExecutionContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.Stack;

@Aspect
public class ExecutionStackAspect {
    private static ConcurrentWeakHashMap<Thread, Stack<ExecutionStackItem>> executionStack = new ConcurrentWeakHashMap<>();
    private static ConcurrentWeakHashMap<Throwable, String> exceptionCauseMap = new ConcurrentWeakHashMap<>();
    
    @Around("execution(lsfusion.server.logics.property.actions.flow.FlowResult lsfusion.server.logics.property.ActionProperty.execute(lsfusion.server.logics.property.ExecutionContext)) && args(executionContext)")
    public Object execution(final ProceedingJoinPoint joinPoint, final ExecutionContext executionContext) throws Throwable {
        ExecuteActionStackItem item = new ExecuteActionStackItem(executionContext, (ActionProperty) joinPoint.getTarget());
        return processStackItem(joinPoint, item);
    }

    @Around("execution(@lsfusion.server.stack.StackMessage * *.*(..))")
    public Object callTwinMethod(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        AspectStackItem item = new AspectStackItem(thisJoinPoint);
        return processStackItem(thisJoinPoint, item);
    }

    // тут важно что цикл жизни ровно в стеке, иначе утечку можем получить
    private Object processStackItem(ProceedingJoinPoint joinPoint, ExecutionStackItem item) throws Throwable {
        boolean pushedMessage = false;
        boolean pushedStack = false;
        Stack<ExecutionStackItem> stack = getOrInitStack();
        if (item != null) {
            stack.push(item);
            pushedStack = true;
            if (presentItem(item)) {
                ThreadLocalContext.pushActionMessage(item);
                pushedMessage = true;
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            if (!(e instanceof HandledException && ((HandledException)e).willDefinitelyBeHandled()) && !exceptionCauseMap.containsKey(e)) {
                String stackString = getStackString();
                if (stackString != null) {
                    exceptionCauseMap.put(e, stackString);
                }
            }
            throw e;
        } finally {
            if (pushedStack) {
                stack.pop();
            }
            if (pushedMessage) {
                ThreadLocalContext.popActionMessage();
            }
        }
    }

    public Stack<ExecutionStackItem> getOrInitStack() {
        Stack<ExecutionStackItem> stack = getStack(Thread.currentThread());
        if (stack == null) {
            stack = new Stack<>();
            executionStack.put(Thread.currentThread(), stack);
        }
        return stack;
    }

    public static Stack<ExecutionStackItem> getStack() {
        return getStack(Thread.currentThread());
    }
    
    public static Stack<ExecutionStackItem> getStack(Thread thread) {
        return executionStack.get(thread);
    }

    public static String getStackString(Throwable t) {
        String result = exceptionCauseMap.get(t);
        exceptionCauseMap.remove(t);
        return result != null ? result : getStackString();
    }
    
    public static String getStackString() {
        return getStackString(Thread.currentThread(), false); // не concurrent по определению
    }
    
    public static String getStackString(Thread thread, boolean checkConcurrent) {
        String result = "";
        Stack<ExecutionStackItem> stack = getStack(thread);
        if (stack != null) {
            if(checkConcurrent) {
                while (true) {
                    try {
                        result = getStackString(stack);
                        break;
                    } catch (ConcurrentModificationException e) {
                    }
                }
            } else {
                result = getStackString(stack);
            }
        }
        return BaseUtils.nullEmpty(result);    
    }

    private static String getStackString(Stack<ExecutionStackItem> stack) {
        String result = "";
        ListIterator<ExecutionStackItem> itemListIterator = stack.listIterator(stack.size());
        while (itemListIterator.hasPrevious()) {
            ExecutionStackItem item = itemListIterator.previous();
            if (presentItem(item)) {
                if (!result.isEmpty()) {
                    result += "\n";
                }
                result += item;
            }
        }
        return result;
    }

    private static boolean presentItem(ExecutionStackItem item) {
//        return true;
        return !isLSFAction(item) || ((ExecuteActionStackItem) item).isInDelegate();
    }
    
    private static boolean isLSFAction(ExecutionStackItem item) {
        return item instanceof ExecuteActionStackItem;
    }
}
                                                      