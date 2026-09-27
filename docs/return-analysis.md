# Return Module Analysis — GameZone Unicesar

This document contains the team's answers to the five guiding questions
required before designing the return module, which lets GameZone
Unicesar register product returns formally, keep the stock consistent,
and generate a monthly balance of sales and returns.

## About the Return class

### 1. A return is a new entity of the system that refers to an existing sale. What type of relationship exists between the Return class and the Sale class? Is it inheritance, association, aggregation, or composition? Justify.

The relationship is a **unidirectional association**: each `Return`
references exactly one `Sale` through its private attribute
`originalSale`, while one `Sale` can be referenced by zero or more
returns (a sale can receive several partial returns).

It is not **inheritance**, because a return is not a kind of sale: it
does not share the sale's structure or behavior, it only needs to know
which sale it refers to.

It is not **composition**, because the sale does not belong to the
return and their life cycles are independent: the sale exists before the
return is created and keeps existing whatever happens to its returns.

It is not **aggregation** either, because a return is not a container
made of sales; it simply points to one sale that already exists in the
system, and the reference cannot change: `Return` has a getter
(`getOriginalSale()`) but no setter for it.

The association is unidirectional: `Return` knows its sale, but `Sale`
does not keep a list of its returns. When the returns of a sale are
needed, `ReturnService.viewReturnsBySale(saleId)` finds them by
filtering. This keeps `Sale` unchanged and avoids a circular dependency
between both classes.

To make this reference possible, sales now have a unique id
(`SALE-1`, `SALE-2`, ...) assigned by `SaleService`, which is stored in
`sales.txt` and used by `ReturnRepository` to resolve the original sale
when loading returns.

### 2. A return may contain only some of the products of the original sale, not necessarily all of them. How is this represented in the attributes of the Return class? What is stored in the returned products attribute?

`Return` has its own attribute `returnedProducts` (`List<Product>`),
separate from the product list of the sale. It stores **only the units
that are being returned**, which can be a subset of the products of the
original sale:
- It contains one element per returned unit. If a sale had two copies of
  the same game and the customer returns both, the product appears twice.
- The elements are the same `Product` objects of the sale (taken from
  `sale.getProducts()`), so the refund is calculated with the prices of
  the products that were sold.
- The list given to the constructor is copied, and `getReturnedProducts()`
  returns a copy, so the returned products cannot be modified from
  outside the class.

The refund amount (`refundAmount`) is calculated from this list with
`calculateRefundAmount()`, which the constructor calls automatically.

The rule that the list must be a real subset of the sale is enforced by
`ReturnService`: every product must belong to the sale, and each unit
can only be returned once, even across several returns of the same
sale. For example, if a sale had one "Zelda", a second return of that
game is rejected. In the data file, the list is stored as the ids of the
returned products.

## About the business rules and the layers

### 3. The business rule states that returns can only be registered within 30 days after the sale. In which layer of the system is this validation located and why? Which Java mechanism is used to calculate the difference between two dates?

The rule is **defined in the model** and **enforced in the service
layer**:
- `Sale.canBeReturned()` decides whether the sale is still within the
  return window. It belongs in `Sale` because the sale date is its own
  attribute (Information Expert principle), so the rule is written in
  one place.
- `ReturnService.registerReturn(...)` calls `sale.canBeReturned()` and
  throws an `IllegalArgumentException` if the window has expired. This
  is where the rule is enforced, because registering a return is a
  business operation, and the service layer guarantees the rule is
  applied every time, no matter which interface is used.
- `ConsoleUI` also calls `canBeReturned()` before asking for the
  products, only to warn the user early. It does not replace the
  validation in the service.

The dates are compared with the **`java.time` API** (Java 8+), which
represents dates as immutable `LocalDate` objects:

```
LocalDate deadline = date.plusDays(30);
return !LocalDate.now().isAfter(deadline);
```

`plusDays(30)` calculates the last valid day in calendar days (handling
months of different lengths and leap years), and `isAfter` compares it
with today, so the 30th day is still allowed. The same difference can
also be expressed as a number of days with
`ChronoUnit.DAYS.between(saleDate, LocalDate.now())`.

### 4. Returning products increases the stock. Which existing method of the system is reused for this operation, and in which class is it invoked from the return module? Why is it important to reuse existing methods instead of duplicating the stock update logic?

The stock is updated through `ProductService`, the service that already
manages the inventory. The Workshop 1 system already had
`reduceStock(productId, amount)`, used when a sale is registered. For
returns, its counterpart `restoreStock(productId, quantity)` was added
to the same class, following the same approach: it finds the product
with `findById`, changes its stock with `setStock`, and saves the
products with `ProductPersistence.save`, all of them existing methods.

`restoreStock` is invoked from `ReturnService.registerReturn(...)`,
once for every returned unit, after all the validations pass.

Reusing the existing methods is important because:
- **There is a single place that changes the stock.** `ProductService`
  keeps the list of products and saves the file after every change. If
  `ReturnService` modified the stock by itself, it could change the
  product and forget to save it, or save an outdated list, leaving the
  inventory inconsistent (the same problem the business had with manual
  adjustments).
- **It respects the layered architecture.** `ReturnService` works
  through another service instead of accessing `ProductPersistence` or
  the file directly.
- **Changes are made once.** If the stock rules change in the future
  (for example, a maximum stock), they are updated in `ProductService`
  and apply to sales and returns alike.

### 5. The monthly balance report needs to consolidate information from two different modules (sales and returns). In which service class is this report located, and why is this location consistent with the layered architecture? Which dependencies does this class need to generate it?

The report is located in `ReturnService`, with three methods:
- `calculateMonthlySalesTotal(month, year)`: the total of the sales of
  that month, using the final total of each sale (after the promotion
  discount), which is the money that actually came in.
- `calculateMonthlyReturnsTotal(month, year)`: the total refunded by the
  returns registered in that month.
- `generateMonthlyBalance(month, year)`: the difference between both.

This location is consistent with the layered architecture because
consolidating data and applying business calculations is the
responsibility of the service layer. `ReturnService` already owns the
list of returns and already depends on `SaleService` to validate
returns, so it can reach both sources of information. It obtains the
sales through `SaleService.listAll()` instead of reading `sales.txt` or
using `SalePersistence`, so each module keeps control over its own data.
`ConsoleUI` only asks for the month and year and displays the three
values; it does not calculate anything.

The dependencies it needs are:
- **`SaleService`**: to obtain the sales of the period.
- **`ReturnRepository`**: to load the stored returns that are included
  in the report (and to persist new ones).
- **`ProductService`**: it is not used by the report itself, but it is a
  dependency of the class, required to restore the stock when a return
  is registered.

If more reports were added in the future, they could be moved to a
dedicated report service that depends on `SaleService` and
`ReturnService`.

## Design decisions not defined in the specification

- **Sale ids**: the specification refers to sales by id, but sales did
  not have one. `SaleService` now assigns sequential ids (`SALE-n`), and
  sales stored before this change receive an id when they are loaded.
- **Refund amount**: as the specification states, the refund is the sum
  of the list prices of the returned products, even if the original sale
  had a promotion discount.
- **Monthly sales total**: it uses the final total of each sale (after
  discount), because the report is meant to show the real income.
- **Returned units**: a unit cannot be returned twice across several
  returns of the same sale.
- **Accessories**: only products can be returned; accessories are not
  part of the return module.