
# Knuckles Kalaha Agent
to start:
- Edit Configurations. 
- Press on the "+" symbol and select Application. 
- The main class should be at.pwd.boardgame.Main
- The Program arguments should be the full classname (including the package), so `Knuckles`.
- The "Use classpath of module" box should be `knuckles.main` or similar.

# using the automated runner
- Edit Configurations. 
- Press on the "+" symbol and select Application. 
- The main class should be at.pwd.boardgame.Main
- The Program arguments should be the full classnames (including the package), of agents you want to play against.
- The first agent is the main agent you want to test, it will play against all others.
- Workers and repetitions are hardcoded in Roadrunner.java 
- The "Use classpath of module" box should be `knuckles.main` or similar.



## Ideas to Explore
- Use Monte Carlo Tree Search.
- Add stones in depot and on your side to back propagation?
- Opening database? (H2, sqlite?)
- Prioritise Double Play?
- Prioritise Captures?
- Use probabilities and random weights
- Learn weights?




#