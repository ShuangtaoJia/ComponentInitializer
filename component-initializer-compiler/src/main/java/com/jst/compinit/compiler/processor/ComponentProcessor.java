package com.jst.compinit.compiler.processor;

import com.google.auto.service.AutoService;
import com.jst.compinit.annotation.Component;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@SupportedAnnotationTypes({"com.jst.compinit.annotation.Component"})
@AutoService(Processor.class)
public class ComponentProcessor extends AbstractProcessor {
    private Elements elementUtils;
    Filer filer;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Component.class)) {

            //接下来的代码所做的事情：被注解类所在的包名下，以 被注解类的类名$$IComponentInfoImpl 生成java文件


            //取出注解对象
            Component component = element.getAnnotation(Component.class);
            //获取包名
            PackageElement packageElement = elementUtils.getPackageOf(element);
            String packageName = packageElement.getQualifiedName().toString();
            //获取被注解类的类名
            String annotatedClassName = ((TypeElement)element).getSimpleName().toString();
            //生成的类的类名
            String generatedClassName = annotatedClassName + "$$IComponentInfoImpl";

            String literal;
            if (component.dependencies().length == 0) {
                literal = "{}";
            }else {
                literal = "{\"" + String.join("\",\"", component.dependencies()) + "\"}";
            }

            ArrayTypeName stringArray = ArrayTypeName.of(String.class);
            ClassName annotatedClass = ClassName.get((TypeElement)element);
            ClassName iComponentEntityClass = ClassName.get("com.jst.compinit", "IComponentInfo");
            ClassName iComponentClass = ClassName.get("com.jst.compinit", "IComponent");

            //getName方法
            MethodSpec getNameMethod = MethodSpec.methodBuilder("getName")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement("return $S",component.name())
                    .build();

            //getDependencies方法
            MethodSpec getDependenciesMethod = MethodSpec.methodBuilder("getDependencies")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(stringArray)
                    .addStatement("return new $T$L",stringArray,literal)
                    .build();

            //getComponent方法
            MethodSpec getComponentMethod = MethodSpec.methodBuilder("getComponent")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(iComponentClass)
                    .addStatement("return new $T()",annotatedClass)
                    .build();

            //类
            TypeSpec generatedClass = TypeSpec.classBuilder(generatedClassName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(iComponentEntityClass)
                    .addMethod(getNameMethod)
                    .addMethod(getDependenciesMethod)
                    .addMethod(getComponentMethod)
                    .build();

            try {
                JavaFile.builder(packageName, generatedClass).build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
