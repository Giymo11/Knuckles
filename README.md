
# How to Run
- Start the GUI
- Add jar file: Knuckles.jar
- Class name: Knuckles


# Additional Info
In the source zip, we also included the runner we used
to test our implementations (Roadrunner.java) as well as
the generator for the Opening Book (Liber.java). 


To build on previously found results, we consulted the literature and found the following papers especially helpful:

"Trade-Offs in Sampling-Based Adversarial Planning", Ramanujan et. al., 2011
"Monte Carlo Tree Search with Heuristic Evaluations using Implicit Minimax Backups", Lanctot et. al., 2014


# Using the automated runner
- Edit Configurations. 
- Press on the "+" symbol and select Application. 
- The main class should be at.pwd.boardgame.Main
- The Program arguments should be the full classnames (including the package), of agents you want to play against.
- The first agent is the main agent you want to test, it will play against all others.
- Workers and repetitions are hardcoded in Roadrunner.java 
- The "Use classpath of module" box should be `knuckles.main` or similar.
