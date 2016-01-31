package org.kevoree.modeling.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.kevoree.modeling.KCallback;
import org.kevoree.modeling.KObject;
import org.kevoree.modeling.cloudmodel.CloudModel;
import org.kevoree.modeling.cloudmodel.CloudUniverse;
import org.kevoree.modeling.cloudmodel.CloudView;
import org.kevoree.modeling.cloudmodel.Node;
import org.kevoree.modeling.cloudmodel.meta.MetaNode;
import org.kevoree.modeling.memory.manager.DataManagerBuilder;

/**
 * @ignore ts
 */
public abstract class BaseKSchedulerTest {

    public abstract KScheduler createScheduler();

    @Test
    public void test() {
        final CloudModel model = new CloudModel(DataManagerBuilder.create().withScheduler(createScheduler()).build());
        model.connect(new KCallback<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                CloudUniverse dimension0 = model.newUniverse();
                CloudView time0 = dimension0.time(0l);
                final Node root = time0.createNode();
                root.setName("root");
                Node n1 = time0.createNode();
                n1.setName("n1");
                Node n2 = time0.createNode();
                n2.setName("n2");
                root.addChildren(n1);
                root.addChildren(n2);
                root.getRelation(MetaNode.REF_CHILDREN, new KCallback<KObject[]>() {
                    @Override
                    public void on(KObject[] kObjects) {
                        Assert.assertEquals(kObjects.length, 2);
                    }
                });
                KObject[] syncResult = root.syncGetRelation(MetaNode.REF_CHILDREN);
                Assert.assertEquals(syncResult.length, 2);
            }
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
