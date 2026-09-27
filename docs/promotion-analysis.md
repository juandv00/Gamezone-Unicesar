# Promotion Module Analysis — GameZone Unicesar

This document contains the team's answers to the five guiding questions
required before designing the promotion module, which lets GameZone
Unicesar run promotional campaigns and apply them automatically when a
sale is registered.

## About the promotion hierarchy

### 1. The three promotions have different calculation rules but share common attributes and behavior. How is this reflected in the design of the class hierarchy? Which object-oriented mechanism allows each type of promotion to calculate its discount differently without the rest of the system knowing the concrete types?

The common part of every promotion is placed in the abstract class
`Promotion`: the attributes `id`, `name`, `startDate`, and `endDate`, and
the behavior that does not depend on the type of promotion, such as
`isActive(LocalDate date)`. Each type of promotion is a concrete subclass
that adds only its particular attributes:
- **PercentageDiscount**: `percentage`.
- **CategoryDiscount**: `percentage` and `targetCategory` (`"VIDEOGAME"` or
  `"CONSOLE"`).
- **BulkPurchaseDiscount**: `minimumQuantity` and `percentage`.

The mechanism that lets each type calculate its discount differently is
**polymorphism**, through method overriding and dynamic binding. Every
subclass overrides `calculateDiscount(Sale sale)` with its own rule, and
the rest of the system works only with references of type `Promotion`.
For example, `PromotionService.findBestPromotionFor(Sale sale)` iterates
over a `List<Promotion>` and calls `calculateDiscount(sale)` on each one;
at runtime, Java executes the implementation of the concrete class of
each object. In the same way, `ConsoleUI` lists promotions through their
`toString()` without checking their type.

Thanks to this, adding a fourth type of promotion only requires a new
subclass (and its persistence format); the selection logic in
`PromotionService` and the discount application in `SaleService` do not
change.

### 2. The base class Promotion cannot implement the discount calculation method because each type has different logic. How is this method declared in the base class, and what does this declaration guarantee regarding the subclasses?

The method is declared as **abstract** in `Promotion`, with a signature
but no body:

```java
public abstract double calculateDiscount(Sale sale);
```

Declaring an abstract method forces the class itself to be abstract, so
`Promotion` cannot be instantiated directly: a "generic" promotion with
no calculation rule cannot exist in the system.

This declaration also guarantees, at compile time, that every concrete
subclass implements `calculateDiscount`. If a subclass does not override
it, the compiler rejects the code unless that subclass is also declared
abstract. As a result, any object referenced as a `Promotion` is
guaranteed to know how to calculate its discount, which is exactly what
`PromotionService` relies on when it compares promotions polymorphically.
The subclasses mark their implementations with `@Override`, so the
compiler also verifies that the signature matches the one declared in
the base class.

## About the business rules and the layers

### 3. The business rule states that only the promotion with the highest discount is applied. In which class is this selection logic located, and why is this location consistent with the layered architecture principle? Why should this logic NOT be in the Sale class or in the console menu?

The selection logic is located in `PromotionService`, in the method
`findBestPromotionFor(Sale sale)`. It evaluates every promotion active on
the date of the sale, calculates the discount each one would grant, and
returns the one with the highest discount, or `null` if no promotion
applies or the best discount is zero. `SaleService` then invokes this
method while registering a sale and stores the result in the sale.

This location is consistent with the layered architecture because
choosing the best promotion is a **business rule**, and business rules
belong to the service layer. `PromotionService` is also the class that
owns the list of registered promotions (loaded through
`PromotionRepository`), so it has all the information needed to make the
decision without any other layer accessing persistence.

The logic should not be in `Sale` because `Sale` is a model class: it
represents a single transaction and should not know about all the
promotions that exist in the system. To choose the best one, `Sale` would
need access to the list of promotions, which means depending on the
service or persistence layer. That would break the rule that the model
must not depend on any other layer. `Sale` is only responsible for
storing the promotion applied to it and calculating its own totals.

The logic should not be in the console menu because the user interface
is only responsible for reading input and showing results. If the rule
lived in `ConsoleUI`, it would only be applied when a sale is registered
through the console; any other entry point (for example, a future
graphical interface) would have to duplicate it or could skip it. It
also could not be reused or tested independently of the menus. Keeping
it in the service layer guarantees that the rule is applied every time
a sale is registered, regardless of the interface.

### 4. What modifications are necessary in the Sale class and in the generateReceipt method so that the receipt shows the discount applied? Do these modifications break any existing behavior of the system?

The following changes were made to `Sale`, all of them additive:
- Two private attributes: `appliedPromotionName` (`String`) and
  `discountAmount` (`double`), with their getters and setters. The setter
  of `discountAmount` rejects negative values.
- `calculateFinalTotal()`, which returns the subtotal minus the discount.
- `hasDiscount()`, which indicates whether a promotion was applied.
- `generateReceipt()`, which returns a text receipt listing each item
  (products and accessories) with its price, followed by the subtotal,
  the discount applied with the name of the promotion (or "none"), and
  the final total.

These modifications do not break the existing behavior because:
- `calculateTotal()` was not changed and still returns the subtotal (the
  sum of all item prices). Existing code that used it keeps working, and
  the promotions calculate their discount on top of it. The final total
  was placed in a new method instead of changing the meaning of the old
  one.
- The new attributes start as `null` and `0`, so a sale without a
  promotion behaves exactly as before: its final total equals its
  subtotal, and its `toString()` produces the same text as before.
- The existing constructors of `Sale` and `SaleService` were kept.
  `SaleService` received a new constructor that injects
  `PromotionService`; the old ones still create a service that registers
  sales without discounts.
- `SalePersistence` stores the promotion name and discount as optional
  fields at the end of each line, only when a discount was applied. Sales
  stored before the promotion module existed are still loaded correctly.

### 5. Active promotions are determined by comparing the current date with the start and end dates of each promotion. Where is this validation performed (in the Promotion class, in PromotionService, or in both)? Justify.

The validation is performed in **both** classes, each with a different
responsibility.

`Promotion` defines **what it means to be active**, through
`isActive(LocalDate date)`: it returns `true` when the given date is
within the range from `startDate` to `endDate`, both included. This
belongs in `Promotion` because the dates are its own attributes
(Information Expert principle), so the rule is written once and every
subclass inherits it.

`PromotionService` decides **which date to check and uses the result**
to filter the promotions:
- `listActivePromotions()` checks each promotion against the current date
  (`LocalDate.now()`), to show the campaigns active today.
- `findBestPromotionFor(Sale sale)` checks each promotion against the
  date of the sale (`sale.getDate()`), since the business rule applies
  the promotions active on the date of the sale.

This division keeps the rule of validity inside the model while the
service applies it according to the context of each operation. If the
service compared the dates directly, the rule would be duplicated in
every method that needs it; if `Promotion` decided which date to use, the
model would depend on the context of the operation.

In addition, the console validates when a promotion is registered that
the start date is not after the end date, so that promotions with an
impossible validity range are not created.