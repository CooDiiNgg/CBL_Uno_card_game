# UNO – Java Swing

A desktop UNO game for 2–4 players: you against 1–3 computer opponents, each set to **Easy** or **Hard**.

## Requirements

- **JDK 8 or newer** (developed and tested with JDK 21). Check with `java -version` and `javac -version`.
- No external libraries or build tools. Everything uses the standard Java library (Swing).

## Building

Run the commands from the **project root** (the folder that contains `src`, `resources` and `saves`).

**Linux / macOS**

```bash
javac -d out $(find src/uno -name "*.java")
```

**Windows (PowerShell)**

```powershell
javac -d out (Get-ChildItem -Recurse src/uno -Filter *.java).FullName
```

This compiles everything into the `out` folder.

## Running

From the project root:

```bash
java -cp out uno.Main
```

## How to play

1. **Start Game** → enter your name, add up to three computer players and pick **Easy** or **Hard** for each one (Hard is the Minimax bot).
2. Click a highlighted card to play it, or click the deck to draw. Wild cards ask you to choose a colour.
3. **Save** stores the game in `saves/`. **Load Game** on the start screen restores it.

Rules used: match colour or number/symbol. Skip, Reverse and +2 work as normal, and with two players Reverse acts as a Skip. +2/+4 can be stacked, and the first player who can't stack draws the whole stack. Drawing a card ends your turn. The first player with no cards left wins.

## Optional: training and testing the Hard bot

```bash
java -cp out uno.bot.MinimaxBot train 20000   # self-play training, updates the weights file
java -cp out uno.bot.MinimaxBot test 1000     # win rate of the Hard bot against the Easy bot
```