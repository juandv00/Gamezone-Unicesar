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

### 8. A5 — Proportional refund for discounted sales
- **Date:** 2026-10-11
- **Tool:** Claude (Anthropic)
- **Phase and branch:** Phase 4 – `fix/return-discounted-refund`
- **Objective:** Stop refunding the list price of returned items when the original sale had a promotion, since the customer would get back more than he paid.
- **Query:** How to calculate the refund of each returned item proportionally to the discount of the original sale, and how to show it in the return receipt.
- **Response:** The refund of each item is `price × (1 − discount / subtotal)`, where the discount and subtotal come from the original sale (`getDiscountAmount()` and `calculateTotal()`); if the subtotal is 0 the factor is 1. The receipt shows, for each item, the list price, the proportional discount, and the refunded amount.
- **Decision:** Accepted. The calculation applies to products and accessories (A4 was already merged). The extended warranty cost is not part of the subtotal, so it does not change the factor. Tested: a sale with subtotal 1,000,000 and discount 100,000; returning items of 200,000 and 300,000 refunds 180,000 and 270,000; a sale without discount refunds the list price.
- **Related commit:** `fix: refund returned items proportionally to the sale discount`


  





















































