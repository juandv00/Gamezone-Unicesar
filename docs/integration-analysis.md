# Integration Analysis — GameZone Unicesar

This document describes how the four modules validated separately in
Requirements 1 to 4 (accessories, promotions, returns, and warranties)
were integrated into a single system in Requirement 5. For each
integration adjustment (A1–A7) it explains the situation or problem, its
cause, and the solution applied. It also records the decisions the team
made where the specification left room for interpretation, and the
incidents of the process.

## Integration order

The modules were integrated in five phases. A phase started only after
every branch of the previous phase was merged into `develop` and deleted
from the remote repository.

| Phase | Module | Adjustments | Pull Requests |
|---|---|---|---|
| 1 | Accessories | — | `feature/accessory-module` |
| 2 | Promotions | A1 | #28 (accessory documentation), #29 (accessory data file), #30 (A1) |
| 3 | Warranties | A2, A3 | #31 (A3), #34 (A2) |
| 4 | Returns | A4, A5, A6, A7 | #36 (A6), #37 (A4), #38 (A5), #39 (A7) |
| 5 | Closing | A8, A9 | `refactor/return-repository-dependencies`, `docs/integration-documentation` (A8), `develop` → `main` (A9) |

The order follows the dependencies between modules: accessories extend
the catalog used by the other modules, promotions work on sales that
already include accessories, warranties change `registerSale` and the
sale total, and returns must revert the effect of all the previous
modules.

## Names used in this project

The specification uses two class names that have an equivalent class in
our code base, created in the workshop before the requirements:

| Specification | Our code | Reason |
|---|---|---|
| `SaleRepository` | `SalePersistence` | The workshop persistence classes are named `*Persistence`. The classes added later follow the `*Repository` name of each requirement. |
| `ConsoleMenu` | `ConsoleUI` | The console interface of the workshop. |

## Integration adjustments

### A1 — Category discount for accessories

- **Type:** new feature — `feature/accessory-category-discount` (Developer 1).
- **Situation:** Requirement 2 limited the target category of
  `CategoryDiscount` to `VIDEOGAME` and `CONSOLE`. With accessories in the
  sales, the store needs promotions on accessories too.
- **Cause:** the promotion module was designed before accessories were
  part of a sale, and `calculateDiscount` only went through the products
  of the sale.
- **Solution:**
    - `CategoryDiscount` accepts `ACCESSORY` as target category. In
      `calculateDiscount`, every `Accessory` of the sale is recognized as
      part of that category.
    - `PromotionService.registerCategoryDiscount` rejects any category
      other than `VIDEOGAME`, `CONSOLE`, or `ACCESSORY`.
    - `ConsoleUI` offers the accessory option when a category promotion is
      registered.
    - `data/promotions.csv` includes the promotion `PROMO4` "Accessories
      25%", active during the week of work.

### A2 — Circular dependency in the warranty module

- **Type:** fix — `fix/warranty-circular-dependency` (Developer 2).
- **Problem:** the specification of Requirement 4 produces the cycle
  `SaleService → WarrantyService → WarrantyRepository → SaleService`.
  With constructor injection, none of them can be created first in
  `Main`.
- **Cause:** `WarrantyRepository` needs the sales to rebuild each
  warranty while loading the file. If it obtains them from `SaleService`,
  which depends on `WarrantyService` to assign warranties, the cycle
  appears. Our Requirement 4 version had already avoided the cycle by
  making the repository read the sales through `SalePersistence`, but the
  repository still built the `Sale` and `Product` references itself,
  which A2 assigns to the service.
- **Solution:**
    - `WarrantyRepository` stores and loads only the ids of the sale and
      the product. `loadAll()` returns `WarrantyRecord` objects (a Java
      record with the type, id, sale id, product id, and start date).
    - `WarrantyService` receives `WarrantyRepository`, `SalePersistence`,
      and `ProductService` through its constructor, and resolves each record
      into a `BasicWarranty` or an `ExtendedWarranty`.
    - `Main` creates `SalePersistence` and `WarrantyService` before
      `SaleService`.
    - `warranty-class-diagram.md` shows the new dependencies.

### A3 — Unified sale registration flow

- **Type:** refactor — `refactor/unified-sale-registration` (Technical Leader).
- **Situation:** Requirements 1, 2, and 4 changed
  `SaleService.registerSale` separately. Once integrated, the order of
  the operations changes the result (for example, whether the discount is
  applied before or after the warranty cost).
- **Cause:** each requirement added its own overload or step without a
  single, defined order.
- **Solution:** the existing `registerSale` overloads and the new
  `registerSaleByItemIds` delegate to one private method, `processSale`,
  which follows the required order:
    1. Validate that the sale has at least one item.
    2. Resolve each item as a product or an accessory and validate its
       stock.
    3. Create the sale and calculate the subtotal (`calculateTotal()`).
    4. Apply the best active promotion
       (`PromotionService.findBestPromotionFor`), calculated only on the
       subtotal.
    5. Assign a basic warranty to each console, or an extended one when it
       was requested, and add up the extended cost.
    6. Final total: subtotal − discount + extended warranty cost
       (`calculateFinalTotal()`).
    7. Reduce the stock through `ProductService` or `AccessoryService`,
       depending on the item type.
    8. Persist the sale.

  `Sale.generateReceipt` shows the subtotal, the discount with the
  promotion name, the extended warranty cost, and the final total. In
  `ConsoleUI`, products and accessories are entered in the same list of
  item ids, and the seller is asked about the extended warranty of each
  console.
- **Behavior changes:**
    - The stock is now reduced in step 7, after the promotion and the
      warranties. Since it is validated in step 2, a rejected sale still
      leaves the inventory untouched.
    - A sale with only accessories is allowed, because A3 requires "at
      least one item". This replaces the rule of Requirement 1 (a sale
      needs at least one product).
    - The warranties are saved by `WarrantyService` when they are assigned
      in step 5, because its assign methods persist each new warranty.

### A4 — Returning accessories

- **Type:** fix — `fix/return-accessory-stock` (Developer 2).
- **Problem:** the return module only restored stock through
  `ProductService.restoreStock`, which does not manage accessories. When
  an accessory was returned, its stock was not restored.
- **Cause:** Requirement 3 was designed for products only; accessories
  were out of the return module.
- **Solution:**
    - `AccessoryService.restoreStock(accessoryId, quantity)`, equivalent to
      the method of `ProductService`.
    - `Return` keeps a separate list, `returnedAccessories`, with a new
      constructor; the previous constructor delegates to it with an empty
      list.
    - `ReturnService` receives `AccessoryService` through its constructor.
      `registerReturn` resolves each id as a product of the sale first and
      then as an accessory of the sale, counts the returnable units of
      products and accessories separately, and restores the stock through
      the service of each item type.
    - The returns file stores the ids of all the returned items, and the
      accessories are resolved from the sale when the returns are loaded.
    - `ConsoleUI` lists the accessories of the sale when a return is
      registered.
- **Note:** this replaces the decision of Requirement 3 (accessories were
  not returnable).

### A5 — Refund of discounted sales

- **Type:** fix — `fix/return-discounted-refund` (Developer 1).
- **Problem:** `Return.calculateRefundAmount` added up the list prices of
  the returned items. If the sale had a promotion, the customer received
  more money than they paid.
- **Cause:** Requirement 3 defined the refund as the sum of the list
  prices, before promotions and returns were integrated.
- **Solution:**
    - Each item is refunded in proportion to the discount of the sale:
      `price × (1 − discount / subtotal)`. The private methods
      `paidFactor()` and `refundFor(listPrice)` calculate it.
    - `generateReturnReceipt` shows, for each item, the list price, the
      proportional discount, and the refund.
    - Example: in a sale with a subtotal of 1,000,000 and a discount of
      100,000, an item of 200,000 refunds 180,000.
- **Note:** this replaces the decision of Requirement 3 (refund at list
  prices).

### A6 — Monthly balance report

- **Type:** fix — `fix/monthly-balance-report` (Developer 2).
- **Problem:** the report must show the total sales, the total returns,
  and the net balance, but the specification of Requirement 3 only
  defined `generateMonthlyBalance`, which returns the net balance.
- **Cause:** the interface needed three values and the service published
  only one.
- **Solution:**
    - `ReturnService.calculateMonthlySales(int month, int year)` and
      `calculateMonthlyReturns(int month, int year)`.
      `generateMonthlyBalance` keeps its signature and returns the
      difference between both.
    - The sales total uses the final total of each sale (with discounts and
      extended warranties).
    - `ConsoleUI` shows the three values.
- **Note:** our Requirement 3 version already calculated the three values
  with the names `calculateMonthlySalesTotal` and
  `calculateMonthlyReturnsTotal`; A6 renamed them to the names of the
  specification and updated `return-analysis.md` and
  `return-class-diagram.md`.

### A7 — Warranty cancellation when a console is returned

- **Type:** new feature — `feature/return-warranty-cancellation` (Developer 2).
- **Situation:** no requirement defined what happens to the warranty of a
  returned console. In the integrated system, a returned console cannot
  keep an active warranty.
- **Cause:** returns (Requirement 3) and warranties (Requirement 4) were
  designed separately.
- **Solution:**
    - `WarrantyService.cancelWarranties(productId, saleId)` removes the
      warranty of the product in that sale, saves the change, and returns
      the refundable cost: 0 for a basic warranty and the additional cost
      for an extended one.
    - `ReturnService.registerReturn` calls it for every returned console,
      after all validations pass, and adds the total to the refund.
    - `Return` stores that amount (`warrantyRefund`), adds it in
      `calculateRefundAmount`, and shows it in the receipt as "Cancelled
      extended warranties". The returns file stores it as an optional last
      field, so the returns saved before A7 still load.
- **Decisions:**
    - Each unit of a console has its own warranty, so `cancelWarranties`
      cancels one warranty per call, and `ReturnService` calls it once per
      returned unit. If it cancelled all of them, returning one of two
      consoles would leave the other without a warranty.
    - The return reason is validated before any warranty is cancelled, so
      a rejected return never removes a warranty.
    - The cost of the extended warranty is refunded in full, because the
      promotion discount is calculated only on the subtotal of the items
      and never reduced the warranty cost.
- **Note:** this replaces the decision of Requirement 4 (returning a
  console did not change its warranty).

## Additional adjustment: return persistence without service dependencies

- **Type:** refactor — `refactor/return-repository-dependencies` (Developer 2).
- **Problem:** `ReturnRepository` (persistence layer) received
  `SaleService` and `ProductService` (service layer) to resolve the sale
  and the products when loading returns. This broke the dependency
  direction required by the workshop (ui → service → persistence →
  model).
- **Cause:** the design of Requirement 3 resolved the references inside
  the repository, the same situation that A2 fixed for warranties.
- **Solution:** the same approach as A2. `ReturnRepository` returns
  `ReturnRecord` objects (return id, date, sale id, item ids, reason,
  and warranty refund) and has no dependencies. `ReturnService` resolves
  each record when it is created. The file format did not change.
- **Verification:** the returns stored before the change, including
  lines without the warranty refund field, load with the same values.

## Verification of the integrated system

The complete scenario of the presentation was tested with the real
application, with every adjustment merged and the preloaded data of
`data/`:

1. A sale with a PlayStation 5 (2,500,000), a video game (200,000), and
   a DualSense controller (300,000), with an extended warranty for the
   console. Of the four active promotions, "Back to school 10%" gives
   the highest discount (300,000; the video game promotion gives 40,000,
   the accessory promotion 75,000, and the bulk promotion does not apply
   because the sale has two products). The receipt showed the subtotal
   (3,000,000), the discount with the promotion name (−300,000), the
   extended warranty (+250,000), and the final total (2,950,000).
2. The extended warranty `WAR-1` appeared among the active warranties.
3. A partial return of the console and the controller: the console
   refunded 2,250,000 and the controller 270,000 (list price minus the
   proportional 10% discount), plus 250,000 for the cancelled extended
   warranty, for a total of 2,770,000.
4. After restarting the application, the stock of the console and the
   controller had been restored (the video game kept its reduced stock),
   no warranty remained, and the return was listed with the same values.
5. The monthly balance showed total sales of 2,950,000, total returns of
   2,770,000, and a net balance of 180,000.

Each adjustment was also tested in its own Pull Request before it was
approved.

## Process incidents

- **PR #35 was merged into `main` instead of `develop`.** The A6 Pull
  Request was opened with `main` as its base, and the branch was deleted
  after the merge. The branch was restored from the closed Pull Request,
  a new Pull Request (#36) was opened with `develop` as its base, and it
  was merged. `main` was not reverted, because its content was the same
  as `develop` at that point; it is updated with the integrated version
  in A9. Since then, the base of every Pull Request is checked before
  approving ("into develop").
- **Merge conflict in A5.** The A5 branch was created from an outdated
  local `develop`, so its Pull Request could not be merged automatically
  (`Return.java` and the AI log had changed in A4). Developer 1 merged
  `origin/develop` into his branch, kept his version of `Return.java`
  (which already included A4), resolved the log by hand, and fixed in a
  later commit some conflict markers left in the log. Since then, every
  branch starts with `git checkout develop` and `git pull`.
- **Formatting changes from the IDE.** When code was pasted, the IDE
  sometimes reformatted whole files (one case changed 445 lines of
  `ConsoleUI`). Before every commit, `git diff --stat` was checked
  against the expected number of lines, and the original formatting was
  restored when needed.

## Decisions of earlier requirements replaced by Requirement 5

| Requirement | Earlier decision | Integrated system |
|---|---|---|
| 1 | A sale needs at least one product. | A sale needs at least one item (product or accessory) — A3. |
| 3 | Only products can be returned. | Products and accessories can be returned — A4. |
| 3 | The refund is the sum of the list prices. | Each item is refunded in proportion to the sale discount — A5. |
| 4 | Returning a console does not change its warranty. | Its warranty is cancelled and the extended cost is refunded — A7. |
| 4 | `WarrantyRepository` resolves the sale and product of each warranty. | It stores ids; `WarrantyService` resolves them — A2. |

The analysis documents of each requirement (`accessory-analysis.md`,
`promotion-analysis.md`, `return-analysis.md`, and
`warranty-analysis.md`) answer the guiding questions of that
requirement and describe its original design. When they disagree with
this document, this document describes the integrated system.

## Documentation of the integrated system

- `integrated-class-diagram.md` — a single class diagram with the four
  layers and every module.
- `layers-diagram.md` — the four layers and the dependencies between
  them, updated with every module.
- `README.md` — every operation and business rule of the integrated
  system.