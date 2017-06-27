/**
 * Copyright 2017 The GreyCat Authors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greycat.generator;

import greycat.Graph;
import greycat.Type;
import greycat.language.*;
import greycat.language.Class;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Visibility;
import org.jboss.forge.roaster.model.source.*;

class ClassTypeGenerator {

    static JavaSource[] generate(String packageName, Model model) {
        JavaSource[] sources = new JavaSource[model.classes().length];

        for (int i = 0; i < model.classes().length; i++) {
            sources[i] = generateClass(packageName, model.classes()[i]);
        }

        return sources;
    }

    private static JavaClassSource generateClass(String packageName, Class classType) {
        final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
        javaClass.setPackage(packageName);
        javaClass.setName(classType.name());

        if (classType.parent() != null) {
            javaClass.setSuperType(packageName + "." + classType.parent().name());
        } else {
            javaClass.setSuperType("greycat.base.BaseNode");
        }

        // create method
        MethodSource<JavaClassSource> create = javaClass.addMethod()
                .setName("create")
                .setVisibility(Visibility.PUBLIC)
                .setStatic(true);
        create.addParameter("long", "p_world");
        create.addParameter("long", "p_time");
        create.addParameter(Graph.class, "p_graph");
        create.setReturnType(classType.name());
        create.setBody("return (" + javaClass.getName() + ") p_graph.newTypedNode(p_world, p_time, " + javaClass.getName() + ".NODE_NAME);");


        // constructor
        MethodSource<JavaClassSource> constructor = javaClass.addMethod().setConstructor(true);
        constructor.addParameter("long", "p_world");
        constructor.addParameter("long", "p_time");
        constructor.addParameter("long", "p_id");
        constructor.addParameter(Graph.class, "p_graph");
        constructor.setBody("super(p_world, p_time, p_id, p_graph);");
        constructor.setVisibility(Visibility.PUBLIC);

        // field for node name
        javaClass.addField()
                .setVisibility(Visibility.PUBLIC)
                .setFinal(true)
                .setName("NODE_NAME")
                .setType(String.class)
                .setStringInitializer(javaClass.getCanonicalName())
                .setStatic(true);

        javaClass.addImport(Type.class);

        // attributes
        classType.properties().forEach(o -> {
            // constants
            if (o instanceof Constant) {
                Constant constant = (Constant) o;
                String value = constant.value();
                if (!constant.type().equals("String")) {
                    value = value.replaceAll("\"", "");
                }
                javaClass.addField()
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setName(constant.name())
                        .setType(TypeManager.cassName(constant.type()))
                        .setLiteralInitializer(value)
                        .setStatic(true);
            } else if (o instanceof Attribute) {
                Attribute att = (Attribute) o;
                javaClass.addField()
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setName(att.name().toUpperCase())
                        .setType(String.class)
                        .setStringInitializer(att.name())
                        .setStatic(true);

                // fields
                FieldSource<JavaClassSource> typeField = javaClass.addField()
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setName(att.name().toUpperCase() + "_TYPE")
                        .setType(int.class)
                        .setStatic(true);
                typeField.setLiteralInitializer(TypeManager.typeName(att.type()));

                // getter
                MethodSource<JavaClassSource> getter = javaClass.addMethod();
                getter.setVisibility(Visibility.PUBLIC).setFinal(true);
                getter.setReturnType(TypeManager.cassName(att.type()));
                getter.setName("get" + Generator.upperCaseFirstChar(att.name()));

                if (TypeManager.isPrimitive(att.type())) {
                    getter.setBody("return (" + TypeManager.cassName(att.type()) + ") super.get(" + att.name().toUpperCase() + ");");

                } else {
                    getter.setBody("return (" + TypeManager.cassName(att.type()) + ") super.getOrCreate(" + att.name().toUpperCase() + ", " + att.name().toUpperCase() + "_TYPE);");

                }

                // setter
                javaClass.addMethod()
                        .setVisibility(Visibility.PUBLIC).setFinal(true)
                        .setName("set" + Generator.upperCaseFirstChar(att.name()))
                        .setReturnType(classType.name())
                        .setBody("super.set(" + att.name().toUpperCase() + ", " + att.name().toUpperCase()
                                + "_TYPE,value);\nreturn this;"
                        )
                        .addParameter(TypeManager.cassName(att.type()), "value");
            } else if (o instanceof Relation) {
                Relation rel = (Relation) o;
                // field
                javaClass.addField()
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setName(rel.name().toUpperCase())
                        .setType(String.class)
                        .setStringInitializer(rel.name())
                        .setStatic(true);

                // getter
                String resultType = rel.type();
                MethodSource<JavaClassSource> getter = javaClass.addMethod();
                getter.setVisibility(Visibility.PUBLIC);
                getter.setFinal(true);
                getter.setReturnTypeVoid();
                getter.setName("get" + Generator.upperCaseFirstChar(rel.name()));
                getter.addParameter("greycat.Callback<" + resultType + "[]>", "callback");
                getter.setBody(
                        "this.traverse(" + rel.name().toUpperCase() + ",new greycat.Callback<greycat.Node[]>() {\n" +
                                "@Override\n" +
                                "public void on(greycat.Node[] nodes) {\n" +
                                resultType + "[] result = new " + resultType + "[nodes.length];\n" +
                                "for(int i=0;i<result.length;i++) {\n" +
                                "result[i] = (" + resultType + ") nodes[i];\n" +
                                "}\n" +
                                "callback.on(result);" +
                                "}\n" +
                                "});"
                );

                // addTo
                StringBuilder addToBodyBuilder = new StringBuilder();
                MethodSource<JavaClassSource> add = javaClass.addMethod();
                add.setVisibility(Visibility.PUBLIC).setFinal(true);
                add.setName("addTo" + Generator.upperCaseFirstChar(rel.name()));
                add.setReturnType(classType.name());
                add.addParameter(rel.type(), "value");
                addToBodyBuilder.append("super.addToRelation(").append(rel.name().toUpperCase()).append(", value);");
                addToBodyBuilder.append("return this;");
                add.setBody(addToBodyBuilder.toString());

                // remove
                StringBuilder removeFromBodyBuilder = new StringBuilder();
                MethodSource<JavaClassSource> remove = javaClass.addMethod();
                remove.setVisibility(Visibility.PUBLIC).setFinal(true);
                remove.setName("removeFrom" + Generator.upperCaseFirstChar(rel.name()));
                remove.setReturnType(classType.name());
                remove.addParameter(rel.type(), "value");
                removeFromBodyBuilder.append("super.removeFromRelation(").append(rel.name().toUpperCase()).append(", value);");
                removeFromBodyBuilder.append("return this;");
                remove.setBody(removeFromBodyBuilder.toString());
            } else if (o instanceof Reference) {
                Reference ref = (Reference) o;
                // field
                javaClass.addField()
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setName(ref.name().toUpperCase())
                        .setType(String.class)
                        .setStringInitializer(ref.name())
                        .setStatic(true);

                // getter
                String resultType = ref.type();
                MethodSource<JavaClassSource> getter = javaClass.addMethod();
                getter.setVisibility(Visibility.PUBLIC);
                getter.setFinal(true);
                getter.setReturnTypeVoid();
                getter.setName("get" + Generator.upperCaseFirstChar(ref.name()));
                getter.addParameter("greycat.Callback<" + resultType + ">", "callback");
                getter.setBody(
                        "this.traverse(" + ref.name().toUpperCase() + ",new greycat.Callback<greycat.Node[]>() {\n" +
                                "@Override\n" +
                                "public void on(greycat.Node[] nodes) {\n" +
                                resultType + " result = (" + resultType + ") nodes[0];\n" +
                                "callback.on(result);" +
                                "}\n" +
                                "});"
                );
                // setter
                StringBuilder addToBodyBuilder = new StringBuilder();
                MethodSource<JavaClassSource> add = javaClass.addMethod();
                add.setVisibility(Visibility.PUBLIC).setFinal(true);
                add.setName("set" + Generator.upperCaseFirstChar(ref.name()));
                add.setReturnType(classType.name());
                add.addParameter(ref.type(), "value");
                addToBodyBuilder.append("if(value != null) {");
                addToBodyBuilder.append("super.removeFromRelation(").append(ref.name().toUpperCase()).append(", value );");
                addToBodyBuilder.append("super.addToRelation(").append(ref.name().toUpperCase()).append(", value );");
                addToBodyBuilder.append("}");
                addToBodyBuilder.append("return this;");
                add.setBody(addToBodyBuilder.toString());

                // remove
                StringBuilder removeFromBodyBuilder = new StringBuilder();
                MethodSource<JavaClassSource> remove = javaClass.addMethod();
                remove.setVisibility(Visibility.PUBLIC).setFinal(true);
                String refName = Generator.upperCaseFirstChar(ref.name());
                String refType = Generator.upperCaseFirstChar(ref.type());
                remove.setName("remove" + refName);
                remove.setReturnTypeVoid();
                remove.addParameter("greycat.Callback<Boolean>", "callback");
                removeFromBodyBuilder.append(classType.name() + " self = this;");
                removeFromBodyBuilder.append("get" + refName + "(new greycat.Callback<" + refType + ">() {");
                removeFromBodyBuilder.append("@Override\n");
                removeFromBodyBuilder.append("public void on(" + refType + " result) {");
                removeFromBodyBuilder.append("self.removeFromRelation(").append(ref.name().toUpperCase()).append(", result);");
                removeFromBodyBuilder.append("callback.on(true);");
                removeFromBodyBuilder.append("}");
                removeFromBodyBuilder.append("});");
                remove.setBody(removeFromBodyBuilder.toString());
            } else if (o instanceof greycat.language.Index) {
                greycat.language.Index li = (greycat.language.Index) o;

                String indexConstant = li.name().toUpperCase();
                String indexName = li.name();
                // field for index name
                javaClass.addField()
                        .setName(indexConstant)
                        .setType(String.class)
                        .setStringInitializer(indexName)
                        .setStatic(true)
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true);
                StringBuilder indexedAttBuilder = new StringBuilder();
                for (AttributeRef attRef : li.attributes()) {
                    indexedAttBuilder.append(li.type() + "." + attRef.ref().name().toUpperCase());
                    indexedAttBuilder.append(",");
                }
                indexedAttBuilder.deleteCharAt(indexedAttBuilder.length() - 1);
                // index method
                MethodSource<JavaClassSource> indexMethod = javaClass.addMethod()
                        .setName("index" + Generator.upperCaseFirstChar(indexName))
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setReturnType("greycat.Index");
                indexMethod.addParameter(Generator.upperCaseFirstChar(li.type()), li.type().toLowerCase());

                StringBuilder indexBodyBuilder = new StringBuilder();
                indexBodyBuilder.append("greycat.Index index = this.getIndex(" + indexConstant + ");");
                indexBodyBuilder.append("if (index == null) {");
                indexBodyBuilder.append("index = (greycat.Index) this.getOrCreate(" + indexConstant + ", Type.INDEX);");
                indexBodyBuilder.append("index.declareAttributes(null, " + indexedAttBuilder.toString() + ");");
                indexBodyBuilder.append("index.update(" + li.type().toLowerCase() + ");");
                indexBodyBuilder.append("}");
                indexBodyBuilder.append("return index;");

                indexMethod.setBody(indexBodyBuilder.toString());

                // unindex method
                MethodSource<JavaClassSource> unindexMethod = javaClass.addMethod()
                        .setName("unindex" + Generator.upperCaseFirstChar(indexName))
                        .setVisibility(Visibility.PUBLIC)
                        .setFinal(true)
                        .setReturnTypeVoid();
                unindexMethod.addParameter(Generator.upperCaseFirstChar(li.type()), li.type().toLowerCase());

                StringBuilder unindexBodyBuilder = new StringBuilder();
                unindexBodyBuilder.append("greycat.Index index = this.getIndex(" + indexConstant + ");");
                unindexBodyBuilder.append("if (index != null) {");
                unindexBodyBuilder.append("index.unindex(" + li.type().toLowerCase() + ");");
                unindexBodyBuilder.append("}");

                unindexMethod.setBody(unindexBodyBuilder.toString());

                // find method
                MethodSource<JavaClassSource> find = javaClass.addMethod();
                find.setVisibility(Visibility.PUBLIC).setFinal(true);
                find.setName("find" + Generator.upperCaseFirstChar(indexName));
                find.setReturnTypeVoid();
                for (AttributeRef indexedAtt : li.attributes()) {
                    find.addParameter("String", indexedAtt.ref().name());
                }
                find.addParameter("greycat.Callback<" + li.type() + "[]>", "callback");
                StringBuilder findBodyBuilder = new StringBuilder();
                findBodyBuilder.append("greycat.Index index = this.getIndex(" + indexConstant + ");\n");
                findBodyBuilder.append("if (index != null) {");
                findBodyBuilder.append("index.find(new Callback<greycat.Node[]>() {");
                findBodyBuilder.append("@Override\n");
                findBodyBuilder.append("public void on(greycat.Node[] result) {");
                findBodyBuilder.append(li.type() + "[] typedResult = new " + li.type() + "[result.length];");
                findBodyBuilder.append("java.lang.System.arraycopy(result, 0, typedResult, 0, result.length);");
                findBodyBuilder.append("callback.on(typedResult);");
                findBodyBuilder.append("}");
                findBodyBuilder.append("},");
                findBodyBuilder.append("this.world(), this.time(),");
                for (AttributeRef indexedAtt : li.attributes()) {
                    findBodyBuilder.append(indexedAtt.ref().name() + ",");
                }
                findBodyBuilder.deleteCharAt(findBodyBuilder.length() - 1);
                findBodyBuilder.append(");");
                findBodyBuilder.append("}");
                findBodyBuilder.append("else {");
                findBodyBuilder.append("callback.on(null);");
                findBodyBuilder.append("}");

                find.setBody(findBodyBuilder.toString());
            }

        });

        return javaClass;
    }


}



