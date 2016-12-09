package org.mwg.core.task;

import org.junit.Assert;
import org.junit.Test;
import org.mwg.Callback;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.struct.RelationIndexed;
import org.mwg.task.ActionFunction;
import org.mwg.task.TaskContext;

import static org.mwg.core.task.Actions.*;
import static org.mwg.core.task.Actions.newTask;

public class ActionTraverseOrAttributeTest extends AbstractActionTest {

    @Test
    public void test() {
        initGraph();
        newTask()
                .then(readGlobalIndex("nodes"))
                .then(traverse("children"))
                .then(traverse("name"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Assert.assertEquals(ctx.result().get(0), "n0");
                        Assert.assertEquals(ctx.result().get(1), "n1");
                    }
                })
                .execute(graph, null);
        removeGraph();
    }

    @Test
    public void testDefaultSynthax() {
        initGraph();
        newTask()
                .then(readGlobalIndex("nodes"))
                .parse("children.name", graph)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Assert.assertEquals(ctx.result().get(0), "n0");
                        Assert.assertEquals(ctx.result().get(1), "n1");
                    }
                })
                .execute(graph, null);
        removeGraph();
    }


    @Test
    public void testParse() {
        initGraph();
        newTask()
                .parse("readGlobalIndex(nodes).traverse(children)", graph)
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Assert.assertEquals(ctx.resultAsNodes().get(0).get("name"), "n0");
                        Assert.assertEquals(ctx.resultAsNodes().get(1).get("name"), "n1");
                    }
                })
                .execute(graph, null);
        removeGraph();
    }

    @Test
    public void testTraverseIndex() {
        initGraph();
        Node node1 = graph.newNode(0, 0);
        node1.set("name", Type.STRING, "node1");
        node1.set("value", Type.INT, 1);

        Node node2 = graph.newNode(0, 0);
        node2.set("name", Type.STRING, "node2");
        node2.set("value", Type.INT, 2);

        Node node3 = graph.newNode(0, 12);
        node3.set("name", Type.STRING, "node3");
        node3.set("value", Type.INT, 3);

        Node root = graph.newNode(0, 0);
        root.set("name", Type.STRING, "root2");

        graph.index(0,0,"roots",rootIndex -> {
            rootIndex.addToIndex(root,"name");

            RelationIndexed irel = (RelationIndexed) root.getOrCreate("childrenIndexed", Type.RELATION_INDEXED);
            irel.add(node1, "name");
            irel.add(node2, "name");
            irel.add(node3, "name");

            root.travelInTime(12, new Callback<Node>() {
                @Override
                public void on(Node result) {

                    RelationIndexed irel12 = (RelationIndexed) root.getOrCreate("childrenIndexed", Type.RELATION_INDEXED);
                    irel12.add(node3, "name");
                }
            });

        });

        /*
        readIndex("rootIndex", "name=root2")
                .traverseIndex("childrenIndexed", "name","node2")
                .then(new ActionFunction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(1, context.result().size());
                        Assert.assertEquals("node2", context.resultAsNodes().get(0).get("name"));
                    }
                }).execute(graph, null);
*/

        newTask()
                .then(readGlobalIndex("rootIndex", "name", "root2"))
                .then(traverse("childrenIndexed", "name", "node3"))
                .thenDo(new ActionFunction() {
                    @Override
                    public void eval(TaskContext ctx) {
                        Assert.assertEquals(0, ctx.result().size());
                    }
                }).execute(graph, null);

        /*
        inject(12).asGlobalVar("time").setTime("{{time}}")
                .readIndex("rootIndex", "name=root2")
                .traverseIndex("childrenIndexed", "name=node2")
                .then(new ActionFunction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(1, context.result().size());
                        Assert.assertEquals("node2", context.resultAsNodes().get(0).get("name"));
                    }
                }).execute(graph, null);

        readIndex("rootIndex", "name=root2")
                .traverseIndexAll("childrenIndexed")
                .then(new ActionFunction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(2, context.result().size());
                        Assert.assertEquals("node1", context.resultAsNodes().get(0).get("name"));
                        Assert.assertEquals("node2", context.resultAsNodes().get(1).get("name"));
                    }
                }).execute(graph, null);

        inject(13).asGlobalVar("time").setTime("{{time}}")
                .readIndex("rootIndex", "name=root2")
                .traverseIndexAll("childrenIndexed")
                .then(new ActionFunction() {
                    @Override
                    public void eval(TaskContext context) {
                        Assert.assertEquals(3, context.result().size());
                        Assert.assertEquals("node1", context.resultAsNodes().get(0).get("name"));
                        Assert.assertEquals("node2", context.resultAsNodes().get(1).get("name"));
                        Assert.assertEquals("node3", context.resultAsNodes().get(2).get("name"));
                    }
                }).execute(graph, null);

                */

        removeGraph();
    }

}
