package listeners;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AnnotationTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod) {
        if (testMethod != null && testMethod.getName().equals("runScenario")) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }
}
