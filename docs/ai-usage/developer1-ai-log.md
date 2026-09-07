# AI Usage Log — Developer 1 (Product Module)

I used Claude for support throughout the development of my module. These are the things I used him for:

- I had problems configuring IntelliJ (it didn't recognize my Maven project, and it wouldn't let me create packages). I showed him screenshots, and he guided me step by step until it was resolved.

- To fully understand the object-oriented design I was working with (inheritance, abstract classes, polymorphism), I asked him to explain why Product had to be abstract and why getDescription() had to be an abstract method there. I used the example he gave me as a reference to write Product, Video Game, and Console, understanding each part (the super() in the constructor, the @Override, why attributes are private with getters/setters).

- For persistence, I asked which file formats were allowed and why, and I decided to use plain text with semicolons because it's the easiest to show and explain in the presentation.

- For the service, with their help, I understood why this layer is the only one that can communicate with persistence, and how I should validate the stock before deducting it (so that the sales module can then use that method).

What I DIDN'T do: I didn't ask them to generate the class diagram, I didn't ask them for the answers to the analysis, and I didn't copy code without understanding what it did—in each class, I can explain why it's written that way.

## Session 2

**Legitimate Uses:**

- Code examples and explanations were requested for: overriding `toString()`

in the product hierarchy, adding validation to the constructor for negative prices/stocks,

and implementing `equals()`/`hashCode()` based on the product ID.

- Help was requested to understand and correct a bug when committing
  a code change (`equals`/`hashCode`) using a leftover commit message from
  a previous commit, and how to safely correct a commit message
  before submitting it (without using `force-push`, which is prohibited).

**My Own Decisions:**
- Decided which additional improvements to add (`toString`, validation,
  `equals`/`hashCode`).



















































