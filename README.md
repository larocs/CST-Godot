# CST-Godot

This is the official implementation of CST-Godot: Bridging the Gap Between Game Engines and Cognitive Agents.

This work aims to create a bridge between the areas of cognitive architectures and game development through the implementation of a framework that facilitates the development of agents with the aid of the Cognitive Systems Toolkit (CST) on the open-source game engine Godot. This project provides two examples as a starting point to developing cognitive architectures with this framework and is built with the theoretical basis to allow the same ideas to be applied in different contexts. This works takes heavy inspiration from examples found in the [CST official website](https://cst.fee.unicamp.br/).

## The general proposed framework

CST-Godot combines the idea of the division between agent and environment common in the AI research field with design patterns common in game development. We propose that experiments should be divided in three main components: the environment, the space with which the agent interacts; the body, the mechanisms with which the agent interacts with the environment; and the mind, the cognitive core of the agent. For instance, in an experiment in which a car drives down a road, the road would be environment, the car's engine, wheels and steering wheel would be the body, and what drives the car would be the mind.

This structure forms a hierarchy of complexity, in which the environment is the least complex component, as it should be the least time consuming to implement, and the mind is the most complex component, as it should be the most time consuming to implement. We also take advantage of composition, as the mind should be instanced as a component of the body and the body, as a component of the environment. This organizational structure ensures that each component can be used independently of the functionality of the ones more complex than it and, more importantly, that we can create different experiments by changing what components we instance at each level. For instance, in the car-driving-down-a-road experimentm described earlier, we should be able to instance no cars or many different cars in the same environment without issue; we should also be able to instance the same mind in different cars without issues, like creating different types of drivers that drive in different ways.

As such, CST-Godot is built to allow the creation of different experiments with as little new code as possible, the same way we may be able to make different levels in videogames by rearranging the different components that make up a level. A diagram of the framework is provided below.

<p align="center">
  <img src="https://user-images.githubusercontent.com/63124011/190865715-2a3f5937-9221-4074-bbdb-4c24b8acc6f7.png">
</p>

## Implementations

As CST is a Java-based toolkit, and Godot natively only supports C#, C++ and GDScript, joining the two of them isn't a trivial problem. In this section, we describe two different experiments built with CST-Godot, as well as their implementations.

### The WS3D-inspired experiment

This specific experiment was an implementation of the [WS3D application](https://cst.fee.unicamp.br/examples/ws3dexample), one of CST's demo experiments, with CST-Godot. It implements an agent that must collect apples in an open environment.

In CST-Godot, Godot's rendering and physical libraries were used to refine the look and feel of the project, and allow it to be exported to Android platforms. We present the framework described by implementing a single mind within two distinct experiments, one bidimensional and the other tridimensional. In the context of the compositional structure of the experiment, the location in which the agent is instanced is the environment (the arquipelago with medkits in the bidimensional experiment and the neighbourhood with apples in the tridimensional one), the vehicle is the body (the plane in the bidimensional experiment and the truck in the tridimensional one), and the cognitive core was the same for both agents.

<p align="center">
  <b>Watch the video!</b>
</p>

https://user-images.githubusercontent.com/63124011/190866614-dc507050-10e2-417d-9edb-20631926cf0f.mp4

This experiment was the first implementation of CST-Godot. It uses [Godot's Android plugin functionality](https://docs.godotengine.org/en/stable/tutorials/platform/android/android_plugin.html), as such, it can only run on Android platforms. The Android part of its implementation can be found in `Godot-Implementation/AndroidProject/CollectorAgentMind/`, while the Godot part of its implementation can be found in `Godot-Implementation/GodotProject/PortCST/`

### The Frogger-inspired experiment

This experiment is an adaptation of the classic game Frogger, built to test the capability of CST-Godot in implementing classic AI techniques. We implemented a reinforcement learning algorithm as part of a larger cognitive architecture to allow a frog to cross a street while avoiding moving cars.

In the context of the compositional structure of the project, the environment is the street, the body is the frog, and the mind is what controls the frog. This project was built so that the size of the road that needs to be crossed could be changed independently of the agent.

<p align="center">
  <b>Watch the video!</b>
</p>

https://user-images.githubusercontent.com/63124011/190867698-00951fd8-c6cc-4cea-abd8-07e19cc02161.mp4

This experiment was implemented by building the mind in [Spring](https://spring.io/) and calling it through the body as a Web API. This implementation creates a small overhead in the form of the time taken to perform HTTPS calls, but that was ruled insignificant for this specific experiment. As the implementation of this mind doesn't depend on Godot, it could potentially be used to implement the ideas of Godot-CST in other game engines. The code for this experiment can be found in `Godot-Implementation/WebApi/godotrl/`

