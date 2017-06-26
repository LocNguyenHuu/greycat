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
package greycat.modeling.example;

import greycat.*;
import model.*;
import model.Constants;

public class HelloModelingWorld {


    public static void main(String[] args) {
        //add  the generated plugin to a GraphBuilder
        GraphBuilder builder = new GraphBuilder().withPlugin(new ModelPlugin());
        //build the graph
        Graph graph = builder.build();
        graph.connect(new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {

                // typed nodes
                Building building = Building.create(0, 0, graph);
                building.setName("building");
                System.out.println("building name " + building.getName());

                Room r1 = Room.create(0, 0, graph);
                r1.setName("room_1");
                Room r2 = Room.create(0, 0, graph);
                r1.setName("room_2");
                Room specialRoom = Room.create(0, 0, graph);
                specialRoom.setName("specialRoom");

                Room localIdxRoom = Room.create(0, 0, graph);
                localIdxRoom.setName("localIdxRoom");

                // relations
                building.addToRooms(r1);
                building.addToRooms(r2);
                building.getRooms(rooms -> System.out.println("found " + rooms.length + " rooms"));

                // references
                building.setSpecialRoom(specialRoom);
                building.getSpecialRoom(room -> System.out.println("special room " + specialRoom));

                // custom types
                SmartCity smartCity = SmartCity.create(0, 0, graph);
                GPSPosition pos = smartCity.getLocation();
                pos.setLng(5.43d);
                pos.setLat(3.23d);
                System.out.println(pos);

                // local index
                building.indexLocalIndex(localIdxRoom);
                building.findLocalIndex("localIdxRoom", rooms -> {
                    System.out.println("found " + rooms.length + " room with local index");
                });

                // global index
                Buildings.declareIndex(graph, 0, new Callback<Boolean>() {
                    @Override
                    public void on(Boolean result) {
                        Buildings.updateIndex(graph, 0, 0, building, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                Buildings.find(graph, 0, 0, "building", new Callback<Building[]>() {
                                    @Override
                                    public void on(Building[] result) {
                                        System.out.println("found " + result.length + " building with global index");

                                    }
                                });
                            }
                        });
                    }
                });


                // override constant
                Constants.CONSTANT_TO_OVERRIDE = "new value";

            }
        });
    }
}

