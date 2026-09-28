# Accessory Module Analysis — GameZone Unicesar

This document gathers the design analysis of the accessory module
(Requirement 1), which lets GameZone Unicesar sell controllers, cables,
and memories together with video games and consoles. The original answer
about the accessory hierarchy was written as question 12 of
`analysis.md`; it is included here, together with the rest of the
module's design decisions, so each module of the integrated system has
its own analysis file.

## 1. Should accessories be integrated into the existing product hierarchy (extending Product), or should they form an independent hierarchy?

Accessories were modeled as an **independent hierarchy**: an abstract
class `Accessory`, extended by `Controller`, `Cable`, and `Memory`,
instead of extending `Product`.

Although accessories share attributes with products (`id`, `title`,
`price`, and `stock`), they also have behavior that does not apply to
video games or consoles: controllers and memories keep a list of
compatible consoles, a relationship that only makes sense for
accessories. Placing them under `Product` would either force that
behavior into a class where it does not belong, or leave the `Product`
subclasses with operations that have no meaning for them.

Keeping a separate hierarchy also protected the existing model.
`Product`, `ProductService`, and the product persistence remained
unchanged, so the module could be developed in parallel and integrated
additively.

The cost of this decision is some duplication of the common attributes
and of the stock logic between both hierarchies. It could be reduced in
the future by extracting a shared abstraction (for example, a `Sellable`
interface with `getId()`, `getPrice()`, and `getStock()`) implemented by
both `Product` and `Accessory`, so a sale could handle a single list of
items.

## 2. How is the accessory hierarchy designed?

The abstract class `Accessory` contains:
- The common private attributes `id`, `title`, `price`, and `stock`, with
  their getters and setters.
- Two abstract methods that every subclass implements in its own way:
  `getType()` (returns `"Controller"`, `"Cable"`, or `"Memory"`) and
  `getDescription()` (a text built with the attributes of each type).
- `equals` and `hashCode` based on the id, so two accessory objects with
  the same id are considered the same accessory.

Each subclass adds only its own attributes:
- **Controller**: `connectionType` (enum `ConnectionType` with `WIRELESS`
  and `WIRED`) and a list of compatible console ids.
- **Cable**: `lengthInMeters` (validated to be greater than 0) and
  `connectorType`.
- **Memory**: `capacityInGB`, `memoryType` (enum `MemoryType` with `SD`,
  `MICRO_SD`, and `INTERNAL_CARD`), and a list of compatible console ids.

The enums restrict the possible values at compile time, instead of
accepting any text. Polymorphism lets the rest of the system list and
filter accessories through `getType()` and `getDescription()` without
checking their concrete class.

## 3. How is the compatibility between accessories and consoles represented?

`Controller` and `Memory` keep a `List<String>` with the ids of the
compatible consoles, and offer `addCompatibleConsole(consoleId)`,
`isCompatibleWith(consoleId)`, and `getCompatibleConsoleIds()` (which
returns a copy). The list stores ids instead of `Console` objects, so the
model does not depend on how products are loaded, and the relationship
can be saved to a file as plain text.

`AccessoryService.findAccessoriesCompatibleWith(consoleId)` answers the
query "which accessories work with this console", and
`AccessoryService.addCompatibleConsole(accessoryId, consoleId)` registers
a compatibility and saves it. The console menu validates that the given
id belongs to an existing `Console` before registering it. `Cable` does
not have console compatibility.

## 4. How are accessories persisted?

`AccessoryRepository` saves all accessories in one file (`data/accessories.csv`
in the integrated system), one accessory per line, with a tab as the
delimiter and a **type discriminator** in the first field (`CONTROLLER`,
`CABLE`, or `MEMORY`). When loading, the discriminator decides which
concrete subclass is created, and the rest of the fields are read
according to that type. If the file does not exist, an empty list is
returned. The model classes contain no file access, following the
layered architecture.

## 5. How are accessories integrated into sales?

The integration was made without changing the existing behavior of
sales:
- `Sale` received a second list, `accessories`, in addition to
  `products`. The original constructors were kept and create sales with
  an empty accessory list, and `calculateTotal()` adds the prices of both
  lists.
- `SaleService` validates the stock of products and accessories before
  reducing anything, so a rejected sale leaves the inventory untouched.
  The stock of each kind of item is updated through its own service
  (`ProductService` or `AccessoryService`).
- `SalePersistence` stores the accessory ids of each sale as an optional
  field, so the sales stored before the module existed still load.
- `ConsoleUI` has an "Accessory management" submenu (register each type,
  list, filter by type, console compatibility, and stock update), and
  the sale registration lets the user add accessories.

In the integrated system, accessories also take part in the other
modules: promotions can target the `ACCESSORY` category (adjustment A1),
and accessories can be returned with their stock restored (adjustment
A4). These changes are described in `integration-analysis.md`.