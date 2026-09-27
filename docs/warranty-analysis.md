# Warranty Module Analysis — GameZone Unicesar

This document contains the team's answers to the five guiding questions
required before designing the warranty module, which lets GameZone
Unicesar register the warranties of the consoles it sells, offer
extended warranties at an additional cost, and check whether a warranty
is still active.

## About the warranty hierarchy

### 1. The two warranty types share common attributes (dates, associated product) but also have different attributes and behavior (duration, coverage, cost). How is this reflected in the design of the class hierarchy? Which object-oriented mechanism allows each warranty type to have its own duration without duplicating code?

The common part is placed in the abstract class `Warranty`:
- The attributes shared by every warranty: `id`, `product`, `sale`,
  `startDate`, and `endDate`.
- The behavior that does not depend on the type: the constructor (which
  validates the data and calculates the end date), the getters,
  `isActive(LocalDate date)`, and `generateWarrantyCertificate()`.

What changes between the types is declared as abstract methods and
implemented by each concrete subclass:

| Method | `BasicWarranty` | `ExtendedWarranty` |
|---|---|---|
| `getDurationInMonths()` | 6 | 12 |
| `getWarrantyType()` | "Basic Warranty" | "Extended Warranty" |
| `getAdditionalCost()` | 0.0 | 10% of the product price |

The mechanism is **inheritance combined with polymorphism** (method
overriding with dynamic binding). The end date is calculated only once,
in the constructor of `Warranty`, with the duration returned by the
concrete subclass:

```
this.endDate = startDate.plusMonths(getDurationInMonths());
```

At runtime, Java executes the `getDurationInMonths()` of the actual
object, so each subclass gets its own end date without repeating the
calculation. This is an example of the **Template Method** pattern: the
base class defines the steps, and the subclasses provide only the part
that varies. In the same way, `generateWarrantyCertificate()` is written
once in `Warranty` and shows the correct type and cost for each
subclass through `getWarrantyType()` and `getAdditionalCost()`.

Adding a third type of warranty would only require a new subclass that
implements the three abstract methods.

## About the business rules and the layers

### 2. The business rule states that only consoles generate an automatic basic warranty, not video games. In which layer of the system is this decision located, and which Java mechanism is used to check the actual type of a product? Justify.

The decision is located in the **service layer**:
- `SaleService`, while registering a sale, goes through the products of
  the sale and assigns a warranty only to those that are consoles
  (private method `assignWarranties`).
- `WarrantyService` validates the same rule again in
  `assignBasicWarranty` and `assignExtendedWarranty`, throwing an
  `IllegalArgumentException` if the product is not a console. This way
  the rule is protected even if another part of the system calls these
  methods directly.

It belongs in the service layer because deciding which products receive
a warranty is a business rule. The model classes (`Product`, `Console`,
`VideoGame`) should not know about warranties, and the console menu only
asks the seller about the extended warranty for the consoles that the
service will accept.

The actual type of a product is checked with the **`instanceof`
operator**, which checks at runtime whether an object belongs to a class
(or one of its subclasses):

```
if (!(product instanceof Console)) {
    continue;
}
```

The sale stores its items as a `List<Product>`, so the declared type is
always `Product`; `instanceof` reveals whether each object is really a
`Console` or a `VideoGame`. The same operator is used in `ConsoleUI` to
ask about extended warranties only for consoles.

An alternative, more polymorphic design would be a method such as
`Product.isWarrantyEligible()` overridden by each product type. We kept
`instanceof`, as the specification indicates, to avoid modifying the
existing product hierarchy from Workshop 1 and to keep the rule in one
place in the service layer.

### 3. The duration of each warranty type is different (6 or 12 months). How is the expiration date calculated in each subclass? Should this calculation be done in the warranty constructor or in a separate method? Justify.

The subclasses do not calculate the date themselves: each one only
returns its duration (6 or 12). The calculation is done once, in the
constructor of `Warranty`:

```
this.endDate = startDate.plusMonths(getDurationInMonths());
```

`plusMonths` works with calendar months and adjusts to the last day of
the month when the day does not exist. For example, a basic warranty
that starts on 2026-08-31 ends on 2027-02-28.

We calculate it **in the constructor** because:
- **Every warranty is valid from the moment it is created.** A warranty
  without an end date would make `isActive` fail, and there is no moment
  in which the object exists in an incomplete state.
- **The end date cannot become inconsistent.** It depends only on the
  start date and the type, and neither can change after creation (there
  are no setters), so calculating it once is enough.
- **It is stored in one attribute and reused.** `isActive`, the
  certificate, and the listings read `endDate` instead of recalculating
  it. When warranties are loaded from `warranties.csv`, only the start
  date is stored, and the constructor recalculates the end date the same
  way.

There is one precaution: the constructor of `Warranty` calls an
overridable method (`getDurationInMonths()`) before the subclass has
initialized its own fields. This is safe here because both subclasses
return constant values. If a future subclass calculated its duration
from one of its own attributes, that attribute would not be initialized
yet, so the duration must always be a constant (or be passed to the
base constructor).

### 4. The extended warranty adds a cost of 10% of the product price to the sale total. At which point of the sale registration flow is this additional cost calculated and applied? What modifications are needed in the SaleService.registerSale method?

A new overload receives the list of consoles that must have an extended
warranty:

```
public Sale registerSale(Client client, Seller seller, List<Product> products,
```

The flow of this method is:
1. Validate the sale: at least one product, and every id in
   `productIdsWithExtendedWarranty` must be a console of the sale.
2. Validate the stock of every product and accessory, and then reduce it.
3. Create the sale and assign its id (`SALE-n`).
4. **Assign the warranties and calculate their cost** (`assignWarranties`):
   each console receives an extended warranty if it was requested, or a
   basic warranty otherwise. The cost returned by `getAdditionalCost()`
   of every extended warranty is added up and stored with
   `sale.setWarrantyCost(...)`.
5. Apply the best active promotion.
6. Save the sale.

The cost is calculated in step 4, after the sale has an id (the
warranties reference it) and before it is saved. It is calculated per
unit: two consoles with extended warranty add 10% of the price twice.

Other modifications that were needed:
- A new constructor of `SaleService` that injects `WarrantyService`. The
  previous constructors are kept and create a service without
  warranties.
- The previous `registerSale` methods are kept: the one with accessories
  calls the new overload without extended warranties, and the one with
  only products also assigns basic warranties, so every sale with
  consoles generates them.
- `Sale` received the attribute `warrantyCost`, and
  `calculateFinalTotal()` became subtotal − discount + warranty cost. The
  receipt shows the warranty cost in its own line.
- `SalePersistence` stores the warranty cost as an optional field at the
  end of each sale line, so previous sales still load.

The promotion discount is calculated on the subtotal (`calculateTotal()`),
so the cost of the extended warranties is not discounted. The monthly
balance, which uses `calculateFinalTotal()`, includes the income from
warranties without further changes.

### 5. The "warranties expiring soon" query requires iterating over all warranties and filtering those whose end date is within the next 30 days. In which class is this method located and what dependencies does it need? Why is this location consistent with the layered architecture?

The method is `listWarrantiesExpiringSoon(int daysAhead)` in
`WarrantyService`. It returns the warranties that are active today and
whose end date is on or before today plus `daysAhead`, and it rejects a
negative number of days. The number of days is a parameter, so the same
method serves "the next 30 days" or any other period chosen by the user.

It needs:
- **The list of warranties**, which `WarrantyService` loads through its
  injected `WarrantyRepository`. The repository, in turn, uses
  `SalePersistence` to resolve the sale and product of each stored
  warranty.
- **The current date** (`LocalDate.now()`).
- **The model methods** `Warranty.isActive(date)` and `getEndDate()`.

This location is consistent with the layered architecture because
filtering and selecting data according to a business rule is the job of
the service layer:
- `Warranty` knows whether it is active on a given date (its own data),
  but it does not know the other warranties.
- `WarrantyRepository` only reads and writes the file; it does not decide
  which warranties are relevant.
- `ConsoleUI` only asks the user for the number of days and prints the
  result.

`WarrantyRepository` resolves sales through `SalePersistence` instead of
`SaleService`. Since `SaleService` depends on `WarrantyService`, using
`SaleService` in the repository would create a circular dependency and
none of the three objects could be created first in `Main`. Using the
persistence class also respects the direction of the layers
(persistence does not depend on services).

## Design decisions not defined in the specification

- **One warranty per console**: the extended warranty replaces the basic
  one instead of being added to it, because it covers the same and more.
  This keeps `findWarrantyByProduct(productId, saleId)` unambiguous.
- **Several units of the same console**: each unit receives its own
  warranty, and if an extended warranty is requested for a console id,
  it applies to every unit of that console in the sale.
- **Warranty cost and promotions**: the cost of the extended warranties
  is added after the discount and is not discounted.
- **Warranty ids**: `WarrantyService` generates sequential ids
  (`WAR-1`, `WAR-2`, ...), since the assign methods do not receive one.
- **Language**: warranty types and certificates are in English
  ("Basic Warranty", "Extended Warranty"), following the team decision
  for all user-facing text. The file uses `BASIC`/`EXTENDED` as the
  discriminator, independent of the display name.
- **Returns**: returning a console does not change its warranty, and the
  extended warranty cost is not refunded (out of the scope of this
  module).