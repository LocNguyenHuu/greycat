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
package greycat.ml;

import greycat.Graph;
import greycat.Node;
import greycat.ml.algorithm.profiling.GaussianNode;
import greycat.ml.algorithm.profiling.GaussianSlotNode;
import greycat.ml.neuralnet.FlatNeuralNode;
import greycat.ml.neuralnet.NeuralNodeEmpty;
import greycat.plugin.ActionFactory;
import greycat.plugin.NodeFactory;
import greycat.Action;
import greycat.Type;
import greycat.ml.algorithm.anomalydetector.InterquartileRangeOutlierDetectorNode;
import greycat.ml.algorithm.profiling.GaussianMixtureNode;
//import org.mwg.ml.algorithm.profiling.GaussianTreeNode;
import greycat.ml.algorithm.regression.LiveLinearRegressionNode;
import greycat.ml.algorithm.regression.PolynomialNode;
import greycat.ml.algorithm.regression.actions.ReadContinuous;
import greycat.ml.algorithm.regression.actions.SetContinuous;
import greycat.ml.neuralnet.NeuralNode;
import greycat.structure.StructurePlugin;

public class MLPlugin extends StructurePlugin {

    @Override
    public void start(Graph graph) {
        super.start(graph);
        graph.actionRegistry()
                .declaration(ReadContinuous.NAME)
                .setParams(Type.STRING)
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return new ReadContinuous((String) params[0]);
                    }
                });
        graph.actionRegistry()
                .declaration(SetContinuous.NAME)
                .setParams(Type.STRING, Type.STRING)
                .setFactory(new ActionFactory() {
                    @Override
                    public Action create(Object[] params) {
                        return new SetContinuous((String) params[0], (String) params[1]);
                    }
                });

        graph.nodeRegistry()
                .declaration(PolynomialNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new PolynomialNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(FlatNeuralNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new FlatNeuralNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(GaussianNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new GaussianNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(GaussianSlotNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new GaussianSlotNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(GaussianMixtureNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new GaussianMixtureNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(LiveLinearRegressionNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new LiveLinearRegressionNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(InterquartileRangeOutlierDetectorNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new InterquartileRangeOutlierDetectorNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(NeuralNode.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new NeuralNode(world, time, id, graph);
                    }
                });
        graph.nodeRegistry()
                .declaration(NeuralNodeEmpty.NAME)
                .setFactory(new NodeFactory() {
                    @Override
                    public Node create(long world, long time, long id, Graph graph) {
                        return new NeuralNodeEmpty(world, time, id, graph);
                    }
                });
    }
}
